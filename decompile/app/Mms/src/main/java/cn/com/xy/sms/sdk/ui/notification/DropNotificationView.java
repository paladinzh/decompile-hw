package cn.com.xy.sms.sdk.ui.notification;

import android.content.Context;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.google.android.gms.R;
import org.json.JSONArray;

public class DropNotificationView extends BaseNotificationView {
    public DropNotificationView() {
        super(R.layout.duoqu_drop_notification);
    }

    public void bindBtnView(Context ctx, JSONArray actionJsons, int viewType, String msgId) {
        if (actionJsons != null) {
            try {
                if (actionJsons.length() > 1 && viewType == 3) {
                    this.mRemoteViews.setViewVisibility(R.id.duoqu_drop_btn_layout, 0);
                    this.mRemoteViews.setTextViewText(R.id.duoqu_drop_btn_one, BaseNotificationView.getButtonName(actionJsons.optJSONObject(0)));
                    this.mRemoteViews.setTextViewText(R.id.duoqu_drop_btn_two, BaseNotificationView.getButtonName(actionJsons.optJSONObject(1)));
                    this.mRemoteViews.setTextColor(R.id.duoqu_drop_btn_one, ctx.getResources().getColor(R.color.multiselect_button_color_black));
                    this.mRemoteViews.setTextColor(R.id.duoqu_drop_btn_two, ctx.getResources().getColor(R.color.multiselect_button_color_black));
                    setButtonListener(ctx, R.id.duoqu_drop_btn_one, getRequestCode(1), actionJsons.optJSONObject(0), msgId);
                    setButtonListener(ctx, R.id.duoqu_drop_btn_two, getRequestCode(1), actionJsons.optJSONObject(1), msgId);
                    return;
                }
            } catch (Exception e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("DropNotificationView bindBtnView error:" + e.getMessage(), e);
                return;
            }
        }
        if (actionJsons == null || actionJsons.length() <= 0) {
            this.mRemoteViews.setViewVisibility(R.id.duoqu_single_btn, 8);
            return;
        }
        this.mRemoteViews.setViewVisibility(R.id.duoqu_single_btn, 0);
        this.mRemoteViews.setTextViewText(R.id.duoqu_single_btn, BaseNotificationView.getButtonName(actionJsons.optJSONObject(0)));
        setButtonListener(ctx, R.id.duoqu_single_btn, getRequestCode(1), actionJsons.optJSONObject(0), msgId);
    }
}
