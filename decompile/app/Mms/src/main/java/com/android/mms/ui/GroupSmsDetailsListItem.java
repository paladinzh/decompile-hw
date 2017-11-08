package com.android.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.mms.data.Contact;
import com.android.mms.transaction.SmsMessageSender;
import com.android.mms.ui.MessageItem.DeliveryStatus;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.ui.AvatarWidget;

public class GroupSmsDetailsListItem extends AvatarWidget {
    private GroupSmsDetailsItem mDetailsItem;
    private MessageResendListener mMessageResendListener;
    private TextView mNameView;
    private TextView mNumberView;
    private ImageView mResendButton;
    private AlertDialog mResendDialog;
    private TextView mStatusView;
    private TextView mTimeView;

    private final class MessageResendListener implements OnClickListener {
        private MessageResendListener() {
        }

        public void onClick(View v) {
            if (GroupSmsDetailsListItem.this.mResendDialog == null || !GroupSmsDetailsListItem.this.mResendDialog.isShowing()) {
                GroupSmsDetailsListItem.this.mResendDialog = new Builder(GroupSmsDetailsListItem.this.mContext).setCancelable(true).setTitle(GroupSmsDetailsListItem.this.getResources().getString(R.string.mms_resend_content)).setPositiveButton(R.string.resend, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        GroupSmsDetailsListItem.this.mResendButton.setVisibility(8);
                        GroupSmsDetailsListItem.this.mResendButton.setOnClickListener(null);
                        GroupSmsDetailsListItem.this.resendFailedMsg(GroupSmsDetailsListItem.this.mContext, GroupSmsDetailsListItem.this.mDetailsItem);
                        MLog.d("AvatarWidget", "re-send failed message");
                    }
                }).setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
                GroupSmsDetailsListItem.this.mResendDialog.show();
            }
        }
    }

    public GroupSmsDetailsListItem(Context context) {
        super(context);
    }

    public GroupSmsDetailsListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GroupSmsDetailsListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mNameView = (TextView) findViewById(R.id.name);
        this.mTimeView = (TextView) findViewById(R.id.time);
        this.mNumberView = (TextView) findViewById(R.id.number);
        this.mStatusView = (TextView) findViewById(R.id.status);
        this.mResendButton = (ImageView) findViewById(R.id.resend_button);
    }

    protected int getContentResId() {
        return R.id.group_sms_deatils_list_item;
    }

    public void bind(GroupSmsDetailsItem msgItem) {
        this.mDetailsItem = msgItem;
        Contact contact = Contact.get(this.mDetailsItem.mAddress, false);
        String name = contact.getName();
        String number = contact.getNumber();
        if (TextUtils.isEmpty(name) || TextUtils.equals(name, number)) {
            this.mNumberView.setVisibility(8);
            this.mNameView.setText(number);
        } else {
            this.mNameView.setText(name);
            this.mNumberView.setText(number);
            this.mNumberView.setVisibility(0);
        }
        updateAvatarIcon(number);
        this.mTimeView.setText(buildTime(this.mDetailsItem.mDate));
        setMsgItemStatus();
    }

    private void setMsgItemStatus() {
        int resID = 0;
        int colorId = 0;
        this.mResendButton.setVisibility(8);
        this.mResendButton.setOnClickListener(null);
        this.mStatusView.setVisibility(0);
        switch (this.mDetailsItem.mType) {
            case 2:
                if (this.mDetailsItem.mDeliveryStatus == DeliveryStatus.RECEIVED) {
                    resID = R.string.message_status_delivered;
                    colorId = R.color.text_color_black_sub_1;
                } else {
                    this.mStatusView.setVisibility(8);
                }
                this.mTimeView.setVisibility(0);
                break;
            case 4:
            case 6:
                resID = R.string.message_status_sending;
                colorId = R.color.text_color_black_sub_1;
                this.mTimeView.setVisibility(8);
                break;
            case 5:
                resID = R.string.send_failed;
                colorId = R.color.conversation_list_item_error_msg_color;
                this.mResendButton.setVisibility(0);
                this.mTimeView.setVisibility(0);
                if (this.mMessageResendListener == null) {
                    this.mMessageResendListener = new MessageResendListener();
                }
                this.mResendButton.setOnClickListener(this.mMessageResendListener);
                break;
            default:
                this.mStatusView.setVisibility(8);
                this.mTimeView.setVisibility(0);
                break;
        }
        if (this.mStatusView.getVisibility() == 0) {
            this.mStatusView.setText(this.mContext.getString(resID));
            this.mStatusView.setTextColor(this.mContext.getResources().getColor(colorId));
        }
    }

    public void resendFailedMsg(Context context, GroupSmsDetailsItem msgItem) {
        Context context2 = context;
        try {
            new SmsMessageSender(context2, new String[]{msgItem.mAddress}, msgItem.mBody, msgItem.mThreadID, msgItem.mSubId).sendMessage(msgItem.mThreadID, msgItem.mUID);
        } catch (MmsException e) {
            MLog.e("AvatarWidget", "Failed to send SMS message, threadId=" + msgItem.mThreadID, (Throwable) e);
        }
        if (-1 == SqliteWrapper.delete(context, context.getContentResolver(), ContentUris.withAppendedId(Sms.CONTENT_URI, msgItem.mMsgId), null, null)) {
            MLog.e("AvatarWidget", "SqliteWrapper delete error");
        }
    }
}
