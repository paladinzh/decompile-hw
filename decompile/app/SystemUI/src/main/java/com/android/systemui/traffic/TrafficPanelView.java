package com.android.systemui.traffic;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.traffic.TrafficPanelManager.TrafficInfo;
import com.android.systemui.utils.HwLog;

public class TrafficPanelView extends RelativeLayout {
    private int mNormalTextColor;
    private int mWarningTextColor;
    private TextView mealTraffic;
    private TextView residueTraffic;
    private TextView todayTraffic;
    private ImageView trafficImage;

    private void initLayout() {
        this.todayTraffic = (TextView) findViewById(R.id.today_traffic);
        this.residueTraffic = (TextView) findViewById(R.id.rest_traffic);
        this.mealTraffic = (TextView) findViewById(R.id.meal_traffic);
        this.trafficImage = (ImageView) findViewById(R.id.traffic_image);
        this.mNormalTextColor = getResources().getColor(R.color.systemui_expand_traffic_text_color);
        this.mWarningTextColor = getResources().getColor(R.color.systemui_expand_traffic_warning_text_color);
    }

    protected void onFinishInflate() {
        initLayout();
        super.onFinishInflate();
    }

    public TrafficPanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TrafficPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrafficPanelView(Context context) {
        super(context);
    }

    public void setTrafficInfo(TrafficInfo trafficInfo) {
        if (trafficInfo == null) {
            HwLog.e("TrafficPanelView", "trafficInfo == null, and return!");
            return;
        }
        HwLog.i("TrafficPanelView", "setTrafficInfo:" + trafficInfo.toString());
        this.todayTraffic.setText(trafficInfo.today);
        this.todayTraffic.setTextColor(trafficInfo.beyondToday ? this.mWarningTextColor : this.mNormalTextColor);
        this.residueTraffic.setText(trafficInfo.residue);
        TextView textView = this.residueTraffic;
        int i = (trafficInfo.beyondToday && trafficInfo.isBeyondMeal) ? this.mWarningTextColor : this.mNormalTextColor;
        textView.setTextColor(i);
        this.mealTraffic.setText(trafficInfo.total);
        textView = this.mealTraffic;
        i = (trafficInfo.beyondToday && trafficInfo.isBeyondMeal) ? this.mWarningTextColor : this.mNormalTextColor;
        textView.setTextColor(i);
        this.trafficImage.setImageResource(R.drawable.ic_notification_data);
        this.trafficImage.setImageLevel(trafficInfo.trafficImageLevel);
    }
}
