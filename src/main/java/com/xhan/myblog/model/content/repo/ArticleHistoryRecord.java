package com.xhan.myblog.model.content.repo;

import com.xhan.myblog.utils.BlogUtils;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.LinkedList;
import java.util.List;

@Data
public class ArticleHistoryRecord {
    private String recordId;
    @NotBlank
    private String content;
    @NotBlank
    private String title;
    private List<String> imagePaths = new LinkedList<>();
    private String createTime = BlogUtils.getCurrentDateTime();

    public Article toArticle() {
        Article article = new Article();
        article.getHistoryRecords().add(this);
        article.setCategory("默认分类");
        article.setTitle(getTitle());
        article.setContent(getContent());
        article.setCreateTime(getCreateTime());
        article.setCommentEnable(true);
        article.setState(ArticleState.DRAFT.getState());
        return article;
    }
}
