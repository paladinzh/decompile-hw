package com.android.gallery3d.ui;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.JobContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectionManager {
    protected boolean mAutoLeave = true;
    private Set<Path> mClickedSet;
    private DataManager mDataManager;
    private final Handler mHandler;
    private boolean mInMyFavoriteMode;
    private boolean mInPrivateMode;
    private boolean mInSelectionMode;
    private boolean mInSingleMode = false;
    private Set<Path> mInitialMyFavoriteItems;
    private Set<Path> mInitialPrivateSet;
    protected boolean mInverseSelection;
    private boolean mIsAlbumSet;
    private int mLimitExceedNum = Integer.MAX_VALUE;
    protected SelectionListener mListener;
    protected MediaSet mSourceMediaSet;
    private int mTotal;
    private SelectionUIListener mUIListener;

    public interface SelectionListener {
        void onSelectionChange(Path path, boolean z);

        void onSelectionLimitExceed();

        void onSelectionModeChange(int i);
    }

    public SelectionManager(GalleryContext activity, boolean isAlbumSet) {
        this.mDataManager = activity.getDataManager();
        this.mClickedSet = new HashSet();
        this.mIsAlbumSet = isAlbumSet;
        this.mTotal = -1;
        this.mInitialPrivateSet = new HashSet();
        this.mInitialMyFavoriteItems = new HashSet();
        this.mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        if (SelectionManager.this.mUIListener != null) {
                            int selectionMode = 0;
                            if (SelectionManager.this.mInPrivateMode) {
                                selectionMode = 2;
                            }
                            if (SelectionManager.this.mInMyFavoriteMode) {
                                selectionMode |= 16;
                            }
                            SelectionManager.this.mUIListener.onUIDoubted(SelectionManager.this.mInSelectionMode, selectionMode, null);
                            return;
                        }
                        return;
                    default:
                        super.handleMessage(msg);
                        return;
                }
            }
        };
    }

    public void setAutoLeaveSelectionMode(boolean enable) {
        this.mAutoLeave = enable;
    }

    public void setSelectionListener(SelectionListener listener) {
        this.mListener = listener;
    }

    public void setInverse(boolean value) {
        this.mInverseSelection = value;
    }

    public void selectAll() {
        if (this.mInSingleMode && getTotalCount() >= 2) {
            return;
        }
        if (getTotalCount() <= this.mLimitExceedNum || this.mListener == null) {
            this.mInverseSelection = true;
            this.mClickedSet.clear();
            enterSelectionMode();
            if (this.mListener != null) {
                this.mListener.onSelectionModeChange(3);
            }
            return;
        }
        this.mListener.onSelectionLimitExceed();
    }

    public void deSelectAll() {
        if (this.mAutoLeave) {
            leaveSelectionMode();
        }
        this.mInverseSelection = false;
        this.mClickedSet.clear();
        if (this.mListener != null) {
            this.mListener.onSelectionModeChange(4);
        }
    }

    public boolean inSelectAllMode() {
        return this.mInverseSelection ? this.mClickedSet.isEmpty() : false;
    }

    public void modifyAllSelection() {
        if (inSelectAllMode()) {
            deSelectAll();
        } else {
            selectAll();
        }
    }

    public boolean inSelectionMode() {
        return this.mInSelectionMode;
    }

    public void enterSelectionMode() {
        if (!this.mInSelectionMode) {
            this.mInSelectionMode = true;
            if (this.mListener != null) {
                this.mListener.onSelectionModeChange(1);
            }
        }
    }

    public void leaveSelectionMode() {
        if (this.mInSelectionMode) {
            this.mInSelectionMode = false;
            this.mInverseSelection = false;
            this.mInPrivateMode = false;
            this.mInSingleMode = false;
            this.mInMyFavoriteMode = false;
            this.mLimitExceedNum = Integer.MAX_VALUE;
            this.mClickedSet.clear();
            this.mInitialPrivateSet.clear();
            this.mInitialMyFavoriteItems.clear();
            if (this.mListener != null) {
                this.mListener.onSelectionModeChange(2);
            }
        }
    }

    public boolean isItemSelected(Path itemId) {
        return this.mInverseSelection ^ this.mClickedSet.contains(itemId);
    }

    public int getTotalCount() {
        if (this.mSourceMediaSet == null) {
            return -1;
        }
        int subMediaSetCount;
        if (this.mIsAlbumSet) {
            subMediaSetCount = this.mSourceMediaSet.getSubMediaSetCount();
        } else {
            subMediaSetCount = this.mSourceMediaSet.getMediaItemCount();
        }
        this.mTotal = subMediaSetCount;
        return this.mTotal;
    }

    public int getSelectedCount() {
        int count = this.mClickedSet.size();
        if (this.mInverseSelection) {
            return getTotalCount() - count;
        }
        return count;
    }

    public void toggle(Path path) {
        toggle(this.mClickedSet, path);
    }

    protected void toggle(Set<Path> clickedSet, Path path) {
        if (clickedSet.contains(path)) {
            clickedSet.remove(path);
        } else {
            enterSelectionMode();
            if (this.mInSingleMode) {
                deSelectAll();
            }
            if (getSelectedCount() < this.mLimitExceedNum || this.mListener == null) {
                clickedSet.add(path);
            } else {
                this.mListener.onSelectionLimitExceed();
                return;
            }
        }
        int count = getSelectedCount();
        if (count == getTotalCount()) {
            selectAll();
        }
        if (this.mListener != null) {
            this.mListener.onSelectionChange(path, isItemSelected(path));
        }
        if (count == 0 && this.mAutoLeave) {
            leaveSelectionMode();
        }
    }

    private static void expandMediaSet(ArrayList<Path> items, MediaSet set, JobContext jc) {
        int subCount = set.getSubMediaSetCount();
        for (int i = 0; i < subCount; i++) {
            MediaSet subSet = set.getSubMediaSet(i);
            if (subSet != null) {
                if (isCancelled(jc)) {
                    break;
                }
                expandMediaSet(items, subSet, jc);
            }
        }
        int total = set.getMediaItemCount();
        for (int index = 0; index < total && !isCancelled(jc); index += 50) {
            int count;
            if (index + 50 < total) {
                count = 50;
            } else {
                count = total - index;
            }
            for (MediaItem item : set.getMediaItem(index, count)) {
                items.add(item.getPath());
            }
        }
    }

    public ArrayList<Path> getSelected(boolean expandSet) {
        return getSelected(expandSet, null);
    }

    public ArrayList<Path> getSelected(boolean expandSet, JobContext jc) {
        return getSelected(this.mClickedSet, expandSet, jc);
    }

    protected ArrayList<Path> getSelected(Set<Path> clickedSet, boolean expandSet, JobContext jc) {
        ArrayList<Path> selected = new ArrayList();
        Path id;
        int total;
        if (!this.mIsAlbumSet) {
            if (!this.mInverseSelection) {
                for (Path id2 : clickedSet) {
                    if (isCancelled(jc)) {
                        break;
                    }
                    selected.add(id2);
                }
            } else {
                total = getTotalCount();
                int index = 0;
                while (index < total && !isCancelled(jc)) {
                    int count = Math.min(total - index, 500);
                    for (MediaItem item : this.mSourceMediaSet.getMediaItem(index, count)) {
                        id2 = item.getPath();
                        if (!clickedSet.contains(id2)) {
                            selected.add(id2);
                        }
                    }
                    index += count;
                }
            }
        } else if (!this.mInverseSelection) {
            for (Path id22 : clickedSet) {
                if (isCancelled(jc)) {
                    break;
                } else if (expandSet) {
                    expandMediaSet(selected, this.mDataManager.getMediaSet(id22), jc);
                } else {
                    selected.add(id22);
                }
            }
        } else {
            total = getTotalCount();
            for (int i = 0; i < total; i++) {
                MediaSet set = this.mSourceMediaSet.getSubMediaSet(i);
                if (set != null) {
                    if (isCancelled(jc)) {
                        break;
                    }
                    id22 = set.getPath();
                    if (!clickedSet.contains(id22)) {
                        if (expandSet) {
                            expandMediaSet(selected, set, jc);
                        } else {
                            selected.add(id22);
                        }
                    }
                }
            }
        }
        return selected;
    }

    protected static boolean isCancelled(JobContext jc) {
        return jc != null ? jc.isCancelled() : false;
    }

    public void setSourceMediaSet(MediaSet set) {
        this.mSourceMediaSet = set;
        this.mTotal = -1;
    }

    public boolean isOnlyEmptyAlbumSelected() {
        for (Path path : getSelected(false)) {
            MediaSet mediaSet = this.mDataManager.getMediaSet(path);
            if (mediaSet != null) {
                if (!mediaSet.isEmptyAlbum()) {
                }
            }
            return false;
        }
        return true;
    }

    public boolean isSpecificAlbumSelected(int bucketId) {
        if (!this.mIsAlbumSet) {
            return false;
        }
        try {
            for (Path path : getSelected(false)) {
                String pathSuffix = path.getSuffix();
                if (GalleryUtils.isPathSuffixInteger(pathSuffix) && Integer.parseInt(pathSuffix) == bucketId) {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public String getSelectedAlbumName() {
        if (!this.mIsAlbumSet) {
            return null;
        }
        List<Path> list = getSelected(false);
        if (list.size() != 1) {
            return null;
        }
        return this.mDataManager.getMediaSet((Path) list.get(0)).getName();
    }

    public String getSelectedAlbumPath() {
        String str = null;
        if (!this.mIsAlbumSet) {
            return null;
        }
        MediaSet media = getMediasetIfSelectedOnlyOne();
        if (media != null) {
            str = media.getBucketPath();
        }
        return str;
    }

    public MediaSet getMediasetIfSelectedOnlyOne() {
        List<Path> list = getSelected(false);
        if (list.size() != 1) {
            return null;
        }
        return this.mDataManager.getMediaSet((Path) list.get(0));
    }

    public boolean inMyFavoriteMode() {
        return this.mInMyFavoriteMode;
    }

    public boolean inPrivateMode() {
        return this.mInPrivateMode;
    }

    public boolean inSingleMode() {
        return this.mInSingleMode;
    }

    public void setSingleMode(boolean enable) {
        this.mInSingleMode = enable;
    }

    public ArrayList<Path> getProcessingList(boolean expandSet) {
        ArrayList<Path> needProcessedList = getSelected(expandSet);
        if (inPrivateMode()) {
            processList(needProcessedList, this.mInitialPrivateSet);
        }
        if (inMyFavoriteMode()) {
            processList(needProcessedList, this.mInitialMyFavoriteItems);
        }
        return needProcessedList;
    }

    private void processList(ArrayList<Path> targetList, Set<Path> originList) {
        for (Path id : originList) {
            if (targetList.contains(id)) {
                targetList.remove(id);
            } else {
                targetList.add(id);
            }
        }
    }

    public void setLimitExceedNum(int num) {
        this.mLimitExceedNum = num;
    }

    public void setClickedSet(Set<Path> clickedSet) {
        this.mClickedSet.addAll(clickedSet);
    }

    public boolean isAlbumSet() {
        return this.mIsAlbumSet;
    }

    public void updateSelectMode(boolean isGetContent) {
        if (this.mListener != null) {
            if (inSelectAllMode() && !isGetContent) {
                this.mListener.onSelectionModeChange(3);
            } else if (getSelectedCount() != 0 || isGetContent) {
                this.mListener.onSelectionChange(null, false);
            } else {
                this.mListener.onSelectionModeChange(4);
            }
        }
    }

    public int getItemSelectCount() {
        if (!this.mIsAlbumSet) {
            return getSelectedCount();
        }
        int total = getTotalCount();
        int itemCount = 0;
        for (int i = 0; i < total; i++) {
            MediaSet set = this.mSourceMediaSet.getSubMediaSet(i);
            if (set != null && isItemSelected(set.getPath())) {
                int subMediaSetCount = set.getSubMediaSetCount();
                if (subMediaSetCount != 0) {
                    for (int j = 0; j < subMediaSetCount; j++) {
                        itemCount += set.getSubMediaSet(j).getMediaItemCount();
                    }
                } else {
                    itemCount += set.getMediaItemCount();
                }
            }
        }
        return itemCount;
    }
}
