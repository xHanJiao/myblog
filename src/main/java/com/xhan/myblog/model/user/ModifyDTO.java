package com.xhan.myblog.model.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;

import static org.springframework.util.StringUtils.hasText;

@Data
public class ModifyDTO {
    @NotBlank
    private String account;
    @NotBlank
    private String password;
    @NotBlank
    private String newPwd;
    @NotBlank
    private String confirmPwd;

    public boolean isNewPwdValid() {
        return newPwd.equals(confirmPwd) && newPwd.length() > 7;
    }
}
