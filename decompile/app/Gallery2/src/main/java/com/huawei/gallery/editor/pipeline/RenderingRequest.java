package com.huawei.gallery.editor.pipeline;

import android.graphics.Bitmap;
import android.graphics.Rect;
import com.android.gallery3d.util.GalleryLog;

public class RenderingRequest {
    private Bitmap mBitmap = null;
    private Rect mBounds = null;
    private RenderingRequestCaller mCaller = null;
    private Rect mDestination = null;
    private Rect mIconBounds = null;
    private ImagePreset mImagePreset = null;
    private int mType = 0;

    public static void post(SimpleEditorManager context, Bitmap source, ImagePreset preset, int type, RenderingRequestCaller caller) {
        post(context, source, preset, type, caller, null, null);
    }

    private static void post(SimpleEditorManager context, Bitmap source, ImagePreset preset, int type, RenderingRequestCaller caller, Rect bounds, Rect destination) {
        if ((type != 1 && source == null) || preset == null || caller == null) {
            GalleryLog.v("RenderingRequest", "something null: source: " + source + " or preset: " + preset + " or caller: " + caller);
            return;
        }
        ImagePreset passedPreset = new ImagePreset(preset);
        RenderingRequest request = new RenderingRequest();
        request.setImagePreset(passedPreset);
        request.setType(type);
        request.setCaller(caller);
        request.post(context);
    }

    public static void postIconRequest(SimpleEditorManager context, int w, int h, ImagePreset preset, RenderingRequestCaller caller, int delay) {
        if (preset == null || caller == null) {
            GalleryLog.v("RenderingRequest", "something null, preset: " + preset + " or caller: " + caller);
            return;
        }
        RenderingRequest request = new RenderingRequest();
        request.setImagePreset(new ImagePreset(preset));
        request.setType(0);
        request.setCaller(caller);
        request.setIconBounds(new Rect(0, 0, w, h));
        request.post(context, delay);
    }

    public void post(SimpleEditorManager context) {
        context.getProcessingService().postRenderingRequest(this, 0);
    }

    public void post(SimpleEditorManager context, int delay) {
        context.getProcessingService().postRenderingRequest(this, delay);
    }

    public void markAvailable() {
        if (this.mBitmap != null && this.mImagePreset != null && this.mCaller != null) {
            this.mCaller.available(this);
        }
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public ImagePreset getImagePreset() {
        return this.mImagePreset;
    }

    public void setImagePreset(ImagePreset imagePreset) {
        this.mImagePreset = imagePreset;
    }

    public int getType() {
        return this.mType;
    }

    public boolean isGemoeteryRendering() {
        return this.mType == 1;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public void setCaller(RenderingRequestCaller caller) {
        this.mCaller = caller;
    }

    public void setIconBounds(Rect bounds) {
        this.mIconBounds = bounds;
    }

    public Rect getIconBounds() {
        return this.mIconBounds;
    }
}
