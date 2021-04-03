package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.data.AuthenticationDatabase;
import com.project.collaborativeauthenticationapplication.data.SecretDao;
import com.project.collaborativeauthenticationapplication.data.SecretEntity;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;

import  java.util.ArrayList;
import  java.util.List;

public class CustomKeyGenerationPersistenceManager extends CustomTokenConsumer<KeyToken>{


    private static final String COMPONENT    = "Persistence manager";
    private static final String ERROR_REMOVE = "error during destruction of the secrets";
    private static Logger logger = new AndroidLogger();

    private        KeyPartDistributionSession session;
    private final  AuthenticationDatabase     db;
    private        ArrayList<BigNumber>       shares;

    public CustomKeyGenerationPersistenceManager() {
        db = AuthenticationDatabase.getAuthenticationDatabaseInstance();
    }

    public void receiveKeyDistributionSession(KeyPartDistributionSession session){
        this.session = session;
    }


    public boolean hasApplicationLoginWithGivenCredentials(String applicationName, String login){
        AuthenticationDatabase db            = AuthenticationDatabase.getAuthenticationDatabaseInstance();
        List<ApplicationLoginEntity> applicationLoginEntityList = db.getApplicationLoginDao().getApplicationWithNameAndLogin(applicationName, login);
        return applicationLoginEntityList.size() > 0;
    }

    public void persist(KeyToken token, AndroidSecretStorage storage) throws IllegalUseOfClosedTokenException, SecureStorageException {
        consumeToken(token);
        if (session == null){
            throw new IllegalStateException("Insufficient amount of information to properly persist the data");
        }
        ApplicationLoginEntity application             = new ApplicationLoginEntity(session.getApplicationName(), session.getLogin(), session.getThreshold());
        ApplicationLoginDao    applicationLoginDao     = db.getApplicationLoginDao();
        SecretDao secretDao = db.getSecretDao();
        try {
            applicationLoginDao.insert(application);
        }
        catch(Exception e){
            throw  new SecureStorageException("could not insert details into the system");
        }
        try{
            storage.storeSecrets(shares, session.getApplicationName(), session.getLogin());
        }
        catch(Exception e){
            List<ApplicationLoginEntity> entities = applicationLoginDao.getApplicationWithNameAndLogin(session.getApplicationName(), session.getLogin());
            for (ApplicationLoginEntity entity : entities){
                applicationLoginDao.delete(entity);
            }
            e.printStackTrace();
            throw new SecureStorageException("Could not store the secrets");
        }
        try{
            List<ApplicationLoginEntity> list = applicationLoginDao.getApplicationWithNameAndLogin(session.getApplicationName(), session.getLogin());
            SecretEntity s = new SecretEntity(list.get(0).applicationLoginId, shares.size());
            secretDao.insert(s);
        }
        catch (Exception e){
            removeCredentials(session.getApplicationName(), session.getLogin(), storage);
            throw new SecureStorageException("Could not store the secrets");
        }

    }

    public void receiveShares(ArrayList<BigNumber> shares){
        this.shares = shares;
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
