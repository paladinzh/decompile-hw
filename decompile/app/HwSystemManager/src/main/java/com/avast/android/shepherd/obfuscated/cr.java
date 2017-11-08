package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bv.ak;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class cr extends AbstractParser<ak> {
    cr() {
    }

    public ak a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new ak(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
