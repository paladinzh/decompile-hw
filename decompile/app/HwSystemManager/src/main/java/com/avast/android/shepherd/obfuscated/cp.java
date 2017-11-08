package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.ag;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class cp extends AbstractParser<ag> {
    cp() {
    }

    public ag a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new ag(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
