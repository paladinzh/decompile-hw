package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.as.g;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class ax extends AbstractParser<g> {
    ax() {
    }

    public g a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new g(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
