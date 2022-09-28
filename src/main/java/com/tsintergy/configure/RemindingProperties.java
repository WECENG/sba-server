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

    private Duration checkReminderInterval = Duration.ofSeconds(10);

    private Duration reminderPeriod = Duration.ofMinutes(10);

}
