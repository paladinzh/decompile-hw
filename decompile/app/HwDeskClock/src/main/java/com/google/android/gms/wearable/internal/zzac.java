package com.google.android.gms.wearable.internal;

import android.net.Uri;
import android.util.Log;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemAsset;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: Unknown */
public class zzac implements DataItem {
    private Uri mUri;
    private byte[] zzayI;
    private Map<String, DataItemAsset> zzbas;

    public String toString() {
        return toString(Log.isLoggable("DataItem", 3));
    }

    public String toString(boolean verbose) {
        StringBuilder stringBuilder = new StringBuilder("DataItemEntity{ ");
        stringBuilder.append("uri=" + this.mUri);
        stringBuilder.append(", dataSz=" + (this.zzayI != null ? Integer.valueOf(this.zzayI.length) : "null"));
        stringBuilder.append(", numAssets=" + this.zzbas.size());
        if (verbose && !this.zzbas.isEmpty()) {
            stringBuilder.append(", assets=[");
            String str = "";
            Iterator it = this.zzbas.entrySet().iterator();
            while (true) {
                String str2 = str;
                if (!it.hasNext()) {
                    break;
                }
                Entry entry = (Entry) it.next();
                stringBuilder.append(str2 + ((String) entry.getKey()) + ": " + ((DataItemAsset) entry.getValue()).getId());
                str = ", ";
            }
            stringBuilder.append("]");
        }
        stringBuilder.append(" }");
        return stringBuilder.toString();
    }
}
