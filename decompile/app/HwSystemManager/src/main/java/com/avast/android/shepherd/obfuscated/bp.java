package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bc.y;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class bp extends AbstractParser<y> {
    bp() {
    }

    public y a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new y(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
