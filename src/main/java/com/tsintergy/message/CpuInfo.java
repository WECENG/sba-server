package com.tsintergy.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * <p>
 * cpu信息
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/10/26 19:26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CpuInfo {

    /**
     * 进程cpu使用率
     */
    private BigDecimal processCpuUsage;

    /**
     * 系统cpu使用率
     */
    private BigDecimal systemCpuUsage;

    /**
     * 告警标题
     */
    private String warningTitle;

}
