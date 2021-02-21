package com.project.collaborativeauthenticationapplication.service.key;

public interface KeyView {

    void navigate(int target);
    int locate();
    void showMetaData(String login, String application);

}
