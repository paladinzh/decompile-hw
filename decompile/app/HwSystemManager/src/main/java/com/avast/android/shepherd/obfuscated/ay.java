package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.as.i;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class ay extends AbstractParser<i> {
    ay() {
    }

    public i a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new i(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
