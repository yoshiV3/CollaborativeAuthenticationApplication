package com.project.collaborativeauthenticationapplication.service.signature.user;

import android.os.Bundle;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.SecretOverviewFragment;
import com.project.collaborativeauthenticationapplication.service.signature.CustomSignaturePresenter;



public class SecretOverviewSignatureFragment extends SecretOverviewFragment {


    private static final String COMPONENT_NAME         = "overview fragment signature ";

    private final Logger logger              = new AndroidLogger();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_secret_overview_signature, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setOverview(view.findViewById(R.id.recycler_overview_credentials_signature));
        setAdapterManager(SignatureActivity.currentInstance);
        setAdapterPresenter(CustomSignaturePresenter.getInstance());
        super.onViewCreated(view, savedInstanceState);
    }
    @Override
    protected View.OnClickListener getOnClickListener() {
        return new  View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int position = getOverview().getChildLayoutPosition(v);
                logger.logEvent(COMPONENT_NAME, "request for signature", "low");
                ApplicationLoginEntity item  = getAdapterManager().getAdapter().getItemAt(position);
                CustomSignaturePresenter.getInstance().onCredentialSelectedForSignature("test", item.applicationName, item.login);
            }
        };
    }
}