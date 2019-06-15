package com.xhan.myblog;

import com.xhan.myblog.utils.BlogUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MyblogApplicationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void contextLoads() {
        String s = "<p>这是一个假图</p>";
        System.out.println(s);
        System.out.println("------------------------------------------------");
        System.out.println(BlogUtils.delHtmlTag(s));
    }

//    @Test
//    public void testRoot() {
//        FileSystemResource resource = new FileSystemResource("images/");
//        System.out.println("path : " + resource.getFile().getAbsolutePath());
//        System.out.println("name : " + resource.getFile().getAbsoluteFile().getName());
//    }

//    @Test
//    public void testAggregation() {
//        String currentDate = BlogUtils.getCurrentDateString();
//        System.out.println("currentDate : " + currentDate);
//        TypedAggregation<MongoLog> aggregation = Aggregation.newAggregation(
//                MongoLog.class,
//                Aggregation.match(Criteria.where("date").is(currentDate)),
//                Aggregation.group("requestURI").count().as("count"),
//                Aggregation.sort(Sort.Direction.DESC, "count")
//        );
//        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation,
//                MongoLog.COLLECTION_NAME, Document.class);
//        results.getMappedResults().forEach(System.out::println);
//    }
//
//    @Data
//    private class URICount {
//        String requestURI;
//        int count;
//    }

}
