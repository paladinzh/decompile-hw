package com.android.contacts.lettertiles;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.android.contacts.ContactPhotoManager;
import com.google.android.gms.R;
import junit.framework.Assert;

public class LetterTileDrawable extends Drawable {
    private static volatile TypedArray sColors;
    private static volatile int sDefaultColor;
    private static volatile Bitmap sDefaultPersonAvatarRect;
    private static volatile Bitmap sDefaultPersonAvatarRound_Black;
    private static volatile Bitmap sDefaultPersonAvatarRound_White;
    private static volatile int sForgroundSize = 1;
    private static final Paint sPaint = new Paint();
    private static volatile int sRealPhotoSize = 1;
    private static final Rect sRect = new Rect();
    private final String TAG = LetterTileDrawable.class.getSimpleName();
    private int mContactType = 1;
    private String mDisplayName;
    public int mHeight;
    private String mIdentifier;
    private boolean mIsCircle = true;
    private float mOffset = 0.0f;
    private final Paint mPaint = new Paint();
    private float mScale = 1.0f;
    public int mWidth;

    public LetterTileDrawable(Resources res) {
        this.mPaint.setFilterBitmap(true);
        this.mPaint.setDither(true);
        initialize(res);
    }

    public static void refreshAvatarCache(Resources res) {
        if (sDefaultPersonAvatarRound_Black != null) {
            sDefaultPersonAvatarRound_Black = ContactPhotoManager.drawableToBitmap(res.getDrawable(R.drawable.ic_contacts_default_list, null));
        }
        if (sDefaultPersonAvatarRound_White != null) {
            sDefaultPersonAvatarRound_White = ContactPhotoManager.drawableToBitmap(res.getDrawable(R.drawable.ic_contacts_default_list, null));
        }
        if (sDefaultPersonAvatarRect != null) {
            sDefaultPersonAvatarRect = ContactPhotoManager.drawableToBitmap(res.getDrawable(R.drawable.ic_contacts_default_list, null));
        }
    }

    private static void initialize(Resources res) {
        if (sColors == null) {
            sColors = res.obtainTypedArray(R.array.letter_tile_colors_default);
        }
        if (sDefaultColor == 0) {
            sDefaultColor = res.getColor(R.color.letter_tile_default_color);
        }
        if (sDefaultPersonAvatarRound_Black == null) {
            sDefaultPersonAvatarRound_Black = ContactPhotoManager.drawableToBitmap(res.getDrawable(R.drawable.ic_contacts_default_list, null));
        }
        if (sDefaultPersonAvatarRound_White == null) {
            sDefaultPersonAvatarRound_White = ContactPhotoManager.drawableToBitmap(res.getDrawable(R.drawable.ic_contacts_default_list, null));
        }
        if (sDefaultPersonAvatarRect == null) {
            sDefaultPersonAvatarRect = ContactPhotoManager.drawableToBitmap(res.getDrawable(R.drawable.ic_contacts_default_list, null));
        }
        if (sForgroundSize == 1 && sRealPhotoSize == 1) {
            sForgroundSize = res.getDimensionPixelSize(R.dimen.contact_detail_photo_forground_size);
            sRealPhotoSize = res.getDimensionPixelSize(R.dimen.contact_detail_photo_size);
            sPaint.setTextAlign(Align.CENTER);
            sPaint.setAntiAlias(true);
        }
    }

    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        if (isVisible() && !bounds.isEmpty()) {
            drawLetterTile(canvas);
        }
    }

    private void drawBitmap(Bitmap bitmap, int width, int height, Canvas canvas) {
        Rect destRect = copyBounds();
        int halfLength = (int) ((this.mScale * ((float) Math.min(destRect.width(), destRect.height()))) / 2.0f);
        destRect.set(destRect.centerX() - halfLength, (int) (((float) (destRect.centerY() - halfLength)) + (this.mOffset * ((float) destRect.height()))), destRect.centerX() + halfLength, (int) (((float) (destRect.centerY() + halfLength)) + (this.mOffset * ((float) destRect.height()))));
        sRect.set(0, 0, width, height);
        this.mWidth = width;
        this.mHeight = height;
        canvas.drawBitmap(bitmap, sRect, destRect, this.mPaint);
    }

    private void drawLetterTile(Canvas canvas) {
        sPaint.setColor(pickColor(this.mIdentifier));
        sPaint.setAlpha(this.mPaint.getAlpha());
        Rect bounds = getBounds();
        int minDimension = Math.min(bounds.width(), bounds.height());
        int realDimension = minDimension;
        if (this.mContactType == 2) {
            realDimension = (sRealPhotoSize * minDimension) / sForgroundSize;
        }
        if (this.mIsCircle) {
            canvas.drawCircle((float) bounds.centerX(), (float) bounds.centerY(), ((float) realDimension) / 2.0f, sPaint);
        } else {
            canvas.drawRect(bounds, sPaint);
        }
        Bitmap sDefaultPersonAvatarRound = sDefaultPersonAvatarRound_Black;
        if (this.mContactType == 2) {
            sDefaultPersonAvatarRound = sDefaultPersonAvatarRound_White;
        }
        if (this.mIsCircle) {
            drawBitmap(sDefaultPersonAvatarRound, sDefaultPersonAvatarRound.getWidth(), sDefaultPersonAvatarRound.getHeight(), canvas);
        } else {
            drawBitmap(sDefaultPersonAvatarRect, sDefaultPersonAvatarRect.getWidth(), sDefaultPersonAvatarRect.getHeight(), canvas);
        }
    }

    private int pickColor(String identifier) {
        if (TextUtils.isEmpty(identifier)) {
            return sDefaultColor;
        }
        if (sColors == null || sColors.length() <= 0) {
            return sDefaultColor;
        }
        return sColors.getColor(Math.abs(identifier.hashCode() & 4095) % sColors.length(), sDefaultColor);
    }

    public void setAlpha(int alpha) {
        this.mPaint.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter cf) {
        this.mPaint.setColorFilter(cf);
    }

    public int getOpacity() {
        return -1;
    }

    public void setScale(float scale) {
        this.mScale = scale;
    }

    public void setOffset(float offset) {
        boolean z = false;
        if (offset >= -0.5f && offset <= 0.5f) {
            z = true;
        }
        Assert.assertTrue(z);
        this.mOffset = offset;
    }

    public void setContactDetails(String displayName, String identifier) {
        this.mDisplayName = displayName;
        this.mIdentifier = identifier;
    }

    public void setContactType(int contactType) {
        this.mContactType = contactType;
    }

    public void setIsCircular(boolean isCircle) {
        this.mIsCircle = isCircle;
    }
}
