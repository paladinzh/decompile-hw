package com.android.contacts.activities;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.ServiceManager;
import android.os.UserManager;
import android.provider.ContactsContract.Contacts;
import android.provider.Telephony.MmsSms;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.amap.api.services.core.AMapException;
import com.android.contacts.CallUtil;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactSaveService.Listener;
import com.android.contacts.ContactSplitUtils;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.ContactsUtils.PredefinedNumbers;
import com.android.contacts.GeoUtil;
import com.android.contacts.MotionRecognition;
import com.android.contacts.MotionRecognition.MotionEventHandler;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.activities.ContactSelectionActivity.TranslucentActivity;
import com.android.contacts.calllog.CallLogDetailFragment;
import com.android.contacts.calllog.CallLogDetailFragment.CalllogsUpdateCallBack;
import com.android.contacts.calllog.CallLogDetailFragment.UpdateCalllogsCallBack;
import com.android.contacts.calllog.CallLogDetailHelper;
import com.android.contacts.calllog.CallLogDetailHistoryAdapter;
import com.android.contacts.calllog.CallLogQuery;
import com.android.contacts.calllog.ContactInfo;
import com.android.contacts.calllog.ContactInfoHelper;
import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.compatibility.CompatUtils;
import com.android.contacts.compatibility.NumberLocationCache;
import com.android.contacts.compatibility.NumberLocationLoader;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.detail.ContactDetailAdapter;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import com.android.contacts.detail.ContactDetailFragment;
import com.android.contacts.detail.ContactDetailFragment.QRcodeEntryListener;
import com.android.contacts.detail.ContactDetailFragment.QueryPhoneNumberListener;
import com.android.contacts.detail.ContactDetailHelper;
import com.android.contacts.detail.ContactDetailPhotoSetter;
import com.android.contacts.detail.ContactDetailPhotoSetter.PhotoClickListener;
import com.android.contacts.detail.ContactLoaderFragment;
import com.android.contacts.detail.ContactLoaderFragment.ContactLoaderFragmentListener;
import com.android.contacts.detail.UnJoinContacesDialogFragment;
import com.android.contacts.fragment.HwBaseFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.blacklist.BlacklistCommonUtils;
import com.android.contacts.hap.editor.HAPSelectAccountDialogFragment;
import com.android.contacts.hap.hotline.HLUtils;
import com.android.contacts.hap.numbermark.NumberMarkManager;
import com.android.contacts.hap.numbermark.NumberMarkUtil;
import com.android.contacts.hap.numbermark.YellowPageContactUtil;
import com.android.contacts.hap.provider.ContactsAppDatabaseHelper.NumberMark;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.activities.RcsContactDetailActivityHelp;
import com.android.contacts.hap.roaming.IsPhoneNetworkRoamingUtils;
import com.android.contacts.hap.service.NumberMarkInfo;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.AlertDialogFragmet;
import com.android.contacts.hap.util.AlertDialogFragmet.OnDialogOptionSelectListener;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.hap.utils.EraseContactMarkUtils;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.hap.utils.ScreenUtils;
import com.android.contacts.hap.widget.MultiShrinkScroller;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.Contact;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.profile.ProfileUtils;
import com.android.contacts.profile.ProfileUtils.ProfileListener;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.AutoEllipseTextView;
import com.android.contacts.util.CloseUtils;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneNumberFormatter;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.util.UriUtils;
import com.android.contacts.widget.MaterialColorMapUtils;
import com.autonavi.amap.mapcore.MapCore;
import com.google.android.gms.R;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.location.LocationRequest;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.CommonConstants;
import com.huawei.cspcommon.util.DialerHighlighter;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCustUtils;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService.Stub;
import com.huawei.numberlocation.NLUtils;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.encode.QRCodeEncoder;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactInfoFragment extends HwBaseFragment implements Listener, QueryPhoneNumberListener, QRcodeEntryListener, MotionEventHandler, OnClickListener, UpdateCalllogsCallBack {
    private static ProfileListener mProfileListener = null;
    private boolean addToExsitContactAleady = false;
    private MenuItem blacklistMenu;
    private DetailViewEntry entry;
    private OnDialogOptionSelectListener eraseContactMarkClickListener = new OnDialogOptionSelectListener() {
        public void onDialogOptionSelected(int which, Context aContext) {
            if (which == -1 && ContactInfoFragment.this.getCallLogDetailFragment() != null) {
                ContactInfoFragment.this.getCallLogDetailFragment().onMenuRemoveFromCallLog();
                StatisticalHelper.report(1121);
                ContactsThreadPool.getInstance().execute(new EraseContactTaskByLookUpUri(ContactInfoFragment.this.getApplicationContext(), ContactInfoFragment.this.mContactData, ContactInfoFragment.this.mNumberType, ContactInfoFragment.this.mPrimaryNum, ContactInfoFragment.this.getCallLogDetailFragment().getCalllogsUpdateListener(), ContactInfoFragment.this.mHandler));
                ContactInfoFragment.this.mHasDataStatInfo = false;
                ContactInfoFragment.this.mHasCalllogConInfo = false;
                ContactInfoFragment.this.mHasMmsConInfo = false;
                ContactInfoFragment.this.getCallLogDetailFragment().onVoicemailMenuDeleted();
                Activity activity = ContactInfoFragment.this.getActivity();
                if (activity != null) {
                    activity.invalidateOptionsMenu();
                }
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel arg0, int arg1) {
        }
    };
    private int freshQrcodeCount = 0;
    private boolean hasInitViews = false;
    private boolean hasPhotoChange;
    private boolean isFromCreate = false;
    boolean isFromRemote = false;
    boolean isLaunchedFromDialpad = false;
    private boolean isMenuDelLogEnabled;
    private boolean isMenuShareEnabled;
    private boolean isRequestIDSame = false;
    private boolean isUnKnownNumberCall = false;
    private final int[] itemNum = new int[]{1, 1};
    private boolean loadingCallLog = true;
    private View mActionBarGradientView;
    private CallLogDetailFragment mCallLogDetailFragment;
    private Contact mContactData;
    private ContactInfoHelper mContactInfoHelper;
    private ViewGroup mContainer;
    private String mCountryIso = null;
    private ContactDetailFragment mDetailFragment;
    private ViewPager mDetailPager;
    private boolean mDetailsLaunchedBeforeContactSaved = false;
    private boolean mFlagStarClicked = false;
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (ContactInfoFragment.this.isAdded()) {
                switch (msg.what) {
                    case 2:
                        ContactInfoFragment.this.mHasClicked = false;
                        break;
                    case 257:
                        if (ContactInfoFragment.this.mQRcodeIndication != null) {
                            ContactInfoFragment.this.buildQRCode();
                            break;
                        } else {
                            ContactInfoFragment.this.bindQRCodeGeneratFail();
                            break;
                        }
                    case 258:
                        ContactInfoFragment.this.bindQRCodeGenerat();
                        break;
                    case 259:
                        ContactInfoFragment.this.bindQRCodeGeneratFail();
                        break;
                    case 260:
                        ContactInfoFragment.this.mLoaderFragment.loadUri((Uri) msg.obj, false, ContactInfoFragment.this.getLoaderFragment());
                        break;
                    case 261:
                        ContactInfoFragment.this.bindHeaderDataWithOnlyNumber();
                        break;
                    case 1003:
                        ContactInfoFragment.this.registerMotionRecognition();
                        break;
                    case 1004:
                        ContactInfoFragment.this.setHeaderNumberMarkInfo(msg.getData().getString("displayName"), msg.getData().getString("displayCompany"), msg.getData().getString("originCompany"), msg.arg1);
                        break;
                    case 1005:
                        CalllogsUpdateCallBack mUpdateCallback = msg.obj;
                        if (mUpdateCallback != null) {
                            mUpdateCallback.onUpdateFinished(true);
                            break;
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        }
    };
    private boolean mHasCalllogConInfo;
    private boolean mHasClicked = false;
    private boolean mHasDataStatInfo;
    private boolean mHasLog;
    private boolean mHasMmsConInfo;
    private HwCustContactDetailActivity mHwCust = null;
    private boolean mIsCheckPermission;
    protected boolean mIsFailedSetContactData = false;
    private boolean mIsFromDialer = false;
    private boolean mIsFromDialpadOrDialer;
    private boolean mIsFromProfileCard = false;
    private boolean mIsInMultiWindowMode;
    private boolean mIsLaunchedFromContacts = true;
    private boolean mIsMotionRecognitionStarted;
    private boolean mIsNeedUpdateWindows;
    private boolean mIsNoNamedContact = false;
    private boolean mIsNotAnimator;
    private boolean mIsReadOnlyContacts = false;
    private boolean mIsSplitMenuContainerLoaded;
    private ContactLoaderFragment mLoaderFragment;
    private final ContactLoaderFragmentListener mLoaderFragmentListener = new ContactLoaderFragmentListener() {
        public void onContactNotFound() {
            if (ContactInfoFragment.this.isAdded()) {
                if (HwLog.HWDBG) {
                    HwLog.d("ContactInfoFragment", "Contacts Data not found or Data error. Finishing activity");
                }
                if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
                    ContactInfoFragment.this.getActivity().finish();
                }
            }
        }

        public void onDetailsLoaded(Contact result) {
            if (!ContactInfoFragment.this.isAdded()) {
                return;
            }
            if (result == null) {
                if (!(ContactInfoFragment.this.mDetailFragment == null || ContactInfoFragment.this.mDetailFragment.getContactDetailLayoutController() == null)) {
                    ContactInfoFragment.this.mDetailFragment.getContactDetailLayoutController().showEmptyState();
                }
                return;
            }
            ContactInfoFragment.this.mContactData = result;
            if (!ContactInfoFragment.this.mIsCheckPermission && ContactInfoFragment.this.mSavedState == null) {
                ContactInfoFragment.this.checkPermisionAndQueryIfHasMmsInfo();
            }
            if (ContactInfoFragment.this.mContactData.getLookupKey() != null && ContactInfoFragment.this.getIntent().getBooleanExtra("intent_key_is_from_dialpad", false)) {
                ContactInfoFragment.this.updateCallLogCacheData(ContactInfoFragment.this.getIntent().getStringExtra("phone"));
            }
            ContactInfoFragment.this.mLookupUri = result.getLookupUri();
            Intent lIntent = ContactInfoFragment.this.getIntent();
            if (!(ContactInfoFragment.this.mLookupUri == null || lIntent == null || lIntent.getData() != null)) {
                if (HwLog.HWDBG) {
                    HwLog.d("ContactInfoFragment", "setting data to the intent!!! mLookupUri :: " + ContactInfoFragment.this.mLookupUri);
                }
                ContactInfoFragment.this.getIntent().setData(ContactInfoFragment.this.mLookupUri);
            }
            loadContactData();
            if (result.isUserProfile()) {
                Intent intent = new Intent("com.android.huawei.profile_exists");
                intent.putExtra("profile_exists", true);
                ContactInfoFragment.this.getActivity().sendBroadcast(intent);
            }
        }

        private void loadContactData() {
            if (HwLog.HWDBG) {
                HwLog.d("ContactInfoFragment", "loadContactData");
            }
            if (ContactInfoFragment.this.mResetFlag) {
                ContactInfoFragment.this.updateHeaderView();
            } else {
                ContactInfoFragment.this.mResetFlag = true;
            }
            if (ContactInfoFragment.this.mDetailFragment != null) {
                ContactInfoFragment.this.mDetailFragment.setContactData(ContactInfoFragment.this.mContactData);
            } else {
                ContactInfoFragment.this.mIsFailedSetContactData = true;
            }
            if (ContactInfoFragment.this.misdismissed) {
                ContactInfoFragment.this.misdismissed = false;
                ContactInfoFragment.this.mHandler.post(new Runnable() {
                    public void run() {
                        ContactInfoFragment.this.mLoaderFragment.doCopyContact();
                    }
                });
            }
        }

        public void onEditRequested(Uri contactLookupUri) {
            if (ContactInfoFragment.this.isAdded()) {
                Intent intent = new Intent("android.intent.action.EDIT", contactLookupUri);
                intent.setComponent(new ComponentName(ContactInfoFragment.this.getContext(), ContactEditorActivity.class));
                intent.putExtra("finishActivityOnSaveCompleted", true);
                intent.putExtra("isFromDetailActivity", true);
                Drawable bitmap = ContactInfoFragment.this.mStaticPhotoContainer.getDrawable();
                if (!(ContactInfoFragment.this.mContactData == null || ContactInfoFragment.this.mContactData.mPhotoBinaryData == null || bitmap == null)) {
                    if (bitmap instanceof BitmapDrawable) {
                        ContactInfoFragment.this.mContactData.setBitmap(((BitmapDrawable) bitmap).getBitmap());
                    } else if (bitmap instanceof RoundedBitmapDrawable) {
                        ContactInfoFragment.this.mContactData.setBitmap(((RoundedBitmapDrawable) bitmap).getBitmap());
                    }
                }
                ContactsApplication app = (ContactsApplication) ContactInfoFragment.this.getApplication();
                long reqId = System.currentTimeMillis();
                app.setContactResultForDetail(ContactInfoFragment.this.mContactData, reqId);
                intent.putExtra("requestid", reqId);
                if (ContactInfoFragment.this.mHwCust != null) {
                    ContactInfoFragment.this.mHwCust.checkAndAddIntentExtra(intent, ContactInfoFragment.this.mContactData, ContactInfoFragment.this.getApplicationContext());
                }
                ContactInfoFragment.this.startActivityForResult(intent, 102);
                if (ContactInfoFragment.this.menuEdit != null) {
                    ContactInfoFragment.this.menuEdit.setEnabled(false);
                }
                if (ContactInfoFragment.this.mSplitMenuEdit != null) {
                    ContactInfoFragment.this.mSplitMenuEdit.setEnabled(false);
                }
            }
        }

        public void onDeleteRequested(Uri contactUri) {
            if (!ContactInfoFragment.this.isAdded()) {
                return;
            }
            if (QueryUtil.isHAPProviderInstalled() || ContactInfoFragment.this.mContactData == null || !ContactInfoFragment.this.mContactData.isSimContact()) {
                if (ContactInfoFragment.this.mContactData != null && ContactInfoFragment.this.mContactData.isUserProfile()) {
                    ContactDeletionInteraction.isUserProfile(true);
                    if (ContactInfoFragment.mProfileListener != null) {
                        ContactInfoFragment.mProfileListener.deleteProfile();
                    }
                }
                if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                    ContactInfoFragment.this.handleDeletionInteraction(contactUri);
                } else {
                    ContactDeletionInteraction.start(ContactInfoFragment.this.getActivity(), contactUri, true, true);
                }
                return;
            }
            Toast.makeText(ContactInfoFragment.this.getContext(), R.string.msg_sim_not_deletable_Toast, 0).show();
        }
    };
    private Uri mLookupUri;
    MotionRecognition mMReco = null;
    private MaterialColorMapUtils mMaterialColorMapUtils;
    private final ContentObserver mMmsObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange) {
            if (ContactInfoFragment.this.getActivity() == null || ContactInfoFragment.this.getActivity().checkSelfPermission("android.permission.READ_SMS") == 0) {
                ContactInfoFragment.this.asyncQueryIfHasMmsInfo();
            }
        }
    };
    private String mName;
    private AutoEllipseTextView mNameTextView;
    private int mNoNameCallPresentation = 0;
    private ArrayList<String> mNumberList = new ArrayList();
    private NumberMarkObserver mNumberMarkObserver = null;
    private int mNumberType = 0;
    private boolean mOnQueryPhoneNumberCompletedCalled = false;
    private OnStarBtnClickedInterface mOnStarBtnClickedInterface = null;
    private Menu mOptionsMenu = null;
    private boolean mOptionsMenuCanCreateShortcut;
    private boolean mOptionsMenuEditable;
    private boolean mOptionsMenuOptions;
    private HashMap<String, String> mPhoneNumCountryISO = new HashMap();
    private PhoneNumberHelper mPhoneNumberHelper;
    private ArrayList<String> mPhoneNumberList = new ArrayList();
    private Bitmap mPhotoBitmap = null;
    private byte[] mPhotoData = null;
    private final ContactDetailPhotoSetter mPhotoSetter = new ContactDetailPhotoSetter();
    protected PopupMenu mPopupMenu;
    private String mPostDialDigits = "";
    private SharedPreferences mPrefs;
    private String mPrimaryNum;
    private View mQRCodeContainer;
    private ImageView mQRCodeImage;
    private TextView mQRCodeTextView;
    private HashMap<String, ArrayList<String>> mQRcodeDataInfo;
    private AlertDialog mQRcodeDialog = null;
    private ImageView mQRcodeIndication;
    private Bitmap mQrCodeBitmap;
    private RcsContactDetailActivityHelp mRcsCust = null;
    private long mReqId = 0;
    private boolean mResetFlag = true;
    private View mRootView;
    private Bundle mSavedState;
    private MultiShrinkScroller mScroller;
    private boolean mSendToVoicemailState;
    private Button mShareButton;
    private View mSingleViewContainer;
    private ImageView mSmallPhoto;
    private ViewGroup mSplitMenuContainer;
    private View mSplitMenuCreate = null;
    private View mSplitMenuDelLog = null;
    private View mSplitMenuEdit = null;
    private final List<View> mSplitMenuGroup = new ArrayList();
    private View mSplitMenuMenu = null;
    private View mSplitMenuSave = null;
    private View mSplitMenuStar = null;
    private boolean mStartProfile = false;
    private ImageView mStaticPhotoContainer;
    private SubTabFragmentPagerAdapter mSubTabFragmentPagerAdapter;
    private SubTabWidget mSubTabWidget;
    private int mTabPagerIndex;
    private View mTitleGradientView;
    private Context mTotalContext;
    private MenuItem menuContactShare;
    private MenuItem menuCreateContact = null;
    private MenuItem menuDelLog = null;
    private ImageView menuDelLogImageView;
    private TextView menuDelLogTextView;
    private MenuItem menuEdit = null;
    private MenuItem menuMenu = null;
    private MenuItem menuProfileShare;
    private MenuItem menuSaveContact = null;
    private MenuItem menuStar = null;
    private boolean misdismissed = false;
    private DialogInterface.OnClickListener shareItemSelectListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    ContactInfoFragment.this.shareBusinessCard();
                    return;
                case 1:
                    ContactInfoFragment.this.shareVcard();
                    return;
                case 2:
                    ContactInfoFragment.this.shareTextCard();
                    return;
                default:
                    return;
            }
        }
    };

    private class ContactInfoTask extends AsyncTask<Void, Void, ContactInfo> {
        Context lContext;
        String lFromatNumber;
        Intent lIntent;
        String lPrimaryNum;

        public ContactInfoTask(Context context, String formatNum, String geo, String primaryNum, Intent intent) {
            this.lFromatNumber = formatNum;
            this.lPrimaryNum = primaryNum;
            this.lContext = context;
            this.lIntent = intent;
        }

        protected void onPostExecute(ContactInfo info) {
            super.onPostExecute(info);
            if (ContactInfoFragment.this.isAdded() && this.lContext != null && this.lIntent != null) {
                String lDisplayName;
                if (ContactInfoFragment.this.mRcsCust != null && ContactInfoFragment.this.mRcsCust.isFromRcsGroupChat(this.lIntent)) {
                    lDisplayName = ContactInfoFragment.this.mRcsCust.getRcsGroupChatNickname(this.lIntent);
                    ContactInfoFragment.this.mName = lDisplayName;
                } else if (info == null) {
                    lDisplayName = ContactInfoFragment.getDisplayName(this.lContext, this.lFromatNumber, ContactInfoFragment.this.mNoNameCallPresentation, ContactInfoFragment.this.mPhoneNumberHelper);
                } else {
                    lDisplayName = info.name;
                    if (TextUtils.isEmpty(lDisplayName)) {
                        lDisplayName = ContactInfoFragment.getDisplayName(this.lContext, this.lFromatNumber, ContactInfoFragment.this.mNoNameCallPresentation, ContactInfoFragment.this.mPhoneNumberHelper);
                        if (ContactInfoFragment.this.mHwCust != null) {
                            lDisplayName = ContactInfoFragment.this.mHwCust.getCnapNameExtraFromIntent(ContactInfoFragment.this.getIntent(), lDisplayName);
                        }
                        if (TextUtils.isEmpty(lDisplayName)) {
                            lDisplayName = this.lFromatNumber;
                        }
                    } else {
                        ContactInfoFragment.this.mName = lDisplayName;
                    }
                }
                String markInfo = this.lIntent.getStringExtra("EXTRA_CALL_LOG_MARKINFO");
                String markLabel = this.lIntent.getStringExtra("EXTRA_CALL_LOG_MARKLABEL");
                markInfo = ContactInfoFragment.this.checkNumFromBlackList(this.lFromatNumber, markInfo);
                Context context = this.lContext;
                TextView -get15 = ContactInfoFragment.this.mNameTextView;
                if (!TextUtils.isEmpty(markLabel)) {
                    lDisplayName = markLabel;
                }
                TextView textView = (TextView) ContactInfoFragment.this.getView().findViewById(R.id.company);
                if (TextUtils.isEmpty(markInfo)) {
                    markInfo = null;
                }
                ContactDetailDisplayUtils.setContactDisplayInfo(context, -get15, lDisplayName, textView, markInfo);
                if (ContactInfoFragment.this.mScroller != null) {
                    ContactInfoFragment.this.mScroller.resetAndUpdateViewVisibleState();
                }
            }
        }

        protected ContactInfo doInBackground(Void... params) {
            ContactInfo lookupNumber;
            if (ContactInfoFragment.this.mPhoneNumberHelper.canPlaceCallsTo(this.lPrimaryNum, ContactInfoFragment.this.mNoNameCallPresentation)) {
                lookupNumber = ContactInfoFragment.this.mContactInfoHelper.lookupNumber(this.lPrimaryNum, ContactInfoFragment.this.mCountryIso);
            } else {
                lookupNumber = null;
            }
            if (this.lIntent != null) {
                String markInfo = this.lIntent.getStringExtra("EXTRA_CALL_LOG_MARKINFO");
                if (ContactInfoFragment.this.isFromRcsGroupChat() || markInfo != null) {
                    String markNum = this.lIntent.getStringExtra("EXTRA_CALL_LOG_NUMBER");
                    String geo = ContactInfoFragment.this.getIntent().getStringExtra("EXTRA_CALL_LOG_GEO");
                    String formatNum = this.lIntent.getStringExtra("EXTRA_CALL_LOG_FORMATTED_NUM");
                    if (!(markNum == null || this.lContext == null)) {
                        Object[] result = ContactInfoFragment.this.queryNumMarkInfo(this.lContext, markNum, geo, formatNum);
                        String displayName = result[0];
                        String displayCompany = result[1];
                        if (((Integer) result[2]).intValue() == 2) {
                            this.lIntent.putExtra("EXTRA_CALL_LOG_MARKINFO", "");
                        } else {
                            this.lIntent.putExtra("EXTRA_CALL_LOG_MARKINFO", displayCompany);
                        }
                        this.lIntent.putExtra("EXTRA_CALL_LOG_MARKLABEL", displayName);
                    }
                }
            }
            return lookupNumber;
        }
    }

    class EraseContactTaskByLookUpUri implements Runnable {
        private Context mContext;
        private Contact mEraseContactData;
        private Handler mHandler;
        private int mNumberType;
        private String mPrimaryNum;
        private CalllogsUpdateCallBack mUpdateCallback;

        public EraseContactTaskByLookUpUri(Context context, Contact contactData, int numberType, String primaryNum, CalllogsUpdateCallBack updateCallback, Handler mHandler) {
            this.mEraseContactData = contactData;
            this.mContext = context;
            this.mNumberType = numberType;
            this.mPrimaryNum = primaryNum;
            this.mUpdateCallback = updateCallback;
            this.mHandler = mHandler;
        }

        public void run() {
            if (this.mEraseContactData != null) {
                EraseContactMarkUtils.eraseContactTimes(this.mContext, this.mEraseContactData.getId());
            }
            if (EmuiFeatureManager.isRcsFeatureEnable()) {
                if (ContactInfoFragment.this.mIsNoNamedContact) {
                    RcsContactsUtils.deleteRcsCache(this.mContext, this.mPrimaryNum);
                } else {
                    String[] phoneNumbers = new String[ContactInfoFragment.this.mNumberList.size()];
                    ContactInfoFragment.this.mNumberList.toArray(phoneNumbers);
                    RcsContactsUtils.deleteRcsCache(this.mContext, phoneNumbers);
                }
            }
            ContactInfoFragment.eraseMmsInfo(this.mContext, this.mEraseContactData, this.mNumberType, this.mPrimaryNum);
            if (this.mUpdateCallback != null) {
                Message msg = Message.obtain();
                msg.what = 1005;
                msg.obj = this.mUpdateCallback;
                if (this.mHandler.hasMessages(1005)) {
                    this.mHandler.removeMessages(1005);
                }
                this.mHandler.sendMessage(msg);
            }
        }
    }

    class GetContactTaskByCallLogUri extends AsyncTask<Void, Void, Void> {
        private Uri mUri;

        public GetContactTaskByCallLogUri(Uri uri) {
            this.mUri = uri;
        }

        public Void doInBackground(Void... params) {
            Cursor callCursor = ContactInfoFragment.this.getContext().getContentResolver().query(this.mUri, CallLogQuery.getProjection(), null, null, null);
            if (callCursor != null) {
                Void voidR;
                try {
                    if (callCursor.moveToNext()) {
                        String lookupUriString = callCursor.getString(11);
                        if (lookupUriString != null) {
                            ContactInfoFragment.this.mLoaderFragment.loadUri(Uri.parse(lookupUriString), false, ContactInfoFragment.this);
                            voidR = null;
                            return voidR;
                        }
                        boolean z;
                        Intent intent = ContactInfoFragment.this.getIntent();
                        if (ContactInfoFragment.this.mHwCust != null) {
                            ContactInfoFragment.this.mHwCust.putNameExtraToIntent(intent, callCursor);
                        }
                        intent.putExtra("EXTRA_CALL_LOG_NONAME_CALL", true);
                        ContactInfoFragment.this.mIsFromDialer = true;
                        intent.putExtra("INTENT_FROM_DIALER", true);
                        ContactInfoFragment.this.mIsNoNamedContact = true;
                        ContactInfoFragment.this.mPrimaryNum = callCursor.getString(1);
                        intent.putExtra("EXTRA_CALL_LOG_NUMBER", ContactInfoFragment.this.mPrimaryNum);
                        if (CompatUtils.isNCompatible()) {
                            ContactInfoFragment.this.mPostDialDigits = callCursor.getString(CallLogQuery.POST_DIAL_DIGITS);
                            intent.putExtra("EXTRA_CALL_LOG_POST_DIAL_DIGITS", ContactInfoFragment.this.mPostDialDigits);
                        }
                        ContactInfoFragment.this.mNoNameCallPresentation = callCursor.getInt(17);
                        intent.putExtra("EXTRA_CALL_LOG_PRESENTATION", ContactInfoFragment.this.mNoNameCallPresentation);
                        if (!ContactInfoFragment.this.mIsLaunchedFromContacts) {
                            ContactInfoFragment.this.isUnKnownNumberCall = !ContactsUtils.isNumberDialable(ContactInfoFragment.this.mPrimaryNum, ContactInfoFragment.this.mNoNameCallPresentation);
                        }
                        String geo = callCursor.getString(7);
                        if (geo != null) {
                            intent.putExtra("EXTRA_CALL_LOG_GEO", geo);
                        }
                        String countryIso = callCursor.getString(5);
                        if (countryIso != null) {
                            intent.putExtra("EXTRA_CALL_LOG_COUNTRY_ISO", countryIso);
                        }
                        String formattedNum = callCursor.getString(15);
                        if (formattedNum == null) {
                            formattedNum = ContactsUtils.formatPhoneNumber(ContactInfoFragment.this.mPrimaryNum, null, ContactsUtils.getCurrentCountryIso(ContactInfoFragment.this.getApplicationContext()), ContactInfoFragment.this.getApplicationContext());
                        }
                        intent.putExtra("EXTRA_CALL_LOG_FORMATTED_NUM", formattedNum);
                        String -get22 = ContactInfoFragment.this.mPrimaryNum;
                        String string = callCursor.getString(CallLogQuery.getMarkContentColumnIndex());
                        String string2 = callCursor.getString(CallLogQuery.getMarkTypeColumnIndex());
                        int i = callCursor.getInt(CallLogQuery.getMarkCountColumnIndex());
                        if (callCursor.getInt(CallLogQuery.getIsCloudMarkColumnIndex()) == 1) {
                            z = true;
                        } else {
                            z = false;
                        }
                        NumberMarkInfo markInfo = new NumberMarkInfo(-get22, string, string2, i, z);
                        if (!TextUtils.isEmpty(NumberMarkUtil.getMarkLabel(ContactInfoFragment.this.getApplicationContext(), markInfo))) {
                            intent.putExtra("EXTRA_CALL_LOG_MARKINFO", markInfo);
                        }
                        intent.putExtra("EXTRA_IGNORE_INCOMING_CALLLOG_IDS", true);
                    }
                    callCursor.close();
                } catch (IllegalArgumentException e) {
                    voidR = "ContactInfoFragment";
                    Log.w(voidR, "invalid URI starting call details", e);
                } finally {
                    callCursor.close();
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            int i = 1;
            super.onPostExecute(result);
            if (ContactInfoFragment.this.isAdded()) {
                if (ContactInfoFragment.this.mIsNoNamedContact) {
                    if (ContactInfoFragment.this.mIsLaunchedFromContacts) {
                        ContactInfoFragment.this.bindHeaderDataWithOnlyNumber();
                        ContactInfoFragment.this.mNumberType = 1;
                    } else {
                        ContactInfoFragment contactInfoFragment = ContactInfoFragment.this;
                        if (ContactInfoFragment.this.isUnKnownNumberCall) {
                            i = 5;
                        }
                        contactInfoFragment.mNumberType = i;
                        ContactInfoFragment.this.setupCallLogDetailFragment();
                        ContactInfoFragment.this.bindHeaderDataWithOnlyNumber();
                        if (ContactInfoFragment.this.isShowSplitMenu()) {
                            ContactInfoFragment.this.changeCustomMenuGroup(ContactInfoFragment.this.getCurrentSelected());
                        } else {
                            ContactInfoFragment.this.changeMenuGroup(ContactInfoFragment.this.getCurrentSelected());
                        }
                    }
                } else if (ContactInfoFragment.this.mIsFromProfileCard || ContactInfoFragment.this.mStartProfile) {
                    ContactInfoFragment.this.setupProfileDetailFragment();
                } else {
                    ContactInfoFragment.this.initializeTabSpecs(ContactInfoFragment.this.getContext(), ContactInfoFragment.this.mSavedState);
                }
            }
        }
    }

    public class NumberMarkObserver extends ContentObserver {
        private Context mContext;

        public NumberMarkObserver(Handler handler, Context context) {
            super(handler);
            this.mContext = context;
        }

        public void onChange(boolean selfChange) {
            if (ContactInfoFragment.this.isAdded()) {
                Intent lIntent = ContactInfoFragment.this.getIntent();
                if (lIntent != null) {
                    String markNum = lIntent.getStringExtra("EXTRA_CALL_LOG_NUMBER");
                    String geo = lIntent.getStringExtra("EXTRA_CALL_LOG_GEO");
                    String formatNum = lIntent.getStringExtra("EXTRA_CALL_LOG_FORMATTED_NUM");
                    if (!(markNum == null || this.mContext == null)) {
                        ContactInfoFragment.this.queryNumMarkInfoInNewThread(this.mContext, markNum, geo, formatNum);
                    }
                }
            }
        }
    }

    public interface OnStarBtnClickedInterface {
        void onStarBtnClicked();
    }

    public class SubTabAdapter extends SubTabFragmentPagerAdapter {
        public SubTabAdapter(FragmentManager fm, Context context, ViewPager pager, SubTabWidget subTabWidget) {
            super(fm, context, pager, subTabWidget);
        }

        public void onSubTabSelected(SubTab subTab, FragmentTransaction ft) {
            super.onSubTabSelected(subTab, ft);
            if (ContactInfoFragment.this.isShowSplitMenu()) {
                ContactInfoFragment.this.changeCustomMenuGroup(subTab.getPosition());
            } else {
                ContactInfoFragment.this.changeMenuGroup(subTab.getPosition());
            }
        }

        public void onPageSelected(int position) {
            int i;
            ContactInfoFragment.this.mTabPagerIndex = position;
            super.onPageSelected(position);
            if (ContactInfoFragment.this.isShowSplitMenu()) {
                ContactInfoFragment.this.changeCustomMenuGroup(position);
            } else {
                ContactInfoFragment.this.changeMenuGroup(position);
            }
            if (position == 0) {
                i = 5000;
            } else {
                i = 5002;
            }
            StatisticalHelper.report(i);
        }
    }

    public ContactDetailFragment getContactNewDetailFragment() {
        return this.mDetailFragment;
    }

    public void setOnStarBtnClickedInterface(OnStarBtnClickedInterface mOnStarBtnClickedInterface) {
        this.mOnStarBtnClickedInterface = mOnStarBtnClickedInterface;
    }

    public static void setProfileListener(ProfileListener listener) {
        mProfileListener = listener;
    }

    public CallLogDetailFragment getCallLogDetailFragment() {
        return this.mCallLogDetailFragment;
    }

    public boolean isIsNoNamedContact() {
        return this.mIsNoNamedContact;
    }

    public RcsContactDetailActivityHelp getRcsCust() {
        return this.mRcsCust;
    }

    public boolean isProfileContact() {
        return !this.mIsFromProfileCard ? this.mStartProfile : true;
    }

    public void onDetach() {
        if (this.mQrCodeBitmap != null) {
            this.mQrCodeBitmap.recycle();
        }
        super.onDetach();
    }

    public void onCreate(Bundle savedState) {
        boolean z;
        boolean z2 = false;
        super.onCreate(savedState);
        CommonConstants.setSimplifiedModeEnabled(CommonUtilMethods.isSimpleModeOn());
        Intent intent = getIntent();
        if (intent != null) {
            this.mIsNoNamedContact = intent.getBooleanExtra("EXTRA_CALL_LOG_NONAME_CALL", false);
            this.mIsFromDialer = intent.getBooleanExtra("INTENT_FROM_DIALER", false);
            this.mIsLaunchedFromContacts = intent.getBooleanExtra("IS_INTENT_EXTRA_FROM_CONTACTS_APP", true);
            this.mIsFromProfileCard = intent.getBooleanExtra("from_profile_card", false);
        }
        if (savedState != null) {
            this.mIsNoNamedContact = savedState.getBoolean("is_no_named_contact", false);
            this.isMenuDelLogEnabled = savedState.getBoolean("is_menu_delLog_enabled", false);
            this.mHasDataStatInfo = savedState.getBoolean("has_data_stat_info");
            this.mHasCalllogConInfo = savedState.getBoolean("has_call_log_con_info");
            this.mHasMmsConInfo = savedState.getBoolean("has_mms_con_info");
            this.isMenuShareEnabled = savedState.getBoolean("is_menu_share_enabled", false);
        }
        this.mPrefs = SharePreferenceUtil.getDefaultSp_de(getContext());
        if (Process.myPid() != this.mPrefs.getInt("last_process_id_contact_detail", -1)) {
            savedState = null;
            this.mPrefs.edit().putInt("last_process_id_contact_detail", Process.myPid()).apply();
        }
        this.mSavedState = savedState;
        PLog.d(0, "ContactDetailActivity onCreate begin");
        ExceptionCapture.reportScene(5);
        this.mMaterialColorMapUtils = new MaterialColorMapUtils(getResources());
        this.hasInitViews = false;
        this.isFromCreate = true;
        if (intent != null) {
            this.isLaunchedFromDialpad = intent.getBooleanExtra("intent_key_is_from_dialpad", false);
            this.mPrimaryNum = intent.getStringExtra("EXTRA_CALL_LOG_NUMBER");
            if (CompatUtils.isNCompatible()) {
                this.mPostDialDigits = intent.getStringExtra("EXTRA_CALL_LOG_POST_DIAL_DIGITS");
            }
            this.mNoNameCallPresentation = intent.getIntExtra("EXTRA_CALL_LOG_PRESENTATION", 1);
            this.mIsFromDialer = intent.getBooleanExtra("INTENT_FROM_DIALER", false);
            this.mCountryIso = intent.getStringExtra("EXTRA_CALL_LOG_COUNTRY_ISO");
        }
        Uri tmpUri = getIntent().getData();
        if (tmpUri != null) {
            String directoryParam = tmpUri.getQueryParameter("directory");
            long directoryId = 0;
            try {
                directoryId = Long.parseLong(directoryParam);
            } catch (NumberFormatException e) {
                HwLog.e("ContactInfoFragment", "NumberFormatException e ,parse remote contact uri");
            }
            if (TextUtils.isEmpty(directoryParam) || directoryId <= 0) {
                z = false;
            } else {
                z = true;
            }
            this.isFromRemote = z;
            this.mStartProfile = ProfileUtils.isProfileUri(tmpUri);
        }
        if (this.mRcsCust == null) {
            this.mRcsCust = new RcsContactDetailActivityHelp();
        }
        if (savedState != null) {
            HAPSelectAccountDialogFragment fragment = (HAPSelectAccountDialogFragment) getChildFragmentManager().findFragmentByTag("accounts_tag");
            if (fragment != null) {
                fragment.dismiss();
                this.misdismissed = true;
            }
        } else if (intent != null && this.mRcsCust.isFromRcsGroupChat(intent)) {
            this.mRcsCust.initFromRcsGroupChat(intent);
            this.mIsNoNamedContact = true;
        }
        if (this.mIsFromDialer && this.mIsNoNamedContact) {
            if (ContactsUtils.isNumberDialable(this.mPrimaryNum, this.mNoNameCallPresentation)) {
                z = false;
            } else {
                z = true;
            }
            this.isUnKnownNumberCall = z;
        }
        if (intent != null && this.mRcsCust.isFromRcsGroupChat(intent)) {
            this.mPrimaryNum = this.mRcsCust.getRcsGroupChatAddress(intent);
            if (ContactsUtils.isNumberDialable(this.mPrimaryNum, this.mNoNameCallPresentation)) {
                z = false;
            } else {
                z = true;
            }
            this.isUnKnownNumberCall = z;
        }
        z = CommonUtilMethods.calcIfNeedSplitScreen() ? !this.isLaunchedFromDialpad ? this.mIsFromDialer : true : false;
        this.mIsFromDialpadOrDialer = z;
        if (this.mIsFromDialpadOrDialer) {
            z2 = ScreenUtils.isLandscape(getActivity());
        }
        this.mIsNeedUpdateWindows = z2;
        if (!isShowSplitMenu()) {
            setHasOptionsMenu(true);
            showSystemActionbar(getActivity());
        }
        if (this.mIsNoNamedContact && this.mSavedState == null) {
            checkPermisionAndQueryIfHasMmsInfo();
        }
        getContext().getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/"), true, this.mMmsObserver);
        this.mTotalContext = getActivity();
    }

    private void checkPermisionAndQueryIfHasMmsInfo() {
        if (getActivity() == null || getActivity().checkSelfPermission("android.permission.READ_SMS") == 0) {
            asyncQueryIfHasMmsInfo();
            return;
        }
        requestPermissions(new String[]{"android.permission.READ_SMS"}, 3);
        this.mIsCheckPermission = true;
    }

    private void asyncQueryIfHasMmsInfo() {
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                Set<String> numbers = new HashSet();
                numbers.add(PhoneNumberUtils.normalizeNumber(ContactInfoFragment.this.mPrimaryNum));
                if (ContactInfoFragment.this.mContactData != null) {
                    for (String num : ContactInfoFragment.this.mContactData.getAllFormattedPhoneNumbers()) {
                        numbers.add(PhoneNumberUtils.normalizeNumber(num));
                    }
                }
                numbers.remove("");
                Cursor cursor = null;
                boolean lHasMmsConInfo = false;
                for (String number : numbers) {
                    Builder mmsUri = Uri.parse("content://mms-sms/threadID").buildUpon();
                    mmsUri.appendQueryParameter("recipient", number);
                    mmsUri.appendQueryParameter("queryOnly", String.valueOf(true));
                    try {
                        cursor = ContactInfoFragment.this.getContext().getContentResolver().query(mmsUri.build(), new String[]{"_id"}, null, null, null);
                        lHasMmsConInfo = cursor != null && cursor.getCount() > 0;
                        if (lHasMmsConInfo) {
                            break;
                        } else if (cursor != null) {
                            cursor.close();
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                ContactInfoFragment.this.mHasMmsConInfo = lHasMmsConInfo;
                HwLog.i("ContactInfoFragment", "asyncQueryIfHasMmsInfo, has mms info:" + ContactInfoFragment.this.mHasMmsConInfo);
                if (ContactInfoFragment.this.getActivity() != null) {
                    ContactInfoFragment.this.getActivity().invalidateOptionsMenu();
                }
            }
        });
    }

    public Uri getLookupUri() {
        return this.mLookupUri;
    }

    public void setNoAnimator(boolean noShow) {
        this.mIsNotAnimator = noShow;
    }

    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        if (this.mIsNotAnimator && CommonUtilMethods.calcIfNeedSplitScreen()) {
            this.mIsNotAnimator = false;
            return null;
        }
        return ContactSplitUtils.createSplitAnimator(transit, enter, nextAnim, this.mRootView, R.drawable.multiselection_background, getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        if ((isSplitActivity(activity) && ContactSplitUtils.isSpiltTwoColumn(activity, activity.isInMultiWindowMode())) || this.mIsNeedUpdateWindows) {
            EmuiFeatureManager.setDetailHeaderAnimation(true);
            this.mRootView = ContactDetailLayoutCache.getCachedDetailsView(getContext(), R.layout.contact_detail_split_land_anim);
            return this.mRootView;
        }
        this.mRootView = ContactDetailLayoutCache.getCachedDetailsView(getContext(), R.layout.contact_detail_anim);
        return this.mRootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PLog.d(0, "ContactInfoFragment onActivityCreated begin");
        adjustLayout();
        this.hasPhotoChange = getIntent().getBooleanExtra("intent_key_has_photo", false);
        this.mReqId = getIntent().getLongExtra("requestid", -1);
        this.isRequestIDSame = ((ContactsApplication) getApplication()).isRequestIDSame(this.mReqId);
        Activity act = getActivity();
        if (!((isSplitActivity(act) ? ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode()) : false) || savedInstanceState == null)) {
            long lReqId = savedInstanceState.getLong("requestid", -1);
            if (lReqId != -1) {
                this.mReqId = lReqId;
            }
        }
        this.hasInitViews = false;
        if (this.mLoaderFragmentListener != null) {
            ContactsApplication lApp = (ContactsApplication) getApplication();
            Contact lContact = null;
            if (this.mReqId != -1) {
                lContact = lApp.getContact(this.mReqId);
            }
            if (lContact != null) {
                initViews();
                initializeTabSpecs(getContext(), this.mSavedState);
                this.mLoaderFragmentListener.onDetailsLoaded(lContact);
            }
            lApp.resetContact();
        }
        if (CommonUtilMethods.isSimpleModeOn()) {
            getActivity().setRequestedOrientation(1);
        }
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            if (getActivity() instanceof ContactDetailActivity) {
                this.mHwCust = (HwCustContactDetailActivity) HwCustUtils.createObj(HwCustContactDetailActivity.class, new Object[]{getActivity()});
            } else {
                this.mHwCust = (HwCustContactDetailActivity) HwCustUtils.createObj(HwCustContactDetailActivity.class, new Object[]{getApplicationContext()});
            }
        }
        if (this.mHwCust != null && this.mHwCust.supportReadOnly()) {
            this.mIsReadOnlyContacts = this.mHwCust.isReadOnlyContact(getIntent().getData());
        }
        if (this.mRcsCust == null) {
            this.mRcsCust = new RcsContactDetailActivityHelp();
        }
        if (!this.hasInitViews) {
            initViews();
            if (this.mIsNoNamedContact) {
                setupCallLogDetailFragment();
            } else if (this.mIsLaunchedFromContacts) {
                initializeTabSpecs(getContext(), this.mSavedState);
            }
        }
        if (this.mIsFromProfileCard || this.mStartProfile) {
            setupProfileDetailFragment();
        }
        createActivity(this.mSavedState != null);
        if (isShowSplitMenu()) {
            changeCustomMenuGroup(getCurrentSelected());
        } else {
            changeMenuGroup(getCurrentSelected());
        }
        if (this.mHwCust != null) {
            this.mHwCust.custOnCreate();
        }
        if (isShowSplitMenu()) {
            setToolbarMenuText();
        }
        this.mIsInMultiWindowMode = getActivity().isInMultiWindowMode();
        if (this.mSavedState != null && this.mSavedState.getBoolean("show_qrcode_dialog", false)) {
            int i;
            this.mQrCodeBitmap = (Bitmap) this.mSavedState.getParcelable("qrcode_bitmap");
            boolean isInMultiWindowModePrev = this.mSavedState.getBoolean("isInMultiWindowMode");
            bindQRCodeGenerat();
            ViewGroup viewGroup = this.mContainer;
            Runnable anonymousClass7 = new Runnable() {
                public void run() {
                    ContactInfoFragment.this.showQRcodeDialog();
                }
            };
            if (isInMultiWindowModePrev) {
                i = 200;
            } else {
                i = 0;
            }
            viewGroup.postDelayed(anonymousClass7, (long) i);
        }
        registNumberMarkObserver();
        PLog.d(0, "ContactInfoFragment onActivityCreated end");
        AlertDialogFragmet alertFragment = (AlertDialogFragmet) getFragmentManager().findFragmentByTag("AlertDialogFragmet");
        if (alertFragment != null && alertFragment.mAlertDialogType == 3) {
            alertFragment.mSetedProfileListener = this.shareItemSelectListener;
        }
    }

    private void registNumberMarkObserver() {
        this.mNumberMarkObserver = new NumberMarkObserver(this.mHandler, getApplicationContext());
        getContext().getContentResolver().registerContentObserver(NumberMark.CONTENT_URI, false, this.mNumberMarkObserver);
    }

    private void unregisterObserver(ContentObserver observer) {
        if (observer != null) {
            getContext().getContentResolver().unregisterContentObserver(observer);
        }
    }

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }

    private void addDetailAndCallFragment() {
        FragmentManager fm = getChildFragmentManager();
        this.mDetailFragment = (ContactDetailFragment) fm.findFragmentByTag(makeFragmentName(R.id.detail_tab_pager, 0));
        if (this.mDetailFragment == null) {
            this.mDetailFragment = new ContactDetailFragment();
        }
        initFragmentByAttashType(this.mDetailFragment);
        this.mCallLogDetailFragment = (CallLogDetailFragment) fm.findFragmentByTag(makeFragmentName(R.id.detail_tab_pager, 1));
        if (this.mCallLogDetailFragment == null) {
            this.mCallLogDetailFragment = new CallLogDetailFragment();
        }
        initFragmentByAttashType(this.mCallLogDetailFragment);
        setCalllogsUpdateListener();
    }

    private void addSingleFragment(String fragmentTag) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.call_log_detail_container, "Call_log_fragment_tag".equals(fragmentTag) ? this.mCallLogDetailFragment : this.mDetailFragment, fragmentTag);
        fragmentTransaction.commitAllowingStateLoss();
        this.mSingleViewContainer.setVisibility(0);
    }

    private void removeSingleFragment(String fragmentTag) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.remove("Call_log_fragment_tag".equals(fragmentTag) ? this.mCallLogDetailFragment : this.mDetailFragment);
        fragmentTransaction.commitAllowingStateLoss();
        this.mSingleViewContainer.setVisibility(8);
    }

    private void createActivity(boolean restart) {
        boolean isSpiltTwoColumn;
        Intent intent = getIntent();
        if (this.mIsNoNamedContact) {
            int i;
            bindHeaderDataWithOnlyNumber();
            if (this.isUnKnownNumberCall) {
                i = 5;
            } else {
                i = 1;
            }
            this.mNumberType = i;
        } else if (YellowPageContactUtil.isYellowPageUri(intent.getData())) {
            this.mNumberType = 3;
        } else if (this.isFromRemote) {
            this.mNumberType = 4;
        } else {
            this.mNumberType = 2;
        }
        FragmentManager fragmentMgr = getChildFragmentManager();
        String sLoaderFragmentTag = "loader_fragment";
        if (fragmentMgr.findFragmentByTag("loader_fragment") == null) {
            ContactLoaderFragment loaderFragment = new ContactLoaderFragment(getContext());
            loaderFragment.setLoadWhenActivityCreated(restart);
            this.mLoaderFragment = loaderFragment;
            fragmentMgr.beginTransaction().add(loaderFragment, "loader_fragment").commit();
        } else {
            this.mLoaderFragment = (ContactLoaderFragment) fragmentMgr.findFragmentByTag("loader_fragment");
            this.mLoaderFragment.setContext(getActivity());
        }
        this.mLoaderFragment.setListener(this.mLoaderFragmentListener);
        Activity act = getActivity();
        if (isSplitActivity(act)) {
            isSpiltTwoColumn = ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode());
        } else {
            isSpiltTwoColumn = false;
        }
        if (!isSpiltTwoColumn) {
            this.mLoaderFragment.setContact(this.mContactData);
        }
        if (!this.mIsNoNamedContact) {
            Uri uri = intent.getData();
            if (uri != null) {
                if ("call_log".equals(uri.getAuthority())) {
                    AsyncTaskExecutors.createThreadPoolExecutor().submit("GET_CALL_DETAIL", new GetContactTaskByCallLogUri(uri), new Void[0]);
                    return;
                }
            }
            this.mLoaderFragment.setContactLoadedListener(this.mDetailFragment);
            if (uri != null) {
                this.mLoaderFragment.loadUri(uri, false, (Fragment) this);
            }
        }
        if (this.isRequestIDSame) {
            saveContactFromEditIntent(getIntent());
        }
    }

    private void initViews() {
        initContainer();
        initHelper();
        initQRcodeIndicationer();
        initScroller();
        initGradientView();
        initBackView();
        if (isShowSplitMenu()) {
            initSplitMenuContainer();
            initSplitMenuGroup();
            setSplitToolbarMenuGroup();
        }
        this.hasInitViews = true;
    }

    private void initContainer() {
        this.mContainer = (ViewGroup) getView().findViewById(R.id.container);
        Activity act = getActivity();
        if (isSplitActivity(act) && ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode())) {
            updatePadding(getContext());
        }
        this.mSingleViewContainer = getView().findViewById(R.id.call_log_detail_container);
        this.mStaticPhotoContainer = (ImageView) getView().findViewById(R.id.static_photo_container);
        this.mStaticPhotoContainer.setVisibility(8);
        this.mNameTextView = (AutoEllipseTextView) getView().findViewById(R.id.name);
    }

    private void updatePadding(Context context) {
        if (this.mContainer != null) {
            int actionbarHeight = ContactDpiAdapter.getActionbarHeight(context);
            this.mContainer.setPadding(0, ContactDpiAdapter.getStatusBarHeight(context) + actionbarHeight, 0, 0);
        }
    }

    private void adjustMarginTop() {
        ViewGroup topBar = (ViewGroup) getView().findViewById(R.id.topbar);
        if (topBar != null) {
            LayoutParams params = (LayoutParams) topBar.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            params.addRule(14);
            params.addRule(10);
            topBar.setLayoutParams(params);
        }
    }

    private void initHelper() {
        String lDefaultCountryIso = GeoUtil.getCurrentCountryIso(getContext());
        this.mContactInfoHelper = new ContactInfoHelper(getContext(), lDefaultCountryIso);
        if (TextUtils.isEmpty(this.mCountryIso)) {
            this.mCountryIso = lDefaultCountryIso;
        }
        this.mPhoneNumberHelper = new PhoneNumberHelper(getResources());
    }

    private void initQRcodeIndicationer() {
        this.mQRcodeIndication = (ImageView) getView().findViewById(R.id.btnQRcode);
        this.mQRcodeIndication.setImageResource(getButtonQRcodeImageOrDefaultPhoto(2));
        Activity activity = getActivity();
        if (activity.isInMultiWindowMode()) {
            this.mQRCodeContainer = activity.getLayoutInflater().inflate(R.layout.qrcode_dialog_multimode, this.mContainer, false);
        } else {
            this.mQRCodeContainer = activity.getLayoutInflater().inflate(R.layout.qrcode_dialog, this.mContainer, false);
        }
        this.mQRCodeImage = (ImageView) this.mQRCodeContainer.findViewById(R.id.two_dimensional_code);
        this.mQRCodeTextView = (TextView) this.mQRCodeContainer.findViewById(R.id.contact_detail_qrcode_text);
        this.mShareButton = (Button) this.mQRCodeContainer.findViewById(R.id.contact_detail_qrcode_share);
        ViewUtil.setStateListIcon(getApplicationContext(), this.mQRcodeIndication, false);
        this.mQRcodeIndication.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ContactInfoFragment.this.isAdded()) {
                    ContactInfoFragment.this.showQRcodeDialog();
                    StatisticalHelper.report(2024);
                }
            }
        });
    }

    private void initScroller() {
        if (EmuiFeatureManager.isDetailHeaderAnimationFeatureEnable(getContext())) {
            try {
                this.mScroller = (MultiShrinkScroller) getView().findViewById(R.id.multiscroller);
            } catch (ClassCastException e) {
                Log.e("ContactInfoFragment", "bad R.id.multiscroller", e);
                getActivity().finish();
            }
            if (this.mScroller != null) {
                this.mScroller.setFragment(this);
                this.mScroller.initValue(getHeadMinHeight());
                this.mScroller.initView();
            }
        }
    }

    private void initGradientView() {
        this.mTitleGradientView = getView().findViewById(R.id.title_gradient);
        this.mActionBarGradientView = getView().findViewById(R.id.action_bar_gradient);
        if (getResources().getConfiguration().orientation != 1) {
            configureGradientViewHeights();
        }
    }

    private void initBackView() {
        Activity act = getActivity();
        ImageView backImage = (ImageView) getView().findViewById(R.id.backImg);
        Fragment fr = getParentFragment();
        FragmentManager childFragmentManager = fr == null ? null : fr.getChildFragmentManager();
        boolean temp = ((act instanceof PeopleActivity) && ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode())) ? childFragmentManager != null : false;
        boolean peopleActivityJudge;
        if (this.mSavedState != null) {
            if (temp && childFragmentManager != null) {
                peopleActivityJudge = childFragmentManager.getBackStackEntryCount() <= 2;
            }
            peopleActivityJudge = false;
        } else if (!temp || childFragmentManager == null) {
            peopleActivityJudge = false;
        } else {
            if (childFragmentManager.getBackStackEntryCount() + 1 <= 2) {
                peopleActivityJudge = true;
            }
            peopleActivityJudge = false;
        }
        boolean isSpiltTwoColumn;
        if (act instanceof VoiceSearchResultActivity) {
            isSpiltTwoColumn = ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode());
        } else {
            isSpiltTwoColumn = false;
        }
        if ((this.mIsNeedUpdateWindows || peopleActivityJudge || r5) && backImage != null) {
            backImage.setVisibility(8);
        }
        if (backImage != null) {
            backImage.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (ContactInfoFragment.this.getActivity() == null || ContactInfoFragment.this.isSplitActivity(ContactInfoFragment.this.getActivity())) {
                        ContactInfoFragment.this.onBackPressed();
                    } else {
                        ContactInfoFragment.this.getActivity().onNavigateUp();
                    }
                }
            });
        }
    }

    private void showQRcodeDialog() {
        if (this.mQrCodeBitmap == null) {
            HwLog.e("ContactInfoFragment", "showQRcodeDialog, mQrCodeBitmap is null");
            return;
        }
        boolean z;
        Button button = this.mShareButton;
        if (EmuiFeatureManager.isSuperSaverMode()) {
            z = false;
        } else {
            z = true;
        }
        button.setEnabled(z);
        if (this.mQRcodeDialog != null) {
            this.mQRcodeDialog.show();
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            HwLog.e("ContactInfoFragment", "showQRcodeDialog, activity is null");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(this.mQRCodeContainer);
        this.mQRcodeDialog = builder.create();
        this.mQRcodeDialog.show();
        this.mQRcodeDialog.setCanceledOnTouchOutside(true);
        if (ScreenUtils.isLandscape(activity)) {
            this.mQRCodeTextView.setMaxLines(1);
            this.mQRCodeTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        } else {
            this.mQRcodeDialog.getWindow().setGravity(80);
        }
        this.mShareButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                StatisticalHelper.report(2041);
                ContactInfoFragment.this.mQRcodeDialog.dismiss();
                ContactInfoFragment.this.shareBusinessCard();
            }
        });
    }

    public boolean isUnKnownNumberCall() {
        return this.isUnKnownNumberCall;
    }

    public boolean needShowDetailEntry() {
        return (this.mIsFromDialer || isFromRcsGroupChat()) ? this.mIsNoNamedContact : false;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contact_detail_option, menu);
        this.mOptionsMenu = menu;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        long duration = event.getEventTime() - event.getDownTime();
        if (this.mOptionsMenu == null || keyCode != 82 || duration >= ((long) ViewConfiguration.getLongPressTimeout())) {
            return false;
        }
        this.mOptionsMenu.performIdentifierAction(R.id.overflow_menu, 0);
        return true;
    }

    public void onSaveInstanceState(Bundle outState) {
        boolean z;
        boolean z2 = false;
        super.onSaveInstanceState(outState);
        outState.putInt("current_tab", getCurrentSelected());
        String str = "save_tab_succeed";
        if (this.mSubTabWidget != null) {
            z = true;
        } else {
            z = false;
        }
        outState.putBoolean(str, z);
        if (!(this.mDetailFragment == null || this.mDetailFragment.getContactDetailLayoutController() == null)) {
            this.mDetailFragment.getContactDetailLayoutController().onSaveInstanceState(outState);
        }
        Activity act = getActivity();
        if (isSplitActivity(act)) {
            z = ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode());
        } else {
            z = false;
        }
        if (!z) {
            ContactsApplication app = (ContactsApplication) getApplication();
            long reqId = System.currentTimeMillis();
            app.setContactResultForDetail(this.mContactData, reqId);
            outState.putLong("requestid", reqId);
        }
        boolean isQRcodeShow = false;
        if (this.mQRcodeDialog != null) {
            isQRcodeShow = this.mQRcodeDialog.isShowing();
        }
        outState.putBoolean("show_qrcode_dialog", isQRcodeShow);
        if (isQRcodeShow && this.mQrCodeBitmap != null) {
            outState.putParcelable("qrcode_bitmap", ContactsUtils.compressBitmap(this.mQrCodeBitmap, this.mQrCodeBitmap.getWidth() / 4, this.mQrCodeBitmap.getHeight() / 4));
            outState.putBoolean("isInMultiWindowMode", this.mIsInMultiWindowMode);
        }
        outState.putBoolean("is_no_named_contact", this.mIsNoNamedContact);
        outState.putBoolean("is_menu_delLog_enabled", this.isMenuDelLogEnabled);
        outState.putBoolean("has_data_stat_info", this.mHasDataStatInfo);
        outState.putBoolean("has_call_log_con_info", this.mHasCalllogConInfo);
        outState.putBoolean("has_mms_con_info", this.mHasMmsConInfo);
        if (!(this.mQrCodeBitmap == null || EmuiFeatureManager.isSuperSaverMode())) {
            z2 = true;
        }
        this.isMenuShareEnabled = z2;
        outState.putBoolean("is_menu_share_enabled", this.isMenuShareEnabled);
    }

    public void setIsFailedSetContactData(boolean isFail) {
        this.mIsFailedSetContactData = isFail;
    }

    private void updateHeaderView() {
        if (this.mStaticPhotoContainer == null) {
            this.mStaticPhotoContainer = (ImageView) getView().findViewById(R.id.static_photo_container);
        }
        if (this.mSmallPhoto == null) {
            this.mSmallPhoto = (ImageView) getView().findViewById(R.id.small_photo);
        }
        bindHeaderData();
    }

    public void onResume() {
        this.mHandler.sendEmptyMessageDelayed(1003, 600);
        if (this.mIsNeedUpdateWindows) {
            updateWindowsParams();
        }
        if (this.menuEdit != null) {
            this.menuEdit.setEnabled(true);
        }
        if (this.mSplitMenuEdit != null) {
            this.mSplitMenuEdit.setEnabled(true);
        }
        if (!(this.isFromCreate || this.mQRcodeDialog == null)) {
            this.mQRcodeDialog.dismiss();
        }
        super.onResume();
    }

    public void onStop() {
        closePopupMenu();
        ContactSaveService.unregisterListener(this);
        super.onStop();
    }

    public void onStart() {
        ContactSaveService.registerListener(this);
        super.onStart();
    }

    public void onPause() {
        super.onPause();
        this.isFromCreate = false;
        if (this.mHandler.hasMessages(1003)) {
            this.mHandler.removeMessages(1003);
        }
        if (this.mIsMotionRecognitionStarted && MotionRecognition.isMotionClassExists() && MotionRecognition.isMotionRecoApkExist(getContext())) {
            destroyMotionRecognition();
            this.mIsMotionRecognitionStarted = false;
        }
    }

    public void onDestroy() {
        ContactSaveService.unregisterListener(this);
        super.onDestroy();
        Activity act = getActivity();
        if (isSplitActivity(act) && ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode())) {
            ((ContactsApplication) getApplication()).getContactAndReset(0);
        }
        if (this.mCallLogDetailFragment != null) {
            this.mCallLogDetailFragment.setUpdateCalllogsListener(null);
        }
        if (this.mDetailFragment != null) {
            this.mDetailFragment.setQueryPhoneNumberListener(null);
        }
        if (this.mQRcodeDialog != null && this.mQRcodeDialog.isShowing()) {
            this.mQRcodeDialog.dismiss();
        }
        unregisterObserver(this.mMmsObserver);
        unregisterObserver(this.mNumberMarkObserver);
    }

    public void unJoinContacts() {
        if (isAdded()) {
            this.mLoaderFragment.initiateUnJoinContacts();
        }
    }

    private void changeMenuGroup(int tabPagerIndex) {
        switch (this.mNumberType) {
            case 1:
                setMenuVisibility(tabPagerIndex, this.menuMenu, this.menuSaveContact, this.menuCreateContact);
                return;
            case 2:
                setMenuVisibility(tabPagerIndex, this.menuEdit, this.menuMenu, this.menuStar);
                updateMenuDelLogState(tabPagerIndex);
                return;
            case 3:
            case 4:
                setMenuVisibility(tabPagerIndex, this.menuMenu);
                updateMenuDelLogState(tabPagerIndex);
                return;
            case 5:
                setMenuVisibility(tabPagerIndex, this.menuMenu, this.menuCreateContact, this.menuSaveContact);
                updateMenuDelLogState(tabPagerIndex);
                return;
            case 6:
                setMenuVisibility(tabPagerIndex, this.menuMenu, this.menuEdit);
                updateMenuDelLogState(tabPagerIndex);
                return;
            default:
                return;
        }
    }

    private void updateMenuDelLogState(int tabPagerIndex) {
        boolean z = true;
        if (tabPagerIndex == 1) {
            setMenuDelLogAvailablity();
        }
        if (this.menuDelLog != null) {
            MenuItem menuItem = this.menuDelLog;
            if (tabPagerIndex == 0) {
                z = false;
            }
            menuItem.setVisible(z);
        }
    }

    private void setMenuDelLogAvailablity() {
        if (this.menuDelLog != null || this.mSplitMenuDelLog != null) {
            if (this.mCallLogDetailFragment == null || this.mCallLogDetailFragment.getCallLogList() == null || this.mCallLogDetailFragment.getCallLogList().getVisibility() == 8 || this.loadingCallLog) {
                if (this.menuDelLog != null) {
                    this.menuDelLog.setEnabled(false);
                }
                setSplitMenuDelLogAvailablity(false);
            } else {
                if (this.menuDelLog != null) {
                    this.menuDelLog.setEnabled(true);
                }
                setSplitMenuDelLogAvailablity(true);
            }
        }
    }

    private void setMenuVisibility(int tabPagerIndex, MenuItem... menuViews) {
        for (MenuItem menuItem : menuViews) {
            if (menuItem != null) {
                boolean z;
                if (tabPagerIndex == 0) {
                    z = true;
                } else {
                    z = false;
                }
                menuItem.setVisible(z);
                if (HLUtils.isShowHotNumberOnTop) {
                    Uri hotnumberUri = HLUtils.getHotNumberKey();
                    if (hotnumberUri != null && hotnumberUri.equals(this.mLookupUri) && menuItem.equals(this.menuEdit)) {
                        menuItem.setVisible(false);
                    }
                }
            }
        }
    }

    private void saveContactFromEditIntent(Intent intent) {
        if (intent != null) {
            Intent lServiceIntent = (Intent) intent.getParcelableExtra("serviceIntent");
            if (lServiceIntent != null && "saveContact".equals(lServiceIntent.getAction())) {
                lServiceIntent.setClass(getContext(), ContactSaveService.class);
                getActivity().startService(lServiceIntent);
                this.mDetailsLaunchedBeforeContactSaved = true;
                this.mNumberType = 2;
            }
        }
    }

    public void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
        this.mResetFlag = true;
        this.isMenuShareEnabled = false;
        this.hasPhotoChange = false;
        if (aRequestCode == OfflineMapStatus.EXCEPTION_SDCARD) {
            this.mCallLogDetailFragment.setupCallRecords();
        }
        if (aData != null) {
            this.hasPhotoChange = aData.getBooleanExtra("intent_key_has_photo", false);
            HwLog.i("ContactInfoFragment", "hasPhotoChange = " + this.hasPhotoChange);
        }
        if (-1 != aResultCode) {
            HwLog.e("ContactInfoFragment", "onActivityResult result not ok for aResultCode:" + aResultCode);
            return;
        }
        switch (aRequestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY /*100*/:
                if (this.mHandler.hasMessages(261)) {
                    this.mHandler.removeMessages(261);
                }
                this.mHandler.sendEmptyMessageDelayed(261, 30);
                break;
            case 102:
                handleActivityResultFromEditor(aData);
                break;
            case MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER /*999*/:
                if (!(aData == null || this.mHwCust == null)) {
                    this.mHwCust.joinAggregate(aData);
                    break;
                }
            case 1000:
                this.mResetFlag = false;
                if (!(this.mDetailFragment == null || aData == null)) {
                    this.mDetailFragment.processActivityResult(aRequestCode, aResultCode, aData);
                }
                StatisticalHelper.report(4048);
                break;
        }
    }

    private void bindHeaderDataWithOnlyNumber() {
        if (HwLog.HWDBG) {
            HwLog.d("ContactInfoFragment", "bindHeaderDataWithOnlyNumber");
        }
        if (this.mStaticPhotoContainer == null) {
            this.mStaticPhotoContainer = (ImageView) getView().findViewById(R.id.static_photo_container);
        }
        if (this.mSmallPhoto == null) {
            this.mSmallPhoto = (ImageView) getView().findViewById(R.id.small_photo);
        }
        this.mSmallPhoto.setVisibility(8);
        this.mStaticPhotoContainer.setImageDrawable(new ColorDrawable(ContactPhotoManager.pickColor(this.mPrimaryNum, getResources())));
        this.mStaticPhotoContainer.setVisibility(0);
        if (this.mScroller != null) {
            updateStatusBarColor(-16777216);
            this.mScroller.setExsitBigPhoto(Boolean.valueOf(false));
        }
        Intent intent = getIntent();
        String geo = intent.getStringExtra("EXTRA_CALL_LOG_GEO");
        String number = this.mPrimaryNum;
        String formatNum = intent.getStringExtra("EXTRA_CALL_LOG_FORMATTED_NUM");
        if (isFromRcsGroupChat()) {
            number = ContactsUtils.removeDashesAndBlanks(number);
            formatNum = ContactsUtils.formatPhoneNumber(number, null, ContactsUtils.getCurrentCountryIso(getContext()), getContext());
            intent.putExtra("EXTRA_CALL_LOG_NUMBER", number);
            intent.putExtra("EXTRA_CALL_LOG_FORMATTED_NUM", formatNum);
        }
        ArrayList<String> aPhoneNumbers = new ArrayList();
        aPhoneNumbers.add(ContactsUtils.removeDashesAndBlanks(number));
        onQueryPhoneNumberCompleted(aPhoneNumbers);
        if (TextUtils.isEmpty(geo)) {
            if (NLUtils.handleWithChinaPhoneOrFixNumberLogic(formatNum)) {
                geo = GeoUtil.getGeocodedLocationFor(getApplicationContext(), formatNum);
            } else {
                String tmpNumber = DialerHighlighter.cleanNumber(formatNum, false);
                geo = NumberLocationCache.getLocation(tmpNumber);
                if (geo == null) {
                    geo = NumberLocationLoader.getAndUpdateGeoNumLocation(getApplicationContext(), tmpNumber);
                }
            }
        }
        buildNoNameDetailEntry(formatNum, geo);
        if (HwLog.HWDBG) {
            HwLog.d("ContactInfoFragment", "bindHeaderDataWithOnlyNumber formatNum");
        }
        AsyncTaskExecutors.createThreadPoolExecutor().submit("GET_CONTACT_INFO_DETAIL", new ContactInfoTask(getApplicationContext(), formatNum, geo, number, intent), new Void[0]);
    }

    private void buildNoNameDetailEntry(String formatNum, String geo) {
        this.entry = new DetailViewEntry();
        this.entry.id = 0;
        this.entry.mimetype = "vnd.android.cursor.item/phone_v2";
        this.entry.isFromNoNameCall = true;
        if (this.mHwCust == null || !this.mHwCust.isCustHideGeoInfo()) {
            this.entry.typeString = CallLogDetailHelper.getFinalGeoCode(geo, formatNum, this.mCountryIso, getApplicationContext());
        } else {
            this.entry.typeString = "";
        }
        if (EmuiFeatureManager.isChinaArea()) {
            this.entry.data = ContactsUtils.getChinaFormatNumber(formatNum);
        } else {
            this.entry.data = formatNum;
        }
        if (HwLog.HWDBG) {
            HwLog.d("ContactInfoFragment", "bindHeaderDataWithOnlyNumber formatNum");
        }
        this.entry.intent = CallUtil.getCallIntent(this.entry.data);
        this.entry.isPrimary = false;
    }

    public DetailViewEntry getNoNameDetailEntry() {
        return this.entry;
    }

    public static String getDisplayName(Context context, String number, int callPresentation, PhoneNumberHelper phoneNUmberHelper) {
        String nameText = number;
        if (context == null) {
            return number;
        }
        String predefinedName = ContactsUtils.getEmergencyOrHotlineName(context, number);
        if (!TextUtils.isEmpty(predefinedName)) {
            nameText = predefinedName;
        } else if (ContactsUtils.displayEmergencyNumber(context) && CommonUtilMethods.isEmergencyNumber(number, SimFactoryManager.isDualSim())) {
            nameText = context.getResources().getString(R.string.emergency_number);
        } else if (phoneNUmberHelper != null) {
            nameText = String.valueOf(phoneNUmberHelper.getDisplayNumber(number, callPresentation, "", ""));
            if (phoneNUmberHelper.canPlaceCallsTo(number, callPresentation)) {
                nameText = number;
            }
        }
        return nameText;
    }

    private void bindPhotoData() {
        if (this.mContactData != null) {
            PhotoClickListener listener;
            boolean photoLoaded = this.mContactData.mPhotoBinaryData != null;
            this.mStaticPhotoContainer.setVisibility(0);
            if (photoLoaded) {
                Bitmap aSource = BitmapFactory.decodeByteArray(this.mContactData.mPhotoBinaryData, 0, this.mContactData.mPhotoBinaryData.length);
                boolean hasBigPhoto = false;
                String photoUri = this.mContactData.getPhotoUri();
                if (!(photoUri == null || "photo".equals(Uri.parse(photoUri).getLastPathSegment()))) {
                    hasBigPhoto = true;
                }
                HwLog.i("ContactInfoFragment", "hasPhotoChange = " + this.hasPhotoChange + "; hasBigPhoto = " + hasBigPhoto);
                if (hasBigPhoto || !this.hasPhotoChange) {
                    this.mActionBarGradientView.setVisibility(hasBigPhoto ? 0 : 8);
                    this.mTitleGradientView.setVisibility(hasBigPhoto ? 0 : 8);
                    if (this.mScroller != null) {
                        this.mScroller.setExsitBigPhoto(Boolean.valueOf(hasBigPhoto));
                    }
                    listener = this.mPhotoSetter.setupContactPhotoForClick(getContext(), this.mContactData, hasBigPhoto ? this.mStaticPhotoContainer : this.mSmallPhoto);
                    bindExistPhoto(aSource, hasBigPhoto);
                } else {
                    if (this.addToExsitContactAleady) {
                        Message msg = this.mHandler.obtainMessage();
                        msg.what = 260;
                        msg.obj = this.mContactData.getLookupUri();
                        this.mHandler.sendMessageDelayed(msg, 0);
                    }
                    this.addToExsitContactAleady = false;
                    return;
                }
            }
            long index;
            if (this.mContactData.isUserProfile()) {
                index = (long) ProfileUtils.getDefaultColorIndex(getApplicationContext());
            } else {
                index = this.mContactData.getId();
            }
            if (index >= 0) {
                listener = this.mPhotoSetter.setupContactPhotoForClick(getContext(), this.mContactData, this.mStaticPhotoContainer);
                this.mSmallPhoto.setVisibility(8);
                this.mActionBarGradientView.setVisibility(8);
                this.mTitleGradientView.setVisibility(8);
                this.mStaticPhotoContainer.setImageDrawable(new ColorDrawable(ContactPhotoManager.pickColor(String.valueOf(index), getResources())));
            } else {
                return;
            }
            updateStatusBarColor(-16777216);
            View staticPhotoPress = getView().findViewById(R.id.static_photo_container_press);
            View smallPhotoPress = getView().findViewById(R.id.small_photo_press);
            smallPhotoPress.setVisibility(this.mSmallPhoto.getVisibility());
            if (this.mContactData.isWritableContact(getContext())) {
                staticPhotoPress.setOnLongClickListener(listener);
                smallPhotoPress.setOnClickListener(listener);
            } else {
                staticPhotoPress.setOnLongClickListener(null);
                smallPhotoPress.setOnClickListener(null);
            }
            if (this.mHandler.hasMessages(257)) {
                this.mHandler.removeMessages(257);
            }
            this.mHandler.sendEmptyMessage(257);
            this.freshQrcodeCount = 0;
        }
    }

    private void bindExistPhoto(Bitmap aSource, boolean isBigPhoto) {
        if (isBigPhoto) {
            this.mPhotoSetter.setupContactPhoto(this.mContactData, this.mStaticPhotoContainer, null);
            this.mSmallPhoto.setVisibility(8);
            return;
        }
        Resources resources = getResources();
        this.mSmallPhoto.setImageDrawable(ContactPhotoManager.createRoundPhotoDrawable(new BitmapDrawable(resources, aSource)));
        this.mSmallPhoto.setVisibility(0);
        int color = resources.getColor(R.color.profile_simple_card_default_bg_color);
        if (this.mContactData.isUserProfile()) {
            color = ProfileUtils.getDefaultColorIndex(getApplicationContext());
        }
        this.mStaticPhotoContainer.setImageDrawable(new ColorDrawable(color));
    }

    private void updateStatusBarColor(int color) {
        if (this.mScroller != null && isAdded()) {
            Activity activity = getActivity();
            if (!(activity instanceof PeopleActivity) && !(activity instanceof VoiceSearchResultActivity)) {
                int desiredStatusBarColor = color;
                if (color != activity.getWindow().getStatusBarColor()) {
                    activity.getWindow().setStatusBarColor(color);
                }
            }
        }
    }

    public void bindHeaderData() {
        if (HwLog.HWDBG) {
            HwLog.d("ContactInfoFragment", "bindHeaderData");
        }
        if (!(getIntent() == null || YellowPageContactUtil.isYellowPageUri(getIntent().getData()) || this.mIsNoNamedContact || this.mContactData == null)) {
            if (isShowSplitMenu()) {
                ContactDetailDisplayUtils.configureStarredMenuItem(getContext(), this.mSplitMenuStar, this.mContactData.isDirectoryEntry(), this.mContactData.isUserProfile(), this.mContactData.getStarred());
            }
            if (this.mContactData.isUserProfile()) {
                this.mNumberType = 6;
            }
        }
        IHarassmentInterceptionService mService = Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService"));
        if (this.mContactData != null) {
            this.mPhoneNumberList = this.mContactData.getAllFormattedPhoneNumbers();
        }
        boolean isBlackContact = BlacklistCommonUtils.checkPhoneNumberFromBlockItem(mService, this.mPhoneNumberList);
        if (!shouldShowBlackListMenuOption()) {
            isBlackContact = false;
        }
        ContactDetailDisplayUtils.setContactDisplayInfo(getContext(), this.mContactData, this.mNameTextView, (TextView) getView().findViewById(R.id.company), this.mStaticPhotoContainer, getView().findViewById(R.id.name_container), isBlackContact);
        if (this.mContactData != null) {
            this.mName = this.mContactData.getDisplayName();
            if (!(TextUtils.isEmpty(this.mName) || this.mCallLogDetailFragment == null)) {
                this.mCallLogDetailFragment.setmName(this.mName);
            }
        }
        bindPhotoData();
        if (this.mScroller != null) {
            this.mScroller.resetAndUpdateViewVisibleState();
        }
    }

    protected void onNewIntent(Intent intent) {
        Uri uri = intent.getData();
        if (HwLog.HWDBG) {
            HwLog.d("ContactInfoFragment", "onNewIntent with action  ");
        }
        if (!(this.mLoaderFragment == null || !intent.getBooleanExtra("refresh_details", false) || uri == null)) {
            this.mLoaderFragment.loadUri(uri, false, (Fragment) this);
        }
        if (!"saveCompleted".equals(intent.getAction())) {
            return;
        }
        if (intent.getBooleanExtra("saveSucceeded", false)) {
            updateCallLogCacheData(this.mPrimaryNum);
        } else if (getActivity() != null) {
            getActivity().finish();
        }
    }

    public boolean isDetailsLaunchedBeforeContactSaved() {
        return this.mDetailsLaunchedBeforeContactSaved;
    }

    public void resetDetailsLaunchedBeforeContactSaved() {
        this.mDetailsLaunchedBeforeContactSaved = false;
    }

    public ContactLoaderFragment getLoaderFragment() {
        return this.mLoaderFragment;
    }

    private void onClickMenuItemEdit() {
        if (!(this.mLoaderFragmentListener == null || this.mLookupUri == null)) {
            String simAccoutType = getSimAccountTypeWhenNotReady();
            if (simAccoutType != null) {
                showSimNotReadyToast(simAccoutType);
                if (HwLog.HWFLOW) {
                    HwLog.i("ContactInfoFragment", "ContactDetailActivity.edit.onClick sim is not ready, simAccoutType = " + simAccoutType);
                }
            } else {
                this.mLoaderFragmentListener.onEditRequested(this.mLookupUri);
                PLog.d(1002, "ContactDetailActivity.edit.onClick for jlog");
            }
        }
        if (this.mContactData == null || !this.mContactData.isUserProfile()) {
            StatisticalHelper.report(2007);
            ExceptionCapture.reportScene(29);
        }
    }

    private void onClickMenuItemStar() {
        if (this.mDetailFragment != null) {
            this.mResetFlag = false;
            if (!this.mFlagStarClicked) {
                this.mFlagStarClicked = true;
                if (this.mOnStarBtnClickedInterface != null) {
                    this.mOnStarBtnClickedInterface.onStarBtnClicked();
                }
                if (isShowSplitMenu()) {
                    ContactDetailHelper.starContact(this.mSplitMenuStar, this.mLookupUri, this.mContactData, getContext(), this.mNameTextView, this.mStaticPhotoContainer);
                } else {
                    ContactDetailHelper.starContact(this.menuStar, this.mLookupUri, this.mContactData, getContext(), this.mNameTextView, this.mStaticPhotoContainer);
                }
            }
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    ContactInfoFragment.this.mFlagStarClicked = false;
                }
            }, 50);
        }
    }

    private void createContact() {
        Intent intent = new Intent("android.intent.action.INSERT", Contacts.CONTENT_URI);
        intent.setComponent(new ComponentName(getContext(), ContactEditorActivity.class));
        intent.putExtra("phone", this.mPrimaryNum);
        intent.putExtra("isFromDetailActivityCreateContact", true);
        Intent aIntent = getIntent();
        if (aIntent != null) {
            String originMarkInfo = aIntent.getStringExtra("EXTRA_CALL_LOG_ORIGIN_MARK_INFO");
            if (!TextUtils.isEmpty(originMarkInfo)) {
                intent.putExtra("name", originMarkInfo);
            }
        }
        if (this.mHwCust != null) {
            this.mHwCust.addCustomIntentExtrasForCnap(getIntent(), intent);
        }
        startActivityForResult(intent, 102);
    }

    private void addExistContact() {
        String number = this.mPrimaryNum;
        Intent intent = new Intent("android.intent.action.INSERT_OR_EDIT");
        intent.putExtra("phone", number);
        intent.putExtra("handle_create_new_contact", false);
        intent.setType("vnd.android.cursor.item/contact");
        intent.putExtra("extra_add_exist_contact", true);
        if (this.isLaunchedFromDialpad) {
            intent.putExtra("intent_key_is_from_dialpad", true);
        }
        intent.setClass(getApplicationContext(), ContactSelectionActivity.class);
        if (isSplitActivity(getActivity()) || this.mIsFromDialpadOrDialer) {
            intent.putExtra("SPLIT_INTENT_KEY_IS_FROM_DETAIL", true);
            intent.setClass(getApplicationContext(), TranslucentActivity.class);
        }
        startActivityForResult(intent, 102);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.contact_menuitem_create_contact:
                if (!this.mHasClicked) {
                    this.mHasClicked = true;
                    this.mHandler.sendEmptyMessageDelayed(2, 600);
                    StatisticalHelper.sendReport((int) AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, "3");
                    createContact();
                    break;
                }
                break;
            case R.id.contact_menuitem_star:
                onClickMenuItemStar();
                break;
            case R.id.contact_menuitem_edit:
            case R.id.profile_menuitem_edit:
                onClickMenuItemEdit();
                break;
            case R.id.contact_menuitem_save_exist_contact:
                StatisticalHelper.report(5003);
                addExistContact();
                break;
            case R.id.contact_menuitem_menu:
                Log.i("huangwei", "click the menu in contactinfoment");
                StatisticalHelper.report(1154);
                break;
            case R.id.contact_menuitem_delete_calllog:
                if (this.mCallLogDetailFragment != null) {
                    this.mCallLogDetailFragment.onMenuItemClicked(R.id.contact_menuitem_delete_calllog);
                    break;
                }
                break;
            default:
                processOptionMenuItemClick(item);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        boolean z = true;
        this.menuCreateContact = menu.findItem(R.id.contact_menuitem_create_contact);
        this.menuSaveContact = menu.findItem(R.id.contact_menuitem_save_exist_contact);
        this.menuMenu = menu.findItem(R.id.contact_menuitem_menu);
        this.menuStar = menu.findItem(R.id.contact_menuitem_star);
        this.menuEdit = menu.findItem(R.id.contact_menuitem_edit);
        this.menuDelLog = menu.findItem(R.id.contact_menuitem_delete_calllog);
        this.menuProfileShare = menu.findItem(R.id.profile_menuitem_share);
        this.menuContactShare = menu.findItem(R.id.contact_menuitem_share);
        if (this.mNumberType != 6) {
            menu.setGroupVisible(R.id.detail_profile, false);
        }
        MenuItem menuItem;
        switch (this.mNumberType) {
            case 1:
                menu.setGroupVisible(R.id.detail_options, false);
                menu.findItem(R.id.contact_menuitem_delete_calllog).setVisible(false);
                setMenuVisibility(this.mTabPagerIndex, this.menuMenu, this.menuSaveContact, this.menuCreateContact);
                break;
            case 2:
                menu.setGroupVisible(R.id.detail_onname_options, false);
                setMenuVisibility(this.mTabPagerIndex, this.menuEdit, this.menuMenu);
                if (this.mContactData != null) {
                    this.menuStar.setVisible(this.mTabPagerIndex == 0);
                    ContactDetailDisplayUtils.configureStarredMenuItem(this.menuStar, this.mContactData.isDirectoryEntry(), this.mContactData.isUserProfile(), this.mContactData.getStarred());
                }
                setMenuDelLogAvailablity();
                menuItem = this.menuDelLog;
                if (this.mTabPagerIndex == 0) {
                    z = false;
                }
                menuItem.setVisible(z);
                break;
            case 3:
            case 4:
                menu.setGroupVisible(R.id.detail_options, false);
                menu.setGroupVisible(R.id.detail_onname_options, false);
                setMenuVisibility(this.mTabPagerIndex, this.menuMenu);
                if (this.menuMenu != null) {
                    if (MultiUsersUtils.isCurrentUserGuest()) {
                        this.menuMenu.setEnabled(false);
                    } else if (CommonUtilMethods.isSimplifiedModeEnabled()) {
                        this.menuMenu.setEnabled(this.mHasLog);
                    }
                }
                setMenuDelLogAvailablity();
                menuItem = this.menuDelLog;
                if (this.mTabPagerIndex == 0) {
                    z = false;
                }
                menuItem.setVisible(z);
                break;
            case 5:
                menu.setGroupVisible(R.id.detail_options, false);
                menu.setGroupVisible(R.id.detail_onname_options, false);
                menu.findItem(R.id.contact_menuitem_menu).setVisible(false);
                menuItem = this.menuDelLog;
                if (this.mTabPagerIndex == 0) {
                    z = false;
                }
                menuItem.setVisible(z);
                setMenuDelLogAvailablity();
                break;
            case 6:
                menu.setGroupVisible(R.id.detail_options, false);
                menu.setGroupVisible(R.id.detail_onname_options, false);
                this.menuDelLog.setVisible(false);
                this.menuMenu.setVisible(false);
                break;
        }
        prepareOptionMenu(menu);
    }

    private boolean isShareEnable() {
        if (this.mQrCodeBitmap == null || EmuiFeatureManager.isSuperSaverMode()) {
            return this.isMenuShareEnabled;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void prepareOptionMenu(Menu menu) {
        if (this.mLoaderFragment != null) {
            boolean z;
            MenuItem menuDeleteCalllog = menu.findItem(R.id.menu_delete_calllog);
            MenuItem optionsSendToVoicemail = menu.findItem(R.id.menu_send_to_voicemail);
            MenuItem deleteMenu = menu.findItem(R.id.menu_delete);
            MenuItem shareMenu = menu.findItem(R.id.contact_menuitem_share);
            MenuItem copyContactMenu = menu.findItem(R.id.menu_copy_contact);
            MenuItem sendNumMenuItem = menu.findItem(R.id.menu_send_number);
            MenuItem numberMarkMenuItem = menu.findItem(R.id.menu_mark_as);
            MenuItem joinMenu = menu.findItem(R.id.menu_join_contacts);
            MenuItem seperateContactsMenu = menu.findItem(R.id.menu_separate_contacts);
            MenuItem createContactShortcutMenu = menu.findItem(R.id.menu_create_contact_shortcut);
            MenuItem contactDetailResetMarkMenu = menu.findItem(R.id.menu_detail_reset_mark);
            this.blacklistMenu = menu.findItem(R.id.menu_detail_add_to_blacklist);
            menuDeleteCalllog.setEnabled(this.isMenuDelLogEnabled);
            joinMenu.setVisible(false);
            sendNumMenuItem.setVisible(false);
            contactDetailResetMarkMenu.setVisible(hasConnectionInfo());
            if (MultiUsersUtils.isCurrentUserGuest() || CommonUtilMethods.isDataOnlyVersion()) {
                contactDetailResetMarkMenu.setVisible(false);
            }
            if (this.mIsFromDialer || (this.mRcsCust != null && this.mRcsCust.isFromRcsGroupChat(getIntent()))) {
                if (!this.mIsNoNamedContact) {
                    sendNumMenuItem.setVisible(false);
                    numberMarkMenuItem.setVisible(false);
                    menuDeleteCalllog.setVisible(false);
                } else if (this.isUnKnownNumberCall) {
                    sendNumMenuItem.setVisible(false);
                    this.blacklistMenu.setVisible(false);
                    numberMarkMenuItem.setVisible(false);
                    deleteMenu.setVisible(false);
                    copyContactMenu.setVisible(false);
                    joinMenu.setVisible(false);
                    seperateContactsMenu.setVisible(false);
                    createContactShortcutMenu.setVisible(false);
                    optionsSendToVoicemail.setVisible(false);
                    return;
                } else {
                    boolean canPlaceCallsTo = this.mPhoneNumberHelper.canPlaceCallsTo(this.mPrimaryNum, this.mNoNameCallPresentation);
                    boolean isVoicemailNumber = this.mPhoneNumberHelper.isVoicemailNumber(this.mPrimaryNum);
                    boolean isSipNumber = this.mPhoneNumberHelper.isSipNumber(this.mPrimaryNum);
                    if (!canPlaceCallsTo || isVoicemailNumber || isSipNumber) {
                        sendNumMenuItem.setVisible(false);
                    } else {
                        sendNumMenuItem.setVisible(true);
                    }
                    shareMenu.setVisible(false);
                }
                if (EmuiFeatureManager.isNumberMarkFeatureEnabled() && !MultiUsersUtils.isCurrentUserGuest()) {
                    if (!(numberMarkMenuItem == null || TextUtils.isEmpty(this.mName))) {
                        if (isFromRcsGroupChat()) {
                        }
                    }
                }
                numberMarkMenuItem.setVisible(false);
            } else {
                if (numberMarkMenuItem != null) {
                    numberMarkMenuItem.setVisible(false);
                }
                menuDeleteCalllog.setVisible(false);
                sendNumMenuItem.setVisible(false);
            }
            this.mOptionsMenuOptions = this.mLoaderFragment.isOptionsMenuChanged();
            this.mOptionsMenuEditable = this.mLoaderFragment.isContactEditable();
            this.mOptionsMenuCanCreateShortcut = this.mLoaderFragment.isContactCanCreateShortcut();
            if (this.mContactData != null) {
                this.mSendToVoicemailState = this.mContactData.isSendToVoicemail();
                this.mLoaderFragment.setCustomRingtone(this.mContactData.getCustomRingtone());
            }
            if (optionsSendToVoicemail != null) {
                if (CommonUtilMethods.isSimplifiedModeEnabled()) {
                    optionsSendToVoicemail.setVisible(false);
                } else if (EmuiFeatureManager.isChinaArea()) {
                    optionsSendToVoicemail.setVisible(false);
                } else {
                    optionsSendToVoicemail.setChecked(this.mSendToVoicemailState);
                    z = (!this.mOptionsMenuOptions || this.mContactData == null) ? false : !this.mContactData.isYellowPage();
                    optionsSendToVoicemail.setVisible(z);
                }
            }
            deleteMenu.setVisible(this.mOptionsMenuEditable);
            if (shouldShowBlackListMenuOption()) {
                handleBlackListMenu();
            } else {
                this.blacklistMenu.setVisible(false);
            }
            AccountWithDataSet currentAccount = this.mLoaderFragment.getCurrentAccountWithDataSet();
            boolean joinContactsRequired = false;
            if (EmuiFeatureManager.isProductCustFeatureEnable()) {
                joinContactsRequired = this.mHwCust != null ? this.mHwCust.joinContactsRequired() : false;
            }
            if ((!CommonUtilMethods.isMergeFeatureEnabled() && !joinContactsRequired) || this.mContactData == null || this.mContactData.isUserProfile() || currentAccount == null || CommonUtilMethods.isSimAccount(currentAccount.type) || this.mContactData.isDirectoryEntry() || CommonUtilMethods.isSimplifiedModeEnabled()) {
                joinMenu.setVisible(false);
                seperateContactsMenu.setVisible(false);
            } else {
                joinMenu.setVisible(joinContactsRequired);
                if (ContactDetailHelper.isContactJoined(this.mContactData)) {
                    seperateContactsMenu.setVisible(true);
                } else {
                    seperateContactsMenu.setVisible(false);
                }
            }
            if (this.mContactData == null || this.mContactData.isYellowPage()) {
                shareMenu.setVisible(false);
            } else {
                z = ((this.mOptionsMenuEditable || this.isFromRemote) && !this.mContactData.isUserProfile() && this.mLoaderFragment.isCopyEnabledForCurrentContact()) ? !CommonUtilMethods.isSimplifiedModeEnabled() : false;
                copyContactMenu.setVisible(z);
            }
            copyContactMenu.setTitle(getStringForCopyMenu());
            if (this.isFromRemote) {
                copyContactMenu.setTitle(getString(R.string.menu_copyContact));
                shareMenu.setVisible(false);
            }
            z = this.mOptionsMenuCanCreateShortcut ? !CommonUtilMethods.isSimplifiedModeEnabled() : false;
            createContactShortcutMenu.setVisible(z);
            if (EmuiFeatureManager.isSuperSaverMode()) {
                createContactShortcutMenu.setEnabled(false);
                shareMenu.setEnabled(isShareEnable());
            }
            if (!EmuiFeatureManager.isSystemVoiceCapable()) {
                optionsSendToVoicemail.setVisible(false);
            }
            if (this.mIsReadOnlyContacts) {
                deleteMenu.setVisible(false);
            }
            if (this.menuProfileShare != null) {
                this.menuProfileShare.setEnabled(isShareEnable());
            }
            if (this.menuContactShare != null) {
                this.menuContactShare.setEnabled(isShareEnable());
            }
        }
    }

    private boolean hasConnectionInfo() {
        return (this.mHasDataStatInfo || this.mHasCalllogConInfo) ? true : this.mHasMmsConInfo;
    }

    private void handleBlackListMenu() {
        this.mPhoneNumberList.clear();
        if (this.mContactData != null) {
            this.mPhoneNumberList = this.mContactData.getAllFormattedPhoneNumbers();
        } else if (this.mIsNoNamedContact) {
            this.mPhoneNumberList.add(PhoneNumberFormatter.parsePhoneNumber(this.mPrimaryNum));
        }
        if (this.mPhoneNumberList.size() > 0) {
            int blacklistMenuString;
            if (BlacklistCommonUtils.checkPhoneNumberFromBlockItem(Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService")), this.mPhoneNumberList)) {
                blacklistMenuString = R.string.contact_menu_remove_from_blacklist;
            } else {
                blacklistMenuString = R.string.contact_menu_add_to_blacklist;
            }
            if (EmuiVersion.isSupportEmui()) {
                this.blacklistMenu.setTitle(blacklistMenuString);
                this.blacklistMenu.setVisible(true);
                return;
            }
            return;
        }
        HwLog.v("ContactInfoFragment", "handleBlackListMenu -> No phoneNumbers present");
        this.blacklistMenu.setVisible(false);
    }

    private boolean shouldShowBlackListMenuOption() {
        if (!EmuiFeatureManager.isBlackListFeatureEnabled() || !MultiUsersUtils.isCurrentUserOwner()) {
            return false;
        }
        if (this.mIsFromDialer || (this.mRcsCust != null && this.mRcsCust.isFromRcsGroupChat(getIntent()))) {
            return true;
        }
        return (this.mContactData == null || this.mContactData.isUserProfile() || this.mContactData.isDirectoryEntry()) ? false : true;
    }

    private String getStringForCopyMenu() {
        if (!(this.mContactData == null || ContactDetailHelper.isContactJoined(this.mContactData) || this.mContactData.isUserProfile())) {
            List<AccountWithDataSet> accountList = AccountTypeManager.getInstance(getApplicationContext()).getAccounts(true);
            ContactLoaderFragment loaderFragment = getLoaderFragment();
            if (loaderFragment != null) {
                AccountWithDataSet currentAccount = loaderFragment.getCurrentAccountWithDataSet();
                if (accountList.size() > 1) {
                    if (!accountList.contains(currentAccount)) {
                        return getString(R.string.copy_to_label);
                    }
                    List<AccountWithDataSet> tempList = new ArrayList();
                    tempList.addAll(accountList);
                    tempList.remove(currentAccount);
                    if (tempList.size() > 1) {
                        return getString(R.string.copy_to_label);
                    }
                    AccountWithDataSet lTargetAccount = (AccountWithDataSet) tempList.get(0);
                    String accountName = lTargetAccount.name;
                    if ("com.android.huawei.phone".equalsIgnoreCase(lTargetAccount.type)) {
                        return String.format(getString(R.string.copy_to_account), new Object[]{getContext().getString(R.string.phoneLabelsGroup)});
                    } else if (CommonUtilMethods.isSimAccount(lTargetAccount.type)) {
                        String lAccName = SimFactoryManager.getSimCardDisplayLabel(lTargetAccount.type);
                        if (HwLog.HWDBG) {
                            HwLog.d("ContactInfoFragment", "accountName");
                        }
                        return String.format(getString(R.string.copy_to_account), new Object[]{lAccName});
                    } else {
                        return String.format(getString(R.string.copy_to_account), new Object[]{accountName});
                    }
                }
            }
        }
        return getString(R.string.copy_to_label);
    }

    private boolean processOptionMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_number_before_call:
                onClickMenuItemEditNumberBeforeCall();
                return false;
            case R.id.menu_send_number:
                StatisticalHelper.report(5005);
                onMenuSendNumber();
                return true;
            case R.id.profile_menuitem_share:
            case R.id.contact_menuitem_share:
                onClickMenuItemShare();
                return true;
            case R.id.menu_detail_add_to_blacklist:
                onClickMenuItemAddToBlackList(item);
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.menu_copy_contact:
                return onClickMenuItemCopyContact();
            case R.id.menu_detail_reset_mark:
                return onClickMenuItemResetMark();
            case R.id.menu_send_to_voicemail:
                onClickMenuItemSendToVoiceMail(item);
                return true;
            case R.id.menu_join_contacts:
                Intent intent = new Intent("com.android.contacts.action.JOIN_CONTACT");
                intent.putExtra("com.android.contacts.action.CONTACT_ID", this.mContactData.getId());
                startActivityForResult(intent, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER);
                return true;
            case R.id.menu_separate_contacts:
                Activity activity = getActivity();
                if (activity instanceof PeopleActivity) {
                    UnJoinContacesDialogFragment.unJoinContacts(((PeopleActivity) activity).getFrameFragmentManager(), this);
                } else if (activity instanceof VoiceSearchResultActivity) {
                    UnJoinContacesDialogFragment.unJoinContacts(((VoiceSearchResultActivity) activity).getFrameFragmentManager(), this);
                } else {
                    UnJoinContacesDialogFragment.unJoinContacts(activity.getFragmentManager(), this);
                }
                StatisticalHelper.report(2034);
                return true;
            case R.id.menu_create_contact_shortcut:
                onClickMenuItemCreatShortCut();
                return true;
            case R.id.menu_delete:
                onClickMenuItemDelete();
                return true;
            case R.id.menu_delete_calllog:
                onClickMenuItemDeleteCallLog();
                return true;
            default:
                return onClickMenuItemDefault(item);
        }
    }

    private void onClickMenuItemDelete() {
        String simAccoutType = getSimAccountTypeWhenNotReady();
        if (simAccoutType != null) {
            showSimNotReadyToast(simAccoutType);
        } else if (this.mLoaderFragmentListener != null) {
            this.mLoaderFragmentListener.onDeleteRequested(this.mLookupUri);
        }
        StatisticalHelper.report(1144);
        ExceptionCapture.reportScene(32);
    }

    private void onClickMenuItemSendToVoiceMail(MenuItem item) {
        setResetFlag(false);
        if (this.mSendToVoicemailState) {
            send2VoiceMail(item);
        } else {
            showIsOpenVMDialog(item);
        }
    }

    private boolean onClickMenuItemCopyContact() {
        StatisticalHelper.report(1143);
        String simAccoutType = getSimAccountTypeWhenNotReady();
        if (simAccoutType != null) {
            showSimNotReadyToast(simAccoutType);
        } else if (this.mDetailFragment != null && this.isFromRemote) {
            this.mDetailFragment.doQuickFix();
            return true;
        } else if (this.mLoaderFragment != null) {
            return this.mLoaderFragment.copyContactEvent();
        }
        return true;
    }

    private void onClickMenuItemAddToBlackList(MenuItem item) {
        String name;
        if (TextUtils.isEmpty(this.mName) || this.mContactData == null) {
            name = this.mCallLogDetailFragment.getmName();
        } else {
            name = this.mContactData.getDisplayName();
        }
        ContactDetailHelper.handleBlackListMenuAction(item.getTitle(), this, getChildFragmentManager(), this.mPhoneNumberList, name);
        if (needShowDetailEntry()) {
            bindHeaderDataWithOnlyNumber();
        } else if (this.mContactData != null) {
            bindHeaderData();
        }
        ExceptionCapture.reportScene(30);
    }

    private void onClickMenuItemEditNumberBeforeCall() {
        if (!TextUtils.isEmpty(this.mPrimaryNum)) {
            startActivity(new Intent("android.intent.action.DIAL", CallUtil.getCallUri(this.mPrimaryNum)));
        }
    }

    private boolean onClickMenuItemResetMark() {
        StatisticalHelper.report(1130);
        if (this.mCallLogDetailFragment != null) {
            if (getActivity().checkSelfPermission("android.permission.READ_SMS") != 0) {
                requestPermissions(new String[]{"android.permission.READ_SMS"}, 1);
                return false;
            }
            this.mResetFlag = false;
            this.mCallLogDetailFragment.eraseContactDialog(this.eraseContactMarkClickListener, true);
        }
        return true;
    }

    private void onClickMenuItemShare() {
        AlertDialogFragmet.show(getFragmentManager(), (int) R.string.profile_dialog_share_contacts, (int) R.array.share_contact_select_items, Boolean.valueOf(true), this.shareItemSelectListener, 3);
    }

    private void shareBusinessCard() {
        if (this.mTotalContext != null) {
            Uri uri = ProfileUtils.getBitmapFileUri(getApplicationContext(), this.mQrCodeBitmap);
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("image/*");
            intent.putExtra("android.intent.extra.STREAM", uri);
            intent.addFlags(3);
            this.mTotalContext.startActivity(Intent.createChooser(intent, this.mTotalContext.getString(R.string.profile_dialog_share_contacts)));
        }
    }

    private void shareVcard() {
        String simAccoutType = getSimAccountTypeWhenNotReady();
        if (simAccoutType != null) {
            showSimNotReadyToast(simAccoutType);
        } else {
            ContactDetailHelper.shareContact(this.mContactData, this.mTotalContext);
        }
        ExceptionCapture.reportScene(26);
        StatisticalHelper.report(2038);
    }

    private void shareTextCard() {
        if (this.mTotalContext != null && this.mDetailFragment != null) {
            String textCardString = ProfileUtils.buildShareTextCard(this.mDetailFragment.getEntriesObject());
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra("android.intent.extra.TEXT", textCardString);
            intent.addHwFlags(16);
            this.mTotalContext.startActivity(Intent.createChooser(intent, this.mTotalContext.getString(R.string.profile_dialog_share_contacts)));
        }
    }

    private void onClickMenuItemCreatShortCut() {
        if (this.mDetailFragment == null || this.mDetailFragment.getPhoneNumbers().size() == 0) {
            ContactDetailHelper.createLauncherShortcutWithContact(getActivity(), this.mLookupUri);
        } else {
            AlertDialogFragmet.show(this.mDetailFragment.getChildFragmentManager(), (int) R.string.shortcutActivityTitle, this.mDetailFragment.getFormattedPhoneNum(), this.mContactData.getUri(), this.mDetailFragment.getFormattedPhoneNumUri());
        }
        ExceptionCapture.reportScene(31);
    }

    private void onClickMenuItemDeleteCallLog() {
        if (this.mCallLogDetailFragment != null) {
            this.mCallLogDetailFragment.onMenuItemClicked(R.id.menu_delete_calllog);
        }
    }

    private boolean onClickMenuItemDefault(MenuItem item) {
        if (this.mCallLogDetailFragment != null) {
            return this.mCallLogDetailFragment.onMenuItemClicked(item.getItemId());
        }
        return false;
    }

    private void asynchronousQueryFrequent() {
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                ContactInfoFragment.this.mHasDataStatInfo = ContactInfoFragment.this.queryFrequent();
                Activity activity = ContactInfoFragment.this.getActivity();
                if (activity != null) {
                    activity.invalidateOptionsMenu();
                }
            }
        });
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean queryFrequent() {
        boolean result = false;
        if (this.mContactData != null) {
            if (this.mCallLogDetailFragment == null || this.mCallLogDetailFragment.getActivity() == null || this.mContactData.getId() <= 0) {
                return false;
            }
            try {
                Cursor cursor = EraseContactMarkUtils.getDataUsageState(this.mCallLogDetailFragment.getActivity(), this.mContactData.getId());
                if (cursor != null) {
                    result = cursor.getCount() > 0;
                }
                CloseUtils.closeCursorIfNotNull(cursor);
            } catch (Throwable th) {
                CloseUtils.closeCursorIfNotNull(null);
            }
        }
        return result;
    }

    public OnDialogOptionSelectListener getEraseContactMarkClickListener() {
        return this.eraseContactMarkClickListener;
    }

    private static void eraseMmsInfo(Context context, Contact contactData, int numberType, String primaryNum) {
        if (context != null) {
            Uri mmsSmsUriDelete;
            ContentResolver resolver = context.getContentResolver();
            Builder mmsBuilder = Uri.withAppendedPath(MmsSms.CONTENT_URI, "delete_messages_by_contact_id").buildUpon();
            if (numberType == 1) {
                if (!TextUtils.isEmpty(primaryNum)) {
                    mmsSmsUriDelete = mmsBuilder.appendPath(Long.toString(0)).appendQueryParameter("delete_messages_number", primaryNum).build();
                } else {
                    return;
                }
            } else if (numberType == 3) {
                if (contactData != null) {
                    mmsSmsUriDelete = mmsBuilder.appendPath(Long.toString(contactData.getId())).appendQueryParameter("yellow_page", "true").build();
                } else {
                    return;
                }
            } else if (contactData != null) {
                mmsSmsUriDelete = mmsBuilder.appendPath(Long.toString(contactData.getId())).build();
            } else {
                return;
            }
            try {
                resolver.delete(mmsSmsUriDelete, null, null);
            } catch (SecurityException e) {
                HwLog.e("ContactInfoFragment", "delete SecurityException");
            } catch (UnsupportedOperationException e2) {
                HwLog.e("ContactInfoFragment", "delete UnsupportedOperationException");
            } catch (Exception e3) {
                HwLog.e("ContactInfoFragment", "delete Exception");
            }
        }
    }

    private void showIsOpenVMDialog(final MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle(R.string.open_voicemail_dialog_title).setNegativeButton(17039360, null).setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                ContactInfoFragment.this.send2VoiceMail(item);
            }
        });
        View view = View.inflate(getContext(), R.layout.alert_dialog_content, null);
        ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(R.string.open_voicemail_dialog_message);
        builder.setView(view);
        builder.create().show();
    }

    private void send2VoiceMail(MenuItem item) {
        if (item != null) {
            this.mSendToVoicemailState = !this.mSendToVoicemailState;
            item.setChecked(this.mSendToVoicemailState);
            getApplicationContext().startService(ContactSaveService.createSetSendToVoicemail(getApplicationContext(), this.mLookupUri, this.mSendToVoicemailState));
        }
    }

    public void onMenuSendNumber() {
        String nameAndNumberSendByMMS;
        if (this.mContactData == null) {
            nameAndNumberSendByMMS = this.mPrimaryNum;
        } else {
            nameAndNumberSendByMMS = this.mContactData.getDisplayName() + "\n" + this.mPrimaryNum;
        }
        boolean canPlaceCallsTo = this.mPhoneNumberHelper.canPlaceCallsTo(this.mPrimaryNum, this.mNoNameCallPresentation);
        boolean isVoicemailNumber = this.mPhoneNumberHelper.isVoicemailNumber(this.mPrimaryNum);
        boolean isSipNumber = this.mPhoneNumberHelper.isSipNumber(this.mPrimaryNum);
        if (canPlaceCallsTo && !isVoicemailNumber && !isSipNumber) {
            try {
                Intent mainActionIntent = new Intent("android.intent.action.SENDTO", Uri.parse("smsto:"));
                mainActionIntent.putExtra("sms_body", nameAndNumberSendByMMS);
                startActivity(mainActionIntent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(getContext(), R.string.quickcontact_missing_app_Toast, 0).show();
                ex.printStackTrace();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void onQueryPhoneNumberCompleted(ArrayList<String> phoneNums) {
        if (isAdded()) {
            ArrayList<String> tempPhoneNumberList;
            if (phoneNums != null) {
                tempPhoneNumberList = (ArrayList) phoneNums.clone();
            } else {
                tempPhoneNumberList = new ArrayList();
            }
            if (this.mCallLogDetailFragment != null) {
                if ((this.mIsFromDialer || isFromRcsGroupChat()) && this.mIsNoNamedContact) {
                    int delayed = this.isUnKnownNumberCall ? 0 : 200;
                    this.mCallLogDetailFragment.updateCallLogs(tempPhoneNumberList, this.mPhoneNumCountryISO);
                    this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (!ContactInfoFragment.this.mIsNoNamedContact) {
                                ContactInfoFragment.this.mHasCalllogConInfo = ContactInfoFragment.this.mCallLogDetailFragment.hasCallLogRecord();
                            }
                        }
                    }, (long) delayed);
                } else if (this.mDetailFragment.isEntriesBuilt() && !(this.mOnQueryPhoneNumberCompletedCalled && this.mNumberList.equals(tempPhoneNumberList))) {
                    int i;
                    this.mOnQueryPhoneNumberCompletedCalled = true;
                    this.mNumberList.clear();
                    this.mNumberList.addAll(tempPhoneNumberList);
                    Handler handler = this.mHandler;
                    Runnable anonymousClass15 = new Runnable() {
                        public void run() {
                            ContactInfoFragment.this.mCallLogDetailFragment.updateCallLogs(tempPhoneNumberList, ContactInfoFragment.this.mPhoneNumCountryISO);
                            ContactInfoFragment.this.mHasCalllogConInfo = ContactInfoFragment.this.mCallLogDetailFragment.hasCallLogRecord();
                            if (!ContactInfoFragment.this.mHasCalllogConInfo) {
                                ContactInfoFragment.this.asynchronousQueryFrequent();
                            }
                        }
                    };
                    if (this.mIsFromDialer) {
                        i = 0;
                    } else {
                        i = 200;
                    }
                    handler.postDelayed(anonymousClass15, (long) i);
                }
                this.mCallLogDetailFragment.setmIsFromDialer(this.mIsFromDialer);
                if (this.mIsNoNamedContact) {
                    this.mCallLogDetailFragment.setmName(this.mName);
                } else if (!TextUtils.isEmpty(this.mName)) {
                    this.mCallLogDetailFragment.setmName(this.mName);
                }
            }
        }
    }

    public void onQueryPhoneNumberCompletedIso(HashMap<String, String> hm) {
        if (hm != null) {
            this.mPhoneNumCountryISO.putAll(hm);
        }
    }

    public boolean acceptThisEvent(int motion) {
        return motion == 302 && this.mMReco != null;
    }

    public void handleMotionEvent(int motion) {
        String lNumber;
        int lCurrentTab = getCurrentSelected();
        if (this.mCallLogDetailFragment == null || !this.mCallLogDetailFragment.hasCallLogRecord() || TextUtils.isEmpty(this.mCallLogDetailFragment.getLatestCallNumber())) {
            lNumber = this.mDetailFragment.getFirstPhoneNumber();
        } else {
            lNumber = this.mCallLogDetailFragment.getLatestCallNumber();
        }
        if (com.android.contacts.hap.CommonConstants.LOG_DEBUG) {
            HwLog.d("ContactInfoFragment", "handleMotionEvent:" + motion);
        }
        if (TextUtils.isEmpty(lNumber)) {
            HwLog.e("ContactInfoFragment", "lNumber is null");
            return;
        }
        boolean hasCallLogRecord = this.mCallLogDetailFragment.hasCallLogRecord();
        int lastCallSimType = this.mCallLogDetailFragment.getLastCallSimType(lNumber);
        if (CommonUtilMethods.isCoverOpen()) {
            ContactDetailHelper.dialOutByProximity(lNumber, SimFactoryManager.isDualSim(), getContext(), hasCallLogRecord, lastCallSimType);
            if (lCurrentTab == 0) {
                StatisticalHelper.report(4045);
            } else {
                StatisticalHelper.report(4046);
            }
        }
    }

    public void startMotionRecognition() {
        if (isAdded()) {
            if (this.mMReco == null) {
                this.mMReco = MotionRecognition.getInstance(getApplicationContext(), this);
            }
            this.mMReco.stratMotionRecognition(302);
        }
    }

    public void destroyMotionRecognition() {
        if (this.mMReco != null) {
            this.mMReco.stopMotionRecognition(302);
            this.mMReco.destroy(this);
            this.mMReco = null;
        }
    }

    public void onUpdateCompleted(PhoneCallDetails latestPhoneCall) {
        if (isAdded() && this.mDetailFragment != null && this.mDetailFragment.isAdded() && this.mDetailFragment.getDetailAdapter() != null) {
            this.mDetailFragment.getDetailAdapter().notifyDataSetChanged();
            if (HwLog.HWFLOW) {
                HwLog.i("ContactInfoFragment", "onUpdateCompleted,notify adapter data set changed");
            }
        }
    }

    private void setNumberMarkInfo(String displayName, String displayCompany) {
        if (!EmuiFeatureManager.isNumberMarkFeatureEnabled() || !MultiUsersUtils.isCurrentUserOwner()) {
            return;
        }
        if ((isFromRcsGroupChat() || TextUtils.isEmpty(this.mName)) && this.mIsNoNamedContact) {
            ContactDetailDisplayUtils.setContactDisplayInfo(getContext(), this.mNameTextView, String.valueOf(displayName), (TextView) getView().findViewById(R.id.company), displayCompany);
            if (this.mScroller != null) {
                this.mScroller.resetAndUpdateViewVisibleState();
            }
        }
    }

    private void updateCallLogCacheData(final String number) {
        new Thread("updateCallLogCacheData") {
            public void run() {
                String newNumber = ContactsUtils.removeDashesAndBlanks(number);
                ContentValues values = new ContentValues();
                Map<String, PredefinedNumbers> mSpecialNumbersMap = ContactsUtils.getPredefinedMap(ContactInfoFragment.this.getContext());
                if (mSpecialNumbersMap == null || !mSpecialNumbersMap.containsKey(newNumber)) {
                    ContactInfo info = ContactInfoFragment.this.mContactInfoHelper.lookupNumber(newNumber, ContactInfoFragment.this.mCountryIso);
                    if (info != null) {
                        values.put("name", info.name);
                        values.put("numbertype", Integer.valueOf(info.type));
                        values.put("numberlabel", info.label);
                        values.put("lookup_uri", UriUtils.uriToString(info.lookupUri));
                        values.put("matched_number", info.number);
                        values.put("normalized_number", info.normalizedNumber);
                        values.put("photo_id", Long.valueOf(info.photoId));
                        values.put("formatted_number", info.formattedNumber);
                        ContactInfoFragment.this.getContext().getContentResolver().update(QueryUtil.getCallsContentUri(), values, "number = ?", new String[]{newNumber});
                    }
                }
            }
        }.start();
    }

    private void reflectSetViewpager(ViewPager detailPager, String feildName) {
        try {
            if (CommonUtilMethods.calcIfNeedSplitScreen() && detailPager != null) {
                Field fieldViewpage = ViewPager.class.getDeclaredField(feildName);
                fieldViewpage.setAccessible(true);
                fieldViewpage.set(detailPager, null);
            }
        } catch (NoSuchFieldException e) {
            HwLog.i("ContactInfoFragment", "mHwViewPager is not exist!");
        } catch (IllegalAccessException e2) {
            HwLog.i("ContactInfoFragment", "mHwViewPager is not illegal!Access");
        }
    }

    private void initializeTabSpecs(Context context, Bundle savedInstanceState) {
        boolean z;
        boolean z2 = true;
        HwLog.i("ContactInfoFragment", "initializeTabSpecs come in !");
        addDetailAndCallFragment();
        this.mDetailPager = (ViewPager) getView().findViewById(R.id.detail_tab_pager);
        reflectSetViewpager(this.mDetailPager, "mHwViewPager");
        this.mSubTabWidget = (SubTabWidget) getView().findViewById(R.id.subTab_layout);
        this.mSubTabFragmentPagerAdapter = new SubTabAdapter(getChildFragmentManager(), getActivity(), this.mDetailPager, this.mSubTabWidget);
        int pos = (savedInstanceState == null || !savedInstanceState.getBoolean("save_tab_succeed")) ? this.mIsLaunchedFromContacts ? 0 : 1 : savedInstanceState.getInt("current_tab");
        SubTab subTab = this.mSubTabWidget.newSubTab(getString(R.string.tab_contact_details));
        subTab.setSubTabId(R.id.contact_info_tab_details);
        SubTabFragmentPagerAdapter subTabFragmentPagerAdapter = this.mSubTabFragmentPagerAdapter;
        Fragment fragment = this.mDetailFragment;
        if (pos == 0) {
            z = true;
        } else {
            z = false;
        }
        subTabFragmentPagerAdapter.addSubTab(subTab, fragment, null, z);
        if (EmuiFeatureManager.isSystemVoiceCapable()) {
            subTab = this.mSubTabWidget.newSubTab(getString(R.string.tab_contact_calllog));
            subTab.setSubTabId(R.id.contact_info_tab_calllog);
            SubTabFragmentPagerAdapter subTabFragmentPagerAdapter2 = this.mSubTabFragmentPagerAdapter;
            Fragment fragment2 = this.mCallLogDetailFragment;
            if (pos != 1) {
                z2 = false;
            }
            subTabFragmentPagerAdapter2.addSubTab(subTab, fragment2, null, z2);
        }
    }

    private int getCurrentSelected() {
        return this.mSubTabWidget == null ? 0 : this.mSubTabWidget.getSelectedSubTabPostion();
    }

    public void onServiceCompleted(final Intent callbackIntent) {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                ContactInfoFragment.this.onNewIntent(callbackIntent);
            }
        }, 100);
    }

    private Bundle getQRCodeEntry() {
        Bundle bundle = new Bundle();
        if (!(this.mQRcodeDataInfo == null || this.mQRcodeDataInfo.size() == 0 || this.mContactData == null)) {
            putEntryForQRCode("phone", (ArrayList) this.mQRcodeDataInfo.get("phone"), 3, bundle);
            putEntryForQRCode(Scopes.EMAIL, (ArrayList) this.mQRcodeDataInfo.get(Scopes.EMAIL), 3, bundle);
            putEntryForQRCode("postal", (ArrayList) this.mQRcodeDataInfo.get("postal"), 1, bundle);
            if (this.mContactData.isYellowPage()) {
                ArrayList<String> yellowName = new ArrayList();
                yellowName.add(this.mContactData.getDisplayName());
                putEntryForQRCode("name", yellowName, 1, bundle);
            } else {
                putEntryForQRCode("name", (ArrayList) this.mQRcodeDataInfo.get("name"), 1, bundle);
            }
            putEntryForQRCode("company", (ArrayList) this.mQRcodeDataInfo.get("company"), 1, bundle);
            putEntryForQRCode("URL_KEY", (ArrayList) this.mQRcodeDataInfo.get("URL_KEY"), 1, bundle);
            putEntryForQRCode("job_title", (ArrayList) this.mQRcodeDataInfo.get("job_title"), 1, bundle);
        }
        return bundle;
    }

    private void putEntryForQRCode(String mTitleName, ArrayList<String> mEntries, int limit, Bundle bundle) {
        if (mEntries != null && mEntries.size() != 0) {
            String keyFirst = mTitleName;
            String keySecond = "secondary_" + mTitleName;
            String keyThird = "tertiary_" + mTitleName;
            String[] keys = new String[]{mTitleName, keySecond, keyThird};
            ArrayList<String> entries = new ArrayList();
            if (mEntries.size() > limit) {
                int count = 0;
                for (String s : mEntries) {
                    if (count > limit) {
                        break;
                    } else if (!entries.contains(s)) {
                        entries.add(s);
                        count++;
                    }
                }
                mEntries = entries;
            }
            int noMoreThanKeysLength = Math.min(Math.min(mEntries.size(), limit), keys.length);
            for (int i = 0; i < noMoreThanKeysLength; i++) {
                bundle.putString(keys[i], (String) mEntries.get(i));
            }
        }
    }

    private void buildQRCode() {
        Bundle lBundle = getQRCodeEntry();
        if (!(this.mQRcodeDataInfo == null || this.mQRcodeDataInfo.size() == 0)) {
            ArrayList<String> notes = (ArrayList) this.mQRcodeDataInfo.get("NOTE_KEY");
            ArrayList<String> ims = (ArrayList) this.mQRcodeDataInfo.get("IM");
            StringBuffer noteInfo = new StringBuffer();
            if (notes != null && notes.size() > 0) {
                noteInfo.append((String) notes.get(0));
            }
            if (ims != null && ims.size() > 0) {
                if (noteInfo.length() > 0) {
                    noteInfo.append("\n");
                }
                noteInfo.append((String) ims.get(0));
            }
            lBundle.putString("NOTE_KEY", String.valueOf(noteInfo));
        }
        getQRCodeBitmap(lBundle);
    }

    private void getQRCodeBitmap(final Bundle bundle) {
        if (isAdded()) {
            final int width = getResources().getDimensionPixelSize(R.dimen.contact_detail_qrcode_width);
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    ContactInfoFragment.this.generatQrBitmap(bundle, width);
                }
            });
            thread.setName("Detail QRCode Thread");
            thread.start();
        }
    }

    private void generatQrBitmap(Bundle bundle, int width) {
        try {
            if (this.mContactData == null) {
                this.mHandler.sendEmptyMessage(259);
                return;
            }
            getHeaderPhoto();
            Bitmap QrCodeBitmap = new QRCodeEncoder(getContext()).encodeQRCodeContents(bundle, this.mPhotoBitmap, "CONTACT_TYPE", null);
            this.mQrCodeBitmap = ContactsUtils.compressBitmap(QrCodeBitmap, width, width);
            if (!(QrCodeBitmap == null || this.mQrCodeBitmap == QrCodeBitmap)) {
                QrCodeBitmap.recycle();
            }
            this.mHandler.sendEmptyMessage(258);
        } catch (WriterException e) {
            HwLog.e("ContactInfoFragment", "Can not generate QRcode bitmap !");
            this.mHandler.sendEmptyMessage(259);
        }
    }

    public void getQRcodeEntryCompleted(HashMap<String, ArrayList<String>> qrCodeDataInfo) {
        if (isAdded() && this.mQRcodeIndication != null && qrCodeDataInfo != null && this.mResetFlag && !qrCodeDataInfo.equals(this.mQRcodeDataInfo)) {
            if (!(this.mQRcodeDialog == null || !this.mQRcodeDialog.isShowing() || this.mQRcodeDataInfo == null)) {
                this.mQRcodeDialog.dismiss();
            }
            boolean needDelay = this.mQRcodeDataInfo == null;
            this.mQRcodeDataInfo = qrCodeDataInfo;
            if (this.mHandler.hasMessages(257)) {
                this.mHandler.removeMessages(257);
            }
            if (this.freshQrcodeCount <= 3) {
                if (needDelay) {
                    this.mHandler.sendEmptyMessageDelayed(257, 500);
                } else {
                    this.mHandler.sendEmptyMessage(257);
                }
                this.freshQrcodeCount++;
            }
        }
    }

    private void getHeaderPhoto() {
        if (this.mContactData != null) {
            byte[] photoData = this.mContactData.mPhotoBinaryData;
            if (photoData == null) {
                this.mPhotoData = null;
                this.mPhotoBitmap = null;
            } else if (!Arrays.equals(this.mPhotoData, photoData)) {
                this.mPhotoData = photoData;
                try {
                    this.mPhotoBitmap = ContactPhotoManager.createRoundPhoto(BitmapFactory.decodeByteArray(photoData, 0, photoData.length, null));
                } catch (Exception e) {
                    HwLog.e("ContactInfoFragment", "parse photo fail!");
                }
            } else if (HwLog.HWDBG) {
                HwLog.d("ContactInfoFragment", "photo no change!");
            }
        }
    }

    private void bindQRCodeGenerat() {
        int i = 0;
        if (this.mQrCodeBitmap == null) {
            bindQRCodeGeneratFail();
            return;
        }
        if (this.mQRCodeImage != null) {
            this.mQRCodeImage.setVisibility(0);
            this.mQRCodeImage.setImageBitmap(this.mQrCodeBitmap);
        }
        if (this.mQRcodeIndication != null) {
            ImageView imageView = this.mQRcodeIndication;
            if ((this.mIsFromDialer || isFromRcsGroupChat()) && this.mIsNoNamedContact) {
                i = 4;
            }
            imageView.setVisibility(i);
        }
        if (this.menuProfileShare != null) {
            this.menuProfileShare.setEnabled(isShareEnable());
        }
        if (this.menuContactShare != null) {
            this.menuContactShare.setEnabled(isShareEnable());
        }
        ProfileUtils.getBitmapFileUri(getApplicationContext(), this.mQrCodeBitmap);
    }

    private void bindQRCodeGeneratFail() {
        if (this.mQRCodeImage != null) {
            this.mQRCodeImage.setVisibility(8);
        }
        if (this.mQRcodeIndication != null) {
            this.mQRcodeIndication.setVisibility(4);
        }
        if (this.menuProfileShare != null) {
            this.menuProfileShare.setEnabled(isShareEnable());
        }
        if (this.menuContactShare != null) {
            this.menuContactShare.setEnabled(isShareEnable());
        }
    }

    private int getButtonQRcodeImageOrDefaultPhoto(int type) {
        boolean isSwitchDrawableID = ImmersionUtils.getImmersionStyle(getContext()) == 1 ? CommonUtilMethods.isLargeThemeApplied(getResources()) : true;
        switch (type) {
            case 1:
                if (isSwitchDrawableID) {
                    return R.drawable.csp_menu_collapse;
                }
                return R.drawable.csp_menu_collapse_light;
            case 2:
                return R.drawable.ic_public_code;
            default:
                return -1;
        }
    }

    private String getSimAccountTypeWhenNotReady() {
        if (EmuiFeatureManager.isPreLoadingSimContactsEnabled()) {
            String str = null;
            boolean isSimContactAndReadOnly = false;
            if (!SimFactoryManager.isBothSimLoadingFinished()) {
                str = SimFactoryManager.getContactAccountType(this.mLookupUri);
                if (CommonUtilMethods.isSimAccount(str)) {
                    isSimContactAndReadOnly = !SimFactoryManager.isSimLoadingFinished(str);
                }
            }
            if (isSimContactAndReadOnly) {
                return str;
            }
        }
        return null;
    }

    private void showSimNotReadyToast(String accountType) {
        if (CommonUtilMethods.isSimAccount(accountType)) {
            Toast.makeText(getContext(), String.format(getString(R.string.sim_not_ready), new Object[]{SimFactoryManager.getSimCardDisplayLabel(accountType)}), 0).show();
        }
    }

    private void queryNumMarkInfoInNewThread(Context context, String markNum, String geo, String formatNum) {
        final Context context2 = context;
        final String str = markNum;
        final String str2 = geo;
        final String str3 = formatNum;
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                Message msg = Message.obtain();
                msg.what = 1004;
                Object obj = null;
                String str = null;
                String str2 = null;
                Object[] result = ContactInfoFragment.this.queryNumMarkInfo(context2, str, str2, str3);
                if (result != null && result.length == 4) {
                    obj = result[0];
                    str = result[1];
                    msg.arg1 = ((Integer) result[2]).intValue();
                    str2 = result[3];
                }
                if (!TextUtils.isEmpty(obj)) {
                    Bundle markBundle = new Bundle();
                    markBundle.putString("displayName", obj);
                    markBundle.putString("displayCompany", str);
                    markBundle.putString("originCompany", str2);
                    msg.setData(markBundle);
                    if (ContactInfoFragment.this.mHandler.hasMessages(1004)) {
                        ContactInfoFragment.this.mHandler.removeMessages(1004);
                    }
                    ContactInfoFragment.this.mHandler.sendMessage(msg);
                }
            }
        });
    }

    private Object[] queryNumMarkInfo(Context context, String markNum, String geo, String formatNum) {
        Cursor cursor = null;
        String displayName = null;
        String displayCompany = null;
        String originName = null;
        try {
            int numberType;
            cursor = NumberMarkManager.getLocalMarkCursor(context, NumberMarkManager.standardizationPhoneNum(markNum, context));
            if (cursor != null && cursor.getCount() > 0) {
                String name;
                if (cursor.getCount() == 2) {
                    while (cursor.moveToNext()) {
                        boolean isCloudData;
                        if (cursor.getInt(4) != 0) {
                            isCloudData = true;
                            continue;
                        } else {
                            isCloudData = false;
                            continue;
                        }
                        if (!isCloudData) {
                            name = cursor.getString(2);
                            originName = name;
                            displayCompany = NumberMarkManager.appendSupplierInfo(context, name, cursor.getString(7));
                            displayName = formatNum;
                            break;
                        }
                    }
                } else if (cursor.getCount() == 1) {
                    cursor.moveToFirst();
                    name = cursor.getString(2);
                    String supplier = cursor.getString(7);
                    String markType = cursor.getString(1);
                    originName = NumberMarkUtil.revertMarkTypeToMarkName(markType, context);
                    if (TextUtils.isEmpty(originName)) {
                        originName = name;
                    } else {
                        name = originName;
                    }
                    boolean isBrandNumberMark = "brand".equals(markType);
                    if ("w3".equals(markType)) {
                        displayCompany = cursor.getString(5);
                        displayName = name;
                    } else if (isBrandNumberMark) {
                        displayCompany = NumberMarkManager.appendSupplierInfo(context, formatNum, supplier);
                        displayName = name;
                    } else {
                        int markCount = cursor.getInt(3);
                        if ("tencent".equals(supplier) && (markCount > 0 || markCount == -501)) {
                            name = NumberMarkUtil.appendMarkCount(context, cursor.getString(1), markCount, markCount == -501);
                        }
                        displayCompany = NumberMarkManager.appendSupplierInfo(context, name, supplier);
                        displayName = formatNum;
                    }
                }
                numberType = 1;
            } else if (IsPhoneNetworkRoamingUtils.isMaritimeSatelliteNumber(markNum)) {
                numberType = 3;
                displayName = formatNum;
                displayCompany = context.getString(R.string.contacts_str_filter_Maritime_Satellite_calls);
            } else {
                displayName = formatNum;
                numberType = 2;
                displayCompany = null;
            }
            Object[] objArr = new Object[]{displayName, displayCompany, Integer.valueOf(numberType), originName};
            return objArr;
        } finally {
            CloseUtils.closeCursorIfNotNull(cursor);
        }
    }

    private boolean isFromRcsGroupChat() {
        return this.mRcsCust != null ? this.mRcsCust.isFromRcsGroupChat(getIntent()) : false;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (isAdded()) {
            int i;
            switch (requestCode) {
                case 1:
                    if (permissions != null && permissions.length > 0) {
                        for (i = 0; i < permissions.length; i++) {
                            if (grantResults[i] != 0) {
                                try {
                                    startActivity(RequestPermissionsActivityBase.createRequestPermissionIntent(permissions, getContext().getPackageName()));
                                } catch (Exception e) {
                                    HwLog.e("ContactInfoFragment", "Activity not find!");
                                }
                                return;
                            }
                        }
                        if (this.mCallLogDetailFragment != null) {
                            this.mCallLogDetailFragment.eraseContactDialog(this.eraseContactMarkClickListener, true);
                            break;
                        }
                    }
                    break;
                case 2:
                    if (permissions != null && permissions.length > 0) {
                        for (i = 0; i < permissions.length; i++) {
                            if (grantResults[i] != 0) {
                                try {
                                    startActivity(RequestPermissionsActivityBase.createRequestPermissionIntent(permissions, getActivity().getPackageName()));
                                } catch (Exception e2) {
                                    HwLog.e("ContactInfoFragment", "Activity not find!");
                                }
                                return;
                            }
                        }
                        if (!(this.mDetailFragment == null || this.mDetailFragment.getDetailAdapter() == null)) {
                            ContactDetailAdapter adapter = this.mDetailFragment.getDetailAdapter();
                            adapter.selectRingTone(adapter.getRingToneUri());
                            break;
                        }
                    }
                case 3:
                    if (permissions != null && permissions.length > 0) {
                        for (i = 0; i < permissions.length; i++) {
                            if (grantResults[i] != 0) {
                                try {
                                    startActivity(RequestPermissionsActivityBase.createRequestPermissionIntent(permissions, getContext().getPackageName()));
                                } catch (Exception e3) {
                                    HwLog.e("ContactInfoFragment", "Activity not find!");
                                }
                                return;
                            }
                        }
                        asyncQueryIfHasMmsInfo();
                        if (this.mCallLogDetailFragment != null) {
                            CallLogDetailHistoryAdapter adapter2 = this.mCallLogDetailFragment.getAdapter();
                            if (adapter2 != null) {
                                adapter2.asynLoadRcsCache();
                                break;
                            }
                        }
                    }
                    break;
            }
        }
    }

    public void setContactIfNeed() {
        if (this.mIsFailedSetContactData && this.mDetailFragment != null) {
            this.mDetailFragment.setContactData(this.mContactData);
        }
    }

    private void setHeaderNumberMarkInfo(String displayName, String displayCompany, String originName, int type) {
        setNumberMarkInfo(displayName, checkNumFromBlackList(this.mPrimaryNum, displayCompany));
        if (type == 2) {
            getIntent().putExtra("EXTRA_CALL_LOG_MARKINFO", "");
        } else {
            getIntent().putExtra("EXTRA_CALL_LOG_MARKINFO", displayCompany);
        }
        getIntent().putExtra("EXTRA_CALL_LOG_MARKLABEL", displayName);
        getIntent().putExtra("EXTRA_CALL_LOG_ORIGIN_MARK_INFO", originName);
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void handleActivityResultFromEditor(Intent aData) {
        if (aData != null) {
            if (aData.getBooleanExtra("isFromDetailActivityCreateContact", false)) {
                operationAfterCreateContact(aData);
                return;
            }
            boolean isFromAddExistContact = aData.getBooleanExtra("extra_add_exist_contact", false);
            this.addToExsitContactAleady = isFromAddExistContact;
            if (isFromAddExistContact) {
                operationAfterAddToExistContact(aData);
                return;
            }
            Contact contact = ((ContactsApplication) getApplication()).getContactAndReset(aData.getLongExtra("requestid", -1));
            if (contact != null) {
                this.mContactData = contact;
                updateHeaderView();
                if (this.mDetailFragment != null) {
                    this.mDetailFragment.setContactData(this.mContactData);
                }
            }
        }
    }

    private void operationAfterCreateContact(Intent aData) {
        this.mDetailsLaunchedBeforeContactSaved = true;
        this.mIsNoNamedContact = false;
        getIntent().putExtra("EXTRA_CALL_LOG_NONAME_CALL", this.mIsNoNamedContact);
        removeSingleFragment("Call_log_fragment_tag");
        initializeTabSpecs(getContext(), this.mSavedState);
        getView().findViewById(16908307).setVisibility(0);
        Intent lServiceIntent = (Intent) aData.getParcelableExtra("serviceIntent");
        if (lServiceIntent == null || !"saveContact".equals(lServiceIntent.getAction())) {
            Uri uri = aData.getData();
            if (uri != null) {
                this.mLoaderFragment.loadUri(uri, false, (Fragment) this);
            }
        } else {
            lServiceIntent.setClass(getContext(), ContactSaveService.class);
            getActivity().startService(lServiceIntent);
        }
        this.mNumberType = 2;
        if (isShowSplitMenu()) {
            changeCustomMenuGroup(getCurrentSelected());
        }
    }

    private void operationAfterAddToExistContact(Intent aData) {
        getIntent().removeExtra("EXTRA_CALL_LOG_NONAME_CALL");
        this.mIsNoNamedContact = false;
        getIntent().putExtra("EXTRA_CALL_LOG_NONAME_CALL", this.mIsNoNamedContact);
        removeSingleFragment("Call_log_fragment_tag");
        initializeTabSpecs(getContext(), this.mSavedState);
        getView().findViewById(16908307).setVisibility(0);
        Uri uri = aData.getData();
        if (uri != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 260;
            msg.obj = uri;
            this.mHandler.sendMessageDelayed(msg, 0);
        }
        this.mNumberType = 2;
        if (isShowSplitMenu()) {
            changeCustomMenuGroup(getCurrentSelected());
        }
    }

    private void initFragmentByAttashType(Fragment fragment) {
        if (fragment instanceof ContactDetailFragment) {
            this.mDetailFragment = (ContactDetailFragment) fragment;
            if (this.mLoaderFragment != null) {
                this.mLoaderFragment.setContactLoadedListener(this.mDetailFragment);
            }
            this.mDetailFragment.setFragment(this);
            if (this.mDetailFragment.getRcsCust() != null) {
                this.mDetailFragment.getRcsCust().setSelectedPhoneNumber(getIntent().getStringExtra("phoneNumber"));
            }
            this.mDetailFragment.setQueryPhoneNumberListener(this);
            this.mDetailFragment.setQRcodeEntryListener(this);
        } else if (fragment instanceof CallLogDetailFragment) {
            this.mCallLogDetailFragment = (CallLogDetailFragment) fragment;
            this.mCallLogDetailFragment.setFragment(this);
            this.mCallLogDetailFragment.setUpdateCalllogsListener(this);
            if (this.mDetailFragment != null) {
                onQueryPhoneNumberCompleted(this.mDetailFragment.getPhoneNumbers());
            }
        }
    }

    private void setCalllogsUpdateListener() {
        if (this.mCallLogDetailFragment != null) {
            this.mCallLogDetailFragment.setCalllogsUpdateListener(new CalllogsUpdateCallBack() {
                public void onUpdateFinished(boolean noData) {
                    boolean z = false;
                    if (ContactInfoFragment.this.isAdded()) {
                        boolean z2;
                        MenuItem -get36;
                        ContactInfoFragment contactInfoFragment = ContactInfoFragment.this;
                        if (noData) {
                            z2 = false;
                        } else {
                            z2 = true;
                        }
                        contactInfoFragment.mHasLog = z2;
                        if (HwLog.HWFLOW) {
                            HwLog.i("ContactInfoFragment", "onUpdateFinished,noData=" + noData);
                        }
                        if (ContactInfoFragment.this.mIsNoNamedContact) {
                            contactInfoFragment = ContactInfoFragment.this;
                            if (noData) {
                                z2 = false;
                            } else {
                                z2 = true;
                            }
                            contactInfoFragment.isMenuDelLogEnabled = z2;
                            if (ContactInfoFragment.this.mPopupMenu != null) {
                                ContactInfoFragment.this.prepareOptionMenu(ContactInfoFragment.this.mPopupMenu.getMenu());
                            }
                        }
                        if ((3 == ContactInfoFragment.this.mNumberType || 4 == ContactInfoFragment.this.mNumberType) && (CommonUtilMethods.isSimplifiedModeEnabled() || MultiUsersUtils.isCurrentUserGuest())) {
                            boolean clickable = !noData;
                            if (MultiUsersUtils.isCurrentUserGuest()) {
                                clickable = false;
                            }
                            if (ContactInfoFragment.this.menuMenu != null) {
                                -get36 = ContactInfoFragment.this.menuMenu;
                                if (noData) {
                                    z2 = false;
                                } else {
                                    z2 = true;
                                }
                                -get36.setEnabled(z2);
                            }
                            if (ContactInfoFragment.this.isShowSplitMenu()) {
                                ContactInfoFragment.this.mSplitMenuMenu.setClickable(clickable);
                                ContactInfoFragment.this.mSplitMenuMenu.findViewById(R.id.contact_menuitem_text).setEnabled(clickable);
                                if (MultiUsersUtils.isCurrentUserGuest()) {
                                    ContactInfoFragment.this.mSplitMenuMenu.findViewById(R.id.contact_menuitem_icon).setEnabled(clickable);
                                }
                            }
                        }
                        if (ContactInfoFragment.this.menuDelLog != null) {
                            -get36 = ContactInfoFragment.this.menuDelLog;
                            if (noData) {
                                z2 = false;
                            } else {
                                z2 = true;
                            }
                            -get36.setEnabled(z2);
                        }
                        contactInfoFragment = ContactInfoFragment.this;
                        if (noData) {
                            z2 = false;
                        } else {
                            z2 = true;
                        }
                        contactInfoFragment.setSplitMenuDelLogAvailablity(z2);
                        ContactInfoFragment.this.loadingCallLog = false;
                        ContactInfoFragment contactInfoFragment2;
                        if (ContactInfoFragment.this.mIsNoNamedContact) {
                            contactInfoFragment2 = ContactInfoFragment.this;
                            if (!noData) {
                                z = true;
                            }
                            contactInfoFragment2.mHasCalllogConInfo = z;
                        } else {
                            contactInfoFragment2 = ContactInfoFragment.this;
                            if (!noData) {
                                z = true;
                            }
                            contactInfoFragment2.mHasCalllogConInfo = z;
                            if (noData) {
                                ContactInfoFragment.this.asynchronousQueryFrequent();
                            }
                        }
                        Activity activity = ContactInfoFragment.this.getActivity();
                        if (activity != null) {
                            activity.invalidateOptionsMenu();
                        }
                    }
                }
            });
        }
    }

    private String checkNumFromBlackList(String number, String originMarkInfo) {
        if (BlacklistCommonUtils.checkPhoneNumberFromBlockItem(Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService")), number) && shouldShowBlackListMenuOption()) {
            return getResources().getString(R.string.added_to_blacklist);
        }
        return originMarkInfo;
    }

    public boolean isNeedUpdateWindows() {
        return this.mIsNeedUpdateWindows;
    }

    private void setupCallLogDetailFragment() {
        if (getChildFragmentManager() != null) {
            if (getChildFragmentManager().findFragmentByTag("Call_log_fragment_tag") == null) {
                this.mCallLogDetailFragment = CallLogDetailFragment.newInstance(getActivity());
            } else {
                Fragment fragment = getChildFragmentManager().findFragmentByTag("Call_log_fragment_tag");
                if (fragment instanceof CallLogDetailFragment) {
                    this.mCallLogDetailFragment = (CallLogDetailFragment) fragment;
                } else {
                    this.mCallLogDetailFragment = CallLogDetailFragment.newInstance(getActivity());
                }
            }
            addSingleFragment("Call_log_fragment_tag");
            initFragmentByAttashType(this.mCallLogDetailFragment);
            setCalllogsUpdateListener();
            getView().findViewById(16908307).setVisibility(8);
        }
    }

    private void setupProfileDetailFragment() {
        if (getChildFragmentManager() != null) {
            if (getChildFragmentManager().findFragmentByTag("Contact_detail_fragment_tag") == null) {
                this.mDetailFragment = ContactDetailFragment.newInstance(getActivity());
            } else {
                Fragment fragment = getChildFragmentManager().findFragmentByTag("Contact_detail_fragment_tag");
                if (fragment instanceof ContactDetailFragment) {
                    this.mDetailFragment = (ContactDetailFragment) fragment;
                } else {
                    this.mDetailFragment = ContactDetailFragment.newInstance(getActivity());
                }
            }
            addSingleFragment("Contact_detail_fragment_tag");
            initFragmentByAttashType(this.mDetailFragment);
            getView().findViewById(16908307).setVisibility(8);
        }
    }

    private void registerMotionRecognition() {
        if (!((UserManager) getContext().getSystemService("user")).isManagedProfile()) {
            if (HwLog.HWFLOW) {
                HwLog.w("ContactInfoFragment", "current user is not AFW, register notion recognition");
            }
            if (!this.mIsMotionRecognitionStarted && MotionRecognition.isMotionClassExists() && MotionRecognition.isMotionRecoApkExist(getContext())) {
                startMotionRecognition();
                this.mIsMotionRecognitionStarted = true;
            }
        }
    }

    private void configureGradientViewHeights() {
        int[] gradientColors = new int[]{0, Integer.MIN_VALUE};
        GradientDrawable titleGradientDrawable = new GradientDrawable(Orientation.TOP_BOTTOM, gradientColors);
        GradientDrawable actionBarGradientDrawable = new GradientDrawable(Orientation.BOTTOM_TOP, gradientColors);
        this.mTitleGradientView.setBackground(titleGradientDrawable);
        this.mActionBarGradientView.setBackground(actionBarGradientDrawable);
        int actionBarSize = ContactDpiAdapter.getActionbarHeight(getContext());
        LayoutParams actionBarGradientLayoutParams = (LayoutParams) this.mActionBarGradientView.getLayoutParams();
        actionBarGradientLayoutParams.height = actionBarSize * 2;
        this.mActionBarGradientView.setLayoutParams(actionBarGradientLayoutParams);
        LayoutParams titleGradientLayoutParams = (LayoutParams) this.mTitleGradientView.getLayoutParams();
        titleGradientLayoutParams.height = actionBarSize * 2;
        this.mTitleGradientView.setLayoutParams(titleGradientLayoutParams);
    }

    public void setResetFlag(boolean flag) {
        this.mResetFlag = flag;
    }

    private void setSplitMenuVisibility(int tabPagerIndex, View... menuViews) {
        for (int i = 0; i < menuViews.length; i++) {
            if (menuViews[i] != null) {
                menuViews[i].setVisibility(tabPagerIndex == 0 ? 0 : 8);
            }
        }
    }

    private void setSplitMenuDelLogAvailablity(boolean isAvailable) {
        if (isShowSplitMenu() && this.mSplitMenuDelLog != null && this.menuDelLogImageView != null && this.menuDelLogTextView != null) {
            this.mSplitMenuDelLog.setClickable(isAvailable);
            this.menuDelLogImageView.setEnabled(isAvailable);
            this.menuDelLogTextView.setEnabled(isAvailable);
        }
    }

    private float getHeadMinHeight() {
        float defaultVal = getResources().getDimension(R.dimen.detail_header_min_height);
        Activity act = getActivity();
        if (act == null) {
            return defaultVal;
        }
        if (this.mIsNeedUpdateWindows || (isSplitActivity(act) && ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode()))) {
            defaultVal = getResources().getDimension(R.dimen.detail_header_min_split_height);
        }
        return defaultVal;
    }

    private void handleDeletionInteraction(Uri contactUri) {
        Activity act = getActivity();
        if (isSplitActivity(act)) {
            FragmentManager fm;
            if (act instanceof PeopleActivity) {
                fm = ((PeopleActivity) act).getFrameFragmentManager();
            } else {
                fm = ((VoiceSearchResultActivity) act).getFrameFragmentManager();
            }
            int stackEntryCount = 1;
            if (fm != null) {
                stackEntryCount = fm.getBackStackEntryCount();
            }
            ContactDeletionInteraction.start(act, contactUri, true, ContactSplitUtils.getColumnsNumber(act, 2, act.isInMultiWindowMode()), ContactSplitUtils.getColumnsNumber(act, 1, act.isInMultiWindowMode()), stackEntryCount);
            return;
        }
        ContactDeletionInteraction.start(act, contactUri, true, true);
    }

    private void updateSplitMenuDelLogState(int tabPagerIndex) {
        int i = 0;
        if (tabPagerIndex == 1) {
            setMenuDelLogAvailablity();
        }
        if (this.mSplitMenuDelLog != null) {
            View view = this.mSplitMenuDelLog;
            if (tabPagerIndex == 0) {
                i = 8;
            }
            view.setVisibility(i);
        }
    }

    private void initSplitMenuGroup() {
        this.mSplitMenuStar = getView().findViewById(R.id.contact_menuitem_star);
        setContactDetailActivityImmersionMenu(getContext(), this.mSplitMenuStar);
        this.mSplitMenuGroup.add(this.mSplitMenuStar);
        this.mSplitMenuEdit = getView().findViewById(R.id.contact_menuitem_edit);
        setContactDetailActivityImmersionMenu(getContext(), this.mSplitMenuEdit);
        this.mSplitMenuGroup.add(this.mSplitMenuEdit);
        this.mSplitMenuMenu = getView().findViewById(R.id.contact_menuitem_menu);
        setContactDetailActivityImmersionMenu(getContext(), this.mSplitMenuMenu);
        this.mSplitMenuGroup.add(this.mSplitMenuMenu);
        this.mSplitMenuCreate = getView().findViewById(R.id.contact_menuitem_create_contact);
        setContactDetailActivityImmersionMenu(getContext(), this.mSplitMenuCreate);
        this.mSplitMenuGroup.add(this.mSplitMenuCreate);
        this.mSplitMenuSave = getView().findViewById(R.id.contact_menuitem_save_exist_contact);
        setContactDetailActivityImmersionMenu(getContext(), this.mSplitMenuSave);
        this.mSplitMenuGroup.add(this.mSplitMenuSave);
        this.mSplitMenuDelLog = getView().findViewById(R.id.contact_menuitem_delete_calllog);
        setContactDetailActivityImmersionMenu(getContext(), this.mSplitMenuDelLog);
        this.mSplitMenuGroup.add(this.mSplitMenuDelLog);
        if (this.mSplitMenuDelLog != null) {
            this.menuDelLogImageView = (ImageView) this.mSplitMenuDelLog.findViewById(R.id.contact_menuitem_icon);
            this.menuDelLogTextView = (TextView) this.mSplitMenuDelLog.findViewById(R.id.contact_menuitem_text);
        }
        if (this.isUnKnownNumberCall) {
            setSplitMenuEnable(false, this.mSplitMenuSave, this.mSplitMenuCreate, this.mSplitMenuMenu);
        }
    }

    private void setSplitMenuEnable(boolean isEnabled, View... menuViews) {
        for (int i = 0; i < menuViews.length; i++) {
            if (menuViews[i] != null) {
                menuViews[i].findViewById(R.id.contact_menuitem_icon).setEnabled(isEnabled);
                menuViews[i].findViewById(R.id.contact_menuitem_text).setEnabled(isEnabled);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setContactDetailActivityImmersionMenu(Context context, View menuItem) {
        if (context != null && menuItem != null && ImmersionUtils.replaceIconCondition(context)) {
            switch (menuItem.getId()) {
                case R.id.contact_menuitem_create_contact:
                    ImmersionUtils.setImmersionImageView(context, menuItem, R.id.contact_menuitem_icon, ImmersionUtils.getImmersionImageID(context, R.drawable.ic_new_contact_light, R.drawable.ic_new_contact));
                    break;
                case R.id.contact_menuitem_star:
                    ImmersionUtils.setImmersionImageView(context, menuItem, R.id.contact_menuitem_icon, ImmersionUtils.getImmersionImageID(context, R.drawable.menu_sharred_unclick_light, R.drawable.menu_sharred_unclick));
                    break;
                case R.id.contact_menuitem_edit:
                    ImmersionUtils.setImmersionImageView(context, menuItem, R.id.contact_menuitem_icon, ImmersionUtils.getImmersionImageID(context, R.drawable.contacts_ic_edit_light, R.drawable.contacts_ic_edit));
                    break;
                case R.id.contact_menuitem_save_exist_contact:
                    ImmersionUtils.setImmersionImageView(context, menuItem, R.id.contact_menuitem_icon, ImmersionUtils.getImmersionImageID(context, R.drawable.ic_add_contact_light, R.drawable.ic_add_contact));
                    break;
                case R.id.contact_menuitem_menu:
                    ImmersionUtils.setImmersionImageView(context, menuItem, R.id.contact_menuitem_icon, ImmersionUtils.getImmersionImageID(context, R.drawable.ic_menu_light, R.drawable.ic_menu));
                    break;
                case R.id.contact_menuitem_delete_calllog:
                    ImmersionUtils.setImmersionImageView(context, menuItem, R.id.contact_menuitem_icon, ImmersionUtils.getImmersionImageID(context, R.drawable.ic_trash_normal_light, R.drawable.ic_trash_normal));
                    break;
            }
        }
    }

    private void setSplitToolbarMenuGroup() {
        for (int i = 0; i < this.mSplitMenuGroup.size(); i++) {
            setSplitToolbarMenuItem((View) this.mSplitMenuGroup.get(i));
        }
        if (this.isUnKnownNumberCall) {
            setViewUnClickable(this.mSplitMenuMenu, this.mSplitMenuCreate, this.mSplitMenuSave, this.mSplitMenuDelLog);
        }
    }

    private void setSplitToolbarMenuItem(View menuItem) {
        if (menuItem != null) {
            ViewUtil.setStateListIcon(getApplicationContext(), menuItem.findViewById(R.id.contact_menuitem_icon), false);
            menuItem.setOnClickListener(this);
        }
    }

    private void setViewUnClickable(View... menuViews) {
        for (int i = 0; i < menuViews.length; i++) {
            if (menuViews[i] != null) {
                menuViews[i].setClickable(false);
            }
        }
    }

    private void setToolbarMenuText() {
        setToolbarMenuText(this.mSplitMenuStar, R.string.contacts_starred);
        setToolbarMenuText(this.mSplitMenuEdit, R.string.menu_editContact);
        setToolbarMenuText(this.mSplitMenuMenu, R.string.contacts_title_menu);
        setToolbarMenuText(this.mSplitMenuCreate, R.string.pickerNewContactText);
        setToolbarMenuText(this.mSplitMenuSave, R.string.contact_saveto_existed_contact_short);
        setToolbarMenuText(this.mSplitMenuDelLog, R.string.str_delete_call_log_entry);
    }

    private void setToolbarMenuText(View menuText, int textId) {
        if (menuText != null) {
            ((TextView) menuText.findViewById(R.id.contact_menuitem_text)).setText(textId);
        }
    }

    private void hideMenuItem(View... menuViews) {
        if (isShowSplitMenu()) {
            for (int i = 0; i < menuViews.length; i++) {
                if (menuViews[i] != null) {
                    menuViews[i].setVisibility(8);
                }
            }
        }
    }

    private void showPopup(View view) {
        if (this.mPopupMenu == null) {
            PopupMenu popup = new PopupMenu(getContext(), view);
            popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem aMenuItem) {
                    ContactInfoFragment.this.mPopupMenu = null;
                    ContactInfoFragment.this.processOptionMenuItemClick(aMenuItem);
                    return false;
                }
            });
            popup.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(PopupMenu menu) {
                    ContactInfoFragment.this.mPopupMenu = null;
                }
            });
            popup.getMenuInflater().inflate(R.menu.view_contact, popup.getMenu());
            prepareOptionMenu(popup.getMenu());
            this.mPopupMenu = popup;
            popup.show();
        }
    }

    public void closePopupMenu() {
        if (this.mPopupMenu != null) {
            this.mPopupMenu.dismiss();
        }
    }

    private void refreshLayout(int tabPagerIndex) {
        int buttomMenuCount = this.itemNum[tabPagerIndex];
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels > dm.heightPixels ? dm.heightPixels : dm.widthPixels;
        Activity act = getActivity();
        if (act != null && this.mSplitMenuContainer != null) {
            LinearLayout.LayoutParams layoutParams;
            int mSplitWidth = dm.widthPixels;
            if (ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode())) {
                mSplitWidth = dm.widthPixels / 2;
            }
            if (this.mIsFromDialpadOrDialer && !this.mIsNeedUpdateWindows) {
                mSplitWidth = dm.widthPixels;
            }
            int contentWidth = (screenWidth - ContactSplitUtils.dip2px(getContext(), 40.0f)) / 4;
            if ((contentWidth * buttomMenuCount) + ContactSplitUtils.dip2px(getContext(), 40.0f) > mSplitWidth) {
                layoutParams = new LinearLayout.LayoutParams((mSplitWidth - ContactSplitUtils.dip2px(getContext(), 40.0f)) / buttomMenuCount, -2);
            } else {
                layoutParams = new LinearLayout.LayoutParams(contentWidth, -2);
            }
            for (int i = 0; i < this.mSplitMenuGroup.size(); i++) {
                View view = this.mSplitMenuContainer.getChildAt(i);
                if (view.getVisibility() == 0) {
                    view.setLayoutParams(layoutParams);
                }
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contact_menuitem_create_contact:
                if (!this.mHasClicked) {
                    this.mHasClicked = true;
                    this.mHandler.sendEmptyMessageDelayed(2, 600);
                    StatisticalHelper.sendReport((int) AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, "3");
                    createContact();
                    return;
                }
                return;
            case R.id.contact_menuitem_star:
                onClickMenuItemStar();
                return;
            case R.id.contact_menuitem_edit:
                onClickMenuItemEdit();
                return;
            case R.id.contact_menuitem_save_exist_contact:
                StatisticalHelper.report(5003);
                addExistContact();
                return;
            case R.id.contact_menuitem_menu:
                StatisticalHelper.report(1154);
                showPopup(v);
                return;
            case R.id.contact_menuitem_delete_calllog:
                if (this.mCallLogDetailFragment != null) {
                    this.mCallLogDetailFragment.onMenuItemClicked(v.getId());
                    return;
                }
                return;
            default:
                return;
        }
    }

    private boolean isShowSplitMenu() {
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            return false;
        }
        return !this.mIsFromDialpadOrDialer ? isSplitActivity(getActivity()) : true;
    }

    private boolean isSplitActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        boolean ret = false;
        if ((activity instanceof PeopleActivity) || (activity instanceof VoiceSearchResultActivity)) {
            ret = true;
        }
        return ret;
    }

    private void showSystemActionbar(Activity act) {
        if (act != null && act.getActionBar() != null) {
            if (getResources().getConfiguration().orientation == 1) {
                View actionBar = act.getWindow().getDecorView().findViewById(getResources().getIdentifier("action_bar_container", "id", "android"));
                if (actionBar != null) {
                    actionBar.setVisibility(8);
                }
            } else {
                act.getActionBar().setTitle("");
                act.getActionBar().setDisplayOptions(28, 28);
            }
        }
    }

    private void adjustLayout() {
        if (this.mIsNeedUpdateWindows) {
            adjustMarginTop();
            View grayView = getView().findViewById(R.id.gray_view);
            grayView.setVisibility(0);
            grayView.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    ContactInfoFragment.this.getActivity().finish();
                }
            });
        }
        Activity act = getActivity();
        if (isSplitActivity(act) && ContactSplitUtils.isSpiltTwoColumn(act, act.isInMultiWindowMode())) {
            adjustMarginTop();
        }
    }

    private void updateWindowsParams() {
        Window window = getActivity().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = 80;
        params.flags = 32;
        window.setFlags(262144, 262144);
        params.height = ContactDpiAdapter.getActivityContentHeights(getActivity());
        window.setAttributes(params);
    }

    private void initSplitMenuContainer() {
        if (!this.mIsSplitMenuContainerLoaded) {
            ViewStub stub = (ViewStub) getView().findViewById(R.id.contacts_detail_menu_container);
            if (stub != null) {
                showSplitMenuInMultiWindowMode();
                stub.inflate();
                this.mSplitMenuContainer = (ViewGroup) getView().findViewById(R.id.menu_container_for_split);
                this.mIsSplitMenuContainerLoaded = true;
            }
        }
    }

    private void changeCustomMenuGroup(int tabPagerIndex) {
        switch (this.mNumberType) {
            case 1:
                setSplitMenuVisibility(tabPagerIndex, this.mSplitMenuMenu, this.mSplitMenuSave, this.mSplitMenuCreate);
                this.itemNum[0] = 3;
                hideMenuItem(this.mSplitMenuEdit, this.mSplitMenuStar);
                break;
            case 2:
                setSplitMenuVisibility(tabPagerIndex, this.mSplitMenuEdit, this.mSplitMenuMenu, this.mSplitMenuStar);
                updateSplitMenuDelLogState(tabPagerIndex);
                this.itemNum[0] = 3;
                hideMenuItem(this.mSplitMenuSave, this.mSplitMenuCreate);
                break;
            case 3:
            case 4:
                updateSplitMenuDelLogState(tabPagerIndex);
                setSplitMenuVisibility(tabPagerIndex, this.mSplitMenuMenu);
                this.itemNum[0] = 1;
                hideMenuItem(this.mSplitMenuCreate, this.mSplitMenuSave, this.mSplitMenuEdit, this.mSplitMenuStar);
                break;
            case 5:
                setSplitMenuVisibility(tabPagerIndex, this.mSplitMenuMenu, this.mSplitMenuCreate, this.mSplitMenuSave);
                updateSplitMenuDelLogState(tabPagerIndex);
                this.itemNum[0] = 3;
                hideMenuItem(this.mSplitMenuEdit, this.mSplitMenuStar);
                break;
            case 6:
                setSplitMenuVisibility(tabPagerIndex, this.mSplitMenuMenu, this.mSplitMenuEdit);
                updateSplitMenuDelLogState(tabPagerIndex);
                this.itemNum[0] = 2;
                hideMenuItem(this.mSplitMenuStar, this.mSplitMenuSave, this.mSplitMenuCreate);
                break;
        }
        refreshLayout(tabPagerIndex);
    }

    private void showSplitMenuInMultiWindowMode() {
        ViewGroup menu = (ViewGroup) getView().findViewById(R.id.menu_title);
        View divider = getView().findViewById(R.id.menu_divider);
        boolean isLand = getResources().getConfiguration().orientation == 2;
        if (getActivity().isInMultiWindowMode() && isLand) {
            if (divider != null) {
                divider.setVisibility(0);
            }
            if (menu != null) {
                menu.setVisibility(0);
            }
        }
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        if (!isInMultiWindowMode) {
            initQRcodeIndicationer();
        }
    }
}
