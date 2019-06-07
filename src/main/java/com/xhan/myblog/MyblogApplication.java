package com.xhan.myblog;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;

@SpringBootApplication
public class MyblogApplication {


    public static void main(String[] args) {
        SpringApplication.run(MyblogApplication.class, args);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient client) {
        MongoTemplate template = new MongoTemplate(client, "xhanblog");
        template.setWriteConcern(WriteConcern.MAJORITY);
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        return template;
    }

}
