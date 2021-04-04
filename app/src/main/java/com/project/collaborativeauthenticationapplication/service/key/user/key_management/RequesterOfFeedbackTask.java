package com.project.collaborativeauthenticationapplication.service.key.user.key_management;

import com.project.collaborativeauthenticationapplication.service.key.application.key_management.FeedbackTask;

public interface RequesterOfFeedbackTask extends Requester {

    void setTask(FeedbackTask task);
    FeedbackTask getTask();
}
