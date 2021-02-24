package com.project.collaborativeauthenticationapplication;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public abstract  class ItemListAdapter<T extends RecyclerView.ViewHolder, E> extends RecyclerView.Adapter<T> {



    private ArrayList<E> items;



    public ItemListAdapter(ArrayList<E> items)
    {
        this.items = items;
    }


    protected ArrayList<E> getItemsInternalCopy()
    {
        return items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    protected E getItemAt(int index)
    {
        return items.get(index);
    }
}
