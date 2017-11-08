package com.android.mms.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.mms.data.Contact;
import com.android.mms.data.Contact.UpdateListener;
import com.android.mms.util.LruSoftCache;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.AvatarWidget;
import com.huawei.mms.ui.MultiModeListView.CheckableView;
import com.huawei.mms.util.HwNumberMatchUtils;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimMessageListItem extends AvatarWidget implements CheckableView, UpdateListener {
    private static final int[] CheckedStateSet = new int[]{16842912};
    private TextView mBodyTextView;
    private CheckBox mCheckBox;
    private HashMap<View, OnClickListener> mClickWidgets = new HashMap();
    private Handler mHandler = new Handler();
    private HashMap<View, OnLongClickListener> mLongClickWidgets = new HashMap();
    public View mMessageBlockSuper;
    private MessageItem mMessageItem;
    private TextView mNameView;
    private TextView mTimeView;

    public SimMessageListItem(Context context) {
        super(context);
        MLog.d("SimMessageListItem", "SimMessageListItem");
    }

    public SimMessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        MLog.d("SimMessageListItem", "SimMessageListItem attrs");
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mMessageBlockSuper = findViewById(R.id.message_block);
        this.mBodyTextView = (TextView) findViewById(R.id.text_view);
        this.mNameView = (TextView) findViewById(R.id.name_view);
        this.mCheckBox = (CheckBox) findViewById(R.id.select);
        this.mTimeView = (TextView) findViewById(R.id.time_view);
    }

    public void bind(MessageItem msgItem, int position, LruSoftCache<Integer, Drawable> lruSoftCache) {
        this.mMessageItem = msgItem;
        setLongClickable(false);
        setClickable(false);
        bindCommonMessage();
        refreshDrawableState();
    }

    public void setMsgListItemHandler(Handler handler) {
    }

    private void bindCommonMessage() {
        this.mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        this.mNameView.setText(this.mMessageItem.getNameFromAddress(getContext(), this.mMessageItem.mAddress));
        if (this.mMessageItem.mDate != 0) {
            this.mTimeView.setText(buildTime(this.mMessageItem.mDate));
        }
        CharSequence formattedMessage = this.mMessageItem.getCachedFormattedMessage();
        if (formattedMessage == null) {
            formattedMessage = formatMessage(this.mMessageItem, this.mMessageItem.mBody, this.mMessageItem.mHighlight, this.mMessageItem.mTextContentType);
            this.mMessageItem.setCachedFormattedMessage(formattedMessage);
        }
        this.mBodyTextView.setText(SmileyParser.getInstance().addSmileySpans(formattedMessage, SMILEY_TYPE.MESSAGE_TEXTVIEW));
        this.mMessageItem.setOnPduLoaded(null);
        requestLayout();
    }

    private CharSequence formatMessage(MessageItem msgItem, String body, Pattern highlight, String contentType) {
        SpannableStringBuilder buf = new SpannableStringBuilder();
        if (!TextUtils.isEmpty(body)) {
            if (contentType == null || !"text/html".equals(contentType)) {
                buf.append(body);
            } else {
                buf.append("\n");
                buf.append(Html.fromHtml(body));
            }
        }
        if (highlight != null) {
            Matcher m = highlight.matcher(buf.toString());
            while (m.find()) {
                buf.setSpan(new StyleSpan(1), m.start(), m.end(), 0);
            }
        }
        return buf;
    }

    public void onUpdate(final Contact updated) {
        if (HwNumberMatchUtils.isNumbersMatched(this.mMessageItem.mAddress, updated.getNumber())) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SimMessageListItem.this.mNameView.setText(updated.getName());
                }
            });
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Contact.addListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Contact.removeListener(this);
    }

    public boolean isEditAble() {
        return this.mCheckBox != null && this.mCheckBox.getVisibility() == 0;
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CheckedStateSet);
        }
        return drawableState;
    }

    public boolean isChecked() {
        return this.mCheckBox != null ? this.mCheckBox.isChecked() : false;
    }

    public void setChecked(boolean checked) {
        if (this.mCheckBox != null) {
            this.mCheckBox.setChecked(checked);
            refreshDrawableState();
        }
    }

    public void toggle() {
        setChecked(!isChecked());
        refreshDrawableState();
    }

    public void setEditAble(boolean editAble) {
        if (this.mCheckBox != null) {
            if (editAble) {
                this.mCheckBox.setVisibility(0);
                for (View v : this.mClickWidgets.keySet()) {
                    v.setClickable(false);
                }
                for (View v2 : this.mLongClickWidgets.keySet()) {
                    v2.setLongClickable(false);
                }
                setEnabled(true);
            } else {
                this.mCheckBox.setVisibility(8);
                for (View v22 : this.mClickWidgets.keySet()) {
                    v22.setClickable(true);
                }
                for (View v222 : this.mLongClickWidgets.keySet()) {
                    v222.setLongClickable(true);
                }
                setEnabled(false);
            }
        }
    }

    public void setEditAble(boolean editable, boolean checked) {
        if (!editable) {
            checked = false;
        }
        setChecked(checked);
        setEditAble(editable);
    }

    protected int getContentResId() {
        return R.id.content;
    }

    public View getMessageBlockSuper() {
        return this.mMessageBlockSuper;
    }
}
