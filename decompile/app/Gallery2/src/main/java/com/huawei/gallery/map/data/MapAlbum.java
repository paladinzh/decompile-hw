package com.huawei.gallery.map.data;

import android.content.res.Resources;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.data.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapAlbum extends MediaSet implements ContentListener, IClusterInfo {
    public final double latitude;
    public final double longitude;
    private final DataManager mDataManager;
    public ArrayList<Path> mItems = new ArrayList();
    private final MapLatLng mLocation;
    private final MapAlbumSet mParentSet;
    private final Resources mResource;

    public MapAlbum(GalleryApp app, Path path, long version) {
        super(path, version);
        String[] locationInfo = path.getSuffix().split(" ");
        this.longitude = Double.parseDouble(locationInfo[0]);
        this.latitude = Double.parseDouble(locationInfo[1]);
        this.mLocation = new MapLatLng(this.latitude, this.longitude);
        this.mDataManager = app.getDataManager();
        this.mParentSet = (MapAlbumSet) this.mDataManager.getMediaSet(path.getParent());
        this.mParentSet.addContentListener(this);
        this.mResource = app.getResources();
    }

    public MapLatLng getLocation() {
        return this.mLocation;
    }

    public boolean isLeafAlbum() {
        return true;
    }

    public static String getPathSuffix(double lng, double lat) {
        return String.format(Locale.US, "%.5f %.5f", new Object[]{Double.valueOf(lng), Double.valueOf(lat)});
    }

    public String getName() {
        return String.format(this.mResource.getQuantityString(R.plurals.photo_count, getMediaItemCount()), new Object[]{Integer.valueOf(count)});
    }

    public int getMediaItemCount() {
        return this.mItems.size();
    }

    public List<Path> getAllItems() {
        return new ArrayList(this.mItems);
    }

    public boolean isEmpty() {
        return this.mItems.size() == 0;
    }

    public long reload() {
        if (this.mParentSet.reload() > this.mDataVersion) {
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public void onContentDirty() {
        notifyContentChanged();
    }

    public MediaItem getCoverMediaItem() {
        if (isEmpty()) {
            return null;
        }
        return (MediaItem) this.mDataManager.getMediaObject((Path) this.mItems.get(0));
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        int i = 0;
        if (start >= this.mItems.size()) {
            return new ArrayList();
        }
        int end = Math.min(start + count, this.mItems.size());
        final MediaItem[] buf = new MediaItem[(end - start)];
        this.mDataManager.mapMediaItems(new ArrayList(this.mItems.subList(start, end)), new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                buf[index] = item;
            }
        }, 0);
        ArrayList<MediaItem> result = new ArrayList();
        int length = buf.length;
        while (i < length) {
            MediaItem item = buf[i];
            if (item != null) {
                result.add(item);
            }
            i++;
        }
        return result;
    }

    public boolean isItemInRect(MapItem item, double radius) {
        radius = (2.0d * radius) / 3.0d;
        double dx = item.longitude - this.longitude;
        double dy = item.latitude - this.latitude;
        if (dx < (-radius) || dx > radius || dy < (-radius) || dy > radius) {
            return false;
        }
        return true;
    }

    public void clear() {
        this.mItems.clear();
    }

    public void addAll(List<Path> itemList) {
        this.mItems.addAll(itemList);
    }
}
