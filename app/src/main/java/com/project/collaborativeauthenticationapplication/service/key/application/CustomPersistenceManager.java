package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.data.AuthenticationDatabase;
import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;

import java.util.List;

public class CustomPersistenceManager extends CustomTokenConsumer<KeyToken>{


    KeyPartDistributionSession session;
    private final AuthenticationDatabase db;

    public CustomPersistenceManager() {
        db = AuthenticationDatabase.getAuthenticationDatabaseInstance();
    }

    public void receiveKeyDistributionSession(KeyPartDistributionSession session){
        this.session = session;
    }


    public boolean hasApplicationLoginWithGivenCredentials(String applicationName, String login){
        AuthenticationDatabase db            = AuthenticationDatabase.getAuthenticationDatabaseInstance();
        List<ApplicationLoginEntity> applicationLoginEntityList = db.getApplicationLoginDao().getApplicationWithNameAndLogin(applicationName, login);
        return applicationLoginEntityList.size() > 0;
    }

    public void persist(KeyToken token) throws IllegalUseOfClosedTokenException {
        consumeToken(token);
        if (session == null){
            throw new IllegalStateException("Insufficient amount of information to properly persist the data");
        }
        ApplicationLoginEntity application   = new ApplicationLoginEntity(session.getApplicationName(), session.getLogin(), session.getThreshold());
        ApplicationLoginDao    doa           = db.getApplicationLoginDao();
        doa.insert(application);
    }

    public void removeCredentials(String applicationName, String login){
        ApplicationLoginDao applicationLoginDao = db.getApplicationLoginDao();
        List<ApplicationLoginEntity> entities = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, login);
        for (ApplicationLoginEntity entity : entities){
            applicationLoginDao.delete(entity);
        }
    }
}
