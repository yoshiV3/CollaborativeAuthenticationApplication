package com.project.collaborativeauthenticationapplication.alternative.key.user;

public interface ProgressNotifier {

    void subscribe(ProgressView view);
    void unSubScribe(ProgressView view);
}
