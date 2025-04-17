package org.example.expert.config.Logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..))"
        + "|| execution(* org.example.expert.domain.user.controller.UserAdminController.*(..))")
    public void controller(){
    }

    @Around("controller()")
    public Object loggingAdminApi(ProceedingJoinPoint joinPoint) throws Throwable{
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        log.debug("This is LoggingAspect.loggingAdminApi");
        log.info("Request User id = {}", request.getAttribute("userId"));
        log.info("Request time : {}", LocalDateTime.now());
        log.info("RequestURL = {}", request.getRequestURL());

        //Request Body
        for(Object arg : joinPoint.getArgs()){
            if(!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse)){
                continue;
            }

            try {
                String requestBodyJson = new ObjectMapper().writeValueAsString(arg);
                log.info("RequestBody = {}", requestBodyJson);
            } catch (Exception e) {
                log.warn("Failed to serialize request body: {}", arg.getClass().getName());
            }
        }

        //메서드 실행
        Object result = joinPoint.proceed();

        //ResponseBody
        try{
            String responseBody = objectMapper.writeValueAsString(result);
            log.info("ResponseBody ={}", responseBody);
        } catch (Exception e) {
            log.warn("Failed to serialize response body: {}", result);
        }

        return result;
    }

}
