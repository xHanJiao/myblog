package com.xhan.myblog.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.xhan.myblog.controller.ControllerConstant.R_ADMIN;

@Service
public class AuthorityHelper {

    public boolean isAdmin() {
        boolean isAdmin = false;
        Collection<? extends GrantedAuthority> authorities =
                SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (GrantedAuthority ga : authorities)
            isAdmin |= ga.getAuthority().equals(R_ADMIN);
        return isAdmin;
    }

}
