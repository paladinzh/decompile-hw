package com.android.contacts.hap.list;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Parcel;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.util.AlertDialogFragmet;
import com.android.contacts.hap.util.AlertDialogFragmet.OnDialogOptionSelectListener;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustContactDataMultiSelectFragmentImpl extends HwCustContactDataMultiSelectFragment {
    protected static final String TAG = "HwCustContactDataMultiSelectFragmentImpl";

    private static class RemoveConfirmDialogListener implements OnDialogOptionSelectListener {
        private ContactDataMultiSelectFragment mContactDataMultiSelectFragment;

        public RemoveConfirmDialogListener(ContactDataMultiSelectFragment aContactDataMultiSelectFragment) {
            this.mContactDataMultiSelectFragment = aContactDataMultiSelectFragment;
        }

        public void onDialogOptionSelected(int which, Context aContext) {
            if (which == -1) {
                this.mContactDataMultiSelectFragment.startRemoveService();
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
        }
    }

    public HwCustContactDataMultiSelectFragmentImpl(Context context) {
        super(context);
    }

    public boolean handleRemoveGrpMemOperationCust(ContactDataMultiSelectFragment aFragment, FragmentManager aFragmentManager) {
        if (HwCustCommonConstants.IS_AAB_ATT) {
            AlertDialogFragmet.show(aFragmentManager, R.string.str_remove_from_group, this.mContext.getString(R.string.str_removegrp_members_warning), R.string.str_removegrp_members_warning, true, new RemoveConfirmDialogListener(aFragment), 16843605, R.string.remove_button_label);
            return true;
        }
        super.handleRemoveGrpMemOperationCust(aFragment, aFragmentManager);
        return false;
    }

    public boolean getEnableEmailContactInMms() {
        if ("true".equals(Systemex.getString(this.mContext.getContentResolver(), "enable_email_contact_in_mms"))) {
            return HwCustContactFeatureUtils.isBindOnlyNumberSwitch(this.mContext);
        }
        return false;
    }

    public int getPickEmailNumber(boolean isSearchMode) {
        if (isSearchMode) {
            return R.string.no_matching_contacts_with_number_email;
        }
        return R.string.contact_noPhoneNumbersOrEmails;
    }
}
