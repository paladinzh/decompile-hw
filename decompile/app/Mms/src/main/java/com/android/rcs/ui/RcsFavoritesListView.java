package com.android.rcs.ui;

import android.content.Context;
import com.android.mms.ui.FavoritesListView;
import com.android.rcs.RcsCommonConfig;
import java.util.HashSet;

public class RcsFavoritesListView {
    public RcsFavoritesListView(Context context) {
    }

    public void setAllSelectedPosition(boolean selected, FavoritesListView msgListView) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            if (selected) {
                HashSet<Integer> newSelected = new HashSet();
                for (int i = 0; i < msgListView.getCount(); i++) {
                    newSelected.add(Integer.valueOf(i));
                }
                msgListView.getRecorder().getRcsSelectRecorder().replacePosition(newSelected);
            } else {
                msgListView.getRecorder().getRcsSelectRecorder().clearPosition();
            }
        }
    }
}
