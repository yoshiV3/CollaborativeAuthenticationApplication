package com.project.collaborativeauthenticationapplication.data;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ApplicationLoginDao {

    @Insert
    void insert(ApplicationLoginEntity applicationLoginEntity);

    @Delete
    void delete(ApplicationLoginEntity... applicationLoginEntities);

    @Query("SELECT * FROM ApplicationLoginEntity")
    List<ApplicationLoginEntity> getApplications();

    @Query("SELECT * FROM ApplicationLoginEntity WHERE applicationName =:applicationName")
    List<ApplicationLoginEntity> getApplicationsWithApplication(String applicationName);

    @Query("SELECT * FROM  ApplicationLoginEntity WHERE applicationName = :applicationName AND login= :login")
    List<ApplicationLoginEntity> getApplicationWithNameAndLogin(String applicationName, String login);
}
