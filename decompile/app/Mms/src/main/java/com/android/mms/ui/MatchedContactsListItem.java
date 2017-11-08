package com.android.mms.ui;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.gms.R;
import com.huawei.mms.ui.AvatarWidget;

public class MatchedContactsListItem extends AvatarWidget {
    public MatchedContactsListItem(Context context) {
        super(context);
    }

    public MatchedContactsListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected int getContentResId() {
        return R.id.matched_contacts_list_item;
    }
}
