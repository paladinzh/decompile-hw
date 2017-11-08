package com.huawei.gallery.map.data;

import com.android.gallery3d.data.Path;
import java.util.ArrayList;
import java.util.List;

public class MapAlbumData {
    private List<Path> mItems;
    private MapLatLng mLocation;
    private Path mPath;
    private MapAlbum mSource;

    public MapAlbumData(MapAlbum album) {
        this.mSource = album;
        this.mPath = album.getPath();
        this.mItems = album.getAllItems();
        this.mLocation = album.getLocation();
        this.mItems.addAll(ClusterManager.get(album));
    }

    public MapAlbum getSourceAlbum() {
        return this.mSource;
    }

    public boolean contains(Path path) {
        return this.mItems.contains(path);
    }

    public MapLatLng getLocation() {
        return this.mLocation;
    }

    public void clearSourceAlbum() {
        this.mSource.clear();
    }

    public String toString() {
        return this.mItems.toString();
    }

    public static List<MapAlbumData> deepCopyOf(List<MapAlbum> albumlist) {
        if (albumlist == null) {
            return new ArrayList(0);
        }
        List<MapAlbumData> result = new ArrayList(albumlist.size());
        for (MapAlbum album : albumlist) {
            result.add(new MapAlbumData(album));
        }
        return result;
    }
}
