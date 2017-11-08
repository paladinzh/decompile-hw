package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.gallery.editor.filters.FilterIllusionRepresentation;
import com.huawei.gallery.editor.ui.IllusionBar.STYLE;

public class IllusionView extends View implements OnClickListener {
    private SparseArray<ShapeControl> mControls = new SparseArray();
    private ShapeControl mCurrentControls;
    private FilterIllusionRepresentation mFRep;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    IllusionView.this.mCurrentControls.mNeedCover = false;
                    IllusionView.this.invalidate();
                    return;
                case 2:
                    if (IllusionView.this.mListener.getApplyBitmap() == null) {
                        sendEmptyMessageDelayed(2, 100);
                        return;
                    }
                    IllusionView.this.mCurrentControls.mNeedCover = true;
                    IllusionView.this.invalidate();
                    IllusionView.this.mHandler.removeMessages(1);
                    IllusionView.this.mHandler.sendEmptyMessageDelayed(1, 500);
                    IllusionView.this.mListener.onStrokeDataChange(IllusionView.this.mFRep.copy());
                    return;
                default:
                    return;
            }
        }
    };
    private Listener mListener;
    private float mX = 0.0f;
    private float mY = 0.0f;

    public interface Listener {
        Bitmap getApplyBitmap();

        float getScaleScreenToImage(boolean z);

        void onStrokeDataChange(FilterIllusionRepresentation filterIllusionRepresentation);
    }

    public IllusionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.mControls.put(STYLE.WHOLE.ordinal(), new FullScreenControl());
        this.mControls.put(STYLE.BAND.ordinal(), new BandControl());
        this.mControls.put(STYLE.CIRCLE.ordinal(), new CircleControl());
        this.mCurrentControls = null;
        setOnClickListener(this);
    }

    public void hide() {
        setVisibility(8);
    }

    public void setFilterIllusionRepresentation(FilterIllusionRepresentation rep) {
        this.mFRep = rep;
    }

    public void setListener(Listener l) {
        this.mListener = l;
    }

    public void setBounds(Rect rect) {
        if (this.mControls != null) {
            for (int i = 0; i < this.mControls.size(); i++) {
                ((ShapeControl) this.mControls.get(this.mControls.keyAt(i))).setScrImageInfo(rect, this.mListener, this.mFRep);
            }
        }
    }

    public void setStyle(STYLE style) {
        if (this.mControls.size() == 0) {
            init();
        }
        this.mFRep.setStyle(style);
        this.mFRep.setNeedApply(true);
        this.mCurrentControls = (ShapeControl) this.mControls.get(style.ordinal());
        if (this.mCurrentControls != null) {
            this.mHandler.sendEmptyMessage(2);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mFRep == null || this.mListener == null || this.mCurrentControls == null) {
            return super.onTouchEvent(event);
        }
        this.mHandler.removeMessages(1);
        switch (event.getAction()) {
            case 0:
                this.mX = event.getX() - ((float) getPaddingLeft());
                this.mY = event.getY() - ((float) getPaddingTop());
                if (event.getPointerCount() == 1) {
                    this.mCurrentControls.actionDown(this.mX, this.mY);
                } else if (event.getPointerCount() == 2) {
                    this.mCurrentControls.actionDown(event.getX(0) - ((float) getPaddingLeft()), event.getY(0) - ((float) getPaddingTop()), event.getX(1) - ((float) getPaddingLeft()), event.getY(1) - ((float) getPaddingTop()));
                }
                this.mFRep.setNeedApply(false);
                this.mListener.onStrokeDataChange(this.mFRep.copy());
                invalidate();
                break;
            case 1:
                this.mFRep.setNeedApply(true);
                this.mCurrentControls.actionUp(this.mFRep, false);
                this.mHandler.removeMessages(1);
                this.mHandler.sendEmptyMessageDelayed(1, 300);
                this.mListener.onStrokeDataChange(this.mFRep.copy());
                break;
            case 2:
                int historySize;
                int h;
                if (event.getPointerCount() == 1) {
                    historySize = event.getHistorySize();
                    for (h = 0; h < historySize; h++) {
                        this.mCurrentControls.actionMove(event.getHistoricalX(0, h) - ((float) getPaddingLeft()), event.getHistoricalY(0, h) - ((float) getPaddingTop()));
                    }
                } else if (event.getPointerCount() == 2) {
                    historySize = event.getHistorySize();
                    for (h = 0; h < historySize; h++) {
                        this.mCurrentControls.actionMove(event.getHistoricalX(0, h) - ((float) getPaddingLeft()), event.getHistoricalY(0, h) - ((float) getPaddingTop()), event.getHistoricalX(1, h) - ((float) getPaddingLeft()), event.getHistoricalY(1, h) - ((float) getPaddingTop()));
                    }
                }
                this.mFRep.setNeedApply(false);
                invalidate();
                break;
        }
        return true;
    }

    public void onClick(View view) {
        if (this.mCurrentControls != null) {
            this.mCurrentControls.actionUp(this.mFRep, this.mX, this.mY);
            this.mCurrentControls.mNeedCover = true;
            this.mListener.onStrokeDataChange(this.mFRep.copy());
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessageDelayed(1, 500);
            invalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.mCurrentControls != null && this.mListener.getApplyBitmap() != null) {
            canvas.save();
            canvas.translate((float) getPaddingLeft(), (float) getPaddingTop());
            this.mCurrentControls.draw(canvas);
            canvas.restore();
        }
    }
}
