package com.tsintergy.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * 钉钉消息
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/26 11:15
 */
@Data
@SuperBuilder
public abstract class DingtalkMessage {

    /**
     * 消息类型
     */
    private String msgtype;

    /**
     * 标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

}
