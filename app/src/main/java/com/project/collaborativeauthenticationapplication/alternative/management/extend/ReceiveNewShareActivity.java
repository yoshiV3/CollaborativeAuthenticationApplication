package com.project.collaborativeauthenticationapplication.alternative.management.extend;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.project.collaborativeauthenticationapplication.R;

public class ReceiveNewShareActivity extends AppCompatActivity {


    private ExtendPresenter presenter;

    private TextView status;

    private TextView applicationName;

    private Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_new_share);

        status = findViewById(R.id.textView_status);

        applicationName = findViewById(R.id.textView_applicationName_extend);

        next        = findViewById(R.id.button_start_extend);

        status.setText("Waiting");

        applicationName.setVisibility(View.GONE);

        next.setVisibility(View.GONE);



        presenter = new ExtendPresenter(this);
        presenter.open(this);
    }


    public void displayApplicationName(String application) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                applicationName.setText(application);
                applicationName.setVisibility(View.VISIBLE);
                status.setText("Permission?");
            }
        });
    }

    public void onCLick(View view){
        status.setText("Calculating");
        next.setVisibility(View.GONE);
        presenter.continueWithOperation();
    }

    public void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                next.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onDone() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText("Done");
                next.setVisibility(View.VISIBLE);
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        });
    }
}