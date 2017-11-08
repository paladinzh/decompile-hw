package com.android.contacts.interactions;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.widget.EditText;
import com.android.contacts.ContactSaveService;
import com.android.contacts.model.account.AccountWithDataSet;
import com.google.android.gms.R;

public class GroupCreationDialogFragment extends GroupNameDialogFragment {
    private final OnGroupCreatedListener mListener;

    public interface OnGroupCreatedListener {
        void onGroupCreated(String str);
    }

    public static void show(FragmentManager fragmentManager, AccountWithDataSet accountWithData, OnGroupCreatedListener listener) {
        GroupCreationDialogFragment dialog = new GroupCreationDialogFragment(listener);
        Bundle args = new Bundle();
        args.putString("accountType", accountWithData.type);
        args.putString("accountName", accountWithData.name);
        args.putString("dataSet", accountWithData.dataSet);
        dialog.setArguments(args);
        dialog.show(fragmentManager, "createGroupDialog");
    }

    public GroupCreationDialogFragment() {
        this.mListener = null;
    }

    private GroupCreationDialogFragment(OnGroupCreatedListener listener) {
        this.mListener = listener;
    }

    protected void initializeGroupLabelEditText(EditText editText) {
    }

    protected int getTitleResourceId() {
        return R.string.create_group_dialog_title;
    }

    protected void onCompleted(String groupLabel) {
        Bundle arguments = getArguments();
        String accountType = arguments.getString("accountType");
        String accountName = arguments.getString("accountName");
        String dataSet = arguments.getString("dataSet");
        if (this.mListener != null) {
            this.mListener.onGroupCreated(groupLabel);
        }
        Activity activity = getActivity();
        activity.startService(ContactSaveService.createNewGroupIntent(activity, new AccountWithDataSet(accountName, accountType, dataSet), groupLabel, null, activity.getClass(), "android.intent.action.EDIT"));
    }
}
