package com.ioncannon.cpuburn.gpugflops;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;

public class PowerRecThread extends HandlerThread implements Callback {
    private Handler mUIHandler;

    public PowerRecThread(String name) {
        super(name);
    }

    /* Access modifiers changed, original: protected */
    public void onLooperPrepared() {
        super.onLooperPrepared();
    }

    public Handler getUIHandler() {
        return this.mUIHandler;
    }

    public PowerRecThread setUIHandler(Handler UIHandler) {
        this.mUIHandler = UIHandler;
        return this;
    }

    public boolean handleMessage(Message msg) {
        if (msg == null || msg.getData() == null) {
            return false;
        }
        return true;
    }

    public boolean quitSafely() {
        this.mUIHandler = null;
        return super.quitSafely();
    }
}
