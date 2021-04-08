package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.data.AuthenticationDatabase;
import com.project.collaborativeauthenticationapplication.data.SecretDao;
import com.project.collaborativeauthenticationapplication.data.SecretEntity;
import com.project.collaborativeauthenticationapplication.service.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;

import com.project.collaborativeauthenticationapplication.service.key.application.KeyPersistenceManager;


import java.nio.charset.StandardCharsets;
import  java.util.List;

public class CustomKeyGenerationPersistenceManager extends KeyPersistenceManager {

    public CustomKeyGenerationPersistenceManager() {
        super();
    }


    public boolean hasApplicationLoginWithGivenCredentials(String applicationName, String login){
        AuthenticationDatabase db            = AuthenticationDatabase.getAuthenticationDatabaseInstance();
        List<ApplicationLoginEntity> applicationLoginEntityList = db.getApplicationLoginDao().getApplicationWithNameAndLogin(applicationName, login);
        return applicationLoginEntityList.size() > 0;
    }


    public void persist(AndroidSecretStorage storage, List<BigNumber> shares, Point publicKey, KeyGenerationSession session) throws SecureStorageException {
        IdentifiedParticipant local = session.getLocalParticipant();
        int firstIndex   = local.getIdentifier();
        int weight       = local.getWeight();

        int[] identifiers = new int[weight];
        for (int i = 0; i < weight; i++){
            identifiers[i] = firstIndex + i;
        }

        String publicKeyX = new String(publicKey.getX().getBigNumberAsByteArray(), StandardCharsets.ISO_8859_1);
        String publicKeyY = new String(publicKey.getY().getBigNumberAsByteArray(), StandardCharsets.ISO_8859_1);
        ApplicationLoginEntity application             = new ApplicationLoginEntity(session.getApplicationName(), session.getLogin(), session.getThreshold(), publicKeyX, publicKeyY, publicKey.isZero());
        ApplicationLoginDao    applicationLoginDao     = getDb().getApplicationLoginDao();
        SecretDao               secretDao              = getDb().getSecretDao();
        try {
            applicationLoginDao.insert(application);
        }
        catch(Exception e){
            throw  new SecureStorageException("could not insert details into the system");
        }
        try{
            storage.storeSecrets(shares, identifiers, session.getApplicationName(), session.getLogin());
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
            for (int i: identifiers){
                SecretEntity s = new SecretEntity(list.get(0).applicationLoginId, i);
                secretDao.insert(s);
            }
        }
        catch (Exception e){
            removeCredentials(session.getApplicationName(), session.getLogin(), storage);
            throw new SecureStorageException("Could not store the secrets");
        }

    }
}
