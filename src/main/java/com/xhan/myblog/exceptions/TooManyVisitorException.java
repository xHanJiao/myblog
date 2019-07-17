package com.xhan.myblog.exceptions;

public class TooManyVisitorException extends RuntimeException {
    public TooManyVisitorException() {
        super();
    }

    public TooManyVisitorException(String message) {
        super(message);
    }

    public TooManyVisitorException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooManyVisitorException(Throwable cause) {
        super(cause);
    }

    public TooManyVisitorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
