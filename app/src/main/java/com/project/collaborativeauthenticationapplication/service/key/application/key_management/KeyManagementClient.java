package com.project.collaborativeauthenticationapplication.service.key.application.key_management;

import com.project.collaborativeauthenticationapplication.service.key.application.Client;
import com.project.collaborativeauthenticationapplication.service.key.application.key_management.Task;

public interface KeyManagementClient extends Client {

    void remove(Task task) ;
    void extend(FeedbackTask task);
}
