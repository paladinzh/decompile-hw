package com.android.contacts.calllog;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.google.android.gms.R;

public class PlayCallRecordDialogfragment extends DialogFragment {
    public static PlayCallRecordDialogfragment newInstance(String[] itemName, String[] itemPath) {
        PlayCallRecordDialogfragment dialog = new PlayCallRecordDialogfragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray("item_name", itemName);
        bundle.putStringArray("item_path", itemPath);
        dialog.setArguments(bundle);
        return dialog;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] nameSimpleArray = getArguments().getStringArray("item_name");
        final String[] pathArray = getArguments().getStringArray("item_path");
        String[] nameArray = new String[nameSimpleArray.length];
        for (int i = 0; i < nameArray.length; i++) {
            nameArray[i] = getString(R.string.call_record) + HwCustPreloadContacts.EMPTY_STRING + nameSimpleArray[i];
        }
        Builder builder = new Builder(getContext());
        builder.setTitle(getString(R.string.select_record)).setItems(nameArray, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CallLogDetailHelper.startRecordPlaybackSafely(PlayCallRecordDialogfragment.this.getActivity(), pathArray[which]);
            }
        });
        return builder.create();
    }

    public static void playCallRecord(FragmentManager fragmentManager, String[] itemName, String[] itemPath) {
        newInstance(itemName, itemPath).show(fragmentManager, "PlayCallRecord");
    }
}
