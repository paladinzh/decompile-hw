package com.android.mms.ui;

import android.content.Context;
import com.android.mms.ui.IconListAdapter.IconListItem;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class LayoutSelectorAdapter extends IconListAdapter {
    public LayoutSelectorAdapter(Context context) {
        super(context, R.layout.layout_selector_list, getData(context));
    }

    protected static List<IconListItem> getData(Context context) {
        List<IconListItem> data = new ArrayList(2);
        addItem(data, context.getString(R.string.select_top_text), R.drawable.compose_editor_grey_background);
        addItem(data, context.getString(R.string.select_bottom_text), R.drawable.btn_quick_action_starred);
        return data;
    }

    protected static void addItem(List<IconListItem> data, String title, int resource) {
        data.add(new IconListItem(title, title, resource));
    }
}
