package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.Utils;
import com.android.systemui.Interpolators;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;
import com.android.systemui.utils.UserSwitchUtils;

public class NotificationGuts extends LinearLayout implements Tunable {
    private float mActiveSliderAlpha = 1.0f;
    private ColorStateList mActiveSliderTint;
    private int mActualHeight;
    private boolean mAuto;
    private ImageView mAutoButton;
    private Drawable mBackground;
    private RadioButton mBlock;
    private int mClipTopAmount;
    private boolean mExposed;
    private Runnable mFalsingCheck;
    private Handler mHandler;
    private INotificationManager mINotificationManager;
    private TextView mImportanceSummary;
    private TextView mImportanceTitle;
    private float mInactiveSliderAlpha;
    private ColorStateList mInactiveSliderTint;
    private OnGutsClosedListener mListener;
    private boolean mNeedsFalsingProtection;
    private int mNotificationImportance;
    private RadioButton mReset;
    private SeekBar mSeekBar;
    private boolean mShowSlider;
    private RadioButton mSilent;
    private int mStartingUserImportance;

    public interface OnGutsClosedListener {
        void onGutsClosed(NotificationGuts notificationGuts);
    }

    final /* synthetic */ class -void_bindToggles_android_view_View_importanceButtons_int_importance_boolean_systemApp_android_service_notification_StatusBarNotification_sbn_LambdaImpl0 implements OnCheckedChangeListener {
        private /* synthetic */ NotificationGuts val$this;

        public /* synthetic */ -void_bindToggles_android_view_View_importanceButtons_int_importance_boolean_systemApp_android_service_notification_StatusBarNotification_sbn_LambdaImpl0(NotificationGuts notificationGuts) {
            this.val$this = notificationGuts;
        }

        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            this.val$this.-com_android_systemui_statusbar_NotificationGuts_lambda$1(arg0, arg1);
        }
    }

    final /* synthetic */ class -void_bindToggles_android_view_View_importanceButtons_int_importance_boolean_systemApp_android_service_notification_StatusBarNotification_sbn_LambdaImpl1 implements OnCheckedChangeListener {
        private /* synthetic */ NotificationGuts val$this;

        public /* synthetic */ -void_bindToggles_android_view_View_importanceButtons_int_importance_boolean_systemApp_android_service_notification_StatusBarNotification_sbn_LambdaImpl1(NotificationGuts notificationGuts) {
            this.val$this = notificationGuts;
        }

        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            this.val$this.-com_android_systemui_statusbar_NotificationGuts_lambda$2(arg0, arg1);
        }
    }

    final /* synthetic */ class -void_bindToggles_android_view_View_importanceButtons_int_importance_boolean_systemApp_android_service_notification_StatusBarNotification_sbn_LambdaImpl2 implements OnCheckedChangeListener {
        private /* synthetic */ NotificationGuts val$this;

        public /* synthetic */ -void_bindToggles_android_view_View_importanceButtons_int_importance_boolean_systemApp_android_service_notification_StatusBarNotification_sbn_LambdaImpl2(NotificationGuts notificationGuts) {
            this.val$this = notificationGuts;
        }

        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            this.val$this.-com_android_systemui_statusbar_NotificationGuts_lambda$3(arg0, arg1);
        }
    }

    public NotificationGuts(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        this.mHandler = new Handler();
        this.mFalsingCheck = new Runnable() {
            public void run() {
                if (NotificationGuts.this.mNeedsFalsingProtection && NotificationGuts.this.mExposed) {
                    NotificationGuts.this.closeControls(-1, -1, true);
                }
            }
        };
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Theme, 0, 0);
        this.mInactiveSliderAlpha = ta.getFloat(3, 0.5f);
        ta.recycle();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService.get(this.mContext).addTunable((Tunable) this, "show_importance_slider");
    }

    protected void onDetachedFromWindow() {
        TunerService.get(this.mContext).removeTunable(this);
        super.onDetachedFromWindow();
    }

    public void resetFalsingCheck() {
        this.mHandler.removeCallbacks(this.mFalsingCheck);
        if (this.mNeedsFalsingProtection && this.mExposed) {
            this.mHandler.postDelayed(this.mFalsingCheck, 8000);
        }
    }

    protected void onDraw(Canvas canvas) {
        draw(canvas, this.mBackground);
    }

    private void draw(Canvas canvas, Drawable drawable) {
        if (drawable != null) {
            drawable.setBounds(0, this.mClipTopAmount, getWidth(), this.mActualHeight);
            drawable.draw(canvas);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mBackground = this.mContext.getDrawable(com.android.systemui.R.drawable.notification_guts_bg);
        if (this.mBackground != null) {
            this.mBackground.setCallback(this);
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mBackground;
    }

    protected void drawableStateChanged() {
        drawableStateChanged(this.mBackground);
    }

    private void drawableStateChanged(Drawable d) {
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        if (this.mBackground != null) {
            this.mBackground.setHotspot(x, y);
        }
    }

    void bindImportance(PackageManager pm, StatusBarNotification sbn, int importance) {
        this.mINotificationManager = Stub.asInterface(ServiceManager.getService("notification"));
        this.mStartingUserImportance = -1000;
        try {
            this.mStartingUserImportance = this.mINotificationManager.getImportance(sbn.getPackageName(), sbn.getUid());
        } catch (RemoteException e) {
        }
        this.mNotificationImportance = importance;
        boolean systemApp = false;
        try {
            systemApp = Utils.isSystemPackage(pm, pm.getPackageInfo(sbn.getPackageName(), 64));
        } catch (NameNotFoundException e2) {
        }
        View importanceSlider = findViewById(com.android.systemui.R.id.importance_slider);
        View importanceButtons = findViewById(com.android.systemui.R.id.importance_buttons);
        if (this.mShowSlider) {
            bindSlider(importanceSlider, systemApp);
            importanceSlider.setVisibility(0);
            importanceButtons.setVisibility(8);
            return;
        }
        bindToggles(importanceButtons, this.mStartingUserImportance, systemApp, sbn);
        importanceButtons.setVisibility(0);
        importanceSlider.setVisibility(8);
    }

    public boolean hasImportanceChanged() {
        return this.mStartingUserImportance != getSelectedImportance();
    }

    void saveImportance(StatusBarNotification sbn) {
        if (this.mBlock.isChecked()) {
            NotificationUserManager.getInstance(getContext()).setSoundVibrateState(UserSwitchUtils.getCurrentUser(), sbn.getPackageName(), -1, sbn.getUid());
        } else if (this.mSilent.isChecked()) {
            NotificationUserManager.getInstance(getContext()).setSoundVibrateState(UserSwitchUtils.getCurrentUser(), sbn.getPackageName(), 0, sbn.getUid());
        } else {
            NotificationUserManager.getInstance(getContext()).setSoundVibrateState(UserSwitchUtils.getCurrentUser(), sbn.getPackageName(), 3, sbn.getUid());
        }
    }

    private int getSelectedImportance() {
        if (this.mSeekBar == null || !this.mSeekBar.isShown()) {
            if (this.mBlock.isChecked()) {
                return 0;
            }
            if (this.mSilent.isChecked()) {
                return 2;
            }
            return -1000;
        } else if (this.mSeekBar.isEnabled()) {
            return this.mSeekBar.getProgress();
        } else {
            return -1000;
        }
    }

    private void bindToggles(View importanceButtons, int importance, boolean systemApp, StatusBarNotification sbn) {
        int i = com.android.systemui.R.color.notification_guts_radio_text_color;
        ((RadioGroup) importanceButtons).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                NotificationGuts.this.resetFalsingCheck();
            }
        });
        int state = NotificationUserManager.getInstance(getContext()).getSoundVibrateState(UserSwitchUtils.getCurrentUser(), sbn.getPackageName());
        this.mBlock = (RadioButton) importanceButtons.findViewById(com.android.systemui.R.id.block_importance);
        this.mSilent = (RadioButton) importanceButtons.findViewById(com.android.systemui.R.id.silent_importance);
        this.mReset = (RadioButton) importanceButtons.findViewById(com.android.systemui.R.id.reset_importance);
        if (systemApp) {
            this.mBlock.setVisibility(8);
            this.mReset.setText(getResetRadioText(state));
        } else {
            this.mReset.setText(getResetRadioText(state));
        }
        this.mBlock.setText(this.mContext.getString(com.android.systemui.R.string.hw_block));
        this.mSilent.setText(this.mContext.getString(com.android.systemui.R.string.hw_show_silently));
        this.mBlock.setTextColor(getResources().getColor(this.mBlock.isChecked() ? com.android.systemui.R.color.notification_guts_radio_text_color : com.android.systemui.R.color.notification_guts_radio_unchecked_text_color));
        this.mSilent.setTextColor(getResources().getColor(this.mSilent.isChecked() ? com.android.systemui.R.color.notification_guts_radio_text_color : com.android.systemui.R.color.notification_guts_radio_unchecked_text_color));
        RadioButton radioButton = this.mReset;
        Resources resources = getResources();
        if (!this.mReset.isChecked()) {
            i = com.android.systemui.R.color.notification_guts_radio_unchecked_text_color;
        }
        radioButton.setTextColor(resources.getColor(i));
        this.mBlock.setOnCheckedChangeListener(new -void_bindToggles_android_view_View_importanceButtons_int_importance_boolean_systemApp_android_service_notification_StatusBarNotification_sbn_LambdaImpl0());
        this.mSilent.setOnCheckedChangeListener(new -void_bindToggles_android_view_View_importanceButtons_int_importance_boolean_systemApp_android_service_notification_StatusBarNotification_sbn_LambdaImpl1());
        this.mReset.setOnCheckedChangeListener(new -void_bindToggles_android_view_View_importanceButtons_int_importance_boolean_systemApp_android_service_notification_StatusBarNotification_sbn_LambdaImpl2());
        if (state == 0) {
            this.mSilent.setChecked(true);
        } else {
            this.mReset.setChecked(true);
        }
    }

    /* synthetic */ void -com_android_systemui_statusbar_NotificationGuts_lambda$1(CompoundButton v, boolean checked) {
        this.mBlock.setTextColor(getResources().getColor(checked ? com.android.systemui.R.color.notification_guts_radio_text_color : com.android.systemui.R.color.notification_guts_radio_unchecked_text_color));
    }

    /* synthetic */ void -com_android_systemui_statusbar_NotificationGuts_lambda$2(CompoundButton v, boolean checked) {
        this.mSilent.setTextColor(getResources().getColor(checked ? com.android.systemui.R.color.notification_guts_radio_text_color : com.android.systemui.R.color.notification_guts_radio_unchecked_text_color));
    }

    /* synthetic */ void -com_android_systemui_statusbar_NotificationGuts_lambda$3(CompoundButton v, boolean checked) {
        this.mReset.setTextColor(getResources().getColor(checked ? com.android.systemui.R.color.notification_guts_radio_text_color : com.android.systemui.R.color.notification_guts_radio_unchecked_text_color));
    }

    private void bindSlider(View importanceSlider, boolean systemApp) {
        int minProgress;
        this.mActiveSliderTint = loadColorStateList(com.android.systemui.R.color.notification_guts_slider_color);
        this.mInactiveSliderTint = loadColorStateList(com.android.systemui.R.color.notification_guts_disabled_slider_color);
        this.mImportanceSummary = (TextView) importanceSlider.findViewById(com.android.systemui.R.id.summary);
        this.mImportanceTitle = (TextView) importanceSlider.findViewById(com.android.systemui.R.id.title);
        this.mSeekBar = (SeekBar) importanceSlider.findViewById(com.android.systemui.R.id.seekbar);
        if (systemApp) {
            minProgress = 1;
        } else {
            minProgress = 0;
        }
        this.mSeekBar.setMax(5);
        this.mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                NotificationGuts.this.resetFalsingCheck();
                if (progress < minProgress) {
                    seekBar.setProgress(minProgress);
                    progress = minProgress;
                }
                NotificationGuts.this.updateTitleAndSummary(progress);
                if (fromUser) {
                    MetricsLogger.action(NotificationGuts.this.mContext, 290);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                NotificationGuts.this.resetFalsingCheck();
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.mSeekBar.setProgress(this.mNotificationImportance);
        this.mAutoButton = (ImageView) importanceSlider.findViewById(com.android.systemui.R.id.auto_importance);
        this.mAutoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                NotificationGuts.this.mAuto = !NotificationGuts.this.mAuto;
                NotificationGuts.this.applyAuto();
            }
        });
        this.mAuto = this.mStartingUserImportance == -1000;
        applyAuto();
    }

    private void applyAuto() {
        this.mSeekBar.setEnabled(!this.mAuto);
        ColorStateList starTint = this.mAuto ? this.mActiveSliderTint : this.mInactiveSliderTint;
        float alpha = this.mAuto ? this.mInactiveSliderAlpha : this.mActiveSliderAlpha;
        Drawable icon = this.mAutoButton.getDrawable().mutate();
        icon.setTintList(starTint);
        this.mAutoButton.setImageDrawable(icon);
        this.mSeekBar.setAlpha(alpha);
        if (this.mAuto) {
            this.mSeekBar.setProgress(this.mNotificationImportance);
            this.mImportanceSummary.setText(this.mContext.getString(com.android.systemui.R.string.notification_importance_user_unspecified));
            this.mImportanceTitle.setText(this.mContext.getString(com.android.systemui.R.string.user_unspecified_importance));
            return;
        }
        updateTitleAndSummary(this.mSeekBar.getProgress());
    }

    private void updateTitleAndSummary(int progress) {
        switch (progress) {
            case 0:
                this.mImportanceSummary.setText(this.mContext.getString(com.android.systemui.R.string.notification_importance_blocked));
                this.mImportanceTitle.setText(this.mContext.getString(com.android.systemui.R.string.blocked_importance));
                return;
            case 1:
                this.mImportanceSummary.setText(this.mContext.getString(com.android.systemui.R.string.notification_importance_min));
                this.mImportanceTitle.setText(this.mContext.getString(com.android.systemui.R.string.min_importance));
                return;
            case 2:
                this.mImportanceSummary.setText(this.mContext.getString(com.android.systemui.R.string.notification_importance_low));
                this.mImportanceTitle.setText(this.mContext.getString(com.android.systemui.R.string.low_importance));
                return;
            case 3:
                this.mImportanceSummary.setText(this.mContext.getString(com.android.systemui.R.string.notification_importance_default));
                this.mImportanceTitle.setText(this.mContext.getString(com.android.systemui.R.string.default_importance));
                return;
            case 4:
                this.mImportanceSummary.setText(this.mContext.getString(com.android.systemui.R.string.notification_importance_high));
                this.mImportanceTitle.setText(this.mContext.getString(com.android.systemui.R.string.high_importance));
                return;
            case 5:
                this.mImportanceSummary.setText(this.mContext.getString(com.android.systemui.R.string.notification_importance_max));
                this.mImportanceTitle.setText(this.mContext.getString(com.android.systemui.R.string.max_importance));
                return;
            default:
                return;
        }
    }

    private ColorStateList loadColorStateList(int colorResId) {
        return ColorStateList.valueOf(this.mContext.getColor(colorResId));
    }

    public void closeControls(int x, int y, boolean notify) {
        if (getWindowToken() == null) {
            if (notify && this.mListener != null) {
                this.mListener.onGutsClosed(this);
            }
            return;
        }
        if (x == -1 || y == -1) {
            x = (getLeft() + getRight()) / 2;
            y = getTop() + (getHeight() / 2);
        }
        Animator a = ViewAnimationUtils.createCircularReveal(this, x, y, (float) Math.hypot((double) Math.max(getWidth() - x, x), (double) Math.max(getHeight() - y, y)), 0.0f);
        a.setDuration(360);
        a.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
        a.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                NotificationGuts.this.setVisibility(8);
            }
        });
        a.start();
        setExposed(false, this.mNeedsFalsingProtection);
        if (notify && this.mListener != null) {
            this.mListener.onGutsClosed(this);
        }
    }

    public void setActualHeight(int actualHeight) {
        this.mActualHeight = actualHeight;
        invalidate();
    }

    public int getActualHeight() {
        return this.mActualHeight;
    }

    public void setClipTopAmount(int clipTopAmount) {
        this.mClipTopAmount = clipTopAmount;
        invalidate();
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setClosedListener(OnGutsClosedListener listener) {
        this.mListener = listener;
    }

    public void setExposed(boolean exposed, boolean needsFalsingProtection) {
        this.mExposed = exposed;
        this.mNeedsFalsingProtection = needsFalsingProtection;
        if (this.mExposed && this.mNeedsFalsingProtection) {
            resetFalsingCheck();
        } else {
            this.mHandler.removeCallbacks(this.mFalsingCheck);
        }
        ExpandableNotificationRow row = (ExpandableNotificationRow) getParent();
        if (row != null) {
            row.setUserLocked(false);
        }
    }

    public boolean areGutsExposed() {
        return this.mExposed;
    }

    public void onTuningChanged(String key, String newValue) {
        boolean z = false;
        if ("show_importance_slider".equals(key)) {
            if (!(newValue == null || Integer.parseInt(newValue) == 0)) {
                z = true;
            }
            this.mShowSlider = z;
        }
    }

    String getResetRadioText(int state) {
        switch (state) {
            case 1:
                return getResources().getString(com.android.systemui.R.string.hw_sound);
            case 2:
                return getResources().getString(com.android.systemui.R.string.hw_vibrate);
            case 3:
                return getResources().getString(com.android.systemui.R.string.hw_sound_vibrate);
            default:
                return getResources().getString(com.android.systemui.R.string.hw_sound_vibrate);
        }
    }
}
