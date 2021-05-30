package com.project.collaborativeauthenticationapplication.alternative.key.user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.alternative.key.CustomKeyGenerationPresenter;
import com.project.collaborativeauthenticationapplication.alternative.key.KeyGenerationPresenter;


public class ModeSelectionFragment extends Fragment {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mode_selection, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText appEditor     = view.findViewById(R.id.edit_application_name);

        Button modeLeader = view.findViewById(R.id.button_leader_mode);
        final KeyGenerationPresenter instance = CustomKeyGenerationPresenter.getInstance();
        modeLeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.setMode(CustomKeyGenerationPresenter.MODE_LEADER);
                instance.switchToModel();

            }
        });


        Button modeFOllower = view.findViewById(R.id.button_follower_mode);
        modeFOllower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.setMode(CustomKeyGenerationPresenter.MODE_FOLLOWER);
                instance.switchToModel();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }



}