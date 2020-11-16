package com.bgu.snipertrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Instructions extends AppCompatActivity implements View.OnClickListener{

    String[] steps_titles,steps_descriptions;

    Button b_next,b_back;
    TextView title,description;
    int stepNum;
    ImageView step_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        // Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialization of TextViews
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);

        // Initialization of Buttons
        b_next = findViewById(R.id.b_next);
        b_back = findViewById(R.id.b_back);

        // setting onClickListener for buttons
        b_next.setOnClickListener(this);
        b_back.setOnClickListener(this);

        // Initialization of Image
        step_image = findViewById(R.id.step_image);

        // Importing string array from xml
        steps_titles = getResources().getStringArray(R.array.steps_titles);
        steps_descriptions = getResources().getStringArray(R.array.steps_descriptions);

        // Setting initial values to step number, TextViews and image
        stepNum = 0;
        title.setText(steps_titles[stepNum]);
        description.setText(steps_descriptions[stepNum]);
        step_image.setImageDrawable(getResources().getDrawable(R.drawable.step1_pic));
        b_back.setVisibility(View.INVISIBLE);
    }

    public void onClick(View view) {

        switch (view.getId()){
            case R.id.b_next:
                stepNum++;

                if(stepNum==1)
                    b_back.setVisibility(View.VISIBLE);

                if(stepNum==3)
                    b_next.setVisibility(View.INVISIBLE);

                break;

            case R.id.b_back:
                stepNum--;

                if(stepNum==0)
                    b_back.setVisibility(View.INVISIBLE);

                if(stepNum==2)
                    b_next.setVisibility(View.VISIBLE);
                break;
        }

        title.setText(steps_titles[stepNum]);
        description.setText(steps_descriptions[stepNum]);
        int picID = this.getResources().getIdentifier("step"+(stepNum+1)+"_pic", "drawable", this.getPackageName());
        step_image.setImageDrawable(getResources().getDrawable(picID));

    }
}
