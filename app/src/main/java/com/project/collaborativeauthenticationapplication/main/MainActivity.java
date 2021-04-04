package com.project.collaborativeauthenticationapplication.main;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.project.collaborativeauthenticationapplication.CustomAuthenticationControllerActivity;
import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.data.AuthenticationDatabase;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.controller.AuthenticationForegroundService;
import com.project.collaborativeauthenticationapplication.service.key.user.DistributedKeyGenerationActivity;
import com.project.collaborativeauthenticationapplication.service.key.user.KeyManagementActivity;


public class MainActivity extends CustomAuthenticationControllerActivity implements MainMenuView, ServiceStatusView{


    private static final String COMPONENT_NAME         = "Main";

    private static final String EVENT_ENABLE_BLUETOOTH = "Bluetooth requested";
    private static final String EVENT_ENABLE_SERVICE   = "Service requested";
    private static final String EVENT_DISABLE_SERVICE  = "service disabled";

    private static final String EVENT_ACTIVE_SERVICE    = "Service active";
    private static final String EVENT_INACTIVE_SERVICE  = "Service inactive";
    private static final String EVENT_DISABLED_SERVICE  = "service disabled";



    private final MainMenuPresenter        menuPresenter       = new CustomMainMenuPresenter();
    private final ServiceStatusPresenter   statusPresenter     = new CustomServiceStatusPresenter(this);
    private final Logger                   logger              = new AndroidLogger();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AuthenticationDatabase.createAuthenticationDatabaseConnection(this);
    }

    @Override
    protected void onResume() {
        statusPresenter.start();
        super.onResume();
    }

    @Override
    protected void onStart() {
        statusPresenter.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        statusPresenter.stop();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public void onClickKeyGeneration(View view)
    {
        logger.logEvent(COMPONENT_NAME, "clicked button", getString(R.string.PRIORITY_LOW));
        if (statusPresenter.isServiceEnabled()) {
            try {
                Intent intent = new Intent(this, DistributedKeyGenerationActivity.class);
                intent.putExtra(DistributedKeyGenerationActivity.APPLICATION_NAME_FIELD_KEY, "Authentication Manager");
                intent.putExtra(DistributedKeyGenerationActivity.LOGIN_NAME_FIELD_KEY, "Login");
                int requestCode = getResources().getInteger(R.integer.keyGenerationRequestCode);
                startActivityForResult(intent, requestCode);
            } catch (Exception e) {
                logger.logError(COMPONENT_NAME, e.getMessage(), getString(R.string.PRIORITY_CRITICAL));
            }
        }
        else
        {
            logger.logEvent(COMPONENT_NAME, "Service disabled so no generation of keys", getString(R.string.PRIORITY_LOW));
            showTemporally("Service is disabled");
        }

    }

    public void onClickKeyManagement(View view){
        logger.logEvent(COMPONENT_NAME, "clicked button", getString(R.string.PRIORITY_LOW));
        if (statusPresenter.isServiceEnabled()) {
            try {
                Intent intent = new Intent(this, KeyManagementActivity.class);
                int requestCode = getResources().getInteger(R.integer.keyManagementRequestCode);
                startActivityForResult(intent, requestCode);
            } catch (Exception e) {
                logger.logError(COMPONENT_NAME, e.getMessage(), getString(R.string.PRIORITY_CRITICAL));
            }
        }
        else
        {
            logger.logEvent(COMPONENT_NAME, "Service disabled so no generation of keys", getString(R.string.PRIORITY_LOW));
            showTemporally("Service is disabled");
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem disableService      = menu.findItem(R.id.menu_option_authentication_stop);
        MenuItem enableService       = menu.findItem(R.id.menu_option_authentication_start);
        MenuItem enableBluetooth     = menu.findItem(R.id.menu_option_enable_bluetooth);

        disableService.setVisible(menuPresenter.getVisibilityDisableService());
        enableService.setVisible(menuPresenter.getVisibilityEnableService());
        enableBluetooth.setVisible(menuPresenter.getVisibilityEnableBluetooth());

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId)
        {
            case R.id.menu_option_authentication_start:
                logger.logEvent(COMPONENT_NAME, EVENT_ENABLE_SERVICE, getString(R.string.PRIORITY_NORMAL));
                onActionOnService(AuthenticationForegroundService.START_ACTION);
                break;
            case R.id.menu_option_authentication_stop:
                logger.logEvent(COMPONENT_NAME, EVENT_DISABLE_SERVICE, getString(R.string.PRIORITY_NORMAL));
                onActionOnService(AuthenticationForegroundService.STOP_ACTION);
                break;
            case R.id.menu_option_enable_bluetooth:
                logger.logEvent(COMPONENT_NAME, EVENT_ENABLE_BLUETOOTH, getString(R.string.PRIORITY_NORMAL));
                onEnableBluetooth();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void showActive() {
        showStatus(getString(R.string.text_status_service_active));
        logger.logEvent(COMPONENT_NAME, EVENT_ACTIVE_SERVICE, getString(R.string.PRIORITY_LOW));
    }

    @Override
    public void showInactive() {
        showStatus(getString(R.string.text_status_service_inactive));
        logger.logEvent(COMPONENT_NAME, EVENT_INACTIVE_SERVICE, getString(R.string.PRIORITY_LOW));
    }

    @Override
    public void showDisabled() {
        showStatus(getString(R.string.text_status_service_off));
        logger.logEvent(COMPONENT_NAME, EVENT_DISABLED_SERVICE, getString(R.string.PRIORITY_LOW));
    }




    private void showStatus(String status) {
        TextView view = findViewById(R.id.textView_main_status_service);
        view.setText(status);
    }

    private void onActionOnService(String action)
    {
        Intent intent = new Intent(this, AuthenticationForegroundService.class);
        intent.setAction(action);
        startForegroundService(intent);
    }
}