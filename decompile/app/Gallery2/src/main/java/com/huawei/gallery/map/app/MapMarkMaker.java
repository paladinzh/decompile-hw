package com.huawei.gallery.map.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import com.android.gallery3d.R;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool;
import com.huawei.gallery.map.data.IClusterInfo;

public class MapMarkMaker {
    private static final int FONT_PADDING = GalleryUtils.dpToPixel(6);
    private static final int FONT_SIZE = GalleryUtils.dpToPixel(13);
    private static final int PADDING = GalleryUtils.dpToPixel(5);
    private static final int PADDING_TOP = GalleryUtils.dpToPixel(3);
    private static final int SIDE_LENGTH = GalleryUtils.dpToPixel(50);
    private final Activity mActivity;
    private final Bitmap mBackground;
    private final Drawable mNumberBg = this.mActivity.getResources().getDrawable(R.drawable.bg_gallery_infosign_number);
    private final Paint mPaint = new Paint();

    public MapMarkMaker(Activity activity) {
        this.mActivity = activity;
        Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;
        this.mBackground = BitmapFactory.decodeResource(this.mActivity.getResources(), R.drawable.btn_gallery_map_mark_bg, options);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setTextSize((float) FONT_SIZE);
        this.mPaint.setColor(-1);
        this.mPaint.setTextAlign(Align.CENTER);
    }

    public Bitmap generateDrawable(IClusterInfo clusterInfo) {
        DisplayMetrics metrics = this.mActivity.getResources().getDisplayMetrics();
        Bitmap canvasBitmap = Bitmap.createBitmap(metrics, this.mBackground.getWidth(), this.mBackground.getHeight(), this.mBackground.getConfig());
        Canvas canvas = new Canvas(canvasBitmap);
        canvas.drawBitmap(this.mBackground, 0.0f, 0.0f, this.mPaint);
        if (clusterInfo == null) {
            GalleryLog.i("MapMarkMaker", "clusterInfo is null, debug-info  mark is wrong.");
            return canvasBitmap;
        }
        MediaItem item = clusterInfo.getCoverMediaItem();
        if (item == null) {
            GalleryLog.i("MapMarkMaker", "item is null, debug-info  mark is wrong.");
            return canvasBitmap;
        }
        Bitmap thumbNail = (Bitmap) item.requestImage(2).run(ThreadPool.JOB_CONTEXT_STUB);
        if (thumbNail != null) {
            int rotation = item.getRotation();
            if (rotation != 0) {
                thumbNail = BitmapUtils.rotateBitmap(thumbNail, rotation, false);
            }
            canvas.drawBitmap(thumbNail, new Rect(0, 0, thumbNail.getWidth(), thumbNail.getHeight()), new Rect(PADDING, PADDING_TOP, PADDING + SIDE_LENGTH, PADDING_TOP + SIDE_LENGTH), this.mPaint);
        }
        int count = clusterInfo.getMediaItemCount();
        if (count < 1) {
            return canvasBitmap;
        }
        String countText = count > 99 ? "99+" : String.valueOf(count);
        Rect textRect = new Rect();
        this.mPaint.getTextBounds(countText, 0, countText.length(), textRect);
        int bgHeight = this.mNumberBg.getIntrinsicHeight();
        int signWidth = textRect.width() + (FONT_PADDING << 1);
        Bitmap signBitmap = Bitmap.createBitmap(metrics, signWidth, bgHeight, Config.ARGB_8888);
        canvas.setBitmap(signBitmap);
        this.mNumberBg.setBounds(0, 0, signWidth, bgHeight);
        this.mNumberBg.draw(canvas);
        canvas.drawText(countText, (float) (signWidth / 2), (float) ((bgHeight / 2) - textRect.centerY()), this.mPaint);
        int extTopPading = Math.max(0, (bgHeight / 2) - PADDING_TOP);
        int extPading = Math.max(0, (signWidth / 2) - PADDING);
        GalleryLog.d("MapMarkMaker", "count: " + count + "extPading: " + extPading + ", extTopPading: " + extTopPading);
        Bitmap targetBitmap = Bitmap.createBitmap(canvasBitmap.getWidth() + (extPading * 2), canvasBitmap.getHeight() + extTopPading, Config.ARGB_8888);
        canvas.setBitmap(targetBitmap);
        canvas.drawColor(0);
        canvas.drawBitmap(canvasBitmap, (float) extPading, (float) extTopPading, this.mPaint);
        canvas.drawBitmap(signBitmap, (float) (targetBitmap.getWidth() - signWidth), 0.0f, this.mPaint);
        return targetBitmap;
    }
}
