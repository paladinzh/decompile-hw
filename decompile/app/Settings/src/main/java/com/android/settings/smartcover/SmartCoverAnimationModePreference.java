package com.android.settings.smartcover;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;

public class SmartCoverAnimationModePreference extends Preference implements OnClickListener {
    private Context mContext;
    private ContentObserver mContextObserver;
    private ImageView mHeartImageView;
    private onListenerTextModeChecked mListenerTexModeStatus;
    private ImageView mPlaneImageView;
    private RadioButton mRadionBtHeart;
    private RadioButton mRadionBtPlane;
    private RadioButton mRadionBtSmile;
    private RadioButton mRadionBtText;
    private ImageView mSmileImageView;
    private ImageView mTextImageView;

    public interface onListenerTextModeChecked {
        void onTextModeChanged(boolean z);
    }

    public SmartCoverAnimationModePreference(Context context) {
        this(context, null);
        this.mContext = context;
        setLayoutResource(2130968622);
        setSelectable(false);
    }

    public SmartCoverAnimationModePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContextObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                SmartCoverAnimationModePreference.this.updateRadionChecked();
            }
        };
        this.mContext = context;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        initView(view);
        updateRadioBtStatus();
        updateRadionChecked();
    }

    private void initView(PreferenceViewHolder view) {
        this.mSmileImageView = (ImageView) view.findViewById(2131886229);
        this.mPlaneImageView = (ImageView) view.findViewById(2131886232);
        this.mHeartImageView = (ImageView) view.findViewById(2131886233);
        this.mTextImageView = (ImageView) view.findViewById(2131886234);
        this.mRadionBtSmile = (RadioButton) view.findViewById(2131886230);
        this.mRadionBtPlane = (RadioButton) view.findViewById(2131886231);
        this.mRadionBtHeart = (RadioButton) view.findViewById(2131886236);
        this.mRadionBtText = (RadioButton) view.findViewById(2131886235);
        if (this.mSmileImageView != null) {
            this.mSmileImageView.setOnClickListener(this);
        }
        if (this.mPlaneImageView != null) {
            this.mPlaneImageView.setOnClickListener(this);
        }
        if (this.mHeartImageView != null) {
            this.mHeartImageView.setOnClickListener(this);
        }
        if (this.mTextImageView != null) {
            this.mTextImageView.setOnClickListener(this);
        }
        if (this.mRadionBtSmile != null) {
            this.mRadionBtSmile.setOnClickListener(this);
        }
        if (this.mRadionBtPlane != null) {
            this.mRadionBtPlane.setOnClickListener(this);
        }
        if (this.mRadionBtHeart != null) {
            this.mRadionBtHeart.setOnClickListener(this);
        }
        if (this.mRadionBtText != null) {
            this.mRadionBtText.setOnClickListener(this);
        }
    }

    public void onResume() {
        if (this.mContextObserver != null && this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("cover_animation_display_mode"), true, this.mContextObserver);
        }
    }

    public void onPause() {
        if (this.mContextObserver != null && this.mContext != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mContextObserver);
        }
    }

    private void updateRadioBtStatus() {
        if (this.mContext != null) {
            onAnimationModeChanged(Global.getInt(this.mContext.getContentResolver(), "cover_animation_display_mode", -1));
        }
    }

    private void onAnimationModeChanged(int animationMode) {
        switch (animationMode) {
            case -1:
                setRadioBtStatus(false);
                return;
            case 9:
                setRadioBtStatus(true);
                return;
            default:
                return;
        }
    }

    public void setOnTextModeChangeListener(onListenerTextModeChecked listenerTextModeStatus) {
        this.mListenerTexModeStatus = listenerTextModeStatus;
    }

    private void updateRadionChecked() {
        changeRadionBtStatus(Global.getInt(this.mContext.getContentResolver(), "animation_mode_checked", 1));
    }

    private void setRadioBtStatus(boolean isEnabledAndClickable) {
        if (this.mSmileImageView != null) {
            this.mSmileImageView.setClickable(isEnabledAndClickable);
        }
        if (this.mPlaneImageView != null) {
            this.mPlaneImageView.setClickable(isEnabledAndClickable);
        }
        if (this.mHeartImageView != null) {
            this.mHeartImageView.setClickable(isEnabledAndClickable);
        }
        if (this.mTextImageView != null) {
            this.mTextImageView.setClickable(isEnabledAndClickable);
        }
        if (this.mRadionBtSmile != null) {
            this.mRadionBtSmile.setEnabled(isEnabledAndClickable);
            this.mRadionBtSmile.setClickable(isEnabledAndClickable);
        }
        if (this.mRadionBtPlane != null) {
            this.mRadionBtPlane.setEnabled(isEnabledAndClickable);
            this.mRadionBtPlane.setClickable(isEnabledAndClickable);
        }
        if (this.mRadionBtHeart != null) {
            this.mRadionBtHeart.setEnabled(isEnabledAndClickable);
            this.mRadionBtHeart.setClickable(isEnabledAndClickable);
        }
        if (this.mRadionBtText != null) {
            this.mRadionBtText.setEnabled(isEnabledAndClickable);
            this.mRadionBtText.setClickable(isEnabledAndClickable);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 2131886229:
            case 2131886230:
                changeRadionBtStatus(1);
                saveRadioBtChecked(1);
                this.mListenerTexModeStatus.onTextModeChanged(false);
                return;
            case 2131886231:
            case 2131886232:
                changeRadionBtStatus(2);
                saveRadioBtChecked(2);
                this.mListenerTexModeStatus.onTextModeChanged(false);
                return;
            case 2131886233:
            case 2131886236:
                changeRadionBtStatus(3);
                saveRadioBtChecked(3);
                this.mListenerTexModeStatus.onTextModeChanged(false);
                return;
            case 2131886234:
            case 2131886235:
                changeRadionBtStatus(4);
                saveRadioBtChecked(4);
                this.mListenerTexModeStatus.onTextModeChanged(true);
                return;
            default:
                return;
        }
    }

    private void saveRadioBtChecked(int checkedIndex) {
        if (this.mContext != null) {
            switch (checkedIndex) {
                case 1:
                    Global.putInt(this.mContext.getContentResolver(), "animation_mode_checked", 1);
                    break;
                case 2:
                    Global.putInt(this.mContext.getContentResolver(), "animation_mode_checked", 2);
                    break;
                case 3:
                    Global.putInt(this.mContext.getContentResolver(), "animation_mode_checked", 3);
                    break;
                case 4:
                    Global.putInt(this.mContext.getContentResolver(), "animation_mode_checked", 4);
                    break;
            }
        }
    }

    private void changeRadionBtStatus(int modeIndex) {
        if (!getRadioBtExist()) {
            switch (modeIndex) {
                case 1:
                    this.mRadionBtSmile.setChecked(true);
                    this.mRadionBtPlane.setChecked(false);
                    this.mRadionBtHeart.setChecked(false);
                    this.mRadionBtText.setChecked(false);
                    break;
                case 2:
                    this.mRadionBtSmile.setChecked(false);
                    this.mRadionBtPlane.setChecked(true);
                    this.mRadionBtHeart.setChecked(false);
                    this.mRadionBtText.setChecked(false);
                    break;
                case 3:
                    this.mRadionBtSmile.setChecked(false);
                    this.mRadionBtPlane.setChecked(false);
                    this.mRadionBtHeart.setChecked(true);
                    this.mRadionBtText.setChecked(false);
                    break;
                case 4:
                    this.mRadionBtSmile.setChecked(false);
                    this.mRadionBtPlane.setChecked(false);
                    this.mRadionBtHeart.setChecked(false);
                    this.mRadionBtText.setChecked(true);
                    break;
            }
        }
    }

    private boolean getRadioBtExist() {
        if (this.mRadionBtSmile == null || this.mRadionBtPlane == null || this.mRadionBtHeart == null || this.mRadionBtText == null) {
            return true;
        }
        return false;
    }
}
