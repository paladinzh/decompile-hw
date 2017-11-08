package com.huawei.systemmanager.power.data.xml;

import android.content.ContentValues;
import android.content.Context;
import com.google.common.base.Predicate;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;
import com.huawei.systemmanager.comm.xml.filter.ExtAttrValueMatchFixValue;
import com.huawei.systemmanager.power.provider.SmartProvider.ROGUE_Columns;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class RogueListPreset {
    private static final String LABEL_ROGUE = "rogue";
    private static final String LABEL_ROGUE_ATTR = "check";
    private static final String LABEL_ROGUE_PKGNAME = "name";
    private static final String LABEL_ROGUE_TAG = "package";
    private static final String TAG = RogueListPreset.class.getSimpleName();

    public static void presetRoguePackage(Context ctx) {
        if (!alreadySaved(ctx)) {
            List<String> rogueList = readRoguePackageList(ctx);
            HwLog.d(TAG, "presetRoguePackage rogueList is:" + rogueList);
            if (!rogueList.isEmpty()) {
                int size = rogueList.size();
                ContentValues values = new ContentValues();
                for (int i = 0; i < size; i++) {
                    values.put("pkgname", (String) rogueList.get(i));
                    values.put(ROGUE_Columns.ISROGUE, Integer.valueOf(0));
                    values.put(ROGUE_Columns.IGNORE, Integer.valueOf(0));
                    values.put(ROGUE_Columns.CLEAR, Integer.valueOf(0));
                    values.put(ROGUE_Columns.PRESETBLACKAPP, Integer.valueOf(1));
                    values.put(ROGUE_Columns.HIGHWAKEUPFREQ, Integer.valueOf(0));
                    values.put(ROGUE_Columns.IGNOREWAKEUPAPP, Integer.valueOf(0));
                    SavingSettingUtil.insertRogue(ctx.getContentResolver(), (String) rogueList.get(i), values);
                    values.clear();
                }
                setSaved(ctx);
            }
        }
    }

    private static boolean alreadySaved(Context ctx) {
        String value = SharePrefWrapper.getPrefValue(ctx, "appDatabase", "savedDatabase", "noSave");
        HwLog.i(TAG, " alreadySaved ? " + value.equals("saved"));
        return value.equals("saved");
    }

    private static void setSaved(Context ctx) {
        SharePrefWrapper.setPrefValue(ctx, "appDatabase", "savedDatabase", "saved");
    }

    private static List<String> readRoguePackageList(Context context) {
        return XmlParsers.xmlAttrValueList(context, "/data/cust/xml/hw_powersaving_rogue_list.xml", (int) R.xml.power_rogue_list, roguePredicate(), XmlParsers.getRowToAttrValueFunc("name"));
    }

    private static Predicate<SimpleXmlRow> roguePredicate() {
        return XmlParsers.joinPredicates(XmlParsers.getTagAttrMatchPredicate("package", "name"), new ExtAttrValueMatchFixValue("check", LABEL_ROGUE));
    }
}
