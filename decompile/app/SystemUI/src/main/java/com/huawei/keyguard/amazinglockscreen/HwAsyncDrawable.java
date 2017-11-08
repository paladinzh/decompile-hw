package com.huawei.keyguard.amazinglockscreen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

public class HwAsyncDrawable extends Drawable {
    private int mAlpha;
    private Bitmap mBitmap;
    private int mHeight;
    private final Paint mPaint = new Paint();
    private HwAsyncDrawable mPreDrawable;
    private Bitmap mScaleBitmap;
    private int mWidth;

    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private BitmapWorkerTask() {
        }

        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeFile(params[0]);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap bitmap) {
            HwAsyncDrawable.this.releaseBitmap();
            HwAsyncDrawable.this.mBitmap = null;
            HwAsyncDrawable.this.mScaleBitmap = null;
            HwAsyncDrawable.this.mBitmap = bitmap;
        }
    }

    public HwAsyncDrawable(String filePath) {
        new BitmapWorkerTask().execute(new String[]{filePath});
    }

    public HwAsyncDrawable(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public void setPreAsyncDrawbale(HwAsyncDrawable preDrawable) {
        this.mPreDrawable = preDrawable;
    }

    public void draw(Canvas canvas) {
        Bitmap bitmap = getBitmap();
        if (bitmap != null && !bitmap.isRecycled()) {
            Rect r = getBounds();
            float scale = AmazingUtils.getScalePara();
            if (scale != 1.0f) {
                canvas.scale(scale, scale);
            }
            canvas.drawBitmap(bitmap, (float) r.left, (float) r.top, this.mPaint);
        }
    }

    public void setColorFilter(ColorFilter cf) {
        this.mPaint.setColorFilter(cf);
    }

    public int getOpacity() {
        return -3;
    }

    public void setAlpha(int alpha) {
        this.mAlpha = alpha;
        this.mPaint.setAlpha(alpha);
    }

    public void setFilterBitmap(boolean filterBitmap) {
        this.mPaint.setFilterBitmap(filterBitmap);
    }

    public int getAlpha() {
        return this.mAlpha;
    }

    public int getIntrinsicWidth() {
        return this.mWidth;
    }

    public int getIntrinsicHeight() {
        return this.mHeight;
    }

    public int getMinimumWidth() {
        return this.mWidth;
    }

    public int getMinimumHeight() {
        return this.mHeight;
    }

    public Bitmap getBitmap() {
        if (this.mBitmap != null) {
            if (this.mScaleBitmap == null || AmazingUtils.getScalePara() == 1.0f) {
                return this.mBitmap;
            }
            return this.mScaleBitmap;
        } else if (this.mPreDrawable != null) {
            return this.mPreDrawable.getBitmap();
        } else {
            return null;
        }
    }

    private void recyleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public void releaseBitmap() {
        if (this.mBitmap != this.mScaleBitmap) {
            recyleBitmap(this.mScaleBitmap);
        }
    }
}
