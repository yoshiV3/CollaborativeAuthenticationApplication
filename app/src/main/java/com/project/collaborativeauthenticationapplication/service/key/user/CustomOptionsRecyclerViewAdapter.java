package com.project.collaborativeauthenticationapplication.service.key.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.collaborativeauthenticationapplication.PopListAdapter;


import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.service.Participant;

import java.util.ArrayList;

public class CustomOptionsRecyclerViewAdapter extends PopListAdapter<CustomOptionsRecyclerViewAdapter.ViewHolder, Participant> {


    public CustomOptionsRecyclerViewAdapter(ArrayList<Participant> items) {
        super(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_options, parent, false);
        view.setOnClickListener(getListener());
        return new CustomOptionsRecyclerViewAdapter.ViewHolder(view);    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getTextView().setText(getItemAt(position).getName());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private  final TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.textView = (TextView) itemView.findViewById(R.id.option);
        }

        public TextView getTextView()
        {
            return this.textView;
        }

    }
}
