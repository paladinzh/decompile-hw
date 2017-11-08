package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.util.ArrayMap;
import android.util.Pools.Pool;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.HwExpandableNotificationRowHelper;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.utils.HwLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class HeadsUpManager implements OnComputeInternalInsetsListener {
    private PhoneStatusBar mBar;
    private Clock mClock;
    private final Context mContext;
    private final int mDefaultSnoozeLengthMs;
    private HashSet<Entry> mEntriesToRemoveAfterExpand = new HashSet();
    private final Pool<HeadsUpEntry> mEntryPool = new Pool<HeadsUpEntry>() {
        private Stack<HeadsUpEntry> mPoolObjects = new Stack();

        public HeadsUpEntry acquire() {
            if (this.mPoolObjects.isEmpty()) {
                return new HeadsUpEntry();
            }
            return (HeadsUpEntry) this.mPoolObjects.pop();
        }

        public boolean release(HeadsUpEntry instance) {
            instance.reset();
            this.mPoolObjects.push(instance);
            return true;
        }
    };
    private final NotificationGroupManager mGroupManager;
    private final Handler mHandler = new Handler();
    private boolean mHasPinnedNotification;
    private HashMap<String, HeadsUpEntry> mHeadsUpEntries = new HashMap();
    private boolean mHeadsUpGoingAway;
    private final int mHeadsUpNotificationDecay;
    private boolean mIsExpanded;
    private boolean mIsObserving;
    private final HashSet<OnHeadsUpChangedListener> mListeners = new HashSet();
    private final int mMinimumDisplayTime;
    private boolean mReleaseOnExpandFinish;
    private ContentObserver mSettingsObserver;
    private int mSnoozeLengthMs;
    private final ArrayMap<String, Long> mSnoozedPackages;
    private final int mStatusBarHeight;
    private final View mStatusBarWindowView;
    private HashSet<String> mSwipedOutKeys = new HashSet();
    private int[] mTmpTwoArray = new int[2];
    private final int mTouchAcceptanceDelay;
    private boolean mTrackingHeadsUp;
    private int mUser;
    private boolean mWaitingOnCollapseWhenGoingAway;

    public interface OnHeadsUpChangedListener {
        void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow);

        void onHeadsUpPinnedModeChanged(boolean z);

        void onHeadsUpStateChanged(Entry entry, boolean z);

        void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow);
    }

    public static class Clock {
        public long currentTimeMillis() {
            return SystemClock.elapsedRealtime();
        }
    }

    public class HeadsUpEntry implements Comparable<HeadsUpEntry> {
        public long earliestRemovaltime;
        public Entry entry;
        public boolean expanded;
        private Runnable mRemoveHeadsUpRunnable;
        public long postTime;
        public boolean remoteInputActive;

        public void setEntry(final Entry entry) {
            this.entry = entry;
            this.postTime = HeadsUpManager.this.mClock.currentTimeMillis() + ((long) HeadsUpManager.this.mTouchAcceptanceDelay);
            this.mRemoveHeadsUpRunnable = new Runnable() {
                public void run() {
                    if (HeadsUpManager.this.mTrackingHeadsUp) {
                        HeadsUpManager.this.mEntriesToRemoveAfterExpand.add(entry);
                    } else {
                        HeadsUpManager.this.removeHeadsUpEntry(entry);
                    }
                }
            };
            updateEntry();
        }

        public void updateEntry() {
            updateEntry(true);
        }

        public void updateEntry(boolean updatePostTime) {
            long currentTime = HeadsUpManager.this.mClock.currentTimeMillis();
            this.earliestRemovaltime = ((long) HeadsUpManager.this.mMinimumDisplayTime) + currentTime;
            if (updatePostTime) {
                this.postTime = Math.max(this.postTime, currentTime);
            }
            removeAutoRemovalCallbacks();
            if (HeadsUpManager.this.mEntriesToRemoveAfterExpand.contains(this.entry)) {
                HeadsUpManager.this.mEntriesToRemoveAfterExpand.remove(this.entry);
            }
            if (!isSticky()) {
                HeadsUpManager.this.mHandler.postDelayed(this.mRemoveHeadsUpRunnable, Math.max((this.postTime + ((long) HeadsUpManager.this.mHeadsUpNotificationDecay)) - currentTime, (long) HeadsUpManager.this.mMinimumDisplayTime));
            }
        }

        private boolean isSticky() {
            if ((this.entry.row.isPinned() && this.expanded) || this.remoteInputActive || HeadsUpManager.this.hasFullScreenIntent(this.entry)) {
                return true;
            }
            return HwExpandableNotificationRowHelper.showHeadsup(this.entry.row);
        }

        public int compareTo(HeadsUpEntry o) {
            int i = 1;
            boolean isPinned = this.entry.row.isPinned();
            boolean otherPinned = o.entry.row.isPinned();
            if (isPinned && !otherPinned) {
                return -1;
            }
            if (!isPinned && otherPinned) {
                return 1;
            }
            boolean selfFullscreen = HeadsUpManager.this.hasFullScreenIntent(this.entry);
            boolean otherFullscreen = HeadsUpManager.this.hasFullScreenIntent(o.entry);
            if (selfFullscreen && !otherFullscreen) {
                return -1;
            }
            if (!selfFullscreen && otherFullscreen) {
                return 1;
            }
            if (this.remoteInputActive && !o.remoteInputActive) {
                return -1;
            }
            if (!this.remoteInputActive && o.remoteInputActive) {
                return 1;
            }
            if (this.postTime >= o.postTime) {
                i = this.postTime == o.postTime ? this.entry.key.compareTo(o.entry.key) : -1;
            }
            return i;
        }

        public void removeAutoRemovalCallbacks() {
            HeadsUpManager.this.mHandler.removeCallbacks(this.mRemoveHeadsUpRunnable);
        }

        public boolean wasShownLongEnough() {
            return this.earliestRemovaltime < HeadsUpManager.this.mClock.currentTimeMillis();
        }

        public void removeAsSoonAsPossible() {
            removeAutoRemovalCallbacks();
            HeadsUpManager.this.mHandler.postDelayed(this.mRemoveHeadsUpRunnable, this.earliestRemovaltime - HeadsUpManager.this.mClock.currentTimeMillis());
        }

        public void reset() {
            removeAutoRemovalCallbacks();
            this.entry = null;
            this.mRemoveHeadsUpRunnable = null;
            this.expanded = false;
            this.remoteInputActive = false;
        }
    }

    public HeadsUpManager(final Context context, View statusBarWindowView, NotificationGroupManager groupManager) {
        this.mContext = context;
        Resources resources = this.mContext.getResources();
        this.mTouchAcceptanceDelay = resources.getInteger(R.integer.touch_acceptance_delay);
        this.mSnoozedPackages = new ArrayMap();
        this.mDefaultSnoozeLengthMs = resources.getInteger(R.integer.heads_up_default_snooze_length_ms);
        this.mSnoozeLengthMs = this.mDefaultSnoozeLengthMs;
        this.mMinimumDisplayTime = resources.getInteger(R.integer.heads_up_notification_minimum_time);
        this.mHeadsUpNotificationDecay = resources.getInteger(R.integer.heads_up_notification_decay);
        this.mClock = new Clock();
        this.mSnoozeLengthMs = Global.getInt(context.getContentResolver(), "heads_up_snooze_length_ms", this.mDefaultSnoozeLengthMs);
        this.mSettingsObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                int packageSnoozeLengthMs = Global.getInt(context.getContentResolver(), "heads_up_snooze_length_ms", -1);
                if (packageSnoozeLengthMs > -1 && packageSnoozeLengthMs != HeadsUpManager.this.mSnoozeLengthMs) {
                    HeadsUpManager.this.mSnoozeLengthMs = packageSnoozeLengthMs;
                }
            }
        };
        context.getContentResolver().registerContentObserver(Global.getUriFor("heads_up_snooze_length_ms"), false, this.mSettingsObserver);
        this.mStatusBarWindowView = statusBarWindowView;
        this.mGroupManager = groupManager;
        this.mStatusBarHeight = resources.getDimensionPixelSize(17104919);
    }

    private void updateTouchableRegionListener() {
        boolean shouldObserve;
        if (this.mHasPinnedNotification || this.mHeadsUpGoingAway) {
            shouldObserve = true;
        } else {
            shouldObserve = this.mWaitingOnCollapseWhenGoingAway;
        }
        if (shouldObserve != this.mIsObserving) {
            if (shouldObserve) {
                this.mStatusBarWindowView.getViewTreeObserver().addOnComputeInternalInsetsListener(this);
                this.mStatusBarWindowView.requestLayout();
            } else {
                this.mStatusBarWindowView.getViewTreeObserver().removeOnComputeInternalInsetsListener(this);
            }
            this.mIsObserving = shouldObserve;
        }
    }

    public void setBar(PhoneStatusBar bar) {
        this.mBar = bar;
    }

    public void addListener(OnHeadsUpChangedListener listener) {
        this.mListeners.add(listener);
    }

    public void showNotification(Entry headsUp) {
        addHeadsUpEntry(headsUp);
        updateNotification(headsUp, true);
        headsUp.setInterruption();
    }

    public void updateNotification(Entry headsUp, boolean alert) {
        headsUp.row.sendAccessibilityEvent(2048);
        if (alert) {
            HeadsUpEntry headsUpEntry = (HeadsUpEntry) this.mHeadsUpEntries.get(headsUp.key);
            if (headsUpEntry != null) {
                headsUpEntry.updateEntry();
                setEntryPinned(headsUpEntry, shouldHeadsUpBecomePinned(headsUp));
            }
        }
    }

    private void addHeadsUpEntry(Entry entry) {
        HwLog.i("HeadsUpManager", "addHeadsUpEntry: " + entry.notification.getKey());
        HeadsUpEntry headsUpEntry = (HeadsUpEntry) this.mEntryPool.acquire();
        headsUpEntry.setEntry(entry);
        this.mHeadsUpEntries.put(entry.key, headsUpEntry);
        entry.row.setHeadsUp(true);
        setEntryPinned(headsUpEntry, shouldHeadsUpBecomePinned(entry));
        for (OnHeadsUpChangedListener listener : this.mListeners) {
            listener.onHeadsUpStateChanged(entry, true);
        }
        entry.row.sendAccessibilityEvent(2048);
    }

    private boolean shouldHeadsUpBecomePinned(Entry entry) {
        return this.mIsExpanded ? hasFullScreenIntent(entry) : true;
    }

    private boolean hasFullScreenIntent(Entry entry) {
        return entry.notification.getNotification().fullScreenIntent != null;
    }

    private void setEntryPinned(HeadsUpEntry headsUpEntry, boolean isPinned) {
        ExpandableNotificationRow row = headsUpEntry.entry.row;
        if (row.isPinned() != isPinned) {
            row.setPinned(isPinned);
            updatePinnedMode();
            for (OnHeadsUpChangedListener listener : this.mListeners) {
                if (isPinned) {
                    listener.onHeadsUpPinned(row);
                } else {
                    listener.onHeadsUpUnPinned(row);
                }
            }
        }
    }

    private void removeHeadsUpEntry(Entry entry) {
        HwLog.i("HeadsUpManager", "removeHeadsUpEntry: " + entry.notification.getKey());
        HeadsUpEntry remove = (HeadsUpEntry) this.mHeadsUpEntries.remove(entry.key);
        entry.row.sendAccessibilityEvent(2048);
        entry.row.setHeadsUp(false);
        setEntryPinned(remove, false);
        for (OnHeadsUpChangedListener listener : this.mListeners) {
            listener.onHeadsUpStateChanged(entry, false);
        }
        this.mEntryPool.release(remove);
    }

    private void updatePinnedMode() {
        boolean hasPinnedNotification = hasPinnedNotificationInternal();
        if (hasPinnedNotification != this.mHasPinnedNotification) {
            this.mHasPinnedNotification = hasPinnedNotification;
            if (this.mHasPinnedNotification) {
                MetricsLogger.count(this.mContext, "note_peek", 1);
            }
            updateTouchableRegionListener();
            for (OnHeadsUpChangedListener listener : this.mListeners) {
                listener.onHeadsUpPinnedModeChanged(hasPinnedNotification);
            }
        }
    }

    public boolean removeNotification(String key, boolean ignoreEarliestRemovalTime) {
        HeadsUpEntry headsUpEntry = (HeadsUpEntry) this.mHeadsUpEntries.get(key);
        boolean isDismissed = false;
        if (!(headsUpEntry == null || headsUpEntry.entry == null || headsUpEntry.entry.row == null)) {
            isDismissed = headsUpEntry.entry.row.isDismissed();
        }
        if (wasShownLongEnough(key) || ignoreEarliestRemovalTime || r1) {
            releaseImmediately(key);
            return true;
        }
        getHeadsUpEntry(key).removeAsSoonAsPossible();
        return false;
    }

    public boolean wasShownLongEnough(String key) {
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(key);
        HeadsUpEntry topEntry = getTopEntry();
        if (this.mSwipedOutKeys.contains(key)) {
            this.mSwipedOutKeys.remove(key);
            return true;
        } else if (headsUpEntry != topEntry) {
            return true;
        } else {
            return headsUpEntry.wasShownLongEnough();
        }
    }

    public boolean isHeadsUp(String key) {
        return this.mHeadsUpEntries.containsKey(key);
    }

    public void releaseAllImmediately() {
        for (String key : new ArrayList(this.mHeadsUpEntries.keySet())) {
            releaseImmediately(key);
        }
    }

    public void releaseImmediately(String key) {
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(key);
        if (headsUpEntry != null) {
            removeHeadsUpEntry(headsUpEntry.entry);
        }
    }

    public boolean isSnoozed(String packageName) {
        Long snoozedUntil = (Long) this.mSnoozedPackages.get(snoozeKey(packageName, this.mUser));
        if (snoozedUntil != null) {
            if (snoozedUntil.longValue() > SystemClock.elapsedRealtime()) {
                return true;
            }
            this.mSnoozedPackages.remove(packageName);
        }
        return false;
    }

    public void snooze() {
        HwLog.i("HeadsUpManager", "snooze");
        for (String key : this.mHeadsUpEntries.keySet()) {
            HeadsUpEntry entry = (HeadsUpEntry) this.mHeadsUpEntries.get(key);
            String packageName = entry.entry.notification.getPackageName();
            if (HwExpandableNotificationRowHelper.canSnooze(entry.entry.row)) {
                HwLog.i("HeadsUpManager", "snooze: " + packageName);
                this.mSnoozedPackages.put(snoozeKey(packageName, this.mUser), Long.valueOf(SystemClock.elapsedRealtime() + ((long) this.mSnoozeLengthMs)));
            }
        }
        this.mReleaseOnExpandFinish = true;
    }

    private static String snoozeKey(String packageName, int user) {
        return user + "," + packageName;
    }

    private HeadsUpEntry getHeadsUpEntry(String key) {
        return (HeadsUpEntry) this.mHeadsUpEntries.get(key);
    }

    public Entry getEntry(String key) {
        return ((HeadsUpEntry) this.mHeadsUpEntries.get(key)).entry;
    }

    public Collection<HeadsUpEntry> getAllEntries() {
        return this.mHeadsUpEntries.values();
    }

    public HeadsUpEntry getTopEntry() {
        if (this.mHeadsUpEntries.isEmpty()) {
            return null;
        }
        HeadsUpEntry topEntry = null;
        for (HeadsUpEntry entry : this.mHeadsUpEntries.values()) {
            if (topEntry == null || entry.compareTo(topEntry) == -1) {
                topEntry = entry;
            }
        }
        return topEntry;
    }

    public boolean shouldSwallowClick(String key) {
        HeadsUpEntry entry = (HeadsUpEntry) this.mHeadsUpEntries.get(key);
        if (entry == null || this.mClock.currentTimeMillis() >= entry.postTime) {
            return false;
        }
        return true;
    }

    public void onComputeInternalInsets(InternalInsetsInfo info) {
        if (!this.mIsExpanded) {
            if (this.mHasPinnedNotification) {
                ExpandableNotificationRow topEntry = getTopEntry().entry.row;
                if (topEntry.isChildInGroup()) {
                    ExpandableNotificationRow groupSummary = this.mGroupManager.getGroupSummary(topEntry.getStatusBarNotification());
                    if (groupSummary != null) {
                        topEntry = groupSummary;
                    }
                }
                topEntry.getLocationOnScreen(this.mTmpTwoArray);
                int minX = this.mTmpTwoArray[0];
                int maxX = this.mTmpTwoArray[0] + topEntry.getWidth();
                int maxY = topEntry.getIntrinsicHeight();
                info.setTouchableInsets(3);
                info.touchableRegion.set(minX, 0, maxX, maxY);
            } else if (this.mHeadsUpGoingAway || this.mWaitingOnCollapseWhenGoingAway) {
                info.setTouchableInsets(3);
                info.touchableRegion.set(0, 0, this.mStatusBarWindowView.getWidth(), this.mStatusBarHeight);
            }
        }
    }

    public void setUser(int user) {
        this.mUser = user;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("HeadsUpManager state:");
        pw.print("  mTouchAcceptanceDelay=");
        pw.println(this.mTouchAcceptanceDelay);
        pw.print("  mSnoozeLengthMs=");
        pw.println(this.mSnoozeLengthMs);
        pw.print("  now=");
        pw.println(SystemClock.elapsedRealtime());
        pw.print("  mUser=");
        pw.println(this.mUser);
        for (HeadsUpEntry entry : this.mHeadsUpEntries.values()) {
            pw.print("  HeadsUpEntry=");
            pw.println(entry.entry);
        }
        int N = this.mSnoozedPackages.size();
        pw.println("  snoozed packages: " + N);
        for (int i = 0; i < N; i++) {
            pw.print("    ");
            pw.print(this.mSnoozedPackages.valueAt(i));
            pw.print(", ");
            pw.println((String) this.mSnoozedPackages.keyAt(i));
        }
    }

    public boolean hasPinnedHeadsUp() {
        return this.mHasPinnedNotification;
    }

    private boolean hasPinnedNotificationInternal() {
        for (String key : this.mHeadsUpEntries.keySet()) {
            if (((HeadsUpEntry) this.mHeadsUpEntries.get(key)).entry.row.isPinned()) {
                return true;
            }
        }
        return false;
    }

    public void addSwipedOutNotification(String key) {
        this.mSwipedOutKeys.add(key);
    }

    public void unpinAll() {
        for (String key : this.mHeadsUpEntries.keySet()) {
            HeadsUpEntry entry = (HeadsUpEntry) this.mHeadsUpEntries.get(key);
            setEntryPinned(entry, false);
            entry.updateEntry(false);
        }
    }

    public void onExpandingFinished() {
        if (this.mReleaseOnExpandFinish) {
            releaseAllImmediately();
            this.mReleaseOnExpandFinish = false;
        } else {
            for (Entry entry : this.mEntriesToRemoveAfterExpand) {
                if (isHeadsUp(entry.key)) {
                    removeHeadsUpEntry(entry);
                }
            }
        }
        this.mEntriesToRemoveAfterExpand.clear();
    }

    public void setTrackingHeadsUp(boolean trackingHeadsUp) {
        this.mTrackingHeadsUp = trackingHeadsUp;
    }

    public boolean isTrackingHeadsUp() {
        return this.mTrackingHeadsUp;
    }

    public void setIsExpanded(boolean isExpanded) {
        if (isExpanded != this.mIsExpanded) {
            this.mIsExpanded = isExpanded;
            if (isExpanded) {
                this.mWaitingOnCollapseWhenGoingAway = false;
                this.mHeadsUpGoingAway = false;
                updateTouchableRegionListener();
            }
        }
    }

    public int getTopHeadsUpPinnedHeight() {
        HeadsUpEntry topEntry = getTopEntry();
        if (topEntry == null || topEntry.entry == null) {
            return 0;
        }
        ExpandableNotificationRow row = topEntry.entry.row;
        if (row.isChildInGroup()) {
            ExpandableNotificationRow groupSummary = this.mGroupManager.getGroupSummary(row.getStatusBarNotification());
            if (groupSummary != null) {
                row = groupSummary;
            }
        }
        return row.getPinnedHeadsUpHeight(true);
    }

    public int compare(Entry a, Entry b) {
        HeadsUpEntry aEntry = getHeadsUpEntry(a.key);
        HeadsUpEntry bEntry = getHeadsUpEntry(b.key);
        if (aEntry != null && bEntry != null) {
            return aEntry.compareTo(bEntry);
        }
        return aEntry == null ? 1 : -1;
    }

    public void setHeadsUpGoingAway(boolean headsUpGoingAway) {
        if (headsUpGoingAway != this.mHeadsUpGoingAway) {
            this.mHeadsUpGoingAway = headsUpGoingAway;
            if (!headsUpGoingAway) {
                waitForStatusBarLayout();
            }
            updateTouchableRegionListener();
        }
    }

    private void waitForStatusBarLayout() {
        this.mWaitingOnCollapseWhenGoingAway = true;
        this.mStatusBarWindowView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (HeadsUpManager.this.mStatusBarWindowView.getHeight() <= HeadsUpManager.this.mStatusBarHeight) {
                    HeadsUpManager.this.mStatusBarWindowView.removeOnLayoutChangeListener(this);
                    HeadsUpManager.this.mWaitingOnCollapseWhenGoingAway = false;
                    HeadsUpManager.this.updateTouchableRegionListener();
                }
            }
        });
    }

    public static void setIsClickedNotification(View child, boolean clicked) {
        child.setTag(R.id.is_clicked_heads_up_tag, clicked ? Boolean.valueOf(true) : null);
    }

    public static boolean isClickedHeadsUpNotification(View child) {
        Boolean clicked = (Boolean) child.getTag(R.id.is_clicked_heads_up_tag);
        return clicked != null ? clicked.booleanValue() : false;
    }

    public void setRemoteInputActive(Entry entry, boolean remoteInputActive) {
        HeadsUpEntry headsUpEntry = (HeadsUpEntry) this.mHeadsUpEntries.get(entry.key);
        if (headsUpEntry != null && headsUpEntry.remoteInputActive != remoteInputActive) {
            headsUpEntry.remoteInputActive = remoteInputActive;
            if (remoteInputActive) {
                headsUpEntry.removeAutoRemovalCallbacks();
            } else {
                headsUpEntry.updateEntry(false);
            }
        }
    }

    public void setExpanded(Entry entry, boolean expanded) {
        HeadsUpEntry headsUpEntry = (HeadsUpEntry) this.mHeadsUpEntries.get(entry.key);
        if (headsUpEntry != null && headsUpEntry.expanded != expanded) {
            headsUpEntry.expanded = expanded;
            if (expanded) {
                headsUpEntry.removeAutoRemovalCallbacks();
            } else {
                headsUpEntry.updateEntry(false);
            }
        }
    }
}
