package com.huawei.watermark.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.huawei.watermark.wmutil.WMFileUtil;
import com.huawei.watermark.wmutil.WMStringUtil;

public class WMThumbImageView extends ImageView {
    private String mWMImageName;
    private String mWMImagePath;
    private LoadThumbnailTask mloadThumbnailTask;

    private class LoadThumbnailTask extends AsyncTask<Void, Void, Bitmap> {
        private LoadThumbnailTask() {
        }

        protected Bitmap doInBackground(Void... voids) {
            if (WMStringUtil.isEmptyString(WMThumbImageView.this.mWMImagePath) || WMStringUtil.isEmptyString(WMThumbImageView.this.mWMImageName)) {
                return null;
            }
            return WMFileUtil.decodeWMThumbBitmap(WMThumbImageView.this.getContext(), WMThumbImageView.this.mWMImagePath, WMThumbImageView.this.mWMImageName);
        }

        protected void onPostExecute(final Bitmap bitmap) {
            WMThumbImageView.this.post(new Runnable() {
                public void run() {
                    WMThumbImageView.this.setImageBitmap(bitmap);
                    WMThumbImageView.this.mloadThumbnailTask = null;
                }
            });
        }
    }

    public WMThumbImageView(Context context) {
        super(context);
    }

    public void setWMImagePath(String mWMImagePath, String name) {
        this.mWMImagePath = mWMImagePath;
        this.mWMImageName = name;
        this.mloadThumbnailTask = new LoadThumbnailTask();
        this.mloadThumbnailTask.execute(new Void[0]);
    }

    public boolean isRecycled() {
        if (this.mloadThumbnailTask != null) {
            return false;
        }
        Drawable dr = getDrawable();
        if (dr == null || !(dr instanceof BitmapDrawable)) {
            return true;
        }
        Bitmap bmp = ((BitmapDrawable) dr).getBitmap();
        if (bmp == null) {
            return true;
        }
        return bmp.isRecycled();
    }
}
