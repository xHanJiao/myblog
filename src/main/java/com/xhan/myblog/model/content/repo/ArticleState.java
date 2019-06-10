package com.xhan.myblog.model.content.repo;

public enum ArticleState {

    DRAFT(0), PUBLISHED(1), HIDDEN(2), RECYCLED(3);

    public int getState() {
        return state;
    }

    private int state;

    ArticleState(int i) {
        state = i;
    }

}
