package com.project.collaborativeauthenticationapplication.service.key.application.key_management;

import com.project.collaborativeauthenticationapplication.service.general.ServiceHandler;
import com.project.collaborativeauthenticationapplication.service.general.Task;

public interface KeyManagementClient extends ServiceHandler {

    void remove(Task task) ;
    void extend(FeedbackTask task);
}
