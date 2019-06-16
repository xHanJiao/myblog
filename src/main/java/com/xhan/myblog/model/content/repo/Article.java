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
@Document
@EqualsAndHashCode(callSuper = true)
public class Article extends ArticleCreateDTO {

    public static final String COLLECTION_NAME = "article";

    @Id
    private String id;
    private List<Comment> comments = new ArrayList<>();
    private Long visitTimes;

    public void preProcessBeforeSave() {
    }

    public Article() {
        setComments(new ArrayList<>());
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
    public void convertToShortcut() {
        if (getContent() == null) return;
        String content = getContent();
        content = BlogUtils.deEscape(content);
        content = BlogUtils.delHtmlTag(content);
        content = content.length() > 80
                ? content.substring(0, 80) + "..."
                : content;
        setContent(content);
    }

}
