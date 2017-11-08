package com.android.gallery3d.data;

import android.content.Context;
import android.content.res.Resources;
import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import java.util.ArrayList;
import tmsdk.common.module.update.UpdateConfig;

public class SizeClustering extends Clustering {
    private static final long[] SIZE_LEVELS = new long[]{0, 1048576, 10485760, 104857600, UpdateConfig.UPDATE_FLAG_VIRUS_BASE, UpdateConfig.UPDATE_FLAG_SYSTEM_SCAN_CONFIG, 4294967296L};
    private ArrayList<Path>[] mClusters;
    private Context mContext;
    private long[] mMinSizes;
    private String[] mNames;

    public SizeClustering(Context context) {
        this.mContext = context;
    }

    public void run(MediaSet baseSet) {
        int i;
        final ArrayList<Path>[] group = new ArrayList[SIZE_LEVELS.length];
        baseSet.enumerateTotalMediaItems(new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                long size = item.getSize();
                int i = 0;
                while (i < SizeClustering.SIZE_LEVELS.length - 1 && size >= SizeClustering.SIZE_LEVELS[i + 1]) {
                    i++;
                }
                ArrayList<Path> list = group[i];
                if (list == null) {
                    list = new ArrayList();
                    group[i] = list;
                }
                list.add(item.getPath());
            }
        });
        int count = 0;
        for (ArrayList<Path> arrayList : group) {
            if (arrayList != null) {
                count++;
            }
        }
        this.mClusters = new ArrayList[count];
        this.mNames = new String[count];
        this.mMinSizes = new long[count];
        Resources res = this.mContext.getResources();
        int k = 0;
        for (i = group.length - 1; i >= 0; i--) {
            if (group[i] != null) {
                this.mClusters[k] = group[i];
                if (i == 0) {
                    this.mNames[k] = String.format(res.getString(R.string.size_below), new Object[]{getSizeString(i + 1)});
                } else if (i == group.length - 1) {
                    this.mNames[k] = String.format(res.getString(R.string.size_above), new Object[]{getSizeString(i)});
                } else {
                    String minSize = getSizeString(i);
                    String maxSize = getSizeString(i + 1);
                    this.mNames[k] = String.format(res.getString(R.string.size_between), new Object[]{minSize, maxSize});
                }
                this.mMinSizes[k] = SIZE_LEVELS[i];
                k++;
            }
        }
    }

    private String getSizeString(int index) {
        long bytes = SIZE_LEVELS[index];
        if (bytes >= UpdateConfig.UPDATE_FLAG_VIRUS_BASE) {
            return (bytes / UpdateConfig.UPDATE_FLAG_VIRUS_BASE) + "GB";
        }
        return (bytes / 1048576) + "MB";
    }

    public int getNumberOfClusters() {
        return this.mClusters.length;
    }

    public ArrayList<Path> getCluster(int index) {
        return this.mClusters[index];
    }

    public String getClusterName(int index) {
        return this.mNames[index];
    }

    public long getMinSize(int index) {
        return this.mMinSizes[index];
    }
}
