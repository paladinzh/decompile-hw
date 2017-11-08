package com.android.gallery3d.data;

import android.content.Context;
import android.os.Process;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReverseGeocoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

class LocationClustering extends Clustering {
    private static final Object LocationClusterLOCK = new Object();
    private GalleryApp mApplication;
    private ArrayList<LocationCluster> mClusters;
    private Context mContext;
    private boolean mFirstDivide = true;
    private LocationInfoListener mListener;
    private boolean mLoading = false;
    private LocationDivideThread mLocationDivideTask;
    private boolean mNeedReload = false;
    private String mNoLocationString;

    public interface LocationInfoListener {
        void onLocationInfoChange();

        void onUpdateAll();
    }

    private static class LocationCluster {
        public static final Comparator<LocationCluster> COMPARATOR = new Comparator<LocationCluster>() {
            public int compare(LocationCluster item1, LocationCluster item2) {
                return Utils.compare(((SmallItem) item2.mCluster.get(0)).dateInMs, ((SmallItem) item1.mCluster.get(0)).dateInMs);
            }
        };
        private ArrayList<SmallItem> mCluster;
        private String mName;

        public LocationCluster(ArrayList<SmallItem> cluster, String name) {
            this.mCluster = cluster;
            this.mName = name;
        }
    }

    private class LocationDivideThread extends Thread {
        private volatile boolean mActive = true;
        private MediaSet mBaseSet;

        public LocationDivideThread(MediaSet baseSet) {
            this.mBaseSet = baseSet;
        }

        public void run() {
            Process.setThreadPriority(10);
            while (this.mActive) {
                synchronized (LocationClustering.LocationClusterLOCK) {
                    LocationClustering.this.mClusters.clear();
                }
                LocationClustering.this.mNeedReload = false;
                int total = this.mBaseSet.getTotalMediaItemCount();
                final SmallItem[] buf = new SmallItem[total];
                try {
                    int i;
                    int j;
                    int position;
                    final int i2 = total;
                    final double[] dArr = new double[2];
                    this.mBaseSet.enumerateTotalMediaItems(new ItemConsumer() {
                        public void consume(int index, MediaItem item) {
                            if (index >= 0 && index < i2) {
                                SmallItem s = new SmallItem();
                                s.path = item.getPath();
                                item.getLatLong(dArr);
                                s.lat = dArr[0];
                                s.lng = dArr[1];
                                s.dateInMs = item.getDateInMs();
                                buf[index] = s;
                            }
                        }
                    });
                    ArrayList<SmallItem> withLatLong = new ArrayList();
                    ArrayList<SmallItem> withoutLatLong = new ArrayList();
                    ArrayList<Point> points = new ArrayList();
                    for (i = 0; i < total; i++) {
                        SmallItem s = buf[i];
                        if (s != null) {
                            if (GalleryUtils.isValidLocation(s.lat, s.lng)) {
                                withLatLong.add(s);
                                points.add(new Point(s.lat, s.lng));
                            } else {
                                withoutLatLong.add(s);
                            }
                        }
                    }
                    if (withoutLatLong.size() > 0) {
                        synchronized (LocationClustering.LocationClusterLOCK) {
                            LocationClustering.this.mClusters.add(new LocationCluster(new ArrayList(withoutLatLong), LocationClustering.this.mNoLocationString));
                        }
                        withoutLatLong.clear();
                        if (LocationClustering.this.mFirstDivide) {
                            LocationClustering.this.notifyContentChanged();
                        }
                    }
                    int size = withLatLong.size();
                    ArrayList<ArrayList<SmallItem>> originDivideList = new ArrayList();
                    for (i = 0; i < size; i++) {
                        SmallItem item = (SmallItem) withLatLong.get(i);
                        boolean listHasExist = false;
                        for (j = 0; j < originDivideList.size(); j++) {
                            SmallItem firstSmallItemInList = (SmallItem) ((ArrayList) originDivideList.get(j)).get(0);
                            if (firstSmallItemInList.lat == item.lat && firstSmallItemInList.lng == item.lng) {
                                ((ArrayList) originDivideList.get(j)).add(item);
                                listHasExist = true;
                                break;
                            }
                        }
                        if (!listHasExist) {
                            ArrayList<SmallItem> list = new ArrayList();
                            list.add(item);
                            originDivideList.add(list);
                        }
                    }
                    int[] hasAddedToCluster = new int[originDivideList.size()];
                    ArrayList<ArrayList<Integer>> secondDivide = new ArrayList();
                    for (i = 0; i < hasAddedToCluster.length; i++) {
                        if (hasAddedToCluster[i] == 0) {
                            SmallItem gpsPoint1 = (SmallItem) ((ArrayList) originDivideList.get(i)).get(0);
                            hasAddedToCluster[i] = 1;
                            ArrayList<Integer> list2 = new ArrayList();
                            list2.add(Integer.valueOf(i));
                            for (j = i + 1; j < hasAddedToCluster.length; j++) {
                                if (hasAddedToCluster[j] == 0) {
                                    SmallItem gpsPoint2 = (SmallItem) ((ArrayList) originDivideList.get(j)).get(0);
                                    if (((double) ((float) GalleryUtils.fastDistanceMeters(Math.toRadians(gpsPoint1.lat), Math.toRadians(gpsPoint1.lng), Math.toRadians(gpsPoint2.lat), Math.toRadians(gpsPoint2.lng)))) < 3000.0d) {
                                        list2.add(Integer.valueOf(j));
                                        hasAddedToCluster[j] = 1;
                                    }
                                }
                            }
                            secondDivide.add(list2);
                        }
                    }
                    ReverseGeocoder reverseGeocoder = new ReverseGeocoder(LocationClustering.this.mContext);
                    ArrayList<ArrayList<SmallItem>> clusters = new ArrayList();
                    if (size > 0) {
                        for (ArrayList<Integer> intList : secondDivide) {
                            ArrayList<SmallItem> tempSmallItem = new ArrayList();
                            for (Integer integer : intList) {
                                ArrayList<SmallItem> arrayList = tempSmallItem;
                                arrayList.addAll((Collection) originDivideList.get(integer.intValue()));
                            }
                            clusters.add(tempSmallItem);
                        }
                        withLatLong.clear();
                        for (ArrayList<SmallItem> cluster : clusters) {
                            String addrName = LocationClustering.generateName(((SmallItem) cluster.get(0)).lat, ((SmallItem) cluster.get(0)).lng, reverseGeocoder);
                            if (addrName == null) {
                                withoutLatLong.addAll(cluster);
                            } else if (addrName.isEmpty()) {
                                continue;
                            } else {
                                synchronized (LocationClustering.LocationClusterLOCK) {
                                    position = -1;
                                    for (i = 0; i < LocationClustering.this.mClusters.size(); i++) {
                                        if (((LocationCluster) LocationClustering.this.mClusters.get(i)).mName.equals(addrName)) {
                                            position = i;
                                            break;
                                        }
                                    }
                                    if (position != -1) {
                                        ((LocationCluster) LocationClustering.this.mClusters.get(position)).mCluster.addAll(cluster);
                                    } else {
                                        LocationClustering.this.mClusters.add(0, new LocationCluster(cluster, addrName));
                                    }
                                }
                                if (LocationClustering.this.mFirstDivide) {
                                    LocationClustering.this.notifyContentChanged();
                                }
                            }
                        }
                    }
                    if (withLatLong.size() > 0) {
                        withoutLatLong.addAll(withLatLong);
                    }
                    if (withoutLatLong.size() > 0) {
                        position = -1;
                        synchronized (LocationClustering.LocationClusterLOCK) {
                            for (i = 0; i < LocationClustering.this.mClusters.size(); i++) {
                                if (((LocationCluster) LocationClustering.this.mClusters.get(i)).mName.equals(LocationClustering.this.mNoLocationString)) {
                                    position = i;
                                    break;
                                }
                            }
                            if (position != -1) {
                                ((LocationCluster) LocationClustering.this.mClusters.get(position)).mCluster.addAll(withoutLatLong);
                            } else {
                                LocationClustering.this.mClusters.add(new LocationCluster(withoutLatLong, LocationClustering.this.mNoLocationString));
                            }
                        }
                        if (LocationClustering.this.mFirstDivide) {
                            LocationClustering.this.notifyContentChanged();
                        }
                    }
                    if (!LocationClustering.this.mNeedReload) {
                        synchronized (LocationClustering.LocationClusterLOCK) {
                            for (LocationCluster c : LocationClustering.this.mClusters) {
                                Collections.sort(c.mCluster, SmallItem.COMPARATOR);
                            }
                            LocationCluster locationCluster = null;
                            int clustersize = LocationClustering.this.mClusters.size();
                            if (clustersize > 0 && ((LocationCluster) LocationClustering.this.mClusters.get(clustersize - 1)).mName.equals(LocationClustering.this.mNoLocationString)) {
                                locationCluster = (LocationCluster) LocationClustering.this.mClusters.get(clustersize - 1);
                                LocationClustering.this.mClusters.remove(clustersize - 1);
                            }
                            Collections.sort(LocationClustering.this.mClusters, LocationCluster.COMPARATOR);
                            if (locationCluster != null) {
                                LocationClustering.this.mClusters.add(locationCluster);
                            }
                        }
                        LocationClustering.this.mLoading = false;
                        terminate();
                        LocationClustering.this.notifyDivideComplete();
                    }
                    LocationClustering.this.mFirstDivide = false;
                } catch (Exception e) {
                    GalleryLog.e("LocationClustering", " LocationDivideThread Exception" + e.toString());
                }
            }
        }

        public synchronized void terminate() {
            this.mActive = false;
        }
    }

    private static class Point {
        public double latRad;
        public double lngRad;

        public Point(double lat, double lng) {
            this.latRad = Math.toRadians(lat);
            this.lngRad = Math.toRadians(lng);
        }
    }

    private static class SmallItem {
        public static final Comparator<SmallItem> COMPARATOR = new Comparator<SmallItem>() {
            public int compare(SmallItem item1, SmallItem item2) {
                return Utils.compare(item2.dateInMs, item1.dateInMs);
            }
        };
        long dateInMs;
        double lat;
        double lng;
        Path path;

        private SmallItem() {
        }
    }

    private void notifyContentChanged() {
        this.mListener.onLocationInfoChange();
    }

    private void notifyDivideComplete() {
        this.mListener.onUpdateAll();
    }

    public void addLocationInfoListener(LocationInfoListener listener) {
        this.mListener = listener;
    }

    public String getNoLocationString() {
        return this.mNoLocationString;
    }

    public boolean isLoading() {
        return this.mLoading;
    }

    public LocationClustering(Context context, GalleryApp application) {
        this.mContext = context;
        this.mApplication = application;
        this.mNoLocationString = this.mContext.getResources().getString(R.string.no_location);
        this.mClusters = new ArrayList();
    }

    public void run(MediaSet baseSet) {
        if (this.mLoading) {
            this.mNeedReload = true;
            return;
        }
        if (this.mLocationDivideTask != null) {
            this.mLocationDivideTask.terminate();
        }
        this.mLocationDivideTask = new LocationDivideThread(baseSet);
        this.mLocationDivideTask.start();
        this.mLoading = true;
    }

    private static String generateName(double lat, double lng, ReverseGeocoder geocoder) {
        return geocoder.getAddress(lat, lng);
    }

    public int getNumberOfClusters() {
        int size;
        synchronized (LocationClusterLOCK) {
            size = this.mClusters.size();
        }
        return size;
    }

    public ArrayList<Path> getCluster(int index) {
        ArrayList<SmallItem> items;
        synchronized (LocationClusterLOCK) {
            items = ((LocationCluster) this.mClusters.get(index)).mCluster;
        }
        ArrayList<Path> result = new ArrayList(items.size());
        int n = items.size();
        for (int i = 0; i < n; i++) {
            result.add(((SmallItem) items.get(i)).path);
        }
        return result;
    }

    public void setFirstDivide(boolean value) {
        this.mFirstDivide = value;
    }

    public String getClusterName(int index) {
        String -get1;
        synchronized (LocationClusterLOCK) {
            -get1 = ((LocationCluster) this.mClusters.get(index)).mName;
        }
        return -get1;
    }

    public MediaItem getClusterCover(int index) {
        try {
            return (MediaItem) this.mApplication.getDataManager().getMediaObject(((SmallItem) ((LocationCluster) this.mClusters.get(index)).mCluster.get(0)).path);
        } catch (Exception e) {
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void deletePath(String name, Path path) {
        if (this.mClusters != null) {
            synchronized (LocationClusterLOCK) {
                for (int i = 0; i < this.mClusters.size(); i++) {
                    LocationCluster location = (LocationCluster) this.mClusters.get(i);
                    if (location.mName.equals(name)) {
                        ArrayList<SmallItem> itemList = location.mCluster;
                        int j = 0;
                        while (j < itemList.size()) {
                            if (((SmallItem) itemList.get(j)).path.equalsIgnoreCase(path.toString())) {
                                itemList.remove(j);
                                if (itemList.size() == 0) {
                                    this.mClusters.remove(i);
                                }
                            } else {
                                j++;
                            }
                        }
                        continue;
                    }
                }
            }
        }
    }
}
