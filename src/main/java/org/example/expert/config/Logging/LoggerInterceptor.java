package org.example.expert.config.Logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;


public class LoggerInterceptor implements HandlerInterceptor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("This is LoggerInterceptor.preHandle");
        log.info("RequestURL = {}", request.getRequestURL());
        log.info("Request time : {}", LocalDateTime.now());
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

}
