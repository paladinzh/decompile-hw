package com.android.mms.ui;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.mms.ui.SpandTextView;

public class HwCustMessageListItem {

    public interface IHwCustMessageListItemCallback {
        ImageView getDeliveredIndicator();

        boolean isDelayMessage();

        void setBodyTextViewVisibility(int i);

        void setDelayMessageStatus(boolean z);
    }

    public HwCustMessageListItem(Context context) {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public boolean bindInCust(MessageItem messageItem, View messageBlock) {
        return false;
    }

    public boolean isSameItem(MessageItem oldMsgItem, MessageItem newMsgItem, boolean isSameItem) {
        return isSameItem;
    }

    public void onResendClick(MessageItem messageItem, ImageView resendBtn) {
    }

    public boolean setStatusText(MessageItem messageItem, TextView messageStatus, ImageView failedIndicator) {
        return false;
    }

    public boolean isFTMsgItem(MessageItem msgItem) {
        return false;
    }

    public boolean setMsgStatus(MessageItem mMessageItem, TextView mMessageStatus) {
        return false;
    }

    public boolean initFailedIndicator(ImageView failedIndicator) {
        return false;
    }

    public void onFinishInflate(MessageListItem listItem) {
    }

    public void bindCommonMessage(SpandTextView mBodyTextView, MessageItem mMessageItem) {
    }

    public void showMmsReportMoreStatus(Context context, MessageItem messageItem, TextView messageStatus) {
    }

    public void setHwCustCallback(IHwCustMessageListItemCallback callback) {
    }

    public IHwCustMessageListItemCallback getHwCustCallback() {
        return null;
    }

    public void setSearchString(String aSearchString) {
    }

    public void highlightWord(TextView aSpanTextView, int aBoxType) {
    }
}
