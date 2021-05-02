package com.project.collaborativeauthenticationapplication.service.signature.user;

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
import com.project.collaborativeauthenticationapplication.service.signature.CustomSignaturePresenter;
import com.project.collaborativeauthenticationapplication.service.signature.SignaturePresenter;

public class SignatureFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signature, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        CustomSignaturePresenter.getInstance().onPauseSignature();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SignaturePresenter presenter = CustomSignaturePresenter.getInstance();
        ((TextView)view.findViewById(R.id.textView_applicationName_signature)).setText(presenter.getApplicationName());
        ((TextView)view.findViewById(R.id.textView_login_signature)).setText(presenter.getLogin());
        view.findViewById(R.id.button_finish_signature).setVisibility(View.GONE);
        view.findViewById(R.id.button_finish_signature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onFinishSignature();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        CustomSignaturePresenter.getInstance().onComputeSignature(new Requester() {
            @Override
            public void signalJobDone() {
                Activity activity = getActivity();
                if (activity != null){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View view = getView();
                            view.findViewById(R.id.button_finish_signature).setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }
}