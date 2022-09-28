package com.tsintergy.notify;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 持续监控抽象类
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/27 18:43
 */
public abstract class AbsRemindingMonitor implements RemindingMonitor {

    private final ConcurrentHashMap<String, AbsRemindingMonitor.Reminder> reminders = new ConcurrentHashMap<>();

    private Duration reminderPeriod = Duration.ofMinutes(10);

    @Override
    public void setReminding(String instanceId, boolean reminding) {
        Optional.ofNullable(reminders.get(instanceId)).ifPresent(item -> item.setReminding(reminding));
    }

    @Override
    public void setStartRemind(String instanceId) {
        Instant now = Instant.now();
        Optional.ofNullable(reminders.get(instanceId)).ifPresent(item -> item.setStartRemind(now));
    }

    @Override
    public boolean isReminding(String instanceId) {
        return Optional.ofNullable(reminders.get(instanceId)).map(Reminder::isReminding).orElse(false);
    }

    @Override
    public boolean shouldNotify(String instanceId) {
        Instant now = Instant.now();
        boolean reminding = Optional.ofNullable(reminders.get(instanceId)).map(Reminder::isReminding).orElse(false);
        ;
        Instant startRemind = Optional.ofNullable(reminders.get(instanceId)).map(Reminder::getStartRemind).orElse(null);
        ;
        return reminding && Objects.nonNull(startRemind) && startRemind.plus(reminderPeriod).isBefore(now);
    }

    @Override
    public void reset(String instanceId) {
        reminders.remove(instanceId);
    }


    public void setReminderPeriod(Duration reminderPeriod) {
        this.reminderPeriod = reminderPeriod;
    }

    public ConcurrentHashMap<String, Reminder> getReminders() {
        return reminders;
    }

    protected static final class Reminder {

        private boolean reminding;

        private Instant startRemind;

        public Reminder(boolean reminding, Instant startRemind) {
            this.reminding = reminding;
            this.startRemind = startRemind;
        }

        public boolean isReminding() {
            return reminding;
        }

        public void setReminding(boolean reminding) {
            this.reminding = reminding;
        }

        public Instant getStartRemind() {
            return startRemind;
        }

        public void setStartRemind(Instant startRemind) {
            this.startRemind = startRemind;
        }
    }

}
