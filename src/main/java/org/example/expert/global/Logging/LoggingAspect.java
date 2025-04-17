package org.example.expert.global.Logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
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
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        log.info("This is LoggingAspect.loggingAdminApi");
        log.info("Request User id = {}", request.getAttribute("userId"));
        log.info("Request time : {}", LocalDateTime.now());
        log.info("RequestURL = {}", request.getRequestURL());

        //Request Body
        Map<String, Object> paramMap = new HashMap<>();
        String[] paramNames = signature.getParameterNames();
        Object[] paramValues = joinPoint.getArgs();

        for (int i = 0; i < paramNames.length; i++) {
            if (!(paramValues[i] instanceof HttpServletRequest) && !(paramValues[i] instanceof HttpServletResponse)) {
                paramMap.put(paramNames[i], paramValues[i]);
            }
        }
        String json = new ObjectMapper().writeValueAsString(paramMap);
        log.info("Request Params = {}", json);


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
