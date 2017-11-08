package com.android.gallery3d.util;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import com.android.gallery3d.data.MediaItem;
import java.io.File;

public class MediaScannerClient implements MediaScannerConnectionClient {
    private Context mContext;
    private long mDateInMs;
    private File mFile;
    private MediaItem mItem;
    private MediaScannerConnection mScannerConnection;

    public MediaScannerClient(Context context, File file, MediaItem item) {
        this.mFile = file;
        this.mItem = item;
        if (this.mItem != null) {
            this.mDateInMs = this.mItem.getDateInMs();
        }
        this.mContext = context;
        this.mScannerConnection = new MediaScannerConnection(context, this);
        this.mScannerConnection.connect();
    }

    public void onMediaScannerConnected() {
        if (this.mFile != null && this.mScannerConnection.isConnected()) {
            if (this.mItem != null) {
                this.mScannerConnection.scanFile(this.mFile.getAbsolutePath(), this.mItem.getMimeType());
            } else {
                this.mScannerConnection.scanFile(this.mFile.getAbsolutePath(), null);
            }
        }
        this.mScannerConnection.disconnect();
    }

    public void onScanCompleted(String path, Uri uri) {
        if (path != null && uri != null && path.equalsIgnoreCase(this.mFile.getAbsolutePath()) && this.mItem != null) {
            boolean shouldUpdateDB = false;
            ContentValues values = new ContentValues();
            double[] latLong = new double[2];
            this.mItem.getLatLong(latLong);
            if (this.mItem.isRefocusPhoto() && !GalleryUtils.isValidLocation(latLong[0], latLong[1])) {
                GalleryLog.d("MediaScannerClient", "update allfocusphoto datetaken " + this.mDateInMs);
                values.put("datetaken", Long.valueOf(this.mDateInMs));
                shouldUpdateDB = true;
            }
            try {
                GalleryLog.d("MediaScannerClient", "shouldUpdateDB ? " + shouldUpdateDB + ", uri " + uri);
                if (shouldUpdateDB) {
                    this.mContext.getContentResolver().update(uri, values, null, null);
                }
            } catch (SecurityException e) {
                GalleryLog.noPermissionForMediaProviderLog("MediaScannerClient");
            }
        }
    }
}
