package com.huawei.systemmanager.adblock.ui.view;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.adblock.ui.apkdlcheck.DlUrlCheckService;
import com.huawei.systemmanager.adblock.ui.connect.result.AdCheckUrlResult;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.text.DecimalFormat;

public class DlChoiceDialog extends AlertDialog implements OnClickListener, View.OnClickListener, OnDismissListener {
    private static final int SIZE_001M = 10485;
    private static final int SIZE_01M = 104857;
    private static final String TAG = "AdBlock_DlChoiceDialog";
    private final Callback mCallback;
    private final TextView mContinueTips;
    private final ImageView mIcon;
    private final TextView mLabel;
    private final TextView mLine1;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && DlUrlCheckService.SCREEN_ORIENTATION_ACTION.equals(intent.getAction())) {
                HwLog.i(DlChoiceDialog.TAG, "SCREEN_ORIENTATION_ACTION onReceive");
                DlChoiceDialog.this.updateUI();
            }
        }
    };
    private final AdCheckUrlResult mResult;
    private final TextView mTips;

    public interface Callback {
        public static final int CHOICE_CANCEL = 0;
        public static final int CHOICE_DL_APP_MARKET = 1;
        public static final int CHOICE_DL_ORIGIN = 2;
        public static final int CHOICE_UNKNOWN = -1;

        void onChoose(AdCheckUrlResult adCheckUrlResult, int i);
    }

    public DlChoiceDialog(Context context, int themeResId, String downloader, int remain, AdCheckUrlResult result, Callback callback) {
        super(context, themeResId);
        this.mResult = result;
        this.mCallback = callback;
        getWindow().setType(2003);
        setCancelable(false);
        View layout = LayoutInflater.from(context).inflate(R.layout.apk_dl_choice_dialog, null);
        this.mIcon = (ImageView) layout.findViewById(R.id.dl_app_icon);
        this.mIcon.setImageDrawable(HsmPackageManager.getInstance().getDefaultIcon());
        this.mLabel = (TextView) layout.findViewById(R.id.dl_app_name);
        String size = getSizeStr(result.getSize());
        this.mLabel.setText(getContext().getString(R.string.power_battery_history_chart_date_format, new Object[]{result.getApkAppName(), size}));
        this.mLine1 = (TextView) layout.findViewById(R.id.dl_dialog_introduce);
        this.mLine1.setText(getContext().getString(R.string.ad_dl_wait_download_introduce, new Object[]{downloader, result.getApkAppName()}));
        this.mTips = (TextView) layout.findViewById(R.id.dl_dialog_tips);
        this.mTips.setText(result.getTips());
        this.mContinueTips = (TextView) layout.findViewById(R.id.dl_continue_tips);
        setView(layout);
        if (result.getOptPolicy() != 1) {
            this.mContinueTips.setVisibility(0);
            this.mContinueTips.setOnClickListener(this);
            if (result.getOptPolicy() == 3) {
                this.mContinueTips.setText(getContext().getString(R.string.ad_dl_countdown, new Object[]{result.getContinueBtnText(), Integer.valueOf(remain)}));
            } else {
                this.mContinueTips.setText(result.getContinueBtnText());
            }
        }
        if (1 == result.getOptPolicy() || 2 == result.getOptPolicy()) {
            setButton(-2, getContext().getString(R.string.ad_dl_countdown, new Object[]{result.getCancelBtnText(), Integer.valueOf(remain)}), this);
        } else {
            setButton(-2, result.getCancelBtnText(), this);
        }
        setButton(-1, result.getOfficalBtnText(), this);
        setOnDismissListener(this);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(this.mReceiver, new IntentFilter(DlUrlCheckService.SCREEN_ORIENTATION_ACTION));
    }

    private void updateUI() {
        if (this.mContinueTips != null) {
            MarginLayoutParams params = (MarginLayoutParams) this.mContinueTips.getLayoutParams();
            params.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.continue_tips_margin_top);
            this.mContinueTips.setLayoutParams(params);
        }
    }

    private String getSizeStr(long size) {
        if (size <= 0) {
            return "0M";
        }
        String sizeStr;
        DecimalFormat decimalFormat = null;
        if (size > 104857) {
            decimalFormat = new DecimalFormat("###.#");
        } else if (size > 10485) {
            decimalFormat = new DecimalFormat("###.##");
        }
        if (decimalFormat != null) {
            sizeStr = decimalFormat.format((((double) size) / 1024.0d) / 1024.0d) + "M";
        } else {
            sizeStr = "0.01M";
        }
        return sizeStr;
    }

    public void setAppIcon(Drawable drawable) {
        if (isShowing()) {
            this.mIcon.setImageDrawable(drawable);
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                this.mCallback.onChoose(this.mResult, 0);
                return;
            case -1:
                this.mCallback.onChoose(this.mResult, 1);
                return;
            default:
                return;
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.dl_continue_tips) {
            dismiss();
            this.mCallback.onChoose(this.mResult, 2);
        }
    }

    public void setRemainingTime(int time) {
        if (1 == this.mResult.getOptPolicy() || 2 == this.mResult.getOptPolicy()) {
            Button button = getButton(-2);
            if (button != null) {
                button.setText(getContext().getString(R.string.ad_dl_countdown, new Object[]{this.mResult.getCancelBtnText(), Integer.valueOf(time)}));
            }
        } else if (this.mResult.getOptPolicy() == 3 && this.mContinueTips != null && this.mContinueTips.getVisibility() == 0) {
            this.mContinueTips.setText(getContext().getString(R.string.ad_dl_countdown, new Object[]{this.mResult.getContinueBtnText(), Integer.valueOf(time)}));
        }
    }

    public void onDismiss(DialogInterface dialog) {
        LocalBroadcastManager.getInstance(getContext().getApplicationContext()).unregisterReceiver(this.mReceiver);
    }
}
