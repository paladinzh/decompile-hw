package com.android.contacts.list;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.Settings.System;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.services.core.AMapException;
import com.android.common.widget.CompositeCursorAdapter.Partition;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactSplitUtils;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GeoUtil;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactEditorActivity;
import com.android.contacts.activities.ContactInfoFragment;
import com.android.contacts.activities.GroupBrowserActivity;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.activities.ProfileSimpleCardActivity;
import com.android.contacts.activities.RequestPermissionsActivityBase;
import com.android.contacts.activities.VoiceSearchResultActivity;
import com.android.contacts.animate.AnimatorHelper.ContactAnimatorListener;
import com.android.contacts.animate.ContactListAnimatorHelper;
import com.android.contacts.compatibility.DirectoryCompat;
import com.android.contacts.detail.ContactDetailHelper;
import com.android.contacts.ext.phone.SetupPhoneAccount;
import com.android.contacts.fragment.DummyStubFragment;
import com.android.contacts.fragments.NoContentFragment;
import com.android.contacts.group.GroupBrowseListFragment;
import com.android.contacts.group.GroupDetailFragment;
import com.android.contacts.group.SmartGroupBrowseListFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.activities.FavoriteContactsActivity;
import com.android.contacts.hap.blacklist.BlacklistCommonUtils;
import com.android.contacts.hap.camcard.CCBrowseListFragment;
import com.android.contacts.hap.camcard.CCUtils;
import com.android.contacts.hap.camcard.bcr.CCSaveService;
import com.android.contacts.hap.camcard.bcr.CCardScanHandler;
import com.android.contacts.hap.hotline.HLUtils;
import com.android.contacts.hap.interactions.ImportContactsInteraction;
import com.android.contacts.hap.numbermark.YellowPageContactUtil;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.AlertDialogFragmet;
import com.android.contacts.hap.util.GenericHandler;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.hap.utils.ScreenUtils;
import com.android.contacts.hap.widget.SplitActionBarView;
import com.android.contacts.hap.widget.SplitActionBarView.OnCustomMenuListener;
import com.android.contacts.hap.widget.SplitActionBarView.SetButtonDetails;
import com.android.contacts.list.DirectorySearchManager.LogStatusChangedListener;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.Contact;
import com.android.contacts.model.ContactLoader;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.preference.ContactsPreferenceActivity;
import com.android.contacts.preference.DisplayOptionsPreferenceFragment;
import com.android.contacts.preference.OrganizeContactsFragment;
import com.android.contacts.profile.ProfileUtils;
import com.android.contacts.profile.ProfileUtils.ContactEntriesObject;
import com.android.contacts.statistical.ContactReport;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AccountFilterUtil;
import com.android.contacts.util.AccountSelectionUtil;
import com.android.contacts.util.Constants;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.ContactLoaderUtils;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HelpUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneNumberFormatter;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.widget.AutoScrollListView;
import com.android.contacts.widget.ContextMenuAdapter;
import com.android.contacts.widget.SuspentionScroller;
import com.google.android.collect.Lists;
import com.google.android.gms.R;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.CommonConstants;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCustUtils;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService.Stub;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.encode.QRCodeEncoder;
import huawei.com.android.internal.widget.HwFragmentContainer;
import huawei.com.android.internal.widget.HwFragmentLayout;
import huawei.com.android.internal.widget.HwFragmentLayout.HwFragmentLayoutCallback;
import java.util.ArrayList;
import java.util.List;

public class DefaultContactBrowseListFragment extends ContactBrowseListFragment implements Callback {
    private static final long[] sPriorityDirectoryIds = new long[]{0, 1000000000};
    private final boolean IS_CHINA_AREN;
    private MenuItem addContactMenu;
    private Handler dismissDialogHandler;
    private OnClickListener emptyContactScreenOnClickListener;
    public boolean isCancelFromCcard;
    private boolean isInPortrait;
    private boolean isInflate;
    private boolean isOnSaveInstance;
    private boolean isTellUserStubInflate;
    ContactsApplication mApp;
    private View mBlankFooter;
    private View mButtonGroup;
    private CCardScanHandler mCcardHandlr;
    private Contact mContactData;
    private ContactListFilterController mContactListFilterController;
    LoaderCallbacks<Contact> mContactLoadedListener;
    private int mContactsCount;
    private EditText mContactsSearchView;
    private final ContextMenuAdapter mContextMenuAdapter;
    private Button mCreateNewContactsButton;
    private HwCustDefaultContactBrowseListFragment mCust;
    private HwFragmentContainer mDefaultFrgContainer;
    private HwFragmentLayout mDefaultFrgLayout;
    private boolean mDelayIfRequired;
    private final LongSparseArray<Boolean> mDirectoryLoadedState;
    private float mDisplayRate;
    private ContactEntriesObject mEntriesObject;
    private TextView mExchangeButton;
    private TextView mExchangeFooter;
    private View mFooterButtons;
    private ArrayList<View> mFooterViews;
    private GenericHandler mGenHdlr;
    private SparseArray<View> mHeaderView;
    private int mHeaderViewHeight;
    private ArrayList<View> mHeaderViews;
    private Button mImportContactsButton;
    private boolean mIsChangeRight;
    private boolean mIsFromNewIntent;
    private boolean mIsHotlineEnabled;
    private boolean mIsNeedShowAnimate;
    private boolean mIsNeedShowSelect;
    private boolean mIsSimpleDisplayMode;
    private int mLastQueryLength;
    private String mLastSearchKey;
    private ContactLoader mLoader;
    private LogStatusChangedListener mLogStatusChangeListener;
    private ContactListAnimatorHelper mMenuAnimator;
    private Messenger mMessenger;
    private OnClickListener mOnlineSearchListener;
    private SharedPreferences mPrefs;
    private Uri mProfileLookupUri;
    private Bitmap mQrCodeBitmap;
    private Messenger mRemoteMessenger;
    private Bundle mSavedState;
    private View mSearchEmptyView;
    private View mSearchHeaderView;
    private LinearLayout mSearchLayout;
    private ViewStub mSearchListEmptyViewStub;
    private Uri mShareUri;
    private boolean mShouldVoiceSearchShow;
    private Button mSignInToAccountButton;
    private Uri mSplitSelectedContactUri;
    private int mSplitSelectedType;
    private SuspentionScroller mSuspentionScroller;
    private View mSyncBirthdayToCalendarView;
    private ViewStub mSyncBirthdayToCalendarViewStub;
    private Object mSyncObject;
    private View mTellUserBirthdayToCalendarView;
    private ViewStub mTellUserBirthdayToCalendarViewStub;
    private Context mTotalContext;
    private Button mViewCamcardButton;
    private ImageView mVoiceSearchButton;
    private TextView mW3Button;
    private View serchlayout;
    private DialogInterface.OnClickListener shareItemSelectListener;
    private MenuItem shareMenu;
    public OnTouchListener touchListener;

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            if (HwLog.HWFLOW) {
                HwLog.i("DefaultContactBrowseListFragment", "handleMessage,what=" + msg.what + ",ag1=" + msg.arg1 + ",arg2=" + msg.arg2 + ",obj=" + msg.obj);
            }
            switch (msg.what) {
                case 10:
                    if (DefaultContactBrowseListFragment.this.getActivity() != null) {
                        DialogFragment dialogFragment = (DialogFragment) DefaultContactBrowseListFragment.this.getFragmentManager().findFragmentByTag(ImportContactsInteraction.IMPORTCONTACTSINTERACTIONTAG);
                        if (dialogFragment != null) {
                            dialogFragment.dismiss();
                            break;
                        }
                    }
                    return;
                    break;
                case 257:
                    DefaultContactBrowseListFragment.this.shareContact();
                    break;
                case 258:
                    DefaultContactBrowseListFragment.this.bindQRCodeGenerat();
                    break;
                case 259:
                    DefaultContactBrowseListFragment.this.bindQRCodeGeneratFail();
                    break;
                case 260:
                    DefaultContactBrowseListFragment.this.getQRCodeBitmap(ProfileUtils.buildQRCodeBundle(DefaultContactBrowseListFragment.this.mContactData, DefaultContactBrowseListFragment.this.mEntriesObject.qrcodeDataInfo));
                    break;
                case 261:
                    DefaultContactBrowseListFragment.this.handleSplitScreenInitFragment(msg.arg1, msg.arg2);
                    break;
                case 262:
                    DefaultContactBrowseListFragment.this.showInitInfoForSplitSearchMode();
                    break;
                case 263:
                    DefaultContactBrowseListFragment.this.mIsChangeRight = false;
                    break;
                case 1001:
                    DefaultContactBrowseListFragment.this.mRemoteMessenger = (Messenger) msg.obj;
                    if (msg.arg1 == 1) {
                        DefaultContactBrowseListFragment.this.startEditorRelativeAnimation(true);
                        break;
                    }
                    break;
                case 1003:
                    if (DefaultContactBrowseListFragment.this.mMenuAnimator != null) {
                        DefaultContactBrowseListFragment.this.mMenuAnimator.prepareAnimateReverse();
                        break;
                    }
                    break;
                case 1004:
                    DefaultContactBrowseListFragment.this.startEditorRelativeAnimation(false);
                    break;
                case 1005:
                    if (msg.obj == null) {
                        DefaultContactBrowseListFragment.this.mRemoteMessenger = null;
                        break;
                    } else {
                        DefaultContactBrowseListFragment.this.mRemoteMessenger = (Messenger) msg.obj;
                        break;
                    }
                case 1006:
                    if (DefaultContactBrowseListFragment.this.mMenuAnimator != null) {
                        DefaultContactBrowseListFragment.this.mMenuAnimator.reset();
                        break;
                    }
                    break;
            }
        }
    }

    private static class SelectedUri {
        private long mSelectedContactDirectoryId;
        private long mSelectedContactId;
        private String mSelectedContactLookupKey;
        private Uri mSelectedContactUri;

        public SelectedUri(Uri uri) {
            this.mSelectedContactUri = uri;
            parseSelectedContactUri();
        }

        private void parseSelectedContactUri() {
            if (this.mSelectedContactUri != null) {
                long j;
                String directoryParam = this.mSelectedContactUri.getQueryParameter("directory");
                if (TextUtils.isEmpty(directoryParam)) {
                    j = 0;
                } else {
                    j = Long.parseLong(directoryParam);
                }
                this.mSelectedContactDirectoryId = j;
                if (this.mSelectedContactUri.toString().startsWith(Contacts.CONTENT_LOOKUP_URI.toString())) {
                    List<String> pathSegments = this.mSelectedContactUri.getPathSegments();
                    this.mSelectedContactLookupKey = Uri.encode((String) pathSegments.get(2));
                    if (pathSegments.size() == 4) {
                        this.mSelectedContactId = ContentUris.parseId(this.mSelectedContactUri);
                        return;
                    }
                    return;
                } else if (!this.mSelectedContactUri.toString().startsWith(Contacts.CONTENT_URI.toString()) || this.mSelectedContactUri.getPathSegments().size() < 2) {
                    this.mSelectedContactLookupKey = null;
                    this.mSelectedContactId = 0;
                    return;
                } else {
                    this.mSelectedContactLookupKey = null;
                    this.mSelectedContactId = ContentUris.parseId(this.mSelectedContactUri);
                    return;
                }
            }
            this.mSelectedContactDirectoryId = 0;
            this.mSelectedContactLookupKey = null;
            this.mSelectedContactId = 0;
        }

        private long getDirId() {
            return this.mSelectedContactDirectoryId;
        }

        private long getContactId() {
            return this.mSelectedContactId;
        }

        private String getLookupKey() {
            return this.mSelectedContactLookupKey;
        }
    }

    public DefaultContactBrowseListFragment() {
        this.mContactsCount = -1;
        this.mPrefs = null;
        this.mSplitSelectedContactUri = null;
        this.mSplitSelectedType = -1;
        this.mIsNeedShowSelect = false;
        this.mLastSearchKey = null;
        this.mHeaderViewHeight = 0;
        this.mSyncObject = new Object();
        this.IS_CHINA_AREN = EmuiFeatureManager.isChinaArea();
        this.mShouldVoiceSearchShow = false;
        this.mIsHotlineEnabled = false;
        this.mHeaderViews = new ArrayList();
        this.mFooterViews = new ArrayList();
        this.mFooterButtons = null;
        this.mBlankFooter = null;
        this.mMenuAnimator = null;
        this.mIsChangeRight = false;
        this.mDisplayRate = 0.4f;
        this.mSearchListEmptyViewStub = null;
        this.mSyncBirthdayToCalendarViewStub = null;
        this.mSyncBirthdayToCalendarView = null;
        this.isInflate = true;
        this.mTellUserBirthdayToCalendarViewStub = null;
        this.mTellUserBirthdayToCalendarView = null;
        this.isTellUserStubInflate = true;
        this.mOnlineSearchListener = new OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.footer_exchange:
                        DefaultContactBrowseListFragment.this.startExchangeSearch();
                        StatisticalHelper.report(4021);
                        return;
                    case R.id.search_w3_contacts:
                        StatisticalHelper.report(1132);
                        DirectorySearchManager.startW3Activity(DefaultContactBrowseListFragment.this.getActivity(), DefaultContactBrowseListFragment.this.getQueryString());
                        return;
                    case R.id.search_exchange_contacts:
                        if (DefaultContactBrowseListFragment.this.mSearchEmptyView != null) {
                            DefaultContactBrowseListFragment.this.mSearchEmptyView.setVisibility(8);
                        }
                        DefaultContactBrowseListFragment.this.mSearchHeaderView.setVisibility(0);
                        DefaultContactBrowseListFragment.this.startExchangeSearch();
                        StatisticalHelper.report(4021);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mLogStatusChangeListener = new LogStatusChangedListener() {
            public void onExchangeStatusChanged(boolean logon) {
                DefaultContactBrowseListFragment.this.initSearchListEmpty();
                if (DefaultContactBrowseListFragment.this.mSearchEmptyView.getVisibility() == 0) {
                    DefaultContactBrowseListFragment.this.mDirectoryManager.updateExchangeButton(DefaultContactBrowseListFragment.this.mExchangeButton);
                    DefaultContactBrowseListFragment.this.mDirectoryManager.updateW3Button(DefaultContactBrowseListFragment.this.mW3Button);
                } else if (DefaultContactBrowseListFragment.this.mFooterButtons == null && DefaultContactBrowseListFragment.this.getListView() != null && DefaultContactBrowseListFragment.this.isSearchMode()) {
                    DefaultContactBrowseListFragment.this.initSearchModeFooter();
                } else {
                    DefaultContactBrowseListFragment.this.mDirectoryManager.updateExchangeButton(DefaultContactBrowseListFragment.this.mExchangeFooter);
                }
            }
        };
        this.mHeaderView = new SparseArray();
        this.mContextMenuAdapter = new ContextMenuAdapter() {
            private boolean mIsSimContactAndReadOnly = false;
            private String mSimAccountType;

            private void onItemClickSplitScreen(int position) {
                if (CommonUtilMethods.calcIfNeedSplitScreen() && DefaultContactBrowseListFragment.this.isTwoColumnSplit()) {
                    DefaultContactBrowseListFragment.this.onItemClick(position, 0);
                }
            }

            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                int position = ((AdapterContextMenuInfo) menuInfo).position - DefaultContactBrowseListFragment.this.getListView().getHeaderViewsCount();
                StatisticalHelper.report(1125);
                if (position >= (!DefaultContactBrowseListFragment.this.hasProfileAndIsVisible() ? 0 : 1) && ((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactUri(position) != null && ((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactDirectoryId(position) == 0) {
                    final int i;
                    long directoryId;
                    final int i2;
                    onItemClickSplitScreen(position);
                    String displayName = ((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactDisplayName(position);
                    menu.setHeaderTitle(displayName);
                    ContentResolver resolver = DefaultContactBrowseListFragment.this.getContext().getContentResolver();
                    boolean isPhoneNumberPresent = false;
                    boolean isEmailPresent = false;
                    List<String> formattedNumbers = Lists.newArrayList();
                    List<String> normalizedNumbers = Lists.newArrayList();
                    String defaultCountryIso = GeoUtil.getCurrentCountryIso(DefaultContactBrowseListFragment.this.getContext());
                    StringBuilder selection = new StringBuilder();
                    selection.append("((").append("contact_id").append("='").append(((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactId(position)).append("') AND (").append("mimetype").append("='").append("vnd.android.cursor.item/phone_v2").append("' OR ").append("mimetype").append("='").append("vnd.android.cursor.item/email_v2").append("'))");
                    Cursor cursor = resolver.query(Data.CONTENT_URI, new String[]{"data1", "mimetype", "data4"}, selection.toString(), null, null);
                    if (cursor != null) {
                        try {
                            if (cursor.moveToFirst()) {
                                do {
                                    String s0;
                                    String s2;
                                    if (isPhoneNumberPresent || !cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/phone_v2")) {
                                        if (!isEmailPresent) {
                                            if (cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/email_v2")) {
                                                isEmailPresent = true;
                                            }
                                        }
                                        if (cursor.getString(1).equals("vnd.android.cursor.item/phone_v2")) {
                                            s0 = PhoneNumberFormatter.parsePhoneNumber(cursor.getString(0));
                                            s2 = PhoneNumberFormatter.parsePhoneNumber(cursor.getString(2));
                                            formattedNumbers.add(PhoneNumberUtils.formatNumber(s0, s2, defaultCountryIso));
                                            normalizedNumbers.add(s2);
                                        }
                                    } else {
                                        s0 = PhoneNumberFormatter.parsePhoneNumber(cursor.getString(0));
                                        s2 = PhoneNumberFormatter.parsePhoneNumber(cursor.getString(2));
                                        formattedNumbers.add(PhoneNumberUtils.formatNumber(s0, s2, defaultCountryIso));
                                        normalizedNumbers.add(s2);
                                        isPhoneNumberPresent = true;
                                    }
                                } while (cursor.moveToNext());
                            }
                            cursor.close();
                        } catch (Throwable th) {
                            cursor.close();
                        }
                    }
                    Uri uri = ((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactUri(position);
                    this.mIsSimContactAndReadOnly = false;
                    if (EmuiFeatureManager.isPreLoadingSimContactsEnabled() && !SimFactoryManager.isBothSimLoadingFinished()) {
                        this.mSimAccountType = SimFactoryManager.getContactAccountType(uri);
                        if (CommonUtilMethods.isSimAccount(this.mSimAccountType)) {
                            this.mIsSimContactAndReadOnly = !SimFactoryManager.isSimLoadingFinished(this.mSimAccountType);
                        }
                    }
                    boolean readOnly = false;
                    if (DefaultContactBrowseListFragment.this.mCust != null && DefaultContactBrowseListFragment.this.mCust.supportReadOnly()) {
                        readOnly = DefaultContactBrowseListFragment.this.mCust.isReadOnlyContact(((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactId(position), DefaultContactBrowseListFragment.this.getContext());
                    }
                    if (!readOnly) {
                        i = position;
                        menu.add(0, 1, 0, R.string.menu_editContact).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                StatisticalHelper.report(2009);
                                ExceptionCapture.reportScene(37);
                                if (AnonymousClass3.this.mIsSimContactAndReadOnly) {
                                    DefaultContactBrowseListFragment.this.showSimNotReadyToast(AnonymousClass3.this.mSimAccountType);
                                } else {
                                    DefaultContactBrowseListFragment.this.editContact(((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactUri(i));
                                }
                                return true;
                            }
                        });
                        i = position;
                        menu.add(0, 0, 0, R.string.menu_deleteContact).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                if (AnonymousClass3.this.mIsSimContactAndReadOnly) {
                                    DefaultContactBrowseListFragment.this.showSimNotReadyToast(AnonymousClass3.this.mSimAccountType);
                                } else {
                                    DefaultContactBrowseListFragment.this.deleteContact(((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactUri(i));
                                }
                                ExceptionCapture.reportScene(38);
                                StatisticalHelper.report(1147);
                                return true;
                            }
                        });
                    }
                    String directoryParameter = ((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactUri(position).getQueryParameter("directory");
                    if (directoryParameter == null) {
                        directoryId = 0;
                    } else {
                        directoryId = Long.parseLong(directoryParameter);
                    }
                    boolean isDirectoryEntry = (directoryId == -1 || directoryId == 0) ? false : directoryId != 1;
                    boolean isProfile = ((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getIsProfileContact(position);
                    boolean isYellowPage = YellowPageContactUtil.isYellowPageUri(((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactUri(position));
                    if (!(isDirectoryEntry || isProfile || isYellowPage || formattedNumbers.size() == 0)) {
                        boolean isStarred = ((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getIsStaredContact(position);
                        final boolean z = isStarred;
                        i2 = position;
                        menu.add(0, 11, 0, isStarred ? R.string.contacts_ContextMenu_removeStar : R.string.contacts_contextMenu_addStar).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                int i;
                                boolean z;
                                if (z) {
                                    i = 4023;
                                } else {
                                    i = 4022;
                                }
                                StatisticalHelper.report(i);
                                Context context = DefaultContactBrowseListFragment.this.getContext();
                                Uri contactUri = ((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactUri(i2);
                                if (z) {
                                    z = false;
                                } else {
                                    z = true;
                                }
                                DefaultContactBrowseListFragment.this.getContext().startService(ContactSaveService.createSetStarredIntent(context, contactUri, z));
                                return true;
                            }
                        });
                    }
                    if (EmuiFeatureManager.isSuperSaverMode()) {
                        menu.add(0, 8, 0, R.string.contact_menu_share_contacts).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                return true;
                            }
                        }).setEnabled(false);
                    } else {
                        i = position;
                        menu.add(0, 8, 0, R.string.contact_menu_share_contacts).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                if (AnonymousClass3.this.mIsSimContactAndReadOnly) {
                                    DefaultContactBrowseListFragment.this.showSimNotReadyToast(AnonymousClass3.this.mSimAccountType);
                                } else {
                                    DefaultContactBrowseListFragment.this.performShareContacts(item, i);
                                }
                                StatisticalHelper.report(1148);
                                ExceptionCapture.reportScene(39);
                                return true;
                            }
                        });
                    }
                    DefaultContactBrowseListFragment.this.shareMenu = menu.findItem(8);
                    DefaultContactBrowseListFragment.this.preLoadContactForShare(position);
                    if (isPhoneNumberPresent && EmuiFeatureManager.isBlackListFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner()) {
                        int blacklistMenuString;
                        final IHarassmentInterceptionService mService = Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService"));
                        final ArrayList lPhoneNumberList = (ArrayList) formattedNumbers;
                        if (BlacklistCommonUtils.checkPhoneNumberFromBlockItem(mService, lPhoneNumberList)) {
                            blacklistMenuString = R.string.contact_menu_remove_from_blacklist;
                        } else {
                            blacklistMenuString = R.string.contact_menu_add_to_blacklist;
                        }
                        final int i3 = position;
                        final String str = displayName;
                        menu.add(0, 9, 0, blacklistMenuString).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                ExceptionCapture.reportScene(40);
                                DefaultContactBrowseListFragment.this.performAddToBlackList(item, i3, str, lPhoneNumberList, mService);
                                return true;
                            }
                        });
                    }
                    if (!CommonUtilMethods.isSimplifiedModeEnabled()) {
                        if (EmuiFeatureManager.isSuperSaverMode()) {
                            menu.add(0, 10, 0, R.string.menu_create_contact_shortcut).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    return true;
                                }
                            }).setEnabled(false);
                        } else {
                            final List<String> list = formattedNumbers;
                            i2 = position;
                            final List<String> list2 = normalizedNumbers;
                            menu.add(0, 10, 0, R.string.menu_create_contact_shortcut).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    ExceptionCapture.reportScene(41);
                                    StatisticalHelper.report(4025);
                                    if (list.size() == 0 || CommonUtilMethods.isWifiOnlyVersion()) {
                                        ContactDetailHelper.createLauncherShortcutWithContact(DefaultContactBrowseListFragment.this.getActivity(), ((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactUri(i2));
                                        return true;
                                    }
                                    AlertDialogFragmet.show(DefaultContactBrowseListFragment.this.getFragmentManager(), (int) R.string.shortcutActivityTitle, list, list2, ((ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter()).getContactUri(i2), null);
                                    return true;
                                }
                            });
                        }
                    }
                    if (DefaultContactBrowseListFragment.this.getActivity() instanceof PeopleActivity) {
                        ((PeopleActivity) DefaultContactBrowseListFragment.this.getActivity()).setContextMenuStatus(false);
                    }
                }
            }

            public boolean onContextItemSelected(MenuItem item) {
                return false;
            }
        };
        this.mDirectoryLoadedState = new LongSparseArray();
        this.touchListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                LocalBroadcastManager.getInstance(DefaultContactBrowseListFragment.this.getActivity()).sendBroadcast(new Intent("android.intent.action.FINISH_MULTI_SELECTED"));
                return true;
            }
        };
        this.emptyContactScreenOnClickListener = new OnClickListener() {
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_sign_into_account:
                        ExceptionCapture.reportScene(49);
                        Intent intent = new Intent("android.settings.SYNC_SETTINGS_EMUI");
                        intent.putExtra("authorities", new String[]{"com.android.contacts"});
                        intent.setFlags(524288);
                        try {
                            DefaultContactBrowseListFragment.this.startActivity(intent);
                            return;
                        } catch (ActivityNotFoundException e) {
                            HwLog.e("DefaultContactBrowseListFragment", "Account settings Activity not found");
                            return;
                        }
                    case R.id.btn_import_contacts:
                        new ImportContactsInteraction(DefaultContactBrowseListFragment.this.getActivity()).startLoadImportOptions(DefaultContactBrowseListFragment.this);
                        ExceptionCapture.reportScene(50);
                        return;
                    case R.id.btn_create_new_contact:
                        DefaultContactBrowseListFragment.this.launchAddNewContact();
                        StatisticalHelper.sendReport((int) AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, "0");
                        ExceptionCapture.reportScene(51);
                        return;
                    case R.id.btn_view_camcard:
                        DefaultContactBrowseListFragment.this.startCardScan();
                        StatisticalHelper.report(AMapException.CODE_AMAP_SERVICE_ILLEGAL_REQUEST);
                        return;
                    default:
                        return;
                }
            }
        };
        this.isOnSaveInstance = false;
        this.mLoader = null;
        this.mShareUri = null;
        this.mContactLoadedListener = new LoaderCallbacks<Contact>() {
            public Loader<Contact> onCreateLoader(int id, Bundle args) {
                DefaultContactBrowseListFragment.this.mLoader = new ContactLoader(DefaultContactBrowseListFragment.this.getApplicationContext(), (Uri) args.getParcelable("contactUri"), false, false, true, true);
                DefaultContactBrowseListFragment.this.mLoader.setContactLoadedListener(null);
                return DefaultContactBrowseListFragment.this.mLoader;
            }

            public void onLoadFinished(Loader<Contact> loader, Contact data) {
                if (!data.isLoaded()) {
                    return;
                }
                if (DefaultContactBrowseListFragment.this.mContactData == null || data.getId() != DefaultContactBrowseListFragment.this.mContactData.getId()) {
                    DefaultContactBrowseListFragment.this.mContactData = data;
                    DefaultContactBrowseListFragment.this.mEntriesObject = ProfileUtils.buildContactData(data, DefaultContactBrowseListFragment.this.getApplicationContext());
                    DefaultContactBrowseListFragment.this.dismissDialogHandler.sendEmptyMessage(260);
                }
            }

            public void onLoaderReset(Loader<Contact> loader) {
            }
        };
        this.shareItemSelectListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        DefaultContactBrowseListFragment.this.shareBusinessCard();
                        return;
                    case 1:
                        DefaultContactBrowseListFragment.this.shareVcard();
                        return;
                    case 2:
                        DefaultContactBrowseListFragment.this.shareTextCard();
                        return;
                    default:
                        return;
                }
            }
        };
        setPhotoLoaderEnabled(true);
        setSectionHeaderDisplayEnabled(true);
        setVisibleScrollbarEnabled(true);
        configFamilynameOverLayDisplayEnabled(true);
    }

    public DefaultContactBrowseListFragment(GenericHandler aGenHdlr, boolean aDelayIfRequired) {
        this();
        this.mDelayIfRequired = aDelayIfRequired;
    }

    public void onStart() {
        super.onStart();
        if (!isReplacable()) {
            if (this.mContactsSearchView != null) {
                this.mContactsSearchView.setText(getQueryString());
            }
            if (this.mMenuAnimator != null) {
                this.mMenuAnimator.reset();
            }
            if (!isSearchMode()) {
                loadDirectories();
            }
        }
    }

    public void onResume() {
        this.isOnSaveInstance = false;
        super.onResume();
        showOrHideMaskView(false);
        if (!isReplacable()) {
            int filterType;
            View noContactsView = getView().findViewById(R.id.no_contacts_screen_layout);
            boolean mIsChangeMode = EmuiFeatureManager.isYellowPageEnable();
            if (mIsChangeMode != this.mIsHotlineEnabled) {
                this.mIsHotlineEnabled = mIsChangeMode;
                if (noContactsView == null || noContactsView.getVisibility() == 8) {
                    reloadHeaderView();
                }
            }
            if (noContactsView != null && noContactsView.getVisibility() == 0) {
                filterType = -2;
                if (getFilter() != null) {
                    filterType = getFilter().filterType;
                }
                if (-2 == filterType && isShowSimContacts()) {
                    setButtonStateInSuperSaverMode();
                    showEmptyFragmentIfSplit(true);
                }
                if (this.mViewCamcardButton != null) {
                    if (filterType == -2 && EmuiFeatureManager.isCamcardEnabled() && !CommonUtilMethods.isSimplifiedModeEnabled() && isShowSimContacts()) {
                        this.mViewCamcardButton.setVisibility(0);
                    } else {
                        this.mViewCamcardButton.setVisibility(8);
                    }
                }
            }
            if (this.mSplitActionBarView != null) {
                this.mSplitActionBarView.setVisibility(2, EmuiFeatureManager.isSuperSaverMode() ? false : EmuiFeatureManager.isCamcardEnabled());
                this.mSplitActionBarView.setEnable(1, true);
            }
            if (this.mCreateNewContactsButton != null) {
                this.mCreateNewContactsButton.setClickable(true);
            }
            if (this.addContactMenu != null) {
                this.addContactMenu.setEnabled(true);
            }
            this.mInnerSearchLayout.setBackgroundResource(R.drawable.contact_textfield_default_holo_light);
            this.mContactsSearchView.setCursorVisible(false);
            this.mShouldVoiceSearchShow = this.IS_CHINA_AREN ? isVoiceRecognitionUseable() : false;
            if (this.mShouldVoiceSearchShow) {
                Editable text = this.mContactsSearchView.getText();
                if (text == null || TextUtils.isEmpty(text.toString().trim())) {
                    this.mVoiceSearchButton.setVisibility(0);
                }
            } else {
                this.mVoiceSearchButton.setVisibility(8);
            }
            Activity activity = getActivity();
            if (activity instanceof PeopleActivity) {
                PeopleActivity peopleActivity = (PeopleActivity) activity;
                peopleActivity.getContactListHelper().sendEmptyMessage(1);
                peopleActivity.getContactListHelper().startInitHeader(getListView());
            }
            if (EmuiFeatureManager.isUseCustAnimation()) {
                if (this.mSplitActionBarView != null) {
                    this.mSplitActionBarView.setEnable(1, true);
                }
                enableEmptyButtons(true);
            }
            if (this.mIsSimpleDisplayMode != ContactDisplayUtils.isSimpleDisplayMode()) {
                this.mIsSimpleDisplayMode = ContactDisplayUtils.isSimpleDisplayMode();
                refreshHeaderViews();
            }
            if (CommonUtilMethods.calcIfNeedSplitScreen() && this.mDefaultFrgContainer != null) {
                if (this.mIsFromNewIntent) {
                    filterType = -2;
                    if (getFilter() != null) {
                        filterType = getFilter().filterType;
                    }
                    if (this.mContactsCount == 0 && filterType == -2 && !isShowSimContacts()) {
                        filterType = -17;
                    }
                    generateAndSendMessageDelayed(261, filterType, this.mContactsCount, 200);
                    this.mIsFromNewIntent = false;
                }
                this.mDefaultFrgContainer.refreshFragmentLayout();
                showOrHideActionbar();
            }
            if (this.mShareUri != null) {
                loadContactForShare(this.mShareUri);
            }
            if (noContactsView == null || noContactsView.getVisibility() == 8) {
                showSyncBirthdayToCalandarView();
            }
        }
    }

    public void generateAndSendMessageDelayed(int msgWhat, int msgArg1, int msgArg2, long delayMills) {
        Message msg = Message.obtain();
        msg.what = msgWhat;
        msg.arg1 = msgArg1;
        msg.arg2 = msgArg2;
        this.dismissDialogHandler.sendMessageDelayed(msg, delayMills);
    }

    public void reloadHeaderView() {
        if (HwLog.HWDBG) {
            HwLog.d("DefaultContactBrowseListFragment", "reloadHeaderView");
        }
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        ListView listView = getListView();
        for (View view : this.mHeaderViews) {
            listView.removeHeaderView(view);
        }
        addHeaderViews(inflater);
    }

    public void setQueryText(String s) {
        if (!(isReplacable() || this.mContactsSearchView == null)) {
            this.mContactsSearchView.setText(s);
            this.mContactsSearchView.setCursorVisible(true);
            this.mIsQueryText = true;
        }
    }

    public void initRightContainer(Fragment fragment) {
        this.mIsChangeRight = false;
        if (this.mDefaultFrgContainer != null && fragment != null && !this.isOnSaveInstance) {
            this.mDefaultFrgContainer.initRightContainer(fragment);
            if (isOneColumnSplit()) {
                this.mDefaultFrgContainer.refreshFragmentLayout();
            }
        }
    }

    public void openRightContainer(Fragment fragment) {
        this.mIsChangeRight = false;
        if (this.mDefaultFrgContainer != null && fragment != null && !this.isOnSaveInstance) {
            if (isOneColumnSplit()) {
                showOrHideActionbar(false);
            }
            this.mDefaultFrgContainer.openRightClearStack(fragment);
        }
    }

    public void changeRightContainer(Fragment now_fragment, Fragment next_fragment) {
        if (!this.mIsChangeRight) {
            this.mIsChangeRight = true;
            this.dismissDialogHandler.sendEmptyMessageDelayed(263, 500);
            if (!(this.mDefaultFrgContainer == null || next_fragment == null)) {
                this.mDefaultFrgContainer.changeRightAddToStack(next_fragment, now_fragment);
            }
        }
    }

    public boolean isBackPressed() {
        if (this.mDefaultFrgContainer != null) {
            return this.mDefaultFrgContainer.isBackPressed();
        }
        return true;
    }

    public HwFragmentContainer getFragmentContainer() {
        return this.mDefaultFrgContainer;
    }

    public void showOrHideActivityActionbar(boolean isShow, boolean isForce) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && this.mDefaultFrgContainer != null) {
            ActionBar actionbar = getActivity().getActionBar();
            if (actionbar != null) {
                boolean isNeedShow;
                if (isForce) {
                    isNeedShow = isShow;
                } else if (isTwoColumnSplit()) {
                    isNeedShow = true;
                } else {
                    isNeedShow = isInMainPage();
                }
                if (isNeedShow) {
                    CommonUtilMethods.disableActionBarShowHideAnimation(actionbar);
                    if (this.mIsNeedShowAnimate) {
                        ContactSplitUtils.startActionbarShowAnimate(actionbar, getActivity().findViewById(16909290));
                    } else {
                        actionbar.show();
                    }
                } else {
                    CommonUtilMethods.disableActionBarShowHideAnimation(actionbar);
                    actionbar.hide();
                }
                this.mIsNeedShowAnimate = false;
            }
        }
    }

    public void setNeedShowActionbarAnimate(boolean show) {
        this.mIsNeedShowAnimate = show;
    }

    public void showOrHideActionbar() {
        Activity act = getActivity();
        if (act instanceof PeopleActivity) {
            ((PeopleActivity) act).showOrHideActionbar();
        } else {
            showOrHideActivityActionbar(true, false);
        }
    }

    private void showOrHideActionbar(boolean isShow) {
        Activity act = getActivity();
        if (act instanceof PeopleActivity) {
            ((PeopleActivity) act).showOrHideActionbar(isShow);
        } else {
            showOrHideActivityActionbar(isShow, true);
        }
    }

    private boolean isOneColumnSplit() {
        boolean z = false;
        Activity activity = getActivity();
        if (activity == null || this.mDefaultFrgContainer == null) {
            return false;
        }
        if (!ContactSplitUtils.isSpiltTwoColumn(activity, activity.isInMultiWindowMode())) {
            z = true;
        }
        return z;
    }

    private boolean isTwoColumnSplit() {
        Activity activity = getActivity();
        if (activity == null || this.mDefaultFrgContainer == null) {
            return false;
        }
        return ContactSplitUtils.isSpiltTwoColumn(activity, activity.isInMultiWindowMode());
    }

    protected void onClickFamilylistItme(int pos) {
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            if (isTwoColumnSplit()) {
                onItemClick(pos, 0);
            } else {
                this.mSplitSelectedContactUri = ((ContactListAdapter) getAdapter()).getContactUri(pos);
                this.mSplitSelectedType = 0;
                setHeadViewFouce(this.mSplitSelectedType);
            }
        }
    }

    protected void requestSelectionToScreen(int selectedPosition, boolean isSmooth) {
        AutoScrollListView listView = (AutoScrollListView) getListView();
        listView.requestPositionToScreen(listView.getHeaderViewsCount() + selectedPosition, isSmooth);
    }

    public void setFromNewIntent(boolean isNewIntent) {
        this.mIsFromNewIntent = isNewIntent;
    }

    public CursorLoader createCursorLoader(int id) {
        ProfileAndContactsLoader loader;
        if (HwLog.HWDBG) {
            HwLog.d("DefaultContactBrowseListFragment", "createCursorLoader ProfileAndContactsLoader");
        }
        if (!CommonUtilMethods.getIsLiteFeatureProducts() || this.mApp == null) {
            loader = new ProfileAndContactsLoader(getContext());
        } else {
            loader = new ProfileAndContactsLoader(getContext(), this.mApp);
        }
        loader.setLoadStar(false);
        return loader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.isClosed()) {
            if (data == null || data.getCount() == 0) {
                PLog.d(7, "Contact list empty.");
            }
            if (HwLog.HWDBG) {
                HwLog.d("DefaultContactBrowseListFragment", "onLoadFinished ProfileAndContactsLoader");
            }
            Activity activity = getActivity();
            if (activity != null) {
                ((ContactsApplication) activity.getApplication()).setLaunchProgress(4);
            }
            super.onLoadFinished(loader, data);
            if (!isSearchMode() && this.mIsDataLoadedForMainList && (loader == null || loader.getId() != -1)) {
                setSearchHint(data != null ? data.getCount() : 0);
            }
            if (CommonUtilMethods.getIsLiteFeatureProducts() && this.mApp != null && this.mApp.getIsFirstStartContacts()) {
                this.mApp.setFirstStartContactsStatus(false);
                forceStartLoading();
            }
            if (PLog.DEBUG) {
                PLog.d(19, "Contact onLoadFinsh.");
            }
            return;
        }
        HwLog.e("DefaultContactBrowseListFragment", "onLoadFinished cursor is Closed");
    }

    private void setSearchHint(int count) {
        ContactListFilter filter = getFilter();
        if (filter != null) {
            String hint;
            if (count <= 0) {
                hint = getResources().getString(R.string.contact_search_hint_no_contact);
            } else if (filter.filterType == -2) {
                hint = getResources().getQuantityString(R.plurals.contact_search_hint_for_all, count, new Object[]{Integer.valueOf(count)});
            } else if (filter.filterType == -3) {
                hint = getResources().getQuantityString(R.plurals.contact_search_hint_for_no_account, count, new Object[]{Integer.valueOf(count)});
            } else {
                if (AccountFilterUtil.getFilterStringToDisplay(getContext(), getFilter(), false) != null) {
                    hint = getResources().getQuantityString(R.plurals.contact_search_hint_for_account, count, new Object[]{Integer.valueOf(count), filterString});
                } else {
                    hint = getResources().getQuantityString(R.plurals.contact_search_hint_for_no_account, count, new Object[]{Integer.valueOf(count)});
                }
            }
            this.mContactsSearchView.setHint(CommonUtilMethods.getSearchViewSpannableHint(getContext(), hint, this.mContactsSearchView.getTextSize()).toString().trim());
            if (TextUtils.isEmpty(this.mContactsSearchView.getText().toString())) {
                this.mContactsSearchView.setText(null);
            }
        }
    }

    protected void onItemClick(int position, long id) {
        PLog.d(9, "DefaultContactBrowserListFragment onItemClick");
        Uri lookUri = ((ContactListAdapter) getAdapter()).getContactUri(position);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            this.mSplitSelectedContactUri = lookUri;
            this.mSplitSelectedType = 0;
            setHeadViewFouce(this.mSplitSelectedType);
            if (!(isOneColumnSplit() || lookUri == null || !lookUri.equals(getSelectedContactUri()))) {
                if (this.mDefaultFrgContainer != null) {
                    this.mDefaultFrgContainer.setSelectedContainer(1);
                }
                return;
            }
        }
        viewContact(lookUri, ((ContactListAdapter) getAdapter()).isEnterpriseContact(position));
        StatisticalHelper.report(1151);
    }

    protected void onHeaderItemClick(int position, View v) {
        Intent obj = v.getTag();
        if (obj instanceof Intent) {
            Intent intent = obj;
            if ("com.huawei.yellowpage.intent.action.main".equals(intent.getAction())) {
                startYellowPageActivity(intent);
                StatisticalHelper.report(1204);
            } else if (!"com.huawei.android.intent.action.CAMCARD_CONTACT".equals(intent.getAction())) {
                if (intent.getComponent() != null) {
                    String className = intent.getComponent().getClassName();
                    if (FavoriteContactsActivity.class.getName().equals(className)) {
                        StatisticalHelper.report(1127);
                    } else if (GroupBrowserActivity.class.getName().equals(className)) {
                        StatisticalHelper.report(1191);
                        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                            switchRightGroupFragment(false, isOneColumnSplit(), true);
                            return;
                        }
                    } else if (ProfileSimpleCardActivity.class.getName().equals(className)) {
                        StatisticalHelper.report(1137);
                    }
                }
                startActivity(intent);
            } else if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                switchRightCCBrowseListFragment(false, isOneColumnSplit(), true);
            } else {
                CCUtils.startCCardActivity(getActivity());
            }
        }
    }

    protected ContactListAdapter createListAdapter() {
        DefaultContactListAdapter adapter = new DefaultContactListAdapter(getContext());
        adapter.setShowCompany(true);
        adapter.setFilter(ContactListFilterController.getInstance(getActivity()).getFilter());
        adapter.setSectionHeaderDisplayEnabled(isSectionHeaderDisplayEnabled());
        adapter.setDisplayPhotos(getResources().getBoolean(R.bool.config_browse_list_show_images));
        return adapter;
    }

    public float getDisplayRate(boolean bLandscape) {
        int i = 2;
        if (this.mDefaultFrgContainer == null) {
            return 0.0f;
        }
        float f;
        int appWidth = 2 == getResources().getConfiguration().orientation ? bLandscape ? getActivity().getWindow().getDecorView().getWidth() : getActivity().getWindow().getDecorView().getHeight() : bLandscape ? getActivity().getWindow().getDecorView().getHeight() : getActivity().getWindow().getDecorView().getWidth();
        HwFragmentContainer hwFragmentContainer = this.mDefaultFrgContainer;
        if (!bLandscape) {
            i = 1;
        }
        if (hwFragmentContainer.getColumnsNumber(i, appWidth) > 1) {
            f = this.mDisplayRate;
        } else {
            f = 0.0f;
        }
        return f;
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        View listView;
        View lView;
        PLog.d(0, "DefaultContactBrowserListFragement inflateView begin");
        if (this.mPrefs == null) {
            this.mPrefs = SharePreferenceUtil.getDefaultSp_de(getActivity());
        }
        if (getActivity() instanceof PeopleActivity) {
            listView = ((PeopleActivity) getActivity()).getContactListHelper().getAllListLayout();
        } else {
            listView = inflater.inflate(R.layout.contact_list_content, null);
        }
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            lView = inflater.inflate(R.layout.all_split_fragment, null);
            this.mDefaultFrgLayout = (HwFragmentLayout) lView.findViewById(R.id.split_layout_all);
            this.mDefaultFrgContainer = new HwFragmentContainer(getActivity(), this.mDefaultFrgLayout, getChildFragmentManager());
            this.mDefaultFrgContainer.setSelectContainerByTouch(true);
            this.mDefaultFrgContainer.setDisplayRate(0.5f);
            this.mDisplayRate = 0.5f;
            View view = this.mDefaultFrgContainer.getSplitLine();
            LayoutParams mSplitLineParams = (LayoutParams) view.getLayoutParams();
            mSplitLineParams.topMargin = ContactDpiAdapter.getActivityPaddingHeight(getActivity());
            mSplitLineParams.height = -1;
            mSplitLineParams.addRule(12);
            view.setLayoutParams(mSplitLineParams);
            if (isTwoColumnSplit()) {
                View bgview = this.mDefaultFrgContainer.getRightBlurLayer();
                FrameLayout.LayoutParams rightParams = (FrameLayout.LayoutParams) bgview.getLayoutParams();
                rightParams.topMargin = ContactDpiAdapter.getStatusBarHeight(getActivity());
                rightParams.height = -1;
                bgview.setLayoutParams(rightParams);
            }
            this.mDefaultFrgContainer.setCanMove(false);
            this.mDefaultFrgLayout.setFragmentLayoutCallback(new HwFragmentLayoutCallback() {
                public void setDisplayRate(float displayRate) {
                    DefaultContactBrowseListFragment.this.mDisplayRate = displayRate;
                }
            });
            if (((DummyStubFragment) getChildFragmentManager().findFragmentByTag(DummyStubFragment.class.getName())) == null) {
                this.mDefaultFrgContainer.openLeftClearStack(new DummyStubFragment());
            }
            this.mDefaultFrgContainer.setSplitMode(3);
            this.mDefaultFrgContainer.getLeftLayout().addView(listView);
            if (HwLog.HWFLOW) {
                HwLog.i("DefaultContactBrowseListFragment", "Default list init split frame");
            }
            if (this.mSavedState != null) {
                Fragment fragment = null;
                if (this.mSavedState.getBoolean(GroupBrowseListFragment.class.getName())) {
                    Fragment groupBrowse = new GroupBrowseListFragment();
                    openRightContainer(groupBrowse);
                    if (this.mSavedState.getBoolean(SmartGroupBrowseListFragment.class.getName())) {
                        fragment = new SmartGroupBrowseListFragment();
                        changeRightContainer(groupBrowse, fragment);
                    }
                    if (this.mSavedState.getBoolean(GroupDetailFragment.class.getName())) {
                        if (fragment == null) {
                            fragment = groupBrowse;
                        }
                        changeRightContainer(fragment, new GroupDetailFragment());
                    }
                }
            }
        } else {
            lView = listView;
        }
        ViewStub lViewStub = (ViewStub) lView.findViewById(R.id.pinnedHeaderList_stub);
        if (lViewStub != null) {
            lViewStub.setLayoutResource(CommonUtilMethods.getPinnedHeaderListViewResId(getContext()));
        }
        this.mSearchListEmptyViewStub = (ViewStub) lView.findViewById(R.id.search_list_empty);
        this.mSyncBirthdayToCalendarViewStub = (ViewStub) lView.findViewById(R.id.contact_list_sync_birthday_to_calendar_stub);
        this.mTellUserBirthdayToCalendarViewStub = (ViewStub) lView.findViewById(R.id.tell_user_sync_birthday_to_calendar_stub);
        PLog.d(0, "DefaultContactBrowserListFragement inflateView end");
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            if (isTwoColumnSplit()) {
                this.mIsNeedShowSelect = true;
            }
            setSelectionVisible(this.mIsNeedShowSelect);
            setHighLightVisible(this.mIsNeedShowSelect);
        }
        return lView;
    }

    private void initSearchListEmpty() {
        if (this.mSearchListEmptyViewStub != null) {
            this.mSearchEmptyView = this.mSearchListEmptyViewStub.inflate();
            this.mExchangeButton = (TextView) this.mSearchEmptyView.findViewById(R.id.search_exchange_contacts);
            this.mW3Button = (TextView) this.mSearchEmptyView.findViewById(R.id.search_w3_contacts);
            if (this.mSyncBirthdayToCalendarView == null || this.mSyncBirthdayToCalendarView.getVisibility() != 0) {
                setEmptyContactLocation(this.mSearchEmptyView.findViewById(R.id.online_search_empty_text));
            }
            updateButtonLayout(this.mExchangeButton);
            updateButtonLayout(this.mW3Button);
            this.mExchangeButton.setOnClickListener(this.mOnlineSearchListener);
            this.mW3Button.setOnClickListener(this.mOnlineSearchListener);
            this.mSearchListEmptyViewStub.setVisibility(8);
            this.mSearchListEmptyViewStub = null;
        }
    }

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (!isReplacable()) {
            boolean z;
            this.dismissDialogHandler = new MyHandler();
            this.mMessenger = new Messenger(this.dismissDialogHandler);
            Activity activity = getActivity();
            this.mApp = (ContactsApplication) getActivity().getApplication();
            CommonConstants.setSimplifiedModeEnabled(CommonUtilMethods.isSimpleModeOn());
            boolean isSimplifiedModeEnabled = CommonUtilMethods.isSimplifiedModeEnabled();
            if (activity != null) {
                int requestOrientationExpected;
                int requestOrientation = activity.getRequestedOrientation();
                if (isSimplifiedModeEnabled) {
                    requestOrientationExpected = 1;
                } else {
                    requestOrientationExpected = -1;
                }
                if (requestOrientation != requestOrientationExpected) {
                    activity.setRequestedOrientation(requestOrientationExpected);
                }
            }
            if (HLUtils.isShowHotNumberOnTop) {
                HLUtils.initPredefineContactLookupUri(getActivity());
            }
            if (EmuiFeatureManager.isProductCustFeatureEnable()) {
                this.mCust = (HwCustDefaultContactBrowseListFragment) HwCustUtils.createObj(HwCustDefaultContactBrowseListFragment.class, new Object[0]);
            }
            ContactReport.getInstance(getContext().getApplicationContext()).reportContactCount();
            if (CommonUtilMethods.calcIfNeedSplitScreen() && savedState == null && (activity instanceof PeopleActivity)) {
                savedState = CommonUtilMethods.getInstanceState();
                this.mSavedState = savedState;
            }
            if (savedState != null) {
                this.mCcardHandlr = CCardScanHandler.onRestoreInstance(savedState);
                this.mContactsCount = savedState.getInt("total_contacts_count", -1);
                this.mSplitSelectedContactUri = (Uri) savedState.getParcelable("key_select_contact_uri");
                this.mSplitSelectedType = savedState.getInt("key_select_type");
                this.mLastSearchKey = savedState.getString("key_last_search");
                this.mDisplayRate = savedState.getFloat("display_rate", 0.4f);
                this.mRemoteMessenger = (Messenger) savedState.getParcelable("remote_messenger");
                this.mHeaderViewHeight = savedState.getInt("serchlayout_visibility", 0);
                this.mShareUri = (Uri) savedState.getParcelable("share_contact_uri");
            }
            if (1 == getResources().getConfiguration().orientation) {
                z = true;
            } else {
                z = false;
            }
            this.isInPortrait = z;
            this.mIsHotlineEnabled = EmuiFeatureManager.isYellowPageEnable();
            this.mDirectoryManager.setListener(this.mLogStatusChangeListener);
            if (this.mRemoteMessenger != null && this.isInPortrait) {
                try {
                    this.mRemoteMessenger.send(Message.obtain(null, 1005, this.mMessenger));
                } catch (RemoteException e) {
                    HwLog.e("DefaultContactBrowseListFragment", "onCreate, send update messenger to remote failed");
                }
            }
            ContactsThreadPool.getInstance().execute(new Runnable() {
                public void run() {
                    AccountTypeManager.getInstance(DefaultContactBrowseListFragment.this.getContext().getApplicationContext());
                }
            });
            this.mTotalContext = getActivity();
        }
    }

    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        if (!isReplacable()) {
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                Activity act = getActivity();
                if ((act instanceof PeopleActivity) || (act instanceof VoiceSearchResultActivity)) {
                    ScreenUtils.adjustPaddingTop(act, (ViewGroup) getView().findViewById(R.id.contact_list_content), true);
                }
                initAndAdjustCustomMenu();
                Activity actRef = getActivity();
                PeopleActivity IPplActRef = null;
                if (actRef != null && (actRef instanceof PeopleActivity)) {
                    IPplActRef = (PeopleActivity) actRef;
                }
                if (IPplActRef != null) {
                    this.mSplitActionBarView.setOnCustomMenuListener(new OnCustomMenuListener() {
                        public boolean onCustomSplitMenuItemClick(int aMenuItem) {
                            switch (aMenuItem) {
                                case R.string.camcard_scan_card:
                                    DefaultContactBrowseListFragment.this.startCardScan();
                                    StatisticalHelper.report(AMapException.CODE_AMAP_SERVICE_ILLEGAL_REQUEST);
                                    break;
                                case R.string.contacts_toobar_new:
                                    DefaultContactBrowseListFragment.this.launchAddNewContact();
                                    StatisticalHelper.sendReport((int) AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, "0");
                                    ExceptionCapture.reportScene(6);
                                    break;
                                default:
                                    return false;
                            }
                            return true;
                        }

                        public boolean onCustomMenuItemClick(MenuItem aMenuItem) {
                            return DefaultContactBrowseListFragment.this.onOptionsItemSelected(aMenuItem);
                        }

                        public void onPrepareOptionsMenu(Menu aMenu) {
                            DefaultContactBrowseListFragment.this.hidePhoneBookOverLay();
                            DefaultContactBrowseListFragment.this.onPrepareOptionsMenu(aMenu);
                        }
                    });
                }
                this.mMenuAnimator = new ContactListAnimatorHelper();
                this.mMenuAnimator.init(this.mSplitActionBarView, getActivity());
                this.mMenuAnimator.setDetailAnimatorListener(new ContactAnimatorListener() {
                    public void onAnimationStart() {
                    }

                    public void onAnimationEnd() {
                        if (DefaultContactBrowseListFragment.this.mRemoteMessenger != null) {
                            try {
                                DefaultContactBrowseListFragment.this.mRemoteMessenger.send(Message.obtain(null, 1002));
                            } catch (RemoteException e) {
                                HwLog.e("DefaultContactBrowseListFragment", "send message for animation end to remote faild");
                            }
                        }
                        DefaultContactBrowseListFragment.this.mSplitActionBarView.setEnable(1, true);
                        DefaultContactBrowseListFragment.this.enableEmptyButtons(true);
                    }
                });
            }
            this.serchlayout = getView().findViewById(R.id.contact_list_serchlayout);
            this.mContactsSearchView = (EditText) this.serchlayout.findViewById(R.id.search_view);
            ContactsUtils.configureSearchViewInputType(this.mContactsSearchView);
            this.mContactsSearchView.setHint(CommonUtilMethods.getSearchViewSpannableHint(getContext(), getResources().getString(R.string.contact_hint_findContacts), this.mContactsSearchView.getTextSize()));
            this.mContactsSearchView.setCustomSelectionActionModeCallback(this);
            this.mContactsSearchView.setCursorVisible(false);
            this.mSearchLayout = (LinearLayout) this.serchlayout.findViewById(R.id.contactListsearchlayout);
            this.mInnerSearchLayout = this.serchlayout.findViewById(R.id.inner_contactListsearchlayout);
            this.mContactsSearchView.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    v.onTouchEvent(event);
                    DefaultContactBrowseListFragment.this.mInnerSearchLayout.setBackgroundResource(R.drawable.textfield_activated_holo_light);
                    DefaultContactBrowseListFragment.this.mContactsSearchView.setCursorVisible(true);
                    DefaultContactBrowseListFragment.this.hidePhoneBookOverLay();
                    if (event.getAction() == 1) {
                        StatisticalHelper.report(1145);
                    }
                    return true;
                }
            });
            this.mSuspentionScroller = (SuspentionScroller) getView().findViewById(R.id.suspention_scroller);
            if (this.mSuspentionScroller != null) {
                this.serchlayout.scrollTo(0, this.mHeaderViewHeight);
                ((ViewGroup) this.serchlayout.getParent()).removeView(this.serchlayout);
                this.mSearchLayout.setBackgroundResource(R.color.searchLayout_background_color);
                this.mSuspentionScroller.setSuspentionView(this.serchlayout);
                this.mSuspentionScroller.setListView(true);
                int h = MeasureSpec.makeMeasureSpec(0, 0);
                this.mSearchLayout.measure(h, h);
                this.mSuspentionScroller.setSuspentionViewHeight(this.mSearchLayout.getMeasuredHeight());
                this.mSuspentionScroller.init();
            }
            final ImageView searchClearButton = (ImageView) this.serchlayout.findViewById(R.id.clearSearchResult);
            searchClearButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    DefaultContactBrowseListFragment.this.mContactsSearchView.setText(null);
                    if (DefaultContactBrowseListFragment.this.getActivity() instanceof PeopleActivity) {
                        ((PeopleActivity) DefaultContactBrowseListFragment.this.getActivity()).cancleSearchMode();
                    }
                }
            });
            this.mContactsSearchView.addTextChangedListener(new TextWatcher() {
                Handler handler = new Handler();
                Runnable run = null;

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (PLog.DEBUG && s != null && s.length() > 0) {
                        PLog.d(18, "DefaultCBLF onTextChanged");
                    }
                    if (this.run != null) {
                        this.handler.removeCallbacks(this.run);
                    }
                    if (s == null || s.length() != 0) {
                        if (PLog.DEBUG) {
                            PLog.d(0, "DefaultCBLF onTextChangedDelayed start");
                        }
                        onTextChangedDelayed(s, start, before, count);
                        if (PLog.DEBUG) {
                            PLog.d(0, "DefaultCBLF onTextChangedDelayed end");
                            return;
                        }
                        return;
                    }
                    final CharSequence charSequence = s;
                    final int i = start;
                    final int i2 = before;
                    final int i3 = count;
                    this.run = new Runnable() {
                        public void run() {
                            if (DefaultContactBrowseListFragment.this.getActivity() != null) {
                                AnonymousClass14.this.onTextChangedDelayed(charSequence, i, i2, i3);
                            }
                        }
                    };
                    this.handler.postDelayed(this.run, 20);
                }

                public void onTextChangedDelayed(CharSequence s, int start, int before, int count) {
                    if (!DefaultContactBrowseListFragment.this.getIsVoiceSearchMode()) {
                        if (s == null || TextUtils.isEmpty(s.toString().trim())) {
                            ContactListAdapter adapter = (ContactListAdapter) DefaultContactBrowseListFragment.this.getAdapter();
                            CharSequence lastQueryStr = null;
                            if (adapter != null) {
                                lastQueryStr = adapter.getQueryString();
                            }
                            DefaultContactBrowseListFragment.this.setSearchModeInitialized(false);
                            DefaultContactBrowseListFragment.this.setSearchMode(false);
                            DefaultContactBrowseListFragment.this.setIncludeProfile(true);
                            DefaultContactBrowseListFragment.this.setQueryString(null, true);
                            searchClearButton.setVisibility(8);
                            if (DefaultContactBrowseListFragment.this.mShouldVoiceSearchShow) {
                                DefaultContactBrowseListFragment.this.mVoiceSearchButton.setVisibility(0);
                            }
                            if (adapter != null) {
                                adapter.configureDefaultPartition(false, false);
                                adapter.notifyChange();
                            }
                            DefaultContactBrowseListFragment.this.setProfileHeader();
                            if (!TextUtils.isEmpty(lastQueryStr)) {
                                DefaultContactBrowseListFragment.this.getListView().setSelection(0);
                            }
                        } else {
                            DefaultContactBrowseListFragment.this.setSearchModeInitialized(true);
                            DefaultContactBrowseListFragment.this.setSearchMode(true);
                            DefaultContactBrowseListFragment.this.setQueryString(s.toString(), true);
                            DefaultContactBrowseListFragment.this.mVoiceSearchButton.setVisibility(8);
                            searchClearButton.setVisibility(0);
                        }
                    }
                }
            });
            this.mVoiceSearchButton = (ImageView) this.serchlayout.findViewById(R.id.voiceSearch);
            this.mVoiceSearchButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    DefaultContactBrowseListFragment.this.startVoiceRecgnizing();
                    StatisticalHelper.reportVoiceSearchTimes(DefaultContactBrowseListFragment.this.getContext());
                }
            });
            this.mShouldVoiceSearchShow = this.IS_CHINA_AREN ? isVoiceRecognitionUseable() : false;
            if (this.mShouldVoiceSearchShow) {
                this.mVoiceSearchButton.setVisibility(0);
            }
            getListView().setOnCreateContextMenuListener(this.mContextMenuAdapter);
            this.mBlankFooter = CommonUtilMethods.addFootEmptyViewPortrait(getListView(), getContext());
            setHasOptionsMenu(false);
            this.mSearchHeaderView = getView().findViewById(R.id.searching_online_loading);
            checkHeaderViewVisibility();
            PLog.d(0, "DefaultContactBrowseListFragment onCreateView end");
        }
    }

    private void initAndAdjustCustomMenu() {
        SetButtonDetails leftInfo = new SetButtonDetails(R.drawable.ic_new_contact, R.string.contacts_toobar_new);
        SetButtonDetails moreMenuInfo = new SetButtonDetails(R.menu.contactslist_option, R.string.contacts_title_menu);
        SetButtonDetails scanMenu = new SetButtonDetails(R.drawable.contacts_scan, R.string.camcard_scan_card);
        this.mSplitActionBarView = (SplitActionBarView) getView().findViewById(R.id.menu_view);
        this.mSplitActionBarView.setVisibility(0);
        this.mSplitActionBarView.fillDetails(leftInfo, scanMenu, null, moreMenuInfo, true);
        if (!EmuiFeatureManager.isCamcardEnabled() || CommonUtilMethods.isSimpleModeOn()) {
            this.mSplitActionBarView.setVisibility(2, false);
        } else {
            this.mSplitActionBarView.setVisibility(2, true);
        }
    }

    private void checkHeaderViewVisibility() {
        if (this.mSearchHeaderView != null) {
            this.mSearchHeaderView.setVisibility(8);
        }
    }

    private void addHeaderViews(LayoutInflater inflater) {
        this.mHeaderViews.clear();
        if (this.mCust != null) {
            this.mCust.customizeListHeaderViews(this, inflater, this.mHeaderViews);
        }
        if (!CommonUtilMethods.isSimplifiedModeEnabled() && !isSearchMode()) {
            if (HLUtils.isShowHotNumberOnTop) {
                Uri lookupKeyUri = HLUtils.getHotNumberKey();
                if (lookupKeyUri != null) {
                    Intent hlIntent = new Intent("android.intent.action.VIEW", lookupKeyUri);
                    hlIntent.setClass(getContext(), ContactDetailActivity.class);
                    addHeaderView(inflater, R.drawable.ic_contacts_default_list, R.string.contact_list_hotline_number, hlIntent, 4);
                }
            }
            if (!EmuiFeatureManager.isShowFavoritesTab(getContext())) {
                addHeaderView(inflater, R.drawable.contact_list_favorites, R.string.contact_list_header_favorites, new Intent(getContext(), FavoriteContactsActivity.class), 0);
            } else if (EmuiFeatureManager.isYellowPageApkExist(getContext())) {
                Intent intent = new Intent("com.huawei.yellowpage.intent.action.main");
                intent.setComponent(new ComponentName("com.huawei.yellowpage", "com.huawei.yellowpage.YellowPageMainActivity"));
                addHeaderView(inflater, R.drawable.contact_list_yellowpage, R.string.online_yellow_pages, intent, 0);
            }
            addHeaderView(inflater, R.drawable.contact_list_groups, R.string.contacts_my_group, new Intent(getContext(), GroupBrowserActivity.class), 1);
            if (EmuiFeatureManager.isCamCardApkInstalled(getContext())) {
                addHeaderView(inflater, R.drawable.contact_list_business_cards, R.string.contact_list_business_cards, CCUtils.createCCActivityIntent(), 2);
            }
            addHeaderView(inflater, R.drawable.ic_contacts_default_list, R.string.user_profile_contacts_list_header, editProfile(getContext()), 3);
        }
    }

    private Intent editProfile(Context context) {
        ActivityNotFoundException e;
        Intent intent = new Intent();
        try {
            this.mProfileLookupUri = ProfileUtils.getProfileLookupUri(context);
            HwLog.i("DefaultContactBrowseListFragment", "mUserProfileExists = " + this.mUserProfileExists + " ; mProfileLookupUri = " + this.mProfileLookupUri);
            Intent intent2;
            if (!this.mUserProfileExists || this.mProfileLookupUri == null) {
                intent2 = new Intent("android.intent.action.INSERT", Contacts.CONTENT_URI);
                intent2.setClass(context, ContactEditorActivity.class);
                intent2.putExtra("newLocalProfile", true);
                intent2.putExtra("ViewDelayedLoadingSwitch", true);
                intent2.putExtra("finishActivityOnSaveCompleted", true);
                intent2.putExtra("from_list_profile_create", true);
                return intent2;
            }
            intent2 = new Intent("com.huawei.android.intent.action.PROFILE_CONTACT", this.mProfileLookupUri);
            try {
                intent2.setClass(context, ProfileSimpleCardActivity.class);
                intent2.setFlags(67108864);
                return intent2;
            } catch (ActivityNotFoundException e2) {
                e = e2;
                intent = intent2;
                HwLog.e("DefaultContactBrowseListFragment", "" + e.getMessage());
                return intent;
            }
        } catch (ActivityNotFoundException e3) {
            e = e3;
            HwLog.e("DefaultContactBrowseListFragment", "" + e.getMessage());
            return intent;
        }
    }

    private void addHeaderView(LayoutInflater inflater, int photoResId, int nameResId, Intent intent, int viewId) {
        ListView listView = getListView();
        if (listView != null) {
            View view = (View) this.mHeaderView.get(viewId);
            if (view == null || photoResId == R.drawable.ic_contacts_default_list) {
                ImageView toIcon;
                if (getActivity() instanceof PeopleActivity) {
                    view = ((PeopleActivity) getActivity()).getContactListHelper().getHeader();
                } else {
                    view = inflater.inflate(R.layout.contact_list_group_item_view, listView, false);
                }
                if (nameResId == R.string.user_profile_contacts_list_header) {
                    toIcon = (ImageView) view.findViewById(R.id.action);
                    if (toIcon != null) {
                        toIcon.setVisibility(4);
                    }
                }
                if (HLUtils.isShowHotNumberOnTop && nameResId == R.string.contact_list_hotline_number) {
                    toIcon = (ImageView) view.findViewById(R.id.action);
                    if (toIcon != null) {
                        toIcon.setVisibility(4);
                    }
                }
                this.mHeaderView.put(viewId, view);
                ImageView photoView = (ImageView) view.findViewById(R.id.photo);
                if (photoResId == R.drawable.ic_contacts_default_list) {
                    ProfileUtils.setMeListResource(photoView, getContext(), this.mProfileLookupUri);
                } else {
                    photoView.setImageResource(photoResId);
                }
                TextView nameView = (TextView) view.findViewById(R.id.name);
                MarginLayoutParams dividerLayoutParams = (MarginLayoutParams) view.findViewById(R.id.contact_list_group_item_view_divider).getLayoutParams();
                RelativeLayout headerListItem = (RelativeLayout) view.findViewById(R.id.contact_list_item_header);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) headerListItem.getLayoutParams();
                if (ContactDisplayUtils.isSimpleDisplayMode()) {
                    photoView.setVisibility(8);
                    nameView.setPaddingRelative((getResources().getDimensionPixelSize(R.dimen.default_contactListItemView_text_adjust_start_padding) + getResources().getDimensionPixelSize(R.dimen.contact_list_item_alphtextheight_size)) - getResources().getDimensionPixelSize(R.dimen.default_contact_list_item_content_padding_left_simple), 0, 0, 0);
                    dividerLayoutParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.contact_list_divider_margin_start_Simple));
                    headerListItem.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.list_item_single_line_height));
                } else {
                    dividerLayoutParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.contact_list_divider_margin_start));
                    headerListItem.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.list_item_double_line_height));
                }
                headerListItem.setLayoutParams(layoutParams);
                nameView.setText(nameResId);
                if (Constants.isEXTRA_HUGE()) {
                    nameView.setTextSize(1, 28.0f);
                }
                ViewUtil.setStateListIcon(getContext(), (ImageView) view.findViewById(R.id.action), false);
            }
            view.setTag(intent);
            listView.addHeaderView(view);
            this.mHeaderViews.add(view);
        }
    }

    private void startCardScan() {
        if (EmuiFeatureManager.isCamcardEnabled()) {
            this.mCcardHandlr = new CCardScanHandler();
            this.mCcardHandlr.recognizeCapture(getActivity(), this);
        }
    }

    private void startYellowPageActivity(Intent intent) {
        try {
            startActivity(intent);
            ExceptionCapture.reportScene(52);
        } catch (Exception e) {
            if (HwLog.HWFLOW) {
                HwLog.i("DefaultContactBrowseListFragment", "yellow page activity start failer");
            }
        }
    }

    public void deleteSearchfieldforSearchContacts() {
        if (isSearchMode() && this.mContactsSearchView != null && !TextUtils.isEmpty(this.mContactsSearchView.getText().toString())) {
            setSearchMode(false);
            this.mContactsSearchView.setText(null);
        }
    }

    public void onStop() {
        super.onStop();
    }

    public void onPause() {
        if (this.mContactsSearchView != null) {
            ((InputMethodManager) getContext().getSystemService("input_method")).hideSoftInputFromWindow(this.mContactsSearchView.getWindowToken(), 0);
        }
        showOrHideMaskView(true);
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mRemoteMessenger != null) {
            try {
                this.mRemoteMessenger.send(Message.obtain(null, 1005, null));
            } catch (RemoteException e) {
                HwLog.e("DefaultContactBrowseListFragment", "onDestroy,send message to remote handler failed");
            }
            this.mRemoteMessenger = null;
        }
        destroyLoaders();
    }

    private void destroyLoaders() {
        ContactListAdapter adapter = (ContactListAdapter) getAdapter();
        if (adapter != null) {
            int count = adapter.getPartitionCount();
            for (int i = 0; i < count; i++) {
                Loader loader = getLoaderManager().getLoader(i);
                if (!(loader == null || loader.isReset())) {
                    getLoaderManager().destroyLoader(i);
                }
                loader = getLoaderManager().getLoader(i + 1073741823);
                if (!(loader == null || loader.isReset())) {
                    getLoaderManager().destroyLoader(i + 1073741823);
                }
            }
        }
    }

    public void onDetach() {
        if (this.mQrCodeBitmap != null) {
            this.mQrCodeBitmap.recycle();
        }
        super.onDetach();
    }

    public void onBackPressed() {
        if (!isReplacable() && isSearchMode() && this.mContactsSearchView != null && !TextUtils.isEmpty(this.mContactsSearchView.getText().toString())) {
            this.mContactsSearchView.setText(null);
            setSearchMode(false);
        }
    }

    public void refreshSearchViewFocus() {
        if (!(isReplacable() || this.mInnerSearchLayout == null)) {
            this.mInnerSearchLayout.setBackgroundResource(R.drawable.contact_textfield_default_holo_light);
            this.mContactsSearchView.setCursorVisible(false);
        }
    }

    public void setQueryString(String queryString, boolean delaySelection) {
        int i = 0;
        super.setQueryString(queryString, delaySelection);
        if (this.mLastQueryLength == 0) {
            Activity activity = getActivity();
            if (activity instanceof PeopleActivity) {
                ContactListHelper helper = ((PeopleActivity) activity).getContactListHelper();
                helper.sendEmptyMessage(1);
                helper.inflateDirectoryHeader(getListView());
            }
        }
        if (queryString != null) {
            i = queryString.length();
        }
        this.mLastQueryLength = i;
    }

    public void setSearchMode(boolean flag) {
        if (!isReplacable()) {
            if (flag) {
                resetDirectoryLoadedState();
            }
            if (!(flag == isSearchMode() || flag)) {
                checkHeaderViewVisibility();
                if (this.mSearchEmptyView != null) {
                    this.mSearchEmptyView.setVisibility(8);
                }
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
                if (getListView() != null && getListView().getHeaderViewsCount() == 0) {
                    addHeaderViews(inflater);
                }
            }
            super.setSearchMode(flag);
        }
    }

    public void setFilter(ContactListFilter filter) {
        if (!isReplacable()) {
            super.setFilter(filter);
        }
    }

    public void setFilter(ContactListFilter filter, boolean restoreSelectedUri, boolean isChanged) {
        if (!isReplacable()) {
            super.setFilter(filter, restoreSelectedUri, isChanged);
        }
    }

    protected void showCount(int partitionIndex, Cursor data) {
        if (!isReplacable()) {
            if (isSearchMode() || data == null) {
                ContactListAdapter adapter = (ContactListAdapter) getAdapter();
                if (adapter != null) {
                    this.mSearchHeaderView.setVisibility(8);
                    if (adapter.areAllPartitionsEmpty()) {
                        initSearchListEmpty();
                        this.mSearchEmptyView.setVisibility(0);
                        this.mDirectoryManager.updateW3Button(this.mW3Button);
                        this.mExchangeButton.setVisibility(8);
                    } else {
                        if (this.mSearchEmptyView != null) {
                            this.mSearchEmptyView.setVisibility(8);
                        }
                        long directoryId = (long) partitionIndex;
                        Partition partition = adapter.getPartition(partitionIndex);
                        if (partition instanceof DirectoryPartition) {
                            directoryId = ((DirectoryPartition) partition).getDirectoryId();
                        }
                        if (data != null && data.getCount() == 0 && directoryId > 0 && !DirectoryCompat.isEnterpriseDirectoryId(directoryId)) {
                            if (this.mDirectoryToast == null) {
                                this.mDirectoryToast = Toast.makeText(getActivity(), getString(R.string.search_exchange_noMatching), 0);
                            } else {
                                this.mDirectoryToast.setText(getString(R.string.search_exchange_noMatching));
                            }
                            this.mDirectoryToast.show();
                        }
                    }
                } else {
                    return;
                }
            }
            int i;
            int count = data.getCount();
            if (hasProfileAndIsVisible()) {
                i = count - 1;
            } else {
                i = count;
            }
            this.mContactsCount = i;
            if (count != 0) {
                if (hasProfileAndIsVisible()) {
                    i = 1;
                } else {
                    i = 0;
                }
                ((ContactListAdapter) getAdapter()).setContactsCount(String.valueOf(count - i));
            }
        }
    }

    protected void setProfileHeader() {
        if (!isReplacable()) {
            this.mUserProfileExists = ((ContactListAdapter) getAdapter()).hasProfile();
            if (getActivity() != null) {
                getActivity().invalidateOptionsMenu();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!isReplacable()) {
            if (this.mCcardHandlr != null) {
                if (this.mCcardHandlr.handlePhotoActivityResult(requestCode, resultCode, data, getActivity(), this)) {
                    return;
                }
            }
            switch (requestCode) {
                case 1:
                    if (getActivity() == null) {
                        HwLog.e("DefaultContactBrowseListFragment", "getActivity() returns null during Fragment#onActivityResult()");
                        break;
                    } else {
                        AccountFilterUtil.handleAccountFilterResult(ContactListFilterController.getInstance(getActivity()), resultCode, data);
                        break;
                    }
                case 2:
                    AccountFilterUtil.handleAccountFilterResult(this.mContactListFilterController, resultCode, data);
                    break;
                case 3:
                    if (-1 == resultCode) {
                        ArrayList<String> matchStringList;
                        if (data != null) {
                            matchStringList = data.getStringArrayListExtra("android.speech.extra.RESULTS");
                        } else {
                            matchStringList = new ArrayList(0);
                        }
                        Intent intent = new Intent();
                        intent.setClass(getContext(), VoiceSearchResultActivity.class);
                        intent.putExtra("searchStrings", matchStringList);
                        intent.putExtra("contactListFilter", getFilter());
                        getContext().startActivity(intent);
                        break;
                    }
                    break;
                case 1091:
                    if (-1 == resultCode && data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            AccountWithDataSet account = (AccountWithDataSet) bundle.get("account");
                            Bundle extrasBundle = (Bundle) bundle.getParcelable("extra_args");
                            if (account != null) {
                                int lResId = -1;
                                if (extrasBundle != null) {
                                    lResId = extrasBundle.getInt("resId");
                                }
                                if (lResId == R.string.import_from_sdcard) {
                                    AccountSelectionUtil.doImport(getActivity(), R.string.import_from_sdcard, account);
                                    break;
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (isAdded()) {
            if (EmuiFeatureManager.isSuperSaverMode()) {
                menu.findItem(R.id.menu_import_export).setEnabled(false);
            }
            MenuItem helpMenu = menu.findItem(R.id.menu_help);
            this.addContactMenu = menu.findItem(R.id.menu_add_contact);
            if (isSearchModeInitialized()) {
                helpMenu.setVisible(false);
                makeMenuItemVisible(menu, R.id.menu_import_export, true);
            }
            HelpUtils.prepareHelpMenuItem(getContext(), helpMenu, (int) R.string.help_url_people_main);
            if (this.mCust != null && this.mCust.isRemoveShareContacts()) {
                this.mCust.makeMenuItemInVisible(menu);
            }
            super.onPrepareOptionsMenu(menu);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean z = false;
        if (this.mCust != null) {
            this.mCust.onOptionsItemSelectedForCust(item, getActivity());
        }
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_add_contact:
                StatisticalHelper.sendReport((int) AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, "0");
                launchAddNewContact();
                if (this.addContactMenu != null) {
                    this.addContactMenu.setEnabled(false);
                }
                return true;
            case R.id.menu_scan_card:
                if (getActivity() != null) {
                    startCardScan();
                    StatisticalHelper.report(AMapException.CODE_AMAP_SERVICE_ILLEGAL_REQUEST);
                    return true;
                }
                break;
            case R.id.overflow_menu_contacts:
                StatisticalHelper.report(1150);
                break;
            case R.id.menu_contacts_filter:
                if (this.mContactListFilterController == null) {
                    Activity actRef = getActivity();
                    if (actRef instanceof PeopleActivity) {
                        this.mContactListFilterController = ((PeopleActivity) actRef).getFilterController();
                    }
                }
                if (this.mContactListFilterController != null) {
                    AccountFilterUtil.startAccountFilterActivityForResult(this, 2, this.mContactListFilterController.getFilter());
                }
                ExceptionCapture.reportScene(23);
                StatisticalHelper.report(4031);
                return true;
            case R.id.menu_organize_contacts:
                ExceptionCapture.reportScene(57);
                StatisticalHelper.report(1133);
                intent = new Intent(getContext(), ContactsPreferenceActivity.class);
                intent.putExtra(":android:show_fragment", OrganizeContactsFragment.class.getName());
                intent.putExtra(":android:show_fragment_title", R.string.contacts_organize_contacts);
                intent.putExtra("profile_exists", this.mUserProfileExists);
                intent.putExtra("contactListFilter", getFilter());
                startActivity(intent);
                return true;
            case R.id.menu_import_export:
                Context activity = getActivity();
                if (getContactsCount() > 0) {
                    z = true;
                }
                CommonUtilMethods.launchManageContactsActivity(activity, z, PeopleActivity.class.getName());
                ExceptionCapture.reportScene(24);
                StatisticalHelper.report(4033);
                return true;
            case R.id.menu_settings:
                intent = new Intent(getContext(), ContactsPreferenceActivity.class);
                boolean settingsAreMultiPane = false;
                if (EmuiVersion.isSupportEmui()) {
                    settingsAreMultiPane = getResources().getBoolean(17956869);
                }
                if (!settingsAreMultiPane) {
                    intent.putExtra(":android:show_fragment", DisplayOptionsPreferenceFragment.class.getName());
                    intent.putExtra(":android:show_fragment_title", R.string.activity_title_settings);
                }
                startActivity(intent);
                StatisticalHelper.report(1153);
                ExceptionCapture.reportScene(25);
                return true;
            case R.id.menu_accounts:
                intent = new Intent("android.settings.SYNC_SETTINGS_EMUI");
                intent.putExtra("authorities", new String[]{"com.android.contacts"});
                intent.setFlags(524288);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    HwLog.e("DefaultContactBrowseListFragment", "Account settings Activity not found");
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    public EditText getContactsSearchView() {
        return this.mContactsSearchView;
    }

    public int getContactsCount() {
        return this.mContactsCount;
    }

    private void loadPredifeContactsFromCust() {
        int hwServiceContactLoaded = 0;
        try {
            hwServiceContactLoaded = System.getInt(getContext().getContentResolver(), "hw_service_contact_loaded", -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (hwServiceContactLoaded == -1) {
            new Thread() {
                public void run() {
                    synchronized (DefaultContactBrowseListFragment.this.mSyncObject) {
                        try {
                            if (-1 == System.getInt(DefaultContactBrowseListFragment.this.getContext().getContentResolver(), "hw_service_contact_loaded", -1) && SetupPhoneAccount.addTypeAndParseLoadPreDefinedContacts(DefaultContactBrowseListFragment.this.getContext())) {
                                System.putInt(DefaultContactBrowseListFragment.this.getContext().getContentResolver(), "hw_service_contact_loaded", 1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
    }

    private boolean isShowSimContacts() {
        return SharePreferenceUtil.getDefaultSp_de(getContext()).getBoolean("preference_show_sim_contacts", true);
    }

    private void initSearchModeFooter() {
        if (this.mBlankFooter != null) {
            getListView().removeFooterView(this.mBlankFooter);
        }
        this.mFooterButtons = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.online_search_buttons, getListView(), false);
        this.mExchangeFooter = (TextView) this.mFooterButtons.findViewById(R.id.footer_exchange);
        TextView mW3Button = (TextView) this.mFooterButtons.findViewById(R.id.search_w3_contacts);
        this.mExchangeFooter.setOnClickListener(this.mOnlineSearchListener);
        mW3Button.setOnClickListener(this.mOnlineSearchListener);
        updateButtonLayout(this.mExchangeFooter);
        updateButtonLayout(mW3Button);
        boolean exchangeVisible = this.mDirectoryManager.updateExchangeButton(this.mExchangeFooter);
        boolean w3Visible = this.mDirectoryManager.updateW3Button(mW3Button);
        if (exchangeVisible || w3Visible) {
            getListView().addFooterView(this.mFooterButtons, null, false);
            this.mFooterViews.add(this.mFooterButtons);
        }
        if (this.mBlankFooter != null) {
            getListView().addFooterView(this.mBlankFooter, null, false);
        }
    }

    private void configureListFooter(int count) {
        ContactListAdapter adapter = (ContactListAdapter) getAdapter();
        if (adapter != null) {
            if (isSearchMode() && getListView() != null && getListView().getHeaderViewsCount() > 0) {
                for (View view : this.mHeaderViews) {
                    getListView().removeHeaderView(view);
                }
            }
            if (!isSearchMode() || count <= 0 || getListView() == null || this.mFooterViews.size() != 0) {
                if (isSearchMode() || getListView() == null || getListView().getFooterViewsCount() <= 0) {
                    if (adapter.areAllPartitionsEmpty()) {
                    }
                }
                for (View view2 : this.mFooterViews) {
                    getListView().removeFooterView(view2);
                }
                this.mFooterViews.clear();
            } else if (this.mDirectoryManager.getExchangeOrW3Status()) {
                initSearchModeFooter();
            }
        }
    }

    private void updateSearchEmptyView() {
        if (((ContactListAdapter) getAdapter()).areAllPartitionsEmpty()) {
            initSearchListEmpty();
            this.mSearchEmptyView.setVisibility(0);
            this.mDirectoryManager.updateExchangeButton(this.mExchangeButton);
            this.mDirectoryManager.updateW3Button(this.mW3Button);
        }
    }

    private boolean areAllPriorityDirectoryLoaded() {
        for (int i = 0; i < this.mDirectoryLoadedState.size(); i++) {
            if (!((Boolean) this.mDirectoryLoadedState.valueAt(i)).booleanValue()) {
                return false;
            }
        }
        return true;
    }

    private void resetDirectoryLoadedState() {
        for (int i = 0; i < this.mDirectoryLoadedState.size(); i++) {
            this.mDirectoryLoadedState.setValueAt(i, Boolean.valueOf(false));
        }
    }

    protected void onDirectoryLoaded(Cursor cursor) {
        this.mDirectoryLoadedState.clear();
        int idColumnIndex = cursor.getColumnIndex("_id");
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            long directoryId = cursor.getLong(idColumnIndex);
            for (long j : sPriorityDirectoryIds) {
                if (j == directoryId) {
                    this.mDirectoryLoadedState.put(directoryId, Boolean.valueOf(false));
                }
            }
        }
        HwLog.i("DefaultContactBrowseListFragment", "onDirectoryLoaded,priritory directory count:" + this.mDirectoryLoadedState.size());
    }

    protected void onPartitionLoaded(int partitionIndex, Cursor data) {
        long directoryId = -1;
        Partition partition = ((ContactListAdapter) getAdapter()).getPartition(partitionIndex);
        if (partition instanceof DirectoryPartition) {
            directoryId = ((DirectoryPartition) partition).getDirectoryId();
        }
        for (int i = 0; i < this.mDirectoryLoadedState.size(); i++) {
            if (this.mDirectoryLoadedState.keyAt(i) == directoryId) {
                this.mDirectoryLoadedState.put(directoryId, Boolean.valueOf(true));
                break;
            }
        }
        super.onPartitionLoaded(partitionIndex, data);
    }

    private void handleAllAccountEmptyList(int aCount, View noContactsView, Activity activity, View pinnedHeaderList, ViewStub pinnedHeaderListStub, View pinnedHeaderView) {
        if (aCount == 0) {
            if (noContactsView == null) {
                ViewStub noContactsViewStub = (ViewStub) getView().findViewById(R.id.no_contacts_layout_container);
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    ((ViewStub) getView().findViewById(R.id.no_contacts_layout_container_split)).inflate();
                } else {
                    noContactsViewStub.inflate();
                }
                noContactsView = getView().findViewById(R.id.no_contacts_screen_layout);
                this.mImportContactsButton = (Button) getView().findViewById(R.id.btn_import_contacts);
                this.mCreateNewContactsButton = (Button) getView().findViewById(R.id.btn_create_new_contact);
                this.mViewCamcardButton = (Button) getView().findViewById(R.id.btn_view_camcard);
                this.mSignInToAccountButton = (Button) getView().findViewById(R.id.btn_sign_into_account);
                this.mButtonGroup = getView().findViewById(R.id.button_group);
            }
            noContactsView.setVisibility(0);
            setEmptyContactLocation(noContactsView.findViewById(R.id.ll_no_contacts_parent_view));
            if (this.mSearchLayout != null) {
                this.mSearchLayout.setVisibility(8);
            }
            if (this.mSplitActionBarView != null) {
                this.mSplitActionBarView.setVisibility(8);
            }
            updateButtonState();
            TextView textNoContacts = (TextView) getView().findViewById(R.id.text_no_contacts);
            textNoContacts.setText(activity.getString(R.string.noContacts));
            textNoContacts.setContentDescription(activity.getString(R.string.noContacts));
            hideBanners();
            return;
        }
        if (noContactsView != null) {
            noContactsView.setVisibility(8);
        }
        pinnedHeaderList.setVisibility(0);
        pinnedHeaderView.setVisibility(0);
        if (this.dismissDialogHandler != null) {
            this.dismissDialogHandler.sendEmptyMessage(10);
        }
        if (!SystemProperties.getBoolean("ro.config.hw_opt_pre_contact", false)) {
            loadPredifeContactsFromCust();
        }
        reloadHeaderView();
        showSyncBirthdayToCalandarView();
    }

    private void handleOtherEmptyList(int aCount, View noContactsView, Activity activity, View pinnedHeaderList, ViewStub pinnedHeaderListStub, int filterType) {
        if (aCount == 0) {
            if (noContactsView == null) {
                ViewStub noContactsViewStub = (ViewStub) getView().findViewById(R.id.no_contacts_layout_container);
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    ((ViewStub) getView().findViewById(R.id.no_contacts_layout_container_split)).inflate();
                } else {
                    noContactsViewStub.inflate();
                }
                noContactsView = getView().findViewById(R.id.no_contacts_screen_layout);
                this.mImportContactsButton = (Button) getView().findViewById(R.id.btn_import_contacts);
                this.mCreateNewContactsButton = (Button) getView().findViewById(R.id.btn_create_new_contact);
                this.mViewCamcardButton = (Button) getView().findViewById(R.id.btn_view_camcard);
                this.mSignInToAccountButton = (Button) getView().findViewById(R.id.btn_sign_into_account);
                this.mButtonGroup = getView().findViewById(R.id.button_group);
            }
            noContactsView.setVisibility(0);
            setEmptyContactLocation(noContactsView.findViewById(R.id.ll_no_contacts_parent_view));
            if (pinnedHeaderListStub != null) {
                pinnedHeaderListStub.setVisibility(8);
            }
            this.mImportContactsButton.setVisibility(8);
            this.mCreateNewContactsButton.setVisibility(8);
            if (this.mViewCamcardButton != null) {
                this.mViewCamcardButton.setVisibility(8);
            }
            this.mSignInToAccountButton.setVisibility(8);
            if (this.mButtonGroup != null) {
                this.mButtonGroup.setVisibility(8);
            }
            TextView textNoContacts = (TextView) getView().findViewById(R.id.text_no_contacts);
            if (-3 == getFilter().filterType || -17 == filterType) {
                textNoContacts.setText(activity.getString(R.string.list_TotalAllContactsZeroCustom));
            } else if ("com.android.huawei.phone".equalsIgnoreCase(getFilter().accountType)) {
                AccountType accountType = AccountTypeManager.getInstance(getContext().getApplicationContext()).getAccountType(getFilter().accountType, getFilter().dataSet);
                textNoContacts.setText(activity.getString(R.string.listTotalAllContactsZeroGroup, new Object[]{accountType.getDisplayLabel(getContext()).toString()}));
            } else if ("com.android.huawei.sim".equalsIgnoreCase(getFilter().accountType)) {
                if (SimFactoryManager.isBothSimEnabled()) {
                    textNoContacts.setText(activity.getString(R.string.listTotalAllContactsZeroGroup, new Object[]{activity.getString(R.string.str_filter_sim1)}));
                } else {
                    textNoContacts.setText(activity.getString(R.string.contact_no_contacts_in_sim));
                }
            } else if (!"com.android.huawei.secondsim".equalsIgnoreCase(getFilter().accountType)) {
                textNoContacts.setText(activity.getString(R.string.listTotalAllContactsZeroGroup, new Object[]{getFilter().accountName}));
            } else if (SimFactoryManager.isBothSimEnabled()) {
                textNoContacts.setText(activity.getString(R.string.listTotalAllContactsZeroGroup, new Object[]{activity.getString(R.string.str_filter_sim2)}));
            } else {
                textNoContacts.setText(activity.getString(R.string.contact_no_contacts_in_sim));
            }
            hideBanners();
        } else {
            if (noContactsView != null) {
                noContactsView.setVisibility(8);
            }
            if (pinnedHeaderListStub != null) {
                pinnedHeaderListStub.setVisibility(0);
            }
            reloadHeaderView();
            showSyncBirthdayToCalandarView();
        }
        pinnedHeaderList.setVisibility(0);
    }

    public void handleEmptyList(int aCount) {
        if (HwLog.HWDBG) {
            HwLog.d("DefaultContactBrowseListFragment", "handleEmptyList, data size>0?" + (aCount > 0) + " isSearchMode():" + isSearchMode());
        }
        Activity activity = getActivity();
        if (activity != null) {
            if (!getIsVoiceSearchMode()) {
                configureListFooter(aCount);
            }
            View pinnedHeaderList = getView().findViewById(R.id.pinned_header_list_layout);
            View noContactsView = getView().findViewById(R.id.no_contacts_screen_layout);
            View pinnedHeaderView = getView().findViewById(R.id.pinnedHeaderRelativeLayout);
            ViewStub pinnedHeaderListStub = (ViewStub) getView().findViewById(R.id.pinnedHeaderList_stub);
            boolean lIsAlphaScrollerVisible = isAlphaScrollerVisible();
            if (this.mSearchLayout != null && this.mSearchLayout.getVisibility() == 0) {
                ViewGroup.LayoutParams lParams = this.mSearchLayout.getLayoutParams();
                if (lParams != null && (lParams instanceof LinearLayout.LayoutParams)) {
                    int i;
                    ViewGroup.LayoutParams lLP = (LinearLayout.LayoutParams) lParams;
                    Resources resources = activity.getResources();
                    if (lIsAlphaScrollerVisible) {
                        i = R.dimen.searchLayout_margin_right_default;
                    } else {
                        i = R.dimen.searchLayout_margin_left_right;
                    }
                    lLP.setMarginEnd(resources.getDimensionPixelOffset(i));
                    this.mSearchLayout.setLayoutParams(lLP);
                }
            }
            if (isSearchMode()) {
                if (this.mSuspentionScroller != null) {
                    if (getIsVoiceSearchMode()) {
                        this.mSuspentionScroller.hideSuspentionView(0, true);
                    } else {
                        this.mSuspentionScroller.showSuspentionView();
                    }
                }
                if (noContactsView != null) {
                    noContactsView.setVisibility(8);
                }
                pinnedHeaderList.setVisibility(0);
                pinnedHeaderView.setVisibility(0);
                super.handleEmptyList(aCount);
                if (aCount == 0) {
                    if (getIsVoiceSearchMode()) {
                        getEmptyTextView().setText(R.string.contact_list_FoundAllContactsZero);
                        getEmptyTextView().setContentDescription(activity.getString(R.string.contact_list_FoundAllContactsZero));
                        getEmptyTextView().setVisibility(0);
                        if (this.mSearchEmptyView != null) {
                            this.mSearchEmptyView.setVisibility(8);
                        }
                    } else {
                        getEmptyTextView().setVisibility(8);
                        if (areAllPriorityDirectoryLoaded()) {
                            updateSearchEmptyView();
                        } else if (this.mSearchEmptyView != null) {
                            this.mSearchEmptyView.setVisibility(8);
                        }
                    }
                } else if (this.mSearchEmptyView != null) {
                    this.mSearchEmptyView.setVisibility(8);
                }
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    if (aCount == 0) {
                        initRightContainer(new NoContentFragment());
                        setSelectedContactUri(null, null, null, null, null);
                        this.mLastSearchKey = getQueryString();
                    } else {
                        this.dismissDialogHandler.sendEmptyMessageDelayed(262, 200);
                    }
                }
                return;
            }
            if (aCount == 0 && this.mSearchLayout != null) {
                this.mSearchLayout.setVisibility(0);
                if (CommonUtilMethods.isSimplifiedModeEnabled()) {
                    this.mContactsSearchView.setText("");
                }
            }
            if (this.mSearchLayout != null) {
                this.mSearchLayout.setVisibility(0);
            }
            if (this.mSplitActionBarView != null) {
                this.mSplitActionBarView.setVisibility(0);
            }
            int filterType = -2;
            if (getFilter() != null) {
                filterType = getFilter().filterType;
            }
            if (aCount == 0 && filterType == -2 && !isShowSimContacts()) {
                filterType = -17;
            }
            if (HwLog.HWDBG) {
                HwLog.d("DefaultContactBrowseListFragment", "filterType:" + filterType);
            }
            switch (filterType) {
                case -17:
                case -3:
                case 0:
                    handleOtherEmptyList(aCount, noContactsView, activity, pinnedHeaderList, pinnedHeaderListStub, filterType);
                    break;
                case -2:
                    handleAllAccountEmptyList(aCount, noContactsView, activity, pinnedHeaderList, pinnedHeaderListStub, pinnedHeaderView);
                    break;
                default:
                    HwLog.w("DefaultContactBrowseListFragment", "Unhandled filter type: " + filterType);
                    break;
            }
            super.handleEmptyList(aCount);
            if (this.mEmptyTextView != null && aCount == 0) {
                this.mEmptyTextView.setVisibility(8);
            }
            if (this.mSearchEmptyView != null) {
                this.mSearchEmptyView.setVisibility(8);
            }
            if (this.mIsDataLoadedForMainList) {
                generateAndSendMessageDelayed(261, filterType, aCount, 200);
            }
            if (!isSearchMode()) {
                this.mLastSearchKey = null;
            }
        }
    }

    private void setEmptyContactLocation(View llNoContacts) {
        Activity activity = getActivity();
        if (activity != null) {
            boolean isPor = getResources().getConfiguration().orientation == 1;
            if (llNoContacts == null) {
                return;
            }
            if (isPor) {
                MarginLayoutParams params = (MarginLayoutParams) llNoContacts.getLayoutParams();
                params.topMargin = CommonUtilMethods.getMarginTopPix(activity, 0.3d, isPor);
                llNoContacts.setLayoutParams(params);
                return;
            }
            llNoContacts.setPadding(0, 0, 0, CommonUtilMethods.getActionBarAndStatusHeight(activity, isPor));
        }
    }

    public void showContactFromNewContact(Uri lookup) {
        if (!isSearchMode()) {
            switchRightContactFragment(false, lookup);
        }
    }

    private void switchRightFragmentByType(int type, boolean isInit) {
        if (type == -1 || !isExistHeadType(type)) {
            type = 0;
            isInit = true;
        }
        switch (type) {
            case 0:
                Uri firstUri = getFristContactUri();
                if (isOneColumnSplit() && isInMainPage()) {
                    setSelectedContactUri(firstUri);
                    this.mSplitSelectedType = 0;
                    this.mSplitSelectedContactUri = firstUri;
                    return;
                }
                switchRightContactFragment(isInit, firstUri);
                return;
            case 1:
                switchRightGroupFragment(isInit, false, false);
                return;
            case 4:
                switchRightCCBrowseListFragment(isInit, false, false);
                return;
            default:
                return;
        }
    }

    private boolean isInMainPage() {
        if (this.mDefaultFrgContainer.getSelectedContainer() == 0 || this.mDefaultFrgContainer.getSelectedContainer() == -1) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onPageSelect() {
        if (!(!isOneColumnSplit() || this.mDefaultFrgContainer == null || isInMainPage())) {
            switchRightFragmentByType(this.mSplitSelectedType, true);
            this.mDefaultFrgContainer.setSelectedContainer(0);
            this.mDefaultFrgContainer.refreshFragmentLayout();
        }
    }

    private void updateButtonLayout(View view) {
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            int width = 0;
            int marrgin = 0;
            Context context = getActivity();
            if (isTwoColumnSplit()) {
                marrgin = ContactDpiAdapter.getNewPxDpi(R.dimen.no_contacts_button_margin_split, context);
            } else {
                width = ContactDpiAdapter.getNewPxDpi(R.dimen.no_contacts_empty_button_width, context);
            }
            ScreenUtils.updateButtonView(context, view, marrgin, width);
        }
    }

    private void showOrHideMaskView(boolean isShow) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && getView() != null) {
            FrameLayout maskView = (FrameLayout) getView().findViewById(R.id.split_mask_overlay);
            if (maskView != null) {
                int statusHeight = ContactDpiAdapter.getStatusBarHeight(getActivity());
                ViewGroup.LayoutParams params = maskView.getLayoutParams();
                if (params instanceof FrameLayout.LayoutParams) {
                    ((FrameLayout.LayoutParams) params).topMargin = statusHeight;
                    maskView.setLayoutParams(params);
                }
                if (getActivity() instanceof PeopleActivity) {
                    if (!((PeopleActivity) getActivity()).getNeedMaskDefault()) {
                        maskView.setVisibility(8);
                        maskView.setOnTouchListener(null);
                    } else if (maskView.getVisibility() != 0) {
                        maskView.setVisibility(0);
                        if (isShow) {
                            AlphaAnimation aAnima = new AlphaAnimation(0.0f, 0.0f);
                            aAnima.setDuration(180);
                            maskView.startAnimation(aAnima);
                        }
                        maskView.setOnTouchListener(this.touchListener);
                    } else {
                        return;
                    }
                    return;
                }
                maskView.setVisibility(8);
                maskView.setOnTouchListener(null);
            }
        }
    }

    private void switchRightContactFragment(boolean isInit, Uri contacturi) {
        if (contacturi == null) {
            HwLog.e("DefaultContactBrowseListFragment", "right contact uri is null");
            return;
        }
        setSelectedContactUri(contacturi);
        Intent intent = new Intent("android.intent.action.VIEW", contacturi);
        ContactInfoFragment contactInfo = new ContactInfoFragment();
        contactInfo.setIntent(intent);
        this.mSplitSelectedType = 0;
        this.mSplitSelectedContactUri = contacturi;
        if (isInit) {
            initRightContainer(contactInfo);
        } else {
            openRightContainer(contactInfo);
        }
        if (this.mIsNeedShowSelect) {
            int pos = getSelectedContactPosition();
            if (pos > 0) {
                requestSelectionToScreen(pos, false);
            }
        }
        setHeadViewFouce(0);
    }

    private void switchRightGroupFragment(boolean isInit, boolean reload, boolean isClick) {
        if (isInit) {
            initRightContainer(new GroupBrowseListFragment());
        } else if (((GroupBrowseListFragment) getChildFragmentManager().findFragmentByTag(GroupBrowseListFragment.class.getName())) == null || reload || this.mSplitSelectedType != 1) {
            if (isClick) {
                openRightContainer(new GroupBrowseListFragment());
            } else {
                initRightContainer(new GroupBrowseListFragment());
            }
        } else if (this.mDefaultFrgContainer != null) {
            this.mDefaultFrgContainer.setSelectedContainer(1);
        }
        setSelectedContactUri(null, false, false, false, false);
        setHeadViewFouce(1);
        this.mSplitSelectedType = 1;
        this.mSplitSelectedContactUri = null;
    }

    private void switchRightCCBrowseListFragment(boolean isInit, boolean reload, boolean isClick) {
        if (isInit) {
            getActivity().startService(CCSaveService.createMultiUpdateIntent(getActivity()));
            initRightContainer(new CCBrowseListFragment());
        } else if (((CCBrowseListFragment) getChildFragmentManager().findFragmentByTag(CCBrowseListFragment.class.getName())) == null || reload || this.mSplitSelectedType != 4) {
            getActivity().startService(CCSaveService.createMultiUpdateIntent(getActivity()));
            if (isClick) {
                openRightContainer(new CCBrowseListFragment());
            } else {
                initRightContainer(new CCBrowseListFragment());
            }
        } else if (this.mDefaultFrgContainer != null) {
            this.mDefaultFrgContainer.setSelectedContainer(1);
        }
        setSelectedContactUri(null, false, false, false, false);
        setHeadViewFouce(4);
        this.mSplitSelectedType = 4;
        this.mSplitSelectedContactUri = null;
    }

    private void handleSplitScreenInitFragment(int filterType, int count) {
        if (HwLog.HWDBG) {
            HwLog.d("DefaultContactBrowseListFragment", "handle split srceen type " + filterType + " count " + count);
        }
        if (CommonUtilMethods.calcIfNeedSplitScreen() && count >= 0 && getActivity() != null && !getActivity().isFinishing() && isAdded()) {
            switch (filterType) {
                case -17:
                case -3:
                case 0:
                    if (count != 0) {
                        showInitInfoForSplit();
                        break;
                    } else {
                        showEmptyFragmentIfSplit(false);
                        break;
                    }
                case -2:
                    if (count != 0) {
                        showInitInfoForSplit();
                        break;
                    } else {
                        showEmptyFragmentIfSplit(true);
                        break;
                    }
                default:
                    HwLog.w("DefaultContactBrowseListFragment", "handleInitShowFragment Unhandled filter type: " + filterType);
                    break;
            }
            showOrHideActionbar();
        }
    }

    private void showEmptyFragmentIfSplit(boolean isShow) {
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            int emptyType;
            TextView textNoContacts = (TextView) getView().findViewById(R.id.text_no_contacts);
            if (isShow && isTwoColumnSplit() && !this.isInPortrait) {
                if (textNoContacts != null) {
                    textNoContacts.setVisibility(4);
                }
                emptyType = 0;
            } else {
                if (textNoContacts != null) {
                    textNoContacts.setVisibility(0);
                }
                emptyType = -1;
            }
            NoContentFragment frag = (NoContentFragment) getChildFragmentManager().findFragmentByTag(NoContentFragment.class.getName());
            if (frag == null || frag.getShowEmptyType() != emptyType) {
                initRightContainer(new NoContentFragment(emptyType));
            }
        }
    }

    private void showInitInfoForSplit() {
        if (this.mDefaultFrgContainer != null) {
            if (-1 == this.mSplitSelectedType || this.mIsFromNewIntent) {
                if (this.mIsFromNewIntent && this.mSplitSelectedType == 0) {
                    if (this.mSplitSelectedContactUri == null) {
                        this.mSplitSelectedContactUri = getFristContactUri();
                    }
                    switchRightContactFragment(true, this.mSplitSelectedContactUri);
                } else {
                    boolean ifNeedFromNew = true;
                    if (this.mIsFromNewIntent && isTwoColumnSplit()) {
                        ifNeedFromNew = false;
                    }
                    switchRightFragmentByType(this.mSplitSelectedType, ifNeedFromNew);
                }
                this.mIsFromNewIntent = false;
            } else if (this.mSplitSelectedType != 0) {
                boolean ifNeedInit = false;
                if (TextUtils.isEmpty(getQueryString()) && !TextUtils.isEmpty(this.mLastSearchKey)) {
                    ifNeedInit = true;
                }
                switchRightFragmentByType(this.mSplitSelectedType, ifNeedInit);
            } else if (this.mSplitSelectedContactUri == null) {
                HwLog.e("DefaultContactBrowseListFragment", "show init contact for split, the uri is null");
            } else {
                Uri lastSelect = getSelectedContactUri();
                SelectedUri selectedUri = new SelectedUri(this.mSplitSelectedContactUri);
                int findpos = getContactPosition(selectedUri.getDirId(), selectedUri.getLookupKey(), selectedUri.getContactId());
                if (findpos != -1) {
                    boolean isNeedReload = false;
                    if (lastSelect == null || !this.mSplitSelectedContactUri.equals(lastSelect)) {
                        isNeedReload = true;
                    } else if (((ContactInfoFragment) getChildFragmentManager().findFragmentByTag(ContactInfoFragment.class.getName())) == null) {
                        isNeedReload = true;
                    }
                    if (isNeedReload) {
                        Intent intent = new Intent("android.intent.action.VIEW", this.mSplitSelectedContactUri);
                        ContactInfoFragment contactInfo = new ContactInfoFragment();
                        contactInfo.setIntent(intent);
                        setSelectedContactUri(this.mSplitSelectedContactUri);
                        initRightContainer(contactInfo);
                    } else {
                        setSelectedContactUri(this.mSplitSelectedContactUri);
                    }
                    if (this.mIsNeedShowSelect) {
                        requestSelectionToScreen(findpos, false);
                        setHeadViewFouce(0);
                    }
                } else {
                    switchRightContactFragment(true, getDefaultContactUri());
                }
            }
        }
    }

    private void showInitInfoForSplitSearchMode() {
        if (this.mDefaultFrgContainer != null) {
            boolean isKeyChange;
            Uri defaultUri;
            CharSequence key = getQueryString();
            if (key != null && key.length() == 0) {
                key = null;
            }
            if (this.mLastSearchKey != null && this.mLastSearchKey.length() == 0) {
                this.mLastSearchKey = null;
            }
            if (TextUtils.equals(key, this.mLastSearchKey)) {
                isKeyChange = false;
            } else {
                isKeyChange = true;
            }
            if (isKeyChange) {
                defaultUri = getFristContactUri();
            } else {
                defaultUri = getDefaultContactUri();
            }
            SelectedUri select = new SelectedUri(getSelectedContactUri());
            int findpos = getContactPosition(select.getDirId(), select.getLookupKey(), select.getContactId());
            Intent intent;
            ContactInfoFragment contactInfo;
            if (findpos != -1) {
                if (((ContactInfoFragment) getChildFragmentManager().findFragmentByTag(ContactInfoFragment.class.getName())) == null && isTwoColumnSplit()) {
                    intent = new Intent("android.intent.action.VIEW", defaultUri);
                    contactInfo = new ContactInfoFragment();
                    contactInfo.setIntent(intent);
                    initRightContainer(contactInfo);
                }
                if (getIsVoiceSearchMode()) {
                    refreshSelection();
                }
            } else if (defaultUri == null) {
                HwLog.e("DefaultContactBrowseListFragment", "SearchMode defaultUri is null !");
                return;
            } else {
                setSelectedContactUri(defaultUri);
                if (isTwoColumnSplit()) {
                    intent = new Intent("android.intent.action.VIEW", defaultUri);
                    contactInfo = new ContactInfoFragment();
                    contactInfo.setIntent(intent);
                    initRightContainer(contactInfo);
                    findpos = 0;
                }
            }
            if (!isKeyChange) {
                requestSelectionToScreen(findpos, false);
            }
            this.mLastSearchKey = getQueryString();
        }
    }

    private int getHeadTypeByView(View view) {
        Intent obj = view.getTag();
        if (obj instanceof Intent) {
            Intent intent = obj;
            if ("com.huawei.yellowpage.intent.action.main".equals(intent.getAction())) {
                return 2;
            }
            if ("com.huawei.android.intent.action.CAMCARD_CONTACT".equals(intent.getAction())) {
                return 4;
            }
            if (intent.getComponent() != null) {
                String className = intent.getComponent().getClassName();
                if (FavoriteContactsActivity.class.getName().equals(className)) {
                    return 5;
                }
                if (GroupBrowserActivity.class.getName().equals(className)) {
                    return 1;
                }
            }
        }
        return -1;
    }

    private boolean isExistHeadType(int type) {
        if (CommonUtilMethods.isSimplifiedModeEnabled() || -1 == type || type == 0) {
            return false;
        }
        if (1 == type) {
            return true;
        }
        return 4 == type && EmuiFeatureManager.isCamcardEnabled();
    }

    private void setHeadViewFouce(int type) {
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            for (int i = 0; i < this.mHeaderViews.size(); i++) {
                View v = (View) this.mHeaderViews.get(i);
                if (v != null) {
                    if (getHeadTypeByView(v) == type && this.mIsNeedShowSelect) {
                        v.setBackgroundColor(getActivity().getResources().getColor(R.color.split_itme_selected));
                    } else {
                        v.setBackgroundColor(0);
                    }
                }
            }
        }
    }

    private void updateButtonState() {
        if (HwLog.HWDBG) {
            HwLog.d("DefaultContactBrowseListFragment", "updateButtonState");
        }
        if (this.mButtonGroup != null) {
            this.mButtonGroup.setVisibility(0);
        }
        if (this.mCreateNewContactsButton != null) {
            this.mCreateNewContactsButton.setVisibility(0);
            this.mCreateNewContactsButton.setOnClickListener(this.emptyContactScreenOnClickListener);
        }
        if (this.mViewCamcardButton != null) {
            if (!EmuiFeatureManager.isCamcardEnabled() || CommonUtilMethods.isSimplifiedModeEnabled()) {
                this.mViewCamcardButton.setVisibility(8);
            } else {
                this.mViewCamcardButton.setVisibility(0);
                if (EmuiFeatureManager.isSuperSaverMode()) {
                    this.mViewCamcardButton.setEnabled(false);
                } else {
                    this.mViewCamcardButton.setEnabled(true);
                    this.mViewCamcardButton.setOnClickListener(this.emptyContactScreenOnClickListener);
                }
            }
        }
        if (this.mSignInToAccountButton != null) {
            this.mSignInToAccountButton.setVisibility(0);
            if (EmuiFeatureManager.isSuperSaverMode()) {
                this.mSignInToAccountButton.setEnabled(false);
            } else {
                this.mSignInToAccountButton.setEnabled(true);
                this.mSignInToAccountButton.setOnClickListener(this.emptyContactScreenOnClickListener);
            }
        }
        if (this.mImportContactsButton != null) {
            this.mImportContactsButton.setVisibility(0);
            if (EmuiFeatureManager.isSuperSaverMode()) {
                this.mImportContactsButton.setEnabled(false);
                return;
            }
            this.mImportContactsButton.setEnabled(true);
            this.mImportContactsButton.setOnClickListener(this.emptyContactScreenOnClickListener);
        }
    }

    private void setButtonStateInSuperSaverMode() {
        if (this.mSignInToAccountButton != null) {
            if (EmuiFeatureManager.isSuperSaverMode()) {
                this.mSignInToAccountButton.setEnabled(false);
            } else {
                this.mSignInToAccountButton.setEnabled(true);
                this.mSignInToAccountButton.setOnClickListener(this.emptyContactScreenOnClickListener);
            }
        }
        if (this.mImportContactsButton != null) {
            if (EmuiFeatureManager.isSuperSaverMode()) {
                this.mImportContactsButton.setEnabled(false);
            } else {
                this.mImportContactsButton.setEnabled(true);
                this.mImportContactsButton.setOnClickListener(this.emptyContactScreenOnClickListener);
            }
        }
        if (this.mViewCamcardButton != null) {
            this.mViewCamcardButton.setVisibility(0);
            if (EmuiFeatureManager.isSuperSaverMode()) {
                this.mViewCamcardButton.setEnabled(false);
                return;
            }
            this.mViewCamcardButton.setEnabled(true);
            this.mViewCamcardButton.setOnClickListener(this.emptyContactScreenOnClickListener);
        }
    }

    protected void startLoading() {
        if (this.mGenHdlr == null || !this.mDelayIfRequired) {
            super.startLoading();
        } else {
            this.mGenHdlr.requestDelayedExecution(new Runnable() {
                public void run() {
                    DefaultContactBrowseListFragment.this.mDelayIfRequired = false;
                    DefaultContactBrowseListFragment.this.startLoadingInternal();
                    DefaultContactBrowseListFragment.this.mGenHdlr = null;
                }
            }, 1500);
        }
    }

    private void startLoadingInternal() {
        super.startLoading();
    }

    public boolean hasProfileAndIsVisible() {
        return false;
    }

    public void onSaveInstanceState(Bundle outState) {
        int i = 0;
        this.isOnSaveInstance = true;
        super.onSaveInstanceState(outState);
        if (this.mCcardHandlr != null) {
            this.mCcardHandlr.onSaveInstance(outState);
        }
        outState.putInt("total_contacts_count", this.mContactsCount);
        outState.putParcelable("key_select_contact_uri", this.mSplitSelectedContactUri);
        outState.putInt("key_select_type", this.mSplitSelectedType);
        outState.putString("key_last_search", this.mLastSearchKey);
        outState.putFloat("display_rate", this.mDisplayRate);
        outState.putParcelable("share_contact_uri", this.mShareUri);
        if (this.mRemoteMessenger != null) {
            outState.putParcelable("remote_messenger", this.mRemoteMessenger);
        }
        if (this.serchlayout != null) {
            String str = "serchlayout_visibility";
            if (this.serchlayout.getScrollY() > 0) {
                i = this.serchlayout.getMeasuredHeight();
            }
            outState.putInt(str, i);
        }
    }

    public void saveInstanceState(Bundle outState) {
        onSaveInstanceState(outState);
        FragmentManager fm = getChildFragmentManager();
        Fragment groupBrowse = fm.findFragmentByTag(GroupBrowseListFragment.class.getName());
        if (groupBrowse != null) {
            outState.putBoolean(GroupBrowseListFragment.class.getName(), true);
            groupBrowse.onSaveInstanceState(outState);
        }
        Fragment smartGroup = fm.findFragmentByTag(SmartGroupBrowseListFragment.class.getName());
        if (smartGroup != null) {
            outState.putBoolean(SmartGroupBrowseListFragment.class.getName(), true);
            smartGroup.onSaveInstanceState(outState);
        }
        Fragment groupDetail = fm.findFragmentByTag(GroupDetailFragment.class.getName());
        if (groupDetail != null) {
            outState.putBoolean(GroupDetailFragment.class.getName(), true);
            groupDetail.onSaveInstanceState(outState);
        }
    }

    private void makeMenuItemVisible(Menu aMenu, int aItemId, boolean aVisible) {
        MenuItem item = aMenu.findItem(aItemId);
        if (item != null) {
            item.setVisible(aVisible);
        }
    }

    public void setFilterController(ContactListFilterController aContactListFilterController, boolean aRestoreSelectedUri, boolean aIsChanged) {
        this.mContactListFilterController = aContactListFilterController;
        setFilter(aContactListFilterController.getFilter(), aRestoreSelectedUri, aIsChanged);
    }

    public void setFilterController(ContactListFilterController aContactListFilterController) {
        this.mContactListFilterController = aContactListFilterController;
        setFilter(aContactListFilterController.getFilter());
    }

    public boolean handleKeyEvent(int aKeyCode) {
        boolean handleclick = true;
        if (this.mContactsCount <= 0 && getFilter() != null && getFilter().filterType == -2) {
            handleclick = false;
        }
        if (82 != aKeyCode || handleclick) {
            return super.handleKeyEvent(aKeyCode);
        }
        return false;
    }

    private void startVoiceRecgnizing() {
        if (HwLog.HWDBG) {
            HwLog.d("DefaultContactBrowseListFragment", "startVoiceRecgnizing");
        }
        try {
            startActivityForResult(new Intent("com.huawei.vassistant.action.CONTACT_SEARCH"), 3);
        } catch (ActivityNotFoundException e) {
            HwLog.e("DefaultContactBrowseListFragment", "start voice assistant fail:" + e.getMessage());
        }
    }

    public void setSimpleShowMode() {
        getView().findViewById(R.id.menu_view).setVisibility(8);
        if (this.mSearchLayout != null) {
            this.mSearchLayout.setVisibility(8);
        }
        setIsVoiceSearchMode(true);
        setSearchModeInitialized(true);
        setSearchMode(true);
    }

    private boolean isVoiceRecognitionUseable() {
        boolean z = true;
        try {
            PackageManager packageManager = getActivity().getPackageManager();
            int enabledSetting = packageManager.getApplicationEnabledSetting("com.huawei.vassistant");
            if (HwLog.HWDBG) {
                HwLog.d("DefaultContactBrowseListFragment", "isVoiceRecognitionExist,enabledSetting=" + enabledSetting);
            }
            PackageInfo packinfo = packageManager.getPackageInfo("com.huawei.vassistant", 1);
            boolean hasVoiceRecActivity = false;
            if (packinfo != null) {
                ActivityInfo[] activityInfo = packinfo.activities;
                if (activityInfo != null) {
                    for (ActivityInfo activityInfo2 : activityInfo) {
                        if ("com.huawei.vassistant.ui.contactsearch.VAssistantWidgetActivity".equals(activityInfo2.name)) {
                            hasVoiceRecActivity = true;
                            break;
                        }
                    }
                }
            }
            if (!hasVoiceRecActivity || 2 == enabledSetting || 3 == enabledSetting) {
                z = false;
            }
            return z;
        } catch (IllegalArgumentException e) {
            HwLog.w("DefaultContactBrowseListFragment", e.getMessage());
            return false;
        } catch (NameNotFoundException e2) {
            HwLog.w("DefaultContactBrowseListFragment", e2.getMessage());
            return false;
        }
    }

    private void performShareContacts(MenuItem item, int position) {
        if (item != null) {
            this.dismissDialogHandler.sendEmptyMessage(257);
        }
    }

    private void preLoadContactForShare(int position) {
        try {
            Uri uriCurrentFormat = ContactLoaderUtils.ensureIsContactUri(getContext().getContentResolver(), ((ContactListAdapter) getAdapter()).getContactUri(position));
            this.mContactData = null;
            this.mShareUri = uriCurrentFormat;
            loadContactForShare(uriCurrentFormat);
        } catch (IllegalArgumentException e) {
            HwLog.e("DefaultContactBrowseListFragment", "URL Exception is invalid");
        }
    }

    private void loadContactForShare(Uri lookupUri) {
        Bundle args = new Bundle();
        args.putParcelable("contactUri", lookupUri);
        getLoaderManager().restartLoader(1, args, this.mContactLoadedListener);
    }

    protected void shareContact() {
        AlertDialogFragmet.show(getFragmentManager(), (int) R.string.profile_dialog_share_contacts, (int) R.array.share_contact_select_items, Boolean.valueOf(true), this.shareItemSelectListener, 3);
    }

    protected void shareVcard() {
        ContactDetailHelper.shareContact(this.mContactData, this.mTotalContext);
        StatisticalHelper.report(2037);
    }

    protected void shareTextCard() {
        if (this.mTotalContext != null) {
            String textCardString = ProfileUtils.buildShareTextCard(this.mEntriesObject);
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra("android.intent.extra.TEXT", textCardString);
            intent.addHwFlags(16);
            this.mTotalContext.startActivity(Intent.createChooser(intent, this.mTotalContext.getString(R.string.profile_dialog_share_contacts)));
        }
    }

    protected void shareBusinessCard() {
        if (this.mTotalContext != null) {
            Uri uri = ProfileUtils.getBitmapFileUri(getApplicationContext(), this.mQrCodeBitmap);
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("image/*");
            intent.putExtra("android.intent.extra.STREAM", uri);
            intent.addFlags(3);
            this.mTotalContext.startActivity(Intent.createChooser(intent, this.mTotalContext.getString(R.string.profile_dialog_share_contacts)));
        }
    }

    private void getQRCodeBitmap(final Bundle bundle) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                DefaultContactBrowseListFragment.this.generatQrBitmap(bundle);
            }
        });
        thread.setName("Profile QRCode Thread");
        thread.start();
    }

    private void generatQrBitmap(Bundle bundle) {
        try {
            if (this.mContactData == null || !isAdded()) {
                this.dismissDialogHandler.sendEmptyMessage(259);
                return;
            }
            Bitmap QrCodeBitmap = new QRCodeEncoder(getApplicationContext()).encodeQRCodeContents(bundle, null, "CONTACT_TYPE", null);
            int width = getResources().getDimensionPixelSize(R.dimen.profile_simple_card_qrcode_size);
            this.mQrCodeBitmap = ProfileUtils.colorBitmap(QrCodeBitmap, width, width, 16.0f, -16776961);
            if (!(QrCodeBitmap == null || this.mQrCodeBitmap == QrCodeBitmap)) {
                QrCodeBitmap.recycle();
            }
            this.dismissDialogHandler.sendEmptyMessage(258);
        } catch (WriterException e) {
            HwLog.e("DefaultContactBrowseListFragment", "Can not generate QRcode bitmap !");
            this.dismissDialogHandler.sendEmptyMessage(259);
        }
    }

    private void bindQRCodeGenerat() {
        if (this.mQrCodeBitmap == null) {
            bindQRCodeGeneratFail();
            return;
        }
        if (this.shareMenu != null) {
            this.shareMenu.setEnabled(!EmuiFeatureManager.isSuperSaverMode());
        }
        ProfileUtils.getBitmapFileUri(getApplicationContext(), this.mQrCodeBitmap);
    }

    private void bindQRCodeGeneratFail() {
        HwLog.w("DefaultContactBrowseListFragment", "Can not generate qrcode bitmap !");
        if (this.shareMenu != null) {
            this.shareMenu.setEnabled(false);
        }
    }

    private void performAddToBlackList(MenuItem item, int position, String displayName, ArrayList<String> lPhoneNumberList, IHarassmentInterceptionService aService) {
        if (item != null) {
            if (item.getTitle().equals(getResources().getText(R.string.contact_menu_add_to_blacklist))) {
                StatisticalHelper.report(4024);
                BlacklistCommonUtils.handleNumberBlockList(getContext(), aService, lPhoneNumberList, displayName, 0);
            } else if (item.getTitle().equals(getResources().getText(R.string.contact_menu_remove_from_blacklist))) {
                BlacklistCommonUtils.handleNumberBlockList(getContext(), aService, lPhoneNumberList, displayName, 1);
            }
            if (CommonUtilMethods.calcIfNeedSplitScreen() && isTwoColumnSplit()) {
                ContactInfoFragment frag = (ContactInfoFragment) getChildFragmentManager().findFragmentByTag(ContactInfoFragment.class.getName());
                if (frag != null) {
                    frag.bindHeaderData();
                }
            }
        }
    }

    private void showSimNotReadyToast(String accountType) {
        if (CommonUtilMethods.isSimAccount(accountType)) {
            Toast.makeText(getActivity(), String.format(getString(R.string.sim_not_ready), new Object[]{SimFactoryManager.getSimCardDisplayLabel(accountType)}), 0).show();
        }
    }

    public LoaderManager getLoaderManager() {
        return getActivity().getLoaderManager();
    }

    private void launchAddNewContact() {
        Activity activity = getActivity();
        Intent intent = new Intent("android.intent.action.INSERT", Contacts.CONTENT_URI);
        intent.putExtra("ViewDelayedLoadingSwitch", true);
        intent.setClass(activity, ContactEditorActivity.class);
        intent.putExtra("intent_extra_messenger", this.mMessenger);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            intent.putExtra("intent_key_is_from_default", true);
        }
        if (!EmuiFeatureManager.isUseCustAnimation() || this.mMenuAnimator == null || !this.isInPortrait || CommonUtilMethods.isSimplifiedModeEnabled()) {
            startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            if (this.mCreateNewContactsButton != null) {
                this.mCreateNewContactsButton.setClickable(false);
            }
        } else {
            enableEmptyButtons(false);
            startActivity(intent);
            activity.overridePendingTransition(0, 0);
        }
        if (this.mSplitActionBarView != null && CommonUtilMethods.calcIfNeedSplitScreen()) {
            this.mSplitActionBarView.setEnable(1, false);
        }
    }

    private void startEditorRelativeAnimation(boolean isAnimOut) {
        if (this.mMenuAnimator != null) {
            this.mMenuAnimator.play(isAnimOut);
        }
    }

    private void enableEmptyButtons(boolean enabled) {
        if (this.mCreateNewContactsButton != null) {
            this.mCreateNewContactsButton.setEnabled(enabled);
        }
        if (this.mViewCamcardButton != null) {
            this.mViewCamcardButton.setEnabled(enabled);
        }
        if (this.mSignInToAccountButton != null) {
            this.mSignInToAccountButton.setEnabled(enabled);
        }
        if (this.mImportContactsButton != null) {
            this.mImportContactsButton.setEnabled(enabled);
        }
    }

    protected boolean needSelectAllDrawable() {
        return false;
    }

    private void refreshHeaderViews() {
        for (View headerView : this.mHeaderViews) {
            RelativeLayout headerListItem = (RelativeLayout) headerView.findViewById(R.id.contact_list_item_header);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) headerListItem.getLayoutParams();
            View photoView = headerView.findViewById(R.id.photo);
            TextView nameView = (TextView) headerView.findViewById(R.id.name);
            MarginLayoutParams dividerLayoutParams = (MarginLayoutParams) headerView.findViewById(R.id.contact_list_group_item_view_divider).getLayoutParams();
            if (this.mIsSimpleDisplayMode) {
                photoView.setVisibility(8);
                int paddingStart = (getResources().getDimensionPixelSize(R.dimen.default_contactListItemView_text_adjust_start_padding) + getResources().getDimensionPixelSize(R.dimen.contact_list_item_alphtextheight_size)) - getResources().getDimensionPixelSize(R.dimen.default_contact_list_item_content_padding_left_simple);
                dividerLayoutParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.contact_list_divider_margin_start_Simple));
                nameView.setPaddingRelative(paddingStart, 0, 0, 0);
                headerListItem.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.list_item_single_line_height));
            } else {
                dividerLayoutParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.contact_list_divider_margin_start));
                photoView.setVisibility(0);
                nameView.setPaddingRelative(0, 0, 0, 0);
                headerListItem.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.list_item_double_line_height));
            }
            headerListItem.setLayoutParams(layoutParams);
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.isOnSaveInstance = false;
        AlertDialogFragmet alertFragment = (AlertDialogFragmet) getFragmentManager().findFragmentByTag("AlertDialogFragmet");
        if (alertFragment != null && alertFragment.mAlertDialogType == 3) {
            alertFragment.mSetedProfileListener = this.shareItemSelectListener;
        }
    }

    private void adjustButtonAndTextWidth(Button firstButton, Button secondButton, TextView textView, Context context) {
        if (firstButton != null && secondButton != null && context != null) {
            int w = MeasureSpec.makeMeasureSpec(0, 0);
            int h = MeasureSpec.makeMeasureSpec(0, 0);
            firstButton.measure(w, h);
            secondButton.measure(w, h);
            int firstButtonWidth = firstButton.getMeasuredWidth();
            int secondButtonWidth = secondButton.getMeasuredWidth();
            int priorityWidth = firstButtonWidth > secondButtonWidth ? firstButtonWidth : secondButtonWidth;
            int maxButtonWidth = (((context.getResources().getDisplayMetrics().widthPixels - context.getResources().getDimensionPixelOffset(R.dimen.contacts_birthday_to_calendar_textview_margin_start)) - context.getResources().getDimensionPixelOffset(R.dimen.contacts_birthday_to_calendar_textview_margin_end)) - context.getResources().getDimensionPixelOffset(R.dimen.cancel_button_margin_end)) / 2;
            if (priorityWidth > maxButtonWidth) {
                firstButton.setWidth(maxButtonWidth);
                secondButton.setWidth(maxButtonWidth);
            } else {
                firstButton.setWidth(priorityWidth);
                secondButton.setWidth(priorityWidth);
            }
            textView.setMaxWidth((context.getResources().getDisplayMetrics().widthPixels - context.getResources().getDimensionPixelOffset(R.dimen.contacts_birthday_to_calendar_textview_margin_start)) - context.getResources().getDimensionPixelOffset(R.dimen.contacts_birthday_to_calendar_textview_margin_end));
        }
    }

    private void showSyncBirthdayToCalandarView() {
        SharedPreferences sp = SharePreferenceUtil.getDefaultSp_de(getContext());
        Context context = getContext();
        if (sp.getBoolean("is_first_request_permission", true)) {
            if (RequestPermissionsActivityBase.hasPermissions(context, new String[]{"android.permission.READ_CALENDAR"})) {
                showBannersCalendarPermissionOpen(context);
                return;
            } else {
                showBannersCalendarPermissionClosed(context);
                return;
            }
        }
        if (this.mSyncBirthdayToCalendarView != null) {
            this.mSyncBirthdayToCalendarView.setVisibility(8);
        }
        if (this.mTellUserBirthdayToCalendarView != null) {
            this.mTellUserBirthdayToCalendarView.setVisibility(8);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (isAdded()) {
            switch (requestCode) {
                case 101:
                    if (permissions != null && permissions.length > 0) {
                        Editor editor = SharePreferenceUtil.getDefaultSp_de(getContext()).edit();
                        editor.putBoolean("is_first_request_permission", false);
                        editor.commit();
                        if (this.mSyncBirthdayToCalendarView != null) {
                            this.mSyncBirthdayToCalendarView.setVisibility(8);
                        }
                        if (this.mSearchEmptyView != null) {
                            setEmptyContactLocation(this.mSearchEmptyView.findViewById(R.id.online_search_empty_text));
                        }
                        for (int i = 0; i < permissions.length; i++) {
                            if (grantResults[i] != 0) {
                                try {
                                    startActivity(RequestPermissionsActivityBase.createRequestPermissionIntent(permissions, getContext().getPackageName()));
                                } catch (Exception e) {
                                    HwLog.e("DefaultContactBrowseListFragment", "Activity not find!");
                                }
                            }
                        }
                        break;
                    }
            }
        }
    }

    private void showBannersCalendarPermissionClosed(final Context context) {
        if (this.isInflate && this.mSyncBirthdayToCalendarViewStub != null) {
            this.mSyncBirthdayToCalendarView = this.mSyncBirthdayToCalendarViewStub.inflate();
            this.isInflate = false;
        }
        if (this.mTellUserBirthdayToCalendarView != null) {
            this.mTellUserBirthdayToCalendarView.setVisibility(8);
        }
        if (this.mSyncBirthdayToCalendarView != null) {
            this.mSyncBirthdayToCalendarView.setVisibility(0);
            Button cancelSendBtn = (Button) this.mSyncBirthdayToCalendarView.findViewById(R.id.cancel_send);
            Button allowSendBtn = (Button) this.mSyncBirthdayToCalendarView.findViewById(R.id.allow_send);
            adjustButtonAndTextWidth(cancelSendBtn, allowSendBtn, (TextView) this.mSyncBirthdayToCalendarView.findViewById(R.id.contacts_birthday_to_calendar_textview), context);
            cancelSendBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    Editor editor = SharePreferenceUtil.getDefaultSp_de(context).edit();
                    editor.putBoolean("is_first_request_permission", false);
                    editor.commit();
                    DefaultContactBrowseListFragment.this.mSyncBirthdayToCalendarView.setVisibility(8);
                    if (DefaultContactBrowseListFragment.this.mSearchEmptyView != null) {
                        DefaultContactBrowseListFragment.this.setEmptyContactLocation(DefaultContactBrowseListFragment.this.mSearchEmptyView.findViewById(R.id.online_search_empty_text));
                    }
                }
            });
            allowSendBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    DefaultContactBrowseListFragment.this.requestPermissions(new String[]{"android.permission.READ_CALENDAR"}, 101);
                }
            });
        }
    }

    private void showBannersCalendarPermissionOpen(final Context context) {
        if (this.isTellUserStubInflate && this.mTellUserBirthdayToCalendarViewStub != null) {
            this.mTellUserBirthdayToCalendarView = this.mTellUserBirthdayToCalendarViewStub.inflate();
            this.isTellUserStubInflate = false;
        }
        if (this.mSyncBirthdayToCalendarView != null) {
            this.mSyncBirthdayToCalendarView.setVisibility(8);
        }
        if (this.mTellUserBirthdayToCalendarView != null) {
            this.mTellUserBirthdayToCalendarView.setVisibility(0);
            ((ImageView) this.mTellUserBirthdayToCalendarView.findViewById(R.id.tell_user_birthday_to_calendar_cancel_iv)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    Editor editor = SharePreferenceUtil.getDefaultSp_de(context).edit();
                    editor.putBoolean("is_first_request_permission", false);
                    editor.commit();
                    DefaultContactBrowseListFragment.this.mTellUserBirthdayToCalendarView.setVisibility(8);
                    if (DefaultContactBrowseListFragment.this.mSearchEmptyView != null) {
                        DefaultContactBrowseListFragment.this.setEmptyContactLocation(DefaultContactBrowseListFragment.this.mSearchEmptyView.findViewById(R.id.online_search_empty_text));
                    }
                }
            });
            ((TextView) this.mTellUserBirthdayToCalendarView.findViewById(R.id.tell_user_birthday_to_calendar_textview)).setMaxWidth((((context.getResources().getDisplayMetrics().widthPixels - context.getResources().getDimensionPixelOffset(R.dimen.tell_user_birthday_to_calendar_text_margin_start)) - context.getResources().getDimensionPixelOffset(R.dimen.tell_user_birthday_to_calendar_text_margin_end)) - context.getResources().getDimensionPixelOffset(R.dimen.tell_user_birthday_to_calendar_button_margin_end)) - context.getResources().getDimensionPixelOffset(R.dimen.tell_user_birthday_to_calendar_button_width));
        }
    }

    private void hideBanners() {
        if (this.mSyncBirthdayToCalendarView != null) {
            this.mSyncBirthdayToCalendarView.setVisibility(8);
        }
        if (this.mTellUserBirthdayToCalendarView != null) {
            this.mTellUserBirthdayToCalendarView.setVisibility(8);
        }
    }
}
