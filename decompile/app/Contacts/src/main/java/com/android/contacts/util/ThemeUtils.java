package com.android.contacts.util;

import android.content.res.Resources.Theme;
import android.util.TypedValue;

public class ThemeUtils {
    public static int getAttribute(Theme theme, int attrId) {
        TypedValue outValue = new TypedValue();
        theme.resolveAttribute(attrId, outValue, true);
        return outValue.resourceId;
    }

    public static int getSelectableItemBackground(Theme theme) {
        return getAttribute(theme, 16843534);
    }
}
