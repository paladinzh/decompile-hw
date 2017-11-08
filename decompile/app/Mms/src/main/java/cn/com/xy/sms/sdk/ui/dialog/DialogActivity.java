package cn.com.xy.sms.sdk.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import cn.com.xy.sms.sdk.SmartSmsSdkDoAction;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import com.google.android.gms.R;

public class DialogActivity extends Activity {
    private AlertDialog mDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setBackgroundDrawable(new ColorDrawable(17170445));
        showDownloadHwLeftDialog();
    }

    private void showDownloadHwLeftDialog() {
        this.mDialog = new Builder(this).setTitle(R.string.duoqu_notity).setCancelable(true).setMessage(R.string.duoqu_down_hwLife_3).setPositiveButton(R.string.duoqu_down, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                try {
                    DuoquUtils.getSdkDoAction().downLoadApp(DialogActivity.this, "com.huawei.lives", SmartSmsSdkDoAction.HUAWEI_LIFE_DOWN_URL, null);
                } catch (Exception e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog("showDownloadHwLeftDialog PositiveButton error:" + e.getMessage(), e);
                }
                DialogActivity.this.dismissDialog();
            }
        }).setNegativeButton(R.string.duoqu_setting_cancel, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                DialogActivity.this.dismissDialog();
            }
        }).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface arg0) {
                DialogActivity.this.finish();
                DialogActivity.this.overridePendingTransition(0, 0);
            }
        }).show();
    }

    protected void onStart() {
        super.onStart();
        setVisible(true);
    }

    public void dismissDialog() {
        if (this.mDialog != null) {
            if (this.mDialog.isShowing()) {
                this.mDialog.dismiss();
                this.mDialog = null;
            } else {
                this.mDialog = null;
            }
        }
    }
}
