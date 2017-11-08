package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Secure;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;

public class ObserverStudentMode extends ObserverItem<Boolean> {
    private boolean mIsStudentMode = false;

    public ObserverStudentMode(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return Uri.parse("content://com.huawei.parentcontrol/childmode_status");
    }

    public void onChange() {
        this.mIsStudentMode = getStudentMode();
        HwLog.i("ObserverStudentMode", "mIsStudentMode=" + this.mIsStudentMode);
    }

    public Boolean getValue() {
        return Boolean.valueOf(this.mIsStudentMode);
    }

    private boolean getStudentMode() {
        return 1 == Secure.getInt(this.mContext.getContentResolver(), "childmode_status", 0) && SystemUiUtil.isPackageExist(this.mContext, "com.huawei.parentcontrol");
    }
}
