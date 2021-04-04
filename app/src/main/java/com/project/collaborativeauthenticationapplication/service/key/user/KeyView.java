package com.project.collaborativeauthenticationapplication.service.key.user;

import android.content.Context;

public interface KeyView {

    void onDone();
    void navigate(int target);
    int  locate();
    void showTemporally(String text);
    Context getContext();


}
