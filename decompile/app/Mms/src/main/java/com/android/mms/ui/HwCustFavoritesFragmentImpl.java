package com.android.mms.ui;

import android.app.Fragment;
import com.android.mms.util.HwCustUiUtils;

public class HwCustFavoritesFragmentImpl extends HwCustFavoritesFragment {
    public HwCustFavoritesFragmentImpl(Fragment fragment) {
        super(fragment);
    }

    public String updateForwardSubject(String aFwdSubject, String aMsgSubject) {
        return HwCustUiUtils.updateForwardSubject(aFwdSubject, aMsgSubject);
    }
}
