package com.android.contacts.hap.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.util.Constants;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.SearchContract$ContactsSearch;
import java.util.ArrayList;
import java.util.List;

public class FavoritesFrequentMultiSelectAdapter extends ContactMultiselectionAdapter {
    private static final String[] COLUMNS = new String[]{"_id", "display_name", "starred", "photo_id", "photo_uri", "lookup", "data1", "data2", "data3", "contact_id", "data4"};
    private Context mContext;
    private int mFavCount = 0;
    private FavoriteHeaderListener mFavoriteHeaderListener;
    private int mFirstDragPos = -1;
    private int mFreCount = 0;
    private ArrayList<Integer> mFrequentContactIds;
    private boolean mIsResetFirstDragItemHeigth;
    private int mItemDoubleHeight;
    private int mItemDragIconWidth;
    private int mItemHeaderViewHeight;
    private int mItemPaddingBottom;
    private int mItemPaddingLeft;
    private int mItemPaddingTop;
    private int mItemPhotoWidth;

    private static class ExContactListItemView extends LinearLayout implements Checkable {
        private ContactListItemView mContactListItemView;

        public ExContactListItemView(Context context) {
            super(context);
        }

        private void setContactListItemView(ContactListItemView view) {
            this.mContactListItemView = view;
        }

        public void setChecked(boolean checked) {
            this.mContactListItemView.setChecked(checked);
        }

        public boolean isChecked() {
            return this.mContactListItemView.isChecked();
        }

        public void toggle() {
            this.mContactListItemView.toggle();
        }
    }

    public interface FavoriteHeaderListener {
        void setFavoriteHeaderValue(String str);
    }

    public FavoritesFrequentMultiSelectAdapter(Context context) {
        super(context);
        this.mContext = context;
        this.mItemHeaderViewHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.drag_list_view_header_height);
        this.mItemPaddingLeft = this.mContext.getResources().getDimensionPixelSize(R.dimen.drag_list_view_icon_left_margin);
        this.mItemPaddingTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_list_item_padding_top);
        this.mItemPaddingBottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_list_item_padding_bottom);
        this.mItemDragIconWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.drag_list_view_icon_width);
        this.mItemPhotoWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.default_detail_contact_photo_margin);
        this.mItemDoubleHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.drag_list_view_double_line_height);
    }

    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        ContactListItemView view = (ContactListItemView) super.newView(context, partition, cursor, position, parent);
        view.setFromFavFreEditFragment(true);
        ExContactListItemView ll = new ExContactListItemView(context);
        ll.setContactListItemView(view);
        LayoutParams lp = new LayoutParams(-1, -2);
        lp.gravity = 80;
        ll.setLayoutParams(lp);
        view.setLayoutParams(lp);
        ll.addView(view);
        return ll;
    }

    public void setFavoriteHeaderListener(FavoriteHeaderListener listener) {
        this.mFavoriteHeaderListener = listener;
    }

    protected Uri getContactLookupUri(Cursor cursor, int pos) {
        if (pos < this.mFavCount) {
            return super.getContactLookupUri(cursor, pos);
        }
        return Uri.withAppendedPath(Data.CONTENT_URI, String.valueOf(Long.valueOf(cursor.getLong(0)).longValue()));
    }

    protected String getContactId(Cursor cursor) {
        if (cursor.getPosition() < this.mFavCount) {
            return cursor.getString(0);
        }
        return cursor.getString(9);
    }

    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        ExContactListItemView ll = (ExContactListItemView) itemView;
        ContactListItemView view = (ContactListItemView) ll.getChildAt(0);
        if (view == null) {
            HwLog.i("FavoritesFrequentMultiSelectAdapter", "bindView view is null");
            return;
        }
        super.bindView(view, partition, cursor, position);
        bindDragIcon(view, position);
        bindPhoneNumberAndLabel(view, cursor, position);
        setDividersPadding(view, position);
        initItemHeight(ll, position);
    }

    public Uri getSelectedContactUri(int aPosition) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(aPosition)) {
            return null;
        }
        if (aPosition < this.mFavCount) {
            return super.getSelectedContactUri(aPosition);
        }
        return Uri.withAppendedPath(Data.CONTENT_URI, String.valueOf(this.mCursor.getLong(0)));
    }

    protected boolean isBindPresenceAndStatusMessage() {
        return false;
    }

    protected long getPhotoId(Cursor cursor) {
        int starred = 0;
        if (!cursor.isNull(2)) {
            starred = cursor.getInt(2);
        }
        if (starred != 0 || cursor.isNull(3)) {
            return super.getPhotoId(cursor);
        }
        return cursor.getLong(3);
    }

    private void bindPhoneNumberAndLabel(ContactListItemView view, Cursor cursor, int pos) {
        if (pos >= this.mFavCount) {
            String number = cursor.getString(6);
            CharSequence label = null;
            if (!cursor.isNull(7)) {
                label = Phone.getTypeLabel(this.mContext.getResources(), cursor.getInt(7), cursor.getString(8));
            }
            view.showPhoneNumber(number, label);
        }
    }

    private void initItemHeight(LinearLayout parentView, int position) {
        int height;
        ViewGroup.LayoutParams params = parentView.getLayoutParams();
        if (this.mIsResetFirstDragItemHeigth && this.mFirstDragPos == position) {
            height = 1;
        } else {
            if (ContactDisplayUtils.isSimpleDisplayMode()) {
                height = this.mItemDoubleHeight;
            } else {
                height = (this.mItemPhotoWidth + this.mItemPaddingTop) + this.mItemPaddingBottom;
            }
            if (position >= this.mFavCount) {
                if (Constants.isEXTRA_HUGE()) {
                    height = (this.mItemPaddingTop + this.mContext.getResources().getDimensionPixelSize(R.dimen.drag_list_view_huge_font_line_height)) + this.mItemPaddingBottom;
                } else if (Constants.isFontSizeHugeorMore()) {
                    height = (this.mItemPaddingTop + this.mContext.getResources().getDimensionPixelSize(R.dimen.drag_list_view_largest_font_line_height)) + this.mItemPaddingBottom;
                }
            }
            if (position == (this.mFreCount == 0 ? -1 : this.mFavCount)) {
                height += this.mItemHeaderViewHeight;
            }
        }
        params.height = height;
        parentView.setLayoutParams(params);
    }

    public void setDragItemHeight(boolean isReset, int position) {
        this.mIsResetFirstDragItemHeigth = isReset;
        this.mFirstDragPos = position;
    }

    private void setDividersPadding(ContactListItemView view, int position) {
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            if (position < this.mFavCount) {
                view.setHorizontalDividerPadding(this.mItemPaddingLeft + this.mItemDragIconWidth, 0);
            } else {
                view.setHorizontalDividerPadding(0, 0);
            }
        } else if (position < this.mFavCount) {
            view.setHorizontalDividerPadding(this.mItemPaddingLeft + this.mItemDragIconWidth, 0);
        } else {
            view.setHorizontalDividerPadding(this.mItemPaddingLeft + this.mItemPhotoWidth, 0);
        }
    }

    public void configureLoader(CursorLoader loader, long directoryId) {
        super.configureLoader(loader, directoryId);
        if (!isSearchMode() && (loader instanceof FavoritesFrequentDataMultiSelectLoader)) {
            FavoritesFrequentDataMultiSelectLoader freLoader = (FavoritesFrequentDataMultiSelectLoader) loader;
            ContactListFilter filter = getFilter();
            configFrequentUri(freLoader, directoryId, filter);
            configFrequentProjection(freLoader);
            configFrequentSelectionAndArgs(freLoader, directoryId, filter);
            configFrequentOrder(freLoader);
        }
        loader.setSortOrder("starred_order");
    }

    protected void configHwSearchLoader(CursorLoader loader, long directoryId) {
        super.configHwSearchLoader(loader, directoryId);
        if (loader instanceof FavoritesFrequentDataMultiSelectLoader) {
            ((FavoritesFrequentDataMultiSelectLoader) loader).setWetherLoadFrequent(false);
            configHwSearchSelectionAndArgs((FavoritesFrequentDataMultiSelectLoader) loader);
        }
    }

    protected void configureSelection(CursorLoader loader, long directoryId, ContactListFilter filter) {
        if (filter != null && directoryId == 0) {
            StringBuilder selection = new StringBuilder();
            selection.append("starred!=0");
            if (isSearchMode() && this.mFrequentContactIds != null && this.mFrequentContactIds.size() > 0) {
                selection.append(" OR ").append("_id").append(" IN (");
                for (int i = 0; i < this.mFrequentContactIds.size(); i++) {
                    selection.append(this.mFrequentContactIds.get(i)).append(",");
                }
                selection.setLength(selection.length() - 1);
                selection.append(")");
            }
            loader.setSelection(selection.toString());
        }
    }

    private void configFrequentProjection(FavoritesFrequentDataMultiSelectLoader loader) {
        loader.setFrequentProjection(getFrequentColumns());
    }

    public static String[] getFrequentColumns() {
        return (String[]) COLUMNS.clone();
    }

    private void configFrequentUri(FavoritesFrequentDataMultiSelectLoader loader, long directoryId, ContactListFilter filter) {
        loader.setFrequentUri(Data.CONTENT_URI.buildUpon().appendQueryParameter("limit", String.valueOf(10)).build());
    }

    private void configFrequentSelectionAndArgs(FavoritesFrequentDataMultiSelectLoader freLoader, long directoryId, ContactListFilter filter) {
        if (filter != null && directoryId == 0) {
            if (this.mFrequentContactIds == null || this.mFrequentContactIds.size() < 1) {
                freLoader.setWetherLoadFrequent(false);
            } else {
                freLoader.setWetherLoadFrequent(true);
                StringBuilder selection = new StringBuilder();
                ArrayList<String> selectionArgs = new ArrayList();
                selection.append("starred").append("=0").append(" AND ").append("mimetype").append("=?").append(" AND ").append("_id").append(" IN (").append("SELECT ").append("data_id").append(" FROM ").append("data_usage_stat WHERE usage_type=0").append(")");
                selectionArgs.add("vnd.android.cursor.item/phone_v2");
                freLoader.setFrequentSelection(selection.toString());
                freLoader.setFrequentSelectionArgs((String[]) selectionArgs.toArray(new String[selectionArgs.size()]));
            }
        }
    }

    private void configFrequentOrder(FavoritesFrequentDataMultiSelectLoader freLoader) {
        freLoader.setFrequentSortOrder("times_used DESC,last_time_used DESC");
    }

    protected void bindSectionHeaderAndDivider(ContactListItemView view, int position, Cursor cursor, boolean isLastItem) {
        Bundle cursorExtra = cursor.getExtras();
        if (cursorExtra != null) {
            int freHeaderPos;
            int favCount = cursorExtra.getInt("favorites_count", 0);
            int freCount = cursorExtra.getInt("frequent_count", 0);
            this.mFavCount = favCount;
            this.mFreCount = freCount;
            int favHeaderPos = favCount == 0 ? -1 : 0;
            if (freCount == 0) {
                freHeaderPos = -1;
            } else {
                freHeaderPos = favCount;
            }
            if (freHeaderPos <= 0 || position != favCount - 1) {
                view.setDividerVisible(true);
            } else {
                view.setDividerVisible(false);
            }
            String title = null;
            CharSequence count = null;
            if (isSectionHeaderDisplayEnabled()) {
                if (position == favHeaderPos) {
                    String favoriteTitle = this.mContext.getString(R.string.contacts_header_starred_count, new Object[]{Integer.valueOf(favCount)});
                    if (this.mFavoriteHeaderListener != null) {
                        this.mFavoriteHeaderListener.setFavoriteHeaderValue(favoriteTitle);
                    }
                } else if (position == freHeaderPos) {
                    title = this.mContext.getString(R.string.contact_favorites_frequent_label);
                    count = String.valueOf(freCount);
                }
            }
            view.setSectionHeaderAndPadding(title, false);
            view.setCountView(count);
        }
    }

    protected void bindDragIcon(ContactListItemView view, int position) {
        if (view != null) {
            view.initDragIcon();
            if (position < this.mFavCount) {
                view.showDragIcon(true);
            } else {
                view.showDragIcon(false);
            }
        }
    }

    public void setFrequentContactIds(ArrayList<Integer> ids) {
        this.mFrequentContactIds = ids;
    }

    protected void configHwSearchUri(CursorLoader loader, long directoryId, ContactListFilter filter) {
        Uri uri = getHwSearchBaseUri(SearchContract$ContactsSearch.CONTACTS_CONTENT_FILTER_URI, "search_type", "search_contacts");
        if (filter == null || directoryId != 0) {
            loader.setUri(uri);
            return;
        }
        Builder builder = uri.buildUpon();
        if (PhoneCapabilityTester.isOnlySyncMyContactsEnabled(this.mContext) && filter.filterType != -3) {
            builder.appendQueryParameter("directory", String.valueOf(0));
        }
        switch (filter.filterType) {
            case -10:
                loader.setUri(uri);
                return;
            default:
                super.configHwSearchUri(loader, directoryId, filter);
                return;
        }
    }

    private void configHwSearchSelectionAndArgs(FavoritesFrequentDataMultiSelectLoader loader) {
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList();
        selection.append("starred").append("=1");
        if (this.mFrequentContactIds != null && this.mFrequentContactIds.size() > 0) {
            selection.append(" OR ").append("contact_id").append(" IN (");
            for (int i = 0; i < this.mFrequentContactIds.size(); i++) {
                selection.append(this.mFrequentContactIds.get(i)).append(",");
            }
            selection.setLength(selection.length() - 1);
            selection.append(")");
        }
        loader.setSelection(selection.toString());
        loader.setSelectionArgs((String[]) selectionArgs.toArray(new String[selectionArgs.size()]));
    }
}
