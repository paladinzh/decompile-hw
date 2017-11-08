package com.android.contacts.dialpad;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.QuickContact;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.HwCarrierConfigManagerInner;
import android.telephony.HwTelephonyManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import com.amap.api.services.core.AMapException;
import com.android.contacts.CallUtil;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsApplication.NetWorkSwitchListener;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GeoUtil;
import com.android.contacts.MotionRecognition;
import com.android.contacts.MotionRecognition.MotionEventHandler;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.PhoneCallDetailsHelper;
import com.android.contacts.PhoneCallDetailsViews;
import com.android.contacts.SpecialCharSequenceMgr;
import com.android.contacts.activities.ActionBarAdapter.TabState;
import com.android.contacts.activities.ContactDetailActivity.TranslucentActivity;
import com.android.contacts.activities.ContactEditorActivity;
import com.android.contacts.activities.ContactSelectionActivity;
import com.android.contacts.activities.DialtactsActivity;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.activities.RequestPermissionsActivityBase;
import com.android.contacts.calllog.CallLogAdapter.CallLogAdapterListener;
import com.android.contacts.calllog.CallLogFragment;
import com.android.contacts.calllog.CallLogFragment.CallLogTouch;
import com.android.contacts.calllog.CallTypeHelper;
import com.android.contacts.calllog.IntentProvider;
import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.compatibility.NumberLocationCache;
import com.android.contacts.compatibility.NumberLocationLoader;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.detail.EspaceDialer;
import com.android.contacts.dialpad.DialpadAnimateHelper.DialpadAnimatorListener;
import com.android.contacts.dialpad.DialpadKeyButton.KeyTimes;
import com.android.contacts.dialpad.DialpadKeyButton.OnPressedListener;
import com.android.contacts.dialpad.gravity.GravityView;
import com.android.contacts.dialpad.gravity.SingleHandAdapter;
import com.android.contacts.dialpad.gravity.SingleHandAdapter.AnimatorListener;
import com.android.contacts.fragment.HwBaseFragment;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.blacklist.BlacklistCommonUtils;
import com.android.contacts.hap.calllog.CallRecordUtils;
import com.android.contacts.hap.dialpad.EncryptCallDialpadFragmentHelper;
import com.android.contacts.hap.dialpad.FreqPrimaryActionView;
import com.android.contacts.hap.hwsearch.HwSearchCursor;
import com.android.contacts.hap.hwsearch.HwSearchCursor.HwSearchDialerCursor;
import com.android.contacts.hap.numbermark.NumberMarkUtil;
import com.android.contacts.hap.optimize.FragmentReplacer;
import com.android.contacts.hap.provider.ContactsAppDatabaseHelper.SpeedDialContract;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.RcseProfile;
import com.android.contacts.hap.rcs.dialer.RcsDialpadFragmentHelper;
import com.android.contacts.hap.receiver.ContactsPropertyChangeReceiver;
import com.android.contacts.hap.receiver.ContactsPropertyChangeReceiver.BigSceenLisener;
import com.android.contacts.hap.roaming.IsPhoneNetworkRoamingUtils;
import com.android.contacts.hap.roaming.RoamingDialPadDirectlyDataListener;
import com.android.contacts.hap.roaming.RoamingDialPadDirectlyManager;
import com.android.contacts.hap.service.NumberMarkInfo;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimStateListener;
import com.android.contacts.hap.sim.SimStateServiceHandler;
import com.android.contacts.hap.sim.SimStateServiceHandler.ISimStateCallback;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.util.HapEncryptCallUtils;
import com.android.contacts.hap.util.HwAnimationReflection;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.hap.util.RefelctionUtils;
import com.android.contacts.hap.util.SingleHandModeManager;
import com.android.contacts.hap.util.UnsupportedException;
import com.android.contacts.hap.utils.BackgroundGenricHandler;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.hap.utils.ScreenUtils;
import com.android.contacts.hap.utils.VoLteStatusObserver;
import com.android.contacts.hap.utils.VoLteStatusObserver.CallBack;
import com.android.contacts.hap.utils.VtLteUtils;
import com.android.contacts.hap.widget.HapViewPager;
import com.android.contacts.hap.widget.SplitActionBarView;
import com.android.contacts.hap.widget.SplitActionBarView.OnCustomMenuListener;
import com.android.contacts.hap.widget.SplitActionBarView.SetButtonDetails;
import com.android.contacts.speeddial.SpeedDialerActivity;
import com.android.contacts.speeddial.SpeedDialerFragment.DialPadState;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.EncryptCallUtils;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.ExpirableCache;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.PhoneNumberFormatter;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.util.ShowErrorDiallogUtils;
import com.android.contacts.util.TextUtil;
import com.android.contacts.widget.ButtonGroupLyout;
import com.android.contacts.widget.ButtonGroupLyout.RadioButtonListener;
import com.android.contacts.widget.DialpadHeaderLayout;
import com.android.contacts.widget.SuspentionScroller;
import com.android.dialer.greeting.GreetingContract$Greetings;
import com.android.dialer.util.ActiveAccountState;
import com.android.dialer.util.TelecomUtil;
import com.android.phone.common.HapticFeedback;
import com.google.android.gms.R;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacesStatusCodes;
import com.huawei.android.provider.SettingsEx.Systemex;
import com.huawei.android.telephony.MSimSmsManagerEx;
import com.huawei.android.telephony.PhoneStateListenerEx;
import com.huawei.contact.util.HwUtil;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.CustomStateListDrawable;
import com.huawei.cspcommon.util.DialerHighlighter;
import com.huawei.cspcommon.util.PhoneItem;
import com.huawei.cspcommon.util.SmartDialType;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCustUtils;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService.Stub;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class DialpadFragment extends HwBaseFragment implements OnClickListener, OnLongClickListener, OnKeyListener, OnItemClickListener, TextWatcher, OnScrollListener, SimStateListener, ISimStateCallback, CallLogTouch, MotionEventHandler, OnTouchListener, KeyTimes, OnPressedListener, DialpadCallBack, CallBack {
    private static final boolean ISLON = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static boolean isIpCallEnabled = CommonUtilMethods.isIpCallEnabled();
    private static HwCustDialpadFragmentHelper mCust = null;
    private static float mDownX = 0.0f;
    private static boolean mShowCallRecord;
    private Dialog addtocontactDialog = null;
    private ContentObserver airPlanModeOberver;
    private ArrayAdapter<String> arrayAdapter = null;
    private MyOnTouchListener avoidTouchToDelegateListener = new MyOnTouchListener();
    private boolean canClickDialpad = true;
    private boolean canDialOut = true;
    private OnClickListener changeHandModeOperationListener = new OnClickListener() {
        public void onClick(View v) {
            if (!DialpadFragment.this.mIsAnimationInProgress && DialpadFragment.this.canClickDialpad) {
                int viewId = v.getId();
                if (R.id.right_hand_mode_view == viewId) {
                    DialpadFragment.this.updateDialpadHandModeView(1, true, true);
                } else if (R.id.left_hand_mode_view == viewId) {
                    DialpadFragment.this.updateDialpadHandModeView(2, true, true);
                }
            }
        }
    };
    private OnClickListener dialButton1Listener;
    private OnClickListener dialButton2Listener;
    private Dialog dialVMDialog = null;
    private OnClickListener emptylistbuttonListener = new OnClickListener() {
        public void onClick(View v) {
            int itemid = ((ChoiceItem) v.getTag()).id;
            switch (itemid) {
                case 0:
                    if (!DialpadFragment.this.mHasClickedNewContact) {
                        DialpadFragment.this.mHasClickedNewContact = true;
                        DialpadFragment.this.mHandler.sendEmptyMessageDelayed(263, 500);
                        DialpadFragment.this.addNewContact();
                        DialpadFragment.this.mNewContactNumber = ContactsUtils.removeDashesAndBlanks(DialpadFragment.this.mDigits.getText().toString());
                        StatisticalHelper.sendReport((int) AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, CallInterceptDetails.UNBRANDED_STATE);
                        return;
                    }
                    return;
                case 2:
                    DialpadFragment.this.sendSmsMessage(DialpadFragment.this.mDigits.getText().toString());
                    StatisticalHelper.report(1112);
                    return;
                case 3:
                    DialpadFragment.this.mNewContactNumber = ContactsUtils.removeDashesAndBlanks(DialpadFragment.this.mDigits.getText().toString());
                    DialpadFragment.this.addExistContact();
                    return;
                case 4:
                    DialpadFragment.this.dialVideoCall();
                    return;
                default:
                    HwLog.w("DialpadFragment", "onItemClick: unexpected itemId: " + itemid);
                    return;
            }
        }
    };
    private boolean enableDelete = true;
    View fragmentView = null;
    boolean hasResponseLongClick = false;
    private OnClickListener ipDialButton1Listener;
    private OnClickListener ipDialButton2Listener;
    public boolean isCursorPositionFirst;
    private boolean isDigistHeadShown = false;
    private boolean isFromNotificationFlag = false;
    private boolean isLongPress = false;
    private boolean isRow0ShownWhenRecreate = false;
    private boolean isSimOneRecommended = false;
    private boolean isSimTwoRecommended = false;
    private Set<String> mActiveQueryStrings = new HashSet();
    private Activity mActivity;
    private DialPadListAdapter mAdapter;
    private LinearLayout mAddButtonIpCall;
    private View mAdditionalButtonsRow;
    private DialpadAnimateHelper mAnimateHelper = new DialpadAnimateHelper();
    private Animation[] mAnimation = new Animation[4];
    private boolean mBePause = false;
    private BroadcastReceiver mBroadcastReceiver = new MyBroadcastReceiver();
    private int mButtonGroupLayoutSelectedType = 1;
    private ButtonGroupLyout mButtonGroupLyout;
    private int mCachedListViewMarginBottom = -1;
    private View mCallLogFragmentView;
    private ContentObserver mCallLogObserver;
    boolean mCallLogScrollFirstVisibleItem = false;
    boolean mCallLogTouchHideDailpad = false;
    private boolean mCallNumberEnable = true;
    private String mCard1Name;
    private String mCard2Name;
    private LinearLayout mCardNameDial2;
    private LinearLayout mCardNameDial3;
    private ContentObserver mCardNameObserver;
    private boolean mClearDigitsOnStop;
    private ClipboardManager mClipboard;
    private int mCurMode = -1;
    private HwCustDialpadFragment mCustDialpadFragment = null;
    private boolean mDelayLoadingLoaded = false;
    private DelayedUpdateModeHandler mDelayedUpdateModeHandler;
    private View mDeleteButton;
    private MenuItem mDeleteCalllogMenu;
    private ImageButton mDialButton;
    private LinearLayout mDialButtonSwitcher1;
    private LinearLayout mDialButtonSwitcher2;
    private LinearLayout mDialButtonSwitcherLayout;
    private View mDialerContainer;
    private DialerHighlighter mDialerHighlighter;
    CallLogAdapterListener mDialerListener = new CallLogAdapterListener() {
        boolean mScrollStateFling = false;

        public void copyTextToEditText(String aNumber) {
            if (!CommonUtilMethods.isEmergencyNumber(aNumber, DialpadFragment.this.mIsPhoneMultiSim)) {
                DialpadFragment.this.updateRecommendedCard(aNumber);
            }
            DialpadFragment.this.setDigitsText(aNumber);
            DialpadFragment.this.mDigits.setSelection(DialpadFragment.this.mDigits.getText().length());
            DialpadFragment.this.mDigits.setCursorVisible(false);
            DialpadFragment.this.mClearDigitsOnStop = false;
            DialpadFragment.this.setDigitsVisible(true, false);
        }

        public void onScrollCallLog(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0) {
                DialpadFragment.this.mCallLogScrollFirstVisibleItem = true;
                return;
            }
            if (this.mScrollStateFling) {
                DialpadFragment.this.showListViewSelectedHeader(DialpadFragment.this.mHeaderView);
            }
            DialpadFragment.this.mCallLogScrollFirstVisibleItem = false;
        }

        public void onScrollStateChangedCallLog(AbsListView listView, int scrollState) {
            if (2 == scrollState) {
                this.mScrollStateFling = true;
            } else if (scrollState == 0) {
                this.mScrollStateFling = false;
            }
            CallLogFragment lCallLogFragment = DialpadFragment.this.getCallLogFragment();
            if (lCallLogFragment != null) {
                lCallLogFragment.getSuspentionScroller().onScrollStateChanged(listView, scrollState);
            }
        }
    };
    private View mDialpad;
    private Animation mDialpadAnimationDown;
    private AnimationListener mDialpadAnimationDownListener = new AnimationListener() {
        public void onAnimationStart(Animation animation) {
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragment", "mDialpadAnimationDownListener onAnimationStart");
            }
            DialpadFragment.this.setIsAnimationInProgress(true);
            if (DialpadFragment.this.mList != null && DialpadFragment.this.mHasSomethingInput) {
                LayoutParams lLayoutParams = DialpadFragment.this.mList.getLayoutParams();
                if (lLayoutParams.height != -1) {
                    lLayoutParams.width = -1;
                    lLayoutParams.height = -1;
                    DialpadFragment.this.setBottomMargin(lLayoutParams, DialpadFragment.this.getDialerHeight(false));
                    DialpadFragment.this.mList.setLayoutParams(lLayoutParams);
                }
                DialpadFragment.this.mHasSomethingInput = false;
            }
            DialpadFragment.this.mIgnoreSmartSearchListReDraw = false;
            DialpadFragment.this.resetCallLogListViewHeight();
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragment", "mDialpadAnimationDownListener onAnimationEnd mIsDialpadVisible :: " + DialpadFragment.this.mShowDialpad);
            }
            DialpadFragment.this.setIsAnimationInProgress(false);
            if (DialpadFragment.this.getActivity() != null) {
                if (DialpadFragment.this.mShowDialpad) {
                    DialpadFragment.this.showDialpad();
                } else {
                    DialpadFragment.this.setCallLogFragmentFullScreen(DialpadFragment.this.getCallLogFragment());
                }
            }
        }
    };
    private Animation mDialpadAnimationUp;
    private ViewGroup mDialpadHuaweiContainer;
    private View mDialpadLine;
    private EditText mDigits;
    private DialpadHeaderLayout mDigitsContainer;
    private boolean mDigitsFilledByIntent;
    private boolean mDigitsVisible = true;
    private View mEmptyFootView;
    private EmptyListAdapter mEmptyListAdapter;
    private View mEncryptCallView = null;
    private int mExtremeSimpleDefaultSimcard;
    private int mFilterType = 0;
    private boolean mFirstPress = false;
    private int mFirstVisiblePosition = 0;
    private boolean mFlagAllRegisted = false;
    private float mFriction = 0.0075f;
    private GestureDetector mGestureDetector;
    private FrameLayout mGravityParent = null;
    private MenuItem mGreetingMenu;
    private boolean mGreetingMenuVisible = false;
    private Handler mHandler;
    private HapticFeedback mHaptic;
    private boolean mHasClickedNewContact;
    private boolean mHasSomethingInput = false;
    private View mHeaderView = null;
    private boolean mIgnoreSmartSearchListReDraw;
    private final Runnable mInitDialNumber = new Runnable() {
        public void run() {
            if (!DialpadFragment.this.isAdded() || !(DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_one) instanceof ImageView)) {
                return;
            }
            if (DialpadFragment.this.mActivity == null || !CommonUtilMethods.isNotSupportRippleInLargeTheme(DialpadFragment.this.mActivity.getResources())) {
                ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_one)).setImageResource(R.drawable.dial_num_1_blk);
                ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_two)).setImageResource(R.drawable.dial_num_2_blk);
                ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_three)).setImageResource(R.drawable.dial_num_3_blk);
                ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_four)).setImageResource(R.drawable.dial_num_4_blk);
                ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_five)).setImageResource(R.drawable.dial_num_5_blk);
                ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_six)).setImageResource(R.drawable.dial_num_6_blk);
                ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_seven)).setImageResource(R.drawable.dial_num_7_blk);
                ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_eight)).setImageResource(R.drawable.dial_num_8_blk);
                ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_nine)).setImageResource(R.drawable.dial_num_9_blk);
                ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_star)).setImageResource(R.drawable.xing);
                ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_zero)).setImageResource(R.drawable.dial_num_0_blk);
                ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_pound)).setImageResource(R.drawable.jing);
                return;
            }
            ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_one)).setImageResource(R.drawable.dial_num_1);
            ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_two)).setImageResource(R.drawable.dial_num_2);
            ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_three)).setImageResource(R.drawable.dial_num_3);
            ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_four)).setImageResource(R.drawable.dial_num_4);
            ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_five)).setImageResource(R.drawable.dial_num_5);
            ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_six)).setImageResource(R.drawable.dial_num_6);
            ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_seven)).setImageResource(R.drawable.dial_num_7);
            ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_eight)).setImageResource(R.drawable.dial_num_8);
            ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_nine)).setImageResource(R.drawable.dial_num_9);
            ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_star)).setImageResource(R.drawable.dial_num_star);
            ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_zero)).setImageResource(R.drawable.dial_num_0);
            ((ImageView) DialpadFragment.this.fragmentView.findViewById(R.id.contacts_dialpad_pound)).setImageResource(R.drawable.dial_num_pound);
        }
    };
    private boolean mIsAnimationInProgress;
    private boolean mIsCallLogViewLoaded = false;
    private boolean mIsCalllogVisible = true;
    private boolean mIsDeleteCalllogMenuEnable = true;
    private boolean mIsDialpadHiddenWhileModeChange = false;
    private boolean mIsFillDataFromIntentIsActive = false;
    private boolean mIsGeoLocationDispEnabled;
    private boolean mIsLandMenuForSplitLoaded = false;
    private boolean mIsLandscape;
    private boolean mIsListViewLoaded = false;
    private boolean mIsPhoneMultiSim;
    private boolean mIsRegistered;
    private boolean mIsSim1Enabled;
    private boolean mIsSim1Present;
    private boolean mIsSim2Enabled;
    private boolean mIsSim2Present;
    private int mLastHeight = 0;
    private String mLastNewContactNumber = null;
    private String mLastNumberDialed = "";
    private Long mLastSelectCallLogId = Long.valueOf(-1);
    private Uri mLastSelectContactUri = null;
    private int mLastSelectHashCode = 0;
    private RelativeLayout mLeftHandLayout;
    private ImageButton mLeftHandModeButton;
    private ViewStub mLeftHandStub;
    private View mLineHorizontalTopTable0;
    private ListView mList;
    private View mListContainer;
    private final BigSceenLisener mListener = new BigSceenLisener() {
        public void onEnabledStatusChanged(boolean status) {
            if (status) {
                DialpadFragment.this.updateDialpadHandModeView(DialpadFragment.this.mSingleHandMode.getCurrentHandMode(), false, false);
            } else {
                DialpadFragment.this.setUpViewsVisibilityInSingleHandMode(-1, false, false);
            }
        }
    };
    private int mLoadedFlag = 0;
    private MotionRecognition mMReco = null;
    private int mMargintTop;
    private boolean mNeedInitSingleHandMode = true;
    private String mNewContactNumber = null;
    private ListView mNewContactOption;
    private float mNormalWidth;
    private boolean mOriginalLazyMode = true;
    private MenuItem mOverflowMenu;
    private String mPhoneAccountId;
    private MSimPhoneStateListener mPhoneStateLinstenerOne;
    private MSimPhoneStateListener mPhoneStateLinstenerTwo;
    private final HashSet<Integer> mPressedDialpadKeys = new HashSet(12);
    private String mProhibitedPhoneNumberRegexp;
    private String mQueryString = null;
    private RcsDialpadFragmentHelper mRcsCust = null;
    private ContentResolver mResolver;
    private Resources mResource;
    private RelativeLayout mRightHandLayout;
    private ImageButton mRightHandModeButton;
    private ViewStub mRightHandStub;
    private ResetCallLogListView mRunnable4CallLog = new ResetCallLogListView();
    private boolean mScrolling;
    private View mSearchButton;
    private MenuItem mSearchMenu;
    private final LoaderCallbacks<Cursor> mSearchTaskLoaderListener = new LoaderCallbacks<Cursor>() {
        public AsyncTaskLoader<Cursor> onCreateLoader(int id, Bundle args) {
            HwLog.d("DialpadFragment", "mSearchTaskLoaderListener onCreateLoader begin");
            DialpadFragment.this.mIgnoreSmartSearchListReDraw = false;
            CursorLoader loader = new SearchTaskLoader(DialpadFragment.this.mActivity, DialpadFragment.this.mAdapter, DialpadFragment.this.mQueryString);
            if (PLog.DEBUG) {
                PLog.d(0, "DialpadFragment mSearchTaskLoaderListener get loader ");
            }
            return loader;
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor aResult) {
            PLog.d(0, "DialpadFragment mSearchTaskLoaderListener onLoadFinished begin");
            if (aResult != null && DialpadFragment.this.isAdded()) {
                if (aResult.getCount() == 0 && DialpadFragment.this.mEmptyFootView != null) {
                    DialpadFragment.this.mList.removeFooterView(DialpadFragment.this.mEmptyFootView);
                    DialpadFragment.this.mEmptyFootView = null;
                }
                if (aResult.getCount() > 0 && DialpadFragment.this.mEmptyFootView == null) {
                    DialpadFragment.this.mEmptyFootView = CommonUtilMethods.addFootEmptyViewPortrait(DialpadFragment.this.mList, DialpadFragment.this.mActivity);
                }
                try {
                    DialpadFragment.this.mIgnoreSmartSearchListReDraw = false;
                    if (DialpadFragment.this.mList == null) {
                        DialpadFragment.this.loadListView();
                    }
                    if (DialpadFragment.this.mAdapter == null) {
                        DialpadFragment.this.mAdapter = new DialPadListAdapter(null, DialpadFragment.this.mActivity, 1);
                        DialpadFragment.this.mList.setAdapter(DialpadFragment.this.mAdapter);
                    }
                    DialpadFragment.this.mAdapter.invalidate();
                    if (!DialpadFragment.this.isDigitsEmpty()) {
                        DialpadFragment.this.showDigistHeader(true, false);
                    }
                    DialpadFragment.this.mAdapter.changeCursor(aResult);
                    DialpadFragment.this.updateDialpadList(DialpadFragment.this.mAdapter.getCursor());
                    if (DialpadFragment.this.mCustDialpadFragment != null) {
                        DialpadFragment.this.mCustDialpadFragment.showStateName(DialpadFragment.this.mShowDialpadLocation, DialpadFragment.this.mQueryString);
                    }
                    DialpadFragment.this.mAdapter.notifyDataSetChanged();
                    if (((PeopleActivity) DialpadFragment.this.mActivity).getCurrentTab() == TabState.DIALER) {
                        DialpadFragment.this.requestDigistFocus();
                    }
                    Object queryString = null;
                    if (loader instanceof SearchTaskLoader) {
                        queryString = ((SearchTaskLoader) loader).getQueryString();
                    }
                    if (DialpadFragment.this.mActiveQueryStrings.contains(queryString) && DialpadFragment.this.mList != null) {
                        DialpadFragment.this.mList.setSelection(0);
                        DialpadFragment.this.mActiveQueryStrings.remove(queryString);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PLog.d(0, "DialpadFragment mSearchTaskLoaderListener onLoadFinished end");
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
            DialpadFragment.this.mIgnoreSmartSearchListReDraw = false;
        }
    };
    private Long mSelectCallLogId = Long.valueOf(-1);
    private Uri mSelectContactUri = null;
    private int mSelectHashCode = 0;
    IHarassmentInterceptionService mService;
    private boolean mShowDialpad;
    private TextView mShowDialpadLocation;
    private boolean mShowEmergencyCallWhenNoSIM;
    private ShowErrorDiallogUtils mShowErrorDiallogUtils;
    int mSingleChoiceID = -1;
    private SingleHandAdapter mSingleHandAdapter;
    private SingleHandModeManager mSingleHandMode;
    private ContentObserver mSingleHandModeObserver;
    private ContentObserver mSingleHandModeSwitchObserver;
    private ContentObserver mSingleHandSmartObserver;
    private float mSingleHandWidth;
    private final LoaderCallbacks<Cursor> mSmartSearchLoaderListener = new LoaderCallbacks<Cursor>() {
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            SmartDialCursorLoader loader = new SmartDialCursorLoader(DialpadFragment.this.mActivity.getApplicationContext());
            if (DialpadFragment.this.mQueryString == null) {
                loader.configureQuery("");
            } else {
                loader.configureQuery(DialpadFragment.this.mQueryString);
            }
            return loader;
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (cursor != null && -1 != cursor.getColumnIndex("_id")) {
                if (HwLog.HWFLOW) {
                    HwLog.i("DialpadFragment", "mSmartSearchLoaderListener.onLoadFinished,data count=" + cursor.getCount());
                }
                DialpadFragment.this.mIgnoreSmartSearchListReDraw = false;
                if (DialpadFragment.this.mAdapter == null) {
                    DialpadFragment.this.mAdapter = new DialPadListAdapter(null, DialpadFragment.this.mActivity, 1);
                    DialpadFragment.this.mList.setAdapter(DialpadFragment.this.mAdapter);
                }
                DialpadFragment.this.mAdapter.changeCursor(cursor);
                DialpadFragment.this.mAdapter.notifyDataSetChanged();
                DialpadFragment.this.updateDialpadList(cursor);
                DialpadFragment.this.requestDigistFocus();
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
            if (DialpadFragment.this.mAdapter != null) {
                DialpadFragment.this.mAdapter.setMode(1);
                DialpadFragment.this.mAdapter.swapCursor(null);
            }
        }
    };
    private HashMap<Integer, DialPadState> mSpeedDialDataMap = new HashMap();
    private final LoaderCallbacks<Cursor> mSpeedDialLoaderListener = new LoaderCallbacks<Cursor>() {
        public Loader<Cursor> onCreateLoader(int aLoaderId, Bundle aBundle) {
            String lErrorMsg = "UnExpected request for loader with ID: " + aLoaderId;
            switch (aLoaderId) {
                case 1:
                    return new CursorLoader(DialpadFragment.this.mActivity, SpeedDialContract.CONTENT_URI, null, null, null, null);
                default:
                    throw new IllegalArgumentException(lErrorMsg);
            }
        }

        public void onLoadFinished(Loader<Cursor> aLoader, Cursor aCursor) {
            if (!(aCursor == null || !DialpadFragment.this.isAdded() || aLoader == null)) {
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragment", "mSpeedDialLoaderListener onLoadFinished " + aLoader.getId());
                }
                DialpadFragment.this.updateSpeedDialDataCache(aCursor, aLoader.getId());
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private HashMap<Integer, String> mSpeedDialPredefinedNumberMap = new HashMap();
    RoamingDialPadDirectlyDataListener mSpeedDirectlyDataListener = new RoamingDialPadDirectlyDataListener() {
        public void selectedDirectlyData(String number) {
            if (!TextUtils.isEmpty(number)) {
                DialpadFragment.this.callSpeedDialNumber(number);
            }
        }
    };
    private NetWorkSwitchListener mSwitchListener = null;
    private OnClickListener mSwitcherClickListener;
    private OnLongClickListener mSwitcherLongClickListener;
    private OnTouchListener mSwitcherTouchListener;
    private Object mSyncObject = new Object();
    private TableRow mTableRow0;
    private TableRow mTableRow1;
    private TableRow mTableRow2;
    private TableRow mTableRow3;
    private TableRow mTableRow4;
    private View mTableRowRcs;
    private DialpadTouchTonePlayer mTonePlayer;
    private float mTranslationX;
    private Handler mUpdate;
    private float mVelocityScale = 0.65f;
    private View mVerticalDivider;
    private ViewSwitcher mViewSwitcher;
    private VoLteStatusObserver mVoLteStatusObserver;
    private Dialog mVoiceMailDialog = null;
    private boolean mWasEmptyBeforeTextChange;
    private View mWriteSpaceView;
    private boolean mshowingaddToContactDialog;
    private boolean needShowDigistHeader = true;
    private boolean needUpdateRecommendedButtonState = false;
    private View overflowMenuButton;
    private PopupMenu popup = null;
    RadioButtonListener radioButtonListener = new RadioButtonListener() {
        public void onRadioButtonClick(int type) {
            switch (type) {
                case 1:
                    DialpadFragment.this.mButtonGroupLyout.setLeftButtonSelected();
                    DialpadFragment.this.setCallLogFilterType(0);
                    ExceptionCapture.reportScene(87);
                    StatisticalHelper.report(5025);
                    return;
                case 2:
                    DialpadFragment.this.mButtonGroupLyout.setMiddleButtonSelected();
                    DialpadFragment.this.setCallLogFilterType(3);
                    ExceptionCapture.reportScene(88);
                    StatisticalHelper.report(5023);
                    return;
                case 3:
                    DialpadFragment.this.mButtonGroupLyout.setRightButtonSelected();
                    DialpadFragment.this.setCallLogFilterType(10);
                    ExceptionCapture.reportScene(89);
                    StatisticalHelper.report(5024);
                    return;
                case 4:
                    DialpadFragment.this.setVoicemailFilterTab(true);
                    return;
                default:
                    if (HwLog.HWFLOW) {
                        HwLog.i("DialpadFragment", "the click is wrong");
                        return;
                    }
                    return;
            }
        }
    };
    private String saveRotateRcsDigits = "";
    private int saveRotateRcsState = 0;
    private int sim1DialRes = R.drawable.call_sim1_selector_dialer;
    private int sim2DialRes = R.drawable.call_sim2_selector_dialer;
    private SuspentionScroller suspentionScroller;
    private boolean visCreate = true;

    private class AirPlanModeObserver extends ContentObserver {
        public AirPlanModeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            DialpadFragment.this.updateButtonStates(true);
        }
    }

    public static final class AllContactListItemViews {
        private String mBlackListName = "";
        private TextView mCompanyView;
        private TextView mDefaultText;
        private View mDivider;
        private Uri mLookupUri;
        private TextView mName;
        private boolean mNeedMark;
        private String mNumber;
        public String mOriginMarkInfo;
        public PhoneCallDetailsViews mPhoneCallDetailsViews;
        private TextView mPhoneNum;
        private TextView mPhoneType;
        private View mPrimaryActionView;
        public ImageView mSecondaryActionView;
        public LinearLayout mSecondaryActionViewLayout = null;
        private ImageView mWorkProfileIcon;
    }

    private class CallLogObserver extends ContentObserver {
        public CallLogObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            DialpadFragment.this.setRefreshDataRequired();
            DialpadFragment.this.queryLastOutgoingCall();
            CallLogFragment callFragment = DialpadFragment.this.getCallLogFragment();
            if (callFragment != null) {
                callFragment.handleCallLogTableChange();
            }
        }
    }

    private static class CallRecordTask extends AsyncTask<Integer, Integer, Boolean> {
        private Context context;

        CallRecordTask(Context context) {
            this.context = context.getApplicationContext();
        }

        protected Boolean doInBackground(Integer... arg0) {
            boolean z = false;
            String[] allRecordPathCandidates = CallRecordUtils.getRecordStoragePaths(this.context);
            if (allRecordPathCandidates == null || allRecordPathCandidates.length == 0) {
                return Boolean.valueOf(false);
            }
            boolean hasContent = false;
            for (String recordStoreDir : allRecordPathCandidates) {
                String[] recordFileList = new File(recordStoreDir).list();
                if (recordFileList != null && recordFileList.length > 0) {
                    for (String s : recordFileList) {
                        if (s != null && !s.contentEquals(".nomedia")) {
                            hasContent = true;
                            break;
                        }
                    }
                }
            }
            if (hasContent) {
                z = ContactsUtils.isCallRecorderInstalled(this.context);
            }
            return Boolean.valueOf(z);
        }

        protected void onPostExecute(Boolean result) {
            if (result != null) {
                DialpadFragment.mShowCallRecord = result.booleanValue();
            }
        }
    }

    private class CardNameObserver extends ContentObserver {
        public CardNameObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            HwLog.i("DialpadFragment", "mCardNameObserver");
            DialpadFragment.this.getSettingsSIMName();
        }
    }

    public static class ChoiceItem {
        int id;
        int mIconId;
        String text;

        public ChoiceItem(String s, int i, int iconId) {
            this.text = s;
            this.id = i;
            this.mIconId = iconId;
        }
    }

    class DelayedUpdateModeHandler extends Handler {
        DelayedUpdateModeHandler() {
        }

        public void handleMessage(Message msg) {
            if (!DialpadFragment.this.isAdded() || DialpadFragment.this.isRemoving()) {
                HwLog.w("DialpadFragment", "dialpad is not show now, do not update");
                return;
            }
            switch (msg.what) {
                case 9000:
                    DialpadFragment.this.afterTextChangedDelay();
                    break;
                case PlacesStatusCodes.USAGE_LIMIT_EXCEEDED /*9001*/:
                    HwLog.i("DialpadFragment", "update single card dialpad bin mode message -> updateSingleCardButton");
                    DialpadFragment.this.updateSingleCardButton();
                    break;
            }
        }
    }

    private class DialButtonListener implements OnClickListener {
        boolean mIsFromEncryptCall;
        boolean mIsIPCallButton;
        int mSlotId;

        public DialButtonListener(DialpadFragment this$0, int slotId, boolean aIsIPCallButton) {
            this(slotId, aIsIPCallButton, false);
        }

        public DialButtonListener(int slotId, boolean aIsIPCallButton, boolean isFromEncryptCall) {
            this.mSlotId = slotId;
            this.mIsIPCallButton = aIsIPCallButton;
            this.mIsFromEncryptCall = isFromEncryptCall;
        }

        public void onClick(View v) {
            if (DialpadFragment.this.mShowDialpad) {
                if (this.mIsIPCallButton) {
                    DialpadFragment.this.ipcallButtonPressed(this.mSlotId);
                } else {
                    DialpadFragment.this.dialButtonPressed(this.mSlotId, this.mIsFromEncryptCall);
                }
                return;
            }
            DialpadFragment.this.setDigitsVisible(true, true);
        }
    }

    private final class DialPadListAdapter extends ResourceCursorAdapter {
        RoamingDialPadDirectlyDataListener dialPadDirectlyDataListener = new RoamingDialPadDirectlyDataListener() {
            public void selectedDirectlyData(String number) {
                if (!CommonUtilMethods.isEmergencyNumber(number, DialpadFragment.this.mIsPhoneMultiSim)) {
                    DialpadFragment.this.updateRecommendedCard(number);
                }
                DialpadFragment.this.setDigitsText(number);
                DialpadFragment.this.mDigits.setSelection(DialpadFragment.this.mDigits.getText().length());
                DialpadFragment.this.mDigits.setCursorVisible(false);
                DialpadFragment.this.mClearDigitsOnStop = false;
                DialpadFragment.this.setDigitsVisible(true, false);
            }
        };
        private boolean mCacheValid = false;
        private ExpirableCache<String, String> mChinaFormatNumberCache;
        private int mCount = 0;
        private int mEnterItemCount = 0;
        private int mEnterpriseIndex = Integer.MAX_VALUE;
        public boolean mIsUpdivider;
        private int mMode;
        private PhoneCallDetailsHelper mPhoneCallDetailsHelper;
        private int mPredefinedIndex = Integer.MAX_VALUE;
        private final OnClickListener mPrimaryActionListener = new OnClickListener() {
            public void onClick(View view) {
                View primaryActionview = null;
                if (view.getTag() instanceof IntentProvider) {
                    primaryActionview = view;
                }
                if (primaryActionview != null && (primaryActionview.getTag() instanceof IntentProvider)) {
                    IntentProvider intentProvider = (IntentProvider) primaryActionview.getTag();
                    if (intentProvider != null && DialpadFragment.this.canDialOut) {
                        DialpadFragment.this.canDialOut = false;
                        if (DialpadFragment.this.mHandler != null) {
                            DialpadFragment.this.mHandler.sendEmptyMessageDelayed(258, 500);
                        }
                        if (!DialpadFragment.this.isRcsLoginStatus()) {
                            DialpadFragment.this.mClearDigitsOnStop = true;
                        }
                        boolean isFirstSimEnabled = CommonUtilMethods.getFirstSimEnabled();
                        boolean isSecondSimEnabled = CommonUtilMethods.getSecondSimEnabled();
                        Intent intent = intentProvider.getIntent(DialpadFragment.this.mActivity);
                        if (intent == null || !intent.getBooleanExtra("EXTRA_IS_YELLOWPAGE_URI", false)) {
                            StatisticalHelper.report(3013);
                        } else {
                            StatisticalHelper.report(3014);
                        }
                        if (intent != null && intent.getIntExtra("EXTRA_FEATURE", 0) == 32) {
                            EspaceDialer.dialVoIpCall(DialpadFragment.this.getActivity(), PhoneNumberUtils.getNumberFromIntent(intent, DialPadListAdapter.this.mContext));
                        } else if (!DialpadFragment.this.mIsPhoneMultiSim) {
                            int aSimType = SimFactoryManager.getSlotidBasedOnSubscription(SimFactoryManager.getDefaultSubscription());
                            DialPadListAdapter.this.postNumberOnScreenOrTelephone(intent, aSimType);
                            StatisticalHelper.sendReport(3000, aSimType);
                        } else if (isFirstSimEnabled && isSecondSimEnabled) {
                            DialpadFragment.this.mExtremeSimpleDefaultSimcard = SimFactoryManager.getDefaultSimcard();
                            if (-1 != DialpadFragment.this.mExtremeSimpleDefaultSimcard) {
                                DialPadListAdapter.this.postNumberOnScreenOrTelephone(intent, DialpadFragment.this.mExtremeSimpleDefaultSimcard);
                                StatisticalHelper.sendReport(3000, DialpadFragment.this.mExtremeSimpleDefaultSimcard);
                                HwLog.i("DialpadFragment", "View.OnClickListener the subid is :" + DialpadFragment.this.mExtremeSimpleDefaultSimcard);
                            } else if (intent != null) {
                                StatisticalHelper.report(5034);
                                String number = PhoneNumberUtils.getNumberFromIntent(intent, DialPadListAdapter.this.mContext);
                                boolean isRoaming = IsPhoneNetworkRoamingUtils.isPhoneNetworkRoamging();
                                if (isRoaming) {
                                    String RoamingNumber = RoamingDialPadDirectlyManager.getDialpadRoamingNumber(DialPadListAdapter.this.mContext, number, intent, isRoaming, true, DialPadListAdapter.this.dialPadDirectlyDataListener);
                                    if (!TextUtils.isEmpty(RoamingNumber)) {
                                        number = RoamingNumber;
                                    } else {
                                        return;
                                    }
                                }
                                if (!CommonUtilMethods.isEmergencyNumber(number, DialpadFragment.this.mIsPhoneMultiSim)) {
                                    DialpadFragment.this.updateRecommendedCard(number);
                                }
                                DialpadFragment.this.setDigitsText(PhoneNumberFormatter.formatNumber(DialPadListAdapter.this.mContext, number));
                                DialpadFragment.this.mDigits.setSelection(DialpadFragment.this.mDigits.getText().length());
                                DialpadFragment.this.mDigits.setCursorVisible(false);
                                DialpadFragment.this.mClearDigitsOnStop = false;
                                DialpadFragment.this.setDigitsVisible(true, false);
                            }
                        } else if (isFirstSimEnabled && !isSecondSimEnabled) {
                            DialPadListAdapter.this.postNumberOnScreenOrTelephone(intent, 0);
                            StatisticalHelper.sendReport(3000, 0);
                            HwLog.i("DialpadFragment", "View.OnClickListener the subid is SimConstants.FIRST_SIM_SLOT");
                        } else if (!isFirstSimEnabled && isSecondSimEnabled) {
                            DialPadListAdapter.this.postNumberOnScreenOrTelephone(intent, 1);
                            StatisticalHelper.sendReport(3000, 1);
                            HwLog.i("DialpadFragment", "View.OnClickListener the subid is SimConstants.SECOND_SIM_SLOT");
                        } else if (!(isFirstSimEnabled || isSecondSimEnabled)) {
                            DialPadListAdapter.this.postNumberOnScreenOrTelephone(intent, 0);
                            StatisticalHelper.sendReport(3000, 0);
                            HwLog.i("DialpadFragment", "View.OnClickListener the subid is SimConstants.FIRST_SIM_SLOT");
                        }
                    }
                }
            }
        };
        private final OnClickListener mSecondaryActionListener = new OnClickListener() {
            public void onClick(View view) {
                Intent intent = ((IntentProvider) view.getTag()).getIntent(DialpadFragment.this.mActivity);
                if (intent != null) {
                    try {
                        if (intent.getBooleanExtra("is_enterprise_contact", false)) {
                            QuickContact.showQuickContact(DialpadFragment.this.mActivity, new Rect(), intent.getData(), 3, null);
                        } else {
                            intent.putExtra("intent_key_is_from_dialpad", true);
                            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                                DialpadFragment.this.mSelectCallLogId = Long.valueOf(intent.getLongExtra("EXTRA_ID_SELECTED", -1));
                                DialpadFragment.this.mSelectContactUri = intent.getData();
                                DialpadFragment.this.mSelectHashCode = intent.getIntExtra("contact_select_hashcode", 0);
                            }
                            boolean waitResult = false;
                            if (CommonUtilMethods.calcIfNeedSplitScreen() && (DialpadFragment.this.getActivity() instanceof PeopleActivity)) {
                                ((PeopleActivity) DialpadFragment.this.getActivity()).setNeedMaskDialpad(true);
                                waitResult = true;
                            }
                            if (waitResult) {
                                ((PeopleActivity) DialpadFragment.this.getActivity()).saveInstanceValues();
                                intent.setClass(DialPadListAdapter.this.mContext, TranslucentActivity.class);
                                DialpadFragment.this.getActivity().startActivityForResult(intent, 3002);
                            } else {
                                DialPadListAdapter.this.mContext.startActivity(intent);
                            }
                        }
                    } catch (ActivityNotFoundException e) {
                        HwLog.e("DialpadFragment", e.toString(), e);
                    }
                    StatisticalHelper.report(5031);
                }
            }
        };

        private void postNumberOnScreenOrTelephone(Intent intent, int sim_flot) {
            String str = null;
            try {
                if (RcsContactsUtils.isBBVersion() || !EmuiFeatureManager.isRcsFeatureEnable() || RcseProfile.getRcsService() == null || !RcseProfile.getRcsService().getLoginState()) {
                    startTelephoneActivity(intent, sim_flot);
                    return;
                }
                String phoneNumber = PhoneNumberUtils.getNumberFromIntent(intent, this.mContext);
                DialpadFragment dialpadFragment = DialpadFragment.this;
                if (intent != null) {
                    str = PhoneNumberFormatter.formatNumber(this.mContext, phoneNumber);
                }
                dialpadFragment.setDigitsText(str);
                DialpadFragment.this.mDigits.setSelection(DialpadFragment.this.mDigits.getText().length());
            } catch (Exception e) {
                HwLog.e("DialpadFragment", "failed to post number on screen");
            }
        }

        private void startTelephoneActivity(Intent intent, int sim_flot) {
            if (intent == null) {
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragment", "the parameter is null!");
                }
                return;
            }
            boolean isRoaming = IsPhoneNetworkRoamingUtils.isPhoneNetworkRoamging();
            if (isRoaming) {
                String RoamingNumber = RoamingDialPadDirectlyManager.getDialpadRoamingNumber(this.mContext, PhoneNumberUtils.getNumberFromIntent(intent, this.mContext), intent, isRoaming, false, null);
                if (!TextUtils.isEmpty(RoamingNumber)) {
                    intent.setData(Uri.fromParts("tel", RoamingNumber, null));
                } else {
                    return;
                }
            }
            try {
                MSimSmsManagerEx.setSimIdToIntent(intent, sim_flot);
            } catch (Exception e) {
                intent.putExtra("subscription", sim_flot);
                e.printStackTrace();
            }
            PhoneAccountHandle accountHandle = CallUtil.makePstnPhoneAccountHandleWithPrefix(false, sim_flot);
            if (accountHandle != null) {
                intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", accountHandle);
            }
            try {
                if (DialpadFragment.this.mCustDialpadFragment == null || !DialpadFragment.this.mCustDialpadFragment.checkAndInitCall(this.mContext, PhoneNumberUtils.getNumberFromIntent(intent, this.mContext))) {
                    DialpadFragment.this.mActivity.startActivity(intent);
                }
            } catch (ActivityNotFoundException e2) {
                HwLog.e("DialpadFragment", e2.toString(), e2);
            }
        }

        public DialPadListAdapter(Cursor aContactsCursor, Context aCtxt, int aMode) {
            super(aCtxt, R.layout.freq_call_list_row, aContactsCursor, false);
            this.mMode = aMode;
            Resources resources = aCtxt.getResources();
            this.mPhoneCallDetailsHelper = new PhoneCallDetailsHelper(aCtxt, new CallTypeHelper(resources), new PhoneNumberHelper(resources));
            this.mChinaFormatNumberCache = ExpirableCache.create(3000);
        }

        protected void invalidate() {
            this.mCacheValid = false;
        }

        protected void ensureCacheValid() {
            if (!this.mCacheValid) {
                Cursor cursor = getCursor();
                if (cursor != null) {
                    this.mCount = cursor.getCount();
                    if (QueryUtil.isProviderSupportHwSeniorSearch()) {
                        this.mPredefinedIndex = ((HwSearchCursor) cursor).getYellowPageFirstPosition();
                        this.mEnterpriseIndex = ((HwSearchDialerCursor) cursor).getEnterpriseFirstPosition();
                        this.mEnterItemCount = ((HwSearchDialerCursor) cursor).getEnterpriseItemCount();
                    }
                    if (this.mPredefinedIndex < Integer.MAX_VALUE && !(EmuiFeatureManager.isProductCustFeatureEnable() && (DialpadFragment.this.mCustDialpadFragment == null || DialpadFragment.this.mCustDialpadFragment.predefinedHeaderNotNeeded()))) {
                        this.mCount++;
                    }
                    this.mCacheValid = true;
                }
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            int offset = 1;
            if (!DialpadFragment.IS_CHINA_AREA || (DialpadFragment.this.mCustDialpadFragment != null && DialpadFragment.this.mCustDialpadFragment.predefinedHeaderNotNeeded())) {
                return super.getView(position, convertView, parent);
            }
            View view;
            ensureCacheValid();
            if (position == this.mPredefinedIndex) {
                view = getHeaderView(convertView, parent);
                View deviderView = view.findViewById(R.id.divider);
                if (deviderView != null) {
                    deviderView.setVisibility(8);
                }
            } else {
                this.mIsUpdivider = false;
                if (this.mPredefinedIndex > 0 && position == this.mPredefinedIndex - 1) {
                    this.mIsUpdivider = true;
                }
                if (position < this.mPredefinedIndex) {
                    offset = 0;
                }
                int curIndex = position - offset;
                if (curIndex == 0) {
                    PLog.d(0, "DialpadFragment getView begin");
                }
                if (getCursor().isClosed() || getCursor().moveToPosition(curIndex)) {
                    view = getView(convertView, parent);
                    if (curIndex == 0) {
                        PLog.d(17, "DialpadFragment getView end");
                    }
                } else {
                    throw new IllegalStateException("Couldn't move cursor to position " + offset);
                }
            }
            return view;
        }

        public View getView(View convertView, ViewGroup parent) {
            View v;
            if (convertView != null) {
                v = convertView;
            } else {
                v = newView(this.mContext, this.mCursor, parent);
            }
            bindView(v, this.mContext, this.mCursor);
            return v;
        }

        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
            invalidate();
        }

        public int getItemViewType(int position) {
            if (DialpadFragment.this.mCustDialpadFragment != null && DialpadFragment.this.mCustDialpadFragment.predefinedHeaderNotNeeded()) {
                return super.getItemViewType(position);
            }
            ensureCacheValid();
            if (position == this.mPredefinedIndex) {
                return 0;
            }
            return 1;
        }

        public int getViewTypeCount() {
            if (DialpadFragment.this.mCustDialpadFragment == null || !DialpadFragment.this.mCustDialpadFragment.predefinedHeaderNotNeeded()) {
                return 2;
            }
            return 1;
        }

        public View getHeaderView(View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = newHeaderView(this.mContext, parent);
            }
            bindHeaderView(view);
            return view;
        }

        protected View newHeaderView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.directory_header, parent, false);
        }

        protected void bindHeaderView(View view) {
            ((TextView) view.findViewById(R.id.label)).setText(CommonUtilMethods.upPercase(DialpadFragment.this.getResources().getString(R.string.contact_yellowpage_title)));
        }

        public View newView(Context aContext, Cursor aCursor, ViewGroup aParent) {
            View lview;
            if (DialpadFragment.this.getActivity() instanceof PeopleActivity) {
                lview = ((PeopleActivity) DialpadFragment.this.getActivity()).getDialpadFragmentHelper().getNewView();
            } else {
                lview = super.newView(aContext, aCursor, aParent);
            }
            AllContactListItemViews views = new AllContactListItemViews();
            views.mName = (TextView) lview.findViewById(R.id.name);
            views.mPhoneType = (TextView) lview.findViewById(R.id.phone_type);
            views.mPhoneNum = (TextView) lview.findViewById(R.id.number);
            views.mDefaultText = (TextView) lview.findViewById(R.id.default_text);
            views.mPrimaryActionView = lview.findViewById(R.id.primary_action_view);
            views.mSecondaryActionView = (ImageView) lview.findViewById(R.id.secondary_action_icon);
            views.mSecondaryActionViewLayout = (LinearLayout) lview.findViewById(R.id.secondary_action_icon_layout);
            views.mPrimaryActionView.setOnClickListener(this.mPrimaryActionListener);
            views.mSecondaryActionViewLayout.setOnClickListener(this.mSecondaryActionListener);
            views.mPhoneCallDetailsViews = PhoneCallDetailsViews.fromView(lview);
            views.mCompanyView = (TextView) lview.findViewById(R.id.company);
            views.mWorkProfileIcon = (ImageView) lview.findViewById(R.id.work_profile_icon);
            views.mDivider = lview.findViewById(R.id.item_divider);
            if (views.mPrimaryActionView instanceof FreqPrimaryActionView) {
                FreqPrimaryActionView primaryView = (FreqPrimaryActionView) views.mPrimaryActionView;
                primaryView.bindViews(views, views.mPrimaryActionView.findViewById(R.id.freq_call_log_list_item_child_content), lview.findViewById(R.id.list_item_shadow), views.mDivider);
                int itemMaxWidth = DialpadFragment.this.mResource.getDisplayMetrics().widthPixels;
                if (DialpadFragment.this.mResource.getConfiguration().orientation == 2) {
                    itemMaxWidth /= 2;
                }
                primaryView.bindWidth(itemMaxWidth);
            }
            lview.setTag(views);
            return lview;
        }

        public void bindView(View aView, Context aContext, Cursor aCursorInfo) {
            if (!DialpadFragment.this.mIgnoreSmartSearchListReDraw || DialpadFragment.this.mScrolling) {
                String lNameStr;
                String lNumberStr;
                boolean isVoiceMailNum;
                TextView numberView;
                AllContactListItemViews views = (AllContactListItemViews) aView.getTag();
                if (views.mPrimaryActionView instanceof FreqPrimaryActionView) {
                    ((FreqPrimaryActionView) views.mPrimaryActionView).setDividerisVisible(this.mIsUpdivider);
                }
                views.mDefaultText.setVisibility(8);
                views.mDefaultText.setText("");
                views.mNeedMark = false;
                int numPresention = 1;
                String normalized_numebr = null;
                int phoneNumberType = 0;
                String str = null;
                int matchType = 0;
                String key = null;
                long contactId = -1;
                String lookupKey = "";
                long date = -1;
                long duration = -1;
                String countryIso = null;
                String geoCodedLocation = null;
                int callType = 0;
                int calllogId = 0;
                String lookup = null;
                CharSequence formattedNumber = null;
                String str2 = null;
                String str3 = null;
                boolean isCloudMark = false;
                int markCount = 0;
                int callFeature = DialpadFragment.this.getCallsFeaturesValue(aCursorInfo);
                synchronized (DialpadFragment.this.mSyncObject) {
                    if (this.mMode == 0) {
                        lNameStr = aCursorInfo.getString(1);
                        lNumberStr = aCursorInfo.getString(5);
                        phoneNumberType = aCursorInfo.getInt(6);
                        str = aCursorInfo.getString(7);
                        contactId = aCursorInfo.getLong(0);
                        lookupKey = aCursorInfo.getString(4);
                    } else {
                        lookup = aCursorInfo.getString(10);
                        if (lookup == null) {
                            lNameStr = aCursorInfo.getString(8);
                            lNumberStr = aCursorInfo.getString(1);
                            numPresention = aCursorInfo.getInt(11);
                            normalized_numebr = aCursorInfo.getString(12);
                            formattedNumber = aCursorInfo.getString(9);
                            date = aCursorInfo.getLong(2);
                            duration = aCursorInfo.getLong(3);
                            countryIso = aCursorInfo.getString(5);
                            geoCodedLocation = aCursorInfo.getString(7);
                            if (PhoneCapabilityTester.isGeoCodeFeatureEnabled(this.mContext) && !QueryUtil.checkGeoLocation(geoCodedLocation, lNumberStr)) {
                                geoCodedLocation = NumberLocationCache.getLocation(lNumberStr);
                                if (geoCodedLocation == null) {
                                    geoCodedLocation = "";
                                }
                            }
                            if (TextUtils.isEmpty(geoCodedLocation)) {
                                geoCodedLocation = aContext.getResources().getString(R.string.numberLocationUnknownLocation2);
                            }
                            callType = aCursorInfo.getInt(4);
                            calllogId = aCursorInfo.getInt(0);
                            if (SmartDialType.getMarkContentColumnIndex() < aCursorInfo.getColumnCount()) {
                                str3 = aCursorInfo.getString(SmartDialType.getMarkContentColumnIndex());
                            }
                            if (SmartDialType.getMarkTypeColumnIndex() < aCursorInfo.getColumnCount()) {
                                str2 = aCursorInfo.getString(SmartDialType.getMarkTypeColumnIndex());
                            }
                            if (SmartDialType.getMarkCountColumnIndex() < aCursorInfo.getColumnCount()) {
                                markCount = aCursorInfo.getInt(SmartDialType.getMarkCountColumnIndex());
                            }
                            if (SmartDialType.getIsCloudMarkColumnIndex() < aCursorInfo.getColumnCount()) {
                                isCloudMark = aCursorInfo.getInt(SmartDialType.getIsCloudMarkColumnIndex()) == 1;
                            }
                        } else if (lookup.contains("com.android.contacts.app")) {
                            lNameStr = aCursorInfo.getString(1);
                            lNumberStr = aCursorInfo.getString(2);
                            if (QueryUtil.isProviderSupportHwSeniorSearch()) {
                                matchType = ((HwSearchCursor) aCursorInfo).getMatchType();
                            }
                        } else {
                            lNameStr = aCursorInfo.getString(1);
                            lNumberStr = aCursorInfo.getString(5);
                            phoneNumberType = aCursorInfo.getInt(6);
                            str = aCursorInfo.getString(7);
                            if (QueryUtil.isProviderSupportHwSeniorSearch()) {
                                matchType = ((HwSearchCursor) aCursorInfo).getMatchType();
                            }
                            key = aCursorInfo.getString(2);
                            contactId = aCursorInfo.getLong(10);
                            lookupKey = aCursorInfo.getString(11);
                            normalized_numebr = aCursorInfo.getString(14);
                        }
                    }
                }
                if (EmuiFeatureManager.isChinaArea() && !TextUtils.isEmpty(lNumberStr)) {
                    String strChinaFormatNumber = (String) this.mChinaFormatNumberCache.getPossiblyExpired(lNumberStr);
                    if (strChinaFormatNumber == null) {
                        strChinaFormatNumber = ContactsUtils.getChinaFormatNumber(lNumberStr);
                        this.mChinaFormatNumberCache.put(lNumberStr, strChinaFormatNumber);
                    }
                    lNumberStr = strChinaFormatNumber;
                }
                views.mPrimaryActionView.setTag(IntentProvider.getDialerCallIntentProvider(lNumberStr, lNameStr, normalized_numebr, countryIso, lookup, duration, callFeature));
                views.mSecondaryActionView.setVisibility(0);
                if (!(views.mSecondaryActionView.getDrawable() instanceof CustomStateListDrawable)) {
                    views.mSecondaryActionView.setImageResource(R.drawable.ic_information_normal);
                    views.mSecondaryActionView.setContentDescription(aContext.getResources().getString(R.string.viewContactTitle));
                    ViewUtil.setStateListIcon(views.mSecondaryActionView.getContext(), views.mSecondaryActionView, false);
                }
                views.mNumber = lNumberStr;
                if (contactId != -1) {
                    views.mBlackListName = lNameStr;
                } else {
                    views.mBlackListName = "";
                }
                NumberMarkInfo markInfo = null;
                CharSequence numberMarkInfo = null;
                CallLogFragment lCallLogFragment = DialpadFragment.this.getCallLogFragment();
                if (lCallLogFragment == null || !lCallLogFragment.isAdded() || lCallLogFragment.mAdapter == null) {
                    isVoiceMailNum = PhoneNumberUtils.isVoiceMailNumber(lNumberStr);
                } else {
                    isVoiceMailNum = lCallLogFragment.mAdapter.isVoiceMailNumber(lNumberStr);
                }
                String originMarkInfo = null;
                if (EmuiFeatureManager.isNumberMarkFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner() && !TextUtils.isEmpty(formattedNumber) && !isVoiceMailNum) {
                    views.mNeedMark = true;
                    markInfo = new NumberMarkInfo(lNumberStr, str3, str2, markCount, isCloudMark);
                    originMarkInfo = NumberMarkUtil.revertMarkTypeToMarkName(str2, this.mContext);
                    if (TextUtils.isEmpty(originMarkInfo)) {
                        originMarkInfo = markInfo.getName();
                    }
                    views.mOriginMarkInfo = originMarkInfo;
                    numberMarkInfo = NumberMarkUtil.getMarkLabel(this.mContext, markInfo);
                }
                if (lookup != null || this.mMode == 0) {
                    int hashCode;
                    if (lookup == null || !lookup.contains("com.android.contacts.app")) {
                        views.mLookupUri = Contacts.getLookupUri(contactId, lookupKey);
                        views.mName.setText(lNameStr, BufferType.SPANNABLE);
                        views.mPhoneNum.setText(lNumberStr, BufferType.SPANNABLE);
                        if (aCursorInfo.getInt(15) == 1) {
                            views.mDefaultText.setVisibility(0);
                            views.mDefaultText.setText(DialpadFragment.this.getResources().getString(R.string.contacts_default));
                        } else {
                            views.mDefaultText.setVisibility(8);
                            views.mDefaultText.setText("");
                        }
                        views.mPhoneCallDetailsViews.cardType.setVisibility(8);
                        views.mPhoneCallDetailsViews.callCount.setVisibility(8);
                        views.mPhoneCallDetailsViews.missedCallCount.setVisibility(8);
                        views.mPhoneCallDetailsViews.outgoingIcon.setVisibility(8);
                        views.mPhoneType.setText((String) Phone.getTypeLabel(DialpadFragment.this.mResource, phoneNumberType, str));
                        hashCode = 0;
                        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                            hashCode = (lNameStr + lNumberStr).hashCode();
                        }
                        views.mSecondaryActionViewLayout.setTag(IntentProvider.getContactDetailIntentProvider(views.mLookupUri, hashCode, isEnterpriseContact(aCursorInfo.getPosition())));
                        if (views.mCompanyView != null) {
                            String company = aCursorInfo.getString(13);
                            if (TextUtils.isEmpty(company)) {
                                views.mCompanyView.setVisibility(8);
                            } else {
                                views.mCompanyView.setVisibility(0);
                                views.mCompanyView.setText(company);
                            }
                        }
                        if (DialpadFragment.this.mList != null) {
                            views.mPhoneNum.setMaxWidth(DialpadFragment.this.mList.getWidth());
                        }
                    } else {
                        views.mName.setText(lNameStr, BufferType.SPANNABLE);
                        views.mPhoneNum.setText(lNumberStr, BufferType.SPANNABLE);
                        views.mPhoneType.setVisibility(8);
                        views.mPhoneCallDetailsViews.cardType.setVisibility(8);
                        views.mPhoneCallDetailsViews.callCount.setVisibility(8);
                        views.mPhoneCallDetailsViews.missedCallCount.setVisibility(8);
                        views.mPhoneCallDetailsViews.outgoingIcon.setVisibility(8);
                        if (views.mPhoneCallDetailsViews.dateView != null) {
                            views.mPhoneCallDetailsViews.dateView.setVisibility(8);
                        }
                        views.mPrimaryActionView.setTag(IntentProvider.getDialerCallIntentProvider(lNumberStr, lNameStr, normalized_numebr, countryIso, lookup, duration, callFeature));
                        views.mLookupUri = Uri.parse(lookup);
                        hashCode = 0;
                        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                            hashCode = (lNameStr + lNumberStr).hashCode();
                        }
                        views.mSecondaryActionViewLayout.setTag(IntentProvider.getContactDetailIntentProvider(views.mLookupUri, hashCode, false));
                        if (views.mCompanyView != null) {
                            views.mCompanyView.setVisibility(8);
                        }
                        if (DialpadFragment.this.mList != null) {
                            views.mPhoneNum.setMaxWidth(DialpadFragment.this.mList.getWidth());
                        }
                    }
                    views.mName.setTextColor(aContext.getResources().getColor(R.color.call_log_primary_text_color));
                    if (views.mPhoneCallDetailsViews.mLocationView != null) {
                        views.mPhoneCallDetailsViews.mLocationView.setVisibility(8);
                    }
                    if (views.mPhoneCallDetailsViews.voicemailIcon != null) {
                        views.mPhoneCallDetailsViews.voicemailIcon.setVisibility(8);
                    }
                } else {
                    views.mLookupUri = null;
                    views.mName.setText(formattedNumber, BufferType.SPANNABLE);
                    PhoneCallDetails details = new PhoneCallDetails(lNumberStr, formattedNumber, countryIso, geoCodedLocation, new int[]{callType}, date, duration, isVoiceMailNum, numberMarkInfo, markInfo);
                    details.setPresentation(numPresention);
                    this.mPhoneCallDetailsHelper.setPhoneCallDetails(views.mPhoneCallDetailsViews, details, false, Long.valueOf(date).toString(), false, true);
                    views.mPhoneType.setVisibility(8);
                    views.mPhoneCallDetailsViews.cardType.setVisibility(8);
                    views.mPhoneCallDetailsViews.callCount.setVisibility(8);
                    views.mPhoneCallDetailsViews.missedCallCount.setVisibility(8);
                    views.mPhoneCallDetailsViews.outgoingIcon.setVisibility(8);
                    if (views.mPhoneCallDetailsViews.voicemailIcon != null) {
                        views.mPhoneCallDetailsViews.voicemailIcon.setVisibility(8);
                    }
                    if (views.mPhoneCallDetailsViews.dateView != null) {
                        views.mPhoneCallDetailsViews.dateView.setVisibility(8);
                    }
                    if (views.mPhoneCallDetailsViews.dateView != null) {
                        views.mPhoneCallDetailsViews.dateView.setTag(details);
                    }
                    views.mSecondaryActionViewLayout.setTag(IntentProvider.getCallDetailIntentProvider((long) calllogId, details, numberMarkInfo, originMarkInfo));
                    if (views.mCompanyView != null) {
                        views.mCompanyView.setVisibility(8);
                    }
                }
                CharSequence name = views.mPhoneCallDetailsViews.nameView.getText();
                if (this.mMode == 1 && !DialpadFragment.this.mScrolling) {
                    PhoneItem phoneItem;
                    String input;
                    int length;
                    if (lookup != null) {
                        phoneItem = new PhoneItem(lNumberStr, lNameStr, matchType, key);
                        views.mName.setTag(phoneItem);
                        input = DialpadFragment.this.mDigits.getText().toString();
                        length = input.length();
                        if (length > 0 && input.indexOf("*") == length - 1) {
                            input = input.substring(0, length - 1);
                        }
                        DialpadFragment.this.getDialerHighlighter().highlightText(phoneItem, views.mName, views.mPhoneNum, DialerHighlighter.cleanNumberForDialpad(input, false), aContext);
                        if (DialpadFragment.this.mCustDialpadFragment != null) {
                            DialpadFragment.this.mCustDialpadFragment.checkAndUpdatePhoneType(views.mPhoneType, (String) Phone.getTypeLabel(DialpadFragment.this.mResource, phoneNumberType, str), lookup);
                        }
                    } else if (name != null && name.toString().equals(formattedNumber)) {
                        phoneItem = new PhoneItem(formattedNumber, formattedNumber, 0, null);
                        views.mPhoneNum.setTag(phoneItem);
                        input = DialpadFragment.this.mDigits.getText().toString();
                        length = input.length();
                        if (length > 0 && input.indexOf("*") == length - 1) {
                            input = input.substring(0, length - 1);
                        }
                        DialpadFragment.this.getDialerHighlighter().highlightText(phoneItem, views.mPhoneNum, views.mName, DialerHighlighter.cleanNumber(input, false), aContext);
                    } else if (name != null) {
                        String param = name.toString();
                        input = name.toString();
                        TextView highLightView = views.mPhoneNum;
                        numberView = views.mName;
                        boolean isMaritimeNum = IsPhoneNetworkRoamingUtils.isMaritimeSatelliteNumber(lNumberStr);
                        if ((markInfo != null && markInfo.isBrandInfo()) || isVoiceMailNum || isMaritimeNum) {
                            param = lNumberStr;
                            input = IsPhoneNetworkRoamingUtils.removeDashesAndBlanksBrackets(DialpadFragment.this.mDigits.getText().toString());
                            views.mPhoneNum.setText(lNumberStr, BufferType.SPANNABLE);
                            highLightView = views.mName;
                            numberView = views.mPhoneNum;
                        }
                        phoneItem = new PhoneItem(param, param, 0, null);
                        highLightView.setTag(phoneItem);
                        length = input.length();
                        if (length > 0 && input.indexOf("*") == length - 1) {
                            input = input.substring(0, length - 1);
                        }
                        DialpadFragment.this.getDialerHighlighter().highlightText(phoneItem, highLightView, numberView, input, aContext);
                    } else if (HwLog.HWFLOW) {
                        HwLog.i("DialpadFragment", "the name view is null!");
                    }
                }
                TextView espaceView = views.mPhoneCallDetailsViews.mEspaceView;
                TextView locationView = views.mPhoneCallDetailsViews.mLocationView;
                numberView = views.mPhoneCallDetailsViews.numberView;
                if (espaceView != null) {
                    espaceView.setVisibility(8);
                    if (32 == callFeature) {
                        espaceView.setVisibility(0);
                        if (numberView != null) {
                            if (markInfo == null || originMarkInfo == null) {
                                numberView.setText("");
                            } else {
                                numberView.setPadding(DialpadFragment.this.mResource.getDimensionPixelSize(R.dimen.call_log_second_line_item_distance), 0, 0, 0);
                            }
                            if (!(markInfo == null || !markInfo.isBrandInfo() || TextUtils.isEmpty(numberMarkInfo))) {
                                numberView.setText("");
                            }
                        }
                        if (locationView != null) {
                            locationView.setText("");
                        }
                    }
                }
                if (isEnterpriseContact(aCursorInfo.getPosition())) {
                    views.mWorkProfileIcon.setVisibility(0);
                } else {
                    views.mWorkProfileIcon.setVisibility(8);
                }
                if (CommonUtilMethods.calcIfNeedSplitScreen() && DialpadFragment.this.mIsLandscape) {
                    Uri lkUri = views.mLookupUri;
                    if (DialpadFragment.this.mSelectCallLogId.longValue() != -1 && DialpadFragment.this.mSelectCallLogId.longValue() == ((long) calllogId)) {
                        aView.setBackgroundColor(aContext.getResources().getColor(R.color.split_itme_selected));
                    } else if (DialpadFragment.this.mSelectContactUri == null || !DialpadFragment.this.mSelectContactUri.equals(lkUri) || (lNameStr + lNumberStr) == null || DialpadFragment.this.mSelectHashCode != (lNameStr + lNumberStr).hashCode()) {
                        if (DialpadFragment.this.mNewContactNumber != null) {
                            if (DialpadFragment.this.mNewContactNumber.equals(ContactsUtils.removeDashesAndBlanks(lNumberStr)) && lNameStr != null) {
                                aView.setBackgroundColor(aContext.getResources().getColor(R.color.split_itme_selected));
                            }
                        }
                        aView.setBackgroundColor(0);
                    } else {
                        aView.setBackgroundColor(aContext.getResources().getColor(R.color.split_itme_selected));
                    }
                }
            }
        }

        private boolean isEnterpriseContact(int position) {
            if (this.mEnterItemCount <= 0 || position < this.mEnterpriseIndex || position >= this.mEnterpriseIndex + this.mEnterItemCount) {
                return false;
            }
            return true;
        }

        public void setMode(int aMode) {
            this.mMode = aMode;
        }

        public int getCount() {
            ensureCacheValid();
            return this.mCount;
        }
    }

    private class EmptyListAdapter extends BaseAdapter {
        private ChoiceItem[] mChoiceItems = null;
        private LayoutInflater mInflater;
        private boolean mIsLteEnable = true;
        private boolean mIsVtLteOn = false;

        public EmptyListAdapter(Context context) {
            initData(context);
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        protected void initData(Context context) {
            int i;
            int i2 = 1;
            this.mIsVtLteOn = VtLteUtils.isVtLteOn(context);
            if (this.mIsVtLteOn) {
                i = 1;
            } else {
                i = 0;
            }
            this.mChoiceItems = new ChoiceItem[(i + 3)];
            this.mInflater = LayoutInflater.from(context);
            this.mChoiceItems[0] = new ChoiceItem(context.getString(R.string.pickerNewContactText), 0, R.drawable.ic_dialer_selector_new_contact);
            this.mChoiceItems[1] = new ChoiceItem(context.getString(R.string.contact_saveto_existed_contact), 3, R.drawable.ic_dialer_selector_add_exist_contact);
            if (this.mIsVtLteOn) {
                this.mChoiceItems[2] = new ChoiceItem(context.getString(R.string.contact_menu_video_call), 4, R.drawable.ic_dialer_selector_video_call);
            }
            ChoiceItem[] choiceItemArr = this.mChoiceItems;
            if (!this.mIsVtLteOn) {
                i2 = 0;
            }
            choiceItemArr[i2 + 2] = new ChoiceItem(context.getString(R.string.contact_menu_send_message), 2, R.drawable.ic_dialer_selector_send_sms);
        }

        public int getCount() {
            return (this.mIsVtLteOn ? 1 : 0) + 3;
        }

        public Object getItem(int position) {
            return this.mChoiceItems[position];
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.mInflater.inflate(R.layout.dialpad_empty_list_item, null);
            }
            TextView text = (TextView) convertView.findViewById(R.id.dialpadlisttext);
            ImageView image = (ImageView) convertView.findViewById(R.id.dialpadlist_icon);
            image.setImageResource(this.mChoiceItems[position].mIconId);
            text.setText(this.mChoiceItems[position].text);
            if (convertView.getTag() == null) {
                convertView.setTag(this.mChoiceItems[position]);
            }
            ChoiceItem choiceItem = (ChoiceItem) convertView.getTag();
            convertView.setOnClickListener(DialpadFragment.this.emptylistbuttonListener);
            if (this.mIsVtLteOn && choiceItem.id == 4) {
                this.mIsLteEnable = VtLteUtils.isLteServiceAbility();
                convertView.setEnabled(this.mIsLteEnable);
                DialpadFragment.this.updateVideoCallItems(image, text);
            }
            return convertView;
        }
    }

    private class GuideGestureListener extends SimpleOnGestureListener {
        private GuideGestureListener() {
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!DialpadFragment.this.mIsLandscape && !DialpadFragment.this.hasResponseLongClick && e2.getY() - e1.getY() > 200.0f && velocityY > velocityX) {
                DialpadFragment.this.setDigitsVisible(false, true);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private class MSimPhoneStateListener extends PhoneStateListenerEx {
        public MSimPhoneStateListener(int subscription) {
            super(subscription);
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            if (state != 2) {
                if (!DialpadFragment.this.phoneIsInUse()) {
                    try {
                        DialpadFragment.this.showDialpadChooser();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Activity activity = DialpadFragment.this.getActivity();
                if (activity != null && !activity.isFinishing()) {
                    DialpadFragment.this.mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                DialpadFragment.this.updateButtonStates(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private MyBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            HwLog.i("DialpadFragment", "mBroadcastReceiver action:" + action);
            if ("com.huawei.android.dsdscardmanager.SIM_CARD_NAME_SUB1_UPDATED_ACTION".equals(action) || "com.huawei.android.dsdscardmanager.SIM_CARD_NAME_SUB2_UPDATED_ACTION".equals(action)) {
                DialpadFragment.this.updateButtonStates(true);
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                if (DialpadFragment.this.getActivity() != null && !DialpadFragment.this.getActivity().isFinishing() && !DialpadFragment.this.mIsLandscape && DialpadFragment.this.mSingleHandMode != null && DialpadFragment.this.mSingleHandMode.isSmartSingleHandFeatureEnabled() && DialpadFragment.this.mSingleHandMode.isSmartSingleHandModeOn() && MotionRecognition.isMotionClassExists() && MotionRecognition.isMotionRecoApkExist(DialpadFragment.this.mActivity)) {
                    DialpadFragment.this.destroyMotionRecognition();
                }
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                if (DialpadFragment.this.getActivity() != null && !DialpadFragment.this.getActivity().isFinishing() && !DialpadFragment.this.mBePause && !DialpadFragment.this.mIsLandscape && DialpadFragment.this.mSingleHandMode != null && DialpadFragment.this.mSingleHandMode.isSmartSingleHandFeatureEnabled() && DialpadFragment.this.mSingleHandMode.isSmartSingleHandModeOn() && MotionRecognition.isMotionClassExists() && MotionRecognition.isMotionRecoApkExist(DialpadFragment.this.mActivity)) {
                    DialpadFragment.this.startMotionRecognition();
                }
            } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                DialpadFragment.this.updateButtonStates(true, true, false);
            } else if ("huawei.intent.action.IMS_SERVICE_STATE_CHANGED".equals(action)) {
                DialpadFragment.this.updateButtonStates(false, false, false);
            } else if ("android.provider.Telephony.SPN_STRINGS_UPDATED".equals(action)) {
                DialpadFragment.this.refreshFilterButton();
            }
        }
    }

    class MyHandler extends Handler {
        MyHandler() {
        }

        public void handleMessage(Message aMsg) {
            switch (aMsg.what) {
                case 112:
                    DialpadFragment.this.mAdapter.notifyDataSetChanged();
                    return;
                case 256:
                    if (!TextUtils.isEmpty(DialpadFragment.this.mQueryString) && DialpadFragment.this.isAdded()) {
                        DialpadFragment.this.requeryData();
                        DialpadFragment.this.mActiveQueryStrings.add(DialpadFragment.this.mQueryString);
                        return;
                    }
                    return;
                case 257:
                    DialpadFragment.this.mShowDialpadLocation.setText(aMsg.obj);
                    return;
                case 258:
                    DialpadFragment.this.canDialOut = true;
                    return;
                case 259:
                    DialpadFragment.this.enableDelete = true;
                    return;
                case 260:
                    DialpadFragment.this.showNoSpeedDialNumberAssignedPopUp(aMsg.arg1);
                    return;
                case 261:
                    DialpadFragment.this.processLongclickOne();
                    return;
                case 262:
                    if (DialpadFragment.this.mDigits != null && !DialpadFragment.this.mDigits.isFocused()) {
                        DialpadFragment.this.mDigits.requestFocus();
                        return;
                    }
                    return;
                case 263:
                    DialpadFragment.this.mHasClickedNewContact = false;
                    return;
                case 264:
                    int resultid = aMsg.arg1;
                    boolean isNeedUpdate = false;
                    if (DialpadFragment.this.mCustDialpadFragment != null) {
                        isNeedUpdate = DialpadFragment.this.mCustDialpadFragment.setEncryptButtonBackgroundChoosed(resultid);
                    }
                    switch (resultid) {
                        case 0:
                            DialpadFragment.this.mCardNameDial2.setBackgroundResource(R.drawable.rectangle);
                            DialpadFragment.this.isSimOneRecommended = true;
                            DialpadFragment.this.needUpdateRecommendedButtonState = true;
                            break;
                        case 1:
                            DialpadFragment.this.mCardNameDial3.setBackgroundResource(R.drawable.rectangle);
                            DialpadFragment.this.isSimTwoRecommended = true;
                            DialpadFragment.this.needUpdateRecommendedButtonState = true;
                            break;
                        default:
                            DialpadFragment.this.mCardNameDial2.setBackgroundResource(R.drawable.btn_call);
                            DialpadFragment.this.mCardNameDial3.setBackgroundResource(R.drawable.btn_call);
                            DialpadFragment.this.isSimOneRecommended = false;
                            DialpadFragment.this.isSimTwoRecommended = false;
                            DialpadFragment.this.needUpdateRecommendedButtonState = false;
                            if (DialpadFragment.this.mCustDialpadFragment != null) {
                                DialpadFragment.this.mCustDialpadFragment.setEncryptBtnBgNormal(resultid);
                                break;
                            }
                            break;
                    }
                    if (isNeedUpdate) {
                        DialpadFragment.this.needUpdateRecommendedButtonState = true;
                        return;
                    }
                    return;
                case 265:
                    if (DialpadFragment.this.getActivity() != null && !DialpadFragment.this.getActivity().isFinishing() && DialpadFragment.this.mDigits != null) {
                        if (DialpadFragment.this.mIsPhoneMultiSim) {
                            DialpadFragment.this.updateButtonStates(true);
                            return;
                        } else {
                            DialpadFragment.this.updateDialAndDeleteButtonEnabledState();
                            return;
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private static class MyOnTouchListener implements OnTouchListener {
        private MyOnTouchListener() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    private class QueryLastOutgoingCallRunnable implements Runnable {
        private QueryLastOutgoingCallRunnable() {
        }

        public void run() {
            Cursor cursor = null;
            try {
                cursor = DialpadFragment.this.mResolver.query(Calls.CONTENT_URI_WITH_VOICEMAIL, new String[]{"number"}, " DELETED = 0 ", null, "date DESC LIMIT 1");
                if (cursor == null || !cursor.moveToFirst()) {
                    DialpadFragment.this.mLastNumberDialed = "";
                } else {
                    DialpadFragment.this.mLastNumberDialed = cursor.getString(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (DialpadFragment.this.mHandler.hasMessages(265)) {
                    DialpadFragment.this.mHandler.removeMessages(265);
                }
                Message message = Message.obtain();
                message.what = 265;
                DialpadFragment.this.mHandler.sendMessage(message);
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private class ResetCallLogListView implements Runnable {
        private ResetCallLogListView() {
        }

        public void run() {
            DialpadFragment.this.resetCallLogListViewHeight();
        }
    }

    private static class RoamingData {
        private String normalizedNumber;
        private String number;

        RoamingData(String number, String normalized) {
            this.number = number;
            this.normalizedNumber = normalized;
        }
    }

    private final class SpeedDialNumberQueryTask extends AsyncTask<Void, Void, RoamingData> {
        private int mKey;

        public SpeedDialNumberQueryTask(int aKey) {
            this.mKey = aKey;
        }

        protected RoamingData doInBackground(Void... aVarArgs) {
            DialPadState lState = (DialPadState) DialpadFragment.this.mSpeedDialDataMap.get(Integer.valueOf(this.mKey));
            Cursor lCursor = DialpadFragment.this.mResolver.query(Data.CONTENT_URI, new String[]{"data1", "data4", "display_name"}, "_id =? ", new String[]{lState.mDataID + ""}, null);
            RoamingData roamingData = null;
            if (lCursor != null) {
                if (lCursor.moveToFirst()) {
                    roamingData = new RoamingData(lCursor.getString(lCursor.getColumnIndex("data1")), lCursor.getString(lCursor.getColumnIndex("data4")));
                }
                lCursor.close();
            }
            return roamingData;
        }

        protected void onPostExecute(RoamingData aResult) {
            if (aResult == null) {
                DialpadFragment.this.showNoSpeedDialNumberAssignedPopUp(this.mKey);
            } else if (IsPhoneNetworkRoamingUtils.isPhoneNetworkRoamging()) {
                String number = RoamingDialPadDirectlyManager.getRoamingDialNumber(DialpadFragment.this.mActivity, aResult.number, aResult.normalizedNumber, DialpadFragment.this.mSpeedDirectlyDataListener);
                if (!TextUtils.isEmpty(number)) {
                    DialpadFragment.this.callSpeedDialNumber(number);
                }
            } else {
                DialpadFragment.this.callSpeedDialNumber(aResult.number);
            }
        }
    }

    private class SwitcherClickListener implements OnClickListener {
        private SwitcherClickListener() {
        }

        public void onClick(View v) {
            if (DialpadFragment.this.mViewSwitcher != null) {
                if (DialpadFragment.this.mShowDialpad) {
                    DialpadFragment.this.dialButtonPressed(DialpadFragment.this.mViewSwitcher.getDisplayedChild());
                    return;
                }
                DialpadFragment.this.setDigitsVisible(true, true);
            }
        }
    }

    private class SwitcherLongClickListener implements OnLongClickListener {
        private SwitcherLongClickListener() {
        }

        public boolean onLongClick(View arg0) {
            DialpadFragment.this.isLongPress = true;
            Vibrator vibrator = (Vibrator) DialpadFragment.this.getActivity().getApplicationContext().getSystemService("vibrator");
            if (vibrator != null) {
                vibrator.vibrate(35);
            }
            return false;
        }
    }

    private class SwitcherTouchListener implements OnTouchListener {
        private SwitcherTouchListener() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            if (DialpadFragment.this.mViewSwitcher == null) {
                return false;
            }
            PeopleActivity peopleActivity = null;
            if (DialpadFragment.this.isLongPress && (DialpadFragment.this.mActivity instanceof PeopleActivity)) {
                peopleActivity = (PeopleActivity) DialpadFragment.this.mActivity;
                ((HapViewPager) peopleActivity.getViewPager()).disableViewPagerSlide(true);
            }
            switch (event.getAction()) {
                case 0:
                    DialpadFragment.mDownX = event.getX();
                    break;
                case 1:
                    if (DialpadFragment.this.isLongPress) {
                        DialpadFragment.this.isLongPress = false;
                        DialpadFragment.this.mViewSwitcher.setPressed(false);
                        if (DialpadFragment.this.mCustDialpadFragment != null) {
                            DialpadFragment.this.mCustDialpadFragment.setViewSwitcherPressed();
                        }
                        int currentSIMCard = DialpadFragment.this.mViewSwitcher.getDisplayedChild();
                        int moveDistance = DialpadFragment.this.mDialButtonSwitcher1.getLayoutParams().width / 2;
                        int i;
                        if (DialpadFragment.mDownX - event.getX() > ((float) moveDistance)) {
                            DialpadFragment.this.mViewSwitcher.setInAnimation(DialpadFragment.this.mAnimation[2]);
                            DialpadFragment.this.mViewSwitcher.setOutAnimation(DialpadFragment.this.mAnimation[1]);
                            DialpadFragment.this.mViewSwitcher.showNext();
                            if (currentSIMCard == 0) {
                                i = 1;
                            } else {
                                i = 0;
                            }
                            SimFactoryManager.setDefaultSimcard(i);
                            if (DialpadFragment.this.mCustDialpadFragment != null && EncryptCallUtils.getCust().isEncryptCallEnable()) {
                                DialpadFragment.this.mCustDialpadFragment.showSwitcherNext(DialpadFragment.this.mAnimation[2], DialpadFragment.this.mAnimation[1], new DialButtonListener(DialpadFragment.this.mExtremeSimpleDefaultSimcard, false, true), DialpadFragment.this.mIsLandscape, DialpadFragment.this.isInLeftOrRightState());
                            }
                        } else if (event.getX() - DialpadFragment.mDownX > ((float) moveDistance)) {
                            DialpadFragment.this.mViewSwitcher.setInAnimation(DialpadFragment.this.mAnimation[0]);
                            DialpadFragment.this.mViewSwitcher.setOutAnimation(DialpadFragment.this.mAnimation[3]);
                            DialpadFragment.this.mViewSwitcher.showPrevious();
                            if (currentSIMCard == 0) {
                                i = 1;
                            } else {
                                i = 0;
                            }
                            SimFactoryManager.setDefaultSimcard(i);
                            if (DialpadFragment.this.mCustDialpadFragment != null && EncryptCallUtils.getCust().isEncryptCallEnable()) {
                                DialpadFragment.this.mCustDialpadFragment.showSwitcherPrevious(DialpadFragment.this.mAnimation[0], DialpadFragment.this.mAnimation[3], new DialButtonListener(DialpadFragment.this.mExtremeSimpleDefaultSimcard, false, true), DialpadFragment.this.mIsLandscape, DialpadFragment.this.isInLeftOrRightState());
                            }
                        }
                        if (HapEncryptCallUtils.isEncryptCallEnabled() && !DialpadFragment.this.isDigitsEmpty()) {
                            EncryptCallDialpadFragmentHelper.updateEncryptCallViewStatus(DialpadFragment.this.mTableRow0, DialpadFragment.this.mEncryptCallView, DialpadFragment.this.mSearchButton, DialpadFragment.this.mLineHorizontalTopTable0, DialpadFragment.this.mIsLandscape);
                            DialpadFragment.this.changeDialerDigitsHeight();
                        }
                        if (!(peopleActivity == null || DialpadFragment.this.mDigitsContainer == null || DialpadFragment.this.mDigitsContainer.isShown())) {
                            ((HapViewPager) peopleActivity.getViewPager()).disableViewPagerSlide(false);
                        }
                        return true;
                    }
                    break;
            }
            return false;
        }
    }

    public void setRow0ShownWhenRecreate(boolean isRow0ShownWhenRecreate) {
        this.isRow0ShownWhenRecreate = isRow0ShownWhenRecreate;
    }

    public void updateItemsStatus() {
        checkVideoCallVisibility();
    }

    public static boolean getIsIpCallEnabled() {
        return isIpCallEnabled && !EmuiFeatureManager.isAutoIpEnabled();
    }

    public boolean isFirstPress() {
        return this.mFirstPress && this.mDigits.getText().length() == 0;
    }

    private void setResetFirstPressFlag(boolean aFirstPress) {
        this.mFirstPress = aFirstPress;
    }

    public DialpadAnimateHelper getAnimateHelper() {
        return this.mAnimateHelper;
    }

    private void setIsAnimationInProgress(boolean inProgress) {
        this.mIsAnimationInProgress = inProgress;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        this.mWasEmptyBeforeTextChange = TextUtils.isEmpty(s);
    }

    public void onTextChanged(CharSequence input, int start, int before, int changeCount) {
        if (isAdded()) {
            ((PeopleActivity) getActivity()).getDialpadFragmentHelper().startInitNewViewInBackground(getActivity());
            if (this.mWasEmptyBeforeTextChange != TextUtils.isEmpty(input)) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.invalidateOptionsMenu();
                }
            }
            this.mFirstVisiblePosition = 0;
            if (isDigitsEmpty()) {
                this.mCardNameDial2.setBackgroundResource(R.drawable.btn_call);
                this.mCardNameDial3.setBackgroundResource(R.drawable.btn_call);
                this.isSimOneRecommended = false;
                this.isSimTwoRecommended = false;
                if (this.mCustDialpadFragment != null) {
                    this.mCustDialpadFragment.setEncryptBtnBgNormal();
                }
                this.mDigits.setHint("");
                setResetFirstPressFlag(true);
            } else {
                setResetFirstPressFlag(false);
            }
        }
    }

    private static void setDialBtnTextAndImage(LinearLayout cardNameDial, String cardName, int imageId, boolean isViewSwitcher, int simSlotId) {
        if (cardNameDial != null) {
            TextView textView;
            ImageView imageView;
            if (isViewSwitcher) {
                textView = (TextView) cardNameDial.findViewById(R.id.button_text_switcher);
                imageView = (ImageView) cardNameDial.findViewById(R.id.button_image_switcher);
            } else {
                textView = (TextView) cardNameDial.findViewById(R.id.button_text);
                imageView = (ImageView) cardNameDial.findViewById(R.id.button_image);
            }
            int isGet4GSubscription = SimFactoryManager.getUserDefaultSubscription();
            if (IsPhoneNetworkRoamingUtils.isPhoneNetworkRoaming(simSlotId)) {
                imageId = R.drawable.call_dial_r_selector_dialer;
            }
            if (textView != null && imageView != null) {
                LayoutParams lp = imageView.getLayoutParams();
                lp.height = textView.getContext().getResources().getDimensionPixelSize(R.dimen.dialpad_dial_button_multi_simcard_icon_height);
                lp.width = textView.getContext().getResources().getDimensionPixelSize(R.dimen.dialpad_dial_button_multi_simcard_icon_width);
                if (mCust != null && mCust.isVOWifiCallEnabled(textView.getContext()) && isGet4GSubscription == simSlotId && isGet4GSubscription != -1) {
                    imageId = mCust.getVOWifiCallBtnIconForSingleSim(imageId);
                } else if (CommonUtilMethods.getTelephonyManager(textView.getContext()).isImsRegistered() && isGet4GSubscription == simSlotId && isGet4GSubscription != -1) {
                    int iconRule = HwCarrierConfigManagerInner.getDefault().getVolteIconRule(textView.getContext(), simSlotId, 192);
                    if (iconRule == 2) {
                        if (HwTelephonyManager.getDefault().getImsDomain() == 1) {
                            imageId = R.drawable.call_vowifi_selector_dialer;
                            lp.width = textView.getContext().getResources().getDimensionPixelSize(R.dimen.dialpad_dial_button_multi_simcard_vowifi_icon_width);
                        } else {
                            imageId = R.drawable.call_volte_selector_dialer;
                            lp.width = textView.getContext().getResources().getDimensionPixelSize(R.dimen.dialpad_dial_button_multi_simcard_volte_icon_width);
                        }
                    } else if (iconRule == 1 && EmuiFeatureManager.isContactDialpadHdIconOn()) {
                        if (TextUtils.isEmpty(cardName)) {
                            imageId = R.drawable.call_dialhd_selector_dialer;
                        } else {
                            imageId = R.drawable.call_hd_selector_dialer;
                        }
                    }
                }
                if (TextUtils.isEmpty(cardName)) {
                    textView.setVisibility(8);
                } else {
                    textView.setText(cardName);
                    textView.setVisibility(0);
                }
                imageView.setLayoutParams(lp);
                imageView.setImageResource(imageId);
                if (simSlotId == 0) {
                    imageView.setContentDescription(imageView.getContext().getString(R.string.description_dialbutton1));
                } else if (simSlotId == 1) {
                    imageView.setContentDescription(imageView.getContext().getString(R.string.description_dialbutton2));
                }
                ViewUtil.setStateListIcon(cardNameDial.getContext(), imageView, false);
            }
        }
    }

    public void adjustNameViewWidth(Context context, View cardNameDial, int width, boolean isViewSwitcher) {
        if (cardNameDial != null) {
            TextView textView;
            if (isViewSwitcher) {
                textView = (TextView) cardNameDial.findViewById(R.id.button_text_switcher);
            } else {
                textView = (TextView) cardNameDial.findViewById(R.id.button_text);
            }
            int dialpadButtonWidth = width;
            if ((width - context.getResources().getDimensionPixelSize(R.dimen.contact_dialpad_dial_del_button_width)) * 2 < TextUtil.getTextWidth(textView.getText().toString(), (float) context.getResources().getDimensionPixelSize(R.dimen.dialpad_text_width))) {
                textView.setTextSize(1, (float) context.getResources().getInteger(R.integer.dialpad_button_text_size));
                textView.setText(textView.getText().toString() + ' ');
                return;
            }
            textView.setTextSize(1, (float) context.getResources().getInteger(R.integer.dialpad_button_text_normal_size));
        }
    }

    private void setButtonStatesForEmergencyNumber() {
        this.mExtremeSimpleDefaultSimcard = SimFactoryManager.getDefaultSimcard();
        if (SimFactoryManager.isExtremeSimplicityMode()) {
            this.mCardNameDial2.setVisibility(8);
            this.mCardNameDial3.setVisibility(8);
            setDialBtnTextAndImage(this.mDialButtonSwitcher1, this.mCard1Name, this.sim1DialRes, true, 0);
            setDialBtnTextAndImage(this.mDialButtonSwitcher2, this.mCard2Name, this.sim2DialRes, true, 1);
            if (this.mViewSwitcher != null) {
                this.mViewSwitcher.setEnabled(true);
                this.mViewSwitcher.setClickable(true);
                this.mViewSwitcher.setLongClickable(true);
                this.mViewSwitcher.setVisibility(0);
                this.mViewSwitcher.setOnClickListener(this.mSwitcherClickListener);
                this.mViewSwitcher.setOnLongClickListener(this.mSwitcherLongClickListener);
                this.mViewSwitcher.setOnTouchListener(this.mSwitcherTouchListener);
                if (this.mExtremeSimpleDefaultSimcard != this.mViewSwitcher.getDisplayedChild()) {
                    this.mViewSwitcher.setDisplayedChild(this.mExtremeSimpleDefaultSimcard);
                }
            }
        } else {
            if (this.mViewSwitcher != null) {
                this.mViewSwitcher.setVisibility(8);
            }
            if (this.mCustDialpadFragment != null) {
                this.mCustDialpadFragment.hideSwitchButton();
            }
            setDialBtnTextAndImage(this.mCardNameDial2, this.mCard1Name, this.sim1DialRes, false, 0);
            setDialBtnTextAndImage(this.mCardNameDial3, this.mCard2Name, this.sim2DialRes, false, 1);
            setCardNameDialMarginEnd(this.mCardNameDial2.getContext().getResources().getDimensionPixelSize(R.dimen.dialpad_additional_buttions_one_marginend));
            this.mCardNameDial2.setOnClickListener(this.dialButton1Listener);
            this.mCardNameDial3.setOnClickListener(this.dialButton2Listener);
            this.mCardNameDial2.setEnabled(true);
            this.mCardNameDial3.setEnabled(true);
            this.mCardNameDial2.setClickable(true);
            this.mCardNameDial3.setClickable(true);
            this.mCardNameDial2.setVisibility(0);
            this.mCardNameDial3.setVisibility(0);
        }
        if (HapEncryptCallUtils.isEncryptCallEnabled()) {
            EncryptCallDialpadFragmentHelper.setEmergencyDialButton(this.mIsLandscape, this.mTableRow0, this.mLineHorizontalTopTable0, this.mSearchButton);
            changeDialerDigitsHeight();
        }
    }

    public void afterTextChanged(Editable input) {
        PLog.d(1003, "DialpadFragment afterTextChanged for jlog");
        if (isAdded()) {
            if (isDigitsEmpty()) {
                this.mTableRowRcs.setVisibility(8);
                hideTableTopRowAndHeader();
            } else {
                if (HapEncryptCallUtils.isEncryptCallEnabled()) {
                    EncryptCallDialpadFragmentHelper.updateEncryptCallViewStatus(this.mTableRow0, this.mEncryptCallView, this.mSearchButton, this.mLineHorizontalTopTable0, this.mIsLandscape);
                    changeDialerDigitsHeight();
                }
                if (this.mDigits.getText().toString().matches("^\\s*$")) {
                    clearDigitsText();
                    showDigistHeader(false, false);
                }
                if (this.mDigitsFilledByIntent || !SpecialCharSequenceMgr.handleChars(getActivity(), input.toString(), this.mDigits)) {
                    if (this.needShowDigistHeader) {
                        if (HwLog.HWDBG) {
                            HwLog.d("DialpadFragment", "showDigistHeader afterTextChanged");
                        }
                        showDigistHeader(true, true);
                    }
                    this.needShowDigistHeader = true;
                    try {
                        if (EmuiFeatureManager.isRcsFeatureEnable()) {
                            if (this.mRcsCust == null || RcseProfile.getRcsService() == null || !RcseProfile.getRcsService().getLoginState()) {
                                if (this.mRcsCust != null) {
                                    this.mRcsCust.setRcsQuestNumber(this.mDigits.getText().toString());
                                    this.mRcsCust.updateNotLoginRcsView();
                                }
                            } else if (isNotNeedSendCapabilityRequest()) {
                                this.mRcsCust.setRotateRcsView(this.saveRotateRcsState, this.mDigits.getText().toString());
                                this.saveRotateRcsDigits = "";
                            } else {
                                this.mRcsCust.sendRcsQuestCapability(this.mDigits.getText().toString());
                            }
                        }
                    } catch (Exception e) {
                        HwLog.e("DialpadFragment", "failed to update rcs view");
                    }
                } else {
                    clearDigitsText();
                    showDigistHeader(false, false);
                    return;
                }
            }
            if (isDigitsEmpty()) {
                this.mDigitsFilledByIntent = false;
                this.mDigits.setCursorVisible(false);
                setFreqContactsTitle();
                setEmptyListList();
                setCallLogTabVisible(true);
                this.mShowDialpadLocation.setText("");
                setQueryString(null);
                this.mActiveQueryStrings.clear();
                if (HapEncryptCallUtils.isEncryptCallEnabled()) {
                    EncryptCallDialpadFragmentHelper.updateEncryptCallViewAlongWithScreen(this.mEncryptCallView, this.mSearchButton, this.mIsLandscape, 4);
                }
            } else {
                String queryString = DialerHighlighter.cleanNumberForDialpad(this.mDigits.getText().toString(), false);
                if (!TextUtils.equals(this.mQueryString, queryString)) {
                    if (this.isCursorPositionFirst) {
                        this.mDigits.postDelayed(new Runnable() {
                            public void run() {
                                DialpadFragment.this.mDigits.setSelection(DialpadFragment.this.mDigits.getText().length());
                                DialpadFragment.this.isCursorPositionFirst = false;
                            }
                        }, 0);
                    }
                    if (!(TextUtils.isEmpty(queryString) || TextUtils.equals(this.mQueryString, queryString))) {
                        if (TextUtils.isEmpty(this.mQueryString) && queryString.length() == 1) {
                            PLog.d(16, "DialpadFragment afterTextChanged");
                        }
                        setQueryString(queryString);
                        if (queryString.length() > 0) {
                            this.mHasSomethingInput = true;
                            if (this.mHandler.hasMessages(256)) {
                                this.mHandler.removeMessages(256);
                            }
                            this.mHandler.sendEmptyMessage(256);
                        } else if (this.mWasEmptyBeforeTextChange) {
                            setFreqContactsTitle();
                        } else {
                            setFreqContactsTitle();
                            setCallLogTabVisible(true);
                        }
                    }
                    if (!TextUtils.isEmpty(this.mDigits.getText())) {
                        ((DigitsEditText) this.mDigits).adjustFontSize();
                    }
                } else {
                    return;
                }
            }
            if (this.mIsPhoneMultiSim) {
                if (this.mDelayedUpdateModeHandler.hasMessages(9000)) {
                    this.mDelayedUpdateModeHandler.removeMessages(9000);
                }
                updateDialPadBtnDelayed();
            } else {
                updateDialAndDeleteButtonEnabledState();
                updateSingleCardButtonDelayed();
            }
        }
    }

    private boolean isNotNeedSendCapabilityRequest() {
        return this.saveRotateRcsDigits != null ? this.saveRotateRcsDigits.equals(this.mDigits.getText().toString()) : false;
    }

    private void hideTableTopRowAndHeader() {
        setTableTopRowVisibility(8);
        if (this.mRcsCust != null) {
            this.mRcsCust.hideRcsCallButton();
        }
        this.isRow0ShownWhenRecreate = false;
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "mTableRow0 set GONE0");
        }
        showDigistHeader(false, true);
    }

    private void afterTextChangedDelay() {
        if (CommonUtilMethods.isEmergencyNumber(this.mDigits.getText().toString(), this.mIsPhoneMultiSim)) {
            updateDialAndDeleteButtonEnabledState();
        } else {
            updateButtonStates(false);
        }
    }

    public void updateDialpadHandModeView(int aCurrentMode, boolean aSaveMode, boolean isFromMotion) {
        boolean playAnimation = true;
        if (this.mIsAnimationInProgress) {
            playAnimation = false;
        }
        updateDialpadHandModeView(aCurrentMode, aSaveMode, isFromMotion, playAnimation);
    }

    public void updateDialpadHandModeView(int aCurrentMode, boolean aSaveMode, boolean isFromMotion, boolean needAnimation) {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "updateDialpadHandModeView aCurrentMode :: " + aCurrentMode + " needAnimation:" + needAnimation);
        }
        if (this.mSingleHandMode != null) {
            if (aSaveMode) {
                this.mSingleHandMode.saveUserSelectionHandMode(aCurrentMode);
            }
            this.mCurMode = this.mSingleHandMode.getCurrentHandMode();
        }
        if ((this.mCurMode != 0 && this.mCurMode != -1) || ((this.mRightHandLayout != null && this.mRightHandLayout.getVisibility() != 8) || (this.mLeftHandLayout != null && this.mLeftHandLayout.getVisibility() != 8))) {
            initSingleHandMode();
            setUpViewsVisibilityInSingleHandMode(aCurrentMode, isFromMotion, needAnimation);
        }
    }

    private void updateDialPadBtnDelayed() {
        this.mDelayedUpdateModeHandler.removeMessages(9000);
        Message msg = new Message();
        msg.what = 9000;
        this.mDelayedUpdateModeHandler.sendMessageDelayed(msg, 500);
    }

    private void setQueryString(String queryStr) {
        this.mQueryString = queryStr;
    }

    public void onCreate(Bundle state) {
        boolean z = true;
        PLog.d(0, "DialpadFragment onCreate");
        ((PeopleActivity) getActivity()).getDialpadFragmentHelper().startInitDialpadFragmentInBackground(getActivity());
        super.onCreate(state);
        this.mHandler = new MyHandler();
        this.mPhoneStateLinstenerOne = new MSimPhoneStateListener(0);
        this.mPhoneStateLinstenerTwo = new MSimPhoneStateListener(1);
        this.mCallLogObserver = new CallLogObserver(this.mHandler);
        this.airPlanModeOberver = new AirPlanModeObserver(this.mHandler);
        this.mCardNameObserver = new CardNameObserver(this.mHandler);
        this.mDelayedUpdateModeHandler = new DelayedUpdateModeHandler();
        if (CommonUtilMethods.calcIfNeedSplitScreen() && state == null && (getActivity() instanceof PeopleActivity)) {
            state = CommonUtilMethods.getInstanceState();
        }
        this.visCreate = true;
        this.mActivity = getActivity();
        if (CommonUtilMethods.isSimplifiedModeEnabled()) {
            this.mActivity.setRequestedOrientation(1);
        } else {
            this.mActivity.setRequestedOrientation(-1);
        }
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCustDialpadFragment = (HwCustDialpadFragment) HwCustUtils.createObj(HwCustDialpadFragment.class, new Object[]{this.mActivity, this});
        }
        if (this.mCustDialpadFragment != null) {
            this.mCustDialpadFragment.registerDialerCallBack(this);
        }
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsCust = new RcsDialpadFragmentHelper();
        }
        if (this.mRcsCust != null) {
            HwLog.i("DialpadFragment", "register rcs service");
            this.mRcsCust.handleCustomizationsOnCreate(this);
        }
        this.mResource = getResources();
        this.mIsLandscape = this.mResource.getConfiguration().orientation == 2;
        if (this.mRcsCust != null) {
            this.mRcsCust.setLandscapeState(this.mIsLandscape);
        }
        this.mIsPhoneMultiSim = SimFactoryManager.isDualSim();
        setHasOptionsMenu(false);
        initCust();
        this.mProhibitedPhoneNumberRegexp = this.mResource.getString(R.string.config_prohibited_phone_number_regexp);
        if (state != null) {
            this.mDigitsFilledByIntent = state.getBoolean("pref_digits_filled_by_intent");
            this.mshowingaddToContactDialog = state.getBoolean("addtocontactdialog");
            this.mClearDigitsOnStop = state.getBoolean("cleardigits");
            this.mLastNumberDialed = state.getString("last_dialed_number");
            this.mButtonGroupLayoutSelectedType = state.getInt("Button_GroupLayout_Selected");
            this.mLastNewContactNumber = state.getString("split_new_number");
            this.mLastSelectCallLogId = Long.valueOf(state.getLong("split_select_calllog_id"));
            this.mLastSelectContactUri = (Uri) state.getParcelable("split_select_contact_uri");
            this.mLastSelectHashCode = state.getInt("split_select_hash_code");
            this.mFilterType = state.getInt("key_filter_type", 0);
            if (this.mRcsCust != null) {
                this.saveRotateRcsDigits = state.getString("save_rotate_rcs_text");
                this.saveRotateRcsState = state.getInt("save_rotate_rcs_state");
            }
        }
        if (!this.mResource.getBoolean(R.bool.config_customer_aus_036_000)) {
            z = CommonConstants.IS_DIALER_EMERGENCY_NO;
        }
        this.mShowEmergencyCallWhenNoSIM = z;
        if (this.mShowEmergencyCallWhenNoSIM) {
            SimStateServiceHandler.registerCallback(this);
        }
        this.mResolver = this.mActivity.getContentResolver();
        this.mTonePlayer = new DialpadTouchTonePlayer(getActivity());
        updateSpeedDialPredefinedCache();
        this.mIsGeoLocationDispEnabled = PhoneCapabilityTester.isGeoCodeFeatureEnabled(this.mActivity);
        if (this.mIsGeoLocationDispEnabled) {
            createNumberLocationHdlr();
        }
        initKeyLayoutParams(this.mActivity);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.android.dsdscardmanager.SIM_CARD_NAME_SUB1_UPDATED_ACTION");
        filter.addAction("com.huawei.android.dsdscardmanager.SIM_CARD_NAME_SUB2_UPDATED_ACTION");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("huawei.intent.action.IMS_SERVICE_STATE_CHANGED");
        filter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED");
        this.mActivity.registerReceiver(this.mBroadcastReceiver, filter);
        ((PeopleActivity) getActivity()).getContactListHelper().startInitDialpadFragmentInBackground(this);
        this.mVoLteStatusObserver = new VoLteStatusObserver(getContext(), this);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void initHaptic() {
        if (HwLog.HWFLOW) {
            HwLog.i("DialpadFragment", "initHaptic begin");
        }
        HapticFeedback haptic = new HapticFeedback();
        try {
            if (getActivity() != null) {
                haptic.init(getActivity(), this.mResource.getBoolean(R.bool.config_enable_dialer_key_vibration));
                this.mHaptic = haptic;
                if (ISLON) {
                    this.mHaptic.setLon();
                }
                if (HwLog.HWFLOW) {
                    HwLog.i("DialpadFragment", "initHaptic END");
                }
            }
        } catch (NotFoundException nfe) {
            HwLog.e("DialpadFragment", "Vibrate control bool missing.", nfe);
        }
    }

    private synchronized HapticFeedback getHapticFeedback() {
        if (this.mHaptic == null) {
            initHaptic();
        }
        return this.mHaptic;
    }

    public void prepareInBackground() {
        if (HwLog.HWFLOW) {
            HwLog.i("DialpadFragment", "prepareInBackground initHaptic");
        }
        getHapticFeedback();
        this.mResolver.registerContentObserver(CallLog.CONTENT_URI, true, this.mCallLogObserver);
        if (this.mIsPhoneMultiSim) {
            try {
                this.mResolver.registerContentObserver(Global.getUriFor("sim_card0_id"), true, this.mCardNameObserver);
                this.mResolver.registerContentObserver(Global.getUriFor("sim_card1_id"), true, this.mCardNameObserver);
                this.mIsRegistered = true;
            } catch (RuntimeException e) {
                try {
                    Uri uri = (Uri) RefelctionUtils.invokeInnerClass("android.provider.Settings", "Global", "getUriFor", new Object[]{"sim_card0_id"});
                    Uri uri1 = (Uri) RefelctionUtils.invokeInnerClass("android.provider.Settings", "Global", "getUriFor", new Object[]{"sim_card1_id"});
                    this.mResolver.registerContentObserver(uri, true, this.mCardNameObserver);
                    this.mResolver.registerContentObserver(uri1, true, this.mCardNameObserver);
                    this.mIsRegistered = true;
                } catch (UnsupportedException e2) {
                    HwLog.e("DialpadFragment", "Registering mCardNameObserver observer:" + e2.getMessage());
                }
            }
        }
        registerAirPlanModeObserver();
        if (this.mCustDialpadFragment != null) {
            this.mCustDialpadFragment.setContentObserver(getActivity());
        }
        this.mFlagAllRegisted = true;
    }

    private void registerAirPlanModeObserver() {
        if (getActivity() != null) {
            getActivity().getContentResolver().registerContentObserver(System.getUriFor("airplane_mode_on"), true, this.airPlanModeOberver);
        }
    }

    private void unregisterAirPlanModeObserver() {
        this.mResolver.unregisterContentObserver(this.airPlanModeOberver);
    }

    private synchronized void setLoadedFlag(int value) {
        this.mLoadedFlag = value;
    }

    private static void initCust() {
        if (EmuiFeatureManager.isProductCustFeatureEnable() && mCust == null) {
            mCust = (HwCustDialpadFragmentHelper) HwCustUtils.createObj(HwCustDialpadFragmentHelper.class, new Object[0]);
        }
    }

    private void onCreateOverflowMenu() {
        this.overflowMenuButton = this.fragmentView.findViewById(R.id.overflow_menu);
        if (this.overflowMenuButton != null) {
            if (this.mIsLandscape) {
                this.overflowMenuButton.setVisibility(8);
            } else {
                this.overflowMenuButton.setOnClickListener(this);
            }
        }
        View oneButton = this.fragmentView.findViewById(R.id.contacts_dialpad_one);
        if (oneButton == null) {
            return;
        }
        if (oneButton instanceof ImageView) {
            setupKeypadHuawei(this.fragmentView);
        } else {
            setupKeypad(this.fragmentView);
        }
    }

    private void onCreateTableRow() {
        this.mTableRow0 = (TableRow) this.fragmentView.findViewById(R.id.table0);
        this.mTableRow1 = (TableRow) this.fragmentView.findViewById(R.id.table1);
        this.mTableRow2 = (TableRow) this.fragmentView.findViewById(R.id.table2);
        this.mTableRow3 = (TableRow) this.fragmentView.findViewById(R.id.table3);
        this.mTableRow4 = (TableRow) this.fragmentView.findViewById(R.id.table4);
        this.mTableRowRcs = this.fragmentView.findViewById(R.id.rcs_call_view);
        if (this.mRcsCust != null) {
            this.mRcsCust.setTableRowRcs(this.mTableRowRcs, this.mLineHorizontalTopTable0);
        }
        if (CommonUtilMethods.isLayoutRTL() && this.mTableRow0 != null) {
            this.mTableRow0.setLayoutDirection(1);
        }
        if (this.isRow0ShownWhenRecreate && this.mTableRow0 != null) {
            HwLog.d("DialpadFragment", "mTableRow0 set VISIBLE1");
            if (this.mLoadedFlag == 0) {
                ((ImageView) this.fragmentView.findViewById(R.id.dialer_selector_new_contact)).setImageResource(R.drawable.ic_dialer_selector_new_contact);
                ((ImageView) this.fragmentView.findViewById(R.id.dialer_selector_add_exist_contact)).setImageResource(R.drawable.ic_dialer_selector_add_exist_contact);
                ((ImageView) this.fragmentView.findViewById(R.id.dialer_selector_send_sms)).setImageResource(R.drawable.ic_dialer_selector_send_sms);
                setLoadedFlag(1);
            }
            setTableTopRowVisibility(0);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        PLog.d(0, "DialpadFragment onCreateView begin");
        HwLog.d("DialpadFragment", "DialpadFragment.onCreateView start");
        setLoadedFlag(0);
        if (CommonUtilMethods.calcIfNeedSplitScreen() && savedState == null && (getActivity() instanceof PeopleActivity)) {
            savedState = CommonUtilMethods.getInstanceState();
        }
        Object obj = null;
        CharSequence charSequence = null;
        if (savedState != null) {
            obj = savedState.getString("digits_head_text");
            charSequence = savedState.getString("digits_head_location_text");
            this.needUpdateRecommendedButtonState = savedState.getBoolean("update_recommended_button_state");
            this.isSimOneRecommended = savedState.getBoolean("is_sim_one_recommended");
            this.isSimTwoRecommended = savedState.getBoolean("is_sim_two_recommended");
            if (this.mCustDialpadFragment != null) {
                this.mCustDialpadFragment.getEncryptButtonRecommended(savedState);
            }
            if (this.mResource.getConfiguration().orientation == 1) {
                this.isRow0ShownWhenRecreate = savedState.getBoolean("extra_table0_shown");
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragment", "isRow0ShownWhenRecreate:" + this.isRow0ShownWhenRecreate);
                }
            }
        }
        this.fragmentView = inflater.inflate(R.layout.dialpad_fragment, null, false);
        View dialpadView = ((PeopleActivity) this.mActivity).getDialpadFragmentHelper().getDialpadView();
        FragmentReplacer lFragmentReplacer = ((PeopleActivity) this.mActivity).getFragmentReplacer();
        if (dialpadView == null && (this.mActivity instanceof PeopleActivity) && lFragmentReplacer != null) {
            dialpadView = lFragmentReplacer.getDiapadView();
        }
        if (this.fragmentView == null) {
            return null;
        }
        if (CommonUtilMethods.calcIfNeedSplitScreen() && (getActivity() instanceof PeopleActivity)) {
            ScreenUtils.adjustPaddingTop(getActivity(), (ViewGroup) this.fragmentView.findViewById(R.id.dialpad_fragment_content), true);
        }
        if (this.mCustDialpadFragment != null) {
            this.mCustDialpadFragment.checkAndAddHeaderView(this.fragmentView, inflater);
        }
        this.mLoadedFlag = 0;
        this.mDialpadHuaweiContainer = (ViewGroup) this.fragmentView.findViewById(R.id.dialpad_huawei_container);
        if (this.mDialpadHuaweiContainer != null) {
            if (this.mDialpadHuaweiContainer.getChildCount() > 0) {
                this.mDialpadHuaweiContainer.removeAllViews();
            }
            if (dialpadView == null) {
                if (CommonUtilMethods.isLargeThemeApplied(this.mResource) || CommonUtilMethods.isSpecialLanguageForDialpad()) {
                    dialpadView = inflater.inflate(R.layout.dialpad_huawei, null);
                } else {
                    dialpadView = inflater.inflate(R.layout.contacts_dialpad, null);
                }
            }
            this.mWriteSpaceView = dialpadView.findViewById(R.id.white_space);
            if (!(mCust == null || CommonUtilMethods.isLargeThemeApplied(this.mResource) || CommonUtilMethods.isSpecialLanguageForDialpad())) {
                mCust.removeVvmIcon(dialpadView);
            }
            this.mDialpadHuaweiContainer.addView(dialpadView);
        }
        this.mAddButtonIpCall = (LinearLayout) this.fragmentView.findViewById(R.id.layout_additional_button_with_ip_call);
        if (this.mCustDialpadFragment != null && EncryptCallUtils.getCust().isEncryptCallEnable()) {
            this.mCustDialpadFragment.repalceAdditionalButtonRowForEncryptCall(inflater, this.mAddButtonIpCall, new DialButtonListener(0, false, true), new DialButtonListener(1, false, true));
        }
        this.mAdditionalButtonsRow = this.fragmentView.findViewById(R.id.dialpadAdditionalButtonsWithIpCall);
        this.mDialpadLine = this.fragmentView.findViewById(R.id.dialpad_line);
        this.mLineHorizontalTopTable0 = this.fragmentView.findViewById(R.id.line_horizontal_top_table0);
        this.mDialerContainer = this.fragmentView.findViewById(R.id.dialer_container);
        if (this.mIsLandscape) {
            this.mVerticalDivider = this.fragmentView.findViewById(R.id.vertical_divider);
            if (this.mDialerContainer != null && (this.mDialerContainer.getLayoutParams() instanceof RelativeLayout.LayoutParams)) {
                this.mDialerContainer.getLayoutParams().width = ContactDpiAdapter.getNewPxDpi(R.dimen.contact_dialpad_dialer_container_landscape_width, this.mActivity);
            }
            this.mDialpadLine.setVisibility(8);
            this.mLineHorizontalTopTable0.setVisibility(8);
        }
        initMenuForSplit();
        this.mAdditionalButtonsRow.setOnTouchListener(this.avoidTouchToDelegateListener);
        View newContactOptionView = this.fragmentView.findViewById(R.id.dialer_option_container);
        if (newContactOptionView instanceof ListView) {
            this.mNewContactOption = (ListView) newContactOptionView;
        }
        this.mDigitsContainer = ((PeopleActivity) this.mActivity).getDialpadFragmentHelper().getDialpadHeaderLayout();
        this.mDigits = (EditText) this.mDigitsContainer.findViewById(R.id.digits);
        this.mDigitsContainer.setOnTouchListener(this.avoidTouchToDelegateListener);
        addHeader();
        onCreateOverflowMenu();
        this.ipDialButton1Listener = new DialButtonListener(this, 0, true);
        this.ipDialButton2Listener = new DialButtonListener(this, 1, true);
        this.dialButton1Listener = new DialButtonListener(this, 0, false);
        this.dialButton2Listener = new DialButtonListener(this, 1, false);
        this.mCardNameDial2 = (LinearLayout) this.fragmentView.findViewById(R.id.nameDialButton2);
        this.mCardNameDial3 = (LinearLayout) this.fragmentView.findViewById(R.id.nameDialButton3);
        if (this.mRcsCust != null) {
            this.mRcsCust.setRcsCallView(this.mSearchButton);
        }
        initExtremeSimplicityMode();
        idForButtonLayoutWithIp(this.fragmentView);
        if (this.mCustDialpadFragment != null) {
            this.mCustDialpadFragment.setEncryptButtonBgByRecommended();
        }
        if (this.isSimOneRecommended) {
            this.mCardNameDial2.setBackgroundResource(R.drawable.rectangle);
        } else if (this.isSimTwoRecommended) {
            this.mCardNameDial3.setBackgroundResource(R.drawable.rectangle);
        } else {
            this.mCardNameDial2.setBackgroundResource(R.drawable.btn_call);
            this.mCardNameDial3.setBackgroundResource(R.drawable.btn_call);
        }
        this.mDialpad = this.fragmentView.findViewById(R.id.contacts_dialpad);
        this.mDialpad.setOnTouchListener(this.avoidTouchToDelegateListener);
        this.mNeedInitSingleHandMode = this.mActivity.getApplicationContext().getResources().getBoolean(R.bool.config_init_single_hand_mode);
        if (this.mNeedInitSingleHandMode) {
            this.mSingleHandMode = SingleHandModeManager.getInstance(this.mActivity.getApplicationContext());
        }
        if (!this.mIsLandscape) {
            this.mRightHandStub = (ViewStub) this.fragmentView.findViewById(R.id.right_hand_mode_view_stub);
            this.mLeftHandStub = (ViewStub) this.fragmentView.findViewById(R.id.left_hand_mode_view_stub);
            this.mGravityParent = (FrameLayout) this.fragmentView.findViewById(R.id.gravity_framelayout);
            this.mGestureDetector = new GestureDetector(getActivity().getApplicationContext(), new GuideGestureListener());
            this.mDialpadAnimationDown = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.dialpad_anim_down);
            this.mDialpadAnimationUp = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.dialpad_anim_up);
        }
        onCreateTableRow();
        initEncryptCallView();
        this.mHandler.postDelayed(this.mInitDialNumber, 1000);
        this.mShowDialpadLocation = (TextView) this.mDigitsContainer.findViewById(R.id.num_location);
        this.mDigits = (EditText) this.mDigitsContainer.findViewById(R.id.digits);
        ImmersionUtils.setTextViewOrEditViewImmersonColorLight(this.mActivity, this.mDigits, false);
        ImmersionUtils.setTextViewOrEditViewImmersonColorLight(this.mActivity, this.mShowDialpadLocation, true);
        this.mDigits.setKeyListener(DialerKeyListener.getInstance());
        this.mDigits.setOnClickListener(this);
        this.mDigits.setOnKeyListener(this);
        this.mDigits.setOnLongClickListener(this);
        this.mDigits.addTextChangedListener(this);
        PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(getActivity(), this.mDigits);
        if (this.mDialpad == null) {
            this.mDigits.setInputType(3);
        } else {
            this.mDigits.setCursorVisible(false);
        }
        if (!TextUtils.isEmpty(obj)) {
            this.needShowDigistHeader = false;
            setDigitsText(obj);
            this.mDigits.setCursorVisible(true);
            this.mDigits.setSelection(this.mDigits.getText().length());
        }
        if (!TextUtils.isEmpty(charSequence)) {
            this.mShowDialpadLocation.setText(charSequence);
        }
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "DialpadFragment.onCreateView end: fragmentView = " + this.fragmentView);
        }
        Activity activity = getActivity();
        NetWorkSwitchListener anonymousClass13 = new NetWorkSwitchListener() {
            public void notifyUpdate() {
                if (!DialpadFragment.this.mBePause) {
                    DialpadFragment.this.updateButtonStates(true, true);
                }
            }
        };
        this.mSwitchListener = anonymousClass13;
        ContactsApplication.addNetWorkSwitchListener(activity, anonymousClass13);
        if (mCust != null) {
            mCust.customizeDialPadView(this.fragmentView, getActivity(), inflater, this.mIsLandscape);
        }
        PLog.d(0, "DialpadFragment onCreateView end");
        loadCalllogView();
        return this.fragmentView;
    }

    private void initEncryptCallView() {
        if (HapEncryptCallUtils.isEncryptCallEnabled()) {
            int sim = -1;
            if (HapEncryptCallUtils.isCdmaBySlot(0)) {
                sim = 0;
            } else if (HapEncryptCallUtils.isCdmaBySlot(1)) {
                sim = 1;
            }
            if (sim != -1) {
                this.mEncryptCallView = this.mTableRow0.findViewById(R.id.encrypt_call);
                EncryptCallDialpadFragmentHelper.initEncryptCallView(this.mEncryptCallView, new DialButtonListener(sim, false, true), this.mSearchButton, this.mIsLandscape);
            }
        }
    }

    private void initMenuForSplit() {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && !this.mIsLandMenuForSplitLoaded) {
            SetButtonDetails setButtonDetails = null;
            SetButtonDetails moreMenuInfo = new SetButtonDetails(R.menu.dialpad_options, R.string.contacts_title_menu);
            SetButtonDetails deleteCallogMenu = new SetButtonDetails(R.drawable.ic_trash_normal, R.string.menu_deleteContact);
            SetButtonDetails greetingMenu = new SetButtonDetails(R.drawable.ic_contact_ans_vvm, R.string.menu_greeting);
            if (!this.mIsLandscape) {
                setButtonDetails = new SetButtonDetails(R.drawable.ic_contacts_keyboard_down, R.string.dialer);
            }
            this.mSplitActionBarView = (SplitActionBarView) ((ViewStub) this.fragmentView.findViewById(R.id.stub_menu_view)).inflate();
            this.mSplitActionBarView.fillDetails(setButtonDetails, deleteCallogMenu, greetingMenu, moreMenuInfo, true);
            this.mSplitActionBarView.setVisibility(3, false);
            if (this.mIsLandscape) {
                this.mSplitActionBarView.setVisibility(0);
            }
            this.mSplitActionBarView.setOnCustomMenuListener(new OnCustomMenuListener() {
                public boolean onCustomSplitMenuItemClick(int aMenuItem) {
                    switch (aMenuItem) {
                        case R.string.menu_deleteContact:
                            DialpadFragment.this.startToDeleteMultiCallLog();
                            break;
                        case R.string.menu_greeting:
                            Intent greetingIntent = new Intent("android.intent.action.VIEW");
                            greetingIntent.setData(GreetingContract$Greetings.CONTENT_URI);
                            greetingIntent.putExtra("phone_account_id", DialpadFragment.this.mPhoneAccountId);
                            greetingIntent.setPackage(DialpadFragment.this.getActivity().getPackageName());
                            DialpadFragment.this.startActivity(greetingIntent);
                            break;
                        case R.string.dialer:
                            DialpadFragment.this.startShowOrHideDialpad();
                            break;
                        default:
                            return false;
                    }
                    return true;
                }

                public boolean onCustomMenuItemClick(MenuItem aMenuItem) {
                    return DialpadFragment.this.onOptionsItemSelected(aMenuItem);
                }

                public void onPrepareOptionsMenu(Menu aMenu) {
                    DialpadFragment.this.onPrepareOptionsMenu(aMenu);
                }
            });
            this.mIsLandMenuForSplitLoaded = true;
        }
    }

    private void initExtremeSimplicityMode() {
        if (SimFactoryManager.isExtremeSimplicityMode() && this.mViewSwitcher == null) {
            this.mSwitcherClickListener = new SwitcherClickListener();
            this.mSwitcherTouchListener = new SwitcherTouchListener();
            this.mSwitcherLongClickListener = new SwitcherLongClickListener();
            this.mAnimation[0] = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_in_left);
            this.mAnimation[1] = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_out_left);
            this.mAnimation[2] = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_in_right);
            this.mAnimation[3] = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_out_right);
            LinearLayout.LayoutParams dialButtonParams = new LinearLayout.LayoutParams(ContactDpiAdapter.getNewPxDpi(R.dimen.contact_dialpad_dial_button_width, this.mActivity.getApplicationContext()), getResources().getDimensionPixelSize(R.dimen.contact_dialpad_dial_button_height));
            LayoutInflater inflater = LayoutInflater.from(this.mActivity);
            this.mDialButtonSwitcher1 = (LinearLayout) inflater.inflate(R.layout.dialpad_additonal_dial_button_switcher, null);
            this.mDialButtonSwitcher2 = (LinearLayout) inflater.inflate(R.layout.dialpad_additonal_dial_button_switcher, null);
            this.mDialButtonSwitcher1.setPadding(0, 0, 0, 0);
            this.mDialButtonSwitcher2.setPadding(0, 0, 0, 0);
            this.mDialButtonSwitcher1.setLayoutParams(dialButtonParams);
            this.mDialButtonSwitcher2.setLayoutParams(dialButtonParams);
            LinearLayout.LayoutParams dialButtonSwitcher = new LinearLayout.LayoutParams(-2, -2);
            this.mViewSwitcher = new DialpadViewSwitcher(this.mActivity);
            this.mViewSwitcher.setId(R.id.dialpad_view_switcher);
            this.mViewSwitcher.setLayoutParams(dialButtonSwitcher);
            this.mViewSwitcher.addView(this.mDialButtonSwitcher1);
            this.mViewSwitcher.addView(this.mDialButtonSwitcher2);
            this.mViewSwitcher.setBackgroundResource(R.drawable.btn_call);
            this.mViewSwitcher.setVisibility(8);
            this.mDialButtonSwitcherLayout = (LinearLayout) this.mAdditionalButtonsRow.findViewById(R.id.dialButtonSwitcherLayout);
            this.mDialButtonSwitcherLayout.addView(this.mViewSwitcher);
            if (this.mCustDialpadFragment != null) {
                this.mCustDialpadFragment.initExtremeSimplicityMode(this.mViewSwitcher, inflater, this.mDialButtonSwitcherLayout);
            }
        }
    }

    private void setVoicemailFilterTab(boolean isUser) {
        this.mButtonGroupLyout.setEndButtonSelected();
        setCallLogFilterType(4, isUser);
        if (!this.mIsLandscape) {
            this.mShowDialpad = false;
            hideDialpad();
        }
    }

    private void setVoicemailFilterTab() {
        setVoicemailFilterTab(false);
    }

    private void setCallLogFilterType(int filterType) {
        CallLogFragment lCallLogFragmentMiddle = getCallLogFragment();
        if (lCallLogFragmentMiddle != null) {
            lCallLogFragmentMiddle.setCallLogFilterType(filterType, 2);
            this.mFilterType = filterType;
        }
    }

    private void setCallLogFilterType(int filterType, boolean isUser) {
        CallLogFragment lCallLogFragmentMiddle = getCallLogFragment();
        if (lCallLogFragmentMiddle != null) {
            lCallLogFragmentMiddle.setCallLogFilterType(filterType, 2, isUser);
            this.mFilterType = filterType;
        }
    }

    private void initMotionRecognition() {
        if (this.mSingleHandMode == null || !this.mSingleHandMode.isSingleHandModeSwitchedOn()) {
            if (MotionRecognition.isMotionClassExists() && MotionRecognition.isMotionRecoApkExist(this.mActivity)) {
                destroyMotionRecognition();
            }
        } else if (getActivity() != null && !getActivity().isFinishing() && !this.mIsLandscape && this.mSingleHandMode != null) {
            if (this.mSingleHandMode.isSmartSingleHandModeOn()) {
                if (MotionRecognition.isMotionClassExists() && MotionRecognition.isMotionRecoApkExist(this.mActivity)) {
                    startMotionRecognition();
                }
            } else if (MotionRecognition.isMotionClassExists() && MotionRecognition.isMotionRecoApkExist(this.mActivity)) {
                destroyMotionRecognition();
            }
        }
    }

    private void initSingleHandMode() {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "initSingleHandMode");
        }
        if (this.mRightHandLayout == null && this.mRightHandStub != null && this.mLeftHandStub != null && this.mSingleHandMode != null) {
            View rightHandView = this.mRightHandStub.inflate();
            View leftHandView = this.mLeftHandStub.inflate();
            if (ContactDpiAdapter.NOT_SRC_DPI) {
                leftHandView.setTranslationX(this.mNormalWidth - this.mSingleHandWidth);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int) this.mSingleHandWidth, -2);
                rightHandView.setLayoutParams(lp);
                leftHandView.setLayoutParams(lp);
            }
            this.mRightHandLayout = (RelativeLayout) rightHandView.findViewById(R.id.right_hand_mode_view);
            this.mLeftHandLayout = (RelativeLayout) leftHandView.findViewById(R.id.left_hand_mode_view);
            this.mRightHandModeButton = (ImageButton) rightHandView.findViewById(R.id.right_hand_mode_btn);
            ViewUtil.setStateListIcon(getActivity(), this.mRightHandModeButton, false);
            this.mLeftHandModeButton = (ImageButton) leftHandView.findViewById(R.id.left_hand_mode_btn);
            ViewUtil.setStateListIcon(getActivity(), this.mLeftHandModeButton, false);
            if (!(this.mSingleHandAdapter == null || this.mDialerContainer == null || !(this.mDialerContainer instanceof GravityView))) {
                this.mSingleHandAdapter.setAnimatedViews(this.mRightHandLayout, this.mLeftHandLayout, (GravityView) this.mDialerContainer, this.mGravityParent);
            }
            if (!(this.mRightHandLayout == null || this.mLeftHandLayout == null)) {
                this.mRightHandLayout.setOnClickListener(this.changeHandModeOperationListener);
                this.mLeftHandLayout.setOnClickListener(this.changeHandModeOperationListener);
            }
            if (this.mSingleHandMode.isSingleHandFeatureEnabled()) {
                if (this.mSingleHandMode.isSmartSingleHandFeatureEnabled()) {
                    Uri lUriSingleSmart = this.mSingleHandMode.getUriForSmartSingleHandModeSwitch();
                    if (HwLog.HWDBG) {
                        HwLog.d("DialpadFragment", "lUriSingleSmart :: " + lUriSingleSmart);
                    }
                    if (lUriSingleSmart != null) {
                        this.mSingleHandSmartObserver = new ContentObserver(this.mHandler) {
                            public void onChange(boolean selfChange) {
                                if (HwLog.HWDBG) {
                                    HwLog.d("DialpadFragment", "mSingleHandSmartObserver onChange");
                                }
                                DialpadFragment.this.initMotionRecognition();
                            }
                        };
                        this.mResolver.registerContentObserver(lUriSingleSmart, false, this.mSingleHandSmartObserver);
                    }
                }
                Uri lUriToObserve = this.mSingleHandMode.getUriForSingleHandModeSwitch();
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragment", "lUriToObserve :: " + lUriToObserve);
                }
                if (lUriToObserve != null) {
                    this.mSingleHandModeSwitchObserver = new ContentObserver(this.mHandler) {
                        public void onChange(boolean selfChange) {
                            if (HwLog.HWDBG) {
                                HwLog.d("DialpadFragment", "mSingleHandModeSwitchObserver onChange");
                            }
                            if (DialpadFragment.this.getActivity() != null && !DialpadFragment.this.getActivity().isFinishing()) {
                                if (!(DialpadFragment.this.mSingleHandMode == null || DialpadFragment.this.mSingleHandMode.getCurrentHandMode() == DialpadFragment.this.mCurMode)) {
                                    DialpadFragment.this.updateDialpadHandModeView(DialpadFragment.this.mSingleHandMode.getCurrentHandMode(), false, false);
                                    DialpadFragment.this.updateButtonStates(true);
                                }
                                DialpadFragment.this.initMotionRecognition();
                            }
                        }
                    };
                    this.mResolver.registerContentObserver(this.mSingleHandMode.getUriForSingleHandModeSwitch(), false, this.mSingleHandModeSwitchObserver);
                }
            }
            registerSingleMode();
            ContactsPropertyChangeReceiver.registerBigScreenListener(this.mListener);
        }
    }

    private void registerSingleMode() {
        if (this.mSingleHandMode != null) {
            Uri lUriToObserve = this.mSingleHandMode.getUriForSingleHandMode();
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragment", "registerSingleMode :: " + lUriToObserve);
            }
            if (lUriToObserve != null) {
                this.mSingleHandModeObserver = new ContentObserver(this.mHandler) {
                    public void onChange(boolean selfChange) {
                        HwLog.e("DialpadFragment", "mSingleHandModeObserver onChange mBePause:" + DialpadFragment.this.mBePause);
                        if (DialpadFragment.this.getActivity() != null && !DialpadFragment.this.getActivity().isFinishing() && DialpadFragment.this.mSingleHandMode != null && !DialpadFragment.this.mBePause) {
                            int lCurrentMode = DialpadFragment.this.mSingleHandMode.getCurrentHandMode();
                            HwLog.e("DialpadFragment", "registerSingleMode :: " + lCurrentMode + " mCurMode:" + DialpadFragment.this.mCurMode);
                            if (DialpadFragment.this.mCurMode != lCurrentMode) {
                                DialpadFragment.this.updateDialpadHandModeView(lCurrentMode, false, false, false);
                                DialpadFragment.this.updateButtonStates(true);
                            }
                        }
                    }
                };
                this.mResolver.registerContentObserver(lUriToObserve, false, this.mSingleHandModeObserver);
            }
        }
    }

    public void updateButtonStatesEx(boolean aQuerySimStates) {
        updateButtonStates(aQuerySimStates);
    }

    private void updateButtonStates(boolean aQuerySimStates) {
        updateButtonStates(aQuerySimStates, false, false);
    }

    private void updateButtonStates(boolean aQuerySimStates, boolean isSetSimName) {
        updateButtonStates(aQuerySimStates, false, isSetSimName);
    }

    private void updateButtonStates(boolean aQuerySimStates, boolean async, boolean isSetSimName) {
        if (this.mDialButton != null) {
            final String digits = this.mDigits.getText().toString();
            final boolean z = isSetSimName;
            final boolean z2 = async;
            final boolean z3 = aQuerySimStates;
            Runnable run4ButtonStates = new Runnable() {
                public void run() {
                    if (z) {
                        DialpadFragment.this.getSettingsSIMName();
                    }
                    boolean isEmergencyNumber = CommonUtilMethods.isEmergencyNumber(digits, DialpadFragment.this.mIsPhoneMultiSim);
                    if (isEmergencyNumber && PhoneCapabilityTester.isTwoButtonsEmergencyDialerActive(DialpadFragment.this.mActivity)) {
                        if (z2) {
                            DialpadFragment.this.mHandler.post(new Runnable() {
                                public void run() {
                                    DialpadFragment.this.setButtonStatesForEmergencyNumber();
                                }
                            });
                        } else {
                            DialpadFragment.this.setButtonStatesForEmergencyNumber();
                        }
                    } else if (DialpadFragment.this.mDialButton.isEnabled() && DialpadFragment.this.mIsPhoneMultiSim && !PhoneCapabilityTester.isTwoButtonsEmergencyDialerActive(DialpadFragment.this.mActivity) && isEmergencyNumber) {
                        r0 = new Runnable() {
                            public void run() {
                                if (DialpadFragment.this.mViewSwitcher != null) {
                                    DialpadFragment.this.mViewSwitcher.setVisibility(8);
                                }
                                if (DialpadFragment.this.mCustDialpadFragment != null) {
                                    DialpadFragment.this.mCustDialpadFragment.hideSwitchButton();
                                    DialpadFragment.this.mCustDialpadFragment.hideEncryptCallButton();
                                }
                                DialpadFragment.this.mCardNameDial2.setVisibility(8);
                                DialpadFragment.this.mCardNameDial3.setVisibility(8);
                                if (HwScenceTransition.isAnimationEnd) {
                                    DialpadFragment.this.mDialButton.setVisibility(0);
                                }
                                DialpadFragment.this.setButtonsLayoutForNoSim();
                            }
                        };
                        if (z2) {
                            DialpadFragment.this.mHandler.post(r0);
                        } else {
                            r0.run();
                        }
                    } else {
                        int simPresence;
                        boolean digitsNotEmpty = !DialpadFragment.this.isDigitsEmpty();
                        if (z3) {
                            DialpadFragment.this.mIsSim1Present = SimFactoryManager.isSIM1CardPresent();
                            DialpadFragment.this.mIsSim2Present = SimFactoryManager.isSIM2CardPresent();
                            DialpadFragment.this.mIsSim1Enabled = DialpadFragment.this.mIsSim1Present ? SimFactoryManager.isSimEnabled(0) : false;
                            DialpadFragment.this.mIsSim2Enabled = DialpadFragment.this.mIsSim2Present ? SimFactoryManager.isSimEnabled(1) : false;
                        }
                        if (DialpadFragment.this.mIsSim1Present) {
                            simPresence = 1;
                        } else {
                            simPresence = 0;
                        }
                        if (DialpadFragment.this.mIsSim2Present) {
                            simPresence |= 2;
                        }
                        boolean hasLastDialed = !TextUtils.isEmpty(DialpadFragment.this.mLastNumberDialed);
                        boolean isSim1Ready = DialpadFragment.this.mIsSim1Enabled;
                        boolean isSim2Ready = DialpadFragment.this.mIsSim2Enabled;
                        boolean z = false;
                        boolean z2 = false;
                        if ((isSim1Ready || isSim2Ready) && !CommonUtilMethods.isAirplaneModeOn(DialpadFragment.this.mActivity) && DialpadFragment.this.mIsPhoneMultiSim) {
                            boolean isSecondSubIdle = true;
                            boolean isFirstSubIdle = true;
                            int subscriptionId1 = SimFactoryManager.getSubscriptionIdBasedOnSlot(0);
                            int subscriptionId2 = SimFactoryManager.getSubscriptionIdBasedOnSlot(1);
                            boolean isDsdaMode = SimFactoryManager.isMultiSimDsda();
                            boolean isS1Cdma = SimFactoryManager.isCdma(subscriptionId1);
                            if (!isDsdaMode) {
                                if (subscriptionId2 != -1) {
                                    isSecondSubIdle = !SimFactoryManager.phoneIsOffhook(1);
                                }
                                if (subscriptionId1 != -1) {
                                    isFirstSubIdle = !SimFactoryManager.phoneIsOffhook(0);
                                }
                            }
                            z = (isSim1Ready && isSecondSubIdle) ? (digitsNotEmpty || hasLastDialed) ? true : SimFactoryManager.phoneIsOffhook(subscriptionId1) ? isS1Cdma : false : false;
                            if (!isSim2Ready || !isFirstSubIdle) {
                                z2 = false;
                            } else if (digitsNotEmpty || hasLastDialed) {
                                z2 = true;
                            } else if (SimFactoryManager.phoneIsOffhook(subscriptionId2)) {
                                z2 = SimFactoryManager.isCdma(subscriptionId2);
                            } else {
                                z2 = false;
                            }
                            HwLog.i("DialpadFragment", "SIM 1 enable:" + z + ", SIM 2 enable:" + z2);
                        } else {
                            simPresence = 0;
                        }
                        HwLog.i("DialpadFragment", "Sim presence : " + simPresence);
                        final boolean lSim1Enabled;
                        final boolean lSim2Enabled;
                        switch (simPresence) {
                            case 0:
                                r0 = new Runnable() {
                                    public void run() {
                                        if (DialpadFragment.this.mIsPhoneMultiSim) {
                                            DialpadFragment.this.setButtonsIfNoSim();
                                        }
                                        DialpadFragment.this.setButtonsLayoutForNoSim();
                                        DialpadFragment.this.updateEncryptButton();
                                    }
                                };
                                if (!z2) {
                                    r0.run();
                                    break;
                                } else {
                                    DialpadFragment.this.mHandler.post(r0);
                                    break;
                                }
                            case 1:
                                lSim1Enabled = z;
                                lSim2Enabled = z2;
                                r0 = new Runnable() {
                                    public void run() {
                                        if (DialpadFragment.this.mViewSwitcher != null) {
                                            DialpadFragment.this.mViewSwitcher.setVisibility(8);
                                        }
                                        if (DialpadFragment.this.mCustDialpadFragment != null) {
                                            DialpadFragment.this.mCustDialpadFragment.hideSwitchButton();
                                        }
                                        DialpadFragment.this.mCardNameDial2.setEnabled(lSim1Enabled);
                                        DialpadFragment.this.mCardNameDial3.setEnabled(lSim1Enabled);
                                        DialpadFragment.this.mCardNameDial2.setClickable(lSim1Enabled);
                                        DialpadFragment.this.mCardNameDial3.setClickable(lSim1Enabled);
                                        DialpadFragment.this.setCardNameDialMarginEnd(0);
                                        DialpadFragment.this.setButtonsForOneSim(DialpadFragment.this.dialButton1Listener, DialpadFragment.this.ipDialButton1Listener);
                                        if (DialpadFragment.this.mCustDialpadFragment == null || !EncryptCallUtils.getCust().isCallCard1Encrypt()) {
                                            if (DialpadFragment.this.mCustDialpadFragment != null) {
                                                DialpadFragment.this.mCustDialpadFragment.hideEncryptCallButton();
                                            }
                                            DialpadFragment.this.setSingleButtonsLayout(DialpadFragment.this.mDialButton);
                                            DialpadFragment.this.updateDialAndDeleteButtonEnabledState();
                                            HwLog.i("DialpadFragment", "sim_one is presence -> updateSingleCardButton");
                                            DialpadFragment.this.updateSingleCardButton();
                                        } else {
                                            DialpadFragment.this.mCustDialpadFragment.setButtonsLayoutForOneSimWithEncryptCall(lSim1Enabled, lSim2Enabled, DialpadFragment.this.mCardNameDial2, 0, DialpadFragment.this.mIsLandscape, DialpadFragment.this.isInLeftOrRightState());
                                        }
                                        DialpadFragment.this.updateEncryptButton();
                                    }
                                };
                                if (!z2) {
                                    r0.run();
                                    break;
                                } else {
                                    DialpadFragment.this.mHandler.post(r0);
                                    break;
                                }
                            case 2:
                                lSim2Enabled = z2;
                                lSim1Enabled = z;
                                r0 = new Runnable() {
                                    public void run() {
                                        if (DialpadFragment.this.mViewSwitcher != null) {
                                            DialpadFragment.this.mViewSwitcher.setVisibility(8);
                                        }
                                        if (DialpadFragment.this.mCustDialpadFragment != null) {
                                            DialpadFragment.this.mCustDialpadFragment.hideSwitchButton();
                                        }
                                        DialpadFragment.this.mCardNameDial2.setEnabled(lSim2Enabled);
                                        DialpadFragment.this.mCardNameDial3.setEnabled(lSim2Enabled);
                                        DialpadFragment.this.mCardNameDial2.setClickable(lSim2Enabled);
                                        DialpadFragment.this.mCardNameDial3.setClickable(lSim2Enabled);
                                        DialpadFragment.this.mCard1Name = DialpadFragment.this.mCard2Name;
                                        DialpadFragment.this.setCardNameDialMarginEnd(0);
                                        DialpadFragment.this.setButtonsForOneSim(DialpadFragment.this.dialButton2Listener, DialpadFragment.this.ipDialButton2Listener);
                                        if (DialpadFragment.this.mCustDialpadFragment == null || !EncryptCallUtils.getCust().isCallCard2Encrypt()) {
                                            if (DialpadFragment.this.mCustDialpadFragment != null) {
                                                DialpadFragment.this.mCustDialpadFragment.hideEncryptCallButton();
                                            }
                                            DialpadFragment.this.setSingleButtonsLayout(DialpadFragment.this.mDialButton);
                                            HwLog.i("DialpadFragment", "sim_two is presence -> updateSingleCardButton");
                                            DialpadFragment.this.updateSingleCardButton();
                                            DialpadFragment.this.updateDialAndDeleteButtonEnabledState();
                                        } else {
                                            DialpadFragment.this.mCustDialpadFragment.setButtonsLayoutForOneSimWithEncryptCall(lSim1Enabled, lSim2Enabled, DialpadFragment.this.mCardNameDial2, 1, DialpadFragment.this.mIsLandscape, DialpadFragment.this.isInLeftOrRightState());
                                        }
                                        DialpadFragment.this.updateEncryptButton();
                                    }
                                };
                                if (!z2) {
                                    r0.run();
                                    break;
                                } else {
                                    DialpadFragment.this.mHandler.post(r0);
                                    break;
                                }
                            case 3:
                                DialpadFragment.this.mCardNameDial2.setOnClickListener(DialpadFragment.this.dialButton1Listener);
                                final int dial2Visible = isSim1Ready ? 0 : 8;
                                final int dial3Visible = isSim2Ready ? 0 : 8;
                                lSim1Enabled = z;
                                lSim2Enabled = z2;
                                DialpadFragment.this.mExtremeSimpleDefaultSimcard = SimFactoryManager.getDefaultSimcard();
                                HwLog.i("DialpadFragment", "Default simcard slotid : " + DialpadFragment.this.mExtremeSimpleDefaultSimcard);
                                Runnable run4BothSim = new Runnable() {
                                    public void run() {
                                        if (SimFactoryManager.isExtremeSimplicityMode()) {
                                            DialpadFragment.this.mCardNameDial2.setVisibility(8);
                                            DialpadFragment.this.mCardNameDial3.setVisibility(8);
                                            if (DialpadFragment.this.mViewSwitcher == null) {
                                                DialpadFragment.this.initExtremeSimplicityMode();
                                            }
                                            if (DialpadFragment.this.mCustDialpadFragment != null) {
                                                DialpadFragment.this.mCustDialpadFragment.hideEncryptCallButton();
                                            }
                                            DialpadFragment.this.mViewSwitcher.setVisibility(0);
                                            DialpadFragment.this.mViewSwitcher.setEnabled(true);
                                            DialpadFragment.this.mViewSwitcher.setClickable(DialpadFragment.this.mExtremeSimpleDefaultSimcard == 0 ? lSim1Enabled : lSim2Enabled);
                                            DialpadFragment.this.mViewSwitcher.setLongClickable(true);
                                            DialpadFragment.this.mViewSwitcher.setOnClickListener(DialpadFragment.this.mSwitcherClickListener);
                                            DialpadFragment.this.mViewSwitcher.setOnTouchListener(DialpadFragment.this.mSwitcherTouchListener);
                                            DialpadFragment.this.mViewSwitcher.setOnLongClickListener(DialpadFragment.this.mSwitcherLongClickListener);
                                            if (DialpadFragment.this.mExtremeSimpleDefaultSimcard != DialpadFragment.this.mViewSwitcher.getDisplayedChild()) {
                                                DialpadFragment.this.mViewSwitcher.setDisplayedChild(DialpadFragment.this.mExtremeSimpleDefaultSimcard);
                                            }
                                            if (DialpadFragment.this.mCustDialpadFragment != null && EncryptCallUtils.getCust().isEncryptCallEnable()) {
                                                DialpadFragment.this.mCustDialpadFragment.setSwitcherBtnState(new DialButtonListener(DialpadFragment.this.mExtremeSimpleDefaultSimcard, false, true), DialpadFragment.this.mSwitcherTouchListener, DialpadFragment.this.mSwitcherLongClickListener, DialpadFragment.this.mIsLandscape, DialpadFragment.this.isInLeftOrRightState(), lSim1Enabled, lSim2Enabled);
                                            }
                                            DialpadFragment.this.mDialButtonSwitcher1.setEnabled(lSim1Enabled);
                                            DialpadFragment.this.mDialButtonSwitcher2.setEnabled(lSim2Enabled);
                                            DialpadFragment.this.mDialButtonSwitcher1.findViewById(R.id.button_text_switcher).setVisibility(0);
                                            DialpadFragment.this.mDialButtonSwitcher2.findViewById(R.id.button_text_switcher).setVisibility(0);
                                            if (dial2Visible == 0 && dial3Visible == 0) {
                                                DialpadFragment.this.mDialButton.setVisibility(8);
                                                if (DialpadFragment.this.mDialButtonSwitcherLayout != null) {
                                                    DialpadFragment.this.mDialButtonSwitcherLayout.setVisibility(0);
                                                }
                                                DialpadFragment.setDialBtnTextAndImage(DialpadFragment.this.mDialButtonSwitcher1, DialpadFragment.this.mCard1Name, DialpadFragment.this.sim1DialRes, true, 0);
                                                DialpadFragment.setDialBtnTextAndImage(DialpadFragment.this.mDialButtonSwitcher2, DialpadFragment.this.mCard2Name, DialpadFragment.this.sim2DialRes, true, 1);
                                                if (DialpadFragment.this.mCustDialpadFragment == null || !EncryptCallUtils.getCust().isEncryptCallEnable()) {
                                                    DialpadFragment.this.setButtonsLayoutForBothSim();
                                                } else {
                                                    DialpadFragment.this.mCustDialpadFragment.setSwitcherBtnLayout(lSim1Enabled, lSim2Enabled, DialpadFragment.this.mDialButtonSwitcher1, DialpadFragment.this.mDialButtonSwitcher2, DialpadFragment.this.mIsLandscape, DialpadFragment.this.isInLeftOrRightState());
                                                }
                                            } else if (dial2Visible == 0) {
                                                DialpadFragment.this.setSingleDialBtnLayout(DialpadFragment.this.mDialButton);
                                                HwLog.i("DialpadFragment", "both_sim is presence, dial2 is visible -> updateSingleCardButton");
                                                DialpadFragment.this.updateSingleCardButton();
                                                if (HwScenceTransition.isAnimationEnd) {
                                                    DialpadFragment.this.mDialButton.setVisibility(0);
                                                }
                                                DialpadFragment.this.setButtonsForOneSim(DialpadFragment.this.dialButton1Listener, DialpadFragment.this.ipDialButton1Listener);
                                            } else if (dial3Visible == 0) {
                                                DialpadFragment.this.setSingleDialBtnLayout(DialpadFragment.this.mDialButton);
                                                HwLog.i("DialpadFragment", "both_sim is presence, dial3 is visible -> updateSingleCardButton");
                                                DialpadFragment.this.updateSingleCardButton();
                                                if (HwScenceTransition.isAnimationEnd) {
                                                    DialpadFragment.this.mDialButton.setVisibility(0);
                                                }
                                                DialpadFragment.this.setButtonsForOneSim(DialpadFragment.this.dialButton2Listener, DialpadFragment.this.ipDialButton2Listener);
                                            }
                                        } else {
                                            int i;
                                            if (DialpadFragment.this.mDialButtonSwitcherLayout != null) {
                                                DialpadFragment.this.mDialButtonSwitcherLayout.setVisibility(8);
                                            }
                                            if (DialpadFragment.this.mViewSwitcher != null) {
                                                DialpadFragment.this.mViewSwitcher.setVisibility(8);
                                            }
                                            if (DialpadFragment.this.mCustDialpadFragment != null) {
                                                DialpadFragment.this.mCustDialpadFragment.hideSwitchButton();
                                            }
                                            DialpadFragment.this.mCardNameDial2.setVisibility(dial2Visible);
                                            DialpadFragment.this.mCardNameDial2.setEnabled(lSim1Enabled);
                                            DialpadFragment.this.mCardNameDial2.setClickable(lSim1Enabled);
                                            View findViewById = DialpadFragment.this.mCardNameDial2.findViewById(R.id.button_text);
                                            if (DialpadFragment.this.isInLeftOrRightState()) {
                                                i = 8;
                                            } else {
                                                i = 0;
                                            }
                                            findViewById.setVisibility(i);
                                            DialpadFragment.this.mCardNameDial3.setOnClickListener(DialpadFragment.this.dialButton2Listener);
                                            DialpadFragment.this.mCardNameDial3.setVisibility(dial3Visible);
                                            DialpadFragment.this.mCardNameDial3.setEnabled(lSim2Enabled);
                                            DialpadFragment.this.mCardNameDial3.setClickable(lSim2Enabled);
                                            DialpadFragment.this.mCardNameDial3.findViewById(R.id.button_text).setVisibility(DialpadFragment.this.isInLeftOrRightState() ? 8 : 0);
                                            if (dial2Visible == 0 && dial3Visible == 0) {
                                                DialpadFragment.this.mDialButton.setVisibility(8);
                                                DialpadFragment.setDialBtnTextAndImage(DialpadFragment.this.mCardNameDial2, DialpadFragment.this.mCard1Name, DialpadFragment.this.sim1DialRes, false, 0);
                                                DialpadFragment.setDialBtnTextAndImage(DialpadFragment.this.mCardNameDial3, DialpadFragment.this.mCard2Name, DialpadFragment.this.sim2DialRes, false, 1);
                                                DialpadFragment.this.setCardNameDialMarginEnd(DialpadFragment.this.mCardNameDial2.getContext().getResources().getDimensionPixelSize(R.dimen.dialpad_additional_buttions_one_marginend));
                                                if (DialpadFragment.this.mCustDialpadFragment == null || !(EncryptCallUtils.getCust().isCallCard1Encrypt() || EncryptCallUtils.getCust().isCallCard2Encrypt())) {
                                                    DialpadFragment.this.setButtonsLayoutForBothSim();
                                                } else {
                                                    DialpadFragment.this.mCustDialpadFragment.setButtonsLayoutForBothSimWithEncryptCall(lSim1Enabled, lSim2Enabled, DialpadFragment.this.mCardNameDial2, DialpadFragment.this.mCardNameDial3, DialpadFragment.this.mIsLandscape, DialpadFragment.this.isInLeftOrRightState());
                                                }
                                            } else if (dial2Visible == 0) {
                                                if (HwScenceTransition.isAnimationEnd) {
                                                    DialpadFragment.this.mDialButton.setVisibility(0);
                                                }
                                                DialpadFragment.this.mCardNameDial2.setVisibility(8);
                                                DialpadFragment.this.mCardNameDial3.setVisibility(8);
                                                if (DialpadFragment.this.mCustDialpadFragment == null || !EncryptCallUtils.getCust().isCallCard1Encrypt()) {
                                                    if (DialpadFragment.this.mCustDialpadFragment != null) {
                                                        DialpadFragment.this.mCustDialpadFragment.hideEncryptCallButton();
                                                    }
                                                    DialpadFragment.this.setSingleButtonsLayout(DialpadFragment.this.mDialButton);
                                                    HwLog.i("DialpadFragment", "both_sim is presence, call card1 is not encrypt -> updateSingleCardButton");
                                                    DialpadFragment.this.updateSingleCardButton();
                                                    DialpadFragment.this.updateDialAndDeleteButtonEnabledState();
                                                } else {
                                                    DialpadFragment.this.mCustDialpadFragment.setButtonsLayoutForOneSimWithEncryptCall(lSim1Enabled, lSim2Enabled, DialpadFragment.this.mCardNameDial2, 0, DialpadFragment.this.mIsLandscape, DialpadFragment.this.isInLeftOrRightState());
                                                }
                                                DialpadFragment.this.setButtonsForOneSim(DialpadFragment.this.dialButton1Listener, DialpadFragment.this.ipDialButton1Listener);
                                            } else if (dial3Visible == 0) {
                                                if (HwScenceTransition.isAnimationEnd) {
                                                    DialpadFragment.this.mDialButton.setVisibility(0);
                                                }
                                                DialpadFragment.this.mCardNameDial2.setVisibility(8);
                                                DialpadFragment.this.mCardNameDial3.setVisibility(8);
                                                if (DialpadFragment.this.mCustDialpadFragment == null || !EncryptCallUtils.getCust().isCallCard2Encrypt()) {
                                                    if (DialpadFragment.this.mCustDialpadFragment != null) {
                                                        DialpadFragment.this.mCustDialpadFragment.hideEncryptCallButton();
                                                    }
                                                    DialpadFragment.this.setSingleButtonsLayout(DialpadFragment.this.mDialButton);
                                                    HwLog.i("DialpadFragment", "both_sim is presence, call card2 is not encrypt -> updateSingleCardButton");
                                                    DialpadFragment.this.updateSingleCardButton();
                                                    DialpadFragment.this.updateDialAndDeleteButtonEnabledState();
                                                } else {
                                                    DialpadFragment.this.mCustDialpadFragment.setButtonsLayoutForOneSimWithEncryptCall(lSim1Enabled, lSim2Enabled, DialpadFragment.this.mCardNameDial3, 1, DialpadFragment.this.mIsLandscape, DialpadFragment.this.isInLeftOrRightState());
                                                }
                                                DialpadFragment.this.setButtonsForOneSim(DialpadFragment.this.dialButton2Listener, DialpadFragment.this.ipDialButton2Listener);
                                            }
                                        }
                                        DialpadFragment.this.updateEncryptButton();
                                    }
                                };
                                if (!z2) {
                                    run4BothSim.run();
                                    break;
                                } else {
                                    DialpadFragment.this.mHandler.post(run4BothSim);
                                    break;
                                }
                            default:
                                HwLog.w("DialpadFragment", "Invalid sim presence status: " + simPresence);
                                break;
                        }
                    }
                }
            };
            if (async) {
                BackgroundGenricHandler.getInstance().post(run4ButtonStates);
            } else {
                run4ButtonStates.run();
            }
        }
    }

    private void setCardNameDialMarginEnd(int marginEnd) {
        if (this.mCardNameDial2 != null) {
            LayoutParams lp = this.mCardNameDial2.getLayoutParams();
            if (lp instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) lp).setMarginEnd(marginEnd);
            }
        }
    }

    private int getDimenPixelSize(int dimenId) {
        return ContactDpiAdapter.getNewPxDpi(dimenId, this.mActivity);
    }

    private int getRealDimenPixelSize(int dimenId) {
        return this.mResource.getDimensionPixelSize(dimenId);
    }

    private void setSingleButtonsLayout(View dialButton) {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "setSingleButtonsLayout");
        }
        setSingleDialBtnLayout(dialButton);
        setSearchBtnsLayout(false);
    }

    public void setSearchBtnsLayout(boolean hasBothSim) {
        setSearchBtnsLayout(hasBothSim, false);
    }

    public void setSearchBtnsLayout(boolean hasBothSim, boolean isEncrypt) {
        int paddingEnd;
        int paddingStart;
        boolean preventWrongOperation = false;
        DisplayMetrics metrics;
        int temp;
        if (this.mIsLandscape) {
            metrics = getContext().getResources().getDisplayMetrics();
            if (!hasBothSim) {
                paddingEnd = ((((metrics.widthPixels / 2) - getRealDimenPixelSize(R.dimen.dialpad_dial_button_one_simcard_width)) / 2) - (metrics.widthPixels / 12)) - (getDimenPixelSize(R.dimen.contact_dialpad_delete_button_width) / 2);
                paddingStart = (metrics.widthPixels / 12) - (getRealDimenPixelSize(R.dimen.contact_dialpad_delete_button_width) / 2);
                if (CommonUtilMethods.isLayoutRTL()) {
                    temp = paddingStart;
                    paddingStart = paddingEnd;
                    paddingEnd = temp;
                }
            } else if (SimFactoryManager.isExtremeSimplicityMode()) {
                paddingEnd = ((((metrics.widthPixels / 2) - getDimenPixelSize(R.dimen.contact_dialpad_dial_button_width_landscape)) / 2) - (metrics.widthPixels / 12)) - (getDimenPixelSize(R.dimen.contact_dialpad_delete_button_width) / 2);
                paddingStart = (metrics.widthPixels / 12) - (getDimenPixelSize(R.dimen.contact_dialpad_delete_button_width) / 2);
                if (CommonUtilMethods.isLayoutRTL()) {
                    temp = paddingStart;
                    paddingStart = paddingEnd;
                    paddingEnd = temp;
                }
            } else if (CommonUtilMethods.isLayoutRTL()) {
                paddingStart = getDimenPixelSize(R.dimen.dialpad_dial_button_multi_simcard_icon_land_padding_start);
                paddingEnd = getDimenPixelSize(R.dimen.dialpad_dial_button_multi_simcard_icon_land_padding_end);
            } else {
                paddingStart = getDimenPixelSize(R.dimen.dialpad_dial_button_multi_simcard_icon_land_padding_end);
                paddingEnd = getDimenPixelSize(R.dimen.dialpad_dial_button_multi_simcard_icon_land_padding_start);
            }
        } else if (this.mShowDialpad) {
            if (hasBothSim) {
                metrics = getContext().getResources().getDisplayMetrics();
                if (!isInLeftOrRightState()) {
                    if (SimFactoryManager.isExtremeSimplicityMode()) {
                        paddingStart = (getRealDimenPixelSize(R.dimen.dialpad_table_row_padding_start) + ((metrics.widthPixels - (getRealDimenPixelSize(R.dimen.dialpad_table_row_padding_start) * 2)) / 6)) - (getRealDimenPixelSize(R.dimen.contact_dialpad_delete_button_width) / 2);
                        paddingEnd = (((metrics.widthPixels - getDimenPixelSize(R.dimen.contact_dialpad_dial_button_width)) / 2) - paddingStart) - getRealDimenPixelSize(R.dimen.contact_dialpad_delete_button_width);
                        if (CommonUtilMethods.isLayoutRTL()) {
                            temp = paddingEnd;
                            paddingEnd = paddingStart;
                            paddingStart = temp;
                        }
                        preventWrongOperation = true;
                    } else {
                        paddingEnd = getDimenPixelSize(R.dimen.contact_dialpad_btn_search_padding_end_2sim);
                        paddingStart = ((((metrics.widthPixels - (getDimenPixelSize(R.dimen.contact_dialpad_dial_button_width) * 2)) - (paddingEnd * 2)) - getRealDimenPixelSize(R.dimen.dialpad_additional_buttions_one_marginend)) - (getRealDimenPixelSize(R.dimen.contact_dialpad_delete_button_width) * 2)) / 2;
                        if (CommonUtilMethods.isLayoutRTL()) {
                            temp = paddingEnd;
                            paddingEnd = paddingStart;
                            paddingStart = temp;
                        }
                    }
                    if (isEncrypt && this.mCustDialpadFragment != null) {
                        paddingStart = this.mCustDialpadFragment.updateSearchBtnsPaddingStart(paddingStart);
                        paddingEnd = this.mCustDialpadFragment.updateSearchBtnsPaddingEnd(paddingStart);
                    }
                } else if (SimFactoryManager.isExtremeSimplicityMode()) {
                    paddingStart = ((metrics.widthPixels - getDimenPixelSize(R.dimen.contact_dialpad_single_hand_width)) / 6) - (getRealDimenPixelSize(R.dimen.contact_dialpad_delete_button_width) / 2);
                    paddingEnd = ((((metrics.widthPixels - getDimenPixelSize(R.dimen.contact_dialpad_single_hand_width)) - getDimenPixelSize(R.dimen.contact_dialpad_dial_button_width)) / 2) - paddingStart) - getRealDimenPixelSize(R.dimen.contact_dialpad_delete_button_width);
                    if (CommonUtilMethods.isLayoutRTL()) {
                        temp = paddingEnd;
                        paddingEnd = paddingStart;
                        paddingStart = temp;
                    }
                } else {
                    paddingEnd = getDimenPixelSize(R.dimen.contact_dialpad_btn_single_multicard_padding_end);
                    paddingStart = (((((metrics.widthPixels - getDimenPixelSize(R.dimen.contact_dialpad_single_hand_width)) - getRealDimenPixelSize(R.dimen.dialpad_additional_buttions_one_marginend)) - (getDimenPixelSize(R.dimen.contact_dialpad_dial_button_single_width_2sim) * 2)) - (getRealDimenPixelSize(R.dimen.contact_dialpad_delete_button_width) * 2)) - (paddingEnd * 2)) / 2;
                    if (CommonUtilMethods.isLayoutRTL()) {
                        temp = paddingEnd;
                        paddingEnd = paddingStart;
                        paddingStart = temp;
                    }
                }
            } else {
                metrics = getContext().getResources().getDisplayMetrics();
                if (isInLeftOrRightState()) {
                    paddingEnd = ((((metrics.widthPixels - getDimenPixelSize(R.dimen.contact_dialpad_single_hand_width)) / 2) - (getRealDimenPixelSize(R.dimen.dialpad_dial_button_one_simcard_width) / 2)) - ((metrics.widthPixels - getDimenPixelSize(R.dimen.contact_dialpad_single_hand_width)) / 6)) - (getRealDimenPixelSize(R.dimen.contact_dialpad_delete_button_width) / 2);
                    paddingStart = ((metrics.widthPixels - getDimenPixelSize(R.dimen.contact_dialpad_single_hand_width)) / 6) - (getRealDimenPixelSize(R.dimen.contact_dialpad_delete_button_width) / 2);
                    if (CommonUtilMethods.isLayoutRTL()) {
                        temp = paddingEnd;
                        paddingEnd = paddingStart;
                        paddingStart = temp;
                    }
                } else {
                    paddingEnd = ((((metrics.widthPixels / 2) - (getRealDimenPixelSize(R.dimen.dialpad_dial_button_one_simcard_width) / 2)) - ((metrics.widthPixels - (getRealDimenPixelSize(R.dimen.dialpad_table_row_padding_start) * 2)) / 6)) - getRealDimenPixelSize(R.dimen.dialpad_table_row_padding_start)) - (getRealDimenPixelSize(R.dimen.contact_dialpad_delete_button_width) / 2);
                    paddingStart = (((metrics.widthPixels - (getRealDimenPixelSize(R.dimen.dialpad_table_row_padding_start) * 2)) / 6) + getRealDimenPixelSize(R.dimen.dialpad_table_row_padding_start)) - (getRealDimenPixelSize(R.dimen.contact_dialpad_delete_button_width) / 2);
                    if (CommonUtilMethods.isLayoutRTL()) {
                        temp = paddingEnd;
                        paddingEnd = paddingStart;
                        paddingStart = temp;
                    }
                    preventWrongOperation = true;
                }
            }
        } else if (CommonUtilMethods.isSimplifiedModeEnabled()) {
            paddingEnd = 0;
            paddingStart = 0;
        } else {
            paddingEnd = getDimenPixelSize(R.dimen.contact_dialpad_btn_search_padding_hidden);
            paddingStart = getDimenPixelSize(R.dimen.contact_dialpad_btn_search_padding_hidden);
        }
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "setSearchBtnsLayout paddingStart End:" + paddingStart + HwCustPreloadContacts.EMPTY_STRING + paddingEnd);
        }
        boolean isMirror = CommonUtilMethods.isLayoutRTL();
        View view = this.mDeleteButton;
        boolean z = preventWrongOperation ? isMirror : false;
        boolean z2 = preventWrongOperation && !isMirror;
        setBtnViewLayoutParams(view, paddingEnd, 0, paddingStart, 0, z, z2, isMirror);
        View view2 = this.mSearchButton;
        boolean z3 = preventWrongOperation && !isMirror;
        setBtnViewLayoutParams(view2, paddingStart, 0, paddingEnd, 0, z3, preventWrongOperation ? isMirror : false, isMirror);
        view = this.overflowMenuButton;
        if (preventWrongOperation) {
            z = isMirror;
        } else {
            z = false;
        }
        if (!preventWrongOperation || isMirror) {
            z2 = false;
        } else {
            z2 = true;
        }
        setBtnViewLayoutParams(view, paddingEnd, 0, paddingStart, 0, z, z2, isMirror);
    }

    private void setBtnViewLayoutParams(View view, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom, boolean isSetLeftMargin, boolean isSetRightMargin, boolean isMirror) {
        if (view != null) {
            int right;
            int leftMargin = isSetLeftMargin ? getRealDimenPixelSize(R.dimen.dialpad_table_row_padding_start) : 0;
            int rightMargin = isSetRightMargin ? getRealDimenPixelSize(R.dimen.dialpad_table_row_padding_end) : 0;
            MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
            layoutParams.leftMargin = leftMargin;
            layoutParams.rightMargin = rightMargin;
            view.setLayoutParams(layoutParams);
            int left = isSetLeftMargin ? paddingLeft - leftMargin : paddingLeft;
            if (isSetRightMargin) {
                right = paddingRight - rightMargin;
            } else {
                right = paddingRight;
            }
            view.setPadding(left, paddingTop, right, paddingBottom);
        }
    }

    private void setSingleDialBtnLayout(View button) {
        if (this.mViewSwitcher != null) {
            this.mViewSwitcher.setVisibility(8);
        }
        if (this.mDialButtonSwitcherLayout != null) {
            this.mDialButtonSwitcherLayout.setVisibility(8);
        }
        if (button != null) {
            LayoutParams params = button.getLayoutParams();
            if (params instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams paramsDial = (LinearLayout.LayoutParams) params;
                if (isAdded()) {
                    int dialBtnWidthLandScape = getResources().getDimensionPixelSize(R.dimen.dialpad_dial_button_one_simcard_width);
                    int dialBtnHeightLandScape = getResources().getDimensionPixelSize(R.dimen.dialpad_dial_button_one_simcard_height);
                    paramsDial.width = dialBtnWidthLandScape;
                    paramsDial.height = dialBtnHeightLandScape;
                    paramsDial.topMargin = ContactDpiAdapter.getNewPxDpi(R.dimen.dialpad_dial_button_one_simcard_marigin_top, this.mActivity.getApplicationContext());
                    paramsDial.bottomMargin = ContactDpiAdapter.getNewPxDpi(R.dimen.dialpad_dial_button_one_simcard_marigin_bottom, this.mActivity.getApplicationContext());
                    button.setLayoutParams(paramsDial);
                } else {
                    HwLog.w("DialpadFragment", "DialpadFragment not attached to Activity");
                }
            }
        }
    }

    private void setButtonsLayoutForNoSim() {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "setButtonsLayoutForNoSim");
        }
        setSingleDialBtnLayout(this.mDialButton);
        setSearchBtnsLayout(false);
        updateDialAndDeleteButtonEnabledState();
        HwLog.i("DialpadFragment", "set buttons layout for no sim -> updateSingleCardButton");
        updateSingleCardButton();
        setButtonsForOneSim(this.dialButton1Listener, this.ipDialButton1Listener);
    }

    private void setButtonsLayoutForBothSim() {
        int btnWidth;
        int btnHeight;
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "setButtonsLayoutForBothSim");
        }
        setSearchBtnsLayout(true);
        boolean isExtremeSimplicity = SimFactoryManager.isExtremeSimplicityMode();
        if (this.mIsLandscape) {
            if (isExtremeSimplicity) {
                btnWidth = ContactDpiAdapter.getNewPxDpi(R.dimen.contact_dialpad_dial_button_width_landscape, this.mActivity);
            } else {
                btnWidth = ContactDpiAdapter.getNewPxDpi(R.dimen.contact_dialpad_dial_button_width, this.mActivity);
            }
            btnHeight = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_height);
        } else {
            if (isExtremeSimplicity) {
                if (isInLeftOrRightState()) {
                    btnWidth = ContactDpiAdapter.getNewPxDpi(R.dimen.contact_dialpad_dial_button_width, this.mActivity);
                } else {
                    btnWidth = ContactDpiAdapter.getNewPxDpi(R.dimen.contact_dialpad_dial_button_width, this.mActivity.getApplicationContext());
                }
            } else if (isInLeftOrRightState()) {
                btnWidth = ContactDpiAdapter.getNewPxDpi(R.dimen.contact_dialpad_dial_button_single_width_2sim, this.mActivity);
            } else {
                btnWidth = ContactDpiAdapter.getNewPxDpi(R.dimen.contact_dialpad_dial_button_width, this.mActivity);
            }
            btnHeight = getDimenPixelSize(R.dimen.contact_dialpad_dial_button_height);
        }
        if (this.mDialButtonSwitcher1 != null && isExtremeSimplicity) {
            LayoutParams params = this.mDialButtonSwitcher1.getLayoutParams();
            params.width = btnWidth;
            params.height = btnHeight;
            this.mDialButtonSwitcher1.setLayoutParams(params);
        }
        if (this.mDialButtonSwitcher2 != null && isExtremeSimplicity) {
            params = this.mDialButtonSwitcher2.getLayoutParams();
            params.width = btnWidth;
            params.height = btnHeight;
            this.mDialButtonSwitcher2.setLayoutParams(params);
        }
        if (this.mDialButtonSwitcherLayout != null && isExtremeSimplicity) {
            params = this.mDialButtonSwitcherLayout.getLayoutParams();
            params = this.mDialButtonSwitcherLayout.getLayoutParams();
            params.width = btnWidth;
            params.height = btnHeight;
            this.mDialButtonSwitcherLayout.setLayoutParams(params);
        }
        if (!(this.mCardNameDial2 == null || isExtremeSimplicity)) {
            params = this.mCardNameDial2.getLayoutParams();
            params.width = btnWidth;
            params.height = btnHeight;
            this.mCardNameDial2.setLayoutParams(params);
        }
        if (this.mCardNameDial3 != null && !isExtremeSimplicity) {
            params = this.mCardNameDial3.getLayoutParams();
            params.width = btnWidth;
            params.height = btnHeight;
            this.mCardNameDial3.setLayoutParams(params);
        }
    }

    private void setButtonsForOneSim(OnClickListener dialButtonListener, OnClickListener ipDialButtonListener) {
        if (this.mViewSwitcher != null) {
            this.mViewSwitcher.setVisibility(8);
        }
        if (this.mCustDialpadFragment != null) {
            this.mCustDialpadFragment.hideSwitchButton();
        }
        this.mCardNameDial2.setVisibility(8);
        this.mCardNameDial3.setVisibility(8);
        if (HwScenceTransition.isAnimationEnd) {
            this.mDialButton.setVisibility(0);
        }
        this.mDialButton.setOnClickListener(dialButtonListener);
    }

    private void setButtonsIfNoSim() {
        if (this.mViewSwitcher != null) {
            this.mViewSwitcher.setVisibility(8);
        }
        if (this.mCustDialpadFragment != null) {
            this.mCustDialpadFragment.hideSwitchButton();
            this.mCustDialpadFragment.hideEncryptCallButton();
        }
        this.mCardNameDial2.setVisibility(8);
        this.mCardNameDial3.setVisibility(8);
        if (HwScenceTransition.isAnimationEnd) {
            this.mDialButton.setVisibility(0);
        }
    }

    private void getSettingsSIMName() {
        if (SimFactoryManager.isBothSimEnabled() || EncryptCallUtils.getCust().isEncryptCallEnable()) {
            this.sim1DialRes = R.drawable.call_sim1_selector_dialer;
            this.sim2DialRes = R.drawable.call_sim2_selector_dialer;
            CommonUtilMethods.setSimcardName(this.mActivity);
            this.mCard1Name = CommonUtilMethods.getSim1CardName();
            this.mCard2Name = CommonUtilMethods.getSim2CardName();
            return;
        }
        this.sim2DialRes = R.drawable.ic_dialer_call;
        this.sim1DialRes = R.drawable.ic_dialer_call;
        String str = "";
        this.mCard2Name = str;
        this.mCard1Name = str;
    }

    private void idForButtonLayoutWithIp(View fragmentView) {
        if (!this.mIsLandscape) {
            this.mSearchButton = this.mAdditionalButtonsRow.findViewById(R.id.searchButton);
            this.mSearchButton.setOnClickListener(this);
            this.mDeleteButton = this.mAdditionalButtonsRow.findViewById(R.id.deleteButton);
            this.mDeleteButton.setOnClickListener(this);
            this.mDeleteButton.setOnLongClickListener(this);
        }
        this.mDialButton = (ImageButton) this.mAdditionalButtonsRow.findViewById(R.id.dialButton);
        ViewUtil.setStateListIcon(this.mActivity, this.mDialButton, false);
        if (this.mResource.getBoolean(R.bool.config_show_onscreen_dial_button)) {
            this.mDialButton.setOnClickListener(this);
        } else {
            this.mDialButton.setVisibility(8);
        }
        if (this.mIsPhoneMultiSim && !this.mDialButton.getContentDescription().equals(getString(R.string.description_dialer_keypad_button))) {
            this.mDialButton.setVisibility(8);
        }
        if (!this.mIsPhoneMultiSim) {
            if (this.mViewSwitcher != null) {
                this.mViewSwitcher.setVisibility(8);
            }
            if (this.mCustDialpadFragment != null) {
                this.mCustDialpadFragment.hideSwitchButton();
            }
            this.mCardNameDial2.setVisibility(8);
            this.mCardNameDial3.setVisibility(8);
        }
    }

    private boolean isLayoutReady() {
        return this.mDigits != null;
    }

    private boolean fillDigitsIfNecessary(Intent intent) {
        String action = intent.getAction();
        if ("android.intent.action.DIAL".equals(action) || "android.intent.action.VIEW".equals(action)) {
            if ("android.intent.action.DIAL".equals(action)) {
                this.mIsFillDataFromIntentIsActive = true;
            }
            boolean isFilledFromIntent = intent.getBooleanExtra("filled_from_intent", false);
            Uri uri = intent.getData();
            if (!(uri == null || isFilledFromIntent)) {
                if ("tel".equals(uri.getScheme())) {
                    String data = uri.getSchemeSpecificPart();
                    this.mDigitsFilledByIntent = true;
                    this.mClearDigitsOnStop = false;
                    setFormattedDigits(data, null);
                    intent.putExtra("filled_from_intent", true);
                    return true;
                }
                String type = intent.getType();
                if ("vnd.android.cursor.item/person".equals(type) || "vnd.android.cursor.item/phone".equals(type)) {
                    Cursor c = this.mResolver.query(intent.getData(), new String[]{"number", "number_key"}, null, null, null);
                    if (c != null) {
                        try {
                            if (c.moveToFirst()) {
                                this.mDigitsFilledByIntent = true;
                                setFormattedDigits(c.getString(0), c.getString(1));
                                intent.putExtra("filled_from_intent", true);
                                return true;
                            }
                            c.close();
                        } finally {
                            c.close();
                        }
                    }
                }
            }
        }
        return false;
    }

    private void setFormattedDigits(String data, String normalizedNumber) {
        boolean z = false;
        String dialString = PhoneNumberUtils.extractNetworkPortion(data);
        if (!TextUtils.isEmpty(dialString)) {
            if (this.mIsPhoneMultiSim) {
                boolean isFirstSimEnabled = CommonUtilMethods.getFirstSimEnabled();
                boolean isSecondSimEnabled = CommonUtilMethods.getSecondSimEnabled();
                if (isFirstSimEnabled && isSecondSimEnabled && !PhoneNumberUtils.isEmergencyNumber(dialString)) {
                    updateRecommendedCard(dialString);
                }
            }
            if (this.mCustDialpadFragment != null && this.mCustDialpadFragment.isEncryptBtnUpdateRecommend(dialString)) {
                updateRecommendedCard(dialString);
            }
            Editable digits = this.mDigits.getText();
            if (digits.length() > 0) {
                digits.clear();
            }
            digits.replace(0, digits.length(), dialString);
            this.mDigits.setSelection(this.mDigits.getText().length());
            this.mDigits.setCursorVisible(false);
            Intent intent = getActivity().getIntent();
            if ("android.intent.action.DIAL".equals(intent.getAction()) && intent.getData() != null) {
                z = true;
            }
            this.isCursorPositionFirst = z;
            afterTextChanged(digits);
            this.mDigits.requestFocus();
        }
    }

    private void setupKeypad(View fragmentView) {
        boolean lEnableSpeedDial;
        DialpadKeyButton dialpadKey;
        if (PhoneCapabilityTester.isSpeedDialForPlatformEnabled(this.mActivity)) {
            lEnableSpeedDial = true;
        } else {
            lEnableSpeedDial = PhoneCapabilityTester.isCMCCCustomer(this.mActivity);
        }
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "setupKeypad lEnableSpeedDial --> " + lEnableSpeedDial);
        }
        int[] buttonIds = new int[]{R.id.contacts_dialpad_zero, R.id.contacts_dialpad_one, R.id.contacts_dialpad_two, R.id.contacts_dialpad_three, R.id.contacts_dialpad_four, R.id.contacts_dialpad_five, R.id.contacts_dialpad_six, R.id.contacts_dialpad_seven, R.id.contacts_dialpad_eight, R.id.contacts_dialpad_nine, R.id.contacts_dialpad_star, R.id.contacts_dialpad_pound};
        int[] numberIds = new int[]{R.string.contacts_dialpad_0_number, R.string.contacts_dialpad_1_number, R.string.contacts_dialpad_2_number, R.string.contacts_dialpad_3_number, R.string.contacts_dialpad_4_number, R.string.contacts_dialpad_5_number, R.string.contacts_dialpad_6_number, R.string.contacts_dialpad_7_number, R.string.contacts_dialpad_8_number, R.string.contacts_dialpad_9_number, R.string.contacts_dialpad_star_number, R.string.contacts_dialpad_pound_number};
        int[] letterIds = new int[]{R.string.contacts_dialpad_0_letters, R.string.contacts_dialpad_1_letters, R.string.contacts_dialpad_2_letters, R.string.contacts_dialpad_3_letters, R.string.contacts_dialpad_4_letters, R.string.contacts_dialpad_5_letters, R.string.contacts_dialpad_6_letters, R.string.contacts_dialpad_7_letters, R.string.contacts_dialpad_8_letters, R.string.contacts_dialpad_9_letters, R.string.contacts_dialpad_star_letters, R.string.contacts_dialpad_pound_letters};
        Resources resources = getResources();
        for (int i = 0; i < buttonIds.length; i++) {
            dialpadKey = (DialpadKeyButton) fragmentView.findViewById(buttonIds[i]);
            dialpadKey.setLayoutParams(new TableRow.LayoutParams(-1, -1));
            dialpadKey.setKeyTimes(this);
            configureKeypadListeners(dialpadKey, buttonIds[i]);
            dialpadKey.setOnTouchListener(this);
            if (lEnableSpeedDial) {
                dialpadKey.setOnLongClickListener(this);
            }
            TextView numberView = (TextView) dialpadKey.findViewById(R.id.contacts_dialpad_key_number);
            TextView lettersView = (TextView) dialpadKey.findViewById(R.id.contacts_dialpad_key_letters);
            String numberString = resources.getString(numberIds[i]);
            if (numberView != null) {
                numberView.setText(numberString);
            }
            dialpadKey.setContentDescription(numberString);
            if (lettersView != null) {
                lettersView.setText(resources.getString(letterIds[i]));
            }
        }
        fragmentView.findViewById(R.id.contacts_dialpad_one).setOnLongClickListener(this);
        dialpadKey = (DialpadKeyButton) fragmentView.findViewById(R.id.contacts_dialpad_star);
        dialpadKey.setOnClickListener(this);
        dialpadKey.setOnLongClickListener(this);
        dialpadKey.setKeyTimes(this);
        dialpadKey = (DialpadKeyButton) fragmentView.findViewById(R.id.contacts_dialpad_zero);
        dialpadKey.setOnClickListener(this);
        dialpadKey.setOnLongClickListener(this);
        dialpadKey.setKeyTimes(this);
        dialpadKey = (DialpadKeyButton) fragmentView.findViewById(R.id.contacts_dialpad_pound);
        dialpadKey.setOnClickListener(this);
        dialpadKey.setOnLongClickListener(this);
        dialpadKey.setKeyTimes(this);
        fragmentView.findViewById(R.id.new_contact).setOnClickListener(this);
        fragmentView.findViewById(R.id.add_exist_contact).setOnClickListener(this);
        fragmentView.findViewById(R.id.send_sms).setOnClickListener(this);
    }

    private void setupKeypadHuawei(View fragmentView) {
        boolean lEnableSpeedDial;
        if (PhoneCapabilityTester.isSpeedDialForPlatformEnabled(this.mActivity)) {
            lEnableSpeedDial = true;
        } else {
            lEnableSpeedDial = PhoneCapabilityTester.isCMCCCustomer(this.mActivity);
        }
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "setupKeypad lEnableSpeedDial --> " + lEnableSpeedDial);
        }
        View view = fragmentView.findViewById(R.id.contacts_dialpad_one);
        view.setOnLongClickListener(this);
        view.setOnTouchListener(this);
        view = fragmentView.findViewById(R.id.contacts_dialpad_two);
        view.setOnTouchListener(this);
        if (lEnableSpeedDial) {
            view.setOnLongClickListener(this);
        }
        view = fragmentView.findViewById(R.id.contacts_dialpad_three);
        view.setOnTouchListener(this);
        if (lEnableSpeedDial) {
            view.setOnLongClickListener(this);
        }
        view = fragmentView.findViewById(R.id.contacts_dialpad_four);
        view.setOnTouchListener(this);
        if (lEnableSpeedDial) {
            view.setOnLongClickListener(this);
        }
        view = fragmentView.findViewById(R.id.contacts_dialpad_five);
        view.setOnTouchListener(this);
        if (lEnableSpeedDial) {
            view.setOnLongClickListener(this);
        }
        view = fragmentView.findViewById(R.id.contacts_dialpad_six);
        view.setOnTouchListener(this);
        if (lEnableSpeedDial) {
            view.setOnLongClickListener(this);
        }
        view = fragmentView.findViewById(R.id.contacts_dialpad_seven);
        view.setOnTouchListener(this);
        if (lEnableSpeedDial) {
            view.setOnLongClickListener(this);
        }
        view = fragmentView.findViewById(R.id.contacts_dialpad_eight);
        view.setOnTouchListener(this);
        if (lEnableSpeedDial) {
            view.setOnLongClickListener(this);
        }
        view = fragmentView.findViewById(R.id.contacts_dialpad_nine);
        view.setOnTouchListener(this);
        if (lEnableSpeedDial) {
            view.setOnLongClickListener(this);
        }
        view = fragmentView.findViewById(R.id.contacts_dialpad_star);
        view.setOnClickListener(this);
        view.setOnTouchListener(this);
        view.setOnLongClickListener(this);
        view = fragmentView.findViewById(R.id.contacts_dialpad_zero);
        view.setOnClickListener(this);
        view.setOnTouchListener(this);
        view.setOnLongClickListener(this);
        view = fragmentView.findViewById(R.id.contacts_dialpad_pound);
        view.setOnClickListener(this);
        view.setOnTouchListener(this);
        view.setOnLongClickListener(this);
        fragmentView.findViewById(R.id.new_contact).setOnClickListener(this);
        fragmentView.findViewById(R.id.add_exist_contact).setOnClickListener(this);
        fragmentView.findViewById(R.id.send_sms).setOnClickListener(this);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void showDigistHeader(boolean show, boolean needAnimate) {
        if (this.mDigits != null && isAdded()) {
            if (!this.isDigistHeadShown || !show) {
                if (this.isDigistHeadShown || show) {
                    PeopleActivity peopleActivity = null;
                    if (this.mActivity instanceof PeopleActivity) {
                        peopleActivity = this.mActivity;
                    }
                    if (show) {
                        if (getActivity() instanceof PeopleActivity) {
                            int lCurrentTab = ((PeopleActivity) getActivity()).getCurrentTab();
                            if (HwLog.HWDBG) {
                                HwLog.d("DialpadFragment", "showDigistHeader lCurrentTab:" + lCurrentTab);
                            }
                            if (TabState.DIALER != lCurrentTab) {
                                return;
                            }
                        }
                        showHeader(true, needAnimate);
                        this.isDigistHeadShown = true;
                        if (peopleActivity != null) {
                            ((HapViewPager) peopleActivity.getViewPager()).disableViewPagerSlide(true);
                        }
                        if (this.mIsLandscape) {
                            if (this.mDeleteButton != null) {
                                if (HwLog.HWDBG) {
                                    HwLog.d("DialpadFragment", "showDigistHeader mDeleteButton visibility:VISIBLE");
                                }
                                this.mDeleteButton.setVisibility(0);
                            }
                        } else if (this.mDeleteButton != null && this.mDeleteButton.getVisibility() == 8) {
                            if (HwLog.HWDBG) {
                                HwLog.d("DialpadFragment", "showDigistHeader mDeleteButton visibility: visible");
                            }
                            this.mDeleteButton.setVisibility(0);
                            if (this.overflowMenuButton != null && this.overflowMenuButton.getVisibility() == 0) {
                                this.overflowMenuButton.setVisibility(8);
                            }
                        }
                    } else {
                        showHeader(false, false);
                        this.isDigistHeadShown = false;
                        if (peopleActivity != null) {
                            ((HapViewPager) peopleActivity.getViewPager()).disableViewPagerSlide(false);
                        }
                        if (this.mTableRow0 != null) {
                            changeDialerDigitsHeight();
                        }
                        if (this.mIsLandscape) {
                            if (this.mDeleteButton != null) {
                                this.mDeleteButton.setVisibility(4);
                            }
                        } else if (this.mDeleteButton != null && this.mDeleteButton.getVisibility() == 0) {
                            this.mDeleteButton.setVisibility(8);
                            if (this.overflowMenuButton != null && this.overflowMenuButton.getVisibility() == 8) {
                                this.overflowMenuButton.setVisibility(0);
                            }
                        }
                    }
                }
            }
        }
    }

    private void showHeader(boolean show, boolean needAnimate) {
        boolean z = true;
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "showHeader:" + show + " mIsLandscape:" + this.mIsLandscape);
        }
        CallLogFragment lCallLog = getCallLogFragment();
        if (lCallLog != null) {
            boolean z2;
            if (show) {
                z2 = false;
            } else {
                z2 = true;
            }
            lCallLog.setCanShowLazyMode(z2);
        }
        if (this.mDigitsContainer != null) {
            if (this.mIsLandscape || !this.mAnimateHelper.isCanPlay() || !needAnimate) {
                int i;
                DialpadHeaderLayout dialpadHeaderLayout = this.mDigitsContainer;
                if (show) {
                    i = 0;
                } else {
                    i = 8;
                }
                dialpadHeaderLayout.setVisibility(i);
                EditText editText = this.mDigits;
                if (show) {
                    i = 0;
                } else {
                    i = 8;
                }
                editText.setVisibility(i);
                if (show) {
                    this.mDigits.setSelection(this.mDigits.getText().length());
                }
            } else if (!show) {
                this.mDigitsContainer.setVisibility(8);
                this.mDigits.setVisibility(8);
            }
        }
        if (!this.mIsLandscape) {
            int margintTop;
            if (show) {
                margintTop = this.mMargintTop;
            } else {
                margintTop = 0;
            }
            if (HwLog.HWFLOW) {
                String str = "DialpadFragment";
                StringBuilder append = new StringBuilder().append("mListContainer==null :");
                if (this.mListContainer != null) {
                    z = false;
                }
                HwLog.i(str, append.append(z).toString());
            }
            if (this.mListContainer == null) {
                loadListView();
            }
            if (this.mListContainer != null) {
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragment", "showHeader mListContainer margintTop:" + margintTop);
                }
                if (this.mAnimateHelper.isCanPlay() && needAnimate) {
                    this.mAnimateHelper.play(show);
                } else {
                    setListContainerLayout(margintTop);
                    setCallLogFragmentTop(margintTop);
                }
            }
        } else if (this.mListContainer != null) {
            LayoutParams paramsList = this.mListContainer.getLayoutParams();
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragment", "showHeader mListContainer margintTop:" + this.mListContainer.getTop());
            }
            if (paramsList instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
                params.setMargins(0, 0, 0, 0);
                this.mListContainer.setLayoutParams(params);
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragment", "showHeader mListContainer top:" + this.mListContainer.getTop());
                }
            }
            setCallLogFragmentTop(0);
        }
        if (!this.mIsLandscape || !CommonUtilMethods.calcIfNeedSplitScreen() || this.mSplitActionBarView == null) {
            return;
        }
        if (show) {
            this.mSplitActionBarView.setVisibility(8);
            return;
        }
        this.mSplitActionBarView.setVisibility(0);
        this.mSplitActionBarView.setEnable(2, this.mIsDeleteCalllogMenuEnable);
    }

    private void setListContainerLayout(int marginTop) {
        if (this.mListContainer != null) {
            if (this.mListContainer.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
                params.setMargins(0, marginTop, 0, 0);
                this.mListContainer.setTranslationY(0.0f);
                this.mListContainer.setLayoutParams(params);
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragment", "showHeader mListContainer top:" + this.mListContainer.getTop());
                }
            }
            setCallLogFragmentTop(marginTop);
        }
    }

    private void setCallLogFragmentTop(int marginTop) {
        if (this.mCallLogFragmentView != null) {
            LayoutParams lCalllogParams = this.mCallLogFragmentView.getLayoutParams();
            if (lCalllogParams instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) lCalllogParams;
                lParams.setMargins(0, marginTop, 0, 0);
                this.mCallLogFragmentView.setLayoutParams(lParams);
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragment", "showHeader mCallLogFragmentView top:" + this.mCallLogFragmentView.getTop());
                }
            }
        }
    }

    private void addHeader() {
        int headerHeight;
        if (this.mDigitsContainer != null) {
            View view = getActivity().getWindow().getDecorView();
            headerHeight = getResources().getDimensionPixelSize(R.dimen.contact_dialpad_header_height);
            if (view instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) view;
                if (vg.getChildCount() > 0) {
                    View child = vg.getChildAt(0);
                    if (child instanceof ViewGroup) {
                        ((ViewGroup) child).addView(this.mDigitsContainer, new LayoutParams(-1, headerHeight));
                    }
                    if (HwLog.HWDBG) {
                        HwLog.d("DialpadFragment", "addHeader width:" + this.mDigitsContainer.getWidth() + " height:" + headerHeight);
                    }
                }
            }
            this.mDigitsContainer.setVisibility(8);
            this.mDigits.setVisibility(8);
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                ViewGroup dialpadHead = (ViewGroup) view.findViewById(R.id.dialpad_head);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) dialpadHead.getLayoutParams();
                params.setMargins(0, ContactDpiAdapter.getStatusBarHeight(getActivity()), 0, 0);
                params.gravity = 1;
                dialpadHead.setLayoutParams(params);
            }
            if (this.mIsLandscape && this.mAdditionalButtonsRow != null) {
                this.mSearchButton = this.mAdditionalButtonsRow.findViewById(R.id.searchButton);
                this.mDeleteButton = this.mAdditionalButtonsRow.findViewById(R.id.deleteButton);
                ImageView deleteImageView = (ImageView) this.mDeleteButton.findViewById(R.id.menu_item_image);
                setDialpadDeletedIcon(this.mActivity, deleteImageView);
                this.mDeleteButton.setOnClickListener(this);
                this.mDeleteButton.setOnLongClickListener(this);
            }
        }
        headerHeight = this.mResource.getDimensionPixelSize(R.dimen.contact_dialpad_header_height);
        TypedArray actionbarSizeTypedArray = getActivity().obtainStyledAttributes(new int[]{16843499});
        float actionBarHeight = actionbarSizeTypedArray.getDimension(0, 0.0f);
        actionbarSizeTypedArray.recycle();
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "showHeader headerHeight:" + headerHeight + " actionBarHeight:" + actionBarHeight);
        }
        this.mMargintTop = headerHeight - ((int) actionBarHeight);
        if (this.mAnimateHelper.isCanPlay()) {
            this.mAnimateHelper.setTranslateHeight(this.mMargintTop);
        }
    }

    private void setDialpadDeletedIcon(Context context, ImageView imageButton) {
        if (this.mIsLandscape && imageButton != null && !CommonUtilMethods.isLargeThemeApplied(getResources()) && ImmersionUtils.getImmersionStyle(context) == 1) {
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_dialer_light_light));
        }
    }

    public void displayDigits() {
        if (!isDigitsEmpty() && (getActivity() instanceof PeopleActivity)) {
            PeopleActivity people = (PeopleActivity) getActivity();
            if (people.getIntent() != null && "android.intent.action.SEARCH".equals(people.getIntent().getAction()) && people.getIntent().getBooleanExtra("SEARCH_MODE", false)) {
                clearDigitsText();
                return;
            }
            int lCurrentTab = people.getCurrentTab();
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragment", "onStart() lCurrentTab:" + lCurrentTab);
            }
            showDigistHeader(TabState.DIALER == lCurrentTab, false);
        }
    }

    public void onStart() {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "onStart()");
        }
        super.onStart();
    }

    public void setShowDialpadFlag(boolean showDialpad) {
        this.visCreate = showDialpad;
    }

    public void onResume() {
        super.onResume();
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "onResume()");
        }
        this.hasResponseLongClick = false;
        checkVideoCallVisibility();
        this.mVoLteStatusObserver.registerObserver();
        this.mPressedDialpadKeys.clear();
        if (this.mDelayLoadingLoaded) {
            displayDigits();
        }
        showOrHideMaskView(false);
        initExtremeSimplicityMode();
        setIsAnimationInProgress(false);
        this.mShowErrorDiallogUtils = new ShowErrorDiallogUtils(this);
        this.mBePause = false;
        if (!(this.mIsLandscape || this.mSingleHandMode == null)) {
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragment", "onResume isSmartSingleHandModeOn:" + this.mSingleHandMode.isSmartSingleHandModeOn());
            }
            if (this.mSingleHandMode.isSmartSingleHandFeatureEnabled() && this.mSingleHandMode.isSmartSingleHandModeOn() && MotionRecognition.isMotionClassExists() && MotionRecognition.isMotionRecoApkExist(this.mActivity)) {
                startMotionRecognition();
            }
            if (this.mSingleHandMode.isSingleHandFeatureEnabled()) {
                adjustDialerContainer(true);
                updateDialpadHandModeView(this.mSingleHandMode.getCurrentHandMode(), false, false, false);
            }
        }
        getLoaderManager().restartLoader(1, null, this.mSpeedDialLoaderListener);
        queryLastOutgoingCall();
        this.mTonePlayer.refreshToneType();
        getHapticFeedback().checkSystemSetting();
        this.mCallNumberEnable = true;
        Activity parent = getActivity();
        if (parent instanceof PeopleActivity) {
            fillDigitsIfNecessary(parent.getIntent());
        }
        if (this.mshowingaddToContactDialog) {
            showaddToContactDialog();
        }
        if (this.mClearDigitsOnStop) {
            this.mClearDigitsOnStop = false;
            clearDigitsText();
        }
        if (this.visCreate || this.mShowDialpad) {
            showDialpadChooser();
        } else {
            hideDialpad();
        }
        this.visCreate = false;
        if (this.mDialerHighlighter != null && TextUtils.isEmpty(DialerHighlighter.cleanNumber(this.mDigits.getText().toString(), false))) {
            setCallLogVisible(true);
        }
        refreshFilterButton();
        SimFactoryManager.addSimStateListener(this);
        if (!this.mIsPhoneMultiSim) {
            updateDialAndDeleteButtonEnabledState();
        }
        if (this.mCustDialpadFragment != null && this.mCustDialpadFragment.isRCMCertificate()) {
            this.mCustDialpadFragment.startListenPhoneState();
        }
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "set isRow0ShownWhenRecreate == false");
        }
        this.isRow0ShownWhenRecreate = false;
        setResetFirstPressFlag(true);
        if (CommonUtilMethods.getIsRefreshCalllog(getActivity())) {
            hasValueRefreshCallLog();
        }
        Intent intent = getActivity().getIntent();
        if (intent == null || !intent.getBooleanExtra("isDissmissDialpad", false) || this.mIsLandscape) {
            setFromNotificationFlag(false);
        } else {
            setFromNotificationFlag(true);
            setDigitsVisible(false, false);
            setCallLogFragmentFullScreen(getCallLogFragment());
            intent.removeExtra("isDissmissDialpad");
        }
        if (this.mCustDialpadFragment != null) {
            this.mCustDialpadFragment.checkAndShowMarqueeOnResume(getActivity());
        }
        this.mSelectCallLogId = Long.valueOf(-1);
        this.mSelectContactUri = null;
        this.mNewContactNumber = null;
        this.mSelectHashCode = 0;
        if (this.mList != null && CommonUtilMethods.calcIfNeedSplitScreen() && this.mIsLandscape) {
            this.mList.invalidateViews();
        }
        if (CommonUtilMethods.calcIfNeedSplitScreen() && (getActivity() instanceof PeopleActivity)) {
            PeopleActivity people = (PeopleActivity) getActivity();
            ActionBar actionbar = people.getActionBar();
            int lCurrentTab = people.getCurrentTab();
            if (!(getIsDigistHeadShown() || TabState.DIALER != lCurrentTab || actionbar == null)) {
                actionbar.show();
                HwLog.d("DialpadFragment", " show actionbar for split");
            }
        }
        initWriteSpaceView();
        new CallRecordTask(this.mActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Integer[0]);
    }

    private synchronized boolean getIsDigistHeadShown() {
        return this.isDigistHeadShown;
    }

    private void showOrHideMaskView(boolean isShow) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && this.fragmentView != null) {
            FrameLayout maskView = (FrameLayout) this.fragmentView.findViewById(R.id.split_mask_overlay);
            if (maskView != null) {
                if (getActivity() instanceof PeopleActivity) {
                    PeopleActivity peopleActivity = (PeopleActivity) getActivity();
                    if (!this.mIsLandscape || !peopleActivity.getNeedMaskDialpad()) {
                        maskView.setVisibility(8);
                    } else if (maskView.getVisibility() != 0) {
                        maskView.setVisibility(0);
                    } else {
                        return;
                    }
                    return;
                }
                maskView.setVisibility(8);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isNeedHideDigistHeader() {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && this.mIsLandscape && (getActivity() instanceof PeopleActivity) && ((PeopleActivity) getActivity()).getNeedMaskDialpad()) {
            return false;
        }
        return true;
    }

    private void checkVideoCallVisibility() {
        if (this.mIsLandscape) {
            if (this.mEmptyListAdapter != null) {
                if (this.mEmptyListAdapter.mIsVtLteOn != VtLteUtils.isVtLteOn(getActivity())) {
                    this.mEmptyListAdapter.initData(getActivity());
                    this.mEmptyListAdapter.notifyDataSetChanged();
                } else if (this.mEmptyListAdapter.mIsVtLteOn) {
                    this.mEmptyListAdapter.mIsLteEnable = true;
                    this.mEmptyListAdapter.notifyDataSetChanged();
                }
            }
        } else if (this.fragmentView != null) {
            int i;
            boolean isVtLteOn = VtLteUtils.isVtLteOn(getActivity());
            View mVideoCallButton = this.fragmentView.findViewById(R.id.video_call_button);
            if (mVideoCallButton != null) {
                mVideoCallButton.setVisibility((isVtLteOn ? 0 : 8) | getNewContactVisibility());
                mVideoCallButton.setOnClickListener(this);
                updateVideoCallItems(mVideoCallButton, (TextView) this.fragmentView.findViewById(R.id.video_call_text));
            }
            TextView saveContactsView = (TextView) this.fragmentView.findViewById(R.id.saveto_existed_contact);
            if (isVtLteOn) {
                i = R.string.contact_saveto_existed_contact_short;
            } else {
                i = R.string.contact_saveto_existed_contact;
            }
            saveContactsView.setText(i);
        }
    }

    private void setCallLogVisible(boolean flag) {
        this.mIsCalllogVisible = flag;
        if (this.mCallLogFragmentView != null) {
            int i;
            if (HwLog.HWFLOW) {
                HwLog.i("DialpadFragment", "setCallLogVisible flag:" + flag);
            }
            View view = this.mCallLogFragmentView;
            if (flag) {
                i = 0;
            } else {
                i = 8;
            }
            view.setVisibility(i);
        }
    }

    private void setCallLogTabVisible(boolean flag) {
        if (HwLog.HWFLOW) {
            HwLog.i("DialpadFragment", "setCallLogTabVisible flag:" + flag);
        }
        if (flag && this.mHeaderView != null) {
            hideListViewSelectedHeader(true);
        }
        setCallLogVisible(flag);
    }

    private void setUpViewsVisibilityInSingleHandMode(int aSingleHandMode, boolean isFromMotion, boolean playAnimation) {
        int i = 0;
        HwLog.d("DialpadFragment", "setUpViewsVisibilityInSingleHandMode aSingleHandMode:" + aSingleHandMode + " isFromMotion:" + isFromMotion + " playAnimation:" + playAnimation);
        if (!isFragmentStateInvalid()) {
            this.mCurMode = aSingleHandMode;
            onUpdateSinglehandView(aSingleHandMode, true, playAnimation);
            if (!(this.mSingleHandMode == null || this.mSingleHandMode.isSmartSingleHandFeatureEnabled())) {
                if (-1 == aSingleHandMode) {
                    if (this.mRightHandLayout != null) {
                        setSingleHandViewGone(this.mRightHandLayout, 8);
                    }
                    if (this.mLeftHandLayout != null) {
                        setSingleHandViewGone(this.mLeftHandLayout, 8);
                    }
                    return;
                }
                boolean lIsRightHandMode = 2 == aSingleHandMode;
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragment", "setUpViewsVisibilityInSingleHandMode aSingleHandMode:" + aSingleHandMode);
                }
                if (!isFromMotion) {
                    if (this.mRightHandLayout != null) {
                        int i2;
                        View view = this.mRightHandLayout;
                        if (lIsRightHandMode) {
                            i2 = 0;
                        } else {
                            i2 = 8;
                        }
                        setSingleHandViewGone(view, i2);
                    }
                    if (this.mLeftHandLayout != null) {
                        View view2 = this.mLeftHandLayout;
                        if (lIsRightHandMode) {
                            i = 8;
                        }
                        setSingleHandViewGone(view2, i);
                    }
                }
            }
        }
    }

    private void adjustDialerContainer(boolean aDigitsVisible) {
        if (HwLog.HWDBG) {
            HwLog.i("DialpadFragment", "aDigitsVisible:" + aDigitsVisible + " mIsLandscape:" + this.mIsLandscape);
        }
        if (this.mIsLandscape) {
            int lButtonHeight;
            int landDialpadFragmentHeight = (getContext().getResources().getDisplayMetrics().heightPixels - getResources().getDimensionPixelSize(R.dimen.contact_dialpad_action_bar_land_height)) - getResources().getDimensionPixelSize(R.dimen.contact_statusbar_height);
            setContainerParentHeightLand(landDialpadFragmentHeight);
            LayoutParams layoutParams = null;
            if (this.mAddButtonIpCall.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                layoutParams = (LinearLayout.LayoutParams) this.mAddButtonIpCall.getLayoutParams();
            }
            if (CommonUtilMethods.isLargeThemeApplied(getResources()) || CommonUtilMethods.isSpecialLanguageForDialpad()) {
                lButtonHeight = getDimenPixelSize(R.dimen.dialpad_huawei_item_height_large_theme);
            } else {
                lButtonHeight = getDimenPixelSize(R.dimen.dialpad_huawei_item_height);
            }
            if (layoutParams != null) {
                layoutParams.height = (landDialpadFragmentHeight - (lButtonHeight * 4)) - getWriteSpaceHeight();
            }
            this.mAddButtonIpCall.setLayoutParams(layoutParams);
            this.mAddButtonIpCall.requestLayout();
            setTableRowsLayout(lButtonHeight, false);
            setTableRowsPadding(new TableRow[]{this.mTableRow1, this.mTableRow2, this.mTableRow3, this.mTableRow4}, 0, 0, 0, 0);
            return;
        }
        this.mDigitsVisible = aDigitsVisible;
        if (!aDigitsVisible) {
            setContainerParentHeight(this.mResource.getDimensionPixelSize(R.dimen.dialpad_additional_buttons_layout_hidden_height));
        } else if (this.mDialerContainer.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams rLayParam = (LinearLayout.LayoutParams) this.mDialerContainer.getLayoutParams();
            rLayParam.height = getDialerHeight(aDigitsVisible);
            rLayParam.width = -1;
            this.mDialerContainer.setLayoutParams(rLayParam);
        } else if (this.mDialerContainer.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams rLayParam2 = (RelativeLayout.LayoutParams) this.mDialerContainer.getLayoutParams();
            rLayParam2.height = getDialerHeight(aDigitsVisible);
            rLayParam2.width = -1;
            this.mDialerContainer.setLayoutParams(rLayParam2);
        } else {
            setContainerParentHeight(getDialerHeight(aDigitsVisible));
        }
        int visibility;
        if (aDigitsVisible) {
            this.mSearchButton = this.mAdditionalButtonsRow.findViewById(R.id.searchButton);
            this.mSearchButton.setOnClickListener(this);
            visibility = this.mDeleteButton.getVisibility();
            this.mDeleteButton = this.mAdditionalButtonsRow.findViewById(R.id.deleteButton);
            this.mDeleteButton.setVisibility(visibility);
            this.mDeleteButton.setOnClickListener(this);
            this.mDeleteButton.setOnLongClickListener(this);
            this.mAdditionalButtonsRow.setVisibility(0);
            setMenuVisible(false);
            visibility = this.overflowMenuButton.getVisibility();
            this.overflowMenuButton = this.mAdditionalButtonsRow.findViewById(R.id.overflow_menu);
            this.overflowMenuButton.setVisibility(visibility);
            this.overflowMenuButton.setOnClickListener(this);
        } else {
            visibility = this.mDeleteButton.getVisibility();
            this.mDeleteButton.setOnClickListener(this);
            this.mDeleteButton.setOnLongClickListener(this);
            this.mDeleteButton.setVisibility(visibility);
            this.overflowMenuButton.setVisibility(this.overflowMenuButton.getVisibility());
            this.overflowMenuButton.setOnClickListener(this);
            this.mAdditionalButtonsRow.setVisibility(8);
            setMenuVisible(true);
            setSearchBtnsLayout(false);
        }
        setSeachBtnsInSimpleMode(this.overflowMenuButton, R.id.overflow_menu_textview);
    }

    private void setSeachBtnsInSimpleMode(View button, int textId) {
        if (button != null) {
            LayoutParams layoutParams = null;
            if (button.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                layoutParams = (LinearLayout.LayoutParams) button.getLayoutParams();
            }
            TextView tv = (TextView) button.findViewById(textId);
            if (CommonUtilMethods.isSimplifiedModeEnabled()) {
                if (layoutParams != null) {
                    layoutParams.setMarginEnd(0);
                    if (!this.mShowDialpad) {
                        layoutParams.width = 0;
                        layoutParams.weight = 1.0f;
                    }
                    button.setLayoutParams(layoutParams);
                }
                if (tv != null) {
                    tv.setTextSize(2, ((float) this.mResource.getInteger(R.integer.contact_menu_textsize_simplifiedmode)) - 5.0f);
                }
            } else if (layoutParams != null) {
                layoutParams.width = -2;
                if (this.mAdditionalButtonsRow == null) {
                    layoutParams.weight = 0.0f;
                } else if (this.mAdditionalButtonsRow.getVisibility() == 0) {
                    layoutParams.weight = 1.0f;
                }
                button.setLayoutParams(layoutParams);
            }
        }
    }

    private void showOrHideDialpad() {
        if (this.mShowDialpad) {
            showDialpad();
        } else {
            hideDialpad();
        }
    }

    private void setDigitsVisible(boolean flag, boolean animate) {
        setDigitsVisible(flag, animate, false);
    }

    private void setDigitsVisible(boolean flag, boolean animate, boolean asyncResetCallLogListView) {
        this.mShowDialpad = flag;
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "setDigitsVisible mIsDialpadVisible :: " + this.mShowDialpad + " animate :: " + animate);
        }
        if (!animate || this.mGravityParent == null || this.mDialpadAnimationDown == null || this.mDialpadAnimationUp == null) {
            showOrHideDialpad();
            if (asyncResetCallLogListView) {
                this.mHandler.postDelayed(this.mRunnable4CallLog, 200);
            } else {
                resetCallLogListViewHeight();
            }
        } else if (this.mShowDialpad) {
            showDialpad();
            this.mDialpadAnimationUp.setAnimationListener(null);
            this.mGravityParent.startAnimation(this.mDialpadAnimationUp);
        } else {
            setIsAnimationInProgress(true);
            this.mDialpadAnimationDown.setAnimationListener(this.mDialpadAnimationDownListener);
            this.mGravityParent.startAnimation(this.mDialpadAnimationDown);
        }
    }

    private void showDialpad() {
        int i = 8;
        if (!isFragmentStateInvalid()) {
            if (HwLog.HWFLOW) {
                HwLog.i("DialpadFragment", "showDialpad");
            }
            boolean resetSingleHandMode = true;
            if (this.mSingleHandMode != null) {
                this.mCurMode = this.mSingleHandMode.getCurrentHandMode();
            }
            if ((this.mCurMode == 0 || this.mCurMode == -1) && ((this.mRightHandLayout == null || this.mRightHandLayout.getVisibility() == 8) && (this.mLeftHandLayout == null || this.mLeftHandLayout.getVisibility() == 8))) {
                resetSingleHandMode = false;
            }
            if (resetSingleHandMode) {
                showSingleHandLayout();
                if (this.mLeftHandLayout != null) {
                    this.mLeftHandLayout.setBackgroundResource(R.drawable.ic_dialer_menu_background);
                }
                if (this.mRightHandLayout != null) {
                    this.mRightHandLayout.setBackgroundResource(R.drawable.ic_dialer_menu_background);
                }
            }
            if (this.mDialpad != null) {
                this.mDialpad.setVisibility(0);
            }
            if (this.mDialpadHuaweiContainer != null) {
                this.mDialpadHuaweiContainer.setVisibility(0);
            }
            if (this.mDialButton != null) {
                ImageButton imageButton = this.mDialButton;
                if (!this.mIsPhoneMultiSim) {
                    i = 0;
                }
                imageButton.setVisibility(i);
                this.mDialButton.setContentDescription(getString(R.string.description_dial_button));
                this.mDialButton.setBackgroundResource(R.drawable.dial_single_button_back);
            }
            adjustDialerContainer(true);
            if (this.mIsPhoneMultiSim) {
                updateButtonStates(true, true);
            } else if (this.mDialButton != null && this.mDialButton.getVisibility() == 0) {
                setButtonsLayoutForNoSim();
            }
            if (this.mShowDialpad && this.mIsDialpadHiddenWhileModeChange) {
                changeDialerDigitsHeight();
                this.mIsDialpadHiddenWhileModeChange = false;
            }
            adjustNoCallIconPosition(true);
        }
    }

    private void hideDialpad() {
        if (!isFragmentStateInvalid()) {
            boolean resetSingleHandMode = true;
            if (this.mSingleHandMode != null) {
                this.mCurMode = this.mSingleHandMode.getCurrentHandMode();
            }
            if ((this.mCurMode == 0 || this.mCurMode == -1) && ((this.mRightHandLayout == null || this.mRightHandLayout.getVisibility() == 8) && (this.mLeftHandLayout == null || this.mLeftHandLayout.getVisibility() == 8))) {
                resetSingleHandMode = false;
            }
            if (resetSingleHandMode) {
                showSingleHandLayout();
                if (this.mLeftHandLayout != null) {
                    this.mLeftHandLayout.setBackgroundResource(R.drawable.csp_bottom_emui);
                }
                if (this.mRightHandLayout != null) {
                    this.mRightHandLayout.setBackgroundResource(R.drawable.csp_bottom_emui);
                }
            }
            if (this.mDialpad != null) {
                this.mDialpad.setVisibility(8);
            }
            if (this.mDialpadHuaweiContainer != null) {
                this.mDialpadHuaweiContainer.setVisibility(8);
            }
            if (this.mDialButton != null) {
                this.mDialButton.setVisibility(8);
                this.mDialButton.setContentDescription(this.mActivity.getString(R.string.description_dialer_keypad_button));
                this.mDialButton.setEnabled(true);
            }
            if (this.mIsPhoneMultiSim) {
                updateButtonStates(true);
            }
            adjustDialerContainer(false);
            adjustNoCallIconPosition(false);
        }
    }

    public void adjustNoCallIconPosition(boolean isDialpadVisible) {
        CallLogFragment fragment = getCallLogFragment();
        if (fragment != null) {
            fragment.adjustNoCallIconPostion(this);
        }
    }

    public boolean isDialpadVisible() {
        return this.mShowDialpad;
    }

    public void fireCallback(int aSimState) {
    }

    public void onPause() {
        super.onPause();
        this.mVoLteStatusObserver.unregisterObserver();
        if (this.mCustDialpadFragment != null) {
            this.mCustDialpadFragment.removeMarqueeMessageOnPause();
        }
        if (-1 == this.mSelectCallLogId.longValue() && this.mSelectContactUri == null && this.mNewContactNumber == null && this.mSelectHashCode == 0) {
            this.mSelectCallLogId = this.mLastSelectCallLogId;
            this.mSelectContactUri = this.mLastSelectContactUri;
            this.mNewContactNumber = this.mLastNewContactNumber;
            this.mSelectHashCode = this.mLastSelectHashCode;
            this.mLastSelectCallLogId = Long.valueOf(-1);
            this.mLastSelectContactUri = null;
            this.mLastNewContactNumber = null;
            this.mLastSelectHashCode = 0;
        }
        if (!(this.mList == null || isNeedHideDigistHeader())) {
            this.mList.invalidateViews();
        }
        showOrHideMaskView(true);
        if (this.mIsPhoneMultiSim && EmuiVersion.isSupportEmui() && this.mPhoneStateLinstenerOne != null && this.mPhoneStateLinstenerTwo != null) {
            SimFactoryManager.listenPhoneState(this.mPhoneStateLinstenerOne, 0);
            SimFactoryManager.listenPhoneState(this.mPhoneStateLinstenerTwo, 0);
        }
        if (this.mCustDialpadFragment != null && this.mCustDialpadFragment.isRCMCertificate()) {
            this.mCustDialpadFragment.stopListenPhoneState();
        }
        this.mBePause = true;
        this.mPressedDialpadKeys.clear();
        this.mTonePlayer.stop();
        SimFactoryManager.removeSimStateListener(this);
        if (this.mIsPhoneMultiSim && this.mVoiceMailDialog != null && this.mVoiceMailDialog.isShowing()) {
            this.mVoiceMailDialog.dismiss();
        }
        SpecialCharSequenceMgr.cleanup();
        if (this.mGravityParent != null) {
            this.mGravityParent.clearAnimation();
        }
        if (this.popup != null) {
            this.popup.dismiss();
        }
        if (this.mSingleHandMode != null && this.mSingleHandMode.isSmartSingleHandFeatureEnabled() && MotionRecognition.isMotionClassExists() && MotionRecognition.isMotionRecoApkExist(this.mActivity)) {
            destroyMotionRecognition();
        }
    }

    public void onBackPressed() {
        this.mClearDigitsOnStop = true;
    }

    public void onStop() {
        super.onStop();
        if (isNeedHideDigistHeader()) {
            showDigistHeader(false, false);
        }
        if (this.mClearDigitsOnStop) {
            this.mClearDigitsOnStop = false;
            clearDigitsText();
        }
        if (this.mTonePlayer != null) {
            this.mTonePlayer.release(false);
        }
        this.mshowingaddToContactDialog = false;
        if (this.addtocontactDialog != null) {
            this.addtocontactDialog.dismiss();
        }
    }

    public void doUnRegisterReceiver() {
        if (this.mFlagAllRegisted) {
            this.mResolver.unregisterContentObserver(this.mCallLogObserver);
            if (this.mCardNameObserver != null && this.mIsRegistered) {
                this.mResolver.unregisterContentObserver(this.mCardNameObserver);
                this.mIsRegistered = false;
            }
            unregisterAirPlanModeObserver();
            if (this.mCustDialpadFragment != null) {
                this.mCustDialpadFragment.removeContentObserver(getActivity());
            }
            this.mFlagAllRegisted = false;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        ContactsPropertyChangeReceiver.unRegisterBigScreenListener(this.mListener);
        if (this.mCustDialpadFragment != null) {
            this.mCustDialpadFragment.unregisterDialerCallBack();
        }
        if (this.mTonePlayer != null) {
            this.mTonePlayer.release(true);
        }
        if (this.mIsGeoLocationDispEnabled) {
            quitNumberLocationHdlr();
        }
        if (this.mShowEmergencyCallWhenNoSIM) {
            SimStateServiceHandler.deRegisterCallback(this);
        }
        ContentResolver lResolver = this.mResolver;
        if (this.mSingleHandModeSwitchObserver != null) {
            lResolver.unregisterContentObserver(this.mSingleHandModeSwitchObserver);
        }
        if (!(this.mSingleHandMode == null || this.mSingleHandSmartObserver == null || !this.mSingleHandMode.isSmartSingleHandFeatureEnabled())) {
            lResolver.unregisterContentObserver(this.mSingleHandSmartObserver);
        }
        if (this.mSingleHandModeObserver != null) {
            lResolver.unregisterContentObserver(this.mSingleHandModeObserver);
        }
        if (this.addtocontactDialog != null) {
            this.addtocontactDialog.dismiss();
        }
        if (this.mBroadcastReceiver != null) {
            this.mActivity.unregisterReceiver(this.mBroadcastReceiver);
        }
        if (this.mAdapter != null) {
            this.mAdapter.changeCursor(null);
            this.mAdapter.notifyDataSetChanged();
        }
        if (this.mSwitchListener != null) {
            HwLog.i("DialpadFragment", "removeSwitchNetWorkListener");
            ContactsApplication.removeSwitchNetWorkListener(getActivity(), this.mSwitchListener);
        }
        getLoaderManager().destroyLoader(107);
        getLoaderManager().destroyLoader(106);
        SpecialCharSequenceMgr.dismissAlertDialog();
        if (this.mRcsCust != null) {
            HwLog.i("DialpadFragment", "unregister rcs service");
            this.mRcsCust.handleCustomizationsOnDestroy(getActivity());
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        int i = 0;
        super.onSaveInstanceState(outState);
        outState.putBoolean("update_recommended_button_state", this.needUpdateRecommendedButtonState);
        outState.putBoolean("is_sim_one_recommended", this.isSimOneRecommended);
        outState.putBoolean("is_sim_two_recommended", this.isSimTwoRecommended);
        outState.putString("split_new_number", this.mNewContactNumber);
        outState.putLong("split_select_calllog_id", this.mSelectCallLogId.longValue());
        outState.putParcelable("split_select_contact_uri", this.mSelectContactUri);
        outState.putInt("split_select_hash_code", this.mSelectHashCode);
        if (this.mCustDialpadFragment != null) {
            this.mCustDialpadFragment.putEncryptButtonRecommended(outState);
        }
        outState.putBoolean("pref_digits_filled_by_intent", this.mDigitsFilledByIntent);
        outState.putBoolean("addtocontactdialog", this.mshowingaddToContactDialog);
        outState.putBoolean("cleardigits", this.mClearDigitsOnStop);
        outState.putString("last_dialed_number", this.mLastNumberDialed);
        if (!(this.mDigits == null || TextUtils.isEmpty(this.mDigits.getText().toString()))) {
            outState.putString("digits_head_text", this.mDigits.getText().toString());
            if (this.mRcsCust != null) {
                outState.putString("save_rotate_rcs_text", this.mDigits.getText().toString());
                outState.putInt("save_rotate_rcs_state", this.mRcsCust.getSaveRotateRcsViewState());
            }
        }
        if (!(this.mShowDialpadLocation == null || TextUtils.isEmpty(this.mShowDialpadLocation.getText()))) {
            outState.putString("digits_head_location_text", this.mShowDialpadLocation.getText().toString());
        }
        if (this.mNewContactOption == null || isDigitsEmpty()) {
            outState.putBoolean("extra_table0_shown", false);
        } else {
            outState.putBoolean("extra_table0_shown", this.mNewContactOption.getVisibility() == 0);
        }
        if (this.mButtonGroupLyout != null) {
            outState.putInt("Button_GroupLayout_Selected", this.mButtonGroupLyout.getButtonSelectedType());
        }
        if (this.mHeaderView != null) {
            String str = "buttongroup_visibility";
            if (this.mHeaderView.getScrollY() > 0) {
                i = this.mHeaderView.getMeasuredHeight();
            }
            outState.putInt(str, i);
        }
        outState.putInt("key_filter_type", this.mFilterType);
    }

    public void saveInstanceState(Bundle outState) {
        onSaveInstanceState(outState);
        Fragment callLogFragment = getChildFragmentManager().findFragmentById(R.id.calllog_fragment);
        if (callLogFragment != null) {
            callLogFragment.onSaveInstanceState(outState);
        }
    }

    private void setMenuVisible(boolean visible) {
        boolean z = false;
        if (!(!CommonUtilMethods.calcIfNeedSplitScreen() || this.mSplitActionBarView == null || this.mIsLandscape)) {
            if (visible) {
                this.mSplitActionBarView.setVisibility(0);
                this.mSplitActionBarView.setEnable(2, this.mIsDeleteCalllogMenuEnable);
            } else {
                this.mSplitActionBarView.setVisibility(8);
            }
        }
        if (CommonUtilMethods.calcIfNeedSplitScreen() && this.mSplitActionBarView != null) {
            boolean z2;
            SplitActionBarView splitActionBarView = this.mSplitActionBarView;
            if (visible || this.mIsLandscape) {
                z2 = this.mGreetingMenuVisible;
            } else {
                z2 = false;
            }
            splitActionBarView.setVisibility(3, z2);
        }
        if (this.mGreetingMenu != null) {
            MenuItem menuItem = this.mGreetingMenu;
            if (visible || this.mIsLandscape) {
                z = this.mGreetingMenuVisible;
            }
            menuItem.setVisible(z);
            HwLog.d("DialpadFragment", "setMenuVisible,mGreetingMenuVisible : " + this.mGreetingMenuVisible);
        }
        if (this.mSearchMenu != null && this.mDeleteCalllogMenu != null && this.mOverflowMenu != null) {
            this.mSearchMenu.setVisible(visible);
            this.mDeleteCalllogMenu.setVisible(visible);
            this.mOverflowMenu.setVisible(visible);
            this.mDeleteCalllogMenu.setEnabled(this.mIsDeleteCalllogMenuEnable);
        }
    }

    private void setupMenuItems(Menu menu) {
        boolean z;
        MenuItem callSettingsMenuItem = menu.findItem(R.id.menu_settings_dialer);
        if (callSettingsMenuItem != null) {
            callSettingsMenuItem.setEnabled(MultiUsersUtils.isCurrentUserOwner());
        }
        MenuItem addToContacts = menu.findItem(R.id.add_to_contacts);
        MenuItem privacyProtectionMenuItem = menu.findItem(R.id.menu_privacy_protection);
        MenuItem itemDeleteAll = menu.findItem(R.id.delete_all);
        MenuItem itemDeleteCalllog = menu.findItem(R.id.overflow_menu_delete);
        boolean lIsDigitsEmpty = isDigitsEmpty();
        CallLogFragment lCallLogFragment = getCallLogFragment();
        if (lCallLogFragment != null && lCallLogFragment.isAdded()) {
            lCallLogFragment.mAdapter.setListViewFocusGone();
        }
        MenuItem overflowMenu = menu.findItem(R.id.overflow_menu_dialer);
        if (overflowMenu != null) {
            overflowMenu.setVisible(true);
        }
        boolean z2 = false;
        if (!(itemDeleteCalllog == null || lCallLogFragment == null)) {
            z2 = EmuiFeatureManager.isMultiDeleteCallLogFeatureEnabled() ? lIsDigitsEmpty : false;
            itemDeleteCalllog.setVisible(z2);
            if (lCallLogFragment.mAdapter == null || lCallLogFragment.mAdapter.isEmpty()) {
                z = false;
            } else {
                z = lIsDigitsEmpty;
            }
            itemDeleteCalllog.setEnabled(z);
        }
        if (!(itemDeleteAll == null || lCallLogFragment == null)) {
            if (z2 || !lIsDigitsEmpty) {
                itemDeleteAll.setVisible(false);
            } else {
                itemDeleteAll.setVisible(false);
                z = lCallLogFragment.mAdapter != null ? !lCallLogFragment.mAdapter.isEmpty() : false;
                itemDeleteAll.setEnabled(z);
            }
        }
        Activity activity = getActivity();
        MenuItem pasteFromClipboard = menu.findItem(R.id.menu_paste);
        if (pasteFromClipboard != null) {
            pasteFromClipboard.setTitle(17039371);
            String text = getTextFromClipboard();
            if (this.mCustDialpadFragment != null && this.mCustDialpadFragment.isFilterText().booleanValue()) {
                text = this.mCustDialpadFragment.getShowText(text);
            }
            pasteFromClipboard.setVisible(true);
            if (text == null || !isNumeric(text)) {
                pasteFromClipboard.setEnabled(false);
            } else {
                pasteFromClipboard.setEnabled(true);
            }
        }
        MenuItem callRecord = menu.findItem(R.id.menu_call_record);
        if (callRecord != null) {
            callRecord.setVisible(mShowCallRecord);
        }
        MenuItem harassmentMenu = menu.findItem(R.id.filter_harassment_dialpad);
        MenuItem tianYiDialer = menu.findItem(R.id.tianyi_dialer_menu_option);
        if (tianYiDialer != null) {
            if (this.mResource.getBoolean(R.bool.config_tianyi_dialer) && (MSimTelephonyManager.getDefault().isNetworkRoaming(0) || MSimTelephonyManager.getDefault().isNetworkRoaming(1))) {
                tianYiDialer.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem arg0) {
                        if (EmuiVersion.isSupportEmui()) {
                            DialpadFragment.this.mSingleChoiceID = Systemex.getInt(DialpadFragment.this.mActivity.getContentResolver(), "tianyi_dialer_menu", 0);
                        }
                        String[] mItems = new String[]{DialpadFragment.this.mResource.getString(R.string.str_tianyi_dialer_menu_open), DialpadFragment.this.mResource.getString(R.string.str_tianyi_dialer_menu_close)};
                        Builder builder = new Builder(DialpadFragment.this.mActivity);
                        builder.setTitle(R.string.str_tianyi_dialer_menu_option);
                        builder.setSingleChoiceItems(mItems, DialpadFragment.this.mSingleChoiceID, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DialpadFragment.this.mSingleChoiceID = whichButton;
                                if (EmuiVersion.isSupportEmui()) {
                                    Systemex.putInt(DialpadFragment.this.mActivity.getContentResolver(), "tianyi_dialer_menu", DialpadFragment.this.mSingleChoiceID);
                                }
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                        return false;
                    }
                });
            } else {
                tianYiDialer.setVisible(false);
            }
        }
        if (activity != null && (this.mDigits.getText().length() == 0 || CommonUtilMethods.isSimplifiedModeEnabled())) {
            addToContacts.setVisible(false);
        }
        boolean hasPrivacyProtection = CommonUtilMethods.hasPrivacyProtectionActivity(getActivity());
        if (privacyProtectionMenuItem != null) {
            if (hasPrivacyProtection && CommonUtilMethods.isPrivacyModeEnabled(this.mActivity) && EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                privacyProtectionMenuItem.setVisible(true);
            } else {
                privacyProtectionMenuItem.setVisible(false);
            }
        }
        if (this.mService == null) {
            this.mService = Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService"));
        }
        if (harassmentMenu == null) {
            return;
        }
        if (this.mService == null || CommonUtilMethods.isSimplifiedModeEnabled()) {
            harassmentMenu.setVisible(false);
        } else if (PhoneCapabilityTester.isHarassmentEnabled(this.mActivity)) {
            harassmentMenu.setVisible(true);
            harassmentMenu.setEnabled(MultiUsersUtils.isCurrentUserOwner());
        } else {
            harassmentMenu.setVisible(false);
        }
    }

    private void checkAndSendKeyPress(int aKeyCode) {
        if (isDigitsEmpty()) {
            sendKeyPress(false, aKeyCode);
        } else {
            String strDigits = this.mDigits.getText().toString();
            int selectionStart = this.mDigits.getSelectionStart();
            int selectionEnd = this.mDigits.getSelectionEnd();
            if (selectionStart != -1) {
                if (selectionStart > selectionEnd) {
                    int tmp = selectionStart;
                    selectionStart = selectionEnd;
                    selectionEnd = tmp;
                }
                if (selectionStart == 0) {
                    sendKeyPress(false, aKeyCode);
                } else if (aKeyCode == 55) {
                    sendKeyPress(true, aKeyCode);
                } else if (aKeyCode == 74) {
                    sendKeyPress(showWait(selectionStart, selectionEnd, strDigits), aKeyCode);
                }
            } else if (aKeyCode == 55) {
                sendKeyPress(true, aKeyCode);
            } else if (aKeyCode == 74) {
                int strLength = strDigits.length();
                sendKeyPress(showWait(strLength, strLength, strDigits), aKeyCode);
            }
        }
    }

    private void sendKeyPress(boolean aAllowed, int aKeyCode) {
        if (aAllowed) {
            keyPressed(aKeyCode);
            return;
        }
        switch (aKeyCode) {
            case 55:
                keyPressed(17);
                return;
            case Place.TYPE_PLACE_OF_WORSHIP /*74*/:
                keyPressed(18);
                return;
            default:
                return;
        }
    }

    private static Intent getAddToContactIntent(CharSequence aDigits, boolean aNewContact) {
        Intent intent = getAddToContactIntentSingleIntent(aDigits, aNewContact);
        intent.putExtra("intent_key_is_from_dialpad", true);
        return intent;
    }

    private static Intent getAddToContactIntentSingleIntent(CharSequence aDigits, boolean aNewContact) {
        Intent intent = new Intent("android.intent.action.INSERT_OR_EDIT");
        intent.putExtra("phone", aDigits.toString());
        intent.putExtra("handle_create_new_contact", aNewContact);
        intent.setType("vnd.android.cursor.item/contact");
        intent.setPackage("com.android.contacts");
        return intent;
    }

    private boolean keyPressed(int keyCode) {
        if (keyCode == 67 && ISLON) {
            playVibrate();
        }
        if (this.hasResponseLongClick && !CommonUtilMethods.isTalkBackEnabled(this.mActivity)) {
            return false;
        }
        if (this.mDigits == null) {
            if (HwLog.HWFLOW) {
                HwLog.i("DialpadFragment", "mDigits == null");
            }
            return false;
        }
        this.mDigits.setCursorVisible(true);
        this.mDigits.onKeyDown(keyCode, new KeyEvent(0, keyCode));
        if (this.mDigits.isCursorVisible()) {
            int length = this.mDigits.length();
            if (length == this.mDigits.getSelectionStart() && length == this.mDigits.getSelectionEnd()) {
                this.mDigits.setCursorVisible(false);
            }
        }
        return true;
    }

    public boolean onKey(View view, int keyCode, KeyEvent event) {
        switch (view.getId()) {
            case R.id.digits:
                if (keyCode == 66) {
                    dialButtonPressed(0);
                    return true;
                }
                break;
        }
        return false;
    }

    public void onClick(View view) {
        PLog.d(1001, "DialpadFragment.onclick for jlog");
        if (this.canClickDialpad) {
            switch (view.getId()) {
                case R.id.new_contact:
                    if (!this.mHasClickedNewContact) {
                        this.mHasClickedNewContact = true;
                        this.mHandler.sendEmptyMessageDelayed(263, 500);
                        addNewContact();
                        StatisticalHelper.sendReport((int) AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, CallInterceptDetails.UNBRANDED_STATE);
                        return;
                    }
                    break;
                case R.id.add_exist_contact:
                    addExistContact();
                    StatisticalHelper.report(4053);
                    return;
                case R.id.video_call_button:
                    dialVideoCall();
                    return;
                case R.id.send_sms:
                    sendSmsMessage(this.mDigits.getText().toString());
                    StatisticalHelper.report(1112);
                    return;
                case R.id.contacts_dialpad_star:
                    keyPressed(17);
                    return;
                case R.id.contacts_dialpad_zero:
                    keyPressed(7);
                    return;
                case R.id.contacts_dialpad_pound:
                    keyPressed(18);
                    return;
                case R.id.searchButton:
                    startShowOrHideDialpad();
                    return;
                case R.id.dialButton:
                    if (!this.mIsAnimationInProgress) {
                        if (this.mDialButton.getContentDescription().equals(getString(R.string.description_dialer_keypad_button))) {
                            setDigitsVisible(true, true);
                            updateDialAndDeleteButtonEnabledState();
                        } else {
                            dialButtonPressed(0);
                        }
                        this.mActivity.invalidateOptionsMenu();
                        return;
                    }
                    return;
                case R.id.overflow_menu:
                    if (this.enableDelete) {
                        showPopupMenu();
                        if (this.mShowDialpad) {
                            StatisticalHelper.report(CommonStatusCodes.AUTH_URL_RESOLUTION);
                        } else {
                            StatisticalHelper.report(5028);
                        }
                        return;
                    }
                    return;
                case R.id.deleteButton:
                    if (!this.mIsAnimationInProgress) {
                        if (this.needUpdateRecommendedButtonState) {
                            this.mCardNameDial2.setBackgroundResource(R.drawable.btn_call);
                            this.mCardNameDial3.setBackgroundResource(R.drawable.btn_call);
                            if (this.mCustDialpadFragment != null) {
                                this.mCustDialpadFragment.setEncryptBtnBgNormal();
                            }
                            this.isSimOneRecommended = false;
                            this.isSimTwoRecommended = false;
                            this.needUpdateRecommendedButtonState = false;
                        }
                        keyPressed(67);
                        this.mActivity.invalidateOptionsMenu();
                        if (isDigitsEmpty()) {
                            setResetFirstPressFlag(true);
                            this.enableDelete = false;
                            this.mHandler.sendEmptyMessageDelayed(259, 800);
                            break;
                        }
                        return;
                    }
                    return;
                case R.id.digits:
                    if (!isDigitsEmpty()) {
                        this.isCursorPositionFirst = false;
                        this.mDigits.setCursorVisible(true);
                        if (!(this.mDialpad == null || this.mShowDialpad)) {
                            setDigitsVisible(true, true);
                        }
                        StatisticalHelper.report(3015);
                    }
                    return;
                case R.id.delete_calllog:
                    startToDeleteMultiCallLog();
                    return;
            }
        }
    }

    private void startCallSeting() {
        ExceptionCapture.reportScene(33);
        try {
            startActivity(DialtactsActivity.getCallSettingsIntent());
            new HwAnimationReflection(this.mActivity).overrideTransition(1);
        } catch (ActivityNotFoundException anfe) {
            HwLog.e("DialpadFragment", "anfe when start activity call settings");
            anfe.printStackTrace();
        }
    }

    private void startShowOrHideDialpad() {
        if (!this.mIsAnimationInProgress) {
            boolean visible = true;
            if (this.mDialpad != null) {
                visible = this.mDialpad.getVisibility() == 8;
            }
            setDigitsVisible(visible, true);
            if (visible) {
                StatisticalHelper.report(3006);
            } else {
                StatisticalHelper.report(5029);
            }
        }
    }

    private void startToDeleteMultiCallLog() {
        int i;
        ExceptionCapture.reportScene(35);
        if (this.mShowDialpad) {
            i = 5033;
        } else {
            i = 5020;
        }
        StatisticalHelper.report(i);
        CallLogFragment clf = getCallLogFragment();
        if (clf != null) {
            clf.handleDeleteMultiCallLog();
        }
    }

    public void showPopupMenu() {
        if (!this.mIsLandscape && this.overflowMenuButton != null && this.popup == null) {
            this.popup = constructPopupMenu(this.overflowMenuButton);
            if (this.popup != null) {
                this.popup.show();
            }
        }
    }

    private PopupMenu constructPopupMenu(View anchorView) {
        Context context = getActivity();
        if (context == null) {
            return null;
        }
        final PopupMenu popupMenu = new PopupMenu(context, anchorView);
        Menu menu = popupMenu.getMenu();
        popupMenu.inflate(R.menu.dialpad_options);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                DialpadFragment.this.popup = null;
                switch (item.getItemId()) {
                    case R.id.menu_settings_dialer:
                        popupMenu.dismiss();
                        DialpadFragment.this.startCallSeting();
                        return true;
                    default:
                        return DialpadFragment.this.onOptionsItemSelected(item);
                }
            }
        });
        popupMenu.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(PopupMenu menu) {
                DialpadFragment.this.popup = null;
            }
        });
        setupMenuItems(menu);
        return popupMenu;
    }

    public boolean onLongClick(View view) {
        if (!this.canClickDialpad) {
            return false;
        }
        switch (view.getId()) {
            case R.id.contacts_dialpad_one:
                if (!isDigitsEmpty()) {
                    return false;
                }
                this.mHandler.sendEmptyMessage(261);
                return true;
            case R.id.contacts_dialpad_two:
                if (!isDigitsEmpty()) {
                    return false;
                }
                checkForSpeedDialAndInitiateCall(2);
                return true;
            case R.id.contacts_dialpad_three:
                if (!isDigitsEmpty()) {
                    return false;
                }
                checkForSpeedDialAndInitiateCall(3);
                return true;
            case R.id.contacts_dialpad_four:
                if (!isDigitsEmpty()) {
                    return false;
                }
                checkForSpeedDialAndInitiateCall(4);
                return true;
            case R.id.contacts_dialpad_five:
                if (!isDigitsEmpty()) {
                    return false;
                }
                checkForSpeedDialAndInitiateCall(5);
                return true;
            case R.id.contacts_dialpad_six:
                if (!isDigitsEmpty()) {
                    return false;
                }
                checkForSpeedDialAndInitiateCall(6);
                return true;
            case R.id.contacts_dialpad_seven:
                if (!isDigitsEmpty()) {
                    return false;
                }
                checkForSpeedDialAndInitiateCall(7);
                return true;
            case R.id.contacts_dialpad_eight:
                if (!isDigitsEmpty()) {
                    return false;
                }
                checkForSpeedDialAndInitiateCall(8);
                return true;
            case R.id.contacts_dialpad_nine:
                if (!isDigitsEmpty()) {
                    return false;
                }
                checkForSpeedDialAndInitiateCall(9);
                return true;
            case R.id.contacts_dialpad_star:
                checkAndSendKeyPress(55);
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.contacts_dialpad_zero:
                keyPressed(81);
                return true;
            case R.id.contacts_dialpad_pound:
                checkAndSendKeyPress(74);
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.deleteButton:
                clearDigitsTextOnCursorBefore();
                if (this.mDigits == null || this.mDigits.getText().length() == 0) {
                    setTableTopRowVisibility(8);
                    showDigistHeader(false, false);
                } else {
                    showDigistHeader(true, false);
                    afterTextChanged(this.mDigits.getText());
                }
                if (this.mDeleteButton != null) {
                    this.mDeleteButton.setPressed(false);
                }
                return true;
            case R.id.digits:
                this.mDigits.setCursorVisible(true);
                return false;
            default:
                return false;
        }
    }

    private void processLongclickOne() {
        if (!isAdded()) {
            return;
        }
        if (!MultiUsersUtils.isCurrentUserGuest() || getActivity() == null) {
            if (this.mIsPhoneMultiSim) {
                checkVoiceMailAvailabilityAndCall();
                this.hasResponseLongClick = true;
            } else if (!SimFactoryManager.hasIccCard(-1)) {
                this.mShowErrorDiallogUtils.showSIMNotAvailableForVoicemailDialog();
                this.hasResponseLongClick = true;
            } else if (CommonUtilMethods.isAirplaneModeOn(this.mActivity)) {
                this.mShowErrorDiallogUtils.showAirplaneModeOnForVoicemailDialog();
                this.hasResponseLongClick = true;
            } else if (CommonUtilMethods.isVoicemailAvailable(-1)) {
                callVoicemail(-1);
                this.hasResponseLongClick = true;
            } else if (getActivity() != null) {
                showVoiceMailNotReady(getActivity());
                this.hasResponseLongClick = true;
            }
            return;
        }
        Toast.makeText(getActivity(), R.string.alert_toast_multi_users, 1).show();
        this.hasResponseLongClick = true;
    }

    private void checkVoiceMailAvailabilityAndCall() {
        boolean isSimEnabled;
        boolean isSim1Present = SimFactoryManager.hasIccCard(0);
        boolean isSim2Present = SimFactoryManager.hasIccCard(1);
        if (isSim1Present) {
            isSimEnabled = SimFactoryManager.isSimEnabled(0);
        } else {
            isSimEnabled = false;
        }
        boolean isSimEnabled2;
        if (isSim2Present) {
            isSimEnabled2 = SimFactoryManager.isSimEnabled(1);
        } else {
            isSimEnabled2 = false;
        }
        if (!isSim1Present && !isSim2Present) {
            this.mShowErrorDiallogUtils.showSIMNotAvailableForVoicemailDialog();
        } else if (CommonUtilMethods.isAirplaneModeOn(this.mActivity)) {
            this.mShowErrorDiallogUtils.showAirplaneModeOnForVoicemailDialog();
        } else if (isSimEnabled || r3) {
            boolean isSim1VoiceMailReady = CommonUtilMethods.isVoicemailAvailable(SimFactoryManager.getSubscriptionIdBasedOnSlot(0));
            boolean isSim2VoiceMailReady = CommonUtilMethods.isVoicemailAvailable(SimFactoryManager.getSubscriptionIdBasedOnSlot(1));
            if (isSimEnabled && !r3 && isSim1VoiceMailReady) {
                callVoicemail(SimFactoryManager.getSubscriptionIdBasedOnSlot(0));
            } else if (!isSimEnabled && r3 && isSim2VoiceMailReady) {
                callVoicemail(SimFactoryManager.getSubscriptionIdBasedOnSlot(1));
            } else if (isSimEnabled && r3) {
                this.mExtremeSimpleDefaultSimcard = SimFactoryManager.getDefaultSimcard();
                if (this.mExtremeSimpleDefaultSimcard == 0 && isSim1VoiceMailReady) {
                    callVoicemail(SimFactoryManager.getSubscriptionIdBasedOnSlot(0));
                } else if (1 == this.mExtremeSimpleDefaultSimcard && isSim2VoiceMailReady) {
                    callVoicemail(SimFactoryManager.getSubscriptionIdBasedOnSlot(1));
                } else if (-1 == this.mExtremeSimpleDefaultSimcard) {
                    showVoiceMailDialog();
                } else if (getActivity() != null) {
                    showVoiceMailNotReady(getActivity());
                }
            } else if (getActivity() != null) {
                showVoiceMailNotReady(getActivity());
            }
        } else {
            this.mShowErrorDiallogUtils.showSIMNotAvailableForVoicemailDialog();
        }
    }

    private void showVoiceMailNotReady(Context context) {
        if (MultiUsersUtils.isCurrentUserOwner()) {
            this.mShowErrorDiallogUtils.showVoiceMailNotReadyDialog();
        } else {
            Toast.makeText(context, R.string.dialog_voicemail_not_ready_title, 1).show();
        }
    }

    public void showVoiceMailDialog() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(this.mActivity, R.layout.voicemail_dialoglist_card_item);
        arrayAdapter.add(SimFactoryManager.getSimCardDisplayLabel(0));
        arrayAdapter.add(SimFactoryManager.getSimCardDisplayLabel(1));
        Builder builder = new Builder(getActivity());
        builder.setTitle(this.mResource.getString(R.string.voicemail));
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (HwLog.HWFLOW) {
                    HwLog.i("DialpadFragment", "Inside voice mail dialog on Click" + which);
                }
                dialog.dismiss();
                if (CommonUtilMethods.isVoicemailAvailable(which)) {
                    DialpadFragment.this.callVoicemail(which);
                } else if (DialpadFragment.this.getActivity() != null) {
                    DialpadFragment.this.showVoiceMailNotReady(DialpadFragment.this.getActivity());
                }
            }
        });
        this.mVoiceMailDialog = builder.create();
        this.mVoiceMailDialog.show();
    }

    private void checkForSpeedDialAndInitiateCall(int aKey) {
        this.hasResponseLongClick = true;
        DialPadState lState = (DialPadState) this.mSpeedDialDataMap.get(Integer.valueOf(aKey));
        if (MultiUsersUtils.isCurrentUserGuest() && getActivity() != null) {
            Toast.makeText(getActivity(), R.string.alert_toast_multi_users, 1).show();
        } else if (CommonUtilMethods.isAirplaneModeOn(this.mActivity)) {
            this.mShowErrorDiallogUtils.showNotReadyForSpeedDialDialog();
        } else {
            if (lState == null) {
                String predefinedNumber = (String) this.mSpeedDialPredefinedNumberMap.get(Integer.valueOf(aKey));
                if (predefinedNumber != null) {
                    callSpeedDialNumber(predefinedNumber);
                } else {
                    showNoSpeedDialNumberAssignedPopUp2(aKey);
                }
            } else if (lState.mDataID == -1) {
                callSpeedDialNumber(lState.mContactNumber);
            } else {
                new SpeedDialNumberQueryTask(aKey).executeOnExecutor(AsyncTaskExecutors.THREAD_POOL_EXECUTOR, (Void[]) null);
            }
        }
    }

    private void showNoSpeedDialNumberAssignedPopUp2(int aKey) {
        Message message = Message.obtain();
        message.what = 260;
        message.arg1 = aKey;
        this.mHandler.sendMessage(message);
    }

    private void callSpeedDialNumber(String aNumber) {
        if (aNumber != null) {
            StatisticalHelper.report(3001);
            aNumber = PhoneNumberFormatter.parsePhoneNumber(aNumber);
            if (SimFactoryManager.isDualSim()) {
                boolean isFirstSimEnabled = CommonUtilMethods.getFirstSimEnabled();
                boolean isSecondSimEnabled = CommonUtilMethods.getSecondSimEnabled();
                if (HwLog.HWFLOW) {
                    HwLog.i("DialpadFragment", "callSpeedDialNumber DIAL_TYPE_SPEED isFirstSimEnabled=" + isFirstSimEnabled + "  isSecondSimEnabled=" + isSecondSimEnabled);
                }
                if (isFirstSimEnabled && isSecondSimEnabled) {
                    this.mExtremeSimpleDefaultSimcard = SimFactoryManager.getDefaultSimcard();
                    if (-1 != this.mExtremeSimpleDefaultSimcard) {
                        startActivity(CallUtil.getCallIntent(aNumber, this.mExtremeSimpleDefaultSimcard));
                        StatisticalHelper.reportDialPortal(getActivity(), 4);
                        StatisticalHelper.sendReport(3000, this.mExtremeSimpleDefaultSimcard);
                        if (HwLog.HWFLOW) {
                            HwLog.i("DialpadFragment", "callSpeedDialNumber mExtremeSimpleDefaultSimcard=" + this.mExtremeSimpleDefaultSimcard);
                        }
                    } else if (this.mCustDialpadFragment == null || !this.mCustDialpadFragment.isCustSdlEyNuber(aNumber)) {
                        boolean mSpeeddialFromSlot1 = false;
                        if (this.mActivity != null) {
                            mSpeeddialFromSlot1 = "true".equals(System.getString(this.mActivity.getContentResolver(), "hw_speeddial_from_slot1"));
                        }
                        if (mSpeeddialFromSlot1) {
                            startActivity(CallUtil.getCallIntent(aNumber, 0));
                            StatisticalHelper.reportDialPortal(getActivity(), 4);
                            StatisticalHelper.sendReport(3000, 0);
                        } else {
                            fillDigitsIfNecessary(ContactsUtils.getDialIntent(aNumber));
                            this.hasResponseLongClick = false;
                        }
                    }
                } else if (isFirstSimEnabled && !isSecondSimEnabled) {
                    startActivity(CallUtil.getCallIntent(aNumber, 0));
                    StatisticalHelper.reportDialPortal(getActivity(), 4);
                    StatisticalHelper.sendReport(3000, 0);
                    if (HwLog.HWFLOW) {
                        HwLog.i("DialpadFragment", "callSpeedDialNumber the subid is :SimConstants.FIRST_SIM_SLOT");
                    }
                } else if (!isFirstSimEnabled && isSecondSimEnabled) {
                    startActivity(CallUtil.getCallIntent(aNumber, 1));
                    StatisticalHelper.reportDialPortal(getActivity(), 4);
                    StatisticalHelper.sendReport(3000, 1);
                    if (HwLog.HWFLOW) {
                        HwLog.i("DialpadFragment", "callSpeedDialNumber the subid is SimConstants.SECOND_SIM_SLOT");
                    }
                } else if (!(isFirstSimEnabled || isSecondSimEnabled)) {
                    startActivity(CallUtil.getCallIntent(aNumber, 0));
                    StatisticalHelper.reportDialPortal(getActivity(), 4);
                    StatisticalHelper.sendReport(3000, 0);
                    if (HwLog.HWFLOW) {
                        HwLog.i("DialpadFragment", "callSpeedDialNumber the subid is +SimConstants.FIRST_SIM_SLOT");
                    }
                }
            } else if (this.mCustDialpadFragment == null || !this.mCustDialpadFragment.checkAndInitCall(getContext(), aNumber)) {
                startActivity(CallUtil.getCallIntent(aNumber));
                StatisticalHelper.reportDialPortal(getActivity(), 4);
                if (HwLog.HWFLOW) {
                    HwLog.i("DialpadFragment", "callSpeedDialNumber DIAL_TYPE_SPEED single sim");
                }
            }
        }
    }

    private void showNoSpeedDialNumberAssignedPopUp(final int aKey) {
        if (isAdded()) {
            HapViewPager viewPager = getHapViewPager();
            if (viewPager != null) {
                viewPager.disableViewPagerSlide(true);
            }
            StatisticalHelper.report(3012);
            AlertDialog lDialog = new Builder(this.mActivity).setMessage(R.string.speed_dial_not_assigned_title).setPositiveButton(R.string.speed_dial_assigned_button_text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface aDialogInterface, int which) {
                    aDialogInterface.dismiss();
                    ArrayList<String> lContactsAddedForSpeedDial = new ArrayList();
                    for (DialPadState lState : DialpadFragment.this.mSpeedDialDataMap.values()) {
                        if (lState.mDataID != -1) {
                            lContactsAddedForSpeedDial.add(String.valueOf(lState.mDataID));
                        }
                    }
                    Bundle bundle = new Bundle();
                    bundle.putInt("key_speed_dial", aKey);
                    DialpadFragment.this.startActivityForResult(CommonUtilMethods.getContactSelectionIntentForSpeedDial(DialpadFragment.this.mActivity, lContactsAddedForSpeedDial, bundle), 5);
                }
            }).setNegativeButton(17039360, null).create();
            lDialog.setMessageNotScrolling();
            if (getActivity() instanceof PeopleActivity) {
                ((PeopleActivity) getActivity()).mGlobalDialogReference = lDialog;
            }
            lDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (DialpadFragment.this.getActivity() != null && (DialpadFragment.this.getActivity() instanceof PeopleActivity)) {
                        ((PeopleActivity) DialpadFragment.this.getActivity()).mGlobalDialogReference = null;
                    }
                    HapViewPager viewPager = DialpadFragment.this.getHapViewPager();
                    if (viewPager != null && DialpadFragment.this.isDigitsEmpty()) {
                        viewPager.disableViewPagerSlide(false);
                    }
                }
            });
            lDialog.show();
        }
    }

    private HapViewPager getHapViewPager() {
        PeopleActivity peopleActivity = null;
        if (this.mActivity instanceof PeopleActivity) {
            peopleActivity = this.mActivity;
        }
        if (peopleActivity != null) {
            return (HapViewPager) peopleActivity.getViewPager();
        }
        return null;
    }

    public void callVoicemail(int aSubscription) {
        if (EmuiFeatureManager.isContactDialVMTip()) {
            showDialVoiceMailTipDialog(aSubscription);
            return;
        }
        startActivity(newVoicemailIntent(aSubscription));
        StatisticalHelper.sendReport(3000, aSubscription);
        clearDigitsText();
        showDigistHeader(false, false);
        if (HwLog.HWFLOW) {
            HwLog.i("DialpadFragment", "callVoicemail the subid is :" + aSubscription);
        }
    }

    public void showDialVoiceMailTipDialog(final int sub) {
        Builder builder = new Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.voicemail));
        builder.setMessage(getResources().getString(R.string.voicemail_status_action_call_server));
        builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DialpadFragment.this.startActivity(DialpadFragment.this.newVoicemailIntent(sub));
                StatisticalHelper.sendReport(3000, sub);
                DialpadFragment.this.clearDigitsText();
                DialpadFragment.this.showDigistHeader(false, false);
            }
        });
        builder.setNegativeButton(17039360, null);
        this.dialVMDialog = builder.create();
        this.dialVMDialog.show();
    }

    public void dialButtonPressed(int slotId) {
        dialButtonPressed(slotId, false);
    }

    public void dialButtonPressed(int slotId, boolean isFromEncryptCall) {
        int subId = SimFactoryManager.getSubscriptionIdBasedOnSlot(slotId);
        if (subId == -1) {
            subId = slotId;
        }
        if (ISLON) {
            playVibrate();
        }
        if (!isDigitsEmpty()) {
            String numberTemp = this.mDigits.getText().toString();
            if (this.mCustDialpadFragment != null) {
                numberTemp = this.mCustDialpadFragment.changeToTwEmergencyNum(this.mDigits.getText().toString(), subId);
            }
            String number = numberTemp;
            if (numberTemp != null && !TextUtils.isEmpty(this.mProhibitedPhoneNumberRegexp) && number.matches(this.mProhibitedPhoneNumberRegexp) && SystemProperties.getInt("persist.radio.otaspdial", 0) != 1) {
                if (HwLog.HWFLOW) {
                    HwLog.i("DialpadFragment", "The phone number is prohibited explicitly by a rule.");
                }
                if (getActivity() != null) {
                    this.mShowErrorDiallogUtils.showCannotMakeCallDialog();
                }
                clearDigitsText();
                showDigistHeader(false, false);
            } else if (this.mCallNumberEnable) {
                this.mCallNumberEnable = false;
                int emrgencySlotId = CommonUtilMethods.getEmergencyNumberSimSlot(number, this.mIsPhoneMultiSim);
                if (emrgencySlotId != -1) {
                    subId = emrgencySlotId;
                }
                if (this.mCustDialpadFragment == null || !this.mCustDialpadFragment.isSupportCallIntercept() || this.mIsFillDataFromIntentIsActive || !this.mCustDialpadFragment.getCallInterceptIntent(number)) {
                    Intent intent = CallUtil.getCallIntent(number, subId);
                    StatisticalHelper.sendReport(3000, subId);
                    if (HwLog.HWFLOW) {
                        HwLog.i("DialpadFragment", "dialButtonPressed the subid is: " + subId + " |isFromEncryptCall: " + isFromEncryptCall);
                    }
                    try {
                        MSimSmsManagerEx.setSimIdToIntent(intent, subId);
                    } catch (Exception e) {
                        intent.putExtra("subscription", subId);
                        e.printStackTrace();
                    }
                    cancelAniAndSearchTask();
                    if (isFromEncryptCall) {
                        HapEncryptCallUtils.buildEncryptIntent(intent);
                    }
                    try {
                        if (this.mCustDialpadFragment == null || !this.mCustDialpadFragment.checkAndInitCall(getContext(), number)) {
                            startActivity(intent);
                            StatisticalHelper.reportDialPortal(this.mActivity, 0);
                            if (HwLog.HWFLOW) {
                                HwLog.i("DialpadFragment", "dialButtonPressed DIAL_TYPE_DIALPAD");
                            }
                            this.mClearDigitsOnStop = true;
                        } else {
                            this.mCallNumberEnable = true;
                        }
                    } catch (ActivityNotFoundException e2) {
                        HwLog.e("DialpadFragment", e2.toString(), e2);
                    }
                } else {
                    clearDigitsText();
                    showDigistHeader(false, false);
                }
            } else if (this.mCustDialpadFragment != null && this.mCustDialpadFragment.isSupportCallIntercept() && !this.mIsFillDataFromIntentIsActive && this.mCustDialpadFragment.getCallInterceptIntent(number)) {
                clearDigitsText();
                showDigistHeader(false, false);
            }
        } else if (phoneIsCdma(subId) && SimFactoryManager.phoneIsOffhook(subId)) {
            try {
                startActivity(newFlashIntent());
            } catch (ActivityNotFoundException e22) {
                HwLog.e("DialpadFragment", e22.toString(), e22);
            }
            StatisticalHelper.reportDialPortal(this.mActivity, 0);
            if (HwLog.HWFLOW) {
                HwLog.i("DialpadFragment", "dialButtonPressed DigitsEmpty");
            }
        } else if (!TextUtils.isEmpty(this.mLastNumberDialed)) {
            boolean isFirstSimEnabled = CommonUtilMethods.getFirstSimEnabled();
            boolean isSecondSimEnabled = CommonUtilMethods.getSecondSimEnabled();
            if (this.mIsPhoneMultiSim && isFirstSimEnabled && isSecondSimEnabled && !CommonUtilMethods.isEmergencyNumber(this.mLastNumberDialed, this.mIsPhoneMultiSim)) {
                updateRecommendedCard(this.mLastNumberDialed);
            }
            if (this.mCustDialpadFragment != null && this.mCustDialpadFragment.isEncryptBtnUpdateRecommend(this.mLastNumberDialed)) {
                updateRecommendedCard(this.mLastNumberDialed);
            }
            setDigitsText(this.mLastNumberDialed);
            this.mDigits.setSelection(this.mDigits.getText().length());
        }
    }

    private void cancelAniAndSearchTask() {
        if (this.mAnimateHelper != null) {
            this.mAnimateHelper.cancel();
        }
        if (getLoaderManager().getLoader(107) != null) {
            boolean canceled = getLoaderManager().getLoader(107).cancelLoad();
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragment", "canceled:" + canceled);
            }
        }
        if (getLoaderManager().getLoader(108) != null) {
            boolean smartDialCanceled = getLoaderManager().getLoader(108).cancelLoad();
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragment", "smartDialCanceled:" + smartDialCanceled);
            }
        }
    }

    void ipcallButtonPressed(int aSubId) {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", ">>>ip call button clicked");
        }
        if (!isDigitsEmpty()) {
            try {
                String lNumber = this.mDigits.getText().toString();
                Intent intent = new Intent("android.intent.action.EDIT");
                intent.addCategory("com.android.ipcallsetting.INPUT");
                intent.putExtra("pickByIntent", true);
                intent.putExtra("phoneNumber", lNumber);
                if (this.mIsPhoneMultiSim) {
                    try {
                        MSimSmsManagerEx.setSimIdToIntent(intent, aSubId);
                    } catch (Exception e) {
                        intent.putExtra("subscription", aSubId);
                        e.printStackTrace();
                    }
                }
                startActivityForResult(intent, 1);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private void showDialpadChooser() {
        if (isLayoutReady() && isAdded()) {
            setDigitsVisible(true, false, true);
            this.mAdditionalButtonsRow.setVisibility(0);
            if (this.mDialerContainer != null && this.mDialerContainer.getVisibility() == 8) {
                this.mDialerContainer.setVisibility(0);
            }
            if (this.mGravityParent != null && this.mGravityParent.getVisibility() == 8) {
                this.mGravityParent.setVisibility(0);
            }
            if (this.mIsLandscape && this.mVerticalDivider != null) {
                this.mVerticalDivider.setVisibility(0);
            }
            setCallLogVisible(isDigitsEmpty());
        }
    }

    private void showSingleHandLayout() {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "showSingleHandLayout");
        }
        if (this.mSingleHandMode != null) {
            if (this.mLeftHandLayout == null && !this.mIsLandscape) {
                initSingleHandMode();
            }
            if (this.mSingleHandMode.getCurrentHandMode() == 0 || this.mSingleHandMode.getCurrentHandMode() == -1) {
                if (this.mLeftHandLayout != null) {
                    setSingleHandViewGone(this.mLeftHandLayout, 8);
                }
                if (this.mRightHandLayout != null) {
                    setSingleHandViewGone(this.mRightHandLayout, 8);
                }
            } else {
                if (this.mLeftHandLayout != null && this.mSingleHandMode.getCurrentHandMode() == 1) {
                    setSingleHandViewGone(this.mLeftHandLayout, 0);
                } else if (this.mLeftHandLayout != null) {
                    setSingleHandViewGone(this.mLeftHandLayout, 8);
                }
                if (this.mRightHandLayout != null && this.mSingleHandMode.getCurrentHandMode() == 2) {
                    setSingleHandViewGone(this.mRightHandLayout, 0);
                } else if (this.mRightHandLayout != null) {
                    setSingleHandViewGone(this.mRightHandLayout, 8);
                }
            }
            changeDialerDigitsHeight();
        }
    }

    public void onItemClick(AdapterView parent, View v, int position, long id) {
    }

    public boolean phoneIsInUse() {
        if (getActivity() == null) {
            return false;
        }
        return originPhoneIsInUse() ? ((TelecomManager) getActivity().getSystemService("telecom")).isInCall() : false;
    }

    private boolean originPhoneIsInUse() {
        if (this.mIsPhoneMultiSim) {
            return SimFactoryManager.phoneIsInUse();
        }
        return !HwUtil.isIdle("com.android.contacts");
    }

    private boolean phoneIsCdma(int subscription) {
        return SimFactoryManager.isCdma(subscription);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchButton:
                if (this.mIsAnimationInProgress) {
                    return true;
                }
                boolean visible = true;
                if (this.mDialpad != null) {
                    visible = this.mDialpad.getVisibility() == 8;
                }
                setDigitsVisible(visible, true);
                if (visible) {
                    StatisticalHelper.report(3006);
                } else {
                    StatisticalHelper.report(5029);
                }
                return true;
            case R.id.overflow_menu:
                StatisticalHelper.report(CommonStatusCodes.AUTH_URL_RESOLUTION);
                return true;
            case R.id.delete_calllog:
                startToDeleteMultiCallLog();
                return true;
            case R.id.menu_greeting:
                Intent greetingIntent = new Intent("android.intent.action.VIEW");
                greetingIntent.setData(GreetingContract$Greetings.CONTENT_URI);
                greetingIntent.putExtra("phone_account_id", this.mPhoneAccountId);
                greetingIntent.setPackage(getActivity().getPackageName());
                startActivity(greetingIntent);
                return true;
            case R.id.menu_paste:
                pasteFromClipboard();
                if (!this.mShowDialpad) {
                    this.mShowDialpad = true;
                    showDialpad();
                }
                StatisticalHelper.report(3002);
                return true;
            case R.id.delete_all:
                CallLogFragment lCallLogFragment = getCallLogFragment();
                if (lCallLogFragment != null) {
                    lCallLogFragment.handleClearCallLog();
                }
                return true;
            case R.id.menu_call_record:
                ExceptionCapture.reportScene(36);
                StatisticalHelper.report(5022);
                toCallRecord();
                return true;
            case R.id.add_to_contacts:
                showaddToContactDialog();
                return true;
            case R.id.menu_privacy_protection:
                try {
                    Intent new_intent = CommonUtilMethods.getPrivacyProtectionIntent();
                    new_intent.setPackage("com.huawei.privacymode");
                    this.mActivity.startActivity(new_intent);
                    new HwAnimationReflection(this.mActivity).overrideTransition(1);
                    StatisticalHelper.report(3003);
                } catch (Exception e) {
                    HwLog.e("DialpadFragment", "pivacy mode entrance action not supported!");
                    e.printStackTrace();
                }
                return true;
            case R.id.filter_harassment_dialpad:
                Intent intent = new Intent("huawei.intent.action.HSM_HARASSMENT");
                intent.putExtra("package_name", "com.android.contacts");
                intent.putExtra("showTabsNumber", 1);
                intent.addCategory("android.intent.category.DEFAULT");
                StatisticalHelper.report(CommonStatusCodes.AUTH_TOKEN_ERROR);
                ExceptionCapture.reportScene(34);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException aE) {
                    HwLog.e("DialpadFragment", "[error]" + aE.getMessage());
                }
                return true;
            case R.id.menu_settings_dialer:
                startCallSeting();
                return true;
            case R.id.overflow_menu_delete:
                startToDeleteMultiCallLog();
                return true;
            case R.id.overflow_menu_dialer:
                StatisticalHelper.report(5028);
                return false;
            default:
                return false;
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        return Pattern.compile("[0-9wpWPN,.()/+-;*#]*").matcher(str.replaceAll(HwCustPreloadContacts.EMPTY_STRING, "")).matches();
    }

    private void toCallRecord() {
        Intent localIntent = new Intent();
        localIntent.addFlags(268468224);
        localIntent.setClassName("com.android.soundrecorder", "com.android.soundrecorder.RecordListActivity");
        localIntent.putExtra("isCallfolder", true);
        try {
            startActivity(localIntent);
        } catch (Exception e) {
            HwLog.e("DialpadFragment", "SoundRecorder is not exist");
        }
    }

    private void pasteFromClipboard() {
        String text = getTextFromClipboard();
        if (text != null) {
            text = text.replaceAll(HwCustPreloadContacts.EMPTY_STRING, "");
            if (text.contains("-")) {
                text = text.replaceAll("-", "");
            }
            updateDialString(text);
        }
    }

    private String getTextFromClipboard() {
        if (this.mClipboard == null) {
            this.mClipboard = (ClipboardManager) getActivity().getSystemService("clipboard");
        }
        ClipData clip = this.mClipboard.getPrimaryClip();
        if (clip == null) {
            return null;
        }
        Item item = clip.getItemAt(0);
        if (item == null) {
            return null;
        }
        if (item.getText() != null) {
            return item.getText().toString();
        }
        return null;
    }

    private void updateDialString(String newDigits) {
        int anchor = this.mDigits.getSelectionStart();
        int point = this.mDigits.getSelectionEnd();
        int selectionStart = Math.min(anchor, point);
        int selectionEnd = Math.max(anchor, point);
        Editable digits = this.mDigits.getText();
        if (selectionStart == -1) {
            int len = this.mDigits.length();
            digits.replace(len, len, newDigits);
        } else if (selectionStart == selectionEnd) {
            digits.replace(selectionStart, selectionStart, newDigits);
            this.mDigits.setSelection(this.mDigits.getText().length());
        } else {
            digits.replace(selectionStart, selectionEnd, newDigits);
            this.mDigits.setSelection(selectionStart + 1);
        }
    }

    private void updateDialAndDeleteButtonEnabledState() {
        boolean z = true;
        if (!isAdded() || isRemoving()) {
            HwLog.i("DialpadFragment", "DialpadFragment is not attached to Activity!");
            return;
        }
        boolean digitsNotEmpty = !isDigitsEmpty();
        if (this.mDialButton != null) {
            if (digitsNotEmpty) {
                this.mDialButton.setEnabled(true);
                this.mDialButton.setClickable(true);
            } else if (phoneIsCdma(SimFactoryManager.getSubscriptionIdBasedOnSlot(0)) && SimFactoryManager.phoneIsOffhook(0)) {
                this.mDialButton.setEnabled(true);
                this.mDialButton.setClickable(true);
            } else if (this.mDialButton.getContentDescription().equals(getString(R.string.description_dialer_keypad_button))) {
                this.mDialButton.setEnabled(true);
                this.mDialButton.setClickable(true);
            } else {
                boolean z2;
                ImageButton imageButton = this.mDialButton;
                if (TextUtils.isEmpty(this.mLastNumberDialed)) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                imageButton.setEnabled(z2);
                ImageButton imageButton2 = this.mDialButton;
                if (TextUtils.isEmpty(this.mLastNumberDialed)) {
                    z = false;
                }
                imageButton2.setClickable(z);
            }
        }
        if (digitsNotEmpty && this.mDialButton != null && this.mDialButton.isEnabled() && this.mIsPhoneMultiSim && !PhoneCapabilityTester.isTwoButtonsEmergencyDialerActive(this.mActivity) && CommonUtilMethods.isEmergencyNumber(this.mDigits.getText().toString(), this.mIsPhoneMultiSim)) {
            if (this.mViewSwitcher != null) {
                this.mViewSwitcher.setVisibility(8);
            }
            if (this.mCustDialpadFragment != null) {
                this.mCustDialpadFragment.hideSwitchButton();
                this.mCustDialpadFragment.hideEncryptCallButton();
            }
            this.mCardNameDial2.setVisibility(8);
            this.mCardNameDial3.setVisibility(8);
            setSingleDialBtnLayout(this.mDialButton);
            setSearchBtnsLayout(false);
            if (HapEncryptCallUtils.isEncryptCallEnabled()) {
                EncryptCallDialpadFragmentHelper.setEmergencyDialButton(this.mIsLandscape, this.mTableRow0, this.mLineHorizontalTopTable0, this.mSearchButton);
                changeDialerDigitsHeight();
            }
            if (HwScenceTransition.isAnimationEnd) {
                this.mDialButton.setVisibility(0);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean showWait(int start, int end, String digits) {
        if (start == end) {
            if (start > digits.length() || digits.charAt(start - 1) == ';') {
                return false;
            }
            if (digits.length() > start && digits.charAt(start) == ';') {
                return false;
            }
        } else if (start > digits.length() || end > digits.length() || digits.charAt(start - 1) == ';') {
            return false;
        }
        return true;
    }

    public boolean isDigitsEmpty() {
        boolean z = true;
        if (this.mDigits == null) {
            return true;
        }
        if (this.mDigits.length() != 0) {
            z = false;
        }
        return z;
    }

    public void queryLastOutgoingCall() {
        ContactsThreadPool.getInstance().execute(new QueryLastOutgoingCallRunnable());
    }

    private Intent newVoicemailIntent(int aSubscription) {
        Intent intent = new Intent(QueryUtil.isSystemAppForContacts() ? "android.intent.action.CALL_PRIVILEGED" : "android.intent.action.CALL", Uri.fromParts("voicemail", "", null));
        intent.setFlags(268435456);
        if (this.mIsPhoneMultiSim) {
            try {
                MSimSmsManagerEx.setSimIdToIntent(intent, aSubscription);
            } catch (Exception e) {
                intent.putExtra("subscription", aSubscription);
            }
        }
        PhoneAccountHandle accountHandle = CallUtil.makePstnPhoneAccountHandleWithPrefix(false, aSubscription);
        if (accountHandle != null) {
            intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", accountHandle);
        }
        return intent;
    }

    private Intent newFlashIntent() {
        Intent intent = CallUtil.getCallIntent("");
        intent.putExtra("com.android.phone.extra.SEND_EMPTY_FLASH", true);
        return intent;
    }

    public int getCallsFeaturesValue(Cursor c) {
        int callsFeatureValueIndex = c.getColumnIndex("features");
        if (callsFeatureValueIndex >= 0) {
            return c.getInt(callsFeatureValueIndex);
        }
        return 0;
    }

    private void requestDigistFocus() {
        if (this.mHandler.hasMessages(262)) {
            this.mHandler.removeMessages(262);
        }
        this.mHandler.sendEmptyMessageDelayed(262, 250);
    }

    private void updateDialpadList(Cursor aCursor) {
        long upTime = System.currentTimeMillis();
        if (this.mAdapter != null) {
            this.mAdapter.mPhoneCallDetailsHelper.updateCustSetting();
            int tableRow0State = 8;
            int lLastDialpadHeight = this.mLastHeight;
            if (!(this.mIsLandscape || this.mTableRow0 == null)) {
                tableRow0State = this.mTableRow0.getVisibility();
            }
            if (!isDigitsEmpty()) {
                this.mAdapter.notifyDataSetChanged();
                setFreqContactsTitle();
                if (this.mList != null) {
                    this.mList.setEmptyView(null);
                    this.mList.setSelection(this.mFirstVisiblePosition);
                }
                setCallLogVisible(false);
            }
            if (this.mIsGeoLocationDispEnabled) {
                if (aCursor == null && this.mQueryString != null && this.mQueryString.length() >= 3) {
                    updateNumberLocation();
                } else if (!(aCursor == null || aCursor.getCount() < 0 || this.mQueryString == null)) {
                    if (this.mQueryString.length() >= 3) {
                        updateNumberLocation();
                    } else {
                        Message msg = new Message();
                        msg.what = 257;
                        msg.obj = "";
                        this.mHandler.sendMessage(msg);
                    }
                }
            }
            setEmptyListList();
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragment", "DialpadAnimateHelper isAnimating:" + this.mAnimateHelper.isAnimating());
            }
            if (!(this.mIsLandscape || this.mTableRow0 == null || this.mDialerContainer == null)) {
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragment", "tableRow0State:" + tableRow0State + " current state:" + this.mTableRow0.getVisibility() + " lLastDialpadHeight:" + lLastDialpadHeight + " mLastHeight:" + this.mLastHeight + " realHeight:" + this.mDialerContainer.getHeight());
                }
                if (!((tableRow0State == this.mTableRow0.getVisibility() && lLastDialpadHeight == this.mDialerContainer.getHeight()) || this.mSingleHandMode == null)) {
                    setUpViewsVisibilityInSingleHandMode(this.mSingleHandMode.getCurrentHandMode(), false, false);
                }
            }
            if (PLog.DEBUG) {
                PLog.d(0, "DialpadFragment updateDialpadList, cost = " + (System.currentTimeMillis() - upTime));
            }
            if (HapEncryptCallUtils.isEncryptCallEnabled() && !isDigitsEmpty()) {
                if (aCursor == null || aCursor.getCount() <= 0) {
                    EncryptCallDialpadFragmentHelper.updateEncryptCallViewStatus(this.mTableRow0, this.mEncryptCallView, this.mSearchButton, this.mLineHorizontalTopTable0, this.mIsLandscape);
                } else {
                    EncryptCallDialpadFragmentHelper.updateEncryptCallViewWithDialpadList(this.mTableRow0);
                }
                changeDialerDigitsHeight();
            }
        }
    }

    private void quitNumberLocationHdlr() {
        if (this.mUpdate != null) {
            this.mUpdate.getLooper().quit();
            this.mUpdate = null;
        }
    }

    private void createNumberLocationHdlr() {
        HandlerThread thread = new HandlerThread("DialpadNumberLocation");
        thread.start();
        this.mUpdate = new Handler(thread.getLooper()) {
            public void handleMessage(Message aMSg) {
                if (DialpadFragment.this.getActivity() != null && DialpadFragment.this.isAdded() && !DialpadFragment.this.isRemoving()) {
                    String showLocationString = NumberLocationLoader.getAndUpdateGeoNumLocation(DialpadFragment.this.getActivity(), DialpadFragment.this.mQueryString, Boolean.valueOf(true));
                    if (TextUtils.isEmpty(showLocationString) && DialpadFragment.IS_CHINA_AREA && DialpadFragment.this.mQueryString != null && DialpadFragment.this.mQueryString.matches("^86\\d{2,3}\\d*$")) {
                        showLocationString = GeoUtil.getGeocodedLocationFor(DialpadFragment.this.getActivity().getApplicationContext(), DialpadFragment.this.mQueryString);
                    }
                    Message msg = new Message();
                    msg.what = 257;
                    msg.obj = showLocationString;
                    DialpadFragment.this.mHandler.sendMessage(msg);
                }
            }
        };
    }

    protected void updateNumberLocation() {
        if (this.mUpdate != null && isAdded()) {
            this.mUpdate.removeMessages(1);
            this.mUpdate.sendEmptyMessageDelayed(1, 500);
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == 0) {
            this.mScrolling = false;
        } else {
            this.mScrolling = true;
        }
        if (1 == scrollState) {
            Activity lActivity = getActivity();
            if (lActivity != null && (lActivity instanceof PeopleActivity)) {
                ((PeopleActivity) lActivity).clearCurrentFocus();
            }
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        final AllContactListItemViews views = (AllContactListItemViews) ((AdapterContextMenuInfo) menuInfo).targetView.getTag();
        if (views != null) {
            final String number = views.mNumber;
            final String lBlackListOnlyName = views.mBlackListName;
            menu.setHeaderTitle(views.mName.getText().toString());
            if (EmuiFeatureManager.isNumberMarkFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner() && views.mNeedMark && !PhoneNumberUtils.isVoiceMailNumber(number)) {
                menu.add(R.string.menu_mark_as).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        try {
                            DialpadFragment.this.startActivityForResult(NumberMarkUtil.getIntentForMark(DialpadFragment.this.getActivity().getApplicationContext(), number), 100);
                        } catch (ActivityNotFoundException e) {
                            HwLog.w("DialpadFragment", "Number mark Activity not found.");
                        }
                        return true;
                    }
                });
            }
            PhoneNumberHelper numberHelper = new PhoneNumberHelper(getResources());
            addSaveContactMenuitem(menu, views, numberHelper);
            menu.add(R.string.contact_menu_send_message).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int i;
                    DialpadFragment.this.sendSmsMessage(number);
                    if (views.mLookupUri == null) {
                        i = 1110;
                    } else {
                        i = 1111;
                    }
                    StatisticalHelper.report(i);
                    return true;
                }
            });
            if (!numberHelper.isSipNumber(number)) {
                menu.add(R.string.recentCalls_editBeforeCall).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (!TextUtils.isEmpty(number)) {
                            Intent intent = new Intent("android.intent.action.DIAL", CallUtil.getCallUri(number));
                            intent.setPackage("com.android.contacts");
                            DialpadFragment.this.startActivity(intent);
                        }
                        return true;
                    }
                });
            }
            if (EmuiFeatureManager.isBlackListFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner()) {
                int blacklistMenuString;
                final IHarassmentInterceptionService mService = Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService"));
                if (BlacklistCommonUtils.checkPhoneNumberFromBlockItem(mService, number)) {
                    blacklistMenuString = R.string.contact_menu_remove_from_blacklist;
                } else {
                    blacklistMenuString = R.string.contact_menu_add_to_blacklist;
                }
                menu.add(blacklistMenuString).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        DialpadFragment.this.performAddBlackList(item, views, number, lBlackListOnlyName, mService);
                        return true;
                    }
                });
            }
        }
    }

    private void addSaveContactMenuitem(ContextMenu menu, final AllContactListItemViews views, PhoneNumberHelper numberHelper) {
        boolean voicemailNumber = numberHelper.isVoicemailNumber(views.mNumber);
        if (views.mLookupUri == null && !voicemailNumber) {
            menu.add(R.string.pickerNewContactText).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent = new Intent("android.intent.action.INSERT", Contacts.CONTENT_URI);
                    intent.setType("vnd.android.cursor.dir/contact");
                    intent.putExtra("phone", views.mNumber);
                    intent.putExtra("intent_key_is_from_dialpad", true);
                    if (!TextUtils.isEmpty(views.mOriginMarkInfo)) {
                        intent.putExtra("name", views.mOriginMarkInfo);
                    }
                    DialpadFragment.this.getActivity().startActivity(intent);
                    return true;
                }
            });
            menu.add(R.string.contact_saveto_existed_contact).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    DialpadFragment.this.getActivity().startActivity(DialpadFragment.getAddToContactIntent(views.mNumber, false));
                    return true;
                }
            });
        }
    }

    private void setFreqContactsTitle() {
        if (this.mEmptyListAdapter == null) {
            this.mEmptyListAdapter = new EmptyListAdapter(this.mActivity);
        }
    }

    private void sendSmsMessage(String number) {
        ExceptionCapture.reportScene(44);
        Activity activity = getActivity();
        if (activity == null || activity.checkSelfPermission("android.permission.READ_SMS") == 0) {
            if (!TextUtils.isEmpty(number)) {
                try {
                    this.mActivity.startActivity(new Intent("android.intent.action.SENDTO", Uri.fromParts("smsto", number, null)));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.quickcontact_missing_app_Toast, 0).show();
                }
            }
            return;
        }
        requestPermissions(new String[]{"android.permission.READ_SMS"}, 3);
    }

    private void addNewContact() {
        ExceptionCapture.reportScene(42);
        Intent intent = new Intent("android.intent.action.INSERT", Contacts.CONTENT_URI);
        intent.setClass(getActivity(), ContactEditorActivity.class);
        intent.putExtra("phone", this.mDigits.getText().toString());
        intent.putExtra("intent_key_is_from_dialpad", true);
        intent.putExtra("ViewDelayedLoadingSwitch", true);
        boolean waitResult = false;
        if (CommonUtilMethods.calcIfNeedSplitScreen() && (getActivity() instanceof PeopleActivity)) {
            ((PeopleActivity) getActivity()).setNeedMaskDialpad(true);
            waitResult = true;
        }
        if (waitResult) {
            ((PeopleActivity) getActivity()).saveInstanceValues();
            getActivity().startActivityForResult(intent, 3003);
            return;
        }
        getActivity().startActivity(intent);
    }

    private void addExistContact() {
        ExceptionCapture.reportScene(43);
        Intent addToContactIntent = getAddToContactIntent(this.mDigits.getText(), false);
        addToContactIntent.setClass(getActivity(), ContactSelectionActivity.class);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            addToContactIntent.setClass(getActivity(), ContactSelectionActivity.TranslucentActivity.class);
        }
        boolean waitResult = false;
        if (CommonUtilMethods.calcIfNeedSplitScreen() && (getActivity() instanceof PeopleActivity)) {
            ((PeopleActivity) getActivity()).setNeedMaskDialpad(true);
            waitResult = true;
        }
        if (waitResult) {
            ((PeopleActivity) getActivity()).saveInstanceValues();
            getActivity().startActivityForResult(addToContactIntent, 3003);
            return;
        }
        getActivity().startActivity(addToContactIntent);
    }

    public void simStateChanged(int aSubScription) {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    if (DialpadFragment.this.getActivity() != null && DialpadFragment.this.isAdded()) {
                        DialpadFragment.this.updateSpeedDialPredefinedCache();
                        DialpadFragment.this.refreshFilterButton();
                        if (DialpadFragment.this.mIsPhoneMultiSim) {
                            DialpadFragment.this.updateButtonStates(true, true);
                            return;
                        }
                        HwLog.i("DialpadFragment", "sim state changed, is not phone multi sim -> updateSingleCardButton");
                        DialpadFragment.this.updateSingleCardButton();
                    }
                }
            });
        }
    }

    private void updateVideoCallItems(View view, TextView textView) {
        boolean is4GStatus = VtLteUtils.isLteServiceAbility();
        if (view != null) {
            view.setEnabled(is4GStatus);
        }
        if (textView == null) {
            return;
        }
        if (is4GStatus) {
            textView.setTextColor(getResources().getColor(R.color.dialpad_huawei_text_color));
        } else {
            textView.setTextColor(getResources().getColor(R.color.dialpad_huawei_text_color_enabled));
        }
    }

    private void updateEncryptButton() {
        if (HapEncryptCallUtils.isEncryptCallEnabled()) {
            int simSlot = -1;
            if (HapEncryptCallUtils.isCdmaBySlot(0)) {
                simSlot = 0;
            } else if (HapEncryptCallUtils.isCdmaBySlot(1)) {
                simSlot = 1;
            }
            if (this.mTableRow0 != null) {
                this.mEncryptCallView = this.mTableRow0.findViewById(R.id.encrypt_call);
            }
            if (simSlot != -1 && this.mEncryptCallView != null) {
                EncryptCallDialpadFragmentHelper.initEncryptCallView(this.mEncryptCallView, new DialButtonListener(simSlot, false, true), this.mSearchButton, this.mIsLandscape);
                if (!isDigitsEmpty()) {
                    EncryptCallDialpadFragmentHelper.updateEncryptCallViewStatus(this.mTableRow0, this.mEncryptCallView, this.mSearchButton, this.mLineHorizontalTopTable0, this.mIsLandscape);
                    changeDialerDigitsHeight();
                }
            } else if (!this.mIsLandscape) {
                EncryptCallDialpadFragmentHelper.updateEncryptButtonAccordingToTableRow0Items(this.mTableRow0, this.mEncryptCallView, this.mLineHorizontalTopTable0);
            } else if (this.mSearchButton != null) {
                this.mSearchButton.setVisibility(4);
                this.mSearchButton.setEnabled(false);
            }
        }
    }

    private void refreshFilterButton() {
        if (this.mActivity != null) {
            boolean isShowVVMFilter;
            if (TelecomUtil.isOperatorCM(this.mActivity)) {
                isShowVVMFilter = true;
            } else {
                isShowVVMFilter = CommonConstants.IS_VVM_FILTER_ON;
            }
            if (this.mButtonGroupLyout != null) {
                this.mButtonGroupLyout.refreshFilterButton(isShowVVMFilter);
            }
            Intent intent = this.mActivity.getIntent();
            if ("android.intent.action.VIEW".equals(intent.getAction()) && "vnd.android.cursor.item/voicemail".equals(intent.resolveType(this.mActivity))) {
                if (isShowVVMFilter) {
                    setVoicemailFilterTab();
                } else {
                    this.mButtonGroupLyout.setLeftButtonSelected();
                    setCallLogFilterType(0);
                }
                Uri voicemailUri = intent.getData();
                CallLogFragment fragment = getCallLogFragment();
                if (!(fragment == null || voicemailUri == null)) {
                    fragment.setVoicemailUri(voicemailUri.toString());
                    intent.setData(Calls.CONTENT_URI);
                    this.mActivity.setIntent(intent);
                }
            }
        }
    }

    private void requeryData() {
        if (this.mList == null) {
            loadListView();
        }
        try {
            if (this.mAdapter == null) {
                this.mAdapter = new DialPadListAdapter(null, this.mActivity, 1);
                this.mList.setAdapter(this.mAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        PLog.d(0, "DialpadFragment requeryData");
        if (this.mAdapter != null) {
            Bundle bundle = new Bundle();
            if (QueryUtil.isProviderSupportHwSeniorSearch()) {
                getLoaderManager().restartLoader(107, bundle, this.mSearchTaskLoaderListener);
                return;
            }
            if (HwLog.HWFLOW) {
                HwLog.i("DialpadFragment", "requeryData,provider NOT support hw senior search");
            }
            getLoaderManager().restartLoader(108, bundle, this.mSmartSearchLoaderListener);
        }
    }

    public void setRefreshDataRequired() {
        if (this.mHandler == null) {
            HwLog.w("DialpadFragment", "setRefreshDataRequired mHandler IS NULL");
            return;
        }
        if (this.mHandler.hasMessages(256)) {
            this.mHandler.removeMessages(256);
        }
        this.mHandler.sendEmptyMessage(256);
    }

    protected void updateSpeedDialDataCache(Cursor aCursor, int aLoaderId) {
        this.mSpeedDialDataMap.clear();
        if (aCursor != null && aCursor.moveToFirst()) {
            switch (aLoaderId) {
                case 1:
                    break;
                default:
                    throw new IllegalArgumentException("UnExpected call back from loader with ID: " + aLoaderId);
            }
            do {
                int lKey = aCursor.getInt(aCursor.getColumnIndex("key_number"));
                DialPadState lDialPadState = new DialPadState();
                this.mSpeedDialDataMap.put(Integer.valueOf(lKey), lDialPadState);
                lDialPadState.mDataID = aCursor.getLong(aCursor.getColumnIndex("phone_data_id"));
                if (lDialPadState.mDataID == -1) {
                    lDialPadState.mContactNumber = aCursor.getString(aCursor.getColumnIndex("number"));
                }
                lDialPadState.mDialpadNumber = lKey;
            } while (aCursor.moveToNext());
        }
    }

    public void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
        if (aResultCode == -1) {
            switch (aRequestCode) {
                case 5:
                    SpeedDialerActivity.handleResultFromContactSelection(aData, getActivity(), false);
                    break;
            }
        }
    }

    public void onPrepareOptionsMenu(Menu aMenu) {
        boolean z = false;
        if (isAdded()) {
            this.mSearchMenu = aMenu.findItem(R.id.searchButton);
            this.mDeleteCalllogMenu = aMenu.findItem(R.id.delete_calllog);
            this.mGreetingMenu = aMenu.findItem(R.id.menu_greeting);
            this.mOverflowMenu = aMenu.findItem(R.id.overflow_menu);
            if (this.mAdditionalButtonsRow.getVisibility() != 0) {
                z = true;
            }
            setMenuVisible(z);
            setupMenuItems(aMenu);
        }
    }

    public int getDialerHeight(boolean aDigitsVisible) {
        int dialerContainerHeight = 0;
        if (isFragmentStateInvalid()) {
            return 0;
        }
        boolean aisSinglehandModeOn = isInLeftOrRightState();
        if (aDigitsVisible) {
            dialerContainerHeight = this.mResource.getDimensionPixelSize(R.dimen.dialpad_additional_buttions_layout_height);
            if (!aisSinglehandModeOn || this.mIsLandscape) {
                dialerContainerHeight += (this.mResource.getDimensionPixelSize(R.dimen.dialpad_huawei_item_height) * 4) + getWriteSpaceHeight();
            } else {
                dialerContainerHeight += this.mResource.getDimensionPixelSize(R.dimen.dialpad_huawei_item_height_singlehandmode) * 4;
            }
            if (HwLog.HWDBG) {
                String str = "DialpadFragment";
                StringBuilder append = new StringBuilder().append("mTableRow0 is shown:");
                boolean z = (this.mTableRow0 == null || this.mTableRow0.getVisibility() != 0) ? this.isRow0ShownWhenRecreate : true;
                HwLog.d(str, append.append(z).toString());
            }
            if (this.mTableRow0 == null || this.mTableRow0.getVisibility() != 0) {
                if (this.isRow0ShownWhenRecreate) {
                }
                dialerContainerHeight += 0;
            }
            if (!this.mIsLandscape) {
                dialerContainerHeight += this.mResource.getDimensionPixelSize(R.dimen.dialpad_huawei_function_row_height);
            }
            dialerContainerHeight += 0;
        } else if (!this.mIsLandscape) {
            dialerContainerHeight = this.mResource.getDimensionPixelSize(R.dimen.dialpad_additional_buttons_layout_hidden_height) + 0;
        }
        return dialerContainerHeight + 0;
    }

    public void changeDialerDigitsHeight() {
        if (this.mIsLandscape) {
            setTableRowsPadding(new TableRow[]{this.mTableRow1, this.mTableRow2, this.mTableRow3, this.mTableRow4}, 0, 0, 0, 0);
        } else if (isAdded()) {
            boolean aisSinglehandModeOn = isInLeftOrRightState();
            int lTotalHeight = getDialerHeight(true);
            if (this.mShowDialpad) {
                setContainerParentHeight(lTotalHeight);
                if (!(this.mSingleHandMode == null || this.mSingleHandMode.isSmartSingleHandFeatureEnabled())) {
                    int lButtonHeight;
                    Resources resources = getResources();
                    if (aisSinglehandModeOn) {
                        lButtonHeight = resources.getDimensionPixelSize(R.dimen.dialpad_huawei_item_height_singlehandmode);
                    } else {
                        lButtonHeight = resources.getDimensionPixelSize(R.dimen.dialpad_huawei_item_height);
                    }
                    if (aisSinglehandModeOn) {
                        setTableRowsPadding(new TableRow[]{this.mTableRow1, this.mTableRow2, this.mTableRow3, this.mTableRow4}, 0, 0, 0, 0);
                    } else {
                        setTableRowsPadding(new TableRow[]{this.mTableRow1, this.mTableRow2, this.mTableRow3, this.mTableRow4}, resources.getDimensionPixelSize(R.dimen.dialpad_table_row_padding_start), 0, resources.getDimensionPixelSize(R.dimen.dialpad_table_row_padding_end), 0);
                    }
                    LayoutParams lButtonLP = this.mTableRow0.getLayoutParams();
                    if (lButtonLP != null) {
                        int dimensionPixelSize;
                        if (aisSinglehandModeOn) {
                            dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.dialpad_huawei_item_height_singlehandmode);
                        } else {
                            dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.dialpad_huawei_function_row_height);
                        }
                        lButtonLP.height = dimensionPixelSize;
                        this.mTableRow0.setLayoutParams(lButtonLP);
                    }
                    setTableRowsLayout(lButtonHeight, aisSinglehandModeOn);
                }
            } else {
                this.mIsDialpadHiddenWhileModeChange = true;
            }
        }
    }

    private void setEmptyListList() {
        if (this.mList == null) {
            loadListView();
        }
        if (isDigitsEmpty()) {
            if (this.mIsLandscape) {
                if (this.mNewContactOption != null) {
                    this.mNewContactOption.setVisibility(0);
                    this.mNewContactOption.setAdapter(null);
                }
                this.mShowDialpadLocation.setText("");
            } else {
                if (this.mNewContactOption != null) {
                    this.mNewContactOption.setVisibility(8);
                }
                this.mShowDialpadLocation.setText("");
            }
            this.mList.setVisibility(8);
            setTableTopRowVisibility(8);
        } else if (this.mList.getCount() > 0) {
            if (this.mNewContactOption != null) {
                this.mNewContactOption.setVisibility(8);
            }
            if (!this.mIsLandscape) {
                showEncryptCallWhenSetEmptyList();
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragment", "mTableRow0 set GONE2");
                }
                changeDialerDigitsHeight();
            }
            this.mList.setVisibility(0);
        } else if (this.mIsLandscape) {
            if (this.mNewContactOption != null) {
                this.mNewContactOption.setVisibility(0);
                this.mNewContactOption.setAdapter(this.mEmptyListAdapter);
            }
            this.mList.setVisibility(8);
        } else {
            if (this.mLoadedFlag == 0) {
                ((ImageView) this.fragmentView.findViewById(R.id.dialer_selector_new_contact)).setImageResource(R.drawable.ic_dialer_selector_new_contact);
                ((ImageView) this.fragmentView.findViewById(R.id.dialer_selector_add_exist_contact)).setImageResource(R.drawable.ic_dialer_selector_add_exist_contact);
                ((ImageView) this.fragmentView.findViewById(R.id.dialer_selector_send_sms)).setImageResource(R.drawable.ic_dialer_selector_send_sms);
                setLoadedFlag(1);
            }
            setTableTopRowVisibility(0);
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragment", "mTableRow0 set VISIBLE2");
            }
            changeDialerDigitsHeight();
        }
    }

    private void showaddToContactDialog() {
        if (this.arrayAdapter == null) {
            this.arrayAdapter = new ArrayAdapter(this.mActivity, R.layout.dialog_add_to_contacts);
            this.arrayAdapter.add(this.mActivity.getResources().getString(R.string.pickerNewContactText));
            this.arrayAdapter.add(this.mActivity.getResources().getString(R.string.contact_saveto_existed_contact));
        }
        if (this.addtocontactDialog == null) {
            Builder builder = new Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.non_phone_add_to_contacts));
            builder.setAdapter(this.arrayAdapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (HwLog.HWFLOW) {
                        HwLog.i("DialpadFragment", "Inside add to contacts dialog on Click" + which);
                    }
                    switch (which) {
                        case 0:
                            DialpadFragment.this.addNewContact();
                            StatisticalHelper.sendReport((int) AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, CallInterceptDetails.UNBRANDED_STATE);
                            break;
                        case 1:
                            DialpadFragment.this.addExistContact();
                            break;
                    }
                    dialog.dismiss();
                    DialpadFragment.this.mshowingaddToContactDialog = false;
                }
            });
            this.addtocontactDialog = builder.create();
        }
        this.addtocontactDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
                DialpadFragment.this.mshowingaddToContactDialog = false;
            }
        });
        this.addtocontactDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                DialpadFragment.this.mshowingaddToContactDialog = false;
            }
        });
        this.addtocontactDialog.show();
        this.mshowingaddToContactDialog = true;
    }

    public EditText getDialerEditText() {
        return this.mDigits;
    }

    public boolean handleKeyEvent(int aKeyCode) {
        if (this.popup != null) {
            this.popup.dismiss();
        }
        return super.handleKeyEvent(aKeyCode);
    }

    private DialerHighlighter getDialerHighlighter() {
        if (this.mDialerHighlighter == null) {
            this.mDialerHighlighter = new DialerHighlighter(getActivity());
        }
        return this.mDialerHighlighter;
    }

    private void updateSpeedDialPredefinedCache() {
        if (this.mSpeedDialPredefinedNumberMap != null) {
            this.mSpeedDialPredefinedNumberMap.clear();
        }
        String predefinedSpeedDialNumbers = SharePreferenceUtil.getDefaultSp_de(this.mActivity).getString("speeddial_predefined_numbers", null);
        if (predefinedSpeedDialNumbers != null) {
            String[] pairs = predefinedSpeedDialNumbers.split(";");
            if (pairs != null) {
                for (String pair : pairs) {
                    String[] singlePair = pair.split(",");
                    if (singlePair != null && singlePair.length == 2) {
                        try {
                            boolean booleanValue;
                            int key = Integer.parseInt(singlePair[0]);
                            if (this.mCustDialpadFragment != null) {
                                booleanValue = this.mCustDialpadFragment.isDisableCustomService().booleanValue();
                            } else {
                                booleanValue = false;
                            }
                            if (!booleanValue) {
                                String temp = singlePair[1];
                                if (this.mCustDialpadFragment != null) {
                                    temp = this.mCustDialpadFragment.getPredefinedSpeedDialNumbersByMccmnc(temp);
                                }
                                if (!TextUtils.isEmpty(temp)) {
                                    this.mSpeedDialPredefinedNumberMap.put(Integer.valueOf(key), temp);
                                }
                            }
                        } catch (NumberFormatException e) {
                            HwLog.e("DialpadFragment", "Problem with the speed dial predefined numbers");
                        }
                    }
                }
            }
        }
    }

    public void touchLogCalls(MotionEvent aEvent) {
        this.mDelayedUpdateModeHandler.removeMessages(9000);
        this.mDelayedUpdateModeHandler.removeMessages(PlacesStatusCodes.USAGE_LIMIT_EXCEEDED);
        if (!this.mIsLandscape) {
            this.mIgnoreSmartSearchListReDraw = false;
            if (!this.mIsAnimationInProgress) {
                if ((1 == aEvent.getAction() || 2 == aEvent.getAction()) && this.mDialpad != null && this.mDialpad.getVisibility() == 0 && this.canClickDialpad) {
                    setDigitsVisible(false, false);
                }
            }
        }
    }

    public CallLogFragment getCallLogFragment() {
        FragmentManager lFragmentMgr = getChildFragmentManager();
        Fragment lFragment = null;
        if (lFragmentMgr != null) {
            lFragment = lFragmentMgr.findFragmentById(R.id.calllog_fragment);
        }
        return lFragment != null ? (CallLogFragment) lFragment : null;
    }

    public void hasValueRefreshCallLog() {
        CallLogFragment lCallLogFragment = getCallLogFragment();
        if (lCallLogFragment != null && lCallLogFragment.isAdded() && lCallLogFragment.isRefreshData()) {
            lCallLogFragment.setRefreshDataClearCache();
            SharePreferenceUtil.getDefaultSp_de(getActivity()).edit().putBoolean("reference_is_refresh_calllog", false).commit();
        }
    }

    private void resetCallLogListViewHeight() {
        int listViewBottomMargin = getDialerHeight(false);
        if (this.mCachedListViewMarginBottom != listViewBottomMargin) {
            CallLogFragment lCallLogFrament = getCallLogFragment();
            if (lCallLogFrament != null) {
                ListView lCallsListView = lCallLogFrament.getCallsListView();
                if (lCallsListView != null && lCallsListView.getVisibility() == 0) {
                    LayoutParams lCallsListParams = lCallsListView.getLayoutParams();
                    lCallsListParams.width = -1;
                    lCallsListParams.height = -1;
                    setBottomMargin(lCallsListParams, listViewBottomMargin);
                    this.mCachedListViewMarginBottom = listViewBottomMargin;
                    lCallsListView.setLayoutParams(lCallsListParams);
                }
            }
        }
    }

    private boolean isFragmentStateInvalid() {
        Activity lActivityRef = getActivity();
        if (lActivityRef == null || lActivityRef.isFinishing() || !isAdded()) {
            return true;
        }
        return isRemoving();
    }

    public void onAttach(Activity activity) {
        ExceptionCapture.reportScene(2);
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "DialpadFragment Attach to Activity");
        }
        super.onAttach(activity);
        BackgroundGenricHandler.getInstance().post(new Runnable() {
            public void run() {
                boolean isSimEnabled;
                boolean z = false;
                DialpadFragment.this.mIsSim1Present = SimFactoryManager.isSIM1CardPresent();
                DialpadFragment.this.mIsSim2Present = SimFactoryManager.isSIM2CardPresent();
                DialpadFragment dialpadFragment = DialpadFragment.this;
                if (DialpadFragment.this.mIsSim1Present) {
                    isSimEnabled = SimFactoryManager.isSimEnabled(0);
                } else {
                    isSimEnabled = false;
                }
                dialpadFragment.mIsSim1Enabled = isSimEnabled;
                DialpadFragment dialpadFragment2 = DialpadFragment.this;
                if (DialpadFragment.this.mIsSim2Present) {
                    z = SimFactoryManager.isSimEnabled(1);
                }
                dialpadFragment2.mIsSim2Enabled = z;
            }
        });
    }

    public void onDetach() {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "DialpadFragment Detach from Activity");
        }
        super.onDetach();
    }

    public void clearDigitsText() {
        if (this.mDigits != null) {
            this.mDigits.getText().clear();
        }
        setResetFirstPressFlag(true);
    }

    public void clearDigitsTextOnCursorBefore() {
        if (this.mDigits != null) {
            int length = this.mDigits.length();
            if (length == this.mDigits.getSelectionStart() && length == this.mDigits.getSelectionEnd()) {
                this.mDigits.setCursorVisible(false);
            }
            int pos = this.mDigits.getSelectionStart();
            while (pos > 0) {
                if (!keyPressed(67)) {
                    this.mDigits.getText().clear();
                    break;
                }
                pos = this.mDigits.getSelectionStart();
            }
        }
        setResetFirstPressFlag(true);
    }

    private void setDigitsText(String text) {
        if (this.mDigits != null) {
            this.mDigits.setText(text);
        }
        if (TextUtils.isEmpty(text)) {
            setResetFirstPressFlag(true);
        } else {
            setResetFirstPressFlag(false);
        }
    }

    private void startMotionRecognition() {
        if (this.mMReco == null) {
            this.mMReco = MotionRecognition.getInstance(this.mActivity.getApplicationContext(), this);
        }
        this.mMReco.stratMotionRecognition(602);
    }

    private void destroyMotionRecognition() {
        if (this.mMReco != null) {
            this.mMReco.stopMotionRecognition(602);
            this.mMReco.destroy(this);
            this.mMReco = null;
        }
    }

    public boolean acceptThisEvent(int motion) {
        return motion == 602 && this.mMReco != null;
    }

    public void handleMotionEvent(int motion) {
        if (this.mSingleHandMode != null) {
            int oldMode = this.mCurMode != -1 ? this.mCurMode : this.mSingleHandMode.getCurrentHandMode();
            if (motion != oldMode) {
                int newMode = motion;
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragment", "handleMotionEvent oldMotion:" + oldMode + " newMotion:" + motion + " mCurMode:" + this.mCurMode);
                }
                if (this.mRightHandLayout != null || this.mLeftHandLayout != null) {
                    if ((motion == 1 && this.mCurMode == 2) || (motion == 2 && this.mCurMode == 1)) {
                        updateDialpadHandModeView(0, true, true);
                        updateButtonStates(true);
                        return;
                    }
                    if ((motion == 2 && this.mCurMode == 0) || this.mCurMode == -1) {
                        updateDialpadHandModeView(2, true, true);
                    } else {
                        if (!(motion == 1 && this.mCurMode == 0)) {
                            if (this.mCurMode == -1) {
                            }
                        }
                        updateDialpadHandModeView(1, true, true);
                    }
                    updateButtonStates(true);
                }
            }
        }
    }

    private boolean isInLeftOrRightState() {
        boolean ret = this.mCurMode == -1 || this.mCurMode == 0;
        if (HwLog.HWDBG) {
            boolean z;
            String str = "DialpadFragment";
            StringBuilder append = new StringBuilder().append("isInLeftOrRightState:");
            if (ret) {
                z = false;
            } else {
                z = true;
            }
            HwLog.d(str, append.append(z).toString());
        }
        if (ret) {
            return false;
        }
        return true;
    }

    private boolean isInView(View view, int x, int y) {
        view.getLocationOnScreen(new int[2]);
        if (x >= view.getWidth() || x <= 0 || y >= view.getHeight() || y <= 0) {
            return false;
        }
        return true;
    }

    void playTone(int action, int id, boolean isFirstPress) {
        int keyIndex;
        if (1 == action || 3 == action) {
            keyIndex = transViewIdToKeyIdx(id);
            this.mPressedDialpadKeys.remove(Integer.valueOf(keyIndex));
            if (isFirstPress && 1 == action) {
                if (!this.hasResponseLongClick) {
                    this.mTonePlayer.playback(keyIndex, true, false, getHapticFeedback());
                }
            } else if (this.mPressedDialpadKeys.isEmpty()) {
                this.mTonePlayer.stop();
            }
        } else if (action == 0 && !isFirstPress) {
            keyIndex = transViewIdToKeyIdx(id);
            this.mPressedDialpadKeys.add(Integer.valueOf(keyIndex));
            this.mTonePlayer.playback(keyIndex, true, true, getHapticFeedback());
        }
    }

    private void playVibrate() {
        this.mTonePlayer.playVibrate(getHapticFeedback());
    }

    private int transViewIdToKeyIdx(int viewId) {
        switch (viewId) {
            case R.id.contacts_dialpad_one:
                return 0;
            case R.id.contacts_dialpad_two:
                return 1;
            case R.id.contacts_dialpad_three:
                return 2;
            case R.id.contacts_dialpad_four:
                return 3;
            case R.id.contacts_dialpad_five:
                return 4;
            case R.id.contacts_dialpad_six:
                return 5;
            case R.id.contacts_dialpad_seven:
                return 6;
            case R.id.contacts_dialpad_eight:
                return 7;
            case R.id.contacts_dialpad_nine:
                return 8;
            case R.id.contacts_dialpad_star:
                return 9;
            case R.id.contacts_dialpad_zero:
                return 10;
            case R.id.contacts_dialpad_pound:
                return 11;
            default:
                return -1;
        }
    }

    private void keyPressedByViewId(int viewId) {
        PLog.d(1001, "DialpadFragment.onTouch for jlog");
        switch (viewId) {
            case R.id.contacts_dialpad_one:
                keyPressed(8);
                return;
            case R.id.contacts_dialpad_two:
                keyPressed(9);
                return;
            case R.id.contacts_dialpad_three:
                keyPressed(10);
                return;
            case R.id.contacts_dialpad_four:
                keyPressed(11);
                return;
            case R.id.contacts_dialpad_five:
                keyPressed(12);
                return;
            case R.id.contacts_dialpad_six:
                keyPressed(13);
                return;
            case R.id.contacts_dialpad_seven:
                keyPressed(14);
                return;
            case R.id.contacts_dialpad_eight:
                keyPressed(15);
                return;
            case R.id.contacts_dialpad_nine:
                keyPressed(16);
                return;
            default:
                return;
        }
    }

    public boolean onTouch(View arg0, MotionEvent arg1) {
        if (this.mGestureDetector != null) {
            this.mGestureDetector.onTouchEvent(arg1);
        }
        if (this.canClickDialpad) {
            int action = arg1.getAction();
            int viewId = arg0.getId();
            boolean isFirstPress = isFirstPress();
            if ((1 == action && isFirstPress) || (action == 0 && !isFirstPress)) {
                if (!isInView(arg0, (int) arg1.getX(), (int) arg1.getY())) {
                    this.hasResponseLongClick = false;
                    return false;
                }
            }
            playTone(action, viewId, isFirstPress);
            if (1 == action && isFirstPress) {
                keyPressedByViewId(viewId);
                if (!this.hasResponseLongClick) {
                    setResetFirstPressFlag(false);
                }
                this.hasResponseLongClick = false;
            } else if (action == 0 && !isFirstPress) {
                keyPressedByViewId(viewId);
                this.hasResponseLongClick = false;
                setResetFirstPressFlag(false);
            }
            return false;
        }
        this.mPressedDialpadKeys.clear();
        this.mTonePlayer.stop();
        return false;
    }

    private final void setBottomMargin(LayoutParams lp, int margin) {
        if (lp == null) {
            return;
        }
        if (lp instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) lp).bottomMargin = margin;
        } else if (lp instanceof RelativeLayout.LayoutParams) {
            ((RelativeLayout.LayoutParams) lp).bottomMargin = margin;
        }
    }

    public void onUpdateSinglehandView(int aCurrentMode, boolean aSaveMode, boolean playAnimation) {
        HwLog.d("DialpadFragment", "onUpdateSinglehandView mIsAnimationInProgress:" + this.mIsAnimationInProgress + " canClickDialpad:" + this.canClickDialpad + " aCurrentMode:" + aCurrentMode);
        if (this.mRightHandLayout != null && this.mLeftHandLayout != null && this.mSingleHandMode != null) {
            boolean lPlayAnimation = playAnimation;
            if (this.mIsAnimationInProgress) {
                lPlayAnimation = false;
            }
            this.mCurMode = aCurrentMode;
            int gravityHeight = this.mResource.getDimensionPixelSize(R.dimen.dialpad_additional_buttons_layout_hidden_height);
            if (this.mDigitsVisible) {
                gravityHeight = getDialerHeight(this.mDigitsVisible);
            }
            if (this.mRightHandLayout.getVisibility() == 8) {
                setSingleHandViewGone(this.mRightHandLayout, 0);
            }
            if (this.mLeftHandLayout.getVisibility() == 8) {
                setSingleHandViewGone(this.mLeftHandLayout, 0);
            }
            if (aCurrentMode == 1) {
                this.mSingleHandAdapter.initAnimatorSet(this.mNormalWidth - this.mSingleHandWidth, (float) gravityHeight, 0.0f);
                this.mSingleHandMode.saveUserSelectionHandMode(aCurrentMode);
            } else if (aCurrentMode == 2) {
                this.mSingleHandAdapter.initAnimatorSet(this.mNormalWidth - this.mSingleHandWidth, (float) gravityHeight, this.mTranslationX);
                this.mSingleHandMode.saveUserSelectionHandMode(aCurrentMode);
            } else {
                this.mSingleHandAdapter.initAnimatorSet(this.mNormalWidth, (float) gravityHeight, 0.0f);
                aCurrentMode = 0;
            }
            this.mSingleHandAdapter.startAnimatorUpdateView(true, aCurrentMode, lPlayAnimation);
            changeDialerDigitsHeight();
        }
    }

    private void setSingleHandViewGone(View singleHandView, int visiblity) {
        singleHandView.setVisibility(visiblity);
    }

    private void initKeyLayoutParams(Context context) {
        this.mSingleHandAdapter = new SingleHandAdapter(context);
        this.mSingleHandAdapter.setAnimatorListener(new AnimatorListener() {
            public void onAnimatorEnd(ObjectAnimator animator) {
                DialpadFragment.this.canClickDialpad = true;
            }

            public void onAnimatorStart(ObjectAnimator animator) {
                DialpadFragment.this.canClickDialpad = false;
            }
        });
        this.mNormalWidth = (float) context.getResources().getDisplayMetrics().widthPixels;
        this.mSingleHandWidth = (float) getDimenPixelSize(R.dimen.contact_dialpad_single_hand_width);
        this.mTranslationX = this.mSingleHandWidth;
    }

    private void setContainerParentHeight(int height) {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "setContainerParentHeight:" + height);
        }
        if (this.mGravityParent == null || !(this.mGravityParent.getLayoutParams() instanceof FrameLayout.LayoutParams)) {
            HwLog.w("DialpadFragment", "mGravityParent is null.");
        } else {
            FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) this.mGravityParent.getLayoutParams();
            param.width = -1;
            param.height = height;
            param.gravity = 80;
            this.mGravityParent.setLayoutParams(param);
            this.mDialerContainer.requestLayout();
        }
        this.mLastHeight = height;
        if (this.mDialerContainer == null || !(this.mDialerContainer.getLayoutParams() instanceof FrameLayout.LayoutParams)) {
            HwLog.w("DialpadFragment", "mDialerContainer is null.");
            return;
        }
        FrameLayout.LayoutParams rLayParam = (FrameLayout.LayoutParams) this.mDialerContainer.getLayoutParams();
        rLayParam.height = height;
        rLayParam.gravity = 80;
        this.mDialerContainer.setLayoutParams(rLayParam);
        this.mDialerContainer.requestLayout();
    }

    public void loadDelayLoadingLayout() {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "DialpadFragment-laodDelayLoadingLayout,START=" + System.nanoTime());
        }
        if (this.fragmentView != null && !this.mDelayLoadingLoaded) {
            loadCalllogView();
            loadListView();
            displayDigits();
            this.mDelayLoadingLoaded = true;
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragment", "DialpadFragment-laodDelayLoadingLayout,  END=" + System.nanoTime());
            }
        }
    }

    private void loadCalllogView() {
        if (!this.mIsCallLogViewLoaded) {
            ViewStub calllogStub = (ViewStub) this.fragmentView.findViewById(R.id.calllog_fragment_container);
            if (calllogStub != null) {
                calllogStub.inflate();
                this.mCallLogFragmentView = this.fragmentView.findViewById(R.id.calllog_fragment);
                if (this.mCallLogFragmentView != null) {
                    this.mCallLogFragmentView.setVisibility(this.mIsCalllogVisible ? 0 : 8);
                    CallLogFragment lCallLogFragment = getCallLogFragment();
                    if (lCallLogFragment != null) {
                        addListViewSelectedHeader(lCallLogFragment, true);
                        lCallLogFragment.getSuspentionScroller().setCallLogListener(this);
                        lCallLogFragment.setCallLogListener(this.mDialerListener);
                    }
                    if (getIsFromNotificationFlag()) {
                        setCallLogFragmentFullScreen(lCallLogFragment);
                    }
                    this.mIsCallLogViewLoaded = true;
                }
            }
        }
    }

    private void addListViewSelectedHeader(CallLogFragment lCallLogFragment, boolean isFromLoadCallLogView) {
        Activity activity = getActivity();
        if (activity != null && lCallLogFragment != null) {
            if (activity instanceof PeopleActivity) {
                this.mHeaderView = ((PeopleActivity) activity).getDialpadFragmentHelper().getCallLogListHeaderView();
            } else {
                this.mHeaderView = activity.getLayoutInflater().inflate(R.layout.contacts_radio_button_group_divider, null);
            }
            this.mButtonGroupLyout = (ButtonGroupLyout) this.mHeaderView.findViewById(R.id.btn_status_group_parent);
            this.mButtonGroupLyout.setButtonText(R.string.contacts_str_filter_all_calls, R.string.contacts_str_filter_missed_calls, R.string.contacts_str_filter_unKnown_calls, R.string.voicemail);
            this.mButtonGroupLyout.setButtonSelectedType(this.mButtonGroupLayoutSelectedType);
            this.mButtonGroupLyout.setRadioButtonListener(this.radioButtonListener);
            ListView mListView = lCallLogFragment.getCallsListView();
            if (mListView != null) {
                measureView(this.mHeaderView);
                CommonUtilMethods.addFootEmptyViewPortrait(mListView, getContext());
                hideListViewSelectedHeader(true);
                this.suspentionScroller = lCallLogFragment.getSuspentionScroller();
                if (this.suspentionScroller != null) {
                    this.suspentionScroller.setSuspentionView(this.mHeaderView);
                    this.suspentionScroller.setListView(false);
                    this.suspentionScroller.init();
                }
            }
        }
    }

    public void hideListViewSelectedHeader(boolean hasCalllog) {
        if ((this.mButtonGroupLyout == null || this.mButtonGroupLyout.getButtonSelectedType() == 1) && this.mHeaderView != null) {
            CallLogFragment lCallLogFragment = getCallLogFragment();
            if (lCallLogFragment != null) {
                this.mOriginalLazyMode = lCallLogFragment.getCanShowLazyMode();
                lCallLogFragment.setCanShowLazyMode(false);
            }
            HwLog.d("DialpadFragment", "hideListViewSelectedHeader");
        }
    }

    private void showListViewSelectedHeader(View headerView) {
        if (headerView != null && !isEmptyCallLogList()) {
            headerView.setPadding(0, 0, 0, 0);
            CallLogFragment lCallLogFragment = getCallLogFragment();
            if (lCallLogFragment != null) {
                lCallLogFragment.setCanShowLazyMode(this.mOriginalLazyMode);
            }
        }
    }

    private boolean isEmptyCallLogList() {
        CallLogFragment callLogFragment = getCallLogFragment();
        int count = 0;
        if (callLogFragment != null) {
            count = callLogFragment.mListView != null ? callLogFragment.mListView.getCount() : 0;
        }
        if (count > 1) {
            return false;
        }
        return true;
    }

    private void measureView(View child) {
        int childHeightSpec;
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(-1, -2);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, params.width);
        int lpHeight = params.height;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, 0);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    private void loadListView() {
        if (!this.mIsListViewLoaded) {
            ViewStub listStub = (ViewStub) this.fragmentView.findViewById(R.id.list_container_stub);
            if (listStub != null) {
                listStub.inflate();
                this.mListContainer = this.fragmentView.findViewById(R.id.list_container);
                this.mList = (ListView) this.fragmentView.findViewById(R.id.freq_call_list);
                this.mList.setFastScrollEnabled(true);
                if (!(!SingleHandModeManager.getInstance(this.mActivity.getApplicationContext()).isSingleHandFeatureEnabled() || ContactDpiAdapter.SRC_DPI == 0 || ContactDpiAdapter.REAL_Dpi == 0)) {
                    float singleDigitsPad = 4.0f * (getResources().getDimension(R.dimen.dialpad_huawei_item_height) - getResources().getDimension(R.dimen.dialpad_huawei_item_height_singlehandmode));
                    float newDimens = ((float) (getResources().getDimensionPixelSize(R.dimen.search_list_first_time_height) * ContactDpiAdapter.SRC_DPI)) / ((float) ContactDpiAdapter.REAL_Dpi);
                    if (this.mList.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.mList.getLayoutParams();
                        if (ContactDpiAdapter.NOT_SRC_DPI) {
                            lp.height = (int) ((((((float) (getResources().getDimensionPixelSize(R.dimen.dialpad_dialer_container_init_height) * ContactDpiAdapter.SRC_DPI)) / ((float) ContactDpiAdapter.REAL_Dpi)) - getResources().getDimension(R.dimen.dialpad_dialer_container_init_height)) + newDimens) + singleDigitsPad);
                        } else {
                            lp.height = (int) (newDimens + singleDigitsPad);
                        }
                    }
                }
                this.mList.setFriction(this.mFriction);
                this.mList.setDivider(null);
                this.mList.setVelocityScale(this.mVelocityScale);
                this.mList.setOnScrollListener(new OnScrollListener() {
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (scrollState == 0) {
                            DialpadFragment.this.mFirstVisiblePosition = DialpadFragment.this.mList.getFirstVisiblePosition();
                            DialpadFragment.this.requestDigistFocus();
                        }
                        if (1 == scrollState) {
                            Activity lActivity = DialpadFragment.this.getActivity();
                            if (lActivity != null && (lActivity instanceof PeopleActivity)) {
                                ((PeopleActivity) lActivity).clearCurrentFocus();
                            }
                        }
                    }

                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    }
                });
                if (!this.mIsLandscape) {
                    this.mList.setOnTouchListener(new OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            if (HwLog.HWDBG) {
                                HwLog.d("DialpadFragment", "mList onTouch!!!! canClickDialpad:" + DialpadFragment.this.canClickDialpad);
                            }
                            DialpadFragment.this.mIgnoreSmartSearchListReDraw = false;
                            if (!DialpadFragment.this.mIsAnimationInProgress && DialpadFragment.this.canClickDialpad && ((1 == event.getAction() || 2 == event.getAction()) && DialpadFragment.this.mDialpad != null && DialpadFragment.this.mDialpad.getVisibility() == 0)) {
                                if (HwLog.HWDBG) {
                                    HwLog.d("DialpadFragment", "minimizing dialer on Touch of listView!!!");
                                }
                                DialpadFragment.this.setDigitsVisible(false, true);
                            }
                            return false;
                        }
                    });
                }
                this.mList.setOnCreateContextMenuListener(this);
                this.mDialpadAnimationDown = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.dialpad_anim_down);
                this.mDialpadAnimationUp = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.dialpad_anim_up);
                this.mIsListViewLoaded = true;
                if (!this.mIsLandscape) {
                    this.mAnimateHelper.initView(getActivity(), this.mDigitsContainer, this.mDigits, this.mListContainer);
                    this.mAnimateHelper.setListener(new DialpadAnimatorListener() {
                        public void onAnimationStart() {
                        }

                        public void onAnimationEnd() {
                            HwLog.d("DialpadAnimateHelper", "notifySearchAdapterChanged mMargintTop:" + DialpadFragment.this.mMargintTop);
                            DialpadFragment.this.setListContainerLayout(DialpadFragment.this.mMargintTop);
                            if (DialpadFragment.this.isDigitsEmpty()) {
                                DialpadFragment.this.showHeader(false, false);
                            }
                            if (DialpadFragment.this.mList != null) {
                                DialpadFragment.this.mList.post(new Runnable() {
                                    public void run() {
                                        if (DialpadFragment.this.mList != null) {
                                            DialpadFragment.this.mList.setSelection(DialpadFragment.this.mFirstVisiblePosition);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }

    private void updateRecommendedCard(String number) {
        this.mCardNameDial2.setBackgroundResource(R.drawable.btn_call);
        this.mCardNameDial3.setBackgroundResource(R.drawable.btn_call);
        if (this.mCustDialpadFragment != null) {
            this.mCustDialpadFragment.setEncryptBtnBgNormal();
        }
        this.isSimOneRecommended = false;
        this.isSimTwoRecommended = false;
        this.needUpdateRecommendedButtonState = true;
        updateRecommendedCardInNewThread(getActivity().getApplicationContext(), IsPhoneNetworkRoamingUtils.removeDashesAndBlanksBrackets(number));
    }

    private void updateRecommendedCardInNewThread(final Context context, final String number) {
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                int slotId;
                if (HapEncryptCallUtils.isEncryptCallEnabled()) {
                    slotId = CommonUtilMethods.queryLastCallNumberFromEncryptCall(number, context);
                } else {
                    slotId = CommonUtilMethods.queryCallNumberLastSlot(number, context);
                }
                if (DialpadFragment.this.mHandler.hasMessages(264)) {
                    DialpadFragment.this.mHandler.removeMessages(264);
                }
                Message message = Message.obtain();
                message.what = 264;
                message.arg1 = slotId;
                DialpadFragment.this.mHandler.sendMessage(message);
            }
        });
    }

    public void checkEmergencyEx() {
    }

    private void performAddBlackList(MenuItem item, AllContactListItemViews aView, String number, String lBlackListOnlyName, IHarassmentInterceptionService aService) {
        if (item != null && aView != null) {
            if (item.getTitle().equals(this.mResource.getText(R.string.contact_menu_add_to_blacklist))) {
                String lName;
                if (aView.mLookupUri != null) {
                    lName = aView.mName.getText().toString();
                } else {
                    lName = lBlackListOnlyName;
                }
                BlacklistCommonUtils.handleNumberBlockList(getActivity(), aService, number, lName, 0, true);
            } else if (item.getTitle().equals(this.mResource.getText(R.string.contact_menu_remove_from_blacklist))) {
                BlacklistCommonUtils.handleNumberBlockList(getActivity(), aService, number, lBlackListOnlyName, 1, true);
            }
        }
    }

    public void dialVideoCall() {
        String number = this.mDigits.getText().toString();
        if (number.length() != 0 && !TextUtils.isEmpty(this.mProhibitedPhoneNumberRegexp) && number.matches(this.mProhibitedPhoneNumberRegexp) && SystemProperties.getInt("persist.radio.otaspdial", 0) != 1) {
            if (getActivity() != null) {
                this.mShowErrorDiallogUtils.showCannotMakeCallDialog();
            }
            clearDigitsText();
            showDigistHeader(false, false);
        } else if (this.mCallNumberEnable) {
            this.mCallNumberEnable = false;
            VtLteUtils.startVideoCall(number, getActivity());
            this.mClearDigitsOnStop = true;
        }
    }

    public void setFromNotificationFlag(boolean isFromNotificationFlag) {
        this.isFromNotificationFlag = isFromNotificationFlag;
    }

    private boolean getIsFromNotificationFlag() {
        return this.isFromNotificationFlag;
    }

    private void setCallLogFragmentFullScreen(CallLogFragment lCallLogFragment) {
        if (lCallLogFragment != null) {
            ListView lCallsListView = lCallLogFragment.getCallsListView();
            if (lCallsListView != null) {
                LayoutParams lCallsListParams = lCallsListView.getLayoutParams();
                lCallsListParams.width = -1;
                lCallsListParams.height = -1;
                setBottomMargin(lCallsListParams, getDialerHeight(false));
                lCallsListView.setLayoutParams(lCallsListParams);
            }
        }
        hideDialpad();
        if (this.mCallLogScrollFirstVisibleItem && !this.mCallLogTouchHideDailpad && this.mHeaderView != null && this.mHeaderView.getPaddingTop() < 0) {
            showListViewSelectedHeader(this.mHeaderView);
        }
    }

    private void setTableTopRowVisibility(int visibility) {
        if (this.mTableRow0 != null) {
            int i;
            this.mTableRow0.setVisibility(this.mTableRowRcs.getVisibility() & visibility);
            this.mTableRow0.findViewById(R.id.new_contact).setVisibility(visibility);
            this.mTableRow0.findViewById(R.id.add_exist_contact).setVisibility(visibility);
            this.mTableRow0.findViewById(R.id.send_sms).setVisibility(visibility);
            View findViewById = this.mTableRow0.findViewById(R.id.video_call_button);
            if (VtLteUtils.isVtLteOn(getActivity())) {
                i = visibility;
            } else {
                i = 8;
            }
            findViewById.setVisibility(i);
        }
        if (this.mLineHorizontalTopTable0 == null) {
            return;
        }
        if (8 == visibility || !this.mIsLandscape) {
            this.mLineHorizontalTopTable0.setVisibility(this.mTableRowRcs.getVisibility() & visibility);
        }
    }

    public void setTableRow0Visible(int visibility) {
        if (this.mTableRow0 != null) {
            this.mTableRow0.setVisibility(visibility);
        }
    }

    public int getNewContactVisibility() {
        if (this.mTableRow0 != null) {
            return this.mTableRow0.findViewById(R.id.new_contact).getVisibility();
        }
        return 8;
    }

    private void updateSingleCardButtonDelayed() {
        if (this.mDelayedUpdateModeHandler.hasMessages(PlacesStatusCodes.USAGE_LIMIT_EXCEEDED)) {
            this.mDelayedUpdateModeHandler.removeMessages(PlacesStatusCodes.USAGE_LIMIT_EXCEEDED);
        }
        Message msg = new Message();
        msg.what = PlacesStatusCodes.USAGE_LIMIT_EXCEEDED;
        this.mDelayedUpdateModeHandler.sendMessageDelayed(msg, 500);
    }

    private void updateSingleCardButton() {
        if (this.mDialButton == null) {
            HwLog.i("DialpadFragment", "updateSingleCardButton,view is not ready");
            return;
        }
        int imageId;
        int contentDescId;
        boolean isImsRegistered = CommonUtilMethods.getTelephonyManager(this.mDialButton.getContext()).isImsRegistered();
        boolean isEmergencyNumber = false;
        if (this.mDigits != null) {
            isEmergencyNumber = CommonUtilMethods.isEmergencyNumber(this.mDigits.getText().toString(), this.mIsPhoneMultiSim);
        }
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "updateSingleCardButton,isImsRegistered = " + isImsRegistered + ",isEmergencyNumber = " + isEmergencyNumber);
        }
        if (isEmergencyNumber) {
            imageId = R.drawable.dial_single_button_back;
            contentDescId = R.string.description_dial_button;
        } else if (mCust != null && mCust.isVOWifiCallEnabled(this.mDialButton.getContext())) {
            imageId = mCust.getVOWifiCallBtnIconForSingleSim(R.drawable.dial_single_button_back);
            if (HwCustContactFeatureUtils.isVOWifiFeatureEnabled()) {
                contentDescId = R.string.content_description_wifi_dial;
            } else {
                contentDescId = R.string.description_dial_button;
            }
        } else if (isImsRegistered) {
            int iconRule = HwCarrierConfigManagerInner.getDefault().getVolteIconRule(this.mDialButton.getContext(), SimFactoryManager.getDefaultSubscription(), 192);
            if (iconRule == 2) {
                if (HwTelephonyManager.getDefault().getImsDomain() == 1) {
                    imageId = R.drawable.dial_single_button_vowifi_back;
                    contentDescId = R.string.content_description_vowifi_dial;
                } else {
                    imageId = R.drawable.dial_single_button_volte_back;
                    contentDescId = R.string.content_description_volte_dial;
                }
            } else if (iconRule == 1 && EmuiFeatureManager.isContactDialpadHdIconOn()) {
                imageId = R.drawable.dial_single_button_hd_back;
                contentDescId = R.string.content_description_hd_dial;
            } else {
                imageId = R.drawable.dial_single_button_back;
                contentDescId = R.string.description_dial_button;
            }
        } else {
            imageId = R.drawable.dial_single_button_back;
            contentDescId = R.string.description_dial_button;
        }
        this.mDialButton.setBackgroundResource(imageId);
        this.mDialButton.setContentDescription(this.mDialButton.getContext().getString(contentDescId));
    }

    private void setTableRowsPadding(TableRow[] array, int pLeft, int pTop, int pRight, int pBottom) {
        for (TableRow t : array) {
            if (t != null) {
                t.setPadding(pLeft, pTop, pRight, pBottom);
            }
        }
    }

    private void setTableRowsLayout(int newHeight, boolean isSingleHandModeOn) {
        if (this.mTableRow1 != null && this.mTableRow2 != null && this.mTableRow3 != null && this.mTableRow4 != null) {
            int backgroundResource;
            int index;
            View ldigit;
            LayoutParams ldigitLP;
            if (isSingleHandModeOn) {
                backgroundResource = R.drawable.contacts_item_background_singlehand_material;
            } else {
                backgroundResource = R.drawable.contacts_item_background_material;
            }
            if (this.fragmentView != null) {
                this.fragmentView.findViewById(R.id.contacts_dialpad_one).setBackgroundResource(backgroundResource);
                this.fragmentView.findViewById(R.id.contacts_dialpad_two).setBackgroundResource(backgroundResource);
                this.fragmentView.findViewById(R.id.contacts_dialpad_three).setBackgroundResource(backgroundResource);
                this.fragmentView.findViewById(R.id.contacts_dialpad_four).setBackgroundResource(backgroundResource);
                this.fragmentView.findViewById(R.id.contacts_dialpad_five).setBackgroundResource(backgroundResource);
                this.fragmentView.findViewById(R.id.contacts_dialpad_six).setBackgroundResource(backgroundResource);
                this.fragmentView.findViewById(R.id.contacts_dialpad_seven).setBackgroundResource(backgroundResource);
                this.fragmentView.findViewById(R.id.contacts_dialpad_eight).setBackgroundResource(backgroundResource);
                this.fragmentView.findViewById(R.id.contacts_dialpad_nine).setBackgroundResource(backgroundResource);
                this.fragmentView.findViewById(R.id.contacts_dialpad_star).setBackgroundResource(backgroundResource);
                this.fragmentView.findViewById(R.id.contacts_dialpad_zero).setBackgroundResource(backgroundResource);
                this.fragmentView.findViewById(R.id.contacts_dialpad_pound).setBackgroundResource(backgroundResource);
            }
            LayoutParams lButtonLP = this.mTableRow1.getLayoutParams();
            if (lButtonLP != null) {
                lButtonLP.height = newHeight;
                this.mTableRow1.setLayoutParams(lButtonLP);
            }
            lButtonLP = this.mTableRow2.getLayoutParams();
            if (lButtonLP != null) {
                lButtonLP.height = newHeight;
                this.mTableRow2.setLayoutParams(lButtonLP);
            }
            lButtonLP = this.mTableRow3.getLayoutParams();
            if (lButtonLP != null) {
                lButtonLP.height = newHeight;
                this.mTableRow3.setLayoutParams(lButtonLP);
            }
            lButtonLP = this.mTableRow4.getLayoutParams();
            if (lButtonLP != null) {
                lButtonLP.height = newHeight;
                this.mTableRow4.setLayoutParams(lButtonLP);
            }
            for (index = 0; index < this.mTableRow1.getChildCount(); index++) {
                ldigit = this.mTableRow1.getChildAt(index);
                ldigitLP = ldigit.getLayoutParams();
                if (ldigitLP != null) {
                    ldigitLP.height = newHeight;
                    ldigit.setLayoutParams(ldigitLP);
                }
            }
            for (index = 0; index < this.mTableRow2.getChildCount(); index++) {
                ldigit = this.mTableRow2.getChildAt(index);
                ldigitLP = ldigit.getLayoutParams();
                if (ldigitLP != null) {
                    ldigitLP.height = newHeight;
                    ldigit.setLayoutParams(ldigitLP);
                }
            }
            for (index = 0; index < this.mTableRow3.getChildCount(); index++) {
                ldigit = this.mTableRow3.getChildAt(index);
                ldigitLP = ldigit.getLayoutParams();
                if (ldigitLP != null) {
                    ldigitLP.height = newHeight;
                    ldigit.setLayoutParams(ldigitLP);
                }
            }
            for (index = 0; index < this.mTableRow4.getChildCount(); index++) {
                ldigit = this.mTableRow4.getChildAt(index);
                ldigitLP = ldigit.getLayoutParams();
                if (ldigitLP != null) {
                    ldigitLP.height = newHeight;
                    ldigit.setLayoutParams(ldigitLP);
                }
            }
        }
    }

    private void setContainerParentHeightLand(int height) {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragment", "setContainerParentHeightLand:" + height);
        }
        if (this.mDialerContainer == null || !(this.mDialerContainer.getLayoutParams() instanceof LinearLayout.LayoutParams)) {
            HwLog.w("DialpadFragment", "mDialerContainer is null.");
            return;
        }
        LinearLayout.LayoutParams rLayParam = (LinearLayout.LayoutParams) this.mDialerContainer.getLayoutParams();
        rLayParam.height = height;
        rLayParam.gravity = 48;
        this.mDialerContainer.setLayoutParams(rLayParam);
        this.mDialerContainer.requestLayout();
    }

    private void initWriteSpaceView() {
        if (this.mWriteSpaceView != null) {
            LayoutParams lp = this.mWriteSpaceView.getLayoutParams();
            lp.height = getWriteSpaceHeight();
            this.mWriteSpaceView.setLayoutParams(lp);
        }
    }

    private int getWriteSpaceHeight() {
        if (!CommonUtilMethods.isSpecialLanguageForDialpad()) {
            return getResources().getDimensionPixelSize(R.dimen.dialpad_table_row_write_space_height);
        }
        if (!isInLeftOrRightState() || this.mIsLandscape) {
            return getResources().getDimensionPixelSize(R.dimen.dialpad_table_row_write_space_special_language_height);
        }
        return getResources().getDimensionPixelSize(R.dimen.dialpad_table_row_write_space_height);
    }

    public String onVoicemailStatusFetched(List<String> activedAccounts) {
        if (this.mActivity == null) {
            HwLog.d("DialpadFragment", "onVoicemailStatusFetched return;");
            return null;
        }
        ActiveAccountState activeState = ActiveAccountState.getActiveAccountState(activedAccounts, this.mActivity);
        HwLog.d("DialpadFragment", "onVoicemailStatusFetched : " + activeState);
        String phoneAccountId = activeState.activedId;
        if (phoneAccountId == null || this.mFilterType != 4) {
            this.mGreetingMenuVisible = false;
            this.mPhoneAccountId = null;
            if (CommonUtilMethods.calcIfNeedSplitScreen() && this.mSplitActionBarView != null) {
                this.mSplitActionBarView.setVisibility(3, false);
            }
            if (this.mGreetingMenu != null) {
                this.mGreetingMenu.setVisible(false);
            }
        } else {
            this.mGreetingMenuVisible = true;
            this.mPhoneAccountId = phoneAccountId;
            if (CommonUtilMethods.calcIfNeedSplitScreen() && this.mSplitActionBarView != null) {
                this.mSplitActionBarView.setVisibility(3, true);
            }
            if (this.mGreetingMenu != null && (this.mIsLandscape || !(this.mAdditionalButtonsRow == null || this.mAdditionalButtonsRow.getVisibility() == 0))) {
                this.mGreetingMenu.setVisible(true);
            }
        }
        return activeState.todoActiveId;
    }

    public void showEmptyView(boolean isShow) {
        boolean z = false;
        if (this.mDeleteCalllogMenu != null) {
            boolean z2;
            MenuItem menuItem = this.mDeleteCalllogMenu;
            if (isShow) {
                z2 = false;
            } else {
                z2 = true;
            }
            menuItem.setEnabled(z2);
        }
        if (!isShow) {
            z = true;
        }
        this.mIsDeleteCalllogMenuEnable = z;
        if (this.mSplitActionBarView != null && CommonUtilMethods.calcIfNeedSplitScreen()) {
            this.mSplitActionBarView.setEnable(2, this.mIsDeleteCalllogMenuEnable);
        }
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
                                    HwLog.e("DialpadFragment", "Activity not find!");
                                }
                                return;
                            }
                        }
                        break;
                    }
            }
        }
    }

    private void configureKeypadListeners(DialpadKeyButton dialpadKey, int buttonId) {
        dialpadKey.setOnPressedListener(this);
        switch (buttonId) {
            case R.id.contacts_dialpad_one:
                dialpadKey.setLongHoverContentDescription(getActivity().getResources().getString(R.string.voicemail));
                return;
            case R.id.contacts_dialpad_two:
            case R.id.contacts_dialpad_three:
            case R.id.contacts_dialpad_four:
            case R.id.contacts_dialpad_five:
            case R.id.contacts_dialpad_six:
            case R.id.contacts_dialpad_seven:
            case R.id.contacts_dialpad_eight:
            case R.id.contacts_dialpad_nine:
                dialpadKey.setLongHoverContentDescription(getActivity().getResources().getString(R.string.button_speed_dial));
                return;
            case R.id.contacts_dialpad_star:
                dialpadKey.setLongHoverContentDescription(getActivity().getResources().getString(R.string.long_hover_content_description_comma));
                return;
            case R.id.contacts_dialpad_zero:
                dialpadKey.setLongHoverContentDescription(getActivity().getResources().getString(R.string.long_hover_content_description_plus));
                return;
            case R.id.contacts_dialpad_pound:
                dialpadKey.setLongHoverContentDescription(getActivity().getResources().getString(R.string.long_hover_content_description_semicolon));
                return;
            default:
                return;
        }
    }

    public void onPressed(View view, boolean pressed) {
        if (pressed && CommonUtilMethods.isTalkBackEnabled(this.mActivity)) {
            switch (view.getId()) {
                case R.id.contacts_dialpad_one:
                    keyPressed(8);
                    break;
                case R.id.contacts_dialpad_two:
                    keyPressed(9);
                    break;
                case R.id.contacts_dialpad_three:
                    keyPressed(10);
                    break;
                case R.id.contacts_dialpad_four:
                    keyPressed(11);
                    break;
                case R.id.contacts_dialpad_five:
                    keyPressed(12);
                    break;
                case R.id.contacts_dialpad_six:
                    keyPressed(13);
                    break;
                case R.id.contacts_dialpad_seven:
                    keyPressed(14);
                    break;
                case R.id.contacts_dialpad_eight:
                    keyPressed(15);
                    break;
                case R.id.contacts_dialpad_nine:
                    keyPressed(16);
                    break;
                case R.id.contacts_dialpad_star:
                    keyPressed(17);
                    break;
                case R.id.contacts_dialpad_zero:
                    keyPressed(7);
                    break;
                case R.id.contacts_dialpad_pound:
                    keyPressed(18);
                    break;
            }
            this.mPressedDialpadKeys.add(Integer.valueOf(view.getId()));
            return;
        }
        this.mPressedDialpadKeys.remove(Integer.valueOf(view.getId()));
        if (this.mPressedDialpadKeys.isEmpty()) {
            this.mTonePlayer.stop();
        }
    }

    private boolean isRcsLoginStatus() {
        try {
            if (EmuiFeatureManager.isRcsFeatureEnable() && RcseProfile.getRcsService() != null && RcseProfile.getRcsService().getLoginState()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            HwLog.e("DialpadFragment", "rcs login query failed");
            return false;
        }
    }

    private void showEncryptCallWhenSetEmptyList() {
        if (!HapEncryptCallUtils.isEncryptCallEnabled() || this.mTableRow0.getVisibility() != 0 || this.mEncryptCallView.getVisibility() != 0) {
            setTableTopRowVisibility(8);
        }
    }
}
