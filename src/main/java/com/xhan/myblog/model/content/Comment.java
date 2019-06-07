package com.xhan.myblog.model.content;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class Comment extends SimpleContent{
    @NotBlank
    private String creator;
    private String replyTo;
}
