package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import com.huawei.watermark.manager.parse.WMElement;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class HistogramView extends View {
    private int[] blueHistogram = new int[256];
    private int[] greenHistogram = new int[256];
    private Handler mHandle = new Handler() {
        public void handleMessage(Message msg) {
            Bitmap bitmap = null;
            switch (msg.what) {
                case 1:
                    if (HistogramView.this.mTask == null || HistogramView.this.mTask.isCancelled()) {
                        HistogramView.this.mTask = new ComputeHistogramTask();
                        Bitmap bitmap2 = (msg.obj == null || !(msg.obj instanceof Bitmap)) ? null : (Bitmap) msg.obj;
                        ComputeHistogramTask -get3 = HistogramView.this.mTask;
                        Bitmap[] bitmapArr = new Bitmap[1];
                        if (!(bitmap2 == null || bitmap2.isRecycled())) {
                            bitmap = bitmap2.copy(Config.ARGB_8888, true);
                        }
                        bitmapArr[0] = bitmap;
                        -get3.execute(bitmapArr);
                        return;
                    }
                    HistogramView.this.mHandle.sendMessageDelayed(Message.obtain(this, msg.what, msg.obj), 200);
                    return;
                default:
                    return;
            }
        }
    };
    private Path mHistoPath = new Path();
    private Paint mPaint = new Paint();
    private ComputeHistogramTask mTask = null;
    private int[] redHistogram = new int[256];

    class ComputeHistogramTask extends AsyncTask<Bitmap, Void, int[]> {
        ComputeHistogramTask() {
        }

        protected int[] doInBackground(Bitmap... params) {
            int[] histo = new int[768];
            if (params == null || params[0] == null) {
                return histo;
            }
            Bitmap bitmap = params[0];
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int[] pixels = new int[(w * h)];
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = (j * w) + i;
                    int r = Color.red(pixels[index]);
                    int g = Color.green(pixels[index]);
                    int b = Color.blue(pixels[index]);
                    histo[r] = histo[r] + 1;
                    int i2 = g + 256;
                    histo[i2] = histo[i2] + 1;
                    i2 = b + 512;
                    histo[i2] = histo[i2] + 1;
                }
            }
            bitmap.recycle();
            return histo;
        }

        protected void onPostExecute(int[] result) {
            System.arraycopy(result, 0, HistogramView.this.redHistogram, 0, 256);
            System.arraycopy(result, 256, HistogramView.this.greenHistogram, 0, 256);
            System.arraycopy(result, 512, HistogramView.this.blueHistogram, 0, 256);
            cancel(false);
            HistogramView.this.invalidate();
        }
    }

    public HistogramView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void sendDrawMessge(Object msgObj) {
        this.mHandle.removeCallbacksAndMessages(null);
        this.mHandle.sendMessage(Message.obtain(this.mHandle, 1, msgObj));
    }

    public void setBitmap(Bitmap bitmap) {
        setVisibility(0);
        sendDrawMessge(bitmap);
    }

    private void drawHistogram(Canvas canvas, int[] histogram, int color, Mode mode) {
        int i;
        int max = 0;
        for (i = 0; i < histogram.length; i++) {
            if (histogram[i] > max) {
                max = histogram[i];
            }
        }
        if (max != 0) {
            float w = (float) getWidth();
            float h = (float) getHeight();
            float wl = w / ((float) histogram.length);
            float wh = h / ((float) max);
            this.mPaint.reset();
            this.mPaint.setAntiAlias(true);
            this.mPaint.setARGB(100, 255, 255, 255);
            this.mPaint.setStrokeWidth(WMElement.CAMERASIZEVALUE1B1);
            this.mPaint.setStyle(Style.FILL);
            this.mPaint.setColor(color);
            this.mPaint.setStrokeWidth(6.0f);
            this.mPaint.setXfermode(new PorterDuffXfermode(mode));
            this.mHistoPath.reset();
            this.mHistoPath.moveTo(0.0f, h);
            boolean firstPointEncountered = false;
            float prev = 0.0f;
            float last = 0.0f;
            for (i = 0; i < histogram.length; i++) {
                float x = (((float) i) * wl) + 0.0f;
                float l = ((float) histogram[i]) * wh;
                if (l != 0.0f) {
                    float v = h - ((l + prev) / 2.0f);
                    if (!firstPointEncountered) {
                        this.mHistoPath.lineTo(x, h);
                        firstPointEncountered = true;
                    }
                    this.mHistoPath.lineTo(x, v);
                    prev = l;
                    last = x;
                }
            }
            this.mHistoPath.lineTo(last, h);
            this.mHistoPath.lineTo(w, h);
            this.mHistoPath.close();
            canvas.drawPath(this.mHistoPath, this.mPaint);
            this.mPaint.setStrokeWidth(2.0f);
            this.mPaint.setStyle(Style.STROKE);
            this.mPaint.setARGB(255, SmsCheckResult.ESCT_200, SmsCheckResult.ESCT_200, SmsCheckResult.ESCT_200);
            canvas.drawPath(this.mHistoPath, this.mPaint);
        }
    }

    private void drawGrid(Canvas canvas) {
        this.mPaint.reset();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setARGB(100, 255, 255, 255);
        this.mPaint.setStrokeWidth(WMElement.CAMERASIZEVALUE1B1);
        int w = getWidth();
        int h = getHeight();
        this.mPaint.setStyle(Style.STROKE);
        canvas.drawRect(0.0f, 0.0f, (float) w, (float) h, this.mPaint);
        canvas.drawLine((float) ((w / 3) + 0), 0.0f, (float) (w / 3), (float) h, this.mPaint);
        canvas.drawLine((float) (((w * 2) / 3) + 0), 0.0f, (float) ((w * 2) / 3), (float) h, this.mPaint);
    }

    public void onDraw(Canvas canvas) {
        canvas.drawARGB(0, 0, 0, 0);
        drawGrid(canvas);
        drawHistogram(canvas, this.redHistogram, -65536, Mode.SCREEN);
        drawHistogram(canvas, this.greenHistogram, -16711936, Mode.SCREEN);
        drawHistogram(canvas, this.blueHistogram, -16776961, Mode.SCREEN);
    }
}
