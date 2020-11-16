package com.bgu.snipertrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class RealitySelection extends AppCompatActivity implements View.OnClickListener{

    View b_vr, b_ar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reality_selection);

        // Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialization of Buttons
        b_vr = findViewById(R.id.virtual_reality);
        b_ar = findViewById(R.id.augmented_reality);

        // setting onClickListener for buttons
        b_vr.setOnClickListener(this);
        b_ar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent i;
        if(view.getId()==R.id.virtual_reality) {
            i = new Intent(RealitySelection.this, RangeSelection.class);
            startActivity(i);
        }
        else if(view.getId()==R.id.augmented_reality){

            i = new Intent(RealitySelection.this, PracticeAr.class);
            startActivity(i);
        }
    }
}
