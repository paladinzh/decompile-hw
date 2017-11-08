package com.android.mms.ui;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.provider.SearchRecentSuggestions;
import android.provider.Telephony.MmsSms;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsPublicinfoUtil;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.amap.api.services.core.AMapException;
import com.android.messaging.util.OsUtil;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.Contact.UpdateListener;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.Conversation.Cache;
import com.android.mms.data.Conversation.ConversationQueryHandler;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.ConversationListAdapter.OnContentChangedListener;
import com.android.mms.ui.ConversationListAdapter.SwipeCallback;
import com.android.mms.util.DraftCache;
import com.android.mms.util.DraftCache.OnDraftChangedListener;
import com.android.mms.util.Recycler;
import com.android.mms.widget.MmsWidgetProvider;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.data.RcsConversationUtils;
import com.android.rcs.data.RcsGroupCache;
import com.android.rcs.ui.RcsConversationListFragment;
import com.google.android.gms.Manifest.permission;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor;
import com.huawei.cspcommon.ex.ErrorMonitor.Radar;
import com.huawei.cspcommon.ex.MultiLoadHandler.ILoadCallBack;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.service.NameMatchResult;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.CspFragment;
import com.huawei.mms.ui.EmuiListViewListener;
import com.huawei.mms.ui.EmuiListView_V3;
import com.huawei.mms.ui.EmuiListView_V3.HandleTouchListener;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.ui.MultiModeListView.EditHandler;
import com.huawei.mms.ui.MultiModeListView.TypedEditHandler;
import com.huawei.mms.ui.NoMessageView;
import com.huawei.mms.ui.SearchMsgUtils;
import com.huawei.mms.ui.SearchViewWrapper;
import com.huawei.mms.ui.SearchViewWrapper.SearchViewListener;
import com.huawei.mms.ui.SmartArchiveSettings;
import com.huawei.mms.ui.SplitActionBarView;
import com.huawei.mms.ui.SplitActionBarView.OnCustomMenuListener;
import com.huawei.mms.ui.TextViewSnippet;
import com.huawei.mms.util.AvatarCache;
import com.huawei.mms.util.CursorUtils;
import com.huawei.mms.util.DefaultSmsAppChangedReceiver;
import com.huawei.mms.util.DefaultSmsAppChangedReceiver.HwDefSmsAppChangedListener;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwCloudBackUpManager;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.Log;
import com.huawei.mms.util.PrivacyModeReceiver.ModeChangeListener;
import com.huawei.mms.util.PrivacyModeReceiver.PrivacyStateListener;
import com.huawei.mms.util.ProviderCallUtils;
import com.huawei.mms.util.ProviderCallUtils.CallRequest;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.SelectRecorder;
import com.huawei.mms.util.SelectionChangedListener;
import com.huawei.mms.util.SmartArchiveSettingUtils;
import com.huawei.mms.util.StatisticalHelper;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class BaseConversationListFragment extends CspFragment implements OnDraftChangedListener, OnItemClickListener, ILoadCallBack {
    private static final String[] ALL_SMS_ADDRESS_PROJECTION = new String[]{"thread_id", "address"};
    private static int FINISH_ACTIVITY_DELAY = 50;
    private static int POS_ADDRESS = 2;
    private static int POS_INDEX_TEXT = 6;
    private static final Object mShowDialogLock = new Object();
    private static AlertDialog mShowconfirmDeleteThreadDialog = null;
    public static final Uri sAllCanonical = Uri.parse("content://mms-sms/canonical-contactRepair");
    private static final Uri sAllSms = Uri.parse("content://sms/");
    private static long sDeleteCount = 0;
    private static long sDeleteStartTime = 0;
    private volatile boolean isDataLoad = false;
    private boolean isMoveDown;
    private boolean isMoveUp;
    private int lastUpDownState = 0;
    ModeChangeListener localPrivacyModitor;
    protected AbstractEmuiActionBar mActionBar;
    private BroadcastReceiver mAddressChangeReceiver = null;
    private Cursor mCachedCursor = null;
    ContentObserver mContactChangeListener = new ContentObserver(new Handler()) {
        public void onChange(boolean updated) {
            HwBackgroundLoader.getUIHandler().removeCallbacks(BaseConversationListFragment.this.mUpdateContactRunner);
            HwBackgroundLoader.getUIHandler().postDelayed(BaseConversationListFragment.this.mUpdateContactRunner, 500);
        }
    };
    ContentObserver mContactDbListenr = new ContentObserver(new Handler()) {
        public void onChange(boolean updated) {
            BaseConversationListFragment.this.mSearchLoader.onDataChanged(2);
        }
    };
    UpdateListener mContactListener = new UpdateListener() {
        public void onUpdate(Contact updated) {
            TextView tv = (TextView) BaseConversationListFragment.this.mContactMap.get(updated);
            if (tv != null) {
                tv.setText(updated.getNameAndNumber());
            }
        }
    };
    private HashMap<Contact, TextView> mContactMap = new HashMap();
    private final OnContentChangedListener mContentChangedListener = new OnContentChangedListener() {
        public void onContentChanged(ConversationListAdapter adapter) {
            if (BaseConversationListFragment.this.mListAdapter.hasOpenSwipe()) {
                BaseConversationListFragment.this.mListAdapter.closeOpenSwipe();
            }
            if (BaseConversationListFragment.this.mSwipeShowconfirmDeleteThreadDialog != null && BaseConversationListFragment.this.mSwipeShowconfirmDeleteThreadDialog.isShowing()) {
                BaseConversationListFragment.this.mSwipeShowconfirmDeleteThreadDialog.dismiss();
            }
            if (BaseConversationListFragment.this.mSwipeShowconfirmDeleteLockThreadDialog != null && BaseConversationListFragment.this.mSwipeShowconfirmDeleteLockThreadDialog.isShowing()) {
                BaseConversationListFragment.this.mSwipeShowconfirmDeleteLockThreadDialog.dismiss();
            }
            if (BaseConversationListFragment.this.mHwCust != null) {
                BaseConversationListFragment.this.mHwCust.onContentChanged();
            }
            BaseConversationListFragment.this.startAsyncQuery();
        }
    };
    private final OnItemLongClickListener mConversationLongClickListener = new OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView<?> adapterView, final View v, int viewPos, final long selectPos) {
            HwBackgroundLoader.getUIHandler().post(new Runnable() {
                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    synchronized (BaseConversationListFragment.this.mLongClickLock) {
                        if (BaseConversationListFragment.this.mListAdapter.hasOpenSwipe()) {
                            BaseConversationListFragment.this.mListAdapter.closeOpenSwipe();
                            return;
                        }
                        if (BaseConversationListFragment.this.isRunningInNotifactionList()) {
                            StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2188);
                        } else {
                            StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2183);
                        }
                        if (v == null || (R.id.inner_searchlayout != v.getId() && (((View) v.getParent()) == null || R.id.inner_searchlayout != ((View) v.getParent()).getId()))) {
                            BaseConversationListFragment.this.gotoEdit(selectPos);
                            if (BaseConversationListFragment.this.mCustomActionBar != null && HwMessageUtils.isSplitOn()) {
                                BaseConversationListFragment.this.mCustomActionBar.setVisibility(0);
                            }
                        } else {
                            BaseConversationListFragment.this.clickToSearchMode();
                        }
                    }
                }
            });
            return true;
        }
    };
    protected SplitActionBarView mCustomActionBar;
    private int mDataLoadingStatus = -1;
    DefaultSmsAppChangedReceiver mDefSmsAppChangedReceiver = null;
    private OnClickListener mDefaultSmsViewClickListener = new OnClickListener() {
        public void onClick(View view) {
            Activity activity = BaseConversationListFragment.this.getActivity();
            if (activity == null || activity.isFinishing()) {
                MLog.e("Mms_UI_CLFrag", "initSmsPromoBanner set default sms failed because activity is null or finishing!");
                return;
            }
            try {
                activity.startActivity(MmsConfig.getRequestDefaultSmsAppActivity());
            } catch (Exception e) {
                MLog.e("Mms_UI_CLFrag", "initSmsPromoBanner set default sms exception:" + e);
            }
        }
    };
    private final Runnable mDeleteObsoleteThreadsRunnable = new Runnable() {
        public void run() {
            if (MLog.isLoggable("Mms_app", 2)) {
                LogTag.debug("mDeleteObsoleteThreadsRunnable getSavingDraft(): " + DraftCache.getInstance().getSavingDraft(), new Object[0]);
            }
            if (DraftCache.getInstance().getSavingDraft()) {
                BaseConversationListFragment.this.getUiHandler().postDelayed(BaseConversationListFragment.this.mDeleteObsoleteThreadsRunnable, 1000);
            } else {
                Conversation.asyncDeleteObsoleteThreads(BaseConversationListFragment.this.mQueryHandler, AMapException.CODE_AMAP_CLIENT_URL_EXCEPTION);
            }
        }
    };
    private boolean mDoOnceAfterFirstQuery;
    protected View mFakeHeadView;
    private Runnable mFinishActivityRunnable = new Runnable() {
        public void run() {
            MLog.d("Mms_UI_CLFrag", "Finish Activity in UI thread");
            Activity activity = BaseConversationListFragment.this.getActivity();
            if (activity != null && !activity.isFinishing()) {
                if (HwMessageUtils.isSplitOn()) {
                    BaseConversationListFragment.this.backToConversationList(activity);
                } else {
                    activity.finish();
                }
            }
        }
    };
    private float mFirstMotionY;
    private OnClickListener mFunctionTipsClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (view.getId() == R.id.cancel_tips) {
                PreferenceUtils.setFunctionTipsNoShowAgain(BaseConversationListFragment.this.getContext(), true);
                if (BaseConversationListFragment.this.mMmsFunctionTipsView != null) {
                    BaseConversationListFragment.this.mMmsFunctionTipsView.setVisibility(8);
                    BaseConversationListFragment.this.mMmsFunctionTipsView = null;
                }
                return;
            }
            if (!Contact.IS_CHINA_REGION) {
                PreferenceUtils.setRiskUrlFunctionTipsOpen(BaseConversationListFragment.this.getContext());
                if (BaseConversationListFragment.this.mMmsFunctionTipsView != null) {
                    BaseConversationListFragment.this.mMmsFunctionTipsView.setVisibility(8);
                    BaseConversationListFragment.this.mMmsFunctionTipsView = null;
                }
            } else if (Contact.IS_CHINA_REGION && UserHandle.myUserId() == 0 && MmsConfig.getSupportSmartSmsFeature()) {
                SmartSmsSdkUtil.setSmartSmsFunctionTipsOpen(BaseConversationListFragment.this.getContext());
                if (BaseConversationListFragment.this.mMmsFunctionTipsView != null) {
                    BaseConversationListFragment.this.mMmsFunctionTipsView.setVisibility(8);
                    BaseConversationListFragment.this.mMmsFunctionTipsView = null;
                }
            }
        }
    };
    private List<Long> mHasReparedEmptyNameThreads = new ArrayList();
    private int mHeadBasePositionY;
    private int[] mHeadlocation = new int[2];
    protected RcsConversationListFragment mHwCust = null;
    private HwCustBaseConversationListFragment mHwCustBaseConversationListFragment;
    private View mImageMask;
    private OnClickListener mImageMaskListener = new OnClickListener() {
        public void onClick(View view) {
            if (view.getVisibility() == 0) {
                Activity activity = BaseConversationListFragment.this.getActivity();
                if (activity != null) {
                    if (HwMessageUtils.isSplitOn()) {
                        Intent intent = new Intent(activity, ConversationList.class);
                        intent.setAction("android.intent.action.MAIN");
                        BaseConversationListFragment.this.startActivity(intent);
                    }
                    activity.finish();
                    ((ConversationList) activity).overridePendingTransition(0, 0);
                }
            }
        }
    };
    private boolean mIsAllSelected = false;
    private boolean mIsAskBeforeDeleting = false;
    protected boolean mIsBeingDragged = false;
    private boolean mIsBeingScroll;
    private boolean mIsFirst = true;
    private boolean mIsFromPause = false;
    protected boolean mIsOnTop = false;
    private boolean mIsSmsDefaultPackage = true;
    private boolean mIsSmsEnabled = true;
    private float mLastMoveY;
    protected ConversationListAdapter mListAdapter;
    private ListViewListener mListListener = null;
    protected EmuiListView_V3 mListView;
    private View mLoadingView;
    private LocalBroadcastManager mLocalBroadcastManager = null;
    Object mLongClickLock = new Object();
    protected BaseFragmentMenu mMenuEx;
    private View mMmsFunctionTipsView;
    private float mMoveDistance;
    private NoMessageView mNoConvView = null;
    private boolean mObsoluteUndeleted = true;
    private List<Long> mPendingEmptyNameThreads = new ArrayList();
    private ThreadListQueryHandler mQueryHandler;
    private BroadcastReceiver mReceiver = null;
    private List<Long> mReparingEmptyNameThreads = new ArrayList();
    protected int mRootLayoutId = -1;
    protected int mRunningMode = 1;
    private int mSavedFirstItemOffset;
    private int mSavedFirstVisiblePosition = -1;
    private OnClickListener mSearchClickListener = null;
    protected View mSearchHeaderView;
    private int[] mSearchHeaderViewLocation = new int[2];
    private SearchCursorAdapter mSearchListAdapter;
    private ListView mSearchListView;
    private SearchDataLoader mSearchLoader;
    private SearchMsgUtils mSearchMsgUtils = null;
    private String mSearchText = null;
    private float mSearchViewHight;
    private SearchViewListener mSearchViewListener = new SearchViewListener() {
        public void onSearchTextChange(CharSequence s, int start, int before, int count) {
            String tempText = s.toString().trim();
            if (TextUtils.isEmpty(BaseConversationListFragment.this.mSearchText) && tempText.length() > 0) {
                BaseConversationListFragment.this.mWaitForSearchText = tempText;
                BaseConversationListFragment.this.switchToSearchView();
                BaseConversationListFragment.this.startSearchAsynQuery();
            } else if (!TextUtils.isEmpty(BaseConversationListFragment.this.mSearchText) && tempText.length() == 0) {
                BaseConversationListFragment.this.getUiHandler().post(new Runnable() {
                    public void run() {
                        BaseConversationListFragment.this.mWaitForSearchText = "";
                        BaseConversationListFragment.this.mSearchText = "";
                        BaseConversationListFragment.this.switchToConversationView();
                        BaseConversationListFragment.this.isDataLoad = false;
                        BaseConversationListFragment.this.checkAsyncQuery();
                    }
                });
            } else if (!TextUtils.isEmpty(BaseConversationListFragment.this.mSearchText) && tempText.length() > 0) {
                BaseConversationListFragment.this.mWaitForSearchText = tempText;
                BaseConversationListFragment.this.startSearchAsynQuery();
            }
        }
    };
    protected SearchViewWrapper mSearchWrapper;
    private View mSmsPromoBannerView;
    private float mSpeed;
    private Long mSwipeDeleteID = Long.valueOf(-1);
    private AlertDialog mSwipeShowconfirmDeleteLockThreadDialog = null;
    private AlertDialog mSwipeShowconfirmDeleteThreadDialog = null;
    private int mUnEditableItemCount = -1;
    protected int mUnreadCount = 0;
    private Runnable mUpdateContactRunner = new Runnable() {
        public void run() {
            if (BaseConversationListFragment.this.mSearchListView != null) {
                BaseConversationListFragment.this.mSearchListView.invalidateViews();
            }
            if (BaseConversationListFragment.this.mListView != null) {
                BaseConversationListFragment.this.mListView.invalidateViews();
            }
        }
    };
    private String mWaitForSearchText = "";
    private boolean sRepairing = false;

    public abstract class BaseFragmentMenu extends EmuiMenu implements OnCustomMenuListener, OnClickListener {
        protected abstract Menu getMenuFromSubClass();

        public BaseFragmentMenu() {
            super(null);
        }

        public void onEnterEditMode() {
            BaseConversationListFragment.this.mActionBar.enterEditMode(this);
            BaseConversationListFragment.this.invalidateOptionsMenu();
        }

        public void onExitEditMode() {
            BaseConversationListFragment.this.mActionBar.exitEditMode();
            boolean inEditMode = BaseConversationListFragment.this.mActionBar.getActionMode() == 2;
            boolean isInConversationList = BaseConversationListFragment.this.isRunningInConversationList();
            if (!(inEditMode || !isInConversationList || BaseConversationListFragment.this.mSearchHeaderView == null)) {
                BaseConversationListFragment.this.mSearchHeaderView.setVisibility(0);
                BaseConversationListFragment.this.showViewAfterExitEdit();
            }
            BaseConversationListFragment.this.invalidateOptionsMenu();
        }

        public void onEnterSearchMode() {
            BaseConversationListFragment.this.invalidateOptionsMenu();
        }

        public void onClick(View v) {
            Activity activity = BaseConversationListFragment.this.getActivity();
            if (activity != null) {
                activity.onBackPressed();
            }
        }

        public Menu getMenu() {
            if (!BaseConversationListFragment.this.checkIsAdded() || BaseConversationListFragment.this.mListView == null) {
                return null;
            }
            return getMenuFromSubClass();
        }

        private MenuItem getMenuItem(int itemId) {
            Menu menu = getMenu();
            if (menu != null) {
                return menu.findItem(itemId);
            }
            return null;
        }

        void setPinup(boolean pin) {
            if (BaseConversationListFragment.this.mCustomActionBar != null) {
                MenuItem item = getMenuItem(278927461);
                if (item == null) {
                    MLog.e("Mms_UI_CLFrag", "pin item is null");
                    return;
                }
                if (pin) {
                    item.setTitle(R.string.mms_menu_pin);
                } else {
                    item.setTitle(R.string.mms_menu_unpin);
                }
                item.setChecked(false);
            }
        }

        public boolean onKeyEvent(int keyCode) {
            if (BaseConversationListFragment.this.mCustomActionBar == null) {
                MLog.d("Mms_UI_CLFrag", "onKeyEvent is sent but SplitActionBar is empty");
                return false;
            } else if (82 != keyCode) {
                return false;
            } else {
                BaseConversationListFragment.this.mCustomActionBar.showPopup();
                return true;
            }
        }

        public boolean onCustomMenuItemClick(MenuItem item) {
            if (item == null) {
                MLog.e("Mms_UI_CLFrag", "choice item is null");
                return false;
            }
            int reslut = 0;
            Long[] selectedItems = BaseConversationListFragment.this.mListView.isInEditMode() ? BaseConversationListFragment.this.mListView.getRecorder().getAllSelectItems() : SelectRecorder.EMPTY_RECORD;
            switch (item.getItemId()) {
                case 16908332:
                    BaseConversationListFragment.this.getActivity().finish();
                    BaseConversationListFragment.this.getActivity().overridePendingTransition(0, 0);
                    break;
                case 278925313:
                    boolean z;
                    if (BaseConversationListFragment.this.mListView.getSelectedCount() == BaseConversationListFragment.this.mListView.getMessageCount() - BaseConversationListFragment.this.mUnEditableItemCount) {
                        StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2185);
                    } else {
                        StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2184);
                    }
                    int selectCount = BaseConversationListFragment.this.mListView.getSelectedCount();
                    int totalCount = BaseConversationListFragment.this.mListView.getMessageCount();
                    BaseConversationListFragment.this.initUnEditableItemCount();
                    EmuiListView_V3 emuiListView_V3 = BaseConversationListFragment.this.mListView;
                    if (selectCount != totalCount - BaseConversationListFragment.this.mUnEditableItemCount) {
                        z = true;
                    } else {
                        z = false;
                    }
                    emuiListView_V3.setAllSelected(z, false);
                    break;
                case 278925315:
                    if (BaseConversationListFragment.this.isRunningInNotifactionList()) {
                        StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2189);
                    } else {
                        StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2186);
                    }
                    BaseConversationListFragment.this.mListView.doOperation(BaseConversationListFragment.this.mListListener.mDeleteHandler);
                    break;
                case 278925317:
                    StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getActivity(), 2118);
                    BaseConversationListFragment.this.startActivity(new Intent(BaseConversationListFragment.this.getContext(), MessagingPreferenceActivity.class));
                    return true;
                case 278925340:
                    if (BaseConversationListFragment.this.isRunningInNotifactionList()) {
                        StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2104);
                    } else {
                        StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2103);
                    }
                    Intent hsmIntent = new Intent("huawei.intent.action.HSM_HARASSMENT");
                    hsmIntent.putExtra("package_name", "com.android.contacts");
                    hsmIntent.addCategory("android.intent.category.DEFAULT");
                    try {
                        BaseConversationListFragment.this.startActivity(hsmIntent);
                        break;
                    } catch (ActivityNotFoundException aE) {
                        MLog.e("Mms_UI_CLFrag", "[error] >>>" + aE);
                        break;
                    }
                case 278925344:
                    StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2117);
                    BaseConversationListFragment.this.gotoDeleteMode();
                    break;
                case 278925349:
                    StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2135);
                    BaseConversationListFragment.this.startActivity(new Intent(BaseConversationListFragment.this.getContext(), RecyclerSmsListActivity.class));
                    break;
                case 278925350:
                    BaseConversationListFragment.this.startActivityForResult(new Intent(BaseConversationListFragment.this.getActivity(), SmartArchiveSettings.class), 7);
                    break;
                case 278927460:
                    if (BaseConversationListFragment.this.mIsSmsEnabled) {
                        if (BaseConversationListFragment.this.mListAdapter.hasOpenSwipe()) {
                            BaseConversationListFragment.this.mListAdapter.closeOpenSwipe();
                        }
                        BaseConversationListFragment.this.createNewMessage();
                    }
                    return true;
                case 278927461:
                    int type = BaseConversationListFragment.this.mListListener.mPinHandler.getOperation(selectedItems);
                    if (278927461 == type) {
                        StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2105);
                    } else {
                        StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2106);
                    }
                    reslut = BaseConversationListFragment.this.mListView.doOperation(BaseConversationListFragment.this.mListListener.mPinHandler.setOperation(type));
                    break;
                case 278927465:
                    MLog.d("Mms_UI_CLFrag", "mark all as read is clicked");
                    if (BaseConversationListFragment.this.isRunningInNotifactionList()) {
                        StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getActivity(), 2146);
                    } else {
                        StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getActivity(), 2021);
                    }
                    if (BaseConversationListFragment.this.mListAdapter.hasOpenSwipe()) {
                        MLog.d("Mms_UI_CLFrag", "hasOpenSwipe is true");
                        BaseConversationListFragment.this.mListAdapter.markAllAsReadWhenHasOpenSwipe();
                    } else {
                        BaseConversationListFragment.this.markAllAsRead();
                    }
                    return true;
                case 278927466:
                    LogTag.dumpInternalTables(BaseConversationListFragment.this.getContext());
                    return true;
                case 278927467:
                    HwBaseActivity.gotoCellBroadCast(BaseConversationListFragment.this.getContext());
                    return true;
                case 278927468:
                    StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2114);
                    HwBaseActivity.startMmsActivity(BaseConversationListFragment.this.getContext(), FavoritesActivity.class);
                    return true;
                default:
                    if (BaseConversationListFragment.this.mHwCust == null || !BaseConversationListFragment.this.mHwCust.onCustomMenuItemClick(BaseConversationListFragment.this, item)) {
                        return false;
                    }
            }
            if (reslut == 0) {
                return true;
            }
            if (BaseConversationListFragment.this.getActivity() == null) {
                MLog.e("Mms_UI_CLFrag", "Can't get Activity");
                return true;
            } else if (MmsConfig.isCspVersion()) {
                BaseConversationListFragment.this.finishSelf(true);
                return true;
            } else {
                BaseConversationListFragment.this.mListView.exitEditMode();
                BaseConversationListFragment.this.resetActionBarAfterExitEdit();
                if (BaseConversationListFragment.this.mSearchWrapper != null) {
                    BaseConversationListFragment.this.mSearchWrapper.setSearchStyle(1);
                }
                return true;
            }
        }

        protected void setupMenu(Menu menu) {
            boolean notInNotification = !BaseConversationListFragment.this.isRunningInNotifactionList();
            boolean inEdit = BaseConversationListFragment.this.mActionBar.getActionMode() == 2;
            if (inEdit || !notInNotification) {
                menu.clear();
            } else {
                menu.removeGroup(R.id.mms_options);
            }
            boolean isInLandscape = BaseConversationListFragment.this.isInLandscape();
            resetOptionMenu(menu);
            if (inEdit) {
                addMenu((int) R.id.mms_options, 278927461, (int) R.string.mms_menu_pin, getDrawableId(278927461, isInLandscape));
                if (BaseConversationListFragment.this.mListView != null && BaseConversationListFragment.this.mListView.getSelectedCount() > 0) {
                    setPinup(BaseConversationListFragment.this.mListListener.doPinOrUnpin(BaseConversationListFragment.this.mListView.getSelectedCount(), BaseConversationListFragment.this.mListView.getCount()));
                }
                addMenu((int) R.id.mms_options, 278925315, (int) R.string.delete, getDrawableId(278925315, isInLandscape));
                addMenu((int) R.id.mms_options, 278925313, (int) R.string.menu_select_all, getDrawableId(278925313, isInLandscape));
                return;
            }
            if (notInNotification) {
                addMenu((int) R.id.mms_options, 278927460, (int) R.string.write_message, getDrawableId(278927460, isInLandscape));
                setItemEnabled(278927460, BaseConversationListFragment.this.mIsSmsEnabled);
            }
            if (BaseConversationListFragment.this.mHwCust != null) {
                BaseConversationListFragment.this.mHwCust.setRcsMenu(this, getDrawableId(278927460, isInLandscape), getDrawableId(278927471, isInLandscape), isInLandscape);
            }
            if (notInNotification && PrivacyStateListener.self().isInPrivacyMode()) {
                addOverflowMenu(R.id.mms_options, 278927468, R.string.mms_myfavorite_common);
            }
            if (BaseConversationListFragment.this.mIsSmsEnabled && notInNotification && PreferenceUtils.isSmsRecoveryEnable(BaseConversationListFragment.this.getContext())) {
                addOverflowMenu(R.id.mms_options, 278925349, R.string.sms_recovery_title);
            }
            if (notInNotification && HwMessageUtils.isCbsEnabled(BaseConversationListFragment.this.getContext())) {
                addOverflowMenu(R.id.mms_options, 278927467, R.string.menu_cell_broadcasts);
            }
            boolean isSecondaryUser = OsUtil.isAtLeastL() ? OsUtil.isSecondaryUser() : false;
            if (HwMessageUtils.checkHarassmentService() && !isSecondaryUser) {
                MenuItem menuItem = addOverflowMenu(R.id.mms_options, 278925340, R.string.conv_menu_harassment_filter);
                if (HwMessageUtils.getUnreadHarassmentSmsCount(BaseConversationListFragment.this.getContext()) > 0) {
                    String str = BaseConversationListFragment.this.getResources().getString(R.string.mms_conv_menu_harassment_filter, new Object[]{Integer.valueOf(HwMessageUtils.getUnreadHarassmentSmsCount(BaseConversationListFragment.this.getContext()))});
                    SpannableStringBuilder bulid = new SpannableStringBuilder(str);
                    bulid.setSpan(new ForegroundColorSpan(BaseConversationListFragment.this.getResources().getColor(R.color.mms_unread_text_color)), 0, str.length(), 18);
                    menuItem.setTitle(bulid);
                }
            }
            boolean isMarkAllReadEnable = false;
            MLog.d("Mms_UI_CLFrag", "has unread messages!,isMarkAllReadEnable=" + Conversation.hasUnreadMsg());
            if (HwMessageUtils.isSplitOn() && BaseConversationListFragment.this.mIsSmsEnabled && Conversation.hasUnreadMsg()) {
                isMarkAllReadEnable = true;
            }
            if (BaseConversationListFragment.this.mIsSmsEnabled && BaseConversationListFragment.this.mListView.getCount() > 0 && Conversation.hasUnreadMsg()) {
                isMarkAllReadEnable = true;
            }
            if (notInNotification) {
                addOverflowMenu(R.id.mms_options, 278927465, R.string.menu_make_all_as_read);
                if (isSecondaryUser) {
                    MLog.d("Mms_UI_CLFrag", "Settings not support in secondary user");
                } else {
                    addOverflowMenu(R.id.mms_options, 278925317, R.string.menu_preferences);
                }
            } else {
                if (MmsConfig.isInSimpleUI()) {
                    addOverflowMenu(R.id.mms_options, 278927465, R.string.menu_make_all_as_read);
                } else {
                    addMenu((int) R.id.mms_options, 278927465, (int) R.string.menu_make_all_as_read, getDrawableId(278927465, isInLandscape));
                }
                if (isSecondaryUser) {
                    MLog.d("Mms_UI_CLFrag", "Smart archive settings not support in secondary user");
                } else {
                    addOverflowMenu(R.id.mms_options, 278925350, R.string.smart_archive_title);
                    if (!(BaseConversationListFragment.this.mHwCustBaseConversationListFragment != null ? BaseConversationListFragment.this.mHwCustBaseConversationListFragment.isServiceMessageEnabled() : false)) {
                        addOverflowMenu(R.id.mms_options, 278925350, R.string.smart_archive_title);
                    }
                }
            }
            setItemEnabled(278927465, isMarkAllReadEnable);
        }

        public void dismissPopup() {
            if (BaseConversationListFragment.this.mCustomActionBar != null) {
                BaseConversationListFragment.this.mCustomActionBar.dismissPopup();
            }
        }

        public boolean isPopupShowing() {
            if (BaseConversationListFragment.this.mCustomActionBar == null) {
                return false;
            }
            return BaseConversationListFragment.this.mCustomActionBar.isPopShow();
        }
    }

    public static class DeleteThreadListener implements DialogInterface.OnClickListener {
        private final Context mDeleteContext;
        private boolean mDeleteLockedMessages;
        Runnable mDeleteRunner = new Runnable() {
            public void run() {
                BaseConversationListFragment.sDeleteStartTime = SystemClock.uptimeMillis();
                if (DeleteThreadListener.this.mThreadIds == null) {
                    Conversation.startDeleteAll(DeleteThreadListener.this.mQueryHandler, AMapException.CODE_AMAP_CLIENT_ERROR_PROTOCOL, DeleteThreadListener.this.mDeleteLockedMessages);
                    DraftCache.getInstance().refresh();
                    BaseConversationListFragment.sDeleteCount = -1;
                    DeleteThreadListener.this.mInfoMsgsFlag = 3;
                } else {
                    boolean isDelete = true;
                    if (RcsConversationUtils.getHwCustUtils() != null && RcsConversationUtils.getHwCustUtils().switchToXmsThreadIds(DeleteThreadListener.this.mDeleteContext, DeleteThreadListener.this.mThreadIds, DeleteThreadListener.this.mQueryHandler)) {
                        isDelete = false;
                    }
                    if (isDelete) {
                        Conversation.startDeleteOnceForAll(DeleteThreadListener.this.mQueryHandler, AMapException.CODE_AMAP_CLIENT_ERROR_PROTOCOL, DeleteThreadListener.this.mDeleteLockedMessages, DeleteThreadListener.this.mThreadIds);
                        DraftCache.getInstance().setDraftState(DeleteThreadListener.this.mThreadIds, false);
                    }
                    BaseConversationListFragment.sDeleteCount = (long) DeleteThreadListener.this.mThreadIds.size();
                }
                if (DeleteThreadListener.this.mOnDeleteCompleteRunner != null) {
                    DeleteThreadListener.this.mOnDeleteCompleteRunner.run();
                }
                long time = System.currentTimeMillis();
                if ((DeleteThreadListener.this.mInfoMsgsFlag & 1) != 0) {
                    StatisticalHelper.reportEvent(DeleteThreadListener.this.mDeleteContext, 2052, StatisticalHelper.getFormatTime(time));
                }
                if ((DeleteThreadListener.this.mInfoMsgsFlag & 2) != 0) {
                    StatisticalHelper.reportEvent(DeleteThreadListener.this.mDeleteContext, 2053, StatisticalHelper.getFormatTime(time));
                }
                if (DeleteThreadListener.this.mNotificationMsg) {
                    StatisticalHelper.incrementReportCount(DeleteThreadListener.this.mDeleteContext, 2190);
                } else {
                    StatisticalHelper.incrementReportCount(DeleteThreadListener.this.mDeleteContext, 2187);
                }
            }
        };
        private int mInfoMsgsFlag = 0;
        private boolean mNotificationMsg = false;
        private Runnable mOnDeleteCompleteRunner;
        private final ConversationQueryHandler mQueryHandler;
        private final Collection<Long> mThreadIds;

        public DeleteThreadListener(Context context, Collection<Long> threadIds, ConversationQueryHandler handler) {
            this.mThreadIds = threadIds;
            this.mQueryHandler = handler;
            this.mDeleteContext = context;
            this.mOnDeleteCompleteRunner = null;
        }

        public void setDeleteLockedMessage(boolean deleteLockedMessages) {
            this.mDeleteLockedMessages = deleteLockedMessages;
        }

        public void setInfoMsgsFlag(int flag) {
            this.mInfoMsgsFlag = flag;
        }

        public void setNotificationMsg(boolean notificationMsg) {
            this.mNotificationMsg = notificationMsg;
        }

        public void setSwipeDeleteThreadId(long threadId) {
            if (this.mThreadIds != null) {
                this.mThreadIds.clear();
                this.mThreadIds.add(Long.valueOf(threadId));
            }
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            MessageUtils.handleReadReport(this.mDeleteContext, this.mThreadIds, 129, this.mDeleteRunner);
        }

        public void comfirmOnClick() {
            MessageUtils.handleReadReport(this.mDeleteContext, this.mThreadIds, 129, this.mDeleteRunner);
        }
    }

    private class ListViewListener implements EmuiListViewListener, OnKeyListener, OnItemLongClickListener, SelectionChangedListener {
        private EditHandler mDeleteHandler;
        private TypedEditHandler mPinHandler;

        private ListViewListener() {
            this.mDeleteHandler = new EditHandler() {
                public int handeleSelecte(Long[] selectedItems, boolean isAllSelected) {
                    if (selectedItems.length > 0) {
                        List<Long> list = Arrays.asList(selectedItems);
                        if (BaseConversationListFragment.this.mHwCust != null) {
                            list = BaseConversationListFragment.this.mHwCust.getNewArrayList(list);
                        }
                        BaseConversationListFragment.this.setThreadIdToShowAfterDelete(list, isAllSelected);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BaseConversationListFragment.this.getContext());
                        if (!MmsConfig.isSmsRecyclerEnable() || PreferenceUtils.isSmsRecoveryEnable(BaseConversationListFragment.this.getContext()) || prefs.getBoolean("pref_sms_recycle_not_show_again", false)) {
                            BaseConversationListFragment.confirmDeleteThreads(list, BaseConversationListFragment.this.mQueryHandler);
                        } else {
                            BaseConversationListFragment.this.showSmsRecycleEnableDialog(list, BaseConversationListFragment.this.mQueryHandler);
                        }
                    }
                    return selectedItems.length;
                }
            };
            this.mPinHandler = new TypedEditHandler() {
                public int handeleSelecte(Long[] selectedItems, boolean isAllSelected) {
                    Activity activity = BaseConversationListFragment.this.getActivity();
                    if (selectedItems.length > 0 && activity != null) {
                        boolean isPinup = this.mOpType == 278927461;
                        if (RcsConversationUtils.getHwCustUtils() == null || !RcsConversationUtils.getHwCustUtils().isRcsSwitchOn()) {
                            Conversation.pinConversation(activity, ListViewListener.this.filtUnmatchedConnversations(selectedItems, isPinup), isPinup);
                        } else {
                            RcsConversationUtils.getHwCustUtils().pinConversationRcs(activity, ListViewListener.this.filtUnmatchedConnversations(selectedItems, isPinup), isPinup);
                        }
                    }
                    return selectedItems.length;
                }

                public int getOperation(Long[] selectedItems) {
                    return Conversation.hasUnpinnedConversation(selectedItems) ? 278927461 : 278927462;
                }
            };
        }

        private Collection<Long> filtUnmatchedConnversations(Long[] threadIds, boolean pinned) {
            ArrayList<Long> valideItem = new ArrayList(threadIds.length);
            for (Long id : threadIds) {
                if (Conversation.isPinned(id.longValue()) != pinned) {
                    valideItem.add(id);
                } else {
                    MLog.d("Mms_UI_CLFrag", "pin conversation with same state ]" + id + "] " + pinned);
                }
            }
            return valideItem;
        }

        public void onEnterEditMode() {
            boolean z;
            if (HwMessageUtils.isSplitOn()) {
                ((ConversationList) BaseConversationListFragment.this.getActivity()).showOrHideRightCover();
                BaseConversationListFragment.this.mActionBar.setTitleGravityCenter(true);
            }
            BaseConversationListFragment.this.mMenuEx.onEnterEditMode();
            AbstractEmuiActionBar abstractEmuiActionBar = BaseConversationListFragment.this.mActionBar;
            if (BaseConversationListFragment.this.isRunningInNotifactionList()) {
                z = false;
            } else {
                z = true;
            }
            abstractEmuiActionBar.setDestronyWhenExitActionMode(z);
            BaseConversationListFragment.this.mActionBar.setStartIconDescription(BaseConversationListFragment.this.getContext().getString(R.string.no));
        }

        public void onExitEditMode() {
            if (HwMessageUtils.isSplitOn()) {
                ((ConversationList) BaseConversationListFragment.this.getActivity()).showOrHideRightCover();
                BaseConversationListFragment.this.mActionBar.setTitleGravityCenter(false);
            }
            BaseConversationListFragment.this.mListAdapter.notifyDataSetChanged();
            BaseConversationListFragment.this.mMenuEx.onExitEditMode();
            setLongpressEnabled(false);
            BaseConversationListFragment.this.mActionBar.setStartIconDescription(BaseConversationListFragment.this.getContext().getString(R.string.up_navigation));
        }

        public EditHandler getHandler(int mode) {
            if (1 == mode) {
                return this.mDeleteHandler;
            }
            return null;
        }

        public String getHintText(int mode, int count) {
            return ResEx.self().getOperTextDelete(count);
        }

        public int getHintColor(int mode, int count) {
            return ResEx.self().getCachedColor(count > 0 ? R.drawable.text_color_red : R.color.sms_number_save_disable);
        }

        private void setLongpressEnabled(boolean enable) {
            BaseConversationListFragment.this.mListView.setLongClickable(enable);
            if (enable) {
                BaseConversationListFragment.this.mListView.setOnItemLongClickListener(BaseConversationListFragment.this.mConversationLongClickListener);
                return;
            }
            BaseConversationListFragment.this.mListView.setOnLongClickListener(null);
            BaseConversationListFragment.this.mListView.setOnCreateContextMenuListener(null);
        }

        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == 0) {
                switch (keyCode) {
                    case Place.TYPE_NIGHT_CLUB /*67*/:
                        long id = BaseConversationListFragment.this.mListView.getSelectedItemId();
                        if (id > 0) {
                            BaseConversationListFragment.confirmDeleteThread(id, BaseConversationListFragment.this.mQueryHandler);
                        }
                        return true;
                }
            }
            return false;
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
            HwBaseActivity.startMmsActivity(BaseConversationListFragment.this.getActivity(), ConversationEditor.class, null, false);
            return true;
        }

        private boolean doPinOrUnpin(int selectedSize, int totalSize) {
            if (selectedSize == 0) {
                return true;
            }
            return Conversation.hasUnpinnedConversation(BaseConversationListFragment.this.mListView.getRecorder().getAllSelectItems());
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onSelectChange(int selectedSize, int totalSize) {
            boolean z = true;
            Activity activity = BaseConversationListFragment.this.getActivity();
            if (activity != null && !activity.isFinishing() && totalSize != 0 && selectedSize <= totalSize) {
                boolean z2;
                if (BaseConversationListFragment.this.mUnEditableItemCount < 0) {
                    if (BaseConversationListFragment.this.isRunningInNotifactionList()) {
                        BaseConversationListFragment.this.mUnEditableItemCount = 0;
                    } else {
                        BaseConversationListFragment.this.initUnEditableItemCount();
                    }
                }
                BaseConversationListFragment baseConversationListFragment = BaseConversationListFragment.this;
                if (selectedSize <= 0 || selectedSize != totalSize - BaseConversationListFragment.this.mUnEditableItemCount) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                baseConversationListFragment.mIsAllSelected = z2;
                BaseConversationListFragment.this.mMenuEx.setAllChecked(BaseConversationListFragment.this.mIsAllSelected, BaseConversationListFragment.this.isInLandscape());
                if (totalSize == BaseConversationListFragment.this.mUnEditableItemCount) {
                    BaseConversationListFragment.this.mMenuEx.setItemEnabled(278925313, false);
                } else {
                    BaseConversationListFragment.this.mMenuEx.setItemEnabled(278925313, true);
                }
                BaseConversationListFragment.this.mMenuEx.setPinup(doPinOrUnpin(selectedSize, totalSize));
                BaseFragmentMenu baseFragmentMenu = BaseConversationListFragment.this.mMenuEx;
                if (selectedSize > 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                baseFragmentMenu.setItemEnabled(278925315, z2);
                BaseFragmentMenu baseFragmentMenu2 = BaseConversationListFragment.this.mMenuEx;
                if (selectedSize <= 0) {
                    z = false;
                }
                baseFragmentMenu2.setItemEnabled(278927461, z);
                BaseConversationListFragment.this.mActionBar.setUseSelecteSize(selectedSize);
            }
        }
    }

    public class SearchCursorAdapter extends CursorAdapter {
        String mMatchString;
        HashMap<String, Pattern> regCache = new HashMap();

        public SearchCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
            this.regCache.clear();
            this.mMatchString = HwMessageUtils.formatRegexString(BaseConversationListFragment.this.mSearchText);
        }

        private Pattern getPattern(String matchString) {
            if (TextUtils.isEmpty(matchString)) {
                return null;
            }
            Pattern ptn = (Pattern) this.regCache.get(matchString);
            if (ptn == null) {
                try {
                    ptn = Pattern.compile(matchString, 2);
                } catch (PatternSyntaxException e) {
                    MLog.e("Mms_UI_CLFrag", "Can't get pattern for [" + matchString + "]", (Throwable) e);
                }
                this.regCache.put(matchString, ptn);
            }
            return ptn;
        }

        public int getItemViewType(int position) {
            return getItemViewType((Cursor) getItem(position));
        }

        private int getItemViewType(Cursor cursor) {
            if (cursor == null) {
                return 1;
            }
            int type = cursor.getInt(7);
            return (type == 0 || type == 2) ? 0 : 1;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public void bindView(View view, Context context, Cursor cursor) {
            if (view instanceof SearchMessageListItem) {
                ((SearchMessageListItem) view).bind(this, context, cursor, cursor.getInt(7));
            } else {
                MLog.e("Mms_UI_CLFrag", "Unexpected bound view: " + view);
            }
        }

        public void bindHintView(SearchMessageListItem listItem, Context context, Cursor cursor, int type) {
            ViewHodler holder = (ViewHodler) listItem.getTag();
            if (holder == null || holder.mHintView == null) {
                MLog.e("Mms_UI_CLFrag", "SearchActivity bindHintView with empty hodler content");
                return;
            }
            String title;
            NumberFormat.getIntegerInstance().setGroupingUsed(false);
            int num = cursor.getInt(1);
            if (type == 2) {
                title = BaseConversationListFragment.this.getResources().getQuantityString(R.plurals.mms_search_results_messages_new, num, new Object[]{nf.format((long) num)});
            } else {
                title = BaseConversationListFragment.this.getResources().getQuantityString(R.plurals.mms_search_results_threads_new, num, new Object[]{nf.format((long) num)});
            }
            holder.mHintView.setText(title);
        }

        public void bindMessageView(SearchMessageListItem listItem, Context context, Cursor cursor) {
            ViewHodler holder = (ViewHodler) listItem.getTag();
            if (holder == null || holder.mTitle == null || holder.mSnippet == null) {
                MLog.e("Mms_UI_CLFrag", "SearchActivity bindMessageView with empty hodler content");
                return;
            }
            String address;
            holder.mRowId = SearchDataLoader.parseCursorMsgId(cursor);
            holder.mThreadId = SearchDataLoader.parseCursorThreadId(cursor);
            holder.mWhichTable = SearchDataLoader.parseCursorTableType(cursor);
            holder.mTime = SearchDataLoader.parseCursorDataTime(cursor);
            long threadId = holder.mThreadId;
            Conversation cov = null;
            ContactList contactList = null;
            String str = null;
            boolean isRcsGroupChat = holder.mWhichTable == 302;
            boolean isRcsGroupMessage = holder.mWhichTable == SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE;
            if (threadId > 0 && (isRcsGroupChat || isRcsGroupMessage)) {
                cov = Cache.getRcsGroupCacheByGroupChatId(threadId);
            } else if (threadId > 0) {
                cov = Conversation.get(context, threadId, false);
            }
            if (cov != null) {
                str = cov.getRecipients().getPurpose();
                contactList = cov.getRecipients();
            }
            if (threadId > 0) {
                if (isRcsGroupChat) {
                    isRcsGroupMessage = true;
                }
                listItem.updateAvatarIcon(contactList, isRcsGroupMessage);
            } else if (threadId == 0) {
                listItem.updateFavoritesInSearchAvatarIcon();
            }
            boolean isMms = false;
            boolean isFav = false;
            boolean isThread = false;
            switch (holder.mWhichTable) {
                case 2:
                    isMms = true;
                    break;
                case 8:
                    isFav = true;
                    break;
                case 9:
                    isMms = true;
                    isFav = true;
                    break;
                case 10:
                    isThread = true;
                    break;
            }
            String str2 = null;
            if (isThread) {
                address = cursor.getString(BaseConversationListFragment.POS_ADDRESS);
                str2 = HwMessageUtils.formatRegexString(cursor.getString(BaseConversationListFragment.POS_INDEX_TEXT));
            } else if (isFav) {
                address = context.getString(R.string.hint_favorites_message);
            } else if (BaseConversationListFragment.this.mHwCust != null && BaseConversationListFragment.this.mHwCust.isRcsTable(holder.mWhichTable)) {
                address = BaseConversationListFragment.this.mHwCust.getAddress(context, holder.mWhichTable, threadId, cursor);
                str2 = BaseConversationListFragment.this.mHwCust.getMatch(holder.mWhichTable, cursor);
                isThread = BaseConversationListFragment.this.mHwCust.isThread(holder.mWhichTable, isThread);
            } else if (cov != null) {
                address = cov.getRecipients().formatNames(", ");
            } else {
                address = cursor.getString(BaseConversationListFragment.POS_ADDRESS);
                Contact contact = address != null ? Contact.get(address, false) : null;
                if (holder.mRowId != -1) {
                    address = contact != null ? contact.getName() : "";
                }
            }
            if (str == null || str.isEmpty()) {
                Pattern pattern;
                TextViewSnippet -get6 = holder.mTitle;
                if (str2 == null) {
                    pattern = null;
                } else {
                    pattern = getPattern(str2);
                }
                -get6.setText(address, str2, pattern);
            } else {
                holder.mTitle.setText(address, str, str2, str2 == null ? null : getPattern(str2));
            }
            String bodyStr = SearchDataLoader.parseCursorMsgBody(cursor);
            if (isMms) {
                if (!TextUtils.isEmpty(bodyStr)) {
                    try {
                        bodyStr = new String(bodyStr.getBytes("ISO-8859-1"), "UTF-8");
                    } catch (Exception e) {
                        MLog.e("Mms_UI_CLFrag", "UnsupportedEncodingException for new body string ");
                    }
                }
                if (bodyStr == null || !bodyStr.contains(BaseConversationListFragment.this.mSearchText)) {
                    bodyStr = cursor.getString(BaseConversationListFragment.POS_INDEX_TEXT);
                }
                if (TextUtils.isEmpty(bodyStr)) {
                    bodyStr = context.getString(R.string.no_subject_view);
                }
            }
            if (isThread) {
                holder.mSnippet.setText(bodyStr, null, null);
            } else {
                holder.mSnippet.setText(bodyStr, BaseConversationListFragment.this.mSearchText, getPattern(this.mMatchString));
            }
            if (holder.mTime != 0) {
                holder.mDateView.setText(listItem.buildTime(((int) (System.currentTimeMillis() / holder.mTime)) >= 1000 ? holder.mTime * 1000 : holder.mTime));
            }
            listItem.updateIconStyle(false);
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            if (getItemViewType(cursor) == 0) {
                View v = inflater.inflate(R.layout.search_result_hint, parent, false);
                v.setTag(new ViewHodler(v, true));
                v.setClickable(false);
                v.setFocusableInTouchMode(false);
                return v;
            }
            v = inflater.inflate(R.layout.search_item, parent, false);
            v.setTag(new ViewHodler(v, false));
            v.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    StatisticalHelper.incrementReportCount(BaseConversationListFragment.this.getContext(), 2180);
                    ViewHodler holder = (ViewHodler) v.getTag();
                    if (10 == holder.mWhichTable) {
                        if (HwMessageUtils.isSplitOn()) {
                            Intent itt = ComposeMessageActivity.createIntent(BaseConversationListFragment.this.getActivity(), holder.mThreadId);
                            itt.putExtra("fromSearch", true);
                            itt.putExtra("table_to_use", 10);
                            BaseConversationListFragment.this.startActivityOverrideAnimation(itt);
                        } else {
                            BaseConversationListFragment.this.mSearchMsgUtils.gotoComposeActivity(holder.mThreadId);
                        }
                        return;
                    }
                    BaseConversationListFragment.this.mSearchMsgUtils.gotoTargetActivity(holder.mWhichTable, holder.mRowId, (int) holder.mThreadId);
                }
            });
            return v;
        }

        protected void onContentChanged() {
            super.onContentChanged();
            MLog.w("Mms_UI_CLFrag", "Search Content changed, rebuild data");
            BaseConversationListFragment.this.mSearchLoader.onDataChanged(0);
            BaseConversationListFragment.this.mSearchLoader.onDataChanged(1);
        }
    }

    private final class ThreadListQueryHandler extends ConversationQueryHandler {
        public void startQuery(int token, Object cookie, Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
            if (BaseConversationListFragment.this.mHwCust != null) {
                selection = BaseConversationListFragment.this.mHwCust.getNewSelection(token, cookie, selection);
            }
            super.startQuery(token, cookie, uri, projection, selection, selectionArgs, orderBy);
        }

        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Activity activity = BaseConversationListFragment.this.getActivity();
            if (activity == null) {
                MLog.e("Mms_UI_CLFrag", "onQueryComplete:: activity is finished, do nothing!");
                return;
            }
            switch (token) {
                case 1701:
                    if (cursor != null) {
                        MLog.d("Mms_UI_CLFrag", "CovnersationList Query Data Finish " + cursor.getCount());
                        BaseConversationListFragment.this.startQueryUnreadMessageCount(BaseConversationListFragment.this.mQueryHandler, 1807, BaseConversationListFragment.this.mRunningMode);
                        BaseConversationListFragment.this.onDataReady(cursor);
                        if (BaseConversationListFragment.this.mMenuEx != null && BaseConversationListFragment.this.mMenuEx.isPopupShowing() && HwMessageUtils.isSplitOn()) {
                            BaseConversationListFragment.this.mMenuEx.dismissPopup();
                            break;
                        }
                    }
                    MLog.e("Mms_UI_CLFrag", "CovnersationList onQueryComplete. with null cursor ");
                    return;
                case AMapException.CODE_AMAP_CLIENT_SOCKET_TIMEOUT_EXCEPTION /*1802*/:
                    if (processLockedMessagesToken(cookie, cursor, activity)) {
                        return;
                    }
                    break;
                case 1805:
                    Collection allThreadIds = new ArrayList();
                    allThreadIds.addAll((Collection) cookie);
                    while (cursor.moveToNext()) {
                        try {
                            allThreadIds.add(Long.valueOf(cursor.getLong(0)));
                        } catch (Exception e) {
                            MLog.e("Mms_UI_CLFrag", "get notification id failed : " + e.getMessage());
                            break;
                        } catch (Throwable th) {
                            try {
                                cursor.close();
                            } catch (Exception e2) {
                                MLog.e("Mms_UI_CLFrag", "cursor close error : " + e2.getMessage());
                            }
                        }
                    }
                    Conversation.startQueryHaveLockedMessages(BaseConversationListFragment.this.mQueryHandler, allThreadIds, (int) AMapException.CODE_AMAP_CLIENT_SOCKET_TIMEOUT_EXCEPTION);
                    try {
                        cursor.close();
                        break;
                    } catch (Exception e22) {
                        MLog.e("Mms_UI_CLFrag", "cursor close error : " + e22.getMessage());
                        break;
                    }
                case AMapException.CODE_AMAP_CLIENT_NETWORK_EXCEPTION /*1806*/:
                    if (cursor != null) {
                        Conversation.sethasUnreadMsg(cursor.getCount() > 0);
                        BaseConversationListFragment.this.mMenuEx.setItemEnabled(278927465, cursor.getCount() > 0);
                        cursor.close();
                        break;
                    }
                    MLog.e("Mms_UI_CLFrag", "CovnersationList onQueryComplete. with null cursor ");
                    return;
                case 1807:
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            BaseConversationListFragment.this.mUnreadCount = cursor.getInt(0);
                            BaseConversationListFragment.this.mActionBar.changeListTitleNumber(BaseConversationListFragment.this.mUnreadCount);
                        }
                        cursor.close();
                        break;
                    }
                    MLog.e("Mms_UI_CLFrag", "CovnersationList onQueryComplete QUERY_UNREAD_MESSAGE_COUNT_TOKEN. with null cursor ");
                    return;
                case AMapException.CODE_AMAP_SERVICE_MAINTENANCE /*2002*/:
                    if (cursor.moveToFirst()) {
                        BaseConversationListFragment.this.gotoComposeMessage(Conversation.from(BaseConversationListFragment.this.getContext(), cursor), false, true);
                        break;
                    }
                    break;
                case AMapException.CODE_AMAP_ENGINE_TABLEID_NOT_EXIST /*2003*/:
                    if (cursor.moveToFirst()) {
                        BaseConversationListFragment.this.gotoComposeMessage(Conversation.from(BaseConversationListFragment.this.getContext(), cursor), true, true);
                        break;
                    }
                    break;
                case 2005:
                    String thread_id = "";
                    Map<String, String> repairAddress = new HashMap();
                    while (cursor.moveToNext()) {
                        String address = cursor.getString(cursor.getColumnIndex("address"));
                        thread_id = cursor.getString(cursor.getColumnIndex("thread_id"));
                        if (!TextUtils.isEmpty(address)) {
                            if (repairAddress.containsKey(thread_id)) {
                                repairAddress.put(thread_id, ((String) repairAddress.get(thread_id)) + " " + address);
                            } else {
                                repairAddress.put(thread_id, address);
                            }
                        }
                    }
                    cursor.close();
                    if (!repairAddress.isEmpty()) {
                        BaseConversationListFragment.this.fixConversations(repairAddress);
                        break;
                    }
                    BaseConversationListFragment.this.sRepairing = false;
                    BaseConversationListFragment.this.mReparingEmptyNameThreads.clear();
                    BaseConversationListFragment.this.checkPendingEmptynameThreads();
                    if (BaseConversationListFragment.this.mPendingEmptyNameThreads.size() > 0) {
                        BaseConversationListFragment.this.mReparingEmptyNameThreads.addAll(BaseConversationListFragment.this.mPendingEmptyNameThreads);
                        BaseConversationListFragment.this.mPendingEmptyNameThreads.clear();
                        BaseConversationListFragment.this.startRepairing();
                        break;
                    }
                    break;
                case 2007:
                    if (!activity.isFinishing()) {
                        Collection<Long> swipethreadId = (Collection) cookie;
                        DeleteThreadListener swipelistener = new DeleteThreadListener(activity, swipethreadId, BaseConversationListFragment.this.mQueryHandler);
                        swipelistener.mOnDeleteCompleteRunner = new Runnable() {
                            public void run() {
                                if ((BaseConversationListFragment.this.mRunningMode == 2 || BaseConversationListFragment.this.mListView.isInEditMode() || BaseConversationListFragment.this.mRunningMode == 6) && BaseConversationListFragment.this.getActivity() != null) {
                                    BaseConversationListFragment.this.getActivity().onBackPressed();
                                }
                            }
                        };
                        boolean hasSwipeLockedMsg = cursor != null && cursor.getCount() > 0;
                        if (BaseConversationListFragment.this.mIsAskBeforeDeleting) {
                            BaseConversationListFragment.this.confirmSwipeDeleteThreadDialog(swipelistener, swipethreadId, hasSwipeLockedMsg, false, false, activity);
                        } else {
                            BaseConversationListFragment.this.confirmSwipeDeleteThread(swipelistener, swipethreadId, hasSwipeLockedMsg, false, false, activity);
                        }
                        if (!(cursor == null || cursor.isClosed())) {
                            cursor.close();
                            break;
                        }
                    }
                    MLog.w("Mms_UI_CLFrag", "ConversationList is finished, do nothing ");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                    break;
                default:
                    MLog.e("Mms_UI_CLFrag", "onQueryComplete called with unknown token " + token);
                    break;
            }
        }

        private boolean processLockedMessagesToken(Object cookie, Cursor cursor, Activity activity) {
            if (activity.isFinishing()) {
                MLog.w("Mms_UI_CLFrag", "ConversationList is finished, do nothing ");
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            }
            Collection<Long> threadIds = (Collection) cookie;
            DeleteThreadListener listener = new DeleteThreadListener(activity, threadIds, BaseConversationListFragment.this.mQueryHandler);
            listener.mOnDeleteCompleteRunner = new Runnable() {
                public void run() {
                    if ((BaseConversationListFragment.this.mRunningMode == 2 || BaseConversationListFragment.this.mListView.isInEditMode() || BaseConversationListFragment.this.mRunningMode == 6) && BaseConversationListFragment.this.getActivity() != null) {
                        BaseConversationListFragment.this.getActivity().onBackPressed();
                    }
                }
            };
            boolean hasLockedMsg = cursor != null && cursor.getCount() > 0;
            SelectRecorder selectRecorder = BaseConversationListFragment.this.mListView.getRecorder();
            boolean hasCommNotis = selectRecorder.contains(-10000000012L);
            boolean hasHwNotis = selectRecorder.contains(-10000000011L);
            boolean contains = !selectRecorder.contains(-10000000012L) ? selectRecorder.contains(-10000000011L) : true;
            int infoMsgsFlag = 0;
            if (hasCommNotis || (BaseConversationListFragment.this.mListView.isAllSelected() && 4 == BaseConversationListFragment.this.mRunningMode)) {
                infoMsgsFlag = 1;
            }
            if (hasHwNotis || (BaseConversationListFragment.this.mListView.isAllSelected() && 5 == BaseConversationListFragment.this.mRunningMode)) {
                infoMsgsFlag |= 2;
            }
            listener.setInfoMsgsFlag(infoMsgsFlag);
            boolean z = contains || 4 == BaseConversationListFragment.this.mRunningMode;
            listener.setNotificationMsg(z);
            BaseConversationListFragment.confirmDeleteThreadDialog(listener, threadIds, hasLockedMsg, contains, BaseConversationListFragment.this.mIsAllSelected, activity);
            if (!(cursor == null || cursor.isClosed())) {
                cursor.close();
            }
            return false;
        }

        protected void onUpdateComplete(int token, Object cookie, int result) {
            MLog.d("Mms_UI_CLFrag", "Query handler onUpdateComplete " + token + " result " + result);
            switch (token) {
                case AMapException.CODE_AMAP_CLIENT_UNKNOWHOST_EXCEPTION /*1804*/:
                    Conversation.updatePinupCache(cookie);
                    break;
            }
            super.onUpdateComplete(token, cookie, result);
        }

        protected void onDeleteComplete(int token, final Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);
            final Context context = BaseConversationListFragment.this.getActivity();
            if (context != null) {
                BaseConversationListFragment.this.getActivity().setProgressBarIndeterminateVisibility(false);
                if (BaseConversationListFragment.this.mListAdapter != null && HwMessageUtils.isSplitOn() && BaseConversationListFragment.this.mListAdapter.getIsSwipeDelete()) {
                    BaseConversationListFragment.this.setThreadIdToShowAfterDelete(Arrays.asList(new Long[]{BaseConversationListFragment.this.mSwipeDeleteID}), false);
                    BaseConversationListFragment.this.mListAdapter.setIsSwipeDelete(false);
                    BaseConversationListFragment.this.mSwipeDeleteID = Long.valueOf(-1);
                }
                switch (token) {
                    case AMapException.CODE_AMAP_CLIENT_ERROR_PROTOCOL /*1801*/:
                        HwMessageUtils.showJlogByID(137, (int) (SystemClock.uptimeMillis() - BaseConversationListFragment.sDeleteStartTime), "Mms::delete " + BaseConversationListFragment.sDeleteCount + " conversations!");
                        BaseConversationListFragment.sDeleteStartTime = 0;
                        BaseConversationListFragment.sDeleteCount = 0;
                        HwBackgroundLoader.getInst().postTask(new Runnable() {
                            public void run() {
                                long threadId = -2;
                                String[] threadIds = null;
                                if (cookie == null) {
                                    threadId = -1;
                                } else {
                                    try {
                                        String cookieStr = cookie;
                                        if (cookieStr.indexOf(",") > 0) {
                                            threadIds = cookieStr.split(",");
                                        } else if (-1 == Long.parseLong(cookieStr)) {
                                            threadId = -1;
                                        } else {
                                            threadIds = new String[]{cookieStr};
                                        }
                                    } catch (Exception e) {
                                        MLog.e("Mms_UI_CLFrag", "onDeleteComplete:: occur exception:" + e);
                                    }
                                }
                                if (threadId == -1) {
                                    Contact.clear(context);
                                } else if (threadIds != null) {
                                    int i = 0;
                                    while (i < threadIds.length) {
                                        try {
                                            for (Contact contact : Conversation.get(BaseConversationListFragment.this.getActivity(), Long.parseLong(threadIds[i]), false).getRecipients()) {
                                                contact.removeFromCache();
                                            }
                                            i++;
                                        } catch (Exception e2) {
                                            MLog.e("Mms_UI_CLFrag", "onDeleteComplete:: conversation remove cache, occur exception:" + e2);
                                            return;
                                        }
                                    }
                                }
                            }
                        });
                        HwBackgroundLoader.getInst().clearLoadMark(8);
                        Conversation.clear(context);
                        if (BaseConversationListFragment.this.mListAdapter != null) {
                            BaseConversationListFragment.this.mListAdapter.clearThreadsMap();
                        }
                        MessagingNotification.nonBlockingUpdateNewMessageIndicator(context, -2, false);
                        MessagingNotification.nonBlockingUpdateSendFailedNotification(context);
                        BaseConversationListFragment.this.setIsAfterDelete(true);
                        BaseConversationListFragment.this.startAsyncQuery();
                        MmsWidgetProvider.notifyDatasetChanged(context);
                        break;
                }
            }
        }
    }

    private static class ViewHodler {
        private TextView mDateView;
        private TextView mHintView;
        private int mRowId;
        private TextViewSnippet mSnippet;
        private long mThreadId;
        private long mTime;
        private TextViewSnippet mTitle;
        private int mWhichTable;

        private ViewHodler(View v, boolean isHint) {
            if (isHint) {
                this.mHintView = (TextView) v.findViewById(R.id.search_title);
                return;
            }
            this.mTitle = (TextViewSnippet) v.findViewById(R.id.title);
            this.mSnippet = (TextViewSnippet) v.findViewById(R.id.subtitle);
            this.mDateView = (TextView) v.findViewById(R.id.date_time);
        }
    }

    protected abstract void clickToSearchMode();

    protected abstract AbstractEmuiActionBar createEmuiActionBar(View view);

    protected abstract void invalidateOptionsMenu();

    private Handler getUiHandler() {
        return HwBackgroundLoader.getUIHandler();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initUnEditableItemCount() {
        if (this.mListAdapter != null && this.mListAdapter.getCursor() != null && this.mListView != null && this.mListView.isInEditMode()) {
            this.mUnEditableItemCount = 0;
            for (int pos = 0; pos < 2; pos++) {
                long itemId = this.mListAdapter.getItemId(pos);
                if (-10000000012L == itemId || -10000000011L == itemId) {
                    this.mUnEditableItemCount++;
                }
            }
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mHwCust == null) {
            this.mHwCust = new RcsConversationListFragment(getActivity());
        }
        this.mHwCustBaseConversationListFragment = (HwCustBaseConversationListFragment) HwCustUtils.createObj(HwCustBaseConversationListFragment.class, new Object[0]);
        this.mDoOnceAfterFirstQuery = true;
        HwBackgroundLoader.getInst().sendTask(2);
        this.mQueryHandler = new ThreadListQueryHandler(getContext().getContentResolver());
        this.mRunningMode = getRunningMode();
        MLog.d("Mms_UI_CLFrag", "mRunningMode:" + this.mRunningMode);
        checkAsyncQuery();
        regiesterPriavcyMonitor();
        registerDefSmsAppChanged();
        if (this.mHwCust != null) {
            this.mHwCust.registerOtherListenerOnCreate(this.mMenuEx);
        }
        initLocalBroadcast();
        this.mMenuEx.setContext(getActivity());
        registBroadcast();
        checkHwCloudServiceStatus();
        registerContactDataChangeObserver();
    }

    private void checkHwCloudServiceStatus() {
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                HwCloudBackUpManager.getInstance().checkNeedShowHwCloudAlert(BaseConversationListFragment.this.getActivity());
                HwCloudBackUpManager.getInstance().showHwCloudServiceGuideAlert(BaseConversationListFragment.this.getActivity());
            }
        });
    }

    public void registBroadcast() {
        if (this.mAddressChangeReceiver == null) {
            this.mAddressChangeReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent != null) {
                        if ("com.android.huawei.notification.ADDRESS_CHANGE".equals(intent.getAction())) {
                            MLog.d("Mms_UI_CLFrag", "com.android.huawei.notification.ADDRESS_CHANGE is received,and mPendingEmptyNameThreads size is " + BaseConversationListFragment.this.mPendingEmptyNameThreads.size());
                            BaseConversationListFragment.this.sRepairing = false;
                            BaseConversationListFragment.this.mReparingEmptyNameThreads.clear();
                            if (BaseConversationListFragment.this.mPendingEmptyNameThreads.size() > 0) {
                                BaseConversationListFragment.this.mReparingEmptyNameThreads.addAll(BaseConversationListFragment.this.mPendingEmptyNameThreads);
                                BaseConversationListFragment.this.mPendingEmptyNameThreads.clear();
                                BaseConversationListFragment.this.startRepairing();
                            }
                            BaseConversationListFragment.this.startAsyncQuery();
                        }
                    }
                }
            };
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.huawei.notification.ADDRESS_CHANGE");
        getContext().registerReceiver(this.mAddressChangeReceiver, filter, permission.ADDRESS_CHANGE, null);
    }

    void enterSearchMode() {
        this.mActionBar.enterSearchMode();
        this.mSearchWrapper = new SearchViewWrapper(getActivity());
        if (this.mSearchMsgUtils == null) {
            this.mSearchMsgUtils = new SearchMsgUtils(getActivity());
        }
        this.mSearchWrapper.init(this.mActionBar.getSearchView(), 3);
        this.mSearchWrapper.setSearchViewListener(this.mSearchViewListener);
        this.mMenuEx.onEnterSearchMode();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootLayoutId == -1) {
            MLog.e("Mms_UI_CLFrag", "Not specify the fragment layout");
            return null;
        }
        View rootView = null;
        try {
            Activity act;
            if (OsUtil.isAppStart() && this.mRunningMode == 0 && !HwMessageUtils.isSplitOn()) {
                rootView = HwBackgroundLoader.getCachedConversationListViews(1);
                if (rootView == null) {
                    rootView = inflater.inflate(this.mRootLayoutId, container, false);
                }
                if (checkIsFragmentActived()) {
                    ConversationListItem.cacheConversationViews(getActivity(), 0);
                }
                act = getActivity();
                if (act != null) {
                    this.mSearchViewHight = act.getResources().getDimension(R.dimen.search_header_height);
                    if (this.mHwCust != null) {
                        this.mHwCust.inflateRcsDisconnectNotify(rootView);
                    }
                    this.mActionBar = createEmuiActionBar(rootView);
                    if (this.mActionBar != null) {
                        if (this.mRunningMode == 4) {
                            this.mActionBar.setListTitle(act.getResources().getString(R.string.mms_common_notification), this.mUnreadCount);
                            this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
                                public void onClick(View v) {
                                    BaseConversationListFragment.this.backToConversationList(BaseConversationListFragment.this.getActivity());
                                }
                            });
                        } else if (this.mRunningMode == 5) {
                            this.mActionBar.setListTitle(act.getResources().getString(R.string.mms_hw_notification), this.mUnreadCount);
                            this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
                                public void onClick(View v) {
                                    BaseConversationListFragment.this.backToConversationList(BaseConversationListFragment.this.getActivity());
                                }
                            });
                        } else if (!MmsConfig.isCspVersion()) {
                            this.mActionBar.setListTitle(act.getResources().getString(R.string.app_label), this.mUnreadCount);
                        }
                        if (rootView != null) {
                            this.mSearchHeaderView = rootView.findViewById(R.id.mms_search_header_view);
                            this.mListView = (EmuiListView_V3) rootView.findViewById(R.id.emui_list_view);
                            this.mListView.setFastScrollEnabled(true);
                        }
                        if (this.mListView != null) {
                            setFirstStartTab(2);
                            if (MmsConfig.isCspVersion()) {
                                if (rootView != null) {
                                    this.mLoadingView = rootView.findViewById(R.id.mms_fragment_loading);
                                }
                                if (this.mLoadingView != null) {
                                    this.mLoadingView.setVisibility(0);
                                }
                                HwBackgroundLoader.getUIHandler().postDelayed(new Runnable() {
                                    public void run() {
                                        if (-1 == BaseConversationListFragment.this.mDataLoadingStatus) {
                                            BaseConversationListFragment.this.mDataLoadingStatus = 0;
                                            return;
                                        }
                                        if (BaseConversationListFragment.this.mLoadingView != null) {
                                            BaseConversationListFragment.this.mLoadingView.setVisibility(8);
                                        }
                                    }
                                }, 500);
                            }
                            if (savedInstanceState == null) {
                                this.mSavedFirstVisiblePosition = -1;
                                this.mSavedFirstItemOffset = 0;
                            } else {
                                this.mSavedFirstVisiblePosition = savedInstanceState.getInt("last_list_pos", -1);
                                this.mSavedFirstItemOffset = savedInstanceState.getInt("last_list_offset", 0);
                            }
                            initListAdapter(savedInstanceState);
                            setHasOptionsMenu(false);
                            HwBackgroundLoader.getInst().postTask(new Runnable() {
                                public void run() {
                                    if (!PreferenceManager.getDefaultSharedPreferences(BaseConversationListFragment.this.getContext()).getBoolean("checked_message_limits", false)) {
                                        BaseConversationListFragment.this.runOneTimeStorageLimitCheckForLegacyMessages();
                                    }
                                }
                            });
                            if (rootView != null) {
                                this.mImageMask = rootView.findViewById(R.id.image_mask_gray);
                                this.mImageMask.setOnClickListener(this.mImageMaskListener);
                            }
                            Log.logPerformance("onCreateView finsh" + rootView);
                            return rootView;
                        }
                        MLog.e("Mms_UI_CLFrag", "Invalid layout list_view_group can't be find");
                        return null;
                    }
                    MLog.e("Mms_UI_CLFrag", "create EmuiActionBar Failed!");
                    return null;
                }
                MLog.e("Mms_UI_CLFrag", "onCreateView has no Activity");
                return null;
            }
            rootView = inflater.inflate(this.mRootLayoutId, container, false);
            if (checkIsFragmentActived()) {
                ConversationListItem.cacheConversationViews(getActivity(), 0);
            }
            act = getActivity();
            if (act != null) {
                MLog.e("Mms_UI_CLFrag", "onCreateView has no Activity");
                return null;
            }
            this.mSearchViewHight = act.getResources().getDimension(R.dimen.search_header_height);
            if (this.mHwCust != null) {
                this.mHwCust.inflateRcsDisconnectNotify(rootView);
            }
            this.mActionBar = createEmuiActionBar(rootView);
            if (this.mActionBar != null) {
                MLog.e("Mms_UI_CLFrag", "create EmuiActionBar Failed!");
                return null;
            }
            if (this.mRunningMode == 4) {
                this.mActionBar.setListTitle(act.getResources().getString(R.string.mms_common_notification), this.mUnreadCount);
                this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, /* anonymous class already generated */);
            } else if (this.mRunningMode == 5) {
                this.mActionBar.setListTitle(act.getResources().getString(R.string.mms_hw_notification), this.mUnreadCount);
                this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, /* anonymous class already generated */);
            } else if (MmsConfig.isCspVersion()) {
                this.mActionBar.setListTitle(act.getResources().getString(R.string.app_label), this.mUnreadCount);
            }
            if (rootView != null) {
                this.mSearchHeaderView = rootView.findViewById(R.id.mms_search_header_view);
                this.mListView = (EmuiListView_V3) rootView.findViewById(R.id.emui_list_view);
                this.mListView.setFastScrollEnabled(true);
            }
            if (this.mListView != null) {
                MLog.e("Mms_UI_CLFrag", "Invalid layout list_view_group can't be find");
                return null;
            }
            if (2 != getFirstStartTab() && this.mRunningMode == 0) {
                setFirstStartTab(2);
                if (MmsConfig.isCspVersion()) {
                    if (rootView != null) {
                        this.mLoadingView = rootView.findViewById(R.id.mms_fragment_loading);
                    }
                    if (this.mLoadingView != null) {
                        this.mLoadingView.setVisibility(0);
                    }
                    HwBackgroundLoader.getUIHandler().postDelayed(/* anonymous class already generated */, 500);
                }
            }
            if (savedInstanceState == null) {
                this.mSavedFirstVisiblePosition = savedInstanceState.getInt("last_list_pos", -1);
                this.mSavedFirstItemOffset = savedInstanceState.getInt("last_list_offset", 0);
            } else {
                this.mSavedFirstVisiblePosition = -1;
                this.mSavedFirstItemOffset = 0;
            }
            initListAdapter(savedInstanceState);
            setHasOptionsMenu(false);
            HwBackgroundLoader.getInst().postTask(/* anonymous class already generated */);
            if (rootView != null) {
                this.mImageMask = rootView.findViewById(R.id.image_mask_gray);
                this.mImageMask.setOnClickListener(this.mImageMaskListener);
            }
            Log.logPerformance("onCreateView finsh" + rootView);
            return rootView;
        } catch (InflateException e) {
            MLog.e("Mms_UI_CLFrag", "BaseConversationList :: createView :: Exception : ", (Throwable) e);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("last_list_pos", this.mSavedFirstVisiblePosition);
        outState.putInt("last_list_offset", this.mSavedFirstItemOffset);
        outState.putInt("last_view_mode", this.mListView.getViewMode());
    }

    public void onPause() {
        super.onPause();
        this.isDataLoad = false;
        this.mIsFromPause = true;
        if (this.mListAdapter != null && this.mListView != null) {
            this.mSavedFirstVisiblePosition = this.mListView.getFirstVisiblePosition();
            View firstChild = this.mListView.getChildAt(0);
            this.mSavedFirstItemOffset = firstChild == null ? 0 : firstChild.getTop();
            CspFragment.setContactChangedWhenPause(false);
            if (3 == this.mRunningMode && this.mSearchWrapper != null) {
                this.mSearchWrapper.hideSoftInputAndClearFocus();
                MLog.d("Mms_UI_CLFrag", "onPause,SearchWrapper.hideSoftInputAndClearFocus");
            }
        }
    }

    public static void pinUpThreads(long[] threadIds, boolean isPinup, boolean autoPinup, Context context) {
        if (threadIds.length > 0) {
            Bundle extras = new Bundle();
            extras.putLongArray("thread_ids", threadIds);
            extras.putBoolean("pin_up", isPinup);
            extras.putBoolean("autoPin", autoPinup);
            Bundle result = SqliteWrapper.call(context, MmsSms.CONTENT_URI, context.getPackageName(), "method_pin_up", null, extras);
            if (result != null && result.getBoolean("call_result")) {
                MLog.d("Mms_UI_CLFrag", "pinup success");
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mHwCust != null) {
            this.mHwCust.showGroupInviteDialogIfNeeded();
        }
        checkNewIntent();
        checkSmsEnable();
        if (OsUtil.isAppStart()) {
            OsUtil.setAppStart(false);
        }
        if (this.mListAdapter != null) {
            this.mListAdapter.setOnContentChangedListener(this.mContentChangedListener);
        }
        if (3 == this.mRunningMode && this.mSearchWrapper != null) {
            MLog.d("Mms_UI_CLFrag", "on resume,mSearchWrapper.showOrHideSoftInputAndClearFocus");
            if (TextUtils.isEmpty(this.mSearchText)) {
                this.mSearchWrapper.showSoftInputAndGetFocus();
            } else {
                this.mSearchWrapper.hideSoftInputAndClearFocus();
            }
            if (TextUtils.isEmpty(this.mWaitForSearchText) && !TextUtils.isEmpty(this.mSearchText)) {
                this.mWaitForSearchText = this.mSearchText;
                startSearchAsynQuery();
            }
        }
        if (this.mHwCust != null) {
            this.mHwCust.startUserGuide();
        }
        if (this.mIsFromPause) {
            checkAndshowFunctionTips();
        }
    }

    private void checkAndshowFunctionTips() {
        if (isRunningInConversationList() && checkFunctionTipsStatus()) {
            if (this.mMmsFunctionTipsView != null) {
                this.mMmsFunctionTipsView.setVisibility(0);
            } else {
                showUpviewFunctionTips();
            }
        } else if (this.mMmsFunctionTipsView != null) {
            this.mMmsFunctionTipsView.setVisibility(8);
        }
    }

    private boolean checkFunctionTipsStatus() {
        if (this.mListAdapter == null) {
            MLog.e("Mms_UI_CLFrag", "checkFunctionTipsStatus but mListAdapter is null, return false");
            return false;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean globeTipsShowBefore = sp.getBoolean("riskUrlPermission_not_show", false);
        boolean chinaTipsShowBefore = sp.getBoolean(SmartSmsSdkUtil.SMARTSMS_NO_SHOW_AGAIN, false);
        if (globeTipsShowBefore || chinaTipsShowBefore) {
            return false;
        }
        boolean moreThan10Conv = sp.getBoolean("pref_key_mms_had_conversations", false);
        if (!moreThan10Conv && this.mListAdapter.getCount() <= 0) {
            return false;
        }
        if (!moreThan10Conv) {
            sp.edit().putBoolean("pref_key_mms_had_conversations", true).apply();
        }
        if (this.mSmsPromoBannerView == null || this.mSmsPromoBannerView.getVisibility() != 0) {
            return !(Contact.IS_CHINA_REGION || sp.getBoolean("riskUrlPermission_not_show", false)) || (Contact.IS_CHINA_REGION && UserHandle.myUserId() == 0 && MmsConfig.getSupportSmartSmsFeature() && !sp.getBoolean(SmartSmsSdkUtil.SMARTSMS_NO_SHOW_AGAIN, false));
        } else {
            MLog.e("Mms_UI_CLFrag", "checkFunctionTipsStatus but mSmsPromoBannerView is visible, return false");
            return false;
        }
    }

    private void showUpviewFunctionTips() {
        if (this.mMmsFunctionTipsView != null) {
            this.mMmsFunctionTipsView.setVisibility(0);
        } else if (getView() == null) {
            MLog.e("Mms_UI_CLFrag", "showUpviewFunctionTips but getView is null");
        } else {
            ViewStub functionTipsStub = (ViewStub) getView().findViewById(R.id.stub_function_tips);
            if (functionTipsStub == null) {
                MLog.e("Mms_UI_CLFrag", "showUpviewFunctionTips but function stub id was wrong");
                return;
            }
            this.mMmsFunctionTipsView = functionTipsStub.inflate();
            View openView = getView().findViewById(R.id.open_function);
            View cancelView = getView().findViewById(R.id.cancel_tips);
            openView.setOnClickListener(this.mFunctionTipsClickListener);
            cancelView.setOnClickListener(this.mFunctionTipsClickListener);
            TextView titleView = (TextView) getView().findViewById(R.id.tips_title);
            TextView subTitleView = (TextView) getView().findViewById(R.id.tips_summary);
            if (Contact.IS_CHINA_REGION) {
                titleView.setText(R.string.open_function_tips_china);
                subTitleView.setText(R.string.smart_sms_url_function_tips_summary);
            } else {
                titleView.setText(R.string.open_function_tips_globe);
                subTitleView.setText(R.string.risk_url_function_tips_summary);
            }
        }
    }

    private void showNoMessageView(boolean show) {
        if (show) {
            if (this.mNoConvView == null) {
                ViewStub vs = (ViewStub) getView().findViewById(R.id.no_conversation);
                if (vs != null) {
                    this.mNoConvView = (NoMessageView) vs.inflate();
                    this.mNoConvView.setViewType(1);
                } else {
                    return;
                }
            }
            this.mNoConvView.setVisibility(0, getActivity().isInMultiWindowMode());
            if (this.mImageMask != null && this.mImageMask.getVisibility() == 0) {
                this.mImageMask.setVisibility(8);
            }
        } else if (this.mNoConvView != null && this.mNoConvView.getVisibility() == 0) {
            this.mNoConvView.setVisibility(8, getActivity().isInMultiWindowMode());
        }
        if (this.mRunningMode == 3) {
            if (this.mImageMask == null) {
                this.mImageMask = getView().findViewById(R.id.image_mask_gray);
                this.mImageMask.setOnClickListener(this.mImageMaskListener);
            }
            if (this.mSearchWrapper == null || !this.mSearchWrapper.isClearViewVisible()) {
                this.mImageMask.setVisibility(0);
            } else {
                this.mImageMask.setVisibility(8);
            }
        }
    }

    private void hideViewInNoConversation(boolean needHide) {
        if (!needHide) {
            boolean isConversationList = isRunningInConversationList();
            if (this.mSearchHeaderView != null && isConversationList && this.mSearchHeaderView.getVisibility() != 0) {
                this.mSearchHeaderView.setVisibility(0);
            }
        } else if (this.mSearchHeaderView != null) {
            this.mSearchHeaderView.setVisibility(8);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mSearchHeaderView != null && isRunningInConversationList()) {
            this.mIsFirst = true;
            if (this.mSearchHeaderView.getTranslationY() != 0.0f) {
                translateViewInVariTime(this.mSearchHeaderView, 0.0f, false);
            }
        }
        if (this.mListAdapter != null && this.mListAdapter.hasOpenSwipe()) {
            this.mListAdapter.onConfigurationChanged();
        }
        if (this.mSwipeShowconfirmDeleteThreadDialog != null && this.mSwipeShowconfirmDeleteThreadDialog.isShowing()) {
            this.mSwipeShowconfirmDeleteThreadDialog.dismiss();
        }
        if (this.mSwipeShowconfirmDeleteLockThreadDialog != null && this.mSwipeShowconfirmDeleteLockThreadDialog.isShowing()) {
            this.mSwipeShowconfirmDeleteLockThreadDialog.dismiss();
        }
    }

    private void showNoMatchMessageView(boolean show) {
        int i;
        MLog.i("Mms_UI_CLFrag", "showNoMatchMessageView=" + show);
        if (this.mNoConvView == null) {
            if (show) {
                View view = getView();
                if (view != null) {
                    ViewStub vs = (ViewStub) view.findViewById(R.id.no_conversation);
                    if (vs != null) {
                        this.mNoConvView = (NoMessageView) vs.inflate();
                    } else {
                        return;
                    }
                }
                return;
            }
            return;
        }
        this.mNoConvView.setViewType(5);
        NoMessageView noMessageView = this.mNoConvView;
        if (show) {
            i = 0;
        } else {
            i = 8;
        }
        noMessageView.setVisibility(i);
    }

    private void initListAdapter(Bundle savedInstanceState) {
        Activity act = getActivity();
        if (act == null) {
            MLog.e("Mms_UI_CLFrag", "InitListAdapter with empty activity");
            return;
        }
        this.mListAdapter = new ConversationListAdapter(act, null, this.mListView.getListView(), MessageUtils.getResoureId(act));
        this.mListView.setAdapter(this.mListAdapter);
        this.mListAdapter.setSwipeCallback(new SwipeCallback() {
            public void swipeDeleteCallback(long threadId) {
                BaseConversationListFragment.this.mIsAskBeforeDeleting = PreferenceUtils.isAskBeforeDeleting(BaseConversationListFragment.this.getContext());
                BaseConversationListFragment.this.mSwipeDeleteID = Long.valueOf(threadId);
                BaseConversationListFragment.confirmSwipeDeleteThread(threadId, BaseConversationListFragment.this.mQueryHandler);
            }

            public void swipeMarkAsRead(int runningMode) {
                MessageUtils.markAllAsRead(BaseConversationListFragment.this.getContext(), runningMode, new Runnable() {
                    public void run() {
                        Conversation.sethasUnreadMsg(false);
                        BaseConversationListFragment.this.clearNotification();
                    }
                });
            }

            public boolean isUsefulMode() {
                if (BaseConversationListFragment.this.mRunningMode == 3 || BaseConversationListFragment.this.mListView.isInEditMode()) {
                    return false;
                }
                return true;
            }

            public void markAllAsRead() {
                BaseConversationListFragment.this.markAllAsRead();
            }
        });
        this.mListListener = new ListViewListener();
        this.mListView.setListViewListener(this.mListListener);
        this.mListView.setSelectionChangeLisenter(this.mListListener);
        this.mListView.setOnKeyListener(this.mListListener);
        if (getActivity() instanceof ConversationList) {
            if (isRunningInConversationList()) {
                this.mSearchClickListener = new OnClickListener() {
                    public void onClick(View v) {
                        if (BaseConversationListFragment.this.mListAdapter.hasOpenSwipe()) {
                            BaseConversationListFragment.this.mListAdapter.closeOpenSwipe();
                        }
                        BaseConversationListFragment.this.clickToSearchMode();
                    }
                };
                addFakeHeadView();
            } else {
                this.mSearchHeaderView.setVisibility(8);
            }
        }
        setListViewScrollListener();
        this.mListView.setOnItemClickListener(this);
        this.mListView.setOnItemLongClickListener(this.mConversationLongClickListener);
        this.mListView.setListViewListener(this.mListListener);
        if (this.mCachedCursor != null) {
            onDataReady(this.mCachedCursor);
            this.mCachedCursor = null;
        }
        initListViewMode(savedInstanceState);
        setListViewTouchListener();
    }

    private void initListViewMode(Bundle savedInstanceState) {
        if (this.mRunningMode == 2) {
            this.mListView.enterEditMode(1);
            checktUserLongClickParams();
        } else if (this.mRunningMode == 6) {
            this.mListView.enterEditMode(2);
        } else if (this.mRunningMode == 3) {
            enterSearchMode();
            this.mListView.setClickable(false);
            this.mListView.setEnabled(false);
            if (this.mSearchWrapper != null && savedInstanceState != null) {
                String queryText = savedInstanceState.getString("search_string");
                if (!TextUtils.isEmpty(queryText)) {
                    this.mSearchWrapper.setQueryText(queryText);
                }
            }
        } else if (savedInstanceState != null && savedInstanceState.getInt("last_view_mode", 0) != 0) {
            this.mListView.exitEditMode();
        }
    }

    private void setListViewScrollListener() {
        this.mListView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                BaseConversationListFragment.this.mIsBeingScroll = false;
                switch (scrollState) {
                    case 0:
                    case 1:
                        if (scrollState == 1) {
                            BaseConversationListFragment.this.mListAdapter.closeOpenSwipe();
                        }
                        boolean isPrevStateScrolling = BaseConversationListFragment.this.mListAdapter.isScroll();
                        BaseConversationListFragment.this.mListAdapter.setScroll(false);
                        if (isPrevStateScrolling) {
                            BaseConversationListFragment.this.mListAdapter.notifyDataSetChanged();
                            break;
                        }
                        break;
                    case 2:
                        BaseConversationListFragment.this.mListAdapter.setScroll(true);
                        break;
                }
                SmartSmsPublicinfoUtil.setScrollStatu(scrollState);
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                BaseConversationListFragment.this.mIsBeingScroll = true;
            }
        });
    }

    private void setListViewTouchListener() {
        this.mListView.setHandleTouchListener(new HandleTouchListener() {
            private int mCountOfTouchPoints;
            private long mDownTime;
            private float mDownY;
            private boolean mHasActionDown = false;
            private boolean mIsMultiPoint;
            private float mLastY;
            private float mUpY;

            public void handleTouchEvent(View view, MotionEvent ev) {
                boolean z = false;
                BaseConversationListFragment baseConversationListFragment;
                switch (ev.getAction()) {
                    case 0:
                        if (BaseConversationListFragment.this.mSearchWrapper != null) {
                            BaseConversationListFragment.this.mSearchWrapper.setSearchClickListener(null);
                        }
                        BaseConversationListFragment.this.mFirstMotionY = ev.getRawY();
                        BaseConversationListFragment.this.mLastMoveY = BaseConversationListFragment.this.mFirstMotionY;
                        this.mDownY = BaseConversationListFragment.this.mFirstMotionY;
                        this.mLastY = BaseConversationListFragment.this.mLastMoveY;
                        this.mDownTime = ev.getDownTime();
                        this.mHasActionDown = true;
                        baseConversationListFragment = BaseConversationListFragment.this;
                        if (!BaseConversationListFragment.this.mListView.canScrollVertically(-1)) {
                            z = true;
                        }
                        baseConversationListFragment.mIsOnTop = z;
                        return;
                    case 1:
                    case 3:
                        this.mHasActionDown = false;
                        processActionUp(ev);
                        return;
                    case 2:
                        if (BaseConversationListFragment.this.mRunningMode != 3 && BaseConversationListFragment.this.mRunningMode != 4 && BaseConversationListFragment.this.mRunningMode != 5) {
                            if (!this.mHasActionDown) {
                                this.mLastY = BaseConversationListFragment.this.mListView.getDownPosition();
                                this.mHasActionDown = true;
                            }
                            float y = ev.getRawY();
                            BaseConversationListFragment.this.isMoveDown = y > this.mLastY;
                            baseConversationListFragment = BaseConversationListFragment.this;
                            if (y < this.mLastY) {
                                z = true;
                            }
                            baseConversationListFragment.isMoveUp = z;
                            BaseConversationListFragment.this.mMoveDistance = y - this.mLastY;
                            this.mLastY = y;
                            if (BaseConversationListFragment.this.isMoveDown) {
                                if (BaseConversationListFragment.this.mMoveDistance > ContentUtil.FONT_SIZE_NORMAL) {
                                    BaseConversationListFragment.this.lastUpDownState = 2;
                                }
                                BaseConversationListFragment.this.showSearchHeaderView(BaseConversationListFragment.this.mMoveDistance);
                            }
                            if (BaseConversationListFragment.this.isMoveUp) {
                                if (BaseConversationListFragment.this.mMoveDistance < -1.0f) {
                                    BaseConversationListFragment.this.lastUpDownState = 1;
                                }
                                BaseConversationListFragment.this.hideSearchHeaderView(BaseConversationListFragment.this.mMoveDistance);
                            }
                            this.mCountOfTouchPoints = ev.getPointerCount();
                            if (this.mCountOfTouchPoints > 1) {
                                this.mIsMultiPoint = true;
                                return;
                            }
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }

            private void processActionUp(MotionEvent ev) {
                if (BaseConversationListFragment.this.mSearchWrapper != null) {
                    BaseConversationListFragment.this.mSearchWrapper.setSearchClickListener(BaseConversationListFragment.this.mSearchClickListener);
                }
                if (3 == BaseConversationListFragment.this.getRunningMode()) {
                    if (BaseConversationListFragment.this.mSearchWrapper != null) {
                        BaseConversationListFragment.this.mSearchWrapper.hideSoftInputAndClearFocus();
                    }
                    BaseConversationListFragment.this.backFromSearch();
                }
                if (BaseConversationListFragment.this.isRunningInConversationList()) {
                    this.mUpY = ev.getRawY();
                    BaseConversationListFragment.this.mSpeed = (this.mUpY - this.mDownY) / ((float) (ev.getEventTime() - this.mDownTime));
                    if (BaseConversationListFragment.this.mSearchHeaderView != null) {
                        float currentY = BaseConversationListFragment.this.mSearchHeaderView.getTranslationY();
                        boolean isfakeViewShown = false;
                        if (BaseConversationListFragment.this.mFakeHeadView != null) {
                            isfakeViewShown = BaseConversationListFragment.this.mFakeHeadView.isShown();
                        }
                        boolean isCanScrollACTION_UP = BaseConversationListFragment.this.mListView.canScrollVertically(-1);
                        if ((!isCanScrollACTION_UP || this.mIsMultiPoint) && currentY > 0.0f) {
                            BaseConversationListFragment.this.translateViewInVariTime(BaseConversationListFragment.this.mSearchHeaderView, 0.0f, true);
                            BaseConversationListFragment.this.lastUpDownState = 0;
                            this.mCountOfTouchPoints = 0;
                            this.mIsMultiPoint = false;
                            return;
                        }
                        if (BaseConversationListFragment.this.lastUpDownState == 1) {
                            if (currentY > (-BaseConversationListFragment.this.mSearchViewHight)) {
                                if (isfakeViewShown) {
                                    if (!BaseConversationListFragment.this.mIsBeingScroll) {
                                        BaseConversationListFragment.this.translateViewInVariTime(BaseConversationListFragment.this.mSearchHeaderView, 0.0f, false);
                                    } else if (Math.abs(BaseConversationListFragment.this.mSpeed) <= 0.5f || !isCanScrollACTION_UP || BaseConversationListFragment.this.isShowAllListViewItem()) {
                                        BaseConversationListFragment.this.translateViewInVariTime(BaseConversationListFragment.this.mSearchHeaderView, 0.0f, false);
                                    } else {
                                        BaseConversationListFragment.this.translateViewVariTime(BaseConversationListFragment.this.mSearchHeaderView, -BaseConversationListFragment.this.mSearchViewHight);
                                    }
                                } else if (BaseConversationListFragment.this.mIsBeingScroll) {
                                    BaseConversationListFragment.this.translateViewVariTime(BaseConversationListFragment.this.mSearchHeaderView, -BaseConversationListFragment.this.mSearchViewHight);
                                } else {
                                    BaseConversationListFragment.this.translateViewInVariTime(BaseConversationListFragment.this.mSearchHeaderView, -BaseConversationListFragment.this.mSearchViewHight, false);
                                }
                            } else if (currentY <= (-BaseConversationListFragment.this.mSearchViewHight) && isfakeViewShown) {
                                BaseConversationListFragment.this.translateViewInVariTime(BaseConversationListFragment.this.mSearchHeaderView, 0.0f, false);
                            }
                        }
                        if (BaseConversationListFragment.this.lastUpDownState == 2 && currentY < 0.0f && currentY >= (-BaseConversationListFragment.this.mSearchViewHight)) {
                            if (BaseConversationListFragment.this.mIsBeingScroll) {
                                BaseConversationListFragment.this.translateViewVariTime(BaseConversationListFragment.this.mSearchHeaderView, 0.0f);
                            } else if (currentY > (-BaseConversationListFragment.this.mSearchViewHight)) {
                                BaseConversationListFragment.this.translateViewInVariTime(BaseConversationListFragment.this.mSearchHeaderView, 0.0f, false);
                            }
                        }
                        BaseConversationListFragment.this.lastUpDownState = 0;
                        this.mCountOfTouchPoints = 0;
                        this.mIsMultiPoint = false;
                    }
                }
            }
        });
    }

    private boolean isRunningInConversationList() {
        return ((getActivity() instanceof ConversationEditor) || (getActivity() instanceof NotificationList)) ? false : true;
    }

    protected void translateViewVariTime(View view, float destination) {
        if (view != null && this.mSpeed != 0.0f) {
            long duration = (long) Math.abs(Math.abs(destination - view.getTranslationY()) / this.mSpeed);
            if (duration > 500) {
                duration = 500;
            }
            ObjectAnimator transAnimator = ObjectAnimator.ofFloat(view, "translationY", new float[]{currentPosition, destination});
            transAnimator.setDuration(duration);
            transAnimator.start();
        }
    }

    protected void translateViewInVariTime(View view, float destination, boolean needDecelerate) {
        if (view != null) {
            ObjectAnimator transAnimator = ObjectAnimator.ofFloat(view, "translationY", new float[]{view.getTranslationY(), destination});
            if (needDecelerate) {
                transAnimator.setDuration(300);
                transAnimator.setInterpolator(new DecelerateInterpolator());
            } else {
                transAnimator.setDuration(150);
            }
            transAnimator.start();
        }
    }

    private void setSearchHeaderViewPosition() {
        if (this.mSearchHeaderView != null && this.mIsFirst) {
            this.mSearchHeaderView.getLocationOnScreen(this.mSearchHeaderViewLocation);
            if (this.mSearchHeaderViewLocation[1] != 0) {
                this.mHeadBasePositionY = this.mSearchHeaderViewLocation[1];
                this.mIsFirst = false;
            } else {
                this.mIsFirst = true;
            }
        }
    }

    protected void showSearchHeaderView(float distance) {
        if (this.mSearchHeaderView != null) {
            float currentY = this.mSearchHeaderView.getTranslationY();
            float destinationY = currentY + distance;
            setSearchHeaderViewPosition();
            if (this.mHeadBasePositionY != 0 && this.mFakeHeadView != null && destinationY > 0.0f) {
                if (currentY < 0.0f) {
                    this.mSearchHeaderView.setTranslationY(0.0f);
                }
            } else if (destinationY >= this.mSearchViewHight) {
                this.mSearchHeaderView.setTranslationY(0.0f);
            } else {
                this.mSearchHeaderView.setTranslationY(destinationY);
            }
        }
    }

    protected void backFromSearch() {
        finishSelf(true);
    }

    protected void hideSearchHeaderView(float distance) {
        if (this.mSearchHeaderView != null) {
            float currentY = this.mSearchHeaderView.getTranslationY();
            float destinationY = currentY + distance;
            setSearchHeaderViewPosition();
            boolean isMove = false;
            if (!(this.mHeadBasePositionY == 0 || this.mFakeHeadView == null)) {
                boolean mIsFakeHeadViewShow = this.mFakeHeadView.isShown();
                this.mFakeHeadView.getLocationOnScreen(this.mHeadlocation);
                isMove = this.mHeadlocation[1] < this.mHeadBasePositionY;
                if (currentY > 0.0f) {
                    if (mIsFakeHeadViewShow) {
                        this.mSearchHeaderView.setTranslationY((float) (this.mHeadlocation[1] - this.mHeadBasePositionY));
                    }
                    return;
                }
            }
            if (destinationY < (-this.mSearchViewHight)) {
                if (currentY > (-this.mSearchViewHight) && isMove) {
                    this.mSearchHeaderView.setTranslationY(-this.mSearchViewHight);
                }
                return;
            }
            if (isMove) {
                this.mSearchHeaderView.setTranslationY(destinationY);
            }
        }
    }

    private void showViewAfterExitEdit() {
        if (this.mListView != null && this.mSearchHeaderView != null) {
            int firstVisiblePosition = this.mListView.getFirstVisiblePosition();
            float currentY = this.mSearchHeaderView.getTranslationY();
            if ((firstVisiblePosition == 0 || !isItemsFillListView()) && currentY < 0.0f) {
                translateViewInVariTime(this.mSearchHeaderView, 0.0f, false);
            }
        }
    }

    private boolean isItemsFillListView() {
        if (this.mListAdapter == null || this.mListAdapter.getCount() == 0 || this.mListView == null) {
            return false;
        }
        int listCount = this.mListAdapter.getCount();
        int headerViewsCount = this.mListView.getHeaderViewsCount();
        View listItem = this.mListAdapter.getView(0, null, this.mListView);
        listItem.measure(0, 0);
        if (((double) (((int) getResources().getDimension(R.dimen.search_header_height)) * headerViewsCount)) + (((double) listCount) * ((double) listItem.getMeasuredHeight())) >= ((double) this.mListView.getHeight())) {
            return true;
        }
        return false;
    }

    private void checktUserLongClickParams() {
        if (getActivity() != null && getIntent() != null) {
            Intent intent = getIntent();
            long selectId = intent.getLongExtra("select_positoin", 0);
            if (2 == intent.getIntExtra("running_mode", -1) && 0 != selectId) {
                this.mSavedFirstVisiblePosition = intent.getIntExtra("last_list_pos", -1);
                this.mSavedFirstItemOffset = intent.getIntExtra("last_list_offset", 0);
                if (!(-10000000012L == selectId || -10000000011L == selectId)) {
                    this.mListView.setSeleceted(selectId, true);
                }
            }
        }
    }

    private void hideSmsPromoBanner() {
        if (this.mSmsPromoBannerView != null) {
            this.mSmsPromoBannerView.setVisibility(8);
            this.mIsFirst = true;
        }
    }

    private void showSmsPromoBanner() {
        if (this.mSmsPromoBannerView != null) {
            this.mSmsPromoBannerView.setVisibility(0);
            this.mIsFirst = true;
        }
    }

    private View initSmsPromoBanner(Activity activity, View parenteView) {
        if (activity == null || parenteView == null) {
            return null;
        }
        ViewStub bannerStub = (ViewStub) parenteView.findViewById(R.id.stub_banner_sms_promo);
        View smsPromoBannerView = null;
        if (bannerStub != null) {
            smsPromoBannerView = bannerStub.inflate();
        }
        if (smsPromoBannerView == null) {
            return null;
        }
        smsPromoBannerView.setOnClickListener(this.mDefaultSmsViewClickListener);
        this.mIsFirst = true;
        TextView setButton = (TextView) smsPromoBannerView.findViewById(R.id.set_as_default_sms);
        if (setButton != null) {
            setButton.setOnClickListener(this.mDefaultSmsViewClickListener);
        }
        return smsPromoBannerView;
    }

    private synchronized void runOneTimeStorageLimitCheckForLegacyMessages() {
        if (Recycler.isAutoDeleteEnabled(getContext())) {
            markCheckedMessageLimit();
        } else {
            HwBackgroundLoader.getInst().postTaskDelayed(new Runnable() {
                public void run() {
                    final Activity activity = BaseConversationListFragment.this.getActivity();
                    if (activity != null && MmsConfig.isEnableAutoDelete()) {
                        if (Recycler.checkForThreadsOverLimit(activity)) {
                            BaseConversationListFragment.this.getUiHandler().postDelayed(new Runnable() {
                                public void run() {
                                    if (BaseConversationListFragment.this.getActivity() != null) {
                                        BaseConversationListFragment.this.startActivity(new Intent(activity, WarnOfStorageLimitsActivity.class));
                                    }
                                }
                            }, 2000);
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Editor editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
                                    editor.putBoolean("pref_key_auto_delete", false);
                                    editor.apply();
                                }
                            });
                        }
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                BaseConversationListFragment.this.markCheckedMessageLimit();
                            }
                        });
                    }
                }
            }, 2000);
        }
    }

    private void markCheckedMessageLimit() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean("checked_message_limits", true);
        editor.apply();
    }

    public void onStart() {
        super.onStart();
        MessagingNotification.cancelNotification(getContext(), 239);
        if (this.mListAdapter != null) {
            this.mListAdapter.setOnContentChangedListener(this.mContentChangedListener);
        }
        checkAsyncQuery();
        if (!isInLandscape()) {
            this.mMenuEx.onPrepareOptionsMenu(this.mMenuEx.getMenu());
        }
        HwBackgroundLoader.getInst().postTaskDelayed(new Runnable() {
            public void run() {
                if (BaseConversationListFragment.this.mIsSmsEnabled && PreferenceUtils.isSmsRecoveryEnable(BaseConversationListFragment.this.getContext())) {
                    ProviderCallUtils.deleteExpireTrashMsgs(BaseConversationListFragment.this.getContext());
                }
            }
        }, 5000);
    }

    public void onStop() {
        super.onStop();
        this.isDataLoad = false;
        this.mListAdapter.setOnContentChangedListener(null);
        DraftCache.removeOnDraftChangedListener(this);
        this.mDoOnceAfterFirstQuery = true;
        ConversationListItem.clearConvListItemCache();
        if (this.mHwCust != null) {
            this.mHwCust.onStop();
        }
        ResEx.self().clearContactDrawableCache();
        if (this.mListAdapter.hasOpenSwipe()) {
            this.mListAdapter.closeOpenSwipe();
        }
        if (this.mSwipeShowconfirmDeleteThreadDialog != null && this.mSwipeShowconfirmDeleteThreadDialog.isShowing()) {
            this.mSwipeShowconfirmDeleteThreadDialog.dismiss();
        }
        if (this.mSwipeShowconfirmDeleteLockThreadDialog != null && this.mSwipeShowconfirmDeleteLockThreadDialog.isShowing()) {
            this.mSwipeShowconfirmDeleteLockThreadDialog.dismiss();
        }
    }

    public void onDraftChanged(final long threadId, final boolean hasDraft) {
        if (this.mListAdapter != null) {
            this.mQueryHandler.post(new Runnable() {
                public void run() {
                    MLog.d("Mms_UI_CLFrag", "onDraftChanged: threadId=" + threadId + ", hasDraft=" + hasDraft);
                    BaseConversationListFragment.this.mListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public void onDraftChanged(final Collection<Long> mThreadIds, boolean hasDraft) {
        if (this.mListAdapter != null) {
            this.mQueryHandler.post(new Runnable() {
                public void run() {
                    MLog.d("Mms_UI_CLFrag", "onDraftChanged: =" + mThreadIds.size());
                    BaseConversationListFragment.this.mListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void checkAsyncQuery() {
        if (!this.isDataLoad) {
            startAsyncQuery();
        }
    }

    private void startAsyncQuery() {
        if (SmartArchiveSettingUtils.isSmartArchiveEnabled(MmsApp.getApplication())) {
            ConversationListItem.setSupportHwMsg(true);
            if (this.mRunningMode == 4) {
                startAsyncQuery(2);
            } else if (this.mRunningMode == 5) {
                startAsyncQuery(1);
            } else {
                startAsyncQuery(255);
            }
            return;
        }
        startAsyncQuery(-1);
        ConversationListItem.setSupportHwMsg(false);
    }

    private void startAsyncQuery(int numberType) {
        try {
            Conversation.setContextForQuery(getActivity());
            this.isDataLoad = true;
            MLog.v("Mms_UI_CLFrag", "BaseConversationListFragment startAsyncQuery[THREAD_LIST_QUERY_TOKEN]");
            if (RcsConversationUtils.getHwCustUtils() == null || !RcsConversationUtils.getHwCustUtils().isRcsSwitchOn()) {
                Conversation.startQueryForAll(this.mQueryHandler, 1701, numberType);
            } else {
                RcsConversationUtils.getHwCustUtils().startQueryByThreadSettings(this.mQueryHandler, 1701, getActivity(), numberType);
            }
        } catch (SQLiteException e) {
            ErrorMonitor.reportErrorInfo(8, "BaseConversationListFragment startAsyncQuery got exception", e);
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (isAdded()) {
            this.mMenuEx.onPrepareOptionsMenu(menu);
        }
    }

    private void setHasUnreadMsg(Cursor cursor) {
        if (cursor == null) {
            Conversation.startQueryHaveUnreadMessages(this.mQueryHandler, AMapException.CODE_AMAP_CLIENT_NETWORK_EXCEPTION, this.mRunningMode);
            return;
        }
        int count = cursor.getCount();
        int maxStep = count > 100 ? 25 : count;
        int idx = 0;
        if (cursor.moveToFirst()) {
            while (cursor.getInt(11) <= 0) {
                idx++;
                if (idx <= maxStep) {
                    if (!cursor.moveToNext()) {
                    }
                }
            }
            Conversation.sethasUnreadMsg(true);
            MLog.d("Mms_UI_CLFrag", "has unread messages!");
            return;
        }
        if (idx == count) {
            Conversation.sethasUnreadMsg(false);
            this.mMenuEx.setItemEnabled(278927465, false);
        } else {
            Conversation.startQueryHaveUnreadMessages(this.mQueryHandler, AMapException.CODE_AMAP_CLIENT_NETWORK_EXCEPTION, this.mRunningMode);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        this.mMenuEx.onCustomMenuItemClick(item);
        return super.onOptionsItemSelected(item);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (this.mListAdapter.hasOpenSwipe()) {
            this.mListAdapter.closeOpenSwipe();
            return;
        }
        Cursor cursor = null;
        try {
            cursor = (Cursor) this.mListView.getItemAtPosition(position);
        } catch (IndexOutOfBoundsException e) {
            MLog.e("Mms_UI_CLFrag", "wrong position " + e);
        }
        if (cursor != null) {
            Conversation conv = Conversation.from(getActivity(), cursor);
            ConversationListItem item = null;
            View itemView = view.findViewById(R.id.mms_animation_list_item_view);
            if (itemView instanceof ConversationListItem) {
                item = (ConversationListItem) itemView;
            }
            if (item == null) {
                MLog.e("Mms_UI_CLFrag", "item is null");
                return;
            }
            Activity activity = getActivity();
            if (activity == null) {
                MLog.e("Mms_UI_CLFrag", "activity is null");
                return;
            }
            MLog.d("Mms_UI_CLFrag", "onListItemClick: pos=" + position + ", " + item.isNotifactionsItem() + ", " + item.isHwNotifactionsItem() + ", tid=" + conv.getThreadId());
            if (item.isNotifactionsItem()) {
                Conversation.sethasUnreadMsg(conv.getUnreadMessageCount() > 0);
                gotoNotificationList(getActivity(), 4);
                setActivityAnimation(activity);
            } else if (item.isHwNotifactionsItem()) {
                Conversation.sethasUnreadMsg(conv.getUnreadMessageCount() > 0);
                gotoNotificationList(getActivity(), 5);
                setActivityAnimation(activity);
            } else if (!isNeedToInteruptOpenConv(conv)) {
                setSelected(conv);
                preLoadAvatarCache(conv);
                if (this.mHwCust == null || !this.mHwCust.openRcsThreadId(conv, false)) {
                    gotoComposeMessage(conv, false, false);
                }
            }
        }
    }

    private void preLoadAvatarCache(Conversation conv) {
        ContactList contacts = conv.getRecipients();
        if (contacts.size() == 1) {
            Contact c = (Contact) contacts.get(0);
            if (c.isMe() || c.existsInDatabase()) {
                AvatarCache.preLoadData(getActivity().getApplicationContext(), c.isMe(), c.getPersonId(), c);
            } else if (c.isYpContact() && !TextUtils.isEmpty(c.getYpPhotoUri())) {
                AvatarCache.preLoadData(getActivity().getApplicationContext(), c.getYpContactId(), c.getYpPhotoUri(), c);
            }
        }
    }

    private void createNewMessage() {
        gotoComposeMessage(null, false, false);
    }

    protected void gotoComposeMessage(Conversation conv, final boolean isHwNotification, final boolean isPreview) {
        if (conv == null || conv.getMessageCount() != -1) {
            long j;
            if (conv == null) {
                StatisticalHelper.incrementReportCount(getContext(), 2035);
            } else {
                StatisticalHelper.incrementReportCount(getContext(), 2037);
                if (conv.hasUnreadMessages()) {
                    StatisticalHelper.incrementReportCount(getContext(), 2033);
                }
            }
            long tId = conv == null ? 0 : conv.getThreadId();
            Context activity = getActivity();
            if (tId < 0) {
                j = 0;
            } else {
                j = tId;
            }
            final Intent intent = ComposeMessageActivity.createIntent(activity, j);
            if (tId <= 0) {
                startNewMessageActivity(intent);
                return;
            }
            String snippet = "";
            Cursor cursor = this.mListAdapter.getCursor();
            if (cursor != null) {
                snippet = Conversation.getSnippetFromCursor(getContext(), cursor);
            }
            if (!SmartArchiveSettingUtils.isSmartArchiveEnabled(getContext()) || (!(getRunningMode() == 5 || isHwNotification) || TextUtils.isEmpty(snippet))) {
                if (isPreview) {
                    switchToPreviewActivity(intent);
                } else {
                    startActivityOverrideAnimation(intent);
                }
                return;
            }
            String senderName = conv.getFromNumberForHw(snippet);
            if (TextUtils.isEmpty(senderName)) {
                intent.putExtra("sender_for_huawei_message", "");
                if (isPreview) {
                    switchToPreviewActivity(intent);
                    return;
                } else {
                    startActivityOverrideAnimation(intent);
                    return;
                }
            }
            intent.putExtra("sender_for_huawei_message", senderName);
            Intent target = intent;
            new AsyncTask<String, Void, NameMatchResult>() {
                protected NameMatchResult doInBackground(String... names) {
                    Context context = BaseConversationListFragment.this.getContext();
                    if (context == null) {
                        return null;
                    }
                    return Contact.getNameMatchedContact(context, names[0]);
                }

                protected void onPostExecute(NameMatchResult result) {
                    if (result == null) {
                        MLog.e("Mms_UI_CLFrag", "Can't get Name match result");
                        intent.putExtra("contact_id", 0);
                    } else {
                        intent.putExtra("contact_id", result.contactId);
                        intent.putExtra("name", result.contactName);
                        if (isHwNotification) {
                            intent.putExtra("sender_for_huawei_message", result.contactName);
                        }
                    }
                    if (isPreview) {
                        BaseConversationListFragment.this.switchToPreviewActivity(intent);
                    } else {
                        BaseConversationListFragment.this.startActivityOverrideAnimation(intent);
                    }
                }
            }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new String[]{senderName});
            return;
        }
        MLog.w("Mms_UI_CLFrag", "Receive a message but UI not updated to last state.");
        startAsyncQuery();
    }

    private void switchToPreviewActivity(Intent intent) {
        intent.putExtra("android.intent.action.START_PEEK_ACTIVITY", "startPeekActivity");
        getActivity().startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    protected void startActivityOverrideAnimation(Intent intent) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
            return;
        }
        MLog.e("Mms_UI_CLFrag", "startActivityOverideAnimation failed::activity is null!!");
    }

    private void gotoEdit(long selectPos) {
        boolean z = false;
        this.mSavedFirstVisiblePosition = this.mListView.getFirstVisiblePosition();
        View firstChild = this.mListView.getChildAt(0);
        this.mSavedFirstItemOffset = firstChild == null ? 0 : firstChild.getTop();
        boolean isNofication = isRunningInNotifactionList();
        if (!MmsConfig.isCspVersion() || isNofication) {
            if (!this.mListView.isInEditMode()) {
                this.mListView.enterEditMode(1);
            }
            if (isNofication || !(-10000000012L == selectPos || -10000000011L == selectPos)) {
                boolean isChecked = this.mListView.isSelected(selectPos);
                EmuiListView_V3 emuiListView_V3 = this.mListView;
                if (!isChecked) {
                    z = true;
                }
                emuiListView_V3.setSeleceted(selectPos, z);
            }
            this.mListAdapter.notifyDataSetChanged();
            MLog.d("Mms_UI_CLFrag", "set user select as check " + selectPos + " offset " + this.mSavedFirstVisiblePosition + " - " + this.mSavedFirstItemOffset);
            if (!(isNofication || this.mSearchWrapper == null || this.mSearchHeaderView == null)) {
                this.mSearchWrapper.setSearchStyle(2);
            }
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("running_mode", 2);
        bundle.putLong("select_positoin", selectPos);
        bundle.putInt("last_list_pos", this.mSavedFirstVisiblePosition);
        bundle.putInt("last_list_offset", this.mSavedFirstItemOffset);
        MLog.d("Mms_UI_CLFrag", "goto edit " + this.mSavedFirstVisiblePosition + " - " + this.mSavedFirstItemOffset);
        HwBaseActivity.startMmsActivity(getActivity(), ConversationEditor.class, bundle, false);
    }

    private void gotoDeleteMode() {
        boolean isNofication = isRunningInNotifactionList();
        if (!MmsConfig.isCspVersion() || isNofication) {
            this.mListView.enterEditMode(2);
            if (!(isNofication || this.mSearchWrapper == null)) {
                this.mSearchWrapper.setSearchStyle(2);
            }
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("running_mode", 6);
        HwBaseActivity.startMmsActivity(getActivity(), ConversationEditor.class, bundle, false);
    }

    public static void gotoNotificationList(Context context, int mode) {
        gotoNotificationList(context, mode, false);
    }

    public static void gotoNotificationList(Context context, int mode, boolean fromNotification) {
        StatisticalHelper.incrementReportCount(context, 2006);
        Bundle bundle = new Bundle();
        bundle.putInt("running_mode", mode);
        if (fromNotification) {
            bundle.putBoolean("fromNotification", true);
        }
        MLog.d("Mms_UI_CLFrag", "gotoNotificationList , and fromNotification is " + fromNotification);
        HwBaseActivity.startMmsActivity(context, NotificationList.class, bundle);
    }

    public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor = this.mListAdapter.getCursor();
        if (cursor == null || cursor.getPosition() > this.mListView.getCount() || cursor.getPosition() < 0) {
            MLog.e("Mms_UI_CLFrag", "proc data but cursor or cursor position has error " + cursor);
            return true;
        }
        Conversation conv = Conversation.from(getActivity(), cursor);
        long threadId = conv.getThreadId();
        switch (item.getItemId()) {
            case 278925315:
                if (this.mHwCust != null) {
                    threadId = this.mHwCust.getNewThreadId(conv, threadId);
                }
                confirmDeleteThread(threadId, this.mQueryHandler);
                break;
            case 278927470:
                gotoComposeMessage(conv, false, false);
                break;
            case 278927472:
                Intent intent = new Intent("android.intent.action.VIEW", ((Contact) conv.getRecipients().get(0)).getUri());
                intent.setFlags(524288);
                startActivity(intent);
                break;
            case 278927473:
                startActivity(HwMessageUtils.createAddContactIntent(((Contact) conv.getRecipients().get(0)).getNumber()));
                break;
        }
        return super.onContextItemSelected(item);
    }

    public static void confirmSwipeDeleteThread(long threadId, AsyncQueryHandler handler) {
        Conversation.startQueryHaveLockedMessages(handler, threadId, 2007);
    }

    public static void confirmDeleteThread(long threadId, AsyncQueryHandler handler) {
        ArrayList<Long> threadIds = new ArrayList();
        if (threadId != 0) {
            threadIds.add(Long.valueOf(threadId));
        }
        confirmDeleteThreads(threadIds, handler);
    }

    public static void confirmDeleteThreads(Collection<Long> threadIds, AsyncQueryHandler handler) {
        ArrayList<Long> commonThreadIds = new ArrayList();
        ArrayList<Long> notificationIds = new ArrayList();
        for (Long id : threadIds) {
            if (id.longValue() == -10000000011L || id.longValue() == -10000000012L) {
                notificationIds.add(id);
            } else {
                commonThreadIds.add(id);
            }
        }
        if (notificationIds.size() == 0) {
            Conversation.startQueryHaveLockedMessages(handler, (Collection) threadIds, (int) AMapException.CODE_AMAP_CLIENT_SOCKET_TIMEOUT_EXCEPTION);
        } else {
            Conversation.confirmDeleteThreads(handler, commonThreadIds, notificationIds, 1805);
        }
    }

    public static void confirmDeleteThreadDialog(DeleteThreadListener listener, Collection<Long> threadIds, boolean hasLockedMessages, boolean isSelectAll, Context context) {
        confirmDeleteThreadDialog(listener, threadIds, hasLockedMessages, false, isSelectAll, context);
    }

    public static void confirmDeleteThreadDialog(final DeleteThreadListener listener, Collection<Long> threadIds, boolean hasLockedMessages, boolean hasNotifications, boolean isSelectAll, Context context) {
        String message;
        View contents = View.inflate(context, R.layout.delete_thread_dialog_view, null);
        if (isSelectAll || threadIds == null) {
            message = context.getResources().getString(R.string.confirm_delete_all_conversations_2);
        } else {
            NumberFormat.getIntegerInstance().setGroupingUsed(false);
            int cnt = threadIds.size();
            if (hasNotifications) {
                message = context.getResources().getQuantityString(R.plurals.mms_delete_noti_waring_msgs, cnt, new Object[]{format.format((long) cnt)});
            } else {
                message = context.getResources().getQuantityString(R.plurals.confirm_delete_conversation, cnt, new Object[]{format.format((long) cnt)});
            }
        }
        synchronized (mShowDialogLock) {
            if (mShowconfirmDeleteThreadDialog == null) {
                mShowconfirmDeleteThreadDialog = new Builder(context).setView(contents).setIconAttribute(16843605).setCancelable(true).setPositiveButton(R.string.delete, listener).setNegativeButton(R.string.no, null).create();
                ((TextView) contents.findViewById(R.id.tv_deleted_message)).setText(message);
            }
        }
        if (!hasLockedMessages && !hasNotifications) {
            listener.setDeleteLockedMessage(true);
        } else if (hasLockedMessages) {
            synchronized (mShowDialogLock) {
                if (mShowconfirmDeleteThreadDialog != null) {
                    mShowconfirmDeleteThreadDialog.setView(contents);
                }
            }
            final CheckBox checkbox = (CheckBox) contents.findViewById(R.id.delete_locked);
            checkbox.setVisibility(0);
            listener.setDeleteLockedMessage(checkbox.isChecked());
            checkbox.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    listener.setDeleteLockedMessage(checkbox.isChecked());
                }
            });
        }
        synchronized (mShowDialogLock) {
            if (mShowconfirmDeleteThreadDialog != null) {
                mShowconfirmDeleteThreadDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialogInterface) {
                        synchronized (BaseConversationListFragment.mShowDialogLock) {
                            BaseConversationListFragment.mShowconfirmDeleteThreadDialog = null;
                        }
                    }
                });
                if (!mShowconfirmDeleteThreadDialog.isShowing()) {
                    mShowconfirmDeleteThreadDialog.show();
                }
                MessageUtils.setButtonTextColor(mShowconfirmDeleteThreadDialog, -1, context.getResources().getColor(R.drawable.text_color_red));
            }
        }
    }

    public void confirmSwipeDeleteThreadDialog(final DeleteThreadListener listener, Collection<Long> collection, boolean hasLockedMessages, boolean hasNotifications, boolean isSelectAll, Context context) {
        View contents = View.inflate(context, R.layout.delete_thread_dialog_view, null);
        String lockMessage = context.getResources().getString(R.string.deleting_lock_message);
        if (hasLockedMessages) {
            this.mSwipeShowconfirmDeleteLockThreadDialog = new Builder(context).setView(contents).setIconAttribute(16843605).setCancelable(true).setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    listener.setDeleteLockedMessage(true);
                    BaseConversationListFragment.this.mListAdapter.deleteConversationWhenClickSwipeDeleteButton(true, listener);
                }
            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    listener.setDeleteLockedMessage(false);
                    BaseConversationListFragment.this.mListAdapter.deleteConversationWhenClickSwipeDeleteButton(true, listener);
                }
            }).create();
            ((TextView) contents.findViewById(R.id.tv_deleted_message)).setText(lockMessage);
        } else {
            listener.setDeleteLockedMessage(true);
        }
        View content = View.inflate(context, R.layout.delete_thread_dialog_view, null);
        String message = context.getResources().getString(R.string.deleting_message);
        final CheckBox checkbox = (CheckBox) content.findViewById(R.id.delete_locked);
        checkbox.setText(context.getResources().getString(R.string.not_ask_before_deleting));
        checkbox.setVisibility(0);
        final Context context2 = context;
        final boolean z = hasLockedMessages;
        final DeleteThreadListener deleteThreadListener = listener;
        this.mSwipeShowconfirmDeleteThreadDialog = new Builder(context).setView(content).setIconAttribute(16843605).setCancelable(true).setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                boolean z;
                if (checkbox.isChecked()) {
                    z = false;
                } else {
                    z = true;
                }
                PreferenceUtils.setIsAskBeforeDeleting(z, context2);
                if (!z || BaseConversationListFragment.this.mSwipeShowconfirmDeleteLockThreadDialog.isShowing()) {
                    BaseConversationListFragment.this.mListAdapter.deleteConversationWhenClickSwipeDeleteButton(true, deleteThreadListener);
                    return;
                }
                BaseConversationListFragment.this.mSwipeShowconfirmDeleteLockThreadDialog.show();
                MessageUtils.setButtonTextColor(BaseConversationListFragment.this.mSwipeShowconfirmDeleteLockThreadDialog, -1, context2.getResources().getColor(R.drawable.text_color_red));
            }
        }).setNegativeButton(R.string.no, null).show();
        this.mSwipeShowconfirmDeleteThreadDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                synchronized (BaseConversationListFragment.mShowDialogLock) {
                    BaseConversationListFragment.this.mSwipeShowconfirmDeleteThreadDialog = null;
                }
            }
        });
        ((TextView) content.findViewById(R.id.tv_deleted_message)).setText(message);
        MessageUtils.setButtonTextColor(this.mSwipeShowconfirmDeleteThreadDialog, -1, context.getResources().getColor(R.drawable.text_color_red));
    }

    public void confirmSwipeDeleteThread(final DeleteThreadListener listener, Collection<Long> collection, boolean hasLockedMessages, boolean hasNotifications, boolean isSelectAll, Context context) {
        if (hasLockedMessages) {
            View contents = View.inflate(context, R.layout.delete_thread_dialog_view, null);
            String lockMessage = context.getResources().getString(R.string.deleting_lock_message);
            TextView textView = (TextView) contents.findViewById(R.id.tv_deleted_message);
            this.mSwipeShowconfirmDeleteLockThreadDialog = new Builder(context).setView(contents).setIconAttribute(16843605).setCancelable(true).setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    listener.setDeleteLockedMessage(true);
                    BaseConversationListFragment.this.mListAdapter.deleteConversationWhenClickSwipeDeleteButton(true, listener);
                }
            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    listener.setDeleteLockedMessage(false);
                    BaseConversationListFragment.this.mListAdapter.deleteConversationWhenClickSwipeDeleteButton(true, listener);
                }
            }).show();
            textView.setText(lockMessage);
            MessageUtils.setButtonTextColor(this.mSwipeShowconfirmDeleteLockThreadDialog, -1, context.getResources().getColor(R.drawable.text_color_red));
            return;
        }
        listener.setDeleteLockedMessage(false);
        this.mListAdapter.deleteConversationWhenClickSwipeDeleteButton(true, listener);
    }

    private void startQueryUnreadMessageCount(AsyncQueryHandler handler, int token, int runningMode) {
        if (!OsUtil.IS_EMUI_LITE) {
            Conversation.startQueryUnreadMessageCount(handler, token, runningMode);
        }
    }

    private void onDataReady(Cursor cursor) {
        CursorUtils.clearCusorRowIdIdx(cursor);
        if (this.mListAdapter == null || this.mListView == null) {
            this.mCachedCursor = cursor;
        } else if (this.mListView.getAdapter() == null) {
            MLog.i("Mms_UI_CLFrag", "onDataReady mListView getAdapter null");
        } else {
            Activity activity = getActivity();
            if (activity != null && !activity.isFinishing()) {
                boolean hasMessages;
                this.mListView.setBlockLayoutList(true);
                this.mListAdapter.changeCursor(cursor);
                this.mListView.setBlockLayoutList(false);
                initUnEditableItemCount();
                if (this.mDataLoadingStatus == 0 && this.mLoadingView != null) {
                    this.mLoadingView.setVisibility(8);
                }
                this.mDataLoadingStatus = 1;
                if (cursor.getCount() > 0) {
                    hasMessages = true;
                } else {
                    hasMessages = false;
                }
                if (hasMessages) {
                    boolean isInEdit;
                    showNoMessageView(false);
                    if (this.mListView.isInEditMode() || 2 == getRunningMode()) {
                        isInEdit = true;
                    } else {
                        isInEdit = false;
                    }
                    if (!(isInEdit || this.mSearchHeaderView == null)) {
                        showViewAfterExitEdit();
                        hideViewInNoConversation(false);
                    }
                    this.mListView.setVisibility(0);
                    this.mListView.onDataReload();
                } else if (isRunningInNotifactionList()) {
                    Handler handler = HwBackgroundLoader.getUIHandler();
                    handler.removeCallbacks(this.mFinishActivityRunnable);
                    handler.postDelayed(this.mFinishActivityRunnable, (long) FINISH_ACTIVITY_DELAY);
                } else {
                    showNoMessageView(true);
                    hideViewInNoConversation(true);
                }
                setHasUnreadMsg(cursor);
                if (this.mDoOnceAfterFirstQuery) {
                    DraftCache.addOnDraftChangedListener(this);
                }
                if (this.mDoOnceAfterFirstQuery) {
                    this.mDoOnceAfterFirstQuery = false;
                    if (hasMessages && CspFragment.isFragmentActived()) {
                        if (!(this.mRunningMode == 1 || this.mRunningMode == 0)) {
                            if (this.mRunningMode == -1) {
                            }
                        }
                        Conversation.markAllConversationsAsSeen(getActivity());
                    }
                }
                if (this.mObsoluteUndeleted) {
                    this.mObsoluteUndeleted = false;
                    getUiHandler().post(this.mDeleteObsoleteThreadsRunnable);
                }
                MLog.d("Mms_UI_CLFrag", "set position edit " + this.mSavedFirstVisiblePosition + " - " + this.mSavedFirstItemOffset);
                if (this.mSavedFirstVisiblePosition != -1) {
                    this.mListView.setSelectionFromTop(this.mSavedFirstVisiblePosition, this.mSavedFirstItemOffset);
                    this.mSavedFirstVisiblePosition = -1;
                    if (this.mSavedFirstItemOffset == 0 && !isRunningInNotifactionList()) {
                        showSearchHeaderView(this.mSearchViewHight);
                    }
                }
                openConversationWhenSplit(cursor);
                invalidateOptionsMenu();
                if (!this.mIsFromPause) {
                    checkAndshowFunctionTips();
                }
            }
        }
    }

    public boolean handleKeyEvent(int keyCode) {
        return this.mMenuEx.onKeyEvent(keyCode);
    }

    public void onDestroy() {
        super.onDestroy();
        unRegiestBroadcast();
        unRegiesterPriavcyMonitor();
        unRegisterDefSmsAppChanged();
        unregisterContactDataChangeObserver();
        unregisterForContactChange();
        if (this.mHwCust != null) {
            this.mHwCust.unRegisterOtherListenerOnDestroy();
        }
        if (this.mLocalBroadcastManager != null) {
            this.mLocalBroadcastManager.unregisterReceiver(this.mReceiver);
        }
        if (this.mListView != null && this.mListView.isInEditMode()) {
            this.mListView.exitEditMode();
        }
        if (this.mListAdapter != null) {
            this.mListAdapter.setOnContentChangedListener(null);
            this.mListAdapter.changeCursor(null);
        }
        DraftCache.removeOnDraftChangedListener(this);
        dissmissDeleteThreadDialog();
        if (this.mMenuEx != null) {
            this.mMenuEx.clear();
            this.mMenuEx.dismissPopup();
        }
        HwCloudBackUpManager.getInstance().dismissHwCloudServiceGuideAlert();
        RcsGroupCache.getInstance().clear();
    }

    private void unRegiestBroadcast() {
        if (this.mAddressChangeReceiver != null) {
            getContext().unregisterReceiver(this.mAddressChangeReceiver);
            this.mAddressChangeReceiver = null;
        }
    }

    public void onRotationChanged(int oldOritation, int newOritation) {
        invalidateOptionsMenu();
        if (this.mSearchWrapper != null) {
            this.mSearchWrapper.onRotationChanged(oldOritation, newOritation);
        }
    }

    int getRunningMode() {
        if (getActivity() == null) {
            return 2;
        }
        Intent intent = getIntent();
        if (intent == null) {
            MLog.d("Mms_UI_CLFrag", "ConversationList getRunningMode can't get intent");
            return 2;
        }
        MLog.d("Mms_UI_CLFrag", "ConversationList start by " + intent.getAction());
        int runningMode = intent.getIntExtra("running_mode", -1);
        if (!MmsConfig.isCspVersion() || !getActivity().getClass().equals(ConversationEditor.class)) {
            return runningMode;
        }
        if (runningMode == 6) {
            return 6;
        }
        return runningMode == 3 ? 3 : 2;
    }

    private boolean isRunningInNotifactionList() {
        return this.mRunningMode == 5 || this.mRunningMode == 4;
    }

    private void regiesterPriavcyMonitor() {
        if (MmsConfig.isSupportPrivacy()) {
            this.localPrivacyModitor = new ModeChangeListener() {
                public void onModeChange(Context context, boolean isInPrivacy) {
                    if (BaseConversationListFragment.this.mCachedCursor != null) {
                        BaseConversationListFragment.this.mCachedCursor.close();
                        BaseConversationListFragment.this.mCachedCursor = null;
                    }
                    BaseConversationListFragment.this.isDataLoad = false;
                }
            };
            PrivacyStateListener.self().register(this.localPrivacyModitor);
        }
    }

    private void unRegiesterPriavcyMonitor() {
        if (MmsConfig.isSupportPrivacy()) {
            PrivacyStateListener.self().unRegister(this.localPrivacyModitor);
        }
    }

    private void checkSmsEnable() {
        updatePromoBar();
        boolean isSmsEnabled = MmsConfig.isSmsEnabled(getActivity());
        if (!isSmsEnabled || !this.mIsSmsEnabled) {
            this.mIsSmsEnabled = isSmsEnabled;
        }
    }

    private void updatePromoBar() {
        if (OsUtil.IS_EMUI_LITE) {
            new AsyncTask<Void, Void, Boolean>() {
                protected Boolean doInBackground(Void... params) {
                    return Boolean.valueOf(MmsConfig.isSmsDefaultApp(BaseConversationListFragment.this.getContext()));
                }

                protected void onPostExecute(Boolean result) {
                    if (result != null) {
                        BaseConversationListFragment.this.updatePromoBar(result.booleanValue());
                    }
                }
            }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
        } else {
            updatePromoBar(MmsConfig.isSmsDefaultApp(getContext()));
        }
    }

    private void updatePromoBar(boolean isDefaultPackage) {
        if (!(this.mIsSmsDefaultPackage && isDefaultPackage) && (this.mIsSmsDefaultPackage || isDefaultPackage)) {
            this.mIsSmsDefaultPackage = isDefaultPackage;
            boolean isSmsDisabledForUser;
            if (OsUtil.isSecondaryUser()) {
                isSmsDisabledForUser = OsUtil.isSmsDisabledForUser(getActivity(), UserHandle.myUserId());
            } else {
                isSmsDisabledForUser = false;
            }
            if (this.mIsSmsDefaultPackage || MmsConfig.isSmsPromoDismissed(getActivity()) || r0) {
                hideSmsPromoBanner();
            } else {
                if (this.mSmsPromoBannerView == null) {
                    this.mSmsPromoBannerView = initSmsPromoBanner(getActivity(), getView());
                }
                showSmsPromoBanner();
            }
        }
    }

    public void registerDefSmsAppChanged() {
        if (this.mDefSmsAppChangedReceiver == null) {
            this.mDefSmsAppChangedReceiver = new DefaultSmsAppChangedReceiver(new HwDefSmsAppChangedListener() {
                public void onDefSmsAppChanged() {
                    BaseConversationListFragment.this.checkSmsEnable();
                }
            });
        }
        getContext().getApplicationContext().registerReceiver(this.mDefSmsAppChangedReceiver, new IntentFilter("com.huawei.mms.default_smsapp_changed"), permission.DEFAULTCHANGED_PERMISSION, null);
    }

    public void unRegisterDefSmsAppChanged() {
        if (this.mDefSmsAppChangedReceiver != null) {
            getContext().getApplicationContext().unregisterReceiver(this.mDefSmsAppChangedReceiver);
        }
    }

    protected void addFakeHeadView() {
        int i = 2;
        Activity activity = getActivity();
        if (this.mRunningMode != 3 && this.mRunningMode != 4 && this.mRunningMode != 5) {
            if (activity == null) {
                MLog.w("Mms_UI_CLFrag", "add ListView Search Header, activity was null and return !");
                return;
            }
            this.mFakeHeadView = activity.getLayoutInflater().inflate(R.layout.fake_head_view, null);
            this.mListView.addHeaderView(this.mFakeHeadView, null, true);
            this.mFakeHeadView.getLocationInWindow(this.mHeadlocation);
            int headHeight = (int) getResources().getDimension(R.dimen.search_header_height);
            this.mListView.setHeadHeight(headHeight);
            this.mListView.setSelectionFromTop(1, headHeight);
            this.mSearchWrapper = new SearchViewWrapper(getActivity());
            if (this.mSearchMsgUtils == null) {
                this.mSearchMsgUtils = new SearchMsgUtils(getActivity());
            }
            boolean grayStyle = 2 == this.mRunningMode || 6 == this.mRunningMode;
            SearchViewWrapper searchViewWrapper = this.mSearchWrapper;
            View view = this.mSearchHeaderView;
            if (!grayStyle) {
                i = 1;
            }
            searchViewWrapper.init(view, i);
            if (this.mSearchClickListener != null) {
                this.mSearchWrapper.setSearchClickListener(this.mSearchClickListener);
            }
            this.mListView.setHeaderDividersEnabled(false);
        }
    }

    protected void addListViewSearchHeader() {
        int i = 2;
        Activity activity = getActivity();
        if (this.mRunningMode != 3 && this.mRunningMode != 4 && this.mRunningMode != 5) {
            if (activity == null) {
                MLog.w("Mms_UI_CLFrag", "add ListView Search Header, activity was null and return !");
                return;
            }
            View listHeader = activity.getLayoutInflater().inflate(R.layout.mms_search_view_with_devider, null);
            this.mListView.addHeaderView(listHeader, null, true);
            this.mListView.setSelection(1);
            this.mListView.setHeadHeight((int) getResources().getDimension(R.dimen.search_header_height));
            this.mSearchWrapper = new SearchViewWrapper(getActivity());
            if (this.mSearchMsgUtils == null) {
                this.mSearchMsgUtils = new SearchMsgUtils(getActivity());
            }
            boolean grayStyle = 2 == this.mRunningMode || 6 == this.mRunningMode;
            SearchViewWrapper searchViewWrapper = this.mSearchWrapper;
            if (!grayStyle) {
                i = 1;
            }
            searchViewWrapper.init(listHeader, i);
            this.mSearchWrapper.setSearchClickListener(new OnClickListener() {
                public void onClick(View v) {
                    BaseConversationListFragment.this.clickToSearchMode();
                }
            });
            this.mListView.setHeaderDividersEnabled(false);
        }
    }

    private void initLocalBroadcast() {
        final Activity activity = getActivity();
        if (activity != null && (activity.getClass().equals(ConversationEditor.class) || activity.getClass().equals(NotificationList.class))) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.huawei.android.conv.incomingmsg");
            this.mReceiver = new BroadcastReceiver() {
                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void onReceive(Context context, Intent intent) {
                    if (intent != null && BaseConversationListFragment.this.isAdded() && ((BaseConversationListFragment.this.mListView.isInEditMode() || BaseConversationListFragment.this.mRunningMode == 2 || BaseConversationListFragment.this.mRunningMode == 6) && "com.huawei.android.conv.incomingmsg".equals(intent.getAction()))) {
                        int type = intent.getIntExtra("START_TYPE", 0);
                        long newMsgThreadId = intent.getLongExtra("thread_id", 0);
                        if (newMsgThreadId > 0) {
                            String address = intent.getStringExtra("address");
                            if (1 == type) {
                                if (activity.getClass().equals(NotificationList.class)) {
                                    BaseConversationListFragment.this.mListView.setSeleceted(newMsgThreadId, false);
                                    BaseConversationListFragment.this.mListAdapter.notifyDataSetChanged();
                                } else {
                                    Conversation conv = Cache.get(newMsgThreadId);
                                    if (conv != null || address != null) {
                                        int numberType;
                                        BaseConversationListFragment baseConversationListFragment = BaseConversationListFragment.this;
                                        if (conv != null) {
                                            numberType = conv.getNumberType();
                                        } else {
                                            numberType = BaseConversationListFragment.this.getNumberType(address);
                                        }
                                        baseConversationListFragment.setListItemUnSelected(newMsgThreadId, numberType);
                                    }
                                }
                            }
                        }
                    }
                }
            };
            this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(activity);
            this.mLocalBroadcastManager.registerReceiver(this.mReceiver, filter);
        }
    }

    private void setListItemUnSelected(long threadId, int numberType) {
        MLog.d("Mms_UI_CLFrag", "numberType: " + numberType);
        switch (numberType) {
            case 0:
                this.mListView.setSeleceted(threadId, false);
                break;
            case 1:
            case 2:
                break;
            default:
                return;
        }
        this.mListAdapter.notifyDataSetChanged();
    }

    private String revert(String recipient) {
        if (recipient.startsWith(StringUtils.MPLUG86)) {
            return recipient.substring(3);
        }
        return recipient;
    }

    private int getNumberType(String recipient) {
        if (TextUtils.isEmpty(recipient)) {
            return 0;
        }
        if (this.mHwCustBaseConversationListFragment != null && this.mHwCustBaseConversationListFragment.isServiceMessageEnabled()) {
            return this.mHwCustBaseConversationListFragment.getNumberTypeForServiceMessageArchival(recipient, 0);
        }
        recipient = revert(recipient);
        for (String str : "1065796709,1065502043,1065902090,106575550211,10690133830,1069055999,106550200271,1065902002801,106906060012,106900679901,106900679914,106900679916,106903345801,106903345814,106903345816,106903345820,106903345901,106903345914".split(",")) {
            if (recipient.startsWith(str)) {
                return 1;
            }
        }
        for (String str2 : new String[]{"106", "100", "118", "116", "400", "95", "9", "12306", "111", "12329", "10198", "12580", "12345"}) {
            if (recipient.startsWith(str2)) {
                return 2;
            }
        }
        return 0;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.mHwCust != null) {
            this.mHwCust.onActivityResult(requestCode, resultCode, data);
        }
        if (7 != requestCode) {
            return;
        }
        if (HwMessageUtils.isSplitOn()) {
            getActivity().onBackPressed();
        } else {
            getActivity().finish();
        }
    }

    public void clearNotification() {
        super.clearNotification();
        if (!CspFragment.getNotificationCleared() && CspFragment.isFragmentActived()) {
            Conversation.markAllConversationsAsSeen(getActivity());
        }
    }

    private void markAllAsRead() {
        Activity activity = getActivity();
        if (activity == null) {
            MLog.e("Mms_UI_CLFrag", "in markAllAsRead(), activity == null");
        } else {
            MessageUtils.markAllAsRead(activity, this.mRunningMode, new Runnable() {
                public void run() {
                    Conversation.sethasUnreadMsg(false);
                    BaseConversationListFragment.this.clearNotification();
                    MLog.d("Mms_UI_CLFrag", "mark all as read after run");
                }
            });
        }
    }

    public static void dissmissDeleteThreadDialog() {
        synchronized (mShowDialogLock) {
            if (mShowconfirmDeleteThreadDialog != null) {
                mShowconfirmDeleteThreadDialog.setOnDismissListener(null);
                if (mShowconfirmDeleteThreadDialog.isShowing()) {
                    mShowconfirmDeleteThreadDialog.dismiss();
                }
                mShowconfirmDeleteThreadDialog = null;
            }
        }
    }

    private void showSmsRecycleEnableDialog(final Collection<Long> threadIds, final AsyncQueryHandler handler) {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            View contents = View.inflate(getContext(), R.layout.not_show_again_dialog_view, null);
            final CheckBox checkbox = (CheckBox) contents.findViewById(R.id.not_show_again);
            checkbox.setChecked(true);
            new Builder(getContext()).setIcon(17301543).setTitle(R.string.sms_recovery_title).setCancelable(true).setMessage(R.string.enable_sms_recovery_notify_message).setPositiveButton(R.string.enable_sms_recovery_notify_open, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseConversationListFragment.this.getContext()).edit();
                    editor.putBoolean("pref_sms_recycle_not_show_again", checkbox.isChecked());
                    editor.putBoolean("pref_key_recovery_support", true);
                    editor.apply();
                    final Collection collection = threadIds;
                    final AsyncQueryHandler asyncQueryHandler = handler;
                    new CallRequest(BaseConversationListFragment.this.getContext(), "method_enable_recovery") {
                        protected void setParam() {
                            this.mRequest.putBoolean("recovery_status", true);
                        }

                        protected void onCallBack() {
                            BaseConversationListFragment.confirmDeleteThreads(collection, asyncQueryHandler);
                        }
                    }.makeCall();
                }
            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    BaseConversationListFragment.this.resetThreadIdAfterDelete();
                    dialog.dismiss();
                    Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseConversationListFragment.this.getContext()).edit();
                    editor.putBoolean("pref_sms_recycle_not_show_again", checkbox.isChecked());
                    editor.apply();
                    BaseConversationListFragment.confirmDeleteThreads(threadIds, handler);
                }
            }).setView(contents).show();
        }
    }

    private void switchToSearchView() {
        this.mListAdapter.setOnContentChangedListener(null);
        this.mListView.setAdapter(null);
        this.mListView.setVisibility(8);
        if (this.mImageMask != null && this.mImageMask.getVisibility() == 0) {
            MLog.i("Mms_UI_CLFrag", "showNoMessageView mImageMask GONE");
            this.mImageMask.setVisibility(8);
        }
        if (this.mSearchListView == null) {
            ViewStub vs = (ViewStub) getView().findViewById(R.id.search_view);
            if (vs != null) {
                this.mSearchListView = (ListView) vs.inflate().findViewById(R.id.list_search);
                this.mSearchListView.setFastScrollEnabled(true);
                this.mSearchLoader = new SearchDataLoader(getContext(), this);
                this.mSearchListAdapter = new SearchCursorAdapter(getContext(), null, false);
                this.mSearchListView.setClickable(true);
                this.mSearchListView.setFilterTouchesWhenObscured(false);
                this.mSearchListView.setDescendantFocusability(393216);
                this.mSearchListView.setOnScrollListener(new OnScrollListener() {
                    public void onScrollStateChanged(AbsListView v, int arg1) {
                        if (BaseConversationListFragment.this.mSearchWrapper != null) {
                            BaseConversationListFragment.this.mSearchWrapper.hideSoftInputAndClearFocus();
                        }
                    }

                    public void onScroll(AbsListView v, int arg1, int arg2, int arg3) {
                    }
                });
            } else {
                return;
            }
        }
        this.mSearchListView.setAdapter(this.mSearchListAdapter);
        Contact.addListener(this.mContactListener);
        registerForContactChange();
    }

    private void switchToConversationView() {
        this.mSearchListView.setVisibility(8);
        this.mSearchListView.setAdapter(null);
        showNoMatchMessageView(false);
        Contact.removeListener(this.mContactListener);
        unregisterForContactChange();
        if (this.mImageMask != null && this.mImageMask.getVisibility() == 8) {
            MLog.i("Mms_UI_CLFrag", "switchToConversationView mImageMask VISIBLE");
            this.mImageMask.setVisibility(0);
        }
        this.mListAdapter.setOnContentChangedListener(this.mContentChangedListener);
        this.mListView.setAdapter(this.mListAdapter);
        this.mListView.setVisibility(0);
    }

    private void startSearchAsynQuery() {
        if (this.mSearchLoader.hasUnfinishedJob() || TextUtils.isEmpty(this.mWaitForSearchText)) {
            MLog.i("Mms_UI_CLFrag", "startSearchAsynQuery return");
            return;
        }
        StatisticalHelper.incrementReportCount(getActivity(), 2005);
        if (this.mHwCust != null) {
            this.mHwCust.clearRcsGroupSubject();
        }
        this.mSearchText = this.mWaitForSearchText;
        this.mWaitForSearchText = "";
        this.mSearchLoader.asyncSearch(this.mSearchText);
    }

    public void onLoadComplete(int token, Cursor c) {
        switch (token) {
            case 10001:
                MLog.i("Mms_UI_CLFrag", " onLoadComplete QUERY_TOKEN_SEARCH");
                onSearchDataReady(c);
                return;
            default:
                return;
        }
    }

    private void onSearchDataReady(Cursor c) {
        if (this.mSearchListView.getAdapter() == null) {
            MLog.i("Mms_UI_CLFrag", "onSearchDataReady mSearchListView getAdapter null");
            return;
        }
        if (!this.mSearchLoader.hasUnfinishedJob()) {
            this.mSearchMsgUtils.setSearchText(this.mSearchText);
        }
        if (!(TextUtils.isEmpty(this.mWaitForSearchText) || this.mSearchLoader.hasUnfinishedJob())) {
            startSearchAsynQuery();
        }
        this.mSearchListAdapter.changeCursor(c);
        int cursorCount = c == null ? 0 : c.getCount();
        MLog.i("Mms_UI_CLFrag", "onSearchDataReady cursorCount=" + cursorCount);
        if (cursorCount != 0) {
            this.mSearchListView.setVisibility(0);
            showNoMatchMessageView(false);
            if (cursorCount > 0) {
                SearchRecentSuggestions recent = MmsApp.getApplication().getRecentSuggestions();
                if (recent != null) {
                    recent.saveRecentQuery(this.mSearchText, getResources().getQuantityString(R.plurals.search_history, cursorCount, new Object[]{Integer.valueOf(cursorCount), this.mSearchText}));
                }
            }
        } else if (this.mSearchLoader.hasUnfinishedJob()) {
            MLog.i("Mms_UI_CLFrag", " hasUnfinishedJob");
        } else {
            showNoMatchMessageView(true);
            this.mSearchListView.setVisibility(8);
        }
    }

    private void registerForContactChange() {
        getContext().getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, this.mContactDbListenr);
    }

    private void unregisterForContactChange() {
        getContext().getContentResolver().unregisterContentObserver(this.mContactDbListenr);
    }

    protected void setFragmentMenu(BaseFragmentMenu fm) {
        this.mMenuEx = fm;
    }

    public void addToEmptyNameThreadList(long threadId) {
        if (!this.mHasReparedEmptyNameThreads.contains(Long.valueOf(threadId))) {
            if (!this.mPendingEmptyNameThreads.contains(Long.valueOf(threadId))) {
                MLog.d("Mms_UI_CLFrag", "thread " + threadId + " has a empty name");
                this.mPendingEmptyNameThreads.add(Long.valueOf(threadId));
            }
            if (!this.sRepairing && !this.mPendingEmptyNameThreads.isEmpty()) {
                this.mReparingEmptyNameThreads.addAll(this.mPendingEmptyNameThreads);
                this.mPendingEmptyNameThreads.clear();
                startRepairing();
            }
        }
    }

    private void startRepairing() {
        if (!this.sRepairing && this.mReparingEmptyNameThreads.size() > 0) {
            this.sRepairing = true;
            this.mHasReparedEmptyNameThreads.addAll(this.mReparingEmptyNameThreads);
            StringBuilder selection = new StringBuilder("thread_id in ( ");
            selection.append(this.mReparingEmptyNameThreads.get(0));
            for (int i = 1; i < this.mReparingEmptyNameThreads.size(); i++) {
                selection.append(",").append(this.mReparingEmptyNameThreads.get(i));
            }
            selection.append(")");
            MLog.d("Mms_UI_CLFrag", "startRepairing for " + selection);
            Radar.reportChr(0, 1312, "search address for " + selection);
            this.mQueryHandler.startQuery(2005, null, sAllSms, ALL_SMS_ADDRESS_PROJECTION, selection.toString(), null, null);
        }
    }

    private void fixConversations(Map<String, String> repairAddress) {
        if (repairAddress != null && repairAddress.size() > 0) {
            ContentValues initialValues = new ContentValues();
            for (Entry<String, String> key : repairAddress.entrySet()) {
                initialValues.put((String) key.getKey(), (String) key.getValue());
            }
            Radar.reportChr(0, 1312, repairAddress.size() + " threads is fixed.");
            getContext().getContentResolver().insert(sAllCanonical, initialValues);
        }
    }

    private void checkPendingEmptynameThreads() {
        List<Long> repairedlist = new ArrayList();
        for (int i = 0; i < this.mPendingEmptyNameThreads.size(); i++) {
            if (this.mHasReparedEmptyNameThreads.contains(this.mPendingEmptyNameThreads.get(i))) {
                MLog.d("Mms_UI_CLFrag", "thread " + this.mPendingEmptyNameThreads.get(i) + " has been repaired, remove it !");
                repairedlist.add((Long) this.mPendingEmptyNameThreads.get(i));
            }
        }
        if (repairedlist.size() > 0) {
            this.mPendingEmptyNameThreads.removeAll(repairedlist);
        }
    }

    protected void setThreadIdToShowAfterDelete(List<Long> list, boolean isAllSelected) {
    }

    protected void openConversationWhenSplit(Cursor cursor) {
    }

    protected boolean isNeedToInteruptOpenConv(Conversation conv) {
        return false;
    }

    protected void setSelected(Conversation conv) {
    }

    protected void setActivityAnimation(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
    }

    protected void startNewMessageActivity(Intent intent) {
        startActivity(intent);
    }

    protected void resetThreadIdAfterDelete() {
    }

    public void setIsAfterDelete(boolean isAfterDelete) {
    }

    protected void backToConversationList(Activity activity) {
        if (HwMessageUtils.isSplitOn()) {
            if (activity instanceof NotificationList) {
                Intent intent = new Intent(activity, ConversationList.class);
                intent.setAction("android.intent.action.MAIN");
                startActivity(intent);
                ((ConversationList) activity).overridePendingTransition(0, 0);
            }
            activity.finish();
            return;
        }
        activity.onBackPressed();
    }

    public boolean isInEditMode() {
        return this.mListView.isInEditMode();
    }

    private void registerContactDataChangeObserver() {
        getActivity().getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, this.mContactChangeListener);
    }

    private void unregisterContactDataChangeObserver() {
        getActivity().getContentResolver().unregisterContentObserver(this.mContactChangeListener);
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (this.mNoConvView != null) {
            this.mNoConvView.setInMultiWindowMode(isInMultiWindowMode);
        }
    }

    private boolean isShowAllListViewItem() {
        boolean z = true;
        if (this.mListView == null) {
            return true;
        }
        if (this.mListView.getLastVisiblePosition() - this.mListView.getFirstVisiblePosition() != this.mListView.getCount() - 1) {
            z = false;
        }
        return z;
    }

    public EmuiListView_V3 getListView() {
        return this.mListView;
    }

    public ConversationListAdapter getConversationListAdapter() {
        return this.mListAdapter;
    }

    private void resetActionBarAfterExitEdit() {
        Activity act = getActivity();
        if (act == null) {
            MLog.e("Mms_UI_CLFrag", "Can't get Activity");
            return;
        }
        this.mActionBar.setListTitle(act.getResources().getString(R.string.app_label), this.mUnreadCount);
        boolean isInNotifactionList = isRunningInNotifactionList();
        boolean isSplitOn = HwMessageUtils.isSplitOn();
        if (isInNotifactionList && isSplitOn) {
            this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
                public void onClick(View v) {
                    BaseConversationListFragment.this.backToConversationList(BaseConversationListFragment.this.getActivity());
                }
            });
        }
    }
}
