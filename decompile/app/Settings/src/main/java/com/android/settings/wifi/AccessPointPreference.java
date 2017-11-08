package com.android.settings.wifi;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.HwCustMSimSubscriptionStatusTabFragmentImpl;
import com.android.settingslib.wifi.AccessPoint;
import com.huawei.android.net.wifi.WifiManagerEx;

public class AccessPointPreference extends Preference implements OnClickListener {
    private static final int[] STATE_CLOUD_SECURITY_CHECK_DANGEROUS = new int[]{2130772329};
    private static final int[] STATE_NONE = new int[0];
    private static final int[] STATE_NO_INTERNET = new int[]{2130772327};
    private static final int[] STATE_PAUSE = new int[]{2130772328};
    private static final int[] STATE_SECURED = new int[]{2130771972};
    static final int[] WIFI_CONNECTION_STRENGTH = new int[]{2131624010, 2131624011, 2131624012, 2131624013};
    private static int[] wifi_signal_attributes = new int[]{2130771973};
    private AccessPoint mAccessPoint;
    private Drawable mBadge;
    private final UserBadgeCache mBadgeCache;
    private final int mBadgePadding;
    private CharSequence mContentDescription;
    private Context mContext;
    private boolean mForSavedNetworks = false;
    private boolean mIsRecommendingAccessPoints;
    private int mLevel;
    private boolean mLinkPlusEnabled;
    private final Runnable mNotifyChanged = new Runnable() {
        public void run() {
            AccessPointPreference.this.notifyChanged();
        }
    };
    private TextView mTitleView;
    private final StateListDrawable mWifiSld;

    public static class UserBadgeCache {
        private final SparseArray<Drawable> mBadges = new SparseArray();
        private final PackageManager mPm;

        public UserBadgeCache(PackageManager pm) {
            this.mPm = pm;
        }

        private Drawable getUserBadge(int userId) {
            int index = this.mBadges.indexOfKey(userId);
            if (index >= 0) {
                return (Drawable) this.mBadges.valueAt(index);
            }
            Drawable badge = this.mPm.getUserBadgeForDensity(new UserHandle(userId), 0);
            this.mBadges.put(userId, badge);
            return badge;
        }
    }

    public AccessPointPreference(AccessPoint accessPoint, Context context, UserBadgeCache cache, boolean forSavedNetworks) {
        super(context);
        resetLayout(context);
        setWidgetLayoutResource(2130969008);
        this.mBadgeCache = cache;
        this.mAccessPoint = accessPoint;
        this.mForSavedNetworks = forSavedNetworks;
        this.mAccessPoint.setTag(this);
        this.mLevel = -1;
        this.mContext = context;
        this.mWifiSld = (StateListDrawable) context.getTheme().obtainStyledAttributes(wifi_signal_attributes).getDrawable(0);
        this.mBadgePadding = context.getResources().getDimensionPixelSize(2131558410);
        initLinkPlusEnabled();
        refresh();
    }

    public AccessPoint getAccessPoint() {
        return this.mAccessPoint;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (this.mAccessPoint != null) {
            initLinkPlusEnabled();
            updateWidgetView(view.itemView);
            this.mTitleView = (TextView) view.findViewById(16908310);
            if (this.mTitleView != null) {
                this.mTitleView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, this.mBadge, null);
                this.mTitleView.setCompoundDrawablePadding(this.mBadgePadding);
            }
            view.itemView.setContentDescription(this.mContentDescription);
            changeTitleColor(view.itemView);
        }
    }

    protected void updateBadge(Context context) {
        WifiConfiguration config = this.mAccessPoint.getConfig();
        if (config != null) {
            this.mBadge = this.mBadgeCache.getUserBadge(config.creatorUid);
        }
    }

    public void refresh() {
        if (this.mAccessPoint != null) {
            CharSequence savedNetworkSummary;
            if (this.mForSavedNetworks) {
                setTitle(this.mAccessPoint.getConfigName());
            } else {
                setTitle(this.mAccessPoint.getSsid());
            }
            Context context = getContext();
            int level = this.mAccessPoint.getLevel();
            if (level != this.mLevel) {
                this.mLevel = level;
                notifyChanged();
            }
            updateBadge(context);
            if (this.mForSavedNetworks) {
                savedNetworkSummary = this.mAccessPoint.getSavedNetworkSummary();
            } else {
                savedNetworkSummary = this.mAccessPoint.getSettingsSummary(this.mLinkPlusEnabled);
            }
            setSummary(savedNetworkSummary);
            this.mContentDescription = getTitle();
            if (getSummary() != null) {
                this.mContentDescription = TextUtils.concat(new CharSequence[]{this.mContentDescription, ",", getSummary()});
            }
            if (level >= 0 && level < WIFI_CONNECTION_STRENGTH.length) {
                this.mContentDescription = TextUtils.concat(new CharSequence[]{this.mContentDescription, ",", getContext().getString(WIFI_CONNECTION_STRENGTH[level])});
            }
        }
    }

    protected void notifyChanged() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            postNotifyChanged();
        } else {
            super.notifyChanged();
        }
    }

    public void onLevelChanged() {
        postNotifyChanged();
    }

    private void postNotifyChanged() {
        if (this.mTitleView != null) {
            this.mTitleView.post(this.mNotifyChanged);
        }
    }

    protected void resetLayout(Context context) {
        if (Global.getInt(context.getContentResolver(), "device_provisioned", 1) == 0) {
            setLayoutResource(2130969009);
        } else {
            setLayoutResource(2130968906);
        }
    }

    protected void changeTitleColor(View root) {
        TextView titleView = (TextView) root.findViewById(16908310);
        if (titleView == null) {
            return;
        }
        if (this.mAccessPoint.getDetailedState() == DetailedState.CONNECTED) {
            titleView.setTextColor(Utils.getControlColor(getContext(), getContext().getResources().getColor(2131427515)));
        } else {
            titleView.setTextColor(getContext().getResources().getColor(2131427331));
        }
    }

    private void updateWidgetView(View view) {
        int i = 0;
        ImageView signal = (ImageView) view.findViewById(2131886971);
        ImageView switchWifi = (ImageView) view.findViewById(2131886862);
        ImageView hiLinkIcon = (ImageView) view.findViewById(2131886863);
        if (signal == null || switchWifi == null || hiLinkIcon == null) {
            Log.w("AccessPointPreference", "Image view not found!");
            return;
        }
        switchWifi.setVisibility(8);
        if (this.mAccessPoint.getRssi() == HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID) {
            signal.setImageDrawable(null);
        } else {
            boolean z;
            boolean isWifiCloudSecurityCheckOn;
            DetailedState detailedState = this.mAccessPoint.getDetailedState();
            boolean noInternet = this.mAccessPoint.isNoInternetAccess();
            Log.d("AccessPointPreference", "onBindView linkPlus = " + this.mLinkPlusEnabled + ", detailedState = " + detailedState + ", noInternet = " + noInternet + ", noHandover = " + this.mAccessPoint.isNoHandoverNetwork() + ", reason = " + this.mAccessPoint.getNoInternetReason() + ", ssid:" + this.mAccessPoint.getSsid());
            updateSignalLevel(signal);
            signal.setImageResource(2130838744);
            if (Secure.getInt(this.mContext.getContentResolver(), "wifipro_recommending_access_points", 0) == 1) {
                z = true;
            } else {
                z = false;
            }
            this.mIsRecommendingAccessPoints = z;
            if (Global.getInt(this.mContext.getContentResolver(), "wifi_cloud_security_check", 0) == 1) {
                isWifiCloudSecurityCheckOn = true;
            } else {
                isWifiCloudSecurityCheckOn = false;
            }
            if (isWifiCloudSecurityCheckOn && this.mAccessPoint.isCloudSecurityCheckDangerous()) {
                signal.setImageState(STATE_CLOUD_SECURITY_CHECK_DANGEROUS, true);
            } else if (this.mLinkPlusEnabled && !this.mIsRecommendingAccessPoints && detailedState == DetailedState.VERIFYING_POOR_LINK) {
                switchWifi.setVisibility(0);
                switchWifi.setOnClickListener(this);
                switchWifi.setEnabled(true);
                signal.setImageState(STATE_PAUSE, true);
            } else if (this.mAccessPoint.isNoInternetAccess()) {
                signal.setImageState(STATE_NO_INTERNET, true);
            } else {
                signal.setImageState(this.mAccessPoint.getSecurity() != 0 ? STATE_SECURED : STATE_NONE, true);
            }
            if (!this.mAccessPoint.isHiLinkNetwork()) {
                i = 8;
            }
            hiLinkIcon.setVisibility(i);
        }
    }

    private void updateSignalLevel(ImageView signal) {
        int level = this.mAccessPoint.getLevel();
        if (level == -1) {
            signal.setVisibility(8);
            return;
        }
        signal.setImageLevel(level);
        signal.setVisibility(0);
    }

    public void onClick(View view) {
        Log.d("AccessPointPreference", "AP onClick enter!");
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager != null) {
            view.setEnabled(false);
            WifiManagerEx.userHandoverWifi(wifiManager);
        }
    }

    public void initLinkPlusEnabled() {
        boolean z = true;
        if (System.getInt(this.mContext.getContentResolver(), "smart_network_switching", 0) != 1) {
            z = false;
        }
        this.mLinkPlusEnabled = z;
    }
}
