package com.project.collaborativeauthenticationapplication.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Transaction;


import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.persistance.CustomKeyGenerationPersistenceManager;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSession;

import java.nio.charset.StandardCharsets;

@Dao
public abstract class StoreCredentialDao {


    private boolean success;

    @Insert(onConflict = OnConflictStrategy.FAIL)
    public abstract long insert(ApplicationLoginEntity applicationLoginEntity);

    @Insert(onConflict = OnConflictStrategy.FAIL)
    public abstract void insert(ApplicationLoginParticipantJoin applicationLoginParticipantJoin);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(ParticipantEntity... participantEntities);

    @Insert(onConflict = OnConflictStrategy.FAIL)
    public abstract  void insert(RemoteSecretEntity secretEntity);

    @Insert(onConflict = OnConflictStrategy.FAIL)
    public abstract void insert(LocalSecretEntity secretEntity);





    public boolean getSuccess(){
        boolean suc = success;
        return suc;
    }


    @Transaction
    public void storeCredentialData(int[] identifiers , Point publicKey, KeyGenerationSession session, String login){
        success = false;
        String publicKeyX = new String(publicKey.getX().getBigNumberAsByteArray(), StandardCharsets.ISO_8859_1);
        String publicKeyY = new String(publicKey.getY().getBigNumberAsByteArray(), StandardCharsets.ISO_8859_1);
        ApplicationLoginEntity application             = new ApplicationLoginEntity(session.getApplicationName(), login, session.getThreshold(),
                publicKeyX, publicKeyY, publicKey.isZero());

        long applicationId = insert(application);

        for (int i: identifiers){
            LocalSecretEntity s = new LocalSecretEntity(applicationId, i);
            insert(s);
        }

        for(IdentifiedParticipant remote: session.getRemoteParticipantList()){
            insert(new ParticipantEntity(remote.getAddress()));
            ApplicationLoginParticipantJoin join = new ApplicationLoginParticipantJoin(applicationId, remote.getAddress(), CustomKeyGenerationPersistenceManager.STATE_LOCAL_PERSISTENCE);
            insert(join);
            for (int i = 0; i < remote.getWeight(); i++){
                int identifier = remote.getIdentifier()+ i;
                insert(new RemoteSecretEntity(applicationId, remote.getAddress(), identifier));
            }
        }
        success = true;
    }
}
































