package com.huawei.rcs.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.ContactsContract.Contacts;
import android.provider.DocumentsContract;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.ui.popu.util.ViewPartId;
import com.android.common.contacts.DataUsageStatUpdater;
import com.android.mms.MmsApp;
import com.android.mms.data.Conversation;
import com.android.mms.data.RecipientIdCache;
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.TextModel;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.FavoritesActivity;
import com.android.mms.ui.FavoritesFragment;
import com.android.mms.ui.FragmentTag;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.harassmentinterception.service.BlacklistCommonUtils;
import com.huawei.mms.util.DelaySendManager;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.commonInterface.metadata.Capabilities;
import com.huawei.rcs.commonInterface.metadata.PeerInformation;
import com.huawei.rcs.media.RcsMediaFileUtils;
import com.huawei.rcs.media.RcsMediaFileUtils.MediaFileType;
import com.huawei.rcs.telephony.RcseTelephonyExt;
import com.huawei.rcs.telephony.RcseTelephonyExt.RcsAttachments;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.ui.RcsMediaCompressActivity;
import com.huawei.rcs.util.RcsXmlParser;
import com.huawei.rcs.utils.RcsUtility.FileInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RcsTransaction {
    private static final String CONTAINS_STR = ("emulated" + File.separator + "0" + File.separator);
    private static final boolean IS_CMCC_RCS_CUST = "true".equalsIgnoreCase(RcsXmlParser.getValueByNameFromXml("is_cmcc_rcs_cust"));
    public static final String RCS_BASE_MEDIASTORAGE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String RCS_RECORD_VIDEO = (RCS_BASE_MEDIASTORAGE_DIR + File.separator + "RCS" + File.separator + "RecordVideo");
    private static Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.getData().getInt("key")) {
                case 1:
                    MLog.i("RcsTransaction FileTrans: ", "show dialog about maxFileSizeLimit");
                    RcsTransaction.showFileExceedSizePermitedDialog(msg.getData().getString("filename"), msg.getData().getString("maxFileSizeLimit"));
                    return;
                case 2:
                    MLog.i("RcsTransaction FileTrans: ", "show dialog about file size is zero");
                    AlertDialog alertDialog = new Builder(RcsTransaction.mContext).setTitle(RcsTransaction.mContext.getString(R.string.rcs_ft_file_size_error)).setMessage(String.format(RcsTransaction.mContext.getString(R.string.rcs_ft_file_size_zero_error), new Object[]{Integer.valueOf(0)})).setPositiveButton(R.string.I_know , new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            RcsTransaction.finishActivity(RcsTransaction.mContext);
                            dialog.dismiss();
                        }
                    }).create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                    return;
                default:
                    return;
            }
        }
    };
    private static int iVideoRecoridingTimeLimit = 90000;
    static Context mContext;
    private static boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private static int mMsgCapValidityTime = 0;
    private static ProgressDialog pDialog;
    private static HashMap<String, Long> sCapReqTimeMap = new HashMap();

    static class CancelSendMixMessageRunnable implements Runnable {
        private int chatType;
        private ComposeMessageFragment fragment;
        private ArrayList<Uri> vCardUris = new ArrayList();

        public CancelSendMixMessageRunnable(ComposeMessageFragment fragment) {
            this.fragment = fragment;
        }

        public void initMessageData(ArrayList<Uri> vCardUris, int chatType) {
            this.vCardUris = vCardUris;
            this.chatType = chatType;
        }

        public void run() {
            MLog.i("RcsTransaction", "CancelSendMixMessageRunnable run.");
            RcsUtility.delveteSendVcf(this.vCardUris, null, this.chatType, this.fragment.getContext(), this.fragment);
            if (this.fragment.getRcsComposeMessage() != null) {
                this.fragment.getRcsComposeMessage().setSentMessage(false);
                this.fragment.getRcsComposeMessage().setSendingMessage(false);
            }
        }
    }

    public static class ImMsgData {
        public Conversation mConv;
        public String mMsgText;
        public long mThreadId;
    }

    public static class LocationData implements Serializable {
        private static final long serialVersionUID = 1;
        public String city;
        public String myAddress;
        public double x;
        public double y;

        public LocationData(double x, double y, String myAddress, String city) {
            this.x = x;
            this.y = y;
            this.myAddress = myAddress;
            this.city = city;
        }
    }

    private static class MyAsyncQueryHandler extends AsyncQueryHandler {
        Bundle mBundle;
        Context mContext;

        public MyAsyncQueryHandler(Context context, Bundle bundle) {
            super(context.getContentResolver());
            this.mContext = context;
            this.mBundle = bundle;
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();
                long threadId;
                String message;
                long threadId2;
                long delayMsgId;
                Bundle data;
                String groupId;
                String path;
                switch (token) {
                    case 1:
                        threadId = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"));
                        message = "";
                        if (this.mBundle != null) {
                            message = this.mBundle.getString("text");
                        }
                        DelaySendManager.getInst().addDelayMsg(RcsTransaction.addGroupChatToDbBeforeSend(message, threadId, this.mContext), "rcs_group_text", false);
                        cursor.close();
                        break;
                    case 2:
                        threadId2 = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"));
                        message = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                        delayMsgId = this.mBundle.getLong("delay_msg_id", 0);
                        data = new Bundle();
                        data.putString("text", message);
                        data.putLong("delay_msg_id", delayMsgId);
                        new MyAsyncQueryHandler(this.mContext, data).startQuery(3, null, RcsGroupChatComposeMessageFragment.sGroupUri, new String[]{"name"}, "thread_id = " + String.valueOf(threadId2), null, null);
                        cursor.close();
                        break;
                    case 3:
                        message = "";
                        delayMsgId = 0;
                        if (this.mBundle != null) {
                            message = this.mBundle.getString("text");
                        }
                        groupId = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        if (this.mBundle != null) {
                            delayMsgId = this.mBundle.getLong("delay_msg_id", 0);
                        }
                        RcsTransaction.sendGroupMessageFinal(groupId, message, delayMsgId);
                        cursor.close();
                        break;
                    case 4:
                        threadId2 = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"));
                        path = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                        delayMsgId = 0;
                        if (this.mBundle != null) {
                            delayMsgId = this.mBundle.getLong("delay_msg_id", 0);
                        }
                        data = new Bundle();
                        data.putLong("delay_msg_id", delayMsgId);
                        data.putString("delay_group_msg_path", path);
                        data.putLong("delay_group_msg_thread_id", threadId2);
                        new MyAsyncQueryHandler(this.mContext, data).startQuery(5, null, RcsGroupChatComposeMessageFragment.sGroupUri, new String[]{"name"}, "thread_id = " + String.valueOf(threadId2), null, null);
                        cursor.close();
                        break;
                    case 5:
                        groupId = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        delayMsgId = 0;
                        path = "";
                        threadId = 0;
                        if (this.mBundle != null) {
                            delayMsgId = this.mBundle.getLong("delay_msg_id", 0);
                            path = this.mBundle.getString("delay_group_msg_path", "");
                            threadId = this.mBundle.getLong("delay_group_msg_thread_id", 0);
                        }
                        FileInfo info = RcsTransaction.getFileInfoByData(this.mContext, path);
                        RcsTransaction.sendGroupFileFinal(groupId, RcseTelephonyExt.createBundleValues(threadId, info.getSendFilePath(), info.getFileDisplayName(), info.getTotalSize(), info.getMimeType()), delayMsgId);
                        cursor.close();
                        break;
                    default:
                        try {
                            cursor.close();
                            break;
                        } catch (IllegalArgumentException e) {
                            MLog.w("RcsTransaction", "MyAsyncQueryHandler db error");
                            if (!cursor.isClosed()) {
                                cursor.close();
                                break;
                            }
                        } catch (Throwable th) {
                            if (!cursor.isClosed()) {
                                cursor.close();
                            }
                        }
                        break;
                }
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
    }

    static class SendMixMessageRunnable implements Runnable {
        private ArrayList<String> addresses = new ArrayList();
        private ArrayList<Uri> audioUris = new ArrayList();
        private ComposeMessageFragment fragment;
        private ArrayList<Uri> imageUris = new ArrayList();
        private ArrayList<HashMap<String, String>> locationDatas = new ArrayList();
        private ArrayList<String> mTexts = new ArrayList();
        private long threadId;
        private ArrayList<Uri> vCalendarUris = new ArrayList();
        private ArrayList<Uri> vCardUris = new ArrayList();
        private ArrayList<Uri> videoUris = new ArrayList();
        private WorkingMessage workingMessage;

        public SendMixMessageRunnable(WorkingMessage workingMessage, ComposeMessageFragment fragment, int maxInputSize) {
            this.workingMessage = workingMessage;
            this.fragment = fragment;
            if (workingMessage != null) {
                this.threadId = workingMessage.getConversation().getThreadId();
            }
        }

        public void initMessageData(ArrayList<String> addresses, ArrayList<String> texts, ArrayList<Uri> imageUris, ArrayList<Uri> videoUris, ArrayList<Uri> audioUris, ArrayList<Uri> vCardUris, ArrayList<Uri> vCalendarUris, ArrayList<HashMap<String, String>> locationDatas) {
            this.addresses = addresses;
            this.mTexts = texts;
            this.imageUris = imageUris;
            this.videoUris = videoUris;
            this.audioUris = audioUris;
            this.vCardUris = vCardUris;
            this.vCalendarUris = vCalendarUris;
            this.locationDatas = locationDatas;
        }

        public void run() {
            if (this.fragment != null) {
                MLog.i("RcsTransaction", "SendMixMessageRunnable run.");
                this.fragment.onPreMessageSent();
                if (this.fragment.getRcsComposeMessage() != null) {
                    this.fragment.getRcsComposeMessage().setSentMessage(true);
                    this.fragment.getRcsComposeMessage().setSendingMessage(true);
                    this.fragment.getRcsComposeMessage().setScrollOnSend(true);
                }
                if (!(this.mTexts == null || this.mTexts.isEmpty())) {
                    for (String text : this.mTexts) {
                        RcsTransaction.preSendImMessage(this.fragment.getContext(), text, (String) this.addresses.get(0));
                    }
                }
                if (!(this.vCalendarUris == null || this.vCalendarUris.isEmpty())) {
                    RcsTransaction.multiSend(this.fragment.getContext(), Long.valueOf(this.threadId), this.vCalendarUris, this.addresses, 0);
                }
                if (!(this.vCardUris == null || this.vCardUris.isEmpty())) {
                    RcsTransaction.multiSend(this.fragment.getContext(), Long.valueOf(this.threadId), this.vCardUris, this.addresses, 0);
                }
                if (!(this.imageUris == null || this.imageUris.isEmpty())) {
                    if (this.fragment.getRichEditor() == null || !this.fragment.getRichEditor().getFullSizeFlag()) {
                        RcsTransaction.multiSendWithUriResized(this.fragment.getContext(), this.threadId, this.imageUris, this.addresses, 0);
                    } else {
                        RcsTransaction.multiSend(this.fragment.getContext(), Long.valueOf(this.threadId), this.imageUris, this.addresses, 0);
                    }
                }
                if (!(this.videoUris == null || this.videoUris.isEmpty())) {
                    if (this.fragment.getRichEditor() == null || !this.fragment.getRichEditor().getFullSizeFlag()) {
                        RcsTransaction.multiSendWithUriResized(this.fragment.getContext(), this.threadId, this.videoUris, this.addresses, 0);
                    } else {
                        ArrayList<Uri> videoUrisTemp = new ArrayList();
                        for (Uri videoUri : this.videoUris) {
                            videoUrisTemp.add(RcsTransaction.copyFileBeforeSendSingle(this.fragment.getContext(), videoUri));
                        }
                        RcsTransaction.multiSend(this.fragment.getContext(), Long.valueOf(this.threadId), videoUrisTemp, this.addresses, 0);
                    }
                }
                if (!(this.audioUris == null || this.audioUris.isEmpty())) {
                    RcsTransaction.multiSend(this.fragment.getContext(), Long.valueOf(this.threadId), this.audioUris, this.addresses, 0);
                }
                if (!(this.locationDatas == null || this.locationDatas.isEmpty())) {
                    boolean isSupportLS = RcsTransaction.getLSCapabilityByNumber((String) this.addresses.get(0));
                    for (HashMap<String, String> locationMap : this.locationDatas) {
                        if (isSupportLS) {
                            String subtitle = (String) locationMap.get("subtitle");
                            RcsTransaction.sendLocationSingleChat(Double.valueOf((String) locationMap.get("latitude")).doubleValue(), Double.valueOf((String) locationMap.get("longitude")).doubleValue(), subtitle, (String) locationMap.get("title"), (String) this.addresses.get(0));
                        } else {
                            RcsTransaction.preSendImMessage(this.fragment.getContext(), MessageUtils.getLocationWebLink(this.fragment.getContext()) + ((String) locationMap.get("latitude")) + "," + ((String) locationMap.get("longitude")), (String) this.addresses.get(0));
                        }
                    }
                }
                this.fragment.onMessageSent();
                RcsTransaction.clearDraft(this.workingMessage, this.fragment.getContext(), true);
                if (this.fragment.getRichEditor() != null) {
                    this.fragment.getRichEditor().setFullSizeFlag(false);
                }
            }
        }
    }

    public static void cancelFtMsgBeforeDeleteConversation(android.content.Context r21, java.util.Collection r22, int r23) {
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
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r15 = new java.util.HashMap;
        r15.<init>();
        r16 = r22.toString();
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "(";
        r2 = r2.append(r3);
        r3 = r16.length();
        r3 = r3 + -1;
        r4 = 1;
        r0 = r16;
        r3 = r0.substring(r4, r3);
        r2 = r2.append(r3);
        r3 = ")";
        r2 = r2.append(r3);
        r20 = r2.toString();
        r9 = 0;
        r8 = 0;
        r2 = 1;
        r0 = r23;
        if (r0 != r2) goto L_0x00ae;
    L_0x0038:
        r2 = "content://rcsim/chat";
        r3 = android.net.Uri.parse(r2);
        r2 = 2;
        r4 = new java.lang.String[r2];
        r2 = "_id";
        r5 = 0;
        r4[r5] = r2;
        r2 = "sdk_sms_id";
        r5 = 1;
        r4[r5] = r2;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r5 = "thread_id IN ";
        r2 = r2.append(r5);
        r0 = r20;
        r2 = r2.append(r0);
        r5 = "AND service_center = 'rcs.file'";
        r2 = r2.append(r5);
        r5 = r2.toString();
        r6 = 0;
        r7 = 0;
        r2 = r21;
        r9 = com.huawei.cspcommon.ex.SqliteWrapper.query(r2, r3, r4, r5, r6, r7);
        r2 = "content://rcsim/file_trans";
        r3 = android.net.Uri.parse(r2);
        r2 = 2;
        r4 = new java.lang.String[r2];
        r2 = "msg_id";
        r5 = 0;
        r4[r5] = r2;
        r2 = "transfer_status";
        r5 = 1;
        r4[r5] = r2;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r5 = "thread_id IN ";
        r2 = r2.append(r5);
        r0 = r20;
        r2 = r2.append(r0);
        r5 = "AND chat_type = 1 ";
        r2 = r2.append(r5);
        r5 = r2.toString();
        r6 = 0;
        r7 = 0;
        r2 = r21;
        r8 = com.huawei.cspcommon.ex.SqliteWrapper.query(r2, r3, r4, r5, r6, r7);
    L_0x00ae:
        r2 = 2;
        r0 = r23;
        if (r0 != r2) goto L_0x0129;
    L_0x00b3:
        r2 = "content://rcsim/rcs_group_message";
        r3 = android.net.Uri.parse(r2);
        r2 = 2;
        r4 = new java.lang.String[r2];
        r2 = "_id";
        r5 = 0;
        r4[r5] = r2;
        r2 = "sdk_rcs_group_message_id";
        r5 = 1;
        r4[r5] = r2;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r5 = "thread_id IN (SELECT thread_id FROM rcs_groups where name IN ";
        r2 = r2.append(r5);
        r0 = r20;
        r2 = r2.append(r0);
        r5 = ") AND file_mode != 0 ";
        r2 = r2.append(r5);
        r5 = r2.toString();
        r6 = 0;
        r7 = 0;
        r2 = r21;
        r9 = com.huawei.cspcommon.ex.SqliteWrapper.query(r2, r3, r4, r5, r6, r7);
        r2 = "content://rcsim/file_trans";
        r3 = android.net.Uri.parse(r2);
        r2 = 2;
        r4 = new java.lang.String[r2];
        r2 = "msg_id";
        r5 = 0;
        r4[r5] = r2;
        r2 = "transfer_status";
        r5 = 1;
        r4[r5] = r2;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r5 = "thread_id IN (SELECT thread_id FROM rcs_groups where name IN ";
        r2 = r2.append(r5);
        r0 = r20;
        r2 = r2.append(r0);
        r5 = ") AND chat_type = 2 ";
        r2 = r2.append(r5);
        r5 = r2.toString();
        r6 = 0;
        r7 = 0;
        r2 = r21;
        r8 = com.huawei.cspcommon.ex.SqliteWrapper.query(r2, r3, r4, r5, r6, r7);
    L_0x0129:
        if (r9 == 0) goto L_0x0193;
    L_0x012b:
        r2 = r9.moveToFirst();	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        if (r2 == 0) goto L_0x0193;	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
    L_0x0131:
        r2 = 0;	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r13 = java.lang.Long.valueOf(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r2 = 0;	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r18 = java.lang.Long.valueOf(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
    L_0x013d:
        r2 = 1;	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r0 = r23;	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        if (r0 != r2) goto L_0x0165;	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
    L_0x0142:
        r2 = "_id";	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r2 = r9.getColumnIndexOrThrow(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r2 = r9.getLong(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r13 = java.lang.Long.valueOf(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r2 = "sdk_sms_id";	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r2 = r9.getColumnIndexOrThrow(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r2 = r9.getLong(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r18 = java.lang.Long.valueOf(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r0 = r18;	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r15.put(r13, r0);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
    L_0x0165:
        r2 = 2;	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r0 = r23;	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        if (r0 != r2) goto L_0x018d;	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
    L_0x016a:
        r2 = "_id";	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r2 = r9.getColumnIndexOrThrow(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r2 = r9.getLong(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r13 = java.lang.Long.valueOf(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r2 = "sdk_rcs_group_message_id";	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r2 = r9.getColumnIndexOrThrow(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r2 = r9.getLong(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r18 = java.lang.Long.valueOf(r2);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r0 = r18;	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r15.put(r13, r0);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
    L_0x018d:
        r2 = r9.moveToNext();	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        if (r2 != 0) goto L_0x013d;
    L_0x0193:
        if (r9 == 0) goto L_0x0198;
    L_0x0195:
        r9.close();
    L_0x0198:
        if (r8 == 0) goto L_0x0271;
    L_0x019a:
        r2 = r8.moveToFirst();	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        if (r2 == 0) goto L_0x0271;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
    L_0x01a0:
        r2 = 0;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r14 = java.lang.Long.valueOf(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = 0;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r19 = java.lang.Long.valueOf(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
    L_0x01ac:
        r2 = 1;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r0 = r23;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        if (r0 != r2) goto L_0x01cf;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
    L_0x01b1:
        r2 = "msg_id";	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = r8.getColumnIndexOrThrow(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = r8.getLong(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r14 = java.lang.Long.valueOf(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = "transfer_status";	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = r8.getColumnIndexOrThrow(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = r8.getLong(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r19 = java.lang.Long.valueOf(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
    L_0x01cf:
        r2 = 2;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r0 = r23;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        if (r0 != r2) goto L_0x01f2;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
    L_0x01d4:
        r2 = "msg_id";	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = r8.getColumnIndexOrThrow(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = r8.getLong(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r14 = java.lang.Long.valueOf(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = "transfer_status";	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = r8.getColumnIndexOrThrow(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = r8.getLong(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r19 = java.lang.Long.valueOf(r2);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
    L_0x01f2:
        r2 = r19.longValue();	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r4 = 1017; // 0x3f9 float:1.425E-42 double:5.025E-321;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        if (r2 != 0) goto L_0x0229;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
    L_0x01fc:
        r17 = r15.get(r14);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r17 = (java.lang.Long) r17;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = r17.longValue();	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = -r2;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r0 = r23;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r4 = (long) r0;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        rejectFile(r2, r4);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = "RcsTransaction";	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r3 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r3.<init>();	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r4 = "ConversationList 1to1Chat RejectFT before delete, sdkId=";	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r3 = r3.append(r4);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r0 = r17;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r3 = r3.append(r0);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r3 = r3.toString();	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        com.huawei.cspcommon.MLog.d(r2, r3);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
    L_0x0229:
        r2 = r19.longValue();	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r4 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        if (r2 == 0) goto L_0x023d;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
    L_0x0233:
        r2 = r19.longValue();	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r4 = 1007; // 0x3ef float:1.411E-42 double:4.975E-321;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        if (r2 != 0) goto L_0x026b;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
    L_0x023d:
        r17 = r15.get(r14);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r17 = (java.lang.Long) r17;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = r17.longValue();	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = -r2;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r0 = r23;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r4 = (long) r0;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r6 = 1;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        cancelFT(r2, r6, r4);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r2 = "RcsTransaction";	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r3 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r3.<init>();	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r4 = "ConversationList 1to1Chat CancelFT before delete,sdkId=";	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r3 = r3.append(r4);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r0 = r17;	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r3 = r3.append(r0);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r3 = r3.toString();	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        com.huawei.cspcommon.MLog.d(r2, r3);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
    L_0x026b:
        r2 = r8.moveToNext();	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        if (r2 != 0) goto L_0x01ac;
    L_0x0271:
        if (r8 == 0) goto L_0x0276;
    L_0x0273:
        r8.close();
    L_0x0276:
        return;
    L_0x0277:
        r12 = move-exception;
        r2 = "RcsTransaction";	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r3 = "cursor unknowable error";	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        com.huawei.cspcommon.MLog.e(r2, r3);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        if (r9 == 0) goto L_0x0198;
    L_0x0283:
        r9.close();
        goto L_0x0198;
    L_0x0288:
        r10 = move-exception;
        r2 = "RcsTransaction";	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        r3 = "cursor unknowable error";	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        com.huawei.cspcommon.MLog.e(r2, r3);	 Catch:{ SQLiteException -> 0x0288, RuntimeException -> 0x0277, all -> 0x0299 }
        if (r9 == 0) goto L_0x0198;
    L_0x0294:
        r9.close();
        goto L_0x0198;
    L_0x0299:
        r2 = move-exception;
        if (r9 == 0) goto L_0x029f;
    L_0x029c:
        r9.close();
    L_0x029f:
        throw r2;
    L_0x02a0:
        r11 = move-exception;
        r2 = "RcsTransaction";	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r3 = "cursor unknowable error";	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        com.huawei.cspcommon.MLog.e(r2, r3);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        if (r8 == 0) goto L_0x0276;
    L_0x02ac:
        r8.close();
        goto L_0x0276;
    L_0x02b0:
        r10 = move-exception;
        r2 = "RcsTransaction";	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        r3 = "cursor unknowable error";	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        com.huawei.cspcommon.MLog.e(r2, r3);	 Catch:{ SQLiteException -> 0x02b0, Exception -> 0x02a0, all -> 0x02c0 }
        if (r8 == 0) goto L_0x0276;
    L_0x02bc:
        r8.close();
        goto L_0x0276;
    L_0x02c0:
        r2 = move-exception;
        if (r8 == 0) goto L_0x02c6;
    L_0x02c3:
        r8.close();
    L_0x02c6:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.rcs.utils.RcsTransaction.cancelFtMsgBeforeDeleteConversation(android.content.Context, java.util.Collection, int):void");
    }

    private static java.util.Map<java.lang.String, java.lang.String> queryFileInfo(android.content.Context r20, android.net.Uri r21) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0170 in list []
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
        r13 = new java.util.HashMap;
        r13.<init>();
        r12 = 0;
        r17 = 0;
        r9 = 0;
        r18 = 0;
        r14 = 0;
        r15 = android.webkit.MimeTypeMap.getSingleton();
        r2 = r21.toString();
        r11 = android.webkit.MimeTypeMap.getFileExtensionFromUrl(r2);
        r14 = r15.getMimeTypeFromExtension(r11);
        if (r14 == 0) goto L_0x0063;
    L_0x001e:
        r2 = com.google.android.mms.ContentType.isImageType(r14);
        if (r2 == 0) goto L_0x0063;
    L_0x0024:
        r2 = "RcsTransaction FileTrans: ";
        r3 = "queryFileInfo  isFileProviderImageType ";
        com.huawei.cspcommon.MLog.i(r2, r3);
        r2 = 1;
        r0 = r20;
        r1 = r21;
        r16 = com.android.mms.util.ShareUtils.fileProvideUriCopy(r0, r1, r2);
        if (r16 == 0) goto L_0x0044;
    L_0x0038:
        r17 = r16.getAbsolutePath();
        r9 = r16.getName();
        r18 = r16.length();
    L_0x0044:
        r2 = "path";
        r0 = r17;
        r13.put(r2, r0);
        r2 = "displayName";
        r13.put(r2, r9);
        r2 = "totalSize";
        r3 = java.lang.String.valueOf(r18);
        r13.put(r2, r3);
        r2 = "mimeType";
        r13.put(r2, r14);
    L_0x0062:
        return r13;
    L_0x0063:
        r2 = 4;
        r4 = new java.lang.String[r2];	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = "_data";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = 0;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r4[r3] = r2;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = "_display_name";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = 1;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r4[r3] = r2;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = "_size";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = 2;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r4[r3] = r2;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = "mime_type";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = 3;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r4[r3] = r2;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r6 = 0;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r7 = 0;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = r20;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = r21;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r12 = com.huawei.cspcommon.ex.SqliteWrapper.query(r2, r3, r4, r5, r6, r7);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        if (r12 != 0) goto L_0x0091;
    L_0x008b:
        if (r12 == 0) goto L_0x0090;
    L_0x008d:
        r12.close();
    L_0x0090:
        return r13;
    L_0x0091:
        r2 = "_data";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r8 = r12.getColumnIndex(r2);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = -1;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        if (r8 == r2) goto L_0x012c;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
    L_0x009b:
        r2 = r12.moveToFirst();	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        if (r2 == 0) goto L_0x012c;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
    L_0x00a1:
        r2 = "_data";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = r12.getColumnIndexOrThrow(r2);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r17 = r12.getString(r2);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = "_display_name";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = r12.getColumnIndexOrThrow(r2);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r9 = r12.getString(r2);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = "_size";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = r12.getColumnIndexOrThrow(r2);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r18 = r12.getLong(r2);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = "mime_type";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = r12.getColumnIndexOrThrow(r2);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r14 = r12.getString(r2);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
    L_0x00cd:
        r2 = "RcsTransaction FileTrans: ";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3.<init>();	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r4 = "path = ";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = r3.append(r4);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r0 = r17;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = r3.append(r0);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = r3.toString();	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        com.huawei.cspcommon.MLog.i(r2, r3);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = "RcsTransaction FileTrans: ";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3.<init>();	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r4 = "Uri =  Safe String :  ";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = r3.append(r4);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r4 = r21.toSafeString();	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = r3.append(r4);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = r3.toString();	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        com.huawei.cspcommon.MLog.i(r2, r3);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = "path";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r0 = r17;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r13.put(r2, r0);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = "displayName";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r13.put(r2, r9);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = "totalSize";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = java.lang.String.valueOf(r18);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r13.put(r2, r3);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = "mimeType";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r13.put(r2, r14);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        if (r12 == 0) goto L_0x0062;
    L_0x0127:
        r12.close();
        goto L_0x0062;
    L_0x012c:
        r2 = com.google.android.mms.ContentType.isVideoType(r14);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        if (r2 != 0) goto L_0x0138;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
    L_0x0132:
        r2 = com.google.android.mms.ContentType.isAudioType(r14);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        if (r2 == 0) goto L_0x0151;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
    L_0x0138:
        r2 = 1;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r0 = r20;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r1 = r21;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r16 = com.android.mms.util.ShareUtils.fileProvideUriCopy(r0, r1, r2);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        if (r16 == 0) goto L_0x00cd;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
    L_0x0143:
        r17 = r16.getAbsolutePath();	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r9 = r16.getName();	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r18 = r16.length();	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        goto L_0x00cd;	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
    L_0x0151:
        r2 = "RcsTransaction FileTrans: ";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = "queryFileInfo error, uri error.";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        com.huawei.cspcommon.MLog.w(r2, r3);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        if (r12 == 0) goto L_0x015f;
    L_0x015c:
        r12.close();
    L_0x015f:
        return r13;
    L_0x0160:
        r10 = move-exception;
        r2 = "RcsTransaction FileTrans: ";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r3 = "queryFileInfo failed, not Image File.";	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        com.huawei.cspcommon.MLog.w(r2, r3);	 Catch:{ RuntimeException -> 0x0160, all -> 0x0171 }
        r2 = 0;
        if (r12 == 0) goto L_0x0170;
    L_0x016d:
        r12.close();
    L_0x0170:
        return r2;
    L_0x0171:
        r2 = move-exception;
        if (r12 == 0) goto L_0x0177;
    L_0x0174:
        r12.close();
    L_0x0177:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.rcs.utils.RcsTransaction.queryFileInfo(android.content.Context, android.net.Uri):java.util.Map<java.lang.String, java.lang.String>");
    }

    public static void sendImReadReport(final String address) {
        new Thread(new Runnable() {
            public void run() {
                if (!BlacklistCommonUtils.isNumberBlocked(address)) {
                    IfMsgplus mMsgplusService = RcsProfile.getRcsService();
                    if (mMsgplusService != null) {
                        try {
                            mMsgplusService.sendImReadReport(address);
                        } catch (RemoteException e) {
                            MLog.e("RcsTransaction", "remote error");
                        }
                    }
                }
            }
        }).start();
    }

    public static void sendImChatMessages(Context aContext, ImMsgData aRcseData) {
        if (aRcseData.mConv != null) {
            String[] RcseNumber = aRcseData.mConv.getRecipients().getNumbers();
            int size = RcseNumber.length;
            long threadId = aRcseData.mThreadId;
            if (size > 0 && size <= 1) {
                if (threadId <= 0) {
                    aRcseData.mConv.ensureThreadId();
                }
                preSendImMessage(aContext, aRcseData.mMsgText, RcseNumber[0]);
            }
        }
    }

    private static void sendImMsgDelay(Context context, long msgId) {
        String textMsg = "";
        String address = "";
        long sdkid = 0;
        Context context2 = context;
        Cursor c = SqliteWrapper.query(context2, Uri.parse("content://rcsim/chat"), new String[]{"address", "body", "sdk_sms_id"}, "_id = ?", new String[]{String.valueOf(msgId)}, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    address = c.getString(c.getColumnIndexOrThrow("address"));
                    textMsg = c.getString(c.getColumnIndexOrThrow("body"));
                    sdkid = c.getLong(c.getColumnIndexOrThrow("sdk_sms_id"));
                }
            } catch (RuntimeException e) {
                MLog.e("RcsTransaction", "cursor unknowable error");
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }
        if (c != null) {
            c.close();
        }
        if (sdkid == 0) {
            sendImMessageFinal(context, textMsg, address, msgId);
        } else {
            resendImMessageFinal(msgId, address);
        }
    }

    public static void sendGroupChatMsgDelay(Context context, long msgId) {
        Bundle data = new Bundle();
        data.putLong("delay_msg_id", msgId);
        new MyAsyncQueryHandler(context, data).startQuery(2, null, RcsGroupChatComposeMessageFragment.sMessageUri, new String[]{"thread_id", "body"}, "_id = " + String.valueOf(msgId), null, null);
    }

    public static void sendRCSMessageWithDelay(Context context, long msgId, String msgType) {
        switch (getDelayMessageType(context, msgId, msgType)) {
            case 1:
                sendImMsgDelay(context, msgId);
                return;
            case 3:
                sendRcsFtWithDelay(context, msgId, msgType);
                return;
            default:
                return;
        }
    }

    private static int getDelayMessageType(Context context, long msgId, String msgType) {
        String serviceCenter = "";
        int type = 0;
        if (msgType.equals("chat") && msgId > 0) {
            Context context2 = context;
            Cursor c = SqliteWrapper.query(context2, Uri.parse("content://rcsim/chat"), new String[]{"service_center"}, "_id = ?", new String[]{String.valueOf(msgId)}, null);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        serviceCenter = c.getString(c.getColumnIndexOrThrow("service_center"));
                    }
                } catch (Exception e) {
                    MLog.d("RcsTransaction", e.toString());
                    if (c != null) {
                        c.close();
                    }
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
            if (c != null) {
                c.close();
            }
        }
        if (!TextUtils.isEmpty(serviceCenter)) {
            if ("rcs.im".equals(serviceCenter)) {
                type = 1;
            } else if ("rcs.file".equals(serviceCenter)) {
                type = 3;
            }
        }
        MLog.d("RcsTransaction", "getDelayMessageType type = " + type);
        MLog.d("RcsTransaction", "getDelayMessageType serviceCenter = " + serviceCenter);
        return type;
    }

    private static void sendRcsFtWithDelay(Context context, long msgId, String msgType) {
        String fileName;
        long fileSize;
        String filePath;
        long threadId;
        Bundle sBundle;
        Throwable th;
        MLog.i("RcsTransaction", "preSendRcsFtWithDelay() localId:" + msgId);
        if (msgType.equals("chat") && msgId > 0) {
            String filePath2 = "";
            String mimeType = "";
            String fileName2 = "";
            String address = "";
            String globalTransId = "";
            long chatType = 0;
            Cursor cursor = null;
            Cursor cursor2 = null;
            try {
                Context context2 = context;
                cursor = SqliteWrapper.query(context2, Uri.parse("content://rcsim/file_trans"), new String[]{"file_name", "file_size", "file_type", "file_content", "global_trans_id", "chat_type"}, "msg_id = ? AND chat_type = 1", new String[]{String.valueOf(msgId)}, null);
                context2 = context;
                cursor2 = SqliteWrapper.query(context2, Uri.parse("content://rcsim/chat"), new String[]{"address", "thread_id"}, "_id = ?", new String[]{String.valueOf(msgId)}, null);
                if (cursor == null) {
                    fileName = fileName2;
                    fileSize = 0;
                    filePath = filePath2;
                } else if (cursor.moveToFirst()) {
                    filePath = cursor.getString(cursor.getColumnIndexOrThrow("file_content"));
                    try {
                        fileName = cursor.getString(cursor.getColumnIndexOrThrow("file_name"));
                        try {
                            fileSize = cursor.getLong(cursor.getColumnIndexOrThrow("file_size"));
                        } catch (RuntimeException e) {
                            fileSize = 0;
                            try {
                                MLog.e("RcsTransaction", "cursor unknowable error");
                                if (cursor != null) {
                                    cursor.close();
                                }
                                if (cursor2 != null) {
                                    cursor2.close();
                                }
                                threadId = 0;
                                sBundle = RcseTelephonyExt.createBundleValues(threadId, filePath, fileName, fileSize, mimeType);
                                if (TextUtils.isEmpty(globalTransId)) {
                                    sendFileFinal(filePath, address, sBundle, msgId);
                                    MLog.d("RcsTransaction", "preSendRcsFtWithDelay() send");
                                    return;
                                }
                                resendMessageFileFinal(msgId, chatType);
                                MLog.d("RcsTransaction", "preSendRcsFtWithDelay() resend");
                            } catch (Throwable th2) {
                                th = th2;
                                if (cursor != null) {
                                    cursor.close();
                                }
                                if (cursor2 != null) {
                                    cursor2.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            fileSize = 0;
                            if (cursor != null) {
                                cursor.close();
                            }
                            if (cursor2 != null) {
                                cursor2.close();
                            }
                            throw th;
                        }
                    } catch (RuntimeException e2) {
                        fileName = fileName2;
                        fileSize = 0;
                        MLog.e("RcsTransaction", "cursor unknowable error");
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (cursor2 != null) {
                            cursor2.close();
                        }
                        threadId = 0;
                        sBundle = RcseTelephonyExt.createBundleValues(threadId, filePath, fileName, fileSize, mimeType);
                        if (TextUtils.isEmpty(globalTransId)) {
                            resendMessageFileFinal(msgId, chatType);
                            MLog.d("RcsTransaction", "preSendRcsFtWithDelay() resend");
                        }
                        sendFileFinal(filePath, address, sBundle, msgId);
                        MLog.d("RcsTransaction", "preSendRcsFtWithDelay() send");
                        return;
                    } catch (Throwable th4) {
                        th = th4;
                        fileName = fileName2;
                        fileSize = 0;
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (cursor2 != null) {
                            cursor2.close();
                        }
                        throw th;
                    }
                    try {
                        mimeType = cursor.getString(cursor.getColumnIndexOrThrow("file_type"));
                        globalTransId = cursor.getString(cursor.getColumnIndexOrThrow("global_trans_id"));
                        chatType = cursor.getLong(cursor.getColumnIndexOrThrow("chat_type"));
                    } catch (RuntimeException e3) {
                        MLog.e("RcsTransaction", "cursor unknowable error");
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (cursor2 != null) {
                            cursor2.close();
                        }
                        threadId = 0;
                        sBundle = RcseTelephonyExt.createBundleValues(threadId, filePath, fileName, fileSize, mimeType);
                        if (TextUtils.isEmpty(globalTransId)) {
                            sendFileFinal(filePath, address, sBundle, msgId);
                            MLog.d("RcsTransaction", "preSendRcsFtWithDelay() send");
                            return;
                        }
                        resendMessageFileFinal(msgId, chatType);
                        MLog.d("RcsTransaction", "preSendRcsFtWithDelay() resend");
                    }
                } else {
                    fileName = fileName2;
                    fileSize = 0;
                    filePath = filePath2;
                }
                if (cursor2 == null) {
                    threadId = 0;
                } else if (cursor2.moveToFirst()) {
                    address = cursor2.getString(cursor2.getColumnIndexOrThrow("address"));
                    threadId = cursor2.getLong(cursor2.getColumnIndexOrThrow("thread_id"));
                } else {
                    threadId = 0;
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (cursor2 != null) {
                    cursor2.close();
                }
            } catch (RuntimeException e4) {
                fileName = fileName2;
                fileSize = 0;
                filePath = filePath2;
                MLog.e("RcsTransaction", "cursor unknowable error");
                if (cursor != null) {
                    cursor.close();
                }
                if (cursor2 != null) {
                    cursor2.close();
                }
                threadId = 0;
                sBundle = RcseTelephonyExt.createBundleValues(threadId, filePath, fileName, fileSize, mimeType);
                if (TextUtils.isEmpty(globalTransId)) {
                    resendMessageFileFinal(msgId, chatType);
                    MLog.d("RcsTransaction", "preSendRcsFtWithDelay() resend");
                }
                sendFileFinal(filePath, address, sBundle, msgId);
                MLog.d("RcsTransaction", "preSendRcsFtWithDelay() send");
                return;
            } catch (Throwable th5) {
                th = th5;
                fileName = fileName2;
                fileSize = 0;
                filePath = filePath2;
                if (cursor != null) {
                    cursor.close();
                }
                if (cursor2 != null) {
                    cursor2.close();
                }
                throw th;
            }
            sBundle = RcseTelephonyExt.createBundleValues(threadId, filePath, fileName, fileSize, mimeType);
            if (TextUtils.isEmpty(globalTransId)) {
                sendFileFinal(filePath, address, sBundle, msgId);
                MLog.d("RcsTransaction", "preSendRcsFtWithDelay() send");
                return;
            }
            resendMessageFileFinal(msgId, chatType);
            MLog.d("RcsTransaction", "preSendRcsFtWithDelay() resend");
        }
    }

    public static void preSendImMessage(Context context, String textMsg, String address) {
        long localMsgId = 0;
        if (PreferenceUtils.isCancelSendEnable(context)) {
            localMsgId = addImToDbBeforeSend(textMsg, address, context);
            DelaySendManager.getInst().addDelayMsg(localMsgId, "chat", false);
        } else {
            sendImMessageFinal(context, textMsg, address, 0);
        }
        MLog.i("RcsTransaction", "preSendImMessage(), localId:" + localMsgId);
    }

    private static void sendImMessageFinal(Context context, String textMsg, String address, long msgIdBeforeSend) {
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (aMsgPlus != null) {
            MLog.i("RcsTransaction", "sendImMessageFinal(), localId:" + msgIdBeforeSend);
            try {
                aMsgPlus.sendMessageImWithLocalId(textMsg, address, msgIdBeforeSend);
            } catch (RemoteException e) {
                MLog.e("RcsTransaction", "sendMessageIm error");
            }
        }
    }

    private static long addImToDbBeforeSend(String textMsg, String address, Context context) {
        ContentValues values = new ContentValues();
        values.put("address", PhoneNumberUtils.normalizeNumber(address));
        values.put("date", Long.valueOf(System.currentTimeMillis()));
        values.put("read", Integer.valueOf(1));
        values.put(NumberInfo.TYPE_KEY, Integer.valueOf(16));
        values.put("body", textMsg);
        values.put("service_center", "rcs.im");
        values.put("seen", Integer.valueOf(1));
        values.put("sdk_sms_id", Integer.valueOf(0));
        return insertRcsDelayMsg(values, "chat", context);
    }

    private static long addFileToDbBeforeSend(String filePath, String address, Context context, FileInfo info, int chatType) {
        ContentValues values = new ContentValues();
        address = PhoneNumberUtils.normalizeNumber(address);
        long data = System.currentTimeMillis();
        values.put("address", address);
        values.put("date", Long.valueOf(data));
        values.put("read", Integer.valueOf(1));
        values.put(NumberInfo.TYPE_KEY, Integer.valueOf(16));
        values.put("body", filePath);
        values.put("service_center", "rcs.file");
        values.put("seen", Integer.valueOf(1));
        values.put("sdk_sms_id", Integer.valueOf(0));
        values.put("file_type", Integer.valueOf(RcsUtility.getFileType(filePath, chatType, true)));
        long retVal = insertRcsDelayMsg(values, "chat", context);
        values.clear();
        values.put("msg_id", Long.valueOf(retVal));
        values.put("file_name", info.getFileDisplayName());
        values.put("file_size", Long.valueOf(info.getTotalSize()));
        values.put("file_type", info.getMimeType());
        values.put("date", Long.valueOf(data));
        values.put("file_content", filePath);
        values.put("chat_type", Integer.valueOf(chatType));
        values.put("transfer_status", Integer.valueOf(Place.TYPE_ROOM));
        insertRcsDelayMsg(values, "ft", context);
        return retVal;
    }

    public static void sendSliceIm(int maxLength, String msg, WorkingMessage workingMessage, ComposeMessageFragment fragment) {
        int offset = 0;
        int sendLength = maxLength;
        Log.d("RcsTransaction", "RcsTransaction sendSliceIm");
        try {
            byte[] data = msg.getBytes("utf-8");
            while (data.length > offset) {
                if (data.length - offset < sendLength) {
                    sendLength = data.length - offset;
                }
                byte[] tempData = new byte[sendLength];
                System.arraycopy(data, offset, tempData, 0, sendLength);
                if (sendLength == maxLength) {
                    String sendStr = RcsUtility.getStrOfUtf8Bytes(tempData);
                    sendRcseIm(workingMessage.getConversation(), workingMessage, null, sendStr, fragment);
                    offset += sendStr.getBytes("utf-8").length;
                } else {
                    sendRcseIm(workingMessage.getConversation(), workingMessage, null, new String(tempData, "utf-8"), fragment);
                    return;
                }
            }
        } catch (UnsupportedEncodingException e) {
            MLog.e("RcsTransaction", "Unsupported Encoding error");
        }
    }

    private static void sendGroupMessageFinal(String groupId, String message, long localId) {
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (aMsgPlus != null) {
            try {
                aMsgPlus.sendGroupMessageWithLocalId(groupId, message, localId);
            } catch (RemoteException e) {
                MLog.w("RcsTransaction", "RcsService Remote exception");
            }
        }
    }

    private static void preSendGroupMessage(Context context, String groupId, String message) {
        if (PreferenceUtils.isCancelSendEnable(context)) {
            Bundle data = new Bundle();
            data.putString("text", message);
            new MyAsyncQueryHandler(context, data).startQuery(1, null, RcsGroupChatComposeMessageFragment.sGroupUri, new String[]{"thread_id"}, "name = '" + groupId + "'", null, null);
            return;
        }
        sendGroupMessageFinal(groupId, message, 0);
    }

    private static long addGroupChatToDbBeforeSend(String message, long threadId, Context context) {
        ContentValues values = new ContentValues();
        values.put("thread_id", Long.valueOf(threadId));
        values.put(NumberInfo.TYPE_KEY, Integer.valueOf(4));
        values.put("date", Long.valueOf(System.currentTimeMillis()));
        values.put("read", Integer.valueOf(1));
        values.put("status", Integer.valueOf(0));
        values.put("file_mode", Integer.valueOf(0));
        values.put("body", message);
        values.put("seen", Integer.valueOf(0));
        values.put("file_type", Integer.valueOf(0));
        values.put("sdk_rcs_group_message_id", Integer.valueOf(0));
        return insertRcsDelayMsg(values, "rcs_group_text", context);
    }

    public static void toSendGroupMessage(Context context, String groupId, String message) {
        if (context != null) {
            int offset = 0;
            int maxLength = getMaxGroupMessageTextSize();
            int sendLength = maxLength;
            Log.d("RcsTransaction", "RcsTransaction toSendGroupMessage");
            try {
                byte[] data = message.getBytes("utf-8");
                while (data.length > offset) {
                    if (data.length - offset < sendLength) {
                        sendLength = data.length - offset;
                    }
                    byte[] tempData = new byte[sendLength];
                    System.arraycopy(data, offset, tempData, 0, sendLength);
                    if (sendLength != maxLength) {
                        preSendGroupMessage(context, groupId, new String(tempData, "utf-8"));
                        break;
                    }
                    String sendStr = RcsUtility.getStrOfUtf8Bytes(tempData);
                    preSendGroupMessage(context, groupId, sendStr);
                    offset += sendStr.getBytes("utf-8").length;
                }
            } catch (UnsupportedEncodingException e) {
                MLog.e("RcsTransaction", "toSendGroupMessage UnsupportedEncodingException");
            } catch (Exception e2) {
                MLog.e("RcsTransaction", "Unknown error");
            }
        }
    }

    public static Uri[] getCopyFileBeforeSendSingleUri(Context context, Uri uri) {
        Uri[] uris = new Uri[]{null, uri};
        if (!(uri == null || context == null)) {
            FileInfo info = getFileInfoByData(context, uri);
            if (info != null) {
                String mimeType = info.getMimeType();
                String path = info.getSendFilePath();
                String displayName = info.getFileDisplayName();
                MLog.d("RcsTransaction", "copyFileBeforeSendSingle -> uri = " + uri + ", mimeType = " + mimeType + ", path = " + path + ", displayName = " + displayName);
                if (!TextUtils.isEmpty(path) && (new File(path).exists() || isMmsUri(uri))) {
                    boolean startsWith = TextUtils.isEmpty(mimeType) ? false : mimeType.startsWith("image");
                    String outputPath = RcsUtility.getCacheDirPath(startsWith) + File.separator + displayName + (!displayName.contains(".") ? "." + RcsMediaFileUtils.getFileExtensionByMimeType(mimeType) : "");
                    MLog.d("RcsTransaction", "copyFileBeforeSendSingle -> outputPath = " + outputPath + ", isImage = " + startsWith);
                    if (!path.equals(outputPath)) {
                        if (!isMmsUri(uri)) {
                            uris[1] = Uri.fromFile(new File(path));
                        }
                        uris[0] = Uri.fromFile(new File(outputPath));
                    }
                }
            }
        }
        return uris;
    }

    public static Uri copyFileBeforeSendSingle(Context context, Uri uri) {
        Uri[] uris = getCopyFileBeforeSendSingleUri(context, uri);
        if (uris == null || uris.length != 2 || uris[0] == null || uris[1] == null) {
            return uri;
        }
        return changeToLocalUri(uris[1], uris[0], context);
    }

    private static void excuteSendMixMessageRunnable(Runnable runnable) {
        HandlerThread mSendMixMessageThread = new HandlerThread("sendMixMessage");
        mSendMixMessageThread.start();
        new Handler(mSendMixMessageThread.getLooper()).post(runnable);
    }

    private static boolean sendMixMessage(Conversation conv, WorkingMessage workingMessage, ComposeMessageFragment fragment, int maxInputSize) {
        boolean result = true;
        boolean videoResult = true;
        boolean hasMmsAttachment = workingMessage.hasAttachment();
        MLog.d("RcsTransaction", "sendMixMessage hasMmsAttachment = " + hasMmsAttachment);
        ArrayList<Uri> vCardUris = new ArrayList();
        if (hasMmsAttachment) {
            ArrayList<Uri> imageUris = new ArrayList();
            ArrayList<String> texts = new ArrayList();
            ArrayList<Uri> videoUris = new ArrayList();
            ArrayList<HashMap<String, String>> locationDatas = new ArrayList();
            ArrayList<Uri> audioUris = new ArrayList();
            ArrayList<Uri> calendarUris = new ArrayList();
            String[] numbers = conv.getRecipients().getNumbers();
            ArrayList<String> addresses = new ArrayList();
            for (String add : numbers) {
                addresses.add(add);
            }
            Iterator<SlideModel> slideModels = workingMessage.getSlideshow().iterator();
            int videoCount = 0;
            while (slideModels.hasNext()) {
                SlideModel sm = (SlideModel) slideModels.next();
                if (sm != null) {
                    Iterator<MediaModel> medias = sm.iterator();
                    while (medias.hasNext()) {
                        MediaModel media = (MediaModel) medias.next();
                        if (media != null) {
                            if (media.isText()) {
                                if ("text/plain".equals(media.getContentType()) && (media instanceof TextModel)) {
                                    TextModel text = (TextModel) media;
                                    MLog.d("RcsTransaction", "sendMixMessage -> text.");
                                    if (!TextUtils.isEmpty(text.getText())) {
                                        texts.add(text.getText());
                                    }
                                }
                            } else if (media.isImage() && !media.isLocation()) {
                                Uri imageUri = media.getUri();
                                Uri imageFileUri = copyFileBeforeSendSingle(fragment.getContext(), imageUri);
                                MLog.d("RcsTransaction", "sendMixMessage -> imageUri = " + imageUri + ", imageFileUri = " + imageFileUri);
                                imageUris.add(imageFileUri);
                            } else if (media.isImage() && media.isLocation()) {
                                if (media.getLocationSource() != null) {
                                    locationDatas.add(media.getLocationSource());
                                }
                            } else if (media.isAudio()) {
                                Uri audioUri = media.getUri();
                                Uri audioFileUri = copyFileBeforeSendSingle(fragment.getContext(), audioUri);
                                MLog.d("RcsTransaction", "sendMixMessage -> audioUri = " + audioUri + ", audioFileUri = " + audioFileUri);
                                audioUris.add(audioFileUri);
                            } else if (media.isVideo()) {
                                Uri videoFileUri = media.getUri();
                                String videoPath = media.getSourceBuild();
                                if (videoPath != null && isMmsUri(videoFileUri)) {
                                    videoFileUri = Uri.fromFile(new File(videoPath));
                                }
                                MLog.d("RcsTransaction", "sendMixMessage videoUris.size = " + media.getMediaSize());
                                if (fragment.getRichEditor() != null && fragment.getRichEditor().getFullSizeFlag()) {
                                    mContext = fragment.getContext();
                                    long maxFileSizePermitedValue = (long) getMaxFileSizePermitedValue();
                                    if (isCustFileSize()) {
                                        checkVideoTypeFile((long) media.getMediaSize());
                                    } else if (((long) media.getMediaSize()) > maxFileSizePermitedValue) {
                                        sendFileExceedMaxSizeMessage(null, maxFileSizePermitedValue);
                                    } else {
                                        videoUris.add(videoFileUri);
                                    }
                                } else if (videoCount == 0) {
                                    videoUris.add(videoFileUri);
                                } else if (videoCount == 1) {
                                    Toast.makeText(fragment.getContext(), fragment.getContext().getString(R.string.text_compress_one_at_a_time), 0).show();
                                    videoUris.clear();
                                } else {
                                    MLog.d("RcsTransaction", "sendMixMessage exceeds 2 video attachments.");
                                }
                                videoCount++;
                            } else if (media.isVcard()) {
                                Uri vCardUri = media.getUri();
                                Uri vCardFileUri = handleVcardData(vCardUri, fragment.getContext());
                                MLog.d("RcsTransaction", "sendMixMessage -> vCardUri = " + vCardUri + ", vCardFileUri = " + vCardFileUri);
                                vCardUris.add(vCardFileUri);
                            } else if (media.isVCalendar()) {
                                Uri vCalendar = media.getUri();
                                Uri vCalendarFileUri = saveVCalendarAsLocalFile(vCalendar, fragment.getContext());
                                MLog.d("RcsTransaction", "sendMixMessage -> vCalendar = " + vCalendar + ", vCalendarFileUri = " + vCalendarFileUri);
                                calendarUris.add(vCalendarFileUri);
                            }
                        }
                    }
                }
            }
            SendMixMessageRunnable mSendMixMessageRunnable = new SendMixMessageRunnable(workingMessage, fragment, maxInputSize);
            mSendMixMessageRunnable.initMessageData(addresses, texts, imageUris, videoUris, audioUris, vCardUris, calendarUris, locationDatas);
            CancelSendMixMessageRunnable cancelSendMixMessageRunnable = new CancelSendMixMessageRunnable(fragment);
            cancelSendMixMessageRunnable.initMessageData(vCardUris, 3);
            if (vCardUris.isEmpty()) {
                MLog.d("RcsTransaction", "sendMixMessage is no vcard.");
                if (!videoUris.isEmpty()) {
                    videoResult = false;
                }
                excuteSendMixMessageRunnable(mSendMixMessageRunnable);
            } else {
                MLog.d("RcsTransaction", "sendMixMessage has vcard.");
                if (fragment.getContext().getSharedPreferences(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, 0).getBoolean("no_need_dialog_for_vcf", false)) {
                    MLog.d("RcsTransaction", "sendMixMessage has vcard. don't show dialog.");
                    excuteSendMixMessageRunnable(mSendMixMessageRunnable);
                } else {
                    MLog.d("RcsTransaction", "sendMixMessage has vcard. need show dialog.");
                    showSafetyDialogForVcard(fragment.getContext(), mSendMixMessageRunnable, cancelSendMixMessageRunnable);
                    result = false;
                }
            }
        }
        MLog.d("RcsTransaction", "sendMixMessage videoResult = " + videoResult);
        if (fragment != null) {
            Context context = fragment.getContext();
            if (videoResult) {
                videoResult = result;
            }
            clearDraft(workingMessage, context, videoResult);
        }
        return result;
    }

    public static boolean send(String recipientsInUI, String messageText, WorkingMessage workingMessage, ComposeMessageFragment fragment, int maxInputSize) {
        MLog.i("RcsTransaction", "RcsTransaction send message ++");
        boolean result = true;
        Conversation conv = workingMessage.getConversation();
        if (messageText != null) {
            int messageLength = messageText.length();
            MLog.d("RcsTransaction", "RcsTransaction send messageLength=" + messageLength);
            if (messageLength <= 0) {
                if (fragment == null) {
                    return false;
                }
                if (fragment.getRcsComposeMessage() != null) {
                    fragment.getRcsComposeMessage().setSentMessage(false);
                    fragment.getRcsComposeMessage().setSendingMessage(false);
                }
                if (workingMessage.getSlideshow() == null || workingMessage.getSlideshow().size() == 0) {
                    return false;
                }
            }
            if (shouldDoSendSliceIm(messageLength, workingMessage)) {
                sendSliceIm(maxInputSize, messageText, workingMessage, fragment);
            }
        }
        boolean needClearDraft = sendMixMessage(conv, workingMessage, fragment, maxInputSize);
        MLog.i("RcsTransaction", "RcsTransaction send needClearDraft:" + needClearDraft);
        if (!needClearDraft) {
            result = false;
        }
        MLog.i("RcsTransaction", "RcsTransaction send message --");
        return result;
    }

    private static boolean shouldDoSendSliceIm(int messageLength, WorkingMessage workingMessage) {
        boolean isTextMsg = false;
        if (messageLength <= 0 || workingMessage == null) {
            return false;
        }
        SlideshowModel slideShow = workingMessage.getSlideshow();
        if (slideShow == null) {
            return true;
        }
        int slideSize = slideShow.size();
        if (slideSize <= 0) {
            return true;
        }
        if (slideSize != 1) {
            return false;
        }
        SlideModel slideModel = slideShow.get(0);
        if (slideModel.hasText()) {
            isTextMsg = slideModel.hasRoomForAttachment();
        }
        return isTextMsg;
    }

    private static void sendRcseIm(final Conversation conv, WorkingMessage workingMessage, String msgSubject, final String massageText, final ComposeMessageFragment fragment) {
        new Thread(new Runnable() {
            public void run() {
                fragment.onPreMessageSent();
                ImMsgData aRcseData = new ImMsgData();
                aRcseData.mConv = conv;
                aRcseData.mMsgText = massageText;
                aRcseData.mThreadId = -1;
                RcsTransaction.sendImChatMessages(fragment.getContext(), aRcseData);
                fragment.onMessageSent();
                RcsTransaction.updateSendStats(fragment.getContext(), aRcseData);
            }
        }).start();
        RecipientIdCache.updateNumbers(conv.getThreadId(), conv.getRecipients());
        workingMessage.setDiscarded(true);
    }

    private static void updateSendStats(Context context, ImMsgData aRcseData) {
        if (aRcseData.mConv != null) {
            String[] RcseNumber = aRcseData.mConv.getRecipients().getNumbers();
            ArrayList<String> phoneNumbers = new ArrayList();
            DataUsageStatUpdater updater = new DataUsageStatUpdater(context);
            try {
                for (String number : RcseNumber) {
                    phoneNumbers.add(HwMessageUtils.replaceNumberFromDatabase(number, context));
                }
                updater.updateWithPhoneNumber(phoneNumbers);
            } catch (SQLiteException e) {
                Log.e("RcsTransaction", "too many SQL variables");
            }
        }
    }

    private static void updateSendStats(Context context, List<String> phoneNumbers) {
        DataUsageStatUpdater updater = new DataUsageStatUpdater(context);
        try {
            ArrayList<String> formatNumbers = new ArrayList();
            for (String number : phoneNumbers) {
                formatNumbers.add(HwMessageUtils.replaceNumberFromDatabase(number, context.getApplicationContext()));
            }
            updater.updateWithPhoneNumber(formatNumbers);
        } catch (SQLiteException e) {
            MLog.e("RcsTransaction", "too many SQL variables");
        }
    }

    public static FileInfo getFileInfoByData(Context context, Object aFileTransData) {
        String path = null;
        String displayName = null;
        long totalSize = 0;
        try {
            String mimeType = "application/octet-stream";
            FileInfo fileInfo = new FileInfo();
            File file;
            if (aFileTransData instanceof Uri) {
                Uri mediaFileUri = (Uri) aFileTransData;
                if ("content".equals(mediaFileUri.getScheme())) {
                    Map<String, String> map;
                    if (isMmsUri(mediaFileUri)) {
                        map = getFileInfoForMmsUri(context, mediaFileUri);
                    } else {
                        map = queryFileInfo(context, mediaFileUri);
                    }
                    if (map == null) {
                        MLog.w("RcsTransaction FileTrans: ", "getFileInfoByData but map is null,so return ");
                        return null;
                    }
                    displayName = (String) map.get("displayName");
                    if (map.get("totalSize") != null) {
                        totalSize = Long.parseLong((String) map.get("totalSize"));
                        mimeType = (String) map.get("mimeType");
                        path = (String) map.get("path");
                        if (path == null) {
                            path = getPath(context, mediaFileUri);
                        }
                        if (path == null) {
                            MLog.w("RcsTransaction FileTrans: ", "getFileInfoByData but path is null,so return ");
                            return null;
                        }
                        mimeType = checkMimeType(mimeType, path);
                    } else {
                        MLog.w("RcsTransaction FileTrans: ", "getFileInfoByData but totalSize is null,so return ");
                        return null;
                    }
                } else if ("file".equals(mediaFileUri.getScheme())) {
                    file = new File(mediaFileUri.getPath());
                    path = file.getAbsolutePath();
                    displayName = file.getName();
                    totalSize = file.length();
                    if (RcsMediaFileUtils.getFileType(path) != null) {
                        mimeType = RcsMediaFileUtils.getFileType(path).mimeType;
                    } else {
                        MLog.w("RcsTransaction FileTrans: ", "RcsMediaFileUtils getFileType is null");
                    }
                }
            } else if (!(aFileTransData instanceof String)) {
                return null;
            } else {
                file = new File(String.valueOf(aFileTransData));
                path = file.getAbsolutePath();
                displayName = file.getName();
                totalSize = file.length();
                if (RcsMediaFileUtils.getFileType(path) != null) {
                    mimeType = RcsMediaFileUtils.getFileType(path).mimeType;
                }
            }
            if (displayName == null && "image/jpeg".equals(mimeType) && path != null) {
                displayName = path.substring(path.lastIndexOf(47) + 1) + ".jpeg";
            }
            fileInfo.setFileDisplayName(displayName);
            fileInfo.setSendFilePath(path);
            fileInfo.setTotalSize(totalSize);
            if (TextUtils.isEmpty(mimeType)) {
                mimeType = "application/octet-stream";
            }
            fileInfo.setMimeType(mimeType);
            return fileInfo;
        } catch (RuntimeException e) {
            MLog.e("RcsTransaction FileTrans: ", "getFileInfoByData Exception");
            return null;
        }
    }

    public static String checkMimeType(String mimeType, String path) {
        if (mimeType != null) {
            return mimeType;
        }
        MediaFileType filetype = RcsMediaFileUtils.getFileType(path);
        if (filetype != null) {
            return filetype.mimeType;
        }
        return mimeType;
    }

    public static boolean handleFileTransferAction(ComposeMessageFragment fragment, long threadId, Intent intent) {
        Context context = fragment.getContext();
        try {
            List<Uri> uriList = RcsUtility.getUriFromIntent(context, intent);
            String address = intent.getStringExtra("ADDRESS");
            List<String> addrList = new ArrayList();
            addrList.add(address);
            if (isAnyImageFile(context, uriList)) {
                MLog.i("RcsTransaction FileTrans: ", "handleFileTransferAction about Image  video and file ");
                handleImageFileTransfer(context, threadId, uriList, addrList);
                return true;
            }
            boolean isNotVcfFile = false;
            List<Uri> listuri = isVCardFile(context, uriList, intent);
            if (listuri != null) {
                List<Uri> vcardList = new ArrayList();
                if (isVcfFileNotVcfShare(listuri)) {
                    isNotVcfFile = true;
                }
                for (Uri preUri : listuri) {
                    vcardList.add(handleVcardData(preUri, context));
                }
                MLog.i("RcsTransaction FileTrans: ", "handleFileTransferAction about vcard ");
                if (isNotVcfFile || listuri.size() != 1) {
                    multiSend(context, Long.valueOf(threadId), vcardList, addrList, 0);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putLong("thread_id", threadId);
                    RcsUtility.showUserFtNoNeedVardDialog(vcardList, addrList, null, bundle, 5, context, fragment, null, null);
                }
                return true;
            } else if (isVCalendarFile(intent) && isNeedToSaveFile((Uri) uriList.get(0))) {
                List<Uri> vcalendarList = new ArrayList();
                for (Uri vUri : uriList) {
                    vcalendarList.add(saveVCalendarAsLocalFile(vUri, context));
                }
                multiSend(context, Long.valueOf(threadId), vcalendarList, addrList, 0);
                return true;
            } else {
                multiSend(context, Long.valueOf(threadId), uriList, addrList, 0);
                return true;
            }
        } catch (RuntimeException e) {
            MLog.e("RcsTransaction FileTrans: ", "Method handleFileTransferAction failed.");
            return false;
        }
    }

    private static boolean isVcfFileNotVcfShare(List<Uri> listuri) {
        if (listuri == null || listuri.size() != 1) {
            return false;
        }
        String filename = ((Uri) listuri.get(0)).toString();
        return filename.substring(filename.lastIndexOf(".") + 1).equals("vcf");
    }

    public static void multiGroupSend(Context context, List<MediaModel> attachmentData, long threadId, String groupId, boolean fullSize) {
        final List<MediaModel> list = attachmentData;
        final Context context2 = context;
        final String str = groupId;
        final boolean z = fullSize;
        final long j = threadId;
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (RcsProfile.getRcsService() == null) {
                        MLog.e("RcsTransaction FileTrans: ", "RcsService is null, send file failed.");
                    } else if (list != null && context2 != null) {
                        Activity activity = null;
                        final List<Uri> videoNeedCompress = new ArrayList();
                        final List<String> addressNeedCompress = new ArrayList();
                        for (MediaModel media : list) {
                            if (!media.isLocation() || media.getLocationSource() == null) {
                                final Uri data = media.getUri();
                                final FileInfo info = RcsTransaction.getFileInfoByData(context2, data);
                                if (info == null) {
                                    MLog.e("RcsTransaction FileTrans: ", "Can't find file represented by this Uri : ");
                                    MLog.i("RcsTransaction FileTrans: ", " data : " + data);
                                    return;
                                } else if (!((z && RcsTransaction.checkFileSize(context2, info)) || !(context2 instanceof RcsGroupChatComposeMessageActivity) || str == null)) {
                                    if (info.getMimeType().contains("image") && !z) {
                                        RcsUtility.compressImage(context2, data, info);
                                        RcsTransaction.sendCompressFile(context2, info, j, str);
                                    } else if (!info.getMimeType().contains("video") || z) {
                                        RcsTransaction.preSendGroupFile(context2, info, j, str);
                                    } else {
                                        activity = (RcsGroupChatComposeMessageActivity) context2;
                                        final Context context = context2;
                                        final long j = j;
                                        final String str = str;
                                        activity.runOnUiThread(new Runnable() {
                                            public void run() {
                                                String filePath = info.getSendFilePath();
                                                if (!TextUtils.isEmpty(filePath) && !RcsUtility.resolutionVerity4Video(filePath)) {
                                                    List<Uri> video = new ArrayList();
                                                    video.add(data);
                                                    List<String> address = new ArrayList();
                                                    address.add("");
                                                    RcsTransaction.sendCompressedVideo(context, Long.valueOf(j), video, address, 120, str);
                                                } else if (!TextUtils.isEmpty(filePath) && RcsUtility.resolutionVerity4Video(filePath)) {
                                                    videoNeedCompress.add(data);
                                                    addressNeedCompress.add("");
                                                }
                                            }
                                        });
                                    }
                                }
                            } else {
                                String title = (String) media.getLocationSource().get("title");
                                String subTitle = (String) media.getLocationSource().get("subtitle");
                                RcsTransaction.groupSendLocation(str, Double.valueOf((String) media.getLocationSource().get("latitude")).doubleValue(), Double.valueOf((String) media.getLocationSource().get("longitude")).doubleValue(), title, subTitle);
                            }
                        }
                        if (activity != null) {
                            final Context context2 = context2;
                            final long j2 = j;
                            final String str2 = str;
                            final List<Uri> list = videoNeedCompress;
                            final List<String> list2 = addressNeedCompress;
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (list != null && list.size() > 1) {
                                        Toast.makeText(context2, context2.getString(R.string.text_compress_one_at_a_time), 0).show();
                                    } else if (list != null && list.size() == 1) {
                                        RcsTransaction.sendCompressedVideo(context2, Long.valueOf(j2), list, list2, 120, str2);
                                    }
                                }
                            });
                        }
                    } else {
                        return;
                    }
                } catch (RuntimeException e) {
                    MLog.e("RcsTransaction", "Method multiSend failed.");
                }
                if (context2 instanceof RcsGroupChatComposeMessageActivity) {
                    RcsGroupChatComposeMessageFragment fragment = (RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag(context2, "Mms_UI_GCCMF");
                    if (!(fragment == null || fragment.getRichEditor() == null)) {
                        fragment.onFinishGroupChatFileSent();
                    }
                }
            }
        }).start();
    }

    public static void sendCompressFile(Context context, FileInfo info, long threadId, String groupId) {
        if (context != null && info != null && !TextUtils.isEmpty(groupId)) {
            preSendGroupFile(context, info, threadId, groupId);
        }
    }

    public static void multiSend(Context context, Object threadIdObject, List<?> dataList, List<String> addrList, int request) {
        multiSend(context, threadIdObject, dataList, addrList, request, null);
    }

    public static void multiSend(Context context, Object threadIdObject, List<?> dataList, List<String> addrList, int request, String groupId) {
        final Object obj = threadIdObject;
        final List<?> list = dataList;
        final Context context2 = context;
        final List<String> list2 = addrList;
        final String str = groupId;
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (RcsProfile.getRcsService() != null) {
                        long threadId = obj instanceof Conversation ? ((Conversation) obj).ensureThreadId() : ((Long) obj).longValue();
                        if (list != null) {
                            for (Object data : list) {
                                FileInfo info = RcsTransaction.getFileInfoByData(context2, data);
                                if (info == null) {
                                    MLog.e("RcsTransaction FileTrans: ", "Can't find file represented by this Uri : ");
                                    MLog.i("RcsTransaction FileTrans: ", " data : " + data);
                                    return;
                                } else if (!(RcsTransaction.checkFileSize(context2, info) || list2 == null)) {
                                    RcsTransaction.updateSendStats(context2, list2);
                                    for (String address : list2) {
                                        if (!(context2 instanceof RcsGroupChatComposeMessageActivity) || str == null) {
                                            RcsTransaction.preSendFile(context2, address, info, threadId);
                                        } else {
                                            RcsTransaction.preSendGroupFile(context2, info, threadId, str);
                                        }
                                    }
                                    continue;
                                }
                            }
                        } else {
                            return;
                        }
                    }
                    MLog.e("RcsTransaction FileTrans: ", "RcsService is null, send file failed.");
                } catch (RuntimeException e) {
                    MLog.e("RcsTransaction", "Method multiSend failed.");
                }
            }
        }).start();
    }

    public static boolean isMmsUri(Uri uri) {
        return uri == null ? false : uri.getAuthority().startsWith("mms");
    }

    public static Map<String, String> getFileInfoForMmsUri(Context context, Uri mediaFileUri) {
        Throwable th;
        Map<String, String> map = null;
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, mediaFileUri, new String[]{"_data", "ct", "cl"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                Map<String, String> map2 = new HashMap();
                try {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                    String contentType = cursor.getString(cursor.getColumnIndexOrThrow("ct"));
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow("cl"));
                    File mediaFile = new File(path);
                    long size = !mediaFile.exists() ? 0 : mediaFile.length();
                    MLog.d("RcsTransaction", "getFileInfoForMmsUri -> path = " + path + ", mimeType = " + contentType + ", size = " + String.valueOf(size) + ", displayname = " + displayName);
                    map2.put("path", path);
                    map2.put("displayName", displayName);
                    map2.put("totalSize", String.valueOf(size));
                    map2.put("mimeType", contentType);
                    map = map2;
                } catch (RuntimeException e) {
                    map = map2;
                    try {
                        MLog.e("RcsTransaction", "cursor unknowable error");
                        if (cursor != null) {
                            cursor.close();
                        }
                        return map;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException e2) {
            MLog.e("RcsTransaction", "cursor unknowable error");
            if (cursor != null) {
                cursor.close();
            }
            return map;
        }
        return map;
    }

    public static void multiSendWithUriResized(Context context, long threadId, List<Uri> uriList, List<String> addrList, int request) {
        multiSendWithUriResized(context, threadId, uriList, addrList, request, null);
    }

    public static void multiSendWithUriResized(Context context, long threadId, List<Uri> uriList, List<String> addrList, int request, String groupId) {
        List<Uri> vList = RcsUtility.divideVideoList(context, uriList);
        if (vList.size() == 1) {
            sendCompressedVideo(context, Long.valueOf(threadId), vList, addrList, request, groupId);
        } else if (vList.size() > 1) {
            Toast.makeText(context, context.getString(R.string.text_compress_one_at_a_time), 0).show();
            return;
        }
        final List<Uri> list = uriList;
        final Context context2 = context;
        final List<String> list2 = addrList;
        final String str = groupId;
        final long j = threadId;
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (RcsProfile.getRcsService() != null) {
                        for (Uri uri : list) {
                            FileInfo info = RcsTransaction.getFileInfoByData(context2, uri);
                            if (info == null) {
                                MLog.e("RcsTransaction FileTrans: ", "Can't find file represented by this Uri : ");
                                MLog.i("RcsTransaction FileTrans: ", "Uri : " + uri);
                                return;
                            }
                            if (info.getMimeType().contains("image")) {
                                RcsUtility.compressImage(context2, uri, info);
                            }
                            if (!RcsTransaction.checkFileSize(context2, info)) {
                                RcsTransaction.updateSendStats(context2, list2);
                                for (String address : list2) {
                                    if (str == null || !(context2 instanceof RcsGroupChatComposeMessageActivity)) {
                                        RcsTransaction.preSendFile(context2, address, info, j);
                                    } else {
                                        RcsTransaction.preSendGroupFile(context2, info, j, str);
                                    }
                                }
                                continue;
                            }
                        }
                    } else {
                        MLog.e("RcsTransaction FileTrans: ", "RcsService is null, send file failed.");
                    }
                } catch (RuntimeException e) {
                    MLog.e("RcsTransaction", "Method multiSendWithUriResized failed.");
                }
            }
        }).start();
    }

    public static void preSendFile(Context context, String address, FileInfo info, long threadId) {
        if (context == null) {
            MLog.d("RcsTransaction", "preSendFile() context is null");
            return;
        }
        long localMsgId = 0;
        if (PreferenceUtils.isCancelSendEnable(context)) {
            localMsgId = addFileToDbBeforeSend(info.getSendFilePath(), address, context, info, 1);
            DelaySendManager.getInst().addDelayMsg(localMsgId, "chat", false);
            if (context instanceof ComposeMessageActivity) {
                ((ComposeMessageFragment) FragmentTag.getFragmentByTag((ComposeMessageActivity) context, "Mms_UI_CMF")).onMessageSent();
            }
        } else {
            sendFileFinal(info.getSendFilePath(), address, RcseTelephonyExt.createBundleValues(threadId, info.getSendFilePath(), info.getFileDisplayName(), info.getTotalSize(), info.getMimeType()), 0);
        }
        MLog.i("RcsTransaction", "preSendImMessage(), localId:" + localMsgId + "   getMimeType = " + info.getMimeType());
    }

    private static void sendFileFinal(String filePath, String address, Bundle bundle, long msgId) {
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (aMsgPlus != null) {
            try {
                aMsgPlus.sendFileWithLocalId(filePath, address, msgId, bundle);
            } catch (Exception e) {
                MLog.e("RcsTransaction", "sendFileFinal error");
            }
        }
    }

    public static boolean checkFileSize(Context context, FileInfo fileInfo) {
        mContext = context;
        if (isCustFileSize()) {
            MLog.i("RcsTransaction FileTrans: ", "isCMCCCust  checkFileSizeByType");
            return checkFileSizeByType(fileInfo);
        }
        long fileTotalSize = fileInfo.getTotalSize();
        Message msg;
        Bundle data;
        if (fileTotalSize > ((long) getMaxFileSizePermitedValue())) {
            msg = handler.obtainMessage();
            data = new Bundle();
            data.putInt("key", 1);
            data.putString("filename", fileInfo.getFileDisplayName());
            data.putString("maxFileSizeLimit", Formatter.formatFileSize(context, (long) getMaxFileSizePermitedValue()));
            msg.setData(data);
            handler.sendMessage(msg);
            return true;
        } else if (fileTotalSize != 0) {
            return false;
        } else {
            msg = handler.obtainMessage();
            data = new Bundle();
            data.putInt("key", 2);
            msg.setData(data);
            handler.sendMessage(msg);
            return true;
        }
    }

    private static boolean checkFileSizeByType(FileInfo fileInfo) {
        MediaFileType fileType = RcsMediaFileUtils.getFileType(fileInfo.getFileDisplayName());
        if (fileType != null) {
            MLog.i("RcsTransaction FileTrans: ", "fileType = " + RcsMediaFileUtils.isImageFileType(fileType.fileType));
            if (RcsMediaFileUtils.isImageFileType(fileType.fileType)) {
                return checkImageTypeFile(fileInfo);
            }
            if (RcsMediaFileUtils.isVideoFileType(fileType.fileType)) {
                Log.d("RcsTransaction", "RcsMediaFileUtils.isVideoFileType");
                return checkVideoTypeFile(fileInfo);
            }
        }
        return checkOtherTypeFile(fileInfo);
    }

    private static boolean checkImageTypeFile(FileInfo fileInfo) {
        if (isImageExceedResolution(fileInfo)) {
            return true;
        }
        long fileTotalSize = fileInfo.getTotalSize();
        int iMaxImageTypeSizePermit = getImageTypeMaxSize();
        MLog.i("RcsTransaction FileTrans: ", "checkImageTypeFile TypeSizePermit = " + iMaxImageTypeSizePermit + " fileTotalSize = " + fileTotalSize);
        if (fileTotalSize <= ((long) iMaxImageTypeSizePermit)) {
            return false;
        }
        sendFileExceedMaxSizeMessage(fileInfo, (long) iMaxImageTypeSizePermit);
        return true;
    }

    private static boolean checkVideoTypeFile(FileInfo fileInfo) {
        long fileTotalSize = fileInfo.getTotalSize();
        int iMaxVideoTypeSizePermit = getVideoTypeMaxSize();
        MLog.i("RcsTransaction FileTrans: ", "checkVideoTypeFile SizePermit = " + iMaxVideoTypeSizePermit + " fileTotalSize " + fileTotalSize);
        if (fileTotalSize <= ((long) iMaxVideoTypeSizePermit)) {
            return false;
        }
        sendFileExceedMaxSizeMessage(fileInfo, (long) iMaxVideoTypeSizePermit);
        return true;
    }

    private static boolean checkVideoTypeFile(long fileTotalSize) {
        int iMaxVideoTypeSizePermit = getVideoTypeMaxSize();
        if (fileTotalSize <= ((long) iMaxVideoTypeSizePermit)) {
            return false;
        }
        sendFileExceedMaxSizeMessage(null, (long) iMaxVideoTypeSizePermit);
        return true;
    }

    private static boolean checkOtherTypeFile(FileInfo fileInfo) {
        long fileTotalSize = fileInfo.getTotalSize();
        int iMaxDefaultSizePermit = getOtherTypeMaxSize();
        MLog.i("RcsTransaction FileTrans: ", "checkOtherTypeFile iMaxDefaultSizePermit = " + iMaxDefaultSizePermit);
        MLog.i("RcsTransaction FileTrans: ", " fileTotalSize = " + fileTotalSize);
        if (fileTotalSize <= ((long) iMaxDefaultSizePermit)) {
            return false;
        }
        sendFileExceedMaxSizeMessage(fileInfo, (long) iMaxDefaultSizePermit);
        return true;
    }

    private static void sendFileExceedMaxSizeMessage(FileInfo fileInfo, long maxSize) {
        Message msg = handler.obtainMessage();
        Bundle data = new Bundle();
        data.putInt("key", 1);
        if (fileInfo != null) {
            data.putString("filename", fileInfo.getFileDisplayName());
        }
        data.putString("maxFileSizeLimit", Formatter.formatFileSize(mContext, (long) getMaxFileSizePermitedValue()));
        msg.setData(data);
        handler.sendMessage(msg);
    }

    private static boolean isImageExceedResolution(FileInfo fileInfo) {
        return false;
    }

    private static void showFileExceedSizePermitedDialog(String fileName, String maxFileSizeLimit) {
        AlertDialog alertDialog = new Builder(mContext).setTitle(R.string.max_file_size).setMessage(mContext.getString(R.string.send_failed) + ", " + String.format(mContext.getString(R.string.send_file_exceed_max_size), new Object[]{maxFileSizeLimit})).setPositiveButton(R.string.yes, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private static int getImageTypeMaxSize() {
        int size = getCustMaxFileSize("cust_image_ft_max_size");
        if (size != 0) {
            return size;
        }
        return 10485760;
    }

    private static int getVideoTypeMaxSize() {
        int size = getCustMaxFileSize("cust_video_ft_max_size");
        if (size != 0) {
            return size;
        }
        return 524288000;
    }

    private static int getOtherTypeMaxSize() {
        int size = getCustMaxFileSize("cust_other_ft_max_size");
        if (size != 0) {
            return size;
        }
        return 104857600;
    }

    public static boolean getCMCCCustStatus() {
        return isCustFileSize();
    }

    public static int getWarFileSizePermitedValue() {
        int iWarFileSizePermit = 0;
        if (RcsProfile.getRcsService() == null) {
            return 0;
        }
        try {
            iWarFileSizePermit = RcsProfile.getRcsService().getWarFileSizePermited();
            MLog.i("RcsTransaction FileTrans: ", " iWarFileSizePermit : " + iWarFileSizePermit);
            return iWarFileSizePermit;
        } catch (RemoteException e) {
            MLog.e("RcsTransaction FileTrans: ", "Can not get the iWarFileSizePermit from the rcsService");
            return iWarFileSizePermit;
        }
    }

    public static int getMaxFileSizePermitedValue() {
        int iMaxFileSizePermit = 10485760;
        if (RcsProfile.getRcsService() != null) {
            try {
                iMaxFileSizePermit = RcsProfile.getRcsService().getMaxFileSizePermited();
            } catch (Exception e) {
                MLog.e("RcsTransaction FileTrans: ", "Can not get the iMaxFileSizePermit from the rcsService");
            }
        }
        return iMaxFileSizePermit;
    }

    public static void handleImageFileTransfer(Context context, long threadId, List<Uri> uriList, List<String> addrList) {
        int requestCode = 0;
        if (context instanceof ComposeMessageActivity) {
            requestCode = 150222;
        } else if (context instanceof RcsGroupChatComposeMessageActivity) {
            requestCode = 150222;
        }
        handleImageFileTransfer(context, threadId, uriList, addrList, null, requestCode);
    }

    public static void handleImageFileTransfer(Context context, long threadId, List<Uri> uriList, List<String> addrList, String groupId) {
        int requestCode = 0;
        if (context instanceof ComposeMessageActivity) {
            requestCode = 150222;
        } else if (context instanceof RcsGroupChatComposeMessageActivity) {
            requestCode = 150222;
        }
        handleImageFileTransfer(context, threadId, uriList, addrList, groupId, requestCode);
    }

    public static void handleImageFileTransfer(Context context, long threadId, List<Uri> uriList, List<String> addrList, String groupId, int requestCode) {
        List<String> imgSrcList = new ArrayList();
        boolean[] preference = checkMediaResolution(context, uriList, imgSrcList, new boolean[8]);
        if (!preference[5] ? preference[3] : true) {
            preference = RcsProfileUtils.getRcsCropImageStatus(context, preference);
            preference[1] = checkMediaFileSize(context, uriList);
            MLog.i("RcsTransaction FileTrans: ", "handleImageFileTransfer ,showCompressActivity");
            showCompressActivity(context, groupId, threadId, uriList, addrList, preference, imgSrcList, requestCode);
            return;
        }
        MLog.i("RcsTransaction FileTrans: ", "handleImageFileTransfer ,but don't need compress ");
        Context finalContext = context;
        if (150 == requestCode) {
            finalContext = context.getApplicationContext();
        }
        multiSend(finalContext, Long.valueOf(threadId), uriList, addrList, 121, groupId);
    }

    public static void rejectFile(final long msgId, final long chatType) {
        new Thread(new Runnable() {
            public void run() {
                IfMsgplus aMsgPlus = RcsProfile.getRcsService();
                if (aMsgPlus == null) {
                    MLog.e("RcsTransaction FileTrans: ", " RejectFile  error ,because rcsservice is null.");
                    return;
                }
                try {
                    MLog.i("RcsTransaction FileTrans: ", " RejectFile  msgId." + msgId);
                    if (aMsgPlus.rejectFile(msgId, chatType) != 0) {
                        aMsgPlus.rejectFile(msgId, chatType);
                    }
                } catch (RemoteException e) {
                    MLog.i("RcsTransaction FileTrans: ", " RejectFile  error .RemoteException ");
                }
            }
        }).start();
    }

    public static void acceptfile(final long msgId, final long chatType) {
        new Thread(new Runnable() {
            public void run() {
                IfMsgplus aMsgPlus = RcsProfile.getRcsService();
                if (aMsgPlus == null) {
                    MLog.e("RcsTransaction FileTrans: ", " acceptfile  error ,because rcsservice is null.");
                    return;
                }
                try {
                    MLog.i("RcsTransaction FileTrans: ", " Accept File msgId " + msgId);
                    aMsgPlus.receiveFile(msgId, chatType);
                } catch (RemoteException e) {
                    MLog.i("RcsTransaction FileTrans: ", " acceptfile  error .RemoteException ");
                }
            }
        }).start();
    }

    public static long getTotalInternalMemorySize(Context mContext) {
        String path = "";
        for (StorageVolume storageVolume : ((StorageManager) mContext.getSystemService("storage")).getVolumeList()) {
            if (!storageVolume.isRemovable() && storageVolume.isEmulated()) {
                path = storageVolume.getPath();
            }
        }
        StatFs stat = new StatFs(path);
        return ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
    }

    public static boolean checksize(long mFilesize, Context mContext) {
        long filesize = mFilesize;
        long internalMemorySize = getTotalInternalMemorySize(mContext);
        long mObligateStore = 1048576 * ((long) getFtXmlConfig("ObligateStoreSize"));
        MLog.i("RcsTransaction FileTrans: ", "checksize internalMemorySize : " + internalMemorySize + "filesize : " + mFilesize + ": mObligateStore :" + mObligateStore);
        if (internalMemorySize - mFilesize < mObligateStore) {
            return true;
        }
        return false;
    }

    public static void cancelFT(long msgID, boolean isOutGoing, long chatType) {
        final long j = msgID;
        final boolean z = isOutGoing;
        final long j2 = chatType;
        new Thread(new Runnable() {
            public void run() {
                IfMsgplus aMsgPlus = RcsProfile.getRcsService();
                if (aMsgPlus != null) {
                    try {
                        if (aMsgPlus.cancelFile(j, z, j2) != 0) {
                            MLog.i("RcsTransaction FileTrans: ", "cancefile failed! msgID is" + j);
                        } else {
                            MLog.i("RcsTransaction FileTrans: ", "cancefile success! msgID is" + j);
                        }
                    } catch (RemoteException e) {
                        MLog.e("RcsTransaction FileTrans: ", "cancelFile error");
                    }
                }
            }
        }).start();
    }

    public static boolean getFTCapabilityByNumber(String number) {
        if (RcsProfile.getRcsService() == null) {
            return false;
        }
        try {
            Capabilities ca = RcsProfile.getRcsService().getContactCapabilities(number);
            if (ca != null) {
                return ca.isFileTransferSupported();
            }
            return false;
        } catch (RemoteException e) {
            MLog.e("RcsTransaction", "remote error");
            return false;
        }
    }

    public static boolean getLSCapabilityByNumber(String number) {
        if (RcsProfile.getRcsService() == null) {
            return false;
        }
        try {
            Capabilities ca = RcsProfile.getRcsService().getContactCapabilities(number);
            if (ca != null) {
                return ca.isLocationSharingSupported();
            }
            return false;
        } catch (RemoteException e) {
            MLog.e("RcsTransaction", "remote error");
            return false;
        }
    }

    public static boolean isFTOfflineSendAvailable(String number) {
        boolean isAvailable = false;
        if (isSupportFtOutDate()) {
            isAvailable = RcsProfile.isImAvailable(number);
        } else {
            try {
                if (RcsProfile.getRcsService() != null) {
                    isAvailable = RcsProfile.getRcsService().isFtAvailable(number);
                }
            } catch (Exception e) {
                MLog.e("RcsTransaction", "needStoreNotification error");
            }
        }
        MLog.i("RcsTransaction FileTrans: ", " isFTOfflineSendAvailable  isAvailable  " + isAvailable);
        return isAvailable;
    }

    public static boolean isFTOfflineSendNotifyNeeded(String number) {
        boolean isNotifyNeeded = false;
        try {
            if (RcsProfile.getRcsService() != null) {
                isNotifyNeeded = RcsProfile.getRcsService().needStoreNotification(number);
            }
        } catch (Exception e) {
            MLog.e("RcsTransaction", "needStoreNotification error");
        }
        return isNotifyNeeded;
    }

    public static boolean isFTOfflineSendNotifyNeeded(List<String> list) {
        for (String number : list) {
            if (isFTOfflineSendNotifyNeeded(number)) {
                return true;
            }
        }
        return false;
    }

    public static String getPath(Context context, Uri uri) {
        boolean isKitKat;
        if (VERSION.SDK_INT >= 19) {
            isKitKat = true;
        } else {
            isKitKat = false;
        }
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            String[] split;
            if (isExternalStorageDocument(uri)) {
                split = DocumentsContract.getDocumentId(uri).split(":");
                return "/storage/" + split[0] + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                return getDataColumn(context, ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(DocumentsContract.getDocumentId(uri))), null, null);
            } else if (isMediaDocument(uri)) {
                Uri contentUri = null;
                String type = DocumentsContract.getDocumentId(uri).split(":")[0];
                if ("image".equals(type)) {
                    contentUri = Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                return getDataColumn(context, contentUri, "_id=?", new String[]{split[1]});
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else {
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        try {
            cursor = SqliteWrapper.query(context, uri, new String[]{"_data"}, selection, selectionArgs, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String string = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
            return string;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static int getValidityTime() {
        int ValidityTime = 0;
        try {
            if (RcsProfile.getRcsService() != null) {
                ValidityTime = RcsProfile.getRcsService().getMsgCapValidityTime();
            }
        } catch (Exception e) {
            MLog.d("RcsTransaction", "getValidityTime exception = " + e.toString());
        }
        return ValidityTime;
    }

    public static int getAlreadyTime(String number) {
        int validityTime = getValidityTime();
        MLog.d("RcsTransaction", "custMms:System.currentTimeMillis()= " + System.currentTimeMillis() + ",validityTime=" + validityTime);
        if (sCapReqTimeMap.get(number) != null) {
            return (int) ((System.currentTimeMillis() - ((Long) sCapReqTimeMap.get(number)).longValue()) / 1000);
        }
        return validityTime;
    }

    public static void checkValidityTimeAndSendCapRequest(String number) {
        int mMsgCapValidityTime = 0;
        if (RcsProfile.getRcsService() == null || number == null || number.isEmpty()) {
            Log.d("RcsTransaction", "enter function [checkValidityTimeAndSendCapRequest], input param error, return");
            return;
        }
        String isSupCapaValidity = RcsXmlParser.getValueByNameFromXml("support_capability_validity");
        MLog.d("RcsTransaction", "enter function [checkValidityTimeAndSendCapRequest], isSupCapaValidity is [" + isSupCapaValidity + "]");
        if (TextUtils.isEmpty(isSupCapaValidity) || !"true".equals(isSupCapaValidity)) {
            try {
                mMsgCapValidityTime = RcsProfile.getRcsService().getMsgCapValidityTime();
                Log.d("RcsTransaction", "enter function [checkValidityTimeAndSendCapRequest], get the Validity Time from RcsService, value is [" + mMsgCapValidityTime + "]");
            } catch (RemoteException e) {
                MLog.e("RcsTransaction", "getMsgCapValidityTime error");
            }
            if (sCapReqTimeMap.containsKey(number)) {
                Log.d("RcsTransaction", "enter function [checkValidityTimeAndSendCapRequest], time gone [" + ((System.currentTimeMillis() - ((Long) sCapReqTimeMap.get(number)).longValue()) / 1000) + "s]");
                if ((System.currentTimeMillis() - ((Long) sCapReqTimeMap.get(number)).longValue()) / 1000 < ((long) mMsgCapValidityTime)) {
                    Log.d("RcsTransaction", "enter function [checkValidityTimeAndSendCapRequest], time not reach [" + mMsgCapValidityTime + "], do not send the capability request");
                    return;
                }
            }
            try {
                if (RcsProfile.getRcsService().requestContactCapabilities(number) == 0) {
                    Long lLastRequestTime = Long.valueOf(System.currentTimeMillis());
                    if (sCapReqTimeMap.containsKey(number)) {
                        sCapReqTimeMap.remove(number);
                    }
                    sCapReqTimeMap.put(number, lLastRequestTime);
                    Log.d("RcsTransaction", "enter function [checkValidityTimeAndSendCapRequest], sCapReqTimeMap add number [xxxxx], add time [" + lLastRequestTime + "]");
                }
            } catch (RemoteException e2) {
                MLog.w("RcsTransaction", "RcsService Remote exception");
            } catch (Exception e3) {
                MLog.e("RcsTransaction", "requestContactCapabilities error");
            }
            return;
        }
        try {
            if (RcsProfile.getRcsService().requestContactCapabilities(number) != 0) {
                MLog.d("RcsTransaction", "enter function [checkValidityTimeAndSendCapRequest], isSupCapaValidity is true, send the request failed");
            }
        } catch (RemoteException e4) {
            MLog.d("RcsTransaction", "enter function [checkValidityTimeAndSendCapRequest], isSupCapaValidity is true, send the request error");
        } catch (Exception e5) {
            MLog.d("RcsTransaction", "enter function [checkValidityTimeAndSendCapRequest], isSupCapaValidity is true, send the request error");
        } catch (Throwable th) {
        }
    }

    public static boolean isAnyImageFile(Context context, List<Uri> uriList) {
        for (Uri uri : uriList) {
            if (isMediaFileNeedCompress(getFileInfoByData(context, uri))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVCalendarFile(Intent intent) {
        if (!(intent == null || intent.getExtras() == null)) {
            String mimeTypeValue = intent.getExtras().getString("mimeType");
            if (mimeTypeValue != null && "text/x-vCalendar".equalsIgnoreCase(mimeTypeValue)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNeedToSaveFile(Uri uri) {
        if (uri == null || !uri.toString().contains("file:///data/data")) {
            return false;
        }
        return true;
    }

    public static Uri saveVCalendarAsLocalFile(Uri uri, Context context) {
        return changeToLocalUri(uri, Uri.fromFile(getOutputVcalendarFile()), context);
    }

    public static List<Uri> isVCardFile(Context context, List<Uri> uriList, Intent intent) {
        if (uriList == null) {
            MLog.i("RcsTransaction", "isVCardFile: uriList is null");
            return null;
        }
        for (Uri uri : uriList) {
            if (!isVCardFile(uri, intent)) {
                if (isVCardFileWithLookupUri(uri, intent)) {
                }
            }
            return uriList;
        }
        return null;
    }

    public static boolean isVCardFileWithLookupUri(Uri uri, Intent intent) {
        if (uri == null || !uri.toSafeString().startsWith(Contacts.CONTENT_LOOKUP_URI.toSafeString())) {
            return false;
        }
        MLog.d("RcsTransaction", "isVCardFileWithLookupUri -> match uri. uri = " + uri.toSafeString());
        return true;
    }

    public static boolean isVCardFile(Uri uri, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String mimeType = bundle.getString("mimeType");
            if (!(uri == null || mimeType == null || !"text/x-vCard".equalsIgnoreCase(mimeType))) {
                return true;
            }
        }
        return false;
    }

    private static Uri changeToLocalUri(Uri inputUri, Uri outputuri, Context context) {
        byte[] buf = new byte[ViewPartId.PART_BODY_SIMPLE_CALL_NUMBER];
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(inputUri);
            outputStream = context.getContentResolver().openOutputStream(outputuri);
            if (inputStream == null || outputStream == null) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e("RcsTransaction", "addVcard IOException", e);
                        return inputUri;
                    }
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                return inputUri;
            }
            while (true) {
                int nRead = inputStream.read(buf);
                if (nRead < 0) {
                    break;
                }
                outputStream.write(buf, 0, nRead);
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    Log.e("RcsTransaction", "addVcard IOException", e2);
                    return inputUri;
                }
            }
            if (outputStream != null) {
                outputStream.close();
            }
            return outputuri;
        } catch (FileNotFoundException e3) {
            Log.e("RcsTransaction", "addVcard FileNotFoundException" + e3.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e22) {
                    Log.e("RcsTransaction", "addVcard IOException", e22);
                    return inputUri;
                }
            }
            if (outputStream != null) {
                outputStream.close();
            }
            return inputUri;
        } catch (IOException e222) {
            Log.e("RcsTransaction", "addVcard IOException", e222);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2222) {
                    Log.e("RcsTransaction", "addVcard IOException", e2222);
                    return inputUri;
                }
            }
            if (outputStream != null) {
                outputStream.close();
            }
            return inputUri;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e22222) {
                    Log.e("RcsTransaction", "addVcard IOException", e22222);
                    return inputUri;
                }
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public static Uri handleVcardData(Uri uri, Context context) {
        Uri outputUri = null;
        if (uri == null) {
            MLog.i("RcsTransaction", "handleVcardData: uri is null");
            return null;
        }
        Intent data = new Intent();
        if (isVCardFileWithLookupUri(uri, data)) {
            data.setData(uri);
            if (RcsUtility.handleAddVCard(context.getApplicationContext(), data, "vcard_temp.vcf")) {
                outputUri = handleSingleVcardData(Uri.fromFile(context.getFileStreamPath("vcard_temp.vcf")), context);
            }
        } else {
            outputUri = handleSingleVcardData(uri, context);
        }
        if (outputUri != null) {
            return outputUri;
        }
        return null;
    }

    public static Uri handleSingleVcardData(Uri uri, Context context) {
        File outputFile = getOutputVcardFile();
        if (outputFile != null) {
            return changeToLocalUri(uri, Uri.fromFile(outputFile), context);
        }
        MLog.w("RcsTransaction", "handleSingleVcardData: getOutputVcardFile failed");
        return null;
    }

    public static File getOutputVcardFile() {
        File vcardStorageDir = new File("/storage/emulated/0/RCS/", ".sendVcard");
        if (vcardStorageDir.exists() || vcardStorageDir.mkdirs()) {
            return new File(vcardStorageDir.getPath() + File.separator + "Vcard_" + (System.currentTimeMillis() + "") + ".vcf");
        }
        Log.d("RcsTransaction", "failed to create directory");
        return null;
    }

    public static File getOutputVcalendarFile() {
        File vcalendarStorageDir = new File("/storage/emulated/0/RCS/", ".sendVCalendar");
        if (vcalendarStorageDir.exists() || vcalendarStorageDir.mkdirs()) {
            return new File(vcalendarStorageDir.getPath() + File.separator + "VCalendar_" + (System.currentTimeMillis() + "") + ".vcs");
        }
        Log.d("RcsTransaction", "failed to create directory");
        return null;
    }

    public static void requesetCapabilitybeforeGroupChat(List<PeerInformation> members) {
        if (members != null && !members.isEmpty()) {
            Log.d("RcsTransaction", "CreateGroupChat and members.size() = " + members.size() + "");
            for (int i = 0; i < members.size(); i++) {
                checkValidityTimeAndSendCapRequest(((PeerInformation) members.get(i)).getNumber());
            }
        }
    }

    public static ProgressDialog getProgressDialog() {
        return pDialog;
    }

    public static void sendCompressedVideo(Context context, Object threadIdObject, List<Uri> uriList, List<String> addrList, int request, String groupId) {
        FileInfo info = getFileInfoByData(context, uriList.get(0));
        if (info != null && info.getSendFilePath() != null) {
            String filePath = info.getSendFilePath();
            if (!filePath.endsWith(".mp4")) {
                Toast.makeText(context, context.getString(R.string.text_compress_format_not_support), 0).show();
                multiSend(context, threadIdObject, uriList, addrList, request, groupId);
            } else if (RcsUtility.resolutionVerity4Video(filePath)) {
                if (RcseCompressUtil.isCompressRunning()) {
                    RcseCompressUtil.cancelVideoCompress();
                }
                final ProgressDialog mDialog = new ProgressDialog(context);
                mDialog.setMax(100);
                mDialog.setTitle(context.getString(R.string.text_compress_started_in_progress));
                mDialog.setProgress(0);
                mDialog.setProgressStyle(1);
                mDialog.setProgressNumberFormat(null);
                mDialog.setCancelable(false);
                mDialog.setButton(context.getString(R.string.nickname_dialog_cancel), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RcseCompressUtil.cancelVideoCompress();
                    }
                });
                pDialog = mDialog;
                final Context context2 = context;
                final Object obj = threadIdObject;
                final List<String> list = addrList;
                final int i = request;
                final String str = groupId;
                RcseCompressUtil.call4ffmpeg(new Handler() {
                    private void dismissDialog() {
                        if (mDialog != null && mDialog.isShowing()) {
                            Activity activity = (Activity) Activity.class.cast(context2);
                            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                                mDialog.dismiss();
                            }
                        }
                    }

                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case -1:
                                Log.e("RcsTransaction", "Conversion failed! " + msg.obj);
                                dismissDialog();
                                ((Activity) context2).setProgressBarIndeterminateVisibility(false);
                                Toast.makeText(context2, context2.getString(R.string.text_compress_failed), 0).show();
                                return;
                            case 0:
                                Log.d("RcsTransaction", "Conversion started!");
                                mDialog.show();
                                ((Activity) context2).setProgressBarIndeterminateVisibility(true);
                                return;
                            case 1:
                                mDialog.setProgress(msg.arg1);
                                return;
                            case 2:
                                Log.d("RcsTransaction", "Conversion ended!");
                                dismissDialog();
                                String filePath = msg.obj;
                                List<String> dataList = new ArrayList();
                                dataList.add(filePath);
                                ((Activity) context2).setProgressBarIndeterminateVisibility(false);
                                if (!RcseCompressUtil.isCancelled()) {
                                    RcsTransaction.multiSend(context2, obj, dataList, list, i, str);
                                    return;
                                }
                                return;
                            default:
                                return;
                        }
                    }
                }, filePath, context);
            } else {
                multiSend(context, threadIdObject, uriList, addrList, request, groupId);
            }
        }
    }

    public static void sendFtReadReport(final long msgID, final long chatTye) {
        MLog.i("RcsTransaction FileTrans: ", " sendFtReadReport= " + msgID + " chatType = " + chatTye);
        new Thread(new Runnable() {
            public void run() {
                IfMsgplus mMsgplusService = RcsProfile.getRcsService();
                if (mMsgplusService != null) {
                    try {
                        mMsgplusService.sendFtReadReport(msgID, chatTye);
                    } catch (RemoteException e) {
                        MLog.e("RcsTransaction", "Remote error");
                    }
                }
            }
        }).start();
    }

    private static int getFtXmlConfig(String DmConfig) {
        if (!mIsRcsOn) {
            return 0;
        }
        int value;
        String valueXml = "";
        valueXml = RcsXmlParser.getValueByNameFromXml(DmConfig);
        if (valueXml.isEmpty()) {
            value = 16;
        } else {
            value = Integer.parseInt(valueXml);
        }
        return value;
    }

    private static int getCustMaxFileSize(String fileType) {
        if (!mIsRcsOn) {
            return 0;
        }
        String valueXml = "";
        int value = 104857600;
        valueXml = RcsXmlParser.getValueByNameFromXml(fileType);
        if (!valueXml.isEmpty()) {
            value = Integer.parseInt(valueXml) * Place.TYPE_SUBLOCALITY_LEVEL_2;
        } else if ("IMAGE".equalsIgnoreCase(fileType)) {
            value = 10485760;
        } else if ("VIDEO".equalsIgnoreCase(fileType)) {
            value = 524288000;
        }
        MLog.i("RcsTransaction FileTrans: ", "getCustFileSize fileType = " + fileType + ", value = " + value);
        return value;
    }

    private static boolean isCustFileSize() {
        if (!mIsRcsOn) {
            return false;
        }
        MLog.i("RcsTransaction FileTrans: ", "isCustFileSize value = " + IS_CMCC_RCS_CUST);
        return IS_CMCC_RCS_CUST;
    }

    public static boolean[] checkMediaResolution(Context context, List<Uri> uriList, List<String> showList, boolean[] preference) {
        preference[2] = false;
        preference[3] = false;
        preference[5] = false;
        for (Uri uri : uriList) {
            FileInfo info = getFileInfoByData(context, uri);
            if (info != null) {
                if (info.getMimeType().contains("image")) {
                    preference[5] = true;
                    showList.add(info.getSendFilePath());
                    showList.add("image");
                    return preference;
                } else if (info.getMimeType().contains("video")) {
                    preference[3] = true;
                    showList.add(info.getSendFilePath());
                    showList.add("video");
                    if (checkVideoResolution(context, uri, info)) {
                        preference[2] = true;
                    }
                    return preference;
                }
            }
        }
        return preference;
    }

    public static boolean checkVideoResolution(Context context, Uri uri, FileInfo info) {
        boolean z = false;
        String filePath = info.getSendFilePath();
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(filePath);
            String bitrate = mmr.extractMetadata(20);
            int width = Integer.parseInt(mmr.extractMetadata(18));
            int height = Integer.parseInt(mmr.extractMetadata(19));
            MLog.d("RcsTransaction", " Bitrate : " + bitrate + " width : " + width + " height : " + height);
            if (width > 640 && height > 480) {
                z = true;
            }
            return z;
        } catch (RuntimeException e) {
            MLog.e("RcsTransaction FileTrans: ", "Can't get correct video resolution from filePath : " + filePath);
            return false;
        }
    }

    public static boolean checkMediaFileSize(Context context, List<Uri> uriList) {
        long warnSize = ((long) getWarFileSizePermitedValue()) * 1024;
        for (Uri uri : uriList) {
            FileInfo info = getFileInfoByData(context, uri);
            if (info != null && ((info.getMimeType().contains("image") || info.getMimeType().contains("video")) && info.getTotalSize() > warnSize)) {
                return true;
            }
        }
        return false;
    }

    public static void showCompressActivity(Context context, String groupId, long threadId, List<Uri> uriList, List<String> addrList, boolean[] preference, List<String> imageViewList, int requestCode) {
        Bundle bundle = new Bundle();
        bundle.putLong("threadId", threadId);
        bundle.putString("groupId", groupId);
        if (addrList instanceof ArrayList) {
            bundle.putStringArrayList("addrList", (ArrayList) addrList);
        } else {
            ArrayList<String> aList = new ArrayList();
            for (String address : addrList) {
                aList.add(address);
            }
            bundle.putStringArrayList("addrList", aList);
        }
        if (uriList instanceof ArrayList) {
            bundle.putParcelableArrayList("uriList", (ArrayList) uriList);
        }
        gotoCompress(context, preference, imageViewList, requestCode, bundle);
    }

    private static void gotoCompress(Context context, boolean[] preference, List<String> imageViewList, int requestCode, Bundle bundle) {
        Intent compressIntent = new Intent(context, RcsMediaCompressActivity.class);
        compressIntent.putExtras(bundle);
        compressIntent.putExtra("preference", preference);
        if (imageViewList.size() > 0) {
            compressIntent.putExtra("filePath", (String) imageViewList.get(0));
            compressIntent.putExtra(NumberInfo.TYPE_KEY, (String) imageViewList.get(1));
        }
        if (context instanceof ComposeMessageActivity) {
            MLog.d("RcsTransaction", "showCompressActivity context is ComposeMessageActivity");
            ComposeMessageFragment fragment = (ComposeMessageFragment) FragmentTag.getFragmentByTag((ComposeMessageActivity) context, "Mms_UI_CMF");
            if (fragment != null) {
                fragment.startActivityForResult(compressIntent, requestCode);
                fragment.getRcsComposeMessage().setCompressActivityStart(true);
            }
        } else if (context instanceof RcsGroupChatComposeMessageActivity) {
            MLog.d("RcsTransaction", "showCompressActivity context is RcsGroupChatComposeMessageActivity");
            RcsGroupChatComposeMessageFragment groupChatComposeMessage = (RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag((RcsGroupChatComposeMessageActivity) context, "Mms_UI_GCCMF");
            if (groupChatComposeMessage != null) {
                groupChatComposeMessage.startActivityForResult(compressIntent, requestCode);
            }
        } else if (context instanceof FavoritesActivity) {
            MLog.d("RcsTransaction", "showCompressActivity context is FavoritesActivity");
            FavoritesFragment fragment2 = (FavoritesFragment) FragmentTag.getFragmentByTag((FavoritesActivity) context, "Mms_UI_FAV");
            if (fragment2 != null) {
                fragment2.startActivityForResult(compressIntent, requestCode);
            }
        } else {
            MLog.d("RcsTransaction", "showCompressActivity context is null");
        }
    }

    public static int getSmsPort() {
        if (!mIsRcsOn) {
            return 0;
        }
        String valueXml = "";
        int value = 1;
        valueXml = RcsXmlParser.getValueByNameFromXml("sms_port");
        if (!valueXml.isEmpty()) {
            value = Integer.parseInt(valueXml);
        }
        MLog.i("RcsTransaction", "getSmsPort value = " + value);
        return value;
    }

    public static void rcsSendGroupAnyFile(Context context, Uri uri, long threadId, String groupId) {
        String str = null;
        if (uri == null) {
            MLog.w("RcsTransaction", "uri is null, rcsSendGroupAnyFile failed");
        } else if (groupId == null) {
            MLog.w("RcsTransaction", "groupId is null, rcsSendGroupAnyFile failed");
        } else {
            FileInfo info = getFileInfoByData(context, uri);
            String str2 = "RcsTransaction";
            StringBuilder append = new StringBuilder().append("rcsSendGroupAnyFile uri = ").append(uri).append("threadId = ").append(threadId).append("mineType = ");
            if (info != null) {
                str = info.getMimeType();
            }
            MLog.d(str2, append.append(str).append(", groupId = ").append(groupId).toString());
            if (info == null) {
                MLog.i("RcsTransaction", "rcsSendGroupAnyFile info is null so return");
                return;
            }
            boolean isRecordVideo = false;
            RcsGroupChatComposeMessageFragment rcsGroupChatComposeMessageFragment = null;
            if (context != null && (context instanceof RcsGroupChatComposeMessageActivity)) {
                rcsGroupChatComposeMessageFragment = (RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag((RcsGroupChatComposeMessageActivity) context, "Mms_UI_GCCMF");
                if (rcsGroupChatComposeMessageFragment != null) {
                    isRecordVideo = rcsGroupChatComposeMessageFragment.isRecordVideo();
                }
            }
            if (isRecordVideo || !isMediaFileNeedCompress(info)) {
                if (context != null) {
                    preSendGroupFile(context, info, threadId, groupId);
                }
                if (rcsGroupChatComposeMessageFragment != null && isRecordVideo) {
                    rcsGroupChatComposeMessageFragment.setIsRecordVideo(false);
                }
            } else {
                List<Uri> imgListUri = new ArrayList();
                imgListUri.add(uri);
                List<String> addList = new ArrayList();
                addList.add("");
                if (context != null) {
                    handleImageFileTransfer(context, threadId, imgListUri, addList, groupId);
                }
            }
        }
    }

    public static void rcsSendGroupAnyFile(Context context, List<Uri> uriList, long threadId, String groupId) {
        if (uriList == null) {
            MLog.w("RcsTransaction", "uri is null, rcsSendGroupAnyFile failed");
        } else if (groupId == null) {
            MLog.w("RcsTransaction", "groupId is null, rcsSendGroupAnyFile failed");
        } else {
            List<String> addList = new ArrayList();
            addList.add("");
            handleImageFileTransfer(context, threadId, uriList, addList, groupId);
        }
    }

    public static boolean isMediaFileNeedCompress(FileInfo info) {
        if (info == null || info.getMimeType() == null || (!info.getMimeType().startsWith("image") && !info.getMimeType().startsWith("video"))) {
            return false;
        }
        return true;
    }

    public static void preSendGroupFile(Context context, FileInfo info, long threadId, String groupId) {
        MLog.i("RcsTransaction", "preSendGroupFile groupId = " + groupId);
        Bundle bundle = RcseTelephonyExt.createBundleValues(threadId, info.getSendFilePath(), info.getFileDisplayName(), info.getTotalSize(), info.getMimeType());
        if (checkFileSize(context, info)) {
            MLog.w("RcsTransaction", "preSendGroupFile file size not support.");
            return;
        }
        if (PreferenceUtils.isCancelSendEnable(context)) {
            DelaySendManager.getInst().addDelayMsg(addGroupFileToDbBeforeSend(bundle, threadId, context), "rcs_group_file", false);
        } else {
            sendGroupFileFinal(groupId, bundle, 0);
        }
    }

    private static long addGroupFileToDbBeforeSend(Bundle bundle, long threadId, Context context) {
        String filePath = bundle.getString("file_content", "");
        ContentValues values = new ContentValues();
        values.put("thread_id", Long.valueOf(threadId));
        values.put(NumberInfo.TYPE_KEY, Integer.valueOf(100));
        values.put("date", Long.valueOf(System.currentTimeMillis()));
        values.put("read", Integer.valueOf(1));
        values.put("file_mode", Integer.valueOf(1));
        values.put("body", filePath);
        values.put("seen", Integer.valueOf(0));
        values.put("file_type", Integer.valueOf(RcsUtility.getFileType(filePath, 2, true)));
        values.put("sdk_rcs_group_message_id", Integer.valueOf(0));
        values.put("status", Integer.valueOf(Place.TYPE_ROOM));
        long retVal = insertRcsDelayMsg(values, "rcs_group_text", context);
        if (retVal > 0) {
            values.clear();
            values.put("msg_id", Long.valueOf(retVal));
            values.put("file_type", bundle.getString("file_type"));
            values.put("thread_id", Long.valueOf(bundle.getLong("thread_id")));
            values.put("file_name", bundle.getString("file_name"));
            values.put("file_content", bundle.getString("file_content"));
            values.put("trans_size", Long.valueOf(bundle.getLong("trans_size")));
            values.put("file_size", Long.valueOf(bundle.getLong("file_size")));
            values.put("date", Long.valueOf(System.currentTimeMillis()));
            values.put("transfer_status", Integer.valueOf(Place.TYPE_ROOM));
            values.put("chat_type", Integer.valueOf(2));
            SqliteWrapper.insert(context, Uri.parse("content://rcsim/file_trans"), values);
        }
        return retVal;
    }

    public static void sendGroupFileMsgDelay(Context context, long msgId) {
        Bundle data = new Bundle();
        data.putLong("delay_msg_id", msgId);
        new MyAsyncQueryHandler(context, data).startQuery(4, null, RcsGroupChatComposeMessageFragment.sMessageUri, new String[]{"thread_id", "body"}, "_id = " + String.valueOf(msgId), null, null);
    }

    private static void sendGroupFileFinal(String groupId, Bundle bundle, long localId) {
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (aMsgPlus == null) {
            MLog.w("RcsTransaction", "sendGroupFileFinal: aMsgPlus is null");
            return;
        }
        try {
            aMsgPlus.groupSendFileWithLocalId(groupId, bundle, localId);
        } catch (RemoteException e) {
            MLog.e("RcsTransaction", "remote error");
        }
    }

    public static boolean isFileExist(Uri uri) {
        if (uri == null) {
            return false;
        }
        File file = new File(uri.getPath());
        if (!file.exists() || file.length() <= 0) {
            return false;
        }
        return true;
    }

    public static boolean isCursorValid(Cursor cursor) {
        if (!cursor.isClosed() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
            return true;
        }
        MLog.w("RcsTransaction", "Bad cursor.", new RuntimeException());
        return false;
    }

    public static boolean isFileExist(String path) {
        if (TextUtils.isEmpty(path)) {
            MLog.w("RcsTransaction", "isFileExist path isEmpty");
            return false;
        }
        File file = new File(path);
        if (!file.exists() || file.length() <= 0) {
            return false;
        }
        return true;
    }

    public static Uri getOutputMediaFileUri(int type, String path) {
        if (TextUtils.isEmpty(path)) {
            MLog.w("RcsTransaction", "getOutputMediaFileUri path isEmpty");
            return null;
        }
        File videoFile = getOutputMediaFile(type, path);
        if (videoFile == null) {
            return null;
        }
        return RcsProfileUtils.getVideoContentUri(MmsApp.getApplication().getApplicationContext(), videoFile);
    }

    public static File getOutputMediaFile(int type, String path) {
        if (TextUtils.isEmpty(path)) {
            MLog.w("RcsTransaction", "getOutputMediaFile path isEmpty");
            return null;
        }
        File mediaStorageDir = new File(path);
        if (mediaStorageDir.exists() || mediaStorageDir.mkdirs()) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + System.currentTimeMillis();
            File file = null;
            if (1 == type) {
                file = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            } else if (2 == type) {
                file = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
            }
            return file;
        }
        MLog.w("RcsTransaction", "failed to create directory");
        return null;
    }

    public static void exitGroupChatBeforeDeleteConversation(Collection<String> groupChatIds) {
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (!(aMsgPlus == null || groupChatIds == null || groupChatIds.isEmpty())) {
            try {
                for (String groupId : groupChatIds) {
                    aMsgPlus.exitGroup(groupId.substring(1, groupId.length() - 1), false);
                }
            } catch (RemoteException e) {
                MLog.e("RcsTransaction", "exitGroupChatBeforeDeleteConversation remote error");
            }
        }
    }

    public static void resendMessageFile(long msgId, long chatType, Context context, String globalTransId) {
        if (context == null) {
            MLog.d("RcsTransaction", "resendMessageFile() context is null");
            return;
        }
        MLog.i("RcsTransaction FileTrans: ", "resendMessageFile() msgId =   " + msgId + "  globalTransId =  " + globalTransId);
        if (TextUtils.isEmpty(globalTransId)) {
            resendDelayFtMessageWithMsgId(msgId, chatType, context);
            MLog.d("RcsTransaction", "resendMessageFile(),is delay message");
        } else {
            preResendMessageFile(msgId, chatType, context);
        }
    }

    private static void resendDelayFtMessageWithMsgId(long msgId, long chatType, Context context) {
        long threadId;
        FileInfo info;
        Throwable th;
        if (context == null) {
            MLog.d("RcsTransaction", "resendDelayFtMessageWithMsgId() context is null");
            return;
        }
        if (isDelaySendOn(context)) {
            if (chatType == 1) {
                DelaySendManager.getInst().addDelayMsg(msgId, "chat", false);
            } else if (chatType == 2) {
                DelaySendManager.getInst().addDelayMsg(msgId, "rcs_group_file", false);
            }
            updateDelayFtDB(context, msgId, chatType);
        } else if (chatType == 1) {
            sendRcsFtWithDelay(context, msgId, "chat");
        } else if (chatType == 2) {
            Cursor cursor = null;
            Cursor groupCursor = null;
            long threadId2 = 0;
            String path = "";
            String groupId = "";
            try {
                Context context2 = context;
                cursor = SqliteWrapper.query(context2, RcsGroupChatComposeMessageFragment.sMessageUri, new String[]{"thread_id", "body"}, "_id = ?", new String[]{String.valueOf(msgId)}, null);
                if (cursor != null && cursor.getCount() == 1) {
                    cursor.moveToFirst();
                    threadId = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"));
                    try {
                        path = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                        threadId2 = threadId;
                    } catch (IllegalArgumentException e) {
                        try {
                            MLog.w("RcsTransaction", "MyAsyncQueryHandler db error");
                            if (cursor != null) {
                                cursor.close();
                            }
                            if (groupCursor != null) {
                                groupCursor.close();
                            }
                            info = getFileInfoByData(context, path);
                            sendGroupFileFinal(groupId, RcseTelephonyExt.createBundleValues(threadId, info.getSendFilePath(), info.getFileDisplayName(), info.getTotalSize(), info.getMimeType()), msgId);
                        } catch (Throwable th2) {
                            th = th2;
                            if (cursor != null) {
                                cursor.close();
                            }
                            if (groupCursor != null) {
                                groupCursor.close();
                            }
                            throw th;
                        }
                    }
                }
                context2 = context;
                groupCursor = SqliteWrapper.query(context2, RcsGroupChatComposeMessageFragment.sGroupUri, new String[]{"name"}, "thread_id = ?", new String[]{String.valueOf(threadId2)}, null);
                if (groupCursor != null && groupCursor.getCount() == 1) {
                    groupCursor.moveToFirst();
                    groupId = groupCursor.getString(groupCursor.getColumnIndexOrThrow("name"));
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (groupCursor != null) {
                    groupCursor.close();
                }
                threadId = threadId2;
            } catch (IllegalArgumentException e2) {
                threadId = threadId2;
                MLog.w("RcsTransaction", "MyAsyncQueryHandler db error");
                if (cursor != null) {
                    cursor.close();
                }
                if (groupCursor != null) {
                    groupCursor.close();
                }
                info = getFileInfoByData(context, path);
                sendGroupFileFinal(groupId, RcseTelephonyExt.createBundleValues(threadId, info.getSendFilePath(), info.getFileDisplayName(), info.getTotalSize(), info.getMimeType()), msgId);
            } catch (Throwable th3) {
                th = th3;
                threadId = threadId2;
                if (cursor != null) {
                    cursor.close();
                }
                if (groupCursor != null) {
                    groupCursor.close();
                }
                throw th;
            }
            info = getFileInfoByData(context, path);
            sendGroupFileFinal(groupId, RcseTelephonyExt.createBundleValues(threadId, info.getSendFilePath(), info.getFileDisplayName(), info.getTotalSize(), info.getMimeType()), msgId);
        }
    }

    private static void preResendMessageFile(long msgId, long chatType, Context context) {
        if (context == null) {
            MLog.d("RcsTransaction", "preResendMessageFile() context is null");
            return;
        }
        if (isDelaySendOn(context)) {
            if (chatType == 1) {
                DelaySendManager.getInst().addDelayMsg(msgId, "chat", false);
            } else if (chatType == 2) {
                DelaySendManager.getInst().addDelayMsg(msgId, "rcs_group_file", false);
            }
            updateDelayFtDB(context, msgId, chatType);
        } else {
            resendMessageFileFinal(msgId, chatType);
        }
    }

    private static boolean isDelaySendOn(Context context) {
        return PreferenceUtils.isCancelSendEnable(context);
    }

    private static void resendMessageFileFinal(long msgId, long chatType) {
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (aMsgPlus != null) {
            try {
                aMsgPlus.resendMessageFile(msgId, chatType);
            } catch (RemoteException e) {
                MLog.e("RcsTransaction", "resendMessageFile error");
            }
        }
    }

    private static void updateDelayFtDB(Context context, long msgId, long chatType) {
        if (context == null) {
            MLog.d("RcsTransaction", "updateDelayFtDB() context is null");
            return;
        }
        ContentValues values = new ContentValues();
        long date = System.currentTimeMillis();
        if (chatType == 1) {
            values.put("date", Long.valueOf(date));
            values.put("transfer_status", Integer.valueOf(Place.TYPE_ROOM));
            updateRcsDelayMsg(values, 3, context, msgId);
            values.clear();
            values.put("date", Long.valueOf(date));
            values.put("status", Integer.valueOf(-1));
            values.put(NumberInfo.TYPE_KEY, Integer.valueOf(16));
            updateRcsDelayMsg(values, 1, context, msgId);
        } else if (chatType == 2) {
            values.put("transfer_status", Integer.valueOf(Place.TYPE_ROOM));
            values.put("date", Long.valueOf(date));
            SqliteWrapper.update(context, RcsAttachments.CONTENT_URI, values, "msg_id = ?  AND chat_type = 2", new String[]{String.valueOf(msgId)});
            values.clear();
            values.put("status", Integer.valueOf(-1));
            values.put("date", Long.valueOf(date));
            SqliteWrapper.update(context, RcsGroupChatComposeMessageFragment.sMessageUri, values, "_id = ?", new String[]{String.valueOf(msgId)});
        }
    }

    public static void groupSendLocation(String groupId, double latitude, double longitude, String title, String subTitle) {
        if (groupId == null) {
            MLog.w("RcsTransaction", "groupId is null, rcsSendGroupAnyFile failed");
            return;
        }
        final String str = groupId;
        final double d = latitude;
        final double d2 = longitude;
        final String str2 = title;
        final String str3 = subTitle;
        new Thread(new Runnable() {
            public void run() {
                IfMsgplus aMsgPlus = RcsProfile.getRcsService();
                if (aMsgPlus != null) {
                    try {
                        aMsgPlus.groupSendLocation(str, d, d2, str2, str3);
                    } catch (RemoteException e) {
                        MLog.e("RcsTransaction", "groupSendLocation error");
                    }
                }
            }
        }).start();
    }

    public static void sendLocationSingleChat(double x, double y, String add, String city, String recipient) {
        final String str = recipient;
        final double d = x;
        final double d2 = y;
        final String str2 = city;
        final String str3 = add;
        new Thread(new Runnable() {
            public void run() {
                IfMsgplus aMsgPlus = RcsProfile.getRcsService();
                if (aMsgPlus != null) {
                    try {
                        aMsgPlus.chatSendLocation(str, d, d2, str2, str3);
                    } catch (Exception e) {
                        MLog.e("RcsTransaction", "sendLocation error");
                    }
                }
            }
        }).start();
    }

    public static void sendMassLocation(List<String> members, double x, double y, String add, String city) {
        final List<String> list = members;
        final double d = x;
        final double d2 = y;
        final String str = city;
        final String str2 = add;
        new Thread(new Runnable() {
            public void run() {
                IfMsgplus aMsgPlus = RcsProfile.getRcsService();
                if (aMsgPlus != null) {
                    try {
                        aMsgPlus.sendMassLocation(list, d, d2, str, str2);
                    } catch (Exception e) {
                        MLog.e("RcsTransaction", "sendMassLocation error");
                    }
                }
            }
        }).start();
    }

    public static boolean isShowUndeliveredIcon() {
        if (!mIsRcsOn) {
            return false;
        }
        String valueXml = "";
        boolean value = true;
        valueXml = RcsXmlParser.getValueByNameFromXml("is_show_undelivered_icon");
        if (!valueXml.isEmpty()) {
            value = "true".equalsIgnoreCase(valueXml);
        }
        MLog.i("RcsTransaction", "is_show_undelivered_icon value = " + value);
        return value;
    }

    public static boolean isShowGroupMessageDeliveryReportSetting() {
        if (mIsRcsOn) {
            return RcsXmlParser.getValueByNameFromXml("CONFIG_GROUPMESSAGE_DELIVERY_REPORT_SETTING_SHOW").equals("1");
        }
        return false;
    }

    public static boolean isShowGroupChatAutoAcceptSetting() {
        if (mIsRcsOn) {
            return RcsXmlParser.getValueByNameFromXml("CONFIG_GROUPCHAT_AUTOACCEPT_SETTING_SHOW").equals("1");
        }
        return false;
    }

    public static void showFileSaveResult(Context context, String uri) {
        if (context == null) {
            MLog.w("RcsTransaction", "showFileSaveResult null == context");
            return;
        }
        if (TextUtils.isEmpty(uri)) {
            MLog.i("RcsTransaction", "showFileSaveResult save file failed uri is empty");
            Toast.makeText(context, context.getResources().getString(R.string.save) + context.getResources().getString(R.string.status_failed_Toast), 0).show();
        } else {
            if (uri.contains(CONTAINS_STR)) {
                uri = uri.replace(CONTAINS_STR, "");
            }
            MLog.i("RcsTransaction", "showFileSaveResult uri=" + uri);
            Toast.makeText(context, context.getString(R.string.copy_to_sdcard_success_Toast) + uri, 0).show();
        }
    }

    public static boolean isEnableGroupSilentMode() {
        if (!mIsRcsOn) {
            return false;
        }
        String valueXml = "";
        boolean value = true;
        valueXml = RcsXmlParser.getValueByNameFromXml("is_enable_group_silentmode");
        if (!valueXml.isEmpty()) {
            value = "true".equalsIgnoreCase(valueXml);
        }
        return value;
    }

    public static boolean isSupportNoSimMode() {
        String FIRST_TIME_LOGIN_MODE = "first_time_login_mode";
        String ONCE_AGAIN_LOGIN_MODE = "once_again_login_mode";
        int firstTimeLoginMode = RcsXmlParser.getInt("first_time_login_mode", 3);
        int onceAgainLoginMode = RcsXmlParser.getInt("once_again_login_mode", 7);
        if (firstTimeLoginMode == 3 && onceAgainLoginMode == 3) {
            return false;
        }
        return true;
    }

    public static boolean isShowGroupDetailsStatusIcon() {
        if (!mIsRcsOn) {
            return false;
        }
        String valueXml = "";
        boolean value = false;
        valueXml = RcsXmlParser.getValueByNameFromXml("CONFIG_RCS_GROUP_DETAIL_SHOW_ICON");
        if (!valueXml.isEmpty()) {
            value = "true".equalsIgnoreCase(valueXml);
        }
        MLog.i("RcsTransaction", "CONFIG_RCS_GROUP_DETAIL_SHOW_ICON = " + value);
        return value;
    }

    public static void sendFileForForward(Intent data, Context context) {
        if (data != null) {
            Bundle bdl = data.getExtras();
            if (bdl != null) {
                long threadId = bdl.getLong("threadId");
                List<Uri> uList = bdl.getParcelableArrayList("uriList");
                if (uList == null) {
                    MLog.d("RcsTransaction", " sendFileForForward: uList is null");
                    return;
                }
                List<String> aList = bdl.getStringArrayList("addrList");
                if (data.getBooleanExtra("fullSize", false)) {
                    MLog.d("RcsTransaction", " sendFileForForward original size media file");
                    multiSend(context.getApplicationContext(), Long.valueOf(threadId), uList, aList, 120, null);
                } else {
                    MLog.d("RcsTransaction", " sendFileForForward: send compressed media file");
                    multiSendWithUriResized(context, threadId, uList, aList, 120, null);
                }
            }
        }
    }

    public static void clearDraft(WorkingMessage workingMessage, Context context, boolean clear) {
        if (workingMessage != null) {
            Conversation conversation = workingMessage.getConversation();
            if ((workingMessage.hasMmsDraft() || workingMessage.hasSmsDraft() || conversation.hasDraft()) && clear) {
                Log.d("RcsTransaction", "RcsTransaction send hasDraft, delete Draft");
                if (workingMessage.hasSmsDraft()) {
                    workingMessage.asyncDeleteDraftSmsMessage(conversation);
                }
                if (workingMessage.getRcsWorkingMessage() != null) {
                    Log.d("RcsTransaction", "RcsTransaction send hasDraft, delete Mms Draft");
                    workingMessage.getRcsWorkingMessage().asyncDeleteDraftMmsMessageCust(workingMessage, conversation, context);
                }
                workingMessage.discard();
                workingMessage.setDiscarded(false);
                conversation.setDraftState(false);
            }
        }
    }

    public static void showSafetyDialogForVcard(Context context, final Runnable callback, final Runnable cancleCallback) {
        final SharedPreferences pref = context.getSharedPreferences(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, 0);
        View view = View.inflate(context, R.layout.rcs_ft_send_notice_dialog, null);
        final CheckBox checkbox = (CheckBox) view.findViewById(R.id.vcard_not_ask_me);
        new Builder(context).setTitle(R.string.mms_remind_title).setView(view).setMessage(R.string.rcs_im_send_contacts_note).setPositiveButton(R.string.nickname_dialog_confirm, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (checkbox.isChecked()) {
                    Editor editor = pref.edit();
                    editor.putBoolean("no_need_dialog_for_vcf", true);
                    editor.commit();
                }
                RcsTransaction.excuteSendMixMessageRunnable(callback);
                dialog.dismiss();
            }
        }).setNegativeButton(R.string.nickname_dialog_cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RcsTransaction.excuteSendMixMessageRunnable(cancleCallback);
                dialog.dismiss();
            }
        }).create().show();
    }

    public static boolean isSupportFtOutDate() {
        if (!mIsRcsOn) {
            return false;
        }
        String valueXml = "";
        boolean value = false;
        valueXml = RcsXmlParser.getValueByNameFromXml("is_support_ft_outdate");
        if (!valueXml.isEmpty()) {
            value = "true".equalsIgnoreCase(valueXml);
        }
        MLog.i("RcsTransaction FileTrans: ", "isSupportFtOutDate value = " + value);
        return value;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long getMmsMsgId(long SdkMsgId, int type, Context context) {
        long ret = 0;
        Cursor cursor = null;
        Context context2;
        switch (type) {
            case 1:
                try {
                    context2 = context;
                    cursor = SqliteWrapper.query(context2, Uri.parse("content://rcsim/chat"), new String[]{"_id"}, "sdk_sms_id = ?", new String[]{String.valueOf(SdkMsgId)}, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        ret = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                    }
                } catch (RuntimeException e) {
                    MLog.e("RcsTransaction", "query mms message id error.");
                    if (cursor != null) {
                        cursor.close();
                        break;
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                break;
            case 2:
                context2 = context;
                cursor = SqliteWrapper.query(context2, Uri.parse("content://rcsim/rcs_group_message"), new String[]{"_id"}, "sdk_rcs_group_message_id = ?", new String[]{String.valueOf(SdkMsgId)}, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    ret = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                }
                if (cursor != null) {
                    cursor.close();
                    break;
                }
                break;
        }
        if (cursor != null) {
            cursor.close();
        }
        return ret;
    }

    public static void deleteChatGroup(long uiMsgId, int chatType, Context context) {
        if (chatType == 1) {
            SqliteWrapper.delete(context, ContentUris.withAppendedId(Uri.parse("content://rcsim/chat"), uiMsgId), null, null);
        } else {
            SqliteWrapper.delete(context, ContentUris.withAppendedId(Uri.parse("content://rcsim/rcs_group_message"), uiMsgId), null, null);
        }
    }

    private static void finishActivity(Context context) {
        if (context != null && (context instanceof ComposeMessageActivity)) {
            ComposeMessageFragment fragment = (ComposeMessageFragment) FragmentTag.getFragmentByTag((ComposeMessageActivity) context, "Mms_UI_CMF");
            if (fragment.getRcsComposeMessage() != null && fragment.getRcsComposeMessage().getComposeMessageListAdapter().getCursor() != null && fragment.getRcsComposeMessage().getComposeMessageListAdapter().getCursor().getCount() == 0) {
                fragment.finishSelf(false);
            }
        }
    }

    public static void resendExtMessage(long msgId, String address, Context ctx) {
        if (mIsRcsOn) {
            preResendImMessage(msgId, address, ctx);
        }
    }

    private static void preResendImMessage(long msgId, String address, Context ctx) {
        boolean isDelaySendOn = PreferenceUtils.isCancelSendEnable(ctx);
        updateImDBWithResend(ctx, msgId, isDelaySendOn);
        if (isDelaySendOn) {
            DelaySendManager.getInst().addDelayMsg(msgId, "chat", false);
        } else {
            resendImMessageFinal(msgId, address);
        }
    }

    private static void updateImDBWithResend(Context context, long msgId, boolean isDelay) {
        ContentValues values = new ContentValues();
        values.put("date", Long.valueOf(System.currentTimeMillis()));
        if (isDelay) {
            values.put(NumberInfo.TYPE_KEY, Integer.valueOf(16));
            values.put("status", Integer.valueOf(-1));
        }
        MLog.i("RcsTransaction", "updateImDBWithResend =" + ((long) SqliteWrapper.update(context, Uri.parse("content://rcsim/chat"), values, "_id = ?", new String[]{String.valueOf(msgId)})));
    }

    public static long updateAudioReadStatus(Context context, long mFileTransId) {
        if (!mIsRcsOn) {
            return 0;
        }
        ContentValues values = new ContentValues();
        values.put("transfer_status", Integer.valueOf(Place.TYPE_ROUTE));
        return (long) SqliteWrapper.update(context, Uri.withAppendedPath(RcsAttachments.CONTENT_URI, String.valueOf(mFileTransId)), values, null, null);
    }

    private static void resendImMessageFinal(long msgId, String address) {
        if (mIsRcsOn) {
            IfMsgplus aMsgPlus = RcsProfile.getRcsService();
            if (aMsgPlus != null) {
                try {
                    aMsgPlus.resendMessageIm(msgId, address);
                } catch (RemoteException e) {
                    MLog.e("RcsTransaction", "resendMessageIm error");
                }
            }
        }
    }

    public static void resendLocationMessage(long msgId, String address) {
        if (mIsRcsOn) {
            IfMsgplus aMsgPlus = RcsProfile.getRcsService();
            if (aMsgPlus != null) {
                try {
                    aMsgPlus.resendMessageIm(msgId, address);
                } catch (RemoteException e) {
                    MLog.e("RcsTransaction", "resendMessageIm error");
                }
            }
        }
    }

    private static void updateRcsDelayMsg(ContentValues values, int type, Context context, long msgId) {
        Uri updateUri = null;
        String selection = "";
        if (context == null) {
            MLog.d("RcsTransaction", "updateRcsDelayMsg() context is null");
            return;
        }
        MLog.d("RcsTransaction", "updateRcsDelayMsg() type = " + type);
        switch (type) {
            case 1:
                updateUri = Uri.parse("content://rcsim/chat");
                selection = "_id = " + msgId;
                break;
            case 3:
                updateUri = Uri.parse("content://rcsim/file_trans");
                selection = "msg_id = " + msgId + " AND chat_type = " + 1;
                break;
        }
        if (!(updateUri == null || TextUtils.isEmpty(selection))) {
            SqliteWrapper.update(context, updateUri, values, selection, null);
        }
    }

    private static long insertRcsDelayMsg(ContentValues values, String type, Context context) {
        Uri insertUri = null;
        long retId = 0;
        if (context == null) {
            return 0;
        }
        if (type.equals("chat")) {
            insertUri = Uri.parse("content://rcsim/chat");
        } else if (type.equals("rcs_group_text")) {
            insertUri = Uri.parse("content://rcsim/rcs_group_message");
        } else if ("ft".equals(type)) {
            insertUri = Uri.parse("content://rcsim/file_trans");
        }
        if (insertUri != null) {
            retId = ContentUris.parseId(SqliteWrapper.insert(context, insertUri, values));
        }
        return retId;
    }

    private static int getMaxGroupMessageTextSize() {
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        int iMaxSize = 0;
        String strMaxSize = "";
        if (aMsgPlus != null) {
            try {
                strMaxSize = aMsgPlus.getDmConfig(2);
            } catch (RemoteException e) {
                MLog.e("RcsTransaction", "updateMaxInputSize getDmConfig error");
            }
        }
        try {
            iMaxSize = Integer.parseInt(strMaxSize);
        } catch (NumberFormatException e2) {
            MLog.e("RcsTransaction", "updateMaxInputSize NumberFormatException error");
        }
        if (iMaxSize == 0 || 16 == iMaxSize) {
            iMaxSize = VTMCDataCache.MAXSIZE;
        }
        int mMaxGroupMessageTextSize = iMaxSize;
        return iMaxSize;
    }

    public static boolean isIncallChatting(long threadId) {
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (aMsgPlus != null) {
            try {
                return aMsgPlus.getIncallChatState(threadId);
            } catch (RemoteException e) {
                MLog.e("RcsTransaction", "getLoginState fail");
            }
        }
        return false;
    }
}
