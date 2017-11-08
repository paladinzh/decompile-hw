package com.android.contacts.hap.group;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.ContactsContract.Contacts;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import com.amap.api.services.core.AMapException;
import com.android.contacts.ContactSaveService;
import com.android.contacts.group.GroupEditorFragment.Member;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.util.ActionBarCustom;
import com.android.contacts.hap.util.AlertDialogFragmet;
import com.android.contacts.hap.util.AlertDialogFragmet.OnDialogOptionSelectListener;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ActionBarCustomTitle;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.ActionBarEx;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.ArrayList;

public class GroupMultiseletionFragment extends ContactEntryListFragment<ContactEntryListAdapter> implements Callback {
    ContactListAdapter adapter;
    private boolean lIsFragmentFinished = false;
    private ActionBar mActionBar;
    private OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            int viewId = v.getId();
            if (viewId == 16908295 || viewId == R.id.icon1) {
                GroupMultiseletionFragment.this.getActivity().finish();
            }
        }
    };
    private MenuItem mActionMenu;
    private ArrayList<Uri> mAllMembers = new ArrayList();
    private Activity mContext;
    private ActionBarCustom mCustActionBar;
    private ArrayList<Integer> mSelectedGroups = new ArrayList();
    private ArrayList<Member> mSelectedMembersList = new ArrayList();
    private ActionBarCustomTitle mTitle;

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (savedState != null) {
            ArrayList<Integer> lTempSelectedGroups = (ArrayList) savedState.getSerializable("selected_contacts");
            ArrayList<Member> lTempSelectedMembersList = (ArrayList) savedState.getSerializable("selected_members_list");
            ArrayList<Uri> lTempAllMembers = (ArrayList) savedState.getSerializable("ALL_MEMBERS");
            if (lTempSelectedGroups != null) {
                this.mSelectedGroups = lTempSelectedGroups;
            }
            if (lTempSelectedMembersList != null) {
                this.mSelectedMembersList = lTempSelectedMembersList;
            }
            if (lTempAllMembers != null) {
                this.mAllMembers = lTempAllMembers;
            }
        }
    }

    public GroupMultiseletionFragment() {
        if (HwLog.HWDBG) {
            HwLog.d("GroupMultiseletionFragment", "GroupMultiseletionFragment constructer is called!");
        }
        setPhotoLoaderEnabled(false);
        setQuickContactEnabled(false);
        setSectionHeaderDisplayEnabled(false);
        setVisibleScrollbarEnabled(true);
        setSelectionVisible(true);
        setHasOptionsMenu(true);
    }

    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        getView().findViewById(R.id.contactListsearchlayout).setVisibility(8);
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        if (HwLog.HWDBG) {
            HwLog.d("GroupMultiseletionFragment", "inflateView() is called!");
        }
        this.mContext = getActivity();
        this.mActionBar = this.mContext.getActionBar();
        this.mTitle = new ActionBarCustomTitle(this.mContext, inflater);
        if (EmuiVersion.isSupportEmui()) {
            ActionBarEx.setStartIcon(this.mActionBar, true, null, this.mActionBarListener);
            ActionBarEx.setCustomTitle(this.mActionBar, this.mTitle.getTitleLayout());
        } else {
            this.mCustActionBar = new ActionBarCustom(this.mContext, this.mActionBar);
            this.mCustActionBar.setStartIcon(true, null, this.mActionBarListener);
            this.mCustActionBar.setCustomTitle(this.mTitle.getTitleLayout());
        }
        this.mTitle.setCustomTitle(CommonUtilMethods.getMultiSelectionTitle(this.mContext, this.mSelectedGroups.size()), this.mSelectedGroups.size());
        View lView = inflater.inflate(R.layout.contact_picker_content, container, false);
        ((ViewStub) lView.findViewById(R.id.pinnedHeaderList_stub)).setLayoutResource(CommonUtilMethods.getPinnedHeaderListViewResId(getContext()));
        return lView;
    }

    protected ContactListAdapter createListAdapter() {
        if (HwLog.HWDBG) {
            HwLog.d("GroupMultiseletionFragment", "createListAdapter() is called!");
        }
        this.adapter = getListAdapter();
        return this.adapter;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.mSelectAllItem = menu.findItem(R.id.menu_action_selectall);
        this.mActionMenu = menu.findItem(R.id.menu_action_operation);
        this.mActionMenu.setTitle(R.string.menu_delete_groups);
        this.mActionMenu.setIcon(ImmersionUtils.getImmersionImageID(this.mContext, R.drawable.ic_trash_normal_light, R.drawable.ic_trash_normal));
        ViewUtil.setMenuItemsStateListIcon(this.mContext, menu);
        this.mActionMenu.setVisible(true);
    }

    private ContactListAdapter getListAdapter() {
        return new GroupMultiselectionAdapter(getActivity());
    }

    protected void configureListView(ListView aListView) {
        if (HwLog.HWDBG) {
            HwLog.d("GroupMultiseletionFragment", "configureListView() is called!");
        }
        aListView.setOnFocusChangeListener(null);
        aListView.setOnTouchListener(null);
        aListView.setChoiceMode(2);
        aListView.setFastScrollEnabled(false);
        CommonUtilMethods.addFootEmptyViewPortrait(aListView, this.mContext);
    }

    public void handleEmptyList(int aCount) {
        setEmptyText((int) R.string.no_Groups);
        getEmptyTextView().setText(getString(R.string.no_Groups));
        super.handleEmptyList(aCount);
    }

    protected void onPartitionLoaded(int partitionIndex, Cursor data) {
        getListView().setFastScrollEnabled(false);
        if (!this.lIsFragmentFinished) {
            prepareList(data);
            super.onPartitionLoaded(partitionIndex, data);
            if (data.getCount() == 0) {
                setEmptyText((int) R.string.no_Groups);
                getEmptyTextView().setText(getString(R.string.no_Groups));
                this.mSelectedGroups.clear();
            } else {
                getEmptyTextView().setVisibility(4);
                getListView().setVisibility(0);
            }
        }
    }

    private void prepareList(Cursor mainCursor) {
        synchronized (this.mAllMembers) {
            this.mAllMembers.clear();
            if (mainCursor.moveToFirst()) {
                do {
                    this.mAllMembers.add(Uri.withAppendedPath(Contacts.CONTENT_URI, mainCursor.getString(0)));
                } while (mainCursor.moveToNext());
            }
        }
    }

    protected void onItemClick(int position, long id) {
        int lGroupId = ((GroupMultiselectionAdapter) getAdapter()).geGroupId(position);
        if (HwLog.HWDBG) {
            HwLog.d("GroupMultiseletionFragment", "onItemClick() is called!, position: [" + position + "]");
            HwLog.d("GroupMultiseletionFragment", "Group id:" + lGroupId);
        }
        if (getListView().isItemChecked(position)) {
            this.mSelectedGroups.add(Integer.valueOf(lGroupId));
        } else {
            this.mSelectedGroups.remove(Integer.valueOf(lGroupId));
        }
        this.mTitle.setCustomTitle(CommonUtilMethods.getMultiSelectionTitle(this.mContext, this.mSelectedGroups.size()), this.mSelectedGroups.size());
        getActivity().invalidateOptionsMenu();
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (HwLog.HWDBG) {
            HwLog.d("GroupMultiseletionFragment", "onItemClick() called for adapter view!, position: [" + position + "]");
        }
        super.onItemClick(parent, view, position, id);
    }

    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    public void onDestroyActionMode(ActionMode mode) {
    }

    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_action_selectall).setVisible(false);
        if (this.mSelectedGroups.size() > 0) {
            this.mActionMenu.setEnabled(true);
        } else {
            this.mActionMenu.setEnabled(false);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.mSelectedGroups.size() <= 0) {
            return false;
        }
        doOperation();
        return true;
    }

    private void doOperation() {
        int resTitleId;
        boolean isAllSelected;
        if (this.mSelectedGroups.size() == this.mAllMembers.size()) {
            resTitleId = R.string.delete_all_groups__title;
            isAllSelected = true;
        } else {
            resTitleId = R.plurals.delete_multi_groups__title;
            isAllSelected = false;
        }
        AlertDialogFragmet.show(getFragmentManager(), resTitleId, isAllSelected, this.mSelectedGroups.size(), true, this.mContext.getString(R.string.delete_multi_groups_dialog_message), R.string.delete_multi_groups_dialog_message, true, new OnDialogOptionSelectListener() {
            public void onDialogOptionSelected(int which, Context aContext) {
                if (which == -1) {
                    long[] lGroupIds = new long[GroupMultiseletionFragment.this.mSelectedGroups.size()];
                    for (int i = 0; i < GroupMultiseletionFragment.this.mSelectedGroups.size(); i++) {
                        lGroupIds[i] = (long) ((Integer) GroupMultiseletionFragment.this.mSelectedGroups.get(i)).intValue();
                    }
                    GroupMultiseletionFragment.this.getContext().startService(ContactSaveService.createMultipleGroupsDeletionIntent(GroupMultiseletionFragment.this.mContext, lGroupIds));
                    StatisticalHelper.sendReport((int) AMapException.CODE_AMAP_ENGINE_RETURN_TIMEOUT, lGroupIds.length);
                    GroupMultiseletionFragment.this.mContext.finish();
                    GroupMultiseletionFragment.this.lIsFragmentFinished = true;
                }
            }

            public int describeContents() {
                return 0;
            }

            public void writeToParcel(Parcel dest, int flags) {
            }
        }, 16843605, R.string.menu_deleteContact);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("selected_contacts", this.mSelectedGroups);
        outState.putSerializable("selected_members_list", this.mSelectedMembersList);
        outState.putSerializable("ALL_MEMBERS", this.mAllMembers);
    }
}
