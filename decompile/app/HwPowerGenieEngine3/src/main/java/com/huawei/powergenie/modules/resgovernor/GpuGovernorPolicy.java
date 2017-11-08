package com.huawei.powergenie.modules.resgovernor;

import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IThermal;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import libcore.io.IoUtils;

public class GpuGovernorPolicy {
    private static GpuGovernorPolicy sInstance;
    private int mDefaultGpuMaxFreq = 450000000;
    private int mDefaultGpuMinFreq = 200000000;
    private FileOutputStream mFosGpuFreqMax = null;
    private int[] mGpuScaleStepFreq;
    private ICoreContext mICoreContext;
    private final IThermal mIThermal;
    private int mPreGpuFreqMax;

    static GpuGovernorPolicy getInstance(ICoreContext context) {
        if (sInstance == null) {
            sInstance = new GpuGovernorPolicy(context);
        }
        return sInstance;
    }

    private GpuGovernorPolicy(ICoreContext context) {
        this.mICoreContext = context;
        this.mIThermal = (IThermal) this.mICoreContext.getService("thermal");
    }

    public void processGpuFreq(CpuFreqItem actionItem) {
        int maxGpuFreq = actionItem.getPolicy(13);
        int maxGpuFreqBeforeMatch = maxGpuFreq;
        maxGpuFreq = matchAvailableGpuFreq(maxGpuFreq, true);
        if (this.mPreGpuFreqMax != maxGpuFreq) {
            scaleGpuFreq(maxGpuFreq, maxGpuFreqBeforeMatch);
        }
    }

    public void setToDefaultState(int freq) {
        int maxFreq;
        if (freq > 0) {
            maxFreq = freq;
        } else {
            maxFreq = -1;
        }
        scaleGpuFreq(matchAvailableGpuFreq(maxFreq, true), maxFreq);
    }

    public boolean initGpuFreqInfo() {
        Object obj;
        Throwable th;
        NumberFormatException e;
        boolean ret = true;
        String str = null;
        String availablePath = null;
        if (this.mIThermal != null) {
            str = this.mIThermal.getThermalInterface("gpu");
            availablePath = this.mIThermal.getThermalInterface("gpu_available");
        }
        if (str == null) {
            str = "/sys/class/kgsl/kgsl-3d0/max_gpuclk";
        }
        if (availablePath == null) {
            availablePath = "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies";
        }
        AutoCloseable autoCloseable = null;
        try {
            byte[] bytes = new byte[300];
            Arrays.fill(bytes, (byte) 0);
            FileInputStream fis = new FileInputStream(str);
            try {
                int len = fis.read(bytes);
                if (len > 0) {
                    this.mDefaultGpuMaxFreq = Integer.parseInt(new String(bytes, 0, len, "UTF-8").trim());
                }
                IoUtils.closeQuietly(fis);
                StringBuffer stringBuffer = new StringBuffer(200);
                autoCloseable = new FileInputStream(availablePath);
                Arrays.fill(bytes, (byte) 0);
                while (true) {
                    len = autoCloseable.read(bytes);
                    if (len <= 0) {
                        break;
                    }
                    stringBuffer.append(new String(bytes, 0, len, "UTF-8"));
                }
                String[] freqSteps = stringBuffer.toString().trim().split(" ");
                len = freqSteps.length;
                this.mGpuScaleStepFreq = new int[len];
                int i;
                if (Integer.parseInt(freqSteps[0]) < Integer.parseInt(freqSteps[len - 1])) {
                    this.mDefaultGpuMinFreq = Integer.parseInt(freqSteps[0]);
                    len--;
                    int i2 = 0;
                    while (len >= 0) {
                        i = i2 + 1;
                        this.mGpuScaleStepFreq[i2] = Integer.parseInt(freqSteps[len]);
                        len--;
                        i2 = i;
                    }
                } else {
                    this.mDefaultGpuMinFreq = Integer.parseInt(freqSteps[len - 1]);
                    for (i = 0; i < len; i++) {
                        this.mGpuScaleStepFreq[i] = Integer.parseInt(freqSteps[i]);
                    }
                }
                this.mPreGpuFreqMax = this.mDefaultGpuMaxFreq;
                if (autoCloseable != null) {
                    try {
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Exception e2) {
                    }
                }
            } catch (IOException e3) {
                obj = fis;
                try {
                    Log.e("GpuGovernorPolicy", "init Gpu FreqInforead failed ");
                    ret = false;
                    if (autoCloseable != null) {
                        try {
                            IoUtils.closeQuietly(autoCloseable);
                        } catch (Exception e4) {
                        }
                    }
                    return ret;
                } catch (Throwable th2) {
                    th = th2;
                    if (autoCloseable != null) {
                        try {
                            IoUtils.closeQuietly(autoCloseable);
                        } catch (Exception e5) {
                        }
                    }
                    throw th;
                }
            } catch (NumberFormatException e6) {
                e = e6;
                obj = fis;
                Log.w("GpuGovernorPolicy", "number format exception", e);
                ret = false;
                if (autoCloseable != null) {
                    try {
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Exception e7) {
                    }
                }
                return ret;
            } catch (Throwable th3) {
                th = th3;
                obj = fis;
                if (autoCloseable != null) {
                    IoUtils.closeQuietly(autoCloseable);
                }
                throw th;
            }
        } catch (IOException e8) {
            Log.e("GpuGovernorPolicy", "init Gpu FreqInforead failed ");
            ret = false;
            if (autoCloseable != null) {
                IoUtils.closeQuietly(autoCloseable);
            }
            return ret;
        } catch (NumberFormatException e9) {
            e = e9;
            Log.w("GpuGovernorPolicy", "number format exception", e);
            ret = false;
            if (autoCloseable != null) {
                IoUtils.closeQuietly(autoCloseable);
            }
            return ret;
        }
        return ret;
    }

    public boolean checkGpuAction(CpuFreqItem actionItem) {
        if (matchAvailableGpuFreq(actionItem.getPolicy(13), true) != this.mPreGpuFreqMax) {
            return true;
        }
        return false;
    }

    private int matchAvailableGpuFreq(int gpuFreq, boolean matchMax) {
        if (-1 != gpuFreq) {
            int highFreq = this.mDefaultGpuMaxFreq;
            int lowFreq = this.mDefaultGpuMinFreq;
            if (this.mGpuScaleStepFreq != null) {
                for (int i = 0; i < this.mGpuScaleStepFreq.length; i++) {
                    if (gpuFreq >= this.mGpuScaleStepFreq[i]) {
                        lowFreq = this.mGpuScaleStepFreq[i];
                        break;
                    }
                    highFreq = this.mGpuScaleStepFreq[i];
                }
                if (highFreq - gpuFreq <= gpuFreq - lowFreq) {
                    gpuFreq = highFreq;
                } else if (lowFreq != 0) {
                    gpuFreq = lowFreq;
                }
            }
            return gpuFreq;
        } else if (matchMax) {
            return this.mDefaultGpuMaxFreq;
        } else {
            return this.mDefaultGpuMinFreq;
        }
    }

    private void scaleGpuFreq(int maxFreq, int maxFreqBeforeMatch) {
        if (maxFreq > this.mDefaultGpuMaxFreq) {
            maxFreq = this.mDefaultGpuMaxFreq;
        }
        if (writeGpuFreq(maxFreq, maxFreqBeforeMatch)) {
            this.mPreGpuFreqMax = maxFreq;
        }
    }

    private boolean writeGpuFreq(int maxFreq, int maxFreqBeforeMatch) {
        if (-1 == maxFreqBeforeMatch) {
            maxFreq = 0;
        }
        boolean ret = NativeAdapter.writeGpuFreq(maxFreq);
        Log.d("GpuGovernorPolicy", "writeGpuFreq max freq= " + maxFreq + ",ret:" + ret);
        return ret;
    }

    public void dump(PrintWriter pw, String[] args) {
        pw.println("    GPU POLICY:");
        pw.println("        DEFAULT GPU MAX: " + this.mDefaultGpuMaxFreq + " MIN: " + this.mDefaultGpuMinFreq);
        pw.println("        Current GPU MAX: " + this.mPreGpuFreqMax);
    }
}
