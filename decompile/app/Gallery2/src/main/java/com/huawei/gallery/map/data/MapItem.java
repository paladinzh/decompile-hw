package com.huawei.gallery.map.data;

import com.android.gallery3d.data.Path;

public class MapItem {
    public final long dateTakenInMs = 0;
    public final double latitude;
    public final double longitude;
    public final Path path;

    public MapItem(Path path, double latitude, double longitude) {
        this.path = path;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean equals(Object other) {
        if (other instanceof MapItem) {
            return ((MapItem) other).path.equalsIgnoreCase(this.path.toString());
        }
        return false;
    }
}
