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


public class ExtendFragment extends Fragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_extend, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KeyManagementPresenter instance = CustomKeyManagementPresenter.getInstance();
        //instance.openCoordinator(getContext());


        Button button = getView().findViewById(R.id.button_extend_now);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.startExtend();
                button.setVisibility(View.GONE);
            }
        });

    }


}