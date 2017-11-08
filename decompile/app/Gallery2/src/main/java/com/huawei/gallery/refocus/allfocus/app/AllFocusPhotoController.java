package com.huawei.gallery.refocus.allfocus.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.animation.LinearInterpolator;
import com.android.gallery3d.R;
import com.android.gallery3d.ui.BitmapScreenNail;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.refocus.allfocus.app.AllFocusPhotoImpl.AllFocusPhotoListener;
import com.huawei.gallery.refocus.app.AbsRefocusController;
import com.huawei.gallery.refocus.app.AbsRefocusDelegate;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

public class AllFocusPhotoController extends AbsRefocusController implements AllFocusPhotoListener {
    private AllFocusPhotoImpl mAllFocusPhoto = new AllFocusPhotoImpl(this.mDelegate.getFilePath(), this.mPhotoWidth, this.mPhotoHeight);
    private boolean mAllFocusPhotoChanged;
    private boolean mExit = false;
    private Handler mHandler;
    private volatile boolean mIsbusy = false;
    private Object mLock = new Object();
    private Thread mPrepareThread;
    private TransitionAnimation mTransitionAnimation;

    private class TransitionAnimation {
        ValueAnimator mAnimation;
        Iterator<BitmapScreenNail> mMapIterator;
        int mRate;

        private TransitionAnimation() {
        }

        void setSource(Collection<BitmapScreenNail> bitmapCollection) {
            if (bitmapCollection != null) {
                this.mMapIterator = bitmapCollection.iterator();
                this.mAnimation = ValueAnimator.ofInt(new int[]{1, bitmapCollection.size()});
                this.mAnimation.setDuration(((long) bitmapCollection.size()) * 50);
                this.mAnimation.setInterpolator(new LinearInterpolator());
                this.mAnimation.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int rate = ((Integer) animation.getAnimatedValue()).intValue();
                        if (TransitionAnimation.this.mRate != rate) {
                            TransitionAnimation.this.mRate = rate;
                            if (TransitionAnimation.this.mMapIterator.hasNext()) {
                                BitmapScreenNail screenNail = (BitmapScreenNail) TransitionAnimation.this.mMapIterator.next();
                                if (screenNail != null) {
                                    AllFocusPhotoController.this.mDelegate.refreshPhoto(screenNail);
                                }
                            }
                        }
                    }
                });
                this.mAnimation.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                    }

                    public void onAnimationCancel(Animator animation) {
                        AllFocusPhotoController.this.mDelegate.refreshPhoto(AllFocusPhotoController.this.mAllFocusPhoto.getPhotoData(0), 0, AllFocusPhotoController.this.mAllFocusPhoto.getPhotoLength(0));
                        synchronized (AllFocusPhotoController.this.mLock) {
                            AllFocusPhotoController.this.mIsbusy = false;
                        }
                        super.onAnimationCancel(animation);
                    }

                    public void onAnimationEnd(Animator animation) {
                        AllFocusPhotoController.this.mDelegate.refreshPhoto(AllFocusPhotoController.this.mAllFocusPhoto.getPhotoData(0), 0, AllFocusPhotoController.this.mAllFocusPhoto.getPhotoLength(0));
                        synchronized (AllFocusPhotoController.this.mLock) {
                            AllFocusPhotoController.this.mIsbusy = false;
                        }
                        super.onAnimationEnd(animation);
                    }
                });
            }
        }

        void start() {
            this.mRate = 0;
            if (this.mAnimation != null) {
                this.mAnimation.start();
            }
        }

        void cancel() {
            if (this.mAnimation != null && this.mAnimation.isRunning()) {
                this.mAnimation.cancel();
            }
        }
    }

    public AllFocusPhotoController(Context context, AbsRefocusDelegate delegate) {
        super(context, delegate);
        this.mAllFocusPhoto.setAllFocusPhotoListener(this);
        this.mTransitionAnimation = new TransitionAnimation();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        File newFile = new File((String) msg.obj);
                        if (msg.arg1 == 0) {
                            RefocusMediaScannerClient refocusMediaScannerClient = new RefocusMediaScannerClient(AllFocusPhotoController.this.mContext, newFile);
                            return;
                        } else if (-1 == msg.arg1) {
                            AbsRefocusController.showHint(AllFocusPhotoController.this.mContext, AllFocusPhotoController.this.mContext.getString(R.string.photoshare_toast_nospace_Toast), 0);
                            if (newFile.exists() && !newFile.delete()) {
                                newFile.deleteOnExit();
                                return;
                            }
                            return;
                        } else {
                            return;
                        }
                    case 2:
                        AllFocusPhotoController.this.mPrepareThread = null;
                        if (!AllFocusPhotoController.this.mExit) {
                            AllFocusPhotoController.this.mPrepareComplete = true;
                            AllFocusPhotoController.this.mDelegate.preparePhotoComplete();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public boolean prepare() {
        this.mPrepareThread = new Thread(new Runnable() {
            public void run() {
                AllFocusPhotoController.this.mAllFocusPhoto.prepare();
            }
        });
        this.mPrepareThread.start();
        return true;
    }

    public void resizePhoto() {
        this.mPhotoWidth = this.mDelegate.getPhotoWidth();
        this.mPhotoHeight = this.mDelegate.getPhotoHeight();
        checkIfNeedSwapPhotoWidthAndHeight();
        this.mAllFocusPhoto.resizePhoto(this.mPhotoWidth, this.mPhotoHeight);
    }

    public boolean doRefocus(Point touchPoint) {
        if (this.mPrepareThread != null || !this.mAllFocusPhoto.isRefocusPhoto()) {
            return false;
        }
        Point refocusPoint = this.mDelegate.getTouchPositionInImage(touchPoint);
        if (refocusPoint.x < 0 || refocusPoint.x > this.mPhotoWidth || refocusPoint.y < 0 || refocusPoint.y > this.mPhotoHeight) {
            return false;
        }
        synchronized (this.mLock) {
            if (this.mIsbusy) {
                return false;
            }
            this.mDelegate.showFocusIndicator(touchPoint);
            this.mIsbusy = true;
            if (this.mAllFocusPhoto.doRefocus(refocusPoint) == 0) {
                this.mIsbusy = false;
                return false;
            } else if (this.mAllFocusPhoto.getPhotoData(0) == null || this.mAllFocusPhoto.getPhotoLength(0) == 0) {
                this.mIsbusy = false;
                return false;
            } else {
                Collection<BitmapScreenNail> bitmapCollection = this.mAllFocusPhoto.getTransitionBitmap();
                if (bitmapCollection != null) {
                    this.mTransitionAnimation.cancel();
                    this.mTransitionAnimation.setSource(bitmapCollection);
                    this.mTransitionAnimation.start();
                } else {
                    this.mDelegate.refreshPhoto(this.mAllFocusPhoto.getPhotoData(0), 0, this.mAllFocusPhoto.getPhotoLength(0));
                    this.mIsbusy = false;
                }
                this.mAllFocusPhotoChanged = true;
                return true;
            }
        }
    }

    public void onGotFocusPoint() {
        this.mDelegate.onGotFocusPoint();
    }

    public void onPrepareComplete() {
        this.mHandler.obtainMessage(2).sendToTarget();
    }

    public void onSaveFileComplete(int saveState) {
        synchronized (this.mLock) {
            this.mAllFocusPhotoChanged = false;
            this.mIsbusy = false;
        }
        this.mDelegate.saveFileComplete(saveState);
    }

    public void onSaveAsComplete(int saveState, String filePath) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, saveState, 0, filePath));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean saveFileIfNecessary() {
        if (this.mPrepareThread != null) {
            return false;
        }
        synchronized (this.mLock) {
            if (!this.mAllFocusPhotoChanged || this.mIsbusy) {
            } else {
                this.mIsbusy = true;
                this.mAllFocusPhoto.saveFile();
                return true;
            }
        }
    }

    public boolean ifPhotoChanged() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mAllFocusPhotoChanged;
        }
        return z;
    }

    public void saveAs() {
        String filePath = getFilePath();
        if (this.mPrepareThread != null || !this.mAllFocusPhoto.isRefocusPhoto()) {
            this.mDelegate.saveAsComplete(-2);
        } else if (filePath == null) {
            GalleryLog.e("AllFocusPhotoController", "Cannot get file path, return.");
            this.mDelegate.saveAsComplete(-2);
        } else {
            synchronized (this.mLock) {
                if (this.mIsbusy) {
                    return;
                }
                this.mIsbusy = true;
                this.mAllFocusPhoto.saveAs(filePath);
            }
        }
    }

    public void cleanUp() {
        this.mExit = true;
        checkAndWaitPrepareComplete();
        synchronized (this.mLock) {
            this.mIsbusy = true;
        }
        if (this.mAllFocusPhoto != null) {
            this.mAllFocusPhoto.cleanupResource();
        }
    }

    public void showFocusIndicator() {
        if (this.mAllFocusPhoto.isRefocusPhoto()) {
            this.mDelegate.showFocusIndicator(this.mDelegate.transformToScreenCoordinate(this.mAllFocusPhoto.getFocusPoint()));
        }
    }

    public boolean isRefocusPhoto() {
        return this.mAllFocusPhoto.isRefocusPhoto();
    }

    public boolean isSupportAdjustFocus() {
        return this.mAllFocusPhoto.isSupportAdjustFocus();
    }

    public void adjustFocus(int direction) {
        synchronized (this.mLock) {
            if (this.mIsbusy) {
                return;
            }
            this.mIsbusy = true;
            if (this.mAllFocusPhoto != null && this.mAllFocusPhoto.isRefocusPhoto()) {
                int retVal = this.mAllFocusPhoto.pickPhotoByFocalLength(direction);
                if (retVal == 2) {
                    this.mDelegate.finishRefocus();
                } else if (retVal == 1) {
                    this.mDelegate.refreshPhoto(this.mAllFocusPhoto.getPhotoData(0), 0, this.mAllFocusPhoto.getPhotoLength(0));
                    this.mAllFocusPhotoChanged = true;
                } else {
                    this.mDelegate.finishRefocus();
                }
            }
            this.mIsbusy = false;
        }
    }

    protected void checkIfNeedSwapPhotoWidthAndHeight() {
        if (this.mAllFocusPhoto.getExifOrientation() == 90 || this.mAllFocusPhoto.getExifOrientation() == 270) {
            this.mPhotoWidth ^= this.mPhotoHeight;
            this.mPhotoHeight ^= this.mPhotoWidth;
            this.mPhotoWidth ^= this.mPhotoHeight;
            this.mAllFocusPhoto.resizePhoto(this.mPhotoWidth, this.mPhotoHeight);
        }
    }

    private void checkAndWaitPrepareComplete() {
        try {
            if (this.mPrepareThread != null) {
                this.mAllFocusPhoto.cancelPrepare();
                this.mPrepareThread.join();
            }
        } catch (InterruptedException e) {
            GalleryLog.i("AllFocusPhotoController", "Thread.join() failed, reason: InterruptedException.");
        }
    }
}
