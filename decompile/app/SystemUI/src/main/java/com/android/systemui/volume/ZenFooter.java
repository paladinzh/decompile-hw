package com.android.systemui.volume;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.service.notification.ZenModeConfig;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.policy.ZenModeController.Callback;
import java.util.Objects;

public class ZenFooter extends LinearLayout {
    private static final String TAG = Util.logTag(ZenFooter.class);
    private ZenModeConfig mConfig;
    private final Context mContext;
    private ZenModeController mController;
    private TextView mEndNowButton;
    private ImageView mIcon;
    private final SpTexts mSpTexts;
    private TextView mSummaryLine1;
    private TextView mSummaryLine2;
    private int mZen = -1;
    private final Callback mZenCallback = new Callback() {
        public void onZenChanged(int zen) {
            ZenFooter.this.setZen(zen);
        }

        public void onConfigChanged(ZenModeConfig config) {
            ZenFooter.this.setConfig(config);
        }
    };

    public ZenFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mSpTexts = new SpTexts(this.mContext);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(new ValueAnimator().getDuration() / 2);
        setLayoutTransition(layoutTransition);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mIcon = (ImageView) findViewById(R.id.volume_zen_icon);
        this.mSummaryLine1 = (TextView) findViewById(R.id.volume_zen_summary_line_1);
        this.mSummaryLine2 = (TextView) findViewById(R.id.volume_zen_summary_line_2);
        this.mEndNowButton = (TextView) findViewById(R.id.volume_zen_end_now);
        this.mSpTexts.add(this.mSummaryLine1);
        this.mSpTexts.add(this.mSummaryLine2);
        this.mSpTexts.add(this.mEndNowButton);
    }

    public void init(final ZenModeController controller) {
        this.mEndNowButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                controller.setZen(0, null, ZenFooter.TAG);
            }
        });
        this.mZen = controller.getZen();
        this.mConfig = controller.getConfig();
        this.mController = controller;
        this.mController.addCallback(this.mZenCallback);
        update();
    }

    public void cleanup() {
        this.mController.removeCallback(this.mZenCallback);
    }

    private void setZen(int zen) {
        if (this.mZen != zen) {
            this.mZen = zen;
            update();
        }
    }

    private void setConfig(ZenModeConfig config) {
        if (!Objects.equals(this.mConfig, config)) {
            this.mConfig = config;
            update();
        }
    }

    private boolean isZenPriority() {
        return this.mZen == 1;
    }

    private boolean isZenAlarms() {
        return this.mZen == 3;
    }

    private boolean isZenNone() {
        return this.mZen == 2;
    }

    public void update() {
        CharSequence string;
        boolean isForever;
        CharSequence line2;
        this.mIcon.setImageResource(isZenNone() ? R.drawable.ic_dnd_total_silence : R.drawable.ic_dnd);
        if (isZenPriority()) {
            string = this.mContext.getString(R.string.interruption_level_priority);
        } else if (isZenAlarms()) {
            string = this.mContext.getString(R.string.interruption_level_alarms);
        } else if (isZenNone()) {
            string = this.mContext.getString(R.string.interruption_level_none);
        } else {
            string = null;
        }
        Util.setText(this.mSummaryLine1, string);
        if (this.mConfig == null || this.mConfig.manualRule == null) {
            isForever = false;
        } else {
            boolean z;
            if (this.mConfig.manualRule.conditionId == null) {
                z = true;
            } else {
                z = false;
            }
            isForever = z;
        }
        if (isForever) {
            line2 = this.mContext.getString(17040807);
        } else {
            line2 = ZenModeConfig.getConditionSummary(this.mContext, this.mConfig, this.mController.getCurrentUser(), true);
        }
        Util.setText(this.mSummaryLine2, line2);
    }

    public void onConfigurationChanged() {
        Util.setText(this.mEndNowButton, this.mContext.getString(R.string.volume_zen_end_now));
        this.mSpTexts.update();
    }
}
