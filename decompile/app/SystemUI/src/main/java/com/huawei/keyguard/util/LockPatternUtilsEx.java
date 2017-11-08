package com.huawei.keyguard.util;

import android.content.Context;
import android.os.RemoteException;
import com.android.internal.widget.LockPatternUtils;

public class LockPatternUtilsEx extends LockPatternUtils {
    public LockPatternUtilsEx(Context context) {
        super(context);
    }

    public boolean isSavedPasswordExists(int userId) {
        try {
            return getLockSettings().havePassword(userId);
        } catch (RemoteException e) {
            return false;
        }
    }
}
