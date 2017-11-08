package com.huawei.rcs.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.SearchActivity;
import com.android.mms.ui.twopane.RightPaneComposeMessageFragment;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.util.HwMessageUtils;

public class RcsSearchMsgUtils {
    private static final boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();

    public boolean startOtherActivity(Context context, long rowid, long threadId, int whichTable, String searchString) {
        if (!mIsRcsOn) {
            return false;
        }
        switch (whichTable) {
            case SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE /*200*/:
                if (threadId != -1) {
                    gotoGroupChatComposeMessageActivity(context, rowid, threadId, whichTable, searchString);
                    return true;
                }
                break;
            case 301:
                gotoComposeActivity(context, whichTable, threadId);
                return true;
            case 302:
                gotoGroupChatComposeMessageActivity(context, threadId);
                return true;
        }
        return false;
    }

    public static void gotoGroupChatComposeMessageActivity(Context context, long sourceId, long threadId, int tos, String searchString) {
        Intent onClickIntent = new Intent(context, RcsGroupChatComposeMessageActivity.class);
        onClickIntent.putExtra("fromSearch", true);
        onClickIntent.putExtra("highlight", searchString);
        onClickIntent.putExtra("select_id", sourceId);
        onClickIntent.putExtra("bundle_group_thread_id", threadId);
        if (HwMessageUtils.isSplitOn()) {
            Activity activity = (Activity) context;
            if (activity instanceof ConversationList) {
                HwBaseFragment fragment = new RcsGroupChatComposeMessageFragment();
                fragment.setIntent(onClickIntent);
                ((ConversationList) activity).openRightClearStack(fragment);
                return;
            }
            gotoFromSearchActivity(activity, onClickIntent);
            return;
        }
        context.startActivity(onClickIntent);
    }

    public static void gotoFromSearchActivity(Activity activity, Intent onClickIntent) {
        if (activity instanceof SearchActivity) {
            activity.startActivity(onClickIntent);
        }
    }

    public static void gotoComposeActivity(Context context, int tableToUse, long threadId) {
        Intent onClickIntent = new Intent(context, ComposeMessageActivity.class);
        onClickIntent.putExtra("fromSearch", true);
        if (threadId != -1) {
            onClickIntent.putExtra("thread_id", threadId);
        }
        onClickIntent.putExtra("table_to_use", 100);
        if (HwMessageUtils.isSplitOn()) {
            Activity activity = (Activity) context;
            if (activity instanceof ConversationList) {
                HwBaseFragment fragment = new RightPaneComposeMessageFragment();
                fragment.setIntent(onClickIntent);
                ((ConversationList) activity).openRightClearStack(fragment);
                return;
            }
            gotoFromSearchActivity(activity, onClickIntent);
            return;
        }
        context.startActivity(onClickIntent);
    }

    public static void gotoGroupChatComposeMessageActivity(Context context, long threadId) {
        Intent onClickIntent = new Intent(context, RcsGroupChatComposeMessageActivity.class);
        onClickIntent.putExtra("fromSearch", true);
        onClickIntent.putExtra("bundle_group_thread_id", threadId);
        if (HwMessageUtils.isSplitOn()) {
            Activity activity = (Activity) context;
            if (activity instanceof ConversationList) {
                HwBaseFragment fragment = new RcsGroupChatComposeMessageFragment();
                fragment.setIntent(onClickIntent);
                ((ConversationList) activity).openRightClearStack(fragment);
                return;
            }
            gotoFromSearchActivity(activity, onClickIntent);
            return;
        }
        context.startActivity(onClickIntent);
    }
}
