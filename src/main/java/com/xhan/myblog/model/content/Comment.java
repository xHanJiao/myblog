package com.xhan.myblog.model.content;

import com.xhan.myblog.exceptions.content.CommentTooLongException;
import com.xhan.myblog.utils.BlogUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class Comment extends SimpleContent{
    @NotBlank
    private String creator;

    @Override
    public void preProcessBeforeSave() {
        super.preProcessBeforeSave();
        if (getContent().length() > 140)
            throw new CommentTooLongException();
        setContent(BlogUtils.cleanXSS(getContent()));
    }
}
