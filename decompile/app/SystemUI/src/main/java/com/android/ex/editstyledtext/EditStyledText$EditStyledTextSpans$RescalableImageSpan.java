package com.android.ex.editstyledtext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.style.ImageSpan;
import android.util.Log;
import java.io.InputStream;

public class EditStyledText$EditStyledTextSpans$RescalableImageSpan extends ImageSpan {
    private final int MAXWIDTH;
    Uri mContentUri;
    private Context mContext;
    private Drawable mDrawable;
    public int mIntrinsicHeight = -1;
    public int mIntrinsicWidth = -1;

    public EditStyledText$EditStyledTextSpans$RescalableImageSpan(Context context, Uri uri, int maxwidth) {
        super(context, uri);
        this.mContext = context;
        this.mContentUri = uri;
        this.MAXWIDTH = maxwidth;
    }

    public EditStyledText$EditStyledTextSpans$RescalableImageSpan(Context context, int resourceId, int maxwidth) {
        super(context, resourceId);
        this.mContext = context;
        this.MAXWIDTH = maxwidth;
    }

    public Drawable getDrawable() {
        if (this.mDrawable != null) {
            return this.mDrawable;
        }
        if (this.mContentUri != null) {
            System.gc();
            try {
                Bitmap bitmap;
                InputStream is = this.mContext.getContentResolver().openInputStream(this.mContentUri);
                Options opt = new Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, opt);
                is.close();
                is = this.mContext.getContentResolver().openInputStream(this.mContentUri);
                int width = opt.outWidth;
                int height = opt.outHeight;
                this.mIntrinsicWidth = width;
                this.mIntrinsicHeight = height;
                if (opt.outWidth > this.MAXWIDTH) {
                    width = this.MAXWIDTH;
                    height = (this.MAXWIDTH * height) / opt.outWidth;
                    bitmap = BitmapFactory.decodeStream(is, new Rect(0, 0, width, height), null);
                } else {
                    bitmap = BitmapFactory.decodeStream(is);
                }
                this.mDrawable = new BitmapDrawable(this.mContext.getResources(), bitmap);
                this.mDrawable.setBounds(0, 0, width, height);
                is.close();
            } catch (Exception e) {
                Log.e("EditStyledTextSpan", "Failed to loaded content " + this.mContentUri, e);
                return null;
            } catch (OutOfMemoryError e2) {
                Log.e("EditStyledTextSpan", "OutOfMemoryError");
                return null;
            }
        }
        this.mDrawable = super.getDrawable();
        rescaleBigImage(this.mDrawable);
        this.mIntrinsicWidth = this.mDrawable.getIntrinsicWidth();
        this.mIntrinsicHeight = this.mDrawable.getIntrinsicHeight();
        return this.mDrawable;
    }

    public Uri getContentUri() {
        return this.mContentUri;
    }

    private void rescaleBigImage(Drawable image) {
        Log.d("EditStyledTextSpan", "--- rescaleBigImage:");
        if (this.MAXWIDTH >= 0) {
            int image_width = image.getIntrinsicWidth();
            int image_height = image.getIntrinsicHeight();
            Log.d("EditStyledTextSpan", "--- rescaleBigImage:" + image_width + "," + image_height + "," + this.MAXWIDTH);
            if (image_width > this.MAXWIDTH) {
                image_width = this.MAXWIDTH;
                image_height = (this.MAXWIDTH * image_height) / image_width;
            }
            image.setBounds(0, 0, image_width, image_height);
        }
    }
}
