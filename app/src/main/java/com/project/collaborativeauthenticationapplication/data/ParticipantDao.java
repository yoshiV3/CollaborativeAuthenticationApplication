package com.project.collaborativeauthenticationapplication.data;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ParticipantDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ParticipantEntity... participantEntities);

    @Query("SELECT * FROM ParticipantEntity WHERE address = :address")
    List<ParticipantEntity> getParticipantAt(String address);
}
