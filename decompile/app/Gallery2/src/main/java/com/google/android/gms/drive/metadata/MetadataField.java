package com.google.android.gms.drive.metadata;

import com.google.android.gms.internal.er;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/* compiled from: Unknown */
public abstract class MetadataField<T> {
    private final String Eg;
    private final Set<String> Eh;
    private final int Ei;

    protected MetadataField(String fieldName, int versionAdded) {
        this.Eg = (String) er.b((Object) fieldName, (Object) "fieldName");
        this.Eh = Collections.singleton(fieldName);
        this.Ei = versionAdded;
    }

    protected MetadataField(String fieldName, Collection<String> dataHolderFieldNames, int versionAdded) {
        this.Eg = (String) er.b((Object) fieldName, (Object) "fieldName");
        this.Eh = Collections.unmodifiableSet(new HashSet(dataHolderFieldNames));
        this.Ei = versionAdded;
    }

    public final String getName() {
        return this.Eg;
    }

    public String toString() {
        return this.Eg;
    }
}
