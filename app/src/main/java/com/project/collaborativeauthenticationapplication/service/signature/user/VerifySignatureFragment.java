package com.project.collaborativeauthenticationapplication.service.signature.user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.service.signature.CustomSignaturePresenter;
import com.project.collaborativeauthenticationapplication.service.signature.SignaturePresenter;


public class VerifySignatureFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verify_signature, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button_finish_verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomSignaturePresenter.getInstance().onFinishVerification();
            }
        });
        SignaturePresenter presenter = CustomSignaturePresenter.getInstance();
        ((TextView)view.findViewById(R.id.textView_applicationName_verify)).setText(presenter.getApplicationName());
        ((TextView)view.findViewById(R.id.textView_login_verify)).setText(presenter.getLogin());
        ((TextView)view.findViewById(R.id.textView_result_verification)).setText("Signature successfully verified");
    }
}