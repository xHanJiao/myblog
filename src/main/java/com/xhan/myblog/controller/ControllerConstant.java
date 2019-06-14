package com.xhan.myblog.controller;

public interface ControllerConstant {

    String IP_SET = "STATISTIC_IP";
    long PEOPLE_MAX_VISIT_PER_10_SECOND = 15;
    long ALL_MAX_VISIT_PER_5_SECOND = 50;
    // ARTICLES
    String SLASH = "/";
    String SUFFIX = "/**";
    String MODI_ADMIN = "modiadmin";
    String MODI_ADMIN_URL = SLASH + MODI_ADMIN;
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
    String RECYCLE = "recycle";
    String RECYCLE_URL = SLASH + RECYCLE;
    String EDIT = "edit";
    String ID_PATH_VAR = SLASH + "{id}";
    String NAME_PATH_VAR = SLASH + "{name}";
    String MODIFY = "modify";
    String CATEGORIES = "categories";
    String DELETE_URL = SLASH + "del";
    String COMMENT_URL = "/comment";
    String RECOVER_URL = SLASH + "recover";
    String DRAFT_URL = SLASH + "draft";
    // LOGIN
    String LOGIN_DISPATCH_URL = "/loginsucc";
    String IS_ADMIN = "isAdmin";
    String R_ADMIN = "ROLE_ADMIN";
    String LOGIN = "login";
    String LOGIN_URL = SLASH + LOGIN;
    // META INF IN ARTICLE LIST
    String ALL_ARTICLE = "articles.meta.all";
    String M_DRAFT_URL = DRAFT_URL + SLASH;
    String M_RECYCLED_URL = SLASH + RECYCLE + SLASH;
    String M_RECYCLED = "articles.meta.recycle";
    String M_DRAFT = "articles.meta.draft";
    String M_PUBLISHED = "articles.meta.published";
    String M_HIDDEN = "articles.meta.notPublished";
    String M_HIDDEN_URL = "/hidden";
    String M_CATE = "articles.meta.category";
    // META URL IN ARTICLE LIST
    String M_ALL_ARTICLE_URL = ARTICLE_URL + SLASH;
//    String M_PUBLISHED_URL =
//    String M_HIDDEN =
    String M_CATE_URL = SLASH + CATEGORY + SLASH;
}

