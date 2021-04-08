package com.project.collaborativeauthenticationapplication.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RemoteDao {

    @Insert
    void insert(RemoteSecretEntity... secretEntities);

    @Query("SELECT * FROM REMOTESECRETENTITY WHERE applicationLoginId = :applicationId AND participantId = :participantId")
    List<RemoteSecretEntity> getRemoteSecretsFor(long applicationId, String participantId);
}
