package com.project.collaborativeauthenticationapplication.service.general;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;




public abstract class SecretOverviewFragment extends Fragment {

    private RecyclerView                     overview;
    private AdapterManager                   adapterManager;
    private SecretOverviewAdapterPresenter   adapterPresenter;


    public RecyclerView getOverview() {
        return overview;
    }

    public void setOverview(RecyclerView overview) {
        this.overview = overview;
    }

    public void setAdapterManager(AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    public AdapterManager getAdapterManager() {
        return adapterManager;
    }

    public SecretOverviewAdapterPresenter getAdapterPresenter() {
        return adapterPresenter;
    }

    public void setAdapterPresenter(SecretOverviewAdapterPresenter adapterPresenter) {
        this.adapterPresenter = adapterPresenter;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View.OnClickListener listenerAdapter = getOnClickListener();
        CustomOverviewSecretsAdapter adapter = adapterManager.getAdapter();
        adapter.setOnClickListener(listenerAdapter);
        overview.setLayoutManager(new LinearLayoutManager(getContext()));
        overview.setAdapter(adapter);
    }



    @Override
    public void onStart() {
        super.onStart();
        adapterPresenter.onStartOverview();
    }

    protected abstract View.OnClickListener getOnClickListener();


}
