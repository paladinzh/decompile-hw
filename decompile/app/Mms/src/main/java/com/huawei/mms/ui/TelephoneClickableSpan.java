package com.huawei.mms.ui;

import android.content.Context;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwCallVideoUtils;
import com.huawei.mms.util.HwCustHwMessageUtils;
import java.util.ArrayList;

public class TelephoneClickableSpan extends HwClickableSpan {
    private long mCId = -1;
    public HwCustHwMessageUtils mCust = ((HwCustHwMessageUtils) HwCustUtils.createObj(HwCustHwMessageUtils.class, new Object[0]));
    private String mName = null;
    private String mNumber = null;

    public TelephoneClickableSpan(Context context, String url) {
        super(context, url, 1);
    }

    protected ArrayList<Integer> getOperations() {
        ArrayList<Integer> menuItems = new ArrayList();
        if (MmsConfig.isVoiceCapable()) {
            if (HwCallVideoUtils.isCallVideoEnabled(this.mContext)) {
                menuItems.add(Integer.valueOf(R.string.clickspan_voice_call));
                menuItems.add(Integer.valueOf(R.string.video_call));
            } else {
                menuItems.add(Integer.valueOf(R.string.clickspan_call));
            }
            menuItems.add(Integer.valueOf(R.string.clickspan_edit_call));
        }
        if (MmsConfig.isSmsEnabled(this.mContext)) {
            menuItems.add(Integer.valueOf(R.string.clickspan_send_message));
        }
        menuItems.add(Integer.valueOf(R.string.clickspan_copy));
        this.mNumber = this.mUrl.substring("tel:".length(), this.mUrl.length());
        if (this.mCust != null && MmsConfig.isVoiceCapable()) {
            this.mCust.removeSendMsgMenu(this.mContext, this.mNumber, menuItems);
        }
        Contact contact = Contact.get(this.mNumber, true);
        this.mCId = contact.getPersonId();
        if (this.mCId > 0) {
            menuItems.add(Integer.valueOf(R.string.clickspan_view_contact));
            this.mName = contact.getName();
        } else {
            menuItems.add(Integer.valueOf(R.string.clickspan_new_contact));
            menuItems.add(Integer.valueOf(R.string.clickspan_save_contact));
        }
        return menuItems;
    }

    protected String getShowingtitle() {
        if (this.mCId > 0) {
            return Contact.formatNumberAndName(this.mNumber, this.mName);
        }
        return this.mNumber;
    }

    protected String getCopiedString() {
        return this.mUrl.substring("tel:".length(), this.mUrl.length());
    }

    protected long getContactId() {
        return this.mCId;
    }
}
