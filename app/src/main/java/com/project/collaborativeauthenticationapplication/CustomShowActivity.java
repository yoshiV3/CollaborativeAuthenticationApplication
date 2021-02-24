package com.project.collaborativeauthenticationapplication;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public abstract class  CustomShowActivity extends AppCompatActivity {


    public void showTemporally(String text)
    {
        Toast toast = new Toast(this );
        toast.setText(text);
        toast.show();

    }
}
