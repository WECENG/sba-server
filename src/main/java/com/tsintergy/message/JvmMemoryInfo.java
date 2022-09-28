package com.tsintergy.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * <p>
 * jvm内存信息
 * {@link java.lang.management.MemoryUsage} {@link com.tsintergy.configure.JvmMonitorProperties}
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/27 14:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JvmMemoryInfo {
    /**
     * 最大堆内存
     */
    private BigDecimal maxHeap;

    /**
     * 已使用堆内存
     */
    private BigDecimal usedHeap;

    /**
     * 已分配堆内存
     */
    private BigDecimal committedHeap;

    /**
     * 最大非堆内存
     */
    private BigDecimal maxNonHeap;

    /**
     * 已使用非堆内存
     */
    private BigDecimal usedNonHeap;

    /**
     * 剩余可用堆内存
     */
    BigDecimal spareHead;

    /**
     * 剩余可分配堆内存
     */
    BigDecimal spareCommitHead;

    /**
     * 剩余最大可用堆内存
     */
    BigDecimal spareMaxHead;

    /**
     * 剩余可用非堆内存
     */
    BigDecimal spareNonHeap;

}
