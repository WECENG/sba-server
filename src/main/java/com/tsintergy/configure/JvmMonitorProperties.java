package com.tsintergy.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * <p>
 * jvm信息监控配置类
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/27 14:11
 */
@Configuration
@ConfigurationProperties("spring.boot.admin.notify.jvm.monitor")
@Data
public class JvmMonitorProperties {

    /**
     * jvm.memory.committed - jvm.memory.used {@link java.lang.management.MemoryUsage},
     * 剩余可以用内存，小于该值将发出警告 {@link NotifierConfig.DingtalkNotifierConfig#jvmMonitor(JvmMonitorProperties,DingtalkProperties,RemindingProperties)}
     */
    private BigDecimal spareHeap;

    /**
     * jvm.memory.max - jvm.memory.committed {@link java.lang.management.MemoryUsage}
     * 剩余可以分配内存，小于该值将发出警告 {@link NotifierConfig.DingtalkNotifierConfig#jvmMonitor(JvmMonitorProperties,DingtalkProperties,RemindingProperties)}
     */
    private BigDecimal spareCommitHeap;

    /**
     * jvm.memory.max - jvm.memory.used {@link java.lang.management.MemoryUsage}
     * 剩余最大可使用内存，小于该值将发出警告 {@link NotifierConfig.DingtalkNotifierConfig#jvmMonitor(JvmMonitorProperties,DingtalkProperties,RemindingProperties)}
     */
    private BigDecimal spareMaxHeap;

    /**
     * 剩余可用非堆空间，小于该值将发出警告
     */
    private BigDecimal spareNonHeap;

    /**
     * 已使用非堆空间，大于该值将发出警告
     */
    private BigDecimal usedNonHeap;

}
