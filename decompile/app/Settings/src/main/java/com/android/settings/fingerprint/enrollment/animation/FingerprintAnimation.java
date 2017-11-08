package com.android.settings.fingerprint.enrollment.animation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import java.util.ArrayList;

public class FingerprintAnimation extends SurfaceView implements Callback {
    private int backgroundColor;
    private int drawColor;
    private int grayColor;
    public float mAnimValue = 0.0f;
    HandlerThread mAnimatorThread;
    Bitmap mCatchBitmap;
    Canvas mCatchCanvas;
    private Paint mClearPaint;
    AnimationHandler mHandler;
    private SurfaceHolder mHolder;
    private boolean mIsCreated = false;
    private boolean mIsRevert = false;
    private ArrayList<Message> mMessages = new ArrayList();
    private Paint mPaint;
    ArrayList<TouchShowPath> mPaths = new ArrayList();
    private int mStage = -1;

    private class AnimationHandler extends Handler {
        public AnimationHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.i("fingerprint.FingerprintAnimation", "Message=" + msg.what);
            Canvas canvas;
            switch (msg.what) {
                case 0:
                    FingerprintAnimation.this.preparAnimation(((Boolean) msg.obj).booleanValue());
                    int i = 1;
                    while (((float) i) <= 20.0f) {
                        canvas = FingerprintAnimation.this.mHolder.lockCanvas();
                        if (canvas != null) {
                            FingerprintAnimation.this.mAnimValue = ((float) i) / 20.0f;
                            if (FingerprintAnimation.this.mStage >= 0) {
                                FingerprintAnimation.this.drawPath(FingerprintAnimation.this.mCatchCanvas);
                            }
                            canvas.drawBitmap(FingerprintAnimation.this.mCatchBitmap, 0.0f, 0.0f, null);
                            FingerprintAnimation.this.mHolder.unlockCanvasAndPost(canvas);
                            if (((Boolean) msg.obj).booleanValue()) {
                                SystemClock.sleep(5);
                            }
                            i++;
                        } else {
                            return;
                        }
                    }
                    break;
                case 1:
                    FingerprintAnimation.this.clear();
                    canvas = FingerprintAnimation.this.mHolder.lockCanvas();
                    if (canvas != null) {
                        FingerprintAnimation.this.mCatchCanvas.drawColor(FingerprintAnimation.this.backgroundColor);
                        FingerprintAnimation.this.drawBackground(FingerprintAnimation.this.mCatchCanvas);
                        canvas.drawBitmap(FingerprintAnimation.this.mCatchBitmap, 0.0f, 0.0f, null);
                        FingerprintAnimation.this.mHolder.unlockCanvasAndPost(canvas);
                        break;
                    }
                    return;
                case 2:
                    Canvas mCanvas = FingerprintAnimation.this.mHolder.lockCanvas();
                    if (mCanvas != null) {
                        FingerprintAnimation.this.mCatchBitmap = Bitmap.createBitmap(mCanvas.getWidth(), mCanvas.getHeight(), Config.ARGB_8888);
                        FingerprintAnimation.this.mCatchCanvas = new Canvas(FingerprintAnimation.this.mCatchBitmap);
                        FingerprintAnimation.this.mCatchCanvas.drawColor(FingerprintAnimation.this.backgroundColor);
                        FingerprintAnimation.this.drawBackground(FingerprintAnimation.this.mCatchCanvas);
                        mCanvas.drawBitmap(FingerprintAnimation.this.mCatchBitmap, 0.0f, 0.0f, null);
                        FingerprintAnimation.this.mHolder.unlockCanvasAndPost(mCanvas);
                        break;
                    }
                    return;
                case 3:
                    FingerprintAnimation.this.mCatchCanvas = null;
                    FingerprintAnimation.this.mCatchBitmap.recycle();
                    FingerprintAnimation.this.clear();
                    break;
            }
        }
    }

    public FingerprintAnimation(Context context) {
        super(context);
        init(context);
    }

    public void destory() {
        if (this.mAnimatorThread != null) {
            this.mAnimatorThread.quit();
            this.mAnimatorThread = null;
        }
    }

    public void needAnimation(boolean isRevertNext) {
        Log.i("fingerprint.FingerprintAnimation", "needAnimation");
        Message message = new Message();
        message.obj = Boolean.valueOf(isRevertNext);
        message.what = 0;
        if (this.mIsCreated) {
            this.mHandler.sendMessage(message);
        } else {
            this.mMessages.add(message);
        }
    }

    private void preparAnimation(boolean isRevertNext) {
        Log.i("fingerprint.FingerprintAnimation", "preparAnimation");
        if (this.mIsRevert) {
            this.mStage--;
        }
        this.mIsRevert = isRevertNext;
        if (!this.mIsRevert) {
            this.mStage++;
        }
        this.mPaths = (ArrayList) AnimationUtil.getmSectionData().get(this.mStage);
        Log.i("fingerprint.FingerprintAnimation", "mStage=" + this.mStage);
    }

    private void drawPath(Canvas canvas) {
        if (this.mPaths != null) {
            for (int i = 0; i < this.mPaths.size(); i++) {
                double length = ((TouchShowPath) this.mPaths.get(i)).allDrawLength();
                this.mPaint.setColor(this.mIsRevert ? this.grayColor : this.drawColor);
                ((TouchShowPath) this.mPaths.get(i)).drawPath(canvas, ((double) this.mAnimValue) * length, this.mClearPaint);
                ((TouchShowPath) this.mPaths.get(i)).drawPath(canvas, ((double) this.mAnimValue) * length, this.mPaint);
            }
        }
    }

    private void init(Context context) {
        this.drawColor = context.getResources().getColor(2131427576);
        this.grayColor = context.getResources().getColor(2131427577);
        this.backgroundColor = context.getResources().getColor(2131427578);
        this.mPaint = new Paint();
        this.mPaint.setStrokeWidth(4.5f);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeCap(Cap.ROUND);
        this.mClearPaint = new Paint();
        this.mClearPaint.setStrokeWidth(8.5f);
        this.mClearPaint.setStyle(Style.STROKE);
        this.mClearPaint.setColor(this.backgroundColor);
        this.mClearPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        this.mClearPaint.setAntiAlias(true);
        this.mClearPaint.setStrokeCap(Cap.ROUND);
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        setZOrderOnTop(true);
        this.mHolder.setFormat(-3);
        this.mAnimatorThread = new HandlerThread("AnimationThread");
        this.mAnimatorThread.setPriority(7);
        this.mAnimatorThread.start();
        this.mHandler = new AnimationHandler(this.mAnimatorThread.getLooper());
    }

    private void drawBackground(Canvas canvas) {
        if (this.mStage == -1) {
            int drawStage = AnimationUtil.getmSectionData().size();
            for (int i = 0; i < drawStage; i++) {
                drawBackgroundPath(canvas, (ArrayList) AnimationUtil.getmSectionData().get(i), Boolean.valueOf(false));
            }
        }
    }

    private void drawBackgroundPath(Canvas canvas, ArrayList<TouchShowPath> path, Boolean alreayDraw) {
        if (this.mPaths != null) {
            for (int i = 0; i < path.size(); i++) {
                double length = ((TouchShowPath) path.get(i)).allDrawLength();
                this.mPaint.setColor(alreayDraw.booleanValue() ? this.drawColor : this.grayColor);
                ((TouchShowPath) path.get(i)).drawPath(canvas, length, this.mPaint);
            }
        }
    }

    public void reset() {
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessage(1);
    }

    private void clear() {
        this.mPaths = new ArrayList();
        this.mIsRevert = false;
        this.mStage = -1;
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    public void surfaceCreated(SurfaceHolder arg0) {
        this.mHandler.sendEmptyMessage(2);
        while (!this.mMessages.isEmpty()) {
            this.mHandler.sendMessage((Message) this.mMessages.remove(0));
        }
        this.mIsCreated = true;
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
        this.mIsCreated = false;
        this.mHandler.sendEmptyMessage(3);
        this.mMessages.clear();
    }

    public void changeBackGroundColor(int color) {
        this.backgroundColor = color;
        this.mClearPaint.setColor(this.backgroundColor);
    }
}
