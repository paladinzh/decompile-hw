package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.NetworkController.IconState;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.SecurityController.SecurityControllerCallback;
import com.android.systemui.tint.TintImageView;
import com.android.systemui.tint.TintManager;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;
import com.android.systemui.utils.HwLog;
import java.util.ArrayList;
import java.util.List;

public abstract class SignalClusterView extends LinearLayout implements SignalCallback, SecurityControllerCallback, Tunable, HwSignalClusterInterface {
    static final boolean DEBUG = Log.isLoggable("SignalClusterView", 3);
    ImageView mAirplane;
    private String mAirplaneContentDescription;
    private int mAirplaneIconId;
    private boolean mBlockAirplane;
    private boolean mBlockEthernet;
    private boolean mBlockMobile;
    private boolean mBlockWifi;
    protected Context mContext;
    private float mDarkIntensity;
    ImageView mEthernet;
    ImageView mEthernetDark;
    private String mEthernetDescription;
    ViewGroup mEthernetGroup;
    private int mEthernetIconId;
    private boolean mEthernetVisible;
    private final float mIconScaleFactor;
    private int mIconTint;
    private boolean mIsAirplaneMode;
    private int mLastAirplaneIconId;
    private int mLastEthernetIconId;
    private int mLastWifiStrengthId;
    protected LinearLayout mMobileSignalGroup;
    NetworkControllerImpl mNC;
    private boolean mNoSimsVisible;
    private ArrayList<PhoneState> mPhoneStates;
    SecurityController mSC;
    private final Rect mTintArea;
    ImageView mVpn;
    private boolean mVpnVisible;
    ImageView mWifi;
    ImageView mWifiDark;
    private String mWifiDescription;
    ViewGroup mWifiGroup;
    private int mWifiStrengthId;
    private boolean mWifiVisible;

    private class PhoneState {
        private int mLastMobileStrengthId;
        private int mLastMobileTypeId;
        private ImageView mMobile;
        private ImageView mMobileDark;
        private ViewGroup mMobileGroup;
        private ImageView mMobileType;
        private boolean mMobileVisible;
        private final int mSubId;
        final /* synthetic */ SignalClusterView this$0;

        private void maybeStopAnimatableDrawable(ImageView view) {
            Drawable drawable = view.getDrawable();
            if (drawable instanceof ScalingDrawableWrapper) {
                drawable = ((ScalingDrawableWrapper) drawable).getDrawable();
            }
            if (drawable instanceof Animatable) {
                Animatable ad = (Animatable) drawable;
                if (ad.isRunning()) {
                    ad.stop();
                }
            }
        }

        public void populateAccessibilityEvent(AccessibilityEvent event) {
            if (this.mMobileVisible && this.mMobileGroup != null && this.mMobileGroup.getContentDescription() != null) {
                event.getText().add(this.mMobileGroup.getContentDescription());
            }
        }

        public void setIconTint(int tint, float darkIntensity, Rect tintArea) {
            this.this$0.applyDarkIntensity(StatusBarIconController.getDarkIntensity(tintArea, this.mMobile, darkIntensity), this.mMobile, this.mMobileDark);
            this.this$0.setTint(this.mMobileType, StatusBarIconController.getTint(tintArea, this.mMobileType, tint));
        }
    }

    public SignalClusterView(Context context) {
        this(context, null);
    }

    public SignalClusterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalClusterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mNoSimsVisible = false;
        this.mVpnVisible = false;
        this.mEthernetVisible = false;
        this.mEthernetIconId = 0;
        this.mLastEthernetIconId = -1;
        this.mWifiVisible = false;
        this.mWifiStrengthId = 0;
        this.mLastWifiStrengthId = -1;
        this.mIsAirplaneMode = false;
        this.mAirplaneIconId = 0;
        this.mLastAirplaneIconId = -1;
        this.mPhoneStates = new ArrayList();
        this.mIconTint = -1;
        this.mTintArea = new Rect();
        this.mContext = context;
        Resources res = getResources();
        TypedValue typedValue = new TypedValue();
        res.getValue(R.dimen.status_bar_icon_scale_factor, typedValue, true);
        this.mIconScaleFactor = typedValue.getFloat();
    }

    public void dispatchDemoCommand(String command, Bundle args) {
        apply();
    }

    public void onTuningChanged(String key, String newValue) {
        if ("icon_blacklist".equals(key)) {
            ArraySet<String> blockList = StatusBarIconController.getIconBlacklist(newValue);
            boolean blockAirplane = blockList.contains("airplane");
            boolean blockMobile = blockList.contains("mobile");
            boolean blockWifi = blockList.contains("wifi");
            boolean blockEthernet = blockList.contains("ethernet");
            HwLog.i("SignalClusterView", "blockAirplane is:" + blockAirplane);
            if (blockAirplane == this.mBlockAirplane && blockMobile == this.mBlockMobile && blockEthernet == this.mBlockEthernet) {
                if (blockWifi != this.mBlockWifi) {
                }
            }
            this.mBlockAirplane = blockAirplane;
            this.mBlockMobile = blockMobile;
            this.mBlockEthernet = blockEthernet;
            this.mBlockWifi = blockWifi;
            if (this.mNC != null) {
                this.mNC.removeSignalCallback(this);
                this.mNC.addSignalCallback(this);
            }
        }
    }

    public void setNetworkController(NetworkControllerImpl nc) {
        if (DEBUG) {
            Log.d("SignalClusterView", "NetworkController=" + nc);
        }
        this.mNC = nc;
    }

    public void setSecurityController(SecurityController sc) {
        if (DEBUG) {
            Log.d("SignalClusterView", "SecurityController=" + sc);
        }
        this.mSC = sc;
        if (this.mSC != null) {
            this.mSC.addCallback(this);
            this.mVpnVisible = this.mSC.isVpnEnabled();
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mVpn = (ImageView) findViewById(R.id.vpn);
        this.mEthernetGroup = (ViewGroup) findViewById(R.id.ethernet_combo);
        this.mEthernet = (ImageView) findViewById(R.id.ethernet);
        this.mEthernetDark = (ImageView) findViewById(R.id.ethernet_dark);
        this.mWifiGroup = (ViewGroup) findViewById(R.id.wifi_combo);
        this.mWifi = (ImageView) findViewById(R.id.wifi_signal);
        this.mWifiDark = (ImageView) findViewById(R.id.wifi_signal_dark);
        this.mAirplane = (ImageView) findViewById(R.id.airplane);
        this.mMobileSignalGroup = (LinearLayout) findViewById(R.id.mobile_combo);
        ((TintImageView) this.mEthernet).setIsResever(true);
        ((TintImageView) this.mEthernetDark).setIsResever(true);
        ((TintImageView) this.mWifi).setIsResever(true);
        ((TintImageView) this.mWifiDark).setIsResever(true);
        maybeScaleVpnAndNoSimsIcons();
    }

    private void maybeScaleVpnAndNoSimsIcons() {
        if (this.mIconScaleFactor != 1.0f) {
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService.get(this.mContext).addTunable((Tunable) this, "icon_blacklist");
        apply();
        applyIconTint();
        if (this.mNC != null) {
            this.mNC.addSignalCallback(this);
        }
    }

    protected void onDetachedFromWindow() {
        TunerService.get(this.mContext).removeTunable(this);
        if (this.mSC != null) {
            this.mSC.removeCallback(this);
        }
        if (this.mNC != null) {
            this.mNC.removeSignalCallback(this);
        }
        super.onDetachedFromWindow();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        applyIconTint();
    }

    public void onStateChanged() {
        post(new Runnable() {
            public void run() {
                if (SignalClusterView.this.mSC != null) {
                    SignalClusterView.this.mVpnVisible = SignalClusterView.this.mSC.isVpnEnabled();
                    SignalClusterView.this.apply();
                }
            }
        });
    }

    public void setWifiIndicators(boolean enabled, IconState statusIcon, IconState qsIcon, boolean activityIn, boolean activityOut, String description) {
        boolean z;
        boolean z2 = false;
        if (!statusIcon.visible || this.mBlockWifi) {
            z = false;
        } else {
            z = true;
        }
        this.mWifiVisible = z;
        this.mWifiStrengthId = statusIcon.icon;
        this.mWifiDescription = statusIcon.contentDescription;
        HwLog.i("SignalClusterView", "mWifiVisible is:" + this.mWifiVisible + "mWifiStrengthId is:" + this.mWifiStrengthId + "mWifiDescription is:" + this.mWifiDescription);
        if (this.mNC != null) {
            if (statusIcon.visible && !this.mBlockWifi) {
                z2 = true;
            }
            setWifiActivityIconId(z2, statusIcon.icon, statusIcon.contentDescription, activityIn, activityOut, this.mNC.getWiFiController().isWifiCharged(), this.mNC.getWiFiController().isWifiNoInternet());
            apply();
        }
    }

    public void setMobileDataIndicators(IconState statusIcon, IconState qsIcon, int statusType, int qsType, boolean activityIn, boolean activityOut, String typeContentDescription, String description, boolean isWide, int subId) {
        if (this.mNC != null) {
            boolean z = statusIcon.visible && !this.mBlockMobile;
            setMobileDataIndicatorsHuawei(z, statusIcon.icon, statusType, statusIcon.contentDescription, typeContentDescription, statusType != 0 ? isWide : false, subId, activityIn, activityOut, this.mNC.getControllerBySubId(subId));
            apply();
        }
    }

    public void setExtData(int sub, int inetCon, boolean isRoam, boolean isSuspend, int... extArgs) {
        updateExtData(sub, inetCon, isRoam, isSuspend, extArgs);
    }

    public void setEthernetIndicators(IconState state) {
        boolean z = false;
        if (state.visible && !this.mBlockEthernet) {
            z = true;
        }
        this.mEthernetVisible = z;
        this.mEthernetIconId = state.icon;
        this.mEthernetDescription = state.contentDescription;
        apply();
    }

    public void setNoSims(boolean show) {
        boolean z = false;
        HwLog.i("SignalClusterView", "setNoSims show:" + show);
        if (show && !this.mBlockMobile) {
            z = true;
        }
        this.mNoSimsVisible = z;
        apply();
    }

    public void setSubs(List<SubscriptionInfo> subs) {
        if (!hasCorrectSubs(subs)) {
            for (PhoneState state : this.mPhoneStates) {
                if (state.mMobile != null) {
                    state.maybeStopAnimatableDrawable(state.mMobile);
                }
                if (state.mMobileDark != null) {
                    state.maybeStopAnimatableDrawable(state.mMobileDark);
                }
            }
            updateSubs(subs);
            if (isAttachedToWindow()) {
                applyIconTint();
            }
        }
    }

    public boolean hasCorrectSubs(List<SubscriptionInfo> subs) {
        int N = subs.size();
        if (N != this.mPhoneStates.size()) {
            return false;
        }
        for (int i = 0; i < N; i++) {
            if (((PhoneState) this.mPhoneStates.get(i)).mSubId != ((SubscriptionInfo) subs.get(i)).getSubscriptionId()) {
                return false;
            }
        }
        return true;
    }

    public void setIsAirplaneMode(IconState icon) {
        boolean z;
        boolean z2 = false;
        String str = "SignalClusterView";
        StringBuilder append = new StringBuilder().append("setIsAirplaneMode is:");
        if (!icon.visible || this.mBlockAirplane) {
            z = false;
        } else {
            z = true;
        }
        HwLog.i(str, append.append(z).toString());
        if (icon.visible && !this.mBlockAirplane) {
            z2 = true;
        }
        this.mIsAirplaneMode = z2;
        this.mAirplaneIconId = icon.icon;
        this.mAirplaneContentDescription = icon.contentDescription;
        apply();
    }

    public void setMobileDataEnabled(boolean enabled) {
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        if (!(!this.mEthernetVisible || this.mEthernetGroup == null || this.mEthernetGroup.getContentDescription() == null)) {
            event.getText().add(this.mEthernetGroup.getContentDescription());
        }
        if (!(!this.mWifiVisible || this.mWifiGroup == null || this.mWifiGroup.getContentDescription() == null)) {
            event.getText().add(this.mWifiGroup.getContentDescription());
        }
        for (PhoneState state : this.mPhoneStates) {
            state.populateAccessibilityEvent(event);
        }
        return super.dispatchPopulateAccessibilityEventInternal(event);
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (this.mEthernet != null) {
            this.mEthernet.setImageDrawable(null);
            this.mEthernetDark.setImageDrawable(null);
            this.mLastEthernetIconId = -1;
        }
        if (this.mWifi != null) {
            this.mWifi.setImageDrawable(null);
            this.mWifiDark.setImageDrawable(null);
            this.mLastWifiStrengthId = -1;
        }
        setWifiViewResource(false);
        for (PhoneState state : this.mPhoneStates) {
            if (state.mMobile != null) {
                state.maybeStopAnimatableDrawable(state.mMobile);
                state.mMobile.setImageDrawable(null);
                state.mLastMobileStrengthId = -1;
            }
            if (state.mMobileDark != null) {
                state.maybeStopAnimatableDrawable(state.mMobileDark);
                state.mMobileDark.setImageDrawable(null);
                state.mLastMobileStrengthId = -1;
            }
            if (state.mMobileType != null) {
                state.mMobileType.setImageDrawable(null);
                state.mLastMobileTypeId = -1;
            }
        }
        if (this.mAirplane != null) {
            this.mAirplane.setImageDrawable(null);
            this.mLastAirplaneIconId = -1;
        }
        apply();
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    private void apply() {
        if (this.mWifiGroup != null) {
            int i;
            String str;
            String str2;
            Object[] objArr;
            ImageView imageView = this.mVpn;
            if (this.mVpnVisible) {
                i = 0;
            } else {
                i = 8;
            }
            imageView.setVisibility(i);
            if (DEBUG) {
                str = "SignalClusterView";
                str2 = "vpn: %s";
                objArr = new Object[1];
                objArr[0] = this.mVpnVisible ? "VISIBLE" : "GONE";
                Log.d(str, String.format(str2, objArr));
            }
            if (this.mEthernetVisible) {
                if (this.mLastEthernetIconId != this.mEthernetIconId) {
                    setIconForView(this.mEthernet, this.mEthernetIconId);
                    setIconForView(this.mEthernetDark, this.mEthernetIconId);
                    this.mLastEthernetIconId = this.mEthernetIconId;
                }
                this.mEthernetGroup.setContentDescription(this.mEthernetDescription);
                this.mEthernetGroup.setVisibility(0);
            } else {
                this.mEthernetGroup.setVisibility(8);
            }
            if (DEBUG) {
                str = "SignalClusterView";
                str2 = "ethernet: %s";
                objArr = new Object[1];
                objArr[0] = this.mEthernetVisible ? "VISIBLE" : "GONE";
                Log.d(str, String.format(str2, objArr));
            }
            if (this.mWifiVisible) {
                if (this.mWifiStrengthId != this.mLastWifiStrengthId) {
                    setIconForView(this.mWifi, this.mWifiStrengthId);
                    setWifiViewResource(true);
                    setIconForView(this.mWifiDark, this.mWifiStrengthId);
                    this.mLastWifiStrengthId = this.mWifiStrengthId;
                }
                this.mWifiGroup.setContentDescription(this.mWifiDescription);
                this.mWifiGroup.setVisibility(0);
            } else {
                this.mWifiGroup.setVisibility(8);
            }
            if (DEBUG) {
                str = "SignalClusterView";
                str2 = "wifi: %s sig=%d";
                objArr = new Object[2];
                objArr[0] = this.mWifiVisible ? "VISIBLE" : "GONE";
                objArr[1] = Integer.valueOf(this.mWifiStrengthId);
                Log.d(str, String.format(str2, objArr));
            }
            if (this.mIsAirplaneMode) {
                if (this.mLastAirplaneIconId != this.mAirplaneIconId) {
                    setIconForView(this.mAirplane, this.mAirplaneIconId);
                    this.mLastAirplaneIconId = this.mAirplaneIconId;
                }
                this.mAirplane.setContentDescription(this.mAirplaneContentDescription);
                this.mAirplane.setVisibility(0);
                if (this.mAirplane instanceof TintImageView) {
                    ((TintImageView) this.mAirplane).setTint();
                }
            } else {
                this.mAirplane.setVisibility(8);
            }
            applyHuawei(this.mNoSimsVisible, this.mWifiVisible, this.mIsAirplaneMode);
        }
    }

    private void setIconForView(ImageView imageView, int iconId) {
        Drawable icon = imageView.getContext().getDrawable(iconId);
        if (this.mIconScaleFactor == 1.0f) {
            imageView.setImageDrawable(icon);
        } else {
            imageView.setImageDrawable(new ScalingDrawableWrapper(icon, this.mIconScaleFactor));
        }
    }

    public void setIconTint(int tint, float darkIntensity, Rect tintArea) {
        boolean changed = (tint == this.mIconTint && darkIntensity == this.mDarkIntensity) ? !this.mTintArea.equals(tintArea) : true;
        this.mIconTint = tint;
        this.mDarkIntensity = darkIntensity;
        this.mTintArea.set(tintArea);
        if (changed && isAttachedToWindow()) {
            applyIconTint();
        }
    }

    private void applyIconTint() {
        if (TintManager.getInstance().isUseTint()) {
            HwLog.i("SignalClusterView", "applyIconTint ignore");
            return;
        }
        applyDarkIntensity(StatusBarIconController.getDarkIntensity(this.mTintArea, this.mWifi, this.mDarkIntensity), this.mWifi, this.mWifiDark);
        applyDarkIntensity(StatusBarIconController.getDarkIntensity(this.mTintArea, this.mEthernet, this.mDarkIntensity), this.mEthernet, this.mEthernetDark);
        for (int i = 0; i < this.mPhoneStates.size(); i++) {
            ((PhoneState) this.mPhoneStates.get(i)).setIconTint(this.mIconTint, this.mDarkIntensity, this.mTintArea);
        }
    }

    private void applyDarkIntensity(float darkIntensity, View lightIcon, View darkIcon) {
        lightIcon.setAlpha(1.0f - darkIntensity);
        darkIcon.setAlpha(darkIntensity);
    }

    private void setTint(ImageView v, int tint) {
        v.setImageTintList(ColorStateList.valueOf(tint));
    }
}
