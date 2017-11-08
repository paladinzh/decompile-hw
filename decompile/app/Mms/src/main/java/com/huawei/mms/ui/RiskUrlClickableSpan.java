package com.huawei.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.mms.data.Contact;
import com.google.android.gms.R;
import com.huawei.mms.util.StatisticalHelper;

public class RiskUrlClickableSpan extends BrowserClickableSpan {
    Context mContext;

    public RiskUrlClickableSpan(Context context, String url, CharSequence sequence) {
        super(context, url, sequence);
        this.mContext = context;
    }

    public void processOpenBrow(final String url, final View widget) {
        StatisticalHelper.incrementReportCount(this.mContext, 2161);
        AlertDialog dialog = new Builder(this.mContext).setTitle(R.string.risk_url_dialog_title).setPositiveButton(R.string.risk_url_dialog_ok_btn, new OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                StatisticalHelper.incrementReportCount(RiskUrlClickableSpan.this.mContext, 2162);
                RiskUrlClickableSpan.this.openBrow(url, widget);
            }
        }).setNegativeButton(R.string.risk_url_dialog_cancel_btn, null).setCancelable(false).create();
        View layout = LayoutInflater.from(this.mContext).inflate(R.layout.risk_url_dialog, null);
        TextView declare = (TextView) layout.findViewById(R.id.tv_risk_url_dialog_declare);
        if (!Contact.IS_CHINA_REGION) {
            declare.setText(R.string.risk_url_dialog_declare_oversea);
        }
        dialog.setView(layout);
        dialog.show();
    }
}
