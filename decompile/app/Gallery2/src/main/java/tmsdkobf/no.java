package tmsdkobf;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdk.common.module.intelli_sms.SmsCheckResult.SmsRuleTypeID;

/* compiled from: Unknown */
public class no implements Parcelable {
    public static final Creator<no> CREATOR = new Creator<no>() {
        public no[] bL(int i) {
            return new no[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return j(parcel);
        }

        public no j(Parcel parcel) {
            no noVar = new no();
            noVar.el = parcel.readInt();
            noVar.em = parcel.readInt();
            return noVar;
        }

        public /* synthetic */ Object[] newArray(int i) {
            return bL(i);
        }
    };
    public int el;
    public int em;

    private no() {
    }

    public no(SmsRuleTypeID smsRuleTypeID) {
        this.el = smsRuleTypeID.uiRuleType;
        this.em = smsRuleTypeID.uiRuleTypeId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.el);
        parcel.writeInt(this.em);
    }
}
