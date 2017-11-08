package com.android.contacts.list;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.Settings.System;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.utils.EasContactsCache;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustContactListCustomizationsImpl extends HwCustContactListCustomizations {
    private static final int CUST_ONLY_PHONE_CONTACTS_VALUE = 1;
    private static final int DEFAULT_ONLY_PHONE_CONTACTS_VALUE = -1;
    private static final String PREF_DISPLAY_ONLY_PHONES = "only_phones";

    public void handleCustOnLoadFinished(Context aContext) {
        if (HwCustCommonConstants.EAS_ACCOUNT_ICON_DISP_EMABLED) {
            EasContactsCache.getInstance(aContext).refresh();
        }
    }

    public void handleAccountIconCustomizations(Cursor aCursor, ContactListItemView aView, Context aContext) {
        if (HwCustCommonConstants.EAS_ACCOUNT_ICON_DISP_EMABLED && !aCursor.isNull(0)) {
            if (EasContactsCache.getInstance(aContext.getApplicationContext()).isEasContact(aCursor.getLong(0))) {
                if (EasContactsCache.getEasSmallIcon(aContext) != null) {
                    aView.setAccountIcons(new Bitmap[]{EasContactsCache.getEasSmallIcon(aContext)});
                    return;
                }
                aView.setAccountIcons(null);
            }
        }
    }

    public boolean activeOnlyPhoneContactsValue(Context aContext) {
        if (aContext != null) {
            return "true".equals(Systemex.getString(aContext.getContentResolver(), "active_option_number_contact_only"));
        }
        return false;
    }

    public void setOnlyPhoneContactsValue(Context aContext) {
        try {
            if (-1 == System.getInt(aContext.getContentResolver(), PREF_DISPLAY_ONLY_PHONES, -1)) {
                Editor editor = aContext.getSharedPreferences("com.android.contacts_preferences", 0).edit();
                editor.putBoolean("preference_contacts_only_phonenumber", true);
                editor.commit();
                System.putInt(aContext.getContentResolver(), PREF_DISPLAY_ONLY_PHONES, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
