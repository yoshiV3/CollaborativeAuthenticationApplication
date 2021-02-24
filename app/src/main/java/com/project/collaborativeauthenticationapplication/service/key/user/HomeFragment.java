package com.project.collaborativeauthenticationapplication.service.key.user;

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
import com.project.collaborativeauthenticationapplication.service.key.CustomKeyPresenter;
import com.project.collaborativeauthenticationapplication.service.key.KeyPresenter;


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
        EditText loginEditor   = view.findViewById(R.id.edit_login);
        EditText appEditor     = view.findViewById(R.id.edit_application_name);

        loginEditor.setOnFocusChangeListener( new View.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    String newLogin = ((EditText) v).getText().toString();
                    CustomKeyPresenter.getInstance().setMessage(DistributedKeyGenerationActivity.KEY_LOGIN, newLogin);
                }
            }
        });



        appEditor.setOnFocusChangeListener( new View.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    String newApplication = ((EditText) v).getText().toString();
                    CustomKeyPresenter.getInstance().setMessage(DistributedKeyGenerationActivity.KEY_APPLICATION_NAME, newApplication);
                }
            }
        });


        Button submit = view.findViewById(R.id.button_submit_login_details);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomKeyPresenter.getInstance().submitLoginDetails();
            }
        });



        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        KeyPresenter presenter = CustomKeyPresenter.getInstance();
        String login           = presenter.getMessage(DistributedKeyGenerationActivity.KEY_LOGIN);
        String application     = presenter.getMessage(DistributedKeyGenerationActivity.KEY_APPLICATION_NAME);

        EditText loginEditor   = getView().findViewById(R.id.edit_login);
        EditText appEditor     = getView().findViewById(R.id.edit_application_name);

        loginEditor.setText(login);
        appEditor.setText(application);


        super.onStart();
    }
}