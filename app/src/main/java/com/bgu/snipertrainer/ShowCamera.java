package com.bgu.snipertrainer;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;

import java.io.IOException;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback{

    Camera camera;
    SurfaceHolder holder;

    public ShowCamera(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try{
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }catch(IOException e)
        {
            e.printStackTrace();
        }


    }
}
