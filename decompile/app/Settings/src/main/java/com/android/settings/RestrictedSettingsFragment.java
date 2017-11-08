package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.View;
import android.widget.TextView;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public abstract class RestrictedSettingsFragment extends SettingsPreferenceFragment {
    private View mAdminSupportDetails;
    private boolean mChallengeRequested;
    private boolean mChallengeSucceeded;
    private TextView mEmptyTextView;
    private EnforcedAdmin mEnforcedAdmin;
    private boolean mIsAdminUser;
    private boolean mOnlyAvailableForAdmins = false;
    private final String mRestrictionKey;
    private RestrictionsManager mRestrictionsManager;
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!RestrictedSettingsFragment.this.mChallengeRequested) {
                RestrictedSettingsFragment.this.mChallengeSucceeded = false;
                RestrictedSettingsFragment.this.mChallengeRequested = false;
            }
        }
    };
    private UserManager mUserManager;

    public RestrictedSettingsFragment(String restrictionKey) {
        this.mRestrictionKey = restrictionKey;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mRestrictionsManager = (RestrictionsManager) getSystemService("restrictions");
        this.mUserManager = (UserManager) getSystemService("user");
        this.mIsAdminUser = this.mUserManager.isAdminUser();
        if (icicle != null) {
            this.mChallengeSucceeded = icicle.getBoolean("chsc", false);
            this.mChallengeRequested = icicle.getBoolean("chrq", false);
        }
        IntentFilter offFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        offFilter.addAction("android.intent.action.USER_PRESENT");
        getActivity().registerReceiver(this.mScreenOffReceiver, offFilter);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mAdminSupportDetails = initAdminSupportDetailsView();
        this.mEmptyTextView = initEmptyTextView();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getActivity().isChangingConfigurations()) {
            outState.putBoolean("chrq", this.mChallengeRequested);
            outState.putBoolean("chsc", this.mChallengeSucceeded);
        }
    }

    public void onResume() {
        super.onResume();
        if (shouldBeProviderProtected(this.mRestrictionKey)) {
            ensurePin();
        }
    }

    public void onDestroy() {
        getActivity().unregisterReceiver(this.mScreenOffReceiver);
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 12309) {
            if (resultCode == -1) {
                this.mChallengeSucceeded = true;
                this.mChallengeRequested = false;
            } else {
                this.mChallengeSucceeded = false;
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void ensurePin() {
        if (!this.mChallengeSucceeded && !this.mChallengeRequested && this.mRestrictionsManager.hasRestrictionsProvider()) {
            Intent intent = this.mRestrictionsManager.createLocalApprovalIntent();
            if (intent != null) {
                this.mChallengeRequested = true;
                this.mChallengeSucceeded = false;
                PersistableBundle request = new PersistableBundle();
                request.putString("android.request.mesg", getResources().getString(2131626850));
                intent.putExtra("android.content.extra.REQUEST_BUNDLE", request);
                startActivityForResult(intent, 12309);
            }
        }
    }

    protected boolean isRestrictedAndNotProviderProtected() {
        boolean z = false;
        if (this.mRestrictionKey == null || "restrict_if_overridable".equals(this.mRestrictionKey)) {
            return false;
        }
        if (this.mUserManager.hasUserRestriction(this.mRestrictionKey) && !this.mRestrictionsManager.hasRestrictionsProvider()) {
            z = true;
        }
        return z;
    }

    protected boolean hasChallengeSucceeded() {
        return (this.mChallengeRequested && this.mChallengeSucceeded) || !this.mChallengeRequested;
    }

    protected boolean shouldBeProviderProtected(String restrictionKey) {
        boolean z = false;
        if (restrictionKey == null) {
            return false;
        }
        boolean restricted;
        if ("restrict_if_overridable".equals(restrictionKey)) {
            restricted = true;
        } else {
            restricted = this.mUserManager.hasUserRestriction(this.mRestrictionKey);
        }
        if (restricted) {
            z = this.mRestrictionsManager.hasRestrictionsProvider();
        }
        return z;
    }

    private View initAdminSupportDetailsView() {
        return getActivity().findViewById(2131886215);
    }

    protected TextView initEmptyTextView() {
        return (TextView) getActivity().findViewById(16908292);
    }

    public EnforcedAdmin getRestrictionEnforcedAdmin() {
        this.mEnforcedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), this.mRestrictionKey, UserHandle.myUserId());
        if (this.mEnforcedAdmin != null && this.mEnforcedAdmin.userId == -10000) {
            this.mEnforcedAdmin.userId = UserHandle.myUserId();
        }
        return this.mEnforcedAdmin;
    }

    public TextView getEmptyTextView() {
        return this.mEmptyTextView;
    }

    protected void onDataSetChanged() {
        highlightPreferenceIfNeeded();
        if (this.mAdminSupportDetails != null && isUiRestrictedByOnlyAdmin()) {
            ShowAdminSupportDetailsDialog.setAdminSupportDetails(getActivity(), this.mAdminSupportDetails, getRestrictionEnforcedAdmin(), false);
            setEmptyView(this.mAdminSupportDetails);
        } else if (this.mEmptyTextView != null) {
            setEmptyView(this.mEmptyTextView);
        }
        super.onDataSetChanged();
    }

    public void setIfOnlyAvailableForAdmins(boolean onlyForAdmins) {
        this.mOnlyAvailableForAdmins = onlyForAdmins;
    }

    protected boolean isUiRestricted() {
        if (isRestrictedAndNotProviderProtected() || !hasChallengeSucceeded()) {
            return true;
        }
        return !this.mIsAdminUser ? this.mOnlyAvailableForAdmins : false;
    }

    protected boolean isUiRestrictedByOnlyAdmin() {
        if (!isUiRestricted() || this.mUserManager.hasBaseUserRestriction(this.mRestrictionKey, UserHandle.of(UserHandle.myUserId()))) {
            return false;
        }
        if (this.mIsAdminUser || !this.mOnlyAvailableForAdmins) {
            return true;
        }
        return false;
    }
}
