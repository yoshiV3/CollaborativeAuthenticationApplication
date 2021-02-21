package com.project.collaborativeauthenticationapplication.service;

public class IllegalNumberOfTokensException extends TokenException {

    private static final String MESSAGE = "too many tokens currently active ( ";

    public IllegalNumberOfTokensException(String message)
    {
        super(MESSAGE + message+ ")");
    }
}
