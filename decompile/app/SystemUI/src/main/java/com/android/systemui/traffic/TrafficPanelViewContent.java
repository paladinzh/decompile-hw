package com.android.systemui.traffic;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.traffic.TrafficPanelManager.TrafficInfo;
import com.android.systemui.traffic.TrafficPanelManager.TrafficPanelChangeListener;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;

public class TrafficPanelViewContent extends ExpandableView implements TrafficPanelChangeListener {
    private View mTip;
    private TrafficPanelView mTrafficPanelView;
    private TextView mTrafficTip;

    public TrafficPanelViewContent(Context context) {
        this(context, null);
    }

    public TrafficPanelViewContent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void performRemoveAnimation(long duration, float translationDirection, Runnable onFinishedRunnable) {
    }

    public void performAddAnimation(long delay, long duration) {
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    protected void onFinishInflate() {
        HwLog.i("TrafficPanelViewContent", "onFinishInflate");
        super.onFinishInflate();
        this.mTrafficPanelView = (TrafficPanelView) findViewById(R.id.traffic_info);
        this.mTip = findViewById(R.id.content_tip);
        this.mTrafficTip = (TextView) findViewById(R.id.traffic_tip);
    }

    public void onShowStatusChange(boolean show) {
        HwLog.i("TrafficPanelViewContent", "onShowStatusChange show:" + show);
        setVisibility(show ? 0 : 8);
    }

    public void onNoSims() {
        HwLog.i("TrafficPanelViewContent", "onNoSims");
        setNormalTrafficViewShowOrNot(false);
        this.mTrafficTip.setText(R.string.traffic_tip_nosim);
    }

    public void onNoMealSetted(int subId) {
        HwLog.i("TrafficPanelViewContent", "onNoMealSetted subId:" + subId);
        setNormalTrafficViewShowOrNot(false);
        this.mTrafficTip.setText(SystemUiUtil.isOwner() ? R.string.traffic_tip_setmeal : R.string.traffic_tip_setmeal_subuser);
    }

    public void refreshDataBySlot(TrafficInfo trafficInfo) {
        setNormalTrafficViewShowOrNot(true);
        this.mTrafficPanelView.setTrafficInfo(trafficInfo);
    }

    private void setNormalTrafficViewShowOrNot(boolean showNormalTrafficInfo) {
        HwLog.i("TrafficPanelViewContent", "setNormalTrafficViewShowOrNot showNormalTrafficInfo:" + showNormalTrafficInfo);
        if (showNormalTrafficInfo) {
            this.mTrafficPanelView.setVisibility(0);
            this.mTip.setVisibility(8);
            return;
        }
        this.mTrafficPanelView.setVisibility(8);
        this.mTip.setVisibility(0);
    }

    public int getVerticalFadingEdgeLength() {
        return super.getVerticalFadingEdgeLength();
    }

    protected void updateClipping() {
        if (this.mClipToActualHeight) {
            this.mClipRect.set(0, getClipTopAmount(), getWidth(), getActualHeight() + getExtraBottomPadding());
            setClipBounds(this.mClipRect);
            return;
        }
        setClipBounds(null);
    }
}
