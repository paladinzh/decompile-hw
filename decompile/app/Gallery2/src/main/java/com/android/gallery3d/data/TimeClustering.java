package com.android.gallery3d.data;

import android.content.Context;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.util.GalleryUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TimeClustering extends Clustering {
    private static int CLUSTER_SPLIT_MULTIPLIER = 3;
    static boolean USE_YYYYMM_ALGO = true;
    private static final Comparator<SmallItem> sDateComparator = new DateComparator();
    private long mClusterSplitTime = 3630000;
    private ArrayList<Cluster> mClusters;
    private Context mContext;
    private Cluster mCurrCluster;
    private int mKey = -1;
    private long mLargeClusterSplitTime = (this.mClusterSplitTime / 2);
    private int mMaxClusterSize = 35;
    private int mMinClusterSize = 11;
    private String[] mNames;

    private static class DateComparator implements Comparator<SmallItem> {
        private DateComparator() {
        }

        public int compare(SmallItem item1, SmallItem item2) {
            return -Utils.compare(item1.dateInMs, item2.dateInMs);
        }
    }

    public TimeClustering(Context context) {
        this.mContext = context;
        this.mClusters = new ArrayList();
        this.mCurrCluster = new Cluster();
    }

    public void run(MediaSet baseSet) {
        int i;
        int total = baseSet.getTotalMediaItemCount();
        final SmallItem[] buf = new SmallItem[total];
        final double[] latLng = new double[2];
        final int i2 = total;
        baseSet.enumerateTotalMediaItems(new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                if (index >= 0 && index < i2) {
                    SmallItem s = new SmallItem();
                    s.path = item.getPath();
                    s.dateInMs = item.getDateInMs();
                    item.getLatLong(latLng);
                    s.key = item.getNormalizedDate();
                    s.lat = latLng[0];
                    s.lng = latLng[1];
                    buf[index] = s;
                }
            }
        });
        ArrayList<SmallItem> items = new ArrayList(total);
        for (i = 0; i < total; i++) {
            if (buf[i] != null) {
                items.add(buf[i]);
            }
        }
        Collections.sort(items, sDateComparator);
        int n = items.size();
        long minTime = 0;
        long maxTime = 0;
        for (i = 0; i < n && !USE_YYYYMM_ALGO; i++) {
            long t = ((SmallItem) items.get(i)).dateInMs;
            if (t != 0) {
                if (minTime == 0) {
                    maxTime = t;
                    minTime = t;
                } else {
                    minTime = Math.min(minTime, t);
                    maxTime = Math.max(maxTime, t);
                }
            }
        }
        setTimeRange(maxTime - minTime, n);
        for (i = 0; i < n; i++) {
            compute((SmallItem) items.get(i));
        }
        compute(null);
        int m = this.mClusters.size();
        this.mNames = new String[m];
        for (i = 0; i < m; i++) {
            this.mNames[i] = ((Cluster) this.mClusters.get(i)).generateCaption(this.mContext);
        }
    }

    public int getNumberOfClusters() {
        return this.mClusters.size();
    }

    public ArrayList<Path> getCluster(int index) {
        ArrayList<SmallItem> items = ((Cluster) this.mClusters.get(index)).getItems();
        ArrayList<Path> result = new ArrayList(items.size());
        int n = items.size();
        for (int i = 0; i < n; i++) {
            result.add(((SmallItem) items.get(i)).path);
        }
        return result;
    }

    public String getClusterName(int index) {
        return this.mNames[index];
    }

    private void setTimeRange(long timeRange, int numItems) {
        if (!USE_YYYYMM_ALGO) {
            if (numItems != 0) {
                int meanItemsPerCluster = numItems / 9;
                this.mMinClusterSize = meanItemsPerCluster / 2;
                this.mMaxClusterSize = meanItemsPerCluster * 2;
                this.mClusterSplitTime = (timeRange / ((long) numItems)) * ((long) CLUSTER_SPLIT_MULTIPLIER);
            }
            this.mClusterSplitTime = Utils.clamp(this.mClusterSplitTime, 60000, 7200000);
            this.mLargeClusterSplitTime = this.mClusterSplitTime / 2;
            this.mMinClusterSize = Utils.clamp(this.mMinClusterSize, 8, 15);
            this.mMaxClusterSize = Utils.clamp(this.mMaxClusterSize, 20, 50);
        }
    }

    private void compute(SmallItem currentItem) {
        if (USE_YYYYMM_ALGO) {
            if (currentItem != null) {
                if (this.mKey == currentItem.key || this.mKey == -1) {
                    if (this.mKey == -1) {
                        this.mKey = currentItem.key;
                    }
                    this.mCurrCluster.addItem(currentItem);
                } else {
                    this.mClusters.add(this.mCurrCluster);
                    this.mKey = currentItem.key;
                    this.mCurrCluster = new Cluster();
                    this.mCurrCluster.addItem(currentItem);
                }
            } else if (this.mCurrCluster.size() != 0) {
                this.mClusters.add(this.mCurrCluster);
            }
            return;
        }
        int numClusters;
        int numCurrClusterItems;
        if (currentItem != null) {
            numClusters = this.mClusters.size();
            numCurrClusterItems = this.mCurrCluster.size();
            boolean geographicallySeparateItem = false;
            boolean itemAddedToCurrentCluster = false;
            if (numCurrClusterItems == 0) {
                this.mCurrCluster.addItem(currentItem);
            } else {
                SmallItem prevItem = this.mCurrCluster.getLastItem();
                if (isGeographicallySeparated(prevItem, currentItem)) {
                    this.mClusters.add(this.mCurrCluster);
                    geographicallySeparateItem = true;
                } else if (numCurrClusterItems > this.mMaxClusterSize) {
                    splitAndAddCurrentCluster();
                } else if (timeDistance(prevItem, currentItem) < this.mClusterSplitTime) {
                    this.mCurrCluster.addItem(currentItem);
                    itemAddedToCurrentCluster = true;
                } else if (numClusters <= 0 || numCurrClusterItems >= this.mMinClusterSize || this.mCurrCluster.mGeographicallySeparatedFromPrevCluster) {
                    this.mClusters.add(this.mCurrCluster);
                } else {
                    mergeAndAddCurrentCluster();
                }
                if (!itemAddedToCurrentCluster) {
                    this.mCurrCluster = new Cluster();
                    if (geographicallySeparateItem) {
                        this.mCurrCluster.mGeographicallySeparatedFromPrevCluster = true;
                    }
                    this.mCurrCluster.addItem(currentItem);
                }
            }
        } else if (this.mCurrCluster.size() > 0) {
            numClusters = this.mClusters.size();
            numCurrClusterItems = this.mCurrCluster.size();
            if (numCurrClusterItems > this.mMaxClusterSize) {
                splitAndAddCurrentCluster();
            } else if (numClusters <= 0 || numCurrClusterItems >= this.mMinClusterSize || this.mCurrCluster.mGeographicallySeparatedFromPrevCluster) {
                this.mClusters.add(this.mCurrCluster);
            } else {
                mergeAndAddCurrentCluster();
            }
            this.mCurrCluster = new Cluster();
        }
    }

    private void splitAndAddCurrentCluster() {
        ArrayList<SmallItem> currClusterItems = this.mCurrCluster.getItems();
        int numCurrClusterItems = this.mCurrCluster.size();
        int secondPartitionStartIndex = getPartitionIndexForCurrentCluster();
        if (secondPartitionStartIndex != -1) {
            int j;
            Cluster partitionedCluster = new Cluster();
            for (j = 0; j < secondPartitionStartIndex; j++) {
                partitionedCluster.addItem((SmallItem) currClusterItems.get(j));
            }
            this.mClusters.add(partitionedCluster);
            partitionedCluster = new Cluster();
            for (j = secondPartitionStartIndex; j < numCurrClusterItems; j++) {
                partitionedCluster.addItem((SmallItem) currClusterItems.get(j));
            }
            this.mClusters.add(partitionedCluster);
            return;
        }
        this.mClusters.add(this.mCurrCluster);
    }

    private int getPartitionIndexForCurrentCluster() {
        int partitionIndex = -1;
        float largestChange = 2.0f;
        ArrayList<SmallItem> currClusterItems = this.mCurrCluster.getItems();
        int numCurrClusterItems = this.mCurrCluster.size();
        int minClusterSize = this.mMinClusterSize;
        if (numCurrClusterItems > minClusterSize + 1) {
            for (int i = minClusterSize; i < numCurrClusterItems - minClusterSize; i++) {
                SmallItem prevItem = (SmallItem) currClusterItems.get(i - 1);
                SmallItem currItem = (SmallItem) currClusterItems.get(i);
                SmallItem nextItem = (SmallItem) currClusterItems.get(i + 1);
                long timeNext = nextItem.dateInMs;
                long timeCurr = currItem.dateInMs;
                long timePrev = prevItem.dateInMs;
                if (!(timeNext == 0 || timeCurr == 0 || timePrev == 0)) {
                    long diff1 = Math.abs(timeNext - timeCurr);
                    long diff2 = Math.abs(timeCurr - timePrev);
                    float change = Math.max(((float) diff1) / (((float) diff2) + 0.01f), ((float) diff2) / (((float) diff1) + 0.01f));
                    if (change > largestChange) {
                        if (timeDistance(currItem, prevItem) > this.mLargeClusterSplitTime) {
                            partitionIndex = i;
                            largestChange = change;
                        } else if (timeDistance(nextItem, currItem) > this.mLargeClusterSplitTime) {
                            partitionIndex = i + 1;
                            largestChange = change;
                        }
                    }
                }
            }
        }
        return partitionIndex;
    }

    private void mergeAndAddCurrentCluster() {
        int numClusters = this.mClusters.size();
        Cluster prevCluster = (Cluster) this.mClusters.get(numClusters - 1);
        ArrayList<SmallItem> currClusterItems = this.mCurrCluster.getItems();
        int numCurrClusterItems = this.mCurrCluster.size();
        if (prevCluster.size() < this.mMinClusterSize) {
            for (int i = 0; i < numCurrClusterItems; i++) {
                prevCluster.addItem((SmallItem) currClusterItems.get(i));
            }
            this.mClusters.set(numClusters - 1, prevCluster);
            return;
        }
        this.mClusters.add(this.mCurrCluster);
    }

    private static boolean isGeographicallySeparated(SmallItem itemA, SmallItem itemB) {
        if (!GalleryUtils.isValidLocation(itemA.lat, itemA.lng) || !GalleryUtils.isValidLocation(itemB.lat, itemB.lng)) {
            return false;
        }
        return GalleryUtils.toMile(GalleryUtils.fastDistanceMeters(Math.toRadians(itemA.lat), Math.toRadians(itemA.lng), Math.toRadians(itemB.lat), Math.toRadians(itemB.lng))) > 20.0d;
    }

    private static long timeDistance(SmallItem a, SmallItem b) {
        return Math.abs(a.dateInMs - b.dateInMs);
    }
}
