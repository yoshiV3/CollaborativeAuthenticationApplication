package com.project.collaborativeauthenticationapplication;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public abstract class  CustomShowActivity extends AppCompatActivity {


    public void showTemporally(String text)
    {
        Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
