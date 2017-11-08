package com.android.contacts.hap.list;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class ContactsMultiSelectSearchFragment extends ContactDataMultiSelectFragment {
    private MenuItem mDoneMenu;

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        View lView = inflater.inflate(R.layout.contact_picker_content, container, false);
        ((ViewStub) lView.findViewById(R.id.pinnedHeaderList_stub)).setLayoutResource(CommonUtilMethods.getPinnedHeaderListViewResId(getContext()));
        prepareSearchViewAndActionBar();
        return lView;
    }

    private void prepareSearchViewAndActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            View searchViewContainer = LayoutInflater.from(actionBar.getThemedContext()).inflate(R.layout.custom_action_bar, null);
            TextView countTextView = (TextView) searchViewContainer.findViewById(R.id.selected_item_count);
            countTextView.setVisibility(0);
            super.setTextViewCount(countTextView);
            actionBar.setCustomView(searchViewContainer, new LayoutParams(-2, -2));
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        this.mDoneMenu = menu.findItem(R.id.menu_action_done);
        MenuItem lSelectAll = menu.findItem(R.id.menu_action_selectall);
        this.mDoneMenu.setVisible(false);
        lSelectAll.setVisible(false);
        MenuItem menu_ok = menu.findItem(R.id.menu_action_ok);
        MenuItem menu_cancel = menu.findItem(R.id.menu_action_cancel);
        menu_ok.setVisible(true);
        menu_cancel.setVisible(true);
        if (!(getAdapter() == null || getAdapter().getCount() == 0)) {
            if (getListView().getCheckedItemCount() == 0) {
            }
            super.onPrepareOptionsMenu(menu);
        }
        menu_ok.setEnabled(false);
        super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_cancel:
                getActivity().finish();
                break;
            case R.id.menu_action_ok:
                return super.onOptionsItemSelected(this.mDoneMenu);
            default:
                HwLog.w("ContactsMultiSelectSearchFragment", "Unknown menu item id: " + item.getItemId());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
