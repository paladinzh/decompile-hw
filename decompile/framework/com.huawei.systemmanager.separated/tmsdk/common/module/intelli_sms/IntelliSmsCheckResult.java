package tmsdk.common.module.intelli_sms;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdk.common.tcc.SmsCheckerContentTypes;
import tmsdk.common.tcc.SmsCheckerSuggestions;

/* compiled from: Unknown */
public final class IntelliSmsCheckResult implements Parcelable, SmsCheckerContentTypes, SmsCheckerSuggestions {
    public static final Creator<IntelliSmsCheckResult> CREATOR = new Creator<IntelliSmsCheckResult>() {
        public IntelliSmsCheckResult[] bJ(int i) {
            return new IntelliSmsCheckResult[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return h(parcel);
        }

        public IntelliSmsCheckResult h(Parcel parcel) {
            return new IntelliSmsCheckResult(parcel);
        }

        public /* synthetic */ Object[] newArray(int i) {
            return bJ(i);
        }
    };
    private MMatchSysResult Cq;
    public int suggestion;

    public IntelliSmsCheckResult(int i, MMatchSysResult mMatchSysResult) {
        this.suggestion = i;
        this.Cq = mMatchSysResult;
    }

    public IntelliSmsCheckResult(Parcel parcel) {
        this.suggestion = parcel.readInt();
        this.Cq = (MMatchSysResult) parcel.readParcelable(MMatchSysResult.class.getClassLoader());
    }

    public static boolean shouldBeBlockedOrNot(IntelliSmsCheckResult intelliSmsCheckResult) {
        if (intelliSmsCheckResult != null) {
            if (intelliSmsCheckResult.suggestion == 3 || intelliSmsCheckResult.suggestion == 2) {
                return true;
            }
        }
        return false;
    }

    public int contentType() {
        return this.Cq != null ? this.Cq.contentType : 1;
    }

    public int describeContents() {
        return 0;
    }

    public Object getSysResult() {
        return this.Cq;
    }

    public boolean isCheatSMS() {
        return contentType() == 4;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.suggestion);
        parcel.writeParcelable(this.Cq, 0);
    }
}
