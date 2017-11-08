package com.common.imageloader.core.imageaware;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import com.common.imageloader.core.assist.ViewScaleType;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import java.lang.reflect.Field;

public class ImageViewAware extends ViewAware {
    public ImageViewAware(ImageView imageView) {
        super(imageView);
    }

    public ImageViewAware(ImageView imageView, boolean checkActualViewSize) {
        super(imageView, checkActualViewSize);
    }

    public int getWidth() {
        int width = super.getWidth();
        if (width > 0) {
            return width;
        }
        ImageView imageView = (ImageView) this.viewRef.get();
        if (imageView != null) {
            return getImageViewFieldValue(imageView, "mMaxWidth");
        }
        return width;
    }

    public int getHeight() {
        int height = super.getHeight();
        if (height > 0) {
            return height;
        }
        ImageView imageView = (ImageView) this.viewRef.get();
        if (imageView != null) {
            return getImageViewFieldValue(imageView, "mMaxHeight");
        }
        return height;
    }

    public ViewScaleType getScaleType() {
        ImageView imageView = (ImageView) this.viewRef.get();
        if (imageView != null) {
            return ViewScaleType.fromImageView(imageView);
        }
        return super.getScaleType();
    }

    public ImageView getWrappedView() {
        return (ImageView) super.getWrappedView();
    }

    protected void setImageDrawableInto(Drawable drawable, View view) {
        ((ImageView) view).setImageDrawable(drawable);
        if (drawable instanceof AnimationDrawable) {
            ((AnimationDrawable) drawable).start();
        }
    }

    protected void setImageBitmapInto(Bitmap bitmap, View view) {
        ((ImageView) view).setImageBitmap(bitmap);
    }

    private static int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        if (object == null || fieldName == null) {
            return 0;
        }
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = ((Integer) field.get(object)).intValue();
            if (fieldValue > 0 && fieldValue < SpaceConst.SCANNER_TYPE_ALL) {
                value = fieldValue;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
        } catch (Exception e5) {
            e5.printStackTrace();
        } catch (AssertionError e6) {
            e6.printStackTrace();
        } catch (Error e7) {
            e7.printStackTrace();
        } catch (Throwable e8) {
            e8.printStackTrace();
        }
        return value;
    }
}
