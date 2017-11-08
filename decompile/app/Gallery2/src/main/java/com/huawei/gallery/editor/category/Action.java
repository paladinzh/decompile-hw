package com.huawei.gallery.editor.category;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.widget.ArrayAdapter;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.pipeline.ImagePreset;
import com.huawei.gallery.editor.pipeline.RenderingRequest;
import com.huawei.gallery.editor.pipeline.RenderingRequestCaller;
import com.huawei.gallery.editor.pipeline.SimpleEditorManager;
import com.huawei.gallery.editor.step.EditorStep;
import com.huawei.gallery.editor.tools.EditorUtils;

public class Action implements RenderingRequestCaller {
    private ArrayAdapter mAdapter;
    private Context mContext;
    private SimpleEditorManager mEditorManager;
    private Class<? extends EditorStep> mEditorStepClass;
    private Bitmap mImage;
    private Rect mImageFrame;
    private String mName;
    private Bitmap mOverlayBitmap;
    private FilterRepresentation mRepresentation;
    private int mType;

    public Action(SimpleEditorManager editorManager, FilterRepresentation representation, int type, Class<? extends EditorStep> stepClass) {
        this(editorManager, representation, type);
        this.mEditorStepClass = stepClass;
    }

    public Action(SimpleEditorManager editorManager, FilterRepresentation representation, int type) {
        this(editorManager, type);
        setRepresentation(representation);
    }

    public Action(SimpleEditorManager editorManager, int type) {
        this.mType = 1;
        this.mEditorManager = editorManager;
        this.mContext = editorManager.getContext();
        setType(type);
        editorManager.registerAction(this);
    }

    public Action(SimpleEditorManager editorManager, FilterRepresentation representation, Class<? extends EditorStep> stepClass) {
        this(editorManager, representation, 1);
        this.mEditorStepClass = stepClass;
    }

    public boolean isDoubleAction() {
        return false;
    }

    public int getType() {
        return this.mType;
    }

    public FilterRepresentation getRepresentation() {
        return this.mRepresentation;
    }

    public void setRepresentation(FilterRepresentation representation) {
        if (representation.getTextId() != 0) {
            representation.setName(this.mContext.getString(representation.getTextId()));
        }
        this.mRepresentation = representation;
        this.mName = representation.getName();
    }

    public String getName() {
        return this.mName;
    }

    public void setImageFrame(Rect imageFrame, View view) {
        if ((this.mImageFrame == null || !this.mImageFrame.equals(imageFrame)) && getType() != 1) {
            Bitmap temp = this.mEditorManager.getImage().getTemporaryThumbnailBitmap();
            if (temp != null) {
                this.mImage = temp;
            }
            if (this.mEditorManager.getImage().getThumbnailBitmap() != null) {
                this.mImageFrame = imageFrame;
                postNewIconRenderRequest(this.mImageFrame.width(), this.mImageFrame.height(), calDelayTime(view));
            }
        }
    }

    public Bitmap getImage() {
        return this.mImage;
    }

    public void setAdapter(ArrayAdapter adapter) {
        this.mAdapter = adapter;
    }

    public void setType(int type) {
        this.mType = type;
    }

    private void postNewIconRenderRequest(int w, int h, int delay) {
        if (this.mRepresentation != null) {
            ImagePreset preset = new ImagePreset();
            EditorStep step = null;
            try {
                step = (EditorStep) this.mEditorStepClass.newInstance();
            } catch (InstantiationException e) {
                GalleryLog.i("Action", "newInstance() failed in postNewIconRenderRequest() method, reason: InstantiationException.");
            } catch (IllegalAccessException e2) {
                GalleryLog.i("Action", "newInstance() failed in postNewIconRenderRequest() method, reason: IllegalAccessException.");
            }
            if (step != null) {
                step.add(this.mRepresentation);
                preset.getEditorStepStack().push(step);
            } else {
                GalleryLog.w("Action", "get step instance null:" + this.mEditorStepClass);
            }
            RenderingRequest.postIconRequest(this.mEditorManager, w, h, preset, this, delay);
        }
    }

    private void drawCenteredImage(Bitmap source, Bitmap destination) {
        int minSide = Math.min(destination.getWidth(), destination.getHeight());
        Matrix m = new Matrix();
        float scaleFactor = ((float) minSide) / ((float) Math.min(source.getWidth(), source.getHeight()));
        float dx = (((float) destination.getWidth()) - (((float) source.getWidth()) * scaleFactor)) / 2.0f;
        float dy = (((float) destination.getHeight()) - (((float) source.getHeight()) * scaleFactor)) / 2.0f;
        if (this.mImageFrame.height() > this.mImageFrame.width()) {
            dy -= 32.0f;
        }
        m.setScale(scaleFactor, scaleFactor);
        m.postTranslate(dx, dy);
        new Canvas(destination).drawBitmap(source, m, new Paint(2));
    }

    public void available(RenderingRequest request) {
        clearBitmap();
        this.mImage = request.getBitmap();
        if (this.mImage == null) {
            this.mImageFrame = null;
            return;
        }
        if (this.mRepresentation.getOverlayId() != 0 && this.mOverlayBitmap == null) {
            this.mOverlayBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), this.mRepresentation.getOverlayId());
        }
        if (this.mOverlayBitmap != null) {
            int filterType = getRepresentation().getFilterType();
            if (filterType == 1 || filterType == 5 || filterType == 4) {
                new Canvas(this.mImage).drawBitmap(this.mOverlayBitmap, new Rect(0, 0, this.mOverlayBitmap.getWidth(), this.mOverlayBitmap.getHeight()), new Rect(0, 0, this.mImage.getWidth(), this.mImage.getHeight()), new Paint());
            } else {
                new Canvas(this.mImage).drawARGB(128, 0, 0, 0);
                drawCenteredImage(this.mOverlayBitmap, this.mImage);
            }
        }
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        }
    }

    public void clearBitmap() {
        if (!(this.mImage == null || this.mImage == this.mEditorManager.getImage().getTemporaryThumbnailBitmap())) {
            this.mEditorManager.getImage().getBitmapCache().cache(this.mImage);
        }
        this.mImage = null;
    }

    public void showRepresentation(EditorStep editorStep) {
        this.mEditorManager.showRepresentation(this.mRepresentation, editorStep);
    }

    public void resetBitmap() {
        clearBitmap();
        this.mImageFrame = null;
    }

    private int calDelayTime(View view) {
        return EditorUtils.isOnParentRightSide(view) ? 0 : 50;
    }
}
