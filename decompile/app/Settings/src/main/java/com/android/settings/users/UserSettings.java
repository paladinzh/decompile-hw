package com.android.settings.users;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.support.v7.appcompat.R$id;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ItemUseStat;
import com.android.settings.OwnerInfoSettings;
import com.android.settings.ParentControl;
import com.android.settings.PrivacySpaceSettingsHelper;
import com.android.settings.PrivacySpaceSettingsHelper.PasswordCheckListener;
import com.android.settings.RadarReporter;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.SettingsPreferenceFragment.SettingsDialogFragment;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.DefaultStorageLocation;
import com.android.settings.deviceinfo.HwCustMSimSubscriptionStatusTabFragmentImpl;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.users.EditUserInfoController.OnContentChangedCallback;
import com.android.settings.users.EditUserInfoController.onPositiveButtonClickListener;
import com.android.settingslib.R$string;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedSwitchPreference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class UserSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnClickListener, OnDismissListener, OnPreferenceChangeListener, OnContentChangedCallback, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            UserCapabilities userCaps = UserCapabilities.create(context);
            if (!userCaps.mEnabled || ParentControl.isChildModeOn(context)) {
                return result;
            }
            Resources res = context.getResources();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = res.getString(2131626433);
            data.screenTitle = res.getString(2131626433);
            result.add(data);
            if (userCaps.mIsAdmin && (!userCaps.mDisallowAddUser || userCaps.mDisallowAddUserSetByAdmin)) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131626466);
                data.screenTitle = res.getString(2131626433);
                result.add(data);
            }
            if (PrivacySpaceSettingsHelper.isPrivacyUser(context, UserHandle.myUserId())) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131628767);
                data.summaryOn = res.getString(2131628768);
                data.summaryOff = res.getString(2131628768);
                data.screenTitle = res.getString(2131626433);
                result.add(data);
            }
            return result;
        }
    };
    OnItemClickListener chooseItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
            switch (arg2) {
                case 0:
                    UserSettings.this.removeDialog(10);
                    if (!UserSettings.this.choose_isGuest) {
                        UserSettings.this.switchUserNow(UserSettings.this.choose_uid);
                        break;
                    } else {
                        UserSettings.this.createAndSwitchToGuestUser(true);
                        break;
                    }
                case 1:
                    CheckBox checkBox = (CheckBox) arg1.findViewById(2131886297);
                    UserHandle userHandle;
                    if (UserSettings.this.choose_isGuest) {
                        boolean allowsGuest = !checkBox.isChecked();
                        if (allowsGuest) {
                            ItemUseStat.getInstance().handleClick(UserSettings.this.getActivity(), 2, "allow_guest_true");
                        } else {
                            ItemUseStat.getInstance().handleClick(UserSettings.this.getActivity(), 2, "allow_guest_false");
                        }
                        Bundle defaultGuestRestrictions = UserSettings.this.mUserManager.getDefaultGuestRestrictions();
                        if (defaultGuestRestrictions == null) {
                            defaultGuestRestrictions = new Bundle();
                        }
                        defaultGuestRestrictions.putBoolean("no_outgoing_calls", !Boolean.valueOf(allowsGuest).booleanValue());
                        defaultGuestRestrictions.putBoolean("no_sms", true);
                        UserSettings.this.mUserManager.setDefaultGuestRestrictions(defaultGuestRestrictions);
                        for (UserInfo user : UserSettings.this.mUserManager.getUsers(true)) {
                            if (user.isGuest()) {
                                checkBox.setChecked(allowsGuest);
                                userHandle = UserHandle.of(user.id);
                                for (String key : defaultGuestRestrictions.keySet()) {
                                    UserSettings.this.mUserManager.setUserRestriction(key, defaultGuestRestrictions.getBoolean(key), userHandle);
                                }
                            }
                        }
                    } else {
                        boolean allowsUser = !checkBox.isChecked();
                        checkBox.setChecked(allowsUser);
                        if (allowsUser) {
                            ItemUseStat.getInstance().handleClick(UserSettings.this.getActivity(), 2, "allow_user_true");
                        } else {
                            ItemUseStat.getInstance().handleClick(UserSettings.this.getActivity(), 2, "allow_user_false");
                        }
                        userHandle = new UserHandle(UserSettings.this.mUserManager.getUserInfo(UserSettings.this.choose_uid).id);
                        UserSettings.this.mUserManager.setUserRestriction("no_outgoing_calls", !Boolean.valueOf(allowsUser).booleanValue(), userHandle);
                        UserSettings.this.mUserManager.setUserRestriction("no_sms", !Boolean.valueOf(allowsUser).booleanValue(), userHandle);
                    }
                    UserSettings.this.removeDialog(10);
                    break;
                case 2:
                    ItemUseStat.getInstance().handleClick(UserSettings.this.getActivity(), 2, "remove_user");
                    UserSettings.this.removeDialog(10);
                    boolean allowRemoveUserBySystem = RestrictedLockUtils.hasBaseUserRestriction(UserSettings.this.getContext(), "no_remove_user", UserHandle.myUserId());
                    EnforcedAdmin removeDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(UserSettings.this.getContext(), "no_remove_user", UserHandle.myUserId());
                    if (removeDisallowedAdmin == null || allowRemoveUserBySystem) {
                        if (!PrivacySpaceSettingsHelper.isPrivacyUser(UserSettings.this.getActivity(), UserSettings.this.choose_uid)) {
                            if (!UserSettings.this.choose_isGuest) {
                                UserSettings.this.onRemoveUserClicked(UserSettings.this.choose_uid);
                                break;
                            } else {
                                UserSettings.this.showDialog(8);
                                break;
                            }
                        }
                        UserSettings.this.showDialog(1);
                        break;
                    }
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(UserSettings.this.getActivity(), removeDisallowedAdmin);
                    return;
                    break;
            }
        }
    };
    private boolean choose_isGuest;
    private int choose_uid;
    private boolean guestVisibility = false;
    private RestrictedSwitchPreference mAddUserWhenLocked;
    private int mAddedUserId = 0;
    private UserAddPreference mAddguestInfoPreference;
    private boolean mAddingUser;
    private String mAddingUserName;
    private UserAddPreference mAddprivacyInfoPreference;
    private UserAddPreference mAdduserInfoPreference;
    private long mChallenge;
    private Drawable mDefaultIconDrawable;
    private int mDialogId = -1;
    private EditUserInfoController mEditUserInfoController = new EditUserInfoController();
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    UserSettings.this.updateUserList();
                    return;
                case 2:
                    UserSettings.this.mEditUserInfoController.hideWatingDialog(UserSettings.this.mWatingDialog);
                    UserSettings.this.onUserCreated(msg.arg1);
                    return;
                case 3:
                    UserSettings.this.mEditUserInfoController.hideWatingDialog(UserSettings.this.mWatingDialog);
                    UserSettings.this.onManageUserClicked(msg.arg1, true);
                    return;
                case 4:
                    UserSettings.this.guestVisibility = true;
                    UserSettings.this.mEditUserInfoController.hideWatingDialog(UserSettings.this.mWatingDialog);
                    UserSettings.this.updateUserList();
                    UserSettings.this.showDialog(13);
                    return;
                case 5:
                    UserSettings.this.mEditUserInfoController.hideWatingDialog(UserSettings.this.mWatingDialog);
                    UserSettings.this.onPrivacyUserCreated(msg.arg1);
                    return;
                case 6:
                    UserSettings.this.showDialog(16);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHasChallenge;
    private PreferenceGroup mHidePrivacyUserEntry;
    private RestrictedSwitchPreference mHidePrivacyUserEntrySwitch;
    private PreferenceGroup mLockScreenSettings;
    private UserPreference mMePreference;
    private PasswordCheckListener mPasswordCheckListener = new PasswordCheckListener() {
        public void onCheckFinished(byte[] token) {
            if (token != null && BiometricManager.isFingerprintSupported(UserSettings.this.getPrefContext()) && UserSettings.this.mPrivacySpaceSettingsHelper.hasPasswordLock(UserSettings.this.mAddedUserId)) {
                UserSettings.this.mPrivacySpaceSettingsHelper.launchFingerPrintEnrollConfirmDialog(token, UserSettings.this.mPrivacySpaceSettingsHelper.getUserPasswordQuality(UserSettings.this.mAddedUserId), UserSettings.this.mAddedUserId, 4);
            } else {
                UserSettings.this.showDialog(16);
            }
        }
    };
    private String mPrivacyChoosenPassword;
    public PrivacySpaceSettingsHelper mPrivacySpaceSettingsHelper;
    private Bitmap mPrivacyUserIcon;
    private String mPrivacyUserName;
    private int mQuitUserId = -1;
    private int mRemovingUserId = -1;
    private boolean mShouldUpdateUserList = true;
    private UserCapabilities mUserCaps;
    private BroadcastReceiver mUserChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                UserSettings.this.mRemovingUserId = -1;
            } else if ("android.intent.action.USER_INFO_CHANGED".equals(intent.getAction())) {
                int userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userHandle != -1) {
                    UserSettings.this.mUserIcons.remove(userHandle);
                }
            }
            synchronized (UserSettings.this.mUserLock) {
                UserSettings.this.mHandler.sendEmptyMessage(1);
            }
        }
    };
    private SparseArray<Bitmap> mUserIcons = new SparseArray();
    private PreferenceGroup mUserListCategory;
    private final Object mUserLock = new Object();
    private UserManager mUserManager;
    private ProgressDialog mWatingDialog;
    private TextWatcher textWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            Dialog dialog = UserSettings.this.mEditUserInfoController.getDialog();
            if (dialog != null && s != null) {
                Button positiveBtn = ((AlertDialog) dialog).getButton(-1);
                if (TextUtils.isEmpty(s.toString().trim())) {
                    positiveBtn.setEnabled(false);
                } else if (UserSettings.this.isDuplicatedUserName(s.toString().trim())) {
                    UserSettings.this.mEditUserInfoController.hintDuplicatedNickname(true, UserSettings.this.mDialogId);
                    positiveBtn.setEnabled(false);
                } else {
                    UserSettings.this.mEditUserInfoController.hintDuplicatedNickname(false, UserSettings.this.mDialogId);
                    positiveBtn.setEnabled(true);
                }
            }
        }
    };

    class UserAdapter extends BaseAdapter {
        private String[] items;
        private LayoutInflater mLayoutInflater;

        public UserAdapter(Context context, String[] items) {
            this.items = items;
            this.mLayoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        }

        public int getCount() {
            return this.items.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View contentView, ViewGroup parent) {
            if (contentView == null) {
                contentView = this.mLayoutInflater.inflate(2130969250, null);
            }
            ((TextView) contentView.findViewById(R$id.title)).setText(this.items[position]);
            CheckBox checkBox = (CheckBox) contentView.findViewById(2131886297);
            if (position == 1) {
                checkBox.setChecked(UserSettings.this.isEnableCallingSMS());
                checkBox.setVisibility(0);
            } else {
                checkBox.setVisibility(8);
            }
            return contentView;
        }
    }

    private static class UserCapabilities {
        boolean mCanAddGuest;
        boolean mCanAddRestrictedProfile = true;
        boolean mCanAddUser = true;
        boolean mDisallowAddUser;
        boolean mDisallowAddUserSetByAdmin;
        boolean mEnabled = true;
        EnforcedAdmin mEnforcedAdmin;
        boolean mIsAdmin;
        boolean mIsGuest;

        private UserCapabilities() {
        }

        public static UserCapabilities create(Context context) {
            UserManager userManager = (UserManager) context.getSystemService("user");
            UserCapabilities caps = new UserCapabilities();
            if (!UserManager.supportsMultipleUsers() || Utils.isMonkeyRunning()) {
                caps.mEnabled = false;
                return caps;
            }
            UserInfo myUserInfo = userManager.getUserInfo(UserHandle.myUserId());
            caps.mIsGuest = myUserInfo.isGuest();
            caps.mIsAdmin = myUserInfo.isAdmin();
            if (((DevicePolicyManager) context.getSystemService("device_policy")).isDeviceManaged() || Utils.isVoiceCapable(context)) {
                caps.mCanAddRestrictedProfile = false;
            }
            caps.updateAddUserCapabilities(context);
            return caps;
        }

        public void updateAddUserCapabilities(Context context) {
            boolean z;
            boolean canAddUsersWhenLocked;
            this.mEnforcedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(context, "no_add_user", UserHandle.myUserId());
            boolean hasBaseUserRestriction = RestrictedLockUtils.hasBaseUserRestriction(context, "no_add_user", UserHandle.myUserId());
            if (this.mEnforcedAdmin == null || hasBaseUserRestriction) {
                z = false;
            } else {
                z = true;
            }
            this.mDisallowAddUserSetByAdmin = z;
            if (this.mEnforcedAdmin != null) {
                hasBaseUserRestriction = true;
            }
            this.mDisallowAddUser = hasBaseUserRestriction;
            this.mCanAddUser = true;
            if (this.mIsAdmin && UserManager.getMaxSupportedUsers() >= 2 && UserManager.supportsMultipleUsers()) {
                if (this.mDisallowAddUser) {
                }
                canAddUsersWhenLocked = this.mIsAdmin || Global.getInt(context.getContentResolver(), "add_users_when_locked", 0) == 1;
                if (this.mIsGuest || this.mDisallowAddUser) {
                    canAddUsersWhenLocked = false;
                }
                this.mCanAddGuest = canAddUsersWhenLocked;
            }
            this.mCanAddUser = false;
            if (!this.mIsAdmin) {
            }
            canAddUsersWhenLocked = false;
            this.mCanAddGuest = canAddUsersWhenLocked;
        }

        public String toString() {
            return "UserCapabilities{mEnabled=" + this.mEnabled + ", mCanAddUser=" + this.mCanAddUser + ", mCanAddRestrictedProfile=" + this.mCanAddRestrictedProfile + ", mIsAdmin=" + this.mIsAdmin + ", mIsGuest=" + this.mIsGuest + ", mCanAddGuest=" + this.mCanAddGuest + ", mDisallowAddUser=" + this.mDisallowAddUser + ", mEnforcedAdmin=" + this.mEnforcedAdmin + '}';
        }
    }

    protected int getMetricsCategory() {
        return 96;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (icicle != null) {
            if (icicle.containsKey("adding_user")) {
                this.mAddedUserId = icicle.getInt("adding_user");
            }
            if (icicle.containsKey("removing_user")) {
                this.mRemovingUserId = icicle.getInt("removing_user");
            }
            if (icicle.containsKey("guest_flag")) {
                this.choose_isGuest = icicle.getBoolean("guest_flag");
            }
            if (icicle.containsKey("user_id")) {
                this.choose_uid = icicle.getInt("user_id");
            }
            this.mEditUserInfoController.onRestoreInstanceState(icicle);
        }
        Context context = getActivity();
        this.mUserCaps = UserCapabilities.create(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
        if (this.mUserCaps.mEnabled) {
            IntentFilter filter;
            int myUserId = UserHandle.myUserId();
            this.mPrivacySpaceSettingsHelper = new PrivacySpaceSettingsHelper((Fragment) this);
            addPreferencesFromResource(2131230922);
            this.mUserListCategory = (PreferenceGroup) findPreference("user_list");
            this.mMePreference = new UserPreference(getPrefContext(), null, myUserId, this, null);
            this.mMePreference.setKey("user_me");
            this.mMePreference.setOnPreferenceClickListener(this);
            if (this.mUserCaps.mIsAdmin) {
                this.mMePreference.setSummary(2131626443);
            }
            if (this.mUserCaps.mCanAddUser && Utils.isDeviceProvisioned(getActivity()) && this.mUserCaps.mCanAddRestrictedProfile) {
                loadProfile();
                this.mLockScreenSettings = (PreferenceGroup) findPreference("lock_screen_settings");
                this.mAddUserWhenLocked = (RestrictedSwitchPreference) findPreference("add_users_when_locked");
                this.mHidePrivacyUserEntry = (PreferenceGroup) findPreference("hide_privacy_user_entry");
                this.mHidePrivacyUserEntrySwitch = (RestrictedSwitchPreference) findPreference("hide_privacy_user_entry_switch");
                setHasOptionsMenu(true);
                filter = new IntentFilter("android.intent.action.USER_REMOVED");
                filter.addAction("android.intent.action.USER_INFO_CHANGED");
            } else {
                loadProfile();
                this.mLockScreenSettings = (PreferenceGroup) findPreference("lock_screen_settings");
                this.mAddUserWhenLocked = (RestrictedSwitchPreference) findPreference("add_users_when_locked");
                this.mHidePrivacyUserEntry = (PreferenceGroup) findPreference("hide_privacy_user_entry");
                this.mHidePrivacyUserEntrySwitch = (RestrictedSwitchPreference) findPreference("hide_privacy_user_entry_switch");
                setHasOptionsMenu(true);
                filter = new IntentFilter("android.intent.action.USER_REMOVED");
                filter.addAction("android.intent.action.USER_INFO_CHANGED");
            }
            synchronized (this.mUserLock) {
                context.registerReceiverAsUser(this.mUserChangeReceiver, UserHandle.ALL, filter, null, this.mHandler);
            }
            loadProfile();
            updateUserList();
            this.mShouldUpdateUserList = false;
            if (Global.getInt(getContentResolver(), "device_provisioned", 0) == 0) {
                getActivity().finish();
                return;
            }
            return;
        }
        Log.e("UserSettings", "mUserCaps.mEnabled is false.");
        getActivity().finish();
    }

    public void onResume() {
        super.onResume();
        if (this.mUserCaps.mEnabled && this.mShouldUpdateUserList) {
            this.mUserCaps.updateAddUserCapabilities(getActivity());
            loadProfile();
            updateUserList();
        }
    }

    public void onPause() {
        this.mShouldUpdateUserList = true;
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        postEnrollPrivacy();
        if (this.mUserCaps.mEnabled) {
            getActivity().unregisterReceiver(this.mUserChangeReceiver);
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mEditUserInfoController.onSaveInstanceState(outState);
        outState.putInt("adding_user", this.mAddedUserId);
        outState.putInt("removing_user", this.mRemovingUserId);
        outState.putBoolean("guest_flag", this.choose_isGuest);
        outState.putInt("user_id", this.choose_uid);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        this.mEditUserInfoController.startingActivityForResult();
        super.startActivityForResult(intent, requestCode);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        UserManager um = (UserManager) getContext().getSystemService(UserManager.class);
        boolean allowRemoveUser = !um.hasUserRestriction("no_remove_user");
        boolean canSwitchUsers = um.canSwitchUsers();
        if (!this.mUserCaps.mIsAdmin && allowRemoveUser && canSwitchUsers) {
            String nickname = this.mUserManager.getUserName();
            MenuItem removeThisUser = menu.add(0, 1, 0, getResources().getString(2131626271));
            removeThisUser.setIcon(2130838281);
            removeThisUser.setShowAsAction(2);
        }
        if (!this.mUserCaps.mIsAdmin && allowRemoveUser && canSwitchUsers && PrivacySpaceSettingsHelper.isPrivacyUser(getActivity(), UserHandle.myUserId())) {
            MenuItem quitPrivacyUser = menu.add(0, 5, 0, getResources().getString(2131628695));
            quitPrivacyUser.setIcon(2130838322);
            quitPrivacyUser.setShowAsAction(2);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        UserInfo guest = findGuest();
        if (itemId == 1) {
            if (guest == null || UserHandle.myUserId() != guest.id) {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "user_remove_self");
            } else {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "guest_remove_self");
            }
            onRemoveUserClicked(UserHandle.myUserId());
            return true;
        } else if (itemId == 5) {
            onQuitUserClicked(UserHandle.myUserId());
            return true;
        } else if (itemId != 16908332) {
            return super.onOptionsItemSelected(item);
        } else {
            finish();
            return true;
        }
    }

    public void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                activity.finish();
            }
        }
    }

    private void postEnrollPrivacy() {
        if (this.mHasChallenge) {
            BiometricManager bm = BiometricManager.open(getActivity());
            if (bm == null) {
                Log.e("UserSettings", "failed to acquire biometric manager!");
                return;
            }
            bm.postEnrollSafe(this.mChallenge);
            Log.i("UserSettings", "execute post enroll when destroy UserSetttings!");
        }
    }

    private void showToast(int resId, int duration) {
        Toast.makeText(getContext(), resId, duration).show();
    }

    private void loadProfile() {
        if (this.mUserCaps.mIsGuest) {
            this.mMePreference.setIcon(getEncircledDefaultIcon());
            this.mMePreference.setTitle(getString(2131626444, new Object[]{getString(R$string.user_guest)}));
            return;
        }
        new AsyncTask<Void, Void, String>() {
            protected void onPostExecute(String result) {
                UserSettings.this.finishLoadProfile(result);
            }

            protected String doInBackground(Void... values) {
                UserInfo user = UserSettings.this.mUserManager.getUserInfo(UserHandle.myUserId());
                if (user.iconPath == null || user.iconPath.equals("")) {
                    Utils.copyMeProfilePhoto(UserSettings.this.getActivity(), user);
                }
                return user.name;
            }
        }.execute(new Void[0]);
    }

    private void finishLoadProfile(String profileName) {
        if (getActivity() != null) {
            this.mMePreference.setTitle(getString(2131626444, new Object[]{profileName}));
            int myUserId = UserHandle.myUserId();
            Bitmap b = this.mUserManager.getUserIcon(myUserId);
            if (b != null) {
                this.mMePreference.setIcon(encircle(b));
                this.mUserIcons.put(myUserId, b);
            }
        }
    }

    private boolean hasLockscreenSecurity() {
        return new LockPatternUtils(getActivity()).isSecure(UserHandle.myUserId());
    }

    private void launchChooseLockscreen() {
        Intent chooseLockIntent = new Intent("android.app.action.SET_NEW_PASSWORD");
        chooseLockIntent.putExtra("minimum_quality", 65536);
        startActivityForResult(chooseLockIntent, 10);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 10) {
            this.mEditUserInfoController.onActivityResult(requestCode, resultCode, data);
        } else if (resultCode != 0 && hasLockscreenSecurity()) {
            addUserNow(2);
        }
        privaterUserOnActivityResult(requestCode, resultCode, data);
    }

    private void privaterUserOnActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 30:
                if (this.mPrivacySpaceSettingsHelper.isOwnerSecure() && (resultCode == -1 || resultCode == 1)) {
                    if (data != null && data.getStringExtra("choosen_password") != null) {
                        this.mPrivacyChoosenPassword = data.getStringExtra("choosen_password");
                        createNewUser(4, this.mPrivacyUserName, this.mPrivacyUserIcon);
                        break;
                    }
                    this.mPrivacySpaceSettingsHelper.launchChooseLockSettingsForPrivacyUser(this.mHasChallenge, this.mChallenge);
                    break;
                }
                break;
            case 31:
                if ((resultCode == -1 || resultCode == 1) && data != null) {
                    this.mPrivacyChoosenPassword = data.getStringExtra("choosen_password");
                    createNewUser(4, this.mPrivacyUserName, this.mPrivacyUserIcon);
                    break;
                }
                return;
                break;
            case 201:
            case 401:
                this.mHandler.sendEmptyMessage(6);
                break;
            case 204:
                if (resultCode == -1) {
                    this.mRemovingUserId = this.choose_uid;
                    removeUserNow();
                    break;
                }
                break;
        }
    }

    private void createNewUser(int userType, String userName, Bitmap userIcon) {
        ItemUseStat.getInstance().handleClick(getActivity(), 2, "add_user_success");
        this.mWatingDialog = this.mEditUserInfoController.createWatingDialog(getString(2131628231), getActivity());
        if (this.mWatingDialog != null) {
            this.mWatingDialog.show();
        }
        addUserNow(userType, userName, userIcon);
    }

    private void buildPrivacyUser(int userType, String userName, Bitmap userIcon) {
        this.mPrivacyChoosenPassword = null;
        this.mPrivacyUserName = userName;
        this.mPrivacyUserIcon = userIcon;
        BiometricManager bm = BiometricManager.open(getActivity());
        if (bm == null) {
            Log.e("UserSettings", "Unable to initialize the BiometricManager");
            this.mHasChallenge = false;
        } else {
            this.mChallenge = bm.preEnrollSafe();
            this.mHasChallenge = true;
        }
        this.mPrivacySpaceSettingsHelper.launchPrivacySpaceWizard(this.mHasChallenge, this.mChallenge);
    }

    private void onAddUserClicked(int userType) {
        boolean isStorageLow = false;
        try {
            isStorageLow = AppGlobals.getPackageManager().isStorageLow();
        } catch (Exception e) {
            Log.e("UserSettings", "check low storage error because e: " + e);
        }
        if (isStorageLow) {
            showToast(17040234, 0);
            return;
        }
        synchronized (this.mUserLock) {
            if (this.mRemovingUserId == -1 && !this.mAddingUser) {
                switch (userType) {
                    case 1:
                        showDialog(2);
                        break;
                    case 2:
                        if (!hasLockscreenSecurity()) {
                            showDialog(7);
                            break;
                        } else {
                            addUserNow(2);
                            break;
                        }
                    case 3:
                        showDialog(12);
                        break;
                    case 4:
                        showDialog(15);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void onRemoveUserClicked(int userId) {
        synchronized (this.mUserLock) {
            if (this.mRemovingUserId == -1 && !this.mAddingUser) {
                this.mRemovingUserId = userId;
                showDialog(1);
            }
        }
    }

    private void onQuitUserClicked(int userId) {
        if (!this.mAddingUser) {
            this.mQuitUserId = userId;
            showDialog(14);
        }
    }

    private UserInfo createPrivacyUser() {
        UserInfo newUserInfo = this.mUserManager.createUser(this.mAddingUserName, 33554432);
        if (newUserInfo == null) {
            return null;
        }
        disableSetupPackage(newUserInfo.id);
        this.mUserManager.setUserRestriction("no_sms", false, UserHandle.of(newUserInfo.id));
        this.mUserManager.setUserRestriction("no_outgoing_calls", false, UserHandle.of(newUserInfo.id));
        ItemUseStat.getInstance().handleClick(getActivity(), 6, "add_hidden");
        return newUserInfo;
    }

    private void disableSetupPackage(int userId) {
        IPackageManager pm = AppGlobals.getPackageManager();
        String setupPackage = "com.google.android.setupwizard";
        if (pm != null) {
            try {
                if (pm.isPackageAvailable(setupPackage, userId)) {
                    Log.i("UserSettings", "Google setup is available, disable for Privacy User");
                    pm.setApplicationEnabledSetting(setupPackage, 2, 0, userId, null);
                    Secure.putIntForUser(getContentResolver(), "user_setup_complete", 1, userId);
                }
            } catch (RemoteException e) {
                Log.e("UserSettings", "Disable setup package fail!");
            }
        }
    }

    private UserInfo createRestrictedProfile() {
        UserInfo newUserInfo = this.mUserManager.createRestrictedProfile(this.mAddingUserName);
        Utils.assignDefaultPhoto(getActivity(), newUserInfo.id);
        return newUserInfo;
    }

    private UserInfo createTrustedUser() {
        UserInfo newUserInfo = this.mUserManager.createUser(this.mAddingUserName, 0);
        if (newUserInfo != null) {
            Utils.assignDefaultPhoto(getActivity(), newUserInfo.id);
        }
        return newUserInfo;
    }

    private void onManageUserClicked(int userId, boolean newUser) {
        this.mAddingUser = false;
        if (userId == -11) {
            this.choose_uid = 0;
            this.choose_isGuest = true;
            showDialog(10);
            return;
        }
        UserInfo info = this.mUserManager.getUserInfo(userId);
        if (info.isRestricted() && this.mUserCaps.mIsAdmin) {
            Bundle extras = new Bundle();
            extras.putInt("user_id", userId);
            extras.putBoolean("new_user", newUser);
            ((SettingsActivity) getActivity()).startPreferencePanel(RestrictedProfileSettings.class.getName(), extras, 2131626576, null, null, 0);
        } else if (info.id == UserHandle.myUserId()) {
            OwnerInfoSettings.show(this);
        } else if (this.mUserCaps.mIsAdmin) {
            this.choose_uid = userId;
            this.choose_isGuest = false;
            showDialog(10);
        }
    }

    private void onUserCreated(int userId) {
        this.mAddedUserId = userId;
        this.mAddingUser = false;
        if (this.mUserManager.getUserInfo(userId).isRestricted()) {
            showDialog(4);
        } else {
            showDialog(3);
        }
    }

    private void onPrivacyUserCreated(int userId) {
        this.mAddedUserId = userId;
        this.mAddingUser = false;
        this.mPrivacySpaceSettingsHelper.setPrivacyLockScreen(this.mPrivacyChoosenPassword, this.mAddedUserId, this.mHasChallenge, this.mChallenge, this.mPasswordCheckListener);
    }

    public void onDialogShowing() {
        super.onDialogShowing();
        setOnDismissListener(this);
        SettingsDialogFragment dialogFragment = (SettingsDialogFragment) getDialogFragment();
        if (dialogFragment == null) {
            return;
        }
        if (dialogFragment.getDialogId() == 2 || dialogFragment.getDialogId() == 15) {
            AlertDialog mNameAlert = (AlertDialog) this.mEditUserInfoController.getDialog();
            if (mNameAlert != null) {
                EditText userNameView = (EditText) mNameAlert.findViewById(2131886207);
                Button positiveBtn = mNameAlert.getButton(-1);
                String s = userNameView.getText().toString().trim();
                if (TextUtils.isEmpty(s)) {
                    positiveBtn.setEnabled(false);
                } else if (isDuplicatedUserName(s)) {
                    this.mEditUserInfoController.hintDuplicatedNickname(true, 2);
                    this.mEditUserInfoController.hintDuplicatedNickname(true, 15);
                    positiveBtn.setEnabled(false);
                } else {
                    this.mEditUserInfoController.hintDuplicatedNickname(false, 2);
                    this.mEditUserInfoController.hintDuplicatedNickname(false, 15);
                    positiveBtn.setEnabled(true);
                }
            }
        }
    }

    public Dialog onCreateDialog(int dialogId) {
        Context context = getActivity();
        if (context == null) {
            return null;
        }
        switch (dialogId) {
            case 1:
                if (this.mRemovingUserId == -1) {
                    this.mRemovingUserId = this.choose_uid;
                }
                Dialog dlg = UserDialogs.createRemoveDialog(getActivity(), this.mRemovingUserId, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ItemUseStat.getInstance().handleClick(UserSettings.this.getActivity(), 2, "remove_user_success");
                        UserInfo guest = UserSettings.this.findGuest();
                        if (guest != null && guest.id == UserHandle.myUserId()) {
                            UserSettings.this.guestVisibility = false;
                        }
                        if (!PrivacySpaceSettingsHelper.isPrivacyUser(UserSettings.this.getActivity(), UserSettings.this.mRemovingUserId)) {
                            UserSettings.this.removeUserNow();
                        } else if (UserSettings.this.mRemovingUserId == UserHandle.myUserId() || !UserSettings.this.hasLockscreenSecurity()) {
                            UserSettings.this.removeUserNow();
                        } else {
                            UserSettings.this.mPrivacySpaceSettingsHelper.launchConfirmPasswordForRemovingPrivacyUser(UserSettings.this.mRemovingUserId);
                        }
                    }
                });
                setDialogButtonRed(dlg);
                return dlg;
            case 2:
                boolean longMessageDisplayed = getActivity().getPreferences(0).getBoolean("key_add_user_long_message_displayed", false);
                int userType = dialogId == 2 ? 1 : 2;
                this.mDialogId = 2;
                this.mEditUserInfoController.setTextWatcher(this.textWatcher);
                return this.mEditUserInfoController.createAddUserDialog(this, userType, new onPositiveButtonClickListener() {
                    public void onclick(int userType, String userName, Bitmap userIcon) {
                        ItemUseStat.getInstance().handleClick(UserSettings.this.getActivity(), 2, "add_user_success");
                        UserSettings.this.mWatingDialog = UserSettings.this.mEditUserInfoController.createWatingDialog(UserSettings.this.getString(2131628231), UserSettings.this.getActivity());
                        if (UserSettings.this.mWatingDialog != null) {
                            UserSettings.this.mWatingDialog.show();
                        }
                        UserSettings.this.addUserNow(userType, userName, userIcon);
                    }
                });
            case 3:
                return new Builder(context).setMessage(2131628729).setPositiveButton(2131628396, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.switchUserNow(UserSettings.this.mAddedUserId);
                    }
                }).setNegativeButton(2131624572, null).create();
            case 4:
                return new Builder(context).setMessage(2131626457).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.switchUserNow(UserSettings.this.mAddedUserId);
                    }
                }).setNegativeButton(17039360, null).create();
            case 5:
                return new Builder(context).setMessage(2131626460).setPositiveButton(17039370, null).create();
            case 6:
                List<HashMap<String, String>> data = new ArrayList();
                HashMap<String, String> addUserItem = new HashMap();
                addUserItem.put("title", getString(2131626450));
                addUserItem.put("summary", getString(2131626448));
                HashMap<String, String> addProfileItem = new HashMap();
                addProfileItem.put("title", getString(2131626451));
                addProfileItem.put("summary", getString(2131626449));
                data.add(addUserItem);
                data.add(addProfileItem);
                Builder builder = new Builder(context);
                SimpleAdapter adapter = new SimpleAdapter(builder.getContext(), data, 2130969221, new String[]{"title", "summary"}, new int[]{R$id.title, 2131886387});
                builder.setTitle(2131626446);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int i;
                        UserSettings userSettings = UserSettings.this;
                        if (which == 0) {
                            i = 1;
                        } else {
                            i = 2;
                        }
                        userSettings.onAddUserClicked(i);
                    }
                });
                return builder.create();
            case 7:
                return new Builder(context).setMessage(2131626438).setPositiveButton(2131626439, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.launchChooseLockscreen();
                    }
                }).setNegativeButton(17039360, null).create();
            case 8:
                Dialog dlg2 = new Builder(context).setMessage(2131626476).setPositiveButton(context.getResources().getString(2131626485), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.exitGuest();
                    }
                }).setNegativeButton(17039360, null).create();
                setDialogButtonRed(dlg2);
                return dlg2;
            case 9:
                this.mEditUserInfoController.setTextWatcher(this.textWatcher);
                this.mDialogId = 9;
                return this.mEditUserInfoController.createDialog(this, this.mMePreference.getIcon(), this.mMePreference.getTitle(), 2131624633, this, Process.myUserHandle());
            case 10:
                String[] menuItems = getChooseMenuItems();
                if (menuItems == null || menuItems.length == 0) {
                    return null;
                }
                UserAdapter userAdapter = new UserAdapter(getActivity(), menuItems);
                View listView = new ListView(getActivity());
                listView.setAdapter(userAdapter);
                listView.setOnItemClickListener(this.chooseItemClickListener);
                return new Builder(getActivity()).setView(listView).create();
            case 11:
                return new Builder(context).setTitle(2131628169).setMessage(2131628168).setPositiveButton(2131627697, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("android.settings.INTERNAL_STORAGE_SETTINGS");
                        intent.setPackage("com.android.settings");
                        UserSettings.this.startActivity(intent);
                    }
                }).setNegativeButton(17039360, null).create();
            case 12:
                return new Builder(context).setView(getActivity().getLayoutInflater().inflate(2130968613, null)).setPositiveButton(2131628702, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.mWatingDialog = UserSettings.this.mEditUserInfoController.createWatingDialog(UserSettings.this.getString(2131628231), UserSettings.this.getActivity());
                        if (UserSettings.this.mWatingDialog != null) {
                            UserSettings.this.mWatingDialog.show();
                        }
                        UserSettings.this.addUserNow(3);
                    }
                }).setNegativeButton(17039360, null).create();
            case 13:
                return new Builder(context).setMessage(2131628237).setPositiveButton(2131628396, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.createAndSwitchToGuestUser(true);
                    }
                }).setNegativeButton(2131624572, null).create();
            case 14:
                return UserDialogs.createQuitDialog(getActivity(), this.mQuitUserId, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.quitUser();
                    }
                });
            case 15:
                this.mDialogId = 15;
                this.mEditUserInfoController.setTextWatcher(this.textWatcher);
                return this.mEditUserInfoController.createAddUserDialog(this, 4, new onPositiveButtonClickListener() {
                    public void onclick(int userType, String userName, Bitmap userIcon) {
                        UserSettings.this.buildPrivacyUser(userType, userName, userIcon);
                    }
                });
            case 16:
                return new Builder(context).setMessage(2131628720).setPositiveButton(2131628396, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.switchUserNow(UserSettings.this.mAddedUserId);
                    }
                }).setNegativeButton(2131624572, null).create();
            default:
                return null;
        }
    }

    private boolean isEnableCallingSMS() {
        boolean isAllow = false;
        if (this.mUserManager != null) {
            if (this.choose_isGuest) {
                Bundle mDefaultGuestRestrictions = this.mUserManager.getDefaultGuestRestrictions();
                if (mDefaultGuestRestrictions != null) {
                    boolean isAllowsGuest;
                    if (mDefaultGuestRestrictions.getBoolean("no_outgoing_calls")) {
                        isAllowsGuest = false;
                    } else {
                        isAllowsGuest = true;
                    }
                    return isAllowsGuest;
                }
            }
            if (!this.mUserManager.hasUserRestriction("no_outgoing_calls", new UserHandle(this.choose_uid))) {
                isAllow = true;
            }
            return isAllow;
        }
        return false;
    }

    private boolean isDuplicatedUserName(String userName) {
        if (!(TextUtils.isEmpty(userName) || this.mUserManager == null)) {
            for (UserInfo userInfo : this.mUserManager.getUsers(true)) {
                if (UserHandle.myUserId() != userInfo.id && userName.equals(userInfo.name)) {
                    this.mEditUserInfoController.setNameDuplicationTip(getResources().getString(2131627923));
                    return true;
                }
            }
        }
        return false;
    }

    private String[] getChooseMenuItems() {
        String accoutSwitch = getResources().getString(2131628127);
        String removeString;
        String enable_calling;
        if (this.choose_isGuest) {
            removeString = getResources().getString(2131628239);
            enable_calling = getResources().getString(2131628293);
            return Utils.isWifiOnly(getPrefContext()) ? new String[]{accoutSwitch, removeString} : new String[]{accoutSwitch, enable_calling, removeString};
        } else if (this.choose_uid == -1) {
            throw new RuntimeException("Arguments to this fragment must contain the user id");
        } else {
            enable_calling = getResources().getString(2131628294);
            if (this.mUserCaps.mIsAdmin) {
                removeString = "";
                if (PrivacySpaceSettingsHelper.isPrivacyUser(getActivity(), this.choose_uid)) {
                    accoutSwitch = getResources().getString(2131628722);
                    removeString = getResources().getString(2131628723);
                } else {
                    removeString = getResources().getString(2131628701);
                }
                return Utils.isWifiOnly(getPrefContext()) ? new String[]{accoutSwitch, removeString} : new String[]{accoutSwitch, enable_calling, removeString};
            }
            return Utils.isWifiOnly(getPrefContext()) ? new String[]{accoutSwitch} : new String[]{accoutSwitch, enable_calling};
        }
    }

    private void setDialogButtonRed(Dialog dlg) {
        dlg.show();
        View del = dlg.findViewById(16908313);
        if (del != null) {
            ((Button) del).setTextColor(-65536);
        }
    }

    private void initAddPrivacyInfoPreference(PreferenceGroup groupToAddUsers) {
        if (this.mPrivacySpaceSettingsHelper.canAddPrivacyUser()) {
            Log.d("UserSettings", "add Preference for PrivacySpace");
            this.mAddprivacyInfoPreference = new UserAddPreference(getPrefContext(), null, this, null);
            this.mAddprivacyInfoPreference.setTitle(2131628717);
            this.mAddprivacyInfoPreference.setOnPreferenceClickListener(this);
            groupToAddUsers.addPreference(this.mAddprivacyInfoPreference);
        }
    }

    private void quitUser() {
        ActivityManager.logoutCurrentUser();
    }

    private void removeUserNow() {
        if (this.mRemovingUserId == UserHandle.myUserId()) {
            removeThisUser();
        } else {
            new Thread() {
                public void run() {
                    synchronized (UserSettings.this.mUserLock) {
                        UserSettings.this.mUserManager.removeUser(UserSettings.this.mRemovingUserId);
                        UserSettings.this.mHandler.sendEmptyMessage(1);
                    }
                }
            }.start();
        }
        if (PrivacySpaceSettingsHelper.isPrivacyUser(getActivity(), this.mRemovingUserId)) {
            this.mPrivacySpaceSettingsHelper.setPrivacyUserEntryHidden(false);
        }
    }

    private void removeThisUser() {
        if (this.mUserManager.canSwitchUsers()) {
            try {
                ActivityManagerNative.getDefault().switchUser(0);
                ((UserManager) getContext().getSystemService(UserManager.class)).removeUser(UserHandle.myUserId());
            } catch (RemoteException e) {
                Log.e("UserSettings", "Unable to remove self user");
            }
            return;
        }
        Log.w("UserSettings", "Cannot remove current user when switching is disabled");
    }

    private void addUserNow(int userType) {
        addUserNow(userType, null, null);
    }

    private void addUserNow(final int userType, final String userName, final Bitmap userIcon) {
        synchronized (this.mUserLock) {
            this.mAddingUser = true;
            if (userName == null) {
                String string;
                if (userType == 1) {
                    string = getString(2131626467);
                } else {
                    string = getString(2131626468);
                }
                this.mAddingUserName = string;
            } else {
                this.mAddingUserName = userName;
            }
            new Thread() {
                public void run() {
                    UserInfo user = null;
                    try {
                        if (userType == 1) {
                            user = UserSettings.this.createTrustedUser();
                        } else if (userType == 3) {
                            user = UserSettings.this.mUserManager.createGuest(UserSettings.this.getActivity(), UserSettings.this.getResources().getString(R$string.user_guest));
                        } else if (userType == 4) {
                            user = UserSettings.this.createPrivacyUser();
                        } else {
                            user = UserSettings.this.createRestrictedProfile();
                        }
                    } catch (Exception e) {
                        Log.e("UserSettings", "addUserNow()-->Exception e : " + e);
                        HashMap<Short, Object> map = new HashMap();
                        map.put(Short.valueOf((short) 0), Integer.valueOf(userType));
                        map.put(Short.valueOf((short) 1), userName);
                        RadarReporter.reportRadar(907018006, map);
                    }
                    if (user == null) {
                        UserSettings.this.mAddingUser = false;
                        Log.w("UserSettings", "addUserNow()-->user is null return");
                        return;
                    }
                    if (userIcon != null) {
                        UserSettings.this.mUserManager.setUserIcon(user.id, userIcon);
                    }
                    synchronized (UserSettings.this.mUserLock) {
                        UserSettings.this.mAddingUser = false;
                        if (userType == 1) {
                            UserSettings.this.mHandler.sendEmptyMessage(1);
                            UserSettings.this.mHandler.sendMessage(UserSettings.this.mHandler.obtainMessage(2, user.id, user.serialNumber));
                        } else if (userType == 3) {
                            UserSettings.this.mHandler.sendEmptyMessage(4);
                        } else if (userType == 4) {
                            UserSettings.this.mHandler.sendEmptyMessage(1);
                            UserSettings.this.mHandler.sendMessage(UserSettings.this.mHandler.obtainMessage(5, user.id, user.serialNumber));
                        } else {
                            UserSettings.this.mHandler.sendMessage(UserSettings.this.mHandler.obtainMessage(3, user.id, user.serialNumber));
                        }
                    }
                }
            }.start();
        }
    }

    private void switchUser(final int userId) {
        int id = userId;
        new Builder(getActivity()).setItems(new String[]{getActivity().getResources().getString(2131628127)}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        UserSettings.this.switchUserNow(userId);
                        dialog.dismiss();
                        return;
                    default:
                        return;
                }
            }
        }).create().show();
    }

    private void switchUserNow(int userId) {
        try {
            UserInfo guest = findGuest();
            if (guest == null || userId != guest.id) {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "switch_to_user");
            } else {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "switch_to_guest");
            }
            ActivityManagerNative.getDefault().switchUser(userId);
        } catch (RemoteException e) {
            Log.i("UserSettings", "switchUserNow failed");
        }
    }

    private void exitGuest() {
        UserInfo guest = findGuest();
        if (guest != null) {
            try {
                ((UserManager) getActivity().getSystemService("user")).removeUser(guest.id);
                this.guestVisibility = false;
            } catch (Exception e) {
                Log.e("UserSettings", "Unable to remove guest user");
            }
        }
    }

    private void requestJlogEnable(boolean enable) {
        Activity activity = getActivity();
        if (activity instanceof SettingsActivity) {
            ((SettingsActivity) activity).requestJlogEnable(enable);
        }
    }

    private List<UserInfo> getUsersWithoutCloneUser() {
        List<UserInfo> users = this.mUserManager.getUsers(true);
        List<UserInfo> usersWithoutClone = new ArrayList();
        for (UserInfo user : users) {
            if (!user.isClonedProfile()) {
                usersWithoutClone.add(user);
            }
        }
        return usersWithoutClone;
    }

    private void updateUserList() {
        if (getActivity() != null) {
            UserPreference pref;
            requestJlogEnable(false);
            List<UserInfo> users = getUsersWithoutCloneUser();
            boolean voiceCapable = Utils.isVoiceCapable(getActivity());
            ArrayList<Integer> missingIcons = new ArrayList();
            ArrayList<UserPreference> userPreferences = new ArrayList();
            userPreferences.add(this.mMePreference);
            for (UserInfo user : users) {
                if (user.supportsSwitchToByUser()) {
                    if (user.id == UserHandle.myUserId()) {
                        pref = this.mMePreference;
                    } else if (user.isGuest()) {
                        this.guestVisibility = true;
                    } else {
                        PrivacySpaceSettingsHelper privacySpaceSettingsHelper = this.mPrivacySpaceSettingsHelper;
                        if (!PrivacySpaceSettingsHelper.isPrivacyUser(user) || !this.mPrivacySpaceSettingsHelper.shouldHidePrivacyUserEntry()) {
                            if (this.mUserCaps.mIsAdmin) {
                                if (!voiceCapable) {
                                    boolean isRestricted = user.isRestricted();
                                }
                            }
                            boolean showDelete = this.mUserCaps.mIsAdmin ? (voiceCapable || user.isRestricted() || user.isGuest()) ? false : true : false;
                            pref = new UserPreference(getPrefContext(), null, user.id, this, showDelete ? this : null);
                            pref.setOnPreferenceClickListener(this);
                            pref.setKey("id=" + user.id);
                            if (PrivacySpaceSettingsHelper.isPrivacyUser(user)) {
                                int hiddenId = user.id;
                                int nowUserId = UserHandle.myUserId();
                                if ((nowUserId == 0) || hiddenId == nowUserId) {
                                    privacySpaceSettingsHelper = this.mPrivacySpaceSettingsHelper;
                                    if (PrivacySpaceSettingsHelper.isPrivacySpaceSupported()) {
                                        userPreferences.add(pref);
                                    }
                                }
                            } else {
                                userPreferences.add(pref);
                            }
                            if (user.isAdmin()) {
                                pref.setSummary(2131626443);
                            }
                            pref.setTitle(user.name);
                        }
                    }
                    if (pref != null) {
                        if (isInitialized(user)) {
                            if (user.isRestricted()) {
                            }
                        } else if (user.isRestricted()) {
                            pref.setSummary(2131626441);
                        } else {
                            pref.setSummary(2131626440);
                        }
                        if (user.iconPath != null) {
                            pref.setIcon(encircle(this.mUserManager.getUserIcon(user.id)));
                        } else {
                            pref.setIcon(getEncircledDefaultIcon());
                        }
                    }
                }
            }
            if (!this.mUserCaps.mIsGuest && this.guestVisibility) {
                pref = new UserPreference(getPrefContext(), null, -11, this, null);
                pref.setTitle((int) R$string.user_guest);
                pref.setIcon(getEncircledDefaultIcon());
                pref.setOnPreferenceClickListener(this);
                userPreferences.add(pref);
                pref.setDisabledByAdmin(this.mUserCaps.mDisallowAddUser ? this.mUserCaps.mEnforcedAdmin : null);
            }
            Collections.sort(userPreferences, UserPreference.SERIAL_NUMBER_COMPARATOR);
            getActivity().invalidateOptionsMenu();
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "user_count", userPreferences.size());
            if (missingIcons.size() > 0) {
                loadIconsAsync(missingIcons);
            }
            PreferenceGroup preferenceScreen = getPreferenceScreen();
            if (getPreferenceScreen() == null) {
                getActivity().finish();
                return;
            }
            PreferenceGroup groupToAddUsers;
            preferenceScreen.removeAll();
            if (this.mUserCaps.mCanAddRestrictedProfile) {
                this.mUserListCategory.removeAll();
                this.mUserListCategory.setOrder(HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID);
                preferenceScreen.addPreference(this.mUserListCategory);
                groupToAddUsers = this.mUserListCategory;
            } else {
                groupToAddUsers = preferenceScreen;
            }
            UserPreference lastUserPreference = null;
            int size = userPreferences.size();
            for (int i = 0; i < size; i++) {
                Preference userPreference = (UserPreference) userPreferences.get(i);
                userPreference.setOrder(HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID);
                groupToAddUsers.addPreference(userPreference);
                userPreference.setDividerVisible(true);
                Preference lastUserPreference2 = userPreference;
            }
            if ((this.mUserCaps.mCanAddUser || this.mUserCaps.mDisallowAddUserSetByAdmin) && Utils.isDeviceProvisioned(getActivity())) {
                this.mAdduserInfoPreference = new UserAddPreference(getPrefContext(), null, this, null);
                this.mAdduserInfoPreference.setTitle(2131628292);
                this.mAdduserInfoPreference.setOnPreferenceClickListener(this);
                this.mAddguestInfoPreference = new UserAddPreference(getPrefContext(), null, this, null);
                this.mAddguestInfoPreference.setTitle(2131628291);
                this.mAddguestInfoPreference.setOnPreferenceClickListener(this);
                if (this.mUserManager.canAddMoreUsers()) {
                    groupToAddUsers.addPreference(this.mAdduserInfoPreference);
                }
                if (!this.guestVisibility) {
                    groupToAddUsers.addPreference(this.mAddguestInfoPreference);
                }
                initAddPrivacyInfoPreference(groupToAddUsers);
                setAddUserDisabled();
            }
            boolean hasLockScreenSettings = this.mUserCaps.mIsAdmin ? this.mUserCaps.mDisallowAddUser ? this.mUserCaps.mDisallowAddUserSetByAdmin : true : false;
            if (hasLockScreenSettings) {
                this.mLockScreenSettings.setOrder(HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID);
                preferenceScreen.addPreference(this.mLockScreenSettings);
                this.mAddUserWhenLocked.setChecked(Global.getInt(getContentResolver(), "add_users_when_locked", 0) == 1);
                this.mAddUserWhenLocked.setOnPreferenceChangeListener(this);
                this.mAddUserWhenLocked.setDisabledByAdmin(this.mUserCaps.mDisallowAddUser ? this.mUserCaps.mEnforcedAdmin : null);
            }
            if (hasLockScreenSettings) {
                if (lastUserPreference != null) {
                    lastUserPreference.setDividerVisible(false);
                }
            } else if (PrivacySpaceSettingsHelper.isPrivacyUser(getActivity(), UserHandle.myUserId())) {
                this.mHidePrivacyUserEntry.setOrder(HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID);
                preferenceScreen.addPreference(this.mHidePrivacyUserEntry);
                this.mHidePrivacyUserEntrySwitch.setChecked(this.mPrivacySpaceSettingsHelper.shouldHidePrivacyUserEntry());
                this.mHidePrivacyUserEntrySwitch.setOnPreferenceChangeListener(this);
                if (lastUserPreference != null) {
                    lastUserPreference.setDividerVisible(false);
                }
            } else if (lastUserPreference != null) {
                lastUserPreference.setDividerVisible(true);
            }
            requestJlogEnable(true);
        }
    }

    private void setAddUserDisabled() {
        EnforcedAdmin isDisable = null;
        if (this.mUserCaps.mDisallowAddUser) {
            isDisable = this.mUserCaps.mEnforcedAdmin;
        }
        this.mAddguestInfoPreference.setDisabledByAdmin(isDisable);
        this.mAdduserInfoPreference.setDisabledByAdmin(isDisable);
    }

    private void loadIconsAsync(List<Integer> missingIcons) {
        new AsyncTask<List<Integer>, Void, Void>() {
            protected void onPostExecute(Void result) {
                UserSettings.this.updateUserList();
            }

            protected Void doInBackground(List<Integer>... values) {
                for (Integer intValue : values[0]) {
                    int userId = intValue.intValue();
                    Bitmap bitmap = UserSettings.this.mUserManager.getUserIcon(userId);
                    if (bitmap == null) {
                        bitmap = Utils.getDefaultUserIconAsBitmap(userId);
                    }
                    UserSettings.this.mUserIcons.append(userId, bitmap);
                }
                return null;
            }
        }.execute(new List[]{missingIcons});
    }

    private Drawable getEncircledDefaultIcon() {
        if (this.mDefaultIconDrawable == null) {
            this.mDefaultIconDrawable = encircle(Utils.getDefaultUserIconAsBitmap(-10000));
        }
        return this.mDefaultIconDrawable;
    }

    public boolean onPreferenceClick(Preference pref) {
        if (pref == this.mMePreference) {
            if (this.mUserCaps.mIsGuest) {
                return true;
            }
            if (this.mUserManager.isLinkedUser()) {
                onManageUserClicked(UserHandle.myUserId(), false);
            } else {
                showDialog(9);
            }
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "click_user_preference");
        } else if (pref instanceof UserPreference) {
            int userId = ((UserPreference) pref).getUserId();
            if (userId != -11) {
                UserInfo user = this.mUserManager.getUserInfo(userId);
                if (isInitialized(user)) {
                    if (this.mUserCaps.mIsAdmin) {
                        onManageUserClicked(userId, false);
                    } else {
                        switchUser(userId);
                    }
                } else if (this.mUserCaps.mIsAdmin) {
                    this.choose_uid = user.id;
                    this.choose_isGuest = false;
                    showDialog(10);
                } else {
                    switchUser(userId);
                }
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "click_user_preference");
            } else if (this.mUserCaps.mIsAdmin) {
                onManageUserClicked(userId, false);
            } else {
                createAndSwitchToGuestUser(false);
            }
        } else if (pref == this.mAdduserInfoPreference) {
            if ((this.mUserCaps.mCanAddUser || this.mUserCaps.mDisallowAddUserSetByAdmin) && Utils.isDeviceProvisioned(getActivity())) {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "add_user");
                if (DefaultStorageLocation.isSdcard()) {
                    showToast(2131628284, 0);
                } else if (this.mUserCaps.mCanAddRestrictedProfile) {
                    showDialog(6);
                } else {
                    onAddUserClicked(1);
                }
            }
        } else if (pref == this.mAddguestInfoPreference) {
            if ((this.mUserCaps.mCanAddUser || this.mUserCaps.mDisallowAddUserSetByAdmin) && Utils.isDeviceProvisioned(getActivity())) {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "add_guest");
                if (DefaultStorageLocation.isSdcard()) {
                    showToast(2131628284, 0);
                } else {
                    onAddUserClicked(3);
                }
            }
        } else if (pref == this.mAddprivacyInfoPreference && ((this.mUserCaps.mCanAddUser || this.mUserCaps.mDisallowAddUserSetByAdmin) && Utils.isDeviceProvisioned(getActivity()))) {
            if (DefaultStorageLocation.isSdcard()) {
                showToast(2131628284, 0);
            } else if (this.mUserCaps.mCanAddRestrictedProfile) {
                showDialog(6);
            } else {
                onAddUserClicked(4);
            }
        }
        return false;
    }

    private void createAndSwitchToGuestUser(boolean isSwitchNow) {
        UserInfo guest = findGuest();
        if (guest != null) {
            if (isSwitchNow) {
                switchUserNow(guest.id);
            } else {
                switchUser(guest.id);
            }
            return;
        }
        UserInfo guestUser = this.mUserManager.createGuest(getActivity(), getResources().getString(R$string.user_guest));
        if (guestUser != null) {
            if (isSwitchNow) {
                switchUserNow(guestUser.id);
            } else {
                switchUser(guestUser.id);
            }
        }
    }

    private UserInfo findGuest() {
        for (UserInfo user : this.mUserManager.getUsers()) {
            if (user.isGuest()) {
                return user;
            }
        }
        return null;
    }

    private boolean isInitialized(UserInfo user) {
        return (user.flags & 16) != 0;
    }

    private Drawable encircle(Bitmap icon) {
        return Utils.createRoundPhotoDrawable(getResources(), icon);
    }

    public void onClick(View v) {
        if (v.getTag() instanceof UserPreference) {
            int userId = ((UserPreference) v.getTag()).getUserId();
            switch (v.getId()) {
                case 2131886966:
                    EnforcedAdmin removeDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(getContext(), "no_remove_user", UserHandle.myUserId());
                    if (removeDisallowedAdmin != null) {
                        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(), removeDisallowedAdmin);
                        return;
                    } else {
                        onRemoveUserClicked(userId);
                        return;
                    }
                default:
                    return;
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        synchronized (this.mUserLock) {
            this.mRemovingUserId = -1;
            updateUserList();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        if (preference == this.mAddUserWhenLocked) {
            Boolean value = (Boolean) newValue;
            ContentResolver contentResolver = getContentResolver();
            String str = "add_users_when_locked";
            if (value != null && value.booleanValue()) {
                i = 1;
            }
            Global.putInt(contentResolver, str, i);
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
            return true;
        } else if (preference != this.mHidePrivacyUserEntrySwitch) {
            return false;
        } else {
            this.mPrivacySpaceSettingsHelper.setPrivacyUserEntryHidden(((Boolean) newValue).booleanValue());
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
            return true;
        }
    }

    public int getHelpResource() {
        return 2131626549;
    }

    public void onPhotoChanged(Drawable photo) {
        this.mMePreference.setIcon(photo);
    }

    public void onLabelChanged(CharSequence label) {
        this.mMePreference.setTitle(getString(2131626444, new Object[]{label}));
    }
}
