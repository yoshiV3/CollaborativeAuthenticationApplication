package com.project.collaborativeauthenticationapplication.alternative.management.application;

import android.content.Context;


import com.project.collaborativeauthenticationapplication.alternative.key.KeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.alternative.management.PersistenceCoordinator;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class  RefreshCoordinator implements PersistenceCoordinator {

    private final KeyManagementPresenter presenter;

    private ThreadedPersistenceClient persistenceClient;


    private LocalRefreshClient localRefreshClient;

    private ArrayList<RefreshClient> clients = new ArrayList<>();


    public RefreshCoordinator(KeyManagementPresenter presenter)
    {
        this.presenter = presenter;
    }


    protected KeyManagementPresenter getPresenter() {
        return presenter;
    }


    private Logger logger = new AndroidLogger();

    public static final String COMPONENT = "refresh coordinator RF";


    public void open(Context context){
        persistenceClient = new  ThreadedPersistenceClient(this);
        persistenceClient.open(context);
    }

    public String getApplicationName(){
        return getPresenter().getApplicationName();
    }


    protected ArrayList<RefreshClient> getClients() {
        return clients;
    }

    public int getThreshold(){
       return  persistenceClient.getThresholdFor(getApplicationName());
    }

    public List<String> getAllDevices(){
        return persistenceClient.getAllRemotes(getApplicationName());
    }

    public int[] getIdentifiersFor(String device){
        return persistenceClient.getIdentifiers(getApplicationName(), device);
    }

    public int[] getLocalIdentifiers(){
        return persistenceClient.getLocalIdentifiers(getApplicationName());
    }



    public void start(){
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                logger.logEvent(COMPONENT, "start run", "high");
                runRefresh();
            }
        });
    }

    protected abstract void runRefresh();


    private String remove ;

    public String getRemove() {
        return remove;
    }

    public void setRemove(String remove) {
        this.remove = remove;
    }

    protected void buildAllClients(String remove){
        logger.logEvent(COMPONENT, "building clients", "high", remove);
        this.remove = remove;
        clients.clear();
        localRefreshClient = new LocalRefreshClient(this);
        List<String> list = persistenceClient.getAllParticipants(getPresenter().getApplicationName());
        for (String participant : list){
            logger.logEvent(COMPONENT, " potential clients", "high", participant);
            if (!participant.equals(remove)){
                clients.add(buildRemoteClient(participant, this));
            }
        }
        clients.add(localRefreshClient);
        logger.logEvent(COMPONENT, "start refresh", "high", String.valueOf(clients.size()));
        for (RefreshClient client : clients){
            client.refresh(remove);
        }
    }

    protected abstract RemoteRefreshClient buildRemoteClient(String device, RefreshCoordinator coordinator);

    public synchronized void passToLocal(ArrayList<BigNumber> share){
        logger.logEvent(COMPONENT, "passing to local", "low");
        localRefreshClient.receiveRefreshShares(share);
    }

    public  synchronized void distributeShares(HashMap<String, ArrayList<BigNumber>> refreshShares){
        logger.logEvent(COMPONENT, "distributing shares", "low");
        for(RefreshClient client : clients){
            client.receiveRefreshShares(refreshShares.get(client.getDevice()));
        }
    }

    public  int getWeight(){
        return persistenceClient.getWeightFor(getApplicationName());
    }

    public  int getNumberOfRemotes(){
        return persistenceClient.getNumberOfRemotes(getApplicationName());
    }

    public  void submitFinalRefreshedShares(ArrayList<BigNumber> shares){

        logger.logEvent(COMPONENT, "final shares", "low");
        persistenceClient.persist(shares, getApplicationName(), getRemove());
    };

    public abstract void persisted();

    public  ArrayList<BigNumber> getLocalShares(){
        return persistenceClient.getAllLocalShares(getApplicationName());
    }
}
