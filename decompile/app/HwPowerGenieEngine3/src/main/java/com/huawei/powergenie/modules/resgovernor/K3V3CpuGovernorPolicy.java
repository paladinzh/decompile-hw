package com.huawei.powergenie.modules.resgovernor;

import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import libcore.io.IoUtils;

public final class K3V3CpuGovernorPolicy extends CpuGovernorPolicy {
    private static final boolean CONFIG_THERMAL_GPU_LESS_MIN = SystemProperties.getBoolean("ro.config.thermal_gpu_min", false);
    private static String mDDRMaxFreqPath;
    private static String mDDRMinFreqPath;
    private static String mDDRStepFreqsPath;
    private static String mGpuMaxFreqPath;
    private static String mGpuMinFreqPath;
    private static String mGpuStepFreqsPath;
    private static K3V3CpuGovernorPolicy sInstance;
    private boolean isLittleBigClusterCpu;
    private int mA15DefaultCpuMaxFreq;
    private int mA15DefaultCpuMinFreq;
    private int mA15PreFreqMax;
    private int mA15PreFreqMin;
    private int[] mA15ScaleStepFreq;
    private int[] mDDRScaleStepFreq;
    private int mDefaultCpuBaseNum;
    private int mDefaultCpuMaxNum;
    private int mDefaultDDRMaxFreq;
    private int mDefaultDDRMinFreq;
    private int mDefaultGpuMaxFreq;
    private int mDefaultGpuMinFreq;
    private FileOutputStream mFosA15FreqMax;
    private FileOutputStream mFosA15FreqMin;
    private FileOutputStream mFosCpuNumMax;
    private FileOutputStream mFosCpuNumMin;
    private FileOutputStream mFosDDRFreqMax;
    private FileOutputStream mFosDDRFreqMin;
    private FileOutputStream mFosFreqMax;
    private FileOutputStream mFosFreqMin;
    private FileOutputStream mFosGpuFreqMax;
    private FileOutputStream mFosGpuFreqMin;
    private FileOutputStream mFosHMPPolicy;
    private int[] mGpuScaleStepFreq;
    private boolean mHasHmpPolicyNode;
    private boolean mIsCpuNumPathOk;
    private boolean mIsCpuPathOk;
    private int mPreBoost;
    private int mPreCpuNumMax;
    private int mPreCpuNumMin;
    private int mPreDDRFreqMax;
    private int mPreDDRFreqMin;
    private int mPreFreqMax;
    private int mPreFreqMin;
    private int mPreFrokOnBig;
    private int mPreGpuFreqMax;
    private int mPreGpuFreqMin;
    private int mPreIpaPower;
    private int mPreIpaSwitch;
    private int mPreIpaTemp;
    private int mPreLowBatThresHoldDown;
    private int mPreLowBatThresHoldUp;
    private int mPreThermalThresHoldDown;
    private int mPreThermalThresHoldUp;
    private int mPreThresHoldDown;
    private int mPreThresHoldUp;

    static K3V3CpuGovernorPolicy getInstance(ICoreContext pgcontext, CpuGovernor cpuGovernor) {
        if (sInstance == null) {
            sInstance = new K3V3CpuGovernorPolicy(pgcontext, cpuGovernor);
        }
        return sInstance;
    }

    private boolean isCpuSupportLitBigCluster() {
        return new File("/sys/devices/system/cpu/cpu4/cpufreq/scaling_min_freq").exists();
    }

    private String getAvailableDeviceNode(String k3v3Candidate, String v8r2k3v3plusCandidate) {
        return new File(k3v3Candidate).exists() ? k3v3Candidate : v8r2k3v3plusCandidate;
    }

    private void adaptDeviceNode() {
        mGpuMinFreqPath = getAvailableDeviceNode("/sys/class/devfreq/e8600000.mali/min_freq", "/sys/class/devfreq/gpufreq/min_freq");
        mGpuMaxFreqPath = getAvailableDeviceNode("/sys/class/devfreq/e8600000.mali/max_freq", "/sys/class/devfreq/gpufreq/max_freq");
        mGpuStepFreqsPath = getAvailableDeviceNode("/sys/class/devfreq/e8600000.mali/available_frequencies", "/sys/class/devfreq/gpufreq/available_frequencies");
        mDDRMinFreqPath = getAvailableDeviceNode("/sys/class/devfreq/fff20000.ddr_devfreq/min_freq", "/sys/class/devfreq/ddrfreq/min_freq");
        mDDRMaxFreqPath = getAvailableDeviceNode("/sys/class/devfreq/fff20000.ddr_devfreq/max_freq", "/sys/class/devfreq/ddrfreq/max_freq");
        mDDRStepFreqsPath = getAvailableDeviceNode("/sys/class/devfreq/fff20000.ddr_devfreq/available_frequencies", "/sys/class/devfreq/ddrfreq/available_frequencies");
    }

    private K3V3CpuGovernorPolicy(ICoreContext pgcontext, CpuGovernor cpuGovernor) {
        super(pgcontext, cpuGovernor);
        this.mIsCpuPathOk = true;
        this.mA15DefaultCpuMaxFreq = 1804800;
        this.mA15DefaultCpuMinFreq = 508800;
        this.mDefaultGpuMaxFreq = 450000000;
        this.mDefaultGpuMinFreq = 200000000;
        this.mDefaultDDRMaxFreq = 800000000;
        this.mDefaultDDRMinFreq = 120000000;
        this.mDefaultCpuBaseNum = 4;
        this.mDefaultCpuMaxNum = 8;
        this.mPreIpaPower = 0;
        this.mPreIpaTemp = 0;
        this.mPreIpaSwitch = 0;
        this.mPreFrokOnBig = -1;
        this.mPreBoost = -1;
        this.mFosFreqMax = null;
        this.mFosFreqMin = null;
        this.mFosA15FreqMax = null;
        this.mFosA15FreqMin = null;
        this.mFosGpuFreqMax = null;
        this.mFosGpuFreqMin = null;
        this.mFosDDRFreqMax = null;
        this.mFosDDRFreqMin = null;
        this.mFosHMPPolicy = null;
        this.mFosCpuNumMin = null;
        this.mFosCpuNumMax = null;
        this.isLittleBigClusterCpu = false;
        this.mHasHmpPolicyNode = false;
        this.mIsCpuNumPathOk = true;
        this.mIsCpuPathOk = initCpuFreqInfo();
        Log.i("K3V3CpuGovernorPolicy", "init cpu info result: " + this.mIsCpuPathOk);
        this.isLittleBigClusterCpu = isCpuSupportLitBigCluster();
        adaptDeviceNode();
        initAllFreqInfo();
        this.mIsCpuNumPathOk = initCpuNumInfo();
        Log.i("K3V3CpuGovernorPolicy", "PG init cpunum info result : " + this.mIsCpuNumPathOk);
    }

    protected void handleStart() {
        super.handleStart();
        this.mHasHmpPolicyNode = isExistPolicyPath("/sys/kernel/set_hmp_thresholds/policy");
        Log.i("K3V3CpuGovernorPolicy", "PG set hmp policy file node: " + this.mHasHmpPolicyNode);
    }

    protected boolean handleCpuAction(PowerAction action, int subActionFlag) {
        if (!this.mIsCpuPathOk || !super.handleCpuAction(action, subActionFlag)) {
            return false;
        }
        this.mPolicyHandler.removeMessages(100);
        if (this.mCurFreqItem == null || requireToProcess(this.mActionItem)) {
            Message msg = this.mPolicyHandler.obtainMessage(100);
            msg.obj = this.mActionItem;
            this.mCurFreqItem = this.mActionItem;
            this.mPolicyHandler.sendMessageDelayed(msg, getDelayTime(this.mActionItem, action.getActionId(), subActionFlag));
        } else {
            stopSysLoadCheck();
            this.mCurFreqItem = this.mActionItem;
        }
        return true;
    }

    public int matchAvailableCpuFreq(int cpuFreq, boolean matchMax) {
        return super.matchAvailableCpuFreq(cpuFreq, matchMax);
    }

    public int matchAvailableFreq(int type, int freq, boolean matchMax) {
        int defaultMaxFreq = 0;
        int defaultMinFreq = 0;
        int[] scaleStepFreq = null;
        switch (type) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
                defaultMaxFreq = this.mA15DefaultCpuMaxFreq;
                defaultMinFreq = this.mA15DefaultCpuMinFreq;
                scaleStepFreq = this.mA15ScaleStepFreq;
                break;
            case NativeAdapter.PLATFORM_HI /*2*/:
                defaultMaxFreq = this.mDefaultGpuMaxFreq;
                defaultMinFreq = this.mDefaultGpuMinFreq;
                scaleStepFreq = this.mGpuScaleStepFreq;
                break;
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                defaultMaxFreq = this.mDefaultDDRMaxFreq;
                defaultMinFreq = this.mDefaultDDRMinFreq;
                scaleStepFreq = this.mDDRScaleStepFreq;
                break;
            default:
                Log.e("K3V3CpuGovernorPolicy", "matchAvailableFreq ERROR type:" + type);
                break;
        }
        if (-1 != freq) {
            int highFreq = defaultMaxFreq;
            int lowFreq = defaultMinFreq;
            if (scaleStepFreq != null) {
                int i = 0;
                while (i < scaleStepFreq.length) {
                    if (freq >= scaleStepFreq[i]) {
                        lowFreq = scaleStepFreq[i];
                        if (highFreq - freq > freq - lowFreq) {
                            freq = highFreq;
                        } else if (lowFreq != 0) {
                            freq = lowFreq;
                        }
                    } else {
                        highFreq = scaleStepFreq[i];
                        i++;
                    }
                }
                if (highFreq - freq > freq - lowFreq) {
                    freq = highFreq;
                } else if (lowFreq != 0) {
                    freq = lowFreq;
                }
            }
            return freq;
        } else if (matchMax) {
            return defaultMaxFreq;
        } else {
            return defaultMinFreq;
        }
    }

    protected void setToDefaultState() {
        int defaultIpaSwitch;
        super.setToDefaultState();
        scaleCpuFreq(getLimitMinFreq(6) > 0 ? getLimitMinFreq(6) : this.mDefaultMaxFreq, this.mDefaultMinFreq);
        if (this.mIsCpuNumPathOk) {
            scaleCpuNum(this.mDefaultCpuMaxNum, this.mDefaultCpuBaseNum);
        }
        if (this.isLittleBigClusterCpu) {
            scaleA15CpuFreq(getLimitMinFreq(19) > 0 ? getLimitMinFreq(19) : this.mA15DefaultCpuMaxFreq, this.mA15DefaultCpuMinFreq);
        }
        scaleGpuFreq(getLimitMinFreq(13) > 0 ? getLimitMinFreq(13) : this.mDefaultGpuMaxFreq, this.mDefaultGpuMinFreq);
        scaleDDRFreq(getLimitMinFreq(10) > 0 ? getLimitMinFreq(10) : this.mDefaultDDRMaxFreq, this.mDefaultDDRMinFreq);
        int defaultIpaTemp = getLimitMinFreq(40) > 0 ? getLimitMinFreq(40) : 0;
        int defaultIpaPower = getLimitMinFreq(39) > 0 ? getLimitMinFreq(39) : 0;
        if (getLimitMinFreq(41) > 0) {
            defaultIpaSwitch = getLimitMinFreq(41);
        } else {
            defaultIpaSwitch = 0;
        }
        scaleIpa(defaultIpaTemp, defaultIpaPower, defaultIpaSwitch);
        scaleForkOnBig(getLimitForkOnBigState());
        scaleBoost(getLimitBoostState());
        if ((this.mPreThermalThresHoldUp > 0 || this.mPreThermalThresHoldDown > 0) && writeHmpPolicy(4, 0, this.mPreThermalThresHoldUp, this.mPreThermalThresHoldDown)) {
            Log.i("K3V3CpuGovernorPolicy", "close previously thermal Hmp policy : " + this.mPreThermalThresHoldUp + " , " + this.mPreThermalThresHoldDown);
            this.mPreThermalThresHoldUp = 0;
            this.mPreThermalThresHoldDown = 0;
        }
        if ((this.mPreLowBatThresHoldUp > 0 || this.mPreLowBatThresHoldDown > 0) && writeHmpPolicy(4, 0, this.mPreLowBatThresHoldUp, this.mPreLowBatThresHoldDown)) {
            Log.i("K3V3CpuGovernorPolicy", "close previously thermal Hmp policy : " + this.mPreLowBatThresHoldUp + " , " + this.mPreLowBatThresHoldDown);
            this.mPreLowBatThresHoldUp = 0;
            this.mPreLowBatThresHoldDown = 0;
        }
        if ((this.mPreThresHoldUp > 0 || this.mPreThresHoldDown > 0) && writeHmpPolicy(2, 0, this.mPreThresHoldUp, this.mPreThresHoldDown)) {
            Log.i("K3V3CpuGovernorPolicy", "close previously scene Hmp policy : " + this.mPreThresHoldUp + " , " + this.mPreThresHoldDown);
            this.mPreThresHoldUp = 0;
            this.mPreThresHoldDown = 0;
        }
    }

    protected void processCpuFreq(CpuFreqItem cpuItem) {
        if (!this.mIsCpuPathOk) {
            return;
        }
        if (cpuItem == null) {
            setToDefaultState();
            stopSysLoadCheck();
            return;
        }
        CpuFreqItem item = adaptCpuItem(cpuItem);
        MsgPolicyThreshold.getInstance().setThreshold(item.getPolicy(26), item.mActionId);
        if (item.getPolicy(17) != -1) {
            startSysLoadCheck(item.getPolicy(18), item.getPolicy(6), getSysLoadMaxFreq(), item.getPolicy(17));
        } else {
            stopSysLoadCheck();
        }
        if (this.mScalingFreqAdapter != null) {
            updateBoostPolicy(item);
            scaleBoost(item.getPolicy(43));
        }
        int maxFreq = item.getPolicy(6);
        int minFreq = item.getPolicy(7);
        scaleCpuFreq(matchAvailableCpuFreq(maxFreq, true), matchAvailableCpuFreq(minFreq, false));
        int maxGpuFreq = item.getPolicy(13);
        int minGpuFreq = item.getPolicy(14);
        scaleGpuFreq(matchAvailableFreq(2, maxGpuFreq, true), matchAvailableFreq(2, minGpuFreq, false));
        int maxDDRFreq = item.getPolicy(10);
        int minDDRFreq = item.getPolicy(11);
        scaleDDRFreq(matchAvailableFreq(3, maxDDRFreq, true), matchAvailableFreq(3, minDDRFreq, false));
        if (this.mIsCpuNumPathOk) {
            scaleCpuNum(item.getPolicy(4), item.getPolicy(5));
        }
        if (this.mHasHmpPolicyNode) {
            setHmpPolicy(cpuItem);
        }
        if (this.isLittleBigClusterCpu) {
            int maxA15CpuFreq = item.getPolicy(19);
            int minA15CpuFreq = item.getPolicy(20);
            scaleA15CpuFreq(matchAvailableFreq(1, maxA15CpuFreq, true), matchAvailableFreq(1, minA15CpuFreq, false));
        }
        if (this.mScalingFreqAdapter != null) {
            updateIpaPolicy(item);
            scaleIpa(item.getPolicy(40), item.getPolicy(39), item.getPolicy(41));
        }
        if (this.mScalingFreqAdapter != null) {
            updateForkOnBigPolicy(item);
            scaleForkOnBig(item.getPolicy(42));
        }
    }

    private void scaleCpuFreq(int maxFreq, int minFreq) {
        if (maxFreq > this.mDefaultMaxFreq) {
            maxFreq = this.mDefaultMaxFreq;
        }
        if (minFreq < this.mDefaultMinFreq) {
            minFreq = this.mDefaultMinFreq;
        }
        if (minFreq >= this.mPreFreqMax) {
            if (writeCpuFreq(maxFreq, true)) {
                this.mPreFreqMax = maxFreq;
            } else if (resetScalingCpuFreq(maxFreq, minFreq)) {
                this.mPreFreqMax = maxFreq;
                this.mPreFreqMin = minFreq;
            }
            if (writeCpuFreq(minFreq, false)) {
                this.mPreFreqMin = minFreq;
                return;
            } else if (resetScalingCpuFreq(maxFreq, minFreq)) {
                this.mPreFreqMax = maxFreq;
                this.mPreFreqMin = minFreq;
                return;
            } else {
                return;
            }
        }
        if (minFreq != this.mPreFreqMin) {
            if (writeCpuFreq(minFreq, false)) {
                this.mPreFreqMin = minFreq;
            } else if (resetScalingCpuFreq(maxFreq, minFreq)) {
                this.mPreFreqMax = maxFreq;
                this.mPreFreqMin = minFreq;
            }
        }
        if (maxFreq == this.mPreFreqMax) {
            return;
        }
        if (writeCpuFreq(maxFreq, true)) {
            this.mPreFreqMax = maxFreq;
        } else if (resetScalingCpuFreq(maxFreq, minFreq)) {
            this.mPreFreqMax = maxFreq;
            this.mPreFreqMin = minFreq;
        }
    }

    private void scaleGpuFreq(int maxFreq, int minFreq) {
        if (maxFreq > this.mDefaultGpuMaxFreq) {
            maxFreq = this.mDefaultGpuMaxFreq;
        }
        if (minFreq < this.mDefaultGpuMinFreq) {
            minFreq = this.mDefaultGpuMinFreq;
        }
        if (maxFreq >= this.mDefaultGpuMinFreq || !isThermalLimitGpuLessDefaultMin()) {
            if (minFreq >= this.mPreGpuFreqMax) {
                if (writeGpuFreq(maxFreq, true)) {
                    this.mPreGpuFreqMax = maxFreq;
                } else if (resetScalingGpuFreq(maxFreq, minFreq)) {
                    this.mPreGpuFreqMax = maxFreq;
                    this.mPreGpuFreqMin = minFreq;
                }
                if (writeGpuFreq(minFreq, false)) {
                    this.mPreGpuFreqMin = minFreq;
                } else if (resetScalingGpuFreq(maxFreq, minFreq)) {
                    this.mPreGpuFreqMax = maxFreq;
                    this.mPreGpuFreqMin = minFreq;
                }
            } else {
                if (minFreq != this.mPreGpuFreqMin) {
                    if (writeGpuFreq(minFreq, false)) {
                        this.mPreGpuFreqMin = minFreq;
                    } else if (resetScalingGpuFreq(maxFreq, minFreq)) {
                        this.mPreGpuFreqMax = maxFreq;
                        this.mPreGpuFreqMin = minFreq;
                    }
                }
                if (maxFreq != this.mPreGpuFreqMax) {
                    if (writeGpuFreq(maxFreq, true)) {
                        this.mPreGpuFreqMax = maxFreq;
                    } else if (resetScalingGpuFreq(maxFreq, minFreq)) {
                        this.mPreGpuFreqMax = maxFreq;
                        this.mPreGpuFreqMin = minFreq;
                    }
                }
            }
            return;
        }
        Log.i("K3V3CpuGovernorPolicy", "GPU: max:" + maxFreq + " min:" + minFreq + " PreGpuFreqMax:" + this.mPreGpuFreqMax);
        if (maxFreq != this.mPreGpuFreqMax) {
            if (writeGpuFreq(maxFreq, true)) {
                this.mPreGpuFreqMax = maxFreq;
            } else if (resetScalingGpuFreq(maxFreq, minFreq)) {
                this.mPreGpuFreqMax = maxFreq;
                this.mPreGpuFreqMin = minFreq;
            }
        }
    }

    private void scaleA15CpuFreq(int maxFreq, int minFreq) {
        if (this.isLittleBigClusterCpu && 1 == getCoreWorkState(4)) {
            if (maxFreq > this.mA15DefaultCpuMaxFreq) {
                maxFreq = this.mA15DefaultCpuMaxFreq;
            }
            if (minFreq < this.mA15DefaultCpuMinFreq) {
                minFreq = this.mA15DefaultCpuMinFreq;
            }
            if (minFreq >= this.mA15PreFreqMax) {
                if (writeA15CpuFreq(maxFreq, true)) {
                    this.mA15PreFreqMax = maxFreq;
                } else if (resetScalingA15CpuFreq(maxFreq, minFreq)) {
                    this.mA15PreFreqMax = maxFreq;
                    this.mA15PreFreqMin = minFreq;
                }
                if (writeA15CpuFreq(minFreq, false)) {
                    this.mA15PreFreqMin = minFreq;
                } else if (resetScalingA15CpuFreq(maxFreq, minFreq)) {
                    this.mA15PreFreqMax = maxFreq;
                    this.mA15PreFreqMin = minFreq;
                }
            } else {
                if (minFreq != this.mA15PreFreqMin) {
                    if (writeA15CpuFreq(minFreq, false)) {
                        this.mA15PreFreqMin = minFreq;
                    } else if (resetScalingA15CpuFreq(maxFreq, minFreq)) {
                        this.mA15PreFreqMax = maxFreq;
                        this.mA15PreFreqMin = minFreq;
                    }
                }
                if (maxFreq != this.mA15PreFreqMax) {
                    if (writeA15CpuFreq(maxFreq, true)) {
                        this.mA15PreFreqMax = maxFreq;
                    } else if (resetScalingA15CpuFreq(maxFreq, minFreq)) {
                        this.mA15PreFreqMax = maxFreq;
                        this.mA15PreFreqMin = minFreq;
                    }
                }
            }
        }
    }

    private void scaleDDRFreq(int maxFreq, int minFreq) {
        if (maxFreq > this.mDefaultDDRMaxFreq) {
            maxFreq = this.mDefaultDDRMaxFreq;
        }
        if (minFreq < this.mDefaultDDRMinFreq) {
            minFreq = this.mDefaultDDRMinFreq;
        }
        if (minFreq >= this.mPreDDRFreqMax) {
            if (writeDDRFreq(maxFreq, true)) {
                this.mPreDDRFreqMax = maxFreq;
            } else if (resetScalingDDRFreq(maxFreq, minFreq)) {
                this.mPreDDRFreqMax = maxFreq;
                this.mPreDDRFreqMin = minFreq;
            }
            if (writeDDRFreq(minFreq, false)) {
                this.mPreDDRFreqMin = minFreq;
                return;
            } else if (resetScalingDDRFreq(maxFreq, minFreq)) {
                this.mPreDDRFreqMax = maxFreq;
                this.mPreDDRFreqMin = minFreq;
                return;
            } else {
                return;
            }
        }
        if (minFreq != this.mPreDDRFreqMin) {
            if (writeDDRFreq(minFreq, false)) {
                this.mPreDDRFreqMin = minFreq;
            } else if (resetScalingDDRFreq(maxFreq, minFreq)) {
                this.mPreDDRFreqMax = maxFreq;
                this.mPreDDRFreqMin = minFreq;
            }
        }
        if (maxFreq == this.mPreDDRFreqMax) {
            return;
        }
        if (writeDDRFreq(maxFreq, true)) {
            this.mPreDDRFreqMax = maxFreq;
        } else if (resetScalingDDRFreq(maxFreq, minFreq)) {
            this.mPreDDRFreqMax = maxFreq;
            this.mPreDDRFreqMin = minFreq;
        }
    }

    private void scaleCpuNum(int maxNum, int minNum) {
        if (-1 == maxNum) {
            maxNum = this.mDefaultCpuMaxNum;
        }
        if (-1 == minNum) {
            if (maxNum >= this.mDefaultCpuBaseNum) {
                minNum = this.mDefaultCpuBaseNum;
            } else {
                minNum = 1;
            }
        }
        if (maxNum > this.mDefaultCpuMaxNum) {
            maxNum = this.mDefaultCpuMaxNum;
        } else if (maxNum < 1) {
            maxNum = 1;
        }
        if (minNum < 1) {
            minNum = 1;
        } else if (minNum > this.mDefaultCpuMaxNum) {
            minNum = this.mDefaultCpuMaxNum;
        }
        Log.i("K3V3CpuGovernorPolicy", "CPU NUM: max:" + maxNum + " min:" + minNum);
        if (maxNum != this.mPreCpuNumMax && writeCpuNum(maxNum, true)) {
            this.mPreCpuNumMax = maxNum;
        }
        if (minNum != this.mPreCpuNumMin && writeCpuNum(minNum, false)) {
            this.mPreCpuNumMin = minNum;
        }
    }

    private void scaleIpa(int ipaTemp, int ipaPower, int ipaSwitch) {
        if (this.mScalingFreqAdapter != null) {
            if (ipaTemp <= -1) {
                ipaTemp = 0;
            }
            if (ipaPower <= -1) {
                ipaPower = 0;
            }
            if (ipaSwitch <= -1) {
                ipaSwitch = 0;
            }
            if (ipaTemp != this.mPreIpaTemp) {
                if (this.mScalingFreqAdapter.setIpaTemp(ipaTemp)) {
                    this.mPreIpaTemp = ipaTemp;
                } else {
                    Log.w("K3V3CpuGovernorPolicy", "set ipa_temp :" + ipaTemp + " failed");
                }
            }
            if (ipaPower != this.mPreIpaPower) {
                if (this.mScalingFreqAdapter.setIpaPower(ipaPower)) {
                    this.mPreIpaPower = ipaPower;
                } else {
                    Log.w("K3V3CpuGovernorPolicy", "set ipa_power :" + ipaPower + " failed");
                }
            }
            if (ipaSwitch == this.mPreIpaSwitch) {
                return;
            }
            if (this.mScalingFreqAdapter.setIpaSwitch(ipaSwitch)) {
                this.mPreIpaSwitch = ipaSwitch;
            } else {
                Log.w("K3V3CpuGovernorPolicy", "set ipa_switch :" + ipaSwitch + " failed");
            }
        }
    }

    private void scaleForkOnBig(int forkOnBig) {
        if (this.mScalingFreqAdapter != null) {
            if (forkOnBig == -1) {
                forkOnBig = 1;
            }
            if (forkOnBig == this.mPreFrokOnBig) {
                return;
            }
            if (this.mScalingFreqAdapter.setForkOnClaster(forkOnBig)) {
                this.mPreFrokOnBig = forkOnBig;
            } else {
                Log.w("K3V3CpuGovernorPolicy", "set fork_on_big :" + forkOnBig + " failed");
            }
        }
    }

    private void scaleBoost(int boost) {
        if (this.mScalingFreqAdapter != null) {
            if (boost == -1) {
                boost = 0;
            }
            if (boost == this.mPreBoost) {
                return;
            }
            if (this.mScalingFreqAdapter.setBoost(boost)) {
                this.mPreBoost = boost;
            } else {
                Log.w("K3V3CpuGovernorPolicy", "set boost :" + boost + " failed");
            }
        }
    }

    private boolean writeCpuNum(int cpunum, boolean isCpuNumMax) {
        if (isCpuNumMax) {
            if (this.mFosCpuNumMax == null) {
                this.mFosCpuNumMax = getFreqStream("/sys/devices/system/cpu/cpuhotplug/cpu_num_limit");
            }
            if (!writeFreq(cpunum, this.mFosCpuNumMax)) {
                return false;
            }
        }
        if (this.mFosCpuNumMin == null) {
            this.mFosCpuNumMin = getFreqStream("/sys/devices/system/cpu/cpuhotplug/cpu_num_base");
        }
        if (!writeFreq(cpunum, this.mFosCpuNumMin)) {
            return false;
        }
        return true;
    }

    private boolean writeDDRFreq(int freq, boolean isFreqMax) {
        if (isFreqMax) {
            if (this.mFosDDRFreqMax == null) {
                this.mFosDDRFreqMax = getFreqStream(mDDRMaxFreqPath);
            }
            if (this.mScalingFreqAdapter != null) {
                return this.mScalingFreqAdapter.setDdrMax(freq);
            }
            if (!writeFreq(freq, this.mFosDDRFreqMax)) {
                return false;
            }
        }
        if (this.mFosDDRFreqMin == null) {
            this.mFosDDRFreqMin = getFreqStream(mDDRMinFreqPath);
        }
        if (this.mScalingFreqAdapter != null) {
            return this.mScalingFreqAdapter.setDdrMin(freq);
        }
        if (!writeFreq(freq, this.mFosDDRFreqMin)) {
            return false;
        }
        if (isFreqMax) {
            printFreqInfo(", DM:", freq);
        }
        return true;
    }

    private boolean writeA15CpuFreq(int freq, boolean isFreqMax) {
        if (isFreqMax) {
            if (this.mFosA15FreqMax == null) {
                this.mFosA15FreqMax = getFreqStream("/sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq");
            }
            if (this.mIDeviceState.isCtsRunning() && freq < 1708800) {
                Log.e("K3V3CpuGovernorPolicy", "CpufreqA15: " + freq + ", cts test the max cpu must be higt than 1708800.");
                return true;
            } else if (this.mScalingFreqAdapter != null) {
                return this.mScalingFreqAdapter.setBCpuMax(freq);
            } else {
                if (!writeFreq(freq, this.mFosA15FreqMax, false)) {
                    closeFos(this.mFosA15FreqMax);
                    this.mFosA15FreqMax = getFreqStream("/sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq");
                    if (!writeFreq(freq, this.mFosA15FreqMax)) {
                        return false;
                    }
                }
            }
        }
        if (this.mFosA15FreqMin == null) {
            this.mFosA15FreqMin = getFreqStream("/sys/devices/system/cpu/cpu4/cpufreq/scaling_min_freq");
        }
        if (this.mScalingFreqAdapter != null) {
            return this.mScalingFreqAdapter.setBCpuMin(freq);
        }
        if (!writeFreq(freq, this.mFosA15FreqMin, false)) {
            closeFos(this.mFosA15FreqMin);
            this.mFosA15FreqMin = getFreqStream("/sys/devices/system/cpu/cpu4/cpufreq/scaling_min_freq");
            if (!writeFreq(freq, this.mFosA15FreqMin)) {
                return false;
            }
        }
        if (isFreqMax) {
            printFreqInfo(", ACM:", freq);
        }
        return true;
    }

    private boolean writeGpuFreq(int freq, boolean isFreqMax) {
        if (isFreqMax) {
            if (this.mFosGpuFreqMax == null) {
                this.mFosGpuFreqMax = getFreqStream(mGpuMaxFreqPath);
            }
            if (this.mIDeviceState.isCtsRunning() && freq < 360000000) {
                Log.e("K3V3CpuGovernorPolicy", "gpufreq:" + freq + ", stc test the max gpu must be higt than 360000000.");
                return true;
            } else if (this.mScalingFreqAdapter != null) {
                return this.mScalingFreqAdapter.setGpuMax(freq);
            } else {
                if (!writeFreq(freq, this.mFosGpuFreqMax)) {
                    return false;
                }
            }
        }
        if (this.mFosGpuFreqMin == null) {
            this.mFosGpuFreqMin = getFreqStream(mGpuMinFreqPath);
        }
        if (this.mScalingFreqAdapter != null) {
            return this.mScalingFreqAdapter.setGpuMin(freq);
        }
        if (!writeFreq(freq, this.mFosGpuFreqMin)) {
            return false;
        }
        if (isFreqMax) {
            printFreqInfo(", GM:", freq);
        }
        return true;
    }

    private boolean writeCpuFreq(int freq, boolean isFreqMax) {
        if (isFreqMax) {
            if (this.mFosFreqMax == null) {
                this.mFosFreqMax = getFreqStream("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
            }
            if (this.mIDeviceState.isCtsRunning() && freq < 1708800) {
                Log.e("K3V3CpuGovernorPolicy", "Cpufreq:" + freq + ", stc test the max cpu must be higt than 1708800.");
                return true;
            } else if (this.mScalingFreqAdapter != null) {
                return this.mScalingFreqAdapter.setLCpuMax(freq);
            } else {
                if (!writeFreq(freq, this.mFosFreqMax)) {
                    return false;
                }
            }
        }
        if (this.mFosFreqMin == null) {
            this.mFosFreqMin = getFreqStream("/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
        }
        if (this.mScalingFreqAdapter != null) {
            return this.mScalingFreqAdapter.setLCpuMin(freq);
        }
        if (!writeFreq(freq, this.mFosFreqMin)) {
            return false;
        }
        if (isFreqMax) {
            printFreqInfo(", CM:", freq);
        }
        return true;
    }

    private FileOutputStream getFreqStream(String path) {
        try {
            return new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private void closeFos(FileOutputStream fos) {
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
            }
        }
    }

    private boolean writeFreq(int freq, FileOutputStream fos) {
        if (fos == null) {
            try {
                Log.w("K3V3CpuGovernorPolicy", "write freq file stream is null !");
                return false;
            } catch (IOException e) {
                Log.w("K3V3CpuGovernorPolicy", "IOException : write fail freq = " + freq);
                return false;
            }
        }
        byte[] byFreq = Integer.toString(freq).getBytes();
        fos.write(byFreq, 0, byFreq.length);
        return true;
    }

    private boolean writeFreq(int freq, FileOutputStream fos, boolean errorLog) {
        if (fos == null) {
            try {
                Log.w("K3V3CpuGovernorPolicy", "write freq file stream is null !");
                return false;
            } catch (IOException e) {
                if (errorLog) {
                    Log.w("K3V3CpuGovernorPolicy", "IOException : write fail freq = " + freq);
                }
                return false;
            }
        }
        byte[] byFreq = Integer.toString(freq).getBytes();
        fos.write(byFreq, 0, byFreq.length);
        return true;
    }

    private boolean resetScalingCpuFreq(int maxFreq, int minFreq) {
        if (writeCpuFreq(this.mDefaultMinFreq, false) && writeCpuFreq(this.mDefaultMaxFreq, true) && writeCpuFreq(maxFreq, true)) {
            return writeCpuFreq(minFreq, false);
        }
        return false;
    }

    private boolean resetScalingGpuFreq(int maxFreq, int minFreq) {
        if (writeGpuFreq(this.mDefaultGpuMinFreq, false) && writeGpuFreq(this.mDefaultGpuMaxFreq, true) && writeGpuFreq(maxFreq, true)) {
            return writeGpuFreq(minFreq, false);
        }
        return false;
    }

    private boolean resetScalingA15CpuFreq(int maxFreq, int minFreq) {
        if (writeA15CpuFreq(this.mA15DefaultCpuMinFreq, false) && writeA15CpuFreq(this.mA15DefaultCpuMaxFreq, true) && writeA15CpuFreq(maxFreq, true)) {
            return writeA15CpuFreq(minFreq, false);
        }
        return false;
    }

    private boolean resetScalingDDRFreq(int maxFreq, int minFreq) {
        if (writeDDRFreq(this.mDefaultDDRMinFreq, false) && writeDDRFreq(this.mDefaultDDRMaxFreq, true) && writeDDRFreq(maxFreq, true)) {
            return writeDDRFreq(minFreq, false);
        }
        return false;
    }

    protected void initFreqInfo(int type) {
        String stepFreqsPath;
        FileInputStream fis;
        Object obj;
        Throwable th;
        NumberFormatException e;
        String maxFreqPath = null;
        String minFreqPath = null;
        switch (type) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
                maxFreqPath = "/sys/devices/system/cpu/cpu4/cpufreq/cpuinfo_max_freq";
                minFreqPath = "/sys/devices/system/cpu/cpu4/cpufreq/scaling_min_freq";
                stepFreqsPath = "/sys/devices/system/cpu/cpu4/cpufreq/scaling_available_frequencies";
                break;
            case NativeAdapter.PLATFORM_HI /*2*/:
                stepFreqsPath = mGpuStepFreqsPath;
                break;
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                stepFreqsPath = mDDRStepFreqsPath;
                break;
            default:
                Log.e("K3V3CpuGovernorPolicy", "ERROR FREQ TYPE:" + type);
                return;
        }
        FileInputStream fis2 = null;
        AutoCloseable fis3;
        try {
            int len;
            StringBuffer stringBuffer;
            String[] freqSteps;
            int[] scaleStepFreq;
            int i;
            int i2;
            int defalutGpuMax;
            int defalutGpuMin;
            int defalutDdrMax;
            int defalutDdrMin;
            byte[] bytes = new byte[300];
            if (type == 1) {
                Arrays.fill(bytes, (byte) 0);
                fis = new FileInputStream(maxFreqPath);
                try {
                    len = fis.read(bytes);
                    if (len > 0) {
                        this.mA15DefaultCpuMaxFreq = Integer.parseInt(new String(bytes, 0, len, "UTF-8").trim());
                    }
                    IoUtils.closeQuietly(fis);
                    Arrays.fill(bytes, (byte) 0);
                    fis2 = new FileInputStream(minFreqPath);
                    len = fis2.read(bytes);
                    if (len > 0) {
                        this.mA15DefaultCpuMinFreq = Integer.parseInt(new String(bytes, 0, len, "UTF-8").trim());
                    }
                    IoUtils.closeQuietly(fis2);
                    if (this.mScalingFreqAdapter != null) {
                        int defalutA15CpuMax = this.mScalingFreqAdapter.getDefBCpuMax();
                        int defalutA15CpuMin = this.mScalingFreqAdapter.getDefBCpuMin();
                        if (defalutA15CpuMax <= 0 || defalutA15CpuMin <= 0) {
                            AutoCloseable fis4 = fis2;
                            stringBuffer = new StringBuffer(200);
                            fis3 = new FileInputStream(stepFreqsPath);
                            Arrays.fill(bytes, (byte) 0);
                            while (true) {
                                len = fis3.read(bytes);
                                if (len > 0) {
                                    stringBuffer.append(new String(bytes, 0, len, "UTF-8"));
                                } else {
                                    freqSteps = stringBuffer.toString().trim().split(" ");
                                    len = freqSteps.length;
                                    scaleStepFreq = new int[len];
                                    if (Integer.parseInt(freqSteps[0]) < Integer.parseInt(freqSteps[len - 1])) {
                                        len--;
                                        i = 0;
                                        while (len >= 0) {
                                            i2 = i + 1;
                                            scaleStepFreq[i] = Integer.parseInt(freqSteps[len]);
                                            len--;
                                            i = i2;
                                        }
                                    } else {
                                        for (i2 = 0; i2 < len; i2++) {
                                            scaleStepFreq[i2] = Integer.parseInt(freqSteps[i2]);
                                        }
                                    }
                                    if (type != 1) {
                                        this.mA15ScaleStepFreq = scaleStepFreq;
                                    } else if (type != 2) {
                                        this.mGpuScaleStepFreq = scaleStepFreq;
                                        this.mDefaultGpuMaxFreq = scaleStepFreq[0];
                                        this.mDefaultGpuMinFreq = scaleStepFreq[freqSteps.length - 1];
                                        if (this.mScalingFreqAdapter != null) {
                                            defalutGpuMax = this.mScalingFreqAdapter.getDefGpuMax();
                                            defalutGpuMin = this.mScalingFreqAdapter.getDefGpuMin();
                                            if (defalutGpuMax > 0 && defalutGpuMin > 0) {
                                                this.mDefaultGpuMaxFreq = defalutGpuMax;
                                                this.mDefaultGpuMinFreq = defalutGpuMin;
                                            }
                                        }
                                        this.mPreGpuFreqMax = this.mDefaultGpuMaxFreq;
                                        this.mPreGpuFreqMin = this.mDefaultGpuMinFreq;
                                    } else if (type == 3) {
                                        this.mDDRScaleStepFreq = scaleStepFreq;
                                        this.mDefaultDDRMaxFreq = scaleStepFreq[0];
                                        this.mDefaultDDRMinFreq = scaleStepFreq[freqSteps.length - 1];
                                        if (this.mScalingFreqAdapter != null) {
                                            defalutDdrMax = this.mScalingFreqAdapter.getDefDdrMax();
                                            defalutDdrMin = this.mScalingFreqAdapter.getDefDdrMin();
                                            if (defalutDdrMax > 0 && defalutDdrMin > 0) {
                                                this.mDefaultDDRMaxFreq = defalutDdrMax;
                                                this.mDefaultDDRMinFreq = defalutDdrMin;
                                            }
                                        }
                                        this.mPreDDRFreqMax = this.mDefaultDDRMaxFreq;
                                        this.mPreDDRFreqMin = this.mDefaultDDRMinFreq;
                                    }
                                    if (fis3 != null) {
                                        try {
                                            IoUtils.closeQuietly(fis3);
                                        } catch (Exception e2) {
                                        }
                                    }
                                }
                            }
                        }
                        this.mA15DefaultCpuMaxFreq = defalutA15CpuMax;
                        this.mA15DefaultCpuMinFreq = defalutA15CpuMin;
                        fis = fis2;
                        stringBuffer = new StringBuffer(200);
                        fis3 = new FileInputStream(stepFreqsPath);
                        Arrays.fill(bytes, (byte) 0);
                        while (true) {
                            len = fis3.read(bytes);
                            if (len > 0) {
                                freqSteps = stringBuffer.toString().trim().split(" ");
                                len = freqSteps.length;
                                scaleStepFreq = new int[len];
                                if (Integer.parseInt(freqSteps[0]) < Integer.parseInt(freqSteps[len - 1])) {
                                    for (i2 = 0; i2 < len; i2++) {
                                        scaleStepFreq[i2] = Integer.parseInt(freqSteps[i2]);
                                    }
                                } else {
                                    len--;
                                    i = 0;
                                    while (len >= 0) {
                                        i2 = i + 1;
                                        scaleStepFreq[i] = Integer.parseInt(freqSteps[len]);
                                        len--;
                                        i = i2;
                                    }
                                }
                                if (type != 1) {
                                    this.mA15ScaleStepFreq = scaleStepFreq;
                                } else if (type != 2) {
                                    this.mGpuScaleStepFreq = scaleStepFreq;
                                    this.mDefaultGpuMaxFreq = scaleStepFreq[0];
                                    this.mDefaultGpuMinFreq = scaleStepFreq[freqSteps.length - 1];
                                    if (this.mScalingFreqAdapter != null) {
                                        defalutGpuMax = this.mScalingFreqAdapter.getDefGpuMax();
                                        defalutGpuMin = this.mScalingFreqAdapter.getDefGpuMin();
                                        this.mDefaultGpuMaxFreq = defalutGpuMax;
                                        this.mDefaultGpuMinFreq = defalutGpuMin;
                                    }
                                    this.mPreGpuFreqMax = this.mDefaultGpuMaxFreq;
                                    this.mPreGpuFreqMin = this.mDefaultGpuMinFreq;
                                } else if (type == 3) {
                                    this.mDDRScaleStepFreq = scaleStepFreq;
                                    this.mDefaultDDRMaxFreq = scaleStepFreq[0];
                                    this.mDefaultDDRMinFreq = scaleStepFreq[freqSteps.length - 1];
                                    if (this.mScalingFreqAdapter != null) {
                                        defalutDdrMax = this.mScalingFreqAdapter.getDefDdrMax();
                                        defalutDdrMin = this.mScalingFreqAdapter.getDefDdrMin();
                                        this.mDefaultDDRMaxFreq = defalutDdrMax;
                                        this.mDefaultDDRMinFreq = defalutDdrMin;
                                    }
                                    this.mPreDDRFreqMax = this.mDefaultDDRMaxFreq;
                                    this.mPreDDRFreqMin = this.mDefaultDDRMinFreq;
                                }
                                if (fis3 != null) {
                                    IoUtils.closeQuietly(fis3);
                                }
                            }
                            stringBuffer.append(new String(bytes, 0, len, "UTF-8"));
                        }
                    }
                } catch (IOException e3) {
                    obj = fis;
                    try {
                        Log.e("K3V3CpuGovernorPolicy", "init FreqInfo failed ");
                        if (fis3 != null) {
                            try {
                                IoUtils.closeQuietly(fis3);
                            } catch (Exception e4) {
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fis3 != null) {
                            try {
                                IoUtils.closeQuietly(fis3);
                            } catch (Exception e5) {
                            }
                        }
                        throw th;
                    }
                } catch (NumberFormatException e6) {
                    e = e6;
                    obj = fis;
                    Log.w("K3V3CpuGovernorPolicy", "number format exception", e);
                    if (fis3 != null) {
                        try {
                            IoUtils.closeQuietly(fis3);
                        } catch (Exception e7) {
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    obj = fis;
                    if (fis3 != null) {
                        IoUtils.closeQuietly(fis3);
                    }
                    throw th;
                }
            }
            fis = fis2;
            stringBuffer = new StringBuffer(200);
            fis3 = new FileInputStream(stepFreqsPath);
            Arrays.fill(bytes, (byte) 0);
            while (true) {
                len = fis3.read(bytes);
                if (len > 0) {
                    stringBuffer.append(new String(bytes, 0, len, "UTF-8"));
                } else {
                    freqSteps = stringBuffer.toString().trim().split(" ");
                    len = freqSteps.length;
                    scaleStepFreq = new int[len];
                    if (Integer.parseInt(freqSteps[0]) < Integer.parseInt(freqSteps[len - 1])) {
                        len--;
                        i = 0;
                        while (len >= 0) {
                            i2 = i + 1;
                            scaleStepFreq[i] = Integer.parseInt(freqSteps[len]);
                            len--;
                            i = i2;
                        }
                    } else {
                        for (i2 = 0; i2 < len; i2++) {
                            scaleStepFreq[i2] = Integer.parseInt(freqSteps[i2]);
                        }
                    }
                    if (type != 1) {
                        this.mA15ScaleStepFreq = scaleStepFreq;
                    } else if (type != 2) {
                        this.mGpuScaleStepFreq = scaleStepFreq;
                        this.mDefaultGpuMaxFreq = scaleStepFreq[0];
                        this.mDefaultGpuMinFreq = scaleStepFreq[freqSteps.length - 1];
                        if (this.mScalingFreqAdapter != null) {
                            defalutGpuMax = this.mScalingFreqAdapter.getDefGpuMax();
                            defalutGpuMin = this.mScalingFreqAdapter.getDefGpuMin();
                            this.mDefaultGpuMaxFreq = defalutGpuMax;
                            this.mDefaultGpuMinFreq = defalutGpuMin;
                        }
                        this.mPreGpuFreqMax = this.mDefaultGpuMaxFreq;
                        this.mPreGpuFreqMin = this.mDefaultGpuMinFreq;
                    } else if (type == 3) {
                        this.mDDRScaleStepFreq = scaleStepFreq;
                        this.mDefaultDDRMaxFreq = scaleStepFreq[0];
                        this.mDefaultDDRMinFreq = scaleStepFreq[freqSteps.length - 1];
                        if (this.mScalingFreqAdapter != null) {
                            defalutDdrMax = this.mScalingFreqAdapter.getDefDdrMax();
                            defalutDdrMin = this.mScalingFreqAdapter.getDefDdrMin();
                            this.mDefaultDDRMaxFreq = defalutDdrMax;
                            this.mDefaultDDRMinFreq = defalutDdrMin;
                        }
                        this.mPreDDRFreqMax = this.mDefaultDDRMaxFreq;
                        this.mPreDDRFreqMin = this.mDefaultDDRMinFreq;
                    }
                    if (fis3 != null) {
                        IoUtils.closeQuietly(fis3);
                    }
                }
            }
        } catch (IOException e8) {
            Log.e("K3V3CpuGovernorPolicy", "init FreqInfo failed ");
            if (fis3 != null) {
                IoUtils.closeQuietly(fis3);
            }
        } catch (NumberFormatException e9) {
            e = e9;
            Log.w("K3V3CpuGovernorPolicy", "number format exception", e);
            if (fis3 != null) {
                IoUtils.closeQuietly(fis3);
            }
        }
    }

    private boolean isThermalLimitGpuLessDefaultMin() {
        if (CONFIG_THERMAL_GPU_LESS_MIN) {
            if (this.mThermalItem != null) {
                int value = this.mThermalItem.getPolicy(13);
                if (value > 0 && value < this.mDefaultGpuMinFreq) {
                    Log.i("K3V3CpuGovernorPolicy", "thermal limit value:" + value + " default Gpu Min Freq:" + this.mDefaultGpuMinFreq);
                    return true;
                }
            }
            return false;
        }
        Log.w("K3V3CpuGovernorPolicy", "not permit limit max gpu less than scaling min freq");
        return false;
    }

    protected void initAllFreqInfo() {
        this.mPreFreqMax = this.mDefaultMaxFreq;
        this.mPreFreqMin = this.mDefaultMinFreq;
        if (this.isLittleBigClusterCpu) {
            initFreqInfo(1);
        }
        initFreqInfo(2);
        initFreqInfo(3);
    }

    protected boolean initCpuNumInfo() {
        Object obj;
        NumberFormatException e;
        Throwable th;
        AutoCloseable autoCloseable = null;
        try {
            byte[] bytes = new byte[300];
            Arrays.fill(bytes, (byte) 0);
            FileInputStream fis = new FileInputStream("/sys/devices/system/cpu/cpuhotplug/cpu_num_limit");
            try {
                int len = fis.read(bytes);
                if (len > 0) {
                    this.mDefaultCpuMaxNum = Integer.parseInt(new String(bytes, 0, len, "UTF-8").trim());
                }
                IoUtils.closeQuietly(fis);
                Arrays.fill(bytes, (byte) 0);
                autoCloseable = new FileInputStream("/sys/devices/system/cpu/cpuhotplug/cpu_num_base");
                len = autoCloseable.read(bytes);
                if (len > 0) {
                    this.mDefaultCpuBaseNum = Integer.parseInt(new String(bytes, 0, len, "UTF-8").trim());
                }
                IoUtils.closeQuietly(autoCloseable);
                Log.i("K3V3CpuGovernorPolicy", "default: cpu_num_max = " + this.mDefaultCpuMaxNum + ", cpu_num_min = " + this.mDefaultCpuBaseNum);
                if (autoCloseable != null) {
                    try {
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Exception e2) {
                        Log.w("K3V3CpuGovernorPolicy", "close failed " + e2);
                    }
                }
                return true;
            } catch (FileNotFoundException e3) {
                obj = fis;
                Log.e("K3V3CpuGovernorPolicy", "cpuhotplug node not exist ");
                if (autoCloseable != null) {
                    try {
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Exception e22) {
                        Log.w("K3V3CpuGovernorPolicy", "close failed " + e22);
                    }
                }
                return false;
            } catch (IOException e4) {
                obj = fis;
                Log.e("K3V3CpuGovernorPolicy", "read failed exception");
                if (autoCloseable != null) {
                    try {
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Exception e222) {
                        Log.w("K3V3CpuGovernorPolicy", "close failed " + e222);
                    }
                }
                return false;
            } catch (NumberFormatException e5) {
                e = e5;
                obj = fis;
                try {
                    Log.w("K3V3CpuGovernorPolicy", "number format exception", e);
                    if (autoCloseable != null) {
                        try {
                            IoUtils.closeQuietly(autoCloseable);
                        } catch (Exception e2222) {
                            Log.w("K3V3CpuGovernorPolicy", "close failed " + e2222);
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (autoCloseable != null) {
                        try {
                            IoUtils.closeQuietly(autoCloseable);
                        } catch (Exception e22222) {
                            Log.w("K3V3CpuGovernorPolicy", "close failed " + e22222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                obj = fis;
                if (autoCloseable != null) {
                    IoUtils.closeQuietly(autoCloseable);
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            Log.e("K3V3CpuGovernorPolicy", "cpuhotplug node not exist ");
            if (autoCloseable != null) {
                IoUtils.closeQuietly(autoCloseable);
            }
            return false;
        } catch (IOException e7) {
            Log.e("K3V3CpuGovernorPolicy", "read failed exception");
            if (autoCloseable != null) {
                IoUtils.closeQuietly(autoCloseable);
            }
            return false;
        } catch (NumberFormatException e8) {
            e = e8;
            Log.w("K3V3CpuGovernorPolicy", "number format exception", e);
            if (autoCloseable != null) {
                IoUtils.closeQuietly(autoCloseable);
            }
            return false;
        }
    }

    protected boolean requireToProcess(CpuFreqItem item) {
        return (matchAvailableCpuFreq(this.mActionItem.getPolicy(6), true) == this.mPreFreqMax && matchAvailableCpuFreq(this.mActionItem.getPolicy(7), false) == this.mPreFreqMin && matchAvailableFreq(2, this.mActionItem.getPolicy(13), true) == this.mPreGpuFreqMax && matchAvailableFreq(2, this.mActionItem.getPolicy(14), false) == this.mPreGpuFreqMin && this.isLittleBigClusterCpu && matchAvailableFreq(1, this.mActionItem.getPolicy(19), true) == this.mA15PreFreqMax && this.isLittleBigClusterCpu && matchAvailableFreq(1, this.mActionItem.getPolicy(20), false) == this.mA15PreFreqMin && matchAvailableFreq(3, this.mActionItem.getPolicy(10), true) == this.mPreDDRFreqMax && matchAvailableFreq(3, this.mActionItem.getPolicy(11), false) == this.mPreDDRFreqMin && this.mActionItem.getPolicy(23) == this.mPreThresHoldUp && this.mActionItem.getPolicy(24) == this.mPreThresHoldDown && this.mActionItem.getPolicy(18) == -1 && this.mActionItem.getPolicy(41) == this.mPreIpaSwitch && this.mActionItem.getPolicy(39) == this.mPreIpaPower && this.mActionItem.getPolicy(40) == this.mPreIpaTemp && this.mActionItem.getPolicy(42) == this.mPreFrokOnBig && this.mActionItem.getPolicy(43) == this.mPreBoost && !MsgPolicyThreshold.getInstance().requireToProcessMsgPlicy(item.getPolicy(26))) ? false : true;
    }

    protected int getCoreWorkState(int core) {
        Throwable th;
        FileInputStream fileInputStream = null;
        byte[] bytes = new byte[10];
        int online = -1;
        StringBuffer corePathBuf = new StringBuffer();
        corePathBuf.append("/sys/devices/system/cpu/cpu").append(core).append("/online");
        String corePath = corePathBuf.toString().trim();
        if (new File(corePath).exists()) {
            try {
                Arrays.fill(bytes, (byte) 0);
                FileInputStream fis = new FileInputStream(corePath);
                try {
                    int len = fis.read(bytes);
                    if (len > 0) {
                        String onlineStr = new String(bytes, 0, len, "UTF-8");
                        try {
                            online = Integer.parseInt(onlineStr.trim());
                            String str = onlineStr;
                        } catch (Exception e) {
                            fileInputStream = fis;
                            try {
                                Log.e("K3V3CpuGovernorPolicy", "read failed: " + corePath);
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (Exception e2) {
                                        Log.w("K3V3CpuGovernorPolicy", "close failed: " + corePath);
                                    }
                                }
                                return online;
                            } catch (Throwable th2) {
                                th = th2;
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (Exception e3) {
                                        Log.w("K3V3CpuGovernorPolicy", "close failed: " + corePath);
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            fileInputStream = fis;
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            throw th;
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (Exception e4) {
                            Log.w("K3V3CpuGovernorPolicy", "close failed: " + corePath);
                        }
                    }
                    fileInputStream = fis;
                } catch (Exception e5) {
                    fileInputStream = fis;
                    Log.e("K3V3CpuGovernorPolicy", "read failed: " + corePath);
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return online;
                } catch (Throwable th4) {
                    th = th4;
                    fileInputStream = fis;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (Exception e6) {
                Log.e("K3V3CpuGovernorPolicy", "read failed: " + corePath);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return online;
            }
            return online;
        }
        Log.w("K3V3CpuGovernorPolicy", "not exist path : " + corePath);
        return -1;
    }

    private int getLimitForkOnBigState() {
        int thermalValue = 1;
        int lowbatValue = 1;
        synchronized (this.mThermalItemLock) {
            if (this.mThermalItem != null) {
                thermalValue = this.mThermalItem.getPolicy(42);
            }
        }
        synchronized (this.mLowBatItemLock) {
            if (this.mLowBatItem != null) {
                lowbatValue = this.mLowBatItem.getPolicy(42);
            }
        }
        if (thermalValue == 0 || lowbatValue == 0) {
            return 0;
        }
        return 1;
    }

    private void updateForkOnBigPolicy(CpuFreqItem item) {
        int limitValue = getLimitForkOnBigState();
        int sceneValue = item.getPolicy(42);
        if (limitValue == 0 || sceneValue == 0) {
            item.addPolicy(42, 0);
        } else {
            item.addPolicy(42, 1);
        }
    }

    private int getLimitBoostState() {
        int thermalValue = 0;
        int lowbatValue = 0;
        synchronized (this.mThermalItemLock) {
            if (this.mThermalItem != null) {
                thermalValue = this.mThermalItem.getPolicy(43);
            }
        }
        synchronized (this.mLowBatItemLock) {
            if (this.mLowBatItem != null) {
                lowbatValue = this.mLowBatItem.getPolicy(43);
            }
        }
        if (thermalValue == 4 || lowbatValue == 4) {
            return 4;
        }
        return 0;
    }

    private void updateBoostPolicy(CpuFreqItem item) {
        int limitValue = getLimitBoostState();
        int sceneValue = item.getPolicy(43);
        if (limitValue == 4 || sceneValue == 4) {
            item.addPolicy(43, 4);
        } else {
            item.addPolicy(43, 0);
        }
    }

    private void updateIpaPolicy(CpuFreqItem item) {
        int ipaLimitTemp = getLimitMinFreq(40);
        int ipaLimitPower = getLimitMinFreq(39);
        int ipaLimitSwitch = getLimitMinFreq(41);
        if (ipaLimitTemp > 0) {
            item.addPolicy(40, getMinFreq(ipaLimitTemp, item.getPolicy(40)));
        }
        if (ipaLimitPower > 0) {
            item.addPolicy(39, getMinFreq(ipaLimitPower, item.getPolicy(39)));
        }
        if (ipaLimitSwitch > 0) {
            item.addPolicy(41, getMinFreq(ipaLimitSwitch, item.getPolicy(41)));
        }
    }

    private void setHmpPolicy(CpuFreqItem cpuItem) {
        int lowBatThresHoldUp;
        int lowBatThresHoldDown;
        int sceneThresholdUp;
        int sceneThresholdDown;
        int thermThresHoldUp = -1;
        int thermThresHoldDown = -1;
        if (this.mThermalItem != null) {
            synchronized (this.mThermalItemLock) {
                if (this.mThermalItem != null) {
                    thermThresHoldUp = this.mThermalItem.getPolicy(23);
                    thermThresHoldDown = this.mThermalItem.getPolicy(24);
                }
            }
            if (thermThresHoldUp > 0 && thermThresHoldDown > 0 && (!(thermThresHoldUp == this.mPreThermalThresHoldUp && thermThresHoldDown == this.mPreThermalThresHoldDown) && writeHmpPolicy(4, 1, thermThresHoldUp, thermThresHoldDown))) {
                this.mPreThermalThresHoldUp = thermThresHoldUp;
                this.mPreThermalThresHoldDown = thermThresHoldDown;
                Log.i("K3V3CpuGovernorPolicy", "set thermal Hmp policy : " + thermThresHoldUp + " , " + thermThresHoldDown);
            }
        }
        if (!(this.mThermalItem == null || cpuItem.mActionId == 10000)) {
            if (thermThresHoldUp == 0 && thermThresHoldDown == 0) {
            }
            lowBatThresHoldUp = -1;
            lowBatThresHoldDown = -1;
            if (this.mLowBatItem != null) {
                synchronized (this.mLowBatItemLock) {
                    if (this.mLowBatItem != null) {
                        lowBatThresHoldUp = this.mLowBatItem.getPolicy(23);
                        lowBatThresHoldDown = this.mLowBatItem.getPolicy(24);
                    }
                }
                if (lowBatThresHoldUp > 0 && lowBatThresHoldDown > 0 && (!(lowBatThresHoldUp == this.mPreLowBatThresHoldUp && lowBatThresHoldDown == this.mPreLowBatThresHoldDown) && writeHmpPolicy(4, 1, lowBatThresHoldUp, lowBatThresHoldDown))) {
                    this.mPreLowBatThresHoldUp = lowBatThresHoldUp;
                    this.mPreLowBatThresHoldDown = lowBatThresHoldDown;
                    Log.i("K3V3CpuGovernorPolicy", "set lowbattery Hmp policy : " + lowBatThresHoldUp + " , " + lowBatThresHoldDown);
                }
            }
            if (!(this.mLowBatItem == null || cpuItem.mActionId == 10000)) {
                if (lowBatThresHoldUp == 0 && lowBatThresHoldDown == 0) {
                }
                sceneThresholdUp = cpuItem.getPolicy(23);
                sceneThresholdDown = cpuItem.getPolicy(24);
                if (-1 != sceneThresholdUp || -1 == sceneThresholdDown) {
                    if (-1 == sceneThresholdUp || -1 != sceneThresholdDown) {
                        Log.w("K3V3CpuGovernorPolicy", "scene HMP policy config is error !");
                    } else if ((this.mPreThresHoldUp > 0 || this.mPreThresHoldDown > 0) && writeHmpPolicy(2, 0, this.mPreThresHoldUp, this.mPreThresHoldDown)) {
                        Log.i("K3V3CpuGovernorPolicy", "close previously scene Hmp policy : " + this.mPreThresHoldUp + " , " + this.mPreThresHoldDown);
                        this.mPreThresHoldUp = 0;
                        this.mPreThresHoldDown = 0;
                        return;
                    } else {
                        return;
                    }
                } else if (!(sceneThresholdUp == this.mPreThresHoldUp && sceneThresholdDown == this.mPreThresHoldDown) && writeHmpPolicy(2, 1, sceneThresholdUp, sceneThresholdDown)) {
                    this.mPreThresHoldUp = sceneThresholdUp;
                    this.mPreThresHoldDown = sceneThresholdDown;
                    Log.i("K3V3CpuGovernorPolicy", "set scene Hmp policy : " + sceneThresholdUp + " , " + sceneThresholdDown);
                    return;
                } else {
                    return;
                }
            }
            if ((this.mPreLowBatThresHoldUp > 0 || this.mPreLowBatThresHoldDown > 0) && writeHmpPolicy(4, 0, this.mPreLowBatThresHoldUp, this.mPreLowBatThresHoldDown)) {
                Log.i("K3V3CpuGovernorPolicy", "close previously lowbattery Hmp policy : " + this.mPreLowBatThresHoldUp + " , " + this.mPreLowBatThresHoldDown);
                this.mPreLowBatThresHoldUp = 0;
                this.mPreLowBatThresHoldDown = 0;
            }
            sceneThresholdUp = cpuItem.getPolicy(23);
            sceneThresholdDown = cpuItem.getPolicy(24);
            if (-1 != sceneThresholdUp) {
            }
            if (-1 == sceneThresholdUp) {
            }
            Log.w("K3V3CpuGovernorPolicy", "scene HMP policy config is error !");
        }
        if ((this.mPreThermalThresHoldUp > 0 || this.mPreThermalThresHoldDown > 0) && writeHmpPolicy(4, 0, this.mPreThermalThresHoldUp, this.mPreThermalThresHoldDown)) {
            Log.i("K3V3CpuGovernorPolicy", "close previously thermal Hmp policy : " + this.mPreThermalThresHoldUp + " , " + this.mPreThermalThresHoldDown);
            this.mPreThermalThresHoldUp = 0;
            this.mPreThermalThresHoldDown = 0;
        }
        lowBatThresHoldUp = -1;
        lowBatThresHoldDown = -1;
        if (this.mLowBatItem != null) {
            synchronized (this.mLowBatItemLock) {
                if (this.mLowBatItem != null) {
                    lowBatThresHoldUp = this.mLowBatItem.getPolicy(23);
                    lowBatThresHoldDown = this.mLowBatItem.getPolicy(24);
                }
            }
            this.mPreLowBatThresHoldUp = lowBatThresHoldUp;
            this.mPreLowBatThresHoldDown = lowBatThresHoldDown;
            Log.i("K3V3CpuGovernorPolicy", "set lowbattery Hmp policy : " + lowBatThresHoldUp + " , " + lowBatThresHoldDown);
        }
        Log.i("K3V3CpuGovernorPolicy", "close previously lowbattery Hmp policy : " + this.mPreLowBatThresHoldUp + " , " + this.mPreLowBatThresHoldDown);
        this.mPreLowBatThresHoldUp = 0;
        this.mPreLowBatThresHoldDown = 0;
        sceneThresholdUp = cpuItem.getPolicy(23);
        sceneThresholdDown = cpuItem.getPolicy(24);
        if (-1 != sceneThresholdUp) {
        }
        if (-1 == sceneThresholdUp) {
        }
        Log.w("K3V3CpuGovernorPolicy", "scene HMP policy config is error !");
    }

    private boolean writeHmpPolicy(int type, int state, int thresholdUp, int thresholdDown) {
        if (this.mScalingFreqAdapter != null) {
            return this.mScalingFreqAdapter.setHmp(type, state, thresholdUp, thresholdDown);
        }
        try {
            if (this.mFosHMPPolicy == null) {
                this.mFosHMPPolicy = new FileOutputStream("/sys/kernel/set_hmp_thresholds/policy");
            }
            StringBuffer policyBuf = new StringBuffer();
            policyBuf.append("powergenie").append(",");
            policyBuf.append(type).append(",");
            policyBuf.append(state).append(",");
            policyBuf.append(thresholdUp).append(",");
            policyBuf.append(thresholdDown);
            byte[] byFreq = policyBuf.toString().trim().getBytes();
            this.mFosHMPPolicy.write(byFreq, 0, byFreq.length);
            return true;
        } catch (IOException e) {
            Log.w("K3V3CpuGovernorPolicy", "Unable to write /sys/kernel/set_hmp_thresholds/policy");
            return false;
        }
    }

    private boolean isExistPolicyPath(String path) {
        if (new File(path).exists()) {
            return true;
        }
        return false;
    }

    public void dump(PrintWriter pw, String[] args) {
        super.dump(pw, args);
        pw.println("    K3V3 CPU GOVENOR POLICY:");
        pw.println("        Default CPU Max Freq : " + this.mDefaultMaxFreq + " Min Freq : " + this.mDefaultMinFreq);
        pw.println("        Default A15 Max Freq : " + this.mA15DefaultCpuMaxFreq + " Min Freq : " + this.mA15DefaultCpuMinFreq);
        pw.println("        Default GPU Max Freq : " + this.mDefaultGpuMaxFreq + "  Min Freq : " + this.mDefaultGpuMinFreq);
        pw.println("        Default DDR Max Freq : " + this.mDefaultDDRMaxFreq + "  Min Freq : " + this.mDefaultDDRMinFreq);
        pw.println("        Default CPU Base Num : " + this.mDefaultCpuBaseNum);
        pw.println("        Default CPU Max Num : " + this.mDefaultCpuMaxNum + "  Min Num : " + 1);
        pw.println("        Default IPA Power : 0 IPA Temp : 0  IPA Switch Temp : 0");
        pw.println("        Default Fork On Big ON : 1  Fork On Big OFF : 0");
        pw.println("");
        pw.println("        Current Cpu Max Freq : " + this.mPreFreqMax + " Min Cpu Freq : " + this.mPreFreqMin);
        pw.println("           Read Cpu Max Freq : " + CpuGovernorPolicy.readFileContent("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq") + " Min Cpu Freq : " + CpuGovernorPolicy.readFileContent("/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"));
        pw.println("        Current A15 Max Freq : " + this.mA15PreFreqMax + " Min Freq : " + this.mA15PreFreqMin);
        pw.println("           Read A15 Max Freq : " + CpuGovernorPolicy.readFileContent("/sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq") + " Min Freq : " + CpuGovernorPolicy.readFileContent("/sys/devices/system/cpu/cpu4/cpufreq/scaling_min_freq"));
        pw.println("        Current Gpu Max Freq : " + this.mPreGpuFreqMax + " Min Freq : " + this.mPreGpuFreqMin);
        pw.println("        Current DDR Max Freq : " + this.mPreDDRFreqMax + " Min Freq : " + this.mPreDDRFreqMin);
        pw.println("        Current Scene HMP Up : " + this.mPreThresHoldUp + " Down : " + this.mPreThresHoldDown);
        pw.println("        Current Thermal HMP Up : " + this.mPreThermalThresHoldUp + " Down : " + this.mPreThermalThresHoldDown);
        pw.println("        Current Lowbattery HMP Up : " + this.mPreLowBatThresHoldUp + " Down : " + this.mPreLowBatThresHoldDown);
        pw.println("        Current Cpu Max Num : " + this.mPreCpuNumMax + " Min Num : " + this.mPreCpuNumMin);
        pw.println("        Current IPA Power : " + this.mPreIpaPower + " Control Temp : " + this.mPreIpaTemp + " Switch Temp : " + this.mPreIpaSwitch);
        pw.println("        Current Fork On Big Value : " + this.mPreFrokOnBig);
    }
}
