package com.project.collaborativeauthenticationapplication.service.controller;

import androidx.annotation.Nullable;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;

import java.util.ArrayList;

public abstract class CustomToken {



    private static final String COMPONENT_NAME         =  "Token";

    private static final String PRIORITY               = "low";

    private static final String EVENT_CLOSE            = "Token closed";

    private static final int MAX_UNIQUE_TOKENS = 250;

    private  static int currentIdentifier = 1;


    private Logger logger = new AndroidLogger();

    private static ArrayList<CustomToken> activeTokens = new ArrayList<>();


    protected static void closeAll()
    {
        for (CustomToken token:  (ArrayList<CustomToken>)activeTokens.clone())
        {
            token.close();
        }
    }


    private boolean closed = false;


    private   static int getNewIdentifier()
    {
        int newIdentifier = currentIdentifier;
        currentIdentifier = (currentIdentifier +1%MAX_UNIQUE_TOKENS)+1;
        return newIdentifier;
    }



    public static int getNumberOfActiveTokens()
    {
        return activeTokens.size();
    }


    private int identifier;


    public CustomToken()
    {
        identifier  = getNewIdentifier();
        activeTokens.add(this);
    }


    public int getIdentifier()
    {
        return identifier;
    }


    public void  close()
    {
        logger.logEvent(COMPONENT_NAME, EVENT_CLOSE, PRIORITY, String.valueOf(getIdentifier()) );
        closed = true;
        activeTokens.remove(this);
    }

    public boolean isClosed()
    {
        return closed;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (! (obj instanceof CustomToken) )
        {
            return false;
        }
        CustomToken other = (CustomToken) obj;
        return other.getIdentifier() == getIdentifier();
    }
}
