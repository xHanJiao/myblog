package com.xhan.myblog.controller;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component(value = "controllerPropertiesBean")
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "controller")
public class ControllerPropertiesBean {

    private String brand;

    private Integer people10SecVisit;

    private Integer all5SecVisit;

    private String articleImages;

    private String categoryImages;

    private String greeting;

    private String pdfPaths;

    private Integer shortcutLen;

    private Integer defaultPageSize;
}
