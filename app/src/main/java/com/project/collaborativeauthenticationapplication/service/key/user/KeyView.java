package com.project.collaborativeauthenticationapplication.service.key.user;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.service.general.NavigationView;

public interface KeyView extends NavigationView {

    void onDone();
    void showTemporally(String text);
    Context getContext();


}
