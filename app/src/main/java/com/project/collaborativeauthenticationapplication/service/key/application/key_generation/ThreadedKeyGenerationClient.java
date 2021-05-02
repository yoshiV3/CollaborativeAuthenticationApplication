package com.project.collaborativeauthenticationapplication.service.key.application.key_generation;


import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.KeyGenerationCoordinator;

import java.util.ArrayList;

public class ThreadedKeyGenerationClient implements KeyGenerationClient {

    private  final KeyGenerationClient client;
    public ThreadedKeyGenerationClient(KeyGenerationClient client){
        this.client = client;
    }
    @Override
    public int getState() {
        return client.getState();
    }

    @Override
    public int getWeight() {
        return client.getWeight();
    }

    @Override
    public int getIdentifier() {
        return client.getIdentifier();
    }

    @Override
    public void generateParts(KeyGenerationCoordinator coordinator) {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                client.generateParts(coordinator);
            }
        });
    }

    @Override
    public void receiveParts(ArrayList<BigNumber> parts, Point publicKey, KeyGenerationCoordinator coordinator) {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                client.receiveParts(parts, publicKey, coordinator);
            }
        });
    }

    @Override
    public void close(boolean success) {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                client.close(success);
            }
        });
    }

    @Override
    public void abort() {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                client.abort();
            }
        });
    }
}
