package com.project.collaborativeauthenticationapplication.alternative.management;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.alternative.key.CustomKeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.alternative.key.user.CustomOptionsRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {


    private ArrayList<String> devices = new ArrayList<>();
    private View.OnClickListener listener;


    public void addNewDevices(List<String> list){
        int insertedItemCount         = list.size();
        int positionFirstInsertedItem = getItemCount();
        devices.addAll(list);
        notifyItemRangeInserted(positionFirstInsertedItem, insertedItemCount);
    }


    public void setListener(View.OnClickListener listener){
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_options, parent, false);
        view.setOnClickListener(listener);
        return new DeviceListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String device = devices.get(position);
        holder.getTextView().setText(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device).getName());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public String getItemAt(int position) {
        return devices.get(position);
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
