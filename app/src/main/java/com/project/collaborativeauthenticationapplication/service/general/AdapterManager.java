package com.project.collaborativeauthenticationapplication.service.general;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;

import java.util.List;

public interface AdapterManager {

    CustomOverviewSecretsAdapter getAdapter();

    void fillAdapter(List<ApplicationLoginEntity> items);
    void clearAdapter();
}
