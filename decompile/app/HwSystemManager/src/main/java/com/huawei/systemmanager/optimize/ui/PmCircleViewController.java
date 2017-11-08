package com.huawei.systemmanager.optimize.ui;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnDrawListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.RollingCommand;
import com.huawei.systemmanager.comm.widget.SimpleTextView;
import com.huawei.systemmanager.util.HwLog;

public class PmCircleViewController {
    private static final long DEFALUT_DURATION = 1300;
    private static final int DEFAULT_DATA = 30;
    private static final String TAG = "PmCircleViewController";
    private final ProcessManagerCircleView mCircleView;
    private int mData = -1;
    private RollingCommand mDoubleRollingCommand = new RollingCommand() {
        protected void onNumberUpate(int count) {
            PmCircleViewController.this.mNumberView.setText(String.valueOf(count));
            PmCircleViewController.this.mCircleView.setProgress((float) count);
        }
    };
    private OnDrawListener mDrawListener = new OnDrawListener() {
        public void onDraw() {
            ViewTreeObserver observer = PmCircleViewController.this.mNumberView.getViewTreeObserver();
            if (observer != null && observer.isAlive()) {
                observer.removeOnDrawListener(PmCircleViewController.this.mDrawListener);
            }
            if (PmCircleViewController.this.mData >= 0) {
                HwLog.i(PmCircleViewController.TAG, "the data is set, need not start pre-anima");
            } else {
                PmCircleViewController.this.startPreAnimaInner();
            }
        }
    };
    private final SimpleTextView mNumberView;

    public PmCircleViewController(View view) {
        this.mNumberView = (SimpleTextView) view.findViewById(R.id.memory_used_perecent);
        this.mCircleView = (ProcessManagerCircleView) view.findViewById(R.id.circle_view);
        this.mDoubleRollingCommand.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    public void startPreAnima() {
        ViewTreeObserver observer = this.mNumberView.getViewTreeObserver();
        if (observer != null && observer.isAlive()) {
            observer.addOnDrawListener(this.mDrawListener);
        }
        startPreAnimaInner();
    }

    public void setDataWithAnima(int data) {
        long duration = (long) (this.mData < 0 ? 1000 : 200);
        this.mData = data;
        this.mDoubleRollingCommand.setNewTarget(data, duration);
    }

    private void startPreAnimaInner() {
        HwLog.i(TAG, "startPreAnimaInner");
        this.mDoubleRollingCommand.setNewTarget(0, 30, DEFALUT_DURATION, new LinearInterpolator());
    }
}
