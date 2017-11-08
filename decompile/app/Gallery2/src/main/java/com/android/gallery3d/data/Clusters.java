package com.android.gallery3d.data;

import com.android.gallery3d.data.ClusterWorker.ClusterListener;
import com.android.gallery3d.util.Future;
import java.util.ArrayList;

public interface Clusters {
    ClusterAlbum get(int i);

    ArrayList<Path> getMediaItems(int i);

    Future<Void> reload();

    void remove(int i);

    void setClusterListener(ClusterListener clusterListener);

    int size();
}
