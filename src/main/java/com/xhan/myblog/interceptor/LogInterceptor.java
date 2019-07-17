package com.xhan.myblog.interceptor;

import com.xhan.myblog.model.content.repo.MongoLog;
import com.xhan.myblog.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component(value = "logInterceptor")
public class LogInterceptor extends HandlerInterceptorAdapter {

    private final LogRepository logRepository;

    @Autowired
    public LogInterceptor(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MongoLog log = new MongoLog(request);
        logRepository.save(log);
        return true;
    }
}
