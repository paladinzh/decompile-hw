package com.android.contacts.calllog;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.contacts.CallUtil;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.PhoneCallDetailsHelper;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactInfoFragment;
import com.android.contacts.calllog.CallLogDetailHistoryAdapter.DetailView;
import com.android.contacts.compatibility.ExtendedSubscriptionCursor;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.detail.ContactDetailFragment;
import com.android.contacts.fragment.HwBaseFragment;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.calllog.CallRecord.CallRecordItem;
import com.android.contacts.hap.numbermark.NumberMarkUtil;
import com.android.contacts.hap.rcs.RcsCLIRBroadCastHelper;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.RcsMediaplayer;
import com.android.contacts.hap.rcs.detail.RcsCallLogDetailHelper;
import com.android.contacts.hap.rcs.dialer.RcsCallLogDetailFramentHelper;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimStateListener;
import com.android.contacts.hap.util.AlertDialogFragmet;
import com.android.contacts.hap.util.AlertDialogFragmet.DialogClickListener;
import com.android.contacts.hap.util.AlertDialogFragmet.OnDialogOptionSelectListener;
import com.android.contacts.hap.util.BackgroundViewCacher;
import com.android.contacts.hap.utils.ScreenUtils;
import com.android.contacts.hap.utils.VoLteStatusObserver;
import com.android.contacts.hap.utils.VoLteStatusObserver.CallBack;
import com.android.contacts.hap.utils.VtLteUtils;
import com.android.contacts.hap.widget.MultiShrinkScroller;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AsyncTaskExecutor;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.dialer.greeting.presenter.PlaybackPresenter;
import com.android.dialer.util.OnVoicemailMenuDeletedListener;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CallLogDetailFragment extends HwBaseFragment implements SimStateListener, CallBack {
    private CallLogDetailHelper callLogDetailHelper;
    private ListView callLogList = null;
    private DialogClickListener delRecordClickListener = new DialogClickListener() {
        public void onClick(DialogInterface dialog, int which, boolean ischecked, int position) {
            if (which == -1) {
                if (position == -1) {
                    CallLogDetailFragment.this.onMenuRemoveFromCallLog(Boolean.valueOf(ischecked));
                    StatisticalHelper.report(1121);
                    CallLogDetailFragment.this.onVoicemailMenuDeleted();
                } else {
                    CallLogDetailFragment.this.deleteSingleCalllog(position, ischecked);
                    StatisticalHelper.report(1120);
                }
            }
        }
    };
    private boolean deleteSingleCalllog;
    private long deletedId = 0;
    private boolean detailsTaskIsCancle;
    private boolean isDetach = false;
    private Activity mActivity;
    private CallLogDetailHistoryAdapter mAdapter = null;
    private AsyncTaskExecutor mAsyncTaskExecutor;
    private AsyncTaskExecutor mAsyncTaskExecutorDiscardOldestPolic = null;
    PhoneCallDetails[] mCallDetails = null;
    private CallLogTableListener mCallLogChangeObserver = null;
    private long[] mCallLogIds;
    private CallTypeHelper mCallTypeHelper;
    private int mCalllogCursorSize;
    private CopyOnWriteArrayList<Long> mCalllogidList = new CopyOnWriteArrayList();
    private CalllogsUpdateCallBack mCalllogsUpdateListener;
    private HwCustCallLogDetailFragment mCust;
    private boolean mDeleteCallLog;
    private View mDurationNoticesView = null;
    private TextView mEmptyView = null;
    private View mEspaceView = null;
    private PhoneCallDetails mFirstCallDetail;
    private Fragment mFragment;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (CallLogDetailFragment.this.isAdded()) {
                switch (msg.what) {
                    case 1001:
                        if (CallLogDetailFragment.this.mAdapter != null) {
                            CallLogDetailFragment.this.mAdapter.setPhoneCallDetails(CallLogDetailFragment.this.mCallDetails);
                        }
                        if (HwLog.HWDBG) {
                            HwLog.d("CallLogDetailFragment", "MSG_NOTIFY_LISTVIEW");
                            break;
                        }
                        break;
                    case 1002:
                        if (msg.obj instanceof PhoneCallDetails) {
                            PhoneCallDetails detail = msg.obj;
                            CallLogDetailFragment.this.parseFirstDetailItem(detail);
                            if (CallLogDetailFragment.this.mUpdateCalllogsListener != null) {
                                CallLogDetailFragment.this.mUpdateCalllogsListener.onUpdateCompleted(detail);
                                break;
                            }
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        }
    };
    private boolean mHeadersAdded = false;
    private boolean mIsFirstSimEnabled;
    private boolean mIsFromDialer = false;
    private boolean mIsNeedShowDetailEntry = false;
    private boolean mIsSecondSimEnabled;
    private boolean mIsViaOnCreate = false;
    private boolean mIsVtLteOn = false;
    private PlaybackPresenter mMediaPlayback;
    private boolean mMultiSimEnable;
    private String mName;
    private View mNoNameDetailEntry = null;
    private final ArrayList<String> mNumbers = new ArrayList();
    private final HashMap<String, String> mNumbersCountryIso = new HashMap();
    private final Object mObject = new Object();
    private PhoneCallDetailsHelper mPhoneCallDetailsHelper;
    private PhoneNumberHelper mPhoneNumberHelper;
    private CharSequence mPhoneNumberLabelToCopy;
    private CharSequence mPhoneNumberToCopy;
    private int mPosition;
    private int mPresentation;
    private String mPrimaryPhoneNumber;
    private RcsCallLogDetailFramentHelper mRcs = null;
    private RcsCLIRBroadCastHelper mRcsCLIRBroadCastHelper;
    private Resources mResources;
    private int mSimPresence = 0;
    private UpdateCalllogsCallBack mUpdateCalllogsListener;
    private BackgroundViewCacher mViewInflator;
    private OnVoicemailMenuDeletedListener<Integer> mVmMenuListener;
    private VoLteStatusObserver mVoLteStatusObserver;
    private VoicemailPlaybackPresenter mVoicemailPlaybackPresenter;
    private boolean mloadingCallLog;
    private UpdateContactDetailsTask updateContactDetailsTask;

    public interface CalllogsUpdateCallBack {
        void onUpdateFinished(boolean z);
    }

    public interface UpdateCalllogsCallBack {
        void onUpdateCompleted(PhoneCallDetails phoneCallDetails);
    }

    private class CallLogTableListener extends ContentObserver {
        public CallLogTableListener(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            if (HwLog.HWDBG) {
                HwLog.d("CallLogDetailFragment", "Call log table is changed so re querying for the data.");
            }
            if (CallLogDetailFragment.this.deleteSingleCalllog && CallLogDetailFragment.this.mCalllogidList != null) {
                if (CallLogDetailFragment.this.mCalllogidList.size() <= 1) {
                }
                CallLogDetailFragment.this.deleteSingleCalllog = false;
            }
            CallLogDetailFragment.this.cleanCallLogids();
            CallLogDetailFragment.this.updateCallLog();
            CallLogDetailFragment.this.deleteSingleCalllog = false;
        }
    }

    class CallPlusOnMenuItemClickListener implements OnMenuItemClickListener {
        AdapterContextMenuInfo contextMenuInfo;

        public CallPlusOnMenuItemClickListener(AdapterContextMenuInfo cntMenuInfo) {
            this.contextMenuInfo = cntMenuInfo;
        }

        public boolean onMenuItemClick(MenuItem item) {
            if (CallLogDetailFragment.this.mAdapter == null || CallLogDetailFragment.this.mAdapter.getCount() <= this.contextMenuInfo.position - CallLogDetailFragment.this.callLogList.getHeaderViewsCount()) {
                return false;
            }
            PhoneCallDetails phoneCallDetail = CallLogDetailFragment.this.mAdapter.getItem(this.contextMenuInfo.position - CallLogDetailFragment.this.callLogList.getHeaderViewsCount());
            if (phoneCallDetail == null) {
                return false;
            }
            StatisticalHelper.report(1221);
            if (CallLogDetailFragment.this.mRcsCLIRBroadCastHelper.isCLIROpen()) {
                CallLogDetailFragment.this.mRcsCLIRBroadCastHelper.showDialog(CallLogDetailFragment.this.mFragment.getActivity());
            } else {
                RcsContactsUtils.startPreCallActivity(CallLogDetailFragment.this.mActivity, phoneCallDetail);
            }
            return true;
        }
    }

    public enum Tasks {
        MARK_VOICEMAIL_READ,
        DELETE_VOICEMAIL_AND_FINISH,
        REMOVE_FROM_CALL_LOG_AND_FINISH,
        UPDATE_PHONE_CALL_DETAILS
    }

    class UpdateContactDetailsTask extends AsyncTask<Void, Void, PhoneCallDetails[]> {
        private long[] callUris;
        boolean ifFromCommonCallLog = false;

        public UpdateContactDetailsTask(long[] callUris) {
            this.callUris = callUris;
        }

        public PhoneCallDetails[] doInBackground(Void... params) {
            PhoneCallDetails[] phoneCallDetailsArr;
            if (HwLog.HWFLOW) {
                HwLog.i("CallLogDetailFragment", "UpdateContactDetailsTask.doInBackground,callUris=null?" + (this.callUris == null) + "mNumbers count=" + (CallLogDetailFragment.this.mNumbers == null ? -1 : CallLogDetailFragment.this.mNumbers.size()));
            }
            if (CallLogDetailFragment.this.mActivity == null) {
                return new PhoneCallDetails[0];
            }
            if (this.callUris == null) {
                if (CallLogDetailFragment.this.mNumbers == null || CallLogDetailFragment.this.mNumbers.isEmpty()) {
                    if (TextUtils.isEmpty(CallLogDetailFragment.this.getmName()) && CallLogDetailFragment.this.mIsFromDialer) {
                        this.ifFromCommonCallLog = false;
                        this.callUris = CallLogDetailFragment.this.getCallLogIdsForUnknowNumber();
                    }
                    CallLogDetailFragment.this.mCallLogIds = this.callUris;
                    if (this.callUris == null || this.callUris.length == 0) {
                        return new PhoneCallDetails[0];
                    }
                }
                PhoneCallDetails[] retValue = CallLogDetailFragment.this.getCallLogForNumber();
                this.ifFromCommonCallLog = true;
                return retValue;
            }
            StringBuilder callIds = new StringBuilder();
            for (long callId : this.callUris) {
                if (callIds.length() != 0) {
                    callIds.append(",");
                }
                callIds.append(callId);
            }
            ContentResolver resolver = CallLogDetailFragment.this.mActivity.getContentResolver();
            if (CallLogDetailFragment.this.callLogDetailHelper == null) {
                return new PhoneCallDetails[0];
            }
            Cursor callCursor = resolver.query(QueryUtil.getCallsContentUri(), CallLogDetailHelper.getCallLogProjection(), "_id IN (" + callIds + ")", null, "date DESC");
            if (callCursor == null) {
                return new PhoneCallDetails[0];
            }
            int numCalls = callCursor.getCount();
            if (!QueryUtil.isContainColumn(callCursor.getColumnNames(), "subscription")) {
                callCursor = new ExtendedSubscriptionCursor(callCursor);
            }
            ArrayList<PhoneCallDetails> details = new ArrayList(numCalls);
            while (callCursor.moveToNext()) {
                try {
                    details.add(CallLogDetailFragment.this.callLogDetailHelper.getPhoneCallDetailsForUri(callCursor, CallLogDetailFragment.this.mPhoneNumberHelper));
                } catch (IllegalArgumentException e) {
                    HwLog.w("CallLogDetailFragment", "invalid URI starting call details", e);
                    phoneCallDetailsArr = new PhoneCallDetails[0];
                    return phoneCallDetailsArr;
                } finally {
                    callCursor.close();
                }
            }
            phoneCallDetailsArr = CallLogDetailFragment.this.callLogDetailHelper;
            phoneCallDetailsArr.clearNumberInfoCache();
            PhoneCallDetails[] phoneCallDetailsArray = new PhoneCallDetails[numCalls];
            details.toArray(phoneCallDetailsArray);
            return phoneCallDetailsArray;
        }

        public void onPostExecute(PhoneCallDetails[] details) {
            if (HwLog.HWFLOW) {
                HwLog.i("CallLogDetailFragment", "UpdateContactDetailsTask.onPostExecute,details size=" + (details == null ? -1 : details.length));
            }
            if (CallLogDetailFragment.this.isAdded()) {
                try {
                    if (CallLogDetailFragment.this.mFragment == null || !(CallLogDetailFragment.this.mFragment instanceof ContactInfoFragment)) {
                        if (CallLogDetailFragment.this.mUpdateCalllogsListener != null) {
                            CallLogDetailFragment.this.mUpdateCalllogsListener.onUpdateCompleted(CallLogDetailFragment.this.mFirstCallDetail);
                        }
                        return;
                    }
                    ContactInfoFragment lActivity = (ContactInfoFragment) CallLogDetailFragment.this.mFragment;
                    if (this.ifFromCommonCallLog) {
                        CallLogDetailFragment.this.mCallDetails = details;
                        CallLogDetailFragment.this.mHandler.sendEmptyMessage(1001);
                    }
                    if (!(lActivity.getContactNewDetailFragment() == null || !TextUtils.isEmpty(lActivity.getContactNewDetailFragment().getPrimaryNumber()) || lActivity.getContactNewDetailFragment().isHasManualPrimaryPhoneEntry())) {
                        if (CallLogDetailFragment.this.mNumbers.size() > 1) {
                            CallLogDetailFragment.this.markDefaultNumber(details);
                        }
                    }
                    if (this.ifFromCommonCallLog) {
                        boolean noData;
                        if (!(details == null || details.length == 0)) {
                            if (details[0] != null) {
                                noData = false;
                                if (noData) {
                                    CallLogDetailFragment.this.mFirstCallDetail = null;
                                }
                                CallLogDetailFragment.this.onDataResult(noData);
                                return;
                            }
                        }
                        noData = true;
                        if (noData) {
                            CallLogDetailFragment.this.mFirstCallDetail = null;
                        }
                        CallLogDetailFragment.this.onDataResult(noData);
                        return;
                    }
                    if (!(details == null || details.length == 0)) {
                        if (details[0] != null) {
                            if (CallLogDetailFragment.this.mActivity.isFinishing()) {
                                if (CallLogDetailFragment.this.mUpdateCalllogsListener != null) {
                                    CallLogDetailFragment.this.mUpdateCalllogsListener.onUpdateCompleted(CallLogDetailFragment.this.mFirstCallDetail);
                                }
                                return;
                            } else if (((ContactInfoFragment) CallLogDetailFragment.this.mFragment).isUnKnownNumberCall()) {
                                CallLogDetailFragment.this.onDataResult(true);
                                if (CallLogDetailFragment.this.mUpdateCalllogsListener != null) {
                                    CallLogDetailFragment.this.mUpdateCalllogsListener.onUpdateCompleted(CallLogDetailFragment.this.mFirstCallDetail);
                                }
                                return;
                            } else {
                                CallLogDetailFragment.this.onDataResult(false);
                                CallLogDetailFragment.this.parseFirstDetailItem(details[0]);
                                CallLogDetailFragment.this.setCallLogAdapter();
                                if (CallLogDetailFragment.this.mUpdateCalllogsListener != null) {
                                    CallLogDetailFragment.this.mUpdateCalllogsListener.onUpdateCompleted(CallLogDetailFragment.this.mFirstCallDetail);
                                }
                                return;
                            }
                        }
                    }
                    CallLogDetailFragment.this.mFirstCallDetail = null;
                    CallLogDetailFragment.this.onDataResult(true);
                    if (CallLogDetailFragment.this.mUpdateCalllogsListener != null) {
                        CallLogDetailFragment.this.mUpdateCalllogsListener.onUpdateCompleted(CallLogDetailFragment.this.mFirstCallDetail);
                    }
                } finally {
                    if (CallLogDetailFragment.this.mUpdateCalllogsListener != null) {
                        CallLogDetailFragment.this.mUpdateCalllogsListener.onUpdateCompleted(CallLogDetailFragment.this.mFirstCallDetail);
                    }
                }
            }
        }
    }

    public ListView getCallLogList() {
        return this.callLogList;
    }

    public void onVoicemailMenuDeleted() {
        if (this.mVoicemailPlaybackPresenter != null) {
            this.mVoicemailPlaybackPresenter.pausePlayback();
        }
    }

    public RcsCLIRBroadCastHelper getRcsCLIRBroadCastHelper() {
        return this.mRcsCLIRBroadCastHelper;
    }

    public CallLogDetailHistoryAdapter getAdapter() {
        return this.mAdapter;
    }

    public boolean ismIsFirstSimEnabled() {
        return this.mIsFirstSimEnabled;
    }

    public boolean ismIsSecondSimEnabled() {
        return this.mIsSecondSimEnabled;
    }

    public int getDefaultSimcard() {
        return SimFactoryManager.getDefaultSimcard();
    }

    public void setCalllogsUpdateListener(CalllogsUpdateCallBack mUpdateCalllogsListener) {
        this.mCalllogsUpdateListener = mUpdateCalllogsListener;
    }

    public CalllogsUpdateCallBack getCalllogsUpdateListener() {
        return this.mCalllogsUpdateListener;
    }

    public void setUpdateCalllogsListener(UpdateCalllogsCallBack mUpdateCalllogsListener) {
        this.mUpdateCalllogsListener = mUpdateCalllogsListener;
    }

    public CallLogDetailFragment() {
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustCallLogDetailFragment) HwCustUtils.createObj(HwCustCallLogDetailFragment.class, new Object[0]);
        }
    }

    public static CallLogDetailFragment newInstance(Activity activity) {
        CallLogDetailFragment fragment = new CallLogDetailFragment();
        fragment.setActivity(activity);
        return fragment;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (HwLog.HWFLOW) {
            HwLog.i("CallLogDetailFragment", "onActivityCreated");
        }
        AlertDialogFragmet alertFragment = (AlertDialogFragmet) getFragmentManager().findFragmentByTag("AlertDialogFragmet");
        if (alertFragment != null) {
            if (HwLog.HWFLOW) {
                HwLog.i("CallLogDetailFragment", "getFragmentManager mAlertDialogType :" + alertFragment.mAlertDialogType);
            }
            if (alertFragment.mAlertDialogType == 2) {
                if (this.mFragment instanceof ContactInfoFragment) {
                    alertFragment.mEarseContactsListener = ((ContactInfoFragment) this.mFragment).getEraseContactMarkClickListener();
                }
            } else if (alertFragment.mAlertDialogType == 1) {
                alertFragment.mDelListener = this.delRecordClickListener;
            }
        }
    }

    public void onAttach(Activity activity) {
        this.mViewInflator = BackgroundViewCacher.getInstance(activity.getApplicationContext());
        super.onAttach(activity);
    }

    public void setFragment(Fragment fragment) {
        this.mFragment = fragment;
    }

    public Fragment getFragment() {
        return this.mFragment;
    }

    private void setActivity(Activity activity) {
        if (HwLog.HWDBG) {
            HwLog.d("CallLogDetailFragment", "setActivity");
        }
        this.mActivity = activity;
        this.mAsyncTaskExecutor = AsyncTaskExecutors.createThreadPoolExecutor();
        this.mAsyncTaskExecutorDiscardOldestPolic = AsyncTaskExecutors.createThreadPoolExecutorDiscardOldestPolicy();
        this.mCallLogChangeObserver = new CallLogTableListener(new Handler());
        this.mPhoneCallDetailsHelper = new PhoneCallDetailsHelper(this.mActivity.getApplicationContext(), this.mCallTypeHelper, this.mPhoneNumberHelper);
        this.callLogDetailHelper = CallLogDetailHelper.getInstance(this.mActivity);
        this.mResources = this.mActivity.getResources();
        this.mCallTypeHelper = new CallTypeHelper(this.mResources);
        this.mPhoneNumberHelper = new PhoneNumberHelper(this.mResources);
    }

    public void onDetach() {
        super.onDetach();
        this.isDetach = true;
        RcsMediaplayer.getInstance().stop();
    }

    public void onCreate(Bundle savedInstanceState) {
        if (HwLog.HWDBG) {
            HwLog.d("CallLogDetailFragment", "onCreate");
        }
        super.onCreate(savedInstanceState);
        this.mIsViaOnCreate = true;
        setActivity(getActivity());
        if (this.mActivity != null) {
            getIntentData(savedInstanceState);
            this.mActivity.getApplicationContext().getContentResolver().registerContentObserver(QueryUtil.getCallsContentUri(), true, this.mCallLogChangeObserver);
        }
        if (savedInstanceState != null) {
            this.mIsFirstSimEnabled = savedInstanceState.getBoolean("ISFIRSTSIM_ENABLED", false);
            this.mIsSecondSimEnabled = savedInstanceState.getBoolean("ISSECONDSIM_ENABLED", false);
            this.mIsNeedShowDetailEntry = savedInstanceState.getBoolean("isNoNamePhoneNumber");
        }
        this.mMultiSimEnable = SimFactoryManager.isDualSim();
        if (this.mMultiSimEnable) {
            SimFactoryManager.addSimStateListener(this);
            updateSimPresentFlag();
        }
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            this.mVoicemailPlaybackPresenter = new VoicemailPlaybackPresenter(this.mActivity);
            this.mVoicemailPlaybackPresenter.init(this.mActivity, savedInstanceState);
        } else {
            this.mVoicemailPlaybackPresenter = VoicemailPlaybackPresenter.getInstance2(this.mActivity, savedInstanceState);
        }
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcs = new RcsCallLogDetailFramentHelper();
            if (this.mFragment instanceof ContactInfoFragment) {
                this.mIsNeedShowDetailEntry = ((ContactInfoFragment) this.mFragment).needShowDetailEntry();
            }
            this.mMediaPlayback = PlaybackPresenter.getInstance(this.mActivity);
            this.mRcsCLIRBroadCastHelper = RcsCLIRBroadCastHelper.getInstance(this.mActivity);
        }
        if (this.mRcs != null) {
            this.mRcs.handleCustomizationsOnCreate(this);
            if (this.mIsNeedShowDetailEntry) {
                this.mRcs.sendRcsQuestCapability(this.mPrimaryPhoneNumber);
            }
        }
        this.mVoLteStatusObserver = new VoLteStatusObserver(getContext(), this);
    }

    public void updateItemsStatus() {
        if (this.mNoNameDetailEntry != null && (this.mNoNameDetailEntry.getTag() instanceof DetailView)) {
            DetailView detailView = (DetailView) this.mNoNameDetailEntry.getTag();
            if (detailView.mVideoAction != null && VtLteUtils.isVtLteOn(getContext())) {
                detailView.mVideoAction.setEnabled(VtLteUtils.isLteServiceAbility());
            }
        }
    }

    private void addPhoneNumber(String phoneNum) {
        if (this.mNumbers != null && !TextUtils.isEmpty(phoneNum) && !this.mNumbers.contains(phoneNum)) {
            this.mNumbers.add(phoneNum);
        }
    }

    private void getIntentData(Bundle icicle) {
        if (this.mActivity != null) {
            if (icicle != null) {
                this.mCallLogIds = icicle.getLongArray("EXTRA_CALL_LOG_IDS");
                this.mPrimaryPhoneNumber = icicle.getString("EXTRA_DATA_FROM_NOTIFICATION");
                addPhoneNumber(this.mPrimaryPhoneNumber);
                this.mPresentation = icicle.getInt("EXTRA_CALL_LOG_PRESENTATION");
            } else {
                this.mPresentation = ((ContactInfoFragment) this.mFragment).getIntent().getIntExtra("EXTRA_CALL_LOG_PRESENTATION", 1);
                this.mPrimaryPhoneNumber = ((ContactInfoFragment) this.mFragment).getIntent().getStringExtra("EXTRA_CALL_LOG_NUMBER");
                if (((ContactInfoFragment) this.mFragment).getIntent().getData() == null) {
                    addPhoneNumber(this.mPrimaryPhoneNumber);
                }
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (HwLog.HWDBG) {
            HwLog.d("CallLogDetailFragment", "onCreateView");
        }
        if (this.mFragment == null) {
            return null;
        }
        Context applicationContext;
        this.mAdapter = new CallLogDetailHistoryAdapter(this.mFragment, inflater, this.mCallTypeHelper, new PhoneCallDetails[0], this.mVoicemailPlaybackPresenter, this.mMediaPlayback);
        this.mVmMenuListener = this.mAdapter;
        this.mAdapter.setCallLogDetailFragment(this);
        this.mAdapter.setViewInflator(this.mViewInflator);
        if (getActivity() != null) {
            applicationContext = getActivity().getApplicationContext();
        } else {
            applicationContext = null;
        }
        this.mIsVtLteOn = VtLteUtils.isVtLteOn(applicationContext);
        View view = this.mViewInflator.getViewFromCache(R.layout.call_log_detail_fragment);
        this.callLogList = (ListView) view.findViewById(R.id.calllog_history);
        setCallLogAdapter();
        if (this.callLogList != null) {
            CommonUtilMethods.addFootEmptyViewPortrait(this.callLogList, getContext());
        }
        this.mEmptyView = (TextView) view.findViewById(R.id.empty_history);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            ScreenUtils.updateViewTopMarrgin(getActivity(), this.mEmptyView, R.dimen.split_detail_container_margin_top);
        }
        if (EmuiFeatureManager.isDetailHeaderAnimationFeatureEnable(getActivity())) {
            MultiShrinkScroller aScroller = (MultiShrinkScroller) this.mFragment.getView().findViewById(R.id.multiscroller);
            if (aScroller != null) {
                aScroller.initCallLogView(this);
            }
        }
        if (this.mCallDetails != null) {
            this.mAdapter.setPhoneCallDetails(this.mCallDetails);
            setupCallRecords();
        }
        if (HwLog.HWDBG) {
            HwLog.d("CallLogDetailFragment", "onCreateView end.");
        }
        return view;
    }

    private void setCallLogHeader(final Context context, LayoutInflater inflater) {
        if (this.mHeadersAdded) {
            if (this.mNoNameDetailEntry != null) {
                DetailView viewCache = (DetailView) this.mNoNameDetailEntry.getTag();
                if (!(!SimFactoryManager.isBothSimEnabled() || viewCache.mSimCard == null || viewCache.mDetailFragment == null)) {
                    boolean isFirstSimEnable = viewCache.mDetailFragment.ismIsFirstSimEnabled();
                    boolean isSecondSimEnable = viewCache.mDetailFragment.ismIsSecondSimEnabled();
                    if ((isFirstSimEnable || isSecondSimEnable) && !(isFirstSimEnable && isSecondSimEnable)) {
                        viewCache.mSimCard.setText(isFirstSimEnable ? R.string.detail_sim_card1_number : R.string.detail_sim_card2_number);
                    } else if (viewCache.mDetailFragment.getDefaultSimcard() != -1 && isFirstSimEnable && isSecondSimEnable) {
                        viewCache.mSimCard.setText(String.valueOf(viewCache.mDetailFragment.getDefaultSimcard() + 1));
                    } else if (viewCache.mDetailFragment.getDefaultSimcard() == -1 && isFirstSimEnable && isSecondSimEnable) {
                        viewCache.mSimCard.setText("");
                    }
                }
            }
            return;
        }
        if (PhoneCapabilityTester.isCallDurationHid() && CallLogDetailHistoryAdapter.getDurationShowNotice(context)) {
            if (this.mDurationNoticesView == null) {
                this.mDurationNoticesView = inflater.inflate(R.layout.call_detail_history_duration_notices, this.callLogList, false);
            }
            ((Button) this.mDurationNoticesView.findViewById(R.id.contact_show_notice_button)).setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    CallLogDetailHistoryAdapter.setDurationShowNotice(context, false);
                    CallLogDetailFragment.this.callLogList.removeHeaderView(CallLogDetailFragment.this.mDurationNoticesView);
                }
            });
            if (this.mAdapter != null && this.mAdapter.isNeedShowDetailEntry) {
                View contact_notice = this.mDurationNoticesView.findViewById(R.id.contact_show_notice_textview);
                LayoutParams params = (LayoutParams) contact_notice.getLayoutParams();
                params.setMarginStart(this.mAdapter.mDetailLabelLeft);
                contact_notice.setLayoutParams(params);
            }
            this.callLogList.addHeaderView(this.mDurationNoticesView);
        }
        if (this.mAdapter != null) {
            if (this.mAdapter.isNeedShowDetailEntry) {
                this.mNoNameDetailEntry = this.mAdapter.getNoNameDetailEntryView(null);
                this.callLogList.addHeaderView(this.mNoNameDetailEntry);
            }
            if (this.mAdapter.mNeedShowEspaceEntry) {
                this.mEspaceView = this.mAdapter.getNoNameEspaceEntryView(null);
                this.callLogList.addHeaderView(this.mEspaceView);
            }
        }
        this.mHeadersAdded = true;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.callLogList.setOnCreateContextMenuListener(this);
        this.mAdapter.onRestoreInstanceState(savedInstanceState);
    }

    public void onDestroy() {
        if (HwLog.HWDBG) {
            HwLog.d("CallLogDetailFragment", "onDestroy");
        }
        if (this.mVoicemailPlaybackPresenter != null) {
            this.mVoicemailPlaybackPresenter.onDestroy(getActivity());
        }
        if (!(getActivity() == null || this.mCallLogChangeObserver == null)) {
            try {
                getActivity().getApplicationContext().getContentResolver().unregisterContentObserver(this.mCallLogChangeObserver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mCallLogChangeObserver = null;
        CallLogDetailHelper.release();
        if (this.mMultiSimEnable) {
            SimFactoryManager.removeSimStateListener(this);
        }
        if (this.mRcs != null) {
            this.mRcs.handleCustomizationsOnDestroy(this.mActivity);
            if (this.mMediaPlayback != null) {
                this.mMediaPlayback.onDestroy();
            }
            if (this.mAdapter != null) {
                this.mAdapter.release();
                this.mAdapter.clearLruCache();
            }
        }
        super.onDestroy();
    }

    public void onPause() {
        if (HwLog.HWDBG) {
            HwLog.d("CallLogDetailFragment", "onPause");
        }
        this.mVoLteStatusObserver.unregisterObserver();
        if (this.mVoicemailPlaybackPresenter != null) {
            this.mVoicemailPlaybackPresenter.onPause(getActivity());
        }
        this.mIsViaOnCreate = false;
        if (!(this.mRcs == null || this.mMediaPlayback == null)) {
            this.mMediaPlayback.onPause();
        }
        super.onPause();
    }

    public void onResume() {
        if (HwLog.HWDBG) {
            HwLog.d("CallLogDetailFragment", "onResume");
        }
        this.mVoLteStatusObserver.registerObserver();
        if (this.mVoicemailPlaybackPresenter != null) {
            this.mVoicemailPlaybackPresenter.onResume();
        }
        if (this.mPhoneCallDetailsHelper != null) {
            this.mPhoneCallDetailsHelper.resetTimeFormats();
            this.mPhoneCallDetailsHelper.updateCustSetting();
        }
        if (!(this.mIsViaOnCreate || this.mAdapter == null)) {
            this.mAdapter.notifyDataSetChanged();
        }
        if (this.mFragment != null && (this.mFragment instanceof ContactInfoFragment)) {
            setmIsFromDialer(((ContactInfoFragment) this.mFragment).getIntent().getBooleanExtra("INTENT_FROM_DIALER", false));
        }
        if (this.mIsVtLteOn != VtLteUtils.isVtLteOn(getActivity().getApplicationContext())) {
            if (this.mAdapter != null) {
                this.mAdapter.notifyDataSetChanged();
            }
            this.mIsVtLteOn = VtLteUtils.isVtLteOn(getActivity().getApplicationContext());
        }
        super.onResume();
        setCallLogHeader(getContext(), LayoutInflater.from(getContext()));
        if (EmuiFeatureManager.isRcsFeatureEnable() && this.mAdapter != null) {
            this.mAdapter.asynLoadRcsCache();
        }
    }

    public void onSaveInstanceState(Bundle aOutState) {
        if (HwLog.HWDBG) {
            HwLog.d("CallLogDetailFragment", "onSaveInstanceState");
        }
        super.onSaveInstanceState(aOutState);
        aOutState.putLongArray("EXTRA_CALL_LOG_IDS", null);
        aOutState.putString("EXTRA_DATA_FROM_NOTIFICATION", this.mPrimaryPhoneNumber);
        aOutState.putInt("EXTRA_CALL_LOG_PRESENTATION", this.mPresentation);
        aOutState.putBoolean("ISFIRSTSIM_ENABLED", this.mIsFirstSimEnabled);
        aOutState.putBoolean("ISSECONDSIM_ENABLED", this.mIsSecondSimEnabled);
        aOutState.putBoolean("isNoNamePhoneNumber", this.mIsNeedShowDetailEntry);
        if (this.mAdapter != null) {
            this.mAdapter.onSaveInstanceState(aOutState);
        }
        if (this.mVoicemailPlaybackPresenter != null) {
            this.mVoicemailPlaybackPresenter.onSaveInstanceState(aOutState);
        }
    }

    public void setmIsFromDialer(boolean mIsFromDialer) {
        this.mIsFromDialer = mIsFromDialer;
    }

    public void updateCallLogs(ArrayList<String> phoneNums, HashMap<String, String> isoHm) {
        this.mNumbersCountryIso.clear();
        this.mNumbersCountryIso.putAll(isoHm);
        this.mNumbers.clear();
        this.mCallLogIds = null;
        this.mNumbers.addAll(phoneNums);
        updateCallLog();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final AdapterContextMenuInfo contextMenuInfo = (AdapterContextMenuInfo) menuInfo;
        if (this.mAdapter != null && this.mAdapter.getCount() + this.callLogList.getHeaderViewsCount() > contextMenuInfo.position && !(contextMenuInfo.targetView.getTag() instanceof DetailView)) {
            setContextMenuHeader(contextMenuInfo, menu);
            if (v.getId() == R.id.call_and_sms_main_action) {
                menu.add(getString(R.string.copy_text)).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        CallLogDetailFragment.this.copyToClipboard(contextMenuInfo);
                        return true;
                    }
                });
            } else {
                final PhoneCallDetails callDetail = this.mAdapter.getItem(contextMenuInfo.position - this.callLogList.getHeaderViewsCount());
                if (callDetail != null) {
                    if (!(this.mRcs == null || RcsContactsUtils.isBBVersion())) {
                        MenuItem callItem = menu.add(R.string.menu_call_to_precall);
                        String phoneNumber = callDetail.number.toString();
                        this.mRcs.setRcsCallView(callItem, phoneNumber, this.mIsNeedShowDetailEntry);
                        if (this.mIsNeedShowDetailEntry) {
                            this.mRcs.sendRcsQuestCapability(phoneNumber);
                        } else {
                            this.mRcs.sendPreCallCap(phoneNumber);
                        }
                        onContextMenuCall(contextMenuInfo, callItem);
                    }
                    menu.add(R.string.str_delete_call_log_entry).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            boolean hasRecord = CallLogDetailFragment.this.singleCallLogHasRecord(contextMenuInfo.position - CallLogDetailFragment.this.callLogList.getHeaderViewsCount());
                            if (callDetail.callTypes[0] == 4) {
                                CallLogDetailFragment.this.delVoicemailCalllog(hasRecord, contextMenuInfo.position - CallLogDetailFragment.this.callLogList.getHeaderViewsCount());
                            } else {
                                CallLogDetailFragment.this.delCalllog(R.string.recentCalls_deleteFromRecentList_message, hasRecord, contextMenuInfo.position - CallLogDetailFragment.this.callLogList.getHeaderViewsCount());
                            }
                            return true;
                        }
                    });
                    if (this.mFragment != null && (this.mFragment instanceof ContactInfoFragment)) {
                        boolean isSipNumber = this.mPhoneNumberHelper.isSipNumber(callDetail.number);
                        if (!(((ContactInfoFragment) this.mFragment).isUnKnownNumberCall() || isSipNumber)) {
                            onContextMenuEditNumberBeforeCall(contextMenuInfo, menu.add(R.string.recentCalls_editBeforeCall));
                        }
                        if (this.mPhoneNumberHelper.isVoicemailNumber(callDetail.number) && !TextUtils.isEmpty(callDetail.getVoiceMailNumber())) {
                            onContextMenuTrashVoicemail(contextMenuInfo, menu.add(R.string.recentCalls_trashVoicemail));
                        }
                    }
                }
            }
        }
    }

    private void onContextMenuCall(AdapterContextMenuInfo contextMenuInfo, MenuItem menuItem) {
        menuItem.setOnMenuItemClickListener(new CallPlusOnMenuItemClickListener(contextMenuInfo));
    }

    private void setContextMenuHeader(AdapterContextMenuInfo contextMenuInfo, ContextMenu menu) {
        PhoneCallDetails callDetail = this.mAdapter.getItem(contextMenuInfo.position - this.callLogList.getHeaderViewsCount());
        if (callDetail != null) {
            CharSequence nameOrNumber;
            if (TextUtils.isEmpty(callDetail.name)) {
                nameOrNumber = callDetail.number;
            } else {
                nameOrNumber = callDetail.name;
            }
            menu.setHeaderIcon(R.mipmap.ic_launcher_phone);
            boolean canPlaceCallsTo = this.mPhoneNumberHelper.canPlaceCallsTo(callDetail.number, this.mPresentation);
            if (this.mPhoneNumberHelper.isVoicemailNumber(callDetail.number)) {
                menu.setHeaderTitle(R.string.voicemail);
            } else if (canPlaceCallsTo) {
                menu.setHeaderTitle(nameOrNumber);
            } else {
                CharSequence displayNumber = this.mPhoneNumberHelper.getDisplayNumber(callDetail.number, this.mPresentation, null, callDetail.postDialDigits);
                if (displayNumber != null) {
                    menu.setHeaderTitle(displayNumber.toString());
                } else {
                    menu.setHeaderTitle(nameOrNumber);
                }
            }
        }
    }

    protected boolean singleCallLogHasRecord(int position) {
        PhoneCallDetails details = this.mAdapter.getItem(position);
        if (details == null) {
            return false;
        }
        CallRecordItem[] items = details.mCallRecordItems;
        if (items == null || items.length <= 0) {
            return false;
        }
        return true;
    }

    private void markDefaultNumber(final PhoneCallDetails[] details) {
        if (HwLog.HWFLOW) {
            HwLog.i("CallLogDetailFragment", "markDefaultNumber start");
        }
        if (details != null && details.length >= 5) {
            ContactsThreadPool.getInstance().execute(new Runnable() {
                public void run() {
                    String lnumber = null;
                    int j = 0;
                    int i = 0;
                    while (i < details.length) {
                        if (details[i].callTypes != null && details[i].callTypes.length > 0 && 2 == details[i].callTypes[0]) {
                            if (lnumber == null) {
                                lnumber = details[i].number.toString();
                            }
                            if (CommonUtilMethods.compareNumsHw(lnumber, details[i].number.toString())) {
                                j++;
                                if (j == 5) {
                                    if ((CallLogDetailFragment.this.mFragment instanceof ContactInfoFragment) && ((ContactInfoFragment) CallLogDetailFragment.this.mFragment).getContactNewDetailFragment() != null) {
                                        long dataId = ((ContactInfoFragment) CallLogDetailFragment.this.mFragment).getContactNewDetailFragment().getPhoneDataIdByNumber(lnumber);
                                        ((ContactInfoFragment) CallLogDetailFragment.this.mFragment).setResetFlag(false);
                                        ContactDetailFragment.setDefaultContactMethod(CallLogDetailFragment.this.mActivity, dataId, true);
                                        if (HwLog.HWFLOW) {
                                            HwLog.i("CallLogDetailFragment", "markDefaultNumber end");
                                        }
                                    }
                                    return;
                                }
                            } else if (j >= 5) {
                                continue;
                            } else if (i + 5 <= details.length) {
                                j = 0;
                                lnumber = null;
                                if (HwLog.HWFLOW) {
                                    HwLog.i("CallLogDetailFragment", "markDefaultNumber reset");
                                }
                            } else {
                                return;
                            }
                        }
                        i++;
                    }
                }
            });
        }
    }

    private void parseFirstDetailItem(PhoneCallDetails firstDetails) {
        this.mFirstCallDetail = firstDetails;
        if (TextUtils.isEmpty(firstDetails.name)) {
            this.mName = "";
        } else {
            this.mName = firstDetails.name.toString();
        }
        if ((this.mActivity instanceof ContactDetailActivity) && !CommonUtilMethods.calcIfNeedSplitScreen()) {
            this.mActivity.invalidateOptionsMenu();
        }
        setupCallRecords();
    }

    public void setupCallRecords() {
        boolean z = false;
        if (HwLog.HWDBG) {
            String str = "CallLogDetailFragment";
            StringBuilder append = new StringBuilder().append(" mAdapter==null:");
            if (this.mAdapter == null) {
                z = true;
            }
            HwLog.d(str, append.append(z).toString());
        }
        if (this.mAdapter == null) {
            return;
        }
        if (this.mNumbers.size() > 0) {
            this.mAdapter.resetRecordList();
            ArrayList<String> numbers = new ArrayList();
            numbers.addAll(this.mNumbers);
            for (String lNumber : numbers) {
                this.mAdapter.setupCallRecords(lNumber);
            }
        } else if (TextUtils.isEmpty(getmName()) && this.mIsFromDialer) {
            this.mAdapter.resetRecordList();
            this.mAdapter.setupCallRecords("unknown");
        }
    }

    private void setCallLogAdapter() {
        if (this.callLogList != null) {
            this.mAdapter.setIsShowImage(SimFactoryManager.isBothSimEnabled());
            this.callLogList.setAdapter(this.mAdapter);
            this.callLogList.setOnScrollListener(this.mAdapter);
        }
    }

    public boolean checkAndInitCall(PhoneCallDetails details) {
        if (this.mCust == null || !this.mCust.checkAndInitCall(this.mActivity, details.number)) {
            return false;
        }
        return true;
    }

    private PhoneCallDetails[] getCallLogForNumber() {
        this.mloadingCallLog = true;
        PhoneCallDetails[] ret = null;
        if (this.mActivity == null) {
            return new PhoneCallDetails[0];
        }
        if (this.mNumbers == null || this.mNumbers.isEmpty()) {
            return new PhoneCallDetails[0];
        }
        String[] lNumbers;
        String lSinglContactSelectionArg = null;
        StringBuilder selectionBuilder = new StringBuilder();
        String lOR = " OR ";
        ArrayList<String> lList = new ArrayList();
        ArrayList<String> templList = new ArrayList();
        synchronized (this.mObject) {
            ArrayList<String> tempNumbers = new ArrayList();
            tempNumbers.addAll(this.mNumbers);
            int sizeOfNumberList = tempNumbers.size();
            String lPhoneNumberSelection = "PHONE_NUMBERS_EQUAL(number, ?)";
            String str = null;
            boolean isMatch = false;
            if (EmuiFeatureManager.isChinaArea() && ((ContactInfoFragment) this.mFragment).getIntent() != null) {
                str = ((ContactInfoFragment) this.mFragment).getIntent().getStringExtra("EXTRA_CALL_LOG_NUMBER");
                if (!TextUtils.isEmpty(str)) {
                    if (str.matches("^0\\d{2,3}[1,9]\\d{4}$")) {
                        isMatch = true;
                    }
                }
            }
            for (int j = 0; j < sizeOfNumberList; j++) {
                if (!TextUtils.isEmpty((CharSequence) tempNumbers.get(j))) {
                    String num = DatabaseUtils.sqlEscapeString((String) tempNumbers.get(j));
                    num = num.substring(1, num.length() - 1);
                    String tempNumSub = PhoneNumberHelper.getQueryCallNumber(num);
                    if (isMatch && str.contains(tempNumSub)) {
                        if (tempNumSub.matches("[1,9]\\d{4}$")) {
                            num = str;
                            str = PhoneNumberHelper.getQueryCallNumber(str);
                            tempNumSub = str;
                        }
                    }
                    if (!templList.contains(tempNumSub)) {
                        templList.add(tempNumSub);
                        selectionBuilder.append(lPhoneNumberSelection);
                        selectionBuilder.append(lOR);
                    }
                    if (!lList.contains(num)) {
                        lList.add(num);
                    }
                }
            }
            lNumbers = (String[]) templList.toArray(new String[templList.size()]);
            this.mNumbers.clear();
            this.mNumbers.addAll(lList);
        }
        if (selectionBuilder.length() > lOR.length()) {
            lSinglContactSelectionArg = selectionBuilder.delete(selectionBuilder.length() - lOR.length(), selectionBuilder.length()).toString();
        }
        Object projection = CallLogDetailHelper.getCallLogProjection();
        String[] newProjection = new String[(projection.length + 1)];
        System.arraycopy(projection, 0, newProjection, 0, projection.length);
        newProjection[newProjection.length - 1] = "_id";
        int indexCallsId = newProjection.length - 1;
        Cursor cursor = this.mActivity.getApplicationContext().getContentResolver().query(QueryUtil.getCallsContentUri(), newProjection, "(" + lSinglContactSelectionArg + ") AND deleted = 0", lNumbers, "date DESC");
        if (HwLog.HWFLOW) {
            int i;
            String str2 = "CallLogDetailFragment";
            StringBuilder append = new StringBuilder().append("getCallLogForNumber,query cursor count=");
            if (cursor == null) {
                i = -1;
            } else {
                i = cursor.getCount();
            }
            HwLog.i(str2, append.append(i).toString());
        }
        if (cursor != null) {
            if (!QueryUtil.isContainColumn(cursor.getColumnNames(), "subscription")) {
                cursor = new ExtendedSubscriptionCursor(cursor);
            }
            this.mCalllogCursorSize = cursor.getCount();
            PhoneCallDetails[] phoneCallDetailsArr;
            if (this.mCalllogCursorSize > 0) {
                ArrayList<PhoneCallDetails> arrayList = new ArrayList(this.mCalllogCursorSize);
                for (String number : lList) {
                    if (!this.mNumbersCountryIso.containsKey(number)) {
                        this.mNumbersCountryIso.put(number, CommonUtilMethods.getCountryIsoFromDbNumberHw(number));
                    }
                }
                ArrayList<String> destNumberList = new ArrayList();
                PhoneNumberHelper.getQueryCallLogNumberList(lList, this.mNumbersCountryIso, destNumberList);
                CopyOnWriteArrayList<Long> calllogidList = new CopyOnWriteArrayList();
                int index = 0;
                boolean firstBatch = true;
                while (cursor.moveToNext() && this.mCalllogCursorSize == cursor.getCount()) {
                    if (CommonConstants.IS_HW_CUSTOM_NUMBER_MATCHING_ENABLED) {
                        String cached_name = cursor.getString(11);
                        String cached_number = cursor.getString(2);
                        String cached_countryISO = cursor.getString(4);
                        for (String number2 : destNumberList) {
                            String countryIso = (String) this.mNumbersCountryIso.get(number2);
                            if (CommonUtilMethods.equalByNameOrNumber(this.mName, number2, cached_name, cached_number) && CommonUtilMethods.compareNumsHw(number2, countryIso, cached_number, cached_countryISO)) {
                                PhoneCallDetails details = this.callLogDetailHelper.getPhoneCallDetailsForUri(cursor, this.mPhoneNumberHelper);
                                arrayList.add(details);
                                long callId = cursor.getLong(indexCallsId);
                                details.mId = String.valueOf(callId);
                                details.mTranscription = cursor.getString(cursor.getColumnIndex("transcription"));
                                calllogidList.add(Long.valueOf(callId));
                                index++;
                                if (!firstBatch || index < 8) {
                                    if (index < 100) {
                                        continue;
                                    }
                                }
                                firstBatch = false;
                                if (this.mDeleteCallLog) {
                                    if (this.mPosition < arrayList.size()) {
                                        arrayList.remove(this.mPosition);
                                    }
                                    this.mDeleteCallLog = !this.mDeleteCallLog;
                                }
                                notifyListviewChanged(arrayList);
                                if (this.isDetach) {
                                    phoneCallDetailsArr = new PhoneCallDetails[0];
                                    return phoneCallDetailsArr;
                                }
                                index = 0;
                            }
                        }
                    } else {
                        try {
                            arrayList.add(this.callLogDetailHelper.getPhoneCallDetailsForUri(cursor, this.mPhoneNumberHelper));
                            calllogidList.add(Long.valueOf(cursor.getLong(indexCallsId)));
                            index++;
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                    if (index >= 8) {
                        notifyListviewChanged(arrayList);
                        if (this.isDetach) {
                            phoneCallDetailsArr = new PhoneCallDetails[0];
                            if (cursor != null) {
                                cursor.close();
                            }
                            return phoneCallDetailsArr;
                        }
                        index = 0;
                    }
                }
                this.callLogDetailHelper.clearNumberInfoCache();
                if (index > 0) {
                    notifyListviewChanged(arrayList);
                    if (this.isDetach) {
                        phoneCallDetailsArr = new PhoneCallDetails[0];
                        if (cursor != null) {
                            cursor.close();
                        }
                        return phoneCallDetailsArr;
                    }
                }
                if (arrayList.size() > 0) {
                    Message msg = Message.obtain();
                    msg.obj = arrayList.get(0);
                    msg.what = 1002;
                    this.mHandler.sendMessage(msg);
                }
                ret = new PhoneCallDetails[arrayList.size()];
                arrayList.toArray(ret);
                long[] calllogids = new long[calllogidList.size()];
                int index2 = 0;
                for (Long id : calllogidList) {
                    index = index2 + 1;
                    calllogids[index2] = id.longValue();
                    index2 = index;
                }
                this.mCalllogidList = calllogidList;
                this.mCallLogIds = calllogids;
            } else {
                phoneCallDetailsArr = new PhoneCallDetails[0];
                if (cursor != null) {
                    cursor.close();
                }
                return phoneCallDetailsArr;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        if (HwLog.HWDBG) {
            HwLog.i("CallLogDetailFragment", "getCallLogForNumber end");
        }
        this.mloadingCallLog = false;
        return ret;
    }

    private void notifyListviewChanged(ArrayList<PhoneCallDetails> lcalllogs) {
        if (lcalllogs != null && (this.mFragment instanceof ContactInfoFragment)) {
            if (lcalllogs.size() == 0 && ((ContactInfoFragment) this.mFragment).needShowDetailEntry()) {
                int size = 1;
                if (PhoneCapabilityTester.isCallDurationHid()) {
                    CallLogDetailHistoryAdapter callLogDetailHistoryAdapter = this.mAdapter;
                    if (CallLogDetailHistoryAdapter.getDurationShowNotice(getActivity().getApplicationContext())) {
                        size = 2;
                    }
                }
                if (this.mAdapter != null && this.mAdapter.mNeedShowEspaceEntry) {
                    size++;
                }
                this.mCallDetails = new PhoneCallDetails[size];
            } else {
                this.mCallDetails = new PhoneCallDetails[lcalllogs.size()];
            }
            lcalllogs.toArray(this.mCallDetails);
            if (isAdded()) {
                this.mHandler.sendEmptyMessage(1001);
            }
            if (HwLog.HWDBG) {
                HwLog.i("CallLogDetailFragment", "isDetach:" + this.isDetach);
            }
        }
    }

    private long[] getCallLogEntryUris() {
        if (this.mFragment == null) {
            return new long[0];
        }
        if (((ContactInfoFragment) this.mFragment).getIntent().getData() == null) {
            return ((ContactInfoFragment) this.mFragment).getIntent().getLongArrayExtra("EXTRA_CALL_LOG_IDS");
        }
        return new long[]{Long.parseLong(((ContactInfoFragment) this.mFragment).getIntent().getData().getLastPathSegment())};
    }

    public void updateCallLog() {
        updateData(this.mCallLogIds);
    }

    private void updateData(long[] callUris) {
        this.updateContactDetailsTask = new UpdateContactDetailsTask(callUris);
        if (this.mAsyncTaskExecutorDiscardOldestPolic == null) {
            this.mAsyncTaskExecutorDiscardOldestPolic = AsyncTaskExecutors.createThreadPoolExecutorDiscardOldestPolicy();
        }
        this.mAsyncTaskExecutorDiscardOldestPolic.submit(Tasks.UPDATE_PHONE_CALL_DETAILS, this.updateContactDetailsTask, new Void[0]);
    }

    public void cleanCallLogids() {
        if (this.mloadingCallLog) {
            this.detailsTaskIsCancle = this.updateContactDetailsTask.cancel(true);
            if (HwLog.HWDBG) {
                HwLog.d("CallLogDetailFragment", "UpdateContactDetailsTask is cancelled: " + this.detailsTaskIsCancle);
            }
        }
        this.mCallLogIds = null;
    }

    private long[] getCallLogIdsForUnknowNumber() {
        if (this.mActivity == null) {
            return new long[0];
        }
        long[] jArr = null;
        Cursor cursor = this.mActivity.getContentResolver().query(QueryUtil.getCallsContentUri(), new String[]{"_id", "presentation"}, "presentation=?", new String[]{String.valueOf(this.mPresentation)}, "date DESC");
        if (cursor != null) {
            try {
                int size = cursor.getCount();
                if (size > 0) {
                    ArrayList<Long> callLogIdsList = new ArrayList(size);
                    while (cursor.moveToNext()) {
                        callLogIdsList.add(Long.valueOf(cursor.getLong(0)));
                    }
                    jArr = new long[callLogIdsList.size()];
                    int index = 0;
                    for (Long id : callLogIdsList) {
                        int index2 = index + 1;
                        jArr[index] = id.longValue();
                        index = index2;
                    }
                    this.mCallLogIds = jArr;
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        if (jArr == null) {
            jArr = getCallLogEntryUris();
            this.mCallLogIds = jArr;
        }
        return jArr;
    }

    private void onDataResult(boolean noData) {
        int i = 0;
        if (HwLog.HWFLOW) {
            HwLog.d("CallLogDetailFragment", "onDataResult,noData=" + noData + ",callLogList=" + this.callLogList + ",mEmptyView=" + this.mEmptyView);
        }
        if (this.mFragment instanceof ContactInfoFragment) {
            if (this.callLogList != null) {
                if (noData && ((ContactInfoFragment) this.mFragment).isUnKnownNumberCall() && this.mEmptyView != null) {
                    this.mEmptyView.setText(R.string.contacts_unsupport_callLog);
                }
                if (!((ContactInfoFragment) this.mFragment).needShowDetailEntry() || ((ContactInfoFragment) this.mFragment).isUnKnownNumberCall()) {
                    int i2;
                    ListView listView = this.callLogList;
                    if (noData) {
                        i2 = 8;
                    } else {
                        i2 = 0;
                    }
                    listView.setVisibility(i2);
                    if (this.mEmptyView != null) {
                        TextView textView = this.mEmptyView;
                        if (!noData) {
                            i = 8;
                        }
                        textView.setVisibility(i);
                    }
                } else {
                    this.callLogList.setVisibility(0);
                    if (this.mEmptyView != null) {
                        this.mEmptyView.setVisibility(8);
                    }
                }
                if (this.mCalllogsUpdateListener != null) {
                    this.mCalllogsUpdateListener.onUpdateFinished(noData);
                }
            } else if (this.mEmptyView != null) {
                this.mEmptyView.setVisibility(0);
            }
        }
    }

    public void handleItemClick(View view) {
        PhoneCallDetails details = (PhoneCallDetails) view.getTag();
        if (details != null) {
            CallRecordItem[] items = details.mCallRecordItems;
            if (items != null && items.length > 0) {
                if (items.length == 1) {
                    CallLogDetailHelper.startRecordPlaybackSafely(this.mActivity, items[0].mAbsolutePath);
                } else {
                    String[] itemName = new String[items.length];
                    String[] itemPath = new String[items.length];
                    String[] time = new String[items.length];
                    String[] data = new String[items.length];
                    for (int i = 0; i < items.length; i++) {
                        itemPath[i] = items[i].mAbsolutePath;
                        int timeIdenx = itemPath[i].lastIndexOf(46);
                        int dataIndex = itemPath[i].lastIndexOf(95);
                        time[i] = itemPath[i].substring(timeIdenx - 6, timeIdenx);
                        data[i] = itemPath[i].substring(dataIndex + 1, dataIndex + 9);
                        itemName[i] = data[i] + "_" + time[i];
                    }
                    if (itemName.length >= 1) {
                        PlayCallRecordDialogfragment.playCallRecord(getFragmentManager(), itemName, itemPath);
                    }
                }
            }
        }
    }

    private void copyToClipboard(AdapterContextMenuInfo contextMenuInfo) {
        generalCopyData(this.mAdapter.getItem(contextMenuInfo.position - this.callLogList.getHeaderViewsCount()));
        CharSequence textToCopy = this.mPhoneNumberToCopy;
        if (!TextUtils.isEmpty(textToCopy)) {
            ((ClipboardManager) getActivity().getSystemService("clipboard")).setPrimaryClip(new ClipData(this.mPhoneNumberLabelToCopy, new String[]{"vnd.android.cursor.item/phone_v2"}, new Item(textToCopy)));
        }
    }

    private void deleteSingleCalllog(final int position, final boolean delete) {
        this.deleteSingleCalllog = true;
        if (this.mCallLogIds == null && this.mCalllogidList != null) {
            int index = 0;
            CopyOnWriteArrayList<Long> calllogidList = this.mCalllogidList;
            long[] calllogids = new long[calllogidList.size()];
            for (Long id : calllogidList) {
                int index2 = index + 1;
                calllogids[index] = id.longValue();
                index = index2;
            }
            this.mCallLogIds = calllogids;
        }
        if (this.mCallLogIds != null && (this.mFragment instanceof ContactInfoFragment)) {
            final long[] ids = this.mCallLogIds;
            this.mPosition = position;
            if (this.mPosition >= 0 && ids.length != 0) {
                if (this.mVmMenuListener != null) {
                    this.mVmMenuListener.onVoicemailMenuDeleted(Integer.valueOf(position));
                }
                boolean needDelRecord = delete;
                new AsyncTask<Void, Void, Void>() {
                    private int isDeleted = 0;

                    public Void doInBackground(Void... params) {
                        long id = ids[CallLogDetailFragment.this.mPosition];
                        CallLogDetailFragment.this.deletedId = id;
                        if (EmuiFeatureManager.isRcsFeatureEnable()) {
                            RcsCallLogDetailHelper.deleteRcsMapAndPicture(CallLogDetailFragment.this.mActivity, id);
                        }
                        this.isDeleted = CallLogDetailFragment.this.mActivity.getApplicationContext().getContentResolver().delete(QueryUtil.getCallsContentUri(), "_id ='" + id + "'", null);
                        if (!delete || position < 0 || position >= CallLogDetailFragment.this.mAdapter.getCount()) {
                            return null;
                        }
                        PhoneCallDetails details = CallLogDetailFragment.this.mAdapter.getItem(position);
                        if (details == null) {
                            return null;
                        }
                        CallRecordItem[] items = details.mCallRecordItems;
                        if (items == null || items.length <= 0) {
                            return null;
                        }
                        for (CallRecordItem callRecordItem : items) {
                            CallLogDetailHelper.deleteRecordSafely(callRecordItem.mAbsolutePath);
                        }
                        return null;
                    }

                    protected void onPostExecute(Void result) {
                        if (CallLogDetailFragment.this.isAdded()) {
                            CallLogDetailFragment.this.mDeleteCallLog = true;
                            if (this.isDeleted > 0) {
                                CallLogDetailFragment.this.mCalllogidList.remove(CallLogDetailFragment.this.mPosition);
                                long[] newIds = new long[(ids.length - 1)];
                                int index = 0;
                                for (int i = 0; i < ids.length; i++) {
                                    if (ids[i] != CallLogDetailFragment.this.deletedId) {
                                        newIds[index] = ids[i];
                                        index++;
                                    }
                                }
                                ((ContactInfoFragment) CallLogDetailFragment.this.mFragment).getIntent().putExtra("EXTRA_CALL_LOG_IDS", newIds);
                                CallLogDetailFragment.this.mCallLogIds = newIds;
                                if (CallLogDetailFragment.this.mAdapter != null) {
                                    CallLogDetailFragment.this.mAdapter.removeSingleCallDetail(CallLogDetailFragment.this.mPosition);
                                }
                            }
                        }
                    }
                }.execute(new Void[0]);
            }
        }
    }

    public boolean onMenuItemClicked(int id) {
        switch (id) {
            case R.id.contact_menuitem_delete_calllog:
            case R.id.menu_remove_from_call_log:
            case R.id.menu_delete_calllog:
                if (id == R.id.contact_menuitem_delete_calllog) {
                    StatisticalHelper.report(5018);
                } else {
                    StatisticalHelper.report(5020);
                }
                delCalllog(R.string.delete_callog_all_title, this.mAdapter.hasRecord(), -1);
                return true;
            case R.id.menu_mark_as:
                Intent intent = NumberMarkUtil.getIntentForMark(this.mActivity.getApplicationContext(), this.mPrimaryPhoneNumber);
                StatisticalHelper.report(5008);
                try {
                    this.mActivity.startActivityForResult(intent, 100);
                } catch (ActivityNotFoundException e) {
                    HwLog.w("CallLogDetailFragment", "Activity not found." + e);
                }
                return true;
            default:
                return false;
        }
    }

    public boolean hasCallLogRecord() {
        if (this.mAdapter != null) {
            return this.mAdapter.getCount() > 0;
        } else {
            return false;
        }
    }

    public void eraseContactDialog(OnDialogOptionSelectListener listener, boolean cancelable) {
        AlertDialogFragmet.show(getFragmentManager(), (int) R.string.contact_menu_detail_erase_mark_title, this.mActivity.getString(R.string.contact_menu_detail_erase_mark_content), (int) R.string.contact_menu_detail_erase_mark_content, cancelable, listener, 16843605, (int) R.string.contact_menu_detail_reset_mark_erase_button, 2);
    }

    public void onMenuRemoveFromCallLog() {
        onMenuRemoveFromCallLog(Boolean.valueOf(true));
    }

    public void onMenuRemoveFromCallLog(final Boolean deleteRecords) {
        final StringBuilder callIds = new StringBuilder();
        if (this.mCallLogIds != null) {
            for (long callUri : this.mCallLogIds) {
                if (callIds.length() != 0) {
                    callIds.append(",");
                }
                callIds.append(callUri);
            }
        }
        this.mAsyncTaskExecutor.submit(Tasks.REMOVE_FROM_CALL_LOG_AND_FINISH, new AsyncTask<Integer, Void, Boolean>() {
            public Boolean doInBackground(Integer... params) {
                if (CallLogDetailFragment.this.mActivity != null) {
                    if (EmuiFeatureManager.isRcsFeatureEnable()) {
                        RcsCallLogDetailHelper.deleteRcsMapAndPicture(CallLogDetailFragment.this.mActivity, callIds);
                    }
                    int num = CallLogDetailFragment.this.mActivity.getContentResolver().delete(QueryUtil.getCallsContentUri(), "_id IN (" + callIds + ")", null);
                    for (int j = 0; j < CallLogDetailFragment.this.mAdapter.getCount(); j++) {
                        PhoneCallDetails details = CallLogDetailFragment.this.mAdapter.getItem(j);
                        if (details != null && deleteRecords.booleanValue()) {
                            CallRecordItem[] items = details.mCallRecordItems;
                            if (items != null && items.length > 0) {
                                for (CallRecordItem callRecordItem : items) {
                                    CallLogDetailHelper.deleteRecordSafely(callRecordItem.mAbsolutePath);
                                }
                            }
                        }
                    }
                    if (num > 0) {
                        return Boolean.valueOf(true);
                    }
                }
                return Boolean.valueOf(false);
            }

            public void onPostExecute(Boolean result) {
                if (Boolean.TRUE.equals(result)) {
                    CallLogDetailFragment.this.mAdapter.clearCallLogs();
                    if (CallLogDetailFragment.this.mCalllogsUpdateListener != null) {
                        CallLogDetailFragment.this.mCalllogsUpdateListener.onUpdateFinished(true);
                    }
                }
            }
        }, new Integer[0]);
    }

    public void onContextMenuEditNumberBeforeCall(final AdapterContextMenuInfo contextMenuInfo, MenuItem menuItem) {
        menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (CallLogDetailFragment.this.mAdapter == null || CallLogDetailFragment.this.mAdapter.getCount() <= contextMenuInfo.position - CallLogDetailFragment.this.callLogList.getHeaderViewsCount()) {
                    return false;
                }
                PhoneCallDetails phoneCallDetail = CallLogDetailFragment.this.mAdapter.getItem(contextMenuInfo.position - CallLogDetailFragment.this.callLogList.getHeaderViewsCount());
                if (phoneCallDetail == null) {
                    return false;
                }
                Intent intent = new Intent("android.intent.action.DIAL", CallUtil.getCallUri(phoneCallDetail.number.toString()));
                intent.setPackage("com.android.contacts");
                CallLogDetailFragment.this.startActivity(intent);
                return true;
            }
        });
    }

    public void onContextMenuTrashVoicemail(final AdapterContextMenuInfo contextMenuInfo, MenuItem menuItem) {
        menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem arg0) {
                if (CallLogDetailFragment.this.mAdapter == null || CallLogDetailFragment.this.mAdapter.getCount() <= contextMenuInfo.position - CallLogDetailFragment.this.callLogList.getHeaderViewsCount()) {
                    return false;
                }
                PhoneCallDetails phoneCallDetail = CallLogDetailFragment.this.mAdapter.getItem(contextMenuInfo.position - CallLogDetailFragment.this.callLogList.getHeaderViewsCount());
                if (phoneCallDetail == null) {
                    return false;
                }
                final Uri voicemailUri = Uri.parse(phoneCallDetail.voiceMailNumber);
                CallLogDetailFragment.this.mAsyncTaskExecutor.submit(Tasks.DELETE_VOICEMAIL_AND_FINISH, new AsyncTask<Void, Void, Void>() {
                    public Void doInBackground(Void... params) {
                        if (CallLogDetailFragment.this.mActivity != null) {
                            CallLogDetailFragment.this.mActivity.getContentResolver().delete(voicemailUri, null, null);
                        }
                        return null;
                    }

                    public void onPostExecute(Void result) {
                        if (CallLogDetailFragment.this.mActivity != null) {
                            CallLogDetailFragment.this.mActivity.finish();
                        }
                    }
                }, new Void[0]);
                return true;
            }
        });
    }

    public String getmName() {
        return this.mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    private void generalCopyData(PhoneCallDetails callDetails) {
        if (this.mPhoneNumberHelper.canPlaceCallsTo(callDetails.number, this.mPresentation)) {
            CharSequence displayNumber = this.mPhoneNumberHelper.getDisplayNumber(callDetails.number, callDetails.getPresentation(), callDetails.formattedNumber, callDetails.postDialDigits);
            if (!(TextUtils.isEmpty(callDetails.number) || PhoneNumberUtils.isUriNumber(callDetails.number.toString()))) {
                int type = callDetails.numberType;
                if (type < 1 && TextUtils.isEmpty(callDetails.name)) {
                    type = 2;
                }
                if (TextUtils.isEmpty(callDetails.geocode)) {
                    if (callDetails.contactUri != null) {
                        this.mPhoneNumberLabelToCopy = Phone.getTypeLabel(this.mResources, type, callDetails.numberLabel);
                    }
                } else if (callDetails.contactUri == null) {
                    this.mPhoneNumberLabelToCopy = callDetails.geocode;
                } else {
                    this.mPhoneNumberLabelToCopy = Phone.getTypeLabel(this.mResources, type, callDetails.numberLabel) + " - " + callDetails.geocode;
                }
            }
            this.mPhoneNumberToCopy = displayNumber;
            return;
        }
        this.mPhoneNumberToCopy = null;
        this.mPhoneNumberLabelToCopy = null;
    }

    public String getLatestCallNumber() {
        if (this.mFirstCallDetail != null) {
            return String.valueOf(this.mFirstCallDetail.number);
        }
        return null;
    }

    public int getLastCallSimType(String phoneNumber) {
        if (this.mAdapter != null) {
            for (int i = 0; i < this.mAdapter.getCount(); i++) {
                PhoneCallDetails detail = this.mAdapter.getItem(i);
                if (detail != null && PhoneNumberUtils.compare(String.valueOf(detail.number), phoneNumber)) {
                    return detail.subscriptionID;
                }
            }
        }
        return -1;
    }

    private void delCalllog(int amessageId, boolean hasRecord, int position) {
        AlertDialogFragmet.show(getFragmentManager(), amessageId, "", (int) R.string.contact_empty_string, (int) R.string.contacts_delete_record_with_callLog, true, this.delRecordClickListener, 16843605, (int) R.string.menu_deleteContact, hasRecord, position, 1);
    }

    private void delVoicemailCalllog(boolean hasRecord, int position) {
        AlertDialogFragmet.show(getFragmentManager(), (int) R.string.menu_deleteContact, getString(R.string.voicemail_delete_notify), (int) R.string.voicemail_delete_notify, (int) R.string.contacts_delete_record_with_callLog, true, this.delRecordClickListener, 16843605, R.string.menu_deleteContact, hasRecord, position, 1);
    }

    public void simStateChanged(int aSubScription) {
        updateSimPresentFlag();
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    if (CallLogDetailFragment.this.callLogList != null && CallLogDetailFragment.this.mAdapter != null) {
                        CallLogDetailFragment.this.callLogList.setAdapter(CallLogDetailFragment.this.mAdapter);
                        CallLogDetailFragment.this.mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void updateSimPresentFlag() {
        int i;
        this.mIsFirstSimEnabled = SimFactoryManager.isSimEnabled(0);
        this.mIsSecondSimEnabled = SimFactoryManager.isSimEnabled(1);
        boolean isSecondSubIdle = true;
        boolean isFirstSubIdle = true;
        if (!CommonConstants.sRo_config_hw_dsda && CommonConstants.IS_SHOW_DUAL_DIALPAD) {
            isSecondSubIdle = !SimFactoryManager.phoneIsOffhook(1);
            isFirstSubIdle = !SimFactoryManager.phoneIsOffhook(0);
            if (HwLog.HWFLOW) {
                HwLog.i("CallLogDetailFragment", "isSecondSubIdle:" + isSecondSubIdle);
                HwLog.i("CallLogDetailFragment", "isFirstSubIdle:" + isFirstSubIdle);
            }
        }
        if (!this.mIsFirstSimEnabled) {
            isSecondSubIdle = false;
        }
        this.mIsFirstSimEnabled = isSecondSubIdle;
        if (!this.mIsSecondSimEnabled) {
            isFirstSubIdle = false;
        }
        this.mIsSecondSimEnabled = isFirstSubIdle;
        if (SimFactoryManager.isSIM1CardPresent()) {
            i = this.mSimPresence | 1;
        } else {
            i = this.mSimPresence;
        }
        this.mSimPresence = i;
        if (SimFactoryManager.isSIM2CardPresent()) {
            i = this.mSimPresence | 2;
        } else {
            i = this.mSimPresence;
        }
        this.mSimPresence = i;
    }
}
