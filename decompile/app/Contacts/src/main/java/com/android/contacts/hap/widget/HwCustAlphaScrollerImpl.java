package com.android.contacts.hap.widget;

import android.content.Context;
import android.provider.SettingsEx.Systemex;
import com.google.android.gms.R;
import java.util.Locale;

public class HwCustAlphaScrollerImpl extends HwCustAlphaScroller {
    private Context mContext;

    public HwCustAlphaScrollerImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean isCustAlphabeticIndexer() {
        if (this.mContext != null) {
            return "true".equals(Systemex.getString(this.mContext.getContentResolver(), "hw_cust_alphabet_indexer"));
        }
        return false;
    }

    public String[] getCustAlphabeticIndexer(String[] alphabet) {
        if ("hu".equals(Locale.getDefault().getLanguage())) {
            return this.mContext.getResources().getStringArray(R.array.cust_alphabet_indexer);
        }
        return alphabet;
    }

    public String[] getCustFullAlphabeticIndexer(String[] fullAlphabet) {
        if ("hu".equals(Locale.getDefault().getLanguage())) {
            return this.mContext.getResources().getStringArray(R.array.cust_full_alphabetic_indexer);
        }
        return fullAlphabet;
    }
}
