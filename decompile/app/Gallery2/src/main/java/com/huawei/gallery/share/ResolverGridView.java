package com.huawei.gallery.share;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.AbsListView.LayoutParams;
import android.widget.GridView;
import android.widget.ListAdapter;

public class ResolverGridView extends GridView {
    public ResolverGridView(Context context) {
        super(context, null);
    }

    public ResolverGridView(Context context, AttributeSet attrs) {
        super(context, attrs, 16842865);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int maxChildHeight = 0;
        int firstChildHeight = 0;
        int numColumns = getNumColumns();
        ListAdapter adapter = getAdapter();
        int count = adapter == null ? 0 : adapter.getCount();
        if (count > 0) {
            i = 0;
            while (i < count) {
                View child = adapter.getView(i, null, null);
                LayoutParams p = (LayoutParams) child.getLayoutParams();
                if (p == null) {
                    p = (LayoutParams) generateDefaultLayoutParams();
                    child.setLayoutParams(p);
                }
                child.measure(getChildMeasureSpec(MeasureSpec.makeMeasureSpec(getColumnWidth(), 1073741824), 0, p.width), getChildMeasureSpec(MeasureSpec.makeMeasureSpec(0, 0), 0, p.height));
                int singleChildHeight = child.getMeasuredHeight();
                if ((i == count - 1 && count == numColumns - 1) || i == numColumns - 1) {
                    firstChildHeight = singleChildHeight;
                } else if (i >= numColumns && singleChildHeight > maxChildHeight) {
                    maxChildHeight = singleChildHeight;
                }
                i++;
            }
        }
        if (heightMode == Integer.MIN_VALUE) {
            int ourSize = getListPaddingBottom();
            for (i = 0; i < count; i += numColumns) {
                if (i / numColumns == 1) {
                    ourSize += maxChildHeight;
                } else {
                    ourSize += firstChildHeight;
                }
                if (i + numColumns < count) {
                    ourSize += getVerticalSpacing();
                }
                if (ourSize >= heightSize) {
                    ourSize = heightSize;
                    break;
                }
            }
            heightSize = ourSize;
        }
        setMeasuredDimension(getMeasuredWidth(), heightSize);
    }
}
