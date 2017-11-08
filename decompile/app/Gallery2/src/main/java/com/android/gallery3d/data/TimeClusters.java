package com.android.gallery3d.data;

import android.content.Context;
import android.text.format.DateUtils;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ClusterWorker.ClusterListener;
import com.android.gallery3d.data.ClusterWorker.Proxy;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.JobContext;
import java.util.ArrayList;
import java.util.Comparator;

public class TimeClusters implements Clusters {
    private static final Comparator<SmallItem> mDataComparator = new Comparator<SmallItem>() {
        public int compare(SmallItem item1, SmallItem item2) {
            return Utils.compare(item2.dateInMs, item1.dateInMs);
        }
    };
    private GalleryApp mApplication;
    private MediaSet mBaseSet;
    private ClusterWorker<SmallItem> mTimeClusterWorker;

    private static class SmallItem {
        long dateInMs;
        int key;
        Path path;

        private SmallItem() {
        }
    }

    private static class SmallItemClient implements Proxy<SmallItem> {
        private Context mContext;

        SmallItemClient(Context context) {
            this.mContext = context;
        }

        public Path getPath(SmallItem item) {
            return item.path;
        }

        public String generateCaption(SmallItem item) {
            return DateUtils.formatDateTime(this.mContext, item.dateInMs, 36);
        }

        public String getClusterKey(SmallItem item) {
            return String.valueOf(item.key);
        }
    }

    public TimeClusters(GalleryApp application, ClusterAlbumSet parent, MediaSet mediaSet, Path root) {
        this.mApplication = application;
        this.mBaseSet = mediaSet;
        this.mTimeClusterWorker = new ClusterWorker(application.getDataManager(), parent, root);
        this.mTimeClusterWorker.setName("TimeCluster");
        this.mTimeClusterWorker.setItemComparator(mDataComparator);
        this.mTimeClusterWorker.setItemProxy(new SmallItemClient(application.getAndroidContext()));
        this.mTimeClusterWorker.start();
    }

    public ArrayList<Path> getMediaItems(int index) {
        return (ArrayList) this.mTimeClusterWorker.get(index).getMediaItems().clone();
    }

    public int size() {
        return this.mTimeClusterWorker.size();
    }

    public Future<Void> reload() {
        return this.mApplication.getThreadPool().submit(new BaseJob<Void>() {
            public Void run(final JobContext jc) {
                if (jc.isCancelled()) {
                    return null;
                }
                TimeClusters.this.mTimeClusterWorker.clear();
                TimeClusters.this.mTimeClusterWorker.setDone(false);
                final int total = TimeClusters.this.mBaseSet.getTotalMediaItemCount();
                TimeClusters.this.mBaseSet.enumerateTotalMediaItems(new ItemConsumer() {
                    public void consume(int index, MediaItem item) {
                        if (index >= 0 && index < total) {
                            if (jc.isCancelled()) {
                                jc.setMode(0);
                                GalleryLog.v("TimeCluster", "job cancelled");
                                return;
                            }
                            SmallItem s = new SmallItem();
                            s.path = item.getPath();
                            s.dateInMs = item.getDateInMs();
                            s.key = item.getNormalizedDate();
                            TimeClusters.this.mTimeClusterWorker.addItem(s);
                        }
                    }
                });
                TimeClusters.this.mTimeClusterWorker.setDone(true);
                return null;
            }

            public String workContent() {
                return "cluster photo in time.";
            }
        });
    }

    public void remove(int index) {
        this.mTimeClusterWorker.remove(index);
    }

    public void setClusterListener(ClusterListener l) {
        this.mTimeClusterWorker.setClusterListener(l);
    }

    public ClusterAlbum get(int index) {
        return this.mTimeClusterWorker.get(index);
    }
}
