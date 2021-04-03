package com.project.collaborativeauthenticationapplication.service.key.user;

import android.content.Context;

public interface KeyGenerationView extends KeyView {


    void showMetaData(String login, String application);
    Context getContext();
}
