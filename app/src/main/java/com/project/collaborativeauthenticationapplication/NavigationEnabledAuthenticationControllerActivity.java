package com.project.collaborativeauthenticationapplication;


import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.project.collaborativeauthenticationapplication.service.Navigator;

public abstract class NavigationEnabledAuthenticationControllerActivity extends  CustomAuthenticationControllerActivity{

    private Navigator navigator;



    protected void buildNavigator(int fragmentId){
        NavController controller = Navigation.findNavController(this, fragmentId);
        navigator = new Navigator() {
            final NavController  androidNavigator = controller;
            @Override
            public void navigate(int target) {
                androidNavigator.navigate(target);
            }

            @Override
            public int getLocation() {
                return controller.getCurrentDestination().getId();
            }

        };
    }


    public Navigator getNavigator(){
        return navigator;
    }
}
