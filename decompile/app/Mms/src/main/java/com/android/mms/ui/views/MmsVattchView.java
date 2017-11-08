package com.android.mms.ui.views;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.mms.model.SlideModel;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.ResEx;

public class MmsVattchView extends MmsPopView {
    private ImageView mVattchImage = null;
    private TextView mVattchName = null;
    private LinearLayout mVattchNodesLayout = null;

    public MmsVattchView(Context context) {
        super(context);
    }

    public MmsVattchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MmsVattchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mVattchImage = (ImageView) findViewById(R.id.vattch_image);
        this.mVattchName = (TextView) findViewById(R.id.vattch_name);
        this.mVattchNodesLayout = (LinearLayout) findViewById(R.id.vattch_nodes);
    }

    public void setDetail(String[] details) {
        boolean z = false;
        if (this.mMessageItem == null) {
            MLog.e("VattchView", "vattch view set detail, but mMessageItem is null");
            return;
        }
        int maxWidth = (((MessageUtils.getWindowWidthPixels(getResources()) - (((int) getResources().getDimension(R.dimen.message_block_margin_screen)) * 2)) - (((int) getResources().getDimension(R.dimen.message_block_padding_start_end)) * 2)) - ((int) getResources().getDimension(R.dimen.vattch_nodes_margin_start))) - ((int) getResources().getDimension(R.dimen.vattch_image_width));
        if (this.mMmsPopViewClickCallback != null && this.mMmsPopViewClickCallback.isInEditMode()) {
            maxWidth = (maxWidth - ((int) getResources().getDimension(R.dimen.checkbox_wapper_width))) + ((int) getResources().getDimension(R.dimen.avatar_view_message_list_item_margin_select));
        }
        ResEx self = ResEx.self();
        if (!this.mMessageItem.isInComingMessage()) {
            z = true;
        }
        int textColor = self.getMsgItemTextColor(z);
        int i = 0;
        while (i < details.length && i != 5) {
            if (i == 0) {
                this.mVattchName.setText(details[i]);
                this.mVattchName.setTextColor(textColor);
                this.mVattchName.setMaxWidth(maxWidth);
            } else {
                View child = this.mVattchNodesLayout.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setText(details[i]);
                    ((TextView) child).setTextColor(textColor);
                    ((TextView) child).setMaxWidth(maxWidth);
                } else {
                    TextView node = new TextView(getContext());
                    node.setEllipsize(TruncateAt.MARQUEE);
                    node.setGravity(17);
                    node.setSingleLine(true);
                    node.setEllipsize(TruncateAt.END);
                    node.setGravity(8388611);
                    node.setTextColor(textColor);
                    node.setTextSize(1, 13.0f);
                    node.setText(details[i]);
                    node.setMaxWidth(maxWidth);
                    this.mVattchNodesLayout.addView(node, new LayoutParams(-2, -2));
                }
            }
            i++;
        }
        if (details.length < this.mVattchNodesLayout.getChildCount()) {
            this.mVattchNodesLayout.removeViews(details.length, this.mVattchNodesLayout.getChildCount() - details.length);
        }
    }

    public void bind(MessageItem messageItem) {
        super.bind(messageItem);
    }

    public void onClick(View view) {
        if (this.mMessageItem == null) {
            MLog.e("VattchView", "click vattch view, but mMessageItem is null");
            return;
        }
        SlideModel firstModel = this.mMessageItem.getFirstModel();
        if (firstModel != null) {
            if (!firstModel.hasVcard()) {
                this.mMessageItem.saveVCalendar();
            } else if (this.mMessageItem.mBoxId == 1) {
                this.mMessageItem.saveVcard();
            } else {
                this.mMessageItem.viewVcardDetail();
            }
        }
    }

    public void setVcard(String textSub1, String textSub2) {
        this.mVattchImage.setImageResource(this.mMessageItem.isInComingMessage() ? R.drawable.mms_ic_item_contact_card_recv : R.drawable.mms_ic_item_contact_card_send);
        String[] vcardDetails = textSub2 == null ? new String[]{textSub1} : this.mMessageItem.getVcardDetail();
        changeBackground();
        setDetail(vcardDetails);
        setContentDescription(getContext().getString(R.string.type_vcard));
    }

    public void setVcalendar(String textSub1, String textSub2) {
        this.mVattchImage.setImageResource(this.mMessageItem.isInComingMessage() ? R.drawable.mms_ic_item_calendar_card_recv : R.drawable.mms_ic_item_calendar_card_send);
        String[] vcalendarDetails = this.mMessageItem.getVcalendarDetail();
        if (vcalendarDetails != null) {
            changeBackground();
            setDetail(vcalendarDetails);
        }
        setContentDescription(getContext().getString(R.string.vcalendar_calendar));
    }
}
