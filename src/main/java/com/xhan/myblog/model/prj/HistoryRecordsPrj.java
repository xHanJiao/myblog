package com.xhan.myblog.model.prj;

import com.xhan.myblog.model.content.repo.ArticleHistoryRecord;
import lombok.Value;

import java.util.List;

@Value
public class HistoryRecordsPrj {
    private final String id;
    private final List<ArticleHistoryRecord> historyRecords;
}
