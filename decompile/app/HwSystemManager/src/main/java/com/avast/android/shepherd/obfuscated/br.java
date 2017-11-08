package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bc.ab;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class br extends AbstractParser<ab> {
    br() {
    }

    public ab a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new ab(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
