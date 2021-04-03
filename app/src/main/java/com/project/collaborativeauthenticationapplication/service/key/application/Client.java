package com.project.collaborativeauthenticationapplication.service.key.application;

import android.content.Context;

public interface Client {
    int getState();

    void open(Context context);
    void close();
}
