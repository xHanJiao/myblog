package com.xhan.myblog.aspect;

import com.xhan.myblog.utils.MapCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

@Aspect
@Component
public class ArticleCacheAspect {

    @Resource(name = "articleRepositoryCache")
    private MapCache cache;

    private static final Logger logger = LoggerFactory.getLogger(ArticleCacheAspect.class);

    @Pointcut("execution(org.springframework.data.domain.Page com.xhan.myblog.repository.ArticleRepository.findAllBy*(..))")
    public void findAllPagePoint() {
    }

    @Pointcut("execution(org.springframework.data.domain.Page com.xhan.myblog.repository.ArticleRepository.getAllBy*(..))")
    public void getAllPagePoint() {
    }

    @Around(value = "getAllPagePoint()")
    public Object cacheForGetAllIdTitleTimeState(ProceedingJoinPoint jp) throws Throwable {
        return getAndCache(jp);
    }


    @Around(value = "findAllPagePoint()")
    public Object cacheForFindAllIdTitleTimeStateContent(ProceedingJoinPoint jp) throws Throwable {
        return getAndCache(jp);
    }

    private Object getAndCache(ProceedingJoinPoint jp) throws Throwable {
        try {
            String methodName = jp.getSignature().getName(),
                    methodArgs = Arrays.toString(jp.getArgs());
            Object result = cache.hget(methodName, methodArgs);
            if (result == null) {
                logger.info(String.format("MISS CACHE --- jp signature - {[%s]} - args - {%s}",
                        methodName, methodArgs));
                result = jp.proceed(jp.getArgs());
                cache.hset(methodName, methodArgs, result);
            } else {
                logger.info(String.format("HIT CACHE --- jp signature - {[%s]} - args - {%s}",
                        methodName, methodArgs));
            }
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }
    }
}
