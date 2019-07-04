package com.xhan.myblog.model.prj;

import lombok.Value;

@Value
public class IdTitleTimeStatePrj {
    private String id;
    private String title;
    private Integer state;
    private String createTime;
}
