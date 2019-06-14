package com.xhan.myblog.listener;

import com.xhan.myblog.model.user.Admin;
import com.xhan.myblog.model.user.Guest;
import com.xhan.myblog.utils.BlogUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;

public class ApplicationStartupListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        MongoTemplate template = contextRefreshedEvent.getApplicationContext().getBean(MongoTemplate.class);

        Assert.notNull(template, "cannot get mongoTemplate in listener");

        Admin admin = new Admin();
        admin.setPassword("{NOOP}niezhidongwu94");
        admin.setAccount("xhanjiao");
        admin.setNickName("小韩");
        admin.setCreateTime(BlogUtils.getCurrentDateTime());
        template.upsert(Query.query(Criteria.byExample(admin)),
                new Update().set("account", admin.getAccount())
                        .set("password", admin.getPassword())
                        .set("nickName", admin.getNickName())
                        .set("createTime", admin.getCreateTime()),
                Guest.COLLECTION_NAME);
    }
}
