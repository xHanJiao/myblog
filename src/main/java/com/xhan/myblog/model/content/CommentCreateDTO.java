package com.xhan.myblog.model.content;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommentCreateDTO extends Comment{
    @NotBlank
    private String articleId;

    @NotBlank @Email
    private String email;

    public CommentCreateDTO(Comment mockComment) {
        setCreator(mockComment.getCreator());
        setReplyTo(mockComment.getReplyTo());
        setContent(mockComment.getContent());
        setCreateTime(mockComment.getCreateTime());
    }

    public Comment toComment() {
        Comment comment = new Comment();
        comment.setCreator(getCreator());
        comment.setReplyTo(getReplyTo());
        comment.setContent(getContent());
        comment.setCreateTime(getCreateTime());
        return comment;
    }
}
