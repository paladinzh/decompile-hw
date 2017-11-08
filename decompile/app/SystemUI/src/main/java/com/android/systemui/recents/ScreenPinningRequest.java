package com.android.systemui.recents;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.R;
import java.util.ArrayList;

public class ScreenPinningRequest implements OnClickListener {
    private final AccessibilityManager mAccessibilityService = ((AccessibilityManager) this.mContext.getSystemService("accessibility"));
    private final Context mContext;
    private RequestWindowView mRequestWindow;
    private final WindowManager mWindowManager = ((WindowManager) this.mContext.getSystemService("window"));
    private int taskId;

    private class RequestWindowView extends FrameLayout {
        private final ColorDrawable mColor;
        private ValueAnimator mColorAnim;
        private ViewGroup mLayout;
        private final BroadcastReceiver mReceiver;
        private boolean mShowCancel;
        final /* synthetic */ ScreenPinningRequest this$0;

        public void onAttachedToWindow() {
            DisplayMetrics metrics = new DisplayMetrics();
            this.this$0.mWindowManager.getDefaultDisplay().getMetrics(metrics);
            float density = metrics.density;
            boolean isLandscape = isLandscapePhone(this.mContext);
            inflateView(isLandscape);
            int bgColor = this.mContext.getColor(R.color.screen_pinning_request_window_bg);
            if (ActivityManager.isHighEndGfx()) {
                this.mLayout.setAlpha(0.0f);
                if (isLandscape) {
                    this.mLayout.setTranslationX(96.0f * density);
                } else {
                    this.mLayout.setTranslationY(96.0f * density);
                }
                this.mLayout.animate().alpha(1.0f).translationX(0.0f).translationY(0.0f).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
                this.mColorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(0), Integer.valueOf(bgColor)});
                this.mColorAnim.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        RequestWindowView.this.mColor.setColor(((Integer) animation.getAnimatedValue()).intValue());
                    }
                });
                this.mColorAnim.setDuration(1000);
                this.mColorAnim.start();
            } else {
                this.mColor.setColor(bgColor);
            }
            IntentFilter filter = new IntentFilter("android.intent.action.CONFIGURATION_CHANGED");
            filter.addAction("android.intent.action.USER_SWITCHED");
            filter.addAction("android.intent.action.SCREEN_OFF");
            this.mContext.registerReceiver(this.mReceiver, filter);
        }

        private boolean isLandscapePhone(Context context) {
            Configuration config = this.mContext.getResources().getConfiguration();
            if (config.orientation != 2 || config.smallestScreenWidthDp >= 600) {
                return false;
            }
            return true;
        }

        private void inflateView(boolean isLandscape) {
            this.mLayout = (ViewGroup) View.inflate(getContext(), isLandscape ? R.layout.screen_pinning_request_land_phone : R.layout.screen_pinning_request, null);
            this.mLayout.setClickable(true);
            this.mLayout.setLayoutDirection(0);
            this.mLayout.findViewById(R.id.screen_pinning_text_area).setLayoutDirection(3);
            View buttons = this.mLayout.findViewById(R.id.screen_pinning_buttons);
            if (Recents.getSystemServices().hasSoftNavigationBar()) {
                buttons.setLayoutDirection(3);
                swapChildrenIfRtlAndVertical(buttons);
            } else {
                buttons.setVisibility(8);
            }
            ((Button) this.mLayout.findViewById(R.id.screen_pinning_ok_button)).setOnClickListener(this.this$0);
            if (this.mShowCancel) {
                ((Button) this.mLayout.findViewById(R.id.screen_pinning_cancel_button)).setOnClickListener(this.this$0);
            } else {
                ((Button) this.mLayout.findViewById(R.id.screen_pinning_cancel_button)).setVisibility(4);
            }
            ((TextView) this.mLayout.findViewById(R.id.screen_pinning_description)).setText(R.string.screen_pinning_description_new);
            int backBgVisibility = this.this$0.mAccessibilityService.isEnabled() ? 4 : 0;
            this.mLayout.findViewById(R.id.screen_pinning_back_bg).setVisibility(backBgVisibility);
            this.mLayout.findViewById(R.id.screen_pinning_back_bg_light).setVisibility(backBgVisibility);
            addView(this.mLayout, this.this$0.getRequestLayoutParams(isLandscape));
        }

        private void swapChildrenIfRtlAndVertical(View group) {
            if (this.mContext.getResources().getConfiguration().getLayoutDirection() == 1) {
                LinearLayout linearLayout = (LinearLayout) group;
                if (linearLayout.getOrientation() == 1) {
                    int i;
                    int childCount = linearLayout.getChildCount();
                    ArrayList<View> childList = new ArrayList(childCount);
                    for (i = 0; i < childCount; i++) {
                        childList.add(linearLayout.getChildAt(i));
                    }
                    linearLayout.removeAllViews();
                    for (i = childCount - 1; i >= 0; i--) {
                        linearLayout.addView((View) childList.get(i));
                    }
                }
            }
        }

        public void onDetachedFromWindow() {
            this.mContext.unregisterReceiver(this.mReceiver);
        }

        protected void onConfigurationChanged() {
            removeAllViews();
            inflateView(isLandscapePhone(this.mContext));
        }
    }

    public ScreenPinningRequest(Context context) {
        this.mContext = context;
    }

    public void clearPrompt() {
        if (this.mRequestWindow != null) {
            this.mWindowManager.removeView(this.mRequestWindow);
            this.mRequestWindow = null;
        }
    }

    public void onConfigurationChanged() {
        if (this.mRequestWindow != null) {
            this.mRequestWindow.onConfigurationChanged();
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.screen_pinning_ok_button || this.mRequestWindow == v) {
            try {
                ActivityManagerNative.getDefault().startSystemLockTaskMode(this.taskId);
            } catch (RemoteException e) {
            }
        }
        clearPrompt();
    }

    public LayoutParams getRequestLayoutParams(boolean isLandscape) {
        int i;
        if (isLandscape) {
            i = 21;
        } else {
            i = 81;
        }
        return new LayoutParams(-2, -2, i);
    }
}
