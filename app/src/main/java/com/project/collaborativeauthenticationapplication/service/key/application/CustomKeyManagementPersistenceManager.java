package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.data.AuthenticationDatabase;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;

import java.util.List;

public class CustomKeyManagementPersistenceManager {

    private static final String COMPONENT       =  "Key Management persistence manager";
    private static final String EVENT_FOUND_AL  =  "found application logins";
    private static Logger logger = new AndroidLogger();

    private final AuthenticationDatabase db;

    public CustomKeyManagementPersistenceManager() {
        db = AuthenticationDatabase.getAuthenticationDatabaseInstance();
    }

    public List<ApplicationLoginEntity> getAllCredentials(){
        ApplicationLoginDao doa = db.getApplicationLoginDao();
        List<ApplicationLoginEntity> applications = doa.getApplications();
        logger.logEvent(COMPONENT, EVENT_FOUND_AL, "low", String.valueOf(applications.size()));
        return applications;
    }
}
