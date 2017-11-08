package com.android.systemui.linkplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.R;

public class LinkPlusView extends FrameLayout {
    private static final String TAG = LinkPlusView.class.getSimpleName();
    AnimationListener mAnimationListener = new AnimationListener() {
        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (animation.equals(LinkPlusView.this.mIconAlphaAnimation)) {
                LinkPlusView.this.mHandler.sendEmptyMessageDelayed(6, 2000);
            } else if (!animation.equals(LinkPlusView.this.mSwitchingAlphaAnimation)) {
            } else {
                if (2 > LinkPlusView.this.mTimes + 1) {
                    LinkPlusView linkPlusView = LinkPlusView.this;
                    linkPlusView.mTimes = linkPlusView.mTimes + 1;
                    LinkPlusView.this.mHandler.sendEmptyMessage(4);
                    return;
                }
                LinkPlusView.this.mTimes = 0;
                LinkPlusView.this.mHandler.sendEmptyMessage(5);
            }
        }
    };
    private int mCurrentState;
    ImageView mDataImageView;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    LinkPlusView.this.startAnimationShowIconView(LinkPlusView.this.mDataImageView);
                    return;
                case 2:
                    LinkPlusView.this.startAnimationShowIconView(LinkPlusView.this.mWifiImageView);
                    return;
                case 3:
                    LinkPlusView.this.show();
                    return;
                case 4:
                    LinkPlusView.this.mSwitchingImageView.startAnimation(LinkPlusView.this.mSwitchingAlphaAnimation);
                    return;
                case 5:
                    LinkPlusView.this.mSwitchingImageView.setAlpha(0.3f);
                    LinkPlusView.this.mHandler.sendEmptyMessage(LinkPlusView.this.mCurrentState);
                    return;
                case 6:
                    LinkPlusView.this.hidden();
                    return;
                default:
                    return;
            }
        }
    };
    Animation mIconAlphaAnimation;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getAction() == null || !"huawei.wifi.pro.NETWORK_CHANGE".equals(intent.getAction()))) {
                LinkPlusView.this.mCurrentState = intent.getIntExtra("extra_network_change_type", 0);
                Log.i(LinkPlusView.TAG, "mCurrentState: " + LinkPlusView.this.mCurrentState);
                LinkPlusView.this.mHandler.sendEmptyMessage(3);
            }
        }
    };
    private boolean mRegister;
    Animation mSwitchingAlphaAnimation;
    ImageView mSwitchingImageView;
    private int mTimes;
    ImageView mWifiImageView;

    public LinkPlusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        this.mSwitchingImageView = (ImageView) findViewById(R.id.link_plus_switching);
        this.mDataImageView = (ImageView) findViewById(R.id.link_plus_data);
        this.mWifiImageView = (ImageView) findViewById(R.id.link_plus_wifi);
        super.onFinishInflate();
    }

    protected void onAttachedToWindow() {
        if (!this.mRegister) {
            this.mRegister = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("huawei.wifi.pro.NETWORK_CHANGE");
            getContext().registerReceiver(this.mReceiver, filter, "huawei.permission.RECEIVE_WIFI_PRO_STATE", null);
        }
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        if (this.mRegister) {
            this.mRegister = false;
            getContext().unregisterReceiver(this.mReceiver);
        }
        super.onDetachedFromWindow();
    }

    private void hidden() {
        this.mSwitchingImageView.setVisibility(8);
        this.mWifiImageView.setVisibility(8);
        this.mDataImageView.setVisibility(8);
    }

    private void createIconAlphaAnimation() {
        this.mIconAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        this.mIconAlphaAnimation.setDuration(334);
        this.mIconAlphaAnimation.setAnimationListener(this.mAnimationListener);
    }

    private void startAnimationShowIconView(ImageView showView) {
        showView.setVisibility(0);
        showView.setAlpha(1.0f);
        showView.startAnimation(this.mIconAlphaAnimation);
    }

    private void show() {
        this.mSwitchingImageView.setVisibility(0);
        if (this.mSwitchingAlphaAnimation == null) {
            this.mSwitchingAlphaAnimation = new AlphaAnimation(0.3f, 1.0f);
            this.mSwitchingAlphaAnimation.setRepeatCount(1);
            this.mSwitchingAlphaAnimation.setRepeatMode(2);
            this.mSwitchingAlphaAnimation.setDuration(1332);
            this.mSwitchingAlphaAnimation.setAnimationListener(this.mAnimationListener);
        } else {
            this.mSwitchingAlphaAnimation.cancel();
        }
        if (this.mIconAlphaAnimation == null) {
            createIconAlphaAnimation();
        } else {
            this.mIconAlphaAnimation.cancel();
        }
        this.mSwitchingImageView.setAlpha(1.0f);
        this.mSwitchingImageView.startAnimation(this.mSwitchingAlphaAnimation);
    }
}
