package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system;

import com.project.collaborativeauthenticationapplication.service.Requester;

public interface FeedbackRequester extends Requester {
    void setResult(boolean result);
}
