package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.key.application.key_management.Task;

public interface KeyManagementClient extends Client {

    void remove(Task task) ;
}
