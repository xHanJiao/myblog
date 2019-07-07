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

    public static HistoryShowDTO getHistoryShowDTO(HistoryCreateDTO dto) {
        return HistoryShowDTO.builder()
                    .createTime(dto.getCreateTime())
                    .recordId(dto.getRecordId())
                    .title(dto.getTitle()).build();
    }

}
