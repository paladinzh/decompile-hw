package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bc.q;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class bl extends AbstractParser<q> {
    bl() {
    }

    public q a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new q(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
