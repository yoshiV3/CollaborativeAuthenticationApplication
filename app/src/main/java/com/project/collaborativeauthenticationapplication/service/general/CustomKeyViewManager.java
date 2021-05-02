package com.project.collaborativeauthenticationapplication.service.general;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginParticipantDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginParticipantJoin;
import com.project.collaborativeauthenticationapplication.data.AuthenticationDatabase;
import com.project.collaborativeauthenticationapplication.data.RemoteDao;
import com.project.collaborativeauthenticationapplication.data.RemoteSecretEntity;
import com.project.collaborativeauthenticationapplication.data.SecretDao;
import com.project.collaborativeauthenticationapplication.data.LocalSecretEntity;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CustomKeyViewManager {

    private static final String COMPONENT       =  "Key Management persistence manager";
    private static final String EVENT_FOUND_AL  =  "found application logins";
    private static Logger logger = new AndroidLogger();

    private final AuthenticationDatabase db;

    public CustomKeyViewManager() {
        db = AuthenticationDatabase.getAuthenticationDatabaseInstance();
    }



    public List<ApplicationLoginEntity> getAllCredentials(){
        ApplicationLoginDao doa = db.getApplicationLoginDao();
        List<ApplicationLoginEntity> applications = doa.getApplications();
        logger.logEvent(COMPONENT, EVENT_FOUND_AL, "low", String.valueOf(applications.size()));
        return applications;
    }

    public List<String> getAllRemoteParticipantsFor(String applicationName, String login){
        ApplicationLoginParticipantDao   applicationLoginParticipantDao     = db.getApplicationLoginParticipantDao();
        List<ApplicationLoginParticipantJoin> entities = applicationLoginParticipantDao.getAllInformation(applicationName, login);

        ArrayList<String>  allRemoteParticipants = new ArrayList<>();
        for(ApplicationLoginParticipantJoin join : entities){
            allRemoteParticipants.add(join.participantId);
        }
        return allRemoteParticipants;
    }

    public Point getPublicKeyForCredential(String application, String login){
        ApplicationLoginDao applicationLoginDao     = db.getApplicationLoginDao();
        List<ApplicationLoginEntity> applicationLoginEntities = applicationLoginDao.getApplicationWithNameAndLogin(application, login);
        ApplicationLoginEntity entity = applicationLoginEntities.get(0);
        String publicKeyX = entity.publicKeyX;
        String publicKeyY = entity.publicKeyY;
        Point publicKey = new Point(new BigNumber(publicKeyX.getBytes(StandardCharsets.ISO_8859_1)),
                new BigNumber(publicKeyY.getBytes(StandardCharsets.ISO_8859_1)), entity.publicKeyIsZero);
        return publicKey;

    }

    public int getNumberOfLocalKeys(String applicationName, String login){
        SecretDao           secretDao               = db.getSecretDao();
        ApplicationLoginDao applicationLoginDao     = db.getApplicationLoginDao();


        List<ApplicationLoginEntity> applicationLoginEntities = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, login);
        if (applicationLoginEntities.size()==0){
            return 0;
        }
        long applicationLoginId = applicationLoginEntities.get(0).applicationLoginId;
        List<LocalSecretEntity> secretEntities = secretDao.getAllSecretsForApplicationLogin(applicationLoginId);
        return secretEntities.size();
    }


    public int[] getAllRemoteIdentifiers(String applicationName, String login, String address){
        ApplicationLoginDao applicationLoginDao     = db.getApplicationLoginDao();
        RemoteDao           remoteDao               = db.getRemoteDao();
        List<ApplicationLoginEntity> applicationLoginEntities = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, login);
        List<RemoteSecretEntity>  secrets           =  remoteDao.getRemoteSecretsFor(applicationLoginEntities.get(0).applicationLoginId, address);
        int size = secrets.size();
        int[] result =new int[size];
        for (int i = 0; i < size; i++){
            result[i] = secrets.get(i).identifier;
        }
        return result;
    }


    public int[] getAllLocalIdentifiers(String applicationName, String login){
        SecretDao           secretDao               = db.getSecretDao();
        ApplicationLoginDao applicationLoginDao     = db.getApplicationLoginDao();
        List<ApplicationLoginEntity> applicationLoginEntities = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, login);
        if (applicationLoginEntities.size()==0){
            throw new IllegalArgumentException();
        }
        long applicationLoginId = applicationLoginEntities.get(0).applicationLoginId;
        List<LocalSecretEntity> secretEntities = secretDao.getAllSecretsForApplicationLogin(applicationLoginId);
        int s = secretEntities.size();
        int[] result = new int[s];
        for (int i = 0; i < s; i++){
            result[i] = secretEntities.get(i).identifier;
        }
        return result;
    }

    public int[] getLocalIdentifiers(String applicationName, String login, int numberOfShares){
        int[] identifiers = new int[numberOfShares];
        SecretDao           secretDao               = db.getSecretDao();
        ApplicationLoginDao applicationLoginDao     = db.getApplicationLoginDao();
        List<ApplicationLoginEntity> applicationLoginEntities = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, login);
        if (applicationLoginEntities.size()==0){
            throw new IllegalArgumentException();
        }
        long applicationLoginId = applicationLoginEntities.get(0).applicationLoginId;
        List<LocalSecretEntity> secretEntities = secretDao.getAllSecretsForApplicationLogin(applicationLoginId);
        for (int i = 0; i < numberOfShares; i++){
            identifiers[i] = secretEntities.get(i).identifier;
        }
        return identifiers;
    }

    public int getNumberOfRemoteSecretsFor(String participant, String applicationName, String login){
        ApplicationLoginDao applicationLoginDao     = db.getApplicationLoginDao();
        RemoteDao           remoteDao               = db.getRemoteDao();
        List<ApplicationLoginEntity> applicationLoginEntities = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, login);
        List<RemoteSecretEntity>  secrets           =  remoteDao.getRemoteSecretsFor(applicationLoginEntities.get(0).applicationLoginId, participant);
        return secrets.size();
    }

    public int getThreshold(String applicationName, String login){
        ApplicationLoginDao applicationLoginDao     = db.getApplicationLoginDao();
        List<ApplicationLoginEntity> applicationLoginEntities = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, login);
        return applicationLoginEntities.get(0).threshold;
    }

    public int getNumberOfRemoteKeys(String applicationName, String login){
        ApplicationLoginParticipantDao   applicationLoginParticipantDao     = db.getApplicationLoginParticipantDao();
        RemoteDao                        remoteDao                          = db.getRemoteDao();


        List<ApplicationLoginParticipantJoin> entities = applicationLoginParticipantDao.getAllInformation(applicationName, login);
        int total = 0;
        for (ApplicationLoginParticipantJoin join : entities){
            List<RemoteSecretEntity>  secrets =  remoteDao.getRemoteSecretsFor(join.applicationLoginId, join.participantId);
            total += secrets.size();
        }
        return total;
    }


    public int getNumberOfRemoteParticipants(String applicationName, String login){
        ApplicationLoginParticipantDao   applicationLoginParticipantDao     = db.getApplicationLoginParticipantDao();
        List<ApplicationLoginParticipantJoin> entities = applicationLoginParticipantDao.getAllInformation(applicationName, login);
        return entities.size();
    }


}
