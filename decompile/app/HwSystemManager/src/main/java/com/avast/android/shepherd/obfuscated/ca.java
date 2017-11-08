package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.g;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class ca extends AbstractParser<g> {
    ca() {
    }

    public g a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new g(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
