package com.huawei.watermark.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.widget.ImageView;
import com.huawei.watermark.wmutil.WMFileUtil;

public class WMImageView extends ImageView {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMImageView.class.getSimpleName());
    private AsyncTask mLoadThumbnailTask;
    private String mWMImagePath;

    private class LoadThumbnailTask extends AsyncTask<Void, Void, Bitmap> {
        String mName;
        int mType;

        LoadThumbnailTask(int type, String name) {
            this.mType = type;
            this.mName = name;
        }

        protected Bitmap doInBackground(Void... voids) {
            return WMFileUtil.decodeBitmap(WMImageView.this.getContext(), WMImageView.this.mWMImagePath, this.mName);
        }

        protected void onPostExecute(final Bitmap bitmap) {
            WMImageView.this.post(new Runnable() {
                public void run() {
                    switch (LoadThumbnailTask.this.mType) {
                        case 0:
                            Log.d(WMImageView.TAG, "setImageBitmap");
                            WMImageView.this.setImageBitmap(bitmap);
                            return;
                        case 1:
                            Log.d(WMImageView.TAG, "setBackground");
                            WMImageView.this.setBackground(new BitmapDrawable(bitmap));
                            return;
                        default:
                            return;
                    }
                }
            });
        }
    }

    public WMImageView(Context context) {
        super(context);
    }

    public void setWMImagePath(String mWMImagePath, String name) {
        this.mWMImagePath = mWMImagePath;
        this.mLoadThumbnailTask = new LoadThumbnailTask(0, name).execute(new Void[0]);
    }

    public void setWMBackgroundImagePath(String mWMImagePath, String name) {
        this.mWMImagePath = mWMImagePath;
        new LoadThumbnailTask(1, name).execute(new Void[0]);
    }

    public boolean isTackFinished() {
        boolean z = true;
        if (this.mLoadThumbnailTask == null) {
            return true;
        }
        if (Status.FINISHED != this.mLoadThumbnailTask.getStatus()) {
            z = false;
        }
        return z;
    }
}
