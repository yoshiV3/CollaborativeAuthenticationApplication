package com.project.collaborativeauthenticationapplication.alternative.key.user;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.service.key.user.KeyView;

public interface KeyGenerationView extends KeyView {


    void showMetaData(String login, String application);

}
