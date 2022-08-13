package com.ioncannon.cpuburn.gpugflops;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;

public class CPUBurnThread extends HandlerThread implements Callback {
    public static final int TYPE_FINISHED = 1;
    public static final int TYPE_RESUME = 2;
    private int defaultloop = 16384;
    private int intv;
    public boolean isRunning;
    private int lastloop;
    private float lastsec;
    private Handler mBurnHandler;
    private Handler mUIHandler;
    private int mask;
    private int mode;
    private int sz;
    private int threadNum;

    public native String getStringFromNative(int mode, int threadNum, int per_core_data_size, int loop_duration, int spec_cpu_core);

    static {
        System.loadLibrary("NeonFPJni");
    }

    public CPUBurnThread(String name) {
        super(name);
    }

    /* Access modifiers changed, original: protected */
    public void onLooperPrepared() {
        super.onLooperPrepared();
        this.mBurnHandler = new Handler(getLooper(), this);
    }

    public Handler getBurnHandler() {
        return this.mBurnHandler;
    }

    public CPUBurnThread setUIHandler(Handler UIHandler) {
        this.mUIHandler = UIHandler;
        return this;
    }

    public void setPara(int[] parameter) {
        this.mode = parameter[0];
        this.threadNum = parameter[1];
        this.sz = parameter[2];
        this.intv = parameter[3];
        this.mask = parameter[4];
    }

    public boolean handleMessage(Message msg) {
        String res = "1:0";
        int myloop = getAdjustLoop(this.intv);
        switch (this.mode) {
            case 0:
                clearLast();
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                res = getStringFromNative(this.mode, this.threadNum + 1, this.sz + 1, myloop, this.mask);
                break;
            default:
                clearLast();
                break;
        }
        this.lastsec = Float.parseFloat(res.substring(0, res.indexOf(58)));
        this.lastsec *= 2.0f;
        this.lastloop = myloop;
        Message finishMsg = this.mUIHandler.obtainMessage(1);
        finishMsg.getData().putString("result", res);
        this.mUIHandler.sendMessage(finishMsg);
        if (this.isRunning) {
            this.mBurnHandler.sendEmptyMessage(2);
        }
        return true;
    }

    private void clearLast() {
        this.lastloop = 0;
        this.lastsec = 0.0f;
    }

    private int getAdjustLoop(int intv) {
        int[] szlist = new int[]{1, 8, 64, 256, 1024, 4096, 16384};
        int myintv = new int[]{1, 2, 4, 6, 10, 16, 30, 60}[intv];
        if (this.lastloop != 0) {
            float newloop = ((float) (this.lastloop * myintv)) / this.lastsec;
            if (newloop < 1.0f) {
                newloop = 1.0f;
            }
            return (int) newloop;
        } else if (this.mode == 4) {
            return this.defaultloop;
        } else {
            return this.defaultloop / szlist[this.sz];
        }
    }

    public boolean quitSafely() {
        this.mUIHandler = null;
        return super.quitSafely();
    }
}
