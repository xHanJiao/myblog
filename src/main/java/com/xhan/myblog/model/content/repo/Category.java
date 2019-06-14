package com.xhan.myblog.model.content.repo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

import static com.xhan.myblog.utils.BlogUtils.getCurrentDateTime;
import static org.springframework.util.StringUtils.hasText;

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
    private String filePath;

    public Category() {
        createTime = getCurrentDateTime();
    }

    public void postProcess() {
        if (!hasText(getDescription())) {
            setDescription("这是建立在" + getCurrentDateTime() + "的一个关于" + name + "的分类");
        }
    }
}
