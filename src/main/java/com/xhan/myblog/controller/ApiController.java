package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.exceptions.content.ArticleNotFoundException;
import com.xhan.myblog.model.content.dto.CommentCreateDTO;
import com.xhan.myblog.model.content.repo.Article;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

import static com.xhan.myblog.controller.ControllerConstant.*;
import static java.util.Collections.singletonMap;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.util.StringUtils.hasText;

@Controller
public class ApiController extends BaseController {

    @GetMapping(value = API_URL + ARTICLE_URL)
    public ResponseEntity<?> getArticles(@RequestParam(defaultValue = "7") Integer pageSize,
                                         @RequestParam(defaultValue = "0") Integer page) {
        return ok(getArticlesDueIsAdmin(pageSize, page).getContent());
    }

    @GetMapping(path = API_URL + CONTENT_URL + ID_PATH_VAR,
            produces = {APPLICATION_JSON_UTF8_VALUE, APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getCertainArticle(@PathVariable String id) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");
        Article dto = getArticleByIdAndModifyVisit(id);
        return ok(singletonMap("article", dto.getContent()));
    }

    /**
     * 保存文章内容的ajax后端接口，前端将id和content发来，后端根据id而更新MongoDB中的
     * 对应文档的内容。
     * @param id 包含了文章id的路径参数
     * @param content 文章内容，这里可以为空，但是为空则不会更新
     * @return 不论是否更新，都会返回200状态码，但是更新的话会返回更新的文章的条目数（1）
     */
    @PostMapping(path = API_URL + CONTENT_URL + ID_PATH_VAR,
            produces = {APPLICATION_JSON_UTF8_VALUE, APPLICATION_JSON_VALUE})
    public ResponseEntity<?> saveContentOfCertainArticle(@PathVariable String id,
                                                         @RequestParam(required = false) String content) {
        if (hasText(content)) {
            UpdateResult result = mongoTemplate.update(Article.class)
                    .matching(getIdQuery(id))
                    .apply(new Update().set("content", content)).first();
            return ok(result.getModifiedCount());
        }
        return ok().build();
    }

    @GetMapping(value = API_URL + ARTICLE_URL + ID_PATH_VAR)
    public ResponseEntity<?> getArticleById(@PathVariable String id) {
        if (!hasText(id))
            throw new ArticleNotFoundException();
        return ok(getArticleByIdAndModifyVisit(id));
    }

    @PostMapping(path = ADD_COMMENTS, consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<?> addComment(@RequestBody @Valid CommentCreateDTO dto,
                                        BindingResult result) {
        if (result.hasFieldErrors())
            return badRequest().body(result.getFieldError());
        else if (!articleRepository.existsById(dto.getArticleId()))
            return badRequest().body("no article");
        UpdateResult updateResult = saveCommentDTO(dto);

        return updateResult.getModifiedCount() == 1
                ? ResponseEntity.created(URI.create(ARTICLE_URL + SLASH + dto.getArticleId())).body("success")
                : ResponseEntity.status(500).body("cannot save comment");
    }

    public ResponseEntity<?> getAllCateAndArticleNum() {
        return ResponseEntity.ok(getCategoryNumDTOS());
    }

}
