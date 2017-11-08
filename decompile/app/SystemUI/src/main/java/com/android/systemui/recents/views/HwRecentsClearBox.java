package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.R$styleable;
import com.android.systemui.utils.analyze.BDReporter;
import com.android.systemui.utils.analyze.PerfDebugUtils;

public class HwRecentsClearBox extends FrameLayout implements OnClickListener, OnLongClickListener, OnTouchListener {
    private static final boolean IS_HIDE_MEM_INFO = SystemProperties.getBoolean("ro.config.vicky_demo_6G", false);
    private CallBack mCallBack;
    private ImageView mClearAllBackground;
    private View mContent;
    private final int mContentId;
    private View mHandle;
    private final int mHandleId;

    public interface CallBack {
        void removeAll();
    }

    public HwRecentsClearBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwRecentsClearBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.SlidingDrawer, defStyle, 0);
        int handleId = a.getResourceId(0, 0);
        if (handleId == 0) {
            throw new IllegalArgumentException("The handle attribute is required and must refer to a valid child.");
        }
        int contentId = a.getResourceId(1, 0);
        if (contentId == 0) {
            throw new IllegalArgumentException("The content attribute is required and must refer to a valid child.");
        } else if (handleId == contentId) {
            throw new IllegalArgumentException("The content and handle attributes must refer to different children.");
        } else {
            this.mHandleId = handleId;
            this.mContentId = contentId;
            a.recycle();
            setAlwaysDrawnWithCacheEnabled(false);
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                this.mClearAllBackground.setAlpha(1.0f);
                this.mClearAllBackground.setVisibility(0);
                break;
            case 1:
            case 3:
                this.mClearAllBackground.animate().alpha(0.0f).setDuration(300).setListener(new AnimatorListener() {
                    public void onAnimationStart(Animator arg0) {
                    }

                    public void onAnimationRepeat(Animator arg0) {
                    }

                    public void onAnimationEnd(Animator arg0) {
                        HwRecentsClearBox.this.mClearAllBackground.setVisibility(4);
                    }

                    public void onAnimationCancel(Animator arg0) {
                    }
                }).start();
                break;
        }
        return false;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mHandle = findViewById(this.mHandleId);
        if (this.mHandle == null) {
            throw new IllegalArgumentException("The handle attribute is must refer to an existing child.");
        }
        this.mContent = findViewById(this.mContentId);
        if (this.mContent == null) {
            throw new IllegalArgumentException("The content attribute is must refer to an existing child.");
        }
        this.mContent.setVisibility(IS_HIDE_MEM_INFO ? 8 : 0);
        this.mHandle.setOnClickListener(this);
        this.mHandle.setOnTouchListener(this);
        this.mHandle.setOnLongClickListener(this);
        this.mClearAllBackground = (ImageView) findViewById(R.id.clear_all_recents_image_button_down);
    }

    public void setCallback(CallBack callBack) {
        this.mCallBack = callBack;
    }

    public void onClick(View v) {
        PerfDebugUtils.perfRecentsRemoveAllElapsedTimeBegin(1);
        BDReporter.c(getContext(), 4);
        this.mCallBack.removeAll();
    }

    public boolean onLongClick(View view) {
        BDReporter.c(getContext(), 32);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.setClassName("com.android.settings", "com.android.settings.ManageApplications");
        intent.setFlags(335544320);
        this.mContext.startActivity(intent);
        return false;
    }
}
