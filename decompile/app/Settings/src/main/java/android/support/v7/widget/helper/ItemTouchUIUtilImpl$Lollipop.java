package android.support.v7.widget.helper;

import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.support.v7.recyclerview.R$id;
import android.support.v7.widget.RecyclerView;
import android.view.View;

class ItemTouchUIUtilImpl$Lollipop extends ItemTouchUIUtilImpl$Honeycomb {
    ItemTouchUIUtilImpl$Lollipop() {
    }

    public void onDraw(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (isCurrentlyActive && view.getTag(R$id.item_touch_helper_previous_elevation) == null) {
            Object originalElevation = Float.valueOf(ViewCompat.getElevation(view));
            ViewCompat.setElevation(view, 1.0f + findMaxElevation(recyclerView, view));
            view.setTag(R$id.item_touch_helper_previous_elevation, originalElevation);
        }
        super.onDraw(c, recyclerView, view, dX, dY, actionState, isCurrentlyActive);
    }

    private float findMaxElevation(RecyclerView recyclerView, View itemView) {
        int childCount = recyclerView.getChildCount();
        float max = 0.0f;
        for (int i = 0; i < childCount; i++) {
            View child = recyclerView.getChildAt(i);
            if (child != itemView) {
                float elevation = ViewCompat.getElevation(child);
                if (elevation > max) {
                    max = elevation;
                }
            }
        }
        return max;
    }

    public void clearView(View view) {
        Object tag = view.getTag(R$id.item_touch_helper_previous_elevation);
        if (tag != null && (tag instanceof Float)) {
            ViewCompat.setElevation(view, ((Float) tag).floatValue());
        }
        view.setTag(R$id.item_touch_helper_previous_elevation, null);
        super.clearView(view);
    }
}
