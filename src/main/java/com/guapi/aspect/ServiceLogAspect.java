package com.guapi.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

//@Component
//@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.guapi.service.*.*(..))")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void before(JoinPoint joinPoint) {
        //用户[1.2.3.4]在[xxx]访问了[com.guapi.service.xxx()]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //此处有可能报空指针异常，因为在Producer和Consumer中直接调用了Service
        //与正常使用Controller调用Service不同。
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
//        Date date = new Date();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
         String target=joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName();

         logger.info(String.format("用户[%s],在[%s]访问了[%s].",ip,now,target));
    }
}
