package com.android.rcs.ui;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Telephony.Sms;
import android.telephony.MSimTelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.ui.HwCustMessageUtilsImpl;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.DraftCache;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.RcsMmsConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.MccMncConfig;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcseMmsExt;
import java.io.File;

public class RcsMessageUtils {
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();

    public boolean configRoamingNationalAsLocal() {
        return RcsMmsConfig.getConfigRoamingNationalAsLocal();
    }

    public boolean isRoamingNationalP4(int subscription) {
        String simMccMnc = MSimTelephonyManager.getDefault().getSimOperator(subscription);
        String networkMccMnc = MSimTelephonyManager.getDefault().getNetworkOperator(subscription);
        MLog.i("RcsMessageUtils", "simMccMnc = " + simMccMnc);
        MLog.i("RcsMessageUtils", "networkMccMnc = " + networkMccMnc);
        if (MccMncConfig.isValideOperator(simMccMnc) && MccMncConfig.isValideOperator(networkMccMnc) && simMccMnc.equals(HwCustMessageUtilsImpl.MCCMNC_PLAY) && !simMccMnc.equals(networkMccMnc) && networkMccMnc.startsWith(HwCustMessageUtilsImpl.MCC_POLAND)) {
            return true;
        }
        return false;
    }

    public String addMsgType(Context context, Cursor cursor) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return null;
        }
        if ("chat".equals(cursor.getString(0))) {
            return context.getResources().getString(R.string.im_message);
        }
        return null;
    }

    private static Intent getRecordVideoIntent() {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return null;
        }
        Uri videoUri = RcsTransaction.getOutputMediaFileUri(2, RcsTransaction.RCS_RECORD_VIDEO);
        if (videoUri == null) {
            MLog.e("RcsMessageUtils FileTrans: ", "recordVideo videoUri is null");
            return null;
        }
        Intent intent = new Intent("android.media.action.VIDEO_CAPTURE");
        intent.putExtra("output", videoUri);
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        long maxFileSize = 0;
        if (aMsgPlus != null) {
            try {
                maxFileSize = (long) aMsgPlus.getMaxFileSizePermited();
            } catch (RemoteException e) {
                MLog.e("RcsMessageUtils", "getMaxFileSizePermited error");
            }
        }
        if (maxFileSize <= 0) {
            maxFileSize = 10485760;
        }
        intent.putExtra("android.intent.extra.videoQuality", 4);
        intent.putExtra("android.intent.extra.sizeLimit", maxFileSize);
        intent.putExtra("rcs_video_bit_rate", 1200000);
        MLog.i("RcsMessageUtils FileTrans: ", "recordVideo maxFileSize=" + maxFileSize);
        return intent;
    }

    public static int getAudioFileDuration(Context mContext, File audioFile) {
        if (audioFile == null) {
            return 0;
        }
        Uri audioUri = Uri.fromFile(audioFile);
        if (audioUri == null) {
            return 0;
        }
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(mContext, audioUri);
            player.prepare();
            int duration = player.getDuration();
            player.release();
            return duration;
        } catch (Throwable e) {
            MLog.e("RcsMessageUtils FileTrans: ", "AudioMessage getduration Unexpected IOException.", e);
            player.release();
            return 0;
        }
    }

    public static void recordVideo(Fragment fragment, int requestCode) {
        Intent intent = getRecordVideoIntent();
        if (intent != null) {
            try {
                if (fragment.getActivity().getApplicationContext().checkCallingOrSelfPermission("android.permission.CAMERA") == 0) {
                    fragment.startActivityForResult(intent, requestCode);
                }
            } catch (ActivityNotFoundException e) {
                MessageUtils.shwNoAppDialog(fragment.getActivity());
                MLog.e("RcsMessageUtils FileTrans: ", "MediaRecorder Open Exception " + e);
            } catch (Exception e2) {
                MLog.e("RcsMessageUtils FileTrans: ", "MediaRecorder Open Exception " + e2);
            }
        }
    }

    public CharSequence formatMessage(String body) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return "";
        }
        SpannableStringBuilder buf = new SpannableStringBuilder();
        SmileyParser parser = SmileyParser.getInstance();
        if (!TextUtils.isEmpty(body)) {
            buf.append(parser.addSmileySpans(body, SMILEY_TYPE.MESSAGE_TEXTVIEW));
        }
        return buf;
    }

    public void markOtherAsRead(ContentResolver cr, ContentValues values) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            cr.update(Uri.parse("content://rcsim/chat"), values, "read = 0", null);
            cr.update(Uri.parse("content://rcsim/rcs_group_message"), values, "read = 0", null);
        }
    }

    public boolean isRcsSwitchOn() {
        return this.isRcsOn;
    }

    public MessageItem getMsgItem(MessageListAdapter msgAdapter, Long itemId) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return null;
        }
        RcsMessageListAdapter rcsMessageListAdapter = msgAdapter.getRcsMessageListAdapter();
        if (rcsMessageListAdapter == null) {
            return null;
        }
        return rcsMessageListAdapter.getMessageItemWithIdAssigned(itemId.intValue(), msgAdapter.getCursor());
    }

    public Intent selectMediaByType(Intent innerIntent, String contentType) {
        if (RcsCommonConfig.isRCSSwitchOn() && "video/*".equals(contentType) && RcseMmsExt.isRcsMode()) {
            innerIntent.putExtra("fromRCS", true);
        }
        return innerIntent;
    }

    public static boolean saveDraft(final Context context, final ContactList recipients, final String msgbody, final int subscription) {
        ThreadEx.execute(new Runnable() {
            public void run() {
                Conversation conv = Conversation.get(context, recipients, true);
                long mThreadId = conv.getHwCust().ensureDraftThreadId(conv, context);
                MLog.d("RcsMessageUtils", "saveDraft mThreadId:" + mThreadId);
                ContentValues values = new ContentValues(4);
                values.put("thread_id", Long.valueOf(mThreadId));
                values.put("body", msgbody);
                values.put(NumberInfo.TYPE_KEY, Integer.valueOf(3));
                values.put("sub_id", Integer.valueOf(subscription));
                SqliteWrapper.insert(context, context.getContentResolver(), Sms.CONTENT_URI, values);
                conv.setDraftState(true);
                conv.setHasTempDraft(true);
            }
        });
        return true;
    }

    public static void saveDraftForGroup(Context context, String contents, long threadId, String mGroupID) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return;
        }
        if (TextUtils.isEmpty(mGroupID) || TextUtils.isEmpty(contents)) {
            MLog.d("RcsMessageUtils", "groupId or contents is null, please check");
            return;
        }
        final long j = threadId;
        final Context context2 = context;
        final String str = contents;
        final String str2 = mGroupID;
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                MLog.d("RcsMessageUtils", " saveDraftForGroup threadId =  " + j);
                if (j > 0) {
                    DraftCache.getInstance().setSavingDraft(true);
                    String draftString = RcsMessageUtils.readDraftGroupMessage(context2, j);
                    ContentValues values = new ContentValues();
                    values.put("date", Long.valueOf(System.currentTimeMillis()));
                    values.put("body", str);
                    if (!TextUtils.isEmpty(draftString)) {
                        SqliteWrapper.delete(context2, context2.getContentResolver(), ContentUris.withAppendedId(Uri.parse("content://rcsim/delete_draft_msg_by_threadid"), j), null, null);
                        MLog.d("RcsMessageUtils", "already has draft, should delete old draft");
                    }
                    values.put("thread_id", Long.valueOf(j));
                    values.put(NumberInfo.TYPE_KEY, Integer.valueOf(112));
                    SqliteWrapper.insert(context2, context2.getContentResolver(), Uri.parse("content://rcsim/rcs_group_message"), values);
                    MLog.d("RcsMessageUtils", "insert draft");
                    if (DraftCache.getInstance().getHwCust() != null) {
                        DraftCache.getInstance().getHwCust().setDraftGroupState(RcsMessageUtils.getRcsThreadId(context2, str2), true);
                    }
                    DraftCache.getInstance().setSavingDraft(false);
                }
            }
        });
    }

    private static String readDraftGroupMessage(Context context, long threadId) {
        long thread_id = threadId;
        Cursor cursor = null;
        MLog.d("RcsMessageUtils", " readDraftGroupMessage , the thread_id is " + threadId);
        if (threadId <= 0) {
            return "";
        }
        String body = "";
        try {
            Context context2 = context;
            cursor = SqliteWrapper.query(context2, context.getContentResolver(), Uri.parse("content://rcsim/rcs_group_message"), new String[]{"body"}, "thread_id = ?  AND type = ?", new String[]{String.valueOf(threadId), String.valueOf(112)}, null);
            if (cursor != null && cursor.moveToFirst()) {
                body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException e) {
            MLog.e("RcsMessageUtils", "cursor unknowable error");
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

    private static long getRcsThreadId(Context context, String mGroupID) {
        Cursor cursor = null;
        long mRcsThreadId = 0;
        try {
            Context context2 = context;
            cursor = SqliteWrapper.query(context2, Uri.parse("content://rcsim/rcs_conversations"), new String[]{"_id"}, "recipient_ids = ?", new String[]{mGroupID}, null);
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow("_id");
                if (cursor.moveToFirst()) {
                    mRcsThreadId = cursor.getLong(idColumn);
                    MLog.d("RcsMessageUtils", "getRcsThreadId mRcsThreadId = " + mRcsThreadId);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException e) {
            MLog.e("RcsMessageUtils", "cursor unknowable error");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mRcsThreadId;
    }
}
