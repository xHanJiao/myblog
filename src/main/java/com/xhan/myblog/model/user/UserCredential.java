package com.xhan.myblog.model.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserCredential {
    @NotBlank
    private String account;
    @NotBlank
    private String password;
}
