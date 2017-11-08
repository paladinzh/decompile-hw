package com.android.contacts.detail;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.android.contacts.activities.ContactInfoFragment;
import com.google.android.gms.R;

public class UnJoinContacesDialogFragment extends DialogFragment {
    public static UnJoinContacesDialogFragment newInstance() {
        return new UnJoinContacesDialogFragment();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new Builder(getActivity()).setMessage(R.string.contact_detail_separate_title).setPositiveButton(R.string.contact_detail_separate_button_text, new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int whichButton) {
                Fragment fragment = UnJoinContacesDialogFragment.this.getTargetFragment();
                if (fragment != null && (fragment instanceof ContactInfoFragment)) {
                    ((ContactInfoFragment) fragment).unJoinContacts();
                }
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                UnJoinContacesDialogFragment.this.dismiss();
            }
        }).create();
        dialog.setMessageNotScrolling();
        return dialog;
    }

    public static void unJoinContacts(FragmentManager fragmentManager, Fragment target) {
        UnJoinContacesDialogFragment dialog = newInstance();
        dialog.setTargetFragment(target, 0);
        dialog.show(fragmentManager, "SeperateContacts");
    }
}
