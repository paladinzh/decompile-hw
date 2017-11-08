package com.avast.android.sdk.engine.obfuscated;

import com.avast.android.sdk.engine.obfuscated.bm.c;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;

/* compiled from: Unknown */
class bp extends AbstractParser<c> {
    bp() {
    }

    public c a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
        return new c(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
        return a(codedInputStream, extensionRegistryLite);
    }
}
