package com.huawei.powergenie.modules.resgovernor;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.ThermalAction;
import com.huawei.powergenie.core.policy.PolicyProvider;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import libcore.io.IoUtils;

public class CpuGovernorPolicy {
    protected static ArrayList<Integer> mConfigCpuPolicys = new ArrayList();
    protected static final ConcurrentHashMap<Integer, CpuFreqItem> mCpuPolicyHash = new ConcurrentHashMap();
    private static HashMap<String, Integer> mCustCombActionId = new HashMap();
    protected static final HashMap<Integer, Integer> mTypeToNodeFd = new HashMap();
    protected static final HashMap<Integer, String> mTypeToNodeName = new HashMap<Integer, String>() {
        {
            put(Integer.valueOf(3), "ipps_policy");
            put(Integer.valueOf(4), "cpu_number_max");
            put(Integer.valueOf(5), "cpu_number_min");
            put(Integer.valueOf(6), "cpu_maxprofile");
            put(Integer.valueOf(7), "cpu_minprofile");
            put(Integer.valueOf(8), "cpu_number_lock");
            put(Integer.valueOf(9), "cpu_profile_block");
            put(Integer.valueOf(10), "ddr_maxprofile");
            put(Integer.valueOf(11), "ddr_minprofile");
            put(Integer.valueOf(12), "ddr_profile_block");
            put(Integer.valueOf(13), "gpu_maxprofile");
            put(Integer.valueOf(14), "gpu_minprofile");
            put(Integer.valueOf(15), "gpu_profile_block");
        }
    };
    protected CpuFreqItem mActionItem;
    private int mCheckLoadActionId = -1;
    private String mCheckLoadPkg = null;
    protected ContentResolver mContentResolver;
    protected CpuFreqItem mCpuDrawItem = null;
    protected final Object mCpuDrawItemLock = new Object();
    protected CpuGovernor mCpuGovernor;
    protected int mCurDefaultActionId = 230;
    protected CpuFreqItem mCurFreqItem;
    protected String mCurPowerPkgName = null;
    private int mCurSubActionFlag = 3;
    protected int mCustMaxActionId = 15000;
    protected int mDefaultMaxFreq = 1800000;
    protected int mDefaultMinFreq = 320000;
    private boolean mHasStartCheck = false;
    protected final ICoreContext mICoreContext;
    protected final IDeviceState mIDeviceState;
    protected final IPolicy mIPolicy;
    protected int mLoadPolicyId = 0;
    protected CpuFreqItem mLowBatItem = null;
    protected final Object mLowBatItemLock = new Object();
    private int mParentActionId = 230;
    protected int mParentDefaultActionId = 230;
    private String mParentPowerPkgName = null;
    protected PolicyManager mPolicyHandler;
    protected CpuFreqItem mPreFreqItem;
    protected int[] mScaleStepFreq;
    protected final ScalingFreqAdapter mScalingFreqAdapter;
    private int mSysLoadCheckMaxFreq = this.mDefaultMaxFreq;
    protected CpuFreqItem mThermalItem = null;
    protected final Object mThermalItemLock = new Object();

    public class PolicyManager extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    CpuGovernorPolicy.this.processCpuFreq(msg.obj == null ? null : msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public CpuGovernorPolicy(ICoreContext pgcontext, CpuGovernor cpuGovernor) {
        this.mICoreContext = pgcontext;
        this.mCpuGovernor = cpuGovernor;
        this.mIPolicy = (IPolicy) this.mICoreContext.getService("policy");
        this.mIDeviceState = (IDeviceState) this.mICoreContext.getService("device");
        this.mContentResolver = pgcontext.getContext().getContentResolver();
        this.mPolicyHandler = new PolicyManager();
        this.mScalingFreqAdapter = ScalingFreqAdapter.getFreqAdapter(pgcontext);
        if (this.mScalingFreqAdapter != null) {
            Log.i("CpuGovernorPolicy", "scaling freq adapter implement");
        }
    }

    protected void handleStart() {
        if (this.mICoreContext.isRestartAfterCrash()) {
            Log.i("CpuGovernorPolicy", "handleStart,crashed is true");
            setToDefaultState();
        }
        loadCpuPolicyCfg(this.mIPolicy.getPowerMode());
    }

    protected boolean handleCpuAction(PowerAction action, int subActionFlag) {
        if (!handleLimitFreqAction(action)) {
            return false;
        }
        int actionId = action.getActionId();
        this.mCurPowerPkgName = action.getPkgName();
        if (requireMatchCust(actionId, subActionFlag)) {
            int matchCustId = matchCustPkgName(this.mCurPowerPkgName);
            if (matchCustId != 0) {
                actionId = matchCustId;
            }
        }
        if (this.mCurFreqItem != null && this.mCurFreqItem.mActionId == actionId && this.mCurSubActionFlag == subActionFlag) {
            return false;
        }
        if (this.mCurFreqItem != null && this.mCurFreqItem.mActionId == 10000 && (subActionFlag == 1 || subActionFlag == 2)) {
            return false;
        }
        if (this.mCurFreqItem != null && ((this.mCurFreqItem.mActionId == 246 || this.mCurFreqItem.mActionId == 210 || this.mCurFreqItem.mActionId == 221 || this.mCurFreqItem.mActionId == 267) && (actionId == 238 || actionId == 239))) {
            return false;
        }
        this.mActionItem = null;
        if (subActionFlag == 2) {
            this.mCurPowerPkgName = this.mParentPowerPkgName;
            this.mCurDefaultActionId = this.mParentActionId;
            this.mActionItem = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(this.mParentActionId));
        } else if (subActionFlag == 1) {
            int custId = matchCustComb(actionId, this.mParentPowerPkgName);
            if (custId != 0) {
                this.mActionItem = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(custId));
                this.mCurDefaultActionId = custId;
            } else {
                Integer combId = (Integer) mCustCombActionId.get(Integer.toString(actionId) + Integer.toString(this.mParentActionId));
                if (combId == null || !mCpuPolicyHash.containsKey(combId)) {
                    this.mActionItem = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(actionId));
                    this.mCurDefaultActionId = action.getActionId();
                } else {
                    this.mActionItem = (CpuFreqItem) mCpuPolicyHash.get(combId);
                    this.mCurDefaultActionId = combId.intValue();
                }
            }
        } else {
            this.mActionItem = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(actionId));
            this.mCurDefaultActionId = action.getActionId();
        }
        if (this.mActionItem == null) {
            return false;
        }
        if (subActionFlag == 2 && this.mCurSubActionFlag == 3) {
            return false;
        }
        if (subActionFlag == 3) {
            this.mParentDefaultActionId = this.mCurDefaultActionId;
            this.mParentActionId = this.mActionItem.mActionId;
            this.mParentPowerPkgName = action.getPkgName();
        }
        this.mPreFreqItem = this.mCurFreqItem;
        this.mCurSubActionFlag = subActionFlag;
        return true;
    }

    protected boolean initCpuFreqInfo() {
        IOException e;
        Throwable th;
        NumberFormatException e2;
        Object obj;
        AutoCloseable autoCloseable = null;
        try {
            byte[] bytes = new byte[300];
            Arrays.fill(bytes, (byte) 0);
            FileInputStream fis = new FileInputStream("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
            try {
                int len = fis.read(bytes);
                if (len > 0) {
                    this.mDefaultMaxFreq = Integer.parseInt(new String(bytes, 0, len, "UTF-8").trim());
                    this.mSysLoadCheckMaxFreq = this.mDefaultMaxFreq;
                }
                IoUtils.closeQuietly(fis);
                Arrays.fill(bytes, (byte) 0);
                autoCloseable = new FileInputStream("/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
                len = autoCloseable.read(bytes);
                if (len > 0) {
                    this.mDefaultMinFreq = Integer.parseInt(new String(bytes, 0, len, "UTF-8").trim());
                }
                IoUtils.closeQuietly(autoCloseable);
                if (this.mScalingFreqAdapter != null) {
                    int defalutCpuMax = this.mScalingFreqAdapter.getDefLCpuMax();
                    int defalutCpuMin = this.mScalingFreqAdapter.getDefLCpuMin();
                    if (defalutCpuMax > 0 && defalutCpuMin > 0) {
                        this.mDefaultMaxFreq = defalutCpuMax;
                        this.mDefaultMinFreq = defalutCpuMin;
                    }
                }
                Log.i("CpuGovernorPolicy", "default: scaling_max_freq= " + this.mDefaultMaxFreq + ", scaling_min_freq=" + this.mDefaultMinFreq);
                StringBuffer sbFreqs = new StringBuffer(200);
                fis = new FileInputStream("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
                Arrays.fill(bytes, (byte) 0);
                while (true) {
                    len = fis.read(bytes);
                    if (len <= 0) {
                        break;
                    }
                    sbFreqs.append(new String(bytes, 0, len, "UTF-8"));
                }
                String strFreq = sbFreqs.toString().trim();
                Log.d("CpuGovernorPolicy", "scaling_available_frequencies:" + strFreq);
                String[] freqSteps = strFreq.split(" ");
                len = freqSteps.length;
                this.mScaleStepFreq = new int[len];
                int i;
                if (Integer.parseInt(freqSteps[0]) < Integer.parseInt(freqSteps[len - 1])) {
                    len--;
                    int i2 = 0;
                    while (len >= 0) {
                        i = i2 + 1;
                        this.mScaleStepFreq[i2] = Integer.parseInt(freqSteps[len]);
                        len--;
                        i2 = i;
                    }
                } else {
                    for (i = 0; i < len; i++) {
                        this.mScaleStepFreq[i] = Integer.parseInt(freqSteps[i]);
                    }
                }
                if (fis != null) {
                    try {
                        IoUtils.closeQuietly(fis);
                    } catch (Exception e3) {
                        Log.w("CpuGovernorPolicy", "close failed " + e3);
                    }
                }
                return true;
            } catch (IOException e4) {
                e = e4;
                autoCloseable = fis;
                try {
                    Log.e("CpuGovernorPolicy", "read failed " + e);
                    if (autoCloseable != null) {
                        try {
                            IoUtils.closeQuietly(autoCloseable);
                        } catch (Exception e32) {
                            Log.w("CpuGovernorPolicy", "close failed " + e32);
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (autoCloseable != null) {
                        try {
                            IoUtils.closeQuietly(autoCloseable);
                        } catch (Exception e322) {
                            Log.w("CpuGovernorPolicy", "close failed " + e322);
                        }
                    }
                    throw th;
                }
            } catch (NumberFormatException e5) {
                e2 = e5;
                obj = fis;
                Log.w("CpuGovernorPolicy", e2.getCause());
                if (autoCloseable != null) {
                    try {
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Exception e3222) {
                        Log.w("CpuGovernorPolicy", "close failed " + e3222);
                    }
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                obj = fis;
                if (autoCloseable != null) {
                    IoUtils.closeQuietly(autoCloseable);
                }
                throw th;
            }
        } catch (IOException e6) {
            e = e6;
            Log.e("CpuGovernorPolicy", "read failed " + e);
            if (autoCloseable != null) {
                IoUtils.closeQuietly(autoCloseable);
            }
            return false;
        } catch (NumberFormatException e7) {
            e2 = e7;
            Log.w("CpuGovernorPolicy", e2.getCause());
            if (autoCloseable != null) {
                IoUtils.closeQuietly(autoCloseable);
            }
            return false;
        }
    }

    private int refreshActionId(String pkgName, int actionId) {
        int tmpId = matchCustPkgName(pkgName);
        if (tmpId == 0) {
            return actionId;
        }
        return tmpId;
    }

    private void refreshCurPolicy() {
        int curActionId = refreshActionId(this.mCurPowerPkgName, this.mCurDefaultActionId);
        this.mParentActionId = refreshActionId(this.mParentPowerPkgName, this.mParentDefaultActionId);
        if (this.mCurSubActionFlag == 2) {
            this.mCurFreqItem = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(this.mParentActionId));
        } else {
            this.mCurFreqItem = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(curActionId));
        }
        processCpuFreq(this.mCurFreqItem);
        this.mPreFreqItem = this.mCurFreqItem;
    }

    public int matchAvailableCpuFreq(int cpuFreq, boolean matchMax) {
        if (-1 == cpuFreq) {
            if (matchMax) {
                return this.mDefaultMaxFreq;
            }
            return this.mDefaultMinFreq;
        } else if (cpuFreq < this.mDefaultMinFreq) {
            return this.mDefaultMinFreq;
        } else {
            if (cpuFreq > this.mDefaultMaxFreq) {
                return this.mDefaultMaxFreq;
            }
            int highFreq = this.mDefaultMaxFreq;
            int lowFreq = this.mDefaultMinFreq;
            if (this.mScaleStepFreq != null) {
                for (int i = 0; i < this.mScaleStepFreq.length; i++) {
                    if (cpuFreq >= this.mScaleStepFreq[i]) {
                        lowFreq = this.mScaleStepFreq[i];
                        break;
                    }
                    highFreq = this.mScaleStepFreq[i];
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

    protected void handlePowerMode(int newMode) {
        setToDefaultState();
        loadCpuPolicyCfg(newMode);
    }

    protected void handleVRMode(boolean connect) {
        Log.i("CpuGovernorPolicy", "VR connect:" + connect);
        if (connect) {
            loadCpuPolicyCfg(99);
        } else {
            loadCpuPolicyCfg(this.mIPolicy.getPowerMode());
        }
        refreshCurPolicy();
    }

    protected void startSysLoadCheck(int mode, int curFreq, int maxFreq, int delay) {
    }

    protected void stopSysLoadCheck() {
    }

    private boolean loadCpuPolicyCfg(int policyId, boolean force) {
        if (!force && this.mLoadPolicyId == policyId) {
            return true;
        }
        this.mLoadPolicyId = policyId;
        ArrayList<Integer> idsList = getPolicyFromDB(policyId);
        if (idsList == null) {
            Log.e("CpuGovernorPolicy", "load cpu policy fail! no any actions!");
            return false;
        }
        this.mCpuGovernor.registerActions(idsList);
        return true;
    }

    private boolean loadCpuPolicyCfg(int policyId) {
        return loadCpuPolicyCfg(policyId, false);
    }

    private static int matchCustComb(int subActionID, String packageName) {
        if (packageName == null || packageName.equals("")) {
            return 0;
        }
        for (Entry entry : mCustCombActionId.entrySet()) {
            String key = (String) entry.getKey();
            if (key != null && key.startsWith(Integer.toString(subActionID)) && Pattern.compile(key, 66).matcher(packageName).find()) {
                return ((Integer) entry.getValue()).intValue();
            }
        }
        return 0;
    }

    private static int matchCustPkgName(String packageName) {
        if (packageName == null || packageName.equals("")) {
            return 0;
        }
        for (Entry entry : mCpuPolicyHash.entrySet()) {
            Integer actionid = (Integer) entry.getKey();
            if (actionid.intValue() >= 10000 && actionid.intValue() <= 20000) {
                CpuFreqItem freqItem = (CpuFreqItem) entry.getValue();
                if (freqItem.mPkgName == null) {
                    Log.w("CpuGovernorPolicy", "match cust pkg name, item pkg is null!");
                } else if (freqItem.mPkgName.equalsIgnoreCase(packageName)) {
                    return actionid.intValue();
                } else {
                    if (Pattern.compile(freqItem.mPkgName, 66).matcher(packageName).find()) {
                        return actionid.intValue();
                    }
                }
            }
        }
        return 0;
    }

    private boolean careCpuPolicy(int actionId, int powerMode) {
        if (!SystemProperties.getBoolean("persist.sys.iaware.cpuenable", false)) {
            return true;
        }
        if ((powerMode != 2 && powerMode != 3) || actionId == 508 || actionId == 251 || actionId == 319 || actionId == 320 || actionId == 333 || actionId == 334 || actionId == 231 || actionId == 208 || actionId == 10000) {
            return true;
        }
        return false;
    }

    private ArrayList<Integer> getPolicyFromDB(int powerMode) {
        RuntimeException ex;
        Cursor cursor = this.mContentResolver.query(PolicyProvider.CPU_POLICY_URI, null, "power_mode = " + powerMode, null, null);
        if (cursor == null) {
            Log.w("CpuGovernorPolicy", "protected table is not exist. powerMode =" + powerMode);
            return null;
        }
        ArrayList<Integer> actionsList = new ArrayList();
        mCpuPolicyHash.clear();
        int count = 0;
        int idCol = cursor.getColumnIndex("action_id");
        int typeCol = cursor.getColumnIndex("policy_type");
        int valueCol = cursor.getColumnIndex("policy_value");
        int pkgCol = cursor.getColumnIndex("pkg_name");
        int extendCol = cursor.getColumnIndex("extend");
        while (cursor.moveToNext()) {
            int parentActionId = -1;
            int actionId = cursor.getInt(idCol);
            if (careCpuPolicy(actionId, powerMode)) {
                int policyType = cursor.getInt(typeCol);
                int policyValue = cursor.getInt(valueCol);
                String pkgName = cursor.getString(pkgCol);
                String extend = cursor.getString(extendCol);
                if (extend != null) {
                    try {
                        parentActionId = Integer.parseInt(extend);
                    } catch (NumberFormatException e) {
                    }
                }
                if (actionId < 10000) {
                    if (!actionsList.contains(Integer.valueOf(actionId))) {
                        actionsList.add(Integer.valueOf(actionId));
                    }
                }
                if (parentActionId >= 0) {
                    String key;
                    if (parentActionId == 0) {
                        key = Integer.toString(actionId) + "|" + pkgName;
                    } else {
                        try {
                            key = Integer.toString(actionId) + Integer.toString(parentActionId);
                        } catch (RuntimeException e2) {
                            ex = e2;
                        }
                    }
                    if (mCustCombActionId.containsKey(key)) {
                        actionId = ((Integer) mCustCombActionId.get(key)).intValue();
                    } else {
                        count++;
                        actionId = count + 20000;
                        mCustCombActionId.put(key, Integer.valueOf(actionId));
                        Log.d("CpuGovernorPolicy", "key = (" + key + ") actionId = (" + actionId + ")");
                    }
                }
                CpuFreqItem item = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(actionId));
                if (item == null) {
                    CpuFreqItem cpuFreqItem = new CpuFreqItem(actionId, pkgName);
                    try {
                        mCpuPolicyHash.put(Integer.valueOf(actionId), cpuFreqItem);
                        item = cpuFreqItem;
                    } catch (RuntimeException e3) {
                        ex = e3;
                        item = cpuFreqItem;
                    } catch (Throwable th) {
                        Throwable th2 = th;
                        item = cpuFreqItem;
                    }
                }
                item.addPolicy(policyType, policyValue);
                if (!mConfigCpuPolicys.contains(Integer.valueOf(policyType))) {
                    mConfigCpuPolicys.add(Integer.valueOf(policyType));
                }
            }
        }
        cursor.close();
        return actionsList;
        try {
            Log.e("CpuGovernorPolicy", "RuntimeException:", ex);
            cursor.close();
            return actionsList;
        } catch (Throwable th3) {
            th2 = th3;
            cursor.close();
            throw th2;
        }
    }

    protected void processCpuFreq(CpuFreqItem item) {
    }

    protected void setToDefaultState() {
        this.mCurFreqItem = null;
        this.mPreFreqItem = null;
        this.mActionItem = null;
    }

    protected long getDelayTime(CpuFreqItem item, int actionId, int subActionFlag) {
        if (item == null || subActionFlag == 2 || item.getPolicy(25) < 0) {
            return getDelayTime(actionId, subActionFlag);
        }
        Log.i("CpuGovernorPolicy", "delay cpuItem: id = " + item.mActionId + ", delay time  = " + item.getPolicy(25));
        return (long) item.getPolicy(25);
    }

    protected long getDelayTime(int actionId, int subActionFlag) {
        if (2 == subActionFlag) {
            switch (actionId) {
                case 211:
                case 244:
                case 268:
                case 501:
                    return 0;
                case 232:
                    return 1000;
                default:
                    return 500;
            }
        }
        switch (actionId) {
            case 204:
            case 205:
            case 225:
            case 228:
                return 2000;
            case 231:
            case 234:
            case 236:
            case 238:
            case 245:
            case 251:
            case 267:
            case 502:
            case 508:
                return 0;
            default:
                return 700;
        }
    }

    protected int getMinFreq(int value1, int value2) {
        if (value1 <= 0 || value2 <= 0) {
            if (value1 <= value2) {
                value1 = value2;
            }
            return value1;
        }
        if (value1 >= value2) {
            value1 = value2;
        }
        return value1;
    }

    protected int getLimitMinFreq(int policyType) {
        int thermalValue = 0;
        int lowbatValue = 0;
        synchronized (this.mThermalItemLock) {
            if (this.mThermalItem != null) {
                thermalValue = this.mThermalItem.getPolicy(policyType);
            }
        }
        synchronized (this.mLowBatItemLock) {
            if (this.mLowBatItem != null) {
                lowbatValue = this.mLowBatItem.getPolicy(policyType);
            }
        }
        return getMinFreq(thermalValue, lowbatValue);
    }

    protected CpuFreqItem adaptCpuItem(CpuFreqItem cpuItem) {
        CpuFreqItem item = cpuItem.clone();
        if (10000 == cpuItem.mActionId) {
            return item;
        }
        if (this.mThermalItem == null && this.mLowBatItem == null && this.mCpuDrawItem == null) {
            return item;
        }
        int cpuLimitFreq = getLimitMinFreq(6);
        int cpu1LimitFreq = getLimitMinFreq(27);
        int cpu2LimitFreq = getLimitMinFreq(29);
        int cpu3LimitFreq = getLimitMinFreq(31);
        int a15CpuLimitFreq = getLimitMinFreq(19);
        int gpuLimitFreq = getLimitMinFreq(13);
        int frcLimitFreq = getLimitMinFreq(16);
        int minCPUProfileValue;
        if (cpuLimitFreq > 0) {
            int cpuFreq = getMinFreq(cpuLimitFreq, item.getPolicy(6));
            item.addPolicy(6, cpuFreq);
            minCPUProfileValue = item.getPolicy(7);
            if (minCPUProfileValue != -1 && minCPUProfileValue > cpuFreq) {
                item.addPolicy(7, -1);
            }
        } else if (this.mCpuDrawItem != null && cpuItem.mActionId < 10000) {
            int maxCPUProfileValue = -1;
            synchronized (this.mCpuDrawItemLock) {
                if (this.mCpuDrawItem != null) {
                    maxCPUProfileValue = this.mCpuDrawItem.getPolicy(6);
                }
            }
            item.addPolicy(6, maxCPUProfileValue);
            if (maxCPUProfileValue != -1) {
                minCPUProfileValue = item.getPolicy(7);
                if (minCPUProfileValue != -1 && minCPUProfileValue > maxCPUProfileValue) {
                    item.addPolicy(7, -1);
                }
            }
        }
        int minCPU1ProfileValue;
        if (cpu1LimitFreq > 0) {
            int cpu1Freq = getMinFreq(cpu1LimitFreq, item.getPolicy(27));
            item.addPolicy(27, cpu1Freq);
            minCPU1ProfileValue = item.getPolicy(28);
            if (minCPU1ProfileValue != -1 && minCPU1ProfileValue > cpu1Freq) {
                item.addPolicy(28, -1);
            }
        } else if (this.mCpuDrawItem != null && cpuItem.mActionId < 10000) {
            int maxCPU1ProfileValue = -1;
            synchronized (this.mCpuDrawItemLock) {
                if (this.mCpuDrawItem != null) {
                    maxCPU1ProfileValue = this.mCpuDrawItem.getPolicy(27);
                }
            }
            item.addPolicy(27, maxCPU1ProfileValue);
            if (maxCPU1ProfileValue != -1) {
                minCPU1ProfileValue = item.getPolicy(28);
                if (minCPU1ProfileValue != -1 && minCPU1ProfileValue > maxCPU1ProfileValue) {
                    item.addPolicy(28, -1);
                }
            }
        }
        int minCPU2ProfileValue;
        if (cpu2LimitFreq > 0) {
            int cpu2Freq = getMinFreq(cpu2LimitFreq, item.getPolicy(29));
            item.addPolicy(29, cpu2Freq);
            minCPU2ProfileValue = item.getPolicy(30);
            if (minCPU2ProfileValue != -1 && minCPU2ProfileValue > cpu2Freq) {
                item.addPolicy(30, -1);
            }
        } else if (this.mCpuDrawItem != null && cpuItem.mActionId < 10000) {
            int maxCPU2ProfileValue = -1;
            synchronized (this.mCpuDrawItemLock) {
                if (this.mCpuDrawItem != null) {
                    maxCPU2ProfileValue = this.mCpuDrawItem.getPolicy(29);
                }
            }
            item.addPolicy(29, maxCPU2ProfileValue);
            if (maxCPU2ProfileValue != -1) {
                minCPU2ProfileValue = item.getPolicy(30);
                if (minCPU2ProfileValue != -1 && minCPU2ProfileValue > maxCPU2ProfileValue) {
                    item.addPolicy(30, -1);
                }
            }
        }
        int minCPU3ProfileValue;
        if (cpu3LimitFreq > 0) {
            int cpu3Freq = getMinFreq(cpu3LimitFreq, item.getPolicy(31));
            item.addPolicy(31, cpu3Freq);
            minCPU3ProfileValue = item.getPolicy(32);
            if (minCPU3ProfileValue != -1 && minCPU3ProfileValue > cpu3Freq) {
                item.addPolicy(32, -1);
            }
        } else if (this.mCpuDrawItem != null && cpuItem.mActionId < 10000) {
            int maxCPU3ProfileValue = -1;
            synchronized (this.mCpuDrawItemLock) {
                if (this.mCpuDrawItem != null) {
                    maxCPU3ProfileValue = this.mCpuDrawItem.getPolicy(31);
                }
            }
            item.addPolicy(31, maxCPU3ProfileValue);
            if (maxCPU3ProfileValue != -1) {
                minCPU3ProfileValue = item.getPolicy(32);
                if (minCPU3ProfileValue != -1 && minCPU3ProfileValue > maxCPU3ProfileValue) {
                    item.addPolicy(32, -1);
                }
            }
        }
        if (a15CpuLimitFreq > 0) {
            int a15CpuFreq = getMinFreq(a15CpuLimitFreq, item.getPolicy(19));
            item.addPolicy(19, a15CpuFreq);
            int minA15CpuProfileValue = item.getPolicy(20);
            if (minA15CpuProfileValue != -1 && minA15CpuProfileValue > a15CpuFreq) {
                item.addPolicy(20, -1);
            }
        } else if (this.mCpuDrawItem != null && cpuItem.mActionId < 10000) {
            int maxA15CPUProfileValue = -1;
            synchronized (this.mCpuDrawItemLock) {
                if (this.mCpuDrawItem != null) {
                    maxA15CPUProfileValue = this.mCpuDrawItem.getPolicy(19);
                }
            }
            item.addPolicy(19, maxA15CPUProfileValue);
            if (maxA15CPUProfileValue != -1) {
                int minA15CPUProfileValue = item.getPolicy(20);
                if (minA15CPUProfileValue != -1 && minA15CPUProfileValue > maxA15CPUProfileValue) {
                    item.addPolicy(7, -1);
                }
            }
        }
        if (gpuLimitFreq > 0) {
            int gpuFreq = getMinFreq(gpuLimitFreq, item.getPolicy(13));
            item.addPolicy(13, gpuFreq);
            int minGPUProfileValue = item.getPolicy(14);
            if (minGPUProfileValue != -1 && minGPUProfileValue > gpuFreq) {
                item.addPolicy(14, -1);
            }
        }
        if (frcLimitFreq > 0) {
            item.addPolicy(16, getMinFreq(frcLimitFreq, item.getPolicy(16)));
        }
        if (cpuLimitFreq > 0) {
            this.mSysLoadCheckMaxFreq = cpuLimitFreq;
        } else {
            this.mSysLoadCheckMaxFreq = this.mDefaultMaxFreq;
        }
        return item;
    }

    protected int getSysLoadMaxFreq() {
        if (this.mThermalItem == null && this.mLowBatItem == null) {
            this.mSysLoadCheckMaxFreq = this.mDefaultMaxFreq;
        }
        return this.mSysLoadCheckMaxFreq;
    }

    private void updateThermalItem(String event, int freqValue) {
        if ("cpu".equals(event)) {
            this.mThermalItem.addPolicy(6, freqValue);
        } else if ("cpu1".equals(event)) {
            this.mThermalItem.addPolicy(27, freqValue);
        } else if ("cpu2".equals(event)) {
            this.mThermalItem.addPolicy(29, freqValue);
        } else if ("cpu3".equals(event)) {
            this.mThermalItem.addPolicy(31, freqValue);
        } else if ("gpu".equals(event)) {
            this.mThermalItem.addPolicy(13, freqValue);
        } else if ("frame".equals(event)) {
            if (freqValue < 0 || freqValue > 3) {
                Log.w("CpuGovernorPolicy", "The thermal frc range is unavailable :" + freqValue);
            } else {
                this.mThermalItem.addPolicy(16, matchFrcRangeToFreq(freqValue));
            }
        } else if ("cpu_a15".equals(event)) {
            this.mThermalItem.addPolicy(19, freqValue);
        } else if ("threshold_up".equals(event)) {
            this.mThermalItem.addPolicy(23, freqValue);
        } else if ("threshold_down".equals(event)) {
            this.mThermalItem.addPolicy(24, freqValue);
        } else if ("ipa_power".equals(event)) {
            this.mThermalItem.addPolicy(39, freqValue);
        } else if ("ipa_temp".equals(event)) {
            this.mThermalItem.addPolicy(40, freqValue);
        } else if ("ipa_switch".equals(event)) {
            this.mThermalItem.addPolicy(41, freqValue);
        } else if ("fork_on_big".equals(event)) {
            this.mThermalItem.addPolicy(42, freqValue);
        } else if ("boost".equals(event)) {
            this.mThermalItem.addPolicy(43, freqValue);
        }
        Log.d("CpuGovernorPolicy", "Thermal item update " + event + ":" + freqValue);
    }

    private int matchFrcRangeToFreq(int range) {
        switch (range) {
            case NativeAdapter.PLATFORM_QCOM /*0*/:
                return -1;
            case NativeAdapter.PLATFORM_MTK /*1*/:
            case NativeAdapter.PLATFORM_HI /*2*/:
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                return 2;
            default:
                return -1;
        }
    }

    private void postUpdateCurFreqItem(int actionId) {
        this.mPolicyHandler.removeMessages(100);
        Message msg = this.mPolicyHandler.obtainMessage(100);
        long delayTime = 500;
        if (this.mCurFreqItem != null) {
            msg.obj = this.mCurFreqItem;
            if (actionId == 265) {
                delayTime = getDelayTime(this.mCurFreqItem, this.mCurDefaultActionId, 3);
            } else if (actionId == 251) {
                delayTime = getDelayTime(actionId, 3);
            } else {
                delayTime = getDelayTime(this.mCurFreqItem.mActionId, 3);
            }
        } else if (this.mThermalItem != null) {
            msg.obj = this.mThermalItem;
            delayTime = getDelayTime(this.mThermalItem.mActionId, 3);
        } else if (this.mLowBatItem != null) {
            msg.obj = this.mLowBatItem;
            delayTime = getDelayTime(this.mLowBatItem.mActionId, 3);
        } else {
            msg.obj = null;
            Log.w("CpuGovernorPolicy", "warning, please check cpu policy config");
        }
        this.mPolicyHandler.sendMessageDelayed(msg, delayTime);
    }

    private boolean handleLimitFreqAction(PowerAction action) {
        int actionId = action.getActionId();
        switch (actionId) {
            case 251:
                if (this.mThermalItem == null) {
                    this.mThermalItem = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(251));
                    if (this.mThermalItem == null) {
                        this.mThermalItem = new CpuFreqItem(251, "");
                        Log.w("CpuGovernorPolicy", "There is no Thermal Policy!, instance it");
                    }
                }
                updateThermalItem(action.getPkgName(), (int) action.getExtraLong());
                if (action.getExtraLong() == 0 && this.mThermalItem.getPolicy(6) <= 0 && this.mThermalItem.getPolicy(27) <= 0 && this.mThermalItem.getPolicy(29) <= 0 && this.mThermalItem.getPolicy(31) <= 0 && this.mThermalItem.getPolicy(13) <= 0 && this.mThermalItem.getPolicy(16) <= 0 && this.mThermalItem.getPolicy(19) <= 0 && this.mThermalItem.getPolicy(23) <= 0 && this.mThermalItem.getPolicy(24) <= 0 && this.mThermalItem.getPolicy(39) <= 0 && this.mThermalItem.getPolicy(40) <= 0 && this.mThermalItem.getPolicy(41) <= 0 && ((this.mThermalItem.getPolicy(43) == 0 || this.mThermalItem.getPolicy(43) == -1) && (this.mThermalItem.getPolicy(42) == 1 || this.mThermalItem.getPolicy(42) == -1))) {
                    synchronized (this.mThermalItemLock) {
                        this.mThermalItem = null;
                        Log.d("CpuGovernorPolicy", "Thermal exit");
                    }
                }
            case 265:
                if (this.mCpuDrawItem == null) {
                    this.mCpuDrawItem = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(265));
                    if (this.mCpuDrawItem != null) {
                        Log.i("CpuGovernorPolicy", "cpu draw ");
                        break;
                    }
                    Log.w("CpuGovernorPolicy", "There is no cpu draw start Policy!");
                    return false;
                }
                return false;
            case 266:
                if (this.mCpuDrawItem != null) {
                    synchronized (this.mCpuDrawItemLock) {
                        this.mCpuDrawItem = null;
                    }
                    Log.i("CpuGovernorPolicy", "gpu draw ");
                    break;
                }
                Log.w("CpuGovernorPolicy", "There is no cpu draw end Policy!");
                return false;
            case 319:
                this.mLowBatItem = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(319));
                if (this.mLowBatItem == null) {
                    Log.w("CpuGovernorPolicy", "There is no low bat Policy!");
                    return false;
                }
                break;
            case 320:
                if (this.mLowBatItem != null) {
                    synchronized (this.mLowBatItemLock) {
                        this.mLowBatItem = null;
                    }
                    break;
                }
                Log.w("CpuGovernorPolicy", "There is no low bat Policy!");
                return false;
            case 333:
                this.mLowBatItem = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(333));
                if (this.mLowBatItem == null) {
                    Log.w("CpuGovernorPolicy", "There is no low critial bat Policy!");
                    return false;
                }
                break;
            case 334:
                if (this.mLowBatItem != null && this.mLowBatItem.mActionId == 333) {
                    this.mLowBatItem = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(319));
                    if (this.mLowBatItem == null) {
                        Log.w("CpuGovernorPolicy", "critical lowbat ok, no low bat Policy to set!");
                        break;
                    }
                }
                break;
            default:
                return true;
        }
        if (this.mCurFreqItem != null && 10000 == this.mCurFreqItem.mActionId) {
            return false;
        }
        if (action.getType() == 3 && actionId == 251 && this.mCurFreqItem != null && this.mCurFreqItem.mActionId == 208 && (action instanceof ThermalAction) && checkBenchmarkFront(((ThermalAction) action).getTemperature())) {
            updateBenchmarkCpuFreqInfo();
        }
        postUpdateCurFreqItem(actionId);
        return false;
    }

    private void updateBenchmarkCpuFreqInfo() {
        CpuFreqItem curFreqItem = (CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(10000));
        if (curFreqItem != null) {
            this.mActionItem = curFreqItem;
            this.mCurFreqItem = this.mActionItem;
            this.mParentActionId = this.mActionItem.mActionId;
            this.mCurSubActionFlag = 3;
        }
    }

    private boolean checkBenchmarkFront(int temperature) {
        if (temperature < 42 || ((CpuFreqItem) mCpuPolicyHash.get(Integer.valueOf(10000))) == null || matchCustPkgName(getTopActivityInTask(1)) != 10000) {
            return false;
        }
        Log.d("CpuGovernorPolicy", "Current top running activity matches one extra temperature control policy");
        return true;
    }

    private String getTopActivityInTask(int taskIdx) {
        List<RunningTaskInfo> runningTaskInfos = ((ActivityManager) this.mICoreContext.getContext().getSystemService("activity")).getRunningTasks(taskIdx);
        if (runningTaskInfos == null || runningTaskInfos.size() <= 0) {
            return null;
        }
        return ((RunningTaskInfo) runningTaskInfos.get(0)).topActivity.getClassName();
    }

    private boolean requireMatchCust(int actionId, int subActionFlag) {
        if (subActionFlag == 2 || subActionFlag == 1) {
            return false;
        }
        switch (actionId) {
            case 210:
            case 234:
            case 236:
            case 245:
            case 246:
            case 258:
            case 259:
            case 267:
                return false;
            default:
                return true;
        }
    }

    public static int readFreqInt(String path) {
        int freq = -1;
        String strFreq = readFileContent(path);
        if (strFreq != null) {
            try {
                freq = Integer.parseInt(strFreq);
            } catch (NumberFormatException e) {
                Log.w("CpuGovernorPolicy", "readFreqInt throws NumberFormatException.");
            }
        }
        return freq;
    }

    public static String readFileContent(String path) {
        Exception e;
        IOException e2;
        Throwable th;
        NullPointerException e3;
        Object fis;
        if (path == null) {
            Log.d("CpuGovernorPolicy", "Try to readFileContent  but path is null");
            return null;
        }
        AutoCloseable autoCloseable = null;
        String freqValue = null;
        byte[] bytes = new byte[300];
        Arrays.fill(bytes, (byte) 0);
        try {
            FileInputStream fis2 = new FileInputStream(path);
            try {
                StringBuffer sbFreqs = new StringBuffer(300);
                while (true) {
                    int len = fis2.read(bytes);
                    if (len <= 0) {
                        break;
                    }
                    sbFreqs.append(new String(bytes, 0, len, "UTF-8"));
                }
                freqValue = sbFreqs.toString().trim();
                if (fis2 != null) {
                    try {
                        IoUtils.closeQuietly(fis2);
                    } catch (Exception e4) {
                        Log.w("CpuGovernorPolicy", "close failed " + e4);
                    }
                }
            } catch (IOException e5) {
                e2 = e5;
                autoCloseable = fis2;
                try {
                    Log.w("CpuGovernorPolicy", "readFileContent failed " + e2);
                    if (autoCloseable != null) {
                        try {
                            IoUtils.closeQuietly(autoCloseable);
                        } catch (Exception e42) {
                            Log.w("CpuGovernorPolicy", "close failed " + e42);
                        }
                    }
                    return freqValue;
                } catch (Throwable th2) {
                    th = th2;
                    if (autoCloseable != null) {
                        try {
                            IoUtils.closeQuietly(autoCloseable);
                        } catch (Exception e422) {
                            Log.w("CpuGovernorPolicy", "close failed " + e422);
                        }
                    }
                    throw th;
                }
            } catch (NullPointerException e6) {
                e3 = e6;
                fis = fis2;
                Log.w("CpuGovernorPolicy", "readFileContent failed " + e3);
                if (autoCloseable != null) {
                    try {
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Exception e4222) {
                        Log.w("CpuGovernorPolicy", "close failed " + e4222);
                    }
                }
                return freqValue;
            } catch (Exception e7) {
                e4222 = e7;
                fis = fis2;
                Log.w("CpuGovernorPolicy", "readFileContent failed " + e4222);
                if (autoCloseable != null) {
                    try {
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Exception e42222) {
                        Log.w("CpuGovernorPolicy", "close failed " + e42222);
                    }
                }
                return freqValue;
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                if (autoCloseable != null) {
                    IoUtils.closeQuietly(autoCloseable);
                }
                throw th;
            }
        } catch (IOException e8) {
            e2 = e8;
            Log.w("CpuGovernorPolicy", "readFileContent failed " + e2);
            if (autoCloseable != null) {
                IoUtils.closeQuietly(autoCloseable);
            }
            return freqValue;
        } catch (NullPointerException e9) {
            e3 = e9;
            Log.w("CpuGovernorPolicy", "readFileContent failed " + e3);
            if (autoCloseable != null) {
                IoUtils.closeQuietly(autoCloseable);
            }
            return freqValue;
        } catch (Exception e10) {
            e42222 = e10;
            Log.w("CpuGovernorPolicy", "readFileContent failed " + e42222);
            if (autoCloseable != null) {
                IoUtils.closeQuietly(autoCloseable);
            }
            return freqValue;
        }
        return freqValue;
    }

    protected void printFreqInfo(String tag, int freq) {
    }

    private void dumpInner(PrintWriter pw, HashMap<Integer, Integer> map) {
        if (map != null) {
            for (Entry entry : map.entrySet()) {
                int type = ((Integer) entry.getKey()).intValue();
                int value = ((Integer) entry.getValue()).intValue();
                String key = "";
                switch (type) {
                    case 6:
                        key = "cpu";
                        break;
                    case 13:
                        key = "gpu";
                        break;
                    case 16:
                        key = "frame";
                        break;
                    case 19:
                        key = "cpu_a15";
                        break;
                    case 23:
                        key = "threshold_up";
                        break;
                    case 24:
                        key = "threshold_down";
                        break;
                    case 27:
                        key = "cpu1";
                        break;
                    case 29:
                        key = "cpu2";
                        break;
                    case 31:
                        key = "cpu3";
                        break;
                    case 39:
                        key = "ipa_power";
                        break;
                    case 40:
                        key = "ipa_temp";
                        break;
                    case 41:
                        key = "ipa_switch";
                        break;
                    case 42:
                        key = "fork_on_big";
                        break;
                    case 43:
                        key = "boost";
                        break;
                    default:
                        key = String.valueOf(type);
                        break;
                }
                pw.println("            " + key + " : " + value);
            }
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        pw.println("    Current CPU Scene Info:");
        pw.println("        Cur Action Id : " + this.mCurDefaultActionId + "  Sub Action Flag : " + this.mCurSubActionFlag);
        pw.println("        Cur Parent Action Id : " + this.mParentActionId + "  Pkg : " + this.mParentPowerPkgName);
        pw.println("        Cur CheckLoad Action Id : " + this.mCheckLoadActionId + " Pkg : " + this.mCheckLoadPkg);
        pw.println("        Cur Power Pkg : " + this.mCurPowerPkgName);
        if (this.mThermalItem != null) {
            pw.println("        Thermal Info:");
            dumpInner(pw, this.mThermalItem.getPolicyMap());
        }
        if (this.mLowBatItem != null) {
            pw.println("        LowBat Info:");
            dumpInner(pw, this.mLowBatItem.getPolicyMap());
        }
        if (this.mCpuDrawItem != null) {
            pw.println("        CpuDraw Info:");
            dumpInner(pw, this.mCpuDrawItem.getPolicyMap());
        }
    }
}
