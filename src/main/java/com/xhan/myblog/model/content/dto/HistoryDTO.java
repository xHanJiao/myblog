package com.xhan.myblog.model.content.dto;

import com.xhan.myblog.model.content.repo.ArticleHistoryRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HistoryDTO extends ArticleHistoryRecord {
    private String articleId;
}
