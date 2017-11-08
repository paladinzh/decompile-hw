package com.avast.android.sdk.engine.obfuscated;

import com.avast.android.sdk.engine.obfuscated.z.d;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;

/* compiled from: Unknown */
class ad extends AbstractParser<d> {
    ad() {
    }

    public d a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
        return new d(codedInputStream, extensionRegistryLite);
    }

    public /* synthetic */ Object parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
        return a(codedInputStream, extensionRegistryLite);
    }
}
