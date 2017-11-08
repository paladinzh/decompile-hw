package android.support.v7.widget.helper;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.view.View;

class ItemTouchUIUtilImpl$Gingerbread implements ItemTouchUIUtil {
    ItemTouchUIUtilImpl$Gingerbread() {
    }

    private void draw(Canvas c, RecyclerView parent, View view, float dX, float dY) {
        c.save();
        c.translate(dX, dY);
        parent.drawChild(c, view, 0);
        c.restore();
    }

    public void clearView(View view) {
        view.setVisibility(0);
    }

    public void onSelected(View view) {
        view.setVisibility(4);
    }

    public void onDraw(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState != 2) {
            draw(c, recyclerView, view, dX, dY);
        }
    }

    public void onDrawOver(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == 2) {
            draw(c, recyclerView, view, dX, dY);
        }
    }
}
