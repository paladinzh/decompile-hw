package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bc.o;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class bk extends AbstractParser<o> {
    bk() {
    }

    public o a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new o(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
