package com.project.collaborativeauthenticationapplication;


import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginDao;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginParticipantJoin;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginParticipantDao;
import com.project.collaborativeauthenticationapplication.data.AuthenticationDatabase;
import com.project.collaborativeauthenticationapplication.data.ParticipantEntity;
import com.project.collaborativeauthenticationapplication.data.ParticipantDao;
import com.project.collaborativeauthenticationapplication.data.SecretEntity;
import com.project.collaborativeauthenticationapplication.data.SecretDao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DatabaseOnDeviceTest {

    private ApplicationLoginDao             applicationLoginDao;
    private ApplicationLoginParticipantDao  applicationLoginParticipantDao;
    private ParticipantDao                  participantDao;
    private SecretDao                       secretDao;
    private AuthenticationDatabase          db;



    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AuthenticationDatabase.class).build();
        applicationLoginDao             = db.getApplicationLoginDao();
        applicationLoginParticipantDao  = db.getApplicationLoginParticipantDao();
        participantDao                  = db.getParticipantDao();
        secretDao                       = db.getSecretDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }



    @Test
    public void testCreateApplicationLogin(){
        final String applicationName      = "name";
        final String applicationNameFalse = "nameFalse";
        final String login                = "login";
        final int    threshold            = 2;
        ApplicationLoginEntity applicationLoginEntity = new ApplicationLoginEntity(applicationName, login, threshold);
        applicationLoginDao.insert(applicationLoginEntity);

        List<ApplicationLoginEntity> applicationLoginEntities = applicationLoginDao.getApplications();
        List<ApplicationLoginEntity> applicationLoginsByNameEntity = applicationLoginDao.getApplicationsWithApplication(applicationName);
        List<ApplicationLoginEntity> applicationLoginsByNameFalseEntity = applicationLoginDao.getApplicationsWithApplication(applicationNameFalse);
        Assert.assertEquals(applicationLoginEntities.size(), 1);
        Assert.assertEquals(applicationLoginsByNameEntity.size(), 1);
        Assert.assertEquals(applicationLoginsByNameFalseEntity.size(), 0);
        Assert.assertEquals(applicationLoginEntities.get(0).applicationName, applicationName);
        Assert.assertEquals(applicationLoginEntities.get(0).login, login);
        Assert.assertEquals(applicationLoginEntities.get(0).threshold, threshold);
    }



    @Test
    public void testQueryApplicationLogin(){
        final String applicationName      = "name";
        final String applicationNameFalse = "nameFalse";
        final String login                = "login";
        final String loginFalse           = "user";
        final int    threshold            = 2;
        ApplicationLoginEntity applicationLoginEntity = new ApplicationLoginEntity(applicationName, login, threshold);
        applicationLoginDao.insert(applicationLoginEntity);


        List<ApplicationLoginEntity> trueApplicationTrueLoginEntities  = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, login);
        List<ApplicationLoginEntity> trueApplicationFalseLoginEntities = applicationLoginDao.getApplicationWithNameAndLogin(applicationName, loginFalse);
        List<ApplicationLoginEntity> falseApplicationTrueLoginEntities = applicationLoginDao.getApplicationWithNameAndLogin(applicationNameFalse, login);
        Assert.assertEquals(trueApplicationTrueLoginEntities.size(), 1);
        Assert.assertEquals(trueApplicationFalseLoginEntities.size(), 0);
        Assert.assertEquals(falseApplicationTrueLoginEntities.size(), 0);
    }


    @Test
    public void testCreateSecret(){
        final String applicationName = "name";
        final String login           = "login";
        final int    threshold       = 2;
        ApplicationLoginEntity applicationLoginEntity = new ApplicationLoginEntity(applicationName, login, threshold);
        applicationLoginDao.insert(applicationLoginEntity);
        List<ApplicationLoginEntity> applicationLoginEntities = applicationLoginDao.getApplications();

        SecretEntity secretEntity = new SecretEntity(applicationLoginEntities.get(0).applicationLoginId);
        secretDao.insert(secretEntity);

        List<SecretEntity> secretEntities = secretDao.getAllSecretsForApplicationLogin(applicationLoginEntities.get(0).applicationLoginId);
        Assert.assertEquals(secretEntities.size(), 1);
    }

    @Test
    public void testCreateParticipant(){
        final String address    = "abc";
        ParticipantEntity participantEntity =  new ParticipantEntity(address);
        participantDao.insert(participantEntity);

        List<ParticipantEntity> participantEntities = participantDao.getParticipantAt(address);
        Assert.assertEquals(participantEntities.size(), 1);
        Assert.assertEquals(participantEntities.get(0).address, address);
    }

    @Test
    public void testCreateApplicationLoginParticipant(){
        final String addressOne    = "abc";
        ParticipantEntity participantEntityOne =  new ParticipantEntity(addressOne);
        participantDao.insert(participantEntityOne);

        final String addressTwo    = "def";
        ParticipantEntity participantEntityTwo =  new ParticipantEntity(addressTwo);
        participantDao.insert(participantEntityTwo);

        final String applicationName = "name";
        final String login           = "login";
        final int    threshold       = 2;
        ApplicationLoginEntity applicationLoginEntity = new ApplicationLoginEntity(applicationName, login, threshold);
        applicationLoginDao.insert(applicationLoginEntity);

        List<ApplicationLoginEntity> applicationLoginEntities = applicationLoginDao.getApplications();

        ApplicationLoginParticipantJoin applicationLoginParticipantJoinOne = new ApplicationLoginParticipantJoin(applicationLoginEntities.get(0).applicationLoginId,
                                                                                                    addressOne, 4, 1
                                                                                                  );
        ApplicationLoginParticipantJoin applicationLoginParticipantJoinTwo = new ApplicationLoginParticipantJoin(applicationLoginEntities.get(0).applicationLoginId,
                                                                                    addressTwo, 4, 1
                                                                            );
        applicationLoginParticipantDao.insert(applicationLoginParticipantJoinOne);
        applicationLoginParticipantDao.insert(applicationLoginParticipantJoinTwo);

        List<ParticipantEntity> participantEntities = applicationLoginParticipantDao.getAllParticipantsForApplication(applicationLoginEntities.get(0).applicationLoginId);
        Assert.assertEquals(participantEntities.size(), 2);
    }


    @Test
    public void testDeleteApplicationLogin(){
        final String addressOne    = "abc";
        ParticipantEntity participantEntityOne =  new ParticipantEntity(addressOne);
        participantDao.insert(participantEntityOne);

        final String addressTwo    = "def";
        ParticipantEntity participantEntityTwo =  new ParticipantEntity(addressTwo);
        participantDao.insert(participantEntityTwo);

        final String applicationName = "name";
        final String login           = "login";
        final int    threshold       = 2;
        ApplicationLoginEntity applicationLoginEntity = new ApplicationLoginEntity(applicationName, login, threshold);
        applicationLoginDao.insert(applicationLoginEntity);

        List<ApplicationLoginEntity> applicationLoginEntities = applicationLoginDao.getApplications();

        ApplicationLoginParticipantJoin applicationLoginParticipantJoinOne = new ApplicationLoginParticipantJoin(applicationLoginEntities.get(0).applicationLoginId,
                addressOne, 4, 1
        );
        ApplicationLoginParticipantJoin applicationLoginParticipantJoinTwo = new ApplicationLoginParticipantJoin(applicationLoginEntities.get(0).applicationLoginId,
                addressTwo, 4, 1
        );
        applicationLoginParticipantDao.insert(applicationLoginParticipantJoinOne);
        applicationLoginParticipantDao.insert(applicationLoginParticipantJoinTwo);

        applicationLoginDao.delete(applicationLoginEntities.get(0));

        List<ParticipantEntity> participantEntities = applicationLoginParticipantDao.getAllParticipantsForApplication(applicationLoginEntities.get(0).applicationLoginId);
        Assert.assertEquals(participantEntities.size(), 0);

        applicationLoginEntities = applicationLoginDao.getApplications();
        Assert.assertEquals(applicationLoginEntities.size(), 0);

    }

}
