package tmsdk.common.module.intelli_sms;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdk.common.module.intelli_sms.SmsCheckResult.SmsRuleTypeID;
import tmsdkobf.no;

/* compiled from: Unknown */
public class MMatchSysResult implements Parcelable {
    public static final Creator<MMatchSysResult> CREATOR = new Creator<MMatchSysResult>() {
        public MMatchSysResult[] bJ(int i) {
            return new MMatchSysResult[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return i(parcel);
        }

        public MMatchSysResult i(Parcel parcel) {
            MMatchSysResult mMatchSysResult = new MMatchSysResult();
            mMatchSysResult.finalAction = parcel.readInt();
            mMatchSysResult.contentType = parcel.readInt();
            mMatchSysResult.matchCnt = parcel.readInt();
            mMatchSysResult.minusMark = parcel.readInt();
            mMatchSysResult.actionReason = parcel.readInt();
            Object[] readArray = parcel.readArray(no.class.getClassLoader());
            if (readArray != null && readArray.length > 0) {
                int length = readArray.length;
                no[] noVarArr = new no[length];
                for (int i = 0; i < length; i++) {
                    noVarArr[i] = (no) readArray[i];
                }
                mMatchSysResult.ruleTypeID = noVarArr;
            }
            return mMatchSysResult;
        }

        public /* synthetic */ Object[] newArray(int i) {
            return bJ(i);
        }
    };
    public static final int EM_FINAL_ACTION_DOUBT = 3;
    public static final int EM_FINAL_ACTION_INTERCEPT = 2;
    public static final int EM_FINAL_ACTION_NEXT_STEP = 4;
    public static final int EM_FINAL_ACTION_PASS = 1;
    public int actionReason;
    public int contentType;
    public int finalAction;
    public int matchCnt;
    public int minusMark;
    public no[] ruleTypeID;

    private MMatchSysResult() {
    }

    public MMatchSysResult(int i, int i2, int i3, int i4, int i5, no[] noVarArr) {
        this.finalAction = i;
        this.contentType = i2;
        this.matchCnt = i3;
        this.minusMark = i4;
        this.actionReason = i5;
        this.ruleTypeID = noVarArr;
    }

    public MMatchSysResult(SmsCheckResult smsCheckResult) {
        this.finalAction = smsCheckResult.uiFinalAction;
        this.contentType = smsCheckResult.uiContentType;
        this.matchCnt = smsCheckResult.uiMatchCnt;
        this.minusMark = (int) smsCheckResult.fScore;
        this.actionReason = smsCheckResult.uiActionReason;
        if (smsCheckResult.stRuleTypeID == null) {
            this.ruleTypeID = null;
            return;
        }
        this.ruleTypeID = new no[smsCheckResult.stRuleTypeID.size()];
        for (int i = 0; i < this.ruleTypeID.length; i++) {
            this.ruleTypeID[i] = new no((SmsRuleTypeID) smsCheckResult.stRuleTypeID.get(i));
        }
    }

    public static int getSuggestion(MMatchSysResult mMatchSysResult) {
        int i = mMatchSysResult.finalAction;
        return (i > 0 && i <= 4) ? i == 1 ? (mMatchSysResult.actionReason == 1 || mMatchSysResult.actionReason == 5) ? mMatchSysResult.minusMark > 10 ? 4 : 1 : i : i : -1;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.finalAction);
        parcel.writeInt(this.contentType);
        parcel.writeInt(this.matchCnt);
        parcel.writeInt(this.minusMark);
        parcel.writeInt(this.actionReason);
        parcel.writeArray(this.ruleTypeID);
    }
}
