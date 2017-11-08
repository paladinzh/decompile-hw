package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.y;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class cl extends AbstractParser<y> {
    cl() {
    }

    public y a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new y(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
