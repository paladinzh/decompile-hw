package com.huawei.rcs.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.mms.MmsApp;
import com.android.mms.attachment.ui.mediapicker.RecorderManager;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageListItem;
import com.android.mms.util.ItemLayoutCallback;
import com.android.rcs.ui.RcsMessageUtils;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.MmsClickListener;
import com.huawei.mms.ui.MmsClickListener.IMmsClickListener;
import com.huawei.mms.util.DelaySendManager;
import com.huawei.rcs.media.RcsMediaFileUtils;
import com.huawei.rcs.media.RcsMediaFileUtils.MediaFileType;
import com.huawei.rcs.ui.RcsAsyncIconLoader.OnIconLoadedCallback;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsProfileUtils;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcsUtility;
import java.io.File;
import java.util.HashMap;

public class RcsFileTransMessageListItem extends MessageListItem implements OnIconLoadedCallback {
    private static final long serialVersionUID = 1;
    private TextView alwayAcceptTextview;
    private View clickView;
    private Handler handler = new Handler();
    private Button mAcceptBtn;
    private CheckBox mAlwaysAcceptBtn;
    private View mAudioAttchView;
    private TextView mAudioDurationView;
    private RcsRoundProgressBar mAudioReceiveBar;
    private ImageView mAudioattchImage;
    private Button mCancelBtn;
    private Context mContext;
    private ImageView mFailedIndicator;
    private TextView mFileSize;
    public long mFileTransId;
    public RcsFileTransProgressBar mFileTransImageSrc;
    private transient RcsFileTransMessageItem mFileTransMessageItem;
    private boolean mIsMultiChoice = false;
    private boolean mIsMultiSimActive = false;
    private transient ItemLayoutCallback<MessageItem> mItemLayoutCallback = null;
    private long mLastMsgID = -1;
    private long mLastTransSize = 0;
    private MmsClickListener mMmsClickListener = null;
    private OnClickListener mMsgOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (RcsFileTransMessageListItem.this.mFileTransMessageItem != null && RcsFileTransMessageListItem.this.mFileTransMessageItem.mIsOutgoing && RcsFileTransMessageListItem.this.mFileTransMessageItem.isFailedFileTransMessage()) {
                MLog.d("RcsFileTransMessageListItem FileTrans: ", "resendList  " + RcsFileTransMessageListItem.this.mFileTransMessageItem.mMsgId);
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putLong("fileTransId", RcsFileTransMessageListItem.this.mFileTransMessageItem.mMsgId);
                bundle.putString("sendAddress", RcsFileTransMessageListItem.this.mFileTransMessageItem.mFileAddress);
                bundle.putString("path", RcsFileTransMessageListItem.this.mFileTransMessageItem.mImAttachmentPath);
                bundle.putInt("request", RcsFileTransMessageListItem.this.getFileTransType(RcsFileTransMessageListItem.this.mFileTransMessageItem.mImAttachmentPath));
                bundle.putString("global_trans_id", RcsFileTransMessageListItem.this.mFileTransMessageItem.mImAttachmentGlobalTransId);
                msg.what = 97;
                msg.obj = bundle;
                RcsFileTransMessageListItem.this.mRcseEventHandler.sendMessage(msg);
            }
        }
    };
    private OnClickListener mMsgOnUndelivedClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (RcsFileTransMessageListItem.this.mFileTransMessageItem != null && RcsFileTransMessageListItem.this.mFileTransMessageItem.mIsOutgoing) {
                MLog.d("RcsFileTransMessageListItem FileTrans: ", "resendList  " + RcsFileTransMessageListItem.this.mFileTransMessageItem.mMsgId);
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putLong("fileTransId", RcsFileTransMessageListItem.this.mFileTransMessageItem.mMsgId);
                bundle.putString("sendAddress", RcsFileTransMessageListItem.this.mFileTransMessageItem.mFileAddress);
                bundle.putString("path", RcsFileTransMessageListItem.this.mFileTransMessageItem.mImAttachmentPath);
                bundle.putInt("request", RcsFileTransMessageListItem.this.getFileTransType(RcsFileTransMessageListItem.this.mFileTransMessageItem.mImAttachmentPath));
                msg.what = 1109;
                msg.obj = bundle;
                RcsFileTransMessageListItem.this.mRcseEventHandler.sendMessage(msg);
            }
        }
    };
    private LinearLayout mRImageFrame;
    private Handler mRcseEventHandler = null;
    private RecorderManager mRecorderManager = null;
    private LinearLayout mRecvFileFailedLayout;
    private Button mRejectBtn;
    private TextView mRejectTextView;
    private ImageView mResendIcon;
    public long mThreadId = 0;
    public TextView mTransInfoTextView;
    private TextView mTransNameTextView;
    private View mVAttchView;
    private ImageView mVattchImage;
    LinearLayout mVattchNodesLayout;
    private RelativeLayout rcs_common_layout;
    public HashMap<Long, Boolean> state = new HashMap();

    private class ItemTouchListener implements IMmsClickListener {
        public ItemTouchListener(RcsFileTransMessageItem msgItem) {
        }

        public void onDoubleClick(View view) {
            boolean isDelayMessage = false;
            if (!(RcsFileTransMessageListItem.this.getRcsMessageListItem() == null || RcsFileTransMessageListItem.this.getRcsMessageListItem().getHwCustCallback() == null)) {
                isDelayMessage = RcsFileTransMessageListItem.this.getRcsMessageListItem().getHwCustCallback().isDelayMessage();
            }
            if (!RcsFileTransMessageListItem.this.mFileTransMessageItem.isNotDelayMsg() && r0 && DelaySendManager.getInst().isDelayMsg(RcsFileTransMessageListItem.this.mFileTransMessageItem.getCancelId(), "chat", false)) {
                DelaySendManager.getInst().setDelayMsgCanceled(RcsFileTransMessageListItem.this.mFileTransMessageItem.getCancelId(), "chat", false);
                RcsFileTransMessageListItem.this.sendMessage(RcsFileTransMessageListItem.this.mFileTransMessageItem, 1000105);
                RcsFileTransMessageListItem.this.getRcsMessageListItem().getHwCustCallback().setDelayMessageStatus(false);
            }
        }

        public void onSingleClick(View view) {
        }
    }

    public void setItemLayoutCallback(ItemLayoutCallback<MessageItem> callBack) {
        MLog.i("RcsFileTransMessageListItem FileTrans: ", "This is FileTransMessageset. call setItemLayoutCallback.");
        this.mItemLayoutCallback = callBack;
    }

    private void onItemLayout(MessageItem item, Throwable exception) {
        if (this.mItemLayoutCallback != null) {
            this.mItemLayoutCallback.onItemLayout(item, exception);
        }
    }

    public RcsFileTransMessageListItem(Context context) {
        super(context);
        this.mContext = context;
    }

    public RcsFileTransMessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void bind(MessageItem messageItem, boolean isLastItem, int position) {
        bind(messageItem, isLastItem, position, this.mIsMultiSimActive);
    }

    public void bind(MessageItem messageItem, boolean isLastItem, int position, boolean isMultiSimActive, String address) {
        super.bind(messageItem, isLastItem, position, isMultiSimActive, address);
        this.mIsMultiSimActive = isMultiSimActive;
        if (messageItem instanceof RcsFileTransMessageItem) {
            this.mFileTransMessageItem = (RcsFileTransMessageItem) messageItem;
            if (RcsTransaction.getCMCCCustStatus() && this.mFileTransMessageItem.isVCardFile()) {
                MLog.i("RcsFileTransMessageListItem FileTrans: ", "bind message about CMCC");
                bindCommonMessageCust(this.mFileTransMessageItem);
                if (this.mFileTransMessageItem.mIsOutgoing) {
                    updateFileTransMessageStatusCust(this.mFileTransMessageItem);
                }
            } else {
                bindCommonMessage(this.mFileTransMessageItem);
                if (this.mFileTransMessageItem.mIsOutgoing) {
                    updateFileTransMessageStatus(this.mFileTransMessageItem);
                }
                if (getTag() == null || !(getTag() instanceof String)) {
                    asyncLoadImageIcon(this.mFileTransMessageItem);
                }
            }
        }
    }

    public void bind(MessageItem messageItem, boolean isLastItem, int position, boolean isMultiSimActive) {
        bind(messageItem, isLastItem, position, isMultiSimActive, null);
    }

    private void asyncLoadImageIcon(RcsFileTransMessageItem ftMsgItem) {
        if (ftMsgItem == null) {
            MLog.i("RcsFileTransMessageListItem FileTrans: ", "asyncLoadImageIcon -> ftMsgItem is null, return.");
            return;
        }
        String sCacheKey = RcsUtility.getBitmapFromMemCacheKey(ftMsgItem.mMsgId, ftMsgItem.mChatType);
        if (ftMsgItem.mFileIcon != null) {
            if (ftMsgItem.getImageCache() != null && ftMsgItem.getImageCache().getBitmapFromMemCache(sCacheKey) != null) {
                MLog.i("RcsFileTransMessageListItem FileTrans: ", " asyncLoadImageIcon - > find in cache. do not need to change.");
                return;
            } else if (!(ftMsgItem.mIsOutgoing || ftMsgItem.mImAttachmentStatus == 1002 || ftMsgItem.mImAttachmentStatus == Place.TYPE_ROUTE)) {
                MLog.i("RcsFileTransMessageListItem FileTrans: ", " asyncLoadImageIcon - > receive not OK. do not need to change.");
                return;
            }
        } else if (ftMsgItem.mImAttachmentStatus == 1009) {
            MLog.i("RcsFileTransMessageListItem FileTrans: ", " asyncLoadImageIcon - > reject,do not need to change.");
            return;
        } else if (!(ftMsgItem.mIsOutgoing || ftMsgItem.mImAttachmentStatus == 1002 || ftMsgItem.mImAttachmentStatus == Place.TYPE_ROUTE || (ftMsgItem.mImAttatchmentIcon != null && !TextUtils.isEmpty(ftMsgItem.mImAttatchmentIcon) && new File(ftMsgItem.mImAttatchmentIcon).exists()))) {
            MLog.i("RcsFileTransMessageListItem FileTrans: ", " asyncLoadImageIcon - > ftMsgItem.mFileIcon is null and receive not OK. do not need to change.");
            return;
        }
        MLog.i("RcsFileTransMessageListItem FileTrans: ", " asyncLoadImageIcon - > we need to async load icon");
        if (ftMsgItem.mImAttatchmentIcon != null && !TextUtils.isEmpty(ftMsgItem.mImAttatchmentIcon) && new File(ftMsgItem.mImAttatchmentIcon).exists()) {
            RcsAsyncIconLoader.getInstance().asyncLoadIcon(sCacheKey, ftMsgItem.mImAttatchmentIcon, this);
            MLog.i("RcsFileTransMessageListItem FileTrans: ", " asyncLoadImageIcon - > we need to async load icon in Pre_ThumbNail Mode");
        } else if (!TextUtils.isEmpty(ftMsgItem.mImAttachmentPath) && new File(ftMsgItem.mImAttachmentPath).exists()) {
            RcsAsyncIconLoader.getInstance().asyncLoadIcon(sCacheKey, ftMsgItem.mImAttachmentPath, this);
            MLog.i("RcsFileTransMessageListItem FileTrans: ", " asyncLoadImageIcon - > we need to async load icon in No_Pre_ThumbNail Mode");
        }
    }

    public void onIconLoaded(String cachekey, Bitmap bitmap, boolean isDefaultIcon) {
        if (bitmap != null) {
            String sCacheKey = "";
            if (this.mFileTransMessageItem != null) {
                sCacheKey = RcsUtility.getBitmapFromMemCacheKey(this.mFileTransMessageItem.mMsgId, this.mFileTransMessageItem.mChatType);
            }
            if (!(isDefaultIcon || this.mFileTransMessageItem == null)) {
                RcsImageCache imageCache = this.mFileTransMessageItem.getImageCache();
                if (imageCache != null) {
                    imageCache.addBitmapToCache(cachekey, bitmap);
                }
            }
            if (this.mFileTransMessageItem != null && TextUtils.equals(cachekey, sCacheKey)) {
                MLog.i("RcsFileTransMessageListItem FileTrans: ", " onIconLoaded -> the current bind msg item is the same that need to create the icon. cachekey = " + cachekey);
                this.mFileTransMessageItem.mFileIcon = bitmap;
                if (this.handler == null) {
                    MLog.w("RcsFileTransMessageListItem FileTrans: ", " onIconLoaded -> handler is null.");
                    return;
                }
                this.handler.post(new Runnable() {
                    public void run() {
                        RcsFileTransMessageListItem.this.setmFileTransImageSrc(RcsFileTransMessageListItem.this.mFileTransMessageItem);
                    }
                });
            }
        }
    }

    private void updateFileTransMessageStatusCust(RcsFileTransMessageItem fileTransItem) {
        if (!(fileTransItem.mImAttachmentStatus == 1001 || fileTransItem.mImAttachmentStatus == 1010)) {
            if (fileTransItem.mImAttachmentStatus != 1009) {
                return;
            }
        }
        fileTransItem.setFailedFileTransMessage(true);
    }

    private void updateFileTransMessageStatus(RcsFileTransMessageItem fileTransItem) {
        boolean z = true;
        if (!(fileTransItem.mImAttachmentStatus == 1001 || fileTransItem.mImAttachmentStatus == 1010)) {
            if (fileTransItem.mImAttachmentStatus != 1009) {
                return;
            }
        }
        fileTransItem.setFailedFileTransMessage(true);
        if (!fileTransItem.isImageFileType()) {
            z = fileTransItem.isVideoFileType();
        }
        if (!z) {
            this.mFileSize.setVisibility(0);
        }
    }

    private void bindCommonMessage(RcsFileTransMessageItem msgItem) {
        if (!(getRcsMessageListItem() == null || getRcsMessageListItem().getHwCustCallback() == null)) {
            getRcsMessageListItem().getHwCustCallback().setBodyTextViewVisibility(8);
        }
        if (this.mRImageFrame != null) {
            this.mRImageFrame.setVisibility(0);
        }
        inflateFileTransferView(msgItem);
        if (msgItem.mImAttachmentPath != null) {
            hideAllFileTransView(msgItem);
            refreshViewByFtStatus(msgItem);
            onItemLayout(msgItem, null);
        }
    }

    private void inflateFileTransferView(RcsFileTransMessageItem msgItem) {
        switch (msgItem.mFileTransType) {
            case 7:
            case 8:
                inflateCommonView(msgItem);
                return;
            case 9:
            case 10:
                inflateCommonView(msgItem);
                inflateSpecialView(msgItem);
                return;
            default:
                inflateCommonView(msgItem);
                return;
        }
    }

    private void inflateCommonView(RcsFileTransMessageItem msgItem) {
        if (this.mRImageFrame == null) {
            this.mRImageFrame = (LinearLayout) findViewById(R.id.rImageFrame);
            this.mAcceptBtn = (Button) findViewById(R.id.accept_button);
            this.mRejectBtn = (Button) findViewById(R.id.reject_button);
            this.mCancelBtn = (Button) findViewById(R.id.cancel_button);
            this.mTransInfoTextView = (TextView) findViewById(R.id.file_transInfo);
            this.mTransNameTextView = (TextView) findViewById(R.id.file_transName);
            this.mFileTransImageSrc = (RcsFileTransProgressBar) findViewById(R.id.fileTransfer_image_src);
            this.mFileTransImageSrc.setContentDescription(getResources().getString(R.string.rcs_view_slideshow));
            this.mAlwaysAcceptBtn = (CheckBox) findViewById(R.id.always_accept_button);
            this.alwayAcceptTextview = (TextView) findViewById(R.id.alwayAcceptTextview);
            this.mRejectTextView = (TextView) findViewById(R.id.rejectTextview);
            this.mFileSize = (TextView) findViewById(R.id.txv_file_size);
            this.mResendIcon = (ImageView) findViewById(R.id.failed_indicator);
            this.mRecvFileFailedLayout = (LinearLayout) findViewById(R.id.recvfilefailed_wrapper);
            this.mFailedIndicator = (ImageView) findViewById(R.id.failed_indicator_ft);
            ViewStub clickviewListItem = (ViewStub) findViewById(R.id.click_view_stub);
            if (clickviewListItem != null) {
                clickviewListItem.setLayoutResource(R.layout.rcs_click_view_stub_layout);
                clickviewListItem.inflate();
            }
            this.clickView = findViewById(R.id.click_view);
        }
    }

    private void bindCommonMessageCust(RcsFileTransMessageItem msgItem) {
        inflateFileTransferViewCust(msgItem);
        refreshFTViewByFtStatusCust(msgItem);
        if (!(getRcsMessageListItem() == null || getRcsMessageListItem().getHwCustCallback() == null)) {
            getRcsMessageListItem().getHwCustCallback().setBodyTextViewVisibility(8);
        }
        if (this.mRImageFrame != null) {
            this.mRImageFrame.setVisibility(0);
        }
        onItemLayout(msgItem, null);
    }

    private void inflateFileTransferViewCust(RcsFileTransMessageItem msgItem) {
        inflateCommonViewCust(msgItem);
        if (msgItem.isVCardFile()) {
            inflateVCardTransferViewCust(msgItem);
            setVcardIndicateVisible(msgItem);
        }
        if (msgItem.isAudioFileType()) {
            inflateAudioTransferViewCust(msgItem);
            setAudioIndicateVisible(msgItem);
        }
    }

    private void inflateSpecialView(RcsFileTransMessageItem msgItem) {
        inflateCommonViewCust(msgItem);
        if (msgItem.isVCardFile()) {
            inflateVCardTransferViewCust(msgItem);
            setVcardIndicateVisible(msgItem);
        } else if (msgItem.isAudioFileType()) {
            inflateAudioTransferViewCust(msgItem);
            setAudioIndicateVisible(msgItem);
        }
    }

    private void inflateVCardTransferViewCust(RcsFileTransMessageItem msgItem) {
        View rcsView = findViewById(R.id.rcs_especial_view);
        rcsView.setVisibility(0);
        this.mVAttchView = rcsView.findViewById(R.id.vcard_view);
        this.mVAttchView.setVisibility(0);
        this.mVattchImage = (ImageView) this.mVAttchView.findViewById(R.id.vcard_attach_image);
        this.mVattchImage.setVisibility(0);
        this.mVattchNodesLayout = (LinearLayout) this.mVAttchView.findViewById(R.id.vcard_attach_nodes);
        this.mVattchNodesLayout.setVisibility(8);
    }

    private int getAudioMessageDuration(RcsFileTransMessageItem msgItem) {
        return RcsMessageUtils.getAudioFileDuration(this.mContext, msgItem.getAttachmentFile());
    }

    private boolean checkAudioReceiving(RcsFileTransMessageItem msgItem) {
        if (msgItem.mIsOutgoing || msgItem.mImAttachmentStatus == 1002 || msgItem.mImAttachmentStatus == Place.TYPE_ROUTE) {
            return false;
        }
        return true;
    }

    private void updateAudioReceiveView(boolean receiving) {
        if (receiving) {
            this.mAudioReceiveBar.setVisibility(0);
            this.mAudioattchImage.setVisibility(8);
            this.mAudioDurationView.setVisibility(8);
            return;
        }
        this.mAudioReceiveBar.setVisibility(8);
        this.mAudioattchImage.setVisibility(0);
        this.mAudioDurationView.setVisibility(0);
    }

    private void inflateAudioTransferViewCust(RcsFileTransMessageItem msgItem) {
        View rcsView = findViewById(R.id.rcs_especial_view);
        rcsView.setVisibility(0);
        this.mAudioAttchView = rcsView.findViewById(R.id.audio_view);
        this.mAudioAttchView.setVisibility(0);
        if (msgItem.mIsOutgoing) {
            this.mAudioattchImage = (ImageView) this.mAudioAttchView.findViewById(R.id.rcs_audio_anima_image_view_send);
            this.mAudioDurationView = (TextView) this.mAudioAttchView.findViewById(R.id.rcs_audio_anima_duration_send);
        } else {
            this.mAudioattchImage = (ImageView) this.mAudioAttchView.findViewById(R.id.rcs_audio_anima_image_view_receiver);
            this.mAudioDurationView = (TextView) this.mAudioAttchView.findViewById(R.id.rcs_audio_anima_duration_receiver);
        }
        this.mAudioReceiveBar = (RcsRoundProgressBar) this.mAudioAttchView.findViewById(R.id.round_progress_bar);
        updateAudioReceiveView(checkAudioReceiving(msgItem));
        if (this.mRecorderManager == null) {
            this.mFileTransId = msgItem.mFileTransId;
            this.mRecorderManager = new RecorderManager(MmsApp.getApplication().getApplicationContext());
            this.mRecorderManager.mAudioAnimaImageView = this.mAudioattchImage;
            File audioFile = msgItem.getAttachmentFile();
            if (audioFile != null) {
                this.mRecorderManager.setAudioUri(Uri.fromFile(audioFile));
            }
        }
    }

    private void inflateCommonViewCust(RcsFileTransMessageItem msgItem) {
        this.rcs_common_layout = (RelativeLayout) findViewById(R.id.rcs_common_view);
        this.rcs_common_layout.setVisibility(8);
        this.mResendIcon = (ImageView) findViewById(R.id.failed_indicator);
        if (this.mResendIcon != null) {
            this.mResendIcon.setVisibility(8);
        }
    }

    private void refreshFTViewByFtStatusCust(RcsFileTransMessageItem msgItem) {
        refreshViewAnyStatusCust(msgItem);
    }

    private void setVcardIndicateVisible(RcsFileTransMessageItem msgItem) {
        setVcardClickListener(this.mVAttchView, msgItem);
        this.mVAttchView.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                RcsFileTransMessageListItem.this.dissmiss();
                return v.showContextMenu();
            }
        });
    }

    private void setAudioIndicateVisible(RcsFileTransMessageItem msgItem) {
        setAudioClickListener(this.mAudioAttchView, msgItem);
        this.mAudioAttchView.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                RcsFileTransMessageListItem.this.dissmiss();
                return v.showContextMenu();
            }
        });
    }

    private void refreshViewAnyStatusCust(RcsFileTransMessageItem msgItem) {
        MLog.i("RcsFileTransMessageListItem FileTrans: ", "refreshViewAnyStatusCust = " + msgItem.mImAttachmentStatus + " msgid = " + msgItem.mMsgId);
        switch (msgItem.mImAttachmentStatus) {
            case 1000:
            case 1001:
            case 1007:
            case 1009:
            case 1010:
                if (msgItem.mIsOutgoing) {
                    showSendFileView(msgItem);
                    return;
                }
                return;
            case 1002:
            case 1003:
            case 1004:
            case 1005:
            case Place.TYPE_ROOM /*1019*/:
            case Place.TYPE_ROUTE /*1020*/:
                showCompleteFileView(msgItem);
                return;
            case Place.TYPE_POST_BOX /*1014*/:
                return;
            case Place.TYPE_POSTAL_TOWN /*1017*/:
                showWaitAcceptFileView(msgItem);
                return;
            default:
                MLog.w("RcsFileTransMessageListItem FileTrans: ", "Such status does not exist in refreshViewAnyStatusCust");
                return;
        }
    }

    private void showCompleteFileView(RcsFileTransMessageItem msgItem) {
        this.mLastTransSize = 0;
        switch (msgItem.mFileTransType) {
            case 7:
            case 8:
                showCompleteFileDefaultView(msgItem);
                this.mTransNameTextView.setVisibility(8);
                this.mFileTransImageSrc.showMediaFileInfo(Formatter.formatFileSize(this.mContext, msgItem.mImAttachmentTotalSize));
                this.mTransInfoTextView.setVisibility(8);
                this.mFileSize.setVisibility(8);
                return;
            case 9:
            case 10:
                setInComingVcardView(msgItem);
                return;
            default:
                showCompleteFileDefaultView(msgItem);
                return;
        }
    }

    private void showCompleteFileDefaultView(RcsFileTransMessageItem msgItem) {
        this.mTransInfoTextView.setVisibility(8);
        this.mTransNameTextView.setVisibility(8);
        this.mFileSize.setVisibility(0);
        String mImAttachmentPath = getFileAttachmentPath(msgItem);
        this.mTransNameTextView.setVisibility(0);
        this.mTransInfoTextView.setTextSize(14.0f);
        setTextForTransNameTextView(mImAttachmentPath);
        this.mFileTransImageSrc.setVisibility(0);
        this.mFileTransImageSrc.hideProgressBar();
        setmFileTransImageSrc(msgItem);
        if (this.mRecvFileFailedLayout != null) {
            this.mRecvFileFailedLayout.setVisibility(8);
        }
        if (msgItem.isVideoFileType()) {
            this.mFileTransImageSrc.showVideoIcon(this.mContext);
        }
    }

    private void showSendFileView(RcsFileTransMessageItem msgItem) {
        switch (msgItem.mFileTransType) {
            case 9:
                setAudioDurationView(msgItem);
                return;
            case 10:
                setVattchViewImageSrc(msgItem);
                return;
            default:
                return;
        }
    }

    private void showWaitAcceptFileView(RcsFileTransMessageItem msgItem) {
        switch (msgItem.mFileTransType) {
            case 7:
            case 8:
                showWaitAcceptFileDefaultView(msgItem);
                this.mTransNameTextView.setVisibility(8);
                return;
            case 9:
                showCommonView();
                showWaitAcceptFileDefaultView(msgItem);
                this.mTransNameTextView.setVisibility(8);
                return;
            case 10:
                showDefualtVcardView();
                return;
            default:
                showWaitAcceptFileDefaultView(msgItem);
                return;
        }
    }

    private void showCommonView() {
        if (this.rcs_common_layout == null) {
            this.rcs_common_layout = (RelativeLayout) findViewById(R.id.rcs_common_view);
        }
        if (this.rcs_common_layout != null) {
            this.rcs_common_layout.setVisibility(0);
        }
        View rcsView = findViewById(R.id.rcs_especial_view);
        if (rcsView != null) {
            rcsView.setVisibility(8);
        }
    }

    private void showWaitAcceptFileDefaultView(RcsFileTransMessageItem msgItem) {
        int i;
        Button button = this.mAcceptBtn;
        if (this.mIsMultiChoice) {
            i = 8;
        } else {
            i = 0;
        }
        button.setVisibility(i);
        CheckBox checkBox = this.mAlwaysAcceptBtn;
        if (this.mIsMultiChoice) {
            i = 8;
        } else {
            i = 0;
        }
        checkBox.setVisibility(i);
        TextView textView = this.alwayAcceptTextview;
        if (this.mIsMultiChoice) {
            i = 8;
        } else {
            i = 0;
        }
        textView.setVisibility(i);
        button = this.mRejectBtn;
        if (this.mIsMultiChoice) {
            i = 8;
        } else {
            i = 0;
        }
        button.setVisibility(i);
        this.mTransNameTextView.setVisibility(0);
        this.mTransNameTextView.setText(msgItem.mImAttachmentContent);
        this.mTransInfoTextView.setVisibility(0);
        this.mTransInfoTextView.setText(Formatter.formatFileSize(this.mContext, msgItem.mImAttachmentTotalSize));
        this.mFileTransImageSrc.setVisibility(0);
        setmFileTransImageSrc(msgItem);
        this.mFileTransImageSrc.hideProgressBar();
        if (this.mRecvFileFailedLayout != null) {
            this.mRecvFileFailedLayout.setVisibility(8);
        }
        if (this.state.get(Long.valueOf(msgItem.mMsgId)) == null || !((Boolean) this.state.get(Long.valueOf(msgItem.mMsgId))).booleanValue()) {
            this.mAlwaysAcceptBtn.setChecked(false);
        } else {
            this.mAlwaysAcceptBtn.setChecked(true);
        }
        if (this.mIsMultiChoice) {
            handleMultiChoice(msgItem);
        } else {
            getListView().setTag("disable-multi-select-move");
        }
    }

    private void handleMultiChoice(RcsFileTransMessageItem msgItem) {
        switch (msgItem.mFileTransType) {
            case 7:
            case 8:
                this.mFileTransImageSrc.showMediaFileInfo(Formatter.formatFileSize(this.mContext, msgItem.mImAttachmentTotalSize));
                this.mTransInfoTextView.setVisibility(8);
                return;
            default:
                return;
        }
    }

    private void setInComingVcardView(RcsFileTransMessageItem msgItem) {
        switch (msgItem.mFileTransType) {
            case 9:
                setAudioDurationView(msgItem);
                return;
            case 10:
                setVattchViewImageSrc(msgItem);
                return;
            default:
                return;
        }
    }

    private void setAudioDurationView(RcsFileTransMessageItem msgItem) {
        int duration = 0;
        boolean receiving = checkAudioReceiving(msgItem);
        if (!receiving) {
            duration = getAudioMessageDuration(msgItem);
        }
        if (duration < 1000) {
            duration += 1000;
        }
        duration /= 1000;
        if (this.mAudioAttchView != null) {
            int width = RcsUtility.getAudioLength(duration, getResources().getDisplayMetrics().density);
            int rcsAudioMargin = (int) getResources().getDimension(R.dimen.rcs_audio_view_margin);
            MarginLayoutParams params = (MarginLayoutParams) this.mAudioAttchView.getLayoutParams();
            if (params != null) {
                params.width = width;
                if (msgItem.mIsOutgoing) {
                    params.setMarginStart(rcsAudioMargin);
                } else {
                    params.setMarginEnd(rcsAudioMargin);
                }
                this.mAudioAttchView.setLayoutParams(params);
            }
        }
        if (this.mAudioDurationView != null) {
            this.mAudioDurationView.setText(String.format("%02d:%02d", new Object[]{Integer.valueOf((duration % 3600) / 60), Integer.valueOf((duration % 3600) % 60)}));
        }
        updateAudioReceiveView(receiving);
    }

    private void setVattchViewImageSrc(RcsFileTransMessageItem msgItem) {
        boolean z = true;
        RcsVCardInfo rcsVCardInfo;
        Context context;
        View view;
        LinearLayout linearLayout;
        ImageView imageView;
        if (msgItem.mVcardInfo != null) {
            MLog.d("RcsFileTransMessageListItem FileTrans: ", "setVattchViewImageSrc: msgItem.mVcardInfo is not null, presentVcardThumbnail");
            rcsVCardInfo = msgItem.mVcardInfo;
            context = this.mContext;
            view = this.mVAttchView;
            linearLayout = this.mVattchNodesLayout;
            imageView = this.mVattchImage;
            if (this.mItemType != 8) {
                z = false;
            }
            rcsVCardInfo.presentVcardThumbnail(context, view, linearLayout, imageView, z);
            this.mVattchNodesLayout.setVisibility(0);
        } else if (msgItem.createVcardParsingModule()) {
            MLog.d("RcsFileTransMessageListItem FileTrans: ", "setVattchViewImageSrc: createVcardParsingModule");
            rcsVCardInfo = msgItem.mVcardInfo;
            context = this.mContext;
            view = this.mVAttchView;
            linearLayout = this.mVattchNodesLayout;
            imageView = this.mVattchImage;
            if (this.mItemType != 8) {
                z = false;
            }
            rcsVCardInfo.presentVcardThumbnail(context, view, linearLayout, imageView, z);
            this.mVattchNodesLayout.setVisibility(0);
        }
        if (msgItem.mFileIcon != null) {
            this.mVattchImage.setImageBitmap(msgItem.mFileIcon);
            MLog.d("RcsFileTransMessageListItem FileTrans: ", "setVattchViewImageSrc: msgItem.mFileIcon is not null");
        }
        if (!new File(msgItem.mImAttachmentPath).exists()) {
            MLog.d("RcsFileTransMessageListItem FileTrans: ", "setVattchViewImageSrc: vcrad file is not exist, show default icon");
            this.mVattchImage.setImageResource(R.drawable.rcs_ic_contact_picture_holo_dark);
            this.mVattchNodesLayout.setVisibility(8);
        }
    }

    private void refreshViewByFtStatus(final RcsFileTransMessageItem msgItem) {
        if (msgItem.isLocation()) {
            refreshViewByLocation(msgItem);
            return;
        }
        setOnClickListener(this.mAcceptBtn, msgItem);
        setOnClickListener(this.mRejectBtn, msgItem);
        setOnClickListener(this.mCancelBtn, msgItem);
        setOnLongClickListener(this.mFileTransImageSrc, msgItem);
        setFileTransImageClickListener(this.mFileTransImageSrc, msgItem);
        refreshViewAnyStatus(msgItem);
        setOnClickListener(this.mAlwaysAcceptBtn, msgItem);
        this.mAlwaysAcceptBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && RcsProfile.rcsIsLogin()) {
                    RcsFileTransMessageListItem.this.receiveFile(msgItem);
                }
            }
        });
        if (this.mResendIcon != null) {
            this.mResendIcon.setEnabled(true);
            this.mResendIcon.setOnClickListener(this.mMsgOnClickListener);
        }
        if (this.clickView != null) {
            this.clickView.setOnClickListener(this.mMsgOnUndelivedClickListener);
        }
    }

    public void refreshViewAnyStatus(RcsFileTransMessageItem msgItem) {
        setFieSizeAndType(msgItem);
        this.mDateView.setVisibility(!this.mNeedHideUnderPopView ? 0 : 8);
        MLog.i("RcsFileTransMessageListItem FileTrans: ", "refreshViewAnyStatus =  " + msgItem.mImAttachmentStatus + "msgid = " + msgItem.mMsgId);
        switch (msgItem.mImAttachmentStatus) {
            case 1000:
            case 1007:
                showProcessingStatusView(msgItem);
                return;
            case 1001:
            case 1010:
                showCommonStatusView(msgItem);
                return;
            case 1002:
            case 1003:
            case 1004:
            case 1005:
            case Place.TYPE_ROOM /*1019*/:
            case Place.TYPE_ROUTE /*1020*/:
                showCompleteFileView(msgItem);
                return;
            case 1009:
                showRejectView();
                return;
            case Place.TYPE_POST_BOX /*1014*/:
                showFiletranSendLast(msgItem);
                return;
            case Place.TYPE_POSTAL_TOWN /*1017*/:
                showWaitAcceptFileView(msgItem);
                return;
            case Place.TYPE_PREMISE /*1018*/:
                showUndelivedStatusView(msgItem);
                return;
            default:
                MLog.w("RcsFileTransMessageListItem FileTrans: ", "Such status does not exist in RefreshViewAnyStatus");
                return;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleWhenClickFile(RcsFileTransMessageItem msgItem, Message aMsg, Bundle aBundle) {
        Intent intent = new Intent("android.intent.action.VIEW");
        if (msgItem.isLocation()) {
            intent.setData(Uri.parse(changeUri(msgItem.mImAttachmentContent)));
        } else {
            if (new File(msgItem.mImAttachmentPath).exists()) {
                if (!(msgItem.mImAttachmentStatus == 1010 || msgItem.mImAttachmentStatus == 1001)) {
                    if (msgItem.mImAttachmentStatus == 1009) {
                    }
                    RcsTransaction.sendFtReadReport(msgItem.mMsgId, serialVersionUID);
                    if (msgItem.mFileTransType == 9) {
                        Intent viewImageInatent = new Intent("android.intent.action.VIEW");
                        viewImageInatent.setDataAndType(RcsProfileUtils.getFileContentUri(this.mContext, new File(msgItem.mImAttachmentPath)), RcsMediaFileUtils.getFileMimeType(msgItem.mImAttachmentPath));
                        viewImageInatent.putExtra("SingleItemOnly", true);
                        this.mContext.startActivity(viewImageInatent);
                        return;
                    } else if (msgItem.mFileTransType == 7) {
                        Bundle bd = new Bundle();
                        bd.putLong("thread_id", this.mThreadId);
                        bd.putInt("chat_type", msgItem.mChatType);
                        bd.putInt("load_type", msgItem.mLoadType);
                        RcsProfileUtils.viewImageFile(this.mContext, msgItem.mImAttachmentPath, bd);
                        return;
                    } else {
                        File tempFile = new File(msgItem.mImAttachmentPath);
                        if (msgItem.mFileTransType == 8) {
                            intent.setDataAndType(RcsProfileUtils.getVideoContentUri(this.mContext, tempFile), RcsMediaFileUtils.getFileMimeType(msgItem.mImAttachmentPath));
                        } else {
                            intent.setDataAndType(RcsProfileUtils.getFileContentUri(this.mContext, tempFile), RcsMediaFileUtils.getFileMimeType(msgItem.mImAttachmentPath));
                            intent.setFlags(1);
                        }
                        intent.putExtra("SingleItemOnly", true);
                    }
                }
            }
            Toast.makeText(this.mContext, R.string.text_file_not_exist, 0).show();
            return;
        }
        try {
            this.mContext.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this.mContext, R.string.current_no_support_app, 0).show();
        }
    }

    private String changeUri(String oldUri) {
        if (oldUri.contains("http://maps.google.com/?q=")) {
            String str = oldUri.substring("http://maps.google.com/?q=".length());
            if (str.contains(",")) {
                String[] latitudeAndLongtitude = str.split(",");
                StringBuffer strBuffer = new StringBuffer();
                strBuffer.append("http://maps.google.com/maps?q=loc:");
                strBuffer.append(latitudeAndLongtitude[0]);
                strBuffer.append(",");
                strBuffer.append(latitudeAndLongtitude[1]);
                return strBuffer.toString();
            }
        }
        return null;
    }

    private int getFileTransType(String mAttachmentPath) {
        if (mAttachmentPath == null) {
            return 0;
        }
        MediaFileType fileType = RcsMediaFileUtils.getFileType(mAttachmentPath);
        if (fileType != null) {
            if (RcsMediaFileUtils.isAudioFileType(fileType.fileType)) {
                return 9;
            }
            if (RcsMediaFileUtils.isImageFileType(fileType.fileType)) {
                return 7;
            }
            if (RcsMediaFileUtils.isVideoFileType(fileType.fileType)) {
                return 8;
            }
        }
        return 0;
    }

    private void setOnLongClickListener(View aButton, RcsFileTransMessageItem msgItem) {
        aButton.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                switch (v.getId()) {
                    case R.id.fileTransfer_image_src:
                        RcsFileTransMessageListItem.this.dissmiss();
                        return false;
                    default:
                        RcsFileTransMessageListItem.this.dissmiss();
                        return v.showContextMenu();
                }
            }
        });
    }

    private void dissmiss() {
        MLog.d("RcsFileTransMessageListItem FileTrans: ", "dismiss for finbugs");
    }

    private void setOnClickListener(View aButton, final RcsFileTransMessageItem msgItem) {
        aButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putLong("totalSize", msgItem.mImAttachmentTotalSize);
                bundle.putLong("fileTransId", msgItem.mFileTransId);
                switch (v.getId()) {
                    case R.id.fileTransfer_image_src:
                        RcsFileTransMessageListItem.this.handleWhenClickFile(msgItem, msg, bundle);
                        return;
                    case R.id.cancel_button:
                        if (msgItem.mImAttachmentStatus == 1000 || msgItem.mImAttachmentStatus == 1007) {
                            RcsFileTransMessageListItem.this.mCancelBtn.setVisibility(8);
                            RcsTransaction.cancelFT(msgItem.mMsgId, msgItem.mIsOutgoing, RcsFileTransMessageListItem.serialVersionUID);
                            RcsFileTransMessageListItem.this.mFileTransImageSrc.setProgress(0);
                            RcsFileTransMessageListItem.this.mFileTransImageSrc.hideProgressBar();
                        }
                        return;
                    case R.id.always_accept_button:
                        if (!RcsFileTransMessageListItem.this.mAlwaysAcceptBtn.isChecked()) {
                            RcsFileTransMessageListItem.this.state.put(Long.valueOf(msgItem.mMsgId), Boolean.valueOf(false));
                            break;
                        } else {
                            RcsFileTransMessageListItem.this.state.put(Long.valueOf(msgItem.mMsgId), Boolean.valueOf(true));
                            break;
                        }
                    case R.id.accept_button:
                        if (RcsProfile.rcsIsLogin()) {
                            if (msgItem.mImAttachmentStatus == Place.TYPE_POSTAL_TOWN) {
                                msg.what = 89;
                                bundle.putLong("ft.msg_id", msgItem.mMsgId);
                                break;
                            }
                        }
                        return;
                        break;
                    case R.id.reject_button:
                        if (msgItem.mImAttachmentStatus == Place.TYPE_POSTAL_TOWN) {
                            bundle.putLong("ft.msg_id", msgItem.mMsgId);
                            msg.what = 90;
                            break;
                        }
                        break;
                    case R.id.vcard_view:
                        RcsTransaction.sendFtReadReport(msgItem.mMsgId, RcsFileTransMessageListItem.serialVersionUID);
                        if (msgItem.isVCardFile()) {
                            if (!msgItem.mIsOutgoing) {
                                msgItem.saveVcard();
                                break;
                            } else {
                                msgItem.showVCardDetailDialog();
                                break;
                            }
                        }
                        break;
                    case R.id.audio_view:
                        RcsFileTransMessageListItem.this.processAudioClick();
                        break;
                }
                msg.obj = bundle;
                RcsFileTransMessageListItem.this.mRcseEventHandler.sendMessage(msg);
            }
        });
    }

    public void processAudioClick() {
        if (!this.mFileTransMessageItem.mIsOutgoing && this.mFileTransMessageItem.mImAttachmentStatus == 1002 && RcsTransaction.updateAudioReadStatus(this.mContext, this.mFileTransMessageItem.mFileTransId) > 0) {
            RcsTransaction.sendFtReadReport(this.mFileTransMessageItem.mMsgId, serialVersionUID);
            this.mFileTransMessageItem.mImAttachmentStatus = Place.TYPE_ROUTE;
            setAudioIconVisibility(8);
        }
        if (this.mRecorderManager.isInPlayingstate()) {
            this.mRecorderManager.stopAudio();
            return;
        }
        File audioFile = this.mFileTransMessageItem.getAttachmentFile();
        if (audioFile != null) {
            this.mRecorderManager.setAudioUri(Uri.fromFile(audioFile));
        }
        this.mRecorderManager.playAudio();
    }

    public RcsFileTransMessageItem getFileTransMessageItem() {
        return this.mFileTransMessageItem;
    }

    private void setTextForTransNameTextView(String mImAttachmentPath) {
        if (mImAttachmentPath != null) {
            this.mTransNameTextView.setText(mImAttachmentPath.substring(mImAttachmentPath.lastIndexOf(47) + 1));
        }
    }

    private void setmFileTransImageSrc(RcsFileTransMessageItem msgItem) {
        if (!msgItem.isVCardFile() || this.mVattchImage == null) {
            this.mFileTransImageSrc.setImageBitmap(null);
            if (msgItem.mFileIcon != null) {
                this.mFileTransImageSrc.setImageBitmap(msgItem.mFileIcon);
                if (!msgItem.isVideoFileType()) {
                    this.mFileTransImageSrc.hideFileIcon();
                }
            } else {
                try {
                    if (msgItem.isImageFileType()) {
                        this.mFileTransImageSrc.showFileIcon(7, this.mContext);
                    } else if (msgItem.isVideoFileType()) {
                        this.mFileTransImageSrc.showFileIcon(8, this.mContext);
                    } else if (msgItem.isAudioFileType()) {
                        this.mFileTransImageSrc.showFileIcon(9, this.mContext);
                    } else {
                        this.mFileTransImageSrc.showFileIcon(20, this.mContext);
                    }
                } catch (OutOfMemoryError e) {
                    try {
                        Class.forName(System.class.getName()).getMethod("gc", new Class[0]).invoke(null, null);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return;
        }
        this.mVattchImage.setImageBitmap(msgItem.mFileIcon);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private void hideAllFileTransView(RcsFileTransMessageItem msgItem) {
        if (this.mRecvFileFailedLayout != null) {
            this.mRecvFileFailedLayout.setVisibility(8);
        }
        if (this.mRImageFrame != null) {
            this.mRImageFrame.setVisibility(8);
        }
        if (this.mAcceptBtn != null) {
            this.mAcceptBtn.setVisibility(8);
        }
        if (this.mAlwaysAcceptBtn != null) {
            this.mAlwaysAcceptBtn.setVisibility(8);
        }
        if (this.alwayAcceptTextview != null) {
            if (MmsApp.getDefaultTelephonyManager().isNetworkRoaming()) {
                this.alwayAcceptTextview.setText(getResources().getString(R.string.roam_auto_accept_file_title));
            } else {
                this.alwayAcceptTextview.setText(getResources().getString(R.string.open_auto_accept));
            }
            this.alwayAcceptTextview.setVisibility(8);
        }
        if (this.mRejectBtn != null) {
            this.mRejectBtn.setVisibility(8);
        }
        if (this.mCancelBtn != null) {
            this.mCancelBtn.setVisibility(8);
        }
        if (this.mRejectTextView != null) {
            this.mRejectTextView.setVisibility(8);
        }
        if (this.mTransInfoTextView != null) {
            this.mTransInfoTextView.setText("");
            this.mTransInfoTextView.setVisibility(8);
            this.mTransNameTextView.setVisibility(8);
            this.mFileTransImageSrc.setVisibility(8);
            this.mFileTransImageSrc.hideFileIcon();
            this.mFileTransImageSrc.hideMediaFileInfo();
        }
        if (this.mFileSize != null) {
            this.mFileSize.setVisibility(8);
        }
        if (this.mFailedIndicator != null) {
            this.mFailedIndicator.setVisibility(8);
        }
        if (this.clickView != null) {
            this.clickView.setVisibility(8);
        }
        if (this.mResendIcon != null) {
            this.mResendIcon.setVisibility(8);
        }
        hideAllSpecailFileTransView(msgItem);
    }

    private void hideAllSpecailFileTransView(RcsFileTransMessageItem msgItem) {
        switch (msgItem.mFileTransType) {
            case 7:
            case 8:
                if (this.mVAttchView != null) {
                    this.mVAttchView.setVisibility(8);
                }
                if (this.mAudioAttchView != null) {
                    this.mAudioAttchView.setVisibility(8);
                }
                if (this.rcs_common_layout != null) {
                    this.rcs_common_layout.setVisibility(0);
                    return;
                }
                return;
            case 9:
                if (this.mVAttchView != null) {
                    this.mVAttchView.setVisibility(8);
                }
                if (this.rcs_common_layout != null) {
                    this.rcs_common_layout.setVisibility(8);
                }
                if (this.mAudioAttchView != null) {
                    this.mAudioAttchView.setVisibility(0);
                    return;
                }
                return;
            case 10:
                if (this.mVAttchView != null) {
                    this.mVAttchView.setVisibility(0);
                }
                if (this.rcs_common_layout != null) {
                    this.rcs_common_layout.setVisibility(8);
                }
                if (this.mAudioAttchView != null) {
                    this.mAudioAttchView.setVisibility(8);
                    return;
                }
                return;
            default:
                if (this.mVAttchView != null) {
                    this.mVAttchView.setVisibility(8);
                }
                if (this.mAudioAttchView != null) {
                    this.mAudioAttchView.setVisibility(8);
                }
                if (this.rcs_common_layout != null) {
                    this.rcs_common_layout.setVisibility(0);
                }
                inflateCommonView(msgItem);
                return;
        }
    }

    private void receiveFile(RcsFileTransMessageItem msgItem) {
        if (msgItem.mImAttachmentStatus == Place.TYPE_POSTAL_TOWN) {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putLong("fileTransId", msgItem.mFileTransId);
            bundle.putLong("totalSize", msgItem.mImAttachmentTotalSize);
            bundle.putLong("ft.msg_id", msgItem.mMsgId);
            msg.what = 89;
            msg.obj = bundle;
            RcsProfileUtils.setAutoAcceptFile(this.mContext, true);
            RcsProfile.setftFileAceeptSwitch(this.mContext, 1, "pref_key_auto_accept_file");
            this.mRcseEventHandler.sendMessage(msg);
        }
    }

    private void refreshViewByLocation(RcsFileTransMessageItem msgItem) {
    }

    public void refreshListItem(boolean isScrolling) {
        getFileTransMessageItem().createAttachmentForRcse(MmsApp.getApplication().getApplicationContext(), isScrolling);
        bind(getMessageItem(), false, 0, this.mIsMultiSimActive);
    }

    public void setFileTransHandler(Handler handler) {
        this.mRcseEventHandler = handler;
    }

    public void setMultiChoice(boolean isMultiChoice) {
        this.mIsMultiChoice = isMultiChoice;
    }

    public void setThreadId(long threadId) {
        this.mThreadId = threadId;
    }

    public void showCommonStatusView(RcsFileTransMessageItem msgItem) {
        if (msgItem == null) {
            MLog.w("RcsFileTransMessageListItem FileTrans: ", "showCommonStatusView messageItemData is null");
            return;
        }
        switch (msgItem.mFileTransType) {
            case 7:
            case 8:
                showCommonStatusDefaultView(msgItem);
                this.mTransNameTextView.setVisibility(8);
                this.mFileTransImageSrc.showMediaFileInfo(Formatter.formatFileSize(this.mContext, msgItem.mImAttachmentTotalSize));
                this.mTransInfoTextView.setVisibility(8);
                this.mFileSize.setVisibility(8);
                break;
            case 9:
                setAudioDurationView(msgItem);
                this.mResendIcon.setVisibility(0);
                break;
            case 10:
                if (msgItem.mIsOutgoing) {
                    setVattchViewImageSrc(msgItem);
                    this.mResendIcon.setVisibility(0);
                    break;
                }
                showDefualtVcardView();
                if (!(1001 == msgItem.mImAttachmentStatus || 1010 == msgItem.mImAttachmentStatus)) {
                    if (1009 == msgItem.mImAttachmentStatus) {
                    }
                    if (this.mRecvFileFailedLayout != null) {
                        this.mRecvFileFailedLayout.setVisibility(0);
                        break;
                    }
                }
                this.mResendIcon.setVisibility(0);
                if (this.mRecvFileFailedLayout != null) {
                    this.mRecvFileFailedLayout.setVisibility(0);
                }
                break;
            default:
                showCommonStatusDefaultView(msgItem);
                break;
        }
    }

    private void showRejectView() {
        if (this.mFileTransImageSrc == null) {
            MLog.e("RcsFileTransMessageListItem FileTrans: ", " showRejectView mFileTransImageSrc is null");
            return;
        }
        this.mFileTransImageSrc.setVisibility(0);
        this.mFileTransImageSrc.hideProgressBar();
        this.mFileTransImageSrc.showRejectIcon(this.mContext);
    }

    private void showCommonStatusDefaultView(RcsFileTransMessageItem msgItem) {
        this.mTransInfoTextView.setVisibility(8);
        this.mTransNameTextView.setVisibility(8);
        this.mFileSize.setVisibility(0);
        this.mTransNameTextView.setVisibility(0);
        this.mTransInfoTextView.setTextSize(14.0f);
        setTextForTransNameTextView(getFileAttachmentPath(msgItem));
        this.mFileTransImageSrc.setVisibility(0);
        this.mFileTransImageSrc.hideProgressBar();
        setmFileTransImageSrc(msgItem);
        if (msgItem.mIsOutgoing) {
            this.mResendIcon.setVisibility(0);
        } else {
            if (!(1001 == msgItem.mImAttachmentStatus || 1010 == msgItem.mImAttachmentStatus)) {
                if (1009 == msgItem.mImAttachmentStatus) {
                }
            }
            this.mResendIcon.setVisibility(8);
        }
        if (this.mRecvFileFailedLayout != null) {
            this.mRecvFileFailedLayout.setVisibility(0);
        }
        if (msgItem.isVideoFileType() && (msgItem.mIsOutgoing || (1001 != msgItem.mImAttachmentStatus && 1010 != msgItem.mImAttachmentStatus))) {
            this.mFileTransImageSrc.showVideoIcon(this.mContext);
        }
    }

    public void showProcessingStatusView(RcsFileTransMessageItem msgItem) {
        if (msgItem == null) {
            MLog.w("RcsFileTransMessageListItem FileTrans: ", "showProcessingStatusView messageItemData is null");
            return;
        }
        switch (msgItem.mFileTransType) {
            case 7:
            case 8:
                showProcessingStatusefaultView(msgItem);
                this.mTransNameTextView.setVisibility(8);
                break;
            case 9:
                setAudioDurationView(msgItem);
                break;
            case 10:
                if (!msgItem.mIsOutgoing) {
                    showDefualtVcardView();
                    break;
                } else {
                    setVattchViewImageSrc(msgItem);
                    break;
                }
            default:
                showProcessingStatusefaultView(msgItem);
                break;
        }
    }

    private void showProcessingStatusefaultView(RcsFileTransMessageItem msgItem) {
        this.mCancelBtn.setVisibility(0);
        String mImAttachmentPath = getFileAttachmentPath(msgItem);
        this.mTransInfoTextView.setVisibility(0);
        if (this.mLastMsgID != msgItem.mMsgId) {
            this.mLastMsgID = msgItem.mMsgId;
            this.mLastTransSize = 0;
        }
        if (msgItem.mImAttachmentTransSize < this.mLastTransSize) {
            MLog.d("RcsFileTransMessageListItem FileTrans: ", "currentSize is less than lasttransSize currentSize = " + msgItem.mImAttachmentTransSize + " lasttransSize = " + this.mLastTransSize);
            msgItem.mImAttachmentTransSize = this.mLastTransSize;
        } else {
            this.mLastTransSize = msgItem.mImAttachmentTransSize;
        }
        this.mTransInfoTextView.setText(RcsUtility.formatFileSizeWithoutUnit(msgItem.mImAttachmentTransSize, msgItem.mImAttachmentTotalSize) + "/" + Formatter.formatFileSize(this.mContext, msgItem.mImAttachmentTotalSize));
        int progress = msgItem.mImAttachmentTotalSize != 0 ? (int) ((msgItem.mImAttachmentTransSize * 100) / msgItem.mImAttachmentTotalSize) : 0;
        this.mFileTransImageSrc.setVisibility(0);
        this.mFileTransImageSrc.setProgress(progress);
        this.mTransNameTextView.setVisibility(0);
        this.mTransInfoTextView.setTextSize(12.0f);
        setTextForTransNameTextView(mImAttachmentPath);
        if (msgItem.mIsOutgoing) {
            this.mCancelBtn.setTextColor(getResources().getColor(R.color.black));
            if (progress == 99) {
                this.mCancelBtn.setClickable(false);
                this.mCancelBtn.setTextColor(getResources().getColor(R.color.text_gray_black_color));
            }
        }
        setmFileTransImageSrc(msgItem);
        if (this.mRecvFileFailedLayout != null) {
            this.mRecvFileFailedLayout.setVisibility(8);
        }
        this.mFileTransImageSrc.hideFileIcon();
    }

    private void showDefualtVcardView() {
        this.mVattchImage.setImageResource(R.drawable.rcs_ic_contact_picture_holo_dark);
        this.mVattchNodesLayout.setVisibility(8);
    }

    private void showFiletranSendLast(RcsFileTransMessageItem msgItem) {
        switch (msgItem.mFileTransType) {
            case 7:
            case 8:
                showFiletranSendLastDefaultView(msgItem);
                this.mTransNameTextView.setVisibility(8);
                return;
            case 9:
            case 10:
                return;
            default:
                showFiletranSendLastDefaultView(msgItem);
                return;
        }
    }

    private void showFiletranSendLastDefaultView(RcsFileTransMessageItem msgItem) {
        this.mTransInfoTextView.setVisibility(0);
        this.mTransInfoTextView.setText(Formatter.formatFileSize(this.mContext, msgItem.mImAttachmentTransSize) + "/" + Formatter.formatFileSize(this.mContext, msgItem.mImAttachmentTotalSize));
        int progress = msgItem.mImAttachmentTotalSize != 0 ? (int) ((msgItem.mImAttachmentTransSize * 100) / msgItem.mImAttachmentTotalSize) : 0;
        this.mFileTransImageSrc.setVisibility(0);
        this.mFileTransImageSrc.setProgress(progress);
        this.mTransNameTextView.setVisibility(0);
        this.mTransInfoTextView.setTextSize(12.0f);
        setTextForTransNameTextView(getFileAttachmentPath(msgItem));
        setmFileTransImageSrc(msgItem);
        this.mFileTransImageSrc.hideFileIcon();
    }

    private void showUndelivedStatusView(RcsFileTransMessageItem msgItem) {
        showCommonStatusView(msgItem);
        if (RcsTransaction.isShowUndeliveredIcon()) {
            this.mFailedIndicator.setVisibility(0);
            this.mResendIcon.setVisibility(8);
            this.mFailedIndicator.setOnClickListener(this.mMsgOnUndelivedClickListener);
        }
    }

    private void setFieSizeAndType(RcsFileTransMessageItem msgItem) {
        this.mFileSize.setText(Formatter.formatFileSize(this.mContext, msgItem.mImAttachmentTotalSize));
        this.mDateView.setVisibility(!this.mNeedHideUnderPopView ? 0 : 8);
    }

    private String getFileAttachmentPath(RcsFileTransMessageItem msgItem) {
        String mImAttachmentPath = msgItem.mImAttachmentPath;
        if (mImAttachmentPath == null) {
            return msgItem.mImAttachmentContent;
        }
        return mImAttachmentPath;
    }

    public boolean onDoubleTapUp(boolean isLink) {
        return false;
    }

    public void onTouchLink(ClickableSpan span) {
    }

    private void setFileTransImageClickListener(RcsFileTransProgressBar fileTransImage, RcsFileTransMessageItem msgItem) {
        if (fileTransImage != null && msgItem != null) {
            if (isDelayMessageStatus()) {
                if (this.mMmsClickListener == null) {
                    this.mMmsClickListener = new MmsClickListener(new ItemTouchListener(msgItem));
                }
                this.mMmsClickListener.setClickListener(fileTransImage);
            } else {
                setOnClickListener(fileTransImage, msgItem);
            }
        }
    }

    private void setVcardClickListener(View vAttchView, RcsFileTransMessageItem msgItem) {
        if (vAttchView != null && msgItem != null) {
            if (isDelayMessageStatus()) {
                if (this.mMmsClickListener == null) {
                    this.mMmsClickListener = new MmsClickListener(new ItemTouchListener(msgItem));
                }
                this.mMmsClickListener.setClickListener(vAttchView);
            } else {
                setOnClickListener(vAttchView, msgItem);
            }
        }
    }

    private void setAudioClickListener(View vAudioView, RcsFileTransMessageItem msgItem) {
        if (vAudioView != null && msgItem != null) {
            if (isDelayMessageStatus()) {
                if (this.mMmsClickListener == null) {
                    this.mMmsClickListener = new MmsClickListener(new ItemTouchListener(msgItem));
                }
                this.mMmsClickListener.setClickListener(vAudioView);
            } else {
                setOnClickListener(vAudioView, msgItem);
            }
        }
    }

    private boolean isDelayMessageStatus() {
        boolean isDelayMessage = false;
        if (!(getRcsMessageListItem() == null || getRcsMessageListItem().getHwCustCallback() == null)) {
            isDelayMessage = getRcsMessageListItem().getHwCustCallback().isDelayMessage();
        }
        MLog.d("RcsFileTransMessageListItem", "isDelayMessageStatus  = " + isDelayMessage);
        if (this.mFileTransMessageItem == null || this.mFileTransMessageItem.isNotDelayMsg() || !isDelayMessage) {
            return false;
        }
        return true;
    }

    public void unbind() {
        if (this.mMmsClickListener != null) {
            this.mMmsClickListener.removeClickListener();
        }
    }
}
