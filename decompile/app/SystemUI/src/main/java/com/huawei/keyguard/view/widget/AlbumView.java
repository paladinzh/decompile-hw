package com.huawei.keyguard.view.widget;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.android.huawei.music.HwCustAlbumView;
import com.android.keyguard.R$dimen;
import com.huawei.cust.HwCustUtils;
import com.huawei.keyguard.util.HwLog;

public class AlbumView extends View {
    private Bitmap mAlbumBmp;
    private int mAlbumRadius;
    private int mBackgroundRadius;
    private Rect mDstRect;
    private Paint mPaint;
    private Paint mPaint2;
    private Paint mPaint3;
    private Rect mSrcRect;

    public AlbumView(Context context) {
        this(context, null);
    }

    public AlbumView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mSrcRect = new Rect();
        this.mDstRect = new Rect();
        this.mPaint = new Paint();
        this.mPaint2 = new Paint();
        this.mPaint3 = new Paint();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPaint.setColor(1291845631);
        this.mPaint.setAntiAlias(true);
        this.mPaint2.setAntiAlias(true);
        this.mPaint2.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        this.mPaint3.setXfermode(new PorterDuffXfermode(Mode.DST_OVER));
        try {
            this.mBackgroundRadius = getResources().getDimensionPixelSize(R$dimen.music_background_size) / 2;
        } catch (NotFoundException e) {
            HwLog.w("AlbumView", "music_background_size not found");
        }
        try {
            this.mAlbumRadius = getResources().getDimensionPixelSize(R$dimen.music_album_size) / 2;
        } catch (NotFoundException e2) {
            HwLog.w("AlbumView", "music_background_size not found");
        }
        HwCustAlbumView hwCustAlbumView = (HwCustAlbumView) HwCustUtils.createObj(HwCustAlbumView.class, new Object[]{getContext()});
        if (hwCustAlbumView != null) {
            this.mBackgroundRadius = hwCustAlbumView.getMusicBackgroundSize(this.mBackgroundRadius);
            this.mAlbumRadius = hwCustAlbumView.getMusicAlbumSize(this.mAlbumRadius);
        }
        int left = this.mBackgroundRadius - this.mAlbumRadius;
        int top = left;
        this.mDstRect.set(left, left, (this.mAlbumRadius * 2) + left, (this.mAlbumRadius * 2) + left);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mAlbumBmp = null;
    }

    public void setAlumBitmap(Bitmap albumBmp) {
        if (albumBmp == null) {
            HwLog.w("AlbumView", "setAlumBitmap, albumBmp is null");
            return;
        }
        this.mAlbumBmp = albumBmp;
        int bitmapW = this.mAlbumBmp.getWidth();
        int bitmapH = this.mAlbumBmp.getHeight();
        int srcSize = bitmapW < bitmapH ? bitmapW : bitmapH;
        int left = bitmapW > bitmapH ? (bitmapW - bitmapH) / 2 : 0;
        this.mSrcRect.set(left, 0, srcSize + left, srcSize);
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        if (this.mAlbumBmp != null) {
            canvas.drawCircle((float) this.mBackgroundRadius, (float) this.mBackgroundRadius, (float) this.mBackgroundRadius, this.mPaint);
            canvas.drawCircle((float) this.mBackgroundRadius, (float) this.mBackgroundRadius, (float) this.mAlbumRadius, this.mPaint2);
            Bitmap circleBitmap = getCircleBitmap(this.mAlbumBmp);
            if (circleBitmap != null) {
                canvas.drawBitmap(circleBitmap, this.mSrcRect, this.mDstRect, this.mPaint3);
                circleBitmap.recycle();
            }
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(-12434878);
        int x = bitmap.getWidth();
        if (x <= 0) {
            return null;
        }
        float radius = ((float) x) / 2.0f;
        canvas.drawCircle(radius, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public Bitmap getmAlbumBmp() {
        return this.mAlbumBmp;
    }
}
