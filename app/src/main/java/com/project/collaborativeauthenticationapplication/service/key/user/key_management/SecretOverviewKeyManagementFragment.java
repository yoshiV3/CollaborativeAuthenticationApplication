package com.project.collaborativeauthenticationapplication.service.key.user.key_management;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.service.general.SecretOverviewFragment;
import com.project.collaborativeauthenticationapplication.service.key.CustomKeyManagementPresenter;


public class SecretOverviewKeyManagementFragment extends SecretOverviewFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_secret_overview_key_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setOverview(view.findViewById(R.id.recycler_overview_credentials));
        setAdapterManager(KeyManagementActivity.currentInstance);
        setAdapterPresenter(CustomKeyManagementPresenter.getInstance());
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
                ApplicationLoginEntity item  = getAdapterManager().getAdapter().getItemAt(position);
                CustomKeyManagementPresenter.getInstance().openManagementSessionFor(item.applicationName, item.login);
            }
        };
    }
}