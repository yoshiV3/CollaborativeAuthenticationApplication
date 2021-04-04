package com.project.collaborativeauthenticationapplication.service.key.application.key_management;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginParticipantDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginParticipantJoin;
import com.project.collaborativeauthenticationapplication.data.AuthenticationDatabase;
import com.project.collaborativeauthenticationapplication.data.SecretDao;
import com.project.collaborativeauthenticationapplication.data.SecretEntity;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;

import java.util.List;

public class CustomKeyManagementViewManager {

    private static final String COMPONENT       =  "Key Management persistence manager";
    private static final String EVENT_FOUND_AL  =  "found application logins";
    private static Logger logger = new AndroidLogger();

    private final AuthenticationDatabase db;

    public CustomKeyManagementViewManager() {
        db = AuthenticationDatabase.getAuthenticationDatabaseInstance();
    }

    public List<ApplicationLoginEntity> getAllCredentials(){
        ApplicationLoginDao doa = db.getApplicationLoginDao();
        List<ApplicationLoginEntity> applications = doa.getApplications();
        logger.logEvent(COMPONENT, EVENT_FOUND_AL, "low", String.valueOf(applications.size()));
        return applications;
    }

    public int getNumberOfLocalKeys(String applicationName, String login){
        SecretDao           secretDao               = db.getSecretDao();
        ApplicationLoginDao applicationLoginDao     = db.getApplicationLoginDao();

        List<ApplicationLoginEntity> applicationLoginEntities = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, login);
        if (applicationLoginEntities.size()==0){
            return 0;
        }
        long applicationLoginId = applicationLoginEntities.get(0).applicationLoginId;
        List<SecretEntity> secretEntities = secretDao.getAllSecretsForApplicationLogin(applicationLoginId);
        return secretEntities.size();
    }

    public int getNumberOfRemoteKeys(String applicationName, String login){
        ApplicationLoginParticipantDao   applicationLoginParticipantDao     = db.getApplicationLoginParticipantDao();


        List<ApplicationLoginParticipantJoin> entities = applicationLoginParticipantDao.getAllInformation(applicationName, login);
        int total = 0;
        for (ApplicationLoginParticipantJoin join : entities){
            total += join.weight;
        }
        return total;
    }


    public int getNumberOfRemoteParticipants(String applicationName, String login){
        ApplicationLoginParticipantDao   applicationLoginParticipantDao     = db.getApplicationLoginParticipantDao();


        List<ApplicationLoginParticipantJoin> entities = applicationLoginParticipantDao.getAllInformation(applicationName, login);
        return entities.size();
    }


}
