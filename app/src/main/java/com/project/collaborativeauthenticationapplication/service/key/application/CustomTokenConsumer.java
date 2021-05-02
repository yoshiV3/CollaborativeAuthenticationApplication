package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.general.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.general.Token;

public abstract class CustomTokenConsumer<T extends Token> {

    protected void consumeToken(T token) throws IllegalUseOfClosedTokenException {
        if (token == null){
            throw new IllegalUseOfClosedTokenException( "null token: no access" );
        }
        if (token.isClosed())
        {
            throw new IllegalUseOfClosedTokenException( "generate session");
        }
    }

}
