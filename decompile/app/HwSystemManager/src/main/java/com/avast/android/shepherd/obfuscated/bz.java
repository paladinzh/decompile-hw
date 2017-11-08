package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.e;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class bz extends AbstractParser<e> {
    bz() {
    }

    public e a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new e(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
