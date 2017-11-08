package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.w;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class ck extends AbstractParser<w> {
    ck() {
    }

    public w a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new w(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
