package com.huawei.gallery.editor.category;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;

public class EditorTextView extends TextView {
    private static final int TEXT_VIEW_EXTRA_LAND = GalleryUtils.dpToPixel(24);
    private static final int TEXT_VIEW_EXTRA_LAND_WIDTH_ACTION = GalleryUtils.dpToPixel(72);
    private static final int TEXT_VIEW_EXTRA_PORT = GalleryUtils.dpToPixel(26);
    private static final int TEXT_VIEW_EXTRA_PORT_WIDTH_ACTION = GalleryUtils.dpToPixel(80);
    private static final int TEXT_VIEW_HEIGHT = GalleryUtils.dpToPixel(36);
    private static final int TEXT_VIEW_INTERVAL_LAND = GalleryUtils.dpToPixel(24);
    private static final int TEXT_VIEW_INTERVAL_PORT = GalleryUtils.dpToPixel(8);
    private static final int TEXT_VIEW_WIDTH_MAX = GalleryUtils.dpToPixel(64);
    private static final int TEXT_VIEW_WIDTH_MIN = GalleryUtils.dpToPixel(52);

    public EditorTextView(Context context) {
        super(context);
    }

    public EditorTextView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public void setAttributes(IconData iconData, int index, int size) {
        setAttributes(iconData, index, size, true, true);
    }

    private int getLeftMargin(int index, int size, boolean withAction, boolean needScroll) {
        int i = 0;
        if (index != 0) {
            return 0;
        }
        int result = withAction ? TEXT_VIEW_EXTRA_PORT_WIDTH_ACTION : TEXT_VIEW_EXTRA_PORT;
        if (!needScroll) {
            i = TEXT_VIEW_EXTRA_PORT;
        }
        return result - i;
    }

    private int getRightMargin(int index, int size, boolean withAction, boolean needScroll) {
        int result = TEXT_VIEW_INTERVAL_PORT;
        if (index != size - 1) {
            return result;
        }
        return (withAction ? TEXT_VIEW_EXTRA_PORT_WIDTH_ACTION : TEXT_VIEW_EXTRA_PORT) - (needScroll ? 0 : TEXT_VIEW_EXTRA_PORT);
    }

    private int getTopMargin(int index, int size, boolean withAction, boolean needScroll) {
        int i = 0;
        if (index != 0) {
            return 0;
        }
        int result = withAction ? TEXT_VIEW_EXTRA_LAND_WIDTH_ACTION : TEXT_VIEW_EXTRA_LAND;
        if (!needScroll) {
            i = TEXT_VIEW_EXTRA_LAND;
        }
        return result - i;
    }

    private int getBottomMargin(int index, int size, boolean withAction, boolean needScroll) {
        int result = TEXT_VIEW_INTERVAL_LAND;
        if (index != size - 1) {
            return result;
        }
        return (withAction ? TEXT_VIEW_EXTRA_LAND_WIDTH_ACTION : TEXT_VIEW_EXTRA_LAND) - (needScroll ? 0 : TEXT_VIEW_EXTRA_LAND);
    }

    public void setAttributes(IconData iconData, int index, int size, boolean withAction, boolean needScroll) {
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        setMaxWidth(TEXT_VIEW_WIDTH_MAX);
        setMinWidth(TEXT_VIEW_WIDTH_MIN);
        setMaxHeight(TEXT_VIEW_HEIGHT);
        setMinHeight(TEXT_VIEW_HEIGHT);
        if (getResources().getConfiguration().orientation == 1) {
            int left = getLeftMargin(index, size, withAction, needScroll);
            int right = getRightMargin(index, size, withAction, needScroll);
            if (GalleryUtils.isLayoutRTL()) {
                int temp = left;
                left = right;
                right = temp;
            }
            layoutParams.setMargins(left, GalleryUtils.dpToPixel(6), right, GalleryUtils.dpToPixel(6));
        } else {
            layoutParams.setMargins(0, getTopMargin(index, size, withAction, needScroll), 0, getBottomMargin(index, size, withAction, needScroll));
        }
        setLayoutParams(layoutParams);
        setGravity(17);
        setId(iconData.getViewId());
        setTextAppearance(getContext(), R.style.EditTextViewStyle);
        if (iconData.getDrawableId() > 0) {
            Drawable drawable = getContext().getResources().getDrawable(iconData.getDrawableId());
            setCompoundDrawablePadding(0);
            setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        }
        if (iconData.getTextId() == 0) {
            setText(iconData.getText());
        } else {
            setText(iconData.getTextId());
        }
        setSingleLine(false);
    }

    public void updateDrawable(int drawableId) {
        setCompoundDrawablesWithIntrinsicBounds(null, getContext().getResources().getDrawable(drawableId), null, null);
    }
}
