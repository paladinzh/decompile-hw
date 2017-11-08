package com.huawei.gallery.story.utils;

import com.huawei.gallery.util.MyPrinter;
import java.util.ArrayList;
import java.util.List;

public class DBSCANClustering {
    private static final MyPrinter LOG = new MyPrinter("DBSCANClustering");
    private List<List<Long>> mClusters = new ArrayList();
    private List<Long> mCores = new ArrayList();
    private int mMinPts;
    private Long[] mPointsToCluster;
    private long mRadius;

    public DBSCANClustering(long radius, int minPts) {
        this.mRadius = radius;
        this.mMinPts = minPts;
    }

    private long countDistance(Long point1, Long point2) {
        return Math.abs(point1.longValue() - point2.longValue());
    }

    private List<Long> findCores(Long[] points, int minpts, long radius) {
        List<Long> coreItems = new ArrayList();
        for (int i = 0; i < points.length; i++) {
            int pts = 0;
            for (Long countDistance : points) {
                if (countDistance(points[i], countDistance) < radius) {
                    pts++;
                }
            }
            if (pts >= minpts) {
                coreItems.add(points[i]);
            }
        }
        return coreItems;
    }

    private void densityConnected(Long[] points, Long core, int clusterNum) {
        this.mCores.remove(core);
        int i = 0;
        while (i < points.length) {
            boolean isneighbour = false;
            boolean isputToCluster = false;
            for (List<Long> cluster : this.mClusters) {
                if (cluster.contains(points[i])) {
                    isputToCluster = true;
                    break;
                }
            }
            if (!isputToCluster) {
                if (countDistance(points[i], core) < this.mRadius) {
                    ((List) this.mClusters.get(clusterNum)).add(points[i]);
                    isneighbour = true;
                }
                if (isneighbour && this.mCores.contains(points[i])) {
                    this.mCores.remove(points[i]);
                    densityConnected(points, points[i], clusterNum);
                }
            }
            i++;
        }
    }

    private void putCoreToCluster() {
        int clusterNum = 0;
        this.mClusters.clear();
        for (int i = 0; i < this.mCores.size(); i++) {
            this.mClusters.add(new ArrayList());
            ((List) this.mClusters.get(clusterNum)).add((Long) this.mCores.get(i));
            densityConnected(this.mPointsToCluster, (Long) this.mCores.get(i), clusterNum);
            clusterNum++;
        }
    }

    public List<List<Long>> getCluster(Long[] data) {
        this.mPointsToCluster = (Long[]) data.clone();
        this.mCores = findCores(this.mPointsToCluster, this.mMinPts, this.mRadius);
        putCoreToCluster();
        return this.mClusters;
    }
}
