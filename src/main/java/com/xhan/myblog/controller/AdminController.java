package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.annotation.CacheInvalid;
import com.xhan.myblog.exceptions.content.ArticleNotFoundException;
import com.xhan.myblog.exceptions.content.BlogException;
import com.xhan.myblog.exceptions.content.CategoryNotFoundException;
import com.xhan.myblog.exceptions.content.CommentNotFoundException;
import com.xhan.myblog.model.content.dto.ArticleCreateDTO;
import com.xhan.myblog.model.content.dto.ArticleHistoryIdDTO;
import com.xhan.myblog.model.content.dto.DelCommDTO;
import com.xhan.myblog.model.content.dto.HistoryCreateDTO;
import com.xhan.myblog.model.content.repo.*;
import com.xhan.myblog.model.prj.CategoryStatePrj;
import com.xhan.myblog.model.prj.HistoryRecordsPrj;
import com.xhan.myblog.model.prj.IdTitleTimeStatePrj;
import com.xhan.myblog.model.user.Admin;
import com.xhan.myblog.model.user.Guest;
import com.xhan.myblog.model.user.ModifyDTO;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static com.xhan.myblog.controller.ControllerConstant.*;
import static com.xhan.myblog.model.content.repo.ArticleState.*;
import static com.xhan.myblog.model.prj.HistoryShowDTO.getHistoryShowDTO;
import static java.util.Collections.singletonMap;
import static org.apache.tomcat.util.http.fileupload.IOUtils.copy;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.util.StringUtils.getFilenameExtension;
import static org.springframework.util.StringUtils.hasText;

@Controller
public class AdminController extends BaseController {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ControllerPropertiesBean propertiesBean;

    @ModelAttribute(name = "brand")
    public String getBrand() {
        return propertiesBean.getBrand();
    }

    @RequestMapping(value = {LOGIN_DISPATCH_URL})
    public String Login(HttpSession session, RedirectAttributes model) {
        model.addFlashAttribute(IS_ADMIN, true);
        session.setAttribute(IS_ADMIN, true);
        return REDIRECT + INDEX;
    }

    @ModelAttribute(name = "greeting")
    public String greeting() {
        return hasText(propertiesBean.getGreeting()) ? propertiesBean.getGreeting() : "吃了吗";
    }

    @GetMapping(value = MODI_ADMIN_URL)
    public String modiAdmin() {
        return MODI_ADMIN;
    }

    @PostMapping(value = MODI_ADMIN_URL)
    public ModelAndView modiAdmin(@Valid ModifyDTO dto, BindingResult result,
                                  ModelAndView mav) {
        if (result.hasFieldErrors()) {
            mav.setStatus(HttpStatus.BAD_REQUEST);
            mav.setViewName(REDIRECT + SLASH + INDEX);
        } else {
            Admin admin = mongoTemplate.findOne(query(where("account").is(dto.getAccount())),
                    Admin.class, Guest.COLLECTION_NAME);
            if (admin == null) throw new BlogException();
            if (passwordEncoder.matches(dto.getPassword(), admin.getPassword())
                    && dto.isNewPwdValid()) {
                String newPwd = passwordEncoder.encode(dto.getNewPwd());
                admin.setPassword(newPwd);
                mongoTemplate.save(admin, Guest.COLLECTION_NAME);
                mav.setViewName(REDIRECT + SLASH + LOGIN);
            }
        }
        return mav;
    }

    @GetMapping(value = LOGIN_URL)
    public String login() {
        return LOGIN;
    }

    @Secured(R_ADMIN)
    @GetMapping(value = EDIT_URL + ARTICLE_URL)
    public String getEditPage(Model model) {
        model.addAttribute("dto", new ArticleCreateDTO());
        model.addAttribute("modify", "");
        model.addAttribute("categories", categoryRepository.findAll());
        return EDIT;
    }

    @Secured(R_ADMIN)
    @CacheInvalid
    @PostMapping(value = ADD_URL + ARTICLE_URL,
            consumes = {APPLICATION_JSON_UTF8_VALUE, APPLICATION_JSON_VALUE})
    public ResponseEntity<?> addArticle(@RequestBody @Valid ArticleCreateDTO dto,
                                        BindingResult result) throws URISyntaxException {
        if (result.hasFieldErrors())
            return badRequest().body(result.getFieldError());

        Article article = saveArticleDTO(dto);
        delCateNumCacheByName(article.getCategory());

        return ResponseEntity
                .created(new URI("/article/" + article.getId()))
                .lastModified(System.currentTimeMillis()).build();
    }

    private UpdateResult modifyDeleted(Query unDeletedIdQuery, int state) {
        return mongoTemplate.update(Article.class)
                .matching(unDeletedIdQuery)
                .apply(new Update().set("state", state)).all();
    }

    @Secured(R_ADMIN)
    @PostMapping(path = DELETE_URL + IMAGE_URL + NAME_PATH_VAR)
    public ResponseEntity<?> delImageOfArticle(@PathVariable String name) {
        FileSystemResource resource = new FileSystemResource(propertiesBean.getArticleImages());
        File file = new File(resource.getFile(), name);
        boolean isSuccess = file.delete();
        return (isSuccess ? ResponseEntity.ok() : ResponseEntity.status(500)).build();
    }

    @Secured(R_ADMIN)
    @GetMapping(path = ARTICLE_URL + IMAGE_URL + ID_PATH_VAR)
    public ResponseEntity<?> getImagesOfCertainArticle(@PathVariable String id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(ArticleNotFoundException::new);

        return ResponseEntity.ok(article.getImagePaths());
    }

    @Secured(R_ADMIN)
    @PostMapping(value = ARTICLE_URL + SLASH + UPLOAD_PIC)
    public ResponseEntity<?> uploadArticleImage(@RequestParam(name = "picture") MultipartFile pic) {
        if (pic == null)
            return ResponseEntity.badRequest().body("empty file");

        FileSystemResource resource = new FileSystemResource(propertiesBean.getArticleImages());
        try {
            File temp = File.createTempFile("pic",
                    "." + getFilenameExtension(pic.getOriginalFilename()),
                    resource.getFile());

            pic.transferTo(temp);
            return ResponseEntity.ok(temp.getName());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("");
        }
    }

    @Secured(R_ADMIN)
    @PostMapping(path = DELETE_URL + COMMENT_URL)
    public ModelAndView delComments(@Valid DelCommDTO dto, BindingResult result,
                                    ModelAndView mav) {
        String viewName = REDIRECT + ARTICLE_URL + SLASH + dto.getArticleId(),
                errMsg = "cannot delete comment";

        if (result.hasFieldErrors()) {
            String error = result.getFieldError().getField();
            return setErrorMav("error in " + (error),
                    mav, viewName);
        } else if (!articleRepository.existsById(dto.getArticleId())) {
            return setErrorMav("no article", mav, viewName);
        } else {
            UpdateResult updateResult = mongoTemplate.update(Article.class)
                    .matching(getIdQuery(dto.getArticleId()))
                    .apply(new Update().pull("comments", singletonMap("content", dto.getContent())))
                    .first();
            return oneModify(mav, viewName, errMsg, updateResult);
        }
    }

    @Secured(R_ADMIN)
    @GetMapping(path = RECYCLE_URL)
    public ModelAndView getRecycleBin(@RequestParam(defaultValue = "0") Integer page,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      ModelAndView mav) {
        findByState(page, pageSize, mav, ArticleState.RECYCLED.getState(), M_RECYCLED, M_RECYCLED_URL);
        return mav;
    }

    @Secured(R_ADMIN)
    @GetMapping(path = DRAFT_URL)
    public ModelAndView getDraft(@RequestParam(defaultValue = "0") Integer page,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 ModelAndView mav) {
        findByState(page, pageSize, mav, ArticleState.DRAFT.getState(), M_DRAFT, M_DRAFT_URL);
        return mav;
    }

//    @Secured(R_ADMIN)
//    @PostMapping(path = DELETE_URL + HISTORY_RECORD_URL)
//    public String delHistory(@RequestParam String articleId,
//                             @RequestParam String historyId,
//                             RedirectAttributes model) {
//        UpdateResult updateResult = mongoTemplate.update(Article.class)
//                .matching(getIdQuery(articleId))
//                .apply(new Update().pull("historyRecords", singletonMap("recordId", historyId)))
//                .first();
//        if (updateResult.getModifiedCount() != 1)
//            model.addFlashAttribute("error", "cannot delete history");
//        return REDIRECT + EDIT_URL + ARTICLE_URL + SLASH + articleId;
//    }

    @Secured(R_ADMIN)
    @PostMapping(path = DELETE_URL + HISTORY_RECORD_URL)
    public ResponseEntity<?> delHistory(@RequestBody @Valid ArticleHistoryIdDTO dto,
                                        BindingResult result) {
        if (result.hasFieldErrors())
            return ResponseEntity.badRequest().body(result.getFieldError());
        UpdateResult updateResult = mongoTemplate.update(Article.class)
                .matching(getIdQuery(dto.getArticleId()))
                .apply(new Update().pull("historyRecords", singletonMap("recordId", dto.getHistoryId())))
                .first();
        if (updateResult.getModifiedCount() != 1)
            ResponseEntity.badRequest().body("cannot modify");
        return ResponseEntity.ok().build();
    }

    @Secured(R_ADMIN)
    @PostMapping(path = "/view" + HISTORY_RECORD_URL)
    public ResponseEntity<?> viewHistory(@RequestParam String historyId) {
        HistoryRecordsPrj prj = articleRepository.getHistoryRecords(historyId);
        ArticleHistoryRecord history = prj.getHistoryRecords().stream()
                .findFirst().orElseThrow(ArticleNotFoundException::new);
        return ok(history.getSnapshotContent());
    }

    @Secured(R_ADMIN)
    @PostMapping(path = RECOVER_URL + HISTORY_RECORD_URL)
    public ResponseEntity<?> recoverToHistory(@RequestParam String historyId) {
        HistoryRecordsPrj prj = articleRepository.getHistoryRecords(historyId);
        ArticleHistoryRecord history = prj.getHistoryRecords().stream()
                .findFirst().orElseThrow(ArticleNotFoundException::new);
        return ResponseEntity.ok(history);
    }

    @Secured(R_ADMIN)
    @PostMapping(path = ADD_URL + HISTORY_RECORD_URL,
            consumes = {APPLICATION_JSON_UTF8_VALUE, APPLICATION_JSON_VALUE})
    public ResponseEntity<?> saveHistory(@RequestBody @Valid HistoryCreateDTO dto, BindingResult result) {
        if (result.hasFieldErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldError());
        } else {
            saveHistoryAndReturnArticleId(dto);
            return ResponseEntity.ok(getHistoryShowDTO(dto));
        }
    }

    private String saveHistoryAndReturnArticleId(@Valid HistoryCreateDTO dto) {
        dto.setRecordId(ObjectId.get().toString());
        String articleId;
        Query idQuery = getIdQuery(dto.getArticleId());
        if (hasText(dto.getArticleId())) {
            UpdateResult changeContent = mongoTemplate.update(Article.class)
                    .matching(idQuery)
                    .apply(new Update().set("content", dto.getSnapshotContent())
                            .set("imagePaths", dto.getImagePaths())
                            .set("title", dto.getTitle())).first();
            Assert.isTrue(changeContent.getMatchedCount() == 1, "MUST MATCH ONE");
            UpdateResult changeHistory = mongoTemplate.update(Article.class)
                    .matching(idQuery)
                    .apply(new Update().push("historyRecords", dto.toRecord())).first();
            articleId = dto.getArticleId();
        } else {
            Article article = articleRepository.save(dto.toArticle());
            delCateNumCacheByName(article.getCategory());
            articleId = article.getId();
        }
        return articleId;
    }

    private void findByState(Integer page, Integer pageSize, ModelAndView mav, int state, String meta, String metaUrl) {
        MyPageRequest mpr = new MyPageRequest(page, pageSize).invoke();
        PageRequest pageRequest = PageRequest.of(mpr.getPage(), mpr.getPageSize(), DESC, "createTime");
        Page<IdTitleTimeStatePrj> recycledArticles = articleRepository.findAllByState(state, pageRequest);
        int totalNum = articleRepository.countByState(state);
        preProcessToArticleList(mav, mpr.getPage(), mpr.getPageSize(), recycledArticles, totalNum, meta, metaUrl);
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
                .matching(getIdQuery(dto.getArticleId()))
                .apply(new Update().pull("comments", toDelete)).all();

        return updateResult.getModifiedCount() == 1
                ? ok("deleted successfully")
                : ResponseEntity.status(500).body("cannot delete comment");
    }

    private Query getIdQueryWithDeleteState(String id, int state) {
        return query(where("id").is(id).and("state").is(state));
    }

    @Secured(R_ADMIN)
    @CacheInvalid
    @PostMapping(path = DELETE_URL + ARTICLE_URL + ID_PATH_VAR)
    public ResponseEntity<?> deleteArticle(@PathVariable String id) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");

        ResponseEntity<?> responseEntity;
        Article article = articleRepository.findById(id)
                .orElseThrow(ArticleNotFoundException::new);

        delCateNumCacheByName(article.getCategory()); // clear cache of category nums
        if (article.getState() != RECYCLED.getState()) {
            UpdateResult result = mongoTemplate.update(Article.class)
                    .matching(getIdQuery(id))
                    .apply(new Update().set("state", RECYCLED.getState())).first();
            responseEntity = result.getModifiedCount() == 1
                    ? ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/recycle")).build()
                    : badRequest().body("check id you input");
        } else {
            article.getImagePaths().forEach(p -> {
                String filename = p.replace(ARTICLE_IMAGES_URL, "");
                String path = propertiesBean.getArticleImages() + filename;
                File toDelete = new File(path);
                if (!toDelete.delete())
                    throw new BlogException("cannot delete the image");
            });
            articleRepository.delete(article);
            responseEntity = ResponseEntity.ok().build();
        }

        return responseEntity;
    }

    @Secured(R_ADMIN)
    @CacheInvalid
    @PostMapping(path = RECOVER_URL + ARTICLE_URL + ID_PATH_VAR)
    public ResponseEntity<?> recoverArticle(@PathVariable String id) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");
        UpdateResult result =
                modifyDeleted(getIdQueryWithDeleteState(id, RECYCLED.getState()), PUBLISHED.getState());
        delNumCacheById(id); // clear cache of category nums
        try {

            return result.getModifiedCount() == 1
                    ? ResponseEntity.status(HttpStatus.FOUND).location(new URI("/article/" + id)).build()
                    : badRequest().body("check id you input");
        } catch (URISyntaxException e) {
            throw new BlogException();
        }
    }

    @Secured(R_ADMIN)
    @CacheInvalid
    @PostMapping(value = VISIBLE_PUBLISH_URL + ID_PATH_VAR)
    public ModelAndView visiblePublish(@PathVariable String id, ModelAndView mav) {
        UpdateResult updateResult = mongoTemplate.update(Article.class)
                .matching(query(where("id").is(id).and("state")
                        .in(DRAFT.getState(), ArticleState.HIDDEN.getState(), RECYCLED.getState())))
                .apply(new Update().set("state", PUBLISHED.getState()))
                .first();

        delNumCacheById(id); // clear cache of category nums
        String viewName = REDIRECT + ARTICLE_URL + SLASH + id;

        return oneModify(mav, viewName, "无法修改", updateResult);
    }

    private void delNumCacheById(@PathVariable String id) {
        CategoryStatePrj categoryStatePrj = articleRepository
                .getFirstById(id)
                .orElseThrow(ArticleNotFoundException::new);
        delCateNumCacheByName(categoryStatePrj.getCategory());
    }

    @Secured(R_ADMIN)
    @GetMapping(value = HIDDEN_URL)
    public ModelAndView getHiddenArticle(@RequestParam(defaultValue = "0") Integer page,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         ModelAndView mav) {
        findByState(page, pageSize, mav, ArticleState.HIDDEN.getState(), M_HIDDEN, M_HIDDEN_URL);
        return mav;
    }

    @Secured(R_ADMIN)
    @CacheInvalid
    @PostMapping(value = UNVISITABLE_PUBLISH_URL + ID_PATH_VAR)
    public ModelAndView unVisiblePublish(@PathVariable String id, ModelAndView mav) {
        UpdateResult updateResult = mongoTemplate.update(Article.class)
                .matching(query(where("id").is(id).and("state")
                        .in(DRAFT.getState(), RECYCLED.getState(), PUBLISHED.getState())))
                .apply(new Update().set("state", ArticleState.HIDDEN.getState())).first();
        delNumCacheById(id); // clear cache of category nums
        String viewName = REDIRECT + ARTICLE_URL + SLASH + id;

        return oneModify(mav, viewName, "无法修改", updateResult);
    }

    /**
     * 这个函数的作用是通过分类名来得到分类并且将该分类下的所有
     * 文章的状态都根据URL进行修改，这里要注意的是有全部删除和
     * 恢复以及隐藏的选项，但是没有全部变成草稿的选项
     *
     * @param name 分类名
     * @param operate  操作名
     * @return ResponseEntity.ok(修改条目数)
     */
    @Secured(R_ADMIN)
    @CacheInvalid
    @PostMapping(value = CATEGORY_URL + "/{operate}" + NAME_PATH_VAR)
    public ResponseEntity<?> modifyStateByCategory(@PathVariable String name, // name of category
                                                   @PathVariable String operate) {
        int state;
        switch (operate) {
            case DELETE:
                state = ArticleState.RECYCLED.getState();
                break;
            case ControllerConstant.HIDDEN:
                state = ArticleState.HIDDEN.getState();
                break;
            case RECOVER:
                state = ArticleState.PUBLISHED.getState();
                break;
            default:
                return ResponseEntity.badRequest().body("error type");
        }
        if (state == ArticleState.RECYCLED.getState()
                && articleRepository.countByCategory(name) == 0) {
            categoryRepository.deleteByName(name);
        }
        UpdateResult result = mongoTemplate.update(Article.class)
                .matching(query(Criteria.where("category").is(name)))
                .apply(new Update().set("state", state))
                .all();

        delCateNumCacheByName(name);
        return ok(result.getModifiedCount());
    }

    @Secured(R_ADMIN)
    @CacheInvalid
    @PostMapping(value = ADD_URL + DRAFT_URL)
    public ResponseEntity<?> addDraft(@Valid Article article, BindingResult result) {
        if (result.hasFieldErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldError().getField());
        } else if (!article.isDraftValid()) {
            return ResponseEntity.badRequest().body("草稿内容不合法");
        } else {
            if (hasText(article.getId())) {
                UpdateResult updateResult = mongoTemplate.update(Article.class)
                        .matching(getIdQuery(article.getId()))
                        .apply(new Update().set("state", DRAFT.getState())
                                .set("content", article.getContent())
                                .set("commentEnable", article.getCommentEnable())
                                .set("title", article.getTitle())
                                .set("imagePaths", article.getImagePaths())
                                .set("category", article.getCategory())).first();
                if (updateResult.getModifiedCount() != 1L)
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .location(URI.create(EDIT_URL + ARTICLE_URL + SLASH + article.getId()))
                            .body(Collections.singletonMap("error", "no changing"));
            } else {
                article.setId(null);
                article = mongoTemplate.save(article, Article.COLLECTION_NAME);
            }
            Assert.isTrue(hasText(article.getId()), "保存后id必定有值");
            delCateNumCacheByName(article.getCategory());
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(EDIT_URL + ARTICLE_URL + SLASH + article.getId()))
                    .build();
        }
    }

    @Secured(R_ADMIN)
    @GetMapping(value = ARTICLE_URL + STATE_URL + ID_PATH_VAR)
    public ResponseEntity<?> getStateOfCertainArticle(@PathVariable String id) {
        Integer state = articleRepository.findById(id)
                .orElseThrow(ArithmeticException::new)
                .getState();
        return ResponseEntity.ok(state);
    }

    @Secured(R_ADMIN)
    @CacheInvalid
    @PostMapping(value = HIDDEN_URL + CATEGORY + NAME_PATH_VAR)
    public ResponseEntity<?> hideArticlesOfCategory(@PathVariable String cateName) {
        UpdateResult result = mongoTemplate.update(Article.class)
                .matching(query(Criteria.where("category").is(cateName)))
                .apply(new Update().set("state", ArticleState.HIDDEN))
                .all();
        delCateNumCacheByName(cateName);
        return ok(result.getModifiedCount());
    }

    @Secured(R_ADMIN)
    @CacheInvalid
    @PostMapping(value = ADD_URL + ARTICLE_URL)
    public ModelAndView addArticle(@Valid ArticleCreateDTO dto, BindingResult result,
                                   ModelAndView mav, RedirectAttributes model) {
        if (result.hasFieldErrors()) {
            mav = setErrorMav(result.getFieldError().getField(), mav, EDIT);
            mav.addObject("dto", dto);
            return mav;
        }
        Article article = saveArticleDTO(dto);
        delCateNumCacheByName(article.getCategory());
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
    @GetMapping(value = EDIT_URL + ARTICLE_URL + ID_PATH_VAR)
    public String getEditPage(Model model, @PathVariable String id) {
        compareForRedirect(model, id);
        return EDIT;
    }

    /* 这里其实是一个修改点啊，因为一个事情由thymeleaf和js两个东西做*/
    @Secured(R_ADMIN)
    @GetMapping(value = EDIT_URL + ARTICLE_URL, params = {"id"})
    public String getEditPage(@RequestParam String id, Model model) {
        compareForRedirect(model, id);
        return EDIT;
    }

    private void compareForRedirect(Model model, String id) {
        Article dto = articleRepository.findById(id)
                .orElseThrow(ArticleNotFoundException::new);
        model.addAttribute("dto", dto);
        model.addAttribute("modify", id);
        model.addAttribute("categories", categoryRepository.findAll());
    }


    @Secured(R_ADMIN)
    @CacheInvalid
    @PostMapping(value = MODIFY_URL + ARTICLE_URL + ID_PATH_VAR)
    public ModelAndView modifyArticle(@PathVariable String id, @Valid ArticleCreateDTO dto,
                                      BindingResult result, ModelAndView mav) {
        if (result.hasFieldErrors()) {
            setErrorMav(result.getFieldError().getField(), mav, EDIT);
            // 如果修改成功，就直接重定向去其他页面，如果修改失败，就还要把修改信息添加返回回去
            mav.addObject("dto", dto);
            mav.addObject("modify", id);
            mav.addObject("categories", categoryRepository.findAll());
        } else {
            CategoryStatePrj categoryStatePrj = articleRepository
                    .getFirstById(id)
                    .orElseThrow(ArticleNotFoundException::new);
            UpdateResult updateResult = mongoTemplate.update(Article.class)
                    .matching(getIdQuery(id))
                    .apply(new Update()
                            .set("content", dto.getContent())
                            .set("title", dto.getTitle())
                            .set("state", dto.getState())
                            .set("commentEnable", dto.getCommentEnable())
                            .set("imagePaths", dto.getImagePaths())
                            .set("category", dto.getCategory())).first();

            delCateNumCacheByName(dto.getCategory());
            delCateNumCacheByName(categoryStatePrj.getCategory());
            if (updateResult.getModifiedCount() == 0) {
                mav.addObject("dto", dto);
                mav.addObject("modify", id);
                mav.addObject("error", "cannot modify this article");
                mav.addObject("categories", categoryRepository.findAll());
                mav.setViewName(REDIRECT + ARTICLE_URL + SLASH + id);
            } else {
                mav.setViewName(REDIRECT + ARTICLE_URL + SLASH + id);
            }
        }
        return mav;
    }

    private void delCateNumCacheByName(String category) {
        cache.hdel(ARTICLE_NUMS_OF_CATE + true, category);
        cache.hdel(ARTICLE_NUMS_OF_CATE + false, category);
    }

    /**
     * 这个函数用来修改Category的信息，在使用时，如果前端没有选择文件，则
     * 在这里不会对现有的图片做出任何改动，如果前端选择了文件，这里会删除
     * 现有的文件并将它替换成前端上传的文件
     *
     * @param category Category对象
     * @param result   绑定结果
     * @param file     图片文件
     * @return 如果上传成功就重定向到/index，否则返回400
     */
    @Secured(R_ADMIN)
    @CacheInvalid
    @PostMapping(value = MODIFY_URL + CATEGORY_URL)
    public ResponseEntity<?> modifyCategory(@Valid Category category, BindingResult result,
                                            @RequestParam(value = "pic") MultipartFile file) {
        if (result.hasFieldErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldError().getField());
        } else {
            Category oldCate = categoryRepository
                    .findByName(category.getName())
                    .orElseThrow(CategoryNotFoundException::new);
            oldCate.copyForModify(category);
            if (file != null && !file.isEmpty()) {
                if (hasText(oldCate.getFilePath()))
                    deleteCategoryImage(oldCate.getFilePath());
                saveCategoryImage(oldCate, file);
            }
            categoryRepository.save(oldCate);
            delCateNumCacheByName(oldCate.getName());
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/index")).body("");
        }
    }

    private void deleteCategoryImage(String filePath) {
        File dir = new File(propertiesBean.getCategoryImages());
        File image = new File(dir.getParentFile(), filePath);
        if (!image.delete()) {
            throw new BlogException("cannot delete category image " + image.getName());
        }
    }

    @Secured(R_ADMIN)
    @PostMapping(value = ADD_URL + CATEGORY_URL)
    public ResponseEntity<?> addCategory(@Valid Category category, BindingResult result,
                                         @RequestParam(value = "pic", required = false) MultipartFile file) {
        if (result.hasFieldErrors()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/index"))
                    .body(result.getFieldError().getField());
        } else {
            if (file != null && !file.isEmpty()) {
                saveCategoryImage(category, file);
            }
        }
        category.postProcess();
        categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/index"))
                .body("");
    }

    private void saveCategoryImage(Category category, @NonNull MultipartFile file) {
        File image = new File("./noPath");
        try {
            File dir = new File(propertiesBean.getCategoryImages());
            if (!dir.exists()) dir.mkdirs();
            image = File.createTempFile("categoryPic",
                    "." + getFilenameExtension(file.getOriginalFilename()), dir);
            try (InputStream in = file.getInputStream();
                 OutputStream out = new FileOutputStream(image)) {
                copy(in, out);
                String[] splits = propertiesBean.getCategoryImages().split("/");

                category.setFilePath("/" + splits[splits.length - 1] + "/" + image.getName());
            } catch (IOException e) {
                throw new BlogException("cannot save image at " + e.getMessage());
            }
        } catch (IOException e) {
            throw new BlogException("cannot save image at " + image.getPath());
        }
    }

}
