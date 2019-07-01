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
    private String snapshotContent;
    @NotBlank
    private String title;
    private List<String> imagePaths = new LinkedList<>();
    private String createTime = BlogUtils.getCurrentDateTime();

    public Article toArticle() {
        return new Article(this);
    }
}
