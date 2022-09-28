package com.tsintergy.notify;

import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;

import java.time.Instant;

/**
 * <p>
 * 持续监控接口
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/27 18:15
 */
public interface RemindingMonitor {

    /**
     * 状态是否持续
     * @param instanceId 实例对象
     * @param reminding true or false
     */
    void setReminding(String instanceId, boolean reminding);

    /**
     * 设置状态开始时间
     *
     * @param instanceId 实例id
     */
    void setStartRemind(String instanceId);

    /**
     * 是否持续
     * @param instanceId 实例id
     * @return
     */
    boolean isReminding(String instanceId);

    /**
     * 是否应该通知
     *
     * @param instanceId 实例id
     * @return 是否应该通知
     */
    boolean shouldNotify(String instanceId);

    /**
     * 重置
     *
     * @param instanceId 实例id
     */
    void reset(String instanceId);

}
