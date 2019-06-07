package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.exceptions.content.ArticleNotFoundException;
import com.xhan.myblog.exceptions.content.BlogException;
import com.xhan.myblog.exceptions.content.CommentNotFoundException;
import com.xhan.myblog.model.content.*;
import com.xhan.myblog.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.util.StringUtils.hasText;

@Controller
public class ContentController {

    private final MongoTemplate mongoTemplate;
    private final ArticleRepository articleRepository;
    private final Article emptyArticle;

    @Autowired
    public ContentController(MongoTemplate mongoTemplate, ArticleRepository articleRepository) {
        this.mongoTemplate = mongoTemplate;
        this.articleRepository = articleRepository;
        this.emptyArticle = new Article();
        emptyArticle.setTitle("当前暂无可用文章");
        emptyArticle.setContent("敬请期待");
    }

    @GetMapping(value = ARTICLE_URL + SLASH + EDIT)
    public String getEditPage(Model model) {
        model.addAttribute("dto", new ArticleCreateDTO());
        return EDIT;
    }

    @PostMapping(value = ARTICLE_URL + ADD, consumes = {APPLICATION_JSON_UTF8_VALUE, APPLICATION_JSON_VALUE})
    public ResponseEntity<?> addArticle(@RequestBody @Valid ArticleCreateDTO dto, BindingResult result) throws URISyntaxException {
        if (result.hasFieldErrors())
            return badRequest().body(result.getFieldError());

        Article article = saveArticleDTO(dto);
        return ResponseEntity
                .created(new URI("/article/" + article.getId()))
                .lastModified(System.currentTimeMillis()).build();
    }

    @PostMapping(value = ARTICLE_URL + ADD)
    public ModelAndView addArticle(@Valid ArticleCreateDTO dto, BindingResult result,
                                   ModelAndView mav, RedirectAttributes model) {
        if (result.hasFieldErrors()) {
            mav.addObject("error", result.getFieldError());
            mav.addObject("dto", dto);
            mav.setViewName(EDIT);
            mav.setStatus(HttpStatus.BAD_REQUEST);
            return mav;
        }
        Article article = saveArticleDTO(dto);
        model.addFlashAttribute("justSaved", article.getTitle());
        mav.setViewName(REDIRECT + INDEX);
        return mav;
    }

    @GetMapping(value = {SLASH + INDEX, SLASH})
    public ModelAndView index(ModelAndView mav, @RequestParam(defaultValue = "0") Integer page,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<IdTitleTimeDTO> articles = getPagedArticles(page, pageSize);
        Page<Article> articleList = articleRepository.findAll(of(0, 1, DESC, "createTime"));
        Article showBoard = articleList.isEmpty() ? emptyArticle : articleList.getContent().get(0);
        showBoard.setContent(
                showBoard.getContent().length() > 80
                        ? showBoard.getContent().substring(0, 80) + "..."
                        : showBoard.getContent()
        );

        mav.setViewName(INDEX);
        mav.addObject("showBoard", showBoard);
        mav.addObject("articles", articles);
        return mav;
    }

    private boolean isIntValid(Integer i) {
        return i != null && i >= 0;
    }

    private Article saveArticleDTO(@RequestBody @Valid ArticleCreateDTO dto) {
        Article article = dto.toDO();
        article = articleRepository.save(article);
        return article;
    }

    @GetMapping(path = ARTICLE_URL)
    public ResponseEntity<?> getArticles(@RequestParam(defaultValue = "0") Integer page,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<IdTitleTimeDTO> articles = getPagedArticles(page, pageSize);

        return ok(articles.getContent());
    }

    private Page<IdTitleTimeDTO> getPagedArticles(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer pageSize) {
        page = isIntValid(page) ? page : 0;
        pageSize = isIntValid(pageSize) ? pageSize : 0;
        return articleRepository.findAllByDeleted(false, of(page, pageSize));
    }

    @GetMapping(path = ARTICLE_URL + ID_PATH_VAR,
            produces = {APPLICATION_JSON_UTF8_VALUE, APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getCertainArticle(@PathVariable String id) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");

        TitleContentCommTimeDTO dto = articleRepository.findByDeletedAndId(false, id)
                .orElseThrow(() -> new ArticleNotFoundException("id not found"));
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

        TitleContentCommTimeDTO dto = articleRepository.findByDeletedAndId(false, id)
                .orElseThrow(() -> new ArticleNotFoundException("id not found"));
        // todo 这里还是应该把它分拆一下的
        mav.addObject("article", dto);
        mav.setViewName(ARTICLE);
        return mav;
    }

    private Query getIdQueryWithDeleteState(String id, boolean state) {
        return Query.query(Criteria.where("id").is(id).and("deleted").is(state));
    }

    @PostMapping(path = ARTICLE_URL + DELETE + ID_PATH_VAR)
    public ResponseEntity<?> deleteArticle(@PathVariable String id) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");

        UpdateResult result =
                modifyDeleted(getIdQueryWithDeleteState(id, false), true);

        return result.getModifiedCount() == 1
                ? ok("modified")
                : badRequest().body("check id you input");
    }

    @PostMapping(path = ARTICLE_URL + RECOVER + ID_PATH_VAR)
    public ResponseEntity<?> recoverArticle(@PathVariable String id) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");

        UpdateResult result =
                modifyDeleted(getIdQueryWithDeleteState(id, true), false);

        return result.getModifiedCount() == 1
                ? ok("modified")
                : badRequest().body("check id you input");
    }

    @PostMapping(path = ADD_COMMENTS)
    public ResponseEntity<?> addComment(@RequestBody @Valid CommentCreateDTO dto,
                                        BindingResult result) {
        if (result.hasFieldErrors())
            return badRequest().body(result.getFieldError());
        if (!articleRepository.existsById(dto.getArticleId()))
            return badRequest().body("no article");
        dto.preProcessBeforeSave();
        Comment comment = dto.toComment();
        UpdateResult updateResult = mongoTemplate.update(Article.class)
                .matching(Query.query(Criteria.where("id").is(dto.getArticleId())))
                .apply(new Update().push("comments", comment)).all();

        return updateResult.getModifiedCount() == 1
                ? ok("success")
                : ResponseEntity.status(500).body("cannot save comment");
    }

    @PostMapping(path = DEL_COMMENTS)
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
                .matching(Query.query(Criteria.where("id").is(dto.getArticleId())))
                .apply(new Update().pull("comments", toDelete)).all();

        return updateResult.getModifiedCount() == 1
                ? ok("success")
                : ResponseEntity.status(500).body("cannot delete comment");
    }

    private UpdateResult modifyDeleted(Query unDeletedIdQuery, boolean b) {
        return mongoTemplate.update(Article.class)
                .matching(unDeletedIdQuery)
                .apply(new Update().set("deleted", b)).all();
    }

    @ExceptionHandler(value = BlogException.class)
    public ResponseEntity<?> handleException(BlogException e) {
        return badRequest().body(e.getMessage());
    }

}
