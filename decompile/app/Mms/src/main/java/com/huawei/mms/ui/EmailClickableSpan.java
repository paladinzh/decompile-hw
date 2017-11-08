package com.huawei.mms.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.text.TextUtils;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.google.android.gms.R;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.util.HwMessageUtils;
import java.util.ArrayList;

public class EmailClickableSpan extends HwClickableSpan {
    private long mCId = 0;
    private String mEmail = null;
    private String mName = null;

    public EmailClickableSpan(Context context, String url, CharSequence sequence) {
        super(context, url, sequence, 0);
    }

    protected ArrayList<Integer> getOperations() {
        ArrayList<Integer> menuItems = new ArrayList();
        if (MmsConfig.getEnableEmailSpanAsMmsRecipient()) {
            menuItems.add(Integer.valueOf(R.string.clickspan_send_message));
        }
        menuItems.add(Integer.valueOf(R.string.clickspan_send_email));
        menuItems.add(Integer.valueOf(R.string.clickspan_copy));
        this.mEmail = this.mUrl.substring("mailto:".length(), this.mUrl.length());
        loadEmailInfo();
        if (this.mCId > 0) {
            menuItems.add(Integer.valueOf(R.string.clickspan_view_contact));
        } else {
            menuItems.add(Integer.valueOf(R.string.clickspan_new_contact));
            menuItems.add(Integer.valueOf(R.string.clickspan_save_contact));
        }
        return menuItems;
    }

    private void loadEmailInfo() {
        Cursor cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(this.mEmail)), new String[]{"contact_id", "display_name"}, null, null, null);
        if (cursor != null) {
            boolean isExistInContacts = false;
            while (cursor.moveToNext()) {
                try {
                    this.mName = cursor.getString(1);
                    if (!TextUtils.isEmpty(this.mName)) {
                        this.mCId = cursor.getLong(0);
                        isExistInContacts = true;
                    }
                } catch (Throwable th) {
                    cursor.close();
                }
            }
            if (!isExistInContacts) {
                this.mCId = 0;
            }
            cursor.close();
        }
    }

    protected String getShowingtitle() {
        if (this.mCId > 0) {
            return Contact.formatNumberAndName(this.mEmail, this.mName);
        }
        return this.mEmail;
    }

    protected String getCopiedString() {
        return HwMessageUtils.copyUrl("mailto:", this.mUrl, this.mBodyText);
    }

    protected long getContactId() {
        return this.mCId;
    }
}
