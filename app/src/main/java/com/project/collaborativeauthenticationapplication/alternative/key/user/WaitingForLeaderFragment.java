package com.project.collaborativeauthenticationapplication.alternative.key.user;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.alternative.key.CustomKeyGenerationPresenter;
import com.project.collaborativeauthenticationapplication.alternative.key.KeyGenerationPresenter;


public class WaitingForLeaderFragment extends Fragment {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_waiting_for_leader, container, false);
    }


    @Override
    public void onStart() {

        KeyGenerationPresenter presenter = CustomKeyGenerationPresenter.getInstance();
        presenter.openCoordinator();

        super.onStart();
    }
}