package com.android.contacts.hap.camcard;

import android.animation.Animator;
import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import com.amap.api.services.core.AMapException;
import com.android.contacts.ContactSplitUtils;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactInfoFragment;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.camcard.bcr.CCardScanHandler;
import com.android.contacts.hap.list.ContactMultiselectionAdapter;
import com.android.contacts.hap.list.ContactMultiselectionFragment;
import com.android.contacts.hap.utils.ActionBarTitle;
import com.android.contacts.hap.utils.ScreenUtils;
import com.android.contacts.hap.widget.SplitActionBarView;
import com.android.contacts.hap.widget.SplitActionBarView.OnCustomMenuListener;
import com.android.contacts.hap.widget.SplitActionBarView.SetButtonDetails;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ActionBarCustomTitle;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.ActionBarEx;
import com.google.android.gms.R;

public class CCBrowseListFragment extends ContactMultiselectionFragment {
    private static final String TAG = CCBrowseListFragment.class.getSimpleName();
    private ContactListAdapter adapter;
    private OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            if (v.getId() == 16908295) {
                CCBrowseListFragment.this.switchMode(false);
            }
        }
    };
    private ActionBarTitle mActionBarTitle;
    private CCardScanHandler mCcardHandlr;
    private int mCount;
    private LayoutInflater mInflater;
    private boolean mIsDeleteMode = false;
    private View mRootView;
    private MenuItem mScanCard;

    public CCBrowseListFragment() {
        setPhotoLoaderEnabled(true);
        setQuickContactEnabled(false);
        setSectionHeaderDisplayEnabled(true);
        setVisibleScrollbarEnabled(false);
        setSelectionVisible(true);
        setHasOptionsMenu(true);
        configFamilynameOverLayDisplayEnabled(true);
    }

    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        return ContactSplitUtils.createSplitAnimator(transit, enter, nextAnim, this.mRootView, R.drawable.multiselection_background, getActivity());
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        boolean z = false;
        this.mContext = getActivity();
        this.mInflater = inflater;
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            switchMode(this.mIsDeleteMode);
        }
        View lView = inflater.inflate(R.layout.contact_picker_content, container, false);
        ((ViewStub) lView.findViewById(R.id.pinnedHeaderList_stub)).setLayoutResource(CommonUtilMethods.getPinnedHeaderListViewResId(getContext()));
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            createSplitActionBar(this.mContext, lView);
            refreshTitleBar();
            if (getActivity() instanceof PeopleActivity) {
                PeopleActivity act = (PeopleActivity) getActivity();
                ViewGroup contentView = (ViewGroup) lView.findViewById(R.id.contact_picker_content);
                if (ContactSplitUtils.getColumnsNumber(act, act.isInMultiWindowMode()) == 2) {
                    z = true;
                }
                ScreenUtils.adjustPaddingTop(act, contentView, z);
            }
        }
        this.mRootView = lView;
        return lView;
    }

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (savedState != null) {
            this.mCcardHandlr = CCardScanHandler.onRestoreInstance(savedState);
            this.mIsDeleteMode = savedState.getBoolean("delete_mode", false);
        }
    }

    public void onResume() {
        super.onResume();
        EmuiFeatureManager.isShowCamCard(getActivity());
        setScanMenuState();
    }

    protected ContactListAdapter createListAdapter() {
        this.adapter = getListAdapter();
        this.adapter.setFilter(getFilterCC());
        this.adapter.setDisplayPhotos(true);
        this.adapter.setLazyItemCheckedListener(getLazyCheckListener());
        if (this.adapter instanceof ContactMultiselectionAdapter) {
            ((ContactMultiselectionAdapter) this.adapter).switchMode(this.mIsDeleteMode);
        }
        refreshListViewDelayed();
        return this.adapter;
    }

    private void refreshListViewDelayed() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (CCBrowseListFragment.this.adapter != null) {
                    CCBrowseListFragment.this.adapter.notifyChange();
                }
            }
        }, 50);
    }

    private ContactListFilter getFilterCC() {
        return new ContactListFilter(-20, null, null, null, null);
    }

    protected String getHintString() {
        return getString(R.string.camcard_searchcard);
    }

    public CursorLoader createCursorLoader(int id) {
        return new CCListLoader(getActivity(), null, null, null, null, null);
    }

    protected ContactListAdapter getListAdapter() {
        return new CCardMultiselectionAdapter(getActivity());
    }

    public void handleEmptyList(int aCount) {
        int i = 0;
        this.mCount = aCount;
        setEmptyText(R.string.camcard_noCC);
        if (!isSearchMode()) {
            LinearLayout linearLayout = this.mSearchLayout;
            if (this.mCount == 0) {
                i = 8;
            }
            linearLayout.setVisibility(i);
        }
        super.handleEmptyList(aCount);
    }

    protected void onItemClick(int position, long id) {
        if (this.mIsDeleteMode) {
            super.onItemClick(position, id);
            return;
        }
        onViewContactAction(this.adapter.getContactUri(position));
        StatisticalHelper.report(AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS);
    }

    protected boolean onItemLongClick(int position, long id) {
        if (this.mIsDeleteMode) {
            return false;
        }
        resetSelect();
        switchMode(true);
        return super.onItemLongClick(position, id);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            super.onCreateOptionsMenu(menu, inflater);
            this.mScanCard = menu.findItem(R.id.menu_scan_card);
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        boolean z = false;
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            refreshSplitActionBar();
            return;
        }
        super.onPrepareOptionsMenu(menu);
        if (this.mIsDeleteMode) {
            menu.setGroupVisible(R.id.camcard_options, false);
            menu.setGroupVisible(R.id.update_options, true);
            HwLog.i(TAG, "Is deleltemode");
        } else {
            menu.setGroupVisible(R.id.camcard_options, true);
            menu.setGroupVisible(R.id.update_options, false);
            MenuItem menuItem = this.mScanCard;
            if (!EmuiFeatureManager.isSuperSaverMode()) {
                z = EmuiFeatureManager.isCamcardEnabled();
            }
            menuItem.setVisible(z);
            HwLog.i(TAG, "Not deleltemode");
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                resetSelect();
                getActivity().finish();
                return true;
            case R.id.menu_scan_card:
                Activity lActivity = getActivity();
                if (lActivity != null) {
                    this.mCcardHandlr = new CCardScanHandler();
                    this.mCcardHandlr.recognizeCapture(lActivity, this);
                    ExceptionCapture.reportScene(59);
                    StatisticalHelper.report(1161);
                    break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mCcardHandlr != null) {
            this.mCcardHandlr.onSaveInstance(outState);
        }
        outState.putBoolean("delete_mode", this.mIsDeleteMode);
        super.onSaveInstanceState(outState);
    }

    protected String getMultiselectionTitle() {
        if (this.mIsDeleteMode) {
            return super.getMultiselectionTitle();
        }
        return getString(R.string.contact_list_business_cards);
    }

    protected void onDeleteConfirmed() {
        switchMode(false);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mCcardHandlr != null) {
            if (this.mCcardHandlr.handlePhotoActivityResult(requestCode, resultCode, data, getActivity(), this)) {
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void refreshActionIcon() {
        if (this.mIsDeleteMode) {
            this.mActionBar.setDisplayHomeAsUpEnabled(false);
            this.mActionBar.setDisplayShowCustomEnabled(true);
            ActionBarEx.setStartIcon(this.mActionBar, true, null, this.mActionBarListener);
            ActionBarEx.setEndIcon(this.mActionBar, false, null, null);
            return;
        }
        ActionBarEx.setStartIcon(this.mActionBar, false, null, null);
        ActionBarEx.setEndIcon(this.mActionBar, false, null, null);
        this.mActionBar.setDisplayShowCustomEnabled(false);
        this.mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void switchMode(boolean isDeleteMode) {
        this.mIsDeleteMode = isDeleteMode;
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            refreshTitleBar();
        } else {
            if (this.mActionBar == null) {
                this.mActionBar = getActivity().getActionBar();
            }
            if (this.mTitle == null) {
                this.mTitle = new ActionBarCustomTitle();
            }
            this.mTitle.retInitCustomTitle(this.mContext, this.mInflater, isDeleteMode);
            if (this.mIsDeleteMode) {
                setActionCode(203);
                if (EmuiVersion.isSupportEmui()) {
                    ActionBarEx.setCustomTitle(this.mActionBar, this.mTitle.getTitleLayout());
                }
                updateActionBarTitle();
            } else {
                if (EmuiVersion.isSupportEmui()) {
                    ActionBarEx.setCustomTitle(this.mActionBar, this.mTitle.getTitleLayout());
                }
                this.mTitle.setCustomTitle(getMultiselectionTitle());
            }
            refreshActionIcon();
        }
        if (this.adapter instanceof ContactMultiselectionAdapter) {
            ((ContactMultiselectionAdapter) this.adapter).switchMode(isDeleteMode);
            this.adapter.notifyChange();
        }
        getActivity().invalidateOptionsMenu();
    }

    private void refreshTitleBar() {
        if (this.mIsDeleteMode) {
            setActionCode(203);
            if (this.mSelectedContacts.size() == 0) {
                this.mActionBarTitle.setTitleMiddle(getString(R.string.contacts_not_selected_text));
            } else {
                this.mActionBarTitle.setTitleMiddle(getString(R.string.contacts_selected_text), this.mSelectedContacts.size());
            }
            this.mActionBarTitle.setTitleVisible(false);
            this.mActionBarTitle.setStartIconVisible(true);
            this.mActionBarTitle.setBackIconVisible(false);
            return;
        }
        this.mActionBarTitle.setTitleMiddleVisible(false);
        this.mActionBarTitle.setTitle(getString(R.string.contact_list_business_cards));
        this.mActionBarTitle.setStartIconVisible(false);
        boolean isShowBackIcon = true;
        if (getActivity() instanceof PeopleActivity) {
            PeopleActivity peopleActivity = (PeopleActivity) getActivity();
            if (ContactSplitUtils.isSpiltTwoColumn(peopleActivity, peopleActivity.isInMultiWindowMode())) {
                isShowBackIcon = false;
            }
        }
        this.mActionBarTitle.setBackIconVisible(isShowBackIcon);
    }

    public boolean onBackPressedRet() {
        if (!this.mIsDeleteMode) {
            return false;
        }
        switchMode(false);
        return true;
    }

    private void onViewContactAction(Uri contactLookupUri) {
        Intent intent = new Intent("android.intent.action.VIEW", contactLookupUri);
        intent.setClass(getActivity(), ContactDetailActivity.class);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            if (getActivity() instanceof PeopleActivity) {
                ContactInfoFragment contactInfo = new ContactInfoFragment();
                contactInfo.setIntent(intent);
                ((PeopleActivity) getActivity()).changeRightContainer(this, contactInfo);
            }
            return;
        }
        startActivity(intent);
    }

    private void createSplitActionBar(Activity mContext, View lView) {
        this.mActionBarTitle = new ActionBarTitle(mContext, lView.findViewById(R.id.edit_layout));
        boolean isShowBackIcon = true;
        if (getActivity() instanceof PeopleActivity) {
            PeopleActivity peopleActivity = (PeopleActivity) getActivity();
            if (ContactSplitUtils.isSpiltTwoColumn(peopleActivity, peopleActivity.isInMultiWindowMode())) {
                isShowBackIcon = false;
            }
        }
        if (isShowBackIcon) {
            this.mActionBarTitle.setBackIcon(true, null, new OnClickListener() {
                public void onClick(View v) {
                    CCBrowseListFragment.this.getActivity().onBackPressed();
                }
            });
        }
        this.mActionBarTitle.setStartIcon(false, null, new OnClickListener() {
            public void onClick(View v) {
                CCBrowseListFragment.this.switchMode(false);
            }
        });
        lView.findViewById(R.id.edit_layout).setVisibility(0);
        this.mSplitActionBarView = (SplitActionBarView) lView.findViewById(R.id.menu_view);
        this.mSplitActionBarView.setVisibility(0);
        this.mSplitActionBarView.fillDetails(new SetButtonDetails(R.drawable.ic_trash_normal, R.string.menu_deleteContact), new SetButtonDetails(R.drawable.contacts_scan, R.string.camcard_scan_card), null, new SetButtonDetails(R.drawable.csp_selected_all_normal, R.string.contact_menu_select_all), false);
        this.mSplitActionBarView.setOnCustomMenuListener(new OnCustomMenuListener() {
            public boolean onCustomSplitMenuItemClick(int aMenuItem) {
                switch (aMenuItem) {
                    case R.string.menu_deleteContact:
                        if (CCBrowseListFragment.this.mSelectedContacts.size() > 0) {
                            CCBrowseListFragment.this.doOperation();
                            break;
                        }
                        break;
                    case R.string.camcard_scan_card:
                        Activity lActivity = CCBrowseListFragment.this.getActivity();
                        if (lActivity != null) {
                            CCBrowseListFragment.this.mCcardHandlr = new CCardScanHandler();
                            CCBrowseListFragment.this.mCcardHandlr.recognizeCapture(lActivity, CCBrowseListFragment.this);
                            ExceptionCapture.reportScene(59);
                            StatisticalHelper.report(1161);
                            break;
                        }
                        break;
                    default:
                        return CCBrowseListFragment.this.customSplitMenuItemClicked(aMenuItem);
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

    private void refreshSplitActionBar() {
        boolean z = false;
        innerRefreshSplitActionBar(1, 4);
        if (this.mIsDeleteMode) {
            this.mSplitActionBarView.setVisibility(4, true);
            this.mSplitActionBarView.setVisibility(1, true);
            this.mSplitActionBarView.setVisibility(2, false);
            if (this.mSelectedContacts.size() == 0) {
                this.mActionBarTitle.setTitleMiddle(getString(R.string.contacts_not_selected_text));
                return;
            } else {
                this.mActionBarTitle.setTitleMiddle(getString(R.string.contacts_selected_text), this.mSelectedContacts.size());
                return;
            }
        }
        this.mSplitActionBarView.setVisibility(4, false);
        this.mSplitActionBarView.setVisibility(1, false);
        this.mSplitActionBarView.setVisibility(2, true);
        SplitActionBarView splitActionBarView = this.mSplitActionBarView;
        if (!EmuiFeatureManager.isSuperSaverMode()) {
            z = true;
        }
        splitActionBarView.setVisibility(2, z);
    }

    private void setScanMenuState() {
        boolean z = false;
        if (this.mScanCard == null) {
            return;
        }
        if (!EmuiFeatureManager.isCamcardEnabled() || EmuiFeatureManager.isSuperSaverMode()) {
            this.mScanCard.setVisible(false);
            return;
        }
        MenuItem menuItem = this.mScanCard;
        if (!this.mIsDeleteMode) {
            z = true;
        }
        menuItem.setVisible(z);
    }
}
