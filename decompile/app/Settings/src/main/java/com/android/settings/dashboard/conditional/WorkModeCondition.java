package com.android.settings.dashboard.conditional;

import android.content.Intent;
import android.content.pm.UserInfo;
import android.graphics.drawable.Icon;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.settings.ItemUseStat;
import com.android.settings.Settings.AccountSettingsActivity;
import java.util.List;

public class WorkModeCondition extends Condition {
    private UserManager mUm = ((UserManager) this.mManager.getContext().getSystemService("user"));
    private UserHandle mUserHandle;

    public WorkModeCondition(ConditionManager conditionManager) {
        super(conditionManager);
    }

    private void updateUserHandle() {
        List<UserInfo> profiles = this.mUm.getProfiles(UserHandle.myUserId());
        int profilesCount = profiles.size();
        this.mUserHandle = null;
        for (int i = 0; i < profilesCount; i++) {
            UserInfo userInfo = (UserInfo) profiles.get(i);
            if (userInfo.isManagedProfile()) {
                this.mUserHandle = userInfo.getUserHandle();
                return;
            }
        }
    }

    public void refreshState() {
        updateUserHandle();
        setActive(this.mUserHandle != null ? this.mUm.isQuietModeEnabled(this.mUserHandle) : false);
    }

    public Icon getIcon() {
        return Icon.createWithResource(this.mManager.getContext(), 2130838423);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(2131627127);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(2131627128);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(2131627113)};
    }

    public void onPrimaryClick() {
        ItemUseStat.getInstance().handleClick(this.mManager.getContext(), 13, "start condition", "Settings$AccountSettingsActivity");
        this.mManager.getContext().startActivity(new Intent(this.mManager.getContext(), AccountSettingsActivity.class));
    }

    public void onActionClick(int index) {
        if (index == 0) {
            this.mUm.trySetQuietModeDisabled(this.mUserHandle.getIdentifier(), null);
            setActive(false);
            ItemUseStat.getInstance().handleClick(this.mManager.getContext(), 13, "condition onActionClick", "Settings$AccountSettingsActivity");
            return;
        }
        throw new IllegalArgumentException("Unexpected index " + index);
    }

    public int getMetricsConstant() {
        return 383;
    }
}
