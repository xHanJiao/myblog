package com.xhan.myblog.model.content.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
/**
 * 这个类是为了删除评论而用的，其中的的两个属性是文章id和评论内容
 */
public class DelCommDTO {
    @NotBlank
    private String content;
    @NotBlank
    private String articleId;
}
