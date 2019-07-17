package com.xhan.myblog.aspect;

import com.xhan.myblog.utils.MapCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

@Aspect
@Component
public class ArticleCacheAspect {

    @Resource(name = "articleRepositoryCache")
    private MapCache cache;

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
                result = jp.proceed(jp.getArgs());
                cache.hset(methodName, methodArgs, result);
            }
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }
    }
}
