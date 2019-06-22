package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.exceptions.content.BlogException;
import com.xhan.myblog.model.content.dto.CategoryNumDTO;
import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.model.content.repo.Category;
import com.xhan.myblog.model.content.dto.CommentCreateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;
import static com.xhan.myblog.controller.ControllerConstant.*;
import static com.xhan.myblog.model.content.repo.ArticleState.PUBLISHED;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.util.StringUtils.hasText;

@Controller
public class ArticleController extends BaseController {

    /**
     * 返回首页所需信息，其中包含最近的pageSize篇Article，并且把最近的
     * 一篇文章的内容提取出头一部分以便展示
     * @return 返回ModelAndView，其中viewName 是INDEX常量
     */
    @GetMapping(value = {SLASH + INDEX, SLASH})
    public ModelAndView index(ModelAndView mav) {
        int defaultPageSize = 7;
        List<Article> articles = getArticlesDueIsAdmin(defaultPageSize, 0).getContent();
        Article showBoard = articles.isEmpty() ? emptyArticle : articles.get(0);
        showBoard.convertToShortcut();

        mav.setViewName(INDEX);
        mav.addObject("category", new Category());
        mav.addObject("showBoard", showBoard);
        mav.addObject("articles", articles);
        return mav;
    }

    @GetMapping(value = "myCv")
    public String getMyCv() {
        return "xhancv";
    }

    /**
     * 使用了ModelAttribute注解！
     * 在这个控制器所有请求中的Model中加入所有的分类信息，还有当前分类中
     * 的文章数目。
     * 这个分类文章数目维护在MapCache单例缓存中，并且管理员（我）和游客
     * 可以看到的内容是不同的
     * @return
     */
    @ModelAttribute(name = "allCate")
    public List<CategoryNumDTO> getAllCateAndArticleNum() {
        return getCategoryNumDTOS();
    }

    /**
     * 分页显示一个分类中的文章，默认显示第一页，每页大小是10
     * @param name 分类的名字，不能为空
     * @param page 页数，如果小于0会显示第0页，如果缺失则为第0页
     * @param pageSize 每页数量，默认是10，建议在请求时不要设置这个值
     * @return ModelAndView，如果无故障则去往 ARTICLE_LIST, 否则去往INDEX
     */
    @GetMapping(path = SLASH + CATEGORY + NAME_PATH_VAR)
    public ModelAndView getArticlesOfCategory(@PathVariable String name, ModelAndView mav,
                                              @RequestParam(defaultValue = "0") Integer page,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        if (!hasText(name)) {
            mav.setViewName(INDEX);
            mav.setStatus(HttpStatus.BAD_REQUEST);
            mav.addObject("error", "分类名不能为空");
            return mav;
        }
        Page<Article> articles = getPagedArticles(page, pageSize, name, isAdmin());
        int nums = articleRepository.countByCategoryAndState(name, PUBLISHED.getState());

        preProcessToArticleList(mav, page, pageSize, articles, nums, M_CATE, M_CATE_URL);
        mav.addObject("cateName", name);
        return mav;
    }

    @GetMapping(path = ARTICLE_URL, consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<?> getArticles(@RequestParam(defaultValue = "0") Integer page,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Article> articles = getArticlesDueIsAdmin(pageSize, page);
        return ok(articles.getContent());
    }

    @GetMapping(path = ARTICLE_URL)
    public ModelAndView getArticles(@RequestParam(defaultValue = "0") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    ModelAndView mav) {
        Page<Article> articles = getArticlesDueIsAdmin(pageSize, page);
        int nums = articleRepository.countByState(PUBLISHED.getState());

        preProcessToArticleList(mav, page, pageSize, articles, nums, ALL_ARTICLE, M_ALL_ARTICLE_URL);
        return mav;
    }

    @GetMapping(path = CONTENT_URL + ID_PATH_VAR,
            produces = {APPLICATION_JSON_UTF8_VALUE, APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getCertainArticle(@PathVariable String id) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");
        Article dto = getArticleByIdAndModifyVisit(id);
        return ok(singletonMap("article", dto.getContent()));
    }

    @GetMapping(path = ARTICLE_URL + ID_PATH_VAR)
    public ModelAndView getCertainArticle(@PathVariable String id, ModelAndView mav) {
        if (!hasText(id)) {
            mav.setStatus(HttpStatus.BAD_REQUEST);
            mav.setViewName(INDEX);
            mav.addObject("error", "no such article");
            return mav;
        }
        Article dto = getArticleByIdAndModifyVisit(id);

        mav.addObject("dto", new CommentCreateDTO());
        mav.addObject("article", dto);
        mav.setViewName(ARTICLE);
        return mav;
    }

    @PostMapping(value = ADD_COMMENTS)
    public ModelAndView addComment(@Valid CommentCreateDTO dto, BindingResult result,
                                   ModelAndView mav) {
        String viewName = REDIRECT + ARTICLE_URL + SLASH + dto.getArticleId(),
                errMsg = "cannot save comment";
        if (result.hasFieldErrors()) {
            return setErrorMav("error in " + result.getFieldError().getField(),
                    mav, viewName);
        } else if (!articleRepository.existsById(dto.getArticleId())) {
            viewName = REDIRECT + SLASH + INDEX;
            return setErrorMav("no article", mav, viewName);
        } else {
            UpdateResult updateResult = saveCommentDTO(dto);
            oneModify(mav, viewName, errMsg, updateResult);
        }
        return mav;
    }

}
