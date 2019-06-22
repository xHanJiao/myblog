package com.xhan.myblog.controller;

public interface ControllerConstant {

    String CATE_NUMS = "cateNums";
    long PEOPLE_MAX_VISIT_PER_10_SECOND = 15;
    long ALL_MAX_VISIT_PER_5_SECOND = 50;
    int DEFAULT_PAGE_SIZE = 7;
    // ARTICLES
    String SLASH = "/";
    String SUFFIX = "/**";
    String MODI_ADMIN = "modiadmin";
    String MODI_ADMIN_URL = SLASH + MODI_ADMIN;
    String ARTICLE = "article";
    String ARTICLE_URL = SLASH + ARTICLE;
    String UPLOAD_PIC = "uploadPic";
    String STATE_URL = SLASH + "state";
    String CATEGORY = "category";
    String CATEGORY_URL = SLASH + CATEGORY;
    String MODIFY = "modify";
    String MODIFY_URL = SLASH + MODIFY;
    String ARTICLE_LIST = "articles";
    String CONTENT_URL = SLASH + "content";
    String API_URL = SLASH + "api";
    // COMMENTS
    String ADD_COMMENTS = SLASH + "addcomm";
    String ADD_URL = SLASH + "add";
    String INDEX = "index";
    String REDIRECT = "redirect:";
    String RECYCLE = "recycle";
    String RECYCLE_URL = SLASH + RECYCLE;
    String EDIT = "edit";
    String EDIT_URL = SLASH + EDIT;
    String ID_PATH_VAR = SLASH + "{id}";
    String NAME_PATH_VAR = SLASH + "{name}";
    String CATEGORIES = "categories";
    String DELETE = "del";
    String DELETE_URL = SLASH + DELETE;
    String IMAGE_URL = SLASH + "image";
    String VISIBLE_PUBLISH_URL = "/vpub";
    String UNVISITABLE_PUBLISH_URL = "/uvpub";
    String HIDDEN = "hidden";
    String HIDDEN_URL = SLASH + HIDDEN;
    String COMMENT_URL = "/comment";
    String RECOVER = "recover";
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
    String M_HIDDEN = "articles.meta.notPublished";
    String M_HIDDEN_URL = "/hidden";
    String M_CATE = "articles.meta.category";
    // META URL IN ARTICLE LIST
    String M_ALL_ARTICLE_URL = ARTICLE_URL + SLASH;
    String M_CATE_URL = SLASH + CATEGORY + SLASH;

    // 这个这么放可是不太好啊
    String ARTICLE_IMAGES_URL = SLASH + "articleImages";
    String CATEGORY_IMAGES_URL = SLASH + "categoryImages";

}

