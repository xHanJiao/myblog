package com.xhan.myblog.model.content.dto;

import com.xhan.myblog.model.content.repo.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommentCreateDTO extends Comment {
    @NotBlank
    private String articleId;

    @NotBlank @Email
    private String email;

    public CommentCreateDTO(Comment comment) {
        setCreator(comment.getCreator());
        setContent(comment.getContent());
        setCreateTime(comment.getCreateTime());
    }

    public Comment toComment() {
        Comment comment = new Comment();
        comment.setCreator(getCreator());
        comment.setContent(getContent());
        comment.setCreateTime(getCreateTime());
        return comment;
    }
}
