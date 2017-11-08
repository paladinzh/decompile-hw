package com.android.contacts.interactions;

import android.preference.Preference;
import android.preference.PreferenceGroup;
import com.android.contacts.util.HwCustContactFeatureUtils;

public class HwCustManageContactsActivityImpl extends HwCustManageContactsActivity {
    public boolean isRemoveShareMenu() {
        return HwCustContactFeatureUtils.isMoveShareContactsToMainMenu();
    }

    public void removeShareMenuPreference(PreferenceGroup aPrefGroup, Preference aPref) {
        if (aPrefGroup != null && aPref != null) {
            aPrefGroup.removePreference(aPref);
        }
    }
}
