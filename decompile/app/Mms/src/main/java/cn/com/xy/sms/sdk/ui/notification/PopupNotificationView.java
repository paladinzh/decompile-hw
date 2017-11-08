package cn.com.xy.sms.sdk.ui.notification;

import android.content.Context;
import com.google.android.gms.R;
import org.json.JSONArray;

public class PopupNotificationView extends BaseNotificationView {
    public PopupNotificationView() {
        super(R.layout.duoqu_popup_notification);
    }

    public void bindBtnView(Context ctx, JSONArray actionJsons, int viewType, String msgId) {
        if (actionJsons == null || actionJsons.length() <= 0) {
            this.mRemoteViews.setViewVisibility(R.id.duoqu_single_btn, 8);
            return;
        }
        this.mRemoteViews.setViewVisibility(R.id.duoqu_single_btn, 0);
        this.mRemoteViews.setTextViewText(R.id.duoqu_single_btn, BaseNotificationView.getButtonName(actionJsons.optJSONObject(0)));
        this.mRemoteViews.setTextColor(R.id.duoqu_single_btn, ctx.getResources().getColor(R.color.multiselect_button_color_black));
        setButtonListener(ctx, R.id.duoqu_single_btn, getRequestCode(1), actionJsons.optJSONObject(0), msgId);
    }
}
