package com.bgu.snipertrainer.video360.rendering;

import static com.bgu.snipertrainer.video360.rendering.Utils.checkGlError;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.GLES20;
import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controls and renders the GL Scene.
 *
 * <p>This class is shared between MonoscopicView. It renders the display mesh, UI
 * and controller reticle as required.
 **/

public class SceneRenderer {
    private static final String TAG = "SceneRenderer";

    // This is the primary interface between the Media Player and the GL Scene.
    private SurfaceTexture displayTexture;
    private final AtomicBoolean frameAvailable = new AtomicBoolean();
    // Used to notify clients that displayTexture has a new frame. This requires synchronized access.
    @Nullable
    private OnFrameAvailableListener externalFrameListener;

    // GL components for the mesh that display the media. displayMesh should only be accessed on the
    // GL Thread, but requestedDisplayMesh needs synchronization.
    @Nullable
    private Mesh displayMesh;
    @Nullable
    private Mesh requestedDisplayMesh;
    private int displayTexId;

    // These are only valid if createForVR() has been called. In the 2D Activity, these are null
    // since the UI is rendered in the standard Android layout.


    /**
     * Constructs the SceneRenderer with the given values.
     */
    /* package */ SceneRenderer(SurfaceTexture.OnFrameAvailableListener externalFrameListener) {
        this.externalFrameListener = externalFrameListener;
    }

    /**
     * Creates a SceneRenderer for 2D but does not initialize it. {@link #glInit()} is used to finish
     * initializing the object on the GL thread.
     */
    public static SceneRenderer createFor2D() {
        return new SceneRenderer(null);
    }

    /**
     * Creates a SceneRenderer for VR but does not initialize it. {@link #glInit()} is used to finish
     * initializing the object on the GL thread.
     *
     * Performs initialization on the GL thread. The scene isn't fully initialized until
     * glConfigureScene() completes successfully.
     */
    public void glInit() {
        checkGlError();

        // Set the background frame color. This is only visible if the display mesh isn't a full sphere.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        checkGlError();

        // Create the texture used to render each frame of video.
        displayTexId = Utils.glCreateExternalTexture();
        displayTexture = new SurfaceTexture(displayTexId);
        checkGlError();

        // When the video decodes a new frame, tell the GL thread to update the image.
        displayTexture.setOnFrameAvailableListener(
                new OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        frameAvailable.set(true);

                        synchronized (SceneRenderer.this) {
                            if (externalFrameListener != null) {
                                externalFrameListener.onFrameAvailable(surfaceTexture);
                            }
                        }
                    }
                });

    }

    /**
     * Creates the Surface & Mesh used by the MediaPlayer to render video.
     *
     * @param width passed to {@link SurfaceTexture#setDefaultBufferSize(int, int)}
     * @param height passed to {@link SurfaceTexture#setDefaultBufferSize(int, int)}
     * @param mesh {@link Mesh} used to display video
     * @return a Surface that can be passed to {@link android.media.MediaPlayer#setSurface(Surface)}
     */
    @AnyThread
    public synchronized @Nullable Surface createDisplay(int width, int height, Mesh mesh) {
        if (displayTexture == null) {
            Log.e(TAG, ".createDisplay called before GL Initialization completed.");
            return null;
        }

        requestedDisplayMesh = mesh;

        displayTexture.setDefaultBufferSize(width, height);
        return new Surface(displayTexture);
    }

    /**
     * Configures any late-initialized components.
     *
     * <p>Since the creation of the Mesh can depend on disk access, this configuration needs to run
     * during each drawFrame to determine if the Mesh is ready yet. This also supports replacing an
     * existing mesh while the app is running.
     *
     * @return true if the scene is ready to be drawn
     */
    private synchronized boolean glConfigureScene() {
        if (displayMesh == null && requestedDisplayMesh == null) {
            // The scene isn't ready and we don't have enough information to configure it.
            return false;
        }

        // The scene is ready and we don't need to change it so we can glDraw it.
        if (requestedDisplayMesh == null) {
            return true;
        }

        // Configure or reconfigure the scene.
        if (displayMesh != null) {
            // Reconfiguration.
            displayMesh.glShutdown();
        }

        displayMesh = requestedDisplayMesh;
        requestedDisplayMesh = null;
        displayMesh.glInit(displayTexId);

        return true;
    }

    /**
     * Draws the scene with a given eye pose and type.
     *
     * @param viewProjectionMatrix 16 element GL matrix.
     * @param eyeType an {@link com.google.vr.sdk.base.Eye.Type} value
     */
    public void glDrawFrame(float[] viewProjectionMatrix, int eyeType) {
        if (!glConfigureScene()) {
            // displayMesh isn't ready.
            return;
        }

        // glClear isn't strictly necessary when rendering fully spherical panoramas, but it can improve
        // performance on tiled renderers by causing the GPU to discard previous data.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        checkGlError();

        // The uiQuad uses alpha.
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        if (frameAvailable.compareAndSet(true, false)) {
            displayTexture.updateTexImage();
            checkGlError();
        }

        displayMesh.glDraw(viewProjectionMatrix, eyeType);

    }
}
