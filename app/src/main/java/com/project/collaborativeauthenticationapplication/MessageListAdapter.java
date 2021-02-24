package com.project.collaborativeauthenticationapplication;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public abstract class  MessageListAdapter<T extends RecyclerView.ViewHolder, E> extends ItemListAdapter<T,E> {


    protected MessageListAdapter()
    {
        super(new ArrayList<>());
    }


    public void pushMessage(E message)
    {
        int position = 0;
        this.getItemsInternalCopy().add(position, message);
        notifyItemInserted(position);
    }
}
