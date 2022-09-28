package com.tsintergy.notify;

import com.tsieframework.core.base.math.BigDecimalFunctions;
import com.tsintergy.configure.DingtalkProperties;
import com.tsintergy.configure.JvmMonitorProperties;
import com.tsintergy.configure.RemindingProperties;
import com.tsintergy.message.JvmMemoryInfo;
import com.tsintergy.util.DingtalkRequestUtil;
import com.tsintergy.util.JvmRequestUtil;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * <p>
 * jvm信息监控
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/27 19:23
 */
@Component
public class JvmMonitor extends AbsRemindingMonitor implements InstanceExchangeFilterFunction {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JvmMonitorProperties jvmMonitorProperties;

    @Autowired
    private DingtalkProperties dingtalkProperties;


    public JvmMonitor(RestTemplate restTemplate,
                      JvmMonitorProperties jvmMonitorProperties,
                      DingtalkProperties dingtalkProperties,
                      RemindingProperties remindingProperties) {
        this.restTemplate = restTemplate;
        this.jvmMonitorProperties = jvmMonitorProperties;
        this.dingtalkProperties = dingtalkProperties;
        setReminderPeriod(remindingProperties.getReminderPeriod());
    }

    /**
     * 更新持续状态
     *
     * @param instance 实例
     */
    protected void updateOrNotReminding(Instance instance) {
        String instanceId = instance.getId().getValue();
        if (!isReminding(instanceId)) {
            setReminding(instanceId, true);
            setStartRemind(instanceId);
        }
    }

    /**
     * 是否需要警告
     *
     * @param jvmMemoryInfo jvm信息
     * @return
     */
    protected boolean shouldAlarm(JvmMemoryInfo jvmMemoryInfo) {
        boolean alarm = false;
        if (Objects.nonNull(jvmMonitorProperties.getSpareHeap()) &&
                Objects.nonNull(jvmMemoryInfo.getSpareHead()) &&
                BigDecimalFunctions.lt(jvmMemoryInfo.getSpareHead(), jvmMonitorProperties.getSpareHeap())) {
            alarm = true;
        }
        if (Objects.nonNull(jvmMonitorProperties.getSpareCommitHeap()) &&
                Objects.nonNull(jvmMemoryInfo.getCommittedHeap()) &&
                BigDecimalFunctions.lt(jvmMemoryInfo.getCommittedHeap(), jvmMonitorProperties.getSpareCommitHeap())) {
            alarm = true;
        }
        if (Objects.nonNull(jvmMonitorProperties.getSpareMaxHeap()) &&
                Objects.nonNull(jvmMemoryInfo.getMaxHeap()) &&
                BigDecimalFunctions.lt(jvmMemoryInfo.getMaxHeap(), jvmMonitorProperties.getSpareMaxHeap())) {
            alarm = true;
        }
        if (Objects.nonNull(jvmMonitorProperties.getUsedNonHeap()) &&
                Objects.nonNull(jvmMemoryInfo.getUsedNonHeap()) &&
                BigDecimalFunctions.gt(jvmMemoryInfo.getUsedNonHeap(), jvmMonitorProperties.getUsedNonHeap())) {
            alarm = true;
        }
        if (Objects.nonNull(jvmMonitorProperties.getSpareNonHeap()) &&
                Objects.nonNull(jvmMemoryInfo.getSpareNonHeap()) &&
                BigDecimalFunctions.lt(jvmMemoryInfo.getSpareNonHeap(), jvmMonitorProperties.getSpareNonHeap())) {
            alarm = true;
        }
        return alarm;
    }

    @Override
    public Mono<ClientResponse> filter(Instance instance, ClientRequest request, ExchangeFunction next) {
        return next.exchange(request).doOnSubscribe((s)->{
            if (request.url().getPath().contains(JvmRequestUtil.HEALTH_URI)) {
                try {
                    getReminders().putIfAbsent(instance.getId().getValue(), new Reminder(false, Instant.now()));
                    BigDecimal maxHeap = JvmRequestUtil.getMaxHeap(restTemplate, instance.getRegistration().getServiceUrl());
                    BigDecimal usedHeap = JvmRequestUtil.getUsedHeap(restTemplate, instance.getRegistration().getServiceUrl());
                    BigDecimal committedHeap = JvmRequestUtil.getCommittedHeap(restTemplate, instance.getRegistration().getServiceUrl());
                    BigDecimal maxNonHeap = JvmRequestUtil.getMaxNonHeap(restTemplate, instance.getRegistration().getServiceUrl());
                    BigDecimal usedNonHeap = JvmRequestUtil.getUsedNonHeap(restTemplate, instance.getRegistration().getServiceUrl());
                    BigDecimal spareHead = BigDecimalFunctions.subtract(committedHeap, usedHeap);
                    BigDecimal spareCommitHead = BigDecimalFunctions.subtract(maxHeap, committedHeap);
                    BigDecimal spareMaxHead = BigDecimalFunctions.subtract(maxHeap, usedHeap);
                    BigDecimal sparedNonHead = BigDecimalFunctions.subtract(maxNonHeap, usedNonHeap);
                    JvmMemoryInfo jvmMemoryInfo = JvmMemoryInfo.builder()
                            .maxHeap(maxHeap)
                            .usedHeap(usedHeap)
                            .committedHeap(committedHeap)
                            .maxNonHeap(maxNonHeap)
                            .usedNonHeap(usedNonHeap)
                            .spareHead(spareHead)
                            .spareCommitHead(spareCommitHead)
                            .spareMaxHead(spareMaxHead)
                            .spareNonHeap(sparedNonHead)
                            .build();
                    String jvmContent = DingtalkRequestUtil.buildJvmContent(instance, jvmMemoryInfo);

                    if (shouldAlarm(jvmMemoryInfo)) {
                        updateOrNotReminding(instance);
                        if (shouldNotify(instance.getId().getValue())) {
                            DingtalkRequestUtil.sendDingTalkMes(restTemplate, dingtalkProperties, jvmContent);
                            reset(instance.getId().getValue());
                        }
                    } else {
                        reset(instance.getId().getValue());
                    }
                } catch (Exception e) {
                    //do nothing
                }
            }
        });
    }
}
