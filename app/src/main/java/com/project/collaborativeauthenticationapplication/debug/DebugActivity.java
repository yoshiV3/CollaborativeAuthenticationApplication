package com.project.collaborativeauthenticationapplication.debug;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.collaborativeauthenticationapplication.R;
import android.os.Bundle;

public class DebugActivity extends AppCompatActivity implements Debuggable{



    private CustomDebugMessageListAdapter adapter = new CustomDebugMessageListAdapter();

    private Debugger debugger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
    }

    @Override
    protected void onStart() {
        RecyclerView view = findViewById(R.id.recycler_debug_messages);
        view.setLayoutManager(new LinearLayoutManager(this ));
        view.setAdapter(adapter);
        debugger = new Debugger(this);
        debugger.runNextTest();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }


    @Override
    public void handleResult(Signal signal, boolean done) {
        adapter.pushMessage(signal);
        if (! done)
        {
            debugger.runNextTest();
        }
        else
        {
            adapter.pushMessage(new Signal("DONE", false));
        }
    }
}