package com.android.server.mtm.iaware.appmng;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo.XmlConfig;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAppLruBase;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList.PackageConfigItem;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList.ProcessConfigItem;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AwareAppMngSort {
    public static final String ACTIVITY_RECENT_TASK = "com.android.systemui/.recents.RecentsActivity";
    public static final int ACTIVITY_TASK_IMPORT_CNT = 1;
    public static final String ADJTYPE_SERVICE = "service";
    public static final int APPMNG_MEM_ALLOWSTOP_GROUP = 2;
    public static final int APPMNG_MEM_ALL_GROUP = 3;
    public static final int APPMNG_MEM_FORBIDSTOP_GROUP = 0;
    public static final int APPMNG_MEM_SHORTAGESTOP_GROUP = 1;
    public static final int APPSORT_FORMEM = 0;
    private static final int BETA_LOG_PRINT_INTERVEL = 60000;
    private static final int CLASSRATE_KEY_OFFSET = 8;
    private static boolean DEBUG = false;
    public static final String EXEC_SERVICES = "exec-service";
    public static final String FG_SERVICE = "fg-service";
    public static final long FOREVER_DECAYTIME = -1;
    public static final int HABITMAX_IMPORT = 10000;
    private static final int INVALID_VALUE = -1;
    public static final int MEM_LEVEL0 = 0;
    public static final int MEM_LEVEL1 = 1;
    private static final int MSG_PRINT_BETA_LOG = 1;
    public static final long PREVIOUS_APP_DIRCACTIVITY_DECAYTIME = 600000;
    private static final int SEC_PER_MIN = 60;
    private static final String SUBTYPE_ASSOCIATION = "assoc";
    private static final String TAG = "AwareAppMngSort";
    private static boolean mEnabled = false;
    private static AwareAppMngSort sInstance = null;
    private boolean mAssocEnable = true;
    private final Context mContext;
    private Handler mHandler = null;
    private HwActivityManagerService mHwAMS = null;
    private long mLastBetaLogOutTime = 0;

    enum AllowStopSubClassRate {
        NONE("none"),
        PREVIOUS("previous"),
        TOPN("user_topN"),
        KEY_SYS_SERVICE("keySysService"),
        HEALTH("health"),
        FG_SERVICES("fg_services"),
        OTHER("other"),
        NONCURUSER("nonCurUser"),
        UNKNOWN("unknown");
        
        String mDescription;

        private AllowStopSubClassRate(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    static class AppBlockKeyBase {
        public int mPid = 0;
        public int mUid = 0;

        public AppBlockKeyBase(int pid, int uid) {
            this.mPid = pid;
            this.mUid = uid;
            if (AwareAppMngSort.DEBUG) {
                AwareLog.d(AwareAppMngSort.TAG, "AppBlockKeyBase constructor pid:" + this.mPid + ",uid:" + this.mUid);
            }
        }
    }

    private static final class BetaLog {
        private static final char FLAG_ITEM_INNER_SPLIT = ',';
        private static final char FLAG_ITEM_SPLIT = ';';
        private static final char FLAG_NEW_LINE = '\n';
        private static final int ITEMS_ONE_LINE = 10;
        private static final int PROCESS_INFO_CNT = 5;
        private List<Integer> mData = new ArrayList();

        BetaLog(AwareAppMngSortPolicy policy) {
            if (policy.getForbidStopProcBlockList() != null) {
                inflat(policy.getForbidStopProcBlockList());
                inflat(policy.getShortageStopProcBlockList());
                inflat(policy.getAllowStopProcBlockList());
            }
        }

        private void inflat(List<AwareProcessBlockInfo> list) {
            if (list != null) {
                for (AwareProcessBlockInfo pinfo : list) {
                    if (!(pinfo == null || pinfo.mProcessList == null)) {
                        for (AwareProcessInfo info : pinfo.mProcessList) {
                            this.mData.add(Integer.valueOf(info.mProcInfo.mPid));
                            this.mData.add(Integer.valueOf(info.mProcInfo.mUid));
                            this.mData.add(Integer.valueOf(info.mClassRate));
                            this.mData.add(Integer.valueOf(info.mSubClassRate));
                            this.mData.add(Integer.valueOf(info.mProcInfo.mCurAdj));
                        }
                    }
                }
            }
        }

        public void print() {
            int size = this.mData.size();
            if (size != 0 && size % 5 == 0) {
                StringBuilder outStr = new StringBuilder();
                int cnt = 0;
                for (Integer cur : this.mData) {
                    outStr.append(cur);
                    cnt++;
                    if (cnt % 5 != 0) {
                        outStr.append(FLAG_ITEM_INNER_SPLIT);
                    } else if (cnt % 50 == 0) {
                        outStr.append(FLAG_NEW_LINE);
                    } else {
                        outStr.append(FLAG_ITEM_SPLIT);
                    }
                }
                AwareLog.i(AwareAppMngSort.TAG, outStr.toString());
            }
        }
    }

    private static final class BetaLogHandler extends Handler {
        public BetaLogHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    BetaLog betaLog = msg.obj;
                    if (betaLog != null) {
                        betaLog.print();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    static class CachedWhiteList {
        private final ArraySet<String> mAllHabitCacheList = new ArraySet();
        final Set<String> mAllProtectApp = new ArraySet();
        final Set<String> mAllUnProtectApp = new ArraySet();
        private Map<String, PackageConfigItem> mAwareProtectCacheMap = new ArrayMap();
        final ArraySet<String> mBadAppList = new ArraySet();
        final ArraySet<String> mBgNonDecayPkg = new ArraySet();
        private final ArraySet<String> mKeyHabitCacheList = new ArraySet();
        private boolean mLowEnd = false;
        final ArraySet<String> mRestartAppList = new ArraySet();

        public void updateCachedList() {
            AwareDefaultConfigList whiteListInstance = AwareDefaultConfigList.getInstance();
            if (whiteListInstance != null) {
                this.mLowEnd = whiteListInstance.isLowEnd();
                this.mKeyHabitCacheList.addAll(whiteListInstance.getKeyHabitAppList());
                this.mAllHabitCacheList.addAll(whiteListInstance.getAllHabitAppList());
                this.mRestartAppList.addAll(whiteListInstance.getRestartAppList());
                this.mBadAppList.addAll(whiteListInstance.getBadAppList());
                AwareUserHabit habit = AwareUserHabit.getInstance();
                if (habit != null) {
                    if (AppMngConfig.getAbroadFlag()) {
                        Set<String> unprotectApp = habit.getAllUnProtectApps();
                        if (unprotectApp != null) {
                            this.mAllUnProtectApp.addAll(unprotectApp);
                        }
                    }
                    if (AppMngConfig.getPgProtectFlag()) {
                        Set<String> protectApp = habit.getAllProtectApps();
                        if (protectApp != null) {
                            this.mAllProtectApp.addAll(protectApp);
                        }
                    }
                    Set<String> bgNonDcyApp = habit.getBackgroundApps(AppMngConfig.getBgDecay() * 60);
                    if (bgNonDcyApp != null) {
                        this.mBgNonDecayPkg.addAll(bgNonDcyApp);
                    }
                }
                this.mAwareProtectCacheMap = whiteListInstance.getAwareProtectMap();
            }
        }

        private boolean isLowEnd() {
            return this.mLowEnd;
        }

        public boolean isInKeyHabitList(ArrayList<String> packageNames) {
            if (packageNames == null || packageNames.isEmpty()) {
                return false;
            }
            for (String pkgName : packageNames) {
                if (this.mKeyHabitCacheList.contains(pkgName)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isInAllHabitList(ArrayList<String> packageNames) {
            if (packageNames == null || packageNames.isEmpty()) {
                return false;
            }
            for (String pkgName : packageNames) {
                if (this.mAllHabitCacheList.contains(pkgName)) {
                    return true;
                }
            }
            return false;
        }

        private ProcessConfigItem getAwareWhiteListItem(ArrayList<String> packageNames, String processName) {
            if (packageNames == null || packageNames.isEmpty() || this.mAwareProtectCacheMap == null) {
                return null;
            }
            for (String pkgName : packageNames) {
                if (this.mAwareProtectCacheMap.containsKey(pkgName)) {
                    PackageConfigItem pkgItem = (PackageConfigItem) this.mAwareProtectCacheMap.get(pkgName);
                    if (pkgItem == null) {
                        return null;
                    }
                    if (pkgItem.isEmpty()) {
                        return pkgItem.copy();
                    }
                    ProcessConfigItem procItem = pkgItem.getItem(processName);
                    if (procItem == null) {
                        return null;
                    }
                    return procItem.copy();
                }
            }
            return null;
        }

        private int getGroupId(ProcessConfigItem item) {
            if (item == null) {
                return 2;
            }
            int group = 2;
            switch (item.mGroupId) {
                case 1:
                    group = 0;
                    break;
                case 2:
                    group = 1;
                    break;
            }
            return group;
        }

        private void updateProcessInfoByConfig(AwareProcessInfo processInfo) {
            if (processInfo != null && processInfo.mProcInfo != null) {
                ProcessConfigItem item = getAwareWhiteListItem(processInfo.mProcInfo.mPackageName, processInfo.mProcInfo.mProcessName);
                if (item != null) {
                    processInfo.mXmlConfig = new XmlConfig(getGroupId(item), item.mFrequentlyUsed, item.mResCleanAllow, item.mRestartFlag);
                }
            }
        }
    }

    enum ClassRate {
        NONE("none"),
        PERSIST("persist"),
        FOREGROUND("foreground"),
        KEYBACKGROUND("keybackground"),
        HOME("home"),
        KEYSERVICES("keyservices"),
        NORMAL("normal"),
        UNKNOWN("unknown");
        
        String mDescription;

        private ClassRate(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    enum ForbidSubClassRate {
        NONE("none"),
        PREVIOUS("previous"),
        AWARE_PROTECTED("awareProtected");
        
        String mDescription;

        private ForbidSubClassRate(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    private static class MemSortGroup {
        public List<AwareProcessBlockInfo> mProcAllowStopList = null;
        public List<AwareProcessBlockInfo> mProcForbidStopList = null;
        public List<AwareProcessBlockInfo> mProcShortageStopList = null;

        public MemSortGroup(List<AwareProcessBlockInfo> procForbidStopList, List<AwareProcessBlockInfo> procShortageStopList, List<AwareProcessBlockInfo> procAllowStopList) {
            this.mProcForbidStopList = procForbidStopList;
            this.mProcShortageStopList = procShortageStopList;
            this.mProcAllowStopList = procAllowStopList;
        }
    }

    private static class ProcessHabitCompare implements Comparator<AwareProcessInfo>, Serializable {
        private static final long serialVersionUID = 1;

        private ProcessHabitCompare() {
        }

        public int compare(AwareProcessInfo arg0, AwareProcessInfo arg1) {
            if (arg0 == null || arg1 == null) {
                return 0;
            }
            return arg0.mImportance - arg1.mImportance;
        }
    }

    static class ShortageProcessInfo {
        public Map<Integer, AwareProcessInfo> mAllProcNeedSort = null;
        final ArrayMap<Integer, AwareProcessInfo> mAudioIn = new ArrayMap();
        final ArrayMap<Integer, AwareProcessInfo> mAudioOut = new ArrayMap();
        private final ArraySet<Integer> mForeGroundServiceUid = new ArraySet();
        private final ArrayMap<Integer, ArrayList<String>> mForeGroundUid = new ArrayMap();
        Set<Integer> mHabitTopN;
        private int mHomeProcessPid;
        private final Set<Integer> mHomeStrong = new ArraySet();
        private Set<Integer> mKeyPercepServicePid;
        public boolean mKillMore = false;
        final ArrayMap<Integer, AwareProcessInfo> mNonCurUserProc = new ArrayMap();
        public AwareAppLruBase mPrevAmsBase = null;
        public AwareAppLruBase mPrevAwareBase = null;
        public AwareAppLruBase mRecentTaskAppBase = null;
        private boolean mRecentTaskShow = false;
        final Set<String> mVisibleWinPkg = new ArraySet();
        final Set<String> mWidgetPkg = new ArraySet();

        public boolean isRecentTaskShow() {
            return this.mRecentTaskShow;
        }

        public boolean isFgServicesUid(int uid) {
            return this.mForeGroundServiceUid.contains(Integer.valueOf(uid));
        }

        public void recordFgServicesUid(int uid) {
            this.mForeGroundServiceUid.add(Integer.valueOf(uid));
        }

        public boolean isForegroundUid(ProcessInfo procInfo) {
            if (procInfo == null || !this.mForeGroundUid.containsKey(Integer.valueOf(procInfo.mUid))) {
                return false;
            }
            if (!AwareAppAssociate.isDealAsPkgUid(procInfo.mUid)) {
                return true;
            }
            return AwareAppMngSort.isPkgIncludeForTgt(procInfo.mPackageName, (ArrayList) this.mForeGroundUid.get(Integer.valueOf(procInfo.mUid)));
        }

        public boolean isKeyPercepService(int pid) {
            if (this.mKeyPercepServicePid == null) {
                return false;
            }
            return this.mKeyPercepServicePid.contains(Integer.valueOf(pid));
        }

        public void recordForegroundUid(int uid, ArrayList<String> packageList) {
            if (AwareAppAssociate.isDealAsPkgUid(uid)) {
                this.mForeGroundUid.put(Integer.valueOf(uid), packageList);
            } else {
                this.mForeGroundUid.put(Integer.valueOf(uid), null);
            }
        }

        private boolean isAudioSubClass(ProcessInfo procInfo, Map<Integer, AwareProcessInfo> audioInfo) {
            if (procInfo == null) {
                return false;
            }
            for (Entry<Integer, AwareProcessInfo> m : audioInfo.entrySet()) {
                AwareProcessInfo info = (AwareProcessInfo) m.getValue();
                if (procInfo.mPid == info.mProcInfo.mPid) {
                    return true;
                }
                if (procInfo.mUid == info.mProcInfo.mUid && (!AwareAppAssociate.isDealAsPkgUid(procInfo.mUid) || AwareAppMngSort.isPkgIncludeForTgt(procInfo.mPackageName, info.mProcInfo.mPackageName))) {
                    return true;
                }
            }
            return false;
        }

        public void updateBaseInfo(Map<Integer, AwareProcessInfo> allProcNeedSort, int homePid, boolean recentTaskShow, Set<Integer> keyPercepServicePid) {
            this.mAllProcNeedSort = allProcNeedSort;
            this.mHabitTopN = getHabitAppTopN(allProcNeedSort, AppMngConfig.getTopN());
            updateVisibleWin();
            updateWidget();
            this.mHomeProcessPid = homePid;
            this.mRecentTaskShow = recentTaskShow;
            this.mKeyPercepServicePid = keyPercepServicePid;
            loadHomeAssoc(this.mHomeProcessPid, allProcNeedSort);
            this.mPrevAmsBase = AwareAppAssociate.getInstance().getPreviousByAmsInfo();
            this.mPrevAwareBase = AwareAppAssociate.getInstance().getPreviousAppInfo();
            this.mRecentTaskAppBase = AwareAppAssociate.getInstance().getRecentTaskPrevInfo();
        }

        public ShortageProcessInfo(int memLevel) {
            boolean z = true;
            if (!(AppMngConfig.getKillMoreFlag() && memLevel == 1)) {
                z = false;
            }
            this.mKillMore = z;
        }

        private boolean isHomeAssocStrong(AwareProcessInfo awareProcInfo) {
            if (awareProcInfo == null || awareProcInfo.mProcInfo == null) {
                return false;
            }
            if (awareProcInfo.mProcInfo.mCurAdj == 600 && (awareProcInfo.mProcInfo.mType == 2 || awareProcInfo.mProcInfo.mType == 3)) {
                return true;
            }
            return this.mHomeStrong.contains(Integer.valueOf(awareProcInfo.mPid));
        }

        private void loadHomeAssoc(int homePid, Map<Integer, AwareProcessInfo> allProc) {
            Set<Integer> homeStrong = new ArraySet();
            AwareAppAssociate.getInstance().getAssocListForPid(homePid, homeStrong);
            for (Integer pid : homeStrong) {
                AwareProcessInfo awareProcInfo = (AwareProcessInfo) allProc.get(pid);
                if (!(awareProcInfo == null || awareProcInfo.mProcInfo == null || awareProcInfo.mHasShownUi || awareProcInfo.mProcInfo.mType != 2)) {
                    this.mHomeStrong.add(pid);
                }
            }
        }

        private boolean isHabitTopN(int pid) {
            return this.mHabitTopN != null ? this.mHabitTopN.contains(Integer.valueOf(pid)) : false;
        }

        public boolean isHomeProcess(int pid) {
            return this.mHomeProcessPid == pid;
        }

        public int getKeyBackgroupTypeInternal(ProcessInfo procInfo) {
            if (procInfo == null) {
                return -1;
            }
            return AwareAppKeyBackgroup.getInstance().getKeyBackgroupTypeInternal(procInfo.mPid, procInfo.mUid, procInfo.mPackageName);
        }

        public int getKeyBackgroupTypeInternalByPid(ProcessInfo procInfo) {
            if (procInfo == null) {
                return -1;
            }
            return AwareAppKeyBackgroup.getInstance().getKeyBackgroupTypeInternal(procInfo.mPid, procInfo.mUid, null);
        }

        private Set<Integer> getHabitAppTopN(Map<Integer, AwareProcessInfo> proc, int topN) {
            if (proc == null) {
                return null;
            }
            List<AwareProcessInfo> procList = new ArrayList();
            for (Entry<Integer, AwareProcessInfo> pm : proc.entrySet()) {
                AwareProcessInfo info = (AwareProcessInfo) pm.getValue();
                if (info.mImportance != 10000) {
                    procList.add(info);
                }
            }
            Collections.sort(procList, new ProcessHabitCompare());
            ArraySet<Integer> uidTopN = new ArraySet();
            int foundUidNum = 0;
            ArraySet<Integer> procTopN = new ArraySet();
            for (AwareProcessInfo info2 : procList) {
                if (info2 != null) {
                    boolean need = uidTopN.contains(Integer.valueOf(info2.mProcInfo.mUid));
                    if (foundUidNum < topN && !need) {
                        uidTopN.add(Integer.valueOf(info2.mProcInfo.mUid));
                        foundUidNum++;
                        need = true;
                    }
                    if (need) {
                        procTopN.add(Integer.valueOf(info2.mProcInfo.mPid));
                    }
                }
            }
            return procTopN;
        }

        private void updateVisibleWin() {
            if (this.mAllProcNeedSort != null) {
                Set<Integer> visibleWindows = new ArraySet();
                AwareAppAssociate.getInstance().getVisibleWindows(visibleWindows);
                for (Integer pid : visibleWindows) {
                    AwareProcessInfo procInfo = (AwareProcessInfo) this.mAllProcNeedSort.get(pid);
                    if (!(procInfo == null || procInfo.mProcInfo.mPackageName == null)) {
                        this.mVisibleWinPkg.addAll(procInfo.mProcInfo.mPackageName);
                    }
                }
            }
        }

        private void updateWidget() {
            if (this.mAllProcNeedSort != null) {
                Set<String> widgets = AwareAppAssociate.getInstance().getWidgetsPkg();
                if (widgets != null) {
                    this.mWidgetPkg.addAll(widgets);
                }
            }
        }
    }

    enum ShortageSubClassRate {
        NONE("none"),
        PREV_ONECLEAN("prevOneclean"),
        MUSIC_PLAY("musicPlay"),
        SOUND_RECORD("soundRecord"),
        FGSERVICES_TOPN("fgservice_topN"),
        SERVICE_ADJ_TOPN("service_adj_topN"),
        HW_SYSTEM("hw_system"),
        KEY_IM("key_im"),
        GUIDE("guide"),
        DOWN_UP_LOAD("downupLoad"),
        HEALTH("health"),
        PREVIOUS("previous"),
        AWARE_PROTECTED("awareProtected"),
        KEY_SYS_SERVICE("keySysService"),
        ASSOC_WITH_FG("assocWithFg"),
        VISIBLEWIN("visibleWin"),
        FREQN("user_freqN"),
        TOPN("user_topN"),
        WIDGET("widget"),
        UNKNOWN("unknown");
        
        String mDescription;
        int subClass;

        private ShortageSubClassRate(String description) {
            this.mDescription = description;
            this.subClass = -1;
        }

        public String description() {
            return this.mDescription;
        }

        public int getSubClassRate() {
            return this.subClass;
        }
    }

    private AwareAppMngSort(Context context) {
        this.mContext = context;
        this.mHandler = new BetaLogHandler(BackgroundThread.get().getLooper());
        init();
    }

    public static synchronized AwareAppMngSort getInstance(Context context) {
        AwareAppMngSort awareAppMngSort;
        synchronized (AwareAppMngSort.class) {
            if (sInstance == null) {
                sInstance = new AwareAppMngSort(context);
            }
            awareAppMngSort = sInstance;
        }
        return awareAppMngSort;
    }

    public static synchronized AwareAppMngSort getInstance() {
        AwareAppMngSort awareAppMngSort;
        synchronized (AwareAppMngSort.class) {
            awareAppMngSort = sInstance;
        }
        return awareAppMngSort;
    }

    private void init() {
        this.mHwAMS = HwActivityManagerService.self();
    }

    public static void enable() {
        mEnabled = true;
    }

    public static void disable() {
        mEnabled = false;
    }

    private boolean containsVisibleWindow(Set<String> visibleWindowList, List<String> pkgList) {
        if (visibleWindowList == null || pkgList == null || visibleWindowList.isEmpty()) {
            return false;
        }
        for (String pkg : pkgList) {
            if (visibleWindowList.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    private void loadAppAssoc(List<AwareProcessInfo> procs, Map<Integer, AwareProcessInfo> pidsClass, Map<Integer, AwareProcessInfo> strongAssocProc) {
        if (procs != null && !procs.isEmpty() && pidsClass != null && strongAssocProc != null) {
            Set<Integer> strong = new ArraySet();
            for (AwareProcessInfo procInfoBase : procs) {
                int pid = procInfoBase.mPid;
                strong.clear();
                loadAssocListForPid(pid, pidsClass, strong, strongAssocProc);
            }
        }
    }

    private boolean isAssocRelation(AwareProcessInfo client, AwareProcessInfo app) {
        if (app == null || client == null || client.mProcInfo == null) {
            return false;
        }
        if (!app.mHasShownUi || app.mPid == getCurHomeProcessPid() || client.mProcInfo.mCurAdj <= 200) {
            return true;
        }
        return false;
    }

    private void loadAssocListForPid(int pid, Map<Integer, AwareProcessInfo> pidsClass, Set<Integer> strong, Map<Integer, AwareProcessInfo> strongAssocProc) {
        if (pidsClass != null && strongAssocProc != null && strong != null) {
            AwareAppAssociate.getInstance().getAssocListForPid(pid, strong);
            for (Integer sPid : strong) {
                AwareProcessInfo procInfo = (AwareProcessInfo) pidsClass.get(sPid);
                if (procInfo != null && isAssocRelation((AwareProcessInfo) pidsClass.get(Integer.valueOf(pid)), procInfo)) {
                    strongAssocProc.put(sPid, procInfo);
                }
            }
        }
    }

    private ArrayMap<Integer, AwareProcessInfo> getNeedSortedProcesses(Map<Integer, AwareProcessInfo> foreGrdProc, Set<Integer> keyPercepServicePid, CachedWhiteList cachedWhitelist, ShortageProcessInfo shortageProc) {
        ArrayList<ProcessInfo> procs = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procs.isEmpty()) {
            return null;
        }
        Map<Integer, AwareProcessBaseInfo> baseInfos = this.mHwAMS != null ? this.mHwAMS.getAllProcessBaseInfo() : null;
        if (baseInfos == null || baseInfos.isEmpty()) {
            return null;
        }
        int curUserUid = AwareAppAssociate.getInstance().getCurUserId();
        Set<Integer> fgServiceUid = new ArraySet();
        ArraySet<Integer> importUid = new ArraySet();
        ArrayMap<Integer, Integer> percepServicePid = new ArrayMap();
        ArrayMap<Integer, AwareProcessInfo> allProcNeedSort = new ArrayMap();
        for (ProcessInfo procInfo : procs) {
            AwareProcessInfo awareProcInfo;
            if (procInfo != null) {
                AwareProcessBaseInfo updateInfo = (AwareProcessBaseInfo) baseInfos.get(Integer.valueOf(procInfo.mPid));
                if (updateInfo != null) {
                    procInfo.mCurAdj = updateInfo.mCurAdj;
                    procInfo.mForegroundActivities = updateInfo.mForegroundActivities;
                    procInfo.mAdjType = updateInfo.mAdjType;
                    awareProcInfo = new AwareProcessInfo(procInfo.mPid, 0, 0, ClassRate.NORMAL.ordinal(), procInfo);
                    awareProcInfo.mHasShownUi = updateInfo.mHasShownUi;
                    cachedWhitelist.updateProcessInfoByConfig(awareProcInfo);
                    if (curUserUid != 0 || isCurUserProc(procInfo, curUserUid)) {
                        allProcNeedSort.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                        if (procInfo.mForegroundActivities) {
                            foreGrdProc.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                            shortageProc.recordForegroundUid(procInfo.mUid, procInfo.mPackageName);
                        }
                        if (procInfo.mCurAdj >= 200) {
                            if (shortageProc.getKeyBackgroupTypeInternalByPid(procInfo) == 2) {
                                shortageProc.mAudioOut.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                            } else {
                                if (shortageProc.getKeyBackgroupTypeInternalByPid(procInfo) == 1) {
                                    shortageProc.mAudioIn.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                                }
                            }
                        }
                        if (procInfo.mCurAdj < 200) {
                            importUid.add(Integer.valueOf(procInfo.mUid));
                        } else if (procInfo.mCurAdj == 200 && FG_SERVICE.equals(procInfo.mAdjType)) {
                            fgServiceUid.add(Integer.valueOf(procInfo.mUid));
                        } else if (procInfo.mCurAdj == 200 && ADJTYPE_SERVICE.equals(procInfo.mAdjType)) {
                            percepServicePid.put(Integer.valueOf(procInfo.mPid), Integer.valueOf(procInfo.mUid));
                        } else if (procInfo.mCurAdj == 200) {
                            importUid.add(Integer.valueOf(procInfo.mUid));
                        }
                    } else {
                        shortageProc.mNonCurUserProc.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                    }
                }
            }
        }
        int myPid = Process.myPid();
        for (Entry<Integer, Integer> m : percepServicePid.entrySet()) {
            int pid = ((Integer) m.getKey()).intValue();
            int uid = ((Integer) m.getValue()).intValue();
            if (!importUid.contains(Integer.valueOf(uid))) {
                if (fgServiceUid.contains(Integer.valueOf(uid))) {
                    Set<Integer> strong = new ArraySet();
                    AwareAppAssociate.getInstance().getAssocClientListForPid(pid, strong);
                    for (Integer clientPid : strong) {
                        if (clientPid.intValue() != myPid) {
                            awareProcInfo = (AwareProcessInfo) allProcNeedSort.get(clientPid);
                            if (awareProcInfo != null && awareProcInfo.mProcInfo != null && awareProcInfo.mProcInfo.mCurAdj <= 200) {
                                keyPercepServicePid.add(Integer.valueOf(pid));
                                break;
                            }
                        } else {
                            keyPercepServicePid.add(Integer.valueOf(pid));
                            break;
                        }
                    }
                }
                keyPercepServicePid.add(Integer.valueOf(pid));
            } else {
                keyPercepServicePid.add(Integer.valueOf(pid));
            }
        }
        return allProcNeedSort;
    }

    private boolean isCurUserProc(ProcessInfo procInfo, int curUserUid) {
        boolean z = false;
        if (procInfo == null) {
            return false;
        }
        if (UserHandle.getUserId(procInfo.mUid) == curUserUid) {
            z = true;
        }
        return z;
    }

    private void groupNonCurUserProc(ArrayMap<Integer, AwareProcessBlockInfo> classNormal, ArrayMap<Integer, AwareProcessInfo> nonCurUserProc) {
        if (classNormal != null && nonCurUserProc != null) {
            for (Entry<Integer, AwareProcessInfo> m : nonCurUserProc.entrySet()) {
                AwareProcessInfo awareProcInfo = (AwareProcessInfo) m.getValue();
                if (awareProcInfo != null) {
                    awareProcInfo.mClassRate = ClassRate.NORMAL.ordinal();
                    awareProcInfo.mSubClassRate = AllowStopSubClassRate.NONCURUSER.ordinal();
                    addProcessInfoToBlock(classNormal, awareProcInfo, awareProcInfo.mPid);
                }
            }
        }
    }

    private boolean isSystemProcess(ProcessInfo procInfo) {
        boolean z = false;
        if (procInfo == null) {
            return false;
        }
        if (procInfo.mType == 2) {
            z = true;
        }
        return z;
    }

    private boolean groupIntoForbidstop(ShortageProcessInfo shortageProc, AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        int curAdj = awareProcInfo.mProcInfo.mCurAdj;
        int classType = ClassRate.UNKNOWN.ordinal();
        int subClassType = ForbidSubClassRate.NONE.ordinal();
        boolean isGroup = true;
        if (curAdj < 0) {
            classType = ClassRate.PERSIST.ordinal();
        } else if (curAdj < 200) {
            classType = ClassRate.FOREGROUND.ordinal();
        } else if (awareProcInfo.mXmlConfig == null || !isCfgDefaultGroup(awareProcInfo, 0)) {
            isGroup = false;
        } else {
            classType = ClassRate.FOREGROUND.ordinal();
            subClassType = ForbidSubClassRate.AWARE_PROTECTED.ordinal();
        }
        if (isGroup) {
            awareProcInfo.mClassRate = classType;
            awareProcInfo.mSubClassRate = subClassType;
        }
        return isGroup;
    }

    private static boolean isPkgIncludeForTgt(ArrayList<String> tgtPkg, ArrayList<String> dstPkg) {
        if (tgtPkg == null || tgtPkg.isEmpty() || dstPkg == null) {
            return false;
        }
        for (String pkg : dstPkg) {
            if (pkg != null && tgtPkg.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isLastRecentlyUsedBase(ProcessInfo procInfo, Map<Integer, AwareProcessInfo> allProcNeedSort, AwareAppLruBase appLruBase, long decayTime) {
        if (procInfo == null || allProcNeedSort == null || appLruBase == null || procInfo.mUid != appLruBase.mUid) {
            return false;
        }
        if (decayTime != -1 && SystemClock.elapsedRealtime() - appLruBase.mTime > decayTime) {
            return false;
        }
        if (!AwareAppAssociate.isDealAsPkgUid(procInfo.mUid)) {
            return true;
        }
        AwareProcessInfo prevProcInfo = (AwareProcessInfo) allProcNeedSort.get(Integer.valueOf(appLruBase.mPid));
        if (prevProcInfo == null) {
            return false;
        }
        return isPkgIncludeForTgt(procInfo.mPackageName, prevProcInfo.mProcInfo.mPackageName);
    }

    private boolean isPerceptable(ProcessInfo procInfo) {
        if (procInfo == null || procInfo.mCurAdj != 200 || FG_SERVICE.equals(procInfo.mAdjType) || ADJTYPE_SERVICE.equals(procInfo.mAdjType)) {
            return false;
        }
        return true;
    }

    private boolean isFgServices(ProcessInfo procInfo, ShortageProcessInfo shortageProc) {
        if (procInfo == null || procInfo.mCurAdj != 200) {
            return false;
        }
        if (FG_SERVICE.equals(procInfo.mAdjType) && (isSystemProcess(procInfo) || shortageProc.isForegroundUid(procInfo))) {
            return true;
        }
        return ADJTYPE_SERVICE.equals(procInfo.mAdjType) && (isSystemProcess(procInfo) || shortageProc.isForegroundUid(procInfo));
    }

    public boolean isFgServicesImportantByAdjtype(String adjType) {
        if (FG_SERVICE.equals(adjType) || ADJTYPE_SERVICE.equals(adjType)) {
            return true;
        }
        return false;
    }

    private boolean isFgServicesImportant(ProcessInfo procInfo) {
        if (procInfo != null && procInfo.mCurAdj == 200) {
            return isFgServicesImportantByAdjtype(procInfo.mAdjType);
        }
        return false;
    }

    private boolean groupIntoShortageStop(AwareProcessInfo awareProcInfo, ShortageProcessInfo shortageProc, CachedWhiteList cachedWhitelist) {
        if (awareProcInfo == null) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        int curAdj = procInfo.mCurAdj;
        int classType = ClassRate.UNKNOWN.ordinal();
        int subClass = ShortageSubClassRate.HW_SYSTEM.ordinal();
        boolean isGroup = true;
        if (isPerceptable(procInfo) || shortageProc.isKeyPercepService(procInfo.mPid)) {
            classType = ClassRate.KEYBACKGROUND.ordinal();
            subClass = ShortageSubClassRate.NONE.ordinal();
        } else if (curAdj == 300) {
            classType = ClassRate.KEYBACKGROUND.ordinal();
            subClass = ShortageSubClassRate.NONE.ordinal();
        } else if (curAdj == 400) {
            classType = ClassRate.KEYBACKGROUND.ordinal();
            subClass = ShortageSubClassRate.NONE.ordinal();
        } else if (shortageProc.isHomeProcess(procInfo.mPid) || shortageProc.isHomeAssocStrong(awareProcInfo)) {
            classType = ClassRate.HOME.ordinal();
            subClass = ShortageSubClassRate.NONE.ordinal();
        } else if (shortageProc.isRecentTaskShow() && isRecentTaskShowApp(procInfo, shortageProc)) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.PREV_ONECLEAN.ordinal();
        } else if (shortageProc.isAudioSubClass(procInfo, shortageProc.mAudioOut)) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.MUSIC_PLAY.ordinal();
        } else if (shortageProc.isAudioSubClass(procInfo, shortageProc.mAudioIn)) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.SOUND_RECORD.ordinal();
        } else if (isFgServices(procInfo, shortageProc)) {
            classType = ClassRate.KEYSERVICES.ordinal();
            shortageProc.recordFgServicesUid(procInfo.mUid);
            subClass = ShortageSubClassRate.FGSERVICES_TOPN.ordinal();
        } else if (cachedWhitelist.isInKeyHabitList(procInfo.mPackageName)) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.KEY_IM.ordinal();
        } else if (shortageProc.getKeyBackgroupTypeInternal(procInfo) == 3) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.GUIDE.ordinal();
        } else if (shortageProc.getKeyBackgroupTypeInternal(procInfo) == 5) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.DOWN_UP_LOAD.ordinal();
        } else if (cachedWhitelist.isLowEnd() || shortageProc.getKeyBackgroupTypeInternal(procInfo) != 4) {
            if (isLastRecentlyUsed(procInfo, shortageProc, shortageProc.mKillMore ? PREVIOUS_APP_DIRCACTIVITY_DECAYTIME : -1)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.PREVIOUS.ordinal();
            } else if (awareProcInfo.mXmlConfig != null && isCfgDefaultGroup(awareProcInfo, 1) && (!awareProcInfo.mXmlConfig.mFrequentlyUsed || shortageProc.isHabitTopN(procInfo.mPid))) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.AWARE_PROTECTED.ordinal();
            } else if (!cachedWhitelist.isLowEnd() && isKeySysProc(awareProcInfo)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.KEY_SYS_SERVICE.ordinal();
            } else if (shortageProc.isForegroundUid(awareProcInfo.mProcInfo)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.ASSOC_WITH_FG.ordinal();
            } else if (containsVisibleWindow(shortageProc.mVisibleWinPkg, procInfo.mPackageName)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.VISIBLEWIN.ordinal();
            } else if (!shortageProc.mKillMore && shortageProc.isHabitTopN(procInfo.mPid)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.TOPN.ordinal();
            } else if (isWidget(shortageProc.mWidgetPkg, procInfo.mPackageName)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.WIDGET.ordinal();
            } else {
                isGroup = false;
            }
        } else {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.HEALTH.ordinal();
        }
        if (isGroup) {
            awareProcInfo.mClassRate = classType;
            awareProcInfo.mSubClassRate = subClass;
        }
        return isGroup;
    }

    private boolean isLastRecentlyUsed(ProcessInfo procInfo, ShortageProcessInfo shortageProc, long decayTime) {
        if (decayTime == -1 && procInfo.mCurAdj == HwActivityManagerService.PREVIOUS_APP_ADJ) {
            return true;
        }
        boolean z;
        if (isLastRecentlyUsedBase(procInfo, shortageProc.mAllProcNeedSort, shortageProc.mPrevAmsBase, decayTime)) {
            z = true;
        } else {
            z = isLastRecentlyUsedBase(procInfo, shortageProc.mAllProcNeedSort, shortageProc.mPrevAwareBase, decayTime);
        }
        return z;
    }

    private boolean isWidget(Set<String> widgets, List<String> pkgList) {
        if (widgets == null || pkgList == null || widgets.isEmpty()) {
            return false;
        }
        for (String pkg : pkgList) {
            if (widgets.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean isClock(Set<String> clocks, ArrayList<String> packageNames) {
        if (!(clocks == null || clocks.isEmpty() || packageNames == null || packageNames.isEmpty())) {
            for (String pkg : packageNames) {
                if (pkg != null && clocks.contains(pkg)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRecentTaskShow(ArrayMap<Integer, AwareProcessInfo> foreGrdProc) {
        if (foreGrdProc == null || foreGrdProc.size() > 0) {
            return false;
        }
        return true;
    }

    private boolean isRecentTaskShowApp(ProcessInfo procInfo, ShortageProcessInfo shortageProc) {
        return isLastRecentlyUsedBase(procInfo, shortageProc.mAllProcNeedSort, shortageProc.mRecentTaskAppBase, -1);
    }

    private boolean isKeySysProc(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (!isSystemProcess(procInfo)) {
            return false;
        }
        if (procInfo.mCurAdj == HwActivityManagerService.SERVICE_ADJ) {
            return true;
        }
        if (procInfo.mUid < 10000 && procInfo.mCurAdj == HwActivityManagerService.SERVICE_B_ADJ) {
            return true;
        }
        if (procInfo.mUid >= 10000 || awareProcInfo.mHasShownUi || procInfo.mCreatedTime == -1 || SystemClock.elapsedRealtime() - procInfo.mCreatedTime >= AppMngConfig.getKeySysDecay()) {
            return procInfo.mUid >= 10000 && !awareProcInfo.mHasShownUi && procInfo.mCreatedTime != -1 && SystemClock.elapsedRealtime() - procInfo.mCreatedTime < AppMngConfig.getSysDecay();
        } else {
            return true;
        }
    }

    private boolean groupIntoAllowStop(AwareProcessInfo awareProcInfo, ShortageProcessInfo shortageProc, CachedWhiteList cachedWhitelist) {
        if (awareProcInfo == null) {
            return false;
        }
        int subClassType;
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (shortageProc.mKillMore && isLastRecentlyUsed(procInfo, shortageProc, -1)) {
            subClassType = AllowStopSubClassRate.PREVIOUS.ordinal();
        } else if (shortageProc.mKillMore && shortageProc.isHabitTopN(procInfo.mPid)) {
            subClassType = AllowStopSubClassRate.TOPN.ordinal();
        } else if (cachedWhitelist.isLowEnd() && isKeySysProc(awareProcInfo)) {
            subClassType = AllowStopSubClassRate.KEY_SYS_SERVICE.ordinal();
        } else if (cachedWhitelist.isLowEnd() && shortageProc.getKeyBackgroupTypeInternal(procInfo) == 4) {
            subClassType = AllowStopSubClassRate.HEALTH.ordinal();
        } else if (isFgServicesImportant(procInfo)) {
            subClassType = AllowStopSubClassRate.FG_SERVICES.ordinal();
        } else {
            subClassType = AllowStopSubClassRate.OTHER.ordinal();
        }
        awareProcInfo.mClassRate = ClassRate.NORMAL.ordinal();
        awareProcInfo.mSubClassRate = subClassType;
        return true;
    }

    private void addProcessInfoToBlock(Map<Integer, AwareProcessBlockInfo> appAllClass, AwareProcessInfo pinfo, int key) {
        if (appAllClass != null && pinfo != null) {
            AwareProcessBlockInfo info = new AwareProcessBlockInfo(pinfo.mProcInfo.mUid, false, pinfo.mClassRate);
            info.mProcessList.add(pinfo);
            info.mSubClassRate = pinfo.mSubClassRate;
            info.mImportance = pinfo.mImportance;
            info.mMinAdj = pinfo.mProcInfo.mCurAdj;
            appAllClass.put(Integer.valueOf(key), info);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isBgDecayApp(String pkgName, CachedWhiteList cachedWhitelist) {
        if (pkgName == null || cachedWhitelist == null || cachedWhitelist.mBgNonDecayPkg.contains(pkgName)) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getRestartFlagByProc(int classRate, int subRate, List<String> pkg, CachedWhiteList cachedWhitelist, boolean isRestartByAppType, ProcessInfo procInfo, int appType, AwareProcessBlockInfo value) {
        if (pkg == null || cachedWhitelist == null || subRate == AllowStopSubClassRate.NONE.ordinal() || subRate == AllowStopSubClassRate.PREVIOUS.ordinal() || subRate == AllowStopSubClassRate.TOPN.ordinal() || subRate == AllowStopSubClassRate.HEALTH.ordinal() || subRate == AllowStopSubClassRate.NONCURUSER.ordinal() || subRate == AllowStopSubClassRate.UNKNOWN.ordinal()) {
            return true;
        }
        if (cachedWhitelist.isLowEnd() && subRate == AllowStopSubClassRate.KEY_SYS_SERVICE.ordinal()) {
            return false;
        }
        for (String pkgName : pkg) {
            if (cachedWhitelist.mRestartAppList.contains(pkgName)) {
                return true;
            }
            if (cachedWhitelist.mAllProtectApp.contains(pkgName) && (!cachedWhitelist.isLowEnd() || !isBgDecayApp(pkgName, cachedWhitelist))) {
                return true;
            }
            if (!cachedWhitelist.mBadAppList.contains(pkgName) && isRestartByAppType) {
                if (AppMngConfig.getAbroadFlag() && cachedWhitelist.mAllUnProtectApp.contains(pkgName)) {
                    value.mAlarmChk = true;
                } else if (cachedWhitelist.isLowEnd()) {
                    if (!isBgDecayApp(pkgName, cachedWhitelist)) {
                        return true;
                    }
                    if (!isDecayAppByAppType(appType)) {
                        value.mAlarmChk = true;
                    }
                } else if (!isDecayAppByAppType(appType) || !isBgDecayApp(pkgName, cachedWhitelist)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean getAllowCleanResByProc(int classRate, int subRate, CachedWhiteList cachedWhitelist, ArrayList<String> pkgList, boolean isRestartByAppType) {
        boolean z = true;
        if (cachedWhitelist == null) {
            return false;
        }
        if (!cachedWhitelist.isLowEnd()) {
            return true;
        }
        if (subRate != AllowStopSubClassRate.FG_SERVICES.ordinal()) {
            if (subRate != AllowStopSubClassRate.OTHER.ordinal()) {
                z = false;
            }
            return z;
        } else if (!isRestartByAppType) {
            return true;
        } else {
            if (!AppMngConfig.getAbroadFlag() || pkgList == null || pkgList.isEmpty()) {
                return false;
            }
            for (String pkgName : pkgList) {
                if (!cachedWhitelist.mAllUnProtectApp.contains(pkgName)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean getAllowCleanResByAppType(int appType) {
        switch (appType) {
            case 14:
                return false;
            default:
                return true;
        }
    }

    private void updateAppType(AwareProcessBlockInfo info, CachedWhiteList cachedWhitelist) {
        if (info != null) {
            for (AwareProcessInfo procInfo : info.mProcessList) {
                if (!(procInfo == null || procInfo.mProcInfo == null || procInfo.mProcInfo.mPackageName == null)) {
                    for (String pkg : procInfo.mProcInfo.mPackageName) {
                        int type = AppTypeRecoManager.getInstance().getAppType(pkg);
                        if (!getRestartFlagByAppType(type)) {
                            info.mAppType = type;
                        } else if (isDecayAppByAppType(type)) {
                            info.mAppType = type;
                        } else {
                            info.mAppType = -1;
                            return;
                        }
                    }
                    continue;
                }
            }
        }
    }

    private boolean getRestartFlagByAppType(int appType) {
        switch (appType) {
            case 3:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 12:
            case 13:
            case 14:
            case 15:
            case 17:
            case 18:
            case 19:
                return false;
            default:
                return true;
        }
    }

    private boolean isDecayAppByAppType(int appType) {
        switch (appType) {
            case 20:
            case 21:
                return true;
            default:
                return false;
        }
    }

    public boolean needCheckAlarm(AwareProcessBlockInfo info) {
        if (info == null) {
            return false;
        }
        if (AppMngConfig.getAlarmCheckFlag()) {
            return true;
        }
        return info.mAlarmChk;
    }

    private void addClassToAllClass(Map<Integer, AwareProcessBlockInfo> appAllClass, Map<Integer, AwareProcessBlockInfo> blocks, ShortageProcessInfo shortageProc, Map<Integer, AwareProcessBlockInfo> allUids, CachedWhiteList cachedWhitelist, Set<String> clocks, boolean chkRestartFlag) {
        for (Entry<Integer, AwareProcessBlockInfo> m : blocks.entrySet()) {
            AwareProcessBlockInfo value = (AwareProcessBlockInfo) m.getValue();
            Map<Integer, AwareProcessBlockInfo> map = appAllClass;
            map.put(Integer.valueOf(((Integer) m.getKey()).intValue()), value);
            if (chkRestartFlag) {
                boolean isWidgetApp = false;
                boolean isClockApp = false;
                boolean isImApp = false;
                boolean isAllowCleanRes = true;
                boolean isRestart = false;
                boolean isRestartByAppType = false;
                if (AppMngConfig.getAbroadFlag() || AppMngConfig.getRestartFlag()) {
                    isRestartByAppType = true;
                } else {
                    updateAppType(value, cachedWhitelist);
                    if (getRestartFlagByAppType(value.mAppType)) {
                        isRestartByAppType = true;
                    } else if (!(cachedWhitelist.isLowEnd() || getAllowCleanResByAppType(value.mAppType))) {
                        isAllowCleanRes = false;
                    }
                }
                for (AwareProcessInfo procInfo : value.mProcessList) {
                    if (!AppMngConfig.getRestartFlag()) {
                        if (!getRestartFlagByProc(value.mClassRate, value.mSubClassRate, procInfo.mProcInfo.mPackageName, cachedWhitelist, isRestartByAppType, procInfo.mProcInfo, value.mAppType, value)) {
                            if (isAllowCleanRes) {
                                if (!isWidgetApp) {
                                    isWidgetApp = isWidget(shortageProc.mWidgetPkg, procInfo.mProcInfo.mPackageName);
                                }
                                if (!isClockApp) {
                                    isClockApp = isClock(clocks, procInfo.mProcInfo.mPackageName);
                                }
                                if (!isImApp) {
                                    isImApp = cachedWhitelist.isInAllHabitList(procInfo.mProcInfo.mPackageName);
                                }
                                if (procInfo.mXmlConfig != null) {
                                    isAllowCleanRes = procInfo.mXmlConfig.mResCleanAllow;
                                }
                                if (isAllowCleanRes) {
                                    if (!getAllowCleanResByProc(value.mClassRate, value.mSubClassRate, cachedWhitelist, procInfo.mProcInfo.mPackageName, isRestartByAppType)) {
                                        isAllowCleanRes = false;
                                    }
                                }
                            }
                        }
                    }
                    isRestart = true;
                }
                if (!(isRestart || isWidgetApp || isClockApp || isImApp || !isAllowCleanRes)) {
                    if (inSameUids(allUids, value.mProcessList)) {
                        value.mResCleanAllow = true;
                        value.mCleanAlarm = true;
                        if (!isRestart || isImApp) {
                            for (AwareProcessInfo procInfo2 : value.mProcessList) {
                                procInfo2.mRestartFlag = true;
                            }
                        }
                    }
                }
                value.mResCleanAllow = false;
                value.mCleanAlarm = false;
                if (isRestart) {
                }
                while (procInfo$iterator.hasNext()) {
                    procInfo2.mRestartFlag = true;
                }
            }
        }
    }

    private void addProcessInfoToGroupBlock(Map<Integer, AwareProcessBlockInfo> appAllClass, AwareProcessInfo pinfo, int key, Map<Integer, Map<Integer, AwareProcessBlockInfo>> groupBlock) {
        Integer groupKey = Integer.valueOf((pinfo.mClassRate << 8) + pinfo.mSubClassRate);
        Map<Integer, AwareProcessBlockInfo> groupUid = (Map) groupBlock.get(groupKey);
        if (groupUid == null) {
            groupUid = new ArrayMap();
            groupBlock.put(groupKey, groupUid);
        }
        AwareProcessBlockInfo block = (AwareProcessBlockInfo) groupUid.get(Integer.valueOf(pinfo.mProcInfo.mUid));
        if (block == null) {
            block = new AwareProcessBlockInfo(pinfo.mProcInfo.mUid, false, pinfo.mClassRate);
            block.mProcessList.add(pinfo);
            block.mSubClassRate = pinfo.mSubClassRate;
            block.mImportance = pinfo.mImportance;
            block.mMinAdj = pinfo.mProcInfo.mCurAdj;
            groupUid.put(Integer.valueOf(pinfo.mProcInfo.mUid), block);
            appAllClass.put(Integer.valueOf(key), block);
            return;
        }
        if (block.mImportance > pinfo.mImportance) {
            block.mImportance = pinfo.mImportance;
        }
        if (block.mMinAdj > pinfo.mProcInfo.mCurAdj) {
            block.mMinAdj = pinfo.mProcInfo.mCurAdj;
        }
        block.mProcessList.add(pinfo);
    }

    private boolean isCfgDefaultGroup(AwareProcessInfo procInfo, int groupId) {
        boolean z = false;
        if (procInfo == null || procInfo.mXmlConfig == null) {
            return false;
        }
        if (procInfo.mXmlConfig.mCfgDefaultGroup == groupId) {
            z = true;
        }
        return z;
    }

    private ArrayMap<Integer, AwareProcessBlockInfo> getAppMemSortClassGroup(int subType) {
        ArraySet<Integer> keyPercepServicePid = new ArraySet();
        ArrayMap<Integer, AwareProcessInfo> foreGrdProc = new ArrayMap();
        ShortageProcessInfo shortageProc = new ShortageProcessInfo(subType);
        CachedWhiteList cachedWhitelist = new CachedWhiteList();
        cachedWhitelist.updateCachedList();
        Set<String> clocks = AppTypeRecoManager.getInstance().getAlarmApps();
        ArrayMap<Integer, AwareProcessInfo> allProcNeedSort = getNeedSortedProcesses(foreGrdProc, keyPercepServicePid, cachedWhitelist, shortageProc);
        if (allProcNeedSort == null) {
            return null;
        }
        ArrayMap<Integer, AwareProcessBlockInfo> appAllClass = new ArrayMap();
        ArrayMap<Integer, AwareProcessBlockInfo> classShort = new ArrayMap();
        ArrayMap<Integer, AwareProcessBlockInfo> classNormal = new ArrayMap();
        Map<Integer, Map<Integer, AwareProcessBlockInfo>> groupBlock = new ArrayMap();
        Map<Integer, AwareProcessBlockInfo> allUids = groupByUid(allProcNeedSort);
        shortageProc.updateBaseInfo(allProcNeedSort, getCurHomeProcessPid(), isRecentTaskShow(foreGrdProc), keyPercepServicePid);
        for (Entry<Integer, AwareProcessInfo> m : allProcNeedSort.entrySet()) {
            AwareProcessInfo awareProcInfo = (AwareProcessInfo) m.getValue();
            boolean isGroup = groupIntoForbidstop(shortageProc, awareProcInfo);
            if (!isGroup) {
                isGroup = groupIntoShortageStop(awareProcInfo, shortageProc, cachedWhitelist);
            }
            if (!isGroup) {
                groupIntoAllowStop(awareProcInfo, shortageProc, cachedWhitelist);
            }
            if (awareProcInfo.mClassRate < ClassRate.KEYBACKGROUND.ordinal()) {
                addProcessInfoToBlock(appAllClass, awareProcInfo, awareProcInfo.mPid);
            } else if (awareProcInfo.mClassRate < ClassRate.NORMAL.ordinal()) {
                addProcessInfoToGroupBlock(classShort, awareProcInfo, awareProcInfo.mPid, groupBlock);
            } else {
                addProcessInfoToBlock(classNormal, awareProcInfo, awareProcInfo.mPid);
            }
        }
        groupNonCurUserProc(classNormal, shortageProc.mNonCurUserProc);
        adjustClassRate(allProcNeedSort, classShort, classNormal, appAllClass, shortageProc, allUids, cachedWhitelist, clocks);
        addClassToAllClass(appAllClass, classShort, shortageProc, allUids, cachedWhitelist, clocks, false);
        return appAllClass;
    }

    private Map<AppBlockKeyBase, AwareProcessBlockInfo> convertToUidBlock(Map<Integer, AwareProcessBlockInfo> pidsBlock, ArrayMap<Integer, AppBlockKeyBase> pidsAppBlock, ArrayMap<Integer, AppBlockKeyBase> uidAppBlock) {
        if (pidsBlock == null) {
            return null;
        }
        Map<AppBlockKeyBase, AwareProcessBlockInfo> uids = new ArrayMap();
        for (Entry<Integer, AwareProcessBlockInfo> m : pidsBlock.entrySet()) {
            AwareProcessBlockInfo blockInfo = (AwareProcessBlockInfo) m.getValue();
            if (blockInfo.mProcessList != null) {
                for (AwareProcessInfo awareProcInfo : blockInfo.mProcessList) {
                    AppBlockKeyBase blockKeyValue;
                    AwareProcessBlockInfo info;
                    if (AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid)) {
                        blockKeyValue = (AppBlockKeyBase) pidsAppBlock.get(Integer.valueOf(awareProcInfo.mProcInfo.mPid));
                    } else {
                        blockKeyValue = (AppBlockKeyBase) uidAppBlock.get(Integer.valueOf(awareProcInfo.mProcInfo.mUid));
                    }
                    if (blockKeyValue == null) {
                        info = null;
                    } else {
                        info = (AwareProcessBlockInfo) uids.get(blockKeyValue);
                    }
                    if (info == null) {
                        info = new AwareProcessBlockInfo(awareProcInfo.mProcInfo.mUid, false, awareProcInfo.mClassRate);
                        info.mProcessList.add(awareProcInfo);
                        info.mClassRate = blockInfo.mClassRate;
                        info.mSubClassRate = blockInfo.mSubClassRate;
                        AppBlockKeyBase keyBase = new AppBlockKeyBase(awareProcInfo.mProcInfo.mPid, awareProcInfo.mProcInfo.mUid);
                        if (AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid)) {
                            pidsAppBlock.put(Integer.valueOf(awareProcInfo.mProcInfo.mPid), keyBase);
                        } else {
                            uidAppBlock.put(Integer.valueOf(awareProcInfo.mProcInfo.mUid), keyBase);
                        }
                        uids.put(keyBase, info);
                    } else {
                        if (info.mSubClassRate > awareProcInfo.mSubClassRate) {
                            info.mSubClassRate = awareProcInfo.mSubClassRate;
                        }
                        info.mProcessList.add(awareProcInfo);
                    }
                }
            }
        }
        return uids;
    }

    private Map<Integer, AwareProcessBlockInfo> groupByUid(Map<Integer, AwareProcessInfo> allProcNeedSort) {
        if (allProcNeedSort == null) {
            return null;
        }
        Map<Integer, AwareProcessBlockInfo> uids = new ArrayMap();
        Map userHabitMap = null;
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null) {
            userHabitMap = habit.getTopList(allProcNeedSort);
        }
        for (Entry<Integer, AwareProcessInfo> m : allProcNeedSort.entrySet()) {
            AwareProcessInfo info = (AwareProcessInfo) m.getValue();
            if (info != null) {
                Integer num = null;
                if (userHabitMap != null) {
                    num = (Integer) userHabitMap.get(Integer.valueOf(info.mPid));
                }
                if (num != null) {
                    info.mImportance = num.intValue();
                } else {
                    info.mImportance = 10000;
                }
                AwareProcessBlockInfo block = (AwareProcessBlockInfo) uids.get(Integer.valueOf(info.mProcInfo.mUid));
                if (block == null) {
                    block = new AwareProcessBlockInfo(info.mProcInfo.mUid, false, info.mClassRate);
                    block.mProcessList.add(info);
                    uids.put(Integer.valueOf(info.mProcInfo.mUid), block);
                } else {
                    block.mProcessList.add(info);
                }
            }
        }
        return uids;
    }

    private void adjustClassByStrongAssoc(AwareProcessBlockInfo blockInfo, Map<Integer, AwareProcessInfo> allProcNeedSort, Map<AppBlockKeyBase, AwareProcessBlockInfo> uids, ArrayMap<Integer, AppBlockKeyBase> pidsAppBlock, ArrayMap<Integer, AppBlockKeyBase> uidAppBlock, Map<AppBlockKeyBase, AwareProcessBlockInfo> assocNormalUids) {
        ArrayMap<Integer, AwareProcessInfo> strong = new ArrayMap();
        loadAppAssoc(blockInfo.mProcessList, allProcNeedSort, strong);
        for (Entry<Integer, AwareProcessInfo> sm : strong.entrySet()) {
            AwareProcessInfo procInfo = (AwareProcessInfo) allProcNeedSort.get(sm.getKey());
            if (!(procInfo == null || procInfo.mProcInfo == null)) {
                AppBlockKeyBase blockKeyValue;
                AwareProcessBlockInfo blockInfoAssoc;
                if (AwareAppAssociate.isDealAsPkgUid(procInfo.mProcInfo.mUid)) {
                    blockKeyValue = (AppBlockKeyBase) pidsAppBlock.get(Integer.valueOf(procInfo.mProcInfo.mPid));
                } else {
                    blockKeyValue = (AppBlockKeyBase) uidAppBlock.get(Integer.valueOf(procInfo.mProcInfo.mUid));
                }
                if (blockKeyValue == null) {
                    blockInfoAssoc = null;
                } else {
                    blockInfoAssoc = (AwareProcessBlockInfo) uids.get(blockKeyValue);
                }
                if (!(blockInfoAssoc == null || blockInfoAssoc.mProcessList == null || blockInfoAssoc.mProcessList.size() <= 0)) {
                    if (blockInfoAssoc.mClassRate > blockInfo.mClassRate) {
                        blockInfoAssoc.mClassRate = blockInfo.mClassRate;
                        blockInfoAssoc.mSubClassRate = blockInfo.mSubClassRate;
                        blockInfoAssoc.mSubTypeStr = SUBTYPE_ASSOCIATION;
                        if (assocNormalUids != null) {
                            assocNormalUids.put(blockKeyValue, blockInfoAssoc);
                        }
                    } else if (blockInfoAssoc.mClassRate == blockInfo.mClassRate && blockInfoAssoc.mSubClassRate > blockInfo.mSubClassRate) {
                        blockInfoAssoc.mSubClassRate = blockInfo.mSubClassRate;
                        blockInfoAssoc.mSubTypeStr = SUBTYPE_ASSOCIATION;
                    }
                }
            }
        }
    }

    private void adjustClassRate(Map<Integer, AwareProcessInfo> allProcNeedSort, Map<Integer, AwareProcessBlockInfo> classShort, Map<Integer, AwareProcessBlockInfo> classNormal, Map<Integer, AwareProcessBlockInfo> allClass, ShortageProcessInfo shortageProc, Map<Integer, AwareProcessBlockInfo> allUids, CachedWhiteList cachedWhitelist, Set<String> clocks) {
        if (allProcNeedSort != null && classNormal != null) {
            ArrayMap<Integer, AppBlockKeyBase> pidsAppBlock = new ArrayMap();
            ArrayMap<Integer, AppBlockKeyBase> uidAppBlock = new ArrayMap();
            Map<AppBlockKeyBase, AwareProcessBlockInfo> uids = convertToUidBlock(classNormal, pidsAppBlock, uidAppBlock);
            if (uids != null) {
                AwareProcessBlockInfo blockInfo;
                Map<Integer, AwareProcessBlockInfo> assocNormalClass = new ArrayMap();
                for (Entry<Integer, AwareProcessBlockInfo> m : classShort.entrySet()) {
                    blockInfo = (AwareProcessBlockInfo) m.getValue();
                    if (blockInfo.mClassRate == ClassRate.KEYSERVICES.ordinal()) {
                        Map<AppBlockKeyBase, AwareProcessBlockInfo> assocNormalUids = new ArrayMap();
                        adjustClassByStrongAssoc(blockInfo, allProcNeedSort, uids, pidsAppBlock, uidAppBlock, assocNormalUids);
                        for (Entry<AppBlockKeyBase, AwareProcessBlockInfo> assocBlock : assocNormalUids.entrySet()) {
                            AppBlockKeyBase blockKeyValue = (AppBlockKeyBase) assocBlock.getKey();
                            AwareProcessBlockInfo blockInfoAssoc = (AwareProcessBlockInfo) assocBlock.getValue();
                            if (AwareAppAssociate.isDealAsPkgUid(blockKeyValue.mUid)) {
                                pidsAppBlock.remove(Integer.valueOf(blockKeyValue.mPid));
                                uids.remove(blockKeyValue);
                            } else {
                                uidAppBlock.remove(Integer.valueOf(blockKeyValue.mUid));
                                uids.remove(blockKeyValue);
                            }
                            assocNormalClass.put(Integer.valueOf(blockKeyValue.mPid), blockInfoAssoc);
                        }
                    }
                }
                classShort.putAll(assocNormalClass);
                Map<Integer, AwareProcessBlockInfo> classNormalBlock = new ArrayMap();
                for (Entry<AppBlockKeyBase, AwareProcessBlockInfo> m2 : uids.entrySet()) {
                    blockInfo = (AwareProcessBlockInfo) m2.getValue();
                    adjustClassByStrongAssoc(blockInfo, allProcNeedSort, uids, pidsAppBlock, uidAppBlock, null);
                    boolean addToAll = false;
                    int subClass = AllowStopSubClassRate.UNKNOWN.ordinal();
                    for (AwareProcessInfo procInfo : blockInfo.mProcessList) {
                        if (addToAll) {
                            if (blockInfo.mSubClassRate > procInfo.mSubClassRate) {
                                blockInfo.mSubClassRate = procInfo.mSubClassRate;
                            }
                            if (blockInfo.mImportance > procInfo.mImportance) {
                                blockInfo.mImportance = procInfo.mImportance;
                            }
                            if (blockInfo.mMinAdj > procInfo.mProcInfo.mCurAdj) {
                                blockInfo.mMinAdj = procInfo.mProcInfo.mCurAdj;
                            }
                        } else {
                            blockInfo.mImportance = procInfo.mImportance;
                            blockInfo.mMinAdj = procInfo.mProcInfo.mCurAdj;
                            blockInfo.mSubClassRate = procInfo.mSubClassRate;
                            classNormalBlock.put(Integer.valueOf(procInfo.mProcInfo.mPid), blockInfo);
                            if (shortageProc.isFgServicesUid(blockInfo.mUid)) {
                                subClass = AllowStopSubClassRate.FG_SERVICES.ordinal();
                            }
                            if (blockInfo.mSubClassRate > subClass) {
                                blockInfo.mSubClassRate = subClass;
                            }
                            addToAll = true;
                        }
                    }
                }
                addClassToAllClass(allClass, classNormalBlock, shortageProc, allUids, cachedWhitelist, clocks, true);
            }
        }
    }

    private boolean inSameUids(Map<Integer, AwareProcessBlockInfo> allUids, List<AwareProcessInfo> lists) {
        if (lists == null || lists.isEmpty()) {
            return false;
        }
        AwareProcessBlockInfo info = (AwareProcessBlockInfo) allUids.get(Integer.valueOf(((AwareProcessInfo) lists.get(0)).mProcInfo.mUid));
        if (info == null || info.mProcessList == null) {
            return false;
        }
        return info.mProcessList.equals(lists);
    }

    private MemSortGroup getAppMemSortGroup(int subType) {
        ArrayMap<Integer, AwareProcessBlockInfo> pidsClass = getAppMemSortClassGroup(subType);
        if (pidsClass == null) {
            return null;
        }
        List<AwareProcessBlockInfo> procForbidStopList = new ArrayList();
        List<AwareProcessBlockInfo> procShortageStopList = new ArrayList();
        List<AwareProcessBlockInfo> procAllowStopList = new ArrayList();
        for (Entry<Integer, AwareProcessBlockInfo> m : pidsClass.entrySet()) {
            AwareProcessBlockInfo awareProcInfo = (AwareProcessBlockInfo) m.getValue();
            awareProcInfo.mUpdateTime = SystemClock.elapsedRealtime();
            int groupId = 0;
            if (awareProcInfo.mClassRate <= ClassRate.FOREGROUND.ordinal()) {
                procForbidStopList.add(awareProcInfo);
            } else if (awareProcInfo.mClassRate < ClassRate.NORMAL.ordinal()) {
                procShortageStopList.add(awareProcInfo);
                groupId = 1;
            } else {
                procAllowStopList.add(awareProcInfo);
                groupId = 2;
            }
            awareProcInfo.setMemGroup(groupId);
        }
        Collections.sort(procShortageStopList);
        Collections.sort(procAllowStopList);
        return new MemSortGroup(procForbidStopList, procShortageStopList, procAllowStopList);
    }

    public static String getClassRateStr(int classRate) {
        for (ClassRate rate : ClassRate.values()) {
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return ClassRate.UNKNOWN.description();
    }

    public boolean isGroupBeHigher(int pid, int uid, String processName, ArrayList<String> arrayList, int groupId) {
        AwareProcessBaseInfo info = null;
        if (!mEnabled || !this.mAssocEnable) {
            return false;
        }
        AwareAppAssociate awareAssoc = AwareAppAssociate.getInstance();
        if (awareAssoc == null) {
            return false;
        }
        Set<Integer> forePid = new ArraySet();
        awareAssoc.getForeGroundApp(forePid);
        if (forePid.contains(Integer.valueOf(pid))) {
            return true;
        }
        if (this.mHwAMS != null) {
            info = this.mHwAMS.getProcessBaseInfo(pid);
        }
        if (info == null) {
            return false;
        }
        if (info.mCurAdj < 200) {
            return true;
        }
        if (groupId == 2) {
            if (info.mCurAdj == 300 || info.mCurAdj == 400) {
                return true;
            }
            return info.mCurAdj == 200 && !isFgServicesImportantByAdjtype(info.mAdjType);
        }
    }

    public static boolean checkAppMngEnable() {
        return mEnabled;
    }

    public AwareAppMngSortPolicy getAppMngSortPolicy(int resourceType, int subType, int groupId) {
        if (!mEnabled) {
            return null;
        }
        long startTime = 0;
        if (DEBUG) {
            startTime = System.currentTimeMillis();
        }
        ArrayMap<Integer, List<AwareProcessBlockInfo>> appGroup = new ArrayMap();
        MemSortGroup sortGroup = getAppMemSortGroup(subType);
        if (sortGroup == null) {
            return null;
        }
        if (groupId == 0) {
            appGroup.put(Integer.valueOf(groupId), sortGroup.mProcForbidStopList);
        } else if (groupId == 1) {
            appGroup.put(Integer.valueOf(groupId), sortGroup.mProcShortageStopList);
        } else if (groupId == 2) {
            appGroup.put(Integer.valueOf(groupId), sortGroup.mProcAllowStopList);
        } else if (groupId == 3) {
            appGroup.put(Integer.valueOf(0), sortGroup.mProcForbidStopList);
            appGroup.put(Integer.valueOf(1), sortGroup.mProcShortageStopList);
            appGroup.put(Integer.valueOf(2), sortGroup.mProcAllowStopList);
        }
        AwareAppMngSortPolicy sortPolicy = new AwareAppMngSortPolicy(this.mContext, appGroup);
        if (DEBUG) {
            AwareLog.i(TAG, "        getAppMngSortPolicy eclipse time     :" + (System.currentTimeMillis() - startTime));
            AwareLog.i(TAG, "MemAvailable(KB): " + MemoryReader.getInstance().getMemAvailable());
            dumpPolicy(sortPolicy, null, false);
        }
        if (Log.HWINFO) {
            long curTime = System.currentTimeMillis();
            if (curTime - this.mLastBetaLogOutTime > AppHibernateCst.DELAY_ONE_MINS && this.mHandler != null) {
                BetaLog betaLog = new BetaLog(sortPolicy);
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = betaLog;
                this.mHandler.sendMessage(msg);
                this.mLastBetaLogOutTime = curTime;
            }
        }
        return sortPolicy;
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }

    public void enableAssocDebug() {
        this.mAssocEnable = true;
    }

    public void disableAssocDebug() {
        this.mAssocEnable = false;
    }

    public boolean getAssocDebug() {
        return this.mAssocEnable;
    }

    private static String getForbidSubClassRateStr(int classRate) {
        for (ForbidSubClassRate rate : ForbidSubClassRate.values()) {
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return ForbidSubClassRate.NONE.description();
    }

    private static String getShortageSubClassRateStr(int classRate) {
        for (ShortageSubClassRate rate : ShortageSubClassRate.values()) {
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return ShortageSubClassRate.NONE.description();
    }

    private static String getAllowSubClassRateStr(int classRate) {
        for (AllowStopSubClassRate rate : AllowStopSubClassRate.values()) {
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return AllowStopSubClassRate.NONE.description();
    }

    private String getClassStr(int classRate, int subClassRate) {
        if (classRate == ClassRate.FOREGROUND.ordinal()) {
            return getForbidSubClassRateStr(subClassRate);
        }
        if (classRate == ClassRate.KEYSERVICES.ordinal()) {
            return getShortageSubClassRateStr(subClassRate);
        }
        return getAllowSubClassRateStr(subClassRate);
    }

    private void updateProcessInfo() {
        ProcessInfoCollector processInfoCollector = ProcessInfoCollector.getInstance();
        if (processInfoCollector != null) {
            ArrayList<ProcessInfo> procs = processInfoCollector.getProcessInfoList();
            if (!procs.isEmpty()) {
                for (ProcessInfo procInfo : procs) {
                    if (procInfo != null) {
                        processInfoCollector.recordProcessInfo(procInfo.mPid, procInfo.mUid);
                    }
                }
            }
        }
    }

    private int getCurHomeProcessPid() {
        return AwareAppAssociate.getInstance().getCurHomeProcessPid();
    }

    public boolean isProcessBlockPidChanged(AwareProcessBlockInfo procGroup) {
        if (!mEnabled || procGroup == null) {
            return false;
        }
        ArrayList<ProcessInfo> procs = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procs.isEmpty()) {
            return false;
        }
        int uid = procGroup.mUid;
        for (ProcessInfo procInfo : procs) {
            if (procInfo != null && procInfo.mUid == uid && procInfo.mCreatedTime - procGroup.mUpdateTime > 0) {
                return true;
            }
        }
        return false;
    }

    private void dumpBlockList(PrintWriter pw, List<AwareProcessBlockInfo> list, boolean toPrint) {
        if (list != null && (pw != null || !toPrint)) {
            for (AwareProcessBlockInfo pinfo : list) {
                if (pinfo != null) {
                    boolean allow = pinfo.mResCleanAllow;
                    print(pw, "AppProc:uid:" + pinfo.mUid + ",import:" + pinfo.mImportance + ",classRates:" + pinfo.mClassRate + ",classStr:" + getClassRateStr(pinfo.mClassRate) + ",subStr:" + getClassStr(pinfo.mClassRate, pinfo.mSubClassRate) + ",subTypeStr:" + pinfo.mSubTypeStr + ",appType:" + pinfo.mAppType);
                    if (pinfo.mProcessList != null) {
                        for (AwareProcessInfo info : pinfo.mProcessList) {
                            print(pw, "     name:" + info.mProcInfo.mProcessName + ",pid:" + info.mProcInfo.mPid + ",uid:" + info.mProcInfo.mUid + ",group:" + info.mMemGroup + ",import:" + info.mImportance + ",classRate:" + info.mClassRate + ",adj:" + info.mProcInfo.mCurAdj + "," + info.mProcInfo.mAdjType + ",classStr:" + getClassRateStr(info.mClassRate) + ",subStr:" + getClassStr(info.mClassRate, info.mSubClassRate) + ",mResCleanAllow:" + allow + ",mRestartFlag:" + info.getRestartFlag() + ",ui:" + info.mHasShownUi);
                        }
                    }
                }
            }
        }
    }

    private void dumpBlock(PrintWriter pw, int memLevel) {
        if (pw != null) {
            if (mEnabled) {
                AwareAppMngSortPolicy policy = getAppMngSortPolicy(0, memLevel, 3);
                if (policy == null) {
                    pw.println("getAppMngSortPolicy return null!");
                    return;
                } else {
                    dumpPolicy(policy, pw, true);
                    return;
                }
            }
            pw.println("AwareAppMngSort disabled!");
        }
    }

    private void dumpGroupBlock(PrintWriter pw, int group) {
        if (pw != null) {
            if (mEnabled) {
                AwareAppMngSortPolicy policy = getAppMngSortPolicy(0, 0, group);
                if (policy == null) {
                    pw.println("getAppMngSortPolicy return null!");
                    return;
                } else {
                    dumpPolicy(policy, pw, true);
                    return;
                }
            }
            pw.println("AwareAppMngSort disabled!");
        }
    }

    private void dumpPolicy(AwareAppMngSortPolicy policy, PrintWriter pw, boolean toPrint) {
        if (policy != null && (pw != null || !toPrint)) {
            print(pw, "------------------start dump Group  forbidstop ------------------");
            dumpBlockList(pw, policy.getForbidStopProcBlockList(), toPrint);
            print(pw, "------------------start dump Group  shortagestop ------------------");
            dumpBlockList(pw, policy.getShortageStopProcBlockList(), toPrint);
            print(pw, "------------------start dump Group  allowstop ------------------");
            dumpBlockList(pw, policy.getAllowStopProcBlockList(), toPrint);
        }
    }

    private void print(PrintWriter pw, String info) {
        if (pw != null) {
            pw.println(info);
        } else if (DEBUG) {
            AwareLog.i(TAG, info);
        }
    }

    public void dump(PrintWriter pw, String type) {
        if (pw != null && type != null) {
            pw.println("  App Group Manager Information dump :");
            if (type.equals("mem")) {
                dumpBlock(pw, 0);
            } else if (type.equals("mem2")) {
                dumpBlock(pw, 1);
            } else if (type.equals("memForbid")) {
                dumpGroupBlock(pw, 0);
            } else if (type.equals("memShortage")) {
                dumpGroupBlock(pw, 1);
            } else if (type.equals("memAllow")) {
                dumpGroupBlock(pw, 2);
            } else if (type.equals("enable")) {
                enable();
            } else if (type.equals("disable")) {
                disable();
            } else if (type.equals("checkEnabled")) {
                pw.println("AwareAppMngSort is " + checkAppMngEnable());
            } else if (!type.equals("procinfo")) {
                pw.println("  dump parameter error!");
            } else if (mEnabled) {
                ProcessInfoCollector mProcInfo = ProcessInfoCollector.getInstance();
                if (mProcInfo != null) {
                    updateProcessInfo();
                    mProcInfo.dump(pw);
                }
            } else {
                pw.println("AwareAppMngSort disabled!");
            }
        }
    }

    private void dumpShortageSubClassRate(PrintWriter pw) {
        if (pw != null) {
            for (ShortageSubClassRate rate : ShortageSubClassRate.values()) {
                pw.println("    sub" + rate.ordinal() + ": value=" + rate.ordinal() + "," + rate.description());
            }
        }
    }

    private void dumpForbidSubClassRate(PrintWriter pw) {
        if (pw != null) {
            for (ForbidSubClassRate rate : ForbidSubClassRate.values()) {
                pw.println("    sub" + rate.ordinal() + ": value=" + rate.ordinal() + "," + rate.description());
            }
        }
    }

    private void dumpAllowStopSubClassRate(PrintWriter pw) {
        if (pw != null) {
            for (AllowStopSubClassRate rate : AllowStopSubClassRate.values()) {
                pw.println("    sub" + rate.ordinal() + ": value=" + rate.ordinal() + "," + rate.description());
            }
        }
    }

    public void dumpClassInfo(PrintWriter pw) {
        if (!mEnabled) {
            pw.println("AwareAppMngSort disabled!");
        } else if (pw != null) {
            for (ClassRate rate : ClassRate.values()) {
                pw.println("Class" + rate.ordinal() + ": value=" + rate.ordinal() + "," + rate.description());
                String subClass = ShortageSubClassRate.NONE.description();
                if (rate == ClassRate.FOREGROUND) {
                    dumpForbidSubClassRate(pw);
                } else if (rate == ClassRate.KEYSERVICES) {
                    dumpShortageSubClassRate(pw);
                } else if (rate == ClassRate.NORMAL) {
                    dumpAllowStopSubClassRate(pw);
                } else {
                    pw.println("    sub" + ShortageSubClassRate.NONE.ordinal() + ": value=" + ShortageSubClassRate.NONE.ordinal() + "," + subClass);
                }
            }
        }
    }
}
