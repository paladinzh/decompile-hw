package com.android.rcs.ui;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.RichMessageEditor;
import com.android.mms.util.SmileyParser;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.RcsMmsConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.ui.EditTextWithSmiley;
import com.huawei.rcs.ui.RcsAudioMessage;
import com.huawei.rcs.ui.RcsImageCache;
import com.huawei.rcs.utils.RcsUtility;
import com.huawei.rcs.utils.RcseMmsExt;

public class RcsRichMessageEditor {
    private static Context mCMFContext = null;
    private static long mMsgId = 0;
    private boolean hasDraftWhenFt = false;
    private boolean mRcsLoadDraftFt = false;
    private EditTextWithSmiley mSmsEditorText = null;

    public void setHwCustRichMessageEditor(Context context, RichMessageEditor editor) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
        }
    }

    public void changeHint(EditTextWithSmiley editText) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mSmsEditorText = editText;
            if (RcseMmsExt.isRcsMode()) {
                editText.setHint(R.string.type_to_compose_im_text_enter_to_send_new_rcs);
            }
        }
    }

    public boolean isDelete(int index, boolean isDelete) {
        boolean z = true;
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return isDelete;
        }
        if (!((index + 1) % 18 == 0 || index == SmileyParser.getSmileyResIds().length - 1)) {
            z = false;
        }
        return z;
    }

    public boolean checkftToMms() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            return RcsComposeMessage.checkftComToMms();
        }
        return false;
    }

    public void setFTtoMmsSendMessageModeAndDeleteChat() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            setFTtoMmsSendMessageMode();
            if (!RcsComposeMessage.getUndelivedFtToMms()) {
                deleteChatForFTtoMms(mMsgId);
            }
            RcsComposeMessage.setIsSendFtToMms(false);
        }
    }

    public static void setMsgId(long msgId, Context context) {
        mMsgId = msgId;
        mCMFContext = context;
    }

    public static void setFTtoMmsSendMessageMode() {
        MLog.d("RcsRichMessageEditor", "setFTtoMmsSendMessageMode() will updateRcsMode");
        Intent smsIntent = new Intent();
        smsIntent.putExtra("send_mode", 0);
        smsIntent.putExtra("force_set_send_mode", true);
        RcseMmsExt.updateRcsMode(smsIntent);
    }

    public static void deleteChatForFTtoMms(long uiMsgId) {
        SqliteWrapper.delete(mCMFContext, ContentUris.withAppendedId(Uri.parse("content://rcsim/chat/"), uiMsgId), null, null);
        RcsImageCache.getInstance(((Activity) mCMFContext).getFragmentManager(), mCMFContext).removeBitmapCache(RcsUtility.getBitmapFromMemCacheKey(uiMsgId, 1));
    }

    public boolean isEmptyThread(ComposeMessageFragment fragment, long threadId, Conversation conv, WorkingMessage workingMessage) {
        boolean z = true;
        if (!RcsCommonConfig.isRCSSwitchOn() || conv == null || workingMessage == null) {
            return false;
        }
        if (threadId <= 0) {
            return true;
        }
        if (workingMessage.hasAttachment() || workingMessage.hasSlideshow() || workingMessage.hasSubject() || workingMessage.hasValidText()) {
            return false;
        }
        if (conv.getMessageCount() - fragment.getRcsComposeMessage().getImMsgCount() != 0) {
            z = false;
        }
        return z;
    }

    public boolean isRcsSwitchOn() {
        return RcsCommonConfig.isRCSSwitchOn();
    }

    public void changeHint() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            if (RcseMmsExt.isRcsMode() && this.mSmsEditorText != null) {
                this.mSmsEditorText.setHint(R.string.type_to_compose_im_text_enter_to_send_new_rcs);
            } else if (!(RcseMmsExt.isRcsMode() || this.mSmsEditorText == null)) {
                this.mSmsEditorText.setHint(R.string.type_to_compose_text_enter_to_send_new_sms);
            }
        }
    }

    public void setRcsSaveDraftWhenFt(boolean rcsSaveDraftWhenFt) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.hasDraftWhenFt = rcsSaveDraftWhenFt;
        }
    }

    public boolean getRcsSaveDraftWhenFt() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            return this.hasDraftWhenFt;
        }
        return false;
    }

    public void setRcsLoadDraftFt(boolean rcsLoadDraftFt) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mRcsLoadDraftFt = rcsLoadDraftFt;
        }
    }

    public boolean getRcsLoadDraftFt() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            return this.mRcsLoadDraftFt;
        }
        return false;
    }

    public boolean getSaveMmsEmailAdress() {
        return RcsMmsConfig.getSaveMmsEmailAdress();
    }

    public boolean isImUIStyle() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            return RcseMmsExt.isRcsMode();
        }
        return false;
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (RcsCommonConfig.isRCSSwitchOn() && RcseMmsExt.isRcsMode() && TextUtils.isEmpty(s)) {
            RcsAudioMessage.setCurrentView(1);
        }
    }
}
