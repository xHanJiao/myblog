package com.xhan.myblog.model.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Document
public class Guest {

    public static final String COLLECTION_NAME = "USER";

    @Id
    private String id;

    @Email @NotBlank
    private String email;

    @NotBlank
    private String nickName;

    private String lastLoginTime;

    private String lastLoginIp;

    private String createTime;
}
