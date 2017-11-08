package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.Interpolators;
import com.android.systemui.R;

public class NotificationSettingsIconRow extends FrameLayout implements OnClickListener {
    private boolean mAnimating;
    private boolean mDismissing;
    private ValueAnimator mFadeAnimator;
    private ImageView mGearIcon;
    private int[] mGearLocation;
    private float mHorizSpaceForGear;
    private boolean mIconPlaced;
    private SettingsIconRowListener mListener;
    private boolean mOnLeft;
    private ExpandableNotificationRow mParent;
    private int[] mParentLocation;
    private boolean mSettingsFadedIn;
    private boolean mSnapping;
    private int mVertSpaceForGear;

    public interface SettingsIconRowListener {
        void onGearTouched(ExpandableNotificationRow expandableNotificationRow, int i, int i2);

        void onSettingsIconRowReset(ExpandableNotificationRow expandableNotificationRow);
    }

    public NotificationSettingsIconRow(Context context) {
        this(context, null);
    }

    public NotificationSettingsIconRow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationSettingsIconRow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NotificationSettingsIconRow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);
        this.mSettingsFadedIn = false;
        this.mAnimating = false;
        this.mOnLeft = true;
        this.mDismissing = false;
        this.mSnapping = false;
        this.mIconPlaced = false;
        this.mGearLocation = new int[2];
        this.mParentLocation = new int[2];
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mGearIcon = (ImageView) findViewById(R.id.gear_icon);
        this.mGearIcon.setOnClickListener(this);
        setOnClickListener(this);
        this.mHorizSpaceForGear = (float) getResources().getDimensionPixelOffset(R.dimen.notification_gear_width);
        this.mVertSpaceForGear = getResources().getDimensionPixelOffset(R.dimen.notification_min_height);
        resetState();
    }

    public void resetState() {
        setGearAlpha(0.0f);
        this.mIconPlaced = false;
        this.mSettingsFadedIn = false;
        this.mAnimating = false;
        this.mSnapping = false;
        this.mDismissing = false;
        setIconLocation(true);
        if (this.mListener != null) {
            this.mListener.onSettingsIconRowReset(this.mParent);
        }
    }

    public void setGearListener(SettingsIconRowListener listener) {
        this.mListener = listener;
    }

    public void setNotificationRowParent(ExpandableNotificationRow parent) {
        this.mParent = parent;
        setIconLocation(this.mOnLeft);
    }

    public void setAppName(String appName) {
        this.mGearIcon.setContentDescription(String.format(getResources().getString(R.string.notification_gear_accessibility), new Object[]{appName}));
    }

    public void setGearAlpha(float alpha) {
        if (alpha == 0.0f) {
            this.mSettingsFadedIn = false;
            setVisibility(4);
        } else {
            setVisibility(0);
        }
        this.mGearIcon.setAlpha(alpha);
    }

    public boolean isIconOnLeft() {
        return this.mOnLeft;
    }

    public float getSpaceForGear() {
        return this.mHorizSpaceForGear;
    }

    public boolean isVisible() {
        return this.mGearIcon.getAlpha() > 0.0f;
    }

    public void cancelFadeAnimator() {
        if (this.mFadeAnimator != null) {
            this.mFadeAnimator.cancel();
        }
    }

    public void updateSettingsIcons(float transX, float size) {
        if (!this.mAnimating && this.mSettingsFadedIn) {
            float desiredAlpha;
            float fadeThreshold = size * 0.3f;
            float absTrans = Math.abs(transX);
            if (absTrans == 0.0f) {
                desiredAlpha = 0.0f;
            } else if (absTrans <= fadeThreshold) {
                desiredAlpha = 1.0f;
            } else {
                desiredAlpha = 1.0f - ((absTrans - fadeThreshold) / (size - fadeThreshold));
            }
            setGearAlpha(desiredAlpha);
        }
    }

    public void fadeInSettings(final boolean fromLeft, final float transX, final float notiThreshold) {
        if (!this.mDismissing && !this.mAnimating) {
            boolean z;
            if (isIconLocationChange(transX)) {
                setGearAlpha(0.0f);
            }
            if (transX > 0.0f) {
                z = true;
            } else {
                z = false;
            }
            setIconLocation(z);
            this.mFadeAnimator = ValueAnimator.ofFloat(new float[]{this.mGearIcon.getAlpha(), 1.0f});
            this.mFadeAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    boolean pastGear = true;
                    float absTrans = Math.abs(transX);
                    if ((!fromLeft || transX > notiThreshold) && (fromLeft || absTrans > notiThreshold)) {
                        pastGear = false;
                    }
                    if (pastGear && !NotificationSettingsIconRow.this.mSettingsFadedIn) {
                        NotificationSettingsIconRow.this.setGearAlpha(((Float) animation.getAnimatedValue()).floatValue());
                    }
                }
            });
            this.mFadeAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    NotificationSettingsIconRow.this.mAnimating = true;
                }

                public void onAnimationCancel(Animator animation) {
                    NotificationSettingsIconRow.this.mGearIcon.setAlpha(0.0f);
                }

                public void onAnimationEnd(Animator animation) {
                    boolean z = false;
                    NotificationSettingsIconRow.this.mAnimating = false;
                    NotificationSettingsIconRow notificationSettingsIconRow = NotificationSettingsIconRow.this;
                    if (NotificationSettingsIconRow.this.mGearIcon.getAlpha() == 1.0f) {
                        z = true;
                    }
                    notificationSettingsIconRow.mSettingsFadedIn = z;
                }
            });
            this.mFadeAnimator.setInterpolator(Interpolators.ALPHA_IN);
            this.mFadeAnimator.setDuration(200);
            this.mFadeAnimator.start();
        }
    }

    public void updateVerticalLocation() {
        if (this.mParent != null) {
            int parentHeight = this.mParent.getCollapsedHeight();
            if (parentHeight < this.mVertSpaceForGear) {
                this.mGearIcon.setTranslationY((float) ((parentHeight / 2) - (this.mGearIcon.getHeight() / 2)));
            } else {
                this.mGearIcon.setTranslationY((float) ((this.mVertSpaceForGear - this.mGearIcon.getHeight()) / 2));
            }
        }
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        setIconLocation(this.mOnLeft);
    }

    public void setIconLocation(boolean onLeft) {
        float right = 0.0f;
        if ((!this.mIconPlaced || onLeft != this.mOnLeft) && !this.mSnapping && this.mParent != null && this.mGearIcon.getWidth() != 0) {
            float left;
            boolean isRtl = this.mParent.isLayoutRtl();
            if (isRtl) {
                left = -(((float) this.mParent.getWidth()) - this.mHorizSpaceForGear);
            } else {
                left = 0.0f;
            }
            if (!isRtl) {
                right = ((float) this.mParent.getWidth()) - this.mHorizSpaceForGear;
            }
            float centerX = (this.mHorizSpaceForGear - ((float) this.mGearIcon.getWidth())) / 2.0f;
            setTranslationX(onLeft ? left + centerX : right + centerX);
            this.mOnLeft = onLeft;
            this.mIconPlaced = true;
        }
    }

    public boolean isIconLocationChange(float translation) {
        boolean onLeft = translation > ((float) this.mGearIcon.getPaddingStart());
        boolean onRight = translation < ((float) (-this.mGearIcon.getPaddingStart()));
        if ((!this.mOnLeft || !onRight) && (this.mOnLeft || !onLeft)) {
            return false;
        }
        return true;
    }

    public void setSnapping(boolean snapping) {
        this.mSnapping = snapping;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.gear_icon && this.mListener != null) {
            this.mGearIcon.getLocationOnScreen(this.mGearLocation);
            this.mParent.getLocationOnScreen(this.mParentLocation);
            this.mListener.onGearTouched(this.mParent, (this.mGearLocation[0] - this.mParentLocation[0]) + ((int) (this.mHorizSpaceForGear / 2.0f)), (this.mGearLocation[1] - this.mParentLocation[1]) + (((int) ((this.mGearIcon.getTranslationY() * 2.0f) + ((float) this.mGearIcon.getHeight()))) / 2));
        }
    }
}
