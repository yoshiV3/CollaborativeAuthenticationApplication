package com.project.collaborativeauthenticationapplication.service.general;

import android.content.Context;

public interface ServiceHandler {
    int getState();

    void open(Context context);
    void close();
}
