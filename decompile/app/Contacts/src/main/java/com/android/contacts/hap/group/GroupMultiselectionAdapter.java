package com.android.contacts.hap.group;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Groups;
import android.text.SpannableString;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.contacts.GroupListLoader;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListItemView;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;

public class GroupMultiselectionAdapter extends ContactListAdapter {
    private Context mContext;
    Cursor mCursor;
    HwCustGroupMultiselectionAdapter mHwCust;
    private String mPrevAccountName;
    private int mRightPadding;

    public GroupMultiselectionAdapter(Context aContext) {
        super(aContext);
        this.mContext = aContext;
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mHwCust = (HwCustGroupMultiselectionAdapter) HwCustUtils.createObj(HwCustGroupMultiselectionAdapter.class, new Object[0]);
        }
    }

    public void configureLoader(CursorLoader loader, long directoryId) {
        ContactListFilter filter = getFilter();
        configureUri(loader);
        configureProjection(loader, directoryId, filter);
        configureSelection(loader, directoryId, filter);
        configureSortOrder(loader);
    }

    private void configureSortOrder(CursorLoader loader) {
        loader.setSortOrder(GroupListLoader.GROUP_LIST_SORT_ORDER);
    }

    private void configureSelection(CursorLoader loader, long directoryId, ContactListFilter filter) {
        StringBuilder selection = new StringBuilder();
        selection.append("deleted").append(" != 1 ").append(" AND ").append("title!= 'PREDEFINED_HUAWEI_GROUP_CCARD'");
        selection.append(" AND ").append("group_is_read_only").append(" != 1");
        if (this.mHwCust != null) {
            this.mHwCust.addCustomSelectionCondition(selection);
        }
        loader.setSelection(selection.toString());
    }

    private void configureProjection(CursorLoader loader, long directoryId, ContactListFilter filter) {
        loader.setProjection(GroupListLoader.getGroupsProjection());
    }

    private void configureUri(CursorLoader loader) {
        loader.setUri(Groups.CONTENT_SUMMARY_URI.buildUpon().appendQueryParameter("remove_duplicate_entries", "contact_id").build());
    }

    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        super.bindView(itemView, partition, cursor, position);
        ContactListItemView view = (ContactListItemView) itemView;
        if (position <= 0) {
            this.mPrevAccountName = null;
        } else if (cursor.moveToPrevious()) {
            this.mPrevAccountName = cursor.getString(0);
            cursor.moveToNext();
        }
        String lAccountName = cursor.getString(0);
        if (this.mPrevAccountName == null || !lAccountName.equals(this.mPrevAccountName)) {
            this.mPrevAccountName = lAccountName;
            if ("phone".equalsIgnoreCase(lAccountName)) {
                lAccountName = this.mContext.getString(R.string.phone_account_name);
            }
            view.setSectionHeader(this.mContext.getResources().getString(R.string.groups_in, new Object[]{lAccountName}));
        } else {
            view.setSectionHeader(null);
        }
        view.setCountView(null);
        bindName(view, cursor);
        bindCheckBox(view);
        if (this.mContext.getResources().getBoolean(R.bool.show_account_icons)) {
            bindAccountInfo(view, cursor);
        }
        view.setSnippet(null);
        view.setTag(Integer.valueOf(cursor.getInt(3)));
    }

    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        ContactListItemView view = (ContactListItemView) super.newView(context, partition, cursor, position, parent);
        view.setHeaderTextLeftIndent(true);
        return view;
    }

    protected void bindName(ContactListItemView view, Cursor cursor) {
        String title = CommonUtilMethods.parseGroupDisplayName(cursor.getString(1), this.mCursor.getString(4), this.mContext, this.mCursor.getString(9), this.mCursor.getInt(7), this.mCursor.getString(8));
        TextView nametextview = view.getNameTextView();
        nametextview.setSingleLine(true);
        if (this.mContext != null) {
            this.mRightPadding = this.mContext.getResources().getDimensionPixelOffset(R.dimen.group_dlete_multiselect_text_padding);
            nametextview.setPadding(0, 0, this.mRightPadding, 0);
        }
        if (title != null) {
            SpannableString spannable = new SpannableString(title);
            spannable.setSpan(TruncateAt.END, 0, spannable.length(), 33);
            nametextview.setText(spannable);
        }
    }

    public void setSelectedContactUri(Uri lLookupUri) {
    }

    public void changeCursor(int partitionIndex, Cursor cursor) {
        this.mCursor = cursor;
        super.changeCursor(partitionIndex, cursor);
    }

    public int geGroupId(int aPosition) {
        if (this.mCursor == null || aPosition >= this.mCursor.getCount()) {
            throw new IllegalArgumentException("Groups cursor is null or Groups cursor has changed");
        }
        this.mCursor.moveToPosition(aPosition);
        return this.mCursor.getInt(3);
    }
}
