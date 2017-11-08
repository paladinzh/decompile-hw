package com.android.mms.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import com.android.mms.attachment.ui.conversation.ConversationInputManager;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.Conversation.ConversationQueryHandler;
import com.android.mms.data.WorkingMessage;
import com.android.mms.ui.ComposeMessageFragment.ConversationActionBarAdapter;
import com.android.mms.ui.ComposeMessageFragment.CustComposeCallbackHandler;
import com.android.mms.ui.views.ComposeRecipientsView;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.DynamicActionBar;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.HwRecipientsEditor;
import com.huawei.mms.ui.PeopleActionBar.PeopleActionBarAdapter;
import java.util.ArrayList;
import java.util.List;

public class HwCustComposeMessage {
    private static final String TAG = "HwCustComposeMessage";

    public interface IHwCustComposeMessageCallback {
        ContactList constructContactsFromInput(boolean z);

        void editRcsMessageItem(MessageItem messageItem);

        HwCustMessageListAdapter getHwCustMsgListAdapter();

        MessageListView getMessageListView();

        MessageListAdapter getMsgListAdapter();

        boolean getRcsLoadDraftFt();

        List<String> getRecipientsNum();

        void hidePanel();

        void hideRecipientEditor();

        boolean isMsgListAdapterValid();

        boolean isRecipientsVisiable();

        void optPanel(boolean z);

        void setMenuExItemEnabled(int i, boolean z);

        void setMenuExItemVisible(int i, boolean z);

        void setRcsSaveDraftWhenFt(boolean z);

        void showVcalendarDlgFromCalendar(Uri uri, ArrayList<Uri> arrayList);

        void updateSendButtonInCust();

        void updateTitle(ContactList contactList);
    }

    public HwCustComposeMessage(HwBaseFragment fragment) {
    }

    public boolean doCustExitCompose(boolean isBackPressed, Runnable exit, WorkingMessage workingMessage) {
        return false;
    }

    public void switchToEditSmsCust(Menu optionMenu) {
    }

    public void switchToEditMmsCust(Menu optionMenu) {
    }

    public void prepareMenuInEditModeCust(Menu optionMenu) {
    }

    public boolean handleCustMenu(int itemID, MessageItem msgItem, ComposeMessageFragment composeObj) {
        return false;
    }

    public void prepareReplyMenu(MenuEx aOptionMenu) {
    }

    public void switchToReplyMenuInEditMode(MenuEx aOptionMenu, boolean isVisisble) {
    }

    public void switchToReplyMenuInEditMode(MenuEx aOptionMenu, MessageItem aMsgItem) {
    }

    public void handleReplyMenu(MessageItem aMsgItem) {
    }

    public String prepareSubjectInReply(String aSubject) {
        return aSubject;
    }

    public String getShortCodeErrorString() {
        return "";
    }

    public boolean isHideKeyboard(boolean isLandscape) {
        return isLandscape;
    }

    public boolean rebuildSendButtonSms(TextView sendButtonSms) {
        return false;
    }

    public boolean allowFwdWapPushMsg() {
        Log.d(TAG, "allowFwdWapPushMsg");
        return true;
    }

    public boolean getIsTitleChangeWhenRecepientsChange() {
        return false;
    }

    public void showNewMessageTitleWithMaxRecipient(ContactList list, AbstractEmuiActionBar mActionBar) {
    }

    public String getRecipientCountStr(ContactList list, Context context) {
        return "";
    }

    public void setHwCustCallback(IHwCustComposeMessageCallback callback) {
    }

    public MessageListAdapter getComposeMessageListAdapter() {
        return null;
    }

    public void setCapabilityFlag(boolean isRcpEditorVisible, List<String> list, Conversation mConversation) {
    }

    public void setSendMessageMode(boolean isRcpEditorVisible, List<String> list) {
    }

    public void initWidget(long mThreadID, MessageListView mMsgListView) {
    }

    public void clearEmptyRcsThread(boolean isSendDiscreetMode, Conversation conv) {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public boolean isSendFileFlagOn() {
        return false;
    }

    public void onCreate(Bundle savedInstanceState) {
    }

    public void onStart() {
    }

    public void onResume(Intent intent) {
    }

    public void onResume() {
    }

    public void onPause4Rcs() {
    }

    public void onStop() {
    }

    public void onDestroy() {
    }

    public void onNewIntent() {
    }

    public void initResourceRefs() {
    }

    public boolean isRcdForRcs(Conversation mConversation) {
        return false;
    }

    public void recordVideo(int requestCode) {
    }

    public void addAttachment(int type) {
    }

    public int getReqCodeForRcs(int requestCode, int resultCode, Conversation mConversation) {
        return requestCode;
    }

    public Intent getVcardDataForRcs(int requestCode, int resultCode, Intent data) {
        return data;
    }

    public boolean takePicForRcs(Conversation mConversation, String filePath) {
        return false;
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data, Conversation mConversation, RichMessageEditor mRichEditor, boolean doAppend) {
        return false;
    }

    public boolean isRCSAction(String action) {
        return false;
    }

    public void resetActionCode(Intent intent) {
    }

    protected boolean isRcsLogin() {
        return false;
    }

    public boolean handleSendIntent(Conversation mConversation, Intent intent, RichMessageEditor richEditor, Handler multiHandler) {
        return false;
    }

    public void initCapabilityFlag() {
    }

    public boolean isNeedShowAddAttachmentForFt() {
        return false;
    }

    public boolean isStopShowAddAttachmentForFt() {
        return false;
    }

    public void initMessageList(MessageListView mMsgListView, MessageListAdapter mMsgListAdapter) {
    }

    public void setMsgItemVisible(MessageItem msgItem) {
    }

    public void setVcardItemVisible(MessageItem msgItem) {
    }

    public boolean saveVcard(MessageItem msgItem) {
        return false;
    }

    public void deleteRcsFtMsg(Uri[] deleteUris, long[] mMsgIds, int mMultyOperType) {
    }

    public void reSendImBySms(MessageItem msgItem) {
    }

    public boolean isChatType(String type) {
        return false;
    }

    public void reSend(MessageItem msgItem) {
    }

    public boolean isLoadMessagesAndDraft() {
        return true;
    }

    public void setFtCapaByNetwork() {
    }

    public boolean getFtCapabilityReqForInsertFile() {
        return false;
    }

    public void setConversationId(MessageListAdapter msgListAdapter, long threadId) {
    }

    public boolean saveFileToPhone(MessageItem msgItem) {
        return false;
    }

    public Uri getDeleteUri(Uri old, long msgId, String type) {
        return old;
    }

    public boolean needDelete(ArrayList<Uri> arrayList, MessageListView listView) {
        return true;
    }

    public boolean deleteRcsMessage(MessageItem messageItem) {
        return false;
    }

    public long getRcsThreadId(long old, MessageItem messageItem) {
        return old;
    }

    public boolean cantUpdateTitle(PeopleActionBarAdapter adapter) {
        return false;
    }

    public String getComposingTitle(ContactList list, String title) {
        return title;
    }

    public void showComposing(String title) {
    }

    public boolean initTextEditorHeight() {
        return false;
    }

    public boolean needUpdateCounter() {
        return true;
    }

    public void onEditTextChange(CharSequence s) {
    }

    public boolean isShowTextCounter() {
        return true;
    }

    public boolean updateSendButtonStateSimple(TextView textView, boolean cardEnabled, boolean readyToSend) {
        return cardEnabled;
    }

    public void updateSendButtonView(View view, boolean enabled) {
    }

    public boolean sendMessage(String debugRecipients) {
        return false;
    }

    public boolean isRcsUIStyle() {
        return false;
    }

    public void switchToEdit(Menu optionMenu, boolean hasMmsItem) {
    }

    public boolean detectMessageToForwardForFt(MessageListView msgListView, Cursor cursor) {
        return false;
    }

    public void onRecipientTextChanged(CharSequence s) {
    }

    public void afterRecipientTextChanged() {
    }

    public void requestCapabilitiesForSubActivity(ContactList recipients) {
    }

    public void requestCapabilitiesOnTextChange() {
    }

    public Uri getGroupMessageUri(Conversation conversation) {
        return null;
    }

    public void updateSendModeToSms() {
    }

    public boolean currentModeisNotIm() {
        return true;
    }

    public void setPeopleActionBar(DynamicActionBar actionBar, PeopleActionBarAdapter adapter) {
    }

    public void handleRecipientEditor() {
    }

    public boolean viewVcardDetail(MessageItem msgItem) {
        return false;
    }

    public boolean checkNeedAppendSignature() {
        return false;
    }

    public boolean handleScrollToPosition(MessageListView listView, int position) {
        return false;
    }

    public boolean isRCSFileTypeInvalid(Context context, int requestCode, Intent data) {
        return true;
    }

    public int getImMsgCount() {
        return 0;
    }

    public void setScrollOnSend(boolean scrollOnSend) {
    }

    public void setSentMessage(boolean sentMessage) {
    }

    public void setSendingMessage(boolean sendingMessage) {
    }

    public void toForward(long threadId) {
    }

    public Long[] getSelectedItems(MessageListView mMsgListView, Long[] selectedItems) {
        return selectedItems;
    }

    public void setCompressActivityStart(boolean isStart) {
    }

    public void redirectSendIntent(Intent intent) {
    }

    public boolean rcsRedirectSendIntent(Intent intent) {
        return false;
    }

    public void refreshAttachmentsContent() {
    }

    public boolean rcsMsgHasText(boolean orgMsgHasText) {
        return orgMsgHasText;
    }

    public boolean isDeleteLocks(boolean deleteLockeds, boolean mDeleteLockeds) {
        return false;
    }

    public boolean processAllSelectItem(MessageListView msgListView, MessageListAdapter msgListAdapter, boolean hasMmsItem) {
        return hasMmsItem;
    }

    public MessageItem onOptionsOnlyOneItemSelected(MessageListView msgListView, MessageListAdapter msgListAdapter) {
        return null;
    }

    public boolean isSelectOnlyOneItem(MessageListView msgListView) {
        return false;
    }

    public void sendImBySms(MessageItem msgItem) {
    }

    public String[] getHwCustProjection() {
        return MessageListAdapter.SINGLE_VIEW_PROJECTION;
    }

    public void clearEmptyRcsThread(boolean isSendDiscreetMode, Conversation conv, WorkingMessage workingMessage) {
    }

    public boolean isExitingActivity() {
        return false;
    }

    public void setIsExitingActivity(boolean isExiting) {
    }

    public void forwardLoc(long threadId) {
    }

    public boolean detectMessageToForwardForLoc(MessageListView msgListView, Cursor cursor) {
        return false;
    }

    public void setOnePageSmsText(WorkingMessage workingMessage, RichMessageEditor richEditor) {
    }

    public void correctThreadIdAfterDelMessage(Conversation mConversation) {
    }

    public void prepareFwdMsg(String msgBody) {
    }

    public boolean isFtToMms() {
        return false;
    }

    public void setFtToMmsTag(boolean defaultValue) {
    }

    public boolean isStopToSendMessageOnLTEOnly(int subscription) {
        return false;
    }

    public boolean isSmsToMmsInCTRoaming(Context context) {
        return false;
    }

    public void setVattachInvisible(View vAttach) {
    }

    public boolean sendMmsUnsupportToast() {
        return false;
    }

    public String getSubjectFromHwCust(String msgItemSubject, String subject) {
        return subject;
    }

    public boolean isRcsShouldLoadDraft() {
        return false;
    }

    public boolean isSameNumberForward(Intent intent) {
        return false;
    }

    public void hideSubjectView(RichMessageEditor richEditor, WorkingMessage workingMessage) {
    }

    public boolean supportSmsToEmail() {
        return false;
    }

    public int[] getParamsWithEmail(WorkingMessage workingMessage, CharSequence text, RichMessageEditor mRichEditor) {
        return SmsMessage.calculateLength(text, false);
    }

    public boolean judgeDSDisableByFDN(int subscription) {
        return false;
    }

    public boolean judgeNumberAndRecipientInFDNList(boolean isMms, Object[] numberList, int subscription) {
        return false;
    }

    public void popForFdn(boolean recipientsEditorVisible, Object[] numberArray, ComposeRecipientsView composeRecipientsView, int subscription) {
    }

    public void showFDNToast() {
    }

    public boolean checkBeforeSendMessage(HwRecipientsEditor recipientsEditor, CustComposeCallbackHandler callback) {
        return false;
    }

    public void setFullScreenFlag(boolean isFullScreen) {
    }

    public void configFullScreenIntent(Intent intent) {
    }

    public boolean isGroupChat(Intent intent) {
        return false;
    }

    public boolean isFileItem(MessageItem msgItem) {
        return false;
    }

    public void updateFileDB(MessageItem msgItem, ConversationQueryHandler mBackgroundQueryHandler) {
    }

    public boolean isRcsImMode() {
        return false;
    }

    public boolean hasMmsDraftBeforeSendFt() {
        return false;
    }

    public void resetHasDraftBeforeSendFt() {
    }

    public boolean isShowToastonDataNotEnabled() {
        return false;
    }

    public int getDataNotEnabledToastId(int textId) {
        return textId;
    }

    public boolean showWifiMessageErrorDialog() {
        return false;
    }

    public void initilizeUI(ComposeMessageFragment aComposeMessageFragment, MessageListView aMessageListView, MessageListAdapter aMessageListAdapter, long aThreadId, ConversationActionBarAdapter aActionbarAdapter, ConversationInputManager aConversationInputManager, EmuiMenu aNormalMenu, AbstractEmuiActionBar aActionBarWhenSplit) {
    }

    public void updateSearchMode() {
    }

    public boolean getSearchMode() {
        return false;
    }

    public void hideKeyboard() {
    }

    public void addSearchMenuItem(int aMenuIdSearch, EmuiMenu aNormalMenu) {
    }

    public void updateNormalMenu() {
    }
}
