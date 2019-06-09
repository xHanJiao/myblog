package com.xhan.myblog.service;

import com.xhan.myblog.model.user.Admin;
import com.xhan.myblog.model.user.Guest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import static org.springframework.data.mongodb.core.query.Query.query;

@Primary
@Component(value = "userDetailService")
public class MyUserDetailService implements UserDetailsService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MyUserDetailService(MongoTemplate template) {
        this.mongoTemplate = template;
    }

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        Admin admin = mongoTemplate.findOne(query(Criteria.where("account").is(account)),
                Admin.class,
                Guest.COLLECTION_NAME);
        User.UserBuilder builder = User.builder();
        builder.username(account)
                .password(admin.getPassword())
                .roles("ADMIN")
                .disabled(false)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false);
        return builder.build();
    }
}
