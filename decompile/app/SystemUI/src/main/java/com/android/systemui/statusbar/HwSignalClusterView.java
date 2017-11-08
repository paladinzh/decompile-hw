package com.android.systemui.statusbar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManager;
import android.telephony.SubscriptionInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.systemui.DemoMode;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.MobileSignalController;
import com.android.systemui.tint.TintImageView;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.Proguard;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.huawei.cust.HwCustUtils;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwSignalClusterView extends SignalClusterView implements DemoMode, HwSignalClusterInterface {
    static boolean oldShowTwoNoSimCards = showTwoNoSimCards;
    static boolean showTwoNoSimCards = false;
    ImageView mConnectHotspotWifi;
    private HwCustMSimSignalClusterView mCustSignalCluster;
    boolean mDataPosFixed;
    private int mFakeCardId;
    ArrayList<HwPhoneState> mHwPhoneStates;
    private boolean mIsOnlyVSim;
    boolean mIsWifiCharged;
    boolean mMoblieVisible;
    private TintImageView mNoSignalImageView;
    LinearLayout mNoSimsLayout;
    boolean mShowNormal;
    public View mSpacer;
    public int mWifiActivityIconId;
    public ImageView mWifiActivityView;
    ImageView mWifiNoInterNetImageView;
    boolean mWifiNoInternet;

    private class HwPhoneState {
        private boolean isRoaming;
        private boolean mIsMobileTypeIconWide;
        private int mMobileActivityId;
        private String mMobileDescription;
        private ViewGroup mMobileGroup;
        private int mMobileStrengthId = 0;
        private String mMobileTypeDescription;
        private int mMobileTypeId = 0;
        private boolean mMobileVisible = false;
        SignalUnitNormalView mSignalUnitNormalView;
        private final int mSubId;
        private final String mTAG = HwPhoneState.class.getSimpleName();
        private boolean showActivity;

        public HwPhoneState(int subId, Context context) {
            this.mSubId = subId;
            this.mSignalUnitNormalView = getActualSignalView(this.mSubId);
            this.mMobileGroup = this.mSignalUnitNormalView;
        }

        public boolean apply(boolean isSecondaryIcon, boolean isAirplaneMode) {
            int i = 0;
            if (!isAirplaneMode) {
                if (HwSignalClusterView.showTwoNoSimCards && HwSignalClusterView.this.mFakeCardId == this.mSubId) {
                    this.mSignalUnitNormalView.setMobileSinalData(R.drawable.stat_sys_no_sim, 0, 0);
                } else {
                    int i2;
                    SignalUnitNormalView signalUnitNormalView = this.mSignalUnitNormalView;
                    int i3 = this.mMobileStrengthId;
                    int i4 = this.mMobileTypeId;
                    if (this.showActivity) {
                        i2 = this.mMobileActivityId;
                    } else {
                        i2 = 0;
                    }
                    signalUnitNormalView.setMobileSinalData(i3, i4, i2);
                    this.mMobileGroup.setContentDescription(this.mMobileTypeDescription + " " + this.mMobileDescription);
                }
            }
            if (HwSignalClusterView.this.mDataPosFixed) {
                int dataSub = System.getInt(HwSignalClusterView.this.mContext.getContentResolver(), "multi_sim_data_call", 0);
                if (this.mSubId == 0 && this.mSubId == dataSub && !isAirplaneMode) {
                    if (this.mMobileTypeId == 0 || this.mMobileActivityId == 0) {
                        this.mMobileGroup.setVisibility(8);
                    } else {
                        this.mMobileGroup.setVisibility(0);
                        SignalUnitNormalView signalUnitNormalView2 = this.mSignalUnitNormalView;
                        int i5 = this.mMobileStrengthId;
                        i3 = this.mMobileTypeId;
                        if (this.showActivity) {
                            i = this.mMobileActivityId;
                        }
                        signalUnitNormalView2.setMobileSinalData(i5, i3, i);
                    }
                } else if (isAirplaneMode) {
                    this.mMobileGroup.setVisibility(8);
                }
            }
            return this.mMobileVisible;
        }

        private SignalUnitNormalView getActualSignalView(int subId) {
            String cardLayout = SystemProperties.get("ro.sysui.signal.layout", BuildConfig.FLAVOR);
            if (HwSignalClusterView.this.mShowNormal && !HwTelephonyManager.getDefault().isCDMASimCard(subId)) {
                cardLayout = "signal_unit_normal_view";
            }
            if (cardLayout != null) {
                try {
                    int resId = HwSignalClusterView.this.mContext.getResources().getIdentifier(cardLayout, "layout", HwSignalClusterView.this.mContext.getPackageName());
                    if (resId == 0) {
                        resId = R.layout.signal_unit_normal_view;
                        HwLog.w("HwSignalClusterView", "getActualSignalView:: not found cardLayout, use normal!");
                    }
                    return (SignalUnitNormalView) View.inflate(HwSignalClusterView.this.mContext, resId, null);
                } catch (Exception e) {
                    HwLog.e("HwSignalClusterView", "inflate " + cardLayout + " error, use normal!!!");
                    return (SignalUnitNormalView) View.inflate(HwSignalClusterView.this.mContext, R.layout.signal_unit_normal_view, null);
                }
            }
            HwLog.i("HwSignalClusterView", "don't find layout in config, use normal!!!");
            return (SignalUnitNormalView) View.inflate(HwSignalClusterView.this.mContext, R.layout.signal_unit_normal_view, null);
        }
    }

    public HwSignalClusterView(Context context) {
        this(context, null);
    }

    public HwSignalClusterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwSignalClusterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mMoblieVisible = true;
        this.mIsWifiCharged = false;
        this.mWifiNoInternet = false;
        this.mDataPosFixed = SystemProperties.getBoolean("ro.config.sysui.datapos", false);
        this.mNoSimsLayout = null;
        this.mShowNormal = SystemProperties.getBoolean("ro.sysui.show.normal.layout", false);
        this.mHwPhoneStates = new ArrayList();
        this.mFakeCardId = -1;
        this.mIsOnlyVSim = false;
        if (SystemUiUtil.isMulityCard(this.mContext)) {
            this.mMoblieVisible = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).isNetworkSupported(0);
        }
        this.mNoSignalImageView = new TintImageView(this.mContext);
        this.mNoSignalImageView.setImageResource(R.drawable.stat_sys_signal_null);
        this.mCustSignalCluster = (HwCustMSimSignalClusterView) HwCustUtils.createObj(HwCustMSimSignalClusterView.class, new Object[]{this});
    }

    public void dispatchDemoCommand(String command, Bundle args) {
        Log.d("HwSignalClusterView", "enter HwSignalClusterView::dispatchDemoCommand(command:" + command + BuildConfig.FLAVOR + " bundler:" + Proguard.get(args) + ")");
        String Key_showTwoCards = "showTwoCards";
        if (args.containsKey("showTwoCards")) {
            setShowTwoCards(args.getBoolean("showTwoCards", true));
        }
        super.dispatchDemoCommand(command, args);
    }

    public static void setShowTwoCards(boolean isShowTwoCards) {
        Log.d("HwSignalClusterView", "enter HwSignalClusterView::setShowTwoCards(isShowTwoCards:" + isShowTwoCards + ")");
        showTwoNoSimCards = isShowTwoCards;
    }

    public static void setOldShowTwoCards(boolean isShowTwoCards) {
        oldShowTwoNoSimCards = isShowTwoCards;
    }

    public void applyHuawei(boolean noSimsVisible, boolean wifiVisible, boolean isAirplaneMode) {
        int i = 0;
        if (oldShowTwoNoSimCards != showTwoNoSimCards) {
            initNoSimsLayout();
            setOldShowTwoCards(showTwoNoSimCards);
        }
        if (!noSimsVisible || this.mIsOnlyVSim || isAirplaneMode) {
            this.mNoSimsLayout.setVisibility(8);
        } else {
            HwLog.i("HwSignalClusterView", " mNoSimsLayout show and mNoSignalImageView remove");
            this.mNoSimsLayout.setVisibility(0);
            removeView(this.mNoSignalImageView);
        }
        if (SystemUiUtil.isMulityCard(this.mContext)) {
            this.mSpacer.setVisibility(8);
        } else if (this.mMoblieVisible && wifiVisible && isAirplaneMode) {
            this.mSpacer.setVisibility(4);
        } else {
            this.mSpacer.setVisibility(8);
        }
        if (this.mCustSignalCluster != null) {
            this.mCustSignalCluster.dualCardNetworkBooster();
        }
        if (isAirplaneMode) {
            this.mMobileSignalGroup.setVisibility(8);
            removeView(this.mNoSignalImageView);
        } else {
            this.mMobileSignalGroup.setVisibility(0);
        }
        boolean anyMobileVisible = false;
        for (HwPhoneState hwPhoneState : this.mHwPhoneStates) {
            boolean mobileGroupVisible = hwPhoneState.apply(anyMobileVisible, isAirplaneMode);
            HwLog.i("HwSignalClusterView", "apply hwPhoneState.mSubId:" + hwPhoneState.mSubId + " anyMobileVisible:" + anyMobileVisible + " mIsAirplaneMode:" + isAirplaneMode + ", mobileGroupVisible=" + mobileGroupVisible);
            if (mobileGroupVisible && !anyMobileVisible) {
                int firstMobileTypeId = hwPhoneState.mMobileTypeId;
                anyMobileVisible = true;
            }
        }
        if (wifiVisible && this.mWifiActivityView != null) {
            this.mWifiActivityView.setImageResource(this.mWifiActivityIconId);
        }
        if (this.mWifiNoInternet) {
            this.mWifiNoInterNetImageView.setVisibility(0);
            if (this.mWifiActivityView != null) {
                this.mWifiActivityView.setVisibility(8);
            }
        } else {
            this.mWifiNoInterNetImageView.setVisibility(8);
            if (this.mWifiActivityView != null) {
                this.mWifiActivityView.setVisibility(0);
            }
        }
        if (this.mConnectHotspotWifi != null) {
            ImageView imageView = this.mConnectHotspotWifi;
            if (!this.mIsWifiCharged) {
                i = 8;
            }
            imageView.setVisibility(i);
        }
    }

    public void setWifiActivityIconId(boolean visible, int strengthIcon, String contentDescription, boolean activityIn, boolean activityOut, boolean isCharged, boolean wifiNoInternet) {
        if (activityIn && activityOut) {
            this.mWifiActivityIconId = R.drawable.stat_sys_wifi_inout;
        } else if (activityIn) {
            this.mWifiActivityIconId = R.drawable.stat_sys_wifi_in;
        } else if (activityOut) {
            this.mWifiActivityIconId = R.drawable.stat_sys_wifi_out;
        } else {
            this.mWifiActivityIconId = 0;
        }
        this.mIsWifiCharged = isCharged;
        this.mWifiNoInternet = wifiNoInternet;
        if (this.mCustSignalCluster != null) {
            this.mCustSignalCluster.setWifiActivityIconId(visible, strengthIcon, contentDescription, activityIn, activityOut, isCharged);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSpacer = findViewById(R.id.spacer);
        this.mWifiActivityView = (ImageView) findViewById(R.id.wifi_inout);
        this.mConnectHotspotWifi = (ImageView) findViewById(R.id.wifi_hotspot);
        this.mWifiNoInterNetImageView = (ImageView) findViewById(R.id.wifi_noInternet);
        this.mNoSimsLayout = (LinearLayout) findViewById(R.id.no_sims);
        for (HwPhoneState hwPhoneState : this.mHwPhoneStates) {
            this.mMobileSignalGroup.addView(hwPhoneState.mMobileGroup);
        }
        if (this.mCustSignalCluster != null) {
            this.mCustSignalCluster.dualCardNetworkBooster();
        }
        initNoSimsLayout();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void initNoSimsLayout() {
        if (this.mNoSimsLayout != null) {
            if (this.mNoSimsLayout.getChildCount() > 0) {
                HwLog.i("HwSignalClusterView", "mNoSimsLayout.removeAllViews();");
                this.mNoSimsLayout.removeAllViews();
            }
            if (showTwoNoSimCards) {
                this.mNoSimsLayout.addView(View.inflate(this.mContext, R.layout.hw_no_sims_two_imageview, null));
                HwLog.i("HwSignalClusterView", "add two no sims");
                return;
            }
            this.mNoSimsLayout.addView(View.inflate(this.mContext, R.layout.hw_no_sims_single_imageview, null));
            HwLog.i("HwSignalClusterView", "add single no sim");
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mWifiActivityView = null;
    }

    public void updateSubs(List<SubscriptionInfo> subs) {
        this.mHwPhoneStates.clear();
        removeView(this.mNoSignalImageView);
        if (this.mMobileSignalGroup != null) {
            this.mMobileSignalGroup.removeAllViews();
            HwLog.i("HwSignalClusterView", " updateSubs mMobileSignalGroup.removeAllViews()");
        } else {
            HwLog.i("HwSignalClusterView", " updateSubs mMobileSignalGroup == null");
        }
        HwLog.i("HwSignalClusterView", "updateSubs subs.size():" + subs.size());
        this.mIsOnlyVSim = false;
        this.mFakeCardId = -1;
        HwLog.i("HwSignalClusterView", "updateSubs mFakeCardId:" + this.mFakeCardId);
        if (this.mFakeCardId != -1) {
            int activeSubId = ((SubscriptionInfo) subs.get(0)).getSubscriptionId();
            HwLog.i("HwSignalClusterView", " activeSubId :" + activeSubId + " mFakeCardId:" + this.mFakeCardId);
            if (activeSubId > 1) {
                addPhoneState(activeSubId);
                activeSubId = ((SubscriptionInfo) subs.get(1)).getSubscriptionId();
            }
            if (activeSubId < this.mFakeCardId) {
                addPhoneState(activeSubId);
                addPhoneState(this.mFakeCardId);
                return;
            }
            addPhoneState(this.mFakeCardId);
            addPhoneState(activeSubId);
            return;
        }
        initPhoneStates(subs);
    }

    private boolean isTwoSimCards(List<SubscriptionInfo> subs) {
        int sum = 0;
        for (SubscriptionInfo sub : subs) {
            if (sub != null && (sub.getSubscriptionId() == 0 || sub.getSubscriptionId() == 1)) {
                sum++;
            }
        }
        return sum == 2;
    }

    private void initPhoneStates(List<SubscriptionInfo> subs) {
        for (SubscriptionInfo sub : subs) {
            addPhoneState(sub.getSubscriptionId());
        }
        if (isTwoSimCards(subs)) {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                int sub1State;
                int sub2State;

                public boolean runInThread() {
                    this.sub1State = HwTelephonyManager.getDefault().getSubState(0);
                    this.sub2State = HwTelephonyManager.getDefault().getSubState(1);
                    return true;
                }

                public void runInUI() {
                    HwSignalClusterView.this.updateSubs(this.sub1State, this.sub2State);
                }
            });
        } else if (1 == subs.size() && ((SubscriptionInfo) subs.get(0)).getSubscriptionId() > 1) {
            HwLog.i("HwSignalClusterView", "initPhoneStates::mIsOnlyVSim=" + this.mIsOnlyVSim + ", subId=" + ((SubscriptionInfo) subs.get(0)).getSubscriptionId());
            this.mIsOnlyVSim = true;
        }
    }

    public void updateSubs(int sub1State, int sub2State) {
        HwLog.i("HwSignalClusterView", "updateSubs sub1State:" + sub1State + " ,sub2State:" + sub2State);
        if (SystemUiUtil.isChinaTelecomArea()) {
            HwLog.i("HwSignalClusterView", "do not handle ACTION_SUBINFO_CONTENT_CHANGE in CL phone, return ");
        } else if (this.mHwPhoneStates == null) {
            HwLog.e("HwSignalClusterView", "updateSubs mHwPhoneStates == null");
        } else {
            Iterator<HwPhoneState> iterator = this.mHwPhoneStates.iterator();
            while (iterator.hasNext()) {
                HwPhoneState hwPhoneState = (HwPhoneState) iterator.next();
                if (hwPhoneState == null || hwPhoneState.mMobileGroup == null) {
                    HwLog.e("HwSignalClusterView", "hwPhoneState or hwPhoneState.mMobileGroup is null, and return");
                    return;
                } else if (hwPhoneState.mSubId == 0) {
                    HwLog.i("HwSignalClusterView", "updateSubs, find subId:" + hwPhoneState.mSubId + " sub1State:" + sub1State);
                    hwPhoneState.mMobileGroup.setVisibility(sub1State == 0 ? 8 : 0);
                } else if (hwPhoneState.mSubId == 1) {
                    HwLog.i("HwSignalClusterView", "updateSubs, find subId:" + hwPhoneState.mSubId + " sub2State:" + sub2State);
                    hwPhoneState.mMobileGroup.setVisibility(sub2State == 0 ? 8 : 0);
                }
            }
            removeView(this.mNoSignalImageView);
            if (sub1State == 0 && sub2State == 0) {
                addView(this.mNoSignalImageView);
            }
        }
    }

    public boolean hasCorrectSubs(List<SubscriptionInfo> subs) {
        int N = subs.size();
        if (N != this.mHwPhoneStates.size()) {
            return false;
        }
        for (int i = 0; i < N; i++) {
            if (((HwPhoneState) this.mHwPhoneStates.get(i)).mSubId != ((SubscriptionInfo) subs.get(i)).getSubscriptionId()) {
                return false;
            }
        }
        return true;
    }

    private HwPhoneState addPhoneState(int subId) {
        HwLog.i("HwSignalClusterView", "inflatePhoneState subId:" + subId);
        HwPhoneState hwPhoneState = new HwPhoneState(subId, this.mContext);
        if (this.mMobileSignalGroup != null) {
            this.mMobileSignalGroup.addView(hwPhoneState.mMobileGroup);
        }
        this.mHwPhoneStates.add(hwPhoneState);
        return hwPhoneState;
    }

    public void updateExtData(int sub, int inetCon, boolean isRoam, boolean isSuspend, int... extArgs) {
        HwPhoneState hwPhoneState = getOrInflateState(sub);
        if (hwPhoneState == null) {
            HwLog.i("HwSignalClusterView", "setExtData hwPhoneState == null and return null !!! sub:" + sub);
            return;
        }
        int[] argsValue = new int[extArgs.length];
        for (int i = 0; i < extArgs.length; i++) {
            argsValue[i] = extArgs[i];
        }
        hwPhoneState.isRoaming = isRoam;
        hwPhoneState.mSignalUnitNormalView.setExtData(sub, inetCon, hwPhoneState.isRoaming, isSuspend, argsValue);
    }

    public void setMobileDataIndicatorsHuawei(boolean visible, int strengthIcon, int typeIcon, String contentDescription, String typeContentDescription, boolean isTypeIconWide, int subId, boolean activityIn, boolean activityOut, MobileSignalController controller) {
        boolean z = false;
        boolean isRoaming = false;
        if (controller != null) {
            z = controller.isShowActivity();
            isRoaming = controller.isRoamingHuawei();
        }
        HwLog.i("HwSignalClusterView", "setMobileDataIndicatorsHuawei subId:" + subId + " visible:" + visible + " strengthIcon:" + strengthIcon + " typeIcon:" + typeIcon + " contentDescription:" + contentDescription + " typeContentDescription:" + typeContentDescription + " isTypeIconWide:" + isTypeIconWide + " showActivity:" + z + " activityIn:" + activityIn + " activityOut:" + activityOut + " isRoaming:" + isRoaming);
        HwPhoneState hwPhoneState = getOrInflateState(subId);
        if (hwPhoneState == null) {
            HwLog.i("HwSignalClusterView", " hwPhoneState == null is null and return !! subId:" + subId);
            return;
        }
        hwPhoneState.mMobileVisible = visible;
        hwPhoneState.mMobileStrengthId = strengthIcon;
        hwPhoneState.mMobileTypeId = typeIcon;
        hwPhoneState.mMobileDescription = contentDescription;
        hwPhoneState.mMobileTypeDescription = typeContentDescription;
        hwPhoneState.mIsMobileTypeIconWide = isTypeIconWide;
        hwPhoneState.mMobileActivityId = hwPhoneState.mSignalUnitNormalView.getMobileActivityIconId(activityIn, activityOut);
        hwPhoneState.isRoaming = isRoaming;
        hwPhoneState.showActivity = z;
        if (this.mCustSignalCluster != null) {
            this.mCustSignalCluster.setMobileDataIndicators(visible, strengthIcon, typeIcon, contentDescription, typeContentDescription, isTypeIconWide, subId, z, activityIn, activityOut, isRoaming);
        }
    }

    private HwPhoneState getOrInflateState(int subId) {
        HwLog.i("HwSignalClusterView", "getOrInflateState subId:" + subId);
        for (HwPhoneState hwPhoneState : this.mHwPhoneStates) {
            if (hwPhoneState.mSubId == subId) {
                return hwPhoneState;
            }
        }
        HwLog.i("HwSignalClusterView", "getOrInflateState subId:" + subId + " NOT in mHwPhoneStates and return null!");
        return null;
    }

    public void setWifiViewResource(boolean showOrNot) {
        if (this.mWifiActivityView == null) {
            return;
        }
        if (showOrNot) {
            this.mWifiActivityView.setImageResource(this.mWifiActivityIconId);
        } else {
            this.mWifiActivityView.setImageDrawable(null);
        }
    }
}
