package com.ioncannon.cpuburn.gpugflops;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements Renderer {
    private static final String TAG = "MyGLRenderer";
    public int TotalFrames = 0;
    public double endTime;
    private boolean fp32mode;
    private float mAngle;
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private Square mSquare;
    private final float[] mViewMatrix = new float[16];
    public int nowframe = 0;
    private boolean scalar;
    public double startTime;

    public MyGLRenderer(boolean scalarin, boolean fp32) {
        this.scalar = scalarin;
        this.fp32mode = fp32;
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        this.mSquare = new Square(this.scalar, this.fp32mode);
    }

    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];
        if (this.nowframe == 0) {
            this.startTime = (double) System.currentTimeMillis();
        }
        GLES20.glClear(16640);
        Matrix.setLookAtM(this.mViewMatrix, 0, 0.0f, 0.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(this.mMVPMatrix, 0, this.mProjectionMatrix, 0, this.mViewMatrix, 0);
        this.mSquare.draw(this.mMVPMatrix);
        Matrix.setRotateM(this.mRotationMatrix, 0, this.mAngle, 0.0f, 0.0f, 1.0f);
        Matrix.multiplyMM(scratch, 0, this.mMVPMatrix, 0, this.mRotationMatrix, 0);
        this.nowframe++;
        this.endTime = (double) System.currentTimeMillis();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = ((float) width) / ((float) height);
        Matrix.frustumM(this.mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 3.0f, 7.0f);
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public static void checkGlError(String glOperation) {
        int error = GLES20.glGetError();
        if (error != 0) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
