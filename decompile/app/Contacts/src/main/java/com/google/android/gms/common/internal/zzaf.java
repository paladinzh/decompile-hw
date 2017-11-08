package com.google.android.gms.common.internal;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

/* compiled from: Unknown */
public class zzaf {
    public static String zza(String str, String str2, Context context, AttributeSet attributeSet, boolean z, boolean z2, String str3) {
        String str4 = null;
        if (attributeSet != null) {
            str4 = attributeSet.getAttributeValue(str, str2);
        }
        if (str4 != null && str4.startsWith("@string/") && z) {
            String substring = str4.substring("@string/".length());
            String packageName = context.getPackageName();
            TypedValue typedValue = new TypedValue();
            try {
                context.getResources().getValue(packageName + ":string/" + substring, typedValue, true);
            } catch (NotFoundException e) {
                Log.w(str3, "Could not find resource for " + str2 + ": " + str4);
            }
            if (typedValue.string == null) {
                Log.w(str3, "Resource " + str2 + " was not a string: " + typedValue);
            } else {
                str4 = typedValue.string.toString();
            }
        }
        if (z2 && str4 == null) {
            Log.w(str3, "Required XML attribute \"" + str2 + "\" missing");
        }
        return str4;
    }
}
