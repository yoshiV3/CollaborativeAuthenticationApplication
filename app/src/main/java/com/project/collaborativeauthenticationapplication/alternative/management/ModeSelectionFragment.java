package com.project.collaborativeauthenticationapplication.alternative.management;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.alternative.key.CustomKeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.alternative.key.KeyManagementPresenter;


public class ModeSelectionFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mode_selection_fragement, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Button modeLeader = view.findViewById(R.id.button_leader_mode_man);
        final KeyManagementPresenter instance = CustomKeyManagementPresenter.getInstance();
        modeLeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.makeLeader();
                instance.openCoordinator(getContext());
            }
        });


        Button modeFOllower = view.findViewById(R.id.button_follower_mode_man);
        modeFOllower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                instance.makeFollower();
                instance.openCoordinator(getContext());
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
}