package com.project.collaborativeauthenticationapplication.service.key.user.key_management;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.service.key.user.KeyView;

import java.util.List;

public interface KeyManagementView extends KeyView {

     void fillAdapter(List<ApplicationLoginEntity> items);
     void clearAdapter();
}
