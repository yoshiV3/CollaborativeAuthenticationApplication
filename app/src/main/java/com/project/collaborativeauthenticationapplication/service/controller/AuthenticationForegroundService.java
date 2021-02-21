package com.project.collaborativeauthenticationapplication.service.controller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.main.MainActivity;

public class AuthenticationForegroundService extends Service implements ServiceView{

    private static ServiceView  empty = new ServiceView() {
        @Override
        public void serviceActive() {

        }

        @Override
        public void serviceDisabled() {

        }

        @Override
        public void serviceSleeps() {

        }
    };

    private static ServiceView serviceView = empty;


    public static ServiceView getInstance()
    {
        return serviceView;
    }

    public static final String START_ACTION = "com.project.collaborativeauthenticationapplication.START";
    public static final String STOP_ACTION  = "com.project.collaborativeauthenticationapplication.STOP";

    private static final String COMPONENT_NAME         = "Service";
    private static final String ERROR                  = "Unsupported action";

    private static final String  CHANNEL_ID            = "AuthenticationService";


    private NotificationManager manager;


    private AuthenticationPresenter           presenter = CustomAuthenticationPresenter.getInstance();

    private AndroidBluetoothBroadcastReceiver receiver  = new AndroidBluetoothBroadcastReceiver();


    private final Logger logger                 = new AndroidLogger();

    public AuthenticationForegroundService() {
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        this.manager   =     getSystemService(NotificationManager.class);
        registerReceiver();
    }


    @Override
    public void onDestroy() {
        serviceView = empty;
        unregisterReceiver();
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
        {
            String action = intent.getAction();
            switch (action)
            {
                case START_ACTION:
                    presenter.onStartCommand();
                    startInForeground();
                    break;
                case STOP_ACTION:
                    presenter.onStopCommand();
                    stop();
                    break;
                default:
                    logger.logError(COMPONENT_NAME, ERROR, getString(R.string.PRIORITY_HIGH));
            }
        }
        return START_REDELIVER_INTENT;
    }






    private void startInForeground()
    {
        serviceView               = this;
        Notification notification = getAuthenticationServiceNotification();
        startForeground(R.integer.foregroundServiceStartCode, notification);
    }


    private void stop()
    {
        serviceView = empty;
        stopForeground(true);
        stopSelf();
    }





    @Override
    public void serviceActive() {
        Notification notification = getNotificationAutoCloseNoActivity(R.string.text_status_service_active);
        manager.notify(R.integer.serviceStatusNotification,notification);
    }

    @Override
    public void serviceDisabled() {
        Notification notification = getNotificationAutoCloseNoActivity(R.string.text_status_service_off);
        manager.notify(R.integer.serviceStatusNotification,notification);
    }

    @Override
    public void serviceSleeps() {
        Notification notification = getNotificationAutoCloseNoActivity(R.string.text_status_service_inactive);
        manager.notify(R.integer.serviceStatusNotification,notification);
    }







    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "ServiceChannel", NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(serviceChannel);
    }


    private Notification getNotification(PendingIntent pendingIntent, int notificationText)
    {
        createNotificationChannel();
        Notification resultNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(notificationText))
                .setContentIntent(pendingIntent)
                .build();
        return resultNotification;
    }

    private Notification getAuthenticationServiceNotification() {
        Intent notificationIntent   = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,0);
        return getNotification(pendingIntent, R.string.notification_text_for_foreground_service);
    }








    private Notification getNotificationAutoCloseNoActivity(int notificationText)
    {
        createNotificationChannel();
        Notification resultNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(notificationText))
                .setSmallIcon(R.drawable.ic_service_status_not)
                .setAutoCancel(true)
                .build();
        return resultNotification;
    }



    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(this.receiver, filter);
    }

    private void unregisterReceiver()
    {
        unregisterReceiver(this.receiver);
    }



}