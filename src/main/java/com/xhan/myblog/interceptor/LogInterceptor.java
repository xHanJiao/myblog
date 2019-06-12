package com.xhan.myblog.interceptor;

import com.xhan.myblog.model.content.repo.MongoLog;
import com.xhan.myblog.repository.LogRepository;
import com.xhan.myblog.utils.MapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

import static com.xhan.myblog.controller.ControllerConstant.ALL_MAX_VISIT_PER_5_SECOND;
import static com.xhan.myblog.controller.ControllerConstant.PEOPLE_MAX_VISIT_PER_10_SECOND;
import static java.time.LocalDateTime.now;

@Component(value = "logInterceptor")
public class LogInterceptor extends HandlerInterceptorAdapter {

    private final LogRepository logRepository;
    private MapCache cache = MapCache.single();

    @Autowired
    public LogInterceptor(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (cache.hget("BANNED_IP", request.getRemoteAddr()) != null) {
            return false;
        }
        MongoLog log = new MongoLog(request);
        logRepository.save(log);
        cache.setnx(log.getHost(), new AtomicLong(0), 10);
        cache.setnx("TOTAL_VISIT", new AtomicLong(0),100);
        AtomicLong one = cache.get(log.getHost()), all = cache.get("TOTAL_VISIT");
        long oneValue = one == null ? 0 : one.incrementAndGet();
        long allValue = all == null ? 0 : all.incrementAndGet();
        if (oneValue > PEOPLE_MAX_VISIT_PER_10_SECOND) {
            cache.hset("BANNED_IP", log.getHost(),
                    now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 300);
        }

        return oneValue <= PEOPLE_MAX_VISIT_PER_10_SECOND && allValue <= ALL_MAX_VISIT_PER_5_SECOND;
    }
}
