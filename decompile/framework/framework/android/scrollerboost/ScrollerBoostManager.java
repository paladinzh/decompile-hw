package android.scrollerboost;

import android.app.admin.DevicePolicyManager;
import android.content.pm.PackageManager;
import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import android.util.Jlog;

public class ScrollerBoostManager {
    private static final long BOOST_TIME_LENGTH = 1000;
    private static final int DEFAULT_ENABLE_SKIPPED_FRAMES = 0;
    private static final int INVALID_VALUE = -1;
    private static final int MESSAGE_RESET_AFFINITY = 1;
    private static final int SWITCH_SCROLLER_BOOST = 8;
    private static final String TAG = "ScrollerBoostManager";
    private static ScrollerBoostManager sScrollerBoostManager;
    private boolean mBoostByEachFling;
    private int mBoostCpuMinFreq;
    private int mBoostDefaultDuration;
    private int mBoostDuration;
    private boolean mBoostSwitch;
    private boolean mEnableBoostByJank;
    private long mEnableSkippedFrames = 0;
    private int mIPAMaxPower;
    private boolean mIsBigcoreBoost;
    private long mLastBoostTime = 0;
    private Handler mResetAffinityHandler = new ResetAffinityHandler();

    private static class ResetAffinityHandler extends Handler {
        private ResetAffinityHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                int pid = Process.myPid();
                if (pid > 0) {
                    try {
                        Process.setProcessAffinity(pid, 255);
                    } catch (RuntimeException e) {
                        AwareLog.e(ScrollerBoostManager.TAG, "Exception in invoke: " + e.getMessage());
                    }
                }
            }
        }
    }

    private ScrollerBoostManager() {
    }

    public static synchronized ScrollerBoostManager getInstance() {
        ScrollerBoostManager scrollerBoostManager;
        synchronized (ScrollerBoostManager.class) {
            if (sScrollerBoostManager == null) {
                sScrollerBoostManager = new ScrollerBoostManager();
            }
            scrollerBoostManager = sScrollerBoostManager;
        }
        return scrollerBoostManager;
    }

    private void initBoostProperty() {
        this.mBoostSwitch = false;
        this.mBoostDefaultDuration = SystemProperties.getInt("persist.sys.boost.durationms", -1);
        if (this.mBoostDefaultDuration > 0) {
            this.mIsBigcoreBoost = SystemProperties.getBoolean("persist.sys.boost.isbigcore", false);
            int freqmin = SystemProperties.getInt("persist.sys.boost.freqmin.b", -1);
            if (freqmin > 0) {
                if (this.mIsBigcoreBoost) {
                    this.mBoostCpuMinFreq = (freqmin / 100) + DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX;
                } else {
                    this.mBoostCpuMinFreq = (freqmin / 100) + 65536;
                }
                int ipapower = SystemProperties.getInt("persist.sys.boost.ipapower", -1);
                if (ipapower > 0) {
                    this.mIPAMaxPower = PackageManager.MATCH_ENCRYPTION_AWARE_AND_UNAWARE + ipapower;
                } else {
                    this.mIPAMaxPower = -1;
                }
                this.mEnableSkippedFrames = (long) SystemProperties.getInt("persist.sys.boost.skipframe", 0);
                this.mBoostByEachFling = SystemProperties.getBoolean("persist.sys.boost.byeachfling", false);
                this.mBoostSwitch = true;
            }
        }
    }

    private void resetAffinity() {
        if (this.mResetAffinityHandler != null) {
            this.mResetAffinityHandler.sendMessageDelayed(this.mResetAffinityHandler.obtainMessage(1), (long) this.mBoostDuration);
        }
    }

    private void cancelResetAffinity() {
        if (this.mResetAffinityHandler != null) {
            this.mResetAffinityHandler.removeMessages(1);
        }
    }

    private boolean isAwareScrollerBoostEnable() {
        boolean awareEnable = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
        boolean cpuEnable = WifiEnterpriseConfig.ENGINE_ENABLE.equals(SystemProperties.get("persist.sys.cpuset.enable", WifiEnterpriseConfig.ENGINE_DISABLE));
        int featureFlag = SystemProperties.getInt("persist.sys.cpuset.subswitch", 0);
        if (awareEnable && cpuEnable && (featureFlag & 8) != 0) {
            return true;
        }
        return false;
    }

    public void init() {
        if (Jlog.isHisiChipset() && isAwareScrollerBoostEnable()) {
            initBoostProperty();
        }
    }

    public boolean isBoostEnable() {
        return this.mBoostSwitch ? isPerformanceMode() : false;
    }

    public void boost(int duration) {
        if (duration <= 0 || duration > this.mBoostDefaultDuration) {
            this.mBoostDuration = this.mBoostDefaultDuration;
        } else {
            this.mBoostDuration = duration;
        }
        this.mLastBoostTime = System.currentTimeMillis();
        int pid = Process.myPid();
        if (pid > 0) {
            if (this.mIsBigcoreBoost) {
                cancelResetAffinity();
                try {
                    Process.setProcessAffinity(pid, 240);
                    resetAffinity();
                } catch (RuntimeException e) {
                    AwareLog.e(TAG, "Exception in invoke: " + e.getMessage());
                    return;
                }
            }
            if (this.mEnableBoostByJank) {
                doScrollerBoost();
            }
        }
    }

    private boolean isPerformanceMode() {
        return "true".equals(SystemProperties.get("persist.sys.performance", "false"));
    }

    private void doScrollerBoost() {
        if (this.mIPAMaxPower == -1) {
            Jlog.perfEvent(4096, ProxyInfo.LOCAL_EXCL_LIST, new int[]{this.mBoostDuration, this.mBoostCpuMinFreq});
            return;
        }
        Jlog.perfEvent(4096, ProxyInfo.LOCAL_EXCL_LIST, new int[]{this.mBoostDuration, this.mBoostCpuMinFreq, this.mIPAMaxPower});
    }

    public void updateFrameJankInfo(long skippedFrames) {
        if (!this.mBoostSwitch) {
            return;
        }
        if (this.mBoostByEachFling || !this.mEnableBoostByJank) {
            long scrollerBoostTime = System.currentTimeMillis() - this.mLastBoostTime;
            if (skippedFrames >= this.mEnableSkippedFrames && scrollerBoostTime <= BOOST_TIME_LENGTH) {
                if (!this.mBoostByEachFling) {
                    this.mEnableBoostByJank = true;
                }
                doScrollerBoost();
            }
        }
    }
}
