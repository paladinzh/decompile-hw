package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation.StrokeData;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.tools.EditorUtils;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class MosaicView extends View {
    protected Delegate mDelegate;
    protected DoubleFingerControl mDoubleFingerControl = new DoubleFingerControl();
    protected int mDownX = 0;
    protected int mDownY = 0;
    protected FilterMosaicRepresentation mFRep;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    MosaicView.this.mRenderRequest = true;
                    if (MosaicView.this.mListener != null) {
                        MosaicView.this.mListener.onTouchChanged(false);
                    }
                    MosaicView.this.invalidate();
                    return;
                case 2:
                    MosaicView.this.mRenderRequest = false;
                    if (MosaicView.this.mFRep != null) {
                        MosaicView.this.mFRep.clearSegment();
                    }
                    if (MosaicView.this.mListener != null) {
                        MosaicView.this.mListener.reset();
                        MosaicView.this.mListener.onTouchChanged(true);
                    }
                    MosaicView.this.invalidate();
                    return;
                default:
                    return;
            }
        }
    };
    protected Listener mListener;
    protected MagnifierControl mMagnifierControl = new MagnifierControl(this);
    protected boolean mRenderRequest = false;
    protected int mStrokeType = 0;
    protected int mStyle = 1;
    protected StyleManager mStyleManager = new StyleManager();

    public interface Listener {
        boolean forceAddData();

        void onDoubleFingerControlChange(Matrix matrix, boolean z);

        void onStrokeDataCommit(FilterMosaicRepresentation filterMosaicRepresentation);

        void onTouchChanged(boolean z);

        void reset();
    }

    public interface Delegate extends com.huawei.gallery.editor.ui.DoubleFingerControl.Delegate {
        float[] getAbsolutePreviewPoint(float f, float f2);

        float[] getAbsolutePreviewPointWithOutCrop(float f, float f2);

        Matrix getBitmapToCanvasMatrix();

        int getColor(int i, int i2);

        Bitmap getCurrentDrawBitmap(StrokeData strokeData);

        Matrix getDoubleFingerControlMatrix();

        Bitmap getExpandBitmap();

        float getExpandBitmapInitScale();

        int getPaintType();

        boolean isEditorMatrixAnimeActive();
    }

    public static class StyleManager {
        private int mCurrentStyle = 0;

        public int getStyle() {
            return this.mCurrentStyle;
        }

        public void setStyle(int style) {
            this.mCurrentStyle = style;
        }
    }

    public void setDelegate(Delegate delegate) {
        this.mDelegate = delegate;
    }

    public void setBounds(Rect rect, Rect source) {
        this.mMagnifierControl.setScrImageInfo(rect, source, this.mDelegate);
        this.mDoubleFingerControl.setDelegate(this.mDelegate);
    }

    public MosaicView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFilterMosaicRepresentation(FilterMosaicRepresentation fRep) {
        this.mFRep = fRep;
        this.mMagnifierControl.setFilterMosaicRepresentation(this.mFRep);
    }

    public void setStrokeType(int type) {
        this.mStrokeType = type;
    }

    public void setListener(Listener l) {
        this.mListener = l;
    }

    private void updateMagnifierSupport() {
        if (this.mDelegate != null) {
            if (this.mStrokeType == 1 || this.mDelegate.getPaintType() != 0) {
                this.mMagnifierControl.setSupport(true);
            } else {
                this.mMagnifierControl.setSupport(false);
            }
        }
    }

    private void updateCurrent(float ex, float ey, boolean force) {
        if (this.mStyle != 2 && this.mMagnifierControl.isPointerValid() && this.mListener != null && this.mDelegate != null && this.mFRep != null) {
            float[] tmpPoint;
            if (this.mMagnifierControl.getVisible()) {
                tmpPoint = this.mDelegate.getAbsolutePreviewPoint(this.mMagnifierControl.getX(), this.mMagnifierControl.getY());
            } else {
                tmpPoint = this.mDelegate.getAbsolutePreviewPoint(ex, ey);
            }
            if (force) {
                this.mFRep.startNewSection(tmpPoint[0], tmpPoint[1], this.mStrokeType, this.mListener.forceAddData(), this.mDelegate.getPaintType(), this.mDoubleFingerControl.getMatrix());
            } else if (!(this.mFRep.getCurrentStrokeData() == null || this.mFRep.addPoint(tmpPoint[0], tmpPoint[1]))) {
                this.mFRep.startNewSection(tmpPoint[0], tmpPoint[1], this.mStrokeType, this.mListener.forceAddData(), this.mDelegate.getPaintType(), this.mDoubleFingerControl.getMatrix());
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mFRep == null || this.mListener == null || this.mDelegate == null || this.mDelegate.isEditorMatrixAnimeActive()) {
            return super.onTouchEvent(event);
        }
        this.mDownX = ((int) event.getX()) - getPaddingLeft();
        this.mDownY = ((int) event.getY()) - getPaddingTop();
        if (event.getAction() == 0) {
            updateMagnifierSupport();
            this.mStyleManager.setStyle(1);
            this.mDoubleFingerControl.setMatrix(this.mDelegate.getDoubleFingerControlMatrix());
            this.mMagnifierControl.actionDown((float) this.mDownX, (float) this.mDownY, EditorUtils.getPaintRadius(this.mDelegate.getPaintType(), this.mDoubleFingerControl.getMatrix(), this.mStrokeType, this.mFRep.getBounds(), GalleryUtils.getWidthPixels(), GalleryUtils.getHeightPixels()));
            updateCurrent((float) this.mDownX, (float) this.mDownY, true);
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHandler.sendEmptyMessageDelayed(1, 70);
        }
        if (event.getAction() == 2) {
            int historySize = event.getHistorySize();
            for (int h = 0; h < historySize; h++) {
                if (event.getPointerCount() != 1) {
                    switch (this.mStyleManager.getStyle()) {
                        case 0:
                            if (!this.mRenderRequest) {
                                this.mDoubleFingerControl.clear();
                                this.mMagnifierControl.actionUp();
                                this.mDoubleFingerControl.actionMove(event.getHistoricalX(0, h), event.getHistoricalY(0, h), event.getHistoricalX(1, h), event.getHistoricalY(1, h));
                                this.mStyleManager.setStyle(2);
                                break;
                            }
                            return true;
                        case 1:
                            event.setAction(3);
                            return onTouchEvent(event);
                        case 2:
                            this.mDoubleFingerControl.actionMove(event.getHistoricalX(0, h), event.getHistoricalY(0, h), event.getHistoricalX(1, h), event.getHistoricalY(1, h));
                            if (this.mListener == null) {
                                break;
                            }
                            this.mListener.onDoubleFingerControlChange(this.mDoubleFingerControl.getMatrix(), false);
                            break;
                        default:
                            break;
                    }
                }
                switch (this.mStyleManager.getStyle()) {
                    case 0:
                        event.setAction(0);
                        return onTouchEvent(event);
                    case 1:
                        float ex = event.getHistoricalX(0, h) - ((float) getPaddingLeft());
                        float ey = event.getHistoricalY(0, h) - ((float) getPaddingTop());
                        this.mMagnifierControl.actionMove(ex, ey);
                        updateCurrent(ex, ey, false);
                        break;
                    case 2:
                        event.setAction(3);
                        return onTouchEvent(event);
                    default:
                        break;
                }
            }
        }
        if (event.getAction() == 1 || event.getAction() == 3) {
            this.mHandler.removeCallbacksAndMessages(null);
            Handler handler = this.mHandler;
            int i = (event.getPointerCount() == 1 && this.mStyleManager.getStyle() == 2) ? 0 : SmsCheckResult.ESCT_200;
            handler.sendEmptyMessageDelayed(2, (long) i);
            if (this.mListener != null) {
                Listener listener = this.mListener;
                Matrix matrix = this.mDoubleFingerControl.getMatrix();
                boolean z = event.getPointerCount() == 1 && this.mStyleManager.getStyle() == 2;
                listener.onDoubleFingerControlChange(matrix, z);
            }
            this.mStyleManager.setStyle(0);
            this.mMagnifierControl.actionUp();
            if (this.mRenderRequest) {
                this.mFRep.close();
            } else {
                this.mFRep.discard();
            }
            if (this.mStyle != 2) {
                FilterRepresentation tempFrep = this.mFRep.copy();
                if (tempFrep instanceof FilterMosaicRepresentation) {
                    this.mListener.onStrokeDataCommit((FilterMosaicRepresentation) tempFrep);
                }
            }
            this.mMagnifierControl.clear();
        }
        invalidate();
        return true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mRenderRequest && this.mListener != null && this.mDelegate != null && this.mFRep != null) {
            canvas.save();
            canvas.translate((float) getPaddingLeft(), (float) getPaddingTop());
            Bitmap bmp = this.mDelegate.getCurrentDrawBitmap(this.mFRep.getCurrentSegment());
            if (bmp != null) {
                canvas.drawBitmap(bmp, this.mDelegate.getBitmapToCanvasMatrix(), null);
            }
            this.mMagnifierControl.draw(canvas, this.mFRep.getCurrentStrokeData(), 1, this.mDoubleFingerControl.getMatrix());
            canvas.restore();
        }
    }
}
