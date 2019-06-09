package com.xhan.myblog.model.content;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

import static org.springframework.util.StringUtils.hasText;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleCreateDTO extends SimpleContent {

    @NotBlank
    private String title;

    private String category;
    private Boolean commentEnable;
    private Boolean finished;

    public Article toArticle() {
        setCategory(hasText(category) ? category : Category.DEFAULT_NAME);
        Article article = new Article();
        article.setDeleted(false);
        article.setCommentEnable(getCommentEnable() == null ? false : getCommentEnable());
        article.setCategory(getCategory());
        article.setTitle(getTitle());
        article.setContent(getContent());
        article.setFinished(getFinished());
        return article;
    }
}
