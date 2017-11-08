package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.ai;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class cq extends AbstractParser<ai> {
    cq() {
    }

    public ai a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new ai(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
