package com.javaproject.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class LoggingAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // get the user from the security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // "log" the attempt
        if (auth != null) {
            logger.info("{} was trying to access {}", auth.getName(), request.getRequestURI());
        }

        // redirect to the permission-denied page
        response.sendRedirect("/permission-denied");
    }
}
