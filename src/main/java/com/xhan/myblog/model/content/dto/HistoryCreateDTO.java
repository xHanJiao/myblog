package com.xhan.myblog.model.content.dto;

import com.xhan.myblog.model.content.repo.ArticleHistoryRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HistoryCreateDTO extends ArticleHistoryRecord {
    private String articleId;

    public ArticleHistoryRecord toRecord() {
        ArticleHistoryRecord record = new ArticleHistoryRecord();
        record.setCreateTime(getCreateTime());
        record.setSnapshotContent(getSnapshotContent());
        record.setTitle(getTitle());
        record.setImagePaths(getImagePaths());
        record.setRecordId(getRecordId());
        return record;
    }
}
