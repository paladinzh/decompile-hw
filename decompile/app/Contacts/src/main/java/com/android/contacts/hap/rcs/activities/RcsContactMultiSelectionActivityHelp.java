package com.android.contacts.hap.rcs.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import com.android.contacts.hap.list.ContactDataMultiSelectFragment;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.google.android.gms.location.places.Place;
import java.util.ArrayList;
import java.util.HashMap;

public class RcsContactMultiSelectionActivityHelp {
    private boolean isRcsOn = EmuiFeatureManager.isRcsFeatureEnable();
    private int mExsitingGroupSize;
    private int mFromActivity;
    private ArrayList<String> mMemberListFromForward;
    private Object mSelectedDataUriLock = new Object();
    private HashMap<String, String> mSelectedPersonMap = new HashMap();

    public HashMap<String, String> getSelectedPersonMap() {
        return this.isRcsOn ? this.mSelectedPersonMap : null;
    }

    public void setSelectedPersonMap(HashMap<String, String> map) {
        if (this.isRcsOn) {
            this.mSelectedPersonMap = map;
        }
    }

    public void clearSelectedPersonMap() {
        if (this.isRcsOn && this.mSelectedPersonMap != null) {
            this.mSelectedPersonMap.clear();
            this.mSelectedPersonMap = null;
        }
    }

    public boolean configureListFragment(Activity activity, int actionCode) {
        if (!this.isRcsOn) {
            return false;
        }
        switch (actionCode) {
            case Place.TYPE_MEAL_DELIVERY /*60*/:
                ((ContactMultiSelectionActivity) activity).mMultiSelectFragment = (ContactDataMultiSelectFragment) ((ContactMultiSelectionActivity) activity).getFragmentToLoad();
                return true;
            default:
                return false;
        }
    }

    public void addUserToMemberList(Context context, Intent intent) {
        if (this.isRcsOn) {
            this.mFromActivity = intent.getIntExtra("from_activity_key", -1);
            if (RcsContactsUtils.isValidFromActivity(this.mFromActivity)) {
                this.mExsitingGroupSize = intent.getIntExtra("member_size_of_exsiting_group", 0);
                this.mMemberListFromForward = intent.getStringArrayListExtra("list_phonenumber_from_forward");
                if (this.mMemberListFromForward == null) {
                    this.mMemberListFromForward = new ArrayList();
                }
                String curLoginUserNumber = RcsContactsUtils.getCurrentUserNumber();
                if (curLoginUserNumber != null) {
                    this.mMemberListFromForward.add(PhoneNumberUtils.normalizeNumber(curLoginUserNumber));
                }
            }
        }
    }

    public int getFromActivity() {
        if (this.isRcsOn) {
            return this.mFromActivity;
        }
        return -1;
    }

    public ArrayList<String> getMemberListFromForward() {
        if (this.isRcsOn) {
            return this.mMemberListFromForward;
        }
        return null;
    }

    public int getExsitedCallLogCount(ContactMultiSelectionActivity activity) {
        if (!this.isRcsOn || activity == null) {
            return 0;
        }
        int count = 0;
        for (Uri uri : activity.mSelectedDataUris) {
            if (uri.toString().startsWith("content://call_log/calls")) {
                count++;
            }
        }
        return count;
    }
}
