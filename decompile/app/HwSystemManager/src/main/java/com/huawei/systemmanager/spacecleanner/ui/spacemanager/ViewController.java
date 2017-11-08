package com.huawei.systemmanager.spacecleanner.ui.spacemanager;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.anima.SimpleAnimatorListener;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.spacecleanner.SpaceCleannerManager;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.DeepItem;
import com.huawei.systemmanager.spacecleanner.utils.VedioCacheUtils;
import com.huawei.systemmanager.util.HwLog;

public class ViewController {
    private static final String TAG = "ViewController";
    private final ImageView arrow;
    private final View completeIcon;
    private final TextView desView;
    private final ImageView iconView;
    private final TextView infrequentlyTip;
    private final Context mContext;
    private final DeepItem mItem;
    private final ViewGroup mMainView;
    private boolean mViewRemoved = false;
    private final View progresBar;
    private final TextView recommendedTip;
    private final ImageView tip;
    private final TextView titleView;

    public ViewController(ViewGroup view, OnClickListener btnClicker, DeepItem item) {
        this.mMainView = view;
        this.mContext = view.getContext();
        this.iconView = (ImageView) view.findViewById(R.id.icon);
        this.titleView = (TextView) view.findViewById(R.id.title);
        this.desView = (TextView) view.findViewById(R.id.description);
        this.recommendedTip = (TextView) view.findViewById(R.id.recommended_tip);
        this.infrequentlyTip = (TextView) view.findViewById(R.id.infrequently_tip);
        this.tip = (ImageView) view.findViewById(R.id.red_tip);
        this.arrow = (ImageView) view.findViewById(R.id.arrow);
        this.progresBar = view.findViewById(R.id.progress_bar);
        this.completeIcon = view.findViewById(R.id.complete_icon);
        this.mItem = item;
        bindView();
    }

    private void bindView() {
        Context ctx = this.mContext;
        DeepItem item = this.mItem;
        this.iconView.setImageDrawable(item.getIcon(ctx));
        this.titleView.setText(item.getTitle(ctx));
        this.arrow.setTag(item);
        if (!item.isFinished()) {
            this.progresBar.setVisibility(0);
            this.arrow.setVisibility(4);
            this.desView.setVisibility(8);
            this.recommendedTip.setVisibility(8);
            if (this.infrequentlyTip != null) {
                this.infrequentlyTip.setVisibility(8);
            }
            this.completeIcon.setVisibility(8);
        } else if (item.isEmpty()) {
            this.progresBar.setVisibility(8);
            this.arrow.setVisibility(8);
            this.desView.setVisibility(8);
            this.recommendedTip.setVisibility(8);
            if (this.infrequentlyTip != null) {
                this.infrequentlyTip.setVisibility(8);
            }
            this.completeIcon.setVisibility(0);
        } else {
            this.progresBar.setVisibility(8);
            this.arrow.setVisibility(0);
            this.desView.setVisibility(0);
            this.recommendedTip.setVisibility(item.showTip() ? 0 : 8);
            if (this.infrequentlyTip != null) {
                SpaceCleannerManager.getInstance();
                if (SpaceCleannerManager.isSupportHwFileAnalysis()) {
                    int i;
                    TextView textView = this.infrequentlyTip;
                    if (item.showInfrequentlyTip()) {
                        i = 0;
                    } else {
                        i = 8;
                    }
                    textView.setVisibility(i);
                } else {
                    this.infrequentlyTip.setVisibility(8);
                }
            }
            this.desView.setText(item.getDescription(ctx));
            this.completeIcon.setVisibility(4);
            if (6 == item.getDeepItemType() && VedioCacheUtils.isRedPoint()) {
                this.tip.setVisibility(0);
            } else {
                this.tip.setVisibility(8);
            }
        }
        setViewInOneline();
    }

    public DeepItem getItem() {
        return this.mItem;
    }

    public void checkIfFinished() {
        if (this.mViewRemoved) {
            HwLog.i(TAG, "checkIfFinished called, but view is reomved, do nothing");
            return;
        }
        DeepItem item = this.mItem;
        if (!item.shouldCheckFinished()) {
            showDescript();
        } else if (item.isFinished()) {
            if (item.isEmpty()) {
                HwLog.i(TAG, "checkIfFinished dismiss item, item tag:" + item.getTag());
                completeView();
            } else {
                HwLog.i(TAG, "checkIfFinished update item info, item tag:" + item.getTag());
                showDescript();
            }
        } else {
            HwLog.i(TAG, "checkIfFinished item is not finished");
        }
    }

    public void updateState() {
        if (this.mViewRemoved) {
            HwLog.i(TAG, "updateState called, but view is reomved, do nothing");
        } else if (!this.mItem.shouldCheckFinished()) {
            showDescript();
        } else if (this.mItem.isFinished()) {
            if (this.mItem.isEmpty()) {
                HwLog.i(TAG, "updateState, change item to complete state, item tag:" + this.mItem.getTag());
                removeViewFromParent();
            } else {
                HwLog.i(TAG, "updateState, update item info, item tag:" + this.mItem.getTag());
                showDescript();
            }
        } else {
            HwLog.i(TAG, "updateState item is not finished");
        }
    }

    private void completeView() {
        AnimatorInflater.loadAnimator(this.mContext, R.animator.space_clean_view_disappear_anima).setTarget(this.progresBar);
        Animator completeIconAnima = AnimatorInflater.loadAnimator(this.mContext, R.animator.space_clean_view_appear_anima);
        completeIconAnima.setTarget(this.completeIcon);
        completeIconAnima.addListener(new SimpleAnimatorListener() {
            public void onAnimationStart(Animator animation) {
                ViewController.this.completeIcon.setVisibility(0);
                ViewController.this.completeIcon.setAlpha(0.0f);
            }
        });
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(new Animator[]{progressBarAnima, completeIconAnima});
        set.addListener(new SimpleAnimatorListener() {
            public void onAnimationEnd(Animator animation) {
                ViewController.this.removeViewFromParent();
            }
        });
        set.start();
    }

    private void showDescript() {
        if (this.desView.getVisibility() == 0) {
            this.desView.setText(this.mItem.getDescription(this.mContext));
            if (this.recommendedTip != null) {
                this.recommendedTip.setVisibility(this.mItem.showTip() ? 0 : 8);
            }
            if (this.infrequentlyTip != null) {
                int i;
                TextView textView = this.infrequentlyTip;
                if (this.mItem.showInfrequentlyTip()) {
                    i = 0;
                } else {
                    i = 8;
                }
                textView.setVisibility(i);
            }
            if (this.tip != null && 6 == this.mItem.getDeepItemType()) {
                if (VedioCacheUtils.isRedPoint()) {
                    this.tip.setVisibility(0);
                } else {
                    this.tip.setVisibility(8);
                }
            }
            return;
        }
        Animator progressBarAnima = AnimatorInflater.loadAnimator(this.mContext, R.animator.space_clean_view_disappear_anima);
        progressBarAnima.setTarget(this.progresBar);
        progressBarAnima.addListener(new SimpleAnimatorListener() {
            public void onAnimationEnd(Animator animation) {
                int i;
                ViewController.this.progresBar.setVisibility(8);
                ViewController.this.desView.setVisibility(0);
                if (ViewController.this.recommendedTip != null) {
                    TextView -get7 = ViewController.this.recommendedTip;
                    if (ViewController.this.mItem.showTip()) {
                        i = 0;
                    } else {
                        i = 8;
                    }
                    -get7.setVisibility(i);
                }
                if (ViewController.this.infrequentlyTip != null) {
                    -get7 = ViewController.this.infrequentlyTip;
                    if (ViewController.this.mItem.showInfrequentlyTip()) {
                        i = 0;
                    } else {
                        i = 8;
                    }
                    -get7.setVisibility(i);
                }
                if (ViewController.this.tip != null && 6 == ViewController.this.mItem.getDeepItemType()) {
                    if (VedioCacheUtils.isRedPoint()) {
                        ViewController.this.tip.setVisibility(0);
                    } else {
                        ViewController.this.tip.setVisibility(8);
                    }
                }
            }
        });
        Animator showBtnAnima = buildShowBtnAnima();
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(new Animator[]{progressBarAnima, showBtnAnima});
        set.start();
    }

    private Animator buildShowBtnAnima() {
        ValueAnimator showBtnAnima = (ValueAnimator) AnimatorInflater.loadAnimator(this.mContext, R.animator.space_clean_view_appear_anima);
        showBtnAnima.setTarget(this.arrow);
        final MarginLayoutParams desviewParam = (MarginLayoutParams) this.desView.getLayoutParams();
        showBtnAnima.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                desviewParam.bottomMargin = (int) ((animation.getAnimatedFraction() - Utility.ALPHA_MAX) * 60.0f);
                ViewController.this.desView.requestLayout();
            }
        });
        showBtnAnima.addListener(new SimpleAnimatorListener() {
            public void onAnimationStart(Animator animation) {
                if (ViewController.this.arrow != null) {
                    ViewController.this.arrow.setVisibility(0);
                }
                desviewParam.bottomMargin = -60;
                ViewController.this.desView.setVisibility(0);
                ViewController.this.desView.setText(ViewController.this.mItem.getDescription(ViewController.this.mContext));
            }

            public void onAnimationEnd(Animator animation) {
                desviewParam.bottomMargin = 0;
                ViewController.this.desView.requestLayout();
            }
        });
        return showBtnAnima;
    }

    private void removeViewFromParent() {
        ViewParent parent = this.mMainView.getParent();
        if (parent == null) {
            HwLog.e(TAG, "removeViewFromParent, the parent of mainview is null!!");
            return;
        }
        ((ViewGroup) parent).removeView(this.mMainView);
        this.mViewRemoved = true;
    }

    private void setViewInOneline() {
        if (GlobalContext.getContext().getResources().getBoolean(R.bool.spaceclean_item_oneline)) {
            HwLog.i(TAG, "Abric need oneline");
            this.titleView.setMaxLines(1);
            this.titleView.setWidth(dp2px(GlobalContext.getContext(), 180.0f));
            this.desView.setMaxLines(1);
        }
    }

    private int dp2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static ViewController create(LayoutInflater inflater, ViewGroup parent, OnClickListener clicker, DeepItem item, TrashScanHandler scanHandler) {
        if (item.shouldCheckFinished()) {
            if (item.isFinished() && item.isEmpty()) {
                HwLog.i(TAG, "create, but item is finished and empty, not show. item tag:" + item.getTag());
                return null;
            }
        } else if (!item.isDeepItemDisplay(scanHandler)) {
            HwLog.i(TAG, "create, but item is not deep item display, not show. item tag:" + item.getTag());
            return null;
        }
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.spacemanager_item_finish, parent, false);
        ViewController controller = new ViewController(view, clicker, item);
        view.setOnClickListener(clicker);
        view.setTag(item);
        parent.addView(view);
        return controller;
    }
}
