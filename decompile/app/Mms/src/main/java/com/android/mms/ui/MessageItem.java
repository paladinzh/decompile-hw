package com.android.mms.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.TempFileProvider;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.TextModel;
import com.android.mms.model.VCalendarModel;
import com.android.mms.model.VcardModel;
import com.android.mms.ui.MessageListAdapter.ColumnsMap;
import com.android.mms.util.AddressUtils;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.util.ItemLoadedFuture;
import com.android.mms.util.PduLoaderManager.PduLoaded;
import com.android.mms.util.VcardMessageHelper;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsMessageItem;
import com.google.android.gms.R;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.DeliveryInd;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.MultimediaMessagePdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.ReadOrigInd;
import com.google.android.mms.pdu.ReadRecInd;
import com.google.android.mms.pdu.RetrieveConf;
import com.google.android.mms.pdu.SendReq;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.IListItem;
import com.huawei.mms.ui.RiskUrlThreadPool;
import com.huawei.mms.util.CommonGatherLinks;
import com.huawei.mms.util.FavoritesUtils;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.TextSpan;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class MessageItem {
    private static String TAG = "MessageItem";
    static final Map<Long, int[]> mMmsRiskUrlCache = new HashMap();
    static final Map<Long, int[]> msmsRichUrlCache = new HashMap();
    HashMap extend;
    int[] mAddrPosInBody;
    public String mAddress;
    AsyncTask<String, Integer, String> mAsyncTask;
    public int mAttachmentType;
    public String mBody;
    public final int mBoxId;
    CharSequence mCachedFormattedMessage;
    public ColumnsMap mColumnsMap;
    String mContact;
    private ContactList mContacts;
    final Context mContext;
    private CryptoMessageItem mCryptoMessageItem;
    public Cursor mCursor;
    public long mDate;
    int[] mDatePosInBody;
    public DeliveryStatus mDeliveryStatus;
    public int mErrorCode;
    int mErrorType;
    public long mFavoritesOrginId;
    int mGroupAllCnt;
    int mGroupFailCnt;
    int mGroupSentCnt;
    private boolean mHasAttach;
    public boolean mHasImageInFirstSlidShow;
    private boolean mHasVCalendar;
    boolean mHasVcard;
    Pattern mHighlight;
    private HwCustMessageItem mHwCustMessageItem;
    public boolean mIsMultiRecipients;
    public int mIsSecret;
    private long mItemId;
    private ItemLoadedFuture mItemLoadedFuture;
    boolean mLastSendingState;
    private IListItem mListItem;
    private int mLoadType;
    public boolean mLocked;
    int mMessageSize;
    public int mMessageType;
    public Uri mMessageUri;
    int mMmsStatus;
    public long mMsgId;
    public int mMsgItemRecipientNo;
    public List<TextSpan> mMsgtextSpan;
    public int mNetworkType;
    public int mNoOfSent;
    private OnMmsTextLoadCallBack mOnMmsTextLoadCallBack;
    String mOutgoingReciever;
    private PduLoadedCallback mPduLoadedCallback;
    private RcsMessageItem mRcsMessageItem;
    public boolean mReadReport;
    int[] mRiskUrlPosInBody;
    long mSentDate;
    public SlideshowModel mSlideshow;
    public String mSmsServiceCenter;
    public String mSmsServiceCenterForFavorites;
    public String mStrUid;
    public int mSubId;
    public String mSubject;
    String mTextContentType;
    public long mThreadId;
    public String mTimeHM;
    public String mTimestamp;
    public final String mType;
    long mUid;
    public String mVSmsOriginalData;

    public interface PduLoadedCallback {
        void onPduLoaded(MessageItem messageItem);
    }

    public enum DeliveryStatus {
        NONE,
        INFO,
        FAILED,
        PENDING,
        RECEIVED,
        READ
    }

    public interface OnMmsTextLoadCallBack {
        void onCallBack();
    }

    public class PduLoadedMessageItemCallback implements ItemLoadedCallback {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onItemLoaded(Object result, Throwable exception) {
            if (exception != null) {
                MLog.e(MessageItem.TAG, "PduLoadedMessageItemCallback PDU couldn't be loaded: ", exception);
                return;
            }
            long timestamp;
            if (MessageItem.this.mItemLoadedFuture != null) {
                synchronized (MessageItem.this.mItemLoadedFuture) {
                    MessageItem.this.mItemLoadedFuture.setIsDone(true);
                }
            }
            PduLoaded pduLoaded = (PduLoaded) result;
            if (130 == MessageItem.this.mMessageType) {
                MessageItem.this.mDeliveryStatus = DeliveryStatus.NONE;
                try {
                    NotificationInd notifInd = (NotificationInd) pduLoaded.mPdu;
                    MessageItem.this.interpretFrom(notifInd.getFrom(), MessageItem.this.mMessageUri);
                    MessageItem.this.mBody = new String(notifInd.getContentLocation(), Charset.defaultCharset());
                    if (MessageItem.this.mOnMmsTextLoadCallBack != null) {
                        MessageItem.this.mOnMmsTextLoadCallBack.onCallBack();
                    }
                    MessageItem.this.mMessageSize = (int) notifInd.getMessageSize();
                    timestamp = notifInd.getExpiry() * 1000;
                } catch (ClassCastException e) {
                    MLog.e(MessageItem.TAG, "class cast exeption, from GenericPdu to NotificationInd: ", (Throwable) e);
                    return;
                }
            } else if (!MessageItem.this.mCursor.isClosed() && !MessageItem.this.mCursor.isAfterLast()) {
                try {
                    MultimediaMessagePdu msg = pduLoaded.mPdu;
                    MessageItem.this.mSlideshow = pduLoaded.mSlideshow;
                    MessageItem.this.mAttachmentType = MessageUtils.getAttachmentType(MessageItem.this.mSlideshow, msg);
                    if (MessageItem.this.mSlideshow != null) {
                        MessageItem.this.mHasImageInFirstSlidShow = MessageUtils.hasImageInFirstSlidShow(MessageItem.this.mSlideshow);
                    }
                    PduBody body = msg.getBody();
                    int mPartNum = body.getPartsNum();
                    if (MessageItem.this.mSlideshow != null) {
                        SlideModel slideModel = MessageItem.this.mSlideshow.get(0);
                    }
                    int i = 0;
                    while (i < mPartNum) {
                        String mConTypeString = new String(body.getPart(i).getContentType(), Charset.defaultCharset());
                        if (mConTypeString.equalsIgnoreCase("text/x-vCard")) {
                            MessageItem.this.mHasVcard = true;
                        } else if ("application/oct-stream".equalsIgnoreCase(mConTypeString)) {
                            if (body.getPart(i).getName() != null && new String(body.getPart(i).getName(), Charset.defaultCharset()).toLowerCase(Locale.getDefault()).endsWith("vcf")) {
                                MessageItem.this.mHasVcard = true;
                            }
                        } else if (mConTypeString.equalsIgnoreCase("text/x-vCalendar")) {
                            MessageItem.this.mHasVCalendar = true;
                        } else if (ContentType.isSupportedAudioType(mConTypeString) || ContentType.isSupportedImageType(mConTypeString) || ContentType.isSupportedVideoType(mConTypeString)) {
                            MessageItem.this.mHasAttach = true;
                        }
                        i++;
                    }
                    if (MessageItem.this.mMessageType == 132) {
                        try {
                            RetrieveConf retrieveConf = (RetrieveConf) msg;
                            MessageItem.this.interpretFrom(retrieveConf.getFrom(), MessageItem.this.mMessageUri);
                            timestamp = retrieveConf.getDate() * 1000;
                            if (!MmsConfig.isDisplaySentTime()) {
                                if (MessageItem.this.isInComingMessage()) {
                                }
                            }
                            if (!(MessageItem.this.mSentDate == 0 || MessageItem.this.mSentDate == 1)) {
                                timestamp = MessageItem.this.mSentDate * 1000;
                            }
                        } catch (ClassCastException e2) {
                            MLog.e(MessageItem.TAG, "class cast exeption, from MultimediaMessagePdu to RetrieveConf: ", (Throwable) e2);
                            return;
                        }
                    }
                    MessageItem messageItem = MessageItem.this;
                    String string = MessageItem.this.mContext.getString(R.string.message_sender_from_self);
                    MessageItem.this.mAddress = string;
                    messageItem.mContact = string;
                    try {
                        timestamp = ((SendReq) msg).getDate() * 1000;
                    } catch (ClassCastException e22) {
                        MLog.e(MessageItem.TAG, "class cast exeption, from MultimediaMessagePdu to SendReq: ", (Throwable) e22);
                        return;
                    }
                    SlideModel slide = MessageItem.this.mSlideshow == null ? null : MessageItem.this.mSlideshow.get(0);
                    if (slide != null && slide.hasText()) {
                        TextModel tm = slide.getText();
                        if (MessageUtils.isNeedLayoutRtl()) {
                            MessageItem.this.mBody = "‏" + tm.getText();
                        } else {
                            MessageItem.this.mBody = tm.getText();
                        }
                        if (MessageItem.this.mOnMmsTextLoadCallBack != null) {
                            MessageItem.this.mOnMmsTextLoadCallBack.onCallBack();
                        }
                        MessageItem.this.mTextContentType = tm.getContentType();
                        if (!(1 == MessageItem.this.mLoadType || MessageItem.this.mLoadType == 0)) {
                            if (2 == MessageItem.this.mLoadType) {
                            }
                        }
                        MessageItem.this.mAddrPosInBody = HwMessageUtils.getAddrFromTMRManager(MessageItem.this.mBody);
                        MessageItem.this.mDatePosInBody = HwMessageUtils.getTimePosition(MessageItem.this.mBody);
                        if (MessageItem.mMmsRiskUrlCache != null) {
                            int[] iArr = (int[]) MessageItem.mMmsRiskUrlCache.get(Long.valueOf(MessageItem.this.mMsgId));
                            MessageItem.this.mRiskUrlPosInBody = iArr;
                            if (iArr == null) {
                                MessageItem.this.checkRiskUrlInAsyncTask();
                            }
                        }
                        MessageItem.this.mMsgtextSpan = CommonGatherLinks.getTextSpans(MessageItem.this.mAddrPosInBody, MessageItem.this.mDatePosInBody, MessageItem.this.mRiskUrlPosInBody, MessageItem.this.mBody, MessageItem.this.mContext, MessageItem.this.mDate);
                    }
                    MessageItem.this.mMessageSize = MessageItem.this.mSlideshow == null ? 0 : MessageItem.this.mSlideshow.getTotalMessageSize();
                    String report = MessageItem.this.mCursor.getString(MessageItem.this.mColumnsMap.mColumnMmsDeliveryReport);
                    if (report == null || !MessageItem.this.mAddress.equals(MessageItem.this.mContext.getString(R.string.message_sender_from_self))) {
                        MessageItem.this.mDeliveryStatus = DeliveryStatus.NONE;
                    } else {
                        try {
                            if (Integer.parseInt(report) == 128) {
                                MessageItem.this.mDeliveryStatus = DeliveryStatus.RECEIVED;
                            } else {
                                MessageItem.this.mDeliveryStatus = DeliveryStatus.NONE;
                            }
                        } catch (NumberFormatException e3) {
                            MLog.e(MessageItem.TAG, "Value for delivery report was invalid.");
                            MessageItem.this.mDeliveryStatus = DeliveryStatus.NONE;
                        }
                    }
                    report = MessageItem.this.mCursor.getString(MessageItem.this.mColumnsMap.mColumnMmsReadReport);
                    if (report == null || !MessageItem.this.mAddress.equals(MessageItem.this.mContext.getString(R.string.message_sender_from_self))) {
                        MessageItem.this.mReadReport = false;
                    } else {
                        try {
                            int reportInt = Integer.parseInt(report);
                            MessageItem.this.mReadReport = reportInt == 128;
                        } catch (NumberFormatException e4) {
                            MLog.e(MessageItem.TAG, "Value for read report was invalid.");
                            MessageItem.this.mReadReport = false;
                        }
                    }
                } catch (ClassCastException e222) {
                    MLog.e(MessageItem.TAG, "class cast exeption, from GenericPdu to MultimediaMessagePdu: ", (Throwable) e222);
                    return;
                }
            } else {
                return;
            }
            if (!MessageItem.this.isOutgoingMessage()) {
                if (130 == MessageItem.this.mMessageType) {
                    MessageItem.this.mTimestamp = MessageItem.this.mContext.getString(R.string.expire_on, new Object[]{MessageUtils.formatTimeStampString(MessageItem.this.mContext, timestamp, true)});
                } else {
                    MessageItem.this.mTimestamp = MessageUtils.getMessageShowTime(MessageItem.this.mContext, timestamp);
                    MessageItem.this.mTimeHM = MessageUtils.getMessageShowTime(MessageItem.this.mContext, timestamp, false);
                }
            }
            if (MessageItem.this.mPduLoadedCallback != null) {
                MessageItem.this.mPduLoadedCallback.onPduLoaded(MessageItem.this);
            }
        }
    }

    class RiskUrlCheckAsyncTask extends AsyncTask<String, Integer, String> {
        RiskUrlCheckAsyncTask() {
        }

        protected String doInBackground(String... arg) {
            synchronized (MessageItem.this) {
                String pos = HwMessageUtils.getRiskUrlPosString(MessageItem.this.mContext, arg[1]);
                if (TextUtils.isEmpty(pos) || "0,".equals(pos)) {
                    if (MessageItem.this.mBoxId == 1) {
                        pos = HwMessageUtils.getUnOfficialUrlPosString(MessageItem.this.mContext, arg[0], arg[1]);
                    }
                    if (TextUtils.isEmpty(pos)) {
                        return null;
                    }
                }
                MessageItem.this.mRiskUrlPosInBody = HwMessageUtils.spanStringToPosition(pos);
                if (MessageItem.this.mType.equals("sms")) {
                    ContentValues values = new ContentValues();
                    values.put("risk_url_body", pos);
                    SqliteWrapper.update(MessageItem.this.mContext, MessageItem.this.mContext.getContentResolver(), MessageItem.this.mMessageUri, values, null, null);
                    MLog.d(MessageItem.TAG, "MessageItem RiskUrl SqliteWrapper.update");
                    MessageItem.msmsRichUrlCache.put(Long.valueOf(MessageItem.this.mMsgId), MessageItem.this.mRiskUrlPosInBody);
                } else if (!(MessageItem.mMmsRiskUrlCache == null || MessageItem.mMmsRiskUrlCache.containsKey(Long.valueOf(MessageItem.this.mMsgId)) || MessageItem.this.mRiskUrlPosInBody == null)) {
                    MessageItem.mMmsRiskUrlCache.put(Long.valueOf(MessageItem.this.mMsgId), MessageItem.this.mRiskUrlPosInBody);
                }
                MessageItem.this.mMsgtextSpan = CommonGatherLinks.getTextSpans(MessageItem.this.mAddrPosInBody, MessageItem.this.mDatePosInBody, MessageItem.this.mRiskUrlPosInBody, MessageItem.this.mBody, MessageItem.this.mContext, MessageItem.this.mDate);
                return null;
            }
        }

        protected void onPostExecute(String result) {
            if (MessageItem.this.mListItem != null && MessageItem.this.mListItem.getMsgItemID() == MessageItem.this.mMsgId) {
                boolean needUpdateWarningStr = false;
                if (MessageItem.this.mType.equals("sms") && MessageItem.this.mMsgtextSpan != null && MessageItem.this.mMsgtextSpan.size() > 0 && MessageItem.this.mRiskUrlPosInBody != null && MessageItem.this.mRiskUrlPosInBody.length > 0) {
                    needUpdateWarningStr = true;
                } else if (MessageItem.this.mType.equals("mms")) {
                    needUpdateWarningStr = true;
                }
                if (needUpdateWarningStr) {
                    MessageItem.this.mListItem.setItemText(MessageItem.this);
                }
            }
        }
    }

    public void registerListItem(IListItem listItem) {
        this.mListItem = listItem;
    }

    public RcsMessageItem getRcsMessageItem() {
        return this.mRcsMessageItem;
    }

    public static void removeSmsRichUrlCache(long MsgId) {
        if (msmsRichUrlCache != null) {
            msmsRichUrlCache.remove(Long.valueOf(MsgId));
        }
    }

    public boolean isRcsChat() {
        if (this.mRcsMessageItem != null) {
            return this.mRcsMessageItem.isRcsChat();
        }
        return false;
    }

    private Uri getMsgLoadUri() {
        Uri uri = null;
        if (isSms()) {
            uri = 1 == this.mLoadType ? FavoritesUtils.URI_FAV_SMS : Sms.CONTENT_URI;
        } else if (isMms()) {
            uri = 1 == this.mLoadType ? FavoritesUtils.URI_FAV_MMS : Mms.CONTENT_URI;
        }
        if (uri == null) {
            return null;
        }
        return ContentUris.withAppendedId(uri, this.mMsgId);
    }

    public void setContactList(ContactList cl) {
        this.mContacts = cl;
    }

    public ContactList getContactList() {
        return this.mContacts;
    }

    public MessageItem(Context context, String type, Cursor cursor, ColumnsMap columnsMap, Pattern highlight) throws MmsException {
        this(context, type, cursor, columnsMap, highlight, 0);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsMessageItem == null) {
            this.mRcsMessageItem = new RcsMessageItem();
        }
    }

    public MessageItem(Context context, String type, Cursor cursor, ColumnsMap columnsMap, Pattern highlight, int loadtype) throws MmsException {
        this(context, type, cursor, columnsMap, highlight, loadtype, false, false);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsMessageItem == null) {
            this.mRcsMessageItem = new RcsMessageItem();
        }
    }

    public MessageItem(Context context, String type, Cursor cursor, ColumnsMap columnsMap, Pattern highlight, int loadtype, boolean isShowAutoLink, boolean isGroupCov) throws MmsException {
        this.mHasVcard = false;
        this.mHasVCalendar = false;
        this.mHasAttach = false;
        this.mHasImageInFirstSlidShow = false;
        this.mDate = 0;
        this.mItemId = 0;
        this.mLoadType = 0;
        this.mGroupSentCnt = 0;
        this.mGroupFailCnt = 0;
        this.mGroupAllCnt = 0;
        this.mUid = 0;
        this.mIsMultiRecipients = false;
        this.mMsgtextSpan = null;
        this.mHwCustMessageItem = (HwCustMessageItem) HwCustUtils.createObj(HwCustMessageItem.class, new Object[0]);
        this.mCryptoMessageItem = new CryptoMessageItem();
        this.mAsyncTask = new RiskUrlCheckAsyncTask();
        this.extend = null;
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mRcsMessageItem = new RcsMessageItem();
        }
        if (this.mRcsMessageItem != null) {
            this.mRcsMessageItem.setRcsMessageItem(context, this);
        }
        this.mLoadType = loadtype;
        this.mDate = 0;
        this.mContext = context;
        this.mMsgId = cursor.getLong(columnsMap.mColumnMsgId);
        if (cursor.getColumnIndex("origin_id") != -1) {
            this.mFavoritesOrginId = cursor.getLong(cursor.getColumnIndex("origin_id"));
        }
        this.mHighlight = highlight;
        this.mType = type;
        this.mThreadId = cursor.getLong(2);
        this.mCursor = cursor;
        this.mColumnsMap = columnsMap;
        this.mIsMultiRecipients = isGroupCov;
        long date;
        long date_sent;
        if ("sms".equals(type)) {
            this.mReadReport = false;
            this.mDate = cursor.getLong(columnsMap.mColumnSmsDate);
            long status = cursor.getLong(columnsMap.mColumnSmsStatus);
            this.mIsSecret = cursor.getInt(columnsMap.mColumnIsSecret);
            if (MmsConfig.getMultiRecipientSingleViewEnabled()) {
                this.mStrUid = cursor.getString(columnsMap.mColumnUID);
                if (this.mStrUid != null) {
                    StringBuilder stringBuilder = new StringBuilder("uid");
                    stringBuilder.append(" = ?");
                    Cursor lCusr = SqliteWrapper.query(this.mContext, Sms.CONTENT_URI, new String[]{"count(*)"}, stringBuilder.toString(), new String[]{this.mStrUid}, null);
                    if (lCusr != null) {
                        if (lCusr.moveToFirst()) {
                            this.mMsgItemRecipientNo = lCusr.getInt(0);
                        }
                        lCusr.close();
                    }
                    stringBuilder.append(" AND ").append(NumberInfo.TYPE_KEY).append(" = ?");
                    lCusr = SqliteWrapper.query(this.mContext, Sms.CONTENT_URI, new String[]{"count(*)"}, stringBuilder.toString(), new String[]{this.mStrUid, String.valueOf(2)}, null);
                    if (lCusr != null) {
                        if (lCusr.moveToFirst()) {
                            this.mNoOfSent = lCusr.getInt(0);
                        }
                        lCusr.close();
                    }
                }
            }
            if (status == -1) {
                this.mDeliveryStatus = DeliveryStatus.NONE;
            } else if (status >= 64) {
                this.mDeliveryStatus = DeliveryStatus.FAILED;
            } else if (status >= 32) {
                this.mDeliveryStatus = DeliveryStatus.PENDING;
            } else {
                this.mDeliveryStatus = DeliveryStatus.RECEIVED;
                if (this.mRcsMessageItem != null) {
                    this.mDeliveryStatus = this.mRcsMessageItem.getDeliveryExtendStatue(status, this.mDeliveryStatus);
                }
            }
            this.mMessageUri = getMsgLoadUri();
            this.mBoxId = cursor.getInt(columnsMap.mColumnSmsType);
            this.mAddress = cursor.getString(columnsMap.mColumnSmsAddress);
            if (1 == this.mBoxId) {
                this.mSmsServiceCenter = cursor.getString(columnsMap.mColumnSmsServiceCenter);
            }
            this.mSmsServiceCenterForFavorites = cursor.getString(columnsMap.mColumnSmsServiceCenter);
            if (this.mLoadType == 0 && isGroupCov) {
                if (-1 != cursor.getColumnIndex("group_id")) {
                    this.mUid = cursor.getLong(columnsMap.mColumnUID);
                }
            }
            if (Sms.isOutgoingFolder(this.mBoxId)) {
                String meString = context.getString(R.string.message_sender_from_self);
                this.mContact = this.mAddress;
                this.mOutgoingReciever = cursor.getString(3);
                setMsgStatus(cursor, this.mColumnsMap);
            } else {
                this.mContact = Contact.get(this.mAddress, false).getName();
                this.mOutgoingReciever = "";
            }
            this.mBody = cursor.getString(columnsMap.mColumnSmsBody);
            this.mCryptoMessageItem.setEncryptSmsType(cursor.getString(columnsMap.mColumnSmsBody));
            if (-1 != columnsMap.mColumnSubId) {
                this.mSubId = cursor.getInt(columnsMap.mColumnSubId);
            } else {
                MLog.w(TAG, "columnsMap.mColumnSubId == COLUMN_INVALID, set mSubId = -1 !");
                this.mSubId = -1;
            }
            if (-1 != columnsMap.mColumnNetworkType) {
                this.mNetworkType = cursor.getInt(columnsMap.mColumnNetworkType);
            }
            date = cursor.getLong(columnsMap.mColumnSmsDate);
            date_sent = cursor.getLong(columnsMap.mColumnSmsDateSent);
            if (!((!MmsConfig.isDisplaySentTime() && (!isInComingMessage() || !HwMessageUtils.displayMmsSentTime(date, date_sent, this.mSubId))) || 3 == loadtype || date_sent == 0 || date_sent == 1)) {
                date = date_sent;
            }
            if (0 != date) {
                this.mTimestamp = MessageUtils.getMessageShowTime(this.mContext, date);
                this.mTimeHM = MessageUtils.getMessageShowTime(this.mContext, date, false);
                this.mDate = date;
            } else {
                this.mTimestamp = " ";
                this.mTimeHM = " ";
                MLog.d(TAG, "the date of the sms is not exist and not show the timestamp. e.g icc sms");
            }
            if (!isOutgoingMessage()) {
                if (cursor.getColumnIndex("subject") != -1) {
                    this.mSubject = cursor.getString(columnsMap.mColumnSmsSubject);
                }
                if (VcardMessageHelper.isVCardSmsSubject(this.mSubject) && (this.mVSmsOriginalData == null || !this.mVSmsOriginalData.equals(this.mSubject))) {
                    this.mVSmsOriginalData = this.mSubject;
                }
            }
            this.mLocked = cursor.getInt(columnsMap.mColumnSmsLocked) != 0;
            this.mErrorCode = cursor.getInt(columnsMap.mColumnSmsErrorCode);
            if (this.mErrorCode != 0) {
                MLog.d(TAG, "Item [" + this.mMsgId + "] has error " + this.mErrorCode);
            }
            if (!(1 == this.mLoadType || this.mLoadType == 0)) {
                if (2 == this.mLoadType) {
                }
            }
            this.mAddrPosInBody = HwMessageUtils.spanStringToPosition(cursor.getString(columnsMap.mColumnAddrInBody));
            this.mDatePosInBody = HwMessageUtils.spanStringToPosition(cursor.getString(columnsMap.mColumnTimeInBody));
            String riskUrlInBody = cursor.getString(columnsMap.mColumnRiskUrlInBody);
            if (!TextUtils.isEmpty(riskUrlInBody) || msmsRichUrlCache.containsKey(Long.valueOf(this.mMsgId))) {
                this.mRiskUrlPosInBody = HwMessageUtils.spanStringToPosition(riskUrlInBody);
            } else {
                checkRiskUrlInAsyncTask();
            }
            if (1 == this.mLoadType || 2 == this.mLoadType) {
                this.mMsgtextSpan = CommonGatherLinks.getTextSpans(this.mAddrPosInBody, this.mDatePosInBody, this.mRiskUrlPosInBody, this.mBody, this.mContext, this.mDate);
                if (this.mIsSecret == 1) {
                    CommonGatherLinks.gatherSafetySms(this.mMsgtextSpan);
                }
            }
        } else if ("mms".equals(type)) {
            this.mDate = cursor.getLong(columnsMap.mColumnMmsDate) * 1000;
            this.mMessageUri = getMsgLoadUri();
            this.mBoxId = cursor.getInt(columnsMap.mColumnMmsMessageBox);
            this.mMessageType = cursor.getInt(columnsMap.mColumnMmsMessageType);
            this.mErrorType = cursor.getInt(columnsMap.mColumnMmsErrorType);
            String subject = cursor.getString(columnsMap.mColumnMmsSubject);
            if (-1 != columnsMap.mColumnSubId) {
                this.mSubId = cursor.getInt(columnsMap.mColumnSubId);
            } else {
                MLog.w(TAG, "columnsMap.mColumnSubId == COLUMN_INVALID, set mSubId = -1 !");
                this.mSubId = -1;
            }
            if (-1 != columnsMap.mColumnNetworkType) {
                this.mNetworkType = cursor.getInt(columnsMap.mColumnNetworkType);
            }
            if (!TextUtils.isEmpty(subject)) {
                this.mSubject = MessageUtils.cleanseMmsSubject(context, new EncodedStringValue(cursor.getInt(columnsMap.mColumnMmsSubjectCharset), PduPersister.getBytes(subject)).getString());
            }
            this.mLocked = cursor.getInt(columnsMap.mColumnMmsLocked) != 0;
            this.mSlideshow = null;
            this.mDeliveryStatus = DeliveryStatus.NONE;
            this.mReadReport = false;
            this.mBody = null;
            this.mMessageSize = 0;
            this.mTextContentType = null;
            this.mTimestamp = "";
            this.mTimeHM = "";
            this.mMmsStatus = cursor.getInt(columnsMap.mColumnMmsStatus);
            this.mAttachmentType = cursor.getInt(columnsMap.mColumnMmsTextOnly) != 0 ? 0 : -1;
            if (1 == loadtype || 2 == loadtype) {
                this.mOutgoingReciever = getMmsMsgReciever();
            }
            boolean loadSlideshow = this.mMessageType != 130;
            date = cursor.getLong(columnsMap.mColumnMmsDate);
            date_sent = cursor.getLong(columnsMap.mColumnMmsDateSent);
            if (!((!MmsConfig.isDisplaySentTime() && (!isInComingMessage() || !HwMessageUtils.displayMmsSentTime(1000 * date, 1000 * date_sent, this.mSubId))) || date_sent == 0 || date_sent == 1)) {
                date = date_sent;
                this.mSentDate = date_sent;
            }
            if (0 != date) {
                this.mTimestamp = MessageUtils.getMessageShowTime(this.mContext, 1000 * date);
                this.mTimeHM = MessageUtils.getMessageShowTime(this.mContext, 1000 * date, false);
                this.mDate = 1000 * date;
            } else {
                this.mTimestamp = " ";
                this.mTimeHM = " ";
                MLog.d(TAG, "the date of the mms is not exist.");
            }
            this.mItemLoadedFuture = MmsApp.getApplication().getPduLoaderManager().getPdu(this.mMessageUri, loadSlideshow, new PduLoadedMessageItemCallback());
        } else if (this.mRcsMessageItem == null || !this.mRcsMessageItem.init()) {
            throw new MmsException("Unknown type of the message: " + type);
        } else {
            this.mMessageUri = getMsgLoadUri();
            this.mBoxId = cursor.getInt(columnsMap.mColumnSmsType);
            this.mAddress = cursor.getString(columnsMap.mColumnSmsAddress);
            if (1 == this.mBoxId) {
                this.mSmsServiceCenter = cursor.getString(columnsMap.mColumnSmsServiceCenter);
            }
            this.mSmsServiceCenterForFavorites = cursor.getString(columnsMap.mColumnSmsServiceCenter);
            if (this.mLoadType == 0 && isGroupCov) {
                if (-1 != cursor.getColumnIndex("group_id")) {
                    this.mUid = cursor.getLong(columnsMap.mColumnUID);
                }
            }
            if (Sms.isOutgoingFolder(this.mBoxId)) {
                this.mContact = context.getString(R.string.message_sender_from_self);
                this.mOutgoingReciever = cursor.getString(3);
                setMsgStatus(cursor, this.mColumnsMap);
            } else {
                this.mContact = Contact.get(this.mAddress, false).getName();
                this.mOutgoingReciever = "";
            }
            this.mRcsMessageItem.initMore();
        }
        if (this.mRcsMessageItem != null) {
            this.mRcsMessageItem.initForFav(columnsMap, loadtype);
        }
        this.mThreadId = cursor.getLong(2);
    }

    private void interpretFrom(EncodedStringValue from, Uri messageUri) {
        String str;
        if (from != null) {
            this.mAddress = from.getString();
        } else {
            this.mAddress = AddressUtils.getFrom(this.mContext, messageUri);
        }
        if (TextUtils.isEmpty(this.mAddress)) {
            str = "";
        } else {
            str = Contact.get(this.mAddress, false).getName();
        }
        this.mContact = str;
    }

    public boolean isMms() {
        return this.mType.equals("mms");
    }

    public boolean isSms() {
        return this.mType.equals("sms");
    }

    public boolean isDownloaded() {
        return this.mMessageType != 130;
    }

    public boolean isMe() {
        boolean isIncomingMms = isMms() ? this.mBoxId != 1 ? this.mBoxId == 0 : true : false;
        boolean isIncomingSms = isSms() ? this.mBoxId != 1 ? this.mBoxId == 0 : true : false;
        if (isIncomingMms || isIncomingSms) {
            return false;
        }
        return true;
    }

    public boolean isOutgoingMessage() {
        boolean isOutgoingGroupSms = true;
        boolean isOutgoingMms = isMms() && this.mBoxId == 4;
        boolean isOutgoingSms = isSms() ? (this.mBoxId == 5 || this.mBoxId == 4) ? true : this.mBoxId == 6 : false;
        boolean isOutgoingOther = false;
        if (this.mRcsMessageItem != null) {
            isOutgoingOther = this.mRcsMessageItem.isOutgoingExtMessage();
        }
        if (this.mIsMultiRecipients && isSms() && this.mGroupAllCnt > 1) {
            if (this.mGroupSentCnt + this.mGroupFailCnt >= this.mGroupAllCnt) {
                isOutgoingGroupSms = false;
            }
            return isOutgoingGroupSms;
        }
        if (isOutgoingMms || isOutgoingSms) {
            isOutgoingOther = true;
        }
        return isOutgoingOther;
    }

    public boolean isSending() {
        boolean z = false;
        boolean z2 = true;
        if (this.mIsMultiRecipients && this.mGroupAllCnt > 1) {
            if (this.mGroupSentCnt == this.mGroupAllCnt) {
                z2 = false;
            }
            return z2;
        } else if (this.mMsgItemRecipientNo <= 1 || !MmsConfig.getMultiRecipientSingleViewEnabled()) {
            if (!isFailedMessage()) {
                z = isOutgoingMessage();
            }
            return z;
        } else {
            if (this.mNoOfSent == this.mMsgItemRecipientNo) {
                z2 = false;
            }
            return z2;
        }
    }

    public boolean isFailedMessage() {
        boolean isFailedMms = isMms() ? this.mErrorType >= 10 : false;
        boolean isFailedSms = isSms() ? this.mBoxId == 5 : false;
        boolean isFailedOther = false;
        if (this.mRcsMessageItem != null) {
            isFailedOther = this.mRcsMessageItem.isFailedExtMessage();
        }
        if (isFailedMms || isFailedSms) {
            return true;
        }
        return isFailedOther;
    }

    public void setCachedFormattedMessage(CharSequence formattedMessage) {
        this.mCachedFormattedMessage = formattedMessage;
    }

    public CharSequence getCachedFormattedMessage() {
        boolean isSending = isSending();
        if (isSending != this.mLastSendingState) {
            this.mLastSendingState = isSending;
            this.mCachedFormattedMessage = null;
        }
        return this.mCachedFormattedMessage;
    }

    public int getBoxId() {
        return this.mBoxId;
    }

    public long getMessageId() {
        return this.mMsgId;
    }

    public String toString() {
        return "type: " + this.mType + " box: " + this.mBoxId + " uri: " + this.mMessageUri + " address: " + this.mAddress + " contact: " + this.mContact + " read: " + this.mReadReport + " delivery status: " + this.mDeliveryStatus + " has vcard: " + this.mHasVcard;
    }

    public boolean isHasVcard() {
        if (this.mHasAttach || this.mHasVCalendar) {
            return false;
        }
        return this.mHasVcard;
    }

    public void viewVcardDetail() {
        VcardMessageHelper vCardMessageHelper = null;
        if (isMms() && this.mHasVcard) {
            vCardMessageHelper = createMmsVcardMessageHelper();
        }
        if (vCardMessageHelper != null) {
            vCardMessageHelper.viewVcardDetail();
        }
    }

    public String[] getVcardDetail() {
        VcardMessageHelper vCardMessageHelper = null;
        if (isMms() && this.mHasVcard) {
            vCardMessageHelper = createMmsVcardMessageHelper();
        }
        if (vCardMessageHelper != null) {
            return vCardMessageHelper.getVcardDetail();
        }
        return new String[0];
    }

    private VcardMessageHelper createMmsVcardMessageHelper() {
        if (this.mSlideshow != null && this.mSlideshow.size() > 0) {
            SlideModel slideModel = this.mSlideshow.get(0);
            if (!(slideModel == null || slideModel.getVcard() == null)) {
                VcardModel vCardModel = slideModel.getVcard();
                try {
                    return new VcardMessageHelper(this.mContext, vCardModel.getData(), vCardModel.getVcardDetailList(), vCardModel.getBitmap());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public String[] getVcalendarDetail() {
        if (this.mSlideshow != null && this.mSlideshow.size() > 0) {
            SlideModel slideModel = this.mSlideshow.get(0);
            if (!(slideModel == null || slideModel.getVCalendar() == null)) {
                VCalendarModel model = slideModel.getVCalendar();
                if (model != null) {
                    return model.getVcalendarDetail();
                }
            }
        }
        return new String[0];
    }

    public void saveVcard() {
        VcardMessageHelper vCardMessageHelper = null;
        if (isMms() && this.mHasVcard) {
            vCardMessageHelper = createMmsVcardMessageHelper();
        }
        if (vCardMessageHelper != null) {
            vCardMessageHelper.saveVcard();
        }
    }

    public static boolean isMms(String type) {
        return "mms".equals(type);
    }

    public boolean isInComingMessage() {
        boolean isInComingMms = isMms() && this.mBoxId == 1;
        boolean isInComingSms = isSms() && this.mBoxId == 1;
        boolean isInComingExtMessage = false;
        if (this.mRcsMessageItem != null) {
            isInComingExtMessage = this.mRcsMessageItem.isInComingExtMessage();
        }
        if (isInComingMms || isInComingSms) {
            return true;
        }
        return isInComingExtMessage;
    }

    public Long getItemId() {
        if (this.mItemId != 0) {
            return Long.valueOf(this.mItemId);
        }
        return Long.valueOf(MessageListAdapter.getKey(isMms(), this.mMsgId));
    }

    public String getMmsMsgReciever() {
        if (this.mCursor == null) {
            return "";
        }
        if (this.mCursor.getInt(18) != 128) {
            return "";
        }
        try {
            EncodedStringValue[] to = ((MultimediaMessagePdu) PduPersister.getPduPersister(this.mContext).load(this.mMessageUri)).getTo();
            if (to == null) {
                return "unknow";
            }
            return EncodedStringValue.concat(to);
        } catch (MmsException e) {
            MLog.e(TAG, "Failed to load the message: " + this.mMessageUri, (Throwable) e);
            return "";
        }
    }

    private void checkRiskUrlInAsyncTask() {
        if (HwMessageUtils.getRiskUrlEnable(this.mContext)) {
            if (this.mAsyncTask == null) {
                this.mAsyncTask = new RiskUrlCheckAsyncTask();
            }
            this.mAsyncTask.executeOnExecutor(RiskUrlThreadPool.getDefault(), new String[]{this.mAddress, this.mBody});
        }
    }

    public String getMsgAddress(Context context) {
        if (isInComingMessage()) {
            return getNameFromAddress(context, this.mAddress);
        }
        if (this.mOutgoingReciever == null) {
            this.mOutgoingReciever = getMmsMsgReciever();
        }
        return getNameFromAddress(context, this.mOutgoingReciever);
    }

    public String getNameFromAddress(Context context, String address) {
        if (TextUtils.isEmpty(address)) {
            return "";
        }
        ContactList cl = getContactList();
        if (cl == null || cl.size() < 1) {
            cl = getContacts(this.mContext, address);
            setContactList(cl);
        }
        return cl.formatNames(";");
    }

    public static ContactList getContacts(Context context, String address) {
        return ContactList.getByNumbers(PhoneNumberUtils.replaceUnicodeDigits(address).replace(',', ';'), false, true);
    }

    public String getMessageSummary() {
        if (!isMms()) {
            return this.mBody;
        }
        if (TextUtils.isEmpty(this.mSubject)) {
            return this.mContext.getResources().getString(R.string.no_subject);
        }
        return this.mSubject;
    }

    public void setOnPduLoaded(PduLoadedCallback pduLoadedCallback) {
        this.mPduLoadedCallback = pduLoadedCallback;
    }

    public void cancelPduLoading() {
        if (this.mItemLoadedFuture != null && !this.mItemLoadedFuture.isDone()) {
            if (MLog.isLoggable("Mms_app", 3)) {
                MLog.v(TAG, "cancelPduLoading for: " + this);
            }
            this.mItemLoadedFuture.cancel(this.mMessageUri);
            this.mItemLoadedFuture = null;
        }
    }

    public SlideshowModel getSlideshow() {
        return this.mSlideshow;
    }

    public boolean hasVCalendar() {
        if (this.mHasAttach || this.mHasVcard) {
            return false;
        }
        return this.mHasVCalendar;
    }

    public void saveVCalendar() {
        createVCalendarTempFile();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(TempFileProvider.SCRAP_VCALENDAR_URI, "text/x-vCalendar".toLowerCase(Locale.getDefault()));
        this.mContext.startActivity(intent);
    }

    private void createVCalendarTempFile() {
        FileOutputStream fileOutputStream = null;
        MediaModel mediaModel = null;
        if (this.mSlideshow != null && this.mSlideshow.size() > 0) {
            for (MediaModel model : this.mSlideshow.get(0)) {
                if (model instanceof VCalendarModel) {
                    mediaModel = model;
                }
            }
            if (mediaModel != null) {
                try {
                    this.mContext.deleteFile("vcalendar_temp.vcs");
                    byte[] data = ((VCalendarModel) mediaModel).getVCalendarData();
                    if (data == null) {
                        MLog.e(TAG, "create Calendar temp file failed, calendar data is null");
                        return;
                    }
                    fileOutputStream = this.mContext.openFileOutput("vcalendar_temp.vcs", 0);
                    fileOutputStream.write(data);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e2) {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e3) {
                        }
                    }
                } catch (Throwable th) {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e4) {
                        }
                    }
                }
            }
        }
    }

    public String getForwordSubject(int strResId) {
        if (isSms()) {
            return null;
        }
        if (MmsConfig.isInSimpleUI() && (this.mSubject == null || TextUtils.getTrimmedLength(this.mSubject) == 0)) {
            return null;
        }
        String subject = this.mContext.getString(strResId);
        if (!TextUtils.isEmpty(this.mSubject)) {
            if (this.mSubject.startsWith(subject)) {
                subject = this.mSubject;
            } else {
                subject = subject + this.mSubject;
            }
        }
        return subject;
    }

    public String getForwordMsgBody(int strResId) {
        String forwardString = "";
        if (isMms()) {
            return forwardString;
        }
        boolean showFowardFrom = PreferenceUtils.getForwardMessageFrom(this.mContext);
        if (1 == this.mBoxId && showFowardFrom) {
            if (TextUtils.isEmpty(this.mContact)) {
                forwardString = this.mContext.getString(strResId, new Object[]{this.mAddress});
            } else {
                forwardString = this.mContext.getString(strResId, new Object[]{this.mContact});
            }
            forwardString = forwardString + System.lineSeparator() + this.mBody;
        } else {
            forwardString = this.mBody;
        }
        return forwardString;
    }

    public boolean isFailedMmsMessage() {
        return isFailedMessage() || this.mDeliveryStatus == DeliveryStatus.FAILED;
    }

    public boolean isFailedSmsMessage() {
        if (this.mBoxId != 5) {
            return isOutgoingMessage() && (isFailedMessage() || this.mDeliveryStatus == DeliveryStatus.FAILED);
        } else {
            return true;
        }
    }

    public boolean isSendingSmsMessage() {
        return this.mBoxId != 6 ? isSending() : true;
    }

    public boolean isSentAndReceivedSmsMessage() {
        return this.mBoxId == 2 && this.mDeliveryStatus == DeliveryStatus.RECEIVED;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setMsgStatus(Cursor cursor, ColumnsMap columnsMap) {
        if (this.mLoadType == 0 && "sms".equals(this.mType) && columnsMap.mColumnGroupFail < cursor.getColumnCount() && this.mIsMultiRecipients) {
            this.mGroupAllCnt = cursor.getInt(columnsMap.mColumnGroupAll);
            this.mGroupSentCnt = cursor.getInt(columnsMap.mColumnGroupSent);
            this.mGroupFailCnt = cursor.getInt(columnsMap.mColumnGroupFail);
        }
    }

    public EncodedStringValue[] getTo() {
        PduPersister p = PduPersister.getPduPersister(this.mContext);
        EncodedStringValue[] toArray = null;
        if (isMms()) {
            try {
                GenericPdu msg = p.load(this.mMessageUri);
                if (msg instanceof MultimediaMessagePdu) {
                    toArray = ((MultimediaMessagePdu) msg).getTo();
                } else if (msg instanceof DeliveryInd) {
                    toArray = ((DeliveryInd) msg).getTo();
                } else if (msg instanceof ReadOrigInd) {
                    toArray = ((ReadOrigInd) msg).getTo();
                } else if (msg instanceof ReadRecInd) {
                    toArray = ((ReadRecInd) msg).getTo();
                }
            } catch (MmsException e) {
                MLog.e(TAG, "getTo: " + e.getMessage());
            }
        }
        if (toArray == null) {
            return new EncodedStringValue[0];
        }
        return toArray;
    }

    public EncodedStringValue[] getCc() {
        PduPersister p = PduPersister.getPduPersister(this.mContext);
        EncodedStringValue[] ccArray = null;
        if (isMms()) {
            try {
                GenericPdu msg = p.load(this.mMessageUri);
                if (msg instanceof RetrieveConf) {
                    ccArray = ((RetrieveConf) msg).getCc();
                } else if (msg instanceof SendReq) {
                    ccArray = ((SendReq) msg).getCc();
                }
            } catch (MmsException e) {
                MLog.e(TAG, "getCc: " + e.getMessage());
            }
        }
        if (ccArray == null) {
            return new EncodedStringValue[0];
        }
        return ccArray;
    }

    public boolean isManualFailedMessage() {
        if (!isMms()) {
            return false;
        }
        if (this.mErrorType == 1) {
            return true;
        }
        return false;
    }

    public boolean isManualFailedMmsMessage(String uri) {
        if (!MessageListAdapter.getManualDownloadFromMap(uri) || MessageListAdapter.getDownloadingStatusFromMap(uri) || MessageListAdapter.getUserStopTransaction(uri)) {
            return false;
        }
        return isManualFailedMessage();
    }

    public SlideModel getFirstModel() {
        if (this.mSlideshow == null || this.mSlideshow.size() == 0) {
            return null;
        }
        return this.mSlideshow.get(0);
    }

    public boolean isFirstSlideVcardOrVcalendar() {
        if (this.mSlideshow == null || this.mSlideshow.size() == 0) {
            return false;
        }
        SlideModel slideMoel = this.mSlideshow.get(0);
        if (slideMoel == null || (!slideMoel.hasVcard() && !slideMoel.hasVCalendar())) {
            return false;
        }
        return true;
    }

    public long getCancelId() {
        if (isSms()) {
            if (this.mIsMultiRecipients) {
                return this.mUid;
            }
            return this.mMsgId;
        } else if (isRcsChat()) {
            return this.mMsgId;
        } else {
            return -this.mMsgId;
        }
    }

    public boolean isNotDelayMsg() {
        if (System.currentTimeMillis() - this.mDate > 8000 || isInComingMessage()) {
            return true;
        }
        return false;
    }

    public HashMap<String, Object> getExtendMap() {
        if (this.extend == null) {
            this.extend = new HashMap();
        }
        this.extend.put("msgId", Long.valueOf(this.mMsgId));
        this.extend.put("phoneNum", this.mAddress);
        this.extend.put("smsCenterNum", this.mSmsServiceCenter);
        this.extend.put("content", this.mBody);
        this.extend.put("simIndex", String.valueOf(this.mSubId));
        this.extend.put("HW_MEETING_WRAP", "true");
        return this.extend;
    }

    public void appendSimpleViewExtendMap(HashMap<String, Object> extendMap) {
        extendMap.put("smsId", Long.valueOf(this.mMsgId));
        extendMap.put("msgTime", Long.valueOf(this.mDate));
        extendMap.put("messageBody", this.mBody);
    }

    public void appendMapOfIsSecrect(HashMap<String, Object> extendMap) {
        extendMap.put(ContentUtil.DUOQU_IS_SAFE_VERIFY_CODE_KEY, String.valueOf(this.mIsSecret));
    }

    public CryptoMessageItem getCryptoMessageItem() {
        return this.mCryptoMessageItem;
    }

    public boolean hasText() {
        return (TextUtils.isEmpty(this.mBody) && TextUtils.isEmpty(this.mSubject)) ? false : true;
    }

    public void setOnMmsTextLoadCallBack(OnMmsTextLoadCallBack onMmsTextLoadCallBack) {
        this.mOnMmsTextLoadCallBack = onMmsTextLoadCallBack;
    }

    public String getNumber() {
        ContactList list = Conversation.get(this.mContext, this.mThreadId, false).getRecipients();
        if (list == null || list.size() <= 0) {
            return null;
        }
        return ((Contact) list.get(0)).getNumber();
    }

    public int getReciSize() {
        ContactList list = Conversation.get(this.mContext, this.mThreadId, false).getRecipients();
        if (list == null || list.size() <= 0) {
            return 0;
        }
        return list.size();
    }

    public String getName() {
        ContactList list = Conversation.get(this.mContext, this.mThreadId, false).getRecipients();
        if (list == null) {
            return null;
        }
        if (list.size() == 1) {
            return ((Contact) list.get(0)).getName();
        }
        if (list.size() <= 1) {
            return null;
        }
        String formatName = list.formatNames(", ");
        if (!TextUtils.isEmpty(formatName)) {
            formatName = list.formatNoNameContactNumber(", ");
        }
        return formatName;
    }

    public void clearModelChangeObservers() {
        if (this.mSlideshow != null) {
            this.mSlideshow.unregisterAllModelChangedObservers();
        }
    }

    public boolean isNeedChangeDrawableFroImage() {
        if (this.mAttachmentType == 1 || this.mAttachmentType == 2) {
            return true;
        }
        return this.mHasImageInFirstSlidShow;
    }

    public boolean isRcsServiceForFavorites() {
        if (this.mSmsServiceCenterForFavorites != null) {
            return this.mSmsServiceCenterForFavorites.startsWith("rcs");
        }
        return false;
    }
}
