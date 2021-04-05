package com.project.collaborativeauthenticationapplication.main;


import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.controller.Notifiable;
import com.project.collaborativeauthenticationapplication.service.controller.ServiceMonitor;

public class CustomServiceStatusPresenter  implements ServiceStatusPresenter, Notifiable {



    private final Logger logger              = new AndroidLogger();

    private final ServiceStatusView view;



    private boolean active = true;



    public CustomServiceStatusPresenter(ServiceStatusView view)
    {
        this.view = view;
        CustomServiceMonitor.getInstance().subscribeMe(this);
        logger.logEvent("Main status presenter", "presenter has subscribed", "low");
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
        logger.logEvent("Main status presenter", "presenter stopped", "low");
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
