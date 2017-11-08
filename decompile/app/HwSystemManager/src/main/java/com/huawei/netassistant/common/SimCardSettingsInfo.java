package com.huawei.netassistant.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.huawei.netassistant.db.NetAssistantStore;
import com.huawei.netassistant.db.NetAssistantStore.SettingTable;
import com.huawei.netassistant.db.NetAssistantStore.SettingTable.Columns;
import com.huawei.netassistant.db.NetAssistantStore.TrafficAdjustTable;
import com.huawei.systemmanager.util.HwLog;

public class SimCardSettingsInfo implements Parcelable {
    public static final Creator<SimCardSettingsInfo> CREATOR = new Creator<SimCardSettingsInfo>() {
        public SimCardSettingsInfo createFromParcel(Parcel in) {
            return new SimCardSettingsInfo(in);
        }

        public SimCardSettingsInfo[] newArray(int size) {
            return new SimCardSettingsInfo[size];
        }
    };
    private static final String TAG = "SimCardSettingsInfo";
    private long mAdjustPackage;
    private int mAdjustType;
    private int mBeginDate;
    private String mBrand;
    private String mCity;
    private long mDailyWarnBytes;
    private int mExcessMontyType;
    private String mIMSI;
    private int mIsNeedNotify;
    private int mIsNeedSpeedNotify;
    private int mIsOverMarkDay;
    private int mIsOverMarkMonth;
    private int mIsUnlockScreen;
    private long mMonthLimitBytes;
    private long mMonthWarnBytes;
    private String mProvider;
    private String mProvince;
    private int mRegularAdjustType;
    private long mTotalPackage;

    public SimCardSettingsInfo(String IMSI) {
        this.mIMSI = IMSI;
    }

    public SimCardSettingsInfo(Parcel parcel) {
        this.mIMSI = parcel.readString();
        this.mTotalPackage = parcel.readLong();
        this.mAdjustPackage = parcel.readLong();
        this.mBeginDate = parcel.readInt();
        this.mRegularAdjustType = parcel.readInt();
        this.mExcessMontyType = parcel.readInt();
        this.mIsOverMarkMonth = parcel.readInt();
        this.mIsOverMarkDay = parcel.readInt();
        this.mIsUnlockScreen = parcel.readInt();
        this.mIsNeedNotify = parcel.readInt();
        this.mIsNeedSpeedNotify = parcel.readInt();
        this.mBrand = parcel.readString();
        this.mProvider = parcel.readString();
        this.mProvince = parcel.readString();
        this.mCity = parcel.readString();
        this.mMonthLimitBytes = parcel.readLong();
        this.mMonthWarnBytes = parcel.readLong();
        this.mDailyWarnBytes = parcel.readLong();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mIMSI);
        dest.writeLong(this.mTotalPackage);
        dest.writeLong(this.mAdjustPackage);
        dest.writeInt(this.mBeginDate);
        dest.writeInt(this.mRegularAdjustType);
        dest.writeInt(this.mExcessMontyType);
        dest.writeInt(this.mIsOverMarkMonth);
        dest.writeInt(this.mIsOverMarkDay);
        dest.writeInt(this.mIsUnlockScreen);
        dest.writeInt(this.mIsNeedNotify);
        dest.writeInt(this.mIsNeedSpeedNotify);
        dest.writeString(this.mBrand);
        dest.writeString(this.mProvider);
        dest.writeString(this.mProvince);
        dest.writeString(this.mCity);
        dest.writeLong(this.mMonthLimitBytes);
        dest.writeLong(this.mMonthWarnBytes);
        dest.writeLong(this.mDailyWarnBytes);
    }

    public int describeContents() {
        return 0;
    }

    public void setTotalPackage(long value) {
        this.mTotalPackage = value;
    }

    public void setBeginDate(int value) {
        this.mBeginDate = value;
    }

    public void setRegularAdjustType(int value) {
        this.mRegularAdjustType = value;
    }

    public void setExcessMontyType(int value) {
        this.mExcessMontyType = value;
    }

    public void setOverMarkMonth(int value) {
        this.mIsOverMarkMonth = value;
    }

    public void setOverMarkDay(int value) {
        this.mIsOverMarkDay = value;
    }

    public void setUnlockScreen(int value) {
        this.mIsUnlockScreen = value;
    }

    public void setNotify(int value) {
        this.mIsNeedNotify = value;
    }

    public void setSpeedNotify(int value) {
        this.mIsNeedSpeedNotify = value;
    }

    public void setAdjustPackage(long value) {
        this.mAdjustPackage = value;
    }

    public void setAdjustType(int value) {
        this.mAdjustType = value;
    }

    public String getBrand() {
        return this.mBrand;
    }

    public void setBrand(String brand) {
        this.mBrand = brand;
    }

    public String getProvider() {
        return this.mProvider;
    }

    public void setProvider(String provider) {
        this.mProvider = provider;
    }

    public String getProvince() {
        return this.mProvince;
    }

    public void setProvince(String province) {
        this.mProvince = province;
    }

    public void setCity(String city) {
        this.mCity = city;
    }

    public String getCity() {
        return this.mCity;
    }

    public long getTotalPackage() {
        return this.mTotalPackage;
    }

    public int getBeginDate() {
        return this.mBeginDate;
    }

    public int getRegularAdjustType() {
        return this.mRegularAdjustType;
    }

    public int getExcessMontyType() {
        return this.mExcessMontyType;
    }

    public int getOverMarkMonth() {
        return this.mIsOverMarkMonth;
    }

    public int getOverMarkDay() {
        return this.mIsOverMarkDay;
    }

    public int getUnlockScreen() {
        return this.mIsUnlockScreen;
    }

    public int getIsNeedNotify() {
        return this.mIsNeedNotify;
    }

    public int getIsNeedSpeedNotify() {
        return this.mIsNeedSpeedNotify;
    }

    public long getAdjustPackageValue() {
        return this.mAdjustPackage;
    }

    public int getAdjustType() {
        return this.mAdjustType;
    }

    public long getmMonthLimitBytes() {
        return this.mMonthLimitBytes;
    }

    public void setmMonthLimitBytes(long mMonthLimitBytes) {
        this.mMonthLimitBytes = mMonthLimitBytes;
    }

    public long getmMonthWarnBytes() {
        return this.mMonthWarnBytes;
    }

    public void setmMonthWarnBytes(long mMonthWarnBytes) {
        this.mMonthWarnBytes = mMonthWarnBytes;
    }

    public long getmDailyWarnBytes() {
        return this.mDailyWarnBytes;
    }

    public void setmDailyWarnBytes(long mDailyWarnBytes) {
        this.mDailyWarnBytes = mDailyWarnBytes;
    }

    public void getCardSettingInfo(Cursor cursorSettings) {
        setTotalPackage(cursorSettings.getLong(2));
        setBeginDate(cursorSettings.getInt(3));
        setExcessMontyType(cursorSettings.getInt(6));
        setRegularAdjustType(cursorSettings.getInt(4));
        setOverMarkMonth(cursorSettings.getInt(7));
        setOverMarkDay(cursorSettings.getInt(8));
        setUnlockScreen(cursorSettings.getInt(9));
        setNotify(cursorSettings.getInt(10));
        setSpeedNotify(cursorSettings.getInt(11));
    }

    public void setSettingDefaultInfo(Context mContext, String imsi) {
        Uri apUri = SettingTable.getContentUri();
        ContentValues contentValues = new ContentValues();
        contentValues.put("imsi", imsi);
        contentValues.put(Columns.PACKAGE_TOTAL, Integer.valueOf(-1));
        contentValues.put(Columns.BEGIN_DATE, Integer.valueOf(-1));
        contentValues.put(Columns.REGULAR_ADJUST_TYPE, Integer.valueOf(0));
        contentValues.put(Columns.EXCESS_MONTH_TYPE, Integer.valueOf(2));
        contentValues.put(Columns.IS_OVERMARK_MONTH, Integer.valueOf(1));
        contentValues.put(Columns.IS_OVERMARK_DAY, Integer.valueOf(1));
        contentValues.put(Columns.IS_AFTER_LOCKED, Integer.valueOf(0));
        contentValues.put(Columns.IS_NOTIFICATION, Integer.valueOf(1));
        contentValues.put(Columns.IS_SPEED_NOTIFICATION, Integer.valueOf(0));
        contentValues.put(Columns.MONTH_LIMIT, Integer.valueOf(-1));
        contentValues.put(Columns.MONTH_WARN, Integer.valueOf(-1));
        contentValues.put(Columns.DAILY_WARN, Integer.valueOf(-1));
        if (mContext.getContentResolver().insert(apUri, contentValues) != null) {
            setTotalPackage(-1);
            setBeginDate(-1);
            setExcessMontyType(2);
            setRegularAdjustType(1);
            setOverMarkMonth(1);
            setOverMarkDay(1);
            setUnlockScreen(1);
            setNotify(1);
            setSpeedNotify(0);
            setmDailyWarnBytes(-1);
            setmMonthLimitBytes(-1);
            setmMonthWarnBytes(-1);
        }
    }

    public void getAdjustInfo(Context mContext, Cursor cursorAdjust, String imsi) {
        if (cursorAdjust != null && cursorAdjust.getCount() > 0) {
            cursorAdjust.moveToNext();
            setAdjustPackage(cursorAdjust.getLong(2));
            setAdjustType(cursorAdjust.getInt(3));
            setProvince(cursorAdjust.getString(5));
            setCity(cursorAdjust.getString(6));
            setProvider(cursorAdjust.getString(7));
            setBrand(cursorAdjust.getString(8));
            cursorAdjust.close();
        } else if (cursorAdjust != null && cursorAdjust.getCount() == 0) {
            setAdjustItemInfo(mContext, imsi, cursorAdjust, -1, -1);
            setAdjustPackage(-1);
            setAdjustType(-1);
            setProvince("");
            setCity("");
            setProvider("");
            setBrand("");
            cursorAdjust.close();
        } else if (cursorAdjust != null) {
            cursorAdjust.close();
        }
    }

    public static boolean setAdjustItemInfo(Context mContext, String imsi, Cursor cursor, int adjustType, long value) {
        Uri apUri = TrafficAdjustTable.getContentUri();
        ContentValues contentValues = new ContentValues();
        contentValues.put("imsi", imsi);
        contentValues.put(TrafficAdjustTable.Columns.ADJUST_VALUE, Long.valueOf(value));
        contentValues.put(TrafficAdjustTable.Columns.ADJUST_TYPE, Integer.valueOf(adjustType));
        contentValues.put(TrafficAdjustTable.Columns.ADJUST_DATE, Long.valueOf(System.currentTimeMillis()));
        if (cursor == null) {
            return false;
        }
        String[] columns = NetAssistantStore.getTrafficAdjustColumns();
        StringBuilder where = new StringBuilder();
        where.append(columns[1]).append(" =? ");
        String userWhere = where.toString();
        String[] userWhereArgs = new String[]{imsi};
        int count = cursor.getCount();
        cursor.close();
        if (1 < count || count < 0) {
            HwLog.e(TAG, "/setAdjustItemInfo table error");
            return false;
        } else if (1 == count) {
            HwLog.e(TAG, "cursor count is 1 !");
            if (mContext.getContentResolver().update(apUri, contentValues, userWhere, userWhereArgs) > 0) {
                return true;
            }
            return false;
        } else {
            HwLog.e(TAG, "cursor count is 0 !");
            if (mContext.getContentResolver().insert(apUri, contentValues) != null) {
                return true;
            }
            return false;
        }
    }
}
