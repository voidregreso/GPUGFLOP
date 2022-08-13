package com.ioncannon.cpuburn.gpugflops;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.InputDeviceCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class CPUBurnActivity extends AppCompatActivity implements Callback {
    private float CPUavggFlops = 0.0f;
    private float CPUgFlops = 0.0f;
    private float CPUmaxgFlops = 0.0f;
    private float CPUsumfFlops = 0.0f;
    private float GPUavggFlops = 0.0f;
    private float GPUgFlops = 0.0f;
    private float GPUmaxgFlops = 0.0f;
    private float GPUsumgFlops = 0.0f;
    private int TotalFrames;
    private boolean batteryOK = false;
    private boolean burning = false;
    private Button buttonCPU;
    private Button buttonIntv;
    private Button buttonLock;
    private Button buttonSize;
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonThread;
    private boolean charging = false;
    private LineChartView chart = null;
    private LineChartView chartPower = null;
    private CheckBox checkBoxGPU;
    private CheckBox checkBoxGPUScalar;
    private CheckBox checkBoxShowPower;
    private CheckBox[] checkTemp;
    private double costTime;
    private int count = 0;
    private int cpucount = 0;
    private boolean cpumode = false;
    private String currentPath = "";
    private float currentnow = 0.0f;
    Runnable doBurn = new Runnable() {
        public void run() {
            CPUBurnActivity.this.mCPUBurnThread.setPara(CPUBurnActivity.this.parameters);
            CPUBurnActivity.this.mCPUBurnThread.isRunning = true;
            CPUBurnActivity.this.mCPUBurnThread.getBurnHandler().sendEmptyMessage(0);
        }
    };
    Runnable doBurnDummy = new Runnable() {
        public void run() {
            if (CPUBurnActivity.this.powernowcount > 0) {
                CPUBurnActivity.this.powernowavg = CPUBurnActivity.this.powernowsum / ((float) CPUBurnActivity.this.powernowcount);
            }
            if (CPUBurnActivity.this.batteryOK) {
                if (CPUBurnActivity.this.powercount > CPUBurnActivity.this.powercountuse) {
                    CPUBurnActivity.this.poweravg = (CPUBurnActivity.this.powersumall - CPUBurnActivity.this.powersuamlluse) / ((float) (CPUBurnActivity.this.powercount - CPUBurnActivity.this.powercountuse));
                }
                if (CPUBurnActivity.this.powernowcount > 0) {
                    CPUBurnActivity.this.powernowavg = CPUBurnActivity.this.powernowsum / ((float) CPUBurnActivity.this.powernowcount);
                }
                CPUBurnActivity.this.textViewPower.setText("Power(mW): MAX " + String.format("%.0f", new Object[]{Float.valueOf(CPUBurnActivity.this.powermax)}) + " AVG " + String.format("%.0f", new Object[]{Float.valueOf(CPUBurnActivity.this.poweravg)}) + " Now " + String.format("%.0f", new Object[]{Float.valueOf(CPUBurnActivity.this.powernow)}));
            } else {
                CPUBurnActivity.this.textViewPower.setText("Unable to get power info");
            }
            CPUBurnActivity.this.UpdateChart();
            CPUBurnActivity.this.powernowsum = 0.0f;
            CPUBurnActivity.this.powernowcount = 0;
            if (!CPUBurnActivity.this.burning) {
                return;
            }
            if (CPUBurnActivity.this.count < 4) {
                CPUBurnActivity.this.handler.postDelayed(CPUBurnActivity.this.doBurnDummy, 1500);
                return;
            }
            if (!(CPUBurnActivity.this.gpumode || CPUBurnActivity.this.cpumode)) {
                CPUBurnActivity.this.handler.postDelayed(CPUBurnActivity.this.doBurnDummy, 1000);
            }
            if (CPUBurnActivity.this.gpumode) {
                CPUBurnActivity.this.handler.post(CPUBurnActivity.this.doBurnGPU);
                CPUBurnActivity.this.mGLView.setCont();
            }
            if (CPUBurnActivity.this.cpumode) {
                CPUBurnActivity.this.handler3.post(CPUBurnActivity.this.doBurn);
            }
        }
    };
    Runnable doBurnGPU = new Runnable() {
        public void run() {
            int mtime = new int[]{1, 2, 4, 6, 10, 16, 30, 60}[CPUBurnActivity.this.parameters[3]] * 500;
            int nf = CPUBurnActivity.this.mGLView.mRenderer.nowframe;
            double nc = CPUBurnActivity.this.mGLView.mRenderer.endTime - CPUBurnActivity.this.mGLView.mRenderer.startTime;
            if (nf > CPUBurnActivity.this.nframes + CPUBurnActivity.this.updateCount) {
                CPUBurnActivity.this.gpucount = CPUBurnActivity.this.gpucount + 1;
                CPUBurnActivity.this.GPUgFlops = ((float) (((CPUBurnActivity.this.pixelHeight * CPUBurnActivity.this.pixelWidth) * (nf - CPUBurnActivity.this.nframes)) * 2)) / (25.0f * ((float) (nc - CPUBurnActivity.this.costTime)));
                CPUBurnActivity.this.updateCount = (int) (((double) (CPUBurnActivity.this.updateCount * mtime)) / (nc - CPUBurnActivity.this.costTime));
                if (CPUBurnActivity.this.updateCount < 2) {
                    CPUBurnActivity.this.updateCount = 2;
                }
                CPUBurnActivity.this.nframes = nf;
                CPUBurnActivity.this.costTime = nc;
                if (CPUBurnActivity.this.GPUgFlops > CPUBurnActivity.this.GPUmaxgFlops) {
                    CPUBurnActivity.this.GPUmaxgFlops = CPUBurnActivity.this.GPUgFlops;
                }
                if (CPUBurnActivity.this.gpucount > 6) {
                    CPUBurnActivity.this.GPUsumgFlops = CPUBurnActivity.this.GPUsumgFlops + CPUBurnActivity.this.GPUgFlops;
                    CPUBurnActivity.this.GPUavggFlops = CPUBurnActivity.this.GPUsumgFlops / ((float) (CPUBurnActivity.this.gpucount - 6));
                    CPUBurnActivity.this.textViewGPUGFLOPS.setText("GPU GFLOPS: MAX " + String.format("%.1f", new Object[]{Float.valueOf(CPUBurnActivity.this.GPUmaxgFlops)}) + " AVG " + String.format("%.1f", new Object[]{Float.valueOf(CPUBurnActivity.this.GPUavggFlops)}) + " Now " + String.format("%.1f", new Object[]{Float.valueOf(CPUBurnActivity.this.GPUgFlops)}));
                }
                if (CPUBurnActivity.this.batteryOK) {
                    if (CPUBurnActivity.this.powercount > CPUBurnActivity.this.powercountuse) {
                        CPUBurnActivity.this.poweravg = (CPUBurnActivity.this.powersumall - CPUBurnActivity.this.powersuamlluse) / ((float) (CPUBurnActivity.this.powercount - CPUBurnActivity.this.powercountuse));
                    }
                    if (CPUBurnActivity.this.powernowcount > 0) {
                        CPUBurnActivity.this.powernowavg = CPUBurnActivity.this.powernowsum / ((float) CPUBurnActivity.this.powernowcount);
                    }
                    CPUBurnActivity.this.textViewPower.setText("Power(mW): MAX " + String.format("%.0f", new Object[]{Float.valueOf(CPUBurnActivity.this.powermax)}) + " AVG " + String.format("%.0f", new Object[]{Float.valueOf(CPUBurnActivity.this.poweravg)}) + " Now " + String.format("%.0f", new Object[]{Float.valueOf(CPUBurnActivity.this.powernow)}));
                } else {
                    CPUBurnActivity.this.textViewPower.setText("Unable to get power info");
                }
                if (CPUBurnActivity.this.GPUgFlops > 0.0f) {
                    CPUBurnActivity.this.UpdateChart();
                }
                CPUBurnActivity.this.powernowsum = 0.0f;
                CPUBurnActivity.this.powernowcount = 0;
            }
            if (CPUBurnActivity.this.burning) {
                CPUBurnActivity.this.handler.postDelayed(CPUBurnActivity.this.doBurnGPU, 20);
            }
        }
    };
    Runnable doGetPower = new Runnable() {
        public void run() {
            CPUBurnActivity.this.getPower();
            if (CPUBurnActivity.this.gpucount == 5) {
                CPUBurnActivity.this.powercountuse = CPUBurnActivity.this.powercount;
                CPUBurnActivity.this.powersuamlluse = CPUBurnActivity.this.powersumall;
            }
            CPUBurnActivity.this.powercount = CPUBurnActivity.this.powercount + 1;
            CPUBurnActivity.this.powersumall = CPUBurnActivity.this.powersumall + CPUBurnActivity.this.powernow;
            CPUBurnActivity.this.powernowcount = CPUBurnActivity.this.powernowcount + 1;
            CPUBurnActivity.this.powernowsum = CPUBurnActivity.this.powernowsum + CPUBurnActivity.this.powernow;
            if (CPUBurnActivity.this.powernow > CPUBurnActivity.this.powermax) {
                CPUBurnActivity.this.powermax = CPUBurnActivity.this.powernow;
            }
            if (CPUBurnActivity.this.burning) {
                CPUBurnActivity.this.handler2.postDelayed(CPUBurnActivity.this.doGetPower, 200);
            }
        }
    };
    private long endtime;
    private boolean fp32mode = true;
    private boolean fpmode = true;
    private boolean gpualu = false;
    private int gpucount = 0;
    private boolean gpumode = false;
    Handler handler = new Handler();
    Handler handler2 = new Handler();
    Handler handler3 = new Handler();
    private boolean is3660 = false;
    private boolean isMTK = false;
    private LinearLayout ll;
    private BatteryManager mBatteryManager;
    private CPUBurnThread mCPUBurnThread;
    private MyGLSurfaceView mGLView;
    private Handler mUIHandler;
    private ViewPager mViewPager;
    private int nframes;
    private boolean nightmode = false;
    private int[] parameters = new int[5];
    private int pixelHeight = 0;
    private int pixelWidth = 0;
    private float poweravg = 0.0f;
    private int powercount = 0;
    private int powercountuse = 0;
    private float powermax = 0.0f;
    private float powernow = 0.0f;
    private float powernowavg = 0.0f;
    private int powernowcount = 0;
    private float powernowsum = 0.0f;
    private float powersuamlluse = 0.0f;
    private float powersumall = 0.0f;
    private RadioButton rbFP16;
    private RadioButton rbFP32;
    private boolean scalarmode = false;
    private float secs = 1.0f;
    private int[] sensorTemp;
    private int[] sensorTempAll;
    private ArrayList<List<PointValue>> sensorTempSave;
    private int sfactor = 10;
    private long starttime;
    private String[] tempName;
    private String[] tempNameAll;
    private int tempNum;
    private int tempNumAll;
    private String[] tempPath;
    private String[] tempPathAll;
    private boolean[] tempUsed;
    private int[] temppara = new int[5];
    private TextView textViewCPUGFLOPS;
    private TextView textViewGPUGFLOPS;
    private TextView textViewInfo;
    private TextView textViewPower;
    private int updateCount;
    List<PointValue> valuesCPU = new ArrayList();
    List<PointValue> valuesGPU = new ArrayList();
    List<PointValue> valuesPower = new ArrayList();
    private String voltPath = "";
    private float voltnow = 0.0f;

    public void burnStart(View view) {
        this.nframes = 0;
        this.TotalFrames = 2000000;
        this.updateCount = 20;
        this.pixelHeight = this.mGLView.getHeight();
        this.pixelWidth = this.mGLView.getWidth();
        this.mGLView.mRenderer.nowframe = 0;
        this.mGLView.mRenderer.TotalFrames = this.TotalFrames;
        this.buttonCPU.setEnabled(false);
        this.buttonThread.setEnabled(false);
        this.buttonSize.setEnabled(false);
        this.buttonIntv.setEnabled(false);
        this.buttonLock.setEnabled(false);
        this.checkBoxGPU.setEnabled(false);
        this.checkBoxGPUScalar.setEnabled(false);
        this.rbFP32.setEnabled(false);
        this.rbFP16.setEnabled(false);
        this.buttonStart.setEnabled(false);
        if (this.parameters[0] > 0) {
            this.cpumode = true;
        } else {
            this.cpumode = false;
        }
        if (this.checkBoxGPU.isChecked()) {
            this.gpumode = true;
            this.gpualu = true;
        } else {
            this.gpumode = false;
            this.gpualu = false;
        }
        if (this.checkBoxGPUScalar.isChecked()) {
            this.scalarmode = true;
        } else {
            this.scalarmode = false;
        }
        if (this.rbFP32.isChecked()) {
            this.fp32mode = true;
        } else {
            this.fp32mode = false;
        }
        this.ll.removeAllViews();
        this.mGLView = new MyGLSurfaceView(this, this.scalarmode, this.fp32mode);
        this.ll.addView(this.mGLView);
        this.burning = true;
        this.starttime = System.currentTimeMillis();
        this.buttonStop.setText("Stop");
        ClearChart();
        this.handler.postDelayed(this.doBurnDummy, 100);
        if (this.batteryOK) {
            this.handler2.post(this.doGetPower);
        }
    }

    public void burnStop(View view) {
        if (this.burning) {
            this.mGLView.setStop();
            this.burning = false;
            this.mCPUBurnThread.isRunning = false;
            Handler burnHandler = this.mCPUBurnThread.getBurnHandler();
            CPUBurnThread cPUBurnThread = this.mCPUBurnThread;
            burnHandler.removeMessages(2);
            this.endtime = System.currentTimeMillis();
            this.buttonCPU.setEnabled(true);
            this.buttonThread.setEnabled(true);
            this.buttonSize.setEnabled(true);
            this.buttonIntv.setEnabled(true);
            this.buttonLock.setEnabled(true);
            this.checkBoxGPU.setEnabled(true);
            this.checkBoxGPUScalar.setEnabled(true);
            this.rbFP32.setEnabled(true);
            this.rbFP16.setEnabled(true);
            this.buttonStart.setEnabled(true);
            this.buttonStop.setText("Mostrar la temperatura máxima");
            return;
        }
        Builder builder = new Builder(this);
        String[] items = new String[]{"None", "NEON FP32", "MIX1", "MIX2", "DMIPS", "SGEMM-A57", "SGEMM-A53", "SGEMM-A55", "FFT", "LINPACK"};
        String title = "Run: " + "CPU " + items[this.parameters[0]];
        if (this.gpualu) {
            title = title + "GPU ALU";
        }
        builder.setTitle(title);
        String msg = "" + "Duration: " + ((this.endtime - this.starttime) / 1000) + "s\n\n";
        if (this.batteryOK) {
            msg = msg + "Power MAX(mW):   " + this.powermax + "\n";
        }
        for (int i = 0; i < this.tempNum; i++) {
            msg = msg + this.tempName[i] + " (MAX, °C):   " + getMaxData((List) this.sensorTempSave.get(i)) + "\n";
        }
        builder.setMessage(msg);
        builder.create().show();
    }

    private int getMaxData(List<PointValue> v) {
        float maxdata = 0.0f;
        for (int i = 0; i < v.size(); i++) {
            float tempdata = ((PointValue) v.get(i)).getY();
            if (tempdata < 0.0f) {
                tempdata = 0.0f - tempdata;
            }
            if (tempdata > maxdata) {
                maxdata = tempdata;
            }
        }
        return (int) maxdata;
    }

    public void doSetCPUMode(View view) {
        new Builder(this).setIcon(R.drawable.ic_launcher).setTitle("CPU Mode\nCPUModo de prueba de esfuerzo").setSingleChoiceItems(
            new String[]{"None", "NEON FP32 float", "MIX1", "MIX2", "DMIPS integer", "Multiplicación matricial SGEMM - Optim para A57", 
            "Multiplicación matricial SGEMM - Optim para A53", "Multiplicación matricial SGEMM - Optim para A55", 
            "FFT", "LINPACK-Perfección en curso"}, this.parameters[0], new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CPUBurnActivity.this.temppara[0] = which;
            }
        }).setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CPUBurnActivity.this.parameters[0] = CPUBurnActivity.this.temppara[0];
                CPUBurnActivity.this.updateBurnInfo();
                dialog.dismiss();
            }
        }).create().show();
    }

    public void doSetThread(View view) {
        new Builder(this).setIcon(R.drawable.ic_launcher).setTitle("Threads").setSingleChoiceItems(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, this.parameters[1], new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CPUBurnActivity.this.temppara[1] = which;
            }
        }).setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CPUBurnActivity.this.parameters[1] = CPUBurnActivity.this.temppara[1];
                CPUBurnActivity.this.updateBurnInfo();
                dialog.dismiss();
            }
        }).create().show();
    }

    public void doSetDataset(View view) {
        new Builder(this).setIcon(R.drawable.ic_launcher).setTitle("Thread Workload Size").setSingleChoiceItems(new String[]{"128B", "1KiB", "8KiB", "32KiB --- L1 Cache", "128KiB", "512KiB --- L2 Cache", "2MiB--- MEM"}, this.parameters[2], new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CPUBurnActivity.this.temppara[2] = which;
            }
        }).setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CPUBurnActivity.this.parameters[2] = CPUBurnActivity.this.temppara[2];
                CPUBurnActivity.this.updateBurnInfo();
                dialog.dismiss();
            }
        }).create().show();
    }

    public void doSetIntv(View view) {
        new Builder(this).setIcon(R.drawable.ic_launcher).setTitle("Refresh Time\n(más lento, mayor la presión)").setSingleChoiceItems(new String[]{"0.5s", "1s", "2s", "3s", "5s", "8s", "15s", "30s"}, this.parameters[3], new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CPUBurnActivity.this.temppara[3] = which;
            }
        }).setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CPUBurnActivity.this.parameters[3] = CPUBurnActivity.this.temppara[3];
                CPUBurnActivity.this.updateBurnInfo();
                dialog.dismiss();
            }
        }).create().show();
    }

    private static boolean getBit(int num, int i) {
        return ((1 << i) & num) != 0;
    }

    private static int setBit1(int num, int i) {
        return (1 << i) | num;
    }

    private static int setBit0(int num, int i) {
        return num & ((1 << i) ^ -1);
    }

    public void doSetCore(View view) {
        int cpunum = Runtime.getRuntime().availableProcessors();
        String[] items = new String[(cpunum + 1)];
        final boolean[] checkedItems = new boolean[(cpunum + 1)];
        items[0] = "All";
        for (int i = 0; i < cpunum; i++) {
            items[i + 1] = "CPU" + i;
            checkedItems[i + 1] = getBit(this.parameters[4], i);
        }
        if (this.parameters[4] == 0) {
            checkedItems[0] = true;
        } else {
            checkedItems[0] = false;
        }
        new Builder(this).setIcon(R.drawable.ic_launcher).setTitle("Select CPU Cores").setMultiChoiceItems(items, checkedItems, new OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedItems[which] = isChecked;
            }
        }).setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (checkedItems[0]) {
                    CPUBurnActivity.this.parameters[4] = 0;
                } else {
                    CPUBurnActivity.this.parameters[4] = 0;
                    for (int i = 1; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            CPUBurnActivity.this.parameters[4] = CPUBurnActivity.setBit1(CPUBurnActivity.this.parameters[4], i - 1);
                        } else {
                            CPUBurnActivity.this.parameters[4] = CPUBurnActivity.setBit0(CPUBurnActivity.this.parameters[4], i - 1);
                        }
                    }
                }
                CPUBurnActivity.this.updateBurnInfo();
                dialog.dismiss();
            }
        }).create().show();
    }

    public void doSetSensor(View view) {
        int sensorNum = this.tempNumAll;
        String[] items = new String[this.tempNumAll];
        final boolean[] checkedItems = new boolean[this.tempNumAll];
        for (int i = 0; i < this.tempNumAll; i++) {
            items[i] = this.tempNameAll[i] + ":" + this.sensorTempAll[i];
            checkedItems[i] = this.tempUsed[i];
        }
        new Builder(this).setIcon(R.drawable.ic_launcher).setTitle("Select Temp Sensors").setMultiChoiceItems(items, checkedItems, new OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedItems[which] = isChecked;
            }
        }).setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int i;
                CPUBurnActivity.this.tempNum = 0;
                for (i = 0; i < CPUBurnActivity.this.tempNumAll; i++) {
                    CPUBurnActivity.this.tempUsed[i] = checkedItems[i];
                    if (CPUBurnActivity.this.tempUsed[i]) {
                        CPUBurnActivity.this.tempNum = CPUBurnActivity.this.tempNum + 1;
                        if (CPUBurnActivity.this.tempNum >= 8) {
                            break;
                        }
                    }
                }
                CPUBurnActivity.this.tempName = new String[CPUBurnActivity.this.tempNum];
                CPUBurnActivity.this.tempPath = new String[CPUBurnActivity.this.tempNum];
                CPUBurnActivity.this.sensorTemp = new int[CPUBurnActivity.this.tempNum];
                CPUBurnActivity.this.tempNum = 0;
                for (i = 0; i < 8; i++) {
                    CPUBurnActivity.this.checkTemp[i].setVisibility(4);
                }
                for (i = 0; i < CPUBurnActivity.this.tempNumAll; i++) {
                    if (CPUBurnActivity.this.tempUsed[i]) {
                        CPUBurnActivity.this.tempName[CPUBurnActivity.this.tempNum] = CPUBurnActivity.this.tempNameAll[i];
                        CPUBurnActivity.this.tempPath[CPUBurnActivity.this.tempNum] = CPUBurnActivity.this.tempPathAll[i];
                        CPUBurnActivity.this.sensorTemp[CPUBurnActivity.this.tempNum] = CPUBurnActivity.this.sensorTempAll[i];
                        CPUBurnActivity.this.checkTemp[CPUBurnActivity.this.tempNum].setText(CPUBurnActivity.this.tempName[CPUBurnActivity.this.tempNum] + ":" + CPUBurnActivity.this.sensorTemp[CPUBurnActivity.this.tempNum]);
                        CPUBurnActivity.this.checkTemp[CPUBurnActivity.this.tempNum].setVisibility(0);
                        CPUBurnActivity.this.checkTemp[CPUBurnActivity.this.tempNum].setEnabled(true);
                        CPUBurnActivity.this.tempNum = CPUBurnActivity.this.tempNum + 1;
                    }
                }
                dialog.dismiss();
            }
        }).create().show();
    }

    public void updateCK(View view) {
        ShowChartPower();
    }

    private int getVolt() {
        return registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra("voltage", -1);
    }

    private void getPower() {
        int i;
        if (this.is3660) {
            this.voltnow = (float) getVolt();
        } else {
            this.voltnow = readFileByLines(this.voltPath);
        }
        for (i = 0; i < 10; i++) {
            if (this.voltnow > 5.0f) {
                this.voltnow /= 10.0f;
            }
        }
        if (this.is3660) {
            this.currentnow = (float) this.mBatteryManager.getIntProperty(2);
        } else {
            this.currentnow = readFileByLines(this.currentPath);
        }
        this.currentnow = Math.abs(this.currentnow);
        if (this.isMTK) {
            this.currentnow *= 100.0f;
        }
        if (this.currentnow > 5000.0f) {
            this.currentnow /= 1000.0f;
        }
        this.powernow = (this.voltnow * this.currentnow) / 1000.0f;
        if (this.powernow < 10.0f) {
            this.powernow *= 1000.0f;
        }
        if (this.powernow < 10.0f) {
            this.powernow *= 1000.0f;
        }
        for (i = 0; i < 10; i++) {
            if (this.powernow > 20000.0f) {
                this.powernow /= 10.0f;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0031 A:{SYNTHETIC, Splitter:B:18:0x0031} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x003a A:{SYNTHETIC, Splitter:B:23:0x003a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static float readFileByLines(String fileName) {
        IOException e;
        Throwable th;
        float v = 0.0f;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            try {
                v = Float.parseFloat(reader2.readLine());
                reader2.close();
                if (reader2 != null) {
                    try {
                        reader2.close();
                        reader = reader2;
                    } catch (IOException e2) {
                        reader = reader2;
                    }
                }
            } catch (IOException e3) {
                e = e3;
                reader = reader2;
                try {
                    e.printStackTrace();
                    if (reader != null) {
                    }
                    return v / 1000.0f;
                } catch (Throwable th2) {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e4) {
                        }
                    }
                }
            } catch (Throwable th3) {
                reader = reader2;
                if (reader != null) {
                }
            }
        } catch (IOException e5) {
            e = e5;
            e.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e6) {
                }
            }
            return v / 1000.0f;
        }
        return v / 1000.0f;
    }

    
    public static int readInt(String fileName) {
        Throwable th;
        int v = 0;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            try {
                v = Integer.parseInt(reader2.readLine());
                reader2.close();
                if (reader2 != null) {
                    try {
                        reader2.close();
                        reader = reader2;
                    } catch (IOException e) {
                        reader = reader2;
                    }
                }
            } catch (IOException e2) {
                reader = reader2;
                if (reader != null) {
                }
                if (v > 150) {
                }
                if (v > 150) {
                }
                if (v > 150) {
                }
                if (v > 150) {
                }
                if (v > 150) {
                }
                if (v > 150) {
                }
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            if (v > 150) {
            }
            if (v > 150) {
            }
            if (v > 150) {
            }
            if (v > 150) {
            }
            if (v > 150) {
            }
            if (v > 150) {
            }
        } catch (Throwable th3) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e5) {
                }
            }
        }
        if (v > 150) {
            v /= 10;
        }
        if (v > 150) {
            v /= 10;
        }
        if (v > 150) {
            v /= 10;
        }
        if (v > 150) {
            v /= 10;
        }
        if (v > 150) {
            v /= 10;
        }
        if (v > 150) {
            return v / 10;
        }
        return v;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0026 A:{SYNTHETIC, Splitter:B:14:0x0026} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x002f A:{SYNTHETIC, Splitter:B:19:0x002f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String readStr(String fileName) {
        Throwable th;
        BufferedReader reader = null;
        String tempString = "";
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            try {
                tempString = reader2.readLine();
                reader2.close();
                if (reader2 != null) {
                    try {
                        reader2.close();
                        reader = reader2;
                    } catch (IOException e) {
                        reader = reader2;
                    }
                }
            } catch (IOException e2) {
                reader = reader2;
                if (reader != null) {
                }
                return tempString;
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            return tempString;
        } catch (Throwable th3) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e5) {
                }
            }
        }
        return tempString;
    }

    private void initChart() {
        Line line = new Line(this.valuesGPU).setColor(-16776961).setCubic(false);
        Line line1 = new Line(this.valuesCPU).setColor(SupportMenu.CATEGORY_MASK).setCubic(false);
        List<Line> lines = new ArrayList();
        lines.add(line);
        lines.add(line1);
        LineChartData data = new LineChartData();
        data.setLines(lines);
        data.setAxisXBottom(new Axis());
        data.setAxisYLeft(new Axis());
        this.chart.setLineChartData(data);
    }

    private void initChartPower() {
        Line line = new Line(this.valuesPower).setColor(SupportMenu.CATEGORY_MASK).setCubic(false);
        List<Line> lines = new ArrayList();
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);
        data.setAxisXBottom(new Axis());
        data.setAxisYLeft(new Axis());
        this.chartPower.setLineChartData(data);
    }

    private void ClearChart() {
        this.count = 0;
        this.cpucount = 0;
        this.gpucount = 0;
        this.GPUgFlops = 0.0f;
        this.GPUmaxgFlops = 0.0f;
        this.GPUavggFlops = 0.0f;
        this.GPUsumgFlops = 0.0f;
        this.CPUgFlops = 0.0f;
        this.CPUmaxgFlops = 0.0f;
        this.CPUsumfFlops = 0.0f;
        this.CPUavggFlops = 0.0f;
        this.secs = 1.0f;
        this.sfactor = 10;
        this.valuesGPU.clear();
        this.valuesGPU.add(new PointValue((float) this.gpucount, this.GPUgFlops));
        this.valuesCPU.clear();
        this.valuesCPU.add(new PointValue((float) this.cpucount, this.CPUgFlops));
        for (int i = 0; i < 8; i++) {
            ((List) this.sensorTempSave.get(i)).clear();
        }
        this.costTime = 0.0d;
        this.powernowsum = 0.0f;
        this.powernowcount = 0;
        this.powernowavg = 0.0f;
        this.powermax = 0.0f;
        this.powersumall = 0.0f;
        this.powercount = 0;
        this.poweravg = 0.0f;
        this.valuesPower.clear();
        this.valuesPower.add(new PointValue((float) this.powercount, this.powernowavg / 1000.0f));
    }

    private void UpdateChartGFLOPS() {
        if (this.gpumode) {
            this.valuesGPU.add(new PointValue((float) this.count, this.GPUgFlops));
        }
        if (this.cpumode) {
            if (this.fpmode) {
                this.valuesCPU.add(new PointValue((float) this.count, this.CPUgFlops));
            } else {
                this.valuesCPU.add(new PointValue((float) this.count, this.CPUgFlops / 100.0f));
            }
        }
        Line line = new Line(this.valuesGPU).setColor(-16776961).setCubic(false);
        line.setHasPoints(false);
        Line line1 = new Line(this.valuesCPU).setColor(SupportMenu.CATEGORY_MASK).setCubic(false);
        line1.setHasPoints(false);
        List<Line> lines = new ArrayList();
        lines.add(line);
        lines.add(line1);
        LineChartData data = new LineChartData();
        data.setLines(lines);
        data.setBaseValue(0.0f);
        Axis axisX = new Axis().setHasLines(false);
        axisX.setFormatter(new SimpleAxisValueFormatter(0));
        data.setAxisXBottom(axisX);
        Axis axisY = new Axis().setHasLines(true);
        axisY.setFormatter(new SimpleAxisValueFormatter(0));
        data.setAxisYLeft(axisY);
        this.chart.setLineChartData(data);
    }

    private int myColor(int n) {
        switch (n) {
            case 1:
                return this.nightmode ? -1 : ViewCompat.MEASURED_STATE_MASK;
            case 2:
                return InputDeviceCompat.SOURCE_ANY;
            case 3:
                return -16711936;
            case 4:
                return -65281;
            case 5:
                return -16711681;
            case 6:
                return -7829368;
            case 7:
                return -3355444;
            case 8:
                return this.nightmode ? -1 : ViewCompat.MEASURED_STATE_MASK;
            case 10:
                return SupportMenu.CATEGORY_MASK;
            case 11:
                return -16711936;
            case 12:
                return InputDeviceCompat.SOURCE_ANY;
            case 14:
                return -16711936;
            case 15:
                return InputDeviceCompat.SOURCE_ANY;
            default:
                return -16776961;
        }
    }

    private void UpdateChartPower() {
        Line line;
        List<Line> lines = new ArrayList();
        this.valuesPower.add(new PointValue((float) this.count, this.powernowavg / 1000.0f));
        if (this.batteryOK && this.checkBoxShowPower.isChecked()) {
            line = new Line(this.valuesPower).setColor(SupportMenu.CATEGORY_MASK).setCubic(false);
            line.setHasPoints(false);
            lines.add(line);
        }
        int sn = 0;
        for (int i = 0; i < this.tempNum; i++) {
            ((List) this.sensorTempSave.get(i)).add(new PointValue((float) this.count, (float) this.sensorTemp[i]));
            if (this.checkTemp[i].isChecked()) {
                line = new Line((List) this.sensorTempSave.get(i)).setColor(myColor(sn)).setCubic(false);
                line.setHasPoints(false);
                lines.add(line);
                sn++;
            }
        }
        LineChartData data = new LineChartData();
        data.setLines(lines);
        Axis axisX = new Axis().setHasLines(false);
        axisX.setFormatter(new SimpleAxisValueFormatter(0));
        data.setAxisXBottom(axisX);
        data.setBaseValue(0.0f);
        Axis axisY = new Axis().setHasLines(true);
        axisY.setFormatter(new SimpleAxisValueFormatter(0));
        data.setAxisYLeft(axisY);
        this.chartPower.setLineChartData(data);
    }

    private void ShowChartPower() {
        Line line;
        List<Line> lines = new ArrayList();
        if (this.batteryOK && this.checkBoxShowPower.isChecked()) {
            line = new Line(this.valuesPower).setColor(SupportMenu.CATEGORY_MASK).setCubic(false);
            line.setHasPoints(false);
            lines.add(line);
        }
        int sn = 0;
        for (int i = 0; i < this.tempNum; i++) {
            ((List) this.sensorTempSave.get(i)).add(new PointValue((float) this.count, (float) this.sensorTemp[i]));
            if (this.checkTemp[i].isChecked()) {
                line = new Line((List) this.sensorTempSave.get(i)).setColor(myColor(sn)).setCubic(false);
                line.setHasPoints(false);
                lines.add(line);
                sn++;
            }
        }
        LineChartData data = new LineChartData();
        data.setLines(lines);
        Axis axisX = new Axis().setHasLines(false);
        axisX.setFormatter(new SimpleAxisValueFormatter(0));
        data.setAxisXBottom(axisX);
        data.setBaseValue(0.0f);
        Axis axisY = new Axis().setHasLines(true);
        axisY.setFormatter(new SimpleAxisValueFormatter(0));
        data.setAxisYLeft(axisY);
        this.chartPower.setLineChartData(data);
    }

    private void UpdateChart() {
        this.count++;
        ReadTemp();
        UpdateChartGFLOPS();
        UpdateChartPower();
    }

    private void ReadTemp() {
        for (int i = 0; i < this.tempNum; i++) {
            this.sensorTemp[i] = readInt(this.tempPath[i]);
            if (this.sensorTemp[i] < -10000) {
                this.sensorTemp[i] = 0;
            }
            this.checkTemp[i].setText(this.tempName[i] + ":" + this.sensorTemp[i]);
        }
    }

    private void initTemp() {
        int i;
        int avcnt;
        String name;
        this.tempNameAll = new String[100];
        this.tempPathAll = new String[100];
        this.sensorTempAll = new int[100];
        this.tempUsed = new boolean[100];
        this.sensorTempSave = new ArrayList();
        this.tempNumAll = 0;
        for (i = 0; i < 8; i++) {
            this.sensorTempSave.add(new ArrayList());
        }
        File[] files = new File("/sys/devices/virtual/thermal").listFiles();
        if (files != null) {
            avcnt = 0;
            for (File name2 : files) {
                name = name2.getName();
                if (name.substring(0, 12).equals("thermal_zone")) {
                    this.tempPathAll[avcnt] = "/sys/devices/virtual/thermal/" + name;
                    this.tempNameAll[avcnt] = readStr(this.tempPathAll[avcnt] + "/type");
                    if (this.tempNameAll[avcnt].length() > 10) {
                        this.tempNameAll[avcnt] = this.tempNameAll[avcnt].substring(9, this.tempNameAll[avcnt].length());
                    }
                    this.sensorTempAll[avcnt] = readInt(this.tempPathAll[avcnt] + "/temp");
                    if (this.sensorTempAll[avcnt] < -10000) {
                        this.sensorTempAll[avcnt] = 0;
                    }
                    this.tempPathAll[avcnt] = this.tempPathAll[avcnt] + "/temp";
                    avcnt++;
                    if (avcnt > 99) {
                        break;
                    }
                }
            }
            this.tempNumAll = avcnt;
        }
        if (this.tempNumAll == 0) {
            files = new File("/sys/devices/virtual/hwmon").listFiles();
            if (files != null) {
                avcnt = 0;
                for (File name22 : files) {
                    name = name22.getName();
                    if (name.length() > 5 && name.substring(0, 5).equals("hwmon")) {
                        this.tempPathAll[avcnt] = "/sys/devices/virtual/hwmon/" + name;
                        this.tempNameAll[avcnt] = readStr(this.tempPathAll[avcnt] + "/name");
                        if (this.tempNameAll[avcnt].length() > 10) {
                            this.tempNameAll[avcnt] = this.tempNameAll[avcnt].substring(9, this.tempNameAll[avcnt].length());
                        }
                        File[] files2 = new File(this.tempPathAll[avcnt]).listFiles();
                        for (File name222 : files2) {
                            String name23 = name222.getName();
                            if (name23.length() > 5 && name23.substring(0, 4).equals("temp")) {
                                this.sensorTempAll[avcnt] = readInt(this.tempPathAll[avcnt] + "/" + name23);
                                if (this.sensorTempAll[avcnt] < -10000) {
                                    this.sensorTempAll[avcnt] = 0;
                                }
                                this.tempPathAll[avcnt] = this.tempPathAll[avcnt] + "/" + name23;
                            }
                        }
                        avcnt++;
                        if (avcnt > 16) {
                            break;
                        }
                    }
                }
                this.tempNumAll = avcnt;
            }
        }
        for (i = this.tempNum; i < 8; i++) {
            this.checkTemp[i].setText("");
            this.checkTemp[i].setEnabled(false);
            this.checkTemp[i].setVisibility(4);
        }
    }

    public void updateBurnInfo() {
        String[] itemsSize = new String[]{"128B", "1KiB", "8KiB", "32KiB", "128KiB", "512KiB", "2MiB"};
        String[] itemsIntv = new String[]{"0.5s", "1s", "2s", "3s", "5s", "8s", "15s", "30s"};
        String info = ("CPU:" + new String[]{"None", "NEON FP", "MIX1", "MIX2", "DMIPS", "SGEMM-A57", "SGEMM-A53", "SGEMM-A55", "FFT", "LINPACK"}[this.parameters[0]] + " " + new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}[this.parameters[1]] + "T --") + itemsSize[this.parameters[2]] + "/T " + itemsIntv[this.parameters[3]];
        if (this.parameters[4] > 0) {
            info = info + " LC";
        }
        this.textViewInfo.setText(info);
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_cpuburn);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        this.buttonCPU = (Button) findViewById(R.id.buttonCPUMode);
        this.buttonThread = (Button) findViewById(R.id.buttonThread);
        this.buttonSize = (Button) findViewById(R.id.buttonSize);
        this.buttonIntv = (Button) findViewById(R.id.buttonIntv);
        this.buttonLock = (Button) findViewById(R.id.buttonCore);
        this.textViewInfo = (TextView) findViewById(R.id.textViewBurnInfo);
        this.textViewGPUGFLOPS = (TextView) findViewById(R.id.textViewGFLOPS);
        this.textViewCPUGFLOPS = (TextView) findViewById(R.id.textViewCPU);
        this.textViewPower = (TextView) findViewById(R.id.textViewPower);
        ((TextView) findViewById(R.id.textViewInfo)).setText(Build.BRAND + " " + Build.MODEL + " " + Build.DEVICE + " " + Build.HARDWARE + " " + Build.BOARD);
        this.chart = (LineChartView) findViewById(R.id.chartResultDebug);
        this.chartPower = (LineChartView) findViewById(R.id.chartPower);
        this.buttonStart = (Button) findViewById(R.id.buttonStart);
        this.buttonStop = (Button) findViewById(R.id.buttonStop);
        this.checkBoxGPU = (CheckBox) findViewById(R.id.checkBoxGPU);
        this.checkBoxGPUScalar = (CheckBox) findViewById(R.id.checkBoxScale);
        this.checkBoxShowPower = (CheckBox) findViewById(R.id.checkBoxShowPower);
        this.rbFP32 = (RadioButton) findViewById(R.id.radioButtonHighP);
        this.rbFP16 = (RadioButton) findViewById(R.id.radioButtonMediumP);
        this.checkTemp = new CheckBox[8];
        this.checkTemp[0] = (CheckBox) findViewById(R.id.checkBox1);
        this.checkTemp[1] = (CheckBox) findViewById(R.id.checkBox2);
        this.checkTemp[2] = (CheckBox) findViewById(R.id.checkBox3);
        this.checkTemp[3] = (CheckBox) findViewById(R.id.checkBox4);
        this.checkTemp[4] = (CheckBox) findViewById(R.id.checkBox5);
        this.checkTemp[5] = (CheckBox) findViewById(R.id.checkBox6);
        this.checkTemp[6] = (CheckBox) findViewById(R.id.checkBox7);
        this.checkTemp[7] = (CheckBox) findViewById(R.id.checkBox8);
        this.checkBoxGPU.setChecked(true);
        initChart();
        initChartPower();
        initTemp();
        this.parameters[0] = 0;
        this.parameters[1] = 0;
        this.parameters[2] = 2;
        this.parameters[3] = 1;
        this.parameters[4] = 0;
        updateBurnInfo();
        this.checkBoxGPU.setChecked(false);
        if (VERSION.SDK_INT >= 23) {
            this.is3660 = true;
            this.batteryOK = true;
            this.mBatteryManager = (BatteryManager) getSystemService("batterymanager");
            this.checkBoxShowPower.setChecked(true);
        }
        initThread();
        this.mGLView = new MyGLSurfaceView(this, false, true);
        this.ll = (LinearLayout) findViewById(R.id.ll);
        this.ll.addView(this.mGLView);
    }

    private void initThread() {
        this.mUIHandler = new Handler(this);
        this.mCPUBurnThread = new CPUBurnThread("CPUBurnThread");
        this.mCPUBurnThread.setUIHandler(this.mUIHandler);
        this.mCPUBurnThread.start();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                String res = msg.getData().getString("result");
                this.cpucount++;
                int idx = res.indexOf(58);
                this.secs = Float.parseFloat(res.substring(0, idx));
                this.sfactor = (int) (((float) this.sfactor) * this.secs);
                if (this.sfactor < 1) {
                    this.sfactor = 1;
                }
                this.CPUgFlops = Float.parseFloat(res.substring(idx + 1, res.length()));
                if (this.CPUgFlops > this.CPUmaxgFlops) {
                    this.CPUmaxgFlops = this.CPUgFlops;
                }
                if (this.cpucount > 6) {
                    this.CPUsumfFlops += this.CPUgFlops;
                    this.CPUavggFlops = this.CPUsumfFlops / ((float) (this.cpucount - 6));
                    switch (this.parameters[0]) {
                        case 1:
                            this.textViewCPUGFLOPS.setText("CPU GFLOPS: MAX " + String.format("%.1f", new Object[]{Float.valueOf(this.CPUmaxgFlops)}) + " AVG " + String.format("%.1f", new Object[]{Float.valueOf(this.CPUavggFlops)}) + " Now " + String.format("%.1f", new Object[]{Float.valueOf(this.CPUgFlops)}));
                            break;
                        case 2:
                        case 3:
                            this.textViewCPUGFLOPS.setText("CPU GOPS: MAX " + String.format("%.1f", new Object[]{Float.valueOf(this.CPUmaxgFlops)}) + " AVG " + String.format("%.1f", new Object[]{Float.valueOf(this.CPUavggFlops)}) + " Now " + String.format("%.1f", new Object[]{Float.valueOf(this.CPUgFlops)}));
                            break;
                        case 4:
                            this.textViewCPUGFLOPS.setText("CPU DMIPS: MAX " + String.format("%.0f", new Object[]{Float.valueOf(this.CPUmaxgFlops)}) + " AVG " + String.format("%.0f", new Object[]{Float.valueOf(this.CPUavggFlops)}) + " Now " + String.format("%.0f", new Object[]{Float.valueOf(this.CPUgFlops)}));
                            break;
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            this.textViewCPUGFLOPS.setText("CPU GFLOPS: MAX " + String.format("%.1f", new Object[]{Float.valueOf(this.CPUmaxgFlops)}) + " AVG " + String.format("%.1f", new Object[]{Float.valueOf(this.CPUavggFlops)}) + " Now " + String.format("%.1f", new Object[]{Float.valueOf(this.CPUgFlops)}));
                            break;
                        case 9:
                            this.textViewCPUGFLOPS.setText("LINPACK: MAX " + String.format("%.1f", new Object[]{Float.valueOf(this.CPUmaxgFlops)}) + " AVG " + String.format("%.1f", new Object[]{Float.valueOf(this.CPUavggFlops)}) + " Now " + String.format("%.1f", new Object[]{Float.valueOf(this.CPUgFlops)}));
                            break;
                    }
                }
                if (!this.gpumode) {
                    if (this.batteryOK) {
                        if (this.powercount > this.powercountuse) {
                            this.poweravg = (this.powersumall - this.powersuamlluse) / ((float) (this.powercount - this.powercountuse));
                        }
                        if (this.powernowcount > 0) {
                            this.powernowavg = this.powernowsum / ((float) this.powernowcount);
                        }
                        this.textViewPower.setText("Power(mW): MAX " + String.format("%.0f", new Object[]{Float.valueOf(this.powermax)}) + " AVG " + String.format("%.0f", new Object[]{Float.valueOf(this.poweravg)}) + " Now " + String.format("%.0f", new Object[]{Float.valueOf(this.powernow)}));
                    } else {
                        this.textViewPower.setText("Unable to get power info");
                    }
                    UpdateChart();
                    this.powernowsum = 0.0f;
                    this.powernowcount = 0;
                }
                if (this.burning) {
                    this.handler3.post(this.doBurn);
                    break;
                }
                break;
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cpuburn, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            ShowInfo();
            return true;
        } else if (id == R.id.miKryoBig) {
            ShowInfo(1);
            return true;
        } else if (id == R.id.miKryoLittle) {
            ShowInfo(0);
            return true;
        } else if (id == R.id.miGPU) {
            ShowInfo(2);
            return true;
        } else if (id == R.id.blackTheme) {
            switchTheme();
            return true;
        } else if (id != R.id.Export) {
            return super.onOptionsItemSelected(item);
        } else {
            exportData();
            return true;
        }
    }

    private float getValueData(List<PointValue> values, int index) {
        if (index >= values.size()) {
            return 0.0f;
        }
        return ((PointValue) values.get(index)).getY();
    }

    private void exportData() {
        String fname = getExternalFilesDir(null) + File.separator + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(System.currentTimeMillis())) + ".csv";
        File file = new File(fname);
        Builder builder;
        try {
            int i;
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter pw = new FileWriter(file, true);
            String str = "Count,CPU,GPU,Power";
            for (i = 0; i < this.tempNum; i++) {
                str = str + ',' + this.tempName[i];
            }
            pw.write(str + 10);
            for (i = 0; i < this.count; i++) {
                str = i + "," + getValueData(this.valuesCPU, i) + "," + getValueData(this.valuesGPU, i) + "," + getValueData(this.valuesPower, i);
                for (int j = 0; j < this.tempNum; j++) {
                    str = str + ',' + getValueData((List) this.sensorTempSave.get(j), i);
                }
                pw.write(str + 10);
            }
            pw.flush();
            pw.close();
            builder = new Builder(this);
            builder.setTitle("Data Export");
            builder.setMessage(fname);
            builder.create().show();
        } catch (IOException e) {
            e.printStackTrace();
            builder = new Builder(this);
            builder.setTitle("Data Export Fail");
            builder.setMessage("No se han podido exportar los datos.");
            builder.create().show();
        }
    }

    private void switchTheme() {
        this.nightmode = !this.nightmode;
        int i;
        if (this.nightmode) {
            findViewById(R.id.main_content).setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
            for (i = 0; i < 8; i++) {
                this.checkTemp[i].setTextColor(-1);
            }
            this.checkBoxShowPower.setTextColor(-1);
            this.buttonCPU.setTextColor(-1);
            this.buttonThread.setTextColor(-1);
            this.buttonSize.setTextColor(-1);
            this.buttonIntv.setTextColor(-1);
            this.buttonLock.setTextColor(-1);
            this.checkBoxGPU.setTextColor(-1);
            this.checkBoxGPUScalar.setTextColor(-1);
            this.rbFP32.setTextColor(-1);
            this.rbFP16.setTextColor(-1);
            this.textViewCPUGFLOPS.setTextColor(-1);
            this.textViewGPUGFLOPS.setTextColor(-1);
            this.textViewPower.setTextColor(-1);
            this.textViewInfo.setTextColor(-1);
            return;
        }
        findViewById(R.id.main_content).setBackgroundColor(-1);
        for (i = 0; i < 8; i++) {
            this.checkTemp[i].setTextColor(ViewCompat.MEASURED_STATE_MASK);
        }
        this.checkBoxShowPower.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.buttonCPU.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.buttonThread.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.buttonSize.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.buttonIntv.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.buttonLock.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.checkBoxGPU.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.checkBoxGPUScalar.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.rbFP32.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.rbFP16.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.textViewCPUGFLOPS.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.textViewGPUGFLOPS.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.textViewPower.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        this.textViewInfo.setTextColor(ViewCompat.MEASURED_STATE_MASK);
    }

    private void ShowInfo() {
        Builder builder = new Builder(this);
        builder.setTitle("Info");
        builder.setMessage("Por la Agencia de Seguridad Nacional de los Estados Unidos");
        builder.create().show();
    }

    private void ShowInfo(int big) {
        Builder builder = new Builder(this);
        String title = "";
        VoltReader v = new VoltReader();
        builder.setTitle(v.GetTitle(big));
        builder.setMessage(v.readOpp(big));
        builder.create().show();
    }
}
