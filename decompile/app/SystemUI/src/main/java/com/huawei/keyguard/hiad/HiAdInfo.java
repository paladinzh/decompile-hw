package com.huawei.keyguard.hiad;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.huawei.openalliance.ad.inter.MagLockAd;
import com.huawei.openalliance.ad.inter.MagLockAdInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HiAdInfo implements Parcelable {
    public static final Creator<HiAdInfo> CREATOR = new Creator<HiAdInfo>() {
        public HiAdInfo createFromParcel(Parcel source) {
            return new HiAdInfo(source);
        }

        public HiAdInfo[] newArray(int size) {
            return new HiAdInfo[size];
        }
    };
    private int code;
    private List<String> inValidIds = new ArrayList();
    private HiAdBean[] multiAds;

    public HiAdInfo(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.inValidIds);
        dest.writeInt(this.code);
        dest.writeParcelableArray(this.multiAds, flags);
    }

    public void readFromParcel(Parcel in) {
        in.readStringList(this.inValidIds);
        this.code = in.readInt();
        Parcelable[] parcelables = in.readParcelableArray(HiAdBean.class.getClassLoader());
        if (parcelables != null) {
            this.multiAds = (HiAdBean[]) Arrays.copyOf(parcelables, parcelables.length, HiAdBean[].class);
        }
    }

    public MagLockAdInfo convertToAdInfo() {
        MagLockAdInfo adInfo = new MagLockAdInfo();
        adInfo.setInvalidContentIds(this.inValidIds);
        adInfo.setRetCode(this.code);
        if (this.multiAds != null && this.multiAds.length > 0) {
            List<MagLockAd> list = new ArrayList();
            for (HiAdBean convertToAd : this.multiAds) {
                list.add(convertToAd.convertToAd());
            }
            adInfo.setMultiAds(list);
        }
        return adInfo;
    }
}
