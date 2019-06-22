package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.exceptions.content.ArticleNotFoundException;
import com.xhan.myblog.model.content.dto.CommentCreateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

import static com.xhan.myblog.controller.ControllerConstant.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.util.StringUtils.hasText;

@Controller
public class ApiController extends BaseController{

    @GetMapping(value = API_URL + ARTICLE_URL)
    public ResponseEntity<?> getArticles(@RequestParam(defaultValue = "7") Integer pageSize,
                                                     @RequestParam(defaultValue = "0") Integer page) {
        return ok(getArticlesDueIsAdmin(pageSize, page).getContent());
    }

    @GetMapping(value = API_URL + ARTICLE_URL + ID_PATH_VAR)
    public ResponseEntity<?> getArticleById(@PathVariable String id) {
        if (!hasText(id))
            throw new ArticleNotFoundException();
        return ok(getArticleByIdAndModifyVisit(id));
    }

    @PostMapping(path = ADD_COMMENTS, consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
    public ResponseEntity<?> addComment(@RequestBody @Valid CommentCreateDTO dto,
                                        BindingResult result){
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
