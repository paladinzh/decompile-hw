package tmsdk.common.module.update;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class CheckResult implements Parcelable {
    public static final Creator<CheckResult> CREATOR = new Creator<CheckResult>() {
        public CheckResult[] cr(int i) {
            return new CheckResult[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return p(parcel);
        }

        public /* synthetic */ Object[] newArray(int i) {
            return cr(i);
        }

        public CheckResult p(Parcel parcel) {
            return new CheckResult(parcel);
        }
    };
    public String mMessage;
    public String mTitle;
    public List<UpdateInfo> mUpdateInfoList;

    public CheckResult(Parcel parcel) {
        readFromParcel(parcel);
    }

    private void readFromParcel(Parcel parcel) {
        this.mTitle = parcel.readString();
        this.mMessage = parcel.readString();
        this.mUpdateInfoList = new ArrayList();
        parcel.readList(this.mUpdateInfoList, getClass().getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mTitle);
        parcel.writeString(this.mMessage);
        parcel.writeList(this.mUpdateInfoList);
    }
}
