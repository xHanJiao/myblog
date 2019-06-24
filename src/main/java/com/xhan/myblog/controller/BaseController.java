package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.exceptions.content.ArticleNotFoundException;
import com.xhan.myblog.exceptions.content.BlogException;
import com.xhan.myblog.model.content.dto.CategoryNumDTO;
import com.xhan.myblog.model.content.dto.CommentCreateDTO;
import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.model.content.repo.Comment;
import com.xhan.myblog.repository.ArticleRepository;
import com.xhan.myblog.repository.CategoryRepository;
import com.xhan.myblog.utils.MapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.xhan.myblog.controller.ControllerConstant.*;
import static com.xhan.myblog.model.content.repo.ArticleState.PUBLISHED;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.http.ResponseEntity.badRequest;

@Controller
public class BaseController {

    @Autowired
    protected MongoTemplate mongoTemplate;
    @Autowired
    protected ArticleRepository articleRepository;
    @Autowired
    protected CategoryRepository categoryRepository;
    @Autowired
    protected MessageSource messageSource;
    protected Article emptyArticle = new Article();

    protected final MapCache cache = MapCache.single();

    protected static final String pageSize = "10";

    public BaseController() {
        emptyArticle.setState(1);
    }

    ModelAndView oneModify(ModelAndView mav, String viewName, String errMsg, UpdateResult updateResult) {
        mav.setViewName(viewName);
        if (updateResult.getModifiedCount() != 1) {
            mav.setStatus(HttpStatus.valueOf(500));
            mav.addObject("error", errMsg);
        }
        return mav;
    }

    protected Page<Article> getArticlesDueIsAdmin(Integer pageSize, Integer page) {
        return getPagedArticles(page, pageSize, isAdmin());
    }

    protected boolean isAdmin() {
        boolean isAdmin = false;
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (GrantedAuthority ga : authorities)
            isAdmin |= ga.getAuthority().equals(R_ADMIN);
        return isAdmin;
    }

    protected Page<Article> getPagedArticles(Integer page, Integer pageSize, boolean isAdmin) {
        MyPageRequest mpr = new MyPageRequest(page, pageSize).invoke();
        PageRequest pageRequest = of(mpr.getPage(), mpr.getPageSize(), DESC, "createTime");
        return isAdmin
                ? articleRepository.findAll(pageRequest)
                : articleRepository.findAllByState(PUBLISHED.getState(), pageRequest);
    }

    Page<Article> getPagedArticles(Integer page, Integer pageSize, String cateName, boolean isAdmin) {
        MyPageRequest mpr = new MyPageRequest(page, pageSize).invoke();
        PageRequest pageRequest = of(mpr.getPage(), mpr.getPageSize(), DESC, "createTime");
        return isAdmin
                ? articleRepository.findAllByCategory(cateName, pageRequest)
                : articleRepository.findAllByStateAndCategory(PUBLISHED.getState(), cateName, pageRequest);
    }


    ModelAndView setErrorMav(String errMsg, ModelAndView mav, String viewName) {
        mav.setStatus(HttpStatus.BAD_REQUEST);
        mav.addObject("error", errMsg);
        mav.setViewName(viewName);
        return mav;
    }

    /**
     * 在去往分类的文章列表之前，进行一些预处理，把文章、当前页数、总页数
     * 列表元信息（回收站？草稿箱？还是只是分类展示？）和对应的URL返回
     *
     * 为了达到尽量复用的目的，在这个项目中，回收站、草稿箱、分类展示、全部
     * 文章展示用的都是同一个视图，并且还是分页的视图，因此必须把关于视图中
     * 显示的文章的元信息发送到前端，这样在翻页的时候才可以找到正确的URL
     * @param mav
     * @param page
     * @param pageSize
     * @param articles
     * @param totalNums
     * @param meta
     * @param metaUrl
     */
    void preProcessToArticleList(ModelAndView mav, Integer page, Integer pageSize,
                                         Page<Article> articles, int totalNums, String meta, String metaUrl) {
        Map<String, Object> data = preProcessToArticleList(page, pageSize, articles, totalNums, meta, metaUrl);
        mav.setViewName(ARTICLE_LIST);
        mav.addAllObjects(data);
    }

    private Map<String, Object> preProcessToArticleList(Integer page, Integer pageSize,
                                                        Page<Article> articles, int totalNums, String meta, String metaUrl) {
        page = isIntValid(page) ? page : 0;
        int maxPage = totalNums % pageSize == 0 ? totalNums / pageSize : totalNums / pageSize + 1;
        List<Integer> pages = IntStream.range(0, maxPage).boxed().map(i -> i+1).collect(toList());

        Map<String, Object> data = new HashMap<>();
        data.put("articles", articles.getContent());
        data.put("currentPage", page + 1);
        data.put("allPages", pages);
        data.put("meta", meta);
        data.put("metaUrl", metaUrl);
        return data;
    }
    private boolean isIntValid(Integer i) {
        return i != null && i >= 0;
    }

    Article getArticleByIdAndModifyVisit(String id) {
        Article dto = getArticleDueIsAdmin(id);
        mongoTemplate.update(Article.class)
                .matching(query(where("id").is(id)))
                .apply(new Update().inc("visitTime", 1)).first();
        return dto;
    }

    UpdateResult saveCommentDTO(CommentCreateDTO dto) {
        dto.preProcessBeforeSave();
        Comment comment = dto.toComment();
        return mongoTemplate.update(Article.class)
                .matching(query(where("id").is(dto.getArticleId())))
                .apply(new Update().push("comments", comment)).all();
    }

    private Article getArticleDueIsAdmin(String id) {
        return (isAdmin()
                ? articleRepository.findById(id)
                : articleRepository.findByStateAndId(PUBLISHED.getState(), id))
                .orElseThrow(ArticleNotFoundException::new);
    }

    @ExceptionHandler(value = BlogException.class)
    public ResponseEntity<?> handleException(BlogException e) {
        return badRequest().body(e.getMessage());
    }

    /**
     * 获取所有分类的信息，以及对应的文章数目（管理员可以看到所有状态的文章
     * 普通用户只能看到已发布的文章）
     * @return 如果没有，就返回空列表，不会返回null
     */
    @NonNull
    List<CategoryNumDTO> getCategoryNumDTOS() {
        boolean isAdmin = isAdmin();
        return categoryRepository.findAll(Sort.by(ASC, "createTime"))
                .stream().map(c -> {
                    Integer num = cache.hget(CATE_NUMS + isAdmin, c.getName());
                    CategoryNumDTO dto = new CategoryNumDTO(c);
                    if (num == null) {
                        num = isAdmin
                                ? articleRepository.countByCategory(c.getName())
                                : articleRepository.countByCategoryAndState(c.getName(), PUBLISHED.getState());
                        cache.hset(CATE_NUMS + isAdmin, c.getName(), num);
                    }
                    dto.setNum(num);
                    return dto;
                }).collect(toList());
    }

    class MyPageRequest {
        private Integer page;
        private Integer pageSize;

        public MyPageRequest(Integer page, Integer pageSize) {
            this.page = page >= 0 ? page : 0;
            this.pageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        }

        public Integer getPage() {
            return page;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public MyPageRequest invoke() {
            page = isIntValid(page) ? page : 0;
            pageSize = isIntValid(pageSize) ? pageSize : 5;
            return this;
        }
    }
}
