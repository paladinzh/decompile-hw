package com.huawei.keyguard.hiad;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.huawei.openalliance.ad.inter.MagLockAd;
import com.huawei.openalliance.ad.inter.MagLockAdContent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HiAdBean implements Parcelable {
    public static final Creator<HiAdBean> CREATOR = new Creator<HiAdBean>() {
        public HiAdBean createFromParcel(Parcel source) {
            return new HiAdBean(source);
        }

        public HiAdBean[] newArray(int size) {
            return new HiAdBean[size];
        }
    };
    private HiAdContent[] adArray;
    private int code;
    private String slotId;

    public HiAdBean(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.slotId);
        dest.writeInt(this.code);
        dest.writeParcelableArray(this.adArray, flags);
    }

    public void readFromParcel(Parcel in) {
        this.slotId = in.readString();
        this.code = in.readInt();
        Parcelable[] parcelables = in.readParcelableArray(HiAdContent.class.getClassLoader());
        if (parcelables != null) {
            this.adArray = (HiAdContent[]) Arrays.copyOf(parcelables, parcelables.length, HiAdContent[].class);
        }
    }

    public MagLockAd convertToAd() {
        MagLockAd ad = new MagLockAd();
        ad.setSlotId(this.slotId);
        ad.setRetCode(this.code);
        if (this.adArray != null && this.adArray.length > 0) {
            List<MagLockAdContent> adContentList = new ArrayList();
            for (HiAdContent convertToAdContent : this.adArray) {
                adContentList.add(convertToAdContent.convertToAdContent());
            }
            ad.setAdList(adContentList);
        }
        return ad;
    }
}
