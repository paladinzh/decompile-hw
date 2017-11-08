package com.android.systemui.statusbar.phone;

import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener;
import com.android.systemui.utils.HwLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class NotificationGroupManager implements OnHeadsUpChangedListener {
    private int mBarState = -1;
    private final HashMap<String, NotificationGroup> mGroupMap = new HashMap();
    private HeadsUpManager mHeadsUpManager;
    private HashMap<String, StatusBarNotification> mIsolatedEntries = new HashMap();
    private OnGroupChangeListener mListener;

    public static class NotificationGroup {
        public final HashSet<Entry> children = new HashSet();
        public boolean expanded;
        public Entry summary;
        public boolean suppressed;

        public String toString() {
            String result = ("    summary:\n      " + (this.summary != null ? this.summary.notification : "null")) + "\n    children size: " + this.children.size();
            for (Entry child : this.children) {
                result = result + "\n      " + child.notification;
            }
            return result + "\n    suppressed=" + this.suppressed;
        }
    }

    public interface OnGroupChangeListener {
        void onGroupCreatedFromChildren(NotificationGroup notificationGroup);

        void onGroupExpansionChanged(ExpandableNotificationRow expandableNotificationRow, boolean z);

        void onGroupsChanged();
    }

    public void setOnGroupChangeListener(OnGroupChangeListener listener) {
        this.mListener = listener;
    }

    public boolean isGroupExpanded(StatusBarNotification sbn) {
        NotificationGroup group = (NotificationGroup) this.mGroupMap.get(getGroupKey(sbn));
        if (group == null) {
            return false;
        }
        return group.expanded;
    }

    public void setGroupExpanded(StatusBarNotification sbn, boolean expanded) {
        HwLog.i("NotificationGroupManager", "setGroupExpanded:" + sbn.getKey() + ", expanded=" + expanded);
        NotificationGroup group = (NotificationGroup) this.mGroupMap.get(getGroupKey(sbn));
        if (group != null) {
            setGroupExpanded(group, expanded);
        }
    }

    private void setGroupExpanded(NotificationGroup group, boolean expanded) {
        group.expanded = expanded;
        if (group.summary != null) {
            HwLog.i("NotificationGroupManager", "setGroupExpanded:" + group.summary.notification.getKey() + ", expanded=" + expanded);
            this.mListener.onGroupExpansionChanged(group.summary.row, expanded);
        }
    }

    public void onEntryRemoved(Entry removed) {
        HwLog.i("NotificationGroupManager", "onEntryRemoved:" + removed.notification.getKey());
        onEntryRemovedInternal(removed, removed.notification);
        this.mIsolatedEntries.remove(removed.key);
    }

    private void onEntryRemovedInternal(Entry removed, StatusBarNotification sbn) {
        HwLog.i("NotificationGroupManager", "onEntryRemovedInternal:" + sbn.getKey() + ", group=" + getGroupKey(sbn));
        String groupKey = getGroupKey(sbn);
        NotificationGroup group = (NotificationGroup) this.mGroupMap.get(groupKey);
        if (group != null) {
            if (isGroupChild(sbn)) {
                group.children.remove(removed);
            } else {
                HwLog.i("NotificationGroupManager", "summary = null");
                group.summary = null;
            }
            updateSuppression(group);
            if (group.children.isEmpty() && group.summary == null) {
                this.mGroupMap.remove(groupKey);
            }
        }
    }

    public void onEntryAdded(Entry added) {
        StatusBarNotification sbn = added.notification;
        HwLog.i("NotificationGroupManager", "onEntryAdded:" + sbn.getKey() + ", group=" + getGroupKey(sbn));
        boolean isGroupChild = isGroupChild(sbn);
        String groupKey = getGroupKey(sbn);
        NotificationGroup group = (NotificationGroup) this.mGroupMap.get(groupKey);
        if (group == null) {
            group = new NotificationGroup();
            this.mGroupMap.put(groupKey, group);
        }
        if (isGroupChild) {
            group.children.add(added);
            updateSuppression(group);
            return;
        }
        group.summary = added;
        group.expanded = added.row.areChildrenExpanded();
        updateSuppression(group);
        if (!group.children.isEmpty()) {
            for (Entry child : (HashSet) group.children.clone()) {
                onEntryBecomingChild(child);
            }
            this.mListener.onGroupCreatedFromChildren(group);
        }
    }

    private void onEntryBecomingChild(Entry entry) {
        HwLog.i("NotificationGroupManager", "onEntryBecomingChild:" + entry.notification.getKey());
        if (entry.row.isHeadsUp()) {
            onHeadsUpStateChanged(entry, true);
        }
    }

    private void updateSuppression(NotificationGroup group) {
        boolean z = true;
        if (group != null) {
            boolean prevSuppressed = group.suppressed;
            if (group.summary == null || group.expanded) {
                z = false;
            } else if (group.children.size() != 1) {
                z = (group.children.size() == 0 && group.summary.notification.getNotification().isGroupSummary()) ? hasIsolatedChildren(group) : false;
            }
            group.suppressed = z;
            if (prevSuppressed != group.suppressed) {
                if (group.suppressed) {
                    handleSuppressedSummaryHeadsUpped(group.summary);
                }
                this.mListener.onGroupsChanged();
            }
        }
    }

    private boolean hasIsolatedChildren(NotificationGroup group) {
        return getNumberOfIsolatedChildren(group.summary.notification.getGroupKey()) != 0;
    }

    private int getNumberOfIsolatedChildren(String groupKey) {
        int count = 0;
        for (StatusBarNotification sbn : this.mIsolatedEntries.values()) {
            if (sbn.getGroupKey().equals(groupKey) && isIsolated(sbn)) {
                count++;
            }
        }
        return count;
    }

    private Entry getIsolatedChild(String groupKey) {
        for (StatusBarNotification sbn : this.mIsolatedEntries.values()) {
            if (sbn.getGroupKey().equals(groupKey) && isIsolated(sbn)) {
                return ((NotificationGroup) this.mGroupMap.get(sbn.getKey())).summary;
            }
        }
        return null;
    }

    public void onEntryUpdated(Entry entry, StatusBarNotification oldNotification) {
        HwLog.i("NotificationGroupManager", "onEntryUpdated:" + oldNotification.getKey());
        String oldGroupKey = oldNotification.getGroupKey();
        String newGroupKey = entry.notification.getGroupKey();
        if (!(this.mGroupMap.get(getGroupKey(oldNotification)) == null || oldGroupKey == null || oldGroupKey.equals(newGroupKey))) {
            onEntryRemovedInternal(entry, oldNotification);
        }
        onEntryAdded(entry);
        if (isIsolated(entry.notification)) {
            this.mIsolatedEntries.put(entry.key, entry.notification);
            String oldKey = oldNotification.getGroupKey();
            String newKey = entry.notification.getGroupKey();
            if (!oldKey.equals(newKey)) {
                updateSuppression((NotificationGroup) this.mGroupMap.get(oldKey));
                updateSuppression((NotificationGroup) this.mGroupMap.get(newKey));
            }
        } else if (!isGroupChild(oldNotification) && isGroupChild(entry.notification)) {
            onEntryBecomingChild(entry);
        }
    }

    public boolean isSummaryOfSuppressedGroup(StatusBarNotification sbn) {
        return isGroupSuppressed(getGroupKey(sbn)) ? sbn.getNotification().isGroupSummary() : false;
    }

    private boolean isOnlyChild(StatusBarNotification sbn) {
        if (sbn.getNotification().isGroupSummary()) {
            return false;
        }
        return getTotalNumberOfChildren(sbn) == 1;
    }

    public boolean isOnlyChildInGroup(StatusBarNotification sbn) {
        boolean z = false;
        if (!isOnlyChild(sbn)) {
            return false;
        }
        ExpandableNotificationRow logicalGroupSummary = getLogicalGroupSummary(sbn);
        if (!(logicalGroupSummary == null || logicalGroupSummary.getStatusBarNotification().equals(sbn))) {
            z = true;
        }
        return z;
    }

    private int getTotalNumberOfChildren(StatusBarNotification sbn) {
        NotificationGroup group = (NotificationGroup) this.mGroupMap.get(sbn.getGroupKey());
        return getNumberOfIsolatedChildren(sbn.getGroupKey()) + (group != null ? group.children.size() : 0);
    }

    private boolean isGroupSuppressed(String groupKey) {
        NotificationGroup group = (NotificationGroup) this.mGroupMap.get(groupKey);
        return group != null ? group.suppressed : false;
    }

    public void setStatusBarState(int newState) {
        if (this.mBarState != newState) {
            this.mBarState = newState;
            if (this.mBarState == 1) {
                collapseAllGroups();
            }
        }
    }

    public void collapseAllGroups() {
        HwLog.i("NotificationGroupManager", "collapseAllGroups:");
        ArrayList<NotificationGroup> groupCopy = new ArrayList(this.mGroupMap.values());
        int size = groupCopy.size();
        for (int i = 0; i < size; i++) {
            NotificationGroup group = (NotificationGroup) groupCopy.get(i);
            if (group.expanded) {
                setGroupExpanded(group, false);
            }
            updateSuppression(group);
        }
    }

    public boolean isChildInGroupWithSummary(StatusBarNotification sbn) {
        if (!isGroupChild(sbn)) {
            return false;
        }
        NotificationGroup group = (NotificationGroup) this.mGroupMap.get(getGroupKey(sbn));
        if (group == null || group.summary == null || group.suppressed) {
            HwLog.d("NotificationGroupManager", "isChildInGroupWithSummary:" + sbn.getGroupKey() + ", " + group + ", group1=" + getGroupKey(sbn));
            return false;
        } else if (!group.children.isEmpty()) {
            return true;
        } else {
            HwLog.d("NotificationGroupManager", "isChildInGroupWithSummary1:" + sbn.getGroupKey());
            return false;
        }
    }

    public boolean isSummaryOfGroup(StatusBarNotification sbn) {
        boolean z = false;
        if (!isGroupSummary(sbn)) {
            return false;
        }
        NotificationGroup group = (NotificationGroup) this.mGroupMap.get(getGroupKey(sbn));
        if (group == null) {
            return false;
        }
        if (!group.children.isEmpty()) {
            z = true;
        }
        return z;
    }

    public ExpandableNotificationRow getGroupSummary(StatusBarNotification sbn) {
        return getGroupSummary(getGroupKey(sbn));
    }

    public ExpandableNotificationRow getLogicalGroupSummary(StatusBarNotification sbn) {
        return getGroupSummary(sbn.getGroupKey());
    }

    @Nullable
    private ExpandableNotificationRow getGroupSummary(String groupKey) {
        NotificationGroup group = (NotificationGroup) this.mGroupMap.get(groupKey);
        if (group == null || group.summary == null) {
            return null;
        }
        return group.summary.row;
    }

    public boolean toggleGroupExpansion(StatusBarNotification sbn) {
        boolean z = false;
        NotificationGroup group = (NotificationGroup) this.mGroupMap.get(getGroupKey(sbn));
        if (group == null) {
            return false;
        }
        if (!group.expanded) {
            z = true;
        }
        setGroupExpanded(group, z);
        return group.expanded;
    }

    private boolean isIsolated(StatusBarNotification sbn) {
        return this.mIsolatedEntries.containsKey(sbn.getKey());
    }

    private boolean isGroupSummary(StatusBarNotification sbn) {
        if (isIsolated(sbn)) {
            return true;
        }
        return sbn.getNotification().isGroupSummary();
    }

    private boolean isGroupChild(StatusBarNotification sbn) {
        boolean z = false;
        if (isIsolated(sbn)) {
            return false;
        }
        if (sbn.isGroup() && !sbn.getNotification().isGroupSummary()) {
            z = true;
        }
        return z;
    }

    private String getGroupKey(StatusBarNotification sbn) {
        if (isIsolated(sbn)) {
            return sbn.getKey();
        }
        return sbn.getGroupKey();
    }

    public void onHeadsUpPinnedModeChanged(boolean inPinnedMode) {
    }

    public void onHeadsUpPinned(ExpandableNotificationRow headsUp) {
    }

    public void onHeadsUpUnPinned(ExpandableNotificationRow headsUp) {
    }

    public void onHeadsUpStateChanged(Entry entry, boolean isHeadsUp) {
        StatusBarNotification sbn = entry.notification;
        HwLog.i("NotificationGroupManager", "onHeadsUpStateChanged:" + sbn.getKey());
        if (entry.row.isHeadsUp()) {
            if (shouldIsolate(sbn)) {
                onEntryRemovedInternal(entry, entry.notification);
                this.mIsolatedEntries.put(sbn.getKey(), sbn);
                onEntryAdded(entry);
                updateSuppression((NotificationGroup) this.mGroupMap.get(entry.notification.getGroupKey()));
                this.mListener.onGroupsChanged();
                return;
            }
            handleSuppressedSummaryHeadsUpped(entry);
        } else if (this.mIsolatedEntries.containsKey(sbn.getKey())) {
            onEntryRemovedInternal(entry, entry.notification);
            this.mIsolatedEntries.remove(sbn.getKey());
            onEntryAdded(entry);
            this.mListener.onGroupsChanged();
        }
    }

    private void handleSuppressedSummaryHeadsUpped(Entry entry) {
        Entry child = null;
        StatusBarNotification sbn = entry.notification;
        if (isGroupSuppressed(sbn.getGroupKey()) && sbn.getNotification().isGroupSummary() && entry.row.isHeadsUp()) {
            NotificationGroup notificationGroup = (NotificationGroup) this.mGroupMap.get(sbn.getGroupKey());
            if (notificationGroup != null) {
                Iterator<Entry> iterator = notificationGroup.children.iterator();
                if (iterator.hasNext()) {
                    child = (Entry) iterator.next();
                }
                if (child == null) {
                    child = getIsolatedChild(sbn.getGroupKey());
                }
                if (child != null) {
                    if (this.mHeadsUpManager.isHeadsUp(child.key)) {
                        this.mHeadsUpManager.updateNotification(child, true);
                    } else {
                        this.mHeadsUpManager.showNotification(child);
                    }
                }
            }
            this.mHeadsUpManager.releaseImmediately(entry.key);
        }
    }

    private boolean shouldIsolate(StatusBarNotification sbn) {
        NotificationGroup notificationGroup = (NotificationGroup) this.mGroupMap.get(sbn.getGroupKey());
        if (!sbn.isGroup() || sbn.getNotification().isGroupSummary()) {
            return false;
        }
        if (sbn.getNotification().fullScreenIntent == null && notificationGroup != null && notificationGroup.expanded) {
            return isGroupNotFullyVisible(notificationGroup);
        }
        return true;
    }

    private boolean isGroupNotFullyVisible(NotificationGroup notificationGroup) {
        if (notificationGroup.summary == null || notificationGroup.summary.row.getClipTopAmount() > 0 || notificationGroup.summary.row.getTranslationY() < 0.0f) {
            return true;
        }
        return false;
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public HashSet<Entry> getGroupChildren(StatusBarNotification sbn) {
        return ((NotificationGroup) this.mGroupMap.get(getGroupKey(sbn))).children;
    }

    public void updateGroupSummuryTime(Entry entry) {
        if (entry != null && entry.notification != null) {
            NotificationGroup group = (NotificationGroup) this.mGroupMap.get(entry.notification.getGroupKey());
            if (!(group == null || group.summary == null)) {
                group.summary.createTime = Math.max(entry.createTime, group.summary.createTime);
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("GroupManager state:");
        pw.println("  number of groups: " + this.mGroupMap.size());
        for (Map.Entry<String, NotificationGroup> entry : this.mGroupMap.entrySet()) {
            pw.println("\n    key: " + ((String) entry.getKey()));
            pw.println(entry.getValue());
        }
        pw.println("\n    isolated entries: " + this.mIsolatedEntries.size());
        for (Map.Entry<String, StatusBarNotification> entry2 : this.mIsolatedEntries.entrySet()) {
            pw.print("      ");
            pw.print((String) entry2.getKey());
            pw.print(", ");
            pw.println(entry2.getValue());
        }
    }
}
