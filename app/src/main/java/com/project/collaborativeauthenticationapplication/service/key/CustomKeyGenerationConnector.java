package com.project.collaborativeauthenticationapplication.service.key;

public class CustomKeyGenerationConnector implements KeyGenerationConnector{


    private static KeyGenerationConnector instance = new CustomKeyGenerationConnector();


    public static KeyGenerationConnector getInstance()
    {
        return instance;
    }


    private CustomKeyGenerationConnector() {}
    @Override
    public void createAllConnections() {

    }
}
