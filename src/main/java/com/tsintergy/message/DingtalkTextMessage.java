package com.tsintergy.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 *  钉钉文本消息
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/26 11:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DingtalkTextMessage {

    /**
     * 文本内容
     */
    private String content;

}
