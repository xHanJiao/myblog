package com.xhan.myblog.model.content;

import java.util.List;

public interface TitleContentCommTimeDTO {
    String getTitle();
    String getContent();
    String getCreateTime();
    List<Comment> getComments();
}
