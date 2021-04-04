package com.project.collaborativeauthenticationapplication.service.key.user.key_generation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.service.key.CustomKeyGenerationPresenter;

public class FinishedFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_finished, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button button =view.findViewById(R.id.button_finished);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomKeyGenerationPresenter.getInstance().onJobDone();
                button.setVisibility(View.GONE);
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
}