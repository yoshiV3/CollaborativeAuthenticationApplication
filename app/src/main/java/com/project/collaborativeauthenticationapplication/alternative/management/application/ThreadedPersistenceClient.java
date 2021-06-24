package com.project.collaborativeauthenticationapplication.alternative.management.application;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.alternative.management.PersistenceCoordinator;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class ThreadedPersistenceClient {

    private AndroidSecretStorage storage;

    private final PersistenceCoordinator coordinator;

    private CustomRefreshPersistenceManager manager = new CustomRefreshPersistenceManager();

    public ThreadedPersistenceClient(PersistenceCoordinator coordinator) {
        this.coordinator = coordinator;
    }


    public void open(Context context){
        storage =  new AndroidSecretStorage(context);
    }


    public List<String> getAllParticipants(String applicationName) {
        return manager.getAllRemoteParticipantsFor(applicationName);
    }

    public int getThresholdFor(String applicationName) {
        return manager.getThreshold(applicationName);
    }


    public List<String> getAllRemotes(String applicationName){
        return manager.getAllRemoteParticipantsFor(applicationName);
    }

    public int[] getIdentifiers(String applicationName, String device){
        int[] remoteIdentifiers = manager.getAllRemoteIdentifiers(applicationName, device);
        Arrays.sort(remoteIdentifiers);
        return remoteIdentifiers;
    }

    public int[] getLocalIdentifiers(String applicationName) {
        int[] allLocalIdentifiers = manager.getAllLocalIdentifiers(applicationName);
        Arrays.sort(allLocalIdentifiers);
        return allLocalIdentifiers;
    }

    public int getWeightFor(String applicationName) {
        return manager.getNumberOfLocalKeys(applicationName);
    }

    public int getNumberOfRemotes(String applicationName) {
        return manager.getNumberOfRemoteParticipants(applicationName);
    }

    public void persist(ArrayList<BigNumber> shares, String application, String remove) {
        int[] identifiers = getLocalIdentifiers(application);
        try {
            storage.storeSecrets(shares, identifiers, application);
            manager.remove(remove, application);
            coordinator.persisted();
        } catch (SecureStorageException e) {
            e.printStackTrace();
        }
    }


    public void persist(int newIdentifier, String applicationName, String address){
        manager.persist(newIdentifier, applicationName, address);
    }


    public void persist(HashMap<String, int[] > participants , Point publicKey, String applicationName,
                        int threshold, int newIdentifier, BigNumber share){
        try {
            storage.storeSecret(share, newIdentifier, applicationName);
            manager.persist(participants, publicKey, applicationName, threshold, newIdentifier);
            coordinator.persisted();
        } catch (SecureStorageException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<BigNumber> getAllLocalShares(String applicationName) {
        int[] identifiers = getLocalIdentifiers(applicationName);

        ArrayList<BigNumber> locals = new ArrayList<>();

        for (int identifier : identifiers){
            try {
                locals.add(storage.getSecrets(applicationName, identifier));
            } catch (SecureStorageException e) {
                e.printStackTrace();
            }
        }

        return locals;

    }

    public int getNumberOfTotalSecrets(String applicationName) {
        return manager.getNumberOfRemoteKeys(applicationName) + manager.getNumberOfLocalKeys(applicationName);
    }

    public Point getPublicKey(String applicationName) {
        return manager.getPublicKeyForCredential(applicationName);
    }

    public int getNumberOfSecretsFor(String applicationName, String device) {
        return manager.getNumberOfRemoteSecretsFor(device, applicationName);
    }

    public ArrayList<BigNumber> getAllLocalSecrets(String applicationName){
        ArrayList<BigNumber> result = new ArrayList<>();
        for(int identifier : getLocalIdentifiers(applicationName)){
            try {
                result.add(storage.getSecrets(applicationName, identifier));
            } catch (SecureStorageException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public void confirm(String applicationName) {
        manager.confirm(applicationName);
    }
}
