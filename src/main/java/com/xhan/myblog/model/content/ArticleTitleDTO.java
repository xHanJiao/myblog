package com.xhan.myblog.model.content;

import lombok.Data;

import java.util.Date;

//@Data
public interface ArticleTitleDTO {
//    private String id;
//    private String title;
//    private String createTime;
    String getId();
    String getTitle();
    String getCreateTime();

    default String getString(ArticleTitleDTO dto) {
        return "[" + "id: " + dto.getId() + ", " +
                "title: " + dto.getTitle() + ", " +
                "createTime: " + dto.getCreateTime() + "]";
    }
}
