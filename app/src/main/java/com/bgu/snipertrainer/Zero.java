package com.bgu.snipertrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnTouchListener;

public class Zero extends AppCompatActivity implements View.OnClickListener{

    private ViewGroup zeroLayout;
    private View vertical_line;
    private View horizontal_line;

    private int xDelta;
    private int yDelta;
    private int half_line_thickness;

    private int zero_x, zero_y;

    private Button b_save;

    //test
    TextView text_x, text_y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zero);

        // Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialization of Layouts
        zeroLayout = (ConstraintLayout) findViewById(R.id.zero_layout);
        vertical_line = (View) findViewById(R.id.vertical_line);
        horizontal_line = (View) findViewById(R.id.horizontal_line);

        // Initialization of onTouch listeners
        vertical_line.setOnTouchListener(onTouchListener());
        horizontal_line.setOnTouchListener(onTouchListener());

        // Initialization of Button and onClick Listener
        b_save = findViewById(R.id.b_save);
        b_save.setOnClickListener(this);

        // Initialization of x,y from Main class
        zero_x = MainActivity.zero_x;
        zero_y = MainActivity.zero_y;

        // Setting lines layout positions
        ConstraintLayout.LayoutParams hParams = (ConstraintLayout.LayoutParams) horizontal_line.getLayoutParams();
        ConstraintLayout.LayoutParams vParams = (ConstraintLayout.LayoutParams) vertical_line.getLayoutParams();

        // Initialization of display metric density
        half_line_thickness = hParams.height/2;

        vParams.leftMargin = zero_x - half_line_thickness;
        hParams.topMargin = zero_y - half_line_thickness;

        horizontal_line.setLayoutParams(hParams);
        vertical_line.setLayoutParams(vParams);


        // Initialization of textViews
        text_x = findViewById(R.id.rawX);
        text_y = findViewById(R.id.rawY);

        // Get screen width and height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        text_x.setText("X: " + String.valueOf(zero_x) + "/" + width);
        text_y.setText("Y: " + String.valueOf(zero_y) + "/" + height);
    }

    // Save button saves the x,y values to static variables in main class.
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.b_save){
            MainActivity.zero_x = zero_x;
            MainActivity.zero_y = zero_y;
            finish();
        }
    }


    // onTouched for horizontal and vertical lines dragging
    private OnTouchListener onTouchListener() {
        return new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if(view.getId() == R.id.vertical_line || view.getId() == R.id.horizontal_line) {
                    // Touching locations
                    final int x = (int) event.getRawX();
                    final int y = (int) event.getRawY();

                    // Get screen width and height
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;

                    ConstraintLayout.LayoutParams lParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();

                    switch (event.getAction() & MotionEvent.ACTION_MASK) {

                        // When a line is touched
                        case MotionEvent.ACTION_DOWN:
                            xDelta = x - lParams.leftMargin;
                            yDelta = y - lParams.topMargin;
                            break;

                        // When a line is dragged
                        case MotionEvent.ACTION_MOVE:

                            lParams.rightMargin = 0;
                            lParams.bottomMargin = 0;

                            switch (view.getId()) {

                                // vertical line is dragged
                                case R.id.vertical_line:
                                    lParams.leftMargin = x - xDelta;
                                    zero_x = x - xDelta + half_line_thickness;
                                    text_x.setText("X: " + String.valueOf(zero_x) + "/" + width);
                                    break;

                                // horizontal line is dragged
                                case R.id.horizontal_line:
                                    lParams.topMargin = y - yDelta;
                                    zero_y = y - yDelta + half_line_thickness;
                                    text_y.setText("Y: " + String.valueOf(zero_y) + "/" + height);
                                    break;
                            }

                            view.setLayoutParams(lParams);
                            break;
                    }
                }

                zeroLayout.invalidate();
                return true;
            }
        };
    }
}
