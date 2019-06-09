package com.xhan.myblog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class GlobalSecurity extends GlobalMethodSecurityConfiguration {

    private final UserDetailsService myService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public GlobalSecurity(UserDetailsService myService, PasswordEncoder passwordEncoder) {
        this.myService = myService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
        auth.userDetailsService(myService).passwordEncoder(passwordEncoder);
    }
}
