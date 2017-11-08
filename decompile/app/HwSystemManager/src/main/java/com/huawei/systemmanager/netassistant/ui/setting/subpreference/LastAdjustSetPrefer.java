package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.ToastUtils;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.netassistant.traffic.trafficcorrection.NetState;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.util.HwLog;

public class LastAdjustSetPrefer extends BaseTrafficSetPreference {
    public static final String TAG = "LastAdjustSetPrefer";
    private Runnable mLoadSummaryTask = new Runnable() {
        public void run() {
            if (LastAdjustSetPrefer.this.mCard == null) {
                HwLog.e(LastAdjustSetPrefer.TAG, "refreshPreferShow, mCard == null");
                return;
            }
            LastAdjustSetPrefer.this.postSetSummary(CommonMethodUtil.formatBytes(LastAdjustSetPrefer.this.getContext(), Math.max(0, LastAdjustSetPrefer.this.mCard.getAdjustSetValue())));
        }
    };

    public LastAdjustSetPrefer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LastAdjustSetPrefer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initValue() {
        super.initValue();
        setKey(TAG);
        setTitle(R.string.content_manual_adjust_settings);
        setDialogTitle(R.string.content_manual_adjust_settings);
        setDialogLayoutResource(R.layout.sub_settings_manual_adjust);
    }

    public void refreshPreferShow() {
        postRunnableAsync(this.mLoadSummaryTask);
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        View buttonSMSView = (Button) view.findViewById(R.id.content_flow_limited_title);
        buttonSMSView.setClickable(true);
        buttonSMSView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (NetState.isCurrentNetActive()) {
                    Dialog dialog = LastAdjustSetPrefer.this.getDialog();
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    LastAdjustSetPrefer.this.createPromptMessageDialog();
                    HsmStat.statE(90);
                    return;
                }
                ToastUtils.toastLongMsg((int) R.string.msm_adjust_no_network);
            }
        });
        if (!CustomizeManager.getInstance().isFeatureEnabled(30)) {
            ViewUtils.setVisibility(buttonSMSView, 4);
        }
    }

    protected void onSetPackage(long size) {
        if (this.mCard == null) {
            HwLog.e(TAG, "onSetPackage mCard == null");
            return;
        }
        this.mCard.setAdjustSetValue(size);
        HsmStat.statE(89);
        refreshPreferShow();
    }

    protected long getEditTxtValue() {
        return getAdjustSetValue();
    }

    private void createPromptMessageDialog() {
        new Builder(getContext()).setTitle(R.string.common_dialog_title_tip).setMessage(R.string.net_assistant_manual_mms_message).setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    LastAdjustSetPrefer.this.mCard.sendAdjustSMS();
                    ToastUtils.toastShortMsg((int) R.string.net_assistant_toast_manul_send_sms_Toast);
                } catch (Exception e) {
                    HwLog.e(LastAdjustSetPrefer.TAG, "setAdjustItemInfo Exception !");
                    e.printStackTrace();
                }
            }
        }).setNegativeButton(R.string.alert_dialog_cancel, null).create().show();
    }

    private long getAdjustSetValue() {
        if (this.mCard != null) {
            return this.mCard.getAdjustSetValue();
        }
        HwLog.e(TAG, "getAdjustSetValue, mCard == null");
        return -1;
    }
}
