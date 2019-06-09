package com.xhan.myblog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xhan.myblog.model.content.Article;
import com.xhan.myblog.model.content.Comment;
import com.xhan.myblog.model.content.CommentCreateDTO;
import com.xhan.myblog.model.content.DelCommDTO;
import com.xhan.myblog.model.user.Admin;
import com.xhan.myblog.model.user.Guest;
import com.xhan.myblog.repository.ArticleRepository;
import com.xhan.myblog.utils.BlogUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static com.xhan.myblog.controller.ControllerConstant.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ContentControllerTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ArticleRepository articleRepository;
    private Article fullArticle;
    private Article noTitle;
    private Article noContent;
    private Article savedArticle;
    private Article deletedArticle;
    private Comment mockComment;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        fullArticle = new Article();
        fullArticle.setTitle("mock title");
        fullArticle.setContent("mock content");
        noTitle = new Article();
        noTitle.setContent("mock content");
        noContent = new Article();
        noContent.setTitle("mock title");
        savedArticle = new Article();
        savedArticle.setTitle("saved title");
        savedArticle.setContent("saved content");
        savedArticle.setPublished(false);
        Comment savedComment = new Comment();
        savedComment.setCreator("mock creator");
        savedComment.setContent("mock comment");
        savedArticle.getComments().add(savedComment);
        savedArticle = articleRepository.save(savedArticle);
        deletedArticle = new Article();
        deletedArticle.setContent("published article");
        deletedArticle.setTitle("published article");
        deletedArticle.setPublished(true);
        deletedArticle = articleRepository.save(deletedArticle);
        mockComment = new Comment();
        mockComment.setCreator("mock Creator");
        mockComment.setContent("mock Comment");
    }

    @After
    public void tear() {
        articleRepository.deleteAll();
    }

    @Test
    public void contextLoader() {

    }

//    @Test
    public void insertAdmin() {
        Admin admin = new Admin();
        admin.setAccount("xhanjiao94217");
        admin.setNickName("小韩");
        admin.setPassword("{NOOP}niezhidongwu94");
        admin.setCreateTime(BlogUtils.getCurrentTime());
        admin.setLastLoginTime(BlogUtils.getCurrentTime());
        mongoTemplate.save(admin, Guest.COLLECTION_NAME);
    }

    @Test
    public void getCertainArticle() throws Exception {
        String id = savedArticle.getId();
        String savedArticleJson = objectMapper.writeValueAsString(savedArticle);
        System.out.println("savedArticleJson : " + savedArticleJson);

        mockMvc.perform(get(ARTICLE_URL + SLASH + id)
                .accept(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().is(200))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    @Test
    public void getArticles() throws Exception {
        MvcResult result = mockMvc.perform(get(ARTICLE_URL))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    public void addFullArticleJson() throws Exception {
        String articleJson = objectMapper.writeValueAsString(fullArticle);
        System.out.println("articleJson : " + articleJson);

        mockMvc.perform(post(ARTICLE_URL + ADD_URL)
                .contentType(APPLICATION_JSON)
                .content(articleJson.getBytes()))
                .andExpect(status().is(201))
                .andExpect(header().exists(HttpHeaders.LAST_MODIFIED))
                .andExpect(header().exists(HttpHeaders.LOCATION));
    }

    @Test
    public void addFullArticleForm() throws Exception {
        String articleJson = objectMapper.writeValueAsString(fullArticle);
        System.out.println("articleJson : " + articleJson);

        mockMvc.perform(post(ARTICLE_URL + ADD_URL)
//                .contentType(APPLICATION_FORM_URLENCODED_VALUE)
                .param("title", "mock form title")
                .param("content", "mock form content"))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/index"));
    }
    @Test
    public void addUnFullArticleJson() throws Exception {
        String noContentJson = objectMapper.writeValueAsString(noContent);
        String noTitleJson = objectMapper.writeValueAsString(noTitle);

        mockMvc.perform(post(ARTICLE_URL + ADD_URL)
                .contentType(APPLICATION_JSON)
                .content(noContentJson.getBytes()))
                .andExpect(status().is(400));

        mockMvc.perform(post(ARTICLE_URL + ADD_URL)
                .contentType(APPLICATION_JSON)
                .content(noTitleJson.getBytes()))
                .andExpect(status().is(400));
    }

    @Test
    public void addUnFullArticleForm() throws Exception {
        mockMvc.perform(post(ARTICLE_URL + ADD_URL)
                .param("title", "")
                .param("content", ""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }



    @Test
    public void deleteArticle() throws Exception {
        Assert.assertFalse(savedArticle.getPublished());

        mockMvc.perform(post(ARTICLE_URL + DELETE_URL + SLASH + savedArticle.getId()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("modified".getBytes()));
    }

    @Test
    public void recoverArticle() throws Exception {
        Assert.assertTrue(deletedArticle.getPublished());

        mockMvc.perform(post(ARTICLE_URL + RECOVER_URL + SLASH + deletedArticle.getId()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("modified".getBytes()));
    }

    @Test
    public void addCommentJson() throws Exception {
        Assert.assertTrue(!savedArticle.getComments().contains(mockComment));

        CommentCreateDTO dto = new CommentCreateDTO(mockComment);
        dto.setArticleId(savedArticle.getId());
        dto.setEmail("mockEmail@213.com");
        String dtoJson = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post(ADD_COMMENTS)
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(dtoJson.getBytes())
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isCreated());
    }

    @Test
    public void addCommentForm() throws Exception {
        Assert.assertTrue(!savedArticle.getComments().contains(mockComment));

        CommentCreateDTO dto = new CommentCreateDTO(mockComment);
        dto.setArticleId(savedArticle.getId());
        dto.setEmail("mockEmail@213.com");

        mockMvc.perform(post(ADD_COMMENTS)
                .param("email", dto.getEmail())
                .param("articleId", dto.getArticleId())
                .param("creator", dto.getCreator())
                .param("content", dto.getContent()))
                .andExpect(status().isFound());
    }

    @Test
    public void delComment() throws Exception {
        Assert.assertTrue(savedArticle.getComments().size() > 0);
        DelCommDTO dto = new DelCommDTO();
        dto.setArticleId(savedArticle.getId());
        dto.setContent(savedArticle.getComments().get(0).getContent());

        String dtoJson = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post(DEL_COMMENTS)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(dtoJson.getBytes()))
                .andExpect(status().isOk());
    }
}