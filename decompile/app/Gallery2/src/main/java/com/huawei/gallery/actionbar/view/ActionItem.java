package com.huawei.gallery.actionbar.view;

import android.view.View;
import com.huawei.gallery.actionbar.Action;

public interface ActionItem {
    View asView();

    Action getAction();
}
