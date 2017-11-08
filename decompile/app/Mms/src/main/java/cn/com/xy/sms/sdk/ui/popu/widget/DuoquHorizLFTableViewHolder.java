package cn.com.xy.sms.sdk.ui.popu.widget;

import android.content.Context;
import android.widget.RelativeLayout;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;

public class DuoquHorizLFTableViewHolder extends DuoquBaseViewHolder {
    public RelativeLayout mUsedLayout;

    public DuoquHorizLFTableViewHolder(Context context) {
        this.mContext = context;
    }

    public void setContent(int pos, BusinessSmsMessage message, String dataKey, boolean isReBind) {
        try {
            super.setContent(pos, message, dataKey, isReBind);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setVisibility(int visibility) {
        try {
            if (this.mUsedLayout != null) {
                this.mUsedLayout.setVisibility(visibility);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
