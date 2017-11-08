package com.huawei.systemmanager.netassistant.traffic.trafficranking.entry;

import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.comparator.SizeComparator;
import com.huawei.systemmanager.netassistant.netapp.entry.INetApp;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppInfo;

public abstract class AbsTrafficAppInfo implements INetApp {
    public static final AlpComparator<AbsTrafficAppInfo> NETASSISTANT_TRAFFIC_RANKING_COMPARATOR = new AlpComparator<AbsTrafficAppInfo>() {
        public String getStringKey(AbsTrafficAppInfo t) {
            return t.getLabel() != null ? t.getLabel().toString() : "";
        }
    };
    public static final SizeComparator<AbsTrafficAppInfo> TRAFFIC_RANKING_MOBILE_COMPARATOR = new SizeComparator<AbsTrafficAppInfo>() {
        public long getKey(AbsTrafficAppInfo t) {
            return t.getMobileTraffic();
        }
    };
    public static final SizeComparator<AbsTrafficAppInfo> TRAFFIC_RANKING_WIFI_COMPARATOR = new SizeComparator<AbsTrafficAppInfo>() {
        public long getKey(AbsTrafficAppInfo t) {
            return t.getWifiTraffic();
        }
    };
    public static final int TYPE_DAY_PEROID = 2;
    public static final int TYPE_MONTH_PEROID = 1;
    public static final int TYPE_WEEK_PEROID = 3;
    private boolean checked;
    protected NetAppInfo mNetAppInfo;
    protected long mobileTraffic;
    protected long wifiTraffic;

    public abstract int getAppPeriod();

    public AbsTrafficAppInfo(int uid) {
        this.mNetAppInfo = NetAppInfo.buildInfo(uid);
    }

    public int getUid() {
        return this.mNetAppInfo == null ? -1 : this.mNetAppInfo.mUid;
    }

    public CharSequence getLabel() {
        return this.mNetAppInfo == null ? null : this.mNetAppInfo.mAppLabel;
    }

    public boolean isMultiApp() {
        return this.mNetAppInfo == null ? false : this.mNetAppInfo.isMultiPkg;
    }

    public Drawable getIcon() {
        return this.mNetAppInfo == null ? null : this.mNetAppInfo.getIcon();
    }

    public long getWifiTraffic() {
        return this.wifiTraffic;
    }

    public void setWifiTraffic(long wifiTraffic) {
        this.wifiTraffic = wifiTraffic;
    }

    public long getMobileTraffic() {
        return this.mobileTraffic;
    }

    public void setMobileTraffic(long mobileTraffic) {
        this.mobileTraffic = mobileTraffic;
    }

    public boolean isChecked() {
        return this.checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
