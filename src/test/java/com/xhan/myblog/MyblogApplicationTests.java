package com.xhan.myblog;

import com.xhan.myblog.utils.BlogUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@SpringBootTest
@RunWith(SpringRunner.class)
public class MyblogApplicationTests {

    @Autowired
    private WebApplicationContext wac;

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
    public void testCounter() {
        ExecutorService es = Executors.newCachedThreadPool();
        IntStream.range(0, 20)
                .forEach(i -> es.submit(() -> {
                    try {
                        mvc.perform(get("/"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
        es.submit(() -> {
                    try {
                        mvc.perform(get("/"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

}
