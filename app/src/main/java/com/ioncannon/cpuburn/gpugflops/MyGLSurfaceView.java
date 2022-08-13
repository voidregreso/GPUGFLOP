package com.ioncannon.cpuburn.gpugflops;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLSurfaceView extends GLSurfaceView {
    public MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context, boolean scalar, boolean fp32) {
        super(context);
        setEGLContextClientVersion(2);
        this.mRenderer = new MyGLRenderer(scalar, fp32);
        setRenderer(this.mRenderer);
        setRenderMode(0);
    }

    public void updateRender() {
        requestRender();
    }

    public void setCont() {
        setRenderMode(1);
    }

    public void setStop() {
        setRenderMode(0);
    }
}
