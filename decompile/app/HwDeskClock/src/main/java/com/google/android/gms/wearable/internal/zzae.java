package com.google.android.gms.wearable.internal;

import android.net.Uri;
import android.util.Log;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.common.data.zzc;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemAsset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: Unknown */
public final class zzae extends zzc implements DataItem {
    private final int zzasB;

    public zzae(DataHolder dataHolder, int i, int i2) {
        super(dataHolder, i);
        this.zzasB = i2;
    }

    public Map<String, DataItemAsset> getAssets() {
        Map<String, DataItemAsset> hashMap = new HashMap(this.zzasB);
        for (int i = 0; i < this.zzasB; i++) {
            zzab zzab = new zzab(this.zzYX, this.zzabh + i);
            if (zzab.getDataItemKey() != null) {
                hashMap.put(zzab.getDataItemKey(), zzab);
            }
        }
        return hashMap;
    }

    public byte[] getData() {
        return getByteArray("data");
    }

    public Uri getUri() {
        return Uri.parse(getString("path"));
    }

    public String toString() {
        return toString(Log.isLoggable("DataItem", 3));
    }

    public String toString(boolean verbose) {
        byte[] data = getData();
        Map assets = getAssets();
        StringBuilder stringBuilder = new StringBuilder("DataItemInternal{ ");
        stringBuilder.append("uri=" + getUri());
        stringBuilder.append(", dataSz=" + (data != null ? Integer.valueOf(data.length) : "null"));
        stringBuilder.append(", numAssets=" + assets.size());
        if (verbose && !assets.isEmpty()) {
            stringBuilder.append(", assets=[");
            String str = "";
            Iterator it = assets.entrySet().iterator();
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
