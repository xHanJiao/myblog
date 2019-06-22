package com.xhan.myblog;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.xhan.myblog.listener.ApplicationStartupListener;
import com.xhan.myblog.model.user.Admin;
import com.xhan.myblog.model.user.Guest;
import com.xhan.myblog.utils.BlogUtils;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableConfigurationProperties
public class MyblogApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(MyblogApplication.class);
        springApplication.run(args);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient client) {
        MongoTemplate template = new MongoTemplate(client, "xhanblog");
        template.setWriteConcern(WriteConcern.JOURNALED);
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        return template;
    }

    @Bean(name = "myPasswordEncoder")
    public PasswordEncoder passwordEncoder() {
        String idForEncode = "BCRYPT" ;
        Map<String, PasswordEncoder> encoders = new HashMap<String, PasswordEncoder>() {{
            put(idForEncode, new BCryptPasswordEncoder());
            put(MyNoOpPasswordEncoder.idForNoOp, new MyNoOpPasswordEncoder());
        }};
        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }

    private final class MyNoOpPasswordEncoder implements PasswordEncoder {

        private static final String idForNoOp = "NOOP";

        @Override
        public String encode(CharSequence rawPassword) {
            return rawPassword.toString();
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return rawPassword.toString().equals(encodedPassword);
        }
    }
}
