package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.Token;

public abstract class CustomTokenConsumer<T extends Token> {

    protected void consumeToken(T token) throws IllegalUseOfClosedTokenException {
        if (token.isClosed())
        {
            throw new IllegalUseOfClosedTokenException( "generate session");
        }
    }

}
