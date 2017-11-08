package com.huawei.gallery.media;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.gallery.media.database.MergedMedia;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import java.util.Collections;
import java.util.List;

public class StoryAlbumFile {
    private static final String[] CLOUD_FILE_PROJECTION = new String[]{"_id", "cloud_bucket_id", "hash", "_display_name", "_data", "fileType", "videoThumbId"};
    private static final String[] CLOUD_FILE_UNIQUEID_PROJECTION = new String[]{"_id", "cloud_bucket_id", "hash", "_display_name", "_data", "fileType", "videoThumbId", "uniqueId"};
    private static final String[] MAX_MIN_DATETAKEN_PROJECTION = new String[]{"MAX(showDatetoken)", "MIN(showDatetoken)"};
    private static final String[] PROJECTION = new String[]{"_id", "showDatetoken", "mime_type", "latitude", "longitude", "story_id", "story_cluster_state"};
    private static final String[] SIMILAR_PROCESS_PROJECTION = new String[]{"showDatetoken", "_size", "_data", "_id"};
    public static final Uri URI = MergedMedia.URI.buildUpon().appendPath("gallery_media").build();
    long dateTaken;
    int id;
    double latitude;
    double longitude;
    String mimeType;
    String storyClusterState;
    String storyId;

    public static final String[] getProjection() {
        return (String[]) PROJECTION.clone();
    }

    public static final String[] getSimilarProcessProjection() {
        return (String[]) SIMILAR_PROCESS_PROJECTION.clone();
    }

    public static final String[] getMaxMinDatetakenProjection() {
        return (String[]) MAX_MIN_DATETAKEN_PROJECTION.clone();
    }

    public static final String[] getCloudFileProjection() {
        if (RecycleUtils.supportRecycle() && PhotoShareUtils.isGUIDSupport()) {
            return (String[]) CLOUD_FILE_UNIQUEID_PROJECTION.clone();
        }
        return (String[]) CLOUD_FILE_PROJECTION.clone();
    }

    public StoryAlbumFile(Cursor c) {
        this.id = c.getInt(0);
        this.dateTaken = c.getLong(1);
        this.mimeType = c.getString(2);
        this.latitude = c.getDouble(3);
        this.longitude = c.getDouble(4);
        this.storyId = c.getString(5);
        this.storyClusterState = c.getString(6);
    }

    public void updateStoryAlbumFile(ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put("story_id", this.storyId);
        values.put("story_cluster_state", this.storyClusterState);
        contentResolver.update(URI, values, "_id = ?", new String[]{String.valueOf(this.id)});
    }

    public static void clearStoryAlbumFile(int id, ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put("story_id", "");
        values.put("story_cluster_state", "todo");
        contentResolver.update(URI, values, "_id = ?", new String[]{String.valueOf(id)});
    }

    public static void removeStoryAlbumFile(int id, ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put("story_id", "");
        values.put("story_cluster_state", "removed");
        contentResolver.update(URI, values, "_id = ?", new String[]{String.valueOf(id)});
    }

    private static void setStoryAlbumFileDuplicated(List<Integer> list, ContentValues values, ContentResolver contentResolver) {
        String[] idArray = new String[list.size()];
        int index = 0;
        for (Integer id : list) {
            idArray[index] = id.toString();
            index++;
        }
        contentResolver.update(URI, values, "_id IN (" + TextUtils.join(",", Collections.nCopies(list.size(), "?")) + ")", idArray);
    }

    public static void setStoryAlbumFileDuplicated(List<Integer> list, ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put("story_cluster_state", "duplicated");
        int length = list.size();
        for (int i = 0; i < length; i += 500) {
            setStoryAlbumFileDuplicated(list.subList(i, Math.min(i + 500, length)), values, contentResolver);
        }
    }

    public static void clearStoryId(int id, ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put("story_id", "");
        contentResolver.update(URI, values, "_id = ?", new String[]{String.valueOf(id)});
    }

    public String toString() {
        return this.id + "/" + this.dateTaken + "/" + this.storyId + "/" + this.storyClusterState;
    }
}
