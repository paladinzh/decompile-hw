package com.android.systemui.tuner;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.volume.ZenModePanel;
import com.android.systemui.volume.ZenModePanel.Callback;

public class TunerZenModePanel extends LinearLayout implements OnClickListener {
    private View mButtons;
    private Callback mCallback;
    private ZenModeController mController;
    private View mDone;
    private OnClickListener mDoneListener;
    private boolean mEditing;
    private View mHeaderSwitch;
    private View mMoreSettings;
    private final Runnable mUpdate = new Runnable() {
        public void run() {
            TunerZenModePanel.this.updatePanel();
        }
    };
    private int mZenMode;
    private ZenModePanel mZenModePanel;

    public TunerZenModePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(ZenModeController zenModeController) {
        this.mController = zenModeController;
        this.mHeaderSwitch = findViewById(R.id.tuner_zen_switch);
        this.mHeaderSwitch.setVisibility(0);
        this.mHeaderSwitch.setOnClickListener(this);
        this.mHeaderSwitch.findViewById(16908363).setVisibility(8);
        ((TextView) this.mHeaderSwitch.findViewById(16908310)).setText(R.string.quick_settings_dnd_label);
        this.mZenModePanel = (ZenModePanel) findViewById(R.id.zen_mode_panel);
        this.mZenModePanel.init(zenModeController);
        this.mButtons = findViewById(R.id.tuner_zen_buttons);
        this.mMoreSettings = this.mButtons.findViewById(16908314);
        this.mMoreSettings.setOnClickListener(this);
        ((TextView) this.mMoreSettings).setText(R.string.quick_settings_more_settings);
        this.mDone = this.mButtons.findViewById(16908313);
        this.mDone.setOnClickListener(this);
        ((TextView) this.mDone).setText(R.string.quick_settings_done);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mEditing = false;
    }

    public void setCallback(Callback zenPanelCallback) {
        this.mCallback = zenPanelCallback;
        this.mZenModePanel.setCallback(zenPanelCallback);
    }

    public void onClick(View v) {
        if (v == this.mHeaderSwitch) {
            this.mEditing = true;
            if (this.mZenMode == 0) {
                this.mZenMode = Prefs.getInt(this.mContext, "DndFavoriteZen", 3);
                this.mController.setZen(this.mZenMode, null, "TunerZenModePanel");
                postUpdatePanel();
                return;
            }
            this.mZenMode = 0;
            this.mController.setZen(0, null, "TunerZenModePanel");
            postUpdatePanel();
        } else if (v == this.mMoreSettings) {
            Intent intent = new Intent("android.settings.ZEN_MODE_SETTINGS");
            intent.addFlags(268435456);
            getContext().startActivity(intent);
        } else if (v == this.mDone) {
            this.mEditing = false;
            setVisibility(8);
            this.mDoneListener.onClick(v);
        }
    }

    public boolean isEditing() {
        return this.mEditing;
    }

    public void setZenState(int zenMode) {
        this.mZenMode = zenMode;
        postUpdatePanel();
    }

    private void postUpdatePanel() {
        removeCallbacks(this.mUpdate);
        postDelayed(this.mUpdate, 40);
    }

    public void setDoneListener(OnClickListener onClickListener) {
        this.mDoneListener = onClickListener;
    }

    private void updatePanel() {
        int i;
        int i2 = 0;
        boolean zenOn = this.mZenMode != 0;
        ((Checkable) this.mHeaderSwitch.findViewById(16908311)).setChecked(zenOn);
        ZenModePanel zenModePanel = this.mZenModePanel;
        if (zenOn) {
            i = 0;
        } else {
            i = 8;
        }
        zenModePanel.setVisibility(i);
        View view = this.mButtons;
        if (!zenOn) {
            i2 = 8;
        }
        view.setVisibility(i2);
    }
}
