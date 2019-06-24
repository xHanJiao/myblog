package com.xhan.myblog.config;

import com.xhan.myblog.controller.ControllerPropertiesBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

import static com.xhan.myblog.controller.ControllerConstant.*;

@Configuration
public class MyMvcConfig implements WebMvcConfigurer {

    @Resource(name = "logInterceptor")
    private HandlerInterceptor logInterceptor;

    @Resource(name = "controllerPropertiesBean")
    private ControllerPropertiesBean propertiesBean;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                ARTICLE_IMAGES_URL + SUFFIX,
                CATEGORY_IMAGES_URL + SUFFIX)
                .addResourceLocations(
                        "file:" + propertiesBean.getArticleImages(),
                        "file:" + propertiesBean.getCategoryImages());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .addPathPatterns(SUFFIX)
                .excludePathPatterns("/fonts/**")
                .excludePathPatterns("/js/**")
                .excludePathPatterns("/css/**")
                .excludePathPatterns("/images/**")
                .excludePathPatterns("/image/**")
                .excludePathPatterns(ARTICLE_IMAGES_URL + SUFFIX)
                .excludePathPatterns(CATEGORY_IMAGES_URL + SUFFIX);

    }
}
