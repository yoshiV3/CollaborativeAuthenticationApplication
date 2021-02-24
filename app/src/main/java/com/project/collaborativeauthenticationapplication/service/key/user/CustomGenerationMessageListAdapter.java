package com.project.collaborativeauthenticationapplication.service.key.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project.collaborativeauthenticationapplication.MessageListAdapter;
import com.project.collaborativeauthenticationapplication.R;


public class CustomGenerationMessageListAdapter extends MessageListAdapter<CustomGenerationMessageListAdapter.ViewHolder, String> {


    public CustomGenerationMessageListAdapter() {
        super();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_messages, parent, false);
        return new CustomGenerationMessageListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getTextViewMessage().setText(getItemAt(position));
    }




    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private  final TextView textViewMessage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.textViewMessage          = (TextView) itemView.findViewById(R.id.message);
        }

        public TextView getTextViewMessage()
        {
            return this.textViewMessage;
        }
    }
}
