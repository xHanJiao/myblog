package com.xhan.myblog.model.content.dto;

import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.model.content.repo.Category;
import com.xhan.myblog.model.content.repo.SimpleContent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.Collections;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleCreateDTO extends SimpleContent {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "目录不能为空")
    private String category;
    @NotNull(message = "请确认是否允许评论")
    private Boolean commentEnable;
    private List<String> imagePaths;
    @NotNull(message = "请确认状态")
    private Integer state;

    public Article toArticle() {
        setCategory(hasText(category) ? category : Category.DEFAULT_NAME);
        Article article = new Article();
        article.setState(getState());
        article.setImagePaths(getImagePaths() == null ? Collections.emptyList(): getImagePaths());
        article.setCommentEnable(getCommentEnable() == null ? false : getCommentEnable());
        article.setCategory(getCategory());
        article.setTitle(getTitle());
        article.setContent(getContent());
        article.setVisitTimes(0L);
        return article;
    }
}
