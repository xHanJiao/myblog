package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.exceptions.content.ArticleNotFoundException;
import com.xhan.myblog.exceptions.content.CommentNotFoundException;
import com.xhan.myblog.model.content.*;
import com.xhan.myblog.utils.BlogUtils;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import static com.xhan.myblog.controller.ControllerConstant.*;
import static java.util.Collections.singletonMap;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.util.StringUtils.hasText;

@Controller
public class AdminController extends BaseController {

    @RequestMapping(value = {LOGIN_DISPATCH_URL})
    public String Login() {
        return REDIRECT + INDEX;
    }

    @GetMapping(value = LOGIN_URL)
    public String login() {
        return LOGIN;
    }

    @Secured(R_ADMIN)
    @GetMapping(value = SLASH + EDIT + ARTICLE_URL)
    public String getEditPage(Model model) {
        model.addAttribute("dto", new ArticleCreateDTO());
        model.addAttribute("modify", "");
        model.addAttribute("categories", categoryRepository.findAll());
        return EDIT;
    }

    @Secured(R_ADMIN)
    @PostMapping(value = ADD_URL + ARTICLE_URL, consumes = {APPLICATION_JSON_UTF8_VALUE, APPLICATION_JSON_VALUE})
    public ResponseEntity<?> addArticle(@RequestBody @Valid ArticleCreateDTO dto,
                                        BindingResult result) throws URISyntaxException {
        if (result.hasFieldErrors())
            return badRequest().body(result.getFieldError());

        Article article = saveArticleDTO(dto);
        return ResponseEntity
                .created(new URI("/article/" + article.getId()))
                .lastModified(System.currentTimeMillis()).build();
    }

    private UpdateResult modifyDeleted(Query unDeletedIdQuery, boolean b) {
        return mongoTemplate.update(Article.class)
                .matching(unDeletedIdQuery)
                .apply(new Update().set("deleted", b)).all();
    }

    @Secured(R_ADMIN)
    @PostMapping(path = DELETE_URL + COMMENT_URL)
    public ModelAndView delComments(@Valid DelCommDTO dto, BindingResult result,
                                    ModelAndView mav) {
        String viewName = REDIRECT + ARTICLE_URL + SLASH + dto.getArticleId(),
                errMsg = "cannot delete comment";

        if (result.hasFieldErrors()) {
            return setErrorMav("error in " + result.getFieldError().getField(),
                    mav, viewName);
        } else if (!articleRepository.existsById(dto.getArticleId())) {
            return setErrorMav("no article", mav, viewName);
        } else {
            UpdateResult updateResult = mongoTemplate.update(Article.class)
                    .matching(query(where("id").is(dto.getArticleId())))
                    .apply(new Update().pull("comments", singletonMap("content", dto.getContent())))
                    .all();
            return oneModify(mav, viewName, errMsg, updateResult);
        }
    }

    @Secured(R_ADMIN)
    @PostMapping(path = DELETE_URL + COMMENT_URL, consumes = {APPLICATION_JSON_UTF8_VALUE, APPLICATION_JSON_VALUE})
    public ResponseEntity<?> delComments(@RequestBody @Valid DelCommDTO dto,
                                         BindingResult result) {
        if (result.hasFieldErrors())
            return badRequest().body(result.getFieldError());

        Article article = articleRepository.findById(dto.getArticleId())
                .orElseThrow(ArticleNotFoundException::new);

        Comment toDelete = new Comment();
        for (Comment comment : article.getComments()) {
            if (comment.getContent().equals(dto.getContent())) {
                toDelete = comment;
                break;
            }
        }
        if (!hasText(toDelete.getContent()))
            throw new CommentNotFoundException(dto.getContent());

        UpdateResult updateResult = mongoTemplate.update(Article.class)
                .matching(query(where("id").is(dto.getArticleId())))
                .apply(new Update().pull("comments", toDelete)).all();

        return updateResult.getModifiedCount() == 1
                ? ok("success")
                : ResponseEntity.status(500).body("cannot delete comment");
    }

    private Query getIdQueryWithDeleteState(String id, boolean state) {
        return query(where("id").is(id).and("deleted").is(state));
    }

    @Secured(R_ADMIN)
    @PostMapping(path = DELETE_URL + ARTICLE_URL + ID_PATH_VAR)
    public ResponseEntity<?> deleteArticle(@PathVariable String id) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");

        UpdateResult result =
                modifyDeleted(getIdQueryWithDeleteState(id, false), true);

        return result.getModifiedCount() == 1
                ? ok("modified")
                : badRequest().body("check id you input");
    }

    @Secured(R_ADMIN)
    @PostMapping(path = RECOVER_URL + ARTICLE_URL + ID_PATH_VAR)
    public ResponseEntity<?> recoverArticle(@PathVariable String id) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");

        UpdateResult result =
                modifyDeleted(getIdQueryWithDeleteState(id, true), false);

        return result.getModifiedCount() == 1
                ? ok("modified")
                : badRequest().body("check id you input");
    }

    @Secured(R_ADMIN)
    @PostMapping(value = ADD_URL + ARTICLE_URL)
    public ModelAndView addArticle(@Valid ArticleCreateDTO dto, BindingResult result,
                                   ModelAndView mav, RedirectAttributes model) {
        if (result.hasFieldErrors()) {
            mav = setErrorMav(result.getFieldError().getField(), mav, EDIT);
            mav.addObject("dto", dto);
            return mav;
        }
        Article article = saveArticleDTO(dto);
        model.addFlashAttribute("justSaved", article.getTitle());
        mav.setViewName(REDIRECT + SLASH + INDEX);
        return mav;
    }

    private Article saveArticleDTO(@RequestBody @Valid ArticleCreateDTO dto) {
        Article article = dto.toArticle();
        article = articleRepository.save(article);
        return article;
    }

    @Secured(R_ADMIN)
    @GetMapping(value = SLASH + EDIT + ARTICLE_URL + ID_PATH_VAR)
    public String getEditPage(Model model, @PathVariable String id) {
        ContentTitleIdDTO dto = articleRepository.getById(id)
                .orElseThrow(ArticleNotFoundException::new);
        model.addAttribute("dto", dto);
        model.addAttribute("modify", id);
        model.addAttribute("categories", categoryRepository.findAll());
        return EDIT;
    }


    @Secured(R_ADMIN)
    @PostMapping(value = SLASH + MODIFY + ARTICLE_URL + ID_PATH_VAR)
    public ModelAndView modifyArticle(@PathVariable String id, @Valid ArticleCreateDTO dto,
                                      BindingResult result, ModelAndView mav) {
        if (result.hasFieldErrors()) {
            setErrorMav(result.getFieldError().getField(), mav, EDIT);
            // 如果修改成功，就直接重定向去其他页面，如果修改失败，就还要把修改信息添加返回回去
            mav.addObject("dto", dto);
            mav.addObject("modify", id);
            mav.addObject("categories", categoryRepository.findAll());
        } else {
            UpdateResult updateResult = mongoTemplate.update(Article.class)
                    .matching(query(where("id").is(id)))
                    .apply(new Update()
                            .set("content", dto.getContent())
                            .set("title", dto.getTitle())
                            .set("commentEnable", dto.getCommentEnable())
                            .set("category", dto.getCategory())
                            .set("finished", dto.getFinished())).first();

            if (updateResult.getModifiedCount() == 0) {
                mav.addObject("dto", dto);
                mav.addObject("modify", id);
                mav.addObject("error", "cannot modify this article");
                mav.addObject("categories", categoryRepository.findAll());
//                mav.setViewName(EDIT);
                mav.setViewName(REDIRECT + ARTICLE_URL + SLASH + id);
            } else {
                mav.setViewName(REDIRECT + ARTICLE_URL + SLASH + id);
            }
        }
        return mav;
    }

    @Secured(R_ADMIN)
    @PostMapping(value = ADD_URL + SLASH + CATEGORY)
    public ModelAndView addCategory(@Valid Category category, BindingResult result,
                                    ModelAndView mav) {
        if (result.hasFieldErrors()) {
            setErrorMav(result.getFieldError().getField(), mav, EDIT);
        } else {
            category.setCreateTime(BlogUtils.getCurrentTime());
            categoryRepository.save(category);
            mav.setViewName(REDIRECT + SLASH + INDEX);
        }
        return mav;
    }

}
