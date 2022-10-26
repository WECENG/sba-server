package com.tsintergy.notify;

import com.tsintergy.configure.CpuMonitorProperties;
import com.tsintergy.configure.DingtalkProperties;
import com.tsintergy.configure.JvmMonitorProperties;
import com.tsintergy.configure.RemindingProperties;
import com.tsintergy.message.CpuInfo;
import com.tsintergy.message.JvmMemoryInfo;
import com.tsintergy.util.CpuRequestUtil;
import com.tsintergy.util.DingtalkRequestUtil;
import com.tsintergy.util.JvmRequestUtil;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;
import io.micrometer.core.lang.NonNull;
import io.micrometer.core.lang.NonNullApi;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/10/25 16:06
 */
public class CpuMonitor extends DefaultKeepingMonitor implements InstanceExchangeFilterFunction {

    private final RestTemplate restTemplate;

    private final CpuMonitorProperties cpuMonitorProperties;

    private final DingtalkProperties dingtalkProperties;

    public CpuMonitor(RestTemplate restTemplate,
                      CpuMonitorProperties cpuMonitorProperties,
                      DingtalkProperties dingtalkProperties,
                      RemindingProperties remindingProperties) {
        super(remindingProperties.getNotificationPeriod(), remindingProperties.getKeepPeriod());
        this.restTemplate = restTemplate;
        this.cpuMonitorProperties = cpuMonitorProperties;
        this.dingtalkProperties = dingtalkProperties;
    }

    @Override
    @NonNull
    public Mono<ClientResponse> filter(@NonNull Instance instance,
                                       @NonNull ClientRequest request,
                                       @NonNull ExchangeFunction next) {
        return next.exchange(request).doOnSubscribe((s) -> {
            if (request.url().getPath().contains(CpuRequestUtil.HEALTH_URI)) {
                BigDecimal processCpuUsage = CpuRequestUtil.getProcessCpuUsage(restTemplate, instance.getRegistration().getManagementUrl(), request);
                BigDecimal systemCpuUsage = CpuRequestUtil.getSystemCpuUsage(restTemplate, instance.getRegistration().getManagementUrl(), request);
                CpuInfo cpuInfo = CpuInfo.builder().processCpuUsage(processCpuUsage).systemCpuUsage(systemCpuUsage).build();
                String instanceId = instance.getId().getValue();
                doMonitor(shouldAlarm(cpuInfo), instanceId, (instId) ->
                        DingtalkRequestUtil.sendDingTalkMes(restTemplate, dingtalkProperties, instance,
                                DingtalkRequestUtil.buildCpuContent(instance, cpuInfo)));
            }
        });
    }

    private boolean shouldAlarm(CpuInfo cpuInfo) {
        boolean alarm = false;
        if (Objects.nonNull(cpuMonitorProperties.getProcessCpuUsage()) &&
                Objects.nonNull(cpuInfo.getProcessCpuUsage()) &&
                cpuInfo.getProcessCpuUsage().compareTo(cpuMonitorProperties.getProcessCpuUsage()) >= 0) {
            cpuInfo.setWarningTitle(MessageFormat.format("进程CPU使用率大于{0}%", cpuMonitorProperties.getProcessCpuUsage()));
            alarm = true;
        }
        if (Objects.nonNull(cpuMonitorProperties.getSystemCpuUsage()) &&
                Objects.nonNull(cpuInfo.getSystemCpuUsage()) &&
                cpuInfo.getSystemCpuUsage().compareTo(cpuMonitorProperties.getSystemCpuUsage()) >= 0) {
            cpuInfo.setWarningTitle(MessageFormat.format("系统CPU使用率大于{0}%", cpuMonitorProperties.getSystemCpuUsage()));
            alarm = true;
        }
        return alarm;
    }
}
