package com.sisbarra.orienteegame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Qui creo intent per creare l'activity vera e propria
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }
}
