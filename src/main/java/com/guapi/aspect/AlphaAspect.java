package com.guapi.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

//@Component
//@Aspect
//是一个切面组件
public class AlphaAspect {
    //com.guapi.service.*.*(..))第一个*代表所有类，所有业务组件，第二个表示所有方法，(..)表示所有参数，所有返回值，也可以写确定的返回值
    @Pointcut("execution(* com.guapi.service.*.*(..))")
    public void pointCut(){}

    @Before("pointCut()")
    public void before(){
        System.out.println("before");
    }
    @AfterReturning("pointCut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    @AfterThrowing("pointCut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }
    @Around("pointCut()")
    //joinPoint----链接点
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        //调用目标对象被处理的方法
        System.out.println("around before");
        Object proceed = joinPoint.proceed();
        System.out.println("around after");
        return proceed;
    }
}
