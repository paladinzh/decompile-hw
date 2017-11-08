package com.android.contacts.hap.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.detail.ContactLoaderFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.group.GroupMultiseletionFragment;
import com.android.contacts.hap.list.ContactDataMultiSelectFragment;
import com.android.contacts.hap.list.ContactMultiselectionFragment;
import com.android.contacts.hap.list.ContactMultiselectionFragment.MultiSelectListener;
import com.android.contacts.hap.list.ContactsMultiSelectSearchFragment;
import com.android.contacts.hap.list.FavoritesFrequentDataMultiSelectFragment;
import com.android.contacts.hap.list.FavoritesFrequentMultiSelectFragment;
import com.android.contacts.hap.list.RawContactMultiSelectFragment;
import com.android.contacts.hap.rcs.activities.RcsContactMultiSelectionActivityHelp;
import com.android.contacts.hap.sim.SimConfig;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactsIntentResolver;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.util.HwLog;
import com.android.internal.app.WindowDecorActionBar;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.HashSet;
import java.util.Set;

public class ContactMultiSelectionActivity extends Activity implements MultiSelectListener {
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final boolean IS_CHINA_AREA = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    public static final String TAG = ContactMultiSelectionActivity.class.getSimpleName();
    public BroadcastReceiver finishReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ContactMultiSelectionActivity.this.finish();
            if (CommonUtilMethods.isLayoutRTL()) {
                ContactMultiSelectionActivity.this.overridePendingTransition(0, R.anim.slide_fast_out_left);
            } else {
                ContactMultiSelectionActivity.this.overridePendingTransition(0, R.anim.slide_fast_out_right);
            }
        }
    };
    public boolean isExpectIntegerListBack = false;
    private int mActionCode = -1;
    public int mCallLogCount = 0;
    private ContactLoaderFragment mContactDetailLoaderFragment;
    private String mDeleteAccountType;
    private int mGravityLeft = 0;
    private boolean mHasPlayedExitAnimation = false;
    private boolean mIsNeedUpdateWindows;
    private boolean mIsSelectAllWithDef;
    private float mLandscapeWindowWidth = 0.0f;
    private LocalBroadcastManager mLbm;
    public int mMaxLimit = -1;
    public ContactEntryListFragment<ContactEntryListAdapter> mMultiSelectFragment;
    private RcsContactMultiSelectionActivityHelp mRcsCust = null;
    protected ContactsRequest mRequest;
    public Set<Long> mSelectedContactId = new HashSet();
    public Set<String> mSelectedData = new HashSet();
    public Set<Uri> mSelectedDataUris = new HashSet();
    private float mVertivalWindowWidth = 0.0f;

    private class ClickListener implements OnClickListener {
        private String mAccountType;

        ClickListener(String accountType) {
            this.mAccountType = accountType;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (-1 == which) {
                Intent lSimContactsDelete = new Intent();
                lSimContactsDelete.setAction("android.intent.action.HAP_DELETE_CONTACTS");
                lSimContactsDelete.putExtra("accountType", this.mAccountType);
                ContactMultiSelectionActivity.this.startActivity(lSimContactsDelete);
            }
        }
    }

    private static class DialogOnClickListener implements OnClickListener {
        private DialogOnClickListener() {
        }

        public void onClick(DialogInterface aDialogInterface, int which) {
            aDialogInterface.dismiss();
        }
    }

    public static class TranslucentActivity extends ContactMultiSelectionActivity {
        public void finish() {
            super.finish();
            CommonUtilMethods.clearInstanceState();
        }
    }

    public RcsContactMultiSelectionActivityHelp getRcsCust() {
        return this.mRcsCust;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        PLog.d(23, "ContactMultiSelectionActivity onCreate");
        getWindow().setFlags(16777216, 16777216);
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsCust = new RcsContactMultiSelectionActivityHelp();
        }
        setTheme(R.style.MultiSelectTheme);
        if (savedInstanceState != null) {
            this.mGravityLeft = savedInstanceState.getInt("gravity_left", 0);
            this.mLandscapeWindowWidth = savedInstanceState.getFloat("landscape_window_width", 0.0f);
            this.mVertivalWindowWidth = savedInstanceState.getFloat("vertical_window_width", 0.0f);
        } else {
            this.mGravityLeft = getIntent().getIntExtra("gravity_left", 0);
            this.mLandscapeWindowWidth = getIntent().getFloatExtra("landscape_window_width", 0.0f);
            this.mVertivalWindowWidth = getIntent().getFloatExtra("vertical_window_width", 0.0f);
        }
        if (CommonUtilMethods.calcIfNeedSplitScreen() && this.mGravityLeft == 0) {
            boolean isPortrait;
            if (1 == getResources().getConfiguration().orientation) {
                isPortrait = true;
            } else {
                isPortrait = false;
            }
            if (isPortrait && this.mVertivalWindowWidth > 0.0f) {
                updateWindowsParams(this.mVertivalWindowWidth);
                this.mIsNeedUpdateWindows = true;
            } else if (!isPortrait && this.mLandscapeWindowWidth > 0.0f) {
                updateWindowsParams(this.mLandscapeWindowWidth);
                this.mIsNeedUpdateWindows = true;
            }
        }
        setContentView(getViewToUse());
        this.mRequest = new ContactsIntentResolver(this).resolveIntent(getIntent());
        if (this.mRequest.isValid()) {
            if (savedInstanceState != null) {
                this.mDeleteAccountType = savedInstanceState.getString("accountType");
            }
            configureTransferAndLimit();
            configureSelectAllMode();
            addMultiFragmentPropertiesIfMultiTabbed();
            if (configureListFragment(savedInstanceState)) {
                if (this.mRcsCust != null) {
                    this.mRcsCust.addUserToMemberList(this, getIntent());
                }
                this.mLbm = LocalBroadcastManager.getInstance(this);
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.FINISH_MULTI_SELECTED");
                if (this.finishReceiver != null) {
                    this.mLbm.registerReceiver(this.finishReceiver, filter);
                }
                return;
            }
            finish();
            return;
        }
        setResult(0);
        finish();
    }

    private void updateWindowsParams(float width) {
        Window window = getWindow();
        LayoutParams params = window.getAttributes();
        boolean isPortrait = 1 == getResources().getConfiguration().orientation;
        if (CommonUtilMethods.isLayoutRTL()) {
            overridePendingTransition(R.anim.slide_fast_in_left, 0);
            params.gravity = 83;
            params.width = (int) width;
        } else {
            overridePendingTransition(R.anim.slide_fast_in_right, 0);
            params.gravity = 85;
            int navBarHeight = 0;
            boolean havNavbar = isNaviBarEnabled(getContentResolver());
            boolean isNavOnBotoom = SystemProperties.getInt("ro.panel.hw_orientation", 0) == 90;
            if (!(isPortrait || !havNavbar || isNavOnBotoom)) {
                navBarHeight = ContactDpiAdapter.getNavigationBarHeight(this);
            }
            params.width = ((int) width) + navBarHeight;
        }
        params.height = ContactDpiAdapter.getActivityContentHeight(this);
        params.flags = 32;
        window.setFlags(262144, 262144);
        window.setCloseOnTouchOutside(true);
        window.setAttributes(params);
        window.setSplitActionBarAlways(true);
        if (!isPortrait) {
            disableActionBarAnimation();
        }
    }

    private void disableActionBarAnimation() {
        ActionBar actionBar = getActionBar();
        if (actionBar instanceof WindowDecorActionBar) {
            ((WindowDecorActionBar) actionBar).setAnimationEnable(false);
        }
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

    private void configureTransferAndLimit() {
        int i = 10000;
        this.isExpectIntegerListBack = getIntent().getBooleanExtra("com.huawei.community.action.EXPECT_INTEGER_LIST", false);
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "isExpectItegerListBack :" + this.isExpectIntegerListBack);
        }
        if (this.isExpectIntegerListBack) {
            this.mMaxLimit = getIntent().getIntExtra("com.huawei.community.action.MAX_SELECT_COUNT", 10000);
            if (this.mMaxLimit <= 10000) {
                i = this.mMaxLimit;
            }
            this.mMaxLimit = i;
        } else {
            this.mMaxLimit = getIntent().getIntExtra("com.huawei.community.action.MAX_SELECT_COUNT", VTMCDataCache.MAXSIZE);
            this.mMaxLimit = this.mMaxLimit > VTMCDataCache.MAXSIZE ? VTMCDataCache.MAXSIZE : this.mMaxLimit;
        }
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "mMaxLimit :" + this.mMaxLimit);
        }
    }

    private void configureSelectAllMode() {
        String lAction = getIntent().getAction();
        boolean isGroupMes = "android.intent.action.HAP_SEND_GROUP_MESSAGE".equals(lAction);
        boolean isGroupMal = "android.intent.action.HAP_SEND_GROUP_MAIL".equals(lAction);
        boolean isRcsGroupMessage = VTMCDataCache.MAX_EXPIREDTIME == this.mRequest.getActionCode();
        boolean isSelFromMesOrMal = false;
        if ("com.huawei.community.action.MULTIPLE_PICK".equals(lAction)) {
            isSelFromMesOrMal = (getIntent().getBooleanExtra("Launch_BlackList_Multi_Pick", false) || getIntent().getBooleanExtra("Launch_WhiteList_Multi_Pick", false)) ? false : true;
        }
        if (isGroupMes || isGroupMal || isSelFromMesOrMal) {
            isRcsGroupMessage = true;
        }
        this.mIsSelectAllWithDef = isRcsGroupMessage;
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "mIsSelectAllWithDef :" + this.mIsSelectAllWithDef);
        }
    }

    public boolean checkIsSelectAllWithDef() {
        return this.mIsSelectAllWithDef;
    }

    protected boolean configureListFragment(Bundle args) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        this.mMultiSelectFragment = (ContactEntryListFragment) fragmentManager.findFragmentByTag("multiselect-tag");
        if (this.mMultiSelectFragment == null) {
            this.mActionCode = this.mRequest.getActionCode();
            switch (this.mActionCode) {
                case 144:
                case 201:
                case 202:
                case 204:
                case 205:
                case 217:
                    if (1 != this.mGravityLeft) {
                        if (!getIntent().getBooleanExtra("contact_multi_select_for_stequent", false)) {
                            this.mMultiSelectFragment = new ContactMultiselectionFragment();
                            break;
                        }
                        this.mMultiSelectFragment = new FavoritesFrequentMultiSelectFragment();
                        break;
                    } else if (!getIntent().getBooleanExtra("contact_multi_select_for_stequent", false)) {
                        this.mMultiSelectFragment = new ContactMultiselectionFragment(this.mLandscapeWindowWidth, this.mVertivalWindowWidth);
                        break;
                    } else {
                        this.mMultiSelectFragment = new FavoritesFrequentMultiSelectFragment(this.mLandscapeWindowWidth, this.mVertivalWindowWidth);
                        break;
                    }
                case 203:
                    int missingItemIndex = -1;
                    if (args == null) {
                        this.mDeleteAccountType = getIntent().getStringExtra("accountType");
                        missingItemIndex = getIntent().getIntExtra("missingItemIndex", -1);
                    }
                    this.mMultiSelectFragment = new ContactMultiselectionFragment(missingItemIndex);
                    break;
                case 206:
                case 207:
                case 218:
                    this.mMultiSelectFragment = new RawContactMultiSelectFragment();
                    break;
                case 208:
                case 209:
                case 213:
                case 214:
                case 215:
                case 216:
                case 221:
                    if (!getIntent().getBooleanExtra("contact_multi_select_for_stequent", false)) {
                        this.mMultiSelectFragment = (ContactDataMultiSelectFragment) getFragmentToLoad();
                        break;
                    }
                    this.mMultiSelectFragment = new FavoritesFrequentDataMultiSelectFragment();
                    break;
                case 210:
                case 211:
                case 212:
                    this.mMultiSelectFragment = new ContactsMultiSelectSearchFragment();
                    break;
                case 219:
                    this.mMultiSelectFragment = new GroupMultiseletionFragment();
                    break;
                case 220:
                    this.mMultiSelectFragment = new ContactMultiselectionFragment();
                    break;
                default:
                    if (this.mRcsCust == null || !this.mRcsCust.configureListFragment(this, this.mActionCode)) {
                        HwLog.e(TAG, "Invalid action code: " + this.mActionCode);
                        return false;
                    }
            }
            transaction.replace(R.id.list_container, this.mMultiSelectFragment, "multiselect-tag");
        }
        if (this.mMultiSelectFragment instanceof ContactMultiselectionFragment) {
            ((ContactMultiselectionFragment) this.mMultiSelectFragment).setListener(this);
        } else if (this.mMultiSelectFragment instanceof ContactDataMultiSelectFragment) {
            ((ContactDataMultiSelectFragment) this.mMultiSelectFragment).setListener(this);
        } else if (this.mMultiSelectFragment instanceof RawContactMultiSelectFragment) {
            ((RawContactMultiSelectFragment) this.mMultiSelectFragment).setListener(this);
        }
        this.mRequest.setSimAccountTypeFilter(this.mDeleteAccountType);
        this.mMultiSelectFragment.setContactsRequest(this.mRequest);
        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.hapmultiselectmenu, menu);
        if (this.mIsNeedUpdateWindows) {
            menu.findItem(R.id.menu_action_operation).setShowAsAction(6);
            menu.findItem(R.id.menu_action_selectall).setShowAsAction(6);
            menu.findItem(R.id.menu_action_cancel).setShowAsAction(6);
        }
        ViewUtil.setMenuItemsStateListIcon(getApplicationContext(), menu);
        return true;
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("gravity_left", this.mGravityLeft);
        outState.putFloat("landscape_window_width", this.mLandscapeWindowWidth);
        outState.putFloat("vertical_window_width", this.mVertivalWindowWidth);
        if (this.mMultiSelectFragment != null) {
            ListView lListView = this.mMultiSelectFragment.getListView();
            if (lListView != null) {
                outState.putInt("selecteditemcount", lListView.getCheckedItemCount());
                outState.putInt("selectedcalllogitemcount", this.mCallLogCount);
                outState.putString("accountType", this.mDeleteAccountType);
            }
        }
    }

    public void setSelectedItemInListview(int[] selectedArray) {
        ListView lListView = this.mMultiSelectFragment.getListView();
        int lHeaderCount = 0;
        if (this.mMultiSelectFragment.isSearchMode()) {
            lHeaderCount = 1;
        }
        if (selectedArray != null) {
            int length = selectedArray.length;
            int lvCount = lListView.getCount();
            int lvSelectLength = lListView.getCheckedItemCount();
            if (length > 0) {
                for (int j = 0; j < lvSelectLength; j++) {
                    lListView.setItemChecked(j + lHeaderCount, false);
                }
                int i = 0;
                while (i < lvCount && i < length) {
                    boolean isChecked;
                    if (selectedArray[i] == 1) {
                        isChecked = true;
                    } else {
                        isChecked = false;
                    }
                    lListView.setItemChecked(i + lHeaderCount, isChecked);
                    i++;
                }
            }
        }
    }

    public int[] getSelectionPositions(Cursor aCursor, int columnIndex, Set<Uri> selectedUris, Uri uriPrefix) {
        int[] iArr = null;
        try {
            int count = aCursor.getCount();
            iArr = new int[count];
            if (!selectedUris.isEmpty()) {
                for (int i = 0; i < count; i++) {
                    aCursor.moveToPosition(i);
                    if (selectedUris.contains(Uri.withAppendedPath(uriPrefix, aCursor.getString(columnIndex)))) {
                        iArr[i] = 1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iArr;
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int selectecount = savedInstanceState.getInt("selecteditemcount");
        this.mCallLogCount = savedInstanceState.getInt("selectedcalllogitemcount");
        this.mDeleteAccountType = savedInstanceState.getString("accountType");
        this.mMultiSelectFragment.showSelectedItemCountInfo(selectecount);
    }

    public ContactEntryListFragment<ContactEntryListAdapter> getFragmentToLoad() {
        return new ContactDataMultiSelectFragment();
    }

    protected int getViewToUse() {
        return R.layout.simple_frame_layout;
    }

    protected void addMultiFragmentPropertiesIfMultiTabbed() {
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        Bundle aBunble = args;
        String simType = args.getString("accountType");
        String lAccName = SimFactoryManager.getSimCardDisplayLabel(simType);
        String message;
        Builder builder;
        View view;
        final Bundle bundle;
        switch (id) {
            case R.id.targetAccountChosen:
                int freeSpaceInSIM = args.getInt("freeSpaceInSIM");
                if (freeSpaceInSIM == 0) {
                    message = getString(R.string.str_copysim_fullandfreeupspace, new Object[]{lAccName, lAccName});
                } else {
                    message = String.format(getResources().getQuantityString(R.plurals.str_copysim_availableandfreeupspace, freeSpaceInSIM), new Object[]{Integer.valueOf(freeSpaceInSIM), lAccName, lAccName});
                }
                builder = new Builder(this).setTitle(getResources().getString(R.string.sim_space_insufficient_title, new Object[]{lAccName})).setPositiveButton(getResources().getString(R.string.button_reorganize_text, new Object[]{lAccName}), new ClickListener(simType)).setNegativeButton(17039360, null);
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(message);
                builder.setView(view);
                return builder.create();
            case R.id.targetAccountChosenEmptySim:
                bundle = args;
                AlertDialog dialog = new Builder(this).setMessage(String.format(getResources().getQuantityString(R.plurals.str_copysim_availablespace, args.getInt("freeSpaceInSIM")), new Object[]{Integer.valueOf(args.getInt("freeSpaceInSIM")), lAccName})).setPositiveButton(R.string.description_save_button, new OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        ContactMultiSelectionActivity.this.showDialog(R.id.copySimInfo, bundle);
                    }
                }).setNegativeButton(17039360, null).create();
                dialog.setMessageNotScrolling();
                return dialog;
            case R.id.copySimInfo:
                SimConfig config = SimFactoryManager.getSimConfig(simType);
                if (config == null) {
                    HwLog.w(TAG, " SimConfig is null ");
                    return super.onCreateDialog(id, args);
                }
                int resId;
                int quantity = config.isANREnabled() ? 2 : 1;
                if (config.isEmailEnabled()) {
                    resId = R.plurals.contact_str_copysim_copynamenumberemail;
                } else {
                    resId = R.plurals.str_copysim_copynamenumber;
                }
                bundle = args;
                builder = new Builder(this).setTitle(R.string.contact_str_copysim_notification).setPositiveButton(R.string.button_continue_text, new OnClickListener() {
                    public void onClick(DialogInterface aDialogInterface, int which) {
                        if (-1 == which) {
                            ContactMultiSelectionActivity.this.startService((Intent) bundle.getParcelable("intent"));
                            ContactMultiSelectionActivity.this.finish();
                        }
                    }
                }).setNegativeButton(17039360, null);
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(getResources().getQuantityString(resId, quantity, new Object[]{Integer.valueOf(quantity), lAccName}));
                builder.setView(view);
                return builder.create();
            case R.id.simDeleteProgress:
                message = String.format(getString(R.string.delete_sim_progress), new Object[]{lAccName});
                builder = new Builder(this).setTitle(R.string.contact_str_copysim_notification).setPositiveButton(R.string.contact_known_button_text, new OnClickListener() {
                    public void onClick(DialogInterface aDialogInterface, int which) {
                        aDialogInterface.dismiss();
                    }
                });
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(message);
                builder.setView(view);
                return builder.create();
            case R.id.simCopyProgress:
                message = String.format(getString(R.string.copy_sim_progress), new Object[]{lAccName});
                builder = new Builder(this).setTitle(R.string.contact_str_copysim_notification).setPositiveButton(R.string.contact_known_button_text, new DialogOnClickListener());
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(message);
                builder.setView(view);
                return builder.create();
            default:
                return super.onCreateDialog(id, args);
        }
    }

    protected void onPrepareDialog(int id, Dialog dialog, final Bundle args) {
        Bundle aBunble = args;
        String simType = args.getString("accountType");
        String mAccName = SimFactoryManager.getSimCardDisplayLabel(simType);
        int freeSpaceInSIM;
        String message;
        View view;
        switch (id) {
            case R.id.targetAccountChosen:
                freeSpaceInSIM = args.getInt("freeSpaceInSIM");
                if (freeSpaceInSIM == 0) {
                    message = getString(R.string.str_copysim_fullandfreeupspace, new Object[]{mAccName, mAccName});
                } else {
                    message = String.format(getResources().getQuantityString(R.plurals.str_copysim_availableandfreeupspace, freeSpaceInSIM), new Object[]{Integer.valueOf(freeSpaceInSIM), mAccName, mAccName});
                }
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(message);
                ((AlertDialog) dialog).setView(view);
                ((AlertDialog) dialog).setTitle(getResources().getString(R.string.sim_space_insufficient_title, new Object[]{mAccName}));
                ((AlertDialog) dialog).setButton(-1, getResources().getString(R.string.button_reorganize_text, new Object[]{mAccName}), new ClickListener(simType));
                break;
            case R.id.targetAccountChosenEmptySim:
                ((AlertDialog) dialog).setMessage(String.format(getResources().getQuantityString(R.plurals.str_copysim_availablespace, args.getInt("freeSpaceInSIM")), new Object[]{Integer.valueOf(freeSpaceInSIM), mAccName}));
                ((AlertDialog) dialog).setMessageNotScrolling();
                ((AlertDialog) dialog).setButton(-1, getString(R.string.description_save_button), new OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        ContactMultiSelectionActivity.this.showDialog(R.id.copySimInfo, args);
                    }
                });
                break;
            case R.id.copySimInfo:
                ((AlertDialog) dialog).setButton(-1, getString(R.string.button_continue_text), new OnClickListener() {
                    public void onClick(DialogInterface aDialogInterface, int which) {
                        if (-1 == which) {
                            if (CommonUtilMethods.isServiceRunning(ContactMultiSelectionActivity.this.getApplicationContext(), "com.android.contacts.hap.copy.CopyContactService")) {
                                ContactMultiSelectionActivity.this.finish();
                            }
                            Intent lIntent = new Intent(ContactMultiSelectionActivity.this.getApplicationContext(), CopyProgressBarActivity.class);
                            lIntent.putExtra("bundle", args);
                            ContactMultiSelectionActivity.this.startActivity(lIntent);
                            ContactMultiSelectionActivity.this.finish();
                        }
                    }
                });
                break;
            case R.id.simDeleteProgress:
                message = String.format(getString(R.string.delete_sim_progress), new Object[]{mAccName});
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(message);
                ((AlertDialog) dialog).setView(view);
                break;
            case R.id.simCopyProgress:
                message = String.format(getString(R.string.copy_sim_progress), new Object[]{mAccName});
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(message);
                ((AlertDialog) dialog).setView(view);
                break;
        }
        super.onPrepareDialog(id, dialog, args);
    }

    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ContactLoaderFragment) {
            ((ContactLoaderFragment) fragment).setIsMultiSelect(true);
        }
    }

    public void onSelectionChanged(Uri aLookUpUri) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (this.mContactDetailLoaderFragment != null) {
            this.mContactDetailLoaderFragment.loadUri(aLookUpUri, false);
            if (this.mContactDetailLoaderFragment.isHidden()) {
                ft.show(this.mContactDetailLoaderFragment);
            }
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    public void finish() {
        if (!this.mHasPlayedExitAnimation && (this.mMultiSelectFragment instanceof ContactMultiselectionFragment) && ((ContactMultiselectionFragment) this.mMultiSelectFragment).beforeActivityFinish()) {
            this.mHasPlayedExitAnimation = true;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    ContactMultiSelectionActivity.this.finish();
                }
            }, 300);
            return;
        }
        super.finish();
        if (this.mMultiSelectFragment instanceof ContactMultiselectionFragment) {
            ((ContactMultiselectionFragment) this.mMultiSelectFragment).afterActivityFinish();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mSelectedData = null;
        this.mSelectedDataUris = null;
        if (!(this.finishReceiver == null || this.mLbm == null)) {
            this.mLbm.unregisterReceiver(this.finishReceiver);
            this.finishReceiver = null;
        }
        if (this.mRcsCust != null) {
            this.mRcsCust.clearSelectedPersonMap();
        }
    }

    public void onBackPressed() {
        finish();
    }
}
