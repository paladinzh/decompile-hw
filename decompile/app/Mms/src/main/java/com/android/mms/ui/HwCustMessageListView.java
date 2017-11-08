package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import com.huawei.cspcommon.ex.AsyncQueryHandlerEx;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HwCustMessageListView {

    public interface IHwCustMessageListViewCallback {
        long getMsgIdFromTypeId(String str);

        String getMsgTypeFromTypeId(String str);
    }

    public HwCustMessageListView(Context context) {
    }

    public void setAllSelectedPosition(boolean selected, MessageListView msgListView) {
    }

    public void setHwCustCallback(IHwCustMessageListViewCallback callback) {
    }

    public IHwCustMessageListViewCallback getHwCustCallback() {
        return null;
    }

    public List<Long> getChatList(HashSet<String> hashSet) {
        return new ArrayList();
    }

    public void deleteRcsMsg(AsyncQueryHandlerEx handler, int token, HashSet<String> hashSet) {
    }

    public long getSmsThreadid(long oldThreadId, List<Long> list) {
        return oldThreadId;
    }

    public void insertExtFav(List<Long> list, List<Long> list2, AsyncQueryHandlerEx handler, int token1, int token2, int recipientSize) {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public Long[] getSelectedItems(MessageListView mMsgListView, Long[] selectedItems) {
        return selectedItems;
    }

    public MessageItem getMessageItemWithIdAssigned(MessageListAdapter adapter, int selectItem, Cursor c, MessageItem msgItem) {
        return msgItem;
    }

    public String updateForwardSubject(String aFwdSubject, String aMsgSubject) {
        return aFwdSubject;
    }
}
