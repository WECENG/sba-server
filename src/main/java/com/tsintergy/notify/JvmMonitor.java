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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
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
@Slf4j
public class JvmMonitor extends DefaultKeepingMonitor implements InstanceExchangeFilterFunction {

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
        super(remindingProperties.getNotificationPeriod(), remindingProperties.getKeepPeriod());
        this.restTemplate = restTemplate;
        this.jvmMonitorProperties = jvmMonitorProperties;
        this.dingtalkProperties = dingtalkProperties;
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
        return next.exchange(request).doOnSubscribe((s) -> {
            if (request.url().getPath().contains(JvmRequestUtil.HEALTH_URI)) {
                HttpHeaders headers = request.headers();
                BigDecimal maxHeap = JvmRequestUtil.getMaxHeap(restTemplate, instance.getRegistration().getManagementUrl(), headers);
                BigDecimal usedHeap = JvmRequestUtil.getUsedHeap(restTemplate, instance.getRegistration().getManagementUrl(), headers);
                BigDecimal committedHeap = JvmRequestUtil.getCommittedHeap(restTemplate, instance.getRegistration().getManagementUrl(), headers);
                BigDecimal maxNonHeap = JvmRequestUtil.getMaxNonHeap(restTemplate, instance.getRegistration().getManagementUrl(), headers);
                BigDecimal usedNonHeap = JvmRequestUtil.getUsedNonHeap(restTemplate, instance.getRegistration().getManagementUrl(), headers);
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
                String instanceId = instance.getId().getValue();
                doMonitor(shouldAlarm(jvmMemoryInfo), instanceId, (instId) -> DingtalkRequestUtil.sendDingTalkMes(restTemplate, dingtalkProperties, jvmContent));
            }
        });
    }
}
