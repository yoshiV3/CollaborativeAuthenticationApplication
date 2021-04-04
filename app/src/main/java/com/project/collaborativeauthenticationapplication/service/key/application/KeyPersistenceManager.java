package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.data.AuthenticationDatabase;
import com.project.collaborativeauthenticationapplication.data.SecretDao;
import com.project.collaborativeauthenticationapplication.data.SecretEntity;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;

import java.util.List;

public abstract class KeyPersistenceManager extends  CustomTokenConsumer<KeyToken>{


    private static final String COMPONENT    = "Persistence manager";
    private static final String ERROR_REMOVE = "error during destruction of the secrets";


    private static Logger logger = new AndroidLogger();


    private final AuthenticationDatabase db;

    protected KeyPersistenceManager() {
        this.db = AuthenticationDatabase.getAuthenticationDatabaseInstance();
    }


    protected AuthenticationDatabase getDb() {
        return db;
    }

    public void removeCredentials(String applicationName, String login, AndroidSecretStorage storage){
        ApplicationLoginDao applicationLoginDao = db.getApplicationLoginDao();
        List<ApplicationLoginEntity> entities = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, login);
        SecretDao secretDao = db.getSecretDao();
        for (ApplicationLoginEntity entity : entities){
            List<SecretEntity> secrets = secretDao.getAllSecretsForApplicationLogin(entity.applicationLoginId);
            int length = secrets.get(0).length;
            try {
                storage.removeSecrets(applicationName, login, length);
            } catch (SecureStorageException e) {
                logger.logError(COMPONENT, ERROR_REMOVE, "Critical", e.toString());
            }
            applicationLoginDao.delete(entity);
        }
    }
}
