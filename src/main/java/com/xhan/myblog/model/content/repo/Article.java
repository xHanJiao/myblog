package com.xhan.myblog.model.content.repo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xhan.myblog.model.content.dto.ArticleCreateDTO;
import com.xhan.myblog.utils.BlogUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Data
@Document(value = Article.COLLECTION_NAME)
@EqualsAndHashCode(callSuper = true)
public class Article extends ArticleCreateDTO {

    public static final String COLLECTION_NAME = "article";

    @Id
    private String id;
    private List<Comment> comments = new ArrayList<>();
    private Long visitTimes;
    private List<ArticleHistoryRecord> historyRecords;

    public Article() {
        setHistoryRecords(new ArrayList<>());
        setComments(new ArrayList<>());
        setImagePaths(new ArrayList<>());
        setVisitTimes(0L);
    }

    public void preProcessBeforeSave() {
    }

    @JsonIgnore
    public boolean isDraftValid() {
        if (!hasText(getContent()))
            return false;
        else if (getState() != ArticleState.DRAFT.getState())
            return false;
        else if (!hasText(getTitle()))
            return false;
        else if (!hasText(getCategory()))
            return false;
        else if (getCommentEnable() == null)
            return false;
        else return true;
    }

    @JsonIgnore
    public void convertToShortcutNoTag(final int maxLen) {
        if (getContent() == null) return;
        String content = BlogUtils.deEscape(getContent());
        content = BlogUtils.delHtmlTag(content);
        content = content.length() > maxLen
                ? content.substring(0, maxLen) + "..."
                : content;
        setContent(content);
    }

    @JsonIgnore
    public void convertToShortCutWithTag(final int maxLen) {
        if (getContent() == null) return;
        String content = BlogUtils.deEscape(getContent());
        setContent(content.length() > maxLen
                ? content.substring(0, maxLen) + "..."
                : content);
    }

}
