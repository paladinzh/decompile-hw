package cn.com.xy.sms.sdk.ui.popu.widget;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import org.json.JSONObject;

public class DuoquBaseViewHolder {
    private static final String CONTENT_SINGLE = "1";
    public TextView leftContentView;
    public TextView leftTitleView;
    public Context mContext;
    public View midView;
    public TextView rightContentView;
    public TextView rightTitleView;

    public DuoquBaseViewHolder(Context context) {
        this.mContext = context;
    }

    public void setContent(int pos, BusinessSmsMessage message, String dataKey, boolean isReBind) {
        try {
            JSONObject jsobj = (JSONObject) message.getTableData(pos, dataKey);
            String leftContentText = (String) JsonUtil.getValFromJsonObject(jsobj, "t2");
            this.leftTitleView.setText((String) JsonUtil.getValFromJsonObject(jsobj, "t1"));
            this.leftContentView.setText(leftContentText);
            String rightTitleText = (String) JsonUtil.getValFromJsonObject(jsobj, "t3");
            String rightContentText = (String) JsonUtil.getValFromJsonObject(jsobj, "t4");
            this.rightTitleView.setText(rightTitleText);
            this.rightContentView.setText(rightContentText);
            if ("1".equals((String) JsonUtil.getValFromJsonObject(jsobj, "z1")) || StringUtils.isNull(rightTitleText)) {
                this.rightTitleView.setVisibility(8);
                this.rightContentView.setVisibility(8);
                this.midView.setVisibility(8);
            } else {
                this.rightTitleView.setVisibility(0);
                this.rightContentView.setVisibility(0);
                this.midView.setVisibility(0);
            }
            setViewStyle(jsobj, message);
        } catch (Throwable ex) {
            LogManager.e("xiaoyuan", ex.getMessage(), ex);
        }
    }

    private void setViewStyle(JSONObject jsobj, BusinessSmsMessage message) {
        try {
            ThemeUtil.setTextColor(this.mContext, this.leftTitleView, (String) message.getValue("v_by_u_color1"), ThemeUtil.getColorId(5010));
            ThemeUtil.setTextColor(this.mContext, this.rightTitleView, (String) message.getValue("v_by_u_color1"), ThemeUtil.getColorId(5010));
            ThemeUtil.setTextColor(this.mContext, this.leftContentView, (String) message.getValue("v_by_d_color1"), ThemeUtil.getColorId(3010));
            ThemeUtil.setTextColor(this.mContext, this.rightContentView, (String) message.getValue("v_by_d_color1"), ThemeUtil.getColorId(3010));
            this.leftTitleView.setTextSize(0, (float) ContentUtil.getVerticalTableTitleTextSize());
            this.rightTitleView.setTextSize(0, (float) ContentUtil.getVerticalTableTitleTextSize());
            this.leftContentView.setTextSize(0, (float) ContentUtil.getVerticalTableContentTextSize());
            this.rightContentView.setTextSize(0, (float) ContentUtil.getVerticalTableContentTextSize());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setVisibility(int visibility) {
        try {
            this.leftTitleView.setVisibility(visibility);
            this.leftContentView.setVisibility(visibility);
            this.rightTitleView.setVisibility(visibility);
            this.rightContentView.setVisibility(visibility);
            this.midView.setVisibility(visibility);
            if (this.leftContentView.getTag(R.id.tag_parent_layout) != null) {
                ((RelativeLayout) this.leftContentView.getTag(R.id.tag_parent_layout)).setVisibility(visibility);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
