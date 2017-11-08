package com.huawei.gallery.map.data;

import android.net.Uri;
import android.provider.MediaStore.Files;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.GalleryImage;
import com.android.gallery3d.data.GalleryVideo;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.map.app.MapConverter;
import com.huawei.gallery.map.app.MapFragmentBase.ClusterRadiusAndDate;
import com.huawei.gallery.map.app.MapManager;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapAlbumSet extends MediaSet implements ContentListener {
    private static final Uri EXTERNAL_FILE_URI = Files.getContentUri("external");
    private static final MyPrinter LOG = new MyPrinter("MapAlbumSet");
    private static final String[] PROJECTION = new String[]{"local_media_id", "media_type", "latitude", "longitude", "showDateToken", "thumbType", "_id"};
    private final GalleryApp mApplication;
    private final MediaSet mBaseSet;
    private double mClusterRadius = -1.0d;
    private MapDataListener mDataListener;
    private SimpleDateFormat mDateFormater = new SimpleDateFormat("yyyyMMdd");
    private int mMapButtonType = 1;
    private ArrayList<MapAlbum> mMapClusterArray = new ArrayList();
    private ArrayList<MapItem> mMapItemArray = new ArrayList();
    private boolean mModeChanged = true;
    private String mWhereQueryClause;

    public interface MapDataListener {
        void onLoadFinish(List<ClusterInfo> list, long j, double d);

        void sendItemRange(List<double[]> list);

        void sendNewestPicture(double d, double d2);
    }

    private static class SmallItem {
        final long dateTaken;
        final double lat;
        final double lng;
        final Path path;

        SmallItem(Path pth, double lati, double lngi, long timeInMs) {
            this.path = pth;
            this.lat = lati;
            this.lng = lngi;
            this.dateTaken = timeInMs;
        }
    }

    public MapAlbumSet(GalleryApp app, Path path, MediaSet baseSet, long version) {
        super(path, version);
        this.mApplication = app;
        this.mWhereQueryClause = baseSet.getWhereQueryClause();
        this.mBaseSet = baseSet;
        this.mBaseSet.addContentListener(this);
    }

    public void setDataListener(MapDataListener listener) {
        LOG.d(false, "reload type " + this.mMapButtonType);
        this.mDataListener = listener;
    }

    public String getName() {
        LOG.d(false, "reload type " + this.mMapButtonType);
        return "";
    }

    public long reload() {
        ClusterRadiusAndDate cluster = MapManager.getInstance().getClusterRadius();
        if (cluster == null) {
            LOG.d("cluster is null, can't reload !!! ");
            return this.mDataVersion;
        }
        boolean baseSetChanged;
        if (this.mBaseSet.reload() > this.mDataVersion) {
            baseSetChanged = true;
        } else {
            baseSetChanged = false;
        }
        if (baseSetChanged) {
            this.mWhereQueryClause = this.mBaseSet.getWhereQueryClause();
        }
        if (baseSetChanged || this.mModeChanged) {
            this.mMapItemArray = reloadAllItems(cluster.clusterDate);
            this.mModeChanged = false;
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        this.mClusterRadius = cluster.clusterRadius;
        List<ClusterInfo> result = doCluster(getPath());
        if (this.mDataListener != null) {
            this.mDataListener.onLoadFinish(result, this.mDataVersion, this.mClusterRadius);
        } else {
            for (ClusterInfo clusterInfo : result) {
                clusterInfo.addToTarget();
            }
        }
        return this.mDataVersion;
    }

    private List<ClusterInfo> doCluster(Path basePath) {
        List<MapItem> mapItemList = new ArrayList(this.mMapItemArray);
        List<MapAlbumData> oldAlbumList = MapAlbumData.deepCopyOf(this.mMapClusterArray);
        this.mMapClusterArray.clear();
        List<MapAlbum> newAlbumList = new ArrayList();
        Map<String, ClusterInfo> clusters = new HashMap();
        String clusterKeyFormat = "%s-%s";
        ClusterManager.clear();
        double radius = this.mClusterRadius;
        for (MapItem item : mapItemList) {
            MapAlbumData oldAlbum = findOldAlbum(oldAlbumList, item);
            MapAlbum newAlbum = findNewAlbum(newAlbumList, item, radius);
            MapLatLng from = null;
            if (oldAlbum != null) {
                from = oldAlbum.getLocation();
            }
            if (newAlbum == null) {
                newAlbum = findHistoryAlbum(oldAlbumList, item, radius);
                Path path = basePath.getChild(MapAlbum.getPathSuffix(item.longitude, item.latitude));
                if (newAlbum == null) {
                    newAlbum = (MapAlbum) this.mApplication.getDataManager().peekMediaObject(path);
                }
                if (newAlbum == null) {
                    newAlbum = new MapAlbum(this.mApplication, path, -1);
                }
                newAlbum.clear();
                newAlbumList.add(newAlbum);
                ClusterManager.add(newAlbum);
            }
            String clusterKey = String.format(clusterKeyFormat, new Object[]{from, newAlbum.getLocation()});
            ClusterInfo info = (ClusterInfo) clusters.get(clusterKey);
            if (info == null) {
                info = new ClusterInfo(this.mApplication.getDataManager(), from, to, newAlbum);
                clusters.put(clusterKey, info);
                ClusterManager.add(info);
            }
            ClusterManager.add(newAlbum, item.path);
            info.addItem(item.path);
        }
        for (MapAlbumData data : oldAlbumList) {
            data.clearSourceAlbum();
        }
        this.mMapClusterArray.addAll(newAlbumList);
        LOG.d("cluster count is " + clusters.size());
        return new ArrayList(clusters.values());
    }

    private MapAlbumData findOldAlbum(List<MapAlbumData> albumList, MapItem item) {
        LOG.d(false, "reload type " + this.mMapButtonType);
        for (MapAlbumData album : albumList) {
            if (album.contains(item.path)) {
                return album;
            }
        }
        return null;
    }

    private MapAlbum findHistoryAlbum(List<MapAlbumData> albumList, MapItem item, double radius) {
        LOG.d(false, "reload type " + this.mMapButtonType);
        for (MapAlbumData data : albumList) {
            MapAlbum album = data.getSourceAlbum();
            if (album != null && album.isItemInRect(item, radius)) {
                return album;
            }
        }
        return null;
    }

    private MapAlbum findNewAlbum(List<MapAlbum> albumList, MapItem item, double radius) {
        LOG.d(false, "reload type " + this.mMapButtonType);
        for (MapAlbum album : albumList) {
            if (album.isItemInRect(item, radius)) {
                return album;
            }
        }
        return null;
    }

    protected void setShowArea(int mapButtonType) {
        this.mMapButtonType = mapButtonType;
        this.mModeChanged = true;
    }

    private ArrayList<MapItem> reloadAllItems(String clusterDate) {
        ArrayList<SmallItem> itemArray = loadFromDatabase();
        ArrayList<MapItem> mapItemArray = new ArrayList();
        ArrayList<double[]> location = new ArrayList();
        long startDateInMs = Long.MIN_VALUE;
        long endDateInMs = Long.MAX_VALUE;
        if (clusterDate != null && this.mMapButtonType == 1) {
            String endDate;
            String[] dateRange = clusterDate.split("-");
            String endDate2 = dateRange[0];
            String startDate = endDate2;
            if (dateRange.length >= 2) {
                endDate = dateRange[1];
            } else {
                endDate = endDate2;
            }
            try {
                startDateInMs = this.mDateFormater.parse(endDate2).getTime();
                endDateInMs = this.mDateFormater.parse(endDate).getTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(endDateInMs);
                calendar.add(6, 1);
                endDateInMs = calendar.getTimeInMillis();
            } catch (Exception e) {
                GalleryLog.w("MapAlbumSet", "parse date range for group failed. " + e.getMessage());
            }
        }
        MapLatLng newestPoint = null;
        MapLatLng latLng = new MapLatLng();
        for (SmallItem item : itemArray) {
            if (GalleryUtils.isValidLocation(item.lat, item.lng)) {
                latLng = MapConverter.transform(item.lat, item.lng, latLng);
                location.add(new double[]{latLng.latitude, latLng.longitude});
                if (item.dateTaken >= startDateInMs && item.dateTaken < endDateInMs) {
                    if (this.mDataListener != null && r21 == null) {
                        newestPoint = latLng;
                        this.mDataListener.sendNewestPicture(latLng.latitude, latLng.longitude);
                    }
                    mapItemArray.add(new MapItem(item.path, latLng.latitude, latLng.longitude));
                }
            }
        }
        if (this.mDataListener != null) {
            this.mDataListener.sendItemRange(location);
        }
        return mapItemArray;
    }

    private ArrayList<SmallItem> loadFromDatabase() {
        ArrayList<SmallItem> mapItemArray = new ArrayList();
        Closeable closeable = null;
        try {
            closeable = this.mApplication.getContentResolver().query(GalleryMedia.URI, PROJECTION, this.mWhereQueryClause, this.mBaseSet.getWhereQueryClauseArgs(0), "showDateToken DESC");
            if (closeable == null) {
                GalleryLog.w("MapAlbumSet", "query fail: ");
                return mapItemArray;
            }
            while (closeable.moveToNext()) {
                double lat = closeable.getDouble(2);
                double lng = closeable.getDouble(3);
                int id = closeable.getInt(0);
                int mediaType = closeable.getInt(1);
                long dateTaken = closeable.getLong(4);
                int thumbType = closeable.getInt(5);
                int galleryId = closeable.getInt(6);
                if (id != -1 || thumbType >= 1) {
                    mapItemArray.add(new SmallItem(obtainPath(galleryId, mediaType), lat, lng, dateTaken));
                }
            }
            Utils.closeSilently(closeable);
            return mapItemArray;
        } catch (SecurityException e) {
            GalleryLog.w("MapAlbumSet", "can not loadFromDatabase because of no permission.");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    private static Path obtainPath(int id, int mediaType) {
        return (mediaType == 1 ? GalleryImage.IMAGE_PATH : GalleryVideo.VIDEO_PATH).getChild(id);
    }

    public void onContentDirty() {
        LOG.d(false, "reload type " + this.mMapButtonType);
        notifyContentChanged();
    }
}
