package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.persistance;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginParticipantDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginParticipantJoin;
import com.project.collaborativeauthenticationapplication.data.AuthenticationDatabase;
import com.project.collaborativeauthenticationapplication.data.ParticipantDao;
import com.project.collaborativeauthenticationapplication.data.ParticipantEntity;
import com.project.collaborativeauthenticationapplication.data.SecretDao;
import com.project.collaborativeauthenticationapplication.data.LocalSecretEntity;
import com.project.collaborativeauthenticationapplication.data.StoreCredentialDao;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;

import com.project.collaborativeauthenticationapplication.service.key.application.KeyPersistenceManager;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSession;


import java.nio.charset.StandardCharsets;
import  java.util.List;

public class CustomKeyGenerationPersistenceManager extends KeyPersistenceManager {

    public CustomKeyGenerationPersistenceManager() {
        super();
    }


    public static final int STATE_LOCAL_PERSISTENCE = 1;
    public static final int STATE_CONFIRMED         = 2;




    private static Logger logger = new AndroidLogger();

    public boolean hasApplicationLoginWithGivenCredentials(String applicationName, String login){
        AuthenticationDatabase db            = AuthenticationDatabase.getAuthenticationDatabaseInstance();
        List<ApplicationLoginEntity> applicationLoginEntityList = db.getApplicationLoginDao().getApplicationWithNameAndLogin(applicationName, login);
        return applicationLoginEntityList.size() > 0;
    }


    public void confirm(String applicationName, String login){
        logger.logEvent("persistence manager", "confirm", "normal");
        ApplicationLoginParticipantDao dao = getDb().getApplicationLoginParticipantDao();
        List<ApplicationLoginParticipantJoin> joins = dao.getAllInformation(applicationName, login);
        for (ApplicationLoginParticipantJoin join: joins){
            join.state = STATE_CONFIRMED;
            dao.updateJoin(join);
        }
    }


    public void persist(AndroidSecretStorage storage, List<BigNumber> shares, Point publicKey, KeyGenerationSession session) throws SecureStorageException {
        logger.logEvent("persistence manager", "persist", "normal");
        IdentifiedParticipant local = session.getLocalParticipant();
        int firstIndex   = local.getIdentifier();
        int weight       = local.getWeight();

        StoreCredentialDao store           = getDb().getStoreDao();


        logger.logEvent("Storage", "persist request", "low", String.valueOf(firstIndex));

        int[] identifiers = new int[weight];
        for (int i = 0; i < weight; i++){
            identifiers[i] = firstIndex + i;
        }

        store.storeCredentialData(identifiers, publicKey, session);
        if (store.getSuccess()){
            try{
                storage.storeSecrets(shares, identifiers, session.getApplicationName(), session.getLogin());
            }
            catch(Exception e){
                removeCredentials(session.getApplicationName(), session.getLogin(), storage);
                throw new SecureStorageException("failed during persistence");
            }
        } else {
            throw new SecureStorageException("failed during persistence");
        }
    }
}
