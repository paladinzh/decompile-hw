package com.android.settings;

import android.accounts.Account;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.widget.ImageView;
import java.util.ArrayList;

public class AccountPreference extends Preference {
    private Account mAccount;
    private ArrayList<String> mAuthorities;
    private boolean mShowTypeIcon;
    private int mStatus;
    private ImageView mSyncStatusIcon;

    public AccountPreference(Context context, Account account, Drawable icon, ArrayList<String> authorities, boolean showTypeIcon) {
        super(context);
        setLayoutResource(2130969013);
        this.mAccount = account;
        this.mAuthorities = authorities;
        this.mShowTypeIcon = showTypeIcon;
        if (showTypeIcon) {
            setIcon(icon);
        } else {
            setIcon(getSyncStatusIcon(1));
        }
        setTitle(this.mAccount.name);
        setSummary((CharSequence) "");
        setPersistent(false);
        setSyncStatus(1, false);
        setWidgetLayoutResource(2130968998);
    }

    public Account getAccount() {
        return this.mAccount;
    }

    public ArrayList<String> getAuthorities() {
        return this.mAuthorities;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (!this.mShowTypeIcon) {
            this.mSyncStatusIcon = (ImageView) view.findViewById(16908294);
            this.mSyncStatusIcon.setImageResource(getSyncStatusIcon(this.mStatus));
            this.mSyncStatusIcon.setContentDescription(getSyncContentDescription(this.mStatus));
        }
    }

    public void setSyncStatus(int status, boolean updateSummary) {
        this.mStatus = status;
        if (!(this.mShowTypeIcon || this.mSyncStatusIcon == null)) {
            this.mSyncStatusIcon.setImageResource(getSyncStatusIcon(status));
            this.mSyncStatusIcon.setContentDescription(getSyncContentDescription(this.mStatus));
        }
        if (updateSummary) {
            setSummary(getSyncStatusMessage(status));
        }
    }

    private int getSyncStatusMessage(int status) {
        switch (status) {
            case 0:
                return 2131626233;
            case 1:
                return 2131627560;
            case 2:
                return 2131626235;
            case 3:
                return 2131626237;
            default:
                Log.e("AccountPreference", "Unknown sync status: " + status);
                return 2131626235;
        }
    }

    private int getSyncStatusIcon(int status) {
        switch (status) {
            case 0:
            case 3:
                return 2130838449;
            case 1:
                return 2130838451;
            case 2:
                return 2130838453;
            default:
                Log.e("AccountPreference", "Unknown sync status: " + status);
                return 2130838453;
        }
    }

    private String getSyncContentDescription(int status) {
        switch (status) {
            case 0:
                return getContext().getString(2131626215);
            case 1:
                return getContext().getString(2131626216);
            case 2:
                return getContext().getString(2131626218);
            case 3:
                return getContext().getString(2131626217);
            default:
                Log.e("AccountPreference", "Unknown sync status: " + status);
                return getContext().getString(2131626218);
        }
    }
}
