package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.exceptions.content.ArticleNotFoundException;
import com.xhan.myblog.exceptions.content.BlogException;
import com.xhan.myblog.model.content.dto.NameDTO;
import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.model.content.repo.ArticleState;
import com.xhan.myblog.model.content.repo.Category;
import com.xhan.myblog.model.content.repo.Comment;
import com.xhan.myblog.model.content.dto.CommentCreateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.xhan.myblog.controller.ControllerConstant.*;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.util.StringUtils.hasText;

@Controller
public class ArticleController extends BaseController {

    @GetMapping(value = {SLASH + INDEX, SLASH})
    public ModelAndView index(@RequestParam(defaultValue = "5") Integer pageSize,
                              @RequestParam(defaultValue = "0") Integer page,
                              ModelAndView mav) {
        Page<Article> articles = getPagedArticles(page, pageSize);
        Article showBoard = articles.isEmpty() ? emptyArticle : articles.getContent().get(0);
        showBoard.convertToShortcut();
        Page<Category> categories = categoryRepository.findAll(of(0, 5));
        mav.addObject("headCate", categories.getContent());
        mav.addObject("allCate", categoryRepository.findAll());
        mav.setViewName(INDEX);
        mav.addObject("category", new Category());
        mav.addObject("showBoard", showBoard);
        mav.addObject("articles", articles.getContent());

        return mav;
    }

    @GetMapping(value = SLASH + CATEGORIES)
    public ResponseEntity<?> getCategories() {
        List<String> categories = categoryRepository.findAllNames()
                .stream().map(NameDTO::getName)
                .collect(toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping(value = SLASH + "lessCate")
    public ResponseEntity<?> getLessCategories() {
        List<String> categories = categoryRepository.findAll(of(0, 5))
                .getContent().stream()
                .map(Category::getName)
                .collect(toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping(path = SLASH + CATEGORY + NAME_PATH_VAR)
    public ModelAndView getArticlesOfCategory(@PathVariable String name, ModelAndView mav,
                                              @RequestParam(defaultValue = "0") Integer page,
                                              @RequestParam(defaultValue = "5") Integer pageSize) {
        if (!hasText(name)) {
            mav.setViewName(INDEX);
            mav.setStatus(HttpStatus.BAD_REQUEST);
            mav.addObject("error", "分类名不能为空");
            return mav;
        }
        Page<Article> articles = getPagedArticles(page, pageSize, name);
        int nums = articleRepository.countByCategoryAndState(name, ArticleState.PUBLISHED.getState());

        preProcessToArticleList(mav, page, pageSize, articles, nums, M_CATE, M_CATE_URL);
        mav.addObject("cateName", name);
        return mav;
    }

    @GetMapping(path = ARTICLE_URL, consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<?> getArticles(@RequestParam(defaultValue = "0") Integer page,
                                         @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<Article> articles = getPagedArticles(page, pageSize);
        return ok(articles.getContent());
    }

    @GetMapping(path = ARTICLE_URL)
    public ModelAndView getArticles(@RequestParam(defaultValue = "0") Integer page,
                                    @RequestParam(defaultValue = "5") Integer pageSize,
                                    ModelAndView mav) {
        Page<Article> articles = getPagedArticles(page, pageSize);
        int nums = articleRepository.countByState(ArticleState.PUBLISHED.getState());

        preProcessToArticleList(mav, page, pageSize, articles, nums, ALL_ARTICLE, M_ALL_ARTICLE_URL);
        return mav;
    }

    private Page<Article> getPagedArticles(Integer page, Integer pageSize) {
        MyPageRequest mpr = new MyPageRequest(page, pageSize).invoke();
        PageRequest pageRequest = of(mpr.getPage(), mpr.getPageSize(), DESC, "createTime");
        return articleRepository.findAllByState(ArticleState.PUBLISHED.getState(), pageRequest);
    }

    private Page<Article> getPagedArticles(Integer page, Integer pageSize, String cateName) {
        MyPageRequest mpr = new MyPageRequest(page, pageSize).invoke();
        PageRequest pageRequest = of(mpr.getPage(), mpr.getPageSize(), DESC, "createTime");
        return articleRepository.findAllByStateAndCategory(ArticleState.PUBLISHED.getState(), cateName, pageRequest);
    }

    @GetMapping(path = ARTICLE_URL + ID_PATH_VAR,
            produces = {APPLICATION_JSON_UTF8_VALUE, APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getCertainArticle(@PathVariable String id) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");
        Article dto = articleRepository
                .findByStateAndId(ArticleState.PUBLISHED.getState(), id)
                .orElseThrow(ArticleNotFoundException::new);
        return ok(singletonMap("article", dto));
    }

    @GetMapping(path = ARTICLE_URL + ID_PATH_VAR)
    public ModelAndView getCertainArticle(@PathVariable String id, ModelAndView mav) {
        if (!hasText(id)) {
            mav.setStatus(HttpStatus.BAD_REQUEST);
            mav.setViewName(INDEX);
            mav.addObject("error", "no such article");
            return mav;
        }
        Article dto = articleRepository.findByStateAndId(ArticleState.PUBLISHED.getState(), id)
                .orElseThrow(ArticleNotFoundException::new);
        mav.addObject("dto", new CommentCreateDTO());
        mav.addObject("article", dto);
        mav.setViewName(ARTICLE);

        return mav;
    }

    @PostMapping(path = ADD_COMMENTS, consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<?> addComment(@RequestBody @Valid CommentCreateDTO dto,
                                        BindingResult result) throws URISyntaxException {
        if (result.hasFieldErrors())
            return badRequest().body(result.getFieldError());
        else if (!articleRepository.existsById(dto.getArticleId()))
            return badRequest().body("no article");
        UpdateResult updateResult = saveComment(dto);

        return updateResult.getModifiedCount() == 1
                ? ResponseEntity.created(new URI(ARTICLE_URL + SLASH + dto.getArticleId())).body("success")
                : ResponseEntity.status(500).body("cannot save comment");
    }

    private UpdateResult saveComment(@RequestBody CommentCreateDTO dto) {
        dto.preProcessBeforeSave();
        Comment comment = dto.toComment();
        return mongoTemplate.update(Article.class)
                .matching(query(where("id").is(dto.getArticleId())))
                .apply(new Update().push("comments", comment)).all();
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
            return setErrorMav("no article", mav, viewName);
        } else {
            // todo 这里要处理一下游客的内容
            UpdateResult updateResult = saveComment(dto);
            oneModify(mav, viewName, errMsg, updateResult);
        }
        return mav;
    }


    @ExceptionHandler(value = BlogException.class)
    public ResponseEntity<?> handleException(BlogException e) {
        return badRequest().body(e.getMessage());
    }

}
