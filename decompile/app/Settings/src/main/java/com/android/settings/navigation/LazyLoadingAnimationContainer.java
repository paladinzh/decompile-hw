package com.android.settings.navigation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class LazyLoadingAnimationContainer {
    private List<AnimationFrame> mAnimationFrames;
    private Handler mHandler;
    private int mIndex;
    private boolean mIsOneshot;
    private boolean mIsRunning;
    private OnAnimationFrameChangedListener mOnAnimationFrameChangedListener;
    private OnAnimationStoppedListener mOnAnimationStoppedListener;
    private Bitmap mRecycleBitmap;
    private boolean mShouldRun;
    private SoftReference<ImageView> mSoftReferenceImageView;
    private String mTag;

    public interface OnAnimationStoppedListener {
        void onAnimationStopped();
    }

    private class FramesSequenceAnimation implements Runnable {
        private FramesSequenceAnimation() {
        }

        public void run() {
            ImageView imageView = (ImageView) LazyLoadingAnimationContainer.this.mSoftReferenceImageView.get();
            if (!LazyLoadingAnimationContainer.this.shouldRun() || imageView == null) {
                LazyLoadingAnimationContainer.this.mIsRunning = false;
                if (LazyLoadingAnimationContainer.this.mOnAnimationStoppedListener != null) {
                    LazyLoadingAnimationContainer.this.mOnAnimationStoppedListener.onAnimationStopped();
                }
                Log.d("LazyLoadingAnimationContainer", "animation stopped finally : " + LazyLoadingAnimationContainer.this.mTag);
                return;
            }
            LazyLoadingAnimationContainer.this.mIsRunning = true;
            if (imageView.isShown()) {
                AnimationFrame frame = LazyLoadingAnimationContainer.this.getNext();
                if (frame == null) {
                    LazyLoadingAnimationContainer.this.stopInternal();
                    LazyLoadingAnimationContainer.this.mIsRunning = false;
                    if (LazyLoadingAnimationContainer.this.mOnAnimationStoppedListener != null) {
                        LazyLoadingAnimationContainer.this.mOnAnimationStoppedListener.onAnimationStopped();
                    }
                    Log.d("LazyLoadingAnimationContainer", "animation stopped cause onshot : " + LazyLoadingAnimationContainer.this.mTag);
                    return;
                }
                new GetImageDrawableTask(imageView).execute(new Integer[]{Integer.valueOf(frame.getResourceId())});
                LazyLoadingAnimationContainer.this.mHandler.postDelayed(this, (long) frame.getDuration());
            }
        }
    }

    private class GetImageDrawableTask extends AsyncTask<Integer, Void, Drawable> {
        private ImageView mImageView;

        public GetImageDrawableTask(ImageView imageView) {
            this.mImageView = imageView;
        }

        protected Drawable doInBackground(Integer... params) {
            int resId = params[0].intValue();
            if (VERSION.SDK_INT < 11) {
                return this.mImageView.getContext().getResources().getDrawable(resId);
            }
            Options options = new Options();
            options.inMutable = true;
            if (LazyLoadingAnimationContainer.this.mRecycleBitmap != null) {
                options.inBitmap = LazyLoadingAnimationContainer.this.mRecycleBitmap;
            }
            LazyLoadingAnimationContainer.this.mRecycleBitmap = BitmapFactory.decodeResource(this.mImageView.getContext().getResources(), resId, options);
            return new BitmapDrawable(this.mImageView.getContext().getResources(), LazyLoadingAnimationContainer.this.mRecycleBitmap);
        }

        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            if (result != null) {
                this.mImageView.setImageDrawable(result);
            }
            if (LazyLoadingAnimationContainer.this.mOnAnimationFrameChangedListener != null) {
                LazyLoadingAnimationContainer.this.mOnAnimationFrameChangedListener.onAnimationFrameChanged(LazyLoadingAnimationContainer.this.mIndex);
            }
        }
    }

    public interface OnAnimationFrameChangedListener {
        void onAnimationFrameChanged(int i);
    }

    public LazyLoadingAnimationContainer(ImageView imageView) {
        init(imageView);
    }

    public void init(ImageView imageView) {
        this.mAnimationFrames = new ArrayList();
        this.mSoftReferenceImageView = new SoftReference(imageView);
        this.mHandler = new Handler();
        if (this.mIsRunning) {
            stop();
        }
        this.mShouldRun = false;
        this.mIsRunning = false;
        this.mIndex = -1;
    }

    public void addAllFrames(int[] resIds, int interval) {
        for (int resId : resIds) {
            this.mAnimationFrames.add(new AnimationFrame(resId, interval));
        }
    }

    private AnimationFrame getNext() {
        this.mIndex++;
        if (this.mIndex >= this.mAnimationFrames.size()) {
            this.mIndex = 0;
            if (this.mIsOneshot) {
                return null;
            }
        }
        return (AnimationFrame) this.mAnimationFrames.get(this.mIndex);
    }

    public void setOnAnimationStoppedListener(OnAnimationStoppedListener listener) {
        this.mOnAnimationStoppedListener = listener;
    }

    public OnAnimationStoppedListener getOnAnimationStoppedListener() {
        return this.mOnAnimationStoppedListener;
    }

    public synchronized void start() {
        this.mShouldRun = true;
        if (this.mIsRunning) {
            Log.d("LazyLoadingAnimationContainer", "already running, do not start!");
            return;
        }
        this.mIndex = -1;
        Log.d("LazyLoadingAnimationContainer", "animation cmd start : " + this.mTag + ", mIndex = " + this.mIndex);
        this.mHandler.post(new FramesSequenceAnimation());
    }

    public synchronized void stop() {
        Log.d("LazyLoadingAnimationContainer", "animation cmd stop : " + this.mTag);
        this.mShouldRun = false;
    }

    public boolean isRunning() {
        return this.mIsRunning;
    }

    public synchronized boolean shouldRun() {
        return this.mShouldRun;
    }

    private synchronized void stopInternal() {
        this.mShouldRun = false;
    }

    public void setOneshot(boolean oneshot) {
        this.mIsOneshot = oneshot;
    }

    public void setTag(String tag) {
        this.mTag = tag;
    }
}
