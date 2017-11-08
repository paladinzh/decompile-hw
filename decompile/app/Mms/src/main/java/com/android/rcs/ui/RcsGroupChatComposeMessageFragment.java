package com.android.rcs.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback2;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.amap.api.services.core.AMapException;
import com.android.messaging.util.OsUtil;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.TempFileProvider;
import com.android.mms.attachment.datamodel.data.AttachmentSelectData;
import com.android.mms.attachment.datamodel.data.AttachmentSelectLocation;
import com.android.mms.attachment.datamodel.media.RichMessageManager;
import com.android.mms.attachment.ui.AttachmentPreview;
import com.android.mms.attachment.ui.MultiAttachmentLayout.OnAttachmentClickListener;
import com.android.mms.attachment.ui.conversation.ConversationInputManager;
import com.android.mms.attachment.ui.conversation.ConversationInputManager.ConversationInputHost;
import com.android.mms.attachment.ui.mediapicker.CameraMediaChooser;
import com.android.mms.attachment.ui.mediapicker.GalleryMediaChooser;
import com.android.mms.attachment.ui.mediapicker.MapMediaChooser;
import com.android.mms.attachment.ui.mediapicker.MediaChooser;
import com.android.mms.attachment.ui.mediapicker.MediaPicker;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation.ConversationQueryHandler;
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.AudioModel;
import com.android.mms.model.ImageModel;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.VcardModel;
import com.android.mms.model.VideoModel;
import com.android.mms.model.control.MediaModelControl;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.AsyncDialog;
import com.android.mms.ui.AttachmentSmileyPagerAdatper;
import com.android.mms.ui.AttachmentTypeSelectorAdapter;
import com.android.mms.ui.AttachmentTypeSelectorAdapter.AttachmentListItem;
import com.android.mms.ui.CommonPhrase;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ContactItemPickActivity;
import com.android.mms.ui.ControllerImpl;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.EditableSlides$RichMessageListener;
import com.android.mms.ui.FragmentTag;
import com.android.mms.ui.HwCustComposeMessageImpl;
import com.android.mms.ui.MediaListItem;
import com.android.mms.ui.MessageFullScreenActivity;
import com.android.mms.ui.MessageFullScreenFragment;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.android.mms.ui.SignView;
import com.android.mms.ui.twopane.LeftPaneConversationListFragment;
import com.android.mms.util.DraftCache;
import com.android.mms.util.ShareUtils;
import com.android.mms.util.SignatureUtil;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.transaction.RcsMessagingNotification;
import com.android.rcs.ui.RcsGroupChatMessageListAdapter.GroupMessageColumn;
import com.android.rcs.ui.RcsGroupChatMessageListAdapter.OnDataSetChangedListener;
import com.android.rcs.ui.RcsGroupChatMessageListView.OnGroupEditModeListener;
import com.android.rcs.ui.RcsGroupChatMessageListView.OnSizeChangedListener;
import com.android.rcs.ui.RcsGroupChatRichMessageEditor.RcsGroupRichAttachmentListener;
import com.google.android.gms.Manifest.permission;
import com.google.android.gms.R;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.EditTextWithSmiley;
import com.huawei.mms.ui.EmuiListViewListener;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.MmsEmuiActionBar;
import com.huawei.mms.ui.MultiModeListView.EditHandler;
import com.huawei.mms.ui.PeopleActionBar.AddWhatsAppPeopleActionBarAdapter;
import com.huawei.mms.ui.SplitActionBarView;
import com.huawei.mms.ui.SplitActionBarView.OnCustomMenuListener;
import com.huawei.mms.util.DefaultSmsAppChangedReceiver;
import com.huawei.mms.util.DefaultSmsAppChangedReceiver.HwDefSmsAppChangedListener;
import com.huawei.mms.util.DelaySendManager;
import com.huawei.mms.util.FavoritesUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwCustFavoritesUtils;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MmsCommon;
import com.huawei.mms.util.MmsScaleSupport.SacleListener;
import com.huawei.mms.util.NumberUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.SelectionChangedListener;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.commonInterface.IfMsgplusCb;
import com.huawei.rcs.commonInterface.IfMsgplusCb.Stub;
import com.huawei.rcs.media.RcsMediaFileUtils;
import com.huawei.rcs.telephony.RcseTelephonyExt.RcsAttachments;
import com.huawei.rcs.ui.RcsAsyncIconLoader;
import com.huawei.rcs.ui.RcsAudioMessage;
import com.huawei.rcs.ui.RcsFileTransDataHander;
import com.huawei.rcs.ui.RcsFileTransGroupMessageItem;
import com.huawei.rcs.ui.RcsFileTransGroupMessageListItem;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.ui.RcsGroupChatConversationDetailActivity;
import com.huawei.rcs.ui.RcsGroupChatDeliveryReportActivity;
import com.huawei.rcs.ui.RcsVideoPreviewActivity;
import com.huawei.rcs.ui.RcseScrollListener;
import com.huawei.rcs.utils.RcsFFMpegMediaScannerNotifier;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsProfileUtils;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcsTransaction.LocationData;
import com.huawei.rcs.utils.RcsUtility;
import com.huawei.rcs.utils.RcsUtility.FileInfo;
import com.huawei.rcs.utils.RcseCompressUtil;
import com.huawei.rcs.utils.RcseMmsExt;
import com.huawei.rcs.utils.map.abs.RcsMapFragment;
import com.huawei.rcs.utils.map.abs.RcsMapLoader;
import com.huawei.rcs.utils.map.abs.RcsMapLoaderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RcsGroupChatComposeMessageFragment extends HwBaseFragment implements OnClickListener, OnEditorActionListener, RcsGroupRichAttachmentListener {
    private static final int[] BUTTON_DELETE = new int[]{R.string.deleting_messages_Toast, R.string.tilte_copy_favorites_message, R.string.forwarding_multi_messages};
    private static final String[] SMS_BODY_PROJECTION = new String[]{"body"};
    private static final String[] SMS_MSGID_PROJECTION = new String[]{"_id"};
    private static boolean mIsGroupchatClosed = false;
    private static boolean mIsInvalidGroupStatus = false;
    private static ArrayList receivelist = new ArrayList();
    public static final Uri sConversationUri = Uri.parse("content://rcsim/rcs_conversations");
    public static final Uri sDeleteAllUri = Uri.parse("content://rcsim/rcs_group_message");
    public static final Uri sDeleteDraftUri = Uri.parse("content://rcsim/delete_draft_msg_by_threadid");
    public static final Uri sDeleteUri = Uri.parse("content://rcsim/delete_all_message_by_threadid");
    public static final Uri sGroupUri = Uri.parse("content://rcsim/rcs_groups");
    public static final Uri sMemberUri = Uri.parse("content://rcsim/rcs_group_members");
    public static final Uri sMessageUri = Uri.parse("content://rcsim/rcs_group_message");
    private static HashMap<Long, String> sendlist = new HashMap();
    private AttachmentSelectLocation attSelectLocation;
    private AlertDialog ftresumeStopDialog = null;
    private GroupChatCreateBroadCastReceiver groupChatCreateReceiver;
    private GroupChatMemberChangeBroadCastReceiver groupChatMemberChangeBroadCastReceiver;
    private GroupChatStatusChangeBroadCastReceiver groupChatStatusChangeBroadCastReceiver;
    private boolean isFirstCheck = true;
    private boolean isShowComposing = false;
    private LocationData locData;
    protected AbstractEmuiActionBar mActionBarWhenSplit;
    private boolean mActionBarWillExpand = false;
    public ActionMode mActionMode = null;
    private AddWhatsAppPeopleActionBarAdapter mActionbarAdapter = new AddWhatsAppPeopleActionBarAdapter() {
        public String getNumber() {
            if (RcsGroupChatComposeMessageFragment.this.isShowComposing) {
                return "";
            }
            return "(" + RcsGroupChatComposeMessageFragment.this.mGroupNumber + ")";
        }

        public String getName() {
            if (RcsGroupChatComposeMessageFragment.this.isShowComposing) {
                return RcsGroupChatComposeMessageFragment.this.mComposeTypingString;
            }
            return TextUtils.isEmpty(RcsGroupChatComposeMessageFragment.this.mGroupName) ? RcsGroupChatComposeMessageFragment.this.getResources().getString(R.string.chat_topic_default) : RcsGroupChatComposeMessageFragment.this.mGroupName;
        }

        public boolean isGroup() {
            return false;
        }

        public boolean isExistsInContact() {
            return false;
        }

        public boolean hasEmail() {
            return false;
        }

        public void writeEmail() {
        }

        public void callRecipients() {
        }

        public void editBeforeCall() {
        }

        public void addToContact() {
        }

        public void viewPeopleInfo() {
            if (!RcsGroupChatComposeMessageFragment.mIsInvalidGroupStatus) {
                RcsGroupChatComposeMessageFragment.this.gotoGroupChatDetailActivity();
            }
        }

        public void addToBlacklist(boolean add, Runnable onComplete) {
        }

        public ContactList getContactList() {
            return null;
        }

        public boolean isHwMsgSender() {
            return false;
        }

        public void writeWeichat() {
        }

        public boolean hasWeichat() {
            return false;
        }

        public boolean hasWhatsapp() {
            return false;
        }

        public void writeWhatsapp() {
        }
    };
    private AsyncDialog mAsyncDialog;
    private AttachmentPreview mAttachmentPreview;
    private ViewStub mAttachmentStub;
    private AttachmentSmileyPagerAdatper mAttachmentTypeSelectorAdapter;
    private BackgroundQueryHandler mBackgroundQueryHandler;
    private int mBottomMaxHeightLandSpace;
    private int mBottomMaxHeightPortrait;
    private int mBottomMaxHeightPortraitNoKeyboard;
    private View mBottomPanel;
    private ScrollView mBottomScroller;
    private boolean mBottomScrollerCanScroll = false;
    private ViewGroup mBottomView;
    private RcsChatMessageForwarder mChatForwarder;
    private ComposeEventHdlr mComposeEventHdlr;
    private LinearLayout mComposeLayoutGroup;
    private String mComposeTypingString = "";
    private ContentResolver mContentResolver;
    private Cursor mCursor = null;
    private final OnDataSetChangedListener mDataSetChangedListener = new OnDataSetChangedListener() {
        public void onDataSetChanged() {
            RcsGroupChatComposeMessageFragment.this.mPossiblePendingNotification = true;
        }

        public void onContentChanged() {
            RcsGroupChatComposeMessageFragment.this.startMsgListQuery();
        }
    };
    private DefaultSmsAppChangedReceiver mDefSmsAppChangedReceiver = null;
    private ImageButton mEmojiAdd;
    RcsFileTransMessageForwarder mForwarder;
    private ImageButton mFullScreenEdit;
    private String mGlobalGroupID = "";
    private String mGroupID = "";
    private String mGroupName = "";
    private int mGroupNumber = 0;
    private int mGroupStatus = -1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    if (RcsGroupChatComposeMessageFragment.this.isAdded()) {
                        if (RcsGroupChatComposeMessageFragment.this.mIsAttachmentShow) {
                            RcsGroupChatComposeMessageFragment.this.setAttachmentPagerAdapter(true, RcsGroupChatComposeMessageFragment.this.getContext());
                        }
                        if (RcsGroupChatComposeMessageFragment.this.mIsSmileyFaceShow) {
                            RcsGroupChatComposeMessageFragment.this.setSmileyPagerAdapter(true, RcsGroupChatComposeMessageFragment.this.getContext());
                        }
                        if (RcsGroupChatComposeMessageFragment.this.mMenuEx != null) {
                            RcsGroupChatComposeMessageFragment.this.mMenuEx.onPrepareOptionsMenu();
                            return;
                        }
                        return;
                    }
                    return;
                case 1002:
                case 1003:
                case 1004:
                    if (RcsGroupChatComposeMessageFragment.this.isAdded()) {
                        RcsGroupChatComposeMessageFragment.this.showEnableFullScreenIcon();
                        return;
                    }
                    return;
                case 1006:
                    RcsGroupChatComposeMessageFragment.this.refreshAttachmentChangedUI(msg.arg1);
                    return;
                case 9805:
                    HwMessageUtils.showKeyboard(RcsGroupChatComposeMessageFragment.this.getActivity(), RcsGroupChatComposeMessageFragment.this.mRichEditor);
                    return;
                case 9806:
                    if (msg.obj != null) {
                        RcsGroupChatComposeMessageFragment.this.setAttachmentPagerAdapter(((Boolean) msg.obj).booleanValue(), RcsGroupChatComposeMessageFragment.this.getContext());
                        return;
                    }
                    return;
                case 1000101:
                    RcsGroupChatMessageItem groupMsgItem = msg.obj;
                    RcsGroupChatComposeMessageFragment.this.editMessageItem(groupMsgItem);
                    DelaySendManager.getInst().removeDelayMsg(groupMsgItem.getCancelId(), groupMsgItem.getMessageType(), false);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHasGroupDraft = false;
    private Runnable mHidePanelRunnable = new Runnable() {
        public void run() {
            if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager != null && RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
                RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.showHideMediaPicker(false, false);
                RcsGroupChatComposeMessageFragment.this.mIsAttachmentShow = false;
            } else if (RcsGroupChatComposeMessageFragment.this.mIsSmileyFaceShow && RcsGroupChatComposeMessageFragment.this.mSmileyFaceStub != null) {
                RcsGroupChatComposeMessageFragment.this.mSmileyFaceStub.setVisibility(8);
                RcsGroupChatComposeMessageFragment.this.mIsSmileyFaceShow = false;
            }
        }
    };
    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_REASON = "reason";
        String SYSTEM_RECENTAPPS_KEY = "recentapps";

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
                String reason = intent.getStringExtra(this.SYSTEM_REASON);
                if (TextUtils.equals(reason, this.SYSTEM_HOME_KEY) || TextUtils.equals(reason, this.SYSTEM_RECENTAPPS_KEY)) {
                    RcsGroupChatComposeMessageFragment.this.resumeListClear();
                }
                if (TextUtils.equals(reason, this.SYSTEM_HOME_KEY)) {
                    RcsGroupChatComposeMessageFragment.this.resumeListClear();
                }
            }
        }
    };
    private boolean mIsAllSelected;
    private boolean mIsAttachmentShow = false;
    private boolean mIsCanCopyText = false;
    private boolean mIsCanDelete = false;
    private boolean mIsCanForward = false;
    private boolean mIsCanSave = false;
    private boolean mIsCanSelectAll = false;
    private boolean mIsCanSeleteText = false;
    private boolean mIsKeyboardOpen;
    private boolean mIsLandscape;
    private boolean mIsLogin = false;
    private boolean mIsNeedSendComposing = true;
    private boolean mIsOwner = false;
    private boolean mIsRcsEnable = false;
    private boolean mIsRecordVideo;
    private boolean mIsRunning;
    private boolean mIsSmileyFaceShow = false;
    private boolean mIsSmsEnabled;
    private boolean mKeyboardHiden = false;
    private long mLastMessageId;
    private int mLastSmoothScrollPosition;
    private LocalBroadcastManager mLocalBroadcastManager = null;
    private MenuEx mMenuEx;
    private View mMessageBlockView = null;
    private boolean mMessagesAndDraftLoaded;
    private RcsGroupChatMessageItem mMsgItem = null;
    private RcsGroupChatMessageListAdapter mMsgListAdapter;
    private final OnCreateContextMenuListener mMsgListMenuCreateListener = new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            if (v == null || menuInfo == null || menu == null || !(v instanceof RcsGroupChatMessageListView)) {
                if (Log.isLoggable("Mms_app", 6)) {
                    Log.e("RcsGroupChatComposeMessageFragment", "CreateContextMenu FIELD");
                }
            } else if (RcsGroupChatComposeMessageFragment.this.isCursorValid()) {
                Cursor cursor = RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getCursor();
                GroupMessageColumn colums = new GroupMessageColumn(cursor);
                String msgId = cursor.getString(colums.columnGlobalID);
                Long msgTime = Long.valueOf(cursor.getLong(colums.columnDate));
                int status = cursor.getInt(colums.columnStatus);
                RcsGroupChatComposeMessageFragment.this.getActivity().getMenuInflater().inflate(R.menu.rcs_groupchat_compose_message_menu, menu);
                MenuItem mi = menu.findItem(R.id.menu_delivery_report);
                if (101 == status) {
                    mi.setEnabled(true);
                    Intent intent = new Intent(RcsGroupChatComposeMessageFragment.this.getContext(), RcsGroupChatDeliveryReportActivity.class);
                    intent.putExtra("bundle_message_id", msgId);
                    intent.putExtra("bundle_sent_time", msgTime);
                    mi.setIntent(intent);
                } else {
                    mi.setEnabled(false);
                }
            }
        }
    };
    private RcsGroupChatMessageListView mMsgListView;
    private HashMap<Integer, IfMsgplusCbImpl> mMsgplusListeners = new HashMap();
    private int mMultyOperType = 0;
    private ContentObserver mObMemberChange = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            RcsGroupChatComposeMessageFragment.this.startGroupMemberInfoQuery();
        }
    };
    private ArrayList<Uri> mPicUriList;
    private int mPopId;
    private int mPos;
    private boolean mPossiblePendingNotification;
    private RcsAudioMessage mRcsAudioMessage;
    public RcsGroupConversationInputHostHolder mRcsGroupConversationInputHostHolder = new RcsGroupConversationInputHostHolder();
    private ConversationInputManager mRcsGroupConversationInputManager;
    private LinearLayout mRcsGroupEditorView;
    private ArrayList<RcsGroupMediaUpdateListener> mRcsGroupMediaUpdateListeners;
    private Button mRcsGroupPickAudio;
    private RcsLoginStatusChangeBroadCastReceiver mRcsLoginStatusChangeBroadCastReceiver;
    private long mRcsThreadId = 0;
    private Handler mRcseEventHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.obj;
            if (bundle != null) {
                switch (msg.what) {
                    case AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR /*1101*/:
                        RcsUtility.scrollWhenMsgFailed(bundle, RcsGroupChatComposeMessageFragment.this.mMsgListView, RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getCursor());
                        break;
                    case AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT /*1102*/:
                        MLog.v("RcsGroupChatComposeMessageFragment FileTrans: ", "Update file transfer progress.");
                        RcsGroupChatComposeMessageFragment.this.updateFileTransProgress(bundle);
                        return;
                    case AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS /*1201*/:
                        RcsGroupChatComposeMessageFragment.this.updateSendButtonState();
                        RcsGroupChatComposeMessageFragment.this.updateAddAttachView();
                        RcsGroupChatComposeMessageFragment.this.updateTitle();
                        RcsGroupChatComposeMessageFragment.this.setLoginAndOwnerStatus();
                        if (!RcsGroupChatComposeMessageFragment.this.mIsOwner) {
                            RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.notifyDataSetChanged();
                            break;
                        }
                        break;
                    case 1301:
                        String groupId = bundle.getString("group_id");
                        if (groupId != null && groupId.equals(RcsGroupChatComposeMessageFragment.this.mGroupID)) {
                            RcsGroupChatComposeMessageFragment.this.composingEventHandler(bundle.getString("rcs.composing.peer"), bundle.getBoolean("rcs.composing.status"));
                            break;
                        }
                    case AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST /*2000*/:
                        long resume_threadid = bundle.getLong("resume_threadid");
                        MLog.d("RcsGroupChatComposeMessageFragment", "FT_RESUME_FAILED_GROUP: =" + resume_threadid + " mIsRunning" + RcsGroupChatComposeMessageFragment.this.mIsRunning + "mThreadID " + RcsGroupChatComposeMessageFragment.this.mThreadID);
                        if (RcsGroupChatComposeMessageFragment.this.mIsRunning && RcsGroupChatComposeMessageFragment.this.mThreadID == resume_threadid) {
                            RcsGroupChatComposeMessageFragment.this.ftresumeStopDialog(bundle);
                            break;
                        }
                }
            }
        }
    };
    Runnable mResetFileRunnable = new Runnable() {
        public void run() {
            RcsGroupChatComposeMessageFragment.this.resetFileSent();
        }
    };
    Runnable mResetMessageRunnable = new Runnable() {
        public void run() {
            RcsGroupChatComposeMessageFragment.this.resetMessage();
        }
    };
    private RcsGroupChatRichMessageEditor mRichEditor;
    private EditableSlides$RichMessageListener mRichEditorListener = new EditableSlides$RichMessageListener() {
        public void onContentChange() {
            RcsGroupChatComposeMessageFragment.this.onUserInteraction();
            if (RcsProfile.canProcessGroupChat(RcsGroupChatComposeMessageFragment.this.mGroupID) && !RcsGroupChatComposeMessageFragment.mIsInvalidGroupStatus && RcsGroupChatComposeMessageFragment.this.mIsSmsEnabled && RcsGroupChatComposeMessageFragment.this.mIsNeedSendComposing) {
                RcsGroupChatComposeMessageFragment.this.mComposeEventHdlr.sendMessage(RcsGroupChatComposeMessageFragment.this.mComposeEventHdlr.obtainMessage(1));
            } else {
                RcsGroupChatComposeMessageFragment.this.mIsNeedSendComposing = true;
            }
            RcsGroupChatComposeMessageFragment.this.updateSendButtonState();
            RcsGroupChatComposeMessageFragment.this.updateTextCounter();
            RcsGroupChatComposeMessageFragment.this.showEnableFullScreenIcon();
        }

        public void richToCheckRestrictedMime(boolean value) {
        }

        public void onDraftLoaded() {
        }

        public void onInputManagerShow() {
            if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
                RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.showHideMediaPicker(false, false);
                RcsGroupChatComposeMessageFragment.this.mIsAttachmentShow = false;
            } else if (RcsGroupChatComposeMessageFragment.this.mIsSmileyFaceShow) {
                RcsGroupChatComposeMessageFragment.this.mSmileyFaceStub.setVisibility(8);
                RcsGroupChatComposeMessageFragment.this.mIsSmileyFaceShow = false;
            }
        }
    };
    private View mRootView;
    private BroadcastReceiver mSaveDraftReceiver;
    private int mSavedScrollPosition = -1;
    private int mScreenHeight;
    private boolean mScrollOnSend;
    private LinearLayout mSendButtonLayout;
    private TextView mSendButtonSms;
    private boolean mShouldLoadDraft;
    private AttachmentSmileyPagerAdatper mSmileyFaceSelectorAdapter;
    private ViewStub mSmileyFaceStub;
    protected SplitActionBarView mSplitActionBar;
    private TextView mTextCounter;
    private long mThreadID = 0;
    RcsGroupChatMapClickCallback mapClickCallback = null;

    public interface RcsGroupMediaUpdateListener {
        boolean hasUpdateMedida(int i);

        void updateMedia(RcsGroupChatRichMessageEditor rcsGroupChatRichMessageEditor);
    }

    public interface IDraftLoaded {
        void onDraftLoaded(Uri uri);
    }

    private final class BackgroundQueryHandler extends ConversationQueryHandler {
        protected void onQueryComplete(int r12, java.lang.Object r13, android.database.Cursor r14) {
            /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r11 = this;
            r10 = 1;
            switch(r12) {
                case 9527: goto L_0x008c;
                case 9528: goto L_0x0098;
                case 9529: goto L_0x00f2;
                case 9701: goto L_0x0005;
                case 9702: goto L_0x0048;
                default: goto L_0x0004;
            };
        L_0x0004:
            return;
        L_0x0005:
            if (r14 == 0) goto L_0x003b;
        L_0x0007:
            r7 = r14.getCount();	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
            if (r7 == 0) goto L_0x003b;	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
        L_0x000d:
            r14.moveToFirst();	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
        L_0x0010:
            r7 = r14.isAfterLast();	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
            if (r7 != 0) goto L_0x003b;	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
        L_0x0016:
            r7 = "sdk_rcs_group_message_id";	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
            r7 = r14.getColumnIndexOrThrow(r7);	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
            r4 = r14.getLong(r7);	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
            r4 = -r4;	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
            r8 = 2;	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
            com.huawei.rcs.utils.RcsTransaction.rejectFile(r4, r8);	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
            r14.moveToNext();	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
            goto L_0x0010;
        L_0x002b:
            r0 = move-exception;
            r7 = "RcsGroupChatComposeMessageFragment";	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
            r8 = "cursor unknowable error";	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
            com.huawei.cspcommon.MLog.e(r7, r8);	 Catch:{ RuntimeException -> 0x002b, all -> 0x0041 }
            if (r14 == 0) goto L_0x0004;
        L_0x0037:
            r14.close();
            goto L_0x0004;
        L_0x003b:
            if (r14 == 0) goto L_0x0004;
        L_0x003d:
            r14.close();
            goto L_0x0004;
        L_0x0041:
            r7 = move-exception;
            if (r14 == 0) goto L_0x0047;
        L_0x0044:
            r14.close();
        L_0x0047:
            throw r7;
        L_0x0048:
            if (r14 == 0) goto L_0x007f;
        L_0x004a:
            r7 = r14.getCount();	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            if (r7 == 0) goto L_0x007f;	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
        L_0x0050:
            r14.moveToFirst();	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
        L_0x0053:
            r7 = r14.isAfterLast();	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            if (r7 != 0) goto L_0x007f;	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
        L_0x0059:
            r7 = "sdk_rcs_group_message_id";	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            r7 = r14.getColumnIndexOrThrow(r7);	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            r4 = r14.getLong(r7);	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            r4 = -r4;	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            r8 = 2;	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            r7 = 1;	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            com.huawei.rcs.utils.RcsTransaction.cancelFT(r4, r7, r8);	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            r14.moveToNext();	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            goto L_0x0053;
        L_0x006f:
            r0 = move-exception;
            r7 = "RcsGroupChatComposeMessageFragment";	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            r8 = "cursor unknowable error";	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            com.huawei.cspcommon.MLog.e(r7, r8);	 Catch:{ RuntimeException -> 0x006f, all -> 0x0085 }
            if (r14 == 0) goto L_0x0004;
        L_0x007b:
            r14.close();
            goto L_0x0004;
        L_0x007f:
            if (r14 == 0) goto L_0x0004;
        L_0x0081:
            r14.close();
            goto L_0x0004;
        L_0x0085:
            r7 = move-exception;
            if (r14 == 0) goto L_0x008b;
        L_0x0088:
            r14.close();
        L_0x008b:
            throw r7;
        L_0x008c:
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;
            r7.mCursor = r14;
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;
            r7.onCached();
            goto L_0x0004;
        L_0x0098:
            r1 = 0;
            if (r14 == 0) goto L_0x00d3;
        L_0x009b:
            r1 = r14.getCount();	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r2 = r1 + 1;	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r7 = r7.mGroupNumber;	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            if (r7 == r2) goto L_0x00d3;	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
        L_0x00a9:
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r7.mGroupNumber = r2;	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r7.updateTitle();	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r7 = "RcsGroupChatComposeMessageFragment";	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r8 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r8.<init>();	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r9 = "after query mGroupNumber = ";	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r8 = r8.append(r9);	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r9 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r9 = r9.mGroupNumber;	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r8 = r8.append(r9);	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r8 = r8.toString();	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            com.huawei.cspcommon.MLog.i(r7, r8);	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
        L_0x00d3:
            if (r14 == 0) goto L_0x0004;
        L_0x00d5:
            r14.close();
            goto L_0x0004;
        L_0x00da:
            r0 = move-exception;
            r7 = "RcsGroupChatComposeMessageFragment";	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            r8 = "onQueryComplete GROUP_INFO_QUERY_TOKEN error";	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            com.huawei.cspcommon.MLog.e(r7, r8);	 Catch:{ RuntimeException -> 0x00da, all -> 0x00eb }
            if (r14 == 0) goto L_0x0004;
        L_0x00e6:
            r14.close();
            goto L_0x0004;
        L_0x00eb:
            r7 = move-exception;
            if (r14 == 0) goto L_0x00f1;
        L_0x00ee:
            r14.close();
        L_0x00f1:
            throw r7;
        L_0x00f2:
            r3 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.mIsInvalidGroupStatus;
            if (r14 == 0) goto L_0x012e;
        L_0x00f8:
            r7 = r14.moveToFirst();	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            if (r7 == 0) goto L_0x012e;	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
        L_0x00fe:
            r7 = "status";	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            r6 = r14.getColumnIndexOrThrow(r7);	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            r7 = r14.getInt(r6);	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            r7 = com.huawei.rcs.utils.RcseMmsExt.checkInvalidGroupStatus(r7);	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            com.android.rcs.ui.RcsGroupChatComposeMessageFragment.mIsInvalidGroupStatus = r7;	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            r7 = "RcsGroupChatComposeMessageFragment";	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            r8 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            r8.<init>();	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            r9 = " UPDATE_GROUP_STATUS mIsInvalidGroupStatus = ";	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            r8 = r8.append(r9);	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            r9 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.mIsInvalidGroupStatus;	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            r8 = r8.append(r9);	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            r8 = r8.toString();	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            com.huawei.cspcommon.MLog.d(r7, r8);	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
        L_0x012e:
            if (r14 == 0) goto L_0x0133;
        L_0x0130:
            r14.close();
        L_0x0133:
            if (r3 != 0) goto L_0x0004;
        L_0x0135:
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.mIsInvalidGroupStatus;
            if (r7 == 0) goto L_0x0004;
        L_0x013b:
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;
            r7.updateInviateGroupChatView();
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;
            r7 = r7.mAttachmentPreview;
            if (r7 == 0) goto L_0x0153;
        L_0x0148:
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;
            r7 = r7.mAttachmentPreview;
            r8 = 8;
            r7.setVisibility(r8);
        L_0x0153:
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;
            r7.updateAddAttachView();
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;
            r7.updateSendButtonState();
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;
            r7 = r7.mActionBarWhenSplit;
            if (r7 == 0) goto L_0x0004;
        L_0x0163:
            r7 = com.android.rcs.ui.RcsGroupChatComposeMessageFragment.this;
            r7 = r7.mActionBarWhenSplit;
            r8 = 2130837918; // 0x7f02019e float:1.7280804E38 double:1.052773812E-314;
            r7.setEndIconDisable(r10, r8);
            goto L_0x0004;
        L_0x016f:
            r0 = move-exception;
            r7 = "RcsGroupChatComposeMessageFragment";	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            r8 = "onQueryComplete UPDATE_GROUP_STATUS error";	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            com.huawei.cspcommon.MLog.e(r7, r8);	 Catch:{ RuntimeException -> 0x016f, all -> 0x017f }
            if (r14 == 0) goto L_0x0133;
        L_0x017b:
            r14.close();
            goto L_0x0133;
        L_0x017f:
            r7 = move-exception;
            if (r14 == 0) goto L_0x0185;
        L_0x0182:
            r14.close();
        L_0x0185:
            throw r7;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.rcs.ui.RcsGroupChatComposeMessageFragment.BackgroundQueryHandler.onQueryComplete(int, java.lang.Object, android.database.Cursor):void");
        }

        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);
            switch (token) {
                case AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST /*2000*/:
                    RcsGroupChatComposeMessageFragment.this.getRcsGroupChatComposeMessageActivity().onFragmentRelease();
                    break;
                case 9700:
                    if ((cookie instanceof Boolean) && ((Boolean) cookie).booleanValue()) {
                        RcsGroupChatComposeMessageFragment.this.mLastMessageId = 0;
                    }
                    MessagingNotification.nonBlockingUpdateNewMessageIndicator(RcsGroupChatComposeMessageFragment.this.getContext(), -2, false);
                    break;
            }
            if (token == 9700) {
                RcsGroupChatComposeMessageFragment.this.startMsgListQuery();
            }
        }

        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            super.onInsertComplete(token, cookie, uri);
            MLog.d("RcsGroupChatComposeMessageFragment", "token " + token + " uri " + uri);
            if (token == 9799) {
                RcsGroupChatComposeMessageFragment.this.getActivity().setProgressBarVisibility(false);
                if (uri == null) {
                    ResEx.makeToast(R.string.add_favorite_failed_Toast);
                }
            } else if (token == 9798 && uri == null) {
                ResEx.makeToast(R.string.add_favorite_failed_Toast);
            }
        }
    }

    private class ComposeEventHdlr extends Handler {
        public void handleMessage(Message aMsg) {
            switch (aMsg.what) {
                case 1:
                    RcsGroupChatComposeMessageFragment.this.handleComposingEvent();
                    return;
                default:
                    return;
            }
        }

        public ComposeEventHdlr(Looper aLooper) {
            super(aLooper);
        }
    }

    private class DeleteGroupMulMessageListener implements DialogInterface.OnClickListener {
        private final long[] mIds;
        private int mInsertMsgCnt = 0;
        private int mRcsChatItemsCount = 0;
        private final long[] mTypes;

        public DeleteGroupMulMessageListener(long[] msgIds, String[] address, long[] types) {
            this.mIds = msgIds;
            this.mTypes = types;
        }

        private void deleteMulti(final long[] mIds, long[] mTypes) {
            if (mIds.length > 0) {
                RcsGroupChatComposeMessageFragment.this.getAsyncDialog().runAsync(new Runnable() {
                    public void run() {
                        List<Long> idList = new ArrayList();
                        if (mIds == null) {
                            MLog.d("RcsGroupChatComposeMessageFragment", "delete multi mIds is null");
                        } else if (mIds.length == 0) {
                            MLog.d("RcsGroupChatComposeMessageFragment", "delete multi mIds length is zero ");
                        } else {
                            StringBuilder ids = new StringBuilder(" msg_id IN ( ").append(mIds[0]);
                            for (int i = 0; i < mIds.length; i++) {
                                idList.add(Long.valueOf(mIds[i]));
                                if (i > 0) {
                                    ids.append(", ").append(mIds[i]);
                                }
                            }
                            ids.append(" ) ");
                            boolean isAllSelected = mIds.length > 0 && mIds.length == RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getInOutMsgCount();
                            MLog.d("RcsGroupChatComposeMessageFragment", "idListChat = " + idList + "isAllSelected = " + isAllSelected);
                            if (RcsGroupChatComposeMessageFragment.this.mMultyOperType == 0) {
                                if (idList.size() > 0) {
                                    String selection = "_id IN( select msg_id from file_trans where" + ids + " AND transfer_status IN  (" + 1000 + "," + 1007 + "))";
                                    MLog.d("RcsGroupChatComposeMessageFragment", "selection=" + selection);
                                    RcsGroupChatComposeMessageFragment.this.mBackgroundQueryHandler.startQuery(9702, null, Uri.parse("content://rcsim/rcs_group_message"), new String[]{"sdk_rcs_group_message_id"}, selection, null, null);
                                    String selectionReject = "_id IN( select msg_id from file_trans where" + ids + " AND transfer_status IN  (" + Place.TYPE_POSTAL_TOWN + "))";
                                    MLog.d("RcsGroupChatComposeMessageFragment", "selectionReject=" + selectionReject);
                                    RcsGroupChatComposeMessageFragment.this.mBackgroundQueryHandler.startQuery(9701, null, Uri.parse("content://rcsim/rcs_group_message"), new String[]{"sdk_rcs_group_message_id"}, selectionReject, null, null);
                                    if (isAllSelected) {
                                        RcsGroupChatComposeMessageFragment.this.mBackgroundQueryHandler.startDelete(9700, null, ContentUris.withAppendedId(RcsGroupChatComposeMessageFragment.sDeleteUri, RcsGroupChatComposeMessageFragment.this.mThreadID), "type IN (1, 4, 100, 101)", null);
                                    } else {
                                        RcsGroupChatComposeMessageFragment.this.mBackgroundQueryHandler.startDelete(9700, null, RcsGroupChatComposeMessageFragment.sDeleteAllUri, FavoritesUtils.getSelectionString("_id", (List) idList), null);
                                    }
                                    RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.removeCache(idList);
                                }
                            } else if (RcsGroupChatComposeMessageFragment.this.mMultyOperType == 1) {
                                RcsGroupChatComposeMessageFragment.this.getActivity().setProgressBarVisibility(true);
                                DeleteGroupMulMessageListener.this.mRcsChatItemsCount = idList.size();
                                int dupcnt = FavoritesUtils.getHwCust().checkAndRemoveDuplicateGroupChatMsgs(RcsGroupChatComposeMessageFragment.this.getContext(), idList);
                                MLog.d("RcsGroupChatComposeMessageFragment", "dupcnt " + dupcnt);
                                if (dupcnt == -1) {
                                    DeleteGroupMulMessageListener.this.mInsertMsgCnt = -1;
                                    return;
                                }
                                DeleteGroupMulMessageListener.this.mInsertMsgCnt = idList.size();
                                if (DeleteGroupMulMessageListener.this.mInsertMsgCnt != 0 && idList.size() > 0) {
                                    RcsFileTransDataHander.addNewFavTransTransRecord(RcsGroupChatComposeMessageFragment.this.getContext(), idList, 2);
                                    RcsGroupChatComposeMessageFragment.this.mBackgroundQueryHandler.startInsert(9799, null, HwCustFavoritesUtils.URI_FAV_GROUP_CHAT, FavoritesUtils.getAddFavoritesContent(HwCustFavoritesUtils.OPER_TYPE_GROUP_CHAT_MULTY, idList));
                                }
                            }
                        }
                    }
                }, new Runnable() {
                    public void run() {
                        if (RcsGroupChatComposeMessageFragment.this.mMultyOperType == 1 && DeleteGroupMulMessageListener.this.mInsertMsgCnt <= 0) {
                            String strRes;
                            if (DeleteGroupMulMessageListener.this.mInsertMsgCnt == -1) {
                                strRes = RcsGroupChatComposeMessageFragment.this.getResources().getString(R.string.add_favorite_failed_Toast);
                            } else {
                                strRes = RcsGroupChatComposeMessageFragment.this.getResources().getQuantityString(R.plurals.already_in_favorites_Toast_Plurals, DeleteGroupMulMessageListener.this.mRcsChatItemsCount, new Object[]{Integer.valueOf(DeleteGroupMulMessageListener.this.mRcsChatItemsCount)});
                            }
                            Toast.makeText(RcsGroupChatComposeMessageFragment.this.getContext(), strRes, 0).show();
                        }
                        RcsGroupChatComposeMessageFragment.this.mMsgListView.exitEditMode();
                    }
                }, RcsGroupChatComposeMessageFragment.BUTTON_DELETE[RcsGroupChatComposeMessageFragment.this.mMultyOperType]);
            }
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -1:
                    deleteMulti(this.mIds, this.mTypes);
                    RcsGroupChatComposeMessageFragment.this.mMsgListView.exitEditMode();
                    return;
                default:
                    return;
            }
        }
    }

    private class EMUIGroupChatListViewListener implements OnItemLongClickListener, EmuiListViewListener, SelectionChangedListener {
        private HashMap<View, Integer> hashMap;

        private EMUIGroupChatListViewListener() {
            this.hashMap = new HashMap();
        }

        private void saveViewState() {
            if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
                RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.showHideMediaPicker(false, false);
                RcsGroupChatComposeMessageFragment.this.mIsAttachmentShow = false;
            }
            if (RcsGroupChatComposeMessageFragment.this.mIsSmileyFaceShow) {
                RcsGroupChatComposeMessageFragment.this.mSmileyFaceStub.setVisibility(8);
                RcsGroupChatComposeMessageFragment.this.mIsSmileyFaceShow = false;
            }
            this.hashMap.clear();
            if (RcsGroupChatComposeMessageFragment.this.mBottomPanel != null) {
                this.hashMap.put(RcsGroupChatComposeMessageFragment.this.mBottomPanel, Integer.valueOf(RcsGroupChatComposeMessageFragment.this.mBottomPanel.getVisibility()));
            }
            for (View v : this.hashMap.keySet()) {
                v.setVisibility(8);
            }
            RcsGroupChatComposeMessageFragment.this.hideKeyboard();
        }

        private void restoreViewState() {
            for (Entry<View, Integer> entry : this.hashMap.entrySet()) {
                ((View) entry.getKey()).setVisibility(((Integer) entry.getValue()).intValue());
            }
        }

        public boolean onItemLongClick(AdapterView<?> listView, View view, int position, long id) {
            View v = listView.getChildAt(position - listView.getFirstVisiblePosition());
            if (v == null || !(v instanceof RcsGroupChatMessageListItem)) {
                return true;
            }
            RcsGroupChatComposeMessageFragment.this.mMessageBlockView = ((RcsGroupChatMessageListItem) v).getMessageBlockSuper();
            RcsGroupChatComposeMessageFragment.this.mMsgItem = RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getMessageItemWithIdAssigned(position, RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getCursor());
            if (RcsGroupChatComposeMessageFragment.this.mMsgItem == null) {
                return true;
            }
            RcsGroupChatComposeMessageFragment.this.hideKeyboard();
            RcsGroupChatComposeMessageFragment.this.mPos = position;
            RcsGroupChatComposeMessageFragment.this.mIsCanCopyText = true;
            RcsGroupChatComposeMessageFragment.this.mIsCanForward = true;
            RcsGroupChatComposeMessageFragment.this.mIsCanDelete = true;
            RcsGroupChatComposeMessageFragment.this.mIsCanSeleteText = true;
            RcsGroupChatComposeMessageFragment.this.mIsCanSelectAll = true;
            RcsFileTransGroupMessageItem fileTransGroupMsgItem = RcsGroupChatComposeMessageFragment.this.mMsgItem.mFtGroupMsgItem;
            if (fileTransGroupMsgItem != null) {
                RcsGroupChatComposeMessageFragment.this.mIsCanCopyText = false;
                RcsGroupChatComposeMessageFragment.this.mIsCanSeleteText = false;
                if (!fileTransGroupMsgItem.isLocation() && !fileTransGroupMsgItem.isVCardFileTypeMsg()) {
                    RcsGroupChatComposeMessageFragment.this.mIsCanSave = true;
                    String path = fileTransGroupMsgItem.mImAttachmentPath;
                    boolean isFileExist = RcsTransaction.isFileExist(path);
                    boolean isFileCanSave = (fileTransGroupMsgItem.mIsOutgoing || fileTransGroupMsgItem.mImAttachmentStatus == 1002) ? true : fileTransGroupMsgItem.mImAttachmentStatus == Place.TYPE_ROUTE;
                    MLog.i("RcsGroupChatComposeMessageFragment", "setMsgItemVisible path=" + path + " isFileExist=" + isFileExist);
                    if (isFileExist && isFileCanSave) {
                        RcsGroupChatComposeMessageFragment.this.mIsCanSave = true;
                    } else {
                        RcsGroupChatComposeMessageFragment.this.mIsCanForward = false;
                        RcsGroupChatComposeMessageFragment.this.mIsCanSave = false;
                    }
                } else if (fileTransGroupMsgItem.isVCardFileTypeMsg() && !RcsTransaction.isFileExist(fileTransGroupMsgItem.mImAttachmentPath)) {
                    RcsGroupChatComposeMessageFragment.this.mIsCanForward = false;
                }
            }
            RcsGroupChatComposeMessageFragment.this.isLocationType();
            RcsGroupChatComposeMessageFragment.this.showPopupFloatingToolbar();
            return true;
        }

        private void updateActionBarTitle(int cnt) {
            RcsGroupChatComposeMessageFragment.this.updateComposeTitle(cnt);
        }

        public void onEnterEditMode() {
            saveViewState();
            RcsGroupChatComposeMessageFragment.this.enterEditUpdate();
            RcsGroupChatComposeMessageFragment.this.mMsgListView.setTag("enable-multi-select-move");
        }

        public void onExitEditMode() {
            restoreViewState();
            RcsGroupChatComposeMessageFragment.this.exitEditUpdate();
            RcsGroupChatComposeMessageFragment.this.mMsgListView.setTag("disable-multi-select-move");
            if (RcsGroupChatComposeMessageFragment.this.mBottomPanel != null) {
                if (RcsGroupChatComposeMessageFragment.mIsInvalidGroupStatus) {
                    RcsGroupChatComposeMessageFragment.this.mBottomPanel.setVisibility(8);
                } else {
                    RcsGroupChatComposeMessageFragment.this.mBottomPanel.setVisibility(0);
                }
            }
            RcsGroupChatComposeMessageFragment.this.updateTitle();
        }

        public EditHandler getHandler(int mode) {
            return null;
        }

        public int getHintColor(int mode, int count) {
            return 0;
        }

        public String getHintText(int mode, int count) {
            return null;
        }

        public void onSelectChange(int selectedSize, int totalSize) {
            boolean z = false;
            RcsGroupChatComposeMessageFragment rcsGroupChatComposeMessageFragment = RcsGroupChatComposeMessageFragment.this;
            if (selectedSize == RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getInOutMsgCount() && selectedSize > 0) {
                z = true;
            }
            rcsGroupChatComposeMessageFragment.mIsAllSelected = z;
            RcsGroupChatComposeMessageFragment.this.mMenuEx.setAllChecked(RcsGroupChatComposeMessageFragment.this.mIsAllSelected, RcsGroupChatComposeMessageFragment.this.isInLandscape());
            updateActionBarTitle(selectedSize);
            RcsGroupChatComposeMessageFragment.this.mMenuEx.switchToEdit(true);
        }
    }

    private class FloatingCallback2 extends Callback2 {
        private boolean mWasAlreadyClick;

        private FloatingCallback2() {
            this.mWasAlreadyClick = false;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(null);
            mode.setSubtitle(null);
            mode.setTitleOptionalHint(true);
            populateMenuWithItems(menu);
            return true;
        }

        private void populateMenuWithItems(Menu menu) {
            TypefaceSpan span = new TypefaceSpan("default");
            if (RcsGroupChatComposeMessageFragment.this.mIsCanCopyText) {
                SpannableString spanString = new SpannableString(RcsGroupChatComposeMessageFragment.this.getString(R.string.button_copy_text));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 1, 0, spanString).setShowAsAction(2);
            }
            if (RcsGroupChatComposeMessageFragment.this.mIsCanForward) {
                spanString = new SpannableString(RcsGroupChatComposeMessageFragment.this.getString(R.string.forward_message));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 2, 1, spanString).setShowAsAction(2);
            }
            if (RcsGroupChatComposeMessageFragment.this.mIsCanDelete) {
                spanString = new SpannableString(RcsGroupChatComposeMessageFragment.this.getString(R.string.delete));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 3, 2, spanString).setShowAsAction(2);
            }
            if (RcsGroupChatComposeMessageFragment.this.mIsCanSeleteText) {
                spanString = new SpannableString(RcsGroupChatComposeMessageFragment.this.getString(R.string.mms_select_text_copy));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 4, 3, spanString).setShowAsAction(2);
            }
            if (RcsGroupChatComposeMessageFragment.this.mIsCanSave) {
                spanString = new SpannableString(RcsGroupChatComposeMessageFragment.this.getString(R.string.save));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 5, 4, spanString).setShowAsAction(1);
            }
            if (RcsGroupChatComposeMessageFragment.this.mIsCanSelectAll) {
                spanString = new SpannableString(RcsGroupChatComposeMessageFragment.this.getString(R.string.menu_add_rcs_more));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 6, 5, spanString).setShowAsAction(1);
            }
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (this.mWasAlreadyClick) {
                return true;
            }
            switch (item.getItemId()) {
                case 1:
                    this.mWasAlreadyClick = true;
                    HwMessageUtils.copyToClipboard(RcsGroupChatComposeMessageFragment.this.getContext(), RcsGroupChatComposeMessageFragment.this.mMsgItem.mBody);
                    break;
                case 2:
                    this.mWasAlreadyClick = true;
                    Integer[] selection = new Integer[]{Integer.valueOf(RcsGroupChatComposeMessageFragment.this.mPos)};
                    if (!RcsGroupChatComposeMessageFragment.this.detectMessageToForwardForFt(selection, RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getCursor())) {
                        if (!RcsGroupChatComposeMessageFragment.this.detectMessageToForwardForLoc(selection, RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getCursor())) {
                            RcsGroupChatComposeMessageFragment.this.forwardMessage(RcsGroupChatComposeMessageFragment.this.mMsgItem);
                            break;
                        }
                        RcsGroupChatComposeMessageFragment.this.forwardLoc();
                        break;
                    }
                    RcsGroupChatComposeMessageFragment.this.toForward();
                    break;
                case 3:
                    this.mWasAlreadyClick = true;
                    RcsGroupChatComposeMessageFragment.this.mMsgListView.addItem(RcsGroupChatComposeMessageFragment.this.mPos);
                    RcsGroupChatComposeMessageFragment.this.mPopId = 278925315;
                    RcsGroupChatComposeMessageFragment.this.mMultyOperType = 0;
                    RcsGroupChatComposeMessageFragment.this.mMsgListView.startMultiChoice();
                    RcsGroupChatComposeMessageFragment.this.mMsgListView.onMenuItemClick(RcsGroupChatComposeMessageFragment.this.mPopId);
                    RcsGroupChatComposeMessageFragment.this.mMsgListView.removeItem(RcsGroupChatComposeMessageFragment.this.mPos);
                    break;
                case 4:
                    if (RcsGroupChatComposeMessageFragment.this.mMsgItem != null) {
                        this.mWasAlreadyClick = true;
                        if (HwMessageUtils.isSplitOn()) {
                            if (((Activity) RcsGroupChatComposeMessageFragment.this.getContext()) instanceof ConversationList) {
                                MessageUtils.viewRcsMessageText(RcsGroupChatComposeMessageFragment.this.getContext(), RcsGroupChatComposeMessageFragment.this.mMsgItem, ((ConversationList) RcsGroupChatComposeMessageFragment.this.getContext()).getRightFragment());
                                break;
                            }
                        }
                        MessageUtils.viewRcsMessageText(RcsGroupChatComposeMessageFragment.this.getContext(), RcsGroupChatComposeMessageFragment.this.mMsgItem, null);
                        break;
                    }
                    MLog.e("RcsGroupChatComposeMessageFragment", "MENU_ID_SELECT_TEXT_COPY msgItem == null");
                    return true;
                    break;
                case 5:
                    if (RcsGroupChatComposeMessageFragment.this.mMsgItem != null) {
                        this.mWasAlreadyClick = true;
                        RcsFileTransGroupMessageItem ftGroupMsgItem = RcsGroupChatComposeMessageFragment.this.mMsgItem.mFtGroupMsgItem;
                        if (ftGroupMsgItem != null) {
                            RcsGroupChatComposeMessageFragment.this.saveGroupFileToPhone(ftGroupMsgItem);
                            break;
                        }
                    }
                    return true;
                    break;
                case 6:
                    MLog.d("RcsGroupChatComposeMessageFragment", "enter onItemLongClick");
                    if (!RcsGroupChatComposeMessageFragment.this.mMsgListView.isInEditMode()) {
                        this.mWasAlreadyClick = true;
                        if (RcsGroupChatComposeMessageFragment.this.mMsgListAdapter != null) {
                            int itemViewType = RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getItemViewType(RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getCursor());
                            if (itemViewType == 0 || 3 == itemViewType) {
                                MLog.d("RcsGroupChatComposeMessageFragment", "onItemLongClick(),is notice message do not need enter edit mode");
                                return true;
                            }
                        }
                        RcsGroupChatComposeMessageFragment.this.mMsgListView.enterEditMode(1);
                        RcsGroupChatComposeMessageFragment.this.mMsgListView.setItemSelected(RcsGroupChatComposeMessageFragment.this.mPos);
                        RcsGroupChatComposeMessageFragment.this.mMsgListView.getRecorder().getRcsSelectRecorder().addPosition(RcsGroupChatComposeMessageFragment.this.mPos);
                        RcsGroupChatComposeMessageFragment.this.mMsgListView.getRecorder().add(RcsGroupChatComposeMessageFragment.this.mMsgListView.getItemIdAtPosition(RcsGroupChatComposeMessageFragment.this.mPos));
                        break;
                    }
                    return true;
            }
            if (RcsGroupChatComposeMessageFragment.this.mActionMode != null) {
                RcsGroupChatComposeMessageFragment.this.mActionMode.finish();
                RcsGroupChatComposeMessageFragment.this.mActionMode = null;
            }
            return true;
        }

        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (RcsGroupChatComposeMessageFragment.this.mMessageBlockView != null) {
                outRect.set(0, 0, RcsGroupChatComposeMessageFragment.this.mMessageBlockView.getWidth(), RcsGroupChatComposeMessageFragment.this.mMessageBlockView.getHeight());
            }
        }

        public void onDestroyActionMode(ActionMode mode) {
            RcsGroupChatComposeMessageFragment.this.mMessageBlockView = null;
            RcsGroupChatComposeMessageFragment.this.mIsCanCopyText = false;
            RcsGroupChatComposeMessageFragment.this.mIsCanForward = false;
            RcsGroupChatComposeMessageFragment.this.mIsCanDelete = false;
            RcsGroupChatComposeMessageFragment.this.mIsCanSeleteText = false;
            RcsGroupChatComposeMessageFragment.this.mIsCanSave = false;
            RcsGroupChatComposeMessageFragment.this.mIsCanSelectAll = false;
        }
    }

    private class GroupChatCreateBroadCastReceiver extends BroadcastReceiver {
        private String groupId = "";
        private Intent mIntent;
        private LocationData mLocData;
        private ArrayList<Uri> mPicUriList;
        private long threadId = 0;

        public GroupChatCreateBroadCastReceiver(ArrayList<Uri> picUriList, Intent intent, long threadId, String groupId) {
            this.mPicUriList = picUriList;
            this.mIntent = intent;
            this.threadId = threadId;
            this.groupId = groupId;
        }

        public GroupChatCreateBroadCastReceiver(LocationData locData, Intent intent, String groupId) {
            this.mLocData = locData;
            this.mIntent = intent;
            this.groupId = groupId;
        }

        public void onReceive(Context context, Intent intent) {
            if ("com.huawei.groupchat.create".equals(intent.getAction()) && this.mPicUriList != null) {
                MLog.i("RcsGroupChatComposeMessageFragment FileTrans: ", "begin create group chat ");
                List picUriList = new ArrayList();
                for (Uri uri : this.mPicUriList) {
                    Uri uri2;
                    if (RcsTransaction.isVCardFile(uri2, this.mIntent)) {
                        uri2 = RcsTransaction.handleSingleVcardData(uri2, context);
                    } else if (RcsTransaction.isVCalendarFile(this.mIntent) && RcsTransaction.isNeedToSaveFile(uri2)) {
                        uri2 = RcsTransaction.saveVCalendarAsLocalFile(uri2, context);
                    }
                    picUriList.add(uri2);
                }
                RcsGroupChatComposeMessageFragment.this.mScrollOnSend = true;
                RcsTransaction.rcsSendGroupAnyFile(context, picUriList, this.threadId, this.groupId);
                this.mPicUriList = null;
            } else if ("com.huawei.groupchat.create".equals(intent.getAction()) && this.mLocData != null) {
                MLog.i("RcsGroupChatComposeMessageFragment FileTrans: ", "begin create group chat ");
                RcsTransaction.groupSendLocation(this.groupId, this.mLocData.x, this.mLocData.y, this.mLocData.city, this.mLocData.myAddress);
                this.mLocData = null;
            }
        }
    }

    private class GroupChatMemberChangeBroadCastReceiver extends BroadcastReceiver {
        private GroupChatMemberChangeBroadCastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals("com.huawei.rcs.message.memberchanged")) {
                String groupId = intent.getStringExtra("groupId");
                if (groupId != null && groupId.equals(RcsGroupChatComposeMessageFragment.this.mGroupID) && intent.getIntExtra("count", -1) <= 1) {
                    RcsGroupChatComposeMessageFragment.mIsInvalidGroupStatus = true;
                    RcsGroupChatComposeMessageFragment.mIsGroupchatClosed = true;
                    RcsGroupChatComposeMessageFragment.this.updateAddAttachView();
                    RcsGroupChatComposeMessageFragment.this.updateSendButtonState();
                    RcsGroupChatComposeMessageFragment.this.updateTitle();
                    RcsGroupChatComposeMessageFragment.this.updateInviateGroupChatView();
                    ResEx.makeToast(RcsGroupChatComposeMessageFragment.this.getString(R.string.groupchat_closed_prompt), 0);
                }
            }
        }
    }

    private class GroupChatStatusChangeBroadCastReceiver extends BroadcastReceiver {
        private GroupChatStatusChangeBroadCastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals("com.huawei.rcs.message.groupcreated")) {
                String groupId = intent.getStringExtra("groupId");
                if (groupId != null && groupId.equals(RcsGroupChatComposeMessageFragment.this.mGroupID)) {
                    Log.d("RcsGroupChatComposeMessageFragment", "mIsInvalidGroupStatus before=" + RcsGroupChatComposeMessageFragment.mIsInvalidGroupStatus);
                    int groupStatus = RcsProfile.getRcsGroupStatus(groupId);
                    RcsGroupChatComposeMessageFragment.this.mGroupStatus = groupStatus;
                    RcsGroupChatComposeMessageFragment.mIsInvalidGroupStatus = RcseMmsExt.checkInvalidGroupStatus(groupStatus);
                    RcsGroupChatComposeMessageFragment.this.updateAddAttachView();
                    RcsGroupChatComposeMessageFragment.this.updateSendButtonState();
                    RcsGroupChatComposeMessageFragment.this.updateTitle();
                    RcsGroupChatComposeMessageFragment.this.updateInviateGroupChatView();
                    RcsGroupChatComposeMessageFragment.this.smoothScrollToEnd(true, 0);
                    Log.d("RcsGroupChatComposeMessageFragment", "mIsInvalidGroupStatus after=" + RcsGroupChatComposeMessageFragment.mIsInvalidGroupStatus);
                }
            }
        }
    }

    private class IfMsgplusCbImpl extends Stub {
        private int mEventListener = 0;

        IfMsgplusCbImpl(int event) {
            this.mEventListener = event;
            RcsGroupChatComposeMessageFragment.this.mMsgplusListeners.put(Integer.valueOf(this.mEventListener), this);
        }

        public void handleEvent(int wEvent, Bundle bundle) throws RemoteException {
            if (wEvent == this.mEventListener) {
                Message msg = RcsGroupChatComposeMessageFragment.this.mRcseEventHandler.obtainMessage(wEvent);
                msg.obj = bundle;
                RcsGroupChatComposeMessageFragment.this.mRcseEventHandler.sendMessage(msg);
            }
        }
    }

    private class MenuEx extends EmuiMenu implements OnCreateContextMenuListener, OnCustomMenuListener {
        public MenuEx() {
            super(null);
        }

        private MenuEx setOptionMenu(Menu menu) {
            this.mOptionMenu = menu;
            return this;
        }

        public boolean createOptionsMenu() {
            if (this.mOptionMenu == null || RcsGroupChatComposeMessageFragment.this.mMsgListView == null) {
                return false;
            }
            this.mOptionMenu.clear();
            return true;
        }

        public boolean onOptionsItemSelected(MenuItem item) {
            Integer[] selection = RcsGroupChatComposeMessageFragment.this.mMsgListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions();
            RcsGroupChatMessageItem rcsGroupChatMessageItem = null;
            if (selection != null && 1 == selection.length) {
                long msgId = RcsGroupChatComposeMessageFragment.this.mMsgListView.getItemIdAtPosition(selection[0].intValue());
                int position = selection[0].intValue();
                rcsGroupChatMessageItem = RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getMessageItemWithIdAssigned(position, RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getCursor());
                if (rcsGroupChatMessageItem == null) {
                    MLog.e("RcsGroupChatComposeMessageFragment", "Cannot load groupchat message item for position = " + position + ", msgId = " + msgId);
                    return true;
                }
            }
            switch (item.getItemId()) {
                case 278925313:
                    boolean isAllSelected = !RcsGroupChatComposeMessageFragment.this.mIsAllSelected;
                    RcsGroupChatComposeMessageFragment.this.mMsgListView.setAllSelected(isAllSelected);
                    RcsGroupChatComposeMessageFragment.this.mMsgListView.setAllSelectedPosition(isAllSelected);
                    RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.notifyDataSetChanged();
                    return true;
                case 278925315:
                    RcsGroupChatComposeMessageFragment.this.mMultyOperType = 0;
                    RcsGroupChatComposeMessageFragment.this.mMsgListView.onMenuItemClick(278925315);
                    return true;
                case 278925316:
                    if (!RcsGroupChatComposeMessageFragment.this.detectMessageToForwardForFt(selection, RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getCursor())) {
                        if (!RcsGroupChatComposeMessageFragment.this.detectMessageToForwardForLoc(selection, RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getCursor())) {
                            RcsGroupChatComposeMessageFragment.this.forwardMsg();
                            break;
                        }
                        RcsGroupChatComposeMessageFragment.this.forwardLoc();
                        break;
                    }
                    RcsGroupChatComposeMessageFragment.this.toForward();
                    break;
                case 278925318:
                    RcsGroupChatComposeMessageFragment.this.mMultyOperType = 1;
                    RcsGroupChatComposeMessageFragment.this.mMsgListView.onMenuItemClick(278925318);
                    break;
                case 278925319:
                    HwMessageUtils.copyToClipboard(RcsGroupChatComposeMessageFragment.this.getContext(), RcsGroupChatComposeMessageFragment.this.getSelectedGroupChatMessageBodies(RcsGroupChatComposeMessageFragment.this.mMsgListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions(), RcsGroupChatComposeMessageFragment.this.mMsgListAdapter));
                    break;
                case 278925321:
                    RcsGroupChatComposeMessageFragment.this.reSend(rcsGroupChatMessageItem);
                    break;
                case 278925326:
                    if (rcsGroupChatMessageItem != null) {
                        RcsFileTransGroupMessageItem ftGroupMsgItem = rcsGroupChatMessageItem.mFtGroupMsgItem;
                        if (ftGroupMsgItem != null) {
                            RcsGroupChatComposeMessageFragment.this.saveGroupFileToPhone(ftGroupMsgItem);
                            break;
                        }
                    }
                    return true;
                    break;
                case 278925328:
                    if (!(rcsGroupChatMessageItem == null || rcsGroupChatMessageItem.mFtGroupMsgItem == null)) {
                        rcsGroupChatMessageItem.mFtGroupMsgItem.saveVcard();
                        break;
                    }
                case 278925329:
                    if (!(rcsGroupChatMessageItem == null || rcsGroupChatMessageItem.mFtGroupMsgItem == null)) {
                        rcsGroupChatMessageItem.mFtGroupMsgItem.showVCardDetailDialog();
                        break;
                    }
                case 278925334:
                    if (rcsGroupChatMessageItem != null) {
                        RcsGroupChatComposeMessageFragment.this.showDeliveryReportForSingleMessage(rcsGroupChatMessageItem.mGlobalID, rcsGroupChatMessageItem.mDate);
                        break;
                    }
                    return true;
                case 278925343:
                    if (rcsGroupChatMessageItem != null) {
                        if (HwMessageUtils.isSplitOn()) {
                            if (((Activity) RcsGroupChatComposeMessageFragment.this.getContext()) instanceof ConversationList) {
                                MessageUtils.viewRcsMessageText(RcsGroupChatComposeMessageFragment.this.getContext(), rcsGroupChatMessageItem, ((ConversationList) RcsGroupChatComposeMessageFragment.this.getContext()).getRightFragment());
                                break;
                            }
                        }
                        MessageUtils.viewRcsMessageText(RcsGroupChatComposeMessageFragment.this.getContext(), rcsGroupChatMessageItem, null);
                        break;
                    }
                    MLog.e("RcsGroupChatComposeMessageFragment", "MENU_ID_SELECT_TEXT_COPY msgItem == null");
                    return true;
                    break;
                case 278927472:
                    if (rcsGroupChatMessageItem != null) {
                        RcsGroupChatComposeMessageFragment.this.shareMessage(rcsGroupChatMessageItem);
                        break;
                    }
                    break;
            }
            if (RcsGroupChatComposeMessageFragment.this.mMsgListView.isInEditMode()) {
                RcsGroupChatComposeMessageFragment.this.mMsgListView.exitEditMode();
            }
            return true;
        }

        public void onCreateContextMenu(ContextMenu arg0, View arg1, ContextMenuInfo arg2) {
        }

        public boolean onPrepareOptionsMenu() {
            boolean isInEditMode;
            if (RcsGroupChatComposeMessageFragment.this.mMsgListView != null) {
                isInEditMode = RcsGroupChatComposeMessageFragment.this.mMsgListView.isInEditMode();
            } else {
                isInEditMode = false;
            }
            if (!isInEditMode) {
                return false;
            }
            prepareOptionsMenuInEditMode();
            switchToEdit(true);
            RcsGroupChatComposeMessageFragment.this.mMsgListView.onMenuPrepared();
            RcsGroupChatComposeMessageFragment.this.setCustomMenuClickListener();
            return true;
        }

        public void switchToEdit(boolean editable) {
            if (!editable) {
                clear();
            }
            Integer[] selection = RcsGroupChatComposeMessageFragment.this.mMsgListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions();
            setItemEnabled(278925316, selection.length > 0);
            setItemEnabled(278925315, selection.length > 0);
            setItemEnabled(278925319, selection.length > 0);
            setItemVisible(278925343, 1 == selection.length);
            switchEditMenuPlus(selection);
            setItemEnabled(278925318, selection.length > 0);
            setItemVisible(278925321, false);
            setItemVisible(278925322, false);
            setItemVisible(278925323, false);
            setItemVisible(278925324, false);
            setItemVisible(278925325, false);
            setItemVisible(278925326, false);
            setItemVisible(278925327, false);
            setItemVisible(278925328, false);
            setItemVisible(278925329, false);
            setItemVisible(278925330, false);
            setItemVisible(278925332, false);
            setItemVisible(278925333, false);
            setItemEnabled(278925333, false);
            setItemVisible(278925334, false);
            setItemVisible(278927472, 1 == selection.length);
            if (1 == selection.length) {
                long msgId = RcsGroupChatComposeMessageFragment.this.mMsgListView.getItemIdAtPosition(selection[0].intValue());
                if (msgId <= 0) {
                    msgId = -msgId;
                }
                int position = selection[0].intValue();
                RcsGroupChatMessageItem msgItem = RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getMessageItemWithIdAssigned(position, RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getCursor());
                if (msgItem == null) {
                    Log.e("RcsGroupChatComposeMessageFragment", "Cannot load message item for position = " + position + ", msgId = " + msgId);
                    return;
                }
                if (msgItem.isOutgoingMessage() && msgItem.isFailedMessage() && RcsGroupChatComposeMessageFragment.this.mIsSmsEnabled) {
                    setItemVisible(278925321, true);
                    if ((!RcsGroupChatComposeMessageFragment.this.getLoginStatus() || RcsGroupChatComposeMessageFragment.this.getOwnerStatus()) && !RcsGroupChatComposeMessageFragment.isExitRcsGroupEnable()) {
                        setItemEnabled(278925321, true);
                    } else {
                        setItemEnabled(278925321, false);
                    }
                }
                RcsFileTransGroupMessageItem fileTransGroupMsgItem = msgItem.mFtGroupMsgItem;
                if (fileTransGroupMsgItem != null) {
                    if (!fileTransGroupMsgItem.isLocation() && !fileTransGroupMsgItem.isVCardFileTypeMsg()) {
                        setItemVisible(278925326, true);
                        String path = fileTransGroupMsgItem.mImAttachmentPath;
                        boolean isFileExist = RcsTransaction.isFileExist(path);
                        boolean isFileCanSave = (fileTransGroupMsgItem.mIsOutgoing || fileTransGroupMsgItem.mImAttachmentStatus == 1002) ? true : fileTransGroupMsgItem.mImAttachmentStatus == Place.TYPE_ROUTE;
                        MLog.i("RcsGroupChatComposeMessageFragment", "setMsgItemVisible path=" + path + " isFileExist=" + isFileExist);
                        setItemEnabled(278925326, isFileExist);
                        if (isFileExist && isFileCanSave) {
                            setItemEnabled(278925326, getItemVisible(this.mOptionMenu, 278925326));
                        } else {
                            setItemEnabled(278925326, false);
                            setItemEnabled(278925316, false);
                        }
                    } else if (fileTransGroupMsgItem.isVCardFileTypeMsg() && !RcsTransaction.isFileExist(fileTransGroupMsgItem.mImAttachmentPath)) {
                        setItemEnabled(278925316, false);
                    }
                }
                if (4 == msgItem.mType) {
                    if (msgItem.isDeliveredMessage() || msgItem.isSentMessage()) {
                        setItemVisible(278925334, true);
                    } else if (msgItem.isFailedMessage()) {
                        setItemVisible(278925334, true);
                        setItemEnabled(278925334, false);
                    }
                } else if (100 == msgItem.mType) {
                    MLog.i("RcsGroupChatComposeMessageActivity", "Group filetrans :" + msgItem.isDeliveredMessage());
                    if (msgItem.isDeliveredMessage() || msgItem.isFtSentMessage()) {
                        setItemVisible(278925334, true);
                        setItemEnabled(278925334, true);
                    }
                }
                if (RcsProfileUtils.getRcsMsgExtType(msgItem.mCursor) == 6) {
                    setItemVisible(278927472, false);
                    setItemEnabled(278925316, true);
                }
                setVcardItemVisible(msgItem);
            }
        }

        private void setVcardItemVisible(RcsGroupChatMessageItem msgItem) {
            if (msgItem.mFtGroupMsgItem != null && msgItem.mFtGroupMsgItem.isVCardFileTypeMsg()) {
                if (msgItem.mFtGroupMsgItem.mIsOutgoing) {
                    setItemVisible(278925329, true);
                } else if (msgItem.mFtGroupMsgItem.mImAttachmentStatus == 1002 || msgItem.mFtGroupMsgItem.mImAttachmentStatus == Place.TYPE_ROUTE) {
                    setItemVisible(278925328, true);
                }
            }
        }

        private void switchEditMenuPlus(Integer[] selection) {
            Cursor cursor = RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getCursor();
            boolean isAfterLast = (cursor == null || cursor.isClosed() || cursor.isBeforeFirst()) ? true : cursor.isAfterLast();
            if (!isAfterLast && selection != null && selection.length > 0) {
                boolean hasLocItem = false;
                if (6 == ((long) RcsProfileUtils.getRcsMsgExtType(cursor))) {
                    hasLocItem = true;
                }
                boolean hasFtItem = false;
                boolean hasWithoutFtItem = false;
                int prePosition = cursor.getPosition();
                for (Integer intValue : selection) {
                    cursor.moveToPosition(intValue.intValue());
                    int rcsMsgType = RcsProfileUtils.getGroupChatRcsMsgType(cursor, RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.getGroupMessageColumn());
                    if (rcsMsgType == 100 || rcsMsgType == 101 || hasLocItem) {
                        hasFtItem = true;
                    } else {
                        hasWithoutFtItem = true;
                    }
                    if (hasFtItem && hasWithoutFtItem) {
                        break;
                    }
                }
                cursor.moveToPosition(prePosition);
                isAfterLast = selection.length > 0 && !hasFtItem;
                setItemEnabled(278925319, isAfterLast);
                isAfterLast = selection.length == 1 && !hasFtItem;
                setItemEnabled(278925343, isAfterLast);
                MenuEx -get37 = RcsGroupChatComposeMessageFragment.this.mMenuEx;
                isAfterLast = (1 >= selection.length || !hasFtItem) ? getItemVisible(this.mOptionMenu, 278925316) : false;
                -get37.setItemEnabled(278925316, isAfterLast);
            }
        }

        public boolean getItemVisible(Menu optionMenu, int itemID) {
            if (optionMenu == null) {
                return false;
            }
            MenuItem menuItem = optionMenu.findItem(itemID);
            if (menuItem != null) {
                return menuItem.isEnabled();
            }
            return false;
        }

        private boolean prepareOptionsMenuInEditMode() {
            resetOptionMenu(RcsGroupChatComposeMessageFragment.this.getResetMenu());
            boolean inLandscape = RcsGroupChatComposeMessageFragment.this.isInLandscape();
            clear();
            addMenuDelete(inLandscape);
            addMenuForawrd(inLandscape);
            addMenuFavorite(inLandscape);
            addMenuChoice(inLandscape);
            addOverflowMenu(278925319, R.string.button_copy_text);
            addOverflowMenu(278927472, R.string.button_share);
            addOverflowMenu(278925322, R.string.menu_edit);
            addOverflowMenu(278925321, R.string.resend);
            addOverflowMenu(278925325, R.string.menu_copy_to_sim);
            addOverflowMenu(278925323, R.string.menu_copy_to_sim1);
            addOverflowMenu(278925324, R.string.menu_copy_to_sim2);
            addOverflowMenu(278925343, R.string.mms_select_text_copy);
            addOverflowMenu(278925332, R.string.menu_unlock);
            addOverflowMenu(278925327, R.string.save_ringtone);
            addOverflowMenu(278925328, R.string.save_to_contacts);
            addOverflowMenu(278925329, R.string.view_vcard_details);
            addOverflowMenu(278925330, R.string.menu_save_to_calendar);
            addOverflowMenu(278925326, R.string.copy_to_sdcard);
            addOverflowMenu(278925333, R.string.view_message_details);
            addOverflowMenu(278925334, R.string.view_delivery_report);
            RcsGroupChatComposeMessageFragment.this.refreshMenu();
            return true;
        }

        public boolean onCustomMenuItemClick(MenuItem aMenuItem) {
            return onOptionsItemSelected(aMenuItem);
        }
    }

    public interface RcsGroupChatMapClickCallback {
        void okClick();
    }

    public class RcsGroupConversationInputHostHolder implements ConversationInputHost, OnAttachmentClickListener {
        public MediaPicker createMediaPicker() {
            return new MediaPicker(RcsGroupChatComposeMessageFragment.this.getActivity());
        }

        public void onMediaItemsSelected(Collection<AttachmentSelectData> attachmentItems) {
            if (attachmentItems != null) {
                for (AttachmentSelectData data : attachmentItems) {
                    setAttachmentSelectData(data);
                }
            }
        }

        private void setAttachmentSelectData(AttachmentSelectData attachment) {
            if (attachment == null) {
                Log.d("RcsGroupChatComposeMessageFragment", "setAttachmentSelectData failed, params is null.");
                return;
            }
            Uri attachmentUri = attachment.getAttachmentUri();
            if (attachmentUri != null) {
                MmsApp.getApplication().removeThumbnail(attachment.getAttachmentUri());
            }
            switch (attachment.getAttachmentType()) {
                case 2:
                case AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS /*1201*/:
                    RcsGroupChatComposeMessageFragment.this.mRichEditor.setNewAttachment(attachmentUri, 2);
                    break;
                case 3:
                    RcsGroupChatComposeMessageFragment.this.mRichEditor.setNewAttachment(attachmentUri, 3);
                    break;
                case 5:
                case 1211:
                    RcsGroupChatComposeMessageFragment.this.mRichEditor.setNewAttachment(attachmentUri, 5);
                    break;
                case 8:
                    if (attachment instanceof AttachmentSelectLocation) {
                        RcsGroupChatComposeMessageFragment.this.setAttachmentLocation((AttachmentSelectLocation) attachment);
                    }
                    RcsGroupChatComposeMessageFragment.this.mRichEditor.setNewAttachment(attachmentUri, 8);
                    break;
            }
        }

        public void onMediaItemsUnselected(AttachmentSelectData attachmentItem) {
            if (attachmentItem != null) {
                switch (attachmentItem.getAttachmentType()) {
                    case 2:
                    case 5:
                    case 8:
                        if (attachmentItem.getAttachmentUri() != null) {
                            RcsGroupChatComposeMessageFragment.this.mRichEditor.removeData(attachmentItem.getAttachmentUri(), attachmentItem.getAttachmentType());
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        }

        public void resumeComposeMessage() {
            RcsGroupChatComposeMessageFragment.this.mRichEditor.requestFocus();
            if (RcsGroupChatComposeMessageFragment.this.mIsLandscape) {
                RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.showHideMediaPicker(false, true);
                return;
            }
            RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.showHideMediaPicker(false, false);
            RcsGroupChatComposeMessageFragment.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    RcsGroupChatComposeMessageFragment.this.showKeyboard();
                }
            }, 200);
        }

        public void onMediaFullScreenChanged(boolean fullscreen, int type) {
            switch (type) {
                case 101:
                    RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.show(!fullscreen);
                    return;
                case 102:
                    if (fullscreen) {
                        enterGalleryFullUpdate();
                        return;
                    } else {
                        exitGalleryFullUpdate();
                        return;
                    }
                case LocationRequest.PRIORITY_LOW_POWER /*104*/:
                    if (fullscreen) {
                        enterLocationFullUpdate();
                        return;
                    } else {
                        exitLocationFullUpdate();
                        return;
                    }
                default:
                    if (fullscreen) {
                        enterTitleModeUpdate();
                        return;
                    } else {
                        exitTitleMode();
                        return;
                    }
            }
        }

        protected void enterLocationFullUpdate() {
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.show(true);
            RcsMapFragment rcsMapFragment = (RcsMapFragment) FragmentTag.getFragmentByTag(RcsGroupChatComposeMessageFragment.this.getActivity(), "MMS_UI_MAP");
            if (rcsMapFragment != null) {
                rcsMapFragment.setActionbar(RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit);
                RcsGroupChatComposeMessageFragment.this.setMapClickCallback(rcsMapFragment);
            }
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.enterSelectModeState();
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setStartIcon(true, (int) R.drawable.ic_public_cancel, new OnClickListener() {
                public void onClick(View arg0) {
                    if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager != null && RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.getMediaPickerFullScreenState()) {
                        RcsGroupConversationInputHostHolder.this.exitMediaPcikerFullScreen();
                    }
                    RcsGroupConversationInputHostHolder.this.exitLocationFullUpdate();
                }
            });
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setEndIcon(true, (int) R.drawable.ic_public_ok, new OnClickListener() {
                public void onClick(View arg0) {
                    if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager != null && RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.getMediaPickerFullScreenState()) {
                        if (RcsGroupChatComposeMessageFragment.this.mapClickCallback != null) {
                            RcsGroupChatComposeMessageFragment.this.mapClickCallback.okClick();
                        }
                        RcsGroupConversationInputHostHolder.this.exitMediaPcikerFullScreen();
                    }
                    RcsGroupConversationInputHostHolder.this.exitLocationFullUpdate();
                }
            });
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setEndIcon(false);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.showMenu(false);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setSubtitle(null);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setTitleGravityCenter();
            if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager != null) {
                RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.updateActionBar(RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit);
            }
        }

        protected void exitLocationFullUpdate() {
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.exitSelectMode();
            RcsGroupChatComposeMessageFragment.this.mSplitActionBar.setVisibility(8);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.showMenu(true);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setTitleGravityCenter(false);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.showEndIcon(false);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.showStartIcon(false);
            RcsMapFragment rcsMapFragment = (RcsMapFragment) FragmentTag.getFragmentByTag(RcsGroupChatComposeMessageFragment.this.getActivity(), "MMS_UI_MAP");
            if (rcsMapFragment != null) {
                rcsMapFragment.setActionbar(null);
                RcsGroupChatComposeMessageFragment.this.setMapClickCallback(null);
            }
            RcsGroupChatComposeMessageFragment.this.updateTitle();
        }

        private void exitTitleMode() {
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.exitTitleMode();
            RcsGroupChatComposeMessageFragment.this.mSplitActionBar.setVisibility(8);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setTitleGravityCenter(false);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.showEndIcon(false);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.showStartIcon(false);
            RcsGroupChatComposeMessageFragment.this.updateTitle();
        }

        private void enterTitleModeUpdate() {
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.show(true);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.enterTitleMode();
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
                public void onClick(View arg0) {
                    if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager != null && RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.getMediaPickerFullScreenState()) {
                        RcsGroupConversationInputHostHolder.this.exitMediaPcikerFullScreen();
                    }
                    RcsGroupConversationInputHostHolder.this.exitTitleMode();
                }
            });
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.showEndIcon(false);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setSubtitle(null);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setTitleGravityCenter();
            if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager != null) {
                RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.updateActionBar(RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit);
            }
        }

        private void exitGalleryFullUpdate() {
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.show(true);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.exitSelectMode();
            RcsGroupChatComposeMessageFragment.this.mSplitActionBar.setVisibility(8);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setTitleGravityCenter(false);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.showEndIcon(false);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.showStartIcon(false);
            RcsGroupChatComposeMessageFragment.this.updateTitle();
        }

        private void exitMediaPcikerFullScreen() {
            if (RcsGroupChatComposeMessageFragment.this.getResources().getConfiguration().orientation == 2) {
                RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.showHideMediaPicker(false, true);
            } else {
                RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.showMediaPicker(false, true);
            }
        }

        private void enterGalleryFullUpdate() {
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.show(true);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.enterSelectModeState();
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
                public void onClick(View arg0) {
                    if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager != null && RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.getMediaPickerFullScreenState()) {
                        RcsGroupConversationInputHostHolder.this.exitMediaPcikerFullScreen();
                    }
                    RcsGroupConversationInputHostHolder.this.exitGalleryFullUpdate();
                }
            });
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setEndIcon(true, (int) R.drawable.ic_public_ok, new OnClickListener() {
                public void onClick(View arg0) {
                    if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager != null && RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.getMediaPickerFullScreenState()) {
                        RcsGroupConversationInputHostHolder.this.exitMediaPcikerFullScreen();
                    }
                    RcsGroupConversationInputHostHolder.this.exitGalleryFullUpdate();
                }
            });
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setSubtitle(null);
            RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.setTitleGravityCenter();
            if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager != null) {
                RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.updateActionBar(RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit);
            }
        }

        public void onPendingOperate(int type, AttachmentSelectData attachmentItem) {
            switch (type) {
                case 1001:
                    RcsGroupChatComposeMessageFragment.this.startActivityForResult(MessageUtils.getSelectImageIntent(RcsGroupChatComposeMessageFragment.this.getActivity(), 5), 144);
                    return;
                case 1002:
                    int i;
                    RcsGroupChatComposeMessageFragment rcsGroupChatComposeMessageFragment = RcsGroupChatComposeMessageFragment.this;
                    Activity activity = RcsGroupChatComposeMessageFragment.this.getActivity();
                    if (attachmentItem == null) {
                        i = 0;
                    } else {
                        i = attachmentItem.getPosition();
                    }
                    rcsGroupChatComposeMessageFragment.startActivity(MessageUtils.getGalleryCompressIntent(activity, i, true, true));
                    if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
                        RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.showMediaPicker(false, false);
                        RcsGroupChatComposeMessageFragment.this.mIsAttachmentShow = false;
                        return;
                    }
                    return;
                case 1003:
                    RcsGroupChatComposeMessageFragment.this.addAttachment(11, false);
                    return;
                case 1004:
                    RcsGroupChatComposeMessageFragment.this.addAttachment(10, false);
                    return;
                case 1005:
                    RcsGroupChatComposeMessageFragment.this.addAttachment(7, false);
                    return;
                case 1006:
                    RcsGroupChatComposeMessageFragment.this.addAttachment(6, false);
                    return;
                case 1009:
                    RcsGroupChatComposeMessageFragment.this.addAttachment(23, false);
                    return;
                default:
                    return;
            }
        }

        public boolean onAttachmentClick(SlideModel attachment, int viewType) {
            return false;
        }

        public boolean onRcsAttachmentClick(MediaModel attachment, int viewType) {
            String fileName = attachment.getSourceBuild();
            File tempfile = new File(fileName);
            if (RcsGroupChatComposeMessageFragment.this.mRichEditor == null) {
                return false;
            }
            String contentType;
            switch (viewType) {
                case 2:
                    if (attachment instanceof ImageModel) {
                        ImageModel image = (ImageModel) attachment;
                        contentType = RcsMediaFileUtils.getFileMimeType(fileName);
                        RcsGroupChatComposeMessageFragment.this.mRichEditor.viewAttach(image, RcsProfileUtils.getImageContentUri(RcsGroupChatComposeMessageFragment.this.getContext(), tempfile), contentType, fileName);
                        break;
                    }
                    break;
                case 3:
                    if (attachment instanceof AudioModel) {
                        AudioModel audio = (AudioModel) attachment;
                        contentType = RcsMediaFileUtils.getFileMimeType(fileName);
                        RcsGroupChatComposeMessageFragment.this.mRichEditor.viewAttach(audio, RcsProfileUtils.getFileContentUri(RcsGroupChatComposeMessageFragment.this.getContext(), tempfile), contentType, fileName);
                        break;
                    }
                    break;
                case 5:
                    if (attachment instanceof VideoModel) {
                        VideoModel video = (VideoModel) attachment;
                        contentType = RcsMediaFileUtils.getFileMimeType(fileName);
                        RcsGroupChatComposeMessageFragment.this.mRichEditor.viewAttach(video, RcsProfileUtils.getVideoContentUri(RcsGroupChatComposeMessageFragment.this.getContext(), tempfile), contentType, fileName);
                        break;
                    }
                    break;
                case 6:
                    if (attachment instanceof VcardModel) {
                        MediaListItem.viewVcardDetail((VcardModel) attachment, RcsGroupChatComposeMessageFragment.this.getContext());
                        break;
                    }
                    break;
                case 8:
                    MediaModelControl.viewLocationMediaModel(RcsGroupChatComposeMessageFragment.this.getContext(), attachment);
                    break;
            }
            return false;
        }

        public void deleteRcsAttachmentView(MediaModel attachment, int type) {
            if (attachment != null && RcsGroupChatComposeMessageFragment.this.mRichEditor != null) {
                RcsGroupChatComposeMessageFragment.this.mRichEditor.removeData(attachment.getUri(), type);
            }
        }

        public void onChooserSlected(MediaChooser mediaChooser) {
            if (RcsGroupChatComposeMessageFragment.this.isInLandscape() && mediaChooser != null) {
                if (mediaChooser instanceof CameraMediaChooser) {
                    if (RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.isInTitleModel()) {
                        exitTitleMode();
                    } else if (RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit.isInSelectModel()) {
                        exitGalleryFullUpdate();
                    }
                } else if (mediaChooser instanceof GalleryMediaChooser) {
                    enterGalleryFullUpdate();
                } else if (mediaChooser instanceof MapMediaChooser) {
                    enterLocationFullUpdate();
                } else {
                    enterTitleModeUpdate();
                }
            }
        }

        public void deleteAttachmentView(SlideModel slideModel, int type) {
        }

        public boolean isShowSlide() {
            return false;
        }

        public int getSlideCounts() {
            return 0;
        }

        public void updateStateLoaded() {
            RcsGroupChatComposeMessageFragment.this.mHandler.post(new Runnable() {
                public void run() {
                    if (RcsGroupChatComposeMessageFragment.this.mAttachmentPreview != null) {
                        RcsGroupChatComposeMessageFragment.this.mAttachmentPreview.setMultiAttachmentScrollState(true);
                        RcsGroupChatComposeMessageFragment.this.mAttachmentPreview.refreshAttachmentScroll();
                    }
                }
            });
        }

        public void invalidateActionBar() {
            if (RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager != null && RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit != null) {
                RcsGroupChatComposeMessageFragment.this.mRcsGroupConversationInputManager.updateActionBar(RcsGroupChatComposeMessageFragment.this.mActionBarWhenSplit);
            }
        }
    }

    private class RcsLoginStatusChangeBroadCastReceiver extends BroadcastReceiver {
        private RcsLoginStatusChangeBroadCastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getExtras() != null) {
                int newStatus = intent.getExtras().getInt("new_status");
                MLog.d("RcsGroupChatComposeMessageFragment", "newStatus = " + newStatus);
                if (newStatus == 1) {
                    RcsProfile.insertGroupOwner(RcsGroupChatComposeMessageFragment.this.mGroupID, RcsGroupChatComposeMessageFragment.this.mGroupStatus);
                }
            }
        }
    }

    public void addMediaUpdateListener(RcsGroupMediaUpdateListener rcsGroupMediaupdatelistener) {
        if (rcsGroupMediaupdatelistener != null) {
            this.mRcsGroupMediaUpdateListeners.add(rcsGroupMediaupdatelistener);
        }
    }

    public RcsGroupChatRichMessageEditor getRichEditor() {
        return this.mRichEditor;
    }

    public void removeMediaUpdateListener(RcsGroupMediaUpdateListener rcsGroupMediaupdatelistener) {
        if (rcsGroupMediaupdatelistener != null) {
            this.mRcsGroupMediaUpdateListeners.remove(rcsGroupMediaupdatelistener);
        }
    }

    private void doMediaUpdate(int changedType) {
        if (this.mRcsGroupMediaUpdateListeners != null) {
            for (RcsGroupMediaUpdateListener listener : this.mRcsGroupMediaUpdateListeners) {
                if (listener.hasUpdateMedida(changedType)) {
                    listener.updateMedia(this.mRichEditor);
                }
            }
            if (!(this.mRcsGroupConversationInputManager == null || this.mActionBarWhenSplit == null || (changedType != 2 && changedType != 5))) {
                this.mRcsGroupConversationInputManager.updateActionBar(this.mActionBarWhenSplit);
            }
        }
    }

    private void editMessageItem(RcsGroupChatMessageItem msgItem) {
        if (msgItem.getMessageType().equals("rcs_group_text")) {
            editGroupTextMessageItem(msgItem);
        } else if (msgItem.getMessageType().equals("rcs_group_file")) {
            editGroupFileMessageItem(msgItem);
        }
    }

    private void editGroupFileMessageItem(RcsGroupChatMessageItem msgItem) {
        if (msgItem != null && msgItem.getCancelId() != 0) {
            ContentValues values = new ContentValues();
            values.put("status", Integer.valueOf(4));
            SqliteWrapper.update(getContext(), sMessageUri, values, "_id = ?", new String[]{msgItem.mMsgId});
            values.clear();
            values.put("transfer_status", Integer.valueOf(1010));
            SqliteWrapper.update(getContext(), RcsAttachments.CONTENT_URI, values, "msg_id = ? and chat_type = 2", new String[]{msgItem.mMsgId});
        }
    }

    private void editGroupTextMessageItem(RcsGroupChatMessageItem msgItem) {
        if (msgItem != null && msgItem.getCancelId() != 0) {
            this.mBackgroundQueryHandler.startDelete(9700, null, sMessageUri.buildUpon().appendPath(String.valueOf(msgItem.getCancelId())).build(), null, null);
            this.mRichEditor.setText(msgItem.mBody);
        }
    }

    public void setIsRecordVideo(boolean isRecordVideo) {
        this.mIsRecordVideo = isRecordVideo;
    }

    public boolean isRecordVideo() {
        return this.mIsRecordVideo;
    }

    public long getmThreadID() {
        return this.mThreadID;
    }

    public String getGroupID() {
        return this.mGroupID;
    }

    private static void log(String logMsg) {
        Thread current = Thread.currentThread();
        long tid = current.getId();
        Log.d("RcsGroupChatComposeMessageFragment", "[" + tid + "] [" + current.getStackTrace()[3].getMethodName() + "] " + logMsg);
    }

    public void onCreate(Bundle savedInstanceState) {
        Log.d("RcsGroupChatComposeMessageFragment", "onCreate");
        super.onCreate(savedInstanceState);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            if (this.mForwarder == null) {
                this.mForwarder = new RcsFileTransMessageForwarder();
            }
            if (this.mChatForwarder == null) {
                this.mChatForwarder = new RcsChatMessageForwarder();
            }
        }
        this.mRcsGroupMediaUpdateListeners = new ArrayList();
        getContext().registerReceiver(this.mHomeKeyEventReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        initComposeHandler();
        if (RcsProfile.getRcsService() != null && RcsProfile.isRcsImServiceSwitchEnabled()) {
            RcsProfile.registerRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS), new IfMsgplusCbImpl(AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS));
            RcsProfile.registerRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST), new IfMsgplusCbImpl(AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST));
        }
        registerDefSmsAppChanged();
        resetConfiguration(getResources().getConfiguration());
        this.mRcsAudioMessage = new RcsAudioMessage(this);
        if (this.mRcsGroupPickAudio != null) {
            this.mRcsAudioMessage.setPickAudioButton(this.mRcsGroupPickAudio);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.rcs_groupchat_compose_message_activity, container, false);
        this.mActionBarWhenSplit = createEmuiActionBar(this.mRootView);
        this.mSplitActionBar = (SplitActionBarView) this.mRootView.findViewById(R.id.rcs_groupchat_message_bottom);
        return this.mRootView;
    }

    protected AbstractEmuiActionBar createEmuiActionBar(View fragmentRootView) {
        return new MmsEmuiActionBar(getActivity(), fragmentRootView.findViewById(R.id.rcs_groupchat_message_top), null);
    }

    public AbstractEmuiActionBar getGroupActionBar() {
        return this.mActionBarWhenSplit;
    }

    public AttachmentSelectLocation getAttachmentLocation() {
        return this.attSelectLocation;
    }

    public void setAttachmentLocation(AttachmentSelectLocation attachmentLocation) {
        this.attSelectLocation = attachmentLocation;
    }

    public void setMapClickCallback(RcsGroupChatMapClickCallback callback) {
        this.mapClickCallback = callback;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mRcsGroupConversationInputManager = new ConversationInputManager(getActivity(), getActivity().getFragmentManager(), this.mRcsGroupConversationInputHostHolder);
        getActivity().setProgressBarVisibility(false);
        this.mContentResolver = getContext().getContentResolver();
        this.mBackgroundQueryHandler = new BackgroundQueryHandler(this.mContentResolver);
        Intent intent = getIntent();
        if (initGroupInfo(intent)) {
            RcsMessagingNotification.clearGroupCreateNotificationByGroupId(getContext(), this.mGroupID);
            registerGroupChatStatusChangeBroadCast();
            registerGroupChatMemberChangeBroadCast();
            this.mPicUriList = intent.getParcelableArrayListExtra("bundle_pic_uri");
            if (this.mPicUriList != null) {
                MLog.i("RcsGroupChatComposeMessageFragment FileTrans: ", "GroupChatCreateBroadCastReceiver and sendfile");
                RcsUtility.delExcLimitUri(getContext(), this.mPicUriList);
                this.groupChatCreateReceiver = new GroupChatCreateBroadCastReceiver(this.mPicUriList, intent, this.mThreadID, this.mGroupID);
                getContext().registerReceiver(this.groupChatCreateReceiver, new IntentFilter("com.huawei.groupchat.create"), "com.huawei.rcs.RCS_BROADCASTER", null);
            }
            this.locData = (LocationData) intent.getSerializableExtra("bundle_loc_data");
            if (this.locData != null) {
                MLog.i("RcsGroupChatComposeMessageFragment FileTrans: ", "GroupChatCreateBroadCastReceiver and send location");
                this.groupChatCreateReceiver = new GroupChatCreateBroadCastReceiver(this.locData, intent, this.mGroupID);
                getContext().registerReceiver(this.groupChatCreateReceiver, new IntentFilter("com.huawei.groupchat.create"), "com.huawei.rcs.RCS_BROADCASTER", null);
                ResEx.makeToast((int) R.string.ft_send_wait_create_group, 1000);
            }
            initResourceRefs();
            initialize(savedInstanceState, 0);
            this.mAttachmentPreview = (AttachmentPreview) this.mRootView.findViewById(R.id.attachment_draft_view);
            this.mAttachmentPreview.setRcsGroupChatRichMessageEditor(this.mRichEditor, this.mRcsGroupConversationInputHostHolder);
            this.mAttachmentPreview.setLanuchMeasureChild(true);
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if ("text/x-vCalendar".equalsIgnoreCase(intent.getStringExtra("mimeType"))) {
                    if (OsUtil.hasCalendarPermission()) {
                        ArrayList<Uri> uriCalendarList = bundle.getParcelableArrayList("hw_eventsurl_list");
                        if (uriCalendarList != null) {
                            this.mRichEditor.insertVcalendarText(uriCalendarList);
                        }
                    } else {
                        MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
                        OsUtil.requestPermission(getActivity(), new String[]{"android.permission.READ_CALENDAR"}, 157);
                    }
                }
            }
            if (mIsGroupchatClosed) {
                ResEx.makeToast(getString(R.string.groupchat_closed_prompt), 0);
            }
            if (mIsInvalidGroupStatus) {
                updateInviateGroupChatView();
            }
            this.mMenuEx = new MenuEx();
            this.mMenuEx.setContext(getContext());
            ((HwBaseActivity) getActivity()).setSupportScale(new SacleListener() {
                public void onScaleChanged(float scaleSize) {
                    if (RcsGroupChatComposeMessageFragment.this.mMsgListAdapter != null) {
                        RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.onScaleChanged(scaleSize);
                    }
                }
            });
            return;
        }
        finishSelf(false);
    }

    public void setSupportScale() {
        ((HwBaseActivity) getActivity()).setSupportScale(new SacleListener() {
            public void onScaleChanged(float scaleSize) {
                if (RcsGroupChatComposeMessageFragment.this.mMsgListAdapter != null) {
                    RcsGroupChatComposeMessageFragment.this.mMsgListAdapter.onScaleChanged(scaleSize);
                }
            }
        });
    }

    public void removeSupportScale() {
        ((HwBaseActivity) getActivity()).removeSupportScale();
    }

    private void initLocalBroadcast() {
        if (this.mLocalBroadcastManager == null) {
            this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        }
        if (this.mSaveDraftReceiver == null) {
            this.mSaveDraftReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent != null && "com.huawei.mms.saveDraft".equals(intent.getAction()) && intent.getBooleanExtra("is_groupchat", false)) {
                        Bundle bundle = intent.getExtras();
                        if (bundle != null) {
                            String smsData = bundle.getString("full_screen_data");
                            if (smsData != null) {
                                RcsGroupChatComposeMessageFragment.this.mRichEditor.setText(smsData);
                                RcsGroupChatComposeMessageFragment.this.saveDraft(true);
                            }
                        }
                    }
                }
            };
        }
        this.mLocalBroadcastManager.registerReceiver(this.mSaveDraftReceiver, new IntentFilter("com.huawei.mms.saveDraft"));
    }

    private void initialize(Bundle savedInstanceState, long originalThreadId) {
        initMessageList();
        if (!this.isPeeking) {
            this.mShouldLoadDraft = true;
        }
        getActivity().invalidateOptionsMenu();
        drawTopPanel(false);
        if (!this.mShouldLoadDraft) {
            drawBottomPanel();
        }
        this.mRichEditor.setEditTextFocus();
        if (getIntent().hasExtra("bundle_ext_msg")) {
            this.mRichEditor.setText(getIntent().getStringExtra("bundle_ext_msg"));
            this.mShouldLoadDraft = false;
        }
        RichMessageManager.get().putRcsGroupChatRichMessageEditor(getActivity(), this.mRichEditor);
    }

    public void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra("needRecreate", false)) {
            startActivity(intent);
            getActivity().finish();
        }
        setIntent(intent);
        if (this.mIsSmsEnabled && this.mMsgListView.isInEditMode()) {
            this.mMsgListView.exitEditMode();
        }
        if (intent.getBooleanExtra("vcard", false)) {
            drawTopPanel(false);
            return;
        }
        saveDraft(false);
        if (initGroupInfo(intent)) {
            initialize(null, 0);
            this.mMessagesAndDraftLoaded = false;
            loadMessagesAndDraft(0);
            appendSignature(true);
            this.mPicUriList = intent.getParcelableArrayListExtra("bundle_pic_uri");
            if (this.mPicUriList != null) {
                MLog.i("RcsGroupChatComposeMessageFragment FileTrans: ", "GroupChatCreateBroadCastReceiver and sendfile");
                RcsUtility.delExcLimitUri(getContext(), this.mPicUriList);
                this.groupChatCreateReceiver = new GroupChatCreateBroadCastReceiver(this.mPicUriList, intent, this.mThreadID, this.mGroupID);
                getContext().registerReceiver(this.groupChatCreateReceiver, new IntentFilter("com.huawei.groupchat.create"), "com.huawei.rcs.RCS_BROADCASTER", null);
                ResEx.makeToast((int) R.string.ft_send_wait_create_group, 1000);
            }
            this.locData = (LocationData) intent.getSerializableExtra("bundle_loc_data");
            if (this.locData != null) {
                MLog.i("RcsGroupChatComposeMessageFragment FileTrans: ", "GroupChatCreateBroadCastReceiver and send location");
                this.groupChatCreateReceiver = new GroupChatCreateBroadCastReceiver(this.locData, intent, this.mGroupID);
                getContext().registerReceiver(this.groupChatCreateReceiver, new IntentFilter("com.huawei.groupchat.create"), "com.huawei.rcs.RCS_BROADCASTER", null);
                ResEx.makeToast((int) R.string.ft_send_wait_create_group, 1000);
            }
            cancelVideoCompressIfRunning();
            return;
        }
        finishSelf(false);
    }

    public void onStart() {
        super.onStart();
        Log.d("RcsGroupChatComposeMessageFragment", "onStart ");
        updateGroupStatus();
        if (RcsProfile.getRcsService() != null && RcsProfile.isRcsImServiceSwitchEnabled()) {
            RcsProfile.registerRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT), new IfMsgplusCbImpl(AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT));
            RcsProfile.registerRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR), new IfMsgplusCbImpl(AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR));
        }
        boolean isSmsEnabled = MmsConfig.isSmsEnabled(getContext());
        if (isSmsEnabled != this.mIsSmsEnabled) {
            this.mIsSmsEnabled = isSmsEnabled;
            getActivity().invalidateOptionsMenu();
        }
        initFocus();
        updateInputMode();
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                RcsGroupChatComposeMessageFragment.this.loadMessagesAndDraft(2);
            }
        }, 0);
        if (!(this.mIsSmsEnabled || this.mMsgListView == null || !this.mMsgListView.isInEditMode())) {
            MLog.v("RcsGroupChatComposeMessageFragment", "onStart:: it is not default sms app, exit multi choice mode");
            this.mMsgListView.exitEditMode();
        }
        initLocalBroadcast();
        registerRcsLoginStatusChangeBroadCast();
        RcsProfile.insertGroupOwner(this.mGroupID, this.mGroupStatus);
        if (this.isPeeking) {
            this.mBottomPanel.setVisibility(8);
            this.mMsgListView.setVerticalScrollBarEnabled(false);
        }
    }

    private boolean initGroupInfo(Intent intent) {
        HwBaseFragment fragment;
        this.mGroupID = intent.getStringExtra("bundle_group_id");
        this.mGlobalGroupID = intent.getStringExtra("globalgroupId");
        this.mThreadID = intent.getLongExtra("bundle_group_thread_id", 0);
        boolean isNewGroupChat = intent.getBooleanExtra("is_new_group_chat", false);
        if (TextUtils.isEmpty(this.mGroupID) && TextUtils.isEmpty(this.mGlobalGroupID) && this.mThreadID == 0) {
            log("initGroupInfo global group id and group id both empty,thread id is 0");
            return false;
        }
        Cursor cursor = null;
        try {
            if (this.mGroupID != null) {
                cursor = SqliteWrapper.query(getContext(), sGroupUri, null, "name = ?", new String[]{this.mGroupID}, null);
            } else if (this.mGlobalGroupID != null) {
                cursor = SqliteWrapper.query(getContext(), sGroupUri, null, "global_group_id = ?", new String[]{this.mGlobalGroupID}, null);
            } else {
                cursor = SqliteWrapper.query(getContext(), sGroupUri, null, "thread_id = ?", new String[]{String.valueOf(this.mThreadID)}, null);
            }
            if (cursor != null) {
                int tid = cursor.getColumnIndexOrThrow("thread_id");
                int gid = cursor.getColumnIndexOrThrow("name");
                int name = cursor.getColumnIndexOrThrow("subject");
                int status = cursor.getColumnIndexOrThrow("status");
                if (cursor.moveToFirst()) {
                    this.mThreadID = (long) cursor.getInt(tid);
                    this.mGroupID = cursor.getString(gid);
                    this.mGroupName = cursor.getString(name);
                    this.mGroupStatus = cursor.getInt(status);
                    mIsInvalidGroupStatus = RcseMmsExt.checkInvalidGroupStatus(cursor.getInt(status));
                    mIsGroupchatClosed = 33 == cursor.getInt(status);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            cursor = SqliteWrapper.query(getContext(), sConversationUri, new String[]{"_id"}, "recipient_ids = ?", new String[]{this.mGroupID}, null);
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow("_id");
                if (cursor.moveToFirst()) {
                    this.mRcsThreadId = cursor.getLong(idColumn);
                    MLog.d("RcsGroupChatComposeMessageFragment", "GroupInfo mRcsThreadId = " + this.mRcsThreadId);
                }
            }
            if (cursor != null) {
                cursor.close();
                if ((getActivity() instanceof ConversationList) && isNewGroupChat) {
                    fragment = ((ConversationList) getActivity()).getFragment();
                    if (fragment instanceof LeftPaneConversationListFragment) {
                        ((LeftPaneConversationListFragment) fragment).setSelectedThread(this.mRcsThreadId);
                    }
                }
            }
        } catch (RuntimeException e) {
            MLog.e("RcsGroupChatComposeMessageFragment", "cursor unknowable error");
            if (cursor != null) {
                cursor.close();
                if ((getActivity() instanceof ConversationList) && isNewGroupChat) {
                    fragment = ((ConversationList) getActivity()).getFragment();
                    if (fragment instanceof LeftPaneConversationListFragment) {
                        ((LeftPaneConversationListFragment) fragment).setSelectedThread(this.mRcsThreadId);
                    }
                }
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (cursor != null) {
                cursor.close();
                if ((getActivity() instanceof ConversationList) && isNewGroupChat) {
                    fragment = ((ConversationList) getActivity()).getFragment();
                    if (fragment instanceof LeftPaneConversationListFragment) {
                        ((LeftPaneConversationListFragment) fragment).setSelectedThread(this.mRcsThreadId);
                    }
                }
            }
        }
        if (TextUtils.isEmpty(this.mGroupID)) {
            log("initGroupInfo group id is empty");
            return false;
        }
        log("initGroupInfo thread_id = " + this.mThreadID);
        return true;
    }

    public void updateGroupStatus() {
        this.mBackgroundQueryHandler.cancelOperation(9529);
        try {
            if (this.mGroupID != null) {
                this.mBackgroundQueryHandler.startQuery(9529, null, sGroupUri, new String[]{"status"}, "name = ?", new String[]{this.mGroupID}, null);
            } else if (this.mGlobalGroupID != null) {
                this.mBackgroundQueryHandler.startQuery(9529, null, sGroupUri, new String[]{"status"}, "global_group_id = ?", new String[]{this.mGlobalGroupID}, null);
            } else {
                this.mBackgroundQueryHandler.startQuery(9529, null, sGroupUri, new String[]{"status"}, "thread_id = ?", new String[]{String.valueOf(this.mThreadID)}, null);
            }
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(getContext(), e);
        }
    }

    private void appendSignature(boolean isCheckAppendSignature) {
        this.mRichEditor.appendSignature(isCheckAppendSignature);
    }

    private void loadMessagesAndDraft(int debugFlag) {
        if (!this.mMessagesAndDraftLoaded) {
            if (Log.isLoggable("Mms_app", 2)) {
                Log.v("RcsGroupChatComposeMessageFragment", "### CMA.loadMessagesAndDraft: flag=" + debugFlag);
            }
            boolean drawBottomPanel = true;
            if (this.mShouldLoadDraft && loadDraft()) {
                drawBottomPanel = false;
            }
            if (this.mMsgListView != null && this.mMsgListView.isInEditMode()) {
                drawBottomPanel = false;
            }
            if (drawBottomPanel) {
                drawBottomPanel();
            }
            this.mMessagesAndDraftLoaded = true;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("RcsGroupChatComposeMessageFragment", "onSaveInstanceState entry");
    }

    public void onResume() {
        super.onResume();
        Log.d("RcsGroupChatComposeMessageFragment", "onResume");
        if (RcsProfile.getRcsService() != null && RcsProfile.isRcsImServiceSwitchEnabled()) {
            RcsProfile.registerRcsCallBack(Integer.valueOf(1301), new IfMsgplusCbImpl(1301));
        }
        startMsgListQuery();
        adjustTextPadding();
        this.mIsRunning = true;
        updateThreadIdIfRunning();
        if (this.mMsgListAdapter != null) {
            this.mMsgListAdapter.setOnDataSetChangedListener(this.mDataSetChangedListener);
        }
        this.mContentResolver.registerContentObserver(sMemberUri, false, this.mObMemberChange);
        startGroupMemberInfoQuery();
        this.isShowComposing = false;
        onKeyboardStateChanged();
        updateSendButtonState();
        updateAddAttachView();
        isUpdateTitle();
        if (RcsProfile.getRcsService() != null) {
            try {
                RcsProfile.getRcsService().readGroupChatMessage(this.mGroupID);
            } catch (RemoteException e) {
                MLog.e("RcsGroupChatComposeMessageFragment", "remote error");
            }
        }
        if (!this.isPeeking) {
            markRcsGroupMessageAsRead(true);
        }
        RcsUserGuideFragment.startUserGuide(getContext(), 3);
        setLoginAndOwnerStatus();
        if (this.isPeeking) {
            hideInputMode();
            hideKeyboard();
            if (mIsInvalidGroupStatus) {
                getRcsGroupChatComposeMessageActivity().setPeekActionStatus(2);
            } else {
                getRcsGroupChatComposeMessageActivity().setPeekActionStatus(1);
            }
        } else if (MmsCommon.isFromPeekReply(getIntent())) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(9805), 300);
        }
    }

    private void isUpdateTitle() {
        if (!this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
            updateTitle();
        }
    }

    public void onPause() {
        super.onPause();
        if (RcsProfile.getRcsService() != null && RcsProfile.isRcsImServiceSwitchEnabled()) {
            RcsProfile.unregisterRcsCallBack(Integer.valueOf(1301), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(1301)));
        }
        Log.d("RcsGroupChatComposeMessageFragment", "onPause ");
        if (mIsGroupchatClosed && this.mHasGroupDraft) {
            this.mHasGroupDraft = false;
            asyncDeleteDraftMessage(this.mThreadID);
        }
        if (this.mMsgListAdapter != null) {
            this.mMsgListAdapter.setOnDataSetChangedListener(null);
        }
        this.mContentResolver.unregisterContentObserver(this.mObMemberChange);
        this.mIsRunning = false;
        if (MessagingNotification.getRcsMessagingNotification() != null) {
            MessagingNotification.getRcsMessagingNotification().setCurrentlyDisplayedThreadId(-2, 0);
        }
        if (this.mAsyncDialog != null) {
            this.mAsyncDialog.clearPendingProgressDialog();
        }
        if (this.mMsgListAdapter == null || this.mMsgListView.getLastVisiblePosition() < this.mMsgListAdapter.getCount() - 1) {
            this.mSavedScrollPosition = this.mMsgListView.getFirstVisiblePosition();
        } else {
            this.mSavedScrollPosition = Integer.MAX_VALUE;
        }
        if (Log.isLoggable("Mms_app", 2)) {
            Log.v("RcsGroupChatComposeMessageFragment", "onPause: mSavedScrollPosition=" + this.mSavedScrollPosition);
        }
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (aMsgPlus != null) {
            try {
                aMsgPlus.readGroupChatMessage(this.mGroupID);
                if (!this.isPeeking) {
                    markRcsGroupMessageAsRead(false);
                }
            } catch (RemoteException e) {
                MLog.e("RcsGroupChatComposeMessageFragment", "onPause RcsService RemoteException");
            }
        }
        if (this.isPeeking) {
            hideKeyboard();
        }
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            this.mActionMode = null;
        }
        this.mSplitActionBar.dismissPopup();
        this.mActionBarWhenSplit.getSplitActionBarView().dismissPopup();
        sendComposeMessageStopPlayBroadcast();
    }

    private void sendComposeMessageStopPlayBroadcast() {
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent("ACTION_COMPOSERESUME_STOPPLAYING"));
    }

    public void onStop() {
        super.onStop();
        if (!(this.mMsgListAdapter == null || this.mMsgListAdapter.getmImageCache() == null)) {
            this.mMsgListAdapter.getmImageCache().clearCache();
        }
        Log.d("RcsGroupChatComposeMessageFragment", "onStop ");
        if (RcsProfile.getRcsService() != null && RcsProfile.isRcsImServiceSwitchEnabled()) {
            RcsProfile.unregisterRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT)));
            RcsProfile.unregisterRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR)));
        }
        this.mBackgroundQueryHandler.cancelOperation(9527);
        this.mBackgroundQueryHandler.cancelOperation(9528);
        this.mBackgroundQueryHandler.cancelOperation(9529);
        if (Log.isLoggable("Mms_app", 2)) {
            log("onStop save draft");
        }
        if (!mIsGroupchatClosed) {
            saveDraft(true);
        }
        this.mShouldLoadDraft = true;
        if (this.mLocalBroadcastManager != null) {
            this.mLocalBroadcastManager.unregisterReceiver(this.mSaveDraftReceiver);
        }
        unRegisterRcsLoginStatusChangeBroadCast();
    }

    public void onDestroy() {
        Log.d("RcsGroupChatComposeMessageFragment", "onDestroy ");
        if (RcsAsyncIconLoader.isInstanceExist()) {
            RcsAsyncIconLoader loader = RcsAsyncIconLoader.getInstance();
            if (loader != null) {
                loader.quit();
            }
        }
        try {
            if (this.mHomeKeyEventReceiver != null) {
                getContext().unregisterReceiver(this.mHomeKeyEventReceiver);
            }
        } catch (RuntimeException e) {
            MLog.e("RcsGroupChatComposeMessageFragment", "onDestroy unregisterReceiver mHomeKeyEventReceiver error " + e);
        }
        if (!(this.mMsgListAdapter == null || this.mMsgListAdapter.getmImageCache() == null)) {
            this.mMsgListAdapter.getmImageCache().clearCache();
        }
        if (RcsProfile.getRcsService() != null && RcsProfile.isRcsImServiceSwitchEnabled()) {
            RcsProfile.unregisterRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS)));
            RcsProfile.unregisterRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST)));
        }
        if (this.mRichEditor != null) {
            this.mRichEditor.removeRcsGroupRichAttachmentListener(this);
            MmsApp.getApplication().getThumbnailManager().clear();
            RichMessageManager.get().removeRichMessageManager(getActivity().hashCode());
        }
        if (this.groupChatCreateReceiver != null) {
            getContext().unregisterReceiver(this.groupChatCreateReceiver);
        }
        super.onDestroy();
        if (this.mMsgListAdapter != null) {
            this.mMsgListAdapter.setOnDataSetChangedListener(null);
            this.mMsgListAdapter.changeCursor(null);
            this.mMsgListAdapter.cancelBackgroundLoading();
        }
        unRegisterDefSmsAppChanged();
        unRegisterGroupChatStatusChangeBroadCast();
        unRegisterGroupChatMemberChangeBroadCast();
        clearComposeing();
        cancelVideoCompressIfRunning();
        if (this.mMenuEx != null) {
            this.mMenuEx.clear();
        }
        LinkerTextTransfer.getInstance().clear();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        boolean z = false;
        super.onConfigurationChanged(newConfig);
        Log.d("RcsGroupChatComposeMessageFragment", "onConfigurationChanged ");
        this.mActionBarWhenSplit.setActionBarHeight(HwMessageUtils.getSplitActionBarHeight(getContext()));
        if (!HwMessageUtils.isSplitOn() && this.mMsgListView.isInEditMode()) {
            resetConfiguration(newConfig);
            this.mMenuEx.onPrepareOptionsMenu();
            SplitActionBarView splitActionBarView = this.mSplitActionBar;
            int i = (!this.mIsLandscape || isInMultiWindowMode()) ? 0 : 8;
            splitActionBarView.setVisibility(i);
            AbstractEmuiActionBar abstractEmuiActionBar = this.mActionBarWhenSplit;
            if (this.mIsLandscape && !isInMultiWindowMode()) {
                z = true;
            }
            abstractEmuiActionBar.showMenu(z);
            this.mSplitActionBar.dismissPopup();
            this.mActionBarWhenSplit.getSplitActionBarView().dismissPopup();
        }
        this.mRichEditor.onConfigurationChanged(newConfig);
        if (resetConfiguration(newConfig)) {
            getActivity().invalidateOptionsMenu();
        }
        onKeyboardStateChanged();
        if (this.mIsSmileyFaceShow) {
            setSmileyPagerAdapter(true, getContext());
        } else if (this.mIsAttachmentShow) {
            setAttachmentPagerAdapter(true, getContext());
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1004), 100);
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            this.mActionMode = null;
            return;
        }
        updateComposeStartIcon();
    }

    private boolean resetConfiguration(Configuration config) {
        boolean z;
        if (config.keyboardHidden == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mIsKeyboardOpen = z;
        boolean isLandscape = config.orientation == 2;
        if (this.mIsLandscape == isLandscape) {
            return false;
        }
        this.mIsLandscape = isLandscape;
        return true;
    }

    private void onKeyboardStateChanged() {
        this.mRichEditor.onKeyboardStateChanged(this.mIsSmsEnabled, this.mIsKeyboardOpen, true);
    }

    public void onUserInteraction() {
        checkPendingNotification();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            checkPendingNotification();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 4:
                boolean stubFlag = false;
                if (this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
                    if (this.mRcsGroupConversationInputManager.getMediaPickerFullScreenState()) {
                        this.mRcsGroupConversationInputManager.showMediaPicker(false, true);
                    }
                    this.mRcsGroupConversationInputManager.showHideMediaPicker(false, false);
                    this.mIsAttachmentShow = false;
                    stubFlag = true;
                }
                if (this.mIsSmileyFaceShow) {
                    this.mSmileyFaceStub.setVisibility(8);
                    this.mIsSmileyFaceShow = false;
                    stubFlag = true;
                }
                return stubFlag || procFastBackKey().booleanValue();
            case Place.TYPE_SCHOOL /*82*/:
                getActivity().invalidateOptionsMenu();
                break;
        }
    }

    private Boolean procFastBackKey() {
        if (!WorkingMessage.isDraftLoading()) {
            return Boolean.valueOf(false);
        }
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                int i = 0;
                while (i < 4) {
                    try {
                        if (!WorkingMessage.isDraftLoading()) {
                            break;
                        }
                        Thread.currentThread();
                        Thread.sleep(20);
                        i++;
                    } catch (InterruptedException e) {
                        MLog.e("RcsGroupChatComposeMessageFragment", "Interrupted error");
                    }
                }
                if (WorkingMessage.isDraftLoading()) {
                    WorkingMessage.setDraftStateUnknow();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                RcsGroupChatComposeMessageFragment.this.getActivity().dispatchKeyEvent(new KeyEvent(0, 4));
            }
        }.execute(new Void[0]);
        return Boolean.valueOf(true);
    }

    private void exitGroupChatComposeMessageActivity(Runnable exit) {
        if (TextUtils.isEmpty(this.mRichEditor.getText().toString()) && !mIsGroupchatClosed) {
            saveDraft(true);
        }
        exit.run();
    }

    private void adjustTextPadding() {
    }

    private void addAttachment(int type, boolean replace) {
        switch (type) {
            case 0:
                showSmileyDialog(replace, getContext());
                return;
            case 1:
                if (OsUtil.hasCameraPermission()) {
                    MessageUtils.capturePicture(this, 101);
                    return;
                }
                MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
                OsUtil.requestPermission((RcsGroupChatComposeMessageActivity) getActivity(), new String[]{"android.permission.CAMERA"}, 155);
                return;
            case 2:
                startActivityForResult(MessageUtils.getSelectImageIntent(), 100);
                return;
            case 3:
                if (OsUtil.hasCameraPermission()) {
                    RcsMessageUtils.recordVideo(this, OfflineMapStatus.EXCEPTION_SDCARD);
                    return;
                }
                MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
                OsUtil.requestPermission((RcsGroupChatComposeMessageActivity) getActivity(), new String[]{"android.permission.CAMERA"}, 156);
                return;
            case 4:
                startActivityForResult(MessageUtils.getSelectVideoIntent(), 102);
                return;
            case 5:
                MessageUtils.recordSound(this, LocationRequest.PRIORITY_NO_POWER, -1);
                return;
            case 6:
                startActivityForResult(MessageUtils.getSelectAudioIntent(this.mRichEditor.computeAddRecordSizeLimit()), LocationRequest.PRIORITY_LOW_POWER);
                return;
            case 7:
                Intent intent = new Intent();
                intent.putExtra("FROM_COMPOCE", false);
                intent.setClass(getContext(), CommonPhrase.class);
                startActivityForResult(intent, 130128);
                return;
            case 10:
                handleInsertCalendar();
                return;
            case 11:
                Intent contactIntent = new Intent();
                contactIntent.setAction("android.intent.action.PICK");
                contactIntent.setType("vnd.android.cursor.dir/contact");
                startActivityForResult(contactIntent, 110);
                return;
            case 23:
                MLog.i("RcsGroupChatComposeMessageFragment FileTrans: ", "selector file and send");
                startActivityForResult(MessageUtils.getIntentForSelectMediaByType("*/*", false), 120);
                return;
            case 24:
                if (RcsMapLoaderFactory.getMapLoader(getContext()) != null) {
                    RcsMapLoaderFactory.getMapLoader(getContext()).requestMap(getContext(), 124);
                    return;
                }
                return;
            default:
                MLog.w("RcsGroupChatComposeMessageFragment FileTrans: ", "Such status does not exist inaddAttachment ");
                return;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("RcsGroupChatComposeMessageFragment", "grantResults.length   = " + grantResults.length + "  permissions.length =" + permissions.length);
        if (grantResults.length == 0 || permissions.length == 0) {
            Log.d("RcsGroupChatComposeMessageFragment", "no permission granted, return");
            return;
        }
        boolean grantResult = true;
        for (int result : grantResults) {
            if (result != 0) {
                grantResult = false;
                break;
            }
        }
        Log.d("RcsGroupChatComposeMessageFragment", "grantResult   = " + grantResult);
        if (grantResult) {
            switchPermissionRequestResult(requestCode);
        } else {
            long timeInterval = System.currentTimeMillis() - MmsCommon.getRequestTimeMillis();
            Log.d("RcsGroupChatComposeMessageFragment", "timeInterval=" + timeInterval);
            if (timeInterval < 500) {
                Intent intent = new Intent("huawei.intent.action.REQUEST_PERMISSIONS");
                intent.setPackage("com.huawei.systemmanager");
                intent.putExtra("KEY_HW_PERMISSION_ARRAY", permissions);
                intent.putExtra("KEY_HW_PERMISSION_PKG", getActivity().getPackageName());
                try {
                    startActivityForResult(intent, requestCode);
                } catch (Exception e) {
                    Log.e("RcsGroupChatComposeMessageFragment", "recheckUserRejectPermissions: Exception", e);
                }
            }
        }
    }

    private void switchPermissionRequestResult(int requestCode) {
        switch (requestCode) {
            case 2:
                MapMediaChooser.remindUserIfNecessary(getContext());
                return;
            case 155:
                MessageUtils.capturePicture(this, 101);
                return;
            case 156:
                RcsMessageUtils.recordVideo(this, OfflineMapStatus.EXCEPTION_SDCARD);
                return;
            case 157:
                handleResultData(1145, getIntent());
                return;
            case 158:
                startCalendarActivity();
                return;
            default:
                return;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!(!this.mRcsGroupConversationInputManager.isMediaPickerVisible() || data == null || resultCode != -1 || requestCode == 110 || requestCode == 144)) {
            this.mRcsGroupConversationInputManager.showHideMediaPicker(false, false);
            this.mIsAttachmentShow = false;
            if (!this.mIsLandscape) {
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        RcsGroupChatComposeMessageFragment.this.showKeyboard();
                    }
                }, 200);
            }
        }
        if (-1 == resultCode) {
            if (requestCode == SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                if (data != null) {
                    String groupName = data.getStringExtra("bundle_group_name");
                    if (!TextUtils.isEmpty(groupName)) {
                        MLog.d("RcsGroupChatComposeMessageFragment", "onActivityResult from groupchat details back,and setgroupname");
                        this.mGroupName = groupName;
                        this.mActionBarWhenSplit.setTitle(this.mGroupName, this.mGroupNumber);
                    }
                }
            } else if (!RcsUtility.isRCSFileTypeInvalid(getContext(), requestCode, 2, data)) {
                if (requestCode == 155) {
                    if (OsUtil.hasCameraPermission()) {
                        MessageUtils.capturePicture(this, 101);
                    }
                    return;
                }
                if (requestCode == 156 && OsUtil.hasCameraPermission()) {
                    RcsMessageUtils.recordVideo(this, OfflineMapStatus.EXCEPTION_SDCARD);
                }
                if (requestCode == 157) {
                    Bundle bundle = getIntent().getExtras();
                    if (bundle != null) {
                        if (OsUtil.hasCalendarPermission()) {
                            ArrayList<Uri> uriList = bundle.getParcelableArrayList("hw_eventsurl_list");
                            if (uriList != null) {
                                this.mRichEditor.insertVcalendarText(uriList);
                            }
                        } else {
                            MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
                            OsUtil.requestPermission(getActivity(), new String[]{"android.permission.READ_CALENDAR"}, 157);
                        }
                    }
                    return;
                }
                this.mShouldLoadDraft = false;
                MLog.i("RcsGroupChatComposeMessageFragment FileTrans: ", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode + ", data=" + data);
                handleResultData(requestCode, data);
            }
        }
    }

    private void handleResultData(int requestCode, Intent data) {
        if (113 == requestCode && data != null) {
            data = RcsUtility.handleVcardForFT(getContext(), "vcard_temp.vcf");
            requestCode = 120;
        }
        Uri fileuri;
        Intent intent;
        Uri videoUri;
        switch (requestCode) {
            case 100:
            case 102:
            case LocationRequest.PRIORITY_LOW_POWER /*104*/:
            case LocationRequest.PRIORITY_NO_POWER /*105*/:
            case 120:
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        if (!uri.toSafeString().startsWith(Contacts.CONTENT_LOOKUP_URI.toSafeString())) {
                            FileInfo info = RcsTransaction.getFileInfoByData(getContext(), uri);
                            if (info != null && 10 == RcsUtility.getFileTransType(info.getSendFilePath())) {
                                Bundle bundle = new Bundle();
                                bundle.putString("groupId", this.mGroupID);
                                bundle.putLong("thread_id", this.mThreadID);
                                RcsUtility.showUserFtNoNeedVardDialog(null, null, uri, bundle, 2, getContext(), null, null, null);
                                break;
                            }
                            if (info == null) {
                                Toast.makeText(getContext(), getContext().getString(R.string.rcs_invalid_file_info), 0).show();
                            }
                            RcsTransaction.rcsSendGroupAnyFile(getContext(), uri, this.mThreadID, this.mGroupID);
                            break;
                        }
                        MLog.d("RcsGroupChatComposeMessageFragment FileTrans: ", "any file -> match uri. uri = " + uri.toSafeString());
                        if (RcsUtility.handleAddVCard(getContext(), data, "vcard_temp.vcf")) {
                            fileuri = Uri.fromFile(getActivity().getFileStreamPath("vcard_temp.vcf"));
                            intent = new Intent();
                            intent.setData(fileuri);
                            intent.setClass(getContext(), ContactItemPickActivity.class);
                            startActivityForResult(intent, 113);
                        } else {
                            MLog.d("RcsGroupChatComposeMessageFragment FileTrans: ", "add vCard file failed. Nothing to do.");
                        }
                        return;
                    }
                }
                break;
            case 101:
                takePicture();
                break;
            case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                if (data != null) {
                    videoUri = data.getData();
                    if (videoUri != null) {
                        String path = RcsTransaction.getPath(getContext(), videoUri);
                        if (!TextUtils.isEmpty(path)) {
                            MLog.i("RcsGroupChatComposeMessageFragment", "onActivityResult case REQUEST_CODE_TAKE_VIDEO data=" + data + " data.getData()=" + data.getData());
                            RcsTransaction.showFileSaveResult(getContext(), path);
                            Intent intent2 = new Intent(getContext(), RcsVideoPreviewActivity.class);
                            intent2.putExtra("file_path", path);
                            startActivityForResult(intent2, 150223);
                            this.mIsRecordVideo = true;
                            RcsFFMpegMediaScannerNotifier.scan(getContext(), path);
                            break;
                        }
                        MLog.e("RcsGroupChatComposeMessageFragment", "onAcitivityResult:path is isEmpty");
                        break;
                    }
                    MLog.e("RcsGroupChatComposeMessageFragment", "onAcitivityResult:videoUri is null");
                    break;
                }
                break;
            case 110:
                if (data != null) {
                    MLog.d("RcsGroupChatComposeMessageFragment FileTrans: ", "pick contacts ->  contactUri = " + data.getData().toSafeString());
                    if (!RcsUtility.handleAddVCard(getContext(), data, "vcard_temp.vcf")) {
                        MLog.d("RcsGroupChatComposeMessageFragment FileTrans: ", "pick contacts -> add vCard file failed. Nothing to do.");
                        break;
                    }
                    fileuri = Uri.fromFile(getActivity().getFileStreamPath("vcard_temp.vcf"));
                    intent = new Intent();
                    intent.setData(fileuri);
                    intent.setClass(getContext(), ContactItemPickActivity.class);
                    startActivityForResult(intent, 113);
                    break;
                }
                break;
            case 117:
                fullScreenVary(data);
                break;
            case 124:
                if (!(data == null || RcsMapLoaderFactory.getMapLoader(getContext()) == null)) {
                    RcsMapLoaderFactory.getMapLoader(getContext()).sendGroupLocation(data, this.mGroupID);
                    break;
                }
            case 144:
                multiImage(data);
                break;
            case HwCustComposeMessageImpl.REQUEST_CODE_MEDIA_COMPRESS_FORWARD /*150*/:
                RcsTransaction.sendFileForForward(data, getContext());
                break;
            case 1145:
                calenderText(data);
                break;
            case 130128:
                if (data != null) {
                    Object extra = data.getExtra("COMMON_PHRASE");
                    if (extra != null) {
                        this.mRichEditor.insertPhrase((CharSequence) extra);
                        break;
                    }
                }
                break;
            case 150222:
                if (data != null) {
                    Bundle bdl = data.getExtras();
                    if (bdl != null) {
                        long threadId = bdl.getLong("threadId");
                        String groupId = bdl.getString("groupId");
                        if (TextUtils.isEmpty(groupId)) {
                            groupId = this.mGroupID;
                        }
                        List<Uri> uList = bdl.getParcelableArrayList("uriList");
                        if (uList != null) {
                            List<String> aList = bdl.getStringArrayList("addrList");
                            if (!data.getBooleanExtra("fullSize", false)) {
                                MLog.d("RcsGroupChatComposeMessageFragment FileTrans: ", " onAcitivityResult: send compressed media file");
                                RcsTransaction.multiSendWithUriResized(getContext(), threadId, uList, aList, 120, groupId);
                                break;
                            }
                            MLog.d("RcsGroupChatComposeMessageFragment FileTrans: ", " onAcitivityResult:send original size media file");
                            RcsTransaction.multiSend(getContext(), Long.valueOf(threadId), uList, aList, 120, groupId);
                            break;
                        }
                    }
                }
                break;
            case 150223:
                if (data != null) {
                    String prePath = data.getStringExtra("file_path");
                    if (!TextUtils.isEmpty(prePath)) {
                        videoUri = Uri.fromFile(new File(prePath));
                        RcsTransaction.rcsSendGroupAnyFile(getContext(), videoUri, this.mThreadID, this.mGroupID);
                        break;
                    }
                    MLog.e("RcsGroupChatComposeMessageFragment FileTrans: ", "path is null");
                    break;
                }
                break;
            case 160125:
            case 160126:
                if (this.mForwarder != null) {
                    this.mForwarder.onForwardResult(data);
                    break;
                }
                break;
            case 160127:
                if (this.mChatForwarder != null) {
                    this.mChatForwarder.rcsActivityResult(data);
                    break;
                }
                break;
        }
        this.mScrollOnSend = true;
    }

    private void calenderText(Intent data) {
        if (data != null) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }
            if (OsUtil.hasCalendarPermission()) {
                ArrayList<Uri> uriList = bundle.getParcelableArrayList("hw_eventsurl_list");
                if (uriList != null) {
                    this.mRichEditor.insertVcalendarText(uriList);
                    return;
                }
                return;
            }
            MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
            OsUtil.requestPermission(getActivity(), new String[]{"android.permission.READ_CALENDAR"}, 157);
        }
    }

    private void fullScreenVary(Intent data) {
        if (data != null) {
            String smsData = data.getStringExtra("full_screen_data");
            this.mRichEditor.setText(smsData);
            if (!TextUtils.isEmpty(smsData) && data.getBooleanExtra("full_screen_send_enable", false)) {
                RcsTransaction.toSendGroupMessage(getContext(), this.mGroupID, smsData);
                this.mRichEditor.setText("");
                hideFullScreenButton();
            }
        }
    }

    private void multiImage(Intent data) {
        if (data != null) {
            ArrayList uriLists = data.getParcelableArrayListExtra("select-item-list");
            if (uriLists != null && uriLists.size() != 0) {
                this.mRichEditor.setNewAttachment(uriLists, 2);
            }
        }
    }

    private void takePicture() {
        Uri tarUri = Uri.fromFile(RcsUtility.createNewFileByCopyOldFile(new File(TempFileProvider.getScrapPicPath()), getContext()));
        MLog.d("RcsGroupChatComposeMessageFragment FileTrans: ", "take pictures tarUri = " + tarUri.toString());
        RcsTransaction.rcsSendGroupAnyFile(getContext(), tarUri, this.mThreadID, this.mGroupID);
    }

    private void showSmileyDialog(boolean replace, Context context) {
        this.mRichEditor.requestFocus();
        if (this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
            this.mRcsGroupConversationInputManager.showHideMediaPicker(false, false);
            this.mIsAttachmentShow = false;
        }
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService("input_method");
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(this.mRichEditor.getWindowToken(), 0);
        }
        setSmileyPagerAdapter(replace, context);
    }

    private void drawBottomPanel() {
        resetCounter();
        if ((this.mMsgListView == null || !this.mMsgListView.isInEditMode()) && this.mBottomPanel != null) {
            if (mIsInvalidGroupStatus) {
                this.mBottomPanel.setVisibility(8);
            } else {
                this.mBottomPanel.setVisibility(0);
            }
        }
    }

    private void drawTopPanel(boolean showSubjectEditor) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_full_screen:
                Intent intent = new Intent(getContext(), MessageFullScreenActivity.class);
                intent.putExtra("smsData", this.mRichEditor.getText().toString());
                intent.putExtra("sendState", 130);
                intent.putExtra("is_groupchat", true);
                intent.putExtra("group_id", this.mGroupID);
                intent.putExtra("isInRcsMode", true);
                intent.putExtra("isSendMessageEnable", this.mIsRcsEnable);
                if (!HwMessageUtils.isSplitOn()) {
                    startActivityForResult(intent, 117);
                    break;
                }
                Activity activity = getActivity();
                HwBaseFragment messageFullScreenFragment = new MessageFullScreenFragment();
                messageFullScreenFragment.setController(new ControllerImpl(activity, messageFullScreenFragment));
                messageFullScreenFragment.setIntent(intent);
                ((ConversationList) activity).changeRightAddToStack(messageFullScreenFragment, (HwBaseFragment) this);
                break;
            case R.id.add_attach:
                if (this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
                    this.mRcsGroupConversationInputManager.showHideMediaPicker(false, false);
                    showKeyboard();
                    this.mIsAttachmentShow = false;
                } else {
                    this.mRcsGroupConversationInputManager.showHideMediaPicker(true, true);
                    hideKeyboard();
                    this.mRcsGroupEditorView.setVisibility(0);
                    this.mRcsGroupPickAudio.setVisibility(8);
                    isDataExsit();
                    this.mSendButtonSms.setBackground(ResEx.self().getStateListDrawable(getContext(), this.mRcsAudioMessage.getViewBackGroud(R.drawable.ic_send_message_rcs)));
                }
                if (this.mIsSmileyFaceShow) {
                    this.mSmileyFaceStub.setVisibility(8);
                    this.mIsSmileyFaceShow = false;
                    return;
                }
                break;
            case R.id.add_emojis:
                if (this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
                    this.mRcsGroupConversationInputManager.showHideMediaPicker(false, false);
                    this.mIsAttachmentShow = false;
                }
                if (this.mIsSmileyFaceShow) {
                    this.mSmileyFaceStub.setVisibility(8);
                    this.mIsSmileyFaceShow = false;
                    this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            RcsGroupChatComposeMessageFragment.this.showKeyboard();
                        }
                    }, 200);
                } else {
                    hideKeyboard();
                    this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            RcsGroupChatComposeMessageFragment.this.setSmileyPagerAdapter(true, RcsGroupChatComposeMessageFragment.this.getContext());
                        }
                    }, 200);
                }
                updateEmojiAddView();
                this.mRichEditor.setEditTextFocus();
                break;
            case R.id.send_button_sms:
                if (!this.mRcsAudioMessage.switchCurrentView()) {
                    String sendText = this.mRichEditor.getText().toString();
                    List<MediaModel> attachmentData = this.mRichEditor.getMediaModelData();
                    onPreGroupChatMessageSent();
                    try {
                        RcsTransaction.toSendGroupMessage(getContext(), this.mGroupID, sendText);
                        this.mScrollOnSend = true;
                    } catch (Exception e) {
                        MLog.e("RcsGroupChatComposeMessageFragment", "sendGroupMessage Exception");
                    }
                    RcsTransaction.multiGroupSend(getContext(), attachmentData, this.mThreadID, this.mGroupID, this.mRichEditor.getFullSizeFlag());
                    this.mRichEditor.setFullSizeFlag(false);
                    judgeAttachSmiley();
                    break;
                }
                this.mSendButtonSms.setBackground(ResEx.self().getStateListDrawable(getContext(), this.mRcsAudioMessage.getViewBackGroud(R.drawable.ic_send_message_rcs)));
                if (this.mRcsAudioMessage.getCurrentView() == 2) {
                    this.mRcsGroupEditorView.setVisibility(8);
                    this.mRcsGroupPickAudio.setVisibility(0);
                    if (this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
                        this.mRcsGroupConversationInputManager.showHideMediaPicker(false, false);
                        this.mIsAttachmentShow = false;
                    }
                    hideKeyboard();
                } else {
                    this.mRcsGroupEditorView.setVisibility(0);
                    this.mRcsGroupPickAudio.setVisibility(8);
                }
                return;
        }
    }

    private void isDataExsit() {
        if (this.mRichEditor != null && this.mRichEditor.getText().length() == 0 && this.mRichEditor.getMediaModelData().size() == 0) {
            RcsAudioMessage rcsAudioMessage = this.mRcsAudioMessage;
            RcsAudioMessage.setCurrentView(1);
        }
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    private void initComposeHandler() {
        if (this.mComposeEventHdlr == null) {
            HandlerThread thread = new HandlerThread("GroupComposeHandler");
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleComposingEvent() {
        CharSequence testMsg = this.mRichEditor.getText();
        ArrayList<Uri> uriDatas = this.mRichEditor.getUriData();
        if (!((TextUtils.isEmpty(testMsg) && (uriDatas == null || uriDatas.size() == 0)) || RcsProfile.getRcsService() == null)) {
            try {
                RcsProfile.getRcsService().sendGroupComposingState(this.mGroupID, 1);
                MLog.i("RcsGroupChatComposeMessageFragment FileTrans: ", "sendGroupComposingState Composing");
            } catch (Exception e) {
                MLog.e("RcsGroupChatComposeMessageFragment", "sendGroupComposingState error");
            }
        }
    }

    private void initResourceRefs() {
        this.mMsgListView = (RcsGroupChatMessageListView) this.mRootView.findViewById(R.id.history);
        this.mMsgListView.setFastScrollEnabled(true);
        this.mMsgListView.setDivider(null);
        this.mMsgListView.setClipToPadding(false);
        this.mMsgListView.setOnSizeChangedListener(new OnSizeChangedListener() {
            public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                if (Log.isLoggable("Mms_app", 2)) {
                    Log.v("RcsGroupChatComposeMessageFragment", "onSizeChanged: w=" + width + " h=" + height + " oldw=" + oldWidth + " oldh=" + oldHeight);
                }
                if (!RcsGroupChatComposeMessageFragment.this.mMessagesAndDraftLoaded && oldHeight - height > SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                    RcsGroupChatComposeMessageFragment.this.loadMessagesAndDraft(3);
                }
                RcsGroupChatComposeMessageFragment.this.smoothScrollToEnd(false, height - oldHeight);
                View vAttch;
                if (!RcsGroupChatComposeMessageFragment.this.isInMultiWindowMode() || MessageUtils.getScreenHeight(RcsGroupChatComposeMessageFragment.this.getActivity()) > RcsGroupChatComposeMessageFragment.this.getContext().getResources().getDimensionPixelSize(R.dimen.multi_window_mode_do_not_show_attchment_or_emoji_min_height)) {
                    RcsGroupChatComposeMessageFragment.this.mEmojiAdd.setEnabled(true);
                    RcsGroupChatComposeMessageFragment.this.mEmojiAdd.setClickable(true);
                    vAttch = RcsGroupChatComposeMessageFragment.this.mRootView.findViewById(R.id.add_attach);
                    vAttch.setEnabled(true);
                    vAttch.setClickable(true);
                } else {
                    RcsGroupChatComposeMessageFragment.this.mEmojiAdd.setEnabled(false);
                    RcsGroupChatComposeMessageFragment.this.mEmojiAdd.setClickable(false);
                    vAttch = RcsGroupChatComposeMessageFragment.this.mRootView.findViewById(R.id.add_attach);
                    vAttch.setEnabled(false);
                    vAttch.setClickable(false);
                    if (RcsGroupChatComposeMessageFragment.this.mIsAttachmentShow || RcsGroupChatComposeMessageFragment.this.mIsSmileyFaceShow) {
                        HwBackgroundLoader.getUIHandler().postDelayed(RcsGroupChatComposeMessageFragment.this.mHidePanelRunnable, 200);
                    }
                }
                RcsGroupChatComposeMessageFragment.this.updateEmojiAddView();
            }
        });
        this.mMsgListView.setClipChildren(false);
        this.mBottomPanel = this.mRootView.findViewById(R.id.bottom_panel);
        this.mRichEditor = (RcsGroupChatRichMessageEditor) this.mRootView.findViewById(R.id.rich_message_editor);
        this.mRichEditor.setFragment(this);
        this.mRichEditor.setListener(this.mRichEditorListener);
        this.mRichEditor.addRcsGroupRichAttachmentListener(this);
        this.mRcsGroupEditorView = (LinearLayout) this.mRootView.findViewById(R.id.rcsgroup_attachment_editor_view);
        this.mRcsGroupPickAudio = (Button) this.mRootView.findViewById(R.id.rcsgroup_pick_audio);
        if (this.mRcsAudioMessage != null) {
            this.mRcsAudioMessage.setPickAudioButton(this.mRcsGroupPickAudio);
        }
        this.mTextCounter = (TextView) this.mRootView.findViewById(R.id.text_counter);
        this.mSendButtonLayout = (LinearLayout) this.mRootView.findViewById(R.id.button_multisim_model);
        this.mSendButtonSms = (TextView) this.mRootView.findViewById(R.id.send_button_sms);
        if (!(this.mSendButtonLayout == null || this.mSendButtonSms == null)) {
            this.mSendButtonLayout.setVisibility(0);
            this.mSendButtonSms.setVisibility(0);
        }
        if (this.mSendButtonSms != null) {
            this.mSendButtonSms.setBackground(ResEx.self().getStateListDrawable(getContext(), R.drawable.ic_send_message_rcs));
            this.mSendButtonSms.setOnClickListener(this);
        }
        this.mBottomView = (ViewGroup) this.mRootView.findViewById(R.id.multiselect_button);
        this.mBottomView.setVisibility(8);
        this.mBottomScroller = (ScrollView) this.mBottomPanel.findViewById(R.id.attachment_editor_scroll_view);
        this.mBottomMaxHeightPortrait = getResources().getDimensionPixelOffset(R.dimen.mms_bottom_layout_max_height_portrait);
        this.mBottomMaxHeightPortraitNoKeyboard = getResources().getDimensionPixelOffset(R.dimen.mms_bottom_layout_max_height_portrait_no_keyboard);
        this.mBottomMaxHeightLandSpace = getResources().getDimensionPixelOffset(R.dimen.mms_bottom_layout_max_height_landspace);
        this.mScreenHeight = MessageUtils.getScreenHeight(getActivity());
        this.mComposeLayoutGroup = (LinearLayout) this.mRootView.findViewById(R.id.mms_compose_view_group);
        this.mComposeLayoutGroup.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (RcsGroupChatComposeMessageFragment.this.mScreenHeight - bottom < RcsGroupChatComposeMessageFragment.this.mBottomMaxHeightPortrait) {
                    if (!RcsGroupChatComposeMessageFragment.this.mKeyboardHiden) {
                        RcsGroupChatComposeMessageFragment.this.mKeyboardHiden = true;
                        RcsGroupChatComposeMessageFragment.this.resetBottomScrollerHeight(true);
                        RcsGroupChatComposeMessageFragment.this.updateEmojiAddView();
                    }
                } else if (RcsGroupChatComposeMessageFragment.this.mKeyboardHiden) {
                    RcsGroupChatComposeMessageFragment.this.mKeyboardHiden = false;
                    RcsGroupChatComposeMessageFragment.this.resetBottomScrollerHeight(true);
                    RcsGroupChatComposeMessageFragment.this.updateEmojiAddView();
                }
            }
        });
        this.mBottomPanel.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                boolean canScrollVertically = !RcsGroupChatComposeMessageFragment.this.mBottomScroller.canScrollVertically(1) ? RcsGroupChatComposeMessageFragment.this.mBottomScroller.canScrollVertically(-1) : true;
                if (!RcsGroupChatComposeMessageFragment.this.mBottomScrollerCanScroll || canScrollVertically) {
                    RcsGroupChatComposeMessageFragment.this.mBottomScrollerCanScroll = canScrollVertically;
                    if (!(oldTop == top && oldBottom == bottom)) {
                        RcsGroupChatComposeMessageFragment.this.resetBottomScrollerHeight(false);
                    }
                    RcsGroupChatComposeMessageFragment.this.updateEmojiAddView();
                    return;
                }
                RcsGroupChatComposeMessageFragment.this.mBottomScrollerCanScroll = false;
                RcsGroupChatComposeMessageFragment.this.resetBottomScrollerHeight(true);
                RcsGroupChatComposeMessageFragment.this.updateEmojiAddView();
            }
        });
        initFullScreenView();
        updateEmojiAddView();
    }

    private void initMessageList() {
        if (this.mMsgListAdapter == null) {
            String highlightString = getIntent().getStringExtra("highlight");
            this.mMsgListAdapter = new RcsGroupChatMessageListAdapter(getContext(), null, this.mMsgListView, true, this.mGroupID, highlightString == null ? null : Pattern.compile(Pattern.quote(highlightString), 2), this);
            if (this.mMsgListView != null) {
                this.mMsgListView.setAdapter(this.mMsgListAdapter);
                this.mMsgListView.setVisibility(0);
                this.mMsgListView.setOnCreateContextMenuListener(this.mMsgListMenuCreateListener);
            }
            initMsgListListener();
            this.mMsgListAdapter.setHandler(this.mHandler);
            if (this.mMsgListView != null) {
                this.mMsgListView.setOnEditModeListener(new OnGroupEditModeListener() {
                    public void multiOperation(long[] msgIds, String[] address, long[] types) {
                        DeleteGroupMulMessageListener l = new DeleteGroupMulMessageListener(msgIds, address, types);
                        if (RcsGroupChatComposeMessageFragment.this.mMultyOperType == 0) {
                            RcsGroupChatComposeMessageFragment.this.confirmDeleteMultiDialog(l);
                        }
                        if (1 == RcsGroupChatComposeMessageFragment.this.mMultyOperType) {
                            l.deleteMulti(msgIds, types);
                        }
                    }
                });
            }
        }
    }

    private void initMsgListListener() {
        EMUIGroupChatListViewListener listener = new EMUIGroupChatListViewListener();
        if (this.mMsgListView != null) {
            this.mMsgListView.setListViewListener(listener);
            this.mMsgListView.setSelectionChangeLisenter(listener);
            this.mMsgListView.setOnItemLongClickListener(listener);
            this.mMsgListView.setOnScrollListener(new RcseScrollListener(this.mMsgListAdapter));
            this.mMsgListView.setMultiModeClickListener(this.mMsgListView.getMultiModeClickListener());
        }
    }

    private boolean loadDraft() {
        if (this.isPeeking) {
            return false;
        }
        if (Log.isLoggable("Mms_app", 2)) {
            log("loadDraft");
        }
        loadDraft(new IDraftLoaded() {
            public void onDraftLoaded(Uri msgUri) {
                RcsGroupChatComposeMessageFragment.this.drawTopPanel(false);
                RcsGroupChatComposeMessageFragment.this.drawBottomPanel();
                if (!RcsGroupChatComposeMessageFragment.this.mHasGroupDraft) {
                    if (RcsGroupChatComposeMessageFragment.this.getIntent().getExtras() != null) {
                        String shareText = RcsGroupChatComposeMessageFragment.this.getIntent().getExtras().getString("android.intent.extra.TEXT");
                        if (shareText != null) {
                            RcsGroupChatComposeMessageFragment.this.mRichEditor.setText(shareText);
                            RcsGroupChatComposeMessageFragment.this.getIntent().removeExtra("android.intent.extra.TEXT");
                        }
                    }
                    RcsGroupChatComposeMessageFragment.this.appendSignature(false);
                }
            }
        });
        return true;
    }

    private String readDraftGroupMessage(long threadId) {
        long thread_id = threadId;
        Cursor cursor = null;
        MLog.d("RcsGroupChatComposeMessageFragment", " read draft , the thread_id is " + threadId);
        if (threadId <= 0) {
            return "";
        }
        String body = "";
        try {
            cursor = SqliteWrapper.query(getContext(), this.mContentResolver, sMessageUri, SMS_BODY_PROJECTION, "thread_id = ?  AND type = ?", new String[]{String.valueOf(threadId), String.valueOf(112)}, null);
            if (cursor != null && cursor.moveToFirst()) {
                body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException e) {
            MLog.e("RcsGroupChatComposeMessageFragment", "cursor unknowable error");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return body;
    }

    public void loadDraft(final IDraftLoaded loadedCallback) {
        final long threadIdLoad = this.mThreadID;
        new AsyncTask<Void, Void, String>() {
            @SuppressLint({"NewApi"})
            protected String doInBackground(Void... none) {
                String draftText = RcsGroupChatComposeMessageFragment.this.readDraftGroupMessage(threadIdLoad);
                if (!TextUtils.isEmpty(draftText)) {
                    RcsGroupChatComposeMessageFragment.this.asyncDeleteDraftMessage(threadIdLoad);
                }
                return draftText;
            }

            protected void onPostExecute(String result) {
                if (result != null && RcsGroupChatComposeMessageFragment.this.mRichEditor != null) {
                    try {
                        JSONObject jSONObject = new JSONObject(result);
                        if (jSONObject.has("edittext")) {
                            String text = jSONObject.getString("edittext");
                            RcsGroupChatComposeMessageFragment.this.mIsNeedSendComposing = false;
                            if (!TextUtils.isEmpty(text)) {
                                RcsGroupChatComposeMessageFragment.this.mHasGroupDraft = true;
                                RcsGroupChatComposeMessageFragment.this.mRichEditor.setText(text);
                            }
                        }
                        if (jSONObject.has("groupChatBody") && jSONObject.has("groupChatBodyMap")) {
                            JSONArray groupBody = jSONObject.getJSONArray("groupChatBody");
                            JSONArray groupBodyMap = jSONObject.getJSONArray("groupChatBodyMap");
                            if (groupBody != null && groupBody.length() > 0) {
                                RcsGroupChatComposeMessageFragment.this.mHasGroupDraft = true;
                                int n = groupBody.length();
                                for (int i = 0; i < n; i++) {
                                    String path = groupBody.getString(i);
                                    String locValuesInfo = groupBodyMap.getJSONObject(i).getString(path);
                                    int attachType = RcsUtility.getFileTransType(path);
                                    Uri dataUri = Uri.parse(path);
                                    MediaModel videoModel;
                                    if (RcsGroupChatComposeMessageFragment.this.isMapInfoExist(locValuesInfo)) {
                                        switch (attachType) {
                                            case 7:
                                                ImageModel imageModel = new ImageModel(RcsGroupChatComposeMessageFragment.this.getContext(), dataUri, null);
                                                imageModel.setBuildSource(dataUri.getPath());
                                                RcsGroupChatComposeMessageFragment.this.mRichEditor.refreshHandlerSendMsg(dataUri, 2);
                                                RcsGroupChatComposeMessageFragment.this.mRichEditor.saveModelData(dataUri, imageModel);
                                                break;
                                            case 8:
                                                videoModel = new VideoModel(RcsGroupChatComposeMessageFragment.this.getContext(), dataUri, null);
                                                videoModel.setBuildSource(dataUri.getPath());
                                                RcsGroupChatComposeMessageFragment.this.mRichEditor.refreshHandlerSendMsg(dataUri, 5);
                                                RcsGroupChatComposeMessageFragment.this.mRichEditor.saveModelData(dataUri, videoModel);
                                                break;
                                            case 9:
                                                AudioModel audioModel = new AudioModel(RcsGroupChatComposeMessageFragment.this.getContext(), dataUri);
                                                RcsGroupChatComposeMessageFragment.this.mRichEditor.refreshHandlerSendMsg(dataUri, 3);
                                                RcsGroupChatComposeMessageFragment.this.mRichEditor.saveModelData(dataUri, audioModel);
                                                break;
                                            default:
                                                continue;
                                        }
                                    } else {
                                        jSONObject = new JSONObject(locValuesInfo);
                                        HashMap<String, String> locationMap = new HashMap();
                                        String locationTitle = jSONObject.getString("title");
                                        locationMap.put("title", locationTitle);
                                        String locationSub = jSONObject.getString("subtitle");
                                        locationMap.put("subtitle", locationSub);
                                        String latitude = jSONObject.getString("latitude");
                                        locationMap.put("latitude", latitude);
                                        String longitude = jSONObject.getString("longitude");
                                        locationMap.put("longitude", longitude);
                                        locationMap.put("locationinfo", locationTitle + "\n" + locationSub + "\n" + MessageUtils.getLocationWebLink(RcsGroupChatComposeMessageFragment.this.getContext()) + latitude + "," + longitude);
                                        videoModel = new ImageModel(RcsGroupChatComposeMessageFragment.this.getContext(), dataUri, null);
                                        videoModel.setBuildSource(dataUri.getPath());
                                        videoModel.setLocation(true);
                                        videoModel.setLocationSource(locationMap);
                                        RcsGroupChatComposeMessageFragment.this.mRichEditor.refreshHandlerSendMsg(dataUri, 8);
                                        RcsGroupChatComposeMessageFragment.this.mRichEditor.saveModelData(dataUri, videoModel);
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        MLog.e("RcsGroupChatComposeMessageFragment", "onPostExecute occurs JSONException");
                    } catch (MmsException e2) {
                        MLog.e("RcsGroupChatComposeMessageFragment", "onPostExecute occurs MmsException");
                    } catch (Exception e3) {
                        MLog.e("RcsGroupChatComposeMessageFragment", "onPostExecute occurs Exception");
                    }
                    if (loadedCallback != null) {
                        loadedCallback.onDraftLoaded(null);
                    }
                }
            }
        }.execute(new Void[0]);
    }

    private boolean isMapInfoExist(String locValuesInfo) {
        return (locValuesInfo == null || "".equals(locValuesInfo) || Constant.EMPTY_JSON.equals(locValuesInfo)) ? true : "[]".equals(locValuesInfo);
    }

    private void saveDraft(boolean isStopping) {
        if (Log.isLoggable("Mms_app", 2)) {
            LogTag.debug("saveDraft", new Object[0]);
        }
        if (!this.isPeeking) {
            MLog.d("RcsGroupChatComposeMessageFragment", " saveDraft , isStopping = " + isStopping);
            String editString = "";
            ArrayList arrayList = null;
            ArrayList draftDataLists = null;
            if (this.mRichEditor != null) {
                editString = this.mRichEditor.getText().toString();
                arrayList = this.mRichEditor.getUriData();
                draftDataLists = this.mRichEditor.getDraftListDatas();
            }
            try {
                JSONObject object = new JSONObject();
                JSONArray arrUri = new JSONArray();
                JSONArray arrUriAndLoc = new JSONArray();
                boolean isEmpty = TextUtils.isEmpty(editString.trim());
                if (arrayList != null) {
                    boolean isPathsExist = arrayList.size() > 0;
                    if (isPathsExist) {
                        int n = arrayList.size();
                        for (int i = 0; i < n; i++) {
                            arrUri.put(arrayList.get(i));
                            arrUriAndLoc.put(draftDataLists.get(i));
                        }
                        object.put("groupChatBody", arrUri);
                        object.put("groupChatBodyMap", arrUriAndLoc);
                    }
                    boolean isOnlySignature = SignatureUtil.deleteNewlineSymbol(editString).equals(SignatureUtil.getSignature(getContext(), ""));
                    if (!isEmpty) {
                        object.put("edittext", editString);
                    }
                    if (mIsInvalidGroupStatus || ((isEmpty || isOnlySignature) && !isPathsExist)) {
                        this.mHasGroupDraft = false;
                        asyncDeleteDraftMessage(this.mThreadID);
                        MLog.d("RcsGroupChatComposeMessageFragment", "saveDraft content is Empty");
                    }
                    this.mHasGroupDraft = true;
                    asyncUpdateDraftMessage(object.toString());
                }
            } catch (JSONException e) {
                MLog.e("RcsGroupChatComposeMessageFragment", "saveDraft occurs JSONException");
            } catch (Exception e2) {
                MLog.e("RcsGroupChatComposeMessageFragment", "saveDraft occurs Exception");
            }
        }
    }

    private void deleteDraftMessage(long threadId) {
        SqliteWrapper.delete(getContext(), this.mContentResolver, ContentUris.withAppendedId(sDeleteDraftUri, threadId), null, null);
        MLog.d("RcsGroupChatComposeMessageFragment", " asyncDeleteDraftMessage threadId = " + threadId);
    }

    private void asyncDeleteDraftMessage(final long mThreadID) {
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                try {
                    DraftCache.getInstance().setSavingDraft(true);
                    RcsGroupChatComposeMessageFragment.this.deleteDraftMessage(mThreadID);
                    if (DraftCache.getInstance().getHwCust() != null) {
                        DraftCache.getInstance().getHwCust().setDraftGroupState(RcsGroupChatComposeMessageFragment.this.mRcsThreadId, RcsGroupChatComposeMessageFragment.this.mHasGroupDraft);
                    }
                    DraftCache.getInstance().setSavingDraft(false);
                } catch (Throwable th) {
                    DraftCache.getInstance().setSavingDraft(false);
                }
            }
        });
    }

    private void asyncUpdateDraftMessage(String contents) {
        final long threadId = this.mThreadID;
        final long rcsThreadId = this.mRcsThreadId;
        final String groupID = this.mGroupID;
        final String str = contents;
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                try {
                    DraftCache.getInstance().setSavingDraft(true);
                    RcsGroupChatComposeMessageFragment.this.updateDraftGroupMessage(str, threadId, groupID);
                    if (DraftCache.getInstance().getHwCust() != null) {
                        DraftCache.getInstance().getHwCust().setDraftGroupState(rcsThreadId, RcsGroupChatComposeMessageFragment.this.mHasGroupDraft);
                    }
                    DraftCache.getInstance().setSavingDraft(false);
                } catch (Throwable th) {
                    DraftCache.getInstance().setSavingDraft(false);
                }
            }
        });
    }

    private void updateDraftGroupMessage(String contents, long threadID, String groupID) {
        long threadId = threadID;
        MLog.d("RcsGroupChatComposeMessageFragment", " updateDraftGroupMessage threadId =  " + threadID);
        if (threadID > 0) {
            String draftString = readDraftGroupMessage(threadID);
            ContentValues values = new ContentValues();
            values.put("date", Long.valueOf(System.currentTimeMillis()));
            values.put("body", contents);
            if (TextUtils.isEmpty(draftString)) {
                values.put("thread_id", Long.valueOf(threadID));
                values.put(NumberInfo.TYPE_KEY, Integer.valueOf(112));
                SqliteWrapper.insert(getContext(), this.mContentResolver, sMessageUri, values);
                MLog.d("RcsGroupChatComposeMessageFragment", "insert draft msg");
            } else {
                MLog.d("RcsGroupChatComposeMessageFragment", "updateDraft when draft not null and count is " + SqliteWrapper.update(getContext(), this.mContentResolver, sMessageUri, values, "thread_id = ?  AND type = ?", new String[]{String.valueOf(threadID), String.valueOf(112)}));
            }
            this.mHasGroupDraft = true;
        }
    }

    private void updateSendButtonState() {
        RcsAudioMessage rcsAudioMessage;
        if (!Boolean.valueOf(RcsProfile.canProcessGroupChat(this.mGroupID)).booleanValue() || mIsInvalidGroupStatus || this.mRichEditor.getText().length() != 0 || this.mRichEditor.getUriData().size() != 0) {
            rcsAudioMessage = this.mRcsAudioMessage;
            RcsAudioMessage.setCurrentView(3);
        } else if (this.mRichEditor.getText().length() != 0 || this.mRcsGroupPickAudio.getVisibility() == 0) {
            rcsAudioMessage = this.mRcsAudioMessage;
            RcsAudioMessage.setCurrentView(2);
        } else {
            rcsAudioMessage = this.mRcsAudioMessage;
            RcsAudioMessage.setCurrentView(1);
        }
        if (this.mRcsGroupPickAudio != null && this.mRcsGroupPickAudio.getVisibility() == 0 && 3 == this.mRcsAudioMessage.getCurrentView() && this.mRichEditor.isOnlyContainsSignature()) {
            RcsAudioMessage.setCurrentView(2);
        }
        if (!this.mIsSmsEnabled || mIsInvalidGroupStatus || (TextUtils.isEmpty(this.mRichEditor.getText()) && (this.mRichEditor.getUriData() == null || this.mRichEditor.getUriData().size() == 0))) {
            updateSendButtonView(false, false);
        } else if (RcsProfile.canProcessGroupChat(this.mGroupID)) {
            updateSendButtonView(true, true);
        } else {
            updateSendButtonView(true, false);
        }
    }

    private void updateSendButtonView(boolean cleanHint, boolean clickable) {
        EditTextWithSmiley editorText = (EditTextWithSmiley) this.mRootView.findViewById(R.id.embedded_text_editor);
        if (editorText != null) {
            if (!cleanHint || this.mRichEditor.getText().length() == 0) {
                editorText.setHint(R.string.type_to_compose_im_text_enter_to_send_new_rcs);
            } else {
                editorText.setHint("");
            }
        }
        clickable = this.mRcsAudioMessage.getClickStatus(clickable);
        setSendMessageButtonEnable(clickable);
        this.mSendButtonSms.setClickable(clickable);
    }

    private void initFocus() {
        if (this.mRichEditor != null && this.mThreadID > 0) {
            this.mRichEditor.setEditTextFocus();
        }
        if (!this.mIsKeyboardOpen || this.mIsAttachmentShow || !this.mIsSmileyFaceShow) {
        }
    }

    private void checkPendingNotification() {
        if (this.mPossiblePendingNotification && getActivity().hasWindowFocus()) {
            IfMsgplus aMsgPlus = RcsProfile.getRcsService();
            if (aMsgPlus != null) {
                try {
                    aMsgPlus.readGroupChatMessage(this.mGroupID);
                    markRcsGroupMessageAsRead(false);
                } catch (RemoteException e) {
                    MLog.e("RcsGroupChatComposeMessageFragment", "remote error");
                }
            }
            this.mPossiblePendingNotification = false;
        }
    }

    private void onCached() {
        Cursor cursor = this.mCursor;
        int newSelectionPos = -1;
        if (cursor != null) {
            log("onCached !!");
            this.mCursor = null;
            long targetMsgId = getIntent().getLongExtra("select_id", -1);
            if (targetMsgId != -1) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    if (cursor.getLong(0) == targetMsgId) {
                        newSelectionPos = cursor.getPosition();
                        break;
                    }
                }
                MLog.d("RcsGroupChatComposeMessageFragment", "get newSelectionPos = " + newSelectionPos);
            } else if (this.mSavedScrollPosition != -1) {
                if (this.mSavedScrollPosition == Integer.MAX_VALUE) {
                    int cnt = this.mMsgListAdapter.getCount();
                    if (cnt > 0) {
                        newSelectionPos = cnt - 1;
                        this.mSavedScrollPosition = -1;
                    }
                } else {
                    newSelectionPos = this.mSavedScrollPosition;
                    this.mSavedScrollPosition = -1;
                }
            }
            this.mMsgListAdapter.clearCachedListeItemTimes();
            this.mMsgListAdapter.changeCursor(cursor);
            this.mMsgListAdapter.updateInOutMsgCount();
            if (newSelectionPos != -1) {
                this.mMsgListView.setSelection(newSelectionPos);
            } else {
                long lastMsgId = 0;
                if (this.mMsgListAdapter.getCount() > 0) {
                    cursor.moveToLast();
                    lastMsgId = cursor.getLong(cursor.getColumnIndex("_id"));
                }
                boolean z = this.mScrollOnSend || lastMsgId != this.mLastMessageId;
                smoothScrollToEnd(z, 0);
                this.mLastMessageId = lastMsgId;
                this.mScrollOnSend = false;
            }
            if (this.mMsgListView.isInEditMode()) {
                this.mMsgListView.onDataReload();
            }
            this.mActionBarWhenSplit.getSplitActionBarView().dismissPopup();
            this.mSplitActionBar.dismissPopup();
            getActivity().invalidateOptionsMenu();
        }
    }

    private void smoothScrollToEnd(boolean force, int listSizeChange) {
        int lastItemVisible = this.mMsgListView.getLastVisiblePosition();
        final int lastItemInList = this.mMsgListAdapter.getCount() - 1;
        if (lastItemVisible < 0 || lastItemInList < 0) {
            if (Log.isLoggable("Mms_app", 2)) {
                Log.v("RcsGroupChatComposeMessageFragment", "smoothScrollToEnd: lastItemVisible=" + lastItemVisible + ", lastItemInList=" + lastItemInList + ", mMsgListView not ready");
            }
            if (force && lastItemVisible < 0 && lastItemInList > 0) {
                this.mMsgListView.setSelection(lastItemInList);
            }
            return;
        }
        boolean willScroll;
        View lastChildVisible = this.mMsgListView.getChildAt(lastItemVisible - this.mMsgListView.getFirstVisiblePosition());
        int lastVisibleItemBottom = 0;
        int lastVisibleItemHeight = 0;
        if (lastChildVisible != null) {
            lastVisibleItemBottom = lastChildVisible.getBottom();
            lastVisibleItemHeight = lastChildVisible.getHeight();
        }
        if (Log.isLoggable("Mms_app", 2)) {
            Log.v("RcsGroupChatComposeMessageFragment", "smoothScrollToEnd newPosition: " + lastItemInList + " mLastSmoothScrollPosition: " + this.mLastSmoothScrollPosition + " first: " + this.mMsgListView.getFirstVisiblePosition() + " lastItemVisible: " + lastItemVisible + " lastVisibleItemBottom: " + lastVisibleItemBottom + " lastVisibleItemBottom + listSizeChange: " + (lastVisibleItemBottom + listSizeChange) + " mMsgListView.getHeight() - mMsgListView.getPaddingBottom(): " + (this.mMsgListView.getHeight() - this.mMsgListView.getPaddingBottom()) + " listSizeChange: " + listSizeChange);
        }
        int listHeight = this.mMsgListView.getHeight();
        boolean lastItemTooTall = lastVisibleItemHeight > listHeight;
        if (force) {
            willScroll = true;
        } else if (listSizeChange == 0 && lastItemInList == this.mLastSmoothScrollPosition) {
            willScroll = false;
        } else {
            willScroll = lastVisibleItemBottom + listSizeChange <= listHeight - this.mMsgListView.getPaddingBottom();
        }
        if (willScroll || (lastItemTooTall && lastItemInList == lastItemVisible)) {
            if (Math.abs(listSizeChange) > SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                if (Log.isLoggable("Mms_app", 2)) {
                    Log.v("RcsGroupChatComposeMessageFragment", "keyboard state changed. setSelection=" + lastItemInList);
                }
                if (lastItemTooTall) {
                    this.mMsgListView.setSelectionFromTop(lastItemInList, listHeight - lastVisibleItemHeight);
                } else {
                    this.mMsgListView.setSelection(lastItemInList);
                }
            } else if (lastItemInList - lastItemVisible <= 1 || lastItemInList - lastItemVisible > 20) {
                if (Log.isLoggable("Mms_app", 2)) {
                    Log.v("RcsGroupChatComposeMessageFragment", "too many to scroll, setSelection=" + lastItemInList);
                }
                this.mMsgListView.setSelection(lastItemInList);
            } else {
                if (Log.isLoggable("Mms_app", 2)) {
                    Log.v("RcsGroupChatComposeMessageFragment", "smooth scroll to " + lastItemInList);
                }
                if (lastItemTooTall) {
                    this.mMsgListView.setSelectionFromTop(lastItemInList, listHeight - lastVisibleItemHeight);
                } else {
                    int lastPosition = lastItemInList;
                    this.mMsgListView.post(new Runnable() {
                        public void run() {
                            MLog.d("RcsGroupChatComposeMessageFragment FileTrans: ", "group handleScrollToPosition = " + lastItemInList);
                            if (!RcsGroupChatComposeMessageFragment.this.isDetached()) {
                                RcsGroupChatComposeMessageFragment.this.mMsgListView.smoothScrollToPosition(lastItemInList);
                            }
                        }
                    });
                }
                this.mLastSmoothScrollPosition = lastItemInList;
            }
        }
    }

    private void startMsgListQuery() {
        this.mBackgroundQueryHandler.cancelOperation(9527);
        if (this.mMsgListAdapter != null) {
            this.mMsgListAdapter.setConversationId(this.mThreadID);
        }
        try {
            this.mBackgroundQueryHandler.startQuery(9527, null, sMessageUri, null, "thread_id = ?", new String[]{String.valueOf(this.mThreadID)}, "date");
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(getContext(), e);
        }
    }

    private void startGroupMemberInfoQuery() {
        this.mBackgroundQueryHandler.cancelOperation(9528);
        try {
            this.mBackgroundQueryHandler.startQuery(9528, null, sMemberUri, null, "thread_id = ?", new String[]{String.valueOf(this.mThreadID)}, null);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(getContext(), e);
        }
    }

    private boolean judgeAttachSmiley() {
        if (this.mIsSmileyFaceShow) {
            this.mSmileyFaceStub.setVisibility(8);
            this.mIsSmileyFaceShow = false;
        }
        if (this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
            this.mRcsGroupConversationInputManager.showHideMediaPicker(false, false);
            this.mIsAttachmentShow = false;
        }
        return true;
    }

    private void setAttachmentPagerAdapter(final boolean replace, Context context) {
        ViewPager pager;
        ViewGroup signView;
        if (this.mAttachmentStub == null) {
            this.mAttachmentStub = (ViewStub) this.mRootView.findViewById(R.id.attachmentview);
            View view = this.mAttachmentStub.inflate();
            pager = (ViewPager) view.findViewById(R.id.grid_pager);
            signView = (ViewGroup) view.findViewById(R.id.current_sign_view);
            view.setBackgroundColor(getResources().getColor(R.color.text_color_dark_splite_line));
            pager.setBackgroundColor(getResources().getColor(R.color.signview_divider_line_color));
            signView.setBackgroundColor(getResources().getColor(R.color.attach_panel_item_color));
        } else {
            signView = (ViewGroup) this.mRootView.findViewById(R.id.current_sign_view);
            pager = (ViewPager) this.mRootView.findViewById(R.id.grid_pager);
            signView.removeAllViews();
            pager.removeAllViews();
        }
        LayoutParams layoutParam = this.mAttachmentStub.getLayoutParams();
        if (MmsConfig.isInSimpleUI()) {
            layoutParam.height = (int) getResources().getDimension(R.dimen.attach_panel_height_sui);
        } else if (isInMultiWindowMode()) {
            layoutParam.height = (int) getResources().getDimension(R.dimen.attach_panel_height_multiwindow);
        } else {
            layoutParam.height = (int) getResources().getDimension(R.dimen.attach_panel_height);
        }
        this.mAttachmentTypeSelectorAdapter = null;
        RcsAttachmentSmileyPagerAdatper.setIsGroup(true);
        this.mAttachmentTypeSelectorAdapter = new AttachmentSmileyPagerAdatper(getContext(), false, false);
        RcsAttachmentSmileyPagerAdatper.setIsGroup(false);
        this.mAttachmentTypeSelectorAdapter.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RcsGroupChatComposeMessageFragment.this.addAttachment(((AttachmentListItem) ((AttachmentTypeSelectorAdapter) parent.getAdapter()).getItem(position)).getCommand(), replace);
            }
        });
        this.mAttachmentTypeSelectorAdapter.getAdapter().notifyDataSetChanged();
        pager.setAdapter(this.mAttachmentTypeSelectorAdapter.getAdapter());
        if (!this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
            this.mRcsGroupConversationInputManager.showHideMediaPicker(true, true);
            this.mIsAttachmentShow = true;
        }
        setPagerView(signView, pager);
    }

    private void setSmileyPagerAdapter(boolean replace, Context context) {
        ViewPager pager;
        ViewGroup signView;
        if (this.mSmileyFaceStub == null || (HwMessageUtils.isSplitOn() && (getActivity() instanceof ConversationList))) {
            View view;
            if (HwMessageUtils.isSplitOn() && (getActivity() instanceof ConversationList)) {
                if (((ConversationList) getActivity()).getSmileyFaceStub() == null) {
                    view = ((ConversationList) getActivity()).findSmileyFaceStub();
                } else {
                    view = ((ConversationList) getActivity()).getSmileyFaceView();
                }
                this.mSmileyFaceStub = ((ConversationList) getActivity()).getSmileyFaceStub();
            } else {
                this.mSmileyFaceStub = (ViewStub) this.mRootView.findViewById(R.id.smileyfaceview);
                view = this.mSmileyFaceStub.inflate();
            }
            pager = (ViewPager) view.findViewById(R.id.smiley_grid_pager);
            signView = (ViewGroup) view.findViewById(R.id.smiley_current_sign_view);
        } else {
            pager = (ViewPager) this.mRootView.findViewById(R.id.smiley_grid_pager);
            signView = (ViewGroup) this.mRootView.findViewById(R.id.smiley_current_sign_view);
            pager.removeAllViewsInLayout();
            signView.removeAllViews();
        }
        this.mSmileyFaceSelectorAdapter = null;
        RcsAttachmentSmileyPagerAdatper.setIsGroup(true);
        this.mSmileyFaceSelectorAdapter = new AttachmentSmileyPagerAdatper(getContext(), true, false);
        RcsAttachmentSmileyPagerAdatper.setIsGroup(false);
        LayoutParams layoutParam = this.mSmileyFaceStub.getLayoutParams();
        boolean isLand = getResources().getConfiguration().orientation == 2;
        boolean isInMultiWindow = isInMultiWindowMode();
        Resources resources = getResources();
        int i = (isLand || isInMultiWindow) ? R.dimen.smiley_panel_height_multiwindow : R.dimen.smiley_panel_height;
        layoutParam.height = (int) resources.getDimension(i);
        pager.getLayoutParams().height = (layoutParam.height - ((int) getResources().getDimension(R.dimen.sign_view_height))) - (((int) getResources().getDimension(R.dimen.attach_panel_spacing)) * 2);
        this.mSmileyFaceSelectorAdapter.setOnItemClickListener(this.mRichEditor);
        this.mSmileyFaceSelectorAdapter.getAdapter().notifyDataSetChanged();
        pager.setAdapter(this.mSmileyFaceSelectorAdapter.getAdapter());
        if (!this.mIsSmileyFaceShow) {
            this.mSmileyFaceStub.setVisibility(0);
            this.mIsSmileyFaceShow = true;
        }
        setPagerView(signView, pager);
    }

    private void setPagerView(ViewGroup signView, ViewPager pager) {
        if (signView != null && pager != null) {
            int count;
            signView.setVisibility(0);
            final float signViewItemHeight = getResources().getDimension(R.dimen.attachment_signview_height);
            final float signViewItemWidth = getResources().getDimension(R.dimen.attachment_signview_width);
            if (this.mIsSmileyFaceShow) {
                count = this.mSmileyFaceSelectorAdapter.getAdapter().getCount();
            } else {
                count = this.mAttachmentTypeSelectorAdapter.getAdapter().getCount();
            }
            int i = 0;
            while (i < count) {
                if (count < 2) {
                    signView.setVisibility(8);
                    break;
                } else {
                    signView.addView(new SignView(getContext(), pager.getCurrentItem() == i), (int) signViewItemWidth, (int) signViewItemHeight);
                    i++;
                }
            }
            final ViewGroup viewGroup = signView;
            pager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
                public void onPageSelected(int position) {
                    if (viewGroup != null) {
                        viewGroup.removeAllViews();
                        int i = 0;
                        while (i < count && count >= 2) {
                            viewGroup.addView(new SignView(RcsGroupChatComposeMessageFragment.this.getContext(), position == i), (int) signViewItemWidth, (int) signViewItemHeight);
                            i++;
                        }
                    }
                }
            });
        }
    }

    private void updateTextCounter() {
        if (this.mTextCounter == null) {
            MLog.v("RcsGroupChatComposeMessageFragment", "updateTextCounter:: mTextCounter is null, return");
            return;
        }
        this.mTextCounter.setText(String.valueOf(this.mRichEditor.getText().length()));
        adjustTextPadding();
    }

    public boolean onBackPressed() {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            this.mActionMode = null;
            return true;
        }
        if (this.mMsgListView.isInEditMode()) {
            this.mMsgListView.exitEditMode();
            updateTitle();
        } else if (HwMessageUtils.isSplitOn()) {
            return false;
        } else {
            pressBackKey();
        }
        return true;
    }

    private void pressBackKey() {
        exitGroupChatComposeMessageActivity(new Runnable() {
            public void run() {
                RcsGroupChatComposeMessageFragment.this.finishSelf(false);
            }
        });
    }

    private boolean isCursorValid() {
        Cursor cursor = this.mMsgListAdapter.getCursor();
        if (!cursor.isClosed() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
            return true;
        }
        Log.e("RcsGroupChatComposeMessageFragment", "Bad cursor.", new RuntimeException());
        return false;
    }

    private void resetCounter() {
        updateTextCounter();
        adjustTextPadding();
    }

    private void updateTitle() {
        updateInfoFromAdapter();
        updateComposeStartIcon();
    }

    private void updateTypingTitle(String name, boolean isShowComposing) {
        this.isShowComposing = isShowComposing;
        this.mComposeTypingString = name;
        if (this.mActionBarWhenSplit != null) {
            this.mActionBarWhenSplit.setTitle(TextUtils.isEmpty(this.mGroupName) ? getResources().getString(R.string.chat_topic_default) : this.mGroupName, this.mGroupNumber);
            if (isShowComposing) {
                this.mActionBarWhenSplit.setSubtitle(String.format(getContext().getResources().getString(R.string.label_contact_is_composing), new Object[]{name}));
                return;
            }
            this.mActionBarWhenSplit.setSubtitle(null);
        }
    }

    public void registerDefSmsAppChanged() {
        if (this.mDefSmsAppChangedReceiver == null) {
            this.mDefSmsAppChangedReceiver = new DefaultSmsAppChangedReceiver(new HwDefSmsAppChangedListener() {
                public void onDefSmsAppChanged() {
                    RcsGroupChatComposeMessageFragment.this.checkSmsEnable();
                }
            });
        }
        getContext().registerReceiver(this.mDefSmsAppChangedReceiver, new IntentFilter("com.huawei.mms.default_smsapp_changed"), permission.DEFAULTCHANGED_PERMISSION, null);
    }

    public void unRegisterDefSmsAppChanged() {
        if (this.mDefSmsAppChangedReceiver != null) {
            getContext().unregisterReceiver(this.mDefSmsAppChangedReceiver);
        }
    }

    private void checkSmsEnable() {
        boolean isSmsEnabled = MmsConfig.isSmsEnabled(getContext());
        if (isSmsEnabled != this.mIsSmsEnabled) {
            this.mIsSmsEnabled = isSmsEnabled;
            getActivity().invalidateOptionsMenu();
            this.mRichEditor.setFocusable(this.mIsSmsEnabled);
            onKeyboardStateChanged();
            updateSendButtonState();
            updateAddAttachView();
            updateTitle();
        }
    }

    private void updateAddAttachView() {
        View vAttach = this.mRootView.findViewById(R.id.add_attach);
        vAttach.setBackground(ResEx.self().getStateListDrawable(getContext(), R.drawable.btn_accessories));
        vAttach.getBackground().setAlpha(MessageUtils.getImageDisplyAlpha(this.mIsSmsEnabled));
        if (this.mIsSmsEnabled && !mIsInvalidGroupStatus && RcsProfile.canProcessGroupChat(this.mGroupID)) {
            vAttach.setClickable(true);
            vAttach.setEnabled(true);
            vAttach.setOnClickListener(this);
            return;
        }
        vAttach.setClickable(false);
        vAttach.setEnabled(false);
        if (this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
            this.mRcsGroupConversationInputManager.showHideMediaPicker(false, false);
            this.mIsAttachmentShow = false;
        }
    }

    private void resumeListAdd(Bundle bundle) {
        boolean issend = bundle.getBoolean("resume_issend");
        MLog.v("RcsGroupChatComposeMessageFragment", "issend: =" + issend);
        long sdkMsgId = bundle.getLong("resume_msgId");
        long msgId = RcsTransaction.getMmsMsgId(sdkMsgId, 2, getContext());
        if (sendlist.containsKey(Long.valueOf(msgId)) || receivelist.contains(Long.valueOf(msgId))) {
            MLog.v("RcsGroupChatComposeMessageFragment", "sendlist or  receivelist contains the same sdkMsgId ");
            return;
        }
        if (issend) {
            String sendpath = bundle.getString("resume_path");
            MLog.v("RcsGroupChatComposeMessageFragment", " ++++ sdkMsgId" + sdkMsgId + "+sendpath" + sendpath);
            sendlist.put(Long.valueOf(msgId), sendpath);
        } else {
            receivelist.add(Long.valueOf(msgId));
        }
    }

    private void resumeListSend(Bundle bundle) {
        try {
            for (Entry entry : sendlist.entrySet()) {
                String path = (String) entry.getValue();
                long msgId = ((Long) entry.getKey()).longValue();
                MLog.i("RcsGroupChatComposeMessageFragment", "resumeListSend path =  " + path);
                FileInfo info = RcsTransaction.getFileInfoByData(getContext(), path);
                if (info == null) {
                    MLog.e("RcsGroupChatComposeMessageFragment", " Can't find file represented by this Uri : ");
                    return;
                } else {
                    RcsTransaction.deleteChatGroup(msgId, 2, getContext());
                    RcsTransaction.preSendGroupFile(getContext(), info, this.mThreadID, this.mGroupID);
                }
            }
            for (int i = 0; i < receivelist.size(); i++) {
                RcsTransaction.acceptfile(((Long) receivelist.get(i)).longValue(), 2);
            }
            resumeListClear();
            MessagingNotification.cancelNotification(getContext(), 789);
        } catch (RuntimeException e) {
            MLog.e("RcsGroupChatComposeMessageFragment", "Method multiSend failed.");
        }
    }

    public void ftresumeStopDialog(Bundle bundle) {
        resumeListAdd(bundle);
        if (this.ftresumeStopDialog == null) {
            resumeDialog(bundle);
        } else if (!this.ftresumeStopDialog.isShowing()) {
            this.ftresumeStopDialog.setTitle(getContext().getResources().getString(R.string.file_fail));
            this.ftresumeStopDialog.setMessage(getContext().getResources().getString(R.string.CS_server_unavailable_message));
            this.ftresumeStopDialog.getButton(-1).setText(R.string.CS_retry);
            this.ftresumeStopDialog.getButton(-3).setText(R.string.rate_limit_surpassed);
            this.ftresumeStopDialog.show();
            this.ftresumeStopDialog.setCanceledOnTouchOutside(false);
        }
    }

    private void resumeDialog(final Bundle bundle) {
        this.ftresumeStopDialog = new Builder(getContext()).setTitle(R.string.file_fail).setMessage(R.string.CS_server_unavailable_message).setPositiveButton(R.string.CS_retry, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                RcsGroupChatComposeMessageFragment.this.resumeListSend(bundle);
            }
        }).setNeutralButton(R.string.rate_limit_surpassed, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                RcsGroupChatComposeMessageFragment.this.resumeListClear();
            }
        }).show();
        this.ftresumeStopDialog.setCanceledOnTouchOutside(false);
    }

    private void resumeListClear() {
        sendlist.clear();
        receivelist.clear();
    }

    private RcsFileTransGroupMessageListItem getMessageListItemById(long fileTransId, RcsGroupChatMessageListView mMsgListView) {
        int listSize = mMsgListView.getChildCount();
        for (int i = 0; i < listSize; i++) {
            RcsGroupChatMessageListItem rcsGroupChatMessageListItem = null;
            if (mMsgListView.getChildAt(i) instanceof RcsGroupChatMessageListItem) {
                rcsGroupChatMessageListItem = (RcsGroupChatMessageListItem) mMsgListView.getChildAt(i);
            }
            if (rcsGroupChatMessageListItem != null && rcsGroupChatMessageListItem.mFtGroupMsgListItem != null && rcsGroupChatMessageListItem.mFtGroupMsgListItem.getMessageItem() != null && rcsGroupChatMessageListItem.mFtGroupMsgListItem.getMessageItem().mMsgId == fileTransId) {
                return rcsGroupChatMessageListItem.mFtGroupMsgListItem;
            }
        }
        return null;
    }

    private void updateFileTransProgress(Bundle bundle) {
        long msgId = bundle.getLong("ft.msg_id");
        long totalSize = bundle.getLong("rcs.ft.progress.totalsize");
        long sendSize = bundle.getLong("rcs.ft.progress.currentsize");
        RcsFileTransGroupMessageListItem view = null;
        if (this.mMsgListAdapter.mListView != null) {
            view = getMessageListItemById(msgId, (RcsGroupChatMessageListView) this.mMsgListAdapter.mListView);
        }
        if (view != null && view.getMessageItem().mImAttachmentStatus == 1000) {
            RcsFileTransGroupMessageItem msgItem = view.getMessageItem();
            msgItem.mImAttachmentTransSize = sendSize;
            msgItem.mImAttachmentTotalSize = totalSize;
            msgItem.mImAttachmentStatus = 1000;
            view.refreshViewAnyStatus(msgItem);
        }
        RcsGroupChatMessageItem msgItem2 = this.mMsgListAdapter.getMessageFromCache(msgId);
        if (msgItem2 != null && msgItem2.mFtGroupMsgItem != null) {
            msgItem2.mFtGroupMsgItem.mImAttachmentTransSize = sendSize;
            msgItem2.mFtGroupMsgItem.mImAttachmentTotalSize = totalSize;
            msgItem2.mFtGroupMsgItem.mImAttachmentStatus = 1000;
        }
    }

    private void composingEventHandler(String msgPlusNumber, boolean rspEvent) {
        String number = NumberUtils.normalizeNumber(msgPlusNumber);
        String name = number;
        Contact contact = Contact.get(number, true);
        if (contact != null) {
            name = contact.getName();
            if (!contact.existsInDatabase() && RcsProfile.isGroupChatNicknameEnabled()) {
                String nickname = RcsProfile.getGroupMemberNickname(number, this.mThreadID);
                if (!TextUtils.isEmpty(nickname)) {
                    name = nickname;
                }
            }
        }
        updateTypingTitle(name, rspEvent);
    }

    private void updateThreadIdIfRunning() {
        if (this.mIsRunning && MessagingNotification.getRcsMessagingNotification() != null) {
            MessagingNotification.getRcsMessagingNotification().setCurrentlyDisplayedThreadId(this.mThreadID, 3);
        }
    }

    private void updateRcsMessagesDBAsRead(String groupId) {
        ContentResolver resolver = getContext().getContentResolver();
        Cursor cursor = null;
        int count = 0;
        long threadId = 0;
        try {
            cursor = SqliteWrapper.query(getContext(), resolver, Uri.parse("content://rcsim/rcs_groups"), null, "name = ?", new String[]{groupId}, null);
            if (cursor != null && cursor.getCount() == 1 && cursor.moveToFirst()) {
                threadId = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"));
                cursor.close();
                cursor = SqliteWrapper.query(getContext(), resolver, Uri.parse("content://rcsim/rcs_group_message"), new String[]{"read"}, "(type = 1 OR type = 101  OR type=4 OR type = 100)AND read = 0 AND thread_id = " + threadId, null, null);
                if (cursor != null) {
                    count = cursor.getCount();
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception exception) {
            Log.e("RcsGroupChatComposeMessageFragment", "IllegalStateException: unstableCount < 0: -1:" + exception);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (count != 0) {
            ContentValues values = new ContentValues(2);
            values.put("read", Integer.valueOf(1));
            values.put("seen", Integer.valueOf(1));
            SqliteWrapper.update(getContext(), resolver, Uri.parse("content://rcsim/rcs_group_message"), values, "(type = 1 OR type = 101 OR type=4  OR type = 100 ) AND read = 0 AND thread_id = " + threadId, null);
        }
    }

    private void markRcsGroupMessageAsRead(final boolean isUpdateNotifications) {
        final String groupId = this.mGroupID;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                boolean isConfirmUpdateNotification = false;
                if (isUpdateNotifications) {
                    Cursor cursor = null;
                    try {
                        cursor = SqliteWrapper.query(RcsGroupChatComposeMessageFragment.this.getContext(), RcsGroupChatComposeMessageFragment.this.mContentResolver, RcsGroupChatComposeMessageFragment.sMessageUri, null, "thread_id = ? and read = 0", new String[]{String.valueOf(RcsGroupChatComposeMessageFragment.this.mThreadID)}, null);
                        if (cursor != null && cursor.getCount() > 0) {
                            isConfirmUpdateNotification = true;
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (SQLiteException e) {
                        Log.e("RcsGroupChatComposeMessageFragment", "markRcsGroupMessageAsRead query error");
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                RcsGroupChatComposeMessageFragment.this.updateRcsMessagesDBAsRead(groupId);
                if (isConfirmUpdateNotification) {
                    MessagingNotification.blockingUpdateAllNotifications(RcsGroupChatComposeMessageFragment.this.getContext(), -2);
                }
            }
        }, "RcsGroupChatComposeMessageActivity.markRcsGroupMessageAsRead");
        thread.setPriority(1);
        thread.start();
    }

    public static boolean isExitRcsGroupEnable() {
        MLog.i("RcsGroupChatComposeMessageFragment FileTrans: ", "mIsInvalidGroupStatus : " + mIsInvalidGroupStatus);
        return mIsInvalidGroupStatus;
    }

    private void registerGroupChatStatusChangeBroadCast() {
        this.groupChatStatusChangeBroadCastReceiver = new GroupChatStatusChangeBroadCastReceiver();
        getContext().registerReceiver(this.groupChatStatusChangeBroadCastReceiver, new IntentFilter("com.huawei.rcs.message.groupcreated"), "com.huawei.rcs.RCS_BROADCASTER", null);
    }

    private void unRegisterGroupChatStatusChangeBroadCast() {
        if (this.groupChatStatusChangeBroadCastReceiver != null) {
            try {
                getContext().unregisterReceiver(this.groupChatStatusChangeBroadCastReceiver);
            } catch (Exception e) {
                MLog.d("RcsGroupChatComposeMessageFragment", "not regisiter receiver groupChatStatusChangeBroadCastReceiver");
            }
        }
    }

    private void registerGroupChatMemberChangeBroadCast() {
        this.groupChatMemberChangeBroadCastReceiver = new GroupChatMemberChangeBroadCastReceiver();
        getContext().registerReceiver(this.groupChatMemberChangeBroadCastReceiver, new IntentFilter("com.huawei.rcs.message.memberchanged"), "com.huawei.rcs.RCS_BROADCASTER", null);
    }

    private void unRegisterGroupChatMemberChangeBroadCast() {
        if (this.groupChatStatusChangeBroadCastReceiver != null) {
            getContext().unregisterReceiver(this.groupChatMemberChangeBroadCastReceiver);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mMenuEx.onOptionsItemSelected(item);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mMenuEx != null) {
            this.mMenuEx.setOptionMenu(menu).createOptionsMenu();
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (this.mMenuEx != null) {
            this.mMenuEx.setOptionMenu(menu).onPrepareOptionsMenu();
        }
    }

    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService("input_method");
        if (this.mRichEditor != null && inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(this.mRichEditor.getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
        if (inputMethodManager == null) {
            Log.e("RcsGroupChatComposeMessageFragment", "showKeyboard can't get inputMethodManager.");
            return;
        }
        Activity act = getActivity();
        if (act == null) {
            Log.e("RcsGroupChatComposeMessageFragment", "showKeyboard getActivity return null, so can not show KeyBoard");
            return;
        }
        View v = act.getCurrentFocus();
        if (v == null) {
            Log.e("RcsGroupChatComposeMessageFragment", "Can't show KeyBoard as no focus view.");
        } else {
            inputMethodManager.showSoftInput(v, 1);
        }
    }

    public void isLocationType() {
        if (RcsProfileUtils.getRcsMsgExtType(this.mMsgItem.mCursor) == 6) {
            this.mIsCanSelectAll = true;
            this.mIsCanForward = true;
            this.mIsCanCopyText = false;
            this.mIsCanDelete = true;
            this.mIsCanSeleteText = false;
            this.mIsCanSave = false;
        }
    }

    public void showPopupFloatingToolbar() {
        this.mActionMode = this.mMessageBlockView.startActionMode(new FloatingCallback2(), 1);
        this.mActionMode.hide(0);
    }

    private void showDeliveryReportForSingleMessage(String msgId, long msgTime) {
        Intent intent = new Intent(getContext(), RcsGroupChatDeliveryReportActivity.class);
        intent.putExtra("bundle_message_id", msgId);
        intent.putExtra("bundle_sent_time", msgTime);
        if (!HwMessageUtils.isSplitOn()) {
            startActivity(intent);
        } else if (getActivity() instanceof ConversationList) {
            Fragment fragment = new RcsGroupChatDeliveryReportFragment();
            fragment.setIntent(intent);
            ((ConversationList) getActivity()).changeRightAddToStack(fragment, (Fragment) this);
        }
    }

    AsyncDialog getAsyncDialog() {
        if (this.mAsyncDialog == null) {
            this.mAsyncDialog = new AsyncDialog(getActivity());
        }
        return this.mAsyncDialog;
    }

    private void confirmDeleteMultiDialog(DeleteGroupMulMessageListener listener) {
        String message;
        if (this.mIsAllSelected) {
            message = getResources().getString(R.string.whether_delete_all_messages);
        } else {
            message = getResources().getQuantityString(R.plurals.whether_delete_selected_messages_2, this.mMsgListView.getSelectedMsgItemsSize(), new Object[]{Integer.valueOf(this.mMsgListView.getSelectedMsgItemsSize())});
        }
        View contents = View.inflate(getActivity(), R.layout.delete_thread_dialog_view, null);
        ((TextView) contents.findViewById(R.id.tv_deleted_message)).setText(message);
        Builder builder = new Builder(getContext());
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.delete, listener);
        builder.setNegativeButton(R.string.no, null);
        builder.setView(contents);
        MessageUtils.setButtonTextColor(builder.show(), -1, getResources().getColor(R.color.mms_unread_text_color));
    }

    private void resetBottomScrollerHeight(boolean needReset) {
        LayoutParams params = this.mBottomScroller.getLayoutParams();
        if (needReset) {
            params.height = -2;
            this.mBottomScroller.setLayoutParams(params);
        }
        int scrollerHeight = this.mBottomScroller.getChildAt(0).getMeasuredHeight();
        if (this.mIsLandscape) {
            if (scrollerHeight > this.mBottomMaxHeightLandSpace) {
                params.height = this.mBottomMaxHeightLandSpace;
                this.mBottomScroller.setLayoutParams(params);
            }
        } else if (!this.mKeyboardHiden || this.mActionBarWillExpand) {
            if (scrollerHeight > this.mBottomMaxHeightPortrait) {
                params.height = this.mBottomMaxHeightPortrait;
                this.mBottomScroller.setLayoutParams(params);
            }
        } else if (scrollerHeight > this.mBottomMaxHeightPortraitNoKeyboard) {
            params.height = this.mBottomMaxHeightPortraitNoKeyboard;
            this.mBottomScroller.setLayoutParams(params);
        }
    }

    private void forwardMsg() {
        StringBuffer forwardMsgList = new StringBuffer();
        Cursor cursor = this.mMsgListAdapter.getCursor();
        if (cursor != null) {
            Integer[] selectedItems = this.mMsgListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions();
            int count = selectedItems.length;
            if (count >= 1) {
                RcsGroupChatMessageItem msgItem;
                if (1 == count) {
                    msgItem = this.mMsgListAdapter.getMessageItemWithIdAssigned(selectedItems[0].intValue(), cursor);
                    if (msgItem != null) {
                        forwardMessage(msgItem);
                    }
                    return;
                }
                Arrays.sort(selectedItems);
                int i = 0;
                while (i < count) {
                    Integer itemId = selectedItems[i];
                    if (itemId.intValue() >= 0) {
                        String forwardString = "";
                        msgItem = this.mMsgListAdapter.getMessageItemWithIdAssigned(itemId.intValue(), cursor);
                        if (msgItem != null) {
                            if (PreferenceUtils.getForwardMessageFrom(getContext())) {
                                if (msgItem.isOutgoingMessage()) {
                                    forwardString = getString(R.string.forward_from, new Object[]{getString(R.string.message_sender_from_self)});
                                } else if (TextUtils.isEmpty(msgItem.mContact)) {
                                    forwardString = getString(R.string.forward_from, new Object[]{msgItem.mAddress});
                                } else {
                                    forwardString = getString(R.string.forward_from, new Object[]{msgItem.mContact});
                                }
                                forwardString = forwardString + System.lineSeparator() + msgItem.mBody;
                            } else {
                                forwardString = msgItem.mBody;
                            }
                            forwardMsgList.append(forwardString + System.lineSeparator() + System.lineSeparator());
                        }
                        i++;
                    } else {
                        return;
                    }
                }
                String forwardMsg = forwardMsgList.toString();
                if (forwardMsg.length() > System.lineSeparator().length() * 2) {
                    forwardMsg = forwardMsg.substring(0, forwardMsg.length() - (System.lineSeparator().length() * 2));
                }
                if (this.mChatForwarder != null) {
                    this.mChatForwarder.setFragment(this);
                    this.mChatForwarder.launchContactsPicker(160127, forwardMsg);
                }
            }
        }
    }

    private void forwardMessage(RcsGroupChatMessageItem msgItem) {
        Intent intent = ComposeMessageActivity.createIntent(getContext(), 0);
        intent.putExtra("forwarded_message", true);
        intent.setClassName(getContext(), "com.android.mms.ui.ForwardMessageActivity");
        String forwardString = "";
        if (msgItem.isOutgoingMessage() || !PreferenceUtils.getForwardMessageFrom(getContext())) {
            forwardString = msgItem.mBody;
        } else {
            if (TextUtils.isEmpty(msgItem.mContact)) {
                forwardString = getString(R.string.forward_from, new Object[]{msgItem.mAddress});
            } else {
                forwardString = getString(R.string.forward_from, new Object[]{msgItem.mContact});
            }
            forwardString = forwardString + System.lineSeparator() + msgItem.mBody;
        }
        if (this.mChatForwarder != null) {
            this.mChatForwarder.setFragment(this);
            this.mChatForwarder.launchContactsPicker(160127, forwardString);
        }
    }

    private void shareMessage(RcsGroupChatMessageItem msgItem) {
        String mssageText = msgItem.mBody;
        String messageType = "text/plain";
        Uri messageUri = null;
        RcsFileTransGroupMessageItem rcsFileMsgItem = msgItem.mFtGroupMsgItem;
        if (rcsFileMsgItem != null) {
            switch (rcsFileMsgItem.mFileTransType) {
                case 7:
                    mssageText = "";
                    break;
                case 8:
                    mssageText = "";
                    break;
                case 9:
                    mssageText = "";
                    break;
                case 10:
                    mssageText = "";
                    break;
            }
            messageUri = ShareUtils.copyFile(getContext(), rcsFileMsgItem.getAttachmentFile());
        }
        ShareUtils.shareMessage(getContext(), messageUri, messageType, mssageText);
    }

    public void updateInputMode() {
        if (this.isPeeking || this.mRcsGroupConversationInputManager.isMediaPickerVisible()) {
            getActivity().getWindow().setSoftInputMode(2);
        } else if (this.mIsSmileyFaceShow) {
            getActivity().getWindow().setSoftInputMode(2);
        } else {
            int mode;
            if (getIntent() == null || !MmsCommon.isFromPeekReply(getIntent())) {
                mode = 18;
            } else {
                mode = 20;
            }
            getActivity().getWindow().setSoftInputMode(mode);
        }
    }

    private void saveGroupFileToPhone(RcsFileTransGroupMessageItem ftGroupMsgItem) {
        if (ftGroupMsgItem == null) {
            MLog.d("RcsGroupChatComposeMessageFragment", "ftGroupMsgItem is null!");
            return;
        }
        File attachmentFile = ftGroupMsgItem.getAttachmentFile();
        if (attachmentFile == null || !attachmentFile.exists()) {
            Toast.makeText(getContext(), R.string.text_file_not_exist, 0).show();
        } else if (!(ftGroupMsgItem.isVCardFileTypeMsg() || ftGroupMsgItem.isLocation())) {
            RcsTransaction.showFileSaveResult(getContext(), RcsMediaFileUtils.saveMediaFile(getContext(), attachmentFile));
        }
    }

    private void hideInputMode() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService("input_method");
        if (inputMethodManager != null && getActivity().getCurrentFocus() != null && getActivity().getCurrentFocus().getWindowToken() != null) {
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 2);
        }
    }

    public void onPreGroupChatMessageSent() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(this.mResetMessageRunnable);
        }
    }

    public void onFinishGroupChatFileSent() {
        getActivity().runOnUiThread(this.mResetFileRunnable);
    }

    private void resetMessage() {
        this.mRichEditor.setText(null);
        appendSignature(false);
    }

    private void resetFileSent() {
        this.mRichEditor.resetAttachmentMap();
        updateSendButtonState();
        this.mActionBarWhenSplit.show(true);
        onDraftChanged();
    }

    private void gotoGroupChatDetailActivity() {
        Intent intent = new Intent(getContext(), RcsGroupChatConversationDetailActivity.class);
        intent.putExtra("bundle_group_id", this.mGroupID);
        intent.putExtra("bundle_thread_id", this.mThreadID);
        intent.putExtra("bundle_rcs_thread_id", this.mRcsThreadId);
        intent.putExtra("bundle_group_name", this.mGroupName);
        if (!HwMessageUtils.isSplitOn()) {
            startActivityForResult(intent, SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE);
        } else if (getActivity() instanceof ConversationList) {
            HwBaseFragment fragment = new RcsGroupChatConversationDetailFragment();
            fragment.setIntent(intent);
            ((ConversationList) getActivity()).changeRightAddToStack(fragment, (HwBaseFragment) this);
        }
    }

    public boolean detectMessageToForwardForFt(Integer[] selection, Cursor cursor) {
        if (this.mForwarder == null) {
            return false;
        }
        this.mForwarder.setFragment(this);
        this.mForwarder.setMessageListAdapter(this.mMsgListAdapter);
        this.mForwarder.setMessageKind(2);
        return this.mForwarder.detectMessageToForwardForFt(selection, cursor);
    }

    public boolean detectMessageToForwardForLoc(Integer[] selection, Cursor cursor) {
        if (this.mForwarder == null) {
            return false;
        }
        this.mForwarder.setFragment(this);
        this.mForwarder.setMessageListAdapter(this.mMsgListAdapter);
        this.mForwarder.setMessageKind(2);
        return this.mForwarder.detectMessageToForwardForLoc(selection, cursor);
    }

    public void forwardLoc() {
        if (this.mForwarder != null) {
            this.mForwarder.forwardLoc();
        }
    }

    public void toForward() {
        MLog.e("RcsGroupChatComposeMessageFragment", "toForward. begin");
        if (this.mForwarder != null) {
            this.mForwarder.forwardFt();
        }
    }

    private void setLoginAndOwnerStatus() {
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (aMsgPlus != null) {
            try {
                this.mIsLogin = aMsgPlus.getLoginState();
                this.mIsOwner = aMsgPlus.isGroupOwner(this.mGroupID);
            } catch (RemoteException e) {
                MLog.e("RcsGroupChatComposeMessageFragment", "getLoginState or isGroupOwner fail");
            }
        }
        MLog.d("RcsGroupChatComposeMessageFragment", " mIsLogin = " + this.mIsLogin + "mIsOwner = " + this.mIsOwner);
    }

    public boolean getLoginStatus() {
        return this.mIsLogin;
    }

    public boolean getOwnerStatus() {
        return this.mIsOwner;
    }

    private void updateInviateGroupChatView() {
        if (this.mMsgListView == null || !this.mMsgListView.isInEditMode()) {
            if (mIsInvalidGroupStatus || this.isPeeking) {
                hideKeyboard();
                this.mBottomPanel.setVisibility(8);
            } else {
                this.mBottomPanel.setVisibility(0);
            }
        }
    }

    private String getSelectedGroupChatMessageBodies(Integer[] selectedItemsPosition, RcsGroupChatMessageListAdapter listAdapter) {
        StringBuffer msgsCopiedString = new StringBuffer();
        if (listAdapter == null || listAdapter.getCursor() == null || selectedItemsPosition == null) {
            MLog.w("RcsGroupChatComposeMessageFragment", "getSelectedGroupChatMessageBodies::nullPointer, return");
            return "";
        }
        int count = selectedItemsPosition.length;
        if (count < 1) {
            MLog.i("RcsGroupChatComposeMessageFragment", "getSelectedGroupChatMessageBodies::the select groupChat item is 0, return");
            return "";
        }
        Arrays.sort(selectedItemsPosition);
        for (int i = 0; i < count; i++) {
            RcsGroupChatMessageItem msgItem = listAdapter.getMessageItemWithIdAssigned(selectedItemsPosition[i].intValue(), listAdapter.getCursor());
            if (msgItem != null) {
                msgsCopiedString.append(msgItem.mBody);
                if (count - 1 != i) {
                    msgsCopiedString.append(System.lineSeparator());
                }
            }
        }
        return msgsCopiedString.toString();
    }

    private void cancelVideoCompressIfRunning() {
        if (!RcseCompressUtil.isCancelled() && RcseCompressUtil.isCompressRunning()) {
            cancelVideoCompress();
            Toast.makeText(getContext(), getString(R.string.text_compress_cancelled), 0).show();
        }
    }

    private void cancelVideoCompress() {
        ProgressDialog dialog = RcsTransaction.getProgressDialog();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        RcseCompressUtil.cancelVideoCompress();
    }

    public void reSend(final RcsGroupChatMessageItem mMessageItem) {
        if (mMessageItem != null) {
            if (!getLoginStatus()) {
                ResEx.makeToast((int) R.string.rcs_im_resend_error_message, 0);
            } else if (!isExitRcsGroupEnable() && RcsProfile.canProcessGroupChat(this.mGroupID)) {
                if (mMessageItem.mType == 100) {
                    new Thread(new Runnable() {
                        public void run() {
                            RcsGroupChatComposeMessageFragment.this.reSendFt(RcsGroupChatComposeMessageFragment.this.mGroupID, mMessageItem);
                        }
                    }).start();
                } else {
                    try {
                        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
                        boolean isLocationType = RcsMapLoader.isLocItem(mMessageItem.mBody);
                        if (aMsgPlus != null) {
                            if (isLocationType) {
                                aMsgPlus.resendGroupMessageLocation(Long.parseLong(mMessageItem.mMsgId), this.mGroupID);
                            } else {
                                deleteGroupChatMessageByGlobalId(mMessageItem.mGlobalID);
                                RcsTransaction.toSendGroupMessage(getContext(), this.mGroupID, mMessageItem.mBody);
                            }
                        }
                    } catch (RemoteException e) {
                        MLog.e("RcsGroupChatComposeMessageFragment", "RcsProfile error");
                    }
                }
            }
        }
    }

    private void reSendFt(String GroupID, RcsGroupChatMessageItem mMessageItem) {
        Bundle bundle = new Bundle();
        bundle.putString("file_type", mMessageItem.mFtGroupMsgItem.mImAttachmentContentType);
        bundle.putString("file_content", mMessageItem.mFtGroupMsgItem.mImAttachmentContent);
        bundle.putLong("file_size", mMessageItem.mFtGroupMsgItem.mImAttachmentTotalSize);
        bundle.putString("file_name", mMessageItem.mFtGroupMsgItem.mImAttachmentPath);
        MLog.i("RcsGroupChatComposeMessageFragment FileTrans: ", "message_resend msgID" + mMessageItem.mFtGroupMsgItem.mMsgId);
        if (new File(mMessageItem.mFtGroupMsgItem.mImAttachmentPath).exists()) {
            try {
                if (!isExitRcsGroupEnable()) {
                    RcsTransaction.resendMessageFile(mMessageItem.mFtGroupMsgItem.mMsgId, 2, getContext(), mMessageItem.mFtGroupMsgItem.mImAttachmentGlobalTransId);
                }
            } catch (RuntimeException e) {
                MLog.e("RcsGroupChatComposeMessageFragment", "resendMessageFile error");
            }
        } else if (this.mHandler != null) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(RcsGroupChatComposeMessageFragment.this.getContext(), R.string.groupchat_resend_file_exit, 0).show();
                }
            });
        }
    }

    private void deleteGroupChatMessageByGlobalId(String mGlobalID) {
        Uri.Builder builder = Uri.parse("content://rcsim/rcs_group_message/").buildUpon().appendPath(mGlobalID);
        MLog.d("RcsGroupChatComposeMessageFragment", "deleteGroupChatMessageByGlobalId uri = " + builder.build());
        SqliteWrapper.delete(getContext(), this.mContentResolver, builder.build(), null, null);
    }

    private void initFullScreenView() {
        boolean isSmsEnabled = MmsConfig.isSmsEnabled(getContext());
        this.mFullScreenEdit = (ImageButton) this.mRootView.findViewById(R.id.btn_full_screen);
        this.mFullScreenEdit.setBackground(ResEx.self().getStateListDrawable(getContext(), R.drawable.full_screen_edit_selector));
        if (isSmsEnabled) {
            this.mFullScreenEdit.setClickable(true);
            this.mFullScreenEdit.setEnabled(true);
            this.mFullScreenEdit.setOnClickListener(this);
            return;
        }
        this.mFullScreenEdit.setClickable(false);
        this.mFullScreenEdit.setEnabled(false);
    }

    private void updateEmojiAddView() {
        this.mEmojiAdd = (ImageButton) this.mRootView.findViewById(R.id.add_emojis);
        if (this.mIsSmileyFaceShow) {
            this.mEmojiAdd.setBackground(ResEx.self().getStateListDrawable(getContext(), R.drawable.ic_enter_emoji_expression_checked));
        } else {
            this.mEmojiAdd.setBackground(ResEx.self().getStateListDrawable(getContext(), R.drawable.ic_enter_emoji_expression));
        }
        if (!isInMultiWindowMode() || MessageUtils.getScreenHeight(getActivity()) > getContext().getResources().getDimensionPixelSize(R.dimen.multi_window_mode_do_not_show_attchment_or_emoji_min_height)) {
            this.mEmojiAdd.setClickable(true);
            this.mEmojiAdd.setEnabled(true);
            this.mEmojiAdd.setOnClickListener(this);
            return;
        }
        this.mEmojiAdd.setClickable(false);
        this.mEmojiAdd.setEnabled(false);
        this.mEmojiAdd.setOnClickListener(null);
    }

    private void showEnableFullScreenIcon() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                if (!RcsGroupChatComposeMessageFragment.this.isDetached()) {
                    if (RcsGroupChatComposeMessageFragment.this.mRichEditor.getLineNumber() > 2) {
                        RcsGroupChatComposeMessageFragment.this.showFullScreenButton();
                    } else if (RcsGroupChatComposeMessageFragment.this.mRichEditor.getLineNumber() == 2 && RcsGroupChatComposeMessageFragment.this.isFirstCheck) {
                        RcsGroupChatComposeMessageFragment.this.isFirstCheck = false;
                        RcsGroupChatComposeMessageFragment.this.showEnableFullScreenIcon();
                    } else {
                        RcsGroupChatComposeMessageFragment.this.hideFullScreenButton();
                    }
                }
            }
        }, 60);
    }

    private void showFullScreenButton() {
        if (this.mFullScreenEdit != null) {
            this.mFullScreenEdit.setVisibility(0);
        }
    }

    private void hideFullScreenButton() {
        if (this.mFullScreenEdit != null) {
            this.mFullScreenEdit.setVisibility(8);
        }
    }

    public void controlNeedSendComposing(boolean needSend) {
        this.mIsNeedSendComposing = needSend;
    }

    private void registerRcsLoginStatusChangeBroadCast() {
        if (this.mRcsLoginStatusChangeBroadCastReceiver == null) {
            this.mRcsLoginStatusChangeBroadCastReceiver = new RcsLoginStatusChangeBroadCastReceiver();
        }
        getContext().registerReceiver(this.mRcsLoginStatusChangeBroadCastReceiver, new IntentFilter("com.huawei.rcs.loginstatus"), "com.huawei.rcs.RCS_BROADCASTER", null);
    }

    private void unRegisterRcsLoginStatusChangeBroadCast() {
        if (this.mRcsLoginStatusChangeBroadCastReceiver != null) {
            try {
                getContext().unregisterReceiver(this.mRcsLoginStatusChangeBroadCastReceiver);
            } catch (Exception e) {
                MLog.e("RcsGroupChatComposeMessageFragment", "not regisiter receiver mRcsLoginStatusChangeBroadCastReceiver e: " + e);
            }
        }
    }

    public void replyCurrentConversation(boolean showSoftInput) {
        Intent intent = new Intent(getContext(), RcsGroupChatComposeMessageActivity.class);
        intent.putExtra("bundle_group_id", this.mGroupID);
        intent.putExtra("needRecreate", true);
        intent.putExtra("EXTRA_VALUE_PEEK_REPLY", showSoftInput);
        getContext().startActivity(intent);
    }

    private RcsGroupChatComposeMessageActivity getRcsGroupChatComposeMessageActivity() {
        return (RcsGroupChatComposeMessageActivity) getActivity();
    }

    protected void updateInfoFromAdapter() {
        int i = 0;
        String title = this.mActionbarAdapter.getName();
        this.mActionBarWhenSplit.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
            public void onClick(View v) {
                RcsGroupChatComposeMessageFragment.this.getActivity().onBackPressed();
            }
        });
        this.mActionBarWhenSplit.showMenu(false);
        AbstractEmuiActionBar abstractEmuiActionBar = this.mActionBarWhenSplit;
        if (!this.isShowComposing) {
            i = this.mGroupNumber;
        }
        abstractEmuiActionBar.setTitle(title, i);
        if (getActivity() != null) {
            this.mActionBarWhenSplit.setEndIcon(true, getActivity().getResources().getDrawable(mIsInvalidGroupStatus ? R.drawable.ic_contact_no_group_unenable : R.drawable.ic_contact_no_group), new OnClickListener() {
                public void onClick(View v) {
                    RcsGroupChatComposeMessageFragment.this.mActionbarAdapter.viewPeopleInfo();
                }
            });
        }
    }

    protected void setCustomMenuClickListener() {
        if (!HwMessageUtils.isSplitOn() && this.mIsLandscape) {
            this.mActionBarWhenSplit.getSplitActionBarView().setOnCustomMenuListener(this.mMenuEx);
        }
        this.mSplitActionBar.setOnCustomMenuListener(this.mMenuEx);
    }

    protected Menu getResetMenu() {
        if (HwMessageUtils.isSplitOn() || !this.mIsLandscape || isInMultiWindowMode()) {
            return this.mSplitActionBar.getMenu();
        }
        return this.mActionBarWhenSplit.getMenu();
    }

    protected void refreshMenu() {
        if (!HwMessageUtils.isSplitOn() && this.mIsLandscape) {
            this.mActionBarWhenSplit.refreshMenu();
        }
        this.mSplitActionBar.refreshMenu();
    }

    protected void enterEditUpdate() {
        int i;
        boolean z;
        if (getActivity() instanceof ConversationList) {
            ((ConversationList) getActivity()).showOrHideLeftCover();
        }
        this.mMenuEx.onPrepareOptionsMenu();
        SplitActionBarView splitActionBarView = this.mSplitActionBar;
        if (HwMessageUtils.isSplitOn() || !this.mIsLandscape || isInMultiWindowMode()) {
            i = 0;
        } else {
            i = 8;
        }
        splitActionBarView.setVisibility(i);
        AbstractEmuiActionBar abstractEmuiActionBar = this.mActionBarWhenSplit;
        if (HwMessageUtils.isSplitOn() || !this.mIsLandscape || isInMultiWindowMode()) {
            z = false;
        } else {
            z = true;
        }
        abstractEmuiActionBar.showMenu(z);
        this.mActionBarWhenSplit.setTitleGravityCenter(true);
        this.mActionBarWhenSplit.showEndIcon(false);
        this.mActionBarWhenSplit.setStartIcon(true, R.drawable.mms_ic_cancel_dark);
        this.mMsgListView.startMultiChoice();
        this.mAttachmentPreview.setVisibility(8);
    }

    protected void exitEditUpdate() {
        if (getActivity() instanceof ConversationList) {
            ((ConversationList) getActivity()).showOrHideLeftCover();
        }
        this.mSplitActionBar.setVisibility(8);
        this.mActionBarWhenSplit.showMenu(false);
        this.mActionBarWhenSplit.setTitleGravityCenter(false);
        this.mActionBarWhenSplit.showEndIcon(true);
        refreshAttachmentPreviewState();
    }

    private void refreshAttachmentPreviewState() {
        if (this.mRichEditor == null || this.mRichEditor.getMediaModelData() == null || this.mRichEditor.getMediaModelData().size() == 0) {
            this.mAttachmentPreview.setVisibility(8);
        } else {
            this.mAttachmentPreview.setVisibility(0);
        }
    }

    protected void updateComposeTitle(int size) {
        this.mActionBarWhenSplit.setTitle(getActivity().getResources().getString(size == 0 ? R.string.no_selected : R.string.has_selected), size);
        this.mActionBarWhenSplit.setSubtitle(null);
    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("RcsGroupChatComposeMessageFragment", "onHiddenChanged -> hidden : " + hidden);
        int requestCode = ((ConversationList) getActivity()).getSplitRequestCode();
        if (!hidden && requestCode != -1) {
            onActivityResult(requestCode, ((ConversationList) getActivity()).getSplitResultCode(), ((ConversationList) getActivity()).getSplitIntent());
            ((ConversationList) getActivity()).resetSplitResultData();
        }
    }

    private void updateComposeStartIcon() {
        if (!this.mMsgListView.isInEditMode()) {
            boolean z;
            boolean isSplitState = false;
            if (getActivity() instanceof ConversationList) {
                isSplitState = ((ConversationList) getActivity()).isSplitState();
            }
            AbstractEmuiActionBar abstractEmuiActionBar = this.mActionBarWhenSplit;
            if (isSplitState) {
                z = false;
            } else {
                z = true;
            }
            abstractEmuiActionBar.showStartIcon(z);
        }
    }

    private void setSendMessageButtonEnable(boolean enable) {
        if (enable) {
            this.mIsRcsEnable = true;
            this.mSendButtonSms.setBackground(ResEx.self().getStateListDrawable(getContext(), this.mRcsAudioMessage.getViewBackGroud(R.drawable.ic_send_message_rcs)));
        } else {
            this.mIsRcsEnable = false;
            this.mSendButtonSms.setBackground(ResEx.self().getStateListDrawable(getContext(), R.drawable.ic_send_message_disable));
        }
        this.mSendButtonSms.setEnabled(enable);
    }

    private boolean isInMultiWindowMode() {
        if (getActivity() == null) {
            return false;
        }
        return getActivity().isInMultiWindowMode();
    }

    public void onRcsGroupChatRichAttachmentChanged(int changedType) {
        Message message = this.mHandler.obtainMessage(1006);
        message.arg1 = changedType;
        this.mHandler.sendMessageDelayed(message, 300);
    }

    private void refreshAttachmentChangedUI(int attachmentType) {
        onDraftChanged();
        updateSendButtonState();
        doMediaUpdate(attachmentType);
    }

    private void onDraftChanged() {
        if (this.mRcsGroupConversationInputManager != null) {
            this.mAttachmentPreview.onAttachmentsChanged(this.mRcsGroupConversationInputManager.getMediaPickerFullScreenState());
        } else {
            this.mAttachmentPreview.onAttachmentsChanged(false);
        }
    }

    private void handleInsertCalendar() {
        if (OsUtil.hasCalendarPermission()) {
            startCalendarActivity();
            return;
        }
        MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
        OsUtil.requestPermission(getActivity(), new String[]{"android.permission.READ_CALENDAR"}, 158);
    }

    private void startCalendarActivity() {
        startActivityForResult(new Intent("com.huawei.action.MESSAGE_EVENTS"), 1145);
    }

    public boolean editorHasText() {
        return this.mRichEditor != null ? this.mRichEditor.hasText() : false;
    }
}
