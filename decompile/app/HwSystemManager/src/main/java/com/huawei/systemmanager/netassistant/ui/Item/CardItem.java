package com.huawei.systemmanager.netassistant.ui.Item;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.netassistant.service.INetAssistantService;
import com.huawei.netassistant.service.NetAssistantManager;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager.HsmSubInfo;
import com.huawei.systemmanager.netassistant.traffic.leisuretraffic.LeisureTrafficSetting;
import com.huawei.systemmanager.netassistant.traffic.notrafficapp.NoTrafficAppDbInfo;
import com.huawei.systemmanager.netassistant.traffic.setting.ExtraTrafficSetting;
import com.huawei.systemmanager.netassistant.traffic.setting.NatSettingManager;
import com.huawei.systemmanager.netassistant.traffic.setting.RoamingTrafficSetting;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.TrafficStatisticsInfo;
import java.util.List;

public class CardItem implements Parcelable {
    public static final Creator<CardItem> CREATOR = new Creator<CardItem>() {
        public CardItem createFromParcel(Parcel in) {
            CardItem item = new CardItem();
            item.mSubInfo = (HsmSubInfo) in.readParcelable(HsmSubInfo.class.getClassLoader());
            return item;
        }

        public CardItem[] newArray(int size) {
            return new CardItem[size];
        }
    };
    private HsmSubInfo mSubInfo;

    private CardItem() {
    }

    public CardItem(HsmSubInfo subInfo) {
        this.mSubInfo = subInfo;
    }

    public String getImsi() {
        return this.mSubInfo.getImsi();
    }

    public boolean hasSetPackage() {
        return NatSettingManager.hasPackageSet(this.mSubInfo.getImsi());
    }

    public long getPackageFlowLimit() {
        return NetAssistantDBManager.getInstance().getSettingTotalPackage(getImsi());
    }

    public boolean setPackgeFlowLimit(long flowLimit) {
        return NetAssistantDBManager.getInstance().setSettingTotalPackage(getImsi(), flowLimit);
    }

    public String[] getProvinceAndCityId(Context ctx) {
        String[] result = new String[]{"", ""};
        List<String> provinceAndCity = NatSettingManager.getAdjustInfo(ctx, getImsi(), 1);
        if (provinceAndCity.size() >= 2) {
            result[0] = (String) provinceAndCity.get(0);
            result[1] = (String) provinceAndCity.get(1);
        }
        return result;
    }

    public void setProvinceAndCity(Context ctx, String provinceId, String cityId) {
        NetAssistantDBManager.getInstance().setProvinceInfo(getImsi(), provinceId, cityId);
    }

    public String[] getOperator(Context ctx) {
        String[] result = new String[]{"", ""};
        List<String> operator = NatSettingManager.getAdjustInfo(ctx, getImsi(), 3);
        if (operator.size() >= 2) {
            result[0] = (String) operator.get(0);
            result[1] = (String) operator.get(1);
        }
        return result;
    }

    public void setOperator(Context ctx, String carrierId, String brandId) {
        NetAssistantDBManager.getInstance().setOperatorInfo(getImsi(), carrierId, brandId);
    }

    public int getStartDay() {
        return NetAssistantDBManager.getInstance().getSettingBeginDate(getImsi());
    }

    public boolean setStartDay(int day) {
        return NetAssistantDBManager.getInstance().setSettingBeginDate(getImsi(), day);
    }

    public long getExtraTraffic() {
        ExtraTrafficSetting extraTrafficSetting = new ExtraTrafficSetting(getImsi());
        extraTrafficSetting.get();
        return extraTrafficSetting.getPackage();
    }

    public boolean setExtraTraffic(long byteCount) {
        ExtraTrafficSetting extraTrafficSetting = new ExtraTrafficSetting(getImsi());
        extraTrafficSetting.get();
        extraTrafficSetting.setPackage(byteCount);
        extraTrafficSetting.save(null);
        return true;
    }

    public long getRoamingTraffic() {
        RoamingTrafficSetting roamingTrafficSetting = new RoamingTrafficSetting(getImsi());
        roamingTrafficSetting.get();
        return roamingTrafficSetting.getPackage();
    }

    public boolean setRoamingTraffic(long byteCount) {
        RoamingTrafficSetting roamingTrafficSetting = new RoamingTrafficSetting(getImsi());
        roamingTrafficSetting.get();
        roamingTrafficSetting.setPackage(byteCount);
        roamingTrafficSetting.save(null);
        return true;
    }

    public String getLeisureTrafficDes(Context ctx) {
        LeisureTrafficSetting leisureTrafficSetting = new LeisureTrafficSetting(getImsi());
        leisureTrafficSetting.get();
        return !leisureTrafficSetting.ismSwitch() ? ctx.getString(R.string.is_not_open) : leisureTrafficSetting.getDesString();
    }

    public int getNoTrafficAppCount() {
        NoTrafficAppDbInfo noTrafficAppDbInfo = new NoTrafficAppDbInfo(getImsi());
        noTrafficAppDbInfo.initDbData();
        return noTrafficAppDbInfo.getNoTrafficSize();
    }

    public long getMonthLimitNotifyBytes() {
        return NatSettingManager.getLimitNotifyByte(getImsi());
    }

    public boolean setMonthNotifyLimit(long bytesCount) {
        NatSettingManager.setMonthLimitNotifyByte(getImsi(), bytesCount);
        return true;
    }

    public long getAdjustSetValue() {
        String imsi = getImsi();
        TrafficStatisticsInfo tsInfo = new TrafficStatisticsInfo(imsi, DateUtil.getYearMonth(imsi), 301);
        tsInfo.get();
        return tsInfo.getTraffic();
    }

    public void setAdjustSetValue(long bytesCount) {
        String imsi = getImsi();
        long curTime = DateUtil.getCurrentTimeMills();
        try {
            TrafficStatisticsInfo tsInfo = new TrafficStatisticsInfo(imsi, DateUtil.getYearMonth(imsi), 301);
            tsInfo.get();
            tsInfo.setTraffic(bytesCount);
            tsInfo.setRecordTime(curTime);
            tsInfo.save(null);
            INetAssistantService service = NetAssistantManager.getService();
            service.clearMonthLimitPreference(imsi);
            service.clearMonthWarnPreference(imsi);
            service.clearDailyWarnPreference(imsi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAdjustSMS() {
        try {
            NetAssistantManager.getService().sendAdjustSMS(getImsi());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getSettingRegularAdjustType() {
        return NetAssistantDBManager.getInstance().getSettingRegularAdjustType(getImsi());
    }

    public boolean setSettingRegularAdjustType(int value) {
        return NetAssistantManager.setSettingRegularAdjustType(getImsi(), value);
    }

    public int getExcessMonthType() {
        return NetAssistantDBManager.getInstance().getSettingExcessMontyType(getImsi());
    }

    public boolean setExcessMonthType(int value) {
        return NetAssistantDBManager.getInstance().setSettingExcessMontyType(getImsi(), value);
    }

    public int getExcessRoamingType() {
        return new RoamingTrafficSetting(getImsi()).get().getNotifyType();
    }

    public boolean setExcessRoamingType(int value) {
        RoamingTrafficSetting roamingInfo = new RoamingTrafficSetting(getImsi()).get();
        roamingInfo.setNotifyType(value);
        roamingInfo.save(null);
        return true;
    }

    public int getMonthWarnPrecent() {
        return (int) NetAssistantDBManager.getInstance().getMonthWarnByte(getImsi());
    }

    public boolean setMonthWarnPercent(int value) {
        String imsi = getImsi();
        NatSettingManager.setMonthWarnNotifyByte(imsi, (long) value);
        try {
            NetAssistantManager.getService().clearMonthWarnPreference(imsi);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public int getDiallyWarnPercent() {
        return (int) NetAssistantDBManager.getInstance().getDailyWarnByte(getImsi());
    }

    public boolean setDiallyWarnPercent(int value) {
        String imsi = getImsi();
        NatSettingManager.setDailyWarnNotifyByte(imsi, (long) value);
        try {
            NetAssistantManager.getService().clearDailyWarnPreference(imsi);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mSubInfo, flags);
    }

    public static List<CardItem> getCardItems() {
        List<HsmSubInfo> subInfos = HsmSubsciptionManager.createSubInfos();
        List<CardItem> result = Lists.newArrayList();
        for (HsmSubInfo subInfo : subInfos) {
            if (!(subInfo == null || TextUtils.isEmpty(subInfo.getImsi()))) {
                result.add(new CardItem(subInfo));
            }
        }
        return result;
    }
}
