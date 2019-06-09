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
    String NAME_PATH_VAR = SLASH + "{name}";
    String MODIFY = "modify";
    String CATEGORIES = "categories";
    String DELETE_URL = SLASH + "del";
    String COMMENT_URL = "/comment";
    String RECOVER_URL = SLASH + "recover";
    // LOGIN
    String LOGIN_DISPATCH_URL = "/loginsucc";
    String IS_ADMIN = "isAdmin";
    String R_ADMIN = "ROLE_ADMIN";
    String LOGIN = "login";
    String LOGIN_URL = SLASH + LOGIN;
    // META INF IN ARTICLE LIST
    String ALL_ARTICLE = "articles.meta.all";
    String PUBLISHED = "articles.meta.published";
    String NOT_PUBLISHED = "articles.meta.notPublished";
    String FINISHED = "articles.meta.finished";
    String NOT_FINISHED = "articles.meta.notFinished";
    String CATE = "articles.meta.category";
    // META URL IN ARTICLE LIST
    String ALL_ARTICLE_URL = ARTICLE_URL + SLASH;
//    String PUBLISHED_URL =
//    String NOT_PUBLISHED_URL =
//    String FINISHED_URL =
//    String NOT_FINISHED_URL =
    String CATE_URL = SLASH + CATEGORY + SLASH;
}

