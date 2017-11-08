package com.android.contacts.activities;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.os.UserManager;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.QuickContact;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.amap.api.services.core.AMapException;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.ContactPreRefreshService;
import com.android.contacts.ContactSplitUtils;
import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.activities.ActionBarAdapter.Listener;
import com.android.contacts.activities.ActionBarAdapter.TabState;
import com.android.contacts.calllog.CallLogAdapter;
import com.android.contacts.calllog.CallLogFragment;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.detail.ContactDetailLayoutController;
import com.android.contacts.detail.EspaceDialer;
import com.android.contacts.dialpad.DialpadFragment;
import com.android.contacts.dialpad.DialpadFragmentHelper;
import com.android.contacts.dialpad.SmartDialPrefix;
import com.android.contacts.dialpad.database.DatabaseHelperManager;
import com.android.contacts.dialpad.database.DialerDatabaseHelper;
import com.android.contacts.group.GroupBrowseListFragment;
import com.android.contacts.group.GroupDetailFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.camcard.bcr.CCardScanHandler;
import com.android.contacts.hap.camcard.groups.ParsePredefinedTask;
import com.android.contacts.hap.hotline.HLUtils;
import com.android.contacts.hap.list.FavoriteGroupFragment;
import com.android.contacts.hap.optimize.DummyContactBrowseListFragment;
import com.android.contacts.hap.optimize.DummyFragment;
import com.android.contacts.hap.optimize.FragmentReplacer;
import com.android.contacts.hap.optimize.OptimizationUtil;
import com.android.contacts.hap.util.GenericHandler;
import com.android.contacts.hap.util.HwAnimationReflection;
import com.android.contacts.hap.utils.BackgroundGenricHandler;
import com.android.contacts.hap.utils.ScreenUtils;
import com.android.contacts.hap.widget.HapViewPager;
import com.android.contacts.hap.yellowpage.YellowPageFragment;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.list.AccountFilterActivity;
import com.android.contacts.list.AccountFilterActivity.ContactsVisibilityListener;
import com.android.contacts.list.ContactBrowseListFragment;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListFilterController;
import com.android.contacts.list.ContactListFilterController.ContactListFilterListener;
import com.android.contacts.list.ContactListHelper;
import com.android.contacts.list.ContactsIntentResolver;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.ContactsUnavailableFragment;
import com.android.contacts.list.DefaultContactBrowseListFragment;
import com.android.contacts.list.OnContactBrowserActionListener;
import com.android.contacts.list.ProviderStatusWatcher;
import com.android.contacts.list.ProviderStatusWatcher.ProviderStatusListener;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AccountPromptUtils;
import com.android.contacts.util.ContactLoaderUtils;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.DialogManager;
import com.android.contacts.util.DialogManager.DialogShowingViewActivity;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.Manifest.permission;
import com.google.android.gms.R;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.common.collect.Maps;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.CommonConstants;
import com.huawei.cust.HwCustUtils;
import huawei.com.android.internal.widget.HwFragmentContainer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PeopleActivity extends ContactsActivity implements OnCreateContextMenuListener, Listener, DialogShowingViewActivity, ContactListFilterListener, ProviderStatusListener, ContactsVisibilityListener, ContactPreRefreshService.Listener {
    private static final AtomicInteger sNextInstanceId = new AtomicInteger();
    boolean isFirstCreateOptionsMenuOrLandScape = true;
    private boolean isRestore = false;
    private ActionBarAdapter mActionBarAdapter;
    private DefaultContactBrowseListFragment mAllFragment;
    private Object mBackGroundTaskLock = new Object();
    private ConfigirationRunnable mConfigRunable;
    private ContactDetailLayoutController mContactDetailLayoutController;
    private ContactListFilterController mContactListFilterController;
    private ContactsApplication mContactsApp;
    private final ContentObserver mContactsObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean aSelfChange) {
            if (HwLog.HWDBG) {
                HwLog.d("PeopleActivity", "dialtactactivity onchange called aSelfChange" + aSelfChange);
            }
            if (PeopleActivity.this.mDialpadFragment != null && (PeopleActivity.this.mDialpadFragment instanceof DialpadFragment) && ((DialpadFragment) PeopleActivity.this.mDialpadFragment).isAdded()) {
                if (HwLog.HWDBG) {
                    HwLog.d("PeopleActivity", "mContactsObserver refresh");
                }
                DialpadFragment lDialpadFragment = (DialpadFragment) PeopleActivity.this.mDialpadFragment;
                lDialpadFragment.setRefreshDataRequired();
                lDialpadFragment.hasValueRefreshCallLog();
            }
        }
    };
    private ContactsPreferences mContactsPrefs;
    private ContactsUnavailableFragment mContactsUnavailableFragment;
    private Context mContext;
    private boolean mCurrentFilterIsValid;
    private HwCustIntializationHelper mCust = null;
    private DialerDatabaseHelper mDialerDatabaseHelper;
    private final DialogManager mDialogManager = new DialogManager(this);
    private Fragment mDialpadFragment;
    private DialpadFragmentHelper mDialpadHelper = null;
    private boolean mDiscardTabState;
    private Fragment mFavorOrYPFragment;
    private boolean mFlagAllRegisted = false;
    private boolean mFragmentInitialized;
    private FragmentReplacer mFragmentReplacer;
    private boolean mFragmentsCreated;
    private GenericHandler mGenHdlr = new GenericHandler();
    public AlertDialog mGlobalDialogReference;
    public Dialog mGlobalDialogViewBy;
    public AlertDialog mGlobalRoamingDialogReference;
    boolean mGroupTab = false;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 5001:
                    PeopleActivity.this.mContactsApp.setIsLaunching(false);
                    break;
                case 5003:
                    if (PeopleActivity.this.mFragmentReplacer != null) {
                        PeopleActivity.this.mFragmentReplacer.replaceDummyFragments(PeopleActivity.this.getCurrentTab());
                        break;
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private ContactListHelper mHelper = null;
    private final BroadcastReceiver mHomeKeyEventBroadCastReceiver = new HomeKeyEventBroadCastReceiver();
    private InputMethodManager mInputMethodManager;
    private final int mInstanceId = sNextInstanceId.getAndIncrement();
    private ContactsIntentResolver mIntentResolver = new ContactsIntentResolver(this);
    private boolean mIsChangeToNormalMode;
    private boolean mIsContextMenuClosed = true;
    private boolean mIsHomeKeyBroadcastRegistered = false;
    private boolean mIsModeChange;
    private boolean mIsNeedMaskDefault;
    private boolean mIsNeedMaskDialpad;
    private boolean mIsNeedShowAnimate;
    private boolean mIsPortrait;
    private boolean mIsRecreatedInstance;
    private boolean mIsSimplifiedModeEnabled;
    private boolean mIsforNewIntent;
    private int mLauchTab = -1;
    private Menu mOptionsMenu = null;
    private boolean mOptionsMenuContactsAvailable;
    private SharedPreferences mPrefs;
    private final ContentObserver mProfileObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (HwLog.HWDBG) {
                HwLog.d("PeopleActivity", "mProfileObserver onchange called");
            }
            int versionGot = CommonUtilMethods.getProfileVersion(PeopleActivity.this.getApplicationContext());
            if (EmuiVersion.isSupportEmui() && PeopleActivity.this.mVersion != versionGot) {
                if (PeopleActivity.this.mAllFragment == null || PeopleActivity.this.mAllFragment.isReplacable() || !PeopleActivity.this.mAllFragment.isAdded()) {
                    HwLog.w("PeopleActivity", "mAllFragment is NULL or is replacable or is not added");
                    return;
                }
                PeopleActivity.this.mAllFragment.reloadHeaderView();
                PeopleActivity.this.mVersion = versionGot;
            }
        }
    };
    private Integer mProviderStatus = Integer.valueOf(0);
    private ProviderStatusWatcher mProviderStatusWatcher;
    private ContactsRequest mRequest;
    private final BroadcastReceiver mSimAccountDisableBroadCastReceiver = new SimAccountDisableBroadCastReceiver();
    private boolean mSimAccountDisableBroadcastRegistered = false;
    private boolean mSimpleMode;
    private ViewPager mTabPager;
    private TabPagerAdapter mTabPagerAdapter;
    private final TabPagerListener mTabPagerListener = new TabPagerListener();
    private Map<Integer, TabSelectedListener> mTabSelectedListener = Maps.newHashMap();
    private int mVersion = -2;
    private StringBuilder selection = new StringBuilder();
    private List<String> selectionArgs = new ArrayList();

    class ConfigirationRunnable implements Runnable {
        boolean checkDialerAlso;
        boolean handleConfigStates;
        boolean searchMode;

        public ConfigirationRunnable(boolean aSearchMode, boolean aHandleConfigStates, boolean aCheckDialerAlso) {
            this.searchMode = aSearchMode;
            this.handleConfigStates = aHandleConfigStates;
            this.checkDialerAlso = aCheckDialerAlso;
        }

        public void run() {
            if (!this.checkDialerAlso || PeopleActivity.this.resetAndCheckInCallForDialer()) {
                if (this.handleConfigStates) {
                    if (PeopleActivity.this.mAllFragment.isReplacable()) {
                        PeopleActivity.this.handleConfigirationWithDelay(this.searchMode, this.handleConfigStates, 1200, false);
                        return;
                    }
                    PeopleActivity.this.configureStates(this.searchMode, false);
                }
                return;
            }
            PeopleActivity.this.handleConfigirationWithDelay(this.searchMode, this.checkDialerAlso, 1200, true);
        }
    }

    private final class ContactBrowserActionListener implements OnContactBrowserActionListener {
        ContactBrowserActionListener() {
        }

        public void onSelectionChange() {
        }

        public void onViewContactAction(Uri contactLookupUri, boolean isEnterpriseContact) {
            if (isEnterpriseContact) {
                QuickContact.showQuickContact(PeopleActivity.this, new Rect(), contactLookupUri, 3, null);
            } else {
                Intent intent = new Intent("android.intent.action.VIEW", contactLookupUri);
                intent.setClass(PeopleActivity.this, ContactDetailActivity.class);
                intent.putExtra("EXTRA_FROM_CONTACT_LIST", true);
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    ContactInfoFragment contactInfo = new ContactInfoFragment();
                    contactInfo.setIntent(intent);
                    PeopleActivity.this.openRightContainer(contactInfo);
                } else {
                    if (contactLookupUri != null) {
                        long contactId = CommonUtilMethods.getContactIdFromUri(contactLookupUri);
                        intent.putExtra("EXTRA_URI_CONTACT_ID", contactId);
                        intent.putExtra("EXTRA_LIST_TO_DETAIL_URI", contactLookupUri.toString());
                        intent.putExtra("EXTRA_CONTACT_ACCOUNT_TYPE", CommonUtilMethods.getAccountTypeFromUri(PeopleActivity.this.getApplicationContext(), contactId));
                    } else {
                        HwLog.e("PeopleActivity", "contactLookupUri == NULL");
                    }
                    PeopleActivity.this.startActivity(intent);
                }
            }
        }

        public void onEditContactAction(Uri contactLookupUri) {
            Intent intent = new Intent("android.intent.action.EDIT", contactLookupUri);
            intent.setClass(PeopleActivity.this, ContactEditorActivity.class);
            Bundle extras = PeopleActivity.this.getIntent().getExtras();
            if (extras != null) {
                intent.putExtras(extras);
            }
            intent.putExtra("finishActivityOnSaveCompleted", true);
            PeopleActivity.this.startActivityForResult(intent, 3);
        }

        public void onDeleteContactAction(Uri contactUri) {
            ContactDeletionInteraction.start(PeopleActivity.this, contactUri, false, false);
        }

        public void onInvalidSelection() {
            ContactListFilter filter;
            ContactListFilter currentFilter = PeopleActivity.this.mAllFragment.getFilter();
            if (currentFilter == null || currentFilter.filterType != -6) {
                filter = ContactListFilter.createFilterWithType(-6);
                PeopleActivity.this.mAllFragment.setFilter(filter, false, false);
            } else {
                filter = ContactListFilter.createFilterWithType(-2);
                PeopleActivity.this.mAllFragment.setFilter(filter);
            }
            PeopleActivity.this.mContactListFilterController.setContactListFilter(filter, true);
        }
    }

    public class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String reason = intent.getStringExtra("reason");
            if (reason != null && reason.equals("homekey")) {
                PeopleActivity.this.mPrefs.edit().putBoolean("PeopleActivity.isHomeOrBackPressed", true).apply();
                PeopleActivity.this.mGroupTab = false;
                if (PeopleActivity.this.mDialpadFragment instanceof DialpadFragment) {
                    ((DialpadFragment) PeopleActivity.this.mDialpadFragment).getAnimateHelper().reset();
                }
            }
        }
    }

    private static class InflateCallLog implements Runnable {
        Context inflateContext;

        public InflateCallLog(Context context) {
            this.inflateContext = context;
        }

        public void run() {
            new ContextThemeWrapper(this.inflateContext.getApplicationContext(), this.inflateContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)).getTheme().applyStyle(R.style.PeopleTheme, true);
        }
    }

    private class SimAccountDisableBroadCastReceiver extends BroadcastReceiver {
        private SimAccountDisableBroadCastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            PeopleActivity.this.mContactListFilterController.setContactListFilter(ContactListFilter.createFilterWithType(-2), true);
        }
    }

    private class TabPagerAdapter extends PagerAdapter {
        private FragmentTransaction mCurTransaction = null;
        private Fragment mCurrentPrimaryItem;
        private final FragmentManager mFragmentManager;
        private boolean mTabPagerAdapterSearchMode;

        public TabPagerAdapter() {
            this.mFragmentManager = PeopleActivity.this.getFragmentManager();
        }

        public boolean isSearchMode() {
            return this.mTabPagerAdapterSearchMode;
        }

        public void setSearchMode(boolean searchMode) {
            if (searchMode != this.mTabPagerAdapterSearchMode) {
                this.mTabPagerAdapterSearchMode = searchMode;
                notifyDataSetChanged();
            }
        }

        public int getCount() {
            int i = 1;
            if (PeopleActivity.this.mIsSimplifiedModeEnabled) {
                if (!this.mTabPagerAdapterSearchMode) {
                    i = 2;
                }
                return i;
            }
            if (!this.mTabPagerAdapterSearchMode) {
                i = TabState.COUNT;
            }
            return i;
        }

        public int getItemPosition(Object object) {
            if (this.mTabPagerAdapterSearchMode) {
                if (object instanceof DefaultContactBrowseListFragment) {
                    return 0;
                }
            } else if (object instanceof DummyFragment) {
                DummyFragment dummyFragment = (DummyFragment) object;
                if (dummyFragment.isReplaced()) {
                    return -2;
                }
                return dummyFragment.getTabIndex();
            } else if ((object instanceof YellowPageFragment) || (object instanceof FavoriteGroupFragment)) {
                return TabState.FAVOR_YELLOWPAGE;
            } else {
                if (object instanceof DefaultContactBrowseListFragment) {
                    if (((DefaultContactBrowseListFragment) object).isReplacable()) {
                        return -2;
                    }
                    return TabState.ALL;
                } else if (object instanceof DialpadFragment) {
                    return TabState.DIALER;
                }
            }
            return -2;
        }

        public void startUpdate(ViewGroup container) {
        }

        private Fragment getFragment(int position) {
            if (this.mTabPagerAdapterSearchMode) {
                if (position != 0) {
                    HwLog.w("PeopleActivity", "Request fragment at position=" + position + ", eventhough we " + "are in search mode");
                }
                return PeopleActivity.this.mAllFragment;
            } else if (position == TabState.FAVOR_YELLOWPAGE) {
                return PeopleActivity.this.mFavorOrYPFragment;
            } else {
                if (position == TabState.ALL) {
                    return PeopleActivity.this.mAllFragment;
                }
                if (position == TabState.DIALER) {
                    return PeopleActivity.this.mDialpadFragment;
                }
                throw new IllegalArgumentException("position: " + position);
            }
        }

        public Object instantiateItem(ViewGroup container, int position) {
            if (this.mCurTransaction == null) {
                this.mCurTransaction = this.mFragmentManager.beginTransaction();
            }
            Fragment f = getFragment(position);
            if (HwLog.HWDBG) {
                HwLog.d("TabPagerAdapter", "Fragment got: " + f + "; position:" + position);
            }
            this.mCurTransaction.show(f);
            f.setUserVisibleHint(f == this.mCurrentPrimaryItem);
            return f;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            if (this.mCurTransaction == null) {
                this.mCurTransaction = this.mFragmentManager.beginTransaction();
            }
            this.mCurTransaction.hide((Fragment) object);
        }

        public void finishUpdate(ViewGroup container) {
            if (this.mCurTransaction != null) {
                this.mCurTransaction.commitAllowingStateLoss();
                this.mCurTransaction = null;
                this.mFragmentManager.executePendingTransactions();
            }
        }

        public boolean isViewFromObject(View view, Object object) {
            return ((Fragment) object).getView() == view;
        }

        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            Fragment fragment = (Fragment) object;
            if (this.mCurrentPrimaryItem != fragment) {
                this.mCurrentPrimaryItem = fragment;
            }
        }

        public Parcelable saveState() {
            return null;
        }

        public void restoreState(Parcelable state, ClassLoader loader) {
        }
    }

    private class TabPagerListener implements OnPageChangeListener {
        TabPagerListener() {
        }

        public void onPageScrollStateChanged(int state) {
            if (state == 1) {
                View lView = PeopleActivity.this.getCurrentFocus();
                if (lView != null) {
                    lView.clearFocus();
                    if (PeopleActivity.this.mInputMethodManager != null) {
                        PeopleActivity.this.mInputMethodManager.hideSoftInputFromWindow(lView.getWindowToken(), 0);
                    }
                }
            }
            if (state == 2 && !PeopleActivity.this.isAllFragmentReplace()) {
                if (PeopleActivity.this.mHandler.hasMessages(5003)) {
                    PeopleActivity.this.mHandler.removeMessages(5003);
                }
                PeopleActivity.this.mHandler.sendEmptyMessageDelayed(5003, 300);
            }
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            if (PeopleActivity.this.mAllFragment != null) {
                PeopleActivity.this.mAllFragment.hidePhoneBookOverLay();
            }
            if (PeopleActivity.this.mCust != null) {
                PeopleActivity.this.mCust.checkAndStartSyncClient(PeopleActivity.this.getApplicationContext(), position, false);
            }
            if (PeopleActivity.this.mFavorOrYPFragment instanceof FavoriteGroupFragment) {
                ((FavoriteGroupFragment) PeopleActivity.this.mFavorOrYPFragment).setCurrentIsShow(position == TabState.FAVOR_YELLOWPAGE);
            }
            if (PeopleActivity.this.mFavorOrYPFragment instanceof YellowPageFragment) {
                ((YellowPageFragment) PeopleActivity.this.mFavorOrYPFragment).onPageSelected(position, PeopleActivity.this.isRestore);
            }
            if (!PeopleActivity.this.mDiscardTabState) {
                if (position == TabState.FAVOR_YELLOWPAGE) {
                    StatisticalHelper.report(1106);
                    ExceptionCapture.reportScene(52);
                }
                if (!PeopleActivity.this.mTabPagerAdapter.isSearchMode()) {
                    if (PeopleActivity.this.mIsModeChange && position == 1 && !PeopleActivity.this.mIsSimplifiedModeEnabled) {
                        position = TabState.ALL;
                    }
                    if (position == TabState.DEFAULT) {
                        PLog.d(21, "PeopleActivity onPageSelected");
                    }
                    PeopleActivity.this.mActionBarAdapter.setCurrentTab(position, false);
                    PeopleActivity.this.mIsModeChange = false;
                    if ((position == TabState.ALL && !PeopleActivity.this.mIsSimplifiedModeEnabled) || (position == 1 && PeopleActivity.this.mIsSimplifiedModeEnabled)) {
                        PeopleActivity.this.mAllFragment.refreshSearchViewFocus();
                        EditText contactSearchView = PeopleActivity.this.mAllFragment.getContactsSearchView();
                        if (!(contactSearchView == null || !PeopleActivity.this.mIsSimplifiedModeEnabled || PeopleActivity.this.mAllFragment.isSearchMode() || TextUtils.isEmpty(contactSearchView.getText().toString()))) {
                            contactSearchView.setText(null);
                            contactSearchView.setCursorVisible(false);
                        }
                        if (contactSearchView != null) {
                            String queryString = contactSearchView.getText().toString();
                            if (queryString != null) {
                                queryString = queryString.trim();
                            }
                            if (!TextUtils.isEmpty(queryString)) {
                                PeopleActivity.this.mAllFragment.setQueryString(queryString, true);
                            }
                        }
                        ExceptionCapture.reportScene(3);
                    } else if (position == TabState.DIALER) {
                        CommonUtilMethods.disableFrameRadar("PeopleActivity_onPageSelected");
                        TabSelectedListener lListener = (TabSelectedListener) PeopleActivity.this.mTabSelectedListener.get(Integer.valueOf(position));
                        if (lListener != null) {
                            lListener.onCurrentSelectedTab();
                        }
                        if (PeopleActivity.this.mDialpadFragment instanceof DialpadFragment) {
                            DialpadFragment dialpadFragment = (DialpadFragment) PeopleActivity.this.mDialpadFragment;
                            dialpadFragment.displayDigits();
                            if (PeopleActivity.this.mProviderStatus != null && PeopleActivity.this.mProviderStatus.intValue() == 1) {
                                dialpadFragment.clearDigitsText();
                                dialpadFragment.showDigistHeader(false, false);
                            }
                            dialpadFragment.loadDelayLoadingLayout();
                        }
                        ExceptionCapture.reportScene(2);
                        CommonUtilMethods.enableFrameRadar("PeopleActivity_onPageSelected");
                    }
                    PeopleActivity.this.invalidateOptionsMenu();
                    InputMethodManager imm = (InputMethodManager) PeopleActivity.this.getApplicationContext().getSystemService("input_method");
                    if (imm != null && imm.isActive()) {
                        imm.hideSoftInputFromWindow(PeopleActivity.this.getWindow().getDecorView().getWindowToken(), 0);
                    }
                }
            }
        }
    }

    public interface TabSelectedListener {
        void onCurrentSelectedTab();
    }

    public FragmentReplacer getFragmentReplacer() {
        return this.mFragmentReplacer;
    }

    public void setNeedMaskDialpad(boolean isNeed) {
        this.mIsNeedMaskDialpad = isNeed;
    }

    public boolean getNeedMaskDialpad() {
        return this.mIsNeedMaskDialpad;
    }

    public void setNeedMaskDefault(boolean isNeed) {
        this.mIsNeedMaskDefault = isNeed;
    }

    public boolean getNeedMaskDefault() {
        return this.mIsNeedMaskDefault;
    }

    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public String toString() {
        return String.format("%s@%d", new Object[]{getClass().getSimpleName(), Integer.valueOf(this.mInstanceId)});
    }

    public boolean areContactsAvailable() {
        if (this.mProviderStatus != null) {
            return !this.mProviderStatus.equals(Integer.valueOf(0)) ? this.mProviderStatus.equals(Integer.valueOf(2)) : true;
        } else {
            return false;
        }
    }

    private boolean areContactWritableAccountsAvailable() {
        return ContactsUtils.areContactWritableAccountsAvailable(this);
    }

    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ContactsUnavailableFragment) {
            this.mContactsUnavailableFragment = (ContactsUnavailableFragment) fragment;
        }
    }

    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        UserManager userManager = (UserManager) getSystemService("user");
        if (userManager == null || userManager.isUserUnlocked()) {
            if (RequestPermissionsActivity.startPermissionActivity(this)) {
                finish();
            }
            getContactListHelper().startInitPeopleActivity(this);
            this.mRequest = this.mIntentResolver.resolveIntent(getIntent());
            int currentTab = ActionBarAdapter.getCurrentTabFromRequest(savedState, this.mRequest);
            if (HwLog.HWDBG) {
                HwLog.d("PeopleActivity", "PeopleActivity.onCreate get start tab currentTab=" + currentTab);
            }
            createFragmentsInBackground(currentTab);
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                requestWindowFeature(9);
                getWindow().addFlags(67108864);
            }
            if (HwLog.HWDBG) {
                HwLog.d("PeopleActivity", "PeopleActivity.onCreate start");
            }
            this.mContactsApp = (ContactsApplication) getApplication();
            this.mContext = getApplicationContext();
            this.mContactListFilterController = ContactListFilterController.getInstance(this.mContext);
            PLog.d(2, "PeopleActivity oncreate begin");
            getWindow().setFlags(16777216, 16777216);
            this.mPrefs = SharePreferenceUtil.getDefaultSp_de(this);
            if (Process.myPid() != this.mPrefs.getInt("last_process_id", -1)) {
                savedState = null;
                CommonUtilMethods.clearInstanceState();
                this.mPrefs.edit().putInt("last_process_id", Process.myPid()).apply();
            } else {
                String lastActivityId = this.mContactsApp.getLastActivityId();
                String currentActivityId = toString();
                int lastScreenMode = this.mContactsApp.getLastScreenMode();
                int currentScreenMode = getResources().getConfiguration().orientation;
                if (!(lastActivityId == null || lastActivityId.equals(currentActivityId) || lastScreenMode != currentScreenMode)) {
                    savedState = null;
                }
                this.mContactsApp.setLastActivityId(currentActivityId);
                this.mContactsApp.setLastScreenMode(currentScreenMode);
            }
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                if (isTopActivity()) {
                    CommonUtilMethods.clearInstanceState();
                }
                if (savedState == null) {
                    savedState = CommonUtilMethods.getInstanceState();
                }
            }
            if (savedState != null) {
                this.mIsNeedMaskDefault = savedState.getBoolean("key_mask_default");
                this.mIsNeedMaskDialpad = savedState.getBoolean("key_mask_dialpad");
            }
            setTheme(R.style.PeopleThemeWithTab);
            if (processIntent(false)) {
                if (CommonUtilMethods.getIsLiteFeatureProducts() && 143 == this.mRequest.getActionCode()) {
                    if (currentTab == TabState.ALL && this.mContactsApp.getIsFirstStartContacts()) {
                        preloadContactsData();
                    } else {
                        this.mContactsApp.setFirstStartContactsStatus(false);
                    }
                }
                setActionBarView();
                if (CommonUtilMethods.getIsLiteFeatureProducts()) {
                    preloadLiteSearchData();
                } else {
                    preloadSearchData();
                }
                this.mInputMethodManager = (InputMethodManager) getApplicationContext().getSystemService("input_method");
                this.mSimpleMode = CommonUtilMethods.isSimpleModeOn();
                CommonConstants.setSimplifiedModeEnabled(this.mSimpleMode);
                this.mProviderStatusWatcher = ProviderStatusWatcher.getInstance(this);
                this.mContactListFilterController.checkFilterValidity(false);
                this.mContactListFilterController.addListener(this);
                if (this.mSimpleMode) {
                    this.mContactListFilterController.resetDefaultFilterToAllTypeIfNecessary();
                }
                this.mProviderStatusWatcher.addListener(this);
                this.mIsRecreatedInstance = savedState != null;
                createTabPager(savedState, currentTab);
                if (HwLog.HWDBG) {
                    HwLog.d("ContactsPerf", "PeopleActivity.onCreate finish");
                }
                registerReceiver(this.mHomeKeyEventBroadCastReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"), "android.permission.INJECT_EVENTS", null);
                registerReceiver(this.mSimAccountDisableBroadCastReceiver, new IntentFilter("com.huawei.android.action.SIM_ACCOUNT_DISABLE"), permission.HW_CONTACTS_ALL, null);
                this.mSimAccountDisableBroadcastRegistered = true;
                this.mIsHomeKeyBroadcastRegistered = true;
                this.mIsPortrait = getResources().getConfiguration().orientation == 1;
                this.mContactsApp.markLaunch();
                if (getCurrentTab() == TabState.DIALER) {
                    this.mHandler.post(new InflateCallLog(getApplicationContext()));
                }
                if (!QueryUtil.isProviderSupportHwSeniorSearch()) {
                    this.mDialerDatabaseHelper = DatabaseHelperManager.getDatabaseHelper(this);
                    SmartDialPrefix.initializeNanpSettings(this);
                }
                if (EmuiFeatureManager.isProductCustFeatureEnable()) {
                    this.mCust = (HwCustIntializationHelper) HwCustUtils.createObj(HwCustIntializationHelper.class, new Object[]{getApplicationContext()});
                    if (this.mCust != null) {
                        this.mCust.initializeCust(this, this.mActionBarAdapter, false, this.mIsRecreatedInstance, savedState);
                    }
                }
                checkEspaceSupportDelayed();
                createFragments(currentTab);
                if (this.mLauchTab == TabState.DIALER) {
                    PLog.d(1008, "PeopleActivity oncreate end. set to calllog.");
                } else if (this.mLauchTab == TabState.ALL) {
                    PLog.d(1009, "PeopleActivity oncreate end. set to contact list.");
                }
                return;
            }
            finish();
            return;
        }
        HwLog.i("PeopleActivity", "PeopleActivity.onCreate,device is locked,finish");
        finish();
        overridePendingTransition(0, 0);
    }

    private boolean isTopActivity() {
        List<RunningTaskInfo> tasksInfo = ((ActivityManager) getSystemService("activity")).getRunningTasks(1);
        return tasksInfo != null && tasksInfo.size() > 0 && getComponentName().getClassName().equals(((RunningTaskInfo) tasksInfo.get(0)).topActivity.getClassName());
    }

    private void checkEspaceSupportDelayed() {
        OptimizationUtil.postTaskToRunAferActivitylaunched(this, this.mHandler, new Runnable() {
            public void run() {
                ContactsThreadPool.getInstance().execute(new Runnable() {
                    public void run() {
                        if (CommonUtilMethods.checkApkExist(PeopleActivity.this.mContext, "com.huawei.espacev2")) {
                            EspaceDialer.setIsShowEspace(true);
                        }
                        EspaceDialer.setIsCheckSuppot(EspaceDialer.querySupport(PeopleActivity.this.mContext));
                    }
                });
            }
        });
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && !isInMultiWindowMode) {
            recreate();
        }
        ContactDetailLayoutCache.clearDetailViewCache();
    }

    private void preloadSearchData() {
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                if (HwLog.HWDBG) {
                    HwLog.d("PeopleActivity", "start preload search data!");
                }
                Uri uri = Contacts.CONTENT_URI.buildUpon().appendPath("preload_search_data").build();
                int count = 0;
                if (!PeopleActivity.this.isFinishing()) {
                    Cursor c = PeopleActivity.this.getContentResolver().query(uri, new String[]{"DATA_COUNT"}, null, null, null);
                    if (c != null) {
                        try {
                            if (c.moveToFirst()) {
                                count = c.getInt(0);
                            }
                            c.close();
                        } catch (Throwable th) {
                            c.close();
                        }
                    }
                }
                if (HwLog.HWDBG) {
                    HwLog.d("PeopleActivity", "preload search data ending! data count is " + count);
                }
            }
        });
    }

    private void preloadLiteSearchData() {
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                if (HwLog.HWDBG) {
                    HwLog.d("PeopleActivity", "start preload lite search data!");
                }
                Uri uri = Contacts.CONTENT_URI.buildUpon().appendPath("preload_search_data").build();
                int count = 0;
                if (!PeopleActivity.this.isFinishing()) {
                    Cursor c = PeopleActivity.this.getContentResolver().query(uri, new String[]{"DATA_COUNT"}, null, null, null);
                    if (c != null) {
                        try {
                            if (c.moveToFirst()) {
                                count = c.getInt(0);
                            }
                            c.close();
                        } catch (Throwable th) {
                            c.close();
                        }
                    }
                }
                if (HwLog.HWDBG) {
                    HwLog.d("PeopleActivity", "preload search data ending! data count is " + count);
                }
            }
        }, 800);
    }

    public void doRegisterReceivers() {
        BackgroundGenricHandler.getInstance().post(new ParsePredefinedTask(getApplicationContext()));
        ContactPreRefreshService.registerListener(this);
        AccountFilterActivity.addContactsVisibilityListener(this);
        getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, this.mContactsObserver);
        getContentResolver().registerContentObserver(Profile.CONTENT_URI, true, this.mProfileObserver);
        this.mFlagAllRegisted = true;
    }

    public void onDelContactsCompleted(Intent callbackIntent) {
        if (callbackIntent.getAction() != null && "action_hide_delete_contacts".equals(callbackIntent.getAction())) {
            ContactBrowseListFragment fragment = getListFragment();
            if (fragment != null && (fragment instanceof DefaultContactBrowseListFragment)) {
                DefaultContactBrowseListFragment defaultContactFragment = (DefaultContactBrowseListFragment) fragment;
                if (defaultContactFragment.getAdapter() != null) {
                    long[] aContactIds = callbackIntent.getLongArrayExtra("ContactIds");
                    if (aContactIds != null && 1 == aContactIds.length) {
                        ((ContactListAdapter) defaultContactFragment.getAdapter()).hideOneContact(aContactIds[0]);
                        if (HLUtils.isShowHotNumberOnTop && !HLUtils.isExistHotNumber(this.mContext)) {
                            defaultContactFragment.reloadHeaderView();
                        }
                    }
                }
            }
        }
    }

    protected void onNewIntent(Intent intent) {
        PLog.d(3, "PeopleActivity onNewIntent");
        UserManager userManager = (UserManager) getSystemService("user");
        if (userManager == null || userManager.isUserUnlocked()) {
            setIntent(intent);
            this.mIsforNewIntent = true;
            if (processIntent(true)) {
                setActionBarView();
                this.mActionBarAdapter.setListener(this);
                this.mActionBarAdapter.initialize(null, this.mRequest);
                if (this.mCust != null) {
                    this.mCust.checkAndStartSyncClient(this, this.mActionBarAdapter.getCurrentTab(), false);
                }
                this.mContactListFilterController.checkFilterValidity(false);
                this.mCurrentFilterIsValid = true;
                configureFragments(true);
                invalidateOptionsMenuIfNeeded();
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    boolean isContactListNotAttached = this.mAllFragment instanceof DummyContactBrowseListFragment;
                    boolean isFavAttached = this.mFavorOrYPFragment instanceof FavoriteGroupFragment;
                    if (this.mIsforNewIntent && (!isContactListNotAttached || isFavAttached)) {
                        if (!isContactListNotAttached) {
                            this.mAllFragment.setFromNewIntent(this.mIsforNewIntent);
                        }
                        if (isFavAttached) {
                            ((FavoriteGroupFragment) this.mFavorOrYPFragment).setFromNewIntent(this.mIsforNewIntent);
                        }
                    }
                    return;
                }
                return;
            }
            finish();
            return;
        }
        HwLog.i("PeopleActivity", "PeopleActivity.onNewIntent,device is locked,finish");
        finish();
        overridePendingTransition(0, 0);
    }

    private boolean processIntent(boolean forNewIntent) {
        this.mRequest = this.mIntentResolver.resolveIntent(getIntent());
        if (HwLog.HWDBG) {
            HwLog.d("PeopleActivity", this + " processIntent: forNewIntent=" + forNewIntent + " intent=" + getIntent() + " request=" + this.mRequest);
        }
        if (this.mRequest.isValid()) {
            Intent redirect = this.mRequest.getRedirectIntent();
            if (redirect != null) {
                startActivity(redirect);
                return false;
            } else if (this.mRequest.getActionCode() != 140) {
                return true;
            } else {
                if (this.mRequest.isFromSearchDetail()) {
                    redirect = new Intent();
                    redirect.setComponent(new ComponentName("com.android.contacts", "com.android.contacts.activities.ContactsSearchDetail"));
                    redirect.setFlags(268435456);
                    redirect.setData(this.mRequest.getContactUri());
                    startActivity(redirect);
                } else {
                    redirect = new Intent(this, ContactDetailActivity.class);
                    redirect.setAction("android.intent.action.VIEW");
                    redirect.setData(this.mRequest.getContactUri());
                    startActivity(redirect);
                }
                return false;
            }
        }
        setResult(0);
        return false;
    }

    private void setActionBarView() {
        String desc = this.mRequest.getDescription();
        if (TextUtils.isEmpty(desc)) {
            setTitle(this.mRequest.getActivityTitle());
        } else {
            setTitle(desc);
        }
        if ("android.intent.action.SEARCH".equals(getIntent().getAction())) {
            getActionBar().setTitle(this.mRequest.getActivityTitle());
            if ((this.mAllFragment instanceof DummyContactBrowseListFragment) && this.mFragmentReplacer != null) {
                this.mFragmentReplacer.replaceDummyFragments(TabState.ALL);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void createFragments(int currentTab) {
        synchronized (this.mBackGroundTaskLock) {
            if (this.mFragmentsCreated) {
                return;
            }
            this.mFragmentsCreated = true;
            if (HwLog.HWDBG) {
                HwLog.d("PeopleActivity", "PeopleActivity.createFragments start");
            }
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            this.mDialpadFragment = fragmentManager.findFragmentByTag("tab-pager-dialer");
            this.mFavorOrYPFragment = fragmentManager.findFragmentByTag("tab-pager-favor-yellow");
            if (!(this.mFavorOrYPFragment == null || this.mDialpadFragment == null || ((!(this.mFavorOrYPFragment instanceof YellowPageFragment) || !EmuiFeatureManager.isShowFavoritesTab(this)) && (!(this.mFavorOrYPFragment instanceof FavoriteGroupFragment) || EmuiFeatureManager.isShowFavoritesTab(this))))) {
                this.mFavorOrYPFragment = new DummyFragment("tab-pager-favor-yellow", TabState.FAVOR_YELLOWPAGE);
                transaction.add(R.id.tab_pager, this.mFavorOrYPFragment, "tab-pager-favor-yellow");
            }
            this.mAllFragment = (DefaultContactBrowseListFragment) fragmentManager.findFragmentByTag("tab-pager-all");
            if (HwLog.HWDBG) {
                HwLog.d("PeopleActivity", "createFragments: mDialpadFragment = " + this.mDialpadFragment + "; currentTab = " + currentTab);
            }
            if (this.mDialpadFragment == null) {
                if (TabState.DIALER == currentTab) {
                    this.mDialpadFragment = new DialpadFragment();
                    this.mFavorOrYPFragment = new DummyFragment("tab-pager-favor-yellow", TabState.FAVOR_YELLOWPAGE);
                    this.mAllFragment = new DummyContactBrowseListFragment();
                } else if (TabState.FAVOR_YELLOWPAGE == currentTab) {
                    this.mDialpadFragment = new DummyFragment("tab-pager-dialer", TabState.DIALER);
                    this.mFavorOrYPFragment = createFavorOrYpFragment();
                    this.mAllFragment = new DummyContactBrowseListFragment();
                } else {
                    this.mDialpadFragment = new DummyFragment("tab-pager-dialer", TabState.DIALER);
                    this.mFavorOrYPFragment = new DummyFragment("tab-pager-favor-yellow", TabState.FAVOR_YELLOWPAGE);
                    this.mAllFragment = new DefaultContactBrowseListFragment(null, false);
                }
                transaction.add(R.id.tab_pager, this.mDialpadFragment, "tab-pager-dialer");
                transaction.add(R.id.tab_pager, this.mAllFragment, "tab-pager-all");
                transaction.add(R.id.tab_pager, this.mFavorOrYPFragment, "tab-pager-favor-yellow");
            }
            if (!this.mAllFragment.isReplacable()) {
                this.mAllFragment.setOnContactListActionListener(new ContactBrowserActionListener());
            }
            transaction.hide(this.mDialpadFragment);
            transaction.hide(this.mFavorOrYPFragment);
            transaction.hide(this.mAllFragment);
            transaction.commitAllowingStateLoss();
            if (HwLog.HWDBG) {
                HwLog.d("PeopleActivity", "PeopleActivity.createViewsAndFragments end");
            }
        }
    }

    private void createFragmentsInBackground(final int currentTab) {
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                PeopleActivity.this.createFragments(currentTab);
                if (currentTab == TabState.ALL) {
                    PeopleActivity.this.getContactListHelper().sendEmptyMessage(5);
                }
            }
        });
    }

    private void createTabPager(Bundle savedState, int tab) {
        setContentView(R.layout.people_activity);
        int currentTab = -1;
        try {
            this.mTabPager = (ViewPager) getView(R.id.tab_pager);
            this.mTabPagerAdapter = new TabPagerAdapter();
            this.mTabPager.setAdapter(this.mTabPagerAdapter);
            this.mTabPager.setOnPageChangeListener(this.mTabPagerListener);
            this.mActionBarAdapter = new ActionBarAdapter(this, this, getActionBar(), false);
            this.mActionBarAdapter.initFromrequest(savedState, this.mRequest, tab);
            if (this.mRequest.getActivityTitle() != null) {
                boolean z;
                ActionBarAdapter actionBarAdapter = this.mActionBarAdapter;
                String charSequence = this.mRequest.getActivityTitle().toString();
                if (savedState == null) {
                    z = true;
                } else {
                    z = false;
                }
                currentTab = actionBarAdapter.correctLauncherMode(charSequence, z, false, tab);
            } else {
                HwLog.e("PeopleActivity", "mRequest.getActivityTitle() is null");
            }
            this.mLauchTab = currentTab;
            this.mActionBarAdapter.init();
        } catch (IllegalArgumentException e) {
            finish();
        }
    }

    protected void onStart() {
        if (!this.mFragmentInitialized) {
            configureFragments(!this.mIsRecreatedInstance);
            HwLog.i("PeopleActivity", "onStart() SimplifiedMode:" + this.mIsSimplifiedModeEnabled + "SearchMode:" + this.mAllFragment.isSearchMode());
            if (this.mIsSimplifiedModeEnabled) {
                hideOrDisplayFragmentSearchBar(true);
            } else if (this.mIsRecreatedInstance) {
                hideOrDisplayFragmentSearchBar(this.mAllFragment.isSearchMode());
            }
            this.mFragmentInitialized = true;
        } else if (!this.mCurrentFilterIsValid) {
            if (this.mAllFragment.getFilter() == null) {
                super.onStart();
                return;
            }
            this.mContactListFilterController.setContactListFilter(this.mAllFragment.getFilter(), true);
            this.mContactListFilterController.checkFilterValidity(true);
            this.mCurrentFilterIsValid = true;
        }
        super.onStart();
    }

    protected void onPause() {
        this.mGroupTab = false;
        this.mOptionsMenuContactsAvailable = false;
        this.mProviderStatusWatcher.stop();
        if (this.mGlobalRoamingDialogReference != null) {
            this.mGlobalRoamingDialogReference.dismiss();
            this.mGlobalRoamingDialogReference = null;
        }
        super.onPause();
    }

    protected void onResume() {
        ExceptionCapture.reportScene(1);
        int curTab = getCurrentTab();
        if (curTab == TabState.DIALER) {
            PLog.d(4, "PeopleActivity onResume: dialer");
        } else if (curTab == TabState.DEFAULT) {
            PLog.d(5, "PeopleActivity onResume: contact");
        }
        EmuiFeatureManager.isShowCamCard(this);
        super.onResume();
        if (!(this.mAllFragment instanceof DummyContactBrowseListFragment) && this.mAllFragment.isCancelFromCcard) {
            this.mAllFragment.isCancelFromCcard = false;
            new CCardScanHandler().recognizeCapture(this, this.mAllFragment);
        }
        if (this.mSimpleMode != CommonUtilMethods.isSimplifiedModeEnabled()) {
            CommonConstants.setSimplifiedModeEnabled(CommonUtilMethods.isSimpleModeOn());
        }
        if (!isAllFragmentReplace()) {
            this.mHandler.sendEmptyMessageDelayed(5003, 0);
        }
        restartFragmentReplacement();
        this.mProviderStatusWatcher.start();
        updateViewConfiguration(true);
        this.mActionBarAdapter.setListener(this);
        this.mTabPager.setOnPageChangeListener(this.mTabPagerListener);
        updateFragmentsVisibility(new boolean[0]);
        this.mHandler.removeMessages(5001);
        this.mContactsApp.setIsLaunching(true);
        setLaunchingFinishingWithDelay();
        if (!QueryUtil.isProviderSupportHwSeniorSearch()) {
            this.mDialerDatabaseHelper.startSmartDialUpdateThread();
        }
        if (!this.mIsforNewIntent) {
            if (CommonUtilMethods.calcIfNeedSplitScreen() && getCurrentTab() != TabState.FAVOR_YELLOWPAGE && (this.mFavorOrYPFragment instanceof FavoriteGroupFragment)) {
                ((FavoriteGroupFragment) this.mFavorOrYPFragment).onPageSelect();
            }
            if (!(!CommonUtilMethods.calcIfNeedSplitScreen() || getCurrentTab() == TabState.ALL || (this.mAllFragment instanceof DummyContactBrowseListFragment))) {
                this.mAllFragment.onPageSelect();
            }
        }
        this.mIsforNewIntent = false;
        showOrHideActionbar();
        PLog.d(6, "PeopleActivity onResume end");
    }

    private void setLaunchingFinishingWithDelay() {
        if (this.mContactsApp.getLaunchingStat()) {
            this.mHandler.sendEmptyMessageDelayed(5001, 2000);
        }
    }

    protected void onStop() {
        super.onStop();
        Editor editor = SharePreferenceUtil.getDefaultSp_de(getApplicationContext()).edit();
        editor.remove("contact_boot_key");
        editor.commit();
        this.mCurrentFilterIsValid = false;
        if (this.mIsPortrait) {
            closeCustomOptionMenu();
        } else if (this.mOptionsMenu != null) {
            this.mOptionsMenu.close();
        }
    }

    public void doUnRegisterReceiver() {
        if (this.mFlagAllRegisted) {
            ContactPreRefreshService.unregisterListener(this);
            AccountFilterActivity.removeContactsVisibilityListener(this);
            getContentResolver().unregisterContentObserver(this.mContactsObserver);
            getContentResolver().unregisterContentObserver(this.mProfileObserver);
            this.mFlagAllRegisted = false;
        }
    }

    protected void onDestroy() {
        getContactListHelper().sendEmptyMessage(100);
        getDialpadFragmentHelper().sendEmptyMessage(4369);
        this.mHandler.removeMessages(5003);
        if (this.mConfigRunable != null) {
            this.mHandler.removeCallbacks(this.mConfigRunable);
        }
        cancelReplacerTask();
        this.mGenHdlr.clearAllMsg();
        if (this.mProviderStatusWatcher != null) {
            this.mProviderStatusWatcher.removeListener(this);
        }
        if (this.mActionBarAdapter != null) {
            this.mActionBarAdapter.setListener(null);
        }
        if (this.mContactListFilterController != null) {
            this.mContactListFilterController.removeListener(this);
        }
        if (this.mIsHomeKeyBroadcastRegistered && this.mHomeKeyEventBroadCastReceiver != null) {
            try {
                unregisterReceiver(this.mHomeKeyEventBroadCastReceiver);
                this.mIsHomeKeyBroadcastRegistered = false;
            } catch (IllegalArgumentException e) {
                HwLog.e("PeopleActivity", "mHomeKeyEventBroadCastReceiver is not registered: " + e);
            }
        }
        if (this.mSimAccountDisableBroadcastRegistered && this.mSimAccountDisableBroadCastReceiver != null) {
            try {
                unregisterReceiver(this.mSimAccountDisableBroadCastReceiver);
                this.mSimAccountDisableBroadcastRegistered = false;
            } catch (IllegalArgumentException e2) {
                HwLog.e("PeopleActivity", "mSimAccountDisableBroadCastReceiver is not registered: " + e2);
            }
        }
        this.mGlobalDialogReference = null;
        this.mGlobalRoamingDialogReference = null;
        if (!deleteShareContactsFiles()) {
            HwLog.w("PeopleActivity", "deleteShareContactsFiles exec failed!");
        }
        CallLogAdapter.releaseCust();
        super.onDestroy();
        File file = new File(getExternalCacheDir(), "profile.jpg");
        if (file.exists()) {
            boolean deleted = file.delete();
            if (HwLog.HWDBG) {
                HwLog.i("PeopleActivity", "file delete :" + deleted);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void configureFragments(boolean fromRequest) {
        boolean doWithDelay = false;
        if (fromRequest) {
            ContactListFilter filter = null;
            int actionCode = this.mRequest.getActionCode();
            boolean searchMode = this.mRequest.isSearchMode();
            int tabToOpen = -1;
            if (this.mGroupTab) {
                actionCode = 20;
            }
            switch (actionCode) {
                case 10:
                    if (!this.mRequest.getActivityTitle().equals("com.android.contacts.activities.PeopleActivity")) {
                        int lastTab = this.mActionBarAdapter.getCurrentTab();
                        if (this.mTabPager != null && this.mTabPager.getCurrentItem() != lastTab && !this.mActionBarAdapter.isSearchMode()) {
                            tabToOpen = this.mTabPager.getCurrentItem();
                            break;
                        } else {
                            tabToOpen = -1;
                            break;
                        }
                    }
                    tabToOpen = TabState.ALL;
                    break;
                    break;
                case 15:
                    filter = ContactListFilter.createFilterWithType(-2);
                    tabToOpen = TabState.ALL;
                    break;
                case 16:
                    filter = ContactListFilter.restoreDefaultPreferences(this.mPrefs);
                    tabToOpen = TabState.ALL;
                    break;
                case 17:
                    filter = ContactListFilter.createFilterWithType(-5);
                    tabToOpen = TabState.ALL;
                    break;
                case 20:
                case 30:
                case Place.TYPE_FURNITURE_STORE /*40*/:
                case Place.TYPE_HOSPITAL /*50*/:
                    tabToOpen = TabState.ALL;
                    break;
                case 140:
                    break;
                case 141:
                    if (!resetAndCheckInCallForDialer()) {
                        doWithDelay = true;
                    }
                    tabToOpen = TabState.DIALER;
                    break;
                case 142:
                    tabToOpen = TabState.DIALER;
                    if (EmuiFeatureManager.isSystemVoiceCapable()) {
                        if (this.mDialpadFragment instanceof DialpadFragment) {
                            ((DialpadFragment) this.mDialpadFragment).setShowDialpadFlag(true);
                            break;
                        }
                    }
                    launchNonPhoneActivity();
                    return;
                    break;
                case 143:
                    if (this.mFragmentInitialized) {
                        handleLauncher();
                        if (!(this.mAllFragment instanceof DummyContactBrowseListFragment) && this.mAllFragment.isCancelFromCcard) {
                            this.mAllFragment.isCancelFromCcard = false;
                        }
                    } else {
                        this.mActionBarAdapter.correctLauncherMode(this.mRequest.getActivityTitle().toString(), true);
                    }
                    if (TabState.ALL != this.mActionBarAdapter.getCurrentTab()) {
                        if (!(this.mDialpadFragment instanceof DialpadFragment)) {
                            doWithDelay = true;
                        } else if (TabState.DIALER == this.mActionBarAdapter.getCurrentTab()) {
                            resetAndCheckInCallForDialer();
                        }
                    }
                    if (this.mDialpadFragment instanceof DialpadFragment) {
                        ((DialpadFragment) this.mDialpadFragment).setShowDialpadFlag(true);
                        break;
                    }
                    break;
            }
        }
        restartFragmentReplacement();
        configureStates(false, true);
    }

    private void handleConfigirationWithDelay(boolean aSearchMode, boolean aHandleConfigStates, long aWaitTime, boolean aCheckDialerAlso) {
        this.mConfigRunable = new ConfigirationRunnable(aSearchMode, aHandleConfigStates, aCheckDialerAlso);
        this.mHandler.postDelayed(this.mConfigRunable, aWaitTime);
    }

    private void configureStates(boolean aSearchMode, boolean aHandleOnlyContractLsitFragment) {
        if (!aHandleOnlyContractLsitFragment) {
            if (aSearchMode) {
                this.mAllFragment.setQueryText(this.mRequest.getQueryString());
                this.mAllFragment.setSearchMode(true);
            }
            HwLog.i("PeopleActivity", "configureStates() simplefied:" + this.mIsSimplifiedModeEnabled + " aSearchMode" + aSearchMode);
            if (this.mIsSimplifiedModeEnabled) {
                hideOrDisplayFragmentSearchBar(true);
            } else {
                hideOrDisplayFragmentSearchBar(aSearchMode);
            }
        }
        configureContactListFragment();
    }

    private boolean resetAndCheckInCallForDialer() {
        if (!(this.mDialpadFragment instanceof DialpadFragment)) {
            return false;
        }
        EditText lDigits = ((DialpadFragment) this.mDialpadFragment).getDialerEditText();
        if (!(lDigits == null || lDigits.getText() == null)) {
            ((DialpadFragment) this.mDialpadFragment).setRow0ShownWhenRecreate(false);
            if (HwLog.HWDBG) {
                HwLog.d("PeopleActivity", "setRow0ShownWhenRecreate false peopleactivity");
            }
            ((DialpadFragment) this.mDialpadFragment).clearDigitsText();
        }
        return true;
    }

    private void handleLauncher() {
        CharSequence lActivityTitle = this.mRequest.getActivityTitle();
        if (lActivityTitle.equals("com.android.contacts.activities.PeopleActivity")) {
            PLog.d(1009, "set to contact list");
            this.mActionBarAdapter.setCurrentTab(TabState.ALL);
        } else if (lActivityTitle.equals("com.android.contacts.activities.DialtactsActivity")) {
            PLog.d(1008, "set to dialer");
            this.mActionBarAdapter.setCurrentTab(TabState.DIALER);
        } else {
            this.mActionBarAdapter.correctLauncherMode(lActivityTitle.toString(), false);
        }
    }

    public void onContactListFilterChanged() {
        if (this.mAllFragment != null && this.mAllFragment.isAdded()) {
            this.mAllFragment.setFilterController(this.mContactListFilterController);
            invalidateOptionsMenuIfNeeded();
        }
    }

    public void onAction(int action) {
        switch (action) {
            case 0:
                setQueryTextToFragment(this.mActionBarAdapter.getQueryString());
                return;
            case 1:
                configureFragments(false);
                updateFragmentsVisibility(new boolean[0]);
                return;
            case 2:
                setQueryTextToFragment("");
                updateFragmentsVisibility(false);
                return;
            default:
                throw new IllegalStateException("Unkonwn ActionBarAdapter action: " + action);
        }
    }

    public void onSelectedTabChanged() {
        boolean z = false;
        if (this.mAllFragment != null) {
            this.mAllFragment.hidePhoneBookOverLay();
        }
        updateFragmentsVisibility(new boolean[0]);
        int tab = this.mActionBarAdapter.getCurrentTab();
        if (this.mCust != null) {
            this.mCust.checkAndStartSyncClient(this, tab, false);
        }
        if (tab == TabState.DIALER && (this.mDialpadFragment instanceof DialpadFragment)) {
            this.mDialpadFragment.displayDigits();
        }
        if (this.mFavorOrYPFragment instanceof FavoriteGroupFragment) {
            FavoriteGroupFragment favoriteGroupFragment = (FavoriteGroupFragment) this.mFavorOrYPFragment;
            if (tab == TabState.FAVOR_YELLOWPAGE) {
                z = true;
            }
            favoriteGroupFragment.setCurrentIsShow(z);
        }
        TabSelectedListener lListener = (TabSelectedListener) this.mTabSelectedListener.get(Integer.valueOf(tab));
        if (lListener != null) {
            lListener.onCurrentSelectedTab();
        }
    }

    public void setTabSelectedListener(int tabId, TabSelectedListener listener) {
        if (listener == null) {
            this.mTabSelectedListener.remove(Integer.valueOf(tabId));
        } else {
            this.mTabSelectedListener.put(Integer.valueOf(tabId), listener);
        }
    }

    private void updateFragmentsVisibility(boolean... isSmooth) {
        int tab = this.mActionBarAdapter.getCurrentTab();
        if (this.mActionBarAdapter.isSearchMode()) {
            this.mTabPagerAdapter.setSearchMode(true);
        } else {
            this.mTabPagerAdapter.setSearchMode(false);
            if (this.mTabPager.getCurrentItem() != tab) {
                if (isSmooth == null || isSmooth.length <= 0) {
                    this.mTabPager.setCurrentItem(tab);
                } else {
                    this.mTabPager.setCurrentItem(tab, isSmooth[0]);
                }
            }
        }
        if (getResources().getConfiguration().orientation == 2) {
            invalidateOptionsMenu();
        }
    }

    public void hideOrDisplayFragmentSearchBar(boolean show) {
        View lSearchLayout = findViewById(R.id.inner_contactListsearchlayout);
        View contactsSearchView = this.mAllFragment == null ? null : this.mAllFragment.getContactsSearchView();
        if (lSearchLayout != null && contactsSearchView != null) {
            if (show) {
                contactsSearchView.requestFocus();
                lSearchLayout.setBackgroundResource(R.drawable.textfield_activated_holo_light);
                if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
                    ((InputMethodManager) getSystemService("input_method")).showSoftInput(contactsSearchView, 0);
                }
                contactsSearchView.setCursorVisible(true);
                return;
            }
            contactsSearchView.setText("");
            contactsSearchView.setCursorVisible(false);
        }
    }

    private void setQueryTextToFragment(String query) {
        this.mAllFragment.setQueryString(query, true);
        if (!this.mIsSimplifiedModeEnabled && !(this.mAllFragment instanceof DummyContactBrowseListFragment)) {
            DefaultContactBrowseListFragment defaultContactBrowseListFragment = this.mAllFragment;
            boolean z = (this.mAllFragment.isSearchMode() || this.mAllFragment.getAdapter() == null) ? false : ((ContactListAdapter) this.mAllFragment.getAdapter()).getCount() > getResources().getInteger(R.integer.contact_per_screen);
            defaultContactBrowseListFragment.setVisibleScrollbarEnabled(z);
        }
    }

    private void configureContactListFragmentForRequest() {
        Uri contactUri = this.mRequest.getContactUri();
        if (contactUri != null) {
            this.mAllFragment.setSelectedContactUri(contactUri);
        }
        this.mAllFragment.setFilterController(this.mContactListFilterController);
        if (this.mRequest.isDirectorySearchEnabled()) {
            this.mAllFragment.setDirectorySearchMode(1);
        } else {
            this.mAllFragment.setDirectorySearchMode(0);
        }
    }

    private void configureContactListFragment() {
        int i;
        this.mAllFragment.setFilterController(this.mContactListFilterController);
        DefaultContactBrowseListFragment defaultContactBrowseListFragment = this.mAllFragment;
        if (CommonUtilMethods.isLayoutRTL()) {
            i = 1;
        } else {
            i = 2;
        }
        defaultContactBrowseListFragment.setVerticalScrollbarPosition(i);
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            this.mAllFragment.setSelectionVisible(false);
        } else if (2 == ContactSplitUtils.getColumnsNumber(this, isInMultiWindowMode())) {
            this.mAllFragment.setSelectionVisible(true);
            this.mAllFragment.setHighLightVisible(true);
        } else {
            this.mAllFragment.setSelectionVisible(false);
            this.mAllFragment.setHighLightVisible(false);
        }
        this.mAllFragment.setQuickContactEnabled(true);
    }

    public void onProviderStatusChange() {
        updateViewConfiguration(false);
    }

    private void updateViewConfiguration(boolean forceUpdate) {
        int providerStatus = this.mProviderStatusWatcher.getProviderStatus();
        if (forceUpdate || this.mProviderStatus == null || !this.mProviderStatus.equals(Integer.valueOf(providerStatus))) {
            this.mProviderStatus = Integer.valueOf(providerStatus);
            ViewStub contactsUnavailableViewStub = (ViewStub) findViewById(R.id.contacts_unavailable_view_stub);
            View contactsUnavailableView = findViewById(R.id.contacts_unavailable_view);
            Fragment fragment;
            if (this.mProviderStatus.intValue() == 0 || this.mProviderStatus.intValue() == 2) {
                if (contactsUnavailableView != null) {
                    contactsUnavailableView.setVisibility(8);
                }
                fragment = getFragmentManager().findFragmentByTag("contacts_unavailable_tag");
                if (fragment != null) {
                    getFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
                    this.mContactsUnavailableFragment = null;
                }
                if (this.mAllFragment != null) {
                    this.mAllFragment.setEnabled(true);
                }
                this.mTabPager.setVisibility(0);
            } else if (areContactWritableAccountsAvailable() || !AccountPromptUtils.shouldShowAccountPrompt(this)) {
                if (this.mAllFragment != null) {
                    this.mAllFragment.setEnabled(false);
                }
                if (contactsUnavailableView == null) {
                    contactsUnavailableViewStub.inflate();
                    contactsUnavailableView = findViewById(R.id.contacts_unavailable_view);
                }
                fragment = getFragmentManager().findFragmentByTag("contacts_unavailable_tag");
                if (fragment != null) {
                    getFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
                }
                this.mContactsUnavailableFragment = new ContactsUnavailableFragment();
                getFragmentManager().beginTransaction().replace(R.id.contacts_unavailable_container, this.mContactsUnavailableFragment, "contacts_unavailable_tag").commitAllowingStateLoss();
                this.mContactsUnavailableFragment.updateStatus(this.mProviderStatus.intValue());
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    contactsUnavailableView.setPadding(0, ContactDpiAdapter.getStatusBarHeight(this.mContext), 0, 0);
                    ScreenUtils.adjustPaddingTop(this.mContext, findViewById(R.id.contacts_unavailable_container), true);
                }
                contactsUnavailableView.setVisibility(0);
                this.mTabPager.setVisibility(4);
            } else {
                AccountPromptUtils.launchAccountPrompt(this);
                return;
            }
            if (!forceUpdate) {
                invalidateOptionsMenuIfNeeded();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.mOptionsMenuContactsAvailable = areContactsAvailable();
        this.mOptionsMenu = menu;
        getMenuInflater().inflate(R.menu.actions, menu);
        if (!this.mOptionsMenuContactsAvailable) {
            menu.setGroupVisible(R.id.phone_options, false);
            menu.setGroupVisible(R.id.dialer_options, false);
            return true;
        } else if (!this.isFirstCreateOptionsMenuOrLandScape) {
            return true;
        } else {
            if (this.mCust != null) {
                this.mCust.customizeOptionsMenu(this, menu, menu.findItem(R.id.overflow_menu_contacts).getSubMenu());
            }
            if (getResources().getConfiguration().orientation == 1) {
                this.isFirstCreateOptionsMenuOrLandScape = false;
            }
            return true;
        }
    }

    private void invalidateOptionsMenuIfNeeded() {
        if (isOptionsMenuChanged()) {
            invalidateOptionsMenu();
        }
    }

    public boolean isOptionsMenuChanged() {
        if (this.mOptionsMenuContactsAvailable != areContactsAvailable()) {
            return true;
        }
        return false;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean showMiscOptions = false;
        if (this.mActionBarAdapter == null) {
            return false;
        }
        boolean isSearchMode = this.mActionBarAdapter.isSearchMode();
        this.mAllFragment.setSearchModeInitialized(isSearchMode);
        int currentTab = this.mActionBarAdapter.getCurrentTab();
        if (!this.mOptionsMenuContactsAvailable) {
            return false;
        }
        if (isSearchMode) {
            currentTab = TabState.ALL;
        }
        boolean isContactsAvailable = (this.mAllFragment.getContactsCount() > 0 || !(this.mContactListFilterController == null || this.mContactListFilterController.getFilter().filterType == -2)) ? true : !SharePreferenceUtil.getDefaultSp_de(getApplicationContext()).getBoolean("preference_show_sim_contacts", true);
        if (TabState.FAVOR_YELLOWPAGE == currentTab) {
            menu.setGroupVisible(R.id.phone_options, false);
            menu.setGroupVisible(R.id.dialer_options, false);
        } else if (TabState.ALL == currentTab) {
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                menu.setGroupVisible(R.id.phone_options, false);
            } else {
                menu.setGroupVisible(R.id.phone_options, isContactsAvailable);
            }
            menu.setGroupVisible(R.id.dialer_options, false);
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                return false;
            }
            if (!EmuiFeatureManager.isCamcardEnabled() || this.mSimpleMode || EmuiFeatureManager.isSuperSaverMode()) {
                menu.findItem(R.id.menu_scan_card).setVisible(false);
            }
            this.mAllFragment.onPrepareOptionsMenu(menu);
            if (!isSearchMode) {
                showMiscOptions = true;
            }
            makeMenuItemVisible(menu, R.id.menu_search, showMiscOptions);
        } else if (TabState.DIALER == currentTab) {
            menu.setGroupVisible(R.id.phone_options, false);
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                menu.setGroupVisible(R.id.dialer_options, false);
                return false;
            }
            this.mDialpadFragment.onPrepareOptionsMenu(menu);
        } else {
            HwLog.w("PeopleActivity", "Unknown tab state: " + currentTab);
        }
        return true;
    }

    private void makeMenuItemVisible(Menu menu, int itemId, boolean visible) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) {
            item.setVisible(visible);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (TabState.DIALER == this.mActionBarAdapter.getCurrentTab() && this.mDialpadFragment.onOptionsItemSelected(item)) {
            return true;
        }
        if (TabState.ALL == this.mActionBarAdapter.getCurrentTab() && this.mAllFragment.onOptionsItemSelected(item)) {
            return true;
        }
        if (TabState.FAVOR_YELLOWPAGE == this.mActionBarAdapter.getCurrentTab() && (this.mFavorOrYPFragment instanceof FavoriteGroupFragment) && this.mFavorOrYPFragment.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case 16908332:
                if (this.mActionBarAdapter.isUpShowing()) {
                    onBackPressed();
                }
                return true;
            case R.id.menu_add_contact:
                startActivity(new Intent("android.intent.action.INSERT", Contacts.CONTENT_URI));
                StatisticalHelper.sendReport((int) AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, "0");
                return true;
            case R.id.menu_settings_dialer:
                startActivity(DialtactsActivity.getCallSettingsIntent());
                new HwAnimationReflection(this).overrideTransition(1);
                return true;
            default:
                return false;
        }
    }

    public boolean onSearchRequested() {
        if (getCurrentTab() == TabState.ALL) {
            this.mActionBarAdapter.setSearchMode(true);
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == -1) {
                    this.mAllFragment.onPickerResult(data);
                    return;
                }
                return;
            case 3001:
                this.mIsNeedMaskDefault = false;
                CommonUtilMethods.clearInstanceState();
                return;
            case 3002:
            case 3003:
                this.mIsNeedMaskDialpad = false;
                CommonUtilMethods.clearInstanceState();
                return;
            default:
                HwLog.w("PeopleActivity", "Unknown request code: " + requestCode);
                return;
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        long duration = event.getEventTime() - event.getDownTime();
        boolean mInrelationsearchmode = false;
        if (getIntent() != null) {
            mInrelationsearchmode = "android.intent.action.SEARCH".equals(getIntent().getAction()) ? this.mActionBarAdapter.isSearchMode() : false;
        }
        if (this.mOptionsMenu != null && keyCode == 82 && duration < ((long) ViewConfiguration.getLongPressTimeout()) && !r1) {
            int currentTab = this.mActionBarAdapter.getCurrentTab();
            if (TabState.ALL == currentTab) {
                if (!this.mIsPortrait) {
                    this.mOptionsMenu.performIdentifierAction(R.id.overflow_menu_contacts, 1);
                } else if (!(this.mAllFragment instanceof DummyContactBrowseListFragment)) {
                    this.mAllFragment.handleKeyEvent(82);
                }
                return true;
            } else if (TabState.FAVOR_YELLOWPAGE == currentTab) {
                return true;
            } else {
                if (TabState.DIALER == currentTab) {
                    if (!this.mIsPortrait) {
                        this.mOptionsMenu.performIdentifierAction(R.id.overflow_menu_dialer, 1);
                    } else if (this.mDialpadFragment instanceof DialpadFragment) {
                        ((DialpadFragment) this.mDialpadFragment).showPopupMenu();
                        StatisticalHelper.report(CommonStatusCodes.AUTH_URL_RESOLUTION);
                    }
                    return true;
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 4:
                if (TabState.ALL == this.mActionBarAdapter.getCurrentTab() || TabState.FAVOR_YELLOWPAGE == this.mActionBarAdapter.getCurrentTab()) {
                    this.mPrefs.edit().putBoolean("PeopleActivity.isHomeOrBackPressed", true).apply();
                }
                if (this.mDialpadFragment instanceof DialpadFragment) {
                    ((DialpadFragment) this.mDialpadFragment).onBackPressed();
                    break;
                }
                break;
            case Place.TYPE_NIGHT_CLUB /*67*/:
                if (deleteSelection()) {
                    return true;
                }
                break;
            default:
                int unicodeChar = event.getUnicodeChar();
                if (!(unicodeChar == 0 || (Integer.MIN_VALUE & unicodeChar) != 0 || Character.isWhitespace(unicodeChar))) {
                    String query = new String(new int[]{unicodeChar}, 0, 1);
                    if (!this.mActionBarAdapter.isSearchMode() && getCurrentTab() == TabState.ALL) {
                        this.mActionBarAdapter.setQueryString(query);
                        this.mActionBarAdapter.setSearchMode(true);
                        return true;
                    }
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void cancleSearchMode() {
        this.mActionBarAdapter.setSearchMode(false);
    }

    public void onBackPressed() {
        if (isSafeToCommitTransactions()) {
            if (this.mActionBarAdapter.isSearchMode()) {
                this.mAllFragment.deleteSearchfieldforSearchContacts();
                this.mActionBarAdapter.setSearchMode(false);
            } else if (!isTaskRoot()) {
                super.onBackPressed();
            } else if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                if (!isBackPressed()) {
                    this.mIsNeedShowAnimate = true;
                    showOrHideActionbar();
                } else if (moveTaskToBack(false)) {
                    this.mAllFragment.onBackPressed();
                } else {
                    super.onBackPressed();
                }
                return;
            } else if (moveTaskToBack(false)) {
                this.mAllFragment.onBackPressed();
            } else {
                super.onBackPressed();
            }
            return;
        }
        HwLog.e("PeopleActivity", "Can not perform this action after onSaveInstanceState");
    }

    public Animator getAnimator(View view, int transit, boolean enter) {
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            return null;
        }
        HwFragmentContainer fc = getFragmentContainer(this.mActionBarAdapter.getCurrentTab());
        if (fc == null || view == null) {
            return null;
        }
        return fc.getAnimator(view, transit, enter);
    }

    private boolean deleteSelection() {
        return false;
    }

    protected void onSaveInstanceState(Bundle outState) {
        if (!(CommonUtilMethods.calcIfNeedSplitScreen() && outState.getBoolean("save_instance_state_manually"))) {
            super.onSaveInstanceState(outState);
        }
        this.mActionBarAdapter.onSaveInstanceState(outState);
        if (this.mContactDetailLayoutController != null) {
            this.mContactDetailLayoutController.onSaveInstanceState(outState);
        }
        this.mActionBarAdapter.setListener(null);
        if (this.mTabPager != null) {
            this.mTabPager.setOnPageChangeListener(null);
        }
        outState.putBoolean("current_mode", this.mIsSimplifiedModeEnabled);
        outState.putBoolean("key_mask_default", this.mIsNeedMaskDefault);
        outState.putBoolean("key_mask_dialpad", this.mIsNeedMaskDialpad);
        if (this.mCust != null) {
            this.mCust.onSaveInstanceState(outState);
        }
    }

    public void saveInstanceValues() {
        Bundle outState = new Bundle();
        outState.putBoolean("save_instance_state_manually", true);
        onSaveInstanceState(outState);
        this.mFavorOrYPFragment.onSaveInstanceState(outState);
        this.mAllFragment.saveInstanceState(outState);
        if (this.mDialpadFragment instanceof DialpadFragment) {
            ((DialpadFragment) this.mDialpadFragment).saveInstanceState(outState);
        }
        CommonUtilMethods.saveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (this.mIsChangeToNormalMode) {
            this.mDiscardTabState = true;
        }
        this.isRestore = true;
        super.onRestoreInstanceState(savedInstanceState);
        this.mDiscardTabState = false;
        if (this.mActionBarAdapter.isSearchMode()) {
            this.mActionBarAdapter.setFocusOnSearchView();
        }
        this.isRestore = false;
    }

    public DialogManager getDialogManager() {
        return this.mDialogManager;
    }

    public ContactBrowseListFragment getListFragment() {
        return this.mAllFragment;
    }

    public void onContactVisibilityChanged(boolean aIsChanged) {
        if (this.mAllFragment != null && this.mAllFragment.isAdded()) {
            if (aIsChanged) {
                this.mAllFragment.setFilterController(this.mContactListFilterController, true, aIsChanged);
            } else {
                this.mAllFragment.setFilterController(this.mContactListFilterController);
            }
            invalidateOptionsMenuIfNeeded();
        }
    }

    public void setContextMenuStatus(boolean aContextMenuStatus) {
        this.mIsContextMenuClosed = aContextMenuStatus;
    }

    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        this.mIsContextMenuClosed = true;
    }

    public int getCurrentTab() {
        int curTab = TabState.DEFAULT;
        if (this.mActionBarAdapter != null) {
            return this.mActionBarAdapter.getCurrentTab();
        }
        return curTab;
    }

    public void setDialpadFragment(DialpadFragment dialpadFragment) {
        HwLog.e("Optimization", "DialpadFragment replaced");
        this.mDialpadFragment = dialpadFragment;
    }

    public void setFavorOrYPFragment(Fragment favorOrYPFragment) {
        if (HwLog.HWFLOW) {
            HwLog.i("PeopleActivity", "FavorOrYPFragment replaced");
        }
        this.mFavorOrYPFragment = favorOrYPFragment;
    }

    public void setAllFragment(DefaultContactBrowseListFragment allFragment) {
        HwLog.e("Optimization", "DefaultContactBrowseListFragment replaced");
        DummyContactBrowseListFragment dummyContactBrowseListFragment = null;
        if (this.mAllFragment instanceof DummyContactBrowseListFragment) {
            dummyContactBrowseListFragment = this.mAllFragment;
        }
        this.mAllFragment = allFragment;
        this.mAllFragment.setOnContactListActionListener(new ContactBrowserActionListener());
        if (!(dummyContactBrowseListFragment == null || dummyContactBrowseListFragment.getFilter() == null)) {
            this.mAllFragment.setFilter(dummyContactBrowseListFragment.getFilter(), dummyContactBrowseListFragment.mRestoreSelectedUri, dummyContactBrowseListFragment.mIsChanged);
        }
        this.mAllFragment.setHasOptionsMenu(true);
        this.mAllFragment.setOnContactListActionListener(new ContactBrowserActionListener());
    }

    public void refreshPager() {
        CommonUtilMethods.disableFrameRadar("PeopleActivity_refreshPager");
        this.mTabPagerAdapter.notifyDataSetChanged();
        if (this.mDialpadFragment != null && (this.mDialpadFragment instanceof DialpadFragment)) {
            ((DialpadFragment) this.mDialpadFragment).loadDelayLoadingLayout();
        }
        CommonUtilMethods.enableFrameRadar("PeopleActivity_refreshPager");
    }

    private void cancelReplacerTask() {
        if (this.mFragmentReplacer != null) {
            if (!this.mFragmentReplacer.isCancelled()) {
                this.mFragmentReplacer.cencal();
            }
            this.mFragmentReplacer.cancelInflateDetailsView();
        }
    }

    private boolean isAllFragmentReplace() {
        if (this.mAllFragment.isReplacable() || (this.mFavorOrYPFragment instanceof DummyFragment) || (this.mDialpadFragment instanceof DummyFragment)) {
            return false;
        }
        return true;
    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (this.mGlobalDialogReference != null) {
            this.mGlobalDialogReference.dismiss();
        }
        if (this.mGlobalRoamingDialogReference != null) {
            this.mGlobalRoamingDialogReference.dismiss();
        }
        if (this.mGlobalDialogViewBy != null) {
            this.mGlobalDialogViewBy.dismiss();
        }
        if (this.mContactsApp != null) {
            this.mContactsApp.setIsLaunching(false);
        }
    }

    public void openRightContainer(Fragment f) {
        if (f != null) {
            int curTabIndex = this.mActionBarAdapter.getCurrentTab();
            if (TabState.FAVOR_YELLOWPAGE == curTabIndex) {
                if (this.mFavorOrYPFragment instanceof FavoriteGroupFragment) {
                    ((FavoriteGroupFragment) this.mFavorOrYPFragment).openRightContainer(f);
                }
            } else if (TabState.ALL == curTabIndex && !(this.mAllFragment instanceof DummyContactBrowseListFragment)) {
                this.mAllFragment.openRightContainer(f);
            }
        }
    }

    public void changeRightContainer(Fragment now_fragment, Fragment next_fragment) {
        if (next_fragment != null) {
            int curTabIndex = this.mActionBarAdapter.getCurrentTab();
            if (TabState.FAVOR_YELLOWPAGE == curTabIndex) {
                if (this.mFavorOrYPFragment instanceof FavoriteGroupFragment) {
                    ((FavoriteGroupFragment) this.mFavorOrYPFragment).changeRightContainer(now_fragment, next_fragment);
                }
            } else if (TabState.ALL == curTabIndex && !(this.mAllFragment instanceof DummyContactBrowseListFragment)) {
                this.mAllFragment.changeRightContainer(now_fragment, next_fragment);
            }
        }
    }

    public FragmentManager getFrameFragmentManager() {
        int curTabIndex = this.mActionBarAdapter.getCurrentTab();
        if (TabState.FAVOR_YELLOWPAGE == curTabIndex) {
            if (this.mFavorOrYPFragment instanceof FavoriteGroupFragment) {
                return this.mFavorOrYPFragment.getChildFragmentManager();
            }
        } else if (TabState.ALL == curTabIndex && !(this.mAllFragment instanceof DummyContactBrowseListFragment)) {
            return this.mAllFragment.getChildFragmentManager();
        }
        return null;
    }

    public boolean isBackPressed() {
        int curTabIndex = this.mActionBarAdapter.getCurrentTab();
        if (TabState.FAVOR_YELLOWPAGE == curTabIndex) {
            if (this.mFavorOrYPFragment instanceof FavoriteGroupFragment) {
                return ((FavoriteGroupFragment) this.mFavorOrYPFragment).isBackPressed();
            }
        } else if (TabState.ALL == curTabIndex && !(this.mAllFragment instanceof DummyContactBrowseListFragment)) {
            return this.mAllFragment.isBackPressed();
        }
        return true;
    }

    public HwFragmentContainer getFragmentContainer(int curTabIndex) {
        if (TabState.FAVOR_YELLOWPAGE == curTabIndex) {
            if (this.mFavorOrYPFragment instanceof FavoriteGroupFragment) {
                return ((FavoriteGroupFragment) this.mFavorOrYPFragment).getFragmentContainer();
            }
        } else if (TabState.ALL == curTabIndex && !(this.mAllFragment instanceof DummyContactBrowseListFragment)) {
            return this.mAllFragment.getFragmentContainer();
        }
        return null;
    }

    private boolean isInMainTabPage(HwFragmentContainer fgContainer) {
        return 1 != ContactSplitUtils.getColumnsNumber(this, isInMultiWindowMode()) || fgContainer.getSelectedContainer() == 0 || fgContainer.getSelectedContainer() == -1;
    }

    public void showOrHideActionbar() {
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            HwFragmentContainer fgContainer = getFragmentContainer(this.mActionBarAdapter.getCurrentTab());
            if (fgContainer == null) {
                showOrHideActionbar(true);
            } else {
                showOrHideActionbar(isInMainTabPage(fgContainer));
            }
        }
    }

    public void showOrHideActionbar(boolean isShow) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && getCurrentTab() != TabState.DIALER) {
            ActionBar actionbar = getActionBar();
            if (actionbar != null) {
                HapViewPager hapViewPager = null;
                if (this.mTabPager instanceof HapViewPager) {
                    hapViewPager = this.mTabPager;
                }
                if (isShow) {
                    CommonUtilMethods.disableActionBarShowHideAnimation(actionbar);
                    if (this.mIsNeedShowAnimate) {
                        ContactSplitUtils.startActionbarShowAnimate(actionbar, findViewById(16909290));
                    } else {
                        actionbar.show();
                    }
                    if (hapViewPager != null) {
                        hapViewPager.disableViewPagerSlide(false);
                    }
                } else {
                    CommonUtilMethods.disableActionBarShowHideAnimation(actionbar);
                    actionbar.hide();
                    if (hapViewPager != null) {
                        hapViewPager.disableViewPagerSlide(true);
                    }
                }
                this.mIsNeedShowAnimate = false;
            }
        }
    }

    public boolean isInMainTabPage() {
        HwFragmentContainer fgContainer = getFragmentContainer(this.mActionBarAdapter.getCurrentTab());
        if (fgContainer == null) {
            return true;
        }
        return isInMainTabPage(fgContainer);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction() & 255;
        if (2 == action || !CommonUtilMethods.calcIfNeedSplitScreen()) {
            return super.dispatchTouchEvent(event);
        }
        if ((1 == action || 3 == action) && getCurrentTab() != TabState.DIALER && isInMainTabPage() && this.mTabPager != null && (this.mTabPager instanceof HapViewPager)) {
            ((HapViewPager) this.mTabPager).disableViewPagerSlide(false);
        }
        if (!getNeedMaskDefault() || action != 0 || !ContactSplitUtils.isSpiltTwoColumn(this, isInMultiWindowMode())) {
            return super.dispatchTouchEvent(event);
        }
        int actinbarHight = ContactDpiAdapter.getActionbarHeight(this);
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        frame.bottom = frame.top + actinbarHight;
        if (frame.contains(x, y)) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("android.intent.action.FINISH_MULTI_SELECTED"));
        }
        return super.dispatchTouchEvent(event);
    }

    public int getRightFramelayoutWidth(boolean bLandscape) {
        return getFramelayoutWidth(bLandscape, false);
    }

    private int getFramelayoutWidth(boolean bLandscape, boolean bLeft) {
        int curTabIndex = this.mActionBarAdapter.getCurrentTab();
        float displayRate = 0.0f;
        if (TabState.FAVOR_YELLOWPAGE == curTabIndex) {
            if (this.mFavorOrYPFragment instanceof FavoriteGroupFragment) {
                displayRate = ((FavoriteGroupFragment) this.mFavorOrYPFragment).getDisplayRate(bLandscape);
            }
        } else if (TabState.ALL == curTabIndex && !(this.mAllFragment instanceof DummyContactBrowseListFragment)) {
            displayRate = this.mAllFragment.getDisplayRate(bLandscape);
        }
        if (!bLeft) {
            displayRate = 1.0f - displayRate;
        }
        int appWidth = getWindowWidth(bLandscape);
        if (displayRate <= 0.0f || displayRate >= 1.0f) {
            return 0;
        }
        return (int) (((float) appWidth) * displayRate);
    }

    public void onServiceCompleted(Intent callbackIntent) {
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            super.onServiceCompleted(callbackIntent);
        } else if (callbackIntent != null) {
            Uri lookUp = callbackIntent.getData();
            boolean isGroupUri = false;
            if (lookUp != null && lookUp.toString().startsWith(Groups.CONTENT_URI.toString())) {
                isGroupUri = true;
            }
            if (getCurrentTab() == TabState.DEFAULT && isGroupUri) {
                Fragment groupBrowse = this.mAllFragment.getChildFragmentManager().findFragmentByTag(GroupBrowseListFragment.class.getName());
                Fragment groupDetail = this.mAllFragment.getChildFragmentManager().findFragmentByTag(GroupDetailFragment.class.getName());
                if ((groupBrowse instanceof GroupBrowseListFragment) && groupDetail == null) {
                    ((GroupBrowseListFragment) groupBrowse).onNewIntent(callbackIntent);
                }
            }
            if (!isGroupUri) {
                try {
                    Uri uriCurrentFormat = ContactLoaderUtils.ensureIsContactUri(getContentResolver(), lookUp);
                    if (!(uriCurrentFormat == null || (this.mAllFragment instanceof DummyContactBrowseListFragment))) {
                        this.mAllFragment.showContactFromNewContact(uriCurrentFormat);
                    }
                } catch (IllegalArgumentException e) {
                    HwLog.e("PeopleActivity", "contact uri exception ");
                }
            }
        }
    }

    private int getWindowWidth(boolean bLandscape) {
        Rect displayRect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRect);
        int displayWidth = displayRect.right - displayRect.left;
        int displayHeght = (displayRect.bottom - displayRect.top) + ContactDpiAdapter.getStatusBarHeight(this.mContext);
        return 2 == getResources().getConfiguration().orientation ? bLandscape ? displayWidth : displayHeght : bLandscape ? displayHeght : displayWidth;
    }

    public ContactListFilterController getFilterController() {
        return this.mContactListFilterController;
    }

    private void closeCustomOptionMenu() {
        int curTabIndex = this.mActionBarAdapter.getCurrentTab();
        if (TabState.DIALER == curTabIndex) {
            if (this.mDialpadFragment instanceof DialpadFragment) {
                ((DialpadFragment) this.mDialpadFragment).handleKeyEvent(4);
            }
        } else if (TabState.FAVOR_YELLOWPAGE != curTabIndex && TabState.ALL == curTabIndex && !(this.mAllFragment instanceof DummyContactBrowseListFragment)) {
            this.mAllFragment.handleKeyEvent(4);
        }
    }

    private void restartFragmentReplacement() {
        if (!isAllFragmentReplace() && (this.mFragmentReplacer == null || this.mFragmentReplacer.isCancelled())) {
            this.mFragmentReplacer = new FragmentReplacer(this);
            this.mFragmentReplacer.inflateDetailsViewWithDelay();
        } else if (this.mFragmentReplacer != null) {
            if (HwLog.HWDBG) {
                HwLog.d("PeopleActivity", "restartFragmentReplacement: commitIfPending");
            }
            this.mFragmentReplacer.commitIfPending();
        }
    }

    public void clearCurrentFocus() {
        View lCurrentFocus = getCurrentFocus();
        if (lCurrentFocus != null) {
            lCurrentFocus.clearFocus();
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        CommonUtilMethods.disableFrameRadar("PeopleActivity_onWindowFocusChanged");
        loadTheRestLayout();
        if (this.mActionBarAdapter == null || this.mDialpadFragment == null) {
            CommonUtilMethods.enableFrameRadar("PeopleActivity_onWindowFocusChanged");
            return;
        }
        if (hasFocus && (this.mDialpadFragment instanceof DialpadFragment) && this.mActionBarAdapter.getCurrentTab() == TabState.DIALER) {
            CallLogFragment lCallLogFragment = ((DialpadFragment) this.mDialpadFragment).getCallLogFragment();
            if (lCallLogFragment != null && lCallLogFragment.isAdded() && lCallLogFragment.mAdapter != null && lCallLogFragment.mAdapter.isInvalidateRequired() && lCallLogFragment.mListView != null && lCallLogFragment.mListView.hasWindowFocus()) {
                lCallLogFragment.mListView.invalidateViews();
                lCallLogFragment.mAdapter.notifyDataSetChanged();
                lCallLogFragment.mAdapter.clearInvalidateRequiredFlag();
            }
        }
        CommonUtilMethods.enableFrameRadar("PeopleActivity_onWindowFocusChanged");
    }

    private void loadTheRestLayout() {
        if (this.mDialpadFragment != null && (this.mDialpadFragment instanceof DialpadFragment)) {
            ((DialpadFragment) this.mDialpadFragment).loadDelayLoadingLayout();
        }
    }

    private boolean deleteShareContactsFiles() {
        int i = 0;
        File dir = getCacheDir();
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return false;
        }
        File[] fileList = dir.listFiles();
        if (fileList == null) {
            return false;
        }
        boolean deleteFlag = true;
        int length = fileList.length;
        while (i < length) {
            File file = fileList[i];
            if (!(file == null || !file.getName().startsWith("share_contacts") || file.delete())) {
                HwLog.w("PeopleActivity", "File delete failed: " + file.getName());
                deleteFlag = false;
            }
            i++;
        }
        return deleteFlag;
    }

    public ViewPager getViewPager() {
        return this.mTabPager;
    }

    public Fragment createFavorOrYpFragment() {
        if (EmuiFeatureManager.isShowFavoritesTab(getApplicationContext())) {
            return new FavoriteGroupFragment();
        }
        return new YellowPageFragment();
    }

    private void launchNonPhoneActivity() {
        if (getIntent() != null) {
            Uri data = getIntent().getData();
            if (data != null && "tel".equals(data.getScheme())) {
                Intent intent = new Intent(this, NonPhoneActivity.class);
                intent.setData(data);
                startActivity(intent);
                finish();
            }
        }
    }

    public int getLaunchTab() {
        return this.mLauchTab;
    }

    public ContactListHelper getContactListHelper() {
        if (this.mHelper == null) {
            this.mHelper = ContactListHelper.createContactListHelper(this);
        }
        return this.mHelper;
    }

    public DialpadFragmentHelper getDialpadFragmentHelper() {
        if (this.mDialpadHelper == null) {
            this.mDialpadHelper = DialpadFragmentHelper.createDialpadFragmentHelper(this);
        }
        return this.mDialpadHelper;
    }

    private void preloadContactsData() {
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                if (HwLog.HWDBG) {
                    HwLog.d("PeopleActivity", "start preload Contacts Data!");
                }
                Uri uri = Contacts.CONTENT_URI.buildUpon().appendQueryParameter("android.provider.extra.ADDRESS_BOOK_INDEX", "true").build();
                PeopleActivity.this.mContactsPrefs = new ContactsPreferences(PeopleActivity.this.mContext);
                ContactListFilter mContactFilter = PeopleActivity.this.mContactListFilterController.getFilter();
                PeopleActivity.this.mContactsApp.setFirstStartContactsStatus(true);
                uri = CommonUtilMethods.configureFilterUri(PeopleActivity.this.mContext, mContactFilter, uri);
                CommonUtilMethods.configureFilterSelection(PeopleActivity.this.mContext, mContactFilter, PeopleActivity.this.selection, PeopleActivity.this.selectionArgs, false);
                String sortOrder = "sort_key_alt";
                if (PeopleActivity.this.mContactsPrefs.getSortOrder() == 1) {
                    sortOrder = "sort_key";
                }
                String[] mSelectionArgs = (String[]) PeopleActivity.this.selectionArgs.toArray(new String[PeopleActivity.this.selectionArgs.size()]);
                if (!PeopleActivity.this.isFinishing()) {
                    synchronized (CommonUtilMethods.mContactCursorLoad) {
                        PeopleActivity.this.mContactsApp.setPreLoadContactsCursor(PeopleActivity.this.mContactsApp.getContentResolver().query(uri, CommonUtilMethods.getProjection(PeopleActivity.this.mContactsPrefs), PeopleActivity.this.selection.toString(), mSelectionArgs, sortOrder, null));
                    }
                }
            }
        });
    }
}
