package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.exceptions.content.ArticleNotFoundException;
import com.xhan.myblog.exceptions.content.BlogException;
import com.xhan.myblog.exceptions.content.CategoryNotFoundException;
import com.xhan.myblog.exceptions.content.CommentNotFoundException;
import com.xhan.myblog.model.content.dto.ArticleCreateDTO;
import com.xhan.myblog.model.content.dto.ContentTitleIdDTO;
import com.xhan.myblog.model.content.dto.DelCommDTO;
import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.model.content.repo.ArticleState;
import com.xhan.myblog.model.content.repo.Category;
import com.xhan.myblog.model.content.repo.Comment;
import com.xhan.myblog.model.user.Admin;
import com.xhan.myblog.model.user.Guest;
import com.xhan.myblog.model.user.ModifyDTO;
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

import static com.xhan.myblog.controller.ControllerConstant.*;
import static com.xhan.myblog.model.content.repo.ArticleState.*;
import static java.util.Collections.singletonMap;
import static org.apache.tomcat.util.http.fileupload.IOUtils.copy;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
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

    @RequestMapping(value = {LOGIN_DISPATCH_URL})
    public String Login(HttpSession session, RedirectAttributes model) {
        model.addFlashAttribute(IS_ADMIN, true);
        session.setAttribute(IS_ADMIN, true);
        return REDIRECT + INDEX;
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
        File temp;
        try {
            temp = File.createTempFile("pic",
                    "." + getFilenameExtension(pic.getOriginalFilename()),
                    resource.getFile());

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("不能创建临时文件");
        }
        try {
            pic.transferTo(temp);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("不能存储");
        }

        return ResponseEntity.ok(temp.getName());
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
                    .matching(query(where("id").is(dto.getArticleId())))
                    .apply(new Update().pull("comments", singletonMap("content", dto.getContent())))
                    .first();
            return oneModify(mav, viewName, errMsg, updateResult);
        }
    }

    @Secured(R_ADMIN)
    @GetMapping(path = RECYCLE_URL)
    public ModelAndView getRecycleBin(@RequestParam(defaultValue = "0") Integer page,
                                      @RequestParam(defaultValue = "5") Integer pageSize,
                                      ModelAndView mav) {
        findByState(page, pageSize, mav, ArticleState.RECYCLED.getState(), M_RECYCLED, M_RECYCLED_URL);
        return mav;
    }

    @Secured(R_ADMIN)
    @GetMapping(path = DRAFT_URL)
    public ModelAndView getDraft(@RequestParam(defaultValue = "0") Integer page,
                                 @RequestParam(defaultValue = "5") Integer pageSize,
                                 ModelAndView mav) {
        findByState(page, pageSize, mav, ArticleState.DRAFT.getState(), M_DRAFT, M_DRAFT_URL);
        return mav;
    }

    private void findByState(Integer page, Integer pageSize, ModelAndView mav, int state, String meta, String metaUrl) {
        MyPageRequest mpr = new MyPageRequest(page, pageSize).invoke();
        PageRequest pageRequest = PageRequest.of(mpr.getPage(), mpr.getPageSize(), DESC, "createTime");
        Page<Article> recycledArticles = articleRepository.findAllByState(state, pageRequest);
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
                .matching(query(where("id").is(dto.getArticleId())))
                .apply(new Update().pull("comments", toDelete)).all();

        return updateResult.getModifiedCount() == 1
                ? ok("deleted successfully")
                : ResponseEntity.status(500).body("cannot delete comment");
    }

    private Query getIdQueryWithDeleteState(String id, int state) {
        return query(where("id").is(id).and("state").is(state));
    }

    @Secured(R_ADMIN)
    @PostMapping(path = DELETE_URL + ARTICLE_URL + ID_PATH_VAR)
    public ResponseEntity<?> deleteArticle(@PathVariable String id) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");

        ResponseEntity<?> responseEntity;
        Article article = articleRepository.findById(id)
                .orElseThrow(ArticleNotFoundException::new);
        try {
            if (article.getState() != RECYCLED.getState()) {
                UpdateResult result = mongoTemplate.update(Article.class)
                        .matching(Query.query(Criteria.where("id").is(id)))
                        .apply(new Update().set("state", RECYCLED.getState())).first();
                responseEntity = result.getModifiedCount() == 1
                        ? ResponseEntity.status(HttpStatus.FOUND).location(new URI("/recycle")).build()
                        : badRequest().body("check id you input");
            } else {
                article.getImagePaths().forEach(p -> {
                    String filename = p.replace(ARTICLE_IMAGES_URL, "");
                    String path = propertiesBean.getArticleImages() + filename;
                    File toDelete = new File(path);
                    while (!toDelete.delete()) {
                    }
                });
                articleRepository.delete(article);
                responseEntity = ResponseEntity.ok().build();
            }
        } catch (URISyntaxException e) {
            throw new BlogException();
        }
        return responseEntity;
    }

    @Secured(R_ADMIN)
    @PostMapping(path = RECOVER_URL + ARTICLE_URL + ID_PATH_VAR)
    public ResponseEntity<?> recoverArticle(@PathVariable String id) {
        if (!hasText(id))
            return badRequest().body("id cannot be null");

        UpdateResult result =
                modifyDeleted(getIdQueryWithDeleteState(id, RECYCLED.getState()), PUBLISHED.getState());

        try {
            return result.getModifiedCount() == 1
                    ? ResponseEntity.status(HttpStatus.FOUND).location(new URI("/article/" + id)).build()
                    : badRequest().body("check id you input");
        } catch (URISyntaxException e) {
            throw new BlogException();
        }
    }

    @Secured(R_ADMIN)
    @PostMapping(value = VISIABLE_PUBLISH_URL + ID_PATH_VAR)
    public ModelAndView visiablePublish(@PathVariable String id, ModelAndView mav) {
        UpdateResult updateResult = mongoTemplate.update(Article.class)
                .matching(query(where("id").is(id).and("state")
                        .in(DRAFT.getState(), ArticleState.HIDDEN.getState(), RECYCLED.getState())))
                .apply(new Update().set("state", PUBLISHED.getState()))
                .first();

        String viewName = REDIRECT + ARTICLE_URL + SLASH + id;
        return oneModify(mav, viewName, "无法修改", updateResult);
    }

    @Secured(R_ADMIN)
    @GetMapping(value = HIDDEN_URL)
    public ModelAndView getHiddenArticle(@RequestParam(defaultValue = "0") Integer page,
                                         @RequestParam(defaultValue = "5") Integer pageSize,
                                         ModelAndView mav) {
        findByState(page, pageSize, mav, ArticleState.HIDDEN.getState(), M_HIDDEN, M_HIDDEN_URL);
        return mav;
    }

    @Secured(R_ADMIN)
    @PostMapping(value = UNVISIABLE_PUBLISH_URL + ID_PATH_VAR)
    public ModelAndView unVisiablePublish(@PathVariable String id, ModelAndView mav) {
        UpdateResult updateResult = mongoTemplate.update(Article.class)
                .matching(query(where("id").is(id).and("state")
                        .in(DRAFT.getState(), RECYCLED.getState(), PUBLISHED.getState())))
                .apply(new Update().set("state", ArticleState.HIDDEN.getState()))
                .first();

        String viewName = REDIRECT + ARTICLE_URL + SLASH + id;
        return oneModify(mav, viewName, "无法修改", updateResult);
    }

    /**
     * 这个函数的作用是通过分类名来得到分类并且将该分类下的所有
     * 文章的状态都根据URL进行修改，这里要注意的是有全部删除和
     * 恢复以及隐藏的选项，但是没有全部变成草稿的选项
     *
     * @param name 分类名
     * @return ResponseEntity.ok(修改条目数)
     */
    @Secured(R_ADMIN)
    @PostMapping(value = CATEGORY_URL + "/{operate}" + NAME_PATH_VAR)
    public ResponseEntity<?> modifyStateByCategory(@PathVariable String name,
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
        return ok(result.getModifiedCount());
    }

    @Secured(R_ADMIN)
    @PostMapping(value = ADD_URL + DRAFT_URL)
    public ResponseEntity<?> saveDraft(@Valid Article article, BindingResult result) {
        if (result.hasFieldErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldError().getField());
        } else if (!article.isDraftValid()) {
            return ResponseEntity.badRequest().body("草稿内容不合法");
        } else {
            article.setId(article.getId().equals("") ? null : article.getId());
            article = mongoTemplate.save(article, Article.COLLECTION_NAME);
            Assert.isTrue(hasText(article.getId()), "保存后id必定有值");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(SLASH + EDIT + ARTICLE_URL + SLASH + article.getId()))
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
    @PostMapping(value = HIDDEN_URL + CATEGORY + NAME_PATH_VAR)
    public ResponseEntity<?> hideArticlesOfCategory(@PathVariable String name) {
        UpdateResult result = mongoTemplate.update(Article.class)
                .matching(query(Criteria.where("category").is(name)))
                .apply(new Update().set("state", ArticleState.HIDDEN))
                .all();
        return ok(result.getModifiedCount());
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
                            .set("state", dto.getState())
                            .set("commentEnable", dto.getCommentEnable())
                            .set("imagePaths", dto.getImagePaths())
                            .set("category", dto.getCategory())).first();

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

    @Secured(R_ADMIN)
    @PostMapping(value = MODI_URL + CATEGORY_URL)
    public ResponseEntity<?> modifyCategory(@Valid Category category, BindingResult result,
                                            @RequestParam(value = "pic", required = false) MultipartFile file) {
        if (result.hasFieldErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldError().getField());
        } else {
            Category oldCate = categoryRepository
                    .findByName(category.getName())
                    .orElseThrow(CategoryNotFoundException::new);

            oldCate.setName(category.getName());
            oldCate.setDescription(category.getDescription());
            if (file != null) {
                if (hasText(oldCate.getFilePath()))
                    deleteCategoryImage(oldCate.getFilePath());
                saveCategoryImage(oldCate, file);
            }
            categoryRepository.save(oldCate);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/index"))
                    .body("");
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
    public ModelAndView addCategory(@Valid Category category, BindingResult result,
                                    @RequestParam(value = "pic", required = false) MultipartFile file, ModelAndView mav) {
        if (result.hasFieldErrors()) {
            setErrorMav(result.getFieldError().getField(), mav, EDIT);
        } else {
            mav.setViewName(REDIRECT + SLASH + CATEGORY);
            if (file != null && !file.isEmpty()) {
                saveCategoryImage(category, file);
            }
        }
        category.postProcess();
        categoryRepository.save(category);
        return mav;
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
