package com.project.collaborativeauthenticationapplication.service;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.collaborativeauthenticationapplication.ItemListAdapter;
import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;


import java.util.ArrayList;
import java.util.List;

public class CustomOverviewSecretsAdapter extends ItemListAdapter<CustomOverviewSecretsAdapter.ViewHolder, ApplicationLoginEntity> {


    private View.OnClickListener      listener;

    public CustomOverviewSecretsAdapter() {
        super(new ArrayList<ApplicationLoginEntity>());
    }


    public void clear(){
        ArrayList<ApplicationLoginEntity> internal = getItemsInternalCopy();
        int oldLength = internal.size();
        internal.clear();
        notifyItemRangeRemoved(0, oldLength);
    }

    public void fillData(List<ApplicationLoginEntity> items){
        ArrayList<ApplicationLoginEntity> internal = getItemsInternalCopy();
        internal.addAll(items);
        notifyItemRangeInserted(0, items.size());
    }


    public void setOnClickListener(View.OnClickListener listener)
    {
        this.listener = listener;
    }
    protected View.OnClickListener getListener()
    {
        return listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_credential, parent, false);
        view.setOnClickListener(getListener());
        return new CustomOverviewSecretsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationLoginEntity entity = getItemsInternalCopy().get(position);
        holder.getTextViewApplicationName().setText(entity.applicationName);
        holder.getTextViewLogin().setText(entity.login);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private  final TextView textViewApplicationName;
        private  final TextView textViewLogin;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.textViewApplicationName = (TextView) itemView.findViewById(R.id.application_name_credential);
            this.textViewLogin = (TextView) itemView.findViewById(R.id.login_credential);
        }

        public TextView getTextViewApplicationName()
        {
            return this.textViewApplicationName;
        }

        public TextView getTextViewLogin() {
            return textViewLogin;
        }

    }
}
