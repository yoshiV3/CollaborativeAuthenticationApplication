package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;

public interface RemoteCoordinator {

    void voteYes();
    void voteNo();
}
