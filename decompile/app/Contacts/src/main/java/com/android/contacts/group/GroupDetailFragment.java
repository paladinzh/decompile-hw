package com.android.contacts.group;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactSplitUtils;
import com.android.contacts.GroupMemberLoader;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactInfoFragment;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.activities.RequestPermissionsActivityBase;
import com.android.contacts.fragment.HwBaseFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.GroupAndContactMetaData;
import com.android.contacts.hap.GroupAndContactMetaData.GroupsData;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity.TranslucentActivity;
import com.android.contacts.hap.delete.ExtendedContactSaveService;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.utils.ActionBarTitle;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.hap.utils.ScreenUtils;
import com.android.contacts.hap.widget.SplitActionBarView;
import com.android.contacts.hap.widget.SplitActionBarView.OnCustomMenuListener;
import com.android.contacts.hap.widget.SplitActionBarView.SetButtonDetails;
import com.android.contacts.interactions.GroupDeletionDialogFragment;
import com.android.contacts.list.ContactTileAdapter$ContactEntry;
import com.android.contacts.list.ContactTileAdapter$DisplayType;
import com.android.contacts.list.GroupDetailBrowserAdapter;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.Constants;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.ContextMenuAdapter;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;

public class GroupDetailFragment extends HwBaseFragment implements OnCreateContextMenuListener, OnScrollListener, OnItemClickListener {
    private boolean bFromSmartGroupList = false;
    private int groupCount;
    private boolean isLoadFirst = false;
    private String mAccountName;
    private AccountTypeManager mAccountTypeManager;
    private String mAccountTypeString;
    private ActionBarTitle mActionBarTitle;
    private GroupDetailBrowserAdapter mAdapter;
    private boolean mCloseActivityAfterDelete;
    private Context mContext;
    private final ContextMenuAdapter mContextMenuAdapter = new ContextMenuAdapter() {
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            boolean z = true;
            ContactTileAdapter$ContactEntry entry = GroupDetailFragment.this.mAdapter.getItem(((AdapterContextMenuInfo) menuInfo).position - GroupDetailFragment.this.mMemberListView.getHeaderViewsCount());
            if (entry != null) {
                GroupDetailFragment groupDetailFragment = GroupDetailFragment.this;
                if (!(GroupDetailFragment.this.isGroupDeletable() && GroupDetailFragment.this.isVisible()) && (GroupDetailFragment.this.mAccountTypeString == null || !GroupDetailFragment.this.mAccountTypeString.contains("com.google") || GroupDetailFragment.this.isGroupDeletable())) {
                    z = false;
                }
                groupDetailFragment.mContextMenuGroupDeletable = z;
                Uri lookupUri = GroupDetailFragment.this.getAndUpdateLookupKey(entry);
                menu.setHeaderTitle(entry.name);
                if (GroupDetailFragment.this.isTargetAccountType(GroupDetailFragment.this.mAccountTypeString)) {
                    GroupDetailFragment.this.addContextMenu(menu, lookupUri, GroupDetailFragment.this.mContextMenuGroupDeletable);
                    return;
                }
                boolean isMenuVisible = GroupDetailFragment.this.mOptionsMenuGroupPresent && (GroupDetailFragment.this.mDataSet == null || !GroupDetailFragment.this.mIsReadOnly);
                GroupDetailFragment.this.addContextMenu(menu, lookupUri, isMenuVisible);
            }
        }

        public boolean onContextItemSelected(MenuItem item) {
            return false;
        }
    };
    private boolean mContextMenuGroupDeletable;
    private HwCustGroupDetailFragment mCust = null;
    private String mDataSet;
    private TextView mEmptyGroup;
    private View mEmptyView;
    private boolean mFling;
    private final Listener mFragmentListener = new Listener() {
        public void onGroupSizeUpdated(String size) {
            GroupDetailFragment.this.getActivity().getActionBar().setSubtitle(size);
        }

        public void onGroupTitleUpdated(String title) {
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                GroupDetailFragment.this.mActionBarTitle.setTitle(title);
            } else {
                GroupDetailFragment.this.getActivity().getActionBar().setTitle(title);
            }
        }

        public void onAccountTypeUpdated(String accountTypeString, String dataSet) {
            GroupDetailFragment.this.mAccountTypeString = accountTypeString;
            GroupDetailFragment.this.mDataSet = dataSet;
            GroupDetailFragment.this.getActivity().invalidateOptionsMenu();
        }

        public void onGroupDeleted() {
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                GroupDetailFragment.this.onBackPressed();
            }
        }
    };
    private long mGroupId;
    private boolean mGroupMemberDataLoaded = false;
    private final LoaderCallbacks<Cursor> mGroupMemberListLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            GroupDetailFragment.this.mGroupMemberDataLoaded = false;
            return GroupMemberLoader.constructLoaderForGroupDetailQuery(GroupDetailFragment.this.mContext, GroupDetailFragment.this.mGroupId);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                if (GroupDetailFragment.this.mAccountTypeString == null) {
                    GroupDetailFragment.this.isLoadFirst = true;
                    GroupDetailFragment.this.groupCount = data.getCount();
                } else {
                    GroupDetailFragment.this.updateSize(data.getCount());
                }
                GroupDetailFragment.this.mAdapter.setContactCursor(data);
                GroupDetailFragment.this.mGroupMemberDataLoaded = true;
                if (GroupDetailFragment.this.mProgressBar != null) {
                    GroupDetailFragment.this.mProgressBar.setVisibility(8);
                }
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    GroupDetailFragment.this.mSplitActionBarView.setVisibility(0);
                }
                GroupDetailFragment.this.setEmptyViewLocation();
                Activity lActivity = GroupDetailFragment.this.getActivity();
                if (lActivity != null) {
                    lActivity.invalidateOptionsMenu();
                }
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private final LoaderCallbacks<Cursor> mGroupMetadataLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            return new GroupMetaDataLoader(GroupDetailFragment.this.mContext, GroupDetailFragment.this.mGroupUri);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            boolean isStartGroupMembersLoader = true;
            if (data != null) {
                if (GroupDetailFragment.this.mPhotoManager != null) {
                    GroupDetailFragment.this.mPhotoManager.refreshCache();
                }
                data.moveToPosition(-1);
                if (data.moveToNext()) {
                    boolean deleted;
                    if (data.getInt(8) == 1) {
                        deleted = true;
                    } else {
                        deleted = false;
                    }
                    if (!deleted) {
                        long oldGroupId = GroupDetailFragment.this.mGroupId;
                        GroupDetailFragment.this.bindGroupMetaData(data);
                        if (GroupDetailFragment.this.isLoadFirst) {
                            GroupDetailFragment.this.isLoadFirst = false;
                            GroupDetailFragment.this.updateSize(GroupDetailFragment.this.groupCount);
                        }
                        if (oldGroupId == GroupDetailFragment.this.mGroupId) {
                            isStartGroupMembersLoader = false;
                        }
                        if (isStartGroupMembersLoader) {
                            GroupDetailFragment.this.startGroupMembersLoader();
                        }
                        return;
                    }
                }
                GroupDetailFragment.this.updateSize(-1);
                GroupDetailFragment.this.updateTitle(null);
                GroupDetailFragment.this.getActivity().finish();
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private String mGroupName;
    private TextView mGroupSize;
    private View mGroupSourceView;
    private ViewGroup mGroupSourceViewContainer;
    private View mGroupTitle;
    private String mGroupTitleStr = null;
    private int mGroupType = 0;
    private Uri mGroupUri;
    private boolean mIsReadOnly;
    private Listener mListener;
    private ListView mMemberListView;
    private Menu mOptionsMenu = null;
    private boolean mOptionsMenuGroupDeletable;
    private boolean mOptionsMenuGroupPresent;
    private ContactPhotoManager mPhotoManager;
    private int mPredefinedSmartGroupType;
    private View mProgressBar;
    private View mRootView;
    private boolean mShowGroupActionInActionBar;
    private final LoaderCallbacks<Cursor> mSmartGroupMemberListLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            return SmartGroupUtil.createSmartGroupLoader(GroupDetailFragment.this.mContext, GroupDetailFragment.this.mGroupUri);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                if (GroupDetailFragment.this.mPhotoManager != null) {
                    GroupDetailFragment.this.mPhotoManager.refreshCache();
                }
                GroupDetailFragment.this.updateSize(data.getCount());
                GroupDetailFragment.this.mAdapter.setContactCursor(data);
                if (GroupDetailFragment.this.mProgressBar != null) {
                    GroupDetailFragment.this.mProgressBar.setVisibility(8);
                }
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    if (data.getCount() > 0) {
                        GroupDetailFragment.this.mSplitActionBarView.setVisibility(0);
                    } else {
                        GroupDetailFragment.this.mSplitActionBarView.setVisibility(8);
                    }
                }
                GroupDetailFragment.this.setEmptyViewLocation();
                Activity lActivity = GroupDetailFragment.this.getActivity();
                if (lActivity != null) {
                    lActivity.invalidateOptionsMenu();
                }
                GroupDetailFragment.this.refreshListViewDelayed();
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private String[] mSmartGroupParameters;
    boolean removeItemVisible = false;
    MenuItem removeMember;

    public interface Listener {
        void onAccountTypeUpdated(String str, String str2);

        void onGroupDeleted();

        void onGroupSizeUpdated(String str);

        void onGroupTitleUpdated(String str);
    }

    public GroupDetailFragment() {
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustGroupDetailFragment) HwCustUtils.createObj(HwCustGroupDetailFragment.class, new Object[0]);
        }
    }

    public GroupDetailFragment(Intent intent) {
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustGroupDetailFragment) HwCustUtils.createObj(HwCustGroupDetailFragment.class, new Object[0]);
        }
        this.mGroupUri = intent.getData();
        this.bFromSmartGroupList = intent.getBooleanExtra("key_from_smart_group", false);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (CommonUtilMethods.calcIfNeedSplitScreen() && savedInstanceState == null && (getActivity() instanceof PeopleActivity)) {
            savedInstanceState = CommonUtilMethods.getInstanceState();
        }
        if (savedInstanceState != null) {
            this.mGroupUri = (Uri) savedInstanceState.getParcelable("group_uri");
            this.bFromSmartGroupList = savedInstanceState.getBoolean("key_from_smart_group");
        } else if (this.mGroupUri == null) {
            this.mGroupUri = getIntent().getData();
            this.bFromSmartGroupList = getIntent().getBooleanExtra("key_from_smart_group", false);
        }
        if (this.mGroupUri == null) {
            HwLog.e("GroupDetailFragment", "[ERROR] URI received is NULL");
            if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
                getActivity().finish();
                return;
            }
        }
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            this.mSmartGroupParameters = SmartGroupUtil.parseSmartGroupUri(this.mGroupUri);
            if (this.mSmartGroupParameters.length != 0) {
                this.mAdapter = new GroupDetailBrowserAdapter(this.mContext, ContactTileAdapter$DisplayType.SMART_GROUP_ONLY);
            } else {
                this.mAdapter = new GroupDetailBrowserAdapter(this.mContext, ContactTileAdapter$DisplayType.GROUP_MEMBERS);
            }
            configurePhotoLoader();
        }
    }

    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        return ContactSplitUtils.createSplitAnimator(transit, enter, nextAnim, this.mRootView, R.drawable.multiselection_background, getActivity());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mSmartGroupParameters.length != 0) {
            this.mGroupTitleStr = this.mSmartGroupParameters[1];
            this.mPredefinedSmartGroupType = Integer.parseInt(this.mSmartGroupParameters[2]);
            if ("smart_groups_company".equals(this.mSmartGroupParameters[0])) {
                StatisticalHelper.report(1192);
            }
            if ("smart_groups_location".equals(this.mSmartGroupParameters[0])) {
                StatisticalHelper.report(1193);
            }
        }
        loadGroup(this.mGroupUri);
        setListener(this.mFragmentListener);
        this.mShowGroupActionInActionBar = getActivity().getResources().getBoolean(R.bool.config_show_group_action_in_action_bar);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            closeActivityAfterDelete(false);
        } else {
            closeActivityAfterDelete(true);
        }
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            ActionBar actionBar = getActivity().getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                if (Constants.STRRED_URL.equals(this.mGroupUri)) {
                    actionBar.setTitle(R.string.contacts_starred);
                } else if (this.mGroupTitleStr != null) {
                    setActionBarTitleForSmartGroup(actionBar);
                }
            }
        } else if (this.mActionBarTitle != null) {
            if (Constants.STRRED_URL.equals(this.mGroupUri)) {
                this.mActionBarTitle.setTitle(getString(R.string.contacts_starred));
            } else if (this.mGroupTitleStr != null) {
                setActionBarTitleForSmartGroup(this.mActionBarTitle);
            }
        }
        ExceptionCapture.reportScene(56);
    }

    private void setActionBarTitleForSmartGroup(ActionBarTitle actionBar) {
        switch (this.mPredefinedSmartGroupType) {
            case -1:
                actionBar.setTitle(this.mGroupTitleStr);
                return;
            case 1:
                actionBar.setTitle(getString(R.string.contacts_company_null));
                return;
            case 2:
                actionBar.setTitle(getString(R.string.contacts_location_null));
                return;
            case 3:
                actionBar.setTitle(getString(R.string.contacts_within_one_week, new Object[]{Integer.valueOf(1)}));
                return;
            case 4:
                actionBar.setTitle(getString(R.string.contacts_within_one_month, new Object[]{Integer.valueOf(1), Integer.valueOf(1)}));
                return;
            case 5:
                actionBar.setTitle(getString(R.string.contacts_within_three_months, new Object[]{Integer.valueOf(3), Integer.valueOf(1)}));
                return;
            case 6:
                actionBar.setTitle(getString(R.string.contacts_contact_over_three_month, new Object[]{Integer.valueOf(3)}));
                return;
            default:
                return;
        }
    }

    private void setActionBarTitleForSmartGroup(ActionBar actionBar) {
        switch (this.mPredefinedSmartGroupType) {
            case -1:
                actionBar.setTitle(this.mGroupTitleStr);
                return;
            case 1:
                actionBar.setTitle(R.string.contacts_company_null);
                return;
            case 2:
                actionBar.setTitle(R.string.contacts_location_null);
                return;
            case 3:
                actionBar.setTitle(getString(R.string.contacts_within_one_week, new Object[]{Integer.valueOf(1)}));
                return;
            case 4:
                actionBar.setTitle(getString(R.string.contacts_within_one_month, new Object[]{Integer.valueOf(1), Integer.valueOf(1)}));
                return;
            case 5:
                actionBar.setTitle(getString(R.string.contacts_within_three_months, new Object[]{Integer.valueOf(3), Integer.valueOf(1)}));
                return;
            case 6:
                actionBar.setTitle(getString(R.string.contacts_contact_over_three_month, new Object[]{Integer.valueOf(3)}));
                return;
            default:
                return;
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
        this.mAccountTypeManager = AccountTypeManager.getInstance(this.mContext);
        if (!(activity == null || CommonUtilMethods.calcIfNeedSplitScreen())) {
            if (this.mGroupUri != null) {
                this.mSmartGroupParameters = SmartGroupUtil.parseSmartGroupUri(this.mGroupUri);
            } else if (activity.getIntent() != null) {
                this.mSmartGroupParameters = SmartGroupUtil.parseSmartGroupUri(activity.getIntent().getData());
            }
            if (this.mSmartGroupParameters.length != 0) {
                this.mAdapter = new GroupDetailBrowserAdapter(activity, ContactTileAdapter$DisplayType.SMART_GROUP_ONLY);
            } else {
                this.mAdapter = new GroupDetailBrowserAdapter(activity, ContactTileAdapter$DisplayType.GROUP_MEMBERS);
            }
        }
        configurePhotoLoader();
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.removeMember != null) {
            outState.putBoolean("key_removeiten_visible", this.removeMember.isVisible());
        }
        outState.putParcelable("group_uri", this.mGroupUri);
        outState.putBoolean("key_from_smart_group", this.bFromSmartGroupList);
        super.onSaveInstanceState(outState);
    }

    public void onDetach() {
        super.onDetach();
        this.mContext = null;
    }

    public void onStop() {
        super.onStop();
        if (this.mOptionsMenu != null) {
            this.mOptionsMenu.close();
        }
    }

    private void addContextMenu(ContextMenu menu, final Uri lookupUri, boolean aVisibile) {
        MenuItem removeFromListItem = menu.add(R.string.str_remove_from_group);
        removeFromListItem.setVisible(aVisibile);
        removeFromListItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (!TextUtils.isEmpty(lookupUri.getLastPathSegment())) {
                    GroupDetailFragment.this.mContext.startService(ExtendedContactSaveService.createAddAndRemoveMembersToGroupIntent(GroupDetailFragment.this.mContext, new GroupAndContactMetaData(new long[]{Long.parseLong(lookupUri.getLastPathSegment())}, null, new GroupsData(GroupDetailFragment.this.mGroupId, GroupDetailFragment.this.mAccountName, GroupDetailFragment.this.mAccountTypeString, null)), true));
                }
                return true;
            }
        });
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && savedState == null && (getActivity() instanceof PeopleActivity)) {
            savedState = CommonUtilMethods.getInstanceState();
        }
        setHasOptionsMenu(true);
        if (savedState != null) {
            this.removeItemVisible = savedState.getBoolean("key_removeiten_visible", false);
        }
        this.mRootView = inflater.inflate(R.layout.group_detail_fragment, container, false);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            this.mActionBarTitle = new ActionBarTitle(this.mContext, this.mRootView.findViewById(R.id.edit_layout));
            this.mRootView.findViewById(R.id.edit_layout).setVisibility(0);
            this.mActionBarTitle.setBackIcon(true, null, new OnClickListener() {
                public void onClick(View v) {
                    GroupDetailFragment.this.getActivity().onBackPressed();
                }
            });
            if (getActivity() instanceof PeopleActivity) {
                PeopleActivity act = (PeopleActivity) getActivity();
                ScreenUtils.adjustPaddingTop(act, (ViewGroup) this.mRootView.findViewById(R.id.group_detail), ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode()));
            }
        } else {
            this.mRootView.findViewById(R.id.edit_layout).setVisibility(8);
        }
        this.mGroupSourceViewContainer = (ViewGroup) this.mRootView.findViewById(R.id.group_source_view_container);
        this.mProgressBar = this.mRootView.findViewById(R.id.progressContainer);
        this.mProgressBar.setVisibility(0);
        this.mEmptyGroup = (TextView) this.mRootView.findViewById(R.id.empty_Group);
        this.mEmptyGroup.setText(R.string.emptyGroup);
        this.mEmptyView = this.mRootView.findViewById(16908292);
        this.mMemberListView = (ListView) this.mRootView.findViewById(16908298);
        this.mMemberListView.setFastScrollEnabled(true);
        this.mGroupTitle = inflater.inflate(R.layout.group_browse_list_account_header, this.mMemberListView, false);
        this.mGroupSize = (TextView) this.mGroupTitle.findViewById(R.id.account_type);
        this.mGroupTitle.findViewById(R.id.header_divider_top).setVisibility(8);
        this.mMemberListView.setAdapter(this.mAdapter);
        this.mMemberListView.setOnScrollListener(this);
        this.mMemberListView.requestLayout();
        this.mMemberListView.setOnItemClickListener(this);
        this.mMemberListView.setOnCreateContextMenuListener(this.mContextMenuAdapter);
        CommonUtilMethods.addFootEmptyViewPortrait(this.mMemberListView, this.mContext);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            createOrRefreshSplitActionBar();
            this.mSplitActionBarView.setOnCustomMenuListener(new OnCustomMenuListener() {
                public boolean onCustomSplitMenuItemClick(int aMenuItem) {
                    Uri uri;
                    switch (aMenuItem) {
                        case R.string.delete_group_dialog_title:
                            GroupDeletionDialogFragment.show(GroupDetailFragment.this.getFragmentManager(), GroupDetailFragment.this.mGroupId, GroupDetailFragment.this.mGroupName, GroupDetailFragment.this.mCloseActivityAfterDelete, GroupDetailFragment.this.mListener);
                            ExceptionCapture.reportScene(19);
                            return true;
                        case R.string.menu_add_group_members_action_bar:
                            StatisticalHelper.report(1149);
                            GroupDetailFragment.this.startAddGroupMembersIntent();
                            ExceptionCapture.reportScene(17);
                            return true;
                        case R.string.contact_menu_add_company_members:
                            GroupDetailFragment.this.launchAddCompanyMembers();
                            return true;
                        case R.string.menu_group_send_mail:
                            if (GroupDetailFragment.this.mSmartGroupParameters.length != 0) {
                                uri = GroupDetailFragment.this.mGroupUri;
                            } else {
                                uri = ContentUris.withAppendedId(Groups.CONTENT_URI, GroupDetailFragment.this.mGroupId);
                            }
                            Intent lIntent = new Intent("android.intent.action.HAP_SEND_GROUP_MAIL");
                            GroupDetailFragment.this.setWindowWidthForIntent(lIntent);
                            lIntent.putExtra("extra_group_uri", uri);
                            lIntent.putExtra("com.huawei.community.action.MAX_SELECT_COUNT", VTMCDataCache.MAXSIZE);
                            GroupDetailFragment.this.startActivityForSplit(lIntent);
                            if (GroupDetailFragment.this.mGroupType == 1) {
                                StatisticalHelper.report(1142);
                            }
                            ExceptionCapture.reportScene(21);
                            return true;
                        case R.string.menu_group_send_message:
                            if (GroupDetailFragment.this.mSmartGroupParameters.length != 0) {
                                uri = GroupDetailFragment.this.mGroupUri;
                            } else {
                                uri = ContentUris.withAppendedId(Groups.CONTENT_URI, GroupDetailFragment.this.mGroupId);
                            }
                            GroupDetailFragment.this.sendSmsMessage(uri);
                            return true;
                        case R.string.contacts_group_rename:
                            GroupDetailFragment.this.renameGroup(ContentUris.withAppendedId(Groups.CONTENT_URI, GroupDetailFragment.this.mGroupId));
                            ExceptionCapture.reportScene(18);
                            return true;
                        default:
                            return false;
                    }
                }

                public boolean onCustomMenuItemClick(MenuItem aMenuItem) {
                    return GroupDetailFragment.this.onOptionsItemSelected(aMenuItem);
                }

                public void onPrepareOptionsMenu(Menu aMenu) {
                    boolean z = true;
                    boolean z2 = false;
                    GroupDetailFragment.this.mOptionsMenuGroupDeletable = GroupDetailFragment.this.isGroupDeletable();
                    GroupDetailFragment.this.mOptionsMenuGroupPresent = GroupDetailFragment.this.isGroupPresent();
                    GroupDetailFragment.this.removeMember = aMenu.findItem(R.id.menu_remove_member);
                    MenuItem deleteMenu = aMenu.findItem(R.id.sub_menu_delete_group);
                    MenuItem renameGroup = aMenu.findItem(R.id.sub_menu_rename_group);
                    boolean z3;
                    if (GroupDetailFragment.this.isTargetAccountType(GroupDetailFragment.this.mAccountTypeString)) {
                        renameGroup.setVisible(GroupDetailFragment.this.mGroupMemberDataLoaded ? GroupDetailFragment.this.mOptionsMenuGroupDeletable : false);
                        MenuItem menuItem = GroupDetailFragment.this.removeMember;
                        if (!GroupDetailFragment.this.mGroupMemberDataLoaded || (!GroupDetailFragment.this.removeItemVisible && (!GroupDetailFragment.this.mOptionsMenuGroupDeletable || GroupDetailFragment.this.mAdapter.getCount() <= 0))) {
                            z3 = false;
                        } else {
                            z3 = true;
                        }
                        menuItem.setVisible(z3);
                    } else {
                        boolean isMenuVisible = GroupDetailFragment.this.mOptionsMenuGroupPresent && (GroupDetailFragment.this.mDataSet == null || !GroupDetailFragment.this.mIsReadOnly);
                        if (GroupDetailFragment.this.mGroupMemberDataLoaded) {
                            z3 = isMenuVisible;
                        } else {
                            z3 = false;
                        }
                        renameGroup.setVisible(z3);
                        MenuItem menuItem2 = GroupDetailFragment.this.removeMember;
                        if (!GroupDetailFragment.this.mGroupMemberDataLoaded || (!GroupDetailFragment.this.removeItemVisible && (!isMenuVisible || GroupDetailFragment.this.mAdapter.getCount() <= 0))) {
                            z = false;
                        }
                        menuItem2.setVisible(z);
                    }
                    if (GroupDetailFragment.this.mGroupMemberDataLoaded) {
                        z2 = GroupDetailFragment.this.mOptionsMenuGroupDeletable;
                    }
                    deleteMenu.setVisible(z2);
                    if (GroupDetailFragment.this.mCust != null) {
                        GroupDetailFragment.this.mCust.customizeOptionsMenu(aMenu, GroupDetailFragment.this.mIsReadOnly, GroupDetailFragment.this.mAccountTypeString);
                    }
                }
            });
        }
        return this.mRootView;
    }

    private void createOrRefreshSplitActionBar() {
        ArrayList<SetButtonDetails> detailsAll = new ArrayList();
        SetButtonDetails menuAddGroupMember = new SetButtonDetails(R.drawable.ic_new_contact, R.string.menu_add_group_members_action_bar);
        SetButtonDetails menuAddCompanyMember = new SetButtonDetails(R.drawable.ic_new_contact, R.string.contact_menu_add_company_members);
        SetButtonDetails setButtonDetails = new SetButtonDetails(R.drawable.ic_portrait_message, R.string.menu_group_send_message);
        setButtonDetails = new SetButtonDetails(R.drawable.ic_portrait_mail, R.string.menu_group_send_mail);
        SetButtonDetails menuMore = new SetButtonDetails(R.menu.view_group_option, R.string.contacts_title_menu);
        SetButtonDetails menuRenameGroup = new SetButtonDetails(R.drawable.ic_rename, R.string.contacts_group_rename);
        SetButtonDetails menuDelGroup = new SetButtonDetails(R.drawable.ic_trash_normal, R.string.delete_group_dialog_title);
        boolean enableSendButton = this.mAdapter.getCount() > 0;
        this.mOptionsMenuGroupDeletable = isGroupDeletable();
        this.mOptionsMenuGroupPresent = isGroupPresent();
        boolean isMenuVisible = this.mOptionsMenuGroupPresent && (this.mDataSet == null || !this.mIsReadOnly);
        boolean removeMemberVisible = false;
        boolean z = this.mGroupMemberDataLoaded ? this.mOptionsMenuGroupDeletable : false;
        boolean renameGroupVisible = false;
        if (isTargetAccountType(this.mAccountTypeString)) {
            if (this.mGroupMemberDataLoaded && this.mOptionsMenuGroupDeletable) {
                detailsAll.add(menuAddGroupMember);
            }
            removeMemberVisible = this.mGroupMemberDataLoaded && (this.removeItemVisible || (this.mOptionsMenuGroupDeletable && this.mAdapter.getCount() > 0));
            renameGroupVisible = this.mGroupMemberDataLoaded ? this.mOptionsMenuGroupDeletable : false;
        } else if (this.mGroupMemberDataLoaded) {
            if (isMenuVisible) {
                detailsAll.add(menuAddGroupMember);
            }
            removeMemberVisible = this.removeItemVisible || (isMenuVisible && this.mAdapter.getCount() > 0);
            renameGroupVisible = this.mGroupMemberDataLoaded ? isMenuVisible : false;
        }
        if (enableSendButton) {
            if (this.mSmartGroupParameters.length != 0 && "smart_groups_company".equals(this.mSmartGroupParameters[0])) {
                detailsAll.add(menuAddCompanyMember);
            }
            if (EmuiFeatureManager.isSystemSMSCapable() && (this.mOptionsMenuGroupPresent || this.mSmartGroupParameters.length != 0)) {
                detailsAll.add(setButtonDetails);
            }
            if ((this.mOptionsMenuGroupPresent && CommonUtilMethods.isGroupEmailSupported(this.mAccountTypeString, this.mDataSet, this.mContext)) || this.mSmartGroupParameters.length != 0) {
                detailsAll.add(setButtonDetails);
            }
            if (removeMemberVisible || z || r19) {
                detailsAll.add(menuMore);
            }
        } else if (this.mGroupMemberDataLoaded) {
            if (isTargetAccountType(this.mAccountTypeString)) {
                if (this.mOptionsMenuGroupDeletable) {
                    detailsAll.add(menuRenameGroup);
                }
            } else if (isMenuVisible) {
                detailsAll.add(menuRenameGroup);
            }
            if (this.mOptionsMenuGroupDeletable) {
                detailsAll.add(menuDelGroup);
            }
        }
        int size = detailsAll.size();
        SetButtonDetails setButtonDetails2 = size > 0 ? (SetButtonDetails) detailsAll.get(0) : null;
        SetButtonDetails setButtonDetails3 = size > 1 ? (SetButtonDetails) detailsAll.get(size - 1) : null;
        SetButtonDetails setButtonDetails4 = size > 2 ? (SetButtonDetails) detailsAll.get(size - 2) : null;
        SetButtonDetails setButtonDetails5 = size > 3 ? (SetButtonDetails) detailsAll.get(size - 3) : null;
        if (this.mSplitActionBarView == null) {
            this.mSplitActionBarView = (SplitActionBarView) this.mRootView.findViewById(R.id.menu_view);
            this.mSplitActionBarView.setVisibility(0);
            this.mSplitActionBarView.fillDetails(setButtonDetails2, setButtonDetails5, setButtonDetails4, setButtonDetails3, detailsAll.contains(menuMore));
        } else {
            this.mSplitActionBarView.reset();
            this.mSplitActionBarView.fillDetails(setButtonDetails2, setButtonDetails5, setButtonDetails4, setButtonDetails3, detailsAll.contains(menuMore));
        }
        this.mSplitActionBarView.setEnable(detailsAll.indexOf(setButtonDetails) + 1, enableSendButton);
        this.mSplitActionBarView.setEnable(detailsAll.indexOf(setButtonDetails) + 1, enableSendButton);
    }

    public void onResume() {
        super.onResume();
        if (this.mAdapter != null) {
            this.mAdapter.upateSimpleDisplayMode();
        }
        this.mPhotoManager.refreshCache();
    }

    public void loadGroup(Uri groupUri) {
        if (groupUri != null) {
            if (SmartGroupUtil.parseSmartGroupUri(groupUri).length != 0) {
                this.mGroupUri = groupUri;
                this.mGroupType = 1;
                startSmartGroupMetadataLoader();
            } else {
                this.mGroupType = 0;
                this.mGroupUri = groupUri;
                startGroupMetadataLoader();
                try {
                    this.mGroupId = Long.parseLong(groupUri.getLastPathSegment());
                    startGroupMembersLoader();
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    private void configurePhotoLoader() {
        if (this.mContext != null) {
            if (this.mPhotoManager == null) {
                this.mPhotoManager = ContactPhotoManager.getInstance(this.mContext);
            }
            if (this.mAdapter != null) {
                this.mAdapter.setPhotoLoader(this.mPhotoManager);
            }
        }
    }

    public void setListener(Listener value) {
        this.mListener = value;
    }

    private void startSmartGroupMetadataLoader() {
        getLoaderManager().restartLoader(3, new Bundle(), this.mSmartGroupMemberListLoaderListener);
    }

    private void refreshListViewDelayed() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (GroupDetailFragment.this.mAdapter != null) {
                    GroupDetailFragment.this.mAdapter.notifyDataSetChanged();
                }
            }
        }, 50);
    }

    private void startGroupMetadataLoader() {
        if (this.mGroupUri != null) {
            getLoaderManager().restartLoader(0, new Bundle(), this.mGroupMetadataLoaderListener);
        }
    }

    private void startGroupMembersLoader() {
        getLoaderManager().restartLoader(1, new Bundle(), this.mGroupMemberListLoaderListener);
    }

    private void setEmptyViewLocation() {
        boolean isPor = true;
        Activity activity = getActivity();
        if (activity != null) {
            if (getResources().getConfiguration().orientation != 1) {
                isPor = false;
            }
            if (isPor) {
                MarginLayoutParams params = (MarginLayoutParams) this.mEmptyGroup.getLayoutParams();
                params.topMargin = CommonUtilMethods.getMarginTopPix(activity, 0.3d, isPor);
                this.mEmptyGroup.setLayoutParams(params);
            } else if (this.mEmptyView instanceof LinearLayout) {
                int paddingBottom = CommonUtilMethods.getActionBarAndStatusHeight(activity, isPor);
                ((LinearLayout) this.mEmptyView).setGravity(17);
                this.mEmptyGroup.setPadding(0, 0, 0, paddingBottom);
            }
        }
        this.mMemberListView.setEmptyView(this.mEmptyView);
    }

    private void bindGroupMetaData(Cursor cursor) {
        boolean z = false;
        cursor.moveToPosition(-1);
        if (cursor.moveToNext()) {
            this.mAccountName = cursor.getString(0);
            this.mAccountTypeString = cursor.getString(1);
            this.mDataSet = cursor.getString(2);
            this.mGroupId = cursor.getLong(3);
            this.mGroupName = cursor.getString(4);
            if (cursor.getInt(7) == 1) {
                z = true;
            }
            this.mIsReadOnly = z;
            this.mGroupName = CommonUtilMethods.parseGroupDisplayName(this.mAccountTypeString, this.mGroupName, this.mContext, cursor.getString(9), cursor.getInt(10), cursor.getString(11));
            updateTitle(this.mGroupName);
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
            updateAccountType(cursor.getString(1), cursor.getString(2));
        }
    }

    private void updateTitle(String title) {
        this.mListener.onGroupTitleUpdated(title);
    }

    private void updateSize(int size) {
        String groupSizeString = null;
        if (size <= 0 || this.mSmartGroupParameters.length != 0) {
            if (!(this.mGroupSize == null || this.mGroupTitle == null || this.mMemberListView == null || this.mMemberListView.getHeaderViewsCount() <= 0)) {
                this.mMemberListView.removeHeaderView(this.mGroupTitle);
            }
        } else if (!(this.mGroupSize == null || this.mGroupTitle == null || this.mMemberListView == null || this.mMemberListView.getHeaderViewsCount() >= 1)) {
            this.mMemberListView.addHeaderView(this.mGroupTitle);
        }
        if (size == -1) {
            groupSizeString = null;
        } else {
            String groupSizeTemplateString = getResources().getQuantityString(R.plurals.num_contacts_in_group, size);
            AccountType accountType = this.mAccountTypeManager.getAccountType(this.mAccountTypeString, this.mDataSet);
            if (accountType != null) {
                if ("com.android.huawei.phone".equals(accountType.accountType) && getResources().getBoolean(R.bool.config_check_Russian_Grammar)) {
                    groupSizeString = String.format(groupSizeTemplateString, new Object[]{Integer.valueOf(size), getString(R.string.phoneLabelsGroup_from)});
                } else if (CommonUtilMethods.isSimAccount(this.mAccountTypeString)) {
                    groupSizeString = String.format(groupSizeTemplateString, new Object[]{Integer.valueOf(size), SimFactoryManager.getSimCardDisplayLabel(this.mAccountTypeString)});
                } else {
                    groupSizeString = String.format(groupSizeTemplateString, new Object[]{Integer.valueOf(size), accountType.getDisplayLabel(this.mContext)});
                }
            }
        }
        if (this.mGroupSize != null) {
            this.mGroupSize.setText(CommonUtilMethods.upPercase(groupSizeString));
        } else {
            this.mListener.onGroupSizeUpdated(groupSizeString);
        }
    }

    private void updateAccountType(String accountTypeString, String dataSet) {
        if (this.mShowGroupActionInActionBar) {
            this.mListener.onAccountTypeUpdated(accountTypeString, dataSet);
            return;
        }
        final AccountType accountType = AccountTypeManager.getInstance(getActivity()).getAccountType(accountTypeString, dataSet);
        if (!TextUtils.isEmpty(accountType.getViewGroupActivity())) {
            if (this.mGroupSourceView == null) {
                this.mGroupSourceView = GroupDetailDisplayUtils.getNewGroupSourceView(this.mContext);
                if (this.mGroupSourceViewContainer != null) {
                    this.mGroupSourceViewContainer.addView(this.mGroupSourceView);
                }
            }
            this.mGroupSourceView.setVisibility(0);
            GroupDetailDisplayUtils.bindGroupSourceView(this.mContext, this.mGroupSourceView, accountTypeString, dataSet);
            this.mGroupSourceView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent("android.intent.action.VIEW", ContentUris.withAppendedId(Groups.CONTENT_URI, GroupDetailFragment.this.mGroupId));
                    intent.setClassName(accountType.syncAdapterPackageName, accountType.getViewGroupActivity());
                    GroupDetailFragment.this.startActivity(intent);
                }
            });
        } else if (this.mGroupSourceView != null) {
            this.mGroupSourceView.setVisibility(8);
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        boolean lPrevFling = this.mFling;
        this.mFling = scrollState == 2;
        if (this.mFling) {
            this.mPhotoManager.pause();
        } else {
            this.mPhotoManager.resume();
        }
        if (this.mAdapter != null) {
            this.mAdapter.setIsListInFlingState(this.mFling);
            if (!this.mFling && lPrevFling) {
                this.mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            inflater.inflate(R.menu.view_group, menu);
            this.mOptionsMenu = menu;
            if (this.mContext.getResources().getConfiguration().orientation == 2) {
                ImmersionUtils.setImmersionMommonMenu(this.mContext, menu.findItem(R.id.menu_add_group_members));
                ImmersionUtils.setImmersionMommonMenu(this.mContext, menu.findItem(R.id.menu_send_group_message));
                ImmersionUtils.setImmersionMommonMenu(this.mContext, menu.findItem(R.id.menu_send_group_mail));
                ImmersionUtils.setImmersionMommonMenu(this.mContext, menu.findItem(R.id.menu_delete_group));
                ImmersionUtils.setImmersionMommonMenu(this.mContext, menu.findItem(R.id.menu_nomembers_add_group_members));
                ImmersionUtils.setImmersionMommonMenu(this.mContext, menu.findItem(R.id.menu_rename_group));
                ImmersionUtils.setImmersionMommonMenu(this.mContext, menu.findItem(R.id.overflow_menu_group_detail));
                ImmersionUtils.setImmersionMommonMenu(this.mContext, menu.findItem(R.id.menu_add_company_members));
            }
            ViewUtil.setMenuItemStateListIcon(this.mContext, menu.findItem(R.id.overflow_menu_group_detail));
            ViewUtil.setMenuItemStateListIcon(this.mContext, menu.findItem(R.id.menu_add_group_members));
            ViewUtil.setMenuItemStateListIcon(this.mContext, menu.findItem(R.id.menu_send_group_message));
            ViewUtil.setMenuItemStateListIcon(this.mContext, menu.findItem(R.id.menu_send_group_mail));
            ViewUtil.setMenuItemStateListIcon(this.mContext, menu.findItem(R.id.menu_delete_group));
            ViewUtil.setMenuItemStateListIcon(this.mContext, menu.findItem(R.id.menu_nomembers_add_group_members));
            ViewUtil.setMenuItemStateListIcon(this.mContext, menu.findItem(R.id.menu_rename_group));
            ViewUtil.setMenuItemStateListIcon(this.mContext, menu.findItem(R.id.menu_add_company_members));
        }
    }

    public boolean isGroupDeletable() {
        return (this.mGroupUri == null || this.mIsReadOnly || this.mGroupId <= 0) ? false : true;
    }

    public boolean isGroupPresent() {
        return this.mGroupUri != null && this.mGroupId > 0;
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            createOrRefreshSplitActionBar();
            return;
        }
        MenuItem addMembersMenu;
        MenuItem deleteMenu;
        MenuItem renameGroup;
        this.mOptionsMenuGroupDeletable = isGroupDeletable() ? isVisible() : false;
        this.mOptionsMenuGroupPresent = isGroupPresent() ? isVisible() : false;
        boolean enableSendButton = this.mAdapter.getCount() > 0;
        MenuItem sendMessagesMenu = menu.findItem(R.id.menu_send_group_message);
        boolean z = (this.mOptionsMenuGroupPresent && enableSendButton) ? true : this.mSmartGroupParameters.length != 0;
        sendMessagesMenu.setVisible(z);
        MenuItem sendMailMenu = menu.findItem(R.id.menu_send_group_mail);
        z = (this.mOptionsMenuGroupPresent && CommonUtilMethods.isGroupEmailSupported(this.mAccountTypeString, this.mDataSet, this.mContext) && enableSendButton) ? true : this.mSmartGroupParameters.length != 0;
        sendMailMenu.setVisible(z);
        sendMessagesMenu.setEnabled(enableSendButton);
        sendMailMenu.setEnabled(enableSendButton);
        this.removeMember = menu.findItem(R.id.menu_remove_member);
        if (enableSendButton) {
            menu.setGroupVisible(R.id.menu_hasmembers_group, true);
            menu.setGroupVisible(R.id.menu_nomembers_group, false);
            addMembersMenu = menu.findItem(R.id.menu_add_group_members);
            deleteMenu = menu.findItem(R.id.sub_menu_delete_group);
            renameGroup = menu.findItem(R.id.sub_menu_rename_group);
        } else {
            menu.setGroupVisible(R.id.menu_hasmembers_group, false);
            if (this.mGroupMemberDataLoaded) {
                menu.setGroupVisible(R.id.menu_nomembers_group, true);
            } else {
                menu.setGroupVisible(R.id.menu_nomembers_group, false);
            }
            addMembersMenu = menu.findItem(R.id.menu_nomembers_add_group_members);
            deleteMenu = menu.findItem(R.id.menu_delete_group);
            renameGroup = menu.findItem(R.id.menu_rename_group);
        }
        if (addMembersMenu != null) {
            addMembersMenu.setVisible(this.mGroupMemberDataLoaded);
        }
        MenuItem menuItem;
        if (isTargetAccountType(this.mAccountTypeString)) {
            if (addMembersMenu != null) {
                addMembersMenu.setVisible(this.mGroupMemberDataLoaded ? this.mOptionsMenuGroupDeletable : false);
            }
            renameGroup.setVisible(this.mGroupMemberDataLoaded ? this.mOptionsMenuGroupDeletable : false);
            menuItem = this.removeMember;
            z = this.mGroupMemberDataLoaded && (this.removeItemVisible || (this.mOptionsMenuGroupDeletable && this.mAdapter.getCount() > 0));
            menuItem.setVisible(z);
        } else {
            boolean isMenuVisible = this.mOptionsMenuGroupPresent && (this.mDataSet == null || !this.mIsReadOnly);
            if (addMembersMenu != null) {
                addMembersMenu.setVisible(this.mGroupMemberDataLoaded ? isMenuVisible : false);
            }
            renameGroup.setVisible(this.mGroupMemberDataLoaded ? isMenuVisible : false);
            menuItem = this.removeMember;
            if (!this.mGroupMemberDataLoaded || (!this.removeItemVisible && (!isMenuVisible || this.mAdapter.getCount() <= 0))) {
                z = false;
            } else {
                z = true;
            }
            menuItem.setVisible(z);
        }
        deleteMenu.setVisible(this.mGroupMemberDataLoaded ? this.mOptionsMenuGroupDeletable : false);
        MenuItem overflow = menu.findItem(R.id.overflow_menu_group_detail);
        if (enableSendButton) {
            z = (this.removeMember.isVisible() || deleteMenu.isVisible()) ? true : renameGroup.isVisible();
            overflow.setVisible(z);
        }
        MenuItem addCompanyMembers = menu.findItem(R.id.menu_add_company_members);
        if (this.mSmartGroupParameters.length != 0) {
            z = "smart_groups_company".equals(this.mSmartGroupParameters[0]);
        } else {
            z = false;
        }
        addCompanyMembers.setVisible(z);
        if (this.mCust != null) {
            this.mCust.customizeOptionsMenu(menu, this.mIsReadOnly, this.mAccountTypeString);
        }
        if (!EmuiFeatureManager.isSystemSMSCapable()) {
            sendMessagesMenu.setVisible(false);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Uri uri;
        Intent lIntent;
        switch (item.getItemId()) {
            case R.id.menu_remove_member:
                uri = ContentUris.withAppendedId(Groups.CONTENT_URI, this.mGroupId);
                lIntent = new Intent("android.intent.action.HAP_REMOVE_GROUP_MEMBERS");
                setWindowWidthForIntent(lIntent);
                lIntent.putExtra("extra_group_uri", uri);
                lIntent.putExtra("com.huawei.community.action.MAX_SELECT_COUNT", 100);
                startActivityForSplit(lIntent);
                ExceptionCapture.reportScene(22);
                return true;
            case R.id.menu_add_group_members:
            case R.id.menu_nomembers_add_group_members:
                StatisticalHelper.report(1149);
                startAddGroupMembersIntent();
                ExceptionCapture.reportScene(17);
                return true;
            case R.id.menu_add_company_members:
                launchAddCompanyMembers();
                return true;
            case R.id.menu_send_group_message:
                if (this.mSmartGroupParameters.length != 0) {
                    uri = getActivity().getIntent().getData();
                } else {
                    uri = ContentUris.withAppendedId(Groups.CONTENT_URI, this.mGroupId);
                }
                sendSmsMessage(uri);
                return true;
            case R.id.menu_send_group_mail:
                if (this.mSmartGroupParameters.length != 0) {
                    uri = getActivity().getIntent().getData();
                } else {
                    uri = ContentUris.withAppendedId(Groups.CONTENT_URI, this.mGroupId);
                }
                lIntent = new Intent("android.intent.action.HAP_SEND_GROUP_MAIL");
                lIntent.putExtra("extra_group_uri", uri);
                lIntent.putExtra("com.huawei.community.action.MAX_SELECT_COUNT", VTMCDataCache.MAXSIZE);
                startActivity(lIntent);
                if (this.mGroupType == 1) {
                    StatisticalHelper.report(1142);
                }
                ExceptionCapture.reportScene(21);
                return true;
            case R.id.sub_menu_delete_group:
            case R.id.menu_delete_group:
                GroupDeletionDialogFragment.show(getFragmentManager(), this.mGroupId, this.mGroupName, this.mCloseActivityAfterDelete, this.mListener);
                ExceptionCapture.reportScene(19);
                return true;
            case R.id.sub_menu_rename_group:
            case R.id.menu_rename_group:
                renameGroup(ContentUris.withAppendedId(Groups.CONTENT_URI, this.mGroupId));
                ExceptionCapture.reportScene(18);
                return true;
            default:
                return false;
        }
    }

    private void launchAddCompanyMembers() {
        Intent multiselectIntent = new Intent();
        setWindowWidthForIntent(multiselectIntent);
        multiselectIntent.setAction("android.intent.action.HAP_ADD_COMPANY_MEMBERS");
        multiselectIntent.putExtra("company_name", this.mSmartGroupParameters[1]);
        if (Integer.parseInt(this.mSmartGroupParameters[2]) == 1) {
            multiselectIntent.putExtra("is_no_company_group", true);
        } else {
            multiselectIntent.putExtra("is_no_company_group", false);
        }
        multiselectIntent.addFlags(67108864);
        startActivityForSplit(multiselectIntent);
        StatisticalHelper.report(1168);
    }

    private void renameGroup(Uri groupUri) {
        CreateGroupDialogFragment creategroupFragment = CreateGroupDialogFragment.newInstance();
        creategroupFragment.setGroupDetailListener(this.mListener);
        creategroupFragment.setLoadMananger(getLoaderManager());
        creategroupFragment.showDialog(getActivity(), "android.intent.action.EDIT", groupUri, null);
    }

    private void startAddGroupMembersIntent() {
        Intent lIntent = new Intent("android.intent.action.HAP_ADD_GROUP_MEMBERS");
        setWindowWidthForIntent(lIntent);
        lIntent.setPackage("com.android.contacts");
        lIntent.putExtra("extra_group_id", this.mGroupId);
        lIntent.putExtra("extra_account_name", this.mAccountName);
        lIntent.putExtra("extra_account_type", this.mAccountTypeString);
        lIntent.putExtra("extra_account_data_set", this.mDataSet);
        startActivityForSplit(lIntent);
    }

    public void closeActivityAfterDelete(boolean closeActivity) {
        this.mCloseActivityAfterDelete = closeActivity;
    }

    private void startActivityForSplit(Intent lIntent) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && (getActivity() instanceof PeopleActivity)) {
            getActivity().startActivityForResult(lIntent, 3001);
        } else {
            startActivity(lIntent);
        }
    }

    public long getGroupId() {
        return this.mGroupId;
    }

    private Uri getAndUpdateLookupKey(ContactTileAdapter$ContactEntry contactEntry) {
        if (contactEntry == null) {
            return null;
        }
        Uri uri = contactEntry.lookupKey;
        if (uri == null && contactEntry.mLookupKeyStr != null && contactEntry.mId > 0) {
            uri = ContentUris.withAppendedId(Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, contactEntry.mLookupKeyStr), contactEntry.mId);
        }
        return uri;
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        int adjPosition = position - this.mMemberListView.getHeaderViewsCount();
        if (adjPosition >= 0) {
            Intent intent = new Intent("android.intent.action.VIEW", getAndUpdateLookupKey(this.mAdapter.getItem(adjPosition)));
            intent.setClass(this.mContext, ContactDetailActivity.class);
            if (this.bFromSmartGroupList) {
                StatisticalHelper.report(1198);
            }
            if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
                startActivity(intent);
            } else if (getActivity() instanceof PeopleActivity) {
                PeopleActivity peopleActivity = (PeopleActivity) getActivity();
                ContactInfoFragment contactInfo = new ContactInfoFragment();
                contactInfo.setIntent(intent);
                peopleActivity.changeRightContainer(this, contactInfo);
            }
        }
    }

    private void setWindowWidthForIntent(Intent intent) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && (getActivity() instanceof PeopleActivity)) {
            PeopleActivity peopleActivity = (PeopleActivity) getActivity();
            peopleActivity.setNeedMaskDefault(true);
            intent.putExtra("landscape_window_width", (float) peopleActivity.getRightFramelayoutWidth(true));
            intent.putExtra("vertical_window_width", (float) peopleActivity.getRightFramelayoutWidth(false));
            intent.setClass(getApplicationContext(), TranslucentActivity.class);
            ((PeopleActivity) getActivity()).saveInstanceValues();
        }
    }

    private boolean isTargetAccountType(String accountTypeString) {
        if ("com.android.huawei.phone".equalsIgnoreCase(this.mAccountTypeString) || CommonUtilMethods.isSimAccount(this.mAccountTypeString)) {
            return true;
        }
        return this.mAccountTypeString != null ? this.mAccountTypeString.contains("com.huawei.android") : false;
    }

    private void sendSmsMessage(Uri uri) {
        Activity activity = getActivity();
        if (activity == null || activity.checkSelfPermission("android.permission.READ_SMS") == 0) {
            Intent lIntent = new Intent("android.intent.action.HAP_SEND_GROUP_MESSAGE");
            setWindowWidthForIntent(lIntent);
            lIntent.putExtra("extra_group_uri", uri);
            lIntent.putExtra("com.huawei.community.action.EXPECT_INTEGER_LIST", true);
            lIntent.putExtra("com.huawei.community.action.MAX_SELECT_COUNT", 1000);
            startActivityForSplit(lIntent);
            if (this.mGroupType == 1) {
                StatisticalHelper.report(1141);
            }
            ExceptionCapture.reportScene(20);
            return;
        }
        requestPermissions(new String[]{"android.permission.READ_SMS"}, 3);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (isAdded()) {
            switch (requestCode) {
                case 3:
                    if (permissions != null && permissions.length > 0) {
                        for (int i = 0; i < permissions.length; i++) {
                            if (grantResults[i] != 0) {
                                try {
                                    startActivity(RequestPermissionsActivityBase.createRequestPermissionIntent(permissions, getContext().getPackageName()));
                                } catch (Exception e) {
                                    HwLog.e("GroupDetailFragment", "Activity not find!");
                                }
                                return;
                            }
                        }
                        break;
                    }
            }
        }
    }
}
