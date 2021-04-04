package com.project.collaborativeauthenticationapplication.service.key.user.key_generation;

public interface ProgressNotifier {

    void subscribe(ProgressView view);
    void unSubScribe(ProgressView view);
}
