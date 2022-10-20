package com.tsintergy.notify;

import com.tsintergy.configure.DingtalkProperties;
import com.tsintergy.util.DingtalkRequestUtil;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.notify.AbstractEventNotifier;
import io.micrometer.core.lang.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static com.tsintergy.enums.SbaInstanceStatus.DOWN;
import static com.tsintergy.enums.SbaInstanceStatus.OFFLINE;

/**
 * <p>
 * 钉钉消息通知
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/24 17:13
 */
@Slf4j
public class StatusChangeNotifier extends AbstractEventNotifier {

    private final Map<InstanceId, String> lastStatuses = new HashMap<>();

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DingtalkProperties dingtalkProperties;

    private final KeepingMonitor keepingMonitor;

    private final InstanceRepository repository;

    public StatusChangeNotifier(InstanceRepository repository, KeepingMonitor keepingMonitor) {
        super(repository);
        this.repository = repository;
        this.keepingMonitor = keepingMonitor;
    }

    @Override
    @NonNull
    public Mono<Void> notify(@NonNull InstanceEvent event) {
        return super.notify(event).then(Mono.fromRunnable(() -> updateLastStatus(event)));
    }

    protected void updateLastStatus(InstanceEvent event) {
        String instanceId = event.getInstance().getValue();
        if (event instanceof InstanceDeregisteredEvent) {
            lastStatuses.remove(event.getInstance());
            keepingMonitor.unRegistry(instanceId);
        }
        if (event instanceof InstanceStatusChangedEvent) {
            repository.find(event.getInstance()).subscribe(instance -> lastStatuses.put(event.getInstance(), instance.getStatusInfo().getStatus()));
        }
    }

    @Override
    @NonNull
    protected Mono<Void> doNotify(@NonNull InstanceEvent event, @NonNull Instance instance) {
        return Mono.fromRunnable(() -> {
            if (event instanceof InstanceStatusChangedEvent) {
                String lastStatus = lastStatuses.getOrDefault(instance.getId(), "UNKNOWN");
                boolean lastDownOrOffline = lastStatus.equals(DOWN.name()) || lastStatus.equals(OFFLINE.name());
                boolean downOrOffline = instance.getStatusInfo().isOffline() || instance.getStatusInfo().isDown();
                boolean up = instance.getStatusInfo().isUp();
                String instanceId = instance.getId().getValue();
                if (downOrOffline) {
                    keepingMonitor.doMonitor(true, instanceId, (instId) ->
                            DingtalkRequestUtil.sendDingTalkMes(restTemplate, dingtalkProperties, instance,
                                    DingtalkRequestUtil.buildDownOrOfflineContent(instance)));
                }
                if (up && lastDownOrOffline) {
                    DingtalkRequestUtil.sendDingTalkMes(restTemplate, dingtalkProperties, instance,
                            DingtalkRequestUtil.buildDownToUpContent(instance));
                }
            }
        });
    }
}
