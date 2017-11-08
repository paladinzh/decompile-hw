package com.huawei.rcs.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class RcsFFMpegMediaScannerNotifier implements MediaScannerConnectionClient {
    private MediaScannerConnection mConnection;
    private String mPath;

    private RcsFFMpegMediaScannerNotifier(Context context, String path) {
        this.mPath = path;
        this.mConnection = new MediaScannerConnection(context, this);
        this.mConnection.connect();
    }

    public static void scan(Context context, String path) {
        if (context != null) {
            RcsFFMpegMediaScannerNotifier rcsFFMpegMediaScannerNotifier = new RcsFFMpegMediaScannerNotifier(context.getApplicationContext(), path);
        }
    }

    public void onMediaScannerConnected() {
        this.mConnection.scanFile(this.mPath, null);
    }

    public void onScanCompleted(String path, Uri uri) {
        this.mConnection.disconnect();
    }
}
