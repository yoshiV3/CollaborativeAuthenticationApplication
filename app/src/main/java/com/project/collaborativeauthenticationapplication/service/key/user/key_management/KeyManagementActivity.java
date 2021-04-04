package com.project.collaborativeauthenticationapplication.service.key.user.key_management;



import android.content.Context;
import android.os.Bundle;


import com.project.collaborativeauthenticationapplication.NavigationEnabledAuthenticationControllerActivity;
import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.service.key.CustomKeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.service.key.KeyManagementPresenter;

import java.util.List;

public class KeyManagementActivity extends NavigationEnabledAuthenticationControllerActivity implements KeyManagementView {


    public static KeyManagementActivity currentInstance;

    private CustomOverviewSecretsAdapter adapter;

    private KeyManagementPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_management);
        buildNavigator(R.id.fragment2);
        CustomKeyManagementPresenter.newInstance(this);
        presenter = CustomKeyManagementPresenter.getInstance();
        currentInstance = this;
        adapter = new CustomOverviewSecretsAdapter();
    }

    public CustomOverviewSecretsAdapter getAdapter() {
        return adapter;
    }

    @Override
    protected void onStart() {
        presenter.onStart();
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        presenter.onBackPressed();
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        presenter.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        presenter.onStop();
        super.onDestroy();
    }

    @Override
    public void onDone() {
        finish();
    }

    @Override
    public void navigate(int target) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getNavigator().navigate(target);
            }
        });
    }

    @Override
    public int locate() {
        return getNavigator().getLocation();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void fillAdapter(List<ApplicationLoginEntity> items) {
        if (adapter != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.fillData(items);
                }
            });
        }
    }

    @Override
    public void clearAdapter() {
        if (adapter != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.clear();
                }
            });
        }
    }
}