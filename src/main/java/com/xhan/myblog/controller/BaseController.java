package com.xhan.myblog.controller;

import com.mongodb.client.result.UpdateResult;
import com.xhan.myblog.exceptions.content.ArticleNotFoundException;
import com.xhan.myblog.exceptions.content.BlogException;
import com.xhan.myblog.model.content.dto.CategoryNumDTO;
import com.xhan.myblog.model.content.dto.CommentCreateDTO;
import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.model.content.repo.Comment;
import com.xhan.myblog.model.prj.IdTitleTimeStatePrj;
import com.xhan.myblog.repository.ArticleRepository;
import com.xhan.myblog.repository.CategoryRepository;
import com.xhan.myblog.service.AuthorityHelper;
import com.xhan.myblog.utils.MapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

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

    protected final MapCache cache = MapCache.single();
    @Autowired
    protected MongoTemplate mongoTemplate;
    @Autowired
    protected ArticleRepository articleRepository;
    @Autowired
    protected CategoryRepository categoryRepository;
    @Autowired
    protected ControllerPropertiesBean propertiesBean;
    protected Article emptyArticle = new Article();

    @Autowired
    protected AuthorityHelper authorityHelper;

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

    protected Page<IdTitleTimeStatePrj> getArticlesDueIsAdmin(Integer pageSize, Integer page) {
        return getPagedArticles(page, pageSize, authorityHelper.isAdmin());
    }

    protected Page<IdTitleTimeStatePrj> getPagedArticles(Integer page, Integer pageSize, boolean isAdmin) {
        MyPageRequest mpr = new MyPageRequest(page, pageSize).invoke();
        PageRequest pageRequest = of(mpr.getPage(), mpr.getPageSize(), DESC, "createTime");
        return isAdmin
                ? articleRepository.findAllBy(pageRequest)
                : articleRepository.findAllByState(PUBLISHED.getState(), pageRequest);
    }

    protected Page<IdTitleTimeStatePrj> getPagedArticles(Integer page, Integer pageSize, String cateName, boolean isAdmin) {
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
     * <p>
     * 为了达到尽量复用的目的，在这个项目中，回收站、草稿箱、分类展示、全部
     * 文章展示用的都是同一个视图，并且还是分页的视图，因此必须把关于视图中
     * 显示的文章的元信息发送到前端，这样在翻页的时候才可以找到正确的URL
     *
     * @param mav
     * @param page
     * @param pageSize
     * @param articles
     * @param totalNums
     * @param meta
     * @param metaUrl
     */
    void preProcessToArticleList(ModelAndView mav, Integer page, Integer pageSize,
                                 Page<IdTitleTimeStatePrj> articles, int totalNums, String meta, String metaUrl) {
        Map<String, Object> data = preProcessToArticleList(page, pageSize, articles, totalNums, meta, metaUrl);
        mav.setViewName(ARTICLE_LIST);
        mav.addAllObjects(data);
    }

    long countByIsAdmin(boolean admin) {
        return admin
                ? articleRepository.count()
                : articleRepository.countByState(PUBLISHED.getState());
    }

    long countByCategoryAndIsAdmin(String category, boolean admin) {
        return admin
                ? articleRepository.countByCategory(category)
                : articleRepository.countByCategoryAndState(category, PUBLISHED.getState());
    }


    private Map<String, Object> preProcessToArticleList(Integer page, Integer pageSize,
                                                        Page<IdTitleTimeStatePrj> articles, int totalNums, String meta, String metaUrl) {
        page = isIntValid(page) ? page : 0;
        int maxPage = totalNums % pageSize == 0 ? totalNums / pageSize : totalNums / pageSize + 1;
        List<Integer> pages = IntStream.range(0, maxPage).boxed().map(i -> i + 1).collect(toList());

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
        Article dto = (authorityHelper.isAdmin()
                ? articleRepository.findById(id)
                : articleRepository.findByIdAndState(id, PUBLISHED.getState()))
                .orElseThrow(ArticleNotFoundException::new);
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

    @ExceptionHandler(value = BlogException.class)
    public ResponseEntity<?> handleException(BlogException e) {
        return badRequest().body(e.getMessage());
    }

    protected Query getIdQuery(String id) {
        Assert.notNull(id, "id cannot be null");
        return query(Criteria.where("id").is(id));
    }

    /**
     * 获取所有分类的信息，以及对应的文章数目（管理员可以看到所有状态的文章
     * 普通用户只能看到已发布的文章）
     *
     * @return 如果没有，就返回空列表，不会返回null
     */
    @NonNull
    List<CategoryNumDTO> getCategoryNumDTOS() {
        boolean isAdmin = authorityHelper.isAdmin();
        return categoryRepository.findAll(Sort.by(ASC, "createTime"))
                .stream().map(c -> {
                    Integer num = cache.hget(ARTICLE_NUMS_OF_CATE + isAdmin, c.getName());
                    CategoryNumDTO dto = new CategoryNumDTO(c);
                    if (num == null) {
                        num = isAdmin
                                ? articleRepository.countByCategory(c.getName())
                                : articleRepository.countByCategoryAndState(c.getName(), PUBLISHED.getState());
                        cache.hset(ARTICLE_NUMS_OF_CATE + isAdmin, c.getName(), num);
                    }
                    dto.setNum(num);
                    return dto;
                }).collect(toList());
    }

    class MyPageRequest {
        private Integer page;
        private Integer pageSize;

        MyPageRequest(Integer page, Integer pageSize) {
            this.page = page >= 0 ? page : 0;
            this.pageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        }

        public Integer getPage() {
            return page;
        }

        Integer getPageSize() {
            return pageSize;
        }

        MyPageRequest invoke() {
            page = isIntValid(page) ? page : 0;
            pageSize = isIntValid(pageSize) ? pageSize : 5;
            return this;
        }
    }
}
