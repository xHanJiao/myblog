package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.model.content.dto.CommentCreateDTO;
import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.model.content.repo.Category;
import com.xhan.myblog.model.prj.IdTitleTimeStateContentPrj;
import com.xhan.myblog.model.prj.IdTitleTimeStatePrj;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.xhan.myblog.controller.ControllerConstant.*;
import static com.xhan.myblog.model.content.repo.ArticleState.PUBLISHED;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Sort.Direction.DESC;
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
        Page<IdTitleTimeStateContentPrj> articles = getBriefAndContentDueIsAdmin(defaultPageSize, page);
        int totalPage = articles.getTotalPages();
        if (page > totalPage - 1){
            page = totalPage - 1;
            articles = getBriefAndContentDueIsAdmin(defaultPageSize, page);
        }
        List<IdTitleTimeStateContentPrj> results = articles
                .stream().map(a -> a.convertToShortcutNoTag(maxLen))
                .collect(Collectors.toList());

        mav.setViewName(INDEX);
        mav.addObject("category", new Category());
        mav.addObject("currentPage", page);
        mav.addObject("articles", results);
        return mav;
    }

    private Page<IdTitleTimeStateContentPrj> getBriefAndContentDueIsAdmin(int pageSize, int page) {
        MyPageRequest mpr = new MyPageRequest(page, pageSize).invoke();
        PageRequest pageRequest = of(mpr.getPage(), mpr.getPageSize(), DESC, "createTime");
        return authorityHelper.isAdmin()
                ? articleRepository.getAllBy(pageRequest)
                : articleRepository.getAllByState(PUBLISHED.getState(), pageRequest);
    }

    @ModelAttribute(CATE_NUM)
    public long getCreatedCategoryNums() {
        return categoryRepository.count();
    }

    @ModelAttribute(POST_NUM)
    public long getPostedArticle() {
        boolean isAdmin = authorityHelper.isAdmin();
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

    @ModelAttribute(name = "brand")
    public String getBrand() {
        return propertiesBean.getBrand();
    }

    @ModelAttribute(name = "greeting")
    public String greeting() {
        return hasText(propertiesBean.getGreeting()) ? propertiesBean.getGreeting() : "小韩博客";
    }

    @GetMapping(path = CATEGORY_URL)
    public String getCategory(Model model) {
        model.addAttribute("allCate", getCategoryNumDTOS());
        return CATEGORY;
    }

    @PostMapping(value = SLASH + "search")
    public String searchByTitle(@RequestParam String title, Model model) {
        boolean isAdmin = authorityHelper.isAdmin();
        List<IdTitleTimeStatePrj> articles = isAdmin
                ? articleRepository.findByTitleRegex(title)
                : articleRepository.findByTitleRegexAndState(title, PUBLISHED.getState());
        model.addAttribute("articles", articles);
        model.addAttribute("meta", M_SEARCH);
        model.addAttribute("allPages", Collections.singletonList(0));
        model.addAttribute("currentPage", 0);
        return "articles";
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
        Page<IdTitleTimeStatePrj> articles = getPagedArticles(page, pageSize, name, authorityHelper.isAdmin());
        int nums = articleRepository.countByCategoryAndState(name, PUBLISHED.getState());

        preProcessToArticleList(mav, page, pageSize, articles, nums, M_CATE, M_CATE_URL);
        mav.addObject("cateName", name);
        return mav;
    }

    @GetMapping(path = ARTICLE_URL, consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<?> getArticles(@RequestParam(defaultValue = "0") Integer page,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<IdTitleTimeStatePrj> articles = getArticlesDueIsAdmin(pageSize, page);
        return ok(articles.getContent());
    }

    @GetMapping(path = ARTICLE_URL)
    public ModelAndView getArticles(@RequestParam(defaultValue = "0") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    ModelAndView mav) {
        Page<IdTitleTimeStatePrj> articles = getArticlesDueIsAdmin(pageSize, page);
        int nums = articleRepository.countByState(PUBLISHED.getState());

        preProcessToArticleList(mav, page, pageSize, articles, nums, M_ALL_ARTICLES, M_ALL_ARTICLES_URL);
        return mav;
    }

    @GetMapping(path = ARTICLE_URL + ID_PATH_VAR)
    public ModelAndView getCertainArticle(@PathVariable final String id, ModelAndView mav) {
        if (!hasText(id)) {
            mav.setViewName(INDEX);
            mav.setStatus(HttpStatus.BAD_REQUEST);
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
