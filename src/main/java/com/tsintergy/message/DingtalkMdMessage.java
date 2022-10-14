package com.tsintergy.message;

import com.tsintergy.enums.DingtalkMessageType;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * markdown类型消息
 * </p>
 *
 * @author chenwc@tsintergy.com
 * @since 2022/10/13 18:26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class DingtalkMdMessage extends DingtalkMessage {

    private Markdown markdown;

    @Override
    public String getMsgtype() {
        return DingtalkMessageType.MARKDOWN.getName();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getContent() {
        return null;
    }

    public Markdown getMarkdown() {
        return new Markdown(super.getTitle(), super.getContent());
    }

    @AllArgsConstructor
    @Data
    private static class Markdown {

        private String title;

        private String text;

    }
}
