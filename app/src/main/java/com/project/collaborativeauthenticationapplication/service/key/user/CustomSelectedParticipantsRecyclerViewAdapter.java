package com.project.collaborativeauthenticationapplication.service.key.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.collaborativeauthenticationapplication.PopListAdapter;
import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.service.Participant;

import java.util.ArrayList;

public class CustomSelectedParticipantsRecyclerViewAdapter extends PopListAdapter<CustomSelectedParticipantsRecyclerViewAdapter.ViewHolder, Participant> {


    public CustomSelectedParticipantsRecyclerViewAdapter() {
        super(new ArrayList<Participant>());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected, parent, false);
        view.setOnClickListener(getListener());
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int viewHolderPosition = position;

        Participant participant = getItemAt(position);

        holder.getTextViewName().setText(participant.getName());


        int weight              = participant.getWeight();


        Spinner spinner = holder.getSpinner();
        spinner.setSelection(weight-1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int newWeight = position + 1;
                holder.getTextViewWeight().setText(String.valueOf(newWeight));
                participant.setWeight(newWeight);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        holder.getTextViewWeight().setText(String.valueOf(weight));

    }



    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private  final TextView textViewName;
        private  final TextView textViewWeight;
        private  final Spinner spinner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.textViewName          = (TextView) itemView.findViewById(R.id.selected_name);
            this.spinner           = (Spinner)  itemView.findViewById(R.id.weights);
            this.textViewWeight   = (TextView) itemView.findViewById(R.id.selected_weight);


            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(itemView.getContext(),
                    R.array.possible_weights, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

        public TextView getTextViewName()
        {
            return this.textViewName;
        }

        public TextView getTextViewWeight() {
            return textViewWeight;
        }

        public Spinner getSpinner() {
            return spinner;
        }
    }
}
