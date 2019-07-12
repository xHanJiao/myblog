package com.xhan.myblog.model.prj;

import com.xhan.myblog.model.content.dto.HistoryCreateDTO;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HistoryShowDTO {
    private String recordId;
    private String title;
    private String createTime;
    private String articleId;

    public static HistoryShowDTO getHistoryShowDTO(HistoryCreateDTO dto) {
        return HistoryShowDTO.builder()
                    .createTime(dto.getCreateTime())
                    .recordId(dto.getRecordId())
                .articleId(dto.getArticleId())
                    .title(dto.getTitle()).build();
    }

}
