package com.android.mms.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment.InstantiationException;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Parcelable;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.Settings.System;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;
import android.view.ActionMode;
import android.view.ActionMode.Callback2;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import cn.com.xy.sms.sdk.HarassNumberUtil;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.db.TrainManager;
import cn.com.xy.sms.sdk.mms.ui.menu.ISmartSmsUIHolder;
import cn.com.xy.sms.sdk.mms.ui.menu.SmartSmsComposeManager;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
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
import com.android.mms.attachment.utils.ContentType;
import com.android.mms.data.Contact;
import com.android.mms.data.Contact.UpdateListener;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.Conversation.Cache;
import com.android.mms.data.Conversation.ConversationQueryHandler;
import com.android.mms.data.HwCustConversation.ParmWrapper;
import com.android.mms.data.WorkingMessage;
import com.android.mms.data.WorkingMessage.MessageStatusListener;
import com.android.mms.drm.DrmUtils;
import com.android.mms.model.AudioModel;
import com.android.mms.model.CarrierContentRestriction;
import com.android.mms.model.ImageModel;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.VcardModel;
import com.android.mms.model.VideoModel;
import com.android.mms.pdu.HwCustPduPersister;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.MmsMessageSender;
import com.android.mms.transaction.SmsMessageSender;
import com.android.mms.transaction.WapPushMsg;
import com.android.mms.ui.BaseConversationListFragment.DeleteThreadListener;
import com.android.mms.ui.CryptoComposeMessage.ICryptoComposeHolder;
import com.android.mms.ui.CryptoMessageListAdapter.CryptoMessageViewListener;
import com.android.mms.ui.MessageItem.DeliveryStatus;
import com.android.mms.ui.MessageListAdapter.ColumnsMap;
import com.android.mms.ui.MessageListAdapter.OnDataSetChangedListener;
import com.android.mms.ui.MessageListView.ICustMessageListHodler;
import com.android.mms.ui.MessageListView.IMessageListHodler;
import com.android.mms.ui.MessageListView.OnSizeChangedListener;
import com.android.mms.ui.RichMessageEditor.RichAttachmentListener;
import com.android.mms.ui.RichMessageEditor.UpdateSubjectReViewListener;
import com.android.mms.ui.twopane.LeftPaneConversationListFragment;
import com.android.mms.ui.views.CommonLisener;
import com.android.mms.ui.views.CommonLisener.HideKeyboardTouchListener;
import com.android.mms.ui.views.ComposeBottomView;
import com.android.mms.ui.views.ComposeBottomView.IBottomHolder;
import com.android.mms.ui.views.ComposeBottomView.IComposeBottomViewHolder;
import com.android.mms.ui.views.ComposeChoosePanel;
import com.android.mms.ui.views.ComposeChoosePanel.IChoosePanelHoler;
import com.android.mms.ui.views.ComposeRecipientsView;
import com.android.mms.ui.views.ComposeRecipientsView.IRecipientsHoler;
import com.android.mms.util.ItemLayoutCallback;
import com.android.mms.util.ShareUtils;
import com.android.mms.util.VcardMessageHelper;
import com.android.mms.widget.MmsWidgetProvider;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.LinkerTextTransfer;
import com.android.rcs.ui.RcsComposeMessage;
import com.android.rcs.ui.RcsComposeMessage.IHwCustComposeMessageCallback;
import com.android.rcs.ui.RcsMessageListAdapter;
import com.android.vcard.VCardComposer;
import com.google.android.gms.Manifest.permission;
import com.google.android.gms.R;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPart;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.AsyncQueryHandlerEx;
import com.huawei.cspcommon.ex.HandlerEx;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.harassmentinterception.service.BlacklistCommonUtils;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.service.NameMatchResult;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.EditTextWithSmiley;
import com.huawei.mms.ui.EmuiListViewListener;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.HwComposeBottomEditView.ScrollableCallback;
import com.huawei.mms.ui.MmsEmuiActionBar;
import com.huawei.mms.ui.MultiModeListView.EditHandler;
import com.huawei.mms.ui.PeopleActionBar.AddWhatsAppPeopleActionBarAdapter;
import com.huawei.mms.ui.SplitActionBarView;
import com.huawei.mms.ui.SplitActionBarView.OnCustomMenuListener;
import com.huawei.mms.util.ActivityExWrapper;
import com.huawei.mms.util.DefaultSmsAppChangedReceiver;
import com.huawei.mms.util.DefaultSmsAppChangedReceiver.HwDefSmsAppChangedListener;
import com.huawei.mms.util.DelaySendManager;
import com.huawei.mms.util.DocumentsUIUtil;
import com.huawei.mms.util.FloatMmsRequsetReceiver;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwNumberMatchUtils;
import com.huawei.mms.util.Log;
import com.huawei.mms.util.MmsCommon;
import com.huawei.mms.util.MmsPduUtils;
import com.huawei.mms.util.MmsRadarInfoManager;
import com.huawei.mms.util.MmsScaleSupport.SacleListener;
import com.huawei.mms.util.PrivacyModeReceiver.ModeChangeListener;
import com.huawei.mms.util.PrivacyModeReceiver.PrivacyStateListener;
import com.huawei.mms.util.ProviderCallUtils.CallRequest;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.SelectionChangedListener;
import com.huawei.mms.util.SimCursorManager;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.ui.RcsAudioMessage;
import com.huawei.rcs.ui.RcsFileTransMessageItem;
import com.huawei.rcs.utils.RcseMmsExt;
import com.huawei.rcs.utils.map.abs.RcsMapFragment;
import com.huawei.rcs.utils.map.abs.RcsMapLoaderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Pattern;

@SuppressLint({"NewApi"})
public class ComposeMessageFragment extends HwBaseFragment implements OnClickListener, OnEditorActionListener, MessageStatusListener, UpdateListener, ISmartSmsUIHolder, RichAttachmentListener {
    private static final Uri COPY_TO_SIM1_URI = Uri.parse("content://sms/copytoicc1");
    private static final Uri COPY_TO_SIM2_URI = Uri.parse("content://sms/copytoicc2");
    private static final Uri COPY_TO_SIM_URI = Uri.parse("content://sms/addtoicc");
    private static final boolean HAS_DRM_CONFIG = SystemProperties.get("ro.huawei.cust.oma_drm", "false").equals("true");
    private static final Uri THREAD_ID_CONTENT_URI = Uri.parse("content://mms-sms/threadID");
    private static final Uri UNDELIVERED_URI = Uri.parse("content://mms-sms/undelivered");
    private static final String mAudioUri = Media.getContentUri("external").toString();
    private static final String mImageUri = Images.Media.getContentUri("external").toString();
    private static final String mLocationUri = Images.Media.getContentUri("external").toString();
    private static final String mVideoUri = Video.Media.getContentUri("external").toString();
    private AttachmentSelectLocation attSelectLocation;
    private boolean isFirstCheck = true;
    private boolean isProcessingBackKeyEvent = false;
    private boolean isSimReply;
    ModeChangeListener localPrivacyMonitor = null;
    protected AbstractEmuiActionBar mActionBarWhenSplit;
    public ActionMode mActionMode = null;
    protected ConversationActionBarAdapter mActionbarAdapter = null;
    private boolean mActivityHasFocused = false;
    private Intent mAddContactIntent;
    private AsyncDialog mAsyncDialog;
    private AttachmentPreview mAttachmentPreview;
    private BackgroundQueryHandler mBackgroundQueryHandler;
    private int mBottomPanalMinHeight;
    private View mBottomView;
    private long mClickTimeWhenTooManyUnsentMsg = -1;
    private int mComposeBottomLayoutPaddingTopBottom;
    private ComposeBottomView mComposeBottomView;
    protected ComposeChoosePanel mComposeChoosePanel;
    private HwComposeCustHolder mComposeHolder;
    private LinearLayout mComposeLayoutGroup;
    private View mComposeMessageView;
    protected ComposeRecipientsView mComposeRecipientsView;
    ContentObserver mContactChangeListener = new ContentObserver(new Handler()) {
        public void onChange(boolean updated) {
            HwBackgroundLoader.getUIHandler().removeCallbacks(ComposeMessageFragment.this.mUpdateContactRunner);
            HwBackgroundLoader.getUIHandler().postDelayed(ComposeMessageFragment.this.mUpdateContactRunner, 500);
        }
    };
    private ContentResolver mContentResolver;
    protected Conversation mConversation;
    private ConversationInputHostHolder mConversationInputHostHolder = new ConversationInputHostHolder();
    protected ConversationInputManager mConversationInputManager;
    private boolean mCreateVCardFile = false;
    private CryptoComposeMessage mCryptoCompose = new CryptoComposeMessage();
    private boolean mCryptoToastIsShow = false;
    private final OnDataSetChangedListener mDataSetChangedListener = new OnDataSetChangedListener() {
        public void onDataSetChanged(MessageListAdapter adapter) {
            ComposeMessageFragment.this.mPossiblePendingNotification = true;
        }

        public void onContentChanged(MessageListAdapter adapter) {
            ComposeMessageFragment.this.mIsContentChanged = true;
            ComposeMessageFragment.this.startMsgListQueryDelayed(200);
        }
    };
    private String mDebugRecipients;
    DefaultSmsAppChangedReceiver mDefSmsAppChangedReceiver = null;
    private AlertDialog mDetalDialog = null;
    private DirtyModel mDirtyModel;
    private AlertDialog mDiscardDraftAlertDialog;
    private short mEditLayoutShowStatu = (short) 0;
    private boolean mEmailCheckIsShowing = false;
    private ImageButton mEmojiAdd;
    private boolean mForwardMessageMode;
    private boolean mFromStop = false;
    public int mFullScreenButtonState = 118;
    private ImageButton mFullScreenEdit;
    private final HandlerEx mHandler = new HandlerEx() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (ComposeMessageFragment.this.mMultiUris != null && ComposeMessageFragment.this.mUriPostion < ComposeMessageFragment.this.mMultiUris.size()) {
                        Parcelable uri = (Parcelable) ComposeMessageFragment.this.mMultiUris.get(ComposeMessageFragment.this.mUriPostion);
                        if (uri != null) {
                            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault()));
                            if (TextUtils.isEmpty(type) && DocumentsUIUtil.isDownloadsDocument((Uri) uri)) {
                                ContentResolver cR = ComposeMessageFragment.this.getContext().getContentResolver();
                                String typeTemp = cR != null ? cR.getType((Uri) uri) : type;
                                MLog.d("Mms_UI_CMA", "typeTemp = " + typeTemp);
                                type = typeTemp;
                            }
                            ComposeMessageFragment composeMessageFragment = ComposeMessageFragment.this;
                            if (type == null) {
                                type = ComposeMessageFragment.this.mMineType;
                            }
                            composeMessageFragment.addAttachment(type, (Uri) uri, true);
                            composeMessageFragment = ComposeMessageFragment.this;
                            composeMessageFragment.mUriPostion = composeMessageFragment.mUriPostion + 1;
                            break;
                        }
                    }
                    break;
                case 2:
                    ComposeMessageFragment.this.mUriPostion = 0;
                    ComposeMessageFragment.this.mMultiUris = null;
                    break;
                case 1000:
                    removeMessages(1000);
                    ComposeMessageFragment.this.updateSendButtonView();
                    ComposeMessageFragment.this.showEnableFullScreenIcon();
                    if (ComposeMessageFragment.this.mHwCustComposeMessage != null) {
                        ComposeMessageFragment.this.mHwCustComposeMessage.setOnePageSmsText(ComposeMessageFragment.this.mRichEditor.getWorkingMessage(), ComposeMessageFragment.this.mRichEditor);
                    }
                    if (ComposeMessageFragment.this.mRcsComposeMessage != null) {
                        ComposeMessageFragment.this.mRcsComposeMessage.onEditTextChange(ComposeMessageFragment.this.mRichEditor.getWorkingMessage().getText());
                        break;
                    }
                    break;
                case 1001:
                    boolean mms = msg.arg1 == 1;
                    if (!ComposeMessageFragment.this.mToastToMms) {
                        ComposeMessageFragment.this.mToastToMms = true;
                    } else if ((MmsConfig.getCustMmsConfig() == null || MmsConfig.getCustMmsConfig().isNotifyMsgtypeChangeEnable(true)) && !RcseMmsExt.isRcsMode()) {
                        int resId;
                        if (mms) {
                            resId = R.string.converting_to_picture_message_Toast;
                        } else {
                            resId = R.string.converting_to_text_message_Toast;
                        }
                        ResEx.makeToast(resId, 0);
                        ComposeMessageFragment.this.saveDraftWhenProtocolChange(mms);
                    }
                    ComposeMessageFragment.this.mCryptoCompose.onProtocolChanged(mms, ComposeMessageFragment.this.mRichEditor);
                    ComposeMessageFragment.this.updateSendButtonView();
                    ComposeMessageFragment.this.showEnableFullScreenIcon();
                    break;
                case 1002:
                    ComposeMessageFragment.this.refreshAttachmentChangedUI(msg.arg1);
                    break;
                case 9527:
                    ComposeMessageFragment.this.startMsgListQuery();
                    break;
                case 9801:
                    if (ComposeMessageFragment.this.isAdded()) {
                        if (!(ComposeMessageFragment.this.mComposeChoosePanel == null || msg.obj == null)) {
                            ComposeMessageFragment.this.mComposeChoosePanel.onMulitWindowChanged(Boolean.parseBoolean(msg.obj.toString()));
                        }
                        if (ComposeMessageFragment.this.mMenuEx != null && ComposeMessageFragment.this.isInLandscape()) {
                            ComposeMessageFragment.this.mMenuEx.onPrepareOptionsMenu();
                        }
                        if (!HwMessageUtils.isSplitOn()) {
                            ComposeMessageFragment.this.exitPeekingStatus();
                            break;
                        }
                    }
                    break;
                case 9802:
                case 9803:
                case 9804:
                    if (ComposeMessageFragment.this.isAdded()) {
                        ComposeMessageFragment.this.showEnableFullScreenIcon();
                        break;
                    }
                    break;
                case 9805:
                    HwMessageUtils.showKeyboard(ComposeMessageFragment.this.getActivity(), ComposeMessageFragment.this.mRichEditor);
                    break;
                case 9806:
                    ComposeMessageFragment.this.mSmartSmsComponse.getMenuRootView().setVisibility(8);
                    ComposeMessageFragment.this.mSmartSmsComponse.getButtonToSmartMenu().setVisibility(0);
                    ComposeMessageFragment.this.mSmartSmsComponse.getMenuRootView().setVisibility(8);
                    break;
                case 10000:
                    ComposeMessageFragment.this.mCryptoCompose.updateUIOnRecipientsChanged();
                    break;
                default:
                    handleListItemMsg(msg);
                    return;
            }
        }

        private void handleListItemMsg(Message msg) {
            Cursor cursor = null;
            MessageItem msgItem = msg.obj;
            if (msgItem != null && !ComposeMessageFragment.this.mMsgListView.isDragListenerCalled()) {
                switch (msg.what) {
                    case 1000101:
                        ComposeMessageFragment.this.editMessageItem(msgItem);
                        break;
                    case 1000102:
                        switch (msgItem.mAttachmentType) {
                            case 0:
                            case 2:
                            case 3:
                            case 4:
                                if (!(msgItem.isSms() || msgItem.isRcsChat())) {
                                    ComposeMessageFragment.this.mCryptoToastIsShow = true;
                                    MessageUtils.viewMmsMessageAttachment(ComposeMessageFragment.this, msgItem.mMessageUri, msgItem.mSlideshow, 132, ComposeMessageFragment.this.getAsyncDialog());
                                    break;
                                }
                            case 1:
                                if (!(msgItem.isSms() || msgItem.isRcsChat())) {
                                    MessageUtils.viewSimpleSlideshow(ComposeMessageFragment.this.getContext(), msgItem.mSlideshow);
                                    break;
                                }
                            default:
                                break;
                        }
                    case 1000103:
                        ComposeMessageFragment.this.showMessageDetails(msgItem);
                        break;
                    case 1000104:
                        Intent intent = new Intent(ComposeMessageFragment.this.getContext(), GroupSmsDetailsActivity.class);
                        intent.putExtra("group_id", msgItem.mUid);
                        intent.putExtra("thread_id", ComposeMessageFragment.this.mConversation.getThreadId());
                        ComposeMessageFragment.this.mCryptoToastIsShow = true;
                        boolean isLastItem = false;
                        if (ComposeMessageFragment.this.mMsgListAdapter != null) {
                            cursor = ComposeMessageFragment.this.mMsgListAdapter.getCursor();
                        }
                        if (cursor != null) {
                            isLastItem = cursor.getCount() == 1;
                            MLog.d("Mms_UI_CMA", "is last item : " + isLastItem);
                        }
                        intent.putExtra("is_last_item", isLastItem);
                        if (!HwMessageUtils.isSplitOn()) {
                            ComposeMessageFragment.this.startActivityForResult(intent, 131);
                            break;
                        }
                        HwBaseFragment groupSmsDetailsFragment = new GroupSmsDetailsFragment();
                        groupSmsDetailsFragment.setIntent(intent);
                        ((ConversationList) ComposeMessageFragment.this.getActivity()).changeRightAddToStack(groupSmsDetailsFragment, ComposeMessageFragment.this);
                        break;
                    case 1000105:
                        StatisticalHelper.incrementReportCount(ComposeMessageFragment.this.getContext(), 2133);
                        ComposeMessageFragment.this.initDirtyModel();
                        if (!(ComposeMessageFragment.this.mMsgListAdapter == null || ComposeMessageFragment.this.mMsgListAdapter.getCryptoMessageListAdapter() == null)) {
                            ComposeMessageFragment.this.mMsgListAdapter.getCryptoMessageListAdapter().removeEncryptCache(Long.valueOf(msgItem.getMessageId()));
                        }
                        ComposeMessageFragment.this.editMessageItem(msgItem);
                        DelaySendManager.getInst().removeDelayMsg(msgItem.getCancelId(), msgItem.mType, msgItem.mIsMultiRecipients);
                        MessageListItem.setMsgItemCancled(true);
                        MessageItem.removeSmsRichUrlCache(msgItem.getMessageId());
                        ComposeMessageFragment.this.refreshAttachmentChangedUI(13);
                        break;
                    default:
                        MLog.w("Mms_UI_CMA", "Unknown message: " + msg.what);
                        return;
                }
            }
        }
    };
    private boolean mHasUnreadMessage = false;
    HideKeyboardTouchListener mHideKeyboardTouchListener = new HideKeyboardTouchListener() {
        protected void hideKeyboard() {
            ComposeMessageFragment.this.hideKeyboard();
        }
    };
    private Runnable mHidePanelRunnable = new Runnable() {
        public void run() {
            if (ComposeMessageFragment.this.mComposeChoosePanel != null) {
                ComposeMessageFragment.this.mComposeChoosePanel.hidePanel();
            }
        }
    };
    private final IntentFilter mHttpProgressFilter = new IntentFilter("com.android.mms.PROGRESS_STATUS");
    private final BroadcastReceiver mHttpProgressReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.mms.PROGRESS_STATUS".equals(intent.getAction()) && intent.getLongExtra(NetUtil.REQ_QUERY_TOEKN, -1) == ComposeMessageFragment.this.mConversation.getThreadId()) {
                int progress = intent.getIntExtra("progress", 0);
                switch (progress) {
                    case -2:
                    case 100:
                        if (ComposeMessageFragment.this.getActivity() != null) {
                            ComposeMessageFragment.this.getActivity().setProgressBarVisibility(false);
                            break;
                        }
                        break;
                    case -1:
                        if (ComposeMessageFragment.this.getActivity() != null) {
                            ComposeMessageFragment.this.getActivity().setProgressBarVisibility(true);
                            break;
                        }
                        break;
                    default:
                        if (ComposeMessageFragment.this.getActivity() != null) {
                            ComposeMessageFragment.this.getActivity().setProgress(progress * 100);
                            break;
                        }
                        break;
                }
            }
        }
    };
    private HwCustComposeMessage mHwCustComposeMessage = null;
    private HwCustPduPersister mHwCustPduPersister = null;
    private boolean mIsAllSelected = false;
    private boolean mIsCanCopyText = false;
    private boolean mIsCanDelete = false;
    private boolean mIsCanForward = false;
    private boolean mIsCanSave = false;
    private boolean mIsCanSelectAll = false;
    private boolean mIsCanSeleteText = false;
    private boolean mIsContentChanged;
    private boolean mIsDataLoadedOnCreate = false;
    private boolean mIsDraftLoadFinished = false;
    private boolean mIsEditOnly = false;
    private boolean mIsForWardMms = false;
    private boolean mIsFromLauncher = false;
    private boolean mIsFromQuickAction = false;
    private boolean mIsKeyboardOpen;
    private boolean mIsLandscape;
    private boolean mIsPushConversation = false;
    private boolean mIsQueryAfterDelete = false;
    private boolean mIsRunning;
    private boolean mIsShowingRich = false;
    private boolean mIsSmsEnabled;
    private boolean mIsSuperPowerSaveMode = false;
    private long mLastMessageId;
    private int mLastSmoothScrollPosition;
    private LocalBroadcastManager mLocalBroadcastManager = null;
    private ArrayList<MediaUpdateListener> mMediaUpdateListeners;
    protected MenuEx mMenuEx = null;
    private View mMessageBlockView = null;
    private boolean mMessagesAndDraftLoaded;
    private String mMineType = null;
    private LinearLayout mMmMessageEditLayout;
    private OnPreDrawListener mMmMessageEditLayoutPreDrawListener = new OnPreDrawListener() {
        public boolean onPreDraw() {
            int heightWithpadding = ComposeMessageFragment.this.mRichEditor.getHeight() + ComposeMessageFragment.this.mComposeBottomLayoutPaddingTopBottom;
            if (heightWithpadding > ComposeMessageFragment.this.mMsBottomPanalMaxHeight) {
                ComposeMessageFragment.this.mBottomPanalMinHeight = ComposeMessageFragment.this.mMsBottomPanalMaxHeight;
            } else {
                ComposeMessageFragment.this.mBottomPanalMinHeight = heightWithpadding;
            }
            return true;
        }
    };
    MmsRadarInfoManager mMmsRadarInfoManager = null;
    private int mMsBottomPanalMaxHeight;
    private MessageItem mMsgItem = null;
    private MessageListAdapter mMsgListAdapter;
    protected MessageListView mMsgListView;
    private ArrayList<Parcelable> mMultiUris;
    private boolean mNeedLoadDraft = true;
    private boolean mNeedPopupEmailCheck = false;
    private boolean mNeedSaveDraft = true;
    EmuiMenu mNormalMenu;
    protected int mOldMessageCount;
    public int mPopId;
    private int mPosition;
    private boolean mPossiblePendingNotification;
    final Runnable mQueryThreadSubIdByRecipientRunner = new Runnable() {
        public void run() {
            Builder uriBuilder = ComposeMessageFragment.THREAD_ID_CONTENT_URI.buildUpon();
            if (ComposeMessageFragment.this.mComposeRecipientsView.getRecipientCount() != 1) {
                ComposeMessageFragment.this.postRecommendResult(-1);
                return;
            }
            List<String> recipients = ComposeMessageFragment.this.mComposeRecipientsView.getNumbers();
            if (recipients.size() != 0) {
                String recipient = (String) recipients.get(0);
                if (Mms.isEmailAddress(recipient)) {
                    recipient = Mms.extractAddrSpec(recipient);
                }
                uriBuilder.appendQueryParameter("recipient", recipient);
                uriBuilder.appendQueryParameter("isInsert", "false");
                uriBuilder.appendQueryParameter("queryOnly", "true");
                Cursor cursor = null;
                try {
                    int subid;
                    cursor = SqliteWrapper.query(ComposeMessageFragment.this.getContext(), uriBuilder.build(), null, null, null, null);
                    if (cursor == null || !cursor.moveToFirst()) {
                        subid = -1;
                    } else {
                        Conversation conv = Conversation.get(ComposeMessageFragment.this.getContext(), cursor.getLong(0), false);
                        if (conv.getMessageCount() > 0) {
                            subid = conv.getSubId();
                        } else {
                            subid = -1;
                        }
                    }
                    ComposeMessageFragment.this.postRecommendResult(subid);
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            MLog.w("Mms_UI_CMA", "mRecommendSubByRecipientsRunner cursor.close error");
                        }
                    }
                } catch (SQLiteException e2) {
                    MLog.w("Mms_UI_CMA", "mRecommendSubByRecipientsRunner query error");
                    ComposeMessageFragment.this.postRecommendResult(-1);
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e3) {
                            MLog.w("Mms_UI_CMA", "mRecommendSubByRecipientsRunner cursor.close error");
                        }
                    }
                } catch (Throwable th) {
                    ComposeMessageFragment.this.postRecommendResult(-1);
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e4) {
                            MLog.w("Mms_UI_CMA", "mRecommendSubByRecipientsRunner cursor.close error");
                        }
                    }
                }
            }
        }
    };
    private RcsAudioMessage mRcsAudioMessage;
    private RcsComposeMessage mRcsComposeMessage = null;
    private int mReCode = 0;
    final Runnable mRecommendSubByRecipientsRunner = new Runnable() {
        public void run() {
            ThreadEx.execute(ComposeMessageFragment.this.mQueryThreadSubIdByRecipientRunner);
        }
    };
    Runnable mResetMessageRunnable = new Runnable() {
        public void run() {
            ComposeMessageFragment.this.resetMessage();
        }
    };
    private ViewGroup mRichBubbleLayoutParent = null;
    protected RichMessageEditor mRichEditor;
    private BroadcastReceiver mSaveDraftReceiver;
    private int mScreenHeight;
    private ScroolOnItemLayoutCallback mScroolOnLastItemLoadedCallback = new ScroolOnItemLayoutCallback(this);
    private boolean mSendDiscreetMode;
    Handler mSendSmsHandler = null;
    private boolean mSendingMessage;
    private boolean mSentMessage;
    private boolean mShouldLoadDraft;
    private View mShowPopupFloatingToolbarView = null;
    private SmartSmsComposeManager mSmartSmsComponse;
    protected SplitActionBarView mSplitActionBar;
    private long mThreadID = 0;
    private boolean mToastForDraftSave;
    private boolean mToastToMms = true;
    private boolean mTooOftenOnUpdate = false;
    private Runnable mUpdateContactRunner = new Runnable() {
        public void run() {
            if (ComposeMessageFragment.this.mMsgListView != null) {
                ComposeMessageFragment.this.mMsgListView.invalidateViews();
            }
        }
    };
    private int mUriPostion = 0;
    private BroadcastReceiver mVoWifiReceiver = null;
    private boolean mWaitingForSubActivity;
    MapClickCallback mapClickCallback = null;
    protected View root;
    private ContactList sEmptyContactList;
    private int subSelectedForDialog = 0;
    private boolean userOperation = true;

    public interface MediaUpdateListener {
        boolean hasUpdateMedida(int i);

        void updateMedia(RichMessageEditor richMessageEditor);
    }

    private final class BackgroundQueryHandler extends ConversationQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            long tid;
            switch (token) {
                case AMapException.CODE_AMAP_CLIENT_SOCKET_TIMEOUT_EXCEPTION /*1802*/:
                    if (!ComposeMessageFragment.this.isDetached()) {
                        ArrayList<Long> threadIds = (ArrayList) cookie;
                        DeleteThreadListener deleteThreadListener = new DeleteThreadListener(ComposeMessageFragment.this.getContext(), threadIds, ComposeMessageFragment.this.mBackgroundQueryHandler);
                        boolean z = cursor != null && cursor.getCount() > 0;
                        BaseConversationListFragment.confirmDeleteThreadDialog(deleteThreadListener, threadIds, z, ComposeMessageFragment.this.mIsAllSelected, ComposeMessageFragment.this.getContext());
                        if (cursor != null) {
                            cursor.close();
                            break;
                        }
                    }
                    MLog.w("Mms_UI_CMA", "ComposeMessageActivity is finished, do nothing ");
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                    break;
                case 9527:
                    tid = ((Long) cookie).longValue();
                    if (MLog.isLoggable("Mms_app", 2)) {
                        ComposeMessageFragment.log("##### onQueryComplete: msg history result for threadId " + tid);
                    }
                    if (tid != ComposeMessageFragment.this.mConversation.getThreadId()) {
                        ComposeMessageFragment.log("onQueryComplete: msg history query result is for threadId " + tid + ", but mConversation has threadId " + ComposeMessageFragment.this.mConversation.getThreadId() + " starting a new query");
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (ComposeMessageFragment.this.mRcsComposeMessage == null || !ComposeMessageFragment.this.mRcsComposeMessage.isExitingActivity()) {
                            ComposeMessageFragment.this.startMsgListQuery();
                        }
                        return;
                    }
                    ComposeMessageFragment.this.sanityCheckConversation();
                    if (cursor != null) {
                        int newMessageCount = cursor.getCount();
                        ComposeMessageFragment.this.onCached(cursor);
                        boolean isXmsThread = true;
                        if (ComposeMessageFragment.this.mConversation.getHwCust() != null) {
                            isXmsThread = ComposeMessageFragment.this.mConversation.getHwCust().isXms();
                        }
                        Conversation conversation = ComposeMessageFragment.this.mConversation;
                        boolean z2 = Conversation.isGroupConversation(ComposeMessageFragment.this.getContext(), tid) ? isXmsThread : false;
                        if (MLog.isLoggable("Mms_app", 2)) {
                            ComposeMessageFragment.log("isXmsThread:" + isXmsThread + ", isXmsGroupConversation:" + z2 + ", tid:" + tid);
                        }
                        if (ComposeMessageFragment.this.mOldMessageCount != newMessageCount && !z2) {
                            ComposeMessageFragment.this.mOldMessageCount = newMessageCount;
                            if (ComposeMessageFragment.this.mRcsComposeMessage == null || !ComposeMessageFragment.this.mRcsComposeMessage.isRcsSwitchOn()) {
                                if (newMessageCount == 0) {
                                    ComposeMessageFragment.this.mComposeBottomView.setRecommendMessageButton(-1);
                                    return;
                                } else if (cursor.moveToLast()) {
                                    ComposeMessageFragment.this.mConversation.setSubId((int) cursor.getLong(5));
                                }
                            }
                            MLog.i("Mms_UI_CMA", "onCached mConversation.getCount =" + ComposeMessageFragment.this.mConversation.getMessageCount());
                            ComposeMessageFragment.this.mRichEditor.setConversation(ComposeMessageFragment.this.mConversation);
                            ComposeMessageFragment.this.updateSendButtonView();
                            if (MmsConfig.getSupportSmartSmsFeature() && ComposeMessageFragment.this.mSmartSmsComponse != null && ComposeMessageFragment.this.getRecipients() != null && ComposeMessageFragment.this.getRecipients().size() == 1) {
                                ComposeMessageFragment.this.mSmartSmsComponse.queryMenu(ComposeMessageFragment.this, ((Contact) ComposeMessageFragment.this.getRecipients().get(0)).getNumber(), (short) 1);
                            }
                            if (ComposeMessageFragment.this.mMsgListView.isInEditMode()) {
                                ComposeMessageFragment.this.mMenuEx.onPrepareOptionsMenu();
                            }
                            ComposeMessageFragment.this.mSplitActionBar.dismissPopup();
                            ComposeMessageFragment.this.mActionBarWhenSplit.getSplitActionBarView().dismissPopup();
                        } else if (z2) {
                            MLog.i("Mms_UI_CMA", "onQueryComplete is group coversation mOldMessageCount: " + ComposeMessageFragment.this.mOldMessageCount + " newMessageCount:" + newMessageCount);
                            conversation = ComposeMessageFragment.this.mConversation;
                            int memberCount = Conversation.getGroupMemberCount(ComposeMessageFragment.this.getContext(), tid);
                            if (ComposeMessageFragment.this.mOldMessageCount != newMessageCount * memberCount) {
                                ComposeMessageFragment.this.mOldMessageCount = newMessageCount * memberCount;
                                ComposeMessageFragment.this.mConversation = new Conversation(ComposeMessageFragment.this.getContext(), ComposeMessageFragment.this.mConversation.getThreadId(), false);
                                ComposeMessageFragment.this.mRichEditor.setConversation(ComposeMessageFragment.this.mConversation);
                                ComposeMessageFragment.this.updateSendButtonView();
                            } else {
                                return;
                            }
                        }
                        return;
                    }
                    return;
                case 9528:
                    tid = ((Long) cookie).longValue();
                    if (MLog.isLoggable("Mms_app", 2)) {
                        ComposeMessageFragment.log("##### onQueryComplete (after delete): msg history result for threadId " + tid);
                    }
                    if (cursor != null) {
                        if (tid > 0 && cursor.getCount() == 0 && !ComposeMessageFragment.this.mRichEditor.isWorthSaving()) {
                            ComposeMessageFragment.log("##### MESSAGE_LIST_QUERY_AFTER_DELETE_TOKEN clearing thread id: " + tid);
                            final Conversation conv = Conversation.get(ComposeMessageFragment.this.getContext(), tid, false);
                            conv.clearThreadId();
                            conv.setDraftState(false);
                            ComposeMessageFragment.this.exitComposeMessageActivity(new Runnable() {
                                public void run() {
                                    if (HwMessageUtils.isSplitOn()) {
                                        ComposeMessageFragment.this.doDeleteAllInCompose();
                                    } else {
                                        ((ComposeMessageActivity) ComposeMessageFragment.this.getActivity()).goToConversationList(conv.getNumberType());
                                    }
                                }
                            });
                        }
                        if (ComposeMessageFragment.this.mRcsComposeMessage != null && ComposeMessageFragment.this.mRcsComposeMessage.isRcsSwitchOn()) {
                            ComposeMessageFragment.this.startMsgListQueryDelayed(100);
                        }
                        cursor.close();
                        break;
                    }
                    return;
            }
        }

        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);
            switch (token) {
                case AMapException.CODE_AMAP_CLIENT_ERROR_PROTOCOL /*1801*/:
                    ComposeMessageFragment.this.mConversation.setMessageCount(0);
                    break;
                case 9786:
                    if (ComposeMessageFragment.this.mMsgListView != null) {
                        HwMessageUtils.showJlogByID(138, (int) (SystemClock.uptimeMillis() - ComposeMessageFragment.this.mMsgListView.getDeleteStartTime()), "Mms::delete " + ComposeMessageFragment.this.mMsgListView.getDeleteSmsCount() + " sms and " + ComposeMessageFragment.this.mMsgListView.getDeleteMmsCount() + " mms messages!");
                    }
                    MessageListItem.setMsgItemCancled(false);
                    if ((cookie instanceof Boolean) && ((Boolean) cookie).booleanValue()) {
                        ComposeMessageFragment.this.mLastMessageId = 0;
                    }
                    MessagingNotification.nonBlockingUpdateNewMessageIndicator(ComposeMessageFragment.this.getContext(), -2, false);
                    ComposeMessageFragment.this.updateSendFailedNotification();
                    if (ComposeMessageFragment.this.mMsgListAdapter != null) {
                        ComposeMessageFragment.this.mMsgListAdapter.clearTextSpanCache(false);
                    }
                    if (HwMessageUtils.isSplitOn() && (ComposeMessageFragment.this.getActivity() instanceof ConversationList)) {
                        HwBaseFragment f = ((ConversationList) ComposeMessageFragment.this.getActivity()).getFragment();
                        if (f instanceof LeftPaneConversationListFragment) {
                            ((LeftPaneConversationListFragment) f).setIsAfterDelete(true);
                            break;
                        }
                    }
                    break;
                case 9806:
                    ComposeMessageFragment.this.getComposeMessageActivity().onFragmentRelease();
                    break;
            }
            if (token == 9786 && !ComposeMessageFragment.this.mConversation.isThreadIdInDB(ComposeMessageFragment.this.mConversation.getThreadId())) {
                if (ComposeMessageFragment.this.mRcsComposeMessage != null && ComposeMessageFragment.this.mRcsComposeMessage.isRcsSwitchOn()) {
                    MLog.i("Mms_UI_CMA", "do not change cursor to null");
                } else if (ComposeMessageFragment.this.mMsgListAdapter != null) {
                    ComposeMessageFragment.this.mMsgListAdapter.changeCursor(null);
                }
                MLog.d("Mms_UI_CMA", "Delete last message, so clear the local thread id.");
                ComposeMessageFragment.this.mComposeBottomView.setRecommendMessageButton(-1);
                ComposeMessageFragment.this.mOldMessageCount = 0;
                ComposeMessageFragment.this.mConversation.setSubId(-1);
            }
            if (token == AMapException.CODE_AMAP_CLIENT_ERROR_PROTOCOL) {
                ContactList<Contact> recipients = ComposeMessageFragment.this.mConversation.getRecipients();
                ComposeMessageFragment.this.mRichEditor.discard();
                if (recipients != null) {
                    for (Contact contact : recipients) {
                        contact.removeFromCache();
                    }
                }
                Conversation.clear(ComposeMessageFragment.this.getContext());
                ComposeMessageFragment.this.finish();
            } else if (token == 9786) {
                ComposeMessageFragment.this.mIsQueryAfterDelete = true;
                ComposeMessageFragment.this.startMsgListQuery(9528);
            }
            MmsWidgetProvider.notifyDatasetChanged(ComposeMessageFragment.this.getContext());
            MLog.i("Mms_UI_CMA", " onDeleteComplete end token = " + token);
        }

        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            boolean z = false;
            super.onInsertComplete(token, cookie, uri);
            if (token == 9799) {
                ComposeMessageFragment.this.getActivity().setProgressBarVisibility(false);
                if (uri == null) {
                    ResEx.makeToast(R.string.add_favorite_failed_Toast);
                }
            } else if (token == 9798 && uri == null) {
                ResEx.makeToast(R.string.add_favorite_failed_Toast);
            } else if (9800 == token) {
                MLog.d("Mms_UI_CMA", "Copy message to SIM finished.");
                Integer subId = (Integer) cookie;
                ComposeMessageFragment composeMessageFragment = ComposeMessageFragment.this;
                int intValue = subId.intValue();
                if (uri != null) {
                    z = true;
                }
                composeMessageFragment.showCopyToSimResult(intValue, z);
                switch (subId.intValue()) {
                    case 0:
                        SimCursorManager.self().clearCursor();
                        return;
                    case 1:
                        SimCursorManager.self().clearCursor(1);
                        return;
                    case 2:
                        SimCursorManager.self().clearCursor(2);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    private class ComposeCommonHolder implements IRecipientsHoler, IChoosePanelHoler, IMessageListHodler, IBottomHolder, EditableSlides$RichMessageListener {
        private ComposeCommonHolder() {
        }

        public void onRecipientsEditorFocusOut() {
            if (ComposeMessageFragment.this.mComposeRecipientsView.isVisible() && ComposeMessageFragment.this.mComposeRecipientsView.hasFocus()) {
                if (!MmsConfig.isSupportDraftWithoutRecipient() || ComposeMessageFragment.this.mComposeRecipientsView.getRecipientCount() >= 1) {
                    ComposeMessageFragment.this.mRichEditor.setEditTextFocus();
                } else {
                    ComposeMessageFragment.this.mRichEditor.setSmsEditTextFocus();
                }
            }
            EditTextWithSmiley editText = ComposeMessageFragment.this.mRichEditor.getMSmsEditorText();
            if (editText != null) {
                editText.setCursorVisible(true);
            }
            ComposeMessageFragment.this.mRichEditor.setHasEmail(ComposeMessageFragment.this.mComposeRecipientsView.containsEmail(), true);
            if (!(HwMessageUtils.isSplitOn() || ComposeMessageFragment.this.mIsLandscape)) {
                HwMessageUtils.displaySoftInput(ComposeMessageFragment.this.getContext(), ComposeMessageFragment.this.mRichEditor.getFocusedChild());
            }
        }

        public void onRecipientsEditorFocusIn() {
            ComposeMessageFragment.this.mComposeChoosePanel.hidePanel();
            if (ComposeMessageFragment.this.mConversationInputManager.isMediaPickerVisible()) {
                ComposeMessageFragment.this.mConversationInputManager.showHideMediaPicker(false, false);
            }
        }

        public void onRecipientTextChanged(boolean userInteraction) {
            if (userInteraction) {
                ComposeMessageFragment.this.onUserInteraction();
            }
            if (ComposeMessageFragment.this.isAdded()) {
                ComposeMessageFragment.this.updateSendButtonView();
            }
            if (ComposeMessageFragment.this.mRcsComposeMessage != null && ComposeMessageFragment.this.mIsRunning) {
                ComposeMessageFragment.this.mRcsComposeMessage.onRecipientTextChanged(ComposeMessageFragment.this.mComposeRecipientsView.getRecipientsEditorText());
            }
            ComposeMessageFragment.this.mHandler.removeMessages(10000);
            ComposeMessageFragment.this.mHandler.sendEmptyMessageDelayed(10000, 200);
        }

        public void afterRecipientTextChanged() {
            if (ComposeMessageFragment.this.mRcsComposeMessage != null) {
                ComposeMessageFragment.this.mRcsComposeMessage.afterRecipientTextChanged();
            }
            List<String> numbers = ComposeMessageFragment.this.mComposeRecipientsView.getNumbers();
            ComposeMessageFragment.this.mRichEditor.setWorkingRecipients(numbers);
            boolean multiRecipients = numbers.size() > 1;
            ComposeMessageFragment.this.mMsgListAdapter.setIsGroupConversation(multiRecipients);
            ComposeMessageFragment.this.mRichEditor.setHasMultipleRecipients(multiRecipients, true);
            ComposeMessageFragment.this.mComposeRecipientsView.checkForTooManyRecipients();
            if (ComposeMessageFragment.this.mRichEditor.isFocused()) {
                ComposeMessageFragment.this.mRichEditor.setHasEmail(ComposeMessageFragment.this.mComposeRecipientsView.containsEmail(), true);
            }
        }

        public void onRecipientsChanged() {
            ComposeMessageFragment.this.mHandler.removeMessages(1000);
            ComposeMessageFragment.this.mHandler.removeMessages(10000);
            ComposeMessageFragment.this.mHandler.sendEmptyMessageDelayed(1000, 100);
            ComposeMessageFragment.this.mHandler.sendEmptyMessageDelayed(10000, 200);
        }

        public void updateTitle(ContactList list) {
            ComposeMessageFragment.this.updateTitle(list);
        }

        public Resources getResources() {
            if (ComposeMessageFragment.this.isAdded()) {
                return ComposeMessageFragment.this.getResources();
            }
            MLog.w("Mms_UI_CMA", "fragment is already to destory");
            return MmsApp.getApplication().getApplicationContext().getResources();
        }

        public ContactList getRecipients() {
            if (ComposeMessageFragment.this.mComposeRecipientsView.isVisible()) {
                return ComposeMessageFragment.this.mComposeRecipientsView.getRecipients();
            }
            if (ComposeMessageFragment.this.mConversation == null) {
                return null;
            }
            return ComposeMessageFragment.this.mConversation.getRecipients();
        }

        public View findViewById(int id) {
            return ComposeMessageFragment.this.findViewById(id);
        }

        public void showNewMessageTitle() {
            if (ComposeMessageFragment.this.mHwCustComposeMessage == null || !ComposeMessageFragment.this.mHwCustComposeMessage.getIsTitleChangeWhenRecepientsChange()) {
                ComposeMessageFragment.this.showNewMessageTitle();
            } else {
                ComposeMessageFragment.this.mHwCustComposeMessage.showNewMessageTitleWithMaxRecipient(ComposeMessageFragment.this.getRecipients(), ComposeMessageFragment.this.mActionBarWhenSplit);
            }
        }

        public HwBaseFragment getFragment() {
            return ComposeMessageFragment.this;
        }

        public boolean isSmsEnabled() {
            return ComposeMessageFragment.this.mIsSmsEnabled;
        }

        public boolean isEditOnly() {
            return ComposeMessageFragment.this.mIsEditOnly;
        }

        public void alertForSendMms() {
            if (!(ComposeMessageFragment.this.getActivity() instanceof HwBaseActivity) || ((HwBaseActivity) ComposeMessageFragment.this.getActivity()).ismHasSmsPermissionsForUser()) {
                ComposeMessageFragment.this.alertForSendMms();
                showEnableFullScreenIcon();
            }
        }

        public Handler getHandler() {
            return ComposeMessageFragment.this.mHandler;
        }

        public boolean isMms() {
            return ComposeMessageFragment.this.mRichEditor.requiresMms();
        }

        public boolean isResumeFromStop() {
            return ComposeMessageFragment.this.mFromStop;
        }

        public int getParentHeight() {
            return ComposeMessageFragment.this.mComposeLayoutGroup == null ? 0 : ComposeMessageFragment.this.mComposeLayoutGroup.getMeasuredHeight();
        }

        public void hideKeyboard() {
            ComposeMessageFragment.this.hideKeyboard();
        }

        public void setCryptoToastIsShow(boolean cryptoToastIsShow) {
            ComposeMessageFragment.this.mCryptoToastIsShow = cryptoToastIsShow;
        }

        public boolean isKeyBoardOpen() {
            return ComposeMessageFragment.this.mIsKeyboardOpen;
        }

        public boolean isInNewMessageMode() {
            if (ComposeMessageFragment.this.mComposeRecipientsView != null) {
                return ComposeMessageFragment.this.mComposeRecipientsView.isVisible();
            }
            return false;
        }

        public boolean hideAttachmentsView() {
            if (ComposeMessageFragment.this.mConversationInputManager != null && ComposeMessageFragment.this.mConversationInputManager.isMediaPickerVisible()) {
                ComposeMessageFragment.this.mConversationInputManager.showHideMediaPicker(false, false);
            }
            return false;
        }

        public void hidePanel() {
            ComposeMessageFragment.this.mComposeChoosePanel.hidePanel();
        }

        public boolean hasExceedsMmsLimit() {
            return ComposeMessageFragment.this.mRichEditor == null ? false : ComposeMessageFragment.this.mRichEditor.hasExceedsMmsLimit();
        }

        public int getActionBarShowHeight() {
            if (ComposeMessageFragment.this.mComposeRecipientsView == null || !ComposeMessageFragment.this.mComposeRecipientsView.isVisible()) {
                return 0;
            }
            return ComposeMessageFragment.this.mComposeRecipientsView.getRecipientGroupLayoutHeight();
        }

        public int getBottomPanalMinHeight() {
            return ComposeMessageFragment.this.mBottomPanalMinHeight;
        }

        public int getMultiSimModelLayoutHeight() {
            if (ComposeMessageFragment.this.mComposeBottomView != null) {
                return ComposeMessageFragment.this.mComposeBottomView.getMultiSimModelLayoutHeight();
            }
            return 0;
        }

        public void addAttachment(int type, boolean replace) {
            ComposeMessageFragment.this.addAttachment(type, replace);
        }

        public void showEnableFullScreenIcon() {
            ComposeMessageFragment.this.showEnableFullScreenIcon();
        }

        public boolean isShowSlideOptions() {
            return ComposeMessageFragment.this.mRichEditor.hasFocus();
        }

        public OnItemClickListener getSmileyItemClickListener() {
            return ComposeMessageFragment.this.mRichEditor;
        }

        public OnTouchListener getDeleteKeyClickListener() {
            return ComposeMessageFragment.this.mRichEditor;
        }

        public boolean isGroupConversation() {
            return ComposeMessageFragment.this.mConversation != null ? ComposeMessageFragment.this.mConversation.isGroupConversation() : false;
        }

        public AsyncDialog getAsyncDialog() {
            return ComposeMessageFragment.this.getAsyncDialog();
        }

        public boolean isInMultiWindowMode() {
            if (ComposeMessageFragment.this.isAdded()) {
                return ComposeMessageFragment.this.getActivity().isInMultiWindowMode();
            }
            return false;
        }

        public long getConversationId() {
            if (ComposeMessageFragment.this.mConversation == null) {
                return -1;
            }
            long tid = ComposeMessageFragment.this.mConversation.getThreadId();
            Cache.getInstance();
            if (Cache.get(tid) == null) {
                Cache.getInstance();
                Cache.put(ComposeMessageFragment.this.mConversation);
            }
            return tid;
        }

        public AsyncQueryHandlerEx getQueryHandler() {
            return ComposeMessageFragment.this.mBackgroundQueryHandler;
        }

        public void showProgressBar(boolean show) {
            ComposeMessageFragment.this.getActivity().setProgressBarVisibility(show);
        }

        public Intent createIntent(long threadId) {
            return ComposeMessageActivity.createIntent(ComposeMessageFragment.this.getContext(), threadId);
        }

        public long getFirstRecipientMsgId(long groupId, long defalutId) {
            return ComposeMessageFragment.this.getFirstRecipientMsgId(groupId, defalutId);
        }

        public void updateFullScreenButtonState(int state) {
            ComposeMessageFragment.this.mFullScreenButtonState = state;
        }

        public void onSendButtonClick(int subId) {
            if (ComposeMessageFragment.this.mRcsComposeMessage != null) {
                ComposeMessageFragment.this.mRcsComposeMessage.requestCapabilitiesOnTextChange();
            }
            ComposeMessageFragment.this.subSelectedForDialog = subId;
            if (ComposeMessageFragment.this.mComposeRecipientsView.isVisible()) {
                ComposeMessageFragment.this.mComposeRecipientsView.commitNumberChip();
                ComposeMessageFragment.this.mRichEditor.setWorkingRecipients(ComposeMessageFragment.this.mComposeRecipientsView.getNumbers());
                if (ComposeMessageFragment.this.mComposeRecipientsView.getRecipientCount() == 0) {
                    return;
                }
            }
            if (ComposeMessageFragment.this.isTooManyRecipients()) {
                ComposeMessageFragment.this.mComposeRecipientsView.displayTooManyRecipientsDialog();
            } else if (ComposeMessageFragment.this.isNeedShowSendMmsPopup()) {
                ComposeMessageFragment.this.mNeedPopupEmailCheck = true;
                if (ComposeMessageFragment.this.mComposeRecipientsView.isVisible()) {
                    alertForSendMms();
                }
            } else {
                ComposeMessageFragment.this.confirmSendMessageIfNeeded(subId);
                ComposeMessageFragment.this.sendComposeMessageStopPlayBroadcast();
            }
        }

        public void onSimStateChanged(int card1State, int card2State) {
            boolean z = true;
            if (ComposeMessageFragment.this.mMsgListAdapter != null) {
                MessageListAdapter -get39 = ComposeMessageFragment.this.mMsgListAdapter;
                if (1 != card1State) {
                    z = false;
                } else if (1 != card2State) {
                    z = false;
                }
                -get39.setMultiSimActive(z);
            }
            ComposeMessageFragment.this.mHandler.removeMessages(1000);
            ComposeMessageFragment.this.mHandler.sendEmptyMessageDelayed(1000, 100);
        }

        public void onMultiSimUpdateSendButton() {
            if (ComposeMessageFragment.this.mConversation.isMessageCountValid()) {
                int defaultCard = ComposeMessageFragment.this.mConversation.getSubId();
                ComposeMessageFragment.this.mRichEditor.setNewMessageDraftSubid(-1);
                ComposeMessageFragment.this.mComposeBottomView.setRecommendMessageButton(defaultCard);
            } else if (ComposeMessageFragment.this.mComposeRecipientsView.isVisible() && ComposeMessageFragment.this.mComposeRecipientsView.getRecipientCount() == 1) {
                ComposeMessageFragment.this.mHandler.removeCallbacks(ComposeMessageFragment.this.mRecommendSubByRecipientsRunner);
                ComposeMessageFragment.this.mHandler.postDelayed(ComposeMessageFragment.this.mRecommendSubByRecipientsRunner, 100);
            }
        }

        public void onInputManagerShow() {
            if (ComposeMessageFragment.this.mConversationInputManager.isMediaPickerVisible()) {
                ComposeMessageFragment.this.mConversationInputManager.showHideMediaPicker(false, false);
            }
            ComposeMessageFragment.this.mComposeChoosePanel.hidePanel();
        }

        public void richToCheckRestrictedMime(boolean value) {
            ComposeMessageFragment.this.checkRestrictedMime(value);
        }

        public void onContentChange() {
            ComposeMessageFragment.this.mHandler.sendPeriodMessage(1000, 100, 300);
        }

        public void onDraftLoaded() {
            new ConversationUpdator(ComposeMessageFragment.this.mConversation).checkRecipientShow();
            ComposeMessageFragment.this.isFirstCheck = true;
            showEnableFullScreenIcon();
            ComposeMessageFragment.this.mCryptoCompose.updateSwitchStateLoadDraft(ComposeMessageFragment.this.mRichEditor.requiresMms());
            ComposeMessageFragment.this.initDirtyModel();
            ComposeMessageFragment.this.mIsDraftLoadFinished = true;
            ComposeMessageFragment.this.onDraftChanged();
            ComposeMessageFragment.this.updateSendButtonView();
            if (ComposeMessageFragment.this.mRichEditor != null && ComposeMessageFragment.this.mRichEditor.hasContentToSend()) {
                ComposeMessageFragment.this.showMessageEditLayoutAndHiddenSmartSmsMenu();
            }
        }
    }

    public static abstract class ConversationActionBarAdapter implements AddWhatsAppPeopleActionBarAdapter {
        protected HwBaseFragment mParentFragment;
        protected HashMap<ComponentName, Uri> mRelatedContactInfo = new HashMap();
        protected Uri[] mRelatedContactUri;

        public abstract ContactList getContactList();

        public ConversationActionBarAdapter(HwBaseFragment fragment, int uriSize) {
            this.mParentFragment = fragment;
            this.mRelatedContactUri = new Uri[uriSize];
        }

        public long getThreadId() {
            return -1;
        }

        public String getName() {
            ContactList list = getContactList();
            if (list == null) {
                return null;
            }
            if (list.size() > 1) {
                String formatName = list.formatNames(", ");
                if (!TextUtils.isEmpty(formatName)) {
                    formatName = list.formatNoNameContactNumber(", ");
                }
                return formatName;
            } else if (list.size() == 1) {
                return ((Contact) list.get(0)).getName();
            } else {
                return null;
            }
        }

        public String getNumber() {
            ContactList list = getContactList();
            if (list == null) {
                return null;
            }
            if (list.size() > 1) {
                HwCustComposeMessage mHwCustCma = (HwCustComposeMessage) HwCustUtils.createObj(HwCustComposeMessage.class, new Object[]{this.mParentFragment});
                if (mHwCustCma != null && mHwCustCma.getIsTitleChangeWhenRecepientsChange()) {
                    return mHwCustCma.getRecipientCountStr(list, this.mParentFragment.getContext());
                }
                return this.mParentFragment.getContext().getResources().getQuantityString(R.plurals.recipient_count, list.size(), new Object[]{Integer.valueOf(list.size())});
            } else if (list.size() == 1) {
                return ((Contact) list.get(0)).getNumber();
            } else {
                return null;
            }
        }

        public boolean isGroup() {
            ContactList list = getContactList();
            if (list == null || list.size() <= 1) {
                return false;
            }
            return true;
        }

        public boolean isExistsInContact() {
            ContactList list = getContactList();
            if (list == null || list.size() != 1) {
                return false;
            }
            return ((Contact) list.get(0)).existsInDatabase();
        }

        public boolean hasEmail() {
            return Contact.isEmailAddress(getNumber());
        }

        public void writeEmail() {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("mailto:" + getNumber()));
            intent.setFlags(524288);
            this.mParentFragment.startActivity(intent);
        }

        public void editBeforeCall() {
        }

        public void callRecipients() {
            call(getNumber());
        }

        protected void call(String number) {
            HwMessageUtils.hideKeyBoard(this.mParentFragment.getActivity());
            if (HwMessageUtils.needShowCallMenus(this.mParentFragment.getContext())) {
                MLog.d("Mms_UI_CMA", "call menus condition turns true, will show call menus");
                HwMessageUtils.showCallMenuInContacts(this.mParentFragment.getActivity(), number, false);
                return;
            }
            if (MessageUtils.isMultiSimEnabled()) {
                int card1State = MessageUtils.getIccCardStatus(0);
                int card2State = MessageUtils.getIccCardStatus(1);
                if (1 == card1State && 1 == card2State) {
                    HwMessageUtils.callNumberByMultiSimWithCheckMode(this.mParentFragment.getActivity(), "tel:" + number);
                } else if (1 == card1State || 1 == card2State) {
                    HwMessageUtils.dialNumber("tel:" + number, this.mParentFragment.getActivity());
                } else {
                    HwMessageUtils.dialNumberBySubscription(this.mParentFragment.getContext(), "tel:" + number, 0);
                }
            } else {
                HwMessageUtils.dialNumberForSingleCard(this.mParentFragment.getContext(), "tel:" + number);
            }
        }

        public void addToContact() {
            Intent addContactIntent = MessageUtils.getShowOrCreateContactIntent(getNumber());
            if (addContactIntent != null) {
                this.mParentFragment.startActivityForResult(addContactIntent, 108);
            }
        }

        public void viewPeopleInfo() {
            if (this.mParentFragment != null && (this.mParentFragment instanceof ComposeMessageFragment)) {
                ((ComposeMessageFragment) this.mParentFragment).setmCryptoToastIsShow(true);
            }
            ContactList contactList = getContactList();
            Intent intent;
            if (contactList != null && contactList.size() == 1 && ((Contact) contactList.get(0)).existsInDatabase()) {
                intent = new Intent("android.intent.action.VIEW", ((Contact) contactList.get(0)).getUri());
                intent.setFlags(67108864);
                if (this.mParentFragment != null) {
                    this.mParentFragment.startActivity(intent);
                }
            } else if (getThreadId() != -1 && this.mParentFragment != null) {
                intent = new Intent(this.mParentFragment.getContext(), RecipientListActivity.class);
                intent.putExtra("thread_id", getThreadId());
                if (HwMessageUtils.isSplitOn()) {
                    recipientListFragment = new RecipientListFragment();
                    recipientListFragment.setIntent(intent);
                    ((ConversationList) this.mParentFragment.getActivity()).changeRightAddToStack(recipientListFragment, this.mParentFragment);
                    return;
                }
                this.mParentFragment.startActivity(intent);
            } else if (contactList != null && contactList.size() > 1 && this.mParentFragment != null) {
                intent = new Intent(this.mParentFragment.getContext(), RecipientListActivity.class);
                intent.putExtra("recipients", contactList.getNumbers());
                if (HwMessageUtils.isSplitOn()) {
                    recipientListFragment = new RecipientListFragment();
                    recipientListFragment.setIntent(intent);
                    ((ConversationList) this.mParentFragment.getActivity()).changeRightAddToStack(recipientListFragment, this.mParentFragment);
                    return;
                }
                this.mParentFragment.startActivity(intent);
            }
        }

        public boolean hasWeichat() {
            return this.mRelatedContactUri[0] != null;
        }

        public boolean hasWhatsapp() {
            boolean z = true;
            if (!MmsConfig.isEnableWhatsApp()) {
                return false;
            }
            if (this.mRelatedContactUri[1] == null) {
                z = false;
            }
            return z;
        }

        public void writeWeichat() {
            MessageUtils.gotoWeichat(this.mParentFragment.getContext());
        }

        protected void checkContactHasRelatedContact() {
        }

        public void writeWhatsapp() {
        }

        public void addToBlacklist(boolean add, Runnable onComplete) {
            String number = getNumber();
            boolean isAlreadyBlocked = BlacklistCommonUtils.isNumberBlocked(number);
            if (!(add && isAlreadyBlocked) && (add || isAlreadyBlocked)) {
                if (add) {
                    BlacklistCommonUtils.comfirmAddContactToBlacklist(this.mParentFragment.getContext(), number, onComplete);
                } else {
                    BlacklistCommonUtils.setNumberBlocked(number, false);
                    BlacklistCommonUtils.toastAddOrRemoveBlacklistInfo(this.mParentFragment.getContext(), false);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        }

        protected void checkContactChange() {
        }

        public boolean isHwMsgSender() {
            return false;
        }
    }

    private class ConversationInputHostHolder implements ConversationInputHost, OnAttachmentClickListener {
        private ConversationInputHostHolder() {
        }

        public MediaPicker createMediaPicker() {
            return new MediaPicker(ComposeMessageFragment.this.getActivity());
        }

        public void onMediaItemsSelected(Collection<AttachmentSelectData> attachmentItems) {
            if (attachmentItems != null) {
                for (AttachmentSelectData -wrap40 : attachmentItems) {
                    ComposeMessageFragment.this.setAttachmentSelectData(-wrap40, false);
                }
            }
        }

        public void onMediaItemsUnselected(AttachmentSelectData attachmentItem) {
            if (attachmentItem != null) {
                switch (attachmentItem.getAttachmentType()) {
                    case 2:
                    case 5:
                    case 8:
                        if (attachmentItem.getAttachmentUri() != null) {
                            ComposeMessageFragment.this.mRichEditor.removeImageAttachment(attachmentItem.getAttachmentUri(), attachmentItem.getAttachmentType());
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        }

        public void resumeComposeMessage() {
            ComposeMessageFragment.this.mRichEditor.requestFocus();
            if (ComposeMessageFragment.this.mIsLandscape) {
                ComposeMessageFragment.this.mConversationInputManager.showHideMediaPicker(false, true);
                return;
            }
            ComposeMessageFragment.this.mConversationInputManager.showHideMediaPicker(false, false);
            ComposeMessageFragment.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    ComposeMessageFragment.this.showKeyboard();
                }
            }, 100);
        }

        public void onMediaFullScreenChanged(boolean fullscreen, int type) {
            boolean isEnter = false;
            switch (type) {
                case 101:
                    break;
                case 102:
                    if (!fullscreen) {
                        ComposeMessageFragment.this.exitGalleryFullUpdate();
                        isEnter = false;
                        break;
                    }
                    ComposeMessageFragment.this.enterGalleryFullUpdate();
                    isEnter = true;
                    break;
                case LocationRequest.PRIORITY_LOW_POWER /*104*/:
                    if (!fullscreen) {
                        ComposeMessageFragment.this.exitGalleryFullUpdate();
                        isEnter = false;
                        break;
                    }
                    ComposeMessageFragment.this.enterLocationFullUpdate();
                    isEnter = true;
                    break;
                default:
                    if (!fullscreen) {
                        ComposeMessageFragment.this.exitTitleMode();
                        isEnter = false;
                        break;
                    }
                    ComposeMessageFragment.this.enterTitleModeUpdate();
                    isEnter = true;
                    break;
            }
            showTitleWhenSplit(isEnter);
        }

        public void showTitleWhenSplit(boolean enter) {
            if (HwMessageUtils.isSplitOn() && (ComposeMessageFragment.this.getActivity() instanceof ConversationList) && ((ConversationList) ComposeMessageFragment.this.getActivity()).getFragmentContainer() != null) {
                if (enter) {
                    if (((ConversationList) ComposeMessageFragment.this.getActivity()).getFragmentContainer().getLeftLayout() != null) {
                        ((ConversationList) ComposeMessageFragment.this.getActivity()).getFragmentContainer().getLeftLayout().setVisibility(8);
                    }
                } else if (((ConversationList) ComposeMessageFragment.this.getActivity()).getFragmentContainer().getLeftLayout() != null && ((ConversationList) ComposeMessageFragment.this.getActivity()).isSplitState()) {
                    ((ConversationList) ComposeMessageFragment.this.getActivity()).getFragmentContainer().getLeftLayout().setVisibility(0);
                }
            }
        }

        public void onPendingOperate(int type, AttachmentSelectData attachmentItem) {
            switch (type) {
                case 1001:
                    if (!ComposeMessageFragment.this.mCryptoCompose.needNotifyUser(type)) {
                        ComposeMessageFragment.this.startActivityForResult(MessageUtils.getSelectImageIntent(ComposeMessageFragment.this.getActivity(), 5), 144);
                        break;
                    }
                    ComposeMessageFragment.this.mCryptoCompose.addAttachment(ComposeMessageFragment.this.mRichEditor, type, false);
                    MLog.d("Mms_UI_CMA", "addAttachment crypto message could not add attachment.");
                    return;
                case 1002:
                    boolean isRcsMode = false;
                    if (ComposeMessageFragment.this.mComposeBottomView != null) {
                        isRcsMode = ComposeMessageFragment.this.mComposeBottomView.getCurrentMessageMode();
                    }
                    Intent intent = MessageUtils.getGalleryCompressIntent(ComposeMessageFragment.this.getActivity(), attachmentItem == null ? 0 : attachmentItem.getPosition(), isRcsMode, false);
                    if (ComposeMessageFragment.this.mRcsComposeMessage != null) {
                        ComposeMessageFragment.this.mRcsComposeMessage.setFullScreenFlag(true);
                        ComposeMessageFragment.this.mRcsComposeMessage.configFullScreenIntent(intent);
                    }
                    ComposeMessageFragment.this.startActivity(intent);
                    break;
                case 1003:
                    ComposeMessageFragment.this.addAttachment(11, false);
                    break;
                case 1004:
                    ComposeMessageFragment.this.addAttachment(10, false);
                    break;
                case 1005:
                    ComposeMessageFragment.this.addAttachment(7, false);
                    break;
                case 1006:
                    ComposeMessageFragment.this.addAttachment(6, false);
                    break;
                case 1007:
                    ComposeMessageFragment.this.addAttachment(13, false);
                    break;
                case 1008:
                    ComposeMessageFragment.this.addAttachment(14, false);
                    break;
                case 1009:
                    ComposeMessageFragment.this.addAttachment(23, false);
                    break;
            }
        }

        public boolean onAttachmentClick(SlideModel attachment, int viewType) {
            if (ComposeMessageFragment.this.mRichEditor == null) {
                return false;
            }
            switch (viewType) {
                case 2:
                    if (attachment.hasImage()) {
                        ImageModel image = attachment.getImage();
                        ComposeMessageFragment.this.setNeedSaveDraftStatus(false);
                        ComposeMessageFragment.this.mRichEditor.getWorkingMessage().saveAsMms(false, false);
                        ComposeMessageFragment.this.mRichEditor.viewAttach(image);
                        StatisticalHelper.reportEvent(ComposeMessageFragment.this.getContext(), 2262, String.valueOf(2));
                        break;
                    }
                    break;
                case 3:
                    if (attachment.hasAudio()) {
                        ComposeMessageFragment.this.mRichEditor.viewAttach(attachment.getAudio());
                        StatisticalHelper.reportEvent(ComposeMessageFragment.this.getContext(), 2262, String.valueOf(3));
                        break;
                    }
                    break;
                case 5:
                    if (attachment.hasVideo()) {
                        VideoModel video = attachment.getVideo();
                        ComposeMessageFragment.this.setNeedSaveDraftStatus(false);
                        ComposeMessageFragment.this.mRichEditor.getWorkingMessage().saveAsMms(false, false);
                        ComposeMessageFragment.this.mRichEditor.viewAttach(video);
                        StatisticalHelper.reportEvent(ComposeMessageFragment.this.getContext(), 2262, String.valueOf(5));
                        break;
                    }
                    break;
                case 6:
                    MediaListItem.viewVcardDetail(attachment.getVcard(), ComposeMessageFragment.this.getContext());
                    StatisticalHelper.reportEvent(ComposeMessageFragment.this.getContext(), 2262, String.valueOf(6));
                    break;
                case 8:
                    if (attachment.hasLocation()) {
                        HashMap<String, String> locInfo = attachment.getImage().getLocationSource();
                        ComposeMessageFragment.this.setNeedSaveDraftStatus(false);
                        ComposeMessageFragment.this.mRichEditor.getWorkingMessage().saveAsMms(false, false);
                        if (locInfo != null) {
                            RcsMapLoaderFactory.getMapLoader(ComposeMessageFragment.this.getContext()).loadMap(ComposeMessageFragment.this.getContext(), locInfo);
                            break;
                        }
                    }
                    break;
            }
            return false;
        }

        public void onChooserSlected(MediaChooser mediaChooser) {
            if (ComposeMessageFragment.this.isInLandscape() && mediaChooser != null) {
                if (mediaChooser instanceof CameraMediaChooser) {
                    if (ComposeMessageFragment.this.mActionBarWhenSplit.getActionMode() == 4) {
                        ComposeMessageFragment.this.exitGalleryFullUpdate();
                    } else if (ComposeMessageFragment.this.mActionBarWhenSplit.getActionMode() == 5) {
                        ComposeMessageFragment.this.exitTitleMode();
                    }
                } else if (mediaChooser instanceof GalleryMediaChooser) {
                    ComposeMessageFragment.this.enterGalleryFullUpdate();
                } else if (mediaChooser instanceof MapMediaChooser) {
                    ComposeMessageFragment.this.enterLocationFullUpdate();
                } else {
                    ComposeMessageFragment.this.enterTitleModeUpdate();
                }
            }
        }

        public void deleteAttachmentView(SlideModel slideModel, int type) {
            if (slideModel != null && ComposeMessageFragment.this.mRichEditor != null) {
                ComposeMessageFragment.this.mRichEditor.removeSlide(slideModel, type);
                StatisticalHelper.reportEvent(ComposeMessageFragment.this.getContext(), 2261, String.valueOf(type));
            }
        }

        public boolean isShowSlide() {
            if (ComposeMessageFragment.this.mRichEditor == null) {
                return false;
            }
            return ComposeMessageFragment.this.mRichEditor.getShowHwSlidePage();
        }

        public void updateStateLoaded() {
            ComposeMessageFragment.this.mHandler.post(new Runnable() {
                public void run() {
                    if (ComposeMessageFragment.this.mAttachmentPreview != null) {
                        ComposeMessageFragment.this.mAttachmentPreview.setMultiAttachmentScrollState(true);
                        ComposeMessageFragment.this.mAttachmentPreview.refreshAttachmentScroll();
                    }
                }
            });
        }

        public int getSlideCounts() {
            if (ComposeMessageFragment.this.mRichEditor == null) {
                return 0;
            }
            return ComposeMessageFragment.this.mRichEditor.getSlideSize();
        }

        public void invalidateActionBar() {
            if (ComposeMessageFragment.this.mConversationInputManager != null && ComposeMessageFragment.this.mActionBarWhenSplit != null) {
                ComposeMessageFragment.this.mConversationInputManager.updateActionBar(ComposeMessageFragment.this.mActionBarWhenSplit);
            }
        }

        public boolean onRcsAttachmentClick(MediaModel attachment, int viewType) {
            return false;
        }

        public void deleteRcsAttachmentView(MediaModel attachment, int type) {
        }
    }

    private class ConversationUpdator {
        Conversation mConv = null;
        private long oldId = 0;

        public ConversationUpdator(Conversation conv) {
            this.oldId = conv.getThreadId();
            this.mConv = conv;
        }

        public void checkRecipientShow() {
            if (0 != this.oldId && 0 == this.mConv.getThreadId()) {
                ComposeMessageFragment.this.mComposeRecipientsView.showRecipientEditor();
            }
        }
    }

    public class CustComposeCallbackHandler implements Callback {
        final /* synthetic */ ComposeMessageFragment this$0;

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    this.this$0.confirmSendMessageIfNeeded();
                    break;
                default:
                    MLog.e("Mms_UI_CMA", "no such event handled");
                    break;
            }
            return false;
        }
    }

    public static class DirtyModel {
        int mDirtySlideSize;
        String mDirtySub;
        String mDirtyText;
        Uri mDirtyUri;
        String mRecipients;

        public void setDirtyText(String txt) {
            this.mDirtyText = txt;
        }

        public void setDirtySub(String subject) {
            this.mDirtySub = subject;
        }

        public void setDirtyUri(Uri uri) {
            this.mDirtyUri = uri;
        }

        public void setWorkingRecipients(String recipients) {
            this.mRecipients = recipients;
        }

        public void setSlideSize(int slidSize) {
            this.mDirtySlideSize = slidSize;
        }

        public String getDirtyText() {
            return this.mDirtyText;
        }

        public String getDirtySub() {
            return this.mDirtySub;
        }

        public Uri getDirtyUri() {
            return this.mDirtyUri;
        }

        public String getWorkingRecipients() {
            return this.mRecipients;
        }

        public int getSlideSize() {
            return this.mDirtySlideSize;
        }
    }

    private class DiscardDraftListener implements DialogInterface.OnClickListener {
        private Intent mNewIntent = null;

        public DiscardDraftListener(Intent intent) {
            this.mNewIntent = intent;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            ComposeMessageFragment.this.mRichEditor.discard();
            dialog.dismiss();
            if (this.mNewIntent != null) {
                ComposeMessageFragment.this.finish();
                ComposeMessageFragment.this.getController().startComposeMessage(this.mNewIntent, 0, 0);
                return;
            }
            ComposeMessageFragment.this.finish();
        }
    }

    private class EMUIListViewListener implements EmuiListViewListener {
        private EMUIListViewListener() {
        }

        public void onEnterEditMode() {
            ComposeMessageFragment.this.mMenuEx.switchToEdit(true);
        }

        public void onExitEditMode() {
            ComposeMessageFragment.this.mMsgListAdapter.notifyDataSetChanged();
            ComposeMessageFragment.this.mMenuEx.switchToEdit(false);
        }

        public EditHandler getHandler(int mode) {
            MLog.e("Mms_UI_CMA", "Unsupport method in EMUI3.0, should't be called");
            return null;
        }

        public String getHintText(int mode, int count) {
            if (mode == 2) {
                return ResEx.self().getOperTextDelete(count);
            }
            if (mode == 4) {
                ResEx.self().getOperTextMultiForward(count);
            }
            return "";
        }

        public int getHintColor(int mode, int count) {
            if (count <= 0 || mode != 2) {
                return ResEx.self().getCachedColor(R.color.sms_number_save_disable);
            }
            return ResEx.self().getCachedColor(R.drawable.text_color_red);
        }
    }

    private class EMUIListViewListenerV3 extends EMUIListViewListener implements SelectionChangedListener, OnItemLongClickListener {
        private HashMap<View, Integer> hashMap;

        private EMUIListViewListenerV3() {
            super();
            this.hashMap = new HashMap();
        }

        private void saveViewState() {
            ComposeMessageFragment.this.mComposeChoosePanel.hidePanel();
            if (ComposeMessageFragment.this.mConversationInputManager.isMediaPickerVisible()) {
                ComposeMessageFragment.this.mConversationInputManager.showHideMediaPicker(false, false);
            }
            this.hashMap.clear();
            this.hashMap.put(ComposeMessageFragment.this.mBottomView, Integer.valueOf(ComposeMessageFragment.this.mBottomView.getVisibility()));
            if (!(ComposeMessageFragment.this.mSmartSmsComponse == null || ComposeMessageFragment.this.mSmartSmsComponse.getMenuRootView() == null)) {
                this.hashMap.put(ComposeMessageFragment.this.mSmartSmsComponse.getMenuRootView(), Integer.valueOf(ComposeMessageFragment.this.mSmartSmsComponse.getMenuRootView().getVisibility()));
            }
            for (View v : this.hashMap.keySet()) {
                v.setVisibility(8);
            }
            ComposeMessageFragment.this.hideKeyboard();
        }

        private void restoreViewState() {
            for (Entry<View, Integer> entry : this.hashMap.entrySet()) {
                ((View) entry.getKey()).setVisibility(((Integer) entry.getValue()).intValue());
            }
        }

        private void updateActionBarTitle(int size) {
            ComposeMessageFragment.this.updateComposeTitle(size);
        }

        public void onSelectChange(int selectedSize, int totalSize) {
            boolean z = false;
            ComposeMessageFragment composeMessageFragment = ComposeMessageFragment.this;
            if (selectedSize == totalSize && selectedSize > 0) {
                z = true;
            }
            composeMessageFragment.mIsAllSelected = z;
            ComposeMessageFragment.this.mMenuEx.setAllChecked(ComposeMessageFragment.this.mIsAllSelected, ComposeMessageFragment.this.isInLandscape());
            updateActionBarTitle(selectedSize);
            ComposeMessageFragment.this.mMenuEx.switchToEdit(true);
        }

        public void onEnterEditMode() {
            if (ComposeMessageFragment.this.mHwCustComposeMessage != null && ComposeMessageFragment.this.mHwCustComposeMessage.getSearchMode()) {
                ComposeMessageFragment.this.mHwCustComposeMessage.updateSearchMode();
            }
            saveViewState();
            ComposeMessageFragment.this.enterEditUpdate();
        }

        public void onExitEditMode() {
            restoreViewState();
            ComposeMessageFragment.this.mMenuEx.clearOptionMenu();
            ComposeMessageFragment.this.exitEditUpdate();
            ComposeMessageFragment.this.updateTitle(ComposeMessageFragment.this.mConversation.getRecipients());
        }

        public boolean onItemLongClick(AdapterView<?> listView, View view, int position, long id) {
            if (ComposeMessageFragment.this.mMsgListView.isDragListenerCalled()) {
                return true;
            }
            View lv = listView.getChildAt(position - listView.getFirstVisiblePosition());
            if (lv == null || !(lv instanceof MessageListItem)) {
                return true;
            }
            MessageListItem messageListItem = (MessageListItem) lv;
            ComposeMessageFragment.this.mMessageBlockView = messageListItem.getMessageBlockSuper();
            ComposeMessageFragment.this.mRichBubbleLayoutParent = messageListItem.getRichBubbleLayoutParent();
            ComposeMessageFragment.this.mIsShowingRich = messageListItem.isShowingRich();
            if (!ComposeMessageFragment.this.mIsShowingRich || ComposeMessageFragment.this.mRichBubbleLayoutParent == null) {
                ComposeMessageFragment.this.mShowPopupFloatingToolbarView = ComposeMessageFragment.this.mMessageBlockView;
            } else {
                ComposeMessageFragment.this.mShowPopupFloatingToolbarView = ComposeMessageFragment.this.mRichBubbleLayoutParent;
            }
            if (ComposeMessageFragment.this.mRcsComposeMessage == null || !ComposeMessageFragment.this.mRcsComposeMessage.isSelectOnlyOneItem(ComposeMessageFragment.this.mMsgListView)) {
                if (id <= 0) {
                    id = -id;
                }
                ComposeMessageFragment.this.mMsgItem = ComposeMessageFragment.this.mMsgListAdapter.getCachedMessageItem(ComposeMessageFragment.this.mMsgListView.getMsgType(position), Long.valueOf(id).longValue(), ComposeMessageFragment.this.mMsgListAdapter.getCursor());
            } else {
                MessageItem messageItemWithIdAssigned;
                ComposeMessageFragment composeMessageFragment = ComposeMessageFragment.this;
                if (ComposeMessageFragment.this.mMsgListAdapter.getRcsMessageListAdapter() != null) {
                    messageItemWithIdAssigned = ComposeMessageFragment.this.mMsgListAdapter.getRcsMessageListAdapter().getMessageItemWithIdAssigned(position, ComposeMessageFragment.this.mMsgListAdapter.getCursor());
                } else {
                    messageItemWithIdAssigned = null;
                }
                composeMessageFragment.mMsgItem = messageItemWithIdAssigned;
            }
            if (ComposeMessageFragment.this.mMsgItem == null) {
                return true;
            }
            ComposeMessageFragment.this.hideKeyboard();
            ComposeMessageFragment.this.mPosition = position;
            if (ComposeMessageFragment.this.mMsgItem != null) {
                ComposeMessageFragment.this.mIsCanDelete = true;
                ComposeMessageFragment.this.mIsCanSelectAll = true;
            }
            if (MessageUtils.msgHasText(ComposeMessageFragment.this.getContext(), ComposeMessageFragment.this.mMsgItem)) {
                ComposeMessageFragment.this.mIsCanCopyText = true;
                ComposeMessageFragment.this.mIsCanSeleteText = true;
            }
            boolean enableForwardItem = (!ComposeMessageFragment.this.mIsPushConversation || ComposeMessageFragment.this.mHwCustComposeMessage == null) ? true : ComposeMessageFragment.this.mHwCustComposeMessage.allowFwdWapPushMsg();
            if (enableForwardItem && ComposeMessageFragment.this.mMsgItem.isDownloaded()) {
                ComposeMessageFragment.this.mIsCanForward = true;
            }
            if (ComposeMessageFragment.this.mMsgItem == null) {
                return true;
            }
            switch (ComposeMessageFragment.this.mMsgItem.mAttachmentType) {
                case -2:
                case 0:
                    break;
                default:
                    if (ComposeMessageFragment.this.haveSomethingToCopyToSDCard(ComposeMessageFragment.this.mMsgItem.mMsgId)) {
                        ComposeMessageFragment.this.mIsCanSave = true;
                        break;
                    }
                    break;
            }
            if (ComposeMessageFragment.this.mRcsComposeMessage != null && (ComposeMessageFragment.this.mMsgItem instanceof RcsFileTransMessageItem)) {
                ComposeMessageFragment.this.mIsCanCopyText = false;
                ComposeMessageFragment.this.mIsCanSeleteText = false;
                RcsFileTransMessageItem fileTransMsgItem = (RcsFileTransMessageItem) ComposeMessageFragment.this.mMsgItem;
                File attachmentFile = fileTransMsgItem.getAttachmentFile();
                boolean isFileCanSave = fileTransMsgItem.mIsOutgoing || fileTransMsgItem.mImAttachmentStatus == 1002;
                if (attachmentFile != null && attachmentFile.exists() && isFileCanSave) {
                    ComposeMessageFragment.this.mIsCanForward = true;
                    ComposeMessageFragment.this.mIsCanSave = true;
                } else {
                    ComposeMessageFragment.this.mIsCanForward = false;
                    ComposeMessageFragment.this.mIsCanSave = false;
                }
                if (fileTransMsgItem.isVCardFile()) {
                    ComposeMessageFragment.this.mIsCanSave = false;
                }
            }
            if (ComposeMessageFragment.this.mRcsComposeMessage != null && ComposeMessageFragment.this.mMsgItem.getRcsMessageItem().mRcsMsgExtType == 6) {
                ComposeMessageFragment.this.mIsCanSelectAll = true;
                ComposeMessageFragment.this.mIsCanForward = true;
                ComposeMessageFragment.this.mIsCanCopyText = false;
                ComposeMessageFragment.this.mIsCanDelete = true;
                ComposeMessageFragment.this.mIsCanSeleteText = false;
                ComposeMessageFragment.this.mIsCanSave = false;
            }
            if (CryptoMessageUtil.isMsgEncrypted(MessageUtils.getMsgText(ComposeMessageFragment.this.getContext(), ComposeMessageFragment.this.mMsgItem))) {
                ComposeMessageFragment.this.mIsCanSelectAll = true;
                ComposeMessageFragment.this.mIsCanForward = false;
                ComposeMessageFragment.this.mIsCanCopyText = false;
                ComposeMessageFragment.this.mIsCanDelete = true;
                ComposeMessageFragment.this.mIsCanSeleteText = false;
                ComposeMessageFragment.this.mIsCanSave = false;
            }
            ComposeMessageFragment.this.showPopupFloatingToolbar();
            return true;
        }
    }

    private class FloatingCallback2 extends Callback2 {
        private boolean mWasAlreadyClick;

        private FloatingCallback2() {
            this.mWasAlreadyClick = false;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setSubtitle(null);
            mode.setTitle(null);
            mode.setTitleOptionalHint(true);
            populateMenuWithItems(menu);
            return true;
        }

        private void populateMenuWithItems(Menu menu) {
            TypefaceSpan span = new TypefaceSpan("default");
            if (ComposeMessageFragment.this.mIsCanCopyText) {
                SpannableString spanString = new SpannableString(ComposeMessageFragment.this.getString(R.string.button_copy_text));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 1, 0, spanString).setShowAsAction(2);
            }
            if (ComposeMessageFragment.this.mIsCanForward) {
                spanString = new SpannableString(ComposeMessageFragment.this.getString(R.string.forward_message));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 2, 1, spanString).setShowAsAction(2);
            }
            if (ComposeMessageFragment.this.mIsCanDelete) {
                spanString = new SpannableString(ComposeMessageFragment.this.getString(R.string.delete));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 3, 2, spanString).setShowAsAction(2);
            }
            if (ComposeMessageFragment.this.mIsCanSeleteText) {
                spanString = new SpannableString(ComposeMessageFragment.this.getString(R.string.mms_select_text_copy));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 4, 3, spanString).setShowAsAction(2);
            }
            if (ComposeMessageFragment.this.mIsCanSave) {
                spanString = new SpannableString(ComposeMessageFragment.this.getString(R.string.save));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 5, 4, spanString).setShowAsAction(1);
            }
            if (ComposeMessageFragment.this.mIsCanSelectAll) {
                spanString = new SpannableString(ComposeMessageFragment.this.getString(R.string.menu_add_rcs_more));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 6, 5, spanString).setShowAsAction(1);
            }
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (this.mWasAlreadyClick) {
                return true;
            }
            switch (item.getItemId()) {
                case 1:
                    this.mWasAlreadyClick = true;
                    HwMessageUtils.copyToClipboard(ComposeMessageFragment.this.getContext(), MessageUtils.getMsgText(ComposeMessageFragment.this.getContext(), ComposeMessageFragment.this.mMsgItem));
                    break;
                case 2:
                    this.mWasAlreadyClick = true;
                    Integer[] selection = new Integer[]{Integer.valueOf(ComposeMessageFragment.this.mPosition)};
                    if (ComposeMessageFragment.this.mRcsComposeMessage == null || !ComposeMessageFragment.this.mRcsComposeMessage.detectMessageToForwardForFtPop(ComposeMessageFragment.this.mMsgListView, ComposeMessageFragment.this.mMsgListAdapter.getCursor(), selection)) {
                        if (ComposeMessageFragment.this.mRcsComposeMessage != null && ComposeMessageFragment.this.mRcsComposeMessage.detectMessageToForwardForLocPop(ComposeMessageFragment.this.mMsgListView, ComposeMessageFragment.this.mMsgListAdapter.getCursor(), selection)) {
                            ComposeMessageFragment.this.mRcsComposeMessage.forwardLoc(ComposeMessageFragment.this.mThreadID);
                            break;
                        }
                        ComposeMessageFragment.this.mMsgListView.forwardMessage(ComposeMessageFragment.this.mMsgItem);
                        break;
                    }
                    ComposeMessageFragment.this.mRcsComposeMessage.toForward(ComposeMessageFragment.this.mThreadID);
                    MLog.v("Mms_UI_CMA", "onOptionsItemSelected detectMessageToForwardForFt true, forwardMsg.");
                    break;
                    break;
                case 3:
                    this.mWasAlreadyClick = true;
                    ComposeMessageFragment.this.mMsgListView.addItem(ComposeMessageFragment.this.mPosition);
                    ComposeMessageFragment.this.mPopId = 278925315;
                    ComposeMessageFragment.this.mMsgListView.onMenuItemClick(ComposeMessageFragment.this.mPopId);
                    ComposeMessageFragment.this.mMsgListView.removeItem(ComposeMessageFragment.this.mPosition);
                    break;
                case 4:
                    if (ComposeMessageFragment.this.mMsgItem != null) {
                        if (HwMessageUtils.isSplitOn()) {
                            MessageUtils.viewMessageText(ComposeMessageFragment.this.getContext(), ComposeMessageFragment.this.mMsgItem, ComposeMessageFragment.this);
                        } else {
                            MessageUtils.viewMessageText(ComposeMessageFragment.this.getContext(), ComposeMessageFragment.this.mMsgItem);
                        }
                        this.mWasAlreadyClick = true;
                        break;
                    }
                    return true;
                case 5:
                    StatisticalHelper.incrementReportCount(ComposeMessageFragment.this.getContext(), 2227);
                    if (ComposeMessageFragment.this.mMsgItem != null) {
                        this.mWasAlreadyClick = true;
                        if (ComposeMessageFragment.this.mRcsComposeMessage == null || !ComposeMessageFragment.this.mRcsComposeMessage.saveFileToPhone(ComposeMessageFragment.this.mMsgItem)) {
                            MmsPduUtils.copyMediaAndShowResult(ComposeMessageFragment.this.getContext(), ComposeMessageFragment.this.mMsgItem.mMsgId);
                            break;
                        }
                    }
                    return true;
                case 6:
                    if (MmsConfig.isSmsEnabled(ComposeMessageFragment.this.getContext())) {
                        if (!ComposeMessageFragment.this.mMsgListView.isInEditMode() && !ComposeMessageFragment.this.mMsgListView.isDragListenerCalled()) {
                            this.mWasAlreadyClick = true;
                            StatisticalHelper.incrementReportCount(ComposeMessageFragment.this.getContext(), 2191);
                            ComposeMessageFragment.this.mMsgListView.enterEditMode(1);
                            ComposeMessageFragment.this.mMsgListView.setItemSelected(ComposeMessageFragment.this.mPosition);
                            if (ComposeMessageFragment.this.mMsgListView.getRecorder() != null) {
                                ComposeMessageFragment.this.mMsgListView.getRecorder().getRcsSelectRecorder().addPosition(ComposeMessageFragment.this.mPosition);
                            }
                            ComposeMessageFragment.this.mMsgListView.getRecorder().add(ComposeMessageFragment.this.mMsgListView.getItemIdAtPosition(ComposeMessageFragment.this.mPosition));
                            break;
                        }
                        return true;
                    }
                    break;
            }
            if (ComposeMessageFragment.this.mActionMode != null) {
                ComposeMessageFragment.this.mActionMode.finish();
                ComposeMessageFragment.this.mActionMode = null;
            }
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            ComposeMessageFragment.this.mRichBubbleLayoutParent = null;
            ComposeMessageFragment.this.mShowPopupFloatingToolbarView = null;
            ComposeMessageFragment.this.mIsShowingRich = false;
            ComposeMessageFragment.this.mMessageBlockView = null;
            ComposeMessageFragment.this.mIsCanCopyText = false;
            ComposeMessageFragment.this.mIsCanForward = false;
            ComposeMessageFragment.this.mIsCanDelete = false;
            ComposeMessageFragment.this.mIsCanSeleteText = false;
            ComposeMessageFragment.this.mIsCanSave = false;
            ComposeMessageFragment.this.mIsCanSelectAll = false;
        }

        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (ComposeMessageFragment.this.mShowPopupFloatingToolbarView != null) {
                outRect.set(0, 0, ComposeMessageFragment.this.mShowPopupFloatingToolbarView.getWidth(), ComposeMessageFragment.this.mShowPopupFloatingToolbarView.getHeight());
            }
        }
    }

    private class HwComposeCustHolder extends ComposeCommonHolder implements IHwCustComposeMessageCallback, IComposeBottomViewHolder, ICustMessageListHodler, ICryptoComposeHolder, CryptoMessageViewListener {
        private HwComposeCustHolder() {
            super();
        }

        public boolean updateSendButtonStateSimple(TextView sendButton, boolean cardEnabled, boolean readyToSend) {
            if (ComposeMessageFragment.this.mRcsComposeMessage != null) {
                return ComposeMessageFragment.this.mRcsComposeMessage.updateSendButtonStateSimple(sendButton, cardEnabled, readyToSend);
            }
            return cardEnabled;
        }

        public boolean isMsgListAdapterValid() {
            if (ComposeMessageFragment.this.mMsgListAdapter != null) {
                return ComposeMessageFragment.this.mMsgListAdapter.isCursorValid(ComposeMessageFragment.this.mMsgListAdapter.getCursor());
            }
            return false;
        }

        public MessageListView getMessageListView() {
            return ComposeMessageFragment.this.mMsgListView;
        }

        public void updateSendButtonInCust() {
            if (ComposeMessageFragment.this.mHandler != null) {
                ComposeMessageFragment.this.mHandler.sendPeriodMessage(1000, 100, 300);
            }
            if (ComposeMessageFragment.this.mRichEditor != null) {
                ComposeMessageFragment.this.mRichEditor.changeRcsEditorHint();
            }
        }

        public boolean isRecipientsVisiable() {
            if (ComposeMessageFragment.this.mComposeRecipientsView != null) {
                return ComposeMessageFragment.this.mComposeRecipientsView.isVisible();
            }
            return false;
        }

        public List<String> getRecipientsNum() {
            if (ComposeMessageFragment.this.mComposeRecipientsView != null) {
                return ComposeMessageFragment.this.mComposeRecipientsView.getNumbers();
            }
            return new ArrayList(0);
        }

        public void showVcalendarDlgFromCalendar(Uri vcsUri, ArrayList<Uri> uriList) {
            ComposeMessageFragment.this.showVcalendarDlgFromCalendar(vcsUri, uriList);
        }

        public void hideRecipientEditor() {
            ComposeMessageFragment.this.mComposeRecipientsView.hideRecipientEditor();
        }

        public RcsMessageListAdapter getHwCustMsgListAdapter() {
            if (ComposeMessageFragment.this.mMsgListAdapter != null) {
                return ComposeMessageFragment.this.mMsgListAdapter.getRcsMessageListAdapter();
            }
            return null;
        }

        public ContactList constructContactsFromInput(boolean blocking) {
            if (ComposeMessageFragment.this.mComposeRecipientsView != null) {
                return ComposeMessageFragment.this.mComposeRecipientsView.constructContactsFromInput(blocking);
            }
            return null;
        }

        public MessageListAdapter getMsgListAdapter() {
            return ComposeMessageFragment.this.mMsgListAdapter;
        }

        public void hidePanel() {
            ComposeMessageFragment.this.mComposeChoosePanel.hidePanel();
            if (ComposeMessageFragment.this.mConversationInputManager.isMediaPickerVisible()) {
                ComposeMessageFragment.this.mConversationInputManager.showHideMediaPicker(false, true);
            }
        }

        public void optPanel(boolean toShow) {
            if (ComposeMessageFragment.this.mRcsComposeMessage != null) {
                if (toShow) {
                    ComposeMessageFragment.this.mConversationInputManager.showHideMediaPicker(true, true);
                } else if (ComposeMessageFragment.this.mConversationInputManager.isMediaPickerVisible()) {
                    ComposeMessageFragment.this.mConversationInputManager.showHideMediaPicker(false, false);
                }
            }
        }

        public void setMenuExItemEnabled(int itemID, boolean isVisible) {
            ComposeMessageFragment.this.mMenuEx.setItemEnabled(itemID, isVisible);
        }

        public boolean isSubjectEditorVisible() {
            return ComposeMessageFragment.this.isSubjectEditorVisible();
        }

        public void updateSendButtonView() {
            ComposeMessageFragment.this.updateSendButtonView();
        }

        public void judgeAttachSmiley() {
            ComposeMessageFragment.this.judgeAttachSmiley();
        }

        public boolean isFromWidget() {
            return ComposeMessageFragment.this.isFromWidget();
        }

        public void goToConversationList() {
            ((ComposeMessageActivity) ComposeMessageFragment.this.getActivity()).goToConversationList();
        }

        public void addAttachment(int type, boolean replace) {
            ComposeMessageFragment.this.addAttachment(type, replace);
        }

        public void addAttachment(int type, AttachmentSelectData attachmentData, boolean replace) {
            ComposeMessageFragment.this.addAttachment(type, attachmentData, replace);
        }

        public void refreshMediaAttachment(int type) {
            ComposeMessageFragment.this.refreshAttachmentChangedUI(type);
        }

        public void showInvalidDestinationToast() {
            ComposeMessageFragment.this.mComposeRecipientsView.showInvalidDestinationToast();
        }

        public ContactList getComposeRecipientsViewRecipients() {
            return ComposeMessageFragment.this.mComposeRecipientsView.getRecipients();
        }

        public boolean isComposeRecipientsViewVisible() {
            return ComposeMessageFragment.this.mComposeRecipientsView.isVisible();
        }

        public void setMenuExItemVisible(int itemID, boolean isVisible) {
            ComposeMessageFragment.this.mMenuEx.setItemVisible(itemID, isVisible);
        }

        public void setRcsSaveDraftWhenFt(boolean rcsSaveDraftWhenFt) {
            if (ComposeMessageFragment.this.mRichEditor != null) {
                ComposeMessageFragment.this.mRichEditor.setRcsSaveDraftWhenFt(rcsSaveDraftWhenFt);
            }
        }

        public boolean getRcsLoadDraftFt() {
            if (ComposeMessageFragment.this.mRichEditor != null) {
                return ComposeMessageFragment.this.mRichEditor.getRcsLoadDraftFt();
            }
            return false;
        }

        public void editRcsMessageItem(MessageItem msgItem) {
            synchronized (ComposeMessageFragment.this.mConversation) {
                if (!ComposeMessageFragment.this.mConversation.isMessageCountValid() || ComposeMessageFragment.this.mConversation.isMessageCountSingle()) {
                    ComposeMessageFragment.this.mConversation.clearThreadId();
                    MessagingNotification.setRcsCurrentlyDisplayedThreadId(-2, 0);
                }
            }
            if (ComposeMessageFragment.this.mRcsComposeMessage != null && ComposeMessageFragment.this.mRcsComposeMessage.isFileItem(msgItem)) {
                ComposeMessageFragment.this.mRcsComposeMessage.updateFileDB(msgItem, ComposeMessageFragment.this.mBackgroundQueryHandler);
                ComposeMessageFragment.this.startMsgListQuery(9527);
            } else if (ComposeMessageFragment.this.mRcsComposeMessage != null) {
                ComposeMessageFragment.this.mBackgroundQueryHandler.startDelete(9786, Integer.valueOf(0), ComposeMessageFragment.this.mRcsComposeMessage.getDeleteUri(null, msgItem.mMsgId, msgItem.mType), null, null);
                ComposeMessageFragment.this.mRichEditor.setText(msgItem.mBody);
            }
        }

        public void beginMsgListQuery() {
            ComposeMessageFragment.this.startMsgListQuery();
        }

        public void prepareFwdMsg(String msgBody) {
            if (ComposeMessageFragment.this.mRcsComposeMessage != null) {
                ComposeMessageFragment.this.mRcsComposeMessage.prepareFwdMsg(msgBody);
            }
        }

        public void setSendButtonEnabled(int resId, boolean enabled) {
            ComposeMessageFragment.this.mComposeBottomView.setSendButtonEnabled(resId, enabled);
        }

        public CharSequence getText() {
            return ComposeMessageFragment.this.mRichEditor.getText();
        }

        public CharSequence get7BitText() {
            return ComposeMessageFragment.this.mRichEditor.get7BitText();
        }

        public boolean requiresMms() {
            return ComposeMessageFragment.this.mRichEditor.requiresMms();
        }

        public boolean isDraftWorthSaving() {
            return ComposeMessageFragment.this.mRichEditor.isWorthSaving();
        }

        public boolean hasAttachment() {
            return ComposeMessageFragment.this.mRichEditor.hasAttachment();
        }

        public boolean hasText() {
            return ComposeMessageFragment.this.mRichEditor.hasText();
        }

        public void setText(String text) {
            ComposeMessageFragment.this.mRichEditor.setWorkingMessageText(text);
        }

        public boolean isContainSignature() {
            if (ComposeMessageFragment.this.mRichEditor != null) {
                return ComposeMessageFragment.this.mRichEditor.isContainSignature();
            }
            return false;
        }

        public void deleteSmsDraft() {
            ComposeMessageFragment.this.mRichEditor.asyncDeleteDraftSmsMessage(ComposeMessageFragment.this.mConversation);
        }

        public void setLengthRequiresMms(boolean mmsRequired, boolean notify) {
            ComposeMessageFragment.this.mRichEditor.setLengthRequiresMms(mmsRequired, notify);
        }

        public int getAttachementViewHeight() {
            int height = ComposeMessageFragment.this.mAttachmentPreview.getMeasuredHeight();
            int minPxHeight = HwMessageUtils.dip2px(ComposeMessageFragment.this.getContext(), 90.0f);
            if (height <= 0 || height >= minPxHeight) {
                return height;
            }
            return minPxHeight;
        }
    }

    public interface MapClickCallback {
        void okClick();
    }

    protected class MenuEx extends EmuiMenu implements OnCustomMenuListener {
        public MenuEx() {
            super(null);
        }

        private MenuEx setOptionMenu(Menu menu) {
            this.mOptionMenu = menu;
            return this;
        }

        public boolean onPrepareOptionsMenu() {
            boolean isInEditMode;
            if (ComposeMessageFragment.this.mMsgListView != null) {
                isInEditMode = ComposeMessageFragment.this.mMsgListView.isInEditMode();
            } else {
                isInEditMode = false;
            }
            if (!isInEditMode) {
                return false;
            }
            prepareOptionsMenuInEditMode();
            switchToEdit(true);
            ComposeMessageFragment.this.mMsgListView.onMenuPrepared();
            ComposeMessageFragment.this.setCustomMenuClickListener();
            return true;
        }

        public void clearOptionMenu() {
            this.mOptionMenu.clear();
        }

        public void switchToEdit(boolean editable) {
            boolean msgHasText;
            boolean z;
            if (!editable) {
                clear();
            }
            Long[] selection = ComposeMessageFragment.this.mMsgListView.getRecorder().getAllSelectItems();
            boolean hasMmsItem = false;
            for (Long longValue : selection) {
                if (longValue.longValue() < 0) {
                    hasMmsItem = true;
                    break;
                }
            }
            if (ComposeMessageFragment.this.mRcsComposeMessage != null && ComposeMessageFragment.this.mRcsComposeMessage.isRcsSwitchOn()) {
                hasMmsItem = ComposeMessageFragment.this.mRcsComposeMessage.processAllSelectItem(ComposeMessageFragment.this.mMsgListView, ComposeMessageFragment.this.mMsgListAdapter, hasMmsItem);
                selection = ComposeMessageFragment.this.mRcsComposeMessage.getSelectedItems(ComposeMessageFragment.this.mMsgListView, selection);
            }
            if (ComposeMessageFragment.this.mRcsComposeMessage == null || !ComposeMessageFragment.this.mRcsComposeMessage.isRcsSwitchOn()) {
                msgHasText = MessageUtils.msgsHaveText(ComposeMessageFragment.this.getContext(), selection, ComposeMessageFragment.this.mMsgListAdapter, 0);
            } else {
                msgHasText = ComposeMessageFragment.this.mRcsComposeMessage.rcsMsgHasText(true);
            }
            boolean hasMmsNotiSelected = ComposeMessageFragment.this.mMsgListView.hasMmsNotiSelected();
            boolean allowFwdWapPushMsg = (!ComposeMessageFragment.this.mIsPushConversation || ComposeMessageFragment.this.mHwCustComposeMessage == null) ? true : ComposeMessageFragment.this.mHwCustComposeMessage.allowFwdWapPushMsg();
            if (hasMmsNotiSelected || (selection.length != 1 && (selection.length <= 1 || selection.length >= MmsConfig.getForwardLimitSize() || hasMmsItem))) {
                allowFwdWapPushMsg = false;
            }
            setItemEnabled(278925316, allowFwdWapPushMsg);
            boolean hasUnlock = ComposeMessageFragment.this.mMsgListView.hasUnlock();
            setItemEnabled(278925315, selection.length > 0);
            if (hasMmsNotiSelected || selection.length <= 0) {
                z = false;
            } else {
                z = PrivacyStateListener.self().isInPrivacyMode();
            }
            setItemEnabled(278925318, z);
            setItemEnabled(278925319, selection.length > 0 ? msgHasText : false);
            if (selection.length != 1) {
                msgHasText = false;
            }
            setItemVisible(278925343, msgHasText);
            if (ComposeMessageFragment.this.mRcsComposeMessage != null) {
                ComposeMessageFragment.this.mRcsComposeMessage.switchToEdit(this.mOptionMenu, hasMmsItem);
            }
            setItemVisible(278925321, false);
            setItemVisible(278925322, false);
            setItemVisible(278925323, false);
            setItemVisible(278925324, false);
            setItemVisible(278925351, false);
            setItemVisible(278925325, false);
            setItemVisible(278925326, false);
            setItemVisible(278925327, false);
            setItemVisible(278925328, false);
            setItemVisible(278925329, false);
            setItemVisible(278925330, false);
            setItemVisible(278925331, false);
            setItemVisible(278925332, false);
            setItemVisible(278925333, false);
            setItemVisible(278925334, false);
            setItemVisible(278925345, false);
            if (ComposeMessageFragment.this.mHwCustComposeMessage != null) {
                ComposeMessageFragment.this.mHwCustComposeMessage.switchToReplyMenuInEditMode(ComposeMessageFragment.this.mMenuEx, false);
            }
            setItemVisible(278927472, 1 == selection.length);
            setItemEnabled(278927472, true);
            if (!ComposeMessageFragment.this.mIsPushConversation) {
                if (hasUnlock && ComposeMessageFragment.this.mIsSmsEnabled) {
                    setItemVisible(278925331, selection.length > 0);
                } else if (ComposeMessageFragment.this.mIsSmsEnabled) {
                    setItemVisible(278925332, selection.length > 0);
                }
            }
            ComposeMessageFragment.this.mCryptoCompose.updateActionBarItemMenuStatus(ComposeMessageFragment.this.mMenuEx, ComposeMessageFragment.this.mMsgListAdapter, selection);
            if (1 == selection.length) {
                MessageItem msgItem;
                if (ComposeMessageFragment.this.mRcsComposeMessage == null || !ComposeMessageFragment.this.mRcsComposeMessage.isRcsSwitchOn()) {
                    long msgId = selection[0].longValue() > 0 ? selection[0].longValue() : -selection[0].longValue();
                    String type = selection[0].longValue() > 0 ? "sms" : "mms";
                    msgItem = ComposeMessageFragment.this.mMsgListAdapter.getCachedMessageItem(type, msgId, ComposeMessageFragment.this.mMsgListAdapter.getCursor());
                    if (msgItem == null) {
                        MLog.e("Mms_UI_CMA", "Cannot load message item for type = " + type + ", msgId = " + msgId);
                        return;
                    }
                }
                msgItem = ComposeMessageFragment.this.mRcsComposeMessage.onOptionsOnlyOneItemSelected(ComposeMessageFragment.this.mMsgListView, ComposeMessageFragment.this.mMsgListAdapter);
                if (msgItem == null) {
                    return;
                }
                if (msgItem.isOutgoingMessage() && msgItem.isFailedMessage() && ComposeMessageFragment.this.mIsSmsEnabled) {
                    setItemVisible(278925321, true);
                }
                if (msgItem.isSms()) {
                    if (ComposeMessageFragment.this.getRecipients().size() == 1 && ((msgItem.mBoxId == 4 || msgItem.mBoxId == 5) && ComposeMessageFragment.this.mIsSmsEnabled)) {
                        setItemVisible(278925322, true);
                        setItemVisible(278925321, false);
                    }
                    if (ComposeMessageFragment.this.mIsSmsEnabled) {
                        if (ComposeMessageFragment.this.mHwCustComposeMessage != null) {
                            ComposeMessageFragment.this.mHwCustComposeMessage.switchToEditSmsCust(this.mOptionMenu);
                        }
                        if (MessageUtils.isMultiSimEnabled()) {
                            if (MessageUtils.isMultiSimState()) {
                                setItemVisible(278925323, true);
                                setItemVisible(278925324, true);
                            } else if (1 == MessageUtils.getIccCardStatus(0) || 1 == MessageUtils.getIccCardStatus(1)) {
                                setItemVisible(278925351, true);
                            }
                        } else if (1 == MessageUtils.getIccCardStatus()) {
                            setItemVisible(278925325, true);
                        }
                        setItemVisible(278927472, true);
                    }
                }
                if (msgItem.isMms() && MmsConfig.getReplyAllEnabled() && 1 == msgItem.mBoxId && msgItem.getTo().length + msgItem.getCc().length > 1) {
                    setItemVisible(278925345, true);
                }
                if (ComposeMessageFragment.this.mHwCustComposeMessage != null) {
                    ComposeMessageFragment.this.mHwCustComposeMessage.switchToReplyMenuInEditMode(ComposeMessageFragment.this.mMenuEx, msgItem);
                }
                if (msgItem.isMms()) {
                    if (ComposeMessageFragment.this.mHwCustComposeMessage != null) {
                        ComposeMessageFragment.this.mHwCustComposeMessage.switchToEditMmsCust(this.mOptionMenu);
                    }
                    switch (msgItem.mBoxId) {
                        case 4:
                            if (ComposeMessageFragment.this.getRecipients().size() == 1 && ComposeMessageFragment.this.mIsSmsEnabled) {
                                setItemVisible(278925322, false);
                                break;
                            }
                    }
                    switch (msgItem.mAttachmentType) {
                        case -2:
                        case 0:
                            break;
                        case 1:
                        case 2:
                            if (ComposeMessageFragment.this.haveSomethingToCopyToSDCard(msgItem.mMsgId)) {
                                setItemVisible(278925326, true);
                                break;
                            }
                            break;
                        case 4:
                            setItemEnabled(278927472, false);
                            break;
                    }
                    if (ComposeMessageFragment.this.haveSomethingToCopyToSDCard(msgItem.mMsgId)) {
                        setItemVisible(278925326, true);
                    }
                    if (ComposeMessageFragment.this.isDrmRingtoneWithRights(msgItem.mMsgId)) {
                        setItemVisible(278925327, true);
                    }
                }
                if (msgItem.isMms() && msgItem.isHasVcard()) {
                    if (msgItem.mBoxId == 1) {
                        setItemVisible(278925328, true);
                    } else {
                        setItemVisible(278925329, true);
                    }
                }
                if (ComposeMessageFragment.this.mRcsComposeMessage != null) {
                    ComposeMessageFragment.this.mRcsComposeMessage.setVcardItemVisible(msgItem);
                    ComposeMessageFragment.this.mRcsComposeMessage.setMsgItemVisible(msgItem);
                    if (msgItem.getRcsMessageItem().mRcsMsgExtType == 6) {
                        setItemVisible(278927472, false);
                        setItemEnabled(278925316, true);
                    }
                }
                if (MmsConfig.getSupportedVCalendarEnabled() && msgItem.isMms() && msgItem.hasVCalendar()) {
                    setItemVisible(278925330, true);
                }
                if (!ComposeMessageFragment.this.mIsPushConversation) {
                    setItemVisible(278925333, true);
                }
                if (msgItem.getRcsMessageItem() != null && msgItem.getRcsMessageItem().isUndeliveredIm()) {
                    setItemVisible(278925334, false);
                } else if ((msgItem.mDeliveryStatus != DeliveryStatus.NONE || msgItem.mReadReport) && MmsConfig.getMMSDeliveryReportsEnabled()) {
                    setItemVisible(278925334, true);
                }
                ComposeMessageFragment.this.mCryptoCompose.updateOverflowMenu(ComposeMessageFragment.this.mMenuEx, ComposeMessageFragment.this.mMsgListAdapter, selection[0]);
            }
        }

        public boolean createOptionsMenu() {
            if (this.mOptionMenu == null || ComposeMessageFragment.this.mMsgListView == null) {
                return false;
            }
            this.mOptionMenu.clear();
            return true;
        }

        private boolean prepareOptionsMenuInEditMode() {
            resetOptionMenu(ComposeMessageFragment.this.getResetMenu());
            boolean inLandscape = ComposeMessageFragment.this.isInLandscape();
            clear();
            addMenuDelete(inLandscape);
            addMenuForawrd(inLandscape);
            addMenuFavorite(inLandscape);
            addMenuChoice(inLandscape);
            addOverflowMenu(278925319, R.string.button_copy_text);
            addOverflowMenu(278927472, R.string.button_share);
            addOverflowMenu(278925345, R.string.reply_all_action);
            if (ComposeMessageFragment.this.mHwCustComposeMessage != null) {
                ComposeMessageFragment.this.mHwCustComposeMessage.prepareReplyMenu(ComposeMessageFragment.this.mMenuEx);
            }
            addOverflowMenu(278925322, R.string.menu_edit);
            addOverflowMenu(278925321, R.string.resend);
            addOverflowMenu(278925325, R.string.menu_copy_to_sim);
            if (ComposeMessageFragment.this.mHwCustComposeMessage != null) {
                ComposeMessageFragment.this.mHwCustComposeMessage.prepareMenuInEditModeCust(this.mOptionMenu);
            }
            addOverflowMenu(278925351, R.string.menu_copy_to_sim);
            addOverflowMenu(278925323, R.string.menu_copy_to_card1);
            addOverflowMenu(278925324, R.string.menu_copy_to_card2);
            addOverflowMenu(278925343, R.string.mms_select_text_copy);
            addOverflowMenu(278925332, R.string.menu_unlock);
            addOverflowMenu(278925331, R.string.menu_lock);
            addOverflowMenu(278925327, R.string.save_ringtone);
            addOverflowMenu(278925328, R.string.save_to_contacts);
            addOverflowMenu(278925329, R.string.view_vcard_details);
            addOverflowMenu(278925330, R.string.menu_save_to_calendar);
            addOverflowMenu(278925326, R.string.copy_to_sdcard);
            addOverflowMenu(278925333, R.string.view_message_details);
            addOverflowMenu(278925334, R.string.view_delivery_report);
            ComposeMessageFragment.this.refreshMenu();
            return true;
        }

        public void setItemVisible(int itemID, boolean isVisible) {
            if (this.mOptionMenu != null) {
                MenuItem menuItem = this.mOptionMenu.findItem(itemID);
                if (menuItem != null) {
                    menuItem.setVisible(isVisible);
                }
            }
        }

        public boolean onCustomMenuItemClick(MenuItem item) {
            return onOptionsItemSelected(item);
        }

        private boolean onOptionsItemSelected(MenuItem item) {
            Long[] selection = ComposeMessageFragment.this.mMsgListView.getRecorder().getAllSelectItems();
            MessageItem messageItem = null;
            if (ComposeMessageFragment.this.mRcsComposeMessage != null && ComposeMessageFragment.this.mRcsComposeMessage.isSelectOnlyOneItem(ComposeMessageFragment.this.mMsgListView)) {
                messageItem = ComposeMessageFragment.this.mRcsComposeMessage.onOptionsOnlyOneItemSelected(ComposeMessageFragment.this.mMsgListView, ComposeMessageFragment.this.mMsgListAdapter);
                if (messageItem == null) {
                    return true;
                }
            } else if (selection != null && 1 == selection.length) {
                long msgId = selection[0].longValue() > 0 ? selection[0].longValue() : -selection[0].longValue();
                String type = selection[0].longValue() > 0 ? "sms" : "mms";
                messageItem = ComposeMessageFragment.this.mMsgListAdapter.getCachedMessageItem(type, msgId, ComposeMessageFragment.this.mMsgListAdapter.getCursor());
                if (messageItem == null) {
                    MLog.e("Mms_UI_CMA", "Cannot load message item for type = " + type + ", msgId = " + msgId);
                    return true;
                }
            }
            if (messageItem != null && ComposeMessageFragment.this.mHwCustComposeMessage != null && ComposeMessageFragment.this.mHwCustComposeMessage.handleCustMenu(item.getItemId(), messageItem, ComposeMessageFragment.this)) {
                return true;
            }
            ContactList contactList = ComposeMessageFragment.this.getRecipients();
            switch (item.getItemId()) {
                case 10:
                    ComposeMessageFragment.this.onAddContactsToMms();
                    break;
                case 12:
                case R.id.menu_view_contact:
                case R.id.menu_group_participants:
                    ComposeMessageFragment.this.mActionbarAdapter.viewPeopleInfo();
                    break;
                case 278925313:
                    boolean isAllSelected = !ComposeMessageFragment.this.mIsAllSelected;
                    ComposeMessageFragment.this.mMsgListView.setAllSelected(!ComposeMessageFragment.this.mIsAllSelected);
                    if (ComposeMessageFragment.this.mMsgListView.getHwCustMessageListView() != null) {
                        ComposeMessageFragment.this.mMsgListView.getHwCustMessageListView().setAllSelectedPosition(isAllSelected, ComposeMessageFragment.this.mMsgListView);
                    }
                    ComposeMessageFragment.this.mMsgListView.onMenuItemClick(278925313);
                    ComposeMessageFragment.this.mMsgListAdapter.notifyDataSetChanged();
                    switchToEdit(true);
                    return true;
                case 278925315:
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ComposeMessageFragment.this.getContext());
                    if (!MmsConfig.isSmsRecyclerEnable() || PreferenceUtils.isSmsRecoveryEnable(ComposeMessageFragment.this.getContext()) || prefs.getBoolean("pref_sms_recycle_not_show_again", false)) {
                        ComposeMessageFragment.this.mMsgListView.onMenuItemClick(item.getItemId());
                    } else {
                        ComposeMessageFragment.this.showSmsRecycleEnableDialog();
                    }
                    return true;
                case 278925316:
                    if (ComposeMessageFragment.this.mRcsComposeMessage != null && ComposeMessageFragment.this.mRcsComposeMessage.detectMessageToForwardForFt(ComposeMessageFragment.this.mMsgListView, ComposeMessageFragment.this.mMsgListAdapter.getCursor())) {
                        ComposeMessageFragment.this.mRcsComposeMessage.toForward(ComposeMessageFragment.this.mThreadID);
                        MLog.v("Mms_UI_CMA", "onOptionsItemSelected detectMessageToForwardForFt true, forwardMsg.");
                    } else if (ComposeMessageFragment.this.mRcsComposeMessage == null || !ComposeMessageFragment.this.mRcsComposeMessage.detectMessageToForwardForLoc(ComposeMessageFragment.this.mMsgListView, ComposeMessageFragment.this.mMsgListAdapter.getCursor())) {
                        ComposeMessageFragment.this.mMsgListView.forwardMsg();
                    } else {
                        ComposeMessageFragment.this.mRcsComposeMessage.forwardLoc(ComposeMessageFragment.this.mThreadID);
                    }
                    ComposeMessageFragment.this.mMsgListView.setAdapter(ComposeMessageFragment.this.mMsgListAdapter);
                    ComposeMessageFragment.this.mMsgListView.exitEditMode();
                    break;
                case 278925318:
                    ComposeMessageFragment.this.mMsgListView.onMenuItemClick(item.getItemId());
                    StatisticalHelper.incrementReportCount(ComposeMessageFragment.this.getContext(), 2113);
                    break;
                case 278925319:
                    Long[] selectedItems = ComposeMessageFragment.this.mMsgListView.getRecorder().getAllSelectItems();
                    if (selectedItems.length <= 10) {
                        if (ComposeMessageFragment.this.mRcsComposeMessage != null) {
                            selectedItems = ComposeMessageFragment.this.mRcsComposeMessage.getSelectedItems(ComposeMessageFragment.this.mMsgListView, selectedItems);
                        }
                        HwMessageUtils.copyToClipboard(ComposeMessageFragment.this.getContext(), MessageUtils.getSelectedMessageBodies(ComposeMessageFragment.this.getContext(), selectedItems, ComposeMessageFragment.this.mMsgListAdapter, ComposeMessageFragment.this.mMsgListView, 0));
                        break;
                    }
                    Toast.makeText(ComposeMessageFragment.this.getContext(), R.string.mms_upto_max_allowed_copy_count, 0).show();
                    return true;
                case 278925321:
                    if (messageItem != null) {
                        if (!MessageUtils.isNeedShowToastWhenNetIsNotAvailable(ComposeMessageFragment.this.getContext(), messageItem)) {
                            ComposeMessageFragment.this.reSend(messageItem);
                            break;
                        }
                    }
                    return true;
                    break;
                case 278925322:
                    ComposeMessageFragment.this.mMsgListView.exitEditMode();
                    if (messageItem != null) {
                        ComposeMessageFragment.this.editMessageItem(messageItem);
                        break;
                    }
                    return true;
                case 278925323:
                    if (messageItem != null) {
                        ComposeMessageFragment.startCopyMessageToSim(ComposeMessageFragment.this.mBackgroundQueryHandler, 1, ComposeMessageFragment.this.getSmsMsgItemValue(messageItem));
                        break;
                    }
                    return true;
                case 278925324:
                    if (messageItem != null) {
                        ComposeMessageFragment.startCopyMessageToSim(ComposeMessageFragment.this.mBackgroundQueryHandler, 2, ComposeMessageFragment.this.getSmsMsgItemValue(messageItem));
                        break;
                    }
                    return true;
                case 278925325:
                    if (messageItem != null) {
                        ComposeMessageFragment.startCopyMessageToSim(ComposeMessageFragment.this.mBackgroundQueryHandler, 0, ComposeMessageFragment.this.getSmsMsgItemValue(messageItem));
                        break;
                    }
                    return true;
                case 278925326:
                    if (messageItem != null) {
                        if (ComposeMessageFragment.this.mRcsComposeMessage != null && ComposeMessageFragment.this.mRcsComposeMessage.saveFileToPhone(messageItem)) {
                            MLog.v("Mms_UI_CMA", "onOptionsItemSelected mRcsComposeMessage.saveFileToPhone is true, break");
                            break;
                        }
                        MmsPduUtils.copyMediaAndShowResult(ComposeMessageFragment.this.getContext(), messageItem.mMsgId);
                        break;
                    }
                    return true;
                    break;
                case 278925327:
                    if (messageItem != null) {
                        int resId = ComposeMessageFragment.this.getDrmMimeSavedStringRsrc(messageItem.mMsgId, ComposeMessageFragment.this.saveRingtone(messageItem.mMsgId));
                        if (!ComposeMessageFragment.this.saveRingtone(messageItem.mMsgId)) {
                            Toast.makeText(ComposeMessageFragment.this.getContext(), resId, 0).show();
                            break;
                        }
                    }
                    return true;
                    break;
                case 278925328:
                    if (messageItem != null) {
                        if (ComposeMessageFragment.this.mRcsComposeMessage != null && ComposeMessageFragment.this.mRcsComposeMessage.saveVcard(messageItem)) {
                            MLog.v("Mms_UI_CMA", "onOptionsItemSelected mRcsComposeMessage.saveVcard is true, break");
                            break;
                        }
                        messageItem.saveVcard();
                        break;
                    }
                    return true;
                    break;
                case 278925329:
                    if (messageItem != null) {
                        if (ComposeMessageFragment.this.mRcsComposeMessage != null && ComposeMessageFragment.this.mRcsComposeMessage.viewVcardDetail(messageItem)) {
                            MLog.v("Mms_UI_CMA", "onOptionsItemSelected mRcsComposeMessage.viewVcardDetail is true, break");
                            break;
                        }
                        messageItem.viewVcardDetail();
                        break;
                    }
                    return true;
                    break;
                case 278925330:
                    if (messageItem != null) {
                        messageItem.saveVCalendar();
                        break;
                    }
                    return true;
                case 278925331:
                case 278925332:
                    ComposeMessageFragment.this.mMsgListView.onMenuItemClick(item.getItemId());
                    break;
                case 278925333:
                    if (messageItem != null) {
                        ComposeMessageFragment.this.showMessageDetails(messageItem);
                        break;
                    }
                    return true;
                case 278925334:
                    if (messageItem != null) {
                        ComposeMessageFragment.this.showDeliveryReportForSingleMessage(messageItem.mMsgId, messageItem.mType, messageItem.mUid, messageItem.mIsMultiRecipients);
                        break;
                    }
                    return true;
                case 278925337:
                    if (!(messageItem == null || ComposeMessageFragment.this.mHwCustComposeMessage == null)) {
                        ComposeMessageFragment.this.mHwCustComposeMessage.handleReplyMenu(messageItem);
                        break;
                    }
                case 278925343:
                    if (messageItem != null) {
                        if (!HwMessageUtils.isSplitOn()) {
                            MessageUtils.viewMessageText(ComposeMessageFragment.this.getContext(), messageItem);
                            break;
                        }
                        MessageUtils.viewMessageText(ComposeMessageFragment.this.getContext(), messageItem, ComposeMessageFragment.this);
                        break;
                    }
                    return true;
                case 278925345:
                    if (messageItem != null) {
                        ComposeMessageFragment.this.mMsgListView.replyMessageToAll(messageItem);
                        break;
                    }
                    break;
                case 278925351:
                    if (messageItem != null) {
                        if (1 == MessageUtils.getIccCardStatus(0)) {
                            ComposeMessageFragment.startCopyMessageToSim(ComposeMessageFragment.this.mBackgroundQueryHandler, 1, ComposeMessageFragment.this.getSmsMsgItemValue(messageItem));
                        }
                        if (1 == MessageUtils.getIccCardStatus(1)) {
                            ComposeMessageFragment.startCopyMessageToSim(ComposeMessageFragment.this.mBackgroundQueryHandler, 2, ComposeMessageFragment.this.getSmsMsgItemValue(messageItem));
                            break;
                        }
                    }
                    return true;
                    break;
                case 278927472:
                    if (messageItem != null) {
                        shareMessage(messageItem);
                        break;
                    }
                    break;
                case R.id.menu_add_to_contact:
                    ComposeMessageFragment.this.addRecipietsToContact();
                    break;
                case R.id.menu_call_recipient:
                    ComposeMessageFragment.this.mActionbarAdapter.callRecipients();
                    break;
                case R.id.menu_discard:
                    ComposeMessageFragment.this.mRichEditor.discard();
                    ComposeMessageFragment.this.finish();
                    break;
                case R.id.menu_remove_from_blacklist:
                    if (BlacklistCommonUtils.judgeAddBlackEntryItem(contactList)) {
                        BlacklistCommonUtils.handleNumberBlockList(BlacklistCommonUtils.getBlacklistService(), ((Contact) contactList.get(0)).getNumber(), R.id.menu_remove_from_blacklist);
                        BlacklistCommonUtils.toastAddOrRemoveBlacklistInfo(ComposeMessageFragment.this.getContext(), false);
                        break;
                    }
                    return false;
                case R.id.menu_add_to_blacklist:
                    if (BlacklistCommonUtils.judgeAddBlackEntryItem(contactList)) {
                        BlacklistCommonUtils.comfirmAddContactToBlacklist(ComposeMessageFragment.this.getContext(), ((Contact) contactList.get(0)).getNumber());
                        break;
                    }
                    return false;
            }
            if (ComposeMessageFragment.this.mMsgListView.isInEditMode()) {
                ComposeMessageFragment.this.mMsgListView.exitEditMode();
            }
            return true;
        }

        private void shareMessage(MessageItem msgItem) {
            String mssageText = MessageUtils.getMsgText(ComposeMessageFragment.this.getContext(), msgItem);
            String messageType = "text/plain";
            Uri messageUri = null;
            if (msgItem.isRcsChat()) {
                messageType = "text/plain";
                if (msgItem instanceof RcsFileTransMessageItem) {
                    RcsFileTransMessageItem rcsMsgItem = (RcsFileTransMessageItem) msgItem;
                    switch (rcsMsgItem.mFileTransType) {
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
                    messageUri = ShareUtils.copyFile(ComposeMessageFragment.this.getContext(), rcsMsgItem.getAttachmentFile());
                }
            } else {
                MediaModel mediaModel = null;
                String messageSrc = null;
                switch (msgItem.mAttachmentType) {
                    case 0:
                        messageType = "text/plain";
                        break;
                    case 1:
                        if (ComposeMessageFragment.this.haveSomethingToCopyToSDCard(msgItem.mMsgId)) {
                            mediaModel = msgItem.mSlideshow.get(0).getImage();
                            break;
                        }
                        break;
                    case 2:
                        if (ComposeMessageFragment.this.haveSomethingToCopyToSDCard(msgItem.mMsgId)) {
                            mediaModel = msgItem.mSlideshow.get(0).getVideo();
                            break;
                        }
                        break;
                    case 3:
                        if (ComposeMessageFragment.this.haveSomethingToCopyToSDCard(msgItem.mMsgId)) {
                            mediaModel = msgItem.mSlideshow.get(0).getAudio();
                            break;
                        }
                        break;
                    case 5:
                        if (ComposeMessageFragment.this.haveSomethingToCopyToSDCard(msgItem.mMsgId)) {
                            mediaModel = msgItem.mSlideshow.get(0).getVcard();
                            break;
                        }
                        break;
                    case 6:
                        if (ComposeMessageFragment.this.haveSomethingToCopyToSDCard(msgItem.mMsgId)) {
                            mediaModel = msgItem.mSlideshow.get(0).getVCalendar();
                            break;
                        }
                        break;
                }
                if (mediaModel != null) {
                    messageUri = mediaModel.getUri();
                    if (5 == msgItem.mAttachmentType) {
                        messageSrc = ((VcardModel) mediaModel).getName() + "_" + mediaModel.getSrc();
                    } else {
                        messageSrc = mediaModel.getSrc();
                    }
                }
                messageUri = ShareUtils.copyFile(ComposeMessageFragment.this.getContext(), messageUri, messageSrc);
            }
            ShareUtils.shareMessage(ComposeMessageFragment.this.getContext(), messageUri, messageType, mssageText);
        }
    }

    private class MyConversationActionBarAdapter extends ConversationActionBarAdapter {
        DialogInterface.OnClickListener mClikcer = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which >= 0 && which < MyConversationActionBarAdapter.this.mPhoneNumbers.size()) {
                    MyConversationActionBarAdapter.this.call((String) MyConversationActionBarAdapter.this.mPhoneNumbers.get(which));
                }
            }
        };
        private String mHwMsgSender = null;
        private long mMatchContactId = Long.MIN_VALUE;
        private String mMatchContactName = null;
        private ArrayList<String> mPhoneNumbers;

        public MyConversationActionBarAdapter(HwBaseFragment parentFragment, int uriSize) {
            super(parentFragment, uriSize);
        }

        public ContactList getContactList() {
            return ComposeMessageFragment.this.mConversation.getRecipients();
        }

        public void checkContactHasRelatedContact() {
            ContactList list = getContactList();
            if (list == null || list.size() != 1) {
                Log.log("Mms_", "Mms_View", "checkContactHasRelatedContact match too many or no match");
                return;
            }
            final Contact contact = (Contact) list.get(0);
            new AsyncTask<Void, Void, int[]>() {
                protected int[] doInBackground(Void... params) {
                    int size = MessageUtils.ALL_RELATED_CONTACTS_INFO.size();
                    int[] relatedContactId = new int[size];
                    for (int i = 0; i < size; i++) {
                        relatedContactId[i] = contact.getRelatedContactId(ComposeMessageFragment.this.getContext(), (String) MessageUtils.ALL_RELATED_CONTACTS_INFO.get(MessageUtils.ALL_RELATED_CONTACTS_INFO_BY_INDEX.get(Integer.valueOf(i))));
                    }
                    return relatedContactId;
                }

                protected void onPostExecute(int[] relatedContactId) {
                    if (relatedContactId != null) {
                        int size = relatedContactId.length;
                        int changeCount = 0;
                        for (int i = 0; i < size; i++) {
                            Object newUri;
                            ComponentName cn = (ComponentName) MessageUtils.ALL_RELATED_CONTACTS_INFO_BY_INDEX.get(Integer.valueOf(i));
                            Uri preUri = (Uri) MyConversationActionBarAdapter.this.mRelatedContactInfo.get(cn);
                            if (relatedContactId[i] != -1) {
                                newUri = ContentUris.withAppendedId(Data.CONTENT_URI, (long) relatedContactId[i]);
                                MyConversationActionBarAdapter.this.mRelatedContactInfo.put(cn, newUri);
                            } else {
                                newUri = null;
                                MyConversationActionBarAdapter.this.mRelatedContactInfo.remove(cn);
                            }
                            if ((preUri == null || preUri.equals(newUri)) && (preUri != null || newUri == null)) {
                                MLog.i("Mms_UI_CMA", "checkContactHasRelatedContact not update:" + newUri);
                            } else {
                                changeCount++;
                                MLog.i("Mms_UI_CMA", "checkContactHasRelatedContact update:" + newUri);
                            }
                            MyConversationActionBarAdapter.this.mRelatedContactUri[i] = newUri;
                        }
                    }
                    if (MyConversationActionBarAdapter.this.getContactList().size() <= 1) {
                        ComposeMessageFragment.this.updateNormalMenu();
                    }
                }
            }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
        }

        protected void checkContactChange() {
            if (!TextUtils.isEmpty(this.mHwMsgSender)) {
                new AsyncTask<Void, Void, NameMatchResult>() {
                    protected NameMatchResult doInBackground(Void... params) {
                        return Contact.getNameMatchedContact(ComposeMessageFragment.this.getContext(), MyConversationActionBarAdapter.this.mHwMsgSender, MyConversationActionBarAdapter.this.mMatchContactId);
                    }

                    protected void onPostExecute(NameMatchResult result) {
                        long newId = result == null ? 0 : result.contactId;
                        if (result != null && MyConversationActionBarAdapter.this.mMatchContactId != newId) {
                            MyConversationActionBarAdapter.this.mMatchContactId = newId;
                            MyConversationActionBarAdapter.this.mMatchContactName = result.contactName;
                            MLog.d("Mms_UI_CMA", "ActionBarAdapter check contact changed");
                        }
                    }
                }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
            }
        }

        private void check() {
            if (this.mMatchContactId == Long.MIN_VALUE) {
                this.mMatchContactId = -1;
                Intent intent = ComposeMessageFragment.this.getIntent();
                Object obj = null;
                if (intent != null && intent.hasExtra("sender_for_huawei_message")) {
                    obj = intent.getStringExtra("sender_for_huawei_message");
                    this.mMatchContactId = intent.getLongExtra("contact_id", 0);
                    if (this.mMatchContactId > 0) {
                        this.mMatchContactName = intent.getStringExtra("name");
                    }
                }
                if (!TextUtils.isEmpty(obj)) {
                    this.mHwMsgSender = obj;
                }
            }
        }

        public String getName() {
            check();
            if (TextUtils.isEmpty(this.mHwMsgSender)) {
                return super.getName();
            }
            return this.mHwMsgSender;
        }

        public boolean isHwMsgSender() {
            check();
            return this.mMatchContactId >= 0;
        }

        public long getThreadId() {
            return ComposeMessageFragment.this.mConversation.getThreadId();
        }

        public void addToContact() {
            ComposeMessageFragment.this.mCryptoToastIsShow = true;
            ComposeMessageFragment.this.mAddContactIntent = MessageUtils.getShowOrCreateContactIntent(getNumber());
            if (ComposeMessageFragment.this.mAddContactIntent != null) {
                ComposeMessageFragment.this.startActivityForResult(ComposeMessageFragment.this.mAddContactIntent, 108);
            }
        }

        public boolean isExistsInContact() {
            return this.mMatchContactId <= 0 ? super.isExistsInContact() : true;
        }

        public void setLocationTitle() {
            if (!isHwMsgSender()) {
                ContactList list = getContactList();
                if (list != null && list.size() == 1) {
                    String number = ((Contact) list.get(0)).getNumber();
                    if (!TextUtils.isEmpty(number)) {
                        String parseNumber = MessageUtils.parseMmsAddress(number);
                        if (!TextUtils.isEmpty(parseNumber)) {
                            number = parseNumber;
                        }
                    }
                    final String tempnumbar = number;
                    if (MmsConfig.getPhoneAttributeEnabled() && Mms.isPhoneNumber(number)) {
                        final String LOCATIONCOL = "geolocation";
                        if ("CN".equalsIgnoreCase(MmsApp.getApplication().getCurrentCountryIso())) {
                            new AsyncTask<Void, Void, String>() {
                                protected String doInBackground(Void... params) {
                                    String str = null;
                                    Cursor cursor = null;
                                    try {
                                        cursor = SqliteWrapper.query(MyConversationActionBarAdapter.this.mParentFragment.getContext(), MessageUtils.CONTENT_URI, null, null, new String[]{tempnumbar}, null);
                                        if (cursor != null && cursor.moveToFirst()) {
                                            str = cursor.getString(cursor.getColumnIndex(LOCATIONCOL));
                                        }
                                        if (cursor != null) {
                                            cursor.close();
                                        }
                                    } catch (NullPointerException e) {
                                        MLog.e("Mms_UI_CMA", "is com.huawei.numberlocation there?");
                                        if (cursor != null) {
                                            cursor.close();
                                        }
                                    } catch (Exception e2) {
                                        MLog.e("Mms_UI_CMA", " get number loction exception :" + e2);
                                        if (cursor != null) {
                                            cursor.close();
                                        }
                                    } catch (Throwable th) {
                                        if (cursor != null) {
                                            cursor.close();
                                        }
                                    }
                                    if (str != null) {
                                        return str;
                                    }
                                    return null;
                                }

                                protected void onPostExecute(String result) {
                                    ComposeMessageFragment.this.mActionBarWhenSplit.setSubtitle(result);
                                }
                            }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
                        } else {
                            ComposeMessageFragment.this.mActionBarWhenSplit.setSubtitle(MessageUtils.getGeocodedLocationFor(this.mParentFragment.getContext(), number));
                        }
                    }
                }
            }
        }

        public boolean isGroup() {
            return isHwMsgSender() ? false : super.isGroup();
        }

        public void callRecipients() {
            if (this.mMatchContactId <= 0 || TextUtils.isEmpty(this.mMatchContactName)) {
                super.callRecipients();
            } else {
                new AsyncTask<Void, Void, Boolean>() {
                    protected Boolean doInBackground(Void... params) {
                        return Boolean.valueOf(MyConversationActionBarAdapter.this.checkPhoneNumbers());
                    }

                    protected void onPostExecute(Boolean result) {
                        if (result.booleanValue()) {
                            MyConversationActionBarAdapter.this.showCallDialogForHwMessage();
                        } else {
                            MyConversationActionBarAdapter.this.call(MyConversationActionBarAdapter.this.getNumber());
                        }
                    }
                }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
            }
        }

        public void editBeforeCall() {
            StatisticalHelper.incrementReportCount(ComposeMessageFragment.this.getContext(), 2207);
            HwMessageUtils.toEditBeforeCall(getNumber(), ComposeMessageFragment.this.getContext());
        }

        public void writeWeichat() {
            CharSequence charSequence = null;
            if (this.mRelatedContactUri[0] != null) {
                Context context = ComposeMessageFragment.this.getContext();
                Uri uri = this.mRelatedContactUri[0];
                if (ComposeMessageFragment.this.mRichEditor != null) {
                    charSequence = ComposeMessageFragment.this.mRichEditor.getSmsText();
                }
                MessageUtils.gotoWeichatWithText(context, uri, charSequence);
                Editor editor = PreferenceManager.getDefaultSharedPreferences(ComposeMessageFragment.this.getActivityContext()).edit();
                editor.putInt("lastUsedMenu", 0);
                editor.commit();
            }
        }

        public void writeWhatsapp() {
            CharSequence charSequence = null;
            if (this.mRelatedContactUri[1] != null) {
                Context context = ComposeMessageFragment.this.getContext();
                Uri uri = this.mRelatedContactUri[1];
                if (ComposeMessageFragment.this.mRichEditor != null) {
                    charSequence = ComposeMessageFragment.this.mRichEditor.getSmsText();
                }
                MessageUtils.gotoWhatsappWithText(context, uri, charSequence);
                Editor editor = PreferenceManager.getDefaultSharedPreferences(ComposeMessageFragment.this.getActivityContext()).edit();
                editor.putInt("lastUsedMenu", 1);
                editor.commit();
            }
        }

        public void viewPeopleInfo() {
            if (this.mMatchContactId > 0) {
                Intent intent = new Intent("android.intent.action.VIEW", ContentUris.withAppendedId(Contacts.CONTENT_URI, this.mMatchContactId));
                intent.setFlags(524288);
                ComposeMessageFragment.this.startActivity(intent);
                return;
            }
            super.viewPeopleInfo();
        }

        private boolean checkPhoneNumbers() {
            this.mPhoneNumbers = Contact.getPhoneNnumbers(ComposeMessageFragment.this.getContext(), this.mMatchContactId);
            if (this.mPhoneNumbers == null) {
                this.mPhoneNumbers = new ArrayList();
            }
            this.mPhoneNumbers.add(0, getNumber());
            if (this.mPhoneNumbers.size() > 1) {
                return true;
            }
            return false;
        }

        private String[] getCallStrings() {
            String[] ret = new String[this.mPhoneNumbers.size()];
            for (int i = 0; i < ret.length; i++) {
                if (i == 0) {
                    ret[i] = (String) this.mPhoneNumbers.get(i);
                } else {
                    ret[i] = Contact.formatNameAndNumber(this.mMatchContactName, (String) this.mPhoneNumbers.get(i));
                }
            }
            return ret;
        }

        private void showCallDialogForHwMessage() {
            AlertDialog.Builder builder = new AlertDialog.Builder(ComposeMessageFragment.this.getContext());
            if (!builder.create().isShowing()) {
                builder.setItems(getCallStrings(), this.mClikcer);
                AlertDialog urlDialog = builder.create();
                urlDialog.setTitle(R.string.menu_call);
                urlDialog.show();
            }
        }

        public long getmMatchContactId() {
            return this.mMatchContactId;
        }
    }

    private static class NotifyRunner implements Runnable {
        long mtid;

        NotifyRunner(long tid) {
            this.mtid = tid;
        }

        public void run() {
            MessagingNotification.updateSendFailedNotificationForThread(MmsApp.getApplication(), this.mtid);
        }

        public String toString() {
            return "ComposeMessageActivity.updateSendFailedNotification." + super.toString();
        }
    }

    private static class ScroolOnItemLayoutCallback implements ItemLayoutCallback<MessageItem> {
        private ComposeMessageFragment mCompose;
        private Runnable mSmoothScrollRunnable = new Runnable() {
            public void run() {
                MLog.d("Mms_UI_CMA", "ScroolOnLastItemLoadedCallback smoothScrollToEnd");
                ScroolOnItemLayoutCallback.this.mCompose.smoothScrollToEnd(true, 0);
            }
        };

        public ScroolOnItemLayoutCallback(ComposeMessageFragment compose) {
            this.mCompose = compose;
        }

        public void onItemLayout(MessageItem item, Throwable exception) {
            if (this.mCompose.mMsgListView.isAutoScrool()) {
                Handler handler = HwBackgroundLoader.getUIHandler();
                handler.removeCallbacks(this.mSmoothScrollRunnable);
                handler.post(this.mSmoothScrollRunnable);
            }
        }
    }

    private class SendIgnoreInvalidRecipientListener implements DialogInterface.OnClickListener {
        private int mSubscription;

        public SendIgnoreInvalidRecipientListener(int subscription) {
            this.mSubscription = subscription;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            ComposeMessageFragment.this.sendMessage(true, this.mSubscription);
            MLog.d("Mms_UI_CMA", "Mms_TX SendIgnoreInvalidRecipient");
            dialog.dismiss();
        }
    }

    public void setmCryptoToastIsShow(boolean cryptoToastIsShow) {
        this.mCryptoToastIsShow = cryptoToastIsShow;
    }

    public static void log(String logMsg) {
        Thread current = Thread.currentThread();
        long tid = current.getId();
        MLog.d("Mms_UI_CMA", "[" + tid + "] [" + current.getStackTrace()[3].getMethodName() + "] " + logMsg);
    }

    public void setSupportScale() {
        ((HwBaseActivity) getActivity()).setSupportScale(new SacleListener() {
            public void onScaleChanged(float scaleSize) {
                if (ComposeMessageFragment.this.mMsgListAdapter != null) {
                    ComposeMessageFragment.this.mMsgListAdapter.onScaleChanged(scaleSize);
                }
            }
        });
    }

    public void removeSupportScale() {
        ((HwBaseActivity) getActivity()).removeSupportScale();
    }

    private boolean showMessageDetails(MessageItem msgItem) {
        Cursor cursor = this.mMsgListAdapter.getCursorForItem(msgItem);
        if (cursor == null || isDetached()) {
            return false;
        }
        String messageDetails = MessageUtils.getMessageDetails(getContext(), cursor, msgItem.mMessageSize, msgItem.mUid, msgItem.mIsMultiRecipients);
        if (this.mDetalDialog == null) {
            this.mDetalDialog = new AlertDialog.Builder(getContext()).setTitle(R.string.message_details_title).setCancelable(true).create();
        }
        if (!this.mDetalDialog.isShowing()) {
            this.mDetalDialog.setMessage(messageDetails);
            this.mDetalDialog.show();
        }
        return true;
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0) {
            this.mWaitingForSubActivity = true;
        }
        if (this.mIsKeyboardOpen && !"android.provider.action.QUICK_CONTACT".equals(intent.getAction())) {
            hideKeyboard();
        }
        try {
            super.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            MessageUtils.shwNoAppDialog(getContext());
            MLog.e("Mms_UI_CMA", "No Activity found to handle Intent");
        }
    }

    private void confirmSendMessageIfNeeded() {
        confirmSendMessageIfNeeded(this.mComposeBottomView.getSingleCardSubId());
    }

    public void reSend(MessageItem msgItem) {
        if ("sms".equals(msgItem.mType)) {
            long threadId = this.mConversation.getThreadId();
            if (this.mRcsComposeMessage != null) {
                threadId = this.mRcsComposeMessage.getRcsThreadId(threadId, msgItem);
            }
            try {
                new SmsMessageSender(getContext(), new String[]{msgItem.mAddress}, msgItem.mBody, threadId, msgItem.mSubId).sendMessage(threadId);
                MLog.d("Mms_UI_CMA", "Mms_TX CMA resend sms " + msgItem.mMessageUri.toString());
            } catch (MmsException e) {
                MLog.e("Mms_UI_CMA", "Mms_TX CMA Failed to send SMS message, threadId=" + threadId, (Throwable) e);
            }
            SqliteWrapper.delete(getContext(), this.mContentResolver, ContentUris.withAppendedId(Sms.CONTENT_URI, msgItem.mMsgId), null, null);
        } else if (!msgItem.isRcsChat()) {
            try {
                MLog.d("Mms_UI_CMA", "Mms_TX CMA resend mms, uri=" + msgItem.mMessageUri.toString());
                ContentValues contentValues = new ContentValues(3);
                contentValues.put("err_type", Integer.valueOf(0));
                contentValues.put("err_code", Integer.valueOf(0));
                contentValues.put("retry_index", Integer.valueOf(0));
                SqliteWrapper.update(getContext(), PendingMessages.CONTENT_URI, contentValues, "msg_id=" + msgItem.mMsgId, null);
                new MmsMessageSender(getContext(), msgItem.mMessageUri, (long) msgItem.mMessageSize, msgItem.mSubId).sendMessage(msgItem.mThreadId);
            } catch (Throwable e2) {
                MLog.e("Mms_UI_CMA", "Mms_TX CMA Failed to resend mms failed: " + msgItem.mMessageUri + ", threadId=" + msgItem.mThreadId, e2);
            }
        } else if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.reSend(msgItem);
        }
    }

    private void editMessageItem(MessageItem msgItem) {
        if ("sms".equals(msgItem.mType)) {
            editSmsMessageItem(msgItem);
        } else if (msgItem.isRcsChat()) {
            this.mComposeHolder.editRcsMessageItem(msgItem);
        } else {
            editMmsMessageItem(msgItem);
        }
        if (msgItem.isFailedMessage() && this.mMsgListAdapter.getCount() <= 1) {
            if (this.mRcsComposeMessage == null || !this.mRcsComposeMessage.isFileItem(msgItem)) {
                this.mComposeRecipientsView.clear();
                this.mComposeRecipientsView.initRecipientsEditor();
                if ("mms".equals(msgItem.mType)) {
                    this.mMsgListAdapter.changeCursor(null);
                }
            } else {
                MLog.d("Mms_UI_CMA", "RCS file transfer message, do not need to initialize the recipientsView");
            }
        }
    }

    private void editSmsMessageItem(MessageItem msgItem) {
        Uri uri;
        synchronized (this.mConversation) {
            if (this.mConversation.getMessageCount() <= 1) {
                this.mConversation.clearThreadId();
                if (MessagingNotification.getRcsMessagingNotification() == null || !MessagingNotification.getRcsMessagingNotification().isRcsSwitchOn()) {
                    MessagingNotification.setCurrentlyDisplayedThreadId(-2);
                } else {
                    MessagingNotification.getRcsMessagingNotification().setCurrentlyDisplayedThreadId(-2, 0);
                }
            }
        }
        String where = null;
        if (this.mConversation.getRecipients().size() > 1) {
            uri = MessageUtils.CONTENT_URI_WITH_UID;
            where = "group_id='" + msgItem.mUid + "'";
        } else {
            uri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgItem.mMsgId);
        }
        this.mBackgroundQueryHandler.startDelete(9786, Integer.valueOf(0), uri, where, null);
        if (CryptoMessageServiceProxy.isLocalEncrypted(msgItem.mBody)) {
            this.mRichEditor.setText(CryptoMessageServiceProxy.localDecrypt(msgItem.mBody));
        } else {
            this.mRichEditor.setText(msgItem.mBody);
        }
    }

    private void editMmsMessageItem(MessageItem msgItem) {
        this.mRichEditor.loadWorkingMessage(getContext(), this, msgItem.mMessageUri);
        this.mRichEditor.setConversation(this.mConversation);
        this.mRichEditor.setSubject(msgItem.mSubject, false);
        this.mCryptoCompose.updateSwitchStateLoadDraft(this.mRichEditor.requiresMms());
        this.mRichEditor.syncWorkingMessageToUI();
    }

    private boolean haveSomethingToCopyToSDCard(long msgId) {
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(getContext(), ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        } catch (MmsException e) {
            MLog.e("Mms_UI_CMA", "haveSomethingToCopyToSDCard can't load pdu body: " + msgId);
        }
        if (body == null) {
            return false;
        }
        boolean result = false;
        int partNum = body.getPartsNum();
        for (int i = 0; i < partNum; i++) {
            String type = new String(body.getPart(i).getContentType(), Charset.defaultCharset());
            if (MLog.isLoggable("Mms_app", 2)) {
                log("[CMA] haveSomethingToCopyToSDCard: part[" + i + "] contentType=" + type);
            }
            if (ContentType.isImageType(type) || ContentType.isVideoType(type) || "application/ogg".equals(type) || ContentType.isAudioType(type) || "text/x-vCard".equalsIgnoreCase(type) || DrmUtils.isDrmType(type) || "text/x-vCalendar".equals(type)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean saveRingtone(long msgId) {
        boolean result = true;
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(getContext(), ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        } catch (MmsException e) {
            MLog.e("Mms_UI_CMA", "copyToDrmProvider can't load pdu body: " + msgId);
        }
        if (body == null) {
            return false;
        }
        int partNum = body.getPartsNum();
        for (int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            if (DrmUtils.isDrmType(new String(part.getContentType(), Charset.defaultCharset()))) {
                result &= MmsPduUtils.copyPart(getContext(), part, Long.toHexString(msgId)).getResult();
            }
        }
        return result;
    }

    private boolean isDrmRingtoneWithRights(long msgId) {
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(getContext(), ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        } catch (MmsException e) {
            MLog.e("Mms_UI_CMA", "isDrmRingtoneWithRights can't load pdu body: " + msgId);
        }
        if (body == null) {
            return false;
        }
        int partNum = body.getPartsNum();
        for (int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            if (DrmUtils.isDrmType(new String(part.getContentType(), Charset.defaultCharset())) && ContentType.isAudioType(MmsApp.getApplication().getDrmManagerClient().getOriginalMimeType(part.getDataUri())) && DrmUtils.haveRightsForAction(part.getDataUri(), 2)) {
                return true;
            }
        }
        return false;
    }

    private int getDrmMimeSavedStringRsrc(long msgId, boolean success) {
        if (!isDrmRingtoneWithRights(msgId)) {
            return 0;
        }
        return success ? R.string.saved_ringtone_Toast : R.string.saved_ringtone_Toast_fail;
    }

    public synchronized ContactList getRecipients() {
        if (this.mComposeRecipientsView.isVisible()) {
            if (this.sEmptyContactList == null) {
                this.sEmptyContactList = new ContactList();
            }
            return this.sEmptyContactList;
        } else if (this.mConversation == null) {
            return null;
        } else {
            return this.mConversation.getRecipients();
        }
    }

    private void updateTitle(ContactList list) {
        if ((this.mRcsComposeMessage == null || !this.mRcsComposeMessage.cantUpdateTitle(this.mActionbarAdapter)) && !this.mMsgListView.isInEditMode() && list != null) {
            for (Contact c : list) {
                c.checkAndUpdateContact();
            }
            if (this.mActionBarWhenSplit.getActionMode() != 4 && this.mActionBarWhenSplit.getActionMode() != 5) {
                if (this.mComposeRecipientsView.isVisible()) {
                    if (this.mHwCustComposeMessage == null || !this.mHwCustComposeMessage.getIsTitleChangeWhenRecepientsChange()) {
                        showNewMessageTitle();
                    } else {
                        this.mHwCustComposeMessage.showNewMessageTitleWithMaxRecipient(list, this.mActionBarWhenSplit);
                    }
                    return;
                }
                updateInfoFromAdapter();
                updateComposeStartIcon();
                if (this.mActionbarAdapter.getContactList().size() <= 1) {
                    updateNormalMenu();
                }
                if (!HwMessageUtils.isSuperPowerSaveModeOn()) {
                    this.mActionbarAdapter.checkContactHasRelatedContact();
                }
                this.mDebugRecipients = list.serialize();
                if (!(this.mHwCustComposeMessage == null || this.mConversation == null)) {
                    this.mHwCustComposeMessage.initilizeUI(this, this.mMsgListView, this.mMsgListAdapter, this.mConversation.getThreadId(), this.mActionbarAdapter, this.mConversationInputManager, this.mNormalMenu, this.mActionBarWhenSplit);
                    this.mHwCustComposeMessage.updateNormalMenu();
                }
            }
        }
    }

    protected void showNewMessageTitle() {
        this.mActionBarWhenSplit.show(false);
    }

    public static boolean cancelFailedToDeliverNotification(Intent intent, Context context) {
        if (!MessagingNotification.isFailedToDeliver(intent)) {
            return false;
        }
        MessagingNotification.cancelNotification(context, 789);
        return true;
    }

    public static boolean cancelFailedDownloadNotification(Intent intent, Context context) {
        if (!MessagingNotification.isFailedToDownload(intent)) {
            return false;
        }
        MessagingNotification.cancelNotification(context, 531);
        return true;
    }

    private int getContentViewResId() {
        return R.layout.compose_message_activity_multisim;
    }

    public void onCreate(Bundle savedInstanceState) {
        MLog.d("Mms_UI_CMA", "ComposeMessageFragment onCreate start");
        this.mComposeHolder = new HwComposeCustHolder();
        this.mCryptoCompose.setmComposeHolder(this.mComposeHolder);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.mIsSmsEnabled = MmsConfig.isSmsEnabled(getContext());
        this.mMediaUpdateListeners = new ArrayList();
        if (((KeyguardManager) getContext().getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
            getActivity().getWindow().addFlags(4194304);
        }
        HwBackgroundLoader.getInst().sendTask(3);
        registerPriavcyMonitor();
        registerDefSmsAppChanged();
        registerContactDataChangeObserver();
        resetConfiguration(getResources().getConfiguration());
        initLocalBroadcast();
        this.mMmsRadarInfoManager = MmsRadarInfoManager.getInstance();
        this.mSendSmsHandler = this.mMmsRadarInfoManager.getHandler();
        this.mDirtyModel = new DirtyModel();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MLog.d("Mms_UI_CMA", "RightPaneComposeMessageFragment onCreateView start");
        this.root = inflater.inflate(getContentViewResId(), container, false);
        this.mActionBarWhenSplit = createEmuiActionBar(this.root);
        this.mSplitActionBar = (SplitActionBarView) this.root.findViewById(R.id.compose_message_bottom);
        return this.root;
    }

    protected AbstractEmuiActionBar createEmuiActionBar(View fragmentRootView) {
        return new MmsEmuiActionBar(getActivity(), fragmentRootView.findViewById(R.id.compose_message_top), null);
    }

    public ConversationInputManager getConversationInputManager() {
        return this.mConversationInputManager;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        int i;
        MLog.d("Mms_UI_CMA", "ComposeMessageFragment onCreateView start");
        super.onActivityCreated(savedInstanceState);
        this.mConversationInputManager = new ConversationInputManager(getActivity(), getActivity().getFragmentManager(), this.mConversationInputHostHolder);
        this.mHwCustComposeMessage = (HwCustComposeMessage) HwCustUtils.createObj(HwCustComposeMessage.class, new Object[]{this});
        this.mHwCustPduPersister = (HwCustPduPersister) HwCustUtils.createObj(HwCustPduPersister.class, new Object[0]);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsComposeMessage == null) {
            this.mRcsComposeMessage = new RcsComposeMessage(this);
        }
        if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.setHwCustCallback(this.mComposeHolder);
        }
        if (this.mHwCustComposeMessage != null) {
            this.mHwCustComposeMessage.onCreate(savedInstanceState);
        }
        if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.onCreate(savedInstanceState);
        }
        this.mActionbarAdapter = new MyConversationActionBarAdapter(this, MessageUtils.ALL_RELATED_CONTACTS_INFO_BY_INDEX.size());
        this.mMenuEx = new MenuEx();
        this.mNormalMenu = new EmuiMenu(null);
        this.mMenuEx.setContext(getContext());
        if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.setPeopleActionBar(this.mActionbarAdapter);
        }
        getActivity().setProgressBarVisibility(false);
        getActivity().getWindow().setSoftInputMode(18);
        initResourceRefs();
        this.mComposeRecipientsView = new ComposeRecipientsView(this.mComposeHolder);
        this.mComposeChoosePanel = new ComposeChoosePanel(this.mComposeHolder);
        this.mAttachmentPreview = (AttachmentPreview) findViewById(R.id.attachment_draft_view);
        this.mAttachmentPreview.setRichMessageEditor(this.mRichEditor, this.mConversationInputHostHolder);
        this.mAttachmentPreview.setLanuchMeasureChild(true);
        this.mComposeBottomView.setScrollableCallback(new ScrollableCallback() {
            public boolean isScrollable() {
                if (ComposeMessageFragment.this.mIsLandscape) {
                    return !ComposeMessageFragment.this.mIsKeyboardOpen ? ComposeMessageFragment.this.mComposeChoosePanel.isShowSmileyFace() : true;
                } else {
                    return false;
                }
            }
        });
        this.mContentResolver = getContext().getContentResolver();
        this.mBackgroundQueryHandler = new BackgroundQueryHandler(this.mContentResolver);
        initialize(savedInstanceState, 0);
        if (this.mConversation == null) {
            i = 0;
        } else {
            i = this.mConversation.getMessageCount();
        }
        this.mOldMessageCount = i;
        ((HwBaseActivity) getActivity()).setSupportScale(new SacleListener() {
            public void onScaleChanged(float scaleSize) {
                if (ComposeMessageFragment.this.mMsgListAdapter != null) {
                    ComposeMessageFragment.this.mMsgListAdapter.onScaleChanged(scaleSize);
                }
            }
        });
        if (this.mRcsComposeMessage == null || !this.mRcsComposeMessage.checkNeedAppendSignature()) {
            appendSignature(false);
        } else {
            appendSignature(true);
        }
        this.mCryptoCompose.onCreate(this.mComposeHolder, this.mRichEditor);
    }

    public void initialize(Bundle savedInstanceState, long originalThreadId) {
        this.mRichEditor.createWorkingMessage(getContext(), this);
        Intent intent = getIntent();
        this.isSimReply = intent.getBooleanExtra("simReply", false);
        this.mIsFromLauncher = intent.getBooleanExtra("is_from_launcher", false);
        this.mIsForWardMms = intent.getBooleanExtra("is_forward_mms", false);
        this.mRichEditor.setIsForwardMms(this.mIsForWardMms);
        String action = getIntent().getAction();
        if ("com.android.mms.ui.action.reply".equals(action) || "android.intent.action.EDIT".equals(action)) {
            Uri uriEp = (Uri) intent.getExtra("ex_uri");
            long threadId = (this.isSimReply || uriEp == null) ? 0 : getThreadId(uriEp);
            intent.setData(ContentUris.withAppendedId(Threads.CONTENT_URI, threadId));
            intent.putExtra("thread_id", threadId);
            if ("android.intent.action.EDIT".equals(action) && intent.hasExtra("exit_on_sent")) {
                intent.putExtra("exit_on_sent", false);
            }
        }
        this.mComposeRecipientsView.setIsFromLauncher(this.mIsFromLauncher);
        initActivityState(savedInstanceState);
        this.mHasUnreadMessage = this.mConversation.hasUnreadMessages();
        if (originalThreadId != 0 && originalThreadId == this.mConversation.getThreadId()) {
            LogTag.warnPossibleRecipientMismatch("ComposeMessageActivity.initialize:  threadId didn't change from: " + originalThreadId, getActivity());
        }
        if (originalThreadId != 0 && originalThreadId == this.mConversation.getThreadId() && getActivityStartType() == 1) {
            this.mConversation.setHasTempDraft(true);
        }
        log("initialize:: savedInstanceState = ***; intent = ***  mConversation tid = " + this.mConversation.getThreadId());
        if (cancelFailedToDeliverNotification(getIntent(), getContext())) {
            undeliveredMessageDialog(getMessageDate(null));
        }
        cancelFailedDownloadNotification(getIntent(), getContext());
        initMessageList();
        this.mShouldLoadDraft = true;
        boolean intentHandled = savedInstanceState == null ? (handleSendIntent() || handleRepliedMessage()) ? true : handleForwardedMessage() : false;
        if (intentHandled || this.isPeeking) {
            this.mShouldLoadDraft = false;
            refreshAttachmentChangedUI(13);
        }
        this.mRichEditor.setConversation(this.mConversation);
        if (this.mRcsAudioMessage == null) {
            this.mRcsAudioMessage = new RcsAudioMessage(this);
        }
        this.mRcsAudioMessage.setConversation(this.mConversation);
        getActivity().invalidateOptionsMenu();
        loadMessagesAndDraft(this.mShouldLoadDraft);
        this.mIsDataLoadedOnCreate = true;
        if (MmsConfig.getCreationModeEnabled() && !MmsConfig.getCurrentCreationMode().equals("freemodemode")) {
            MLog.d("Mms_UI_CMA", "Initilize: RistrictMode");
            checkRestrictedMime(this.mForwardMessageMode);
        }
        boolean isFromNotification = MmsCommon.isFromNotification(intent);
        if (isFromNotification) {
            StatisticalHelper.incrementReportCount(getContext(), 2032);
        }
        if (!isFromNotification && MmsCommon.isFromFloatMms(intent)) {
            isFromNotification = 1 != getActivityStartType();
            StatisticalHelper.incrementReportCount(getContext(), 2034);
        }
        boolean isEmptyThread = this.mConversation.getThreadId() <= 0 || (!isFromNotification && this.mConversation.getMessageCount() == 0);
        if (this.isPeeking) {
            this.mComposeRecipientsView.hideRecipientEditor();
        } else if ((!isEmptyThread || this.isSimReply) && !this.mIsEditOnly) {
            MLog.d("Mms_UI_CMA", "Initilize: SimReply " + this.isSimReply + "tid=" + this.mConversation.getThreadId() + " " + (this.mConversation.getMessageCount() == 0 ? " empty " : "hasContent"));
            this.mComposeRecipientsView.hideRecipientEditor();
        } else {
            MLog.d("Mms_UI_CMA", "Initilize: SimReply " + this.isSimReply + "; EditOnly" + this.mIsEditOnly + "; tid=" + this.mConversation.getThreadId() + " " + (this.mConversation.getMessageCount() == 0 ? " empty " : "hasContent"));
            this.mComposeRecipientsView.hideRecipientEditor();
            this.mComposeRecipientsView.initRecipientsEditor();
            this.mComposeRecipientsView.setNumberFromIntent(getIntent());
        }
        if (savedInstanceState != null) {
            MLog.d("Mms_UI_CMA", "Sync working message to UI");
            this.mRichEditor.syncWorkingMessageToUI();
        }
        onKeyboardStateChanged();
        if (MLog.isLoggable("Mms_app", 2)) {
            log("initialize:: update title, mConversation tid=" + this.mConversation.getThreadId());
        }
        updateTitle(this.mConversation.getRecipients());
        if (!HwMessageUtils.isSplitOn()) {
            setPeekActions();
        }
        this.mMsgListAdapter.setIsGroupConversation(this.mConversation.getRecipients().size() > 1);
        updateRecipientsStartIcon();
    }

    public void onNewIntent(Intent intent) {
        this.mIsFromQuickAction = false;
        if (ComposeMessageActivity.isFromQuickAction(intent)) {
            this.mIsFromQuickAction = true;
        }
        if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.onNewIntent();
        }
        if (MmsConfig.isSupportDraftWithoutRecipient()) {
            this.mToastForDraftSave = true;
        }
        if (!(this.mRichEditor == null || !this.mRichEditor.isWorthSaving() || this.mComposeRecipientsView == null || !this.mComposeRecipientsView.isVisible() || this.mComposeRecipientsView.hasValidRecipient(this.mRichEditor.requiresMms()))) {
            if (this.mCryptoCompose.isSmsEncryptionSwitchOn()) {
                MLog.d("Mms_UI_CMA", "onNewIntent crypto message does not save draft.");
                return;
            }
            if (this.mDiscardDraftAlertDialog != null && this.mDiscardDraftAlertDialog.isShowing()) {
                this.mDiscardDraftAlertDialog.dismiss();
            }
            if (MmsConfig.isSupportDraftWithoutRecipient()) {
                this.mToastForDraftSave = false;
            } else {
                this.mDiscardDraftAlertDialog = MessageUtils.showDiscardDraftConfirmDialog(getContext(), new DiscardDraftListener(intent));
                return;
            }
        }
        if (!MmsConfig.isSupportDraftWithoutRecipient()) {
            this.mToastForDraftSave = true;
        }
        finish();
        if (!(getController() == null || intent == null)) {
            getController().startComposeMessage(intent);
        }
    }

    private void sanityCheckConversation() {
        if (this.mRichEditor.getConversation() != this.mConversation) {
            LogTag.warnPossibleRecipientMismatch("ComposeMessageActivity: mricheditor.mConversation=" + this.mRichEditor.getConversation() + ", mConversation=" + this.mConversation + ", MISMATCH!", getActivity());
        }
    }

    public void updateInputMode() {
        if (this.mComposeChoosePanel.isVisible() || this.mForwardMessageMode || this.mConversationInputManager.isMediaPickerVisible()) {
            getActivity().getWindow().setSoftInputMode(2);
            return;
        }
        int mode;
        if (this.mComposeRecipientsView.isVisible()) {
            if (this.mComposeRecipientsView.isFocused()) {
                mode = 18;
            } else if (this.mConversation.getRecipients() == null) {
                mode = 18;
            } else {
                EditTextWithSmiley editText = this.mRichEditor.getMSmsEditorText();
                if (editText != null) {
                    editText.setCursorVisible(true);
                }
                mode = 20;
            }
        } else if (getIntent() == null || !MmsCommon.isFromPeekReply(getIntent())) {
            mode = 18;
        } else {
            mode = 20;
        }
        getActivity().getWindow().setSoftInputMode(mode);
    }

    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
        if (inputMethodManager == null) {
            MLog.e("Mms_UI_CMA", "hideKeyboard can't get inputMethodManager.");
            return;
        }
        Activity act = getActivity();
        if (act == null) {
            MLog.e("Mms_UI_CMA", "hideKeyboard getActivity return null.");
            return;
        }
        View v = act.getCurrentFocus();
        if (v == null || v.getWindowToken() == null) {
            MLog.e("Mms_UI_CMA", "Can't get hide KeyBoard as no focus view or no token." + v);
        } else {
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 2);
        }
    }

    private void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
        if (inputMethodManager == null) {
            MLog.e("Mms_UI_CMA", "showKeyboard can't get inputMethodManager.");
            return;
        }
        Activity act = getActivity();
        if (act == null) {
            MLog.e("Mms_UI_CMA", "showKeyboard getActivity return null.");
            return;
        }
        View v = act.getCurrentFocus();
        if (v == null) {
            MLog.e("Mms_UI_CMA", "Can't show KeyBoard as no focus view.");
        } else {
            inputMethodManager.showSoftInput(v, 1);
        }
    }

    private void updateFocus() {
        if (!(this.mRichEditor == null || this.mRichEditor.isEmptyThread(this.mConversation))) {
            this.mRichEditor.setEditTextFocus();
        }
        if (!this.mComposeRecipientsView.isVisible() || this.mComposeRecipientsView.getRecipientCount() >= 1 || this.mConversationInputManager.isMediaPickerVisible()) {
            if (this.mRichEditor != null) {
                this.mRichEditor.setEditTextFocus();
            }
        } else if (!(this.mReCode == 130128 || this.mReCode == 110 || this.mReCode == LocationRequest.PRIORITY_LOW_POWER || this.mReCode == 144)) {
            this.mComposeRecipientsView.requestFocus();
        }
        if (this.mComposeRecipientsView.isInEdit()) {
            this.mComposeRecipientsView.setScrollerHeightAndHint(MmsConfig.getChangeScrollerHeightDelayLong() * 5);
        }
    }

    public void onStart() {
        super.onStart();
        boolean isSmsEnabled = MmsConfig.isSmsEnabled(getContext());
        if (isSmsEnabled != this.mIsSmsEnabled) {
            this.mIsSmsEnabled = isSmsEnabled;
            getActivity().invalidateOptionsMenu();
        }
        if (this.mMsgListAdapter == null) {
            finish();
            return;
        }
        int i;
        this.mMsgListAdapter.setOnDataSetChangedListener(this.mDataSetChangedListener);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(this.mHttpProgressReceiver, this.mHttpProgressFilter);
        if (this.mIsSmsEnabled) {
            updateFocus();
            updateInputMode();
        }
        this.mMessagesAndDraftLoaded = false;
        blockMarkAsRead(true);
        if (this.mIsDataLoadedOnCreate) {
            this.mIsDataLoadedOnCreate = false;
        } else {
            final boolean loadDraft = this.mShouldLoadDraft;
            this.mHandler.post(new Runnable() {
                public void run() {
                    ComposeMessageFragment.this.loadMessagesAndDraft(loadDraft);
                    if (ComposeMessageFragment.this.mRichEditor.getRcsRichMessageEditor() != null) {
                        ComposeMessageFragment.this.mRichEditor.getRcsRichMessageEditor().setRcsLoadDraftFt(false);
                    }
                }
            });
        }
        MessageListView messageListView = this.mMsgListView;
        if (this.mIsEditOnly) {
            i = 8;
        } else {
            i = 0;
        }
        messageListView.setVisibility(i);
        this.mRichEditor.syncWorkingRecipients();
        log("onStart::update title, mConversation tid=" + this.mConversation.getThreadId());
        if (!this.mIsSmsEnabled && this.mMsgListView.isInEditMode()) {
            MLog.v("Mms_UI_CMA", "onStart:: it is not default sms app, exit multi choice mode");
            this.mMsgListView.exitEditMode();
        }
        if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.onStart();
        }
        this.mComposeRecipientsView.onActivityStart();
        registerVowifiReceiver();
        if (this.isPeeking) {
            this.mBottomView.setVisibility(8);
            this.mMsgListView.setVerticalScrollBarEnabled(false);
        }
    }

    private void initLocalBroadcast() {
        if (this.mLocalBroadcastManager == null) {
            this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        }
        if (this.mSaveDraftReceiver == null) {
            this.mSaveDraftReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent != null && "com.huawei.mms.saveDraft".equals(intent.getAction())) {
                        if (ComposeMessageFragment.this.mRcsComposeMessage == null || !ComposeMessageFragment.this.mRcsComposeMessage.isGroupChat(intent)) {
                            Bundle bundle = intent.getExtras();
                            if (bundle != null) {
                                String smsData = bundle.getString("full_screen_data");
                                if (!(smsData == null || ComposeMessageFragment.this.mRichEditor == null)) {
                                    ComposeMessageFragment.this.setRichEditorText(bundle, smsData);
                                    if (!intent.getBooleanExtra("full_screen_send_broadcast_enable", true)) {
                                        ComposeMessageFragment.this.mToastToMms = intent.getBooleanExtra("full_screen_send_broadcast_enable", true);
                                    }
                                    ComposeMessageFragment.this.saveDraft(true);
                                }
                            } else {
                                return;
                            }
                        }
                        MLog.d("Mms_UI_CMA", "is RCS group chat message,don't save draft");
                    }
                }
            };
            this.mLocalBroadcastManager.registerReceiver(this.mSaveDraftReceiver, new IntentFilter("com.huawei.mms.saveDraft"));
        }
    }

    private void setRichEditorText(Bundle bundle, String smsData) {
        if (!HwMessageUtils.isSplitOn() || bundle.getLong("is_from_message_full_fragment_conversation ") == getConversation().getThreadId()) {
            this.mRichEditor.setText(smsData);
        }
    }

    private void appendSignature(boolean isCheckAppendSignature) {
        boolean needAppend = false;
        if (MmsConfig.isSupportDraftWithoutRecipient()) {
            long threadId = this.mRichEditor.getConversation().getThreadId();
            if ((this.mRichEditor.getConversation().hasDraft() && 0 != threadId) || !this.mIsSmsEnabled || this.mForwardMessageMode) {
                if (!this.mShouldLoadDraft && 0 == threadId) {
                    needAppend = true;
                }
                if (!needAppend) {
                    return;
                }
            }
        } else if (this.mRichEditor.getConversation().hasDraft() || !this.mIsSmsEnabled || this.mForwardMessageMode) {
            return;
        }
        this.mRichEditor.appendSignature(isCheckAppendSignature);
    }

    public void clearMsgAndAppendSignature() {
        if (this.mRichEditor != null) {
            this.mRichEditor.setText(null);
            appendSignature(false);
        }
    }

    public void loadMessageContent() {
        if (!(this.mComposeRecipientsView.isVisible() || this.mConversation.getThreadId() == 0)) {
            this.mConversation.blockMarkAsRead(true);
            this.mConversation.markAsRead();
        }
        startMsgListQuery();
        updateSendFailedNotification();
    }

    int getActivityStartType() {
        Intent intent = getIntent();
        if (intent == null) {
            return 0;
        }
        String styp = intent.getStringExtra("START_TYPE");
        if (TextUtils.isEmpty(styp) || !"FLOAT_EDIT_NEW".equals(styp)) {
            return 0;
        }
        return 1;
    }

    private void loadMessagesAndDraft(boolean shouldLoadDraft) {
        if (!this.mMessagesAndDraftLoaded) {
            if (getActivityStartType() == 0) {
                MLog.v("Mms_UI_CMA", "### CMA.loadMessagesAndDraft: START_TYPE_NONE. shouldLoadDraft=" + shouldLoadDraft);
                loadMessageContent();
            } else if (getActivityStartType() == 1) {
                MLog.v("Mms_UI_CMA", "### CMA.loadMessagesAndDraft: START_TYPE_FLOAT. shouldLoadDraft=" + shouldLoadDraft);
                this.mComposeRecipientsView.initRecipientsEditor();
            }
            if (shouldLoadDraft || (this.mRcsComposeMessage != null && this.mRcsComposeMessage.isRcsShouldLoadDraft())) {
                loadDraft();
            }
            this.mMessagesAndDraftLoaded = true;
        }
    }

    private void updateSendFailedNotification() {
        long threadId = this.mConversation.getThreadId();
        if (threadId > 0) {
            ThreadEx.execute(new NotifyRunner(threadId));
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        ContactList listContact;
        super.onSaveInstanceState(outState);
        MLog.d("Mms_UI_CMA", "onSaveInstanceState entry");
        if (this.mComposeRecipientsView == null || !this.mComposeRecipientsView.isVisible()) {
            listContact = this.mConversation.getRecipients();
        } else {
            this.mComposeRecipientsView.commitNumberChip();
            listContact = this.mComposeRecipientsView.getRecipients();
        }
        outState.putString("recipients", listContact != null ? listContact.serialize() : "");
        outState.putBoolean("display_subject_editor", isSubjectEditorVisible());
        this.mRichEditor.writeStateToBundle(outState);
        if (this.mSendDiscreetMode) {
            outState.putBoolean("exit_on_sent", this.mSendDiscreetMode);
        }
        if (this.mForwardMessageMode) {
            outState.putBoolean("forwarded_message", this.mForwardMessageMode);
        }
    }

    public void onResume() {
        super.onResume();
        MLog.d("Mms_UI_CMA", "onResume");
        this.mNeedSaveDraft = true;
        if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.onResume(getIntent());
        }
        FloatMmsRequsetReceiver.stopPopupMsgAcitvity(getContext());
        String number = null;
        if (getRecipients() != null && getRecipients().size() > 0) {
            number = ((Contact) getRecipients().get(0)).getNumber();
        }
        if (number == null || !number.equals(WapPushMsg.WAP_PUSH_MESSAGE_ID)) {
            this.mIsPushConversation = false;
        } else {
            this.mIsPushConversation = true;
        }
        if (!isBottomViewVisible()) {
            this.mBottomView.setVisibility(8);
        }
        if (!this.isPeeking) {
            if (!MmsConfig.getSupportSmartSmsFeature() || this.mSmartSmsComponse == null || getRecipients() == null || getRecipients().size() != 1) {
                this.mEditLayoutShowStatu = (short) 1;
            } else {
                this.mSmartSmsComponse.queryMenu(this, number, (short) 2);
            }
        }
        this.mRichEditor.resumePosition();
        this.mMsgListAdapter.setOnDataSetChangedListener(this.mDataSetChangedListener);
        this.mMsgListAdapter.setDarkThemeStaus(System.getInt(getContext().getContentResolver(), "power_save_theme_status", 0));
        this.mCryptoCompose.notifyActivityVisibility(true, this.mMsgListAdapter);
        addRecipientsListeners();
        if (MLog.isLoggable("Mms_app", 2)) {
            log("onResume::update title, mConversation tid=" + this.mConversation.getThreadId());
        }
        if (this.mFromStop) {
            this.mActionbarAdapter.checkContactChange();
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (!ComposeMessageFragment.this.isDetached()) {
                        ContactList recipients = ComposeMessageFragment.this.mComposeRecipientsView.isVisible() ? ComposeMessageFragment.this.mComposeRecipientsView.constructContactsFromInput(false) : ComposeMessageFragment.this.getRecipients();
                        ComposeMessageFragment.this.updateTitle(recipients);
                        if (ComposeMessageFragment.this.mRcsComposeMessage != null) {
                            ComposeMessageFragment.this.mRcsComposeMessage.requestCapabilitiesForSubActivity(recipients);
                        }
                    }
                }
            }, 100);
        }
        this.mIsRunning = true;
        updateThreadIdIfRunning();
        if (HwMessageUtils.isSplitOn()) {
            this.mActivityHasFocused = true;
        }
        if (this.mActivityHasFocused) {
            markAsRead();
        }
        this.mThreadID = this.mConversation.getThreadId();
        checkSuperPowerSaveMode();
        if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.onResume();
        }
        if (!isInMultiWindowMode()) {
            this.mComposeRecipientsView.onActivityResume();
        }
        this.mCryptoCompose.onResume(getActivity(), this.mConversation);
        if (this.mComposeChoosePanel.isVisible() || this.mConversationInputManager.isMediaPickerVisible()) {
            hideKeyboard();
        }
        if (this.isPeeking) {
            this.mComposeRecipientsView.hideRecipientEditor();
            this.mMsgListView.setVisibility(0);
            hideKeyboard();
        } else if (MmsCommon.isFromPeekReply(getIntent())) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(9805), 300);
        }
        if (this.mRichEditor != null) {
            ViewTreeObserver vto = this.mRichEditor.getViewTreeObserver();
            vto.removeOnPreDrawListener(this.mMmMessageEditLayoutPreDrawListener);
            vto.addOnPreDrawListener(this.mMmMessageEditLayoutPreDrawListener);
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                MessagingNotification.clearAllNotificationInThread(ComposeMessageFragment.this.getActivity(), ComposeMessageFragment.this.mConversation.getThreadId());
            }
        });
    }

    public void onPause() {
        super.onPause();
        this.mFromStop = true;
        getActivity().getWindow().clearFlags(4194304);
        if (HwMessageUtils.isSplitOn() && this.mMsgListAdapter != null) {
            this.mMsgListAdapter.setOnDataSetChangedListener(null);
        }
        if (MessagingNotification.getRcsMessagingNotification() == null || !MessagingNotification.getRcsMessagingNotification().isRcsSwitchOn()) {
            MessagingNotification.setCurrentlyDisplayedThreadId(-2);
        } else {
            MessagingNotification.getRcsMessagingNotification().setCurrentlyDisplayedThreadId(-2, 0);
        }
        removeRecipientsListeners();
        if (this.mAsyncDialog != null) {
            this.mAsyncDialog.clearPendingProgressDialog();
        }
        markAsRead();
        this.mIsRunning = false;
        if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.onPause4Rcs();
        }
        this.mCryptoCompose.onPause();
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            this.mActionMode = null;
        }
        this.mSplitActionBar.dismissPopup();
        this.mActionBarWhenSplit.getSplitActionBarView().dismissPopup();
        sendComposeMessageStopPlayBroadcast();
    }

    public void onStop() {
        super.onStop();
        this.mBackgroundQueryHandler.cancelOperation(9527);
        if (this.mMsgListAdapter != null) {
            this.mMsgListAdapter.setOnDataSetChangedListener(null);
        }
        if (this.mActivityHasFocused) {
            blockMarkAsRead(false);
        }
        if (HwMessageUtils.isSplitOn() && this.mMsgListView != null) {
            int childCount = this.mMsgListView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                MessageListItem mgsItem = (MessageListItem) this.mMsgListView.getChildAt(i);
                if (!(mgsItem == null || mgsItem.getMessageItem().mSlideshow == null)) {
                    mgsItem.getMessageItem().mSlideshow.unregisterAllModelChangedObservers();
                }
            }
        }
        if (MLog.isLoggable("Mms_app", 2)) {
            log("save draft");
        }
        setMessageFullScreen();
        if ((!MmsConfig.isSupportDraftWithoutRecipient() || this.mNeedSaveDraft) && (!HwMessageUtils.isSplitOn() || this.mIsDraftLoadFinished || (getActivity() instanceof ComposeMessageActivity))) {
            saveDraft(true);
        }
        this.mCryptoToastIsShow = false;
        this.mShouldLoadDraft = true;
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this.mHttpProgressReceiver);
        this.mComposeRecipientsView.onActivityStop();
        this.mDetalDialog = null;
        MmsWidgetProvider.notifyDatasetChanged(getContext());
        if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.onStop();
        }
        if (this.mMsgListAdapter != null) {
            this.mMsgListAdapter.clearCachedListItems();
        }
        this.mCryptoCompose.notifyActivityVisibility(false, this.mMsgListAdapter);
    }

    private void setMessageFullScreen() {
        if (HwMessageUtils.isSplitOn() && (getActivity() instanceof ConversationList)) {
            HwBaseFragment frag = ((ConversationList) getActivity()).getRightFragment();
            if (frag instanceof MessageFullScreenFragment) {
                this.mRichEditor.setText(((MessageFullScreenFragment) frag).getmDataEditor().getText().toString());
            }
        }
    }

    private void deleteFileWhenOnDestroy() {
        getActivity().deleteFile("vcard_temp.vcf");
    }

    private void destroyMyCryptoCompose() {
        this.mCryptoCompose.onDestroy(getContext());
    }

    private void sendComposeMessageStopPlayBroadcast() {
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent("ACTION_COMPOSERESUME_STOPPLAYING"));
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (HwMessageUtils.isSplitOn()) {
            if (this.mMsgListAdapter != null) {
                this.mMsgListAdapter.setOnDataSetChangedListener(null);
                this.mMsgListAdapter.changeCursor(null);
                this.mMsgListAdapter.cancelBackgroundLoading();
                this.mMsgListAdapter.clearTextSpanCache(true);
            }
            if (this.mRichEditor != null) {
                this.mRichEditor.getViewTreeObserver().removeOnPreDrawListener(this.mMmMessageEditLayoutPreDrawListener);
                this.mMmMessageEditLayoutPreDrawListener = null;
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        MLog.d("Mms_UI_CMA", "onDestroy ");
        if (this.mComposeRecipientsView == null && (getActivity() instanceof ConversationList)) {
            ((ConversationList) getActivity()).setFlagBackToLeft(true);
            return;
        }
        if (HwMessageUtils.isSplitOn() && MmsApp.getApplication().getThumbnailManager() != null) {
            MmsApp.getApplication().getThumbnailManager().clear();
        }
        if (HwMessageUtils.isSplitOn() && this.mRichEditor != null) {
            this.mRichEditor.removeRichAttachmentListener(this);
        }
        if (!(this.mRichEditor == null || HwMessageUtils.isSplitOn())) {
            this.mRichEditor.removeRichAttachmentListener(this);
            MmsApp.getApplication().getThumbnailManager().clear();
            RichMessageManager.get().removeRichMessageManager(getActivity().hashCode());
        }
        if (this.mCreateVCardFile) {
            deleteFileWhenOnDestroy();
        }
        if (this.mComposeRecipientsView != null) {
            this.mComposeRecipientsView.onActivityDestroy();
        }
        if (!(HwMessageUtils.isSplitOn() || this.mMsgListAdapter == null)) {
            this.mMsgListAdapter.setOnDataSetChangedListener(null);
            this.mMsgListAdapter.changeCursor(null);
            this.mMsgListAdapter.cancelBackgroundLoading();
            this.mMsgListAdapter.clearTextSpanCache(true);
        }
        this.mComposeBottomView.onActivityDestroy();
        if (this.mComposeRecipientsView != null) {
            this.mComposeRecipientsView.onActivityDestroy();
        }
        unregisterContactDataChangeObserver();
        unRegisterPriavcyMonitor();
        unRegisterDefSmsAppChanged();
        if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.onDestroy();
        }
        if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.clearEmptyRcsThread(this.mSendDiscreetMode, this.mConversation);
        }
        if (!(this.mSendDiscreetMode || this.mConversation == null || this.mRichEditor == null || !this.mRichEditor.isEmptyThread(this.mConversation) || this.mContentResolver == null)) {
            HwBackgroundLoader.getInst().postTask(new Runnable() {
                public void run() {
                    SqliteWrapper.delete(ComposeMessageFragment.this.getContext(), ComposeMessageFragment.this.mContentResolver, Threads.OBSOLETE_THREADS_URI, "", null);
                    Conversation.clear(ComposeMessageFragment.this.getContext());
                    ComposeMessageFragment.this.mThreadID = 0;
                }
            });
        }
        if (MmsConfig.getSupportSmartSmsFeature() && getRecipients() != null && getRecipients().size() > 0 && !this.isPeeking) {
            String number = ((Contact) getRecipients().get(0)).getNumber();
            if (this.isPeeking) {
                SmartSmsSdkUtil.setUnclearCacheNumber(number);
            } else {
                SmartSmsSdkUtil.clearCache(hashCode(), number);
            }
        }
        if (this.mLocalBroadcastManager != null) {
            this.mLocalBroadcastManager.unregisterReceiver(this.mSaveDraftReceiver);
        }
        if (this.mVoWifiReceiver != null) {
            getActivity().unregisterReceiver(this.mVoWifiReceiver);
            this.mVoWifiReceiver = null;
        }
        destroyMyCryptoCompose();
        if (this.mMenuEx != null) {
            this.mMenuEx.clear();
        }
        if (this.mMsgListView != null) {
            this.mMsgListView.clearAllModelChangeObserversInDescendants();
        }
        LinkerTextTransfer.getInstance().clear();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        boolean z = false;
        super.onConfigurationChanged(newConfig);
        this.mActionBarWhenSplit.setActionBarHeight(HwMessageUtils.getSplitActionBarHeight(getContext()));
        this.mActionBarWhenSplit.doOnConfigurationChange(newConfig);
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
        }
        if (isDetached()) {
            MLog.d("Mms_UI_CMA", "onConfigurationChanged::activity is finishing, return");
            return;
        }
        this.mComposeRecipientsView.onActivityConfigurationChanged(newConfig);
        this.mComposeBottomView.onActivityConfigurationChanged(newConfig);
        this.mRichEditor.onConfigurationChanged(newConfig);
        if (resetConfiguration(newConfig)) {
            getActivity().invalidateOptionsMenu();
        }
        onKeyboardStateChanged();
        this.mComposeChoosePanel.onActivityConfigurationChanged(newConfig);
        resetScreenHeight();
        this.mComposeBottomView.resetBottomScrollerHeight(true);
        updateEmojiAddView();
        if (this.mSmartSmsComponse != null) {
            this.mSmartSmsComponse.reShowMenu();
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(9804), 200);
        updateComposeStartIcon();
        updateRecipientsStartIcon();
        this.mSplitActionBar.dismissPopup();
        this.mActionBarWhenSplit.getSplitActionBarView().dismissPopup();
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            this.mActionMode = null;
        }
    }

    public void resetScreenHeight() {
        this.mScreenHeight = MessageUtils.getWindowHeightPixels(getResources());
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
        this.mComposeRecipientsView.onKeyboardStateChanged(this.mIsSmsEnabled, this.mIsKeyboardOpen);
        this.mRichEditor.onKeyboardStateChanged(this.mIsSmsEnabled, this.mIsKeyboardOpen);
        initFullScreenView();
        this.isFirstCheck = true;
        showEnableFullScreenIcon();
    }

    public void onUserInteraction() {
        checkPendingNotification();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 23:
            case Place.TYPE_MUSEUM /*66*/:
                if (isPreparedForSending() && this.mComposeBottomView.isOneCardValid()) {
                    confirmSendMessageIfNeeded();
                    return true;
                }
            case Place.TYPE_SCHOOL /*82*/:
                if (keyCode == 82) {
                    getActivity().invalidateOptionsMenu();
                    break;
                }
                break;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 4:
                if (event.isTracking() && !event.isCanceled()) {
                    if (this.mComposeChoosePanel.hidePanel()) {
                        return true;
                    }
                    if (this.mConversationInputManager.isMediaPickerVisible()) {
                        if (this.mConversationInputManager.getMediaPickerFullScreenState()) {
                            this.mConversationInputManager.showMediaPicker(false, true);
                            return true;
                        }
                        this.mConversationInputManager.showHideMediaPicker(false, true);
                        return true;
                    } else if (this.isProcessingBackKeyEvent && this.userOperation) {
                        MLog.d("quit_compose", "back key event is processing , ignore");
                        return true;
                    } else {
                        this.isProcessingBackKeyEvent = true;
                        if (procFastBackKey().booleanValue()) {
                            return true;
                        }
                        getActivity().onBackPressed();
                        this.isProcessingBackKeyEvent = false;
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    private Boolean procFastBackKey() {
        if (!this.mRichEditor.isDraftLoading()) {
            return Boolean.valueOf(false);
        }
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                int i = 0;
                while (i < 4) {
                    try {
                        if (!ComposeMessageFragment.this.mRichEditor.isDraftLoading()) {
                            break;
                        }
                        Thread.currentThread();
                        Thread.sleep(20);
                        i++;
                    } catch (InterruptedException e) {
                        MLog.d("Mms_UI_CMA", "procFastBackKey occur InterruptedException");
                    }
                }
                if (ComposeMessageFragment.this.mRichEditor.isDraftLoading()) {
                    ComposeMessageFragment.this.mRichEditor.setDraftStateUnknow();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                ComposeMessageFragment.this.userOperation = false;
                if (ComposeMessageFragment.this.getActivity() != null) {
                    ComposeMessageFragment.this.getActivity().dispatchKeyEvent(new KeyEvent(0, 4));
                }
            }
        }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
        return Boolean.valueOf(true);
    }

    private void exitComposeMessageActivity(Runnable exit) {
        exitComposeMessageActivity(exit, false);
    }

    public void exitComposeMessageActivity(Runnable exit, boolean isBackPressed) {
        if (this.mComposeRecipientsView.isVisible()) {
            this.mComposeRecipientsView.commitNumberChip();
            this.mRichEditor.setWorkingRecipients(this.mComposeRecipientsView.getNumbers());
        }
        if (this.mRichEditor.isWorthSaving()) {
            if (MmsConfig.isSupportDraftWithoutRecipient()) {
                this.mToastForDraftSave = true;
            }
            boolean isMms = this.mRichEditor.requiresMms();
            if (this.mComposeRecipientsView.isVisible()) {
                if (!this.mComposeRecipientsView.hasValidRecipient(isMms)) {
                    StatisticalHelper.incrementReportCount(getContext(), 2007);
                    if (!this.mCryptoCompose.isSmsEncryptionSwitchOn() || this.mCryptoCompose.isSmsRelateSignature()) {
                        if (this.mDiscardDraftAlertDialog != null && this.mDiscardDraftAlertDialog.isShowing()) {
                            this.mDiscardDraftAlertDialog.dismiss();
                        }
                        if (MmsConfig.isSupportDraftWithoutRecipient()) {
                            this.mToastForDraftSave = false;
                        } else {
                            this.mDiscardDraftAlertDialog = MessageUtils.showDiscardDraftConfirmDialog(getContext(), new DiscardDraftListener(null));
                            return;
                        }
                    }
                    MLog.d("Mms_UI_CMA", " exitComposeMessageActivity crypto message does not save draft.");
                    return;
                }
                if (this.mComposeRecipientsView.hasInvalidRecipient(isMms)) {
                    this.mComposeRecipientsView.filterInvalidRecipients(isMms);
                }
            }
            if (!MmsConfig.isSupportDraftWithoutRecipient()) {
                this.mToastForDraftSave = true;
            }
            if (!(this.mHwCustComposeMessage == null || this.mHwCustComposeMessage.doCustExitCompose(isBackPressed, exit, getWorkingMessage()))) {
                exit.run();
            }
            return;
        }
        exit.run();
    }

    public WorkingMessage getWorkingMessage() {
        return this.mRichEditor.getWorkingMessage();
    }

    private boolean isSubjectEditorVisible() {
        return this.mRichEditor.isSubjectEditorVisible();
    }

    public void onProtocolChanged(boolean mms) {
        this.mHandler.removeMessages(1001);
        this.mHandler.removeMessages(1000);
        Message msg = this.mHandler.obtainMessage(1001);
        msg.arg1 = mms ? 1 : 0;
        this.mHandler.sendMessageDelayed(msg, 100);
    }

    public void onPreMessageSent() {
        this.mCryptoCompose.onPreMessageSent(this.mComposeRecipientsView.hasFocus());
        if (getActivity() != null) {
            getActivity().runOnUiThread(this.mResetMessageRunnable);
        }
    }

    public void onMessageSent() {
        this.mRichEditor.setDiscarded(false);
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ComposeMessageFragment.this.startMsgListQuery();
                    ComposeMessageFragment.this.mCryptoCompose.notifyMessageSent(ComposeMessageFragment.this.mMsgListAdapter);
                    ComposeMessageFragment.this.updateThreadIdIfRunning();
                }
            });
        }
    }

    public void onMaxPendingMessagesReached() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ComposeMessageFragment.this.getContext(), R.string.too_many_unsent_mms_Toast, 1).show();
                }
            });
        }
    }

    public void onEmailAddressInput() {
        this.mNeedPopupEmailCheck = true;
        if (this.mComposeRecipientsView.isVisible() && MmsConfig.isShowCheckEmailPoup()) {
            alertForSendMms();
        }
    }

    public void onMessageStateChanged() {
        showEnableFullScreenIcon();
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (!isDetached() && this.mMenuEx != null) {
            this.mMenuEx.setOptionMenu(menu).onPrepareOptionsMenu();
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mMenuEx != null) {
            this.mMenuEx.setOptionMenu(menu).createOptionsMenu();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mMenuEx.onOptionsItemSelected(item);
    }

    private void onAddContactsToMms() {
        Intent contactIntent = new Intent();
        contactIntent.setAction("android.intent.action.PICK");
        contactIntent.setType("vnd.android.cursor.dir/contact");
        startActivityForResult(contactIntent, 111);
    }

    public void addAttachment(int type, boolean replace) {
        addAttachment(type, null, replace);
    }

    public void addAttachment(int type, AttachmentSelectData attachmentData, boolean replace) {
        if (this.mCryptoCompose.needNotifyUser(type)) {
            this.mCryptoCompose.addAttachment(this.mRichEditor, type, replace);
            MLog.d("Mms_UI_CMA", "addAttachment crypto message could not add attachment.");
            return;
        }
        MmsApp.getApplication().getThumbnailManager().clear();
        if (getAvailableSpace() < ((long) MmsConfig.getMmsDownloadAvailableSpaceLimit())) {
            Toast.makeText(getContext(), R.string.sim_full_body_Toast, 1).show();
        } else if (attachmentData != null) {
            switch (type) {
                case 2:
                case 3:
                case 5:
                case AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS /*1201*/:
                case 1211:
                    setAttachmentSelectData(attachmentData, replace);
                    break;
                default:
                    MLog.d("Mms_UI_CMA", "addAttahcment attachmentData contentType is error.");
                    break;
            }
        } else {
            long sizeLimit;
            Intent contactIntent;
            switch (type) {
                case 0:
                    StatisticalHelper.incrementReportCount(getContext(), 2093);
                    this.mRichEditor.getFocus(false);
                    this.mComposeChoosePanel.showSmileyDialog(replace, getContext());
                    break;
                case 1:
                    StatisticalHelper.incrementReportCount(getContext(), 2094);
                    this.mNeedSaveDraft = false;
                    if (RcseMmsExt.isRcsMode()) {
                        this.mNeedSaveDraft = true;
                    }
                    if (!OsUtil.hasCameraPermission()) {
                        MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
                        OsUtil.requestPermission(getSplitActivity(), new String[]{"android.permission.CAMERA"}, 145);
                        break;
                    }
                    MessageUtils.capturePicture(this, 101);
                    break;
                case 2:
                    StatisticalHelper.incrementReportCount(getContext(), 2095);
                    this.mNeedSaveDraft = false;
                    if (RcseMmsExt.isRcsMode()) {
                        this.mNeedSaveDraft = true;
                    }
                    startActivityForResult(MessageUtils.getSelectImageIntent(), 100);
                    break;
                case 3:
                    if (this.mRcsComposeMessage != null && this.mRcsComposeMessage.isRcdForRcs(this.mConversation)) {
                        if (!OsUtil.hasCameraPermission()) {
                            MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
                            OsUtil.requestPermission(getSplitActivity(), new String[]{"android.permission.CAMERA"}, 146);
                            break;
                        }
                        this.mRcsComposeMessage.recordVideo(150223);
                        break;
                    }
                    sizeLimit = this.mRichEditor.computeAddRecordSizeLimit();
                    if (sizeLimit <= 0) {
                        Toast.makeText(getContext(), getString(R.string.message_too_big_for_video_Toast), 0).show();
                        break;
                    }
                    StatisticalHelper.incrementReportCount(getContext(), 2098);
                    this.mNeedSaveDraft = false;
                    if (RcseMmsExt.isRcsMode()) {
                        this.mNeedSaveDraft = true;
                    }
                    MessageUtils.recordVideo(this, OfflineMapStatus.EXCEPTION_SDCARD, sizeLimit);
                    break;
                    break;
                case 4:
                    StatisticalHelper.incrementReportCount(getContext(), 2097);
                    this.mNeedSaveDraft = false;
                    if (RcseMmsExt.isRcsMode()) {
                        this.mNeedSaveDraft = true;
                    }
                    startActivityForResult(MessageUtils.getSelectVideoIntent(), 102);
                    break;
                case 5:
                    sizeLimit = this.mRichEditor.computeAddRecordSizeLimit();
                    if (sizeLimit <= 0) {
                        Toast.makeText(getContext(), getString(R.string.exceed_message_size_limitation), 0).show();
                        break;
                    }
                    StatisticalHelper.incrementReportCount(getContext(), AMapException.CODE_AMAP_NEARBY_KEY_NOT_BIND);
                    this.mNeedSaveDraft = false;
                    if (RcseMmsExt.isRcsMode()) {
                        this.mNeedSaveDraft = true;
                    }
                    MessageUtils.recordSound(this, LocationRequest.PRIORITY_NO_POWER, sizeLimit);
                    break;
                case 6:
                    sizeLimit = this.mRichEditor.computeAddRecordSizeLimit();
                    if (sizeLimit <= 0) {
                        Toast.makeText(getContext(), getString(R.string.exceed_message_size_limitation), 0).show();
                        break;
                    }
                    StatisticalHelper.incrementReportCount(getContext(), AMapException.CODE_AMAP_NEARBY_INVALID_USERID);
                    this.mNeedSaveDraft = false;
                    if (RcseMmsExt.isRcsMode()) {
                        this.mNeedSaveDraft = true;
                    }
                    startActivityForResult(MessageUtils.getSelectAudioIntent(sizeLimit), LocationRequest.PRIORITY_LOW_POWER);
                    break;
                case 7:
                    MLog.i("Mms_UI_CMA", "in Method:addAttachment()::This is the smiley case ");
                    this.mCryptoToastIsShow = true;
                    StatisticalHelper.incrementReportCount(getContext(), 2092);
                    Intent intent = new Intent();
                    intent.putExtra("FROM_COMPOCE", false);
                    intent.setClass(getContext(), CommonPhrase.class);
                    this.mNeedSaveDraft = false;
                    if (RcseMmsExt.isRcsMode()) {
                        this.mNeedSaveDraft = true;
                    }
                    startActivityForResult(intent, 130128);
                    break;
                case 8:
                    this.mCryptoToastIsShow = true;
                    StatisticalHelper.incrementReportCount(getContext(), 2091);
                    this.mNeedSaveDraft = false;
                    if (RcseMmsExt.isRcsMode()) {
                        this.mNeedSaveDraft = true;
                    }
                    onAddContactsToMms();
                    break;
                case 10:
                    if (!OsUtil.hasCalendarPermission()) {
                        MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
                        OsUtil.requestPermission(getSplitActivity(), new String[]{"android.permission.READ_CALENDAR"}, 140);
                        break;
                    }
                    this.mCryptoToastIsShow = true;
                    StatisticalHelper.incrementReportCount(getContext(), 2099);
                    this.mNeedSaveDraft = false;
                    if (RcseMmsExt.isRcsMode()) {
                        this.mNeedSaveDraft = true;
                    }
                    showVcalendarDlgToCalendar();
                    break;
                case 11:
                    StatisticalHelper.incrementReportCount(getContext(), 2091);
                    contactIntent = new Intent();
                    contactIntent.setAction("android.intent.action.PICK");
                    contactIntent.setType("vnd.android.cursor.dir/contact");
                    startActivityForResult(contactIntent, 111);
                    break;
                case 12:
                    contactIntent = new Intent();
                    contactIntent.setAction("android.intent.action.PICK");
                    contactIntent.setType("vnd.android.cursor.dir/vnd.google.note");
                    startActivityForResult(contactIntent, 113);
                    break;
                case 13:
                    if (isSubjectEditorVisible()) {
                        this.mRichEditor.showSubjectEditor(false);
                        if (!TextUtils.isEmpty(this.mRichEditor.getSubject())) {
                            this.mRichEditor.setSubject(null, true);
                        }
                        this.mRichEditor.setEditTextFocus();
                    } else {
                        StatisticalHelper.incrementReportCount(getContext(), 2096);
                        this.mComposeChoosePanel.hidePanel();
                        if (this.mConversationInputManager.isMediaPickerVisible()) {
                            this.mConversationInputManager.showHideMediaPicker(false, false);
                            this.mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    HwMessageUtils.displaySoftInput(ComposeMessageFragment.this.getContext(), ComposeMessageFragment.this.mRichEditor.getFocusedChild());
                                }
                            }, 200);
                        } else {
                            HwMessageUtils.displaySoftInput(getContext(), this.mRichEditor.getFocusedChild());
                        }
                        this.mRichEditor.showSubjectEditor(true);
                        this.mRichEditor.setSubjectEditFocus();
                    }
                    this.mCryptoCompose.handleInsertSubject();
                    updateSendButtonView();
                    if (this.mIsLandscape && this.mConversationInputManager.isMediaPickerVisible()) {
                        this.mConversationInputManager.showHideMediaPicker(false, true);
                        break;
                    }
                case 14:
                    if (this.mRichEditor != null) {
                        this.mRichEditor.showSlideOptionsDialog();
                    }
                    if (this.mRcsComposeMessage != null) {
                        this.mRcsComposeMessage.updateSendModeToSms();
                        break;
                    }
                    break;
                case 1001:
                    startActivityForResult(MessageUtils.getSelectImageIntent(getActivity(), 5), 144);
                    break;
                default:
                    if (this.mRcsComposeMessage != null) {
                        this.mRcsComposeMessage.addAttachment(type);
                        break;
                    }
                    break;
            }
        }
    }

    private Activity getSplitActivity() {
        if (getActivity() instanceof ConversationList) {
            return (ConversationList) getActivity();
        }
        return (ComposeMessageActivity) getActivity();
    }

    private void switchPermissionRequestResult(int requestCode) {
        switch (requestCode) {
            case 1:
                StatisticalHelper.incrementReportCount(getContext(), 2258);
                return;
            case 2:
                MapMediaChooser.remindUserIfNecessary(getContext());
                return;
            case 3:
                StatisticalHelper.incrementReportCount(getContext(), 2259);
                return;
            case 140:
                this.mCryptoToastIsShow = true;
                StatisticalHelper.incrementReportCount(getActivity(), 2099);
                showVcalendarDlgToCalendar();
                return;
            case 141:
                handleSendIntent();
                return;
            case 142:
                HwMessageUtils.showCallMenuInContacts(getActivity(), null, true);
                return;
            case 145:
                MessageUtils.capturePicture(this, 101);
                return;
            case 146:
                this.mRcsComposeMessage.recordVideo(150223);
                return;
            default:
                return;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length == 0 || permissions.length == 0) {
            MLog.d("Mms_UI_CMA", "no permission granted, return");
            return;
        }
        boolean grantResult = true;
        for (int result : grantResults) {
            if (result != 0) {
                grantResult = false;
                break;
            }
        }
        if (grantResult) {
            switchPermissionRequestResult(requestCode);
        } else if (System.currentTimeMillis() - MmsCommon.getRequestTimeMillis() < 500) {
            Intent intent = new Intent("huawei.intent.action.REQUEST_PERMISSIONS");
            intent.setPackage("com.huawei.systemmanager");
            intent.putExtra("KEY_HW_PERMISSION_ARRAY", permissions);
            intent.putExtra("KEY_HW_PERMISSION_PKG", getActivity().getPackageName());
            try {
                startActivityForResult(intent, requestCode);
            } catch (Exception e) {
                MLog.e("Mms_UI_CMA", "recheckUserRejectPermissions: Exception", (Throwable) e);
            }
        }
    }

    private void showVcalendarDlgToCalendar() {
        if (MmsConfig.getEnableSendVcalByMms()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setItems(getContext().getResources().getStringArray(R.array.vcal_menu), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            ComposeMessageFragment.this.startActivityForResult(new Intent("com.huawei.action.MESSAGE_EVENTS"), 110);
                            return;
                        case 1:
                            Intent addVCalendarIntent = new Intent("android.intent.action.GET_CONTENT");
                            addVCalendarIntent.addCategory("android.intent.category.OPENABLE");
                            addVCalendarIntent.setType("text/x-vcalendar");
                            ComposeMessageFragment.this.startActivityForResult(addVCalendarIntent, 114);
                            return;
                        default:
                            return;
                    }
                }
            });
            builder.setTitle(R.string.vcalendar_calendar);
            builder.show();
            return;
        }
        startActivityForResult(new Intent("com.huawei.action.MESSAGE_EVENTS"), 110);
    }

    private void delaySetAttachment(final ArrayList<Uri> uriLists, final int type, final boolean doAppend) {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                ComposeMessageFragment.this.mRichEditor.setNewAttachment(uriLists, type, doAppend);
            }
        }, 500);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.mReCode = requestCode;
        this.mComposeChoosePanel.hidePanel();
        if (!(RcseMmsExt.isRcsMode() || !this.mConversationInputManager.isMediaPickerVisible() || data == null || resultCode != -1 || requestCode == 111 || requestCode == 144)) {
            this.mConversationInputManager.showHideMediaPicker(false, false);
            if (!this.mIsLandscape) {
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        ComposeMessageFragment.this.showKeyboard();
                    }
                }, 100);
            }
        }
        this.mWaitingForSubActivity = false;
        this.mShouldLoadDraft = false;
        this.mRichEditor.removeFakeMmsForDraft();
        if (requestCode == 12 && data != null) {
            this.mRichEditor.changeSlideDuration(Integer.parseInt(data.getAction()) * 1000);
        }
        if (this.mRcsComposeMessage != null && this.mRcsComposeMessage.isRcsSwitchOn()) {
            if (!this.mRcsComposeMessage.isRCSFileTypeInvalid(getContext(), requestCode, data)) {
                data = this.mRcsComposeMessage.getVcardDataForRcs(requestCode, resultCode, data);
                requestCode = this.mRcsComposeMessage.getReqCodeForRcs(requestCode, resultCode, this.mConversation);
            } else {
                return;
            }
        }
        if (requestCode == 109) {
            this.mRichEditor.asyncDeleteDraftSmsMessage(this.mConversation);
        }
        if (requestCode == 111) {
            this.mNeedSaveDraft = true;
            handleAddVcard(data);
        }
        if (requestCode == 116 && resultCode == -1) {
            handleVcardAfterPickItem();
        }
        if (requestCode == 108 && this.mAddContactIntent != null) {
            String address = this.mAddContactIntent.getStringExtra(Scopes.EMAIL);
            if (address == null) {
                address = this.mAddContactIntent.getStringExtra("phone");
            }
            if (address != null) {
                Contact contact = Contact.get(address, false);
                if (contact != null) {
                    contact.reload();
                }
            }
        }
        if (requestCode == 132) {
            refreshScaleSize();
        }
        if (resultCode == -1) {
            switch (requestCode) {
                case 100:
                    this.mNeedSaveDraft = true;
                    if (data != null) {
                        if (!HAS_DRM_CONFIG || !isForwardLock(data.getData())) {
                            this.mRichEditor.setNewAttachment(data.getData(), 2, false);
                            break;
                        } else {
                            Toast.makeText(getContext(), R.string.message_compose_attachments_skipped_drm_Toast, 1).show();
                            return;
                        }
                    }
                    return;
                case 101:
                    this.mNeedSaveDraft = true;
                    if (this.mRcsComposeMessage != null && this.mRcsComposeMessage.takePicForRcs(this.mConversation, TempFileProvider.getScrapPicPath())) {
                        MLog.i("Mms_UI_CMA", "onActivityResult REQUEST_CODE_TAKE_PICTURE mRcsComposeMessage.takePicForRcs is true, break");
                        break;
                    }
                    Uri picUri = TempFileProvider.renameScrapFile(".jpg", "" + System.currentTimeMillis());
                    getContext().sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", picUri));
                    MmsApp.getApplication().getThumbnailManager().removeThumbnail(picUri);
                    this.mRichEditor.setTakePictureState(true);
                    this.mRichEditor.setNewAttachment(picUri, 2, false);
                    break;
                case 102:
                    this.mNeedSaveDraft = true;
                    if (data != null) {
                        if (!HAS_DRM_CONFIG || !isForwardLock(data.getData())) {
                            this.mRichEditor.setNewAttachment(data.getData(), 5, false);
                            break;
                        } else {
                            Toast.makeText(getContext(), R.string.message_compose_attachments_skipped_drm_Toast, 1).show();
                            return;
                        }
                    }
                    return;
                case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                    this.mNeedSaveDraft = true;
                    Uri videoUri = TempFileProvider.renameScrapFile(".3gp", "" + System.currentTimeMillis());
                    getContext().sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", videoUri));
                    MmsApp.getApplication().getThumbnailManager().removeThumbnail(videoUri);
                    this.mRichEditor.setNewAttachment(videoUri, 5, false);
                    break;
                case LocationRequest.PRIORITY_LOW_POWER /*104*/:
                    this.mNeedSaveDraft = true;
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            if (!HAS_DRM_CONFIG || !isForwardLock(uri)) {
                                this.mRichEditor.setNewAttachment(getActivity(), data.getData(), 3, false);
                                break;
                            } else {
                                Toast.makeText(getContext(), R.string.message_compose_attachments_skipped_drm_Toast, 1).show();
                                return;
                            }
                        }
                        return;
                    }
                    return;
                case LocationRequest.PRIORITY_NO_POWER /*105*/:
                    this.mNeedSaveDraft = true;
                    if (data != null) {
                        this.mRichEditor.setNewAttachment(data.getData(), 3, false);
                        break;
                    }
                    return;
                case 107:
                    if (data != null) {
                        if (data.getBooleanExtra("exit_ecm_result", false)) {
                            sendMessage(false);
                            break;
                        }
                    }
                    return;
                    break;
                case 109:
                    if (data != null) {
                        this.mCryptoToastIsShow = false;
                        this.mComposeRecipientsView.processPickResult(data);
                        break;
                    }
                    return;
                case 110:
                    this.mNeedSaveDraft = true;
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            ArrayList<Uri> uriList = bundle.getParcelableArrayList("hw_eventsurl_list");
                            if (uriList != null) {
                                this.mRichEditor.insertVcalendarText(uriList);
                                break;
                            }
                        }
                        return;
                    }
                    return;
                    break;
                case 114:
                    this.mNeedSaveDraft = true;
                    if (data != null) {
                        this.mRichEditor.setNewAttachment(data.getData(), 7, false);
                        break;
                    }
                    return;
                case 115:
                    if (data != null) {
                        if (data.hasExtra("forward_thread_id")) {
                            if (data.getLongExtra("forward_thread_id", -1) == this.mConversation.getThreadId()) {
                                this.mNeedLoadDraft = false;
                                break;
                            }
                        }
                    }
                    return;
                case 117:
                    if (data != null) {
                        this.mCryptoToastIsShow = false;
                        String smsData = data.getStringExtra("full_screen_data");
                        if (!data.getBooleanExtra("full_screen_send_broadcast_enable", true)) {
                            this.mToastToMms = data.getBooleanExtra("full_screen_send_broadcast_enable", true);
                        }
                        this.mRichEditor.setText(smsData);
                        if (!TextUtils.isEmpty(smsData)) {
                            if (data.getBooleanExtra("full_screen_send_enable", false)) {
                                this.mComposeHolder.onSendButtonClick(this.mComposeBottomView.getSingleCardSubId());
                                hideFullScreenButton();
                                break;
                            }
                        }
                    }
                    return;
                    break;
                case 131:
                case 132:
                    if (data != null) {
                        if (data.getBooleanExtra("go_to_conversation_list", false)) {
                            finish();
                            return;
                        }
                    }
                    return;
                    break;
                case 140:
                    if (OsUtil.hasCalendarPermission()) {
                        this.mCryptoToastIsShow = true;
                        StatisticalHelper.incrementReportCount(getActivity(), 2099);
                        showVcalendarDlgToCalendar();
                        break;
                    }
                    break;
                case 141:
                    if (OsUtil.hasCalendarPermission()) {
                        handleSendIntent();
                        break;
                    }
                    break;
                case 142:
                    if (OsUtil.hasChooseSubScriptionPermission()) {
                        HwMessageUtils.showCallMenuInContacts(getActivity(), null, true);
                        break;
                    }
                    break;
                case 144:
                    this.mNeedSaveDraft = true;
                    if (data != null) {
                        ArrayList<Uri> uriLists = data.getParcelableArrayListExtra("select-item-list");
                        if (uriLists != null && uriLists.size() != 0) {
                            delaySetAttachment(uriLists, 2, false);
                            break;
                        }
                        return;
                    }
                    return;
                    break;
                case 145:
                    if (OsUtil.hasCameraPermission()) {
                        MessageUtils.capturePicture(this, 101);
                        break;
                    }
                    break;
                case 146:
                    if (OsUtil.hasCameraPermission()) {
                        this.mRcsComposeMessage.recordVideo(150223);
                        break;
                    }
                    break;
                case 130128:
                    this.mNeedSaveDraft = true;
                    if (data != null) {
                        Object extra = data.getExtra("COMMON_PHRASE");
                        if (extra != null) {
                            this.mRichEditor.insertPhrase((CharSequence) extra);
                            break;
                        }
                    }
                    return;
                    break;
                default:
                    if (this.mRcsComposeMessage != null) {
                        break;
                    }
                    return;
            }
            this.mComposeRecipientsView.showInvalidDestinationToast();
        }
    }

    private void refreshScaleSize() {
        float scale = PreferenceUtils.getPreferenceFloat(getContext(), "pref_key_sms_font_scale", ContentUtil.FONT_SIZE_NORMAL);
        ((HwBaseActivity) getActivity()).setFontScale(scale);
        this.mMsgListAdapter.onScaleChanged(scale);
    }

    public void handleAddVcard(Intent data) {
        IOException e;
        Throwable th;
        if (data != null && data.getData() != null) {
            getActivity().deleteFile("vcard_temp.vcf");
            ArrayList<Uri> uriList = data.getParcelableArrayListExtra("SelItemData_KeyValue");
            if (uriList == null) {
                uriList = new ArrayList();
                uriList.add(data.getData());
            }
            VCardComposer vCardComposer = null;
            FileOutputStream outputStream = null;
            Uri fileuri;
            Intent intent;
            try {
                VCardComposer composer = new VCardComposer(getContext(), -1073741824, true);
                try {
                    outputStream = getContext().openFileOutput("vcard_temp.vcf", 32768);
                    StringBuffer selection = new StringBuffer("_id");
                    selection.append(" in (");
                    for (Uri uri : uriList) {
                        selection.append(uri.getLastPathSegment()).append(",");
                    }
                    selection.deleteCharAt(selection.length() - 1).append(")");
                    if (!composer.init(selection.toString(), null)) {
                        MLog.i("Mms_UI_CMA", "VCardComposer init failed");
                        if (composer != null) {
                            composer.terminate();
                        }
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e2) {
                                MLog.e("Mms_UI_CMA", MLog.getStackTraceString(e2));
                            }
                        }
                    } else if (composer.getCount() == 0) {
                        MLog.i("Mms_UI_CMA", " VCardComposer.getCount() == 0");
                        if (composer != null) {
                            composer.terminate();
                        }
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e22) {
                                MLog.e("Mms_UI_CMA", MLog.getStackTraceString(e22));
                            }
                        }
                    } else {
                        while (!composer.isAfterLast()) {
                            outputStream.write(VcardMessageHelper.filterVcardNumbers(composer.createOneEntry()).getBytes(Charset.defaultCharset()));
                        }
                        if (composer != null) {
                            composer.terminate();
                        }
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e222) {
                                MLog.e("Mms_UI_CMA", MLog.getStackTraceString(e222));
                            }
                        }
                        this.mCreateVCardFile = true;
                        fileuri = Uri.fromFile(getContext().getFileStreamPath("vcard_temp.vcf"));
                        intent = new Intent();
                        intent.setData(fileuri);
                        intent.setClass(getContext(), ContactItemPickActivity.class);
                        this.mCryptoToastIsShow = true;
                        startActivityForResult(intent, 116);
                    }
                } catch (IOException e3) {
                    e222 = e3;
                    vCardComposer = composer;
                } catch (Throwable th2) {
                    th = th2;
                    vCardComposer = composer;
                }
            } catch (IOException e4) {
                e222 = e4;
                try {
                    MLog.e("Mms_UI_CMA", "composer.createOneEntry() failed : ", (Throwable) e222);
                    if (vCardComposer != null) {
                        vCardComposer.terminate();
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e2222) {
                            MLog.e("Mms_UI_CMA", MLog.getStackTraceString(e2222));
                        }
                    }
                    this.mCreateVCardFile = true;
                    fileuri = Uri.fromFile(getContext().getFileStreamPath("vcard_temp.vcf"));
                    intent = new Intent();
                    intent.setData(fileuri);
                    intent.setClass(getContext(), ContactItemPickActivity.class);
                    this.mCryptoToastIsShow = true;
                    startActivityForResult(intent, 116);
                } catch (Throwable th3) {
                    th = th3;
                    if (vCardComposer != null) {
                        vCardComposer.terminate();
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e22222) {
                            MLog.e("Mms_UI_CMA", MLog.getStackTraceString(e22222));
                        }
                    }
                    throw th;
                }
            }
        }
    }

    private void handleVcardAfterPickItem() {
        Uri fileuri = Uri.fromFile(getContext().getFileStreamPath("vcard_temp.vcf"));
        if (MmsConfig.getEnableMmsVcard()) {
            showVcardMmsTypeDialog(fileuri);
            return;
        }
        this.mRichEditor.insertVcardText(fileuri);
        this.mComposeRecipientsView.showInvalidDestinationToast();
    }

    private String getCreationModeWarning(MediaModel media) {
        String mWarningMessage = "";
        if (media == null) {
            return getString(R.string.dialog_msg_warningmode_send);
        }
        if (media.isImage()) {
            return getString(R.string.dialog_msg_warningmode_image);
        }
        if (media.isAudio()) {
            return getString(R.string.prefEntries_creation_mode_audio);
        }
        if (media.isVideo()) {
            return getString(R.string.dialog_msg_warningmode_video);
        }
        return getString(R.string.dialog_msg_warningmode);
    }

    private void showCreationModeWarning(boolean discard, boolean send, MediaModel media, int index) {
        if (getActivity() != null) {
            final MediaModel mediaModel = media;
            final boolean z = send;
            final boolean z2 = discard;
            final int i = index;
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder message = new AlertDialog.Builder(ComposeMessageFragment.this.getContext()).setIcon(17301543).setTitle(R.string.dialog_title_warning).setMessage(ComposeMessageFragment.this.getCreationModeWarning(mediaModel));
                    final boolean z = z;
                    message = message.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (z) {
                                ComposeMessageFragment.this.mRichEditor.sendMessage(ComposeMessageFragment.this.mDebugRecipients, ComposeMessageFragment.this.subSelectedForDialog);
                                ComposeMessageFragment.this.checkMessageSendMode();
                                ComposeMessageFragment.this.mSentMessage = true;
                                ComposeMessageFragment.this.mSendingMessage = true;
                                ComposeMessageFragment.this.addRecipientsListeners();
                            }
                        }
                    });
                    z = z2;
                    final int i = i;
                    message.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (z) {
                                ComposeMessageFragment.this.mRichEditor.removeSlide(i);
                            } else {
                                ComposeMessageFragment.this.mSendingMessage = false;
                            }
                        }
                    }).show();
                }
            });
        }
    }

    private boolean handleRepliedMessage() {
        int i = false;
        Intent intent = getIntent();
        if (this.isSimReply) {
            String number = intent.getStringExtra(HarassNumberUtil.NUMBER);
            if (number != null) {
                this.mConversation.setRecipients(ContactList.getByNumbers(number, false, true));
            }
            return false;
        } else if (!intent.getBooleanExtra("replied_mms_to_all", false)) {
            return false;
        } else {
            Uri uri = (Uri) intent.getParcelableExtra("msg_uri");
            if (uri != null) {
                this.mRichEditor.loadWorkingMessage(getContext(), this, uri);
            }
            this.mMsgListAdapter.changeCursor(null);
            String recipients = intent.getStringExtra("recipients");
            String subject = getResources().getString(R.string.message_reply, new Object[]{"Re:"});
            if (this.mHwCustComposeMessage != null) {
                subject = this.mHwCustComposeMessage.prepareSubjectInReply(subject);
            }
            String oldSubject = intent.getStringExtra("subject");
            if (oldSubject != null) {
                if (oldSubject.startsWith(subject)) {
                    subject = oldSubject;
                } else {
                    subject = subject + oldSubject;
                }
            }
            if (!TextUtils.isEmpty(subject)) {
                this.mRichEditor.setSubject(subject, true);
                this.mRichEditor.showSubjectEditor(true);
            }
            updateSendButtonView();
            List<String> numbers = new ArrayList();
            if (recipients != null) {
                ContactList contactList = ContactList.getByNumbers(recipients, false, true);
                String[] recArray = contactList.getNumbers();
                this.mConversation.setRecipients(contactList);
                int length = recArray.length;
                while (i < length) {
                    numbers.add(recArray[i]);
                    i++;
                }
                this.mRichEditor.setWorkingRecipients(numbers);
            }
            return true;
        }
    }

    private boolean handleForwardedMessage() {
        Intent intent = getIntent();
        if (intent == null || !this.mForwardMessageMode) {
            return false;
        }
        Uri uri = (Uri) intent.getParcelableExtra("msg_uri");
        if (uri != null) {
            this.mRichEditor.loadWorkingMessage(getContext(), this, uri);
            this.mRichEditor.setSubject(intent.getStringExtra("subject"), false);
            this.mRichEditor.syncWorkingMessageToUI();
        } else {
            this.mRichEditor.setText(intent.getStringExtra("sms_body"));
        }
        this.mMsgListAdapter.changeCursor(null);
        return true;
    }

    private boolean checkCreationModeError(boolean discard, boolean send, SlideshowModel slideshowModel) {
        boolean CreationCheckResult = true;
        if (slideshowModel != null && slideshowModel.size() > 0) {
            for (MediaModel media : slideshowModel.get(slideshowModel.size() - 1)) {
                int errorRes = -1;
                if (media instanceof ImageModel) {
                    boolean exceedsbound = false;
                    if (CarrierContentRestriction.getRestricedKeySet().contains(media.getContentType())) {
                        boolean notexceedboundifportrait = ((ImageModel) media).getWidth() <= MmsConfig.getMaxRestrictedImageWidth() ? ((ImageModel) media).getHeight() <= MmsConfig.getMaxRestrictedImageHeight() : false;
                        boolean notexceedboundiflandscape = ((ImageModel) media).getWidth() <= MmsConfig.getMaxRestrictedImageHeight() ? ((ImageModel) media).getHeight() <= MmsConfig.getMaxRestrictedImageWidth() : false;
                        exceedsbound = (notexceedboundifportrait || notexceedboundiflandscape) ? false : true;
                    }
                    if (MmsConfig.isGcfMms305Enabled() && exceedsbound) {
                        errorRes = R.string.type_picture;
                    }
                }
                ArrayList<String[]> mimeType = new ArrayList();
                mimeType.add(new String[]{media.getContentType(), media.getMediaSize() + ""});
                if (MessageUtils.isRestrictedType(mimeType)) {
                    if (media instanceof VideoModel) {
                        errorRes = R.string.type_video;
                    } else if (media instanceof AudioModel) {
                        errorRes = R.string.type_audio;
                    } else if (media instanceof ImageModel) {
                        errorRes = R.string.type_picture;
                    }
                }
                if (errorRes != -1) {
                    String creationMode = MmsConfig.getCurrentCreationMode();
                    if (creationMode.equals("restrictionmode")) {
                        if (discard) {
                            this.mRichEditor.discard();
                        }
                        this.mSendingMessage = false;
                        this.mRichEditor.handleAddAttachmentError(-3, errorRes);
                    } else if (creationMode.equals("warningmode")) {
                        showCreationModeWarning(discard, send, media, slideshowModel.size() - 1);
                    }
                    CreationCheckResult = false;
                }
            }
        }
        return CreationCheckResult;
    }

    private void checkRestrictedMime(boolean discard) {
        SlideshowModel slideshowModel = this.mRichEditor.getSlideshow();
        if (slideshowModel != null) {
            checkCreationModeError(discard, false, slideshowModel);
        }
    }

    private boolean checkRestrictedSize(int subscription) {
        SlideshowModel slideshowmodel = this.mRichEditor.getSlideshow();
        return slideshowmodel == null || checkCreationModeError(false, true, slideshowmodel);
    }

    private boolean handleSendIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return false;
        }
        String mimeType = intent.getStringExtra("mimeType") != null ? intent.getStringExtra("mimeType") : intent.getType();
        if (this.mRcsComposeMessage != null && this.mRcsComposeMessage.rcsRedirectSendIntent(intent)) {
            return false;
        }
        String action = intent.getAction();
        if ("android.intent.action.SEND".equals(action)) {
            boolean result = false;
            if (extras.containsKey("android.intent.extra.STREAM")) {
                if (this.mHwCustComposeMessage != null && this.mHwCustComposeMessage.sendMmsUnsupportToast()) {
                    return false;
                }
                Uri uri = (Uri) extras.getParcelable("android.intent.extra.STREAM");
                if (uri != null && "file".equals(uri.getScheme()) && "text/plain".equals(mimeType)) {
                    Toast.makeText(getContext(), getString(R.string.invalid_file_format_Toast), 0).show();
                }
                if (uri == null || !"text/x-vCard".equalsIgnoreCase(mimeType)) {
                    if (!"text/x-vCalendar".equalsIgnoreCase(mimeType)) {
                        if (mimeType != null) {
                            addAttachment(mimeType, uri, false);
                        }
                        result = true;
                    } else if (OsUtil.hasCalendarPermission()) {
                        ArrayList<Uri> lEventList = extras.getParcelableArrayList("hw_eventsurl_list");
                        if (lEventList == null && uri != null) {
                            this.mRichEditor.setNewAttachment(uri, 7, false);
                            result = true;
                        } else if (lEventList != null) {
                            showVcalendarDlgFromCalendar(uri, lEventList);
                            result = true;
                        }
                    } else {
                        MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
                        OsUtil.requestPermission(getActivity(), new String[]{"android.permission.READ_CALENDAR"}, 141);
                    }
                } else if (intent.getBooleanExtra("from_contact_profile", false)) {
                    this.mRichEditor.setNewAttachment(uri, 6, false);
                    this.mComposeRecipientsView.showInvalidDestinationToast();
                    result = true;
                } else {
                    showVcardMmsTypeDialog(uri);
                    result = true;
                }
            }
            if (extras.containsKey("android.intent.extra.TEXT")) {
                this.mRichEditor.setText(extras.getString("android.intent.extra.TEXT"));
                result = true;
            }
            if (result) {
                return true;
            }
        } else if ("android.intent.action.SEND_MULTIPLE".equals(action) && extras.containsKey("android.intent.extra.STREAM")) {
            if (this.mHwCustComposeMessage != null && this.mHwCustComposeMessage.sendMmsUnsupportToast()) {
                return false;
            }
            ArrayList<Parcelable> uris = extras.getParcelableArrayList("android.intent.extra.STREAM");
            if (uris == null) {
                return false;
            }
            this.mMultiUris = uris;
            this.mMineType = mimeType;
            this.mUriPostion = 0;
            int currentSlideCount = this.mRichEditor.getSlideSize();
            int importCount = uris.size();
            int maxSlides = MmsConfig.getMaxSlides();
            if (importCount + currentSlideCount > MmsConfig.getMaxSlides()) {
                importCount = Math.min(MmsConfig.getMaxSlides() - currentSlideCount, importCount);
                Toast.makeText(getContext(), getResources().getQuantityString(R.plurals.too_many_attachments_Toast, maxSlides, new Object[]{Integer.valueOf(maxSlides), Integer.valueOf(importCount)}), 1).show();
            }
            this.mRichEditor.setAddMultiHandler(this.mHandler);
            this.mHandler.sendEmptyMessage(1);
            return true;
        } else if ("android.intent.action.forward_message".equals(action)) {
            MessageItem msgitem = getMessageItemFromIntent(intent);
            if (msgitem != null) {
                this.mMsgListView.forwardMessage(msgitem);
            }
        } else if (this.mRcsComposeMessage != null && this.mRcsComposeMessage.isRCSAction(action)) {
            return this.mRcsComposeMessage.handleSendIntent(this.mConversation, intent, this.mRichEditor, this.mHandler);
        }
        return false;
    }

    private MessageItem getMessageItemFromIntent(Intent intent) {
        Cursor cur;
        Exception e;
        Throwable th;
        long id = intent.getLongExtra("mmsid", -1);
        long threadId = intent.getLongExtra("threadid", -1);
        Uri fUri = ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
        if (threadId > 0) {
            try {
                MessageItem item;
                cur = SqliteWrapper.query(getContext(), getContext().getContentResolver(), fUri, MessageListAdapter.PROJECTION, null, null, null);
                if (cur != null) {
                    try {
                        if (cur.moveToFirst()) {
                            ColumnsMap Map = new ColumnsMap();
                            while (!cur.isAfterLast()) {
                                if (cur.getLong(Map.mColumnMsgId) == id) {
                                    item = new MessageItem(getContext(), "mms", cur, new ColumnsMap(), null);
                                    break;
                                }
                                cur.moveToNext();
                            }
                        }
                        item = null;
                    } catch (Exception e2) {
                        e = e2;
                    }
                } else {
                    item = null;
                }
                if (cur == null) {
                    return item;
                }
                cur.close();
                return item;
            } catch (Exception e3) {
                e = e3;
                cur = null;
                try {
                    MLog.e("Mms_UI_CMA", "querry error in getMessageItemFromIntent", (Throwable) e);
                    if (cur != null) {
                        cur.close();
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (cur != null) {
                        cur.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                cur = null;
                if (cur != null) {
                    cur.close();
                }
                throw th;
            }
        }
        cur = null;
        return null;
    }

    public void addAttachment(String type, Uri uri, boolean append) {
        if (uri != null) {
            boolean wildcard = "*/*".equals(type);
            if (type.startsWith("image/") || (wildcard && uri.toString().startsWith(mImageUri))) {
                this.mRichEditor.setNewAttachment(uri, 2, true);
            } else if (type.startsWith("video/") || (wildcard && uri.toString().startsWith(mVideoUri))) {
                this.mRichEditor.setNewAttachment(uri, 5, true);
            } else if (type.startsWith("audio/") || (wildcard && uri.toString().startsWith(mAudioUri))) {
                this.mRichEditor.setNewAttachment(uri, 3, true);
            } else if ("text/x-vCalendar".equalsIgnoreCase(type)) {
                this.mRichEditor.setNewAttachment(uri, 7, true);
            } else {
                Toast.makeText(getContext(), getString(R.string.unsupported_media_format_Toast, new Object[]{""}), 0).show();
            }
        }
    }

    public void onClick(View v) {
        if (R.id.select_all == v.getId()) {
            judgeAttachSmiley();
        } else if (R.id.add_emojis == v.getId()) {
            StatisticalHelper.incrementReportCount(getContext(), 2228);
            if (this.mComposeChoosePanel.isShowSmileyFace()) {
                this.mComposeChoosePanel.hidePanel();
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        ComposeMessageFragment.this.showKeyboard();
                    }
                }, 100);
            } else {
                if (this.mConversationInputManager.isMediaPickerVisible()) {
                    this.mConversationInputManager.showHideMediaPicker(false, false);
                }
                hideKeyboard();
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        ComposeMessageFragment.this.mComposeChoosePanel.showSmileyDialog(true, ComposeMessageFragment.this.getContext());
                    }
                }, 100);
            }
            updateEmojiAddView();
            if (this.mComposeRecipientsView.isFocused()) {
                this.mRichEditor.setEditTextFocus();
            }
        } else if (R.id.add_attach == v.getId()) {
            StatisticalHelper.incrementReportCount(getContext(), 2119);
            if (this.mIsSuperPowerSaveMode) {
                ResEx.self().showMmsToast(getContext(), R.string.mms_function_not_available, 0);
                return;
            }
            if (this.mRcsComposeMessage != null) {
                this.mRcsComposeMessage.handleRecipientEditor();
                this.mRcsComposeMessage.initCapabilityFlag();
            }
            if (this.mComposeBottomView != null) {
                this.mComposeBottomView.switchToAudioView();
            }
            if (this.mComposeChoosePanel.isShowAttachment()) {
                this.mComposeChoosePanel.hidePanel();
            }
            if (this.mConversationInputManager.isMediaPickerVisible()) {
                this.mConversationInputManager.showHideMediaPicker(false, false);
                showKeyboard();
            } else if (this.mRcsComposeMessage == null || !this.mRcsComposeMessage.isStopShowAddAttachmentForFt()) {
                if (this.mComposeRecipientsView.isFocused()) {
                    this.mRichEditor.setEditTextFocus();
                }
                MLog.v("Mms_UI_CMA", "onClick:: add attachment");
                if (this.mRcsComposeMessage != null) {
                    this.mRcsComposeMessage.setFtCapaByNetwork();
                    this.mRcsComposeMessage.setCapabilityFlag(this.mComposeRecipientsView.isVisible(), this.mComposeRecipientsView.getNumbers(), this.mConversation);
                }
                if (this.mComposeBottomView.isMultiCardsValid() && this.mIsLandscape && this.mComposeRecipientsView.isVisible()) {
                    hideFullScreenButton();
                }
                if (this.mComposeChoosePanel.isShowSmileyFace()) {
                    this.mComposeChoosePanel.hidePanel();
                    updateEmojiAddView();
                }
                hideKeyboard();
                this.mConversationInputManager.showHideMediaPicker(true, true);
                this.mComposeBottomView.updateshowEditor();
            } else {
                this.mRcsComposeMessage.setCapabilityFlag(this.mComposeRecipientsView.isVisible(), this.mComposeRecipientsView.getNumbers(), this.mConversation);
                return;
            }
            if (this.mHwCustComposeMessage != null) {
                this.mHwCustComposeMessage.hideKeyboard();
            }
        } else if (R.id.btn_full_screen == v.getId()) {
            StatisticalHelper.incrementReportCount(getContext(), 2143);
            Intent intent = new Intent(getContext(), MessageFullScreenActivity.class);
            intent.putExtra("smsData", this.mRichEditor.getText().toString());
            intent.putExtra("sendState", this.mFullScreenButtonState);
            intent.putExtra("isSmsEncryption", this.mCryptoCompose.isSmsEncryptionSwitchOn());
            if (HwMessageUtils.isSplitOn()) {
                intent.putExtra("conversationid", getConversation().getThreadId());
            }
            if (this.mComposeBottomView != null) {
                intent.putExtra("isInRcsMode", this.mComposeBottomView.isMessageInRcsMode());
                intent.putExtra("isSendMessageEnable", this.mComposeBottomView.isSendMessageEnable());
            }
            if (this.mRcsComposeMessage != null) {
                this.mRcsComposeMessage.setFullScreenFlag(true);
                this.mRcsComposeMessage.configFullScreenIntent(intent);
            }
            this.mCryptoToastIsShow = true;
            Intent cryptoIntent = this.mCryptoCompose.setIntentValue(intent);
            if (HwMessageUtils.isSplitOn() && (getActivity() instanceof ConversationList)) {
                if (this.mComposeChoosePanel != null && this.mComposeChoosePanel.isVisible()) {
                    this.mComposeChoosePanel.hidePanel();
                }
                if (this.mConversationInputManager != null && this.mConversationInputManager.isMediaPickerVisible()) {
                    this.mConversationInputManager.showHideMediaPicker(false, true);
                }
                Activity activity = getActivity();
                HwBaseFragment messageFullScreenFragment = new MessageFullScreenFragment();
                messageFullScreenFragment.setController(new ControllerImpl(activity, messageFullScreenFragment));
                messageFullScreenFragment.setIntent(cryptoIntent);
                ((ConversationList) activity).changeRightAddToStack(messageFullScreenFragment, (HwBaseFragment) this);
            } else {
                startActivityForResult(cryptoIntent, 117);
            }
        } else {
            judgeAttachSmiley();
        }
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    private void initResourceRefs() {
        this.mRichEditor = (RichMessageEditor) findViewById(R.id.rich_message_editor);
        this.mRichEditor.setFragment(this);
        this.mRichEditor.setMessageStatusListener(this);
        this.mRichEditor.setListener(this.mComposeHolder);
        this.mRichEditor.addRichAttachmentListener(this);
        this.mRichEditor.setUpdateSubjectReViewListener(new UpdateSubjectReViewListener() {
            public void updateSubjectReView(boolean subjectState) {
                if (ComposeMessageFragment.this.mCryptoCompose != null) {
                    ComposeMessageFragment.this.mCryptoCompose.handleInsertSubject();
                }
            }
        });
        RichMessageManager.get().putRichMessageEditor(getActivity(), this.mRichEditor);
        this.mBottomView = findViewById(R.id.mms_message_edit_layout);
        this.mComposeBottomView = new ComposeBottomView(MmsApp.getApplication().getApplicationContext());
        this.mComposeBottomView.init(this.mBottomView, this.mComposeHolder);
        if (this.mRcsAudioMessage == null) {
            this.mRcsAudioMessage = new RcsAudioMessage(this);
        }
        this.mComposeBottomView.setRcsAudioMessage(this.mRcsAudioMessage);
        this.mComposeBottomView.setComposeButtomGroupView(findViewById(R.id.mms_compose_view_group));
        this.mBottomPanalMinHeight = (int) getContext().getResources().getDimension(R.dimen.mms_bottom_panal_max_height);
        this.mComposeBottomLayoutPaddingTopBottom = (int) getContext().getResources().getDimension(R.dimen.compose_bottom_layout_padding_top_bottom);
        this.mMsBottomPanalMaxHeight = (int) getContext().getResources().getDimension(R.dimen.mms_bottom_panal_max_height);
        this.mMsgListView = (MessageListView) findViewById(R.id.history);
        this.mMsgListView.setFastScrollEnabled(true);
        this.mMsgListView.setMsgListHoder(this.mComposeHolder);
        this.mMsgListView.setDivider(null);
        this.mMsgListView.setClipToPadding(false);
        this.mMsgListView.setOnSizeChangedListener(new OnSizeChangedListener() {
            public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                if (MLog.isLoggable("Mms_app", 2)) {
                    MLog.v("Mms_UI_CMA", "onSizeChanged: w=" + width + " h=" + height + " oldw=" + oldWidth + " oldh=" + oldHeight);
                }
                if (!ComposeMessageFragment.this.mMessagesAndDraftLoaded && oldHeight - height > 100) {
                    ComposeMessageFragment.this.loadMessagesAndDraft(ComposeMessageFragment.this.mShouldLoadDraft);
                }
                ComposeMessageFragment.this.smoothScrollToEnd(false, height - oldHeight);
                View vAttch;
                if (!ComposeMessageFragment.this.isInMultiWindowMode() || MessageUtils.getScreenHeight(ComposeMessageFragment.this.getActivity()) > ComposeMessageFragment.this.getContext().getResources().getDimensionPixelSize(R.dimen.multi_window_mode_do_not_show_attchment_or_emoji_min_height)) {
                    ComposeMessageFragment.this.mEmojiAdd.setEnabled(true);
                    ComposeMessageFragment.this.mEmojiAdd.setClickable(true);
                    vAttch = ComposeMessageFragment.this.findViewById(R.id.add_attach);
                    vAttch.setEnabled(true);
                    vAttch.setClickable(true);
                } else {
                    ComposeMessageFragment.this.mEmojiAdd.setEnabled(false);
                    ComposeMessageFragment.this.mEmojiAdd.setClickable(false);
                    vAttch = ComposeMessageFragment.this.findViewById(R.id.add_attach);
                    vAttch.setEnabled(false);
                    vAttch.setClickable(false);
                    if (ComposeMessageFragment.this.mComposeChoosePanel != null && (ComposeMessageFragment.this.mComposeChoosePanel.isShowAttachment() || ComposeMessageFragment.this.mComposeChoosePanel.isShowSmileyFace())) {
                        HwBackgroundLoader.getUIHandler().postDelayed(ComposeMessageFragment.this.mHidePanelRunnable, 200);
                    }
                }
                ComposeMessageFragment.this.updateEmojiAddView();
            }
        });
        this.mMsgListView.setOnTouchListener(this.mHideKeyboardTouchListener);
        this.mMsgListView.setClipChildren(false);
        this.mScreenHeight = MessageUtils.getWindowHeightPixels(getResources());
        this.mComposeMessageView = findViewById(R.id.mms_compose_message_view);
        this.mComposeLayoutGroup = (LinearLayout) findViewById(R.id.mms_compose_view_group);
        this.mComposeLayoutGroup.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                View bottomView = ComposeMessageFragment.this.mComposeLayoutGroup.findViewById(R.id.mms_bottom_group);
                if (ComposeMessageFragment.this.mScreenHeight - ComposeMessageFragment.this.mComposeMessageView.getMeasuredHeight() > 288) {
                    if (ComposeMessageFragment.this.mComposeRecipientsView == null || !ComposeMessageFragment.this.mComposeRecipientsView.isFocused() || ComposeMessageFragment.this.mComposeRecipientsView.getMatchedContactsCount() <= 0 || ComposeMessageFragment.this.getActivity().isInMultiWindowMode()) {
                        bottomView.setVisibility(0);
                    } else {
                        bottomView.setVisibility(8);
                    }
                    if (!ComposeMessageFragment.this.mIsKeyboardOpen) {
                        ComposeMessageFragment.this.mIsKeyboardOpen = true;
                        ComposeMessageFragment.this.mComposeRecipientsView.updateRecentContactList();
                    }
                } else {
                    bottomView.setVisibility(0);
                    if (ComposeMessageFragment.this.mIsKeyboardOpen) {
                        ComposeMessageFragment.this.mIsKeyboardOpen = false;
                        ComposeMessageFragment.this.mComposeRecipientsView.updateRecentContactList();
                    }
                }
                if (!(oldTop == top && oldBottom == bottom)) {
                    ComposeMessageFragment.this.mComposeBottomView.resetBottomScrollerHeight(true);
                }
                ComposeMessageFragment.this.updateEmojiAddView();
            }
        });
        updateAddAttachView();
        initFullScreenView();
        updateEmojiAddView();
        if (MmsConfig.getSupportSmartSmsFeature()) {
            initSmartSmsMenu();
        }
        if (this.mRcsComposeMessage != null) {
            this.mRcsComposeMessage.initResourceRefs();
        }
    }

    void undeliveredMessageDialog(long date) {
        String body;
        if (date >= 0) {
            body = getString(R.string.undelivered_msg_dialog_body, new Object[]{MessageUtils.formatTimeStampString(getContext(), date)});
        } else {
            body = getString(R.string.undelivered_sms_dialog_body_Toast);
        }
        Toast.makeText(getContext(), body, 1).show();
    }

    private void startMsgListQueryDelayed(long delay) {
        this.mHandler.removeMessages(9527);
        this.mHandler.sendEmptyMessageDelayed(9527, delay);
    }

    private void startMsgListQuery() {
        startMsgListQuery(9527);
    }

    private void startMsgListQuery(int token) {
        if (this.mSendDiscreetMode || this.mIsEditOnly) {
            MLog.d("Mms_UI_CMA", "Block MsgListQuery , bail!. SendDiscreetMode: " + this.mSendDiscreetMode + "; IsEditOnly: " + this.mIsEditOnly);
            return;
        }
        Uri conversationUri;
        String[] projection;
        if (this.mRcsComposeMessage != null && this.mRcsComposeMessage.isRcsSwitchOn()) {
            this.mRcsComposeMessage.initWidget(this.mThreadID, this.mMsgListView);
        }
        if (this.mRcsComposeMessage != null && this.mRcsComposeMessage.isSendFileFlagOn()) {
            MLog.d("Mms_UI_CMA", "MsgListQuery,photo sharing need query message list");
        } else if (this.mComposeRecipientsView.isVisible()) {
            MLog.d("Mms_UI_CMA", "Block MsgListQuery as RecipientsEditor is Visible, bail!");
            return;
        }
        if (this.mConversation.getRecipients().size() > 1) {
            conversationUri = this.mConversation.getGroupMessageUri();
            projection = MessageListAdapter.SINGLE_VIEW_PROJECTION;
        } else if (this.mRcsComposeMessage != null && this.mRcsComposeMessage.isRcsSwitchOn() && this.mConversation.getRecipients().size() == 1) {
            conversationUri = this.mRcsComposeMessage.getGroupMessageUri(this.mConversation);
            projection = MessageListAdapter.PROJECTION;
        } else {
            conversationUri = this.mConversation.getUri();
            projection = MessageListAdapter.PROJECTION;
        }
        if (conversationUri == null) {
            MLog.w("Mms_UI_CMA", "Block MsgListQuery as conversationUri is null, bail!");
            return;
        }
        long threadId = this.mConversation.getThreadId();
        if (this.mRcsComposeMessage != null) {
            MLog.i("Mms_UI_CMA", "mRcsComposeMessage != null, threadId = " + threadId);
            this.mRcsComposeMessage.setConversationId(this.mMsgListAdapter, threadId);
        }
        MLog.i("Mms_UI_CMA", "startMsgListQuery for " + conversationUri + ", threadId=" + threadId + " token: " + token);
        this.mBackgroundQueryHandler.cancelOperation(token);
        this.mBackgroundQueryHandler.startQuery(token, Long.valueOf(threadId), conversationUri, projection, null, null, null);
    }

    private void initMessageList() {
        if (this.mMsgListAdapter == null) {
            Pattern pattern;
            int i;
            String highlightString = getIntent().getStringExtra("highlight");
            if (highlightString == null) {
                pattern = null;
            } else {
                pattern = Pattern.compile(Pattern.quote(highlightString), 2);
            }
            String address = null;
            if (getRecipients() != null && getRecipients().size() > 0) {
                address = ((Contact) getRecipients().get(0)).getNumber();
            }
            this.mMsgListAdapter = new MessageListAdapter(getContext(), null, this.mMsgListView, true, pattern, address);
            this.mMsgListAdapter.setOnDataSetChangedListener(this.mDataSetChangedListener);
            this.mMsgListAdapter.setMCryptoMsgLiatAdapterLister(this.mComposeHolder);
            this.mMsgListAdapter.setMsgListItemHandler(this.mHandler);
            this.mMsgListAdapter.setLastItemLayoutCallback(this.mScroolOnLastItemLoadedCallback);
            this.mMsgListView.setAdapter(this.mMsgListAdapter);
            if (this.mRcsComposeMessage != null) {
                this.mRcsComposeMessage.initMessageList(this.mMsgListView, this.mMsgListAdapter);
            }
            this.mCryptoCompose.initEncryptSms(this.mMsgListAdapter, this.mMsgListView);
            MessageListView messageListView = this.mMsgListView;
            if (this.mSendDiscreetMode) {
                i = 4;
            } else {
                i = 0;
            }
            messageListView.setVisibility(i);
            EMUIListViewListenerV3 listener = new EMUIListViewListenerV3();
            this.mMsgListView.setListViewListener(listener);
            this.mMsgListView.setSelectionChangeLisenter(listener);
            this.mMsgListView.setOnItemLongClickListener(listener);
            this.mMsgListView.setMultiModeClickListener(this.mMsgListView.getMultiModeClickListener());
        }
    }

    private boolean loadDraft() {
        if (this.mNeedLoadDraft) {
            return this.mRichEditor.loadDraft(this.mConversation);
        }
        MLog.w("Mms_UI_CMA", "loadDraft mNeedLoadDraft is false");
        return false;
    }

    private boolean isForwardRecipientSameWithOriginThread() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("forwarded_message", false)) {
            long fid = intent.getLongExtra("forwarded_from_tid", 0);
            if (fid != 0) {
                long cid = this.mConversation.getThreadId();
                if (cid > 0 && cid == fid) {
                    return true;
                }
                if (cid != 0) {
                    return false;
                }
                Cache.getInstance();
                Conversation fConversation = Cache.get(fid);
                if (fConversation == null || !this.mComposeRecipientsView.isVisible()) {
                    return false;
                }
                ContactList fromContacts = fConversation.getRecipients();
                List<String> numbers = this.mComposeRecipientsView.getNumbers();
                if (numbers.size() == 0 || fromContacts == null || fromContacts.size() != numbers.size()) {
                    return false;
                }
                for (Contact c : ContactList.getByNumbers(numbers, false)) {
                    if (!fromContacts.contains(c)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    protected void saveDraft(boolean isStopping) {
        if (MLog.isLoggable("Mms_app", 2)) {
            LogTag.debug("saveDraft", new Object[0]);
        }
        if (!this.isPeeking) {
            if (this.mHwCustComposeMessage != null && this.mHwCustComposeMessage.isSameNumberForward(getIntent())) {
                MLog.d("Mms_UI_CMA", "This is same number, no need save draft.");
            } else if (isForwardRecipientSameWithOriginThread()) {
                MLog.d("Mms_UI_CMA", "This is same thread, note save draft");
            } else if (isDirty() || this.mConversation.getThreadId() == 0 || this.mHasUnreadMessage) {
                boolean recipientsHasNothing = !this.mComposeRecipientsView.isVisible() || this.mComposeRecipientsView.getRecipientCount() == 0;
                if (this.mCryptoCompose.isSmsEncryptionSwitchOn()) {
                    if (!(this.mCryptoToastIsShow || TextUtils.isEmpty(this.mRichEditor.getWorkingMessage().getText()) || this.mCryptoCompose.isSmsRelateSignature())) {
                        this.mRichEditor.getWorkingMessage().setText("");
                    }
                    this.mRichEditor.saveDraft(isStopping, recipientsHasNothing, this.mWaitingForSubActivity, this.mToastForDraftSave);
                    initDirtyModel();
                    return;
                }
                if (this.mComposeRecipientsView.isVisible()) {
                    this.mRichEditor.setHasEmail(this.mComposeRecipientsView.containsEmail(), false);
                }
                this.mRichEditor.saveDraft(isStopping, recipientsHasNothing, this.mWaitingForSubActivity, this.mToastForDraftSave);
                initDirtyModel();
            } else {
                MLog.d("Mms_UI_CMA", "draft contents not dirty, do not save draft.");
            }
        }
    }

    private void sendMessage(boolean bCheckEcmMode) {
        sendMessage(bCheckEcmMode, this.mComposeBottomView.getSingleCardSubId());
    }

    private void resetMessage() {
        if (MLog.isLoggable("Mms_app", 2)) {
            log("resetMessage");
        }
        this.mIsEditOnly = false;
        this.mRichEditor.resetMessage(this.mConversation);
        onDraftChanged();
        doMediaUpdate(13);
        this.mComposeRecipientsView.hideRecipientEditor();
        this.mActionBarWhenSplit.show(true);
        if (HwMessageUtils.isSplitOn() && (getActivity() instanceof ConversationList)) {
            ((ConversationList) getActivity()).hideLeftCover();
            if (!this.mForwardMessageMode) {
                ((LeftPaneConversationListFragment) ((ConversationList) getActivity()).getFragment()).setSelected(this.mConversation);
            }
        }
        if (this.mHwCustComposeMessage != null ? this.mHwCustComposeMessage.isHideKeyboard(this.mIsLandscape) : this.mIsLandscape) {
            hideKeyboard();
        }
        this.mSendingMessage = false;
        getActivity().invalidateOptionsMenu();
        appendSignature(false);
    }

    private long getMessageDate(Uri uri) {
        if (uri == null) {
            return -1;
        }
        Cursor cursor = SqliteWrapper.query(getContext(), this.mContentResolver, uri, new String[]{"date"}, null, null, null);
        if (cursor == null) {
            return -1;
        }
        try {
            if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                long j = cursor.getLong(0) * 1000;
                return j;
            }
            cursor.close();
            return -1;
        } finally {
            cursor.close();
        }
    }

    private void initActivityState(Bundle bundle) {
        Intent intent = getIntent();
        if (bundle != null) {
            setIntent(getIntent().setAction("android.intent.action.VIEW"));
            this.mConversation = Conversation.get(getContext(), ContactList.getByNumbers(bundle.getString("recipients"), false, true), false);
            this.mSendDiscreetMode = bundle.getBoolean("exit_on_sent", false);
            this.mForwardMessageMode = bundle.getBoolean("forwarded_message", false);
            if (this.mSendDiscreetMode) {
                this.mMsgListView.setVisibility(4);
            }
            this.mRichEditor.readStateFromBundle(bundle);
            boolean displaySubjectEditor = bundle.getBoolean("display_subject_editor");
            if (displaySubjectEditor) {
                this.mRichEditor.showSubjectEditor(displaySubjectEditor);
            }
            if (!TextUtils.isEmpty(bundle.getString("sms_body"))) {
                this.mRichEditor.asyncDeleteDraftSmsMessage(this.mConversation);
            }
            return;
        }
        Conversation conversation;
        long threadId = intent.getLongExtra("thread_id", 0);
        Uri intentData = intent.getData();
        if (threadId > 0) {
            if (this.mRcsComposeMessage == null || !this.mRcsComposeMessage.isRcsSwitchOn()) {
                conversation = Conversation.get(getContext(), threadId, false);
            } else {
                conversation = Conversation.get(getContext(), threadId, false, new ParmWrapper(null, Integer.valueOf(intent.getIntExtra("table_to_use", 1))));
            }
        } else if (intentData == null) {
            String address = intent.getStringExtra("address");
            if (TextUtils.isEmpty(address)) {
                conversation = Conversation.createNew(getContext());
            } else {
                conversation = Conversation.get(getContext(), ContactList.getByNumbers(address, false, true), false);
            }
        } else if (getActivityStartType() == 1) {
            conversation = Conversation.createtNew(getContext(), intentData);
        } else {
            conversation = Conversation.get(getContext(), intentData, false);
        }
        if (this.mIsEditOnly) {
            this.mConversation = Conversation.createNew(getContext());
            this.mConversation.setRecipients(conversation.getRecipients());
            MLog.d("Mms_UI_CMA", "Create ComposeMessageActivity with edit only");
        } else {
            this.mConversation = conversation;
        }
        updateThreadIdIfRunning();
        this.mSendDiscreetMode = intent.getBooleanExtra("exit_on_sent", false);
        this.mForwardMessageMode = intent.getBooleanExtra("forwarded_message", false);
        if (this.mSendDiscreetMode) {
            this.mMsgListView.setVisibility(4);
        }
        if (intentData != null) {
            this.mRichEditor.setText(getBody(intentData));
        }
        if (intent.hasExtra("sms_body")) {
            this.mRichEditor.setText(intent.getStringExtra("sms_body"));
            if (getActivityStartType() == 1) {
                this.mNeedLoadDraft = false;
            }
        }
        String subject = intent.getStringExtra("subject");
        if (!TextUtils.isEmpty(subject)) {
            this.mRichEditor.setSubject(subject, true);
            this.mRichEditor.showSubjectEditor(true);
        }
    }

    private void checkPendingNotification() {
        if (this.mPossiblePendingNotification && getActivity() != null && getActivity().hasWindowFocus()) {
            markAsRead();
            this.mPossiblePendingNotification = false;
        }
    }

    private void smoothScrollToEnd(boolean force, int listSizeChange) {
        int lastItemVisible = this.mMsgListView.getLastVisiblePosition();
        int lastItemInList = this.mMsgListAdapter.getCount() - 1;
        if (lastItemVisible < 0 || lastItemInList < 0) {
            if (MLog.isLoggable("Mms_app", 2)) {
                MLog.v("Mms_UI_CMA", "smoothScrollToEnd: lastItemVisible=" + lastItemVisible + ", lastItemInList=" + lastItemInList + ", mMsgListView not ready");
            }
            if (force && lastItemVisible < 0 && lastItemInList >= 0) {
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
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.v("Mms_UI_CMA", "smoothScrollToEnd newPosition: " + lastItemInList + " mLastSmoothScrollPosition: " + this.mLastSmoothScrollPosition + " first: " + this.mMsgListView.getFirstVisiblePosition() + " lastItemVisible: " + lastItemVisible + " lastVisibleItemBottom: " + lastVisibleItemBottom + " lastVisibleItemBottom + listSizeChange: " + (lastVisibleItemBottom + listSizeChange) + " mMsgListView.getHeight() - mMsgListView.getPaddingBottom(): " + (this.mMsgListView.getHeight() - this.mMsgListView.getPaddingBottom()) + " listSizeChange: " + listSizeChange);
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
        if (willScroll || lastItemInList == lastItemVisible) {
            if (Math.abs(listSizeChange) > SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                if (MLog.isLoggable("Mms_app", 2)) {
                    MLog.v("Mms_UI_CMA", "keyboard state changed. setSelection=" + lastItemInList);
                }
                if (lastItemTooTall) {
                    this.mMsgListView.setSelectionFromTop(lastItemInList, (listHeight - lastVisibleItemHeight) - this.mMsgListView.getPaddingTop());
                } else {
                    this.mMsgListView.setSelection(lastItemInList);
                }
            } else if (lastItemInList - lastItemVisible > 20 || lastItemInList - lastItemVisible <= 1) {
                if (MLog.isLoggable("Mms_app", 2)) {
                    MLog.v("Mms_UI_CMA", "too many to scroll, setSelection=" + lastItemInList);
                }
                this.mMsgListView.setSelection(lastItemInList);
            } else {
                if (MLog.isLoggable("Mms_app", 2)) {
                    MLog.v("Mms_UI_CMA", "smooth scroll to " + lastItemInList);
                }
                if (lastItemTooTall) {
                    this.mMsgListView.setSelectionFromTop(lastItemInList, (listHeight - lastVisibleItemHeight) - this.mMsgListView.getPaddingTop());
                } else {
                    this.mMsgListView.smoothScrollToPosition(lastItemInList);
                }
                this.mLastSmoothScrollPosition = lastItemInList;
            }
        }
    }

    public void onUpdate(Contact updated) {
        if (Conversation.isContactChanged(updated, this.mConversation) && !this.mTooOftenOnUpdate) {
            this.mTooOftenOnUpdate = true;
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    ComposeMessageFragment.this.mTooOftenOnUpdate = false;
                    ContactList recipients = ComposeMessageFragment.this.mComposeRecipientsView.isVisible() ? ComposeMessageFragment.this.mComposeRecipientsView.constructContactsFromInput(false) : ComposeMessageFragment.this.getRecipients();
                    if (MLog.isLoggable("Mms_app", 2)) {
                        ComposeMessageFragment.log("[CMA] onUpdate contact updated");
                    }
                    ComposeMessageFragment.this.updateTitle(recipients);
                }
            }, 300);
        }
    }

    private void addRecipientsListeners() {
        Contact.addListener(this);
    }

    private void removeRecipientsListeners() {
        Contact.removeListener(this);
    }

    private void confirmSendMessageIfNeeded(int subscription) {
        Exception e;
        String shortCode;
        if (this.mHwCustComposeMessage == null || !this.mHwCustComposeMessage.showWifiMessageErrorDialog()) {
            this.mSendSmsHandler.sendMessage(this.mSendSmsHandler.obtainMessage(100, subscription, 0));
            if (this.mHwCustComposeMessage == null || !this.mHwCustComposeMessage.isStopToSendMessageOnLTEOnly(subscription)) {
                boolean isMms;
                String[] numberArray;
                if (this.mRichEditor.requiresMms()) {
                    if (this.mRcsComposeMessage != null && this.mRcsComposeMessage.isRcsImMode()) {
                        MLog.d("Mms_UI_CMA", "rcs function no need focus on many unsent msg,skip it");
                    } else if (this.mClickTimeWhenTooManyUnsentMsg != -1 && System.currentTimeMillis() - this.mClickTimeWhenTooManyUnsentMsg <= 6000) {
                        MLog.d("Mms_UI_CMA", "has too many unsent msg over time, skip it");
                        this.mMmsRadarInfoManager.writeLogMsg(1311, "1001");
                        return;
                    } else if (this.mRichEditor.hasTooManyUnSentMsg()) {
                        this.mClickTimeWhenTooManyUnsentMsg = System.currentTimeMillis();
                        onMaxPendingMessagesReached();
                        MLog.d("Mms_UI_CMA", "has too many unsent msg, skip it");
                        this.mMmsRadarInfoManager.writeLogMsg(1311, "1000");
                        return;
                    }
                }
                VcardModel vCardModel = this.mRichEditor.getVcardModel();
                if (vCardModel != null) {
                    try {
                        VcardMessageHelper vCardMessageHelper = new VcardMessageHelper(getContext(), vCardModel.getData(), vCardModel.getVcardDetailList());
                        try {
                            vCardMessageHelper.updateVcardData();
                            vCardModel.setData(vCardMessageHelper.getData());
                            vCardModel.saveSelectState(false);
                        } catch (Exception e2) {
                            e = e2;
                            e.printStackTrace();
                            if (this.mRichEditor.getWorkingMessage() == null) {
                                MLog.d("Mms_UI_CMA", "working message is null, return");
                                return;
                            }
                            isMms = this.mRichEditor.requiresMms();
                            if (this.mComposeRecipientsView == null) {
                            }
                            this.mMmsRadarInfoManager.setIsMms(isMms);
                            if (!isMms) {
                            }
                            if (this.mComposeRecipientsView.isVisible()) {
                                this.mRichEditor.setHasEmail(this.mComposeRecipientsView.containsEmail(), true);
                                if (!this.mComposeRecipientsView.hasInvalidRecipient()) {
                                }
                                this.mComposeRecipientsView.alertForInvalidRecipient();
                                return;
                            }
                            MLog.i("Mms_UI_CMA", "User click send. No recipients. button sub: " + subscription);
                            numberArray = this.mRichEditor.getRecipientNumbers();
                            if (isMms) {
                            }
                            shortCode = "";
                            if (this.mHwCustComposeMessage != null) {
                                shortCode = this.mHwCustComposeMessage.getShortCodeErrorString();
                            }
                            sendMessage(this.mRichEditor.getText(), subscription);
                            return;
                        }
                    } catch (Exception e3) {
                        e = e3;
                        e.printStackTrace();
                        if (this.mRichEditor.getWorkingMessage() == null) {
                            isMms = this.mRichEditor.requiresMms();
                            if (this.mComposeRecipientsView == null) {
                            }
                            this.mMmsRadarInfoManager.setIsMms(isMms);
                            if (!isMms) {
                            }
                            if (this.mComposeRecipientsView.isVisible()) {
                                this.mRichEditor.setHasEmail(this.mComposeRecipientsView.containsEmail(), true);
                                if (this.mComposeRecipientsView.hasInvalidRecipient()) {
                                }
                                this.mComposeRecipientsView.alertForInvalidRecipient();
                                return;
                            }
                            MLog.i("Mms_UI_CMA", "User click send. No recipients. button sub: " + subscription);
                            numberArray = this.mRichEditor.getRecipientNumbers();
                            if (isMms) {
                            }
                            shortCode = "";
                            if (this.mHwCustComposeMessage != null) {
                                shortCode = this.mHwCustComposeMessage.getShortCodeErrorString();
                            }
                            sendMessage(this.mRichEditor.getText(), subscription);
                            return;
                        }
                        MLog.d("Mms_UI_CMA", "working message is null, return");
                        return;
                    }
                }
                if (this.mRichEditor.getWorkingMessage() == null) {
                    MLog.d("Mms_UI_CMA", "working message is null, return");
                    return;
                }
                isMms = this.mRichEditor.requiresMms();
                if (this.mComposeRecipientsView == null && ((this.mComposeRecipientsView.containsEmail() || this.mRichEditor.hasAttachment() || this.mRichEditor.hasSubject()) && this.mHwCustComposeMessage != null && this.mHwCustComposeMessage.sendMmsUnsupportToast())) {
                    MLog.d("Mms_UI_CMA", "not support mms, returned");
                    return;
                }
                this.mMmsRadarInfoManager.setIsMms(isMms);
                if (!isMms && this.mHwCustComposeMessage != null && this.mHwCustComposeMessage.judgeDSDisableByFDN(subscription)) {
                    this.mHwCustComposeMessage.showFDNToast();
                    MLog.d("Mms_UI_CMA", "disable by fdn DS, return");
                    this.mMmsRadarInfoManager.writeLogMsg(1311, "1002");
                    return;
                } else if (this.mComposeRecipientsView.isVisible()) {
                    MLog.i("Mms_UI_CMA", "User click send. No recipients. button sub: " + subscription);
                    numberArray = this.mRichEditor.getRecipientNumbers();
                    if (isMms || this.mHwCustComposeMessage == null || !this.mHwCustComposeMessage.judgeNumberAndRecipientInFDNList(isMms, numberArray, subscription)) {
                        shortCode = "";
                        if (this.mHwCustComposeMessage != null) {
                            shortCode = this.mHwCustComposeMessage.getShortCodeErrorString();
                        }
                        if (this.mHwCustPduPersister == null || !this.mHwCustPduPersister.isShortCodeFeatureEnabled() || TextUtils.isEmpty(shortCode) || !this.mHwCustPduPersister.hasShortCode(getWorkingMessage().requiresMms(), numberArray, getContext(), shortCode)) {
                            sendMessage(this.mRichEditor.getText(), subscription);
                        }
                        return;
                    }
                    this.mHwCustComposeMessage.popForFdn(this.mComposeRecipientsView.isVisible(), numberArray, this.mComposeRecipientsView, subscription);
                    MLog.d("Mms_UI_CMA", "disable by fdn, return");
                    this.mMmsRadarInfoManager.writeLogMsg(1311, "1002");
                    return;
                } else {
                    this.mRichEditor.setHasEmail(this.mComposeRecipientsView.containsEmail(), true);
                    if (this.mComposeRecipientsView.hasInvalidRecipient() || this.mComposeRecipientsView.hasComplexInvalidRecipient()) {
                        this.mComposeRecipientsView.alertForInvalidRecipient();
                    } else if (!isMms || this.mHwCustComposeMessage == null || this.mComposeRecipientsView.getRecipientsEditor() == null || !this.mHwCustComposeMessage.judgeNumberAndRecipientInFDNList(isMms, this.mComposeRecipientsView.getNumbers().toArray(), subscription)) {
                        MLog.i("Mms_UI_CMA", "User click send button with recipients. sub: " + subscription);
                        ContactList contactList = null;
                        shortCode = "";
                        if (!(this.mHwCustComposeMessage == null || this.mHwCustPduPersister == null || !this.mHwCustPduPersister.isShortCodeFeatureEnabled())) {
                            contactList = ContactList.getByNumbers(this.mComposeRecipientsView.getNumbers(), false);
                            shortCode = this.mHwCustComposeMessage.getShortCodeErrorString();
                        }
                        if (this.mHwCustPduPersister == null || r2 == null || !this.mHwCustPduPersister.isShortCodeFeatureEnabled() || TextUtils.isEmpty(shortCode) || !this.mHwCustPduPersister.hasShortCode(getWorkingMessage().requiresMms(), r2.getNumbers(), getContext(), shortCode)) {
                            MLog.d("Mms_UI_CMA", "sendMessage sub: " + subscription);
                            sendMessage(this.mRichEditor.getText(), subscription);
                        }
                    } else {
                        this.mHwCustComposeMessage.popForFdn(this.mComposeRecipientsView.isVisible(), this.mComposeRecipientsView.getNumbers().toArray(), this.mComposeRecipientsView, subscription);
                        MLog.d("Mms_UI_CMA", "disable by fdn_, return");
                        this.mMmsRadarInfoManager.writeLogMsg(1311, "1002");
                        return;
                    }
                    return;
                }
            }
            MLog.w("Mms_UI_CMA", "Stop To Send Message On LTE Only, return.");
            return;
        }
        MLog.w("Mms_UI_CMA", "HwCustComposeMessage showWifiMessageErrorDialog, return.");
    }

    private int getRecipientCount() {
        if (this.mComposeRecipientsView.isVisible()) {
            return this.mComposeRecipientsView.getRecipientCount();
        }
        if (this.mConversation != null) {
            return this.mConversation.getRecipients().size();
        }
        MLog.e("Mms_UI_CMA", "mConversation is EMPTY!!");
        return 0;
    }

    private void sendMessage(boolean bCheckEcmMode, int subscription) {
        if (bCheckEcmMode && Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
            try {
                startActivityForResult(new Intent("android.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS", null), 107);
                return;
            } catch (ActivityNotFoundException e) {
                MLog.e("Mms_UI_CMA", "Cannot find EmergencyCallbackModeExitDialog", (Throwable) e);
            }
        }
        if (!this.mSendingMessage) {
            if (!this.mConversation.getRecipients().serialize().equals(this.mDebugRecipients)) {
                String workingRecipients = this.mRichEditor.getWorkingRecipients();
                if (!(this.mDebugRecipients == null || this.mDebugRecipients.equals(workingRecipients))) {
                    LogTag.warnPossibleRecipientMismatch("Mms_TX CMA ComposeMessageActivity.sendMessage recipients in window differ from recipients from conv, and working recipients ", getActivity());
                }
            }
            sanityCheckConversation();
            removeRecipientsListeners();
            if (this.mRcsComposeMessage != null && this.mRcsComposeMessage.sendMessage(this.mDebugRecipients)) {
                if (MLog.isLoggable("Mms_app", 2)) {
                    log("HwCustComposeMessage sendMessage true. mForwardMessageMode:" + this.mForwardMessageMode + " hasSubject:" + this.mRichEditor.hasSubject());
                }
                if (this.mForwardMessageMode && this.mRichEditor.hasSubject()) {
                    MLog.d("Mms_UI_CMA", "RCS sendMessage is forward Mms, skipped");
                    return;
                }
            } else if (!this.mRichEditor.requiresMms() || MessageUtils.isNetworkAvailable(subscription)) {
                boolean restrictedCheckResult = false;
                if (MmsConfig.getCreationModeEnabled() && !MmsConfig.getCurrentCreationMode().equals("freemodemode")) {
                    restrictedCheckResult = checkRestrictedSize(subscription);
                    if (!restrictedCheckResult) {
                        MLog.w("Mms_UI_CMA", "restrictedCheckResult is false !");
                        return;
                    }
                }
                this.mRichEditor.sendMessage(this.mDebugRecipients, subscription);
                if (restrictedCheckResult) {
                    checkMessageSendMode();
                }
                this.mSentMessage = true;
                this.mSendingMessage = true;
                judgeAttachSmiley();
            } else {
                int textId = R.string.mobileDataDisabled_Toast;
                if (this.mHwCustComposeMessage != null && this.mHwCustComposeMessage.isShowToastonDataNotEnabled()) {
                    textId = this.mHwCustComposeMessage.getDataNotEnabledToastId(R.string.mobileDataDisabled_Toast);
                }
                Toast.makeText(getContext(), textId, 0).show();
                MLog.w("Mms_UI_CMA", "mobile data disabled !");
                return;
            }
            addRecipientsListeners();
            Intent intent = getIntent();
            if (!isFromWidget()) {
                if (MmsCommon.isFromNotification(intent)) {
                    StatisticalHelper.incrementReportCount(getContext(), 2040);
                } else if (this.mConversation.getMessageCount() == 0) {
                    StatisticalHelper.incrementReportCount(getContext(), 2037);
                } else {
                    StatisticalHelper.incrementReportCount(getContext(), 2038);
                }
            }
        }
        if (!MmsConfig.getCreationModeEnabled()) {
            checkMessageSendMode();
        } else if (MmsConfig.getCurrentCreationMode().equals("freemodemode")) {
            checkMessageSendMode();
        }
    }

    private void checkMessageSendMode() {
        if (this.mForwardMessageMode) {
            Intent intent = new Intent();
            intent.putExtra("forward_thread_id", this.mConversation.ensureThreadId());
            if (getController() == null || (HwMessageUtils.isSplitOn() && (getActivity() instanceof ConversationList))) {
                finishWhenSplit(intent);
                return;
            }
            getController().setResult(this, -1, intent);
            finish();
        } else if (this.mSendDiscreetMode) {
            MessageUtils.goToLauncherHome(getActivity());
            finish();
        }
    }

    private void finishWhenSplit(Intent intent) {
        Activity activity = getActivity();
        if (activity instanceof ConversationList) {
            ((ConversationList) activity).setSplitResultData(115, -1, intent);
            ((ConversationList) activity).hideLeftCover();
            hideKeyboard();
            if (((ConversationList) activity).isRightPaneOnTop()) {
                ((ConversationList) activity).backToListWhenSplit();
            }
        }
    }

    private String getBody(Uri uri) {
        if (uri == null) {
            return null;
        }
        String urlStr = uri.getSchemeSpecificPart();
        if (urlStr == null || !urlStr.contains("?")) {
            MLog.e("Mms_UI_CMA", "getBody(Uri uri) get error uri");
            return null;
        }
        String[] params = urlStr.substring(urlStr.indexOf(63) + 1).split("&");
        int i = 0;
        int length = params.length;
        while (i < length) {
            String p = params[i];
            if (p.startsWith("body=")) {
                try {
                    return URLDecoder.decode(p.substring(5), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                }
            } else {
                i++;
            }
        }
        return null;
    }

    public static long getAvailableSpace() {
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().toString());
            return ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean isForwardLock(Uri uri) {
        boolean isForwardLock = false;
        if (uri == null || Uri.EMPTY == uri) {
            return false;
        }
        try {
            Class<?> mClsDrmManagerClient = Class.forName("android.drm.DrmManagerClient");
            Object mDrmManagerClient = mClsDrmManagerClient.getConstructor(new Class[]{Context.class}).newInstance(new Object[]{getContext()});
            Method mMethodCanHandle = mClsDrmManagerClient.getMethod("canHandle", new Class[]{Uri.class, String.class});
            Method mMethodGetDrmObjectType = mClsDrmManagerClient.getMethod("getDrmObjectType", new Class[]{Uri.class, String.class});
            boolean mCanHandle = ((Boolean) mMethodCanHandle.invoke(mDrmManagerClient, new Object[]{uri, null})).booleanValue();
            if (HAS_DRM_CONFIG && mCanHandle) {
                MLog.d("Mms_UI_CMA", "HAS_DRM_CONFIG and can Handle,Method isForwardLock is invoked!");
                isForwardLock = ((Integer) mMethodGetDrmObjectType.invoke(mDrmManagerClient, new Object[]{uri, null})).intValue() != Integer.valueOf(Class.forName("com.huawei.android.drm.DrmStoreEx$DrmObjectType").getField("DRM_SEPARATE_DELIVERY").getInt(null)).intValue();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            MLog.e("Mms_UI_CMA", "exception occured .." + e.getMessage());
        } catch (InstantiationException e2) {
            e2.printStackTrace();
            MLog.e("Mms_UI_CMA", "exception occured .." + e2.getMessage());
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            MLog.e("Mms_UI_CMA", "exception occured .." + e3.getMessage());
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
            MLog.e("Mms_UI_CMA", "exception occured .." + e4.getMessage());
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
            MLog.e("Mms_UI_CMA", "exception occured .." + e5.getMessage());
        } catch (ClassNotFoundException e6) {
            e6.printStackTrace();
            MLog.e("Mms_UI_CMA", "exception occured .." + e6.getMessage());
        } catch (NoSuchFieldException e7) {
            e7.printStackTrace();
            MLog.e("Mms_UI_CMA", "exception occured .." + e7.getMessage());
        } catch (InstantiationException e8) {
            e8.printStackTrace();
            MLog.e("Mms_UI_CMA", "exception occured .." + e8.getMessage());
        }
        MLog.d("Mms_UI_CMA", "isForwardLock = " + isForwardLock);
        return isForwardLock;
    }

    public void showVcardMmsTypeDialog(final Uri uri) {
        if (this.mCryptoCompose.isSmsEncryptionSwitchOn()) {
            this.mCryptoCompose.showVcardMmsTypeDialog(this.mRichEditor, uri);
            MLog.d("Mms_UI_CMA", "showVcardMmsTypeDialog crypto message could not suport mms vcard.");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.vcard_contact);
        builder.setItems(R.array.mms_vcard_menu, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        ComposeMessageFragment.this.mRichEditor.insertVcardText(uri);
                        ComposeMessageFragment.this.mComposeRecipientsView.showInvalidDestinationToast();
                        break;
                    case 1:
                        ComposeMessageFragment.this.mRichEditor.setNewAttachment(ComposeMessageFragment.this.getActivity(), uri, 6, false);
                        ComposeMessageFragment.this.mComposeRecipientsView.showInvalidDestinationToast();
                        break;
                }
                dialog.dismiss();
                ComposeMessageFragment.this.mRichEditor.requestFocus();
                ComposeMessageFragment.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        ComposeMessageFragment.this.showKeyboard();
                    }
                }, 100);
            }
        });
        builder.show();
    }

    private void updateThreadIdIfRunning() {
        if (this.mIsRunning && this.mConversation != null) {
            if (MessagingNotification.getRcsMessagingNotification() == null || !MessagingNotification.getRcsMessagingNotification().isRcsSwitchOn()) {
                MessagingNotification.setCurrentlyDisplayedThreadId(this.mConversation.getThreadId());
            } else {
                MessagingNotification.getRcsMessagingNotification().setCurrentlyDisplayedThreadId(this.mConversation.getThreadId(), (long) (this.mConversation.getHwCust() == null ? 0 : this.mConversation.getHwCust().getRcsThreadType()));
            }
        }
    }

    private long getThreadId(Uri uri) {
        String[] projection;
        Uri provider;
        Long thId = Long.valueOf(0);
        String id = (String) uri.getPathSegments().get(0);
        Cursor cursor = null;
        if ("sms".equals(uri.getAuthority())) {
            projection = new String[]{"_id", "thread_id"};
            provider = Sms.CONTENT_URI;
        } else {
            projection = new String[]{"_id", "thread_id"};
            provider = Mms.CONTENT_URI;
        }
        try {
            cursor = SqliteWrapper.query(getContext(), getContext().getContentResolver(), provider, projection, " _id = ? ", new String[]{id}, null);
            if (cursor.moveToFirst()) {
                thId = Long.valueOf(cursor.getLong(1));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            MLog.e("Mms_UI_CMA", e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return thId.longValue();
    }

    public AsyncDialog getAsyncDialog() {
        if (this.mAsyncDialog == null) {
            this.mAsyncDialog = new AsyncDialog(getActivity());
        }
        return this.mAsyncDialog;
    }

    private void judgeAttachSmiley() {
        this.mComposeChoosePanel.hidePanel();
    }

    public boolean onBackPressed() {
        MLog.d("Mms_UI_CMA", "in CMF, onBackPressed runs");
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            this.mActionMode = null;
            return true;
        } else if (this.mHwCustComposeMessage != null && this.mHwCustComposeMessage.getSearchMode()) {
            this.mHwCustComposeMessage.updateSearchMode();
            return true;
        } else if (this.mMsgListView.isInEditMode()) {
            this.mMsgListView.exitEditMode();
            return true;
        } else if (this.mSmartSmsComponse != null && this.mSmartSmsComponse.isShowMenu()) {
            return true;
        } else {
            if (!this.mCryptoCompose.isSmsEncryptionSwitchOn() || this.mCryptoCompose.isSmsRelateSignature()) {
                return false;
            }
            this.mCryptoCompose.onBackPressed();
            MLog.d("Mms_UI_CMA", "onBackPressed crypto message could not save draft.");
            return true;
        }
    }

    public boolean needHidePanel() {
        if (getActivity() == null || this.mComposeChoosePanel.isShowAttachment() || this.mComposeChoosePanel.isShowSmileyFace()) {
            return true;
        }
        return super.needHidePanel();
    }

    private boolean isFromWidget() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getBooleanExtra("fromWidget", false);
        }
        return false;
    }

    private void finish() {
        hideKeyboard();
        finishSelf(false);
    }

    private void onCached(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            int newSelectionPos = -1;
            long targetMsgId = getIntent().getLongExtra("select_id", -1);
            int witchTable = getIntent().getIntExtra("table_to_use", 0);
            String usedTable;
            if (witchTable == 1) {
                usedTable = "sms";
            } else if (witchTable == 2) {
                usedTable = "mms";
            } else {
                usedTable = "chat";
            }
            if (this.mActivityHasFocused) {
                blockMarkAsRead(false);
            }
            if (targetMsgId != -1) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    long msgId = cursor.getLong(1);
                    String type = cursor.getString(0);
                    if (msgId == targetMsgId && usedTable.equals(type)) {
                        newSelectionPos = cursor.getPosition();
                        break;
                    }
                }
            }
            newSelectionPos = getSelectionPosForCallLog(cursor, newSelectionPos);
            this.mMsgListAdapter.clearCachedListeItemTimes();
            this.mMsgListAdapter.changeCursor(cursor);
            if (newSelectionPos != -1) {
                this.mMsgListView.setSelection(newSelectionPos);
            } else {
                long lastMsgId = 0;
                if (this.mMsgListAdapter.getCount() > 0) {
                    cursor.moveToLast();
                    lastMsgId = cursor.getLong(1);
                }
                if (lastMsgId != this.mLastMessageId) {
                    this.mMsgListView.setAutoScrool(true);
                    smoothScrollToEnd(true, 0);
                    this.mLastMessageId = lastMsgId;
                }
            }
            this.mConversation.setMessageCount(this.mMsgListAdapter.getCount());
            if (cursor.getCount() == 0 && !this.mComposeRecipientsView.isVisible() && !this.mSentMessage && !this.mIsQueryAfterDelete && !this.isPeeking && isShowRecipientsWhenSplit() && this.mActionBarWhenSplit.getActionMode() != 4) {
                this.mComposeRecipientsView.initRecipientsEditor();
            } else if (MmsConfig.isSupportPrivacy() && this.mComposeRecipientsView.isVisible() && cursor.getCount() > 0) {
                this.mComposeRecipientsView.hideRecipientEditor();
            } else if (this.mRcsComposeMessage != null && this.mRcsComposeMessage.isRcsSwitchOn() && this.mComposeRecipientsView.isVisible() && cursor.getCount() > 0) {
                this.mComposeRecipientsView.hideRecipientEditor();
            }
            if (this.mMsgListView.isInEditMode()) {
                this.mMsgListView.onDataReload();
            }
            getActivity().invalidateOptionsMenu();
        }
    }

    private boolean isTooManyRecipients() {
        int recipientLimit = MmsConfig.getRecipientLimit();
        if (MmsConfig.getCustMmsConfig() != null) {
            recipientLimit = MmsConfig.getCustMmsConfig().getCustRecipientLimit(getWorkingMessage().requiresMms(), recipientLimit);
        }
        return getRecipientCount() > recipientLimit;
    }

    private void registerPriavcyMonitor() {
        if (MmsConfig.isSupportPrivacy()) {
            this.localPrivacyMonitor = new ModeChangeListener() {
                public void onModeChange(Context context, boolean isInPrivacy) {
                    if (ComposeMessageFragment.this.mConversation.isPrivacyConversation(context)) {
                        if (isInPrivacy) {
                            ComposeMessageFragment.this.getActivity().recreate();
                            ComposeMessageFragment.this.initialize(null, ComposeMessageFragment.this.mConversation.getThreadId());
                            return;
                        }
                        ComposeMessageFragment.this.mHandler.post(new Runnable() {
                            public void run() {
                                ComposeMessageFragment.this.finish();
                            }
                        });
                    }
                }
            };
            PrivacyStateListener.self().register(this.localPrivacyMonitor);
        }
    }

    private void unRegisterPriavcyMonitor() {
        if (MmsConfig.isSupportPrivacy() && this.localPrivacyMonitor != null) {
            PrivacyStateListener.self().unRegister(this.localPrivacyMonitor);
        }
    }

    private void sendMessage(CharSequence body, int subscription) {
        if (this.mRichEditor.requiresMms() || !TextUtils.isEmpty(body)) {
            sendMessage(true, subscription);
            return;
        }
        MLog.d("Mms_UI_CMA", "sendMessage : empty msg !");
        if (MmsConfig.getSendingBlankSMSEnabled()) {
            alertForSendEmptySms(subscription);
        }
    }

    private void alertForSendEmptySms(int subscription) {
        new AlertDialog.Builder(getContext()).setIcon(17301543).setTitle(R.string.confirm_empty_msg_dialog_title).setCancelable(true).setMessage(R.string.confirm_empty_msg_dialog_body).setPositiveButton(R.string.yes, new SendIgnoreInvalidRecipientListener(subscription)).setNegativeButton(R.string.no, CommonLisener.getDismissClickListener()).show();
    }

    private void showDeliveryReportForSingleMessage(long aMessageId, String aType, long aUID, boolean isMultiRecipients) {
        Intent intent = new Intent(getContext(), DeliveryReportActivity.class);
        intent.putExtra("message_id", aMessageId);
        intent.putExtra("message_type", aType);
        intent.putExtra("group_id", aUID);
        intent.putExtra("is_multi_recipients", isMultiRecipients);
        startActivity(intent);
    }

    private void showCopyToSimResult(int subId, boolean isSuccess) {
        String str;
        NumberFormat.getIntegerInstance().setGroupingUsed(false);
        if (isSuccess) {
            StatisticalHelper.incrementReportCount(getContext(), 2064);
        }
        switch (subId) {
            case 0:
                if (!isSuccess) {
                    str = getString(R.string.copy_to_sim_failed_Toast, new Object[]{""});
                    break;
                }
                str = getString(R.string.copy_to_sim_successed_Toast, new Object[]{""});
                break;
            case 1:
                if (!isSuccess) {
                    str = getString(R.string.copy_to_card_failed, new Object[]{" " + format.format(1)});
                    break;
                }
                str = getString(R.string.copy_to_card_successed, new Object[]{" " + format.format(1)});
                break;
            case 2:
                if (!isSuccess) {
                    str = getString(R.string.copy_to_card_failed, new Object[]{" " + format.format(2)});
                    break;
                }
                str = getString(R.string.copy_to_card_successed, new Object[]{" " + format.format(2)});
                break;
            default:
                MLog.e("Mms_UI_CMA", "Unknown sub id.");
                return;
        }
        Toast.makeText(getContext(), str, 0).show();
    }

    public static void startCopyMessageToSim(AsyncQueryHandler queryHandler, int subId, ContentValues value) {
        Uri simUri;
        if (subId == 0) {
            simUri = COPY_TO_SIM_URI;
        } else if (1 == subId) {
            simUri = COPY_TO_SIM1_URI;
        } else {
            simUri = COPY_TO_SIM2_URI;
        }
        queryHandler.startInsert(9800, Integer.valueOf(subId), simUri, value);
    }

    private ContentValues getSmsMsgItemValue(MessageItem msgItem) {
        ContactList contactList = getRecipients();
        MLog.d("Mms_UI_CMA", "getSmsMsgItemValue count : " + contactList.size());
        if (1 == contactList.size()) {
            return getSmsValueByMsgItem(msgItem);
        }
        return getFirstRecipientsMsg(msgItem.mUid, msgItem);
    }

    public static ContentValues getSmsValueByMsgItem(MessageItem msgItem) {
        int i;
        ContentValues smsValues = new ContentValues(4);
        smsValues.put("address", msgItem.mAddress);
        smsValues.put("body", msgItem.mBody);
        smsValues.put("timestamp", Long.valueOf(msgItem.mDate));
        String str = "status";
        if (msgItem.isInComingMessage()) {
            i = 1;
        } else {
            i = 5;
        }
        smsValues.put(str, Integer.valueOf(i));
        return smsValues;
    }

    public long getFirstRecipientMsgId(long groupId, long id) {
        ContactList contactList = getRecipients();
        MLog.d("Mms_UI_CMA", "recipients count : " + contactList.size());
        long j = true;
        if (j == contactList.size()) {
            return id;
        }
        Cursor cursor = getFirstRecipientsCursor(groupId, ((Contact) contactList.get(0)).getNumber());
        if (cursor == null) {
            return id;
        }
        try {
            j = cursor.getLong(0);
            return j;
        } catch (Exception e) {
            j = "getFirstRecipientMsgId has an error >>> " + e;
            MLog.e("Mms_UI_CMA", j);
            return id;
        } finally {
            cursor.close();
        }
    }

    private ContentValues getFirstRecipientsMsg(long groupId, MessageItem msgItem) {
        String firstRecipient = ((Contact) getRecipients().get(0)).getNumber();
        Cursor cursor = getFirstRecipientsCursor(groupId, firstRecipient);
        if (cursor == null) {
            return getSmsValueByMsgItem(msgItem);
        }
        try {
            ContentValues smsValues = new ContentValues(4);
            smsValues.put("address", firstRecipient);
            smsValues.put("body", cursor.getString(2));
            long date = cursor.getLong(3);
            if (MessageUtils.IS_CHINA_TELECOM_OPTA_OPTB) {
                long date_sent = cursor.getLong(4);
                if (!(date_sent == 0 || date_sent == 1)) {
                    date = date_sent;
                }
            }
            smsValues.put("timestamp", Long.valueOf(date));
            smsValues.put("status", Integer.valueOf(5));
            return smsValues;
        } catch (Exception e) {
            MLog.e("Mms_UI_CMA", "getFirstRecipientsMsg has an error >>> " + e);
            return getSmsValueByMsgItem(msgItem);
        } finally {
            cursor.close();
        }
    }

    private Cursor getFirstRecipientsCursor(long groupId, String firstRecipient) {
        if (1 == getRecipients().size()) {
            MLog.d("Mms_UI_CMA", "getFirstRecipientsMsg single person");
            return null;
        }
        StringBuilder lSelection = new StringBuilder("group_id");
        lSelection.append(" = ?");
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(getContext(), Sms.CONTENT_URI, new String[]{"_id", "address", "body", "date", "date_sent"}, lSelection.toString(), new String[]{String.valueOf(groupId)}, null);
            if (cursor != null && cursor.getCount() > 0) {
                Cursor matchCursor = HwNumberMatchUtils.getMatchedCursor(cursor, firstRecipient);
                if (matchCursor != null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return matchCursor;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            MLog.e("Mms_UI_CMA", " get first recipient sms cursor exception :" + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private boolean isNeedShowSendMmsPopup() {
        boolean bIsSms = !this.mRichEditor.requiresMms();
        if (MmsConfig.isShowCheckEmailPoup() && bIsSms && this.mComposeRecipientsView.containsEmail()) {
            return true;
        }
        return false;
    }

    private void alertForSendMms() {
        if (this.mNeedPopupEmailCheck && !this.mEmailCheckIsShowing && !this.mSendingMessage && (MmsConfig.getCustMmsConfig() == null || MmsConfig.getCustMmsConfig().isNotifyMsgtypeChangeEnable(true))) {
            this.mNeedPopupEmailCheck = false;
            this.mEmailCheckIsShowing = true;
            View contents = View.inflate(getContext(), R.layout.send_mms_waring_dialog_view, null);
            String title = getString(R.string.add_attchment_failed_replace_title);
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).setIconAttribute(16843605).setCancelable(true).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ComposeMessageFragment.this.mRichEditor.setHasEmail(ComposeMessageFragment.this.mComposeRecipientsView.containsEmail(), true);
                    Toast.makeText(ComposeMessageFragment.this.getContext(), R.string.converting_to_picture_message_Toast, 0).show();
                    ComposeMessageFragment.this.mEmailCheckIsShowing = false;
                    dialog.dismiss();
                }
            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ComposeMessageFragment.this.mComposeRecipientsView.removeEmailAddress();
                    ComposeMessageFragment.this.mRichEditor.setHasEmail(ComposeMessageFragment.this.mComposeRecipientsView.containsEmail(), true);
                    ComposeMessageFragment.this.mComposeRecipientsView.requestFocus();
                    ComposeMessageFragment.this.mEmailCheckIsShowing = false;
                    dialog.dismiss();
                }
            }).setMessage(R.string.send_email_mms_popup_warning).setView(contents).create();
            alertDialog.setTitle(title);
            alertDialog.show();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    ComposeMessageFragment.this.mEmailCheckIsShowing = false;
                }
            });
        }
    }

    private void addRecipietsToContact() {
        ContactList contactList = this.mConversation.getRecipients();
        if (contactList.size() == 1) {
            this.mAddContactIntent = MessageUtils.getShowOrCreateContactIntent(((Contact) contactList.get(0)).getNumber());
            if (this.mAddContactIntent != null) {
                startActivityForResult(this.mAddContactIntent, 108);
            }
        }
    }

    public void showPopupFloatingToolbar() {
        this.mActionMode = this.mShowPopupFloatingToolbarView.startActionMode(new FloatingCallback2(), 1);
        this.mActionMode.hide(0);
    }

    public void registerDefSmsAppChanged() {
        if (this.mDefSmsAppChangedReceiver == null) {
            this.mDefSmsAppChangedReceiver = new DefaultSmsAppChangedReceiver(new HwDefSmsAppChangedListener() {
                public void onDefSmsAppChanged() {
                    ComposeMessageFragment.this.checkSmsEnable();
                }
            });
        }
        getContext().registerReceiver(this.mDefSmsAppChangedReceiver, new IntentFilter("com.huawei.mms.default_smsapp_changed"), permission.DEFAULTCHANGED_PERMISSION, null);
    }

    private void unRegisterDefSmsAppChanged() {
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
            updateSendButtonView();
            updateAddAttachView();
        }
    }

    private void initFullScreenView() {
        this.mFullScreenEdit = (ImageButton) findViewById(R.id.btn_full_screen);
        this.mFullScreenEdit.setBackground(ResEx.self().getStateListDrawable(getContext(), R.drawable.full_screen_edit_selector));
        if (this.mIsSmsEnabled) {
            this.mFullScreenEdit.setClickable(true);
            this.mFullScreenEdit.setEnabled(true);
            this.mFullScreenEdit.setOnClickListener(this);
            return;
        }
        this.mFullScreenEdit.setClickable(false);
        this.mFullScreenEdit.setEnabled(false);
    }

    private void updateEmojiAddView() {
        this.mEmojiAdd = (ImageButton) findViewById(R.id.add_emojis);
        if (this.mComposeChoosePanel == null || !this.mComposeChoosePanel.isShowSmileyFace()) {
            this.mEmojiAdd.setBackground(ResEx.self().getStateListDrawable(getContext(), R.drawable.ic_enter_emoji_expression));
        } else {
            this.mEmojiAdd.setBackground(ResEx.self().getStateListDrawable(getContext(), R.drawable.ic_enter_emoji_expression_checked));
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

    public void updateAddAttachView() {
        View vAttach = findViewById(R.id.add_attach);
        vAttach.setBackground(ResEx.self().getStateListDrawable(getContext(), R.drawable.btn_accessories));
        vAttach.getBackground().setAlpha(MessageUtils.getImageDisplyAlpha(this.mIsSmsEnabled));
        if (this.mIsSmsEnabled) {
            vAttach.setClickable(true);
            vAttach.setEnabled(true);
            vAttach.setOnClickListener(this);
        } else {
            vAttach.setClickable(false);
            vAttach.setEnabled(false);
        }
        if (this.mHwCustComposeMessage != null) {
            this.mHwCustComposeMessage.setVattachInvisible(vAttach);
        }
    }

    private void checkSuperPowerSaveMode() {
        if (this.mIsSmsEnabled) {
            boolean isSuperPowerSaveMode = HwMessageUtils.isSuperPowerSaveModeOn();
            if (this.mIsSuperPowerSaveMode != isSuperPowerSaveMode) {
                int i;
                this.mIsSuperPowerSaveMode = isSuperPowerSaveMode;
                Drawable background = findViewById(R.id.add_attach).getBackground();
                if (isSuperPowerSaveMode) {
                    i = 85;
                } else {
                    i = 255;
                }
                background.setAlpha(i);
            }
        }
    }

    private void showVcalendarDlgFromCalendar(final Uri vcsUri, final ArrayList<Uri> uriList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.save_message_to_calendar);
        builder.setItems(R.array.vcal_menu, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (uriList != null) {
                            ComposeMessageFragment.this.mRichEditor.insertVcalendarText(uriList);
                            return;
                        }
                        return;
                    case 1:
                        ComposeMessageFragment.this.mRichEditor.setNewAttachment(vcsUri, 7, false);
                        return;
                    default:
                        return;
                }
            }
        });
        builder.show();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        this.mActivityHasFocused = hasFocus;
        if (this.mActivityHasFocused) {
            blockMarkAsRead(false);
        }
        MLog.d("Mms_UI_CMA", "onWindowFocusChanged mActivityFocus: " + this.mActivityHasFocused);
    }

    private void postRecommendResult(final int subid) {
        this.mRichEditor.setNewMessageDraftSubid(subid);
        this.mHandler.post(new Runnable() {
            public void run() {
                ComposeMessageFragment.this.mComposeBottomView.setRecommendMessageButton(subid);
            }
        });
    }

    public void blockMarkAsRead(boolean block) {
        if (!canBeMarkedAsRead()) {
            return;
        }
        if (this.mIsContentChanged || !this.isPeeking) {
            this.mConversation.blockMarkAsRead(block);
        }
    }

    private void markAsRead() {
        if (!canBeMarkedAsRead()) {
            return;
        }
        if (this.mIsContentChanged || !this.isPeeking) {
            this.mConversation.markAsRead();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean canBeMarkedAsRead() {
        if (this.mConversation == null || this.mConversation.getThreadId() == 0 || this.mComposeRecipientsView.isVisible()) {
            return false;
        }
        return true;
    }

    public void rcsMarkAsRead() {
        this.mConversation.markAsRead();
    }

    private void registerVowifiReceiver() {
        if (this.mVoWifiReceiver == null) {
            this.mVoWifiReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if ("huawei.intent.action.IMS_SERVICE_VOWIFI_STATE_CHANGED".equals(intent.getAction())) {
                        boolean readyForSend = ComposeMessageFragment.this.isPreparedForSending();
                        if (readyForSend) {
                            ComposeMessageFragment.this.mComposeBottomView.updateSendButtonView(true, readyForSend);
                        }
                    }
                }
            };
        }
        IntentFilter voWifiFilter = new IntentFilter();
        voWifiFilter.addAction("huawei.intent.action.IMS_SERVICE_VOWIFI_STATE_CHANGED");
        getActivity().registerReceiver(this.mVoWifiReceiver, voWifiFilter, "com.huawei.ims.permission.GET_IMS_SERVICE_VOWIFI_STATE", null);
    }

    private void updateSendButtonView() {
        MLog.w("Mms_UI_CMA", "update sendbutton view");
        if (isBottomViewVisible()) {
            boolean readyForSend = isPreparedForSending();
            if (readyForSend) {
                this.mComposeBottomView.updateSendButtonView(true, readyForSend);
                if (this.mRichEditor.requiresMms()) {
                    this.mComposeBottomView.updateMmsCapacitySize(this.mRichEditor.getMmsSizeRate());
                } else {
                    this.mComposeBottomView.updateSmsTextCount(this.mRichEditor.getTextCount());
                }
            } else {
                this.mComposeBottomView.updateSendButtonView(false, readyForSend);
                this.mFullScreenButtonState = 118;
            }
            this.mCryptoCompose.updateSendButtonState();
        }
    }

    private boolean isPreparedForSending() {
        return (!this.mIsSmsEnabled || getRecipientCount() <= 0) ? false : this.mRichEditor.hasContentToSend();
    }

    private void initDirtyModel() {
        if (this.mRichEditor.getText() != null) {
            this.mDirtyModel.setDirtyText(this.mRichEditor.getText().toString());
        }
        if (this.mRichEditor.getSubjectText() != null) {
            this.mDirtyModel.setDirtySub(this.mRichEditor.getSubjectText().toString());
        }
        this.mDirtyModel.setDirtyUri(this.mRichEditor.getWorkingMessage().getMessageUri());
        this.mDirtyModel.setWorkingRecipients(this.mRichEditor.getWorkingRecipients());
        this.mDirtyModel.setSlideSize(this.mRichEditor.getSlideSize());
    }

    private String getDirtyRecipients() {
        String recipients = this.mRichEditor.getWorkingRecipients();
        if (!TextUtils.isEmpty(recipients) || this.mComposeRecipientsView == null) {
            return recipients;
        }
        return ContactList.getByNumbers(this.mComposeRecipientsView.getNumbers(), false).serialize();
    }

    private boolean isDirty() {
        if (this.mRichEditor.isRMESlideDirty()) {
            return true;
        }
        Object txt = null;
        if (this.mRichEditor.getText() != null) {
            txt = this.mRichEditor.getText().toString();
        }
        Object subject = null;
        if (this.mRichEditor.getSubjectText() != null) {
            subject = this.mRichEditor.getSubjectText().toString();
        }
        String recipients = getDirtyRecipients();
        int modelSize = this.mRichEditor.getSlideSize();
        Uri msgUri = this.mRichEditor.getWorkingMessage().getMessageUri();
        String dirtyTxt = this.mDirtyModel.getDirtyText();
        String dirtySubject = this.mDirtyModel.getDirtySub();
        String dirtyRecipients = this.mDirtyModel.getWorkingRecipients();
        int dirtyModelSize = this.mDirtyModel.getSlideSize();
        Uri dirtyMsgUri = this.mDirtyModel.getDirtyUri();
        if (((!TextUtils.isEmpty(txt) || !TextUtils.isEmpty(dirtyTxt)) && (TextUtils.isEmpty(txt) || !txt.equals(dirtyTxt))) || (((!TextUtils.isEmpty(r8) || !TextUtils.isEmpty(dirtySubject)) && (TextUtils.isEmpty(r8) || !r8.equals(dirtySubject))) || (((recipients == null || !recipients.equals(dirtyRecipients)) && (!TextUtils.isEmpty(recipients) || !TextUtils.isEmpty(dirtyRecipients))) || modelSize != dirtyModelSize || ((msgUri == null || !msgUri.equals(dirtyMsgUri)) && (msgUri != null || dirtyMsgUri != null))))) {
            return true;
        }
        return false;
    }

    private void showEnableFullScreenIcon() {
        int i;
        HandlerEx handlerEx = this.mHandler;
        Runnable anonymousClass43 = new Runnable() {
            public void run() {
                if (ComposeMessageFragment.this.mComposeBottomView.isMultiCardsValid() && ComposeMessageFragment.this.mIsLandscape && ComposeMessageFragment.this.mComposeChoosePanel.isVisible() && ComposeMessageFragment.this.mComposeRecipientsView.isVisible()) {
                    ComposeMessageFragment.this.hideFullScreenButton();
                    return;
                }
                if (!ComposeMessageFragment.this.mRichEditor.requiresMms() && ComposeMessageFragment.this.mRichEditor.getLineNumber() > 2) {
                    ComposeMessageFragment.this.showFullScreenButton();
                } else if ((!ComposeMessageFragment.this.mRichEditor.requiresMms() || RcseMmsExt.isRcsMode()) && ComposeMessageFragment.this.mRichEditor.getLineNumber() == 2 && ComposeMessageFragment.this.isFirstCheck) {
                    ComposeMessageFragment.this.isFirstCheck = false;
                    ComposeMessageFragment.this.showEnableFullScreenIcon();
                } else {
                    ComposeMessageFragment.this.hideFullScreenButton();
                }
            }
        };
        if (isInMultiWindowMode()) {
            i = SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE;
        } else {
            i = 5;
        }
        handlerEx.postDelayed(anonymousClass43, (long) i);
    }

    private void showFullScreenButton() {
        this.mFullScreenEdit.setVisibility(0);
    }

    private void hideFullScreenButton() {
        this.mFullScreenEdit.setVisibility(8);
    }

    private void initSmartSmsMenu() {
        this.mMmMessageEditLayout = (LinearLayout) findViewById(R.id.mms_message_edit_layout);
        this.mSmartSmsComponse = new SmartSmsComposeManager(this);
    }

    public boolean equalMsgNumber(String phoneNum) {
        if (getRecipients() == null || getRecipients().size() != 1) {
            return false;
        }
        return HwNumberMatchUtils.isNumbersMatched(phoneNum, ((Contact) getRecipients().get(0)).getNumber());
    }

    public void setReplySmsBody(String smsBody) {
        this.mRichEditor.setText(smsBody);
        if (this.mMmMessageEditLayout != null && this.mMmMessageEditLayout.getVisibility() != 0) {
            this.mMmMessageEditLayout.setVisibility(0);
            this.mSmartSmsComponse.hideSmartsmsMenu();
        }
    }

    private void showMessageEditLayoutAndHiddenSmartSmsMenu() {
        if (this.mSmartSmsComponse != null && this.mMmMessageEditLayout != null) {
            this.mMmMessageEditLayout.setVisibility(0);
            this.mSmartSmsComponse.hideSmartsmsMenu();
        }
    }

    public boolean editorHasContent() {
        return this.mRichEditor != null ? this.mRichEditor.hasContentToSend() : false;
    }

    public boolean editorHasText() {
        return this.mRichEditor != null ? this.mRichEditor.hasText() : false;
    }

    public Activity getActivityContext() {
        return getActivity();
    }

    public View findViewById(int resId) {
        return getActivity() == null ? null : getActivity().findViewById(resId);
    }

    public boolean onSmartSmsEvent(short eventType) {
        Animation showAnim = AnimationUtils.loadAnimation(getContext(), R.anim.translate_fade_in);
        Animation fadeAnim = AnimationUtils.loadAnimation(getContext(), R.anim.translate);
        switch (eventType) {
            case (short) 1:
                if (this.mMmMessageEditLayout != null) {
                    this.mMmMessageEditLayout.setAnimation(showAnim);
                    this.mMmMessageEditLayout.setVisibility(0);
                }
                if (this.mRichEditor != null) {
                    this.mRichEditor.requestLayout();
                }
                if (this.mEditLayoutShowStatu == (short) 0) {
                    updateInputMode();
                }
                this.mSmartSmsComponse.setSmartMenuClickListener(this);
                break;
            case (short) 4:
                if (this.mMmMessageEditLayout != null) {
                    this.mMmMessageEditLayout.setAnimation(fadeAnim);
                    this.mMmMessageEditLayout.setVisibility(8);
                }
                this.mComposeChoosePanel.hidePanel();
                if (this.mConversationInputManager.isMediaPickerVisible()) {
                    this.mConversationInputManager.showHideMediaPicker(false, true);
                }
                hideKeyboard();
                break;
            case (short) 6:
                if (!MmsCommon.isFromPeekReply(getIntent())) {
                    if (this.mMmMessageEditLayout != null) {
                        this.mMmMessageEditLayout.setVisibility(8);
                    }
                    this.mComposeChoosePanel.hidePanel();
                    this.mConversationInputManager.showHideMediaPicker(false, true);
                    hideKeyboard();
                    break;
                }
                this.mSmartSmsComponse.toEditMenu(this, false);
                findViewById(R.id.duoqu_menu_layout_stub).setVisibility(8);
                return false;
            case (short) 7:
                if (this.mMmMessageEditLayout != null) {
                    this.mMmMessageEditLayout.setVisibility(0);
                }
                if (this.mRichEditor != null) {
                    this.mRichEditor.requestLayout();
                }
                if (this.mEditLayoutShowStatu == (short) 0) {
                    updateInputMode();
                }
                this.mSmartSmsComponse.setSmartMenuClickListener(this);
                break;
        }
        this.mEditLayoutShowStatu = eventType;
        return false;
    }

    public boolean isNotifyComposeMessage() {
        if (this.mSmartSmsComponse != null) {
            return this.mSmartSmsComponse.getIsNotifyComposeMessage();
        }
        return false;
    }

    public boolean isIntentHasSmsBody() {
        if (getIntent() == null || TextUtils.isEmpty(getIntent().getStringExtra("sms_body"))) {
            return false;
        }
        return true;
    }

    public void addNeedRefreshSmartBubbleItem(SmartSmsBubbleManager bubbleItem) {
        if (this.mSmartSmsComponse != null) {
            this.mSmartSmsComponse.addNeedRefreshSmartBubbleItem(bubbleItem);
        }
    }

    public void setFlingState(boolean isFling) {
        if (!isFling && this.mMsgListAdapter != null && this.mSmartSmsComponse != null) {
            this.mSmartSmsComponse.notifyCheckRefresh(this.mMsgListAdapter);
        }
    }

    public RcsComposeMessage getRcsComposeMessage() {
        return this.mRcsComposeMessage;
    }

    public void setSentMessage(boolean mSentMessage) {
        this.mSentMessage = mSentMessage;
    }

    public void setScrollOnSend(boolean mScrollOnSend) {
    }

    public Conversation getConversation() {
        return this.mConversation;
    }

    public boolean isRunning() {
        return this.mIsRunning;
    }

    public void setSendingMessage(boolean mSendingMessage) {
        this.mSendingMessage = mSendingMessage;
    }

    public boolean getIsAttachmentShow() {
        return !this.mComposeChoosePanel.isShowAttachment() ? this.mConversationInputManager.isMediaPickerVisible() : true;
    }

    public RichMessageEditor getRichEditor() {
        return this.mRichEditor;
    }

    public void setMultiUris(ArrayList<Parcelable> uris) {
        this.mMultiUris = uris;
    }

    public void setUriPostion(int position) {
        this.mUriPostion = position;
    }

    public void setMimeType(String type) {
        this.mMineType = type;
    }

    private void showSmsRecycleEnableDialog() {
        View contents = View.inflate(getContext(), R.layout.not_show_again_dialog_view, null);
        final CheckBox checkbox = (CheckBox) contents.findViewById(R.id.not_show_again);
        checkbox.setChecked(true);
        new AlertDialog.Builder(getContext()).setIcon(17301543).setTitle(R.string.sms_recovery_title).setCancelable(true).setMessage(R.string.enable_sms_recovery_notify_message).setPositiveButton(R.string.enable_sms_recovery_notify_open, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Editor editor = PreferenceManager.getDefaultSharedPreferences(ComposeMessageFragment.this.getContext()).edit();
                editor.putBoolean("pref_sms_recycle_not_show_again", checkbox.isChecked());
                editor.putBoolean("pref_key_recovery_support", true);
                editor.apply();
                new CallRequest(ComposeMessageFragment.this.getContext(), "method_enable_recovery") {
                    protected void setParam() {
                        this.mRequest.putBoolean("recovery_status", true);
                    }

                    protected void onCallBack() {
                        ComposeMessageFragment.this.mMsgListView.onMenuItemClick(278925315);
                    }
                }.makeCall();
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Editor editor = PreferenceManager.getDefaultSharedPreferences(ComposeMessageFragment.this.getContext()).edit();
                editor.putBoolean("pref_sms_recycle_not_show_again", checkbox.isChecked());
                editor.apply();
                ComposeMessageFragment.this.mMsgListView.onMenuItemClick(278925315);
            }
        }).setView(contents).show();
    }

    private boolean isBottomViewVisible() {
        boolean valid = true;
        if (this.mIsPushConversation) {
            return false;
        }
        if (this.mComposeRecipientsView.isVisible() || this.mConversation == null) {
            return true;
        }
        ContactList contacts = this.mConversation.getRecipients();
        if (contacts == null || contacts.size() != 1) {
            return true;
        }
        if (!MessageUtils.isValidMmsAddress(((Contact) contacts.get(0)).getNumber())) {
            valid = MessageUtils.isServerAddress(((Contact) contacts.get(0)).getNumber());
        }
        if (!valid) {
            MLog.e("Mms_UI_CMA", "Can't send message to thread: " + this.mConversation.getThreadId());
        }
        return valid;
    }

    public void callCurrentConversation() {
        if (this.mActionbarAdapter != null) {
            this.mActionbarAdapter.callRecipients();
        }
    }

    public void replyCurrentConversation(boolean showSoftInput) {
        Intent intent;
        if (this.mRcsComposeMessage == null || !this.mRcsComposeMessage.isRcsSwitchOn()) {
            long tId = this.mConversation == null ? 0 : this.mConversation.getThreadId();
            Context activity = getActivity();
            if (tId < 0) {
                tId = 0;
            }
            intent = ComposeMessageActivity.createIntent(activity, tId);
            if (this.mConversation != null && this.mConversation.getNumberType() == 1) {
                intent.putExtra("sender_for_huawei_message", this.mActionbarAdapter.getName());
                intent.putExtra("contact_id", ((MyConversationActionBarAdapter) this.mActionbarAdapter).getmMatchContactId());
            }
            intent.putExtra("EXTRA_VALUE_PEEK_REPLY", showSoftInput);
            getActivity().startActivity(intent);
            if (showSoftInput) {
                getActivity().overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
            } else {
                getActivity().overridePendingTransition(0, 0);
            }
        } else if (this.mConversation != null && this.mConversation.getHwCust() != null) {
            intent = RcsComposeMessage.createIntent(getContext(), this.mConversation.getThreadId(), this.mConversation.getHwCust().getRcsThreadType());
            intent.putExtra("EXTRA_VALUE_PEEK_REPLY", showSoftInput);
            getContext().startActivity(intent);
            if (showSoftInput) {
                getActivity().overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
            } else {
                getActivity().overridePendingTransition(0, 0);
            }
        }
    }

    private void setPeekActions() {
        if (this.mConversation != null && this.mActionbarAdapter != null && this.mActionbarAdapter.getName() != null && this.mActionbarAdapter.getNumber() != null) {
            int recipientsSize = this.mConversation.getRecipients().size();
            if (recipientsSize != 0) {
                if (recipientsSize > 1) {
                    getComposeMessageActivity().setPeekActionStatus(3);
                } else if (this.mActionbarAdapter.isExistsInContact()) {
                    getComposeMessageActivity().setPeekActionStatus(1);
                } else {
                    getComposeMessageActivity().setPeekActionStatus(2);
                }
            }
        }
    }

    public void onFragmentPop() {
        if (ActivityExWrapper.IS_PRESS_SUPPORT) {
            this.isPeeking = false;
        }
        this.mBottomView.setVisibility(0);
        if (!MmsConfig.getSupportSmartSmsFeature() || this.mSmartSmsComponse == null || getRecipients() == null || getRecipients().size() != 1) {
            this.mEditLayoutShowStatu = (short) 1;
        } else {
            this.mSmartSmsComponse.queryMenu(this, ((Contact) getRecipients().get(0)).getNumber(), (short) 2);
        }
        this.mMsgListView.setVerticalScrollBarEnabled(true);
        loadDraft();
    }

    public ComposeMessageActivity getComposeMessageActivity() {
        return (ComposeMessageActivity) getActivity();
    }

    public void exitPeekingStatus() {
        getComposeMessageActivity().onFragmentExit();
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

    protected void updateComposeTitle(int size) {
        if (getActivity() != null) {
            this.mActionBarWhenSplit.setUseSelecteSize(size);
        }
        this.mActionBarWhenSplit.setSubtitle(null);
    }

    protected void enterEditUpdate() {
        int i;
        boolean z;
        if (getActivity() instanceof ConversationList) {
            ((ConversationList) getActivity()).showOrHideLeftCover();
        }
        this.mMenuEx.onPrepareOptionsMenu();
        updateNormalMenu();
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
        if (this.mIsFromLauncher) {
            this.mActionBarWhenSplit.setStartIcon(true, (int) R.drawable.mms_ic_cancel_dark, new OnClickListener() {
                public void onClick(View v) {
                    ComposeMessageFragment.this.getActivity().onBackPressed();
                }
            });
        } else {
            this.mActionBarWhenSplit.setStartIcon(true, R.drawable.mms_ic_cancel_dark);
        }
        this.mAttachmentPreview.setVisibility(8);
    }

    protected void exitEditUpdate() {
        if (getActivity() instanceof ConversationList) {
            ((ConversationList) getActivity()).showOrHideLeftCover();
        }
        this.mSplitActionBar.setVisibility(8);
        updateNormalMenu();
        this.mActionBarWhenSplit.showMenu(true);
        this.mActionBarWhenSplit.setTitleGravityCenter(false);
        this.mActionBarWhenSplit.showEndIcon(true);
        this.mActionBarWhenSplit.showStartIcon(false);
        this.mActionBarWhenSplit.setStartIconDescription(getString(R.string.up_navigation));
        refreshAttachmentPreviewState();
    }

    protected void enterTitleModeUpdate() {
        if (this.mComposeRecipientsView.isVisible()) {
            this.mComposeRecipientsView.setVisibility(false);
            this.mActionBarWhenSplit.show(true);
        }
        this.mActionBarWhenSplit.enterTitleMode();
        this.mActionBarWhenSplit.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
            public void onClick(View arg0) {
                if (ComposeMessageFragment.this.mConversationInputManager != null && ComposeMessageFragment.this.mConversationInputManager.getMediaPickerFullScreenState()) {
                    ComposeMessageFragment.this.exitMediaPcikerFullScreen();
                }
                ComposeMessageFragment.this.exitTitleMode();
            }
        });
        this.mActionBarWhenSplit.showEndIcon(false);
        this.mActionBarWhenSplit.showMenu(false);
        this.mActionBarWhenSplit.setSubtitle(null);
        this.mActionBarWhenSplit.setTitleGravityCenter();
        if (this.mConversationInputManager != null) {
            this.mConversationInputManager.updateActionBar(this.mActionBarWhenSplit);
        }
    }

    protected void exitTitleMode() {
        this.mActionBarWhenSplit.exitTitleMode();
        switchToWhenSplitActionBar();
    }

    protected void enterGalleryFullUpdate() {
        if (this.mComposeRecipientsView.isVisible()) {
            this.mComposeRecipientsView.setVisibility(false);
            this.mActionBarWhenSplit.show(true);
        }
        this.mActionBarWhenSplit.enterSelectModeState();
        this.mActionBarWhenSplit.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
            public void onClick(View arg0) {
                if (ComposeMessageFragment.this.mConversationInputManager != null && ComposeMessageFragment.this.mConversationInputManager.getMediaPickerFullScreenState()) {
                    ComposeMessageFragment.this.exitMediaPcikerFullScreen();
                }
                ComposeMessageFragment.this.exitGalleryFullUpdate();
            }
        });
        this.mActionBarWhenSplit.setEndIcon(true, (int) R.drawable.ic_public_ok, new OnClickListener() {
            public void onClick(View arg0) {
                if (ComposeMessageFragment.this.mConversationInputManager != null && ComposeMessageFragment.this.mConversationInputManager.getMediaPickerFullScreenState()) {
                    ComposeMessageFragment.this.exitMediaPcikerFullScreen();
                }
                ComposeMessageFragment.this.exitGalleryFullUpdate();
            }
        });
        this.mActionBarWhenSplit.setEndIconDescription(getContext().getString(R.string.done));
        this.mActionBarWhenSplit.showMenu(false);
        this.mActionBarWhenSplit.setSubtitle(null);
        this.mActionBarWhenSplit.setTitleGravityCenter();
        if (this.mConversationInputManager != null) {
            this.mConversationInputManager.updateActionBar(this.mActionBarWhenSplit);
        }
    }

    private void exitMediaPcikerFullScreen() {
        if (getResources().getConfiguration().orientation == 2) {
            this.mConversationInputManager.showHideMediaPicker(false, true);
        } else {
            this.mConversationInputManager.showMediaPicker(false, true);
        }
    }

    protected void exitGalleryFullUpdate() {
        this.mActionBarWhenSplit.exitSelectMode();
        switchToWhenSplitActionBar();
    }

    private void switchToWhenSplitActionBar() {
        boolean isNewConversation = false;
        this.mSplitActionBar.setVisibility(8);
        updateNormalMenu();
        this.mActionBarWhenSplit.showMenu(true);
        this.mActionBarWhenSplit.setTitleGravityCenter(false);
        this.mActionBarWhenSplit.showEndIcon(false);
        this.mActionBarWhenSplit.showStartIcon(false);
        if (this.mConversation != null && this.mConversation.getMessageCount() == 0) {
            isNewConversation = true;
        }
        if (isNewConversation) {
            this.mComposeRecipientsView.setVisibility(true);
            showNewMessageTitle();
            return;
        }
        updateTitle(getRecipients());
    }

    protected void exitLocationFullUpdate() {
        boolean isNewConversation = false;
        this.mActionBarWhenSplit.exitSelectMode();
        this.mSplitActionBar.setVisibility(8);
        updateNormalMenu();
        this.mActionBarWhenSplit.showMenu(true);
        this.mActionBarWhenSplit.setTitleGravityCenter(false);
        this.mActionBarWhenSplit.showEndIcon(false);
        this.mActionBarWhenSplit.showStartIcon(false);
        RcsMapFragment rcsMapFragment = (RcsMapFragment) FragmentTag.getFragmentByTag(getActivity(), "MMS_UI_MAP");
        if (rcsMapFragment != null) {
            rcsMapFragment.setActionbar(null);
            setMapClickCallback(null);
        }
        if (this.mConversation != null && this.mConversation.getMessageCount() == 0) {
            isNewConversation = true;
        }
        if (isNewConversation) {
            this.mComposeRecipientsView.setVisibility(true);
            showNewMessageTitle();
            return;
        }
        updateTitle(getRecipients());
    }

    public void setMapClickCallback(MapClickCallback callback) {
        this.mapClickCallback = callback;
    }

    protected void enterLocationFullUpdate() {
        if (this.mComposeRecipientsView.isVisible()) {
            this.mComposeRecipientsView.setVisibility(false);
            this.mActionBarWhenSplit.show(true);
        }
        RcsMapFragment rcsMapFragment = (RcsMapFragment) FragmentTag.getFragmentByTag(getActivity(), "MMS_UI_MAP");
        if (rcsMapFragment != null) {
            rcsMapFragment.setActionbar(this.mActionBarWhenSplit);
            setMapClickCallback(rcsMapFragment);
        }
        this.mActionBarWhenSplit.enterSelectModeState();
        this.mActionBarWhenSplit.setStartIcon(true, (int) R.drawable.ic_public_cancel, new OnClickListener() {
            public void onClick(View arg0) {
                if (ComposeMessageFragment.this.mConversationInputManager != null && ComposeMessageFragment.this.mConversationInputManager.getMediaPickerFullScreenState()) {
                    ComposeMessageFragment.this.exitMediaPcikerFullScreen();
                }
                ComposeMessageFragment.this.exitLocationFullUpdate();
            }
        });
        this.mActionBarWhenSplit.setEndIcon(true, (int) R.drawable.ic_public_ok, new OnClickListener() {
            public void onClick(View arg0) {
                if (ComposeMessageFragment.this.mConversationInputManager != null && ComposeMessageFragment.this.mConversationInputManager.getMediaPickerFullScreenState()) {
                    if (ComposeMessageFragment.this.mapClickCallback != null) {
                        ComposeMessageFragment.this.mapClickCallback.okClick();
                    }
                    ComposeMessageFragment.this.exitMediaPcikerFullScreen();
                }
                ComposeMessageFragment.this.exitLocationFullUpdate();
            }
        });
        this.mActionBarWhenSplit.setEndIcon(false);
        this.mActionBarWhenSplit.showMenu(false);
        this.mActionBarWhenSplit.setSubtitle(null);
        this.mActionBarWhenSplit.setTitleGravityCenter();
        if (this.mConversationInputManager != null) {
            this.mConversationInputManager.updateActionBar(this.mActionBarWhenSplit);
        }
    }

    protected void updateInfoFromAdapter() {
        String title = this.mActionbarAdapter.getName();
        String title1 = this.mActionbarAdapter.getNumber();
        boolean isNeedSetLocation = false;
        if (TextUtils.isEmpty(title) || title.equals(title1)) {
            title = title1;
            isNeedSetLocation = true;
        }
        int size = this.mActionbarAdapter.getContactList().size();
        this.mActionBarWhenSplit.show(true);
        this.mActionBarWhenSplit.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
            public void onClick(View v) {
                ComposeMessageFragment.this.hideKeyboard();
                MLog.d("Mms_UI_CMA", "in updateInfoFromAdapter(), mIsFromLauncher = " + ComposeMessageFragment.this.mIsFromLauncher);
                if (ComposeMessageFragment.this.mIsFromLauncher) {
                    ComposeMessageFragment.this.backToConversationList();
                } else {
                    ComposeMessageFragment.this.getActivity().onBackPressed();
                }
            }
        });
        if (size > 1) {
            this.mActionBarWhenSplit.setTitle(title, size);
            this.mActionBarWhenSplit.setSubtitle(null);
            this.mActionBarWhenSplit.showMenu(false);
            this.mActionBarWhenSplit.setEndIcon(true, getContext().getResources().getDrawable(R.drawable.ic_contact_no_group), new OnClickListener() {
                public void onClick(View v) {
                    ComposeMessageFragment.this.mActionbarAdapter.viewPeopleInfo();
                }
            });
            this.mActionBarWhenSplit.setEndIconDescription(getContext().getString(R.string.group_info));
            return;
        }
        this.mActionBarWhenSplit.setTitle(title);
        if (isNeedSetLocation) {
            this.mActionbarAdapter.setLocationTitle();
        } else {
            this.mActionBarWhenSplit.setSubtitle(title1);
        }
        this.mActionBarWhenSplit.showMenu(true);
    }

    private void backToConversationList() {
        Intent itt = new Intent(getActivity(), ConversationList.class);
        itt.setAction("android.intent.action.MAIN");
        startActivity(itt);
        getActivity().overridePendingTransition(R.anim.activity_from_launcher_enter, R.anim.activity_from_launcher_exit);
        getActivity().finish();
    }

    protected void updateNormalMenu() {
        int i = R.drawable.ic_sms_wechat;
        if (HwMessageUtils.isSplitOn() || !this.mMsgListView.isInEditMode()) {
            final AddWhatsAppPeopleActionBarAdapter adapter = this.mActionbarAdapter;
            this.mNormalMenu.resetOptionMenu(this.mActionBarWhenSplit.getMenu());
            this.mNormalMenu.clear();
            if (this.mHwCustComposeMessage != null) {
                this.mHwCustComposeMessage.addSearchMenuItem(HwCustComposeMessageImpl.MENU_ID_SEARCH, this.mNormalMenu);
            }
            if (adapter.isExistsInContact()) {
                this.mNormalMenu.addOverflowMenu(11111, R.string.menu_view_contact);
            } else {
                this.mNormalMenu.addOverflowMenu(11117, R.string.contact_unavailable_create_contact);
                this.mNormalMenu.addOverflowMenu(11112, R.string.saveto_existed_contact);
            }
            if (this.mRcsComposeMessage != null && this.mRcsComposeMessage.updateRcsMenu()) {
                this.mNormalMenu.addOverflowMenu(11121, R.string.write_im_group_chat);
            }
            if (adapter.hasEmail()) {
                this.mNormalMenu.addOverflowMenu(11113, R.string.menu_mail);
            } else if (MmsConfig.isVoiceCapable()) {
                this.mNormalMenu.addOverflowMenu(11120, R.string.clickspan_edit_call);
                this.mNormalMenu.addMenu(11114, R.string.menu_call, R.drawable.ic_public_call);
            }
            boolean isChinaVersion = Contact.IS_CHINA_REGION;
            ImageView appsIcon = (ImageView) findViewById(R.id.action_bar_apps);
            if (appsIcon != null) {
                if (adapter.hasWeichat() && adapter.hasWhatsapp()) {
                    appsIcon.setVisibility(0);
                    switch (getLastUsedMenuApps()) {
                        case 0:
                            appsIcon.setBackgroundResource(R.drawable.ic_sms_wechat);
                            break;
                        case 1:
                            appsIcon.setBackgroundResource(R.drawable.ic_sms_whatsapp);
                            break;
                        default:
                            if (!isChinaVersion) {
                                i = R.drawable.ic_sms_whatsapp;
                            }
                            appsIcon.setBackgroundResource(i);
                            break;
                    }
                } else if (adapter.hasWeichat()) {
                    appsIcon.setVisibility(0);
                    appsIcon.setBackgroundResource(R.drawable.ic_sms_wechat);
                } else if (adapter.hasWhatsapp()) {
                    appsIcon.setVisibility(0);
                    appsIcon.setBackgroundResource(R.drawable.ic_sms_whatsapp);
                } else {
                    appsIcon.setVisibility(8);
                }
            }
            if (isChinaVersion) {
                if (adapter.hasWeichat()) {
                    this.mNormalMenu.addOverflowMenu(11118, R.string.menu_weichat);
                }
                if (adapter.hasWhatsapp()) {
                    this.mNormalMenu.addOverflowMenu(11119, R.string.menu_whatsapp);
                }
            } else {
                if (adapter.hasWhatsapp()) {
                    this.mNormalMenu.addOverflowMenu(11119, R.string.menu_whatsapp);
                }
                if (adapter.hasWeichat()) {
                    this.mNormalMenu.addOverflowMenu(11118, R.string.menu_weichat);
                }
            }
            String number = adapter.getNumber();
            if (isBlacklistFeatureEnable()) {
                new AsyncTask<String, Void, Boolean>() {
                    protected Boolean doInBackground(String... names) {
                        return Boolean.valueOf(BlacklistCommonUtils.isNumberBlocked(names[0]));
                    }

                    protected void onPostExecute(Boolean result) {
                        if (result.booleanValue()) {
                            ComposeMessageFragment.this.mNormalMenu.addOverflowMenu(11115, R.string.menu_remove_from_blacklist);
                        } else {
                            ComposeMessageFragment.this.mNormalMenu.addOverflowMenu(11116, R.string.menu_blacklist);
                        }
                        ComposeMessageFragment.this.mActionBarWhenSplit.refreshMenu();
                    }
                }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new String[]{number});
            }
            this.mActionBarWhenSplit.refreshMenu();
            this.mActionBarWhenSplit.showMenu(true);
            this.mActionBarWhenSplit.getSplitActionBarView().setOnCustomMenuListener(new OnCustomMenuListener() {
                public boolean onCustomMenuItemClick(MenuItem aMenuItem) {
                    int itemId = aMenuItem.getItemId();
                    try {
                        boolean isEmail = ((Contact) ComposeMessageFragment.this.mConversation.getRecipients().get(0)).isEmail();
                        switch (itemId) {
                            case 11111:
                                adapter.viewPeopleInfo();
                                break;
                            case 11112:
                                Intent saveToIntent = new Intent("android.intent.action.INSERT_OR_EDIT");
                                if (isEmail) {
                                    saveToIntent.putExtra(Scopes.EMAIL, adapter.getNumber());
                                } else {
                                    saveToIntent.putExtra("phone", adapter.getNumber());
                                }
                                saveToIntent.setType("vnd.android.cursor.item/contact");
                                saveToIntent.putExtra("handle_create_new_contact", false);
                                ComposeMessageFragment.this.getContext().startActivity(saveToIntent);
                                break;
                            case 11113:
                                adapter.writeEmail();
                                break;
                            case 11114:
                                adapter.callRecipients();
                                break;
                            case 11115:
                                adapter.addToBlacklist(false, null);
                                break;
                            case 11116:
                                adapter.addToBlacklist(true, null);
                                break;
                            case 11117:
                                Intent createIntent = new Intent("android.intent.action.INSERT");
                                if (isEmail) {
                                    createIntent.putExtra(Scopes.EMAIL, adapter.getNumber());
                                } else {
                                    createIntent.putExtra("phone", adapter.getNumber());
                                }
                                createIntent.setData(Contacts.CONTENT_URI);
                                ComposeMessageFragment.this.getContext().startActivity(createIntent);
                                break;
                            case 11118:
                                adapter.writeWeichat();
                                break;
                            case 11119:
                                adapter.writeWhatsapp();
                                break;
                            case 11120:
                                adapter.editBeforeCall();
                                break;
                            case 11121:
                                ComposeMessageFragment.this.mRcsComposeMessage.mActionbarAdapterExt.createGroupChat();
                                break;
                            case HwCustComposeMessageImpl.MENU_ID_SEARCH /*11122*/:
                                if (ComposeMessageFragment.this.mHwCustComposeMessage != null) {
                                    ComposeMessageFragment.this.mHwCustComposeMessage.updateSearchMode();
                                    break;
                                }
                                break;
                        }
                        ComposeMessageFragment.this.updateNormalMenu();
                        return false;
                    } catch (Exception e) {
                        MLog.e("Mms_UI_CMA", " getRecipients exception :" + e);
                        return false;
                    }
                }
            });
        }
    }

    private int getLastUsedMenuApps() {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("lastUsedMenu", -1);
    }

    private boolean isBlacklistFeatureEnable() {
        return !this.mActionbarAdapter.isHwMsgSender() ? BlacklistCommonUtils.isBlacklistFeatureEnable() : false;
    }

    private void updateComposeStartIcon() {
        if (!this.mMsgListView.isInEditMode()) {
            boolean isSplitState = false;
            if (getActivity() instanceof ConversationList) {
                isSplitState = ((ConversationList) getActivity()).isSplitState();
            }
            if (this.mHwCustComposeMessage == null || !this.mHwCustComposeMessage.getIsTitleChangeWhenRecepientsChange()) {
                this.mActionBarWhenSplit.showStartIcon(!isSplitState);
            }
        }
    }

    private void updateRecipientsStartIcon() {
        boolean isSplitState = false;
        if (getActivity() instanceof ConversationList) {
            isSplitState = ((ConversationList) getActivity()).isSplitState();
        }
        ComposeRecipientsView composeRecipientsView = this.mComposeRecipientsView;
        boolean z = (this.mConversation.hasDraft() && isSplitState) ? false : true;
        composeRecipientsView.setBackButtonState(z);
    }

    protected boolean isShowRecipientsWhenSplit() {
        return true;
    }

    public String getNumberForRcs() {
        String number = "";
        if (this.mActionbarAdapter != null) {
            return this.mActionbarAdapter.getNumber();
        }
        return number;
    }

    protected void doDeleteAllInCompose() {
    }

    public AbstractEmuiActionBar getActionbar() {
        return this.mActionBarWhenSplit;
    }

    public void setEditorViewSuperLayoutPaddingValues(boolean chooseButtonVisible) {
        if (this.mComposeBottomView != null) {
            this.mComposeBottomView.setEditorViewSuperLayoutPaddingValues(chooseButtonVisible);
        }
    }

    public void setCurrentMessageMode(boolean isRcsMode) {
        if (this.mComposeBottomView != null) {
            this.mComposeBottomView.setCurrentMessageMode(isRcsMode);
        }
    }

    public void setNeedSaveDraftStatus(boolean needSaveDraft) {
        this.mNeedSaveDraft = needSaveDraft;
    }

    private void registerContactDataChangeObserver() {
        getActivity().getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, this.mContactChangeListener);
    }

    private void unregisterContactDataChangeObserver() {
        getActivity().getContentResolver().unregisterContentObserver(this.mContactChangeListener);
    }

    private boolean isInMultiWindowMode() {
        if (getActivity() == null) {
            return false;
        }
        return getActivity().isInMultiWindowMode();
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (this.mComposeChoosePanel != null) {
            this.mComposeChoosePanel.onMultiWindowModeChanged(isInMultiWindowMode);
            this.mComposeChoosePanel.setIsAttachmentShow(false);
        }
    }

    public void hideLandscapeMediaPicker(boolean animate) {
        if (this.mIsLandscape) {
            this.mConversationInputManager.showHideMediaPicker(false, animate);
        }
    }

    public AttachmentSelectLocation getAttachmentLocation() {
        return this.attSelectLocation;
    }

    public void setAttachmentLocation(AttachmentSelectLocation attachmentLocation) {
        this.attSelectLocation = attachmentLocation;
    }

    private void setAttachmentSelectData(AttachmentSelectData attachment, boolean doAppend) {
        if (attachment == null) {
            MLog.d("Mms_UI_CMA", "setAttachmentSelectData failed, params is null.");
        } else if (this.mCryptoCompose.needNotifyUser(attachment.getAttachmentType())) {
            this.mCryptoCompose.addAttachment(this.mRichEditor, attachment.getAttachmentType(), doAppend, attachment);
        } else {
            Uri attachmentUri = attachment.getAttachmentUri();
            if (attachmentUri != null) {
                MmsApp.getApplication().getThumbnailManager().removeThumbnail(attachment.getAttachmentUri());
            }
            switch (attachment.getAttachmentType()) {
                case 2:
                    this.mRichEditor.setNewAttachment(attachmentUri, 2, doAppend);
                    break;
                case 3:
                    this.mRichEditor.setNewAttachment(attachmentUri, 3, doAppend);
                    break;
                case 5:
                case 1211:
                    this.mRichEditor.setNewAttachment(attachmentUri, 5, doAppend);
                    break;
                case 8:
                    if (attachment instanceof AttachmentSelectLocation) {
                        setAttachmentLocation((AttachmentSelectLocation) attachment);
                    }
                    this.mRichEditor.setNewAttachment(attachmentUri, 8, doAppend);
                    break;
                case AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS /*1201*/:
                    this.mRichEditor.setTakePictureState(true);
                    this.mRichEditor.setNewAttachment(attachmentUri, 2, doAppend);
                    break;
            }
        }
    }

    public void addMediaUpdateListener(MediaUpdateListener mediaupdatelistener) {
        if (mediaupdatelistener != null) {
            this.mMediaUpdateListeners.add(mediaupdatelistener);
        }
    }

    public void removeMediaUpdateListener(MediaUpdateListener mediaupdatelistener) {
        if (mediaupdatelistener != null) {
            this.mMediaUpdateListeners.remove(mediaupdatelistener);
        }
    }

    private void doMediaUpdate(int changedType) {
        if (this.mMediaUpdateListeners != null) {
            for (MediaUpdateListener listener : this.mMediaUpdateListeners) {
                if (listener.hasUpdateMedida(changedType)) {
                    listener.updateMedia(this.mRichEditor);
                }
            }
            if (!(this.mConversationInputManager == null || this.mActionBarWhenSplit == null || (changedType != 2 && changedType != 5))) {
                this.mConversationInputManager.updateActionBar(this.mActionBarWhenSplit);
            }
        }
    }

    public void saveDraftWhenProtocolChange(boolean mms) {
        if (HwMessageUtils.isSplitOn() && !(getActivity() instanceof ComposeMessageActivity)) {
            if (mms) {
                this.mConversation.setDraftState(true);
            } else {
                this.mRichEditor.getWorkingMessage().saveDraft(true);
                if (!TextUtils.isEmpty(this.mRichEditor.getWorkingMessage().getText())) {
                    this.mConversation.setDraftState(false);
                }
            }
            ConversationListItem item = null;
            View itemView = this.root.findViewById(R.id.mms_animation_list_item_view);
            if (itemView instanceof ConversationListItem) {
                item = (ConversationListItem) itemView;
            }
            if (item != null) {
                item.updateDraftViewWhenSplit();
            } else {
                MLog.e("Mms_UI_CMA", "Item view is not found !!!");
            }
            if (getActivity() instanceof ConversationList) {
                HwBaseFragment mFragment = ((ConversationList) getActivity()).getFragment();
                if (mFragment instanceof LeftPaneConversationListFragment) {
                    ((LeftPaneConversationListFragment) mFragment).getConversationListAdapter().notifyDataSetChanged();
                }
            }
        }
    }

    private void onDraftChanged() {
        if (this.mConversationInputManager != null) {
            this.mAttachmentPreview.onAttachmentsChanged(this.mRichEditor.getSlideshow(), this.mConversationInputManager.getMediaPickerFullScreenState());
        } else {
            this.mAttachmentPreview.onAttachmentsChanged(this.mRichEditor.getSlideshow(), false);
        }
    }

    private void refreshAttachmentChangedUI(int attachmentType) {
        onDraftChanged();
        updateSendButtonView();
        doMediaUpdate(attachmentType);
        this.mComposeBottomView.resetBottomScrollerHeight(true);
    }

    private void refreshAttachmentPreviewState() {
        if (this.mRichEditor == null || this.mRichEditor.getSlideshow() == null) {
            this.mAttachmentPreview.setVisibility(8);
            return;
        }
        if (this.mRichEditor.getSlideshow().canShowPreview()) {
            this.mAttachmentPreview.setVisibility(0);
        } else {
            this.mAttachmentPreview.setVisibility(8);
        }
    }

    public void onRichAttachmentChanged(int attachmentType) {
        Message message = this.mHandler.obtainMessage(1002);
        message.arg1 = attachmentType;
        this.mHandler.sendMessageDelayed(message, 100);
    }

    private int getSelectionPosForCallLog(Cursor cursor, int oldSelectionPos) {
        Intent intent = getIntent();
        long startTime = intent.getLongExtra("rcs_start_time", -1);
        long callDuation = intent.getLongExtra(TrainManager.DURATION, -1);
        if (-1 == startTime || -1 == callDuation) {
            return oldSelectionPos;
        }
        log("startTime = " + startTime + ", callDuation = " + callDuation);
        callDuation *= 1000;
        cursor.moveToPosition(-1);
        int newSelectionPos = oldSelectionPos;
        while (cursor.moveToNext()) {
            long recvTime = cursor.getLong(6);
            if (recvTime >= startTime && recvTime < startTime + callDuation) {
                newSelectionPos = cursor.getPosition();
                break;
            }
        }
        log("new position: " + newSelectionPos + ", old position:" + oldSelectionPos);
        return newSelectionPos;
    }

    public boolean isFromRcsContact() {
        if (this.mRcsComposeMessage == null || !this.mRcsComposeMessage.isRcsSwitchOn()) {
            return false;
        }
        return this.mRcsComposeMessage.isFromContacts();
    }
}
