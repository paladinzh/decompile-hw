package com.android.settings;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;

public class ConfirmDeviceCredentialActivity extends Activity {
    public static final String TAG = ConfirmDeviceCredentialActivity.class.getSimpleName();

    public static class InternalActivity extends ConfirmDeviceCredentialActivity {
    }

    public static Intent createIntent(CharSequence title, CharSequence details) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", ConfirmDeviceCredentialActivity.class.getName());
        intent.putExtra("android.app.extra.TITLE", title);
        intent.putExtra("android.app.extra.DESCRIPTION", details);
        return intent;
    }

    public void onCreate(Bundle savedInstanceState) {
        boolean launched;
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String title = intent.getStringExtra("android.app.extra.TITLE");
        String details = intent.getStringExtra("android.app.extra.DESCRIPTION");
        int userId = Utils.getCredentialOwnerUserId(this);
        if (isInternalActivity()) {
            try {
                userId = Utils.getUserIdFromBundle(this, intent.getExtras());
            } catch (SecurityException se) {
                Log.e(TAG, "Invalid intent extra", se);
            }
        }
        boolean isManagedProfile = Utils.isManagedProfile(UserManager.get(this), userId);
        if (title == null && isManagedProfile) {
            title = getTitleFromOrganizationName(userId);
        }
        ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(this);
        LockPatternUtils lockPatternUtils = new LockPatternUtils(this);
        if (isManagedProfile && isInternalActivity() && !lockPatternUtils.isSeparateProfileChallengeEnabled(userId)) {
            launched = helper.launchConfirmationActivityWithExternalAndChallenge(0, null, title, details, true, 0, userId);
        } else {
            launched = helper.launchConfirmationActivity(0, null, title, details, false, true, userId);
        }
        if (!launched) {
            Log.d(TAG, "No pattern, password or PIN set.");
            setResult(-1);
        }
        finish();
    }

    private boolean isInternalActivity() {
        return this instanceof InternalActivity;
    }

    private String getTitleFromOrganizationName(int userId) {
        CharSequence organizationNameForUser;
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService("device_policy");
        if (dpm != null) {
            organizationNameForUser = dpm.getOrganizationNameForUser(userId);
        } else {
            organizationNameForUser = null;
        }
        if (organizationNameForUser != null) {
            return organizationNameForUser.toString();
        }
        return null;
    }
}
