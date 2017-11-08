package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bc.w;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class bo extends AbstractParser<w> {
    bo() {
    }

    public w a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new w(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
