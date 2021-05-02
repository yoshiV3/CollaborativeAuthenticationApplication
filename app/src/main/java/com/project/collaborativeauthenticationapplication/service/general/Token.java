package com.project.collaborativeauthenticationapplication.service.general;

public interface Token {

    int getIdentifier();

    void close();

    boolean isClosed();
}
