package com.android.systemui.recents.events.ui;

import android.util.MutableInt;
import com.android.systemui.recents.events.EventBus.ReusableEvent;

public class StackViewScrolledEvent extends ReusableEvent {
    public final MutableInt yMovement = new MutableInt(0);

    public void updateY(int y) {
        this.yMovement.value = y;
    }
}
