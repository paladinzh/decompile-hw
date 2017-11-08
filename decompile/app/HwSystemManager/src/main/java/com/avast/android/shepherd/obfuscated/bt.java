package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bc.ae;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class bt extends AbstractParser<ae> {
    bt() {
    }

    public ae a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new ae(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
