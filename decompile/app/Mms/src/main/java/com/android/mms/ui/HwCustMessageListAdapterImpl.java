package com.android.mms.ui;

import android.content.Context;
import android.view.View;
import com.android.mms.HwCustMmsConfigImpl;
import com.google.android.gms.R;
import java.util.Arrays;

public class HwCustMessageListAdapterImpl extends HwCustMessageListAdapter {
    private static final String MMS_TYPE = "mms";
    private static final String TAG = "HwCustMessageListAdapterImpl";
    private Integer[] mPositions;
    private String mSearchString = "";

    public HwCustMessageListAdapterImpl(Context context) {
        super(context);
    }

    public void setSearchString(String aSearchString) {
        if (HwCustMmsConfigImpl.getSupportSearchConversation()) {
            this.mSearchString = aSearchString;
        }
    }

    public String getSearchString() {
        if (HwCustMmsConfigImpl.getSupportSearchConversation()) {
            return this.mSearchString;
        }
        return "";
    }

    public void setPositionList(Integer[] aPositions) {
        if (!HwCustMmsConfigImpl.getSupportSearchConversation() || aPositions == null) {
            this.mPositions = null;
        } else {
            this.mPositions = (Integer[]) Arrays.copyOf(aPositions, aPositions.length);
        }
    }

    public void highlightMessageListItem(MessageListItem aMessageListItem, int position, String aMsgItem, int aBoxType) {
        if (HwCustMmsConfigImpl.getSupportSearchConversation() && aMessageListItem != null && position != -1 && MMS_TYPE.equals(aMsgItem)) {
            boolean flag = false;
            View v = aMessageListItem.findViewById(R.id.message_block);
            if (!(this.mPositions == null || v == null)) {
                int i = 0;
                while (i < this.mPositions.length) {
                    if (position == this.mPositions[i].intValue()) {
                        if (aBoxType == 2) {
                            v.setBackgroundResource(R.drawable.search_mms_receive);
                        } else {
                            v.setBackgroundResource(R.drawable.search_mms_send);
                        }
                        flag = true;
                    } else {
                        i++;
                    }
                }
            }
            if (!(flag || v == null)) {
                if (aBoxType == 2) {
                    v.setBackgroundResource(R.drawable.message_pop_incoming_bg);
                } else {
                    v.setBackgroundResource(R.drawable.message_pop_send_bg);
                }
            }
        }
    }
}
