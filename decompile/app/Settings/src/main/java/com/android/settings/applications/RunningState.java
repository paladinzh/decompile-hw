package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseArray;
import com.android.settingslib.Utils;
import com.android.settingslib.applications.InterestingConfigChanges;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RunningState {
    static Object sGlobalLock = new Object();
    static RunningState sInstance;
    final ArrayList<ProcessItem> mAllProcessItems = new ArrayList();
    final ActivityManager mAm;
    final Context mApplicationContext;
    final Comparator<MergedItem> mBackgroundComparator = new Comparator<MergedItem>() {
        public int compare(MergedItem lhs, MergedItem rhs) {
            int i = -1;
            int i2 = 1;
            if (lhs.mUserId != rhs.mUserId) {
                if (lhs.mUserId == RunningState.this.mMyUserId) {
                    return -1;
                }
                if (rhs.mUserId == RunningState.this.mMyUserId) {
                    return 1;
                }
                if (lhs.mUserId >= rhs.mUserId) {
                    i = 1;
                }
                return i;
            } else if (lhs.mProcess == rhs.mProcess) {
                if (lhs.mLabel == rhs.mLabel) {
                    return 0;
                }
                if (lhs.mLabel != null) {
                    i = lhs.mLabel.compareTo(rhs.mLabel);
                }
                return i;
            } else if (lhs.mProcess == null) {
                return -1;
            } else {
                if (rhs.mProcess == null) {
                    return 1;
                }
                boolean rhsBg;
                RunningAppProcessInfo lhsInfo = lhs.mProcess.mRunningProcessInfo;
                RunningAppProcessInfo rhsInfo = rhs.mProcess.mRunningProcessInfo;
                boolean lhsBg = lhsInfo.importance >= 400;
                if (rhsInfo.importance >= 400) {
                    rhsBg = true;
                } else {
                    rhsBg = false;
                }
                if (lhsBg != rhsBg) {
                    if (!lhsBg) {
                        i2 = -1;
                    }
                    return i2;
                }
                boolean rhsA;
                boolean lhsA = (lhsInfo.flags & 4) != 0;
                if ((rhsInfo.flags & 4) != 0) {
                    rhsA = true;
                } else {
                    rhsA = false;
                }
                if (lhsA != rhsA) {
                    if (!lhsA) {
                        i = 1;
                    }
                    return i;
                } else if (lhsInfo.lru != rhsInfo.lru) {
                    if (lhsInfo.lru >= rhsInfo.lru) {
                        i = 1;
                    }
                    return i;
                } else if (lhs.mProcess.mLabel == rhs.mProcess.mLabel) {
                    return 0;
                } else {
                    if (lhs.mProcess.mLabel == null) {
                        return 1;
                    }
                    if (rhs.mProcess.mLabel == null) {
                        return -1;
                    }
                    return lhs.mProcess.mLabel.compareTo(rhs.mProcess.mLabel);
                }
            }
        }
    };
    final BackgroundHandler mBackgroundHandler;
    ArrayList<MergedItem> mBackgroundItems = new ArrayList();
    long mBackgroundProcessMemory;
    final HandlerThread mBackgroundThread;
    long mForegroundProcessMemory;
    final Handler mHandler = new Handler() {
        int mNextUpdate = 0;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    int i;
                    if (msg.arg1 != 0) {
                        i = 2;
                    } else {
                        i = 1;
                    }
                    this.mNextUpdate = i;
                    break;
                case 4:
                    synchronized (RunningState.this.mLock) {
                        if (!RunningState.this.mResumed) {
                            return;
                        }
                    }
                    break;
            }
        }
    };
    boolean mHaveData;
    final boolean mHideManagedProfiles;
    final InterestingConfigChanges mInterestingConfigChanges = new InterestingConfigChanges();
    final ArrayList<ProcessItem> mInterestingProcesses = new ArrayList();
    ArrayList<BaseItem> mItems = new ArrayList();
    final Object mLock = new Object();
    ArrayList<MergedItem> mMergedItems = new ArrayList();
    final int mMyUserId;
    int mNumBackgroundProcesses;
    int mNumForegroundProcesses;
    int mNumServiceProcesses;
    final SparseArray<MergedItem> mOtherUserBackgroundItems = new SparseArray();
    final SparseArray<MergedItem> mOtherUserMergedItems = new SparseArray();
    final PackageManager mPm;
    final ArrayList<ProcessItem> mProcessItems = new ArrayList();
    OnRefreshUiListener mRefreshUiListener;
    boolean mResumed;
    final SparseArray<ProcessItem> mRunningProcesses = new SparseArray();
    int mSequence = 0;
    final ServiceProcessComparator mServiceProcessComparator = new ServiceProcessComparator();
    long mServiceProcessMemory;
    final SparseArray<HashMap<String, ProcessItem>> mServiceProcessesByName = new SparseArray();
    final SparseArray<ProcessItem> mServiceProcessesByPid = new SparseArray();
    final SparseArray<AppProcessInfo> mTmpAppProcesses = new SparseArray();
    final UserManager mUm;
    private final UserManagerBroadcastReceiver mUmBroadcastReceiver = new UserManagerBroadcastReceiver();
    ArrayList<MergedItem> mUserBackgroundItems = new ArrayList();
    boolean mWatchingBackgroundItems;

    interface OnRefreshUiListener {
        void onRefreshUi(int i);
    }

    static class AppProcessInfo {
        boolean hasForegroundServices;
        boolean hasServices;
        final RunningAppProcessInfo info;

        AppProcessInfo(RunningAppProcessInfo _info) {
            this.info = _info;
        }
    }

    final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    RunningState.this.reset();
                    break;
                case 2:
                    synchronized (RunningState.this.mLock) {
                        if (!RunningState.this.mResumed) {
                            return;
                        }
                    }
                    break;
            }
        }
    }

    static class BaseItem {
        long mActiveSince;
        boolean mBackground;
        int mCurSeq;
        String mCurSizeStr;
        String mDescription;
        CharSequence mDisplayLabel;
        final boolean mIsProcess;
        String mLabel;
        boolean mNeedDivider;
        PackageItemInfo mPackageInfo;
        long mSize;
        String mSizeStr;
        final int mUserId;

        public BaseItem(boolean isProcess, int userId) {
            this.mIsProcess = isProcess;
            this.mUserId = userId;
        }

        public Drawable loadIcon(Context context, RunningState state) {
            if (this.mPackageInfo == null) {
                return null;
            }
            return state.mPm.getUserBadgedIcon(this.mPackageInfo.loadUnbadgedIcon(state.mPm), new UserHandle(this.mUserId));
        }
    }

    static class MergedItem extends BaseItem {
        final ArrayList<MergedItem> mChildren = new ArrayList();
        private int mLastNumProcesses = -1;
        private int mLastNumServices = -1;
        final ArrayList<ProcessItem> mOtherProcesses = new ArrayList();
        ProcessItem mProcess;
        final ArrayList<ServiceItem> mServices = new ArrayList();
        UserState mUser;

        MergedItem(int userId) {
            super(false, userId);
        }

        private void setDescription(Context context, int numProcesses, int numServices) {
            if (this.mLastNumProcesses != numProcesses || this.mLastNumServices != numServices) {
                this.mLastNumProcesses = numProcesses;
                this.mLastNumServices = numServices;
                int resid = 2131625719;
                if (numProcesses != 1) {
                    if (numServices != 1) {
                        resid = 2131625722;
                    } else {
                        resid = 2131625721;
                    }
                } else if (numServices != 1) {
                    resid = 2131625720;
                }
                this.mDescription = context.getResources().getString(resid, new Object[]{Integer.valueOf(numProcesses), Integer.valueOf(numServices)});
            }
        }

        boolean update(Context context, boolean background) {
            this.mBackground = background;
            int i;
            if (this.mUser != null) {
                this.mPackageInfo = ((MergedItem) this.mChildren.get(0)).mProcess.mPackageInfo;
                this.mLabel = this.mUser != null ? this.mUser.mLabel : null;
                this.mDisplayLabel = this.mLabel;
                int numProcesses = 0;
                int numServices = 0;
                this.mActiveSince = -1;
                for (i = 0; i < this.mChildren.size(); i++) {
                    MergedItem child = (MergedItem) this.mChildren.get(i);
                    numProcesses += child.mLastNumProcesses;
                    numServices += child.mLastNumServices;
                    if (child.mActiveSince >= 0 && this.mActiveSince < child.mActiveSince) {
                        this.mActiveSince = child.mActiveSince;
                    }
                }
                if (!this.mBackground) {
                    setDescription(context, numProcesses, numServices);
                }
            } else {
                this.mPackageInfo = this.mProcess.mPackageInfo;
                this.mDisplayLabel = this.mProcess.mDisplayLabel;
                this.mLabel = this.mProcess.mLabel;
                if (!this.mBackground) {
                    setDescription(context, (this.mProcess.mPid > 0 ? 1 : 0) + this.mOtherProcesses.size(), this.mServices.size());
                }
                this.mActiveSince = -1;
                for (i = 0; i < this.mServices.size(); i++) {
                    ServiceItem si = (ServiceItem) this.mServices.get(i);
                    if (si.mActiveSince >= 0 && this.mActiveSince < si.mActiveSince) {
                        this.mActiveSince = si.mActiveSince;
                    }
                }
            }
            return false;
        }

        boolean updateSize(Context context) {
            int i;
            if (this.mUser != null) {
                this.mSize = 0;
                for (i = 0; i < this.mChildren.size(); i++) {
                    MergedItem child = (MergedItem) this.mChildren.get(i);
                    child.updateSize(context);
                    this.mSize += child.mSize;
                }
            } else {
                this.mSize = this.mProcess.mSize;
                for (i = 0; i < this.mOtherProcesses.size(); i++) {
                    this.mSize += ((ProcessItem) this.mOtherProcesses.get(i)).mSize;
                }
            }
            String sizeStr = Formatter.formatShortFileSize(context, this.mSize);
            if (sizeStr.equals(this.mSizeStr)) {
                return false;
            }
            this.mSizeStr = sizeStr;
            return false;
        }

        public Drawable loadIcon(Context context, RunningState state) {
            if (this.mUser == null) {
                return super.loadIcon(context, state);
            }
            if (this.mUser.mIcon == null) {
                return context.getDrawable(17302445);
            }
            ConstantState constState = this.mUser.mIcon.getConstantState();
            if (constState == null) {
                return this.mUser.mIcon;
            }
            return constState.newDrawable();
        }
    }

    static class ProcessItem extends BaseItem {
        long mActiveSince;
        ProcessItem mClient;
        final SparseArray<ProcessItem> mDependentProcesses = new SparseArray();
        boolean mInteresting;
        boolean mIsStarted;
        boolean mIsSystem;
        int mLastNumDependentProcesses;
        MergedItem mMergedItem;
        int mPid;
        final String mProcessName;
        RunningAppProcessInfo mRunningProcessInfo;
        int mRunningSeq;
        final HashMap<ComponentName, ServiceItem> mServices = new HashMap();
        final int mUid;

        public ProcessItem(Context context, int uid, String processName) {
            super(true, UserHandle.getUserId(uid));
            this.mDescription = context.getResources().getString(2131625717, new Object[]{processName});
            this.mUid = uid;
            this.mProcessName = processName;
        }

        void ensureLabel(PackageManager pm) {
            if (this.mLabel == null) {
                ApplicationInfo ai;
                try {
                    ai = pm.getApplicationInfo(this.mProcessName, 8192);
                    if (ai.uid == this.mUid) {
                        this.mDisplayLabel = ai.loadLabel(pm);
                        this.mLabel = this.mDisplayLabel.toString();
                        this.mPackageInfo = ai;
                        return;
                    }
                } catch (NameNotFoundException e) {
                }
                String[] pkgs = pm.getPackagesForUid(this.mUid);
                if (pkgs != null) {
                    if (pkgs.length == 1) {
                        try {
                            ai = pm.getApplicationInfo(pkgs[0], 8192);
                            this.mDisplayLabel = ai.loadLabel(pm);
                            this.mLabel = this.mDisplayLabel.toString();
                            this.mPackageInfo = ai;
                            return;
                        } catch (NameNotFoundException e2) {
                        }
                    }
                    for (String name : pkgs) {
                        try {
                            PackageInfo pi = pm.getPackageInfo(name, 0);
                            if (pi.sharedUserLabel != 0) {
                                CharSequence nm = pm.getText(name, pi.sharedUserLabel, pi.applicationInfo);
                                if (nm != null) {
                                    this.mDisplayLabel = nm;
                                    this.mLabel = nm.toString();
                                    this.mPackageInfo = pi.applicationInfo;
                                    return;
                                }
                            } else {
                                continue;
                            }
                        } catch (NameNotFoundException e3) {
                        }
                    }
                    if (this.mServices.size() > 0) {
                        this.mPackageInfo = ((ServiceItem) this.mServices.values().iterator().next()).mServiceInfo.applicationInfo;
                        this.mDisplayLabel = this.mPackageInfo.loadLabel(pm);
                        this.mLabel = this.mDisplayLabel.toString();
                        return;
                    }
                    try {
                        ai = pm.getApplicationInfo(pkgs[0], 8192);
                        this.mDisplayLabel = ai.loadLabel(pm);
                        this.mLabel = this.mDisplayLabel.toString();
                        this.mPackageInfo = ai;
                    } catch (NameNotFoundException e4) {
                    }
                }
            }
        }

        boolean updateService(Context context, RunningServiceInfo service) {
            PackageManager pm = context.getPackageManager();
            boolean changed = false;
            ServiceItem si = (ServiceItem) this.mServices.get(service.service);
            if (si == null) {
                changed = true;
                si = new ServiceItem(this.mUserId);
                si.mRunningService = service;
                try {
                    si.mServiceInfo = ActivityThread.getPackageManager().getServiceInfo(service.service, 8192, UserHandle.getUserId(service.uid));
                    if (si.mServiceInfo == null) {
                        Log.d("RunningService", "getServiceInfo returned null for: " + service.service);
                        return false;
                    }
                } catch (RemoteException e) {
                }
                si.mDisplayLabel = RunningState.makeLabel(pm, si.mRunningService.service.getClassName(), si.mServiceInfo);
                this.mLabel = this.mDisplayLabel != null ? this.mDisplayLabel.toString() : null;
                if (si.mServiceInfo != null) {
                    si.mPackageInfo = si.mServiceInfo.applicationInfo;
                }
                this.mServices.put(service.service, si);
            }
            si.mCurSeq = this.mCurSeq;
            si.mRunningService = service;
            long activeSince = service.restarting == 0 ? service.activeSince : -1;
            if (si.mActiveSince != activeSince) {
                si.mActiveSince = activeSince;
                changed = true;
            }
            if (service.clientPackage == null || service.clientLabel == 0) {
                if (!si.mShownAsStarted) {
                    si.mShownAsStarted = true;
                    changed = true;
                }
                si.mDescription = context.getResources().getString(2131625712);
            } else {
                if (si.mShownAsStarted) {
                    si.mShownAsStarted = false;
                    changed = true;
                }
                try {
                    String label = pm.getResourcesForApplication(service.clientPackage).getString(service.clientLabel);
                    si.mDescription = context.getResources().getString(2131625713, new Object[]{label});
                } catch (NameNotFoundException e2) {
                    si.mDescription = null;
                }
            }
            return changed;
        }

        boolean updateSize(Context context, long pss, int curSeq) {
            this.mSize = 1024 * pss;
            if (this.mCurSeq == curSeq) {
                String sizeStr = Formatter.formatShortFileSize(context, this.mSize);
                if (!sizeStr.equals(this.mSizeStr)) {
                    this.mSizeStr = sizeStr;
                    return false;
                }
            }
            return false;
        }

        boolean buildDependencyChain(Context context, PackageManager pm, int curSeq) {
            int NP = this.mDependentProcesses.size();
            boolean changed = false;
            for (int i = 0; i < NP; i++) {
                ProcessItem proc = (ProcessItem) this.mDependentProcesses.valueAt(i);
                if (proc.mClient != this) {
                    changed = true;
                    proc.mClient = this;
                }
                proc.mCurSeq = curSeq;
                proc.ensureLabel(pm);
                changed |= proc.buildDependencyChain(context, pm, curSeq);
            }
            if (this.mLastNumDependentProcesses == this.mDependentProcesses.size()) {
                return changed;
            }
            this.mLastNumDependentProcesses = this.mDependentProcesses.size();
            return true;
        }

        void addDependentProcesses(ArrayList<BaseItem> dest, ArrayList<ProcessItem> destProc) {
            int NP = this.mDependentProcesses.size();
            for (int i = 0; i < NP; i++) {
                ProcessItem proc = (ProcessItem) this.mDependentProcesses.valueAt(i);
                proc.addDependentProcesses(dest, destProc);
                dest.add(proc);
                if (proc.mPid > 0) {
                    destProc.add(proc);
                }
            }
        }
    }

    static class ServiceItem extends BaseItem {
        MergedItem mMergedItem;
        RunningServiceInfo mRunningService;
        ServiceInfo mServiceInfo;
        boolean mShownAsStarted;

        public ServiceItem(int userId) {
            super(false, userId);
        }
    }

    class ServiceProcessComparator implements Comparator<ProcessItem> {
        ServiceProcessComparator() {
        }

        public int compare(ProcessItem object1, ProcessItem object2) {
            int i = 1;
            int i2 = -1;
            if (object1.mUserId != object2.mUserId) {
                if (object1.mUserId == RunningState.this.mMyUserId) {
                    return -1;
                }
                if (object2.mUserId == RunningState.this.mMyUserId) {
                    return 1;
                }
                if (object1.mUserId >= object2.mUserId) {
                    i2 = 1;
                }
                return i2;
            } else if (object1.mIsStarted != object2.mIsStarted) {
                if (!object1.mIsStarted) {
                    i2 = 1;
                }
                return i2;
            } else if (object1.mIsSystem != object2.mIsSystem) {
                if (!object1.mIsSystem) {
                    i = -1;
                }
                return i;
            } else if (object1.mActiveSince == object2.mActiveSince) {
                return 0;
            } else {
                if (object1.mActiveSince <= object2.mActiveSince) {
                    i2 = 1;
                }
                return i2;
            }
        }
    }

    private final class UserManagerBroadcastReceiver extends BroadcastReceiver {
        private volatile boolean usersChanged;

        private UserManagerBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (RunningState.this.mLock) {
                if (RunningState.this.mResumed) {
                    RunningState.this.mHaveData = false;
                    RunningState.this.mBackgroundHandler.removeMessages(1);
                    RunningState.this.mBackgroundHandler.sendEmptyMessage(1);
                    RunningState.this.mBackgroundHandler.removeMessages(2);
                    RunningState.this.mBackgroundHandler.sendEmptyMessage(2);
                } else {
                    this.usersChanged = true;
                }
            }
        }

        public boolean checkUsersChangedLocked() {
            boolean oldValue = this.usersChanged;
            this.usersChanged = false;
            return oldValue;
        }

        void register(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_STOPPED");
            filter.addAction("android.intent.action.USER_STARTED");
            filter.addAction("android.intent.action.USER_INFO_CHANGED");
            context.registerReceiverAsUser(this, UserHandle.ALL, filter, null, null);
        }
    }

    static class UserState {
        Drawable mIcon;
        UserInfo mInfo;
        String mLabel;

        UserState() {
        }
    }

    static CharSequence makeLabel(PackageManager pm, String className, PackageItemInfo item) {
        if (!(item == null || (item.labelRes == 0 && item.nonLocalizedLabel == null))) {
            CharSequence label = item.loadLabel(pm);
            if (label != null) {
                return label;
            }
        }
        String label2 = className;
        int tail = className.lastIndexOf(46);
        if (tail >= 0) {
            label2 = className.substring(tail + 1, className.length());
        }
        return label2;
    }

    static RunningState getInstance(Context context) {
        RunningState runningState;
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new RunningState(context);
            }
            runningState = sInstance;
        }
        return runningState;
    }

    private RunningState(Context context) {
        boolean z;
        this.mApplicationContext = context.getApplicationContext();
        this.mAm = (ActivityManager) this.mApplicationContext.getSystemService("activity");
        this.mPm = this.mApplicationContext.getPackageManager();
        this.mUm = (UserManager) this.mApplicationContext.getSystemService("user");
        this.mMyUserId = UserHandle.myUserId();
        UserInfo userInfo = this.mUm.getUserInfo(this.mMyUserId);
        if (userInfo == null || !userInfo.canHaveProfile()) {
            z = true;
        } else {
            z = false;
        }
        this.mHideManagedProfiles = z;
        this.mResumed = false;
        this.mBackgroundThread = new HandlerThread("RunningState:Background");
        this.mBackgroundThread.start();
        this.mBackgroundHandler = new BackgroundHandler(this.mBackgroundThread.getLooper());
        this.mUmBroadcastReceiver.register(this.mApplicationContext);
    }

    void resume(OnRefreshUiListener listener) {
        synchronized (this.mLock) {
            this.mResumed = true;
            this.mRefreshUiListener = listener;
            boolean usersChanged = this.mUmBroadcastReceiver.checkUsersChangedLocked();
            boolean configChanged = this.mInterestingConfigChanges.applyNewConfig(this.mApplicationContext.getResources());
            if (usersChanged || configChanged) {
                this.mHaveData = false;
                this.mBackgroundHandler.removeMessages(1);
                this.mBackgroundHandler.removeMessages(2);
                this.mBackgroundHandler.sendEmptyMessage(1);
            }
            if (!this.mBackgroundHandler.hasMessages(2)) {
                this.mBackgroundHandler.sendEmptyMessage(2);
            }
            this.mHandler.sendEmptyMessage(4);
        }
    }

    void updateNow() {
        synchronized (this.mLock) {
            this.mBackgroundHandler.removeMessages(2);
            this.mBackgroundHandler.sendEmptyMessage(2);
        }
    }

    boolean hasData() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mHaveData;
        }
        return z;
    }

    void waitForData() {
        synchronized (this.mLock) {
            while (!this.mHaveData) {
                try {
                    this.mLock.wait(0);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    void pause() {
        synchronized (this.mLock) {
            this.mResumed = false;
            this.mRefreshUiListener = null;
            this.mHandler.removeMessages(4);
        }
    }

    private boolean isInterestingProcess(RunningAppProcessInfo pi) {
        if ((pi.flags & 1) != 0) {
            return true;
        }
        return (pi.flags & 2) == 0 && pi.importance >= 100 && pi.importance < 170 && pi.importanceReasonCode == 0;
    }

    private void reset() {
        this.mServiceProcessesByName.clear();
        this.mServiceProcessesByPid.clear();
        this.mInterestingProcesses.clear();
        this.mRunningProcesses.clear();
        this.mProcessItems.clear();
        this.mAllProcessItems.clear();
    }

    private void addOtherUserItem(Context context, ArrayList<MergedItem> newMergedItems, SparseArray<MergedItem> userItems, MergedItem newItem) {
        boolean first = true;
        MergedItem userItem = (MergedItem) userItems.get(newItem.mUserId);
        if (userItem != null && userItem.mCurSeq == this.mSequence) {
            first = false;
        }
        if (first) {
            UserInfo info = this.mUm.getUserInfo(newItem.mUserId);
            if (info != null) {
                if (!this.mHideManagedProfiles || !info.isManagedProfile()) {
                    if (userItem == null) {
                        userItem = new MergedItem(newItem.mUserId);
                        userItems.put(newItem.mUserId, userItem);
                    } else {
                        userItem.mChildren.clear();
                    }
                    userItem.mCurSeq = this.mSequence;
                    userItem.mUser = new UserState();
                    userItem.mUser.mInfo = info;
                    userItem.mUser.mIcon = Utils.getUserIcon(context, this.mUm, info);
                    userItem.mUser.mLabel = Utils.getUserLabel(context, info);
                    newMergedItems.add(userItem);
                } else {
                    return;
                }
            }
            return;
        }
        userItem.mChildren.add(newItem);
    }

    private boolean update(Context context, ActivityManager am) {
        AppProcessInfo ainfo;
        MergedItem mergedItem;
        MergedItem mergedItem2;
        int NU;
        MergedItem user;
        int bgi;
        PackageManager pm = context.getPackageManager();
        this.mSequence++;
        boolean changed = false;
        List<RunningServiceInfo> services = am.getRunningServices(100);
        int NS = services != null ? services.size() : 0;
        int i = 0;
        while (i < NS) {
            RunningServiceInfo si = (RunningServiceInfo) services.get(i);
            if (!si.started && si.clientLabel == 0) {
                services.remove(i);
                i--;
                NS--;
            } else if ((si.flags & 8) != 0) {
                services.remove(i);
                i--;
                NS--;
            }
            i++;
        }
        List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        int NP = processes != null ? processes.size() : 0;
        this.mTmpAppProcesses.clear();
        for (i = 0; i < NP; i++) {
            RunningAppProcessInfo pi = (RunningAppProcessInfo) processes.get(i);
            this.mTmpAppProcesses.put(pi.pid, new AppProcessInfo(pi));
        }
        for (i = 0; i < NS; i++) {
            si = (RunningServiceInfo) services.get(i);
            if (si.restarting == 0 && si.pid > 0) {
                ainfo = (AppProcessInfo) this.mTmpAppProcesses.get(si.pid);
                if (ainfo != null) {
                    ainfo.hasServices = true;
                    if (si.foreground) {
                        ainfo.hasForegroundServices = true;
                    }
                }
            }
        }
        for (i = 0; i < NS; i++) {
            int changed2;
            si = (RunningServiceInfo) services.get(i);
            if (si.restarting == 0 && si.pid > 0) {
                ainfo = (AppProcessInfo) this.mTmpAppProcesses.get(si.pid);
                if (!(ainfo == null || ainfo.hasForegroundServices || ainfo.info.importance >= 300)) {
                    boolean skip = false;
                    ainfo = (AppProcessInfo) this.mTmpAppProcesses.get(ainfo.info.importanceReasonPid);
                    while (ainfo != null) {
                        if (!ainfo.hasServices) {
                            if (!isInterestingProcess(ainfo.info)) {
                                ainfo = (AppProcessInfo) this.mTmpAppProcesses.get(ainfo.info.importanceReasonPid);
                            }
                        }
                        skip = true;
                        break;
                    }
                    if (skip) {
                    }
                }
            }
            HashMap<String, ProcessItem> procs = (HashMap) this.mServiceProcessesByName.get(si.uid);
            if (procs == null) {
                procs = new HashMap();
                this.mServiceProcessesByName.put(si.uid, procs);
            }
            ProcessItem proc = (ProcessItem) procs.get(si.process);
            if (proc == null) {
                changed2 = true;
                ProcessItem processItem = new ProcessItem(context, si.uid, si.process);
                procs.put(si.process, processItem);
            }
            if (proc.mCurSeq != this.mSequence) {
                int pid = si.restarting == 0 ? si.pid : 0;
                if (pid != proc.mPid) {
                    changed2 = true;
                    if (proc.mPid != pid) {
                        if (proc.mPid != 0) {
                            this.mServiceProcessesByPid.remove(proc.mPid);
                        }
                        if (pid != 0) {
                            this.mServiceProcessesByPid.put(pid, proc);
                        }
                        proc.mPid = pid;
                    }
                }
                proc.mDependentProcesses.clear();
                proc.mCurSeq = this.mSequence;
            }
            changed = changed2 | proc.updateService(context, si);
        }
        for (i = 0; i < NP; i++) {
            pi = (RunningAppProcessInfo) processes.get(i);
            proc = (ProcessItem) this.mServiceProcessesByPid.get(pi.pid);
            if (proc == null) {
                proc = (ProcessItem) this.mRunningProcesses.get(pi.pid);
                if (proc == null) {
                    changed = true;
                    processItem = new ProcessItem(context, pi.uid, pi.processName);
                    processItem.mPid = pi.pid;
                    this.mRunningProcesses.put(pi.pid, processItem);
                }
                proc.mDependentProcesses.clear();
            }
            if (isInterestingProcess(pi)) {
                if (!this.mInterestingProcesses.contains(proc)) {
                    changed = true;
                    this.mInterestingProcesses.add(proc);
                }
                proc.mCurSeq = this.mSequence;
                proc.mInteresting = true;
                proc.ensureLabel(pm);
            } else {
                proc.mInteresting = false;
            }
            proc.mRunningSeq = this.mSequence;
            proc.mRunningProcessInfo = pi;
        }
        int NRP = this.mRunningProcesses.size();
        i = 0;
        while (i < NRP) {
            proc = (ProcessItem) this.mRunningProcesses.valueAt(i);
            if (proc.mRunningSeq == this.mSequence) {
                int clientPid = proc.mRunningProcessInfo.importanceReasonPid;
                if (clientPid != 0) {
                    ProcessItem client = (ProcessItem) this.mServiceProcessesByPid.get(clientPid);
                    if (client == null) {
                        client = (ProcessItem) this.mRunningProcesses.get(clientPid);
                    }
                    if (client != null) {
                        client.mDependentProcesses.put(proc.mPid, proc);
                    }
                } else {
                    proc.mClient = null;
                }
                i++;
            } else {
                changed = true;
                this.mRunningProcesses.remove(this.mRunningProcesses.keyAt(i));
                NRP--;
            }
        }
        int NHP = this.mInterestingProcesses.size();
        i = 0;
        while (i < NHP) {
            proc = (ProcessItem) this.mInterestingProcesses.get(i);
            if (!proc.mInteresting || this.mRunningProcesses.get(proc.mPid) == null) {
                changed = true;
                this.mInterestingProcesses.remove(i);
                i--;
                NHP--;
            }
            i++;
        }
        int NAP = this.mServiceProcessesByPid.size();
        for (i = 0; i < NAP; i++) {
            proc = (ProcessItem) this.mServiceProcessesByPid.valueAt(i);
            if (proc.mCurSeq == this.mSequence) {
                changed |= proc.buildDependencyChain(context, pm, this.mSequence);
            }
        }
        ArrayList uidToDelete = null;
        for (i = 0; i < this.mServiceProcessesByName.size(); i++) {
            procs = (HashMap) this.mServiceProcessesByName.valueAt(i);
            Iterator<ProcessItem> pit = procs.values().iterator();
            while (pit.hasNext()) {
                ProcessItem pi2 = (ProcessItem) pit.next();
                if (pi2.mCurSeq == this.mSequence) {
                    pi2.ensureLabel(pm);
                    if (pi2.mPid == 0) {
                        pi2.mDependentProcesses.clear();
                    }
                    Iterator<ServiceItem> sit = pi2.mServices.values().iterator();
                    while (sit.hasNext()) {
                        if (((ServiceItem) sit.next()).mCurSeq != this.mSequence) {
                            changed = true;
                            sit.remove();
                        }
                    }
                } else {
                    changed = true;
                    pit.remove();
                    if (procs.size() == 0) {
                        if (uidToDelete == null) {
                            uidToDelete = new ArrayList();
                        }
                        uidToDelete.add(Integer.valueOf(this.mServiceProcessesByName.keyAt(i)));
                    }
                    if (pi2.mPid != 0) {
                        this.mServiceProcessesByPid.remove(pi2.mPid);
                    }
                }
            }
        }
        if (uidToDelete != null) {
            for (i = 0; i < uidToDelete.size(); i++) {
                this.mServiceProcessesByName.remove(((Integer) uidToDelete.get(i)).intValue());
            }
        }
        if (changed) {
            ArrayList<ProcessItem> sortedProcesses = new ArrayList();
            for (i = 0; i < this.mServiceProcessesByName.size(); i++) {
                for (ProcessItem pi22 : ((HashMap) this.mServiceProcessesByName.valueAt(i)).values()) {
                    pi22.mIsSystem = false;
                    pi22.mIsStarted = true;
                    pi22.mActiveSince = Long.MAX_VALUE;
                    for (ServiceItem si2 : pi22.mServices.values()) {
                        if (!(si2.mServiceInfo == null || (si2.mServiceInfo.applicationInfo.flags & 1) == 0)) {
                            pi22.mIsSystem = true;
                        }
                        if (!(si2.mRunningService == null || si2.mRunningService.clientLabel == 0)) {
                            pi22.mIsStarted = false;
                            if (pi22.mActiveSince > si2.mRunningService.activeSince) {
                                pi22.mActiveSince = si2.mRunningService.activeSince;
                            }
                        }
                    }
                    sortedProcesses.add(pi22);
                }
            }
            Collections.sort(sortedProcesses, this.mServiceProcessComparator);
            ArrayList<BaseItem> newItems = new ArrayList();
            ArrayList<MergedItem> newMergedItems = new ArrayList();
            this.mProcessItems.clear();
            for (i = 0; i < sortedProcesses.size(); i++) {
                pi22 = (ProcessItem) sortedProcesses.get(i);
                pi22.mNeedDivider = false;
                int firstProc = this.mProcessItems.size();
                pi22.addDependentProcesses(newItems, this.mProcessItems);
                newItems.add(pi22);
                if (pi22.mPid > 0) {
                    this.mProcessItems.add(pi22);
                }
                mergedItem = null;
                boolean haveAllMerged = false;
                boolean needDivider = false;
                for (ServiceItem si22 : pi22.mServices.values()) {
                    si22.mNeedDivider = needDivider;
                    needDivider = true;
                    newItems.add(si22);
                    if (si22.mMergedItem != null) {
                        if (!(mergedItem == null || mergedItem == si22.mMergedItem)) {
                            haveAllMerged = false;
                        }
                        mergedItem = si22.mMergedItem;
                    } else {
                        haveAllMerged = false;
                    }
                }
                if (!(haveAllMerged && mergedItem != null && mergedItem.mServices.size() == pi22.mServices.size())) {
                    mergedItem2 = new MergedItem(pi22.mUserId);
                    for (ServiceItem si222 : pi22.mServices.values()) {
                        mergedItem2.mServices.add(si222);
                        si222.mMergedItem = mergedItem2;
                    }
                    mergedItem2.mProcess = pi22;
                    mergedItem2.mOtherProcesses.clear();
                    for (int mpi = firstProc; mpi < this.mProcessItems.size() - 1; mpi++) {
                        mergedItem2.mOtherProcesses.add((ProcessItem) this.mProcessItems.get(mpi));
                    }
                }
                mergedItem.update(context, false);
                if (mergedItem.mUserId != this.mMyUserId) {
                    addOtherUserItem(context, newMergedItems, this.mOtherUserMergedItems, mergedItem);
                } else {
                    newMergedItems.add(mergedItem);
                }
            }
            NHP = this.mInterestingProcesses.size();
            for (i = 0; i < NHP; i++) {
                proc = (ProcessItem) this.mInterestingProcesses.get(i);
                if (proc.mClient == null && proc.mServices.size() <= 0) {
                    if (proc.mMergedItem == null) {
                        proc.mMergedItem = new MergedItem(proc.mUserId);
                        proc.mMergedItem.mProcess = proc;
                    }
                    proc.mMergedItem.update(context, false);
                    if (proc.mMergedItem.mUserId != this.mMyUserId) {
                        addOtherUserItem(context, newMergedItems, this.mOtherUserMergedItems, proc.mMergedItem);
                    } else {
                        newMergedItems.add(0, proc.mMergedItem);
                    }
                    this.mProcessItems.add(proc);
                }
            }
            NU = this.mOtherUserMergedItems.size();
            for (i = 0; i < NU; i++) {
                user = (MergedItem) this.mOtherUserMergedItems.valueAt(i);
                if (user.mCurSeq == this.mSequence) {
                    user.update(context, false);
                }
            }
            synchronized (this.mLock) {
                this.mItems = newItems;
                this.mMergedItems = newMergedItems;
            }
        }
        this.mAllProcessItems.clear();
        this.mAllProcessItems.addAll(this.mProcessItems);
        int numBackgroundProcesses = 0;
        int numForegroundProcesses = 0;
        int numServiceProcesses = 0;
        NRP = this.mRunningProcesses.size();
        for (i = 0; i < NRP; i++) {
            proc = (ProcessItem) this.mRunningProcesses.valueAt(i);
            if (proc.mCurSeq == this.mSequence) {
                numServiceProcesses++;
            } else if (proc.mRunningProcessInfo.importance >= 400) {
                numBackgroundProcesses++;
                this.mAllProcessItems.add(proc);
            } else if (proc.mRunningProcessInfo.importance <= 200) {
                numForegroundProcesses++;
                this.mAllProcessItems.add(proc);
            } else {
                Log.i("RunningState", "Unknown non-service process: " + proc.mProcessName + " #" + proc.mPid);
            }
        }
        long backgroundProcessMemory = 0;
        long foregroundProcessMemory = 0;
        long serviceProcessMemory = 0;
        ArrayList<MergedItem> arrayList = null;
        ArrayList<MergedItem> newUserBackgroundItems = null;
        int i2 = 0;
        int numProc = this.mAllProcessItems.size();
        int[] pids = new int[numProc];
        for (i = 0; i < numProc; i++) {
            pids[i] = ((ProcessItem) this.mAllProcessItems.get(i)).mPid;
        }
        long[] pss = ActivityManagerNative.getDefault().getProcessPss(pids);
        int bgIndex = 0;
        i = 0;
        ArrayList<MergedItem> newBackgroundItems = null;
        while (true) {
            if (i >= pids.length) {
                break;
            }
            ArrayList<MergedItem> arrayList2;
            proc = (ProcessItem) this.mAllProcessItems.get(i);
            changed |= proc.updateSize(context, pss[i], this.mSequence);
            if (proc.mCurSeq == this.mSequence) {
                serviceProcessMemory += proc.mSize;
                arrayList = newBackgroundItems;
            } else if (proc.mRunningProcessInfo.importance >= 400) {
                backgroundProcessMemory += proc.mSize;
                if (newBackgroundItems != null) {
                    mergedItem2 = new MergedItem(proc.mUserId);
                    proc.mMergedItem = mergedItem2;
                    proc.mMergedItem.mProcess = proc;
                    i2 |= mergedItem2.mUserId != this.mMyUserId ? 1 : 0;
                    newBackgroundItems.add(mergedItem2);
                    arrayList = newBackgroundItems;
                } else if (bgIndex >= this.mBackgroundItems.size() || ((MergedItem) this.mBackgroundItems.get(bgIndex)).mProcess != proc) {
                    arrayList2 = new ArrayList(numBackgroundProcesses);
                    for (bgi = 0; bgi < bgIndex; bgi++) {
                        mergedItem = (MergedItem) this.mBackgroundItems.get(bgi);
                        i2 |= mergedItem.mUserId != this.mMyUserId ? 1 : 0;
                        arrayList2.add(mergedItem);
                    }
                    mergedItem2 = new MergedItem(proc.mUserId);
                    proc.mMergedItem = mergedItem2;
                    proc.mMergedItem.mProcess = proc;
                    i2 |= mergedItem2.mUserId != this.mMyUserId ? 1 : 0;
                    arrayList2.add(mergedItem2);
                } else {
                    try {
                        mergedItem = (MergedItem) this.mBackgroundItems.get(bgIndex);
                        arrayList = newBackgroundItems;
                    } catch (RemoteException e) {
                        arrayList = newBackgroundItems;
                    }
                }
                try {
                    mergedItem.update(context, true);
                    mergedItem.updateSize(context);
                    bgIndex++;
                } catch (RemoteException e2) {
                }
            } else if (proc.mRunningProcessInfo.importance <= 200) {
                foregroundProcessMemory += proc.mSize;
                arrayList = newBackgroundItems;
            } else {
                arrayList = newBackgroundItems;
            }
            i++;
            newBackgroundItems = arrayList;
        }
        arrayList = newBackgroundItems;
        if (arrayList == null && this.mBackgroundItems.size() > numBackgroundProcesses) {
            arrayList2 = new ArrayList(numBackgroundProcesses);
            for (bgi = 0; bgi < numBackgroundProcesses; bgi++) {
                mergedItem = (MergedItem) this.mBackgroundItems.get(bgi);
                i2 |= mergedItem.mUserId != this.mMyUserId ? 1 : 0;
                arrayList2.add(mergedItem);
            }
        }
        if (arrayList != null) {
            if (i2 == 0) {
                newUserBackgroundItems = arrayList;
            } else {
                newUserBackgroundItems = new ArrayList();
                int NB = arrayList.size();
                for (i = 0; i < NB; i++) {
                    mergedItem = (MergedItem) arrayList.get(i);
                    if (mergedItem.mUserId != this.mMyUserId) {
                        addOtherUserItem(context, newUserBackgroundItems, this.mOtherUserBackgroundItems, mergedItem);
                    } else {
                        newUserBackgroundItems.add(mergedItem);
                    }
                }
                NU = this.mOtherUserBackgroundItems.size();
                for (i = 0; i < NU; i++) {
                    user = (MergedItem) this.mOtherUserBackgroundItems.valueAt(i);
                    if (user.mCurSeq == this.mSequence) {
                        user.update(context, true);
                        user.updateSize(context);
                    }
                }
            }
        }
        for (i = 0; i < this.mMergedItems.size(); i++) {
            ((MergedItem) this.mMergedItems.get(i)).updateSize(context);
        }
        synchronized (this.mLock) {
            this.mNumBackgroundProcesses = numBackgroundProcesses;
            this.mNumForegroundProcesses = numForegroundProcesses;
            this.mNumServiceProcesses = numServiceProcesses;
            this.mBackgroundProcessMemory = backgroundProcessMemory;
            this.mForegroundProcessMemory = foregroundProcessMemory;
            this.mServiceProcessMemory = serviceProcessMemory;
            if (arrayList != null) {
                this.mBackgroundItems = arrayList;
                this.mUserBackgroundItems = newUserBackgroundItems;
                if (this.mWatchingBackgroundItems) {
                    changed = true;
                }
            }
            if (!this.mHaveData) {
                this.mHaveData = true;
                this.mLock.notifyAll();
            }
        }
        return changed;
    }

    void setWatchingBackgroundItems(boolean watching) {
        synchronized (this.mLock) {
            this.mWatchingBackgroundItems = watching;
        }
    }

    ArrayList<MergedItem> getCurrentMergedItems() {
        ArrayList<MergedItem> arrayList;
        synchronized (this.mLock) {
            arrayList = this.mMergedItems;
        }
        return arrayList;
    }

    ArrayList<MergedItem> getCurrentBackgroundItems() {
        ArrayList<MergedItem> arrayList;
        synchronized (this.mLock) {
            arrayList = this.mUserBackgroundItems;
        }
        return arrayList;
    }
}
