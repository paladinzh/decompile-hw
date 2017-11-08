package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.bc.ad;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

/* compiled from: Unknown */
class bs extends AbstractParser<ad> {
    bs() {
    }

    public ad a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new ad(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return a(codedInputStream, extensionRegistryLite);
    }
}
