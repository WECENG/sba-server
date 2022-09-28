package com.tsintergy.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 钉钉消息
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/26 11:15
 */
@Data
public class DingtalkMessage {

    private String msgtype;

    private DingtalkTextMessage text;

}
