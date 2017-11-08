package cn.com.xy.sms.sdk.ui.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.Map;
import org.json.JSONArray;

public class DuoquNotificationViewManager {
    public static final int BUTTON_ONE_CLICK_ACTION = 1;
    public static final int BUTTON_TWO_CLICK_ACTION = 2;
    public static final int NOFITY_CLICK_ACTION = 0;
    public static final int TYPE_BIG_CONTENT = 3;
    public static final int TYPE_CONTENT = 2;
    public static final int TYPE_FLOAT = 1;
    private static NotificationManager mNotifyManager = null;

    public static RemoteViews getContentView(Context context, String msgId, String phoneNum, String msg, Map<String, Object> resultMap, Map<String, String> map, Bitmap avatar, int viewType) {
        BaseNotificationView dropView;
        String mTitle = (String) resultMap.get("view_content_title");
        String mText = (String) resultMap.get("view_content_text");
        if (StringUtils.isNull(mTitle) || mTitle.equals("NO_TITLE")) {
            mTitle = phoneNum;
        }
        if (StringUtils.isNull(mText)) {
            mText = msg.trim();
        }
        if (viewType == 1) {
            dropView = new PopupNotificationView();
        } else if (viewType != 2 && viewType != 3) {
            return null;
        } else {
            dropView = new DropNotificationView();
        }
        RemoteViews remoteView = dropView.getRemoteViews(context);
        if (remoteView != null) {
            dropView.bindViewData(context, avatar, mTitle, mText, getButtonName(resultMap), viewType, msgId);
        }
        return remoteView;
    }

    private static JSONArray getButtonName(Map<String, Object> map) {
        String adAction = (String) map.get("ADACTION");
        try {
            if (!StringUtils.isNull(adAction)) {
                return new JSONArray(adAction);
            }
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquNotificationViewManager.getButtonName ERROR: " + e.getMessage(), e);
        }
        return null;
    }

    private static NotificationManager getNotificationManager(Context context) {
        if (mNotifyManager == null) {
            mNotifyManager = (NotificationManager) context.getSystemService("notification");
        }
        return mNotifyManager;
    }

    public static void cancelNotification(Context context, int cancelId) {
        if (cancelId != 0) {
            getNotificationManager(context).cancel(cancelId);
        }
    }
}
