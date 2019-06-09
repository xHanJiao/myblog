package com.xhan.myblog.model.content;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.xhan.myblog.utils.BlogUtils.getCurrentTime;

@Data
public class SimpleContent {
    @NotBlank
    private String content;
    private String createTime;

    public SimpleContent() {
        setCreateTime(getCurrentTime());
    }

    public void preProcessBeforeSave() {
    }
}
