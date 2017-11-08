package com.android.settings.accessibility;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.internal.widget.SubtitleView;

public class EdgeTypePreference extends ListDialogPreference {
    public EdgeTypePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();
        setValues(res.getIntArray(2131361896));
        setTitles(res.getStringArray(2131361895));
        setDialogLayoutResource(2130968827);
        setListItemLayoutResource(2130969018);
    }

    public boolean shouldDisableDependents() {
        return getValue() != 0 ? super.shouldDisableDependents() : true;
    }

    protected void onBindListItem(View view, int index) {
        SubtitleView preview = (SubtitleView) view.findViewById(2131886519);
        preview.setForegroundColor(-1);
        preview.setBackgroundColor(0);
        preview.setTextSize(32.0f * getContext().getResources().getDisplayMetrics().density);
        preview.setEdgeType(getValueAt(index));
        preview.setEdgeColor(-16777216);
        CharSequence title = getTitleAt(index);
        if (title != null) {
            ((TextView) view.findViewById(2131886387)).setText(title);
        }
    }
}
