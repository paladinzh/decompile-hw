package com.huawei.systemmanager.comm.widget.slideview;

import android.view.View;

public interface PanelSlideListener {

    public static class SimplePanelSlideListener implements PanelSlideListener {
        public void onPanelSlide(View panel, float slideOffset) {
        }

        public void onPanelCollapsed(View panel) {
        }

        public void onPanelExpanded(View panel) {
        }

        public void onPanelAnchored(View panel) {
        }

        public void onPanelHidden(View panel) {
        }
    }

    void onPanelAnchored(View view);

    void onPanelCollapsed(View view);

    void onPanelExpanded(View view);

    void onPanelHidden(View view);

    void onPanelSlide(View view, float f);
}
