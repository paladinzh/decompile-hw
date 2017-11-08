package com.android.contacts.list;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.common.widget.CompositeCursorAdapter.Partition;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.ContactListEmptyView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.activities.ActionBarAdapter.TabState;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.compatibility.DirectoryCompat;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.fragment.HwBaseFragment;
import com.android.contacts.hap.AccountCacheUpdatedListener;
import com.android.contacts.hap.AccountsDataManager;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.activities.ContactAndGroupMultiSelectionActivity;
import com.android.contacts.hap.optimize.BackgroundCacheHdlr;
import com.android.contacts.hap.rcs.list.RcsContactEntryListFragment;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.hap.widget.AlphaIndexerPinnedHeaderListView;
import com.android.contacts.list.AlphaIndexFamilynameAdapter.FamilynameInfo;
import com.android.contacts.list.AlphaIndexFamilynameAdapter.FamilynameViewCache;
import com.android.contacts.list.MultiCursor.DummyCursor;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.preference.ContactsPreferences.ChangeListener;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.ContextMenuAdapter;
import com.android.contacts.widget.SuspentionScroller;
import com.google.android.gms.R;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.SortUtils;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCustUtils;
import com.huawei.immersion.Vibetonz;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public abstract class ContactEntryListFragment<T extends ContactEntryListAdapter> extends HwBaseFragment implements OnItemClickListener, OnItemLongClickListener, OnScrollListener, OnFocusChangeListener, OnTouchListener, LoaderCallbacks<Cursor> {
    View contactsAvailableView;
    private boolean includeStar = false;
    private boolean isDataToBeLoaded = true;
    private boolean isDataToBeReLoaded = false;
    private boolean isDisableLoadOnPrefChange = false;
    private boolean isNeedNotify = false;
    private AccountsDataManager mAccountDataManager;
    private Set<String> mActiveQueryStrings = new HashSet();
    private T mAdapter;
    private Configuration mConfiguration;
    private ContactsPreferences mContactsPrefs;
    private Context mContext;
    private ContextMenuAdapter mContextMenuAdapter;
    private boolean mDarkTheme;
    private Handler mDelayedDirectorySearchHandler;
    private int mDirectoryListStatus = 0;
    private final LoaderCallbacks<Cursor> mDirectoryLoaderListener = new LoaderCallbacks<Cursor>() {
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            DirectoryListLoader loader = new DirectoryListLoader(ContactEntryListFragment.this.mContext.getApplicationContext());
            ContactEntryListFragment.this.mAdapter.configureDirectoryLoader(loader);
            return loader;
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (ContactEntryListFragment.this.mEnabled && data != null && ContactEntryListFragment.this.getActivity() != null) {
                if (ContactEntryListFragment.this.isSearchMode()) {
                    ContactEntryListFragment.this.mDirectoryListStatus = 2;
                    if (HwLog.HWFLOW) {
                        HwLog.i("ContactEntryListFragment", "onLoadFinished,directory loaded,count:" + data.getCount() + ",load status: STATUS_LOADED");
                    }
                    ContactEntryListFragment.this.mAdapter.changeDirectories(data);
                    ContactEntryListFragment.this.mDirectoryManager.updateDiretories(data);
                    if (ContactEntryListFragment.this.mIsQueryText) {
                        ContactEntryListFragment.this.mIsQueryText = false;
                        if (ContactEntryListFragment.this.isSearchMode() && ContactEntryListFragment.this.mSearchCount > 0) {
                            ContactEntryListFragment.this.handleEmptyList(ContactEntryListFragment.this.mSearchCount);
                            ContactEntryListFragment.this.mSearchCount = 0;
                        }
                    }
                    ContactEntryListFragment.this.startLoading();
                    return;
                }
                ContactEntryListFragment.this.onDirectoryLoaded(data);
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    protected DirectorySearchManager mDirectoryManager;
    private int mDirectoryResultLimit = 20;
    private int mDirectorySearchMode = 0;
    protected Toast mDirectoryToast;
    private int mDisplayOrder;
    protected TextView mEmptyTextView;
    private ContactListEmptyView mEmptyView;
    private boolean mEnabled = true;
    protected boolean mExcludePrivateContacts;
    private float mExtraWithOfalphaIndex;
    private ListView mFamilyListView;
    private AlphaIndexFamilynameAdapter mFamilynameAdapter;
    private int mFamilynameListMarginBottom;
    private OnTouchListener mFamilynameListViewTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                case 2:
                    if (ContactEntryListFragment.this.mHandler.hasMessages(1)) {
                        ContactEntryListFragment.this.mHandler.removeMessages(1);
                        break;
                    }
                    break;
                case 1:
                case 3:
                    ContactEntryListFragment.this.mHandler.sendEmptyMessageDelayed(1, 3000);
                    break;
            }
            return false;
        }
    };
    private HashMap<String, ArrayList<FamilynameInfo>> mFamilynameMap = new HashMap();
    private View mFamilynameOverLayView;
    private int mFamilynameOverlayHeight;
    private boolean mForceLoad;
    private Handler mHandler;
    private boolean mHighLightVisible;
    HwCustContactListCustomizations mHwCustContactListCustObj = null;
    private Vibetonz mImmDevice;
    private boolean mIncludeProfile;
    private View mInflatedStubView;
    private final Object mInitializationLock = new Object();
    protected View mInnerSearchLayout;
    protected boolean mIsDataLoadedForMainList = true;
    private boolean mIsDefaultContactList = false;
    private boolean mIsInMultiWindowMode;
    protected boolean mIsQueryText = false;
    private boolean mIsReCreateView;
    private boolean mIsShowFamilynameOverlayView = true;
    protected boolean mIsShowOverLay;
    private boolean mIsStarted;
    private boolean mIsVibrateOn = false;
    private boolean mIsVoiceSearchMode = false;
    private boolean mLegacyCompatibility;
    private HashMap<String, Integer> mLetterIndexMap = new HashMap();
    private Parcelable mListState;
    private ListView mListView;
    private Handler mListViewUpdateHandler;
    private AccountCacheUpdatedListener mListener = new AccountCacheUpdatedListener() {
        public void onCacheUpdated() {
            ContactEntryListFragment.this.mListViewUpdateHandler.sendEmptyMessage(1);
        }
    };
    private boolean mLoadFinished;
    protected boolean mLoadPriorityDirectoriesOnly;
    private LoaderManager mLoaderManager;
    protected Locale mLocale;
    protected int mMissingItemIndex = -1;
    private ImageView mOverLayImageView;
    private TextView mOverLayTextView;
    private View mOverLayView;
    private TextView mPhonebookOverLayTextView;
    private View mPhonebookOverLayView;
    private int mPhonebookOverlayHeight;
    private int mPhonebookOverlayTextMarginTop;
    private int mPhonebookOverlayWidth;
    private boolean mPhotoLoaderEnabled;
    private ContactPhotoManager mPhotoManager;
    private ChangeListener mPreferencesChangeListener = new ChangeListener() {
        public void onChange() {
            if (HwLog.HWDBG) {
                HwLog.d("ContactEntryListFragment", "isDisableLoadOnPrefChange:" + ContactEntryListFragment.this.isDisableLoadOnPrefChange);
            }
            if (ContactEntryListFragment.this.isDisableLoadOnPrefChange) {
                ContactEntryListFragment.this.isDataToBeReLoaded = true;
                return;
            }
            ContactEntryListFragment.this.loadPreferences();
            ContactEntryListFragment.this.reloadData();
        }
    };
    private QueryLoaderQueue mQueryLoaderQueue = new QueryLoaderQueue();
    private ArrayList<String> mQueryMultiStrings;
    private String mQueryString;
    private String mQueryStringCached;
    private boolean mQuickContactEnabled = true;
    private RcsContactEntryListFragment mRcsCust = null;
    private ContactsRequest mRequest;
    private int mScreenContactsCount;
    private int mSearchCount = 0;
    protected LinearLayout mSearchLayout;
    private boolean mSearchMode;
    private boolean mSearchModeInitialized;
    private boolean mSectionHeaderDisplayEnabled;
    protected Drawable mSelectAllDrawable;
    protected MenuItem mSelectAllItem;
    protected boolean mSelectAllVisible = true;
    protected Drawable mSelectNoneDrawable;
    private boolean mSelectionVisible;
    protected int mSlipingItemNum;
    private int mSortOrder;
    public SparseArray<String> mSparseArray = null;
    private ImageView mStarredOverLayImageView;
    private SuspentionScroller mSuspentionScroller;
    private String mText = null;
    protected boolean mUserProfileExists;
    private int mVerticalScrollbarPosition = 2;
    private View mView;
    private boolean mVisibleScrollbarEnabled;
    private final LoaderCallbacks<Cursor> mVoiceSearchLoader = new LoaderCallbacks<Cursor>() {
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            long directoryId;
            VoiceSearchContactsLoader loader = new VoiceSearchContactsLoader(ContactEntryListFragment.this.mContext);
            if (args == null || !args.containsKey("directoryId")) {
                directoryId = 0;
            } else {
                directoryId = args.getLong("directoryId");
            }
            ContactEntryListFragment.this.mAdapter.configureLoader(loader, directoryId);
            return loader;
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            int i;
            String[] projection = ((VoiceSearchContactsLoader) loader).getProjection();
            int stringNum = ContactEntryListFragment.this.mQueryMultiStrings.size();
            ArrayList<MatrixCursor> cursors = new ArrayList();
            for (i = 0; i < stringNum; i++) {
                cursors.add(new MatrixCursor(projection));
            }
            MatrixCursor noMatchedCursor = new MatrixCursor(projection);
            Object[] row = new Object[projection.length];
            if (data != null) {
                data.moveToPosition(-1);
                while (data.moveToNext()) {
                    for (i = 0; i < row.length; i++) {
                        row[i] = data.getString(i);
                    }
                    int j = 0;
                    while (j < stringNum) {
                        String queryString = (String) ContactEntryListFragment.this.mQueryMultiStrings.get(j);
                        if (queryString != null && queryString.equals(data.getString(1))) {
                            ((MatrixCursor) cursors.get(j)).addRow(row);
                            break;
                        }
                        j++;
                    }
                    if (j >= ContactEntryListFragment.this.mQueryMultiStrings.size()) {
                        noMatchedCursor.addRow(row);
                    }
                }
            }
            cursors.add(noMatchedCursor);
            Cursor orderedCursor = new VoiceSearchMergeCursor((Cursor[]) cursors.toArray(new MatrixCursor[cursors.size()]), data);
            ContactEntryListFragment.this.mAdapter.configureDefaultPartition(false, true);
            Partition partition = ContactEntryListFragment.this.mAdapter.getPartition(0);
            if (partition instanceof DirectoryPartition) {
                ((DirectoryPartition) partition).setStatus(2);
            }
            if (ContactEntryListFragment.this.contactsAvailableView != null) {
                ContactEntryListFragment.this.contactsAvailableView.setVisibility(0);
            }
            ContactEntryListFragment.this.mSelectAllVisible = true;
            ContactEntryListFragment.this.getActivity().invalidateOptionsMenu();
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                ContactEntryListFragment.this.mAdapter.changeCursor(loader.getId(), orderedCursor);
                ContactEntryListFragment.this.handleEmptyList(orderedCursor.getCount());
                return;
            }
            ContactEntryListFragment.this.handleEmptyList(orderedCursor.getCount());
            ContactEntryListFragment.this.mAdapter.changeCursor(loader.getId(), orderedCursor);
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    public interface ContactsSearchLoader {
        String getQueryString();

        void setQueryString(String str);
    }

    private class DirectorySearchHandler extends Handler {
        private DirectorySearchHandler() {
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            if (msg.what == 1) {
                ContactEntryListFragment contactEntryListFragment = ContactEntryListFragment.this;
                int i = msg.arg1;
                DirectoryPartition directoryPartition = (DirectoryPartition) msg.obj;
                if (msg.arg2 != 1) {
                    z = false;
                }
                contactEntryListFragment.loadDirectoryPartition(i, directoryPartition, z);
            }
        }
    }

    public static class Fade {
        public static void show(View view) {
            if (view == null) {
                return;
            }
            if (view.getVisibility() != 0 || isFadingOut(view)) {
                view.animate().cancel();
                view.setTag(R.layout.pinned_header_listview_alpha_indexer, null);
                view.setAlpha(0.0f);
                view.setVisibility(0);
                view.animate().setDuration(250);
                view.animate().alpha(1.0f);
            }
        }

        public static void hide(final View view, final int visibility) {
            if (view != null && view.getVisibility() == 0) {
                if (visibility == 4 || visibility == 8) {
                    view.setTag(R.layout.pinned_header_listview_alpha_indexer, "fading_out");
                    view.animate().cancel();
                    view.animate().setDuration(600);
                    view.animate().alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            view.setAlpha(1.0f);
                            view.setVisibility(visibility);
                            view.animate().setListener(null);
                            view.setTag(R.layout.pinned_header_listview_alpha_indexer, null);
                        }
                    });
                }
            }
        }

        public static boolean isFadingOut(View view) {
            return view.getTag(R.layout.pinned_header_listview_alpha_indexer) == "fading_out";
        }
    }

    private class ListViewUpdateHandler extends Handler {
        private ListViewUpdateHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (HwLog.HWDBG) {
                        HwLog.d("ContactEntryListFragment", "ListView is invalidated on Cache Updated");
                    }
                    ContactEntryListFragment.this.mAdapter.notifyDataSetChanged();
                    return;
                default:
                    return;
            }
        }
    }

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ContactEntryListFragment.this.handlePhonebookOverLayViewDisplay(false, null);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public interface OnOverLayActionListener {
        void onStateChange(int i, String str, boolean z);
    }

    public final class OverLayActionListener implements OnOverLayActionListener {
        public void onStateChange(int itemPos, String sectionIndexer, boolean switchState) {
            if (ContactEntryListFragment.this.mOverLayView != null) {
                if (ContactEntryListFragment.this.mSuspentionScroller != null) {
                    ContactEntryListFragment.this.mSuspentionScroller.hideSuspentionView(itemPos, switchState);
                }
                if (!switchState) {
                    ContactEntryListFragment.this.handleOverLayViewDisplay(false, sectionIndexer);
                    if (ContactEntryListFragment.this.mHandler.hasMessages(1)) {
                        ContactEntryListFragment.this.mHandler.removeMessages(1);
                    }
                    ContactEntryListFragment.this.mHandler.sendEmptyMessageDelayed(1, 3000);
                } else if (EmuiFeatureManager.isChinaArea() && ContactEntryListFragment.this.mIsShowFamilynameOverlayView && CommonUtilMethods.isChineseLanguage()) {
                    ContactEntryListFragment.this.handlePhonebookOverLayViewDisplay(true, sectionIndexer);
                } else {
                    ContactEntryListFragment.this.handleOverLayViewDisplay(true, sectionIndexer);
                }
            }
        }
    }

    private class QueryLoaderQueue {
        private Message[] loadMsg = new Message[16];
        private boolean[] newLoaderFlag = new boolean[16];

        public QueryLoaderQueue() {
            for (int i = 0; i < 16; i++) {
                this.newLoaderFlag[i] = false;
                this.loadMsg[i] = null;
            }
        }

        public boolean isLoadIdInQueue(int loadId) {
            if (isInputRight(loadId)) {
                return this.newLoaderFlag[loadId - 1073741823];
            }
            return false;
        }

        public void setQueue(int loadId, Message msg) {
            if (isInputRight(loadId)) {
                this.newLoaderFlag[loadId - 1073741823] = true;
                this.loadMsg[loadId - 1073741823] = msg;
            }
        }

        public boolean releaseQueue(int loadId) {
            if (isInputRight(loadId)) {
                if (this.loadMsg[loadId - 1073741823] != null) {
                    ContactEntryListFragment.this.mDelayedDirectorySearchHandler.sendMessageDelayed(this.loadMsg[loadId - 1073741823], 300);
                    this.loadMsg[loadId - 1073741823] = null;
                    return true;
                }
                this.newLoaderFlag[loadId - 1073741823] = false;
            }
            return false;
        }

        private boolean isInputRight(int loadId) {
            if (loadId - 1073741823 >= 16 || loadId < 1073741823) {
                return false;
            }
            return true;
        }
    }

    public static class VoiceSearchMergeCursor extends MergeCursor {
        private Cursor mCursorOfSourceData = null;

        public VoiceSearchMergeCursor(Cursor[] cursors, Cursor sourceCursor) {
            super(cursors);
            this.mCursorOfSourceData = sourceCursor;
        }

        public void close() {
            super.close();
            if (this.mCursorOfSourceData != null) {
                this.mCursorOfSourceData.close();
            }
        }
    }

    protected abstract T createListAdapter();

    protected abstract View inflateView(LayoutInflater layoutInflater, ViewGroup viewGroup);

    protected abstract void onItemClick(int i, long j);

    protected boolean needSelectAllDrawable() {
        return true;
    }

    protected boolean onItemLongClick(int position, long id) {
        return false;
    }

    protected void onHeaderItemClick(int position, View v) {
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setContext(activity);
        this.mIsInMultiWindowMode = activity.isInMultiWindowMode();
        if (getLoaderManager() == null) {
            setLoaderManager(super.getLoaderManager());
        }
    }

    public void setContext(Context context) {
        this.mContext = context;
        this.mDirectoryManager = new DirectorySearchManager();
        configurePhotoLoader();
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setEnabled(boolean enabled) {
        if (this.mEnabled != enabled) {
            this.mEnabled = enabled;
            if (this.mAdapter == null) {
                return;
            }
            if (this.mEnabled) {
                reloadData();
            } else {
                this.mAdapter.clearPartitions();
            }
        }
    }

    public void setLoaderManager(LoaderManager loaderManager) {
        this.mLoaderManager = loaderManager;
    }

    public LoaderManager getLoaderManager() {
        return this.mLoaderManager;
    }

    public T getAdapter() {
        return this.mAdapter;
    }

    public View getView() {
        return this.mView;
    }

    public ListView getListView() {
        return this.mListView;
    }

    public ContactListEmptyView getEmptyView() {
        return this.mEmptyView;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!isReplacable()) {
            outState.putBoolean("sectionHeaderDisplayEnabled", this.mSectionHeaderDisplayEnabled);
            outState.putBoolean("photoLoaderEnabled", this.mPhotoLoaderEnabled);
            outState.putBoolean("quickContactEnabled", this.mQuickContactEnabled);
            outState.putBoolean("includeProfile", this.mIncludeProfile);
            outState.putBoolean("searchMode", this.mSearchMode);
            outState.putBoolean("visibleScrollbarEnabled", this.mVisibleScrollbarEnabled);
            outState.putInt("scrollbarPosition", this.mVerticalScrollbarPosition);
            outState.putInt("directorySearchMode", this.mDirectorySearchMode);
            outState.putBoolean("selectionVisible", this.mSelectionVisible);
            outState.putBoolean("legacyCompatibility", this.mLegacyCompatibility);
            outState.putString("queryString", this.mQueryString);
            outState.putInt("directoryResultLimit", this.mDirectoryResultLimit);
            outState.putParcelable("request", this.mRequest);
            outState.putBoolean("darkTheme", this.mDarkTheme);
            outState.putBoolean("force_reload", this.mForceLoad);
            outState.putInt("display_order", getContactNameDisplayOrder());
            outState.putInt("sort_order", getSortOrder());
            if (this.mLocale != null) {
                outState.putInt("local_language", this.mLocale.toString().hashCode());
            }
            if (this.mListView != null) {
                outState.putParcelable("liststate", this.mListView.onSaveInstanceState());
            }
            outState.putInt("missing_item_index", this.mMissingItemIndex);
        }
    }

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (!isReplacable()) {
            this.mHandler = new MyHandler();
            this.mDelayedDirectorySearchHandler = new DirectorySearchHandler();
            this.mListViewUpdateHandler = new ListViewUpdateHandler();
            restoreSavedState(savedState);
            if (needSelectAllDrawable()) {
                this.mSelectAllDrawable = BackgroundCacheHdlr.getSelectAllDrawable(this.mContext);
                if (this.mSelectAllDrawable == null) {
                    this.mSelectAllDrawable = ViewUtil.getSelectAllItemIcon(this.mContext);
                }
                this.mSelectNoneDrawable = BackgroundCacheHdlr.getSelectNoneDrawable(this.mContext);
                if (this.mSelectNoneDrawable == null) {
                    this.mSelectNoneDrawable = ViewUtil.getSelectNoneItemIcon(this.mContext);
                }
            }
            boolean defaultContactList = this instanceof DefaultContactBrowseListFragment;
            this.mIsDefaultContactList = false;
            if ((getActivity() instanceof PeopleActivity) && ((PeopleActivity) getActivity()).getLaunchTab() != TabState.ALL && defaultContactList) {
                this.mIsDefaultContactList = true;
            }
            if (EmuiFeatureManager.isProductCustFeatureEnable()) {
                this.mHwCustContactListCustObj = (HwCustContactListCustomizations) HwCustUtils.createObj(HwCustContactListCustomizations.class, new Object[0]);
            }
            if (EmuiFeatureManager.isRcsFeatureEnable()) {
                this.mRcsCust = new RcsContactEntryListFragment();
            }
            this.mExtraWithOfalphaIndex = (float) this.mContext.getResources().getDimensionPixelSize(R.dimen.alpha_index_extra_width);
            this.mConfiguration = getResources().getConfiguration();
            this.mFamilynameOverlayHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_familyname_overlay_height);
            this.mPhonebookOverlayTextMarginTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_overlay_phonebook_margin_top_or_bottom);
            this.mPhonebookOverlayHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_phonebook_overlay_height);
            this.mPhonebookOverlayWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_phonebook_overlay_width);
            this.mFamilynameListMarginBottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_familyname_list_margin_bottom);
            if (EmuiFeatureManager.isProductCustFeatureEnable() && this.mHwCustContactListCustObj != null && this.mHwCustContactListCustObj.activeOnlyPhoneContactsValue(this.mContext)) {
                this.mHwCustContactListCustObj.setOnlyPhoneContactsValue(this.mContext);
            }
        }
    }

    public void restoreSavedState(Bundle savedState) {
        if (savedState == null) {
            this.mLocale = Locale.getDefault();
            return;
        }
        this.mIsReCreateView = true;
        this.mSectionHeaderDisplayEnabled = savedState.getBoolean("sectionHeaderDisplayEnabled");
        this.mPhotoLoaderEnabled = savedState.getBoolean("photoLoaderEnabled");
        this.mQuickContactEnabled = savedState.getBoolean("quickContactEnabled");
        this.mIncludeProfile = savedState.getBoolean("includeProfile");
        this.mSearchMode = savedState.getBoolean("searchMode");
        this.mVisibleScrollbarEnabled = savedState.getBoolean("visibleScrollbarEnabled");
        this.mVerticalScrollbarPosition = savedState.getInt("scrollbarPosition");
        this.mDirectorySearchMode = savedState.getInt("directorySearchMode");
        this.mSelectionVisible = savedState.getBoolean("selectionVisible");
        this.mLegacyCompatibility = savedState.getBoolean("legacyCompatibility");
        this.mQueryString = savedState.getString("queryString");
        this.mDirectoryResultLimit = savedState.getInt("directoryResultLimit");
        this.mRequest = (ContactsRequest) savedState.getParcelable("request");
        this.mDarkTheme = savedState.getBoolean("darkTheme");
        this.mListState = savedState.getParcelable("liststate");
        this.mForceLoad = savedState.getBoolean("force_reload");
        setContactNameDisplayOrder(savedState.getInt("display_order"));
        setSortOrder(savedState.getInt("sort_order"));
        this.mLocale = Locale.getDefault();
        this.mForceLoad = savedState.getInt("local_language", this.mLocale.toString().hashCode()) != this.mLocale.toString().hashCode() ? true : this.mForceLoad;
        this.mMissingItemIndex = savedState.getInt("missing_item_index", -1);
    }

    public ContactsRequest getContactsRequest() {
        return this.mRequest;
    }

    public void setContactsRequest(ContactsRequest request) {
        this.mRequest = request;
    }

    public void onStart() {
        super.onStart();
        if (!isReplacable()) {
            if (this.mRcsCust != null) {
                this.mRcsCust.handleCustomizationsOnStart(this.mContext, this);
            }
            this.isDisableLoadOnPrefChange = false;
            this.mIsStarted = true;
            if (this.mQueryStringCached != null) {
                setQueryString(this.mQueryStringCached, true);
                this.mQueryStringCached = null;
            }
            if (HwLog.HWDBG) {
                HwLog.d("ContactEntryListFragment", "isDataToBeLoaded:" + this.isDataToBeLoaded + " isDataToBeReLoaded:" + this.isDataToBeReLoaded);
            }
            loadData();
        }
    }

    protected void loadData() {
        if (this.isDataToBeLoaded) {
            this.mForceLoad = loadPreferences();
            this.mDirectoryListStatus = 0;
            this.mLoadPriorityDirectoriesOnly = true;
            startLoading();
            this.isDataToBeLoaded = false;
        } else if (this.isDataToBeReLoaded) {
            loadPreferences();
            reloadData();
        }
    }

    protected void forceLoading() {
        this.mForceLoad = true;
        startLoading();
    }

    public void forceStartLoading() {
        this.mForceLoad = true;
        if (!this.mIsVoiceSearchMode && this.mAdapter != null) {
            int partitionCount = this.mAdapter.getPartitionCount();
            this.mSelectAllVisible = false;
            for (int i = 0; i < partitionCount; i++) {
                Partition partition = this.mAdapter.getPartition(i);
                if (partition instanceof DirectoryPartition) {
                    DirectoryPartition directoryPartition = (DirectoryPartition) partition;
                    if (directoryPartition.getStatus() == 0) {
                        if (directoryPartition.isPriorityDirectory() || !this.mLoadPriorityDirectoriesOnly) {
                            startLoadingDirectoryPartition(i, false);
                        }
                    } else if (directoryPartition.getStatus() == 2) {
                        startLoadingDirectoryPartition(i, false);
                    }
                } else {
                    getLoaderManager().initLoader(i, new Bundle(), this);
                }
            }
        }
    }

    protected void startLoading() {
        startLoading(this.mSearchMode);
    }

    protected void startLoading(boolean isForSearch) {
        PLog.d(0, "ContactEntryListFragment startLoading");
        if (!this.mIsVoiceSearchMode) {
            if (!(getActivity() == null || this.mLoadFinished)) {
                if ((getActivity() instanceof PeopleActivity) && CommonUtilMethods.calcIfNeedSplitScreen()) {
                    this.contactsAvailableView = getView().findViewById(R.id.pinned_header_list_layout);
                } else {
                    this.contactsAvailableView = getActivity().findViewById(R.id.pinned_header_list_layout);
                }
            }
            if (this.mAdapter != null) {
                configureAdapter();
                int partitionCount = this.mAdapter.getPartitionCount();
                this.mSelectAllVisible = false;
                for (int i = 0; i < partitionCount; i++) {
                    Partition partition = this.mAdapter.getPartition(i);
                    if (partition instanceof DirectoryPartition) {
                        DirectoryPartition directoryPartition = (DirectoryPartition) partition;
                        if (directoryPartition.getStatus() == 0 && (directoryPartition.isPriorityDirectory() || !this.mLoadPriorityDirectoriesOnly)) {
                            startLoadingDirectoryPartition(i, isForSearch);
                        }
                    } else {
                        getLoaderManager().initLoader(i, new Bundle(), this);
                    }
                }
            }
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String str;
        long directoryId;
        CursorLoader loader = createCursorLoader(id);
        boolean isForSearch = this.mSearchMode;
        if (args != null) {
            isForSearch = args.getBoolean("is_for_search", false);
        }
        if (isForSearch) {
            str = this.mQueryString;
        } else {
            str = null;
        }
        if (args == null || !args.containsKey("directoryId")) {
            directoryId = 0;
        } else {
            directoryId = args.getLong("directoryId");
        }
        if ((loader instanceof ContactsSearchLoader) && (directoryId == 0 || DirectoryCompat.isEnterpriseDirectoryId(directoryId))) {
            ((ContactsSearchLoader) loader).setQueryString(str);
        }
        if (isForSearch && ((directoryId == 0 || DirectoryCompat.isEnterpriseDirectoryId(directoryId)) && QueryUtil.isUseHwSearch())) {
            this.mAdapter.configHwSearchLoader(loader, directoryId);
        } else {
            this.mAdapter.configureLoader(loader, directoryId);
        }
        return loader;
    }

    public CursorLoader createCursorLoader(int id) {
        return new ContactListLoader(this.mContext.getApplicationContext(), null, null, null, null, null);
    }

    private void startLoadingDirectoryPartition(int partitionIndex, boolean isForSearch) {
        DirectoryPartition partition = (DirectoryPartition) this.mAdapter.getPartition(partitionIndex);
        partition.setStatus(1);
        long directoryId = partition.getDirectoryId();
        if (directoryId == 0 || DirectoryCompat.isEnterpriseDirectoryId(directoryId)) {
            loadDirectoryPartition(partitionIndex, partition, isForSearch);
        } else {
            loadDirectoryPartitionDelayed(partitionIndex, partition, isForSearch);
        }
    }

    private void loadDirectoryPartitionDelayed(int partitionIndex, DirectoryPartition partition, boolean isForSearch) {
        this.mDelayedDirectorySearchHandler.removeMessages(1, partition);
        Message msg = this.mDelayedDirectorySearchHandler.obtainMessage(1, partitionIndex, isForSearch ? 1 : 0, partition);
        if (isForSearch) {
            if (this.mQueryLoaderQueue.isLoadIdInQueue(partitionIndex + 1073741823)) {
                this.mQueryLoaderQueue.setQueue(partitionIndex + 1073741823, msg);
            } else {
                this.mDelayedDirectorySearchHandler.sendMessageDelayed(msg, 300);
                this.mQueryLoaderQueue.setQueue(partitionIndex + 1073741823, null);
            }
            return;
        }
        this.mDelayedDirectorySearchHandler.sendMessageDelayed(msg, 300);
    }

    protected void loadDirectoryPartition(int partitionIndex, DirectoryPartition partition, boolean isForSearh) {
        if (HwLog.HWFLOW) {
            HwLog.i("ContactEntryListFragment", "loadDirectoryPartition,partitionIndex=" + partitionIndex + ",isForSearh=" + isForSearh);
        }
        if (isAdded()) {
            Bundle args = new Bundle();
            args.putLong("directoryId", partition.getDirectoryId());
            args.putBoolean("is_for_search", isForSearh);
            int loaderId = isForSearh ? partitionIndex + 1073741823 : partitionIndex;
            if (this.mForceLoad) {
                getLoaderManager().restartLoader(loaderId, args, this);
            } else {
                getLoaderManager().initLoader(loaderId, args, this);
            }
            return;
        }
        HwLog.w("ContactEntryListFragment", "loadDirectoryPartition activity is not attached");
    }

    private void removePendingDirectorySearchRequests() {
        if (this.mDelayedDirectorySearchHandler != null) {
            this.mDelayedDirectorySearchHandler.removeMessages(1);
        }
    }

    public void onUpdateFamilyMap(Cursor data) {
        if (data != null && data.getCount() > 0) {
            final Bundle b = data.getExtras();
            if (b != null) {
                ContactsThreadPool.getInstance().execute(new Runnable() {
                    public void run() {
                        int starredCount = 0;
                        if (b.containsKey("android.provider.extra.ADDRESS_BOOK_INDEX_TITLES")) {
                            int[] phoneboolIndexCount = b.getIntArray("android.provider.extra.ADDRESS_BOOK_INDEX_COUNTS");
                            String[] phonebookIndexTitle = b.getStringArray("android.provider.extra.ADDRESS_BOOK_INDEX_TITLES");
                            if (phonebookIndexTitle != null && phonebookIndexTitle.length > 0 && "☆".equals(phonebookIndexTitle[0])) {
                                starredCount = phoneboolIndexCount[0];
                            }
                        }
                        ContactEntryListFragment.this.updateFamilynameMap(b, starredCount);
                    }
                });
            }
        }
    }

    protected void onDirectoryLoaded(Cursor cursor) {
    }

    protected boolean isPriorityDirectory(long directoryId) {
        if (directoryId != 0) {
            return DirectoryCompat.isEnterpriseDirectoryId(directoryId);
        }
        return true;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader == null || !this.mQueryLoaderQueue.isLoadIdInQueue(loader.getId()) || !this.mQueryLoaderQueue.releaseQueue(loader.getId())) {
            if (PLog.DEBUG) {
                PLog.d(0, "ContactEntryListFragment onLoadFinished");
            }
            if (!this.mEnabled) {
                return;
            }
            if (data == null) {
                if (HwLog.HWFLOW) {
                    HwLog.i("ContactEntryListFragment", "onLoadFinished data == null");
                }
                handleEmptyList(0);
            } else if (getActivity() != null) {
                handleData(loader, data);
            }
        }
    }

    public void setPiositionMap(Cursor data) {
        if (data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String[] strArray = bundle.getStringArray("android.provider.extra.ADDRESS_BOOK_INDEX_TITLES");
                int[] intArray = bundle.getIntArray("android.provider.extra.ADDRESS_BOOK_INDEX_COUNTS");
                this.mSparseArray = new SparseArray();
                int num = 0;
                if (strArray != null && strArray.length > 0) {
                    this.mSparseArray.put(0, strArray[0]);
                    for (int i = 1; i < strArray.length; i++) {
                        num += intArray[i - 1];
                        this.mSparseArray.put(num, strArray[i]);
                    }
                }
            }
        }
    }

    private void upDatePositionMap(Cursor data) {
        if (this.mAdapter instanceof DefaultContactListAdapter) {
            setPiositionMap(data);
            ((DefaultContactListAdapter) this.mAdapter).setCursorMap(this.mSparseArray);
            if (isSearchMode() && this.mSearchCount > 0) {
                ((DefaultContactListAdapter) this.mAdapter).setSearchMode(true);
            }
        }
    }

    protected void handleData(Loader<Cursor> loader, Cursor data) {
        onUpdateFamilyMap(data);
        int loaderId = loader == null ? 0 : loader.getId();
        int partitionIdex = loaderId;
        if (loaderId >= 1073741823) {
            partitionIdex -= 1073741823;
        }
        long directoryId = getDirectoryId(partitionIdex);
        this.mIsDataLoadedForMainList = loaderId < 1073741823;
        if (partitionIdex >= this.mAdapter.getPartitionCount() || this.mDirectoryManager.isDirectorySearchAborted(directoryId)) {
            HwLog.i("ContactEntryListFragment", "partition index >= adapter's partition count or abort; return");
            return;
        }
        HwLog.i("ContactEntryListFragment", "onLoadFinished loaderId" + loaderId + ",partitionIdex=" + partitionIdex + ",mSerchMode=" + this.mSearchMode);
        if (directoryId == 0 && loaderId < 1073741823) {
            upDatePositionMap(data);
        }
        this.isNeedNotify = false;
        Cursor oldCursor = this.mAdapter.getCursor(partitionIdex);
        MultiCursor multiCursor;
        if (oldCursor instanceof MultiCursor) {
            multiCursor = (MultiCursor) oldCursor;
            if (loaderId < 1073741823) {
                multiCursor.changeCursor(0, data);
            } else if (multiCursor.getCursor(1) == null) {
                multiCursor.addCursor(data);
            } else {
                multiCursor.changeCursor(1, data);
            }
            multiCursor.switchCursor(this.mSearchMode ? 1 : 0);
            this.mAdapter.notifyChange();
            this.isNeedNotify = true;
            data = oldCursor;
        } else {
            if (loaderId < 1073741823) {
                multiCursor = new MultiCursor(data);
                multiCursor.addCursor(new DummyCursor());
            } else {
                multiCursor = new MultiCursor(new DummyCursor());
                multiCursor.addCursor(data);
            }
            multiCursor.switchCursor(this.mSearchMode ? 1 : 0);
            Object data2 = multiCursor;
        }
        boolean z = data.getCount() > this.mScreenContactsCount ? !isSearchMode() : false;
        setVisibleScrollbarEnabled(z);
        if (this.mHwCustContactListCustObj != null) {
            this.mHwCustContactListCustObj.handleCustOnLoadFinished(this.mContext.getApplicationContext());
        }
        onPartitionLoaded(partitionIdex, data);
        if (this.isNeedNotify) {
            this.mAdapter.notifyChange();
        }
        if (isSearchMode() && getDirectorySearchMode() != 0 && this.mDirectoryListStatus == 0) {
            this.mDirectoryListStatus = 1;
            getLoaderManager().initLoader(-1, null, this.mDirectoryLoaderListener);
        }
        if (isPriorityDirectory(directoryId)) {
            this.mSelectAllVisible = true;
            if (getActivity() != null) {
                getActivity().invalidateOptionsMenu();
            }
            if (this.contactsAvailableView != null) {
                this.contactsAvailableView.setVisibility(0);
            }
            this.mLoadFinished = true;
            handleEmptyList(data.getCount());
            Object queryString = null;
            if (loader instanceof ContactsSearchLoader) {
                queryString = ((ContactsSearchLoader) loader).getQueryString();
            }
            if (loaderId >= 1073741823 && this.mActiveQueryStrings.contains(queryString)) {
                this.mListView.setSelection(0);
                this.mActiveQueryStrings.remove(queryString);
            }
            return;
        }
        if (HwLog.HWFLOW) {
            HwLog.i("ContactEntryListFragment", "handleData,not default directory and not enterprise directory,return");
        }
    }

    private long getDirectoryId(int partitionIndex) {
        if (partitionIndex < this.mAdapter.getPartitionCount()) {
            Partition partition = this.mAdapter.getPartition(partitionIndex);
            if (partition instanceof DirectoryPartition) {
                return ((DirectoryPartition) partition).getDirectoryId();
            }
        }
        return 2147483647L;
    }

    protected void startExchangeSearch() {
        removePendingDirectorySearchRequests();
        this.mDirectoryManager.abortResult(false);
        this.mForceLoad = true;
        this.mLoadPriorityDirectoriesOnly = false;
        this.mAdapter.onRemoteDataReload();
        startLoading();
    }

    public void handleEmptyList(int aCount) {
        if (HwLog.HWDBG) {
            boolean z;
            String str = "ContactEntryListFragment";
            StringBuilder append = new StringBuilder().append("handleEmptyList, data size>0?");
            if (aCount > 0) {
                z = true;
            } else {
                z = false;
            }
            HwLog.d(str, append.append(z).append(" isSearchMode():").append(isSearchMode()).toString());
        }
        checkAlphaScrollerAndUpdateViews();
        View loadingLayout = getView().findViewById(R.id.loadingcontacts);
        if (loadingLayout != null) {
            loadingLayout.setVisibility(8);
        }
        if (isSearchMode() && isLoading()) {
            if (this.mIsQueryText) {
                this.mSearchCount = aCount;
            }
        } else if (this.mAdapter.getPartitionCount() <= 1) {
            this.mIsQueryText = false;
            if (aCount == 0) {
                this.mListView.setVisibility(8);
                if (this.mEmptyTextView != null) {
                    this.mEmptyTextView.setVisibility(0);
                    setEmptyContactLocation();
                }
            } else {
                this.mListView.setVisibility(0);
                if (this.mEmptyTextView != null) {
                    this.mEmptyTextView.setVisibility(8);
                }
            }
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void setEmptyContactLocation() {
        Activity activity = getActivity();
        if (!(activity == null || this.mEmptyTextView == null)) {
            boolean isPor = getResources().getConfiguration().orientation == 1;
            int searchLayoutHeight = 0;
            if (this.mSearchLayout != null && (activity instanceof ContactAndGroupMultiSelectionActivity)) {
                searchLayoutHeight = getResources().getDimensionPixelSize(R.dimen.suspention_view_height);
            }
            if (activity.isInMultiWindowMode()) {
                this.mEmptyTextView.setPadding(0, searchLayoutHeight, 0, 0);
            } else if (isPor) {
                MarginLayoutParams params = (MarginLayoutParams) this.mEmptyTextView.getLayoutParams();
                params.topMargin = CommonUtilMethods.getMarginTopPix(activity, 0.3d, isPor) - searchLayoutHeight;
                this.mEmptyTextView.setLayoutParams(params);
            } else {
                int paddingBottom = CommonUtilMethods.getActionBarAndStatusHeight(activity, isPor) + searchLayoutHeight;
                RelativeLayout view = this.mView.findViewById(R.id.rl_no_contacts);
                if (view != null && (view instanceof RelativeLayout)) {
                    view.setGravity(17);
                    this.mEmptyTextView.setPadding(0, 0, 0, paddingBottom);
                }
            }
        }
    }

    protected void onPartitionLoaded(int partitionIndex, Cursor data) {
        int partitionCount = this.mAdapter.getPartitionCount();
        if (HwLog.HWDBG) {
            HwLog.d("ContactEntryListFragment", "onPartitionLoaded partitionIndex:" + partitionIndex + " partitionCount:" + partitionCount);
        }
        if (partitionIndex < partitionCount) {
            this.mAdapter.configureDefaultPartition(false, isSearchMode());
            if (this.mRcsCust != null) {
                this.mRcsCust.handleCustomizationsOnPartitionLoaded(this, data);
            }
            this.mAdapter.changeCursor(partitionIndex, data);
            if (CommonUtilMethods.isAlphaScrollerEnabled(this.mContext)) {
                if (HwLog.HWDBG) {
                    HwLog.d("ContactEntryListFragment", "AlphaScroller is enabled");
                }
                if (this.mListView instanceof AlphaIndexerPinnedHeaderListView) {
                    this.mListView.populateIndexerArray(this.mAdapter.getSections());
                }
            }
            setProfileHeader();
            showCount(partitionIndex, data);
            if (!isLoading()) {
                completeRestoreInstanceState();
            }
            if (this.isNeedNotify) {
                this.mAdapter.notifyChange();
                this.isNeedNotify = false;
            }
        }
    }

    public boolean isLoading() {
        if ((this.mAdapter == null || !this.mAdapter.isLoading()) && !isLoadingDirectoryList()) {
            return false;
        }
        return true;
    }

    public boolean isLoadingDirectoryList() {
        if (!isSearchMode() || getDirectorySearchMode() == 0) {
            return false;
        }
        return this.mDirectoryListStatus == 0 || this.mDirectoryListStatus == 1;
    }

    public void onStop() {
        super.onStop();
        this.isDisableLoadOnPrefChange = true;
        if (this.mRcsCust != null) {
            this.mRcsCust.handleCustomizationsOnStop(this.mContext);
        }
        this.mIsStarted = false;
    }

    public void onDestroy() {
        super.onDestroy();
        if (!isReplacable()) {
            if (this.mAdapter != null && this.mAdapter.isScrolling() && isPhotoLoaderEnabled()) {
                this.mPhotoManager.resume();
            }
            if (this.mContactsPrefs != null) {
                this.mContactsPrefs.unregisterChangeListener();
            }
        }
    }

    public void reloadData() {
        if (HwLog.HWDBG) {
            HwLog.d("ContactEntryListFragment", "reloadData(),mSearchMode=" + this.mSearchMode);
        }
        reloadData(true, this.mSearchMode);
        if (this.mSearchMode) {
            reloadData(true, false);
        }
    }

    private void reloadData(boolean isForceLoad, boolean isForSearch) {
        if (HwLog.HWFLOW) {
            HwLog.i("ContactEntryListFragment", "reloadData,isForceLoad=" + isForceLoad + ",isForSearch=" + isForSearch);
        }
        if (!this.mIsVoiceSearchMode) {
            removePendingDirectorySearchRequests();
            this.mAdapter.onDataReloadDefault();
            this.mLoadPriorityDirectoriesOnly = true;
            this.mForceLoad = isForceLoad;
            startLoading(isForSearch);
        }
    }

    protected void showCount(int partitionIndex, Cursor data) {
    }

    protected void setProfileHeader() {
        this.mUserProfileExists = false;
    }

    public void setSectionHeaderDisplayEnabled(boolean flag) {
        if (this.mSectionHeaderDisplayEnabled != flag) {
            this.mSectionHeaderDisplayEnabled = flag;
            if (this.mAdapter != null) {
                this.mAdapter.setSectionHeaderDisplayEnabled(flag);
            }
            configureVerticalScrollbar();
        }
    }

    public boolean isSectionHeaderDisplayEnabled() {
        return this.mSectionHeaderDisplayEnabled;
    }

    public void setVisibleScrollbarEnabled(boolean flag) {
        if (this.mVisibleScrollbarEnabled != flag) {
            this.mVisibleScrollbarEnabled = flag;
            configureVerticalScrollbar();
        }
    }

    public boolean isVisibleScrollbarEnabled() {
        return this.mVisibleScrollbarEnabled;
    }

    public void setVerticalScrollbarPosition(int position) {
        if (this.mVerticalScrollbarPosition != position) {
            this.mVerticalScrollbarPosition = position;
            configureVerticalScrollbar();
        }
    }

    public boolean isAlphaScrollerVisible() {
        HwLog.i("ContactEntryListFragment", "make sure the alphaScroller is visible, mVisibleScrollbarEnabled: " + this.mVisibleScrollbarEnabled + "; mSectionHeaderDisplayEnabled : " + this.mSectionHeaderDisplayEnabled);
        return isVisibleScrollbarEnabled() ? isSectionHeaderDisplayEnabled() : false;
    }

    private void configureVerticalScrollbar() {
        boolean hasScrollbar = isAlphaScrollerVisible();
        if (this.mMissingItemIndex >= 0) {
            hasScrollbar = false;
        }
        if (!hasScrollbar) {
            handleOverLayViewDisplay(false, null);
        }
        if (this.mListView != null) {
            HwLog.i("ContactEntryListFragment", "configure Vertical Scrollbar, hasScrollbar: " + hasScrollbar);
            this.mListView.setFastScrollEnabled(hasScrollbar);
            this.mListView.setFastScrollAlwaysVisible(hasScrollbar);
            if (hasScrollbar) {
                this.mListView.setVerticalScrollBarEnabled(false);
            } else {
                this.mListView.setVerticalScrollBarEnabled(true);
            }
            this.mListView.setVerticalScrollbarPosition(this.mVerticalScrollbarPosition);
            this.mListView.setScrollBarStyle(33554432);
            if (this.mListView instanceof AlphaIndexerPinnedHeaderListView) {
                this.mListView.setOverLayIndexerListener(new OverLayActionListener());
            }
        }
    }

    public void setPhotoLoaderEnabled(boolean flag) {
        this.mPhotoLoaderEnabled = flag;
        configurePhotoLoader();
    }

    public boolean isPhotoLoaderEnabled() {
        return this.mPhotoLoaderEnabled;
    }

    public boolean isSelectionVisible() {
        return this.mSelectionVisible;
    }

    public void setSelectionVisible(boolean flag) {
        this.mSelectionVisible = flag;
    }

    public void setHighLightVisible(boolean flag) {
        this.mHighLightVisible = flag;
    }

    public void setQuickContactEnabled(boolean flag) {
        this.mQuickContactEnabled = flag;
    }

    public void setIncludeProfile(boolean flag) {
        this.mIncludeProfile = flag;
        if (this.mAdapter != null) {
            this.mAdapter.setIncludeProfile(flag);
        }
    }

    public boolean isSearchModeInitialized() {
        return this.mSearchModeInitialized;
    }

    public void setSearchModeInitialized(boolean aSearchModeInitialized) {
        this.mSearchModeInitialized = aSearchModeInitialized;
    }

    public void setSearchMode(boolean flag) {
        boolean z = false;
        if (this.mSearchMode != flag) {
            boolean z2;
            this.mSearchMode = flag;
            if (this.mSearchMode) {
                z2 = false;
            } else {
                z2 = true;
            }
            setSectionHeaderDisplayEnabled(z2);
            if (!flag) {
                this.mDirectoryListStatus = 0;
                getLoaderManager().destroyLoader(-1);
            }
            if (this.mAdapter != null) {
                this.mAdapter.setPinnedPartitionHeadersEnabled(flag);
                this.mAdapter.setSearchMode(flag);
                if (!flag) {
                    this.mAdapter.removeDirectoriesAfterDefault();
                }
            }
            if (this.mListView != null && this.mAdapter != null) {
                ListView listView = this.mListView;
                if (this.mAdapter.getCount() > this.mScreenContactsCount && !flag) {
                    z = true;
                }
                listView.setFastScrollEnabled(z);
                if (this.mAdapter.getCount() > 0) {
                    this.mEmptyTextView.setVisibility(8);
                }
            }
        }
    }

    public final boolean isSearchMode() {
        return this.mSearchMode;
    }

    public final String getQueryString() {
        return this.mQueryString;
    }

    public void setQueryString(String queryString, boolean delaySelection) {
        if (getActivity() != null) {
            if (HwLog.HWFLOW) {
                Log.i("ContactEntryListFragment", "setQueryString,string length=" + (queryString == null ? 0 : queryString.length()));
            }
            if (this.mIsStarted) {
                if (queryString != null && queryString.length() == 0) {
                    queryString = null;
                }
                if (!TextUtils.equals(this.mQueryString, queryString)) {
                    this.mQueryString = queryString;
                    setSearchMode(!TextUtils.isEmpty(this.mQueryString));
                    if (TextUtils.isEmpty(this.mQueryString)) {
                        this.mActiveQueryStrings.clear();
                    } else {
                        this.mActiveQueryStrings.add(this.mQueryString);
                    }
                    if (this.mAdapter != null) {
                        if (this.mAdapter.getFilter() == null) {
                            configureAdapter();
                        }
                        this.mAdapter.setQueryString(queryString);
                        if (this.mDirectoryManager.getExchangeLogStatus()) {
                            this.mDirectoryManager.abortResult(true);
                            this.mAdapter.removeRemoteDirectories();
                            this.mDirectoryListStatus = 0;
                        }
                        if (this.mSearchMode) {
                            reloadData(true, true);
                        } else {
                            reloadData(false, false);
                        }
                    }
                }
                return;
            }
            this.mQueryStringCached = queryString;
        }
    }

    public int getDirectorySearchMode() {
        return this.mDirectorySearchMode;
    }

    public void setDirectorySearchMode(int mode) {
        this.mDirectorySearchMode = mode;
    }

    public boolean isLegacyCompatibilityMode() {
        return this.mLegacyCompatibility;
    }

    public void setLegacyCompatibilityMode(boolean flag) {
        this.mLegacyCompatibility = flag;
    }

    public int getContactNameDisplayOrder() {
        return this.mDisplayOrder;
    }

    public void setContactNameDisplayOrder(int displayOrder) {
        this.mDisplayOrder = displayOrder;
        if (this.mAdapter != null) {
            this.mAdapter.setContactNameDisplayOrder(displayOrder);
        }
    }

    public int getSortOrder() {
        return this.mSortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.mSortOrder = sortOrder;
        if (this.mAdapter != null) {
            this.mAdapter.setSortOrder(sortOrder);
        }
    }

    public void setDirectoryResultLimit(int limit) {
        this.mDirectoryResultLimit = limit;
    }

    public ContextMenuAdapter getContextMenuAdapter() {
        return this.mContextMenuAdapter;
    }

    protected boolean loadPreferences() {
        boolean changed = false;
        if (this.mContactsPrefs == null) {
            this.mContactsPrefs = new ContactsPreferences(this.mContext.getApplicationContext());
            if (!isReplacable()) {
                this.mContactsPrefs.registerChangeListener(this.mPreferencesChangeListener);
            }
        }
        if (getContactNameDisplayOrder() != this.mContactsPrefs.getDisplayOrder()) {
            setContactNameDisplayOrder(this.mContactsPrefs.getDisplayOrder());
            changed = true;
        }
        if (getSortOrder() != this.mContactsPrefs.getSortOrder()) {
            setSortOrder(this.mContactsPrefs.getSortOrder());
            changed = true;
        }
        Activity activity = getActivity();
        if (activity == null) {
            return changed;
        }
        ContactListFilter currentFilter = ContactListFilterController.getInstance(activity).getFilter();
        if (currentFilter == null || !currentFilter.mIsFiterChanged) {
            return changed;
        }
        currentFilter.mIsFiterChanged = false;
        return true;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateView(inflater, container);
        this.mAdapter = createListAdapter();
        if (isReplacable()) {
            return this.mView;
        }
        boolean searchMode = isSearchMode();
        this.mAdapter.setSearchMode(searchMode);
        this.mAdapter.configureDefaultPartition(false, searchMode);
        this.mAdapter.setPhotoLoader(this.mPhotoManager);
        this.mAdapter.setAccountDataLoader(this.mAccountDataManager);
        this.mListView.setAdapter(this.mAdapter);
        if (!isSearchMode()) {
            this.mListView.setFocusableInTouchMode(true);
            this.mListView.requestFocus();
        }
        Resources res = getResources();
        if (CommonUtilMethods.isSimplifiedModeEnabled()) {
            this.mScreenContactsCount = res.getInteger(R.integer.contact_per_screen_simplified);
        } else {
            this.mScreenContactsCount = res.getInteger(R.integer.contact_per_screen);
        }
        return this.mView;
    }

    public void reSetFastScrllEnabled(AlphaIndexerPinnedHeaderListView listView) {
        if (listView != null) {
            listView.setIncludeStar(this.includeStar);
        }
    }

    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        PLog.d(0, "ContactEntryListFragment onCreate begin");
        this.mView = inflateView(inflater, container);
        if (!isReplacable()) {
            int i;
            this.mListView = (ListView) this.mView.findViewById(16908298);
            if (this.mListView == null) {
                ViewStub lViewStub = (ViewStub) this.mView.findViewById(R.id.pinnedHeaderList_stub);
                if (lViewStub != null) {
                    if (HwLog.HWDBG) {
                        HwLog.d("ContactEntryListFragment", "List is available in form of stub");
                    }
                    try {
                        this.mInflatedStubView = lViewStub.inflate();
                    } catch (RuntimeException e) {
                        HwLog.e("ContactEntryListFragment", "ViewStub inflate error: " + e);
                    }
                    this.mListView = (ListView) this.mInflatedStubView.findViewById(16908298);
                    if (this.mListView instanceof AlphaIndexerPinnedHeaderListView) {
                        reSetFastScrllEnabled((AlphaIndexerPinnedHeaderListView) this.mListView);
                    }
                    this.mOverLayView = this.mInflatedStubView.findViewById(R.id.overlay);
                    this.mOverLayTextView = (TextView) this.mOverLayView.findViewById(R.id.overlay_textview);
                    this.mOverLayImageView = (ImageView) this.mOverLayView.findViewById(R.id.overlay_image);
                    if (EmuiFeatureManager.isChinaArea() && this.mConfiguration.orientation == 1) {
                        this.mFamilynameOverLayView = this.mInflatedStubView.findViewById(R.id.familyname_overlay);
                        this.mFamilyListView = (ListView) this.mFamilynameOverLayView.findViewById(R.id.familyname_overlay_list);
                        this.mPhonebookOverLayView = this.mInflatedStubView.findViewById(R.id.overlay1);
                        if (getActivity() != null && getActivity().isInMultiWindowMode()) {
                            LayoutParams phonebookParams = (LayoutParams) this.mPhonebookOverLayView.getLayoutParams();
                            phonebookParams.topMargin /= 2;
                            this.mPhonebookOverLayView.setLayoutParams(phonebookParams);
                            LayoutParams familynameparams = (LayoutParams) this.mFamilynameOverLayView.getLayoutParams();
                            familynameparams.topMargin -= phonebookParams.topMargin;
                            this.mFamilynameOverLayView.setLayoutParams(familynameparams);
                        }
                        if (ContactDpiAdapter.NOT_SRC_DPI) {
                            int newDimes = (this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_overlay_margin_start_portrait) * ContactDpiAdapter.SRC_DPI) / ContactDpiAdapter.REAL_Dpi;
                            if (this.mFamilynameOverLayView.getLayoutParams() instanceof LayoutParams) {
                                ((LayoutParams) this.mFamilynameOverLayView.getLayoutParams()).setMarginStart(newDimes);
                            }
                            if (this.mPhonebookOverLayView.getLayoutParams() instanceof LayoutParams) {
                                ((LayoutParams) this.mPhonebookOverLayView.getLayoutParams()).setMarginStart(newDimes);
                            }
                        }
                        this.mPhonebookOverLayTextView = (TextView) this.mPhonebookOverLayView.findViewById(R.id.overlay_phonebook_textview);
                        this.mStarredOverLayImageView = (ImageView) this.mPhonebookOverLayView.findViewById(R.id.overlay_starred_image);
                    }
                } else {
                    throw new RuntimeException("Your content must have a ListView whose id attribute is 'android.R.id.list'");
                }
            }
            if (this.mListView instanceof AlphaIndexerPinnedHeaderListView) {
                reSetFastScrllEnabled((AlphaIndexerPinnedHeaderListView) this.mListView);
            }
            this.mOverLayView = this.mView.findViewById(R.id.overlay);
            if (this.mOverLayView != null) {
                this.mOverLayTextView = (TextView) this.mOverLayView.findViewById(R.id.overlay_textview);
                this.mOverLayImageView = (ImageView) this.mOverLayView.findViewById(R.id.overlay_image);
            }
            if (EmuiFeatureManager.isChinaArea() && this.mConfiguration.orientation == 1) {
                this.mFamilynameOverLayView = this.mView.findViewById(R.id.familyname_overlay);
                if (this.mFamilynameOverLayView != null) {
                    this.mFamilyListView = (ListView) this.mFamilynameOverLayView.findViewById(R.id.familyname_overlay_list);
                    this.mPhonebookOverLayView = this.mView.findViewById(R.id.overlay1);
                    this.mPhonebookOverLayTextView = (TextView) this.mPhonebookOverLayView.findViewById(R.id.overlay_phonebook_textview);
                    this.mStarredOverLayImageView = (ImageView) this.mPhonebookOverLayView.findViewById(R.id.overlay_starred_image);
                }
            }
            if (this.mPhonebookOverLayView != null) {
                this.mPhonebookOverLayView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ContactEntryListFragment.this.handleOnclickForPhonebookOverLayView();
                    }
                });
            }
            if (this.mFamilyListView != null) {
                this.mFamilynameAdapter = new AlphaIndexFamilynameAdapter(this.mContext);
                this.mFamilyListView.setAdapter(this.mFamilynameAdapter);
                this.mFamilyListView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        int contactPos = ((FamilynameViewCache) view.getTag()).getPosition();
                        ContactEntryListFragment.this.mListView.setSelection(ContactEntryListFragment.this.mListView.getHeaderViewsCount() + contactPos);
                        ContactEntryListFragment.this.onClickFamilylistItme(contactPos);
                        StatisticalHelper.report(1139);
                    }
                });
                this.mFamilyListView.setOnTouchListener(this.mFamilynameListViewTouchListener);
            }
            this.mEmptyTextView = (TextView) this.mView.findViewById(R.id.list_empty);
            View emptyView = this.mView.findViewById(16908292);
            if (emptyView != null) {
                this.mListView.setEmptyView(emptyView);
                if (emptyView instanceof ContactListEmptyView) {
                    this.mEmptyView = (ContactListEmptyView) emptyView;
                }
            }
            this.mListView.setOnItemClickListener(this);
            this.mListView.setOnItemLongClickListener(this);
            this.mListView.setOnFocusChangeListener(this);
            this.mListView.setOnTouchListener(this);
            this.mListView.setFastScrollEnabled(!isSearchMode());
            this.mListView.setDividerHeight(0);
            this.mListView.setSaveEnabled(false);
            if (this.mContextMenuAdapter != null) {
                this.mListView.setOnCreateContextMenuListener(this.mContextMenuAdapter);
            }
            configureListView(this.mListView);
            if (CommonUtilMethods.isLayoutRTL()) {
                i = 1;
            } else {
                i = 2;
            }
            setVerticalScrollbarPosition(i);
            configureVerticalScrollbar();
            configurePhotoLoader();
            configureAccountDataLoader();
            if (this.mIsDefaultContactList && !this.mIsReCreateView) {
                this.mListView.setVisibility(8);
                View loadingView = this.mView.findViewById(R.id.loadingcontacts);
                if (loadingView != null) {
                    loadingView.setVisibility(0);
                    loadingView.setPadding(0, 0, 0, calcBottomLocation());
                }
            }
            PLog.d(0, "ContactEntryListFragment onCreate end");
            View view = getView().findViewById(R.id.suspention_scroller);
            if (view instanceof SuspentionScroller) {
                this.mSuspentionScroller = (SuspentionScroller) view;
            }
        }
    }

    private int calcBottomLocation() {
        if (getActivity() == null || getResources() == null) {
            HwLog.w("ContactEntryListFragment", "lactivity is NULL or getResources is NULL");
            return 0;
        }
        return CommonUtilMethods.getActionBarAndStatusHeight(getActivity(), getResources().getConfiguration().orientation == 1);
    }

    protected void configureListView(ListView aListView) {
    }

    protected void onClickFamilylistItme(int pos) {
    }

    protected void configurePhotoLoader() {
        if (isPhotoLoaderEnabled() && this.mContext != null) {
            if (this.mPhotoManager == null) {
                this.mPhotoManager = ContactPhotoManager.getInstance(this.mContext);
            }
            if (this.mListView != null) {
                this.mListView.setOnScrollListener(this);
            }
            if (this.mAdapter != null) {
                this.mAdapter.setPhotoLoader(this.mPhotoManager);
            }
        }
    }

    protected void configureAccountDataLoader() {
        if (this.mContext != null) {
            if (this.mAccountDataManager == null) {
                this.mAccountDataManager = AccountsDataManager.getInstance(this.mContext);
            }
            if (this.mAdapter != null) {
                this.mAdapter.setAccountDataLoader(this.mAccountDataManager);
            }
        }
    }

    protected void configureAdapter() {
        if (this.mAdapter != null) {
            this.mAdapter.setQuickContactEnabled(this.mQuickContactEnabled);
            this.mAdapter.setIncludeProfile(this.mIncludeProfile);
            this.mAdapter.setQueryString(this.mQueryString);
            this.mAdapter.setDirectorySearchMode(this.mDirectorySearchMode);
            this.mAdapter.setPinnedPartitionHeadersEnabled(this.mSearchMode);
            this.mAdapter.setContactNameDisplayOrder(this.mDisplayOrder);
            this.mAdapter.setSortOrder(this.mSortOrder);
            this.mAdapter.setSectionHeaderDisplayEnabled(this.mSectionHeaderDisplayEnabled);
            this.mAdapter.setSelectionVisible(this.mSelectionVisible);
            this.mAdapter.setHighLightVisible(this.mHighLightVisible);
            this.mAdapter.setDirectoryResultLimit(this.mDirectoryResultLimit);
        }
    }

    private void handleOverLayViewDisplay(boolean isShow, String text) {
        if (this.mMissingItemIndex < 0 && this.mOverLayView != null) {
            if (text != null) {
                if ("☆".equals(text)) {
                    this.mOverLayImageView.setVisibility(0);
                    this.mOverLayTextView.setVisibility(8);
                } else {
                    this.mOverLayImageView.setVisibility(8);
                    this.mOverLayTextView.setVisibility(0);
                    this.mOverLayTextView.setText(text);
                    if (this.mIsVibrateOn) {
                        if (!(this.mText == null || this.mText.equals(text) || this.mAdapter.isScrolling() || this.mImmDevice == null)) {
                            this.mImmDevice.playIvtEffect(1600);
                        }
                        this.mText = text;
                    }
                }
            }
            if (isShow) {
                Fade.show(this.mOverLayView);
            } else {
                Fade.hide(this.mOverLayView, 8);
            }
        }
    }

    private void handlePhonebookOverLayViewDisplay(boolean isShow, String text) {
        if (this.mPhonebookOverLayView != null) {
            boolean isShowFamilynameOverLay = false;
            if (text != null) {
                if ("☆".equals(text)) {
                    this.mStarredOverLayImageView.setVisibility(0);
                    this.mPhonebookOverLayTextView.setVisibility(8);
                    if (this.mFamilynameMap != null) {
                        this.mFamilynameAdapter.setArrayList(null);
                    }
                    this.mFamilynameOverLayView.setVisibility(8);
                } else {
                    Drawable drawable;
                    ArrayList arrayList = this.mFamilynameMap != null ? (ArrayList) this.mFamilynameMap.get(text) : null;
                    LayoutParams layoutParams = (LayoutParams) this.mFamilynameOverLayView.getLayoutParams();
                    LayoutParams phoneBookOverLayLayoutParams = (LayoutParams) this.mPhonebookOverLayView.getLayoutParams();
                    MarginLayoutParams phoneBookTextViewLayoutParams = (MarginLayoutParams) this.mPhonebookOverLayTextView.getLayoutParams();
                    int size = 0;
                    if (arrayList != null) {
                        size = arrayList.size();
                    }
                    this.mStarredOverLayImageView.setVisibility(8);
                    this.mPhonebookOverLayTextView.setVisibility(0);
                    this.mPhonebookOverLayTextView.setText(text);
                    boolean inMultiWindownmode = false;
                    int sizeWrap = 6;
                    if (getActivity() != null && getActivity().isInMultiWindowMode()) {
                        inMultiWindownmode = true;
                        sizeWrap = 2;
                    }
                    if (size == 0) {
                        phoneBookTextViewLayoutParams.setMargins(0, 0, 0, 0);
                        drawable = getResources().getDrawable(R.drawable.fastscroll_label_phonebook_emui_null);
                        this.mFamilynameOverLayView.setVisibility(8);
                        phoneBookOverLayLayoutParams.height = this.mPhonebookOverlayWidth;
                    } else if (size <= 0 || size >= sizeWrap) {
                        isShowFamilynameOverLay = true;
                        layoutParams.height = this.mFamilynameOverlayHeight + this.mFamilynameListMarginBottom;
                        if (inMultiWindownmode) {
                            layoutParams.height = (layoutParams.height / 3) + this.mFamilynameListMarginBottom;
                        }
                        this.mFamilynameOverLayView.setLayoutParams(layoutParams);
                        drawable = getResources().getDrawable(R.drawable.fastscroll_label_phonebook_emui);
                        phoneBookTextViewLayoutParams.setMargins(0, this.mPhonebookOverlayTextMarginTop, 0, 0);
                        phoneBookOverLayLayoutParams.height = this.mPhonebookOverlayHeight;
                    } else {
                        isShowFamilynameOverLay = true;
                        layoutParams.height = -2;
                        this.mFamilynameOverLayView.setLayoutParams(layoutParams);
                        drawable = getResources().getDrawable(R.drawable.fastscroll_label_phonebook_emui);
                        phoneBookTextViewLayoutParams.setMargins(0, this.mPhonebookOverlayTextMarginTop, 0, 0);
                        phoneBookOverLayLayoutParams.height = this.mPhonebookOverlayHeight;
                    }
                    this.mPhonebookOverLayTextView.setLayoutParams(phoneBookTextViewLayoutParams);
                    this.mPhonebookOverLayView.setLayoutParams(phoneBookOverLayLayoutParams);
                    int color = ImmersionUtils.getControlColor(getResources());
                    if (color != 0) {
                        drawable.setTint(color);
                    }
                    this.mPhonebookOverLayView.setBackground(drawable);
                    this.mFamilynameAdapter.setArrayList(arrayList);
                    if (this.mIsVibrateOn) {
                        if (!(this.mText == null || this.mText.equals(text) || this.mAdapter.isScrolling() || this.mImmDevice == null)) {
                            this.mImmDevice.playIvtEffect(1600);
                        }
                        this.mText = text;
                    }
                }
                this.mFamilyListView.setSelection(0);
            }
            if (isShow) {
                Fade.show(this.mPhonebookOverLayView);
                if (isShowFamilynameOverLay) {
                    Fade.show(this.mFamilynameOverLayView);
                }
            } else {
                Fade.hide(this.mPhonebookOverLayView, 8);
                Fade.hide(this.mFamilynameOverLayView, 8);
            }
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean hasScrollbar = false;
        if (this.mOverLayView != null) {
            if (isVisibleScrollbarEnabled()) {
                hasScrollbar = isSectionHeaderDisplayEnabled();
            }
            if (hasScrollbar) {
                int position = firstVisibleItem - this.mListView.getHeaderViewsCount();
                if (position < 0) {
                    position = 0;
                }
                if (this.mListView instanceof AlphaIndexerPinnedHeaderListView) {
                    this.mListView.setOverLayIndexer(position);
                }
                if (this.mIsShowOverLay) {
                    if (Math.abs(firstVisibleItem - this.mSlipingItemNum) >= visibleItemCount / 2 && this.mAdapter != null) {
                        ContactEntryListAdapter adpter = this.mAdapter;
                        int section = adpter.getSectionForPosition(position);
                        if (section >= 0) {
                            Object sectionheader = null;
                            Object sectionobj = adpter.getSections()[section];
                            if (sectionobj != null) {
                                sectionheader = sectionobj.toString();
                            }
                            if (!TextUtils.isEmpty(sectionheader) && isDisplayOverLayView()) {
                                handleOverLayViewDisplay(true, sectionheader);
                            }
                        } else {
                            return;
                        }
                    }
                    return;
                }
                this.mSlipingItemNum = firstVisibleItem;
            }
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (this.mSuspentionScroller != null) {
            this.mSuspentionScroller.onScrollStateChanged(view, scrollState);
        }
        if (scrollState == 2) {
            this.mPhotoManager.pause();
            this.mIsShowOverLay = true;
            this.mAdapter.setScrollingState(true);
        } else if (isPhotoLoaderEnabled()) {
            boolean isPrevStateScrolling = this.mAdapter.isScrolling();
            this.mAdapter.setScrollingState(false);
            if (isPrevStateScrolling) {
                this.mAdapter.notifyDataSetChanged();
            }
            this.mPhotoManager.resume();
        }
        if (scrollState == 0) {
            this.mIsShowOverLay = false;
            this.mAdapter.setScrollingState(false);
            handleOverLayViewDisplay(false, null);
        }
        if (this.mRcsCust != null) {
            this.mRcsCust.handleCustomizationsForScroll(scrollState);
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        hideSoftKeyboard();
        int adjPosition = position - this.mListView.getHeaderViewsCount();
        if (adjPosition >= 0) {
            if (getListView().isItemChecked(adjPosition)) {
                getListView().setItemChecked(adjPosition, true);
            } else {
                getListView().setItemChecked(adjPosition, false);
            }
            onItemClick(adjPosition, id);
        } else if (position >= 0) {
            onHeaderItemClick(position, view);
        }
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        int adjPosition = position - this.mListView.getHeaderViewsCount();
        if (adjPosition >= 0) {
            return onItemLongClick(adjPosition, id);
        }
        return false;
    }

    private void hideSoftKeyboard() {
        ((InputMethodManager) this.mContext.getSystemService("input_method")).hideSoftInputFromWindow(this.mListView.getWindowToken(), 0);
    }

    public void onFocusChange(View view, boolean hasFocus) {
        if (view == this.mListView && hasFocus) {
            hideSoftKeyboard();
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        if (view == this.mListView && event.getAction() == 0) {
            hideSoftKeyboard();
        }
        if (!(!(this.mListView instanceof AlphaIndexerPinnedHeaderListView) || ((AlphaIndexerPinnedHeaderListView) this.mListView).isPointInsideAlphaScroller(event.getX() + this.mExtraWithOfalphaIndex, event.getY()) || ((AlphaIndexerPinnedHeaderListView) this.mListView).isPointInsideAlphaScroller(event.getX(), event.getY()))) {
            handlePhonebookOverLayViewDisplay(false, null);
        }
        return false;
    }

    public void onPause() {
        super.onPause();
        if (!isReplacable()) {
            handlePhonebookOverLayViewDisplay(false, null);
            handleOverLayViewDisplay(false, null);
            this.mAccountDataManager.setListener(null);
        }
    }

    public void onResume() {
        super.onResume();
        if (!isReplacable()) {
            if (Vibetonz.isVibrateOn(this.mContext)) {
                this.mImmDevice = Vibetonz.getInstance();
                this.mIsVibrateOn = true;
            } else {
                this.mImmDevice = null;
                this.mIsVibrateOn = false;
            }
            this.mAccountDataManager.setListener(this.mListener);
            if (this.mAdapter != null) {
                this.mAdapter.upateSimpleDisplayMode();
            }
        }
    }

    protected void completeRestoreInstanceState() {
        if (this.mListState != null) {
            this.mListView.onRestoreInstanceState(this.mListState);
            this.mListState = null;
        }
    }

    protected void setEmptyText(int resourceId) {
        setEmptyText(this.mContext.getString(resourceId));
    }

    public void setEmptyText(String text) {
        View emptyView = getEmptyView();
        if (emptyView != null) {
            TextView empty = (TextView) emptyView.findViewById(16908292);
            empty.setText(text);
            empty.setVisibility(0);
        }
    }

    public void onPickerResult(Intent data) {
        throw new UnsupportedOperationException("Picker result handler is not implemented.");
    }

    protected TextView getEmptyTextView() {
        return this.mEmptyTextView;
    }

    public void showSelectedItemCountInfo(int selectecount) {
    }

    public boolean isReplacable() {
        return false;
    }

    public void setExcludePrivateContacts(boolean aExcludePrivateContacts) {
        this.mExcludePrivateContacts = aExcludePrivateContacts;
    }

    public boolean getIfExcludePrivateContacts() {
        return this.mExcludePrivateContacts;
    }

    protected void checkAlphaScrollerAndUpdateViews() {
        boolean lHasAlphaScroller = isAlphaScrollerVisible();
        if (this.mSearchLayout != null) {
            ViewGroup.LayoutParams lParams = this.mSearchLayout.getLayoutParams();
            if (lParams != null && (lParams instanceof LinearLayout.LayoutParams)) {
                int i;
                LinearLayout.LayoutParams lLP = (LinearLayout.LayoutParams) lParams;
                Resources resources = this.mContext.getResources();
                if (lHasAlphaScroller) {
                    i = R.dimen.searchLayout_margin_right_default;
                } else {
                    i = R.dimen.searchLayout_margin_left_right;
                }
                lLP.rightMargin = resources.getDimensionPixelOffset(i);
                this.mSearchLayout.setLayoutParams(lLP);
            }
        }
    }

    public void setIsVoiceSearchMode(boolean isVoiceSearchMode) {
        this.mIsVoiceSearchMode = isVoiceSearchMode;
    }

    public boolean getIsVoiceSearchMode() {
        return this.mIsVoiceSearchMode;
    }

    public void setQueryMultiStrings(ArrayList<String> stringList) {
        if ((getActivity() instanceof PeopleActivity) && CommonUtilMethods.calcIfNeedSplitScreen()) {
            this.contactsAvailableView = getView().findViewById(R.id.pinned_header_list_layout);
        } else {
            this.contactsAvailableView = getActivity().findViewById(R.id.pinned_header_list_layout);
        }
        this.mQueryMultiStrings = stringList;
        trimStringList(this.mQueryMultiStrings);
        if (this.mQueryMultiStrings == null || this.mQueryMultiStrings.size() == 0) {
            if (this.contactsAvailableView != null) {
                this.contactsAvailableView.setVisibility(0);
            }
            Partition partition = this.mAdapter.getPartition(0);
            if (partition instanceof DirectoryPartition) {
                ((DirectoryPartition) partition).setStatus(2);
            }
            handleEmptyList(0);
            return;
        }
        this.mAdapter.setQueryMultiString(this.mQueryMultiStrings);
        setSearchMode(true);
        this.mAdapter.setVoiceSearchMode(true);
        configureAdapter();
        loadVoiceSerachContacts();
    }

    private void trimStringList(ArrayList<String> stringList) {
        if (stringList != null && stringList.size() != 0) {
            int i = 0;
            while (i < stringList.size()) {
                if (stringList.get(i) == null || ((String) stringList.get(i)).length() == 0) {
                    stringList.remove(i);
                }
                i++;
            }
        }
    }

    private void loadVoiceSerachContacts() {
        int partitionCount = this.mAdapter.getPartitionCount();
        for (int i = 0; i < partitionCount; i++) {
            Partition partition = this.mAdapter.getPartition(i);
            if (partition instanceof DirectoryPartition) {
                DirectoryPartition directoryPartition = (DirectoryPartition) partition;
                if (directoryPartition.getStatus() == 0 && (directoryPartition.isPriorityDirectory() || !this.mLoadPriorityDirectoriesOnly)) {
                    directoryPartition.setStatus(1);
                    long directoryId = directoryPartition.getDirectoryId();
                    Bundle args = new Bundle();
                    args.putLong("directoryId", directoryId);
                    getLoaderManager().restartLoader(i, args, this.mVoiceSearchLoader);
                }
            } else {
                getLoaderManager().initLoader(i, new Bundle(), this.mVoiceSearchLoader);
            }
        }
    }

    protected void loadDirectories() {
        getLoaderManager().restartLoader(-1, new Bundle(), this.mDirectoryLoaderListener);
    }

    private void updateFamilynameMap(Bundle b, int starredCount) {
        String[] familyname = b.getStringArray("familyname_title");
        int[] familynameCount = b.getIntArray("familyname_count");
        String[] phonebookLabel = b.getStringArray("familyname_phonebook_label");
        if (familyname != null && familynameCount != null && phonebookLabel != null) {
            int familynameIndexLength = familyname.length;
            HashMap<String, ArrayList<FamilynameInfo>> familynameMap = new HashMap();
            HashMap<String, Integer> letterIndexMap = new HashMap();
            int position = 0;
            if (starredCount != 0) {
                letterIndexMap.put("☆", Integer.valueOf(0));
                position = starredCount + 0;
            }
            for (int i = 0; i < familynameIndexLength; i++) {
                if (SortUtils.isChinese(familyname[i].charAt(0))) {
                    if (familynameMap.containsKey(phonebookLabel[i])) {
                        ((ArrayList) familynameMap.get(phonebookLabel[i])).add(new FamilynameInfo(familyname[i], position));
                    } else {
                        ArrayList<FamilynameInfo> familynameInfoList = new ArrayList();
                        familynameInfoList.add(new FamilynameInfo(familyname[i], position));
                        familynameMap.put(phonebookLabel[i], familynameInfoList);
                    }
                }
                if (!letterIndexMap.containsKey(phonebookLabel[i])) {
                    letterIndexMap.put(phonebookLabel[i], Integer.valueOf(position));
                }
                position += familynameCount[i];
            }
            synchronized (this.mInitializationLock) {
                this.mFamilynameMap = (HashMap) familynameMap.clone();
                this.mLetterIndexMap = (HashMap) letterIndexMap.clone();
            }
        }
    }

    private void handleOnclickForPhonebookOverLayView() {
        int letterIndex;
        int headerCount = this.mListView.getHeaderViewsCount();
        if (this.mOverLayImageView.getVisibility() == 0) {
            letterIndex = ((Integer) this.mLetterIndexMap.get("☆")).intValue();
        } else {
            String text = this.mOverLayTextView.getText().toString();
            letterIndex = this.mLetterIndexMap.get(text) == null ? 0 : ((Integer) this.mLetterIndexMap.get(text)).intValue();
        }
        this.mListView.setSelection(letterIndex + headerCount);
    }

    public void configFamilynameOverLayDisplayEnabled(boolean showFamilynameOverLay) {
        this.mIsShowFamilynameOverlayView = showFamilynameOverLay;
    }

    private boolean isDisplayOverLayView() {
        return (this.mConfiguration.orientation == 1 && EmuiFeatureManager.isChinaArea() && this.mIsShowFamilynameOverlayView && CommonUtilMethods.isChineseLanguage() && !this.mIsInMultiWindowMode) ? false : true;
    }

    public void hidePhoneBookOverLay() {
        handlePhonebookOverLayViewDisplay(false, null);
    }
}
