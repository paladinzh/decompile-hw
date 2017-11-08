package com.android.rcs.ui;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import com.android.mms.data.Contact;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.mms.ui.AvatarWidget;
import com.huawei.mms.ui.SpandLinkMovementMethod.SpandTouchMonitor;
import com.huawei.mms.ui.SpandTextView;
import com.huawei.mms.util.NumberUtils;
import com.huawei.rcs.utils.RcsProfile;

public class RcsGroupChatNoticeMessageListItem extends AvatarWidget implements SpandTouchMonitor {
    private SpandTextView mBodyTextView;
    private Context mContext;
    private TextView mDateView;
    private RcsGroupChatMessageItem mMessageItem;
    private boolean mNeedShowTimePhase = false;
    private int mNextYear = -1;
    private TextView mTextTimePhase;
    private TextView mTextViewYear;

    public RcsGroupChatNoticeMessageListItem(Context context) {
        super(context);
        this.mContext = context;
    }

    public RcsGroupChatNoticeMessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void bind(RcsGroupChatMessageItem msgItem) {
        this.mMessageItem = msgItem;
        bindCommonMessage();
    }

    private void bindCommonMessage() {
        Log.d("GroupChatEventMessageListItem", "bindCommonMessage bind member event");
        this.mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        String body = this.mMessageItem.mBody;
        if (isNeedShowTimePhase()) {
            this.mTextTimePhase.setVisibility(0);
            this.mTextTimePhase.setText(buildTime(this.mMessageItem.mDate, true));
        } else {
            this.mTextTimePhase.setText(buildTime(this.mMessageItem.mDate, false));
        }
        boolean isCannotConvert = false;
        if (!TextUtils.isEmpty(body)) {
            if (!(this.mMessageItem.mType == 50 || this.mMessageItem.mType == 51 || this.mMessageItem.mType == 52)) {
                if (this.mMessageItem.mType == 53) {
                }
            }
            String[] parts = body.split(" ");
            if (parts.length > 1) {
                String name = updateMembersNameInBody(parts[0]);
                if ("join".equalsIgnoreCase(parts[1])) {
                    body = String.format(this.mContext.getString(R.string.groupchat_member_was_added), new Object[]{name});
                } else if (parts.length <= 2 || !"invited".equalsIgnoreCase(parts[2])) {
                    body = String.format(this.mContext.getString(R.string.groupchat_member_leave), new Object[]{name});
                } else {
                    body = String.format(this.mContext.getString(R.string.init_group_names), new Object[]{name});
                }
                isCannotConvert = true;
            }
        }
        if (256 == this.mMessageItem.mType) {
            body = this.mContext.getString(R.string.groupchat_left_prompt);
        }
        if (255 == this.mMessageItem.mType) {
            body = this.mContext.getString(R.string.create_group_failed);
        }
        if (257 == this.mMessageItem.mType) {
            body = this.mContext.getString(R.string.rcs_group_not_owner);
        }
        if (258 == this.mMessageItem.mType) {
            body = this.mContext.getString(R.string.rcs_group_recover_owner);
        }
        this.mBodyTextView.setText(isCannotConvert ? new SpannableStringBuilder().append(body) : MessageUtils.getHwCust().formatMessage(body));
        this.mBodyTextView.setVisibility(0);
        setTime(this.mMessageItem.mDate, 1);
        if (TextUtils.isEmpty(this.mMessageItem.mTimestamp)) {
            this.mDateView.setVisibility(8);
        } else {
            this.mDateView.setText(this.mMessageItem.mTimeHM);
        }
        if (-1 != this.mNextYear) {
            this.mTextViewYear.setVisibility(0);
            this.mTextViewYear.setText(getContext().getResources().getString(R.string.sms_list_item_current_year, new Object[]{Integer.valueOf(this.mNextYear)}));
            return;
        }
        this.mTextViewYear.setVisibility(8);
    }

    String updateMembersNameInBody(String phoneNumberInBody) {
        StringBuffer sb = new StringBuffer();
        if (!TextUtils.isEmpty(phoneNumberInBody)) {
            for (String member : phoneNumberInBody.split(",")) {
                String member2 = NumberUtils.normalizeNumber(member2);
                Contact contact = Contact.get(member2, true);
                String name = member2;
                if (contact != null) {
                    name = contact.getName();
                    if (!contact.existsInDatabase() && RcsProfile.isGroupChatNicknameEnabled()) {
                        String nickname = RcsProfile.getGroupMemberNickname(member2, (long) this.mMessageItem.mThreadId);
                        if (!TextUtils.isEmpty(nickname)) {
                            name = nickname;
                        }
                    }
                }
                sb.append(name);
                sb.append(this.mContext.getString(R.string.comma_between_numbers));
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mBodyTextView = (SpandTextView) findViewById(R.id.text_view);
        this.mDateView = (TextView) findViewById(R.id.date_view);
        this.mDateView.setOnClickListener(null);
        this.mTextViewYear = (TextView) findViewById(R.id.textview_year);
        this.mTextTimePhase = (TextView) findViewById(R.id.time_phase);
    }

    protected int getContentResId() {
        return R.id.mms_layout_view_super_parent;
    }

    public void onTouchOutsideSpanText() {
    }

    public void onSpanTextPressed(boolean pressed) {
    }

    public boolean isEditTextClickable() {
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public boolean onDoubleTapUp(boolean isLink) {
        return false;
    }

    public void onTouchLink(ClickableSpan span) {
    }

    public void setNeedShowTimePhase(boolean needShowTimePhase) {
        this.mNeedShowTimePhase = needShowTimePhase;
    }

    public boolean isNeedShowTimePhase() {
        return this.mNeedShowTimePhase;
    }
}
