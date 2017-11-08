package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.ao;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class ct extends AbstractParser<ao> {
    ct() {
    }

    public ao a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new ao(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
