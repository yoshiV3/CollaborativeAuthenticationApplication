package com.project.collaborativeauthenticationapplication.data;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SecretDao {
    @Insert
    void insert(SecretEntity... secretEntities);

    @Query("SELECT * FROM SecretEntity WHERE applicationLoginId = :applicationLoginId")
    List<SecretEntity> getAllSecretsForApplicationLogin(final long applicationLoginId);
}
