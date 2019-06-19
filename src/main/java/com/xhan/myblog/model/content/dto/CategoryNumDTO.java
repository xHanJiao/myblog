package com.xhan.myblog.model.content.dto;

import com.xhan.myblog.model.content.repo.Category;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryNumDTO extends Category {

    private Integer num;

    public CategoryNumDTO() {
        super();
    }

    public CategoryNumDTO(Category category) {
        setDescription(category.getDescription());
        setFilePath(category.getFilePath());
        setCreateTime(category.getCreateTime());
        setName(category.getName());
        setId(category.getId());
        setNum(0);
    }

}
