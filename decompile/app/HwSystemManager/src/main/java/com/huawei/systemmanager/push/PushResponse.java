package com.huawei.systemmanager.push;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import com.huawei.systemmanager.comm.misc.StringUtils;
import com.huawei.systemmanager.util.HwLog;

public class PushResponse implements Parcelable {
    public static final String ACTION_FIELD = "action";
    public static final Creator<PushResponse> CREATOR = new Creator<PushResponse>() {
        public PushResponse[] newArray(int size) {
            return new PushResponse[size];
        }

        public PushResponse createFromParcel(Parcel source) {
            return new PushResponse(source);
        }
    };
    public static final String DATA_FIELD = "data";
    private static final String FILE_ID_ACTION = "com.huawei.systemmanager.action.downloadfileid";
    public static final String FILE_NAME_FIELD = "fileName";
    public static final String MODULE_FIELD = "module";
    public static final String PACKAGE_NAME_FIELD = "packageName";
    public static final String PUSH_TYPE_FIELD = "pushType";
    private static final String TAG = "PushResponse";
    public String action = null;
    public String data;
    public String fileId = null;
    public String fileName = null;
    public String module = null;
    public String packageName = null;
    public String pushType = null;
    public String romVersion = null;

    public PushResponse(Parcel source) {
        this.pushType = source.readString();
        this.packageName = source.readString();
        this.romVersion = source.readString();
        this.module = source.readString();
        this.fileName = source.readString();
        this.action = source.readString();
        this.fileId = source.readString();
        this.data = source.readString();
    }

    public void sendFileIntent(Context context) {
        HwLog.i(TAG, "sendFileIntent action " + this.action);
        if (!StringUtils.isEmpty(this.action)) {
            Intent intent = new Intent();
            intent.setExtrasClassLoader(PushResponse.class.getClassLoader());
            intent.putExtra(TAG, this);
            intent.setAction(FILE_ID_ACTION);
            context.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        }
    }

    public void sendNormalIntent(Context context) {
        HwLog.i(TAG, "sendNormalIntent action " + this.action);
        if (!StringUtils.isEmpty(this.action)) {
            Intent intent = new Intent();
            intent.putExtra(PUSH_TYPE_FIELD, this.pushType);
            intent.putExtra(DATA_FIELD, this.data);
            intent.setAction(this.action);
            context.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        }
    }

    public String getFilePath() {
        return (StringUtils.isEmpty(this.module) ? "" : this.module + "/") + this.fileName;
    }

    public String toString() {
        return "pushType:" + this.pushType + ", packageName:" + this.packageName + ", romVersion:" + this.romVersion + ", module:" + this.module + ", fileName:" + this.fileName + ", action:" + this.action + ", fileId:" + this.fileId + ", data:" + this.data;
    }

    public String getUri() {
        return "content://com.huawei.systemmanager.push.provider/" + this.module + "/" + this.fileName;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.pushType);
        dest.writeString(this.packageName);
        dest.writeString(this.romVersion);
        dest.writeString(this.module);
        dest.writeString(this.fileName);
        dest.writeString(this.action);
        dest.writeString(this.fileId);
        dest.writeString(this.data);
    }

    public int describeContents() {
        return 0;
    }
}
