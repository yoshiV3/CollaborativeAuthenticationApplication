package com.project.collaborativeauthenticationapplication.service.key.user;

public interface ProgressNotifier {

    void subscribe(ProgressView view);
    void unSubScribe(ProgressView view);
}
