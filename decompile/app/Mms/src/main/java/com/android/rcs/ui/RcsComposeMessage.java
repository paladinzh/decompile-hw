package com.android.rcs.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.amap.api.services.core.AMapException;
import com.android.messaging.util.BugleActivityUtil;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.Conversation.ConversationQueryHandler;
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.SlideshowModel;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.SmsMessageSender;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.HwCustComposeMessageImpl;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageListView;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.RichMessageEditor;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.data.RcsConversationUtils;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.harassmentinterception.service.BlacklistCommonUtils;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.PeopleActionBar.PeopleActionBarAdapter;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.MmsCommon;
import com.huawei.mms.util.NumberUtils;
import com.huawei.mms.util.NumberUtils.AddrMatcher;
import com.huawei.mms.util.ResEx;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.commonInterface.IfMsgplusCb;
import com.huawei.rcs.commonInterface.IfMsgplusCb.Stub;
import com.huawei.rcs.commonInterface.metadata.Capabilities;
import com.huawei.rcs.commonInterface.metadata.PeerInformation;
import com.huawei.rcs.media.RcsMediaFileUtils;
import com.huawei.rcs.telephony.RcseTelephonyExt.RcsAttachments;
import com.huawei.rcs.ui.RcsAsyncIconLoader;
import com.huawei.rcs.ui.RcsFileTransMessageItem;
import com.huawei.rcs.ui.RcsFileTransMessageListItem;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.ui.RcsPeopleActionBar.PeopleActionBarAdapterExt;
import com.huawei.rcs.ui.RcsVideoPreviewActivity;
import com.huawei.rcs.ui.RcseScrollListener;
import com.huawei.rcs.util.RCSConst;
import com.huawei.rcs.util.RcsXmlParser;
import com.huawei.rcs.utils.RcsFFMpegMediaScannerNotifier;
import com.huawei.rcs.utils.RcsNetworkAdapter;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsProfile.BindServiceListener;
import com.huawei.rcs.utils.RcsProfileUtils;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcsUtility;
import com.huawei.rcs.utils.RcsUtility.FileInfo;
import com.huawei.rcs.utils.RcseCompressUtil;
import com.huawei.rcs.utils.RcseMmsExt;
import com.huawei.rcs.utils.RcseMmsExt.SendModeSetListener;
import com.huawei.rcs.utils.map.abs.RcsMapLoader;
import com.huawei.rcs.utils.map.abs.RcsMapLoaderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

public class RcsComposeMessage implements OnClickListener {
    private static final Uri DELETE_EMPTY_RCS_THREAD = Uri.parse("content://rcsim/delete_empty_rcs_threads");
    private static final boolean HAS_DRM_CONFIG = SystemProperties.get("ro.huawei.cust.oma_drm", "false").equals("true");
    private static String SEND_SWITCHER = "send_switcher";
    private static String TAG = "RcsComposeMessage";
    private static boolean mFtToMmsUndelivedNotDeletChat = false;
    private static boolean mIsSendFtToMms = false;
    private static ArrayList receivelist = new ArrayList();
    private static final Uri sXmsReceivedUri = Uri.parse("content://xms/received");
    private static HashMap<Long, String> sendlist = new HashMap();
    private int imMsgCount = 0;
    boolean isCompressActivityStart = false;
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private boolean isShowComposing = false;
    private boolean issupLs = false;
    PeopleActionBarAdapter mActionBarAdapter = null;
    public PeopleActionBarAdapterExt mActionbarAdapterExt = new PeopleActionBarAdapterExt() {
        public void createGroupChat() {
            if (RcsComposeMessage.this.mActionBarAdapter != null) {
                Intent contactIntent = new Intent();
                contactIntent.setAction("android.intent.action.PICK");
                contactIntent.setType("vnd.android.cursor.item/rcs_contacts_for_message");
                contactIntent.putExtra("com.huawei.community.action.MULTIPLE_PICK", true);
                contactIntent.putExtra("from_activity_key", 1);
                ArrayList<String> memberList = new ArrayList();
                memberList.add(RcsComposeMessage.this.mActionBarAdapter.getNumber());
                contactIntent.putStringArrayListExtra("list_phonenumber_from_forward", memberList);
                boolean isSelfChat = RcsComposeMessage.this.isSelfChat();
                MLog.d(RcsComposeMessage.TAG, "startChooseContact isSelfChat:" + isSelfChat);
                contactIntent.putExtra("is_self_chat", isSelfChat);
                RcsComposeMessage.this.mFragment.startActivityForResult(contactIntent, 123);
            }
        }

        public boolean isRcsGroupChat() {
            return false;
        }

        public void showRcsGroupChatDetail() {
        }
    };
    private BindServiceListener mBindServiceListener = new MyBindServiceListener();
    private IHwCustComposeMessageCallback mCallback;
    private RcsChatMessageForwarder mChatForwarder = new RcsChatMessageForwarder();
    private ComposeEventHdlr mComposeEventHdlr;
    Context mContext;
    RcsFileTransMessageForwarder mForwarder = new RcsFileTransMessageForwarder();
    private ComposeMessageFragment mFragment;
    private AlertDialog mFtresumeStopDialog = null;
    private boolean mHandleSendIntentRcs = false;
    private boolean mHasDraftBeforeSendFt = false;
    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_REASON = "reason";
        String SYSTEM_RECENTAPPS_KEY = "recentapps";

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
                String reason = intent.getStringExtra(this.SYSTEM_REASON);
                if (TextUtils.equals(reason, this.SYSTEM_HOME_KEY) || TextUtils.equals(reason, this.SYSTEM_RECENTAPPS_KEY)) {
                    RcsComposeMessage.this.resumeListClear();
                }
                if (TextUtils.equals(reason, this.SYSTEM_HOME_KEY)) {
                    RcsComposeMessage.this.resumeListClear();
                }
            }
        }
    };
    private boolean mIgnoreLoginStatus = false;
    private boolean mIsBlackNumber = false;
    private boolean mIsCanSendImCache = false;
    private boolean mIsExitingActivity = false;
    private boolean mIsFromContacts;
    private boolean mIsFtToMmsDeletExitActivity = false;
    private boolean mIsFullScreenInput = false;
    private boolean mIsNeedResetSendMode = false;
    private boolean mIsNetWork = false;
    private boolean mIsPickContacts = false;
    private int mIsRcsModeSendVcard = 0;
    private boolean mIsRecordVideo;
    private boolean mIsSendComposingStatus = true;
    private HashMap<Integer, IfMsgplusCbImpl> mMsgplusListeners = new HashMap();
    private boolean mNewTaskFlag = false;
    private boolean mNoneFtCapability = false;
    private ContentObserver mObXmsChange = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            boolean isSameNumber = true;
            super.onChange(selfChange, uri);
            if (RcsComposeMessage.this.is1To1ChatExisted()) {
                String address = (String) uri.getPathSegments().get(1);
                if (!TextUtils.isEmpty(address)) {
                    if (AddrMatcher.isNumberMatch(NumberUtils.normalizeNumber(((Contact) RcsComposeMessage.this.mFragment.getRecipients().get(0)).getNumber()), NumberUtils.normalizeNumber(address)) <= 0) {
                        isSameNumber = false;
                    }
                    if (isSameNumber && RcseMmsExt.isRcsMode()) {
                        Intent switchIntent = new Intent();
                        switchIntent.putExtra("send_mode", 0);
                        MLog.d(RcsComposeMessage.TAG, "database change will updateRcsMode");
                        RcseMmsExt.updateRcsMode(switchIntent);
                    }
                }
            }
        }
    };
    private Menu mOptionMenu;
    private boolean mPreIsEmpty = false;
    private Handler mRcseEventHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.obj;
            if (bundle != null) {
                long msgId = 0;
                String recipient;
                String phoneNumber;
                boolean isSamePhoneNumber;
                Intent switchIntent;
                switch (msg.what) {
                    case Place.TYPE_SUBWAY_STATION /*89*/:
                        msgId = bundle.getLong("ft.msg_id");
                        MLog.d(RcsComposeMessage.TAG + " FileTrans : ", "CODE_ACCEPT msgId: " + msgId);
                        if (!RcsTransaction.checksize(bundle.getLong("totalSize"), RcsComposeMessage.this.mContext)) {
                            RcsTransaction.acceptfile(msgId, 1);
                            break;
                        }
                        if (RcsComposeMessage.this.mFragment.isRunning()) {
                            RcsComposeMessage.this.FTisBig();
                        } else {
                            Toast.makeText(RcsComposeMessage.this.mContext, R.string.storage_is_not_enough_totast, 0).show();
                        }
                        RcsTransaction.rejectFile(msgId, 1);
                        break;
                    case Place.TYPE_SYNAGOGUE /*90*/:
                        msgId = bundle.getLong("ft.msg_id");
                        RcsTransaction.rejectFile(msgId, 1);
                        break;
                    case 97:
                        RcsComposeMessage.this.chooseResendModeFt(bundle);
                        msgId = bundle.getLong("fileTransId");
                        RcsComposeMessage.this.resumeRemove(msgId, true);
                        break;
                    case AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR /*1101*/:
                        MLog.d(RcsComposeMessage.TAG + " FileTrans : ", "--- log FTstatus: =" + bundle.getString("rcs.ft.status"));
                        msgId = bundle.getLong("ft.msg_id");
                        RcsComposeMessage.this.notifyFtStatus(bundle);
                        RcsComposeMessage.this.scrollWhenMsgFailed(bundle);
                        break;
                    case AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT /*1102*/:
                        MLog.d(RcsComposeMessage.TAG + " FileTrans : ", "Update file transfer progress.");
                        RcsComposeMessage.this.updateFileTransProgress(bundle);
                        return;
                    case 1105:
                        MLog.d(RcsComposeMessage.TAG + " FileTrans : ", "Refresh current file transfer item.");
                        RcsFileTransMessageListItem ftmli = RcsUtility.getMessageListItemById(bundle.getLong("ft.msg_id"), RcsComposeMessage.this.mCallback.getMessageListView());
                        if (ftmli != null) {
                            ftmli.refreshListItem(RcsComposeMessage.this.mCallback.getHwCustMsgListAdapter() != null ? RcsComposeMessage.this.mCallback.getHwCustMsgListAdapter().isScrollRcs() : false);
                        }
                        return;
                    case 1109:
                        RcsComposeMessage.this.chooseFtResendOrWait(bundle);
                        break;
                    case 1116:
                        long resume_threadid = bundle.getLong("resume_threadid");
                        Conversation mConversation = RcsComposeMessage.this.mFragment.getConversation();
                        MLog.d(RcsComposeMessage.TAG, "FILE_KEEP_RESUME : =" + resume_threadid + " mIsRunning" + RcsComposeMessage.this.mFragment.isRunning());
                        if (mConversation.getHwCust() != null) {
                            MLog.d(RcsComposeMessage.TAG, "FILE_KEEP_RESUME  mConversation threadid = " + mConversation.getHwCust().ensureFtSendThreadId(mConversation, RcsComposeMessage.this.mContext));
                            if (RcsComposeMessage.this.mFragment.isRunning()) {
                                if (mConversation.getHwCust().ensureFtSendThreadId(mConversation, RcsComposeMessage.this.mContext) == resume_threadid) {
                                    RcsComposeMessage.this.ftresumeStopDialog(bundle);
                                    break;
                                }
                            }
                        }
                        break;
                    case 1301:
                        String msgPlusNumber = bundle.getString("rcs.composing.peer");
                        String groupId = bundle.getString("group_id");
                        boolean rspEvent = bundle.getBoolean("rcs.composing.status");
                        MLog.d(RcsComposeMessage.TAG, "iscomposing: telUri= ***, composingType=" + rspEvent);
                        if (groupId == null) {
                            RcsComposeMessage.this.composingEventHandler(msgPlusNumber, rspEvent);
                            break;
                        }
                        break;
                    case 1302:
                        if (RcsComposeMessage.this.mFragment.isRunning() && RcsComposeMessage.this.is1To1ChatExisted()) {
                            recipient = ((Contact) RcsComposeMessage.this.mFragment.getRecipients().get(0)).getNumber();
                            phoneNumber = bundle.getString("address");
                            msgId = bundle.getLong("msgId");
                            isSamePhoneNumber = AddrMatcher.isNumberMatch(NumberUtils.normalizeNumber(recipient), NumberUtils.normalizeNumber(phoneNumber)) > 0;
                            boolean undeliveredAutoResend = bundle.getBoolean("undelivered_auto_resend", false);
                            MLog.d(RcsComposeMessage.TAG, "EVENT_IM_MSG_FAILED isSamePhoneNumber=" + isSamePhoneNumber + " msgId=" + msgId + ", undliveredAutoResend: " + undeliveredAutoResend);
                            if (undeliveredAutoResend && isSamePhoneNumber) {
                                RcsComposeMessage.this.undeliverdMsgResendList.put(Long.valueOf(msgId), NumberUtils.normalizeNumber(phoneNumber));
                            }
                            if (isSamePhoneNumber) {
                                switchIntent = new Intent();
                                switchIntent.putExtra("send_mode", 0);
                                RcsComposeMessage.this.mIsCanSendImCache = false;
                                MLog.d(RcsComposeMessage.TAG, "EVENT_IM_MSG_FAILED will updateRcsMode");
                                RcseMmsExt.updateRcsMode(switchIntent);
                                RcsComposeMessage.this.sendFailedImBySms(msgId);
                                break;
                            }
                        }
                        break;
                    case 1303:
                        if (RcsComposeMessage.this.mFragment.isRunning() && RcsComposeMessage.this.is1To1ChatExisted()) {
                            recipient = ((Contact) RcsComposeMessage.this.mFragment.getRecipients().get(0)).getNumber();
                            phoneNumber = bundle.getString("address");
                            isSamePhoneNumber = AddrMatcher.isNumberMatch(NumberUtils.normalizeNumber(recipient), NumberUtils.normalizeNumber(phoneNumber)) > 0;
                            boolean isReceiveFt = bundle.getBoolean("incomingfile", false);
                            if (isSamePhoneNumber && RcsProfile.getRcsImSessionStartValue() == 0 && !isReceiveFt) {
                                RcsProfile.acceptRcsImSession(phoneNumber);
                            }
                            if (!RcsProfile.isRcsImSupportSF() || RcsProfile.isRcsFTSupportSF()) {
                                boolean isCanConvertToIm = RcseMmsExt.isCanConvertToIm(RcsComposeMessage.this.mFragment.getWorkingMessage());
                                if (isSamePhoneNumber && isCanConvertToIm) {
                                    RcsComposeMessage.this.mIsCanSendImCache = true;
                                    switchIntent = new Intent();
                                    switchIntent.putExtra("send_mode", 1);
                                    MLog.d(RcsComposeMessage.TAG, "EVENT_IM_MESSAGE_INCOMING will updateRcsMode");
                                    RcseMmsExt.updateRcsMode(switchIntent);
                                }
                            }
                            RcsComposeMessage.this.mFragment.rcsMarkAsRead();
                            break;
                        }
                    case 1504:
                        MLog.d(RcsComposeMessage.TAG, "callback EVENT_CAPABILITY_CHANGED");
                        if (RcsComposeMessage.this.mFragment.isRunning() || RcsComposeMessage.this.mIsFullScreenInput) {
                            ContactList recipients = RcsComposeMessage.this.getRecipientsForOthers();
                            if (recipients != null && 1 == recipients.size()) {
                                String telNumber = NumberUtils.normalizeNumber(((Contact) recipients.get(0)).getNumber());
                                phoneNumber = "";
                                Capabilities cap = null;
                                try {
                                    bundle.setClassLoader(Capabilities.class.getClassLoader());
                                    phoneNumber = bundle.getString("phonenumber");
                                    cap = (Capabilities) bundle.getParcelable("capabilitiesclass");
                                } catch (Exception e) {
                                    MLog.e(RcsComposeMessage.TAG, " getCapability " + e.toString());
                                }
                                if (!phoneNumber.isEmpty() && cap != null) {
                                    RcsComposeMessage.this.issupLs = cap.isLocationSharingSupported();
                                    isSamePhoneNumber = false;
                                    boolean isWarnImDeferred = false;
                                    if (RcsProfile.getRcsService() != null) {
                                        try {
                                            isSamePhoneNumber = RcsProfile.getRcsService().compareUri(telNumber, phoneNumber);
                                            isWarnImDeferred = RcsProfile.getRcsService().warnIMmessageDeferredIfNeed(telNumber);
                                        } catch (RemoteException e2) {
                                            MLog.e(RcsComposeMessage.TAG, "Remote error");
                                        }
                                    }
                                    if (isSamePhoneNumber) {
                                        Intent autoIntent = new Intent();
                                        autoIntent.putExtra("auto_set_send_mode", true);
                                        autoIntent.putExtra("ignore_cap_time_out", true);
                                        MLog.d(RcsComposeMessage.TAG, "EVENT_CAPABILITY_CHANGED_MMS will updateRcsMode");
                                        RcseMmsExt.updateRcsMode(autoIntent);
                                    }
                                    if (!RcsComposeMessage.this.mCallback.isRecipientsVisiable() && isSamePhoneNumber && r15 && RcseMmsExt.isRcsMode()) {
                                        Toast.makeText(RcsComposeMessage.this.mContext, RcsComposeMessage.this.mContext.getString(R.string.rcs_im_session_interruption), 0).show();
                                        break;
                                    }
                                }
                                MLog.d(RcsComposeMessage.TAG, "EVENT_CAPABILITY_CHANGED  phoneNumber or cap is null");
                                return;
                            }
                            return;
                        }
                        break;
                }
                if (msgId > 0) {
                    Message msg_refresh = Message.obtain();
                    msg_refresh.what = 1105;
                    Bundle bundle_refresh = new Bundle();
                    bundle_refresh.putLong("ft.msg_id", msgId);
                    msg_refresh.obj = bundle_refresh;
                    RcsComposeMessage.this.mRcseEventHandler.sendMessage(msg_refresh);
                }
            }
        }
    };
    private TimerTask mRequstCapTimeTask;
    private SendModeInfo mSendModeInfo;
    private SendModeSetListener mSendModeListener;
    private RcsFtShareMsgForwarder mSharedForward;
    private int mXMSMsgCount = 0;
    private RcsLoginStatusChangeBroadCastReceiver rcsLoginStatusChangeBroadCastReceiver;
    final Handler requestCapHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg == null) {
                MLog.w(RcsComposeMessage.TAG, "custMms:requestCapHandler,msg = null ");
                return;
            }
            Bundle bundle = msg.getData();
            if (bundle == null) {
                MLog.w(RcsComposeMessage.TAG, "custMms:requestCapHandler,bundle = null ");
                return;
            }
            String telNumber = bundle.getString("RCS_TEL_NUMBER");
            MLog.w(RcsComposeMessage.TAG, "custMms:handleMessage");
            RcsComposeMessage.this.requesCapTimeCountDown(telNumber);
        }
    };
    private boolean sendFileFromIntentFlag = false;
    private Timer timer;
    private HashMap<Long, String> undeliverdMsgResendList = new HashMap();

    public interface IHwCustComposeMessageCallback {
        void beginMsgListQuery();

        ContactList constructContactsFromInput(boolean z);

        RcsMessageListAdapter getHwCustMsgListAdapter();

        MessageListView getMessageListView();

        MessageListAdapter getMsgListAdapter();

        boolean getRcsLoadDraftFt();

        List<String> getRecipientsNum();

        void hideRecipientEditor();

        boolean isMsgListAdapterValid();

        boolean isRecipientsVisiable();

        void optPanel(boolean z);

        void setMenuExItemEnabled(int i, boolean z);

        void setMenuExItemVisible(int i, boolean z);

        void setRcsSaveDraftWhenFt(boolean z);

        void showVcalendarDlgFromCalendar(Uri uri, ArrayList<Uri> arrayList);

        void updateSendButtonInCust();

        void updateTitle(ContactList contactList);
    }

    private class ComposeEventHdlr extends Handler {
        public ComposeEventHdlr(Looper aLooper) {
            super(aLooper);
        }

        public void handleMessage(Message aMsg) {
            switch (aMsg.what) {
                case 1:
                    RcsComposeMessage.this.handleComposingEvent();
                    return;
                default:
                    return;
            }
        }
    }

    private class IfMsgplusCbImpl extends Stub {
        private int mEventListener = 0;

        IfMsgplusCbImpl(int event) {
            this.mEventListener = event;
            RcsComposeMessage.this.mMsgplusListeners.put(Integer.valueOf(this.mEventListener), this);
        }

        public void handleEvent(int wEvent, Bundle bundle) throws RemoteException {
            if (wEvent == this.mEventListener) {
                Message msg = RcsComposeMessage.this.mRcseEventHandler.obtainMessage(wEvent);
                msg.obj = bundle;
                RcsComposeMessage.this.mRcseEventHandler.sendMessage(msg);
            }
        }
    }

    private static class MyBindServiceListener implements BindServiceListener {
        private MyBindServiceListener() {
        }

        public void onBindServiceListenerSet() {
            MLog.d(RcsComposeMessage.TAG, "onBindServiceListenerSet() will updateRcsMode");
            Intent updateIntent = new Intent();
            updateIntent.putExtra("auto_set_send_mode", true);
            RcseMmsExt.updateRcsMode(updateIntent);
        }
    }

    private class RcsLoginStatusChangeBroadCastReceiver extends BroadcastReceiver {
        private RcsLoginStatusChangeBroadCastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getExtras() == null)) {
                int newStatus = intent.getExtras().getInt("new_status");
                MLog.d(RcsComposeMessage.TAG, "newStatus==" + newStatus);
                if (newStatus == 1) {
                    MLog.d(RcsComposeMessage.TAG, "custMms:start");
                    ContactList recipients = RcsComposeMessage.this.getRecipientsForOthers();
                    if (recipients == null || 1 != recipients.size()) {
                        MLog.d(RcsComposeMessage.TAG, "custMms:recipients  = null or size !=1");
                        return;
                    }
                    String telNumber = NumberUtils.normalizeNumber(((Contact) recipients.get(0)).getNumber());
                    int alreadyTime = RcsTransaction.getAlreadyTime(telNumber);
                    int validityTime = RcsTransaction.getValidityTime();
                    MLog.d(RcsComposeMessage.TAG, "custMms:alreadyTime = " + alreadyTime + ",validityTime=" + validityTime);
                    Intent autoSetIntent;
                    if (alreadyTime <= validityTime) {
                        RcsComposeMessage.this.initTimeTaskForReqCap(validityTime - alreadyTime, telNumber);
                        autoSetIntent = new Intent();
                        autoSetIntent.putExtra("auto_set_send_mode", true);
                        MLog.d(RcsComposeMessage.TAG, "Rcs login status change to login will updateRcsMode()");
                        RcseMmsExt.updateRcsMode(autoSetIntent);
                    } else {
                        MLog.d(RcsComposeMessage.TAG, "custMms:alreadyTime>=validityTime,reqCap now");
                        autoSetIntent = new Intent();
                        autoSetIntent.putExtra("auto_set_send_mode", true);
                        autoSetIntent.putExtra("ignore_cap_time_out", false);
                        MLog.d(RcsComposeMessage.TAG, "Rcs login status change to login will updateRcsMode()");
                        RcseMmsExt.updateRcsMode(autoSetIntent);
                        RcsTransaction.checkValidityTimeAndSendCapRequest(telNumber);
                    }
                    RcsComposeMessage.this.autoResendUndeliveredMsg();
                }
            }
        }
    }

    public static class SendModeInfo {
        protected boolean mSendLock;
        protected int mSendMode;

        public SendModeInfo() {
            init();
        }

        public void init() {
            this.mSendMode = 0;
            this.mSendLock = false;
        }

        public void setSendMode(int sendMode) {
            this.mSendMode = sendMode;
        }

        public void setSendLock(boolean sendLock) {
            this.mSendLock = sendLock;
        }
    }

    private void fixFindBugs() {
        MLog.v(TAG, "fix find bugs warning");
    }

    public RcsComposeMessage(ComposeMessageFragment fragment) {
        this.mContext = fragment.getContext();
        this.mFragment = fragment;
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mSharedForward = new RcsFtShareMsgForwarder();
        }
    }

    public void setHwCustCallback(IHwCustComposeMessageCallback callback) {
        this.mCallback = callback;
    }

    public void switchToEdit(Menu optionMenu, boolean hasMmsItem) {
        if (this.isRcsOn && this.mFragment != null) {
            this.mOptionMenu = optionMenu;
            Cursor cursor = this.mCallback.getMsgListAdapter().getCursor();
            if (this.mCallback.isMsgListAdapterValid() && this.mCallback.getMessageListView() != null) {
                Integer[] positions = this.mCallback.getMessageListView().getRecorder().getRcsSelectRecorder().getAllSelectPositions();
                if (positions != null && positions.length > 0) {
                    boolean hasLocItem = false;
                    boolean hasFtItem = false;
                    boolean hasWithoutFtItem = false;
                    int prePosition = cursor.getPosition();
                    for (Integer intValue : positions) {
                        cursor.moveToPosition(intValue.intValue());
                        if (this.mCallback.isMsgListAdapterValid()) {
                            if (6 == ((long) RcsProfileUtils.getRcsMsgExtType(cursor))) {
                                hasLocItem = true;
                            }
                            long mRcsMsgType = (long) RcsProfileUtils.getRcsMsgType(cursor);
                            if (3 == mRcsMsgType || 4 == mRcsMsgType || 5 == mRcsMsgType || 6 == mRcsMsgType || hasLocItem) {
                                hasFtItem = true;
                            } else {
                                hasWithoutFtItem = true;
                            }
                            if (hasFtItem && r7) {
                                break;
                            }
                        }
                    }
                    cursor.moveToPosition(prePosition);
                    IHwCustComposeMessageCallback iHwCustComposeMessageCallback = this.mCallback;
                    boolean itemVisible = positions.length > 0 ? (hasFtItem || hasMmsItem) ? false : getItemVisible(optionMenu, 278925319) : false;
                    iHwCustComposeMessageCallback.setMenuExItemEnabled(278925319, itemVisible);
                    boolean isSelectTextVisible = false;
                    if (optionMenu != null) {
                        MenuItem selectTextCopy = optionMenu.findItem(278925343);
                        if (selectTextCopy != null) {
                            isSelectTextVisible = selectTextCopy.isVisible();
                        }
                    }
                    IHwCustComposeMessageCallback iHwCustComposeMessageCallback2 = this.mCallback;
                    if (positions.length != 1) {
                        isSelectTextVisible = false;
                    } else if (hasFtItem || hasMmsItem) {
                        isSelectTextVisible = false;
                    }
                    iHwCustComposeMessageCallback2.setMenuExItemEnabled(278925343, isSelectTextVisible);
                    iHwCustComposeMessageCallback = this.mCallback;
                    itemVisible = (1 >= positions.length || !hasFtItem) ? getItemVisible(optionMenu, 278925316) : false;
                    iHwCustComposeMessageCallback.setMenuExItemEnabled(278925316, itemVisible);
                }
            }
        }
    }

    public boolean isFromContacts() {
        return this.mIsFromContacts;
    }

    public boolean isSendFileFlagOn() {
        return this.isRcsOn ? this.sendFileFromIntentFlag : false;
    }

    public boolean isRcsSwitchOn() {
        return this.isRcsOn;
    }

    private void storeRcsSendModeCache(boolean isRcsMode, boolean isSendModeLocked) {
        this.mSendModeInfo.setSendMode(isRcsMode ? 1 : 0);
        this.mSendModeInfo.setSendLock(isSendModeLocked);
    }

    private void storeAndSetRcsButtons(boolean isRcsMode, boolean isSendModeLocked) {
        storeRcsSendModeCache(isRcsMode, isSendModeLocked);
        updateMessageModeChooseButtonIfNeed(isRcsMode, isSendModeLocked);
        this.mCallback.updateSendButtonInCust();
    }

    private void initSendModeListener() {
        this.mSendModeListener = new SendModeSetListener() {
            public void onSendModeSet(boolean isRcsMode, boolean isSendModeLocked) {
                RcsComposeMessage.this.storeAndSetRcsButtons(isRcsMode, isSendModeLocked);
                RcsComposeMessage.this.clearComposingTitleInSmsMode(isRcsMode);
                RcsComposeMessage.this.refreshAttachmentsContent();
            }

            public int autoSetSendMode(boolean ignoreCapTimeOut, boolean ignoreLoginStatus) {
                if (RcsComposeMessage.this.mCallback == null) {
                    MLog.d(RcsComposeMessage.TAG, "autoSetSendMode() return xms,mCallback is null");
                    return 0;
                }
                WorkingMessage workingMessage = RcsComposeMessage.this.mFragment.getWorkingMessage();
                List<String> numberList = RcsComposeMessage.this.getContactsList();
                if (numberList == null || numberList.size() != 1) {
                    MLog.d(RcsComposeMessage.TAG, "autoSetSendMode() return xms,number is null or bigger than 1");
                    return 0;
                }
                boolean canConvertToIm = RcseMmsExt.isCanConvertToIm(workingMessage);
                boolean isRcsServiceEnabledAndUserLogin = RcsProfile.isRcsServiceEnabledAndUserLogin();
                MLog.d(RcsComposeMessage.TAG, "autoSetSendMode(), login status:" + isRcsServiceEnabledAndUserLogin + ",ignore login:" + ignoreLoginStatus + "canConvertToIm:" + canConvertToIm + "number list size:" + numberList.size() + ",ignoreCapTimeOut:" + ignoreCapTimeOut);
                int retVal = 0;
                if ((isRcsServiceEnabledAndUserLogin || ignoreLoginStatus) && canConvertToIm) {
                    RcsComposeMessage.this.mIsCanSendImCache = RcsProfile.isImAvailable((String) numberList.get(0), ignoreCapTimeOut);
                    MLog.d(RcsComposeMessage.TAG, "mIsCanSendImCache = " + RcsComposeMessage.this.mIsCanSendImCache);
                    if (RcsComposeMessage.this.mIsCanSendImCache) {
                        retVal = 1;
                    }
                }
                MLog.d(RcsComposeMessage.TAG, "autoSetSendMode return:" + retVal);
                return retVal;
            }
        };
    }

    public void onCreate(Bundle savedInstanceState) {
        if (this.isRcsOn) {
            initSendModeListener();
            MLog.d(TAG, "onCreate() will updateRcsMode");
            RcseMmsExt.updateRcsMode(this.mFragment.getIntent());
            this.mIsNeedResetSendMode = false;
            setTargetHeapUtilization(0.75f);
            this.mContext.registerReceiver(this.mHomeKeyEventReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
            new Intent().putExtra("send_mode", 0);
            initComposeHandler();
            if (RcsProfile.isRcsSwitchEnabled()) {
                if (RcsCommonConfig.isRCSSwitchOn() && this.rcsLoginStatusChangeBroadCastReceiver == null) {
                    this.rcsLoginStatusChangeBroadCastReceiver = new RcsLoginStatusChangeBroadCastReceiver();
                }
                if (this.rcsLoginStatusChangeBroadCastReceiver != null) {
                    this.mContext.registerReceiver(this.rcsLoginStatusChangeBroadCastReceiver, new IntentFilter("com.huawei.rcs.loginstatus"), "com.huawei.rcs.RCS_BROADCASTER", null);
                }
            }
            this.mSendModeInfo = new SendModeInfo();
            cancelVideoCompressIfRunning();
        }
    }

    public void onStart() {
        if (this.isRcsOn) {
            this.mIsSendComposingStatus = false;
            if (RcsProfile.isRcsImServiceSwitchEnabled()) {
                RcsProfile.registerRcsCallBack(Integer.valueOf(1302), new IfMsgplusCbImpl(1302));
                RcsProfile.registerRcsCallBack(Integer.valueOf(1303), new IfMsgplusCbImpl(1303));
                RcsProfile.registerRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT), new IfMsgplusCbImpl(AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT));
                RcsProfile.registerRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR), new IfMsgplusCbImpl(AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR));
                RcsProfile.registerRcsCallBack(Integer.valueOf(1116), new IfMsgplusCbImpl(1116));
                RcseMmsExt.registerSendModeSetListener(this.mSendModeListener);
            }
        }
    }

    public void onPause4Rcs() {
        if (this.isRcsOn) {
            if (RcsProfile.isRcsImServiceSwitchEnabled()) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mObXmsChange);
            }
            if (RcsProfile.isRcsImServiceSwitchEnabled() && !this.mIsFullScreenInput) {
                RcsProfile.unregisterRcsCallBack(Integer.valueOf(1301), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(1301)));
                MLog.d(TAG, "unRegister EVENT_CAPABILITY_CHANGED_MMS");
                RcsProfile.unregisterRcsCallBack(Integer.valueOf(1504), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(1504)));
            }
        }
    }

    public void onResume(Intent intent) {
        if (this.isRcsOn) {
            if (RcsProfile.getRcsService() == null) {
                RcsProfile.setBindServiceListener(this.mBindServiceListener);
            }
            if (RcsProfile.isRcsImServiceSwitchEnabled() && RcsXmlParser.getBoolean(SEND_SWITCHER, false)) {
                this.mContext.getContentResolver().registerContentObserver(sXmsReceivedUri, true, this.mObXmsChange);
            }
            acceptImSession(0);
        }
    }

    public void onResume() {
        if (this.isRcsOn) {
            RcseMmsExt.refreshUI();
            if (RcsProfile.isRcsImServiceSwitchEnabled() && !this.mIsFullScreenInput) {
                RcsProfile.registerRcsCallBack(Integer.valueOf(1301), new IfMsgplusCbImpl(1301));
                MLog.d(TAG, "onResume register EVENT_CAPABILITY_CHANGED_MMS");
                RcsProfile.registerRcsCallBack(Integer.valueOf(1504), new IfMsgplusCbImpl(1504));
            }
            if (!this.mCallback.isRecipientsVisiable()) {
                this.mIsPickContacts = false;
            }
            if (!this.mIsNeedResetSendMode) {
                this.mIsNeedResetSendMode = true;
            } else if (!this.mIsFullScreenInput) {
                RcseMmsExt.resetRcsMode();
            }
            Intent autoIntent = new Intent();
            autoIntent.putExtra("auto_set_send_mode", true);
            autoIntent.putExtra("ignore_cap_time_out", false);
            autoIntent.putExtra("ignore_login_status", this.mIgnoreLoginStatus);
            MLog.d(TAG, "onResume() will updateRcsMode");
            RcseMmsExt.updateRcsMode(autoIntent);
            this.mIgnoreLoginStatus = false;
            ContactList contactList = getRecipientsForOthers();
            if (this.sendFileFromIntentFlag || !(this.mIsPickContacts || contactList == null || contactList.size() != 1)) {
                getRecipientsAndRequestCapabilities();
            }
            this.mIsPickContacts = false;
            this.mIsExitingActivity = false;
            if (this.mIsFullScreenInput) {
                this.mIsFullScreenInput = false;
            }
        }
    }

    public void onStop() {
        if (this.isRcsOn) {
            RcsProfile.setBindServiceListener(null);
            if (RcsProfile.isRcsImServiceSwitchEnabled()) {
                RcsProfile.unregisterRcsCallBack(Integer.valueOf(1302), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(1302)));
                RcsProfile.unregisterRcsCallBack(Integer.valueOf(1303), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(1303)));
                RcsProfile.unregisterRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT)));
                RcsProfile.unregisterRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR)));
                RcsProfile.unregisterRcsCallBack(Integer.valueOf(1116), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(1116)));
                RcseMmsExt.unRegisterSendModeSetListener(this.mSendModeListener);
                this.mIsCanSendImCache = false;
            }
            this.undeliverdMsgResendList.clear();
            this.mRcseEventHandler.removeCallbacksAndMessages(null);
            this.mIsNeedResetSendMode = false;
        }
    }

    public void onDestroy() {
        if (this.isRcsOn) {
            clearComposeing();
            cancelVideoCompressIfRunning();
            if (this.rcsLoginStatusChangeBroadCastReceiver != null) {
                this.mContext.unregisterReceiver(this.rcsLoginStatusChangeBroadCastReceiver);
            }
            if (this.mHomeKeyEventReceiver != null) {
                this.mContext.unregisterReceiver(this.mHomeKeyEventReceiver);
            }
            if (RcsAsyncIconLoader.isInstanceExist()) {
                RcsAsyncIconLoader loader = RcsAsyncIconLoader.getInstance();
                if (loader != null) {
                    loader.quit();
                }
            }
        }
    }

    public void onNewIntent() {
        if (this.isRcsOn) {
            this.isShowComposing = false;
            MLog.d(TAG, "onNewIntent() will updateRcsMode");
            RcseMmsExt.updateRcsMode(this.mFragment.getIntent());
            this.mIsNeedResetSendMode = false;
            this.mSendModeInfo = new SendModeInfo();
            cancelVideoCompressIfRunning();
        }
    }

    public void initResourceRefs() {
        if (this.isRcsOn) {
            ViewStub rcsStub = (ViewStub) this.mFragment.getView().findViewById(R.id.stub_rcs_mode_choose_button);
            rcsStub.setLayoutResource(R.layout.rcs_mode_choose_button);
            TextView vMsgModeChooseButton = (TextView) rcsStub.inflate();
            if (vMsgModeChooseButton == null) {
                MLog.w(TAG, "rcs mode btn cant findviewby");
            }
            if (vMsgModeChooseButton != null && vMsgModeChooseButton.getVisibility() == 0) {
                vMsgModeChooseButton.setClickable(true);
                vMsgModeChooseButton.setEnabled(true);
                vMsgModeChooseButton.setOnClickListener(this);
            }
        }
    }

    public void onClick(View view) {
        if (R.id.msg_mode_choose_button == view.getId()) {
            Intent switchIntent;
            if (RcseMmsExt.isRcsMode()) {
                if (this.mFragment.getConversationInputManager() != null) {
                    this.mFragment.getConversationInputManager().removeMediaPick();
                }
                switchIntent = new Intent();
                switchIntent.putExtra("send_mode", 0);
                switchIntent.putExtra("force_set_send_mode", true);
                MLog.d(TAG, "onClick() will updateRcsMode");
                RcseMmsExt.updateRcsMode(switchIntent);
                if (!RcseMmsExt.isRcsMode() && this.mFragment.getWorkingMessage().hasExceedsMmsLimit()) {
                    onExceedsMaxMessageLimit();
                }
            } else if (!checkHasSubject()) {
                if (this.mFragment.getConversationInputManager() != null) {
                    this.mFragment.getConversationInputManager().removeMediaPick();
                }
                switchIntent = new Intent();
                switchIntent.putExtra("send_mode", 1);
                switchIntent.putExtra("force_set_send_mode", true);
                MLog.d(TAG, "onClick() will updateRcsMode");
                RcseMmsExt.updateRcsMode(switchIntent);
                removeSubject();
            }
        }
    }

    public boolean isRcdForRcs(Conversation mConversation) {
        boolean z = false;
        if (!this.isRcsOn) {
            return false;
        }
        boolean isRcsUser = false;
        this.mFragment.getWorkingMessage().syncWorkingRecipients();
        String[] numbers = mConversation.getRecipients().getNumbers();
        if (numbers != null && numbers.length > 0 && RcsTransaction.getFTCapabilityByNumber(numbers[0]) && RcsTransaction.isFTOfflineSendAvailable(numbers[0])) {
            isRcsUser = true;
        }
        if (isRcsUser) {
            z = RcseMmsExt.isRcsMode();
        }
        return z;
    }

    public void recordVideo(int requestCode) {
        if (this.isRcsOn) {
            RcsMessageUtils.recordVideo(this.mFragment, requestCode);
        }
    }

    public void addAttachment(int type) {
        if (this.isRcsOn) {
            switch (type) {
                case 23:
                    this.mFragment.startActivityForResult(MessageUtils.getIntentForSelectMediaByType("*/*", false), 120);
                    break;
                case 24:
                    RcsMapLoader mapLoader = RcsMapLoaderFactory.getMapLoader(this.mContext);
                    if (mapLoader != null) {
                        mapLoader.requestMap(this.mContext, 9999999);
                        break;
                    }
                    break;
                default:
                    return;
            }
        }
    }

    public int getReqCodeForRcs(int requestCode, int resultCode, Conversation mConversation) {
        if (!this.isRcsOn) {
            return requestCode;
        }
        this.mIsNeedResetSendMode = false;
        if (117 == requestCode) {
            return requestCode;
        }
        Intent sendModeCache = new Intent();
        if (116 == requestCode && !this.mIsFromContacts) {
            sendModeCache.putExtra("send_mode", this.mIsRcsModeSendVcard);
            this.mIsRcsModeSendVcard = 0;
        } else if (111 == requestCode || 120 == requestCode) {
            sendModeCache.putExtra("send_mode", this.mSendModeInfo.mSendMode);
            this.mIsRcsModeSendVcard = this.mSendModeInfo.mSendMode;
        } else {
            sendModeCache.putExtra("send_mode", this.mSendModeInfo.mSendMode);
        }
        sendModeCache.putExtra("force_set_send_mode", this.mSendModeInfo.mSendLock);
        MLog.d(TAG, "getReqCodeForRcs() will updateRcsMode");
        RcseMmsExt.updateRcsMode(sendModeCache);
        if (requestCode == 109 && resultCode == -1) {
            this.mIsPickContacts = true;
        }
        if (!(102 == requestCode || 100 == requestCode || LocationRequest.PRIORITY_LOW_POWER == requestCode || LocationRequest.PRIORITY_NO_POWER == requestCode)) {
            if (116 == requestCode) {
            }
            return requestCode;
        }
        this.mFragment.getWorkingMessage().syncWorkingRecipients();
        if (RcseMmsExt.isRcsMode()) {
            if (116 != requestCode) {
                requestCode = 120;
            } else if (resultCode == -1) {
                requestCode = 120;
            }
        }
        return requestCode;
    }

    public Intent getVcardDataForRcs(int requestCode, int resultCode, Intent data) {
        boolean canHandleVcardForFT = true;
        if (!this.isRcsOn) {
            return data;
        }
        if (this.mIsFromContacts) {
            canHandleVcardForFT = RcseMmsExt.isRcsMode();
        } else if (this.mIsRcsModeSendVcard != 1) {
            canHandleVcardForFT = false;
        }
        if (canHandleVcardForFT && 116 == requestCode && -1 == resultCode) {
            data = handleVcardForFT();
        }
        return data;
    }

    public boolean takePicForRcs(Conversation mConversation, String filePath) {
        if (!this.isRcsOn) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists() || !RcseMmsExt.isRcsMode()) {
            return false;
        }
        Uri uri_ft = Uri.fromFile(RcsUtility.createNewFileByCopyOldFile(file, this.mContext));
        List<Uri> uriList = new ArrayList();
        this.mFragment.getWorkingMessage().syncWorkingRecipients();
        String[] numbers = mConversation.getRecipients().getNumbers();
        uriList.add(uri_ft);
        this.mCallback.setRcsSaveDraftWhenFt(true);
        rcsFt(uriList, Arrays.asList(numbers));
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data, Conversation mConversation, RichMessageEditor mRichEditor, boolean doAppend) {
        if (!this.isRcsOn) {
            return false;
        }
        MLog.d(TAG + " FileTrans : ", "onActivityResult " + requestCode + " mIsFromContacts=" + this.mIsFromContacts);
        if (data == null && this.mIsFromContacts && isFtReqCode(requestCode) && this.mFragment != null) {
            this.mIsFromContacts = false;
            this.mFragment.finishSelf(false);
        }
        Uri uri;
        List<String> rcsList;
        String[] numbers;
        List<Uri> uriList;
        Intent intent;
        String path;
        switch (requestCode) {
            case 1:
                if (this.mSharedForward != null) {
                    this.mSharedForward.onShareFtResult(data);
                    break;
                }
                break;
            case 120:
                if (data != null) {
                    this.mCallback.setRcsSaveDraftWhenFt(true);
                    uri = data.getData();
                    if (uri == null || !uri.toSafeString().startsWith(Contacts.CONTENT_LOOKUP_URI.toSafeString())) {
                        this.mFragment.getWorkingMessage().syncWorkingRecipients();
                        rcsList = new ArrayList();
                        numbers = mConversation.getRecipients().getNumbers();
                        this.mIsNetWork = RcsNetworkAdapter.isNetworkAvailable(this.mContext);
                        MLog.i(TAG + " FileTrans : ", "mIsNetWork " + this.mIsNetWork);
                        if (!this.mIsNetWork) {
                            this.mIgnoreLoginStatus = true;
                        }
                        for (String number : numbers) {
                            rcsList.add(number);
                        }
                        uriList = new ArrayList();
                        uriList.add(data.getData());
                        rcsFt(uriList, rcsList);
                        break;
                    }
                    MLog.d(TAG + " FileTrans : ", "RequestCode anyfile to pick_contacts uri = " + uri.toSafeString());
                    this.mFragment.handleAddVcard(data);
                    return true;
                }
                break;
            case 123:
                if (resultCode == -1 && data != null) {
                    String groupId;
                    List<PeerInformation> members = RcsGroupChatConversationDetailFragment.processPickIMcontactResult(this.mContext, data);
                    if (members == null) {
                        members = new ArrayList();
                    }
                    PeerInformation peerInformation = new PeerInformation(((Contact) this.mFragment.getRecipients().get(0)).getName(), NumberUtils.normalizeNumber(((Contact) mConversation.getRecipients().get(0)).getNumber()));
                    boolean flag = false;
                    for (PeerInformation peer : members) {
                        if (AddrMatcher.isNumberMatch(peerInformation.getNumber(), peer.getNumber()) > 0) {
                            flag = true;
                            if (!flag) {
                                members.add(peerInformation);
                            }
                            MLog.d(TAG + " FileTrans : ", "CreateGroupChat from single im and members.size() = " + members.size() + "");
                            RcsTransaction.requesetCapabilitybeforeGroupChat(members);
                            if (RcsProfile.getRcsService() != null) {
                                try {
                                    if (!members.isEmpty()) {
                                        groupId = RcsProfile.getRcsService().createGroup(this.mContext.getString(R.string.rcs_add_group_chat), members);
                                        intent = new Intent(this.mContext, RcsGroupChatComposeMessageActivity.class);
                                        intent.putExtra("bundle_group_id", groupId);
                                        this.mFragment.startActivity(intent);
                                        break;
                                    }
                                } catch (RemoteException e) {
                                    MLog.e(TAG, "Remote error");
                                    break;
                                }
                            }
                        }
                    }
                    if (flag) {
                        members.add(peerInformation);
                    }
                    MLog.d(TAG + " FileTrans : ", "CreateGroupChat from single im and members.size() = " + members.size() + "");
                    RcsTransaction.requesetCapabilitybeforeGroupChat(members);
                    if (RcsProfile.getRcsService() != null) {
                        if (members.isEmpty()) {
                            groupId = RcsProfile.getRcsService().createGroup(this.mContext.getString(R.string.rcs_add_group_chat), members);
                            intent = new Intent(this.mContext, RcsGroupChatComposeMessageActivity.class);
                            intent.putExtra("bundle_group_id", groupId);
                            this.mFragment.startActivity(intent);
                        }
                    }
                }
                break;
            case HwCustComposeMessageImpl.REQUEST_CODE_MEDIA_COMPRESS_FORWARD /*150*/:
                RcsTransaction.sendFileForForward(data, this.mContext);
                break;
            case 15001:
                if (resultCode == -1) {
                    this.mCallback.beginMsgListQuery();
                    break;
                }
                break;
            case 150222:
                this.isCompressActivityStart = false;
                if (data != null) {
                    Bundle bdl = data.getExtras();
                    if (bdl != null) {
                        long threadId = bdl.getLong("threadId");
                        List<Uri> uList = bdl.getParcelableArrayList("uriList");
                        if (uList != null) {
                            List<String> aList = bdl.getStringArrayList("addrList");
                            if (data.getBooleanExtra("fullSize", false)) {
                                RcsTransaction.multiSend(this.mContext, Long.valueOf(threadId), uList, aList, 120);
                            } else {
                                RcsTransaction.multiSendWithUriResized(this.mContext, threadId, uList, aList, 120);
                            }
                            this.mIsFromContacts = false;
                            break;
                        }
                    }
                }
                return false;
                break;
            case 150223:
                if (data != null) {
                    Uri videoUri = data.getData();
                    if (videoUri != null) {
                        path = RcsTransaction.getPath(this.mContext, videoUri);
                        if (!TextUtils.isEmpty(path)) {
                            RcsTransaction.showFileSaveResult(this.mContext, path);
                            intent = new Intent(this.mContext, RcsVideoPreviewActivity.class);
                            intent.putExtra("file_path", path);
                            this.mFragment.startActivityForResult(intent, 150224);
                            this.mIsRecordVideo = true;
                            RcsFFMpegMediaScannerNotifier.scan(this.mContext, path);
                            break;
                        }
                        MLog.e(TAG + " FileTrans : ", "path is isEmpty");
                        break;
                    }
                    MLog.e(TAG + " FileTrans : ", "videoUri is null");
                    break;
                }
                break;
            case 150224:
                if (data != null) {
                    this.mFragment.getWorkingMessage().syncWorkingRecipients();
                    path = data.getStringExtra("file_path");
                    if (path != null && !TextUtils.isEmpty(path)) {
                        numbers = mConversation.getRecipients().getNumbers();
                        uri = Uri.fromFile(new File(path));
                        if (numbers != null && numbers.length != 0 && uri != null) {
                            this.mCallback.setRcsSaveDraftWhenFt(true);
                            if (numbers.length != 1) {
                                HashMap<String, List<String>> map = RcsProfileUtils.divideRcsFTGroups(numbers);
                                List<String> rcs_ft = (List) map.get("rcs_ft");
                                List<String> nonRcsList = (List) map.get("non_rcs");
                                if (rcs_ft != null && rcs_ft.size() == numbers.length) {
                                    uriList = new ArrayList();
                                    uriList.add(uri);
                                    rcsFt(uriList, rcs_ft);
                                    break;
                                }
                                if (rcs_ft != null) {
                                    uriList = new ArrayList();
                                    uriList.add(data.getData());
                                    rcsFt(uriList, rcs_ft);
                                }
                                if (nonRcsList != null) {
                                    if (!HAS_DRM_CONFIG || !isForwardLock(uri)) {
                                        mRichEditor.setNewAttachment(uri, 2, doAppend);
                                        break;
                                    }
                                    Toast.makeText(this.mContext, R.string.message_compose_attachments_skipped_drm_Toast, 1).show();
                                    break;
                                }
                            }
                            uriList = new ArrayList();
                            uriList.add(uri);
                            rcsFt(uriList, Arrays.asList(numbers));
                            break;
                        }
                        MLog.e(TAG + " FileTrans : ", "recipients is null or recipients is empty or uri is null");
                        break;
                    }
                    MLog.e(TAG + " FileTrans : ", "path is null");
                    break;
                }
                break;
            case 160125:
            case 160126:
                this.mForwarder.onForwardResult(data);
                break;
            case 160127:
                ArrayList<String> memberList = new ArrayList();
                for (String member : mConversation.getRecipients().getNumbers()) {
                    memberList.add(member);
                }
                if (data != null) {
                    data.putStringArrayListExtra("address", memberList);
                }
                this.mChatForwarder.rcsActivityResult(data);
                break;
            case 9999999:
                if (data != null) {
                    this.mFragment.getWorkingMessage().syncWorkingRecipients();
                    double x = data.getDoubleExtra("x", 0.0d);
                    double y = data.getDoubleExtra("y", 0.0d);
                    String add = data.getStringExtra("address");
                    String city = data.getStringExtra("city");
                    numbers = mConversation.getRecipients().getNumbers();
                    if (numbers.length != 1) {
                        rcsList = new ArrayList();
                        for (String number2 : numbers) {
                            rcsList.add(number2);
                        }
                        sendMassLocation(rcsList, x, y, add, city);
                        break;
                    }
                    sendLocationSingleChat(x, y, add, city, numbers[0]);
                    break;
                }
            default:
                return false;
        }
        return true;
    }

    private void sendLocationSingleChat(double x, double y, String add, String city, String recipient) {
        this.mFragment.onPreMessageSent();
        this.mFragment.setSentMessage(true);
        this.mFragment.getConversation().ensureThreadId();
        RcsTransaction.sendLocationSingleChat(x, y, add, city, recipient);
        this.mFragment.onMessageSent();
    }

    private void sendMassLocation(List<String> members, double x, double y, String add, String city) {
        this.mFragment.onPreMessageSent();
        this.mFragment.setSentMessage(true);
        this.mFragment.getConversation().ensureThreadId();
        RcsTransaction.sendMassLocation(members, x, y, add, city);
        this.mFragment.onMessageSent();
    }

    public boolean isRCSAction(String action) {
        return this.isRcsOn ? "android.intent.action.SENDTO".equals(action) : false;
    }

    private void insertVcalendarText(RichMessageEditor richEditor, Bundle extras) {
        if (richEditor != null && extras != null) {
            if (OsUtil.hasCalendarPermission()) {
                ArrayList<Uri> lEventList = extras.getParcelableArrayList("hw_eventsurl_list");
                if (lEventList != null) {
                    richEditor.insertVcalendarText(lEventList);
                }
            } else {
                MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
                OsUtil.requestPermission(this.mFragment.getActivity(), new String[]{"android.permission.READ_CALENDAR"}, 141);
            }
        }
    }

    public boolean handleSendIntent(Conversation mConversation, Intent intent, RichMessageEditor richEditor, Handler multiHandler) {
        if (!this.isRcsOn) {
            return false;
        }
        if (BugleActivityUtil.checkPermissionIfNeeded(this.mContext, null)) {
            return false;
        }
        this.sendFileFromIntentFlag = true;
        if ("RCS_FT".equals(intent.getStringExtra("Contacts"))) {
            this.mIsFromContacts = true;
            if ("RCS_SEND_FILE".equals(intent.getStringExtra("SEND_FILE"))) {
                this.isCompressActivityStart = true;
                this.mFragment.getActivity().startActivityForResult(MessageUtils.getIntentForSelectMediaByType("*/*", false), 120);
                return true;
            }
            this.mHandleSendIntentRcs = true;
            Bundle extras = intent.getExtras();
            String mimeType = intent.getStringExtra("mimeType");
            if (extras == null) {
                return false;
            }
            final RichMessageEditor richMessageEditor;
            final Handler handler;
            if (extras.containsKey("android.intent.extra.TEXT")) {
                richMessageEditor = richEditor;
                handler = multiHandler;
                this.mFragment.getAsyncDialog().runAsync(new Runnable() {
                    public void run() {
                        RcsComposeMessage.this.fixFindBugs();
                    }
                }, new Runnable() {
                    public void run() {
                        RcsComposeMessage.this.fixFindBugs();
                        richMessageEditor.setAddMultiHandler(handler);
                        handler.sendEmptyMessage(1);
                    }
                }, R.string.adding_attachments_title);
                this.mFragment.getWorkingMessage().setText(extras.getString("android.intent.extra.TEXT"));
                richEditor.setText(extras.getString("android.intent.extra.TEXT"));
                return true;
            }
            String address = intent.getStringExtra("ADDRESS");
            List<Uri> uriList = RcsUtility.getUriFromIntent(intent);
            if (RcsTransaction.getFTCapabilityByNumber(address) && RcsTransaction.isFTOfflineSendAvailable(address)) {
                this.mFragment.setSentMessage(true);
                boolean sendResult = false;
                this.mFragment.setScrollOnSend(true);
                if (mConversation.getHwCust() != null) {
                    sendResult = RcsTransaction.handleFileTransferAction(this.mFragment, mConversation.getHwCust().ensureFtSendThreadId(mConversation, this.mContext), intent);
                }
                if ("text/x-vCalendar".equalsIgnoreCase(mimeType)) {
                    insertVcalendarText(richEditor, extras);
                }
                return sendResult;
            } else if (extras.containsKey("android.intent.extra.STREAM")) {
                this.mFragment.setMultiUris(extras.getParcelableArrayList("android.intent.extra.STREAM"));
                this.mFragment.setMimeType(mimeType);
                this.mFragment.setUriPostion(0);
                if (uriList.size() > 1) {
                    int currentSlideCount;
                    SlideshowModel slideShow = this.mFragment.getWorkingMessage().getSlideshow();
                    if (slideShow != null) {
                        currentSlideCount = slideShow.size();
                    } else {
                        currentSlideCount = 0;
                    }
                    int importCount = uriList.size();
                    if (importCount + currentSlideCount > MmsConfig.getMaxSlides()) {
                        importCount = Math.min(MmsConfig.getMaxSlides() - currentSlideCount, importCount);
                        Toast.makeText(this.mContext, this.mContext.getString(R.string.too_many_attachments_Toast, new Object[]{Integer.valueOf(MmsConfig.getMaxSlides()), Integer.valueOf(importCount)}), 1).show();
                    }
                    richMessageEditor = richEditor;
                    handler = multiHandler;
                    this.mFragment.getAsyncDialog().runAsync(new Runnable() {
                        public void run() {
                            RcsComposeMessage.this.fixFindBugs();
                        }
                    }, new Runnable() {
                        public void run() {
                            RcsComposeMessage.this.fixFindBugs();
                            richMessageEditor.setAddMultiHandler(handler);
                            handler.sendEmptyMessage(1);
                        }
                    }, R.string.adding_attachments_title);
                    return true;
                }
                Uri uri = (Uri) uriList.get(0);
                if (uri != null && "file".equals(uri.getScheme()) && "text/plain".equals(mimeType)) {
                    Toast.makeText(this.mContext, this.mContext.getString(R.string.invalid_file_format_Toast), 0).show();
                    return false;
                } else if (uri != null && (uri.toString().startsWith("content://com.android.contacts/contacts/as_vcard") || uri.toString().startsWith("content://com.android.contacts/contacts/as_multi_vcard") || (uri.toString().lastIndexOf(".") > -1 && ".vcf".equals(uri.toString().substring(uri.toString().lastIndexOf(".")))))) {
                    r2 = uri;
                    this.mFragment.getAsyncDialog().runAsync(new Runnable() {
                        public void run() {
                            RcsComposeMessage.this.fixFindBugs();
                        }
                    }, new Runnable() {
                        public void run() {
                            RcsComposeMessage.this.fixFindBugs();
                            RcsComposeMessage.this.mFragment.showVcardMmsTypeDialog(r2);
                        }
                    }, R.string.vcard_progressdialog_message);
                    return true;
                } else if ("text/x-vCalendar".equalsIgnoreCase(mimeType)) {
                    if (OsUtil.hasCalendarPermission()) {
                        Uri calendarUri = (Uri) extras.getParcelable("android.intent.extra.STREAM");
                        ArrayList<Uri> lEventList = extras.getParcelableArrayList("hw_eventsurl_list");
                        if (lEventList == null && calendarUri != null) {
                            richEditor.setNewAttachment(calendarUri, 7, false);
                        } else if (lEventList != null) {
                            this.mCallback.showVcalendarDlgFromCalendar(calendarUri, lEventList);
                        }
                    } else {
                        MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
                        OsUtil.requestPermission(this.mFragment.getActivity(), new String[]{"android.permission.READ_CALENDAR"}, 141);
                    }
                    return true;
                } else {
                    r2 = uri;
                    this.mFragment.getAsyncDialog().runAsync(new Runnable() {
                        public void run() {
                            RcsComposeMessage.this.fixFindBugs();
                        }
                    }, new Runnable() {
                        public void run() {
                            RcsComposeMessage.this.fixFindBugs();
                            FileInfo fileinfo = RcsTransaction.getFileInfoByData(RcsComposeMessage.this.mContext, r2);
                            if (fileinfo != null) {
                                RcsComposeMessage.this.mFragment.addAttachment(fileinfo.getMimeType(), r2, false);
                            }
                        }
                    }, R.string.adding_attachments_title);
                    return true;
                }
            } else {
                MLog.d(TAG + " FileTrans : ", "handleSendIntent not a file");
                return false;
            }
        }
        this.mIsFromContacts = false;
        return false;
    }

    public void initWidget(long mThreadID, MessageListView mMsgListView) {
        if (this.isRcsOn && this.sendFileFromIntentFlag && mThreadID != 0 && this.mFragment.getConversation().getMessageCount() > 0) {
            if (this.mFragment.getActivity() != null) {
                this.mFragment.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        RcsComposeMessage.this.mCallback.hideRecipientEditor();
                    }
                });
            }
            if (mMsgListView != null) {
                mMsgListView.setFocusable(true);
            }
        }
    }

    public void initMessageList(MessageListView mMsgListView, MessageListAdapter mMsgListAdapter) {
        if (this.isRcsOn) {
            RcsMessageListAdapter rcsMessageListAdapter = mMsgListAdapter.getRcsMessageListAdapter();
            if (rcsMessageListAdapter != null) {
                rcsMessageListAdapter.setRcseEventHandler(this.mRcseEventHandler);
                mMsgListView.setOnScrollListener(new RcseScrollListener(rcsMessageListAdapter));
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setMsgItemVisible(MessageItem msgItem) {
        if (this.isRcsOn && this.mFragment != null && (msgItem instanceof RcsFileTransMessageItem)) {
            if (!((RcsFileTransMessageItem) msgItem).isVCardFile() && !((RcsFileTransMessageItem) msgItem).isLocation()) {
                this.mCallback.setMenuExItemVisible(278925326, true);
                String path = ((RcsFileTransMessageItem) msgItem).mImAttachmentPath;
                boolean isFileExist = RcsTransaction.isFileExist(path);
                RcsFileTransMessageItem fileItem = (RcsFileTransMessageItem) msgItem;
                boolean isFileCanSave = (fileItem.mIsOutgoing || fileItem.mImAttachmentStatus == 1002) ? true : fileItem.mImAttachmentStatus == Place.TYPE_ROUTE;
                this.mCallback.setMenuExItemEnabled(278925326, isFileExist);
                MLog.i(TAG, "setMsgItemVisible path=" + path + " isFileExist=" + isFileExist);
                if (isFileExist && isFileCanSave) {
                    this.mCallback.setMenuExItemEnabled(278925326, getItemVisible(this.mOptionMenu, 278925326));
                } else {
                    this.mCallback.setMenuExItemEnabled(278925326, false);
                    this.mCallback.setMenuExItemEnabled(278925316, false);
                }
            } else if (((RcsFileTransMessageItem) msgItem).isVCardFile() && !RcsTransaction.isFileExist(((RcsFileTransMessageItem) msgItem).mImAttachmentPath)) {
                this.mCallback.setMenuExItemEnabled(278925316, false);
            }
        }
    }

    public void setVcardItemVisible(MessageItem msgItem) {
        if (this.isRcsOn && (msgItem instanceof RcsFileTransMessageItem) && ((RcsFileTransMessageItem) msgItem).isVCardFile()) {
            if (((RcsFileTransMessageItem) msgItem).isFTOutGoingMessage()) {
                this.mCallback.setMenuExItemVisible(278925329, true);
            } else {
                this.mCallback.setMenuExItemVisible(278925328, true);
            }
        }
    }

    public boolean saveVcard(MessageItem msgItem) {
        if (!this.isRcsOn || !(msgItem instanceof RcsFileTransMessageItem) || !((RcsFileTransMessageItem) msgItem).isVCardFile()) {
            return false;
        }
        ((RcsFileTransMessageItem) msgItem).saveVcard();
        return true;
    }

    public boolean viewVcardDetail(MessageItem msgItem) {
        if (!this.isRcsOn || !(msgItem instanceof RcsFileTransMessageItem) || !((RcsFileTransMessageItem) msgItem).isVCardFile()) {
            return false;
        }
        ((RcsFileTransMessageItem) msgItem).showVCardDetailDialog();
        return true;
    }

    public void initCapabilityFlag() {
        if (this.isRcsOn) {
            MmsConfig.getHwCustMmsConfig().setFileTransferCapability(false);
            MmsConfig.getHwCustMmsConfig().setLocationSharingCapability(false);
        }
    }

    public void setCapabilityFlag(boolean isRcpEditorVisible, List<String> numberList, Conversation mConversation) {
        if (this.isRcsOn) {
            List<String> numbers = new ArrayList();
            if (isRcpEditorVisible) {
                numbers = numberList;
            } else if (mConversation != null) {
                numbers = Arrays.asList(mConversation.getRecipients().getNumbers());
            }
            int length = numbers.size();
            if (length > 0) {
                int i;
                for (i = 0; i < length; i++) {
                    if (RcsTransaction.getFTCapabilityByNumber((String) numbers.get(i))) {
                        MmsConfig.getHwCustMmsConfig().setFileTransferCapability(true);
                        break;
                    }
                }
                for (i = 0; i < length; i++) {
                    if (RcsTransaction.getLSCapabilityByNumber((String) numbers.get(i))) {
                        MmsConfig.getHwCustMmsConfig().setLocationSharingCapability(true);
                        break;
                    }
                }
            }
        }
    }

    public boolean isStopShowAddAttachmentForFt() {
        boolean z = false;
        if (!this.isRcsOn) {
            return false;
        }
        List<String> numberList = getContactsList();
        if (RcseMmsExt.isRcsMode() && !RcseMmsExt.isCanSendFt(numberList)) {
            z = true;
        }
        return z;
    }

    public void reSendImBySms(final MessageItem msgItem) {
        if (this.isRcsOn) {
            new Thread(new Runnable() {
                public void run() {
                    RcsComposeMessage.this.mFragment.getConversation();
                    long threadId = Conversation.getOrCreateThreadId(RcsComposeMessage.this.mFragment.getActivity().getApplicationContext(), msgItem.mAddress);
                    try {
                        new SmsMessageSender(RcsComposeMessage.this.mContext, new String[]{msgItem.mAddress}, msgItem.mBody, threadId, msgItem.mSubId).sendMessage(threadId);
                        RcsComposeMessage.this.deleteImMessage(msgItem);
                    } catch (Throwable e) {
                        MLog.e(RcsComposeMessage.TAG, "reSendImBySms Failed to send SMS message, threadId=" + threadId, e);
                    }
                }
            }).start();
        }
    }

    public void reSend(MessageItem msgItem) {
        if (this.isRcsOn) {
            MLog.d(TAG + " FileTrans : ", "RESEND CHAT Message in Menu,msg Type =" + msgItem.mType);
            if (msgItem instanceof RcsFileTransMessageItem) {
                MLog.d(TAG + " FileTrans : ", "RESEND CHAT Message in Menu,and msg is FT");
                RcsFileTransMessageItem fileTransMessageItem = (RcsFileTransMessageItem) msgItem;
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putLong("fileTransId", msgItem.mMsgId);
                bundle.putString("sendAddress", msgItem.mAddress);
                bundle.putString("path", fileTransMessageItem.mImAttachmentPath);
                bundle.putInt("request", RcsUtility.getFileTransType(fileTransMessageItem.mImAttachmentPath));
                bundle.putString("global_trans_id", fileTransMessageItem.mImAttachmentGlobalTransId);
                msg.what = 97;
                msg.obj = bundle;
                this.mRcseEventHandler.sendMessage(msg);
            } else {
                chooseResendMode(msgItem);
            }
        }
    }

    public void setFtCapaByNetwork() {
        if (this.isRcsOn) {
            this.mIsNetWork = RcsNetworkAdapter.isNetworkAvailable(this.mContext);
            if (RcseMmsExt.isNeedShowAddAttachmentForFt(getContactsList()) && !this.mIsNetWork) {
                this.mNoneFtCapability = true;
            }
        }
    }

    public boolean getFtCapabilityReqForInsertFile() {
        if (!this.isRcsOn) {
            return false;
        }
        MLog.d(TAG, "getFtCapabilityReqForInsertFile mNoneFtCapability = " + this.mNoneFtCapability);
        return this.mNoneFtCapability;
    }

    private void setTargetHeapUtilization(float value) {
        try {
            Class VMRuntimeClass = Class.forName("dalvik.system.VMRuntime");
            Method getRuntimeMethod = VMRuntimeClass.getMethod("getRuntime", new Class[0]);
            VMRuntimeClass.getMethod("setTargetHeapUtilization", new Class[]{Float.TYPE}).invoke(getRuntimeMethod.invoke(null, new Object[0]), new Object[]{Float.valueOf(value)});
        } catch (Throwable th) {
            MLog.e(TAG, "setTargetHeapUtilization no such method error");
        }
    }

    private void cancelVideoCompressIfRunning() {
        if (!RcseCompressUtil.isCancelled() && RcseCompressUtil.isCompressRunning()) {
            cancelVideoCompress();
            Toast.makeText(this.mContext, this.mContext.getString(R.string.text_compress_cancelled), 0).show();
        }
    }

    private void cancelVideoCompress() {
        ProgressDialog dialog = RcsTransaction.getProgressDialog();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        RcseCompressUtil.cancelVideoCompress();
    }

    private Intent handleVcardForFT() {
        Uri fileuri;
        Intent intent;
        Throwable th;
        String inputPath = this.mContext.getFileStreamPath("vcard_temp.vcf").getAbsolutePath();
        File outputFile = RcsTransaction.getOutputVcardFile();
        String outputPath = null;
        InputStream inputStream = null;
        FileOutputStream fs = null;
        if (outputFile != null) {
            outputPath = outputFile.getAbsolutePath();
        }
        if (outputPath == null) {
            MLog.e(TAG + " FileTrans : ", "handleVcardForFT error, outputPath is null");
            return null;
        }
        int bytesum = 0;
        try {
            if (new File(inputPath).exists()) {
                InputStream inStream = new FileInputStream(inputPath);
                try {
                    FileOutputStream fs2 = new FileOutputStream(outputPath);
                    try {
                        byte[] buffer = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                        while (true) {
                            int byteread = inStream.read(buffer);
                            if (byteread == -1) {
                                break;
                            }
                            bytesum += byteread;
                            fs2.write(buffer, 0, byteread);
                        }
                        inStream.close();
                        fs2.close();
                        fs = fs2;
                        inputStream = inStream;
                    } catch (IOException e) {
                        fs = fs2;
                        inputStream = inStream;
                        try {
                            MLog.e(TAG + " FileTrans : ", "handleVcardForFT error");
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e2) {
                                    MLog.e(TAG + " FileTrans : ", "handleVcardForFT close FileStream failed");
                                    if (fs != null) {
                                        try {
                                            fs.close();
                                        } catch (IOException e3) {
                                            MLog.e(TAG + " FileTrans : ", "handleVcardForFT close FileStream failed");
                                        }
                                    }
                                } catch (Throwable th2) {
                                    if (fs != null) {
                                        try {
                                            fs.close();
                                        } catch (IOException e4) {
                                            MLog.e(TAG + " FileTrans : ", "handleVcardForFT close FileStream failed");
                                        }
                                    }
                                }
                            }
                            if (fs != null) {
                                try {
                                    fs.close();
                                } catch (IOException e5) {
                                    MLog.e(TAG + " FileTrans : ", "handleVcardForFT close FileStream failed");
                                }
                            }
                            fileuri = Uri.fromFile(outputFile);
                            MLog.d(TAG + " FileTrans : ", "handleVcardForFT fileuri = " + fileuri);
                            intent = new Intent();
                            intent.setData(fileuri);
                            return intent;
                        } catch (Throwable th3) {
                            th = th3;
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e6) {
                                    MLog.e(TAG + " FileTrans : ", "handleVcardForFT close FileStream failed");
                                    if (fs != null) {
                                        try {
                                            fs.close();
                                        } catch (IOException e7) {
                                            MLog.e(TAG + " FileTrans : ", "handleVcardForFT close FileStream failed");
                                        }
                                    }
                                } catch (Throwable th4) {
                                    if (fs != null) {
                                        try {
                                            fs.close();
                                        } catch (IOException e8) {
                                            MLog.e(TAG + " FileTrans : ", "handleVcardForFT close FileStream failed");
                                        }
                                    }
                                }
                            }
                            if (fs != null) {
                                try {
                                    fs.close();
                                } catch (IOException e9) {
                                    MLog.e(TAG + " FileTrans : ", "handleVcardForFT close FileStream failed");
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        fs = fs2;
                        inputStream = inStream;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (fs != null) {
                            fs.close();
                        }
                        throw th;
                    }
                } catch (IOException e10) {
                    inputStream = inStream;
                    MLog.e(TAG + " FileTrans : ", "handleVcardForFT error");
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (fs != null) {
                        fs.close();
                    }
                    fileuri = Uri.fromFile(outputFile);
                    MLog.d(TAG + " FileTrans : ", "handleVcardForFT fileuri = " + fileuri);
                    intent = new Intent();
                    intent.setData(fileuri);
                    return intent;
                } catch (Throwable th6) {
                    th = th6;
                    inputStream = inStream;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (fs != null) {
                        fs.close();
                    }
                    throw th;
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e11) {
                    MLog.e(TAG + " FileTrans : ", "handleVcardForFT close FileStream failed");
                    if (fs != null) {
                        try {
                            fs.close();
                        } catch (IOException e12) {
                            MLog.e(TAG + " FileTrans : ", "handleVcardForFT close FileStream failed");
                        }
                    }
                } catch (Throwable th7) {
                    if (fs != null) {
                        try {
                            fs.close();
                        } catch (IOException e13) {
                            MLog.e(TAG + " FileTrans : ", "handleVcardForFT close FileStream failed");
                        }
                    }
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e14) {
                    MLog.e(TAG + " FileTrans : ", "handleVcardForFT close FileStream failed");
                }
            }
        } catch (IOException e15) {
            MLog.e(TAG + " FileTrans : ", "handleVcardForFT error");
            if (inputStream != null) {
                inputStream.close();
            }
            if (fs != null) {
                fs.close();
            }
            fileuri = Uri.fromFile(outputFile);
            MLog.d(TAG + " FileTrans : ", "handleVcardForFT fileuri = " + fileuri);
            intent = new Intent();
            intent.setData(fileuri);
            return intent;
        }
        fileuri = Uri.fromFile(outputFile);
        MLog.d(TAG + " FileTrans : ", "handleVcardForFT fileuri = " + fileuri);
        intent = new Intent();
        intent.setData(fileuri);
        return intent;
    }

    private void resumeListAdd(Bundle bundle) {
        boolean issend = bundle.getBoolean("resume_issend");
        MLog.d(TAG + " FileTrans : ", "issend: =" + issend);
        long sdkMsgId = bundle.getLong("resume_msgId");
        long msgId = RcsTransaction.getMmsMsgId(sdkMsgId, 1, this.mContext);
        if (sendlist.containsKey(Long.valueOf(msgId)) || receivelist.contains(Long.valueOf(msgId))) {
            MLog.v(TAG + " FileTrans : ", "sendlist or  receivelist contains the same sdkMsgId ");
            return;
        }
        if (issend) {
            String sendpath = bundle.getString("resume_path");
            MLog.v(TAG + " FileTrans : ", " ++++ sdkMsgId" + sdkMsgId + "+sendpath" + sendpath);
            sendlist.put(Long.valueOf(msgId), sendpath);
        } else {
            receivelist.add(Long.valueOf(msgId));
        }
    }

    private void resumeListSend(Bundle bundle) {
        try {
            IfMsgplus aMsgPlus = RcsProfile.getRcsService();
            String recipient = ((Contact) this.mFragment.getRecipients().get(0)).getNumber();
            Conversation mConversation = this.mFragment.getConversation();
            for (Entry entry : sendlist.entrySet()) {
                String path = (String) entry.getValue();
                long msgId = ((Long) entry.getKey()).longValue();
                MLog.i(TAG + " FileTrans : ", "resumeListSend path =  " + path);
                RcsTransaction.deleteChatGroup(msgId, 1, this.mContext);
                FileInfo info = RcsTransaction.getFileInfoByData(this.mContext, path);
                if (info == null) {
                    MLog.w(TAG + " FileTrans : ", "resumeListSend Can't find file represented by this Uri : ");
                    return;
                }
                if (!(aMsgPlus == null || mConversation.getHwCust() == null)) {
                    RcsTransaction.preSendFile(this.mContext, recipient, info, mConversation.getHwCust().ensureFtSendThreadId(mConversation, this.mContext));
                }
                MessagingNotification.cancelNotification(this.mContext, 789);
            }
            for (int i = 0; i < receivelist.size(); i++) {
                RcsTransaction.acceptfile(((Long) receivelist.get(i)).longValue(), 1);
            }
            resumeListClear();
        } catch (RuntimeException e) {
            MLog.e(TAG, "Method multiSend failed.");
        }
    }

    private void resumeListClear() {
        sendlist.clear();
        receivelist.clear();
    }

    private void resumeRemove(long msgId, boolean issend) {
        if (issend) {
            sendlist.remove(Long.valueOf(msgId));
        } else {
            receivelist.remove(Long.valueOf(msgId));
        }
    }

    private void composingEventHandler(String msgPlusNumber, boolean rspEvent) {
        if (msgPlusNumber != null && this.mFragment.getRecipients().size() > 0 && PhoneNumberUtils.compare(msgPlusNumber, ((Contact) this.mFragment.getRecipients().get(0)).getNumber())) {
            if (BlacklistCommonUtils.isNumberBlocked(((Contact) this.mFragment.getRecipients().get(0)).getNumber())) {
                this.isShowComposing = false;
                this.mIsBlackNumber = true;
            } else {
                if (rspEvent && RcseMmsExt.isRcsMode()) {
                    this.isShowComposing = true;
                } else {
                    this.isShowComposing = false;
                }
                this.mIsBlackNumber = false;
            }
            Contact contact = (Contact) this.mFragment.getRecipients().get(0);
            AbstractEmuiActionBar actionbar = this.mFragment.getActionbar();
            if (actionbar != null && !this.mIsBlackNumber) {
                String compstiongString = this.mContext.getResources().getString(R.string.label_contact_is_composing);
                CharSequence format;
                if (contact.isYpContact() || contact.isPrivacyContact()) {
                    if (rspEvent) {
                        format = String.format(compstiongString, new Object[]{""});
                    } else {
                        format = this.mFragment.getNumberForRcs();
                    }
                    actionbar.setSubtitle(format);
                    return;
                }
                if (rspEvent) {
                    format = String.format(compstiongString, new Object[]{""});
                } else {
                    format = "";
                }
                actionbar.setSubtitle(format);
            }
        }
    }

    private void clearComposingTitleInSmsMode(boolean isRcsMode) {
        MLog.i(TAG, "clearComposingTitleInSmsMode isShowComposing:" + this.isShowComposing);
        if (this.isShowComposing && !isRcsMode) {
            ContactList recipients = this.mCallback.isRecipientsVisiable() ? this.mCallback.constructContactsFromInput(false) : this.mFragment.getRecipients();
            this.isShowComposing = false;
            this.mCallback.updateTitle(recipients);
        }
    }

    private boolean is1To1ChatExisted() {
        if (this.mCallback.isRecipientsVisiable() || getContactsList().size() != 1) {
            return false;
        }
        return true;
    }

    private MessageItem getRcsImItemByMsgId(String msgType, long msgId) {
        Cursor cursor = this.mCallback.getMsgListAdapter().getCursor();
        if (this.mCallback.getMsgListAdapter().getRcsMessageListAdapter() != null) {
            return this.mCallback.getMsgListAdapter().getRcsMessageListAdapter().getMessageItemWithMsgId(msgType, msgId, cursor);
        }
        return null;
    }

    private void sendFailedImBySms(final long msgId) {
        this.mFragment.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                final MessageItem msgItem = RcsComposeMessage.this.getRcsImItemByMsgId("chat", msgId);
                if (msgItem != null) {
                    new Builder(RcsComposeMessage.this.mContext).setIcon(17301543).setTitle(R.string.add_attchment_failed_replace_title).setMessage(R.string.send_failed_IM_by_XMS).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            RcsComposeMessage.this.reSendImBySms(msgItem);
                        }
                    }).setNegativeButton(R.string.no, null).show();
                }
            }
        });
    }

    private void deleteImMessage(MessageItem msgItem) {
        try {
            Uri uri = ContentUris.withAppendedId(Uri.parse("content://rcsim/chat"), msgItem.mMsgId);
            MLog.d(TAG, "deleteImMessage uri = " + uri);
            SqliteWrapper.delete(this.mContext, uri, null, null);
        } catch (Exception e) {
            MLog.d(TAG, "deleteImMessage" + e.toString());
        }
    }

    public void ftresumeStopDialog(Bundle bundle) {
        resumeListAdd(bundle);
        if (this.mFtresumeStopDialog == null) {
            resumeDialog(bundle);
        } else if (!this.mFtresumeStopDialog.isShowing()) {
            this.mFtresumeStopDialog.setTitle(this.mContext.getResources().getString(R.string.file_fail));
            this.mFtresumeStopDialog.setMessage(this.mContext.getResources().getString(R.string.CS_server_unavailable_message));
            this.mFtresumeStopDialog.getButton(-1).setText(R.string.CS_retry);
            this.mFtresumeStopDialog.getButton(-3).setText(R.string.rate_limit_surpassed);
            this.mFtresumeStopDialog.show();
            this.mFtresumeStopDialog.setCanceledOnTouchOutside(false);
        }
    }

    private void resumeDialog(final Bundle bundle) {
        this.mFtresumeStopDialog = new Builder(this.mContext).setTitle(R.string.file_fail).setMessage(R.string.CS_server_unavailable_message).setPositiveButton(R.string.CS_retry, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                RcsComposeMessage.this.resumeListSend(bundle);
                RcsComposeMessage.this.mIsFtToMmsDeletExitActivity = true;
            }
        }).setNeutralButton(R.string.rate_limit_surpassed, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                RcsComposeMessage.this.resumeListClear();
            }
        }).show();
        this.mFtresumeStopDialog.setCanceledOnTouchOutside(false);
    }

    private void FTisBig() {
        new Builder(this.mContext).setTitle(R.string.storage_is_not_enough).setMessage(R.string.delete_file).setPositiveButton(R.string.I_know , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                RcsComposeMessage.this.fixFindBugs();
            }
        }).show();
    }

    private void notifyFtStatus(Bundle bundle) {
        long msgId = bundle.getLong("ft.msg_id");
        if (msgId > 0) {
            String ft_status = bundle.getString("rcs.ft.status");
            int status = 0;
            if ("send_ok".equals(ft_status)) {
                status = 1002;
            } else if ("send_failed".equals(ft_status)) {
                status = 1001;
            } else if ("send_draft".equals(ft_status)) {
                status = 1006;
            } else if ("send_progressing".equals(ft_status)) {
                status = 1000;
            } else if (!"send_display_ok".equals(ft_status) && "send_recv_ok".equals(ft_status)) {
                status = 1005;
            }
            RcsUtility.updateItemAnyStatusWithMsgId(msgId, status, this.mCallback.getMessageListView(), this.mFragment);
        }
    }

    private void updateFileTransProgress(Bundle bundle) {
        long msgId = bundle.getLong("ft.msg_id");
        MLog.v(TAG + " FileTrans : ", "updateFileTransProgress msgId : " + msgId);
        long totalSize = bundle.getLong("rcs.ft.progress.totalsize");
        long sendSize = bundle.getLong("rcs.ft.progress.currentsize");
        RcsFileTransMessageListItem view = RcsUtility.getMessageListItemById(msgId, this.mCallback.getMessageListView());
        if (view != null && view.getFileTransMessageItem().mImAttachmentStatus == 1000) {
            RcsFileTransMessageItem msgItem = view.getFileTransMessageItem();
            msgItem.mImAttachmentTransSize = sendSize;
            msgItem.mImAttachmentTotalSize = totalSize;
            msgItem.mImAttachmentStatus = 1000;
            view.refreshViewAnyStatus(msgItem);
        }
    }

    private void rcsFt(List<Uri> uriList, List<String> addrList) {
        checkHasMmsDraftBeforeSendFt();
        this.mFragment.onPreMessageSent();
        this.mFragment.setSentMessage(true);
        FileInfo info = RcsTransaction.getFileInfoByData(this.mContext, uriList.get(0));
        if (info == null) {
            Toast.makeText(this.mContext, this.mContext.getString(R.string.rcs_invalid_file_info), 0).show();
        }
        MLog.d(TAG + " FileTrans : ", " RcsFt begin ");
        if (info == null || info.getMimeType() == null || (!info.getMimeType().startsWith("image") && (!info.getMimeType().startsWith("video") || this.mIsRecordVideo))) {
            this.isCompressActivityStart = false;
            this.mIsFromContacts = false;
            if (info == null || 10 != RcsUtility.getFileTransType(info.getSendFilePath())) {
                RcsTransaction.multiSend(this.mContext, Long.valueOf(this.mFragment.getConversation().ensureThreadId()), uriList, addrList, 120);
                this.mIsRecordVideo = false;
            } else {
                Bundle bundle = new Bundle();
                bundle.putLong("thread_id", this.mFragment.getConversation().ensureThreadId());
                RcsUtility.showUserFtNoNeedVardDialog(uriList, addrList, null, bundle, 1, this.mContext, this.mFragment, null, null);
            }
        } else {
            RcsTransaction.handleImageFileTransfer(this.mContext, this.mFragment.getConversation().ensureThreadId(), uriList, addrList);
        }
        if (RcsTransaction.isFTOfflineSendNotifyNeeded((List) addrList)) {
            Toast.makeText(this.mContext, this.mContext.getString(R.string.rcs_filetrans_transit_notification), 0).show();
        }
        this.mFragment.setScrollOnSend(true);
        this.mFragment.onMessageSent();
    }

    private boolean isForwardLock(Uri uri) {
        boolean isForwardLock = false;
        try {
            Class<?> mClsDrmManagerClient = Class.forName("android.drm.DrmManagerClient");
            Object mDrmManagerClient = mClsDrmManagerClient.getConstructor(new Class[]{Context.class}).newInstance(new Object[]{this.mContext});
            Method mMethodGetDrmObjectType = mClsDrmManagerClient.getMethod("getDrmObjectType", new Class[]{Uri.class, String.class});
            boolean mCanHandle = ((Boolean) mClsDrmManagerClient.getMethod("canHandle", new Class[]{Uri.class, String.class}).invoke(mDrmManagerClient, new Object[]{uri, null})).booleanValue();
            if (HAS_DRM_CONFIG && mCanHandle) {
                MLog.d(TAG + " FileTrans : ", "HAS_DRM_CONFIG and can Handle,Method isForwardLock is invoked!");
                isForwardLock = ((Integer) mMethodGetDrmObjectType.invoke(mDrmManagerClient, new Object[]{uri, null})).intValue() != Integer.valueOf(Class.forName("android.drm.DrmStore$DrmObjectType").getField("DRM_SEPARATE_DELIVERY").getInt(null)).intValue();
            }
        } catch (ClassNotFoundException e) {
            MLog.e(TAG + " FileTrans : ", "exception occured class not found error..");
        } catch (NoSuchMethodException e2) {
            MLog.e(TAG + " FileTrans : ", "exception occured no such method error..");
        } catch (InstantiationException e3) {
            MLog.e(TAG + " FileTrans : ", "exception occured instantiation error..");
        } catch (IllegalAccessException e4) {
            MLog.e(TAG + " FileTrans : ", "exception occured illegal access error..");
        } catch (IllegalArgumentException e5) {
            MLog.e(TAG + " FileTrans : ", "exception occured illegal argument error..");
        } catch (InvocationTargetException e6) {
            MLog.e(TAG + " FileTrans : ", "exception occured invocation target error..");
        } catch (NoSuchFieldException e7) {
            MLog.e(TAG + " FileTrans : ", "exception occured no such field error..");
        }
        MLog.d(TAG + " FileTrans : ", "isForwardLock = " + isForwardLock);
        return isForwardLock;
    }

    public void setConversationId(MessageListAdapter msgListAdapter, long threadId) {
        if (this.isRcsOn && msgListAdapter.getRcsMessageListAdapter() != null) {
            MLog.i(TAG, "setConversationId  threadId = " + threadId);
            msgListAdapter.getRcsMessageListAdapter().setConversationId(threadId);
        }
    }

    public boolean saveFileToPhone(MessageItem msgItem) {
        if (!this.isRcsOn) {
            return false;
        }
        if (msgItem == null) {
            MLog.w(TAG, "msgItem is null!");
            return false;
        } else if (msgItem instanceof RcsFileTransMessageItem) {
            RcsFileTransMessageItem fileTransMsgItem = (RcsFileTransMessageItem) msgItem;
            File attachmentFile = fileTransMsgItem.getAttachmentFile();
            if (attachmentFile == null || !attachmentFile.exists()) {
                Toast.makeText(this.mContext, R.string.text_file_not_exist, 0).show();
            } else if (!(fileTransMsgItem.isVCardFile() || fileTransMsgItem.isLocation())) {
                RcsTransaction.showFileSaveResult(this.mContext, RcsMediaFileUtils.saveMediaFile(this.mContext, attachmentFile));
            }
            return true;
        } else {
            MLog.w(TAG, "msgItem does not match RcsFileTransMessageItem type.");
            return false;
        }
    }

    public Uri getDeleteUri(Uri old, long msgId, String type) {
        if (this.isRcsOn && "chat".equals(type)) {
            return ContentUris.withAppendedId(Uri.parse("content://rcsim/chat"), msgId);
        }
        return old;
    }

    public long getRcsThreadId(long old, MessageItem messageItem) {
        if (this.isRcsOn) {
            return messageItem.mThreadId;
        }
        return old;
    }

    public boolean cantUpdateTitle(PeopleActionBarAdapter adapter) {
        boolean z = false;
        if (!this.isRcsOn) {
            return false;
        }
        if (adapter.getNumber() == null) {
            z = true;
        }
        return z;
    }

    public void onEditTextChange(CharSequence s) {
        if (this.isRcsOn) {
            if (RcsProfile.getRcsService() != null) {
                if (this.mComposeEventHdlr != null && this.mIsSendComposingStatus && RcseMmsExt.isRcsMode()) {
                    this.mComposeEventHdlr.sendMessage(this.mComposeEventHdlr.obtainMessage(1));
                } else {
                    this.mIsSendComposingStatus = true;
                }
            }
            boolean curIsEmpty = TextUtils.isEmpty(s);
            boolean sendOption = this.mPreIsEmpty && !curIsEmpty;
            this.mPreIsEmpty = curIsEmpty;
            if (sendOption) {
                ContactList<Contact> contactList = getRecipientsForOthers();
                if (contactList != null && contactList.size() == 1) {
                    for (Contact contact : contactList) {
                        String noFormatNum = PhoneNumberUtils.normalizeNumber(contact.getNumber());
                        if (!(noFormatNum == null || TextUtils.isEmpty(noFormatNum))) {
                            RcsTransaction.checkValidityTimeAndSendCapRequest(noFormatNum);
                        }
                    }
                }
            }
        }
    }

    private void updateMessageModeChooseButtonIfNeed(boolean isRcsMode, boolean isSendModeLocked) {
        TextView vMsgModeChooseButton = (TextView) this.mFragment.getView().findViewById(R.id.msg_mode_choose_button);
        if (vMsgModeChooseButton == null) {
            MLog.w(TAG, "resource msg_mode_choose_button or duoqu_edite_line not found.");
            return;
        }
        boolean hasLockedByUser = isSendModeLocked && vMsgModeChooseButton.getVisibility() == 0;
        if ((isRcsMode || hasLockedByUser) && RcsProfile.isRcsImServiceSwitchEnabled()) {
            vMsgModeChooseButton.setVisibility(0);
            vMsgModeChooseButton.setOnClickListener(this);
            this.mFragment.setEditorViewSuperLayoutPaddingValues(true);
            startUserGuide();
        } else {
            vMsgModeChooseButton.setVisibility(8);
            this.mFragment.setEditorViewSuperLayoutPaddingValues(false);
        }
        if (isRcsMode) {
            vMsgModeChooseButton.setBackgroundResource(R.drawable.msg_mode_choose_rcs_bg);
            vMsgModeChooseButton.setText(R.string.message_send_mode_choose_rcs);
            vMsgModeChooseButton.setContentDescription(this.mContext.getString(R.string.rcs_switch_mode_im));
        } else {
            vMsgModeChooseButton.setBackgroundResource(R.drawable.msg_mode_choose_sms_bg);
            vMsgModeChooseButton.setText(R.string.message_send_mode_choose_sms);
            vMsgModeChooseButton.setContentDescription(this.mContext.getString(R.string.rcs_switch_mode));
        }
        this.mFragment.setCurrentMessageMode(isRcsMode);
    }

    public boolean updateSendButtonStateSimple(TextView textView, boolean cardEnabled, boolean readyToSend) {
        if (!this.isRcsOn) {
            return cardEnabled;
        }
        boolean loginstate = false;
        try {
            if (RcsProfile.getRcsService() != null) {
                loginstate = RcsProfile.getRcsService().getLoginState();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!loginstate) {
            return cardEnabled;
        }
        if (ResEx.self().getHwCust() != null) {
            ResEx.self().getHwCust().removeStateListDrawable(R.drawable.ic_send_message_rcs);
        }
        textView.setBackground(ResEx.self().getStateListDrawable(this.mFragment.getContext(), R.drawable.ic_send_message_rcs));
        textView.setVisibility(readyToSend ? 0 : 8);
        return RcseMmsExt.isNeedShowSendButton(cardEnabled, readyToSend, this.mFragment.getWorkingMessage(), this.mIsCanSendImCache);
    }

    public boolean sendMessage(String debugRecipients) {
        if (!this.isRcsOn) {
            return false;
        }
        this.mFragment.getWorkingMessage().syncWorkingRecipients();
        MLog.i(TAG, "sendMessage isRcsMode = " + RcseMmsExt.isRcsMode());
        if (!RcseMmsExt.isRcsMode()) {
            return false;
        }
        if (checkHasSubject()) {
            return true;
        }
        acceptImSession(2);
        boolean isSent = RcsTransaction.send(debugRecipients, (String) this.mFragment.getRichEditor().getText(), this.mFragment.getWorkingMessage(), this.mFragment, getMaxInputSize());
        MLog.i(TAG, "sendMessage isSent:" + isSent);
        if (isSent) {
            this.mFragment.setSentMessage(true);
            this.mFragment.setSendingMessage(true);
        }
        if (RcseMmsExt.isNeedStoreNotification(this.mFragment.getWorkingMessage())) {
            Toast.makeText(this.mContext, R.string.IM_store_by_server, 0).show();
        }
        if (this.mFragment.getIntent().getBooleanExtra("forwarded_message", false)) {
            this.mFragment.hideKeyboard();
            this.mFragment.getActivity().finish();
        }
        return true;
    }

    public void onRecipientTextChanged(CharSequence s) {
        if (this.isRcsOn && this.mCallback.isRecipientsVisiable()) {
            RcseMmsExt.resetRcsMode();
            if (RcsProfile.isRcsImServiceSwitchEnabled() && this.mCallback.getRecipientsNum().size() == 1 && lastCharacterIsCommitCharacter(s)) {
                Intent autoIntent = new Intent();
                autoIntent.putExtra("auto_set_send_mode", true);
                autoIntent.putExtra("ignore_cap_time_out", false);
                MLog.d(TAG, "onRecipientTextChanged() will updateRcsMode");
                RcseMmsExt.updateRcsMode(autoIntent);
                RcsTransaction.checkValidityTimeAndSendCapRequest((String) this.mCallback.getRecipientsNum().get(0));
            }
        }
    }

    public void afterRecipientTextChanged() {
        if (this.isRcsOn) {
            this.mCallback.optPanel(false);
        }
    }

    public void requestCapabilitiesForSubActivity(ContactList recipients) {
        if (!this.isRcsOn) {
        }
    }

    public void requestCapabilitiesOnTextChange() {
        if (this.isRcsOn) {
            ContactList contactList = getRecipientsForOthers();
            if (contactList != null && contactList.size() == 1) {
                getRecipientsAndRequestCapabilities();
            }
        }
    }

    public Uri getGroupMessageUri(Conversation conversation) {
        if (!this.isRcsOn || conversation == null || conversation.getHwCust() == null) {
            return null;
        }
        if (conversation.getThreadId() <= 0) {
            MLog.d(TAG, "getGroupMessageUri conversation.getThreadId() <= 0");
            conversation.ensureThreadId();
        }
        return conversation.getHwCust().getGroupMessageUri(conversation, this.mContext);
    }

    private void handleComposingEvent() {
        CharSequence testMsg = this.mFragment.getRichEditor().getText();
        ContactList list = this.mFragment.getRecipients();
        if (list != null && 1 == list.size()) {
            String msgPlusUserId = ((Contact) this.mFragment.getRecipients().get(0)).getNumber();
            if (!TextUtils.isEmpty(testMsg)) {
                try {
                    if (RcsProfile.getRcsService() != null) {
                        MLog.i(TAG, "handleComposingEvent sendComposingState Composing...");
                        RcsProfile.getRcsService().sendComposingState(msgPlusUserId, 1);
                    }
                } catch (Exception e) {
                    MLog.e(TAG, "handleComposingEvent getRcsService");
                }
                acceptImSession(1);
            }
        }
    }

    private void initComposeHandler() {
        if (this.mComposeEventHdlr == null && RcsProfile.getRcsService() != null) {
            HandlerThread thread = new HandlerThread("ComposeHandler");
            thread.start();
            thread.setPriority(10);
            this.mComposeEventHdlr = new ComposeEventHdlr(thread.getLooper());
        }
    }

    private void clearComposeing() {
        if (this.mComposeEventHdlr != null) {
            this.mComposeEventHdlr.removeMessages(1);
            this.mComposeEventHdlr.getLooper().quit();
            this.mComposeEventHdlr = null;
        }
    }

    private void acceptImSession(int iImSessionStartType) {
        boolean bImEnabled = RcsProfile.isRcsImServiceSwitchEnabled();
        boolean isAlreadyBlocked = false;
        String telNumber = "";
        if (!(this.mCallback.isRecipientsVisiable() || this.mFragment.getRecipients() == null || this.mFragment.getRecipients().size() != 1)) {
            telNumber = ((Contact) this.mFragment.getRecipients().get(0)).getNumber();
            isAlreadyBlocked = BlacklistCommonUtils.isNumberBlocked(telNumber);
        }
        if (!bImEnabled || r1) {
            MLog.i(TAG, "acceptImSession send xms only or IM unenabled or in black list");
            return;
        }
        if (iImSessionStartType == RcsProfile.getRcsImSessionStartValue() && !TextUtils.isEmpty(telNumber)) {
            RcsProfile.acceptRcsImSession(telNumber);
        }
    }

    private void initTimeTaskForReqCap(int leftTime, final String telNumber) {
        MLog.d(TAG, "custMms:will reqCap in " + leftTime + " seconds");
        if (telNumber == null || TextUtils.isEmpty(telNumber)) {
            MLog.d(TAG, "custMms:telNumber is empty");
            return;
        }
        if (!this.mNewTaskFlag) {
            this.mNewTaskFlag = true;
            this.mRequstCapTimeTask = new TimerTask() {
                Bundle bundle = new Bundle();
                Message msg = new Message();

                public void run() {
                    this.bundle.putString("RCS_TEL_NUMBER", telNumber);
                    this.msg.setData(this.bundle);
                    RcsComposeMessage.this.requestCapHandler.sendMessage(this.msg);
                    MLog.d(RcsComposeMessage.TAG, "custMms:mRequstCapTimeTask sendMessage");
                }
            };
        }
        if (this.mRequstCapTimeTask != null) {
            this.timer = new Timer();
            this.timer.schedule(this.mRequstCapTimeTask, ((long) leftTime) * 1000);
        }
    }

    private void requesCapTimeCountDown(String telNumber) {
        MLog.d(TAG, "custMms:reqCap now");
        clearRequestCapCountDownTask();
        ContactList recipients = getRecipientsForOthers();
        if (recipients != null && 1 == recipients.size()) {
            String telCurrentNumber = NumberUtils.normalizeNumber(((Contact) recipients.get(0)).getNumber());
            if (telCurrentNumber == null || TextUtils.isEmpty(telCurrentNumber)) {
                MLog.d(TAG, "custMms:telCurrentNumber is empty");
            } else if (telNumber.equals(telCurrentNumber)) {
                MLog.d(TAG, "custMms:now checkValidityTimeAndSendCapRequest");
                RcsTransaction.checkValidityTimeAndSendCapRequest(telNumber);
            } else {
                MLog.d(TAG, "custMms:telCurrentNumber RcsTransaction.checkValidityTimeAndSendCapRequest");
                RcsTransaction.checkValidityTimeAndSendCapRequest(telCurrentNumber);
            }
        }
    }

    public void clearRequestCapCountDownTask() {
        if (this.mRequstCapTimeTask != null) {
            this.mRequstCapTimeTask.cancel();
            this.mRequstCapTimeTask = null;
        }
        this.mNewTaskFlag = false;
        if (this.timer != null) {
            this.timer.purge();
        }
    }

    private ContactList getRecipientsForOthers() {
        return this.mCallback.isRecipientsVisiable() ? this.mCallback.constructContactsFromInput(false) : this.mFragment.getRecipients();
    }

    private void requestCapabilities(ContactList recipients) {
        if (recipients != null && recipients.size() > 0) {
            for (Contact contact : recipients) {
                String noFormatNum = PhoneNumberUtils.normalizeNumber(contact.getNumber());
                if (!(noFormatNum == null || TextUtils.isEmpty(noFormatNum))) {
                    RcsTransaction.checkValidityTimeAndSendCapRequest(noFormatNum);
                }
            }
        }
    }

    private void getRecipientsAndRequestCapabilities() {
        requestCapabilities(this.mCallback.isRecipientsVisiable() ? this.mCallback.constructContactsFromInput(false) : this.mFragment.getRecipients());
    }

    public void updateSendModeToSms() {
        if (this.isRcsOn) {
            Intent intent = new Intent();
            intent.putExtra("send_mode", 0);
            intent.putExtra("force_set_send_mode", true);
            intent.putExtra("ignore_mode_lock", true);
            MLog.d(TAG, "updateSendModeToSms updateRcsMode()");
            RcseMmsExt.updateRcsMode(intent);
        }
    }

    public boolean getItemVisible(Menu optionMenu, int itemID) {
        if (!this.isRcsOn || optionMenu == null) {
            return false;
        }
        MenuItem menuItem = optionMenu.findItem(itemID);
        if (menuItem != null) {
            return menuItem.isEnabled();
        }
        return false;
    }

    public boolean detectMessageToForwardForFt(MessageListView msgListView, Cursor cursor) {
        if (!this.isRcsOn) {
            return false;
        }
        Integer[] selection = msgListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions();
        this.mForwarder.setFragment(this.mFragment);
        this.mForwarder.setMessageListAdapter(this.mCallback.getMsgListAdapter());
        this.mForwarder.setMessageKind(1);
        return this.mForwarder.detectMessageToForwardForFt(selection, cursor);
    }

    public boolean detectMessageToForwardForFtPop(MessageListView msgListView, Cursor cursor, Integer[] selection) {
        if (!this.isRcsOn) {
            return false;
        }
        this.mForwarder.setFragment(this.mFragment);
        this.mForwarder.setMessageListAdapter(this.mCallback.getMsgListAdapter());
        this.mForwarder.setMessageKind(1);
        return this.mForwarder.detectMessageToForwardForFt(selection, cursor);
    }

    public void setPeopleActionBar(PeopleActionBarAdapter adapter) {
        if (this.isRcsOn) {
            this.mActionBarAdapter = adapter;
        }
    }

    private boolean isSelfChat() {
        boolean z = true;
        MLog.i(TAG, "isSelfChat in");
        ContactList recipients = getRecipientsForOthers();
        if (recipients == null || recipients.getNumbers().length != 1) {
            return false;
        }
        String recipient = recipients.getNumbers()[0];
        String loginAddress = null;
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (aMsgPlus != null) {
            try {
                loginAddress = aMsgPlus.getCurrentLoginUserNumber();
            } catch (RemoteException e) {
                MLog.e(TAG, "isSelfChat RemoteException error");
            }
        }
        if (loginAddress == null || recipient == null) {
            return false;
        }
        if (AddrMatcher.isNumberMatch(NumberUtils.normalizeNumber(recipient), NumberUtils.normalizeNumber(loginAddress)) <= 0) {
            z = false;
        }
        return z;
    }

    public void handleRecipientEditor() {
        if (!this.isRcsOn) {
            return;
        }
        if (this.mCallback.isRecipientsVisiable()) {
            if (this.mCallback.getRecipientsNum().size() == 1) {
                RcsTransaction.checkValidityTimeAndSendCapRequest((String) this.mCallback.getRecipientsNum().get(0));
            }
        } else if (!this.mFragment.getIsAttachmentShow()) {
            Conversation conversation = this.mFragment.getConversation();
            if (conversation != null) {
                ContactList contacts = conversation.getRecipients();
                if (contacts.size() <= 1 && !contacts.isEmpty()) {
                    RcsTransaction.checkValidityTimeAndSendCapRequest(((Contact) contacts.get(0)).getNumber());
                }
            }
        }
    }

    private boolean lastCharacterIsCommitCharacter(CharSequence s) {
        boolean z = true;
        int len = s.length() - 2;
        if (len < 0) {
            return false;
        }
        char last = s.charAt(len);
        if (!(last == ',' || last == ';' || last == '\n')) {
            z = false;
        }
        return z;
    }

    public boolean checkNeedAppendSignature() {
        if (this.isRcsOn) {
            return this.mHandleSendIntentRcs;
        }
        return false;
    }

    public boolean isRCSFileTypeInvalid(Context context, int requestCode, Intent data) {
        if (!this.isRcsOn) {
            return true;
        }
        if (data != null || !this.mIsFromContacts || !isFtReqCode(requestCode) || requestCode == 141) {
            return RcsUtility.isRCSFileTypeInvalid(context, requestCode, 1, data);
        }
        MLog.d(TAG, "rcsft from contacts and user canceled,finish Activity");
        if (context != null) {
            this.mIsFromContacts = false;
        } else {
            MLog.e(TAG, "mActivity is null!");
        }
        return true;
    }

    private boolean isFtReqCode(int requestCode) {
        switch (requestCode) {
            case 15001:
                return false;
            default:
                return true;
        }
    }

    private void calXmsIMMsgCount(Conversation mConversation) {
        int i = 0;
        if (this.isRcsOn && mConversation != null && mConversation.getHwCust() != null) {
            long threadId = mConversation.getThreadId();
            int threadType = mConversation.getHwCust().getRcsThreadType();
            if (threadId <= 0) {
                this.imMsgCount = 0;
                this.mXMSMsgCount = 0;
                return;
            }
            int i2;
            Cursor c = null;
            if (threadType == 1) {
                try {
                    c = SqliteWrapper.query(this.mContext, Conversation.sAllThreadsUri, new String[]{"message_count"}, "_id = ?", new String[]{Long.toString(threadId)}, null);
                    if (c != null && c.getCount() > 0 && c.moveToFirst()) {
                        this.mXMSMsgCount = (int) c.getLong(0);
                    }
                    this.imMsgCount = mConversation.getMessageCount() - this.mXMSMsgCount;
                } catch (Exception e) {
                    MLog.e(TAG, "curosr is wrong " + e);
                    if (c != null) {
                        c.close();
                    }
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                }
            } else if (threadType == 2) {
                Uri rcsUri = ContentUris.withAppendedId(RCSConst.RCS_URI_CONVERSATIONS, threadId).buildUpon().appendQueryParameter("threadMod", "Im").appendQueryParameter("newMessage", "true").build();
                c = SqliteWrapper.query(this.mContext, rcsUri, new String[]{"message_count"}, null, null, null);
                if (c != null && c.getCount() > 0 && c.moveToFirst()) {
                    this.imMsgCount = (int) c.getLong(2);
                }
                this.mXMSMsgCount = mConversation.getMessageCount() - this.imMsgCount;
            }
            if (c != null) {
                c.close();
            }
            if (this.imMsgCount >= 0) {
                i2 = this.imMsgCount;
            } else {
                i2 = 0;
            }
            this.imMsgCount = i2;
            if (this.mXMSMsgCount >= 0) {
                i = this.mXMSMsgCount;
            }
            this.mXMSMsgCount = i;
        }
    }

    public int getImMsgCount() {
        if (this.isRcsOn) {
            return this.imMsgCount;
        }
        return 0;
    }

    private void autoResendUndeliveredMsg() {
        Iterator<Entry<Long, String>> iter = this.undeliverdMsgResendList.entrySet().iterator();
        while (iter != null) {
            try {
                if (!iter.hasNext()) {
                    break;
                }
                Entry<Long, String> entry = (Entry) iter.next();
                RcsTransaction.resendExtMessage(((Long) entry.getKey()).longValue(), (String) entry.getValue(), this.mContext);
            } catch (NoSuchElementException e) {
                MLog.e(TAG, "autoResendUndeliveredMsg error");
            }
        }
        this.undeliverdMsgResendList.clear();
    }

    private void scrollWhenMsgFailed(Bundle bundle) {
        if (this.mCallback.getMsgListAdapter() != null && this.mCallback.getMessageListView() != null) {
            RcsUtility.scrollWhenMsgFailed(bundle, this.mCallback.getMessageListView(), this.mCallback.getMsgListAdapter().getCursor());
        }
    }

    public void setScrollOnSend(boolean scrollOnSend) {
        if (this.isRcsOn && this.mFragment != null) {
            this.mFragment.setScrollOnSend(scrollOnSend);
        }
    }

    public void setSentMessage(boolean sentMessage) {
        if (this.isRcsOn && this.mFragment != null) {
            this.mFragment.setSentMessage(sentMessage);
        }
    }

    public void setSendingMessage(boolean sendingMessage) {
        if (this.isRcsOn && this.mFragment != null) {
            this.mFragment.setSendingMessage(sendingMessage);
        }
    }

    private void chooseResendMode(final MessageItem messageItem) {
        new Builder(this.mContext).setTitle(R.string.im_resend).setItems(new String[]{this.mContext.getString(R.string.rcs_resend_by_text_message), this.mContext.getString(R.string.rcs_resend_by_chat_message)}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        RcsComposeMessage.this.reSendImBySms(messageItem);
                        return;
                    case 1:
                        if (RcsProfile.isResendImAvailable(messageItem.mAddress)) {
                            RcsTransaction.resendExtMessage(messageItem.mMsgId, messageItem.mAddress, RcsComposeMessage.this.mContext);
                            return;
                        } else {
                            ResEx.makeToast((int) R.string.rcs_im_resend_error_message, 0);
                            return;
                        }
                    default:
                        return;
                }
            }
        }).create().show();
    }

    public void toForward(long threadId) {
        if (this.isRcsOn) {
            MLog.e(TAG, "toForward. begin");
            this.mForwarder.forwardFt();
        }
    }

    public Long[] getSelectedItems(MessageListView mMsgListView, Long[] selectedItems) {
        if (!this.isRcsOn) {
            return selectedItems;
        }
        Integer[] selectedItemsExt = mMsgListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions();
        Long[] mSelectedItems = new Long[selectedItemsExt.length];
        for (int i = 0; i < selectedItemsExt.length; i++) {
            mSelectedItems[i] = Long.valueOf(selectedItemsExt[i].longValue());
        }
        return mSelectedItems;
    }

    private void chooseFtResendOrWait(Bundle bundle) {
        AlertDialog d = undeliveredResendDialog(bundle);
        d.show();
        d.getButton(-3).setVisibility(8);
    }

    private AlertDialog undeliveredResendDialog(final Bundle bundle) {
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case -2:
                        RcsComposeMessage.markAsFtAndFTSent(bundle.getLong("fileTransId"), RcsComposeMessage.this.mContext, false, bundle);
                        RcsComposeMessage.mFtToMmsUndelivedNotDeletChat = true;
                        RcsComposeMessage.this.reSendFtByMms(bundle);
                        return;
                    case -1:
                        RcsComposeMessage.markAsFtAndFTSent(bundle.getLong("fileTransId"), RcsComposeMessage.this.mContext, false, bundle);
                        return;
                    default:
                        return;
                }
            }
        };
        return new Builder(this.mContext).setTitle(R.string.message_status_undelivered).setMessage(R.string.undelivered_ft_message).setPositiveButton(R.string.undelivered_wait_btn, dialogListener).setNegativeButton(R.string.undelivered_ft_resend_btn, dialogListener).setNeutralButton(R.string.undelivered_wait_btn, null).create();
    }

    private void reSendFtByMms(Bundle bundle) {
        String mPath = bundle.getString("path");
        long msgId = bundle.getLong("fileTransId");
        int mFileTransType = RcsUtility.getFileTransType(mPath);
        Uri uriPath = Uri.fromFile(new File(mPath));
        RichMessageEditor mRichEditor = this.mFragment.getRichEditor();
        RcsRichMessageEditor.setMsgId(msgId, this.mContext);
        mIsSendFtToMms = true;
        switch (mFileTransType) {
            case 7:
                mRichEditor.setNewAttachment(uriPath, 2, false);
                return;
            case 8:
                mRichEditor.setNewAttachment(uriPath, 5, false);
                return;
            case 9:
                mRichEditor.setNewAttachment(uriPath, 3, false);
                return;
            case 10:
                mRichEditor.setNewAttachment(uriPath, 6, false);
                return;
            default:
                Toast.makeText(this.mContext, R.string.cannot_multi_forward_mms, 0).show();
                mIsSendFtToMms = false;
                return;
        }
    }

    public static boolean checkftComToMms() {
        return mIsSendFtToMms;
    }

    public static void setIsSendFtToMms(boolean inputFtMms) {
        mIsSendFtToMms = inputFtMms;
        mFtToMmsUndelivedNotDeletChat = inputFtMms;
    }

    private int getMaxInputSize() {
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        int iMaxSize = 0;
        String strMaxSize = "";
        if (aMsgPlus != null) {
            try {
                strMaxSize = aMsgPlus.getDmConfig(1);
            } catch (RemoteException e) {
                MLog.e(TAG, "updateMaxInputSize getDmConfig error");
            }
        }
        try {
            iMaxSize = Integer.parseInt(strMaxSize);
        } catch (NumberFormatException e2) {
            MLog.e(TAG, "updateMaxInputSize NumberFormatException error");
        }
        if (iMaxSize == 0 || 16 == iMaxSize) {
            return VTMCDataCache.MAXSIZE;
        }
        return iMaxSize;
    }

    private void startUserGuide() {
        if (RcseMmsExt.isRcsMode() && this.mFragment.isRunning() && !this.isCompressActivityStart) {
            RcsUserGuideFragment.startUserGuide(this.mContext, 2);
        }
    }

    public void setCompressActivityStart(boolean isStart) {
        if (this.isRcsOn) {
            this.isCompressActivityStart = isStart;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean rcsRedirectSendIntent(Intent intent) {
        if (!this.isRcsOn || !RcsUtility.isRcsLogin() || BugleActivityUtil.checkPermissionIfNeeded(this.mContext, null)) {
            return false;
        }
        boolean result = false;
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && (TextUtils.equals("android.intent.action.SEND", action) || TextUtils.equals("android.intent.action.SEND_MULTIPLE", action))) {
                if (intent.getStringExtra("address") != null) {
                    return false;
                }
                if (this.mSharedForward != null) {
                    this.mSharedForward.setFragment(this.mFragment);
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        bundle.putString("mimeType", intent.getType());
                    }
                    this.mSharedForward.launchContactsPicker(1, bundle);
                }
                result = true;
            }
        }
        return result;
    }

    public void refreshAttachmentsContent() {
        if (this.isRcsOn && this.mCallback != null && this.mFragment.getConversationInputManager().isShowBeforeChangeMode()) {
            this.mFragment.getConversationInputManager().setShowBeforeChangeMode(false);
            if (isStopShowAddAttachmentForFt()) {
                setCapabilityFlag(this.mCallback.isRecipientsVisiable(), this.mCallback.getRecipientsNum(), this.mFragment.getConversation());
                return;
            }
            setFtCapaByNetwork();
            setCapabilityFlag(this.mCallback.isRecipientsVisiable(), this.mCallback.getRecipientsNum(), this.mFragment.getConversation());
            this.mCallback.optPanel(true);
        }
    }

    private boolean checkHasSubject() {
        boolean result = false;
        if (this.mFragment.getWorkingMessage() != null) {
            result = this.mFragment.getWorkingMessage().hasSubject() && !RcsProfileUtils.isImModeChangeDialogShow(this.mContext);
            if (result) {
                showChangeModeDialog();
            }
        }
        return result;
    }

    private void showChangeModeDialog() {
        View customView = this.mFragment.getActivity().getLayoutInflater().inflate(R.layout.rcs_dialog_alert_not_subject, null);
        ((CheckBox) customView.findViewById(R.id.cb_not_remind)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                RcsProfileUtils.saveImModeChangeDialogShow(RcsComposeMessage.this.mContext, arg1);
            }
        });
        new Builder(this.mContext).setTitle(R.string.mms_remind_title).setMessage(R.string.rcs_mms_change_to_im_note).setView(customView).setCancelable(true).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RcsComposeMessage.this.fixFindBugs();
                if (RcsComposeMessage.this.mFragment.getConversationInputManager() != null) {
                    RcsComposeMessage.this.mFragment.getConversationInputManager().removeMediaPick();
                }
                RcseMmsExt.resetRcsMode();
                Intent switchIntent = new Intent();
                switchIntent.putExtra("send_mode", 1);
                switchIntent.putExtra("force_set_send_mode", true);
                MLog.d(RcsComposeMessage.TAG, "showChangeModeDialog() positiveButton click will updateRcsMode");
                RcsComposeMessage.this.mIsCanSendImCache = true;
                RcseMmsExt.updateRcsMode(switchIntent);
                RcsComposeMessage.this.removeSubject();
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RcsComposeMessage.this.fixFindBugs();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        }).create().show();
    }

    private void removeSubject() {
        RichMessageEditor mRichEditor = this.mFragment.getRichEditor();
        WorkingMessage mWorkingMessage = this.mFragment.getWorkingMessage();
        if (mRichEditor != null && mRichEditor.isSubjectEditorVisible()) {
            mRichEditor.showSubjectEditor(false);
            if (!TextUtils.isEmpty(mWorkingMessage.getSubject())) {
                boolean requiresMms = mWorkingMessage.requiresMms();
                mWorkingMessage.setSubject(null, false);
                if (requiresMms && !mWorkingMessage.requiresMms()) {
                    mWorkingMessage.removeAttachment(false);
                }
            }
            mRichEditor.setEditTextFocus();
        }
    }

    private void chooseResendModeFt(final Bundle bundle) {
        new Builder(this.mContext).setTitle(R.string.resend).setItems(new String[]{this.mContext.getString(R.string.rcs_resend_by_multimedia_message), this.mContext.getString(R.string.rcs_resend_by_chat_message)}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        RcsComposeMessage.mIsSendFtToMms = true;
                        RcsComposeMessage.this.mIsFtToMmsDeletExitActivity = true;
                        RcsComposeMessage.this.reSendFtByMms(bundle);
                        return;
                    case 1:
                        RcsComposeMessage.this.reSendFtByIM(bundle);
                        return;
                    default:
                        return;
                }
            }
        }).create().show();
    }

    private void reSendFtByIM(Bundle bundle) {
        long msgId = bundle.getLong("fileTransId");
        String mPath = bundle.getString("path");
        String mGlobalTransId = bundle.getString("global_trans_id");
        MLog.i(TAG + " FileTrans : ", "reSendFtByIM mImAttachmentPath is " + mPath);
        boolean z = false;
        if (new File(mPath).exists()) {
            try {
                if (RcsProfile.getRcsService() != null) {
                    z = RcsProfile.getRcsService().getLoginState();
                }
            } catch (Exception e) {
                MLog.e(TAG, "reSendFtByIM getLoginState error");
            }
            List<String> numberList = getContactsList();
            if (z || RcseMmsExt.isNeedShowAddAttachmentForFt(numberList)) {
                this.mFragment.setSentMessage(true);
                this.mFragment.setScrollOnSend(true);
                RcsTransaction.resendMessageFile(msgId, 1, this.mContext, mGlobalTransId);
                return;
            }
            Toast.makeText(this.mContext, R.string.groupchat_resend_network_off, 0).show();
            return;
        }
        Toast.makeText(this.mContext, R.string.groupchat_resend_file_exit, 0).show();
    }

    public boolean rcsMsgHasText(boolean orgMsgHasText) {
        if (this.isRcsOn) {
            return true;
        }
        return orgMsgHasText;
    }

    public static Intent createIntent(Context context, long threadId, int threadType) {
        Intent intent = new Intent(context, ComposeMessageActivity.class);
        if (threadId > 0 && RcsConversationUtils.getHwCustUtils() != null) {
            intent.setData(RcsConversationUtils.getHwCustUtils().getRcsUri(threadId, threadType, true));
        }
        return intent;
    }

    public boolean processAllSelectItem(MessageListView msgListView, MessageListAdapter msgListAdapter, boolean hasMmsItem) {
        boolean z = false;
        if (!this.isRcsOn || msgListView.getRecorder().getRcsSelectRecorder() == null) {
            return hasMmsItem;
        }
        hasMmsItem = false;
        boolean hasImItem = false;
        for (Integer intValue : msgListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions()) {
            int position = intValue.intValue();
            if (!hasMmsItem && msgListView.getItemIdAtPosition(position) < 0) {
                hasMmsItem = true;
            }
            if (!hasImItem) {
                MessageItem msgItem;
                if (msgListAdapter.getRcsMessageListAdapter() != null) {
                    msgItem = msgListAdapter.getRcsMessageListAdapter().getMessageItemWithIdAssigned(position, msgListAdapter.getCursor());
                } else {
                    msgItem = null;
                }
                if (msgItem != null && msgItem.isRcsChat()) {
                    hasImItem = true;
                }
            }
        }
        IHwCustComposeMessageCallback iHwCustComposeMessageCallback = this.mCallback;
        if (!hasImItem) {
            z = true;
        }
        iHwCustComposeMessageCallback.setMenuExItemEnabled(278925331, z);
        return hasMmsItem;
    }

    public MessageItem onOptionsOnlyOneItemSelected(MessageListView msgListView, MessageListAdapter msgListAdapter) {
        MessageItem msgItem = null;
        if (!this.isRcsOn || msgListView.getRecorder().getRcsSelectRecorder() == null) {
            return null;
        }
        Integer[] selection = msgListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions();
        long msgId = msgListView.getItemIdAtPosition(selection[0].intValue());
        if (msgId <= 0) {
            msgId = -msgId;
        }
        int position = selection[0].intValue();
        if (msgListAdapter.getRcsMessageListAdapter() != null) {
            msgItem = msgListAdapter.getRcsMessageListAdapter().getMessageItemWithIdAssigned(position, msgListAdapter.getCursor());
        }
        if (msgItem == null) {
            MLog.e(TAG, "onOptionsOnlyOneItemSelected Cannot load message item for position = " + position + ", msgId = " + msgId);
        }
        return msgItem;
    }

    public boolean isSelectOnlyOneItem(MessageListView msgListView) {
        if (!this.isRcsOn || msgListView.getRecorder().getRcsSelectRecorder() == null) {
            return false;
        }
        boolean isSelectOnlyOneItem = false;
        Integer[] selection = msgListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions();
        if (selection != null && 1 == selection.length) {
            isSelectOnlyOneItem = true;
        }
        return isSelectOnlyOneItem;
    }

    public void sendImBySms(final MessageItem msgItem) {
        if (this.isRcsOn) {
            new Thread(new Runnable() {
                public void run() {
                    RcsComposeMessage.this.mFragment.getConversation();
                    long threadId = Conversation.getOrCreateThreadId(RcsComposeMessage.this.mContext, msgItem.mAddress);
                    try {
                        new SmsMessageSender(RcsComposeMessage.this.mContext, new String[]{msgItem.mAddress}, msgItem.mBody, threadId, msgItem.mSubId).sendMessage(threadId);
                        RcsComposeMessage.this.deleteImMessage(msgItem);
                    } catch (Throwable e) {
                        MLog.e(RcsComposeMessage.TAG, "sendImBySms Failed to send SMS message, threadId=" + threadId, e);
                    }
                }
            }).start();
        }
    }

    public static void markAsSent(final MessageItem msgItem, final Context mContext, final boolean mMarkFromUndeliver) {
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                ContentValues contentValues = new ContentValues(3);
                contentValues.put(NumberInfo.TYPE_KEY, Integer.valueOf(2));
                contentValues.put("status", Integer.valueOf(16));
                if (!mMarkFromUndeliver) {
                    contentValues.put("network_type", Integer.valueOf(9966));
                }
                SqliteWrapper.update(mContext, mContext.getContentResolver(), ContentUris.withAppendedId(Uri.parse("content://rcsim/chat"), msgItem.mMsgId), contentValues, null, null);
            }
        });
    }

    public static void markAsFtAndFTSent(long msgId, Context mContext, boolean mMarkFromUndeliver, Bundle bundle) {
        final Bundle bundle2 = bundle;
        final boolean z = mMarkFromUndeliver;
        final long j = msgId;
        final Context context = mContext;
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                ContentValues contentValues = new ContentValues(3);
                contentValues.put(NumberInfo.TYPE_KEY, Integer.valueOf(2));
                contentValues.put("status", Integer.valueOf(16));
                int fileType = RcsUtility.getFileType(bundle2.getString("path"), 1, true);
                MLog.i(RcsComposeMessage.TAG, "markAsFtAndFTSent fileType =" + fileType);
                contentValues.put("file_type", Integer.valueOf(fileType));
                if (!z) {
                    contentValues.put("network_type", Integer.valueOf(9966));
                }
                SqliteWrapper.update(context, context.getContentResolver(), ContentUris.withAppendedId(Uri.parse("content://rcsim/chat"), j), contentValues, null, null);
                ContentValues contentValuesFt = new ContentValues(3);
                ContentResolver cr = context.getContentResolver();
                contentValuesFt.put("msg_id", Long.valueOf(j));
                contentValuesFt.put("transfer_status", Integer.valueOf(1002));
                SqliteWrapper.update(context, cr, Uri.parse("content://rcsim/file_trans"), contentValuesFt, "msg_id=" + j + " AND " + "chat_type" + "=" + 1, null);
            }
        });
    }

    public void clearEmptyRcsThread(boolean isSendDiscreetMode, Conversation conv) {
        if (this.isRcsOn && !isSendDiscreetMode && conv != null) {
            if (this.mSharedForward == null || !this.mSharedForward.isFtShareProcess()) {
                calXmsIMMsgCount(conv);
                if (this.imMsgCount == 0) {
                    HwBackgroundLoader.getInst().postTask(new Runnable() {
                        public void run() {
                            SqliteWrapper.delete(RcsComposeMessage.this.mContext, RcsComposeMessage.this.mContext.getContentResolver(), RcsComposeMessage.DELETE_EMPTY_RCS_THREAD, null, null);
                        }
                    });
                }
            }
        }
    }

    public boolean isExitingActivity() {
        if (this.isRcsOn) {
            return this.mIsExitingActivity;
        }
        return false;
    }

    public boolean detectMessageToForwardForLoc(MessageListView msgListView, Cursor cursor) {
        if (!this.isRcsOn) {
            return false;
        }
        this.mForwarder.setFragment(this.mFragment);
        this.mForwarder.setMessageListAdapter(this.mCallback.getMsgListAdapter());
        this.mForwarder.setMessageKind(1);
        return this.mForwarder.detectMessageToForwardForLoc(msgListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions(), cursor);
    }

    public boolean detectMessageToForwardForLocPop(MessageListView msgListView, Cursor cursor, Integer[] selection) {
        if (!this.isRcsOn) {
            return false;
        }
        this.mForwarder.setFragment(this.mFragment);
        this.mForwarder.setMessageListAdapter(this.mCallback.getMsgListAdapter());
        this.mForwarder.setMessageKind(1);
        return this.mForwarder.detectMessageToForwardForLoc(selection, cursor);
    }

    public void forwardLoc(long threadId) {
        if (this.isRcsOn) {
            this.mForwarder.forwardLoc();
        }
    }

    public void prepareFwdMsg(String msgBody) {
        if (this.isRcsOn) {
            this.mChatForwarder.setFragment(this.mFragment);
            this.mChatForwarder.launchContactsPicker(160127, msgBody);
        }
    }

    public static boolean getUndelivedFtToMms() {
        return mFtToMmsUndelivedNotDeletChat;
    }

    public MessageListAdapter getComposeMessageListAdapter() {
        if (this.isRcsOn) {
            return this.mCallback.getMsgListAdapter();
        }
        return null;
    }

    public boolean isRcsShouldLoadDraft() {
        if (!this.isRcsOn) {
            return false;
        }
        return !isRcsFileShare() ? this.mCallback.getRcsLoadDraftFt() : true;
    }

    private boolean isRcsFileShare() {
        boolean z = false;
        if (!this.isRcsOn) {
            return false;
        }
        if (this.sendFileFromIntentFlag) {
            z = RcseMmsExt.isRcsMode();
        }
        return z;
    }

    public void setFullScreenFlag(boolean isFullScreen) {
        if (this.isRcsOn) {
            this.mIsFullScreenInput = isFullScreen;
        }
    }

    public void configFullScreenIntent(Intent intent) {
        if (this.isRcsOn) {
            List<String> recipients = getContactsList();
            if (recipients != null && recipients.size() == 1) {
                String noFormatNum = PhoneNumberUtils.normalizeNumber((String) recipients.get(0));
                if (!TextUtils.isEmpty(noFormatNum)) {
                    intent.putExtra("phonenumber", noFormatNum);
                }
            }
        }
    }

    public boolean isGroupChat(Intent intent) {
        if (!this.isRcsOn || intent == null) {
            return false;
        }
        return intent.getBooleanExtra("is_groupchat", false);
    }

    public boolean isFileItem(MessageItem msgItem) {
        if (!this.isRcsOn || msgItem == null) {
            return false;
        }
        return msgItem instanceof RcsFileTransMessageItem;
    }

    public void updateFileDB(MessageItem msgItem, ConversationQueryHandler mBackgroundQueryHandler) {
        if (this.isRcsOn && isFileItem(msgItem)) {
            Uri uri = RcsAttachments.CONTENT_URI;
            ContentValues values = new ContentValues();
            values.put("transfer_status", Integer.valueOf(1010));
            long msgId = 0;
            if (msgItem instanceof RcsFileTransMessageItem) {
                msgId = ((RcsFileTransMessageItem) msgItem).mFileTransId;
            }
            mBackgroundQueryHandler.startUpdate(0, null, uri, values, "_id = " + msgId + " AND chat_type = " + 1, null);
            Uri chatUri = Uri.parse("content://rcsim/chat");
            ContentValues chatValues = new ContentValues();
            chatValues.put("status", Integer.valueOf(64));
            chatValues.put(NumberInfo.TYPE_KEY, Integer.valueOf(5));
            mBackgroundQueryHandler.startUpdate(0, null, chatUri, chatValues, "_id = " + msgItem.mMsgId, null);
        }
    }

    public boolean isRcsImMode() {
        return isRcsSwitchOn() ? RcseMmsExt.isRcsMode() : false;
    }

    private void checkHasMmsDraftBeforeSendFt() {
        if (this.mFragment.getWorkingMessage().hasMmsDraft()) {
            this.mHasDraftBeforeSendFt = true;
        } else {
            this.mHasDraftBeforeSendFt = false;
        }
    }

    public boolean hasMmsDraftBeforeSendFt() {
        return isRcsSwitchOn() ? this.mHasDraftBeforeSendFt : false;
    }

    public void resetHasDraftBeforeSendFt() {
        if (this.isRcsOn) {
            this.mHasDraftBeforeSendFt = false;
        }
    }

    private List<String> getContactsList() {
        if (this.mCallback.isRecipientsVisiable()) {
            return this.mCallback.getRecipientsNum();
        }
        Conversation conversation = this.mFragment.getConversation();
        if (conversation != null && conversation.getRecipients() != null) {
            return Arrays.asList(conversation.getRecipients().getNumbers());
        }
        MLog.w(TAG, "getContactsList(), conversation or recipients is null");
        return new ArrayList(0);
    }

    public boolean updateRcsMenu() {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return false;
        }
        String number = this.mActionBarAdapter.getNumber();
        if (number == null) {
            MLog.e(TAG, "updateRcsMenu number is null");
            return false;
        }
        boolean isNeedToShowCreateGroupchat = isNeedToShowCreateGroupchat(number);
        MLog.i(TAG, "updateRcsMenu isNeedToShowCreateGroupchat: " + isNeedToShowCreateGroupchat);
        return isNeedToShowCreateGroupchat;
    }

    private boolean isNeedToShowCreateGroupchat(String peerNumber) {
        boolean z = false;
        boolean isRcsUser = false;
        if (RcsProfile.getRcsService() != null) {
            try {
                z = RcsProfile.getRcsService().getLoginState();
                isRcsUser = RcsProfile.getRcsService().isRcsUeser(peerNumber);
                MLog.d(TAG, "isNeedToShowCreateGroupchat login status " + z + " rcs user " + isRcsUser);
            } catch (RemoteException e) {
                MLog.e(TAG, "getRcsService error " + e.toString());
            }
        }
        return isRcsUser ? z : false;
    }

    private void onExceedsMaxMessageLimit() {
        Toast.makeText(this.mContext, String.format(this.mContext.getString(R.string.send_file_exceed_max_size), new Object[]{Formatter.formatFileSize(this.mContext, (long) MmsConfig.getMaxMessageSize())}), 1).show();
    }
}
