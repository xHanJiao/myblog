package com.xhan.myblog.model.content.repo;

import com.xhan.myblog.exceptions.content.CommentTooLongException;
import com.xhan.myblog.utils.BlogUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class Comment extends SimpleContent{
    @NotBlank @Max(value = 7, message = "昵称长度不能超过7")
    private String creator;

    @Override
    public void preProcessBeforeSave() {
        super.preProcessBeforeSave();
        if (getContent().length() > 140)
            throw new CommentTooLongException();
        setContent(BlogUtils.cleanXSS(getContent()));
    }
}
