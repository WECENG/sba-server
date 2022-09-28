package com.tsintergy.message;

import com.tsintergy.enums.DingtalkMessageType;

/**
 * <p>
 * 钉钉消息builder
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/26 11:40
 */
public final class DingtalkMessageBuilder {

    private String msgtype;

    private DingtalkTextMessage text;

    private DingtalkMessageBuilder() {
    }

    public static DingtalkMessageBuilder textMessageBuilder() {
        DingtalkMessageBuilder builder = new DingtalkMessageBuilder();
        builder.msgtype = DingtalkMessageType.TEXT.getName();
        return builder;
    }


    public DingtalkMessageBuilder textMessage(DingtalkTextMessage textMessage) {
        this.text = textMessage;
        return this;
    }

    public DingtalkMessage build() {
        DingtalkMessage dingtalkMessage = new DingtalkMessage();
        dingtalkMessage.setMsgtype(msgtype);
        dingtalkMessage.setText(text);
        return dingtalkMessage;
    }
}
