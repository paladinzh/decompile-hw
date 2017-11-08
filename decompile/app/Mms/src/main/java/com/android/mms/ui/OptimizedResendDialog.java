package com.android.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import com.android.mms.MmsApp;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor.Radar;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MccMncConfig;
import com.huawei.mms.util.MmsRadarInfoManager;

public class OptimizedResendDialog {
    Context mContext = null;
    private OnClickListener mDoCallCarrier = null;
    private OnClickListener mDoGotoSmscEditor = null;
    private OnClickListener mDoResend = null;
    private MessageItem mMessageItem = null;
    private MmsRadarInfoManager mMmsRadarInfoManager = null;

    private static final class CancelButtonOnClickListener implements OnClickListener {
        private CancelButtonOnClickListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
        }
    }

    public OptimizedResendDialog(Context context, MessageItem msgItem) {
        this.mContext = context;
        this.mMessageItem = msgItem;
        this.mMmsRadarInfoManager = MmsRadarInfoManager.getInstance();
    }

    private String getTitleString() {
        String ret = "";
        switch (this.mMessageItem.mErrorCode) {
            case Place.TYPE_HOSPITAL /*50*/:
            case Place.TYPE_PARK /*69*/:
            case 330:
                return this.mContext.getResources().getString(R.string.notify_send_failed);
            default:
                return ret;
        }
    }

    private String getMessageString() {
        String ret = "";
        switch (this.mMessageItem.mErrorCode) {
            case Place.TYPE_HOSPITAL /*50*/:
            case Place.TYPE_PARK /*69*/:
                return this.mContext.getResources().getString(R.string.notify_smsc_service_disable);
            case 330:
                return this.mContext.getResources().getString(R.string.notify_smsc_empty);
            default:
                return this.mContext.getResources().getString(R.string.mms_resend_content);
        }
    }

    private String getStrBtnPositive() {
        String ret = "";
        switch (this.mMessageItem.mErrorCode) {
            case Place.TYPE_HOSPITAL /*50*/:
            case Place.TYPE_PARK /*69*/:
            case 330:
                return this.mContext.getResources().getString(R.string.cancel_btn);
            default:
                return this.mContext.getResources().getString(R.string.resend);
        }
    }

    private String getStrBtnNegative() {
        String ret = "";
        switch (this.mMessageItem.mErrorCode) {
            case Place.TYPE_HOSPITAL /*50*/:
            case Place.TYPE_PARK /*69*/:
                return this.mContext.getResources().getString(R.string.botton_call_carrier);
            case 330:
                return this.mContext.getResources().getString(R.string.botton_settings);
            default:
                return this.mContext.getResources().getString(R.string.cancel_btn);
        }
    }

    private String getStrBtnNeutral() {
        String ret = "";
        switch (this.mMessageItem.mErrorCode) {
            case Place.TYPE_HOSPITAL /*50*/:
            case Place.TYPE_PARK /*69*/:
            case 330:
                return this.mContext.getResources().getString(R.string.resend);
            default:
                return ret;
        }
    }

    public boolean isNeedUserRepairSelf() {
        if (!this.mMessageItem.isSms()) {
            MLog.d("OptimizedResendDialog", "not sms, return");
            return false;
        } else if (!HwMessageUtils.IS_CHINA_REGION) {
            MLog.d("OptimizedResendDialog", "not in china region, return");
            return false;
        } else if (50 != this.mMessageItem.mErrorCode && 69 != this.mMessageItem.mErrorCode && 330 != this.mMessageItem.mErrorCode) {
            MLog.d("OptimizedResendDialog", "not special errorCode, return");
            return false;
        } else if (HwMessageUtils.isInRoaming()) {
            MLog.d("OptimizedResendDialog", "is in roaming, return");
            return false;
        } else if (330 == this.mMessageItem.mErrorCode && this.mMmsRadarInfoManager.isSmscCorrectNow(this.mMessageItem.mSubId)) {
            MLog.d("OptimizedResendDialog", "smsc is correct now, return");
            return false;
        } else if (this.mMmsRadarInfoManager.isSmsCanSendNow(this.mMessageItem.mSubId)) {
            MLog.d("OptimizedResendDialog", "sms can send success now, return");
            return false;
        } else {
            String mcc = MmsApp.getDefaultMSimTelephonyManager().getSimOperator(this.mMessageItem.mSubId);
            boolean isChinaUnicomOperator = !MccMncConfig.isChinaMobieOperator(mcc) ? MccMncConfig.isChinaUnicomOperator(mcc) : true;
            MLog.d("OptimizedResendDialog", "is need user repair self ret:" + isChinaUnicomOperator + " mcc:" + mcc + " errorCode:" + this.mMessageItem.mErrorCode + " sub:" + this.mMessageItem.mSubId);
            return isChinaUnicomOperator;
        }
    }

    public void callCarrier() {
        String mcc = MmsApp.getDefaultMSimTelephonyManager().getSimOperator(this.mMessageItem.mSubId);
        if (MccMncConfig.isChinaMobieOperator(mcc)) {
            MLog.d("OptimizedResendDialog", "call china mobile hotline");
            HwMessageUtils.dialNumberBySubscription(this.mContext, "tel:10086", this.mMessageItem.mSubId);
            Radar.reportChr(this.mMessageItem.mSubId, 1311, "call cmcc: " + this.mMessageItem.mErrorCode);
        } else if (MccMncConfig.isChinaUnicomOperator(mcc)) {
            MLog.d("OptimizedResendDialog", "call china unicom hotline");
            HwMessageUtils.dialNumberBySubscription(this.mContext, "tel:10010", this.mMessageItem.mSubId);
            Radar.reportChr(this.mMessageItem.mSubId, 1311, "call unicom: " + this.mMessageItem.mErrorCode);
        } else {
            MLog.e("OptimizedResendDialog", "other carrier or no service entry, do nothing: " + mcc);
        }
    }

    public void goHwSmscEditorActivity() {
        Intent intent = new Intent(this.mContext, HwSmsCenterNumberEditerActivity.class);
        intent.putExtra("intent_key_crad_sub_id", this.mMessageItem.mSubId);
        intent.putExtra("intent_key_mutil_mode", HwMessageUtils.getMultiSimState());
        try {
            this.mContext.startActivity(intent);
            Radar.reportChr(this.mMessageItem.mSubId, 1311, "go smsc editor");
        } catch (ActivityNotFoundException aE) {
            MLog.e("OptimizedResendDialog", "[error] >>>" + aE);
        }
    }

    private OnClickListener getListenerBtnNegative() {
        switch (this.mMessageItem.mErrorCode) {
            case Place.TYPE_HOSPITAL /*50*/:
            case Place.TYPE_PARK /*69*/:
                return this.mDoCallCarrier;
            case 330:
                return this.mDoGotoSmscEditor;
            default:
                return new CancelButtonOnClickListener();
        }
    }

    private OnClickListener getListenerBtnPositive() {
        switch (this.mMessageItem.mErrorCode) {
            case Place.TYPE_HOSPITAL /*50*/:
            case Place.TYPE_PARK /*69*/:
            case 330:
                return new CancelButtonOnClickListener();
            default:
                return this.mDoResend;
        }
    }

    private OnClickListener getListenerBtnNeutral() {
        switch (this.mMessageItem.mErrorCode) {
            case Place.TYPE_HOSPITAL /*50*/:
            case Place.TYPE_PARK /*69*/:
            case 330:
                return this.mDoResend;
            default:
                return null;
        }
    }

    public AlertDialog getOptResendDailog(OnClickListener doCallCarrier, OnClickListener doResend, OnClickListener doGotoSmscEditor) {
        MLog.d("OptimizedResendDialog", "show special dialog to induce user repart self");
        this.mDoCallCarrier = doCallCarrier;
        this.mDoResend = doResend;
        this.mDoGotoSmscEditor = doGotoSmscEditor;
        return new Builder(this.mContext).setCancelable(true).setTitle(getTitleString()).setMessage(getMessageString()).setPositiveButton(getStrBtnPositive(), getListenerBtnPositive()).setNegativeButton(getStrBtnNegative(), getListenerBtnNegative()).setNeutralButton(getStrBtnNeutral(), getListenerBtnNeutral()).create();
    }
}
