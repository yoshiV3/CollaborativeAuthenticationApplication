package com.project.collaborativeauthenticationapplication;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PopListAdapter<T extends RecyclerView.ViewHolder, E> extends ItemListAdapter<T, E>
{
    private View.OnClickListener      listener;


    public PopListAdapter(ArrayList<E> items)
    {
        super(items);
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
        E old = getItemsInternalCopy().remove(index);
        notifyItemRemoved(index);
        return old;
    }

    public void add(E item)
    {
        this.getItemsInternalCopy().add(item);
        notifyItemInserted(getItemCount()-1);
    }

    public void addList(ArrayList<E> items)
    {
        int insertedItemCount         = items.size();
        int positionFirstInsertedItem = getItemCount();
        this.getItemsInternalCopy().addAll(items);
        notifyItemRangeInserted(positionFirstInsertedItem, insertedItemCount);
    }

    public List<E> getItems()
    {
        return Collections.unmodifiableList(getItemsInternalCopy());
    }


}
