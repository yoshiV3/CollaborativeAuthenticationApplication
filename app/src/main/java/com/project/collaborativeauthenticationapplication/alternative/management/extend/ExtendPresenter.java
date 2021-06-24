package com.project.collaborativeauthenticationapplication.alternative.management.extend;

import android.content.Context;

public class ExtendPresenter {


    private final ReceiveNewShareActivity activity;

    private ExtendCoordinator coordinator;

    public ExtendPresenter(ReceiveNewShareActivity activity) {
        this.activity = activity;
    }

    public void open(Context context){
        coordinator = new ExtendCoordinator(this);
        coordinator.open(context);

    }

    public void foundLeader(String applicationName) {
        activity.makeButtonVisible();
        activity.displayApplicationName(applicationName);
    }

    public void continueWithOperation() {
        coordinator.continueWithOperation();
    }

    public void finish() {
        activity.onDone();
    }
}
