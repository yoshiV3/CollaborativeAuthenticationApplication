package com.project.collaborativeauthenticationapplication.logger;

public interface Logger {

    void logEvent(String component, String event, String priority);
    void logEvent(String component, String event, String priority, String extraInformation);
    void logError(String component, String event, String priority);
    public void logError(String component, String event, String priority, String extraInformation);
}
