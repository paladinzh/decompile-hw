package com.android.settingslib.applications;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageStatsObserver.Stub;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.format.Formatter;
import android.trustspace.TrustSpaceManager;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.ArrayUtils;
import com.android.settingslib.R$drawable;
import java.io.File;
import java.text.Collator;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class ApplicationsState {
    public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
        private final Collator sCollator = Collator.getInstance();

        public int compare(AppEntry object1, AppEntry object2) {
            int compareResult = this.sCollator.compare(object1.label, object2.label);
            if (compareResult != 0) {
                return compareResult;
            }
            if (!(object1.info == null || object2.info == null)) {
                compareResult = this.sCollator.compare(object1.info.packageName, object2.info.packageName);
                if (compareResult != 0) {
                    return compareResult;
                }
            }
            return object1.info.uid - object2.info.uid;
        }
    };
    public static final Comparator<AppEntry> EXTERNAL_SIZE_COMPARATOR = new Comparator<AppEntry>() {
        public int compare(AppEntry object1, AppEntry object2) {
            if (object1.externalSize < object2.externalSize) {
                return 1;
            }
            if (object1.externalSize > object2.externalSize) {
                return -1;
            }
            return ApplicationsState.ALPHA_COMPARATOR.compare(object1, object2);
        }
    };
    public static final AppFilter FILTER_ALL_ENABLED = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return entry.info.enabled;
        }
    };
    public static final AppFilter FILTER_CLONE = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            UserManager userManager = (UserManager) entry.appEntryContext.getSystemService("user");
            if (entry.appEntryContext.getSharedPreferences("com.android.settings_appclone", 0).getAll().containsKey(entry.info.packageName)) {
                return userManager.getUserInfo(UserHandle.getUserId(entry.info.uid)).isClonedProfile();
            }
            return false;
        }
    };
    public static final AppFilter FILTER_DISABLED = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return !entry.info.enabled;
        }
    };
    public static final AppFilter FILTER_DOWNLOADED_AND_LAUNCHER = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return (entry.info.flags & 128) != 0 || (entry.info.flags & 1) == 0 || entry.hasLauncherEntry;
        }
    };
    public static final AppFilter FILTER_EVERYTHING = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return true;
        }
    };
    public static final AppFilter FILTER_PERSONAL = new AppFilter() {
        private int mCurrentUser;

        public void init() {
            this.mCurrentUser = ActivityManager.getCurrentUser();
        }

        public boolean filterApp(AppEntry entry) {
            return UserHandle.getUserId(entry.info.uid) == this.mCurrentUser;
        }
    };
    public static final AppFilter FILTER_THIRD_PARTY = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return (entry.info.flags & 128) != 0 || (entry.info.flags & 1) == 0;
        }
    };
    public static final AppFilter FILTER_WITHOUT_DISABLED_UNTIL_USED = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return entry.info.enabledSetting != 4;
        }
    };
    public static final AppFilter FILTER_WITH_DOMAIN_URLS = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return (entry.info.privateFlags & 16) != 0;
        }
    };
    public static final AppFilter FILTER_WORK = new AppFilter() {
        private int mCurrentUser;

        public void init() {
            this.mCurrentUser = ActivityManager.getCurrentUser();
        }

        public boolean filterApp(AppEntry entry) {
            UserManager userManager = (UserManager) entry.appEntryContext.getSystemService("user");
            if (UserHandle.getUserId(entry.info.uid) == this.mCurrentUser || userManager.getUserInfo(UserHandle.getUserId(entry.info.uid)).isClonedProfile()) {
                return false;
            }
            return true;
        }
    };
    public static final Comparator<AppEntry> INTERNAL_SIZE_COMPARATOR = new Comparator<AppEntry>() {
        public int compare(AppEntry object1, AppEntry object2) {
            if (object1.internalSize < object2.internalSize) {
                return 1;
            }
            if (object1.internalSize > object2.internalSize) {
                return -1;
            }
            return ApplicationsState.ALPHA_COMPARATOR.compare(object1, object2);
        }
    };
    static final Pattern REMOVE_DIACRITICALS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    public static final Comparator<AppEntry> SIZE_COMPARATOR = new Comparator<AppEntry>() {
        public int compare(AppEntry object1, AppEntry object2) {
            if (object1.size < object2.size) {
                return 1;
            }
            if (object1.size > object2.size) {
                return -1;
            }
            return ApplicationsState.ALPHA_COMPARATOR.compare(object1, object2);
        }
    };
    static ApplicationsState sInstance;
    static final Object sLock = new Object();
    final ArrayList<Session> mActiveSessions = new ArrayList();
    final int mAdminRetrieveFlags;
    final ArrayList<AppEntry> mAppEntries = new ArrayList();
    List<ApplicationInfo> mApplications = new ArrayList();
    final BackgroundHandler mBackgroundHandler;
    final Context mContext;
    String mCurComputingSizePkg;
    int mCurComputingSizeUserId;
    long mCurId = 1;
    final SparseArray<HashMap<String, AppEntry>> mEntriesMap = new SparseArray();
    boolean mHaveDisabledApps;
    final InterestingConfigChanges mInterestingConfigChanges = new InterestingConfigChanges();
    final IPackageManager mIpm;
    final MainHandler mMainHandler = new MainHandler(Looper.getMainLooper());
    PackageIntentReceiver mPackageIntentReceiver;
    final PackageManager mPm;
    final ArrayList<Session> mRebuildingSessions = new ArrayList();
    boolean mResumed;
    final int mRetrieveFlags;
    final ArrayList<Session> mSessions = new ArrayList();
    boolean mSessionsChanged;
    final HandlerThread mThread;
    final UserManager mUm;

    public interface Callbacks {
        void onAllSizesComputed();

        void onLauncherInfoChanged();

        void onLoadEntriesCompleted();

        void onPackageIconChanged();

        void onPackageListChanged();

        void onPackageSizeChanged(String str);

        void onRebuildComplete(ArrayList<AppEntry> arrayList);

        void onRunningStateChanged(boolean z);
    }

    public interface AppFilter {
        boolean filterApp(AppEntry appEntry);

        void init();
    }

    public static class SizeInfo {
        public long cacheSize;
        public long codeSize;
        public long dataSize;
        public long externalCacheSize;
        public long externalCodeSize;
        public long externalDataSize;
    }

    public static class AppEntry extends SizeInfo implements Comparable<AppEntry> {
        public final File apkFile;
        public Context appEntryContext;
        public long externalSize;
        public String externalSizeStr;
        public Object extraInfo;
        public boolean hasLauncherEntry;
        public Drawable icon;
        public final long id;
        public ApplicationInfo info;
        public long internalSize;
        public String internalSizeStr;
        public String label;
        public boolean mounted;
        public String normalizedLabel;
        public long size = -1;
        public long sizeLoadStart;
        public boolean sizeStale = true;
        public String sizeStr;

        public String getNormalizedLabel() {
            if (this.normalizedLabel != null) {
                return this.normalizedLabel;
            }
            this.normalizedLabel = ApplicationsState.normalize(this.label);
            return this.normalizedLabel;
        }

        AppEntry(Context context, ApplicationInfo info, long id) {
            this.apkFile = new File(info.sourceDir);
            this.id = id;
            this.info = info;
            this.appEntryContext = context;
            ensureLabel(context);
        }

        public void ensureLabel(Context context) {
            if (this.label != null && this.mounted) {
                return;
            }
            if (this.apkFile.exists()) {
                this.mounted = true;
                CharSequence label = this.info.loadLabel(context.getPackageManager());
                this.label = label != null ? label.toString() : this.info.packageName;
                return;
            }
            this.mounted = false;
            this.label = this.info.packageName;
        }

        boolean ensureIconLocked(Context context, PackageManager pm) {
            if (this.icon == null) {
                if (this.apkFile.exists()) {
                    this.icon = getBadgedIcon(pm);
                    return true;
                }
                this.mounted = false;
                this.icon = context.getDrawable(17303323);
            } else if (!this.mounted && this.apkFile.exists()) {
                this.mounted = true;
                this.icon = getBadgedIcon(pm);
                return true;
            }
            return false;
        }

        private Drawable getBadgedIcon(PackageManager pm) {
            if (!TrustSpaceManager.getDefault().isIntentProtectedApp(this.info.packageName) || isCloneApp(this.info.uid)) {
                return pm.getUserBadgedIcon(pm.loadUnbadgedItemIcon(this.info, this.info), new UserHandle(UserHandle.getUserId(this.info.uid)));
            }
            return getTrustSpaceBadgedDrawable(pm.loadUnbadgedItemIcon(this.info, this.info));
        }

        private boolean isCloneApp(int uid) {
            UserInfo userInfo = ((UserManager) this.appEntryContext.getSystemService("user")).getUserInfo(UserHandle.getUserId(uid));
            if (userInfo != null) {
                return userInfo.isClonedProfile();
            }
            return false;
        }

        private Drawable getTrustSpaceBadgedDrawable(Drawable drawable) {
            int badgedWidth = drawable.getIntrinsicWidth();
            int badgedHeight = drawable.getIntrinsicHeight();
            Drawable badgeDrawable = this.appEntryContext.getResources().getDrawable(R$drawable.ic_trustspace_badge, null);
            Bitmap bitmap = Bitmap.createBitmap(badgedWidth, badgedHeight, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, badgedWidth, badgedHeight);
            drawable.draw(canvas);
            badgeDrawable.setBounds(0, 0, badgedWidth, badgedHeight);
            badgeDrawable.draw(canvas);
            return new BitmapDrawable(bitmap);
        }

        public int compareTo(AppEntry o) {
            return 0;
        }
    }

    private class BackgroundHandler extends Handler {
        boolean mRunning;
        final Stub mStatsObserver = new Stub() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
                boolean sizeChanged = false;
                synchronized (ApplicationsState.this.mEntriesMap) {
                    HashMap<String, AppEntry> userMap = (HashMap) ApplicationsState.this.mEntriesMap.get(stats.userHandle);
                    if (userMap == null) {
                        return;
                    }
                    AppEntry entry = (AppEntry) userMap.get(stats.packageName);
                    if (entry != null) {
                        synchronized (entry) {
                            entry.sizeStale = false;
                            entry.sizeLoadStart = 0;
                            long externalCodeSize = stats.externalCodeSize + stats.externalObbSize;
                            long externalDataSize = stats.externalDataSize + stats.externalMediaSize;
                            long newSize = (externalCodeSize + externalDataSize) + ApplicationsState.this.getTotalInternalSize(stats);
                            if (entry.size == newSize && entry.cacheSize == stats.cacheSize) {
                                if (entry.codeSize == stats.codeSize && entry.dataSize == stats.dataSize && entry.externalCodeSize == externalCodeSize && entry.externalDataSize == externalDataSize) {
                                    if (entry.externalCacheSize != stats.externalCacheSize) {
                                    }
                                }
                            }
                            entry.size = newSize;
                            entry.cacheSize = stats.cacheSize;
                            entry.codeSize = stats.codeSize;
                            entry.dataSize = stats.dataSize;
                            entry.externalCodeSize = externalCodeSize;
                            entry.externalDataSize = externalDataSize;
                            entry.externalCacheSize = stats.externalCacheSize;
                            entry.sizeStr = ApplicationsState.this.getSizeStr(entry.size);
                            entry.internalSize = ApplicationsState.this.getTotalInternalSize(stats);
                            entry.internalSizeStr = ApplicationsState.this.getSizeStr(entry.internalSize);
                            entry.externalSize = ApplicationsState.this.getTotalExternalSize(stats);
                            entry.externalSizeStr = ApplicationsState.this.getSizeStr(entry.externalSize);
                            sizeChanged = true;
                        }
                        if (sizeChanged) {
                            ApplicationsState.this.mMainHandler.sendMessage(ApplicationsState.this.mMainHandler.obtainMessage(4, stats.packageName));
                        }
                    }
                    if (ApplicationsState.this.mCurComputingSizePkg != null && ApplicationsState.this.mCurComputingSizePkg.equals(stats.packageName) && ApplicationsState.this.mCurComputingSizeUserId == stats.userHandle) {
                        ApplicationsState.this.mCurComputingSizePkg = null;
                        BackgroundHandler.this.sendEmptyMessage(4);
                    }
                }
            }
        };

        BackgroundHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            Throwable th;
            ArrayList arrayList = null;
            synchronized (ApplicationsState.this.mEntriesMap) {
                try {
                    if (ApplicationsState.this.mRebuildingSessions.size() > 0) {
                        ArrayList<Session> arrayList2 = new ArrayList(ApplicationsState.this.mRebuildingSessions);
                        try {
                            ApplicationsState.this.mRebuildingSessions.clear();
                            arrayList = arrayList2;
                        } catch (Throwable th2) {
                            th = th2;
                            ArrayList<Session> rebuildingSessions = arrayList2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
    }

    public static class CompoundFilter implements AppFilter {
        private final AppFilter mFirstFilter;
        private final AppFilter mSecondFilter;

        public CompoundFilter(AppFilter first, AppFilter second) {
            this.mFirstFilter = first;
            this.mSecondFilter = second;
        }

        public void init() {
            this.mFirstFilter.init();
            this.mSecondFilter.init();
        }

        public boolean filterApp(AppEntry info) {
            return this.mFirstFilter.filterApp(info) ? this.mSecondFilter.filterApp(info) : false;
        }
    }

    class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            ApplicationsState.this.rebuildActiveSessions();
            int i;
            switch (msg.what) {
                case 1:
                    Session s = msg.obj;
                    if (ApplicationsState.this.mActiveSessions.contains(s)) {
                        s.mCallbacks.onRebuildComplete(s.mLastAppList);
                        return;
                    }
                    return;
                case 2:
                    for (i = 0; i < ApplicationsState.this.mActiveSessions.size(); i++) {
                        ((Session) ApplicationsState.this.mActiveSessions.get(i)).mCallbacks.onPackageListChanged();
                    }
                    return;
                case 3:
                    for (i = 0; i < ApplicationsState.this.mActiveSessions.size(); i++) {
                        ((Session) ApplicationsState.this.mActiveSessions.get(i)).mCallbacks.onPackageIconChanged();
                    }
                    return;
                case 4:
                    for (i = 0; i < ApplicationsState.this.mActiveSessions.size(); i++) {
                        ((Session) ApplicationsState.this.mActiveSessions.get(i)).mCallbacks.onPackageSizeChanged((String) msg.obj);
                    }
                    return;
                case 5:
                    for (i = 0; i < ApplicationsState.this.mActiveSessions.size(); i++) {
                        ((Session) ApplicationsState.this.mActiveSessions.get(i)).mCallbacks.onAllSizesComputed();
                    }
                    return;
                case 6:
                    for (i = 0; i < ApplicationsState.this.mActiveSessions.size(); i++) {
                        boolean z;
                        Callbacks callbacks = ((Session) ApplicationsState.this.mActiveSessions.get(i)).mCallbacks;
                        if (msg.arg1 != 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        callbacks.onRunningStateChanged(z);
                    }
                    return;
                case 7:
                    for (i = 0; i < ApplicationsState.this.mActiveSessions.size(); i++) {
                        ((Session) ApplicationsState.this.mActiveSessions.get(i)).mCallbacks.onLauncherInfoChanged();
                    }
                    return;
                case 8:
                    for (i = 0; i < ApplicationsState.this.mActiveSessions.size(); i++) {
                        ((Session) ApplicationsState.this.mActiveSessions.get(i)).mCallbacks.onLoadEntriesCompleted();
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class PackageIntentReceiver extends BroadcastReceiver {
        private PackageIntentReceiver() {
        }

        void registerReceiver() {
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addDataScheme("package");
            ApplicationsState.this.mContext.registerReceiver(this, filter);
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
            sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
            ApplicationsState.this.mContext.registerReceiver(this, sdFilter);
            IntentFilter userFilter = new IntentFilter();
            userFilter.addAction("android.intent.action.USER_ADDED");
            userFilter.addAction("android.intent.action.USER_REMOVED");
            ApplicationsState.this.mContext.registerReceiver(this, userFilter);
        }

        void unregisterReceiver() {
            ApplicationsState.this.mContext.unregisterReceiver(this);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            String pkgName;
            int i;
            if ("android.intent.action.PACKAGE_ADDED".equals(actionStr)) {
                pkgName = intent.getData().getEncodedSchemeSpecificPart();
                for (i = 0; i < ApplicationsState.this.mEntriesMap.size(); i++) {
                    ApplicationsState.this.addPackage(pkgName, ApplicationsState.this.mEntriesMap.keyAt(i));
                }
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(actionStr)) {
                pkgName = intent.getData().getEncodedSchemeSpecificPart();
                for (i = 0; i < ApplicationsState.this.mEntriesMap.size(); i++) {
                    ApplicationsState.this.removePackage(pkgName, ApplicationsState.this.mEntriesMap.keyAt(i));
                }
            } else if ("android.intent.action.PACKAGE_CHANGED".equals(actionStr)) {
                pkgName = intent.getData().getEncodedSchemeSpecificPart();
                for (i = 0; i < ApplicationsState.this.mEntriesMap.size(); i++) {
                    ApplicationsState.this.invalidatePackage(pkgName, ApplicationsState.this.mEntriesMap.keyAt(i));
                }
            } else if ("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(actionStr) || "android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(actionStr)) {
                String[] pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                if (!(pkgList == null || pkgList.length == 0 || !"android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(actionStr))) {
                    for (String pkgName2 : pkgList) {
                        for (i = 0; i < ApplicationsState.this.mEntriesMap.size(); i++) {
                            ApplicationsState.this.invalidatePackage(pkgName2, ApplicationsState.this.mEntriesMap.keyAt(i));
                        }
                    }
                }
            } else if ("android.intent.action.USER_ADDED".equals(actionStr)) {
                ApplicationsState.this.addUser(intent.getIntExtra("android.intent.extra.user_handle", -10000));
            } else if ("android.intent.action.USER_REMOVED".equals(actionStr)) {
                ApplicationsState.this.removeUser(intent.getIntExtra("android.intent.extra.user_handle", -10000));
            }
        }
    }

    public class Session {
        final Callbacks mCallbacks;
        ArrayList<AppEntry> mLastAppList;
        boolean mRebuildAsync;
        Comparator<AppEntry> mRebuildComparator;
        AppFilter mRebuildFilter;
        boolean mRebuildForeground;
        boolean mRebuildRequested;
        ArrayList<AppEntry> mRebuildResult;
        final Object mRebuildSync = new Object();
        boolean mResumed;

        Session(Callbacks callbacks) {
            this.mCallbacks = callbacks;
        }

        public void resume() {
            synchronized (ApplicationsState.this.mEntriesMap) {
                if (!this.mResumed) {
                    this.mResumed = true;
                    ApplicationsState.this.mSessionsChanged = true;
                    ApplicationsState.this.doResumeIfNeededLocked();
                }
            }
        }

        public void pause() {
            synchronized (ApplicationsState.this.mEntriesMap) {
                if (this.mResumed) {
                    this.mResumed = false;
                    ApplicationsState.this.mSessionsChanged = true;
                    ApplicationsState.this.mBackgroundHandler.removeMessages(1, this);
                    ApplicationsState.this.doPauseIfNeededLocked();
                }
            }
        }

        public ArrayList<AppEntry> getAllApps() {
            ArrayList<AppEntry> arrayList;
            synchronized (ApplicationsState.this.mEntriesMap) {
                arrayList = new ArrayList(ApplicationsState.this.mAppEntries);
            }
            return arrayList;
        }

        public ArrayList<AppEntry> rebuild(AppFilter filter, Comparator<AppEntry> comparator) {
            return rebuild(filter, comparator, true);
        }

        public ArrayList<AppEntry> rebuild(AppFilter filter, Comparator<AppEntry> comparator, boolean foreground) {
            ArrayList<AppEntry> arrayList;
            synchronized (this.mRebuildSync) {
                synchronized (ApplicationsState.this.mEntriesMap) {
                    ApplicationsState.this.mRebuildingSessions.add(this);
                    this.mRebuildRequested = true;
                    this.mRebuildAsync = false;
                    this.mRebuildFilter = filter;
                    this.mRebuildComparator = comparator;
                    this.mRebuildForeground = foreground;
                    this.mRebuildResult = null;
                    if (!ApplicationsState.this.mBackgroundHandler.hasMessages(1)) {
                        ApplicationsState.this.mBackgroundHandler.sendMessage(ApplicationsState.this.mBackgroundHandler.obtainMessage(1));
                    }
                }
                long waitend = SystemClock.uptimeMillis() + 250;
                while (this.mRebuildResult == null) {
                    long now = SystemClock.uptimeMillis();
                    if (now >= waitend) {
                        break;
                    }
                    try {
                        this.mRebuildSync.wait(waitend - now);
                    } catch (InterruptedException e) {
                    }
                }
                this.mRebuildAsync = true;
                arrayList = this.mRebuildResult;
            }
            return arrayList;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void handleRebuildList() {
            synchronized (this.mRebuildSync) {
                if (this.mRebuildRequested) {
                    AppFilter filter = this.mRebuildFilter;
                    Comparator<AppEntry> comparator = this.mRebuildComparator;
                    this.mRebuildRequested = false;
                    this.mRebuildFilter = null;
                    this.mRebuildComparator = null;
                    if (this.mRebuildForeground) {
                        Process.setThreadPriority(-2);
                        this.mRebuildForeground = false;
                    }
                } else {
                    return;
                }
            }
            synchronized (this.mRebuildSync) {
                if (!this.mRebuildRequested) {
                    this.mLastAppList = filteredApps;
                    if (!this.mRebuildAsync) {
                        this.mRebuildResult = filteredApps;
                        this.mRebuildSync.notifyAll();
                    } else if (!ApplicationsState.this.mMainHandler.hasMessages(1, this)) {
                        ApplicationsState.this.mMainHandler.sendMessage(ApplicationsState.this.mMainHandler.obtainMessage(1, this));
                    }
                }
            }
            Process.setThreadPriority(10);
        }

        public void release() {
            pause();
            synchronized (ApplicationsState.this.mEntriesMap) {
                ApplicationsState.this.mSessions.remove(this);
            }
        }
    }

    public static class VolumeFilter implements AppFilter {
        private final String mVolumeUuid;

        public VolumeFilter(String volumeUuid) {
            this.mVolumeUuid = volumeUuid;
        }

        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            return Objects.equals(info.info.volumeUuid, this.mVolumeUuid);
        }
    }

    public static ApplicationsState getInstance(Application app) {
        ApplicationsState applicationsState;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new ApplicationsState(app);
            }
            applicationsState = sInstance;
        }
        return applicationsState;
    }

    private ApplicationsState(Application app) {
        this.mContext = app;
        this.mPm = this.mContext.getPackageManager();
        this.mIpm = AppGlobals.getPackageManager();
        this.mUm = (UserManager) app.getSystemService("user");
        for (int userId : this.mUm.getProfileIdsWithDisabled(UserHandle.myUserId())) {
            this.mEntriesMap.put(userId, new HashMap());
        }
        this.mThread = new HandlerThread("ApplicationsState.Loader", 10);
        this.mThread.start();
        this.mBackgroundHandler = new BackgroundHandler(this.mThread.getLooper());
        this.mAdminRetrieveFlags = 41472;
        this.mRetrieveFlags = 33280;
        synchronized (this.mEntriesMap) {
            try {
                this.mEntriesMap.wait(1);
            } catch (InterruptedException e) {
            }
        }
    }

    public Looper getBackgroundLooper() {
        return this.mThread.getLooper();
    }

    public Session newSession(Callbacks callbacks) {
        Session s = new Session(callbacks);
        synchronized (this.mEntriesMap) {
            this.mSessions.add(s);
        }
        return s;
    }

    void doResumeIfNeededLocked() {
        if (!this.mResumed) {
            int i;
            this.mResumed = true;
            if (this.mPackageIntentReceiver == null) {
                this.mPackageIntentReceiver = new PackageIntentReceiver();
                this.mPackageIntentReceiver.registerReceiver();
            }
            this.mApplications = new ArrayList();
            for (UserInfo user : this.mUm.getProfiles(UserHandle.myUserId())) {
                try {
                    if (this.mEntriesMap.indexOfKey(user.id) < 0) {
                        this.mEntriesMap.put(user.id, new HashMap());
                    }
                    if (user.isClonedProfile()) {
                        loadCloneProfileApps(user.id);
                    } else {
                        ParceledListSlice<ApplicationInfo> list = this.mIpm.getInstalledApplications(user.isAdmin() ? this.mAdminRetrieveFlags : this.mRetrieveFlags, user.id);
                        if (list != null) {
                            this.mApplications.addAll(list.getList());
                        }
                    }
                } catch (RemoteException e) {
                }
            }
            if (this.mInterestingConfigChanges.applyNewConfig(this.mContext.getResources())) {
                clearEntries();
            } else {
                for (i = 0; i < this.mAppEntries.size(); i++) {
                    ((AppEntry) this.mAppEntries.get(i)).sizeStale = true;
                }
            }
            this.mHaveDisabledApps = false;
            i = 0;
            while (i < this.mApplications.size()) {
                ApplicationInfo info = (ApplicationInfo) this.mApplications.get(i);
                if (!info.enabled) {
                    if (info.enabledSetting != 3) {
                        this.mApplications.remove(i);
                        i--;
                        i++;
                    } else {
                        this.mHaveDisabledApps = true;
                    }
                }
                AppEntry entry = (AppEntry) ((HashMap) this.mEntriesMap.get(UserHandle.getUserId(info.uid))).get(info.packageName);
                if (entry != null) {
                    entry.info = info;
                }
                i++;
            }
            if (this.mAppEntries.size() != this.mApplications.size()) {
                clearEntries();
            }
            this.mCurComputingSizePkg = null;
            if (!this.mBackgroundHandler.hasMessages(2)) {
                this.mBackgroundHandler.sendEmptyMessage(2);
            }
        }
    }

    private void loadCloneProfileApps(int userId) {
        try {
            Intent launcherIntent = new Intent("android.intent.action.MAIN");
            launcherIntent.addCategory("android.intent.category.LAUNCHER");
            ParceledListSlice<ResolveInfo> parceledList = this.mIpm.queryIntentActivities(launcherIntent, launcherIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 786432, userId);
            Set<String> pkgs = new HashSet();
            for (ResolveInfo ri : parceledList.getList()) {
                if (!pkgs.contains(ri.activityInfo.packageName)) {
                    this.mApplications.add(ri.activityInfo.applicationInfo);
                    pkgs.add(ri.activityInfo.packageName);
                }
            }
        } catch (RemoteException e) {
        }
    }

    private void clearEntries() {
        for (int i = 0; i < this.mEntriesMap.size(); i++) {
            ((HashMap) this.mEntriesMap.valueAt(i)).clear();
        }
        this.mAppEntries.clear();
    }

    public boolean haveDisabledApps() {
        return this.mHaveDisabledApps;
    }

    void doPauseIfNeededLocked() {
        if (this.mResumed) {
            int i = 0;
            while (i < this.mSessions.size()) {
                if (!((Session) this.mSessions.get(i)).mResumed) {
                    i++;
                } else {
                    return;
                }
            }
            doPauseLocked();
        }
    }

    void doPauseLocked() {
        this.mResumed = false;
        if (this.mPackageIntentReceiver != null) {
            this.mPackageIntentReceiver.unregisterReceiver();
            this.mPackageIntentReceiver = null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AppEntry getEntry(String packageName, int userId) {
        synchronized (this.mEntriesMap) {
            AppEntry entry = (AppEntry) ((HashMap) this.mEntriesMap.get(userId)).get(packageName);
            if (entry == null) {
                ApplicationInfo info = getAppInfoLocked(packageName, userId);
                if (info == null) {
                    if (this.mContext.getUserId() != userId) {
                        UserInfo ui = this.mUm.getUserInfo(userId);
                        if (ui != null && ui.isClonedProfile()) {
                            return null;
                        }
                    }
                    try {
                        info = this.mIpm.getApplicationInfo(packageName, 0, userId);
                    } catch (RemoteException e) {
                        Log.w("ApplicationsState", "getEntry couldn't reach PackageManager", e);
                        return null;
                    }
                }
                if (info != null) {
                    entry = getEntryLocked(info);
                }
            }
        }
    }

    private ApplicationInfo getAppInfoLocked(String pkg, int userId) {
        for (int i = 0; i < this.mApplications.size(); i++) {
            ApplicationInfo info = (ApplicationInfo) this.mApplications.get(i);
            if (pkg.equals(info.packageName) && userId == UserHandle.getUserId(info.uid)) {
                return info;
            }
        }
        return null;
    }

    public void ensureIcon(AppEntry entry) {
        if (entry.icon == null) {
            synchronized (entry) {
                entry.ensureIconLocked(this.mContext, this.mPm);
            }
        }
    }

    public void requestSize(String packageName, int userId) {
        synchronized (this.mEntriesMap) {
            if (((AppEntry) ((HashMap) this.mEntriesMap.get(userId)).get(packageName)) != null) {
                this.mPm.getPackageSizeInfoAsUser(packageName, userId, this.mBackgroundHandler.mStatsObserver);
            }
        }
    }

    int indexOfApplicationInfoLocked(String pkgName, int userId) {
        for (int i = this.mApplications.size() - 1; i >= 0; i--) {
            ApplicationInfo appInfo = (ApplicationInfo) this.mApplications.get(i);
            if (appInfo.packageName.equals(pkgName) && UserHandle.getUserId(appInfo.uid) == userId) {
                return i;
            }
        }
        return -1;
    }

    void addPackage(String pkgName, int userId) {
        try {
            synchronized (this.mEntriesMap) {
                if (!this.mResumed) {
                } else if (indexOfApplicationInfoLocked(pkgName, userId) >= 0) {
                } else {
                    ApplicationInfo info = this.mIpm.getApplicationInfo(pkgName, this.mUm.isUserAdmin(userId) ? this.mAdminRetrieveFlags : this.mRetrieveFlags, userId);
                    if (info == null) {
                        return;
                    }
                    if (!info.enabled) {
                        if (info.enabledSetting != 3) {
                            return;
                        }
                        this.mHaveDisabledApps = true;
                    }
                    this.mApplications.add(info);
                    if (!this.mBackgroundHandler.hasMessages(2)) {
                        this.mBackgroundHandler.sendEmptyMessage(2);
                    }
                    if (!this.mMainHandler.hasMessages(2)) {
                        this.mMainHandler.sendEmptyMessage(2);
                    }
                }
            }
        } catch (RemoteException e) {
        }
    }

    public void removePackage(String pkgName, int userId) {
        synchronized (this.mEntriesMap) {
            int idx = indexOfApplicationInfoLocked(pkgName, userId);
            if (idx >= 0) {
                AppEntry entry = (AppEntry) ((HashMap) this.mEntriesMap.get(userId)).get(pkgName);
                if (entry != null) {
                    ((HashMap) this.mEntriesMap.get(userId)).remove(pkgName);
                    this.mAppEntries.remove(entry);
                }
                ApplicationInfo info = (ApplicationInfo) this.mApplications.get(idx);
                this.mApplications.remove(idx);
                if (!info.enabled) {
                    this.mHaveDisabledApps = false;
                    for (int i = 0; i < this.mApplications.size(); i++) {
                        if (!((ApplicationInfo) this.mApplications.get(i)).enabled) {
                            this.mHaveDisabledApps = true;
                            break;
                        }
                    }
                }
                if (!this.mMainHandler.hasMessages(2)) {
                    this.mMainHandler.sendEmptyMessage(2);
                }
            }
        }
    }

    public void invalidatePackage(String pkgName, int userId) {
        removePackage(pkgName, userId);
        addPackage(pkgName, userId);
    }

    private void addUser(int userId) {
        if (ArrayUtils.contains(this.mUm.getProfileIdsWithDisabled(UserHandle.myUserId()), userId)) {
            synchronized (this.mEntriesMap) {
                this.mEntriesMap.put(userId, new HashMap());
                if (this.mResumed) {
                    doPauseLocked();
                    doResumeIfNeededLocked();
                }
                if (!this.mMainHandler.hasMessages(2)) {
                    this.mMainHandler.sendEmptyMessage(2);
                }
            }
        }
    }

    private void removeUser(int userId) {
        synchronized (this.mEntriesMap) {
            HashMap<String, AppEntry> userMap = (HashMap) this.mEntriesMap.get(userId);
            if (userMap != null) {
                for (AppEntry appEntry : userMap.values()) {
                    this.mAppEntries.remove(appEntry);
                    this.mApplications.remove(appEntry.info);
                }
                this.mEntriesMap.remove(userId);
                if (!this.mMainHandler.hasMessages(2)) {
                    this.mMainHandler.sendEmptyMessage(2);
                }
            }
        }
    }

    private AppEntry getEntryLocked(ApplicationInfo info) {
        int userId = UserHandle.getUserId(info.uid);
        AppEntry entry = (AppEntry) ((HashMap) this.mEntriesMap.get(userId)).get(info.packageName);
        if (entry == null) {
            Context context = this.mContext;
            long j = this.mCurId;
            this.mCurId = 1 + j;
            entry = new AppEntry(context, info, j);
            ((HashMap) this.mEntriesMap.get(userId)).put(info.packageName, entry);
            this.mAppEntries.add(entry);
            return entry;
        } else if (entry.info == info) {
            return entry;
        } else {
            entry.info = info;
            return entry;
        }
    }

    private long getTotalInternalSize(PackageStats ps) {
        if (ps != null) {
            return ps.codeSize + ps.dataSize;
        }
        return -2;
    }

    private long getTotalExternalSize(PackageStats ps) {
        if (ps != null) {
            return (((ps.externalCodeSize + ps.externalDataSize) + ps.externalCacheSize) + ps.externalMediaSize) + ps.externalObbSize;
        }
        return -2;
    }

    private String getSizeStr(long size) {
        if (size >= 0) {
            return Formatter.formatFileSize(this.mContext, size);
        }
        return null;
    }

    void rebuildActiveSessions() {
        synchronized (this.mEntriesMap) {
            if (this.mSessionsChanged) {
                this.mActiveSessions.clear();
                for (int i = 0; i < this.mSessions.size(); i++) {
                    Session s = (Session) this.mSessions.get(i);
                    if (s.mResumed) {
                        this.mActiveSessions.add(s);
                    }
                }
                return;
            }
        }
    }

    public static String normalize(String str) {
        return REMOVE_DIACRITICALS_PATTERN.matcher(Normalizer.normalize(str, Form.NFD)).replaceAll("").toLowerCase();
    }
}
