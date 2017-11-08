package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.aa;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class cm extends AbstractParser<aa> {
    cm() {
    }

    public aa a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new aa(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
