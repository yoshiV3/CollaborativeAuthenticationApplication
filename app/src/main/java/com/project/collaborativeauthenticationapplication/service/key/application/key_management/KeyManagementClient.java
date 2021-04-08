package com.project.collaborativeauthenticationapplication.service.key.application.key_management;

import com.project.collaborativeauthenticationapplication.service.ServiceHandler;
import com.project.collaborativeauthenticationapplication.service.Task;

public interface KeyManagementClient extends ServiceHandler {

    void remove(Task task) ;
    void extend(FeedbackTask task);
}
