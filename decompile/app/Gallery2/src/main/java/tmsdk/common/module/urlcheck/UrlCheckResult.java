package tmsdk.common.module.urlcheck;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdkobf.et;

/* compiled from: Unknown */
public class UrlCheckResult implements Parcelable {
    public static Creator<UrlCheckResult> CREATOR = new Creator<UrlCheckResult>() {
        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return s(parcel);
        }

        public UrlCheckResult[] ct(int i) {
            return new UrlCheckResult[i];
        }

        public /* synthetic */ Object[] newArray(int i) {
            return ct(i);
        }

        public UrlCheckResult s(Parcel parcel) {
            UrlCheckResult urlCheckResult = new UrlCheckResult();
            urlCheckResult.mainHarmId = parcel.readInt();
            urlCheckResult.result = parcel.readInt();
            urlCheckResult.mErrCode = parcel.readInt();
            return urlCheckResult;
        }
    };
    public static final int RESULT_HARM = 3;
    public static final int RESULT_REGULAR = 0;
    public static final int RESULT_SHADINESS = 2;
    public static final int RESULT_UNKNOWN = Integer.MAX_VALUE;
    public int mErrCode;
    public int mainHarmId;
    public int result;

    private UrlCheckResult() {
        this.mainHarmId = Integer.MAX_VALUE;
        this.result = Integer.MAX_VALUE;
        this.mErrCode = 0;
    }

    public UrlCheckResult(int i) {
        this.mainHarmId = Integer.MAX_VALUE;
        this.result = Integer.MAX_VALUE;
        this.mErrCode = 0;
        this.mErrCode = i;
    }

    public UrlCheckResult(et etVar) {
        this.mainHarmId = Integer.MAX_VALUE;
        this.result = Integer.MAX_VALUE;
        this.mErrCode = 0;
        this.mainHarmId = etVar.mainHarmId;
        if (this.mainHarmId == 13) {
            this.mainHarmId = 0;
        }
        this.result = etVar.ld;
        if (this.result == 1) {
            this.result = 0;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mainHarmId);
        parcel.writeInt(this.result);
        parcel.writeInt(this.mErrCode);
    }
}
