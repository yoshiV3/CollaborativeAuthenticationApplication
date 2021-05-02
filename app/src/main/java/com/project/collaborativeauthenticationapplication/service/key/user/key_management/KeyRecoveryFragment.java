package com.project.collaborativeauthenticationapplication.service.key.user.key_management;


import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.general.Requester;
import com.project.collaborativeauthenticationapplication.service.key.CustomKeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.service.key.KeyManagementPresenter;


public class KeyRecoveryFragment extends Fragment {


    private Logger logger = new AndroidLogger();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_key_recovery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button_finish_recovery).setVisibility(View.GONE);
        view.findViewById(R.id.button_finish_recovery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomKeyManagementPresenter.getInstance().onFinishedRecovery();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        logger.logEvent("Key recovery fragment", "start recovery", "low");
        KeyManagementPresenter presenter = CustomKeyManagementPresenter.getInstance();
        presenter.onExtend(new Requester() {
            @Override
            public void signalJobDone() {
                presenter.onUpDate();
                Activity activity = getActivity();
                if (activity != null){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View view = getView();
                            view.findViewById(R.id.button_finish_recovery).setVisibility(View.VISIBLE);
                            ((TextView) view.findViewById(R.id.textView_recovery)).setText("Complete");
                        }
                    });
                }
                else {
                    logger.logEvent("Key recovery fragment", "null activity", "medium");
                }
            }
        });
    }
}