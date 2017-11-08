package com.fyusion.sdk.common.ext.internal;

/* compiled from: Unknown */
public class a {
    public static void a(String[] strArr, Settings settings) {
        for (String cls : strArr) {
            try {
                ((Capabilities) Class.forName(cls).newInstance()).inject(settings);
            } catch (InstantiationException e) {
            }
        }
    }
}
