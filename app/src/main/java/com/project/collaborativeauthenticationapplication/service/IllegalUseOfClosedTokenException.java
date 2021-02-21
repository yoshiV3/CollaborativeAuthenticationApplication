package com.project.collaborativeauthenticationapplication.service;

public class IllegalUseOfClosedTokenException extends TokenException {


    private static final String MESSAGE = "token used after revocation ( ";

    public IllegalUseOfClosedTokenException(String message) {
        super(MESSAGE + message + ")");
    }
}
