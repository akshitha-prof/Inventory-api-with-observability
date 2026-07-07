package com.inventory.observability.tracing;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Creates X-Ray subsegments for service and repository method calls.
 * This gives you a breakdown of where time is spent within each request:
 *
 * Request Segment
 *   └── Controller (auto-traced by servlet filter)
 *       ├── Service.borrowItem (this aspect)
 *       │   ├── Repository.findById (this aspect)
 *       │   └── Repository.save (this aspect)
 *       └── Response
 */
@Aspect
@Component
@Profile("aws")
@Slf4j
public class XRayTracingInterceptor {

    @Around("execution(* com.inventory.service.*.*(..))")
    public Object traceServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String segmentName = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();

        Subsegment subsegment = AWSXRay.beginSubsegment(segmentName);
        try {
            subsegment.putMetadata("class", joinPoint.getSignature().getDeclaringTypeName());
            subsegment.putMetadata("method", joinPoint.getSignature().getName());
            return joinPoint.proceed();
        } catch (Exception e) {
            subsegment.addException(e);
            throw e;
        } finally {
            AWSXRay.endSubsegment();
        }
    }

    @Around("execution(* com.inventory.repository.*.*(..))")
    public Object traceRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String segmentName = "DB:" + joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();

        Subsegment subsegment = AWSXRay.beginSubsegment(segmentName);
        try {
            subsegment.putAnnotation("db.operation", joinPoint.getSignature().getName());
            return joinPoint.proceed();
        } catch (Exception e) {
            subsegment.addException(e);
            throw e;
        } finally {
            AWSXRay.endSubsegment();
        }
    }
}
