package com.bgu.snipertrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button b_practice, b_zero, b_instructions, b_about;
    public static int zero_x, zero_y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full Screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set xml layout
        setContentView(R.layout.activity_main);

        // Initialization of Buttons
        b_practice = findViewById(R.id.b_practice);
        b_zero = findViewById(R.id.b_zero);
        b_instructions = findViewById(R.id.b_instructions);
        b_about = findViewById(R.id.b_about);

        // setting onClickListener for buttons
        b_practice.setOnClickListener(this);
        b_zero.setOnClickListener(this);
        b_instructions.setOnClickListener(this);
        b_about.setOnClickListener(this);

        zero_x = 1000;
        zero_y = 500;
    }

    @Override
    public void onClick(View view) {
        Intent i;
        switch (view.getId()){
            case R.id.b_practice:
                i = new Intent(MainActivity.this, RealitySelection.class);
                startActivity(i);
                break;

            case R.id.b_zero:
                i = new Intent(MainActivity.this, Zero.class);
                startActivity(i);
                break;

            case R.id.b_instructions:
                i = new Intent(MainActivity.this, Instructions.class);
                startActivity(i);
                break;

            case R.id.b_about:
                i = new Intent(MainActivity.this, About.class);
                startActivity(i);
                break;
        }
    }





}