package com.tsintergy.notify;

import org.apache.poi.ss.formula.functions.T;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>
 * 持续监控接口
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/27 18:15
 */
public interface KeepingMonitor {

    /**
     * 是否应该通知
     *
     * @param instanceId 实例id
     * @return 是否应该通知
     */
    boolean shouldNotify(String instanceId);

    /**
     * 注册
     *
     * @param instanceId 实例id
     */
    void registry(String instanceId);

    /**
     * 注销
     *
     * @param instanceId 实例id
     */
    void unRegistry(String instanceId);

    /**
     * 监控
     *
     * @param keeping  状态是否持续
     * @param instanceId 实例id
     * @param monitorFun 监控回调方法
     */
    void doMonitor(boolean keeping, String instanceId, Consumer<String> monitorFun);

}
