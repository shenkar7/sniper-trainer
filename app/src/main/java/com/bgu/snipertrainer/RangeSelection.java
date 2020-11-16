package com.bgu.snipertrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class RangeSelection extends AppCompatActivity implements View.OnClickListener{

    View b_goegap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_range_selection);

        // Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialization of Buttons
        b_goegap = findViewById(R.id.goegapImage);

        // setting onClickListener for buttons
        b_goegap.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent i;
        if(view.getId()==R.id.goegapImage) {
            i = new Intent(RangeSelection.this, Practice.class);
            startActivity(i);
        }
    }
}
