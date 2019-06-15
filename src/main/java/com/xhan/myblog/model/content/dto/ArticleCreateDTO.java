package com.xhan.myblog.model.content.dto;

import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.model.content.repo.ArticleState;
import com.xhan.myblog.model.content.repo.Category;
import com.xhan.myblog.model.content.repo.SimpleContent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

import java.util.Collections;
import java.util.List;

import static com.xhan.myblog.model.content.repo.ArticleState.PUBLISHED;
import static org.springframework.util.StringUtils.hasText;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleCreateDTO extends SimpleContent {

    @NotBlank
    private String title;

    private String category;
    private Boolean commentEnable;
    private List<String> imagePaths;
    private int state;

    public Article toArticle() {
        setCategory(hasText(category) ? category : Category.DEFAULT_NAME);
        Article article = new Article();
        article.setState(getState());
        article.setImagePaths(getImagePaths() == null ? Collections.emptyList(): getImagePaths());
        article.setCommentEnable(getCommentEnable() == null ? false : getCommentEnable());
        article.setCategory(getCategory());
        article.setTitle(getTitle());
        article.setContent(getContent());
        return article;
    }
}
