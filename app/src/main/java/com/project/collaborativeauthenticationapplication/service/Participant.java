package com.project.collaborativeauthenticationapplication.service;

public interface Participant {


    String getName();
    String getAddress();

    void setWeight(int weight);
    int  getWeight();


    boolean isLocal();
}
