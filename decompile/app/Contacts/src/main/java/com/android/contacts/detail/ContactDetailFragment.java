package com.android.contacts.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ParseException;
import android.net.Uri;
import android.net.Uri.Builder;
import android.net.WebAddress;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.Data;
import android.provider.MediaStore.Audio.Media;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.services.core.AMapException;
import com.android.contacts.CallUtil;
import com.android.contacts.Collapser;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GroupMetaData;
import com.android.contacts.MoreContactUtils;
import com.android.contacts.MotionRecognition;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactInfoFragment;
import com.android.contacts.activities.ContactInfoFragment.OnStarBtnClickedInterface;
import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.detail.ContactDetailAdapter.AddConnectionViewEntry;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.detail.ContactDetailAdapter.ViewEntry;
import com.android.contacts.editor.SelectAccountDialogFragment;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.camcard.groups.CamcardGroup;
import com.android.contacts.hap.numbermark.CapabilityInfo;
import com.android.contacts.hap.numbermark.NumberMarkManager;
import com.android.contacts.hap.numbermark.NumberMarkManager.Callback;
import com.android.contacts.hap.numbermark.NumberMarkManager.CapabilityQueryCallback;
import com.android.contacts.hap.rcs.RcsCLIRBroadCastHelper;
import com.android.contacts.hap.rcs.detail.RcsContactDetailFragmentHelper;
import com.android.contacts.hap.roaming.IsPhoneNetworkRoamingUtils;
import com.android.contacts.hap.roaming.RoamingLearnManage;
import com.android.contacts.hap.roaming.RoamingPhoneGatherUtils;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimStateListener;
import com.android.contacts.hap.util.BackgroundViewCacher;
import com.android.contacts.hap.util.ContactStaticCache;
import com.android.contacts.hap.utils.BackgroundGenricHandler;
import com.android.contacts.hap.utils.VoLteStatusObserver;
import com.android.contacts.hap.utils.VoLteStatusObserver.CallBack;
import com.android.contacts.hap.utils.VtLteUtils;
import com.android.contacts.hap.widget.MultiShrinkScroller;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.Contact;
import com.android.contacts.model.ContactLoader.ContactLoadedListener;
import com.android.contacts.model.RawContact;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountType.DisplayLabelComparator;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.model.dataitem.DataItem;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.model.dataitem.EmailDataItem;
import com.android.contacts.model.dataitem.EventDataItem;
import com.android.contacts.model.dataitem.GroupMembershipDataItem;
import com.android.contacts.model.dataitem.ImDataItem;
import com.android.contacts.model.dataitem.NicknameDataItem;
import com.android.contacts.model.dataitem.NoteDataItem;
import com.android.contacts.model.dataitem.OrganizationDataItem;
import com.android.contacts.model.dataitem.PhoneDataItem;
import com.android.contacts.model.dataitem.RelationDataItem;
import com.android.contacts.model.dataitem.SipAddressDataItem;
import com.android.contacts.model.dataitem.StructuredNameDataItem;
import com.android.contacts.model.dataitem.StructuredPostalDataItem;
import com.android.contacts.model.dataitem.WebsiteDataItem;
import com.android.contacts.profile.ProfileUtils.ContactEntriesObject;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AccountsListAdapter.AccountListFilter;
import com.android.contacts.util.DataStatus;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.DrmUtils;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.LunarUtils;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.PhoneNumberFormatter;
import com.android.contacts.util.RadarIMonitorUpload;
import com.android.contacts.util.StructuredPostalUtils;
import com.google.android.gms.R;
import com.google.android.gms.actions.SearchIntents;
import com.google.android.gms.common.Scopes;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.huawei.android.media.RingtoneManagerEx;
import com.huawei.cust.HwCustUtils;
import huawei.android.widget.ListPopupWindow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContactDetailFragment extends Fragment implements com.android.contacts.editor.SelectAccountDialogFragment.Listener, OnItemClickListener, SimStateListener, ContactLoadedListener, CallBack {
    private static final String ROOT_EXTERNAL = Media.EXTERNAL_CONTENT_URI.toString();
    private static final String TAG = ContactDetailFragment.class.getSimpleName();
    private static HwCustContactDetailFragmentHelper mConDetFragmentHelper;
    private boolean hasManualPrimaryPhoneEntry = false;
    private boolean isNeedFlush = true;
    private ContactDetailAdapter mAdapter;
    private Contact mCachedContactData = null;
    private EntriesObject mCachedEntriesObject = null;
    private String mCachedSimAccountType = null;
    private CapabilityCallback mCapabilityCallback = new CapabilityCallback();
    private String[] mCapabilityQueryNumbers = null;
    private Contact mContactData;
    private final Listener mContactDetailFragmentListener = new Listener() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onItemClicked(Intent intent) {
            if (intent != null && ContactDetailFragment.this.isAdded()) {
                try {
                    if ((EmuiFeatureManager.isSystemVoiceCapable() || !("android.intent.action.CALL_PRIVILEGED".equals(intent.getAction()) || "android.intent.action.CALL".equals(intent.getAction()))) && !ContactDetailFragment.this.mIsIntentCalled) {
                        StatisticalHelper.report(AMapException.CODE_AMAP_SERVICE_UNKNOWN_ERROR);
                        if ("android.intent.action.CALL_PRIVILEGED".equals(intent.getAction()) || "android.intent.action.CALL".equals(intent.getAction())) {
                            if (ContactDetailFragment.mConDetFragmentHelper == null || !ContactDetailFragment.mConDetFragmentHelper.checkAndInitCall(ContactDetailFragment.this.getContext(), intent)) {
                                if (!ContactDetailFragment.this.mIsIntentCalledOriginal) {
                                    StatisticalHelper.reportDialPortal(ContactDetailFragment.this.getContext(), 1);
                                    if (HwLog.HWFLOW) {
                                        HwLog.i(ContactDetailFragment.TAG, "onItemClicked DIAL_TYPE_CONTACT_DETAIL the subid is:0");
                                    }
                                }
                                StatisticalHelper.sendReport(3000, 0);
                            } else {
                                return;
                            }
                        }
                        ContactDetailFragment.this.mIsIntentCalledOriginal = true;
                        if (!RoamingPhoneGatherUtils.parseSingleCardRoamingPhoneItemData(ContactDetailFragment.this.getContext(), intent, 3)) {
                            singleCardSaveRaomingLearn(ContactDetailFragment.this.getContext(), intent);
                            startAcitvityWhenNotDisposeRoaming(intent);
                            ContactDetailFragment.this.mIsIntentCalled = true;
                        }
                    }
                } catch (ActivityNotFoundException e) {
                    HwLog.e(ContactDetailFragment.TAG, "No activity found for intent");
                    Toast.makeText(ContactDetailFragment.this.getContext(), R.string.quickcontact_missing_app_Toast, 0).show();
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void singleCardSaveRaomingLearn(Context context, Intent pIntent) {
            if ((QueryUtil.isSystemAppForContacts() ? "android.intent.action.CALL_PRIVILEGED" : "android.intent.action.CALL").equals(pIntent.getAction()) && context != null && pIntent.getBooleanExtra(" pref_Original_Normalized_Number_Is_Null", false)) {
                String originalNumber = pIntent.getStringExtra("pref_Original_Numbe");
                String dialNumber = pIntent.getStringExtra("pref_Dial_Number");
                if (!(originalNumber == null || dialNumber == null)) {
                    RoamingLearnManage.saveRoamingLearnCarrier(context.getApplicationContext(), originalNumber, dialNumber);
                }
            }
        }

        private void startAcitvityWhenNotDisposeRoaming(Intent intent) {
            if ("android.intent.action.SENDTO".equals(intent.getAction())) {
                if ("mailto".equals(intent.getScheme())) {
                    StatisticalHelper.report(1119);
                    ExceptionCapture.reportScene(86);
                }
                if ("imto".equals(intent.getScheme())) {
                    StatisticalHelper.report(1114);
                }
            }
            if ("android.intent.action.VIEW".equals(intent.getAction())) {
                if (!TextUtils.isEmpty(intent.getDataString()) && intent.getDataString().contains("hangouts")) {
                    StatisticalHelper.report(1114);
                } else if ("vnd.android.cursor.item/vnd.com.tencent.mm.plugin.sns.timeline".equals(intent.getType())) {
                    StatisticalHelper.report(1178);
                } else if ("vnd.android.cursor.item/vnd.com.tencent.mm.chatting.profile".equals(intent.getType())) {
                    StatisticalHelper.report(1180);
                } else if ("vnd.android.cursor.item/vnd.com.tencent.mobileqq.voicecall.profile".equals(intent.getType())) {
                    StatisticalHelper.report(1181);
                } else if ("vnd.android.cursor.item/vnd.com.whatsapp.voip.call".equals(intent.getType())) {
                    StatisticalHelper.report(1182);
                } else if ("vnd.android.cursor.item/vnd.com.whatsapp.profile".equals(intent.getType())) {
                    StatisticalHelper.report(1183);
                }
            }
            if ("android.intent.action.SEARCH".equals(intent.getAction())) {
                intent.putExtra("SEARCH_MODE", true);
            }
            ContactDetailFragment.this.startActivity(intent);
        }

        public void onCreateRawContactRequested(ArrayList<ContentValues> values, AccountWithDataSet account) {
            if (ContactDetailFragment.this.getActivity() != null && ContactDetailFragment.this.isAdded()) {
                Toast.makeText(ContactDetailFragment.this.getActivity(), R.string.toast_making_personal_copy, 1).show();
                ContactDetailFragment.this.getActivity().startService(ContactSaveService.createNewRawContactIntent(ContactDetailFragment.this.getActivity(), values, account, ContactDetailActivity.class, "android.intent.action.VIEW"));
            }
        }
    };
    protected ContactDetailLayoutController mContactDetailLayoutController;
    private Context mContext;
    private int mDefaultSimcard;
    private View mEmptyView;
    private boolean mEntriesBuilt = false;
    private ArrayList<String> mFormattedPhoneNum = new ArrayList();
    private HashMap<String, Uri> mFormattedPhoneNumUri = new HashMap();
    private OnDragListener mForwardDragToListView = new OnDragListener() {
        public boolean onDrag(View v, DragEvent event) {
            if (ContactDetailFragment.this.mListView == null) {
                return false;
            }
            ContactDetailFragment.this.mListView.dispatchDragEvent(event);
            return true;
        }
    };
    private OnTouchListener mForwardTouchToListView = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (ContactDetailFragment.this.mListView == null) {
                return false;
            }
            ContactDetailFragment.this.mListView.dispatchTouchEvent(event);
            return true;
        }
    };
    protected Fragment mFragment;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (ContactDetailFragment.this.mCachedEntriesObject != null && ContactDetailFragment.this.isAdded()) {
                Uri lPickedUri = msg.obj;
                ContactDetailFragment.this.mRingtonePosition = ContactDetailFragment.this.getRingtonePosition();
                if (-1 != ContactDetailFragment.this.mRingtonePosition) {
                    if (lPickedUri != null) {
                        if (RingtoneManager.isDefault(lPickedUri)) {
                            ((DetailViewEntry) ContactDetailFragment.this.mCachedEntriesObject.mAllEntries.get(ContactDetailFragment.this.mRingtonePosition)).data = ContactDetailFragment.this.getString(R.string.default_ringtone);
                            ContactDetailFragment.this.mRingtoneString = "";
                        } else {
                            Ringtone lRingtone = RingtoneManager.getRingtone(ContactDetailFragment.this.mContext, lPickedUri);
                            if (lRingtone != null) {
                                ((DetailViewEntry) ContactDetailFragment.this.mCachedEntriesObject.mAllEntries.get(ContactDetailFragment.this.mRingtonePosition)).data = lRingtone.getTitle(ContactDetailFragment.this.mContext);
                            }
                            ContactDetailFragment.this.mRingtoneString = lPickedUri.toString();
                        }
                        ContactDetailFragment.this.updateRingtoneOnContact();
                    } else {
                        ContactDetailFragment.this.mRingtoneString = "-1";
                        ContactDetailFragment.this.updateRingtoneOnContact();
                    }
                }
            }
        }
    };
    private boolean mHasPhone;
    private boolean mHasSip;
    private boolean mHighligh = false;
    private DetailViewEntry mIPEntry;
    private LayoutInflater mInflater;
    AlertDialog mIpDialog = null;
    private boolean mIsFirstBuildRingtoneEntry = true;
    private boolean mIsFirstSimEnabled;
    private boolean mIsIPDialogVisible;
    private boolean mIsIntentCalled;
    private boolean mIsIntentCalledOriginal;
    private boolean mIsNoNamedContact = false;
    private boolean mIsSMSDialogVisible;
    private boolean mIsSecondSimEnabled;
    private boolean mIsUniqueEmail;
    private boolean mIsUniqueNumber;
    private boolean mIsVtLteOn = false;
    private Parcelable mListState;
    private ListView mListView;
    private Listener mListener;
    private final Object mLock = new Object();
    private Uri mLookupUri;
    MotionRecognition mMReco = null;
    private MarkDefaultRunnable mMarkDefault;
    private NumberMarkManager mMarkManager;
    AlertDialog mMessageDialog = null;
    private boolean mMultiSimEnable;
    private OnStarBtnClickedInterface mOnStarBtnClickedInterface = new OnStarBtnClickedInterface() {
        public void onStarBtnClicked() {
            ContactDetailFragment.this.isNeedFlush = false;
        }
    };
    private HashMap<String, String> mPhoneNumberCountryIsoHM = new HashMap();
    private ArrayList<String> mPhoneNumbers = new ArrayList();
    private HashMap<String, Uri> mPhoneNumbersUri = new HashMap();
    private ListPopupWindow mPopup;
    private final QuickFix[] mPotentialQuickFixes = new QuickFix[]{new MakeLocalCopyQuickFix(), new AddToMyContactsQuickFix()};
    private String mPrimaryNumber;
    private Uri mPrimaryPhoneUri = null;
    private QRcodeEntryListener mQRcodeEntryListener;
    private QueryPhoneNumberListener mQueryPhoneNumberListener;
    private QuickFix mQuickFix;
    private RcsCLIRBroadCastHelper mRcsCLIRBroadCastHelper;
    private RcsContactDetailFragmentHelper mRcsCust = null;
    private SharedPreferences mRingtoneFilePathPreference;
    private int mRingtonePosition;
    private String mRingtoneString;
    private Uri mSelectRingtoneUri;
    private int mSimPresence = 0;
    private ContactEntriesObject mSubEntriesObject;
    private OnScrollListener mVerticalScrollListener;
    private View mView;
    private ViewEntryDimensions mViewEntryDimensions;
    private BackgroundViewCacher mViewInflator;
    private VoLteStatusObserver mVoLteStatusObserver;
    private final BroadcastReceiver sdCardUnmountBroadcastRec = new BroadcastReceiver() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.MEDIA_UNMOUNTED".equals(intent.getAction()) && ContactDetailFragment.this.isAdded() && ContactDetailFragment.this.mRingtoneString != null && !"-1".equals(ContactDetailFragment.this.mRingtoneString) && ContactDetailFragment.this.mRingtoneString.contains(ContactDetailFragment.ROOT_EXTERNAL) && ContactDetailFragment.this.mCachedEntriesObject != null && -1 != ContactDetailFragment.this.getRingtonePosition()) {
                ContactDetailFragment.this.mCachedEntriesObject.mAllEntries.remove(ContactDetailFragment.this.getRingtonePosition());
                ContactDetailFragment.this.mCachedEntriesObject.mRingtoneEntries.clear();
                Collapser.collapseList(ContactDetailFragment.this.mCachedEntriesObject.mRingtoneEntries, false);
                ContactDetailFragment.this.buildRingtoneEntries(ContactDetailFragment.this.mCachedSimAccountType, false, ContactDetailFragment.this.mCachedEntriesObject, ContactDetailFragment.this.mContactData);
                ContactDetailFragment.this.flattenList(ContactDetailFragment.this.mCachedEntriesObject.mRingtoneEntries, ContactDetailFragment.this.mCachedEntriesObject);
                if (!(ContactDetailFragment.this.mAdapter == null || ContactDetailFragment.this.mListView == null)) {
                    ContactDetailFragment.this.mAdapter.setAllEntries(ContactDetailFragment.this.mCachedEntriesObject.mAllEntries);
                    ContactDetailFragment.this.mAdapter.notifyDataSetChanged();
                    ContactDetailFragment.this.mListView.invalidateViews();
                }
            }
        }
    };

    public interface QueryPhoneNumberListener {
        void onQueryPhoneNumberCompleted(ArrayList<String> arrayList);

        void onQueryPhoneNumberCompletedIso(HashMap<String, String> hashMap);
    }

    public interface QRcodeEntryListener {
        void getQRcodeEntryCompleted(HashMap<String, ArrayList<String>> hashMap);
    }

    public interface Listener {
        void onCreateRawContactRequested(ArrayList<ContentValues> arrayList, AccountWithDataSet accountWithDataSet);

        void onItemClicked(Intent intent);
    }

    private static abstract class QuickFix {
        public abstract void execute();

        public abstract boolean isApplicable();

        private QuickFix() {
        }
    }

    private class AddToMyContactsQuickFix extends QuickFix {
        private AddToMyContactsQuickFix() {
            super();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean isApplicable() {
            boolean z = false;
            if (ContactDetailFragment.this.mContactData == null || ContactDetailFragment.this.mContactData.isDirectoryEntry() || ContactDetailFragment.this.mContactData.getRawContacts() == null || ContactDetailFragment.this.mContactData.isUserProfile() || ContactDetailFragment.this.mContactData.getRawContacts().size() != 1) {
                return false;
            }
            List<GroupMetaData> groups = ContactDetailFragment.this.mContactData.getGroupMetaData();
            if (groups == null) {
                return false;
            }
            long defaultGroupId = ContactDetailFragment.this.getDefaultGroupId(groups);
            if (defaultGroupId == -1) {
                return false;
            }
            RawContact rawContact = (RawContact) ContactDetailFragment.this.mContactData.getRawContacts().get(0);
            AccountType type = rawContact.getAccountType(ContactDetailFragment.this.getContext());
            if (type == null || !type.areContactsWritable()) {
                return false;
            }
            boolean isInDefaultGroup = false;
            for (GroupMembershipDataItem dataItem : Iterables.filter(rawContact.getDataItems(), GroupMembershipDataItem.class)) {
                if (dataItem.getGroupRowId().longValue() == defaultGroupId) {
                    isInDefaultGroup = true;
                    break;
                }
            }
            if (!isInDefaultGroup) {
                z = true;
            }
            return z;
        }

        public void execute() {
            if (ContactDetailFragment.this.mContactData != null) {
                long defaultGroupId = ContactDetailFragment.this.getDefaultGroupId(ContactDetailFragment.this.mContactData.getGroupMetaData());
                if (defaultGroupId != -1) {
                    RawContactDeltaList contactDeltaList = ContactDetailFragment.this.mContactData.createRawContactDeltaList();
                    if (contactDeltaList != null) {
                        RawContactDelta rawContactEntityDelta = (RawContactDelta) contactDeltaList.get(0);
                        RawContactModifier.insertChild(rawContactEntityDelta, rawContactEntityDelta.getAccountType(AccountTypeManager.getInstance(ContactDetailFragment.this.mContext)).getKindForMimetype("vnd.android.cursor.item/group_membership")).setGroupRowId(defaultGroupId);
                        ContactDetailFragment.this.getActivity().startService(ContactSaveService.createSaveContactIntent(ContactDetailFragment.this.getActivity(), contactDeltaList, "", 0, false, ContactDetailFragment.this.getActivity().getClass(), "android.intent.action.VIEW", null));
                    }
                }
            }
        }
    }

    class CapabilityCallback implements Callback, CapabilityQueryCallback {
        CapabilityCallback() {
        }

        public void refresh(List<CapabilityInfo> list) {
            if (ContactDetailFragment.this.isAdded() && ContactDetailFragment.this.mCachedEntriesObject != null) {
                List<DetailViewEntry> entryList = new ArrayList();
                int index = 0;
                for (CapabilityInfo info : list) {
                    boolean z;
                    if (index == 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    entryList.add(info.buildViewEntry(z));
                    index++;
                }
                ArrayList<ViewEntry> lAllEntries = (ArrayList) ContactDetailFragment.this.mCachedEntriesObject.mAllEntries.clone();
                for (int i = lAllEntries.size() - 1; i >= 0; i--) {
                    ViewEntry entry = (ViewEntry) lAllEntries.get(i);
                    if ((entry instanceof DetailViewEntry) && "capability".equals(((DetailViewEntry) entry).mimetype)) {
                        lAllEntries.remove(i);
                    }
                }
                lAllEntries.addAll(entryList);
                if (ContactDetailFragment.this.mAdapter != null) {
                    ContactDetailFragment.this.mAdapter.setAllEntries(lAllEntries);
                }
                ContactDetailFragment.this.mCachedEntriesObject.mAllEntries = (ArrayList) lAllEntries.clone();
                if (ContactDetailFragment.this.mAdapter != null) {
                    ContactDetailFragment.this.mAdapter.notifyDataSetChanged();
                }
            }
        }

        public void onServiceConnected() {
            if (ContactDetailFragment.this.mCapabilityQueryNumbers != null) {
                ContactDetailFragment.this.mMarkManager.getCapabilityInfoAsync(ContactDetailFragment.this.mCapabilityQueryNumbers, this);
                ContactDetailFragment.this.mCapabilityQueryNumbers = null;
            }
        }
    }

    private static class EntriesObject {
        private ArrayList<ViewEntry> mAllEntries;
        private ArrayList<DetailViewEntry> mCompanyEntries;
        private ArrayList<DetailViewEntry> mEmailEntries;
        private ArrayList<DetailViewEntry> mEmerygencyEntries;
        private ArrayList<DetailViewEntry> mEventEntries;
        private ArrayList<DetailViewEntry> mGroupEntries;
        private ArrayList<DetailViewEntry> mHwSNSEntries;
        private ArrayList<DetailViewEntry> mIPCallEntries;
        private ArrayList<DetailViewEntry> mImEntries;
        private ArrayList<DetailViewEntry> mNameEntries;
        private ArrayList<DetailViewEntry> mNicknameEntries;
        private ArrayList<DetailViewEntry> mNoteEntries;
        private final Map<AccountType, List<DetailViewEntry>> mOtherEntriesMap;
        private ArrayList<DetailViewEntry> mPhoneEntries;
        private ArrayList<DetailViewEntry> mPhoneticNameEntries;
        private ArrayList<DetailViewEntry> mPositionEntries;
        private ArrayList<DetailViewEntry> mPostalEntries;
        private ArrayList<DetailViewEntry> mQQEntries;
        private ArrayList<String> mQrCompanys;
        private ArrayList<String> mQrEmails;
        private ArrayList<String> mQrIms;
        private ArrayList<String> mQrJobTitles;
        private ArrayList<String> mQrNames;
        private ArrayList<String> mQrNotes;
        private ArrayList<String> mQrNumbers;
        private ArrayList<String> mQrPostals;
        private ArrayList<String> mQrWebsites;
        private HashMap<String, ArrayList<String>> mQrcodeDataInfo;
        private ArrayList<DetailViewEntry> mRelationEntries;
        private ArrayList<DetailViewEntry> mRingtoneEntries;
        private ArrayList<DetailViewEntry> mSipEntries;
        private ArrayList<DetailViewEntry> mSkypeEntries;
        private ArrayList<DetailViewEntry> mStatusEntriesForIm;
        private ArrayList<DetailViewEntry> mWeChatEntries;
        private ArrayList<DetailViewEntry> mWebsiteEntries;
        private ArrayList<DetailViewEntry> mWhatsAppEntries;

        private EntriesObject() {
            this.mPhoneEntries = new ArrayList();
            this.mIPCallEntries = new ArrayList();
            this.mCompanyEntries = new ArrayList();
            this.mPositionEntries = new ArrayList();
            this.mEmailEntries = new ArrayList();
            this.mPostalEntries = new ArrayList();
            this.mImEntries = new ArrayList();
            this.mNameEntries = new ArrayList();
            this.mNicknameEntries = new ArrayList();
            this.mPhoneticNameEntries = new ArrayList();
            this.mGroupEntries = new ArrayList();
            this.mRelationEntries = new ArrayList();
            this.mNoteEntries = new ArrayList();
            this.mWebsiteEntries = new ArrayList();
            this.mSipEntries = new ArrayList();
            this.mEventEntries = new ArrayList();
            this.mRingtoneEntries = new ArrayList();
            this.mWeChatEntries = new ArrayList();
            this.mHwSNSEntries = new ArrayList();
            this.mEmerygencyEntries = new ArrayList();
            this.mWhatsAppEntries = new ArrayList();
            this.mQQEntries = new ArrayList();
            this.mSkypeEntries = new ArrayList();
            this.mOtherEntriesMap = new HashMap();
            this.mStatusEntriesForIm = new ArrayList();
            this.mAllEntries = new ArrayList();
            this.mQrNumbers = new ArrayList();
            this.mQrNames = new ArrayList();
            this.mQrEmails = new ArrayList();
            this.mQrPostals = new ArrayList();
            this.mQrCompanys = new ArrayList();
            this.mQrJobTitles = new ArrayList();
            this.mQrWebsites = new ArrayList();
            this.mQrNotes = new ArrayList();
            this.mQrIms = new ArrayList();
            this.mQrcodeDataInfo = new HashMap();
        }
    }

    private static final class InvitableAccountTypesAdapter extends BaseAdapter {
        private final ArrayList<AccountType> mAccountTypes;
        private final Context mContext;
        private final LayoutInflater mInflater;

        public InvitableAccountTypesAdapter(Context context, Contact contactData) {
            this.mContext = context;
            this.mInflater = LayoutInflater.from(context);
            List<AccountType> types = contactData.getInvitableAccountTypes();
            this.mAccountTypes = new ArrayList(types.size());
            for (int i = 0; i < types.size(); i++) {
                this.mAccountTypes.add((AccountType) types.get(i));
            }
            Collections.sort(this.mAccountTypes, new DisplayLabelComparator(this.mContext));
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View resultView;
            if (convertView != null) {
                resultView = convertView;
            } else {
                resultView = this.mInflater.inflate(R.layout.account_selector_list_item, parent, false);
            }
            TextView text1 = (TextView) resultView.findViewById(16908308);
            TextView text2 = (TextView) resultView.findViewById(16908309);
            ImageView icon = (ImageView) resultView.findViewById(16908294);
            AccountType accountType = (AccountType) this.mAccountTypes.get(position);
            CharSequence action = accountType.getInviteContactActionLabel(this.mContext);
            CharSequence label = accountType.getDisplayLabel(this.mContext);
            if (TextUtils.isEmpty(action)) {
                text1.setText(label);
                text2.setVisibility(8);
            } else {
                text1.setText(action);
                text2.setVisibility(0);
                text2.setText(label);
            }
            icon.setImageDrawable(accountType.getDisplayIcon(this.mContext));
            return resultView;
        }

        public int getCount() {
            return this.mAccountTypes.size();
        }

        public AccountType getItem(int position) {
            return (AccountType) this.mAccountTypes.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }
    }

    private class MakeLocalCopyQuickFix extends QuickFix {
        private MakeLocalCopyQuickFix() {
            super();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean isApplicable() {
            if (ContactDetailFragment.this.mContactData == null || !ContactDetailFragment.this.mContactData.isDirectoryEntry() || ContactDetailFragment.this.mContactData.getDirectoryExportSupport() == 0) {
                return false;
            }
            return true;
        }

        public void execute() {
            if (ContactDetailFragment.this.mListener != null && ContactDetailFragment.this.mContactData != null) {
                switch (ContactDetailFragment.this.mContactData.getDirectoryExportSupport()) {
                    case 1:
                        ContactDetailFragment.this.createCopy(new AccountWithDataSet(ContactDetailFragment.this.mContactData.getDirectoryAccountName(), ContactDetailFragment.this.mContactData.getDirectoryAccountType(), null));
                        break;
                    case 2:
                        List<AccountWithDataSet> accounts = AccountTypeManager.getInstance(ContactDetailFragment.this.mContext).getAccounts(true);
                        if (!accounts.isEmpty()) {
                            if (accounts.size() != 1) {
                                SelectAccountDialogFragment.show(ContactDetailFragment.this.getFragmentManager(), ContactDetailFragment.this, R.string.dialog_new_contact_account, AccountListFilter.ACCOUNTS_CONTACT_WRITABLE, null);
                                break;
                            } else {
                                ContactDetailFragment.this.createCopy((AccountWithDataSet) accounts.get(0));
                                return;
                            }
                        }
                        ContactDetailFragment.this.createCopy(null);
                        return;
                }
            }
        }
    }

    private static class MarkDefaultRunnable implements Runnable {
        Context mContext;
        long mId = 0;

        public MarkDefaultRunnable(Context context, long id) {
            this.mId = id;
            this.mContext = context;
        }

        public void run() {
            Uri fraquentUri = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "contacts/data_phone_frequent");
            Cursor cursor = null;
            try {
                if (this.mContext != null) {
                    cursor = this.mContext.getContentResolver().query(fraquentUri, new String[]{"data_id", "times_used", "contact_id"}, "contact_id=?", new String[]{String.valueOf(this.mId)}, "times_used DESC");
                    if (cursor != null) {
                        float maxUsedItem = 0.0f;
                        float totalUsed = 0.0f;
                        long maxUsedItemid = 0;
                        while (cursor.moveToNext()) {
                            long data_id = cursor.getLong(cursor.getColumnIndex("data_id"));
                            long times_used = cursor.getLong(cursor.getColumnIndex("times_used"));
                            if (cursor.getPosition() == 0) {
                                maxUsedItem = (float) times_used;
                                maxUsedItemid = data_id;
                            }
                            totalUsed += (float) times_used;
                        }
                        if (totalUsed > 0.0f) {
                            float rate = maxUsedItem / totalUsed;
                            if (maxUsedItem >= 10.0f && rate >= 0.8f) {
                                ContactDetailFragment.setDefaultContactMethod(this.mContext, maxUsedItemid, true);
                            }
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public static class NumberInfo {
        public String label;
        public String number;
    }

    class UserAdapter extends BaseAdapter {
        private ArrayList<NumberInfo> mEspaceCallEntries = new ArrayList();

        private class ViewHolder {
            TextView number;
            TextView type;

            private ViewHolder() {
            }
        }

        public UserAdapter(ArrayList<DetailViewEntry> callEntries) {
            ArrayList<DetailViewEntry> espaceCallEntries = callEntries;
            if (ContactDetailFragment.this.getActivity() != null) {
                for (int i = 0; i < callEntries.size(); i++) {
                    NumberInfo numberInfo = new NumberInfo();
                    numberInfo.number = PhoneNumberFormatter.formatNumber(ContactDetailFragment.this.getActivity().getApplicationContext(), PhoneNumberFormatter.parsePhoneNumber(((DetailViewEntry) callEntries.get(i)).data));
                    numberInfo.label = ((DetailViewEntry) callEntries.get(i)).typeString;
                    this.mEspaceCallEntries.add(numberInfo);
                }
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(ContactDetailFragment.this.mContext).inflate(R.layout.contact_phone_number_selection_item, null);
                holder = new ViewHolder();
                holder.number = (TextView) convertView.findViewById(R.id.contacts_phone_number);
                holder.type = (TextView) convertView.findViewById(R.id.contacts_phone_label);
                convertView.setTag(holder);
            }
            holder = (ViewHolder) convertView.getTag();
            holder.number.setText(((NumberInfo) this.mEspaceCallEntries.get(position)).number);
            holder.type.setText(((NumberInfo) this.mEspaceCallEntries.get(position)).label);
            return convertView;
        }

        public int getCount() {
            return this.mEspaceCallEntries.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }
    }

    protected static class ViewEntryDimensions {
        private final int mPaddingBottom;
        private final int mPaddingLeft;
        private final int mPaddingRight;
        private final int mPaddingTop = 0;
        private final int mWidePaddingLeft;

        public ViewEntryDimensions(Resources resources) {
            this.mPaddingLeft = resources.getDimensionPixelSize(R.dimen.detail_item_side_margin);
            this.mWidePaddingLeft = (this.mPaddingLeft + resources.getDimensionPixelSize(R.dimen.detail_item_icon_margin)) + resources.getDimensionPixelSize(R.dimen.detail_network_icon_size);
            this.mPaddingRight = this.mPaddingLeft;
            this.mPaddingBottom = this.mPaddingTop;
        }

        public int getPaddingTop() {
            return this.mPaddingTop;
        }

        public int getPaddingBottom() {
            return this.mPaddingBottom;
        }
    }

    static {
        mConDetFragmentHelper = null;
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            mConDetFragmentHelper = (HwCustContactDetailFragmentHelper) HwCustUtils.createObj(HwCustContactDetailFragmentHelper.class, new Object[0]);
        }
    }

    public ContactDetailAdapter getDetailAdapter() {
        return this.mAdapter;
    }

    public ViewEntryDimensions getViewEntryDimensions() {
        return this.mViewEntryDimensions;
    }

    public boolean isHighligh() {
        return this.mHighligh;
    }

    public void setHighligh(boolean highligh) {
        this.mHighligh = highligh;
    }

    public static ContactDetailFragment newInstance(Activity activity) {
        return new ContactDetailFragment();
    }

    public BackgroundViewCacher getViewInflator() {
        return this.mViewInflator;
    }

    public void setQueryPhoneNumberListener(QueryPhoneNumberListener queryPhoneNumberListener) {
        this.mQueryPhoneNumberListener = queryPhoneNumberListener;
    }

    public ArrayList<String> getPhoneNumbers() {
        return this.mPhoneNumbers;
    }

    public boolean ismIsFirstSimEnabled() {
        return this.mIsFirstSimEnabled;
    }

    public boolean ismIsSecondSimEnabled() {
        return this.mIsSecondSimEnabled;
    }

    public String getmRingtoneString() {
        return this.mRingtoneString;
    }

    public ContactDetailLayoutController getContactDetailLayoutController() {
        return this.mContactDetailLayoutController;
    }

    public void setFragment(Fragment fragment) {
        this.mFragment = fragment;
    }

    public Fragment getFragment() {
        return this.mFragment;
    }

    public ListView getContactDetailList() {
        return this.mListView;
    }

    public String getRemainInstruct(int slotId) {
        return null;
    }

    public RcsCLIRBroadCastHelper getRcsCLIRBroadCastHelper() {
        return this.mRcsCLIRBroadCastHelper;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mRcsCust = new RcsContactDetailFragmentHelper();
        if (savedInstanceState != null) {
            this.mSelectRingtoneUri = (Uri) savedInstanceState.getParcelable("SelectRingtoneUri");
            this.mLookupUri = (Uri) savedInstanceState.getParcelable("contactUri");
            this.mListState = savedInstanceState.getParcelable("liststate");
            this.mHighligh = savedInstanceState.getBoolean("key.new.add.number.highligh");
            this.mIsIPDialogVisible = savedInstanceState.getBoolean("IPDialog");
            this.mIsSMSDialogVisible = savedInstanceState.getBoolean("SMSDialog");
            this.mRcsCust.setOtherDiaglogVisibleFlag(savedInstanceState);
            this.mIPEntry = (DetailViewEntry) savedInstanceState.getParcelable("entry");
            this.mIsFirstSimEnabled = savedInstanceState.getBoolean("ISFIRSTSIM_ENABLED");
            this.mIsSecondSimEnabled = savedInstanceState.getBoolean("ISSECONDSIM_ENABLED");
            this.mIsNoNamedContact = savedInstanceState.getBoolean("is_no_named_contact", false);
        } else if (this.mFragment != null && (this.mFragment instanceof ContactInfoFragment)) {
            this.mIsNoNamedContact = ((ContactInfoFragment) this.mFragment).getIntent().getBooleanExtra("EXTRA_CALL_LOG_NONAME_CALL", false);
            if (((ContactInfoFragment) this.mFragment).getRcsCust() != null && ((ContactInfoFragment) this.mFragment).getRcsCust().isFromRcsGroupChat(((ContactInfoFragment) this.mFragment).getIntent())) {
                this.mIsNoNamedContact = true;
            }
        }
        if (EmuiFeatureManager.isNumberCapabilityEnable()) {
            this.mMarkManager = new NumberMarkManager(getContext(), this.mCapabilityCallback);
        }
        this.mMultiSimEnable = SimFactoryManager.isDualSim();
        if (this.mMultiSimEnable) {
            SimFactoryManager.addSimStateListener(this);
        }
        this.mRingtoneFilePathPreference = getActivity().getSharedPreferences("com.android.contacts.custom_ringtone_emui_2.0", 0);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MEDIA_UNMOUNTED");
        intentFilter.addDataScheme("file");
        this.mContext.registerReceiver(this.sdCardUnmountBroadcastRec, intentFilter);
        this.mRcsCust.handleCustomizationsOnCreate(this);
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsCLIRBroadCastHelper = RcsCLIRBroadCastHelper.getInstance(this.mContext);
        }
        this.mVoLteStatusObserver = new VoLteStatusObserver(this.mContext, this);
    }

    public void updateItemsStatus() {
        this.mAdapter.notifyDataSetChanged();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("contactUri", this.mLookupUri);
        outState.putBoolean("key.new.add.number.highligh", this.mHighligh);
        outState.putBoolean("IPDialog", this.mIsIPDialogVisible);
        outState.putBoolean("SMSDialog", this.mIsSMSDialogVisible);
        if (this.mIsIPDialogVisible || this.mIsSMSDialogVisible) {
            outState.putParcelable("entry", this.mIPEntry);
        } else if (this.mRcsCust != null) {
            this.mRcsCust.setOutStatusForOtherEntry(outState, "entry", this.mIPEntry);
        }
        if (this.mListView != null) {
            outState.putParcelable("liststate", this.mListView.onSaveInstanceState());
        }
        outState.putBoolean("ISFIRSTSIM_ENABLED", this.mIsFirstSimEnabled);
        outState.putBoolean("ISSECONDSIM_ENABLED", this.mIsSecondSimEnabled);
        outState.putBoolean("is_no_named_contact", this.mIsNoNamedContact);
        if (this.mAdapter != null) {
            this.mAdapter.onSaveInstanceState(outState);
        }
    }

    public void onPause() {
        dismissPopupIfShown();
        this.mVoLteStatusObserver.unregisterObserver();
        super.onPause();
    }

    public void onResume() {
        this.mVoLteStatusObserver.registerObserver();
        this.mIsIntentCalled = false;
        if (this.mMultiSimEnable) {
            this.mListView.invalidateViews();
            updateSimPresentFlag();
        }
        this.mDefaultSimcard = SimFactoryManager.getDefaultSimcard();
        if (!(!isAdded() || this.mRingtoneString == null || "-1".equals(this.mRingtoneString) || CommonUtilMethods.getPathFromUri(this.mContext, Uri.parse(this.mRingtoneString)) != null || -1 == getRingtonePosition() || this.mAdapter == null || this.mListView == null || this.mCachedEntriesObject == null)) {
            this.mCachedEntriesObject.mAllEntries.remove(getRingtonePosition());
            this.mCachedEntriesObject.mRingtoneEntries.clear();
            Collapser.collapseList(this.mCachedEntriesObject.mRingtoneEntries, false);
            buildRingtoneEntries(this.mCachedSimAccountType, false, this.mCachedEntriesObject, this.mContactData);
            flattenList(this.mCachedEntriesObject.mRingtoneEntries, this.mCachedEntriesObject);
            this.mAdapter.setAllEntries(this.mCachedEntriesObject.mAllEntries);
            this.mAdapter.notifyDataSetChanged();
            this.mListView.invalidateViews();
        }
        if (!(!isAdded() || this.mAdapter == null || VtLteUtils.isVtLteOn(this.mContext) == this.mIsVtLteOn)) {
            this.mAdapter.notifyDataSetChanged();
            this.mIsVtLteOn = VtLteUtils.isVtLteOn(this.mContext);
        }
        super.onResume();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mViewInflator = BackgroundViewCacher.getInstance(activity.getApplicationContext());
        this.mContext = activity;
        this.mViewEntryDimensions = new ViewEntryDimensions(this.mContext.getResources());
    }

    public void setContactData(Contact contactData) {
        if (this.mContactDetailLayoutController != null) {
            this.mContactDetailLayoutController.setContactData(contactData);
        } else if (CommonUtilMethods.calcIfNeedSplitScreen() && this.mFragment != null && (this.mFragment instanceof ContactInfoFragment)) {
            ((ContactInfoFragment) this.mFragment).setIsFailedSetContactData(true);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        this.mView = this.mViewInflator.getViewFromCache(R.layout.contact_detail_fragment);
        this.mView.setOnTouchListener(this.mForwardTouchToListView);
        this.mView.setOnDragListener(this.mForwardDragToListView);
        this.mInflater = inflater;
        this.mListView = (ListView) this.mView.findViewById(16908298);
        this.mListView.setOnItemClickListener(this);
        this.mListView.setItemsCanFocus(true);
        this.mListView.setOnScrollListener(this.mVerticalScrollListener);
        this.mListView.setFastScrollEnabled(true);
        CommonUtilMethods.addFootEmptyViewPortrait(this.mListView, this.mContext);
        this.mEmptyView = this.mView.findViewById(16908292);
        this.mView.setVisibility(4);
        if (this.mContactData != null && this.mContactData.getId() > 0) {
            this.mIsNoNamedContact = false;
        }
        if (!(this.mIsNoNamedContact || this.mContactData == null)) {
            bindData();
        }
        if (this.mFragment != null && EmuiFeatureManager.isDetailHeaderAnimationFeatureEnable(getContext())) {
            MultiShrinkScroller aScroller = (MultiShrinkScroller) this.mFragment.getView().findViewById(R.id.multiscroller);
            if (aScroller != null) {
                aScroller.initDetailView(this);
            }
        }
        this.mContactDetailLayoutController = new ContactDetailLayoutController(getActivity(), this, savedState, this.mContactDetailFragmentListener);
        if (this.mFragment != null && (this.mFragment instanceof ContactInfoFragment)) {
            ((ContactInfoFragment) this.mFragment).setOnStarBtnClickedInterface(this.mOnStarBtnClickedInterface);
            ((ContactInfoFragment) this.mFragment).setContactIfNeed();
        }
        return this.mView;
    }

    public void setListener(Listener value) {
        this.mListener = value;
    }

    public Context getContext() {
        return this.mContext;
    }

    protected Listener getListener() {
        return this.mListener;
    }

    public Contact getContactData() {
        return this.mContactData;
    }

    public Uri getUri() {
        return this.mLookupUri;
    }

    public void showEmptyState() {
        setData(null, null);
    }

    public void setData(Uri lookupUri, Contact result) {
        if (isAdded()) {
            this.mLookupUri = lookupUri;
            this.mContactData = result;
            bindData();
            if (!this.hasManualPrimaryPhoneEntry && this.mPhoneNumbers.size() > 1) {
                if (this.mMarkDefault != null) {
                    BackgroundGenricHandler.getInstance().removeCallbacks(this.mMarkDefault);
                }
                if (!(!isAdded() || getContext() == null || this.mContactData == null)) {
                    this.mMarkDefault = new MarkDefaultRunnable(getContext().getApplicationContext(), this.mContactData.getId());
                    BackgroundGenricHandler.getInstance().postDelayed(this.mMarkDefault, 2000);
                }
            }
        }
    }

    public void resetAdapter() {
        if (this.mListView != null) {
            boolean lDetailsLaunchedBeforeContactSave = false;
            if (this.mFragment != null && (this.mFragment instanceof ContactInfoFragment)) {
                lDetailsLaunchedBeforeContactSave = ((ContactInfoFragment) this.mFragment).isDetailsLaunchedBeforeContactSaved();
            }
            if (lDetailsLaunchedBeforeContactSave) {
                ((ContactInfoFragment) this.mFragment).resetDetailsLaunchedBeforeContactSaved();
            }
        }
    }

    protected void bindData() {
        if (this.mView != null && isAdded()) {
            getActivity().invalidateOptionsMenu();
            if (this.mContactData == null && TextUtils.isEmpty(getActivity().getIntent().getStringExtra("EXTRA_CALL_LOG_NUMBER"))) {
                this.mView.setVisibility(4);
                if (this.mCachedEntriesObject != null) {
                    this.mCachedEntriesObject.mAllEntries.clear();
                }
                if (this.mAdapter != null) {
                    this.mAdapter.notifyDataSetChanged();
                }
            } else if (this.mCachedContactData != this.mContactData) {
                if (this.isNeedFlush) {
                    synchronized (this.mLock) {
                        EntriesObject object = buildEntries(this.mContactData);
                        if (object != null) {
                            buildSubEntriesObject(object);
                            setListViewData(object, this.mContactData);
                        }
                    }
                    return;
                }
                this.isNeedFlush = true;
            }
        }
    }

    public long getPhoneDataIdByNumber(String phoneNumber) {
        if (this.mCachedEntriesObject != null) {
            try {
                if (!(this.mCachedEntriesObject.mPhoneEntries == null || TextUtils.isEmpty(phoneNumber))) {
                    for (DetailViewEntry entry : this.mCachedEntriesObject.mPhoneEntries) {
                        if (entry != null && CommonUtilMethods.compareNumsHw(entry.data, phoneNumber)) {
                            if (HwLog.HWFLOW) {
                                HwLog.i(TAG, "find same phonenumber");
                            }
                            return entry.getId();
                        }
                    }
                }
            } catch (RuntimeException e) {
                HwLog.w(TAG, "getPhoneDataIdByNumber fail");
            }
        }
        return -1;
    }

    private void queryCapability(List<DetailViewEntry> phonelist) {
        if (EmuiFeatureManager.isNumberCapabilityEnable() && phonelist != null && phonelist.size() != 0 && this.mMarkManager != null) {
            String[] numbers = new String[phonelist.size()];
            for (int i = 0; i < phonelist.size(); i++) {
                numbers[i] = ((DetailViewEntry) phonelist.get(i)).data;
            }
            this.mMarkManager.getCapabilityInfoAsync(numbers, this.mCapabilityCallback);
        }
    }

    private void setListViewData(final EntriesObject allEntries, Contact contact) {
        boolean z = true;
        if (this.mContext != null) {
            allEntries.mAllEntries.clear();
            Collapser.collapseList(allEntries.mPhoneEntries, false);
            if (this.mRcsCust != null) {
                this.mRcsCust.setOtherEntryViewData();
            }
            Collapser.collapseList(allEntries.mEmailEntries, false);
            Collapser.collapseList(allEntries.mPostalEntries, false);
            Collapser.collapseList(allEntries.mRingtoneEntries, false);
            if (CommonUtilMethods.isMergeFeatureEnabled()) {
                Collapser.collapseList(allEntries.mWebsiteEntries, false);
                Collapser.collapseList(allEntries.mNicknameEntries, false);
                Collapser.collapseList(allEntries.mNoteEntries, false);
                Collapser.collapseList(allEntries.mSipEntries, false);
            }
            Collapser.collapseList(allEntries.mEmerygencyEntries, false);
            this.mIsUniqueNumber = allEntries.mPhoneEntries.size() == 1;
            if (allEntries.mEmailEntries.size() != 1) {
                z = false;
            }
            this.mIsUniqueEmail = z;
            CommonUtilMethods.checkPrimary(allEntries.mPhoneEntries);
            CommonUtilMethods.checkPrimary(allEntries.mEmailEntries);
            setupFlattenedList(allEntries, contact);
            if (this.mAdapter == null) {
                this.mAdapter = new ContactDetailAdapter(this.mContext);
                this.mIsVtLteOn = VtLteUtils.isVtLteOn(this.mContext);
                this.mAdapter.setDetailFragment(this);
                this.mAdapter.setInflater(this.mInflater);
                this.mAdapter.setContactData(this.mContactData);
            }
            if (this.mSelectRingtoneUri != null) {
                this.mAdapter.selectRingTone(this.mSelectRingtoneUri);
                getActivity().setRequestedOrientation(2);
                this.mSelectRingtoneUri = null;
            }
            if (this.mRcsCust != null) {
                this.mRcsCust.addOtherEntry(this.mAdapter, allEntries.mIPCallEntries, allEntries.mAllEntries);
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    ContactDetailFragment.this.mListView.setAdapter(ContactDetailFragment.this.mAdapter);
                    if (ContactDetailFragment.this.mRcsCust != null) {
                        ContactDetailFragment.this.mRcsCust.getOtherDialog(allEntries.mIPCallEntries, ContactDetailFragment.this.mIPEntry, ContactDetailFragment.this);
                    }
                    ContactDetailFragment.this.mCachedEntriesObject = allEntries;
                    ContactDetailFragment.this.mAdapter.setAllEntries((ArrayList) allEntries.mAllEntries.clone());
                    ContactDetailFragment.this.mAdapter.notifyDataSetChanged();
                    ContactDetailFragment.this.queryCapability(allEntries.mPhoneEntries);
                    if (!(ContactDetailFragment.this.mRcsCust == null || ContactDetailFragment.this.mCachedEntriesObject == null)) {
                        if (HwLog.HWFLOW) {
                            HwLog.i(ContactDetailFragment.TAG, "Send rcs capability query!");
                        }
                        ContactDetailFragment.this.mRcsCust.sendRcsQuestCapability(ContactDetailFragment.this.mCachedEntriesObject.mIPCallEntries);
                    }
                    if (!(!ContactDetailFragment.this.mIsIPDialogVisible || ContactDetailFragment.this.mIpDialog == null || ContactDetailFragment.this.mIpDialog.isShowing())) {
                        ContactDetailFragment.this.getEspaceDialog();
                    }
                    if (!(!ContactDetailFragment.this.mIsSMSDialogVisible || ContactDetailFragment.this.mMessageDialog == null || ContactDetailFragment.this.mMessageDialog.isShowing())) {
                        ContactDetailFragment.this.getMessageDialog(ContactDetailFragment.this.mIPEntry);
                    }
                    if (ContactDetailFragment.this.mListState != null) {
                        ContactDetailFragment.this.mListView.onRestoreInstanceState(ContactDetailFragment.this.mListState);
                        ContactDetailFragment.this.mListState = null;
                    }
                    ContactDetailFragment.this.mAdapter.notifyDataSetChanged();
                    ContactDetailFragment.this.mListView.setEmptyView(ContactDetailFragment.this.mEmptyView);
                    ContactDetailFragment.this.configureQuickFix();
                    ContactDetailFragment.this.mView.setVisibility(0);
                }
            });
        }
    }

    private void configureQuickFix() {
        this.mQuickFix = null;
        for (QuickFix fix : this.mPotentialQuickFixes) {
            if (fix.isApplicable()) {
                this.mQuickFix = fix;
                return;
            }
        }
    }

    public void doQuickFix() {
        if (this.mQuickFix != null) {
            this.mQuickFix.execute();
        }
    }

    private long getDefaultGroupId(List<GroupMetaData> groups) {
        long defaultGroupId = -1;
        if (groups.size() > 0 && CommonUtilMethods.isSimAccount(((GroupMetaData) groups.get(0)).getAccountType())) {
            return -1;
        }
        for (GroupMetaData group : groups) {
            if (group.isDefaultGroup()) {
                if (defaultGroupId != -1) {
                    return -1;
                }
                defaultGroupId = group.getGroupId();
            }
        }
        return defaultGroupId;
    }

    public boolean isHasManualPrimaryPhoneEntry() {
        return this.hasManualPrimaryPhoneEntry;
    }

    private final EntriesObject buildEntries(Contact contact) {
        if (this.mCachedContactData == contact) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "buildEntries() mCachedContactData == contact");
            }
            return null;
        }
        this.mCachedContactData = contact;
        this.mHasPhone = PhoneCapabilityTester.isPhone(this.mContext);
        this.mHasSip = PhoneCapabilityTester.isSipPhone(this.mContext);
        this.mPrimaryPhoneUri = null;
        this.mPrimaryNumber = null;
        if (contact == null) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "buildEntries() contact == null");
            }
            return null;
        } else if (this.mContactData.getRawContacts() != null || CommonUtilMethods.calcIfNeedSplitScreen()) {
            EntriesObject entriesObject = new EntriesObject();
            ArrayList<String> groups = new ArrayList();
            this.mPhoneNumbers.clear();
            this.mFormattedPhoneNum.clear();
            String mAccountType = null;
            entriesObject.mQrcodeDataInfo.clear();
            entriesObject.mQrNumbers.clear();
            entriesObject.mQrNames.clear();
            entriesObject.mQrEmails.clear();
            entriesObject.mQrPostals.clear();
            entriesObject.mQrCompanys.clear();
            entriesObject.mQrJobTitles.clear();
            entriesObject.mQrWebsites.clear();
            entriesObject.mQrNotes.clear();
            entriesObject.mQrIms.clear();
            entriesObject.mPhoneEntries.clear();
            if (contact.getRawContacts() == null) {
                if (HwLog.HWFLOW) {
                    HwLog.i(TAG, "contact.getRawContacts() == null");
                }
                return null;
            }
            for (RawContact rawContact : contact.getRawContacts()) {
                long rawContactId = rawContact.getId().longValue();
                AccountType accountType = rawContact.getAccountType(this.mContext);
                if (accountType.accountType != null) {
                    mAccountType = accountType.accountType;
                }
                for (DataItem dataItem : rawContact.getDataItems()) {
                    dataItem.setRawContactId(rawContactId);
                    if (dataItem.getMimeType() != null) {
                        if (dataItem instanceof GroupMembershipDataItem) {
                            Long groupId = ((GroupMembershipDataItem) dataItem).getGroupRowId();
                            if (groupId != null) {
                                handleGroupMembership(groups, contact.getGroupMetaData(), groupId.longValue());
                            }
                        } else {
                            DataKind kind = AccountTypeManager.getInstance(this.mContext).getKindOrFallback(accountType, dataItem.getMimeType());
                            if (kind != null) {
                                DetailViewEntry entry = DetailViewEntry.fromValues(this.mContext, dataItem, contact.isDirectoryEntry(), contact.getDirectoryId(), kind);
                                entry.maxLines = kind.maxLinesForDisplay;
                                boolean hasData = !TextUtils.isEmpty(entry.data);
                                boolean isSuperPrimary = dataItem.isSuperPrimary();
                                boolean isManualprimay = dataItem.isManualPrimary();
                                if ((dataItem instanceof StructuredNameDataItem) || "vnd.android.huawei.cursor.item/ringtone".equals(dataItem.getMimeType())) {
                                    if (dataItem instanceof StructuredNameDataItem) {
                                        entriesObject.mQrNames.add(entry.data);
                                        entriesObject.mNameEntries.add(entry);
                                    }
                                } else if ((dataItem instanceof PhoneDataItem) && hasData) {
                                    String callNumberString;
                                    if (contact.getNameRawContactId() != -1 && EmuiFeatureManager.isChinaArea()) {
                                        entry.location = dataItem.getContentValues().getAsString("data6");
                                    }
                                    entry.isYellowPage = contact.isYellowPage();
                                    PhoneDataItem phone = (PhoneDataItem) dataItem;
                                    entry.mOriginalPhoneNum = phone.getNumber();
                                    entry.normalizedNumber = phone.getNormalizedNumber();
                                    String roamingFormString = null;
                                    boolean isNeedDealWithRoamingData = IsPhoneNetworkRoamingUtils.isRoamingDealWithNumber(getContext(), entry.mOriginalPhoneNum) ? IsPhoneNetworkRoamingUtils.isPhoneNetworkRoamging() : false;
                                    if (isNeedDealWithRoamingData) {
                                        roamingFormString = IsPhoneNetworkRoamingUtils.getPhoneNumber(getContext(), entry, phone);
                                        entry.roamingData = roamingFormString;
                                    } else if (entry.normalizedNumber == null || entry.normalizedNumber.length() == 0) {
                                        entry.setNullOriginalRoamingData(true);
                                    }
                                    if (EmuiFeatureManager.isChinaArea()) {
                                        entry.data = ContactsUtils.getChinaFormatNumber(phone.getFormattedPhoneNumber());
                                    } else {
                                        entry.data = phone.getFormattedPhoneNumber();
                                    }
                                    entriesObject.mQrNumbers.add(entry.data);
                                    if (isNeedDealWithRoamingData) {
                                        boolean isNumberMatchCurrentCountry = false;
                                        if (!entry.isNullOriginalRoamingData) {
                                            isNumberMatchCurrentCountry = IsPhoneNetworkRoamingUtils.isNumberMatchCurrentCountry(entry.data, entry.roamingData, getContext());
                                        }
                                        if (isNumberMatchCurrentCountry) {
                                            callNumberString = entry.data;
                                        } else {
                                            callNumberString = roamingFormString;
                                        }
                                    } else {
                                        callNumberString = entry.data;
                                    }
                                    Intent pIntent = null;
                                    if (this.mHasPhone) {
                                        pIntent = CallUtil.getCallIntent(callNumberString);
                                        if (entry.isNullOriginalRoamingData) {
                                            pIntent.putExtra(" pref_Original_Normalized_Number_Is_Null", true);
                                            pIntent.putExtra("pref_Original_Numbe", entry.data);
                                            pIntent.putExtra("pref_Dial_Number", callNumberString);
                                        }
                                        if (entry.getRoamingPhoneGatherUtils() != null) {
                                            RoamingPhoneGatherUtils.disposeSingleCardRoamingPhoneItem(getActivity(), entry.getRoamingPhoneGatherUtils(), pIntent);
                                        }
                                    }
                                    Intent phoneIntent = pIntent;
                                    if (this.mHasPhone) {
                                        entry.intent = phoneIntent;
                                    } else {
                                        entry.intent = null;
                                    }
                                    if (contact.isUserProfile()) {
                                        entry.intent = null;
                                    }
                                    if (isSuperPrimary) {
                                        this.mPrimaryPhoneUri = entry.uri;
                                    }
                                    entry.isPrimary = isSuperPrimary;
                                    if (isManualprimay) {
                                        this.hasManualPrimaryPhoneEntry = true;
                                    }
                                    String lNumber;
                                    if (entry.isPrimary) {
                                        entriesObject.mPhoneEntries.add(0, entry);
                                        lNumber = PhoneNumberFormatter.parsePhoneNumber(entry.data);
                                        addFormattedPhoneNumAndUriInArrayLis(lNumber, entry.uri);
                                        lNumber = PhoneNumberUtils.stripSeparators(lNumber);
                                        addPhoneNumAndUriInArraylist(lNumber, entry.uri);
                                        this.mPrimaryNumber = lNumber;
                                    } else {
                                        entriesObject.mPhoneEntries.add(entry);
                                        lNumber = PhoneNumberFormatter.parsePhoneNumber(entry.data);
                                        addFormattedPhoneNumAndUriInArrayLis(lNumber, entry.uri);
                                        lNumber = PhoneNumberUtils.stripSeparators(lNumber);
                                        addPhoneNumAndUriInArraylist(lNumber, entry.uri);
                                        addPhoneNumCountryIsoInHashMap(lNumber, entry.normalizedNumber);
                                    }
                                } else if ((dataItem instanceof EmailDataItem) && hasData) {
                                    entry.intent = new Intent("android.intent.action.SENDTO", Uri.fromParts("mailto", entry.data, null));
                                    entry.isPrimary = isSuperPrimary;
                                    if (entry.isPrimary) {
                                        entriesObject.mEmailEntries.add(0, entry);
                                    } else {
                                        entriesObject.mEmailEntries.add(entry);
                                    }
                                    entriesObject.mQrEmails.add(entry.data);
                                    if (contact.getStatuses() != null) {
                                        status = (DataStatus) contact.getStatuses().get(Long.valueOf(entry.id));
                                        if (status != null) {
                                            ImDataItem im = ImDataItem.createFromEmail((EmailDataItem) dataItem);
                                            DetailViewEntry imEntry = DetailViewEntry.fromValues(this.mContext, im, contact.isDirectoryEntry(), contact.getDirectoryId(), kind);
                                            buildImActions(this.mContext, imEntry, im);
                                            imEntry.setPresence(status.getPresence());
                                            imEntry.maxLines = kind.maxLinesForDisplay;
                                            entriesObject.mImEntries.add(imEntry);
                                        }
                                    }
                                } else if ((dataItem instanceof StructuredPostalDataItem) && hasData) {
                                    entry.intent = StructuredPostalUtils.getViewPostalAddressIntent(entry.data);
                                    entriesObject.mPostalEntries.add(entry);
                                    entriesObject.mQrPostals.add(entry.data);
                                } else if ((dataItem instanceof ImDataItem) && hasData) {
                                    buildImActions(this.mContext, entry, (ImDataItem) dataItem);
                                    if (contact.getStatuses() != null) {
                                        status = (DataStatus) contact.getStatuses().get(Long.valueOf(entry.id));
                                        if (status != null) {
                                            entry.setPresence(status.getPresence());
                                        }
                                    }
                                    if (!ContactDetailHelper.isEntryAlreadyExisted(entriesObject.mImEntries, entry)) {
                                        entriesObject.mImEntries.add(entry);
                                        entriesObject.mQrIms.add(entry.data);
                                    }
                                } else if (dataItem instanceof OrganizationDataItem) {
                                    OrganizationDataItem orgDataItem = (OrganizationDataItem) dataItem;
                                    String company = orgDataItem.getCompany();
                                    String title = orgDataItem.getTitle();
                                    entriesObject.mQrCompanys.add(company);
                                    entriesObject.mQrJobTitles.add(title);
                                    entry.data = company;
                                    entry.typeString = null;
                                    entry.kind = getString(R.string.ghostData_company);
                                    entriesObject.mCompanyEntries.add(entry);
                                    DetailViewEntry entryTemp = new DetailViewEntry();
                                    entryTemp.data = title;
                                    entryTemp.kind = getString(R.string.ghostData_title);
                                    entryTemp.typeString = null;
                                    entryTemp.mimetype = entry.mimetype;
                                    entriesObject.mPositionEntries.add(entryTemp);
                                } else if ((dataItem instanceof NicknameDataItem) && hasData) {
                                    boolean duplicatesTitle = (contact.getNameRawContactId() > rawContactId ? 1 : (contact.getNameRawContactId() == rawContactId ? 0 : -1)) == 0 ? contact.getDisplayNameSource() == 35 : false;
                                    if (!duplicatesTitle) {
                                        entry.uri = null;
                                        entriesObject.mNicknameEntries.add(entry);
                                    }
                                } else if ((dataItem instanceof NoteDataItem) && hasData) {
                                    entry.uri = null;
                                    entriesObject.mNoteEntries.add(entry);
                                    entriesObject.mQrNotes.add(entry.data);
                                } else if ((dataItem instanceof WebsiteDataItem) && hasData) {
                                    entry.uri = null;
                                    try {
                                        entry.intent = new Intent("android.intent.action.VIEW", Uri.parse(new WebAddress(entry.data).toString()));
                                    } catch (ParseException e) {
                                        HwLog.e(TAG, "Couldn't parse website");
                                    }
                                    entriesObject.mWebsiteEntries.add(entry);
                                    entriesObject.mQrWebsites.add(entry.data);
                                } else if ((dataItem instanceof SipAddressDataItem) && hasData) {
                                    if (!PhoneCapabilityTester.isChinaTelecomCustomer(this.mContext)) {
                                        entry.uri = null;
                                        if (this.mHasSip) {
                                            entry.intent = CallUtil.getCallIntent(Uri.fromParts("sip", entry.data, null));
                                        } else {
                                            entry.intent = null;
                                        }
                                        entriesObject.mSipEntries.add(entry);
                                    }
                                } else if ((dataItem instanceof EventDataItem) && hasData) {
                                    HwLog.i(TAG, "ContactDetailFragment : entry.data  ");
                                    if (entry.type != 4 || LunarUtils.supportLunarAccount(rawContact.getAccountTypeString(), getContext())) {
                                        if (LunarUtils.hasYear(getContext(), entry.data)) {
                                            entry.data = LunarUtils.getCurrentYear() + entry.data.substring(1, entry.data.length());
                                        }
                                        boolean isLunarBirthday = false;
                                        if (LunarUtils.supportLunarAccount(rawContact.getAccountTypeString(), getContext()) && entry.type == 4) {
                                            isLunarBirthday = true;
                                        }
                                        if (LunarUtils.checkTimeValidity(false, entry.data)) {
                                            long millis;
                                            if (isLunarBirthday) {
                                                LunarUtils.initYearAndWeek(this.mContext);
                                                millis = LunarUtils.getNextLunarBirthday(entry.data).longValue();
                                                entry.data = LunarUtils.titleSolarToLunar(this.mContext, entry.data);
                                            } else {
                                                millis = LunarUtils.getNextSolarBirthday(entry.data, true);
                                                entry.data = DateUtils.formatDate(this.mContext, entry.data);
                                            }
                                            if (millis != 0) {
                                                entry.intent = LunarUtils.getEventIntent(millis);
                                                entry.uri = null;
                                                if (!ContactDetailHelper.isEntryAlreadyExisted(entriesObject.mEventEntries, entry)) {
                                                    entriesObject.mEventEntries.add(entry);
                                                }
                                            }
                                        }
                                    }
                                } else if ((dataItem instanceof RelationDataItem) && hasData) {
                                    entry.intent = new Intent("android.intent.action.SEARCH");
                                    entry.intent.putExtra(SearchIntents.EXTRA_QUERY, entry.data);
                                    entry.intent.setType("vnd.android.cursor.dir/contact");
                                    if (!ContactDetailHelper.isEntryAlreadyExisted(entriesObject.mRelationEntries, entry)) {
                                        entriesObject.mRelationEntries.add(entry);
                                    }
                                } else if ("vnd.android.huawei.cursor.item/status_update".equals(dataItem.getMimeType()) && hasData) {
                                    entry.kind = getString(R.string.str_title_res_for_status);
                                    entriesObject.mStatusEntriesForIm.add(entry);
                                } else {
                                    entry.intent = new Intent("android.intent.action.VIEW");
                                    entry.intent.setDataAndType(entry.uri, entry.mimetype);
                                    entry.intent.addFlags(536870912);
                                    entry.data = dataItem.buildDataString(getContext(), kind);
                                    if (!TextUtils.isEmpty(entry.data)) {
                                        if ("com.tencent.mm.account".equals(accountType.accountType)) {
                                            buildWeChatEntry(entry, entriesObject);
                                        } else if ("com.tencent.qq.account".equals(accountType.accountType) || "com.tencent.mobileqq.account".equals(accountType.accountType)) {
                                            buildQQEntry(entry, entriesObject);
                                        } else if ("com.whatsapp".equals(accountType.accountType)) {
                                            buildWhatsAppEntry(entry, accountType, entriesObject);
                                        } else if ("com.skype.contacts.sync".equals(accountType.accountType)) {
                                            buildSkypeEntry(entry, accountType, entriesObject);
                                        } else if ("com.huawei.hwid".equals(accountType.accountType)) {
                                            buildHwSNSEntry(entry, accountType, entriesObject);
                                        } else {
                                            if ("vnd.android.cursor.item/vnd.com.viber.voip.viber_number_call".equals(dataItem.getMimeType()) || "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_message".equals(dataItem.getMimeType())) {
                                                entry.data = setNumberStringForAr(entry.data);
                                            }
                                            entry.intent.setFlags(335544320);
                                            if (entriesObject.mOtherEntriesMap.containsKey(accountType)) {
                                                ((List) entriesObject.mOtherEntriesMap.get(accountType)).add(entry);
                                            } else {
                                                List<DetailViewEntry> listEntries = new ArrayList();
                                                listEntries.add(entry);
                                                entriesObject.mOtherEntriesMap.put(accountType, listEntries);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (entriesObject.mPhoneEntries.size() > 0) {
                ((DetailViewEntry) entriesObject.mPhoneEntries.get(0)).isFristEntry = true;
            }
            if (entriesObject.mEmailEntries.size() > 0) {
                ((DetailViewEntry) entriesObject.mEmailEntries.get(0)).isFristEntry = true;
            }
            if (entriesObject.mEventEntries.size() > 0) {
                ((DetailViewEntry) entriesObject.mEventEntries.get(0)).isFristEntry = true;
            }
            if (entriesObject.mPostalEntries.size() > 0) {
                ((DetailViewEntry) entriesObject.mPostalEntries.get(0)).isFristEntry = true;
            }
            if (entriesObject.mWebsiteEntries.size() > 0) {
                ((DetailViewEntry) entriesObject.mWebsiteEntries.get(0)).isFristEntry = true;
            }
            if (entriesObject.mImEntries.size() > 0) {
                ((DetailViewEntry) entriesObject.mImEntries.get(0)).isFristEntry = true;
            }
            if (entriesObject.mRelationEntries.size() > 0) {
                ((DetailViewEntry) entriesObject.mRelationEntries.get(0)).isFristEntry = true;
            }
            entriesObject.mQrcodeDataInfo.put("phone", entriesObject.mQrNumbers);
            entriesObject.mQrcodeDataInfo.put("name", entriesObject.mQrNames);
            entriesObject.mQrcodeDataInfo.put(Scopes.EMAIL, entriesObject.mQrEmails);
            entriesObject.mQrcodeDataInfo.put("postal", entriesObject.mQrPostals);
            entriesObject.mQrcodeDataInfo.put("company", entriesObject.mQrCompanys);
            entriesObject.mQrcodeDataInfo.put("job_title", entriesObject.mQrJobTitles);
            entriesObject.mQrcodeDataInfo.put("URL_KEY", entriesObject.mQrWebsites);
            entriesObject.mQrcodeDataInfo.put("NOTE_KEY", entriesObject.mQrNotes);
            entriesObject.mQrcodeDataInfo.put("IM", entriesObject.mQrIms);
            if (this.mQRcodeEntryListener != null) {
                this.mQRcodeEntryListener.getQRcodeEntryCompleted(entriesObject.mQrcodeDataInfo);
            }
            this.mCachedSimAccountType = mAccountType;
            this.mEntriesBuilt = true;
            if (this.mQueryPhoneNumberListener != null) {
                this.mQueryPhoneNumberListener.onQueryPhoneNumberCompletedIso(this.mPhoneNumberCountryIsoHM);
                this.mQueryPhoneNumberListener.onQueryPhoneNumberCompleted(this.mPhoneNumbers);
            }
            buildGroupEntries(groups, entriesObject);
            addPhoneticName(entriesObject, contact);
            if (mConDetFragmentHelper != null) {
                mConDetFragmentHelper.buildCustomEntries(this.mContext, contact, mAccountType);
            }
            return entriesObject;
        } else {
            if (getActivity() != null) {
                Toast.makeText(getActivity().getApplicationContext(), R.string.quickcontact_missing_app_Toast, 0).show();
                getActivity().finish();
            }
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "mContactData.getRawContacts() == null");
            }
            return null;
        }
    }

    public boolean isEntriesBuilt() {
        return this.mEntriesBuilt;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void buildRingtoneEntries(String accountType, boolean async, EntriesObject entriesObject, Contact contact) {
        if (!(contact == null || entriesObject == null || this.mContext == null || entriesObject.mRingtoneEntries.size() != 0 || contact.isUserProfile() || contact.isYellowPage() || !EmuiFeatureManager.isSystemVoiceCapable() || ((!this.mContactData.isWritableContact(this.mContext) && !"com.android.huawei.sim".equals(accountType) && !"com.android.huawei.secondsim".equals(accountType)) || !isAdded()))) {
            final DetailViewEntry detailViewEntry = new DetailViewEntry();
            detailViewEntry.kind = getString(R.string.label_ringtone);
            detailViewEntry.id = ((RawContact) contact.getRawContacts().get(0)).getId().longValue();
            detailViewEntry.mimetype = "vnd.android.huawei.cursor.item/ringtone";
            detailViewEntry.typeString = "";
            if (!TextUtils.isEmpty(contact.getCustomRingtone())) {
                this.mRingtoneString = contact.getCustomRingtone();
                if ("-1".equals(this.mRingtoneString)) {
                    detailViewEntry.data = getString(R.string.contact_silent_ringtone);
                } else {
                    Runnable run = new Runnable() {
                        public void run() {
                            Ringtone ringtone = null;
                            if (!TextUtils.isEmpty(ContactDetailFragment.this.mRingtoneString)) {
                                ringtone = RingtoneManager.getRingtone(ContactDetailFragment.this.mContext, Uri.parse(ContactDetailFragment.this.mRingtoneString));
                            }
                            if (ringtone == null || RingtoneManagerEx.getSetUriStat()) {
                                if ("-1".equals(ContactDetailFragment.this.mRingtoneString)) {
                                    detailViewEntry.data = ContactDetailFragment.this.getString(R.string.contact_silent_ringtone);
                                } else if (!DrmUtils.DRM_ENABLED || ringtone == null) {
                                    detailViewEntry.data = ContactDetailFragment.this.getString(R.string.default_ringtone);
                                    ContactDetailFragment.this.mRingtoneString = null;
                                } else {
                                    String[] proj = new String[]{"_data"};
                                    Cursor cursor = ContactDetailFragment.this.mContext.getContentResolver().query(Uri.parse(ContactDetailFragment.this.mContactData.getCustomRingtone()), proj, null, null, null);
                                    if (cursor != null) {
                                        try {
                                            int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                                            if (cursor.moveToFirst()) {
                                                String filePath = cursor.getString(column_index);
                                                DrmUtils.initialize(ContactDetailFragment.this.mContext);
                                                if (!DrmUtils.isDrmFile(filePath) || DrmUtils.haveRightsForAction(filePath, 1)) {
                                                    detailViewEntry.data = ringtone.getTitle(ContactDetailFragment.this.mContext);
                                                } else {
                                                    ContactDetailFragment.this.mRingtoneString = null;
                                                    ContactDetailFragment.this.updateRingtoneOnContact();
                                                }
                                            } else {
                                                detailViewEntry.data = ContactDetailFragment.this.getString(R.string.default_ringtone);
                                                ContactDetailFragment.this.mRingtoneString = null;
                                            }
                                            cursor.close();
                                        } catch (Throwable th) {
                                            cursor.close();
                                        }
                                    }
                                }
                            } else if (!TextUtils.isEmpty(ringtone.getTitle(ContactDetailFragment.this.mContext))) {
                                detailViewEntry.data = ringtone.getTitle(ContactDetailFragment.this.mContext);
                            } else if ("-1".equals(ContactDetailFragment.this.mRingtoneString)) {
                                detailViewEntry.data = ContactDetailFragment.this.getString(R.string.contact_silent_ringtone);
                            } else {
                                detailViewEntry.data = ContactDetailFragment.this.getString(R.string.default_ringtone);
                                ContactDetailFragment.this.mRingtoneString = null;
                            }
                        }
                    };
                    if (async) {
                        BackgroundGenricHandler.getInstance().post(run);
                    } else {
                        run.run();
                    }
                }
            } else if ("-1".equals(this.mRingtoneString)) {
                detailViewEntry.data = getString(R.string.contact_silent_ringtone);
            } else {
                detailViewEntry.data = getString(R.string.default_ringtone);
                this.mRingtoneString = null;
                detailViewEntry.mAccountType = accountType;
            }
            detailViewEntry.uri = ContentUris.withAppendedId(Data.CONTENT_URI, detailViewEntry.id);
            if (!(this.mRingtoneString == null || "-1".equals(this.mRingtoneString) || Uri.parse(this.mRingtoneString) == null || !CommonUtilMethods.isUriOfExternalSource(Uri.parse(this.mRingtoneString)))) {
                this.mRingtoneFilePathPreference.edit().putString(this.mRingtoneString, CommonUtilMethods.getPathFromUri(this.mContext, Uri.parse(this.mRingtoneString))).commit();
            }
            entriesObject.mRingtoneEntries.add(detailViewEntry);
        }
    }

    private void buildGroupEntries(ArrayList<String> groups, EntriesObject entriesObject) {
        if (!groups.isEmpty() && !CommonUtilMethods.isSimplifiedModeEnabled()) {
            DetailViewEntry entry = new DetailViewEntry();
            Collections.sort(groups);
            StringBuilder sb = new StringBuilder();
            int groupSize = groups.size();
            for (int i = 0; i < groupSize; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append((String) groups.get(i));
            }
            entry.mimetype = "vnd.android.cursor.item/group_membership";
            entry.kind = this.mContext.getString(R.string.groupsLabel);
            entry.data = CamcardGroup.replaceTitle(sb, this.mContext);
            if (entriesObject != null) {
                entriesObject.mGroupEntries.add(entry);
            }
        }
    }

    private void buildWeChatEntry(DetailViewEntry entry, EntriesObject entriesObject) {
        if (entriesObject == null) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "buildWeChatEntry()  entriesObject == null");
            }
            return;
        }
        DetailViewEntry wechatEntry;
        if (entriesObject.mWeChatEntries.size() > 0) {
            wechatEntry = (DetailViewEntry) entriesObject.mWeChatEntries.get(0);
        } else {
            wechatEntry = new DetailViewEntry();
            wechatEntry.mimetype = "wechat";
            wechatEntry.data = getResources().getString(R.string.contact_weichat);
            entriesObject.mWeChatEntries.add(wechatEntry);
        }
        if ("vnd.android.cursor.item/vnd.com.tencent.mm.chatting.profile".equals(entry.mimetype)) {
            wechatEntry.intent = entry.intent;
            wechatEntry.mPrimaryLabel = entry.data;
        } else if ("vnd.android.cursor.item/vnd.com.tencent.mm.plugin.sns.timeline".equals(entry.mimetype)) {
            wechatEntry.secondaryIntent = entry.intent;
            wechatEntry.mSecondaryLabel = entry.data;
        } else if ("vnd.android.cursor.item/vnd.com.tencent.mm.chatting.voip.video".equals(entry.mimetype)) {
            wechatEntry.tertiaryIntent = entry.intent;
            wechatEntry.mTertiaryLabel = entry.data;
        }
        entry.intent.setFlags(67108864);
    }

    private void buildHwSNSEntry(DetailViewEntry entry, AccountType accountType, EntriesObject entriesObject) {
        if (entriesObject == null) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "buildHwSNSEntry()  entriesObject == null");
            }
        } else if (entry != null && accountType != null && this.mContext != null) {
            DetailViewEntry hwSNSEntry;
            if (entriesObject.mHwSNSEntries.size() > 0) {
                hwSNSEntry = (DetailViewEntry) entriesObject.mHwSNSEntries.get(0);
            } else {
                hwSNSEntry = new DetailViewEntry();
                hwSNSEntry.mimetype = "hwsns";
                hwSNSEntry.data = String.valueOf(accountType.getDisplayLabel(this.mContext));
                entriesObject.mHwSNSEntries.add(hwSNSEntry);
            }
            if ("vnd.android.cursor.item/vnd.com.huawei.sns.chat".equals(entry.mimetype)) {
                hwSNSEntry.secondaryIntent = entry.intent;
                hwSNSEntry.mSecondaryLabel = entry.data;
            } else if ("vnd.android.cursor.item/vnd.com.huawei.sns.card".equals(entry.mimetype)) {
                hwSNSEntry.intent = entry.intent;
                hwSNSEntry.mPrimaryLabel = entry.data;
            }
            entry.intent.setFlags(67108864);
        }
    }

    private void buildHwEmergencyEntry(EntriesObject entriesObject) {
        if (entriesObject == null) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "buildHwEmergencyEntry()  entriesObject == null");
            }
        } else if (this.mContext != null) {
            DetailViewEntry hwEmergencyEntry;
            if (entriesObject.mEmerygencyEntries.size() > 0) {
                hwEmergencyEntry = (DetailViewEntry) entriesObject.mEmerygencyEntries.get(0);
            } else {
                hwEmergencyEntry = new DetailViewEntry();
                hwEmergencyEntry.mimetype = "emergency";
                hwEmergencyEntry.data = getString(R.string.data_emergency);
                hwEmergencyEntry.kind = getString(R.string.kind_emergency);
                hwEmergencyEntry.typeString = null;
                hwEmergencyEntry.maxLines = Integer.MAX_VALUE;
                entriesObject.mEmerygencyEntries.add(hwEmergencyEntry);
            }
            hwEmergencyEntry.intent = ContactsUtils.getEmergencyIntent();
        }
    }

    private void buildQQEntry(DetailViewEntry entry, EntriesObject entriesObject) {
        if (entriesObject == null) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "buildQQEntry()  entriesObject == null");
            }
            return;
        }
        DetailViewEntry qqEntry;
        if (entriesObject.mQQEntries.size() > 0) {
            qqEntry = (DetailViewEntry) entriesObject.mQQEntries.get(0);
        } else {
            qqEntry = new DetailViewEntry();
            qqEntry.mimetype = "qq";
            qqEntry.data = getResources().getString(R.string.contact_qq);
            entriesObject.mQQEntries.add(qqEntry);
        }
        if ("vnd.android.cursor.item/vnd.com.tencent.mobileqq.voicecall".equals(entry.mimetype) || "vnd.android.cursor.item/vnd.com.tencent.mobileqq.voicecall.profile".equals(entry.mimetype)) {
            qqEntry.intent = entry.intent;
            qqEntry.mPrimaryLabel = entry.data;
        } else if ("vnd.android.cursor.item/vnd.com.tencent.mobileqq.chat".equals(entry.mimetype)) {
            qqEntry.secondaryIntent = entry.intent;
            qqEntry.mSecondaryLabel = entry.data;
        }
        entry.intent.setFlags(67108864);
    }

    private void buildWhatsAppEntry(DetailViewEntry entry, AccountType accountType, EntriesObject entriesObject) {
        if (entriesObject == null) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "buildWhatsAppEntry()  entriesObject == null");
            }
        } else if (entry != null && accountType != null && this.mContext != null) {
            DetailViewEntry whatsappEntry;
            if (entriesObject.mWhatsAppEntries.size() > 0) {
                whatsappEntry = (DetailViewEntry) entriesObject.mWhatsAppEntries.get(0);
            } else {
                whatsappEntry = new DetailViewEntry();
                whatsappEntry.mimetype = "whatsapp";
                whatsappEntry.data = String.valueOf(accountType.getDisplayLabel(this.mContext));
                entriesObject.mWhatsAppEntries.add(whatsappEntry);
            }
            if ("vnd.android.cursor.item/vnd.com.whatsapp.voip.call".equals(entry.mimetype)) {
                whatsappEntry.intent = entry.intent;
                whatsappEntry.mPrimaryLabel = entry.data;
            } else if ("vnd.android.cursor.item/vnd.com.whatsapp.profile".equals(entry.mimetype)) {
                whatsappEntry.secondaryIntent = entry.intent;
                whatsappEntry.mSecondaryLabel = entry.data;
            }
            entry.intent.setFlags(67108864);
        }
    }

    private void buildSkypeEntry(DetailViewEntry entry, AccountType accountType, EntriesObject entriesObject) {
        if (entriesObject == null) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "buildSkypeEntry()  entriesObject == null");
            }
        } else if (entry != null && accountType != null && this.mContext != null) {
            DetailViewEntry skypeEntry;
            if (entriesObject.mSkypeEntries.size() > 0) {
                skypeEntry = (DetailViewEntry) entriesObject.mSkypeEntries.get(0);
            } else {
                skypeEntry = new DetailViewEntry();
                skypeEntry.mimetype = "skype";
                skypeEntry.data = String.valueOf(accountType.getDisplayLabel(this.mContext));
                entriesObject.mSkypeEntries.add(skypeEntry);
            }
            if ("vnd.android.cursor.item/com.skype.android.skypecall.action".equals(entry.mimetype)) {
                skypeEntry.intent = entry.intent;
                skypeEntry.mPrimaryLabel = entry.data;
            } else if ("vnd.android.cursor.item/com.skype.android.chat.action".equals(entry.mimetype)) {
                skypeEntry.secondaryIntent = entry.intent;
                skypeEntry.mSecondaryLabel = entry.data;
            } else if ("vnd.android.cursor.item/com.skype.android.videocall.action".equals(entry.mimetype)) {
                skypeEntry.tertiaryIntent = entry.intent;
                skypeEntry.mTertiaryLabel = entry.data;
            }
            entry.intent.setFlags(67108864);
        }
    }

    private void setupFlattenedList(EntriesObject entriesObject, Contact contact) {
        if (entriesObject == null) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "setupFlattenedList()  entriesObject == null");
            }
            return;
        }
        flattenList(entriesObject.mPhoneEntries, entriesObject, false);
        if (!(this.mContext == null || !EspaceDialer.checkIsShowEspace(this.mContext) || this.mContactData.isUserProfile())) {
            addEspaceCallEntry(entriesObject);
        }
        if (this.mRcsCust != null) {
            this.mRcsCust.setFlattenListForOtherEntry(this);
        }
        flattenList(entriesObject.mEmailEntries, entriesObject);
        addCommonIM(entriesObject.mWeChatEntries, entriesObject.mAllEntries);
        addCommonIM(entriesObject.mQQEntries, entriesObject.mAllEntries);
        addCommonIM(entriesObject.mHwSNSEntries, entriesObject.mAllEntries);
        addCommonIM(entriesObject.mWhatsAppEntries, entriesObject.mAllEntries);
        addCommonIM(entriesObject.mSkypeEntries, entriesObject.mAllEntries);
        flattenList(entriesObject.mImEntries, entriesObject);
        flattenList(entriesObject.mNicknameEntries, entriesObject);
        flattenList(entriesObject.mWebsiteEntries, entriesObject);
        if (!(contact == null || contact.isDirectoryEntry())) {
            addNetworks(entriesObject, contact);
        }
        flattenList(entriesObject.mSipEntries, entriesObject);
        flattenList(entriesObject.mPostalEntries, entriesObject);
        flattenList(entriesObject.mEventEntries, entriesObject);
        flattenList(entriesObject.mGroupEntries, entriesObject);
        flattenList(entriesObject.mPhoneticNameEntries, entriesObject);
        boolean z = CommonUtilMethods.calcIfNeedSplitScreen() ? false : entriesObject.mAllEntries.size() > 5 ? this.mIsFirstBuildRingtoneEntry : false;
        buildRingtoneEntries(this.mCachedSimAccountType, z, entriesObject, contact);
        this.mIsFirstBuildRingtoneEntry = false;
        flattenList(entriesObject.mRingtoneEntries, entriesObject);
        flattenList(entriesObject.mRelationEntries, entriesObject);
        flattenList(entriesObject.mNoteEntries, entriesObject);
        flattenList(entriesObject.mStatusEntriesForIm, entriesObject);
        if (contact != null && contact.isUserProfile()) {
            buildHwEmergencyEntry(entriesObject);
            flattenList(entriesObject.mEmerygencyEntries, entriesObject);
        }
        if (mConDetFragmentHelper != null) {
            mConDetFragmentHelper.setupCustomFlattenedList(this, entriesObject);
        }
    }

    private void addPhoneticName(EntriesObject entriesObject, Contact contact) {
        if (contact != null && this.mContext != null) {
            String phoneticName = ContactDetailDisplayUtils.getPhoneticName(this.mContext, contact);
            if (!TextUtils.isEmpty(phoneticName)) {
                String phoneticNameKindTitle = this.mContext.getString(R.string.name_phonetic);
                DetailViewEntry entry = new DetailViewEntry();
                entry.kind = phoneticNameKindTitle;
                entry.data = phoneticName;
                entry.mimetype = "#phoneticName";
                if (entriesObject != null) {
                    entriesObject.mPhoneticNameEntries.add(entry);
                    entriesObject.mAllEntries.add(entry);
                }
            }
        }
    }

    private void addNetworks(EntriesObject entriesObjects, Contact contact) {
        if (entriesObjects == null) {
            HwLog.i(TAG, "addNetworks()  entriesObject == null");
        } else if (contact != null) {
            String attribution = ContactDetailDisplayUtils.getAttribution(this.mContext, contact);
            boolean hasAttribution = !TextUtils.isEmpty(attribution);
            int networksCount = entriesObjects.mOtherEntriesMap.keySet().size();
            ImmutableList<AccountType> types = contact.getInvitableAccountTypes();
            int invitableCount = types == null ? 0 : types.size();
            if (hasAttribution || networksCount != 0 || invitableCount != 0) {
                String networkKindTitle = this.mContext.getString(R.string.connections);
                if (hasAttribution) {
                    DetailViewEntry entry = new DetailViewEntry();
                    entry.kind = networkKindTitle;
                    entry.data = attribution;
                    entriesObjects.mAllEntries.add(entry);
                }
                for (AccountType accountType : entriesObjects.mOtherEntriesMap.keySet()) {
                    entriesObjects.mAllEntries.add(new NetworkTitleViewEntry(this.mContext, accountType));
                    for (DetailViewEntry detailEntry : (List) entriesObjects.mOtherEntriesMap.get(accountType)) {
                        detailEntry.setIsInSubSection(true);
                        entriesObjects.mAllEntries.add(detailEntry);
                    }
                }
                entriesObjects.mOtherEntriesMap.clear();
                if (invitableCount > 0) {
                    addMoreNetworks(entriesObjects, contact);
                }
            }
        }
    }

    private void addCommonIM(ArrayList<? extends ViewEntry> imEntries, ArrayList<ViewEntry> allEntries) {
        if (imEntries == null || allEntries == null) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "addCommonIM()  imEntries == null || allEntries == null");
            }
        } else if (imEntries.size() != 0) {
            for (ViewEntry detailEntry : imEntries) {
                allEntries.add(detailEntry);
            }
            imEntries.clear();
        }
    }

    private void addMoreNetworks(EntriesObject entriesObject, final Contact contact) {
        final InvitableAccountTypesAdapter popupAdapter = new InvitableAccountTypesAdapter(this.mContext, contact);
        final OnItemClickListener popupItemListener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (ContactDetailFragment.this.mListener != null && contact != null) {
                    ContactDetailFragment.this.mListener.onItemClicked(MoreContactUtils.getInvitableIntent(popupAdapter.getItem(position), contact.getLookupUri()));
                }
            }
        };
        OnClickListener onClickListener = new OnClickListener() {
            public void onClick(View v) {
                ContactDetailFragment.this.showListPopup(v, popupAdapter, popupItemListener);
            }
        };
        if (entriesObject != null) {
            entriesObject.mAllEntries.add(new AddConnectionViewEntry(this.mContext, onClickListener));
        }
    }

    private void addEspaceCallEntry(EntriesObject entriesObject) {
        if (entriesObject == null) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "addEspaceCallEntry()  entriesObject == null");
            }
        } else if (entriesObject.mPhoneEntries == null || entriesObject.mPhoneEntries.size() != 0) {
            entriesObject.mAllEntries.add(getEspaceCallEntry());
        }
    }

    private DetailViewEntry getEspaceCallEntry() {
        DetailViewEntry lIPCallEntry = new DetailViewEntry();
        lIPCallEntry.data = this.mContext.getString(R.string.name_escape);
        lIPCallEntry.mimetype = "vnd.android.cursor.item/phone_v2";
        lIPCallEntry.mCustom_mimetype = "ip_call";
        lIPCallEntry.kind = this.mContext.getString(R.string.phoneLabelsGroup);
        lIPCallEntry.typeString = "";
        return lIPCallEntry;
    }

    public void flattenList(ArrayList<DetailViewEntry> arrayList) {
    }

    public void flattenList(ArrayList<DetailViewEntry> entries, Object object) {
        if (object instanceof EntriesObject) {
            flattenList(entries, (EntriesObject) object, false);
        }
    }

    public void flattenList(ArrayList<DetailViewEntry> entries, EntriesObject entriesObject, boolean clearContent) {
        if (entriesObject == null || entries == null) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "flattenList()  entriesObject == null || entries == null");
            }
        } else if (isAdded()) {
            ArrayList<DetailViewEntry> lEntries = (ArrayList) entries.clone();
            int count = lEntries.size();
            boolean isPhoneEntry = false;
            if (count > 0) {
                String kind = ((DetailViewEntry) lEntries.get(0)).kind;
                if ("vnd.android.cursor.item/phone_v2".equals(((DetailViewEntry) lEntries.get(0)).mimetype)) {
                    isPhoneEntry = true;
                    kind = getString(R.string.phoneLabelsGroup);
                    if (entriesObject.mIPCallEntries != null) {
                        entriesObject.mIPCallEntries.clear();
                    }
                }
            }
            for (int i = 0; i < count; i++) {
                entriesObject.mAllEntries.add((ViewEntry) lEntries.get(i));
                if (isPhoneEntry && entriesObject.mIPCallEntries != null) {
                    entriesObject.mIPCallEntries.add((DetailViewEntry) lEntries.get(i));
                }
            }
            if (clearContent) {
                entries.clear();
            }
        }
    }

    private void handleGroupMembership(ArrayList<String> groups, List<GroupMetaData> groupMetaData, long groupId) {
        if (groupMetaData != null) {
            for (GroupMetaData group : groupMetaData) {
                if (group.getGroupId() == groupId) {
                    if (!(group.isDefaultGroup() || group.isFavorites())) {
                        String title = CommonUtilMethods.parseGroupDisplayName(group.getAccountType(), group.getTitle(), this.mContext, group.getmSync4(), group.getmTitleRes(), group.getmResPackage());
                        if (!(TextUtils.isEmpty(title) || groups.contains(title))) {
                            groups.add(title);
                        }
                    }
                }
            }
        }
    }

    @VisibleForTesting
    public static void buildImActions(Context context, DetailViewEntry entry, ImDataItem im) {
        int protocol = 0;
        boolean isEmail = im.isCreatedFromEmail();
        String host;
        if (isEmail || im.isProtocolValid()) {
            String data = im.getData();
            if (!TextUtils.isEmpty(data)) {
                if (isEmail) {
                    protocol = 5;
                } else {
                    protocol = im.getProtocol().intValue();
                }
                if (protocol == 5) {
                    int chatCapability = im.getChatCapability();
                    entry.chatCapability = chatCapability;
                    entry.typeString = Im.getProtocolLabel(context.getResources(), 5, null).toString();
                    if ((chatCapability & 4) != 0) {
                        entry.intent = new Intent("android.intent.action.SENDTO", Uri.parse("xmpp:" + data + "?message"));
                        entry.secondaryIntent = new Intent("android.intent.action.SENDTO", Uri.parse("xmpp:" + data + "?call"));
                    } else if ((chatCapability & 1) != 0) {
                        entry.intent = new Intent("android.intent.action.SENDTO", Uri.parse("xmpp:" + data + "?message"));
                        entry.secondaryIntent = new Intent("android.intent.action.SENDTO", Uri.parse("xmpp:" + data + "?call"));
                    } else {
                        entry.intent = new Intent("android.intent.action.VIEW", Uri.parse("https://plus.google.com/hangouts/_/UNIQUE_HANGOUT_ID"));
                    }
                } else {
                    host = im.getCustomProtocol();
                    if (protocol != -1) {
                        host = ContactsUtils.lookupProviderNameFromId(protocol);
                    }
                    if (!(host == null || TextUtils.isEmpty(host))) {
                        String str = "android.intent.action.SENDTO";
                        entry.intent = new Intent(str, new Builder().scheme("imto").authority(host.toLowerCase(Locale.getDefault())).appendPath(data).build());
                    }
                }
                return;
            }
            return;
        }
        if (im.getData() != null) {
            if (im.isProtocolValid()) {
                protocol = im.getProtocol().intValue();
            }
            host = ContactsUtils.lookupProviderNameFromId(protocol);
            if (!(host == null || TextUtils.isEmpty(host))) {
                str = "android.intent.action.SENDTO";
                entry.intent = new Intent(str, new Builder().scheme("imto").authority(host.toLowerCase(Locale.getDefault())).appendPath(im.getData()).build());
            }
        }
    }

    private void showListPopup(View anchorView, ListAdapter adapter, final OnItemClickListener onItemClickListener) {
        dismissPopupIfShown();
        this.mPopup = new ListPopupWindow(this.mContext, null);
        this.mPopup.setAnchorView(anchorView);
        this.mPopup.setWidth(anchorView.getWidth());
        this.mPopup.setHwFullDim();
        this.mPopup.setAdapter(adapter);
        this.mPopup.setModal(true);
        this.mPopup.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemClickListener.onItemClick(parent, view, position, id);
                ContactDetailFragment.this.dismissPopupIfShown();
            }
        });
        this.mPopup.show();
    }

    private void dismissPopupIfShown() {
        if (this.mPopup != null && this.mPopup.isShowing()) {
            this.mPopup.dismiss();
        }
        this.mPopup = null;
    }

    public void onAccountSelectorCancelled() {
    }

    public void onAccountChosen(AccountWithDataSet account, Bundle extraArgs) {
        createCopy(account);
    }

    private void createCopy(AccountWithDataSet account) {
        if (!(this.mContactData == null || this.mListener == null)) {
            this.mListener.onCreateRawContactRequested(this.mContactData.getContentValues(), account);
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (this.mListener != null) {
            ViewEntry entry = this.mAdapter.getItem(position);
            if (entry != null) {
                entry.click(view, this.mListener);
            }
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        if (this.mAdapter != null) {
            ViewEntry viewEntry = this.mAdapter.getItem(info.position);
            if (viewEntry instanceof DetailViewEntry) {
                DetailViewEntry selectedEntry = (DetailViewEntry) viewEntry;
                String selectedMimeType = selectedEntry.mimetype;
                if (!"vnd.android.huawei.cursor.item/ringtone".equals(selectedMimeType) && !selectedEntry.isEspaceCallEntry() && !selectedEntry.isMessageEntry()) {
                    if (this.mRcsCust == null || !this.mRcsCust.checkOtherEntry(selectedEntry, this.mAdapter.getRcsContactDetailAdapter())) {
                        if (ContactStaticCache.isMimeTypeEqual(selectedMimeType, 1, "vnd.android.cursor.item/phone_v2")) {
                            StatisticalHelper.report(2020);
                        }
                        if (isNeedCopyTypeString(selectedEntry)) {
                            menu.setHeaderTitle(selectedEntry.typeString);
                        } else {
                            menu.setHeaderTitle(selectedEntry.data);
                        }
                        menu.add(0, 1000, 0, getString(R.string.copy_text));
                        boolean isUniqueMimeType = true;
                        PhoneNumberHelper phoneNumberHelper = new PhoneNumberHelper(getResources());
                        if ("vnd.android.cursor.item/phone_v2".equals(selectedMimeType) && EmuiFeatureManager.isSystemVoiceCapable() && !isProfile() && !phoneNumberHelper.isSipNumber(selectedEntry.data)) {
                            menu.add(0, 1003, 0, getString(R.string.recentCalls_editBeforeCall));
                            isUniqueMimeType = this.mIsUniqueNumber;
                        } else if ("vnd.android.cursor.item/email_v2".equals(selectedMimeType)) {
                            isUniqueMimeType = this.mIsUniqueEmail;
                        }
                        if (this.mContactData == null || !(this.mContactData.isYellowPage() || this.mContactData.isDirectoryEntry())) {
                            if (selectedEntry.isPrimary) {
                                menu.add(0, 1001, 0, getString(R.string.clear_default));
                            } else if (!isUniqueMimeType) {
                                menu.add(0, 1002, 0, getString(R.string.set_default));
                            }
                        }
                        if (mConDetFragmentHelper != null) {
                            mConDetFragmentHelper.customizeContextMenu(menu);
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "onCreateContextMenu viewEntry is not DetailViewEntry");
            }
        }
    }

    public boolean isNeedCopyTypeString(DetailViewEntry selectedEntry) {
        if ("capability".equals(selectedEntry.mimetype) && ("website".equals(selectedEntry.mCustom_mimetype) || "weibo".equals(selectedEntry.mCustom_mimetype) || "weixin".equals(selectedEntry.mCustom_mimetype) || "address".equals(selectedEntry.mCustom_mimetype))) {
            return true;
        }
        return false;
    }

    public boolean onContextItemSelected(MenuItem item) {
        try {
            AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case 1000:
                    copyToClipboard(menuInfo.position);
                    return true;
                case 1001:
                    if (this.mFragment != null && (this.mFragment instanceof ContactInfoFragment)) {
                        ((ContactInfoFragment) this.mFragment).setResetFlag(false);
                    }
                    clearDefaultContactMethod(getContext().getApplicationContext(), this.mListView.getItemIdAtPosition(menuInfo.position));
                    return true;
                case 1002:
                    if (this.mFragment != null && (this.mFragment instanceof ContactInfoFragment)) {
                        ((ContactInfoFragment) this.mFragment).setResetFlag(false);
                    }
                    setDefaultContactMethod(getContext().getApplicationContext(), this.mListView.getItemIdAtPosition(menuInfo.position), false);
                    return true;
                case 1003:
                    if (this.mAdapter == null) {
                        return false;
                    }
                    ViewEntry viewEntry = this.mAdapter.getItem(menuInfo.position);
                    if (viewEntry instanceof DetailViewEntry) {
                        DetailViewEntry detailViewEntry = (DetailViewEntry) viewEntry;
                        CharSequence number = detailViewEntry.data;
                        if (ContactStaticCache.isMimeTypeEqual(detailViewEntry.mimetype, 1, "vnd.android.cursor.item/phone_v2")) {
                            number = PhoneNumberFormatter.parsePhoneNumber(number.toString());
                        }
                        Intent intent = new Intent("android.intent.action.DIAL", CallUtil.getCallUri(number.toString()));
                        intent.setPackage("com.android.contacts");
                        getActivity().startActivity(intent);
                        StatisticalHelper.report(2025);
                        return true;
                    }
                    if (HwLog.HWFLOW) {
                        HwLog.i(TAG, "onContextItemSelected viewEntry is not DetailViewEntry");
                    }
                    return false;
                default:
                    return super.onContextItemSelected(item);
            }
        } catch (ClassCastException e) {
            HwLog.e(TAG, "bad menuInfo", e);
            return false;
        }
    }

    public static void setDefaultContactMethod(Context context, long id, boolean isSmart) {
        if (context != null && id > -1) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "setDefaultContactMethod isSmart:" + isSmart);
            }
            context.startService(ContactSaveService.createSetSuperPrimaryIntent(context, id, isSmart));
        }
    }

    public String getPrimaryNumber() {
        return this.mPrimaryNumber;
    }

    private static void clearDefaultContactMethod(Context context, long id) {
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "clearDefaultContactMethod ");
        }
        context.startService(ContactSaveService.createClearPrimaryIntent(context, id));
    }

    private void copyToClipboard(int viewEntryPosition) {
        if (this.mAdapter != null) {
            ViewEntry viewEntry = this.mAdapter.getItem(viewEntryPosition);
            if (viewEntry instanceof DetailViewEntry) {
                CharSequence textToCopy;
                DetailViewEntry detailViewEntry = (DetailViewEntry) viewEntry;
                if (isNeedCopyTypeString(detailViewEntry)) {
                    textToCopy = detailViewEntry.typeString;
                } else {
                    textToCopy = detailViewEntry.data;
                }
                if (!TextUtils.isEmpty(textToCopy)) {
                    if (ContactStaticCache.isMimeTypeEqual(detailViewEntry.mimetype, 1, "vnd.android.cursor.item/phone_v2")) {
                        textToCopy = PhoneNumberFormatter.parsePhoneNumber(textToCopy.toString());
                        if (!TextUtils.isEmpty(textToCopy)) {
                            textToCopy = ContactsUtils.removeDashesAndBlanks(textToCopy.toString());
                        }
                    }
                    ((ClipboardManager) getActivity().getSystemService("clipboard")).setPrimaryClip(new ClipData(detailViewEntry.typeString, "vnd.android.cursor.item/email_v2".equals(detailViewEntry.mimetype) ? new String[]{"text/plain", detailViewEntry.mimetype} : new String[]{detailViewEntry.mimetype}, new Item(textToCopy)));
                    return;
                }
                return;
            }
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "copyToClipboard viewEntry is not DetailViewEntry");
            }
        }
    }

    public void simStateChanged(int aSubScription) {
        updateSimPresentFlag();
    }

    public void processActivityResult(int aRequestCode, int aResultCode, Intent aData) {
        if (aResultCode != -1) {
            return;
        }
        if (aRequestCode == 1000 || aRequestCode == 1001) {
            Uri lPickedUri = (Uri) aData.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
            if (lPickedUri != null) {
                RingtoneManager.getRingtone(this.mContext, lPickedUri);
            } else {
                lPickedUri = aData.getData();
            }
            Message msg = new Message();
            msg.obj = lPickedUri;
            this.mHandler.sendMessageDelayed(msg, 500);
            if (lPickedUri != null && CommonUtilMethods.isUriOfExternalSource(lPickedUri)) {
                this.mRingtoneFilePathPreference.edit().putString(lPickedUri.toString(), CommonUtilMethods.getPathFromUri(this.mContext, lPickedUri)).commit();
            }
        }
    }

    private int getRingtonePosition() {
        if (this.mCachedEntriesObject == null) {
            return -1;
        }
        for (int i = 0; i < this.mCachedEntriesObject.mAllEntries.size(); i++) {
            if (((ViewEntry) this.mCachedEntriesObject.mAllEntries.get(i)).getViewType() == 0) {
                String mimetype = ((DetailViewEntry) this.mCachedEntriesObject.mAllEntries.get(i)).mimetype;
                if (mimetype != null && "vnd.android.huawei.cursor.item/ringtone".equals(mimetype)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void updateRingtoneOnContact() {
        if (this.mContext != null) {
            if (this.mLookupUri == null && this.mContactData != null) {
                this.mLookupUri = this.mContactData.getLookupUri();
            }
            this.mContext.startService(ContactSaveService.createSetRingtone(this.mContext, this.mLookupUri, this.mRingtoneString));
            if (this.mAdapter != null) {
                this.mAdapter.notifyDataSetChanged();
            }
        }
    }

    protected void getEspaceDialog() {
        if (this.mCachedEntriesObject != null) {
            UserAdapter adapter = new UserAdapter(this.mCachedEntriesObject.mPhoneEntries);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            if (adapter.getCount() == 1) {
                String number = ContactsUtils.removeDashesAndBlanks(((DetailViewEntry) this.mCachedEntriesObject.mPhoneEntries.get(0)).data);
                if (this.mContactData != null) {
                    EspaceDialer.dialVoIpCall(this.mContext, number, this.mContactData.getDisplayName());
                }
            } else {
                DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ContactDetailFragment.this.mIsIPDialogVisible = false;
                        String number = ContactsUtils.removeDashesAndBlanks(((DetailViewEntry) ContactDetailFragment.this.mCachedEntriesObject.mPhoneEntries.get(which)).data);
                        if (ContactDetailFragment.this.mContactData != null) {
                            EspaceDialer.dialVoIpCall(ContactDetailFragment.this.mContext, number, ContactDetailFragment.this.mContactData.getDisplayName());
                        }
                    }
                };
                builder.setTitle(getString(R.string.espace_dialog_title));
                builder.setAdapter(adapter, clickListener);
                this.mIsIPDialogVisible = true;
                this.mIpDialog = builder.create();
                this.mIpDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        ContactDetailFragment.this.mIsIPDialogVisible = false;
                    }
                });
                this.mIpDialog.show();
            }
        }
    }

    protected void getMessageDialog(DetailViewEntry aEntry) {
        if (this.mCachedEntriesObject != null) {
            this.mIPEntry = aEntry;
            ArrayAdapter<String> adapter = new ArrayAdapter(this.mContext, R.layout.select_dialog_item);
            final HashMap<Integer, String> numbersDetails = new HashMap();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            int mPrimarypos = -1;
            for (int i = 0; i < this.mCachedEntriesObject.mIPCallEntries.size(); i++) {
                adapter.insert(((DetailViewEntry) this.mCachedEntriesObject.mIPCallEntries.get(i)).data, i);
                if (((DetailViewEntry) this.mCachedEntriesObject.mIPCallEntries.get(i)).isPrimary) {
                    mPrimarypos = i;
                }
                numbersDetails.put(Integer.valueOf(i), ((DetailViewEntry) this.mCachedEntriesObject.mIPCallEntries.get(i)).data);
            }
            if (adapter.getCount() == 1) {
                try {
                    startActivity(new Intent("android.intent.action.SENDTO", Uri.fromParts("smsto", (String) numbersDetails.get(Integer.valueOf(0)), null)));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this.mContext, R.string.quickcontact_missing_app_Toast, 0).show();
                }
            } else if (mPrimarypos >= 0) {
                startActivity(new Intent("android.intent.action.SENDTO", Uri.fromParts("smsto", (String) numbersDetails.get(Integer.valueOf(mPrimarypos)), null)));
            } else {
                DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ContactDetailFragment.this.mIsSMSDialogVisible = false;
                        try {
                            ContactDetailFragment.this.startActivity(new Intent("android.intent.action.SENDTO", Uri.fromParts("smsto", (String) numbersDetails.get(Integer.valueOf(which)), null)));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(ContactDetailFragment.this.mContext, R.string.quickcontact_missing_app_Toast, 0).show();
                        }
                    }
                };
                builder.setTitle(getActivity().getString(R.string.contact_menu_send_message));
                builder.setSingleChoiceItems(adapter, -1, clickListener);
                this.mIsSMSDialogVisible = true;
                this.mMessageDialog = builder.create();
                this.mMessageDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        ContactDetailFragment.this.mIsSMSDialogVisible = false;
                    }
                });
                this.mMessageDialog.show();
            }
        }
    }

    public void setIPEntry(DetailViewEntry entry) {
        this.mIPEntry = entry;
    }

    public ArrayList<DetailViewEntry> getIPCallEntries() {
        if (this.mCachedEntriesObject != null) {
            return this.mCachedEntriesObject.mIPCallEntries;
        }
        return new ArrayList();
    }

    public RcsContactDetailFragmentHelper getRcsCust() {
        return this.mRcsCust;
    }

    public void setSmsDialogVisibleStatus(boolean SMSDialogStatus) {
        this.mIsSMSDialogVisible = SMSDialogStatus;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mContext.unregisterReceiver(this.sdCardUnmountBroadcastRec);
        if (this.mMultiSimEnable) {
            SimFactoryManager.removeSimStateListener(this);
        }
        if (EmuiFeatureManager.isNumberCapabilityEnable() && this.mMarkManager != null) {
            this.mMarkManager.destory();
        }
        if (this.mRcsCust != null) {
            this.mRcsCust.handleCustomizationsOnDestroy(this.mContext);
        }
        this.mContext = null;
        if (this.mMarkDefault != null) {
            BackgroundGenricHandler.getInstance().removeCallbacks(this.mMarkDefault);
        }
    }

    public int getDefaultSimcard() {
        return this.mDefaultSimcard;
    }

    public String getFirstPhoneNumber() {
        if (!TextUtils.isEmpty(this.mPrimaryNumber)) {
            return this.mPrimaryNumber;
        }
        if (this.mPhoneNumbers == null || this.mPhoneNumbers.size() <= 0) {
            return null;
        }
        return (String) this.mPhoneNumbers.get(0);
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
                HwLog.i(TAG, "isSecondSubIdle:" + isSecondSubIdle);
                HwLog.i(TAG, "isFirstSubIdle:" + isFirstSubIdle);
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

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void addPhoneNumAndUriInArraylist(String num, Uri uri) {
        if (!this.mPhoneNumbers.contains(num)) {
            this.mPhoneNumbers.add(num);
            this.mPhoneNumbersUri.put(num, uri);
        }
    }

    private void addFormattedPhoneNumAndUriInArrayLis(String num, Uri uri) {
        if (!this.mFormattedPhoneNum.contains(num)) {
            this.mFormattedPhoneNum.add(num);
            this.mFormattedPhoneNumUri.put(num, uri);
        }
    }

    public ArrayList<String> getFormattedPhoneNum() {
        return this.mFormattedPhoneNum;
    }

    public HashMap<String, Uri> getFormattedPhoneNumUri() {
        return this.mFormattedPhoneNumUri;
    }

    private void addPhoneNumCountryIsoInHashMap(String num, String normalizedNumbe) {
        if (!this.mPhoneNumberCountryIsoHM.containsKey(num)) {
            String countryIso;
            if (normalizedNumbe == null || normalizedNumbe.length() <= 0) {
                countryIso = CommonUtilMethods.getCountryIsoFromDbNumberHw(num);
            } else {
                countryIso = CommonUtilMethods.getCountryIsoFromDbNumberHw(normalizedNumbe);
            }
            this.mPhoneNumberCountryIsoHM.put(num, countryIso);
        }
    }

    public void onStop() {
        if (this.mIpDialog != null && this.mIpDialog.isShowing()) {
            this.mIpDialog.dismiss();
        }
        try {
            EspaceDialer.closeDialog();
        } catch (IllegalArgumentException e) {
            HwLog.e(TAG, "ContactDetailFragment stop error.");
        }
        if (this.mRcsCust != null) {
            this.mRcsCust.closeFTDialog();
        }
        super.onStop();
    }

    public void onContactLoaded(Contact contact) {
        this.mIsNoNamedContact = false;
        this.mContactData = contact;
        if (isAdded() && this.isNeedFlush) {
            Intent intent = ((ContactInfoFragment) this.mFragment).getIntent();
            long listContactId = intent.getLongExtra("EXTRA_URI_CONTACT_ID", 0);
            if (this.mContactData.getId() != listContactId) {
                RadarIMonitorUpload.capturePbListToDetailException(listContactId, intent.getStringExtra("EXTRA_CONTACT_ACCOUNT_TYPE"), "Pblist uri: " + intent.getStringExtra("EXTRA_LIST_TO_DETAIL_URI") + " Detail uri: " + contact.getUri());
            }
            synchronized (this.mLock) {
                EntriesObject object = buildEntries(contact);
                if (object != null) {
                    buildSubEntriesObject(object);
                    setListViewData(object, contact);
                }
            }
        }
    }

    private String setNumberStringForAr(String dataString) {
        if (TextUtils.isEmpty(dataString)) {
            return null;
        }
        StringBuilder tempStringBuilder = new StringBuilder();
        for (int i = 0; i < dataString.length(); i++) {
            char c = dataString.charAt(i);
            if (c == '(') {
                tempStringBuilder.append('‪');
                tempStringBuilder.append(c);
            } else if (c == ')') {
                tempStringBuilder.append(c);
                tempStringBuilder.append('‬');
            } else {
                tempStringBuilder.append(c);
            }
        }
        return tempStringBuilder.toString();
    }

    public void setQRcodeEntryListener(QRcodeEntryListener qrCodeEntryListener) {
        this.mQRcodeEntryListener = qrCodeEntryListener;
    }

    public boolean isProfile() {
        if (this.mContactData != null) {
            return this.mContactData.isUserProfile();
        }
        if (this.mFragment == null || !(this.mFragment instanceof ContactInfoFragment)) {
            return false;
        }
        return ((ContactInfoFragment) this.mFragment).isProfileContact();
    }

    private void buildSubEntriesObject(EntriesObject object) {
        if (object != null) {
            Collapser.collapseList(object.mNameEntries, false);
            Collapser.collapseList(object.mCompanyEntries, false);
            Collapser.collapseList(object.mPositionEntries, false);
            this.mSubEntriesObject = new ContactEntriesObject();
            this.mSubEntriesObject.companyEntries = object.mCompanyEntries;
            this.mSubEntriesObject.emailEntries = object.mEmailEntries;
            this.mSubEntriesObject.eventEntries = object.mEventEntries;
            this.mSubEntriesObject.imEntries = object.mImEntries;
            this.mSubEntriesObject.nameEntries = object.mNameEntries;
            this.mSubEntriesObject.nicknameEntries = object.mNicknameEntries;
            this.mSubEntriesObject.noteEntries = object.mNoteEntries;
            this.mSubEntriesObject.phoneEntries = object.mPhoneEntries;
            this.mSubEntriesObject.phoneticNameEntries = object.mPhoneticNameEntries;
            this.mSubEntriesObject.positionEntries = object.mPositionEntries;
            this.mSubEntriesObject.postalEntries = object.mPostalEntries;
            this.mSubEntriesObject.qrcodeDataInfo = object.mQrcodeDataInfo;
            this.mSubEntriesObject.relationEntries = object.mRelationEntries;
            this.mSubEntriesObject.sipEntries = object.mSipEntries;
            this.mSubEntriesObject.websiteEntries = object.mWebsiteEntries;
        }
    }

    public ContactEntriesObject getEntriesObject() {
        return this.mSubEntriesObject;
    }
}
