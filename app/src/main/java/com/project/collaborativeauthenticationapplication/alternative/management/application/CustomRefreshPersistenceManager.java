package com.project.collaborativeauthenticationapplication.alternative.management.application;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginParticipantDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginParticipantJoin;
import com.project.collaborativeauthenticationapplication.data.ParticipantEntity;
import com.project.collaborativeauthenticationapplication.data.RemoteSecretEntity;
import com.project.collaborativeauthenticationapplication.data.StoreCredentialDao;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.CustomKeyViewManager;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.persistance.CustomKeyGenerationPersistenceManager;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

public class CustomRefreshPersistenceManager extends CustomKeyViewManager {
    public void remove(String remove, String application) {

        ApplicationLoginParticipantDao applicationLoginParticipantDao = getDb().getApplicationLoginParticipantDao();
        List<ApplicationLoginParticipantJoin> joins = applicationLoginParticipantDao.getAllInformationFromParticipant(application, remove);

        for (ApplicationLoginParticipantJoin join : joins){
            applicationLoginParticipantDao.delete(join);
        }

       List<RemoteSecretEntity> list = getAllRemoteSecretsOf(remove, application);
       for(RemoteSecretEntity secret : list){
           getDb().getRemoteDao().delete(secret);
       }
    }

    public void persist(HashMap<String,int[]> participants, Point publicKey, String applicationName, int threshold, int newIdentifier) {
        StoreCredentialDao dao = getDb().getStoreDao();
        String login = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(publicKey.getX().getBigNumberAsByteArray());
            byte[] bytes = digest.digest(publicKey.getY().getBigNumberAsByteArray());
            // login = new String(digest.digest(publicKey.getY().getBigNumberAsByteArray()),StandardCharsets.ISO_8859_1);;
            StringBuilder hex = new StringBuilder(bytes.length*2);
            for(byte b : bytes){
                hex.append(String.format("%02x",b));
            }
            login = (new BigInteger(hex.toString(), 16)).toString(36).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        dao.storeCredentialData(participants, publicKey, login, applicationName, threshold, newIdentifier);
    }

    public void confirm(String applicationName){
        ApplicationLoginParticipantDao dao = getDb().getApplicationLoginParticipantDao();
        List<ApplicationLoginParticipantJoin> joins = dao.getAllInformation(applicationName);
        for (ApplicationLoginParticipantJoin join: joins){
            join.state = CustomKeyGenerationPersistenceManager.STATE_CONFIRMED;
            dao.updateJoin(join);
        }
    }

    public void persist(int newIdentifier, String applicationName, String address) {
        ApplicationLoginDao dao = getDb().getApplicationLoginDao();
        List<ApplicationLoginEntity> lists = dao.getApplicationsWithApplication(applicationName);
        getDb().getParticipantDao().insert(new ParticipantEntity(address));

        long applicationId = lists.get(0).applicationLoginId;
        ApplicationLoginParticipantJoin join = new ApplicationLoginParticipantJoin(applicationId, address, CustomKeyGenerationPersistenceManager.STATE_CONFIRMED);
        getDb().getApplicationLoginParticipantDao().insert(join);
        getDb().getRemoteDao().insert(new RemoteSecretEntity(applicationId, address, newIdentifier));
    }
}
