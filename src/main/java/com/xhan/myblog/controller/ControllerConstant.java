package com.xhan.myblog.controller;

public interface ControllerConstant {
    // ARTICLES
    String SLASH = "/";
    String ARTICLE = "article";
    String ARTICLE_URL = SLASH + ARTICLE;
    String CATEGORY = "category";
    String ARTICLE_LIST = "articles";
    // COMMENTS
    String ADD_COMMENTS = SLASH + "addcomm";
    String DEL_COMMENTS = SLASH + "delcomm";
    String ADD_URL = SLASH + "add";
    String INDEX = "index";
    String REDIRECT = "redirect:";
    String EDIT = "edit";
    String ID_PATH_VAR = SLASH + "{id}";
    String MODIFY = "modify";
    String DELETE_URL = SLASH + "del";
    String COMMENT_URL = "/comment";
    String RECOVER_URL = SLASH + "recover";
    // LOGIN
    String LOGIN_DISPATCH_URL = "/loginsucc";
    String IS_ADMIN = "isAdmin";
    String ADMIN_URL = SLASH + "admin";
    String R_ADMIN = "ROLE_ADMIN";
    String LOGIN = "login";
    String LOGIN_URL = SLASH + LOGIN;
}
