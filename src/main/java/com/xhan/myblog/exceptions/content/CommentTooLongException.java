package com.xhan.myblog.exceptions.content;

public class CommentTooLongException extends CommentException{
    public CommentTooLongException() {
        super("评论过长！（请限制在140个字以内）");
    }

    public CommentTooLongException(String message) {
        super(message);
    }

    public CommentTooLongException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommentTooLongException(Throwable cause) {
        super(cause);
    }

    public CommentTooLongException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
