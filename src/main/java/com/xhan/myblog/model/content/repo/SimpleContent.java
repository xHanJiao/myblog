package com.xhan.myblog.model.content.repo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

import static com.xhan.myblog.utils.BlogUtils.getCurrentDateTime;

@Data
public class SimpleContent {
    @NotBlank
    private String content;
    private String createTime;

    public SimpleContent() {
        setCreateTime(getCurrentDateTime());
    }

    public void preProcessBeforeSave() {
    }
}
