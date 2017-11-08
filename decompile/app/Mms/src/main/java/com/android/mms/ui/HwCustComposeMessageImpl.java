package com.android.mms.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.attachment.ui.conversation.ConversationInputManager;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.HwCustWorkingMessageImpl;
import com.android.mms.data.WorkingMessage;
import com.android.mms.ui.ComposeMessageFragment.ConversationActionBarAdapter;
import com.android.mms.ui.ComposeMessageFragment.CustComposeCallbackHandler;
import com.android.mms.ui.HwCustComposeMessage.IHwCustComposeMessageCallback;
import com.android.mms.ui.views.ComposeRecipientsView;
import com.android.mms.util.SignatureUtil;
import com.google.android.gms.R;
import com.huawei.android.telephony.MSimTelephonyManagerCustEx;
import com.huawei.cspcommon.ex.MultiLoadHandler.ILoadCallBack;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.CustEditText;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.HwRecipientsEditor;
import com.huawei.mms.ui.PeopleActionBar.AddWhatsAppPeopleActionBarAdapter;
import com.huawei.mms.ui.SplitActionBarView.OnCustomMenuListener;
import com.huawei.mms.util.HwCustPhoneServiceStateListener;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MccMncConfig;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class HwCustComposeMessageImpl extends HwCustComposeMessage implements ILoadCallBack {
    private static final boolean DBG = true;
    private static final int MENU_ID_BASE = 278927360;
    private static final int MENU_ID_BASE_INTERNAL = 278925312;
    public static final int MENU_ID_RECEIPENTS = 11123;
    private static final int MENU_ID_REPORT_SPAN = 278927490;
    public static final int MENU_ID_SEARCH = 11122;
    public static final String PROPERTY_FDN_ACTIVATED_SUB1 = "gsm.hw.fdn.activated1";
    public static final String PROPERTY_FDN_ACTIVATED_SUB2 = "gsm.hw.fdn.activated2";
    public static final String PROPERTY_FDN_PS_FLAG_EXISTS_SUB1 = "gsm.hw.fdn.ps.flag.exists1";
    public static final String PROPERTY_FDN_PS_FLAG_EXISTS_SUB2 = "gsm.hw.fdn.ps.flag.exists2";
    public static final int REQUEST_CODE_MEDIA_COMPRESS_FORWARD = 150;
    private static final String TAG = "HwCustComposeMessageImpl";
    private static final String VO_WIFI_API_NAME = (VERSION.SDK_INT > 23 ? "isWifiCallingAvailable" : "isWifiCallingEnabled");
    private static String VO_WIFI_MCCMNC_LIST = SystemProperties.get("ro.enable.vowifi", "");
    private static ArrayList<String> mVoWifiMccMncList = null;
    private static ArrayList receivelist = new ArrayList();
    private static HashMap<Long, String> sendlist = new HashMap();
    private OnClickListener editRecipientListener;
    private EmuiMenu emuiMenu;
    private AbstractEmuiActionBar mActionBarWhenSplit;
    private AddWhatsAppPeopleActionBarAdapter mActionbarAdapter;
    private ImageView mClearSearchMode;
    private ComposeMessageFragment mComposeMessageFragment;
    Context mContext;
    private ConversationInputManager mConversationInputManager;
    private int mCursorIndex;
    private TextView mCursorText;
    private CustEditText mEditSearch;
    private MessageListView mMessageListView;
    private MessageListAdapter mMsgListAdapter;
    private ImageView mNext;
    private EmuiMenu mNormalMenu;
    private ImageView mPrevious;
    private View mSearchCountView;
    private Integer[] mSearchId;
    private SearchDataLoader mSearchLoader;
    private boolean mSearchMode = false;
    private String mSearchString;
    private View mSearchStub;
    private boolean mSearchVisiblity = false;
    private long mThreadId;
    private LinearLayout mTitleHeader;
    private OnClickListener sendMessageListener;

    private static class EditRecipientListener implements OnClickListener {
        private HwRecipientsEditor mRecipientsEditor = null;

        public EditRecipientListener(HwRecipientsEditor recipientsEditor) {
            this.mRecipientsEditor = recipientsEditor;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
            this.mRecipientsEditor.requestFocus();
        }
    }

    private static class IgnoreDraftListener implements OnClickListener {
        private Runnable mExit = null;
        private WorkingMessage mWorkingMessage = null;

        public IgnoreDraftListener(Runnable exit, WorkingMessage workingmessage) {
            this.mExit = exit;
            this.mWorkingMessage = workingmessage;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            if (this.mWorkingMessage != null) {
                this.mWorkingMessage.discard();
            }
            dialog.dismiss();
            this.mExit.run();
        }
    }

    private static class SaveDraftListener implements OnClickListener {
        private Runnable mExit = null;

        public SaveDraftListener(Runnable exit) {
            this.mExit = exit;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
            this.mExit.run();
        }
    }

    private static class SendMessageListener implements OnClickListener {
        CustComposeCallbackHandler mHandlercallback = null;

        public SendMessageListener(CustComposeCallbackHandler handlercallback) {
            this.mHandlercallback = handlercallback;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
            Message msg = Message.obtain();
            msg.what = 1;
            this.mHandlercallback.handleMessage(msg);
        }
    }

    public boolean isNumberInFdnList(java.lang.String r12, int r13) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x007d in list []
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
        r11 = this;
        r10 = 1;
        r9 = 0;
        r1 = "content://icc/fdn/exits_query/subId/";
        r8 = android.net.Uri.parse(r1);
        r1 = r11.mContext;
        r0 = r1.getContentResolver();
        r1 = " ";
        r2 = "";
        r12 = r12.replaceAll(r1, r2);
        r6 = 0;
        r2 = (long) r13;
        r1 = android.content.ContentUris.withAppendedId(r8, r2);	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2 = 1;	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r4 = new java.lang.String[r2];	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2 = 0;	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r4[r2] = r12;	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2 = 0;	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r3 = 0;	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r5 = 0;	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        if (r6 == 0) goto L_0x004e;	 Catch:{ Exception -> 0x006e, all -> 0x007e }
    L_0x002e:
        r1 = "HwCustComposeMessageImpl";	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2.<init>();	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r3 = "[mms_fdn]number exists in fdn list, number is ";	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2 = r2.append(r12);	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2 = r2.toString();	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        android.util.Log.d(r1, r2);	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        if (r6 == 0) goto L_0x004d;
    L_0x004a:
        r6.close();
    L_0x004d:
        return r10;
    L_0x004e:
        r1 = "HwCustComposeMessageImpl";	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2.<init>();	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r3 = "[mms_fdn]number doesn't exists in fdn list, number is ";	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2 = r2.append(r12);	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2 = r2.toString();	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        android.util.Log.d(r1, r2);	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        if (r6 == 0) goto L_0x006d;
    L_0x006a:
        r6.close();
    L_0x006d:
        return r9;
    L_0x006e:
        r7 = move-exception;
        r1 = "HwCustComposeMessageImpl";	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        r2 = "[mms_fdn]get fdn List exception ";	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        android.util.Log.e(r1, r2, r7);	 Catch:{ Exception -> 0x006e, all -> 0x007e }
        if (r6 == 0) goto L_0x007d;
    L_0x007a:
        r6.close();
    L_0x007d:
        return r9;
    L_0x007e:
        r1 = move-exception;
        if (r6 == 0) goto L_0x0084;
    L_0x0081:
        r6.close();
    L_0x0084:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.ui.HwCustComposeMessageImpl.isNumberInFdnList(java.lang.String, int):boolean");
    }

    public HwCustComposeMessageImpl(HwBaseFragment fragment) {
        super(fragment);
        this.mContext = fragment.getContext();
    }

    public void setHwCustCallback(IHwCustComposeMessageCallback callback) {
    }

    public boolean doCustExitCompose(boolean isBackPressed, Runnable exit, WorkingMessage workingMessage) {
        if (!isBackPressed || !HwCustMmsConfigImpl.isShowConfirmDialog()) {
            return false;
        }
        if (workingMessage.requiresMms()) {
            showSaveDraftConfirmDialog(exit, workingMessage);
            return DBG;
        }
        String signature = SignatureUtil.getSignature(this.mContext, "");
        String content = workingMessage.getText().toString();
        if (TextUtils.isEmpty(content) || SignatureUtil.deleteNewlineSymbol(content).equals(signature)) {
            return false;
        }
        showSaveDraftConfirmDialog(exit, workingMessage);
        return DBG;
    }

    public void showSaveDraftConfirmDialog(Runnable exit, WorkingMessage workingMessage) {
        OnClickListener saveListener = new SaveDraftListener(exit);
        new Builder(this.mContext).setMessage(R.string.message_save_or_discard_confirm).setPositiveButton(R.string.save, saveListener).setNegativeButton(R.string.ignore, new IgnoreDraftListener(exit, workingMessage)).setTitle(R.string.message_save_or_discard_confirm_title).show();
    }

    public void switchToEdit(Menu optionMenu, boolean hasMmsItem) {
        if (HwCustMmsConfigImpl.isAllowReportSpam()) {
            this.emuiMenu = new EmuiMenu(optionMenu);
            this.emuiMenu.setItemVisible(MENU_ID_REPORT_SPAN, false);
        }
    }

    public void switchToEditSmsCust(Menu optionMenu) {
        if (HwCustMmsConfigImpl.isAllowReportSpam()) {
            this.emuiMenu = new EmuiMenu(optionMenu);
            this.emuiMenu.setItemVisible(MENU_ID_REPORT_SPAN, DBG);
        }
    }

    public void switchToEditMmsCust(Menu optionMenu) {
        if (HwCustMmsConfigImpl.isAllowReportSpam()) {
            this.emuiMenu = new EmuiMenu(optionMenu);
            this.emuiMenu.setItemVisible(MENU_ID_REPORT_SPAN, false);
        }
    }

    public void prepareMenuInEditModeCust(Menu optionMenu) {
        if (HwCustMmsConfigImpl.isAllowReportSpam()) {
            this.emuiMenu = new EmuiMenu(optionMenu);
            this.emuiMenu.addOverflowMenu(MENU_ID_REPORT_SPAN, R.string.menu_report_spam);
        }
    }

    public boolean handleCustMenu(int itemID, MessageItem msgItem, ComposeMessageFragment composeObj) {
        if (MENU_ID_REPORT_SPAN != itemID) {
            return false;
        }
        String SPRINT_EMAIL_NUMBER = "6245";
        Intent intent = new Intent();
        String address = msgItem.mAddress;
        String body = msgItem.mBody;
        if (!(address == null || !address.equals(SPRINT_EMAIL_NUMBER) || body == null)) {
            int i = body.indexOf(" ");
            if (Contact.isEmailAddress(body.substring(0, i))) {
                body = body.substring(i + 1);
            }
        }
        intent.putExtra("sms_body", body);
        intent.putExtra("android.intent.extra.PHONE_NUMBER", HwCustMmsConfigImpl.getReportSpamNumber());
        intent.setClassName(composeObj.getContext(), "com.android.mms.ui.ComposeMessageActivity");
        composeObj.startActivity(intent);
        return DBG;
    }

    public void prepareReplyMenu(MenuEx aOptionMenu) {
        if (HwCustMmsConfigImpl.supportReplyInGroupMessage() && aOptionMenu != null) {
            aOptionMenu.addOverflowMenu(278925337, R.string.menu_reply);
            aOptionMenu.setItemVisible(278925337, false);
            aOptionMenu.setItemEnabled(278925337, DBG);
        }
    }

    public void switchToReplyMenuInEditMode(MenuEx aOptionMenu, boolean isVisisble) {
        if (HwCustMmsConfigImpl.supportReplyInGroupMessage() && aOptionMenu != null) {
            aOptionMenu.setItemVisible(278925337, isVisisble);
        }
    }

    public void switchToReplyMenuInEditMode(MenuEx aOptionMenu, MessageItem aMsgItem) {
        if (HwCustMmsConfigImpl.supportReplyInGroupMessage() && aOptionMenu != null && aMsgItem != null) {
            boolean setVisible = (1 != aMsgItem.mBoxId || aMsgItem.getTo().length + aMsgItem.getCc().length <= 1) ? false : DBG;
            aOptionMenu.setItemVisible(278925337, setVisible);
        }
    }

    public String prepareSubjectInReply(String aSubject) {
        if (HwCustMmsConfigImpl.supportEmptyFWDSubject()) {
            return aSubject;
        }
        return "";
    }

    public void handleReplyMenu(MessageItem aMsgItem) {
        if (HwCustMmsConfigImpl.supportReplyInGroupMessage() && aMsgItem != null) {
            Intent intent = new Intent();
            intent.setData(Uri.parse(aMsgItem.mAddress));
            intent.setClassName(this.mContext, "com.android.mms.ui.ComposeMessageActivity");
            this.mContext.startActivity(intent);
        }
    }

    public String getShortCodeErrorString() {
        return this.mContext.getString(R.string.group_messaging_prohibited_short_code);
    }

    public String getSubjectFromHwCust(String msgItemSubject, String subject) {
        if (HwCustMmsConfigImpl.allowSubject() || msgItemSubject != null) {
            return subject;
        }
        return null;
    }

    public void hideSubjectView(RichMessageEditor richEditor, WorkingMessage workingMessage) {
        if (richEditor != null && richEditor.getVisibility() == 0 && workingMessage != null && !HwCustMmsConfigImpl.allowSubject() && !workingMessage.hasSubject()) {
            richEditor.showSubjectEditor(false);
        }
    }

    public boolean isHideKeyboard(boolean isLandscape) {
        return !HwCustMmsConfigImpl.isHideKeyboard() ? isLandscape : DBG;
    }

    public boolean rebuildSendButtonSms(TextView sendButtonSms) {
        if (!MmsConfig.getMmsBoolConfig("enableSendButtonSmsText")) {
            return false;
        }
        sendButtonSms.setText(R.string.send);
        sendButtonSms.setPadding(0, 0, 0, 4);
        return DBG;
    }

    public boolean allowFwdWapPushMsg() {
        Log.d(TAG, "allowFwdWapPushMsg");
        return HwCustMmsConfigImpl.allowFwdWapPushMsg();
    }

    public void setOnePageSmsText(WorkingMessage workingMessage, RichMessageEditor richEditor) {
        if (HwCustMmsConfigImpl.getEnableAlertLongSms() && getSmsSegmentCount(workingMessage) > 1) {
            alertForSmsTooLong();
            CharSequence s = getSmsInOnePage(workingMessage);
            workingMessage.setText(s);
            richEditor.setText(s);
        }
    }

    private CharSequence getSmsInOnePage(WorkingMessage workingMessage) {
        CharSequence textOrigin = workingMessage.getText();
        CharSequence text = textOrigin;
        int i = 0;
        while (true) {
            int i2 = i + 1;
            if (i > textOrigin.length() || SmsMessage.calculateLength(text, false)[0] <= 1) {
                return text;
            }
            text = textOrigin.subSequence(0, textOrigin.length() - i2);
            i = i2;
        }
        return text;
    }

    private int getSmsSegmentCount(WorkingMessage workingMessage) {
        return SmsMessage.calculateLength(workingMessage.getText(), false)[0];
    }

    private void alertForSmsTooLong() {
        Toast.makeText(this.mContext, R.string.sms_too_long, 0).show();
    }

    public void setVattachInvisible(View vAttach) {
        if (HwCustMmsConfigImpl.getRemoveMms()) {
            vAttach.setVisibility(8);
        }
    }

    public boolean sendMmsUnsupportToast() {
        if (!HwCustMmsConfigImpl.getRemoveMms()) {
            return false;
        }
        Toast.makeText(this.mContext, R.string.mms_not_supported, 1).show();
        return DBG;
    }

    public boolean checkBeforeSendMessage(HwRecipientsEditor recipientsEditor, CustComposeCallbackHandler handlercallback) {
        if (HwCustMmsConfigImpl.check7DigitNumber()) {
            boolean isContain7DigitNum = false;
            for (String num : recipientsEditor.getNumbers()) {
                if (num != null && getTrimmedLength(num) == 7) {
                    isContain7DigitNum = DBG;
                    break;
                }
            }
            if (isContain7DigitNum) {
                showSendEditConfirmDialog(recipientsEditor, handlercallback);
                return DBG;
            }
        }
        return false;
    }

    public void showSendEditConfirmDialog(HwRecipientsEditor recipientsEditor, CustComposeCallbackHandler handlercallback) {
        if (this.sendMessageListener == null) {
            this.sendMessageListener = new SendMessageListener(handlercallback);
        }
        if (this.editRecipientListener == null) {
            this.editRecipientListener = new EditRecipientListener(recipientsEditor);
        }
        new Builder(this.mContext).setMessage(R.string.send_msg_or_edit_number_confirm).setPositiveButton(R.string.send_msg_with_7digit_number, this.sendMessageListener).setNegativeButton(R.string.edit_7digit_number_to_10digit, this.editRecipientListener).show();
    }

    private int getTrimmedLength(String num) {
        int len = num.length();
        int start = 0;
        int length = 0;
        while (start < len) {
            if (num.charAt(start) >= '0' && num.charAt(start) <= '9') {
                length++;
            }
            start++;
        }
        return length;
    }

    public boolean isSmsToMmsInCTRoaming(Context context) {
        return getCTNetworkRoaming(context) ? HwCustMmsConfigImpl.isCTRoamingMultipartSmsLimit() : false;
    }

    public static boolean getCTNetworkRoaming(Context context) {
        int currentSub = 0;
        try {
            currentSub = MSimTelephonyManagerCustEx.getUserDefaultSubscription(context);
        } catch (Exception e) {
            Log.e(TAG, "user default subscription is failed to get");
        }
        return isChinatelecom() ? MessageUtils.isNetworkRoaming(currentSub) : false;
    }

    private static boolean isChinatelecom() {
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
            return SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        }
        return false;
    }

    public boolean getIsTitleChangeWhenRecepientsChange() {
        return HwCustMmsConfigImpl.getIsTitleChangeWhenRecepientsChange();
    }

    public void showNewMessageTitleWithMaxRecipient(ContactList list, AbstractEmuiActionBar mActionBar) {
        int cnt = list.size();
        String title = this.mContext.getString(R.string.new_message);
        String subTitle = null;
        mActionBar.setTitleGravityCenter(DBG);
        mActionBar.setTitle(title);
        if (cnt > 0) {
            int maxCnt = MmsConfig.getRecipientLimit();
            subTitle = this.mContext.getString(R.string.recipient_max_count, new Object[]{Integer.valueOf(cnt), Integer.valueOf(maxCnt)});
        }
        mActionBar.setSubtitle(subTitle);
        mActionBar.setSubTitleGravityCenter();
    }

    public String getRecipientCountStr(ContactList list, Context context) {
        int cnt = list.size();
        int maxCnt = MmsConfig.getRecipientLimit();
        return context.getResources().getQuantityString(R.plurals.recipient_count_with_max, cnt, new Object[]{Integer.valueOf(cnt), Integer.valueOf(maxCnt)});
    }

    private int getUserDefaultSubscription() {
        int currentSub = 0;
        try {
            currentSub = MSimTelephonyManagerCustEx.getUserDefaultSubscription(this.mContext);
        } catch (Exception e) {
            Log.e(TAG, "On LTE Only, user default subscription is failed to get");
        }
        return currentSub;
    }

    public boolean supportSmsToEmail() {
        return HwCustMmsConfigImpl.allowSendSmsToEmail();
    }

    public int[] getParamsWithEmail(WorkingMessage workingMessage, CharSequence text, RichMessageEditor mRichEditor) {
        StringBuffer msgBuffer = new StringBuffer();
        if (TextUtils.isEmpty(text)) {
            text = mRichEditor.getText();
        }
        msgBuffer.append(text);
        String longestEmailRecipient = HwCustWorkingMessageImpl.getLongestEmailRecipient(workingMessage);
        if (longestEmailRecipient != null) {
            msgBuffer.append(" ");
            msgBuffer.append(longestEmailRecipient);
        }
        return SmsMessage.calculateLength(msgBuffer.toString(), false);
    }

    public boolean isDataServiceDisabledByFDN(int subscription) {
        boolean z = false;
        if (subscription == 0) {
            if (SystemProperties.getBoolean("ro.config.fdn.restrict.ds", false) && SystemProperties.getBoolean(PROPERTY_FDN_ACTIVATED_SUB1, false) && !SystemProperties.getBoolean(PROPERTY_FDN_PS_FLAG_EXISTS_SUB1, false)) {
                z = DBG;
            }
            return z;
        } else if (subscription != 1) {
            return false;
        } else {
            if (SystemProperties.getBoolean("ro.config.fdn.restrict.ds", false) && SystemProperties.getBoolean(PROPERTY_FDN_ACTIVATED_SUB2, false) && !SystemProperties.getBoolean(PROPERTY_FDN_PS_FLAG_EXISTS_SUB2, false)) {
                z = DBG;
            }
            return z;
        }
    }

    public boolean isInvalidFdnNumber(String number, int subscription) {
        return (!needToCheckNumberInFdnList(subscription) || isNumberInFdnList(MccMncConfig.getFilterNumberByMCCMNC(number), subscription)) ? false : DBG;
    }

    public boolean needToCheckNumberInFdnList(int subscription) {
        boolean z = false;
        if (subscription == 0) {
            if (SystemProperties.getBoolean("ro.config.fdn.restrict.ds", false) && SystemProperties.getBoolean(PROPERTY_FDN_ACTIVATED_SUB1, false)) {
                z = SystemProperties.getBoolean(PROPERTY_FDN_PS_FLAG_EXISTS_SUB1, false);
            }
            return z;
        } else if (subscription != 1) {
            return false;
        } else {
            if (SystemProperties.getBoolean("ro.config.fdn.restrict.ds", false) && SystemProperties.getBoolean(PROPERTY_FDN_ACTIVATED_SUB2, false)) {
                z = SystemProperties.getBoolean(PROPERTY_FDN_PS_FLAG_EXISTS_SUB2, false);
            }
            return z;
        }
    }

    public boolean hasInvalidRecipientForFDN(boolean isMms, Object[] numberList, int subscription) {
        if (isMms) {
            for (Object number : numberList) {
                if (isInvalidFdnNumber(number.toString(), subscription)) {
                    return DBG;
                }
            }
        }
        return false;
    }

    public String formatInvalidNumbersForFdn(Object[] numberList, int subscription) {
        StringBuilder sb = new StringBuilder();
        for (Object number : numberList) {
            if (isInvalidFdnNumber(number.toString(), subscription)) {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(number);
            }
        }
        return sb.toString();
    }

    private void alertForInvalidRecipientForFdnEditorNotVisible(String title, int length) {
        new Builder(this.mContext).setIconAttribute(16843605).setTitle(title).setMessage(this.mContext.getResources().getQuantityString(R.plurals.invalid_recipient_message, length)).setPositiveButton(R.string.yes, null).show();
    }

    public void popForFdn(boolean recipientsEditorVisible, Object[] numberArray, ComposeRecipientsView composeRecipientsView, int subscription) {
        String title = this.mContext.getResources().getQuantityString(R.plurals.has_invalid_recipient, numberArray.length, new Object[]{formatInvalidNumbersForFdn(numberArray, subscription)});
        if (recipientsEditorVisible) {
            composeRecipientsView.alertForInvalidRecipient(title);
        } else {
            alertForInvalidRecipientForFdnEditorNotVisible(title, numberArray.length);
        }
        Toast.makeText(this.mContext, R.string.fdn_check_failure, 0).show();
    }

    public boolean judgeDSDisableByFDN(int subscription) {
        return HwCustMmsConfigImpl.isEnableFdnCheckForMms() ? isDataServiceDisabledByFDN(subscription) : false;
    }

    public boolean judgeNumberAndRecipientInFDNList(boolean isMms, Object[] numberList, int subscription) {
        if (HwCustMmsConfigImpl.isEnableFdnCheckForMms() && needToCheckNumberInFdnList(subscription)) {
            return hasInvalidRecipientForFDN(isMms, numberList, subscription);
        }
        return false;
    }

    public void showFDNToast() {
        Toast.makeText(this.mContext, this.mContext.getString(R.string.fdn_mms_forbidden), 0).show();
    }

    public boolean isShowToastonDataNotEnabled() {
        return MmsConfig.getMmsBoolConfig("showToastonDataNotEnabled", false);
    }

    public int getDataNotEnabledToastId(int textId) {
        return R.string.mobileDataDisabled_toast_on_mms_send;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean showWifiMessageErrorDialog() {
        if (MessageUtils.isMultiSimEnabled() || this.mContext == null || !isVOWifiFeatureEnabled()) {
            return false;
        }
        if (!HwCustPhoneServiceStateListener.isNetworkNotAvailable()) {
            Log.i(TAG, "Network is available.");
            return false;
        } else if (isWifiCallEnabled()) {
            Log.i(TAG, "Wifi calling is enabled");
            return false;
        } else {
            Builder builder = new Builder(this.mContext).setPositiveButton(17039370, null).setCancelable(false);
            builder.setMessage(this.mContext.getString(R.string.wifi_message_alert_text));
            builder.show();
            return DBG;
        }
    }

    public static boolean isVOWifiFeatureEnabled() {
        if (TextUtils.isEmpty(VO_WIFI_MCCMNC_LIST)) {
            return false;
        }
        if (mVoWifiMccMncList == null) {
            mVoWifiMccMncList = new ArrayList();
            String[] lMccMncArray = VO_WIFI_MCCMNC_LIST.split(";");
            if (lMccMncArray != null && lMccMncArray.length > 0) {
                for (String mccmnc : lMccMncArray) {
                    mVoWifiMccMncList.add(mccmnc);
                }
            }
        }
        TelephonyManager mSimTelephonyManager = MmsApp.getDefaultTelephonyManager();
        if (mSimTelephonyManager != null) {
            return mVoWifiMccMncList.contains(mSimTelephonyManager.getSimOperator());
        }
        return false;
    }

    public boolean isWifiCallEnabled() {
        boolean ret = false;
        TelephonyManager mSimTelephonyManager = MmsApp.getDefaultTelephonyManager();
        if (mSimTelephonyManager != null) {
            try {
                Method isWifiCallingEnabled = TelephonyManager.class.getDeclaredMethod(VO_WIFI_API_NAME, new Class[0]);
                isWifiCallingEnabled.setAccessible(DBG);
                Boolean isEnabled = (Boolean) isWifiCallingEnabled.invoke(mSimTelephonyManager, (Object[]) null);
                Method isImsRegistered = TelephonyManager.class.getDeclaredMethod("isImsRegistered", new Class[0]);
                isImsRegistered.setAccessible(DBG);
                Boolean isRegistered = (Boolean) isImsRegistered.invoke(mSimTelephonyManager, (Object[]) null);
                if (!(isEnabled == null || isRegistered == null)) {
                    ret = isEnabled.booleanValue() ? isRegistered.booleanValue() : false;
                }
                return ret;
            } catch (Exception aEx) {
                aEx.printStackTrace();
            }
        }
        return false;
    }

    public void initilizeUI(ComposeMessageFragment aComposeMessageFragment, MessageListView aMessageListView, MessageListAdapter aMessageListAdapter, long aThreadId, ConversationActionBarAdapter aActionbarAdapter, ConversationInputManager aConversationInputManager, EmuiMenu aNormalMenu, AbstractEmuiActionBar aActionBarWhenSplit) {
        if (aComposeMessageFragment != null && aMessageListView != null && aMessageListAdapter != null && aThreadId > 0 && HwCustMmsConfigImpl.getSupportSearchConversation() && !getSearchVisibility() && aActionbarAdapter != null && aNormalMenu != null && aActionBarWhenSplit != null && aConversationInputManager != null) {
            this.mSearchVisiblity = DBG;
            this.mThreadId = aThreadId;
            this.mComposeMessageFragment = aComposeMessageFragment;
            this.mMsgListAdapter = aMessageListAdapter;
            this.mMessageListView = aMessageListView;
            this.mTitleHeader = (LinearLayout) aComposeMessageFragment.findViewById(R.id.title_header);
            this.mEditSearch = (CustEditText) aComposeMessageFragment.findViewById(R.id.search_text);
            this.mEditSearch.setHint(this.mContext.getString(R.string.search_hint));
            this.mClearSearchMode = (ImageView) aComposeMessageFragment.findViewById(R.id.clearSearchResult);
            this.mSearchStub = aComposeMessageFragment.findViewById(R.id.search_actionbar);
            this.mSearchCountView = aComposeMessageFragment.findViewById(R.id.search_count_bar);
            this.mNext = (ImageView) aComposeMessageFragment.findViewById(R.id.next);
            this.mPrevious = (ImageView) aComposeMessageFragment.findViewById(R.id.previous);
            this.mCursorText = (TextView) aComposeMessageFragment.findViewById(R.id.search_count);
            View aBtnBack = aComposeMessageFragment.findViewById(R.id.bt_back);
            aBtnBack.setVisibility(0);
            this.mConversationInputManager = aConversationInputManager;
            this.mActionbarAdapter = aActionbarAdapter;
            this.mNormalMenu = aNormalMenu;
            this.mActionBarWhenSplit = aActionBarWhenSplit;
            this.mClearSearchMode.setVisibility(0);
            this.mClearSearchMode.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    HwCustComposeMessageImpl.this.mSearchString = "";
                    HwCustComposeMessageImpl.this.mMsgListAdapter.setSearchString("");
                    HwCustComposeMessageImpl.this.mMsgListAdapter.setPositionList(null);
                    HwCustComposeMessageImpl.this.mSearchCountView.setVisibility(8);
                    HwCustComposeMessageImpl.this.mCursorText.setText("");
                    HwCustComposeMessageImpl.this.mEditSearch.setText("");
                    HwCustComposeMessageImpl.this.mMsgListAdapter.notifyDataSetChanged();
                }
            });
            aBtnBack.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    HwCustComposeMessageImpl.this.updateSearchMode();
                }
            });
            this.mSearchLoader = new SearchDataLoader(aComposeMessageFragment.getActivity(), this);
            this.mEditSearch.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    HwCustComposeMessageImpl.this.mSearchId = null;
                    if (s.length() > 1) {
                        HwCustComposeMessageImpl.this.mSearchString = s.toString();
                        HwCustComposeMessageImpl.this.mSearchLoader.asyncSearch(s.toString(), String.valueOf(HwCustComposeMessageImpl.this.mThreadId));
                        HwCustComposeMessageImpl.this.mSearchCountView.setVisibility(0);
                        return;
                    }
                    HwCustComposeMessageImpl.this.mSearchString = "";
                    HwCustComposeMessageImpl.this.mMsgListAdapter.setSearchString("");
                    HwCustComposeMessageImpl.this.mMsgListAdapter.setPositionList(HwCustComposeMessageImpl.this.mSearchId);
                    HwCustComposeMessageImpl.this.mSearchCountView.setVisibility(8);
                    HwCustComposeMessageImpl.this.mCursorText.setText("");
                    HwCustComposeMessageImpl.this.mMsgListAdapter.notifyDataSetChanged();
                }
            });
            this.mEditSearch.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (HwCustComposeMessageImpl.this.mConversationInputManager != null && HwCustComposeMessageImpl.this.mConversationInputManager.isMediaPickerVisible()) {
                        HwCustComposeMessageImpl.this.mConversationInputManager.showHideMediaPicker(false, HwCustComposeMessageImpl.DBG);
                    }
                    HwCustComposeMessageImpl.this.mEditSearch.requestFocus();
                    return false;
                }
            });
            this.mNext.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (HwCustComposeMessageImpl.this.mSearchId != null && HwCustComposeMessageImpl.this.mCursorIndex != -1 && HwCustComposeMessageImpl.this.mCursorIndex < HwCustComposeMessageImpl.this.mSearchId.length) {
                        if (HwCustComposeMessageImpl.this.mCursorIndex == HwCustComposeMessageImpl.this.mSearchId.length - 1) {
                            HwCustComposeMessageImpl.this.mCursorIndex = -1;
                        }
                        HwCustComposeMessageImpl hwCustComposeMessageImpl = HwCustComposeMessageImpl.this;
                        hwCustComposeMessageImpl.mCursorIndex = hwCustComposeMessageImpl.mCursorIndex + 1;
                        HwCustComposeMessageImpl.this.mMessageListView.smoothScrollToPosition(HwCustComposeMessageImpl.this.mSearchId[HwCustComposeMessageImpl.this.mCursorIndex].intValue());
                        HwCustComposeMessageImpl.this.mCursorText.setText(String.format(HwCustComposeMessageImpl.this.mContext.getString(R.string.search_results), new Object[]{Integer.valueOf(HwCustComposeMessageImpl.this.mCursorIndex + 1), Integer.valueOf(HwCustComposeMessageImpl.this.mSearchId.length)}));
                    }
                }
            });
            this.mPrevious.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (HwCustComposeMessageImpl.this.mSearchId != null && HwCustComposeMessageImpl.this.mCursorIndex >= 0) {
                        if (HwCustComposeMessageImpl.this.mCursorIndex == 0) {
                            HwCustComposeMessageImpl.this.mCursorIndex = HwCustComposeMessageImpl.this.mSearchId.length;
                        }
                        HwCustComposeMessageImpl hwCustComposeMessageImpl = HwCustComposeMessageImpl.this;
                        hwCustComposeMessageImpl.mCursorIndex = hwCustComposeMessageImpl.mCursorIndex - 1;
                        HwCustComposeMessageImpl.this.mMessageListView.smoothScrollToPosition(HwCustComposeMessageImpl.this.mSearchId[HwCustComposeMessageImpl.this.mCursorIndex].intValue());
                        HwCustComposeMessageImpl.this.mCursorText.setText(String.format(HwCustComposeMessageImpl.this.mContext.getString(R.string.search_results), new Object[]{Integer.valueOf(HwCustComposeMessageImpl.this.mCursorIndex + 1), Integer.valueOf(HwCustComposeMessageImpl.this.mSearchId.length)}));
                    }
                }
            });
            if (this.mActionbarAdapter.getContactList().size() > 1) {
                updateNormalMenu();
            }
        }
    }

    public void updateNormalMenu() {
        if (HwCustMmsConfigImpl.getSupportSearchConversation() && this.mActionbarAdapter != null && this.mMessageListView != null && this.mActionbarAdapter.getContactList() != null && this.mActionbarAdapter.getContactList().size() > 1 && (HwMessageUtils.isSplitOn() || !this.mMessageListView.isInEditMode())) {
            ((ImageView) this.mComposeMessageFragment.findViewById(R.id.bt_ok)).setVisibility(8);
            this.mNormalMenu.resetOptionMenu(this.mActionBarWhenSplit.getMenu());
            this.mNormalMenu.clear();
            this.mNormalMenu.addOverflowMenu(MENU_ID_SEARCH, R.string.menu_search);
            this.mNormalMenu.addOverflowMenu(MENU_ID_RECEIPENTS, R.string.menu_check_group_participants);
            this.mActionBarWhenSplit.refreshMenu();
            this.mActionBarWhenSplit.showMenu(DBG);
            this.mActionBarWhenSplit.getSplitActionBarView().setOnCustomMenuListener(new OnCustomMenuListener() {
                public void onPrepareOptionsMenu(Menu aMenu) {
                }

                public boolean onCustomMenuItemClick(MenuItem aMenuItem) {
                    switch (aMenuItem.getItemId()) {
                        case HwCustComposeMessageImpl.MENU_ID_SEARCH /*11122*/:
                            HwCustComposeMessageImpl.this.updateSearchMode();
                            break;
                        case HwCustComposeMessageImpl.MENU_ID_RECEIPENTS /*11123*/:
                            if (HwCustComposeMessageImpl.this.mActionbarAdapter != null) {
                                HwCustComposeMessageImpl.this.mActionbarAdapter.viewPeopleInfo();
                                break;
                            }
                            break;
                    }
                    HwCustComposeMessageImpl.this.updateNormalMenu();
                    return false;
                }
            });
        }
    }

    public void onLoadComplete(int token, Cursor c) {
        this.mCursorIndex = 0;
        ArrayList<Integer> lSearchIds = new ArrayList();
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    do {
                        long lId = c.getLong(c.getColumnIndex("_id"));
                        if (lId != -1) {
                            lSearchIds.add(Integer.valueOf(getPosition(lId, c.getString(c.getColumnIndex("table_to_use")))));
                        }
                    } while (c.moveToNext());
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
        if (lSearchIds.size() > 0) {
            this.mSearchId = (Integer[]) lSearchIds.toArray(new Integer[lSearchIds.size()]);
        } else {
            this.mSearchId = null;
        }
        if (this.mSearchId == null || this.mSearchId.length <= 0) {
            this.mMsgListAdapter.setSearchString("");
            this.mCursorText.setText(String.format(this.mContext.getString(R.string.search_results), new Object[]{Integer.valueOf(this.mCursorIndex), Integer.valueOf(this.mCursorIndex)}));
            this.mMsgListAdapter.notifyDataSetChanged();
        } else {
            this.mMsgListAdapter.setSearchString(this.mSearchString);
            Arrays.sort(this.mSearchId);
            this.mCursorIndex = this.mSearchId.length - 1;
            this.mCursorText.setText(String.format(this.mContext.getString(R.string.search_results), new Object[]{Integer.valueOf(this.mCursorIndex + 1), Integer.valueOf(this.mSearchId.length)}));
            this.mMessageListView.smoothScrollToPosition(this.mSearchId[this.mCursorIndex].intValue());
        }
        this.mMsgListAdapter.setPositionList(this.mSearchId);
        this.mMsgListAdapter.notifyDataSetChanged();
    }

    public void updateSearchMode() {
        if (HwCustMmsConfigImpl.getSupportSearchConversation()) {
            if (this.mSearchMode) {
                this.mSearchMode = false;
                this.mSearchStub.setVisibility(8);
                this.mTitleHeader.setVisibility(0);
                this.mCursorIndex = -1;
                this.mSearchId = null;
            } else {
                this.mSearchStub.setVisibility(0);
                this.mSearchMode = DBG;
                this.mSearchCountView.setVisibility(0);
                this.mTitleHeader.setVisibility(8);
                this.mCursorIndex = 0;
                this.mMsgListAdapter.setSearchString("");
                this.mMsgListAdapter.notifyDataSetChanged();
            }
            this.mEditSearch.setText("");
            this.mCursorText.setText("");
            if (this.mSearchMode) {
                this.mEditSearch.requestFocus();
                ((InputMethodManager) this.mContext.getSystemService("input_method")).toggleSoftInput(2, 1);
            }
        }
    }

    private int getPosition(long aId, String aType) {
        Cursor lCursor = this.mMsgListAdapter.getCursor();
        if (lCursor == null || !lCursor.moveToFirst()) {
            return -1;
        }
        int lPostion = 0;
        do {
            long lId = lCursor.getLong(lCursor.getColumnIndex("_id"));
            String lType = lCursor.getString(lCursor.getColumnIndex("transport_type"));
            if (aId == lId) {
                if (Integer.parseInt(aType) == ("mms".equals(lType) ? 2 : 1)) {
                    return lPostion;
                }
            }
            lPostion++;
        } while (lCursor.moveToNext());
        return -1;
    }

    public boolean getSearchMode() {
        if (HwCustMmsConfigImpl.getSupportSearchConversation()) {
            return this.mSearchMode;
        }
        return false;
    }

    private boolean getSearchVisibility() {
        return this.mSearchVisiblity;
    }

    public void hideKeyboard() {
        if (HwCustMmsConfigImpl.getSupportSearchConversation() && this.mEditSearch != null) {
            this.mEditSearch.clearFocus();
            ((InputMethodManager) this.mContext.getSystemService("input_method")).hideSoftInputFromWindow(this.mEditSearch.getWindowToken(), 0);
        }
    }

    public void addSearchMenuItem(int aMenuIdSearch, EmuiMenu aNormalMenu) {
        if (aNormalMenu != null && HwCustMmsConfigImpl.getSupportSearchConversation()) {
            aNormalMenu.addOverflowMenu(aMenuIdSearch, R.string.menu_search);
        }
    }
}
