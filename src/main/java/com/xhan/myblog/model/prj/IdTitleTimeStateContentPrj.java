package com.xhan.myblog.model.prj;

import com.xhan.myblog.utils.BlogUtils;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IdTitleTimeStateContentPrj {
    private String id;
    private String content;
    private String title;
    private Integer state;
    private String createTime;

    public IdTitleTimeStateContentPrj convertToShortcutNoTag(final int maxLen) {
        if (getContent() == null) return this;
        String content = BlogUtils.deEscape(getContent());
        content = BlogUtils.delHtmlTag(content);
        content = content.length() > maxLen
                ? content.substring(0, maxLen) + "..."
                : content;
        return IdTitleTimeStateContentPrj.builder()
                .content(content)
                .createTime(getCreateTime())
                .id(getId())
                .title(getTitle())
                .state(getState()).build();
    }
}
