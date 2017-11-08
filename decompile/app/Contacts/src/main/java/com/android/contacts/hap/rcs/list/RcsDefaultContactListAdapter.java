package com.android.contacts.hap.rcs.list;

import android.content.Context;
import android.database.Cursor;
import android.widget.ImageView;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.list.ContactListItemView;
import com.google.android.gms.R;
import com.huawei.rcs.capability.CapabilityService;

public class RcsDefaultContactListAdapter {
    private Context mContext;
    private CapabilityService mRCS;

    public RcsDefaultContactListAdapter(Context context) {
        this.mContext = context;
        this.mRCS = CapabilityService.getInstance("contacts");
        if (this.mRCS != null) {
            this.mRCS.checkRcsServiceBind();
        }
    }

    public void bindCustomizationsInfo(ContactListItemView view, Cursor cursor) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && RcsContactsUtils.isRCSContactIconEnable()) {
            CapabilityService mRCS = CapabilityService.getInstance("contacts");
            if (!(this.mContext.getResources().getBoolean(R.bool.show_account_icons) || view.getRcsCust() == null)) {
                ImageView rcsView = view.getRcsCust().getRCSView(view);
                if (rcsView != null) {
                    rcsView.setImageResource(R.drawable.rcs_contacts_notif_on_icon);
                    if (RcsContactEntryListFragment.getScrollState()) {
                        rcsView.setVisibility(8);
                    } else if (mRCS.isRCSUeserByContactId(cursor.getLong(0))) {
                        rcsView.setVisibility(0);
                    } else {
                        rcsView.setVisibility(8);
                    }
                }
            }
        }
    }
}
