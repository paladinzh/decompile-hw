package com.avast.android.shepherd.obfuscated;

import android.content.Context;
import com.avast.android.shepherd.obfuscated.as.m;
import com.google.protobuf.MessageLite;

/* compiled from: Unknown */
public class ap {
    public static m a(Context context, MessageLite messageLite, ak akVar, ao aoVar, byte[] bArr, al alVar) {
        ar arVar = null;
        if (context != null) {
            if (bArr != null || messageLite != null) {
                if (!(akVar == null || aoVar == null)) {
                    ah a = am.a(context, alVar);
                    if (bArr != null) {
                        arVar = new aq(bArr);
                    }
                    return a.a(messageLite, akVar, aoVar.a(), arVar);
                }
            }
        }
        return null;
    }
}
