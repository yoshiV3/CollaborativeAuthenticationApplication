package com.project.collaborativeauthenticationapplication.logger;

import android.util.Log;

public class AndroidLogger implements Logger {

    @Override
    public void logEvent(String component, String event, String priority) {
        Log.i(getTag(component, priority), event);

    }

    @Override
    public void logEvent(String component, String event, String priority, String extraInformation) {
        String adaptEvent = event + "(" + extraInformation + ")";
        logEvent(component, adaptEvent, priority);
    }


    @Override
    public void logError(String component, String event, String priority) {

        Log.e(getTag(component, priority), event);

    }


    @Override
    public void logError(String component, String event, String priority, String extraInformation) {
        String adaptEvent = event + "(" + extraInformation + ")";
        logError(component, adaptEvent, priority);
    }


    private String getTag(String component, String priority) {
        return "CAM_" + component + "(" + priority +")";
    }



}
