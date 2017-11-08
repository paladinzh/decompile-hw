package com.android.server.rms.algorithm;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.SystemClock;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.LruCache;
import com.android.internal.os.BackgroundThread;
import com.android.server.am.HwActivityManagerService;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.algorithm.utils.ProtectApp;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class AwareUserHabitAlgorithm {
    private static final double DECREASE_RATE_PER_TRAINING = 0.9d;
    private static final String DEFAULT_PKG_NAME = "com.huawei.android.launcher";
    private static final String LRU = "lru";
    private static final int LRU_APP_NUM_LIMITED = 2;
    private static final long LRU_TIME_LIMIT = 1800000;
    private static final int MATCHTYPE_LRU = 2;
    private static final int MATCHTYPE_NONE_LRU = 3;
    private static final int MATCHTYPE_TRANS = 1;
    private static final long MIN_THRESHOLD = -31536000000L;
    private static final String NONE_LRU = "nonelru";
    private static final long STAY_IN_BACKGROUND_LIMIT_TIME = 86400000;
    private static final String TAG = "AwareUserHabitAlgorithm";
    private static final String TRANS = "trans";
    private static final int TRANS_APP_NUM_LIMITED = 2;
    private static final double TRANS_PRO_THRESHOLD = 0.3d;
    private ContentResolver mContentResolver = null;
    private Context mContext = null;
    private ArraySet<String> mFilterAppSet = new ArraySet();
    private final ArrayMap<String, Long> mGCMAppsArrayMap = new ArrayMap();
    private final Object mHabitLock = new Object();
    private final List<ProtectApp> mHabitProtectAppsList = new ArrayList();
    private final ArrayMap<String, Long> mHabitProtectArrayMap = new ArrayMap();
    private final ArrayList<String> mHabitProtectListTopN = new ArrayList();
    private LinkedHashMap<Integer, String> mIdToPkgNameMap = new LinkedHashMap();
    private boolean mIsUsageCountLoaded = false;
    private final ArraySet<HabitProtectListChangeListener> mListeners = new ArraySet();
    private LruCache<String, Long> mLruCache = new LruCache(HwActivityManagerService.SERVICE_ADJ);
    private LinkedHashMap<String, Integer> mPkgNameToIdMap = new LinkedHashMap();
    private ArrayList<ArrayList<Integer>> mTransProMatrix = new ArrayList();
    private LinkedHashMap<String, Integer> mUsageCount = new LinkedHashMap();
    private final AtomicInteger mUserId = new AtomicInteger(0);
    private LinkedHashMap<String, Integer> mUserTrackList = new LinkedHashMap();

    private static class AppCountDescComparator implements Comparator<Entry<String, Integer>>, Serializable {
        private static final long serialVersionUID = 1;

        private AppCountDescComparator() {
        }

        public int compare(Entry<String, Integer> entry, Entry<String, Integer> entry2) {
            return ((Integer) entry2.getValue()).compareTo((Integer) entry.getValue());
        }
    }

    private static class DoubleAscComparator implements Comparator<Entry<Integer, Double>>, Serializable {
        private static final long serialVersionUID = 1;

        private DoubleAscComparator() {
        }

        public int compare(Entry<Integer, Double> entry, Entry<Integer, Double> entry2) {
            return ((Double) entry.getValue()).compareTo((Double) entry2.getValue());
        }
    }

    public interface HabitProtectListChangeListener {
        void onListChanged();
    }

    private static class IntDescComparator implements Comparator<Entry<Integer, Integer>>, Serializable {
        private static final long serialVersionUID = 1;

        private IntDescComparator() {
        }

        public int compare(Entry<Integer, Integer> entry, Entry<Integer, Integer> entry2) {
            return ((Integer) entry2.getValue()).compareTo((Integer) entry.getValue());
        }
    }

    static class PkgInfo {
        private int hitType;
        private int id;

        public PkgInfo(int i, int i2) {
            this.id = i;
            this.hitType = i2;
        }

        public int getId() {
            return this.id;
        }

        public int getHitType() {
            return this.hitType;
        }
    }

    public AwareUserHabitAlgorithm(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mContentResolver = this.mContext.getContentResolver();
        }
    }

    public void deinit() {
        clearHabitProtectList();
        synchronized (this.mHabitLock) {
            this.mIsUsageCountLoaded = false;
            clearData();
        }
    }

    private void initFilterPkg() {
        addDefaultApp();
        addHomeApp();
        addRemovedApp();
    }

    void reloadFilterPkg() {
        clearFilterPkg();
        initFilterPkg();
    }

    void addFilterPkg(String str) {
        synchronized (this.mFilterAppSet) {
            this.mFilterAppSet.add(str);
        }
    }

    private void addDefaultApp() {
        ArraySet habitFilterListFromCMS = IAwareHabitUtils.getHabitFilterListFromCMS();
        if (habitFilterListFromCMS != null && !habitFilterListFromCMS.isEmpty()) {
            synchronized (this.mFilterAppSet) {
                this.mFilterAppSet.addAll(habitFilterListFromCMS);
            }
        }
    }

    private void addRemovedApp() {
        Collection loadRemovedPkg = IAwareHabitUtils.loadRemovedPkg(this.mContentResolver, this.mUserId.get());
        if (loadRemovedPkg != null) {
            synchronized (this.mFilterAppSet) {
                this.mFilterAppSet.addAll(loadRemovedPkg);
            }
        }
    }

    private void addHomeApp() {
        if (this.mContext != null) {
            PackageManager packageManager = this.mContext.getPackageManager();
            if (packageManager != null) {
                List queryIntentActivities = packageManager.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT"), 0);
                if (queryIntentActivities != null) {
                    for (int i = 0; i < queryIntentActivities.size(); i++) {
                        String str = ((ResolveInfo) queryIntentActivities.get(i)).activityInfo.packageName;
                        if (str != null) {
                            addFilterPkg(str);
                        }
                    }
                }
            }
        }
    }

    ArraySet<String> getFilterApp() {
        ArraySet<String> arraySet = new ArraySet();
        synchronized (this.mFilterAppSet) {
            arraySet.addAll(this.mFilterAppSet);
        }
        return arraySet;
    }

    protected void removePkgFromLru(String str) {
        synchronized (this.mLruCache) {
            this.mLruCache.remove(str);
        }
    }

    protected void addPkgToLru(String str, long j) {
        synchronized (this.mLruCache) {
            this.mLruCache.put(str, Long.valueOf(j));
        }
    }

    protected LinkedHashMap<String, Long> getLruCache() {
        LinkedHashMap<String, Long> linkedHashMap;
        synchronized (this.mLruCache) {
            Map snapshot = this.mLruCache.snapshot();
            if (snapshot instanceof LinkedHashMap) {
                linkedHashMap = (LinkedHashMap) snapshot;
            } else {
                linkedHashMap = null;
            }
        }
        return linkedHashMap;
    }

    void clearLruCache() {
        synchronized (this.mLruCache) {
            this.mLruCache.evictAll();
        }
    }

    protected List<String> getAppPkgNamesFromLRU() {
        LinkedHashMap lruCache = getLruCache();
        if (lruCache == null) {
            return null;
        }
        return new ArrayList(lruCache.keySet());
    }

    protected List<String> getForceProtectAppsFromLRU(List<String> list, int i) {
        int i2 = 0;
        if (list == null) {
            return null;
        }
        List appPkgNamesFromLRU = getAppPkgNamesFromLRU();
        if (appPkgNamesFromLRU == null) {
            return null;
        }
        List arrayList = new ArrayList();
        int size = appPkgNamesFromLRU.size() - 1;
        while (size >= 0) {
            String str = (String) appPkgNamesFromLRU.get(size);
            if (i == i2) {
                break;
            }
            int i3;
            if (list.contains(str) || containsFilterPkg(str)) {
                i3 = i2;
            } else {
                arrayList.add(str);
                i3 = i2 + 1;
            }
            size--;
            i2 = i3;
        }
        return arrayList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean isPredictHit(String str, int i) {
        synchronized (this.mUserTrackList) {
            if (this.mUserTrackList.containsKey(str) && ((Integer) this.mUserTrackList.get(str)).intValue() <= i) {
                return true;
            }
        }
    }

    private void clearFilterPkg() {
        synchronized (this.mFilterAppSet) {
            this.mFilterAppSet.clear();
        }
    }

    void removeFilterPkg(String str) {
        synchronized (this.mFilterAppSet) {
            this.mFilterAppSet.remove(str);
        }
    }

    boolean containsFilterPkg(String str) {
        synchronized (this.mFilterAppSet) {
            if (this.mFilterAppSet.contains(str)) {
                return true;
            }
            return false;
        }
    }

    void init() {
        synchronized (this.mHabitLock) {
            this.mIsUsageCountLoaded = true;
            this.mUsageCount.clear();
            loadData();
        }
    }

    void reloadDataInfo() {
        synchronized (this.mHabitLock) {
            clearData();
            loadData();
        }
    }

    private void clearData() {
        this.mPkgNameToIdMap.clear();
        this.mIdToPkgNameMap.clear();
        this.mTransProMatrix.clear();
        this.mUsageCount.clear();
        clearFilterPkg();
    }

    private void loadData() {
        int i = this.mUserId.get();
        IAwareHabitUtils.loadPkgInfo(this.mContentResolver, this.mPkgNameToIdMap, this.mIdToPkgNameMap, this.mUsageCount, i);
        IAwareHabitUtils.loadAppAssociateInfo(this.mContentResolver, this.mPkgNameToIdMap, this.mTransProMatrix, i);
        initFilterPkg();
    }

    void updatePkgNameMap(String str) {
        if (!this.mPkgNameToIdMap.containsKey(str)) {
            int size = this.mPkgNameToIdMap.size();
            this.mPkgNameToIdMap.put(str, Integer.valueOf(size));
            this.mIdToPkgNameMap.put(Integer.valueOf(size), str);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    List<String> getMostFrequentUsedApp(int i, int i2, Set<String> set) {
        if (i <= 0) {
            return null;
        }
        List<String> arrayList = new ArrayList();
        synchronized (this.mHabitLock) {
            if (!this.mIsUsageCountLoaded) {
                AwareLog.i(TAG, "getMostFrequentUsedApp: preload the usage count info");
                IAwareHabitUtils.loadUsageData(this.mContentResolver, this.mUsageCount, this.mUserId.get());
                initFilterPkg();
                this.mIsUsageCountLoaded = true;
            }
            if (this.mUsageCount.isEmpty()) {
                return arrayList;
            }
            List<Entry> arrayList2 = new ArrayList(this.mUsageCount.entrySet());
            Collections.sort(arrayList2, new AppCountDescComparator());
            int i3 = i;
            for (Entry entry : arrayList2) {
                if (i3 > 0 && ((Integer) entry.getValue()).intValue() > i2) {
                    int i4;
                    String str = (String) entry.getKey();
                    if (((set != null ? set.contains(str) : 0) | containsFilterPkg(str)) != 0) {
                        i4 = i3;
                    } else {
                        arrayList.add(str);
                        i4 = i3 - 1;
                    }
                    i3 = i4;
                }
            }
            return arrayList;
        }
    }

    private List<PkgInfo> getTopMove(String str, Set<Integer> set) {
        int i = 0;
        Object arrayList = new ArrayList();
        if (!this.mPkgNameToIdMap.containsKey(str) || ((Integer) this.mPkgNameToIdMap.get(str)).intValue() >= this.mTransProMatrix.size()) {
            return arrayList;
        }
        int intValue = ((Integer) this.mPkgNameToIdMap.get(str)).intValue();
        ArrayList arrayList2 = (ArrayList) this.mTransProMatrix.get(intValue);
        Object arrayList3 = new ArrayList();
        double d = 0.0d;
        for (int i2 = 0; i2 < arrayList2.size(); i2++) {
            if (i2 != intValue) {
                d += (double) ((Integer) arrayList2.get(i2)).intValue();
                arrayList3.add(new SimpleEntry(Integer.valueOf(i2), arrayList2.get(i2)));
            }
        }
        Collections.sort(arrayList3, new IntDescComparator());
        Iterator it = arrayList3.iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            if (((double) ((Integer) entry.getValue()).intValue()) / d < TRANS_PRO_THRESHOLD || i >= 2) {
                break;
            }
            arrayList.add(new PkgInfo(((Integer) entry.getKey()).intValue(), 1));
            set.add(entry.getKey());
            i++;
        }
        return arrayList;
    }

    private List<Integer> getLRUAppList(Map<String, Long> map, long j, String str, Set<Integer> set) {
        List<Integer> arrayList = new ArrayList();
        List appPkgNamesFromLRU = getAppPkgNamesFromLRU();
        if (appPkgNamesFromLRU == null) {
            return arrayList;
        }
        int size;
        if (this.mUsageCount.isEmpty()) {
            size = appPkgNamesFromLRU.size();
        } else {
            size = 2;
        }
        for (int size2 = appPkgNamesFromLRU.size() - 1; size2 >= 0; size2--) {
            String str2 = (String) appPkgNamesFromLRU.get(size2);
            if (!(str2 == null || str2.equals(str) || containsFilterPkg(str2) || set.contains(this.mPkgNameToIdMap.get(str2)) || !this.mPkgNameToIdMap.containsKey(str2) || !map.containsKey(str2))) {
                if ((Math.abs(j - ((Long) map.get(str2)).longValue()) >= 1800000 ? 1 : null) != null || arrayList.size() >= r2) {
                    break;
                }
                arrayList.add(this.mPkgNameToIdMap.get(str2));
            }
        }
        return arrayList;
    }

    private List<PkgInfo> getUsePatternList(String str, Map<String, Long> map, long j, String str2) {
        Object arraySet = new ArraySet();
        Collection topMove = getTopMove(str, arraySet);
        List arrayList = new ArrayList();
        arrayList.addAll(topMove);
        List<Integer> lRUAppList = getLRUAppList(map, j, str2, arraySet);
        Object arrayList2 = new ArrayList();
        for (Integer intValue : lRUAppList) {
            arrayList.add(new PkgInfo(intValue.intValue(), 2));
        }
        int size = this.mPkgNameToIdMap.size();
        int intValue2;
        if (this.mPkgNameToIdMap.containsKey(str2)) {
            intValue2 = ((Integer) this.mPkgNameToIdMap.get(str2)).intValue();
        } else {
            intValue2 = size;
        }
        int i = 0;
        while (i < this.mPkgNameToIdMap.size()) {
            if (!(arraySet.contains(Integer.valueOf(i)) || lRUAppList.contains(Integer.valueOf(i)) || i == r3)) {
                String str3 = (String) this.mIdToPkgNameMap.get(Integer.valueOf(i));
                if (!this.mUsageCount.isEmpty() && this.mUsageCount.containsKey(str3)) {
                    arrayList2.add(new SimpleEntry(Integer.valueOf(i), Double.valueOf(0.0d - ((double) ((Integer) this.mUsageCount.get(str3)).intValue()))));
                } else {
                    long j2 = MIN_THRESHOLD;
                    if (map.containsKey(str3)) {
                        j2 = ((Long) map.get(str3)).longValue();
                    }
                    arrayList2.add(new SimpleEntry(Integer.valueOf(i), Double.valueOf((double) Math.abs(j - j2))));
                }
            }
            i++;
        }
        Collections.sort(arrayList2, new DoubleAscComparator());
        Iterator it = arrayList2.iterator();
        while (it.hasNext()) {
            arrayList.add(new PkgInfo(((Integer) ((Entry) it.next()).getKey()).intValue(), 3));
        }
        return arrayList;
    }

    protected Map<String, String> getUserTrackPredictDumpInfo(String str, Map<String, Long> map, long j, String str2) {
        Map<String, String> map2 = null;
        synchronized (this.mHabitLock) {
            if (!(str == null || str2 == null || map == null)) {
                List<PkgInfo> usePatternList = getUsePatternList(str, map, j, str2);
                LinkedHashMap linkedHashMap = new LinkedHashMap();
                for (PkgInfo pkgInfo : usePatternList) {
                    String str3 = (String) this.mIdToPkgNameMap.get(Integer.valueOf(pkgInfo.getId()));
                    if (!(str3 == null || containsFilterPkg(str3))) {
                        if (1 == pkgInfo.getHitType()) {
                            linkedHashMap.put(str3, TRANS);
                        } else if (2 != pkgInfo.getHitType()) {
                            linkedHashMap.put(str3, NONE_LRU);
                        } else {
                            linkedHashMap.put(str3, LRU);
                        }
                    }
                }
                Object obj = linkedHashMap;
            }
        }
        return map2;
    }

    void triggerUserTrackPredict(String str, Map<String, Long> map, long j, String str2) {
        if (str != null && str2 != null && map != null) {
            synchronized (this.mHabitLock) {
                predict(str, map, j, str2);
            }
        }
    }

    private void predict(String str, Map<String, Long> map, long j, String str2) {
        updatePkgNameMap(str2);
        List<PkgInfo> usePatternList = getUsePatternList(str, map, j, str2);
        int i = 1;
        synchronized (this.mUserTrackList) {
            this.mUserTrackList.clear();
            for (PkgInfo id : usePatternList) {
                int i2;
                String str3 = (String) this.mIdToPkgNameMap.get(Integer.valueOf(id.getId()));
                if (str3 == null || containsFilterPkg(str3)) {
                    i2 = i;
                } else {
                    this.mUserTrackList.put(str3, Integer.valueOf(i));
                    i2 = i + 1;
                }
                i = i2;
            }
        }
    }

    protected Map<String, Integer> getUserTrackList() {
        Map linkedHashMap = new LinkedHashMap();
        synchronized (this.mUserTrackList) {
            linkedHashMap.putAll(this.mUserTrackList);
        }
        return linkedHashMap;
    }

    List<String> getTopN(int i) {
        List<String> arrayList = new ArrayList();
        Iterator it = getUserTrackList().entrySet().iterator();
        while (it.hasNext() && i != 0) {
            arrayList.add(((Entry) it.next()).getKey());
            i--;
        }
        return arrayList;
    }

    protected Map<String, Long> getLongTimePkgsFromLru(long j) {
        LinkedHashMap lruCache = getLruCache();
        if (lruCache == null) {
            return null;
        }
        Map<String, Long> arrayMap = new ArrayMap();
        long elapsedRealtime = SystemClock.elapsedRealtime();
        for (Entry entry : lruCache.entrySet()) {
            Object obj;
            String str = (String) entry.getKey();
            long longValue = ((Long) entry.getValue()).longValue();
            if (elapsedRealtime - longValue < j) {
                obj = 1;
            } else {
                obj = null;
            }
            if (obj == null) {
                arrayMap.put(str, Long.valueOf(longValue));
            }
        }
        return arrayMap;
    }

    protected String getLastPkgNameExcludeLauncher(String str, List<String> list) {
        if (str == null || list == null) {
            return null;
        }
        List appPkgNamesFromLRU = getAppPkgNamesFromLRU();
        if (appPkgNamesFromLRU == null) {
            return null;
        }
        String str2;
        for (int size = appPkgNamesFromLRU.size() - 1; size >= 0; size--) {
            str2 = (String) appPkgNamesFromLRU.get(size);
            if (!list.contains(str2)) {
                if (!str2.equals(str)) {
                    break;
                }
            }
        }
        str2 = null;
        return str2;
    }

    void initHabitProtectList() {
        synchronized (this.mHabitProtectAppsList) {
            this.mHabitProtectAppsList.clear();
            IAwareHabitUtils.loadHabitProtectList(this.mContentResolver, this.mHabitProtectAppsList, this.mUserId.get());
        }
        Collection habitProtectAppList = getHabitProtectAppList(1, 3, null, true);
        synchronized (this.mHabitProtectListTopN) {
            this.mHabitProtectListTopN.clear();
            this.mHabitProtectListTopN.addAll(habitProtectAppList);
        }
    }

    private void clearHabitProtectList() {
        synchronized (this.mHabitProtectListTopN) {
            this.mHabitProtectListTopN.clear();
        }
        synchronized (this.mHabitProtectAppsList) {
            this.mHabitProtectAppsList.clear();
        }
    }

    protected List<String> queryHabitProtectAppList(int i, int i2) {
        int i3 = 0;
        List arrayList = new ArrayList();
        synchronized (this.mHabitProtectListTopN) {
            Iterator it = this.mHabitProtectListTopN.iterator();
            int i4 = 0;
            while (it.hasNext()) {
                int i5;
                String str = (String) it.next();
                int appType = AppTypeRecoManager.getInstance().getAppType(str);
                if (appType == 0 || appType == 311) {
                    if (i4 >= i) {
                        i5 = i3;
                        i3 = i4;
                    } else {
                        arrayList.add(str);
                        int i6 = i3;
                        i3 = i4 + 1;
                        i5 = i6;
                    }
                } else if (appType != 1 && appType != MemoryConstant.MSG_DIRECT_SWAPPINESS) {
                    i5 = i3;
                    i3 = i4;
                } else if (i3 >= i2) {
                    i5 = i3;
                    i3 = i4;
                } else {
                    arrayList.add(str);
                    i5 = i3 + 1;
                    i3 = i4;
                }
                if (i3 == i) {
                    if (i5 == i2) {
                        break;
                    }
                }
                i4 = i3;
                i3 = i5;
            }
        }
        return arrayList;
    }

    protected void foregroundUpdateHabitProtectList(String str) {
        int i;
        int i2 = 1;
        synchronized (this.mHabitProtectAppsList) {
            int i3;
            for (ProtectApp protectApp : this.mHabitProtectAppsList) {
                if (protectApp.getAppPkgName().equals(str)) {
                    if (protectApp.getDeletedTag() != 1) {
                        i3 = 0;
                        i = 0;
                    } else {
                        protectApp.setDeletedTag(0);
                        i3 = 1;
                        i = 0;
                    }
                }
            }
            i3 = 0;
            i = 1;
        }
        if (i != 0) {
            int appType = AppTypeRecoManager.getInstance().getAppType(str);
            if (appType == 0 || appType == 311) {
                i2 = 0;
            } else if (!(appType == 1 || appType == MemoryConstant.MSG_DIRECT_SWAPPINESS)) {
                return;
            }
            synchronized (this.mHabitProtectAppsList) {
                this.mHabitProtectAppsList.add(new ProtectApp(str, i2, 0, 0.0f, this.mUserId.get()));
            }
        }
        if (i != 0 || r6 != 0) {
            synchronized (this.mHabitProtectListTopN) {
                if (!this.mHabitProtectListTopN.contains(str)) {
                    this.mHabitProtectListTopN.add(str);
                }
            }
        }
    }

    protected void backgroundActivityChangedEvent(String str, Long l) {
        int i = 0;
        if (str != null) {
            int i2;
            int appType = AppTypeRecoManager.getInstance().getAppType(str);
            if (appType == 0 || appType == 1 || appType == 311 || appType == MemoryConstant.MSG_DIRECT_SWAPPINESS) {
                synchronized (this.mHabitProtectArrayMap) {
                    if (!this.mHabitProtectArrayMap.containsKey(str)) {
                        i = 1;
                    }
                    this.mHabitProtectArrayMap.put(str, l);
                }
            }
            synchronized (this.mGCMAppsArrayMap) {
                boolean z;
                boolean containsKey = this.mGCMAppsArrayMap.containsKey(str);
                boolean z2;
                if (containsKey) {
                    z2 = containsKey;
                    i2 = i;
                    z = z2;
                } else {
                    containsKey = IAwareHabitUtils.isGCMApp(this.mContext, str);
                    z2 = containsKey;
                    i2 = i | containsKey;
                    z = z2;
                }
                if (z) {
                    this.mGCMAppsArrayMap.put(str, l);
                }
            }
            if (i2 != 0) {
                updateHabitProtectList();
            }
        }
    }

    protected void trainedUpdateHabitProtectList() {
        boolean z = true;
        synchronized (this.mHabitProtectAppsList) {
            this.mHabitProtectAppsList.clear();
            IAwareHabitUtils.loadHabitProtectList(this.mContentResolver, this.mHabitProtectAppsList, this.mUserId.get());
        }
        synchronized (this.mHabitProtectArrayMap) {
            Iterator it = this.mHabitProtectArrayMap.entrySet().iterator();
            boolean z2 = false;
            while (it.hasNext()) {
                boolean z3;
                if (SystemClock.elapsedRealtime() - ((Long) ((Entry) it.next()).getValue()).longValue() <= 86400000) {
                    z3 = z2;
                } else {
                    it.remove();
                    z3 = z2 ? z2 : true;
                }
                z2 = z3;
            }
        }
        synchronized (this.mGCMAppsArrayMap) {
            Iterator it2 = this.mGCMAppsArrayMap.entrySet().iterator();
            boolean z4 = z2;
            while (it2.hasNext()) {
                if (SystemClock.elapsedRealtime() - ((Long) ((Entry) it2.next()).getValue()).longValue() <= 86400000) {
                    z3 = z4;
                } else {
                    it2.remove();
                    z3 = z4 ? z4 : true;
                }
                z4 = z3;
            }
        }
        Collection habitProtectAppList = getHabitProtectAppList(1, 3, null, true);
        synchronized (this.mHabitProtectListTopN) {
            int size = habitProtectAppList.size();
            if (size == this.mHabitProtectListTopN.size()) {
                for (int i = 0; i < size; i++) {
                    String str = (String) this.mHabitProtectListTopN.get(i);
                    String str2 = (String) habitProtectAppList.get(i);
                    if (str != null) {
                        if (!str.equals(str2)) {
                            break;
                        }
                    }
                }
                z = false;
            }
            if (z) {
                this.mHabitProtectListTopN.clear();
                this.mHabitProtectListTopN.addAll(habitProtectAppList);
                AwareLog.d(TAG, "new habitProtectList topN:" + this.mHabitProtectListTopN);
            }
        }
        if (z4) {
            updateHabitProtectList();
        }
    }

    private void updateHabitProtectList() {
        BackgroundThread.getHandler().post(new Runnable() {
            public void run() {
                synchronized (AwareUserHabitAlgorithm.this.mListeners) {
                    if (AwareUserHabitAlgorithm.this.mListeners.isEmpty()) {
                        return;
                    }
                    Iterator it = AwareUserHabitAlgorithm.this.mListeners.iterator();
                    while (it.hasNext()) {
                        ((HabitProtectListChangeListener) it.next()).onListChanged();
                    }
                }
            }
        });
    }

    protected void uninstallUpdateHabitProtectList(String str) {
        Object obj = null;
        synchronized (this.mHabitProtectAppsList) {
            Iterator it;
            for (ProtectApp protectApp : this.mHabitProtectAppsList) {
                int i;
                if (protectApp.getAppPkgName().equals(str)) {
                    if (protectApp.getDeletedTag() != 0) {
                        i = 1;
                    } else {
                        protectApp.setDeletedTag(1);
                        i = 1;
                    }
                }
            }
            Object obj2 = null;
        }
        if (obj2 != null) {
            synchronized (this.mHabitProtectListTopN) {
                if (this.mHabitProtectListTopN.contains(str)) {
                    this.mHabitProtectListTopN.remove(str);
                    List habitProtectAppList = getHabitProtectAppList(1, 3, null, true);
                    if (habitProtectAppList.size() > 0) {
                        for (int i2 = 0; i2 < habitProtectAppList.size(); i2++) {
                            String str2 = (String) habitProtectAppList.get(i2);
                            if (!this.mHabitProtectListTopN.contains(str2)) {
                                this.mHabitProtectListTopN.add(str2);
                                break;
                            }
                        }
                    }
                }
            }
        }
        synchronized (this.mHabitProtectArrayMap) {
            if (this.mHabitProtectArrayMap.containsKey(str)) {
                it = this.mHabitProtectArrayMap.entrySet().iterator();
                while (it.hasNext()) {
                    if (((String) ((Entry) it.next()).getKey()).equals(str)) {
                        it.remove();
                        int i3 = 1;
                        break;
                    }
                }
            }
        }
        synchronized (this.mGCMAppsArrayMap) {
            if (this.mGCMAppsArrayMap.containsKey(str)) {
                it = this.mGCMAppsArrayMap.entrySet().iterator();
                while (it.hasNext()) {
                    if (((String) ((Entry) it.next()).getKey()).equals(str)) {
                        it.remove();
                        if (obj != null) {
                            obj2 = obj;
                        } else {
                            i = 1;
                        }
                    }
                }
                obj2 = obj;
            } else {
                obj2 = obj;
            }
        }
        if (obj2 != null) {
            updateHabitProtectList();
        }
    }

    protected List<String> getForceProtectAppsFromHabitProtect(int i, int i2, Set<String> set) {
        return getHabitProtectAppList(i, i2, set, true);
    }

    private List<String> getHabitProtectAppList(int i, int i2, Set<String> set, boolean z) {
        List arrayList = new ArrayList();
        synchronized (this.mHabitProtectAppsList) {
            int i3 = 0;
            int i4 = 0;
            for (ProtectApp protectApp : this.mHabitProtectAppsList) {
                String appPkgName = protectApp.getAppPkgName();
                if (protectApp.getDeletedTag() != 1) {
                    int i5;
                    if (z) {
                        if ((protectApp.getAvgUsedFrequency() <= 0.5f ? 1 : null) == null && !containsFilterPkg(appPkgName)) {
                            if (set != null) {
                                if (set.contains(appPkgName)) {
                                    continue;
                                }
                            }
                        }
                    }
                    if (protectApp.getAppType() != 0) {
                        if (protectApp.getAppType() != 1) {
                            i5 = i4;
                            i4 = i3;
                        } else if (i4 >= i) {
                            i5 = i4;
                            i4 = i3;
                        } else {
                            i5 = i4 + 1;
                            arrayList.add(appPkgName);
                            i4 = i3;
                        }
                    } else if (i3 >= i2) {
                        i5 = i4;
                        i4 = i3;
                    } else {
                        i5 = i3 + 1;
                        arrayList.add(appPkgName);
                        int i6 = i4;
                        i4 = i5;
                        i5 = i6;
                    }
                    if (i4 == i2) {
                        if (i5 == i) {
                            break;
                        }
                    }
                    i3 = i4;
                    i4 = i5;
                }
            }
        }
        return arrayList;
    }

    public void dumpHabitProtectList(PrintWriter printWriter) {
        if (printWriter != null) {
            printWriter.println("dump user habit protect list.");
            synchronized (this.mHabitProtectListTopN) {
                Iterator it = this.mHabitProtectListTopN.iterator();
                while (it.hasNext()) {
                    printWriter.println((String) it.next());
                }
            }
        }
    }

    protected void registHabitProtectListChangeListener(HabitProtectListChangeListener habitProtectListChangeListener) {
        if (habitProtectListChangeListener != null) {
            synchronized (this.mListeners) {
                this.mListeners.add(habitProtectListChangeListener);
            }
        }
    }

    protected void unregistHabitProtectListChangeListener(HabitProtectListChangeListener habitProtectListChangeListener) {
        if (habitProtectListChangeListener != null) {
            synchronized (this.mListeners) {
                this.mListeners.remove(habitProtectListChangeListener);
            }
        }
    }

    protected List<String> getHabitProtectAppsAll(int i, int i2) {
        return getHabitProtectAppList(i, i2, null, false);
    }

    protected List<String> getHabitProtectList(int i, int i2) {
        List<String> habitProtectAppList = getHabitProtectAppList(i, i2, null, true);
        int i3 = i + i2;
        synchronized (this.mHabitProtectArrayMap) {
            if (habitProtectAppList.size() < i3) {
                for (int i4 = 0; i4 < this.mHabitProtectArrayMap.size(); i4++) {
                    String str = (String) this.mHabitProtectArrayMap.keyAt(i4);
                    if (!habitProtectAppList.contains(str)) {
                        habitProtectAppList.add(str);
                    }
                    if (habitProtectAppList.size() >= i3) {
                        break;
                    }
                }
            }
        }
        return habitProtectAppList;
    }

    protected List<String> getGCMAppsList() {
        List arrayList;
        synchronized (this.mGCMAppsArrayMap) {
            arrayList = new ArrayList(this.mGCMAppsArrayMap.keySet());
        }
        return arrayList;
    }

    void setUserId(int i) {
        this.mUserId.set(i);
    }

    void clearHabitProtectApps() {
        synchronized (this.mGCMAppsArrayMap) {
            this.mGCMAppsArrayMap.clear();
        }
        synchronized (this.mHabitProtectArrayMap) {
            this.mHabitProtectArrayMap.clear();
        }
        updateHabitProtectList();
    }
}
