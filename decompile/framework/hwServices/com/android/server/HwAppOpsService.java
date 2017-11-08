package com.android.server;

import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Slog;
import java.io.File;
import java.util.List;

public class HwAppOpsService extends AppOpsService {
    static final long DELAY_POST_SYSTEM_ALERT_WINDOW = 500;
    static final String TAG = HwAppOpsService.class.getSimpleName();

    public HwAppOpsService(File storagePath) {
        super(storagePath, null);
    }

    public HwAppOpsService(File storagePath, Handler handler) {
        super(storagePath, handler);
    }

    protected void scheduleWriteLockedHook(int code) {
    }

    public void setUserRestriction(int code, boolean restricted, IBinder token, int userHandle, String[] exceptionPackages) {
        int callingUid = Binder.getCallingUid();
        if ((45 == code || 24 == code) && callingUid != 1000) {
            long identity = Binder.clearCallingIdentity();
            try {
                if ("com.google.android.packageinstaller".equals(Settings.getPackageNameForUid(this.mContext, callingUid))) {
                    List<UserInfo> users = ((UserManager) this.mContext.getSystemService("user")).getProfiles(userHandle);
                    if (users != null) {
                        for (UserInfo ui : users) {
                            if (ui.id != userHandle) {
                                super.setUserRestriction(code, restricted, token, ui.id, exceptionPackages);
                                Slog.i(TAG, "AppOpsService.setUserRestriction code: " + code + ", restricted: " + restricted + ", userId: " + ui.id + ", callingUid: " + callingUid);
                            }
                        }
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }
        super.setUserRestriction(code, restricted, token, userHandle, exceptionPackages);
    }
}
