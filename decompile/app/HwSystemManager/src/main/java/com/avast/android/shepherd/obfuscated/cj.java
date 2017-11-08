package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.u;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class cj extends AbstractParser<u> {
    cj() {
    }

    public u a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new u(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
