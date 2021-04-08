package com.project.collaborativeauthenticationapplication.service.key.application.key_management;


import com.project.collaborativeauthenticationapplication.data.ApplicationLoginDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.data.SecretDao;
import com.project.collaborativeauthenticationapplication.data.SecretEntity;
import com.project.collaborativeauthenticationapplication.service.CustomKeyViewManager;
import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.key.application.KeyPersistenceManager;


import java.util.ArrayList;
import java.util.List;

public class CustomKeyManagementPersistenceManager extends KeyPersistenceManager {

    CustomKeyViewManager viewManager=  new CustomKeyViewManager();


    public int [] getSharesAndIdentifiersForRecovery(KeyToken token, String applicationName, String login,
                                                   AndroidSecretStorage storage, ArrayList<BigNumber> shares) throws IllegalUseOfClosedTokenException, SecureStorageException {

        int threshold  = getThreshold(applicationName, login);
        int[] identifiers = new int[threshold];
        int localKeys  = getNumberOfLocalKeys(applicationName, login);
        int remoteKeys = getNumberOfRemoteKeys(applicationName, login);
        if (localKeys >= threshold){
            ApplicationLoginDao applicationLoginDao    = getDb().getApplicationLoginDao();
            SecretDao secretDao                        = getDb().getSecretDao();
            List<ApplicationLoginEntity> loginEntities = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, login);
            if (loginEntities.size() == 0){
                throw new IllegalArgumentException("Credentials are unknown");
            }
            List<SecretEntity> entities = secretDao.getAllSecretsForApplicationLogin(loginEntities.get(0).applicationLoginId);
            for (int i =0; i < threshold; i++){
                SecretEntity secretEntity = entities.get(i);
                int identifier = secretEntity.identifier;
                shares.add(storage.getSecrets(applicationName,login, identifier));
                identifiers[i] = identifier;
            }
            return identifiers;
        }
        else if ( localKeys + remoteKeys < threshold){
            throw new IllegalStateException("Cannot generate a new share with less than a threshold number of shares");
        }
        else {
            throw new UnsupportedOperationException("We do not yet offer this functionality. Please contact the developer");
        }
    }


    public void persist(KeyToken token, String applicationName, String login, BigNumber share, int identifier, AndroidSecretStorage storage)
            throws IllegalUseOfClosedTokenException, SecureStorageException {
        SecretDao secretDao                         = getDb().getSecretDao();
        ApplicationLoginDao applicationLoginDao     = getDb().getApplicationLoginDao();
        storage.storeSecret(share, identifier, applicationName, login);
        try{
            List<ApplicationLoginEntity> loginEntities = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, login);
            if (loginEntities.size() == 0){
                throw new IllegalArgumentException("Credentials are unknown");
            }
            SecretEntity s = new SecretEntity(loginEntities.get(0).applicationLoginId, identifier);
            secretDao.insert(s);
        }
        catch (Exception e){
            storage.removeSecret(applicationName, login, identifier);
            throw  new IllegalStateException("Failed");
        }
    }


    private int getThreshold(String applicationName, String login){
        ApplicationLoginDao dao = getDb().getApplicationLoginDao();
        List<ApplicationLoginEntity> entities = dao.getApplicationWithNameAndLogin(applicationName, login);
        if (entities.size() == 0){
            return 0;
        }
        return entities.get(0).threshold;
    }


    public List<ApplicationLoginEntity> getAllCredentials(){
        return viewManager.getAllCredentials();
    }

    public int getTotalNumberOfKeys(String applicationName, String login){
        return getNumberOfLocalKeys(applicationName, login) + getNumberOfRemoteKeys(applicationName, login);
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
