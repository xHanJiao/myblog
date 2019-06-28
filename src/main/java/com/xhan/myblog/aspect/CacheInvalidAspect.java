package com.xhan.myblog.aspect;

import com.xhan.myblog.annotation.CacheInvalid;
import com.xhan.myblog.utils.MapCache;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component("cacheInvalidAspect")
public class CacheInvalidAspect {

    private final MapCache cache = MapCache.single();

    @Pointcut("@annotation(com.xhan.myblog.annotation.CacheInvalid)")
    public void deletePoint() {
    }

    @AfterReturning(pointcut = "deletePoint()")
    public void doAfter(JoinPoint jp) {
        CacheInvalid cacheInvalid = getAnnotationCacheInvalid(jp);
        if (cacheInvalid == null) return;
        String [] keys = cacheInvalid.keys();
        Arrays.stream(keys).forEach(cache::del);
    }

    private CacheInvalid getAnnotationCacheInvalid(JoinPoint jp) {
        Signature signature = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        return method == null ? null : method.getAnnotation(CacheInvalid.class);
    }

}
