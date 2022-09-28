package com.tsintergy.configure;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 钉钉消息通知配置类
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/25 15:31
 */
@Data
@Configuration
@ConditionalOnProperty(prefix = "spring.boot.admin.notify.dingtalk", name = "url")
@ConfigurationProperties("spring.boot.admin.notify.dingtalk")
public class DingtalkProperties {

    /**
     * 钉钉路径
     */
    private String url;

    /**
     * 钉钉token
     */
    private String token;

    /**
     * 钉钉密钥
     */
    private String secret;


}
