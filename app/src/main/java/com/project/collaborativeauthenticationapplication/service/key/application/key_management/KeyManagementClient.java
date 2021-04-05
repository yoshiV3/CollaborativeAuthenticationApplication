package com.project.collaborativeauthenticationapplication.service.key.application.key_management;

import com.project.collaborativeauthenticationapplication.service.Client;
import com.project.collaborativeauthenticationapplication.service.Task;

public interface KeyManagementClient extends Client {

    void remove(Task task) ;
    void extend(FeedbackTask task);
}
