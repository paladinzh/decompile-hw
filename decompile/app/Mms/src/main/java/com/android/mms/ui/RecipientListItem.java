package com.android.mms.ui;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.gms.R;
import com.huawei.mms.ui.AvatarWidget;

public class RecipientListItem extends AvatarWidget {
    public RecipientListItem(Context context) {
        super(context);
    }

    public RecipientListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecipientListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected int getContentResId() {
        return R.id.recipient_list_item;
    }
}
