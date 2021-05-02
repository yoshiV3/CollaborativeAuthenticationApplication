package com.project.collaborativeauthenticationapplication.service.general;

public class TokenException extends Exception {
    private static final String MESSAGE = "TOKEN EXCEPTION: ";

    public TokenException(String message)
    {
        super(MESSAGE + message);
    }
}
