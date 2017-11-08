package com.android.systemui.statusbar;

import android.app.Notification;
import android.app.Notification.Builder;
import android.content.Context;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService.Ranking;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.view.View;
import android.widget.RemoteViews;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.utils.SystemUiUtil;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class NotificationData {
    public final ArrayMap<String, Entry> mEntries = new ArrayMap();
    private final Environment mEnvironment;
    private NotificationGroupManager mGroupManager;
    private HeadsUpManager mHeadsUpManager;
    private final Comparator<Entry> mRankingComparator = new Comparator<Entry>() {
        private final Ranking mRankingA = new Ranking();
        private final Ranking mRankingB = new Ranking();

        public int compare(Entry a, Entry b) {
            boolean -wrap0;
            boolean -wrap02;
            StatusBarNotification na = a.notification;
            StatusBarNotification nb = b.notification;
            int aImportance = 3;
            int bImportance = 3;
            boolean aOngoing = na.isOngoing();
            boolean bOngoing = nb.isOngoing();
            if (NotificationData.this.mRankingMap != null) {
                NotificationData.this.mRankingMap.getRanking(a.key, this.mRankingA);
                NotificationData.this.mRankingMap.getRanking(b.key, this.mRankingB);
                aImportance = this.mRankingA.getImportance();
                bImportance = this.mRankingB.getImportance();
            }
            if (aImportance >= 5) {
                -wrap0 = NotificationData.isSystemNotification(na);
            } else {
                -wrap0 = false;
            }
            if (bImportance >= 5) {
                -wrap02 = NotificationData.isSystemNotification(nb);
            } else {
                -wrap02 = false;
            }
            boolean aMarketPlace = SystemUiUtil.isMarketPlaceSbn(na);
            boolean bMarketPlace = SystemUiUtil.isMarketPlaceSbn(nb);
            boolean isHeadsUp = a.row.isHeadsUp();
            if (isHeadsUp != b.row.isHeadsUp()) {
                int i;
                if (isHeadsUp) {
                    i = -1;
                } else {
                    i = 1;
                }
                return i;
            } else if (isHeadsUp) {
                return NotificationData.this.mHeadsUpManager.compare(a, b);
            } else {
                if (aMarketPlace != bMarketPlace) {
                    return aMarketPlace ? -1 : 1;
                } else if (-wrap0 != -wrap02) {
                    return -wrap0 ? -1 : 1;
                } else if (aOngoing == bOngoing) {
                    return Long.compare(b.createTime, a.createTime);
                } else {
                    return aOngoing ? -1 : 1;
                }
            }
        }
    };
    private RankingMap mRankingMap;
    private final ArrayList<Entry> mSortedAndFiltered = new ArrayList();
    private final Ranking mTmpRanking = new Ranking();

    public interface Environment {
        NotificationGroupManager getGroupManager();

        boolean isDeviceProvisioned();

        boolean isNotificationForCurrentProfiles(StatusBarNotification statusBarNotification);

        boolean onSecureLockScreen();

        boolean shouldHideNotifications(int i);

        boolean shouldHideNotifications(String str);
    }

    public static final class Entry {
        public boolean autoRedacted;
        public RemoteViews cachedBigContentView;
        public RemoteViews cachedContentView;
        public RemoteViews cachedHeadsUpContentView;
        public RemoteViews cachedPublicContentView;
        public long createTime = System.currentTimeMillis();
        public StatusBarIconView icon;
        private boolean interruption;
        public String key;
        private long lastFullScreenIntentLaunchTime = -2000;
        public boolean legacy;
        public StatusBarNotification notification;
        public CharSequence remoteInputText;
        public ExpandableNotificationRow row;
        public int targetSdk;

        public Entry(StatusBarNotification n, StatusBarIconView ic) {
            this.key = n.getKey();
            this.notification = n;
            this.icon = ic;
        }

        public void setInterruption() {
            this.interruption = true;
        }

        public boolean hasInterrupted() {
            return this.interruption;
        }

        public void reset() {
            this.autoRedacted = false;
            this.legacy = false;
            this.lastFullScreenIntentLaunchTime = -2000;
            if (this.row != null) {
                this.row.reset();
            }
        }

        public View getContentView() {
            return this.row.getPrivateLayout().getContractedChild();
        }

        public View getExpandedContentView() {
            return this.row.getPrivateLayout().getExpandedChild();
        }

        public View getHeadsUpContentView() {
            return this.row.getPrivateLayout().getHeadsUpChild();
        }

        public View getPublicContentView() {
            return this.row.getPublicLayout().getContractedChild();
        }

        public boolean cacheContentViews(Context ctx, Notification updatedNotification) {
            boolean applyInPlace;
            if (updatedNotification != null) {
                try {
                    Builder updatedNotificationBuilder = Builder.recoverBuilder(ctx, updatedNotification);
                    if (updatedNotificationBuilder == null) {
                        return false;
                    }
                    RemoteViews newContentView = updatedNotificationBuilder.createContentView();
                    RemoteViews newBigContentView = updatedNotificationBuilder.createBigContentView();
                    RemoteViews newHeadsUpContentView = updatedNotificationBuilder.createHeadsUpContentView();
                    RemoteViews newPublicNotification = updatedNotificationBuilder.makePublicContentView();
                    boolean sameCustomView = Objects.equals(Boolean.valueOf(this.notification.getNotification().extras.getBoolean("android.contains.customView")), Boolean.valueOf(updatedNotification.extras.getBoolean("android.contains.customView")));
                    if (compareRemoteViews(this.cachedContentView, newContentView) && compareRemoteViews(this.cachedBigContentView, newBigContentView) && compareRemoteViews(this.cachedHeadsUpContentView, newHeadsUpContentView) && compareRemoteViews(this.cachedPublicContentView, newPublicNotification)) {
                        applyInPlace = sameCustomView;
                    } else {
                        applyInPlace = false;
                    }
                    this.cachedPublicContentView = newPublicNotification;
                    this.cachedHeadsUpContentView = newHeadsUpContentView;
                    this.cachedBigContentView = newBigContentView;
                    this.cachedContentView = newContentView;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            try {
                Builder builder = Builder.recoverBuilder(ctx, this.notification.getNotification());
                this.cachedContentView = builder.createContentView();
                this.cachedBigContentView = builder.createBigContentView();
                this.cachedHeadsUpContentView = builder.createHeadsUpContentView();
                this.cachedPublicContentView = builder.makePublicContentView();
                applyInPlace = false;
            } catch (Exception e2) {
                e2.printStackTrace();
                return false;
            }
            if (this.icon != null) {
                this.icon.updateTint();
            }
            return applyInPlace;
        }

        private boolean compareRemoteViews(RemoteViews a, RemoteViews b) {
            if (a == null && b == null) {
                return true;
            }
            if (a == null || b == null || b.getPackage() == null || a.getPackage() == null || !a.getPackage().equals(b.getPackage())) {
                return false;
            }
            if (a.getLayoutId() != b.getLayoutId()) {
                return false;
            }
            return true;
        }

        public void notifyFullScreenIntentLaunched() {
            this.lastFullScreenIntentLaunchTime = SystemClock.elapsedRealtime();
        }

        public boolean hasJustLaunchedFullScreenIntent() {
            return SystemClock.elapsedRealtime() < this.lastFullScreenIntentLaunchTime + 2000;
        }
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public NotificationData(Environment environment) {
        this.mEnvironment = environment;
        this.mGroupManager = environment.getGroupManager();
    }

    public ArrayList<Entry> getActiveNotifications() {
        return this.mSortedAndFiltered;
    }

    public Entry get(String key) {
        return (Entry) this.mEntries.get(key);
    }

    public void add(Entry entry, RankingMap ranking) {
        synchronized (this.mEntries) {
            this.mEntries.put(entry.notification.getKey(), entry);
        }
        this.mGroupManager.onEntryAdded(entry);
        updateRankingAndSort(ranking);
    }

    public Entry remove(String key, RankingMap ranking) {
        synchronized (this.mEntries) {
            Entry removed = (Entry) this.mEntries.remove(key);
        }
        if (removed == null) {
            return null;
        }
        this.mGroupManager.onEntryRemoved(removed);
        updateRankingAndSort(ranking);
        return removed;
    }

    public void updateRanking(RankingMap ranking) {
        updateRankingAndSort(ranking);
    }

    public boolean isAmbient(String key) {
        if (this.mRankingMap == null) {
            return false;
        }
        this.mRankingMap.getRanking(key, this.mTmpRanking);
        return this.mTmpRanking.isAmbient();
    }

    public int getVisibilityOverride(String key) {
        if (this.mRankingMap == null) {
            return -1000;
        }
        this.mRankingMap.getRanking(key, this.mTmpRanking);
        return this.mTmpRanking.getVisibilityOverride();
    }

    public boolean shouldSuppressScreenOff(String key) {
        boolean z = false;
        if (this.mRankingMap == null) {
            return false;
        }
        this.mRankingMap.getRanking(key, this.mTmpRanking);
        if ((this.mTmpRanking.getSuppressedVisualEffects() & 1) != 0) {
            z = true;
        }
        return z;
    }

    public boolean shouldSuppressScreenOn(String key) {
        boolean z = false;
        if (this.mRankingMap == null) {
            return false;
        }
        this.mRankingMap.getRanking(key, this.mTmpRanking);
        if ((this.mTmpRanking.getSuppressedVisualEffects() & 2) != 0) {
            z = true;
        }
        return z;
    }

    public int getImportance(String key) {
        if (this.mRankingMap == null) {
            return -1000;
        }
        this.mRankingMap.getRanking(key, this.mTmpRanking);
        return this.mTmpRanking.getImportance();
    }

    public String getOverrideGroupKey(String key) {
        if (this.mRankingMap == null) {
            return null;
        }
        this.mRankingMap.getRanking(key, this.mTmpRanking);
        return this.mTmpRanking.getOverrideGroupKey();
    }

    private void updateRankingAndSort(RankingMap ranking) {
        if (ranking != null) {
            this.mRankingMap = ranking;
            synchronized (this.mEntries) {
                int N = this.mEntries.size();
                for (int i = 0; i < N; i++) {
                    Entry entry = (Entry) this.mEntries.valueAt(i);
                    StatusBarNotification oldSbn = entry.notification.clone();
                    String overrideGroupKey = getOverrideGroupKey(entry.key);
                    if (!Objects.equals(oldSbn.getOverrideGroupKey(), overrideGroupKey)) {
                        entry.notification.setOverrideGroupKey(overrideGroupKey);
                        this.mGroupManager.onEntryUpdated(entry, oldSbn);
                        this.mGroupManager.updateGroupSummuryTime(entry);
                    }
                }
            }
        }
        filterAndSort();
    }

    public void filterAndSort() {
        this.mSortedAndFiltered.clear();
        synchronized (this.mEntries) {
            int N = this.mEntries.size();
            for (int i = 0; i < N; i++) {
                Entry entry = (Entry) this.mEntries.valueAt(i);
                if (!shouldFilterOut(entry.notification)) {
                    this.mSortedAndFiltered.add(entry);
                }
            }
        }
        Collections.sort(this.mSortedAndFiltered, this.mRankingComparator);
    }

    public boolean shouldFilterOut(StatusBarNotification sbn) {
        if (!HwPhoneStatusBar.getInstance().showLockNotification(sbn)) {
            return true;
        }
        boolean z;
        if (this.mEnvironment.isDeviceProvisioned()) {
            z = true;
        } else {
            z = showNotificationEvenIfUnprovisioned(sbn);
        }
        if (!z || !this.mEnvironment.isNotificationForCurrentProfiles(sbn)) {
            return true;
        }
        if (this.mEnvironment.onSecureLockScreen() && (sbn.getNotification().visibility == -1 || this.mEnvironment.shouldHideNotifications(sbn.getUserId()) || (!HwExpandableNotificationRowHelper.isVip(sbn) ? !this.mEnvironment.shouldHideNotifications(sbn.getKey()) : HwExpandableNotificationRowHelper.isVipLock(sbn)))) {
            return true;
        }
        if (BaseStatusBar.ENABLE_CHILD_NOTIFICATIONS || !this.mGroupManager.isChildInGroupWithSummary(sbn)) {
            return false;
        }
        return true;
    }

    public boolean hasActiveClearableNotifications() {
        int N = this.mSortedAndFiltered.size();
        for (int i = 0; i < N; i++) {
            Entry e = (Entry) this.mSortedAndFiltered.get(i);
            if (e.getContentView() != null && e.notification.isClearable()) {
                return true;
            }
        }
        return false;
    }

    public static boolean showNotificationEvenIfUnprovisioned(StatusBarNotification sbn) {
        if ("android".equals(sbn.getPackageName())) {
            return sbn.getNotification().extras.getBoolean("android.allowDuringSetup");
        }
        return false;
    }

    public int getTotalCount() {
        int size;
        synchronized (this.mEntries) {
            size = this.mEntries.size();
        }
        return size;
    }

    public int getActiveCount() {
        return this.mSortedAndFiltered.size();
    }

    public void dump(PrintWriter pw, String indent) {
        int N = this.mSortedAndFiltered.size();
        pw.print(indent);
        pw.println("active notifications: " + N);
        int active = 0;
        while (active < N) {
            dumpEntry(pw, indent, active, (Entry) this.mSortedAndFiltered.get(active));
            active++;
        }
        synchronized (this.mEntries) {
            int M = this.mEntries.size();
            pw.print(indent);
            pw.println("inactive notifications: " + (M - active));
            int inactiveCount = 0;
            for (int i = 0; i < M; i++) {
                Entry entry = (Entry) this.mEntries.valueAt(i);
                if (!this.mSortedAndFiltered.contains(entry)) {
                    dumpEntry(pw, indent, inactiveCount, entry);
                    inactiveCount++;
                }
            }
        }
    }

    private void dumpEntry(PrintWriter pw, String indent, int i, Entry e) {
        this.mRankingMap.getRanking(e.key, this.mTmpRanking);
        pw.print(indent);
        pw.println("  [" + i + "] key=" + e.key + " icon=" + e.icon);
        StatusBarNotification n = e.notification;
        pw.print(indent);
        pw.println("      pkg=" + n.getPackageName() + " id=" + n.getId() + " importance=" + this.mTmpRanking.getImportance());
        pw.print(indent);
        pw.println("      notification=" + n.getNotification());
        pw.print(indent);
        pw.println("      tickerText=\"" + n.getNotification().tickerText + "\"");
        pw.println("      isVIP=\"" + HwExpandableNotificationRowHelper.isVip(n) + "\"");
        pw.println("      isVIPLock=\"" + HwExpandableNotificationRowHelper.isVipLock(n) + "\"");
        pw.println("      isVIPStatus=\"" + HwExpandableNotificationRowHelper.isVipStatusBar(n) + "\"");
    }

    private static boolean isSystemNotification(StatusBarNotification sbn) {
        String sbnPackage = sbn.getPackageName();
        return !"android".equals(sbnPackage) ? "com.android.systemui".equals(sbnPackage) : true;
    }
}
