package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.mms.data.Contact;
import com.android.mms.data.Contact.UpdateListener;
import com.google.android.gms.R;
import com.huawei.mms.ui.AvatarWidget;
import com.huawei.mms.ui.MultiModeListView.CheckableView;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwNumberMatchUtils;
import com.huawei.mms.util.ResEx;

public class RecyclerSmsListItem extends AvatarWidget implements UpdateListener, CheckableView {
    private CryptoReclerSmsListItem cryptoReclerSmsListItem;
    private CheckBox mCheckBoxView;
    private Context mContext;
    private TextView mDayRemainView;
    private TextView mMsgBodyView;
    private TextView mMsgFromView;
    private Contact mTrashSmsAddress;
    private String mTrashSmsBody;
    private long mTrashSmsId;

    public RecyclerSmsListItem(Context context) {
        super(context);
        this.mContext = context;
    }

    public RecyclerSmsListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public RecyclerSmsListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mMsgFromView = (TextView) findViewById(R.id.recycler_sms_from);
        this.mMsgBodyView = (TextView) findViewById(R.id.recycler_sms_subject);
        this.mDayRemainView = (TextView) findViewById(R.id.recycler_sms_date_leaving);
        this.mCheckBoxView = (CheckBox) findViewById(R.id.recycler_sms_select);
        this.cryptoReclerSmsListItem = new CryptoReclerSmsListItem();
    }

    public void bind(Cursor cursor, boolean isInEditMode, boolean isChecked, long preTime) {
        this.mTrashSmsAddress = Contact.get(cursor.getString(1), false);
        this.mMsgFromView.setText(this.mTrashSmsAddress.getName());
        this.mTrashSmsBody = cursor.getString(2);
        this.mMsgBodyView.setText(this.cryptoReclerSmsListItem.checkForCryptoMessage(this, this.mTrashSmsBody, this));
        long deleteTime = cursor.getLong(3);
        this.mTrashSmsId = cursor.getLong(0);
        int remainedDay = getRemainedDay(deleteTime, preTime);
        this.mDayRemainView.setText(this.mContext.getResources().getQuantityString(R.plurals.sms_trash_remained_day, remainedDay, new Object[]{Integer.valueOf(remainedDay)}));
        if (3 >= remainedDay) {
            this.mDayRemainView.setTextColor(ResEx.self().getConvItemUnreadTextColor());
        } else {
            this.mDayRemainView.setTextColor(ResEx.self().getConvItemNormalTextColor());
        }
        if (isInEditMode) {
            this.mCheckBoxView.setVisibility(0);
            this.mCheckBoxView.setChecked(isChecked);
            return;
        }
        this.mCheckBoxView.setVisibility(8);
    }

    public void onUpdate(Contact updated) {
        if (Contact.isEmailAddress(this.mTrashSmsAddress.getNumber())) {
            if (!this.mTrashSmsAddress.getNumber().equals(updated.getNumber())) {
                return;
            }
        } else if (!HwNumberMatchUtils.isNumbersMatched(this.mTrashSmsAddress.getNumber(), updated.getNumber())) {
            return;
        }
        HwBackgroundLoader.getUIHandler().post(new Runnable() {
            public void run() {
                RecyclerSmsListItem.this.mMsgFromView.setText(RecyclerSmsListItem.this.mTrashSmsAddress.getName());
            }
        });
    }

    protected int getContentResId() {
        return R.id.content;
    }

    private int getRemainedDay(long deleteTime, long preTime) {
        if (-1 == preTime) {
            preTime = System.currentTimeMillis();
        }
        int remainedDay = (int) ((preTime - deleteTime) / 86400000);
        if (remainedDay < 0) {
            return 15;
        }
        if (remainedDay > 15) {
            return 0;
        }
        return 15 - remainedDay;
    }

    public boolean isChecked() {
        return this.mCheckBoxView != null ? this.mCheckBoxView.isChecked() : false;
    }

    public void setChecked(boolean checked) {
        if (this.mCheckBoxView != null) {
            this.mCheckBoxView.setChecked(checked);
        }
    }

    public void toggle() {
        setChecked(!isChecked());
    }

    public void setEditAble(boolean editable) {
        if (this.mCheckBoxView != null) {
            if (editable) {
                this.mCheckBoxView.setVisibility(0);
            } else {
                this.mCheckBoxView.setVisibility(8);
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

    public boolean isEditAble() {
        return this.mCheckBoxView != null && this.mCheckBoxView.getVisibility() == 0;
    }

    public String getSmsBodyText() {
        return this.mTrashSmsBody;
    }

    public Contact getSmsAddress() {
        return this.mTrashSmsAddress;
    }

    public long getSmsMsgId() {
        return this.mTrashSmsId;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Contact.addListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Contact.getChangeMoniter().removeListener(this);
    }

    public boolean needSetBackground() {
        return false;
    }
}
