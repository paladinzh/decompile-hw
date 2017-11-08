package com.huawei.hwid.core.helper.handler;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.huawei.hwid.core.encrypt.f;

public class ErrorStatus implements Parcelable {
    public static final Creator<ErrorStatus> CREATOR = new a();
    private int a;
    private String b;

    public ErrorStatus(int i, String str) {
        this.a = i;
        this.b = str;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.a);
        parcel.writeString(this.b);
    }

    public int getErrorCode() {
        return this.a;
    }

    public String getErrorReason() {
        return this.b;
    }

    public String toString() {
        return "[ErrorCode]:" + this.a + ", [ErrorReason:]" + f.a(this.b);
    }
}
