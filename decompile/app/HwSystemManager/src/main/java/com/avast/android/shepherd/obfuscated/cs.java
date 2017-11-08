package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.am;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class cs extends AbstractParser<am> {
    cs() {
    }

    public am a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new am(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
