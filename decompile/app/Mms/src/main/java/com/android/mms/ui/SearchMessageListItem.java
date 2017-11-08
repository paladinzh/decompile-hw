package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import com.android.mms.ui.BaseConversationListFragment.SearchCursorAdapter;
import com.google.android.gms.R;
import com.huawei.mms.ui.AvatarWidget;

public class SearchMessageListItem extends AvatarWidget {
    public SearchMessageListItem(Context context) {
        super(context);
    }

    public SearchMessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bind(SearchCursorAdapter adapter, Context context, Cursor cursor, int type) {
        this.mSplitContext = context;
        if (type == 2 || type == 0) {
            adapter.bindHintView(this, context, cursor, type);
        } else {
            adapter.bindMessageView(this, context, cursor);
        }
    }

    protected int getContentResId() {
        if (getItemType() == 2) {
            return R.id.search_title;
        }
        if (getItemType() == 3) {
            return R.id.search_item;
        }
        return -1;
    }
}
