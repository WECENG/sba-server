package com.tsintergy.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * <p>
 * 持续通知配置类
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/26 17:12
 */
@Configuration
@ConfigurationProperties("spring.boot.admin.notify.reminder")
@Data
public class RemindingProperties {

    /**
     * 检查周期
     */
    private Duration checkReminderInterval = Duration.ofSeconds(10);

    /**
     * 状态保持周期
     */
    private Duration keepPeriod = Duration.ofMinutes(2);

    /**
     * 消息发送周期
     */
    private Duration notificationPeriod = Duration.ofMinutes(10);

}
