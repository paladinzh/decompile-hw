package android.support.v17.leanback.widget;

import android.graphics.Rect;
import android.support.v17.leanback.widget.ItemAlignmentFacet.ItemAlignmentDef;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

class ItemAlignmentFacetHelper {
    private static Rect sRect = new Rect();

    ItemAlignmentFacetHelper() {
    }

    static int getAlignmentPosition(View itemView, ItemAlignmentDef facet, int orientation) {
        LayoutParams p = (LayoutParams) itemView.getLayoutParams();
        View view = itemView;
        if (facet.mViewId != 0) {
            view = itemView.findViewById(facet.mViewId);
            if (view == null) {
                view = itemView;
            }
        }
        int alignPos = facet.mOffset;
        if (orientation == 0) {
            if (facet.mOffset >= 0) {
                if (facet.mOffsetWithPadding) {
                    alignPos += view.getPaddingLeft();
                }
            } else if (facet.mOffsetWithPadding) {
                alignPos -= view.getPaddingRight();
            }
            if (facet.mOffsetPercent != -1.0f) {
                alignPos = (int) (((((float) (view == itemView ? p.getOpticalWidth(view) : view.getWidth())) * facet.mOffsetPercent) / 100.0f) + ((float) alignPos));
            }
            if (itemView == view) {
                return alignPos;
            }
            sRect.left = alignPos;
            ((ViewGroup) itemView).offsetDescendantRectToMyCoords(view, sRect);
            return sRect.left - p.getOpticalLeftInset();
        }
        if (facet.mOffset >= 0) {
            if (facet.mOffsetWithPadding) {
                alignPos += view.getPaddingTop();
            }
        } else if (facet.mOffsetWithPadding) {
            alignPos -= view.getPaddingBottom();
        }
        if (facet.mOffsetPercent != -1.0f) {
            alignPos = (int) (((((float) (view == itemView ? p.getOpticalHeight(view) : view.getHeight())) * facet.mOffsetPercent) / 100.0f) + ((float) alignPos));
        }
        if (itemView != view) {
            sRect.top = alignPos;
            ((ViewGroup) itemView).offsetDescendantRectToMyCoords(view, sRect);
            alignPos = sRect.top - p.getOpticalTopInset();
        }
        if ((view instanceof TextView) && facet.isAlignedToTextViewBaseLine()) {
            return alignPos + (-((TextView) view).getPaint().getFontMetricsInt().top);
        }
        return alignPos;
    }
}
