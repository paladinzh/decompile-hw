package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bc.e;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class bf extends AbstractParser<e> {
    bf() {
    }

    public e a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new e(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
