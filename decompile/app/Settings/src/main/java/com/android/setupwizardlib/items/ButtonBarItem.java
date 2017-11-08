package com.android.setupwizardlib.items;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.setupwizardlib.R$layout;
import com.android.setupwizardlib.items.ItemInflater.ItemParent;
import java.util.ArrayList;

public class ButtonBarItem extends AbstractItem implements ItemParent {
    private final ArrayList<ButtonItem> mButtons = new ArrayList();
    private boolean mVisible = true;

    public ButtonBarItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getCount() {
        return isVisible() ? 1 : 0;
    }

    public boolean isEnabled() {
        return false;
    }

    public int getLayoutResource() {
        return R$layout.suw_items_button_bar;
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    public int getViewId() {
        return getId();
    }

    public void onBindView(View view) {
        LinearLayout layout = (LinearLayout) view;
        layout.removeAllViews();
        for (ButtonItem buttonItem : this.mButtons) {
            layout.addView(buttonItem.createButton(layout));
        }
        view.setId(getViewId());
    }

    public void addChild(ItemHierarchy child) {
        if (child instanceof ButtonItem) {
            this.mButtons.add((ButtonItem) child);
            return;
        }
        throw new UnsupportedOperationException("Cannot add non-button item to Button Bar");
    }

    public ItemHierarchy findItemById(int id) {
        if (getId() == id) {
            return this;
        }
        for (ButtonItem button : this.mButtons) {
            ItemHierarchy item = button.findItemById(id);
            if (item != null) {
                return item;
            }
        }
        return null;
    }
}
