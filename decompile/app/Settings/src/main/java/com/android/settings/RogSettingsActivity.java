package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.IPackageDataObserver.Stub;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.huawei.android.hardware.display.DisplayManagerCustEx;

public class RogSettingsActivity extends Activity {
    private int gClearCount = 0;
    private int gCompletedCount = 0;
    private ClearWorkObserver mClearObs = new ClearWorkObserver();
    private DisplayManagerCustEx mDisplayManagerCustEx = new DisplayManagerCustEx();
    private Handler mDisplaySwitchHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    RogSettingsActivity.this.setLowPowerDisplay();
                    return;
                default:
                    return;
            }
        }
    };
    private AlertDialog mRogSettingsDialog;

    class ClearWorkObserver extends Stub {
        ClearWorkObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            RogSettingsActivity rogSettingsActivity = RogSettingsActivity.this;
            rogSettingsActivity.gCompletedCount = rogSettingsActivity.gCompletedCount + 1;
            Log.d("RogSettingsActivity", "onRemoveCompleted: " + RogSettingsActivity.this.gCompletedCount);
            if (RogSettingsActivity.this.gCompletedCount == RogSettingsActivity.this.gClearCount) {
                RogSettingsActivity.this.mDisplaySwitchHandler.sendMessage(RogSettingsActivity.this.mDisplaySwitchHandler.obtainMessage(1));
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(17170445);
        showDialog(100);
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 100:
                String msg = getResources().getString(2131629107);
                boolean isCrypting = getIntent().getBooleanExtra("IS_CRYPTING", false);
                if (1 == this.mDisplayManagerCustEx.getLowPowerDisplayLevel()) {
                    msg = getResources().getString(2131629108);
                }
                int titlsRes = 2131629106;
                int positiveButtonRes = 17039370;
                if (isCrypting) {
                    msg = getResources().getString(2131629308);
                    positiveButtonRes = 2131626104;
                    titlsRes = 2131627350;
                }
                if (this.mRogSettingsDialog != null && this.mRogSettingsDialog.isShowing()) {
                    this.mRogSettingsDialog.dismiss();
                }
                this.mRogSettingsDialog = new Builder(this).setTitle(titlsRes).setMessage(msg).setPositiveButton(positiveButtonRes, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (LockPatternUtils.isDeviceEncryptionEnabled()) {
                            RogSettingsActivity.this.mRogSettingsDialog.dismiss();
                        } else {
                            RogSettingsActivity.this.switchDisplayLevel();
                        }
                    }
                }).setNegativeButton(2131627333, null).create();
                this.mRogSettingsDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        RogSettingsActivity.this.finish();
                    }
                });
                this.mRogSettingsDialog.show();
                break;
        }
        return super.onCreateDialog(id);
    }

    private void setLowPowerDisplay() {
        if (!ActivityManager.isUserAMonkey()) {
            int level;
            if (this.mDisplayManagerCustEx == null) {
                this.mDisplayManagerCustEx = new DisplayManagerCustEx();
            }
            if (this.mDisplayManagerCustEx.getLowPowerDisplayLevel() == 0) {
                level = 1;
            } else {
                level = 0;
            }
            this.mDisplayManagerCustEx.setLowPowerDisplayLevel(level);
            Log.d("RogSettingsActivity", "Set low power display level: " + level);
            try {
                IPowerManager.Stub.asInterface(ServiceManager.getService("power")).reboot(false, "huawei_reboot", false);
            } catch (RemoteException e) {
                Log.e("RogSettingsActivity", "PowerManager service died!", e);
            }
        }
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onDestroy() {
        if (this.mRogSettingsDialog != null && this.mRogSettingsDialog.isShowing()) {
            this.mRogSettingsDialog.dismiss();
        }
        super.onDestroy();
    }

    private void switchDisplayLevel() {
        int i = 0;
        Log.d("RogSettingsActivity", "switchDisplayLevel");
        this.gCompletedCount = 0;
        PackageManager pm = getPackageManager();
        ActivityManager am = (ActivityManager) getSystemService("activity");
        String[] packageForClearCache = new String[]{"com.android.gallery3d"};
        String[] packageForClearData = new String[]{"com.meitu.mtxx.huawei"};
        this.gClearCount = packageForClearCache.length + packageForClearData.length;
        for (String s : packageForClearCache) {
            pm.deleteApplicationCacheFiles(s, this.mClearObs);
        }
        int length = packageForClearData.length;
        while (i < length) {
            am.clearApplicationUserData(packageForClearData[i], this.mClearObs);
            i++;
        }
    }
}
