package com.project.collaborativeauthenticationapplication.alternative.management.application;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

import java.util.ArrayList;

public abstract class RefreshClient {

    private final RefreshCoordinator coordinator;



    private Logger logger = new AndroidLogger();

    public static final String COMPONENT = " refresh client RF";


    protected RefreshClient(RefreshCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    public void refresh(String remove) {
        logger.logEvent(COMPONENT, "refresh", "low");
        this.remove = remove;
        ThreadPoolSupplier.getSupplier().execute(getRefreshCode());
    }

    private String remove;

    protected String getRemove() {
        return remove;
    }

    protected  abstract Runnable getRefreshCode();

    public String getDevice(){
        return "here";
    }

    protected RefreshCoordinator getCoordinator() {
        return coordinator;
    }

    public abstract void receiveRefreshShares(ArrayList<BigNumber> bigNumbers);

    public abstract void close();
}
