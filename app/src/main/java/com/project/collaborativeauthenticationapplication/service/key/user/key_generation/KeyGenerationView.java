package com.project.collaborativeauthenticationapplication.service.key.user.key_generation;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.service.key.user.KeyView;

public interface KeyGenerationView extends KeyView {


    void showMetaData(String login, String application);

}
