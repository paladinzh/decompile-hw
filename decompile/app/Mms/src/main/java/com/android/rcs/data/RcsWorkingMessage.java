package com.android.rcs.data;

import android.content.Context;
import android.provider.Telephony.Mms.Draft;
import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
import com.android.rcs.RcsCommonConfig;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.util.HwBackgroundLoader;

public class RcsWorkingMessage {

    private static class HwBackGroundLoaderDel {
        private Context mContext;
        private String mWhere;

        public HwBackGroundLoaderDel(Context context, String where) {
            this.mContext = context;
            this.mWhere = where;
        }

        public void postDelete() {
            HwBackgroundLoader.getInst().postTaskDelayed(new Runnable() {
                public void run() {
                    SqliteWrapper.delete(HwBackGroundLoaderDel.this.mContext, HwBackGroundLoaderDel.this.mContext.getContentResolver(), Draft.CONTENT_URI, HwBackGroundLoaderDel.this.mWhere, null);
                }
            }, 0);
        }
    }

    public void setRcsWorkingMessage(Context context) {
    }

    public boolean isHasDraft(boolean hasDraft) {
        return hasDraft;
    }

    public boolean isRcsSwitchOn() {
        return RcsCommonConfig.isRCSSwitchOn();
    }

    public void asyncDeleteDraftMmsMessageCust(WorkingMessage workingMessage, Conversation conv, Context context) {
        if (!(!isRcsSwitchOn() || workingMessage == null || conv == null || context == null || conv.getHwCust() == null)) {
            workingMessage.setmHasMmsDraft(false);
            long threadId = conv.getHwCust().getSmsThreadId(conv, context);
            new HwBackGroundLoaderDel(context, "thread_id" + (threadId > 0 ? " = " + threadId : " IS NULL")).postDelete();
        }
    }
}
