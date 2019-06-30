package com.xhan.myblog.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface ControllerConstant {

    String ARTICLE_NUMS_OF_CATE = "cateNums";
    long PEOPLE_MAX_VISIT_PER_10_SECOND = 20;
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
    String POST_NUM = "postedArticleNum";
    String POST_NUM_URL = SLASH + POST_NUM;
    String CATE_NUM = "createdCategoryNum";
    String CATE_NUM_URL = SLASH + CATE_NUM;
    // LOGIN
    String LOGIN_DISPATCH_URL = "/loginsucc";
    String IS_ADMIN = "isAdmin";
    String R_ADMIN = "ROLE_ADMIN";
    String LOGIN = "login";
    String LOGIN_URL = SLASH + LOGIN;
    String HISTORY_RECORD = "history";
    String HISTORY_RECORD_URL = SLASH + HISTORY_RECORD;
    // META INF IN ARTICLE LIST
    String M_DRAFT_URL = DRAFT_URL + SLASH;
    String M_RECYCLED_URL = RECYCLE_URL + SLASH;
    String M_HIDDEN_URL = SLASH + HIDDEN;
    String M_ALL_ARTICLES_URL = ARTICLE_URL + SLASH;
    String M_CATE_URL = CATEGORY_URL + SLASH;

    String M_DRAFT = "articles.meta.draft";
    String M_RECYCLED = "articles.meta.recycle";
    String M_HIDDEN = "articles.meta.notPublished";
    String M_ALL_ARTICLES = "articles.meta.all";
    String M_CATE = "articles.meta.category";
    String M_SEARCH = "articles.meta.search";
    // META URL IN ARTICLE LIST
    Map<String, String> metaMap = Collections.unmodifiableMap(new HashMap<String, String>(){{
        put(M_CATE, M_CATE_URL);
        put(M_RECYCLED, M_RECYCLED_URL);
        put(M_DRAFT, M_DRAFT_URL);
        put(M_HIDDEN, M_HIDDEN_URL);
        put(M_ALL_ARTICLES, M_ALL_ARTICLES_URL);
    }});

    // 这个这么放可是不太好啊
    String ARTICLE_IMAGES_URL = SLASH + "articleImages";
    String CATEGORY_IMAGES_URL = SLASH + "categoryImages";

}

