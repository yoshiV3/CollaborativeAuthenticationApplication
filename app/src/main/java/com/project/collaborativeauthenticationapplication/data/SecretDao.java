package com.project.collaborativeauthenticationapplication.data;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SecretDao {
    @Insert
    void insert(LocalSecretEntity... secretEntities);

    @Query("SELECT * FROM LocalSecretEntity WHERE applicationLoginId = :applicationLoginId")
    List<LocalSecretEntity> getAllSecretsForApplicationLogin(final long applicationLoginId);
}
