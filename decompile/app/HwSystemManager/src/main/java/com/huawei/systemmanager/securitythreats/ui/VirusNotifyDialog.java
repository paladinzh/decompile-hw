package com.huawei.systemmanager.securitythreats.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.securitythreats.ui.VirusNotifyService.VirusNotifyCallback;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class VirusNotifyDialog extends AlertDialog implements OnClickListener {
    private static final String TAG = "VirusNotifyDialog";
    private CheckBox mCheckBox;
    private final Context mContext;
    private ImageView mIcon;
    private TextView mLabel;
    private TextView mLine1;
    private final boolean mNeedStat;
    private boolean mNoNeedRemind = false;
    private final String mPkg;
    private RelativeLayout mVirusCheckboxView;
    private final int mVirusLevel;
    private final VirusNotifyCallback mVirusNotifyCallback;

    public VirusNotifyDialog(Context context, int themeResId, HsmPkgInfo info, boolean needStat, boolean needCallback, int level, VirusNotifyCallback callback) {
        super(context, themeResId);
        this.mContext = context;
        this.mPkg = info.getPackageName();
        this.mVirusLevel = level;
        this.mNeedStat = needStat;
        this.mVirusNotifyCallback = callback;
        View virusNotify = LayoutInflater.from(this.mContext).inflate(R.layout.virus_notify_dialog, null);
        setView(virusNotify);
        setCancelable(false);
        getWindow().setType(2003);
        this.mIcon = (ImageView) virusNotify.findViewById(R.id.virus_notify_icon);
        this.mIcon.setImageDrawable(info.icon());
        this.mLabel = (TextView) virusNotify.findViewById(R.id.virus_notify_label);
        this.mLabel.setText(info.label());
        this.mLine1 = (TextView) virusNotify.findViewById(R.id.virus_notify_tv_line1);
        this.mLine1.setText(1 == this.mVirusLevel ? R.string.virus_dialog_content_risk : R.string.virus_dialog_content_virus);
        this.mCheckBox = (CheckBox) virusNotify.findViewById(R.id.virus_checkbox);
        this.mCheckBox.setChecked(this.mNoNeedRemind);
        this.mVirusCheckboxView = (RelativeLayout) virusNotify.findViewById(R.id.virus_checkbox_view);
        this.mVirusCheckboxView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean z;
                boolean z2 = false;
                CheckBox -get0 = VirusNotifyDialog.this.mCheckBox;
                if (VirusNotifyDialog.this.mNoNeedRemind) {
                    z = false;
                } else {
                    z = true;
                }
                -get0.setChecked(z);
                VirusNotifyDialog virusNotifyDialog = VirusNotifyDialog.this;
                if (!VirusNotifyDialog.this.mNoNeedRemind) {
                    z2 = true;
                }
                virusNotifyDialog.mNoNeedRemind = z2;
                HwLog.i(VirusNotifyDialog.TAG, "mVirusCheckboxView onClick:" + VirusNotifyDialog.this.mNoNeedRemind);
            }
        });
        setButton(-2, getContext().getString(R.string.virus_dialog_not_handle), this);
        setButton(-1, getContext().getString(R.string.virus_dialog_uninstall), this);
    }

    public void onClick(DialogInterface dialog, int which) {
        boolean uninstall;
        switch (which) {
            case -2:
                uninstall = false;
                break;
            case -1:
                uninstall = true;
                break;
            default:
                uninstall = false;
                break;
        }
        this.mVirusNotifyCallback.onResult(this.mPkg, this.mNoNeedRemind, this.mNeedStat, uninstall);
    }
}
