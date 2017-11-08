package com.android.contacts.list;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.contacts.compatibility.DirectoryCompat;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class DirectorySearchManager {
    private static final String TAG = DirectorySearchManager.class.getSimpleName();
    private static W3Version w3Version = null;
    private boolean abort = false;
    private boolean exchangeLogon = false;
    private LogStatusChangedListener mListener;
    private int mRemoteDirectoryCount = 0;

    public interface LogStatusChangedListener {
        void onExchangeStatusChanged(boolean z);
    }

    public enum W3Version {
        NONE_SUPPORT,
        SEARCH_ACTION_SUPPORT,
        SPLASH_ACTION_SUPPORT
    }

    public boolean getExchangeLogStatus() {
        return this.exchangeLogon;
    }

    public void updateDiretories(Cursor cursor) {
        if (cursor != null) {
            int position = cursor.getPosition();
            int remoteCount = 0;
            this.exchangeLogon = false;
            int idColumnIndex = cursor.getColumnIndex("_id");
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                if (DirectoryCompat.isRemoteDirectory(cursor.getLong(idColumnIndex))) {
                    this.exchangeLogon = true;
                    remoteCount++;
                }
            }
            cursor.moveToPosition(position);
            if (remoteCount != this.mRemoteDirectoryCount) {
                this.mRemoteDirectoryCount = remoteCount;
                if (this.mListener != null) {
                    this.mListener.onExchangeStatusChanged(this.exchangeLogon);
                }
            }
        }
    }

    public boolean updateExchangeButton(TextView exchange) {
        int i = 0;
        if (exchange == null) {
            return false;
        }
        boolean visible = this.exchangeLogon && !EmuiFeatureManager.isSuperSaverMode();
        if (!visible) {
            i = 8;
        }
        exchange.setVisibility(i);
        return visible;
    }

    public void abortResult(boolean flag) {
        this.abort = flag;
    }

    public void setListener(LogStatusChangedListener listener) {
        this.mListener = listener;
    }

    public boolean getExchangeOrW3Status() {
        boolean support = w3Version != W3Version.SPLASH_ACTION_SUPPORT ? w3Version == W3Version.SEARCH_ACTION_SUPPORT : true;
        if ((this.exchangeLogon || support) && !EmuiFeatureManager.isSuperSaverMode()) {
            return true;
        }
        return false;
    }

    public boolean updateW3Button(TextView w3) {
        boolean visible = true;
        int i = 0;
        if (w3 == null) {
            return false;
        }
        boolean support;
        if (w3Version == W3Version.SPLASH_ACTION_SUPPORT) {
            support = true;
        } else if (w3Version == W3Version.SEARCH_ACTION_SUPPORT) {
            support = true;
        } else {
            support = false;
        }
        if (!support || EmuiFeatureManager.isSuperSaverMode()) {
            visible = false;
        }
        if (!visible) {
            i = 8;
        }
        w3.setVisibility(i);
        return visible;
    }

    public static void updateW3PackageInfo(PackageManager pm) {
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo("huawei.w3", 16384);
        } catch (NameNotFoundException e) {
            HwLog.e(TAG, "NameNotFoundException " + e.getMessage());
        }
        if (info == null) {
            w3Version = null;
        } else if (info.versionCode > 901) {
            w3Version = W3Version.SPLASH_ACTION_SUPPORT;
        } else {
            Intent intent = new Intent("huawei.w3.contact.SEARCH");
            intent.setPackage("huawei.w3");
            if (intent.resolveActivity(pm) == null) {
                w3Version = W3Version.NONE_SUPPORT;
            } else {
                w3Version = W3Version.SEARCH_ACTION_SUPPORT;
            }
        }
    }

    public static void startW3Activity(Context context, String searchStr) {
        if (context != null && searchStr != null && !TextUtils.isEmpty(searchStr.trim())) {
            try {
                context.startActivity(getW3SearchIntent(searchStr));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, R.string.quickcontact_missing_app_Toast, 1).show();
            }
        }
    }

    public boolean isDirectorySearchAborted(long directoryId) {
        return this.abort ? DirectoryCompat.isRemoteDirectory(directoryId) : false;
    }

    private static Intent getW3SearchIntent(String searchStr) {
        if (w3Version == W3Version.SPLASH_ACTION_SUPPORT) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setClassName("huawei.w3", "huawei.w3.welcome.W3SplashScreenActivity");
            intent.putExtra("src", 203);
            intent.putExtra("target", OfflineMapStatus.EXCEPTION_SDCARD);
            intent.putExtra("uri", "activity://com.huawei.it.w3m.im/contactSearchActivity?immediateSearch=true&searchKey=" + searchStr);
            return intent;
        }
        intent = new Intent("huawei.w3.contact.SEARCH");
        intent.putExtra("searchKey", searchStr);
        return intent;
    }
}
