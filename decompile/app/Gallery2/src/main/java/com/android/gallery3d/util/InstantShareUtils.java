package com.android.gallery3d.util;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.huawei.gallery.provider.GalleryProvider;
import java.util.ArrayList;

public class InstantShareUtils {
    public static final Uri INSTANTSHARE_QUERY_URI = GalleryProvider.BASE_URI.buildUpon().appendPath("instantshare/*").build();

    public static Cursor query(Uri uri, String[] projectionIn, String selection, String[] selectionArgs, String sort) {
        String strPath = selectionArgs[0];
        if (strPath == null || strPath.isEmpty()) {
            return null;
        }
        Path mediaSetPath = Path.fromString(strPath);
        if (mediaSetPath == null) {
            return null;
        }
        MediaObject mediaObj = mediaSetPath.getObject();
        if (!(mediaObj instanceof MediaSet)) {
            return null;
        }
        MediaSet mediaSet = (MediaSet) mediaObj;
        long queryStartTime = System.currentTimeMillis();
        String queryType = (String) uri.getPathSegments().get(1);
        long startTime;
        MatrixCursor cursor;
        if ("count".equals(queryType)) {
            startTime = replaceMediaSetStartTime(mediaSet);
            cursor = new MatrixCursor(new String[]{"instantshare_count"});
            cursor.addRow(new Object[]{Integer.valueOf(mediaSet.getMediaItemCount())});
            restoreMediaSetStartTime(mediaSet, startTime);
            GalleryLog.d("InstantShareUtils", "query count cost " + (System.currentTimeMillis() - queryStartTime) + " ms");
            return cursor;
        } else if (!"items".equals(queryType)) {
            return null;
        } else {
            startTime = replaceMediaSetStartTime(mediaSet);
            int queryStart = Integer.parseInt(selectionArgs[1]);
            ArrayList<MediaItem> mediaItems = mediaSet.getMediaItem(queryStart, Math.min(Integer.parseInt(selectionArgs[2]), Math.max(mediaSet.getMediaItemCount() - queryStart, 0)));
            restoreMediaSetStartTime(mediaSet, startTime);
            cursor = new MatrixCursor(new String[]{"_data", "uri", "mime_type", "orientation"});
            for (MediaItem mediaItem : mediaItems) {
                String uri2;
                Uri itemUri = null;
                if ((mediaItem.getSupportedOperations() & 4) != 0) {
                    itemUri = mediaItem.getContentUri();
                }
                if (itemUri != null) {
                    uri2 = itemUri.toString();
                } else {
                    uri2 = null;
                }
                cursor.addRow(new Object[]{mediaItem.getFilePath(), uri2, mediaItem.getMimeType(), Integer.valueOf(mediaItem.getRotation())});
            }
            GalleryLog.d("InstantShareUtils", "query items cost " + (System.currentTimeMillis() - queryStartTime) + " ms");
            return cursor;
        }
    }

    private static long replaceMediaSetStartTime(MediaSet mediaSet) {
        long startTime = mediaSet.getStartTakenTime();
        if (startTime != 0) {
            mediaSet.setStartTakenTime(0);
            mediaSet.reload();
        }
        return startTime;
    }

    private static void restoreMediaSetStartTime(MediaSet mediaSet, long restoreTime) {
        if (restoreTime != 0) {
            mediaSet.setStartTakenTime(restoreTime);
        }
    }
}
