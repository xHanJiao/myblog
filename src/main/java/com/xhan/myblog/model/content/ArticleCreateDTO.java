package com.xhan.myblog.model.content;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleCreateDTO extends SimpleContent{

    @NotBlank
    private String title;

    public Article toDO() {
        Article article = new Article();
        article.setDeleted(false);
        article.setTitle(getTitle());
        article.setContent(getContent());
        return article;
    }
}
