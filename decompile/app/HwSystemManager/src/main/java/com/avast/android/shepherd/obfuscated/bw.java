package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.a;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class bw extends AbstractParser<a> {
    bw() {
    }

    public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new a(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
