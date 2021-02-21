package com.project.collaborativeauthenticationapplication.main;


import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.controller.Notifiable;
import com.project.collaborativeauthenticationapplication.service.controller.ServiceMonitor;

public class CustomServiceStatusPresenter  implements ServiceStatusPresenter, Notifiable {



    private final ServiceStatusView view;



    private boolean active = true;



    public CustomServiceStatusPresenter(ServiceStatusView view)
    {
        this.view = view;
        CustomServiceMonitor.getInstance().subscribeMe(this);
    }

    @Override
    public void serviceSleeps() {
        if (active)
        {
            view.showInactive();

        }
    }

    @Override
    public void serviceActive() {
        if (active) {
            view.showActive();
        }
    }

    @Override
    public void serviceDisabled() {
        if (active)
        {
            view.showDisabled();
        }
    }

    @Override
    public boolean isServiceEnabled() {
        return CustomServiceMonitor.getInstance().isServiceEnabled();
    }

    @Override
    public void stop() {
        CustomServiceMonitor.getInstance().unsubscribeMe(this);
        active = false;
    }




    @Override
    public void start() {
        ServiceMonitor monitor = CustomServiceMonitor.getInstance();
        if (monitor.isServiceEnabled())
        {
            if (monitor.isServiceActive())
            {
                view.showActive();
            }
            else
            {
                view.showInactive();
            }
        }
        else
        {
            view.showDisabled();
        }
    }
}
