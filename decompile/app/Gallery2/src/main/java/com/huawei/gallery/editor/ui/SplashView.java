package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.FilterSplashRepresentation;
import com.huawei.gallery.editor.ui.MosaicView.Listener;

public class SplashView extends MosaicView {
    private int mCountColor = 0;
    private int mCountEraser = 0;

    public SplashView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setStyle(int style) {
        this.mStyle = style;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (this.mStyle) {
            case 1:
                break;
            case 2:
                if (treatColorNotActionDown(event)) {
                    return super.onTouchEvent(event);
                }
                break;
        }
        if ((this.mFRep instanceof FilterSplashRepresentation) && ((FilterSplashRepresentation) this.mFRep).getColor() != 0) {
            this.mFRep.setChangable(false);
            super.onTouchEvent(event);
            if (event.getAction() == 1) {
                this.mCountEraser++;
            }
        }
        return true;
    }

    private boolean treatColorNotActionDown(MotionEvent event) {
        boolean z = true;
        if (this.mFRep == null || this.mListener == null || this.mDelegate == null || this.mDelegate.isEditorMatrixAnimeActive()) {
            return true;
        }
        if (event.getAction() == 0) {
            this.mRenderRequest = false;
        }
        if (event.getAction() != 1 || event.getPointerCount() != 1 || this.mStyleManager.getStyle() != 1 || !this.mMagnifierControl.getVisible()) {
            return true;
        }
        this.mMagnifierControl.actionUp();
        this.mStyleManager.setStyle(0);
        if ((this.mFRep instanceof FilterSplashRepresentation) && this.mRenderRequest) {
            FilterSplashRepresentation representation = this.mFRep;
            float[] tmpPoint = this.mDelegate.getAbsolutePreviewPoint(this.mMagnifierControl.getX(), this.mMagnifierControl.getY());
            int color = this.mDelegate.getColor((int) tmpPoint[0], (int) tmpPoint[1]);
            if (color != 0) {
                representation.setColor(color);
                representation.setChangable(true);
                representation.clearStrokeData();
                FilterRepresentation tempFrep = this.mFRep.copy();
                if (tempFrep instanceof FilterMosaicRepresentation) {
                    this.mListener.onStrokeDataCommit((FilterMosaicRepresentation) tempFrep);
                }
            }
        }
        this.mMagnifierControl.clear();
        Listener listener = this.mListener;
        if (this.mMagnifierControl.getVisible()) {
            z = false;
        }
        listener.onTouchChanged(z);
        this.mCountColor++;
        return false;
    }

    protected void onDraw(Canvas canvas) {
        switch (this.mStyle) {
            case 1:
                super.onDraw(canvas);
                break;
            case 2:
                if (this.mRenderRequest) {
                    canvas.save();
                    canvas.translate((float) getPaddingLeft(), (float) getPaddingTop());
                    this.mMagnifierControl.draw(canvas, null, 2, this.mDoubleFingerControl.getMatrix());
                    canvas.restore();
                    break;
                }
                return;
        }
    }

    public int getSetColorCount() {
        return this.mCountColor;
    }

    public int getUseEraserCount() {
        return this.mCountEraser;
    }
}
