package com.project.collaborativeauthenticationapplication.debug;

public class Signal {


    private boolean isError;
    private String message;

    public Signal(String message, boolean isError)
    {
        this.message = message;
        this.isError = isError;
    }

    public boolean didSomethingGoWrong()
    {
        return isError;
    }

    public String getMessage() {
        return message;
    }
}
