package com.xhan.myblog.model.content;

import lombok.Data;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
public class Category {

    public static final String DEFAULT_NAME = "未分类";
    @Id
    private String id;
    private String name;
    private String description;
    private Date createTime;
}
