package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class KeyguardSet extends MediaSet {
    private static final String[] PROJECTION = new String[]{"COUNT(*)"};
    private static final Uri[] mWatchUris = new Uri[]{Keyguard.URI};
    private int mAlbumType = -1;
    private final GalleryApp mApplication;
    private int mCachedCount = -1;
    private List<Classification> mClassifications = new ArrayList();
    private final ChangeNotifier mNotifier;
    private String[] mWhereArgs;
    private String mWhereClause;

    public KeyguardSet(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mNotifier = new ChangeNotifier((MediaSet) this, mWatchUris, application);
        if ("download".equalsIgnoreCase(path.getSuffix())) {
            this.mAlbumType = 0;
        } else if ("custom".equalsIgnoreCase(path.getSuffix())) {
            this.mAlbumType = 1;
        }
        this.mWhereClause = "isHidden=? AND isCustom=?";
        this.mWhereArgs = new String[]{String.valueOf(0), String.valueOf(this.mAlbumType)};
    }

    public int getMediaItemCount() {
        if (this.mCachedCount == -1) {
            Closeable closeable = null;
            try {
                closeable = this.mApplication.getContentResolver().query(Keyguard.URI, PROJECTION, this.mWhereClause, this.mWhereArgs, "_id DESC");
                if (closeable == null) {
                    GalleryLog.w("Keyguard_Set", "query fail");
                    return 0;
                }
                if (closeable.moveToNext()) {
                    this.mCachedCount = closeable.getInt(0);
                }
                Utils.closeSilently(closeable);
            } catch (SQLException e) {
                GalleryLog.w("Keyguard_Set", "get group data fail." + e.getMessage());
            } finally {
                Utils.closeSilently(closeable);
            }
        }
        return this.mCachedCount;
    }

    public static Cursor getItemCursor(ContentResolver resolver, Uri uri, String[] projection, int id) {
        return resolver.query(uri, projection, "_id=?", new String[]{String.valueOf(id)}, null);
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        ArrayList<MediaItem> list = new ArrayList();
        GalleryUtils.assertNotInRenderThread();
        Uri uri = Keyguard.URI.buildUpon().appendQueryParameter("limit", start + "," + count).build();
        Cursor cursor = this.mApplication.getContentResolver().query(uri, KeyguardItem.PROJECTION, this.mWhereClause, this.mWhereArgs, "_id DESC");
        if (cursor == null) {
            GalleryLog.w("Keyguard_Set", "query fial: " + uri);
            return list;
        }
        DataManager dm = this.mApplication.getDataManager();
        while (cursor.moveToNext()) {
            try {
                list.add(loadOrUpdateItem(cursor, dm, this.mApplication));
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    private KeyguardItem loadOrUpdateItem(Cursor cursor, DataManager dm, GalleryApp app) {
        KeyguardItem item;
        synchronized (DataManager.LOCK) {
            Path childPath = KeyguardItem.ITEMPATH.getChild(cursor.getInt(0));
            item = (KeyguardItem) dm.peekMediaObject(childPath);
            if (item == null) {
                item = new KeyguardItem(app, childPath, cursor);
            } else {
                item.updateContent(cursor);
            }
        }
        return item;
    }

    public String getName() {
        return getNameByType(this.mAlbumType);
    }

    public long reload() {
        if (this.mNotifier.isDirty()) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            this.mCachedCount = -1;
            this.mClassifications.clear();
        }
        return this.mDataVersion;
    }

    private String getNameByType(int type) {
        switch (type) {
            case 0:
                return this.mApplication.getResources().getString(R.string.keyguard_download_title);
            case 1:
                return this.mApplication.getResources().getString(R.string.keyguard_custom_title);
            default:
                throw new IllegalArgumentException("illegal type = " + type);
        }
    }

    public boolean isLeafAlbum() {
        return true;
    }
}
