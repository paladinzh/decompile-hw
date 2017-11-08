package com.huawei.systemmanager.comm.simcard;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.google.android.collect.Lists;
import com.hsm.netmanager.M2NAdapter;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

@TargetApi(22)
public class HsmSubsciptionManager {
    private static final String TAG = "HsmSubsciptionManager";

    public static class HsmSubInfo implements Parcelable {
        public static final Creator<HsmSubInfo> CREATOR = new Creator<HsmSubInfo>() {
            public HsmSubInfo createFromParcel(Parcel in) {
                return new HsmSubInfo(in);
            }

            public HsmSubInfo[] newArray(int size) {
                return new HsmSubInfo[size];
            }
        };
        String imsi;
        int slotIndex;
        int subId;
        String subName;

        protected HsmSubInfo(Parcel in) {
            this.subId = in.readInt();
            this.imsi = in.readString();
            this.slotIndex = in.readInt();
            this.subName = in.readString();
        }

        private HsmSubInfo() {
        }

        public static HsmSubInfo create(int subId) {
            HsmSubInfo info = new HsmSubInfo();
            info.subId = subId;
            SubscriptionInfo subInfo = HsmSubsciptionManager.getManager().getActiveSubscriptionInfo(subId);
            if (subInfo != null) {
                info.slotIndex = subInfo.getSimSlotIndex();
                if (subInfo.getCarrierName() != null) {
                    info.subName = subInfo.getCarrierName().toString();
                } else {
                    info.subName = HsmSubsciptionManager.getTelephonyManager().getNetworkOperatorName(subId);
                }
            } else {
                info.slotIndex = -1;
                info.subName = GlobalContext.getString(R.string.net_assistant_card_no_service);
            }
            if (TextUtils.isEmpty(info.subName)) {
                info.subName = GlobalContext.getString(R.string.net_assistant_card_no_service);
            }
            info.imsi = HsmSubsciptionManager.getTelephonyManager().getSubscriberId(subId);
            return info;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.subId);
            dest.writeString(this.imsi);
            dest.writeInt(this.slotIndex);
            dest.writeString(this.subName);
        }

        public CharSequence getOpName() {
            return SimCardManager.getInstance().getOpName(this.imsi);
        }

        public String getImsi() {
            return this.imsi;
        }

        public boolean isActive() {
            return M2NAdapter.getDefaultDataSubscriptionId() == this.subId;
        }

        public int getSubId() {
            return this.subId;
        }
    }

    private static SubscriptionManager getManager() {
        return SubscriptionManager.from(GlobalContext.getContext());
    }

    public static boolean isMultiSubs() {
        if (getManager().getActiveSubscriptionInfoCount() > 1) {
            return true;
        }
        return false;
    }

    public static List<HsmSubInfo> createSubInfos() {
        List<SubscriptionInfo> infos = getManager().getActiveSubscriptionInfoList();
        List<HsmSubInfo> subInfos = Lists.newArrayList();
        if (infos == null) {
            return subInfos;
        }
        HwLog.i(TAG, "infos = " + infos.size() + ";infos = ");
        for (SubscriptionInfo info : infos) {
            subInfos.add(HsmSubInfo.create(info.getSubscriptionId()));
        }
        return subInfos;
    }

    public static int getDataDefaultSubId() {
        SubscriptionInfo info = getManager().getDefaultDataSubscriptionInfo();
        if (info != null) {
            return info.getSubscriptionId();
        }
        return -1;
    }

    public static int getSubIndex(int subId) {
        SubscriptionInfo info = getManager().getActiveSubscriptionInfo(subId);
        if (info != null) {
            return info.getSimSlotIndex();
        }
        return -1;
    }

    private static TelephonyManager getTelephonyManager() {
        return TelephonyManager.from(GlobalContext.getContext());
    }

    public static String getImsi(int subId) {
        return getTelephonyManager().getSubscriberId(subId);
    }

    public static boolean isSupportDualcard(Context ctx) {
        try {
            return TelephonyManager.from(ctx).isMultiSimEnabled();
        } catch (Exception e) {
            HwLog.e(TAG, "isSupportDualcard check failed!", e);
            return false;
        }
    }
}
