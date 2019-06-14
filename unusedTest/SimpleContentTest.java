package com.xhan.myblog.model.content;

import com.xhan.myblog.model.content.repo.SimpleContent;
import org.junit.Test;

public class SimpleContentTest {

    @Test
    public void test() {
        SimpleContent content = new SimpleContent();
        System.out.println(content.getCreateTime());
    }

}