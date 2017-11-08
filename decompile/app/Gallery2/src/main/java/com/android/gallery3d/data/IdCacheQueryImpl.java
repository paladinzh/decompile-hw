package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.net.Uri;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import java.io.Closeable;
import java.util.ArrayList;

public class IdCacheQueryImpl {
    private static final String[] ID = new String[]{"_id"};

    public static void setIdCache(ContentResolver resolver, Uri uri, String queryClause, String[] queryClauseArgs, String orderByParams, ArrayList<Integer> idCache, String tag) {
        GalleryUtils.assertNotInRenderThread();
        Closeable closeable = null;
        try {
            closeable = resolver.query(uri, ID, queryClause, queryClauseArgs, orderByParams);
            if (closeable == null) {
                GalleryLog.w(tag, "query fail");
                return;
            }
            while (closeable.moveToNext()) {
                idCache.add(Integer.valueOf(closeable.getInt(0)));
            }
            Utils.closeSilently(closeable);
        } catch (SecurityException e) {
            GalleryLog.w(tag, "No permission to query!");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static String translateQueryClause(ArrayList<Integer> idCache, int start, int count) {
        StringBuffer sb = new StringBuffer();
        int i = start;
        while (i < idCache.size() - 1 && i < (start + count) - 1) {
            sb.append(idCache.get(i));
            sb.append(",");
            i++;
        }
        sb.append(idCache.get(i));
        return "_id in (" + sb.toString() + ")";
    }

    public static ArrayList<MediaItem> checkMediaItemsOrder(ArrayList<MediaItem> list, ArrayList<Integer> idCache, int start) {
        int i;
        int size = list.size();
        boolean isInOder = true;
        for (i = 0; i < size; i++) {
            if (((Integer) idCache.get(start + i)).intValue() != ((MediaItem) list.get(i)).getId()) {
                isInOder = false;
                break;
            }
        }
        if (isInOder) {
            return list;
        }
        int j = 0;
        ArrayList<MediaItem> mediaItems = new ArrayList();
        for (i = start; i < start + size; i++) {
            while (j < list.size()) {
                if (((Integer) idCache.get(i)).intValue() == ((MediaItem) list.get(j)).getId()) {
                    mediaItems.add((MediaItem) list.remove(j));
                    j = 0;
                    break;
                }
                j++;
            }
        }
        return mediaItems;
    }

    public static boolean isIdCacheReady(ArrayList<Integer> idCache, int start, int count) {
        if (idCache.size() == 0 || start + count > idCache.size()) {
            return false;
        }
        return true;
    }

    public static boolean resetIdCache(ArrayList<Integer> idCache) {
        idCache.clear();
        return true;
    }
}
