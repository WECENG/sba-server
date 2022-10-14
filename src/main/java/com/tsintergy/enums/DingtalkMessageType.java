package com.tsintergy.enums;

/**
 * <p>
 * 钉钉消息类型
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/9/26 11:19
 */
public enum DingtalkMessageType {
    /**
     * 文本
     */
    TEXT("text"),

    /**
     * markdown
     */
    MARKDOWN("markdown");

    private final String name;

    DingtalkMessageType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
