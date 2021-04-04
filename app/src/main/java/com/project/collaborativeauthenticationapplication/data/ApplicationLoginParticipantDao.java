package com.project.collaborativeauthenticationapplication.data;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ApplicationLoginParticipantDao {
    @Insert
    void insert(ApplicationLoginParticipantJoin applicationLoginParticipantJoin);

    @Query("SELECT * FROM ParticipantEntity INNER JOIN ApplicationLoginParticipantJoin ON ParticipantEntity.address = ApplicationLoginParticipantJoin.participantId WHERE ApplicationLoginParticipantJoin.applicationLoginId =:applicationID")
    List<ParticipantEntity> getAllParticipantsForApplication(final long applicationID);



    @Query(
            "SELECT * FROM ApplicationLoginEntity INNER JOIN ApplicationLoginParticipantJoin ON ApplicationLoginEntity.applicationLoginId = ApplicationLoginParticipantJoin.applicationLoginId WHERE ApplicationLoginEntity.applicationName = :applicationName AND ApplicationLoginEntity.login = :login "
    )
    List<ApplicationLoginParticipantJoin> getAllInformation(String applicationName, String login);

    @Delete
    void delete(ApplicationLoginParticipantJoin applicationLoginParticipantJoin);
}