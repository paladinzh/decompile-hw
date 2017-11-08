package com.android.contacts.hap.rcs.activities;

import android.content.Intent;
import com.android.contacts.hap.EmuiFeatureManager;
import java.util.ArrayList;

public class RcsContactSelectionActivityHelp {
    private boolean isRcsOn = EmuiFeatureManager.isRcsFeatureEnable();

    public Intent getRcsContactIntent(Intent orgIntent, Intent contactIntent) {
        if (!this.isRcsOn) {
            return contactIntent;
        }
        int fromActivity = orgIntent.getIntExtra("from_activity_key", -1);
        int exsitingGroupSize = orgIntent.getIntExtra("member_size_of_exsiting_group", 0);
        ArrayList<String> memberList = orgIntent.getStringArrayListExtra("list_phonenumber_from_forward");
        contactIntent.putExtra("from_activity_key", fromActivity);
        contactIntent.putExtra("member_size_of_exsiting_group", exsitingGroupSize);
        contactIntent.putStringArrayListExtra("list_phonenumber_from_forward", memberList);
        return contactIntent;
    }
}
