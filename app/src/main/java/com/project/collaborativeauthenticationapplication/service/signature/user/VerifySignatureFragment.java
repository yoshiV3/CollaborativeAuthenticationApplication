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
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
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
        View button = view.findViewById(R.id.button_finish_verify);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomSignaturePresenter.getInstance().onFinishVerification();
            }
        });
        button.setVisibility(View.GONE);
        SignaturePresenter presenter = CustomSignaturePresenter.getInstance();
        ((TextView)view.findViewById(R.id.textView_applicationName_verify)).setText(presenter.getApplicationName());
    }

    @Override
    public void onStart() {
        super.onStart();
        SignaturePresenter presenter = CustomSignaturePresenter.getInstance();
        presenter.onVerify(new FeedbackRequester() {
            boolean result;
            @Override
            public void setResult(boolean result) {
                this.result = result;
            }

            @Override
            public void signalJobDone() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View button = getView().findViewById(R.id.button_finish_verify);
                        button.setVisibility(View.GONE);
                        if(result){
                            ((TextView)getView().findViewById(R.id.textView_result_verification)).setText("Signature successfully verified");
                        }
                        else {
                            ((TextView)getView().findViewById(R.id.textView_result_verification)).setText("Signature failed to verify");
                        }
                    }
                });
            }
        });
    }
}