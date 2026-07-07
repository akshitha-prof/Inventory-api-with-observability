package com.inventory.observability.metrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Timer;

/**
 * AOP aspect that automatically records business metrics when
 * service methods are called — no manual instrumentation in service code.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsAspect {

    private final BusinessMetrics metrics;

    @Around("execution(* com.inventory.service.TransactionService.borrowItem(..))")
    public Object aroundBorrow(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = metrics.startBorrowTimer();
        try {
            Object result = joinPoint.proceed();
            metrics.recordBorrow();
            return result;
        } finally {
            metrics.stopBorrowTimer(sample);
        }
    }

    @Around("execution(* com.inventory.service.TransactionService.returnItem(..))")
    public Object aroundReturn(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        metrics.recordReturn();
        return result;
    }

    @Around("execution(* com.inventory.service.AuthService.register(..))")
    public Object aroundRegister(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        metrics.recordRegistration();
        return result;
    }
}
