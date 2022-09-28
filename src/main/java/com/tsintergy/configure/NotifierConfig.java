package com.tsintergy.configure;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tsieframework.core.base.math.BigDecimalFunctions;
import com.tsintergy.message.JvmMemoryInfo;
import com.tsintergy.notify.DingtalkNotifier;
import com.tsintergy.notify.JvmMonitor;
import com.tsintergy.util.DingtalkRequestUtil;
import com.tsintergy.util.JvmRequestUtil;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.notify.CompositeNotifier;
import de.codecentric.boot.admin.server.notify.MailNotifier;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.RemindingNotifier;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.tsintergy.enums.SbaInstanceStatus.*;

/**
 * <p>
 * 消息通知配置
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/24 16:10
 */
@Configuration(proxyBeanMethods = false)
public class NotifierConfig {


    @Configuration
    public static class ReminderNotifierConfiguration {

        private final InstanceRepository repository;

        private final ObjectProvider<List<Notifier>> otherNotifiers;

        private final RemindingProperties remindingProperties;

        public ReminderNotifierConfiguration(InstanceRepository repository,
                                             ObjectProvider<List<Notifier>> otherNotifiers,
                                             RemindingProperties remindingProperties) {
            this.repository = repository;
            this.otherNotifiers = otherNotifiers;
            this.remindingProperties = remindingProperties;
        }

        @Bean
        public FilteringNotifier filteringNotifier() {
            CompositeNotifier delegate = new CompositeNotifier(this.otherNotifiers.getIfAvailable(Collections::emptyList));
            return new FilteringNotifier(delegate, this.repository);
        }

        @Primary
        @Bean(initMethod = "start", destroyMethod = "stop")
        public RemindingNotifier remindingNotifier() {
            //离线持续通知
            RemindingNotifier notifier = new RemindingNotifier(filteringNotifier(), this.repository);
            notifier.setReminderStatuses(new String[]{DOWN.name(), OFFLINE.name(), UP.name()});
            //10分钟通知一次
            notifier.setReminderPeriod(remindingProperties.getReminderPeriod());
            notifier.setCheckReminderInverval(remindingProperties.getCheckReminderInterval());
            return notifier;
        }
    }

    @Configuration
    @AutoConfigureBefore({AdminServerNotifierAutoConfiguration.NotifierTriggerConfiguration.class,
            AdminServerNotifierAutoConfiguration.CompositeNotifierConfiguration.class,
            ReminderNotifierConfiguration.class})
    @Lazy(false)
    public static class DingtalkNotifierConfig {

        private final InstanceRepository repository;

        public DingtalkNotifierConfig(InstanceRepository repository) {
            this.repository = repository;
        }

        @Bean
        @ConditionalOnMissingBean
        public RestTemplate getRestTemplate() {
            return new RestTemplate();
        }

        @Bean
        @ConditionalOnBean({DingtalkProperties.class, RestTemplate.class})
        public DingtalkNotifier dingtalkNotifier() {
            return new DingtalkNotifier(repository);
        }

        @Bean
        @ConditionalOnBean({DingtalkProperties.class, RestTemplate.class, JvmMonitorProperties.class, RemindingProperties.class})
        public InstanceExchangeFilterFunction jvmMonitor(JvmMonitorProperties jvmMonitorProperties,
                                                         DingtalkProperties dingtalkProperties,
                                                         RemindingProperties remindingProperties) {
            return new JvmMonitor(getRestTemplate(), jvmMonitorProperties, dingtalkProperties, remindingProperties);
        }

    }

    @Configuration
    @AutoConfigureAfter(AdminServerNotifierAutoConfiguration.MailNotifierConfiguration.class)
    public class CustomMailNotifierConfiguration implements InitializingBean {

        @Autowired(required = false)
        private MailNotifier mailNotifier;

        @Override
        public void afterPropertiesSet() throws Exception {
            if (null != mailNotifier) {
                mailNotifier.setIgnoreChanges(new String[]{"*:" + UP.name(), "*:" + UNKNOWN.name(), UNKNOWN.name() + ":*"});
            }
        }

    }


}
