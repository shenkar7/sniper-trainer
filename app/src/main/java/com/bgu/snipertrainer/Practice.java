package com.bgu.snipertrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import java.util.Random;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import com.bgu.snipertrainer.video360.MonoscopicView;

public class Practice extends AppCompatActivity implements View.OnClickListener{

    // fire is in process
    boolean firing;
    // while trigger is pressed
    boolean pressed;

    AudioManager audioManager;
    MediaPlayer fireSound;
    Button b_reset;
    Button b_reticle;

    //Text views
    TextView hits_num;
    TextView azimuth;
    TextView pitch;
    TextView roll;
    TextView bottom_text;
    TextView left_text;
    TextView top_text;
    TextView right_text;
    TextView target_x;
    TextView target_y;

    // counters
    int shots;
    int hits;

    // screen density
    int density;

    // targets positions
    float[] targets_x;
    float[] targets_y;

    //Targets size addition to hit
    int[] targets_x_addition;
    int[] targets_y_addition;

    //Targets vertical & horizontal offset for hit (distance, wind etc)
    int[] targets_ver_offset;
    int[] targets_hor_offset;

    // Targets position in field
    double[] targets_h_position;
    double[] targets_v_position;

    // Targets motion horizontal addition
    double[] targets_motion_add;

    // screen width and height
    int screen_width;
    //int screen_height;

    // Reticle position on screen
    int x_pos;
    int y_pos;

    // Constraint Layouts
    ConstraintLayout target_1, target_2, target_3, target_4, target_5, target_6, target_7, target_8, target_9, target_10;

    ConstraintLayout targets_array[];

    private MonoscopicView videoView;

    // LayoutParams
    //ConstraintLayout.LayoutParams lParams_1;
    //ConstraintLayout.LayoutParams lParams_2;
    ConstraintLayout.LayoutParams h_zero_view_lParams;
    ConstraintLayout.LayoutParams v_zero_view_lParams;
    ConstraintLayout.LayoutParams targets_lParams[];

    //Zero views for testing
    View h_zero_view;
    View v_zero_view;

    // Targets motions
    Motion targets_motion[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_vr);

        // Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Initialization of variables
        firing = false;
        pressed = false;
        shots=0;
        hits=0;

        // Get screen width and height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //screen_height = displayMetrics.heightPixels;
        screen_width = displayMetrics.widthPixels;
        density = (int)displayMetrics.density; // 3 in galaxy s7

        // Set reticle position on screen. x and y are of portrait mode.
        x_pos = screen_width - MainActivity.zero_y;
        y_pos = MainActivity.zero_x;

        // Initialization of Buttons
        b_reset = findViewById(R.id.b_reset);
        b_reticle = findViewById(R.id.b_reticle);

        // setting onClickListener for buttons
        b_reset.setOnClickListener(this);
        b_reticle.setOnClickListener(this);

        // Initialization of TextViews
        hits_num = findViewById(R.id.hits_num);
        hits_num.setText("0");
        azimuth = findViewById(R.id.azimuth);
        pitch = findViewById(R.id.pitch);
        roll = findViewById(R.id.roll);
        bottom_text = findViewById(R.id.bottomText);
        left_text = findViewById(R.id.leftText);
        top_text = findViewById(R.id.topText);
        right_text = findViewById(R.id.rightText);
        target_x = findViewById(R.id.target_x);
        target_y = findViewById(R.id.target_y);

        // Initialization Targets
        target_1 = findViewById(R.id.target_1);
        target_2 = findViewById(R.id.target_2);
        target_3 = findViewById(R.id.target_3);
        target_4 = findViewById(R.id.target_4);
        target_5 = findViewById(R.id.target_5);
        target_6 = findViewById(R.id.target_6);
        target_7 = findViewById(R.id.target_7);
        target_8 = findViewById(R.id.target_8);
        target_9 = findViewById(R.id.target_9);
        target_10 = findViewById(R.id.target_10);

        // Setting Array of targets. indexes:
        // 0 - test target
        // 1 - 200m   2 - 300m   3 - 400m   4 - 500m  5 - 600m   6 - 700m all static
        // 7 - 200m   8 - 300m   9 - 400m
        targets_array = new ConstraintLayout[]{target_1, target_2, target_3, target_4, target_5, target_6, target_7, target_8, target_9, target_10};

        // Setting Arrays of targets live on screen position
        targets_x = new float[targets_array.length];
        targets_y = new float[targets_array.length];

        // Setting Arrays of targets position in field
            // 1 radian = 57 deg
            // to move target up: negative offset in targets_v_position
            // to move target right: negative offset in targets_h_position
        targets_h_position = new double[]{-0.056, -2.63, -2.64, -2.62, -2.61, -2.62,  -2.63 , -2.58, -2.57, -2.59};
        targets_v_position = new double[]{-0.023,  0.08, 0.057, 0.043, 0.036, 0.032,  0.0295,  0.08, 0.057, 0.043};

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

        // Setting Array of targets size additions (equivalent to target width and height in xml)
        targets_x_addition = new int[]{
                this.density*27,
                Math.round(this.density*13.5f),
                Math.round(this.density*9),
                Math.round(this.density*6.75f),
                Math.round(this.density*5.4f),
                Math.round(this.density*4.5f),
                Math.round(this.density*3.857f),
                Math.round(this.density*13.5f),
                Math.round(this.density*9),
                Math.round(this.density*6.75f)
        };

        targets_y_addition = new int[]{
                Math.round(this.density*20.25f),
                Math.round(this.density*10.125f),
                Math.round(this.density*6.75f),
                Math.round(this.density*5.0625f),
                Math.round(this.density*4.05f),
                Math.round(this.density*3.375f),
                Math.round(this.density*2.893f),
                Math.round(this.density*10.125f),
                Math.round(this.density*6.75f),
                Math.round(this.density*5.0625f)
        };

        //4.5dp = 1 MIL
        // 13.5 pixel = 1 MIL
        // 100m: 0  ,  200m: 0.6MIL = 16pix  ,  300m: 1.5MIL =  40pix   , 400m = 2.5MIL = 67pix  ,  500m: 3.7MIL = 100pix  ,  600m: 5.1MIL = 138pix  ,  700m: 6.7MIL = 181pix
        targets_ver_offset = new int[]{0,8,20,34,50,69,90,8,20,34};

        // 0.7MIL = 19pix   1.4MIL = 38pix    2.1MIL = 57pix
        targets_hor_offset = new int[]{0,0,0,0,0,0,0,19,19,9};

        h_zero_view = findViewById(R.id.h_zero_line);
        v_zero_view = findViewById(R.id.v_zero_line);

        h_zero_view_lParams = (ConstraintLayout.LayoutParams) h_zero_view.getLayoutParams();
        v_zero_view_lParams = (ConstraintLayout.LayoutParams) v_zero_view.getLayoutParams();

        h_zero_view_lParams.topMargin = MainActivity.zero_x;
        v_zero_view_lParams.rightMargin = MainActivity.zero_y;

        h_zero_view.setVisibility(View.INVISIBLE);
        v_zero_view.setVisibility(View.INVISIBLE);


        // Sett targets motions. start and exit is in onPause and onResume
        targets_motion = new Motion[]{
                new Motion(this, 7, 200, 3.0,0.00012),
                new Motion(this, 8, 300, 2.5,0.00008),
                new Motion(this, 9, 400, 2.5,0.00004),
        };


        // Configure the MonoscopicView which will render the video and UI.
        videoView = (MonoscopicView) findViewById(R.id.video_view);
        videoView.initialize();

        // VideoView Listener for ui and targets
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
                      double a = 24500.953f * Math.sin(angles[2]);
                      double b = 24500.953f * Math.cos(angles[2]);

                      rotation_reference = 90.0f + roll_deg;

                      // 1 radian = 57 deg

                      // Roll rotation in deg
                      for(int i=0; i<targets_array.length; i++){
                          int top_margin = (int)((angles[0]+targets_h_position[i]+targets_motion_add[i])*a - (angles[1]+targets_v_position[i])*b);
                          int right_margin = -(int)((angles[0]+targets_h_position[i]+targets_motion_add[i])*b + (angles[1]+targets_v_position[i])*a);
                          targets_lParams[i].topMargin = top_margin;
                          targets_lParams[i].bottomMargin = (-1)*top_margin;
                          targets_lParams[i].rightMargin = right_margin;
                          targets_lParams[i].leftMargin = (-1)*right_margin;

                          targets_array[i].setRotation(rotation_reference);

                          targets_x[i] = targets_array[i].getX();
                          targets_y[i] = targets_array[i].getY();
                      }


                      // Setting texts in the ui

                      try {
                          azimuth.setText(String.valueOf(azimuth_deg).substring(0, 5));
                          pitch.setText(String.valueOf(pitch_deg).substring(0, 5));
                          roll.setText(String.valueOf(roll_deg).substring(0, 5));
                          bottom_text.setText(String.valueOf(targets_lParams[0].bottomMargin));
                          left_text.setText(String.valueOf(targets_lParams[0].leftMargin));
                          top_text.setText(String.valueOf(targets_lParams[0].topMargin));
                          right_text.setText(String.valueOf(targets_lParams[0].rightMargin));
                          target_y.setText(String.valueOf(targets_array[0].getX()));
                          target_x.setText(String.valueOf(targets_array[0].getY()));
                      } catch (Exception e) {
                      }

                  }
              }
        });

        initializeActivity();

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Save the new Intent which may contain a new Uri. Then tear down & recreate this Activity to
        // load that Uri.
        setIntent(intent);
        recreate();
    }

    /** Initializes the Activity only if the permission has been granted.*/
    private void initializeActivity() {
        ViewGroup root = (ViewGroup) findViewById(R.id.activity_root);
        for (int i = 0; i < root.getChildCount(); ++i) {
            root.getChildAt(i).setVisibility(View.VISIBLE);
        }
        videoView.loadMedia(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.onResume();

        //Initialization of sounds - For speakers when audio plug is used
        audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.STREAM_MUSIC);
        audioManager.setSpeakerphoneOn(true);

        // Starting the motion threads
        for (Motion m : targets_motion) {
            m.start();
        }

    }

    @Override
    protected void onPause() {
        videoView.onPause();
        super.onPause();

        audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);

        // Stopping the motion threads
        for (Motion m : targets_motion) {
            m.stopThread();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.b_reset){
                hits_num.setText("0");
                shots=0;
                hits=0;
        }
        if(view.getId() == R.id.b_reticle){
            if(h_zero_view.getVisibility() == View.VISIBLE){
                h_zero_view.setVisibility(View.INVISIBLE);
                v_zero_view.setVisibility(View.INVISIBLE);
            }
            else{
                h_zero_view.setVisibility(View.VISIBLE);
                v_zero_view.setVisibility(View.VISIBLE);
            }
        }
    }


    // Make a fire sound
    private void fireSound(){
        fireSound = MediaPlayer.create(this, R.raw.shot5);
        fireSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        fireSound.start();
    }


    // Plays a random ding (hit) sound with random delay
    private void dingSound(){
        // random ding sound
        int i = getRandomNumberInRange(1,6);
        // random ding delay
        int delay = getRandomNumberInRange(300,800);

        // play ding sound with delay
        Sound dingSoundThread = new Sound(this,"ding"+i, delay);
        dingSoundThread.start();
    }


    // creates a random int in a certain input range
    private int getRandomNumberInRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }


    // Setting external trigger button down. While button is down this method is called repeatedly.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // if trigger is pulled
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ) {
            // if shooting is possible (no fire in progress and trigger isn't pulled)
            if (!firing && !pressed) {
                pressed = true;
                firing = true;
                pressed = true;
                shot();
                Fire fire = new Fire(this);
                fire.start();

            }
            // if trigger pulled but previous fire is still in process
            else if (!pressed) {
                pressed = true;
            }
            return true;
        }
        // this return is for other buttons that were pressed (like volume)
        return super.onKeyDown(keyCode, event);
    }


    // When trigger is released
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK )
            pressed = false;
        return super.onKeyUp(keyCode, event);
    }


    // Main function of shooting.
    private void shot(){
        fireSound();
        // test - randoms a probability to hit
        //double random = new Random().nextDouble();



        // Check if any target got hit
        for(int i=0; i < targets_array.length; i++){
            if(x_pos > (targets_x[i] + targets_ver_offset[i]) && x_pos < (targets_x[i] + targets_x_addition[i] + + targets_ver_offset[i]) &&
                    y_pos > (targets_y[i] + targets_hor_offset[i]) && y_pos < (targets_y[i] + targets_y_addition[i] + targets_hor_offset[i]) ) {
                dingSound();
                hits++;
                break;
            }
        }

        shots++;
        hits_num.setText(hits + "/" + shots);
    }
}


///////////////////////////////////
// Other Classes


// Fire process class. When thread starts, makes a delay and lets new firing only after this one is done.
class Fire extends Thread{
    private Practice practice;

    public Fire(Practice practice){
        this.practice = practice;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(500);
        }catch(InterruptedException e){}
        practice.firing = false;
    }
}


// Sound class. Plays a requested sound with a requested delay.
class Sound extends Thread{

    private Practice practice;
    private String sound_name;
    private int delay;
    private MediaPlayer sound;


    public Sound(Practice practice, String sound_name, int delay){
        this.practice = practice;
        this.sound_name = sound_name;
        this.delay = delay;
    }

    @Override
    public void run() {
        // performs delay
        try {
            Thread.sleep(delay);
        }catch(InterruptedException e){}

        // play sound
        int soundID = practice.getResources().getIdentifier(sound_name, "raw", practice.getPackageName());
        sound = MediaPlayer.create(practice.getApplicationContext(), soundID);
        sound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override public
            void onCompletion(MediaPlayer mp) { mp.release(); }
        });
        sound.start();
    }
}

// Fire process class. When thread starts, makes a delay and lets new firing only after this one is done.
class Motion extends Thread{
    private Practice practice;
    int i;
    double addition;    // Speed - How much horizontal distance to add each time interval.
    double limit;       //  In radians. Determines when the target switch direction (half of the distance)
    double added;       // How much total was added in radians.
    boolean exit;
    int hor_offset;
    int target_index;

    // target_distance & motion length are in meters
    public Motion(Practice practice, int target_index, int target_distance, double motion_length,double addition){
        this.practice = practice;
        this.i = target_index;
        this.addition = addition;
        this.limit = ((motion_length * 100.0) / (target_distance / 10.0)) / 1000.0;
        this. added = 0.0;
        this.exit = false;
        this.target_index = target_index;
        this.hor_offset = -practice.targets_hor_offset[target_index];

    }

    @Override
    public void run() {

        while(true && !exit){
            try {
                Thread.sleep(33);
            }catch(InterruptedException e){}
            practice.targets_motion_add[i] += addition;
            added += addition;
            practice.targets_hor_offset[target_index] = hor_offset;

            if(Math.abs(added) > limit){

                practice.targets_hor_offset[target_index] = 0;
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