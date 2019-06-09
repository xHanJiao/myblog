package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.model.content.Article;
import com.xhan.myblog.repository.ArticleRepository;
import com.xhan.myblog.repository.CategoryRepository;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

@Controller
@NoArgsConstructor
public class BaseController {

    @Autowired
    protected MongoTemplate mongoTemplate;
    @Autowired
    protected ArticleRepository articleRepository;
    @Autowired
    protected CategoryRepository categoryRepository;
    protected Article emptyArticle = new Article();

    ModelAndView oneModify(ModelAndView mav, String viewName, String errMsg, UpdateResult updateResult) {
        mav.setViewName(viewName);
        if (updateResult.getModifiedCount() != 1) {
            mav.setStatus(HttpStatus.valueOf(500));
            mav.addObject("error", errMsg);
        }
        return mav;
    }

    ModelAndView setErrorMav(String errMsg, ModelAndView mav, String viewName) {
        mav.setStatus(HttpStatus.BAD_REQUEST);
        mav.addObject("error", errMsg);
        mav.setViewName(viewName);
        return mav;
    }
}
