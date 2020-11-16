package com.bgu.snipertrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.Manifest.permission;

import com.bgu.snipertrainer.video360.MonoscopicView;

public class PracticeAr extends AppCompatActivity {

    // For MediaLoader to load the empty jpg as background
    static public boolean inAR;

    Camera camera;
    ShowCamera showCamera;
    FrameLayout frameLayout;
    private static final int CAMERA_PERMISSION_CODE = 1;

    // Targets motion horizontal addition
    double[] targets_motion_add;

    ConstraintLayout target_1, target_2, target_3, target_4, target_5, target_6, target_7, target_8, target_9, target_10;
    ConstraintLayout ui;

    ConstraintLayout targets_array[];

    // Targets position in field
    double[] targets_h_position;
    double[] targets_v_position;

    // LayoutParams
    ConstraintLayout.LayoutParams targets_lParams[];

    private MonoscopicView videoView;

    TextView azimuth;
    TextView pitch;
    TextView roll;
    TextView bottom_text;
    TextView left_text;
    TextView top_text;
    TextView right_text;

    // Targets motions
    MotionAr targets_motion[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_ar);

        // Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        ui = (ConstraintLayout) findViewById(R.id.ar_ui) ;
        ui.setVisibility(View.INVISIBLE);

        //Initialization of frameLayout (for camera)
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);


        // Initialization Targets
        target_1 = findViewById(R.id.target_ar_1);
        target_2 = findViewById(R.id.target_ar_2);
        target_3 = findViewById(R.id.target_ar_3);
        target_4 = findViewById(R.id.target_ar_4);
        target_5 = findViewById(R.id.target_ar_5);
        target_6 = findViewById(R.id.target_ar_6);
        target_7 = findViewById(R.id.target_ar_7);
        target_8 = findViewById(R.id.target_ar_8);
        target_9 = findViewById(R.id.target_ar_9);
        target_10 = findViewById(R.id.target_ar_10);

        // Setting Array of targets. indexes:
        // 0 - test target
        // 1 - 200m   2 - 300m   3 - 400m   4 - 500m  5 - 600m   6 - 700m all static
        // 7 - 200m   8 - 300m   9 - 400m
        targets_array = new ConstraintLayout[]{target_1, target_2, target_3, target_4, target_5, target_6, target_7, target_8, target_9, target_10};


        // Setting Arrays of targets position in field
        // 1 radian = 57 deg
        // to move target up: negative offset in targets_v_position
        // to move target right: negative offset in targets_h_position
        targets_h_position = new double[]{-0.056, -2.63, -2.4, -2.2, -2.0, -2.2,  -1.8 , -1.6, -1.4, -1.2};
        targets_v_position = new double[]{-0.023,  0.0, 0.0, 0.0, 0.0, 0.0,  0.0,  0.0, 0.0, 0.0};

        // Setting Array of targets motion addition
        targets_motion_add = new double[]{0,0,0,0,0,0,0,0,0,0};

        // Setting Array of targets lParams
        targets_lParams = new ConstraintLayout.LayoutParams[] {
                (ConstraintLayout.LayoutParams) target_1.getLayoutParams(),
                (ConstraintLayout.LayoutParams) target_2.getLayoutParams(),
                (ConstraintLayout.LayoutParams) target_3.getLayoutParams(),
                (ConstraintLayout.LayoutParams) target_4.getLayoutParams(),
                (ConstraintLayout.LayoutParams) target_5.getLayoutParams(),
                (ConstraintLayout.LayoutParams) target_6.getLayoutParams(),
                (ConstraintLayout.LayoutParams) target_7.getLayoutParams(),
                (ConstraintLayout.LayoutParams) target_8.getLayoutParams(),
                (ConstraintLayout.LayoutParams) target_9.getLayoutParams(),
                (ConstraintLayout.LayoutParams) target_10.getLayoutParams()
        };

        azimuth = findViewById(R.id.azimuth);
        pitch = findViewById(R.id.pitch);
        roll = findViewById(R.id.roll);

        bottom_text = findViewById(R.id.bottomText);
        left_text = findViewById(R.id.leftText);
        top_text = findViewById(R.id.topText);
        right_text = findViewById(R.id.rightText);


        // Sett targets motions. start and exit is in onPause and onResume
        targets_motion = new MotionAr[]{
                new MotionAr(this, 7, 200, 3.0,0.00012),
                new MotionAr(this, 8, 300, 2.5,0.00008),
                new MotionAr(this, 9, 400, 2.5,0.00004),
        };

        videoView = (MonoscopicView) findViewById(R.id.video_view_ar);
        videoView.initialize();

        videoView.setListener(new MonoscopicView.Listener() {
            @Override
            public void onRotation(float[] angles, int accuracy) {
                float azimuth_deg = 0;
                float pitch_deg = 0;
                float roll_deg = 0;

                float rotation_reference;

                if (accuracy == 3) {

                    // Converting radians to deg
                    azimuth_deg = angles[0] * (180.0f / ((float) Math.PI));
                    pitch_deg = angles[1] * (180.0f / ((float) Math.PI));
                    roll_deg = angles[2] * (180.0f / ((float) Math.PI));

                    // top marging = azimuth * a - pitch * b
                    // right margin = -(azimuth * b + pitch * a)

                    // was 16800.953f with FOV 13 deg
                    double a = 10500.953f * Math.sin(angles[2]);
                    double b = 10500.953f * Math.cos(angles[2]);

                    rotation_reference = 90.0f + roll_deg;

                    // 1 radian = 57 deg

                    // Roll rotation in deg
                    for (int i = 0; i < targets_array.length; i++) {
                        int top_margin = -(int) ((angles[0] + targets_h_position[i]+targets_motion_add[i]) * b + (angles[1] + targets_v_position[i]) * a);
                        int left_margin = (int) ((angles[0] + targets_h_position[i]+targets_motion_add[i]) * a - (angles[1] + targets_v_position[i]) * b);
                        targets_lParams[i].topMargin = top_margin;
                        targets_lParams[i].bottomMargin = (-1) * top_margin;
                        targets_lParams[i].rightMargin = (-1) * left_margin;
                        targets_lParams[i].leftMargin = left_margin;

                        targets_array[i].setRotation(rotation_reference);
                    /*
                    targets_x[i] = targets_array[i].getX();
                    targets_y[i] = targets_array[i].getY();
                    */
                    }


                    // Setting texts in the ui

                    try {


                        // Setting texts in the ui
                        azimuth.setText(String.valueOf(azimuth_deg).substring(0, 5));
                        pitch.setText(String.valueOf(pitch_deg).substring(0, 5));
                        roll.setText(String.valueOf(roll_deg).substring(0, 5));
                    /*
                    bottom_text.setText(String.valueOf(lParams.bottomMargin));
                    left_text.setText(String.valueOf(lParams.leftMargin));
                    top_text.setText(String.valueOf(lParams.topMargin));
                    right_text.setText(String.valueOf(lParams.rightMargin));
                    */
                    } catch (Exception e) {
                    }
                }
            }

        });


        // Boilerplate for checking runtime permissions in Android.
        if (ContextCompat.checkSelfPermission(this, permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    PracticeAr.this,
                    new String[] {Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            // Permission has already been granted.
            initializeActivity();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.onResume();
        inAR = true;

        // Starting the motion threads
        for (MotionAr m : targets_motion) {
            m.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.onPause();

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Save the new Intent which may contain a new Uri. Then tear down & recreate this Activity to
        // load that Uri.
        setIntent(intent);
        recreate();
    }

    /* Handles the user accepting the permission. */
     @Override
     public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
         if (requestCode == CAMERA_PERMISSION_CODE) {
             if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                 initializeActivity();
             }
             else{
                 finish();
             }
         }
     }

    @Override
    protected void onStop() {
        super.onStop();
        inAR = false;
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initializeActivity(){

         // Open the camera
         camera = Camera.open();
         showCamera = new ShowCamera(this, camera);
         frameLayout.addView(showCamera);

         // Set target and ui visible
         ui.setVisibility(View.VISIBLE);

        ViewGroup root = (ViewGroup) findViewById(R.id.practice_ar);
        for (int i = 0; i < root.getChildCount(); ++i) {
            root.getChildAt(i).setVisibility(View.VISIBLE);
        }
        videoView.loadMedia(getIntent());
     }

}

class MotionAr extends Thread{
    private PracticeAr practiceAr;
    int i;
    double addition;    // Speed - How much horizontal distance to add each time interval.
    double limit;       //  In radians. Determines when the target switch direction (half of the distance)
    double added;       // How much total was added in radians.
    boolean exit;
    int hor_offset;
    int target_index;

    // target_distance & motion length are in meters
    public MotionAr(PracticeAr practiceAr, int target_index, int target_distance, double motion_length,double addition){
        this.practiceAr = practiceAr;
        this.i = target_index;
        this.addition = addition;
        this.limit = ((motion_length * 100.0) / (target_distance / 10.0)) / 1000.0;
        this. added = 0.0;
        this.exit = false;
        this.target_index = target_index;
        //this.hor_offset = -practiceAr.targets_hor_offset[target_index];

    }

    @Override
    public void run() {

        while(true && !exit){
            try {
                Thread.sleep(33);
            }catch(InterruptedException e){}
            practiceAr.targets_motion_add[i] += addition;
            added += addition;
            //practiceAr.targets_hor_offset[target_index] = hor_offset;

            if(Math.abs(added) > limit){

                //practiceAr.targets_hor_offset[target_index] = 0;
                addition = -addition;
                hor_offset = -hor_offset;

                try {
                    Thread.sleep(1500);
                }catch(InterruptedException e){}
            }
        }
    }

    public void stopThread(){
        this.exit = true;
    }
}
