package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.q;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class cg extends AbstractParser<q> {
    cg() {
    }

    public q a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new q(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
