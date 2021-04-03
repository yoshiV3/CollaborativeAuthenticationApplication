package com.project.collaborativeauthenticationapplication.service.key.user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.service.key.CustomKeyGenerationPresenter;

public class GenerationFragment extends Fragment implements ProgressView {



    private CustomGenerationMessageListAdapter adapter;

    public GenerationFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generation, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView messagesView  =  view.findViewById(R.id.messages_view);
        adapter = new CustomGenerationMessageListAdapter();
        messagesView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesView.setAdapter(adapter);

        Button button =view.findViewById(R.id.button_run);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomKeyGenerationPresenter.getInstance().onRun();
                button.setVisibility(View.GONE);
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        CustomKeyGenerationPresenter presenter = (CustomKeyGenerationPresenter) CustomKeyGenerationPresenter.getInstance();
        if (presenter != null && presenter.isCurrentlyActive())
        {
            presenter.subscribe(this); //multiple times subscription won't change the effect
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        CustomKeyGenerationPresenter presenter = (CustomKeyGenerationPresenter) CustomKeyGenerationPresenter.getInstance();
        if (presenter !=  null)
        {
            presenter.unSubScribe(this);
        }
        super.onDestroy();
    }

    @Override
    public void onStop() {
        CustomKeyGenerationPresenter presenter = (CustomKeyGenerationPresenter) CustomKeyGenerationPresenter.getInstance();
        presenter.unSubScribe(this);
        super.onStop();
    }

    @Override
    public void pushNewMessage(String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.pushMessage(message);
            }
        });
    }
}