package com.android.mms.util;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony.MmsSms;
import com.android.mms.MmsConfig;
import com.android.mms.transaction.MmsPermReceiver;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.util.RcsDraftCache;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.util.Log;
import java.util.Collection;
import java.util.HashSet;

public class DraftCache {
    static final String[] DRAFT_PROJECTION = new String[]{"thread_id"};
    private static DraftCache sInstance;
    private final HashSet<OnDraftChangedListener> mChangeListeners = new HashSet(1);
    private final Object mChangeListenersLock = new Object();
    private final Context mContext;
    private HashSet<Long> mDraftSet = new HashSet(4);
    private final Object mDraftSetLock = new Object();
    private RcsDraftCache mHwCust;
    private boolean mSavingDraft;
    private final Object mSavingDraftLock = new Object();

    public interface OnDraftChangedListener {
        void onDraftChanged(long j, boolean z);

        void onDraftChanged(Collection<Long> collection, boolean z);
    }

    private DraftCache(Context context) {
        if (MLog.isLoggable("Mms_app", 3)) {
            log("DraftCache.constructor", new Object[0]);
        }
        this.mContext = context;
        if (RcsCommonConfig.isRCSSwitchOn() && this.mHwCust == null) {
            this.mHwCust = new RcsDraftCache(context);
        }
    }

    public RcsDraftCache getHwCust() {
        return this.mHwCust;
    }

    public void refresh() {
        if (MLog.isLoggable("Mms_app", 3)) {
            log("refresh", new Object[0]);
        }
        Log.logPerformance("DraftCache refresh start");
        rebuildCache();
        if (this.mHwCust != null) {
            this.mHwCust.rebuildGroupCache();
        }
        Log.logPerformance("DraftCache refresh finish");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void rebuildCache() {
        if (MLog.isLoggable("Mms_app", 3)) {
            log("rebuildCache", new Object[0]);
        }
        HashSet<Long> newDraftSet = new HashSet();
        Cursor cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), MmsSms.CONTENT_DRAFT_URI, DRAFT_PROJECTION, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        long threadId = cursor.getLong(0);
                        newDraftSet.add(Long.valueOf(threadId));
                        if (MLog.isLoggable("Mms_app", 3)) {
                            log("rebuildCache: add tid=" + threadId, new Object[0]);
                        }
                        cursor.moveToNext();
                    }
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
        synchronized (this.mDraftSetLock) {
            HashSet<Long> oldDraftSet = this.mDraftSet;
            this.mDraftSet = newDraftSet;
            synchronized (this.mChangeListenersLock) {
                if (this.mChangeListeners.size() < 1) {
                }
            }
        }
    }

    public void setDraftState(long threadId, boolean hasDraft) {
        if (!MmsConfig.isSupportDraftWithoutRecipient() ? threadId <= 0 : threadId < 0) {
            if (changeDraftState(threadId, hasDraft)) {
                MmsPermReceiver.noticeThreadDraftState(threadId, hasDraft);
            }
        }
    }

    public boolean changeDraftState(long threadId, boolean hasDraft) {
        boolean changed;
        synchronized (this.mDraftSetLock) {
            if (hasDraft) {
                changed = this.mDraftSet.add(Long.valueOf(threadId));
            } else {
                changed = this.mDraftSet.remove(Long.valueOf(threadId));
            }
        }
        if (MLog.isLoggable("Mms_app", 3)) {
            log("setDraftState: tid=" + threadId + ", value=" + hasDraft + ", changed=" + changed, new Object[0]);
        }
        if (changed) {
            synchronized (this.mChangeListenersLock) {
                for (OnDraftChangedListener l : this.mChangeListeners) {
                    l.onDraftChanged(threadId, hasDraft);
                }
            }
        }
        return changed;
    }

    public void setDraftState(Collection<Long> mThreadIds, boolean hasDraft) {
        int changed = 0;
        for (Long longValue : mThreadIds) {
            long threadId = longValue.longValue();
            if (MmsConfig.isSupportDraftWithoutRecipient()) {
                if (threadId < 0) {
                    continue;
                }
            } else if (threadId <= 0) {
            }
            synchronized (this.mDraftSetLock) {
                if (hasDraft) {
                    changed |= this.mDraftSet.add(Long.valueOf(threadId));
                } else {
                    changed |= this.mDraftSet.remove(Long.valueOf(threadId));
                }
            }
            if (MLog.isLoggable("Mms_app", 3)) {
                log("setDraftState: tid=" + threadId + ", value=" + hasDraft + ", changed=" + changed, new Object[0]);
            }
            if (MLog.isLoggable("Mms_app", 2)) {
                dump();
            }
        }
        if (changed != 0) {
            synchronized (this.mChangeListenersLock) {
                for (OnDraftChangedListener l : this.mChangeListeners) {
                    l.onDraftChanged((Collection) mThreadIds, hasDraft);
                }
            }
        }
    }

    public boolean hasDraft(long threadId) {
        boolean contains;
        synchronized (this.mDraftSetLock) {
            contains = this.mDraftSet.contains(Long.valueOf(threadId));
        }
        return contains;
    }

    public static void addOnDraftChangedListener(OnDraftChangedListener l) {
        if (sInstance != null) {
            sInstance.addOnDraftChangedListenerInner(l);
        }
    }

    private void addOnDraftChangedListenerInner(OnDraftChangedListener l) {
        if (MLog.isLoggable("Mms_app", 3)) {
            log("addOnDraftChangedListener " + l, new Object[0]);
        }
        synchronized (this.mChangeListenersLock) {
            this.mChangeListeners.add(l);
        }
    }

    public static void removeOnDraftChangedListener(OnDraftChangedListener l) {
        if (sInstance != null) {
            sInstance.removeOnDraftChangedListenerInner(l);
        }
    }

    private void removeOnDraftChangedListenerInner(OnDraftChangedListener l) {
        if (MLog.isLoggable("Mms_app", 3)) {
            log("removeOnDraftChangedListener " + l, new Object[0]);
        }
        synchronized (this.mChangeListenersLock) {
            this.mChangeListeners.remove(l);
        }
    }

    public void setSavingDraft(boolean savingDraft) {
        synchronized (this.mSavingDraftLock) {
            this.mSavingDraft = savingDraft;
        }
    }

    public boolean getSavingDraft() {
        boolean z;
        synchronized (this.mSavingDraftLock) {
            z = this.mSavingDraft;
        }
        return z;
    }

    public static void init(Context context) {
        sInstance = new DraftCache(context);
    }

    public static DraftCache getInstance() {
        return sInstance;
    }

    public void dump() {
        MLog.i("Mms/draft", "dump:");
        synchronized (this.mDraftSetLock) {
            for (Long threadId : this.mDraftSet) {
                MLog.i("Mms/draft", "  tid: " + threadId);
            }
        }
    }

    private void log(String format, Object... args) {
        MLog.d("Mms/draft", "[DraftCache/" + Thread.currentThread().getId() + "] " + String.format(format, args));
    }
}
