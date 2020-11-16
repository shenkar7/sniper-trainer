package com.bgu.snipertrainer.video360;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import androidx.annotation.AnyThread;
import android.view.Surface;

import com.bgu.snipertrainer.PracticeAr;
import com.bgu.snipertrainer.R;
import com.bgu.snipertrainer.video360.rendering.Mesh;
import com.bgu.snipertrainer.video360.rendering.SceneRenderer;

public class MediaLoader {
    public static final String MEDIA_FORMAT_KEY = "stereoFormat";

    /** A spherical mesh for video should be large enough that there are no stereo artifacts. */
    private static final int SPHERE_RADIUS_METERS = 50;

    /** These should be configured based on the video type. But this sample assumes 360 video. */
    private static final int DEFAULT_SPHERE_VERTICAL_DEGREES = 180;
    private static final int DEFAULT_SPHERE_HORIZONTAL_DEGREES = 360;

    /** The 360 x 180 sphere has 15 degree quads. Increase these if lines in your video look wavy. */
    private static final int DEFAULT_SPHERE_ROWS = 12;
    private static final int DEFAULT_SPHERE_COLUMNS = 24;

    private final Context context;

    // This sample also supports loading images.
    Bitmap mediaImage;
    // If the video or image fails to load, a placeholder panorama is rendered with error text.
    String errorText;

    // Due to the slow loading media times, it's possible to tear down the app before mediaPlayer is
    // ready. In that case, abandon all the pending work.
    // This should be set or cleared in a synchronized manner.

    // The type of mesh created depends on the type of media.
    Mesh mesh;
    // The sceneRenderer is set after GL initialization is complete.
    private SceneRenderer sceneRenderer;
    // The displaySurface is configured after both GL initialization and media loading.
    private Surface displaySurface;

    // The actual work of loading media happens on a background thread.
    private MediaLoaderTask mediaLoaderTask;

    public MediaLoader(Context context) {
        this.context = context;
    }

    /**
     * Loads custom videos based on the Intent or load the default video. See the Javadoc for this
     * class for information on generating a custom intent via adb.
     */
    public void handleIntent(Intent intent) {
        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.
        // Note that this sample doesn't cancel any pending mediaLoaderTasks since it assumes only one
        // Intent will ever be fired for a single Activity lifecycle.
        mediaLoaderTask = new MediaLoaderTask();
        mediaLoaderTask.execute(intent);
    }

    /** Notifies MediaLoader that GL components have initialized. */
    public void onGlSceneReady(SceneRenderer sceneRenderer) {
        this.sceneRenderer = sceneRenderer;
        displayWhenReady();
    }

    /**
     * Helper class to media loading. This accesses the disk and decodes images so it needs to run in
     * the background.
     */
    private class MediaLoaderTask extends AsyncTask<Intent, Void, Void> {

        public MediaLoaderTask() {
        }

        @Override
        protected Void doInBackground(Intent... intent) {

            // Extract the stereoFormat from the Intent's extras.
            int stereoFormat = intent[0].getIntExtra(MEDIA_FORMAT_KEY, Mesh.MEDIA_MONOSCOPIC);
            if (stereoFormat != Mesh.MEDIA_STEREO_LEFT_RIGHT
                    && stereoFormat != Mesh.MEDIA_STEREO_TOP_BOTTOM) {
                stereoFormat = Mesh.MEDIA_MONOSCOPIC;
            }

            mesh = Mesh.createUvSphere(
                    SPHERE_RADIUS_METERS, DEFAULT_SPHERE_ROWS, DEFAULT_SPHERE_COLUMNS,
                    DEFAULT_SPHERE_VERTICAL_DEGREES, DEFAULT_SPHERE_HORIZONTAL_DEGREES,
                    stereoFormat);

            // Load the appropriate media from disk.
            try {
                // Yariv: 16k works in phone, 7k in android studio
                // Checks if we ar in AR. if so, load the empty jpg. If in VR load the correct jpg.
                try {
                    Class.forName( "MediaLoader" );
                    if(PracticeAr.inAR) {
                        mediaImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.empty);
                    }
                    else{
                        mediaImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.goegap_10k);
                    }
                } catch( ClassNotFoundException e ) {
                    mediaImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.goegap_10k);
                }

            } catch (Exception e){
                e.printStackTrace();
            }

            displayWhenReady();
            return null;
        }

    }

    /**
     * Creates the 3D scene and load the media after sceneRenderer & mediaPlayer are ready. This can
     * run on the GL Thread or a background thread.
     */
    @AnyThread
    private synchronized void displayWhenReady() {


        if ((errorText == null && mediaImage == null) || sceneRenderer == null) {
            // Wait for everything to be initialized.
            return;
        }

        // The important methods here are the setSurface & lockCanvas calls. These will have to happen
        // after the GLView is created.
        if (mediaImage != null) {
            // For images, acquire the displaySurface and draw the bitmap to it. Since our Mesh class uses
            // an GL_TEXTURE_EXTERNAL_OES texture, it's possible to perform this decoding and rendering of
            // a bitmap in the background without stalling the GL thread. If the Mesh used a standard
            // GL_TEXTURE_2D, then it's possible to stall the GL thread for 100+ ms during the
            // glTexImage2D call when loading 4k x 4k panoramas and copying the bitmap's data.
            displaySurface = sceneRenderer.createDisplay(
                    mediaImage.getWidth(), mediaImage.getHeight(), mesh);
            Canvas c = displaySurface.lockCanvas(null);
            c.drawBitmap(mediaImage, 0, 0, null);
            displaySurface.unlockCanvasAndPost(c);
        }
    }
}
