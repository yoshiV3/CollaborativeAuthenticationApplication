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
import com.project.collaborativeauthenticationapplication.service.general.Requester;
import com.project.collaborativeauthenticationapplication.alternative.key.CustomKeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.alternative.key.KeyManagementPresenter;


public class CredentialManagementFragment extends Fragment {





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_credential_management, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        KeyManagementPresenter  presenter = CustomKeyManagementPresenter.getInstance();
        displayInformation(getView(), presenter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KeyManagementPresenter  presenter = CustomKeyManagementPresenter.getInstance();
        displayInformation(view, presenter);
        view.findViewById(R.id.button_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onRemove(new Requester() {
                    @Override
                    public void signalJobDone() {
                        presenter.onUpDate();
                        Activity activity = getActivity();
                        if (activity != null){
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    displayInformation(view, presenter);
                                }
                            });
                        }
                    }
                });
            }
        });

        view.findViewById(R.id.button_extend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onExtendSecret();
            }
        });

        view.findViewById(R.id.button_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onRefreshSecret();
            }
        });

    }

    private void displayInformation(@NonNull View view, KeyManagementPresenter presenter) {
        ((TextView) view.findViewById(R.id.textView_applicationName)).setText(presenter.retrieveMessage(CustomKeyManagementPresenter.KEY_APPLICATION_NAME));
        ((TextView) view.findViewById(R.id.textView_login)).setText(presenter.retrieveMessage(CustomKeyManagementPresenter.KEY_LOGIN));
        ((TextView) view.findViewById(R.id.textView_number_of_local_keys)).setText(presenter.retrieveMessage(CustomKeyManagementPresenter.KEY_NB_OF_LOC_KEYS));
        ((TextView) view.findViewById(R.id.textView_number_of_remote_devices)).setText(presenter.retrieveMessage(CustomKeyManagementPresenter.KEY_NB_OF_REM_DEV));
        ((TextView) view.findViewById(R.id.textView_number_of_remote_keys)).setText(presenter.retrieveMessage(CustomKeyManagementPresenter.KEY_NB_OF_REM_KEYS));
    }
}