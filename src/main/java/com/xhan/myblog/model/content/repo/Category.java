package com.xhan.myblog.model.content.repo;

import com.xhan.myblog.utils.BlogUtils;
import lombok.Data;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.xhan.myblog.utils.BlogUtils.getCurrentTime;

@Data
@Document
public class Category {

    public static final String DEFAULT_NAME = "未分类";
    @Id
    private String id;
    @NotBlank
    private String name;
    private String description;
    private String createTime;

    public Category() {
        createTime = getCurrentTime();
    }
}
