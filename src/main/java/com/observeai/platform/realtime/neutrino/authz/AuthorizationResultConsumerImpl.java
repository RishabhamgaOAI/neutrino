/*
package com.observeai.platform.realtime.neutrino.authz;

import com.observeai.authorization.client.consumer.AuthResultConsumer;
import com.observeai.authorization.client.dto.AuthorizationFailureResult;
import com.observeai.authorization.client.dto.AuthorizationSuccessResult;
import com.observeai.platform.integration.commons.context.AuthorizationContext;
import com.observeai.platform.integration.commons.context.ContextThreadLocal;
import com.observeai.platform.realtime.neutrino.exception.neutrino.AuthenticationFailureException;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationResultConsumerImpl implements AuthResultConsumer {

    @Override
    public void authorizationSuccessfulHandler(AuthorizationSuccessResult authorizationSuccessResult) {
        AuthorizationContext authorizationContext = ContextThreadLocal.getAuthorizationContext();
        authorizationContext.setAccountId(authorizationSuccessResult.getAccountId());
        authorizationContext.setUserId(authorizationSuccessResult.getUserId());
        authorizationContext.setScope(authorizationSuccessResult.getScope().toLowerCase());
        authorizationContext.setPowerUserId(authorizationSuccessResult.getPowerUserId());
        authorizationContext.setTicketType(authorizationSuccessResult.getTicketType());
    }

    @Override
    public void authorizationFailureHandler(AuthorizationFailureResult authorizationFailureResult) {
        throw new AuthenticationFailureException(authorizationFailureResult.getMessage());
    }
}


*/
