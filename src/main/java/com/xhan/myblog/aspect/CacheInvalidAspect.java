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

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.xhan.myblog.controller.ControllerConstant.POST_NUM;

@Aspect
@Component("cacheInvalidAspect")
public class CacheInvalidAspect {

    private final MapCache cache = MapCache.single();

    @Resource(name = "articleRepositoryCache")
    private MapCache articleRepositoryCache;

    @Pointcut("@annotation(com.xhan.myblog.annotation.CacheInvalid)")
    public void deletePoint() {
    }

    @AfterReturning(pointcut = "deletePoint()")
    public void doAfter(JoinPoint jp) {
        String [] keys = {POST_NUM + true, POST_NUM + false};
        Arrays.stream(keys).forEach(cache::del);
        articleRepositoryCache.clean();
    }

    private CacheInvalid getAnnotationCacheInvalid(JoinPoint jp) {
        Signature signature = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        return method == null ? null : method.getAnnotation(CacheInvalid.class);
    }

}
