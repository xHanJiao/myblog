package com.xhan.myblog.model.content;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class SimpleContent {
    @NotBlank
    private String content;
    private String createTime;

    public SimpleContent() {
        createTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public void preProcessBeforeSave() {
    }
}
