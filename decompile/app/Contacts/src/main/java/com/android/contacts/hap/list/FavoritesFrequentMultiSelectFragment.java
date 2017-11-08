package com.android.contacts.hap.list;

import android.app.ActionBar;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.delete.ExtendedContactSaveService;
import com.android.contacts.hap.list.FavoritesFrequentMultiSelectAdapter.FavoriteHeaderListener;
import com.android.contacts.hap.widget.SplitActionBarView;
import com.android.contacts.hap.widget.SplitActionBarView.OnCustomMenuListener;
import com.android.contacts.list.ContactEntryListAdapter.LazyItemCheckedListener;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.DragListView;
import com.android.contacts.list.DragListView.DropListener;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.ActionBarEx;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

public class FavoritesFrequentMultiSelectFragment extends ContactMultiselectionFragment {
    private OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            if (v.getId() == 16908296) {
                final ContentResolver resolver = FavoritesFrequentMultiSelectFragment.this.mContext.getApplicationContext().getContentResolver();
                final Set entries = FavoritesFrequentMultiSelectFragment.this.mDraggedItemAfterOrderMap.entrySet();
                ContactsThreadPool.getInstance().execute(new Runnable() {
                    public void run() {
                        FavoritesFrequentMultiSelectFragment.this.saveStarredOrderOperation(resolver, entries);
                    }
                });
            }
            FavoritesFrequentMultiSelectFragment.this.doActionCancel();
        }
    };
    private FavoritesFrequentMultiSelectAdapter mAdapter;
    private OnKeyListener mBacklistener = new OnKeyListener() {
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (keyEvent.getAction() != 0 || i != 4) {
                return false;
            }
            FavoritesFrequentMultiSelectFragment.this.doActionCancel();
            return true;
        }
    };
    private ReoderedCursor mCursor;
    private int[] mCursorIndexs;
    private HashMap<Integer, Integer> mDraggedItemAfterOrderMap = new HashMap();
    DropListener mDropListener = new DropListener() {
        public void onDragStart(int position) {
        }

        public void onDragEnd() {
        }

        public void drop(int from, int to) {
            if (from != to) {
                int start;
                int end;
                FavoritesFrequentMultiSelectFragment.this.showActionBarEndIcon(true);
                from = from > 0 ? from - 1 : 0;
                if (to > 0) {
                    to--;
                } else {
                    to = 0;
                }
                if (from > to) {
                    start = to;
                    end = from;
                } else {
                    start = from;
                    end = to;
                }
                int[] orders = FavoritesFrequentMultiSelectFragment.this.getOldStarredOrder(start, end);
                FavoritesFrequentMultiSelectFragment.this.mCursor.reorder(from, to);
                FavoritesFrequentMultiSelectFragment.this.pushOrderIntoMap(start, end, orders);
                StatisticalHelper.report(4050);
            }
        }
    };
    private FavoriteHeaderListener mFavoriteHeaderListener = new FavoriteHeaderViewListener();
    private RelativeLayout mFavoriteHeaderView;
    private ArrayList<Integer> mFrequentContactIds = new ArrayList();
    private int mHeaderCountInSearch = 1;
    private boolean mIsAllSelected;
    private boolean mIsDragged = false;
    private HashMap<Integer, Integer> mOrderChangedContactsMap;
    private LinkedHashSet<Uri> mSelectedFrequent = new LinkedHashSet();
    private LinkedHashSet<Uri> mSelectedStarred = new LinkedHashSet();
    private int mStarredCount;

    public class FavoriteHeaderViewListener implements FavoriteHeaderListener {
        public void setFavoriteHeaderValue(String value) {
            CharSequence charSequence = null;
            if (FavoritesFrequentMultiSelectFragment.this.mFavoriteHeaderView != null) {
                TextView favTitle = (TextView) FavoritesFrequentMultiSelectFragment.this.mFavoriteHeaderView.findViewById(R.id.tv_drag_pinned_list_view_favorite_header);
                if (value != null) {
                    charSequence = value.toUpperCase();
                }
                favTitle.setText(charSequence);
            }
        }
    }

    public FavoritesFrequentMultiSelectFragment(float landListWeight, float verticalListWeight) {
        super(landListWeight, verticalListWeight);
    }

    public CursorLoader createCursorLoader(int id) {
        FavoritesFrequentDataMultiSelectLoader loader = new FavoritesFrequentDataMultiSelectLoader(getActivity(), null, null, null, null, null);
        loader.setSortOrder("starred_order");
        return loader;
    }

    private void saveStarredOrderOperation(ContentResolver resolver, Set entries) {
        ArrayList<ContentProviderOperation> aDiff = new ArrayList();
        if (entries != null) {
            try {
                int index = 0;
                for (Entry entry : entries) {
                    int id = ((Integer) entry.getKey()).intValue();
                    int starred_order = ((Integer) entry.getValue()).intValue();
                    Builder builder = ContentProviderOperation.newUpdate(Contacts.CONTENT_URI);
                    builder.withSelection("_id = ? ", new String[]{String.valueOf(id)});
                    builder.withValue("starred_order", Integer.valueOf(starred_order));
                    aDiff.add(builder.build());
                    index++;
                    if (index % 400 == 0) {
                        resolver.applyBatch("com.android.contacts", aDiff);
                        aDiff.clear();
                    }
                }
                if (aDiff.size() > 0) {
                    resolver.applyBatch("com.android.contacts", aDiff);
                    aDiff.clear();
                }
            } catch (Exception e) {
                HwLog.e("FavoritesFrequentMultiSelectFragment", "save starred order  message: " + e.getMessage());
            }
        }
    }

    protected boolean isShowSearchLayout() {
        return false;
    }

    protected void doActionCancel() {
        super.doActionCancel();
        getActivity().overridePendingTransition(0, 0);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ((DragListView) getListView()).setDropListener(this.mDropListener);
        ((DragListView) getListView()).setSelection(getActivity().getIntent().getIntExtra("FIRST_VISIBLE_POSITION", 0));
        int contactType = getActivity().getIntent().getIntExtra("SELECTED_CONTACT_TYPE_KEY", -1);
        String contactLookupUri = getIntent().getStringExtra("SELECTED_CONTACT_LOOKUP_URI");
        if (contactLookupUri != null) {
            Uri defaultSelectedContactLookupUri = Uri.parse(contactLookupUri);
            if (contactType == 0) {
                this.mSelectedStarred.add(defaultSelectedContactLookupUri);
            } else {
                this.mSelectedFrequent.add(defaultSelectedContactLookupUri);
            }
            getIntent().removeExtra("SELECTED_CONTACT_LOOKUP_URI");
        }
        this.mFavoriteHeaderView = (RelativeLayout) LayoutInflater.from(this.mContext).inflate(R.layout.drag_list_view_favorite_header_view, null).findViewById(R.id.ll_drag_pinned_list_view_favorite_header);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(this.mBacklistener);
        return view;
    }

    protected int initViewStub() {
        return R.layout.drag_pinned_header_listview;
    }

    private int[] getOldStarredOrder(int start, int end) {
        int[] orders = new int[((end - start) + 1)];
        if (this.mCursor != null) {
            this.mCursor.setSingleMoveToPosition(true);
            int i = start;
            int j = 0;
            while (i <= end) {
                int j2;
                if (this.mCursor.moveToPosition(i)) {
                    j2 = j + 1;
                    orders[j] = this.mCursor.getInt(this.mCursor.getColumnIndex("starred_order"));
                } else {
                    j2 = j;
                }
                i++;
                j = j2;
            }
            this.mCursor.setSingleMoveToPosition(false);
        }
        return orders;
    }

    private void pushOrderIntoMap(int start, int end, int[] orders) {
        int i = start;
        int j = 0;
        while (i <= end) {
            int j2;
            if (this.mCursor.moveToPosition(i)) {
                j2 = j + 1;
                this.mDraggedItemAfterOrderMap.put(Integer.valueOf(this.mCursor.getInt(this.mCursor.getColumnIndex("_id"))), Integer.valueOf(orders[j]));
            } else {
                j2 = j;
            }
            i++;
            j = j2;
        }
    }

    private void showActionBarEndIcon(boolean isShow) {
        ActionBar mActionBar = getActivity().getActionBar();
        if (EmuiVersion.isSupportEmui()) {
            ActionBarEx.setEndIcon(mActionBar, isShow, null, this.mActionBarListener);
            this.mIsDragged = true;
        }
    }

    protected ContactListAdapter getListAdapter() {
        this.mAdapter = new FavoritesFrequentMultiSelectAdapter(getActivity());
        this.mFrequentContactIds = getActivity().getIntent().getIntegerArrayListExtra("extra_frequent_contact_ids");
        this.mAdapter.setFrequentContactIds(this.mFrequentContactIds);
        ((DragListView) getListView()).setDragListViewAdapter(this.mAdapter);
        this.mAdapter.setFavoriteHeaderListener(this.mFavoriteHeaderListener);
        return this.mAdapter;
    }

    protected LazyItemCheckedListener getLazyCheckListener() {
        return new LazyItemCheckedListener() {
            public void setItemChecked(int position, Uri bindUri, boolean isSearchMode) {
                int headerViewCount = FavoritesFrequentMultiSelectFragment.this.getListView().getHeaderViewsCount();
                FavoritesFrequentMultiSelectFragment.this.getListView().setItemChecked(isSearchMode ? FavoritesFrequentMultiSelectFragment.this.mHeaderCountInSearch + position : position + headerViewCount, !FavoritesFrequentMultiSelectFragment.this.mSelectedStarred.contains(bindUri) ? FavoritesFrequentMultiSelectFragment.this.mSelectedFrequent.contains(bindUri) : true);
            }
        };
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            if (this.mCursorIndexs == null || data.getCount() != this.mCursorIndexs.length) {
                this.mCursor = new ReoderedCursor(data);
            } else {
                this.mCursor = new ReoderedCursor(data, this.mCursorIndexs);
                if (this.mOrderChangedContactsMap != null) {
                    boolean z;
                    if (this.mOrderChangedContactsMap.size() > 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    showActionBarEndIcon(z);
                    this.mDraggedItemAfterOrderMap = (HashMap) this.mOrderChangedContactsMap.clone();
                }
            }
            Bundle cursorExtra = this.mCursor.getExtras();
            if (cursorExtra != null) {
                this.mStarredCount = cursorExtra.getInt("favorites_count", 0);
                getListView().removeHeaderView(this.mFavoriteHeaderView);
                if (this.mStarredCount > 0) {
                    getListView().addHeaderView(this.mFavoriteHeaderView);
                    ((DragListView) getListView()).setStarredCount(this.mStarredCount);
                }
            }
        }
        super.onLoadFinished((Loader) loader, this.mCursor);
        getActivity().invalidateOptionsMenu();
        setVisibleScrollbarEnabled(false);
    }

    protected void onItemClick(int position, long id) {
        int dataPos;
        int headerViewCount = getListView().getHeaderViewsCount();
        if (isSearchMode()) {
            dataPos = position - this.mHeaderCountInSearch;
        } else {
            dataPos = position;
        }
        if (dataPos >= 0) {
            Uri contactLookupUri = this.mAdapter.getSelectedContactUri(dataPos);
            if (contactLookupUri == null) {
                HwLog.e("FavoritesFrequentMultiSelectFragment", "onItemClick,contactLookupUri=null");
                return;
            }
            ListView listView = getListView();
            if (isSearchMode()) {
                int contactId = this.mAdapter.getSelectedContactId(dataPos);
                if (listView.isItemChecked(position)) {
                    if (this.mFrequentContactIds == null || !this.mFrequentContactIds.contains(Integer.valueOf(contactId))) {
                        this.mSelectedStarred.add(contactLookupUri);
                        StatisticalHelper.report(2016);
                    } else {
                        this.mSelectedFrequent.add(contactLookupUri);
                        StatisticalHelper.report(2016);
                    }
                } else if (this.mFrequentContactIds == null || !this.mFrequentContactIds.contains(Integer.valueOf(contactId))) {
                    this.mSelectedStarred.remove(contactLookupUri);
                } else {
                    this.mSelectedFrequent.remove(contactLookupUri);
                }
            } else if (listView.isItemChecked(position + headerViewCount)) {
                if (dataPos < this.mStarredCount) {
                    this.mSelectedStarred.add(contactLookupUri);
                    StatisticalHelper.report(2016);
                } else {
                    this.mSelectedFrequent.add(contactLookupUri);
                    StatisticalHelper.report(2016);
                }
            } else if (dataPos < this.mStarredCount) {
                this.mSelectedStarred.remove(contactLookupUri);
            } else {
                this.mSelectedFrequent.remove(contactLookupUri);
            }
            getActivity().invalidateOptionsMenu();
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        totleCount = isSearchMode() ? this.mAdapter.getCount() > 0 ? this.mAdapter.getCount() - 1 : 0 : this.mAdapter.getCount();
        int selectedCount = this.mSelectedStarred.size() + this.mSelectedFrequent.size();
        boolean z;
        if (this.mSplitActionBarView != null) {
            SplitActionBarView splitActionBarView = this.mSplitActionBarView;
            if (selectedCount > 0) {
                z = true;
            } else {
                z = false;
            }
            splitActionBarView.setEnable(1, z);
            splitActionBarView = this.mSplitActionBarView;
            if (totleCount > 0) {
                z = true;
            } else {
                z = false;
            }
            splitActionBarView.setEnable(4, z);
        } else {
            MenuItem menuItem = this.mActionMenu;
            if (selectedCount > 0) {
                z = true;
            } else {
                z = false;
            }
            menuItem.setEnabled(z);
            menuItem = this.mSelectAllItem;
            if (totleCount > 0) {
                z = true;
            } else {
                z = false;
            }
            menuItem.setEnabled(z);
        }
        boolean isAllItemSelected = true;
        if (isSearchMode()) {
            for (int i = this.mHeaderCountInSearch; i < getListView().getCount(); i++) {
                Uri contactLookupUri = this.mAdapter.getSelectedContactUri(i - this.mHeaderCountInSearch);
                if (!this.mSelectedStarred.contains(contactLookupUri) && !this.mSelectedFrequent.contains(contactLookupUri)) {
                    isAllItemSelected = false;
                    break;
                }
            }
        } else {
            isAllItemSelected = selectedCount >= totleCount;
        }
        if (totleCount <= 0 || !isAllItemSelected || selectedCount <= 0) {
            setSelectAllItemTitle(false);
            this.mIsAllSelected = false;
        } else {
            setSelectAllItemTitle(true);
            this.mIsAllSelected = true;
        }
        this.mTitle.setCustomTitle(CommonUtilMethods.getMultiSelectionTitle(getContext(), selectedCount), selectedCount);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_operation:
                if (this.mSelectedStarred.size() + this.mSelectedFrequent.size() > 0) {
                    doOperation();
                    StatisticalHelper.report(2017);
                    break;
                }
                break;
            case R.id.menu_action_selectall:
                if (this.mIsAllSelected) {
                    deselectAllItems();
                } else {
                    selectAllItems();
                }
                this.mAdapter.notifyChange();
                getActivity().invalidateOptionsMenu();
                break;
        }
        return false;
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        View view = super.inflateView(inflater, container);
        if (this.mSplitActionBarView != null) {
            this.mSplitActionBarView.setOnCustomMenuListener(new OnCustomMenuListener() {
                public void onPrepareOptionsMenu(Menu aMenu) {
                }

                public boolean onCustomSplitMenuItemClick(int aMenuItem) {
                    switch (aMenuItem) {
                        case R.string.contact_menu_select_all:
                        case R.string.menu_select_none:
                            if (FavoritesFrequentMultiSelectFragment.this.mIsAllSelected) {
                                FavoritesFrequentMultiSelectFragment.this.deselectAllItems();
                            } else {
                                FavoritesFrequentMultiSelectFragment.this.selectAllItems();
                            }
                            FavoritesFrequentMultiSelectFragment.this.mAdapter.notifyChange();
                            FavoritesFrequentMultiSelectFragment.this.getActivity().invalidateOptionsMenu();
                            break;
                        case R.string.add_label:
                        case R.string.remove_label:
                            if (FavoritesFrequentMultiSelectFragment.this.mSelectedStarred.size() + FavoritesFrequentMultiSelectFragment.this.mSelectedFrequent.size() > 0) {
                                FavoritesFrequentMultiSelectFragment.this.doOperation();
                                StatisticalHelper.report(2017);
                                break;
                            }
                            break;
                        default:
                            return false;
                    }
                    return true;
                }

                public boolean onCustomMenuItemClick(MenuItem aMenuItem) {
                    return false;
                }
            });
        }
        return view;
    }

    protected void selectAllItems() {
        ListView listView = getListView();
        int totleCount = listView.getCount();
        int headerViewCount = listView.getHeaderViewsCount();
        int footerViewCount = listView.getFooterViewsCount();
        try {
            int i;
            Uri contactLookupUri;
            if (isSearchMode()) {
                for (i = this.mHeaderCountInSearch; i < totleCount; i++) {
                    contactLookupUri = this.mAdapter.getSelectedContactUri(i - this.mHeaderCountInSearch);
                    int contactId = this.mAdapter.getSelectedContactId(i - this.mHeaderCountInSearch);
                    if (this.mFrequentContactIds == null || !this.mFrequentContactIds.contains(Integer.valueOf(contactId))) {
                        this.mSelectedStarred.add(contactLookupUri);
                    } else {
                        this.mSelectedFrequent.add(contactLookupUri);
                    }
                }
                return;
            }
            for (i = 0; i < (totleCount - headerViewCount) - footerViewCount; i++) {
                contactLookupUri = this.mAdapter.getSelectedContactUri(i);
                if (i < this.mStarredCount) {
                    this.mSelectedStarred.add(contactLookupUri);
                } else {
                    this.mSelectedFrequent.add(contactLookupUri);
                }
            }
        } catch (IllegalStateException e) {
            HwLog.e("FavoritesFrequentMultiSelectFragment", "fail to selectAllItems");
        }
    }

    protected void deselectAllItems() {
        ListView listView = getListView();
        int headerViewCount = listView.getHeaderViewsCount();
        try {
            int i;
            Uri contactLookupUri;
            if (isSearchMode()) {
                for (i = this.mHeaderCountInSearch; i < listView.getCount(); i++) {
                    contactLookupUri = this.mAdapter.getSelectedContactUri(i - this.mHeaderCountInSearch);
                    this.mSelectedStarred.remove(contactLookupUri);
                    this.mSelectedFrequent.remove(contactLookupUri);
                }
                return;
            }
            for (i = 0; i < listView.getCount() - headerViewCount; i++) {
                contactLookupUri = this.mAdapter.getSelectedContactUri(i);
                this.mSelectedStarred.remove(contactLookupUri);
                this.mSelectedFrequent.remove(contactLookupUri);
            }
        } catch (IllegalStateException e) {
            HwLog.e("FavoritesFrequentMultiSelectFragment", "fail to deselectAllItems");
        }
    }

    protected void doOperation() {
        if (this.mHasGrayForFav) {
            final Context context = getContext();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    FavoritesFrequentMultiSelectFragment.this.doRemoveAction(context);
                }
            }, 300);
        } else {
            doRemoveAction(getContext());
        }
        getActivity().finish();
        getActivity().overridePendingTransition(0, 0);
    }

    private void doRemoveAction(Context context) {
        if (this.mIsDragged) {
            saveStarredOrderOperation(context.getContentResolver(), this.mDraggedItemAfterOrderMap.entrySet());
        }
        if (this.mSelectedStarred.size() > 0) {
            removeStarredContacts(context);
        }
        if (this.mSelectedFrequent.size() > 0) {
            removeFrequentContacts(context);
        }
    }

    private void removeStarredContacts(Context context) {
        context.startService(ExtendedContactSaveService.createMarkUnmarkFavoriteSelectedContactsIntent(context, getSelectedItemIds(this.mSelectedStarred), false));
    }

    private void removeFrequentContacts(Context context) {
        context.startService(ExtendedContactSaveService.createClearUsageSelectedContactsIntent(context, getSelectedItemIds(this.mSelectedFrequent)));
    }

    private long[] getSelectedItemIds(LinkedHashSet<Uri> selectedUris) {
        long[] selectedContactIDs = new long[selectedUris.size()];
        int i = 0;
        for (Uri contactUri : selectedUris) {
            if (contactUri != null) {
                int i2 = i + 1;
                selectedContactIDs[i] = ContentUris.parseId(contactUri);
                i = i2;
            }
        }
        return selectedContactIDs;
    }

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (savedState != null) {
            Iterator<String> it;
            ArrayList<String> starredList = savedState.getStringArrayList("selected_starred");
            if (starredList != null) {
                it = starredList.iterator();
                while (it.hasNext()) {
                    this.mSelectedStarred.add(Uri.withAppendedPath(Contacts.CONTENT_URI, (String) it.next()));
                }
            }
            ArrayList<String> frequentList = savedState.getStringArrayList("selected_frequent");
            if (frequentList != null) {
                it = frequentList.iterator();
                while (it.hasNext()) {
                    this.mSelectedFrequent.add(Uri.parse((String) it.next()));
                }
            }
            this.mFrequentContactIds = (ArrayList) savedState.getSerializable("frequent_contact_ids");
            this.mCursorIndexs = (int[]) savedState.getSerializable("order_cursor_indexs");
            this.mOrderChangedContactsMap = (HashMap) savedState.getSerializable("order_changed_contacts");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> starredList = new ArrayList();
        Iterator<Uri> it = this.mSelectedStarred.iterator();
        while (it.hasNext()) {
            starredList.add(((Uri) it.next()).getLastPathSegment());
        }
        outState.putStringArrayList("selected_starred", starredList);
        ArrayList<String> frequentList = new ArrayList();
        it = this.mSelectedFrequent.iterator();
        while (it.hasNext()) {
            frequentList.add(((Uri) it.next()).toString());
        }
        outState.putStringArrayList("selected_frequent", frequentList);
        outState.putSerializable("frequent_contact_ids", this.mFrequentContactIds);
        if (this.mCursor != null) {
            outState.putSerializable("order_cursor_indexs", this.mCursor.getIndexs());
        }
        outState.putSerializable("order_changed_contacts", this.mDraggedItemAfterOrderMap);
    }
}
