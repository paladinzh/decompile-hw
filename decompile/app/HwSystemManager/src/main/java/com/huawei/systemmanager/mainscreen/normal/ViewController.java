package com.huawei.systemmanager.mainscreen.normal;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.anima.SimpleAnimatorListener;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.mainscreen.detector.item.DetectItem;
import com.huawei.systemmanager.util.HwLog;

public class ViewController {
    private static final String TAG = "ViewController";
    private final View completeIcon;
    private final TextView desView;
    private Animator mAnima;
    private final Context mContext;
    private final DetectItemEx mItem;
    private final View mMainView;
    private final View progresBar;
    private final TextView titleView;

    public ViewController(View view, DetectItemEx item) {
        this.mMainView = view;
        this.mContext = view.getContext();
        this.titleView = (TextView) view.findViewById(R.id.TextView_title);
        this.desView = (TextView) view.findViewById(R.id.TextView_des);
        this.progresBar = view.findViewById(R.id.progress_bar);
        this.completeIcon = view.findViewById(R.id.complete_icon);
        this.mItem = item;
        bindView();
    }

    public View getView() {
        return this.mMainView;
    }

    private void bindView() {
        Context ctx = this.mContext;
        int titleId = R.string.space_optimize_completed;
        switch (this.mItem.getmType()) {
            case 1:
                titleId = R.string.space_optimize_performance;
                break;
            case 2:
                titleId = R.string.space_optimize_security;
                break;
            case 3:
                titleId = R.string.space_optimize_battery;
                break;
            case 4:
                titleId = R.string.space_optimize_manage;
                break;
        }
        this.titleView.setText(ctx.getString(titleId));
        this.progresBar.setVisibility(0);
        this.completeIcon.setVisibility(8);
    }

    public void updateItemView(DetectItem currentItem) {
        DetectItem item = currentItem;
        this.desView.setText(currentItem.getTitle(this.mContext));
        this.desView.setVisibility(0);
    }

    public void completeView() {
        this.desView.setText(this.mContext.getString(R.string.space_optimize_completed));
        playScanEnd();
    }

    public static ViewController create(LayoutInflater inflater, ViewGroup parent, DetectItemEx optitem) {
        View view = inflater.inflate(R.layout.main_screen_optimize_process_item, null);
        ViewController controller = new ViewController(view, optitem);
        parent.addView(view);
        return controller;
    }

    private Animator buildAnimator() {
        Context ctx = GlobalContext.getContext();
        Animator anima1 = AnimatorInflater.loadAnimator(ctx, R.animator.space_clean_view_disappear_anima);
        anima1.setTarget(this.progresBar);
        anima1.addListener(new SimpleAnimatorListener() {
            public void onAnimationEnd(Animator animation) {
                ViewController.this.progresBar.setVisibility(8);
            }
        });
        Animator anima2 = AnimatorInflater.loadAnimator(ctx, R.animator.space_clean_view_appear_anima);
        anima2.setTarget(this.completeIcon);
        anima2.addListener(new SimpleAnimatorListener() {
            public void onAnimationStart(Animator animation) {
                ViewController.this.completeIcon.setVisibility(0);
                ViewController.this.completeIcon.setAlpha(0.0f);
                ViewController.this.completeIcon.setScaleX(0.0f);
                ViewController.this.completeIcon.setScaleY(0.0f);
            }
        });
        AnimatorSet result = new AnimatorSet();
        result.playSequentially(new Animator[]{anima1, anima2});
        return result;
    }

    public void playScanEnd() {
        if (this.completeIcon.getVisibility() == 0) {
            HwLog.i(TAG, "playScanEnd, end flag is visiable, need not play");
        } else if (this.mAnima != null) {
            HwLog.e(TAG, "playScanEnd, mAnima != null, something wrong!");
        } else {
            this.mAnima = buildAnimator();
            this.mAnima.addListener(new SimpleAnimatorListener() {
                public void onAnimationEnd(Animator animation) {
                    ViewController.this.mAnima = null;
                }
            });
            HwLog.i(TAG, "playScanEnd, start anima safe");
            this.mAnima.start();
        }
    }
}
