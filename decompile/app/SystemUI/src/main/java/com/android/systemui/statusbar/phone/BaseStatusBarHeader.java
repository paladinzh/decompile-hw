package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.android.systemui.qs.QSDetailClipper.IDetailsCallback;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSPanel.Callback;
import com.android.systemui.statusbar.policy.NetworkController.EmergencyListener;

public abstract class BaseStatusBarHeader extends RelativeLayout implements EmergencyListener, IDetailsCallback {
    public abstract void setActivityStarter(ActivityStarter activityStarter);

    public abstract void setCallback(Callback callback);

    public abstract void setExpanded(boolean z);

    public abstract void setExpansion(float f);

    public abstract void setListening(boolean z);

    public abstract void setQSPanel(QSPanel qSPanel);

    public abstract void updateEverything();

    public BaseStatusBarHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
