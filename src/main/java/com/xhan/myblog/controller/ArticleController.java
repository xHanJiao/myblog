package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.exceptions.content.ArticleNotFoundException;
import com.xhan.myblog.exceptions.content.BlogException;
import com.xhan.myblog.model.content.Article;
import com.xhan.myblog.model.content.Category;
import com.xhan.myblog.model.content.Comment;
import com.xhan.myblog.model.content.CommentCreateDTO;
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

import static com.xhan.myblog.controller.ControllerConstant.*;
import static java.util.Collections.singletonMap;
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
    public ModelAndView index(ModelAndView mav, HttpSession session,
                              @SessionAttribute(value = IS_ADMIN, required = false) Boolean isAdmin,
                              @RequestParam(defaultValue = "0") Integer page,
                              @RequestParam(defaultValue = "7") Integer pageSize) {
        isAdmin = checkAndSetIsAdmin(session, isAdmin);
        Page<Article> articles = getPagedArticles(page, pageSize, isAdmin);
        Page<Article> articleList =
                articleRepository.findAll(of(0, 1, DESC, "createTime"));
        Article showBoard = articleList.isEmpty() ? emptyArticle : articleList.getContent().get(0);
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

    private Boolean checkAndSetIsAdmin(HttpSession session, Boolean isAdmin) {
        if (isAdmin == null) {
            session.setAttribute(IS_ADMIN, false);
        }
        return false;
    }

    private boolean isIntValid(Integer i) {
        return i != null && i >= 0;
    }

    @GetMapping(path = SLASH + CATEGORY + NAME_PATH_VAR)
    public ModelAndView getArticlesOfCategory(@PathVariable String name, ModelAndView mav,
                                              @RequestParam(defaultValue = "0") Integer page,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @SessionAttribute(value = IS_ADMIN, required = false) Boolean isAdmin,
                                              HttpSession session) {
        if (!hasText(name)) {
            mav.setViewName(INDEX);
            mav.setStatus(HttpStatus.BAD_REQUEST);
            mav.addObject("error", "分类名不能为空");
            return mav;
        }
        isAdmin = checkAndSetIsAdmin(session, isAdmin);
        Page<Article> articles = getPagedArticles(page, pageSize, isAdmin, name);
        mav.setViewName(ARTICLE_LIST);
        mav.addObject("articles", articles.getContent());
        return mav;
    }

    /**
     * 获取一个分类下的文章
     *
     */
    private Page<Article> getPagedArticles(Integer page, Integer pageSize, Boolean isAdmin, String cateName) {
        page = isIntValid(page) ? page : 0;
        pageSize = isIntValid(pageSize) ? pageSize : 0;
        isAdmin = isAdmin == null ? false : isAdmin;
        PageRequest request = of(page, pageSize, DESC, "createTime");
        return isAdmin
                ? articleRepository.findAllByCategory(cateName, request)
                : articleRepository.findAllByPublishedAndFinishedAndCategory(false,
                true, cateName, request);
    }

    @GetMapping(path = ARTICLE_URL, consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<?> getArticles(@RequestParam(defaultValue = "0") Integer page,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @SessionAttribute(value = IS_ADMIN, required = false) Boolean isAdmin,
                                         HttpSession session) {
        isAdmin = checkAndSetIsAdmin(session, isAdmin);
        Page<Article> articles = getPagedArticles(page, pageSize, isAdmin);
        return ok(articles.getContent());
    }

    @GetMapping(path = ARTICLE_URL)
    public ModelAndView getArticles(@RequestParam(defaultValue = "0") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @SessionAttribute(value = IS_ADMIN, required = false) Boolean isAdmin,
                                    ModelAndView mav, HttpSession session) {
        isAdmin = checkAndSetIsAdmin(session, isAdmin);
        Page<Article> articles = getPagedArticles(page, pageSize, isAdmin);
        mav.setViewName(ARTICLE_LIST);
        mav.addObject("articles", articles.getContent());
        return mav;
    }

    private Page<Article> getPagedArticles(@RequestParam(defaultValue = "0") Integer page,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           Boolean isAdmin) {
        page = isIntValid(page) ? page : 0;
        pageSize = isIntValid(pageSize) ? pageSize : 0;
        isAdmin = isAdmin == null ? false : isAdmin;
        return isAdmin
                ? articleRepository.findAll(of(page, pageSize, DESC, "createTime"))
                : articleRepository.findAllByPublishedAndFinished(false, true, of(page, pageSize,
                DESC, "createTime"));
    }

    @GetMapping(path = ARTICLE_URL + ID_PATH_VAR,
            produces = {APPLICATION_JSON_UTF8_VALUE, APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getCertainArticle(@PathVariable String id, HttpSession session,
                                               @SessionAttribute(value = IS_ADMIN, required = false) Boolean isAdmin) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");
        isAdmin = checkAndSetIsAdmin(session, isAdmin);

        Article dto = isAdmin
                ? articleRepository.findById(id).orElseThrow(ArticleNotFoundException::new)
                : articleRepository.findByPublishedAndFinishedAndId(false, true, id)
                .orElseThrow(ArticleNotFoundException::new);
        return ok(singletonMap("article", dto));
    }

    @GetMapping(path = ARTICLE_URL + ID_PATH_VAR)
    public ModelAndView getCertainArticle(@PathVariable String id,
                                          ModelAndView mav, HttpSession session,
                                          @SessionAttribute(value = IS_ADMIN, required = false) Boolean isAdmin) {
        if (!hasText(id)) {
            mav.setStatus(HttpStatus.BAD_REQUEST);
            mav.setViewName(INDEX);
            mav.addObject("error", "no such article");
            return mav;
        }
        isAdmin = checkAndSetIsAdmin(session, isAdmin);

        Article dto = isAdmin
                ? articleRepository.findById(id).orElseThrow(ArticleNotFoundException::new)
                : articleRepository.findByPublishedAndFinishedAndId(false, true, id)
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
