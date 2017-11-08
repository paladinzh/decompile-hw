package cn.com.xy.sms.sdk.ui.popu.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.dialog.GotoMyPositionDialog;
import cn.com.xy.sms.sdk.ui.popu.web.IXYSmartMessageActivity;
import cn.com.xy.sms.sdk.ui.popu.web.NearbyPointListFragment;
import cn.com.xy.sms.sdk.ui.settings.PermissionRequestActivity;
import com.android.mms.ui.PermissionCheckActivity;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseFragment;
import org.json.JSONObject;

public class PermissionRequestFragment extends HwBaseFragment {
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
    private JSONObject jsObject;
    private Activity mActivity = null;
    private String[] mPermissionArr = new String[]{LOCATION_PERMISSION};
    private long mRequestTimeMillis = 0;
    private int mStatu = 0;
    private NearbyPointListFragment nearListFragmint;

    public void setJsObject(JSONObject jsObject) {
        this.jsObject = jsObject;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = new View(this.mActivity);
        rootView.setBackgroundResource(17170445);
        return rootView;
    }

    public void onResume() {
        super.onResume();
        if (this.mStatu == 2) {
            if (isOnLocationService()) {
                redirectToTarget();
            } else {
                finish();
            }
        } else if (this.mStatu != 1) {
            this.mStatu = 1;
            try {
                if (!PermissionRequestActivity.appHasLocationPermission()) {
                    tryRequestPermission();
                } else if (isOnLocationService()) {
                    redirectToTarget();
                } else {
                    gotoPositionDialog();
                }
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("PermissionRequestFragment onResume error " + e.getMessage(), e);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (PermissionRequestActivity.appHasLocationPermission()) {
                if (isOnLocationService()) {
                    redirectToTarget();
                } else {
                    gotoPositionDialog();
                }
                return;
            }
            if (SystemClock.elapsedRealtime() - this.mRequestTimeMillis < AUTOMATED_RESULT_THRESHOLD_MILLLIS) {
                MLog.w(TAG, "onRequestPermissionsResult user permenent reject");
                if (!PermissionCheckActivity.recheckUserRejectPermissions(this.mActivity, this.mPermissionArr)) {
                    MLog.w(TAG, "onRequestPermissionsResult user permenent reject and recheck fail");
                    PermissionCheckActivity.gotoPackageSettings(this.mActivity);
                } else {
                    return;
                }
            }
            MLog.w(TAG, "onRequestPermissionsResult user reject");
            finish();
        }
    }

    private void tryRequestPermission() {
        try {
            this.mRequestTimeMillis = SystemClock.elapsedRealtime();
            requestPermissions(this.mPermissionArr, 1);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("PermissionRequestFragment tryRequestPermission error " + e.getMessage(), e);
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

    private boolean isOnLocationService() {
        boolean z = false;
        try {
            if (getLocationMode(this.mActivity) != 0) {
                z = true;
            }
            return z;
        } catch (Throwable e) {
            e.printStackTrace();
            return true;
        }
    }

    private void redirectToTarget() {
        if (this.nearListFragmint == null) {
            this.nearListFragmint = new NearbyPointListFragment();
            this.nearListFragmint.loasList(this.jsObject);
        }
        finish();
    }

    public static void redirectToLocationServiceSetting(Context ctx) {
        try {
            Intent it = new Intent();
            it.setPackage("com.android.settings");
            it.setAction("android.settings.LOCATION_SOURCE_SETTINGS");
            ctx.startActivity(it);
        } catch (Throwable e) {
            e.printStackTrace();
            SmartSmsSdkUtil.smartSdkExceptionLog("PermissionRequestFragment tryRequestPermission error " + e.getMessage(), e);
        }
    }

    private void finish() {
        if (this.mActivity instanceof IXYSmartMessageActivity) {
            ((IXYSmartMessageActivity) this.mActivity).finshFragemnt(this);
        }
    }

    private void gotoPositionDialog() {
        GotoMyPositionDialog.show(this.mActivity, new XyCallBack() {
            public void execute(Object... arg) {
                if (arg != null && arg.length >= 1 && arg[0] != null && (arg[0] instanceof Integer)) {
                    switch (((Integer) arg[0]).intValue()) {
                        case 0:
                            PermissionRequestFragment.this.finish();
                            break;
                        case 1:
                            PermissionRequestFragment.redirectToLocationServiceSetting(PermissionRequestFragment.this.mActivity);
                            PermissionRequestFragment.this.mStatu = 2;
                            break;
                    }
                }
            }
        });
    }
}
