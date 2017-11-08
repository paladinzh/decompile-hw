package com.huawei.gallery.burst.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.BaseAdapter;
import com.huawei.gallery.burst.BurstSelectManager;
import com.huawei.gallery.burst.BurstThumbnailLoader;

public abstract class BurstImageGalleryAdapterBase extends BaseAdapter {
    protected final Context mContext;
    protected int mCount = 1;
    private Bitmap mCoverBitmap;
    private Handler mNotifyHandler = new Handler() {
        public void handleMessage(Message msg) {
            BurstImageGalleryAdapterBase.this.notifyDataSetChanged();
        }
    };
    protected final BurstSelectManager mSelectManager;
    protected BurstThumbnailLoader mThumbnailLoader;

    public Bitmap getCoverBitmap() {
        return this.mCoverBitmap;
    }

    public void setThumbnailLoader(BurstThumbnailLoader thumbnailLoader) {
        this.mThumbnailLoader = thumbnailLoader;
        this.mCount = this.mThumbnailLoader.size();
    }

    protected void notifyDataChangedByHandler() {
        this.mNotifyHandler.sendEmptyMessage(0);
    }

    public BurstImageGalleryAdapterBase(Context context, BurstSelectManager selectManager) {
        this.mContext = context;
        this.mSelectManager = selectManager;
    }

    public void clear() {
        this.mCount = 0;
        this.mNotifyHandler.removeCallbacksAndMessages(null);
    }

    public void updateViewForPosition(int index, Bitmap bitmap) {
        notifyDataChangedByHandler();
    }

    public int getCount() {
        return this.mCount;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getImgRotation(int position) {
        return this.mThumbnailLoader == null ? 0 : this.mThumbnailLoader.getRotation(position);
    }
}
