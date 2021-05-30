package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.data.AuthenticationDatabase;
import com.project.collaborativeauthenticationapplication.data.LocalSecretEntity;
import com.project.collaborativeauthenticationapplication.data.SecretDao;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;

import java.util.List;

public abstract class KeyPersistenceManager {


    private static final String COMPONENT    = "Persistence manager";
    private static final String ERROR_REMOVE = "error during destruction of the secrets";


    private static Logger logger = new AndroidLogger();


    private final AuthenticationDatabase db;

    public KeyPersistenceManager() {
        this.db = AuthenticationDatabase.getAuthenticationDatabaseInstance();
    }


    protected AuthenticationDatabase getDb() {
        return db;
    }

    public void removeCredentials(String applicationName, AndroidSecretStorage storage){
        ApplicationLoginDao applicationLoginDao = db.getApplicationLoginDao();
        List<ApplicationLoginEntity> entities = applicationLoginDao.getApplicationsWithApplication(applicationName);
        SecretDao secretDao = db.getSecretDao();
        for (ApplicationLoginEntity entity : entities){
            List<LocalSecretEntity> secrets = secretDao.getAllSecretsForApplicationLogin(entity.applicationLoginId);
            for (LocalSecretEntity localSecretEntity : secrets){
                try {
                    storage.removeSecret(applicationName, localSecretEntity.identifier);
                } catch (SecureStorageException e) {
                    logger.logError(COMPONENT, ERROR_REMOVE, "Critical", e.toString());
                }
            }
            applicationLoginDao.delete(entity);
        }
    }
}
