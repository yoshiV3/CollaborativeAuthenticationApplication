package com.project.collaborativeauthenticationapplication.service;

import android.content.Context;

public interface Client {
    int getState();

    void open(Context context);
    void close();
}
