package com.android.contacts.calllog;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.KeyguardManager;
import android.app.ListFragment;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.ServiceManager;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.VoicemailContract.Status;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.text.Html;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.android.common.io.MoreCloseables;
import com.android.contacts.CallUtil;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GeoUtil;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.activities.ActionBarAdapter.TabState;
import com.android.contacts.activities.ContactSelectionActivity;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.activities.PeopleActivity.TabSelectedListener;
import com.android.contacts.activities.RequestPermissionsActivityBase;
import com.android.contacts.calllog.CallLogAdapter.CallFetcher;
import com.android.contacts.calllog.CallLogAdapter.CallLogAdapterListener;
import com.android.contacts.calllog.CallLogQueryHandler.Listener;
import com.android.contacts.calllog.ClearCallLogDialog.clickListener;
import com.android.contacts.compatibility.NumberLocationLoader;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.dialog.CallSubjectDialog;
import com.android.contacts.dialpad.DialpadFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.blacklist.BlacklistCommonUtils;
import com.android.contacts.hap.calllog.CallLogMultiSelectionActivity;
import com.android.contacts.hap.calllog.CallLogMultiSelectionActivity.TranslucentActivity;
import com.android.contacts.hap.numbermark.NumberMarkUtil;
import com.android.contacts.hap.rcs.detail.RcsCallLogDetailHelper;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimStateListener;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.util.AlertDialogFragmet;
import com.android.contacts.hap.util.AlertDialogFragmet.OnDialogOptionSelectListener;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.hap.util.SingleHandModeManager;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.EmptyLoader.Callback;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.widget.SuspentionScroller;
import com.android.dialer.calllog.VoicemailDeleteDialog;
import com.android.dialer.util.OnVoicemailMenuDeletedListener;
import com.android.dialer.voicemail.ActivationProgressDialog;
import com.android.dialer.voicemail.ActiveVVM4CMView;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.android.dialer.voicemail.VoicemailStatusHelper;
import com.android.dialer.voicemail.VoicemailStatusHelper.StatusMessage;
import com.android.dialer.voicemail.VoicemailStatusHelperImpl;
import com.google.android.gms.Manifest.permission;
import com.google.android.gms.R;
import com.google.android.gms.location.LocationRequest;
import com.google.common.annotations.VisibleForTesting;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService.Stub;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CallLogFragment extends ListFragment implements Listener, CallFetcher, SimStateListener, TabSelectedListener, clickListener {
    private static Cursor mSavedCallLogData;
    private static int originMode = 0;
    private boolean isCallLogForSingleContact;
    private OnClickListener mActiveListener = new OnClickListener() {
        public void onClick(View v) {
            CallLogFragment.this.showNotifacationDialog();
        }
    };
    private ActiveVVM4CMView mActiveView;
    public CallLogAdapter mAdapter;
    String mBlackListOnlyName = "";
    private View mCallLogEmptyLayout;
    private boolean mCallLogFetched;
    private View mCallLogList;
    private CallLogQueryHandler mCallLogQueryHandler;
    private int mCallTypeFilter = -1;
    private boolean mCanShowLazyMode = true;
    private Uri mContactCallLogUri;
    private ArrayList<String> mContactNumbersList = new ArrayList();
    private Context mContext;
    public int mCurrentCallTypeFilter = 0;
    public int mCurrentNetworkTypeFilter = 2;
    private ClearCallLogDialog mDialog;
    ArrayList<String> mEmergencynumbers = null;
    private boolean mEmptyLoaderRunning;
    private LinearLayout mEmptyTextParentView;
    private TextView mEmptyView;
    private ViewStub mEmptyViewStub;
    private float mFriction = 0.0075f;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 113:
                    CallLogFragment.this.mAdapter.notifyDataSetChanged();
                    break;
                case 112:
                    if (CallLogFragment.this.isAdded() && msg.obj != null) {
                        if (CallLogFragment.this.mHandler.hasMessages(113)) {
                            CallLogFragment.this.mHandler.removeMessages(113);
                        }
                        CallLogFragment.this.mHandler.sendEmptyMessageDelayed(113, 200);
                        break;
                    }
            }
        }
    };
    private boolean mIsContactsGetDirty;
    private boolean mIsIdle;
    private boolean mIsMultiSim;
    private KeyguardManager mKeyguardManager;
    public ListView mListView;
    private View mLoadingView;
    private boolean mMenuVisible = true;
    private NumberLocationLoader mNumberLocationLoader;
    private Calendar mPausingDate;
    private PhoneNumberHelper mPhoneNumberHelper;
    private CallPhoneStateListener mPhoneStateListener = new CallPhoneStateListener();
    private ClearCallLogDialog mProgressDialog;
    private boolean mRefreshDataRequired = true;
    private CallLogRefreshReceiver mRefreshReceiver = new CallLogRefreshReceiver();
    private LinearLayout mSearchGuide;
    private long mSelectCallLogId = -1;
    private SharedPreferences mSp;
    private TextView mStatusMessageAction;
    private TextView mStatusMessageText;
    private View mStatusMessageView;
    private SuspentionScroller mSuspentionScroller;
    AsyncTask<Void, Void, Void> mTask;
    private float mVelocityScale = 0.65f;
    private OnVoicemailMenuDeletedListener<CallLogListItemViews> mVmMenuListener;
    private boolean mVoicemailNotificationRefreshrequired = true;
    private VoicemailPlaybackPresenter mVoicemailPlaybackPresenter;
    private boolean mVoicemailStatusFetched;
    private VoicemailStatusHelper mVoicemailStatusHelper;
    private String mVoicemailUri;
    private ContentObserver voicemailStatusObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            HwLog.d("CallLogFragment", "voicemailStatusObserver, onchange");
            CallLogFragment.this.mVoicemailStatusFetched = false;
            if (QueryUtil.isSystemAppForContacts()) {
                CallLogFragment.this.startVoicemailStatusQuery();
            }
        }
    };
    private int voicemail_status_message_max_width;

    public class CallLogRefreshReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            CallLogFragment.this.mAdapter.clearPresetCache();
            CallLogFragment.this.mAdapter.invalidateCache();
            if (HwLog.HWDBG) {
                HwLog.d("CallLogFragment", "onreceive");
            }
            CallLogFragment.this.refreshData(true);
        }
    }

    public interface CallLogTouch {
        void touchLogCalls(MotionEvent motionEvent);
    }

    private final class CallPhoneStateListener extends PhoneStateListener {
        private boolean mbFirstCallStateChanged;

        private CallPhoneStateListener() {
            this.mbFirstCallStateChanged = false;
        }

        public void disableFirstCallStateChangedRequired() {
            this.mbFirstCallStateChanged = false;
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            if (!(!this.mbFirstCallStateChanged || state == 2 || CallLogFragment.this.mAdapter == null)) {
                CallLogFragment.this.mAdapter.notifyDataSetChanged();
            }
            this.mbFirstCallStateChanged = true;
        }
    }

    private static class DeleteThread extends AsyncTask<Void, Void, Void> {
        Context mApplContext = null;
        StringBuilder mCallIds = null;

        DeleteThread(Context appContext, StringBuilder callIds) {
            this.mApplContext = appContext;
            this.mCallIds = callIds;
        }

        protected Void doInBackground(Void... params) {
            if (CommonUtilMethods.getShowCallLogMergeStatus(this.mApplContext)) {
                this.mCallIds = CommonUtilMethods.getDeleteCallLogIds(this.mApplContext, this.mCallIds);
            }
            if (EmuiFeatureManager.isRcsFeatureEnable()) {
                RcsCallLogDetailHelper.deleteRcsMapAndPicture(this.mApplContext, this.mCallIds);
            }
            this.mApplContext.getContentResolver().delete(Calls.CONTENT_URI_WITH_VOICEMAIL, "_id IN (" + this.mCallIds + ")", null);
            return null;
        }
    }

    private class RemoveMissedCallNotification extends AsyncTask<Void, Void, Void> {
        private RemoveMissedCallNotification() {
        }

        protected Void doInBackground(Void... params) {
            ((TelecomManager) CallLogFragment.this.mContext.getSystemService("telecom")).cancelMissedCallsNotification();
            return null;
        }

        protected void onPostExecute(Void unused) {
        }
    }

    private static void saveCallLogData(Cursor data) {
        mSavedCallLogData = data;
    }

    public String getVoicemailUri() {
        return this.mVoicemailUri;
    }

    public void setVoicemailUri(String uri) {
        this.mVoicemailUri = uri;
    }

    public void setCanShowLazyMode(boolean canShowLazyMode) {
        this.mCanShowLazyMode = canShowLazyMode;
    }

    public boolean getCanShowLazyMode() {
        return this.mCanShowLazyMode;
    }

    public CallLogFragment(Uri lookupContactUri, boolean callLogForSingleContact) {
        this.mContactCallLogUri = lookupContactUri;
        this.isCallLogForSingleContact = callLogForSingleContact;
    }

    public void onCreate(Bundle state) {
        super.onCreate(state);
        PLog.d(0, "CallLogFragment onCreate");
        if (CommonUtilMethods.calcIfNeedSplitScreen() && state == null && (getActivity() instanceof PeopleActivity)) {
            state = CommonUtilMethods.getInstanceState();
        }
        if (state != null) {
            this.isCallLogForSingleContact = state.getBoolean("singleContactCallLog", false);
            if (this.isCallLogForSingleContact) {
                this.mContactCallLogUri = (Uri) state.getParcelable("contactCallLogUri");
            }
            this.mCurrentCallTypeFilter = state.getInt("currentFilter");
            this.mSelectCallLogId = state.getLong("split_calllog_id");
            this.mCurrentNetworkTypeFilter = state.getInt("currentTab");
            this.mVoicemailUri = state.getString("voicemail_uri");
        }
        this.mContext = getActivity();
        this.voicemail_status_message_max_width = getResources().getDimensionPixelSize(R.dimen.calllog_voicemail_status_message_max_width);
        this.mCallLogQueryHandler = new CallLogQueryHandler(this.mContext, getActivity().getContentResolver(), this);
        String currentCountryIso = GeoUtil.getCurrentCountryIso(this.mContext);
        this.mVoicemailPlaybackPresenter = VoicemailPlaybackPresenter.getInstance(getActivity(), state);
        this.mActiveView = new ActiveVVM4CMView(getActivity(), this);
        this.mAdapter = new CallLogAdapter(this.mContext, this, new ContactInfoHelper(this.mContext, currentCountryIso), this.mVoicemailPlaybackPresenter);
        this.mVmMenuListener = this.mAdapter;
        this.mKeyguardManager = (KeyguardManager) getActivity().getSystemService("keyguard");
        this.mPhoneNumberHelper = new PhoneNumberHelper(getResources());
        this.mIsMultiSim = SimFactoryManager.isDualSim();
        if (this.mContext instanceof PeopleActivity) {
            ((PeopleActivity) this.mContext).setTabSelectedListener(TabState.DIALER, this);
            onCurrentSelectedTab();
        }
        refreshData(true);
        FragmentManager fm = getFragmentManager();
        ClearCallLogDialog aDialog = (ClearCallLogDialog) fm.findFragmentByTag("ClearCallLogDialog");
        if (aDialog != null) {
            this.mDialog = aDialog;
            this.mDialog.setListener(this);
        }
        ClearCallLogDialog aProgressDialog = (ClearCallLogDialog) fm.findFragmentByTag("progressTag");
        if (aProgressDialog != null) {
            this.mProgressDialog = aProgressDialog;
        }
        setHasOptionsMenu(false);
        getActivity().getContentResolver().registerContentObserver(Status.CONTENT_URI, true, this.voicemailStatusObserver);
    }

    public void onCallsFetched(Cursor cursor) {
        if (getActivity() == null || getActivity().isFinishing()) {
            if (cursor != null) {
                cursor.close();
            }
            HwLog.w("CallLogFragment", "onCallsFetched getActivity finished.");
            return;
        }
        ((ContactsApplication) getActivity().getApplication()).setLaunchProgress(8);
        this.mAdapter.setLoading(false);
        if (cursor == null || cursor.getCount() != 0) {
            showEmptyView(false);
        } else {
            PLog.d(8, "Calllog list empty.");
            hideTabHeader();
            showEmptyView(true);
            if (HwLog.HWFLOW) {
                HwLog.d("CallLogFragment", "onCallsFetched cursor ==null or getCount ==0.");
            }
        }
        if (this.mActiveView != null) {
            this.mActiveView.onCallFetched(cursor);
        }
        this.mAdapter.setLoading(false);
        Cursor tempCursor = cursor;
        this.mAdapter.changeCursor(cursor);
        saveCallLogData(cursor);
        Activity act = getActivity();
        if (act != null) {
            act.invalidateOptionsMenu();
        }
        this.mCallLogFetched = true;
        destroyEmptyLoaderIfAllDataFetched();
        if (this.mLoadingView != null) {
            this.mLoadingView.setVisibility(8);
        }
        this.mCallLogList.setVisibility(0);
        HwLog.i("CallLogFragment", "onCallsFetched mLoadingView : " + this.mLoadingView + ", mCallLogList.visible");
        refreshListViewDelayed();
    }

    private void refreshListViewDelayed() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                CallLogFragment.this.mAdapter.notifyDataSetChanged();
            }
        }, 50);
    }

    private void hideTabHeader() {
        Fragment dialpadFragment = getGoalFragment("tab-pager-dialer");
        if ((dialpadFragment instanceof DialpadFragment) && this.mListView != null && this.mListView.getCount() > 0) {
            ((DialpadFragment) dialpadFragment).hideListViewSelectedHeader(false);
        }
    }

    private void showEmptyView(boolean isShow) {
        if (this.mSearchGuide != null) {
            this.mSearchGuide.setVisibility(8);
        }
        boolean showGuide = false;
        if (EmuiFeatureManager.isChinaArea()) {
            showGuide = this.mSp.getBoolean("reference_is_show_guide", true);
        }
        Fragment dialpadFragment = getGoalFragment("tab-pager-dialer");
        if (dialpadFragment instanceof DialpadFragment) {
            ((DialpadFragment) dialpadFragment).showEmptyView(isShow);
        }
        if (!isShow) {
            this.mSearchGuide = null;
            if (showGuide) {
                this.mSp.edit().putBoolean("reference_is_show_guide", false).commit();
            }
            if (this.mEmptyTextParentView != null) {
                this.mEmptyTextParentView.setVisibility(8);
                if (this.mListView != null) {
                    this.mListView.setOverScrollMode(0);
                }
            }
        } else if (showGuide) {
            showSearchGuide();
        } else {
            this.mSearchGuide = null;
            if (this.mEmptyViewStub != null) {
                this.mCallLogEmptyLayout = this.mEmptyViewStub.inflate();
                this.mEmptyView = (TextView) this.mCallLogEmptyLayout.findViewById(R.id.empty_text);
                this.mEmptyTextParentView = (LinearLayout) this.mCallLogEmptyLayout.findViewById(R.id.ll_empty_text_parent_view);
                this.mEmptyViewStub = null;
                this.mActiveView.initViews(this.mCallLogEmptyLayout, this.mActiveListener);
            }
            if (this.mEmptyTextParentView != null) {
                if (this.mCurrentCallTypeFilter != 4) {
                    this.mEmptyTextParentView.setVisibility(0);
                }
                if (this.mListView != null) {
                    this.mListView.setOverScrollMode(2);
                }
            }
            adjustNoCallIconPosition();
            setEmptyView();
        }
    }

    public void showNotifacationDialog() {
        this.mDialog = ClearCallLogDialog.showNotifacation(getFragmentManager());
        this.mDialog.setListener(this);
    }

    private void showSearchGuide() {
        if (this.mEmptyViewStub != null) {
            this.mCallLogEmptyLayout = this.mEmptyViewStub.inflate();
            this.mEmptyView = (TextView) this.mCallLogEmptyLayout.findViewById(R.id.empty_text);
            this.mEmptyTextParentView = (LinearLayout) this.mCallLogEmptyLayout.findViewById(R.id.ll_empty_text_parent_view);
            this.mSearchGuide = (LinearLayout) this.mCallLogEmptyLayout.findViewById(R.id.search_guide);
            this.mEmptyViewStub = null;
            this.mActiveView.initViews(this.mCallLogEmptyLayout, this.mActiveListener);
        }
        if (this.mSearchGuide != null) {
            this.mSearchGuide.setVisibility(0);
            TextView guideConten = (TextView) this.mSearchGuide.findViewById(R.id.guideContent);
            TextView textView6 = (TextView) this.mSearchGuide.findViewById(R.id.dialpad_guide_content6);
            TextView textView7 = (TextView) this.mSearchGuide.findViewById(R.id.dialpad_guide_content7);
            TextView textView8 = (TextView) this.mSearchGuide.findViewById(R.id.dialpad_guide_content8);
            int color = ImmersionUtils.getControlColor(getResources());
            String sColor = Integer.toString(getResources().getColor(R.color.dialpad_guidle_letter_color));
            if (color != 0) {
                sColor = Integer.toString(color);
            }
            String text = this.mContext.getResources().getString(R.string.dialpad_guide_content, new Object[]{sColor, sColor, sColor});
            String text6 = this.mContext.getResources().getString(R.string.dialpad_guide_content6, new Object[]{sColor});
            String text7 = this.mContext.getResources().getString(R.string.dialpad_guide_content7, new Object[]{sColor});
            String text8 = this.mContext.getResources().getString(R.string.dialpad_guide_content8, new Object[]{sColor});
            guideConten.setText(Html.fromHtml(text));
            textView6.setText(Html.fromHtml(text6));
            textView7.setText(Html.fromHtml(text7));
            textView8.setText(Html.fromHtml(text8));
        }
        adjustNoCallIconPosition();
    }

    public boolean isShowSearchGuide() {
        boolean z = false;
        if (this.mSearchGuide == null) {
            return false;
        }
        if (this.mSearchGuide.getVisibility() == 0) {
            z = true;
        }
        return z;
    }

    private void adjustNoCallIconPosition() {
        FragmentManager lFragmentMgr = getParentFragment().getFragmentManager();
        Fragment lFragment = null;
        if (lFragmentMgr != null) {
            lFragment = lFragmentMgr.findFragmentByTag("tab-pager-dialer");
        }
        if (lFragment instanceof DialpadFragment) {
            adjustNoCallIconPostion((DialpadFragment) lFragment);
        }
    }

    public void adjustNoCallIconPostion(DialpadFragment fragment) {
        View currentView = this.mSearchGuide != null ? this.mSearchGuide : this.mEmptyTextParentView;
        if (currentView != null && (currentView.getVisibility() == 0 || this.mCurrentCallTypeFilter == 4)) {
            boolean isDialpadVisible = fragment != null ? fragment.isDialpadVisible() : false;
            boolean isPor = getResources().getConfiguration().orientation == 1;
            Activity activity = getActivity();
            if (this.mCurrentCallTypeFilter == 4) {
                if (this.mSearchGuide != null) {
                    this.mSearchGuide.setVisibility(8);
                }
                if (this.mActiveView != null) {
                    this.mActiveView.setVmDescriptionLocation(activity, isDialpadVisible, isPor);
                }
            }
            if (activity != null) {
                int paddingTop;
                if (this.mSearchGuide == null) {
                    if (isPor) {
                        if (isDialpadVisible) {
                            paddingTop = activity.getResources().getDimensionPixelSize(R.dimen.call_log_empty_icon_dialpad_visible_margin_top);
                        } else {
                            paddingTop = CommonUtilMethods.getMarginTopPix(activity, 0.3d, isPor);
                        }
                        this.mEmptyTextParentView.setGravity(49);
                        currentView.setPadding(0, paddingTop, 0, 0);
                    } else {
                        this.mEmptyTextParentView.setGravity(17);
                        currentView.setPadding(0, 0, 0, CommonUtilMethods.getActionBarAndStatusHeight(activity, isPor));
                    }
                } else if (isPor) {
                    if (isDialpadVisible) {
                        paddingTop = ContactDpiAdapter.getNewPxDpi(R.dimen.contact_nocall_icon_top_margin, this.mContext);
                    } else {
                        paddingTop = CommonUtilMethods.getMarginTopPix(activity, 0.3d, isPor);
                    }
                    this.mSearchGuide.setGravity(49);
                    currentView.setPadding(0, paddingTop, 0, 0);
                } else {
                    this.mSearchGuide.setGravity(17);
                    currentView.setPadding(0, 0, 0, CommonUtilMethods.getActionBarAndStatusHeight(activity, isPor));
                }
            }
        }
    }

    public void onVoicemailStatusFetched(Cursor statusCursor) {
        boolean z;
        boolean z2 = false;
        String str = "CallLogFragment";
        StringBuilder append = new StringBuilder().append("onVoicemailStatusFetched,(statusCursor != null) : ");
        if (statusCursor != null) {
            z = true;
        } else {
            z = false;
        }
        HwLog.d(str, append.append(z).toString());
        if (statusCursor != null) {
            try {
                updateVoicemailStatusMessage(statusCursor);
                int activeSources = this.mVoicemailStatusHelper.getNumberActivityVoicemailSources(statusCursor);
                String todoActivedId = null;
                Fragment dialpadFragment = getGoalFragment("tab-pager-dialer");
                if (dialpadFragment instanceof DialpadFragment) {
                    todoActivedId = ((DialpadFragment) dialpadFragment).onVoicemailStatusFetched(this.mVoicemailStatusHelper.getActivityVoicemailSoucesAccounts(statusCursor));
                }
                if (this.mActiveView != null) {
                    ActiveVVM4CMView activeVVM4CMView = this.mActiveView;
                    if (activeSources != 0) {
                        z2 = true;
                    }
                    activeVVM4CMView.onVoicemailStatusFetched(z2, todoActivedId);
                }
                MoreCloseables.closeQuietly(statusCursor);
                this.mVoicemailStatusFetched = true;
                destroyEmptyLoaderIfAllDataFetched();
            } catch (Throwable th) {
                MoreCloseables.closeQuietly(statusCursor);
            }
        }
    }

    private void destroyEmptyLoaderIfAllDataFetched() {
        if (this.mCallLogFetched && this.mVoicemailStatusFetched && this.mEmptyLoaderRunning) {
            this.mEmptyLoaderRunning = false;
            if (isAdded()) {
                getLoaderManager().destroyLoader(0);
            }
        }
        if (this.mCallLogFetched && this.mVoicemailStatusFetched && this.mActiveView != null) {
            Fragment dialpadFragment = getGoalFragment("tab-pager-dialer");
            boolean isDialpadVisible = true;
            if (dialpadFragment instanceof DialpadFragment) {
                isDialpadVisible = ((DialpadFragment) dialpadFragment).isDialpadVisible();
            }
            this.mActiveView.onAllDataFetched(this.mCurrentCallTypeFilter, isDialpadVisible);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        PLog.d(0, "CallLogFragment onCreateView begin");
        ViewGroup view = (ViewGroup) ((PeopleActivity) this.mContext).getContactListHelper().getCallLogView();
        this.mCallLogList = view.findViewById(R.id.call_log_list);
        this.mCallLogList.setVisibility(8);
        this.mListView = (ListView) view.findViewById(16908298);
        if (this.mListView != null) {
            this.mListView.setFastScrollEnabled(true);
        }
        this.mSuspentionScroller = (SuspentionScroller) view.findViewById(R.id.suspention_scroller);
        initListView();
        this.mEmptyViewStub = (ViewStub) view.findViewById(R.id.contact_empty_view_stub);
        this.mSp = SharePreferenceUtil.getDefaultSp_de(getActivity());
        this.mVoicemailStatusHelper = new VoicemailStatusHelperImpl();
        this.mStatusMessageView = view.findViewById(R.id.voicemail_status);
        this.mStatusMessageText = (TextView) view.findViewById(R.id.voicemail_status_message);
        this.mStatusMessageAction = (TextView) view.findViewById(R.id.voicemail_status_action);
        View voicemail_status_message_layout = view.findViewById(R.id.voicemail_status_message_layout);
        if (CommonUtilMethods.isLayoutRTL()) {
            this.mStatusMessageText.setLayoutDirection(1);
            this.mStatusMessageAction.setLayoutDirection(1);
            voicemail_status_message_layout.setLayoutDirection(1);
        }
        if (this.mProgressDialog != null) {
            prepareProgressDialog();
        }
        this.mLoadingView = view.findViewById(R.id.loadingcontacts);
        this.mLoadingView.setVisibility(0);
        if (!(mSavedCallLogData == null || mSavedCallLogData.isClosed())) {
            boolean bClearSaveCalllog;
            if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                bClearSaveCalllog = ContactsApplication.isAdminToGuestMode();
            } else {
                bClearSaveCalllog = false;
            }
            if (bClearSaveCalllog) {
                mSavedCallLogData.close();
                saveCallLogData(null);
            } else {
                onCallsFetched(mSavedCallLogData);
            }
            if (HwLog.HWDBG) {
                HwLog.d("CallLogFragment", "onCreateView clear save calllog " + bClearSaveCalllog);
            }
        }
        PLog.d(0, "CallLogFragment onCreateView end");
        return view;
    }

    private void initListView() {
        if (this.mListView != null) {
            boolean showDialpad = false;
            int dialpadHeight = 0;
            Fragment goalFragment = getGoalFragment("tab-pager-dialer");
            if (goalFragment instanceof DialpadFragment) {
                showDialpad = ((DialpadFragment) goalFragment).isDialpadVisible();
                dialpadHeight = ((DialpadFragment) goalFragment).getDialerHeight(true);
            }
            if (!showDialpad || ContactDpiAdapter.SRC_DPI == 0 || ContactDpiAdapter.REAL_Dpi == 0) {
                LayoutParams lpa = new LayoutParams(-1, -1);
                lpa.bottomMargin = dialpadHeight;
                this.mListView.setLayoutParams(lpa);
            } else if (SingleHandModeManager.getInstance(this.mContext).isSingleHandFeatureEnabled()) {
                float singleDigitsPad = 4.0f * (this.mContext.getResources().getDimension(R.dimen.dialpad_huawei_item_height) - this.mContext.getResources().getDimension(R.dimen.dialpad_huawei_item_height_singlehandmode));
                float newDimens = ((float) (getResources().getDimensionPixelSize(R.dimen.calllog_list_first_time_height) * ContactDpiAdapter.SRC_DPI)) / ((float) ContactDpiAdapter.REAL_Dpi);
                if (this.mListView.getLayoutParams() instanceof LayoutParams) {
                    LayoutParams lp = (LayoutParams) this.mListView.getLayoutParams();
                    if (ContactDpiAdapter.NOT_SRC_DPI) {
                        lp.height = (int) ((((((float) (getResources().getDimensionPixelSize(R.dimen.dialpad_dialer_container_init_height) * ContactDpiAdapter.SRC_DPI)) / ((float) ContactDpiAdapter.REAL_Dpi)) - this.mContext.getResources().getDimension(R.dimen.dialpad_dialer_container_init_height)) + newDimens) + singleDigitsPad);
                    } else {
                        lp.height = (int) (newDimens + singleDigitsPad);
                    }
                }
            }
            this.mListView.setFriction(this.mFriction);
            this.mListView.setVelocityScale(this.mVelocityScale);
        }
    }

    private Fragment getGoalFragment(String tag) {
        FragmentManager lFragmentMgr = getParentFragment().getFragmentManager();
        if (lFragmentMgr != null) {
            return lFragmentMgr.findFragmentByTag(tag);
        }
        return null;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(this.mAdapter);
        this.mListView.setItemsCanFocus(true);
        this.mListView.setOnCreateContextMenuListener(this);
        this.mListView.setOnScrollListener(this.mAdapter);
        this.mListView.setChoiceMode(1);
        if (this.isCallLogForSingleContact) {
            getView().setBackgroundResource(R.drawable.people_background);
        }
        this.mAdapter.onRestoreInstanceState(savedInstanceState);
    }

    public void onStart() {
        getLoaderManager().initLoader(0, new Bundle(), new Callback(getActivity()));
        this.mEmptyLoaderRunning = true;
        if (this.mIsContactsGetDirty) {
            ContactPhotoManager.getInstance(this.mContext).refreshCache();
        }
        this.mIsIdle = false;
        super.onStart();
    }

    public void onResume() {
        super.onResume();
        ((PeopleActivity) getActivity()).getContactListHelper().checkVoicemailNumberChange(this.mHandler, this.mAdapter);
        this.mNumberLocationLoader = new NumberLocationLoader(this.mContext.getApplicationContext(), this.mListView);
        if (this.mVoicemailPlaybackPresenter != null) {
            this.mVoicemailPlaybackPresenter.onResume();
        }
        if (this.mAdapter != null) {
            this.mAdapter.updateCustSetting();
            this.mAdapter.setNumberLocationLoader(this.mNumberLocationLoader);
            this.mAdapter.setSpecialNumbersMap();
            this.mAdapter.setHandleSpecialNumbers();
            this.mAdapter.clearClickedFlag();
        }
        boolean isLand = this.mContext.getResources().getConfiguration().orientation == 2;
        if (this.mListView != null && CommonUtilMethods.calcIfNeedSplitScreen() && isLand) {
            this.mListView.invalidateViews();
        }
        this.mContext.registerReceiver(this.mRefreshReceiver, new IntentFilter("com.android.contacts.action.UPDATE_YP"), permission.HW_CONTACTS_ALL, null);
        if (this.isCallLogForSingleContact) {
            readCallInfo();
        } else {
            this.mNumberLocationLoader.loadInBackground();
            if (this.mAdapter != null && this.mAdapter.restoreTimeFormatFlag()) {
                this.mRefreshDataRequired = true;
            } else if (!(this.mAdapter == null || this.mPausingDate == null)) {
                Calendar currentDate = Calendar.getInstance();
                if (currentDate.get(12) == this.mPausingDate.get(12) && currentDate.get(11) == this.mPausingDate.get(11) && currentDate.get(5) == this.mPausingDate.get(5) && currentDate.get(2) == this.mPausingDate.get(2) && currentDate.get(1) == this.mPausingDate.get(1) && currentDate.get(15) == this.mPausingDate.get(15)) {
                    if (this.mAdapter.hasExpandedItem()) {
                    }
                }
                this.mAdapter.notifyDataSetChanged();
            }
            int currentMode = EmuiFeatureManager.isSuperSaverMode() ? 1 : 0;
            if (originMode != currentMode) {
                this.mRefreshDataRequired = true;
                setOriginMode(currentMode);
            }
            if (isRefreshData()) {
                refreshData(true);
            }
        }
        if (this.mIsMultiSim) {
            this.mPhoneStateListener.disableFirstCallStateChangedRequired();
            SimFactoryManager.listenPhoneState(this.mPhoneStateListener, 32);
        }
        SimFactoryManager.addSimStateListener(this);
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                if (CallLogFragment.this.mAdapter != null) {
                    CallLogFragment.this.mAdapter.updateSimStatesAndResources();
                }
            }
        });
    }

    public void onSaveInstanceState(Bundle outState) {
        if (!(CommonUtilMethods.calcIfNeedSplitScreen() && outState.getBoolean("save_instance_state_manually"))) {
            super.onSaveInstanceState(outState);
        }
        if (this.isCallLogForSingleContact) {
            outState.putBoolean("singleContactCallLog", this.isCallLogForSingleContact);
            if (this.mContactCallLogUri != null) {
                outState.putParcelable("contactCallLogUri", this.mContactCallLogUri);
            }
        }
        this.mAdapter.onSaveInstanceState(outState);
        if (this.mVoicemailPlaybackPresenter != null) {
            this.mVoicemailPlaybackPresenter.onSaveInstanceState(outState);
        }
        outState.putInt("currentFilter", this.mCurrentCallTypeFilter);
        outState.putInt("currentTab", this.mCurrentNetworkTypeFilter);
        outState.putLong("split_calllog_id", this.mAdapter.getClickedCalllogID());
        outState.putString("voicemail_uri", this.mVoicemailUri);
        Cursor currentCursor = this.mAdapter.getCursor();
        if (mSavedCallLogData != currentCursor) {
            if (mSavedCallLogData != null) {
                mSavedCallLogData.close();
            }
            mSavedCallLogData = currentCursor;
        }
    }

    private void readCallInfo() {
        if (!(this.mContactCallLogUri == null || this.mAdapter == null)) {
            this.mContactNumbersList = getNumberList(this.mContactCallLogUri);
            if (this.mContactNumbersList == null || this.mContactNumbersList.size() == 0) {
                setEmptyView();
                this.mAdapter.setLoading(false);
                return;
            }
            getCallDetailsByContactNumber(this.mContactNumbersList);
        }
    }

    private void getCallDetailsByContactNumber(ArrayList<String> mContactNumbersList) {
        if (this.mCallLogQueryHandler != null) {
            this.mAdapter.invalidateCache();
            this.mAdapter.setLoading(true);
            this.mCallLogQueryHandler.setUpContactCallLog(true);
            this.mCallLogQueryHandler.setFilterMode(this.mCurrentNetworkTypeFilter, this.mCurrentCallTypeFilter);
            this.mCallLogQueryHandler.getCallDetailsByContactNumber(mContactNumbersList);
        }
    }

    private ArrayList<String> getNumberList(Uri mContactCallLogUri) {
        if (this.mCallLogQueryHandler != null) {
            Activity activity = getActivity();
            if (activity != null) {
                return this.mCallLogQueryHandler.getNumberList(activity.getContentResolver(), mContactCallLogUri);
            }
        }
        return null;
    }

    private void updateVoicemailStatusMessage(Cursor statusCursor) {
        if (statusCursor != null) {
            List<StatusMessage> messages = this.mVoicemailStatusHelper.getStatusMessages(statusCursor);
            if (messages.size() == 0 || this.mCurrentCallTypeFilter != 4) {
                this.mStatusMessageView.setVisibility(8);
                return;
            }
            this.mStatusMessageView.setVisibility(0);
            final StatusMessage message = (StatusMessage) messages.get(0);
            if (message.showInCallLog()) {
                this.mStatusMessageText.setMaxWidth(this.voicemail_status_message_max_width);
                this.mStatusMessageText.setText(message.callLogMessageId);
            }
            if (message.actionMessageId != -1) {
                this.mStatusMessageAction.setText(message.actionMessageId);
            }
            if (message.actionUri != null) {
                this.mStatusMessageAction.setVisibility(0);
                this.mStatusMessageAction.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        try {
                            CallLogFragment.this.getActivity().startActivity(new Intent("android.intent.action.VIEW", message.actionUri));
                        } catch (ActivityNotFoundException e) {
                            HwLog.e("CallLogFragment", "Activity Not Found Exception");
                        }
                    }
                });
                return;
            }
            this.mStatusMessageAction.setVisibility(8);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isNeedFreshForHighlight(boolean isLand) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && isLand && (getActivity() instanceof PeopleActivity) && ((PeopleActivity) getActivity()).getNeedMaskDialpad()) {
            return true;
        }
        return false;
    }

    public void onPause() {
        super.onPause();
        if (this.mAdapter.getClickedCalllogID() == -1) {
            this.mAdapter.setClickedCalllogID(this.mSelectCallLogId);
            this.mSelectCallLogId = -1;
        }
        boolean isLand = this.mContext.getResources().getConfiguration().orientation == 2;
        if (this.mListView != null && isNeedFreshForHighlight(isLand)) {
            this.mListView.invalidateViews();
        }
        if (this.mNumberLocationLoader != null) {
            this.mNumberLocationLoader.cancelLoading();
        }
        if (this.mVoicemailPlaybackPresenter != null) {
            this.mVoicemailPlaybackPresenter.onPause(getActivity());
        }
        this.mAdapter.stopRequestProcessing();
        this.mAdapter.saveTimeFormatFlag();
        if (this.mIsMultiSim) {
            SimFactoryManager.listenPhoneState(this.mPhoneStateListener, 0);
        }
        SimFactoryManager.removeSimStateListener(this);
        this.mContext.unregisterReceiver(this.mRefreshReceiver);
        this.mPausingDate = Calendar.getInstance();
    }

    public void onStop() {
        super.onStop();
        if (isRemoving() || getActivity().isChangingConfigurations() || getActivity().isFinishing()) {
            this.mAdapter.destroyRequestProcessing();
            this.mCallLogQueryHandler.cleanUp();
        }
        this.mIsIdle = true;
        updateOnExit();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mAdapter.destroyRequestProcessing();
        this.mAdapter.destroyEmergencyNumberHelper();
        if (this.mVoicemailPlaybackPresenter != null) {
            this.mVoicemailPlaybackPresenter.onDestroy(getActivity());
        }
        if (getActivity() instanceof PeopleActivity) {
            ((PeopleActivity) getActivity()).setTabSelectedListener(TabState.DIALER, null);
        }
        this.mAdapter.clearPresetCache();
        if (this.mTask != null) {
            this.mTask.cancel(true);
        }
        this.mContext.getContentResolver().unregisterContentObserver(this.voicemailStatusObserver);
    }

    public void fetchCalls() {
        if (this.isCallLogForSingleContact) {
            readCallInfo();
            return;
        }
        this.mCallLogQueryHandler.setUpContactCallLog(false);
        this.mCallLogQueryHandler.fetchCalls(this.mCallTypeFilter, this.mEmergencynumbers);
    }

    public void startCallsQuery() {
        this.mAdapter.setLoading(true);
        if (this.mCurrentCallTypeFilter == 6) {
            callLogNumbers();
        } else {
            this.mCallLogQueryHandler.fetchCalls(this.mCallTypeFilter, this.mEmergencynumbers);
        }
    }

    private void startVoicemailStatusQuery() {
        this.mCallLogQueryHandler.fetchVoicemailStatus();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void setCallLogFilterType(int aSelectionCalltype, int aSelectionNetwork) {
        this.mCurrentCallTypeFilter = aSelectionCalltype;
        this.mCurrentNetworkTypeFilter = aSelectionNetwork;
        refreshCallLogWithCurrentFilter();
    }

    public void setCallLogFilterType(int aSelectionCalltype, int aSelectionNetwork, boolean isUser) {
        this.mActiveView.setFromUser(isUser);
        this.mCurrentCallTypeFilter = aSelectionCalltype;
        this.mCurrentNetworkTypeFilter = aSelectionNetwork;
        refreshCallLogWithCurrentFilter();
    }

    public void refreshCallLogWithCurrentFilter() {
        this.mRefreshDataRequired = true;
        if (this.isCallLogForSingleContact) {
            readCallInfo();
        } else {
            refreshData(true);
        }
    }

    public void handleClearCallLog() {
        this.mDialog = ClearCallLogDialog.show(getFragmentManager());
        this.mDialog.setListener(this);
    }

    public void handleDeleteMultiCallLog() {
        Intent lIntent;
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            if (getActivity() instanceof PeopleActivity) {
                ((PeopleActivity) getActivity()).saveInstanceValues();
            }
            lIntent = new Intent(getActivity(), TranslucentActivity.class);
        } else {
            lIntent = new Intent(getActivity(), CallLogMultiSelectionActivity.class);
        }
        lIntent.putExtra("call_type_filter", this.mCurrentCallTypeFilter);
        lIntent.putExtra("network_type_filter", this.mCurrentNetworkTypeFilter);
        startActivity(lIntent);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_calllog:
                handleDeleteMultiCallLog();
                return true;
            case R.id.delete_all:
                handleClearCallLog();
                return true;
            default:
                return false;
        }
    }

    public void callSelectedEntry() {
        int position = -1;
        if (this.mListView != null) {
            position = this.mListView.getSelectedItemPosition();
        }
        if (position < 0) {
            position = 0;
        }
        Cursor cursor = (Cursor) this.mAdapter.getItem(position);
        if (cursor != null) {
            String number = cursor.getString(1);
            if (ContactsUtils.isNumberDialable(number, cursor.getInt(17))) {
                Intent intent;
                if (PhoneNumberUtils.isUriNumber(number)) {
                    intent = CallUtil.getCallIntent(Uri.fromParts("sip", number, null));
                } else {
                    int callType = cursor.getInt(4);
                    if (!number.startsWith("+") && (callType == 1 || CommonUtilMethods.isMissedType(callType))) {
                        number = this.mAdapter.getBetterNumberFromContacts(number, cursor.getString(5));
                    }
                    intent = CallUtil.getCallIntent(Uri.fromParts("tel", number, null));
                }
                intent.setFlags(276824064);
                startActivity(intent);
            }
        }
    }

    @VisibleForTesting
    CallLogAdapter getAdapter() {
        return this.mAdapter;
    }

    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (this.mMenuVisible != menuVisible) {
            this.mMenuVisible = menuVisible;
            if (!menuVisible) {
                if (this.mEmptyView != null) {
                    this.mEmptyView.setVisibility(8);
                    if (this.mListView != null) {
                        this.mListView.setOverScrollMode(0);
                    }
                }
                this.mCurrentCallTypeFilter = 0;
                updateOnExit();
            } else if (isResumed() && this.isCallLogForSingleContact) {
                readCallInfo();
            } else if (isResumed()) {
                refreshData(true);
            }
        }
    }

    private void refreshData(boolean refreshCallStatusAndNotification) {
        Activity actRef = getActivity();
        if (isRemoving() || actRef == null || actRef.isFinishing()) {
            HwLog.e("CallLogFragment", "refreshData Fragment is removing or destroyed so not refreshing");
            return;
        }
        if (this.mRefreshDataRequired) {
            PLog.d(0, "CallLogFragment refreshData");
            this.mAdapter.invalidateCache();
            this.mCallLogQueryHandler.setUpContactCallLog(false);
            this.mCallLogQueryHandler.setFilterMode(this.mCurrentNetworkTypeFilter, this.mCurrentCallTypeFilter);
            this.mCallLogFetched = false;
            this.mVoicemailStatusFetched = false;
            startCallsQuery();
            if (QueryUtil.isSystemAppForContacts()) {
                startVoicemailStatusQuery();
            }
            if (refreshCallStatusAndNotification) {
                updateOnEntry();
            }
            this.mRefreshDataRequired = false;
        }
    }

    public boolean isRefreshData() {
        return this.mListView != null && this.mListView.getCount() > 1;
    }

    private void setEmptyView() {
        if (isVisible()) {
            Drawable topDrawable;
            String lMsg = "";
            if (!this.mIsMultiSim) {
                lMsg = getNoCallsStringByCallTypeForSingleSIM();
            } else if (2 == this.mCurrentNetworkTypeFilter) {
                lMsg = getNoCallsStringByCallTypeForSingleSIM();
            } else {
                String lCardType = null;
                String lCallTypeMsg = getNoCallsStringByCallTypeForDualSIM();
                if (this.mCurrentNetworkTypeFilter == 0) {
                    lCardType = SimFactoryManager.getSimCardDisplayLabel(0);
                } else if (this.mCurrentNetworkTypeFilter == 1) {
                    lCardType = SimFactoryManager.getSimCardDisplayLabel(1);
                }
                if (TextUtils.isEmpty(lCallTypeMsg)) {
                    lMsg = String.format(getString(R.string.new_str_no_calls), new Object[]{lCardType});
                } else {
                    lMsg = String.format(lCallTypeMsg, new Object[]{lCardType});
                }
            }
            if (this.mCurrentCallTypeFilter == 4) {
                topDrawable = getResources().getDrawable(R.drawable.ic_no_vociemail);
            } else {
                topDrawable = getResources().getDrawable(R.drawable.nocalls);
            }
            topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
            this.mEmptyView.setCompoundDrawables(null, topDrawable, null, null);
            this.mEmptyView.setContentDescription(lMsg);
            this.mEmptyView.setText(lMsg);
        }
    }

    private String getNoCallsStringByCallTypeForDualSIM() {
        int lResId;
        String lMsg = "";
        switch (this.mCurrentCallTypeFilter) {
            case 1:
                lResId = R.string.msg_type_incoming;
                break;
            case 2:
                lResId = R.string.msg_type_outgoing;
                break;
            case 3:
            case 5:
                lResId = R.string.msg_type_missed;
                break;
            case 4:
                lResId = R.string.msg_type_voicemails;
                break;
            case 6:
                lResId = R.string.msg_type_emergency;
                break;
            default:
                lResId = -1;
                break;
        }
        if (-1 != lResId) {
            return getString(lResId);
        }
        return lMsg;
    }

    private String getNoCallsStringByCallTypeForSingleSIM() {
        int lResId;
        String lMsg = "";
        switch (this.mCurrentCallTypeFilter) {
            case 1:
                lResId = R.string.new_str_filter_no_incoming_calls;
                break;
            case 2:
                lResId = R.string.new_str_filter_no_outgoing_calls;
                break;
            case 3:
            case 5:
                lResId = R.string.new_str_filter_no_missed_calls;
                break;
            case 4:
                lResId = R.string.new_str_filter_no_voicemails;
                break;
            case 6:
                lResId = R.string.new_str_filter_no_emergency_calls;
                break;
            default:
                lResId = R.string.contact_calllog_empty_string_alltab;
                break;
        }
        return getString(lResId);
    }

    private void removeMissedCallNotifications() {
        new RemoveMissedCallNotification().executeOnExecutor(AsyncTaskExecutors.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private void updateOnExit() {
        updateOnTransition(false);
    }

    private void updateOnEntry() {
        updateOnTransition(true);
    }

    private void updateOnTransition(boolean onEntry) {
        if (this.mKeyguardManager != null && !this.mKeyguardManager.inKeyguardRestrictedInputMode() && !onEntry) {
            this.mCallLogQueryHandler.markMissedCallsAsRead();
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo contextMenuInfo = (AdapterContextMenuInfo) menuInfo;
        CallLogListItemViews views = (CallLogListItemViews) contextMenuInfo.targetView.getTag();
        if (views != null) {
            String toUse;
            final String str;
            final String number = views.number;
            int numPresentation = views.mNumPresentation;
            Cursor cursor = (Cursor) getListAdapter().getItem(contextMenuInfo.position - 1);
            ContactInfo info = this.mAdapter.getContactInfo(number, cursor != null ? cursor.getString(5) : null);
            if (info == null) {
                info = ContactInfo.EMPTY;
            }
            boolean canPlaceCallsTo = this.mPhoneNumberHelper.canPlaceCallsTo(number, numPresentation);
            boolean isVoicemailNumber = this.mPhoneNumberHelper.isVoicemailNumber(number);
            boolean isSipNumber = this.mPhoneNumberHelper.isSipNumber(number);
            String cnapName = "";
            boolean isContact;
            if (info.lookupUri == null) {
                isContact = false;
                if (canPlaceCallsTo) {
                    if (!TextUtils.isEmpty(info.name)) {
                        toUse = info.name;
                    } else if (PhoneNumberUtils.isLocalEmergencyNumber(this.mContext, number)) {
                        toUse = this.mContext.getResources().getString(R.string.emergency_number);
                    } else if (info.formattedNumber == null) {
                        toUse = number;
                    } else {
                        toUse = info.formattedNumber;
                    }
                    if (isVoicemailNumber) {
                        toUse = getString(R.string.voicemail);
                    }
                    CallLogAdapter callLogAdapter = this.mAdapter;
                    if (CallLogAdapter.getCust() != null) {
                        callLogAdapter = this.mAdapter;
                        cnapName = CallLogAdapter.getCust().getCustomContextMenuTitle(getActivity(), views, toUse);
                        if (toUse.equals(cnapName)) {
                            cnapName = "";
                        } else {
                            toUse = cnapName;
                        }
                    }
                } else {
                    toUse = this.mPhoneNumberHelper.getDisplayNumber(number, numPresentation, info.formattedNumber, views.mPostDialDigits).toString();
                }
                menu.setHeaderTitle(toUse);
                this.mBlackListOnlyName = "";
            } else {
                isContact = true;
                toUse = info.name;
                this.mBlackListOnlyName = toUse;
                menu.setHeaderTitle(toUse);
            }
            toUse = HwCustPreloadContacts.EMPTY_STRING + toUse;
            boolean isPredefinedContact = !TextUtils.isEmpty(info.name) && info.lookupUri == null;
            if (EmuiFeatureManager.isNumberMarkFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner() && !isContact && !isPredefinedContact && canPlaceCallsTo && !PhoneNumberUtils.isVoiceMailNumber(number)) {
                str = number;
                menu.add(R.string.menu_mark_as).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        try {
                            CallLogFragment.this.startActivityForResult(NumberMarkUtil.getIntentForMark(CallLogFragment.this.getActivity().getApplicationContext(), str), 100);
                            StatisticalHelper.report(5017);
                            ExceptionCapture.reportScene(90);
                        } catch (ActivityNotFoundException e) {
                            HwLog.e("CallLogFragment", "Numbermark Activity not found.");
                        }
                        return true;
                    }
                });
            }
            addSaveContactMenuItem(info, menu, isVoicemailNumber, canPlaceCallsTo, views.mOriginMarkInfo, cnapName);
            if (!(!canPlaceCallsTo || isSipNumber || isVoicemailNumber)) {
                str = number;
                menu.add(R.string.contact_menu_send_message).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        CallLogFragment.this.sendSmsMessage(str);
                        return true;
                    }
                });
            }
            if (canPlaceCallsTo) {
                str = number;
                menu.add(R.string.menu_copy_number).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        CharSequence textToCopy = str;
                        if (TextUtils.isEmpty(textToCopy) || CallLogFragment.this.getActivity() == null) {
                            return true;
                        }
                        ((ClipboardManager) CallLogFragment.this.getActivity().getSystemService("clipboard")).setPrimaryClip(new ClipData(str, new String[]{"vnd.android.cursor.item/phone_v2"}, new Item(textToCopy)));
                        Activity act = CallLogFragment.this.getActivity();
                        if (act != null) {
                            act.invalidateOptionsMenu();
                        }
                        StatisticalHelper.report(5015);
                        ExceptionCapture.reportScene(92);
                        return true;
                    }
                });
            }
            if (!(!canPlaceCallsTo || isSipNumber || isVoicemailNumber)) {
                str = number;
                menu.add(R.string.recentCalls_editBeforeCall).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent("android.intent.action.DIAL", new PhoneNumberHelper(CallLogFragment.this.getResources()).getCallUri(str));
                        intent.setPackage("com.android.contacts");
                        CallLogFragment.this.getActivity().startActivity(intent);
                        StatisticalHelper.report(5012);
                        ExceptionCapture.reportScene(93);
                        return true;
                    }
                });
            }
            if (EmuiFeatureManager.isBlackListFeatureEnabled() && canPlaceCallsTo && MultiUsersUtils.isCurrentUserOwner()) {
                int blacklistMenuString;
                IHarassmentInterceptionService mService = Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService"));
                if (BlacklistCommonUtils.checkPhoneNumberFromBlockItem(mService, number)) {
                    blacklistMenuString = R.string.contact_menu_remove_from_blacklist;
                } else {
                    blacklistMenuString = R.string.contact_menu_add_to_blacklist;
                }
                str = number;
                final IHarassmentInterceptionService iHarassmentInterceptionService = mService;
                menu.add(blacklistMenuString).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        CallLogFragment.this.performAddBlackList(item, str, iHarassmentInterceptionService);
                        return true;
                    }
                });
            }
            final CallLogListItemViews callLogListItemViews = views;
            menu.add(R.string.str_delete_call_log_entry).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    CallLogFragment.this.performDeleteCalllog(CallLogFragment.this.mContext, callLogListItemViews);
                    return true;
                }
            });
            PhoneCallDetails details = views.phoneCallDetailsViews.getPhoneCallDetails();
            final long photoId = info.photoId;
            final Uri photoUri = info.photoUri;
            final Uri lookupUri = info.lookupUri;
            final CharSequence name = details.name;
            final CharSequence nameOrNumber = !TextUtils.isEmpty(name) ? name : details.number;
            final String displayNumber = this.mPhoneNumberHelper.getDisplayNumber(number, numPresentation, info.formattedNumber, views.mPostDialDigits).toString();
            final PhoneAccountHandle accountHandle = views.accountHandle;
            final String numberLabel = info.label;
            if (canPlaceCallsTo && CallUtil.isCallWithSubjectSupported(this.mContext) && !isSipNumber && !isVoicemailNumber) {
                menu.add(R.string.call_with_a_note).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        CallSubjectDialog.start((Activity) CallLogFragment.this.mContext, photoId, photoUri, lookupUri, (String) nameOrNumber, number, TextUtils.isEmpty(name) ? null : displayNumber, numberLabel, accountHandle);
                        return true;
                    }
                });
            }
            if (getActivity() instanceof PeopleActivity) {
                ((PeopleActivity) getActivity()).setContextMenuStatus(false);
            }
        }
    }

    private void addSaveContactMenuItem(final ContactInfo info, Menu menu, boolean isVoicemailNumber, boolean canPlaceCallsTo, String originMarkInfo, String cnapNameInfo) {
        if (info.lookupUri == null && !isVoicemailNumber && canPlaceCallsTo) {
            final String lName = info.name;
            final ContactInfo contactInfo = info;
            final String str = cnapNameInfo;
            final String str2 = originMarkInfo;
            menu.add(R.string.pickerNewContactText).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    Intent mainActionIntent = new Intent("android.intent.action.INSERT", Contacts.CONTENT_URI);
                    mainActionIntent.setType("vnd.android.cursor.dir/contact");
                    if (!TextUtils.isEmpty(lName)) {
                        mainActionIntent.putExtra("name", lName);
                    }
                    mainActionIntent.putExtra("phone", contactInfo.number);
                    mainActionIntent.putExtra("intent_key_is_from_dialpad", true);
                    if (!TextUtils.isEmpty(str)) {
                        mainActionIntent.putExtra("name", str);
                    }
                    if (!TextUtils.isEmpty(str2)) {
                        mainActionIntent.putExtra("name", str2);
                    }
                    CallLogFragment.this.getActivity().startActivity(mainActionIntent);
                    return true;
                }
            });
            menu.add(R.string.contact_saveto_existed_contact).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    Intent mainActionIntent = new Intent("android.intent.action.INSERT_OR_EDIT");
                    mainActionIntent.setType("vnd.android.cursor.item/contact");
                    mainActionIntent.putExtra("handle_create_new_contact", false);
                    if (!TextUtils.isEmpty(lName)) {
                        mainActionIntent.putExtra("name", lName);
                    }
                    mainActionIntent.putExtra("phone", info.number);
                    mainActionIntent.putExtra("intent_key_is_from_dialpad", true);
                    if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                        mainActionIntent.setClass(CallLogFragment.this.getActivity(), ContactSelectionActivity.TranslucentActivity.class);
                    }
                    CallLogFragment.this.getActivity().startActivity(mainActionIntent);
                    return true;
                }
            });
        }
    }

    public void simStateChanged(int aSubScription) {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    if (CallLogFragment.this.mAdapter != null) {
                        CallLogFragment.this.mAdapter.updateSimStatesAndResources();
                        CallLogAdapter callLogAdapter = CallLogFragment.this.mAdapter;
                        CallLogAdapter.updateEmergencyNumberCache();
                    }
                    ContactsUtils.updatePredefinedMapForSimChange(CallLogFragment.this.mContext);
                    if (CallLogFragment.this.mListView != null) {
                        CallLogFragment.this.mListView.invalidateViews();
                    }
                    CallLogFragment.this.refreshCallLogWithNewSimState();
                }
            });
        }
    }

    private void refreshCallLogWithNewSimState() {
        if (!SimFactoryManager.isDualSimPresent()) {
            this.mCurrentNetworkTypeFilter = 2;
        }
        refreshCallLogWithCurrentFilter();
    }

    private void callLogNumbers() {
        new AsyncTask<Void, Void, Void>() {
            public Void doInBackground(Void... params) {
                Cursor lCursor = CallLogFragment.this.mContext.getContentResolver().query(Calls.CONTENT_URI, new String[]{"number"}, null, null, null);
                if (lCursor == null || lCursor.getCount() <= 0) {
                    if (lCursor != null) {
                        lCursor.close();
                    }
                    return null;
                }
                CallLogFragment.this.mEmergencynumbers = new ArrayList();
                lCursor.moveToFirst();
                do {
                    if (!CallLogFragment.this.mEmergencynumbers.contains(lCursor.getString(0)) && PhoneNumberUtils.isLocalEmergencyNumber(CallLogFragment.this.mContext, lCursor.getString(0))) {
                        CallLogFragment.this.mEmergencynumbers.add(lCursor.getString(0));
                    }
                } while (lCursor.moveToNext());
                if (lCursor != null) {
                    lCursor.close();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                CallLogFragment.this.mCallLogQueryHandler.fetchCalls(-1, CallLogFragment.this.mEmergencynumbers);
            }
        }.executeOnExecutor(AsyncTaskExecutors.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public void setRefreshDataRequired(boolean aClearMissedCallNotification) {
        Activity actRef = getActivity();
        if (isRemoving() || actRef == null || actRef.isFinishing()) {
            HwLog.e("CallLogFragment", "setRefreshDataRequired Fragment is removing or destroyed so not refreshing");
            return;
        }
        this.mRefreshDataRequired = true;
        if (this.mIsIdle) {
            this.mIsContactsGetDirty = true;
        }
        if (this.mAdapter.isIdle()) {
            refreshData(aClearMissedCallNotification);
        }
    }

    public void setRefreshDataClearCache() {
        this.mAdapter.clearPresetCache();
        this.mAdapter.invalidateCache();
        setRefreshDataRequired(false);
    }

    public void onCurrentSelectedTab() {
        if ((getActivity() instanceof PeopleActivity) && TabState.DIALER == ((PeopleActivity) getActivity()).getCurrentTab()) {
            removeMissedCallNotifications();
            this.mCallLogQueryHandler.markNewCallsAsOld();
            if (this.mVoicemailNotificationRefreshrequired) {
                ContactsThreadPool.getInstance().execute(new Runnable() {
                    public void run() {
                        Context lContext = CallLogFragment.this.getActivity();
                        if (lContext != null) {
                            DefaultVoicemailNotifier.getInstance(lContext.getApplicationContext()).clearAllNotification();
                        }
                    }
                });
                this.mVoicemailNotificationRefreshrequired = false;
            }
        }
    }

    private void prepareProgressDialog() {
        if (this.mProgressDialog == null) {
            this.mProgressDialog = ClearCallLogDialog.showProgress(getFragmentManager());
        }
        final ContentResolver resolver = getActivity().getContentResolver();
        this.mTask = new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                String lSinglContactSelectionArg = null;
                if (CallLogFragment.this.mContactNumbersList != null && CallLogFragment.this.mContactNumbersList.size() > 0) {
                    if (HwLog.HWDBG) {
                        HwLog.v("CallLogFragment", "number list size..." + CallLogFragment.this.mContactNumbersList.size());
                    }
                    int sizeOfNumberList = CallLogFragment.this.mContactNumbersList.size();
                    StringBuilder selectionBuilder = new StringBuilder();
                    String lPhoneNumberSelection = " PHONE_NUMBERS_EQUAL( Calls.NUMBER, ";
                    String lOR = " OR ";
                    int j = 0;
                    while (j < sizeOfNumberList) {
                        if (!(CallLogFragment.this.mContactNumbersList.get(j) == null || "".equals(CallLogFragment.this.mContactNumbersList.get(j)))) {
                            selectionBuilder.append(lPhoneNumberSelection);
                            selectionBuilder.append((String) CallLogFragment.this.mContactNumbersList.get(j));
                            selectionBuilder.append(")").append(lOR);
                        }
                        j++;
                    }
                    lSinglContactSelectionArg = selectionBuilder.delete(selectionBuilder.length() - lOR.length(), selectionBuilder.length()).toString();
                }
                if (CallLogFragment.this.isCallLogForSingleContact) {
                    resolver.delete(Calls.CONTENT_URI, lSinglContactSelectionArg, null);
                } else {
                    resolver.delete(Calls.CONTENT_URI, null, null);
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                onCancelled(result);
            }

            protected void onCancelled(Void result) {
                if (CallLogFragment.this.mProgressDialog != null && CallLogFragment.this.mProgressDialog.isAdded() && !CallLogFragment.this.mProgressDialog.isRemoving()) {
                    CallLogFragment.this.mProgressDialog.dismiss();
                    CallLogFragment.this.mProgressDialog = null;
                }
            }
        };
        this.mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public void clickdone(int mode) {
        if (mode == 2) {
            prepareProgressDialog();
        } else if (mode == 0) {
            ActivationProgressDialog.show(getFragmentManager(), this, 1);
        }
        this.mDialog = null;
    }

    private void deleteCallLog(Intent lIntent, Context context) {
        if (lIntent != null && context != null) {
            long[] ids = lIntent.getLongArrayExtra("EXTRA_CALL_LOG_IDS");
            StringBuilder callIds = new StringBuilder();
            if (ids != null) {
                for (long id : ids) {
                    if (callIds.length() != 0) {
                        callIds.append(",");
                    }
                    callIds.append(id);
                }
            }
            new DeleteThread(this.mContext.getApplicationContext(), callIds).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }
    }

    public ListView getCallsListView() {
        return this.mListView;
    }

    public SuspentionScroller getSuspentionScroller() {
        return this.mSuspentionScroller;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
        if (aResultCode == -1 && aData != null && !this.mActiveView.onActivityResult(aRequestCode, aResultCode, aData)) {
            switch (aRequestCode) {
                case LocationRequest.PRIORITY_HIGH_ACCURACY /*100*/:
                    setRefreshDataRequired(false);
                    break;
            }
        }
    }

    public void handleCallLogTableChange() {
        if (HwLog.HWDBG) {
            HwLog.d("CallLogFragment", "dialtactactivity onchange called");
        }
        this.mVoicemailNotificationRefreshrequired = true;
        setRefreshDataRequired(false);
    }

    public void setCallLogListener(CallLogAdapterListener aDialer) {
        this.mAdapter.setCallLogAdapterListener(aDialer);
    }

    private void performAddBlackList(MenuItem item, String number, IHarassmentInterceptionService aService) {
        if (item != null) {
            if (item.getTitle().equals(getResources().getText(R.string.contact_menu_add_to_blacklist))) {
                BlacklistCommonUtils.handleNumberBlockList(getActivity(), aService, number, this.mBlackListOnlyName, 0, true);
            } else if (item.getTitle().equals(getResources().getText(R.string.contact_menu_remove_from_blacklist))) {
                StatisticalHelper.report(5036);
                BlacklistCommonUtils.handleNumberBlockList(getActivity(), aService, number, this.mBlackListOnlyName, 1, true);
            }
        }
    }

    private void performDeleteCalllog(Context aContext, CallLogListItemViews views) {
        if (aContext != null && views != null) {
            if (views.callType != 4 || views.voicemailUri == null) {
                IntentProvider intentProvider = (IntentProvider) views.secondaryActionViewLayout.getTag();
                if (intentProvider != null) {
                    final Intent lIntent = intentProvider.getIntent(getActivity());
                    AlertDialogFragmet.show(getFragmentManager(), R.string.recentCalls_deleteFromRecentList_message, "", R.string.contact_empty_string, true, new OnDialogOptionSelectListener() {
                        public void writeToParcel(Parcel dest, int flags) {
                        }

                        public int describeContents() {
                            return 0;
                        }

                        public void onDialogOptionSelected(int which, Context aContext) {
                            if (which == -1) {
                                CallLogFragment.this.deleteCallLog(lIntent, CallLogFragment.this.mContext);
                                CallLogFragment.this.removeMissedCallNotifications();
                                StatisticalHelper.report(5013);
                                ExceptionCapture.reportScene(94);
                            }
                        }
                    }, 16843605, R.string.menu_deleteContact);
                }
            } else {
                VoicemailDeleteDialog.show(getFragmentManager(), Uri.parse(views.voicemailUri));
                if (this.mVmMenuListener != null) {
                    this.mVmMenuListener.onVoicemailMenuDeleted(views);
                }
            }
        }
    }

    private static void setOriginMode(int currentMode) {
        originMode = currentMode;
    }

    private void sendSmsMessage(String number) {
        Activity activity = getActivity();
        if (activity == null || activity.checkSelfPermission("android.permission.READ_SMS") == 0) {
            try {
                getActivity().startActivity(new Intent("android.intent.action.SENDTO", Uri.fromParts("smsto", CommonUtilMethods.deleteIPHead(number), null)));
                StatisticalHelper.report(5009);
                ExceptionCapture.reportScene(91);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this.mContext, R.string.quickcontact_missing_app_Toast, 0).show();
            }
            return;
        }
        requestPermissions(new String[]{"android.permission.READ_SMS"}, 3);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (isAdded()) {
            switch (requestCode) {
                case 3:
                    if (permissions != null && permissions.length > 0) {
                        for (int i = 0; i < permissions.length; i++) {
                            if (grantResults[i] != 0) {
                                try {
                                    startActivity(RequestPermissionsActivityBase.createRequestPermissionIntent(permissions, getContext().getPackageName()));
                                } catch (Exception e) {
                                    HwLog.e("CallLogFragment", "Activity not find!");
                                }
                                return;
                            }
                        }
                        break;
                    }
            }
        }
    }

    public Context getContext() {
        Activity act = getActivity();
        if (act != null) {
            return act;
        }
        return ContactsApplication.getContext();
    }
}
