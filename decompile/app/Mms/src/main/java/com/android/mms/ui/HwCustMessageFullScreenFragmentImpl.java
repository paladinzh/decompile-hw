package com.android.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.telephony.SmsMessage;
import com.amap.api.services.core.AMapException;
import com.android.mms.HwCustMmsConfigImpl;
import com.google.android.gms.R;
import com.huawei.mms.ui.EditTextWithSmiley;

public class HwCustMessageFullScreenFragmentImpl extends HwCustMessageFullScreenFragment {
    private AlertDialog mAlertPageLimitDialog;
    private Context mContext;

    public HwCustMessageFullScreenFragmentImpl(Context context) {
        this.mContext = context;
    }

    public void setOnePageSmsText(CharSequence smsText, EditTextWithSmiley mDataEditor) {
        if (HwCustMmsConfigImpl.getEnableAlertLongSms() && getSmsSegmentCount(smsText) > 1) {
            alertForSmsTooLong();
            CharSequence s = getSmsInOnePage(smsText);
            mDataEditor.setText(s);
            mDataEditor.setSelection(s.length());
        }
    }

    private CharSequence getSmsInOnePage(CharSequence s) {
        CharSequence textOrigin = s;
        CharSequence text = s;
        int i = 0;
        while (true) {
            int i2 = i + 1;
            if (i > s.length() || SmsMessage.calculateLength(text, false)[0] <= 1) {
                return text;
            }
            text = s.subSequence(0, s.length() - i2);
            i = i2;
        }
        return text;
    }

    private int getSmsSegmentCount(CharSequence s) {
        return SmsMessage.calculateLength(s, false)[0];
    }

    private void alertForSmsTooLong() {
        if (this.mAlertPageLimitDialog == null) {
            this.mAlertPageLimitDialog = new Builder(this.mContext).setIconAttribute(16843605).setTitle(R.string.sms_too_long_title).setMessage(R.string.sms_too_long).setPositiveButton(R.string.yes, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    HwCustMessageFullScreenFragmentImpl.this.mAlertPageLimitDialog = null;
                }
            }).setCancelable(true).create();
            this.mAlertPageLimitDialog.getWindow().setType(AMapException.CODE_AMAP_ENGINE_TABLEID_NOT_EXIST);
        }
        this.mAlertPageLimitDialog.show();
    }
}
