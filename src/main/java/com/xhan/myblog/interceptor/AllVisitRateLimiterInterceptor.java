package com.xhan.myblog.interceptor;

import com.xhan.myblog.controller.ControllerConstant;
import com.xhan.myblog.exceptions.TooManyVisitorException;
import com.xhan.myblog.utils.TokenBucketManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.xhan.myblog.controller.ControllerConstant.ALL_VISIT_KEY;

@Component(value = "allVisitRateLimiterInterceptor")
public class AllVisitRateLimiterInterceptor extends HandlerInterceptorAdapter {

    private final TokenBucketManager manager;

    @Autowired
    public AllVisitRateLimiterInterceptor(TokenBucketManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!manager.tryAcquire(ALL_VISIT_KEY, ControllerConstant.MAX_WAIT_SECOND))
            throw new TooManyVisitorException();
        return true;
    }
}
