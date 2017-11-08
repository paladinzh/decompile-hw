package com.android.contacts.preference;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustContactsPreferencesImpl extends HwCustContactsPreferences {
    public HwCustContactsPreferencesImpl(Context context) {
        super(context);
        this.changeSortByLang = Systemex.getString(this.mContext.getContentResolver(), "change_sort_by_lang");
    }

    public boolean isChangeSortByLang() {
        if (TextUtils.isEmpty(this.changeSortByLang)) {
            return false;
        }
        return true;
    }

    public int getOrderByLang(int oldOrder, int i) {
        int newOrder = oldOrder;
        Configuration configuration = this.mContext.getResources().getConfiguration();
        String strLang = configuration.locale.getLanguage() + "_" + configuration.locale.getCountry();
        if (this.changeSortByLang.contains(strLang)) {
            String[] SortByLangArray = this.changeSortByLang.split(";");
            int i2 = 0;
            int length = SortByLangArray.length;
            while (i2 < length) {
                String lang = SortByLangArray[i2];
                if (lang.contains(strLang)) {
                    int newOrderTmp = Integer.parseInt(lang.split(":")[1].split(",")[i]);
                    if (1 != newOrderTmp && 2 != newOrderTmp) {
                        return oldOrder;
                    }
                    newOrder = newOrderTmp;
                } else {
                    i2++;
                }
            }
        }
        return newOrder;
    }
}
