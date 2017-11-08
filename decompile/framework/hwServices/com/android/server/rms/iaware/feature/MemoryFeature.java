package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IAwaredConnection;
import android.rms.iaware.ICMSManager;
import android.rms.iaware.ICMSManager.Stub;
import android.rms.iaware.StatisticsData;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.util.MemInfoReader;
import com.android.server.PPPOEStateMachine;
import com.android.server.am.HwActivityManagerService;
import com.android.server.jankshield.TableJankBd;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.action.Action;
import com.android.server.rms.iaware.memory.action.KillAction;
import com.android.server.rms.iaware.memory.action.QuickKillAction;
import com.android.server.rms.iaware.memory.action.ReclaimAction;
import com.android.server.rms.iaware.memory.data.dispatch.DataDispatch;
import com.android.server.rms.iaware.memory.policy.DMEServer;
import com.android.server.rms.iaware.memory.policy.MemoryExecutorServer;
import com.android.server.rms.iaware.memory.policy.MemoryScenePolicy;
import com.android.server.rms.iaware.memory.policy.MemoryScenePolicyList;
import com.android.server.rms.iaware.memory.utils.BigDataStore;
import com.android.server.rms.iaware.memory.utils.BigMemoryInfo;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageInfoCollector;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;

public class MemoryFeature extends RFeature {
    private static final int INTERVAL_SAVE_DATA_IN_MSEC = 7200000;
    private static final int MAX_CACHED_APPS = SystemProperties.getInt("ro.sys.fw.bg_apps_limit", 32);
    private static final int MAX_CACHED_APP_SIZE = 512;
    private static final String TAG = "AwareMem_MemFeature";
    private static final FeatureHandler mFeatureHandler = new FeatureHandler();
    private BigDataStore mBigDataStore;
    private HwActivityManagerService mHwAMS;
    private int mNumProcessLimit;
    private final AtomicBoolean mRunning;

    private static final class FeatureHandler extends Handler {
        private static final int MSG_CONFIG_PROTECTLRU = 102;
        private static final int MSG_DISABLE_PROTECTLRU = 101;
        private static final int MSG_ENABLE_PROTECTLRU = 100;
        private boolean mFirstAccess;

        private FeatureHandler() {
            this.mFirstAccess = true;
        }

        public void removeProtectLruMsg() {
            removeMessages(100);
            removeMessages(101);
        }

        public void enableProtectLru() {
            removeProtectLruMsg();
            if (this.mFirstAccess) {
                sendEmptyMessageDelayed(102, TableJankBd.recordMAXCOUNT);
            } else {
                sendEmptyMessageDelayed(100, 1000);
            }
            this.mFirstAccess = false;
        }

        public void disableProtectLru() {
            removeProtectLruMsg();
            sendEmptyMessageDelayed(101, 1000);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    MemoryUtils.enableProtectLru();
                    return;
                case 101:
                    MemoryUtils.disableProtectLru();
                    return;
                case 102:
                    MemoryUtils.onProtectLruConfigUpdate();
                    return;
                default:
                    AwareLog.e(MemoryFeature.TAG, "error msg what = " + msg.what);
                    return;
            }
        }
    }

    public MemoryFeature(Context context, FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        this.mRunning = new AtomicBoolean(false);
        this.mHwAMS = null;
        this.mBigDataStore = BigDataStore.getInstance();
        this.mHwAMS = HwActivityManagerService.self();
        this.mNumProcessLimit = MAX_CACHED_APPS;
        loadMemConfig();
    }

    public boolean enable() {
        subscribleEvents();
        DMEServer.getInstance().enable();
        DataDispatch.getInstance().start();
        setBoostKillSwitch(true);
        setProcessLimit(this.mNumProcessLimit);
        MemoryUtils.writeSwappiness(MemoryConstant.getConfigSwappiness());
        MemoryUtils.writeDirectSwappiness(MemoryConstant.getConfigDirectSwappiness());
        mFeatureHandler.enableProtectLru();
        this.mRunning.set(true);
        return true;
    }

    public boolean disable() {
        MemoryUtils.writeExtraFreeKbytes(MemoryConstant.DEFAULT_EXTRA_FREE_KBYTES);
        MemoryUtils.writeSwappiness(60);
        MemoryUtils.writeDirectSwappiness(60);
        setProcessLimit(MAX_CACHED_APPS);
        setBoostKillSwitch(false);
        mFeatureHandler.disableProtectLru();
        DataDispatch.getInstance().stop();
        DMEServer.getInstance().disable();
        unSubscribeEvents();
        this.mRunning.set(false);
        return true;
    }

    public boolean reportData(CollectData data) {
        if (!this.mRunning.get()) {
            return false;
        }
        DataDispatch.getInstance().reportData(data);
        return true;
    }

    public ArrayList<DumpData> getDumpData(int time) {
        if (this.mRunning.get()) {
            return EventTracker.getInstance().getDumpData(time);
        }
        return null;
    }

    public String saveBigData(boolean clear) {
        AwareLog.d(TAG, "enter saveBigData");
        if (!this.mRunning.get()) {
            return null;
        }
        this.mBigDataStore.totalTimeEnd = SystemClock.elapsedRealtime();
        this.mBigDataStore.getMeminfoAllocCount();
        this.mBigDataStore.getLmkOccurCount();
        String rtStatisJsonStr = creatJsonStr();
        if (clear) {
            clearCache();
        }
        return rtStatisJsonStr;
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        if (this.mRunning.get()) {
            return EventTracker.getInstance().getStatisticsData();
        }
        return null;
    }

    public boolean configUpdate() {
        loadMemConfig();
        setProcessLimit(this.mNumProcessLimit);
        MemoryUtils.onProtectLruConfigUpdate();
        return true;
    }

    private void loadMemConfig() {
        loadMemConstantConfig(false);
        loadBigMemAppPolicyConfig();
        MemoryExecutorServer.getInstance().setMemoryScenePolicyList(loadMemPolicyListConfig());
        loadFileCacheConfig();
        loadMemConstantConfig(true);
    }

    private void loadMemConstantConfig(boolean isCustConfig) {
        AwareLog.i(TAG, "loadMemConstantConfig, isCustConfig : " + isCustConfig);
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_CONSTANT_CONFIGNAME, isCustConfig);
        if (configList == null) {
            AwareLog.w(TAG, "loadMemConstantConfig failure cause null configList");
            return;
        }
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        Long totalMemMb = Long.valueOf(minfo.getTotalSize() / MemoryConstant.MB_SIZE);
        for (Item item : configList.getConfigList()) {
            if (item != null && item.getProperties() != null) {
                String ramSize = (String) item.getProperties().get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME);
                if (checkRamSize(ramSize, totalMemMb)) {
                    AwareLog.i(TAG, "loadMemConstantConfig success. ramSize: " + ramSize + " totalMemMb: " + totalMemMb);
                    saveMemConstantItem(item);
                    break;
                }
                AwareLog.d(TAG, "checkRamSize failure cause ramSize: " + ramSize + " totalMemMb: " + totalMemMb);
            } else {
                AwareLog.d(TAG, "loadMemConstantConfig continue cause null item");
            }
        }
    }

    private void loadBigMemAppPolicyConfig() {
        AwareConfig custConfigList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_BIGMEMAPP, true);
        if (custConfigList == null) {
            AwareLog.w(TAG, "loadBigMemAppConfig failure cause null configList");
            return;
        }
        for (Item item : custConfigList.getConfigList()) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadBigMemAppPolicyConfig continue cause null item");
            } else {
                String appName = (String) item.getProperties().get(MemoryConstant.MEM_POLICY_BIGAPPNAME);
                AwareLog.d(TAG, "big memory app is " + appName);
                saveBigMemoryAppConfig(appName, item);
            }
        }
    }

    private void saveBigMemoryAppConfig(String appName, Item item) {
        String appNameTemp = appName;
        for (SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            try {
                if (itemName.equals(MemoryConstant.MEM_SCENE_LAUNCH)) {
                    long launchRequestMem = Long.parseLong(itemValue.trim());
                    AwareLog.d(TAG, "saveBigMemoryAppConfig " + appName + " request memory is " + launchRequestMem);
                    BigMemoryInfo.getInstance().setRequestMemForLaunch(appName, launchRequestMem);
                } else {
                    AwareLog.w(TAG, "no such configuration!");
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "parse app mem error: " + e);
            }
        }
    }

    private boolean checkRamSize(String ramSize, Long totalMemMb) {
        if (ramSize == null) {
            return false;
        }
        try {
            long ramSizeL = Long.parseLong(ramSize.trim());
            if (totalMemMb.longValue() > ramSizeL || totalMemMb.longValue() <= ramSizeL - 1024) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse ramsze error: " + e);
            return false;
        }
    }

    private void loadFileCacheConfig() {
        AwareLog.d(TAG, "loadFileCacheConfig begin");
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_FILECACHE, false);
        if (configList == null) {
            AwareLog.w(TAG, "loadFileCacheConfig failure cause null configList");
            return;
        }
        ArrayMap<Integer, ArraySet<String>> fileCacheMap = new ArrayMap();
        for (Item item : configList.getConfigList()) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadFileCacheConfig continue cause null item");
            } else {
                Map<String, String> configPropertries = item.getProperties();
                String strName = (String) configPropertries.get("name");
                if (TextUtils.isEmpty(strName)) {
                    AwareLog.w(TAG, "loadFileCacheConfig failure cause name: " + strName);
                } else {
                    String strLevel = (String) configPropertries.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL);
                    int fileCacheLevel = checkFileCacheLevel(strLevel);
                    if (fileCacheLevel == -1) {
                        AwareLog.w(TAG, "checkFileCacheLevel failure cause level: " + strLevel);
                    } else {
                        AwareLog.d(TAG, "loadFileCacheConfig success. level: " + strLevel);
                        if (item.getSubItemList() == null) {
                            AwareLog.w(TAG, "loadFileCacheConfig continue cause null item");
                        } else {
                            ArraySet<String> xmlSet = new ArraySet();
                            for (SubItem subitem : item.getSubItemList()) {
                                if (!(subitem == null || TextUtils.isEmpty(subitem.getValue()))) {
                                    xmlSet.add(subitem.getValue());
                                }
                            }
                            AwareLog.i(TAG, "loadFileCacheConfig xmlSet=" + xmlSet.toString());
                            if ("file".equals(strName)) {
                                fileCacheMap.put(Integer.valueOf(fileCacheLevel), xmlSet);
                            } else if (ControlScope.PACKAGE_ELEMENT_KEY.equals(strName)) {
                                ArraySet<String> pkgSet = PackageInfoCollector.getLibFilesFromPackage(this.mContext, xmlSet);
                                if (pkgSet != null && pkgSet.size() > 0) {
                                    fileCacheMap.put(Integer.valueOf(fileCacheLevel + 50), pkgSet);
                                }
                            }
                        }
                    }
                }
            }
        }
        MemoryConstant.setFileCacheMap(fileCacheMap);
        AwareLog.d(TAG, "loadFileCacheConfig end");
    }

    private int checkFileCacheLevel(String fileCacheLevel) {
        if (TextUtils.isEmpty(fileCacheLevel)) {
            return -1;
        }
        try {
            int level = Integer.parseInt(fileCacheLevel.trim());
            if (1 > level || level > 3) {
                level = -1;
            }
            return level;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse filecache index error: " + e);
            return -1;
        }
    }

    private void saveMemConstantItem(Item item) {
        for (SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            try {
                if (!itemName.equals(MemoryConstant.MEM_CONSTANT_LOWCPULOADNAME)) {
                    if (!(itemName.equals(MemoryConstant.MEM_CONSTANT_HIGHCPULOADNAME) || itemName.equals(MemoryConstant.MEM_CONSTANT_EMERGEMCYMEMORYNAME) || itemName.equals(MemoryConstant.MEM_CONSTANT_BIGMEMCRITICALMEMORYNAME) || itemName.equals(MemoryConstant.MEM_CONSTANT_DEFAULTCRITICALMEMORYNAME) || itemName.equals(MemoryConstant.MEM_CONSTANT_NORMALMEMORYNAME))) {
                        if (itemName.equals(MemoryConstant.MEM_CONSTANT_RESERVEDZRAMNAME)) {
                            long reservedZram = Long.parseLong(itemValue.trim());
                            AwareLog.i(TAG, "loadMemConfig reservedZram: " + (1024 * reservedZram));
                            MemoryConstant.setReservedZramSpace(1024 * reservedZram);
                        } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROCESSLIMIT)) {
                            int numProcessLimit = Integer.parseInt(itemValue.trim());
                            AwareLog.i(TAG, "loadMemConfig numProcessLimit: " + numProcessLimit);
                            this.mNumProcessLimit = numProcessLimit;
                        } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_SWAPPINESSNAME)) {
                            int swappiness = Integer.parseInt(itemValue.trim());
                            AwareLog.i(TAG, "loadMemConfig swappiness: " + swappiness);
                            MemoryConstant.setConfigSwappiness(swappiness);
                        } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_DIRECTSWAPPINESSNAME)) {
                            int directswappiness = Integer.parseInt(itemValue.trim());
                            AwareLog.i(TAG, "loadMemConfig directswappiness: " + directswappiness);
                            MemoryConstant.setConfigDirectSwappiness(directswappiness);
                        } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_EXTRAFREEKBYTESNAME)) {
                            int extraFreeKbytes = Integer.parseInt(itemValue.trim());
                            AwareLog.i(TAG, "loadMemConfig extra_free_kbytes: " + extraFreeKbytes);
                            MemoryConstant.setConfigExtraFreeKbytes(extraFreeKbytes);
                        } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROTECTLRULIMIT)) {
                            String protectLruLimit = itemValue.trim();
                            AwareLog.i(TAG, "loadMemConfig protect lru limit: " + protectLruLimit);
                            MemoryConstant.setConfigProtectLruLimit(protectLruLimit);
                        } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROTECTRATIO)) {
                            int ratio = Integer.parseInt(itemValue.trim());
                            AwareLog.i(TAG, "loadMemConfig protect lru ratio: " + ratio);
                            MemoryConstant.setConfigProtectLruRatio(ratio);
                        } else {
                            AwareLog.w(TAG, "loadMemConfig no such configuration. " + itemName);
                        }
                    }
                }
                AwareLog.i(TAG, "loadMemConfig itemName: " + itemName);
                saveMemConstantKillItems(itemName, itemValue);
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "parse memory xml error");
            }
        }
    }

    private void saveMemConstantKillItems(String itemName, String itemValue) {
        try {
            if (itemName.equals(MemoryConstant.MEM_CONSTANT_LOWCPULOADNAME)) {
                long lowCpuLoad = Long.parseLong(itemValue.trim());
                AwareLog.i(TAG, "loadMemConfig lowCpuLoad: " + lowCpuLoad);
                MemoryConstant.setIdleThresHold(lowCpuLoad);
                return;
            }
            if (itemName.equals(MemoryConstant.MEM_CONSTANT_HIGHCPULOADNAME)) {
                long highCpuLoad = Long.parseLong(itemValue.trim());
                AwareLog.i(TAG, "loadMemConfig highCpuLoad: " + highCpuLoad);
                MemoryConstant.setNormalThresHold(highCpuLoad);
                return;
            }
            if (itemName.equals(MemoryConstant.MEM_CONSTANT_EMERGEMCYMEMORYNAME)) {
                long emergemcyMemory = Long.parseLong(itemValue.trim());
                AwareLog.i(TAG, "loadMemConfig emergemcyMemory: " + (1024 * emergemcyMemory));
                MemoryConstant.setEmergencyMemory(1024 * emergemcyMemory);
                return;
            }
            if (itemName.equals(MemoryConstant.MEM_CONSTANT_BIGMEMCRITICALMEMORYNAME)) {
                long bigMemCriticalMemory = Long.parseLong(itemValue.trim());
                AwareLog.i(TAG, "loadMemConfig bigMemAppCriticalMemory: " + (1024 * bigMemCriticalMemory));
                MemoryConstant.setBigMemoryAppCriticalMemory(1024 * bigMemCriticalMemory);
                return;
            }
            if (itemName.equals(MemoryConstant.MEM_CONSTANT_DEFAULTCRITICALMEMORYNAME)) {
                long criticalMemory = Long.parseLong(itemValue.trim());
                AwareLog.i(TAG, "loadMemConfig criticalMemory: " + (1024 * criticalMemory));
                MemoryConstant.setDefaultCriticalMemory(1024 * criticalMemory);
                return;
            }
            if (itemName.equals(MemoryConstant.MEM_CONSTANT_NORMALMEMORYNAME)) {
                long normalMemory = Long.parseLong(itemValue.trim());
                AwareLog.i(TAG, "loadMemConfig normalMemory: " + (1024 * normalMemory));
                MemoryConstant.setIdleMemory(1024 * normalMemory);
                return;
            }
            AwareLog.w(TAG, "loadMemConfig no such configuration. " + itemName);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
        }
    }

    private MemoryScenePolicyList loadMemPolicyListConfig() {
        Map<String, MemoryScenePolicy> memoryScenePolicies = new ArrayMap();
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_CONFIGNAME, false);
        if (configList == null) {
            return new MemoryScenePolicyList(createDefaultMemorPolicyList());
        }
        for (Item item : configList.getConfigList()) {
            if (item.getProperties() != null) {
                String scene = (String) item.getProperties().get(MemoryConstant.MEM_POLICY_SCENE);
                if (scene != null) {
                    AwareLog.d(TAG, "add scene: " + scene);
                    ArrayList<Action> memActions = getActionList(item);
                    if (memActions.size() > 0) {
                        memoryScenePolicies.put(scene, new MemoryScenePolicy(scene, memActions));
                    }
                }
            }
        }
        return new MemoryScenePolicyList(memoryScenePolicies);
    }

    private Map<String, MemoryScenePolicy> createDefaultMemorPolicyList() {
        Map<String, MemoryScenePolicy> memoryScenePolicies = new ArrayMap();
        ArrayList<Action> memActions = new ArrayList();
        memActions.add(new KillAction(this.mContext));
        memoryScenePolicies.put(MemoryConstant.MEM_SCENE_DEFAULT, new MemoryScenePolicy(MemoryConstant.MEM_SCENE_DEFAULT, memActions));
        ArrayList<Action> bigMemActions = new ArrayList();
        bigMemActions.add(new QuickKillAction(this.mContext));
        memoryScenePolicies.put(MemoryConstant.MEM_SCENE_BIGMEM, new MemoryScenePolicy(MemoryConstant.MEM_SCENE_BIGMEM, bigMemActions));
        ArrayList<Action> idleActions = new ArrayList();
        idleActions.add(new ReclaimAction(this.mContext));
        memoryScenePolicies.put(MemoryConstant.MEM_SCENE_IDLE, new MemoryScenePolicy(MemoryConstant.MEM_SCENE_IDLE, idleActions));
        AwareLog.i(TAG, "use default memory policy cause reading xml config failure");
        return memoryScenePolicies;
    }

    private ArrayList<Action> getActionList(Item item) {
        ArrayList<Action> actions = new ArrayList();
        for (SubItem subItem : item.getSubItemList()) {
            if (subItem.getProperties() != null) {
                Object reclaimAction;
                String actionName = (String) subItem.getProperties().get("name");
                if (actionName.equals(MemoryConstant.MEM_POLICY_RECLAIM)) {
                    reclaimAction = new ReclaimAction(this.mContext);
                } else if (actionName.equals(MemoryConstant.MEM_POLICY_KILLACTION)) {
                    reclaimAction = new KillAction(this.mContext);
                } else if (actionName.equals(MemoryConstant.MEM_POLICY_QUICKKILLACTION)) {
                    reclaimAction = new QuickKillAction(this.mContext);
                } else {
                    AwareLog.e(TAG, "no such action!");
                    reclaimAction = null;
                }
                if (reclaimAction != null) {
                    AwareLog.d(TAG, "add action: " + actionName);
                    actions.add(reclaimAction);
                }
            }
        }
        AwareLog.d(TAG, "getActionList return: " + actions);
        return actions;
    }

    private AwareConfig getConfig(String featureName, String configName, boolean isCustConfig) {
        if (featureName == null || featureName.equals(AppHibernateCst.INVALID_PKG) || configName == null || configName.equals(AppHibernateCst.INVALID_PKG)) {
            AwareLog.i(TAG, "featureName or configName is null");
            return null;
        }
        AwareConfig configList = null;
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                configList = isCustConfig ? awareservice.getCustConfig(featureName, configName) : awareservice.getConfig(featureName, configName);
            } else {
                AwareLog.i(TAG, "can not find service awareservice.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "MemoryFeature getConfig RemoteException");
        }
        return configList;
    }

    private void subscribleEvents() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.subscribeData(ResourceType.RES_APP, this.mFeatureType);
            this.mIRDataRegister.subscribeData(ResourceType.RES_DEV_STATUS, this.mFeatureType);
            this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.subscribeData(ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mIRDataRegister.subscribeData(ResourceType.RES_INPUT, this.mFeatureType);
        }
    }

    private void unSubscribeEvents() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.unSubscribeData(ResourceType.RES_APP, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(ResourceType.RES_DEV_STATUS, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(ResourceType.RES_INPUT, this.mFeatureType);
        }
    }

    private void setBoostKillSwitch(boolean isEnable) {
        AwareLog.i(TAG, "setBoostSigKill switch = " + isEnable);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_BOOST_SIGKILL_SWITCH);
        buffer.putInt(isEnable ? 1 : 0);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    private void setProcessLimit(int max) {
        if (max >= 0 && max <= 512 && this.mHwAMS != null) {
            this.mHwAMS.setProcessLimit(max);
        }
    }

    private String creatJsonStr() {
        JSONObject memoryAllocateJson = new JSONObject();
        JSONObject memoryControlAndLMKJson = new JSONObject();
        JSONObject availMemoryTimeJson = new JSONObject();
        JSONObject appStartJson = new JSONObject();
        try {
            memoryAllocateJson.put("memoryAllocCount", String.valueOf(this.mBigDataStore.meminfoAllocCount - this.mBigDataStore.meminfoAllocCountStash));
            memoryAllocateJson.put("slowPathAllocCount", String.valueOf(this.mBigDataStore.slowPathAllocCount - this.mBigDataStore.slowPathAllocCountStash));
            memoryControlAndLMKJson.put("LMKCount", String.valueOf(this.mBigDataStore.lmkOccurCount - this.mBigDataStore.lmkOccurCountStash));
            memoryControlAndLMKJson.put("memoryControlCount", String.valueOf(this.mBigDataStore.lowMemoryManageCount));
            availMemoryTimeJson.put("belowThresholdTime", String.valueOf(this.mBigDataStore.belowThresholdTime));
            this.mBigDataStore.aboveThresholdTime = (this.mBigDataStore.totalTimeEnd - this.mBigDataStore.totalTimeBegin) - this.mBigDataStore.belowThresholdTime;
            availMemoryTimeJson.put("aboveThresholdTime", String.valueOf(this.mBigDataStore.aboveThresholdTime));
            appStartJson.put("coldStartCount", String.valueOf(this.mBigDataStore.coldStartCount));
            appStartJson.put("hotStartCount", this.mBigDataStore.totalStartCount > this.mBigDataStore.coldStartCount ? String.valueOf(this.mBigDataStore.totalStartCount - this.mBigDataStore.coldStartCount) : PPPOEStateMachine.PHASE_DEAD);
        } catch (JSONException e) {
            AwareLog.e(TAG, "JSONException...");
        }
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("[iAwareMemoryRTStatis_Start]\n").append("{\n").append("\"").append("memoryAllocateCount").append("\"").append(":").append(memoryAllocateJson.toString()).append(",").append("\n").append("\"").append("memoryControlAndLMKCount").append("\"").append(":").append(memoryControlAndLMKJson.toString()).append(",").append("\n").append("\"").append("availMemoryTimeCount").append("\"").append(":").append(availMemoryTimeJson.toString()).append(",").append("\n").append("\"").append("appStartCount").append("\"").append(":").append(appStartJson.toString()).append("\n").append("}\n").append("[iAwareMemoryRTStatis_End]");
        return strBuilder.toString();
    }

    private void clearCache() {
        AwareLog.e(TAG, "enter clearCache...");
        this.mBigDataStore.lowMemoryManageCount = 0;
        this.mBigDataStore.belowThresholdTime = 0;
        this.mBigDataStore.coldStartCount = 0;
        this.mBigDataStore.totalStartCount = 0;
        this.mBigDataStore.meminfoAllocCountStash = this.mBigDataStore.meminfoAllocCount;
        this.mBigDataStore.slowPathAllocCountStash = this.mBigDataStore.slowPathAllocCount;
        this.mBigDataStore.lmkOccurCountStash = this.mBigDataStore.lmkOccurCount;
        this.mBigDataStore.aboveThresholdTime = 0;
        this.mBigDataStore.totalTimeBegin = SystemClock.elapsedRealtime();
        this.mBigDataStore.totalTimeEnd = SystemClock.elapsedRealtime();
    }
}
