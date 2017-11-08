package cn.com.xy.sms.sdk.ui.dialog;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.util.ParseManager;
import com.google.android.gms.R;
import com.huawei.mms.util.StatisticalHelper;

public class EnhanceServiceDialog {
    public static final String TAG = "EnhanceServiceDialog";
    private int mAutoUpdateType = 0;
    private Context mContext;
    private OnClickListener mNegativeListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            SmartSmsSdkUtil.setEnhance(EnhanceServiceDialog.this.mContext, false);
            SmartSmsSdkUtil.setNoShowAgain(EnhanceServiceDialog.this.mContext, EnhanceServiceDialog.this.mNoShowAgain);
            StatisticalHelper.incrementReportCount(EnhanceServiceDialog.this.mContext, 2129);
            if (EnhanceServiceDialog.this.mNoShowAgain) {
                Toast.makeText(EnhanceServiceDialog.this.mContext, EnhanceServiceDialog.this.mContext.getResources().getString(R.string.duoqu_privacy_toast), 1).show();
            }
            PreferenceManager.getDefaultSharedPreferences(EnhanceServiceDialog.this.mContext).edit().putBoolean("pref_key_risk_url_check", false).commit();
        }
    };
    private boolean mNoShowAgain = true;
    private OnClickListener mPositiveListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            boolean onAppDestroyResetValue = !EnhanceServiceDialog.this.mNoShowAgain;
            SmartSmsSdkUtil.setEnhance(EnhanceServiceDialog.this.mContext, true, onAppDestroyResetValue);
            SmartSmsSdkUtil.setUpdateType(EnhanceServiceDialog.this.mContext, EnhanceServiceDialog.this.mAutoUpdateType, onAppDestroyResetValue);
            SmartSmsSdkUtil.setNoShowAgain(EnhanceServiceDialog.this.mContext, EnhanceServiceDialog.this.mNoShowAgain);
            StatisticalHelper.incrementReportCount(EnhanceServiceDialog.this.mContext, EnhanceServiceDialog.this.mAutoUpdateType == 1 ? 2128 : 2129);
            new Thread(new Runnable() {
                public void run() {
                    ParseManager.updateNow();
                }
            }).start();
            DuoquUtils.getSdkDoAction().simChange();
            PreferenceManager.getDefaultSharedPreferences(EnhanceServiceDialog.this.mContext).edit().putBoolean("pref_key_risk_url_check", true).commit();
        }
    };

    public EnhanceServiceDialog(Context ctx, int themeId, OnDismissListener onDismissListener) {
        this.mContext = ctx;
        Builder builder = new Builder(this.mContext);
        builder.setTitle(R.string.duoqu_privacy_title_5);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.mms_enable, this.mPositiveListener);
        builder.setNegativeButton(R.string.no, this.mNegativeListener);
        View contentsView = View.inflate(this.mContext, R.layout.duoqu_privacy_dialog, null);
        builder.setView(contentsView);
        final CheckBox checkbox = (CheckBox) contentsView.findViewById(R.id.ckb_not_disturb);
        checkbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EnhanceServiceDialog.this.mAutoUpdateType = checkbox.isChecked() ? 1 : 0;
            }
        });
        this.mAutoUpdateType = SmartSmsSdkUtil.getUpdateType(this.mContext);
        if (this.mAutoUpdateType == 0) {
            checkbox.setVisibility(0);
            this.mAutoUpdateType = 1;
        } else {
            checkbox.setVisibility(8);
        }
        builder.setOnDismissListener(onDismissListener);
        SmartSmsSdkUtil.setNoShowAgain(this.mContext, true);
        builder.create().show();
    }

    public static void show(Context ctx, OnDismissListener onDismissListener) {
        int themeID = ctx.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.NoActionBar", null, null);
        if (themeID == 0) {
            themeID = 16973941;
        }
        EnhanceServiceDialog enhanceServiceDialog = new EnhanceServiceDialog(ctx, themeID, onDismissListener);
    }
}
