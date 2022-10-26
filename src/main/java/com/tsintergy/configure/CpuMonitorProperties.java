package com.tsintergy.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/10/25 16:14
 */
@Configuration
@ConfigurationProperties("spring.boot.admin.notify.cpu.monitor")
@Data
public class CpuMonitorProperties {

    /**
     * 进程cpu使用率，大于该值将发出警告
     */
    private BigDecimal processCpuUsage;

    /**
     * 系统cpu使用率，大于该值将发出警告
     */
    private BigDecimal systemCpuUsage;

}
