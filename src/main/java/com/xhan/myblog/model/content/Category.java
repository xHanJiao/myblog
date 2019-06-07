package com.xhan.myblog.model.content;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
public class Category {

    @Id
    private String id;
    private String name;
    private String description;
    private Date createTime;
}
