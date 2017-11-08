package com.huawei.powergenie.modules.resgovernor;

import android.os.Message;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.policy.PolicyProvider;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

public final class MsmCpuGovernorPolicy extends CpuGovernorPolicy {
    private static MsmCpuGovernorPolicy sInstance;
    private GpuGovernorPolicy mGpuPolicy = null;
    private boolean mIsInitialDefaultFreq = false;
    private HashMap<Integer, CpuCluster> mPolicyCpus = new HashMap();

    static MsmCpuGovernorPolicy getInstance(ICoreContext pgcontext, CpuGovernor cpuGovernor) {
        if (sInstance == null) {
            sInstance = new MsmCpuGovernorPolicy(pgcontext, cpuGovernor);
        }
        return sInstance;
    }

    private MsmCpuGovernorPolicy(ICoreContext pgcontext, CpuGovernor cpuGovernor) {
        super(pgcontext, cpuGovernor);
        this.mGpuPolicy = initGpuGovernor(pgcontext);
    }

    protected void handleStart() {
        super.handleStart();
        for (Integer intValue : mConfigCpuPolicys) {
            initCpuPolicy(intValue.intValue());
        }
    }

    protected boolean handleCpuAction(PowerAction action, int subActionFlag) {
        if (!super.handleCpuAction(action, subActionFlag)) {
            return false;
        }
        if (this.mActionItem != null) {
            for (Entry entry : this.mActionItem.getPolicyMap().entrySet()) {
                initCpuPolicy(((Integer) entry.getKey()).intValue());
            }
        }
        this.mPolicyHandler.removeMessages(100);
        if (shouldScaleFreq()) {
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
        CpuCluster cpu0 = (CpuCluster) this.mPolicyCpus.get(Integer.valueOf(0));
        return cpu0 != null ? cpu0.matchAvailableCpuFreq(cpuFreq, matchMax) : cpuFreq;
    }

    private boolean shouldScaleFreq() {
        if (this.mCurFreqItem == null || shouldCpuScaleFreq() || ((this.mGpuPolicy != null && this.mGpuPolicy.checkGpuAction(this.mActionItem)) || this.mActionItem.getPolicy(18) != -1)) {
            return true;
        }
        return MsgPolicyThreshold.getInstance().requireToProcessMsgPlicy(this.mActionItem.getPolicy(26));
    }

    private boolean shouldCpuScaleFreq() {
        boolean ret = false;
        for (Entry entry : this.mPolicyCpus.entrySet()) {
            int core = ((Integer) entry.getKey()).intValue();
            ret |= ((CpuCluster) entry.getValue()).checkCpuAction(this.mActionItem.getPolicy(((Integer) PolicyProvider.mMaxProfile.get(Integer.valueOf(core))).intValue()), this.mActionItem.getPolicy(((Integer) PolicyProvider.mMinProfile.get(Integer.valueOf(core))).intValue()));
        }
        return ret;
    }

    private CpuCluster initCpuCluster(int core) {
        CpuCluster cluster = new CpuCluster();
        if (cluster.createCpuCluster(core)) {
            Log.d("MsmCpuGovernorPolicy", "create CPU" + core + " cluster ok.");
            return cluster;
        }
        Log.d("MsmCpuGovernorPolicy", "create CPU" + core + " cluster failed.");
        return null;
    }

    private void initCpuPolicy(int policyID) {
        if (PolicyProvider.mTagToCpuCore.containsKey(Integer.valueOf(policyID))) {
            int core = ((Integer) PolicyProvider.mTagToCpuCore.get(Integer.valueOf(policyID))).intValue();
            if (!this.mPolicyCpus.containsKey(Integer.valueOf(core))) {
                CpuCluster policyCpu = initCpuCluster(core);
                if (policyCpu != null) {
                    this.mPolicyCpus.put(Integer.valueOf(core), policyCpu);
                }
            }
        }
    }

    private GpuGovernorPolicy initGpuGovernor(ICoreContext pgcontext) {
        GpuGovernorPolicy gpuPolicy = GpuGovernorPolicy.getInstance(pgcontext);
        if (gpuPolicy.initGpuFreqInfo()) {
            Log.d("MsmCpuGovernorPolicy", "create GPU governor ok.");
            return gpuPolicy;
        }
        Log.d("MsmCpuGovernorPolicy", "create GPU governor failed.");
        return null;
    }

    protected void setToDefaultState() {
        super.setToDefaultState();
        for (Entry entry : this.mPolicyCpus.entrySet()) {
            ((CpuCluster) entry.getValue()).setToDefaultState(getLimitMinFreq(((Integer) PolicyProvider.mMaxProfile.get(Integer.valueOf(((Integer) entry.getKey()).intValue()))).intValue()));
        }
        if (this.mGpuPolicy != null) {
            this.mGpuPolicy.setToDefaultState(getLimitMinFreq(13));
        }
    }

    protected void processCpuFreq(CpuFreqItem cpuItem) {
        if (cpuItem == null) {
            setToDefaultState();
            stopSysLoadCheck();
            return;
        }
        CpuFreqItem item = adaptCpuItem(cpuItem);
        if (!this.mIsInitialDefaultFreq) {
            CpuCluster cpu0 = (CpuCluster) this.mPolicyCpus.get(Integer.valueOf(0));
            if (cpu0 != null) {
                int defaultMaxFreq = cpu0.getDefaultMaxFreq();
                int defaultMinFreq = cpu0.getDefaultMinFreq();
                if (!(defaultMaxFreq == -1 || defaultMinFreq == -1)) {
                    this.mDefaultMaxFreq = defaultMaxFreq;
                    this.mDefaultMinFreq = defaultMinFreq;
                    this.mIsInitialDefaultFreq = true;
                }
            }
        }
        MsgPolicyThreshold.getInstance().setThreshold(item.getPolicy(26), item.mActionId);
        if (item.getPolicy(17) != -1) {
            startSysLoadCheck(item.getPolicy(18), item.getPolicy(((Integer) PolicyProvider.mMaxProfile.get(Integer.valueOf(0))).intValue()), getSysLoadMaxFreq(), item.getPolicy(17));
        } else {
            stopSysLoadCheck();
        }
        if (this.mGpuPolicy != null) {
            this.mGpuPolicy.processGpuFreq(item);
        }
        for (Entry entry : this.mPolicyCpus.entrySet()) {
            int core = ((Integer) entry.getKey()).intValue();
            CpuCluster cpu = (CpuCluster) entry.getValue();
            int maxFreq = item.getPolicy(((Integer) PolicyProvider.mMaxProfile.get(Integer.valueOf(core))).intValue());
            int minFreq = item.getPolicy(((Integer) PolicyProvider.mMinProfile.get(Integer.valueOf(core))).intValue());
            int minCores = -1;
            if (core == 0) {
                minCores = item.getPolicy(5);
            }
            cpu.processCpuFreq(maxFreq, minFreq, minCores);
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        super.dump(pw, args);
        pw.println("    Msm CPU GOVENOR POLICY:");
        for (Entry entry : this.mPolicyCpus.entrySet()) {
            int core = ((Integer) entry.getKey()).intValue();
            ((CpuCluster) entry.getValue()).dump(pw, args);
        }
        if (this.mGpuPolicy != null) {
            this.mGpuPolicy.dump(pw, args);
        }
    }
}
