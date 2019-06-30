package com.xhan.myblog;

import com.xhan.myblog.model.prj.HistoryRecordsPrj;
import com.xhan.myblog.repository.ArticleRepository;
import com.xhan.myblog.utils.BlogUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@SpringBootTest
@RunWith(SpringRunner.class)
public class MyblogApplicationTests {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ArticleRepository articleRepository;

    private MockMvc mvc;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void contextLoads() {
        String s = "<p>这是一个假图</p>";
        System.out.println(s);
        System.out.println("------------------------------------------------");
        System.out.println(BlogUtils.delHtmlTag(s));
    }

    @Test
    public void testget() {
        HistoryRecordsPrj prj = articleRepository
                .getHistoryRecords("5d1878d256975e071cfb87f7");
        System.out.println(prj);
    }

}
