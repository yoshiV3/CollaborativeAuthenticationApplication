package com.project.collaborativeauthenticationapplication.service.key.user.key_generation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.service.general.Participant;
import com.project.collaborativeauthenticationapplication.service.key.CustomKeyGenerationPresenter;
import com.project.collaborativeauthenticationapplication.service.key.KeyGenerationPresenter;

import java.util.ArrayList;


public class DeviceSelectionFragment extends Fragment {




    private RecyclerView options;
    private RecyclerView selections;
    private Spinner spinner;
    private TextView thresholdText;

    private ArrayAdapter<String>  spinnerAdapter;
    private CustomOptionsRecyclerViewAdapter optionAdapter;
    private CustomSelectedParticipantsRecyclerViewAdapter selAdapter;

    private int currentThreshold = 0;


    public DeviceSelectionFragment() { }
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
        KeyGenerationPresenter presenter =  CustomKeyGenerationPresenter.getInstance();

        options        = view.findViewById(R.id.recycler_options);
        selections     = view.findViewById(R.id.recycler_selection);
        spinner        = view.findViewById(R.id.threshold);
        thresholdText  = view.findViewById(R.id.selected_threshold);


        thresholdText.setText("");

        ArrayList<Participant> initialParticipants = new ArrayList<>(presenter.getInitialOptions());


        optionAdapter = new CustomOptionsRecyclerViewAdapter(initialParticipants);
        selAdapter    = new CustomSelectedParticipantsRecyclerViewAdapter();


        selAdapter.setWeightUpdater(() -> adaptThresholdSelection());

        View.OnClickListener listenOptionsAdapter = new  View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                int position = options.getChildLayoutPosition(v);
                Participant item  = optionAdapter.pop(position);
                selAdapter.add(item);
                adaptThresholdSelection();
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
                adaptThresholdSelection();
            }
        };


        optionAdapter.setOnClickListener(listenOptionsAdapter);
        selAdapter.setOnClickListener(listenSelAdapter);

        options.setLayoutManager(new LinearLayoutManager(getContext()));
        selections.setLayoutManager(new LinearLayoutManager(getContext()));

        options.setAdapter(optionAdapter);
        selections.setAdapter(selAdapter);


        spinnerAdapter =  new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int newThreshold = position + 1;
                currentThreshold = newThreshold;
                thresholdText.setText(String.valueOf(newThreshold));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });



        view.findViewById(R.id.button_submit_devices).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomKeyGenerationPresenter.getInstance().submitSelectedParticipants(selAdapter.getItems());
                CustomKeyGenerationPresenter.getInstance().submitThreshold(currentThreshold);
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    private void adaptThresholdSelection()
    {
        spinnerAdapter.clear();
        int totalWeight = selAdapter.getTotalWeight();
        for (int possibility = 1; possibility <= totalWeight; possibility++)
        {
            spinnerAdapter.add(String.valueOf(possibility));
        }
        spinnerAdapter.notifyDataSetChanged();
        if (totalWeight == 0)
        {
            currentThreshold = 0;
            thresholdText.setText("");
        }
        else
        {
            if (currentThreshold > totalWeight)
            {
                currentThreshold = totalWeight;
                thresholdText.setText(String.valueOf(currentThreshold));
            }
        }
    }
}