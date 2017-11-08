package com.android.contacts.group;

import android.animation.Animator;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Groups;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import com.amap.api.services.core.AMapException;
import com.android.contacts.ContactSplitUtils;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GroupListLoader;
import com.android.contacts.activities.GroupDetailActivity;
import com.android.contacts.activities.HwCustGroupBrowserActivity;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.activities.SmartGroupBrowserActivity;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.editor.SelectAccountDialogFragment.Listener;
import com.android.contacts.fragment.HwBaseFragment;
import com.android.contacts.group.GroupBrowseListAdapter.GroupListItemViewCache;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity.TranslucentActivity;
import com.android.contacts.hap.utils.ActionBarTitle;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.hap.utils.ScreenUtils;
import com.android.contacts.hap.widget.SplitActionBarView;
import com.android.contacts.hap.widget.SplitActionBarView.OnCustomMenuListener;
import com.android.contacts.hap.widget.SplitActionBarView.SetButtonDetails;
import com.android.contacts.interactions.GroupDeletionDialogFragment;
import com.android.contacts.list.DefaultContactBrowseListFragment;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.AutoScrollListView;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCustUtils;

public class GroupBrowseListFragment extends HwBaseFragment implements OnFocusChangeListener, OnTouchListener, Listener, OnCreateContextMenuListener {
    public static final boolean DEBUG = HwLog.HWDBG;
    private static final String TAG = GroupBrowseListFragment.class.getSimpleName();
    CreateGroupDialogFragment creategroupFragment;
    private GroupEditorFragment.Listener groupEditorListener = new GroupEditorFragment.Listener() {
        public void onSaveFinished(int resultCode, Intent resultIntent) {
            if (resultIntent != null && resultIntent.getData() != null) {
                if ("android.intent.action.EDIT".equals(resultIntent.getAction())) {
                    GroupBrowseListFragment.this.getAdapter().notifyDataSetChanged();
                    return;
                }
                Intent intent = new Intent(GroupBrowseListFragment.this.getActivity(), GroupDetailActivity.class);
                intent.setData(resultIntent.getData());
                intent.setFlags(67108864);
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    Activity activity = GroupBrowseListFragment.this.getActivity();
                    if (activity instanceof PeopleActivity) {
                        ((PeopleActivity) activity).changeRightContainer(GroupBrowseListFragment.this, new GroupDetailFragment(intent));
                        return;
                    }
                    return;
                }
                GroupBrowseListFragment.this.startActivity(intent);
            }
        }

        public void onReverted() {
        }

        public void onGroupNotFound() {
        }

        public void onAccountsNotFound() {
        }
    };
    private ActionBarTitle mActionBarTitle;
    private GroupBrowseListAdapter mAdapter;
    private View mAddAccountButton;
    private View mAddAccountsView;
    private Context mContext;
    private MenuItem mDeleteMenu;
    private boolean mDeleteMenuState = false;
    private TextView mEmptyView;
    private HwCustGroupBrowserActivity mGroupBrowserActivityCust = null;
    private final LoaderCallbacks<Cursor> mGroupLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            GroupBrowseListFragment.this.mEmptyView.setText(null);
            return new GroupListLoader(GroupBrowseListFragment.this.mContext);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                GroupBrowseListFragment.this.updateDeleteGroupMenuState(data);
                GroupBrowseListFragment.this.bindGroupList(data);
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private AutoScrollListView mListView;
    private OnGroupBrowserActionListener mListener;
    private Menu mMenu;
    private int mPreDefinedItemCount;
    private View mRootView;
    private Uri mSelectedGroupUri;
    private boolean mSelectionToScreenRequested;
    private boolean mSelectionVisible;
    private int mVerticalScrollbarPosition = 2;

    public interface OnGroupBrowserActionListener {
        void onViewGroupAction(Uri uri);
    }

    private final class GroupBrowserActionListener implements OnGroupBrowserActionListener {
        GroupBrowserActionListener() {
        }

        public void onViewGroupAction(Uri groupUri) {
            if (GroupBrowseListFragment.DEBUG) {
                HwLog.d(GroupBrowseListFragment.TAG, "onViewGroupAction:" + groupUri);
            }
            if (GroupBrowseListFragment.this.getActivity() != null) {
                Intent intent = new Intent(GroupBrowseListFragment.this.getActivity(), GroupDetailActivity.class);
                intent.setData(groupUri);
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    Activity activity = GroupBrowseListFragment.this.getActivity();
                    if (activity instanceof PeopleActivity) {
                        ((PeopleActivity) activity).changeRightContainer(GroupBrowseListFragment.this, new GroupDetailFragment(intent));
                    }
                } else {
                    GroupBrowseListFragment.this.startActivity(intent);
                }
                StatisticalHelper.report(AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT);
            }
        }
    }

    public void onNewIntent(Intent intent) {
        if (!"saveCompleted".equals(intent.getAction()) || this.creategroupFragment == null) {
            setIntent(intent);
        } else {
            this.creategroupFragment.onSaveCompleted(getActivity(), true, intent.getData());
        }
    }

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setHasOptionsMenu(true);
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mGroupBrowserActivityCust = (HwCustGroupBrowserActivity) HwCustUtils.createObj(HwCustGroupBrowserActivity.class, new Object[0]);
        }
        if (CommonUtilMethods.calcIfNeedSplitScreen() && (getParentFragment() instanceof DefaultContactBrowseListFragment) && getParentFragment().getChildFragmentManager().findFragmentByTag("") != null) {
            this.creategroupFragment = CreateGroupDialogFragment.newInstance();
            initGroupEditorListener(this.creategroupFragment);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && savedInstanceState == null && (getActivity() instanceof PeopleActivity)) {
            savedInstanceState = CommonUtilMethods.getInstanceState();
        }
        if (savedInstanceState != null) {
            this.mSelectedGroupUri = (Uri) savedInstanceState.getParcelable("groups.groupUri");
            if (this.mSelectedGroupUri != null) {
                this.mSelectionToScreenRequested = true;
            }
        }
        this.mRootView = inflater.inflate(R.layout.group_browse_list_fragment, null);
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        this.mEmptyView = (TextView) this.mRootView.findViewById(R.id.empty);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            this.mActionBarTitle = new ActionBarTitle(this.mContext, this.mRootView.findViewById(R.id.edit_layout));
            this.mRootView.findViewById(R.id.edit_layout).setVisibility(0);
            this.mActionBarTitle.setTitle(getString(R.string.contacts_my_group));
            boolean isShowBackIcon = true;
            if (getActivity() instanceof PeopleActivity) {
                PeopleActivity act = (PeopleActivity) getActivity();
                ViewGroup contentView = (ViewGroup) this.mRootView.findViewById(R.id.group_browse_list_fragment);
                boolean isTwoColumn = ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode());
                ScreenUtils.adjustPaddingTop(act, contentView, isTwoColumn);
                isShowBackIcon = !isTwoColumn;
            }
            if (isShowBackIcon) {
                this.mActionBarTitle.setBackIcon(true, null, new OnClickListener() {
                    public void onClick(View arg0) {
                        GroupBrowseListFragment.this.getActivity().onBackPressed();
                    }
                });
            }
        } else {
            this.mRootView.findViewById(R.id.edit_layout).setVisibility(8);
        }
        this.mAdapter = new GroupBrowseListAdapter(this.mContext);
        this.mAdapter.setSelectionVisible(this.mSelectionVisible);
        this.mAdapter.setSelectedGroup(this.mSelectedGroupUri);
        this.mListView = (AutoScrollListView) this.mRootView.findViewById(R.id.list);
        this.mListView.setFastScrollEnabled(true);
        this.mListView.setOnFocusChangeListener(this);
        this.mListView.setOnTouchListener(this);
        this.mListView.setItemsCanFocus(true);
        this.mAdapter.setShowBottomDivider(true);
        this.mListView.setAdapter(this.mAdapter);
        CommonUtilMethods.addFootEmptyViewPortrait(this.mListView, this.mContext);
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (GroupBrowseListFragment.DEBUG) {
                    HwLog.d(GroupBrowseListFragment.TAG, "onCreateView position:" + position);
                }
                GroupListItemViewCache groupListItem = (GroupListItemViewCache) view.getTag();
                if (GroupBrowseListFragment.this.onItemClickForSmartGroup(groupListItem)) {
                    StatisticalHelper.sendReport(1140, position);
                    StatisticalHelper.report(AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT);
                    return;
                }
                if (groupListItem != null) {
                    GroupBrowseListFragment.this.viewGroup(groupListItem.getUri());
                }
            }
        });
        this.mListView.setEmptyView(this.mEmptyView);
        this.mAddAccountsView = this.mRootView.findViewById(R.id.add_accounts);
        this.mAddAccountButton = this.mRootView.findViewById(R.id.add_account_button);
        this.mAddAccountButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.settings.ADD_ACCOUNT_SETTINGS");
                intent.setFlags(524288);
                intent.putExtra("authorities", new String[]{"com.android.contacts"});
                GroupBrowseListFragment.this.startActivity(intent);
            }
        });
        setAddAccountsVisibility(!ContactsUtils.areGroupWritableAccountsAvailable(this.mContext));
        this.mListView.setOnCreateContextMenuListener(this);
        setPreDefinedItemNumber();
        initMenuForSplit(this.mRootView);
        return this.mRootView;
    }

    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        return ContactSplitUtils.createSplitAnimator(transit, enter, nextAnim, this.mRootView, R.drawable.multiselection_background, getActivity());
    }

    private void initMenuForSplit(View rootView) {
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            SetButtonDetails newGroup = new SetButtonDetails(R.drawable.ic_new_contact, R.string.groups_toobar_new);
            SetButtonDetails delGroup = new SetButtonDetails(R.drawable.ic_trash_normal, R.string.menu_delete_groups);
            this.mSplitActionBarView = (SplitActionBarView) rootView.findViewById(R.id.menu_view);
            this.mSplitActionBarView.setVisibility(0);
            this.mSplitActionBarView.fillDetails(newGroup, null, null, delGroup, false);
            this.mSplitActionBarView.setOnCustomMenuListener(new OnCustomMenuListener() {
                public boolean onCustomSplitMenuItemClick(int aMenuItem) {
                    switch (aMenuItem) {
                        case R.string.menu_delete_groups:
                            Intent lDeleteIntent = new Intent("com.huawei.android.groups.multiple.delete");
                            if ((GroupBrowseListFragment.this.getActivity() instanceof PeopleActivity) && CommonUtilMethods.calcIfNeedSplitScreen()) {
                                PeopleActivity peopleActivity = (PeopleActivity) GroupBrowseListFragment.this.getActivity();
                                peopleActivity.setNeedMaskDefault(true);
                                lDeleteIntent.putExtra("landscape_window_width", (float) peopleActivity.getRightFramelayoutWidth(true));
                                lDeleteIntent.putExtra("vertical_window_width", (float) peopleActivity.getRightFramelayoutWidth(false));
                                lDeleteIntent.setClass(GroupBrowseListFragment.this.getApplicationContext(), TranslucentActivity.class);
                                ((PeopleActivity) GroupBrowseListFragment.this.getActivity()).saveInstanceValues();
                            }
                            GroupBrowseListFragment.this.startActivityForSplit(lDeleteIntent);
                            ExceptionCapture.reportScene(11);
                            StatisticalHelper.report(1199);
                            break;
                        case R.string.groups_toobar_new:
                            GroupBrowseListFragment.this.createNewGroup();
                            ExceptionCapture.reportScene(10);
                            StatisticalHelper.report(1184);
                            break;
                        default:
                            return false;
                    }
                    return true;
                }

                public boolean onCustomMenuItemClick(MenuItem aMenuItem) {
                    return false;
                }

                public void onPrepareOptionsMenu(Menu aMenu) {
                }
            });
        }
    }

    private void startActivityForSplit(Intent lIntent) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && (getActivity() instanceof PeopleActivity)) {
            getActivity().startActivityForResult(lIntent, 3001);
        } else {
            startActivity(lIntent);
        }
    }

    private void setPreDefinedItemNumber() {
        if (QueryUtil.isHAPProviderInstalled()) {
            if (EmuiFeatureManager.isChinaArea()) {
                this.mPreDefinedItemCount = 3;
            } else {
                this.mPreDefinedItemCount = 2;
            }
            return;
        }
        this.mPreDefinedItemCount = 0;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
        setListener(new GroupBrowserActionListener());
    }

    public void onDetach() {
        super.onDetach();
        this.mContext = null;
    }

    public void onStart() {
        getLoaderManager().restartLoader(1, new Bundle(), this.mGroupLoaderListener);
        super.onStart();
    }

    private void bindGroupList(Cursor cursor) {
        this.mEmptyView.setText(R.string.no_Groups);
        setAddAccountsVisibility(!ContactsUtils.areGroupWritableAccountsAvailable(this.mContext));
        if (cursor != null) {
            this.mAdapter.setCursor(cursor);
            if (this.mSelectionToScreenRequested) {
                this.mSelectionToScreenRequested = false;
                requestSelectionToScreen();
            }
            this.mSelectedGroupUri = this.mAdapter.getSelectedGroup();
            if (this.mSelectionVisible && this.mSelectedGroupUri != null) {
                viewGroup(this.mSelectedGroupUri);
            }
        }
    }

    public void setListener(OnGroupBrowserActionListener listener) {
        this.mListener = listener;
    }

    private void setSelectedGroup(Uri groupUri) {
        if (DEBUG) {
            HwLog.d(TAG, "setSelectedGroup uri:" + groupUri);
        }
        this.mSelectedGroupUri = groupUri;
        this.mAdapter.setSelectedGroup(groupUri);
        this.mListView.invalidateViews();
    }

    private void viewGroup(Uri groupUri) {
        setSelectedGroup(groupUri);
        if (this.mListener != null && groupUri != null) {
            if (DEBUG) {
                HwLog.d(TAG, "viewGroup groupUri:" + groupUri);
            }
            this.mListener.onViewGroupAction(groupUri);
        }
    }

    protected void requestSelectionToScreen() {
        if (this.mSelectionVisible) {
            int selectedPosition = this.mAdapter.getSelectedGroupPosition();
            if (selectedPosition != -1) {
                this.mListView.requestPositionToScreen(selectedPosition, true);
            }
        }
    }

    private void hideSoftKeyboard() {
        if (this.mContext != null) {
            ((InputMethodManager) this.mContext.getSystemService("input_method")).hideSoftInputFromWindow(this.mListView.getWindowToken(), 0);
        }
    }

    public void onFocusChange(View view, boolean hasFocus) {
        if (view == this.mListView && hasFocus) {
            hideSoftKeyboard();
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        if (view == this.mListView) {
            hideSoftKeyboard();
        }
        return false;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("groups.groupUri", this.mSelectedGroupUri);
    }

    public void setAddAccountsVisibility(boolean visible) {
        if (this.mAddAccountsView != null) {
            this.mAddAccountsView.setVisibility(visible ? 0 : 8);
        }
    }

    public GroupBrowseListAdapter getAdapter() {
        return this.mAdapter;
    }

    public void updateDeleteGroupMenuState(Cursor data) {
        if (data != null) {
            while (data.moveToNext()) {
                boolean isGroupReadOnly;
                if (data.getInt(6) == 1) {
                    isGroupReadOnly = true;
                    continue;
                } else {
                    isGroupReadOnly = false;
                    continue;
                }
                if (!isGroupReadOnly) {
                    setDeleteMenuEnable(true);
                    return;
                }
            }
            setDeleteMenuEnable(false);
        }
    }

    private boolean onItemClickForSmartGroup(GroupListItemViewCache groupListItem) {
        boolean isSmartGroup;
        String groupType = null;
        if (groupListItem != null) {
            groupType = groupListItem.getGroupType();
        }
        if ("smart_groups_company".equals(groupType) || "smart_groups_location".equals(groupType)) {
            isSmartGroup = true;
        } else {
            isSmartGroup = "smart_groups_last_contact_time".equals(groupType);
        }
        if (isSmartGroup) {
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                Activity activity = getActivity();
                if (activity instanceof PeopleActivity) {
                    ((PeopleActivity) activity).changeRightContainer(this, new SmartGroupBrowseListFragment(groupType));
                }
            } else {
                Intent intent = new Intent();
                intent.putExtra("smart_groups_type", groupType);
                intent.setClass(getActivity(), SmartGroupBrowserActivity.class);
                startActivity(intent);
            }
        }
        return isSmartGroup;
    }

    public void onDestroy() {
        this.creategroupFragment = null;
        super.onDestroy();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        GroupListItem groupItem = getAdapter().getItem(((AdapterContextMenuInfo) menuInfo).position - this.mPreDefinedItemCount);
        if (groupItem != null) {
            menu.setHeaderTitle(groupItem.getTitle());
            String accountType = groupItem.getAccountType();
            if ("com.android.huawei.phone".equalsIgnoreCase(accountType) || "com.android.huawei.sim".equalsIgnoreCase(accountType) || "com.android.huawei.secondsim".equalsIgnoreCase(accountType) || (accountType != null && accountType.contains("com.huawei.android"))) {
                if (!groupItem.isGroupReadOnly()) {
                    addGroupRenameContextmenu(menu, groupItem);
                }
            } else if (groupItem.getGroupId() > 0 && groupItem.getDataSet() == null) {
                addGroupRenameContextmenu(menu, groupItem);
            }
            if (!groupItem.isGroupReadOnly()) {
                addGroupDeleteContextmenu(menu, groupItem);
            }
            if (this.mGroupBrowserActivityCust != null) {
                this.mGroupBrowserActivityCust.customizeContextMenu(menu, groupItem.isGroupReadOnly(), accountType, getActivity());
            }
        }
    }

    private void addGroupDeleteContextmenu(Menu menu, final GroupListItem groupItem) {
        menu.add(R.string.menu_deleteContact).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                GroupDeletionDialogFragment.show(GroupBrowseListFragment.this.getFragmentManager(), groupItem.getGroupId(), groupItem.getTitle(), false, null);
                ExceptionCapture.reportScene(55);
                return true;
            }
        });
    }

    private void addGroupRenameContextmenu(Menu menu, final GroupListItem groupItem) {
        menu.add(R.string.contacts_group_rename).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                GroupBrowseListFragment.this.renameGroup(ContentUris.withAppendedId(Groups.CONTENT_URI, groupItem.getGroupId()));
                ExceptionCapture.reportScene(54);
                return true;
            }
        });
    }

    private void renameGroup(Uri groupUri) {
        initCreateGroupFragment();
        this.creategroupFragment.setLoadMananger(getLoaderManager());
        this.creategroupFragment.showDialog(getActivity(), "android.intent.action.EDIT", groupUri, null);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            inflater.inflate(R.menu.group_browser, menu);
            this.mDeleteMenu = menu.findItem(R.id.menu_delete_groups_action);
            if (this.mDeleteMenuState) {
                this.mDeleteMenu.setEnabled(true);
            } else {
                this.mDeleteMenu.setEnabled(false);
            }
            ImmersionUtils.setImmersionMommonMenu(getActivity(), menu.findItem(R.id.menu_group_browser_newgroup));
            ImmersionUtils.setImmersionMommonMenu(getActivity(), this.mDeleteMenu);
            ViewUtil.setMenuItemStateListIcon(getContext(), menu.findItem(R.id.menu_group_browser_newgroup));
            ViewUtil.setMenuItemStateListIcon(getContext(), this.mDeleteMenu);
            this.mMenu = menu;
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (this.mSplitActionBarView == null) {
            return;
        }
        if (this.mDeleteMenuState) {
            this.mSplitActionBarView.setEnable(4, true);
        } else {
            this.mSplitActionBarView.setEnable(4, false);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                getActivity().finish();
                return true;
            case R.id.menu_delete_groups_action:
                startActivity(new Intent("com.huawei.android.groups.multiple.delete"));
                ExceptionCapture.reportScene(11);
                StatisticalHelper.report(1199);
                return true;
            case R.id.menu_group_browser_newgroup:
                createNewGroup();
                ExceptionCapture.reportScene(10);
                StatisticalHelper.report(1184);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.mMenu != null) {
            MenuItem newGroupItem = this.mMenu.findItem(R.id.menu_group_browser_newgroup);
            ImmersionUtils.setImmersionMommonMenu(getActivity(), newGroupItem);
            ImmersionUtils.setImmersionMommonMenu(getActivity(), this.mDeleteMenu);
            ImmersionUtils.setImmersionMommonMenu(getActivity(), this.mMenu.findItem(R.id.menu_confirm));
            ViewUtil.setMenuItemStateListIcon(getContext(), newGroupItem);
            ViewUtil.setMenuItemStateListIcon(getContext(), this.mDeleteMenu);
        }
        super.onConfigurationChanged(newConfig);
    }

    private void initCreateGroupFragment() {
        this.creategroupFragment = CreateGroupDialogFragment.newInstance();
        this.creategroupFragment.setTargetFragment(this, 0);
        initGroupEditorListener(this.creategroupFragment);
    }

    private void createNewGroup() {
        initCreateGroupFragment();
        this.creategroupFragment.showDialog(getActivity(), "android.intent.action.INSERT", null, null);
    }

    private void initGroupEditorListener(CreateGroupDialogFragment editGroupDialogFragment) {
        if (editGroupDialogFragment != null) {
            editGroupDialogFragment.setListener(this.groupEditorListener);
        }
    }

    private void setDeleteMenuEnable(boolean enableDeleteMenu) {
        if (enableDeleteMenu) {
            this.mDeleteMenuState = true;
        } else {
            this.mDeleteMenuState = false;
        }
        getActivity().invalidateOptionsMenu();
    }

    public void onAccountChosen(AccountWithDataSet account, Bundle extraArgs) {
        initCreateGroupFragment();
        this.creategroupFragment.setmActivity(getActivity());
        this.creategroupFragment.load("android.intent.action.INSERT", null, null);
        this.creategroupFragment.onAccountChosen(account, extraArgs);
    }

    public void onAccountSelectorCancelled() {
    }
}
