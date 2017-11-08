package com.huawei.gallery.media;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.huawei.gallery.media.database.MergedMedia;

public class StoryAlbum {
    private static final String[] PROJECTION = new String[]{"story_id", "date", "name", "min_datetaken", "max_datetaken", "project_id", "cover_id"};
    private static final String[] PROJECTION_VIEW = new String[]{"story_id", "min_datetaken", "max_datetaken", "name", "cover_id"};
    public static final Uri URI = MergedMedia.URI.buildUpon().appendPath("t_story_album").build();
    int coverId;
    String date;
    long maxDateTaken;
    long minDateTaken;
    String name;
    String projectId;
    String storyId;

    public static String[] getProjection() {
        return (String[]) PROJECTION.clone();
    }

    public static String[] getProjectionView() {
        return (String[]) PROJECTION_VIEW.clone();
    }

    StoryAlbum(String id, long minDate, long maxDate) {
        this.storyId = id;
        this.minDateTaken = minDate;
        this.maxDateTaken = maxDate;
    }

    public StoryAlbum(Cursor c) {
        this.storyId = c.getString(0);
        this.date = c.getString(1);
        this.name = c.getString(2);
        this.minDateTaken = c.getLong(3);
        this.maxDateTaken = c.getLong(4);
        this.projectId = c.getString(5);
        this.coverId = c.getInt(6);
    }

    public void insertStoryAlbum(ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put("story_id", this.storyId);
        values.put("date", this.date);
        values.put("name", this.name);
        values.put("min_datetaken", Long.valueOf(this.minDateTaken));
        values.put("max_datetaken", Long.valueOf(this.maxDateTaken));
        values.put("cover_id", Integer.valueOf(this.coverId));
        contentResolver.insert(URI, values);
    }

    public void updateStoryAlbum(ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put("date", this.date);
        values.put("name", this.name);
        values.put("min_datetaken", Long.valueOf(this.minDateTaken));
        values.put("max_datetaken", Long.valueOf(this.maxDateTaken));
        values.put("cover_id", Integer.valueOf(this.coverId));
        contentResolver.update(URI, values, "story_id = ?", new String[]{this.storyId});
    }

    public static void setStoryAlbumProjectId(String id, String projectId, ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put("project_id", projectId);
        contentResolver.update(URI, values, "story_id = ?", new String[]{id});
    }

    public static void setStoryAlbumCoverId(String id, String coverId, ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put("cover_id", coverId);
        contentResolver.update(URI, values, "story_id = ?", new String[]{id});
    }

    public static void renameStoryAlbum(String id, String manualName, ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put("name", manualName);
        contentResolver.update(URI, values, "story_id = ?", new String[]{id});
    }

    public static void removeStoryAlbum(String id, ContentResolver contentResolver) {
        contentResolver.delete(URI, "story_id = ?", new String[]{id});
    }

    public boolean isCoverId(int id) {
        return id == this.coverId;
    }

    public String getStoryId() {
        return this.storyId;
    }

    public String getProjectId() {
        return this.projectId;
    }

    public String getStoryName() {
        return this.name;
    }

    public String toString() {
        return this.storyId + "/" + this.date + "/" + this.name;
    }
}
