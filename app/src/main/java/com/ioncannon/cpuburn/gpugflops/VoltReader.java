package com.ioncannon.cpuburn.gpugflops;

import android.os.Build;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class VoltReader {
    private String model;
    int[][] msm8996_big;

    public VoltReader() {
        this.model = "";
        this.msm8996_big = new int[][]{new int[]{545, 625}, new int[]{560, 625}, new int[]{570, 625}, new int[]{580, 625}, new int[]{600, 640}, new int[]{605, 645}, new int[]{615, 655}, new int[]{625, 665}, new int[]{630, 670}, new int[]{650, 695}, new int[]{665, 710}, new int[]{675, 725}, new int[]{685, 735}, new int[]{705, 775}, new int[]{725, 810}, new int[]{745, 850}, new int[]{765, 885}, new int[]{785, 925}, new int[]{805, 960}, new int[]{825, 1000}, new int[]{835, 1010}, new int[]{860, 960}, new int[]{880, 985}, new int[]{895, 1010}, new int[]{915, 1035}, new int[]{935, 1055}, new int[]{955, 1075}, new int[]{975, 1095}, new int[]{995, 1115}, new int[]{1020, 1130}};
        this.model = Build.BOARD;
    }

    public String readOpp(int n) {
        String s = "Freq MHz\tVoltage mV\tMin\tMax\n";
        if (!this.model.equals("msm8996")) {
            return s;
        }
        if (n == 0) {
            s = s + readMultiStrAdd("/sys/devices/system/cpu/cpu0/opp_table");
        }
        if (n == 1) {
            s = s + readMultiStrAdd("/sys/devices/system/cpu/cpu2/opp_table", this.msm8996_big);
        }
        if (n == 2) {
            return s + readMultiStrAdd("/sys/devices/soc/b00000.qcom,kgsl-3d0/opp_table");
        }
        return s;
    }

    private String readMultiStrAdd(String fileName) {
        Throwable th;
        BufferedReader reader = null;
        String allStr = "";
        String tempString = "";
        String ts = "";
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            do {
                try {
                    tempString = reader2.readLine();
                    if (tempString != null) {
                        allStr = allStr + (((Long.parseLong(tempString.split(" ")[0]) / 1000000) + "     ") + (Integer.parseInt(tempString.split(" ")[1]) / 1000) + " ") + "\n";
                        continue;
                    }
                } catch (IOException e) {
                    reader = reader2;
                    if (reader != null) {
                    }
                    return allStr;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            } while (tempString != null);
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
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            return allStr;
        } catch (Throwable th3) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e5) {
                }
            }
        }
        return allStr;
    }

    private String GetQuality(int vol, int refmin, int refmax) {
        int vdiff1 = vol - refmin;
        int vdiff2 = refmax - vol;
        int totaldiff = refmax - refmin;
        if (vol <= refmin) {
            return "Lo mejor";
        }
        if (vol >= refmax) {
            return "Basura";
        }
        double tdiff = ((double) vdiff1) / ((double) totaldiff);
        if (tdiff < 0.2d) {
            return "Excelente";
        }
        if (tdiff < 0.4d) {
            return "Medio superior";
        }
        if (tdiff < 0.6d) {
            return "Medio";
        }
        if (tdiff < 0.8d) {
            return "Medio bajo";
        }
        if (tdiff <= 1.0d) {
            return "Basura";
        }
        return "Medio";
    }

    private String readMultiStrAdd(String fileName, int[][] ref) {
        Throwable th;
        BufferedReader reader = null;
        String allStr = "";
        String tempString = "";
        String ts = "";
        int n = 0;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            do {
                try {
                    tempString = reader2.readLine();
                    if (tempString != null) {
                        int tfreq = (int) (Long.parseLong(tempString.split(" ")[0]) / 1000000);
                        int tint = Integer.parseInt(tempString.split(" ")[1]) / 1000;
                        ts = (tfreq + "  ") + tint + "          ";
                        if (tfreq < 1000) {
                            ts = ts + "  ";
                        }
                        ts = (ts + ref[n][0] + " ") + ref[n][1] + "   ";
                        if (tfreq > 1100) {
                            ts = ts + GetQuality(tint, ref[n][0], ref[n][1]);
                        }
                        allStr = allStr + ts + "\n";
                        n++;
                        continue;
                    }
                } catch (IOException e) {
                    reader = reader2;
                    if (reader != null) {
                    }
                    return allStr;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            } while (tempString != null);
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
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            return allStr;
        } catch (Throwable th3) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e5) {
                }
            }
        }
        return allStr;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0043 A:{SYNTHETIC, Splitter:B:18:0x0043} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004c A:{SYNTHETIC, Splitter:B:23:0x004c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String readMultiStr(String fileName) {
        Throwable th;
        BufferedReader reader = null;
        String allStr = "";
        String tempString = "";
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            do {
                try {
                    tempString = reader2.readLine();
                    if (tempString != null) {
                        allStr = allStr + tempString + "\n";
                        continue;
                    }
                } catch (IOException e) {
                    reader = reader2;
                    if (reader != null) {
                    }
                    return allStr;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            } while (tempString != null);
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
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            return allStr;
        } catch (Throwable th3) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e5) {
                }
            }
        }
        return allStr;
    }

    public String GetTitle(int big) {
        String title = "";
        if (!this.model.equals("msm8996")) {
            return title;
        }
        if (big == 0) {
            title = "Kryo Little Core";
        }
        if (big == 1) {
            title = "Kryo Big Core";
        }
        if (big == 2) {
            return "Adreno 530 GPU";
        }
        return title;
    }
}
