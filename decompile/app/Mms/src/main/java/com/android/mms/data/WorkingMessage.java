package com.android.mms.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Telephony.Mms.Draft;
import android.provider.Telephony.Mms.Outbox;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Conversations;
import android.provider.Telephony.Threads;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Pair;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.common.userhappiness.UserHappinessSignals;
import com.android.mms.ContentRestrictionException;
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.model.ImageModel;
import com.android.mms.model.RegionModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.transaction.MessageSender;
import com.android.mms.transaction.MmsMessageSender;
import com.android.mms.transaction.SmsMessageSender;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.android.mms.ui.SlideshowEditor;
import com.android.mms.util.DraftCache;
import com.android.mms.util.PhoneNumberFormatter;
import com.android.mms.util.Recycler;
import com.android.mms.util.SignatureUtil;
import com.android.mms.util.ThumbnailManager;
import com.android.mms.widget.MmsWidgetProvider;
import com.android.rcs.data.RcsWorkingMessage;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.SendReq;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MccMncConfig;
import com.huawei.mms.util.MmsRadarInfoManager;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.incallui.service.MessagePlusService;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@SuppressLint({"NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi"})
public class WorkingMessage {
    private static final String[] MMS_DRAFT_PROJECTION = new String[]{"_id", "sub", "sub_cs"};
    private static final String[] MMS_OUTBOX_PROJECTION = new String[]{"_id", "m_size"};
    private static final String[] SMS_BODY_PROJECTION = new String[]{"body"};
    private static LoadDraftStatus sLoadDraftStatus = LoadDraftStatus.UNKNOWN;
    long dirtyThreadId = 0;
    private int mAttachmentType;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private Conversation mConversation;
    private CryptoWorkingMessage mCryptoWorkingMessage = new CryptoWorkingMessage();
    private boolean mDiscarded = false;
    private volatile boolean mHasMmsDraft;
    private volatile boolean mHasSmsDraft;
    private HwCustWorkingMessage mHwCustWorkingMessage = ((HwCustWorkingMessage) HwCustUtils.createObj(HwCustWorkingMessage.class, new Object[0]));
    private Uri mLastDraftUri = null;
    private Uri mMessageUri;
    private int mMmsState;
    private int mNewMessageDraftSubid = -1;
    RcsWorkingMessage mRcsWorkingMessage = new RcsWorkingMessage();
    private SlideshowModel mSlideshow;
    private SlideshowEditor mSlideshowEditor;
    private final MessageStatusListener mStatusListener;
    private CharSequence mSubject;
    private CharSequence mText;
    private List<String> mWorkingRecipients;

    public interface IDraftLoaded {
        void onDraftLoaded(Uri uri);
    }

    private enum LoadDraftStatus {
        UNKNOWN,
        LOADING,
        LOADED
    }

    public interface MessageStatusListener {
        void onEmailAddressInput();

        void onMessageSent();

        void onMessageStateChanged();

        void onPreMessageSent();

        void onProtocolChanged(boolean z);
    }

    private boolean hasDraftMessageToDelete(android.net.Uri r11, java.lang.String r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x004d in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = this;
        r9 = 0;
        r8 = 1;
        r6 = 0;
        r0 = r10.mContext;	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r1 = 1;	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r2 = new java.lang.String[r1];	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r1 = "_id";	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r3 = 0;	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r2[r3] = r1;	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r4 = 0;	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r5 = 0;	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r1 = r11;	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r3 = r12;	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r6 = com.huawei.cspcommon.ex.SqliteWrapper.query(r0, r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        if (r6 != 0) goto L_0x001e;
    L_0x0018:
        if (r6 == 0) goto L_0x001d;
    L_0x001a:
        r6.close();
    L_0x001d:
        return r8;
    L_0x001e:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        if (r0 <= 0) goto L_0x002b;
    L_0x0024:
        r0 = r8;
    L_0x0025:
        if (r6 == 0) goto L_0x002a;
    L_0x0027:
        r6.close();
    L_0x002a:
        return r0;
    L_0x002b:
        r0 = r9;
        goto L_0x0025;
    L_0x002d:
        r7 = move-exception;
        r0 = "WorkingMessage";	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r1.<init>();	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r2 = "query whether hasDraftMessageToDelete occur exception: ";	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r1 = r1.append(r7);	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        r1 = r1.toString();	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        com.huawei.cspcommon.MLog.e(r0, r1);	 Catch:{ Exception -> 0x002d, all -> 0x004e }
        if (r6 == 0) goto L_0x004d;
    L_0x004a:
        r6.close();
    L_0x004d:
        return r8;
    L_0x004e:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0054;
    L_0x0051:
        r6.close();
    L_0x0054:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.data.WorkingMessage.hasDraftMessageToDelete(android.net.Uri, java.lang.String):boolean");
    }

    private void updateState(int r1, boolean r2, boolean r3) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.mms.data.WorkingMessage.updateState(int, boolean, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.data.WorkingMessage.updateState(int, boolean, boolean):void");
    }

    private WorkingMessage(Context context, MessageStatusListener listener) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mStatusListener = listener;
        this.mAttachmentType = 0;
        this.mText = "";
        if (this.mHwCustWorkingMessage != null) {
            this.mHwCustWorkingMessage.setHwCustWorkingMessage(this.mContext);
        }
        this.mRcsWorkingMessage.setRcsWorkingMessage(this.mContext);
    }

    public static WorkingMessage createEmpty(Context context, MessageStatusListener listener) {
        return new WorkingMessage(context, listener);
    }

    public static WorkingMessage load(Context context, MessageStatusListener listener, Uri uri) {
        return load(context, listener, uri, true);
    }

    public static WorkingMessage load(Context context, MessageStatusListener listener, Uri uri, boolean shouldCorrectState) {
        if (!uri.toString().startsWith(Draft.CONTENT_URI.toString())) {
            PduPersister persister = PduPersister.getPduPersister(context);
            debug("load: moving %s to drafts", uri);
            Uri uriTemp = uri;
            try {
                uri = persister.move(uri, Draft.CONTENT_URI);
                MmsApp.getApplication().getPduLoaderManager().removePdu(uriTemp);
            } catch (MmsException e) {
                LogTag.error("Can't move %s to drafts", uri);
                return null;
            }
        }
        WorkingMessage msg = new WorkingMessage(context, listener);
        if (!msg.loadFromUri(uri, shouldCorrectState)) {
            return null;
        }
        msg.mHasMmsDraft = true;
        return msg;
    }

    private void correctAttachmentState(boolean notify) {
        correctAttachmentState(notify, true);
    }

    public void correctAttachmentState(boolean notify, boolean isUpdateState) {
        boolean z = false;
        if (this.mSlideshow != null) {
            int slideCount = this.mSlideshow.size();
            if (slideCount == 0) {
                if (!isUpdateState) {
                    z = true;
                }
                removeAttachment(z);
            } else if (slideCount > 1) {
                this.mAttachmentType = 4;
            } else {
                SlideModel slide = this.mSlideshow.get(0);
                if (slide.hasImage()) {
                    this.mAttachmentType = 1;
                } else if (slide.hasVideo()) {
                    this.mAttachmentType = 2;
                } else if (slide.hasAudio()) {
                    this.mAttachmentType = 3;
                } else if (slide.hasVcard()) {
                    this.mAttachmentType = 5;
                } else if (slide.hasVCalendar()) {
                    this.mAttachmentType = 6;
                }
            }
            if (isUpdateState) {
                updateState(4, hasAttachment(), notify);
            }
        }
    }

    public boolean ensureSlideshow() {
        if (this.mSlideshow != null) {
            return false;
        }
        this.mSlideshow = SlideshowModel.createNew(this.mContext);
        this.mSlideshowEditor = new SlideshowEditor(this.mContext, this.mSlideshow);
        this.mSlideshowEditor.addNewSlide(0);
        return true;
    }

    public void syncTextToSlideshow(String newText) {
        if (this.mSlideshow != null) {
            this.mSlideshowEditor.changeText(0, newText);
        }
    }

    private boolean loadFromUri(Uri uri) {
        return loadFromUri(uri, true);
    }

    private boolean loadFromUri(Uri uri, boolean shouldCorrectState) {
        if (MLog.isLoggable("Mms_app", 2)) {
            LogTag.debug("loadFromUri %s", uri);
        }
        try {
            this.mSlideshow = SlideshowModel.createFromMessageUri(this.mContext, uri);
            if (this.mSlideshow != null) {
                this.mSlideshowEditor = new SlideshowEditor(this.mContext, this.mSlideshow);
                for (int i = 0; i < this.mSlideshow.size(); i++) {
                    if (this.mSlideshow.get(i).getText() == null) {
                        this.mSlideshowEditor.changeText(i, "");
                    }
                }
            }
            this.mMessageUri = uri;
            syncTextFromSlideshow();
            if (shouldCorrectState) {
                correctAttachmentState(false);
            }
            return true;
        } catch (MmsException e) {
            LogTag.error("Couldn't load URI %s", uri);
            return false;
        }
    }

    public static boolean isDraftLoading() {
        return sLoadDraftStatus == LoadDraftStatus.LOADING;
    }

    public static void setDraftStateUnknow() {
        sLoadDraftStatus = LoadDraftStatus.UNKNOWN;
    }

    public static WorkingMessage loadDraft(Context context, MessageStatusListener listener, final Conversation conv, final IDraftLoaded loadedCallback) {
        if (MLog.isLoggable("Mms_app", 2)) {
            LogTag.debug("WorkingMessage", "loadDraft::tid" + conv.getThreadId());
        }
        sLoadDraftStatus = LoadDraftStatus.LOADING;
        final WorkingMessage msg = createEmpty(context, listener);
        long threadId = conv.getThreadId();
        if (MmsConfig.isSupportDraftWithoutRecipient() ? threadId < 0 : threadId <= 0) {
            if (loadedCallback != null) {
                loadedCallback.onDraftLoaded(null);
            }
            sLoadDraftStatus = LoadDraftStatus.LOADED;
            return msg;
        }
        Handler handler = new Handler() {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1000:
                        final WorkingMessage workingMessage = msg;
                        final Conversation conversation = conv;
                        final IDraftLoaded iDraftLoaded = loadedCallback;
                        new AsyncTask<Void, Void, Pair<String, String>>() {
                            @SuppressLint({"NewApi"})
                            protected Pair<String, String> doInBackground(Void... none) {
                                String draftText = workingMessage.readDraftSmsMessage(conversation);
                                Object subject = null;
                                if (TextUtils.isEmpty(draftText)) {
                                    StringBuilder sb = new StringBuilder();
                                    Uri uri = WorkingMessage.readDraftMmsMessage(workingMessage.mContext, conversation, sb);
                                    if (uri != null && workingMessage.loadFromUri(uri)) {
                                        subject = sb.toString();
                                    }
                                }
                                return new Pair(draftText, subject);
                            }

                            protected void onPostExecute(Pair<String, String> result) {
                                if (result != null) {
                                    if (!TextUtils.isEmpty((CharSequence) result.first)) {
                                        workingMessage.mHasSmsDraft = true;
                                        workingMessage.setText((CharSequence) result.first);
                                    }
                                    if (result.second != null) {
                                        workingMessage.mHasMmsDraft = true;
                                        if (!TextUtils.isEmpty((CharSequence) result.second)) {
                                            workingMessage.setSubject((CharSequence) result.second, false);
                                        }
                                    }
                                    if (iDraftLoaded != null) {
                                        iDraftLoaded.onDraftLoaded(workingMessage.mHasMmsDraft ? workingMessage.getMessageUri() : null);
                                    }
                                    WorkingMessage.sLoadDraftStatus = LoadDraftStatus.LOADED;
                                    conversation.setHasTempDraft(false);
                                }
                            }
                        }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
                        return;
                    default:
                        return;
                }
            }
        };
        if (MmsConfig.isSupportDraftWithoutRecipient() && HwMessageUtils.isSuperPowerSaveModeOn() && threadId == 0) {
            handler.sendEmptyMessageDelayed(1000, 500);
        } else {
            handler.sendEmptyMessage(1000);
        }
        return msg;
    }

    public void setText(CharSequence s) {
        if (s == null) {
            s = "";
        }
        this.mText = s.toString();
    }

    public CharSequence getText() {
        return this.mText;
    }

    public CharSequence get7BitText() {
        CharSequence text7Bit = MccMncConfig.is7bitEnable() ? MessageUtils.replaceAlphabetFor7Bit(this.mText, 0, this.mText.length()) : this.mText;
        return text7Bit == null ? this.mText : text7Bit;
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(this.mText);
    }

    public void removeAttachment(boolean notify) {
        removeThumbnailsFromCache(this.mSlideshow);
        this.mAttachmentType = 0;
        this.mSlideshow = null;
        this.mSlideshowEditor = null;
        if (this.mMessageUri != null) {
            asyncDelete(this.mMessageUri, null, null);
            this.mMessageUri = null;
        }
        updateState(4, false, notify);
    }

    public static void removeThumbnailsFromCache(SlideshowModel slideshow) {
        if (slideshow != null) {
            ThumbnailManager thumbnailManager = MmsApp.getApplication().getThumbnailManager();
            boolean removedSomething = false;
            Iterator<SlideModel> iterator = slideshow.iterator();
            while (iterator.hasNext()) {
                SlideModel slideModel = (SlideModel) iterator.next();
                if (slideModel.hasImage()) {
                    thumbnailManager.removeThumbnail(slideModel.getImage().getUri());
                    removedSomething = true;
                } else if (slideModel.hasVideo()) {
                    thumbnailManager.removeThumbnail(slideModel.getVideo().getUri());
                    removedSomething = true;
                }
            }
            if (removedSomething) {
                MmsApp.getApplication().getThumbnailManager().clearBackingStore();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isWorthSaving() {
        if (hasText() || hasSubject() || hasAttachment() || hasSlideshow() || isFakeMmsForDraft()) {
            return true;
        }
        return false;
    }

    private void cancelThumbnailLoading() {
        int numSlides = 0;
        if (this.mSlideshow != null) {
            numSlides = this.mSlideshow.size();
        }
        if (numSlides > 0) {
            ImageModel imgModel = this.mSlideshow.get(numSlides - 1).getImage();
            if (imgModel != null) {
                imgModel.cancelThumbnailLoading();
            }
        }
    }

    public boolean isFakeMmsForDraft() {
        return (this.mMmsState & 16) != 0;
    }

    public boolean hasAttachment() {
        return this.mAttachmentType > 0;
    }

    public SlideshowModel getSlideshow() {
        return this.mSlideshow;
    }

    public SlideshowEditor getSlideshowEditor() {
        return this.mSlideshowEditor;
    }

    public boolean hasSlideshow() {
        return this.mAttachmentType == 4;
    }

    public boolean hasSmsDraft() {
        return this.mHasSmsDraft;
    }

    public boolean hasMmsDraft() {
        return this.mHasMmsDraft;
    }

    public void setSubject(CharSequence s, boolean notify) {
        this.mSubject = s;
        updateState(2, s != null, notify);
    }

    public CharSequence getSubject() {
        return this.mSubject;
    }

    public boolean hasSubject() {
        return this.mSubject != null && TextUtils.getTrimmedLength(this.mSubject) > 0;
    }

    private void syncTextFromSlideshow() {
        if (this.mSlideshow != null && this.mSlideshow.size() == 1) {
            SlideModel slide = this.mSlideshow.get(0);
            if (slide != null && slide.hasText()) {
                setText(slide.getText().getText());
            }
        }
    }

    private void removeSubjectIfEmpty(boolean notify) {
        if (!hasSubject()) {
            setSubject(null, notify);
        }
    }

    private void prepareForSave(boolean notify) {
        prepareForSave(notify, false);
    }

    private void prepareForSave(boolean notify, boolean sendMsg) {
        syncWorkingRecipients();
        if (hasMmsContentToSave() || (sendMsg && hasOnlySignaTextToSave())) {
            try {
                if (this.mSlideshow == null) {
                    ensureSlideshow();
                    syncTextToSlideshow(this.mText.toString());
                }
            } catch (ExceedMessageSizeException e) {
                MLog.v("WorkingMessage", "ExceedMessageSizeException");
            }
        }
    }

    public void syncWorkingRecipients() {
        boolean z = false;
        if (this.mWorkingRecipients != null) {
            ContactList recipients = ContactList.getByNumbers(this.mWorkingRecipients, false, true);
            this.mConversation.setRecipients(recipients);
            if (recipients.size() > 1) {
                z = true;
            }
            setHasMultipleRecipients(z, true);
            this.mWorkingRecipients = null;
        }
    }

    public String getWorkingRecipients() {
        if (this.mWorkingRecipients == null) {
            return null;
        }
        return ContactList.getByNumbers(this.mWorkingRecipients, false).serialize();
    }

    public void removeFakeMmsForDraft() {
        updateState(16, false, false);
    }

    public Uri saveAsMms(boolean notify, boolean takePictureState) {
        if (!this.mDiscarded || takePictureState) {
            return saveAsMms(notify);
        }
        LogTag.warn("workingmessage:: saveAsMms mDiscarded: true mConversation: " + this.mConversation.getThreadId() + " returning NULL uri and bailing", new Object[0]);
        return null;
    }

    public Uri saveAsMms(boolean notify) {
        debug("saveAsMms mConversation=" + this.mConversation.getThreadId());
        updateState(16, true, notify);
        prepareForSave(true);
        if (this.mSlideshow != null) {
            this.mSlideshow.clearPduCache();
        }
        try {
            DraftCache.getInstance().setSavingDraft(true);
            if (!this.mConversation.getRecipients().isEmpty()) {
                this.mConversation.ensureThreadId();
            }
            this.mConversation.setDraftState(true);
            PduPersister persister = PduPersister.getPduPersister(this.mContext);
            SendReq sendReq = makeSendReq(this.mConversation, this.mSubject);
            int subid = this.mNewMessageDraftSubid;
            if (subid == -1) {
                subid = this.mConversation.getSubId();
            }
            if (this.mMessageUri == null) {
                this.mMessageUri = createDraftMmsMessage(persister, sendReq, this.mSlideshow, null, this.mContext, null, subid);
            } else {
                updateDraftMmsMessage(this.mMessageUri, persister, this.mSlideshow, sendReq, null, this.mContext, subid);
            }
            this.mHasMmsDraft = true;
            delOldDraftMms(this.mConversation, this.mMessageUri);
            return this.mMessageUri;
        } finally {
            DraftCache.getInstance().setSavingDraft(false);
        }
    }

    public void saveDraft(boolean isStopping) {
        boolean z = true;
        if (this.mRcsWorkingMessage != null && this.mRcsWorkingMessage.isRcsSwitchOn() && this.mDiscarded) {
            this.mHasMmsDraft = this.mRcsWorkingMessage.isHasDraft(this.mHasMmsDraft);
            this.mHasSmsDraft = this.mRcsWorkingMessage.isHasDraft(this.mHasSmsDraft);
        } else if (this.mConversation == null) {
            throw new IllegalStateException("saveDraft() called with no conversation");
        } else {
            debug("workingmessage:: saveDraft for mConversation " + this.mConversation.getThreadId() + " stop=" + isStopping + " state " + this.mMmsState + " " + this.mMessageUri);
            Uri oldUri = this.mConversation.getUri();
            prepareForSave(false);
            if (this.mSlideshow != null) {
                this.mSlideshow.clearPduCache();
            }
            this.mHasSmsDraft = false;
            this.mHasMmsDraft = false;
            if (!requiresMms()) {
                String content = this.mText.toString();
                String signature = SignatureUtil.getSignature(this.mContext, MmsConfig.getDefaultSignatureText());
                if (this.dirtyThreadId != 0) {
                    asyncDelete(ContentUris.withAppendedId(Conversations.CONTENT_URI, this.dirtyThreadId), "type=3", null);
                }
                if (TextUtils.isEmpty(content) || SignatureUtil.deleteNewlineSymbol(content).equals(signature)) {
                    this.mHasSmsDraft = false;
                } else {
                    asyncUpdateDraftSmsMessage(this.mConversation, content, isStopping, oldUri);
                    this.mHasSmsDraft = true;
                }
            } else if (hasMmsContentToSave()) {
                asyncUpdateDraftMmsMessage(this.mConversation, isStopping, oldUri);
                this.mHasMmsDraft = true;
            }
            if (this.mHasMmsDraft) {
                if (!(MmsConfig.isSupportDraftWithoutRecipient() && this.mConversation.getThreadId() == 0)) {
                    debug("Savedraft has sms draft to be deleted:" + this.mConversation.getThreadId() + " Conv.hasDraft " + this.mConversation.hasDraft());
                    asyncDeleteDraftSmsMessage(this.mConversation);
                }
            } else if (!this.mHasSmsDraft) {
                if (this.mMessageUri != null) {
                    debug("WorkingMessage", "SaveDraft delete " + this.mMessageUri);
                    asyncDelete(this.mMessageUri, null, null);
                }
                if (MmsConfig.isSupportDraftWithoutRecipient() && this.mConversation.getThreadId() == 0) {
                    asyncDeleteDraftMmsMessage(this.mConversation);
                    this.mMessageUri = null;
                } else {
                    asyncDeleteDraftSmsMessage(this.mConversation);
                    asyncDeleteDraftMmsMessage(this.mConversation);
                    this.mMessageUri = null;
                }
            } else if (!(MmsConfig.isSupportDraftWithoutRecipient() && this.mConversation.getThreadId() == 0)) {
                debug("Savedraft has mms drfat tobe deleted:" + this.mConversation.getThreadId() + " Conv.hasDraft " + this.mConversation.hasDraft());
                asyncDeleteDraftMmsMessage(this.mConversation);
                this.mMessageUri = null;
            }
            Conversation conversation = this.mConversation;
            if (!this.mHasSmsDraft) {
                z = this.mHasMmsDraft;
            }
            conversation.setDraftState(z);
        }
    }

    public synchronized void discard() {
        discard(-1);
    }

    public synchronized void discard(long msgId) {
        if (MLog.isLoggable("Mms_app", 2)) {
            LogTag.debug("[WorkingMessage] discard", new Object[0]);
        }
        if (sLoadDraftStatus != LoadDraftStatus.LOADING) {
            if (!this.mDiscarded) {
                if (this.mConversation == null) {
                    MLog.w("WorkingMessage", "Discard but conversation is not initialized");
                    return;
                }
                if (this.mMessageUri != null) {
                    asyncDelete(this.mMessageUri, null, null);
                }
                this.mDiscarded = true;
                cancelThumbnailLoading();
                if (this.mHasMmsDraft) {
                    asyncDeleteDraftMmsMessage(this.mConversation, msgId);
                }
                if (this.mHasSmsDraft || this.mConversation.hasTempDraft()) {
                    asyncDeleteDraftSmsMessage(this.mConversation);
                }
                clearConversation(this.mConversation, true);
            }
        }
    }

    public boolean setDiscarded(boolean discard) {
        this.mDiscarded = discard;
        return discard;
    }

    public boolean isDiscarded() {
        return this.mDiscarded;
    }

    public void writeStateToBundle(Bundle bundle) {
        if (hasSubject()) {
            bundle.putString("subject", this.mSubject.toString());
        }
        if (this.mMessageUri != null) {
            bundle.putParcelable("msg_uri", this.mMessageUri);
        } else if (hasText()) {
            bundle.putString("sms_body", this.mText.toString());
        }
    }

    public void readStateFromBundle(Bundle bundle) {
        if (bundle != null) {
            setSubject(bundle.getString("subject"), false);
            Uri uri = (Uri) bundle.getParcelable("msg_uri");
            if (uri != null) {
                loadFromUri(uri);
                return;
            }
            String body = bundle.getString("sms_body");
            setText(body);
            if (!TextUtils.isEmpty(body)) {
                this.mHasSmsDraft = true;
            }
        }
    }

    public void setWorkingRecipients(List<String> numbers) {
        this.mWorkingRecipients = numbers;
        if (numbers != null) {
            int size = numbers.size();
            String s;
            switch (size) {
                case 0:
                    s = "empty";
                    return;
                case 1:
                    s = (String) numbers.get(0);
                    return;
                default:
                    s = "{...} len=" + size;
                    return;
            }
        }
    }

    public void setConversation(Conversation conv) {
        boolean z = true;
        this.mConversation = conv;
        ContactList contactList = conv.getRecipients();
        setHasEmail(contactList.containsEmail(), false);
        if (contactList.size() <= 1) {
            z = false;
        }
        setHasMultipleRecipients(z, false);
    }

    public Conversation getConversation() {
        return this.mConversation;
    }

    public void setHasEmail(boolean hasEmail, boolean notify) {
        if (MmsConfig.getEmailGateway() != null) {
            updateState(1, false, notify);
        } else {
            updateState(1, hasEmail, notify);
        }
    }

    public void setHasMultipleRecipients(boolean hasMultipleRecipients, boolean notify) {
        boolean isGroupMmsEnabled = hasMultipleRecipients ? PreferenceUtils.getIsGroupMmsEnabled(this.mContext) : false;
        if (this.mHwCustWorkingMessage != null) {
            isGroupMmsEnabled &= this.mHwCustWorkingMessage.customizeUpdateState(this.mWorkingRecipients, this.mConversation) ? 0 : 1;
        }
        updateState(32, isGroupMmsEnabled, notify);
    }

    public boolean requiresMms() {
        return this.mMmsState > 0;
    }

    public boolean hasEmailRecipientRequireMms() {
        return this.mMmsState == 1;
    }

    public boolean hasMmsContentToSave() {
        if (this.mMmsState == 0) {
            return false;
        }
        if (this.mMmsState == 32 && !hasValidText()) {
            return false;
        }
        if ((this.mMmsState & 16) != 0 || hasAttachment() || hasValidText() || hasSubject()) {
            return true;
        }
        return false;
    }

    public void setLengthRequiresMms(boolean mmsRequired, boolean notify) {
        updateState(8, mmsRequired, notify);
    }

    private static String stateString(int state) {
        if (state == 0) {
            return "<none>";
        }
        StringBuilder sb = new StringBuilder();
        if ((state & 1) != 0) {
            sb.append("RECIPIENTS_REQUIRE_MMS | ");
        }
        if ((state & 2) != 0) {
            sb.append("HAS_SUBJECT | ");
        }
        if ((state & 4) != 0) {
            sb.append("HAS_ATTACHMENT | ");
        }
        if ((state & 8) != 0) {
            sb.append("LENGTH_REQUIRES_MMS | ");
        }
        if ((state & 16) != 0) {
            sb.append("FORCE_MMS | ");
        }
        if ((state & 32) != 0) {
            sb.append("MULTIPLE_RECIPIENTS | ");
        }
        sb.delete(sb.length() - 3, sb.length());
        return sb.toString();
    }

    public void send(String recipientsInUI, int subscription) {
        long origThreadId = this.mConversation.getThreadId();
        if (MLog.isLoggable("Mms_TXN", 2)) {
            LogTag.debug("WorkingMessage  Mms_TX send origThreadId: " + origThreadId, new Object[0]);
        }
        removeSubjectIfEmpty(true);
        prepareForSave(true, true);
        if (this.mSlideshow != null) {
            this.mSlideshow.clearPduCache();
        }
        final Conversation conv = this.mConversation;
        final Conversation mConversationCopy = new Conversation(this.mConversation);
        String msgTxt = this.mText.toString();
        ContactList pureEmailAddressRecipients = mConversationCopy.getPureEmailAddressRecipients();
        ContactList purePhoneNumberRecipients = mConversationCopy.getPurePhoneNumberRecipients();
        if (MmsConfig.enableSplitNumberAndEmailRecipients() && pureEmailAddressRecipients.size() > 0 && purePhoneNumberRecipients.size() > 0 && hasEmailRecipientRequireMms()) {
            conv.setRecipients(pureEmailAddressRecipients);
            mConversationCopy.setRecipients(purePhoneNumberRecipients);
        }
        if (msgTxt.length() > 70) {
            StatisticalHelper.incrementReportCount(this.mContext, 2062);
        }
        if (conv.getRecipients().size() > 1) {
            StatisticalHelper.incrementReportCount(this.mContext, 2019);
        } else {
            StatisticalHelper.incrementReportCount(this.mContext, 2018);
        }
        int i;
        if (!requiresMms() && !addressContainsEmailToMms(conv, msgTxt)) {
            if (MessageUtils.isMultiSimActive()) {
                if (subscription == 0) {
                    StatisticalHelper.incrementReportCount(this.mContext, 2008);
                } else if (1 == subscription) {
                    StatisticalHelper.incrementReportCount(this.mContext, 2009);
                }
            }
            i = subscription;
            final Conversation conversation = conv;
            final String charSequence = this.mText.toString();
            final String str = recipientsInUI;
            final int i2 = subscription;
            ThreadEx.execute(new Runnable() {
                public void run() {
                    WorkingMessage.this.preSendSmsWorker(conversation, charSequence, str, i2);
                    RecipientIdCache.updateNumbers(conversation.getThreadId(), conversation.getRecipients());
                    WorkingMessage.this.updateSendStats(conversation);
                }
            });
        } else if (MmsConfig.getUaProfUrl() == null) {
            String err = "Mms_TX WorkingMessage.send MMS sending failure. mms_config.xml is missing uaProfUrl setting.  uaProfUrl is required for MMS service, but can be absent for SMS.";
            Throwable contentRestrictionException = new ContentRestrictionException(err);
            MLog.e("WorkingMessage", err, contentRestrictionException);
            throw contentRestrictionException;
        } else {
            final Uri mmsUri = this.mMessageUri;
            final PduPersister persister = PduPersister.getPduPersister(this.mContext);
            final SlideshowModel slideshow = this.mSlideshow;
            final CharSequence subject = this.mSubject;
            i = subscription;
            final boolean textOnly = this.mAttachmentType == 0;
            final int i3 = subscription;
            ThreadEx.getSerialExecutor().execute(new Runnable() {
                public void run() {
                    SendReq sendReq = WorkingMessage.makeSendReq(conv, subject);
                    if (slideshow != null) {
                        slideshow.prepareForSend();
                    } else {
                        MLog.e("WorkingMessage", "Mms_TX send exception:: slideshow is null!!");
                    }
                    if (slideshow != null) {
                        WorkingMessage.this.syncTextForLoation(slideshow);
                    }
                    if (mmsUri != null) {
                        MessagePlusService.addToMmsUriList(mmsUri.toString().replace("drafts", "sent"));
                    }
                    WorkingMessage.this.sendMmsWorker(conv, mmsUri, persister, slideshow, sendReq, textOnly, i3);
                    RecipientIdCache.updateNumbers(conv.getThreadId(), conv.getRecipients());
                    WorkingMessage.this.updateSendStats(conv);
                }
            });
            if (MmsConfig.enableSplitNumberAndEmailRecipients() && pureEmailAddressRecipients.size() > 0 && purePhoneNumberRecipients.size() > 0 && hasEmailRecipientRequireMms()) {
                final String recipientsInUIContainsNumberOnly = purePhoneNumberRecipients.serialize();
                final String msgText = this.mText.toString();
                final int i4 = subscription;
                ThreadEx.getSerialExecutor().execute(new Runnable() {
                    public void run() {
                        WorkingMessage.this.preSendSmsWorker(mConversationCopy, msgText, recipientsInUIContainsNumberOnly, i4);
                        WorkingMessage.this.updateSendStats(mConversationCopy);
                        RecipientIdCache.updateNumbers(mConversationCopy.getThreadId(), mConversationCopy.getRecipients());
                    }
                });
            }
        }
        this.mDiscarded = true;
    }

    private void syncTextForLoation(SlideshowModel slideshow) {
        MLog.d("WorkingMessage", "syncText for Loation");
        for (int i = 0; i < slideshow.size(); i++) {
            SlideModel slideModel = slideshow.get(i);
            if (!(slideModel == null || !slideModel.hasLocation() || slideModel.getImage().getLocationSource() == null)) {
                String locationMsg = (String) slideModel.getImage().getLocationSource().get("locationinfo");
                if (slideModel.getText() != null) {
                    String text = slideModel.getText().getText();
                    if (TextUtils.isEmpty(text)) {
                        this.mSlideshowEditor.changeText(i, locationMsg);
                    } else {
                        this.mSlideshowEditor.changeText(i, locationMsg + "\n" + text);
                    }
                }
            }
        }
    }

    private void updateSendStats(Conversation conv) {
        String[] dests = conv.getRecipients().getNumbers();
        try {
            ArrayList<String> formatNumbers = new ArrayList();
            for (String number : dests) {
                formatNumbers.add(HwMessageUtils.replaceNumberFromDatabase(number, this.mContext));
            }
            HwMessageUtils.updateRecentContactsToDB(this.mContext, formatNumbers);
        } catch (SQLiteException e) {
            MLog.e("WorkingMessage", "too many SQL variables");
        }
    }

    private boolean addressContainsEmailToMms(Conversation conv, String text) {
        if (this.mHwCustWorkingMessage == null || !this.mHwCustWorkingMessage.supportSendToEmail()) {
            if (MmsConfig.getEmailGateway() != null) {
                String[] dests = conv.getRecipients().getNumbers();
                int length = dests.length;
                int i = 0;
                while (i < length) {
                    if ((Contact.isEmailAddress(dests[i]) || MessageUtils.isAlias(dests[i])) && SmsMessage.calculateLength(dests[i] + " " + text, false)[0] > 1) {
                        updateState(1, true, true);
                        if (this.mSlideshow == null) {
                            ensureSlideshow();
                            syncTextToSlideshow(this.mText.toString());
                        }
                        return true;
                    }
                    i++;
                }
            }
            return false;
        }
        MLog.w("WorkingMessage", "supportSendToEmail returned");
        return false;
    }

    private void preSendSmsWorker(Conversation conv, String msgText, String recipientsInUI, int subscription) {
        String msg;
        UserHappinessSignals.userAcceptedImeText(this.mContext);
        this.mStatusListener.onPreMessageSent();
        long origThreadId = conv.getThreadId();
        long threadId = conv.ensureThreadId();
        String semiSepRecipients = conv.getRecipients().serialize();
        if (origThreadId == 0 || origThreadId == threadId) {
            if (!semiSepRecipients.equals(recipientsInUI)) {
                if (TextUtils.isEmpty(recipientsInUI)) {
                }
            }
            sendSmsWorker(msgText, semiSepRecipients, threadId, subscription);
            deleteDraftSmsMessage(threadId);
            deleteDraftMmsMessage(threadId);
        }
        if (origThreadId == 0 || origThreadId == threadId) {
            msg = "Recipients in window: \"" + recipientsInUI + "\" differ from recipients from conv: \"" + semiSepRecipients + "\"";
        } else {
            msg = "WorkingMessage.preSendSmsWorker threadId changed or recipients changed. origThreadId: " + origThreadId + " new threadId: " + threadId + " also mConversation.getThreadId(): " + this.mConversation.getThreadId();
        }
        if (this.mContext instanceof Activity) {
            LogTag.warnPossibleRecipientMismatch(msg, (Activity) this.mContext);
        }
        sendSmsWorker(msgText, semiSepRecipients, threadId, subscription);
        deleteDraftSmsMessage(threadId);
        deleteDraftMmsMessage(threadId);
    }

    private void sendSmsWorker(String msgText, String semiSepRecipients, long threadId, int subscription) {
        String[] dests = TextUtils.split(semiSepRecipients, ";");
        for (int i = 0; i < dests.length; i++) {
            String afterParseContact = MessageUtils.parseMmsAddress(dests[i]);
            if (!TextUtils.isEmpty(afterParseContact)) {
                dests[i] = afterParseContact;
            }
        }
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.d("Mms_TXN", "sendSmsWorker sending  for threadId=" + threadId);
        }
        if (MccMncConfig.is7bitEnable() && !this.mCryptoWorkingMessage.isCryptoMessage(this.mContext, this.mConversation.getRecipients())) {
            CharSequence snew = MessageUtils.replaceAlphabetFor7Bit(msgText.subSequence(0, msgText.length()), 0, msgText.length());
            if (snew != null) {
                msgText = snew.toString();
            }
        }
        msgText = this.mCryptoWorkingMessage.localEncrypt(msgText, subscription, this.mContext, this.mConversation.getRecipients());
        if (TextUtils.isEmpty(msgText)) {
            if (this.mCryptoWorkingMessage.isCryptoMessage(this.mContext, this.mConversation.getRecipients())) {
                MLog.e("WorkingMessage", "Encrypted Message localEncrypt failed, return empty(sms).");
            } else {
                MLog.e("WorkingMessage", "send msgText is empty");
            }
        }
        MessageSender sender = new SmsMessageSender(this.mContext, dests, msgText, threadId, subscription);
        if (MLog.isHwLoggable()) {
            String applicationName = (String) this.mContext.getPackageManager().getApplicationLabel(this.mContext.getApplicationInfo());
            MLog.i(" ctaifs <" + applicationName + ">[" + applicationName + "][" + this.mContext.getApplicationInfo().packageName + "]", "[WorkingMessage.sendSmsWorker]" + this.mContext.getString(R.string.send_sms) + TextUtils.join(";", PhoneNumberFormatter.transSafeNumbers(dests)));
        }
        try {
            sender.sendMessage(threadId);
            Recycler.getSmsRecycler().deleteOldMessagesByThreadId(this.mContext, threadId);
        } catch (Exception e) {
            MLog.e("WorkingMessage", "Failed to send SMS message, threadId=" + threadId, (Throwable) e);
            MmsRadarInfoManager.getInstance().writeLogMsg(1311, e.getMessage());
        }
        this.mStatusListener.onMessageSent();
        MmsWidgetProvider.notifyDatasetChanged(this.mContext);
    }

    private void sendMmsWorker(Conversation conv, Uri mmsUri, PduPersister persister, SlideshowModel slideshow, SendReq sendReq, boolean textOnly, int subscription) {
        ContentValues values;
        long threadId = 0;
        boolean newMessage = false;
        try {
            DraftCache.getInstance().setSavingDraft(true);
            this.mStatusListener.onPreMessageSent();
            threadId = conv.ensureThreadId();
            if (MLog.isLoggable("Mms_app", 2)) {
                LogTag.debug("sendMmsWorker: update draft MMS message " + mmsUri + " threadId: " + threadId, new Object[0]);
            }
            String[] dests = conv.getRecipients().getNumbers(true);
            if (dests.length == 1) {
                String newAddress = Conversation.verifySingleRecipient(this.mContext, conv.getThreadId(), dests[0]);
                if (MLog.isLoggable("Mms_app", 2)) {
                    LogTag.debug("sendMmsWorker: newAddress ****", new Object[0]);
                }
                if (!dests[0].equals(newAddress)) {
                    dests[0] = newAddress;
                    EncodedStringValue[] encodedNumbers = EncodedStringValue.encodeStrings(dests);
                    if (encodedNumbers != null) {
                        if (MLog.isLoggable("Mms_app", 2)) {
                            LogTag.debug("sendMmsWorker: REPLACING number!!!", new Object[0]);
                        }
                        sendReq.setTo(encodedNumbers);
                    }
                }
            }
            newMessage = mmsUri == null;
            if (newMessage) {
                values = new ContentValues();
                values.put("msg_box", Integer.valueOf(4));
                values.put("thread_id", Long.valueOf(threadId));
                values.put("m_type", Integer.valueOf(128));
                if (textOnly) {
                    values.put("text_only", Integer.valueOf(1));
                }
                mmsUri = SqliteWrapper.insert(this.mContext, this.mContentResolver, Outbox.CONTENT_URI, values);
            }
            UserHappinessSignals.userAcceptedImeText(this.mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (newMessage) {
            try {
                mmsUri = createDraftMmsMessage(persister, sendReq, slideshow, mmsUri, this.mContext, null, subscription);
            } catch (Throwable th) {
                DraftCache.getInstance().setSavingDraft(false);
            }
        } else {
            updateDraftMmsMessage(mmsUri, persister, slideshow, sendReq, null, this.mContext, subscription);
        }
        deleteDraftSmsMessage(threadId);
        delOldDraftMms(conv, mmsUri);
        DraftCache.getInstance().setSavingDraft(false);
        if (MessageUtils.isMultiSimEnabled() && mmsUri != null) {
            values = new ContentValues(2);
            values.put("sub_id", Integer.valueOf(subscription));
            values.put("network_type", Integer.valueOf(MessageUtils.getNetworkType(subscription)));
            SqliteWrapper.update(this.mContext, this.mContentResolver, mmsUri, values, null, null);
        }
        MmsRadarInfoManager mMmsRadarInfoManager = MmsRadarInfoManager.getInstance();
        Handler mSendSmsHandler = mMmsRadarInfoManager.getHandler();
        if (mmsUri != null) {
            mMmsRadarInfoManager.writeLogMsg(1331, "mms saved");
            mSendSmsHandler.sendMessage(mSendSmsHandler.obtainMessage(101));
        } else {
            mMmsRadarInfoManager.writeLogMsg(1331, "mms saved fail");
        }
        int msgSize = 0;
        if (slideshow != null) {
            try {
                msgSize = slideshow.getCurrentMessageSize();
            } catch (Throwable e2) {
                MLog.e("WorkingMessage", "Failed to send message: threadId=" + threadId, e2);
                MmsRadarInfoManager.getInstance().writeLogMsg(1331, e2.getMessage());
            }
        } else {
            MLog.e("WorkingMessage", "sendMmsWorker:: msgSize is 0 because slideshow is null!!");
        }
        MessageSender sender = new MmsMessageSender(this.mContext, mmsUri, (long) msgSize, subscription);
        if (MLog.isHwLoggable()) {
            String applicationName = (String) this.mContext.getPackageManager().getApplicationLabel(this.mContext.getApplicationInfo());
            MLog.i(" ctaifs <" + applicationName + ">[" + applicationName + "][" + this.mContext.getApplicationInfo().packageName + "]", "[WorkingMessage.sendMmsWorker]" + this.mContext.getString(R.string.send_mms) + TextUtils.join(";", PhoneNumberFormatter.transSafeNumbers(conv.getRecipients().getNumbers(true))));
        }
        if (!sender.sendMessage(threadId)) {
            SqliteWrapper.delete(this.mContext, this.mContentResolver, mmsUri, null, null);
        }
        Recycler.getMmsRecycler().deleteOldMessagesByThreadId(this.mContext, threadId);
        this.mStatusListener.onMessageSent();
    }

    private static Uri readDraftMmsMessage(Context context, Conversation conv, StringBuilder sb) {
        if (MLog.isLoggable("Mms_app", 2)) {
            LogTag.debug("workingmessage:: readDraftMmsMessage conv: " + conv.getThreadId(), new Object[0]);
        }
        ContentResolver cr = context.getContentResolver();
        long threadId = conv.getThreadId();
        if (conv.getHwCust() != null && conv.getHwCust().isRcsSwitchOn()) {
            threadId = checkRcsThreadIdFromMms(conv.getHwCust().getSmsThreadId(threadId, conv, context), conv);
        }
        String selection = "thread_id = " + threadId;
        Cursor cursor = SqliteWrapper.query(context, cr, Draft.CONTENT_URI, MMS_DRAFT_PROJECTION, selection, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                Uri uri = ContentUris.withAppendedId(Draft.CONTENT_URI, cursor.getLong(0));
                String subject = MessageUtils.extractEncStrFromCursor(cursor, 1, 2);
                if (subject != null && TextUtils.getTrimmedLength(subject) > 0) {
                    sb.append(subject);
                }
                if (MLog.isLoggable("Mms_app", 2)) {
                    LogTag.debug("readDraftMmsMessage uri: ", uri);
                }
                cursor.close();
                return uri;
            }
            cursor.close();
            return null;
        } catch (Throwable th) {
            cursor.close();
        }
    }

    private static SendReq makeSendReq(Conversation conv, CharSequence subject) {
        String[] dests = conv.getRecipients().getNumbers(true);
        SendReq req = new SendReq();
        EncodedStringValue[] encodedNumbers = EncodedStringValue.encodeStrings(dests);
        if (encodedNumbers != null) {
            req.setTo(encodedNumbers);
        }
        if (subject != null && TextUtils.getTrimmedLength(subject) > 0) {
            req.setSubject(new EncodedStringValue(subject.toString()));
        }
        req.setDate(System.currentTimeMillis() / 1000);
        return req;
    }

    private static Uri createDraftMmsMessage(PduPersister persister, SendReq sendReq, SlideshowModel slideshow, Uri preUri, Context context, HashMap<Uri, InputStream> preOpenedFiles, int subid) {
        if (slideshow == null) {
            return null;
        }
        try {
            Uri uri;
            PduBody pb = slideshow.toPduBody();
            sendReq.setBody(pb);
            if (preUri == null) {
                uri = Draft.CONTENT_URI;
            } else {
                uri = preUri;
            }
            Uri res = persister.persist(sendReq, uri, true, PreferenceUtils.getIsGroupMmsEnabled(context), preOpenedFiles);
            ContentValues values = new ContentValues(1);
            values.put("sub_id", Integer.valueOf(subid));
            SqliteWrapper.update(context, res, values, null, null);
            slideshow.sync(pb);
            return res;
        } catch (MmsException e) {
            return null;
        } catch (IllegalStateException e2) {
            MLog.e("WorkingMessage", "failed to create draft mms " + e2);
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }

    private void asyncUpdateDraftMmsMessage(Conversation conv, boolean isStopping, Uri oldUri) {
        if (this.mSlideshow == null) {
            MLog.e("WorkingMessage", "asyncUpdateDraftMmsMessage:: slideShow is null, return");
            return;
        }
        final HashMap<Uri, InputStream> preOpenedFiles = this.mSlideshow.openPartFiles(this.mContentResolver);
        final Conversation conversation = conv;
        final boolean z = isStopping;
        final Uri uri = oldUri;
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                MLog.i("WorkingMessage", "asyncUpdateDraftMmsMessage conversation=%s MessageUri=%s thread=%s", Long.valueOf(conversation.getThreadId()), WorkingMessage.this.mMessageUri, Long.valueOf(Thread.currentThread().getId()));
                Cursor cursor;
                try {
                    DraftCache.getInstance().setSavingDraft(true);
                    PduPersister persister = PduPersister.getPduPersister(WorkingMessage.this.mContext);
                    SendReq sendReq = WorkingMessage.makeSendReq(conversation, WorkingMessage.this.mSubject);
                    int subid = WorkingMessage.this.mNewMessageDraftSubid;
                    if (subid == -1) {
                        subid = conversation.getSubId();
                    }
                    if (MmsConfig.isSupportDraftWithoutRecipient() && z && conversation.getRecipients().isEmpty() && conversation.getThreadId() == 0) {
                        SqliteWrapper.delete(WorkingMessage.this.mContext, WorkingMessage.this.mContentResolver, WorkingMessage.this.mMessageUri, null, null);
                        WorkingMessage.this.asyncDeleteDraftSmsMessage(conversation);
                        WorkingMessage.this.mMessageUri = null;
                    }
                    if (WorkingMessage.this.mMessageUri == null) {
                        WorkingMessage.debug("asyncUpdateDraftMmsMessage: createDraftMmsMessage...");
                        WorkingMessage.this.mMessageUri = WorkingMessage.createDraftMmsMessage(persister, sendReq, WorkingMessage.this.mSlideshow, null, WorkingMessage.this.mContext, preOpenedFiles, subid);
                    } else {
                        Conversation oldConv = Conversation.get(WorkingMessage.this.mContext, uri, z);
                        MLog.d("WorkingMessage", "asyncUpdateDraftMmsMessage conversation=%s ", Long.valueOf(oldConv.getThreadId()));
                        if (oldConv.hasDraft()) {
                            if (!oldConv.equals(conversation)) {
                                if (oldConv.getMessageCount() <= 0 && oldConv.getThreadId() != conversation.getThreadId()) {
                                    persister.updateHeaders(WorkingMessage.this.mMessageUri, sendReq);
                                    String where = "thread_id = " + oldConv.getThreadId();
                                    WorkingMessage.debug("asyncUpdateDraftMmsMessage: delete old conversation's mms draft for " + where);
                                    SqliteWrapper.delete(WorkingMessage.this.mContext, WorkingMessage.this.mContentResolver, Draft.CONTENT_URI, where, null);
                                    WorkingMessage.debug("asyncUpdateDraftMmsMessage: delete old conversation's sms draft" + where);
                                    WorkingMessage.this.asyncDelete(ContentUris.withAppendedId(Conversations.CONTENT_URI, oldConv.getThreadId()), "type=3", null);
                                    WorkingMessage.this.clearConversation(oldConv, true);
                                    boolean needCreate = false;
                                    cursor = null;
                                    cursor = SqliteWrapper.query(WorkingMessage.this.mContext, WorkingMessage.this.mMessageUri, new String[]{"thread_id"}, null, null, null);
                                    if (cursor == null || cursor.getCount() == 0) {
                                        needCreate = true;
                                    }
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                    if (needCreate) {
                                        WorkingMessage.this.mMessageUri = WorkingMessage.createDraftMmsMessage(persister, sendReq, WorkingMessage.this.mSlideshow, null, WorkingMessage.this.mContext, preOpenedFiles, subid);
                                    }
                                }
                            }
                        }
                        WorkingMessage.debug("asyncUpdateDraftMmsMessage: updateDraftMmsMessage uri=" + WorkingMessage.this.mMessageUri);
                        long oldThread = WorkingMessage.this.getThreadIdByUri(WorkingMessage.this.mContext);
                        WorkingMessage.updateDraftMmsMessage(WorkingMessage.this.mMessageUri, persister, WorkingMessage.this.mSlideshow, sendReq, preOpenedFiles, WorkingMessage.this.mContext, subid);
                        long newThread = WorkingMessage.this.getThreadIdByUri(WorkingMessage.this.mContext);
                        if (oldThread > 0 && newThread > 0 && oldThread != newThread) {
                            WorkingMessage.debug("asyncUpdateDraftMmsMessage delete old draft thread: " + oldThread);
                            SqliteWrapper.delete(WorkingMessage.this.mContext, WorkingMessage.this.mContentResolver, Threads.OBSOLETE_THREADS_URI, null, null);
                        }
                    }
                } catch (Throwable e) {
                    MLog.e("WorkingMessage", "Exception in asyncUpdateDraftMmsMessage query THREAD_ID", e);
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    DraftCache.getInstance().setSavingDraft(false);
                }
                WorkingMessage.this.ensureThreadIdIfNeeded(conversation, z);
                conversation.setDraftState(true);
                WorkingMessage.debug("asyncUpdateDraftMmsMessage conv tid: " + conversation.getThreadId() + " uri: " + WorkingMessage.this.mMessageUri);
                WorkingMessage.this.asyncDeleteDraftSmsMessage(conversation);
                WorkingMessage.this.delOldDraftMms(conversation, WorkingMessage.this.mMessageUri);
                DraftCache.getInstance().setSavingDraft(false);
            }
        });
    }

    private void delOldDraftMms(Conversation conv, Uri newDraftUri) {
        if (newDraftUri != null) {
            String thread_id = String.valueOf(conv.getThreadId());
            String id = newDraftUri.getLastPathSegment();
            SqliteWrapper.delete(this.mContext, this.mContentResolver, Draft.CONTENT_URI, "thread_id=? and _id!=?", new String[]{thread_id, id});
        }
    }

    private static void updateDraftMmsMessage(Uri uri, PduPersister persister, SlideshowModel slideshow, SendReq sendReq, HashMap<Uri, InputStream> preOpenedFiles, Context context, int subid) {
        if (MLog.isLoggable("Mms_app", 2)) {
            LogTag.debug("updateDraftMmsMessage uri=%s", uri);
        }
        if (uri == null) {
            MLog.e("WorkingMessage", "updateDraftMmsMessage null uri");
        } else if (slideshow == null) {
            MLog.e("WorkingMessage", "updateDraftMmsMessage:: slideshow is null");
        } else {
            if (SystemProperties.getBoolean("ro.config.show_mms_storage", false)) {
                sendReq.setMessageSize((long) slideshow.getCurrentMessageSize());
            }
            PduBody pduBody = null;
            try {
                persister.updateHeaders(uri, sendReq);
                pduBody = slideshow.toPduBody();
                persister.updateParts(uri, pduBody, preOpenedFiles);
            } catch (MmsException e) {
                MLog.e("WorkingMessage", "updateDraftMmsMessage:" + uri + " cannot update message " + e);
            } catch (NullPointerException e2) {
                MLog.e("WorkingMessage", "updateDraftMmsMessage:" + uri + " cannot update message: " + e2);
            } catch (Exception e3) {
                MLog.e("WorkingMessage", "updateDraftMmsMessage:" + uri + " cannot update message: " + e3);
            }
            if (pduBody != null) {
                slideshow.sync(pduBody);
            }
            ContentValues values = new ContentValues(1);
            values.put("sub_id", Integer.valueOf(subid));
            SqliteWrapper.update(context, uri, values, null, null);
        }
    }

    private String readDraftSmsMessage(Conversation conv) {
        long thread_id = conv.getThreadId();
        if (conv.getHwCust() != null) {
            thread_id = checkRcsThreadIdFromMms(conv.getHwCust().getSmsThreadId(thread_id, conv, this.mContext), conv);
        }
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.d("WorkingMessage", "readDraftSmsMessage conv tid: " + conv.getThreadId());
        }
        if (MmsConfig.isSupportDraftWithoutRecipient()) {
            if (thread_id < 0 || !(thread_id == 0 || conv.hasDraft())) {
                return "";
            }
        } else if (thread_id <= 0 || !conv.hasDraft()) {
            return "";
        }
        String body = "";
        Cursor c = SqliteWrapper.query(this.mContext, this.mContentResolver, ContentUris.withAppendedId(Conversations.CONTENT_URI, thread_id), SMS_BODY_PROJECTION, "type=3", null, null);
        boolean haveDraft = false;
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    body = c.getString(0);
                    haveDraft = true;
                }
                c.close();
            } catch (Throwable th) {
                c.close();
            }
        }
        if (haveDraft && conv.getMessageCount() == 0) {
            if (conv.getHwCust() == null || !conv.getHwCust().isRcsSwitchOn()) {
                this.dirtyThreadId = conv.getThreadId();
            } else {
                this.dirtyThreadId = conv.getHwCust().getSmsThreadId(conv, this.mContext);
            }
            if (this.dirtyThreadId == 0) {
                asyncDeleteDraftSmsMessage(conv);
                clearConversation(conv, true);
            }
        }
        if (MLog.isLoggable("Mms_app", 2)) {
            String str = "readDraftSmsMessage haveDraft: ";
            Object[] objArr = new Object[1];
            objArr[0] = Boolean.valueOf(!TextUtils.isEmpty(body));
            LogTag.debug(str, objArr);
        }
        return body;
    }

    public static long checkRcsThreadIdFromMms(long threadId, Conversation conv) {
        if (threadId > 0) {
            return threadId;
        }
        if (conv == null) {
            return 0;
        }
        ContactList contacts = conv.getRecipients();
        if (contacts == null || contacts.size() == 0) {
            return 0;
        }
        return -1;
    }

    public void clearConversation(Conversation conv, boolean resetThreadId) {
        if (resetThreadId && conv.getMessageCount() == 0) {
            conv.clearThreadId();
        }
        conv.setDraftState(false);
    }

    private void asyncUpdateDraftSmsMessage(Conversation conv, String contents, boolean isStopping, Uri oldUri) {
        final Conversation conversation = conv;
        final Uri uri = oldUri;
        final boolean z = isStopping;
        final String str = contents;
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                try {
                    WorkingMessage.debug("asyncUpdateDraftSmsMessage for tid " + conversation.getThreadId() + " old=" + uri);
                    if (conversation.hasTempDraft() && !(MmsConfig.isSupportDraftWithoutRecipient() && conversation.getThreadId() == 0)) {
                        WorkingMessage.this.deleteDraftSmsMessage(conversation.getThreadId());
                    }
                    DraftCache.getInstance().setSavingDraft(true);
                    if (!conversation.getRecipients().isEmpty() || (MmsConfig.isSupportDraftWithoutRecipient() && conversation.getThreadId() == 0)) {
                        WorkingMessage.this.ensureThreadIdIfNeeded(conversation, z);
                        conversation.setDraftState(true);
                        Conversation oldConv = Conversation.get(WorkingMessage.this.mContext, uri, z);
                        if (oldConv.hasDraft() && !oldConv.equals(conversation)) {
                            if (oldConv.getMessageCount() <= 0 && oldConv.getThreadId() != conversation.getThreadId()) {
                                WorkingMessage.info("asyncUpdateDraftSmsMessage  don't save as has no recipients");
                                if (!(MmsConfig.isSupportDraftWithoutRecipient() && oldConv.getThreadId() == 0)) {
                                    WorkingMessage.this.asyncDelete(ContentUris.withAppendedId(Conversations.CONTENT_URI, oldConv.getThreadId()), "type=3", null);
                                    WorkingMessage.this.clearConversation(oldConv, true);
                                }
                            }
                        }
                        WorkingMessage.this.updateDraftSmsMessage(conversation, str);
                        DraftCache.getInstance().setSavingDraft(false);
                        return;
                    }
                    WorkingMessage.info("asyncUpdateDraftSmsMessage :  don't save as has no recipients");
                } finally {
                    DraftCache.getInstance().setSavingDraft(false);
                }
            }
        });
    }

    private void updateDraftSmsMessage(Conversation conv, String contents) {
        long threadId = conv.getThreadId();
        if (MLog.isLoggable("Mms_app", 2)) {
            LogTag.debug("updateDraftSmsMessage tid=%d, contents=***", Long.valueOf(threadId));
        }
        if (!MmsConfig.isSupportDraftWithoutRecipient() ? threadId <= 0 : threadId < 0) {
            ContentValues values = new ContentValues(4);
            values.put("thread_id", Long.valueOf(threadId));
            values.put("body", contents);
            values.put(NumberInfo.TYPE_KEY, Integer.valueOf(3));
            int subid = this.mNewMessageDraftSubid;
            if (subid == -1) {
                subid = conv.getSubId();
            }
            values.put("sub_id", Integer.valueOf(subid));
            this.mLastDraftUri = SqliteWrapper.insert(this.mContext, this.mContentResolver, Sms.CONTENT_URI, values);
            asyncDeleteDraftMmsMessage(conv);
            this.mMessageUri = null;
            this.mHasSmsDraft = true;
        }
    }

    private void asyncDelete(Uri uri, String selection, String[] selectionArgs) {
        asyncDelete(uri, selection, selectionArgs, 0);
    }

    private void asyncDelete(final Uri uri, final String selection, final String[] selectionArgs, long delay) {
        debug("asyncDelete %s where %s delay=%s", uri, selection, Long.valueOf(delay));
        HwBackgroundLoader.getInst().postTaskDelayed(new Runnable() {
            public void run() {
                SqliteWrapper.delete(WorkingMessage.this.mContext, WorkingMessage.this.mContentResolver, uri, selection, selectionArgs);
            }
        }, delay);
    }

    public void asyncDeleteDraftSmsMessage(Conversation conv) {
        long threadId;
        this.mHasSmsDraft = false;
        debug("asyncDeleteDraftSmsMessage for conversation " + conv.getThreadId());
        if (conv.getHwCust() == null || !conv.getHwCust().isRcsSwitchOn()) {
            threadId = conv.getThreadId();
        } else {
            threadId = conv.getHwCust().getSmsThreadId(conv, this.mContext);
        }
        if (MmsConfig.isSupportDraftWithoutRecipient()) {
            if (threadId < 0) {
                return;
            }
        } else if (threadId <= 0) {
            return;
        }
        asyncDelete(ContentUris.withAppendedId(Conversations.CONTENT_URI, threadId), "type=3", null);
    }

    private void deleteDraftSmsMessage(long threadId) {
        debug("deleteDraftSmsMessage for tid " + threadId);
        if (threadId > 0) {
            Uri uri = ContentUris.withAppendedId(Conversations.CONTENT_URI, threadId);
            if (hasDraftMessageToDelete(uri, "type=3")) {
                SqliteWrapper.delete(this.mContext, this.mContentResolver, uri, "type=3", null);
            }
        }
    }

    private void deleteDraftMmsMessage(long threadId) {
        debug("deleteDraftMmsMessage for tid " + threadId);
        String where = "thread_id" + (threadId > 0 ? " = " + threadId : " IS NULL");
        if (hasDraftMessageToDelete(Draft.CONTENT_URI, where)) {
            SqliteWrapper.delete(this.mContext, this.mContentResolver, Draft.CONTENT_URI, where, null);
        }
    }

    private void asyncDeleteDraftMmsMessage(Conversation conv) {
        asyncDeleteDraftMmsMessage(conv, -1);
    }

    private void asyncDeleteDraftMmsMessage(Conversation conv, long msgId) {
        this.mHasMmsDraft = false;
        debug("asyncDeleteDraftMmsMessage for conversation " + conv.getThreadId());
        long threadId = conv.getThreadId();
        String where = "thread_id" + (threadId > 0 ? " = " + threadId : " IS NULL");
        if (-1 != msgId) {
            where = where + " AND _id != " + msgId;
        }
        asyncDelete(Draft.CONTENT_URI, where, null);
    }

    public void setmHasMmsDraft(boolean mHasMmsDraft) {
        this.mHasMmsDraft = mHasMmsDraft;
    }

    private void ensureThreadIdIfNeeded(Conversation conv, boolean isStopping) {
        if (isStopping && conv.getMessageCount() == 0) {
            conv.clearThreadId();
        }
        if (!conv.getRecipients().isEmpty()) {
            if (conv.getHwCust() == null || !conv.getHwCust().isRcsSwitchOn()) {
                conv.ensureThreadId();
            } else {
                conv.getHwCust().ensureDraftThreadId(conv, this.mContext);
            }
        }
    }

    public boolean hasTooManyUnSentMsg() {
        if (this.mSlideshow == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(this.mContext, this.mContentResolver, Outbox.CONTENT_URI, MMS_OUTBOX_PROJECTION, null, null, null);
            if (cursor != null) {
                long maxMessageSize = ((long) MmsConfig.getMaxSizeScaleForPendingMmsAllowed()) * ((long) MmsConfig.getMaxMessageSize());
                long totalPendingSize = 0;
                while (cursor.moveToNext()) {
                    totalPendingSize += cursor.getLong(1);
                }
                boolean z = totalPendingSize + ((long) this.mSlideshow.getCurrentMessageSize()) >= maxMessageSize;
                if (cursor != null) {
                    cursor.close();
                }
                return z;
            }
            if (cursor != null) {
                cursor.close();
            }
            return false;
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

    public void setAttachmentState(boolean hasAttachment, boolean notify) {
        updateState(4, hasAttachment, notify);
    }

    public boolean hasValidText() {
        return hasText() && !hasOnlySignatureText();
    }

    private boolean hasOnlySignatureText() {
        String content = this.mText.toString();
        return SignatureUtil.deleteNewlineSymbol(content).equals(SignatureUtil.getSignature(this.mContext, MmsConfig.getDefaultSignatureText()));
    }

    private boolean hasOnlySignaTextToSave() {
        return requiresMms() ? hasOnlySignatureText() : false;
    }

    private long getThreadIdByUri(Context context) {
        long thread = 0;
        if (this.mMessageUri == null) {
            return 0;
        }
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, context.getContentResolver(), this.mMessageUri, new String[]{"thread_id"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                thread = cursor.getLong(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            MLog.e("WorkingMessage", "getThreadIdByUri sqlite exception " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return thread;
    }

    public Uri getMessageUri() {
        return this.mMessageUri;
    }

    public void resetMmsDraftUri(Uri uri) {
        this.mMessageUri = uri;
    }

    private static void debug(String msg) {
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.d("WorkingMessage", msg);
        }
    }

    public static void debug(String format, Object... args) {
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.d("WorkingMessage", LogTag.logFormat(format, args));
        }
    }

    private static void info(String msg) {
        MLog.i("WorkingMessage", msg);
    }

    public void setNewMessageDraftSubid(int subid) {
        this.mNewMessageDraftSubid = subid;
    }

    public RcsWorkingMessage getRcsWorkingMessage() {
        return this.mRcsWorkingMessage;
    }

    public boolean requiresMmsExceptLength() {
        return (this.mMmsState & -9) != 0;
    }

    public RegionModel getSlideTextRegion() {
        if (this.mSlideshow == null || this.mSlideshow.getLayout() == null) {
            return null;
        }
        return this.mSlideshow.getLayout().getTextRegion();
    }

    public int getSlideCurrentSize() {
        if (this.mSlideshow == null) {
            return 0;
        }
        return this.mSlideshow.getCurrentMessageSize();
    }

    public boolean hasExceedsMmsLimit() {
        return getSlideCurrentSize() > MmsConfig.getMaxMessageSize() + -4096;
    }
}
