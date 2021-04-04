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

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.service.key.CustomKeyManagementPresenter;


public class SecretOverviewFragment extends Fragment {


    private RecyclerView overview;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_secret_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        overview   = view.findViewById(R.id.recycler_overview_credentials);

        View.OnClickListener listenerAdapter = new  View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int position = overview.getChildLayoutPosition(v);
                ApplicationLoginEntity item  = KeyManagementActivity.currentInstance.getAdapter().getItemAt(position);
                CustomKeyManagementPresenter.getInstance().openManagementSessionFor(item.applicationName, item.login);
            }
        };
        KeyManagementActivity.currentInstance.getAdapter().setOnClickListener(listenerAdapter);
        overview.setLayoutManager(new LinearLayoutManager(getContext()));
        overview.setAdapter(KeyManagementActivity.currentInstance.getAdapter());
    }


    @Override
    public void onStart() {
        super.onStart();
        CustomKeyManagementPresenter.getInstance().onStart();
    }
}