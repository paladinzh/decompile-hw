package com.huawei.rcs.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.MarginLayoutParams;
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
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.android.rcs.ui.RcsGroupChatMessageListItem.GroupChatMsgListItemCallback;
import com.android.rcs.ui.RcsMessageUtils;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.MmsClickListener;
import com.huawei.mms.ui.MmsClickListener.IMmsClickListener;
import com.huawei.rcs.media.RcsMediaFileUtils;
import com.huawei.rcs.ui.RcsAsyncIconLoader.OnIconLoadedCallback;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsProfileUtils;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcsUtility;
import java.io.File;
import java.util.HashMap;

public class RcsFileTransGroupMessageListItem extends LinearLayout implements OnIconLoadedCallback {
    private static final long serialVersionUID = 1;
    private int FILERECEIVE = 101;
    private int FILESEND = 100;
    private long groupChat = 2;
    private Handler handler = new Handler();
    private Button mAcceptBtn;
    private CheckBox mAlawaysAcceptBtn;
    private TextView mAlawaysAcceptTextView;
    private View mAudioAttchView;
    private TextView mAudioDurationView;
    private RcsRoundProgressBar mAudioReceiveBar;
    private ImageView mAudioattchImage;
    private GroupChatMsgListItemCallback mCallback = null;
    private Button mCancelBtn;
    private Context mContext;
    private TextView mFileSize;
    private RcsFileTransProgressBar mFileTransImageSrc;
    private transient RcsFileTransGroupMessageItem mFileTransMessageItem;
    RcsGroupChatComposeMessageFragment mGroupFrg = null;
    private boolean mIsMultiChoice = false;
    private long mLastMsgID = -1;
    private long mLastTransSize = 0;
    private LinearLayout mRImageFrame;
    private RecorderManager mRecorderManager = null;
    private Button mRejectBtn;
    private TextView mRejectTextView;
    private long mThreadId = 0;
    public TextView mTransInfoTextView;
    private TextView mTransNameTextView;
    private View mVAttchView;
    private ImageView mVattchImage;
    LinearLayout mVattchNodesLayout;
    private RelativeLayout rcs_common_layout;
    public HashMap<Long, Boolean> state = new HashMap();

    private class ItemTouchListener implements IMmsClickListener {
        private RcsFileTransGroupMessageItem mMsgItem;

        public ItemTouchListener(RcsFileTransGroupMessageItem msgItem) {
            this.mMsgItem = msgItem;
        }

        public void onDoubleClick(View view) {
            if (RcsFileTransGroupMessageListItem.this.mCallback == null || !this.mMsgItem.isDelayMsg()) {
                RcsFileTransGroupMessageListItem.this.processSingleClick(view, this.mMsgItem);
            } else {
                RcsFileTransGroupMessageListItem.this.mCallback.cancleDelayMsg();
            }
        }

        public void onSingleClick(View view) {
            RcsFileTransGroupMessageListItem.this.processSingleClick(view, this.mMsgItem);
        }
    }

    public RcsFileTransGroupMessageListItem(Context context) {
        super(context);
        this.mContext = context;
    }

    public RcsFileTransGroupMessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void setConversationId(long threadId) {
        this.mThreadId = threadId;
    }

    public void bind(RcsFileTransGroupMessageItem messageIte) {
        bind(messageIte, false);
    }

    public void bind(RcsFileTransGroupMessageItem messageItem, boolean isMultiChoice) {
        this.mFileTransMessageItem = messageItem;
        bindCommonMessage(messageItem);
        if (getTag() == null || !(getTag() instanceof Boolean)) {
            asyncLoadImageIcon(this.mFileTransMessageItem);
        }
    }

    private void asyncLoadImageIcon(RcsFileTransGroupMessageItem ftMsgItem) {
        if (ftMsgItem == null) {
            MLog.w("RcsFileTransGroupMessageListItem FileTrans: ", " asyncLoadImageIcon -> group ftMsgItem is null, return.");
            return;
        }
        RcsImageCache cache = RcsFileTransGroupMessageItem.getmCache();
        String sCacheKey = RcsUtility.getBitmapFromMemCacheKey(ftMsgItem.mMsgId, ftMsgItem.mChatType);
        if (ftMsgItem.mFileIcon != null) {
            if (cache != null && cache.getBitmapFromMemCache(sCacheKey) != null) {
                MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", " asyncLoadImageIcon - > group find in cache. do not need to change.");
                return;
            } else if (!(ftMsgItem.mIsOutgoing || ftMsgItem.mImAttachmentStatus == 1002 || ftMsgItem.mImAttachmentStatus == Place.TYPE_ROUTE)) {
                MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", " asyncLoadImageIcon - > group receive not OK. do not need to change.");
                return;
            }
        } else if (ftMsgItem.mImAttachmentStatus == 1009) {
            MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", " asyncLoadImageIcon - > reject,do not need to change.");
            return;
        } else if (!(ftMsgItem.mIsOutgoing || ftMsgItem.mImAttachmentStatus == 1002 || ftMsgItem.mImAttachmentStatus == Place.TYPE_ROUTE || (ftMsgItem.mImAttatchmentIcon != null && !TextUtils.isEmpty(ftMsgItem.mImAttatchmentIcon) && new File(ftMsgItem.mImAttatchmentIcon).exists()))) {
            MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", " asyncLoadImageIcon - > mFileIcon is null and group receive not OK. do not need to change.");
            return;
        }
        MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", " asyncLoadImageIcon - > group we need to async load icon.");
        if (ftMsgItem.mImAttatchmentIcon != null && !TextUtils.isEmpty(ftMsgItem.mImAttatchmentIcon) && new File(ftMsgItem.mImAttatchmentIcon).exists()) {
            RcsAsyncIconLoader.getInstance().asyncLoadIcon(sCacheKey, ftMsgItem.mImAttatchmentIcon, this);
            MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", " asyncLoadImageIcon - > we need to async load icon in Pre_ThumbNail Mode");
        } else if (!TextUtils.isEmpty(ftMsgItem.mImAttachmentPath) && new File(ftMsgItem.mImAttachmentPath).exists()) {
            MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", " asyncLoadImageIcon - > we need to async load icon in No_Pre_ThumbNail Mode");
            RcsAsyncIconLoader.getInstance().asyncLoadIcon(sCacheKey, ftMsgItem.mImAttachmentPath, this);
        }
    }

    private void bindCommonMessage(RcsFileTransGroupMessageItem msgItem) {
        inflateFileTransferView(msgItem);
        hideAllFileTransView(msgItem);
        refreshViewByFtStatus(msgItem);
        if (this.mRImageFrame != null) {
            this.mRImageFrame.setVisibility(0);
        }
    }

    public void onIconLoaded(String cacheKey, Bitmap bitmap, boolean isDefaultIcon) {
        if (bitmap != null) {
            String sCacheKey = "";
            if (this.mFileTransMessageItem != null) {
                sCacheKey = RcsUtility.getBitmapFromMemCacheKey(this.mFileTransMessageItem.mMsgId, this.mFileTransMessageItem.mChatType);
            }
            if (!isDefaultIcon) {
                RcsImageCache imageCache = RcsFileTransGroupMessageItem.getmCache();
                if (imageCache != null) {
                    imageCache.addBitmapToCache(cacheKey, bitmap);
                }
            }
            if (this.mFileTransMessageItem != null && TextUtils.equals(sCacheKey, cacheKey)) {
                MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", " onIconLoaded -> group the current bind msg item is the same that need to create the icon. sCacheKey = " + sCacheKey);
                this.mFileTransMessageItem.mFileIcon = bitmap;
                if (this.handler == null) {
                    MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", " onIconLoaded -> handler is null.");
                    return;
                }
                this.handler.post(new Runnable() {
                    public void run() {
                        RcsFileTransGroupMessageListItem.this.setmFileTransImageSrc(RcsFileTransGroupMessageListItem.this.mFileTransMessageItem);
                    }
                });
            }
        }
    }

    private void inflateFileTransferView(RcsFileTransGroupMessageItem msgItem) {
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

    private void inflateCommonView(RcsFileTransGroupMessageItem msgItem) {
        if (this.mRImageFrame == null) {
            this.mRImageFrame = (LinearLayout) findViewById(R.id.rImageFrame);
            this.mAcceptBtn = (Button) findViewById(R.id.accept_button);
            this.mRejectBtn = (Button) findViewById(R.id.reject_button);
            this.mCancelBtn = (Button) findViewById(R.id.cancel_button);
            this.mAlawaysAcceptBtn = (CheckBox) findViewById(R.id.always_accept_button);
            this.mTransInfoTextView = (TextView) findViewById(R.id.file_transInfo);
            this.mTransNameTextView = (TextView) findViewById(R.id.file_transName);
            this.mFileTransImageSrc = (RcsFileTransProgressBar) findViewById(R.id.fileTransfer_image_src);
            this.mFileTransImageSrc.setContentDescription(getResources().getString(R.string.rcs_view_slideshow));
            this.mFileSize = (TextView) findViewById(R.id.txv_file_size);
            this.mAlawaysAcceptTextView = (TextView) findViewById(R.id.alwayAcceptTextview);
            this.mRejectTextView = (TextView) findViewById(R.id.rejectTextview);
        }
    }

    private void inflateSpecialView(RcsFileTransGroupMessageItem msgItem) {
        inflateCommonViewCust(msgItem);
        if (msgItem.isVCardFileTypeMsg()) {
            inflateVCardTransferView(msgItem);
            setVcardIndicateVisible(msgItem);
        } else if (msgItem.isAudioFileType()) {
            inflateAudioTransferView(msgItem);
            setAudioIndicateVisible(msgItem);
        }
    }

    private void inflateCommonViewCust(RcsFileTransGroupMessageItem msgItem) {
        this.rcs_common_layout = (RelativeLayout) findViewById(R.id.rcs_common_view);
        if (this.rcs_common_layout != null) {
            this.rcs_common_layout.setVisibility(8);
        }
    }

    private void inflateVCardTransferView(RcsFileTransGroupMessageItem msgItem) {
        View rcsView = findViewById(R.id.rcs_especial_view);
        rcsView.setVisibility(0);
        this.mVAttchView = rcsView.findViewById(R.id.vcard_view);
        this.mVAttchView.setVisibility(0);
        this.mVattchImage = (ImageView) this.mVAttchView.findViewById(R.id.vcard_attach_image);
        this.mVattchNodesLayout = (LinearLayout) this.mVAttchView.findViewById(R.id.vcard_attach_nodes);
    }

    private boolean checkAudioReceiving(RcsFileTransGroupMessageItem msgItem) {
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

    private void inflateAudioTransferView(RcsFileTransGroupMessageItem msgItem) {
        View rcsView = findViewById(R.id.rcs_especial_view);
        rcsView.setVisibility(0);
        this.mAudioAttchView = rcsView.findViewById(R.id.audio_view);
        this.mAudioAttchView.setVisibility(0);
        if (msgItem.mIsOutgoing) {
            this.mAudioDurationView = (TextView) this.mAudioAttchView.findViewById(R.id.rcs_audio_anima_duration_send);
            this.mAudioattchImage = (ImageView) this.mAudioAttchView.findViewById(R.id.rcs_audio_anima_image_view_send);
        } else {
            this.mAudioDurationView = (TextView) this.mAudioAttchView.findViewById(R.id.rcs_audio_anima_duration_receiver);
            this.mAudioattchImage = (ImageView) this.mAudioAttchView.findViewById(R.id.rcs_audio_anima_image_view_receiver);
        }
        this.mAudioReceiveBar = (RcsRoundProgressBar) this.mAudioAttchView.findViewById(R.id.round_progress_bar);
        updateAudioReceiveView(checkAudioReceiving(msgItem));
        if (this.mRecorderManager == null) {
            this.mRecorderManager = new RecorderManager(MmsApp.getApplication().getApplicationContext());
            this.mRecorderManager.mAudioAnimaImageView = this.mAudioattchImage;
            String filepath = getFileAttachmentPath(msgItem);
            File file = null;
            if (!TextUtils.isEmpty(filepath)) {
                file = new File(filepath);
            }
            if (file != null) {
                this.mRecorderManager.setAudioUri(Uri.fromFile(file));
            }
        }
    }

    private void setVcardIndicateVisible(RcsFileTransGroupMessageItem msgItem) {
        setOnClickListener(this.mVAttchView, msgItem);
        this.mVAttchView.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                RcsFileTransGroupMessageListItem.this.dismiss();
                return v.showContextMenu();
            }
        });
    }

    private void setAudioIndicateVisible(RcsFileTransGroupMessageItem msgItem) {
        setOnClickListener(this.mAudioAttchView, msgItem);
        this.mAudioAttchView.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                RcsFileTransGroupMessageListItem.this.dismiss();
                return v.showContextMenu();
            }
        });
    }

    private void refreshViewByFtStatus(RcsFileTransGroupMessageItem msgItem) {
        setListener(msgItem);
        refreshViewAnyStatus(msgItem);
    }

    private void setListener(final RcsFileTransGroupMessageItem msgItem) {
        setOnClickListener(this.mAcceptBtn, msgItem);
        setOnClickListener(this.mRejectBtn, msgItem);
        setOnClickListener(this.mCancelBtn, msgItem);
        setOnClickListener(this.mFileTransImageSrc, msgItem);
        setOnLongClickListener(this.mFileTransImageSrc, msgItem);
        this.mAlawaysAcceptBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && RcsProfile.rcsIsLogin()) {
                    if (RcsFileTransGroupMessageListItem.this.mGroupFrg != null) {
                        RcsGroupChatComposeMessageFragment rcsGroupChatComposeMessageFragment = RcsFileTransGroupMessageListItem.this.mGroupFrg;
                        if (RcsGroupChatComposeMessageFragment.isExitRcsGroupEnable()) {
                            RcsFileTransGroupMessageListItem.this.mAlawaysAcceptBtn.setChecked(false);
                            return;
                        }
                    }
                    RcsFileTransGroupMessageListItem.this.receiveFile(msgItem);
                }
            }
        });
    }

    public void refreshViewAnyStatus(RcsFileTransGroupMessageItem msgItem) {
        setFieSizeAndType(msgItem);
        MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", "refreshViewAnyStatus  mMsgId = " + msgItem.mMsgId + " and Status = " + msgItem.mImAttachmentStatus + "type = " + msgItem.mFileTransType);
        switch (msgItem.mImAttachmentStatus) {
            case 1000:
            case 1007:
                showProcessingStatusView(msgItem);
                return;
            case 1001:
            case 1010:
            case Place.TYPE_PREMISE /*1018*/:
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
            case Place.TYPE_POSTAL_TOWN /*1017*/:
                showWaitAcceptFileView(msgItem);
                return;
            default:
                MLog.w("RcsFileTransGroupMessageListItem FileTrans: ", " Such status does not exist in refreshViewAnyStatus");
                return;
        }
    }

    private void showRejectView() {
        if (this.mFileTransImageSrc == null) {
            MLog.e("RcsFileTransGroupMessageListItem FileTrans: ", " showRejectView mFileTransImageSrc is null");
            return;
        }
        this.mFileTransImageSrc.setVisibility(0);
        this.mFileTransImageSrc.hideProgressBar();
        this.mFileTransImageSrc.showRejectIcon(this.mContext);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleWhenClickFile(RcsFileTransGroupMessageItem msgItem, Message aMsg, Bundle aBundle) {
        Intent intent = new Intent("android.intent.action.VIEW");
        if (msgItem.isLocation()) {
            intent.setData(Uri.parse(changeUri(msgItem.mImAttachmentContent)));
        } else {
            if (new File(msgItem.mImAttachmentPath).exists()) {
                if (!(msgItem.mImAttachmentStatus == 1010 || msgItem.mImAttachmentStatus == 1001)) {
                    if (msgItem.mImAttachmentStatus == 1009) {
                    }
                    if (msgItem.mFileTransType == 9) {
                        Intent viewImageInatent = new Intent("android.intent.action.VIEW");
                        viewImageInatent.putExtra("msgId", msgItem.mMsgId);
                        Uri Fileuri = RcsProfileUtils.getFileContentUri(this.mContext, new File(msgItem.mImAttachmentPath));
                        viewImageInatent.putExtra("SingleItemOnly", true);
                        viewImageInatent.setDataAndType(Fileuri, RcsMediaFileUtils.getFileMimeType(msgItem.mImAttachmentPath));
                        this.mContext.startActivity(viewImageInatent);
                        return;
                    } else if (msgItem.mFileTransType == 7) {
                        Bundle bd = new Bundle();
                        bd.putLong("thread_id", this.mThreadId);
                        bd.putInt("load_type", msgItem.mLoadType);
                        bd.putInt("chat_type", msgItem.mChatType);
                        RcsProfileUtils.viewImageFile(this.mContext, msgItem.mImAttachmentPath, bd);
                        return;
                    } else if (msgItem.mFileTransType == 10) {
                        saveOrViewDetailOfVCard(msgItem);
                        return;
                    } else {
                        File tempFile = new File(msgItem.mImAttachmentPath);
                        if (msgItem.mFileTransType == 8) {
                            intent.setDataAndType(RcsProfileUtils.getVideoContentUri(this.mContext, tempFile), RcsMediaFileUtils.getFileMimeType(msgItem.mImAttachmentPath));
                        } else {
                            intent.setDataAndType(RcsProfileUtils.getFileContentUri(this.mContext, tempFile), RcsMediaFileUtils.getFileMimeType(msgItem.mImAttachmentPath));
                            intent.setFlags(1);
                        }
                    }
                }
            }
            Toast.makeText(this.mContext, R.string.text_file_not_exist, 0).show();
            return;
        }
        intent.putExtra("SingleItemOnly", true);
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

    private void setOnLongClickListener(View aButton, RcsFileTransGroupMessageItem msgItem) {
        aButton.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                switch (v.getId()) {
                    case R.id.fileTransfer_image_src:
                        RcsFileTransGroupMessageListItem.this.dismiss();
                        return false;
                    default:
                        RcsFileTransGroupMessageListItem.this.dismiss();
                        return v.showContextMenu();
                }
            }
        });
    }

    private void setOnClickListener(View aButton, RcsFileTransGroupMessageItem msgItem) {
        new MmsClickListener(new ItemTouchListener(msgItem)).setClickListener(aButton);
    }

    private void processSingleClick(View v, RcsFileTransGroupMessageItem msgItem) {
        if (!msgItem.isDelayMsg()) {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putLong("fileTransId", msgItem.mFileTransId);
            bundle.putLong("totalSize", msgItem.mImAttachmentTotalSize);
            switch (v.getId()) {
                case R.id.fileTransfer_image_src:
                    handleWhenClickFile(msgItem, msg, bundle);
                    return;
                case R.id.cancel_button:
                    this.mCancelBtn.setVisibility(8);
                    boolean isOutGoing = true;
                    if (msgItem.mtype == this.FILERECEIVE) {
                        isOutGoing = false;
                    }
                    RcsTransaction.cancelFT(msgItem.mMsgId, isOutGoing, this.groupChat);
                    this.mFileTransImageSrc.setProgress(0);
                    this.mFileTransImageSrc.hideProgressBar();
                    return;
                case R.id.always_accept_button:
                    receiveFile(msgItem);
                    if (!this.mAlawaysAcceptBtn.isChecked()) {
                        this.state.put(Long.valueOf(msgItem.mMsgId), Boolean.valueOf(false));
                        break;
                    } else {
                        this.state.put(Long.valueOf(msgItem.mMsgId), Boolean.valueOf(true));
                        break;
                    }
                case R.id.accept_button:
                    if (RcsProfile.rcsIsLogin()) {
                        if (msgItem.mImAttachmentStatus == Place.TYPE_POSTAL_TOWN) {
                            msg.what = 89;
                            if (!RcsTransaction.checksize(msgItem.mImAttachmentTotalSize, this.mContext)) {
                                RcsTransaction.acceptfile(msgItem.mMsgId, this.groupChat);
                                break;
                            }
                            MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", " storage is not enough,so reject file receive ");
                            FTisBig();
                            RcsTransaction.rejectFile(msgItem.mMsgId, this.groupChat);
                            break;
                        }
                    }
                    return;
                    break;
                case R.id.reject_button:
                    if (msgItem.mImAttachmentStatus == Place.TYPE_POSTAL_TOWN) {
                        msg.what = 90;
                        RcsTransaction.rejectFile(msgItem.mMsgId, this.groupChat);
                        break;
                    }
                    break;
                case R.id.vcard_view:
                    if (msgItem.isVCardFileTypeMsg()) {
                        saveOrViewDetailOfVCard(msgItem);
                        break;
                    }
                    break;
                case R.id.audio_view:
                    processAudioClick();
                    break;
            }
            msg.obj = bundle;
        }
    }

    public void processAudioClick() {
        if (!this.mFileTransMessageItem.mIsOutgoing && this.mFileTransMessageItem.mImAttachmentStatus == 1002 && RcsTransaction.updateAudioReadStatus(this.mContext, this.mFileTransMessageItem.mFileTransId) > 0) {
            this.mFileTransMessageItem.mImAttachmentStatus = Place.TYPE_ROUTE;
            if (this.mCallback != null) {
                this.mCallback.setAudioIconVisibility(8);
            }
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

    public RcsFileTransGroupMessageItem getMessageItem() {
        return this.mFileTransMessageItem;
    }

    private void setTextForTransNameTextView(String mImAttachmentPath) {
        if (mImAttachmentPath != null) {
            int index = mImAttachmentPath.lastIndexOf(47);
            MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", "setTextForTransNameTextView file name" + mImAttachmentPath.substring(index + 1));
            this.mTransNameTextView.setText(mImAttachmentPath.substring(index + 1));
        }
    }

    private void setmFileTransImageSrc(RcsFileTransGroupMessageItem msgItem) {
        if (!msgItem.isVCardFileTypeMsg() || this.mVattchImage == null) {
            this.mFileTransImageSrc.setImageBitmap(null);
            if (msgItem.mFileIcon != null) {
                this.mFileTransImageSrc.setImageBitmap(msgItem.mFileIcon);
                if (!msgItem.isVideoFileType()) {
                    this.mFileTransImageSrc.hideFileIcon();
                }
            } else if (msgItem.isImageFileType()) {
                this.mFileTransImageSrc.showFileIcon(7, this.mContext);
            } else if (msgItem.isVideoFileType()) {
                this.mFileTransImageSrc.showFileIcon(8, this.mContext);
            } else if (msgItem.isAudioFileType()) {
                this.mFileTransImageSrc.showFileIcon(9, this.mContext);
            } else {
                this.mFileTransImageSrc.showFileIcon(20, this.mContext);
            }
            return;
        }
        this.mVattchImage.setImageBitmap(msgItem.mFileIcon);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private void hideAllFileTransView(RcsFileTransGroupMessageItem msgItem) {
        if (this.mRImageFrame != null) {
            this.mRImageFrame.setVisibility(8);
        }
        if (this.mAcceptBtn != null) {
            this.mAcceptBtn.setVisibility(8);
        }
        if (this.mRejectBtn != null) {
            this.mRejectBtn.setVisibility(8);
        }
        if (this.mCancelBtn != null) {
            this.mCancelBtn.setVisibility(8);
        }
        if (this.mTransInfoTextView != null) {
            this.mTransInfoTextView.setText("");
            this.mTransInfoTextView.setVisibility(8);
            this.mTransNameTextView.setVisibility(8);
            this.mFileTransImageSrc.setVisibility(8);
            this.mFileTransImageSrc.hideFileIcon();
            this.mFileTransImageSrc.hideMediaFileInfo();
        }
        if (this.mRejectTextView != null) {
            this.mRejectTextView.setVisibility(8);
        }
        if (this.mFileSize != null) {
            this.mFileSize.setVisibility(8);
        }
        if (this.mAlawaysAcceptBtn != null) {
            this.mAlawaysAcceptBtn.setVisibility(8);
        }
        if (this.mAlawaysAcceptTextView != null) {
            if (MmsApp.getDefaultTelephonyManager().isNetworkRoaming()) {
                this.mAlawaysAcceptTextView.setText(getResources().getString(R.string.roam_auto_accept_file_title));
            } else {
                this.mAlawaysAcceptTextView.setText(getResources().getString(R.string.open_auto_accept));
            }
            this.mAlawaysAcceptTextView.setVisibility(8);
        }
        hideAllSpecailFileTransView(msgItem);
    }

    private void hideAllSpecailFileTransView(RcsFileTransGroupMessageItem msgItem) {
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

    private void receiveFile(RcsFileTransGroupMessageItem msgItem) {
        if (msgItem.mImAttachmentStatus == Place.TYPE_POSTAL_TOWN) {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putLong("fileTransId", msgItem.mFileTransId);
            bundle.putLong("totalSize", msgItem.mImAttachmentTotalSize);
            msg.what = 89;
            msg.obj = bundle;
            RcsTransaction.acceptfile(msgItem.mMsgId, this.groupChat);
            RcsProfileUtils.setAutoAcceptFile(this.mContext, true);
            RcsProfile.setftFileAceeptSwitch(this.mContext, 1, "pref_key_auto_accept_file");
        }
    }

    private void FTisBig() {
        new Builder(this.mContext).setTitle(R.string.storage_is_not_enough).setMessage(R.string.delete_file).setPositiveButton(R.string.I_know , new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                RcsFileTransGroupMessageListItem.this.dismiss();
            }
        }).show();
    }

    private void dismiss() {
        MLog.d("RcsFileTransGroupMessageListItem FileTrans: ", "disMiss for finbugs");
    }

    protected void detachAllViewsFromParent() {
        super.detachAllViewsFromParent();
        this.mFileTransImageSrc.setImageBitmap(null);
    }

    private void saveOrViewDetailOfVCard(RcsFileTransGroupMessageItem msgItem) {
        if (msgItem == null) {
            MLog.w("RcsFileTransGroupMessageListItem FileTrans: ", "saveOrViewDetailOfVCard: msgItem is null");
            return;
        }
        if (msgItem.mIsOutgoing) {
            msgItem.showVCardDetailDialog();
        } else {
            msgItem.saveVcard();
        }
    }

    public void showWaitAcceptFileView(RcsFileTransGroupMessageItem msgItem) {
        switch (msgItem.mFileTransType) {
            case 7:
            case 8:
                showWaitAcceptFileDefaultView(msgItem);
                this.mTransNameTextView.setVisibility(8);
                if (1 == msgItem.mLoadType) {
                    this.mAcceptBtn.setVisibility(8);
                    this.mAlawaysAcceptBtn.setVisibility(8);
                    this.mAlawaysAcceptTextView.setVisibility(8);
                    this.mRejectBtn.setVisibility(8);
                    this.mTransNameTextView.setVisibility(8);
                    this.mTransInfoTextView.setVisibility(8);
                    return;
                }
                return;
            case 9:
            case 10:
                return;
            default:
                if (1 == msgItem.mLoadType) {
                    showCommonStatusView(msgItem);
                    return;
                } else {
                    showWaitAcceptFileDefaultView(msgItem);
                    return;
                }
        }
    }

    private void showWaitAcceptFileDefaultView(RcsFileTransGroupMessageItem msgItem) {
        int i;
        int i2 = 8;
        Button button = this.mAcceptBtn;
        if (this.mIsMultiChoice) {
            i = 8;
        } else {
            i = 0;
        }
        button.setVisibility(i);
        CheckBox checkBox = this.mAlawaysAcceptBtn;
        if (this.mIsMultiChoice) {
            i = 8;
        } else {
            i = 0;
        }
        checkBox.setVisibility(i);
        TextView textView = this.mAlawaysAcceptTextView;
        if (this.mIsMultiChoice) {
            i = 8;
        } else {
            i = 0;
        }
        textView.setVisibility(i);
        setmFileTransImageSrc(msgItem);
        Button button2 = this.mRejectBtn;
        if (!this.mIsMultiChoice) {
            i2 = 0;
        }
        button2.setVisibility(i2);
        this.mTransNameTextView.setVisibility(0);
        setTextForTransNameTextView(getFileAttachmentPath(msgItem));
        this.mFileTransImageSrc.setVisibility(0);
        this.mFileTransImageSrc.hideProgressBar();
        this.mTransInfoTextView.setTextSize(12.0f);
        this.mTransInfoTextView.setVisibility(0);
        this.mTransInfoTextView.setText(Formatter.formatFileSize(this.mContext, msgItem.mImAttachmentTotalSize));
        if (this.state.get(Long.valueOf(msgItem.mMsgId)) == null || !((Boolean) this.state.get(Long.valueOf(msgItem.mMsgId))).booleanValue()) {
            this.mAlawaysAcceptBtn.setChecked(false);
        } else {
            this.mAlawaysAcceptBtn.setChecked(true);
        }
        checkIsExitGroupToClickableButton();
        if (this.mIsMultiChoice) {
            handleMultiChoice(msgItem);
        }
    }

    private void handleMultiChoice(RcsFileTransGroupMessageItem msgItem) {
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

    private void checkIsExitGroupToClickableButton() {
        if (this.mGroupFrg != null) {
            if (!this.mGroupFrg.getLoginStatus() || this.mGroupFrg.getOwnerStatus()) {
                RcsGroupChatComposeMessageFragment rcsGroupChatComposeMessageFragment = this.mGroupFrg;
                if (!RcsGroupChatComposeMessageFragment.isExitRcsGroupEnable()) {
                    this.mAcceptBtn.setEnabled(true);
                    this.mAcceptBtn.setTextColor(getResources().getColor(R.color.text_color_balck_sub));
                    this.mRejectBtn.setEnabled(true);
                    this.mRejectBtn.setTextColor(getResources().getColor(R.color.text_color_balck_sub));
                    this.mAlawaysAcceptBtn.setEnabled(true);
                    this.mAlawaysAcceptBtn.setTextColor(getResources().getColor(R.color.text_color_balck_sub));
                }
            }
            this.mAcceptBtn.setEnabled(false);
            this.mAcceptBtn.setTextColor(getResources().getColor(R.color.gray));
            this.mRejectBtn.setEnabled(false);
            this.mRejectBtn.setTextColor(getResources().getColor(R.color.gray));
            this.mAlawaysAcceptBtn.setEnabled(false);
            this.mAlawaysAcceptBtn.setTextColor(getResources().getColor(R.color.gray));
        }
    }

    public void showCommonStatusView(RcsFileTransGroupMessageItem msgItem) {
        if (msgItem == null) {
            MLog.w("RcsFileTransGroupMessageListItem", "showCommonStatusView messageItemData is null");
            return;
        }
        switch (msgItem.mFileTransType) {
            case 7:
            case 8:
                showCommonStatusDefaultView(msgItem);
                this.mTransNameTextView.setVisibility(8);
                if (1 != msgItem.mLoadType) {
                    this.mFileTransImageSrc.showMediaFileInfo(Formatter.formatFileSize(this.mContext, msgItem.mImAttachmentTotalSize));
                }
                this.mTransInfoTextView.setVisibility(8);
                this.mFileSize.setVisibility(8);
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
                showCommonStatusDefaultView(msgItem);
                break;
        }
    }

    private void showCommonStatusDefaultView(RcsFileTransGroupMessageItem msgItem) {
        this.mTransNameTextView.setVisibility(0);
        setTextForTransNameTextView(getFileAttachmentPath(msgItem));
        this.mTransInfoTextView.setTextSize(14.0f);
        this.mFileTransImageSrc.hideProgressBar();
        setmFileTransImageSrc(msgItem);
        this.mFileSize.setVisibility(0);
        this.mFileTransImageSrc.setVisibility(0);
        if (msgItem.isVideoFileType() && (msgItem.mIsOutgoing || (1001 != msgItem.mImAttachmentStatus && 1010 != msgItem.mImAttachmentStatus))) {
            this.mFileTransImageSrc.showVideoIcon(this.mContext);
        }
    }

    public void showProcessingStatusView(RcsFileTransGroupMessageItem msgItem) {
        if (msgItem == null) {
            MLog.w("RcsFileTransGroupMessageListItem", "showProcessingStatusView messageItemData is null");
            return;
        }
        switch (msgItem.mFileTransType) {
            case 7:
            case 8:
                showProcessingStatusefaultView(msgItem);
                this.mTransNameTextView.setVisibility(8);
                if (1 == msgItem.mLoadType) {
                    this.mFileTransImageSrc.hideProgressBar();
                    this.mTransInfoTextView.setVisibility(8);
                    this.mCancelBtn.setVisibility(8);
                    break;
                }
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
                if (1 != msgItem.mLoadType) {
                    showProcessingStatusefaultView(msgItem);
                    break;
                } else {
                    showCommonStatusView(msgItem);
                    break;
                }
        }
    }

    private void showProcessingStatusefaultView(RcsFileTransGroupMessageItem msgItem) {
        this.mRejectBtn.setVisibility(8);
        this.mAcceptBtn.setVisibility(8);
        this.mAlawaysAcceptBtn.setVisibility(8);
        this.mAlawaysAcceptTextView.setVisibility(8);
        this.mCancelBtn.setVisibility(0);
        this.mFileTransImageSrc.setVisibility(0);
        this.mTransInfoTextView.setVisibility(0);
        if (this.mLastMsgID != msgItem.mMsgId) {
            this.mLastMsgID = msgItem.mMsgId;
            this.mLastTransSize = 0;
        }
        if (msgItem.mImAttachmentTransSize < this.mLastTransSize) {
            MLog.d("RcsFileTransGroupMessageListItem FileTrans: ", "currentSize is less than lasttransSize currentSize = " + msgItem.mImAttachmentTransSize + " lasttransSize = " + this.mLastTransSize);
            msgItem.mImAttachmentTransSize = this.mLastTransSize;
        } else {
            this.mLastTransSize = msgItem.mImAttachmentTransSize;
        }
        this.mTransInfoTextView.setText(RcsUtility.formatFileSizeWithoutUnit(msgItem.mImAttachmentTransSize, msgItem.mImAttachmentTotalSize) + "/" + Formatter.formatFileSize(this.mContext, msgItem.mImAttachmentTotalSize));
        int progress = msgItem.mImAttachmentTotalSize != 0 ? (int) ((msgItem.mImAttachmentTransSize * 100) / msgItem.mImAttachmentTotalSize) : 0;
        if (msgItem.mtype == this.FILESEND) {
            this.mCancelBtn.setTextColor(getResources().getColor(R.color.black));
            if (progress == 99) {
                this.mCancelBtn.setClickable(false);
                this.mCancelBtn.setTextColor(getResources().getColor(R.color.text_gray_black_color));
            }
        }
        this.mFileTransImageSrc.setProgress(progress);
        this.mTransNameTextView.setVisibility(0);
        this.mTransInfoTextView.setTextSize(12.0f);
        setTextForTransNameTextView(getFileAttachmentPath(msgItem));
        setmFileTransImageSrc(msgItem);
        this.mFileTransImageSrc.hideFileIcon();
    }

    public void showCompleteFileView(RcsFileTransGroupMessageItem msgItem) {
        this.mLastTransSize = 0;
        switch (msgItem.mFileTransType) {
            case 7:
            case 8:
                showCompleteFileDefaultView(msgItem);
                this.mTransNameTextView.setVisibility(8);
                if (1 == msgItem.mLoadType) {
                    this.mTransInfoTextView.setVisibility(8);
                    this.mFileSize.setVisibility(8);
                    return;
                }
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

    private void showCompleteFileDefaultView(RcsFileTransGroupMessageItem msgItem) {
        this.mRejectBtn.setVisibility(8);
        this.mAcceptBtn.setVisibility(8);
        this.mAlawaysAcceptBtn.setVisibility(8);
        this.mAlawaysAcceptTextView.setVisibility(8);
        this.mTransInfoTextView.setVisibility(8);
        this.mTransNameTextView.setVisibility(0);
        this.mTransInfoTextView.setTextSize(14.0f);
        String mImAttachmentPath = getFileAttachmentPath(msgItem);
        setTextForTransNameTextView(mImAttachmentPath);
        this.mFileTransImageSrc.hideProgressBar();
        this.mTransNameTextView.setVisibility(0);
        this.mTransInfoTextView.setTextSize(14.0f);
        setTextForTransNameTextView(mImAttachmentPath);
        this.mFileTransImageSrc.setVisibility(0);
        this.mFileSize.setVisibility(0);
        setmFileTransImageSrc(msgItem);
        this.mFileTransImageSrc.setVisibility(0);
        if (msgItem.isVideoFileType()) {
            this.mFileTransImageSrc.showVideoIcon(this.mContext);
        }
    }

    private void setFieSizeAndType(RcsFileTransGroupMessageItem msgItem) {
        this.mFileSize.setText(Formatter.formatFileSize(this.mContext, msgItem.mImAttachmentTotalSize));
    }

    public String getFileAttachmentPath(RcsFileTransGroupMessageItem msgItem) {
        String mImAttachmentPath = msgItem.mImAttachmentPath;
        if (mImAttachmentPath == null) {
            mImAttachmentPath = msgItem.mImAttachmentContent;
        }
        MLog.i("RcsFileTransGroupMessageListItem FileTrans: ", "refreshViewAnyStatus mImAttachmentPath = " + mImAttachmentPath);
        return mImAttachmentPath;
    }

    private int getAudioMessageDuration(RcsFileTransGroupMessageItem msgItem) {
        return RcsMessageUtils.getAudioFileDuration(this.mContext, msgItem.getAttachmentFile());
    }

    private void setAudioDurationView(RcsFileTransGroupMessageItem msgItem) {
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

    private void setVattchViewImageSrc(RcsFileTransGroupMessageItem msgItem) {
        if (msgItem.mVcardInfo != null) {
            MLog.d("RcsFileTransGroupMessageListItem FileTrans: ", "setVattchViewImageSrc: msgItem.mVcardInfo is not null, presentVcardThumbnail");
            msgItem.mVcardInfo.presentVcardThumbnail(this.mContext, this.mVAttchView, this.mVattchNodesLayout, this.mVattchImage, msgItem.mIsOutgoing);
            this.mVattchNodesLayout.setVisibility(0);
        } else if (msgItem.createVcardParsingModule()) {
            msgItem.mVcardInfo.presentVcardThumbnail(this.mContext, this.mVAttchView, this.mVattchNodesLayout, this.mVattchImage, msgItem.mIsOutgoing);
            this.mVattchNodesLayout.setVisibility(0);
        }
        if (msgItem.mFileIcon != null) {
            this.mVattchImage.setImageBitmap(msgItem.mFileIcon);
            MLog.d("RcsFileTransGroupMessageListItem FileTrans: ", "setVattchViewImageSrc: msgItem.mFileIcon is not null");
        }
        if (!new File(msgItem.mImAttachmentPath).exists()) {
            MLog.d("RcsFileTransGroupMessageListItem FileTrans: ", "setVattchViewImageSrc: vcrad file is not exist, show default icon");
            this.mVattchImage.setImageResource(R.drawable.rcs_ic_contact_picture_holo_dark);
            this.mVattchNodesLayout.setVisibility(8);
        }
    }

    private void showDefualtVcardView() {
        this.mVattchImage.setImageResource(R.drawable.rcs_ic_contact_picture_holo_dark);
        this.mVattchNodesLayout.setVisibility(8);
    }

    private void setInComingVcardView(RcsFileTransGroupMessageItem msgItem) {
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

    public void setMultiChoice(boolean isMultiChoice) {
        this.mIsMultiChoice = isMultiChoice;
    }

    public void setGroupMsgListItemCallback(GroupChatMsgListItemCallback callback) {
        this.mCallback = callback;
    }

    public void setFragment(RcsGroupChatComposeMessageFragment gccmf) {
        this.mGroupFrg = gccmf;
    }
}
