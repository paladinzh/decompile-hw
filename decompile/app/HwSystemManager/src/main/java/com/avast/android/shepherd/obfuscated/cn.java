package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.ac;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class cn extends AbstractParser<ac> {
    cn() {
    }

    public ac a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new ac(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
