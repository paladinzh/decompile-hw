package com.android.contacts.hap.camcard;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import com.android.contacts.hap.list.ContactMultiselectionAdapter;
import com.android.contacts.list.ContactListItemView;
import com.google.android.gms.R;

public class CCardMultiselectionAdapter extends ContactMultiselectionAdapter {
    public CCardMultiselectionAdapter(Context context) {
        super(context);
    }

    protected void bindSectionHeaderAndDivider(ContactListItemView view, int position, Cursor cursor, boolean isLastItem) {
        Bundle cursorExtra = cursor.getExtras();
        if (cursorExtra != null) {
            int newCount = cursorExtra.getInt("new_card_count", 0);
            int ccardCount = cursorExtra.getInt("ccard_count", 0);
            int newHeaderPos = newCount == 0 ? -1 : 0;
            int ccardHeaderPos = ccardCount == 0 ? -1 : newCount;
            String title = null;
            CharSequence count = null;
            if (isSectionHeaderDisplayEnabled()) {
                if (position == newHeaderPos) {
                    title = this.mContext.getString(R.string.camcard_section_new);
                    count = String.valueOf(newCount);
                } else if (position == ccardHeaderPos) {
                    title = this.mContext.getString(R.string.camcard_card);
                    count = String.valueOf(ccardCount);
                }
            }
            view.setSectionHeader(title);
            view.setCountView(count);
        }
    }
}
