package com.xhan.myblog.model.content.dto;

import com.xhan.myblog.model.content.repo.Category;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CategoryNumDTO extends Category{

    private Integer num;

    public CategoryNumDTO(Category c) {
        setNum(0);
        setDescription(c.getDescription());
        setName(c.getName());
        setFilePath(c.getFilePath());
        setCreateTime(c.getCreateTime());
        setId(c.getId());
    }
}
