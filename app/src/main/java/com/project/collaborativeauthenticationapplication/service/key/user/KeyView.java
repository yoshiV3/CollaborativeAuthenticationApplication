package com.project.collaborativeauthenticationapplication.service.key.user;

public interface KeyView {

    void navigate(int target);
    int locate();
    void showMetaData(String login, String application);
    void showTemporally(String text);
}
