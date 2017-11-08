package com.android.contacts.hap.list;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.NonEmptyGroupListLoader;
import com.android.contacts.hap.activities.ContactAndGroupMultiSelectionActivity;
import com.android.contacts.hap.list.FavoritesAndGroupsAdapter.CursorDataChangeListener;
import com.android.contacts.hap.util.ActionBarCustom;
import com.android.contacts.hap.util.SelectedDataCache;
import com.android.contacts.list.ChildListItemView;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.util.ActionBarCustomTitle;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.widget.ActionBarEx;
import com.android.contacts.widget.ExpandableAutoScrollListView;
import com.google.android.gms.R;
import com.huawei.cspcommon.performance.PLog;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.android.widget.SubTabWidget.SubTabListener;
import java.util.HashSet;

public class FavoritesAndGroupsMultiSelectFragment extends ListFragment implements OnFocusChangeListener, OnTouchListener, CursorDataChangeListener, SubTabListener {
    private FavoritesAndGroupsAdapter mAdapter;
    private Context mContext;
    private ActionBarCustom mCustActionBar;
    private View mEmptyScreen;
    private ExpandableAutoScrollListView mExpandableListView;
    private HashSet<Integer> mExpandedSet = new HashSet();
    private Cursor mGroupListCursor;
    Handler mHideHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int type = msg.what;
            if (FavoritesAndGroupsMultiSelectFragment.this.mContext != null) {
                switch (type) {
                    case 1:
                        CommonUtilMethods.hideSoftKeyboard(FavoritesAndGroupsMultiSelectFragment.this.mContext, FavoritesAndGroupsMultiSelectFragment.this.getListView());
                        break;
                }
            }
        }
    };
    private final LoaderCallbacks<Cursor> mLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            PLog.d(0, "FavoritesAndGroupsMultiSelectFragment onCreateLoader");
            return new NonEmptyGroupListLoader(FavoritesAndGroupsMultiSelectFragment.this.mContext, FavoritesAndGroupsMultiSelectFragment.this.mRequest.getActionCode());
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            PLog.d(0, "FavoritesAndGroupsMultiSelectFragment onLoadFinished");
            if (!(data == null || loader == null)) {
                switch (loader.getId()) {
                    case 1:
                        FavoritesAndGroupsMultiSelectFragment.this.mGroupListCursor = data;
                        FavoritesAndGroupsMultiSelectFragment.this.mAdapter.setHasFavourites(((NonEmptyGroupListLoader) loader).hasFavouritesValue());
                        FavoritesAndGroupsMultiSelectFragment.this.mAdapter.setGroupCursor(FavoritesAndGroupsMultiSelectFragment.this.mGroupListCursor);
                        FavoritesAndGroupsMultiSelectFragment.this.mAdapter.notifyDataSetChanged();
                        FavoritesAndGroupsMultiSelectFragment.this.mLoadingView.setVisibility(8);
                        if (FavoritesAndGroupsMultiSelectFragment.this.mAdapter.getGroupCount() != 0) {
                            FavoritesAndGroupsMultiSelectFragment.this.getListView().setVisibility(0);
                            FavoritesAndGroupsMultiSelectFragment.this.mEmptyScreen.setVisibility(8);
                            break;
                        }
                        FavoritesAndGroupsMultiSelectFragment.this.getListView().setVisibility(8);
                        FavoritesAndGroupsMultiSelectFragment.this.mEmptyScreen.setVisibility(0);
                        FavoritesAndGroupsMultiSelectFragment.this.setEmptyViewLocation();
                        break;
                }
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    LinearLayout mLoadingView;
    private boolean mReCreate = false;
    private ContactsRequest mRequest;

    public void setSelectedData(SelectedDataCache cache) {
        this.mAdapter.setSelectedCache(cache);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PLog.d(0, "FavoritesAndGroupsMultiSelectFragment onCreateView begin");
        View lRootView = inflater.inflate(R.layout.group_fav_list_fragment, null);
        this.mLoadingView = (LinearLayout) lRootView.findViewById(R.id.loadingcontacts);
        setHasOptionsMenu(true);
        PLog.d(0, "FavoritesAndGroupsMultiSelectFragment onCreateView end");
        return lRootView;
    }

    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
        if (this.mAdapter != null) {
            this.mAdapter.upateSimpleDisplayMode();
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.mExpandableListView = (ExpandableAutoScrollListView) view.findViewById(R.id.list);
        this.mExpandableListView.setFastScrollEnabled(true);
        this.mExpandableListView.setOnFocusChangeListener(null);
        this.mExpandableListView.setOnTouchListener(null);
        this.mExpandableListView.setChoiceMode(2);
        this.mAdapter = new FavoritesAndGroupsAdapter(this.mContext, R.layout.group_list_item, this.mExpandableListView, this.mExpandedSet, this.mReCreate);
        this.mAdapter.setListener(this);
        this.mAdapter.setFilterType(this.mRequest.getActionCode());
        this.mExpandableListView.setGroupIndicator(null);
        this.mExpandableListView.setAdapter(this.mAdapter);
        ((ContactAndGroupMultiSelectionActivity) this.mContext).initSelectedDataCache();
        this.mExpandableListView.setOnChildClickListener(new OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                FavoritesAndGroupsMultiSelectFragment.this.mAdapter.performChildClick((ChildListItemView) v, groupPosition, childPosition);
                return false;
            }
        });
        this.mExpandableListView.setOnCreateContextMenuListener(getActivity());
        this.mEmptyScreen = view.findViewById(R.id.empty_screen_view);
    }

    public boolean onTouch(View view, MotionEvent arg1) {
        if (view == this.mExpandableListView) {
            hideSoftKeyboard();
        }
        return false;
    }

    private void hideSoftKeyboard() {
        if (this.mContext != null) {
            ((InputMethodManager) this.mContext.getSystemService("input_method")).hideSoftInputFromWindow(this.mExpandableListView.getWindowToken(), 0);
        }
    }

    public void onFocusChange(View view, boolean hasFocus) {
        if (view == this.mExpandableListView && hasFocus) {
            hideSoftKeyboard();
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
        if (!EmuiVersion.isSupportEmui() && this.mCustActionBar == null) {
            this.mCustActionBar = new ActionBarCustom(this.mContext, activity.getActionBar());
        }
    }

    public void onDetach() {
        super.onDetach();
        this.mContext = null;
    }

    public void onStart() {
        getLoaderManager().initLoader(1, null, this.mLoaderListener);
        super.onStart();
    }

    private void setEmptyViewLocation() {
        this.mEmptyScreen.setVisibility(0);
        TextView emptyTextView = (TextView) this.mEmptyScreen.findViewById(R.id.no_contacts_group);
        Activity activity = getActivity();
        if (activity != null && emptyTextView != null) {
            boolean isPor = getResources().getConfiguration().orientation == 1;
            int subTabHeight = getResources().getDimensionPixelSize(R.dimen.suspention_view_height);
            if (isPor) {
                MarginLayoutParams params = (MarginLayoutParams) emptyTextView.getLayoutParams();
                params.topMargin = CommonUtilMethods.getMarginTopPix(activity, 0.3d, isPor) - subTabHeight;
                emptyTextView.setLayoutParams(params);
            } else if (this.mEmptyScreen instanceof LinearLayout) {
                int paddingBottom = CommonUtilMethods.getActionBarAndStatusHeight(activity, isPor) + subTabHeight;
                ((LinearLayout) this.mEmptyScreen).setGravity(17);
                emptyTextView.setPadding(0, 0, 0, paddingBottom);
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("EXPANDED_SET", this.mExpandedSet);
        outState.putBoolean("RECREATE_STATE", true);
        super.onSaveInstanceState(outState);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mReCreate = savedInstanceState.getBoolean("RECREATE_STATE");
            this.mExpandedSet = (HashSet) savedInstanceState.getSerializable("EXPANDED_SET");
        }
    }

    public void setContactsRequest(ContactsRequest aRequest) {
        this.mRequest = aRequest;
    }

    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_action_selectall).setVisible(false);
        setActionBarTitle();
    }

    private void setActionBarTitle() {
        ActionBar actionBar = getActivity().getActionBar();
        ActionBarCustomTitle title = new ActionBarCustomTitle(this.mContext);
        if (EmuiVersion.isSupportEmui()) {
            ActionBarEx.setCustomTitle(actionBar, title.getTitleLayout());
        } else {
            this.mCustActionBar.setCustomTitle(title.getTitleLayout());
        }
        if (((ContactAndGroupMultiSelectionActivity) getActivity()).mSelectedDataUris.size() == 0) {
            title.setCustomTitle(getResources().getString(R.string.contacts_not_selected_text), ((ContactAndGroupMultiSelectionActivity) getActivity()).mSelectedDataUris.size());
        } else {
            title.setCustomTitle(getResources().getString(R.string.contacts_selected_text), ((ContactAndGroupMultiSelectionActivity) getActivity()).mSelectedDataUris.size());
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_cancel:
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ListView getListView() {
        return this.mExpandableListView;
    }

    private void releaseAdapter() {
        if (this.mAdapter != null) {
            this.mAdapter.releaseCursorHelpers();
            this.mAdapter.setListener(null);
            this.mAdapter.clearQueryHandler();
        }
    }

    public void onDestroy() {
        getLoaderManager().destroyLoader(1);
        super.onDestroy();
    }

    public void onDestroyView() {
        releaseAdapter();
        super.onDestroyView();
    }

    public void onCursorDataChanged() {
        if (this.mAdapter.getGroupCount() == 0) {
            getListView().setVisibility(8);
            this.mEmptyScreen.setVisibility(0);
            setEmptyViewLocation();
            return;
        }
        getListView().setVisibility(0);
        this.mEmptyScreen.setVisibility(8);
    }

    public void onSubTabReselected(SubTab subTab, FragmentTransaction ft) {
    }

    public void onSubTabSelected(SubTab subTab, FragmentTransaction ft) {
        Message msg = new Message();
        msg.what = 1;
        this.mHideHandler.sendMessage(msg);
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        }
    }

    public void setCustActionBar(ActionBarCustom custActionBar) {
        this.mCustActionBar = custActionBar;
    }

    public void onSubTabUnselected(SubTab subTab, FragmentTransaction ft) {
    }
}
