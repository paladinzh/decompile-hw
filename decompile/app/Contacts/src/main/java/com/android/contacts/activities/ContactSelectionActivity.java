package com.android.contacts.activities;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.provider.Settings.System;
import android.provider.Telephony.Mms;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.activities.ContactAndGroupMultiSelectionActivity;
import com.android.contacts.hap.activities.ContactsMultiSelectMessageActivitySimplified;
import com.android.contacts.hap.rcs.activities.RcsContactSelectionActivityHelp;
import com.android.contacts.hap.sim.SimFactory;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.utils.ActionBarTitle;
import com.android.contacts.hap.utils.ScreenUtils;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactPickerFragment;
import com.android.contacts.list.ContactsIntentResolver;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.EmailAddressPickerFragment;
import com.android.contacts.list.OnContactPickerActionListener;
import com.android.contacts.list.OnEmailAddressPickerActionListener;
import com.android.contacts.list.OnPhoneNumberPickerActionListener;
import com.android.contacts.list.OnPostalAddressPickerActionListener;
import com.android.contacts.list.PhoneNumberPickerFragment;
import com.android.contacts.list.PostalAddressPickerFragment;
import com.android.contacts.list.SpeedDialContactPickerFragment;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.ContextMenuAdapter;
import com.google.android.gms.R;
import com.google.android.gms.actions.SearchIntents;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.google.common.collect.Sets;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCustUtils;
import java.util.Set;

public class ContactSelectionActivity extends ContactsActivity implements OnCreateContextMenuListener, OnClickListener, Callback {
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final boolean IS_CHINA_AREA = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    private boolean isLaunchedFromDetail;
    private boolean isLaunchedFromDialpad;
    private ActionBarTitle mActionBarTitle;
    private int mActionCode = -1;
    protected EditText mContactsSearchView;
    private View mCreateNewContactButton;
    private HwCustContactSelectionActivity mHwCust = null;
    private ContactsIntentResolver mIntentResolver = new ContactsIntentResolver(this);
    private boolean mIsNeedUpdateWindows;
    protected ContactEntryListFragment<?> mListFragment;
    private RcsContactSelectionActivityHelp mRcsCust = null;
    private ContactsRequest mRequest;
    protected View mSearchLayout;
    public OnTouchListener touchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            v.onTouchEvent(event);
            ContactSelectionActivity.this.mSearchLayout.setBackgroundResource(R.drawable.textfield_activated_holo_light);
            ContactSelectionActivity.this.mContactsSearchView.setCursorVisible(true);
            return true;
        }
    };

    private final class ContactPickerActionListener implements OnContactPickerActionListener {
        private long LIMIT_TIME_SHOWDIALOG;
        boolean cancle;
        private AlertDialog dialog;
        boolean waiting;

        private class GetSimContactIdTask extends AsyncTask<Uri, Void, Integer> {
            private int id;
            private Intent intent;

            public GetSimContactIdTask(Intent intent) {
                this.intent = intent;
            }

            protected Integer doInBackground(Uri... params) {
                this.id = ContactPickerActionListener.this.getSIMContactId(params[0]);
                if (this.id != -1) {
                    this.id = ContactPickerActionListener.this.isSIMEmailExist(this.id);
                }
                return Integer.valueOf(this.id);
            }

            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                ContactPickerActionListener.this.waiting = false;
                switch (result.intValue()) {
                    case -3:
                        if (!ContactPickerActionListener.this.cancle) {
                            ContactPickerActionListener.this.dialog.setTitle(R.string.replace_email_title);
                            ContactPickerActionListener.this.dialog.show();
                            ContactPickerActionListener.this.dialog.getButton(-1).setEnabled(true);
                            return;
                        }
                        return;
                    case -2:
                    case -1:
                        ContactPickerActionListener.this.dialog.dismiss();
                        ContactSelectionActivity.this.startActivityAndForwardResult(this.intent);
                        return;
                    default:
                        return;
                }
            }
        }

        private ContactPickerActionListener() {
            this.LIMIT_TIME_SHOWDIALOG = 500;
            this.dialog = null;
            this.waiting = true;
            this.cancle = false;
        }

        public void onCreateNewContactAction() {
            ContactSelectionActivity.this.startCreateNewContactActivity();
        }

        private int getSIMContactId(Uri uri) {
            if (uri == null) {
                return -1;
            }
            Cursor cursor = null;
            int id = -1;
            try {
                cursor = ContactSelectionActivity.this.getContentResolver().query(Uri.withAppendedPath(uri, "entities"), new String[]{"_id", "account_type"}, null, null, "raw_contact_id");
                if (cursor == null || !cursor.moveToFirst()) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return -1;
                }
                int id_column = cursor.getColumnIndexOrThrow("_id");
                int account_type_column = cursor.getColumnIndexOrThrow("account_type");
                id = cursor.getInt(id_column);
                String account_type = cursor.getString(account_type_column);
                if (CommonUtilMethods.isSimAccount(account_type)) {
                    return id;
                }
                return -1;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        private int isSIMEmailExist(int contactId) {
            Cursor cursor = null;
            int count = 0;
            try {
                cursor = ContactSelectionActivity.this.getContentResolver().query(Email.CONTENT_URI, null, "contact_id=?", new String[]{contactId + ""}, null);
                if (cursor != null) {
                    count = cursor.getCount();
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (count > 0) {
                    return -3;
                }
                return -2;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        public void onEditContactAction(Uri contactLookupUri) {
            Bundle extras = ContactSelectionActivity.this.getIntent().getExtras();
            String newEmail = ContactSelectionActivity.this.getIntent().getStringExtra(Scopes.EMAIL);
            final Intent intent;
            if (launchAddToContactDialog(extras)) {
                intent = new Intent("android.intent.action.EDIT", contactLookupUri);
                if (extras != null) {
                    extras.remove("name");
                    intent.putExtras(extras);
                    if (TextUtils.isEmpty(newEmail)) {
                        ContactSelectionActivity.this.startActivityAndForwardResult(intent);
                        return;
                    }
                    this.waiting = true;
                    this.cancle = false;
                    final GetSimContactIdTask task = new GetSimContactIdTask(intent);
                    this.dialog = new Builder(ContactSelectionActivity.this).setTitle(R.string.contact_list_loading).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ContactSelectionActivity.this.startActivityAndForwardResult(intent);
                        }
                    }).setNegativeButton(17039369, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ContactPickerActionListener.this.cancle = true;
                            task.cancel(true);
                        }
                    }).create();
                    this.dialog.show();
                    this.dialog.dismiss();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            if (ContactPickerActionListener.this.waiting) {
                                ContactPickerActionListener.this.dialog.show();
                                ContactPickerActionListener.this.dialog.getButton(-1).setEnabled(false);
                            }
                        }
                    }, this.LIMIT_TIME_SHOWDIALOG);
                    task.execute(new Uri[]{contactLookupUri});
                    return;
                }
                return;
            }
            intent = new Intent("android.intent.action.EDIT", contactLookupUri);
            intent.setPackage("com.android.contacts");
            ContactSelectionActivity.this.startActivityAndForwardResult(intent);
        }

        public void onPickContactAction(Uri contactUri) {
            ContactSelectionActivity.this.returnPickerResult(contactUri);
        }

        public void onShortcutIntentCreated(Intent intent) {
            ContactSelectionActivity.this.returnPickerResult(intent);
        }

        private boolean launchAddToContactDialog(Bundle extras) {
            boolean z = true;
            if (extras == null) {
                return false;
            }
            Set<String> intentExtraKeys = Sets.newHashSet();
            intentExtraKeys.addAll(extras.keySet());
            if (intentExtraKeys.contains("name")) {
                intentExtraKeys.remove("name");
            }
            if (intentExtraKeys.contains("handle_create_new_contact")) {
                intentExtraKeys.remove("handle_create_new_contact");
            }
            int numIntentExtraKeys = intentExtraKeys.size();
            if (numIntentExtraKeys == 2) {
                boolean hasPhone;
                boolean hasEmail;
                if (!intentExtraKeys.contains("phone")) {
                    hasPhone = false;
                } else if (intentExtraKeys.contains("phone_type")) {
                    hasPhone = true;
                } else {
                    hasPhone = intentExtraKeys.contains(SearchIntents.EXTRA_QUERY);
                }
                if (!intentExtraKeys.contains(Scopes.EMAIL)) {
                    hasEmail = false;
                } else if (intentExtraKeys.contains("email_type")) {
                    hasEmail = true;
                } else {
                    hasEmail = intentExtraKeys.contains(SearchIntents.EXTRA_QUERY);
                }
                if (hasPhone) {
                    hasEmail = true;
                }
                return hasEmail;
            } else if (numIntentExtraKeys != 1) {
                return false;
            } else {
                if (!intentExtraKeys.contains("phone")) {
                    z = intentExtraKeys.contains(Scopes.EMAIL);
                }
                return z;
            }
        }
    }

    private final class EmailAddressPickerActionListener implements OnEmailAddressPickerActionListener {
        private EmailAddressPickerActionListener() {
        }

        public void onPickEmailAddressAction(Uri dataUri) {
            ContactSelectionActivity.this.returnPickerResult(dataUri);
        }
    }

    private final class PhoneNumberPickerActionListener implements OnPhoneNumberPickerActionListener {
        private PhoneNumberPickerActionListener() {
        }

        public void onPickPhoneNumberAction(Uri dataUri) {
            ContactSelectionActivity.this.returnPickerResult(dataUri);
        }

        public void onShortcutIntentCreated(Intent intent) {
            ContactSelectionActivity.this.returnPickerResult(intent);
        }

        public void onHomeInActionBarSelected() {
            ContactSelectionActivity.this.onBackPressed();
        }
    }

    private final class PostalAddressPickerActionListener implements OnPostalAddressPickerActionListener {
        private PostalAddressPickerActionListener() {
        }

        public void onPickPostalAddressAction(Uri dataUri) {
            ContactSelectionActivity.this.returnPickerResult(dataUri);
        }
    }

    public static class TranslucentActivity extends ContactSelectionActivity {
    }

    public ContactSelectionActivity() {
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mHwCust = (HwCustContactSelectionActivity) HwCustUtils.createObj(HwCustContactSelectionActivity.class, new Object[0]);
        }
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsCust = new RcsContactSelectionActivityHelp();
        }
    }

    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ContactEntryListFragment) {
            this.mListFragment = (ContactEntryListFragment) fragment;
            setupActionListener();
        }
    }

    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        if (this.mHwCust == null) {
            this.mHwCust = (HwCustContactSelectionActivity) HwCustUtils.createObj(HwCustContactSelectionActivity.class, new Object[0]);
        }
        if (EmuiFeatureManager.isRcsFeatureEnable() && this.mRcsCust == null) {
            this.mRcsCust = new RcsContactSelectionActivityHelp();
        }
        if (!handleIntent()) {
            this.isLaunchedFromDetail = getIntent().getBooleanExtra("SPLIT_INTENT_KEY_IS_FROM_DETAIL", false);
            this.isLaunchedFromDialpad = getIntent().getBooleanExtra("intent_key_is_from_dialpad", false);
            boolean z = (CommonUtilMethods.calcIfNeedSplitScreen() && ScreenUtils.isLandscape(this)) ? !this.isLaunchedFromDetail ? this.isLaunchedFromDialpad : true : false;
            this.mIsNeedUpdateWindows = z;
            if (this.mIsNeedUpdateWindows) {
                setTheme(R.style.ContactPickerSelectorTheme);
            } else {
                setTheme(R.style.ContactPickerTheme);
            }
            if (savedState != null) {
                this.mActionCode = savedState.getInt("actionCode");
            }
            this.mRequest = this.mIntentResolver.resolveIntent(getIntent());
            if (this.mRequest.isValid()) {
                Intent redirect = this.mRequest.getRedirectIntent();
                if (redirect != null) {
                    startActivity(redirect);
                    finish();
                    return;
                }
                configureActivityTitle();
                setContentView(R.layout.contact_picker);
                if (this.mIsNeedUpdateWindows) {
                    getActionBar().hide();
                    this.mActionBarTitle = new ActionBarTitle(getApplicationContext(), findViewById(R.id.edit_layout));
                    findViewById(R.id.edit_layout).setVisibility(0);
                    this.mActionBarTitle.setTitle(getString(R.string.contactPickerActivityTitle));
                    this.mActionBarTitle.setBackIcon(true, null, new OnClickListener() {
                        public void onClick(View v) {
                            ContactSelectionActivity.this.onBackPressed();
                        }
                    });
                    if (CommonUtilMethods.isLayoutRTL()) {
                        overridePendingTransition(R.anim.slide_in_left, 0);
                    }
                    updateWindowsParams();
                    updateContentwidth();
                }
                if (this.mActionCode != this.mRequest.getActionCode()) {
                    this.mActionCode = this.mRequest.getActionCode();
                    if (!configureListFragment()) {
                        finish();
                        return;
                    } else if (!this.mListFragment.getIfExcludePrivateContacts()) {
                        this.mListFragment.setExcludePrivateContacts(getIntent().getBooleanExtra("exclude_private_contacts", false));
                    }
                }
                prepareSearchViewAndActionBar();
                this.mCreateNewContactButton = findViewById(R.id.new_contact);
                if (this.mCreateNewContactButton != null) {
                    if (shouldShowCreateNewContactButton()) {
                        this.mCreateNewContactButton.setVisibility(0);
                        this.mCreateNewContactButton.setOnClickListener(this);
                    } else {
                        this.mCreateNewContactButton.setVisibility(8);
                    }
                }
                return;
            }
            setResult(0);
            finish();
        }
    }

    protected void onResume() {
        if (this.mIsNeedUpdateWindows) {
            updateWindowsParams();
        }
        super.onResume();
    }

    private boolean shouldShowCreateNewContactButton() {
        if (!getIntent().getBooleanExtra("handle_create_new_contact", true)) {
            return false;
        }
        if (this.mActionCode == 80) {
            return true;
        }
        if (this.mActionCode != 70 || this.mRequest.isSearchMode()) {
            return false;
        }
        return true;
    }

    private void prepareSearchViewAndActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.select_contact_label);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (shouldShowCreateNewContactButton() && this.mCreateNewContactButton == null) {
            getMenuInflater().inflate(R.menu.contact_picker_options, menu);
            ViewUtil.setMenuItemStateListIcon(getApplicationContext(), menu.findItem(R.id.create_new_contact));
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                setResult(0);
                finish();
                return true;
            case R.id.create_new_contact:
                startCreateNewContactActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("actionCode", this.mActionCode);
    }

    private void configureActivityTitle() {
        if (this.mRequest.getActivityTitle() != null) {
            setTitle(this.mRequest.getActivityTitle());
            return;
        }
        int actionCode = this.mRequest.getActionCode();
        switch (actionCode) {
            case Place.TYPE_MEAL_DELIVERY /*60*/:
                setTitle(R.string.contactPickerActivityTitle);
                break;
            case Place.TYPE_PARKING /*70*/:
                setTitle(R.string.contactPickerActivityTitle);
                break;
            case Place.TYPE_ROOFING_CONTRACTOR /*80*/:
                setTitle(R.string.contactPickerActivityTitle);
                break;
            case Place.TYPE_SYNAGOGUE /*90*/:
                setTitle(R.string.contactPickerActivityTitle);
                break;
            case LocationRequest.PRIORITY_HIGH_ACCURACY /*100*/:
                setTitle(R.string.contactPickerActivityTitle);
                break;
            case LocationRequest.PRIORITY_NO_POWER /*105*/:
                setTitle(R.string.contactPickerActivityTitle);
                break;
            case 110:
                setTitle(R.string.shortcutActivityTitle);
                break;
            case 120:
                setTitle(R.string.callShortcutActivityTitle);
                break;
            case 130:
                setTitle(R.string.messageShortcutActivityTitle);
                break;
            default:
                HwLog.w("ContactSelectionActivity", "Unknown action code: " + actionCode);
                break;
        }
    }

    private boolean isSimSupported(Bundle bundle) {
        if (bundle == null) {
            return false;
        }
        if ((Mms.isEmailAddress(bundle.getString("phone")) || bundle.containsKey(Scopes.EMAIL)) && !CommonUtilMethods.isGroupEmailSupported("com.android.huawei.sim", null, this)) {
            return false;
        }
        return true;
    }

    public boolean configureListFragment() {
        ContactPickerFragment fragment;
        boolean mExcludePrivateContactsWithToast;
        PhoneNumberPickerFragment fragment2;
        switch (this.mActionCode) {
            case Place.TYPE_MEAL_DELIVERY /*60*/:
                fragment = new ContactPickerFragment();
                if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                    mExcludePrivateContactsWithToast = getIntent().getBooleanExtra("has_privacy_contacts", true);
                    if (HwLog.HWDBG) {
                        HwLog.d("ContactSelectionActivity", "mExcludePrivateContactsWithToast::" + mExcludePrivateContactsWithToast);
                    }
                    fragment.setExcludePrivateContactsWithToast(mExcludePrivateContactsWithToast);
                }
                fragment.setIncludeProfile(this.mRequest.shouldIncludeProfile());
                fragment.setExcludeSimAndReadOnly(getIntent().getBooleanExtra("PICK_CONTACT_PHOTO", false));
                fragment.setExcludeSim1AndReadOnly(getIntent().getBooleanExtra("PICK_CONTACT_PHOTO", false));
                fragment.setExcludeSim2AndReadOnly(getIntent().getBooleanExtra("PICK_CONTACT_PHOTO", false));
                fragment.setIgnoreShowSimContactsPref(true);
                this.mListFragment = fragment;
                break;
            case Place.TYPE_PARKING /*70*/:
                fragment = new ContactPickerFragment();
                fragment.setExcludeSimAndReadOnly(getIntent().getBooleanExtra("PICK_CONTACT_PHOTO", false));
                fragment.setExcludeSim1AndReadOnly(getIntent().getBooleanExtra("PICK_CONTACT_PHOTO", false));
                fragment.setExcludeSim2AndReadOnly(getIntent().getBooleanExtra("PICK_CONTACT_PHOTO", false));
                this.mListFragment = fragment;
                break;
            case Place.TYPE_ROOFING_CONTRACTOR /*80*/:
                fragment = new ContactPickerFragment();
                fragment.setEditMode(true);
                fragment.setDirectorySearchMode(0);
                if (SimFactoryManager.isDualSim()) {
                    Bundle bundle = getIntent().getExtras();
                    SimFactory firstSimFactory = SimFactoryManager.getSimFactory(0);
                    if (bundle == null || !((!Mms.isEmailAddress(bundle.getString("phone")) && !bundle.containsKey(Scopes.EMAIL)) || firstSimFactory == null || firstSimFactory.getSimConfig().isEmailEnabled())) {
                        fragment.setExcludeSim1AndReadOnly(true);
                    } else {
                        fragment.setExcludeReadOnly(true);
                    }
                    SimFactory secondSimFactory = SimFactoryManager.getSimFactory(1);
                    if (bundle == null || !((!Mms.isEmailAddress(bundle.getString("phone")) && !bundle.containsKey(Scopes.EMAIL)) || secondSimFactory == null || secondSimFactory.getSimConfig().isEmailEnabled())) {
                        fragment.setExcludeSim2AndReadOnly(true);
                    } else {
                        fragment.setExcludeReadOnly(true);
                    }
                } else if (isSimSupported(getIntent().getExtras())) {
                    fragment.setExcludeReadOnly(true);
                } else {
                    fragment.setExcludeSimAndReadOnly(true);
                }
                this.mListFragment = fragment;
                break;
            case Place.TYPE_SYNAGOGUE /*90*/:
                if (!getIntent().getBooleanExtra("speed_dial", false)) {
                    this.mListFragment = new PhoneNumberPickerFragment();
                    break;
                }
                SpeedDialContactPickerFragment fragment3 = new SpeedDialContactPickerFragment();
                fragment3.setContactsAddedInSpeedDial(getIntent().getStringArrayListExtra("contacts_added"));
                this.mListFragment = fragment3;
                break;
            case LocationRequest.PRIORITY_HIGH_ACCURACY /*100*/:
                this.mListFragment = new PostalAddressPickerFragment();
                break;
            case LocationRequest.PRIORITY_NO_POWER /*105*/:
                this.mListFragment = new EmailAddressPickerFragment();
                break;
            case 110:
                fragment = new ContactPickerFragment();
                fragment.setShortcutRequested(true);
                fragment.setExcludePrivateContacts(false);
                if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                    mExcludePrivateContactsWithToast = getIntent().getBooleanExtra("has_privacy_contacts", true);
                    if (HwLog.HWDBG) {
                        HwLog.d("ContactSelectionActivity", "mExcludePrivateContactsWithToast::" + mExcludePrivateContactsWithToast);
                    }
                    fragment.setExcludePrivateContactsWithToast(mExcludePrivateContactsWithToast);
                }
                fragment.setIgnoreShowSimContactsPref(true);
                this.mListFragment = fragment;
                break;
            case 120:
                fragment2 = new PhoneNumberPickerFragment();
                fragment2.setShortcutAction("com.android.contacts.action.CHOOSE_SUB_HUAWEI");
                fragment2.setExcludePrivateContacts(false);
                this.mListFragment = fragment2;
                break;
            case 130:
                fragment2 = new PhoneNumberPickerFragment();
                fragment2.setShortcutAction("android.intent.action.SENDTO");
                fragment2.setExcludePrivateContacts(false);
                this.mListFragment = fragment2;
                break;
            default:
                HwLog.e("ContactSelectionActivity", "Invalid action code: " + this.mActionCode);
                return false;
        }
        this.mListFragment.setLegacyCompatibilityMode(this.mRequest.isLegacyCompatibilityMode());
        this.mListFragment.setDirectoryResultLimit(20);
        getFragmentManager().beginTransaction().replace(R.id.list_container, this.mListFragment).commitAllowingStateLoss();
        return true;
    }

    public void setupActionListener() {
        if (this.mListFragment instanceof ContactPickerFragment) {
            ((ContactPickerFragment) this.mListFragment).setOnContactPickerActionListener(new ContactPickerActionListener());
        } else if (this.mListFragment instanceof PhoneNumberPickerFragment) {
            ((PhoneNumberPickerFragment) this.mListFragment).setOnPhoneNumberPickerActionListener(new PhoneNumberPickerActionListener());
        } else if (this.mListFragment instanceof PostalAddressPickerFragment) {
            ((PostalAddressPickerFragment) this.mListFragment).setOnPostalAddressPickerActionListener(new PostalAddressPickerActionListener());
        } else if (this.mListFragment instanceof EmailAddressPickerFragment) {
            ((EmailAddressPickerFragment) this.mListFragment).setOnEmailAddressPickerActionListener(new EmailAddressPickerActionListener());
        } else {
            throw new IllegalStateException("Unsupported list fragment type: " + this.mListFragment);
        }
    }

    public void startActivityAndForwardResult(Intent intent) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if ("android.intent.action.EDIT".equals(intent.getAction()) && extras.containsKey("name")) {
                extras.remove("name");
            }
            intent.putExtras(extras);
        }
        if (getIntent().getBooleanExtra("PICK_CONTACT_PHOTO", false)) {
            intent.putExtra("PICK_CONTACT_PHOTO", true);
            startActivityForResult(intent, 1);
            return;
        }
        if (80 == this.mActionCode) {
            if (SimFactoryManager.isDualSim()) {
                SimFactory firstSimFactory = SimFactoryManager.getSimFactory(0);
                SimFactory secondSimFactory = SimFactoryManager.getSimFactory(1);
                Bundle bundle = getIntent().getExtras();
                try {
                    if (!((!Mms.isEmailAddress(bundle.getString("phone")) && !bundle.containsKey(Scopes.EMAIL)) || firstSimFactory == null || firstSimFactory.getSimConfig().isEmailEnabled())) {
                        intent.putExtra("EXCLUDE_SIM1", true);
                    }
                } catch (Exception e) {
                    intent.putExtra("EXCLUDE_SIM1", true);
                }
                try {
                    if (!((!Mms.isEmailAddress(bundle.getString("phone")) && !bundle.containsKey(Scopes.EMAIL)) || secondSimFactory == null || secondSimFactory.getSimConfig().isEmailEnabled())) {
                        intent.putExtra("EXCLUDE_SIM2", true);
                    }
                } catch (Exception e2) {
                    intent.putExtra("EXCLUDE_SIM2", true);
                }
            } else if (!isSimSupported(getIntent().getExtras())) {
                intent.putExtra("EXCLUDE_SIM", true);
            }
        }
        intent.setFlags(33554432);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e3) {
            e3.printStackTrace();
        }
        finish();
    }

    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuAdapter menuAdapter = this.mListFragment.getContextMenuAdapter();
        if (menuAdapter != null) {
            return menuAdapter.onContextItemSelected(item);
        }
        return super.onContextItemSelected(item);
    }

    public void returnPickerResult(Uri data) {
        Intent intent = new Intent();
        intent.setData(data);
        if (!(getIntent() == null || getIntent().getExtras() == null)) {
            intent.putExtras(getIntent().getExtras());
        }
        returnPickerResult(intent);
    }

    public void returnPickerResult(Intent intent) {
        intent.setFlags(1);
        setResult(-1, intent);
        finish();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_contact:
                startCreateNewContactActivity();
                return;
            default:
                return;
        }
    }

    private void startCreateNewContactActivity() {
        Intent intent = new Intent("android.intent.action.INSERT", Contacts.CONTENT_URI);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        intent.setPackage("com.android.contacts");
        startActivityAndForwardResult(intent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null) {
            returnPickerResult(data);
        }
    }

    private boolean handleIntent() {
        Intent intent = getIntent();
        boolean isMultipick = intent.getBooleanExtra("com.huawei.community.action.MULTIPLE_PICK", false);
        if (isMultipick) {
            String type = intent.getType();
            Intent contactIntent = new Intent(intent);
            if (!intent.getBooleanExtra("show_group_tab", true)) {
                contactIntent.setAction("com.huawei.community.action.MULTIPLE_PICK");
            } else if (CommonUtilMethods.isSimpleModeOn()) {
                contactIntent.setClass(this, ContactsMultiSelectMessageActivitySimplified.class);
                contactIntent.setAction("com.huawei.community.action.MULTIPLE_PICK");
            } else {
                contactIntent.setClass(this, ContactAndGroupMultiSelectionActivity.class);
                if (this.mRcsCust != null) {
                    contactIntent = this.mRcsCust.getRcsContactIntent(intent, contactIntent);
                }
                contactIntent.setAction("com.huawei.community.action.MULTIPLE_PICK");
            }
            contactIntent.setType(type);
            contactIntent.setFlags(33554432);
            contactIntent.putExtra("com.huawei.community.action.MULTIPLE_PICK", true);
            startActivity(contactIntent);
            finish();
        }
        return isMultipick;
    }

    public void setUpFragment() {
        this.mSearchLayout = this.mListFragment.getView().findViewById(R.id.inner_contactListsearchlayout);
        this.mContactsSearchView = (EditText) this.mListFragment.getView().findViewById(R.id.search_view);
        ContactsUtils.configureSearchViewInputType(this.mContactsSearchView);
        final ImageView searchClearButton = (ImageView) this.mListFragment.getView().findViewById(R.id.clearSearchResult);
        this.mContactsSearchView.setHint(CommonUtilMethods.getSearchViewSpannableHint(this, getResources().getString(R.string.contact_hint_findContacts), this.mContactsSearchView.getTextSize()));
        this.mContactsSearchView.setCustomSelectionActionModeCallback(this);
        this.mContactsSearchView.setCursorVisible(false);
        this.mContactsSearchView.setOnTouchListener(this.touchListener);
        searchClearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ContactSelectionActivity.this.mListFragment.setSearchMode(false);
                ContactSelectionActivity.this.mContactsSearchView.setText(null);
            }
        });
        this.mContactsSearchView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = null;
                if (s == null || TextUtils.isEmpty(s.toString().trim())) {
                    ContactSelectionActivity.this.mListFragment.setSearchModeInitialized(false);
                    ContactSelectionActivity.this.mListFragment.setSearchMode(false);
                    ContactSelectionActivity.this.mListFragment.setIncludeProfile(true);
                    ContactEntryListFragment contactEntryListFragment = ContactSelectionActivity.this.mListFragment;
                    if (s != null) {
                        str = s.toString();
                    }
                    contactEntryListFragment.setQueryString(str, true);
                    searchClearButton.setVisibility(8);
                    return;
                }
                ContactSelectionActivity.this.mListFragment.setSearchModeInitialized(true);
                ContactSelectionActivity.this.mListFragment.setSearchMode(true);
                ContactSelectionActivity.this.mListFragment.setIncludeProfile(false);
                ContactSelectionActivity.this.mListFragment.setQueryString(s.toString(), true);
                searchClearButton.setVisibility(0);
            }
        });
    }

    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    public void onDestroyActionMode(ActionMode mode) {
    }

    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    protected void onPause() {
        super.onPause();
        if (this.mHwCust != null) {
            this.mHwCust.fingerPrintBindContactsFinish(this);
        }
        if (this.mContactsSearchView != null && CommonUtilMethods.calcIfNeedSplitScreen()) {
            hideSoftInput();
        }
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService("input_method");
        if (imm != null && imm.isActive()) {
            imm.hideSoftInputFromWindow(this.mContactsSearchView.getWindowToken(), 0);
        }
    }

    public void finish() {
        super.finish();
        if (this.mIsNeedUpdateWindows && CommonUtilMethods.isLayoutRTL()) {
            overridePendingTransition(0, R.anim.slide_out_left);
        }
    }

    private void updateWindowsParams() {
        Window window = getWindow();
        LayoutParams params = window.getAttributes();
        params.gravity = 80;
        params.flags = 32;
        window.setFlags(262144, 262144);
        boolean isLandscape = 2 == getResources().getConfiguration().orientation;
        boolean isNaviBarEnabled = isNaviBarEnabled(getContentResolver());
        boolean isNavOnBotoom = SystemProperties.getInt("ro.panel.hw_orientation", 0) == 90;
        if (isLandscape && isNaviBarEnabled && isNavOnBotoom) {
            params.height = ContactDpiAdapter.getActivityContentHeight(this) - ContactDpiAdapter.getNavigationBarHeight(this);
        } else {
            params.height = ContactDpiAdapter.getActivityContentHeight(this);
        }
        window.setAttributes(params);
    }

    private void updateContentwidth() {
        LinearLayout contactPicker = (LinearLayout) findViewById(R.id.contact_picker_UpdateWindows);
        if (contactPicker != null) {
            contactPicker.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1.0f));
        }
        View grayView = findViewById(R.id.gray_view);
        grayView.setVisibility(0);
        grayView.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                ContactSelectionActivity.this.finish();
            }
        });
    }

    private boolean isNaviBarEnabled(ContentResolver resolver) {
        boolean z = true;
        int NAVI_BAR_DEFAULT_STATUS = 1;
        if (!FRONT_FINGERPRINT_NAVIGATION) {
            return true;
        }
        if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0) {
            if (IS_CHINA_AREA) {
                NAVI_BAR_DEFAULT_STATUS = 0;
            } else {
                NAVI_BAR_DEFAULT_STATUS = 1;
            }
        } else if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
            return false;
        }
        if (System.getIntForUser(resolver, "enable_navbar", NAVI_BAR_DEFAULT_STATUS, ActivityManager.getCurrentUser()) != 1) {
            z = false;
        }
        return z;
    }
}
