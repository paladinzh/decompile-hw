package com.android.contacts.interactions;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.contacts.ContactSaveService;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.group.GroupDetailFragment.Listener;
import com.android.contacts.hap.CommonUtilMethods;
import com.google.android.gms.R;

public class GroupDeletionDialogFragment extends DialogFragment {
    private Listener mListener;

    public static void show(FragmentManager fragmentManager, long groupId, String label, boolean endActivity, Listener mGroupDeletionListener) {
        GroupDeletionDialogFragment dialog = new GroupDeletionDialogFragment();
        Bundle args = new Bundle();
        args.putLong("groupId", groupId);
        args.putString("label", label);
        args.putBoolean("endActivity", endActivity);
        dialog.setArguments(args);
        dialog.mListener = mGroupDeletionListener;
        CommonUtilMethods.showFragment(fragmentManager, dialog, "deleteGroup");
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String label = getArguments().getString("label");
        String message = "";
        String title = "";
        if (getActivity() != null) {
            message = getActivity().getString(R.string.delete_group_dialog_message_41);
            title = getActivity().getString(R.string.delete_group_dialog_title_41, new Object[]{label});
        }
        Builder builder = new Builder(getActivity()).setIconAttribute(16843605).setTitle(title).setPositiveButton(getString(R.string.menu_deleteContact), new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                GroupDeletionDialogFragment.this.deleteGroup();
            }
        }).setNegativeButton(17039360, null);
        if (!isAdded() || getActivity() == null) {
            builder.setMessage(message);
        } else {
            View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
            ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(message);
            builder.setView(view);
        }
        final AlertDialog aAlertDialog = builder.create();
        aAlertDialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                Button aPositiveButton = aAlertDialog.getButton(-1);
                if (aPositiveButton != null && GroupDeletionDialogFragment.this.getActivity() != null) {
                    aPositiveButton.setTextColor(GroupDeletionDialogFragment.this.getActivity().getResources().getColor(R.color.delete_text_color));
                }
            }
        });
        if (getActivity() instanceof PeopleActivity) {
            ((PeopleActivity) getActivity()).mGlobalDialogReference = aAlertDialog;
        }
        return aAlertDialog;
    }

    protected void deleteGroup() {
        getActivity().startService(ContactSaveService.createGroupDeletionIntent(getActivity(), getArguments().getLong("groupId")));
        if (this.mListener != null) {
            this.mListener.onGroupDeleted();
        }
        if (shouldEndActivity()) {
            getActivity().finish();
        }
    }

    private boolean shouldEndActivity() {
        return getArguments().getBoolean("endActivity");
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() != null && (getActivity() instanceof PeopleActivity)) {
            ((PeopleActivity) getActivity()).mGlobalDialogReference = null;
        }
    }
}
