package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bc.c;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class be extends AbstractParser<c> {
    be() {
    }

    public c a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new c(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
