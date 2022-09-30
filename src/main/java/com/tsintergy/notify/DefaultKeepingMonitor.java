package com.tsintergy.notify;

import com.tsintergy.util.DingtalkRequestUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

/**
 * <p>
 * 持续监控默认类
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/27 18:43
 */
public class DefaultKeepingMonitor implements KeepingMonitor {

    private final ConcurrentHashMap<String, Keeper> keepers = new ConcurrentHashMap<>();

    private final Duration keepingPeriod;

    private final Duration notificationPeriod;


    public DefaultKeepingMonitor(Duration notificationPeriod, Duration keepingPeriod) {
        this.notificationPeriod = notificationPeriod;
        this.keepingPeriod = keepingPeriod;
    }

    public void setLastNotify(String instanceId) {
        Instant now = Instant.now();
        Optional.ofNullable(keepers.get(instanceId)).ifPresent(item -> item.setLastNotify(now));
    }

    @Override
    public boolean shouldNotify(String instanceId) {
        Instant now = Instant.now();
        Instant startKeep = Optional.ofNullable(keepers.get(instanceId)).map(Keeper::getStartKeep).orElse(null);
        Instant lastNotify = Optional.ofNullable(keepers.get(instanceId)).map(Keeper::getLastNotify).orElse(null);
        return Objects.nonNull(startKeep) && startKeep.plus(keepingPeriod).isBefore(now) &&
                (Objects.isNull(lastNotify) || lastNotify.plus(notificationPeriod).isBefore(now));
    }

    @Override
    public void registry(String instanceId) {
        keepers.putIfAbsent(instanceId, new Keeper(Instant.now()));
    }

    @Override
    public void unRegistry(String instanceId) {
        keepers.remove(instanceId);
    }

    public void reset(String instanceId) {
        Instant now = Instant.now();
        Optional.ofNullable(keepers.get(instanceId)).ifPresent(keeper -> {
            if (Objects.isNull(keeper.getLastNotify()) || keeper.getLastNotify().plus(notificationPeriod).isBefore(now)) {
                keepers.remove(instanceId);
            } else {
                keeper.setStartKeep(null);
            }
        });
    }

    @Override
    public void doMonitor(boolean keeping, String instanceId, Consumer<String> monitorFun) {
        if (keeping) {
            registry(instanceId);
            if (shouldNotify(instanceId)) {
                setLastNotify(instanceId);
                monitorFun.accept(instanceId);
            }
        } else {
            reset(instanceId);
        }
    }

    protected static final class Keeper {

        private Instant startKeep;

        private Instant lastNotify;

        public Keeper(Instant startKeep) {
            this.startKeep = startKeep;
        }

        public Keeper(Instant startKeep, Instant lastNotify) {
            this.startKeep = startKeep;
            this.lastNotify = lastNotify;
        }

        public Instant getStartKeep() {
            return startKeep;
        }

        public void setStartKeep(Instant startKeep) {
            this.startKeep = startKeep;
        }

        public Instant getLastNotify() {
            return lastNotify;
        }

        public void setLastNotify(Instant lastNotify) {
            this.lastNotify = lastNotify;
        }
    }

}
