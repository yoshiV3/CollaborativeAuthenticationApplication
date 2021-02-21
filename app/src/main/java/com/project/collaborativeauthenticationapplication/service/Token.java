package com.project.collaborativeauthenticationapplication.service;

public interface Token {

    int getIdentifier();

    void close();

    boolean isClosed();
}
