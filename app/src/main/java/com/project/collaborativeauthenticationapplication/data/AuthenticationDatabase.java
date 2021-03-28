package com.project.collaborativeauthenticationapplication.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;


@Database(entities = { ApplicationLoginEntity.class, ApplicationLoginParticipantJoin.class, ParticipantEntity.class, SecretEntity.class },
        version = 1)
public abstract class AuthenticationDatabase extends RoomDatabase {

    private static final String COMPONENT = "DATABASE";
    private static final String REQUEST   = "connection to database requested";
    private static final String NEW       = "new database connection";


    private static AuthenticationDatabase db = null;

    private static Logger logger = new AndroidLogger();


    public static void  createAuthenticationDatabaseConnection(Context context){
        db = Room.databaseBuilder(context,
                AuthenticationDatabase.class, "authentication").build();
        logger.logEvent(COMPONENT, NEW, "low" );
    }


    public static AuthenticationDatabase getAuthenticationDatabaseInstance(){
        logger.logEvent(COMPONENT, REQUEST, "low" );
        if (db == null){
            throw new IllegalStateException("No connection available");
        }
        return db;
    }



    public abstract ApplicationLoginDao            getApplicationLoginDao();
    public abstract ApplicationLoginParticipantDao getApplicationLoginParticipantDao();
    public abstract ParticipantDao                 getParticipantDao();
    public abstract SecretDao                      getSecretDao(); 

}
