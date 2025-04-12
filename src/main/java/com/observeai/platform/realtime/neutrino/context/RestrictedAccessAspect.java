package com.observeai.platform.realtime.neutrino.context;

import com.observeai.authorization.client.dto.AuthContextDTO;
import com.observeai.platform.realtime.neutrino.auth.AuthenticationService;
import com.observeai.platform.realtime.neutrino.config.WebSocketConfig;
import com.observeai.platform.realtime.neutrino.exception.neutrino.AuthenticationFailureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RestrictedAccessAspect {
    private final AuthenticationService authenticationService;
    private final WebSocketConfig webSocketConfig;

    @Around("@annotation(restrictedAccess)")
    public Object verifyAuthentication(ProceedingJoinPoint joinPoint, RestrictedAccess restrictedAccess) throws Throwable {
        if (!webSocketConfig.isEnableAuth()) {
            return joinPoint.proceed();
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");
        AuthContextDTO authContextDTO = authenticationService.authenticate(authHeader);

        if (authContextDTO == null || authContextDTO.getAccountId() == null) {
            throw new AuthenticationFailureException("authorization header is not present or not valid");
        }
        return joinPoint.proceed();
    }
}
