package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.i;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class cc extends AbstractParser<i> {
    cc() {
    }

    public i a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new i(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
