package com.android.systemui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.android.internal.os.ProcessCpuTracker;
import com.android.internal.os.ProcessCpuTracker.Stats;
import fyusion.vislib.BuildConfig;

public class LoadAverageService extends Service {
    private View mView;

    private static final class CpuTracker extends ProcessCpuTracker {
        String mLoadText;
        int mLoadValWidth = ((int) this.mPaint.measureText("  100%"));
        int mLoadWidth;
        private final Paint mPaint;

        CpuTracker(Paint paint) {
            super(false);
            this.mPaint = paint;
        }

        public void onLoadChanged(float load1, float load5, float load15) {
            this.mLoadText = load1 + " / " + load5 + " / " + load15;
            this.mLoadWidth = ((int) this.mPaint.measureText(this.mLoadText)) + this.mLoadValWidth;
        }

        public int onMeasureProcessName(String name) {
            return ((int) this.mPaint.measureText(name)) + this.mLoadValWidth;
        }
    }

    private class LoadView extends View {
        private Paint mAddedPaint;
        private float mAscent;
        private int mFH;
        private Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    LoadView.this.mStats.update();
                    LoadView.this.updateDisplay();
                    sendMessageDelayed(obtainMessage(1), 2000);
                }
            }
        };
        private Paint mIrqPaint;
        private Paint mLoadPaint;
        private int mNeededHeight;
        private int mNeededWidth;
        private Paint mRemovedPaint;
        private Paint mShadow2Paint;
        private Paint mShadowPaint;
        private final CpuTracker mStats;
        private Paint mSystemPaint;
        private Paint mUserPaint;

        LoadView(Context c) {
            int textSize;
            super(c);
            setPadding(4, 4, 4, 4);
            float density = c.getResources().getDisplayMetrics().density;
            if (density < 1.0f) {
                textSize = 9;
            } else {
                textSize = (int) (10.0f * density);
                if (textSize < 10) {
                    textSize = 10;
                }
            }
            this.mLoadPaint = new Paint();
            this.mLoadPaint.setAntiAlias(true);
            this.mLoadPaint.setTextSize((float) textSize);
            this.mLoadPaint.setARGB(255, 255, 255, 255);
            this.mAddedPaint = new Paint();
            this.mAddedPaint.setAntiAlias(true);
            this.mAddedPaint.setTextSize((float) textSize);
            this.mAddedPaint.setARGB(255, 128, 255, 128);
            this.mRemovedPaint = new Paint();
            this.mRemovedPaint.setAntiAlias(true);
            this.mRemovedPaint.setStrikeThruText(true);
            this.mRemovedPaint.setTextSize((float) textSize);
            this.mRemovedPaint.setARGB(255, 255, 128, 128);
            this.mShadowPaint = new Paint();
            this.mShadowPaint.setAntiAlias(true);
            this.mShadowPaint.setTextSize((float) textSize);
            this.mShadowPaint.setARGB(192, 0, 0, 0);
            this.mLoadPaint.setShadowLayer(4.0f, 0.0f, 0.0f, -16777216);
            this.mShadow2Paint = new Paint();
            this.mShadow2Paint.setAntiAlias(true);
            this.mShadow2Paint.setTextSize((float) textSize);
            this.mShadow2Paint.setARGB(192, 0, 0, 0);
            this.mLoadPaint.setShadowLayer(2.0f, 0.0f, 0.0f, -16777216);
            this.mIrqPaint = new Paint();
            this.mIrqPaint.setARGB(128, 0, 0, 255);
            this.mIrqPaint.setShadowLayer(2.0f, 0.0f, 0.0f, -16777216);
            this.mSystemPaint = new Paint();
            this.mSystemPaint.setARGB(128, 255, 0, 0);
            this.mSystemPaint.setShadowLayer(2.0f, 0.0f, 0.0f, -16777216);
            this.mUserPaint = new Paint();
            this.mUserPaint.setARGB(128, 0, 255, 0);
            this.mSystemPaint.setShadowLayer(2.0f, 0.0f, 0.0f, -16777216);
            this.mAscent = this.mLoadPaint.ascent();
            this.mFH = (int) ((this.mLoadPaint.descent() - this.mAscent) + 0.5f);
            this.mStats = new CpuTracker(this.mLoadPaint);
            this.mStats.init();
            updateDisplay();
        }

        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            this.mHandler.sendEmptyMessage(1);
        }

        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.mHandler.removeMessages(1);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(resolveSize(this.mNeededWidth, widthMeasureSpec), resolveSize(this.mNeededHeight, heightMeasureSpec));
        }

        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int W = this.mNeededWidth;
            int RIGHT = getWidth() - 1;
            CpuTracker stats = this.mStats;
            int userTime = stats.getLastUserTime();
            int systemTime = stats.getLastSystemTime();
            int iowaitTime = stats.getLastIoWaitTime();
            int irqTime = stats.getLastIrqTime();
            int softIrqTime = stats.getLastSoftIrqTime();
            int totalTime = ((((userTime + systemTime) + iowaitTime) + irqTime) + softIrqTime) + stats.getLastIdleTime();
            if (totalTime != 0) {
                int userW = (userTime * W) / totalTime;
                int systemW = (systemTime * W) / totalTime;
                int irqW = (((iowaitTime + irqTime) + softIrqTime) * W) / totalTime;
                int paddingRight = getPaddingRight();
                int x = RIGHT - paddingRight;
                int top = getPaddingTop() + 2;
                int bottom = (getPaddingTop() + this.mFH) - 2;
                if (irqW > 0) {
                    canvas.drawRect((float) (x - irqW), (float) top, (float) x, (float) bottom, this.mIrqPaint);
                    x -= irqW;
                }
                if (systemW > 0) {
                    canvas.drawRect((float) (x - systemW), (float) top, (float) x, (float) bottom, this.mSystemPaint);
                    x -= systemW;
                }
                if (userW > 0) {
                    canvas.drawRect((float) (x - userW), (float) top, (float) x, (float) bottom, this.mUserPaint);
                    x -= userW;
                }
                String text = String.format("%d%%", new Object[]{Integer.valueOf(((userTime + systemTime) * 100) / totalTime)});
                while (((int) this.mLoadPaint.measureText(text)) < stats.mLoadValWidth) {
                    text = " " + text;
                }
                text = stats.mLoadText + text;
                int y = getPaddingTop() - ((int) this.mAscent);
                canvas.drawText(text, (float) (((RIGHT - paddingRight) - stats.mLoadWidth) - 1), (float) (y - 1), this.mShadowPaint);
                canvas.drawText(text, (float) (((RIGHT - paddingRight) - stats.mLoadWidth) - 1), (float) (y + 1), this.mShadowPaint);
                canvas.drawText(text, (float) (((RIGHT - paddingRight) - stats.mLoadWidth) + 1), (float) (y - 1), this.mShadow2Paint);
                canvas.drawText(text, (float) (((RIGHT - paddingRight) - stats.mLoadWidth) + 1), (float) (y + 1), this.mShadow2Paint);
                canvas.drawText(text, (float) ((RIGHT - paddingRight) - stats.mLoadWidth), (float) y, this.mLoadPaint);
                int N = stats.countWorkingStats();
                for (int i = 0; i < N; i++) {
                    Stats st = stats.getWorkingStats(i);
                    y += this.mFH;
                    top += this.mFH;
                    bottom += this.mFH;
                    userW = (st.rel_utime * W) / totalTime;
                    systemW = (st.rel_stime * W) / totalTime;
                    x = RIGHT - paddingRight;
                    if (systemW > 0) {
                        canvas.drawRect((float) (x - systemW), (float) top, (float) x, (float) bottom, this.mSystemPaint);
                        x -= systemW;
                    }
                    if (userW > 0) {
                        canvas.drawRect((float) (x - userW), (float) top, (float) x, (float) bottom, this.mUserPaint);
                        x -= userW;
                    }
                    text = String.format("%d%%", new Object[]{Integer.valueOf(((st.rel_utime + st.rel_stime) * 100) / totalTime)});
                    StringBuilder stringBuilder = new StringBuilder();
                    String str = st.added ? "+" : st.removed ? "-" : BuildConfig.FLAVOR;
                    text = stringBuilder.append(str).append(text).toString();
                    while (((int) this.mLoadPaint.measureText(text)) < stats.mLoadValWidth) {
                        text = " " + text;
                    }
                    text = st.name + text;
                    canvas.drawText(text, (float) (((RIGHT - paddingRight) - st.nameWidth) - 1), (float) (y - 1), this.mShadowPaint);
                    canvas.drawText(text, (float) (((RIGHT - paddingRight) - st.nameWidth) - 1), (float) (y + 1), this.mShadowPaint);
                    canvas.drawText(text, (float) (((RIGHT - paddingRight) - st.nameWidth) + 1), (float) (y - 1), this.mShadow2Paint);
                    canvas.drawText(text, (float) (((RIGHT - paddingRight) - st.nameWidth) + 1), (float) (y + 1), this.mShadow2Paint);
                    Paint p = this.mLoadPaint;
                    if (st.added) {
                        p = this.mAddedPaint;
                    }
                    if (st.removed) {
                        p = this.mRemovedPaint;
                    }
                    canvas.drawText(text, (float) ((RIGHT - paddingRight) - st.nameWidth), (float) y, p);
                }
            }
        }

        void updateDisplay() {
            CpuTracker stats = this.mStats;
            int NW = stats.countWorkingStats();
            int maxWidth = stats.mLoadWidth;
            for (int i = 0; i < NW; i++) {
                Stats st = stats.getWorkingStats(i);
                if (st.nameWidth > maxWidth) {
                    maxWidth = st.nameWidth;
                }
            }
            int neededWidth = (getPaddingLeft() + getPaddingRight()) + maxWidth;
            int neededHeight = (getPaddingTop() + getPaddingBottom()) + (this.mFH * (NW + 1));
            if (neededWidth == this.mNeededWidth && neededHeight == this.mNeededHeight) {
                invalidate();
                return;
            }
            this.mNeededWidth = neededWidth;
            this.mNeededHeight = neededHeight;
            requestLayout();
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mView = new LoadView(this);
        LayoutParams params = new LayoutParams(-1, -2, 2015, 24, -3);
        params.gravity = 8388661;
        params.setTitle("Load Average");
        ((WindowManager) getSystemService("window")).addView(this.mView, params);
    }

    public void onDestroy() {
        super.onDestroy();
        ((WindowManager) getSystemService("window")).removeView(this.mView);
        this.mView = null;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
