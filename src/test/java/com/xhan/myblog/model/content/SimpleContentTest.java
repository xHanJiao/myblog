package com.xhan.myblog.model.content;

import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleContentTest {

    @Test
    public void test() {
        SimpleContent content = new SimpleContent();
        System.out.println(content.getCreateTime());
    }

}