package com.huawei.mms.ui;

import com.android.mms.ui.EmuiSwitchPreference;
import com.google.android.gms.R;
import com.huawei.mms.util.HwCustSmartArchiveSettingUtilsImpl;

public class HwCustSmartArchiveSettingsImpl extends HwCustSmartArchiveSettings {
    public void addPreferenceSummaryForServiceMessage(EmuiSwitchPreference aPref, String aKey) {
        if (aPref != null && aKey != null && HwCustSmartArchiveSettingUtilsImpl.SERVICE_MESSAGE_ARCHIVAL_ENABLED && aKey.equals(HwCustSmartArchiveSettingUtilsImpl.PREF_KEY_ARCHIVE_NUM_SERVICE_MESSAGE)) {
            aPref.setSummary(R.string.archive_num_bak_summary);
        }
    }
}
