package com.huawei.systemmanager.antivirus.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.huawei.systemmanager.comm.misc.SystemManagerConst;

public class RotateImageView extends ImageView {
    private static final long DELAY_MILLIS = 20;
    private static final int ROTATE_START = 2001;
    private int mAngle = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2001:
                    RotateImageView.this.invalidate();
                    if (RotateImageView.this.isEnabled() && RotateImageView.this.mIsRotate && RotateImageView.this.mIsTachedToWindow) {
                        RotateImageView.this.mHandler.sendEmptyMessageDelayed(2001, RotateImageView.DELAY_MILLIS);
                        break;
                    }
            }
            super.handleMessage(msg);
        }
    };
    private boolean mIsRotate = false;
    private boolean mIsTachedToWindow = false;

    public RotateImageView(Context context) {
        super(context);
    }

    public RotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void startRefresh() {
        if (!this.mIsRotate) {
            this.mIsRotate = true;
            this.mAngle = 0;
            this.mHandler.sendEmptyMessage(2001);
        }
    }

    public void stopRefresh() {
        this.mIsRotate = false;
        if (this.mHandler.hasMessages(2001)) {
            this.mHandler.removeMessages(2001);
        }
        invalidate();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mIsRotate) {
            this.mHandler.sendEmptyMessage(2001);
        }
        this.mIsTachedToWindow = true;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mIsTachedToWindow = false;
        if (this.mHandler.hasMessages(2001)) {
            this.mHandler.removeMessages(2001);
        }
    }

    protected void onDraw(Canvas canvas) {
        if (!isEnabled()) {
            super.onDraw(canvas);
        } else if (!this.mIsRotate) {
            super.onDraw(canvas);
        } else if (this.mIsRotate) {
            canvas.save();
            Drawable able = getDrawable();
            if (able != null) {
                this.mAngle += 10;
                if (this.mAngle > SystemManagerConst.CIRCLE_DEGREE) {
                    this.mAngle -= 360;
                }
                int centerX = (getWidth() - able.getIntrinsicWidth()) >> 1;
                int centerY = (getHeight() - able.getIntrinsicHeight()) >> 1;
                able.setBounds(centerX, centerY, able.getIntrinsicWidth() + centerX, able.getIntrinsicHeight() + centerY);
                canvas.rotate((float) this.mAngle, ((float) getWidth()) / 2.0f, ((float) getHeight()) / 2.0f);
                able.draw(canvas);
                canvas.restore();
                able.setBounds(0, 0, able.getIntrinsicWidth(), able.getIntrinsicHeight());
            }
        }
    }
}
