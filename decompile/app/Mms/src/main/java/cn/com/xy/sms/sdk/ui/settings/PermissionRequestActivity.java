package cn.com.xy.sms.sdk.ui.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.dialog.GotoMyPositionDialog;
import cn.com.xy.sms.sdk.ui.popu.web.NearbyPointList;
import com.android.messaging.util.OsUtil;
import com.android.mms.ui.PermissionCheckActivity;
import com.huawei.cspcommon.MLog;

public class PermissionRequestActivity extends Activity {
    private static final long AUTOMATED_RESULT_THRESHOLD_MILLLIS = 500;
    private static final String LOCATION_MODE = "location_mode";
    private static final String LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION";
    public static final int REQUEST_NEAR_SITE = 1;
    public static final int REQUEST_PER_ENTER_BACK_LOCATION_SERVICE_SETTING = 2;
    public static final int REQUEST_PER_ENTER_FIRST = 0;
    public static final int REQUEST_PER_ENTER_FROM_OTHER = 1;
    public static final String REQUEST_TARGET_KEY = "request_target_key";
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 1;
    private static final String TAG = "XYRequestPerm";
    private String[] mPermissionArr = new String[]{LOCATION_PERMISSION};
    private long mRequestTimeMillis = 0;
    private int mStatu = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setBackgroundDrawableResource(17170445);
    }

    private void gotoPositionDialog() {
        GotoMyPositionDialog.show(this, new XyCallBack() {
            public void execute(Object... arg) {
                if (arg != null && arg.length >= 1 && arg[0] != null && (arg[0] instanceof Integer)) {
                    switch (((Integer) arg[0]).intValue()) {
                        case 0:
                            PermissionRequestActivity.this.finish();
                            break;
                        case 1:
                            PermissionRequestActivity.redirectToLocationServiceSetting(PermissionRequestActivity.this);
                            PermissionRequestActivity.this.mStatu = 2;
                            break;
                    }
                }
            }
        });
    }

    protected void onResume() {
        super.onResume();
        if (this.mStatu == 2) {
            if (isOnLocationService()) {
                redirectToTarget();
            } else {
                finish();
            }
        } else if (this.mStatu != 1) {
            setVisible(true);
            this.mStatu = 1;
            try {
                if (!appHasLocationPermission()) {
                    tryRequestPermission();
                } else if (isOnLocationService()) {
                    redirectToTarget();
                } else {
                    gotoPositionDialog();
                }
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("PermissionRequestActivity onResume error " + e.getMessage(), e);
            }
        }
    }

    private void tryRequestPermission() {
        try {
            this.mRequestTimeMillis = SystemClock.elapsedRealtime();
            requestPermissions(this.mPermissionArr, 1);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("PermissionRequestActivity tryRequestPermission error " + e.getMessage(), e);
        }
    }

    public static boolean appHasLocationPermission() {
        boolean hasLocationPermission = false;
        try {
            return OsUtil.hasLocationPermission();
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("PermissionRequestActivity appHasLocationPermission error " + e.getMessage(), e);
            return hasLocationPermission;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (appHasLocationPermission()) {
                if (isOnLocationService()) {
                    redirectToTarget();
                } else {
                    gotoPositionDialog();
                }
                return;
            }
            if (SystemClock.elapsedRealtime() - this.mRequestTimeMillis < AUTOMATED_RESULT_THRESHOLD_MILLLIS) {
                MLog.w(TAG, "onRequestPermissionsResult user permenent reject");
                if (!PermissionCheckActivity.recheckUserRejectPermissions(this, this.mPermissionArr)) {
                    MLog.w(TAG, "onRequestPermissionsResult user permenent reject and recheck fail");
                    PermissionCheckActivity.gotoPackageSettings(this);
                } else {
                    return;
                }
            }
            MLog.w(TAG, "onRequestPermissionsResult user reject");
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (2 == requestCode) {
            if (resultCode == 0) {
                MLog.i(TAG, "recheckUserRejectPermissions: RESULT_CANCELED");
                finish();
            } else if (-1 == resultCode) {
                MLog.i(TAG, "User has grant permissions in package installer");
                if (appHasLocationPermission()) {
                    if (isOnLocationService()) {
                        redirectToTarget();
                    } else {
                        gotoPositionDialog();
                    }
                }
            }
        }
    }

    private boolean isOnLocationService() {
        boolean z = false;
        try {
            if (getLocationMode(this) != 0) {
                z = true;
            }
            return z;
        } catch (Throwable e) {
            e.printStackTrace();
            return true;
        }
    }

    public static int getLocationMode(Context context) {
        if (context != null) {
            try {
                return Secure.getInt(context.getContentResolver(), LOCATION_MODE, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private void redirectToTarget() {
        switch (getIntent().getIntExtra("request_target_key", 0)) {
            case 1:
                String menuName = null;
                Intent intent = getIntent();
                if (intent.hasExtra("menuName")) {
                    menuName = getIntent().getStringExtra("menuName");
                }
                NearbyPointList.openNearSiteActivity(this, intent.getStringExtra("address"), menuName);
                break;
        }
        finish();
    }

    public static void redirectToLocationServiceSetting(Context ctx) {
        try {
            Intent intent = new Intent();
            intent.setPackage("com.android.settings");
            intent.setAction("android.settings.LOCATION_SOURCE_SETTINGS");
            ctx.startActivity(intent);
        } catch (Throwable e) {
            e.printStackTrace();
            SmartSmsSdkUtil.smartSdkExceptionLog("PermissionRequestActivity tryRequestPermission error " + e.getMessage(), e);
        }
    }
}
