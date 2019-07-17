package com.xhan.myblog.interceptor;

import com.xhan.myblog.controller.ControllerPropertiesBean;
import com.xhan.myblog.model.content.repo.MongoLog;
import com.xhan.myblog.repository.LogRepository;
import com.xhan.myblog.utils.MapCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;

@Component(value = "logInterceptor")
public class LogInterceptor extends HandlerInterceptorAdapter {

    private static final String BANNED_IP = "BANNED_IP";
    private final ControllerPropertiesBean propertiesBean;
    private final LogRepository logRepository;
    private MapCache cache = MapCache.single();

    private static final Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

    @Autowired
    public LogInterceptor(ControllerPropertiesBean propertiesBean, LogRepository logRepository) {
        this.propertiesBean = propertiesBean;
        this.logRepository = logRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MongoLog log = new MongoLog(request);
        logRepository.save(log);

        String host = request.getRemoteAddr();
        // 一个bug，这里没有考虑到过期的情况啊
        AtomicLong one = (AtomicLong) cache.setnx(host, new AtomicLong(0), 10),
                all = (AtomicLong) cache.setnx("TOTAL_VISIT", new AtomicLong(0), 100);
        long oneValue = one.incrementAndGet();
        long allValue = all.incrementAndGet();
//        logger.info(format("[ip: %s]---[time: %s]---[URI: %s]---[one: %s]---[all: %s]",
//                request.getRemoteAddr(), BlogUtils.getCurrentDateTime(),
//                request.getRequestURI(), oneValue, allValue));

        if (cache.hget(BANNED_IP, request.getRemoteAddr()) != null) {
            return false;
        }
        if (oneValue > propertiesBean.getPeople10SecVisit()) {
            cache.hset(BANNED_IP, host,
                    now().format(ofPattern("yyyy-MM-dd HH:mm:ss")), 300);
            logger.info(format("BANNED [ip: %s] for [%s] times visit", host, oneValue));
            return false;
        }
        return allValue <= propertiesBean.getAll5SecVisit();
    }
}
