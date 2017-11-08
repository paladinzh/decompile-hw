package com.android.contacts.common.vcard;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.google.android.gms.R;

public class ImportVCardDialogFragment extends DialogFragment {

    public interface Listener {
        void onImportVCardConfirmed(int i);

        void onImportVCardDenied();
    }

    public static void show(Activity activity, int resId) {
        if (activity instanceof Listener) {
            Bundle args = new Bundle();
            args.putInt("sourceId", resId);
            ImportVCardDialogFragment dialog = new ImportVCardDialogFragment();
            dialog.setArguments(args);
            dialog.show(activity.getFragmentManager(), "importVCardDialog");
            return;
        }
        throw new IllegalArgumentException("Activity must implement " + Listener.class.getName());
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int resId = getArguments().getInt("sourceId");
        return new Builder(getActivity()).setIconAttribute(16843605).setMessage(R.string.import_from_vcf_file_confirmation_message).setPositiveButton(17039379, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Listener listener = (Listener) ImportVCardDialogFragment.this.getActivity();
                if (listener != null) {
                    listener.onImportVCardConfirmed(resId);
                }
            }
        }).setNegativeButton(17039369, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Listener listener = (Listener) ImportVCardDialogFragment.this.getActivity();
                if (listener != null) {
                    listener.onImportVCardDenied();
                }
            }
        }).create();
    }
}
