package cn.com.xy.sms.sdk.ui.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.UserHandle;
import android.widget.RemoteViews;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class BaseNotificationView {
    public static final int REQUEST_TYPE_BTN_CLICK = 1;
    public static final int REQUEST_TYPE_LAYOUT = 0;
    private static int mRequestBtnClick = 100000;
    private static int mRequestLayoutClick = 300000;
    protected int mLayoutId = -1;
    protected RemoteViews mRemoteViews = null;

    public static int getRequestBtnClick() {
        return mRequestBtnClick;
    }

    public static void setRequestBtnClick(int mRequestBtnClick) {
        mRequestBtnClick = mRequestBtnClick;
    }

    public static int getRequestLayoutClick() {
        return mRequestLayoutClick;
    }

    public static void setRequestLayoutClick(int mRequestLayoutClick) {
        mRequestLayoutClick = mRequestLayoutClick;
    }

    public BaseNotificationView(int layoutId) {
        this.mLayoutId = layoutId;
    }

    public RemoteViews getRemoteViews(Context ctx) {
        if (this.mLayoutId == -1) {
            return null;
        }
        this.mRemoteViews = new RemoteViews(ctx.getPackageName(), this.mLayoutId);
        return this.mRemoteViews;
    }

    public void bindViewData(Context ctx, Bitmap logoBitmap, String contentTitle, String contentText, JSONArray actionJsons, int viewType, String msgId) {
        setContentText(ctx, logoBitmap, contentTitle, contentText);
        bindBtnView(ctx, actionJsons, viewType, msgId);
    }

    protected void bindBtnView(Context ctx, JSONArray actionJsons, int viewType, String msgId) {
    }

    protected void setContentText(Context ctx, Bitmap logoBitmap, String contentTitle, String contentText) {
        this.mRemoteViews.setImageViewBitmap(R.id.duoqu_logo_img, logoBitmap);
        this.mRemoteViews.setTextViewText(R.id.duoqu_content_title, contentTitle);
        this.mRemoteViews.setTextViewText(R.id.duoqu_content_text, contentText);
        this.mRemoteViews.setTextColor(R.id.duoqu_content_title, ctx.getResources().getColor(R.color.multiselect_button_color_black));
        this.mRemoteViews.setTextColor(R.id.duoqu_content_text, ctx.getResources().getColor(R.color.multiselect_button_color_black));
    }

    protected void setButtonListener(Context ctx, int viewId, int requestCode, JSONObject action, String msgId) {
        this.mRemoteViews.setOnClickPendingIntent(viewId, getNotifyActionIntent(ctx, requestCode, action.optString("action_data"), action.optString("action"), action.optString(Constant.KEY_HW_PARSE_TIME), msgId));
    }

    protected int getRequestCode(int requestType) {
        if (requestType == 0) {
            if (getRequestLayoutClick() == 399999) {
                setRequestLayoutClick(300000);
            } else {
                setRequestLayoutClick(getRequestLayoutClick() + 1);
            }
            return getRequestLayoutClick();
        } else if (1 != requestType) {
            return 0;
        } else {
            if (getRequestBtnClick() == 299999) {
                setRequestBtnClick(200000);
            } else {
                setRequestBtnClick(getRequestBtnClick() + 1);
            }
            return getRequestBtnClick();
        }
    }

    protected PendingIntent getNotifyActionIntent(Context context, int id, String actionData, String actionType, String hwParseTime, String msgId) {
        if (StringUtils.isNull(actionData)) {
            return null;
        }
        Intent contentIntent = new Intent();
        contentIntent.setClassName(context, DoActionActivity.class.getName());
        contentIntent.putExtra("action_data", actionData);
        contentIntent.putExtra("action_type", actionType);
        contentIntent.putExtra(Constant.KEY_HW_PARSE_TIME, hwParseTime);
        contentIntent.putExtra("msgId", msgId);
        contentIntent.addFlags(268566528);
        return PendingIntent.getActivityAsUser(context, id, contentIntent, 134217728, null, UserHandle.CURRENT_OR_SELF);
    }

    public static String getButtonName(JSONObject btnDataJson) {
        if (btnDataJson == null) {
            return "";
        }
        String buttonName = btnDataJson.optString("btn_short_name");
        if (StringUtils.isNull(buttonName)) {
            buttonName = btnDataJson.optString("btn_name");
        }
        return buttonName;
    }
}
