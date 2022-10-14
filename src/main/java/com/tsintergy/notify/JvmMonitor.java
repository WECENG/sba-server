package com.tsintergy.notify;

import com.tsintergy.configure.DingtalkProperties;
import com.tsintergy.configure.JvmMonitorProperties;
import com.tsintergy.configure.RemindingProperties;
import com.tsintergy.message.JvmMemoryInfo;
import com.tsintergy.util.DingtalkRequestUtil;
import com.tsintergy.util.JvmRequestUtil;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;
import io.micrometer.core.lang.NonNull;
import io.micrometer.core.lang.NonNullApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Objects;

/**
 * <p>
 * jvm信息监控
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/27 19:23
 */
@Slf4j
public class JvmMonitor extends DefaultKeepingMonitor implements InstanceExchangeFilterFunction {

    private final RestTemplate restTemplate;

    private final JvmMonitorProperties jvmMonitorProperties;

    private final DingtalkProperties dingtalkProperties;


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
                jvmMemoryInfo.getSpareHead().compareTo(jvmMonitorProperties.getSpareHeap()) <= 0) {
            alarm = true;
            jvmMemoryInfo.setWarningTitle(MessageFormat.format("剩余可用堆内存小于{0}M", jvmMonitorProperties.getSpareHeap()));
        }
        if (Objects.nonNull(jvmMonitorProperties.getSpareCommitHeap()) &&
                Objects.nonNull(jvmMemoryInfo.getCommittedHeap()) &&
                jvmMemoryInfo.getCommittedHeap().compareTo(jvmMonitorProperties.getSpareCommitHeap()) <= 0) {
            jvmMemoryInfo.setWarningTitle(MessageFormat.format("剩余可分配堆内存小于{0}M", jvmMonitorProperties.getSpareCommitHeap()));
            alarm = true;
        }
        if (Objects.nonNull(jvmMonitorProperties.getSpareMaxHeap()) &&
                Objects.nonNull(jvmMemoryInfo.getMaxHeap()) &&
                jvmMemoryInfo.getMaxHeap().compareTo(jvmMonitorProperties.getSpareMaxHeap()) <= 0) {
            jvmMemoryInfo.setWarningTitle(MessageFormat.format("剩余最大可用堆内存小于{0}M", jvmMonitorProperties.getSpareMaxHeap()));
            alarm = true;
        }
        if (Objects.nonNull(jvmMonitorProperties.getUsedNonHeap()) &&
                Objects.nonNull(jvmMemoryInfo.getUsedNonHeap()) &&
                jvmMemoryInfo.getUsedNonHeap().compareTo(jvmMonitorProperties.getUsedNonHeap()) >= 0) {
            jvmMemoryInfo.setWarningTitle(MessageFormat.format("已使用非堆空间大于{0}M", jvmMonitorProperties.getUsedNonHeap()));
            alarm = true;
        }
        if (Objects.nonNull(jvmMonitorProperties.getSpareNonHeap()) &&
                Objects.nonNull(jvmMemoryInfo.getSpareNonHeap()) &&
                jvmMemoryInfo.getSpareNonHeap().compareTo(jvmMonitorProperties.getSpareNonHeap()) <= 0) {
            jvmMemoryInfo.setWarningTitle(MessageFormat.format("剩余可用非堆空间小于{0}M", jvmMonitorProperties.getSpareNonHeap()));
            alarm = true;
        }
        return alarm;
    }

    @Override
    @NonNull
    public Mono<ClientResponse> filter(@NonNull Instance instance, @NonNull ClientRequest request, ExchangeFunction next) {
        return next.exchange(request).doOnSubscribe((s) -> {
            if (request.url().getPath().contains(JvmRequestUtil.HEALTH_URI)) {
                BigDecimal maxHeap = JvmRequestUtil.getMaxHeap(restTemplate, instance.getRegistration().getManagementUrl(), request);
                BigDecimal usedHeap = JvmRequestUtil.getUsedHeap(restTemplate, instance.getRegistration().getManagementUrl(), request);
                BigDecimal committedHeap = JvmRequestUtil.getCommittedHeap(restTemplate, instance.getRegistration().getManagementUrl(), request);
                BigDecimal maxNonHeap = JvmRequestUtil.getMaxNonHeap(restTemplate, instance.getRegistration().getManagementUrl(), request);
                BigDecimal usedNonHeap = JvmRequestUtil.getUsedNonHeap(restTemplate, instance.getRegistration().getManagementUrl(), request);
                BigDecimal spareHead = Objects.isNull(committedHeap) || Objects.isNull(usedHeap) ? null : committedHeap.subtract(usedHeap);
                BigDecimal spareCommitHead = Objects.isNull(maxHeap) || Objects.isNull(committedHeap) ? null : maxHeap.subtract(committedHeap);
                BigDecimal spareMaxHead = Objects.isNull(maxHeap) || Objects.isNull(usedHeap) ? null : maxHeap.subtract(usedHeap);
                BigDecimal sparedNonHead = Objects.isNull(maxNonHeap) || Objects.isNull(usedNonHeap) ? null : maxNonHeap.subtract(usedNonHeap);
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
                String instanceId = instance.getId().getValue();
                doMonitor(shouldAlarm(jvmMemoryInfo), instanceId, (instId) ->
                        DingtalkRequestUtil.sendDingTalkMes(restTemplate, dingtalkProperties,
                                DingtalkRequestUtil.buildJvmContent(instance, jvmMemoryInfo)));
            }
        });
    }
}
