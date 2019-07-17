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

    @Resource(name = "allVisitRateLimiterInterceptor")
    private HandlerInterceptor allVisitRateLimiter;

    @Resource(name = "controllerPropertiesBean")
    private ControllerPropertiesBean propertiesBean;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                ARTICLE_IMAGES_URL + SUFFIX,
                CATEGORY_IMAGES_URL + SUFFIX,
                "/pdf" + SUFFIX)
                .addResourceLocations(
                        "file:" + propertiesBean.getPdfPaths(),
                        "file:" + propertiesBean.getArticleImages(),
                        "file:" + propertiesBean.getCategoryImages());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .addPathPatterns(SUFFIX)
                .excludePathPatterns("/fonts/**", "/js/**", "/css/**",
                        "/images/**", "/image/**", ARTICLE_IMAGES_URL + SUFFIX,
                        CATEGORY_IMAGES_URL + SUFFIX);
        registry.addInterceptor(allVisitRateLimiter)
                .addPathPatterns(SUFFIX)
                .excludePathPatterns("/fonts/**", "/js/**", "/css/**",
                        "/images/**", "/image/**", ARTICLE_IMAGES_URL + SUFFIX,
                        CATEGORY_IMAGES_URL + SUFFIX);
    }
}
