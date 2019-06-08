package com.xhan.myblog.controller;

public interface ControllerConstant {
    // ARTICLES
    String SLASH = "/";
    String ARTICLE = "article";
    String ARTICLE_URL = SLASH + ARTICLE;
    // COMMENTS
    String ADD_COMMENTS = SLASH + "addcomm";
    String DEL_COMMENTS = SLASH + "delcomm";
    String ADD = SLASH + "add";
    String INDEX = "index";
    String REDIRECT = "redirect:";
    String EDIT = "edit";
    String ID_PATH_VAR = SLASH + "{id}";
    String MODIFY = "modify";
    String DELETE = SLASH + "del";
    String RECOVER = SLASH + "recover";
}
