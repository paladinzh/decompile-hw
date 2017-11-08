package com.huawei.gallery.map.data;

import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.util.MyPrinter;
import java.util.ArrayList;
import java.util.List;

public class ClusterInfo implements IClusterInfo {
    private static final MyPrinter LOG = new MyPrinter(ClusterInfo.class.getSimpleName());
    public final MapLatLng from;
    private final DataManager mDataManager;
    private List<Path> mItems = new ArrayList();
    public final MapAlbum target;
    public final MapLatLng to;

    public ClusterInfo(DataManager dataManager, MapLatLng fromPosition, MapLatLng toPosition, MapAlbum targetAlbum) {
        this.mDataManager = dataManager;
        this.from = fromPosition;
        this.to = toPosition;
        this.target = targetAlbum;
    }

    public String toString() {
        return " itemCount " + this.mItems.size() + " target " + this.target;
    }

    public void addItem(Path item) {
        this.mItems.add(item);
    }

    public void addToTarget() {
        if (this.target != null) {
            GalleryLog.d("", "debug-info:  [addToTarget] mItems length " + this.mItems.size());
            this.mItems.removeAll(this.target.getAllItems());
            this.target.addAll(this.mItems);
            this.mItems.clear();
        }
    }

    public MediaItem getCoverMediaItem() {
        if (this.mItems.isEmpty()) {
            return null;
        }
        return (MediaItem) this.mDataManager.getMediaObject((Path) this.mItems.get(0));
    }

    public int getMediaItemCount() {
        return this.mItems.size();
    }
}
