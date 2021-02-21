package com.project.collaborativeauthenticationapplication;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PopListAdapter<T extends RecyclerView.ViewHolder, E> extends RecyclerView.Adapter<T>
{
    private View.OnClickListener      listener;
    private ArrayList<E>              items;



    public PopListAdapter(ArrayList<E> items)
    {
        this.items = items;
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    protected E getItemAt(int index)
    {
        return items.get(index);
    }

    public void setOnClickListener(View.OnClickListener listener)
    {
        this.listener = listener;
    }

    protected View.OnClickListener getListener()
    {
        return listener;
    }

    public E pop(int index)
    {
        E old = items.remove(index);
        notifyItemRemoved(index);
        return old;
    }

    public void add(E item)
    {
        this.items.add(item);
        notifyItemInserted(this.items.size()-1);
    }

    public void addList(ArrayList<E> items)
    {
        int insertedItemCount         = items.size();
        int positionFirstInsertedItem = this.items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionFirstInsertedItem, insertedItemCount);
    }

    public List<E> getItems()
    {
        return Collections.unmodifiableList(this.items);
    }


}
