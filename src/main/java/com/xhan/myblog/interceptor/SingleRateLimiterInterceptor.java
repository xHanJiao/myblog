package com.xhan.myblog.interceptor;

import com.xhan.myblog.controller.ControllerPropertiesBean;
import com.xhan.myblog.utils.BlogUtils;
import com.xhan.myblog.utils.MapCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;

@Slf4j
@Component(value = "RateLimiterInterceptor")
public class SingleRateLimiterInterceptor extends HandlerInterceptorAdapter {

    private static final String BANNED_IP = "BANNED_IP";
    private final ControllerPropertiesBean propertiesBean;
    private MapCache cache = MapCache.single();

    @Autowired
    public SingleRateLimiterInterceptor(ControllerPropertiesBean propertiesBean) {
        this.propertiesBean = propertiesBean;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String host = request.getRemoteAddr();
        AtomicLong one = (AtomicLong) cache.setnx(host, new AtomicLong(0), 10);
        long oneValue = one.incrementAndGet();
        log.info(format("[ip: %s]---[time: %s]---[URI: %s]---[one: %s]",
                request.getRemoteAddr(), BlogUtils.getCurrentDateTime(),
                request.getRequestURI(), oneValue));

        if (cache.hget(BANNED_IP, request.getRemoteAddr()) != null) {
            return false;
        }
        if (oneValue > propertiesBean.getPeople10SecVisit()) {
            cache.hset(BANNED_IP, host,
                    now().format(ofPattern("yyyy-MM-dd HH:mm:ss")), 300);
            log.info(format("BANNED [ip: %s] for [%s] times visit", host, oneValue));
            return false;
        }
        return true;
    }
}
