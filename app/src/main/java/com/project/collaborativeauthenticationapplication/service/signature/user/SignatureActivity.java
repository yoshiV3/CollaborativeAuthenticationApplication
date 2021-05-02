package com.project.collaborativeauthenticationapplication.service.signature.user;

import android.content.Context;
import android.os.Bundle;


import com.project.collaborativeauthenticationapplication.NavigationEnabledAuthenticationControllerActivity;
import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.service.general.CustomOverviewSecretsAdapter;
import com.project.collaborativeauthenticationapplication.service.signature.CustomSignaturePresenter;
import com.project.collaborativeauthenticationapplication.service.signature.SignaturePresenter;


import java.util.List;

public class SignatureActivity extends NavigationEnabledAuthenticationControllerActivity implements SignatureView {


    public static SignatureActivity currentInstance;

    private CustomOverviewSecretsAdapter adapter;

    private SignaturePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        buildNavigator(R.id.fragment3);

        CustomSignaturePresenter.newInstance(this);
        presenter        = CustomSignaturePresenter.getInstance();
        currentInstance  = this;
        adapter          = new CustomOverviewSecretsAdapter();
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
        currentInstance = null;
        super.onDestroy();
    }


    @Override
    public void onDone() {
        finish();
    }

    @Override
    public Context getContext() {
        return this;
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
    public CustomOverviewSecretsAdapter getAdapter() {
        return adapter;
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