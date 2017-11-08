package com.android.gallery3d.data;

import android.content.Context;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ClusterWorker.ClusterListener;
import com.android.gallery3d.data.ClusterWorker.Proxy;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReverseGeocoder;
import com.android.gallery3d.util.ThreadPool.JobContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class LocationClusters implements Clusters {
    private static final Comparator<SmallItem> mDateComparator = new Comparator<SmallItem>() {
        public int compare(SmallItem item1, SmallItem item2) {
            return Utils.compare(item2.dateInMs, item1.dateInMs);
        }
    };
    private GalleryApp mApplication;
    private MediaSet mBaseSet;
    private ClusterWorker<SmallItem> mLocationClusterWorker;

    private static class SmallItem {
        long dateInMs;
        double lat;
        double lng;
        Path path;

        private SmallItem() {
        }
    }

    private static class SmallItemClient implements Proxy<SmallItem> {
        private final CaptionKey S_NO_LOCATION;
        private Map<SmallItem, CaptionKey> mCachedCaption = new HashMap();
        private Context mContext;
        ReverseGeocoder mGeocoder;

        static class CaptionKey {
            String caption;
            String key;

            CaptionKey() {
            }
        }

        SmallItemClient(Context context) {
            this.mContext = context;
            this.mGeocoder = new ReverseGeocoder(this.mContext);
            this.S_NO_LOCATION = new CaptionKey();
            this.S_NO_LOCATION.key = "-1";
            this.S_NO_LOCATION.caption = context.getString(R.string.no_location);
        }

        public Path getPath(SmallItem item) {
            return item.path;
        }

        public String generateCaption(SmallItem item) {
            return findCaptionKey(item).caption;
        }

        public String getClusterKey(SmallItem item) {
            return findCaptionKey(item).key;
        }

        private CaptionKey findCaptionKey(SmallItem item) {
            if (!GalleryUtils.isValidLocation(item.lat, item.lng)) {
                return this.S_NO_LOCATION;
            }
            CaptionKey captionKey = (CaptionKey) this.mCachedCaption.get(item);
            if (captionKey != null) {
                return captionKey;
            }
            Map<String, CaptionKey> captionCache = new HashMap();
            for (SmallItem s : this.mCachedCaption.keySet()) {
                captionKey = (CaptionKey) this.mCachedCaption.get(s);
                if ((item.lat == s.lat && item.lng == s.lng) || ((double) ((float) GalleryUtils.fastDistanceMeters(Math.toRadians(s.lat), Math.toRadians(s.lng), Math.toRadians(item.lat), Math.toRadians(item.lng)))) < 3000.0d) {
                    return captionKey;
                }
                captionCache.put(captionKey.caption, captionKey);
            }
            String cpt = this.mGeocoder.getAddress(item.lat, item.lng);
            if (cpt == null || "".equals(cpt)) {
                captionKey = this.S_NO_LOCATION;
            } else if (captionCache.containsKey(cpt)) {
                captionKey = (CaptionKey) captionCache.get(cpt);
            } else {
                captionKey = new CaptionKey();
                captionKey.key = String.format("%s-%s", new Object[]{Double.valueOf(item.lat), Double.valueOf(item.lng)});
                captionKey.caption = cpt;
            }
            captionCache.clear();
            if (!captionKey.equals(this.S_NO_LOCATION)) {
                this.mCachedCaption.put(item, captionKey);
            }
            return captionKey;
        }
    }

    public LocationClusters(GalleryApp application, ClusterAlbumSet parent, MediaSet mediaSet, Path root) {
        this.mApplication = application;
        this.mBaseSet = mediaSet;
        this.mLocationClusterWorker = new ClusterWorker(application.getDataManager(), parent, root);
        this.mLocationClusterWorker.setName("LocationClusters");
        this.mLocationClusterWorker.setItemComparator(mDateComparator);
        this.mLocationClusterWorker.setAlbumCompartor(mDateComparator);
        this.mLocationClusterWorker.setItemProxy(new SmallItemClient(application.getAndroidContext()));
        this.mLocationClusterWorker.start();
    }

    public ArrayList<Path> getMediaItems(int index) {
        return (ArrayList) this.mLocationClusterWorker.get(index).getMediaItems().clone();
    }

    public ClusterAlbum get(int index) {
        return this.mLocationClusterWorker.get(index);
    }

    public void remove(int index) {
        this.mLocationClusterWorker.remove(index);
    }

    public int size() {
        return this.mLocationClusterWorker.size();
    }

    public Future<Void> reload() {
        return this.mApplication.getThreadPool().submit(new BaseJob<Void>() {
            public Void run(final JobContext jc) {
                if (jc.isCancelled()) {
                    return null;
                }
                final int total = LocationClusters.this.mBaseSet.getTotalMediaItemCount();
                final double[] latLng = new double[2];
                LocationClusters.this.mLocationClusterWorker.clear();
                LocationClusters.this.mLocationClusterWorker.setDone(false);
                LocationClusters.this.mBaseSet.enumerateTotalMediaItems(new ItemConsumer() {
                    public void consume(int index, MediaItem item) {
                        if (index >= 0 && index < total) {
                            if (jc.isCancelled()) {
                                jc.setMode(0);
                                return;
                            }
                            SmallItem s = new SmallItem();
                            s.path = item.getPath();
                            s.dateInMs = item.getDateInMs();
                            item.getLatLong(latLng);
                            s.lat = latLng[0];
                            s.lng = latLng[1];
                            LocationClusters.this.mLocationClusterWorker.addItem(s);
                        }
                    }
                });
                LocationClusters.this.mLocationClusterWorker.setDone(true);
                return null;
            }

            public boolean isHeavyJob() {
                return true;
            }

            public String workContent() {
                return "cluster picture in location.";
            }
        });
    }

    public void setClusterListener(ClusterListener l) {
        this.mLocationClusterWorker.setClusterListener(l);
    }
}
