package com.android.settings.users;

import android.content.Context;
import android.content.RestrictionEntry;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import java.util.ArrayList;

public class RestrictionUtils {
    public static final int[] sRestrictionDescriptions = new int[]{2131626590};
    public static final String[] sRestrictionKeys = new String[]{"no_share_location"};
    public static final int[] sRestrictionTitles = new int[]{2131626589};

    public static ArrayList<RestrictionEntry> getRestrictions(Context context, UserHandle user) {
        Resources res = context.getResources();
        ArrayList<RestrictionEntry> entries = new ArrayList();
        Bundle userRestrictions = UserManager.get(context).getUserRestrictions(user);
        for (int i = 0; i < sRestrictionKeys.length; i++) {
            boolean z;
            String str = sRestrictionKeys[i];
            if (userRestrictions.getBoolean(sRestrictionKeys[i], false)) {
                z = false;
            } else {
                z = true;
            }
            RestrictionEntry entry = new RestrictionEntry(str, z);
            entry.setTitle(res.getString(sRestrictionTitles[i]));
            entry.setDescription(res.getString(sRestrictionDescriptions[i]));
            entry.setType(1);
            entries.add(entry);
        }
        return entries;
    }

    public static void setRestrictions(Context context, ArrayList<RestrictionEntry> entries, UserHandle user) {
        UserManager um = UserManager.get(context);
        for (RestrictionEntry entry : entries) {
            um.setUserRestriction(entry.getKey(), !entry.getSelectedState(), user);
        }
    }
}
