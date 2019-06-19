package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.repository.ArticleRepository;
import com.xhan.myblog.repository.CategoryRepository;
import com.xhan.myblog.utils.MapCache;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.IntStream;

import static com.xhan.myblog.controller.ControllerConstant.ARTICLE_LIST;
import static java.util.stream.Collectors.toList;

@Controller
public class BaseController {

    @Autowired
    protected MongoTemplate mongoTemplate;
    @Autowired
    protected ArticleRepository articleRepository;
    @Autowired
    protected CategoryRepository categoryRepository;
    protected Article emptyArticle = new Article();

    protected MapCache cache = MapCache.single();

    protected static final String pageSize = "10";

    public BaseController() {
        emptyArticle.setState(1);
    }

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

    void preProcessToArticleList(ModelAndView mav, Integer page, Integer pageSize,
                                         Page<Article> articles, int totalNums, String meta, String metaUrl) {
        mav.setViewName(ARTICLE_LIST);
        page = isIntValid(page) ? page : 0;
        int maxPage = totalNums % pageSize == 0 ? totalNums / pageSize : totalNums / pageSize + 1;
        List<Integer> pages = IntStream.range(0, maxPage).boxed().map(i -> i+1).collect(toList());
        mav.addObject("articles", articles.getContent());
        mav.addObject("currentPage", page + 1);
        mav.addObject("allPages", pages);
        mav.addObject("meta", meta);
        mav.addObject("metaUrl", metaUrl);
        Article mockArticle = articles.getTotalElements() > 0
                ? articles.getContent().get(0)
                : emptyArticle;
        mav.addObject("article", mockArticle);
    }

    protected boolean isIntValid(Integer i) {
        return i != null && i >= 0;
    }

    protected class MyPageRequest {
        private Integer page;
        private Integer pageSize;

        public MyPageRequest(Integer page, Integer pageSize) {
            this.page = page;
            this.pageSize = pageSize;
        }

        public Integer getPage() {
            return page;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public MyPageRequest invoke() {
            page = isIntValid(page) ? page : 0;
            pageSize = isIntValid(pageSize) ? pageSize : 5;
            return this;
        }
    }
}
