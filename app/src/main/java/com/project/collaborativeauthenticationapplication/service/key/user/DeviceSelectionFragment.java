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
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.key.CustomKeyPresenter;
import com.project.collaborativeauthenticationapplication.service.key.KeyPresenter;

import java.util.ArrayList;


public class DeviceSelectionFragment extends Fragment {




    private RecyclerView options;
    private RecyclerView selections;
    
    public DeviceSelectionFragment() {
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
        return inflater.inflate(R.layout.fragment_device_selection, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        KeyPresenter presenter =  CustomKeyPresenter.getInstance();

        options    =  view.findViewById(R.id.recycler_options);
        selections = view.findViewById(R.id.recycler_selection);

        ArrayList<Participant> initialParticipants = new ArrayList<>(presenter.getInitialOptions());


        CustomOptionsRecyclerViewAdapter               optionAdapter =new CustomOptionsRecyclerViewAdapter(initialParticipants);
        CustomSelectedParticipantsRecyclerViewAdapter  selAdapter    =new CustomSelectedParticipantsRecyclerViewAdapter();

        View.OnClickListener listenOptionsAdapter = new  View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                int position = options.getChildLayoutPosition(v);
                Participant item  = optionAdapter.pop(position);
                selAdapter.add(item);
            }
        };


        View.OnClickListener listenSelAdapter = new  View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                int position = selections.getChildLayoutPosition(v);
                Participant item  = selAdapter.pop(position);
                optionAdapter.add(item);
            }
        };


        optionAdapter.setOnClickListener(listenOptionsAdapter);
        selAdapter.setOnClickListener(listenSelAdapter);

        options.setLayoutManager(new LinearLayoutManager(getContext()));
        selections.setLayoutManager(new LinearLayoutManager(getContext()));

        options.setAdapter(optionAdapter);
        selections.setAdapter(selAdapter);


        view.findViewById(R.id.button_submit_devices).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomKeyPresenter.getInstance().submitSelectedParticipants(selAdapter.getItems());
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
}