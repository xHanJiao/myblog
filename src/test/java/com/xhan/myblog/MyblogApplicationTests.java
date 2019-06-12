package com.xhan.myblog;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MyblogApplicationTests {

    @Autowired
    private PasswordEncoder encoder;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testEncoder() {
        String raw = "niezhidongwu94";
        String encoded = encoder.encode(raw);
        System.out.println("raw : " + raw);
        System.out.println("encoded : " + encoded);
        System.out.println(encoder.matches(raw, encoded));
        System.out.println(encoded.equals("{BCRYPT}$2a$10$DvF6yOfbGAJ0YfBzJWKFnuvPYjkRTaqJf0vzVvQKqHsqmqMT4weWy"));
    }

}
