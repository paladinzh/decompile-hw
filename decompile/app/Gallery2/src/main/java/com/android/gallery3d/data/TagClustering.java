package com.android.gallery3d.data;

import android.content.Context;
import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TagClustering extends Clustering {
    private ArrayList<ArrayList<Path>> mClusters;
    private String[] mNames;
    private String mUntaggedString;

    public TagClustering(Context context) {
        this.mUntaggedString = context.getResources().getString(R.string.untagged);
    }

    public void run(MediaSet baseSet) {
        int i = 0;
        final TreeMap<String, ArrayList<Path>> map = new TreeMap();
        final ArrayList<Path> untagged = new ArrayList();
        baseSet.enumerateTotalMediaItems(new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                Path path = item.getPath();
                String[] tags = item.getTags();
                if (tags == null || tags.length == 0) {
                    untagged.add(path);
                    return;
                }
                for (String key : tags) {
                    ArrayList<Path> list = (ArrayList) map.get(key);
                    if (list == null) {
                        list = new ArrayList();
                        map.put(key, list);
                    }
                    list.add(path);
                }
            }
        });
        int m = map.size();
        this.mClusters = new ArrayList();
        if (untagged.size() > 0) {
            i = 1;
        }
        this.mNames = new String[(i + m)];
        int i2 = 0;
        for (Entry<String, ArrayList<Path>> entry : map.entrySet()) {
            int i3 = i2 + 1;
            this.mNames[i2] = (String) entry.getKey();
            this.mClusters.add((ArrayList) entry.getValue());
            i2 = i3;
        }
        if (untagged.size() > 0) {
            i3 = i2 + 1;
            this.mNames[i2] = this.mUntaggedString;
            this.mClusters.add(untagged);
            i2 = i3;
        }
    }

    public int getNumberOfClusters() {
        return this.mClusters.size();
    }

    public ArrayList<Path> getCluster(int index) {
        return (ArrayList) this.mClusters.get(index);
    }

    public String getClusterName(int index) {
        return this.mNames[index];
    }
}
