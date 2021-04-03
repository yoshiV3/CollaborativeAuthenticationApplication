package com.project.collaborativeauthenticationapplication.service.key.user;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;

import java.util.List;

public interface KeyManagementView extends KeyView{

     void fillAdapter(List<ApplicationLoginEntity> items);
     void clearAdapter();
}
