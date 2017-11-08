package com.android.contacts.hap.list;

import android.app.FragmentTransaction;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout.LayoutParams;
import com.android.contacts.hap.activities.ContactAndGroupMultiSelectionActivity;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import com.android.contacts.hap.widget.AlphaIndexerPinnedHeaderListView;
import com.android.contacts.util.ActionBarCustomTitle;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.widget.ActionBarEx;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.android.widget.SubTabWidget.SubTabListener;
import java.util.ArrayList;

public class ContactDataMultiSelectFragmentEx extends ContactDataMultiSelectFragment implements SubTabListener {
    public void showSelectedItemCountInfo(int aCount) {
    }

    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        View view = getView().findViewById(R.id.overlay);
        if (view != null && getResources().getConfiguration().orientation == 1) {
            LayoutParams params = (LayoutParams) view.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.contact_multiselect_overlay_topmargin);
            view.setLayoutParams(params);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ContactAndGroupMultiSelectionActivity localActivityRef = this.mParentActivity;
        Uri selectedUri = this.mAdapter.getSelectedDataUri(position);
        if (!getListView().isItemChecked(getListView().getHeaderViewsCount() + position)) {
            int selGroupSize;
            int i;
            ArrayList<String> selectedGroups = (ArrayList) localActivityRef.mDataUriWithGroupsMap.get(selectedUri);
            if (selectedGroups != null) {
                selGroupSize = selectedGroups.size();
                for (i = 0; i < selGroupSize; i++) {
                    if (localActivityRef.mSelectedGroupIdList.contains(selectedGroups.get(i))) {
                        localActivityRef.mSelectedGroupIdList.remove(selectedGroups.get(i));
                    }
                }
                localActivityRef.mDataUriWithGroupsMap.remove(selectedUri);
            }
            selectedGroups = (ArrayList) localActivityRef.mSelectedDataUriWithGroupsMap.get(selectedUri);
            if (selectedGroups != null) {
                selGroupSize = selectedGroups.size();
                for (i = 0; i < selGroupSize; i++) {
                    if (localActivityRef.mSelectedGroupIdList.contains(selectedGroups.get(i))) {
                        localActivityRef.mSelectedGroupIdList.remove(selectedGroups.get(i));
                    }
                }
                localActivityRef.mSelectedDataUriWithGroupsMap.remove(selectedUri);
            }
        }
        super.onItemClick(parent, view, position, id);
    }

    public void onSubTabReselected(SubTab subTab, FragmentTransaction ft) {
    }

    public void onSubTabSelected(SubTab subTab, FragmentTransaction ft) {
        if (!(this.mScreenModeChange || this.mAdapter == null)) {
            updateSelectedCountInSearch();
            getActivity().invalidateOptionsMenu();
            this.mAdapter.notifyDataSetChanged();
        }
        this.mScreenModeChange = false;
    }

    public void onSubTabUnselected(SubTab subTab, FragmentTransaction ft) {
    }

    public void reSetFastScrllEnabled(AlphaIndexerPinnedHeaderListView listView) {
        if (listView != null) {
            listView.setIncludeStar(false);
            listView.setFastScrollEnabled(true);
        }
    }

    protected void configureActionBar() {
        if (EmuiVersion.isSupportEmui()) {
            ActionBarEx.setStartIcon(this.mActionBar, true, null, this.mActionBarListener);
            ActionBarEx.setEndIcon(this.mActionBar, true, null, this.mActionBarListener);
            return;
        }
        this.mCustActionBar.setStartIcon(true, null, this.mActionBarListener);
        this.mCustActionBar.setEndIcon(true, null, this.mActionBarListener);
    }

    protected void configureOptionsMenu(Menu menu) {
        ViewUtil.setMenuItemsStateListIcon(this.mContext, menu);
    }

    protected void setCustomTitle() {
        ActionBarCustomTitle customTitle = new ActionBarCustomTitle(this.mContext);
        if (EmuiVersion.isSupportEmui()) {
            ActionBarEx.setCustomTitle(this.mActionBar, customTitle.getTitleLayout());
        } else {
            this.mCustActionBar.setCustomTitle(customTitle.getTitleLayout());
        }
        if (((ContactMultiSelectionActivity) this.mParentActivity).mSelectedDataUris.size() == 0) {
            customTitle.setCustomTitle(getResources().getString(R.string.contacts_not_selected_text), this.localActivityRef.mSelectedDataUris.size());
        } else {
            customTitle.setCustomTitle(getResources().getString(R.string.contacts_selected_text), this.localActivityRef.mSelectedDataUris.size());
        }
    }
}
