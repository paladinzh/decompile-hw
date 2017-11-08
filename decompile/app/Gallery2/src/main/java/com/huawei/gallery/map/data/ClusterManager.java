package com.huawei.gallery.map.data;

import com.android.gallery3d.data.Path;
import com.huawei.gallery.util.MyPrinter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClusterManager {
    private static final MyPrinter LOG = new MyPrinter(ClusterManager.class.getSimpleName());
    private static Set<ClusterInfo> sAllClusters = new HashSet();
    private static Map<MapAlbum, List<Path>> sAllMapAlbums = new HashMap();
    private static Object sLockObject = new Object();

    public static void clear() {
        synchronized (sLockObject) {
            sAllClusters.clear();
            sAllMapAlbums.clear();
        }
    }

    public static void add(ClusterInfo info) {
        synchronized (sLockObject) {
            sAllClusters.add(info);
        }
    }

    public static void add(MapAlbum album) {
        synchronized (sLockObject) {
            if (((List) sAllMapAlbums.get(album)) == null) {
                sAllMapAlbums.put(album, null);
            }
        }
    }

    public static void add(MapAlbum album, Path item) {
        synchronized (sLockObject) {
            List<Path> items = (List) sAllMapAlbums.get(album);
            if (items == null) {
                items = new ArrayList();
                sAllMapAlbums.put(album, items);
            }
            items.add(item);
        }
    }

    public static List<Path> get(MapAlbum album) {
        List<Path> items;
        synchronized (sLockObject) {
            items = (List) sAllMapAlbums.get(album);
            if (items == null) {
                items = new ArrayList(0);
            }
        }
        return items;
    }

    public static boolean isValid(MapAlbum album) {
        boolean containsKey;
        synchronized (sLockObject) {
            containsKey = sAllMapAlbums.containsKey(album);
        }
        return containsKey;
    }

    public static void modifyMapAlbum(MapAlbum album) {
        synchronized (sLockObject) {
            List<Path> items = (List) sAllMapAlbums.get(album);
            if (items == null) {
                return;
            }
            album.clear();
            album.addAll(items);
        }
    }
}
