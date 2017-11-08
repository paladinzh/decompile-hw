package com.huawei.systemmanager.netassistant.netapp.bean;

import android.graphics.drawable.Drawable;
import android.widget.Checkable;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppInfo;

public class NoTrafficAppInfo implements Checkable {
    public static final AlpComparator<NoTrafficAppInfo> NO_TRAFFIC_APP_COMPARATOR = new AlpComparator<NoTrafficAppInfo>() {
        public String getStringKey(NoTrafficAppInfo t) {
            if (t == null || t.getAppLabel() == null) {
                return "";
            }
            return t.getAppLabel();
        }
    };
    private boolean checked;
    private final NetAppInfo mNetAppInfo;

    public NoTrafficAppInfo(NetAppInfo info) {
        this.mNetAppInfo = info;
    }

    public boolean isChecked() {
        return this.checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void toggle() {
        setChecked(!this.checked);
    }

    public String getName() {
        return this.mNetAppInfo == null ? null : this.mNetAppInfo.mAppLabel;
    }

    public int getUid() {
        return this.mNetAppInfo == null ? -1 : this.mNetAppInfo.mUid;
    }

    public Drawable getIcon() {
        return this.mNetAppInfo == null ? null : this.mNetAppInfo.getIcon();
    }

    public String getAppLabel() {
        return this.mNetAppInfo == null ? null : this.mNetAppInfo.mAppLabel;
    }

    public boolean isMultiApp() {
        return this.mNetAppInfo.isMultiPkg;
    }
}
