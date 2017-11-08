package com.android.mms.ui;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.util.ResEx;

public class CryptoConversationListItem {
    private ImageView mEncryptSmsImg;

    private ImageView createEncryptSmsImg(ConversationListItem conversationListItem) {
        if (this.mEncryptSmsImg != null) {
            return this.mEncryptSmsImg;
        }
        ViewStub stub = (ViewStub) conversationListItem.findViewById(R.id.encrypt_sms_view);
        if (stub == null) {
            MLog.d("HwCustConversationListItemImpl", "can not find encrypt_sms_view !");
            return null;
        }
        stub.setLayoutResource(R.layout.encrypt_sms_image_view);
        return (ImageView) stub.inflate();
    }

    public void updateEncryptSmsImgVisible(ConversationListItem conversationListItem, String messageBody, TextView subjectView, boolean hasError) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            this.mEncryptSmsImg = createEncryptSmsImg(conversationListItem);
            if (this.mEncryptSmsImg == null) {
                MLog.d("HwCustConversationListItemImpl", "createEncryptSmsImg error !");
                return;
            }
            boolean isVisible = !TextUtils.isEmpty(messageBody);
            if (isVisible) {
                int eType = CryptoMessageServiceProxy.getEncryptedType(messageBody);
                isVisible = (4 == eType || 3 == eType) ? true : 2 == eType;
            }
            if (isVisible) {
                this.mEncryptSmsImg.setVisibility(0);
                CharSequence messageBody2 = conversationListItem.getContext().getString(hasError ? R.string.mms_has_error_message : R.string.sms_encrypt_info);
                SpannableStringBuilder subjectText = new SpannableStringBuilder();
                SmileyParser.getInstance().addSmileySpans(messageBody2, SMILEY_TYPE.CONV_LIST_TEXTVIEW, subjectText);
                ResEx.setMarqueeText(subjectView, subjectText);
            } else {
                this.mEncryptSmsImg.setVisibility(8);
            }
        }
    }
}
