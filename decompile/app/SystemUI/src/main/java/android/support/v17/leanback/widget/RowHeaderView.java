package android.support.v17.leanback.widget;

import android.content.Context;
import android.support.v17.leanback.R$attr;
import android.util.AttributeSet;
import android.widget.TextView;

public final class RowHeaderView extends TextView {
    public RowHeaderView(Context context) {
        this(context, null);
    }

    public RowHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, R$attr.rowHeaderStyle);
    }

    public RowHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
