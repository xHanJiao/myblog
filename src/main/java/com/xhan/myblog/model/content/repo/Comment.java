package com.xhan.myblog.model.content.repo;

import com.xhan.myblog.exceptions.content.CommentTooLongException;
import com.xhan.myblog.utils.BlogUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = true)
public class Comment extends SimpleContent{

    @NotBlank @Size(max = 7, min=1, message = "昵称在1到7个字之间")
    private String creator;

    @Override
    public void preProcessBeforeSave() {
        super.preProcessBeforeSave();
        if (getContent().length() > 140)
            throw new CommentTooLongException();
        setContent(BlogUtils.cleanXSS(getContent()));
    }
}
