package com.avast.android.sdk.engine.obfuscated;

import com.avast.android.sdk.engine.obfuscated.af.a;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;

/* compiled from: Unknown */
class ah extends AbstractParser<a> {
    ah() {
    }

    public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
        return new a(codedInputStream, extensionRegistryLite, null);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
        return a(codedInputStream, extensionRegistryLite);
    }
}
