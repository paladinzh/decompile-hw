package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bc.s;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class bm extends AbstractParser<s> {
    bm() {
    }

    public s a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new s(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
