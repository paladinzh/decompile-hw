package com.android.emui.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.PerfAdjust;
import com.android.systemui.utils.analyze.MemUtils;
import huawei.android.hwpicaveragenoises.HwPicAverageNoises;

public class WindowBlur {
    private int mBlurRadius = 25;
    private Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (1 == msg.what) {
                WindowBlur.this.mThread = null;
                Bitmap blurBitmap = msg.obj;
                msg.obj = null;
                if (WindowBlur.this.mOnBlurObserver != null) {
                    WindowBlur.this.mOnBlurObserver.onBlurFinish(blurBitmap);
                }
            }
        }
    };
    private int[] mLastFingerprint;
    private OnBlurObserver mOnBlurObserver;
    private BitmapThread mThread;

    private class BitmapThread extends Thread {
        private Bitmap mScreenBitmap;

        public void run() {
            Bitmap screenShot = null;
            if (PerfAdjust.supportBlurBackgound()) {
                try {
                    if (WindowBlur.this.mOnBlurObserver != null) {
                        screenShot = WindowBlur.this.mOnBlurObserver.getBaseBitmap();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } catch (Error err) {
                    Log.e("WindowBlur", "startWindowBlur  Error er = " + err.getMessage());
                    MemUtils.logCurrentMemoryInfo();
                }
                if (screenShot == null) {
                    Log.e("WindowBlur", "start screen shot fail,notify caller");
                    WindowBlur.this.notifyBlurResult(null);
                    return;
                }
                this.mScreenBitmap = screenShot;
                if (isInterrupted()) {
                    this.mScreenBitmap.recycle();
                    return;
                }
                if (!(this.mScreenBitmap.isMutable() && this.mScreenBitmap.getConfig() == Config.ARGB_8888)) {
                    Bitmap tmp = this.mScreenBitmap.copy(Config.ARGB_8888, true);
                    this.mScreenBitmap.recycle();
                    this.mScreenBitmap = tmp;
                }
                if (WindowBlur.this.compareFingerprint(this.mScreenBitmap)) {
                    this.mScreenBitmap.recycle();
                    return;
                }
                new BlurUtils().blurImage(WindowBlur.this.mContext, this.mScreenBitmap, this.mScreenBitmap, WindowBlur.this.mBlurRadius);
                HwPicAverageNoises hwPicAverageNoises = new HwPicAverageNoises();
                Bitmap bitmap = null;
                if (HwPicAverageNoises.isAverageNoiseSupported()) {
                    bitmap = BlurUtils.resizeImage(WindowBlur.this.mContext, this.mScreenBitmap);
                    if (bitmap != null) {
                        this.mScreenBitmap = hwPicAverageNoises.jniNoiseBitmap(bitmap);
                    }
                }
                if (isInterrupted()) {
                    if (this.mScreenBitmap != null) {
                        this.mScreenBitmap.recycle();
                    }
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    return;
                }
                if (PerfAdjust.supportScreenRotation() && this.mScreenBitmap != null) {
                    Matrix matrix = new Matrix();
                    matrix.postScale(0.5f, 0.5f);
                    this.mScreenBitmap = Bitmap.createBitmap(this.mScreenBitmap, 0, 0, this.mScreenBitmap.getWidth(), this.mScreenBitmap.getHeight(), matrix, true);
                }
                WindowBlur.this.notifyBlurResult(this.mScreenBitmap);
                return;
            }
            HwLog.i("WindowBlur", "not support blur background, notify null");
            WindowBlur.this.notifyBlurResult(null);
        }
    }

    public interface OnBlurObserver {
        Bitmap getBaseBitmap();

        void onBlurFinish(Bitmap bitmap);
    }

    public WindowBlur(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void setOnBlurObserver(OnBlurObserver observer) {
        this.mOnBlurObserver = observer;
    }

    public void setBlurRadius(int blurRadius) {
        this.mBlurRadius = blurRadius;
    }

    public void start() {
        if (this.mThread != null && this.mThread.isAlive()) {
            this.mThread.interrupt();
        }
        this.mThread = new BitmapThread();
        try {
            this.mThread.start();
        } catch (Exception e) {
            HwLog.e("WindowBlur", "start::occur exception=" + e);
            notifyBlurResult(null);
        } catch (Error e2) {
            HwLog.e("WindowBlur", "start::occur error=" + e2);
            notifyBlurResult(null);
        }
    }

    private boolean compareFingerprint(Bitmap bitmap) {
        if (bitmap == null) {
            Log.d("WindowBlur", "bitmap = null");
            return false;
        }
        try {
            int dstWidth = bitmap.getWidth();
            int dstHeight = bitmap.getHeight();
            while (dstWidth * dstHeight >= 2304) {
                dstWidth >>= 1;
                dstHeight >>= 1;
            }
            int[] fingerPrint = new int[(dstWidth * dstHeight)];
            Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false).getPixels(fingerPrint, 0, dstWidth, 0, 0, dstWidth, dstHeight);
            if (this.mLastFingerprint == null || this.mLastFingerprint.length != fingerPrint.length) {
                this.mLastFingerprint = fingerPrint;
                Log.d("WindowBlur", "fingerprint equals.");
                return false;
            }
            fallPixelColor(fingerPrint);
            int similitudeRate = 0;
            for (int i = 0; i < this.mLastFingerprint.length; i++) {
                if (this.mLastFingerprint[i] != fingerPrint[i]) {
                    similitudeRate++;
                }
            }
            Log.d("WindowBlur", "similitudeRate = " + similitudeRate);
            if (similitudeRate < 10) {
                return true;
            }
            this.mLastFingerprint = fingerPrint;
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } catch (Error er) {
            er.printStackTrace();
            return false;
        }
    }

    private final void fallPixelColor(int[] fingerPrint) {
        for (int i = 0; i < fingerPrint.length; i++) {
            fingerPrint[i] = Color.rgb(Color.red(fingerPrint[i]) >> 2, Color.green(fingerPrint[i]) >> 2, Color.blue(fingerPrint[i]) >> 2);
        }
    }

    private final void notifyBlurResult(Bitmap bitmap) {
        Log.d("WindowBlur", "notifyBlurResult bitmap = " + bitmap);
        Message msg = Message.obtain();
        msg.obj = bitmap;
        msg.what = 1;
        this.mHandler.sendMessage(msg);
    }
}
