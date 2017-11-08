package com.android.rcs.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.android.mms.MmsConfig;
import com.android.mms.util.DraftCache.OnDraftChangedListener;
import com.android.rcs.RcsCommonConfig;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.util.Collection;
import java.util.HashSet;

public class RcsDraftCache {
    public static final Uri sQueryDraftUri = Uri.parse("content://rcsim/get_draft_group_thread_id");
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private final HashSet<OnDraftChangedListener> mChangeListeners = new HashSet(1);
    private final Object mChangeListenersLock = new Object();
    private Context mContext;
    public HashSet<Long> mDraftGroupSet = new HashSet(4);
    private final Object mDraftSetLock = new Object();

    public RcsDraftCache(Context context) {
        this.mContext = context;
    }

    private void addGroupDraftThreadId(HashSet<Long> newDraftSet) {
        Cursor cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), sQueryDraftUri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        newDraftSet.add(Long.valueOf(cursor.getLong(0)));
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void rebuildGroupCache() {
        if (this.isRcsOn) {
            MLog.d("RcsDraftCache", "rebuildGroupCache");
            HashSet<Long> newDraftSet = new HashSet();
            addGroupDraftThreadId(newDraftSet);
            synchronized (this.mDraftSetLock) {
                HashSet<Long> oldDraftSet = this.mDraftGroupSet;
                this.mDraftGroupSet = newDraftSet;
                synchronized (this.mChangeListenersLock) {
                    if (this.mChangeListeners.size() < 1) {
                    }
                }
            }
        }
    }

    public void setDraftGroupState(long threadId, boolean hasDraft) {
        if (this.isRcsOn && threadId > 0) {
            boolean changed;
            synchronized (this.mDraftSetLock) {
                if (hasDraft) {
                    changed = this.mDraftGroupSet.add(Long.valueOf(threadId));
                } else {
                    changed = this.mDraftGroupSet.remove(Long.valueOf(threadId));
                }
            }
            MLog.d("RcsDraftCache", "setDraftGroupState: tid=" + threadId + ", value=" + hasDraft + ", changed=" + changed);
            if (changed) {
                synchronized (this.mChangeListenersLock) {
                    for (OnDraftChangedListener l : this.mChangeListeners) {
                        l.onDraftChanged(threadId, hasDraft);
                    }
                }
            }
        }
    }

    public void setRcsGroupDraftGroupState(Collection<Long> mThreadIds, boolean hasDraft) {
        if (this.isRcsOn) {
            boolean changed = false;
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
                        changed = this.mDraftGroupSet.add(Long.valueOf(threadId));
                    } else {
                        changed = this.mDraftGroupSet.remove(Long.valueOf(threadId));
                    }
                }
                MLog.d("RcsDraftCache", "setDraftGroupState: tid=" + threadId + ", value=" + hasDraft + ", changed=" + changed);
            }
            if (changed) {
                synchronized (this.mChangeListenersLock) {
                    for (OnDraftChangedListener l : this.mChangeListeners) {
                        l.onDraftChanged((Collection) mThreadIds, hasDraft);
                    }
                }
            }
        }
    }

    public boolean hasGroupDraft(long threadId) {
        if (!this.isRcsOn) {
            return false;
        }
        boolean contains;
        synchronized (this.mDraftSetLock) {
            contains = this.mDraftGroupSet.contains(Long.valueOf(threadId));
        }
        return contains;
    }
}
