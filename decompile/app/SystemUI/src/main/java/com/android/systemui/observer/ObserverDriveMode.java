package com.android.systemui.observer;

import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import com.android.systemui.HwSystemUIApplication;
import com.android.systemui.utils.HwLog;

public class ObserverDriveMode extends ObserverItem<Boolean> {
    private boolean mIsDriveMode = false;
    private boolean mSuccess = false;

    public ObserverDriveMode(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return Uri.parse("content://com.huawei.vdrive.mirrorlinktogo/mirrorlink_status");
    }

    public void onChange() {
        this.mIsDriveMode = getDriveMode();
        HwLog.i("ObserverDriveMode", "mIsDriveMode=" + this.mIsDriveMode);
    }

    public Boolean getValue() {
        if (!this.mSuccess) {
            this.mIsDriveMode = getDriveMode();
        }
        return Boolean.valueOf(this.mIsDriveMode);
    }

    private boolean getDriveMode() {
        int status = 0;
        Cursor cursor = null;
        try {
            cursor = HwSystemUIApplication.getContext().getContentResolver().query(Uri.parse("content://com.huawei.vdrive.mirrorlinktogo/mirrorlink_status"), null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                status = cursor.getInt(cursor.getColumnIndex("connected"));
                HwLog.i("ObserverDriveMode", "status = " + status);
            }
            this.mSuccess = true;
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e("ObserverDriveMode", "Query failed, URI = content://com.huawei.vdrive.mirrorlinktogo/mirrorlink_status");
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (status == 1) {
            return true;
        }
        return false;
    }
}
