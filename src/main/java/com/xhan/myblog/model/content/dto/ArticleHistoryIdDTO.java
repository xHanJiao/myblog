package com.xhan.myblog.model.content.dto;

import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
public class ArticleHistoryIdDTO {
    @NotBlank
    private String articleId;
    @NotBlank
    private String historyId;
}
