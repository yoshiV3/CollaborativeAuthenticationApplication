package com.project.collaborativeauthenticationapplication.alternative.management;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.alternative.key.CustomKeyGenerationPresenter;
import com.project.collaborativeauthenticationapplication.alternative.key.CustomKeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.alternative.key.KeyGenerationPresenter;
import com.project.collaborativeauthenticationapplication.alternative.key.KeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.service.network.Device;

import java.util.List;


public class SelectParametersFragment extends Fragment {


    private String remove = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_parameters, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        KeyManagementPresenter presenter =  CustomKeyManagementPresenter.getInstance();

        Button button = view.findViewById(R.id.button_next);;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.selectedDevice(remove);
            }
        });




        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_devices);

        DeviceListAdapter adapter = new DeviceListAdapter();

        presenter.getDeviceList(new DataFiller() {

            @Override
            public void fill(List<String> list) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addNewDevices(list);
                    }
                });
            }
        } );

        TextView textView = view.findViewById(R.id.textView_remove);

        adapter.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildLayoutPosition(v);

                String item = adapter.getItemAt(position);


                if (!item.equals(remove)){
                    remove = item;
                    textView.setText(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(item).getName());

                } else {
                    remove = null;
                    textView.setText("");
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setAdapter(adapter);
    }
}