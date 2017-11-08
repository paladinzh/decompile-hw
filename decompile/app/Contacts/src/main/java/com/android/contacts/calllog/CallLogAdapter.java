package com.android.contacts.calllog;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.QuickContact;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import com.android.common.widget.GroupingListAdapter;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.ContactsUtils.PredefinedNumbers;
import com.android.contacts.EncryptPhoneCallDetailsViews;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.PhoneCallDetailsHelper;
import com.android.contacts.PhoneCallDetailsViews;
import com.android.contacts.activities.ContactDetailActivity.TranslucentActivity;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.calllog.EmergencyNumberHelper.QueryObject;
import com.android.contacts.compatibility.CompatUtils;
import com.android.contacts.compatibility.ContactsCompat;
import com.android.contacts.compatibility.NumberLocationCache;
import com.android.contacts.compatibility.NumberLocationLoader;
import com.android.contacts.compatibility.ProviderFeatureChecker;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.detail.EspaceDialer;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.calllog.PrimaryActionView;
import com.android.contacts.hap.dialer.PhoneAccountUtils;
import com.android.contacts.hap.numbermark.NumberMarkUtil;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.RcseProfile;
import com.android.contacts.hap.rcs.dialer.RcsPhoneCallDetailHelper;
import com.android.contacts.hap.roaming.IsPhoneNetworkRoamingUtils;
import com.android.contacts.hap.roaming.RoamingDialPadDirectlyDataListener;
import com.android.contacts.hap.roaming.RoamingDialPadDirectlyManager;
import com.android.contacts.hap.roaming.RoamingLearnManage;
import com.android.contacts.hap.service.NumberMarkInfo;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.HapEncryptCallUtils;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.hap.utils.VtLteUtils;
import com.android.contacts.list.ContactListHelper;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ExpirableCache;
import com.android.contacts.util.ExpirableCache.CachedValue;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.Objects;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.TextUtil;
import com.android.contacts.util.UriUtils;
import com.android.contacts.widget.AbstractExpandableViewAdapter;
import com.android.dialer.calllog.VoicemailDeleteDialog;
import com.android.dialer.util.OnVoicemailMenuDeletedListener;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter.OnAutoCollapseListener;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter.OnVoicemailDeletedListener;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.google.common.annotations.VisibleForTesting;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cust.HwCustUtils;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CallLogAdapter extends GroupingListAdapter implements OnPreDrawListener, OnScrollListener, OnAutoCollapseListener, OnVoicemailMenuDeletedListener<CallLogListItemViews>, OnVoicemailDeletedListener {
    private static HwCustCallLogAdapter mCust = null;
    private static final HashSet<String> sEmergencyNumberCache = new HashSet();
    private static final HashSet<String> sNonEmergencyNumberCash = new HashSet();
    boolean fling;
    boolean isInvalidateViewsRequired;
    private View lastOpen;
    private boolean mBothSimEnabled;
    private final CallFetcher mCallFetcher;
    private final CallLogListItemHelper mCallLogViewsHelper;
    private QueryThread mCallerIdThread;
    private ExpirableCache<NumberWithCountryIso, ContactInfo> mContactInfoCache;
    private final ContactInfoHelper mContactInfoHelper;
    private final Context mContext;
    private int mCountSize;
    private boolean mCountSizeChanged;
    private int mCurrentlyExpandedPosition;
    private long mCurrentlyExpandedRowId;
    private String mDefaultGeocode;
    private CallLogAdapterListener mDialerListener;
    private boolean mDisplayEmergencyNumber;
    RoamingDialPadDirectlyDataListener mDoubleCardRoamingDialPadDirectlyDataListener;
    private String mEmergencyName;
    private final EmergencyNumberHelper mEmergencyNumberHelper;
    private final OnClickListener mExpandCollapseListener;
    private Set<Long> mGroupSet;
    private boolean mHandleSpecialNumbers;
    private Handler mHandler;
    private boolean mIsContentChange;
    private boolean mIsDualSim;
    private boolean mIsNeedFocus;
    private boolean mIsSub1Enabled;
    private boolean mIsSub2Enabled;
    private boolean mIsSub3Enabled;
    private boolean mIssRingTimeEnabled;
    boolean mItemClicked;
    private int mItemViewWidth;
    private SparseArray<Integer> mListPos;
    private AbsListView mListView;
    private boolean mLoading;
    private NumberLocationLoader mNumberLocationLoader;
    private PhoneNumberHelper mPhoneNumberHelper;
    private Handler mPollListStateHandler;
    private ExpirableCache<NumberWithCountryIso, ContactInfo> mPresetContactInfoCache;
    private final OnClickListener mPrimaryActionListener;
    boolean mPrimaryItemClicked;
    private volatile boolean mRequestProcessingDisabled;
    private final LinkedList<ContactInfoRequest> mRequests;
    private int mSIMcombination;
    private final OnClickListener mSecondaryActionListener;
    private long mSelectCallLogId;
    private String mSim1VoiceMailNumber;
    private String mSim2VoiceMailNumber;
    private Map<String, PredefinedNumbers> mSpecialNumbersMap;
    private boolean mStayInIdle;
    private boolean mStopProcessingCompletely;
    private int mSub1ImageRes;
    private int mSub2ImageRes;
    private int mSub3ImageRes;
    private ViewTreeObserver mViewTreeObserver;
    private final VoicemailPlaybackPresenter mVoicemailPlaybackPresenter;
    private PhoneCallDetailsHelper phoneCallDetailsHelper;

    public interface CallFetcher {
        void fetchCalls();
    }

    public interface CallLogAdapterListener {
        void copyTextToEditText(String str);

        void onScrollCallLog(AbsListView absListView, int i, int i2, int i3);

        void onScrollStateChangedCallLog(AbsListView absListView, int i);
    }

    private static final class ContactInfoRequest {
        public final ContactInfo callLogInfo;
        public final String countryIso;
        public final String number;

        public ContactInfoRequest(String number, String countryIso, ContactInfo callLogInfo) {
            this.number = number;
            this.countryIso = countryIso;
            this.callLogInfo = callLogInfo;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof ContactInfoRequest)) {
                return false;
            }
            ContactInfoRequest other = (ContactInfoRequest) obj;
            return TextUtils.equals(this.number, other.number) && TextUtils.equals(this.countryIso, other.countryIso) && Objects.equal(this.callLogInfo, other.callLogInfo);
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((((this.callLogInfo == null ? 0 : this.callLogInfo.hashCode()) + 31) * 31) + (this.countryIso == null ? 0 : this.countryIso.hashCode())) * 31;
            if (this.number != null) {
                i = this.number.hashCode();
            }
            return hashCode + i;
        }
    }

    private static final class NumberWithCountryIso {
        public final String countryIso;
        public final String number;

        public NumberWithCountryIso(String number, String countryIso) {
            this.number = number;
            this.countryIso = countryIso;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (o == null || !(o instanceof NumberWithCountryIso)) {
                return false;
            }
            NumberWithCountryIso other = (NumberWithCountryIso) o;
            if (TextUtils.equals(this.number, other.number)) {
                z = TextUtils.equals(this.countryIso, other.countryIso);
            }
            return z;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = this.number == null ? 0 : this.number.hashCode();
            if (this.countryIso != null) {
                i = this.countryIso.hashCode();
            }
            return hashCode ^ i;
        }
    }

    private class QueryThread extends Thread {
        private volatile boolean mDone = false;

        public QueryThread() {
            super("CallLogAdapter.QueryThread");
        }

        public void stopProcessing() {
            this.mDone = true;
        }

        public void run() {
            int i = 0;
            while (!this.mDone) {
                ContactInfoRequest req = null;
                synchronized (CallLogAdapter.this.mRequests) {
                    if (!(CallLogAdapter.this.mRequests.isEmpty() || CallLogAdapter.this.fling)) {
                        req = (ContactInfoRequest) CallLogAdapter.this.mRequests.removeFirst();
                    }
                }
                if (req == null) {
                    if (!(CallLogAdapter.this.fling || i == 0)) {
                        i = 0;
                        synchronized (CallLogAdapter.this.mHandler) {
                            CallLogAdapter.this.mHandler.sendEmptyMessage(1);
                        }
                    }
                    synchronized (CallLogAdapter.this.mRequests) {
                        do {
                            try {
                                CallLogAdapter.this.mRequests.wait(1000);
                            } catch (InterruptedException e) {
                            }
                        } while (CallLogAdapter.this.fling);
                    }
                } else if (!CallLogAdapter.this.fling) {
                    try {
                        i |= CallLogAdapter.this.queryContactInfo(req.number, req.countryIso, req.callLogInfo);
                    } catch (SQLiteDiskIOException e2) {
                        HwLog.e("CallLogAdapter", "android.database.sqlite.SQLiteDiskIOException: disk I/O error");
                    }
                }
            }
        }
    }

    private void postNumberOnScreen(Uri uri, Context context) {
        Intent intentDial = new Intent("android.intent.action.DIAL", uri);
        intentDial.setPackage("com.android.contacts");
        context.startActivity(intentDial);
    }

    private void postNumberOnScreenOrTelephone(Uri numberUri, String number) {
        try {
            if (RcsContactsUtils.isBBVersion() || !EmuiFeatureManager.isRcsFeatureEnable() || RcseProfile.getRcsService() == null || !RcseProfile.getRcsService().getLoginState()) {
                CommonUtilMethods.dialNumberFromcalllog(this.mContext, numberUri, this.mContext.getString(R.string.title_call_via), -1, false, true, number);
                return;
            }
            postNumberOnScreen(numberUri, this.mContext);
        } catch (Exception e) {
            HwLog.e("CallLogAdapter", "failed to post number on screen");
        }
    }

    public boolean onPreDraw() {
        unregisterPreDrawListener();
        synchronized (this) {
            if (this.mCallerIdThread == null && !this.mHandler.hasMessages(2)) {
                this.mHandler.sendEmptyMessageDelayed(2, 1000);
            }
        }
        return true;
    }

    private void expandViewHolderActions(CallLogListItemViews viewHolder) {
        if (!(this.mCurrentlyExpandedPosition == -1 || this.lastOpen == null)) {
            AbstractExpandableViewAdapter.animateView(this.lastOpen, 1);
        }
        viewHolder.showActionsAnimation(true);
        this.mCurrentlyExpandedPosition = viewHolder.getAdapterPosition();
        this.mCurrentlyExpandedRowId = viewHolder.rowId;
        this.lastOpen = viewHolder.actionsView;
    }

    public CallLogAdapter(Context context, CallFetcher callFetcher, ContactInfoHelper contactInfoHelper, VoicemailPlaybackPresenter voicemailPlaybackPresenter) {
        super(context);
        this.mViewTreeObserver = null;
        this.mHandleSpecialNumbers = false;
        this.mListPos = new SparseArray();
        this.mSelectCallLogId = -1;
        this.mIsNeedFocus = false;
        this.mItemViewWidth = 0;
        this.mLoading = true;
        this.mRequestProcessingDisabled = false;
        this.mPrimaryActionListener = new OnClickListener() {
            public void onClick(View primaryActionview) {
                IntentProvider tag = primaryActionview.getTag();
                if (tag instanceof IntentProvider) {
                    IntentProvider intentProvider = tag;
                    if (!CallLogAdapter.this.mPrimaryItemClicked) {
                        Intent intent = intentProvider.getIntent(CallLogAdapter.this.mContext);
                        if (intent != null) {
                            CallLogAdapter.this.mPrimaryItemClicked = true;
                            CallLogAdapter.this.mHandler.sendEmptyMessageDelayed(3, 500);
                            String number = PhoneNumberUtils.getNumberFromIntent(intent, CallLogAdapter.this.mContext);
                            boolean isVideoCall = intent.getIntExtra("android.telecom.extra.START_CALL_WITH_VIDEO_STATE", 0) == 3;
                            if (intent.getIntExtra("EXTRA_FEATURE", 0) == 32) {
                                EspaceDialer.dialVoIpCall(CallLogAdapter.this.mContext, number);
                                return;
                            } else if (isVideoCall) {
                                CallLogAdapter.this.mContext.startActivity(intent);
                            } else {
                                String authority = intent.getData().getAuthority();
                                String scheme = intent.getData().getScheme();
                                if (!(TextUtils.isEmpty(authority) && TextUtils.isEmpty(scheme))) {
                                    Uri numberUri;
                                    boolean canPlaceCallsTo = CallLogAdapter.this.mPhoneNumberHelper.canPlaceCallsTo(number, intent.getIntExtra("EXTRA_CALL_LOG_PRESENTATION", 1));
                                    boolean isSipNumber = CallLogAdapter.this.mPhoneNumberHelper.isSipNumber(number);
                                    boolean isRoamingStatus = IsPhoneNetworkRoamingUtils.isPhoneNetworkRoamging();
                                    boolean secondSimEnabled = CommonUtilMethods.getFirstSimEnabled() ? CommonUtilMethods.getSecondSimEnabled() : false;
                                    if (isRoamingStatus && !isSipNumber && (!secondSimEnabled || (secondSimEnabled && SimFactoryManager.isExtremeSimplicityMode()))) {
                                        number = RoamingDialPadDirectlyManager.getDialpadRoamingNumber(CallLogAdapter.this.mContext, number, intent, isRoamingStatus, false, null);
                                    }
                                    if (isSipNumber) {
                                        numberUri = Uri.fromParts("sip", number, null);
                                    } else {
                                        numberUri = Uri.fromParts("tel", number, null);
                                    }
                                    if (!TextUtils.isEmpty(number)) {
                                        if (CallLogAdapter.this.mIsDualSim) {
                                            boolean isNeedCopyText = CallLogAdapter.this.isCopyTextToEditText(secondSimEnabled);
                                            if (secondSimEnabled || isNeedCopyText) {
                                                if (!SimFactoryManager.isExtremeSimplicityMode() || isNeedCopyText) {
                                                    if (isRoamingStatus && !isSipNumber) {
                                                        number = RoamingDialPadDirectlyManager.getDialpadRoamingNumber(CallLogAdapter.this.mContext, number, intent, isRoamingStatus, secondSimEnabled, CallLogAdapter.this.mDoubleCardRoamingDialPadDirectlyDataListener);
                                                    }
                                                    if (!(TextUtils.isEmpty(number) || CallLogAdapter.this.mDialerListener == null)) {
                                                        CallLogAdapter.this.mDialerListener.copyTextToEditText(number);
                                                    }
                                                } else if (canPlaceCallsTo) {
                                                    CallLogAdapter.this.postNumberOnScreenOrTelephone(numberUri, number);
                                                }
                                                StatisticalHelper.report(5034);
                                            } else if (canPlaceCallsTo) {
                                                CallLogAdapter.this.postNumberOnScreenOrTelephone(numberUri, number);
                                            }
                                        } else if (canPlaceCallsTo) {
                                            CallLogAdapter.this.postNumberOnScreenOrTelephone(numberUri, number);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return;
                }
                if (HwLog.HWFLOW) {
                    HwLog.i("CallLogAdapter", "could not dial ,primaryActionview getTag:" + tag);
                }
            }
        };
        this.mDoubleCardRoamingDialPadDirectlyDataListener = new RoamingDialPadDirectlyDataListener() {
            public void selectedDirectlyData(String number) {
                if (CommonUtilMethods.getFirstSimEnabled() && CommonUtilMethods.getSecondSimEnabled() && CallLogAdapter.this.mDialerListener != null) {
                    CallLogAdapter.this.mDialerListener.copyTextToEditText(number);
                }
            }
        };
        this.mSecondaryActionListener = new OnClickListener() {
            public void onClick(View view) {
                IntentProvider intentProvider = (IntentProvider) view.getTag();
                if (!(intentProvider == null || CallLogAdapter.this.mItemClicked)) {
                    Intent intent = intentProvider.getIntent(CallLogAdapter.this.mContext);
                    if (intent != null) {
                        Uri lookupUri = intent.getData();
                        if (lookupUri == null || !ContactsCompat.isEnterpriseContactId(ContentUris.parseId(lookupUri))) {
                            if (!TextUtils.isEmpty(intent.getStringExtra("contact_display_name"))) {
                                Uri uri = intent.getData();
                                HwLog.i("CallLogAdapter", "uri is null:" + (uri == null));
                                if (uri == null) {
                                    boolean isEmergencyNumber = false;
                                    boolean isSpecialNumber = intent.getBooleanExtra("contact_display_name_is_special_num", false);
                                    String mNumber = intent.getStringExtra("EXTRA_CALL_LOG_NUMBER");
                                    if (mNumber != null) {
                                        isEmergencyNumber = CommonUtilMethods.isEmergencyNumber(mNumber, SimFactoryManager.isDualSim());
                                    }
                                    boolean isSupportCnap = false;
                                    if (CallLogAdapter.mCust != null) {
                                        isSupportCnap = CallLogAdapter.mCust.isSupportCnap();
                                    }
                                    if (!(isEmergencyNumber || isSpecialNumber || r7)) {
                                        return;
                                    }
                                }
                            }
                            CallLogAdapter.this.mItemClicked = true;
                            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                                CallLogAdapter.this.mSelectCallLogId = intent.getLongExtra("EXTRA_ID_SELECTED", -1);
                            }
                            boolean waitResult = false;
                            if (intent.getBooleanExtra("INTENT_FROM_DIALER", false) && CommonUtilMethods.calcIfNeedSplitScreen()) {
                                PeopleActivity peopleActivity = null;
                                if (CallLogAdapter.this.mContext instanceof PeopleActivity) {
                                    peopleActivity = (PeopleActivity) CallLogAdapter.this.mContext;
                                }
                                if (peopleActivity != null) {
                                    peopleActivity.setNeedMaskDialpad(true);
                                    waitResult = true;
                                    intent.setClass(CallLogAdapter.this.mContext, TranslucentActivity.class);
                                    peopleActivity.saveInstanceValues();
                                    peopleActivity.startActivityForResult(intent, 3002);
                                }
                            }
                            if (!waitResult) {
                                CallLogAdapter.this.mContext.startActivity(intent);
                            }
                        } else {
                            QuickContact.showQuickContact(CallLogAdapter.this.mContext, new Rect(), lookupUri, 3, null);
                        }
                        StatisticalHelper.report(5031);
                    }
                }
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        CallLogAdapter.this.notifyDataSetChanged();
                        return;
                    case 2:
                        CallLogAdapter.this.startRequestProcessing();
                        return;
                    case 3:
                        CallLogAdapter.this.mPrimaryItemClicked = false;
                        return;
                    default:
                        return;
                }
            }
        };
        this.mCurrentlyExpandedPosition = -1;
        this.mCurrentlyExpandedRowId = -1;
        this.lastOpen = null;
        this.mExpandCollapseListener = new OnClickListener() {
            public void onClick(View v) {
                CallLogListItemViews viewHolder = (CallLogListItemViews) v.getTag();
                if (viewHolder != null) {
                    if (CallLogAdapter.this.mVoicemailPlaybackPresenter != null) {
                        CallLogAdapter.this.mVoicemailPlaybackPresenter.resetAll();
                    }
                    if (viewHolder.getAdapterPosition() == CallLogAdapter.this.mCurrentlyExpandedPosition) {
                        viewHolder.showActionsAnimation(false);
                        CallLogAdapter.this.mCurrentlyExpandedPosition = -1;
                        CallLogAdapter.this.mCurrentlyExpandedRowId = -1;
                        CallLogAdapter.this.lastOpen = null;
                    } else {
                        CallLogAdapter.this.expandViewHolderActions(viewHolder);
                    }
                }
            }
        };
        this.mCountSizeChanged = false;
        this.mCountSize = 0;
        this.mDefaultGeocode = null;
        this.mBothSimEnabled = false;
        this.mIsContentChange = false;
        this.mStayInIdle = true;
        this.mPollListStateHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    CallLogAdapter.this.mStayInIdle = true;
                    if (CallLogAdapter.this.mIsContentChange) {
                        CallLogAdapter.this.mIsContentChange = false;
                        CallLogAdapter.this.mCallFetcher.fetchCalls();
                    }
                }
                if (msg.what == 3 && CallLogAdapter.this.mListView != null) {
                    CallLogAdapter.this.mListView.invalidateViews();
                }
            }
        };
        this.isInvalidateViewsRequired = false;
        this.mEmergencyNumberHelper = new EmergencyNumberHelper(new Handler() {
            public void handleMessage(Message msg) {
                QueryObject obj = msg.obj;
                String number = obj.mNumber;
                if (msg.what == 2) {
                    if (obj.mDetails == obj.mViews.getPhoneCallDetails()) {
                        boolean z;
                        String name = CallLogAdapter.this.mEmergencyName;
                        if (CallLogAdapter.mCust != null) {
                            name = CallLogAdapter.mCust.setSdnName(number, name);
                        }
                        obj.mDetails.name = name;
                        PhoneCallDetailsHelper -get14 = CallLogAdapter.this.phoneCallDetailsHelper;
                        PhoneCallDetailsViews phoneCallDetailsViews = obj.mViews;
                        PhoneCallDetails phoneCallDetails = obj.mDetails;
                        boolean z2 = CallLogAdapter.this.fling;
                        if (obj.mDetails.mReadState == 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        -get14.displayNameAndNumber(phoneCallDetailsViews, phoneCallDetails, z2, false, z);
                    }
                    CallLogAdapter.sEmergencyNumberCache.add(number);
                } else {
                    if (CallLogAdapter.sNonEmergencyNumberCash.size() > VTMCDataCache.MAXSIZE) {
                        CallLogAdapter.sNonEmergencyNumberCash.remove(Integer.valueOf(0));
                    }
                    CallLogAdapter.sNonEmergencyNumberCash.add(number);
                }
                CallLogAdapter.this.mEmergencyNumberHelper.recycleQueryObject(obj);
                super.handleMessage(msg);
            }
        });
        this.mContext = context;
        this.mCallFetcher = callFetcher;
        this.mContactInfoHelper = contactInfoHelper;
        this.mVoicemailPlaybackPresenter = voicemailPlaybackPresenter;
        if (this.mVoicemailPlaybackPresenter != null) {
            this.mVoicemailPlaybackPresenter.setOnVoicemailDeletedListener(this);
            this.mVoicemailPlaybackPresenter.setOnAutoCollapseListener(this);
        }
        this.mContactInfoCache = ExpirableCache.create(100);
        this.mPresetContactInfoCache = ExpirableCache.create(100);
        this.mRequests = new LinkedList();
        Resources resources = this.mContext.getResources();
        CallTypeHelper callTypeHelper = new CallTypeHelper(resources);
        this.mPhoneNumberHelper = new PhoneNumberHelper(resources);
        this.phoneCallDetailsHelper = new PhoneCallDetailsHelper(this.mContext, callTypeHelper, this.mPhoneNumberHelper);
        this.mCallLogViewsHelper = new CallLogListItemHelper(this.phoneCallDetailsHelper, this.mPhoneNumberHelper, resources);
        this.mIsDualSim = SimFactoryManager.isDualSim();
        this.mSIMcombination = SimFactoryManager.getSimCombination();
        this.mIssRingTimeEnabled = EmuiFeatureManager.isRingTimesDisplayEnabled(null);
        this.mDisplayEmergencyNumber = ContactsUtils.displayEmergencyNumber(this.mContext);
        if (this.mDisplayEmergencyNumber) {
            this.mEmergencyName = this.mContext.getResources().getString(R.string.emergency_number);
        }
        initCust();
        if (mCust != null) {
            mCust.getSdnNumbers();
        }
        boolean isLand = this.mContext.getResources().getConfiguration().orientation == 2;
        this.mIsNeedFocus = CommonUtilMethods.calcIfNeedSplitScreen() ? isLand : false;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        this.mItemViewWidth = isLand ? metrics.widthPixels / 2 : metrics.widthPixels;
        ProviderFeatureChecker.getInstance(this.mContext);
        ContactsApplication.isPrivateModeOn();
    }

    public CallLogAdapter(Context context, CallFetcher callFetcher, ContactInfoHelper contactInfoHelper) {
        this(context, callFetcher, contactInfoHelper, null);
    }

    public void updateCustSetting() {
        if (mCust != null) {
            mCust.updateCustSetting();
            mCust.addSdnNumbers41();
        }
        if (this.phoneCallDetailsHelper != null) {
            this.phoneCallDetailsHelper.updateCustSetting();
        }
    }

    public void setSpecialNumbersMap() {
        this.mSpecialNumbersMap = ContactsUtils.getPredefinedMap(this.mContext);
    }

    public void setHandleSpecialNumbers() {
        boolean z = false;
        if (this.mSpecialNumbersMap != null) {
            if (this.mSpecialNumbersMap.size() > 0) {
                z = true;
            }
            this.mHandleSpecialNumbers = z;
        }
    }

    public boolean hasExpandedItem() {
        return this.mCurrentlyExpandedPosition != -1;
    }

    public void setNumberLocationLoader(NumberLocationLoader numberLocationLoader) {
        this.mNumberLocationLoader = numberLocationLoader;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("expanded_position", this.mCurrentlyExpandedPosition);
        outState.putLong("expanded_row_id", this.mCurrentlyExpandedRowId);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mCurrentlyExpandedPosition = savedInstanceState.getInt("expanded_position", -1);
            this.mCurrentlyExpandedRowId = savedInstanceState.getLong("expanded_row_id", -1);
        }
    }

    protected void onContentChanged() {
        if (this.mStayInIdle) {
            this.mCallFetcher.fetchCalls();
            this.mIsContentChange = false;
            return;
        }
        this.mIsContentChange = true;
    }

    void setLoading(boolean loading) {
        this.mLoading = loading;
    }

    public boolean isEmpty() {
        if (this.mLoading) {
            return false;
        }
        return super.isEmpty();
    }

    public ContactInfo getContactInfo(String number, String countryISO) {
        ContactInfo lInfo = (ContactInfo) this.mContactInfoCache.getPossiblyExpired(new NumberWithCountryIso(number, countryISO));
        if (lInfo == null) {
            return (ContactInfo) this.mPresetContactInfoCache.getPossiblyExpired(new NumberWithCountryIso(number, countryISO));
        }
        return lInfo;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void startRequestProcessing() {
        if (!this.mRequestProcessingDisabled && !this.mStopProcessingCompletely) {
            if (this.mCallerIdThread == null) {
                this.mCallerIdThread = new QueryThread();
                this.mCallerIdThread.setPriority(1);
                this.mCallerIdThread.start();
                RoamingLearnManage.getInstance().startRequestProcessing();
            }
        }
    }

    public synchronized void stopRequestProcessing() {
        this.mHandler.removeMessages(2);
        if (this.mCallerIdThread != null) {
            this.mCallerIdThread.stopProcessing();
            this.mCallerIdThread.interrupt();
            this.mCallerIdThread = null;
        }
        RoamingLearnManage.getInstance().stopRequestProcessing();
    }

    public synchronized void destroyRequestProcessing() {
        this.mStopProcessingCompletely = true;
        stopRequestProcessing();
    }

    public void destroyEmergencyNumberHelper() {
        this.mEmergencyNumberHelper.releaseHelper();
    }

    private void unregisterPreDrawListener() {
        if (this.mViewTreeObserver != null && this.mViewTreeObserver.isAlive()) {
            this.mViewTreeObserver.removeOnPreDrawListener(this);
        }
        this.mViewTreeObserver = null;
    }

    public void invalidateCache() {
        this.mContactInfoCache.expireAll();
        stopRequestProcessing();
        unregisterPreDrawListener();
        this.phoneCallDetailsHelper.resetTimeFormats();
    }

    public void clearPresetCache() {
        this.mPresetContactInfoCache.expireAll();
    }

    public void saveTimeFormatFlag() {
        this.phoneCallDetailsHelper.saveTimeFormatFlag();
    }

    public boolean restoreTimeFormatFlag() {
        return this.phoneCallDetailsHelper.restoreTimeFormatFlag();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @VisibleForTesting
    void enqueueRequest(String number, String countryIso, ContactInfo callLogInfo, boolean immediate) {
        synchronized (this) {
            if (this.mStopProcessingCompletely) {
            }
        }
    }

    private boolean queryContactInfo(String number, String countryIso, ContactInfo callLogInfo) {
        boolean isPredefinedContact = false;
        ContactInfo info = this.mContactInfoHelper.lookupNumber(number, countryIso);
        if (info == null) {
            return false;
        }
        ContactInfo existingInfo;
        if (callLogInfo.lookupUri == null) {
            callLogInfo.lookupUri = UriUtils.parseUriOrNull(callLogInfo.mCachedLookUpUriString);
        }
        NumberWithCountryIso numberCountryIso = new NumberWithCountryIso(number, countryIso);
        if (!(TextUtils.isEmpty(callLogInfo.name) || info.lookupUri == null)) {
            isPredefinedContact = info.lookupUri.getAuthority().equals("com.android.contacts.app");
        }
        if (isPredefinedContact) {
            existingInfo = (ContactInfo) this.mPresetContactInfoCache.getPossiblyExpired(numberCountryIso);
        } else {
            existingInfo = (ContactInfo) this.mContactInfoCache.getPossiblyExpired(numberCountryIso);
        }
        boolean updated = !info.equals(existingInfo);
        if (isPredefinedContact) {
            this.mPresetContactInfoCache.put(numberCountryIso, info);
            if (this.mContactInfoCache.getPossiblyExpired(numberCountryIso) != null) {
                this.mContactInfoCache.put(numberCountryIso, null);
            }
        } else {
            this.mContactInfoCache.put(numberCountryIso, info);
        }
        updateCallLogContactInfoCache(number, countryIso, info, callLogInfo);
        if (EmuiFeatureManager.isPrivacyFeatureEnabled() && existingInfo != null) {
            updated = updated || info.mIsPrivate != existingInfo.mIsPrivate;
        }
        return updated;
    }

    protected void addGroups(Cursor cursor) {
        long startTime = System.currentTimeMillis();
        if (this.mGroupSet != null) {
            for (Long longValue : this.mGroupSet) {
                long groupData = longValue.longValue();
                addGroup((int) (4294967295L & groupData), (int) ((9223372032559808512L & groupData) >> 32), false);
            }
        }
        int lCountSise = getCount();
        if (HwLog.HWDBG) {
            if (cursor != null) {
                HwLog.d("CallLogAdapter", "addGroups spentTime:" + (System.currentTimeMillis() - startTime) + " cursor size:" + cursor.getCount() + " lCountSise:" + lCountSise);
            } else {
                HwLog.d("CallLogAdapter", "addGroups spentTime:" + (System.currentTimeMillis() - startTime) + " lCountSise:" + lCountSise);
            }
        }
        if (this.mCountSize == lCountSise) {
            this.mCountSizeChanged = false;
        } else {
            this.mCountSizeChanged = true;
        }
    }

    public void buildGroupData(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            Bundle b = cursor.getExtras();
            if (b == null || !b.getBoolean("IS_MULTI_SELECT", false)) {
                this.mGroupSet = CallLogGroupBuilder.getGroups(cursor, this.mListPos);
                return;
            }
            Set<Long> groupSet = null;
            Set<Long> obj = b.getSerializable("GROUP_SET");
            if (obj instanceof Set) {
                groupSet = obj;
            }
            this.mGroupSet = groupSet;
            Object obj2 = b.getSerializable("GROUP_ID_MAP");
            if (obj2 instanceof SparseArray) {
                this.mListPos = (SparseArray) obj2;
            }
        }
    }

    public void changeCursor(Cursor cursor) {
        this.mCurrentlyExpandedPosition = -1;
        this.mCountSize = getCount();
        if (HwLog.HWDBG) {
            HwLog.d("CallLogAdapter", "changeCursor mCountSize:" + this.mCountSize);
        }
        buildGroupData(cursor);
        super.changeCursor(cursor);
    }

    protected View newStandAloneView(Context context, ViewGroup parent) {
        return initializeNewView(context, parent);
    }

    protected void bindStandAloneView(View view, Context context, Cursor cursor) {
        bindView(view, cursor, 1);
    }

    protected View newChildView(Context context, ViewGroup parent) {
        return initializeNewView(context, parent);
    }

    protected void bindChildView(View view, Context context, Cursor cursor) {
        bindView(view, cursor, 1);
    }

    protected View newGroupView(Context context, ViewGroup parent) {
        return initializeNewView(context, parent);
    }

    protected void bindGroupView(View view, Context context, Cursor cursor, int groupSize, boolean expanded) {
        bindView(view, cursor, groupSize);
    }

    private View initializeNewView(Context context, ViewGroup parent) {
        PLog.d(0, "CallLogAdapter initializeNewView begin");
        View view = ((PeopleActivity) this.mContext).getContactListHelper().getCallLogItem();
        findAndCacheTimeAxisWidgetViews(context, view, view.findViewById(R.id.call_log_list_item_time_axis_child_content), true);
        PLog.d(0, "CallLogAdapter initializeNewView end");
        return view;
    }

    protected void findAndCacheTimeAxisWidgetViews(Context context, View view, View interView, boolean isCallLogFragment) {
        if (view != null && interView != null) {
            CallLogListItemViews views = CallLogListItemViews.create(this.mContext, view, interView, this.mVoicemailPlaybackPresenter);
            views.primaryActionView.setOnClickListener(this.mPrimaryActionListener);
            views.secondaryActionViewLayout.setOnClickListener(this.mSecondaryActionListener);
            view.setTag(views);
            if (views.primaryActionView instanceof PrimaryActionView) {
                PrimaryActionView primaryViews = views.primaryActionView;
                primaryViews.bindViews(views, views.primaryActionView.findViewById(R.id.call_log_list_item_time_axis_child_content), (CheckBox) view.findViewById(R.id.checkbox), view.findViewById(R.id.list_item_shadow));
                int mItemMaxWidth = context.getResources().getDisplayMetrics().widthPixels;
                if ((isCallLogFragment || CommonUtilMethods.calcIfNeedSplitScreen()) && primaryViews.isLandScape()) {
                    mItemMaxWidth /= 2;
                }
                primaryViews.bindWidth(mItemMaxWidth, isCallLogFragment, primaryViews.isLandScape());
            }
        }
    }

    private void bindView(View view, Cursor c, int count) {
        if (c.getCount() >= 1) {
            int i;
            CachedValue<ContactInfo> cachedInfo;
            PhoneCallDetails details;
            if (view.getPaddingTop() < 0) {
                view.setPadding(0, 0, 0, 0);
            }
            int pos = c.getPosition();
            if (pos == 0) {
                PLog.d(0, "CallLogAdapter bindView begin");
            }
            String callId = c.getString(0);
            CallLogListItemViews views = (CallLogListItemViews) view.getTag();
            views.primaryActionView.setVisibility(0);
            if (this.mListPos.get(pos) == null) {
                i = pos;
            } else {
                i = ((Integer) this.mListPos.get(pos)).intValue();
            }
            views.mPosition = i;
            views.rowId = Long.parseLong(callId);
            views.voicemailUri = c.getString(6);
            views.callType = c.getInt(4);
            int hdType = c.getInt(27);
            if (isFromCallLogFragment()) {
                if (views.callType == 4) {
                    views.primaryActionView.setOnClickListener(this.mExpandCollapseListener);
                    views.transcription = c.getString(22);
                } else {
                    views.primaryActionView.setOnClickListener(this.mPrimaryActionListener);
                }
                if (this.mCallFetcher instanceof CallLogFragment) {
                    CallLogFragment calllogFragment = (CallLogFragment) this.mCallFetcher;
                    if (calllogFragment.getVoicemailUri() != null && calllogFragment.getVoicemailUri().equals(views.voicemailUri)) {
                        this.mCurrentlyExpandedRowId = views.rowId;
                        calllogFragment.setVoicemailUri(null);
                        views.mVoicemailPrimaryActionButtonClicked = true;
                    }
                }
                if (views.primaryActionView instanceof PrimaryActionView) {
                    PrimaryActionView primaryViews = (PrimaryActionView) views.primaryActionView;
                    if (!(this.mItemViewWidth == 0 || this.mItemViewWidth == primaryViews.getViewMaxWidth())) {
                        HwLog.d("CallLogAdapter", "calllog bindview refresh width");
                        primaryViews.bindWidth(this.mItemViewWidth, true, true);
                    }
                }
            }
            final String number = c.getString(1);
            String postDialDigits = CompatUtils.isNCompatible() ? c.getString(CallLogQuery.POST_DIAL_DIGITS) : "";
            views.accountHandle = PhoneAccountUtils.getAccount(c.getString(18), c.getString(19));
            int presentation = c.getInt(17);
            final String countryIso = c.getString(5);
            long date = c.getLong(2);
            long duration = c.getLong(3);
            int subId = getSubId(views.accountHandle, views.callType, c);
            boolean isSupportDualSim = this.mIsDualSim ? QueryUtil.isSupportDualSim() : false;
            ContactInfo cachedContactInfo = getContactInfoFromCallLog(c, count);
            boolean lIsPresetContact = (TextUtils.isEmpty(cachedContactInfo.name) || cachedContactInfo.lookupUri != null) ? cachedContactInfo.lookupUri != null ? "com.android.contacts.app".equals(cachedContactInfo.lookupUri.getAuthority()) : false : true;
            boolean isNumberNotEmpty = !TextUtils.isEmpty(number);
            views.setNumber(number);
            views.setPostDialDigits(postDialDigits);
            views.setNumPresentation(presentation);
            boolean isVoiceMailNum = isVoiceMailNumber(number);
            boolean z = false;
            if (mCust != null) {
                z = mCust.isSdnNumber(number);
                if (z) {
                    isVoiceMailNum = false;
                }
            }
            updateEncryptCallView(views.phoneCallDetailsViews, c);
            decideIfCallLogHdIconIsShown(views.phoneCallDetailsViews.hdcallIcon, hdType);
            if (this.fling) {
                if (isNumberNotEmpty && isSupportDualSim) {
                    setDualSimCallImages(subId, views.phoneCallDetailsViews.cardType);
                }
            } else if (isNumberNotEmpty) {
                if (isSupportDualSim) {
                    setDualSimCallImages(subId, views.phoneCallDetailsViews.cardType);
                }
                String normalizedNumber = c.getString(13);
                String callLookupuri = c.getString(11);
                String callName = c.getString(8);
                int feature = c.getInt(20);
                boolean isVideoCall = false;
                if (VtLteUtils.isVtLteSupport()) {
                    isVideoCall = c.getInt(20) == 1;
                }
                if (isVideoCall) {
                    views.primaryActionView.setTag(VtLteUtils.getVideoCallIntentProvider(number));
                } else if (views.callType == 4) {
                    views.primaryActionView.setTag(views);
                } else {
                    views.primaryActionView.setTag(IntentProvider.getReturnCallIntentProvider(number + postDialDigits, presentation, subId, normalizedNumber, duration, countryIso, callLookupuri, callName, feature));
                    ((IntentProvider) views.primaryActionView.getTag()).setName(cachedContactInfo.name);
                }
            } else {
                if (isSupportDualSim) {
                    setDualSimCallImages(subId, views.phoneCallDetailsViews.cardType);
                }
                views.primaryActionView.setTag(null);
            }
            if (this.mIsNeedFocus) {
                long longCallLogId = c.getLong(0);
                if (this.mSelectCallLogId == -1 || this.mSelectCallLogId != longCallLogId) {
                    view.setBackgroundColor(0);
                } else {
                    view.setBackgroundColor(this.mContext.getResources().getColor(R.color.split_itme_selected));
                }
            }
            NumberWithCountryIso numberWithCountryIso = new NumberWithCountryIso(number, countryIso);
            if (lIsPresetContact) {
                cachedInfo = this.mPresetContactInfoCache.getCachedValue(numberWithCountryIso);
                if (cachedInfo == null) {
                    cachedInfo = this.mContactInfoCache.getCachedValue(numberWithCountryIso);
                }
            } else {
                cachedInfo = this.mContactInfoCache.getCachedValue(numberWithCountryIso);
            }
            ContactInfo contactInfo = cachedInfo == null ? null : (ContactInfo) cachedInfo.getValue();
            if (isNumberNotEmpty ? this.mPhoneNumberHelper.canPlaceCallsToAvoidNullCheck(number) : false) {
                final ContactInfo contactInfo2;
                if (cachedInfo != null) {
                    if (EmuiFeatureManager.isPrivacyFeatureEnabled() && !ContactsApplication.isPrivateModeOn()) {
                        if (!cachedInfo.isExpired()) {
                        }
                    }
                    if (!isVoiceMailNum) {
                        if (!this.fling && (cachedInfo.isExpired() || !callLogInfoMatches(cachedContactInfo, contactInfo))) {
                            contactInfo2 = cachedContactInfo;
                            this.mPollListStateHandler.postDelayed(new Runnable() {
                                public void run() {
                                    CallLogAdapter.this.enqueueRequest(number, countryIso, contactInfo2, false);
                                }
                            }, 300);
                        }
                        if (contactInfo == ContactInfo.EMPTY || isContactInfoOnlyHasNumber(contactInfo)) {
                            contactInfo = cachedContactInfo;
                        }
                    }
                }
                contactInfo = cachedContactInfo;
                if (!this.fling) {
                    if (lIsPresetContact) {
                        if (!isVoiceMailNum) {
                            this.mPresetContactInfoCache.put(numberWithCountryIso, ContactInfo.EMPTY);
                        }
                    } else if (!isVoiceMailNum) {
                        this.mContactInfoCache.put(numberWithCountryIso, ContactInfo.EMPTY);
                    }
                    contactInfo2 = cachedContactInfo;
                    this.mPollListStateHandler.postDelayed(new Runnable() {
                        public void run() {
                            CallLogAdapter.this.enqueueRequest(number, countryIso, contactInfo2, true);
                        }
                    }, 300);
                }
            } else {
                contactInfo = ContactInfo.EMPTY;
            }
            Uri uri = null;
            CharSequence name = null;
            if (contactInfo != null) {
                uri = contactInfo.lookupUri;
                name = contactInfo.name;
            }
            boolean isEmergencyNumber = false;
            boolean isSpecialNumber = false;
            if (!(!this.mHandleSpecialNumbers || this.mSpecialNumbersMap.get(number) == null || ((PredefinedNumbers) this.mSpecialNumbersMap.get(number)).isEmergencyNumber) || r81) {
                isSpecialNumber = true;
            }
            if (!this.fling && this.mHandleSpecialNumbers && TextUtils.isEmpty(r48)) {
                PredefinedNumbers lPredefined = (PredefinedNumbers) this.mSpecialNumbersMap.get(number);
                if (lPredefined != null) {
                    if (lPredefined.isEmergencyNumber) {
                        name = this.mEmergencyName;
                    } else {
                        Object obj = lPredefined.mPredefinedName;
                    }
                }
            }
            boolean needQueryEmergency = false;
            if (this.mDisplayEmergencyNumber && TextUtils.isEmpty(r48)) {
                if (sEmergencyNumberCache.contains(number)) {
                    name = this.mEmergencyName;
                    isEmergencyNumber = true;
                    needQueryEmergency = false;
                } else if (sNonEmergencyNumberCash.contains(number)) {
                    isEmergencyNumber = false;
                    needQueryEmergency = false;
                } else {
                    needQueryEmergency = true;
                }
            }
            int ntype = 0;
            CharSequence charSequence = null;
            CharSequence formattedNumber = null;
            if (contactInfo != null) {
                ntype = contactInfo.type;
                charSequence = contactInfo.label;
                formattedNumber = contactInfo.formattedNumber;
            }
            int[] callTypes = cachedContactInfo.mCallTypes;
            String geocode = "";
            boolean isNeedLoad = false;
            String geocode1 = cachedContactInfo.mGeoLocation;
            boolean showGeo = true;
            if (ContactsUtils.isUnknownNumber(presentation) || isVoiceMailNum) {
                geocode = "";
                showGeo = false;
            } else if (!PhoneCapabilityTester.isGeoCodeFeatureEnabled(this.mContext)) {
                geocode = geocode1;
            } else if (QueryUtil.checkGeoLocation(geocode1, number)) {
                geocode = geocode1;
            } else {
                geocode = NumberLocationCache.getLocation(number);
                if (geocode == null) {
                    isNeedLoad = true;
                    geocode = "";
                }
            }
            if (TextUtils.isEmpty(geocode) && showGeo && !lIsPresetContact) {
                if (this.mDefaultGeocode == null) {
                    this.mDefaultGeocode = this.mContext.getResources().getString(R.string.numberLocationUnknownLocation2);
                }
                geocode = this.mDefaultGeocode;
                if (EmuiFeatureManager.isHideUnknownGeo()) {
                    geocode = "";
                }
            }
            int ringTimes = 1;
            if (this.mIssRingTimeEnabled) {
                int ringTimesIndex = c.getColumnIndex("ring_times");
                if (ringTimesIndex >= 0) {
                    ringTimes = c.getInt(ringTimesIndex);
                }
            }
            long callsFeatures = getCallsFeaturesValue(c);
            long callFeatures = 0;
            if (mCust != null) {
                callFeatures = mCust.getCallFeaturesValue(c);
            }
            if (32 == callsFeatures) {
                geocode = null;
            }
            if (mCust != null) {
                name = mCust.setSdnName41(number, name, isVoiceMailNum, subId);
            }
            String numberMarkInfo = null;
            String originMarkInfo = null;
            if (EmuiFeatureManager.isNumberMarkFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner() && TextUtils.isEmpty(name) && !isVoiceMailNum) {
                NumberMarkInfo markInfo = new NumberMarkInfo(number, c.getString(CallLogQuery.getMarkContentColumnIndex()), c.getString(CallLogQuery.getMarkTypeColumnIndex()), c.getInt(CallLogQuery.getMarkCountColumnIndex()), c.getInt(CallLogQuery.getIsCloudMarkColumnIndex()) == 1);
                originMarkInfo = NumberMarkUtil.revertMarkTypeToMarkName(markInfo.getClassify(), this.mContext);
                if (TextUtils.isEmpty(originMarkInfo)) {
                    originMarkInfo = markInfo.getName();
                }
                views.mOriginMarkInfo = originMarkInfo;
                numberMarkInfo = NumberMarkUtil.getMarkLabel(this.mContext, markInfo);
                details = new PhoneCallDetails(number, formattedNumber, countryIso, geocode, callTypes, date, duration, subId, isVoiceMailNum, ringTimes, numberMarkInfo, markInfo, contactInfo == null ? 0 : contactInfo.userType, postDialDigits);
            } else {
                long j;
                String str = contactInfo == null ? null : contactInfo.mCachedLookUpUriString;
                if (contactInfo == null) {
                    j = 0;
                } else {
                    j = contactInfo.userType;
                }
                PhoneCallDetails phoneCallDetails = new PhoneCallDetails(number, formattedNumber, countryIso, geocode, callTypes, date, duration, name, ntype, charSequence, uri, null, subId, isVoiceMailNum, str, ringTimes, j, postDialDigits);
            }
            if (EmuiFeatureManager.isRcsFeatureEnable()) {
                details.addSubject(c);
                RcsPhoneCallDetailHelper.fromView(views.phoneCallDetailsViews, view, details);
            }
            views.phoneCallDetailsViews.setPhoneCallDetail(details);
            if (needQueryEmergency) {
                this.mEmergencyNumberHelper.queryEmergencyNumber(number, views.phoneCallDetailsViews);
            }
            details.setCallsTypeFeatures(callsFeatures);
            details.setCallTypeFeatures(callFeatures);
            if (!this.fling) {
                String str2 = null;
                String lookupUriString = null;
                if (contactInfo != null) {
                    str2 = contactInfo.formattedNumber;
                    lookupUriString = contactInfo.mCachedLookUpUriString;
                }
                views.secondaryActionViewLayout.setTag(IntentProvider.getCallDetailIntentProvider(getCursor(), c.getPosition(), c.getLong(0), count, str2, numberMarkInfo, lookupUriString, name, isSpecialNumber, originMarkInfo));
            }
            details.mIsEmergencyNumber = isEmergencyNumber;
            details.setPresentation(presentation);
            boolean isNew = cachedContactInfo.mIsNew;
            boolean isHighlighted = isNew;
            this.mCallLogViewsHelper.setPhoneCallDetails(views, details, isNew, callId, this.fling, this.mContext);
            if (this.mCurrentlyExpandedRowId == views.rowId) {
                this.mCurrentlyExpandedPosition = views.mPosition;
                views.initActionView();
                this.lastOpen = views.actionsView;
            }
            views.showActions(this.mCurrentlyExpandedPosition == views.mPosition);
            if (isNumberNotEmpty && isNeedLoad && this.mNumberLocationLoader != null) {
                this.mNumberLocationLoader.putWaitingView(details);
            }
            if (!this.fling) {
                views.phoneCallDetailsViews.numberView.setTag(details);
                if (this.mViewTreeObserver == null) {
                    this.mViewTreeObserver = view.getViewTreeObserver();
                    this.mViewTreeObserver.addOnPreDrawListener(this);
                }
            }
            if (pos == 0) {
                PLog.d(8, "CallLogAdapter bindView end");
            }
        }
    }

    private boolean isContactInfoOnlyHasNumber(ContactInfo info) {
        if (info == null || (!TextUtils.isEmpty(info.number) && !TextUtils.isEmpty(info.formattedNumber) && info.name == null && info.lookupUri == null && info.mCachedLookUpUriString == null)) {
            return true;
        }
        return false;
    }

    private int getSubId(PhoneAccountHandle handle, int callType, Cursor c) {
        int subId = -1;
        if (QueryUtil.isSupportDualSim()) {
            String accountComponentName = c.getString(18);
            String accountId = c.getString(19);
            if (accountComponentName == null || accountId == null) {
                subId = c.getInt(CallLogQuery.getSubscriptionColumnIndex());
            } else if (callType != 4) {
                try {
                    subId = Integer.parseInt(accountId);
                } catch (NumberFormatException e) {
                    subId = c.getInt(CallLogQuery.getSubscriptionColumnIndex());
                }
            } else {
                List<PhoneAccountHandle> callCapablePhoneAccounts = ((TelecomManager) this.mContext.getSystemService("telecom")).getCallCapablePhoneAccounts();
                if (callCapablePhoneAccounts.size() > 1) {
                    return (!accountId.equals(((PhoneAccountHandle) callCapablePhoneAccounts.get(0)).getId()) && accountId.equals(((PhoneAccountHandle) callCapablePhoneAccounts.get(1)).getId())) ? 1 : 0;
                }
            }
        }
        return subId;
    }

    protected boolean isFromCallLogFragment() {
        return true;
    }

    public void updateNumberLocation(PhoneCallDetails details, CallLogListItemViews views) {
        boolean z;
        PhoneCallDetailsHelper phoneCallDetailsHelper = this.phoneCallDetailsHelper;
        PhoneCallDetailsViews phoneCallDetailsViews = views.phoneCallDetailsViews;
        boolean z2 = this.fling;
        if (details.mReadState == 0) {
            z = true;
        } else {
            z = false;
        }
        phoneCallDetailsHelper.displayNameAndNumber(phoneCallDetailsViews, details, z2, false, z);
    }

    private void setDualSimCallImages(int subscriptionID, ImageView cardTypeView) {
        if (this.mBothSimEnabled) {
            setCallImages(subscriptionID, cardTypeView);
        } else if (cardTypeView != null) {
            cardTypeView.setVisibility(8);
        }
    }

    public void setCallImages(int subscriptionID, ImageView cardTypeView) {
        boolean isSubEnabled = false;
        int resId = 0;
        switch (subscriptionID) {
            case 0:
                resId = this.mSub1ImageRes;
                isSubEnabled = this.mIsSub1Enabled;
                break;
            case 1:
                resId = this.mSub2ImageRes;
                isSubEnabled = this.mIsSub2Enabled;
                break;
            case 2:
                resId = this.mSub3ImageRes;
                isSubEnabled = this.mIsSub3Enabled;
                break;
            default:
                HwLog.w("CallLogAdapter", "Unknown subscription id: " + subscriptionID);
                break;
        }
        cardTypeView.setImageResource(resId);
        cardTypeView.setVisibility(0);
        cardTypeView.setContentDescription(getCardTypeContentDescription(resId));
        cardTypeView.setEnabled(isSubEnabled);
    }

    private String getCardTypeContentDescription(int resId) {
        String cardTypeContentDescription = "";
        if (resId == R.drawable.stat_sys_sim1) {
            return this.mContext.getString(R.string.str_filter_sim1);
        }
        if (resId == R.drawable.stat_sys_sim2) {
            return this.mContext.getString(R.string.str_filter_sim2);
        }
        return cardTypeContentDescription;
    }

    public void updateSimStatesAndResources() {
        if (this.mIsDualSim) {
            boolean z;
            boolean isSim1Enabled = SimFactoryManager.isSIM1CardPresent() && SimFactoryManager.isSimEnabled(0);
            boolean isSim2Enabled = SimFactoryManager.isSIM2CardPresent() && SimFactoryManager.isSimEnabled(1);
            if (isSim1Enabled) {
                z = isSim2Enabled;
            } else {
                z = false;
            }
            this.mBothSimEnabled = z;
            boolean isSecondSubIdle = true;
            boolean isFirstSubIdle = true;
            if (!CommonConstants.sRo_config_hw_dsda) {
                isFirstSubIdle = !SimFactoryManager.phoneIsOffhook(0);
                isSecondSubIdle = !SimFactoryManager.phoneIsOffhook(1);
            }
            isSim1Enabled = isSim1Enabled ? isSecondSubIdle : false;
            isSim2Enabled = isSim2Enabled ? isFirstSubIdle : false;
            this.mIsSub1Enabled = false;
            this.mIsSub2Enabled = false;
            this.mIsSub3Enabled = false;
            initVoiceMailNumber();
            if (this.mSIMcombination == 4 || this.mSIMcombination == 1 || this.mSIMcombination == 3) {
                this.mSub1ImageRes = R.drawable.stat_sys_sim1;
                this.mSub2ImageRes = R.drawable.stat_sys_sim2;
                if (isSim1Enabled) {
                    this.mIsSub1Enabled = true;
                }
                if (isSim2Enabled) {
                    this.mIsSub2Enabled = true;
                }
            } else if (this.mSIMcombination == 2) {
                if (SimFactoryManager.getSubscriptionIdBasedOnSlot(0) == 1) {
                    this.mSub1ImageRes = R.drawable.stat_sys_sim1;
                    this.mSub2ImageRes = R.drawable.stat_sys_sim2;
                    this.mSub3ImageRes = R.drawable.stat_sys_sim2;
                    if (isSim1Enabled) {
                        this.mIsSub3Enabled = true;
                    }
                } else if (SimFactoryManager.getSubscriptionIdBasedOnSlot(0) == 0) {
                    this.mSub1ImageRes = R.drawable.stat_sys_sim1;
                    this.mSub2ImageRes = R.drawable.stat_sys_sim2;
                    this.mSub3ImageRes = R.drawable.stat_sys_sim2;
                    if (isSim1Enabled) {
                        this.mIsSub1Enabled = true;
                    }
                    if (isSim2Enabled) {
                        this.mIsSub2Enabled = true;
                    }
                } else if (SimFactoryManager.getSubscriptionIdBasedOnSlot(0) == -1) {
                    this.mSub1ImageRes = R.drawable.stat_sys_sim1;
                    this.mSub2ImageRes = R.drawable.stat_sys_sim2;
                    this.mSub3ImageRes = R.drawable.stat_sys_sim2;
                    if (isSim2Enabled) {
                        this.mIsSub2Enabled = true;
                    }
                }
            }
            return;
        }
        initVoiceMailNumber();
    }

    private void getVoiceMailNumberInContactListHelper() {
        try {
            ContactListHelper helper = ((PeopleActivity) this.mContext).getContactListHelper();
            this.mSim1VoiceMailNumber = helper.getSim1VoiceMailNumber();
            this.mSim2VoiceMailNumber = helper.getSim2VoiceMailNumber();
        } catch (SecurityException e) {
            HwLog.w("CallLogAdapter", "SecurityException is thrown. Maybe privilege isn't sufficient.");
        }
    }

    private void initVoiceMailNumber() {
        if (this.mContext instanceof PeopleActivity) {
            getVoiceMailNumberInContactListHelper();
            return;
        }
        try {
            this.mSim1VoiceMailNumber = SimFactoryManager.getVoiceMailNumber(SimFactoryManager.getSubscriptionIdBasedOnSlot(0));
            this.mSim2VoiceMailNumber = SimFactoryManager.getVoiceMailNumber(SimFactoryManager.getSubscriptionIdBasedOnSlot(1));
        } catch (SecurityException e) {
            HwLog.w("CallLogAdapter", "SecurityException is thrown. Maybe privilege isn't sufficient.");
        }
    }

    public boolean isVoiceMailNumber(String number) {
        boolean z = true;
        if (MultiUsersUtils.isCurrentUserGuest()) {
            return false;
        }
        if (mCust != null && mCust.isLongVoiceMailNumber(number)) {
            return true;
        }
        number = PhoneNumberUtils.extractNetworkPortionAlt(number);
        if (TextUtils.isEmpty(number)) {
            z = false;
        } else if (!PhoneNumberUtils.compare(number, this.mSim1VoiceMailNumber)) {
            z = PhoneNumberUtils.compare(number, this.mSim2VoiceMailNumber);
        }
        return z;
    }

    private boolean callLogInfoMatches(ContactInfo callLogInfo, ContactInfo info) {
        boolean z = false;
        if (callLogInfo == null || info == null) {
            return false;
        }
        if (TextUtils.equals(callLogInfo.name, info.name) && callLogInfo.type == info.type && TextUtils.equals(callLogInfo.label, info.label)) {
            z = TextUtil.equals(callLogInfo.lookupUri, info.lookupUri);
        }
        return z;
    }

    private void updateCallLogContactInfoCache(String number, String countryIso, ContactInfo updatedInfo, ContactInfo callLogInfo) {
        ContentValues values = new ContentValues();
        if (mCust != null) {
            mCust.updateCallLogContactInfoCache();
        }
        if (this.mSpecialNumbersMap == null || !this.mSpecialNumbersMap.containsKey(number)) {
            boolean needsUpdate;
            if (callLogInfo != null) {
                needsUpdate = convertCallLogInfoToCV(values, updatedInfo, callLogInfo);
            } else {
                values.put("name", updatedInfo.name);
                values.put("numbertype", Integer.valueOf(updatedInfo.type));
                values.put("numberlabel", updatedInfo.label);
                values.put("lookup_uri", UriUtils.uriToString(updatedInfo.lookupUri));
                values.put("matched_number", updatedInfo.number);
                values.put("normalized_number", updatedInfo.normalizedNumber);
                values.put("photo_id", Long.valueOf(updatedInfo.photoId));
                values.put("formatted_number", updatedInfo.formattedNumber);
                needsUpdate = true;
            }
            if (updatedInfo.normalizedNumber == null || updatedInfo.normalizedNumber.length() == 0) {
                RoamingLearnManage.getInstance().addCallLog(this.mContext, number);
            }
            if (needsUpdate) {
                if (countryIso == null) {
                    this.mContext.getContentResolver().update(QueryUtil.getCallsContentUri(), values, "number = ? AND countryiso IS NULL", new String[]{number});
                } else {
                    this.mContext.getContentResolver().update(QueryUtil.getCallsContentUri(), values, "number = ? AND countryiso = ?", new String[]{number, countryIso});
                }
            }
        }
    }

    private boolean convertCallLogInfoToCV(ContentValues values, ContactInfo updatedInfo, ContactInfo callLogInfo) {
        boolean needsUpdate = false;
        boolean isNameUpdateRequried = mCust != null ? mCust.isNameUpdateRequried(updatedInfo, callLogInfo) : true;
        if (!TextUtils.equals(updatedInfo.name, callLogInfo.name) && isNameUpdateRequried) {
            values.put("name", updatedInfo.name);
            needsUpdate = true;
        }
        if (updatedInfo.type != callLogInfo.type) {
            values.put("numbertype", Integer.valueOf(updatedInfo.type));
            needsUpdate = true;
        }
        if (!TextUtils.equals(updatedInfo.label, callLogInfo.label)) {
            values.put("numberlabel", updatedInfo.label);
            needsUpdate = true;
        }
        if (!UriUtils.areEqual(updatedInfo.lookupUri, callLogInfo.lookupUri)) {
            values.put("lookup_uri", UriUtils.uriToString(updatedInfo.lookupUri));
            needsUpdate = true;
        }
        if (!TextUtils.equals(updatedInfo.normalizedNumber, callLogInfo.normalizedNumber)) {
            values.put("normalized_number", updatedInfo.normalizedNumber);
            needsUpdate = true;
        }
        if (!TextUtils.equals(updatedInfo.number, callLogInfo.mCachedMatchedNumber)) {
            values.put("matched_number", updatedInfo.number);
            needsUpdate = true;
        }
        if (updatedInfo.photoId != callLogInfo.photoId) {
            values.put("photo_id", Long.valueOf(updatedInfo.photoId));
            needsUpdate = true;
        }
        if (TextUtils.equals(updatedInfo.formattedNumber, callLogInfo.formattedNumber)) {
            return needsUpdate;
        }
        values.put("formatted_number", updatedInfo.formattedNumber);
        return true;
    }

    @VisibleForTesting
    void disableRequestProcessingForTest() {
        this.mRequestProcessingDisabled = true;
    }

    @VisibleForTesting
    void injectContactInfoForTest(String number, String countryIso, ContactInfo contactInfo) {
        this.mContactInfoCache.put(new NumberWithCountryIso(number, countryIso), contactInfo);
    }

    public String getBetterNumberFromContacts(String number, String countryIso) {
        String str = null;
        ContactInfo ci = (ContactInfo) this.mContactInfoCache.getPossiblyExpired(new NumberWithCountryIso(number, countryIso));
        if (ci == null || ci == ContactInfo.EMPTY) {
            try {
                Uri uri = QueryUtil.getPhoneLookupUri(number);
                Cursor phonesCursor = this.mContext.getContentResolver().query(uri, PhoneQuery.getPhoneLookupProjection(uri), null, null, null);
                if (phonesCursor != null) {
                    if (phonesCursor.moveToFirst()) {
                        str = phonesCursor.getString(4);
                    }
                    phonesCursor.close();
                }
            } catch (Exception e) {
            }
        } else {
            str = ci.number;
        }
        if (TextUtils.isEmpty(str)) {
            return number;
        }
        if (str.startsWith("+") || str.length() > number.length()) {
            return str;
        }
        return number;
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (this.mDialerListener != null) {
            this.mDialerListener.onScrollCallLog(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public void onScrollStateChanged(AbsListView listView, int scrollState) {
        boolean z;
        boolean prevFling = this.fling;
        this.mListView = listView;
        boolean isFocus = !this.mPollListStateHandler.hasMessages(2);
        if (scrollState == 2) {
            z = true;
        } else {
            z = false;
        }
        this.fling = z;
        this.mCallLogViewsHelper.setFlingMode(this.fling);
        this.mPollListStateHandler.removeMessages(1);
        this.mPollListStateHandler.removeMessages(3);
        if (scrollState == 0) {
            if (isFocus) {
                this.mPollListStateHandler.sendMessageDelayed(this.mPollListStateHandler.obtainMessage(3), 0);
            } else {
                this.isInvalidateViewsRequired = true;
            }
            this.mPollListStateHandler.sendMessageDelayed(this.mPollListStateHandler.obtainMessage(1), 1000);
            int header = ((ListView) this.mListView).getHeaderViewsCount();
            int firstVisiblePosition = this.mListView.getFirstVisiblePosition() - header;
            int lastVisiblePosition = this.mListView.getLastVisiblePosition() - header;
            if (this.mCurrentlyExpandedPosition != -1 && (this.mCurrentlyExpandedPosition < firstVisiblePosition || this.mCurrentlyExpandedPosition > lastVisiblePosition)) {
                HwLog.d("CallLogAdapter", "f : " + firstVisiblePosition + ", l : " + lastVisiblePosition + " , c : " + this.mCurrentlyExpandedPosition);
                if (this.mVoicemailPlaybackPresenter != null) {
                    this.mVoicemailPlaybackPresenter.pausePlayback();
                }
                this.mCurrentlyExpandedPosition = -1;
                this.mCurrentlyExpandedRowId = -1;
                this.lastOpen = null;
            }
        } else {
            this.mStayInIdle = false;
        }
        if (!this.fling && prevFling) {
            initVoiceMailNumber();
            this.isInvalidateViewsRequired = true;
        }
        if (this.mDialerListener != null) {
            this.mDialerListener.onScrollStateChangedCallLog(listView, scrollState);
        }
    }

    public void notifyDataSetChanged() {
        if (!this.fling || this.mCountSizeChanged) {
            super.notifyDataSetChanged();
        }
    }

    public void refreshListView(AbsListView listView) {
        if (this.mStayInIdle && !this.mPollListStateHandler.hasMessages(2)) {
            listView.invalidateViews();
        }
    }

    public boolean isIdle() {
        return this.mStayInIdle;
    }

    public void clearClickedFlag() {
        this.mItemClicked = false;
        this.mSelectCallLogId = -1;
    }

    public long getClickedCalllogID() {
        return this.mSelectCallLogId;
    }

    public void setClickedCalllogID(long id) {
        this.mSelectCallLogId = id;
    }

    public boolean isInvalidateRequired() {
        return this.isInvalidateViewsRequired;
    }

    public void clearInvalidateRequiredFlag() {
        this.isInvalidateViewsRequired = false;
        this.mPollListStateHandler.removeMessages(2);
    }

    public void setListViewFocusGone() {
        this.mPollListStateHandler.sendEmptyMessageDelayed(2, 500);
    }

    public void setCallLogAdapterListener(CallLogAdapterListener aDialer) {
        this.mDialerListener = aDialer;
    }

    private static ContactInfo getContactInfoFromCallLog(Cursor aCursor, int aCount) {
        boolean z = true;
        ContactInfo info = new ContactInfo();
        info.mCachedLookUpUriString = aCursor.getString(11);
        info.lookupUri = UriUtils.parseUriOrNull(info.mCachedLookUpUriString);
        info.name = aCursor.getString(8);
        info.type = aCursor.getInt(9);
        info.label = aCursor.getString(10);
        String matchedNumber = aCursor.getString(12);
        info.mCachedMatchedNumber = matchedNumber;
        String postDialDigits = CompatUtils.isNCompatible() ? aCursor.getString(CallLogQuery.POST_DIAL_DIGITS) : "";
        if (matchedNumber == null) {
            matchedNumber = aCursor.getString(1) + postDialDigits;
        }
        info.number = matchedNumber;
        info.normalizedNumber = aCursor.getString(13);
        info.photoId = aCursor.getLong(14);
        info.photoUri = null;
        info.formattedNumber = aCursor.getString(15);
        info.mGeoLocation = aCursor.getString(7);
        if (aCursor.getInt(16) != 0) {
            z = false;
        }
        info.mIsNew = z;
        info.mCallTypes = getCallTypes(aCursor, aCount);
        return info;
    }

    public static int[] getCallTypes(Cursor aCursor, int aCount) {
        int[] callTypes = new int[aCount];
        callTypes[0] = aCursor.getInt(4);
        if (1 == aCount) {
            return callTypes;
        }
        int position = aCursor.getPosition();
        for (int index = 1; index < aCount; index++) {
            aCursor.moveToNext();
            callTypes[index] = aCursor.getInt(4);
        }
        aCursor.moveToPosition(position);
        return callTypes;
    }

    private void initCust() {
        if (EmuiFeatureManager.isProductCustFeatureEnable() && mCust == null) {
            mCust = (HwCustCallLogAdapter) HwCustUtils.createObj(HwCustCallLogAdapter.class, new Object[]{this.mContext.getApplicationContext()});
        }
    }

    public static HwCustCallLogAdapter getCust() {
        return mCust;
    }

    public static void releaseCust() {
        mCust = null;
    }

    public long getCallsFeaturesValue(Cursor c) {
        int callsFeatureValueIndex = c.getColumnIndex("features");
        if (callsFeatureValueIndex >= 0) {
            return c.getLong(callsFeatureValueIndex);
        }
        return 0;
    }

    public void setVoicemailNumber(String vm1, String vm2) {
        this.mSim1VoiceMailNumber = vm1;
        this.mSim2VoiceMailNumber = vm2;
    }

    public static void updateEmergencyNumberCache() {
        sEmergencyNumberCache.clear();
        sNonEmergencyNumberCash.clear();
    }

    public void onAutoCollapse() {
        if (this.lastOpen != null) {
            AbstractExpandableViewAdapter.animateView(this.lastOpen, 1);
        }
        this.mCurrentlyExpandedPosition = -1;
        this.mCurrentlyExpandedRowId = -1;
        this.lastOpen = null;
    }

    public void onVoicemailMenuDeleted(CallLogListItemViews view) {
        if (this.mCurrentlyExpandedPosition != -1 && this.mVoicemailPlaybackPresenter != null && this.mCurrentlyExpandedPosition == view.getAdapterPosition()) {
            HwLog.d("CallLogAdapter", "onVoicemailMenuDeleted,pausePlayback");
            this.mVoicemailPlaybackPresenter.pausePlayback();
        }
    }

    public void onVoicemailDeleted(Uri uri) {
        if (this.mCallFetcher instanceof Fragment) {
            VoicemailDeleteDialog.show(((Fragment) this.mCallFetcher).getFragmentManager(), uri);
        }
    }

    public void onVoicemailDeletedInDatabase() {
    }

    private void decideIfCallLogHdIconIsShown(ImageView hdIcon, int hdType) {
        if (EmuiFeatureManager.isContactDialpadHdIconOn() && hdIcon != null && this.mContext != null) {
            hdType &= 15;
            switch (hdType) {
                case 0:
                    hdIcon.setVisibility(8);
                    break;
                case 1:
                    hdIcon.setImageResource(R.drawable.ic_contacts_calllog_hd);
                    hdIcon.setContentDescription(this.mContext.getString(R.string.content_description_hd_call));
                    LayoutParams hdLp = hdIcon.getLayoutParams();
                    hdLp.width = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_hd_icon_width);
                    hdLp.height = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_hd_icon_height);
                    hdIcon.setLayoutParams(hdLp);
                    hdIcon.setVisibility(0);
                    break;
                case 2:
                    hdIcon.setImageResource(R.drawable.ic_contacts_calllog_volte);
                    hdIcon.setContentDescription(this.mContext.getString(R.string.content_description_volte_call));
                    LayoutParams volteLp = hdIcon.getLayoutParams();
                    volteLp.width = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_volte_icon_width);
                    volteLp.height = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_volte_icon_height);
                    hdIcon.setLayoutParams(volteLp);
                    hdIcon.setVisibility(0);
                    break;
                case 3:
                    hdIcon.setImageResource(R.drawable.ic_contacts_calllog_vowifi);
                    LayoutParams vowifiLp = hdIcon.getLayoutParams();
                    vowifiLp.width = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_vowifi_icon_width);
                    vowifiLp.height = this.mContext.getResources().getDimensionPixelSize(R.dimen.call_log_vowifi_icon_height);
                    hdIcon.setLayoutParams(vowifiLp);
                    hdIcon.setVisibility(0);
                    break;
                default:
                    hdIcon.setVisibility(8);
                    if (HwLog.HWFLOW) {
                        HwLog.i("CallLogAdapter", "call log hdType = " + hdType + " not legal");
                        break;
                    }
                    break;
            }
        }
    }

    private boolean isCopyTextToEditText(boolean doubleSimCardEnabled) {
        if (!HapEncryptCallUtils.isEncryptCallEnabled()) {
            return false;
        }
        if (SimFactoryManager.isExtremeSimplicityMode()) {
            return HapEncryptCallUtils.isEncryptCallCard(SimFactoryManager.getDefaultSimcard());
        }
        if (doubleSimCardEnabled) {
            return true;
        }
        if (CommonUtilMethods.getFirstSimEnabled()) {
            return HapEncryptCallUtils.isCallCard1Encrypt();
        }
        if (CommonUtilMethods.getSecondSimEnabled()) {
            return HapEncryptCallUtils.isCallCard2Encrypt();
        }
        return false;
    }

    private void updateEncryptCallView(PhoneCallDetailsViews phoneCallDetailsViews, Cursor c) {
        int i = 0;
        if (phoneCallDetailsViews != null) {
            int encryptCallColumnIndex = c.getColumnIndex("encrypt_call");
            if (encryptCallColumnIndex >= 0) {
                int encryptCall = c.getInt(encryptCallColumnIndex);
                EncryptPhoneCallDetailsViews viewCust = phoneCallDetailsViews.getEncryptPhoneCallDetailsView();
                if (viewCust != null) {
                    ImageView imageView = viewCust.getEncryptCallView();
                    if (imageView != null) {
                        if (encryptCall != 1) {
                            i = 8;
                        }
                        imageView.setVisibility(i);
                    }
                }
            }
        }
    }
}
