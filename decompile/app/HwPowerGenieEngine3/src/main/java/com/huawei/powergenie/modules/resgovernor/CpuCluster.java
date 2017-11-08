package com.huawei.powergenie.modules.resgovernor;

import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class CpuCluster {
    private int mCore;
    private int mDeviceMaxFreqCluster;
    private FileOutputStream mFosFreqMaxCluster;
    private FileOutputStream mFosFreqMinCluster;
    private int mFreqPropReadCount;
    private boolean mIsInitial;
    private boolean mPerfLockFeature;
    private MsmPerformance mPerformance;
    private int mPreFreqMaxCluster;
    private int mPreFreqMinCluster;
    private int[] mScaleStepFreqCluster;
    private String mScalingMaxFreqPathCluster;
    private int mScalingMinFreqCluster;
    private String mScalingMinFreqPathCluster;

    public CpuCluster() {
        this.mCore = -1;
        this.mIsInitial = false;
        this.mDeviceMaxFreqCluster = -1;
        this.mScalingMinFreqCluster = -1;
        this.mFosFreqMaxCluster = null;
        this.mFosFreqMinCluster = null;
        this.mFreqPropReadCount = 0;
        this.mPerfLockFeature = false;
        this.mPerformance = null;
        this.mPerformance = new MsmPerformance();
        this.mPerfLockFeature = this.mPerformance.initPerfLock();
        Log.i("CpuCluster", "support performance lock:" + this.mPerfLockFeature);
    }

    public boolean createCpuCluster(int core) {
        if (new File(String.format("/sys/devices/system/cpu/cpu%s", new Object[]{Integer.valueOf(core)})).exists()) {
            this.mCore = core;
            this.mIsInitial = initCpuCluster(core);
            Log.i("CpuCluster", "create Cpu Cluster:" + core + " true intial:" + this.mIsInitial);
            return true;
        }
        Log.i("CpuCluster", "create Cpu Cluster:" + core + " false intial:" + this.mIsInitial);
        return false;
    }

    private void closeFos(FileOutputStream fos) {
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
            }
        }
    }

    private boolean initCpuCluster(int core) {
        if (!initCpuFreqInfo(core)) {
            return false;
        }
        this.mFosFreqMaxCluster = getFileOutputStream(this.mScalingMaxFreqPathCluster);
        this.mFosFreqMinCluster = getFileOutputStream(this.mScalingMinFreqPathCluster);
        this.mFreqPropReadCount = 10;
        return true;
    }

    private void scaleCpuFreq(int maxFreq, int minFreq) {
        scaleCpuFreq(maxFreq, minFreq, -1);
    }

    private void scaleCpuFreq(int maxFreq, int minFreq, int minCores) {
        if (maxFreq > this.mDeviceMaxFreqCluster) {
            maxFreq = this.mDeviceMaxFreqCluster;
        } else if (maxFreq < this.mScalingMinFreqCluster) {
            maxFreq = this.mScalingMinFreqCluster;
        }
        if (minFreq < this.mScalingMinFreqCluster) {
            minFreq = this.mScalingMinFreqCluster;
        }
        if (minFreq >= this.mPreFreqMaxCluster) {
            preWriteCpuFreq(maxFreq, minFreq, true, minCores);
            preWriteCpuFreq(maxFreq, minFreq, false, minCores);
            return;
        }
        if (minFreq != this.mPreFreqMinCluster) {
            preWriteCpuFreq(maxFreq, minFreq, false, minCores);
        }
        if (maxFreq != this.mPreFreqMaxCluster) {
            preWriteCpuFreq(maxFreq, minFreq, true, minCores);
        }
    }

    private void preWriteCpuFreq(int maxFreq, int minFreq, boolean isFreqMax, int minCores) {
        boolean writeSuccess;
        if (isFreqMax) {
            writeSuccess = writeCpuFreq(maxFreq, true, minCores);
        } else {
            writeSuccess = writeCpuFreq(minFreq, false, minCores);
        }
        if (writeSuccess) {
            if (isFreqMax) {
                this.mPreFreqMaxCluster = maxFreq;
            } else {
                this.mPreFreqMinCluster = minFreq;
            }
        } else if (resetScalingFreq(maxFreq, minFreq, minCores)) {
            this.mPreFreqMaxCluster = maxFreq;
            this.mPreFreqMinCluster = minFreq;
        }
    }

    private boolean resetScalingFreq(int maxFreq, int minFreq, int minCores) {
        closeFos(this.mFosFreqMaxCluster);
        closeFos(this.mFosFreqMinCluster);
        this.mFosFreqMaxCluster = getFileOutputStream(this.mScalingMaxFreqPathCluster);
        this.mFosFreqMinCluster = getFileOutputStream(this.mScalingMinFreqPathCluster);
        if (writeCpuFreq(this.mScalingMinFreqCluster, false, minCores) && writeCpuFreq(this.mDeviceMaxFreqCluster, true, minCores) && writeCpuFreq(maxFreq, true, minCores)) {
            return writeCpuFreq(minFreq, false, minCores);
        }
        return false;
    }

    private boolean writeCpuFreq(int freq, boolean isFreqMax, int minCores) {
        if (isFreqMax) {
            if (this.mFosFreqMaxCluster == null) {
                this.mFosFreqMaxCluster = getFileOutputStream(this.mScalingMaxFreqPathCluster);
            }
            if (this.mFosFreqMaxCluster != null) {
                return writeFreq(freq, this.mFosFreqMaxCluster);
            }
            return false;
        } else if (this.mPerfLockFeature) {
            boolean ret = perfLockAcquire(freq, minCores);
            if (ret) {
                return ret;
            }
            if (this.mFosFreqMinCluster == null) {
                this.mFosFreqMinCluster = getFileOutputStream(this.mScalingMinFreqPathCluster);
            }
            if (this.mFosFreqMinCluster != null) {
                return writeFreq(freq, this.mFosFreqMinCluster);
            }
            return ret;
        } else {
            if (this.mFosFreqMinCluster == null) {
                this.mFosFreqMinCluster = getFileOutputStream(this.mScalingMinFreqPathCluster);
            }
            if (this.mFosFreqMinCluster != null) {
                return writeFreq(freq, this.mFosFreqMinCluster);
            }
            return false;
        }
    }

    private boolean writeFreq(int freq, FileOutputStream fos) {
        if (fos == null) {
            try {
                Log.w("CpuCluster", "write Cpu" + this.mCore + " freq file stream is null !");
                return false;
            } catch (IOException e) {
                Log.e("CpuCluster", "write cpu" + this.mCore + " " + freq + " failure");
                return false;
            }
        }
        byte[] byFreq = Integer.toString(freq).getBytes();
        fos.write(byFreq, 0, byFreq.length);
        return true;
    }

    public void processCpuFreq(int maxFreq, int minFreq, int minCores) {
        if (!this.mIsInitial) {
            this.mIsInitial = initCpuCluster(this.mCore);
            if (!this.mIsInitial) {
                return;
            }
        }
        if (this.mFreqPropReadCount > 0) {
            initQCOMCpuMinFreq();
            this.mFreqPropReadCount--;
        }
        if (this.mPerfLockFeature) {
            perfLockRelease();
        }
        scaleCpuFreq(matchAvailableCpuFreq(maxFreq, true), matchAvailableCpuFreq(minFreq, false), minCores);
    }

    public boolean checkCpuAction(int max, int min) {
        if (!this.mIsInitial) {
            this.mIsInitial = initCpuCluster(this.mCore);
            if (!this.mIsInitial) {
                return false;
            }
        }
        return (matchAvailableCpuFreq(max, true) == this.mPreFreqMaxCluster && matchAvailableCpuFreq(min, false) == this.mPreFreqMinCluster) ? false : true;
    }

    public void setToDefaultState(int freq) {
        int maxFreq;
        if (!this.mIsInitial) {
            this.mIsInitial = initCpuCluster(this.mCore);
            if (!this.mIsInitial) {
                return;
            }
        }
        if (freq > 0) {
            maxFreq = freq;
        } else {
            maxFreq = this.mDeviceMaxFreqCluster;
        }
        scaleCpuFreq(matchAvailableCpuFreq(maxFreq, true), this.mScalingMinFreqCluster);
    }

    public int matchAvailableCpuFreq(int cpuFreq, boolean matchMax) {
        if (-1 == cpuFreq) {
            if (matchMax) {
                return this.mDeviceMaxFreqCluster;
            }
            return this.mScalingMinFreqCluster;
        } else if (cpuFreq < this.mScalingMinFreqCluster) {
            return this.mScalingMinFreqCluster;
        } else {
            if (cpuFreq > this.mDeviceMaxFreqCluster) {
                return this.mDeviceMaxFreqCluster;
            }
            int highFreq = this.mDeviceMaxFreqCluster;
            int lowFreq = this.mScalingMinFreqCluster;
            if (this.mScaleStepFreqCluster != null) {
                for (int i = 0; i < this.mScaleStepFreqCluster.length; i++) {
                    if (cpuFreq >= this.mScaleStepFreqCluster[i]) {
                        lowFreq = this.mScaleStepFreqCluster[i];
                        break;
                    }
                    highFreq = this.mScaleStepFreqCluster[i];
                }
                if (highFreq - cpuFreq <= cpuFreq - lowFreq) {
                    cpuFreq = highFreq;
                } else if (lowFreq != 0) {
                    cpuFreq = lowFreq;
                }
            }
            return cpuFreq;
        }
    }

    private void initQCOMCpuMinFreq() {
        String propname = String.format("ro.min_freq_%s", new Object[]{Integer.valueOf(this.mCore)});
        int intFreq = SystemProperties.getInt(propname, 0);
        if (intFreq == 0) {
            if (this.mCore == 1 || this.mCore == 2 || this.mCore == 3) {
                propname = String.format("ro.min_freq_%s", new Object[]{Integer.valueOf(0)});
            } else {
                if (!(this.mCore == 5 || this.mCore == 6)) {
                    if (this.mCore == 7) {
                    }
                }
                propname = String.format("ro.min_freq_%s", new Object[]{Integer.valueOf(4)});
            }
            intFreq = SystemProperties.getInt(propname, 0);
        }
        if (intFreq != 0 && intFreq != this.mScalingMinFreqCluster) {
            this.mScalingMinFreqCluster = intFreq;
            Log.i("CpuCluster", "reset " + this.mCore + " scaling_min_freq to " + this.mScalingMinFreqCluster);
        }
    }

    private boolean initCpuFreqInfo(int core) {
        String deviceMaxPath = String.format("/sys/devices/system/cpu/cpu%s/cpufreq/cpuinfo_max_freq", new Object[]{Integer.valueOf(core)});
        String scalingMinPath = String.format("/sys/devices/system/cpu/cpu%s/cpufreq/scaling_min_freq", new Object[]{Integer.valueOf(core)});
        String deviceStepPath = String.format("/sys/devices/system/cpu/cpu%s/cpufreq/scaling_available_frequencies", new Object[]{Integer.valueOf(core)});
        this.mScalingMaxFreqPathCluster = String.format("/sys/devices/system/cpu/cpu%s/cpufreq/scaling_max_freq", new Object[]{Integer.valueOf(core)});
        this.mScalingMinFreqPathCluster = scalingMinPath;
        int deviceMaxFreq = CpuGovernorPolicy.readFreqInt(deviceMaxPath);
        if (deviceMaxFreq == -1) {
            Log.d("CpuCluster", "Cpu" + this.mCore + " initCpuFreqInfo: read path " + deviceMaxPath + " failed.");
            return false;
        }
        this.mDeviceMaxFreqCluster = deviceMaxFreq;
        int scalingMinFreq = CpuGovernorPolicy.readFreqInt(scalingMinPath);
        if (scalingMinFreq == -1) {
            Log.d("CpuCluster", "Cpu" + this.mCore + " initCpuFreqInfo: read path " + scalingMinPath + " failed.");
            return false;
        }
        this.mScalingMinFreqCluster = scalingMinFreq;
        String strStepFreq = CpuGovernorPolicy.readFileContent(deviceStepPath);
        if (strStepFreq == null) {
            Log.d("CpuCluster", "Cpu" + this.mCore + " initCpuFreqInfo: read path " + deviceStepPath + " failed.");
            return false;
        }
        String[] freqSteps = strStepFreq.split(" ");
        int len = freqSteps.length;
        this.mScaleStepFreqCluster = new int[len];
        try {
            int i;
            if (Integer.parseInt(freqSteps[0]) < Integer.parseInt(freqSteps[len - 1])) {
                len--;
                int i2 = 0;
                while (len >= 0) {
                    try {
                        i = i2 + 1;
                        this.mScaleStepFreqCluster[i2] = Integer.parseInt(freqSteps[len]);
                        len--;
                        i2 = i;
                    } catch (NumberFormatException e) {
                        i = i2;
                    }
                }
            } else {
                for (i = 0; i < len; i++) {
                    this.mScaleStepFreqCluster[i] = Integer.parseInt(freqSteps[i]);
                }
            }
        } catch (NumberFormatException e2) {
        }
        this.mPreFreqMaxCluster = this.mDeviceMaxFreqCluster;
        this.mPreFreqMinCluster = this.mScalingMinFreqCluster;
        return true;
        Log.w("CpuCluster", "Cpu" + this.mCore + " initCpuFreqInfo throws NumberFormatException.");
        this.mPreFreqMaxCluster = this.mDeviceMaxFreqCluster;
        this.mPreFreqMinCluster = this.mScalingMinFreqCluster;
        return true;
    }

    private boolean perfLockAcquire(int freq, int minCores) {
        return this.mPerformance.perfLockAcquire(freq, minCores);
    }

    private boolean perfLockRelease() {
        return this.mPerformance.perfLockRelease();
    }

    public int getDefaultMaxFreq() {
        return this.mDeviceMaxFreqCluster;
    }

    public int getDefaultMinFreq() {
        return this.mScalingMinFreqCluster;
    }

    private FileOutputStream getFileOutputStream(String path) {
        if (path == null) {
            Log.d("CpuCluster", "Try to getFileOutputStream but path is null.");
            return null;
        }
        try {
            return new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            Log.w("CpuCluster", "getFileOutputStream throws FileNotFoundException.");
            return null;
        } catch (SecurityException e2) {
            Log.w("CpuCluster", "getFileOutputStream throws SecurityException.");
            return null;
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        pw.println(String.format("        Default CPU%s Max Freq: %s, Min Freq:", new Object[]{Integer.valueOf(this.mCore), Integer.valueOf(this.mDeviceMaxFreqCluster), Integer.valueOf(this.mScalingMinFreqCluster)}));
        pw.println(String.format("        Current CPU%s Max Freq : %s, Min Freq:", new Object[]{Integer.valueOf(this.mCore), Integer.valueOf(this.mPreFreqMaxCluster), Integer.valueOf(this.mPreFreqMinCluster)}));
        pw.println(String.format("           Read CPU%s Max Freq : %s, Min Freq:", new Object[]{Integer.valueOf(this.mCore), CpuGovernorPolicy.readFileContent(this.mScalingMaxFreqPathCluster), CpuGovernorPolicy.readFileContent(this.mScalingMinFreqPathCluster)}));
    }
}
