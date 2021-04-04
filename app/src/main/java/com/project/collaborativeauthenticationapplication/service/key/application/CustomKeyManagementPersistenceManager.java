package com.project.collaborativeauthenticationapplication.service.key.application;


import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;


import java.util.List;

public class CustomKeyManagementPersistenceManager extends KeyPersistenceManager{

    CustomKeyManagementViewManager viewManager=  new CustomKeyManagementViewManager();

    public List<ApplicationLoginEntity> getAllCredentials(){
        return viewManager.getAllCredentials();
    }

    public int getNumberOfLocalKeys(String applicationName, String login){
        return viewManager.getNumberOfLocalKeys(applicationName, login);
    }

    public int getNumberOfRemoteKeys(String applicationName, String login){
        return viewManager.getNumberOfRemoteKeys(applicationName, login);
    }


    public int getNumberOfRemoteParticipants(String applicationName, String login){
        return viewManager.getNumberOfRemoteParticipants(applicationName, login);
    }

}
