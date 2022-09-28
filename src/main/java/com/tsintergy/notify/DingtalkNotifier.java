package com.tsintergy.notify;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.constant.HttpHeaderConsts;
import com.alibaba.nacos.common.http.param.MediaType;
import com.tsintergy.configure.DingtalkProperties;
import com.tsintergy.message.DingtalkMessage;
import com.tsintergy.message.DingtalkMessageBuilder;
import com.tsintergy.message.DingtalkTextMessage;
import com.tsintergy.util.DingtalkRequestUtil;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.notify.AbstractEventNotifier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static com.tsintergy.enums.SbaInstanceStatus.*;

/**
 * <p>
 * 钉钉消息通知
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/24 17:13
 */
@Slf4j
public class DingtalkNotifier extends AbstractEventNotifier {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DingtalkProperties dingtalkProperties;

    private final InstanceRepository instanceRepository;

    private final Map<InstanceId, String> lastStatuses = new HashMap<>();

    public DingtalkNotifier(InstanceRepository repository) {
        super(repository);
        instanceRepository = repository;
    }

    @Override
    public Mono<Void> notify(InstanceEvent event) {
        return super.notify(event).then(Mono.fromRunnable(() ->
                instanceRepository.find(event.getInstance()).subscribe((instance) ->
                        updateLastStatus(event, instance))));
    }

    @Override
    protected boolean shouldNotify(InstanceEvent event, Instance instance) {
        return true;
    }

    protected final String getLastStatus(InstanceId instanceId) {
        return lastStatuses.getOrDefault(instanceId, "UNKNOWN");
    }

    protected void updateLastStatus(InstanceEvent event, Instance instance) {
        if (event instanceof InstanceDeregisteredEvent) {
            lastStatuses.remove(instance.getId());
        }
        if (event instanceof InstanceStatusChangedEvent) {
            lastStatuses.put(instance.getId(), instance.getStatusInfo().getStatus());
        }
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return Mono.fromRunnable(() -> {
            if (event instanceof InstanceStatusChangedEvent & super.shouldNotify(event, instance)) {
                String lastStatus = getLastStatus(instance.getId());
                boolean lastDownOrOffline = lastStatus.equals(DOWN.name()) || lastStatus.equals(OFFLINE.name());
                boolean downOrOffline = instance.getStatusInfo().isOffline() || instance.getStatusInfo().isDown();
                boolean up = instance.getStatusInfo().isUp();
                String content = null;
                if (downOrOffline) {
                    content = DingtalkRequestUtil.buildDownOrOfflineContent(instance);
                }
                if (up && lastDownOrOffline) {
                    content = DingtalkRequestUtil.buildDownToUpContent(instance);
                }
                try {
                    DingtalkRequestUtil.sendDingTalkMes(restTemplate, dingtalkProperties, content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
