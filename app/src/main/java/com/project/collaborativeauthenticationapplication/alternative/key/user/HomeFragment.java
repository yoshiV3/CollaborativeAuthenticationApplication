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


public class HomeFragment extends Fragment {




    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText appEditor     = view.findViewById(R.id.edit_application_name);

        Button submit = view.findViewById(R.id.button_submit_login_details);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newApplication = ((EditText) appEditor ).getText().toString();
                CustomKeyGenerationPresenter.getInstance().setMessage(DistributedKeyGenerationActivity.KEY_APPLICATION_NAME, newApplication);
                CustomKeyGenerationPresenter.getInstance().submitLoginDetails();
            }
        });



        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        KeyGenerationPresenter presenter = CustomKeyGenerationPresenter.getInstance();
        String application     = presenter.getMessage(DistributedKeyGenerationActivity.KEY_APPLICATION_NAME);

        EditText appEditor     = getView().findViewById(R.id.edit_application_name);

        appEditor.setText(application);

        presenter.openCoordinator();


        super.onStart();
    }
}