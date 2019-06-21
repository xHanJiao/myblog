package com.xhan.myblog.interceptor;

import com.xhan.myblog.model.content.repo.MongoLog;
import com.xhan.myblog.repository.LogRepository;
import com.xhan.myblog.utils.BlogUtils;
import com.xhan.myblog.utils.MapCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import static com.xhan.myblog.controller.ControllerConstant.ALL_MAX_VISIT_PER_5_SECOND;
import static com.xhan.myblog.controller.ControllerConstant.PEOPLE_MAX_VISIT_PER_10_SECOND;
import static java.time.LocalDateTime.now;

@Component(value = "logInterceptor")
public class LogInterceptor extends HandlerInterceptorAdapter {

    private static final String BANNED_IP = "BANNED_IP";
    private final LogRepository logRepository;
    private MapCache cache = MapCache.single();

    private static final Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

    @Autowired
    public LogInterceptor(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MongoLog log = new MongoLog(request);
        logRepository.save(log);

        String host = request.getRemoteAddr();
        cache.setnx(host, new AtomicLong(0), 10);
        cache.setnx("TOTAL_VISIT", new AtomicLong(0),100);
        AtomicLong one = cache.get(host), all = cache.get("TOTAL_VISIT");
        long oneValue = one == null ? 0 : one.incrementAndGet();
        long allValue = all == null ? 0 : all.incrementAndGet();
        logger.info(String.format("[ip: %s]---[time: %s]---[URI: %s]---[one: %s]---[all: %s]", request.getRemoteAddr(),
                BlogUtils.getCurrentDateTime(), request.getRequestURI(), oneValue, allValue));

        if (cache.hget(BANNED_IP, request.getRemoteAddr()) != null) {
            return false;
        }
        if (oneValue > PEOPLE_MAX_VISIT_PER_10_SECOND) {
            cache.hset(BANNED_IP, host,
                    now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 300);
            logger.info(String.format("BANNED [ip: %s] for [%s] times visit", host, oneValue));
            return false;
        }
        return allValue <= ALL_MAX_VISIT_PER_5_SECOND;
    }
}
