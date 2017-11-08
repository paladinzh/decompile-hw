package com.android.contacts.hap.list;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactSplitUtils;
import com.android.contacts.ContactsApplication;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactInfoFragment;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.fragment.DummyStubFragment;
import com.android.contacts.fragment.HwBaseFragment;
import com.android.contacts.fragments.NoContentFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity.TranslucentActivity;
import com.android.contacts.hap.list.FavoritesFrequentAdapter.ItemActionListener;
import com.android.contacts.hap.list.FavoritesStarredAdapter.OnActionListener;
import com.android.contacts.hap.roaming.RoamingDialPadDirectlyDataListener;
import com.android.contacts.hap.roaming.RoamingDialPadDirectlyManager;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.GenericHandler;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.hap.utils.ScreenUtils;
import com.android.contacts.hap.widget.SplitActionBarView;
import com.android.contacts.hap.widget.SplitActionBarView.OnCustomMenuListener;
import com.android.contacts.hap.widget.SplitActionBarView.SetButtonDetails;
import com.android.contacts.list.ContactNumberSelectionDialogFragment;
import com.android.contacts.list.ContactNumberSelectionDialogFragment.NumberInfo;
import com.android.contacts.list.ContactNumberSelectionDialogFragment.NumberSelectionListener;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneNumberFormatter;
import com.android.contacts.widget.AutoScrollListView;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import huawei.com.android.internal.widget.HwFragmentContainer;
import huawei.com.android.internal.widget.HwFragmentLayout;
import huawei.com.android.internal.widget.HwFragmentLayout.HwFragmentLayoutCallback;
import java.util.ArrayList;

public class FavoriteGroupFragment extends HwBaseFragment implements NumberSelectionListener {
    private static final String[] CALL_CONTACT_COLUMNS = new String[]{"data_id", "display_name", "data1", "data2", "data3", "is_super_primary", "_id", "data4"};
    private static int LOADER_ID_CONTACT_TILE = 1;
    private static int LOADER_ID_FREQUENT = 2;
    private static final String TAG = FavoriteGroupFragment.class.getSimpleName();
    RoamingDialPadDirectlyDataListener fravoriteGropDirectlyDataListener = new RoamingDialPadDirectlyDataListener() {
        public void selectedDirectlyData(String number) {
            if (!TextUtils.isEmpty(number)) {
                FavoriteGroupFragment.this.callNumber(number);
            }
        }
    };
    private FavoriteGroupMergeAdapter mAdapter;
    private View mAddFavorites;
    private int mContactQueryToken;
    private View mContentContainerView;
    private Context mContext;
    private float mDisplayRate = 0.4f;
    private View mEmptyView;
    private HwFragmentContainer mFavFrgContainer;
    private HwFragmentLayout mFavFrgLayout;
    private FavoritesFrequentAdapter mFrequentAdapter;
    private final FrequentAdapterListener mFrequentAdapterListener = new FrequentAdapterListener();
    private final LoaderCallbacks<Cursor> mFrequentLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            Uri uri = Data.CONTENT_URI.buildUpon().appendQueryParameter("limit", String.valueOf(10)).build();
            StringBuilder selection = new StringBuilder();
            ArrayList<String> selectionArgs = new ArrayList();
            selection.append("starred").append("=0").append(" AND ").append("mimetype").append("=?").append(" AND ").append("_id").append(" IN (").append("SELECT ").append("data_id").append(" FROM ").append("data_usage_stat WHERE usage_type=0").append(")");
            selectionArgs.add("vnd.android.cursor.item/phone_v2");
            return new CursorLoader(FavoriteGroupFragment.this.mContext, uri, FavoritesFrequentAdapter.getFrequentColumns(), selection.toString(), (String[]) selectionArgs.toArray(new String[selectionArgs.size()]), "times_used DESC,last_time_used DESC");
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (HwLog.HWFLOW) {
                HwLog.i(FavoriteGroupFragment.TAG, "Frequnet load finished start");
            }
            FavoriteGroupFragment.this.mIsFrequentLoaded = true;
            if (data != null) {
                if (FavoriteGroupFragment.this.mFrequentAdapter == null) {
                    FavoriteGroupFragment.this.mFrequentAdapter = new FavoritesFrequentAdapter(FavoriteGroupFragment.this.mContext);
                }
                FavoriteGroupFragment.this.mFrequentAdapter.setSelectionVisible(FavoriteGroupFragment.this.mIsNeedShowSelect);
                FavoriteGroupFragment.this.mFrequentAdapter.setCursor(data);
                FavoriteGroupFragment.this.setupFavoriteAdapter();
                FavoriteGroupFragment.this.handleEmptyList();
                if (HwLog.HWFLOW) {
                    HwLog.i(FavoriteGroupFragment.TAG, "Frequnet load finished end");
                }
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private boolean mIsCurrentShow = true;
    private boolean mIsFavoritesLoaded;
    private boolean mIsFrequentLoaded;
    private boolean mIsFromNewIntent;
    private boolean mIsHasSaveState = false;
    private boolean mIsListEmpty = true;
    private boolean mIsNeedShowSelect = false;
    private boolean mIsNotShowAnimator = false;
    private boolean mIsPortrait;
    private ListView mListView;
    private TextView mNoFavTextView;
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            FavoriteGroupFragment.this.launchAddFavorites();
        }
    };
    private String mSelectFrequentNumber = null;
    private Uri mSelectFrequentUri = null;
    private int mSelectLastPos = -1;
    private Uri mSelectStarredUri = null;
    private final OnActionListener mStarredActionListener = new StarredActionListener();
    private FavoritesStarredAdapter mStarredAdapter;
    private final LoaderCallbacks<Cursor> mStarredLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            return new CursorLoader(FavoriteGroupFragment.this.mContext, Contacts.CONTENT_URI, FavoritesStarredAdapter.getStarredColumns(), "starred=?", new String[]{CallInterceptDetails.BRANDED_STATE}, "starred_order ASC");
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (HwLog.HWFLOW) {
                HwLog.i(FavoriteGroupFragment.TAG, "Starred load finished start");
            }
            FavoriteGroupFragment.this.mIsFavoritesLoaded = true;
            if (data != null) {
                ((ContactsApplication) FavoriteGroupFragment.this.getActivity().getApplication()).setLaunchProgress(1);
                if (FavoriteGroupFragment.this.mStarredAdapter == null) {
                    FavoriteGroupFragment.this.mStarredAdapter = new FavoritesStarredAdapter(FavoriteGroupFragment.this.mContext, data);
                    FavoriteGroupFragment.this.mStarredAdapter.setOnActionListener(FavoriteGroupFragment.this.mStarredActionListener);
                }
                FavoriteGroupFragment.this.mStarredAdapter.setSelectionVisible(FavoriteGroupFragment.this.mIsNeedShowSelect);
                FavoriteGroupFragment.this.mStarredAdapter.setCursor(data);
                FavoriteGroupFragment.this.mStarredAdapter.notifyDataSetChanged();
                FavoriteGroupFragment.this.setupFavoriteAdapter();
                FavoriteGroupFragment.this.handleEmptyList();
                if (HwLog.HWFLOW) {
                    HwLog.i(FavoriteGroupFragment.TAG, "Starred load finished end");
                }
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    private class ContactQueryHandler extends AsyncQueryHandler {
        private Uri mUri;

        public ContactQueryHandler(ContentResolver cr, Uri uri) {
            super(cr);
            this.mUri = uri;
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            try {
                FavoriteGroupFragment.this.callContact(cursor, this.mUri);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private class FrequentAdapterListener implements ItemActionListener {
        private FrequentAdapterListener() {
        }

        public void onViewContact(Uri contactUri, String number) {
            FavoriteGroupFragment.this.viewContactDetail(contactUri, false, number);
        }

        public void onItmeClick(Uri contactUri, String number) {
            FavoriteGroupFragment.this.mIsNotShowAnimator = true;
            FavoriteGroupFragment.this.refreshFavoriteParamOrViewContact(contactUri, false, number);
            FavoriteGroupFragment.this.mIsNotShowAnimator = false;
        }

        public void onCallNumber(String number) {
            FavoriteGroupFragment.this.callNumber(number);
        }

        public void onViewLongClicked(Uri contactUri) {
            FavoriteGroupFragment.this.launchEditFragment(contactUri, 1);
        }
    }

    private class StarredActionListener implements OnActionListener {
        private StarredActionListener() {
        }

        public void onContactClicked(Uri contactUri) {
            StatisticalHelper.report(1128);
            FavoriteGroupFragment.this.queryAndCallContactNumers(contactUri);
        }

        public void onViewContactDetail(Uri contactLookupUri) {
            FavoriteGroupFragment.this.viewContactDetail(contactLookupUri, true, null);
        }

        public void onContactLongClicked(Uri contactUri) {
            FavoriteGroupFragment.this.launchEditFragment(contactUri, 0);
        }
    }

    public FavoriteGroupFragment(GenericHandler aGenHdlr, boolean aDelayIfRequired) {
    }

    private void handleEmptyList() {
        if (this.mIsFavoritesLoaded && this.mIsFrequentLoaded) {
            if (this.mStarredAdapter.getCount() + this.mFrequentAdapter.getCount() > 0) {
                this.mContentContainerView.setVisibility(0);
                if (this.mEmptyView != null) {
                    this.mEmptyView.setVisibility(8);
                }
                this.mIsListEmpty = false;
                handleSplitScreenInitFragment(false);
            } else {
                this.mContentContainerView.setVisibility(8);
                if (this.mEmptyView == null) {
                    ((ViewStub) getView().findViewById(R.id.no_favorites_layout_container)).inflate();
                    this.mEmptyView = getView().findViewById(R.id.no_favorites_screen_layout);
                    this.mAddFavorites = (Button) getView().findViewById(R.id.btn_add_favorites);
                    updateButtonLayout(this.mAddFavorites);
                    this.mAddFavorites.setOnClickListener(this.mOnClickListener);
                    this.mNoFavTextView = (TextView) getView().findViewById(R.id.text_no_contacts);
                }
                this.mEmptyView.setVisibility(0);
                setEmptyContactLocation();
                this.mIsListEmpty = true;
                handleSplitScreenInitFragment(true);
            }
            getActivity().invalidateOptionsMenu();
        }
    }

    private void setEmptyContactLocation() {
        Activity activity = getActivity();
        TextView tvNoContactsView = (TextView) this.mEmptyView.findViewById(R.id.text_no_contacts);
        if (activity != null && tvNoContactsView != null) {
            boolean isPor = getResources().getConfiguration().orientation == 1;
            MarginLayoutParams params = (MarginLayoutParams) tvNoContactsView.getLayoutParams();
            if (isPor) {
                params.topMargin = CommonUtilMethods.getMarginTopPix(activity, 0.3d, isPor);
                tvNoContactsView.setLayoutParams(params);
                return;
            }
            ((RelativeLayout) this.mEmptyView.findViewById(R.id.favorites_no_contacts)).setGravity(17);
            int btnAddFavHeight = getResources().getDimensionPixelSize(R.dimen.no_favorite_layout_btn_add_favorites_margin_bottom) + getResources().getDimensionPixelSize(R.dimen.no_contacts_empty_button_height);
            int actionBarHeight = CommonUtilMethods.getActionBarAndStatusHeight(activity, isPor);
            int paddingTop = 0;
            int paddingBottom = 0;
            if (btnAddFavHeight > actionBarHeight) {
                paddingTop = btnAddFavHeight - actionBarHeight;
            } else {
                paddingBottom = actionBarHeight - btnAddFavHeight;
            }
            tvNoContactsView.setPadding(0, paddingTop, 0, paddingBottom);
        }
    }

    private void updateButtonLayout(View view) {
        if (isNeedShowSplit()) {
            int width = 0;
            int marrgin = 0;
            Context context = getActivity();
            if (isTwoColumnSplit()) {
                marrgin = ContactDpiAdapter.getNewPxDpi(R.dimen.no_contacts_button_margin_split, context);
            } else {
                width = ContactDpiAdapter.getNewPxDpi(R.dimen.no_contacts_empty_button_width, context);
            }
            ScreenUtils.updateButtonView(context, view, marrgin, width);
        }
    }

    private boolean isInMainPage() {
        if (this.mFavFrgContainer.getSelectedContainer() == 0 || this.mFavFrgContainer.getSelectedContainer() == -1) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onPageSelect() {
        if (!(!isOneColumnSplit() || this.mFavFrgContainer == null || isInMainPage())) {
            showInitInfoForSplit(-2);
            this.mFavFrgContainer.refreshFragmentLayout();
        }
    }

    private boolean isNeedShowSplit() {
        if (!CommonUtilMethods.calcIfNeedSplitScreen() || this.mFavFrgContainer == null) {
            return false;
        }
        return true;
    }

    private void showEmptyFragmentIfSplit() {
        if (((NoContentFragment) getChildFragmentManager().findFragmentByTag(NoContentFragment.class.getName())) == null) {
            initRightContainer(new NoContentFragment());
        }
        this.mSelectLastPos = -1;
        this.mSelectStarredUri = null;
        this.mSelectFrequentUri = null;
        this.mSelectFrequentNumber = null;
    }

    private void launchEditFragment(Uri contactUri, int contactType) {
        Intent editIntent = new Intent("android.intent.action.HAP_REMOVE_FAVORITES");
        setWindowWidthForIntent(editIntent);
        editIntent.putExtra("contact_multi_select_for_stequent", true);
        if (contactUri != null) {
            editIntent.putExtra("SELECTED_CONTACT_LOOKUP_URI", contactUri.toString());
        }
        editIntent.putExtra("SELECTED_CONTACT_TYPE_KEY", contactType);
        editIntent.putExtra("FIRST_VISIBLE_POSITION", this.mListView.getFirstVisiblePosition());
        if (this.mFrequentAdapter.getCount() > 0) {
            editIntent.putExtra("extra_frequent_contact_ids", getFrequentIds());
        }
        startActivity(editIntent);
        StatisticalHelper.report(2011);
        ExceptionCapture.reportScene(16);
        getActivity().overridePendingTransition(0, 0);
        StatisticalHelper.report(4049);
    }

    private void viewContactDetail(Uri contactLookupUri, boolean isFavorite, String number) {
        Activity activity = getActivity();
        Intent intent = new Intent("android.intent.action.VIEW", contactLookupUri);
        intent.setClass(activity, ContactDetailActivity.class);
        if (isNeedShowSplit()) {
            if (isFavorite) {
                if (isTwoColumnSplit() && contactLookupUri != null && contactLookupUri.equals(this.mSelectStarredUri)) {
                    this.mFavFrgContainer.setSelectedContainer(1);
                    return;
                }
                refreshSplitSelectionParam(true, contactLookupUri, null);
            } else if (isTwoColumnSplit() && contactLookupUri != null && contactLookupUri.equals(this.mSelectFrequentUri) && number.equals(this.mSelectFrequentNumber)) {
                this.mFavFrgContainer.setSelectedContainer(1);
                return;
            } else {
                refreshSplitSelectionParam(false, contactLookupUri, number);
            }
            if (this.mIsNeedShowSelect) {
                this.mListView.invalidateViews();
            }
            if (isAdded()) {
                if (this.mIsNotShowAnimator) {
                    ContactInfoFragment frag = (ContactInfoFragment) getChildFragmentManager().findFragmentByTag(ContactInfoFragment.class.getName());
                    if (frag != null) {
                        frag.setNoAnimator(true);
                    }
                }
                ContactInfoFragment contactInfo = new ContactInfoFragment();
                contactInfo.setIntent(intent);
                if (this.mIsNotShowAnimator) {
                    contactInfo.setNoAnimator(true);
                }
                openRightContainer(contactInfo);
            }
            return;
        }
        activity.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (CommonUtilMethods.calcIfNeedSplitScreen() && savedInstanceState == null && (getActivity() instanceof PeopleActivity)) {
            savedInstanceState = CommonUtilMethods.getInstanceState();
        }
        if (savedInstanceState != null) {
            Fragment numberSelectionFragment = getFragmentManager().findFragmentByTag("number_selection_dialog");
            if (numberSelectionFragment != null && (numberSelectionFragment instanceof ContactNumberSelectionDialogFragment)) {
                ((ContactNumberSelectionDialogFragment) numberSelectionFragment).setListener(this);
            }
            this.mIsHasSaveState = true;
            this.mDisplayRate = savedInstanceState.getFloat("display_rate", 0.4f);
            this.mSelectStarredUri = (Uri) savedInstanceState.getParcelable("key_selected_starr_uri");
            this.mSelectFrequentUri = (Uri) savedInstanceState.getParcelable("key_selected_freq_uri");
            this.mSelectFrequentNumber = savedInstanceState.getString("key_selected_freq_num");
            this.mSelectLastPos = savedInstanceState.getInt("key_selected_last_pos");
        }
    }

    public void onAttach(Activity activity) {
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "onAttach()");
        }
        super.onAttach(activity);
        this.mContext = activity;
        getLoaderManager().initLoader(LOADER_ID_CONTACT_TILE, null, this.mStarredLoaderListener);
        this.mIsFavoritesLoaded = false;
        getLoaderManager().initLoader(LOADER_ID_FREQUENT, null, this.mFrequentLoaderListener);
        this.mIsFrequentLoaded = false;
        ContactPhotoManager photoManger = ContactPhotoManager.getInstance(getActivity());
        this.mFrequentAdapter = new FavoritesFrequentAdapter(this.mContext);
        this.mFrequentAdapter.setPhotoManager(photoManger);
        this.mFrequentAdapter.setItemActionListener(this.mFrequentAdapterListener);
        this.mStarredAdapter = new FavoritesStarredAdapter(this.mContext, null);
        this.mStarredAdapter.setOnActionListener(this.mStarredActionListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View listLayout;
        if (CommonUtilMethods.calcIfNeedSplitScreen() && (getActivity() instanceof PeopleActivity)) {
            listLayout = inflater.inflate(R.layout.favorite_split_fragment, container, false);
            this.mFavFrgLayout = (HwFragmentLayout) listLayout.findViewById(R.id.split_layout_fav);
            this.mFavFrgContainer = new HwFragmentContainer(this.mContext, this.mFavFrgLayout, getChildFragmentManager());
            if (((DummyStubFragment) getChildFragmentManager().findFragmentByTag(DummyStubFragment.class.getName())) == null) {
                this.mFavFrgContainer.openLeftClearStack(new DummyStubFragment());
            }
            this.mFavFrgContainer.setDisplayRate(0.5f);
            this.mDisplayRate = 0.5f;
            this.mFavFrgContainer.setSelectContainerByTouch(true);
            this.mFavFrgContainer.setSplitMode(3);
            View subView = inflater.inflate(R.layout.favorite_group_contact_tile_list, container, false);
            ScreenUtils.adjustPaddingTop(getActivity(), (ViewGroup) subView.findViewById(R.id.favorite_group_contact_tile_list), true);
            this.mFavFrgContainer.getLeftLayout().addView(subView);
            View view = this.mFavFrgContainer.getSplitLine();
            LayoutParams mSplitLineParams = (LayoutParams) view.getLayoutParams();
            mSplitLineParams.topMargin = ContactDpiAdapter.getActivityPaddingHeight(getActivity());
            mSplitLineParams.height = -1;
            mSplitLineParams.addRule(12);
            view.setLayoutParams(mSplitLineParams);
            if (isTwoColumnSplit()) {
                View bgview = this.mFavFrgContainer.getRightBlurLayer();
                FrameLayout.LayoutParams rightParams = (FrameLayout.LayoutParams) bgview.getLayoutParams();
                rightParams.topMargin = ContactDpiAdapter.getStatusBarHeight(getActivity());
                rightParams.height = -1;
                bgview.setLayoutParams(rightParams);
            }
            this.mFavFrgContainer.setCanMove(false);
            this.mFavFrgLayout.setFragmentLayoutCallback(new HwFragmentLayoutCallback() {
                public void setDisplayRate(float displayRate) {
                    FavoriteGroupFragment.this.mDisplayRate = displayRate;
                }
            });
            if (HwLog.HWDBG) {
                HwLog.d(TAG, "FavoriteGroupFragment init split frame");
            }
        } else {
            listLayout = inflater.inflate(R.layout.favorite_group_contact_tile_list, container, false);
        }
        this.mIsPortrait = getResources().getConfiguration().orientation == 1;
        if (isNeedShowSplit()) {
            SetButtonDetails menuAdd = new SetButtonDetails(R.drawable.ic_new_contact, R.string.contact_menu_add_favorites);
            SetButtonDetails menuRemove = new SetButtonDetails(R.drawable.contacts_ic_edit, R.string.menu_editContact);
            this.mSplitActionBarView = (SplitActionBarView) listLayout.findViewById(R.id.menu_view);
            this.mSplitActionBarView.setVisibility(0);
            this.mSplitActionBarView.fillDetails(menuAdd, null, null, menuRemove, false);
            this.mSplitActionBarView.setOnCustomMenuListener(new OnCustomMenuListener() {
                public boolean onCustomSplitMenuItemClick(int aMenuItem) {
                    switch (aMenuItem) {
                        case R.string.menu_editContact:
                            FavoriteGroupFragment.this.launchEditFragment(null, -1);
                            break;
                        case R.string.contact_menu_add_favorites:
                            FavoriteGroupFragment.this.launchAddFavorites();
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
        this.mListView = (ListView) listLayout.findViewById(R.id.contact_tile_list);
        this.mListView.setItemsCanFocus(true);
        CommonUtilMethods.addFootEmptyViewPortrait(this.mListView, this.mContext);
        this.mListView.setFastScrollEnabled(true);
        this.mContentContainerView = listLayout.findViewById(R.id.contact_favorites_content_container);
        if (isTwoColumnSplit()) {
            this.mIsNeedShowSelect = true;
        }
        return listLayout;
    }

    public void onResume() {
        super.onResume();
        if (isNeedShowSplit()) {
            if (this.mIsFromNewIntent && this.mIsFavoritesLoaded && this.mIsFrequentLoaded) {
                boolean isEmpty;
                if (this.mStarredAdapter.getCount() + this.mFrequentAdapter.getCount() > 0) {
                    isEmpty = false;
                } else {
                    isEmpty = true;
                }
                handleSplitScreenInitFragment(isEmpty);
                this.mIsFromNewIntent = false;
            } else {
                reloadContactForUriChanged();
            }
            this.mFavFrgContainer.refreshFragmentLayout();
            showOrHideActionbar();
        }
        this.mListView.invalidateViews();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("key_selected_starr_uri", this.mSelectStarredUri);
        outState.putFloat("display_rate", this.mDisplayRate);
        outState.putParcelable("key_selected_freq_uri", this.mSelectFrequentUri);
        outState.putString("key_selected_freq_num", this.mSelectFrequentNumber);
        outState.putInt("key_selected_last_pos", this.mSelectLastPos);
    }

    public void setFromNewIntent(boolean isNewIntent) {
        this.mIsFromNewIntent = isNewIntent;
    }

    private void handleSplitScreenInitFragment(boolean isEmpty) {
        if (isNeedShowSplit() && this.mStarredAdapter != null && this.mFrequentAdapter != null) {
            if (isEmpty) {
                showEmptyFragmentIfSplit();
            } else if (!this.mIsHasSaveState || this.mIsFromNewIntent) {
                if (this.mIsHasSaveState) {
                    showInitInfoForSplit(this.mSelectLastPos);
                } else {
                    showInitInfoForSplit(0);
                }
                this.mIsHasSaveState = true;
                this.mIsFromNewIntent = false;
            } else {
                int starredPos = this.mStarredAdapter.getSelectUriPos(this.mSelectStarredUri);
                Intent intent;
                ContactInfoFragment contactInfo;
                if (starredPos != -1) {
                    this.mStarredAdapter.setSelectUri(this.mSelectStarredUri);
                    this.mSelectFrequentUri = null;
                    this.mSelectFrequentNumber = null;
                    this.mSelectLastPos = starredPos;
                    if (((ContactInfoFragment) getChildFragmentManager().findFragmentByTag(ContactInfoFragment.class.getName())) == null) {
                        intent = new Intent("android.intent.action.VIEW", this.mSelectStarredUri);
                        contactInfo = new ContactInfoFragment();
                        contactInfo.setIntent(intent);
                        initRightContainer(contactInfo);
                    }
                    if (this.mIsNeedShowSelect) {
                        requestSelectionToScreen(starredPos + 1, false);
                        this.mListView.invalidateViews();
                    }
                    return;
                }
                int freqPos = this.mFrequentAdapter.getSelectUriPos(this.mSelectFrequentUri, this.mSelectFrequentNumber);
                if (freqPos != -1) {
                    this.mFrequentAdapter.setSelectUri(this.mSelectFrequentUri, this.mSelectFrequentNumber);
                    this.mSelectStarredUri = null;
                    this.mSelectLastPos = this.mStarredAdapter.getCount() + freqPos;
                    if (((ContactInfoFragment) getChildFragmentManager().findFragmentByTag(ContactInfoFragment.class.getName())) == null) {
                        intent = new Intent("android.intent.action.VIEW", this.mSelectFrequentUri);
                        contactInfo = new ContactInfoFragment();
                        contactInfo.setIntent(intent);
                        initRightContainer(contactInfo);
                    }
                    if (this.mIsNeedShowSelect) {
                        int movePos = this.mSelectLastPos + 1;
                        if (this.mStarredAdapter.getCount() > 0) {
                            movePos++;
                        }
                        requestSelectionToScreen(movePos, false);
                        this.mListView.invalidateViews();
                    }
                    return;
                }
                showInitInfoForSplit(this.mSelectLastPos);
            }
            showOrHideActionbar();
        }
    }

    private void requestSelectionToScreen(int selectedPosition, boolean isSmooth) {
        AutoScrollListView listView = this.mListView;
        listView.requestPositionToScreen(listView.getHeaderViewsCount() + selectedPosition, isSmooth);
    }

    private void showInitInfoForSplit(int initpos) {
        int selectpos = initpos;
        if (initpos < 0) {
            selectpos = 0;
        }
        int count = this.mStarredAdapter.getCount() + this.mFrequentAdapter.getCount();
        if (count > 0) {
            Uri uri;
            int pos = selectpos;
            if (selectpos >= count) {
                pos = count - 1;
            }
            int starredCount = this.mStarredAdapter.getCount();
            if (starredCount > pos) {
                uri = this.mStarredAdapter.getItmeContactUri(pos);
                this.mStarredAdapter.setSelectUri(uri);
                this.mFrequentAdapter.setSelectUri(null, null);
                this.mSelectStarredUri = uri;
                this.mSelectFrequentUri = null;
                this.mSelectFrequentNumber = null;
            } else {
                uri = this.mFrequentAdapter.getItmeContactUri(pos - starredCount);
                String number = this.mFrequentAdapter.getItmeContactNmuber(pos - starredCount);
                this.mStarredAdapter.setSelectUri(null);
                this.mFrequentAdapter.setSelectUri(uri, number);
                this.mSelectStarredUri = null;
                this.mSelectFrequentUri = uri;
                this.mSelectFrequentNumber = number;
            }
            if (uri != null) {
                boolean isNowLoad = true;
                if (isOneColumnSplit() && isInMainPage()) {
                    isNowLoad = false;
                }
                if (-2 == initpos) {
                    isNowLoad = true;
                }
                if (isNowLoad) {
                    Intent intent = new Intent("android.intent.action.VIEW", uri);
                    ContactInfoFragment contactInfo = new ContactInfoFragment();
                    contactInfo.setIntent(intent);
                    initRightContainer(contactInfo);
                }
                if (this.mIsNeedShowSelect) {
                    int movePos = pos + 1;
                    if (starredCount > 0) {
                        movePos++;
                    }
                    requestSelectionToScreen(movePos, false);
                    this.mListView.invalidateViews();
                }
                this.mSelectLastPos = pos;
                return;
            }
            HwLog.e(TAG, " showInitInfo the contact uri is null!");
        } else if (-2 == initpos) {
            this.mFavFrgContainer.setSelectedContainer(0);
        }
    }

    private void setupFavoriteAdapter() {
        if (this.mStarredAdapter == null || this.mFrequentAdapter == null) {
            HwLog.e(TAG, "mStarredAdapter:" + this.mStarredAdapter + "  mFrequentAdapter:" + this.mFrequentAdapter);
        } else if (this.mAdapter == null) {
            this.mAdapter = new FavoriteGroupMergeAdapter(getActivity(), this.mStarredAdapter, this.mFrequentAdapter);
            this.mListView.setAdapter(this.mAdapter);
        }
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        }
    }

    public void initRightContainer(Fragment fragment) {
        if (this.mFavFrgContainer != null && fragment != null) {
            this.mFavFrgContainer.initRightContainer(fragment);
            if (isOneColumnSplit()) {
                this.mFavFrgContainer.refreshFragmentLayout();
            }
        }
    }

    public void openRightContainer(Fragment fragment) {
        if (this.mFavFrgContainer != null && fragment != null) {
            if (isOneColumnSplit()) {
                showOrHideActionbar(false);
            }
            this.mFavFrgContainer.openRightClearStack(fragment);
        }
    }

    public void changeRightContainer(Fragment now_fragment, Fragment next_fragment) {
        if (this.mFavFrgContainer != null && next_fragment != null) {
            this.mFavFrgContainer.changeRightAddToStack(next_fragment, now_fragment);
        }
    }

    public HwFragmentContainer getFragmentContainer() {
        return this.mFavFrgContainer;
    }

    public void showOrHideActionbar() {
        Activity act = getActivity();
        if (act instanceof PeopleActivity) {
            ((PeopleActivity) act).showOrHideActionbar();
        }
    }

    public void showOrHideActionbar(boolean isShow) {
        Activity act = getActivity();
        if (act instanceof PeopleActivity) {
            ((PeopleActivity) act).showOrHideActionbar(isShow);
        }
    }

    private boolean isOneColumnSplit() {
        return !ContactSplitUtils.isSpiltTwoColumn(getActivity(), getActivity().isInMultiWindowMode());
    }

    private boolean isTwoColumnSplit() {
        return ContactSplitUtils.isSpiltTwoColumn(getActivity(), getActivity().isInMultiWindowMode());
    }

    public boolean isBackPressed() {
        if (this.mFavFrgContainer != null) {
            return this.mFavFrgContainer.isBackPressed();
        }
        return true;
    }

    public float getDisplayRate(boolean bLandscape) {
        int i = 2;
        if (this.mFavFrgContainer == null) {
            return 0.0f;
        }
        float f;
        int appWidth = 2 == getResources().getConfiguration().orientation ? bLandscape ? getActivity().getWindow().getDecorView().getWidth() : getActivity().getWindow().getDecorView().getHeight() : bLandscape ? getActivity().getWindow().getDecorView().getHeight() : getActivity().getWindow().getDecorView().getWidth();
        HwFragmentContainer hwFragmentContainer = this.mFavFrgContainer;
        if (!bLandscape) {
            i = 1;
        }
        if (hwFragmentContainer.getColumnsNumber(i, appWidth) > 1) {
            f = this.mDisplayRate;
        } else {
            f = 0.0f;
        }
        return f;
    }

    private void refreshSplitSelectionParam(boolean isFavorite, Uri contactLookupUri, String number) {
        if (isFavorite) {
            this.mStarredAdapter.setSelectUri(contactLookupUri);
            if (this.mFrequentAdapter != null) {
                this.mFrequentAdapter.setSelectUri(null, null);
            }
            StatisticalHelper.report(1129);
            this.mSelectStarredUri = contactLookupUri;
            this.mSelectFrequentUri = null;
            this.mSelectFrequentNumber = null;
            this.mSelectLastPos = this.mStarredAdapter.getSelectUriPos(contactLookupUri);
            return;
        }
        this.mFrequentAdapter.setSelectUri(contactLookupUri, number);
        this.mSelectStarredUri = null;
        this.mSelectFrequentUri = contactLookupUri;
        this.mSelectFrequentNumber = number;
        this.mSelectLastPos = this.mFrequentAdapter.getSelectUriPos(contactLookupUri, number);
        if (this.mStarredAdapter != null) {
            this.mStarredAdapter.setSelectUri(null);
            this.mSelectLastPos += this.mStarredAdapter.getCount();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void reloadContactForUriChanged() {
        ContactInfoFragment frag = (ContactInfoFragment) getChildFragmentManager().findFragmentByTag(ContactInfoFragment.class.getName());
        if (frag != null) {
            Uri lookupUri = frag.getLookupUri();
            Uri selectUri = this.mSelectStarredUri != null ? this.mSelectStarredUri : this.mSelectFrequentUri;
            if (!(lookupUri == null || selectUri == null || lookupUri.equals(selectUri))) {
                Intent intent = new Intent("android.intent.action.VIEW", selectUri);
                ContactInfoFragment contactInfo = new ContactInfoFragment();
                contactInfo.setIntent(intent);
                initRightContainer(contactInfo);
            }
        }
    }

    private void refreshFavoriteParamOrViewContact(Uri uri, boolean isFav, String number) {
        if (isNeedShowSplit()) {
            if (isTwoColumnSplit()) {
                viewContactDetail(uri, isFav, number);
            } else {
                refreshSplitSelectionParam(isFav, uri, number);
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(LOADER_ID_CONTACT_TILE);
        getLoaderManager().destroyLoader(LOADER_ID_FREQUENT);
    }

    public void setCurrentIsShow(boolean isShow) {
        this.mIsCurrentShow = isShow;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Context context = getActivity().getApplicationContext();
        inflater.inflate(R.menu.favorite_contacts, menu);
        ImmersionUtils.setImmersionMommonMenu(getActivity(), menu.findItem(R.id.favorites_menu_add_sharred_members));
        ImmersionUtils.setImmersionMommonMenu(getActivity(), menu.findItem(R.id.favorites_menu_edit_starred_member));
        ViewUtil.setMenuItemStateListIcon(context, menu.findItem(R.id.favorites_menu_add_sharred_members));
        ViewUtil.setMenuItemStateListIcon(context, menu.findItem(R.id.favorites_menu_edit_starred_member));
    }

    public void onPrepareOptionsMenu(Menu menu) {
        boolean z = false;
        boolean areContactsAvailable = false;
        if (getActivity() instanceof PeopleActivity) {
            areContactsAvailable = ((PeopleActivity) getActivity()).areContactsAvailable();
        }
        if (isNeedShowSplit() || !r0) {
            menu.setGroupVisible(R.id.favorite_options, false);
        } else {
            if (!this.mIsListEmpty) {
                z = this.mIsCurrentShow;
            }
            menu.setGroupVisible(R.id.favorite_options, z);
        }
        super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorites_menu_add_sharred_members:
                launchAddFavorites();
                return true;
            case R.id.favorites_menu_edit_starred_member:
                launchEditFragment(null, -1);
                return true;
            default:
                return false;
        }
    }

    private void setWindowWidthForIntent(Intent intent) {
        if (isNeedShowSplit() && (getActivity() instanceof PeopleActivity)) {
            intent.putExtra("landscape_window_width", getDisplayRate(true));
            intent.putExtra("vertical_window_width", getDisplayRate(false));
            intent.putExtra("gravity_left", 1);
            intent.setClass(getApplicationContext(), TranslucentActivity.class);
            ((PeopleActivity) getActivity()).saveInstanceValues();
        }
    }

    private void launchAddFavorites() {
        Intent multiselectIntent = new Intent();
        setWindowWidthForIntent(multiselectIntent);
        multiselectIntent.setAction("android.intent.action.HAP_ADD_FAVORITES");
        multiselectIntent.putExtra("favorite_from_widget", true);
        multiselectIntent.addFlags(67108864);
        startActivity(multiselectIntent);
        StatisticalHelper.report(2010);
        ExceptionCapture.reportScene(13);
    }

    private ArrayList<Integer> getFrequentIds() {
        ArrayList<Integer> contactIds = new ArrayList();
        Cursor freCursor = this.mFrequentAdapter.getCursor();
        if (freCursor == null || freCursor.getCount() <= 0) {
            return contactIds;
        }
        freCursor.moveToFirst();
        do {
            contactIds.add(Integer.valueOf(freCursor.getInt(9)));
        } while (freCursor.moveToNext());
        return contactIds;
    }

    private void queryAndCallContactNumers(Uri uri) {
        String[] selectionArgs = new String[]{"vnd.android.cursor.item/phone_v2"};
        ContactQueryHandler contactQueryHandler = new ContactQueryHandler(getActivity().getContentResolver(), uri);
        int i = this.mContactQueryToken;
        this.mContactQueryToken = i + 1;
        contactQueryHandler.startQuery(i, null, Uri.withAppendedPath(uri, "entities"), CALL_CONTACT_COLUMNS, "mimetype=?", selectionArgs, null);
    }

    private void callContact(Cursor cursor, Uri uri) {
        if (cursor == null || cursor.getCount() == 0) {
            refreshFavoriteParamOrViewContact(uri, true, null);
            Toast.makeText(getActivity(), getString(R.string.contact_has_no_available_numbers), 0).show();
            return;
        }
        ArrayList<NumberInfo> numberInfoList = new ArrayList();
        getSelectedContactInfo(cursor, numberInfoList);
        if (numberInfoList.size() > 1) {
            String defaultNumber = getDefaultNumber(cursor);
            if (defaultNumber != null) {
                callRoamingDirectlyNumber(this.mContext, PhoneNumberFormatter.formatNumber(getActivity(), PhoneNumberFormatter.parsePhoneNumber(defaultNumber)), cursor.getString(7));
            } else {
                ContactNumberSelectionDialogFragment.show(getFragmentManager(), numberInfoList, this);
                refreshFavoriteParamOrViewContact(uri, true, null);
                StatisticalHelper.report(1163);
                return;
            }
        } else if (numberInfoList.size() == 1) {
            callRoamingDirectlyNumber(this.mContext, ((NumberInfo) numberInfoList.get(0)).number, ((NumberInfo) numberInfoList.get(0)).normalizedNumber);
        }
        this.mIsNotShowAnimator = true;
        refreshFavoriteParamOrViewContact(uri, true, null);
        this.mIsNotShowAnimator = false;
    }

    private void callRoamingDirectlyNumber(Context context, String number, String normalizedNumber) {
        String dialNumber = RoamingDialPadDirectlyManager.getRoamingDialNumber(context, number, normalizedNumber, this.fravoriteGropDirectlyDataListener);
        if (!TextUtils.isEmpty(dialNumber)) {
            callNumber(dialNumber);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getSelectedContactInfo(Cursor cursor, ArrayList<NumberInfo> numberInfoList) {
        if (cursor != null && cursor.getCount() >= 1 && cursor.moveToFirst()) {
            ArrayList<String> numberList = new ArrayList();
            do {
                String number = PhoneNumberFormatter.formatNumber(getActivity(), PhoneNumberFormatter.parsePhoneNumber(cursor.getString(2)));
                if (!(numberList.contains(number) || TextUtils.isEmpty(number))) {
                    numberList.add(number);
                    int phoneType = cursor.getInt(3);
                    String customPhoneLabel = cursor.getString(4);
                    long dataId = cursor.getLong(0);
                    NumberInfo numberInfo = new NumberInfo();
                    numberInfo.number = number;
                    numberInfo.dataId = dataId;
                    numberInfo.phoneType = phoneType;
                    numberInfo.customPhoneLabel = customPhoneLabel;
                    numberInfo.normalizedNumber = cursor.getString(7);
                    numberInfoList.add(numberInfo);
                }
            } while (cursor.moveToNext());
        }
    }

    private void callNumber(String number) {
        if (!TextUtils.isEmpty(number) && isAdded()) {
            Uri numberUri = Uri.fromParts("tel", number, null);
            Context activity = getActivity();
            if (SimFactoryManager.isDualSim()) {
                startActivity(new Intent("com.android.contacts.action.CHOOSE_SUB", numberUri));
                if (activity != null) {
                    activity.overridePendingTransition(0, 0);
                }
            } else {
                CommonUtilMethods.dialNumber(activity, numberUri, 0, true, false);
            }
        }
    }

    private String getDefaultNumber(Cursor numberCursor) {
        if (numberCursor == null) {
            return null;
        }
        String defaultNumber = null;
        numberCursor.moveToFirst();
        while (numberCursor.getInt(5) == 0) {
            if (!numberCursor.moveToNext()) {
                break;
            }
        }
        defaultNumber = numberCursor.getString(2);
        return defaultNumber;
    }

    public void onNumberSelected(NumberInfo numberInfo, boolean setToDefault) {
        callRoamingDirectlyNumber(this.mContext, numberInfo.number, numberInfo.normalizedNumber);
        if (setToDefault) {
            getActivity().startService(ContactSaveService.createSetSuperPrimaryIntent(getActivity().getApplicationContext(), numberInfo.dataId, false));
            StatisticalHelper.report(1164);
        }
    }
}
