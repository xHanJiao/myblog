package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.model.content.dto.CategoryNumDTO;
import com.xhan.myblog.model.content.dto.CommentCreateDTO;
import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.model.content.repo.Category;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

import static com.xhan.myblog.controller.ControllerConstant.*;
import static com.xhan.myblog.model.content.repo.ArticleState.PUBLISHED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.util.StringUtils.hasText;

@Controller
public class ArticleController extends BaseController {

    /**
     * 返回首页所需信息，其中包含最近的pageSize篇Article，并且把最近的
     * 一篇文章的内容提取出头一部分以便展示
     *
     * @return 返回ModelAndView，其中viewName 是INDEX常量
     */
    @GetMapping(value = {SLASH + INDEX, SLASH})
    public ModelAndView index(@RequestParam(required = false, defaultValue = "0") int page,
                              ModelAndView mav) {

        final int defaultPageSize = propertiesBean.getDefaultPageSize(),
                maxLen = propertiesBean.getShortcutLen();
        if (page < 0) page = 0;
        Page<Article> articles = getArticlesDueIsAdmin(defaultPageSize, page);
        int totalPage = articles.getTotalPages();
        if (page > totalPage - 1){
            page = totalPage - 1;
            articles = getArticlesDueIsAdmin(defaultPageSize, page);
        }
        articles.forEach(a -> a.convertToShortcutNoTag(maxLen));

        mav.setViewName(INDEX);
        mav.addObject("category", new Category());
        mav.addObject("currentPage", page);
        mav.addObject("articles", articles.getContent());
        return mav;
    }

    @ModelAttribute(CATE_NUM)
    public long getCreatedCategoryNums() {
        return categoryRepository.count();
    }

    @ModelAttribute(POST_NUM)
    public long getPostedArticle() {
        boolean isAdmin = isAdmin();
        String key = POST_NUM + isAdmin;
        Long postNum = cache.get(key);
        if (postNum == null) {
            if (isAdmin) {
                postNum = articleRepository.count();
            } else {
                postNum = (long) articleRepository.countByState(PUBLISHED.getState());
            }
            cache.set(key, postNum);
        }
        return postNum;
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
     *
     * @return
     */
    @ModelAttribute(name = "allCate")
    public List<CategoryNumDTO> getAllCateAndArticleNum() {
        return getCategoryNumDTOS();
    }

    @ModelAttribute(name = "greeting")
    public String greeting() {
        return hasText(propertiesBean.getGreeting()) ? propertiesBean.getGreeting() : "吃了吗";
    }

    @GetMapping(path = CATEGORY_URL)
    public String getCategory(Model model) {
        model.addAttribute("allCate", getCategoryNumDTOS());
        return CATEGORY;
    }

    /**
     * 分页显示一个分类中的文章，默认显示第一页，每页大小是10
     *
     * @param name     分类的名字，不能为空
     * @param page     页数，如果小于0会显示第0页，如果缺失则为第0页
     * @param pageSize 每页数量，默认是10，建议在请求时不要设置这个值
     * @return ModelAndView，如果无故障则去往 ARTICLE_LIST, 否则去往INDEX
     */
    @GetMapping(path = CATEGORY_URL + NAME_PATH_VAR)
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

        preProcessToArticleList(mav, page, pageSize, articles, nums, M_ALL_ARTICLES, M_ALL_ARTICLES_URL);
        return mav;
    }

    @GetMapping(path = ARTICLE_URL + ID_PATH_VAR)
    public ModelAndView getCertainArticle(@PathVariable final String id, ModelAndView mav) {
        if (!hasText(id)) {
            mav.setStatus(HttpStatus.BAD_REQUEST);
            mav.setViewName(INDEX);
            mav.addObject("error", "no such article");
            return mav;
        }
        Article dto = getArticleByIdAndModifyVisit(id);
        String metaName, metaURL;
        metaName = dto.getCategory();
        metaURL = M_CATE_URL + metaName;

        mav.addObject("dto", new CommentCreateDTO());
        mav.addObject("article", dto);
        mav.addObject("metaName", metaName);
        mav.addObject("metaURL", metaURL);
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
