package com.android.contacts.statistical;

import android.content.Context;
import com.amap.api.services.core.AMapException;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.ValuesDelta;
import java.util.ArrayList;
import java.util.HashMap;

public class ContactEditorReport {
    public static String checkRawContactDeltaList(RawContactDeltaList state, Context context) {
        StringBuilder builder = new StringBuilder();
        boolean containEmail = false;
        boolean groupMembership = false;
        boolean ringtone = false;
        boolean structuredPostal = false;
        boolean im = false;
        boolean note = false;
        boolean photo = false;
        boolean z = false;
        boolean z2 = false;
        boolean organization = false;
        int nameCount = 0;
        int numberCount = 0;
        boolean hasWechat = false;
        boolean hasWhatsapp = false;
        if (!(state == null || state.isEmpty())) {
            for (RawContactDelta delta : state) {
                HashMap<String, ArrayList<ValuesDelta>> entries = delta.getEntries();
                if (entries.containsKey("vnd.android.cursor.item/name")) {
                    nameCount++;
                }
                if (entries.containsKey("vnd.android.cursor.item/phone_v2")) {
                    numberCount += ((ArrayList) entries.get("vnd.android.cursor.item/phone_v2")).size();
                }
                if (isContainKind(entries, "vnd.android.cursor.item/email_v2")) {
                    containEmail = true;
                }
                if (isContainKind(entries, "vnd.android.cursor.item/group_membership")) {
                    groupMembership = true;
                }
                if (isContainKind(entries, "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.profile")) {
                    hasWechat = true;
                }
                if (isContainKind(entries, "vnd.android.cursor.item/vnd.com.whatsapp.profile")) {
                    hasWhatsapp = true;
                }
                boolean lHasRingtoneChanges = false;
                if (delta.getValues().getAfter() != null) {
                    lHasRingtoneChanges = delta.getValues().getAfter().containsKey("custom_ringtone");
                }
                if (lHasRingtoneChanges) {
                    ringtone = true;
                }
                if (isContainKind(entries, "vnd.android.cursor.item/postal-address_v2")) {
                    structuredPostal = true;
                }
                if (isContainKind(entries, "vnd.android.cursor.item/im")) {
                    im = true;
                }
                if (isContainKind(entries, "vnd.android.cursor.item/note")) {
                    note = true;
                }
                if (isContainKind(entries, "vnd.android.cursor.item/photo")) {
                    photo = true;
                }
                if (isContainKind(entries, "vnd.android.cursor.item/contact_event")) {
                    for (ValuesDelta value : (ArrayList) entries.get("vnd.android.cursor.item/contact_event")) {
                        String type = getAsString(value, "data2");
                        if (type != null) {
                            z = !z ? type.equals("3") : true;
                            z2 = !z2 ? type.equals("4") : true;
                        }
                    }
                }
                if (isContainKind(entries, "vnd.android.cursor.item/organization")) {
                    organization = true;
                }
            }
        }
        builder.append(containEmail ? '1' : '0');
        builder.append(groupMembership ? '1' : '0');
        builder.append(ringtone ? '1' : '0');
        builder.append(structuredPostal ? '1' : '0');
        builder.append(im ? '1' : '0');
        builder.append(note ? '1' : '0');
        builder.append(photo ? '1' : '0');
        builder.append(z ? '1' : '0');
        builder.append(z2 ? '1' : '0');
        builder.append(organization ? '1' : '0');
        if (!(nameCount < 1 || numberCount != 1 || organization || containEmail || groupMembership || ringtone || structuredPostal || im || note || photo || z)) {
            StatisticalHelper.report(AMapException.CODE_AMAP_ID_NOT_EXIST);
        }
        if (hasWechat) {
            StatisticalHelper.report(2026);
        }
        if (hasWhatsapp) {
            StatisticalHelper.report(2027);
        }
        if (numberCount > 0 && context != null && CommonUtilMethods.checkApkExist(context, "com.huawei.espacev2")) {
            StatisticalHelper.report(2029);
        }
        return builder.toString();
    }

    private static boolean isContainKind(HashMap<String, ArrayList<ValuesDelta>> entries, String mimeType) {
        ArrayList<ValuesDelta> mimeEntries = (ArrayList) entries.get(mimeType);
        if (mimeEntries != null) {
            String dataType = "data1";
            if ("vnd.android.cursor.item/photo".equals(mimeType)) {
                dataType = "data15";
            }
            for (ValuesDelta entry : mimeEntries) {
                if (getAsString(entry, dataType) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String getAsString(ValuesDelta entry, String dataType) {
        if (entry == null || dataType == null || entry.isDelete()) {
            return null;
        }
        return entry.getAsString(dataType);
    }
}
