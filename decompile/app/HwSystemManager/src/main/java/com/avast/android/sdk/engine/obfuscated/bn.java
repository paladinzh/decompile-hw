package com.avast.android.sdk.engine.obfuscated;

import com.avast.android.sdk.engine.obfuscated.bm.a;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;

/* compiled from: Unknown */
class bn extends AbstractParser<a> {
    bn() {
    }

    public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
        return new a(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
        return a(codedInputStream, extensionRegistryLite);
    }
}
