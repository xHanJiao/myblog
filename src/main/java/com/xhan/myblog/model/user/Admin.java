package com.xhan.myblog.model.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

@Data
@Document
@EqualsAndHashCode(callSuper = true)
public class Admin extends Guest {

    @NotBlank
    private String account;

    @NotBlank
    private String password;
}
