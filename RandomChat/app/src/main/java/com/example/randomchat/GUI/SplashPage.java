package com.example.randomchat.GUI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.randomchat.Logic.Controller;
import com.example.randomchat.R;
import com.google.android.material.button.MaterialButton;

public class SplashPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashpage);
        MaterialButton button = findViewById(R.id.entraStanze);
        Log.d("Button-width", String.valueOf(button.getWidth()));
    }

    public void entraPremuto(View view) {
        MaterialButton button = findViewById(R.id.entraStanze);
        float density = getResources().getDisplayMetrics().density;
        Log.d("Button-width", String.valueOf(button.getWidth()/density));
        EditText nick = findViewById(R.id.nickname);
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Nickname", nick.getText().toString());
        editor.apply();
        Controller.entraPremuto(this, nick.getText().toString());
    }
}