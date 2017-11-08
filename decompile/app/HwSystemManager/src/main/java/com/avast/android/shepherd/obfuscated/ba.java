package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.as.k;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class ba extends AbstractParser<k> {
    ba() {
    }

    public k a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new k(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
