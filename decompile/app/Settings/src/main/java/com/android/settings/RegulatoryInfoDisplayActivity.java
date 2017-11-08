package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.cust.HwCustUtils;

public class RegulatoryInfoDisplayActivity extends Activity implements OnDismissListener {
    private HwCustRegulatoryInfoDisplay mCust;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources resources = getResources();
        this.mCust = (HwCustRegulatoryInfoDisplay) HwCustUtils.createObj(HwCustRegulatoryInfoDisplay.class, new Object[]{this});
        if (!resources.getBoolean(2131492878)) {
            finish();
        }
        Builder builder = new Builder(this).setTitle(2131625506).setOnDismissListener(this);
        if (this.mCust == null || !this.mCust.isImageFlagExits()) {
            boolean regulatoryInfoDrawableExists = false;
            int resId = getResourceId();
            if (resId != 0) {
                try {
                    Drawable d = getDrawable(resId);
                    regulatoryInfoDrawableExists = d.getIntrinsicWidth() > 2 ? d.getIntrinsicHeight() > 2 : false;
                } catch (NotFoundException e) {
                    regulatoryInfoDrawableExists = false;
                }
            }
            CharSequence regulatoryText = resources.getText(2131626597);
            if (regulatoryInfoDrawableExists) {
                View view = getLayoutInflater().inflate(2130969048, null);
                ((ImageView) view.findViewById(2131887073)).setImageResource(resId);
                builder.setView(view);
                builder.show();
            } else if (regulatoryText.length() > 0) {
                builder.setMessage(regulatoryText);
                ((TextView) builder.show().findViewById(16908299)).setGravity(17);
            } else {
                finish();
            }
            return;
        }
        this.mCust.setRegulatoryInfoImages(builder, getLayoutInflater().inflate(2130969048, null));
    }

    private int getResourceId() {
        int resId = getResources().getIdentifier("regulatory_info", "drawable", getPackageName());
        String sku = SystemProperties.get("ro.boot.hardware.sku", "");
        if (TextUtils.isEmpty(sku)) {
            return resId;
        }
        int id = getResources().getIdentifier("regulatory_info_" + sku.toLowerCase(), "drawable", getPackageName());
        if (id != 0) {
            return id;
        }
        return resId;
    }

    public void onDismiss(DialogInterface dialog) {
        finish();
    }
}
