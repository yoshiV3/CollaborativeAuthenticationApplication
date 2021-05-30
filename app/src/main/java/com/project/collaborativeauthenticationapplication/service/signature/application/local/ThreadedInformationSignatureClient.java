package com.project.collaborativeauthenticationapplication.service.signature.application.local;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.signature.application.ThreadedSignatureClient;

import java.util.ArrayList;


public class ThreadedInformationSignatureClient extends ThreadedSignatureClient implements InformationSignatureClient{

    private InformationSignatureClient client;


    public ThreadedInformationSignatureClient(InformationSignatureClient client){
        super(client);
        this.client = client;
    }


    @Override
    public void checkInformationAboutCredential(String applicationName, DatabaseInformationRequester requester) {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                client.checkInformationAboutCredential(applicationName, requester);
            }
        });
    }

    @Override
    public void checkIfEnoughLocalShares(int numberOfShares, String applicationName, FeedbackRequester requester) {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                client.checkIfEnoughLocalShares(numberOfShares, applicationName, requester);
            }
        });
    }

    @Override
    public void calculateFinalSignature(ArrayList<BigNumber> parts,SignatureRequester requester) {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                client.calculateFinalSignature(parts, requester);
            }
        });
    }


    @Override
    public int getState() {
        return client.getState();
    }

    @Override
    public void open(Context context) {
        client.open(context);
    }

    @Override
    public void close() {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                client.close();
            }
        });
    }
}
