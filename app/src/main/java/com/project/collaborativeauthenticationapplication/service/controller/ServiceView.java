package com.project.collaborativeauthenticationapplication.service.controller;

import android.content.Context;


public interface ServiceView {

     void serviceActive();
     void serviceDisabled();
     void serviceSleeps();

     Context getContext();

     void notify(String text);

}
