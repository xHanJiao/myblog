package com.xhan.myblog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

import static com.xhan.myblog.controller.ControllerConstant.*;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource(name = "userDetailService")
    private UserDetailsService myService;
    @Resource(name = "myPasswordEncoder")
    private PasswordEncoder passwordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
        auth.userDetailsService(myService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeRequests()
                .antMatchers(SLASH + EDIT + SUFFIX,
                        RECOVER_URL + SUFFIX,
                        MODI_ADMIN_URL,
                        DELETE_URL + SUFFIX,
                        RECYCLE_URL, DRAFT_URL,
                        ADD_URL + SUFFIX,
                        "/view" + SUFFIX,
                        SLASH + MODIFY + SUFFIX,
                        "/actuator" + SUFFIX).authenticated()
                .anyRequest().permitAll()
                .and()
                .formLogin().loginPage(LOGIN_URL)
                .usernameParameter("account")
                .successForwardUrl(LOGIN_DISPATCH_URL)
                .and().logout().logoutUrl("/logout");
    }
}
