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

@Data
@Document
@EqualsAndHashCode(callSuper = true)
public class Article extends ArticleCreateDTO {

    public static final String COLLECTION_NAME = "articles";

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
