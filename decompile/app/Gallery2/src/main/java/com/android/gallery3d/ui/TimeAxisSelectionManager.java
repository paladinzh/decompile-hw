package com.android.gallery3d.ui;

import android.util.SparseArray;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.IGroupAlbum;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.data.AbsGroupData;
import com.huawei.gallery.ui.ListSlotView.ItemCoordinate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TimeAxisSelectionManager extends SelectionManager {
    private SparseArray<BucketSelectionEntry> mBucketSelectionEntryArray = new SparseArray();
    private Delegate mDelegate;
    private ArrayList<AbsGroupData> mGroupData;

    private static class BucketSelectionEntry {
        public Set<Path> clickedSet;
        public int group;
        public boolean inverseSelection;
        public int totalCount;

        private BucketSelectionEntry() {
            this.group = -1;
            this.clickedSet = new HashSet();
        }

        public void selectAll() {
            this.inverseSelection = true;
            this.clickedSet.clear();
        }

        public void deSelectAll() {
            this.inverseSelection = false;
            this.clickedSet.clear();
        }

        public boolean inSelectAllMode() {
            return this.inverseSelection ? this.clickedSet.isEmpty() : false;
        }

        public void modifyAllSelection() {
            if (inSelectAllMode()) {
                deSelectAll();
            } else {
                selectAll();
            }
        }

        public int getSelectedCount() {
            int count = this.clickedSet.size();
            if (this.inverseSelection) {
                return this.totalCount - count;
            }
            return count;
        }

        public boolean isItemSelected(Path itemId) {
            return this.inverseSelection ^ this.clickedSet.contains(itemId);
        }
    }

    public interface Delegate {
        ArrayList<AbsGroupData> getGroupDatas();
    }

    public TimeAxisSelectionManager(GalleryContext activity, Delegate delegate) {
        super(activity, false);
        this.mDelegate = delegate;
    }

    public void selectAll() {
        this.mBucketSelectionEntryArray.clear();
        super.selectAll();
    }

    public void deSelectAll() {
        this.mBucketSelectionEntryArray.clear();
        super.deSelectAll();
    }

    public boolean inSelectAllMode() {
        return this.mInverseSelection && this.mBucketSelectionEntryArray.size() == 0;
    }

    public boolean inSelectAllGroupMode(ItemCoordinate coordinate) {
        boolean z = false;
        BucketSelectionEntry entry = (BucketSelectionEntry) this.mBucketSelectionEntryArray.get(coordinate.group);
        if (this.mInverseSelection) {
            if (entry == null) {
                z = true;
            }
            return z;
        }
        if (entry != null) {
            z = entry.inSelectAllMode();
        }
        return z;
    }

    public void leaveSelectionMode() {
        this.mBucketSelectionEntryArray.clear();
        super.leaveSelectionMode();
    }

    public boolean isItemSelected(ItemCoordinate coordinate, Path itemId) {
        boolean z = true;
        boolean z2 = false;
        BucketSelectionEntry entry = (BucketSelectionEntry) this.mBucketSelectionEntryArray.get(coordinate.group);
        if (this.mInverseSelection) {
            if (entry != null && entry.isItemSelected(itemId)) {
                z = false;
            }
            return z;
        }
        if (entry != null) {
            z2 = entry.isItemSelected(itemId);
        }
        return z2;
    }

    public boolean isItemSelected(int absIndex, Path itemId) {
        return isItemSelected(getItemCoordinateByAbsIndex(absIndex), itemId);
    }

    public int getSelectedCount() {
        int count = 0;
        for (int index = 0; index < this.mBucketSelectionEntryArray.size(); index++) {
            count += ((BucketSelectionEntry) this.mBucketSelectionEntryArray.valueAt(index)).getSelectedCount();
        }
        if (this.mInverseSelection) {
            return getTotalCount() - count;
        }
        return count;
    }

    public void toggleGroup(ItemCoordinate coordinate) {
        if (!this.mInverseSelection) {
            BucketSelectionEntry entry = getBucketSelectionEntry(coordinate);
            entry.modifyAllSelection();
            if (entry.getSelectedCount() == 0) {
                this.mBucketSelectionEntryArray.remove(entry.group);
            }
        } else if (((BucketSelectionEntry) this.mBucketSelectionEntryArray.get(coordinate.group)) == null) {
            getBucketSelectionEntry(coordinate).selectAll();
        } else {
            this.mBucketSelectionEntryArray.remove(coordinate.group);
        }
        int count = getSelectedCount();
        if (count == getTotalCount()) {
            selectAll();
        }
        if (this.mListener != null) {
            this.mListener.onSelectionChange(null, false);
        }
        if (count == 0 && this.mAutoLeave) {
            leaveSelectionMode();
        }
    }

    public void toggle(ItemCoordinate coordinate, Path path) {
        BucketSelectionEntry entry = getBucketSelectionEntry(coordinate);
        toggle(entry.clickedSet, path);
        int entrySelectedCount = entry.getSelectedCount();
        if (entrySelectedCount == 0) {
            this.mBucketSelectionEntryArray.remove(entry.group);
        } else if (entrySelectedCount == entry.totalCount) {
            entry.selectAll();
        }
    }

    public void toggle(int absIndex, Path path) {
        toggle(getItemCoordinateByAbsIndex(absIndex), path);
    }

    public ArrayList<Path> getSelected(boolean expandSet, JobContext jc) {
        if (!(this.mSourceMediaSet instanceof IGroupAlbum)) {
            return new ArrayList();
        }
        Set<Path> clickedSet = new HashSet();
        ArrayList<AbsGroupData> groupDatas = this.mDelegate.getGroupDatas();
        for (int i = 0; i < this.mBucketSelectionEntryArray.size(); i++) {
            BucketSelectionEntry entry = (BucketSelectionEntry) this.mBucketSelectionEntryArray.valueAt(i);
            if (entry.group < groupDatas.size()) {
                if (SelectionManager.isCancelled(jc)) {
                    break;
                } else if (entry.inverseSelection) {
                    int total = entry.totalCount;
                    int index = 0;
                    while (index < total && !SelectionManager.isCancelled(jc)) {
                        AbsGroupData groupData = (AbsGroupData) groupDatas.get(entry.group);
                        IGroupAlbum sourceMediaSet = this.mSourceMediaSet;
                        int count = Math.min(total - index, sourceMediaSet.getBatchSize());
                        for (MediaItem item : sourceMediaSet.getMediaItem(index, count, groupData)) {
                            Path id = item.getPath();
                            if (!entry.clickedSet.contains(id)) {
                                clickedSet.add(id);
                            }
                        }
                        index += count;
                    }
                } else {
                    clickedSet.addAll(entry.clickedSet);
                }
            }
        }
        if (SelectionManager.isCancelled(jc)) {
            return new ArrayList();
        }
        return getSelected(clickedSet, expandSet, jc);
    }

    private ItemCoordinate getItemCoordinateByAbsIndex(int absIndex) {
        if (this.mGroupData == null) {
            return new ItemCoordinate(-1, -1);
        }
        int i;
        int index = 0;
        int size = this.mGroupData.size();
        int group = 0;
        while (group < size) {
            int count = ((AbsGroupData) this.mGroupData.get(group)).count;
            index += count;
            if (index > absIndex) {
                index -= count;
                i = group - 1;
                i = Math.max(0, group);
                break;
            }
            group++;
        }
        i = group;
        return new ItemCoordinate(i, absIndex - index);
    }

    private BucketSelectionEntry getBucketSelectionEntry(ItemCoordinate coordinate) {
        BucketSelectionEntry entry = (BucketSelectionEntry) this.mBucketSelectionEntryArray.get(coordinate.group);
        if (entry == null) {
            entry = new BucketSelectionEntry();
            entry.group = coordinate.group;
            ArrayList<AbsGroupData> groupDatas = this.mDelegate.getGroupDatas();
            if (coordinate.group >= 0 && coordinate.group < groupDatas.size()) {
                entry.totalCount = ((AbsGroupData) groupDatas.get(coordinate.group)).count;
            }
            this.mBucketSelectionEntryArray.put(coordinate.group, entry);
        }
        return entry;
    }

    public void setGroupData(ArrayList<AbsGroupData> groupData) {
        this.mGroupData = groupData;
    }

    public void updateGroupData(ArrayList<AbsGroupData> groupData) {
        this.mGroupData = groupData;
        for (int i = 0; i < this.mBucketSelectionEntryArray.size(); i++) {
            BucketSelectionEntry entry = (BucketSelectionEntry) this.mBucketSelectionEntryArray.valueAt(i);
            if (entry.group < groupData.size()) {
                entry.totalCount = ((AbsGroupData) groupData.get(entry.group)).count;
            }
        }
    }
}
