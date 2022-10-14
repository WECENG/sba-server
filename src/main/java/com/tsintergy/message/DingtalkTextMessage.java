package com.tsintergy.message;

import com.tsintergy.enums.DingtalkMessageType;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 *  钉钉文本消息
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/26 11:00
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class DingtalkTextMessage extends DingtalkMessage{

    @Override
    public String getMsgtype() {
        return DingtalkMessageType.TEXT.getName();
    }

}
