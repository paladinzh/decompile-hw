package com.android.systemui.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.R$styleable;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.utils.HwLog;
import com.huawei.cust.HwCustUtils;

public class ToggleSlider extends RelativeLayout {
    public ImageView mBrightnessIndicatorImageView;
    private final OnCheckedChangeListener mCheckListener;
    private View mContainer;
    private HwCustToggleSlider mHwCustToggleSlider;
    private TextView mLabel;
    private Listener mListener;
    private ToggleSlider mMirror;
    private BrightnessMirrorController mMirrorController;
    private final OnSeekBarChangeListener mSeekListener;
    private ToggleSeekBar mSlider;
    private CompoundButton mToggle;
    private boolean mTracking;

    public interface Listener {
        void onChanged(ToggleSlider toggleSlider, boolean z, boolean z2, int i, boolean z3);

        void onInit(ToggleSlider toggleSlider);
    }

    public ToggleSlider(Context context) {
        this(context, null);
    }

    public ToggleSlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToggleSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCheckListener = new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton toggle, boolean checked) {
                HwLog.i("ToggleSlider", "mCheckListener checked:" + checked);
                if (ToggleSlider.this.mListener != null) {
                    ToggleSlider.this.mListener.onChanged(ToggleSlider.this, ToggleSlider.this.mTracking, checked, ToggleSlider.this.mSlider.getProgress(), false);
                }
                if (ToggleSlider.this.mMirror != null) {
                    ToggleSlider.this.mMirror.mToggle.setChecked(checked);
                }
            }
        };
        this.mSeekListener = new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                HwLog.i("ToggleSlider", " mSeekListener onProgressChanged progress:" + progress + " fromUser:" + fromUser);
                if (fromUser) {
                    if (ToggleSlider.this.mListener != null) {
                        ToggleSlider.this.mListener.onChanged(ToggleSlider.this, ToggleSlider.this.mTracking, ToggleSlider.this.mToggle.isChecked(), progress, false);
                        int level = HwBrightnessUtils.getCurrentLevelOfIndicatorView(progress);
                        if (ToggleSlider.this.mBrightnessIndicatorImageView == null || ToggleSlider.this.mMirror == null || ToggleSlider.this.mMirror.mBrightnessIndicatorImageView == null) {
                            HwLog.e("ToggleSlider", "mBrightnessIndicatorImageView or mMirror or mMirror.mBrightnessIndicatorImageView is null");
                            return;
                        } else {
                            ToggleSlider.this.mBrightnessIndicatorImageView.setImageLevel(level);
                            ToggleSlider.this.mMirror.mBrightnessIndicatorImageView.setImageLevel(level);
                        }
                    }
                    if (ToggleSlider.this.mMirror != null) {
                        ToggleSlider.this.mMirror.setValue(ToggleSlider.this.mSlider.getProgress());
                    }
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                ToggleSlider.this.mTracking = true;
                HwLog.i("ToggleSlider", " mSeekListener onStartTrackingTouch");
                if (ToggleSlider.this.mListener != null) {
                    ToggleSlider.this.mListener.onChanged(ToggleSlider.this, ToggleSlider.this.mTracking, ToggleSlider.this.mToggle.isChecked(), ToggleSlider.this.mSlider.getProgress(), false);
                }
                if (ToggleSlider.this.mMirrorController != null) {
                    ToggleSlider.this.mMirrorController.showMirror();
                    ToggleSlider.this.mMirrorController.setLocation((View) ToggleSlider.this.getParent());
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                ToggleSlider.this.mTracking = false;
                HwLog.i("ToggleSlider", " mSeekListener onStopTrackingTouch");
                if (ToggleSlider.this.mListener != null) {
                    ToggleSlider.this.mListener.onChanged(ToggleSlider.this, ToggleSlider.this.mTracking, ToggleSlider.this.mToggle.isChecked(), ToggleSlider.this.mSlider.getProgress(), true);
                }
                if (ToggleSlider.this.mMirrorController != null) {
                    ToggleSlider.this.mMirrorController.hideMirror();
                }
            }
        };
        View.inflate(new ContextThemeWrapper(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null)), R.layout.hw_status_bar_toggle_slider, this);
        Resources res = context.getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.ToggleSlider, defStyle, 0);
        this.mToggle = (CompoundButton) findViewById(R.id.toggle);
        this.mToggle.setOnCheckedChangeListener(this.mCheckListener);
        this.mSlider = (ToggleSeekBar) findViewById(R.id.slider);
        this.mSlider.setOnSeekBarChangeListener(this.mSeekListener);
        this.mSlider.setProgressDrawable(context.getDrawable(R.drawable.systemui_scrubber_progress_horizontal_emui));
        this.mLabel = (TextView) findViewById(R.id.label);
        this.mBrightnessIndicatorImageView = (ImageView) findViewById(R.id.brightness_indicator);
        this.mSlider.setAccessibilityLabel(getContentDescription().toString());
        a.recycle();
        this.mContainer = findViewById(R.id.brightness);
        this.mHwCustToggleSlider = (HwCustToggleSlider) HwCustUtils.createObj(HwCustToggleSlider.class, new Object[0]);
        updateAutoBrightnessSwitch(getRootView(), context);
    }

    public void updateAutoBrightnessSwitch(View view, Context context) {
        if (this.mHwCustToggleSlider != null) {
            this.mHwCustToggleSlider.isNotShowBrightnessSwitch(view, context);
        }
    }

    public void setMirror(ToggleSlider toggleSlider) {
        this.mMirror = toggleSlider;
        if (this.mMirror != null) {
            this.mMirror.setChecked(this.mToggle.isChecked());
            this.mMirror.setMax(this.mSlider.getMax());
            this.mMirror.setValue(this.mSlider.getProgress());
        }
    }

    public void setMirrorController(BrightnessMirrorController c) {
        this.mMirrorController = c;
    }

    protected void onAttachedToWindow() {
        HwLog.i("ToggleSlider", "onAttachedToWindow");
        super.onAttachedToWindow();
        if (this.mListener != null) {
            this.mListener.onInit(this);
        } else {
            HwLog.i("ToggleSlider", "onAttachedToWindow mListener == null");
        }
    }

    public void setOnChangedListener(Listener l) {
        this.mListener = l;
    }

    public void setChecked(boolean checked) {
        this.mToggle.setChecked(checked);
    }

    public void setMax(int max) {
        this.mSlider.setMax(max);
        if (this.mMirror != null) {
            this.mMirror.setMax(max);
        }
    }

    public void setValue(int value) {
        this.mSlider.setProgress(value);
        if (this.mMirror != null) {
            this.mMirror.setValue(value);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateResources();
    }

    public void updateResources() {
        LayoutParams lp1 = (LayoutParams) this.mBrightnessIndicatorImageView.getLayoutParams();
        lp1.setMarginStart(getResources().getDimensionPixelSize(R.dimen.brightness_indicator_margin_start));
        this.mBrightnessIndicatorImageView.setLayoutParams(lp1);
        LayoutParams lp2 = (LayoutParams) this.mLabel.getLayoutParams();
        lp2.setMarginStart(getResources().getDimensionPixelSize(R.dimen.brightness_label_margin_start));
        this.mLabel.setLayoutParams(lp2);
        this.mLabel.setText(R.string.str_auto);
        LayoutParams lp3 = (LayoutParams) this.mToggle.getLayoutParams();
        lp3.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.brightness_toogle_margin_end));
        this.mToggle.setLayoutParams(lp3);
        RelativeLayout.LayoutParams lp4 = (RelativeLayout.LayoutParams) this.mContainer.getLayoutParams();
        lp4.height = getResources().getDimensionPixelSize(R.dimen.brightness_height);
        this.mContainer.setLayoutParams(lp4);
    }
}
