package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.c;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class by extends AbstractParser<c> {
    by() {
    }

    public c a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new c(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
