package com.huawei.systemmanager.sdk.tmsdk.netassistant;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdk.bg.module.network.ProfileInfo;

public class SimProfileDes implements Parcelable {
    public static final Creator<SimProfileDes> CREATOR = new Creator<SimProfileDes>() {
        public SimProfileDes createFromParcel(Parcel in) {
            return new SimProfileDes(in);
        }

        public SimProfileDes[] newArray(int size) {
            return new SimProfileDes[size];
        }
    };
    public static final String SIM_PROFILE_DES = "sim_profile_des";
    public static final String SIM_PROFILE_SIM_DES = "sim_profile_sim_des";
    public final String brand;
    public final String carry;
    public final String city;
    public final String imsi;
    public final String province;

    public SimProfileDes() {
        this(null, null, null, null, null);
    }

    private SimProfileDes(Parcel source) {
        this.imsi = source.readString();
        this.province = source.readString();
        this.city = source.readString();
        this.carry = source.readString();
        this.brand = source.readString();
    }

    public SimProfileDes(ProfileInfo info) {
        this(info.imsi, info.province, info.city, info.carry, info.brand);
    }

    public SimProfileDes(String imsi, int province, int city, String carry, int brand) {
        this(imsi, String.valueOf(province), String.valueOf(city), String.valueOf(carry), String.valueOf(brand));
    }

    public SimProfileDes(String imsi, String province, String city, String carry, String brand) {
        this.imsi = imsi;
        this.province = province;
        this.city = city;
        this.carry = carry;
        this.brand = brand;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.imsi);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeString(this.carry);
        dest.writeString(this.brand);
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("  imsi: ").append(this.imsi);
        strBuilder.append("  province: ").append(this.province);
        strBuilder.append("  city: ").append(this.city);
        strBuilder.append("  carry: ").append(this.carry);
        strBuilder.append("  brand: ").append(this.brand);
        return strBuilder.toString();
    }
}
