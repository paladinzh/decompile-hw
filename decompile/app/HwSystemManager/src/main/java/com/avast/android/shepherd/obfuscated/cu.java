package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.aq;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class cu extends AbstractParser<aq> {
    cu() {
    }

    public aq a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new aq(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
