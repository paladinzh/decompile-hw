package com.android.settings.notification;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import com.android.settings.RestrictedListPreference;
import com.android.settings.RestrictedListPreference.RestrictedArrayAdapter;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class NotificationLockscreenPreference extends RestrictedListPreference {
    private EnforcedAdmin mAdminRestrictingRemoteInput;
    private boolean mAllowRemoteInput;
    private Listener mListener;
    private boolean mRemoteInputCheckBoxEnabled = true;
    private boolean mShowRemoteInput;
    private int mUserId = UserHandle.myUserId();

    private class Listener implements OnClickListener, OnCheckedChangeListener, View.OnClickListener {
        private final OnClickListener mInner;
        private View mView;

        public Listener(OnClickListener inner) {
            this.mInner = inner;
        }

        public void onClick(DialogInterface dialog, int which) {
            this.mInner.onClick(dialog, which);
            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
            if (this.mView != null) {
                this.mView.setVisibility(NotificationLockscreenPreference.this.checkboxVisibilityForSelectedIndex(selectedPosition, NotificationLockscreenPreference.this.mShowRemoteInput));
            }
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            NotificationLockscreenPreference.this.mAllowRemoteInput = !isChecked;
        }

        public void setView(View view) {
            this.mView = view;
        }

        public void onClick(View v) {
            if (v.getId() == 16909091) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(NotificationLockscreenPreference.this.getContext(), NotificationLockscreenPreference.this.mAdminRestrictingRemoteInput);
            }
        }
    }

    public NotificationLockscreenPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRemoteInputCheckBoxEnabled(boolean enabled) {
        this.mRemoteInputCheckBoxEnabled = enabled;
    }

    public void setRemoteInputRestricted(EnforcedAdmin admin) {
        this.mAdminRestrictingRemoteInput = admin;
    }

    protected void onClick() {
        Context context = getContext();
        if (!Utils.startQuietModeDialogIfNecessary(context, UserManager.get(context), this.mUserId)) {
            super.onClick();
        }
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener innerListener) {
        boolean z;
        boolean z2 = true;
        this.mListener = new Listener(innerListener);
        builder.setSingleChoiceItems(createListAdapter(), getSelectedValuePos(), this.mListener);
        if (getEntryValues().length == 3) {
            z = true;
        } else {
            z = false;
        }
        this.mShowRemoteInput = z;
        if (Secure.getInt(getContext().getContentResolver(), "lock_screen_allow_remote_input", 0) == 0) {
            z2 = false;
        }
        this.mAllowRemoteInput = z2;
        builder.setView(2130968858);
    }

    protected void onDialogCreated(Dialog dialog) {
        boolean z;
        int i;
        boolean z2 = true;
        super.onDialogCreated(dialog);
        dialog.create();
        CheckBox checkbox = (CheckBox) dialog.findViewById(2131886771);
        if (this.mAllowRemoteInput) {
            z = false;
        } else {
            z = true;
        }
        checkbox.setChecked(z);
        checkbox.setOnCheckedChangeListener(this.mListener);
        if (this.mAdminRestrictingRemoteInput != null) {
            z2 = false;
        }
        checkbox.setEnabled(z2);
        View restricted = dialog.findViewById(2131886772);
        if (this.mAdminRestrictingRemoteInput == null) {
            i = 8;
        } else {
            i = 0;
        }
        restricted.setVisibility(i);
        if (this.mAdminRestrictingRemoteInput != null) {
            checkbox.setClickable(false);
            dialog.findViewById(16909091).setOnClickListener(this.mListener);
        }
    }

    protected void onDialogStateRestored(Dialog dialog, Bundle savedInstanceState) {
        super.onDialogStateRestored(dialog, savedInstanceState);
        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
        View panel = dialog.findViewById(16909091);
        panel.setVisibility(checkboxVisibilityForSelectedIndex(selectedPosition, this.mShowRemoteInput));
        this.mListener.setView(panel);
    }

    protected ListAdapter createListAdapter() {
        return new RestrictedArrayAdapter(getContext(), getEntries(), -1);
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        Secure.putInt(getContext().getContentResolver(), "lock_screen_allow_remote_input", this.mAllowRemoteInput ? 1 : 0);
    }

    protected boolean isAutoClosePreference() {
        return false;
    }

    private int checkboxVisibilityForSelectedIndex(int selected, boolean showRemoteAtAll) {
        if (selected == 1 && showRemoteAtAll && this.mRemoteInputCheckBoxEnabled) {
            return 0;
        }
        return 8;
    }
}
