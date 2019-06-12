package com.xhan.myblog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

import static com.xhan.myblog.controller.ControllerConstant.*;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Resource(name = "logInterceptor")
    private HandlerInterceptor logInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .addPathPatterns(SUFFIX)
                .excludePathPatterns("/fonts/**")
                .excludePathPatterns("/js/**")
                .excludePathPatterns("/css/**")
                .excludePathPatterns("/image/**");

    }
}
