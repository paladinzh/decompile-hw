package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.android.internal.widget.LockPatternUtils;

public class OwnerInfoSettings extends DialogFragment implements OnClickListener {
    private LockPatternUtils mLockPatternUtils;
    private EditText mOwnerInfo;
    private int mUserId;
    private View mView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUserId = UserHandle.myUserId();
        this.mLockPatternUtils = new LockPatternUtils(getActivity());
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.mView = LayoutInflater.from(getActivity()).inflate(2130968894, null);
        initView();
        Dialog dialog = new Builder(getActivity()).setTitle(2131624625).setView(this.mView).setPositiveButton(2131624575, this).setNegativeButton(2131624572, this).show();
        dialog.getWindow().setSoftInputMode(5);
        return dialog;
    }

    private void initView() {
        String info = this.mLockPatternUtils.getOwnerInfo(this.mUserId);
        this.mOwnerInfo = (EditText) this.mView.findViewById(2131886845);
        if (!TextUtils.isEmpty(info)) {
            this.mOwnerInfo.setText(info);
            this.mOwnerInfo.setSelection(info.length());
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            if (!TextUtils.isEmpty(this.mOwnerInfo.getText().toString())) {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "owner_info_set");
                ItemUseStat.getInstance().cacheData(getActivity());
            }
            String info = this.mOwnerInfo.getText().toString();
            this.mLockPatternUtils.setOwnerInfoEnabled(!TextUtils.isEmpty(info), this.mUserId);
            this.mLockPatternUtils.setOwnerInfo(info, this.mUserId);
            if (getTargetFragment() instanceof ScreenLockSettings) {
                ((ScreenLockSettings) getTargetFragment()).updateOwnerInfo();
            }
        }
    }

    public static void show(Fragment parent) {
        if (parent.isAdded()) {
            OwnerInfoSettings dialog = new OwnerInfoSettings();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), "ownerInfo");
        }
    }
}
