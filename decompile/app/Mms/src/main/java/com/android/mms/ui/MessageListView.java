package com.android.mms.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.SystemClock;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Draft;
import android.provider.Telephony.Sms;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.mms.MmsApp;
import com.android.mms.data.ContactList;
import com.android.mms.data.WorkingMessage;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.twopane.RightPaneComposeMessageFragment;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsMessageListView;
import com.android.rcs.ui.RcsMessageListView.IHwCustMessageListViewCallback;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.SendReq;
import com.google.android.mms.util.PduCache;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.AsyncQueryHandlerEx;
import com.huawei.cspcommon.ex.CheckableRunnable;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.EditableList;
import com.huawei.mms.ui.EmuiListView_V3;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.MultiModeListView.MultiModeClickListener;
import com.huawei.mms.util.FavoritesUtils;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.util.RcsFavoritesUtils;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public final class MessageListView extends EmuiListView_V3 implements EditableList {
    private boolean bIsMultiChoice;
    private boolean mAutoScrool = false;
    private long mDeleteMmsCount = 0;
    private long mDeleteSmsCount = 0;
    private float mDownY = 0.0f;
    private HashMap<Long, MessageItem> mForwardItem = new HashMap();
    private ICustMessageListHodler mHolder;
    private RcsMessageListView mHwCust = null;
    private HwCustMessageListView mHwCustMessageListView;
    private boolean mIsLastVisible = false;
    private boolean mLock;
    private HashSet<Long> mMmsItems = new HashSet();
    private float mMoveY = 0.0f;
    private OnSizeChangedListener mOnSizeChangedListener;
    private HashSet<String> mSelectedMsgItems = new HashSet();
    private long mStartDeleteTime = 0;
    private Uri mTempMmsUri;
    private long mTempThreadId;
    private Toast mToast;
    private int mType = 0;
    private VelocityTracker mVelocityTracker;

    public interface OnSizeChangedListener {
        void onSizeChanged(int i, int i2, int i3, int i4);
    }

    public interface IMessageListHodler {
        Intent createIntent(long j);

        AsyncDialog getAsyncDialog();

        long getConversationId();

        long getFirstRecipientMsgId(long j, long j2);

        HwBaseFragment getFragment();

        AsyncQueryHandlerEx getQueryHandler();

        ContactList getRecipients();

        boolean isGroupConversation();

        void showProgressBar(boolean z);
    }

    public interface ICustMessageListHodler extends IMessageListHodler {
        void prepareFwdMsg(String str);
    }

    private abstract class BaseOperation {
        private HashSet<String> selectedMsgItems = new HashSet();

        protected abstract int getOperationTitle();

        protected abstract void handle();

        public BaseOperation(HashSet<String> selectedMsgItem) {
            this.selectedMsgItems.addAll(selectedMsgItem);
        }

        protected void getMmsAndSmsList(List<Long> idListSms, List<Long> idListMms) {
            getMmsAndSmsList(idListSms, idListMms, false);
        }

        protected void getMmsAndSmsList(List<Long> idListSms, List<Long> idListMms, boolean skipLockMsg) {
            Iterator<String> iterator = this.selectedMsgItems.iterator();
            while (iterator.hasNext()) {
                String sid = (String) iterator.next();
                String type = MessageListView.getMsgTypeFromTypeId(sid);
                if (!skipLockMsg || !sid.contains(",L,")) {
                    if ("mms".equals(type)) {
                        idListMms.add(Long.valueOf(MessageListView.getMsgIdFromTypeId(sid)));
                    } else if ("sms".equals(type)) {
                        idListSms.add(Long.valueOf(getSmsId(sid)));
                    }
                }
            }
        }

        protected void getMmsAndSmsisUnLockList(List<Long> idListSms, List<Long> idListMms, boolean locked) {
            Iterator<String> iterator = this.selectedMsgItems.iterator();
            while (iterator.hasNext()) {
                String sid = (String) iterator.next();
                String type = MessageListView.getMsgTypeFromTypeId(sid);
                if (sid.contains(locked ? ",U," : ",L,")) {
                    if ("mms".equals(type)) {
                        idListMms.add(Long.valueOf(MessageListView.getMsgIdFromTypeId(sid)));
                    } else if ("sms".equals(type)) {
                        idListSms.add(Long.valueOf(getSmsId(sid)));
                    }
                }
            }
        }

        protected void getChatList(List<Long> idListChat, boolean skipLockMsg) {
            Iterator<String> iterator = this.selectedMsgItems.iterator();
            while (iterator.hasNext()) {
                String sid = (String) iterator.next();
                String type = MessageListView.getMsgTypeFromTypeId(sid);
                if (!(skipLockMsg && sid.contains(",L,")) && "chat".equals(type)) {
                    idListChat.add(Long.valueOf(MessageListView.getMsgIdFromTypeId(sid)));
                }
            }
        }

        protected long getSmsId(String sid) {
            if (MessageListView.this.mHolder.isGroupConversation()) {
                return MessageListView.getMsgUIdFromTypeId(sid);
            }
            return MessageListView.getMsgIdFromTypeId(sid);
        }

        protected void clearMmsCache(List<Long> idListMms) {
            for (Long id : idListMms) {
                Uri selectUri = ContentUris.withAppendedId(Mms.CONTENT_URI, id.longValue());
                PduCache.getInstance().purge(selectUri);
                WorkingMessage.removeThumbnailsFromCache(MmsApp.getApplication().getPduLoaderManager().getCachedModel(selectUri));
                MmsApp.getApplication().getPduLoaderManager().removePdu(selectUri);
            }
        }

        protected void doOperation() {
            if (this.selectedMsgItems.size() <= 0) {
                MLog.e("Mms_UI_MsgListView", "Nothing todo for multi-operation");
            } else {
                MessageListView.this.mHolder.getAsyncDialog().runAsync(new Runnable() {
                    public void run() {
                        BaseOperation.this.handle();
                    }
                }, new Runnable() {
                    public void run() {
                        BaseOperation.this.afterHandle();
                    }
                }, getOperationTitle());
            }
        }

        protected void afterHandle() {
            if (MessageListView.this.isInEditMode()) {
                MessageListView.this.exitEditMode();
            }
        }
    }

    private class CustMessageListViewCallback implements IHwCustMessageListViewCallback {
        private CustMessageListViewCallback() {
        }

        public String getMsgTypeFromTypeId(String s) {
            return MessageListView.getMsgTypeFromTypeId(s);
        }

        public long getMsgIdFromTypeId(String s) {
            return MessageListView.getMsgIdFromTypeId(s);
        }
    }

    private class DeleteOperation extends BaseOperation implements OnClickListener {
        private boolean mCheckboxDelLockMsg = false;
        private boolean mDeleteLockMsg = false;
        private int mLockMsgCount = -1;

        public DeleteOperation(HashSet<String> selectedMsgItem) {
            super(selectedMsgItem);
        }

        private int getLockedMsgCount() {
            int cnt = 0;
            for (String sid : MessageListView.this.mSelectedMsgItems) {
                if (sid.contains(",L,")) {
                    cnt++;
                }
            }
            return cnt;
        }

        protected void doOperation() {
            confirmDeleteMessages();
        }

        private void doOperation(boolean lock) {
            this.mDeleteLockMsg = lock;
            super.doOperation();
        }

        protected int getOperationTitle() {
            return R.string.deleting_messages_Toast;
        }

        private String getSelectCondition(List<Long> idList, boolean fastMode, boolean isSms, boolean deleteLockeds) {
            StringBuilder selection;
            if (isSms) {
                selection = new StringBuilder(" group_id IN ( ").append(idList.get(0));
            } else {
                selection = new StringBuilder(" _id IN ( ").append(idList.get(0));
            }
            for (int i = 1; i < idList.size(); i++) {
                selection.append(", ").append(idList.get(i));
            }
            selection.append(" ) ");
            if (!deleteLockeds) {
                selection.append(" AND locked=0");
            }
            if (fastMode) {
                selection.append(" AND 'fastmode'='fastmode'");
            }
            return selection.toString();
        }

        protected void handle() {
            List<Long> idListMms = new ArrayList();
            List<Long> idListSms = new ArrayList();
            List<Long> idListChat = new ArrayList();
            if (MessageListView.this.mHwCust != null) {
                getChatList(idListChat, !this.mDeleteLockMsg);
            }
            getMmsAndSmsList(idListSms, idListMms, !this.mDeleteLockMsg);
            MessageListView.this.mStartDeleteTime = SystemClock.uptimeMillis();
            MessageListView.this.mDeleteSmsCount = (long) idListSms.size();
            MessageListView.this.mDeleteMmsCount = (long) idListMms.size();
            MLog.i("Mms_UI_MsgListView", "DeleteOperation begin delete  mDeleteSmsCount  = " + MessageListView.this.mDeleteSmsCount + " mDeleteMmsCount = " + MessageListView.this.mDeleteMmsCount);
            if (MessageListView.this.mDeleteSmsCount > 0) {
                Builder builder = Uri.parse("content://sms/conversations/").buildUpon();
                long tmpThreadId = MessageListView.this.mHolder.getConversationId();
                if (!(MessageListView.this.mHwCust == null || MessageListView.this.mHolder.isGroupConversation())) {
                    tmpThreadId = MessageListView.this.mHwCust.getSmsThreadid(tmpThreadId, idListSms);
                }
                MessageListView.this.mHolder.getQueryHandler().startDelete(idListMms.size() > 0 ? 9787 : 9786, null, builder.appendEncodedPath(String.valueOf(tmpThreadId)).build(), getSelectCondition(idListSms, true, MessageListView.this.mHolder.isGroupConversation(), this.mDeleteLockMsg), null);
                clearMmsCache(idListMms);
                SmartSmsSdkUtil.clearSmartSmsCacheData(idListSms);
            }
            if (MessageListView.this.mDeleteMmsCount > 0) {
                MessageListView.this.mHolder.getQueryHandler().startDelete(9786, null, Mms.CONTENT_URI, getSelectCondition(idListMms, true, false, this.mDeleteLockMsg), null);
            }
            if (MessageListView.this.mHwCust != null) {
                MessageListView.this.mHwCust.deleteRcsMsg(MessageListView.this.mHolder.getQueryHandler(), 9786, MessageListView.this.mSelectedMsgItems);
                MessageListView.this.mHwCust.deleteRcsMsgPop(MessageListView.this.mHolder.getQueryHandler(), 9786, idListChat);
            }
        }

        protected void afterHandle() {
            super.afterHandle();
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            switch (whichButton) {
                case -2:
                    doOperation(false);
                    break;
                case -1:
                    StatisticalHelper.incrementReportCount(MessageListView.this.getContext(), 2193);
                    if (!this.mCheckboxDelLockMsg) {
                        doOperation(false);
                        break;
                    } else {
                        doOperation(true);
                        break;
                    }
            }
            dialog.dismiss();
        }

        private void confirmDeleteMessages() {
            String message;
            View contentsView = View.inflate(MessageListView.this.mHolder.getFragment().getContext(), R.layout.delete_thread_dialog_view, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(MessageListView.this.mHolder.getFragment().getContext());
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.delete, this);
            builder.setNegativeButton(R.string.no, null);
            builder.setView(contentsView);
            if (MessageListView.this.isAllChecked()) {
                message = MessageListView.this.getResources().getString(R.string.whether_delete_all_messages);
            } else {
                NumberFormat.getIntegerInstance().setGroupingUsed(false);
                message = MessageListView.this.getResources().getQuantityString(R.plurals.whether_delete_selected_messages_2, MessageListView.this.mSelectedMsgItems.size(), new Object[]{format.format((long) MessageListView.this.mSelectedMsgItems.size())});
            }
            ((TextView) contentsView.findViewById(R.id.tv_deleted_message)).setText(message);
            this.mLockMsgCount = getLockedMsgCount();
            if (this.mLockMsgCount > 0) {
                builder.setView(contentsView);
                final CheckBox checkbox = (CheckBox) contentsView.findViewById(R.id.delete_locked);
                checkbox.setVisibility(0);
                checkbox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        DeleteOperation.this.mCheckboxDelLockMsg = checkbox.isChecked();
                    }
                });
            }
            MessageUtils.setButtonTextColor(builder.show(), -1, MessageListView.this.getResources().getColor(R.color.mms_unread_text_color));
        }
    }

    private class FavoriteOperation extends BaseOperation {
        protected int mInsertMsgCnt = 0;
        protected int mRcsChatItemsCount = 0;

        public FavoriteOperation(HashSet<String> selectedMsgItem) {
            super(selectedMsgItem);
        }

        protected long getSmsId(String sid) {
            long msgId = MessageListView.getMsgIdFromTypeId(sid);
            if (!MessageListView.this.mHolder.isGroupConversation()) {
                return msgId;
            }
            return MessageListView.this.mHolder.getFirstRecipientMsgId(MessageListView.getMsgUIdFromTypeId(sid), msgId);
        }

        protected void handle() {
            int i = 0;
            List<Long> idListMms = new ArrayList();
            List<Long> idListSms = new ArrayList();
            getMmsAndSmsList(idListSms, idListMms);
            List<Long> idListChat = new ArrayList();
            if (MessageListView.this.mHwCust != null) {
                idListChat = MessageListView.this.mHwCust.getChatList(MessageListView.this.mSelectedMsgItems);
                this.mRcsChatItemsCount = idListChat.size();
            }
            MessageListView.this.mHolder.showProgressBar(true);
            int dupcnt = FavoritesUtils.checkAndRemoveDuplicateMsgs(MessageListView.this.mHolder.getFragment().getContext(), idListSms, idListMms);
            if (FavoritesUtils.getHwCust() != null) {
                RcsFavoritesUtils hwCust = FavoritesUtils.getHwCust();
                Context context = MessageListView.this.mHolder.getFragment().getContext();
                if (MessageListView.this.mHolder.getRecipients() != null) {
                    i = MessageListView.this.mHolder.getRecipients().size();
                }
                dupcnt += hwCust.checkAndRemoveDuplicateImMsgs(context, idListChat, i);
            }
            if (dupcnt == -1) {
                this.mInsertMsgCnt = -1;
                return;
            }
            this.mInsertMsgCnt = idListSms.size() + idListMms.size();
            if (MessageListView.this.mHwCust != null && MessageListView.this.mHwCust.isRcsSwitchOn()) {
                this.mInsertMsgCnt += idListChat.size();
                MLog.i("Mms_UI_MsgListView", " mInsertMsgCnt =  " + this.mInsertMsgCnt + " dupcnt = " + dupcnt);
            }
            if (this.mInsertMsgCnt != 0) {
                if (idListSms.size() > 0) {
                    boolean isFirstToken = idListMms.size() > 0;
                    if (MessageListView.this.mHwCust != null && MessageListView.this.mHwCust.isRcsSwitchOn()) {
                        isFirstToken = idListMms.size() + idListChat.size() > 0;
                    }
                    AsyncQueryHandlerEx queryHandler = MessageListView.this.mHolder.getQueryHandler();
                    if (isFirstToken) {
                        i = 9798;
                    } else {
                        i = 9799;
                    }
                    queryHandler.startInsert(i, null, FavoritesUtils.URI_FAV_SMS, FavoritesUtils.getAddFavoritesContent("sms-multy", idListSms));
                }
                if (idListMms.size() > 0) {
                    MessageListView.this.mHolder.getQueryHandler().startInsert(9799, null, FavoritesUtils.URI_FAV_MMS, FavoritesUtils.getAddFavoritesContent("mms-multy", idListMms));
                }
                if (MessageListView.this.mHwCust != null) {
                    MessageListView.this.mHwCust.insertExtFav(idListChat, idListMms, MessageListView.this.mHolder.getQueryHandler(), 9798, 9799, MessageListView.this.mHolder.getRecipients().size());
                }
            }
        }

        protected void afterHandle() {
            if (this.mInsertMsgCnt <= 0) {
                String strRes;
                List<Long> idListMmsBySelected = new ArrayList();
                List<Long> idListSmsBySelected = new ArrayList();
                getMmsAndSmsList(idListSmsBySelected, idListMmsBySelected);
                int spareLeng = (idListSmsBySelected.size() + idListMmsBySelected.size()) + this.mRcsChatItemsCount;
                if (this.mInsertMsgCnt == -1) {
                    strRes = MessageListView.this.getResources().getString(R.string.add_favorite_failed_Toast);
                } else {
                    strRes = MessageListView.this.getResources().getQuantityString(R.plurals.already_in_favorites_Toast_Plurals, spareLeng, new Object[]{Integer.valueOf(spareLeng)});
                }
                Toast.makeText(MessageListView.this.mHolder.getFragment().getContext(), strRes, 0).show();
            }
            super.afterHandle();
        }

        protected int getOperationTitle() {
            return R.string.copy_to_favorites;
        }
    }

    private class LockOrUnlockOperation extends BaseOperation {
        public LockOrUnlockOperation(HashSet<String> selectedMsgItem) {
            super(selectedMsgItem);
        }

        protected void handle() {
            int i;
            int i2 = 1;
            List<Long> idListMms = new ArrayList();
            List<Long> idListSms = new ArrayList();
            getMmsAndSmsisUnLockList(idListSms, idListMms, MessageListView.this.mLock);
            MessageListView.this.mHolder.showProgressBar(true);
            final ContentValues values = new ContentValues(1);
            String str = "locked";
            if (!MessageListView.this.mLock) {
                i2 = 0;
            }
            values.put(str, Integer.valueOf(i2));
            boolean isMultiRecipients = MessageListView.this.mHolder.isGroupConversation();
            if (idListSms.size() > 0) {
                for (i = 0; i < idListSms.size(); i++) {
                    final Uri lockUri = isMultiRecipients ? MessageUtils.CONTENT_URI_WITH_UID : ContentUris.withAppendedId(Sms.CONTENT_URI, ((Long) idListSms.get(i)).longValue());
                    final String str2 = isMultiRecipients ? "group_id='" + idListSms.get(i) + "'" : null;
                    ThreadEx.execute(new CheckableRunnable() {
                        public void run() {
                            SqliteWrapper.update(MessageListView.this.mHolder.getFragment().getContext(), lockUri, values, str2, null);
                        }

                        public long getMaxRunningTime() {
                            return 100;
                        }

                        public String toString() {
                            return "ComposeMessageActivity.lockMessage" + super.toString();
                        }

                        public void onTimeout(long runTime) {
                            MLog.w("Mms_UI_MsgListView", "CMA lockMessage time exceed, use " + runTime);
                        }
                    });
                }
            }
            if (idListMms.size() > 0) {
                for (i = 0; i < idListMms.size(); i++) {
                    lockUri = ContentUris.withAppendedId(Mms.CONTENT_URI, ((Long) idListMms.get(i)).longValue());
                    ThreadEx.execute(new CheckableRunnable() {
                        public void run() {
                            SqliteWrapper.update(MessageListView.this.mHolder.getFragment().getContext(), lockUri, values, null, null);
                        }

                        public String toString() {
                            return "ComposeMessageActivity.lockMessage" + super.toString();
                        }

                        public void onTimeout(long runTime) {
                            MLog.w("Mms_UI_MsgListView", "CMA lockMessage time exceed, use " + runTime);
                        }

                        public long getMaxRunningTime() {
                            return 100;
                        }
                    });
                }
            }
        }

        protected int getOperationTitle() {
            return R.string.copy_to_favorites;
        }
    }

    public class MsgListEditModeClickListener implements MultiModeClickListener {
        public void onItemClickNormal(AdapterView<?> adapterView, View view, int position, long id) {
        }

        public boolean onItemClickEdit(AdapterView<?> adapterView, View view, int position, long id, int mode) {
            MessageListItem v = (MessageListItem) view;
            if (v == null) {
                return true;
            }
            String typedId = MessageListView.this.getMsgTypeId(position);
            MessageItem item = v.getMessageItem();
            if (MessageListView.this.mSelectedMsgItems.contains(typedId)) {
                MessageListView.this.mSelectedMsgItems.remove(typedId);
                if (MessageListView.this.mType == 3) {
                    MessageListView.this.mForwardItem.remove(Long.valueOf(item.mMsgId));
                }
            } else if (MessageListView.this.mType != 3) {
                MessageListView.this.mSelectedMsgItems.add(typedId);
            } else if (item.isSms()) {
                MessageListView.this.mSelectedMsgItems.add(typedId);
                MessageListView.this.mForwardItem.put(Long.valueOf(item.mMsgId), item);
            } else {
                if (MessageListView.this.mToast == null) {
                    MessageListView.this.mToast = Toast.makeText(MessageListView.this.getContext(), R.string.cannot_multi_forward_mms_Toast, 0);
                }
                MessageListView.this.mToast.show();
            }
            return false;
        }
    }

    public void setMsgListHoder(ICustMessageListHodler holder) {
        this.mHolder = holder;
    }

    public boolean isAllSelected() {
        return getRecorder().size() == (getViewMode() == 3 ? getSmsCount() : getCount());
    }

    public boolean isSelected(long itemId) {
        return false;
    }

    public MessageListView(Context context) {
        super(context);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mHwCust = new RcsMessageListView(context);
        }
        if (this.mHwCust != null) {
            this.mHwCust.setHwCustCallback(new CustMessageListViewCallback());
        }
        setTranscriptMode(0);
    }

    public MessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mHwCust = new RcsMessageListView(context);
        }
        if (this.mHwCust != null) {
            this.mHwCust.setHwCustCallback(new CustMessageListViewCallback());
        }
        this.mHwCustMessageListView = (HwCustMessageListView) HwCustUtils.createObj(HwCustMessageListView.class, new Object[]{getContext()});
        setTranscriptMode(0);
    }

    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 31:
                MessageListItem view = (MessageListItem) getSelectedView();
                if (view != null) {
                    MessageItem item = view.getMessageItem();
                    if (item != null && item.isSms()) {
                        ((ClipboardManager) getContext().getSystemService("clipboard")).setText(item.mBody);
                        return true;
                    }
                }
                break;
        }
        return super.onKeyShortcut(keyCode, event);
    }

    public void onDataReload() {
        if (this.bIsMultiChoice) {
            for (int index = 0; index < getChildCount(); index++) {
                MessageListItem listItem = (MessageListItem) getChildAt(index);
                if (3 == this.mType && listItem.getMessageItem().isMms()) {
                    listItem.setCheckBoxEnable(false);
                }
            }
        }
    }

    private boolean isAllChecked() {
        boolean z = true;
        if (3 != this.mType) {
            if (this.mSelectedMsgItems.size() != getCount()) {
                z = false;
            }
            return z;
        } else if (this.mSelectedMsgItems.size() <= 0) {
            return false;
        } else {
            if (this.mSelectedMsgItems.size() != getSmsCount()) {
                z = false;
            }
            return z;
        }
    }

    private void setAllSelected() {
        Cursor c = ((MessageListAdapter) getAdapter()).getCursor();
        if (c != null) {
            int prePos = c.getPosition();
            c.moveToFirst();
            do {
                String typeAndId = getMsgTypeId(c);
                if (3 == this.mType && typeAndId.startsWith("sms")) {
                    MessageItem msgItem = ((MessageListAdapter) getAdapter()).getCachedMessageItemWithIdAssigned("sms", c.getLong(1), c);
                    if (msgItem != null) {
                        this.mForwardItem.put(Long.valueOf(msgItem.mMsgId), msgItem);
                    }
                }
                if (!(3 == this.mType ? typeAndId.startsWith("mms") : false)) {
                    this.mSelectedMsgItems.add(typeAndId);
                }
            } while (c.moveToNext());
            c.moveToPosition(prePos);
        }
    }

    public boolean onMenuItemClick(int type) {
        BaseOperation l = null;
        switch (type) {
            case 278925313:
                if (!isAllChecked()) {
                    setAllSelected();
                    break;
                }
                this.mForwardItem.clear();
                this.mSelectedMsgItems.clear();
                break;
            case 278925315:
                StatisticalHelper.incrementReportCount(getContext(), 2192);
                l = new DeleteOperation(this.mSelectedMsgItems);
                break;
            case 278925318:
                l = new FavoriteOperation(this.mSelectedMsgItems);
                break;
            case 278925331:
                l = new LockOrUnlockOperation(this.mSelectedMsgItems);
                this.mLock = true;
                break;
            case 278925332:
                l = new LockOrUnlockOperation(this.mSelectedMsgItems);
                this.mLock = false;
                break;
        }
        int len = this.mSelectedMsgItems.size();
        if (l == null || this.mSelectedMsgItems.size() <= 0) {
            MLog.e("Mms_UI_MsgListView", "Invalid call " + len);
        } else {
            l.doOperation();
        }
        return true;
    }

    private static String getMsgTypeFromTypeId(String s) {
        return s.substring(0, s.indexOf(44));
    }

    private static long getMsgIdFromTypeId(String s) {
        return Long.parseLong(s.substring(s.lastIndexOf(44) + 1));
    }

    private String getMsgTypeId(Cursor cursor) {
        String str;
        if (cursor.isAfterLast()) {
            cursor.moveToLast();
        }
        long msgId = cursor.getLong(1);
        String type = cursor.getString(0);
        long uID = msgId;
        if (this.mHolder.isGroupConversation()) {
            uID = cursor.getLong(35);
        }
        boolean locked = false;
        boolean isMmsNoti = false;
        if ("sms".equals(type)) {
            locked = cursor.getInt(11) != 0;
        } else if ("mms".equals(type)) {
            locked = cursor.getInt(23) != 0;
            isMmsNoti = cursor.getInt(18) == 130;
        }
        StringBuilder append = new StringBuilder().append(type).append(',');
        if (locked) {
            str = "L";
        } else {
            str = "U";
        }
        append = append.append(str).append(',');
        if (isMmsNoti) {
            str = "N";
        } else {
            str = "D";
        }
        return append.append(str).append(',').append(uID).append(',').append(msgId).toString();
    }

    private String getMsgTypeId(int position) {
        Object itemObj = getItemAtPosition(position);
        if (itemObj != null) {
            return getMsgTypeId((Cursor) itemObj);
        }
        MLog.e("MessageListView", "getMsgTypeId return null obj");
        return "mms,U,0,0";
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mOnSizeChangedListener != null) {
            this.mOnSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }

    void setOnSizeChangedListener(OnSizeChangedListener l) {
        this.mOnSizeChangedListener = l;
    }

    public int getViewMode() {
        return this.mType;
    }

    public boolean isSelected(String type, long msgId, long msgUid, boolean locked) {
        if (!isInEditMode()) {
            return false;
        }
        for (String id : this.mSelectedMsgItems) {
            if (getMsgTypeFromTypeId(id).equals(type) && getMsgIdFromTypeId(id) == msgId) {
                return true;
            }
        }
        return false;
    }

    private static long getMsgUIdFromTypeId(String s) {
        String subString = s.substring(0, s.lastIndexOf(44));
        return Long.parseLong(subString.substring(subString.lastIndexOf(44) + 1));
    }

    public void enterEditMode(int opMode) {
        super.enterEditMode(opMode);
        ((MessageListAdapter) getAdapter()).notifyDataSetChanged();
        if (RcsCommonConfig.isRCSSwitchOn()) {
            setTag("enable-multi-select-move");
        }
    }

    public void exitEditMode() {
        super.exitEditMode();
        this.mSelectedMsgItems.clear();
        ((MessageListAdapter) getAdapter()).notifyDataSetChanged();
        if (RcsCommonConfig.isRCSSwitchOn()) {
            setTag("disable-multi-select-move");
        }
    }

    private int getSmsCount() {
        int smsCount = 0;
        Cursor cursor = ((MessageListAdapter) getAdapter()).getCursor();
        if (cursor == null || cursor.getCount() <= 0) {
            return 0;
        }
        int prePos = cursor.getPosition();
        cursor.moveToFirst();
        do {
            long msgId = cursor.getLong(1);
            String type = cursor.getString(0);
            if ("sms".equals(type)) {
                smsCount++;
            } else if ("mms".equals(type)) {
                this.mMmsItems.add(Long.valueOf(msgId));
            }
        } while (cursor.moveToNext());
        cursor.moveToPosition(prePos);
        return smsCount;
    }

    public int getMessageCount() {
        return getViewMode() == 2 ? getSmsCount() : getCount();
    }

    public void setAllSelected(boolean selected) {
        if (selected) {
            HashSet<Long> newSelected = new HashSet();
            for (int i = 0; i < getCount(); i++) {
                newSelected.add(Long.valueOf(getItemIdAtPosition(i)));
            }
            getRecorder().replace(newSelected);
        } else {
            getRecorder().clear();
        }
        setAllViewsChecked(selected);
    }

    public RcsMessageListView getHwCustMessageListView() {
        return this.mHwCust;
    }

    private void setAllViewsChecked(boolean setChecked) {
        for (int index = 0; index < getChildCount(); index++) {
            boolean isMms;
            MessageListItem listItem = (MessageListItem) getChildAt(index);
            if (3 == this.mType) {
                isMms = listItem.getMessageItem().isMms();
            } else {
                isMms = false;
            }
            if (!isMms) {
                listItem.setChecked(true, true);
            }
        }
    }

    protected MultiModeClickListener getMultiModeClickListener() {
        return new MsgListEditModeClickListener();
    }

    public void setItemSelected(int position) {
        String typedId = getMsgTypeId(position);
        if (this.mSelectedMsgItems.contains(typedId)) {
            this.mSelectedMsgItems.remove(typedId);
            if (this.mType != 3) {
                return;
            }
            return;
        }
        this.mSelectedMsgItems.add(typedId);
    }

    public void addItem(int position) {
        this.mSelectedMsgItems.add(getMsgTypeId(position));
    }

    public void removeItem(int position) {
        this.mSelectedMsgItems.remove(getMsgTypeId(position));
    }

    public String getMsgType(int position) {
        return getMsgTypeFromTypeId(getMsgTypeId(position));
    }

    public boolean isInvalideItemId(long itemId) {
        return itemId == 0;
    }

    public boolean hasMmsNotiSelected() {
        for (String id : this.mSelectedMsgItems) {
            if (id.contains(",N,")) {
                return true;
            }
        }
        return false;
    }

    public boolean hasUnlock() {
        for (String id : this.mSelectedMsgItems) {
            if (id.contains(",U,")) {
                return true;
            }
        }
        return false;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean z = false;
        setAutoScrool(false);
        Adapter adapter = getAdapter();
        if (!(adapter instanceof MessageListAdapter)) {
            return super.dispatchTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case 0:
                if (getLastVisiblePosition() == getCount() - 1) {
                    z = true;
                }
                this.mIsLastVisible = z;
                this.mDownY = ev.getY();
                if (this.mVelocityTracker == null) {
                    this.mVelocityTracker = VelocityTracker.obtain();
                } else {
                    this.mVelocityTracker.clear();
                }
                this.mVelocityTracker.addMovement(ev);
                break;
            case 2:
                this.mMoveY = ev.getY();
                if (this.mVelocityTracker != null && Math.abs(this.mDownY - this.mMoveY) > 20.0f) {
                    this.mVelocityTracker.addMovement(ev);
                    this.mVelocityTracker.computeCurrentVelocity(1000);
                    if (this.mIsLastVisible && this.mVelocityTracker.getYVelocity() < 0.0f) {
                        ((MessageListAdapter) adapter).listScrollAnimation.setVelocity(0.0f);
                        break;
                    }
                    ((MessageListAdapter) adapter).listScrollAnimation.setVelocity(this.mVelocityTracker.getYVelocity());
                    break;
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setAutoScrool(boolean autoScrool) {
        this.mAutoScrool = autoScrool;
    }

    public boolean isAutoScrool() {
        return this.mAutoScrool;
    }

    public void forwardMsg() {
        MessageListAdapter msgListAdapter = (MessageListAdapter) getAdapter();
        Cursor cursor = msgListAdapter.getCursor();
        if (cursor != null) {
            Long[] selectedItems = getRecorder().getAllSelectItems();
            if (this.mHwCust != null) {
                selectedItems = this.mHwCust.getSelectedItems(this, selectedItems);
            }
            int count = selectedItems.length;
            if (count >= 1) {
                if (1 == count) {
                    MessageItem msgItem = msgListAdapter.getCachedMessageItemWithIdAssigned("sms", selectedItems[0].longValue(), cursor);
                    if (this.mHwCust != null) {
                        msgItem = this.mHwCust.getMessageItemWithIdAssigned(msgListAdapter, selectedItems[0].intValue(), cursor, msgItem);
                    }
                    if (msgItem != null) {
                        forwardMessage(msgItem);
                    }
                    return;
                }
                forwardMessages(selectedItems, cursor);
            }
        }
    }

    private String getForwardMsgString(Long[] selectedItems, Cursor cursor, boolean withFrom) {
        if (selectedItems == null || cursor == null) {
            MLog.e("Mms_UI_MsgListView", "selectedItems or cursor is null");
            return "";
        }
        StringBuffer forwardMsgList = new StringBuffer();
        Arrays.sort(selectedItems);
        for (Long itemId : selectedItems) {
            if (itemId.longValue() < 0) {
                return "";
            }
            String forwardString = "";
            MessageItem msgItem = ((MessageListAdapter) getAdapter()).getCachedMessageItemWithIdAssigned("sms", itemId.longValue(), cursor);
            if (this.mHwCust != null) {
                msgItem = this.mHwCust.getMessageItemWithIdAssigned((MessageListAdapter) getAdapter(), itemId.intValue(), cursor, msgItem);
            }
            if (msgItem != null) {
                if (withFrom && PreferenceUtils.getForwardMessageFrom(this.mHolder.getFragment().getContext())) {
                    if (!msgItem.isInComingMessage()) {
                        String mSelf = getResources().getString(R.string.message_sender_from_self);
                        forwardString = getResources().getString(R.string.forward_from, new Object[]{mSelf});
                    } else if (TextUtils.isEmpty(msgItem.mContact)) {
                        forwardString = getResources().getString(R.string.forward_from, new Object[]{msgItem.mAddress});
                    } else {
                        forwardString = getResources().getString(R.string.forward_from, new Object[]{msgItem.mContact});
                    }
                    forwardString = forwardString + System.lineSeparator() + msgItem.mBody;
                } else {
                    forwardString = msgItem.mBody;
                }
                forwardMsgList.append(forwardString + System.lineSeparator() + System.lineSeparator());
            }
        }
        return forwardMsgList.toString();
    }

    private void forwardMessages(Long[] selectedItems, Cursor cursor) {
        if (selectedItems == null || cursor == null) {
            MLog.e("Mms_UI_MsgListView", "selectedItems or cursor is null");
            return;
        }
        Intent intent = this.mHolder.createIntent(0);
        intent.putExtra("exit_on_sent", true);
        intent.putExtra("forwarded_message", true);
        intent.putExtra("forwarded_from_tid", this.mHolder.getConversationId());
        String forwardMsg = MessageUtils.correctForwardMsg(getForwardMsgString(selectedItems, cursor, true));
        intent.putExtra("sms_body", forwardMsg);
        intent.setClassName(this.mHolder.getFragment().getContext(), "com.android.mms.ui.ForwardMessageActivity");
        String cleanForwardMsg = MessageUtils.correctForwardMsg(getForwardMsgString(selectedItems, cursor, false));
        if (this.mHwCust == null || !this.mHwCust.isRcsSwitchOn()) {
            MessageUtils.forwardByChooser(this.mHolder.getFragment().getContext(), intent, cleanForwardMsg, getResources().getString(R.string.forward_message), 115);
        } else {
            this.mHolder.prepareFwdMsg(forwardMsg);
        }
    }

    public void forwardMessage(final MessageItem msgItem) {
        this.mTempThreadId = 0;
        final Context context = this.mHolder.getFragment().getContext();
        this.mHolder.getAsyncDialog().runAsync(new Runnable() {
            public void run() {
                if (msgItem.mType.equals("mms")) {
                    SendReq sendReq = new SendReq();
                    String subject = MessageListView.this.getResources().getString(R.string.forward_prefix);
                    if (msgItem.mSubject != null) {
                        if (msgItem.mSubject.startsWith(subject)) {
                            subject = msgItem.mSubject;
                        } else {
                            subject = subject + msgItem.mSubject;
                        }
                    }
                    if (MessageListView.this.mHwCustMessageListView != null) {
                        subject = MessageListView.this.mHwCustMessageListView.updateForwardSubject(subject, msgItem.mSubject);
                    }
                    if (subject != null) {
                        sendReq.setSubject(new EncodedStringValue(subject));
                    }
                    if (msgItem.mSlideshow != null) {
                        sendReq.setBody(msgItem.mSlideshow.makeCopy());
                    }
                    MessageListView.this.mTempMmsUri = null;
                    try {
                        MessageListView.this.mTempMmsUri = PduPersister.getPduPersister(context).persist(sendReq, Draft.CONTENT_URI, true, PreferenceUtils.getIsGroupMmsEnabled(context), null);
                        MessageListView.this.mTempThreadId = MessagingNotification.getThreadId(context, MessageListView.this.mTempMmsUri);
                    } catch (MmsException e) {
                        MLog.e("Mms_UI_MsgListView", "Failed to copy message: " + msgItem.mMessageUri);
                        Activity activity = MessageListView.this.mHolder.getFragment().getActivity();
                        final Context context = context;
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(context, R.string.cannot_save_message_Toast, 0).show();
                            }
                        });
                    }
                }
            }
        }, new Runnable() {
            public void run() {
                Intent intent = MessageListView.this.mHolder.createIntent(0);
                intent.putExtra("forwarded_message", true);
                if (MessageListView.this.mTempThreadId > 0) {
                    intent.putExtra("thread_id", MessageListView.this.mTempThreadId);
                }
                intent.putExtra("forwarded_from_tid", MessageListView.this.mHolder.getConversationId());
                String forwardString = "";
                if (msgItem.mType.equals("sms") || msgItem.isRcsChat()) {
                    if (1 == msgItem.mBoxId && PreferenceUtils.getForwardMessageFrom(context)) {
                        if (TextUtils.isEmpty(msgItem.mContact)) {
                            forwardString = MessageListView.this.getResources().getString(R.string.forward_from, new Object[]{msgItem.mAddress});
                        } else {
                            forwardString = MessageListView.this.getResources().getString(R.string.forward_from, new Object[]{msgItem.mContact});
                        }
                        forwardString = forwardString + System.lineSeparator() + msgItem.mBody;
                    } else {
                        forwardString = msgItem.mBody;
                    }
                    intent.putExtra("sms_body", forwardString);
                } else {
                    intent.putExtra("msg_uri", MessageListView.this.mTempMmsUri);
                    String subject = MessageListView.this.getResources().getString(R.string.forward_prefix);
                    if (msgItem.mSubject != null) {
                        if (msgItem.mSubject.startsWith(subject)) {
                            subject = msgItem.mSubject;
                        } else {
                            subject = subject + msgItem.mSubject;
                        }
                    }
                    if (MessageListView.this.mHwCustMessageListView != null) {
                        subject = MessageListView.this.mHwCustMessageListView.updateForwardSubject(subject, msgItem.mSubject);
                    }
                    if (subject != null) {
                        intent.putExtra("subject", subject);
                    }
                }
                if (MessageListView.this.mHwCust == null || !MessageListView.this.mHwCust.isRcsSwitchOn() || ((msgItem.mType.equals("sms") && !msgItem.isRcsChat()) || msgItem.isMms())) {
                    intent.setClassName(context, "com.android.mms.ui.ForwardMessageActivity");
                    Activity activity;
                    if (msgItem.isMms()) {
                        intent.putExtra("is_forward_mms", true);
                        if (HwMessageUtils.isSplitOn()) {
                            activity = MessageListView.this.mHolder.getFragment().getActivity();
                            if (activity instanceof ConversationList) {
                                HwBaseFragment fragment = new RightPaneComposeMessageFragment();
                                fragment.setIntent(intent);
                                ((ConversationList) activity).openRightClearStack(fragment);
                                ((ConversationList) activity).showLeftCover();
                                return;
                            }
                            MessageListView.this.mHolder.getFragment().startActivityForResult(intent, 115);
                            return;
                        }
                        MessageListView.this.mHolder.getFragment().startActivityForResult(intent, 115);
                        return;
                    }
                    intent.putExtra("is_forward_sms", true);
                    MessageUtils.forwardByChooser(context, intent, msgItem.mBody, MessageListView.this.getResources().getString(R.string.forward_message), 115);
                    activity = (Activity) context;
                    if (activity instanceof ConversationList) {
                        ((ConversationList) activity).showLeftCover();
                        return;
                    }
                    return;
                }
                MessageListView.this.mHolder.prepareFwdMsg(forwardString);
            }
        }, R.string.building_slideshow_title);
    }

    public void replyMessageToAll(MessageItem msgItem) {
        Intent intent = this.mHolder.createIntent(0);
        intent.putExtra("forwarded_message", true);
        intent.putExtra("replied_mms_to_all", true);
        if (!msgItem.mType.equals("sms")) {
            SendReq sendReq = new SendReq();
            List<String> recList = new ArrayList();
            recList.add(msgItem.mAddress);
            addToRecList(msgItem.getTo(), recList);
            addToRecList(msgItem.getCc(), recList);
            intent.putExtra("recipients", ContactList.getByNumbers(recList, false).serialize());
            intent.putExtra("subject", msgItem.mSubject);
            try {
                intent.putExtra("msg_uri", PduPersister.getPduPersister(this.mHolder.getFragment().getContext()).persist(sendReq, Draft.CONTENT_URI, true, PreferenceUtils.getIsGroupMmsEnabled(this.mHolder.getFragment().getContext()), null));
                intent.setClassName(this.mHolder.getFragment().getContext(), "com.android.mms.ui.ForwardMessageActivity");
                this.mHolder.getFragment().startActivity(intent);
            } catch (MmsException e) {
                MLog.e("Mms_UI_MsgListView", "Failed to copy message: " + msgItem.mMessageUri, (Throwable) e);
                Toast.makeText(this.mHolder.getFragment().getContext(), R.string.cannot_save_message_Toast, 0).show();
            }
        }
    }

    private void addToRecList(EncodedStringValue[] recArray, List<String> recList) {
        MessageUtils.setLocalNumber(null);
        for (EncodedStringValue each : recArray) {
            if (!MessageUtils.isLocalNumber(each.getString())) {
                recList.add(each.getString());
            }
        }
    }

    public long getDeleteStartTime() {
        return this.mStartDeleteTime;
    }

    public long getDeleteSmsCount() {
        return this.mDeleteSmsCount;
    }

    public long getDeleteMmsCount() {
        return this.mDeleteMmsCount;
    }

    public void clearAllModelChangeObserversInDescendants() {
        int childernCount = getChildCount();
        for (int i = 0; i < childernCount; i++) {
            View child = getChildAt(0);
            if (child instanceof MessageListItem) {
                ((MessageListItem) child).clearModelChangeObservers();
            }
        }
    }
}
