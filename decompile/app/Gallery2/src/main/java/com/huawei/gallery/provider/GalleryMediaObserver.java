package com.huawei.gallery.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.media.MediaSyncerService;
import com.huawei.gallery.media.database.MergedMedia;

public class GalleryMediaObserver extends ContentObserver {
    private Context mContext;
    private Handler mHandler;
    private Runnable mServiceStarter = new Runnable() {
        public void run() {
            GalleryMediaObserver.this.mHandler.removeCallbacks(GalleryMediaObserver.this.mServiceStarter);
            GalleryMediaObserver.this.mContext.startService(new Intent(GalleryMediaObserver.this.mContext, MediaSyncerService.class));
        }
    };

    public GalleryMediaObserver(Context context, Handler handler) {
        super(handler);
        this.mContext = context;
        this.mHandler = handler;
        this.mContext.startService(new Intent(this.mContext, MediaSyncerService.class));
        GalleryLog.d("GalleryMediaObserver", "Gallery Provider onCreate with observer " + this);
    }

    public void register() {
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.registerContentObserver(MergedMedia.IMAGE_URI, true, this);
        resolver.registerContentObserver(MergedMedia.VIDEO_URI, true, this);
        resolver.registerContentObserver(MergedMedia.OPERATION_URI, true, this);
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        this.mHandler.postDelayed(this.mServiceStarter, 1000);
    }
}
