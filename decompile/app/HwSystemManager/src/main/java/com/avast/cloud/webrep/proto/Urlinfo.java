package com.avast.cloud.webrep.proto;

import android.support.v4.view.MotionEventCompat;
import com.avast.cloud.webrep.proto.BrowserInfo.UpdateRequest;
import com.avast.cloud.webrep.proto.BrowserInfo.UpdateResponse;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.Internal.EnumLite;
import com.google.protobuf.Internal.EnumLiteMap;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.LazyStringArrayList;
import com.google.protobuf.LazyStringList;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import com.google.protobuf.Parser;
import com.google.protobuf.UnmodifiableLazyStringList;
import com.huawei.rcs.common.HwRcsCommonObject;
import com.huawei.systemmanager.comm.widget.CircleViewNew;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Urlinfo {

    public interface AvastIdentityOrBuilder extends MessageLiteOrBuilder {
        ByteString getAuid();

        ByteString getGuid();

        ByteString getHwid();

        ByteString getToken();

        ByteString getUserid();

        ByteString getUuid();

        boolean hasAuid();

        boolean hasGuid();

        boolean hasHwid();

        boolean hasToken();

        boolean hasUserid();

        boolean hasUuid();
    }

    public static final class AvastIdentity extends GeneratedMessageLite implements AvastIdentityOrBuilder {
        public static final int AUID_FIELD_NUMBER = 4;
        public static final int GUID_FIELD_NUMBER = 1;
        public static final int HWID_FIELD_NUMBER = 6;
        public static Parser<AvastIdentity> PARSER = new AbstractParser<AvastIdentity>() {
            public AvastIdentity parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new AvastIdentity(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int TOKEN_FIELD_NUMBER = 3;
        public static final int USERID_FIELD_NUMBER = 5;
        public static final int UUID_FIELD_NUMBER = 2;
        private static final AvastIdentity defaultInstance = new AvastIdentity(true);
        private static final long serialVersionUID = 0;
        private ByteString auid_;
        private int bitField0_;
        private ByteString guid_;
        private ByteString hwid_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private ByteString token_;
        private ByteString userid_;
        private ByteString uuid_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<AvastIdentity, Builder> implements AvastIdentityOrBuilder {
            private ByteString auid_ = ByteString.EMPTY;
            private int bitField0_;
            private ByteString guid_ = ByteString.EMPTY;
            private ByteString hwid_ = ByteString.EMPTY;
            private ByteString token_ = ByteString.EMPTY;
            private ByteString userid_ = ByteString.EMPTY;
            private ByteString uuid_ = ByteString.EMPTY;

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.guid_ = ByteString.EMPTY;
                this.bitField0_ &= -2;
                this.uuid_ = ByteString.EMPTY;
                this.bitField0_ &= -3;
                this.token_ = ByteString.EMPTY;
                this.bitField0_ &= -5;
                this.auid_ = ByteString.EMPTY;
                this.bitField0_ &= -9;
                this.userid_ = ByteString.EMPTY;
                this.bitField0_ &= -17;
                this.hwid_ = ByteString.EMPTY;
                this.bitField0_ &= -33;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public AvastIdentity getDefaultInstanceForType() {
                return AvastIdentity.getDefaultInstance();
            }

            public AvastIdentity build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public AvastIdentity buildPartial() {
                AvastIdentity avastIdentity = new AvastIdentity((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                avastIdentity.guid_ = this.guid_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                avastIdentity.uuid_ = this.uuid_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                avastIdentity.token_ = this.token_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                avastIdentity.auid_ = this.auid_;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                avastIdentity.userid_ = this.userid_;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                avastIdentity.hwid_ = this.hwid_;
                avastIdentity.bitField0_ = i2;
                return avastIdentity;
            }

            public Builder mergeFrom(AvastIdentity avastIdentity) {
                if (avastIdentity == AvastIdentity.getDefaultInstance()) {
                    return this;
                }
                if (avastIdentity.hasGuid()) {
                    setGuid(avastIdentity.getGuid());
                }
                if (avastIdentity.hasUuid()) {
                    setUuid(avastIdentity.getUuid());
                }
                if (avastIdentity.hasToken()) {
                    setToken(avastIdentity.getToken());
                }
                if (avastIdentity.hasAuid()) {
                    setAuid(avastIdentity.getAuid());
                }
                if (avastIdentity.hasUserid()) {
                    setUserid(avastIdentity.getUserid());
                }
                if (avastIdentity.hasHwid()) {
                    setHwid(avastIdentity.getHwid());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                AvastIdentity avastIdentity;
                Throwable th;
                AvastIdentity avastIdentity2;
                try {
                    avastIdentity = (AvastIdentity) AvastIdentity.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (avastIdentity != null) {
                        mergeFrom(avastIdentity);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    avastIdentity = (AvastIdentity) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    avastIdentity2 = avastIdentity;
                    th = th3;
                }
                if (avastIdentity2 != null) {
                    mergeFrom(avastIdentity2);
                }
                throw th;
            }

            public boolean hasGuid() {
                return (this.bitField0_ & 1) == 1;
            }

            public ByteString getGuid() {
                return this.guid_;
            }

            public Builder setGuid(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.guid_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearGuid() {
                this.bitField0_ &= -2;
                this.guid_ = AvastIdentity.getDefaultInstance().getGuid();
                return this;
            }

            public boolean hasUuid() {
                return (this.bitField0_ & 2) == 2;
            }

            public ByteString getUuid() {
                return this.uuid_;
            }

            public Builder setUuid(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.uuid_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearUuid() {
                this.bitField0_ &= -3;
                this.uuid_ = AvastIdentity.getDefaultInstance().getUuid();
                return this;
            }

            public boolean hasToken() {
                return (this.bitField0_ & 4) == 4;
            }

            public ByteString getToken() {
                return this.token_;
            }

            public Builder setToken(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 4;
                    this.token_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearToken() {
                this.bitField0_ &= -5;
                this.token_ = AvastIdentity.getDefaultInstance().getToken();
                return this;
            }

            public boolean hasAuid() {
                return (this.bitField0_ & 8) == 8;
            }

            public ByteString getAuid() {
                return this.auid_;
            }

            public Builder setAuid(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 8;
                    this.auid_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearAuid() {
                this.bitField0_ &= -9;
                this.auid_ = AvastIdentity.getDefaultInstance().getAuid();
                return this;
            }

            public boolean hasUserid() {
                return (this.bitField0_ & 16) == 16;
            }

            public ByteString getUserid() {
                return this.userid_;
            }

            public Builder setUserid(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 16;
                    this.userid_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearUserid() {
                this.bitField0_ &= -17;
                this.userid_ = AvastIdentity.getDefaultInstance().getUserid();
                return this;
            }

            public boolean hasHwid() {
                return (this.bitField0_ & 32) == 32;
            }

            public ByteString getHwid() {
                return this.hwid_;
            }

            public Builder setHwid(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 32;
                    this.hwid_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearHwid() {
                this.bitField0_ &= -33;
                this.hwid_ = AvastIdentity.getDefaultInstance().getHwid();
                return this;
            }
        }

        private AvastIdentity(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private AvastIdentity(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static AvastIdentity getDefaultInstance() {
            return defaultInstance;
        }

        public AvastIdentity getDefaultInstanceForType() {
            return defaultInstance;
        }

        private AvastIdentity(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 10:
                            this.bitField0_ |= 1;
                            this.guid_ = codedInputStream.readBytes();
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.uuid_ = codedInputStream.readBytes();
                            break;
                        case 26:
                            this.bitField0_ |= 4;
                            this.token_ = codedInputStream.readBytes();
                            break;
                        case 34:
                            this.bitField0_ |= 8;
                            this.auid_ = codedInputStream.readBytes();
                            break;
                        case MotionEventCompat.AXIS_GENERIC_11 /*42*/:
                            this.bitField0_ |= 16;
                            this.userid_ = codedInputStream.readBytes();
                            break;
                        case 50:
                            this.bitField0_ |= 32;
                            this.hwid_ = codedInputStream.readBytes();
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                obj = 1;
                                break;
                            }
                            break;
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<AvastIdentity> getParserForType() {
            return PARSER;
        }

        public boolean hasGuid() {
            return (this.bitField0_ & 1) == 1;
        }

        public ByteString getGuid() {
            return this.guid_;
        }

        public boolean hasUuid() {
            return (this.bitField0_ & 2) == 2;
        }

        public ByteString getUuid() {
            return this.uuid_;
        }

        public boolean hasToken() {
            return (this.bitField0_ & 4) == 4;
        }

        public ByteString getToken() {
            return this.token_;
        }

        public boolean hasAuid() {
            return (this.bitField0_ & 8) == 8;
        }

        public ByteString getAuid() {
            return this.auid_;
        }

        public boolean hasUserid() {
            return (this.bitField0_ & 16) == 16;
        }

        public ByteString getUserid() {
            return this.userid_;
        }

        public boolean hasHwid() {
            return (this.bitField0_ & 32) == 32;
        }

        public ByteString getHwid() {
            return this.hwid_;
        }

        private void initFields() {
            this.guid_ = ByteString.EMPTY;
            this.uuid_ = ByteString.EMPTY;
            this.token_ = ByteString.EMPTY;
            this.auid_ = ByteString.EMPTY;
            this.userid_ = ByteString.EMPTY;
            this.hwid_ = ByteString.EMPTY;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeBytes(1, this.guid_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, this.uuid_);
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeBytes(3, this.token_);
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeBytes(4, this.auid_);
            }
            if ((this.bitField0_ & 16) == 16) {
                codedOutputStream.writeBytes(5, this.userid_);
            }
            if ((this.bitField0_ & 32) == 32) {
                codedOutputStream.writeBytes(6, this.hwid_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, this.guid_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, this.uuid_);
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeBytesSize(3, this.token_);
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeBytesSize(4, this.auid_);
            }
            if ((this.bitField0_ & 16) == 16) {
                i += CodedOutputStream.computeBytesSize(5, this.userid_);
            }
            if ((this.bitField0_ & 32) == 32) {
                i += CodedOutputStream.computeBytesSize(6, this.hwid_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static AvastIdentity parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (AvastIdentity) PARSER.parseFrom(byteString);
        }

        public static AvastIdentity parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (AvastIdentity) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static AvastIdentity parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (AvastIdentity) PARSER.parseFrom(bArr);
        }

        public static AvastIdentity parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (AvastIdentity) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static AvastIdentity parseFrom(InputStream inputStream) throws IOException {
            return (AvastIdentity) PARSER.parseFrom(inputStream);
        }

        public static AvastIdentity parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (AvastIdentity) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static AvastIdentity parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (AvastIdentity) PARSER.parseDelimitedFrom(inputStream);
        }

        public static AvastIdentity parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (AvastIdentity) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static AvastIdentity parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (AvastIdentity) PARSER.parseFrom(codedInputStream);
        }

        public static AvastIdentity parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (AvastIdentity) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(AvastIdentity avastIdentity) {
            return newBuilder().mergeFrom(avastIdentity);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface BlockerOrBuilder extends MessageLiteOrBuilder {
        long getBlock();

        boolean hasBlock();
    }

    public static final class Blocker extends GeneratedMessageLite implements BlockerOrBuilder {
        public static final int BLOCK_FIELD_NUMBER = 1;
        public static Parser<Blocker> PARSER = new AbstractParser<Blocker>() {
            public Blocker parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new Blocker(codedInputStream, extensionRegistryLite);
            }
        };
        private static final Blocker defaultInstance = new Blocker(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private long block_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<Blocker, Builder> implements BlockerOrBuilder {
            private int bitField0_;
            private long block_;

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.block_ = 0;
                this.bitField0_ &= -2;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public Blocker getDefaultInstanceForType() {
                return Blocker.getDefaultInstance();
            }

            public Blocker build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public Blocker buildPartial() {
                Blocker blocker = new Blocker((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = 0;
                if ((this.bitField0_ & 1) == 1) {
                    i = 1;
                }
                blocker.block_ = this.block_;
                blocker.bitField0_ = i;
                return blocker;
            }

            public Builder mergeFrom(Blocker blocker) {
                if (blocker != Blocker.getDefaultInstance() && blocker.hasBlock()) {
                    setBlock(blocker.getBlock());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                Blocker blocker;
                Blocker blocker2;
                try {
                    blocker2 = (Blocker) Blocker.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (blocker2 != null) {
                        mergeFrom(blocker2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    blocker2 = (Blocker) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    blocker = blocker2;
                    th = th3;
                }
                if (blocker != null) {
                    mergeFrom(blocker);
                }
                throw th;
            }

            public boolean hasBlock() {
                return (this.bitField0_ & 1) == 1;
            }

            public long getBlock() {
                return this.block_;
            }

            public Builder setBlock(long j) {
                this.bitField0_ |= 1;
                this.block_ = j;
                return this;
            }

            public Builder clearBlock() {
                this.bitField0_ &= -2;
                this.block_ = 0;
                return this;
            }
        }

        private Blocker(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private Blocker(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static Blocker getDefaultInstance() {
            return defaultInstance;
        }

        public Blocker getDefaultInstanceForType() {
            return defaultInstance;
        }

        private Blocker(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 8:
                            this.bitField0_ |= 1;
                            this.block_ = codedInputStream.readSInt64();
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                obj = 1;
                                break;
                            }
                            break;
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<Blocker> getParserForType() {
            return PARSER;
        }

        public boolean hasBlock() {
            return (this.bitField0_ & 1) == 1;
        }

        public long getBlock() {
            return this.block_;
        }

        private void initFields() {
            this.block_ = 0;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeSInt64(1, this.block_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeSInt64Size(1, this.block_) + 0;
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static Blocker parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (Blocker) PARSER.parseFrom(byteString);
        }

        public static Blocker parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Blocker) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static Blocker parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (Blocker) PARSER.parseFrom(bArr);
        }

        public static Blocker parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Blocker) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static Blocker parseFrom(InputStream inputStream) throws IOException {
            return (Blocker) PARSER.parseFrom(inputStream);
        }

        public static Blocker parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Blocker) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static Blocker parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (Blocker) PARSER.parseDelimitedFrom(inputStream);
        }

        public static Blocker parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Blocker) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static Blocker parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (Blocker) PARSER.parseFrom(codedInputStream);
        }

        public static Blocker parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Blocker) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(Blocker blocker) {
            return newBuilder().mergeFrom(blocker);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface BrowserExtInfoOrBuilder extends MessageLiteOrBuilder {
        BrowserType getBrowserType();

        String getBrowserVersion();

        ByteString getBrowserVersionBytes();

        int getDataVersion();

        ExtensionType getExtensionType();

        int getExtensionVersion();

        OS getOs();

        String getOsVersion();

        ByteString getOsVersionBytes();

        boolean hasBrowserType();

        boolean hasBrowserVersion();

        boolean hasDataVersion();

        boolean hasExtensionType();

        boolean hasExtensionVersion();

        boolean hasOs();

        boolean hasOsVersion();
    }

    public static final class BrowserExtInfo extends GeneratedMessageLite implements BrowserExtInfoOrBuilder {
        public static final int BROWSERTYPE_FIELD_NUMBER = 3;
        public static final int BROWSERVERSION_FIELD_NUMBER = 4;
        public static final int DATAVERSION_FIELD_NUMBER = 7;
        public static final int EXTENSIONTYPE_FIELD_NUMBER = 1;
        public static final int EXTENSIONVERSION_FIELD_NUMBER = 2;
        public static final int OSVERSION_FIELD_NUMBER = 6;
        public static final int OS_FIELD_NUMBER = 5;
        public static Parser<BrowserExtInfo> PARSER = new AbstractParser<BrowserExtInfo>() {
            public BrowserExtInfo parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new BrowserExtInfo(codedInputStream, extensionRegistryLite);
            }
        };
        private static final BrowserExtInfo defaultInstance = new BrowserExtInfo(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private BrowserType browserType_;
        private Object browserVersion_;
        private int dataVersion_;
        private ExtensionType extensionType_;
        private int extensionVersion_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object osVersion_;
        private OS os_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<BrowserExtInfo, Builder> implements BrowserExtInfoOrBuilder {
            private int bitField0_;
            private BrowserType browserType_ = BrowserType.CHROME;
            private Object browserVersion_ = "";
            private int dataVersion_;
            private ExtensionType extensionType_ = ExtensionType.AOS;
            private int extensionVersion_;
            private Object osVersion_ = "";
            private OS os_ = OS.WIN;

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.extensionType_ = ExtensionType.AOS;
                this.bitField0_ &= -2;
                this.extensionVersion_ = 0;
                this.bitField0_ &= -3;
                this.browserType_ = BrowserType.CHROME;
                this.bitField0_ &= -5;
                this.browserVersion_ = "";
                this.bitField0_ &= -9;
                this.os_ = OS.WIN;
                this.bitField0_ &= -17;
                this.osVersion_ = "";
                this.bitField0_ &= -33;
                this.dataVersion_ = 0;
                this.bitField0_ &= -65;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public BrowserExtInfo getDefaultInstanceForType() {
                return BrowserExtInfo.getDefaultInstance();
            }

            public BrowserExtInfo build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public BrowserExtInfo buildPartial() {
                BrowserExtInfo browserExtInfo = new BrowserExtInfo((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                browserExtInfo.extensionType_ = this.extensionType_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                browserExtInfo.extensionVersion_ = this.extensionVersion_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                browserExtInfo.browserType_ = this.browserType_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                browserExtInfo.browserVersion_ = this.browserVersion_;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                browserExtInfo.os_ = this.os_;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                browserExtInfo.osVersion_ = this.osVersion_;
                if ((i & 64) == 64) {
                    i2 |= 64;
                }
                browserExtInfo.dataVersion_ = this.dataVersion_;
                browserExtInfo.bitField0_ = i2;
                return browserExtInfo;
            }

            public Builder mergeFrom(BrowserExtInfo browserExtInfo) {
                if (browserExtInfo == BrowserExtInfo.getDefaultInstance()) {
                    return this;
                }
                if (browserExtInfo.hasExtensionType()) {
                    setExtensionType(browserExtInfo.getExtensionType());
                }
                if (browserExtInfo.hasExtensionVersion()) {
                    setExtensionVersion(browserExtInfo.getExtensionVersion());
                }
                if (browserExtInfo.hasBrowserType()) {
                    setBrowserType(browserExtInfo.getBrowserType());
                }
                if (browserExtInfo.hasBrowserVersion()) {
                    this.bitField0_ |= 8;
                    this.browserVersion_ = browserExtInfo.browserVersion_;
                }
                if (browserExtInfo.hasOs()) {
                    setOs(browserExtInfo.getOs());
                }
                if (browserExtInfo.hasOsVersion()) {
                    this.bitField0_ |= 32;
                    this.osVersion_ = browserExtInfo.osVersion_;
                }
                if (browserExtInfo.hasDataVersion()) {
                    setDataVersion(browserExtInfo.getDataVersion());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                BrowserExtInfo browserExtInfo;
                BrowserExtInfo browserExtInfo2;
                try {
                    browserExtInfo2 = (BrowserExtInfo) BrowserExtInfo.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (browserExtInfo2 != null) {
                        mergeFrom(browserExtInfo2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    browserExtInfo2 = (BrowserExtInfo) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    browserExtInfo = browserExtInfo2;
                    th = th3;
                }
                if (browserExtInfo != null) {
                    mergeFrom(browserExtInfo);
                }
                throw th;
            }

            public boolean hasExtensionType() {
                return (this.bitField0_ & 1) == 1;
            }

            public ExtensionType getExtensionType() {
                return this.extensionType_;
            }

            public Builder setExtensionType(ExtensionType extensionType) {
                if (extensionType != null) {
                    this.bitField0_ |= 1;
                    this.extensionType_ = extensionType;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearExtensionType() {
                this.bitField0_ &= -2;
                this.extensionType_ = ExtensionType.AOS;
                return this;
            }

            public boolean hasExtensionVersion() {
                return (this.bitField0_ & 2) == 2;
            }

            public int getExtensionVersion() {
                return this.extensionVersion_;
            }

            public Builder setExtensionVersion(int i) {
                this.bitField0_ |= 2;
                this.extensionVersion_ = i;
                return this;
            }

            public Builder clearExtensionVersion() {
                this.bitField0_ &= -3;
                this.extensionVersion_ = 0;
                return this;
            }

            public boolean hasBrowserType() {
                return (this.bitField0_ & 4) == 4;
            }

            public BrowserType getBrowserType() {
                return this.browserType_;
            }

            public Builder setBrowserType(BrowserType browserType) {
                if (browserType != null) {
                    this.bitField0_ |= 4;
                    this.browserType_ = browserType;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearBrowserType() {
                this.bitField0_ &= -5;
                this.browserType_ = BrowserType.CHROME;
                return this;
            }

            public boolean hasBrowserVersion() {
                return (this.bitField0_ & 8) == 8;
            }

            public String getBrowserVersion() {
                Object obj = this.browserVersion_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.browserVersion_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getBrowserVersionBytes() {
                Object obj = this.browserVersion_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.browserVersion_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setBrowserVersion(String str) {
                if (str != null) {
                    this.bitField0_ |= 8;
                    this.browserVersion_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearBrowserVersion() {
                this.bitField0_ &= -9;
                this.browserVersion_ = BrowserExtInfo.getDefaultInstance().getBrowserVersion();
                return this;
            }

            public Builder setBrowserVersionBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 8;
                    this.browserVersion_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasOs() {
                return (this.bitField0_ & 16) == 16;
            }

            public OS getOs() {
                return this.os_;
            }

            public Builder setOs(OS os) {
                if (os != null) {
                    this.bitField0_ |= 16;
                    this.os_ = os;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearOs() {
                this.bitField0_ &= -17;
                this.os_ = OS.WIN;
                return this;
            }

            public boolean hasOsVersion() {
                return (this.bitField0_ & 32) == 32;
            }

            public String getOsVersion() {
                Object obj = this.osVersion_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.osVersion_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getOsVersionBytes() {
                Object obj = this.osVersion_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.osVersion_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setOsVersion(String str) {
                if (str != null) {
                    this.bitField0_ |= 32;
                    this.osVersion_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearOsVersion() {
                this.bitField0_ &= -33;
                this.osVersion_ = BrowserExtInfo.getDefaultInstance().getOsVersion();
                return this;
            }

            public Builder setOsVersionBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 32;
                    this.osVersion_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasDataVersion() {
                return (this.bitField0_ & 64) == 64;
            }

            public int getDataVersion() {
                return this.dataVersion_;
            }

            public Builder setDataVersion(int i) {
                this.bitField0_ |= 64;
                this.dataVersion_ = i;
                return this;
            }

            public Builder clearDataVersion() {
                this.bitField0_ &= -65;
                this.dataVersion_ = 0;
                return this;
            }
        }

        private BrowserExtInfo(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private BrowserExtInfo(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static BrowserExtInfo getDefaultInstance() {
            return defaultInstance;
        }

        public BrowserExtInfo getDefaultInstanceForType() {
            return defaultInstance;
        }

        private BrowserExtInfo(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 8:
                            ExtensionType valueOf = ExtensionType.valueOf(codedInputStream.readEnum());
                            if (valueOf == null) {
                                break;
                            }
                            this.bitField0_ |= 1;
                            this.extensionType_ = valueOf;
                            break;
                        case 16:
                            this.bitField0_ |= 2;
                            this.extensionVersion_ = codedInputStream.readSInt32();
                            break;
                        case 24:
                            BrowserType valueOf2 = BrowserType.valueOf(codedInputStream.readEnum());
                            if (valueOf2 == null) {
                                break;
                            }
                            this.bitField0_ |= 4;
                            this.browserType_ = valueOf2;
                            break;
                        case 34:
                            this.bitField0_ |= 8;
                            this.browserVersion_ = codedInputStream.readBytes();
                            break;
                        case 40:
                            OS valueOf3 = OS.valueOf(codedInputStream.readEnum());
                            if (valueOf3 == null) {
                                break;
                            }
                            this.bitField0_ |= 16;
                            this.os_ = valueOf3;
                            break;
                        case 50:
                            this.bitField0_ |= 32;
                            this.osVersion_ = codedInputStream.readBytes();
                            break;
                        case 56:
                            this.bitField0_ |= 64;
                            this.dataVersion_ = codedInputStream.readSInt32();
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                obj = 1;
                                break;
                            }
                            break;
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<BrowserExtInfo> getParserForType() {
            return PARSER;
        }

        public boolean hasExtensionType() {
            return (this.bitField0_ & 1) == 1;
        }

        public ExtensionType getExtensionType() {
            return this.extensionType_;
        }

        public boolean hasExtensionVersion() {
            return (this.bitField0_ & 2) == 2;
        }

        public int getExtensionVersion() {
            return this.extensionVersion_;
        }

        public boolean hasBrowserType() {
            return (this.bitField0_ & 4) == 4;
        }

        public BrowserType getBrowserType() {
            return this.browserType_;
        }

        public boolean hasBrowserVersion() {
            return (this.bitField0_ & 8) == 8;
        }

        public String getBrowserVersion() {
            Object obj = this.browserVersion_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.browserVersion_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getBrowserVersionBytes() {
            Object obj = this.browserVersion_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.browserVersion_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasOs() {
            return (this.bitField0_ & 16) == 16;
        }

        public OS getOs() {
            return this.os_;
        }

        public boolean hasOsVersion() {
            return (this.bitField0_ & 32) == 32;
        }

        public String getOsVersion() {
            Object obj = this.osVersion_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.osVersion_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getOsVersionBytes() {
            Object obj = this.osVersion_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.osVersion_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasDataVersion() {
            return (this.bitField0_ & 64) == 64;
        }

        public int getDataVersion() {
            return this.dataVersion_;
        }

        private void initFields() {
            this.extensionType_ = ExtensionType.AOS;
            this.extensionVersion_ = 0;
            this.browserType_ = BrowserType.CHROME;
            this.browserVersion_ = "";
            this.os_ = OS.WIN;
            this.osVersion_ = "";
            this.dataVersion_ = 0;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeEnum(1, this.extensionType_.getNumber());
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeSInt32(2, this.extensionVersion_);
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeEnum(3, this.browserType_.getNumber());
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeBytes(4, getBrowserVersionBytes());
            }
            if ((this.bitField0_ & 16) == 16) {
                codedOutputStream.writeEnum(5, this.os_.getNumber());
            }
            if ((this.bitField0_ & 32) == 32) {
                codedOutputStream.writeBytes(6, getOsVersionBytes());
            }
            if ((this.bitField0_ & 64) == 64) {
                codedOutputStream.writeSInt32(7, this.dataVersion_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeEnumSize(1, this.extensionType_.getNumber()) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeSInt32Size(2, this.extensionVersion_);
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeEnumSize(3, this.browserType_.getNumber());
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeBytesSize(4, getBrowserVersionBytes());
            }
            if ((this.bitField0_ & 16) == 16) {
                i += CodedOutputStream.computeEnumSize(5, this.os_.getNumber());
            }
            if ((this.bitField0_ & 32) == 32) {
                i += CodedOutputStream.computeBytesSize(6, getOsVersionBytes());
            }
            if ((this.bitField0_ & 64) == 64) {
                i += CodedOutputStream.computeSInt32Size(7, this.dataVersion_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static BrowserExtInfo parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (BrowserExtInfo) PARSER.parseFrom(byteString);
        }

        public static BrowserExtInfo parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (BrowserExtInfo) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static BrowserExtInfo parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (BrowserExtInfo) PARSER.parseFrom(bArr);
        }

        public static BrowserExtInfo parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (BrowserExtInfo) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static BrowserExtInfo parseFrom(InputStream inputStream) throws IOException {
            return (BrowserExtInfo) PARSER.parseFrom(inputStream);
        }

        public static BrowserExtInfo parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (BrowserExtInfo) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static BrowserExtInfo parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (BrowserExtInfo) PARSER.parseDelimitedFrom(inputStream);
        }

        public static BrowserExtInfo parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (BrowserExtInfo) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static BrowserExtInfo parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (BrowserExtInfo) PARSER.parseFrom(codedInputStream);
        }

        public static BrowserExtInfo parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (BrowserExtInfo) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(BrowserExtInfo browserExtInfo) {
            return newBuilder().mergeFrom(browserExtInfo);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public enum BrowserType implements EnumLite {
        CHROME(0, 0),
        FIREFOX(1, 1),
        IE(2, 2),
        OPERA(3, 3),
        SAFAR(4, 4),
        PRODUCTS(5, 5),
        VIDEO(6, 6),
        STOCK(7, 7),
        STOCK_JB(8, 8),
        DOLPHIN_MINI(9, 9),
        DOLPHIN(10, 10),
        SILK(11, 11),
        BOAT_MINI(12, 12),
        BOAT(13, 13),
        CHROME_M(14, 14);
        
        public static final int BOAT_MINI_VALUE = 12;
        public static final int BOAT_VALUE = 13;
        public static final int CHROME_M_VALUE = 14;
        public static final int CHROME_VALUE = 0;
        public static final int DOLPHIN_MINI_VALUE = 9;
        public static final int DOLPHIN_VALUE = 10;
        public static final int FIREFOX_VALUE = 1;
        public static final int IE_VALUE = 2;
        public static final int OPERA_VALUE = 3;
        public static final int PRODUCTS_VALUE = 5;
        public static final int SAFAR_VALUE = 4;
        public static final int SILK_VALUE = 11;
        public static final int STOCK_JB_VALUE = 8;
        public static final int STOCK_VALUE = 7;
        public static final int VIDEO_VALUE = 6;
        private static EnumLiteMap<BrowserType> internalValueMap;
        private final int value;

        static {
            internalValueMap = new EnumLiteMap<BrowserType>() {
                public BrowserType findValueByNumber(int i) {
                    return BrowserType.valueOf(i);
                }
            };
        }

        public final int getNumber() {
            return this.value;
        }

        public static BrowserType valueOf(int i) {
            switch (i) {
                case 0:
                    return CHROME;
                case 1:
                    return FIREFOX;
                case 2:
                    return IE;
                case 3:
                    return OPERA;
                case 4:
                    return SAFAR;
                case 5:
                    return PRODUCTS;
                case 6:
                    return VIDEO;
                case 7:
                    return STOCK;
                case 8:
                    return STOCK_JB;
                case 9:
                    return DOLPHIN_MINI;
                case 10:
                    return DOLPHIN;
                case 11:
                    return SILK;
                case 12:
                    return BOAT_MINI;
                case 13:
                    return BOAT;
                case 14:
                    return CHROME_M;
                default:
                    return null;
            }
        }

        public static EnumLiteMap<BrowserType> internalGetValueMap() {
            return internalValueMap;
        }

        private BrowserType(int i, int i2) {
            this.value = i2;
        }
    }

    public interface ClientOrBuilder extends MessageLiteOrBuilder {
        BrowserExtInfo getBrowserExtInfo();

        AvastIdentity getId();

        MessageClientInfo getMessageClientInfo();

        CType getType();

        boolean hasBrowserExtInfo();

        boolean hasId();

        boolean hasMessageClientInfo();

        boolean hasType();
    }

    public static final class Client extends GeneratedMessageLite implements ClientOrBuilder {
        public static final int BROWSEREXTINFO_FIELD_NUMBER = 3;
        public static final int ID_FIELD_NUMBER = 1;
        public static final int MESSAGECLIENTINFO_FIELD_NUMBER = 4;
        public static Parser<Client> PARSER = new AbstractParser<Client>() {
            public Client parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new Client(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int TYPE_FIELD_NUMBER = 2;
        private static final Client defaultInstance = new Client(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private BrowserExtInfo browserExtInfo_;
        private AvastIdentity id_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private MessageClientInfo messageClientInfo_;
        private CType type_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<Client, Builder> implements ClientOrBuilder {
            private int bitField0_;
            private BrowserExtInfo browserExtInfo_ = BrowserExtInfo.getDefaultInstance();
            private AvastIdentity id_ = AvastIdentity.getDefaultInstance();
            private MessageClientInfo messageClientInfo_ = MessageClientInfo.getDefaultInstance();
            private CType type_ = CType.TEST;

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.id_ = AvastIdentity.getDefaultInstance();
                this.bitField0_ &= -2;
                this.type_ = CType.TEST;
                this.bitField0_ &= -3;
                this.browserExtInfo_ = BrowserExtInfo.getDefaultInstance();
                this.bitField0_ &= -5;
                this.messageClientInfo_ = MessageClientInfo.getDefaultInstance();
                this.bitField0_ &= -9;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public Client getDefaultInstanceForType() {
                return Client.getDefaultInstance();
            }

            public Client build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public Client buildPartial() {
                Client client = new Client((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                client.id_ = this.id_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                client.type_ = this.type_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                client.browserExtInfo_ = this.browserExtInfo_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                client.messageClientInfo_ = this.messageClientInfo_;
                client.bitField0_ = i2;
                return client;
            }

            public Builder mergeFrom(Client client) {
                if (client == Client.getDefaultInstance()) {
                    return this;
                }
                if (client.hasId()) {
                    mergeId(client.getId());
                }
                if (client.hasType()) {
                    setType(client.getType());
                }
                if (client.hasBrowserExtInfo()) {
                    mergeBrowserExtInfo(client.getBrowserExtInfo());
                }
                if (client.hasMessageClientInfo()) {
                    mergeMessageClientInfo(client.getMessageClientInfo());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                Client client;
                Client client2;
                try {
                    client2 = (Client) Client.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (client2 != null) {
                        mergeFrom(client2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    client2 = (Client) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    client = client2;
                    th = th3;
                }
                if (client != null) {
                    mergeFrom(client);
                }
                throw th;
            }

            public boolean hasId() {
                return (this.bitField0_ & 1) == 1;
            }

            public AvastIdentity getId() {
                return this.id_;
            }

            public Builder setId(AvastIdentity avastIdentity) {
                if (avastIdentity != null) {
                    this.id_ = avastIdentity;
                    this.bitField0_ |= 1;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setId(Builder builder) {
                this.id_ = builder.build();
                this.bitField0_ |= 1;
                return this;
            }

            public Builder mergeId(AvastIdentity avastIdentity) {
                if ((this.bitField0_ & 1) == 1 && this.id_ != AvastIdentity.getDefaultInstance()) {
                    this.id_ = AvastIdentity.newBuilder(this.id_).mergeFrom(avastIdentity).buildPartial();
                } else {
                    this.id_ = avastIdentity;
                }
                this.bitField0_ |= 1;
                return this;
            }

            public Builder clearId() {
                this.id_ = AvastIdentity.getDefaultInstance();
                this.bitField0_ &= -2;
                return this;
            }

            public boolean hasType() {
                return (this.bitField0_ & 2) == 2;
            }

            public CType getType() {
                return this.type_;
            }

            public Builder setType(CType cType) {
                if (cType != null) {
                    this.bitField0_ |= 2;
                    this.type_ = cType;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearType() {
                this.bitField0_ &= -3;
                this.type_ = CType.TEST;
                return this;
            }

            public boolean hasBrowserExtInfo() {
                return (this.bitField0_ & 4) == 4;
            }

            public BrowserExtInfo getBrowserExtInfo() {
                return this.browserExtInfo_;
            }

            public Builder setBrowserExtInfo(BrowserExtInfo browserExtInfo) {
                if (browserExtInfo != null) {
                    this.browserExtInfo_ = browserExtInfo;
                    this.bitField0_ |= 4;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setBrowserExtInfo(Builder builder) {
                this.browserExtInfo_ = builder.build();
                this.bitField0_ |= 4;
                return this;
            }

            public Builder mergeBrowserExtInfo(BrowserExtInfo browserExtInfo) {
                if ((this.bitField0_ & 4) == 4 && this.browserExtInfo_ != BrowserExtInfo.getDefaultInstance()) {
                    this.browserExtInfo_ = BrowserExtInfo.newBuilder(this.browserExtInfo_).mergeFrom(browserExtInfo).buildPartial();
                } else {
                    this.browserExtInfo_ = browserExtInfo;
                }
                this.bitField0_ |= 4;
                return this;
            }

            public Builder clearBrowserExtInfo() {
                this.browserExtInfo_ = BrowserExtInfo.getDefaultInstance();
                this.bitField0_ &= -5;
                return this;
            }

            public boolean hasMessageClientInfo() {
                return (this.bitField0_ & 8) == 8;
            }

            public MessageClientInfo getMessageClientInfo() {
                return this.messageClientInfo_;
            }

            public Builder setMessageClientInfo(MessageClientInfo messageClientInfo) {
                if (messageClientInfo != null) {
                    this.messageClientInfo_ = messageClientInfo;
                    this.bitField0_ |= 8;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setMessageClientInfo(Builder builder) {
                this.messageClientInfo_ = builder.build();
                this.bitField0_ |= 8;
                return this;
            }

            public Builder mergeMessageClientInfo(MessageClientInfo messageClientInfo) {
                if ((this.bitField0_ & 8) == 8 && this.messageClientInfo_ != MessageClientInfo.getDefaultInstance()) {
                    this.messageClientInfo_ = MessageClientInfo.newBuilder(this.messageClientInfo_).mergeFrom(messageClientInfo).buildPartial();
                } else {
                    this.messageClientInfo_ = messageClientInfo;
                }
                this.bitField0_ |= 8;
                return this;
            }

            public Builder clearMessageClientInfo() {
                this.messageClientInfo_ = MessageClientInfo.getDefaultInstance();
                this.bitField0_ &= -9;
                return this;
            }
        }

        public enum CType implements EnumLite {
            TEST(0, 1),
            AVAST(1, 2),
            BROWSER_EXT(2, 3),
            MESSAGE(3, 4),
            PARTNER(4, 5),
            WEBSITE(5, 6);
            
            public static final int AVAST_VALUE = 2;
            public static final int BROWSER_EXT_VALUE = 3;
            public static final int MESSAGE_VALUE = 4;
            public static final int PARTNER_VALUE = 5;
            public static final int TEST_VALUE = 1;
            public static final int WEBSITE_VALUE = 6;
            private static EnumLiteMap<CType> internalValueMap;
            private final int value;

            static {
                internalValueMap = new EnumLiteMap<CType>() {
                    public CType findValueByNumber(int i) {
                        return CType.valueOf(i);
                    }
                };
            }

            public final int getNumber() {
                return this.value;
            }

            public static CType valueOf(int i) {
                switch (i) {
                    case 1:
                        return TEST;
                    case 2:
                        return AVAST;
                    case 3:
                        return BROWSER_EXT;
                    case 4:
                        return MESSAGE;
                    case 5:
                        return PARTNER;
                    case 6:
                        return WEBSITE;
                    default:
                        return null;
                }
            }

            public static EnumLiteMap<CType> internalGetValueMap() {
                return internalValueMap;
            }

            private CType(int i, int i2) {
                this.value = i2;
            }
        }

        private Client(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private Client(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static Client getDefaultInstance() {
            return defaultInstance;
        }

        public Client getDefaultInstanceForType() {
            return defaultInstance;
        }

        private Client(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            Object obj = null;
            while (obj == null) {
                try {
                    Object obj2;
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            readTag = 1;
                            break;
                        case 10:
                            Builder builder;
                            if ((this.bitField0_ & 1) != 1) {
                                builder = null;
                            } else {
                                builder = this.id_.toBuilder();
                            }
                            this.id_ = (AvastIdentity) codedInputStream.readMessage(AvastIdentity.PARSER, extensionRegistryLite);
                            if (builder != null) {
                                builder.mergeFrom(this.id_);
                                this.id_ = builder.buildPartial();
                            }
                            this.bitField0_ |= 1;
                            obj2 = obj;
                            break;
                        case 16:
                            CType valueOf = CType.valueOf(codedInputStream.readEnum());
                            if (valueOf != null) {
                                this.bitField0_ |= 2;
                                this.type_ = valueOf;
                                obj2 = obj;
                                break;
                            }
                            obj2 = obj;
                            break;
                        case 26:
                            Builder builder2;
                            if ((this.bitField0_ & 4) != 4) {
                                builder2 = null;
                            } else {
                                builder2 = this.browserExtInfo_.toBuilder();
                            }
                            this.browserExtInfo_ = (BrowserExtInfo) codedInputStream.readMessage(BrowserExtInfo.PARSER, extensionRegistryLite);
                            if (builder2 != null) {
                                builder2.mergeFrom(this.browserExtInfo_);
                                this.browserExtInfo_ = builder2.buildPartial();
                            }
                            this.bitField0_ |= 4;
                            obj2 = obj;
                            break;
                        case 34:
                            Builder builder3;
                            if ((this.bitField0_ & 8) != 8) {
                                builder3 = null;
                            } else {
                                builder3 = this.messageClientInfo_.toBuilder();
                            }
                            this.messageClientInfo_ = (MessageClientInfo) codedInputStream.readMessage(MessageClientInfo.PARSER, extensionRegistryLite);
                            if (builder3 != null) {
                                builder3.mergeFrom(this.messageClientInfo_);
                                this.messageClientInfo_ = builder3.buildPartial();
                            }
                            this.bitField0_ |= 8;
                            obj2 = obj;
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                readTag = 1;
                                break;
                            } else {
                                obj2 = obj;
                                break;
                            }
                    }
                    obj = obj2;
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<Client> getParserForType() {
            return PARSER;
        }

        public boolean hasId() {
            return (this.bitField0_ & 1) == 1;
        }

        public AvastIdentity getId() {
            return this.id_;
        }

        public boolean hasType() {
            return (this.bitField0_ & 2) == 2;
        }

        public CType getType() {
            return this.type_;
        }

        public boolean hasBrowserExtInfo() {
            return (this.bitField0_ & 4) == 4;
        }

        public BrowserExtInfo getBrowserExtInfo() {
            return this.browserExtInfo_;
        }

        public boolean hasMessageClientInfo() {
            return (this.bitField0_ & 8) == 8;
        }

        public MessageClientInfo getMessageClientInfo() {
            return this.messageClientInfo_;
        }

        private void initFields() {
            this.id_ = AvastIdentity.getDefaultInstance();
            this.type_ = CType.TEST;
            this.browserExtInfo_ = BrowserExtInfo.getDefaultInstance();
            this.messageClientInfo_ = MessageClientInfo.getDefaultInstance();
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeMessage(1, this.id_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeEnum(2, this.type_.getNumber());
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeMessage(3, this.browserExtInfo_);
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeMessage(4, this.messageClientInfo_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.id_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeEnumSize(2, this.type_.getNumber());
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeMessageSize(3, this.browserExtInfo_);
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeMessageSize(4, this.messageClientInfo_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static Client parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (Client) PARSER.parseFrom(byteString);
        }

        public static Client parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Client) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static Client parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (Client) PARSER.parseFrom(bArr);
        }

        public static Client parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Client) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static Client parseFrom(InputStream inputStream) throws IOException {
            return (Client) PARSER.parseFrom(inputStream);
        }

        public static Client parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Client) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static Client parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (Client) PARSER.parseDelimitedFrom(inputStream);
        }

        public static Client parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Client) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static Client parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (Client) PARSER.parseFrom(codedInputStream);
        }

        public static Client parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Client) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(Client client) {
            return newBuilder().mergeFrom(client);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public enum EventType implements EnumLite {
        CLICK(0, 0),
        FRESHOPEN(1, 1),
        REOPEN(2, 2);
        
        public static final int CLICK_VALUE = 0;
        public static final int FRESHOPEN_VALUE = 1;
        public static final int REOPEN_VALUE = 2;
        private static EnumLiteMap<EventType> internalValueMap;
        private final int value;

        static {
            internalValueMap = new EnumLiteMap<EventType>() {
                public EventType findValueByNumber(int i) {
                    return EventType.valueOf(i);
                }
            };
        }

        public final int getNumber() {
            return this.value;
        }

        public static EventType valueOf(int i) {
            switch (i) {
                case 0:
                    return CLICK;
                case 1:
                    return FRESHOPEN;
                case 2:
                    return REOPEN;
                default:
                    return null;
            }
        }

        public static EnumLiteMap<EventType> internalGetValueMap() {
            return internalValueMap;
        }

        private EventType(int i, int i2) {
            this.value = i2;
        }
    }

    public enum ExtensionType implements EnumLite {
        AOS(0, 1),
        SP(1, 2),
        AOSP(2, 3),
        ABOS(3, 4);
        
        public static final int ABOS_VALUE = 4;
        public static final int AOSP_VALUE = 3;
        public static final int AOS_VALUE = 1;
        public static final int SP_VALUE = 2;
        private static EnumLiteMap<ExtensionType> internalValueMap;
        private final int value;

        static {
            internalValueMap = new EnumLiteMap<ExtensionType>() {
                public ExtensionType findValueByNumber(int i) {
                    return ExtensionType.valueOf(i);
                }
            };
        }

        public final int getNumber() {
            return this.value;
        }

        public static ExtensionType valueOf(int i) {
            switch (i) {
                case 1:
                    return AOS;
                case 2:
                    return SP;
                case 3:
                    return AOSP;
                case 4:
                    return ABOS;
                default:
                    return null;
            }
        }

        public static EnumLiteMap<ExtensionType> internalGetValueMap() {
            return internalValueMap;
        }

        private ExtensionType(int i, int i2) {
            this.value = i2;
        }
    }

    public interface IdentityOrBuilder extends MessageLiteOrBuilder {
        ByteString getAuid();

        BrowserType getBrowserType();

        ByteString getGuid();

        ByteString getHwid();

        ByteString getIpAddress();

        ByteString getProduct();

        ByteString getToken();

        int getTokenVerified();

        ByteString getUserid();

        ByteString getUuid();

        ByteString getVersion();

        boolean hasAuid();

        boolean hasBrowserType();

        boolean hasGuid();

        boolean hasHwid();

        boolean hasIpAddress();

        boolean hasProduct();

        boolean hasToken();

        boolean hasTokenVerified();

        boolean hasUserid();

        boolean hasUuid();

        boolean hasVersion();
    }

    public static final class Identity extends GeneratedMessageLite implements IdentityOrBuilder {
        public static final int AUID_FIELD_NUMBER = 4;
        public static final int BROWSERTYPE_FIELD_NUMBER = 5;
        public static final int GUID_FIELD_NUMBER = 1;
        public static final int HWID_FIELD_NUMBER = 11;
        public static final int IP_ADDRESS_FIELD_NUMBER = 7;
        public static Parser<Identity> PARSER = new AbstractParser<Identity>() {
            public Identity parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new Identity(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int PRODUCT_FIELD_NUMBER = 9;
        public static final int TOKEN_FIELD_NUMBER = 3;
        public static final int TOKEN_VERIFIED_FIELD_NUMBER = 6;
        public static final int USERID_FIELD_NUMBER = 8;
        public static final int UUID_FIELD_NUMBER = 2;
        public static final int VERSION_FIELD_NUMBER = 10;
        private static final Identity defaultInstance = new Identity(true);
        private static final long serialVersionUID = 0;
        private ByteString auid_;
        private int bitField0_;
        private BrowserType browserType_;
        private ByteString guid_;
        private ByteString hwid_;
        private ByteString ipAddress_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private ByteString product_;
        private int tokenVerified_;
        private ByteString token_;
        private ByteString userid_;
        private ByteString uuid_;
        private ByteString version_;

        public enum BrowserType implements EnumLite {
            CHROME(0, 0),
            FIREFOX(1, 1),
            IE(2, 2),
            OPERA(3, 3),
            SAFAR(4, 4),
            PRODUCTS(5, 5),
            VIDEO(6, 6);
            
            public static final int CHROME_VALUE = 0;
            public static final int FIREFOX_VALUE = 1;
            public static final int IE_VALUE = 2;
            public static final int OPERA_VALUE = 3;
            public static final int PRODUCTS_VALUE = 5;
            public static final int SAFAR_VALUE = 4;
            public static final int VIDEO_VALUE = 6;
            private static EnumLiteMap<BrowserType> internalValueMap;
            private final int value;

            static {
                internalValueMap = new EnumLiteMap<BrowserType>() {
                    public BrowserType findValueByNumber(int i) {
                        return BrowserType.valueOf(i);
                    }
                };
            }

            public final int getNumber() {
                return this.value;
            }

            public static BrowserType valueOf(int i) {
                switch (i) {
                    case 0:
                        return CHROME;
                    case 1:
                        return FIREFOX;
                    case 2:
                        return IE;
                    case 3:
                        return OPERA;
                    case 4:
                        return SAFAR;
                    case 5:
                        return PRODUCTS;
                    case 6:
                        return VIDEO;
                    default:
                        return null;
                }
            }

            public static EnumLiteMap<BrowserType> internalGetValueMap() {
                return internalValueMap;
            }

            private BrowserType(int i, int i2) {
                this.value = i2;
            }
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<Identity, Builder> implements IdentityOrBuilder {
            private ByteString auid_ = ByteString.EMPTY;
            private int bitField0_;
            private BrowserType browserType_ = BrowserType.CHROME;
            private ByteString guid_ = ByteString.EMPTY;
            private ByteString hwid_ = ByteString.EMPTY;
            private ByteString ipAddress_ = ByteString.EMPTY;
            private ByteString product_ = ByteString.EMPTY;
            private int tokenVerified_;
            private ByteString token_ = ByteString.EMPTY;
            private ByteString userid_ = ByteString.EMPTY;
            private ByteString uuid_ = ByteString.EMPTY;
            private ByteString version_ = ByteString.EMPTY;

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.guid_ = ByteString.EMPTY;
                this.bitField0_ &= -2;
                this.uuid_ = ByteString.EMPTY;
                this.bitField0_ &= -3;
                this.token_ = ByteString.EMPTY;
                this.bitField0_ &= -5;
                this.auid_ = ByteString.EMPTY;
                this.bitField0_ &= -9;
                this.browserType_ = BrowserType.CHROME;
                this.bitField0_ &= -17;
                this.tokenVerified_ = 0;
                this.bitField0_ &= -33;
                this.ipAddress_ = ByteString.EMPTY;
                this.bitField0_ &= -65;
                this.userid_ = ByteString.EMPTY;
                this.bitField0_ &= -129;
                this.product_ = ByteString.EMPTY;
                this.bitField0_ &= -257;
                this.version_ = ByteString.EMPTY;
                this.bitField0_ &= -513;
                this.hwid_ = ByteString.EMPTY;
                this.bitField0_ &= -1025;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public Identity getDefaultInstanceForType() {
                return Identity.getDefaultInstance();
            }

            public Identity build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public Identity buildPartial() {
                Identity identity = new Identity((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                identity.guid_ = this.guid_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                identity.uuid_ = this.uuid_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                identity.token_ = this.token_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                identity.auid_ = this.auid_;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                identity.browserType_ = this.browserType_;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                identity.tokenVerified_ = this.tokenVerified_;
                if ((i & 64) == 64) {
                    i2 |= 64;
                }
                identity.ipAddress_ = this.ipAddress_;
                if ((i & 128) == 128) {
                    i2 |= 128;
                }
                identity.userid_ = this.userid_;
                if ((i & 256) == 256) {
                    i2 |= 256;
                }
                identity.product_ = this.product_;
                if ((i & 512) == 512) {
                    i2 |= 512;
                }
                identity.version_ = this.version_;
                if ((i & 1024) == 1024) {
                    i2 |= 1024;
                }
                identity.hwid_ = this.hwid_;
                identity.bitField0_ = i2;
                return identity;
            }

            public Builder mergeFrom(Identity identity) {
                if (identity == Identity.getDefaultInstance()) {
                    return this;
                }
                if (identity.hasGuid()) {
                    setGuid(identity.getGuid());
                }
                if (identity.hasUuid()) {
                    setUuid(identity.getUuid());
                }
                if (identity.hasToken()) {
                    setToken(identity.getToken());
                }
                if (identity.hasAuid()) {
                    setAuid(identity.getAuid());
                }
                if (identity.hasBrowserType()) {
                    setBrowserType(identity.getBrowserType());
                }
                if (identity.hasTokenVerified()) {
                    setTokenVerified(identity.getTokenVerified());
                }
                if (identity.hasIpAddress()) {
                    setIpAddress(identity.getIpAddress());
                }
                if (identity.hasUserid()) {
                    setUserid(identity.getUserid());
                }
                if (identity.hasProduct()) {
                    setProduct(identity.getProduct());
                }
                if (identity.hasVersion()) {
                    setVersion(identity.getVersion());
                }
                if (identity.hasHwid()) {
                    setHwid(identity.getHwid());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                Identity identity;
                Identity identity2;
                try {
                    identity2 = (Identity) Identity.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (identity2 != null) {
                        mergeFrom(identity2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    identity2 = (Identity) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    identity = identity2;
                    th = th3;
                }
                if (identity != null) {
                    mergeFrom(identity);
                }
                throw th;
            }

            public boolean hasGuid() {
                return (this.bitField0_ & 1) == 1;
            }

            public ByteString getGuid() {
                return this.guid_;
            }

            public Builder setGuid(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.guid_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearGuid() {
                this.bitField0_ &= -2;
                this.guid_ = Identity.getDefaultInstance().getGuid();
                return this;
            }

            public boolean hasUuid() {
                return (this.bitField0_ & 2) == 2;
            }

            public ByteString getUuid() {
                return this.uuid_;
            }

            public Builder setUuid(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.uuid_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearUuid() {
                this.bitField0_ &= -3;
                this.uuid_ = Identity.getDefaultInstance().getUuid();
                return this;
            }

            public boolean hasToken() {
                return (this.bitField0_ & 4) == 4;
            }

            public ByteString getToken() {
                return this.token_;
            }

            public Builder setToken(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 4;
                    this.token_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearToken() {
                this.bitField0_ &= -5;
                this.token_ = Identity.getDefaultInstance().getToken();
                return this;
            }

            public boolean hasAuid() {
                return (this.bitField0_ & 8) == 8;
            }

            public ByteString getAuid() {
                return this.auid_;
            }

            public Builder setAuid(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 8;
                    this.auid_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearAuid() {
                this.bitField0_ &= -9;
                this.auid_ = Identity.getDefaultInstance().getAuid();
                return this;
            }

            public boolean hasBrowserType() {
                return (this.bitField0_ & 16) == 16;
            }

            public BrowserType getBrowserType() {
                return this.browserType_;
            }

            public Builder setBrowserType(BrowserType browserType) {
                if (browserType != null) {
                    this.bitField0_ |= 16;
                    this.browserType_ = browserType;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearBrowserType() {
                this.bitField0_ &= -17;
                this.browserType_ = BrowserType.CHROME;
                return this;
            }

            public boolean hasTokenVerified() {
                return (this.bitField0_ & 32) == 32;
            }

            public int getTokenVerified() {
                return this.tokenVerified_;
            }

            public Builder setTokenVerified(int i) {
                this.bitField0_ |= 32;
                this.tokenVerified_ = i;
                return this;
            }

            public Builder clearTokenVerified() {
                this.bitField0_ &= -33;
                this.tokenVerified_ = 0;
                return this;
            }

            public boolean hasIpAddress() {
                return (this.bitField0_ & 64) == 64;
            }

            public ByteString getIpAddress() {
                return this.ipAddress_;
            }

            public Builder setIpAddress(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 64;
                    this.ipAddress_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearIpAddress() {
                this.bitField0_ &= -65;
                this.ipAddress_ = Identity.getDefaultInstance().getIpAddress();
                return this;
            }

            public boolean hasUserid() {
                return (this.bitField0_ & 128) == 128;
            }

            public ByteString getUserid() {
                return this.userid_;
            }

            public Builder setUserid(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 128;
                    this.userid_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearUserid() {
                this.bitField0_ &= -129;
                this.userid_ = Identity.getDefaultInstance().getUserid();
                return this;
            }

            public boolean hasProduct() {
                return (this.bitField0_ & 256) == 256;
            }

            public ByteString getProduct() {
                return this.product_;
            }

            public Builder setProduct(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 256;
                    this.product_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearProduct() {
                this.bitField0_ &= -257;
                this.product_ = Identity.getDefaultInstance().getProduct();
                return this;
            }

            public boolean hasVersion() {
                return (this.bitField0_ & 512) == 512;
            }

            public ByteString getVersion() {
                return this.version_;
            }

            public Builder setVersion(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 512;
                    this.version_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearVersion() {
                this.bitField0_ &= -513;
                this.version_ = Identity.getDefaultInstance().getVersion();
                return this;
            }

            public boolean hasHwid() {
                return (this.bitField0_ & 1024) == 1024;
            }

            public ByteString getHwid() {
                return this.hwid_;
            }

            public Builder setHwid(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1024;
                    this.hwid_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearHwid() {
                this.bitField0_ &= -1025;
                this.hwid_ = Identity.getDefaultInstance().getHwid();
                return this;
            }
        }

        private Identity(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private Identity(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static Identity getDefaultInstance() {
            return defaultInstance;
        }

        public Identity getDefaultInstanceForType() {
            return defaultInstance;
        }

        private Identity(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 10:
                            this.bitField0_ |= 1;
                            this.guid_ = codedInputStream.readBytes();
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.uuid_ = codedInputStream.readBytes();
                            break;
                        case 26:
                            this.bitField0_ |= 4;
                            this.token_ = codedInputStream.readBytes();
                            break;
                        case 34:
                            this.bitField0_ |= 8;
                            this.auid_ = codedInputStream.readBytes();
                            break;
                        case 40:
                            BrowserType valueOf = BrowserType.valueOf(codedInputStream.readEnum());
                            if (valueOf == null) {
                                break;
                            }
                            this.bitField0_ |= 16;
                            this.browserType_ = valueOf;
                            break;
                        case HwRcsCommonObject.BLACKLIST_MSG_VIDEO_TYPE /*48*/:
                            this.bitField0_ |= 32;
                            this.tokenVerified_ = codedInputStream.readSInt32();
                            break;
                        case 58:
                            this.bitField0_ |= 64;
                            this.ipAddress_ = codedInputStream.readBytes();
                            break;
                        case Events.E_ANTISPAM_VIEW_CALL /*66*/:
                            this.bitField0_ |= 128;
                            this.userid_ = codedInputStream.readBytes();
                            break;
                        case 74:
                            this.bitField0_ |= 256;
                            this.product_ = codedInputStream.readBytes();
                            break;
                        case Events.E_ANTIVIRUS_SCAN /*82*/:
                            this.bitField0_ |= 512;
                            this.version_ = codedInputStream.readBytes();
                            break;
                        case 90:
                            this.bitField0_ |= 1024;
                            this.hwid_ = codedInputStream.readBytes();
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                obj = 1;
                                break;
                            }
                            break;
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<Identity> getParserForType() {
            return PARSER;
        }

        public boolean hasGuid() {
            return (this.bitField0_ & 1) == 1;
        }

        public ByteString getGuid() {
            return this.guid_;
        }

        public boolean hasUuid() {
            return (this.bitField0_ & 2) == 2;
        }

        public ByteString getUuid() {
            return this.uuid_;
        }

        public boolean hasToken() {
            return (this.bitField0_ & 4) == 4;
        }

        public ByteString getToken() {
            return this.token_;
        }

        public boolean hasAuid() {
            return (this.bitField0_ & 8) == 8;
        }

        public ByteString getAuid() {
            return this.auid_;
        }

        public boolean hasBrowserType() {
            return (this.bitField0_ & 16) == 16;
        }

        public BrowserType getBrowserType() {
            return this.browserType_;
        }

        public boolean hasTokenVerified() {
            return (this.bitField0_ & 32) == 32;
        }

        public int getTokenVerified() {
            return this.tokenVerified_;
        }

        public boolean hasIpAddress() {
            return (this.bitField0_ & 64) == 64;
        }

        public ByteString getIpAddress() {
            return this.ipAddress_;
        }

        public boolean hasUserid() {
            return (this.bitField0_ & 128) == 128;
        }

        public ByteString getUserid() {
            return this.userid_;
        }

        public boolean hasProduct() {
            return (this.bitField0_ & 256) == 256;
        }

        public ByteString getProduct() {
            return this.product_;
        }

        public boolean hasVersion() {
            return (this.bitField0_ & 512) == 512;
        }

        public ByteString getVersion() {
            return this.version_;
        }

        public boolean hasHwid() {
            return (this.bitField0_ & 1024) == 1024;
        }

        public ByteString getHwid() {
            return this.hwid_;
        }

        private void initFields() {
            this.guid_ = ByteString.EMPTY;
            this.uuid_ = ByteString.EMPTY;
            this.token_ = ByteString.EMPTY;
            this.auid_ = ByteString.EMPTY;
            this.browserType_ = BrowserType.CHROME;
            this.tokenVerified_ = 0;
            this.ipAddress_ = ByteString.EMPTY;
            this.userid_ = ByteString.EMPTY;
            this.product_ = ByteString.EMPTY;
            this.version_ = ByteString.EMPTY;
            this.hwid_ = ByteString.EMPTY;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeBytes(1, this.guid_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, this.uuid_);
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeBytes(3, this.token_);
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeBytes(4, this.auid_);
            }
            if ((this.bitField0_ & 16) == 16) {
                codedOutputStream.writeEnum(5, this.browserType_.getNumber());
            }
            if ((this.bitField0_ & 32) == 32) {
                codedOutputStream.writeSInt32(6, this.tokenVerified_);
            }
            if ((this.bitField0_ & 64) == 64) {
                codedOutputStream.writeBytes(7, this.ipAddress_);
            }
            if ((this.bitField0_ & 128) == 128) {
                codedOutputStream.writeBytes(8, this.userid_);
            }
            if ((this.bitField0_ & 256) == 256) {
                codedOutputStream.writeBytes(9, this.product_);
            }
            if ((this.bitField0_ & 512) == 512) {
                codedOutputStream.writeBytes(10, this.version_);
            }
            if ((this.bitField0_ & 1024) == 1024) {
                codedOutputStream.writeBytes(11, this.hwid_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, this.guid_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, this.uuid_);
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeBytesSize(3, this.token_);
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeBytesSize(4, this.auid_);
            }
            if ((this.bitField0_ & 16) == 16) {
                i += CodedOutputStream.computeEnumSize(5, this.browserType_.getNumber());
            }
            if ((this.bitField0_ & 32) == 32) {
                i += CodedOutputStream.computeSInt32Size(6, this.tokenVerified_);
            }
            if ((this.bitField0_ & 64) == 64) {
                i += CodedOutputStream.computeBytesSize(7, this.ipAddress_);
            }
            if ((this.bitField0_ & 128) == 128) {
                i += CodedOutputStream.computeBytesSize(8, this.userid_);
            }
            if ((this.bitField0_ & 256) == 256) {
                i += CodedOutputStream.computeBytesSize(9, this.product_);
            }
            if ((this.bitField0_ & 512) == 512) {
                i += CodedOutputStream.computeBytesSize(10, this.version_);
            }
            if ((this.bitField0_ & 1024) == 1024) {
                i += CodedOutputStream.computeBytesSize(11, this.hwid_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static Identity parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (Identity) PARSER.parseFrom(byteString);
        }

        public static Identity parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Identity) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static Identity parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (Identity) PARSER.parseFrom(bArr);
        }

        public static Identity parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Identity) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static Identity parseFrom(InputStream inputStream) throws IOException {
            return (Identity) PARSER.parseFrom(inputStream);
        }

        public static Identity parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Identity) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static Identity parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (Identity) PARSER.parseDelimitedFrom(inputStream);
        }

        public static Identity parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Identity) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static Identity parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (Identity) PARSER.parseFrom(codedInputStream);
        }

        public static Identity parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Identity) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(Identity identity) {
            return newBuilder().mergeFrom(identity);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface KeyValueOrBuilder extends MessageLiteOrBuilder {
        String getKey();

        ByteString getKeyBytes();

        String getValue();

        ByteString getValueBytes();

        boolean hasKey();

        boolean hasValue();
    }

    public static final class KeyValue extends GeneratedMessageLite implements KeyValueOrBuilder {
        public static final int KEY_FIELD_NUMBER = 1;
        public static Parser<KeyValue> PARSER = new AbstractParser<KeyValue>() {
            public KeyValue parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new KeyValue(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int VALUE_FIELD_NUMBER = 2;
        private static final KeyValue defaultInstance = new KeyValue(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private Object key_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object value_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<KeyValue, Builder> implements KeyValueOrBuilder {
            private int bitField0_;
            private Object key_ = "";
            private Object value_ = "";

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.key_ = "";
                this.bitField0_ &= -2;
                this.value_ = "";
                this.bitField0_ &= -3;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public KeyValue getDefaultInstanceForType() {
                return KeyValue.getDefaultInstance();
            }

            public KeyValue build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public KeyValue buildPartial() {
                KeyValue keyValue = new KeyValue((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                keyValue.key_ = this.key_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                keyValue.value_ = this.value_;
                keyValue.bitField0_ = i2;
                return keyValue;
            }

            public Builder mergeFrom(KeyValue keyValue) {
                if (keyValue == KeyValue.getDefaultInstance()) {
                    return this;
                }
                if (keyValue.hasKey()) {
                    this.bitField0_ |= 1;
                    this.key_ = keyValue.key_;
                }
                if (keyValue.hasValue()) {
                    this.bitField0_ |= 2;
                    this.value_ = keyValue.value_;
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                KeyValue keyValue;
                Throwable th;
                KeyValue keyValue2;
                try {
                    keyValue = (KeyValue) KeyValue.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (keyValue != null) {
                        mergeFrom(keyValue);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    keyValue = (KeyValue) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    keyValue2 = keyValue;
                    th = th3;
                }
                if (keyValue2 != null) {
                    mergeFrom(keyValue2);
                }
                throw th;
            }

            public boolean hasKey() {
                return (this.bitField0_ & 1) == 1;
            }

            public String getKey() {
                Object obj = this.key_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.key_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getKeyBytes() {
                Object obj = this.key_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.key_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setKey(String str) {
                if (str != null) {
                    this.bitField0_ |= 1;
                    this.key_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearKey() {
                this.bitField0_ &= -2;
                this.key_ = KeyValue.getDefaultInstance().getKey();
                return this;
            }

            public Builder setKeyBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.key_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasValue() {
                return (this.bitField0_ & 2) == 2;
            }

            public String getValue() {
                Object obj = this.value_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.value_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getValueBytes() {
                Object obj = this.value_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.value_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setValue(String str) {
                if (str != null) {
                    this.bitField0_ |= 2;
                    this.value_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearValue() {
                this.bitField0_ &= -3;
                this.value_ = KeyValue.getDefaultInstance().getValue();
                return this;
            }

            public Builder setValueBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.value_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }
        }

        private KeyValue(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private KeyValue(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static KeyValue getDefaultInstance() {
            return defaultInstance;
        }

        public KeyValue getDefaultInstanceForType() {
            return defaultInstance;
        }

        private KeyValue(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 10:
                            this.bitField0_ |= 1;
                            this.key_ = codedInputStream.readBytes();
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.value_ = codedInputStream.readBytes();
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                obj = 1;
                                break;
                            }
                            break;
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<KeyValue> getParserForType() {
            return PARSER;
        }

        public boolean hasKey() {
            return (this.bitField0_ & 1) == 1;
        }

        public String getKey() {
            Object obj = this.key_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.key_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getKeyBytes() {
            Object obj = this.key_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.key_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasValue() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getValue() {
            Object obj = this.value_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.value_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getValueBytes() {
            Object obj = this.value_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.value_ = copyFromUtf8;
            return copyFromUtf8;
        }

        private void initFields() {
            this.key_ = "";
            this.value_ = "";
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeBytes(1, getKeyBytes());
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, getValueBytes());
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, getKeyBytes()) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, getValueBytes());
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static KeyValue parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (KeyValue) PARSER.parseFrom(byteString);
        }

        public static KeyValue parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (KeyValue) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static KeyValue parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (KeyValue) PARSER.parseFrom(bArr);
        }

        public static KeyValue parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (KeyValue) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static KeyValue parseFrom(InputStream inputStream) throws IOException {
            return (KeyValue) PARSER.parseFrom(inputStream);
        }

        public static KeyValue parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (KeyValue) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static KeyValue parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (KeyValue) PARSER.parseDelimitedFrom(inputStream);
        }

        public static KeyValue parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (KeyValue) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static KeyValue parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (KeyValue) PARSER.parseFrom(codedInputStream);
        }

        public static KeyValue parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (KeyValue) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(KeyValue keyValue) {
            return newBuilder().mergeFrom(keyValue);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface MessageClientInfoOrBuilder extends MessageLiteOrBuilder {
        int getDataVersion();

        OS getOs();

        String getOsVersion();

        ByteString getOsVersionBytes();

        boolean hasDataVersion();

        boolean hasOs();

        boolean hasOsVersion();
    }

    public static final class MessageClientInfo extends GeneratedMessageLite implements MessageClientInfoOrBuilder {
        public static final int DATAVERSION_FIELD_NUMBER = 3;
        public static final int OSVERSION_FIELD_NUMBER = 2;
        public static final int OS_FIELD_NUMBER = 1;
        public static Parser<MessageClientInfo> PARSER = new AbstractParser<MessageClientInfo>() {
            public MessageClientInfo parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new MessageClientInfo(codedInputStream, extensionRegistryLite);
            }
        };
        private static final MessageClientInfo defaultInstance = new MessageClientInfo(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private int dataVersion_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object osVersion_;
        private OS os_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<MessageClientInfo, Builder> implements MessageClientInfoOrBuilder {
            private int bitField0_;
            private int dataVersion_;
            private Object osVersion_ = "";
            private OS os_ = OS.WIN;

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.os_ = OS.WIN;
                this.bitField0_ &= -2;
                this.osVersion_ = "";
                this.bitField0_ &= -3;
                this.dataVersion_ = 0;
                this.bitField0_ &= -5;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public MessageClientInfo getDefaultInstanceForType() {
                return MessageClientInfo.getDefaultInstance();
            }

            public MessageClientInfo build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public MessageClientInfo buildPartial() {
                MessageClientInfo messageClientInfo = new MessageClientInfo((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                messageClientInfo.os_ = this.os_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                messageClientInfo.osVersion_ = this.osVersion_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                messageClientInfo.dataVersion_ = this.dataVersion_;
                messageClientInfo.bitField0_ = i2;
                return messageClientInfo;
            }

            public Builder mergeFrom(MessageClientInfo messageClientInfo) {
                if (messageClientInfo == MessageClientInfo.getDefaultInstance()) {
                    return this;
                }
                if (messageClientInfo.hasOs()) {
                    setOs(messageClientInfo.getOs());
                }
                if (messageClientInfo.hasOsVersion()) {
                    this.bitField0_ |= 2;
                    this.osVersion_ = messageClientInfo.osVersion_;
                }
                if (messageClientInfo.hasDataVersion()) {
                    setDataVersion(messageClientInfo.getDataVersion());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                MessageClientInfo messageClientInfo;
                MessageClientInfo messageClientInfo2;
                try {
                    messageClientInfo2 = (MessageClientInfo) MessageClientInfo.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (messageClientInfo2 != null) {
                        mergeFrom(messageClientInfo2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    messageClientInfo2 = (MessageClientInfo) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    messageClientInfo = messageClientInfo2;
                    th = th3;
                }
                if (messageClientInfo != null) {
                    mergeFrom(messageClientInfo);
                }
                throw th;
            }

            public boolean hasOs() {
                return (this.bitField0_ & 1) == 1;
            }

            public OS getOs() {
                return this.os_;
            }

            public Builder setOs(OS os) {
                if (os != null) {
                    this.bitField0_ |= 1;
                    this.os_ = os;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearOs() {
                this.bitField0_ &= -2;
                this.os_ = OS.WIN;
                return this;
            }

            public boolean hasOsVersion() {
                return (this.bitField0_ & 2) == 2;
            }

            public String getOsVersion() {
                Object obj = this.osVersion_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.osVersion_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getOsVersionBytes() {
                Object obj = this.osVersion_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.osVersion_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setOsVersion(String str) {
                if (str != null) {
                    this.bitField0_ |= 2;
                    this.osVersion_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearOsVersion() {
                this.bitField0_ &= -3;
                this.osVersion_ = MessageClientInfo.getDefaultInstance().getOsVersion();
                return this;
            }

            public Builder setOsVersionBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.osVersion_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasDataVersion() {
                return (this.bitField0_ & 4) == 4;
            }

            public int getDataVersion() {
                return this.dataVersion_;
            }

            public Builder setDataVersion(int i) {
                this.bitField0_ |= 4;
                this.dataVersion_ = i;
                return this;
            }

            public Builder clearDataVersion() {
                this.bitField0_ &= -5;
                this.dataVersion_ = 0;
                return this;
            }
        }

        private MessageClientInfo(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private MessageClientInfo(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static MessageClientInfo getDefaultInstance() {
            return defaultInstance;
        }

        public MessageClientInfo getDefaultInstanceForType() {
            return defaultInstance;
        }

        private MessageClientInfo(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 8:
                            OS valueOf = OS.valueOf(codedInputStream.readEnum());
                            if (valueOf == null) {
                                break;
                            }
                            this.bitField0_ |= 1;
                            this.os_ = valueOf;
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.osVersion_ = codedInputStream.readBytes();
                            break;
                        case 24:
                            this.bitField0_ |= 4;
                            this.dataVersion_ = codedInputStream.readSInt32();
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                obj = 1;
                                break;
                            }
                            break;
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<MessageClientInfo> getParserForType() {
            return PARSER;
        }

        public boolean hasOs() {
            return (this.bitField0_ & 1) == 1;
        }

        public OS getOs() {
            return this.os_;
        }

        public boolean hasOsVersion() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getOsVersion() {
            Object obj = this.osVersion_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.osVersion_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getOsVersionBytes() {
            Object obj = this.osVersion_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.osVersion_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasDataVersion() {
            return (this.bitField0_ & 4) == 4;
        }

        public int getDataVersion() {
            return this.dataVersion_;
        }

        private void initFields() {
            this.os_ = OS.WIN;
            this.osVersion_ = "";
            this.dataVersion_ = 0;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeEnum(1, this.os_.getNumber());
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, getOsVersionBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeSInt32(3, this.dataVersion_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeEnumSize(1, this.os_.getNumber()) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, getOsVersionBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeSInt32Size(3, this.dataVersion_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static MessageClientInfo parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (MessageClientInfo) PARSER.parseFrom(byteString);
        }

        public static MessageClientInfo parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (MessageClientInfo) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static MessageClientInfo parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (MessageClientInfo) PARSER.parseFrom(bArr);
        }

        public static MessageClientInfo parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (MessageClientInfo) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static MessageClientInfo parseFrom(InputStream inputStream) throws IOException {
            return (MessageClientInfo) PARSER.parseFrom(inputStream);
        }

        public static MessageClientInfo parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (MessageClientInfo) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static MessageClientInfo parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (MessageClientInfo) PARSER.parseDelimitedFrom(inputStream);
        }

        public static MessageClientInfo parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (MessageClientInfo) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static MessageClientInfo parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (MessageClientInfo) PARSER.parseFrom(codedInputStream);
        }

        public static MessageClientInfo parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (MessageClientInfo) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(MessageClientInfo messageClientInfo) {
            return newBuilder().mergeFrom(messageClientInfo);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public enum OS implements EnumLite {
        WIN(0, 1),
        MAC(1, 2),
        LINUX(2, 3),
        ANDROID(3, 4),
        IOS(4, 5);
        
        public static final int ANDROID_VALUE = 4;
        public static final int IOS_VALUE = 5;
        public static final int LINUX_VALUE = 3;
        public static final int MAC_VALUE = 2;
        public static final int WIN_VALUE = 1;
        private static EnumLiteMap<OS> internalValueMap;
        private final int value;

        static {
            internalValueMap = new EnumLiteMap<OS>() {
                public OS findValueByNumber(int i) {
                    return OS.valueOf(i);
                }
            };
        }

        public final int getNumber() {
            return this.value;
        }

        public static OS valueOf(int i) {
            switch (i) {
                case 1:
                    return WIN;
                case 2:
                    return MAC;
                case 3:
                    return LINUX;
                case 4:
                    return ANDROID;
                case 5:
                    return IOS;
                default:
                    return null;
            }
        }

        public static EnumLiteMap<OS> internalGetValueMap() {
            return internalValueMap;
        }

        private OS(int i, int i2) {
            this.value = i2;
        }
    }

    public enum OriginType implements EnumLite {
        LINK(0, 0),
        ADDRESSBAR(1, 1),
        BOOKMARK(2, 2),
        SEARCHWINDOW(3, 3),
        JAVASCRIPT(4, 4),
        REDIRECT(5, 5),
        HOMEPAGE(6, 6);
        
        public static final int ADDRESSBAR_VALUE = 1;
        public static final int BOOKMARK_VALUE = 2;
        public static final int HOMEPAGE_VALUE = 6;
        public static final int JAVASCRIPT_VALUE = 4;
        public static final int LINK_VALUE = 0;
        public static final int REDIRECT_VALUE = 5;
        public static final int SEARCHWINDOW_VALUE = 3;
        private static EnumLiteMap<OriginType> internalValueMap;
        private final int value;

        static {
            internalValueMap = new EnumLiteMap<OriginType>() {
                public OriginType findValueByNumber(int i) {
                    return OriginType.valueOf(i);
                }
            };
        }

        public final int getNumber() {
            return this.value;
        }

        public static OriginType valueOf(int i) {
            switch (i) {
                case 0:
                    return LINK;
                case 1:
                    return ADDRESSBAR;
                case 2:
                    return BOOKMARK;
                case 3:
                    return SEARCHWINDOW;
                case 4:
                    return JAVASCRIPT;
                case 5:
                    return REDIRECT;
                case 6:
                    return HOMEPAGE;
                default:
                    return null;
            }
        }

        public static EnumLiteMap<OriginType> internalGetValueMap() {
            return internalValueMap;
        }

        private OriginType(int i, int i2) {
            this.value = i2;
        }
    }

    public interface PhishingOrBuilder extends MessageLiteOrBuilder {
        int getPhishing();

        int getPhishingDomain();

        int getTtl();

        boolean hasPhishing();

        boolean hasPhishingDomain();

        boolean hasTtl();
    }

    public static final class Phishing extends GeneratedMessageLite implements PhishingOrBuilder {
        public static Parser<Phishing> PARSER = new AbstractParser<Phishing>() {
            public Phishing parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new Phishing(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int PHISHINGDOMAIN_FIELD_NUMBER = 2;
        public static final int PHISHING_FIELD_NUMBER = 1;
        public static final int TTL_FIELD_NUMBER = 3;
        private static final Phishing defaultInstance = new Phishing(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private int phishingDomain_;
        private int phishing_;
        private int ttl_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<Phishing, Builder> implements PhishingOrBuilder {
            private int bitField0_;
            private int phishingDomain_;
            private int phishing_;
            private int ttl_;

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.phishing_ = 0;
                this.bitField0_ &= -2;
                this.phishingDomain_ = 0;
                this.bitField0_ &= -3;
                this.ttl_ = 0;
                this.bitField0_ &= -5;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public Phishing getDefaultInstanceForType() {
                return Phishing.getDefaultInstance();
            }

            public Phishing build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public Phishing buildPartial() {
                Phishing phishing = new Phishing((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                phishing.phishing_ = this.phishing_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                phishing.phishingDomain_ = this.phishingDomain_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                phishing.ttl_ = this.ttl_;
                phishing.bitField0_ = i2;
                return phishing;
            }

            public Builder mergeFrom(Phishing phishing) {
                if (phishing == Phishing.getDefaultInstance()) {
                    return this;
                }
                if (phishing.hasPhishing()) {
                    setPhishing(phishing.getPhishing());
                }
                if (phishing.hasPhishingDomain()) {
                    setPhishingDomain(phishing.getPhishingDomain());
                }
                if (phishing.hasTtl()) {
                    setTtl(phishing.getTtl());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Phishing phishing;
                Throwable th;
                Phishing phishing2;
                try {
                    phishing = (Phishing) Phishing.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (phishing != null) {
                        mergeFrom(phishing);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    phishing = (Phishing) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    phishing2 = phishing;
                    th = th3;
                }
                if (phishing2 != null) {
                    mergeFrom(phishing2);
                }
                throw th;
            }

            public boolean hasPhishing() {
                return (this.bitField0_ & 1) == 1;
            }

            public int getPhishing() {
                return this.phishing_;
            }

            public Builder setPhishing(int i) {
                this.bitField0_ |= 1;
                this.phishing_ = i;
                return this;
            }

            public Builder clearPhishing() {
                this.bitField0_ &= -2;
                this.phishing_ = 0;
                return this;
            }

            public boolean hasPhishingDomain() {
                return (this.bitField0_ & 2) == 2;
            }

            public int getPhishingDomain() {
                return this.phishingDomain_;
            }

            public Builder setPhishingDomain(int i) {
                this.bitField0_ |= 2;
                this.phishingDomain_ = i;
                return this;
            }

            public Builder clearPhishingDomain() {
                this.bitField0_ &= -3;
                this.phishingDomain_ = 0;
                return this;
            }

            public boolean hasTtl() {
                return (this.bitField0_ & 4) == 4;
            }

            public int getTtl() {
                return this.ttl_;
            }

            public Builder setTtl(int i) {
                this.bitField0_ |= 4;
                this.ttl_ = i;
                return this;
            }

            public Builder clearTtl() {
                this.bitField0_ &= -5;
                this.ttl_ = 0;
                return this;
            }
        }

        private Phishing(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private Phishing(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static Phishing getDefaultInstance() {
            return defaultInstance;
        }

        public Phishing getDefaultInstanceForType() {
            return defaultInstance;
        }

        private Phishing(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 8:
                            this.bitField0_ |= 1;
                            this.phishing_ = codedInputStream.readSInt32();
                            break;
                        case 16:
                            this.bitField0_ |= 2;
                            this.phishingDomain_ = codedInputStream.readSInt32();
                            break;
                        case 24:
                            this.bitField0_ |= 4;
                            this.ttl_ = codedInputStream.readSInt32();
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                obj = 1;
                                break;
                            }
                            break;
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<Phishing> getParserForType() {
            return PARSER;
        }

        public boolean hasPhishing() {
            return (this.bitField0_ & 1) == 1;
        }

        public int getPhishing() {
            return this.phishing_;
        }

        public boolean hasPhishingDomain() {
            return (this.bitField0_ & 2) == 2;
        }

        public int getPhishingDomain() {
            return this.phishingDomain_;
        }

        public boolean hasTtl() {
            return (this.bitField0_ & 4) == 4;
        }

        public int getTtl() {
            return this.ttl_;
        }

        private void initFields() {
            this.phishing_ = 0;
            this.phishingDomain_ = 0;
            this.ttl_ = 0;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeSInt32(1, this.phishing_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeSInt32(2, this.phishingDomain_);
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeSInt32(3, this.ttl_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeSInt32Size(1, this.phishing_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeSInt32Size(2, this.phishingDomain_);
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeSInt32Size(3, this.ttl_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static Phishing parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (Phishing) PARSER.parseFrom(byteString);
        }

        public static Phishing parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Phishing) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static Phishing parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (Phishing) PARSER.parseFrom(bArr);
        }

        public static Phishing parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Phishing) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static Phishing parseFrom(InputStream inputStream) throws IOException {
            return (Phishing) PARSER.parseFrom(inputStream);
        }

        public static Phishing parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Phishing) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static Phishing parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (Phishing) PARSER.parseDelimitedFrom(inputStream);
        }

        public static Phishing parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Phishing) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static Phishing parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (Phishing) PARSER.parseFrom(codedInputStream);
        }

        public static Phishing parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Phishing) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(Phishing phishing) {
            return newBuilder().mergeFrom(phishing);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface SafeShopOrBuilder extends MessageLiteOrBuilder {
        String getRegex();

        ByteString getRegexBytes();

        String getSelector();

        ByteString getSelectorBytes();

        long getTimestamp();

        boolean hasRegex();

        boolean hasSelector();

        boolean hasTimestamp();
    }

    public static final class SafeShop extends GeneratedMessageLite implements SafeShopOrBuilder {
        public static Parser<SafeShop> PARSER = new AbstractParser<SafeShop>() {
            public SafeShop parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new SafeShop(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int REGEX_FIELD_NUMBER = 2;
        public static final int SELECTOR_FIELD_NUMBER = 3;
        public static final int TIMESTAMP_FIELD_NUMBER = 1;
        private static final SafeShop defaultInstance = new SafeShop(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object regex_;
        private Object selector_;
        private long timestamp_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<SafeShop, Builder> implements SafeShopOrBuilder {
            private int bitField0_;
            private Object regex_ = "";
            private Object selector_ = "";
            private long timestamp_;

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.timestamp_ = 0;
                this.bitField0_ &= -2;
                this.regex_ = "";
                this.bitField0_ &= -3;
                this.selector_ = "";
                this.bitField0_ &= -5;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public SafeShop getDefaultInstanceForType() {
                return SafeShop.getDefaultInstance();
            }

            public SafeShop build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public SafeShop buildPartial() {
                SafeShop safeShop = new SafeShop((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                safeShop.timestamp_ = this.timestamp_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                safeShop.regex_ = this.regex_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                safeShop.selector_ = this.selector_;
                safeShop.bitField0_ = i2;
                return safeShop;
            }

            public Builder mergeFrom(SafeShop safeShop) {
                if (safeShop == SafeShop.getDefaultInstance()) {
                    return this;
                }
                if (safeShop.hasTimestamp()) {
                    setTimestamp(safeShop.getTimestamp());
                }
                if (safeShop.hasRegex()) {
                    this.bitField0_ |= 2;
                    this.regex_ = safeShop.regex_;
                }
                if (safeShop.hasSelector()) {
                    this.bitField0_ |= 4;
                    this.selector_ = safeShop.selector_;
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                SafeShop safeShop;
                SafeShop safeShop2;
                try {
                    safeShop2 = (SafeShop) SafeShop.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (safeShop2 != null) {
                        mergeFrom(safeShop2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    safeShop2 = (SafeShop) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    safeShop = safeShop2;
                    th = th3;
                }
                if (safeShop != null) {
                    mergeFrom(safeShop);
                }
                throw th;
            }

            public boolean hasTimestamp() {
                return (this.bitField0_ & 1) == 1;
            }

            public long getTimestamp() {
                return this.timestamp_;
            }

            public Builder setTimestamp(long j) {
                this.bitField0_ |= 1;
                this.timestamp_ = j;
                return this;
            }

            public Builder clearTimestamp() {
                this.bitField0_ &= -2;
                this.timestamp_ = 0;
                return this;
            }

            public boolean hasRegex() {
                return (this.bitField0_ & 2) == 2;
            }

            public String getRegex() {
                Object obj = this.regex_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.regex_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getRegexBytes() {
                Object obj = this.regex_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.regex_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setRegex(String str) {
                if (str != null) {
                    this.bitField0_ |= 2;
                    this.regex_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearRegex() {
                this.bitField0_ &= -3;
                this.regex_ = SafeShop.getDefaultInstance().getRegex();
                return this;
            }

            public Builder setRegexBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.regex_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasSelector() {
                return (this.bitField0_ & 4) == 4;
            }

            public String getSelector() {
                Object obj = this.selector_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.selector_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getSelectorBytes() {
                Object obj = this.selector_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.selector_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setSelector(String str) {
                if (str != null) {
                    this.bitField0_ |= 4;
                    this.selector_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearSelector() {
                this.bitField0_ &= -5;
                this.selector_ = SafeShop.getDefaultInstance().getSelector();
                return this;
            }

            public Builder setSelectorBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 4;
                    this.selector_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }
        }

        private SafeShop(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private SafeShop(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static SafeShop getDefaultInstance() {
            return defaultInstance;
        }

        public SafeShop getDefaultInstanceForType() {
            return defaultInstance;
        }

        private SafeShop(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 8:
                            this.bitField0_ |= 1;
                            this.timestamp_ = codedInputStream.readInt64();
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.regex_ = codedInputStream.readBytes();
                            break;
                        case 26:
                            this.bitField0_ |= 4;
                            this.selector_ = codedInputStream.readBytes();
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                obj = 1;
                                break;
                            }
                            break;
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<SafeShop> getParserForType() {
            return PARSER;
        }

        public boolean hasTimestamp() {
            return (this.bitField0_ & 1) == 1;
        }

        public long getTimestamp() {
            return this.timestamp_;
        }

        public boolean hasRegex() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getRegex() {
            Object obj = this.regex_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.regex_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getRegexBytes() {
            Object obj = this.regex_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.regex_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasSelector() {
            return (this.bitField0_ & 4) == 4;
        }

        public String getSelector() {
            Object obj = this.selector_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.selector_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getSelectorBytes() {
            Object obj = this.selector_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.selector_ = copyFromUtf8;
            return copyFromUtf8;
        }

        private void initFields() {
            this.timestamp_ = 0;
            this.regex_ = "";
            this.selector_ = "";
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeInt64(1, this.timestamp_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, getRegexBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeBytes(3, getSelectorBytes());
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeInt64Size(1, this.timestamp_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, getRegexBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeBytesSize(3, getSelectorBytes());
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static SafeShop parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (SafeShop) PARSER.parseFrom(byteString);
        }

        public static SafeShop parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (SafeShop) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static SafeShop parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (SafeShop) PARSER.parseFrom(bArr);
        }

        public static SafeShop parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (SafeShop) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static SafeShop parseFrom(InputStream inputStream) throws IOException {
            return (SafeShop) PARSER.parseFrom(inputStream);
        }

        public static SafeShop parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (SafeShop) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static SafeShop parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (SafeShop) PARSER.parseDelimitedFrom(inputStream);
        }

        public static SafeShop parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (SafeShop) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static SafeShop parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (SafeShop) PARSER.parseFrom(codedInputStream);
        }

        public static SafeShop parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (SafeShop) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(SafeShop safeShop) {
            return newBuilder().mergeFrom(safeShop);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface TypoOrBuilder extends MessageLiteOrBuilder {
        String getBrandDomain();

        ByteString getBrandDomainBytes();

        boolean getIsTypo();

        UrlInfo getUrlInfo();

        String getUrlTo();

        ByteString getUrlToBytes();

        boolean hasBrandDomain();

        boolean hasIsTypo();

        boolean hasUrlInfo();

        boolean hasUrlTo();
    }

    public static final class Typo extends GeneratedMessageLite implements TypoOrBuilder {
        public static final int BRAND_DOMAIN_FIELD_NUMBER = 2;
        public static final int IS_TYPO_FIELD_NUMBER = 4;
        public static Parser<Typo> PARSER = new AbstractParser<Typo>() {
            public Typo parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new Typo(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int URLINFO_FIELD_NUMBER = 3;
        public static final int URL_TO_FIELD_NUMBER = 1;
        private static final Typo defaultInstance = new Typo(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private Object brandDomain_;
        private boolean isTypo_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private UrlInfo urlInfo_;
        private Object urlTo_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<Typo, Builder> implements TypoOrBuilder {
            private int bitField0_;
            private Object brandDomain_ = "";
            private boolean isTypo_;
            private UrlInfo urlInfo_ = UrlInfo.getDefaultInstance();
            private Object urlTo_ = "";

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.urlTo_ = "";
                this.bitField0_ &= -2;
                this.brandDomain_ = "";
                this.bitField0_ &= -3;
                this.urlInfo_ = UrlInfo.getDefaultInstance();
                this.bitField0_ &= -5;
                this.isTypo_ = false;
                this.bitField0_ &= -9;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public Typo getDefaultInstanceForType() {
                return Typo.getDefaultInstance();
            }

            public Typo build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public Typo buildPartial() {
                Typo typo = new Typo((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                typo.urlTo_ = this.urlTo_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                typo.brandDomain_ = this.brandDomain_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                typo.urlInfo_ = this.urlInfo_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                typo.isTypo_ = this.isTypo_;
                typo.bitField0_ = i2;
                return typo;
            }

            public Builder mergeFrom(Typo typo) {
                if (typo == Typo.getDefaultInstance()) {
                    return this;
                }
                if (typo.hasUrlTo()) {
                    this.bitField0_ |= 1;
                    this.urlTo_ = typo.urlTo_;
                }
                if (typo.hasBrandDomain()) {
                    this.bitField0_ |= 2;
                    this.brandDomain_ = typo.brandDomain_;
                }
                if (typo.hasUrlInfo()) {
                    mergeUrlInfo(typo.getUrlInfo());
                }
                if (typo.hasIsTypo()) {
                    setIsTypo(typo.getIsTypo());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                Typo typo;
                Typo typo2;
                try {
                    typo2 = (Typo) Typo.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (typo2 != null) {
                        mergeFrom(typo2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    typo2 = (Typo) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    typo = typo2;
                    th = th3;
                }
                if (typo != null) {
                    mergeFrom(typo);
                }
                throw th;
            }

            public boolean hasUrlTo() {
                return (this.bitField0_ & 1) == 1;
            }

            public String getUrlTo() {
                Object obj = this.urlTo_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.urlTo_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getUrlToBytes() {
                Object obj = this.urlTo_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.urlTo_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setUrlTo(String str) {
                if (str != null) {
                    this.bitField0_ |= 1;
                    this.urlTo_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearUrlTo() {
                this.bitField0_ &= -2;
                this.urlTo_ = Typo.getDefaultInstance().getUrlTo();
                return this;
            }

            public Builder setUrlToBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.urlTo_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasBrandDomain() {
                return (this.bitField0_ & 2) == 2;
            }

            public String getBrandDomain() {
                Object obj = this.brandDomain_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.brandDomain_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getBrandDomainBytes() {
                Object obj = this.brandDomain_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.brandDomain_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setBrandDomain(String str) {
                if (str != null) {
                    this.bitField0_ |= 2;
                    this.brandDomain_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearBrandDomain() {
                this.bitField0_ &= -3;
                this.brandDomain_ = Typo.getDefaultInstance().getBrandDomain();
                return this;
            }

            public Builder setBrandDomainBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.brandDomain_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasUrlInfo() {
                return (this.bitField0_ & 4) == 4;
            }

            public UrlInfo getUrlInfo() {
                return this.urlInfo_;
            }

            public Builder setUrlInfo(UrlInfo urlInfo) {
                if (urlInfo != null) {
                    this.urlInfo_ = urlInfo;
                    this.bitField0_ |= 4;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setUrlInfo(Builder builder) {
                this.urlInfo_ = builder.build();
                this.bitField0_ |= 4;
                return this;
            }

            public Builder mergeUrlInfo(UrlInfo urlInfo) {
                if ((this.bitField0_ & 4) == 4 && this.urlInfo_ != UrlInfo.getDefaultInstance()) {
                    this.urlInfo_ = UrlInfo.newBuilder(this.urlInfo_).mergeFrom(urlInfo).buildPartial();
                } else {
                    this.urlInfo_ = urlInfo;
                }
                this.bitField0_ |= 4;
                return this;
            }

            public Builder clearUrlInfo() {
                this.urlInfo_ = UrlInfo.getDefaultInstance();
                this.bitField0_ &= -5;
                return this;
            }

            public boolean hasIsTypo() {
                return (this.bitField0_ & 8) == 8;
            }

            public boolean getIsTypo() {
                return this.isTypo_;
            }

            public Builder setIsTypo(boolean z) {
                this.bitField0_ |= 8;
                this.isTypo_ = z;
                return this;
            }

            public Builder clearIsTypo() {
                this.bitField0_ &= -9;
                this.isTypo_ = false;
                return this;
            }
        }

        private Typo(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private Typo(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static Typo getDefaultInstance() {
            return defaultInstance;
        }

        public Typo getDefaultInstanceForType() {
            return defaultInstance;
        }

        private Typo(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            Object obj = null;
            while (obj == null) {
                try {
                    Object obj2;
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            readTag = 1;
                            break;
                        case 10:
                            this.bitField0_ |= 1;
                            this.urlTo_ = codedInputStream.readBytes();
                            obj2 = obj;
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.brandDomain_ = codedInputStream.readBytes();
                            obj2 = obj;
                            break;
                        case 26:
                            Builder builder;
                            if ((this.bitField0_ & 4) != 4) {
                                builder = null;
                            } else {
                                builder = this.urlInfo_.toBuilder();
                            }
                            this.urlInfo_ = (UrlInfo) codedInputStream.readMessage(UrlInfo.PARSER, extensionRegistryLite);
                            if (builder != null) {
                                builder.mergeFrom(this.urlInfo_);
                                this.urlInfo_ = builder.buildPartial();
                            }
                            this.bitField0_ |= 4;
                            obj2 = obj;
                            break;
                        case 32:
                            this.bitField0_ |= 8;
                            this.isTypo_ = codedInputStream.readBool();
                            obj2 = obj;
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                readTag = 1;
                                break;
                            } else {
                                obj2 = obj;
                                break;
                            }
                    }
                    obj = obj2;
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<Typo> getParserForType() {
            return PARSER;
        }

        public boolean hasUrlTo() {
            return (this.bitField0_ & 1) == 1;
        }

        public String getUrlTo() {
            Object obj = this.urlTo_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.urlTo_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getUrlToBytes() {
            Object obj = this.urlTo_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.urlTo_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasBrandDomain() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getBrandDomain() {
            Object obj = this.brandDomain_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.brandDomain_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getBrandDomainBytes() {
            Object obj = this.brandDomain_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.brandDomain_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasUrlInfo() {
            return (this.bitField0_ & 4) == 4;
        }

        public UrlInfo getUrlInfo() {
            return this.urlInfo_;
        }

        public boolean hasIsTypo() {
            return (this.bitField0_ & 8) == 8;
        }

        public boolean getIsTypo() {
            return this.isTypo_;
        }

        private void initFields() {
            this.urlTo_ = "";
            this.brandDomain_ = "";
            this.urlInfo_ = UrlInfo.getDefaultInstance();
            this.isTypo_ = false;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeBytes(1, getUrlToBytes());
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, getBrandDomainBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeMessage(3, this.urlInfo_);
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeBool(4, this.isTypo_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, getUrlToBytes()) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, getBrandDomainBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeMessageSize(3, this.urlInfo_);
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeBoolSize(4, this.isTypo_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static Typo parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (Typo) PARSER.parseFrom(byteString);
        }

        public static Typo parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Typo) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static Typo parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (Typo) PARSER.parseFrom(bArr);
        }

        public static Typo parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Typo) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static Typo parseFrom(InputStream inputStream) throws IOException {
            return (Typo) PARSER.parseFrom(inputStream);
        }

        public static Typo parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Typo) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static Typo parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (Typo) PARSER.parseDelimitedFrom(inputStream);
        }

        public static Typo parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Typo) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static Typo parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (Typo) PARSER.parseFrom(codedInputStream);
        }

        public static Typo parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Typo) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(Typo typo) {
            return newBuilder().mergeFrom(typo);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface UrlInfoOrBuilder extends MessageLiteOrBuilder {
        Blocker getBlocker();

        Phishing getPhishing();

        SafeShop getSafeshop();

        Typo getTypo();

        Webrep getWebrep();

        boolean hasBlocker();

        boolean hasPhishing();

        boolean hasSafeshop();

        boolean hasTypo();

        boolean hasWebrep();
    }

    public static final class UrlInfo extends GeneratedMessageLite implements UrlInfoOrBuilder {
        public static final int BLOCKER_FIELD_NUMBER = 3;
        public static Parser<UrlInfo> PARSER = new AbstractParser<UrlInfo>() {
            public UrlInfo parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new UrlInfo(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int PHISHING_FIELD_NUMBER = 2;
        public static final int SAFESHOP_FIELD_NUMBER = 5;
        public static final int TYPO_FIELD_NUMBER = 4;
        public static final int WEBREP_FIELD_NUMBER = 1;
        private static final UrlInfo defaultInstance = new UrlInfo(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private Blocker blocker_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Phishing phishing_;
        private SafeShop safeshop_;
        private Typo typo_;
        private Webrep webrep_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<UrlInfo, Builder> implements UrlInfoOrBuilder {
            private int bitField0_;
            private Blocker blocker_ = Blocker.getDefaultInstance();
            private Phishing phishing_ = Phishing.getDefaultInstance();
            private SafeShop safeshop_ = SafeShop.getDefaultInstance();
            private Typo typo_ = Typo.getDefaultInstance();
            private Webrep webrep_ = Webrep.getDefaultInstance();

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.webrep_ = Webrep.getDefaultInstance();
                this.bitField0_ &= -2;
                this.phishing_ = Phishing.getDefaultInstance();
                this.bitField0_ &= -3;
                this.blocker_ = Blocker.getDefaultInstance();
                this.bitField0_ &= -5;
                this.typo_ = Typo.getDefaultInstance();
                this.bitField0_ &= -9;
                this.safeshop_ = SafeShop.getDefaultInstance();
                this.bitField0_ &= -17;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public UrlInfo getDefaultInstanceForType() {
                return UrlInfo.getDefaultInstance();
            }

            public UrlInfo build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public UrlInfo buildPartial() {
                UrlInfo urlInfo = new UrlInfo((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                urlInfo.webrep_ = this.webrep_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                urlInfo.phishing_ = this.phishing_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                urlInfo.blocker_ = this.blocker_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                urlInfo.typo_ = this.typo_;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                urlInfo.safeshop_ = this.safeshop_;
                urlInfo.bitField0_ = i2;
                return urlInfo;
            }

            public Builder mergeFrom(UrlInfo urlInfo) {
                if (urlInfo == UrlInfo.getDefaultInstance()) {
                    return this;
                }
                if (urlInfo.hasWebrep()) {
                    mergeWebrep(urlInfo.getWebrep());
                }
                if (urlInfo.hasPhishing()) {
                    mergePhishing(urlInfo.getPhishing());
                }
                if (urlInfo.hasBlocker()) {
                    mergeBlocker(urlInfo.getBlocker());
                }
                if (urlInfo.hasTypo()) {
                    mergeTypo(urlInfo.getTypo());
                }
                if (urlInfo.hasSafeshop()) {
                    mergeSafeshop(urlInfo.getSafeshop());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                UrlInfo urlInfo;
                UrlInfo urlInfo2;
                try {
                    urlInfo2 = (UrlInfo) UrlInfo.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (urlInfo2 != null) {
                        mergeFrom(urlInfo2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    urlInfo2 = (UrlInfo) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    urlInfo = urlInfo2;
                    th = th3;
                }
                if (urlInfo != null) {
                    mergeFrom(urlInfo);
                }
                throw th;
            }

            public boolean hasWebrep() {
                return (this.bitField0_ & 1) == 1;
            }

            public Webrep getWebrep() {
                return this.webrep_;
            }

            public Builder setWebrep(Webrep webrep) {
                if (webrep != null) {
                    this.webrep_ = webrep;
                    this.bitField0_ |= 1;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setWebrep(Builder builder) {
                this.webrep_ = builder.build();
                this.bitField0_ |= 1;
                return this;
            }

            public Builder mergeWebrep(Webrep webrep) {
                if ((this.bitField0_ & 1) == 1 && this.webrep_ != Webrep.getDefaultInstance()) {
                    this.webrep_ = Webrep.newBuilder(this.webrep_).mergeFrom(webrep).buildPartial();
                } else {
                    this.webrep_ = webrep;
                }
                this.bitField0_ |= 1;
                return this;
            }

            public Builder clearWebrep() {
                this.webrep_ = Webrep.getDefaultInstance();
                this.bitField0_ &= -2;
                return this;
            }

            public boolean hasPhishing() {
                return (this.bitField0_ & 2) == 2;
            }

            public Phishing getPhishing() {
                return this.phishing_;
            }

            public Builder setPhishing(Phishing phishing) {
                if (phishing != null) {
                    this.phishing_ = phishing;
                    this.bitField0_ |= 2;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setPhishing(Builder builder) {
                this.phishing_ = builder.build();
                this.bitField0_ |= 2;
                return this;
            }

            public Builder mergePhishing(Phishing phishing) {
                if ((this.bitField0_ & 2) == 2 && this.phishing_ != Phishing.getDefaultInstance()) {
                    this.phishing_ = Phishing.newBuilder(this.phishing_).mergeFrom(phishing).buildPartial();
                } else {
                    this.phishing_ = phishing;
                }
                this.bitField0_ |= 2;
                return this;
            }

            public Builder clearPhishing() {
                this.phishing_ = Phishing.getDefaultInstance();
                this.bitField0_ &= -3;
                return this;
            }

            public boolean hasBlocker() {
                return (this.bitField0_ & 4) == 4;
            }

            public Blocker getBlocker() {
                return this.blocker_;
            }

            public Builder setBlocker(Blocker blocker) {
                if (blocker != null) {
                    this.blocker_ = blocker;
                    this.bitField0_ |= 4;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setBlocker(Builder builder) {
                this.blocker_ = builder.build();
                this.bitField0_ |= 4;
                return this;
            }

            public Builder mergeBlocker(Blocker blocker) {
                if ((this.bitField0_ & 4) == 4 && this.blocker_ != Blocker.getDefaultInstance()) {
                    this.blocker_ = Blocker.newBuilder(this.blocker_).mergeFrom(blocker).buildPartial();
                } else {
                    this.blocker_ = blocker;
                }
                this.bitField0_ |= 4;
                return this;
            }

            public Builder clearBlocker() {
                this.blocker_ = Blocker.getDefaultInstance();
                this.bitField0_ &= -5;
                return this;
            }

            public boolean hasTypo() {
                return (this.bitField0_ & 8) == 8;
            }

            public Typo getTypo() {
                return this.typo_;
            }

            public Builder setTypo(Typo typo) {
                if (typo != null) {
                    this.typo_ = typo;
                    this.bitField0_ |= 8;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setTypo(Builder builder) {
                this.typo_ = builder.build();
                this.bitField0_ |= 8;
                return this;
            }

            public Builder mergeTypo(Typo typo) {
                if ((this.bitField0_ & 8) == 8 && this.typo_ != Typo.getDefaultInstance()) {
                    this.typo_ = Typo.newBuilder(this.typo_).mergeFrom(typo).buildPartial();
                } else {
                    this.typo_ = typo;
                }
                this.bitField0_ |= 8;
                return this;
            }

            public Builder clearTypo() {
                this.typo_ = Typo.getDefaultInstance();
                this.bitField0_ &= -9;
                return this;
            }

            public boolean hasSafeshop() {
                return (this.bitField0_ & 16) == 16;
            }

            public SafeShop getSafeshop() {
                return this.safeshop_;
            }

            public Builder setSafeshop(SafeShop safeShop) {
                if (safeShop != null) {
                    this.safeshop_ = safeShop;
                    this.bitField0_ |= 16;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setSafeshop(Builder builder) {
                this.safeshop_ = builder.build();
                this.bitField0_ |= 16;
                return this;
            }

            public Builder mergeSafeshop(SafeShop safeShop) {
                if ((this.bitField0_ & 16) == 16 && this.safeshop_ != SafeShop.getDefaultInstance()) {
                    this.safeshop_ = SafeShop.newBuilder(this.safeshop_).mergeFrom(safeShop).buildPartial();
                } else {
                    this.safeshop_ = safeShop;
                }
                this.bitField0_ |= 16;
                return this;
            }

            public Builder clearSafeshop() {
                this.safeshop_ = SafeShop.getDefaultInstance();
                this.bitField0_ &= -17;
                return this;
            }
        }

        private UrlInfo(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private UrlInfo(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static UrlInfo getDefaultInstance() {
            return defaultInstance;
        }

        public UrlInfo getDefaultInstanceForType() {
            return defaultInstance;
        }

        private UrlInfo(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            Object obj = null;
            while (obj == null) {
                try {
                    Object obj2;
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            readTag = 1;
                            break;
                        case 10:
                            Builder builder;
                            if ((this.bitField0_ & 1) != 1) {
                                builder = null;
                            } else {
                                builder = this.webrep_.toBuilder();
                            }
                            this.webrep_ = (Webrep) codedInputStream.readMessage(Webrep.PARSER, extensionRegistryLite);
                            if (builder != null) {
                                builder.mergeFrom(this.webrep_);
                                this.webrep_ = builder.buildPartial();
                            }
                            this.bitField0_ |= 1;
                            obj2 = obj;
                            break;
                        case 18:
                            Builder builder2;
                            if ((this.bitField0_ & 2) != 2) {
                                builder2 = null;
                            } else {
                                builder2 = this.phishing_.toBuilder();
                            }
                            this.phishing_ = (Phishing) codedInputStream.readMessage(Phishing.PARSER, extensionRegistryLite);
                            if (builder2 != null) {
                                builder2.mergeFrom(this.phishing_);
                                this.phishing_ = builder2.buildPartial();
                            }
                            this.bitField0_ |= 2;
                            obj2 = obj;
                            break;
                        case 26:
                            Builder builder3;
                            if ((this.bitField0_ & 4) != 4) {
                                builder3 = null;
                            } else {
                                builder3 = this.blocker_.toBuilder();
                            }
                            this.blocker_ = (Blocker) codedInputStream.readMessage(Blocker.PARSER, extensionRegistryLite);
                            if (builder3 != null) {
                                builder3.mergeFrom(this.blocker_);
                                this.blocker_ = builder3.buildPartial();
                            }
                            this.bitField0_ |= 4;
                            obj2 = obj;
                            break;
                        case 34:
                            Builder builder4;
                            if ((this.bitField0_ & 8) != 8) {
                                builder4 = null;
                            } else {
                                builder4 = this.typo_.toBuilder();
                            }
                            this.typo_ = (Typo) codedInputStream.readMessage(Typo.PARSER, extensionRegistryLite);
                            if (builder4 != null) {
                                builder4.mergeFrom(this.typo_);
                                this.typo_ = builder4.buildPartial();
                            }
                            this.bitField0_ |= 8;
                            obj2 = obj;
                            break;
                        case MotionEventCompat.AXIS_GENERIC_11 /*42*/:
                            Builder builder5;
                            if ((this.bitField0_ & 16) != 16) {
                                builder5 = null;
                            } else {
                                builder5 = this.safeshop_.toBuilder();
                            }
                            this.safeshop_ = (SafeShop) codedInputStream.readMessage(SafeShop.PARSER, extensionRegistryLite);
                            if (builder5 != null) {
                                builder5.mergeFrom(this.safeshop_);
                                this.safeshop_ = builder5.buildPartial();
                            }
                            this.bitField0_ |= 16;
                            obj2 = obj;
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                readTag = 1;
                                break;
                            } else {
                                obj2 = obj;
                                break;
                            }
                    }
                    obj = obj2;
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<UrlInfo> getParserForType() {
            return PARSER;
        }

        public boolean hasWebrep() {
            return (this.bitField0_ & 1) == 1;
        }

        public Webrep getWebrep() {
            return this.webrep_;
        }

        public boolean hasPhishing() {
            return (this.bitField0_ & 2) == 2;
        }

        public Phishing getPhishing() {
            return this.phishing_;
        }

        public boolean hasBlocker() {
            return (this.bitField0_ & 4) == 4;
        }

        public Blocker getBlocker() {
            return this.blocker_;
        }

        public boolean hasTypo() {
            return (this.bitField0_ & 8) == 8;
        }

        public Typo getTypo() {
            return this.typo_;
        }

        public boolean hasSafeshop() {
            return (this.bitField0_ & 16) == 16;
        }

        public SafeShop getSafeshop() {
            return this.safeshop_;
        }

        private void initFields() {
            this.webrep_ = Webrep.getDefaultInstance();
            this.phishing_ = Phishing.getDefaultInstance();
            this.blocker_ = Blocker.getDefaultInstance();
            this.typo_ = Typo.getDefaultInstance();
            this.safeshop_ = SafeShop.getDefaultInstance();
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeMessage(1, this.webrep_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeMessage(2, this.phishing_);
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeMessage(3, this.blocker_);
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeMessage(4, this.typo_);
            }
            if ((this.bitField0_ & 16) == 16) {
                codedOutputStream.writeMessage(5, this.safeshop_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.webrep_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeMessageSize(2, this.phishing_);
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeMessageSize(3, this.blocker_);
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeMessageSize(4, this.typo_);
            }
            if ((this.bitField0_ & 16) == 16) {
                i += CodedOutputStream.computeMessageSize(5, this.safeshop_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static UrlInfo parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (UrlInfo) PARSER.parseFrom(byteString);
        }

        public static UrlInfo parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UrlInfo) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static UrlInfo parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (UrlInfo) PARSER.parseFrom(bArr);
        }

        public static UrlInfo parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UrlInfo) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static UrlInfo parseFrom(InputStream inputStream) throws IOException {
            return (UrlInfo) PARSER.parseFrom(inputStream);
        }

        public static UrlInfo parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlInfo) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static UrlInfo parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (UrlInfo) PARSER.parseDelimitedFrom(inputStream);
        }

        public static UrlInfo parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlInfo) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static UrlInfo parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (UrlInfo) PARSER.parseFrom(codedInputStream);
        }

        public static UrlInfo parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlInfo) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(UrlInfo urlInfo) {
            return newBuilder().mergeFrom(urlInfo);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface UrlInfoRequestOrBuilder extends MessageLiteOrBuilder {
        ByteString getApikey();

        long getCallerId();

        Client getClient();

        KeyValue getCustomKeyValue(int i);

        int getCustomKeyValueCount();

        List<KeyValue> getCustomKeyValueList();

        boolean getDnl();

        Identity getIdentity();

        String getLocale();

        ByteString getLocaleBytes();

        OriginType getOrigin();

        String getReferer();

        ByteString getRefererBytes();

        int getRequestedServices();

        ByteString getReserved();

        long getSafeShop();

        int getTabNum();

        UpdateRequest getUpdateRequest();

        String getUri(int i);

        ByteString getUriBytes(int i);

        int getUriCount();

        List<String> getUriList();

        boolean getVisited();

        EventType getWindowEvent();

        int getWindowNum();

        boolean hasApikey();

        boolean hasCallerId();

        boolean hasClient();

        boolean hasDnl();

        boolean hasIdentity();

        boolean hasLocale();

        boolean hasOrigin();

        boolean hasReferer();

        boolean hasRequestedServices();

        boolean hasReserved();

        boolean hasSafeShop();

        boolean hasTabNum();

        boolean hasUpdateRequest();

        boolean hasVisited();

        boolean hasWindowEvent();

        boolean hasWindowNum();
    }

    public static final class UrlInfoRequest extends GeneratedMessageLite implements UrlInfoRequestOrBuilder {
        public static final int APIKEY_FIELD_NUMBER = 4;
        public static final int CALLERID_FIELD_NUMBER = 2;
        public static final int CLIENT_FIELD_NUMBER = 18;
        public static final int CUSTOMKEYVALUE_FIELD_NUMBER = 9;
        public static final int DNL_FIELD_NUMBER = 15;
        public static final int IDENTITY_FIELD_NUMBER = 5;
        public static final int LOCALE_FIELD_NUMBER = 3;
        public static final int ORIGIN_FIELD_NUMBER = 14;
        public static Parser<UrlInfoRequest> PARSER = new AbstractParser<UrlInfoRequest>() {
            public UrlInfoRequest parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new UrlInfoRequest(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int REFERER_FIELD_NUMBER = 10;
        public static final int REQUESTEDSERVICES_FIELD_NUMBER = 8;
        public static final int RESERVED_FIELD_NUMBER = 16;
        public static final int SAFESHOP_FIELD_NUMBER = 17;
        public static final int TABNUM_FIELD_NUMBER = 12;
        public static final int UPDATEREQUEST_FIELD_NUMBER = 7;
        public static final int URI_FIELD_NUMBER = 1;
        public static final int VISITED_FIELD_NUMBER = 6;
        public static final int WINDOWEVENT_FIELD_NUMBER = 13;
        public static final int WINDOWNUM_FIELD_NUMBER = 11;
        private static final UrlInfoRequest defaultInstance = new UrlInfoRequest(true);
        private static final long serialVersionUID = 0;
        private ByteString apikey_;
        private int bitField0_;
        private long callerId_;
        private Client client_;
        private List<KeyValue> customKeyValue_;
        private boolean dnl_;
        private Identity identity_;
        private Object locale_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private OriginType origin_;
        private Object referer_;
        private int requestedServices_;
        private ByteString reserved_;
        private long safeShop_;
        private int tabNum_;
        private UpdateRequest updateRequest_;
        private LazyStringList uri_;
        private boolean visited_;
        private EventType windowEvent_;
        private int windowNum_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<UrlInfoRequest, Builder> implements UrlInfoRequestOrBuilder {
            private ByteString apikey_ = ByteString.EMPTY;
            private int bitField0_;
            private long callerId_;
            private Client client_ = Client.getDefaultInstance();
            private List<KeyValue> customKeyValue_ = Collections.emptyList();
            private boolean dnl_;
            private Identity identity_ = Identity.getDefaultInstance();
            private Object locale_ = "";
            private OriginType origin_ = OriginType.LINK;
            private Object referer_ = "";
            private int requestedServices_;
            private ByteString reserved_ = ByteString.EMPTY;
            private long safeShop_;
            private int tabNum_;
            private UpdateRequest updateRequest_ = UpdateRequest.getDefaultInstance();
            private LazyStringList uri_ = LazyStringArrayList.EMPTY;
            private boolean visited_;
            private EventType windowEvent_ = EventType.CLICK;
            private int windowNum_;

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.uri_ = LazyStringArrayList.EMPTY;
                this.bitField0_ &= -2;
                this.callerId_ = 0;
                this.bitField0_ &= -3;
                this.locale_ = "";
                this.bitField0_ &= -5;
                this.apikey_ = ByteString.EMPTY;
                this.bitField0_ &= -9;
                this.identity_ = Identity.getDefaultInstance();
                this.bitField0_ &= -17;
                this.visited_ = false;
                this.bitField0_ &= -33;
                this.updateRequest_ = UpdateRequest.getDefaultInstance();
                this.bitField0_ &= -65;
                this.requestedServices_ = 0;
                this.bitField0_ &= -129;
                this.customKeyValue_ = Collections.emptyList();
                this.bitField0_ &= -257;
                this.referer_ = "";
                this.bitField0_ &= -513;
                this.windowNum_ = 0;
                this.bitField0_ &= -1025;
                this.tabNum_ = 0;
                this.bitField0_ &= -2049;
                this.windowEvent_ = EventType.CLICK;
                this.bitField0_ &= -4097;
                this.origin_ = OriginType.LINK;
                this.bitField0_ &= -8193;
                this.dnl_ = false;
                this.bitField0_ &= -16385;
                this.reserved_ = ByteString.EMPTY;
                this.bitField0_ &= -32769;
                this.safeShop_ = 0;
                this.bitField0_ &= -65537;
                this.client_ = Client.getDefaultInstance();
                this.bitField0_ &= -131073;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public UrlInfoRequest getDefaultInstanceForType() {
                return UrlInfoRequest.getDefaultInstance();
            }

            public UrlInfoRequest build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public UrlInfoRequest buildPartial() {
                UrlInfoRequest urlInfoRequest = new UrlInfoRequest((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((this.bitField0_ & 1) == 1) {
                    this.uri_ = new UnmodifiableLazyStringList(this.uri_);
                    this.bitField0_ &= -2;
                }
                urlInfoRequest.uri_ = this.uri_;
                if ((i & 2) == 2) {
                    i2 = 1;
                }
                urlInfoRequest.callerId_ = this.callerId_;
                if ((i & 4) == 4) {
                    i2 |= 2;
                }
                urlInfoRequest.locale_ = this.locale_;
                if ((i & 8) == 8) {
                    i2 |= 4;
                }
                urlInfoRequest.apikey_ = this.apikey_;
                if ((i & 16) == 16) {
                    i2 |= 8;
                }
                urlInfoRequest.identity_ = this.identity_;
                if ((i & 32) == 32) {
                    i2 |= 16;
                }
                urlInfoRequest.visited_ = this.visited_;
                if ((i & 64) == 64) {
                    i2 |= 32;
                }
                urlInfoRequest.updateRequest_ = this.updateRequest_;
                if ((i & 128) == 128) {
                    i2 |= 64;
                }
                urlInfoRequest.requestedServices_ = this.requestedServices_;
                if ((this.bitField0_ & 256) == 256) {
                    this.customKeyValue_ = Collections.unmodifiableList(this.customKeyValue_);
                    this.bitField0_ &= -257;
                }
                urlInfoRequest.customKeyValue_ = this.customKeyValue_;
                if ((i & 512) == 512) {
                    i2 |= 128;
                }
                urlInfoRequest.referer_ = this.referer_;
                if ((i & 1024) == 1024) {
                    i2 |= 256;
                }
                urlInfoRequest.windowNum_ = this.windowNum_;
                if ((i & 2048) == 2048) {
                    i2 |= 512;
                }
                urlInfoRequest.tabNum_ = this.tabNum_;
                if ((i & 4096) == 4096) {
                    i2 |= 1024;
                }
                urlInfoRequest.windowEvent_ = this.windowEvent_;
                if ((i & 8192) == 8192) {
                    i2 |= 2048;
                }
                urlInfoRequest.origin_ = this.origin_;
                if ((i & 16384) == 16384) {
                    i2 |= 4096;
                }
                urlInfoRequest.dnl_ = this.dnl_;
                if ((i & 32768) == 32768) {
                    i2 |= 8192;
                }
                urlInfoRequest.reserved_ = this.reserved_;
                if ((i & 65536) == 65536) {
                    i2 |= 16384;
                }
                urlInfoRequest.safeShop_ = this.safeShop_;
                if ((i & 131072) == 131072) {
                    i2 |= 32768;
                }
                urlInfoRequest.client_ = this.client_;
                urlInfoRequest.bitField0_ = i2;
                return urlInfoRequest;
            }

            public Builder mergeFrom(UrlInfoRequest urlInfoRequest) {
                if (urlInfoRequest == UrlInfoRequest.getDefaultInstance()) {
                    return this;
                }
                if (!urlInfoRequest.uri_.isEmpty()) {
                    if (this.uri_.isEmpty()) {
                        this.uri_ = urlInfoRequest.uri_;
                        this.bitField0_ &= -2;
                    } else {
                        ensureUriIsMutable();
                        this.uri_.addAll(urlInfoRequest.uri_);
                    }
                }
                if (urlInfoRequest.hasCallerId()) {
                    setCallerId(urlInfoRequest.getCallerId());
                }
                if (urlInfoRequest.hasLocale()) {
                    this.bitField0_ |= 4;
                    this.locale_ = urlInfoRequest.locale_;
                }
                if (urlInfoRequest.hasApikey()) {
                    setApikey(urlInfoRequest.getApikey());
                }
                if (urlInfoRequest.hasIdentity()) {
                    mergeIdentity(urlInfoRequest.getIdentity());
                }
                if (urlInfoRequest.hasVisited()) {
                    setVisited(urlInfoRequest.getVisited());
                }
                if (urlInfoRequest.hasUpdateRequest()) {
                    mergeUpdateRequest(urlInfoRequest.getUpdateRequest());
                }
                if (urlInfoRequest.hasRequestedServices()) {
                    setRequestedServices(urlInfoRequest.getRequestedServices());
                }
                if (!urlInfoRequest.customKeyValue_.isEmpty()) {
                    if (this.customKeyValue_.isEmpty()) {
                        this.customKeyValue_ = urlInfoRequest.customKeyValue_;
                        this.bitField0_ &= -257;
                    } else {
                        ensureCustomKeyValueIsMutable();
                        this.customKeyValue_.addAll(urlInfoRequest.customKeyValue_);
                    }
                }
                if (urlInfoRequest.hasReferer()) {
                    this.bitField0_ |= 512;
                    this.referer_ = urlInfoRequest.referer_;
                }
                if (urlInfoRequest.hasWindowNum()) {
                    setWindowNum(urlInfoRequest.getWindowNum());
                }
                if (urlInfoRequest.hasTabNum()) {
                    setTabNum(urlInfoRequest.getTabNum());
                }
                if (urlInfoRequest.hasWindowEvent()) {
                    setWindowEvent(urlInfoRequest.getWindowEvent());
                }
                if (urlInfoRequest.hasOrigin()) {
                    setOrigin(urlInfoRequest.getOrigin());
                }
                if (urlInfoRequest.hasDnl()) {
                    setDnl(urlInfoRequest.getDnl());
                }
                if (urlInfoRequest.hasReserved()) {
                    setReserved(urlInfoRequest.getReserved());
                }
                if (urlInfoRequest.hasSafeShop()) {
                    setSafeShop(urlInfoRequest.getSafeShop());
                }
                if (urlInfoRequest.hasClient()) {
                    mergeClient(urlInfoRequest.getClient());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                UrlInfoRequest urlInfoRequest;
                UrlInfoRequest urlInfoRequest2;
                try {
                    urlInfoRequest2 = (UrlInfoRequest) UrlInfoRequest.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (urlInfoRequest2 != null) {
                        mergeFrom(urlInfoRequest2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    urlInfoRequest2 = (UrlInfoRequest) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    urlInfoRequest = urlInfoRequest2;
                    th = th3;
                }
                if (urlInfoRequest != null) {
                    mergeFrom(urlInfoRequest);
                }
                throw th;
            }

            private void ensureUriIsMutable() {
                if ((this.bitField0_ & 1) != 1) {
                    this.uri_ = new LazyStringArrayList(this.uri_);
                    this.bitField0_ |= 1;
                }
            }

            public List<String> getUriList() {
                return Collections.unmodifiableList(this.uri_);
            }

            public int getUriCount() {
                return this.uri_.size();
            }

            public String getUri(int i) {
                return (String) this.uri_.get(i);
            }

            public ByteString getUriBytes(int i) {
                return this.uri_.getByteString(i);
            }

            public Builder setUri(int i, String str) {
                if (str != null) {
                    ensureUriIsMutable();
                    this.uri_.set(i, str);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addUri(String str) {
                if (str != null) {
                    ensureUriIsMutable();
                    this.uri_.add(str);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addAllUri(Iterable<String> iterable) {
                ensureUriIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(iterable, this.uri_);
                return this;
            }

            public Builder clearUri() {
                this.uri_ = LazyStringArrayList.EMPTY;
                this.bitField0_ &= -2;
                return this;
            }

            public Builder addUriBytes(ByteString byteString) {
                if (byteString != null) {
                    ensureUriIsMutable();
                    this.uri_.add(byteString);
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasCallerId() {
                return (this.bitField0_ & 2) == 2;
            }

            public long getCallerId() {
                return this.callerId_;
            }

            public Builder setCallerId(long j) {
                this.bitField0_ |= 2;
                this.callerId_ = j;
                return this;
            }

            public Builder clearCallerId() {
                this.bitField0_ &= -3;
                this.callerId_ = 0;
                return this;
            }

            public boolean hasLocale() {
                return (this.bitField0_ & 4) == 4;
            }

            public String getLocale() {
                Object obj = this.locale_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.locale_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getLocaleBytes() {
                Object obj = this.locale_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.locale_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setLocale(String str) {
                if (str != null) {
                    this.bitField0_ |= 4;
                    this.locale_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearLocale() {
                this.bitField0_ &= -5;
                this.locale_ = UrlInfoRequest.getDefaultInstance().getLocale();
                return this;
            }

            public Builder setLocaleBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 4;
                    this.locale_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasApikey() {
                return (this.bitField0_ & 8) == 8;
            }

            public ByteString getApikey() {
                return this.apikey_;
            }

            public Builder setApikey(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 8;
                    this.apikey_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearApikey() {
                this.bitField0_ &= -9;
                this.apikey_ = UrlInfoRequest.getDefaultInstance().getApikey();
                return this;
            }

            public boolean hasIdentity() {
                return (this.bitField0_ & 16) == 16;
            }

            public Identity getIdentity() {
                return this.identity_;
            }

            public Builder setIdentity(Identity identity) {
                if (identity != null) {
                    this.identity_ = identity;
                    this.bitField0_ |= 16;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setIdentity(Builder builder) {
                this.identity_ = builder.build();
                this.bitField0_ |= 16;
                return this;
            }

            public Builder mergeIdentity(Identity identity) {
                if ((this.bitField0_ & 16) == 16 && this.identity_ != Identity.getDefaultInstance()) {
                    this.identity_ = Identity.newBuilder(this.identity_).mergeFrom(identity).buildPartial();
                } else {
                    this.identity_ = identity;
                }
                this.bitField0_ |= 16;
                return this;
            }

            public Builder clearIdentity() {
                this.identity_ = Identity.getDefaultInstance();
                this.bitField0_ &= -17;
                return this;
            }

            public boolean hasVisited() {
                return (this.bitField0_ & 32) == 32;
            }

            public boolean getVisited() {
                return this.visited_;
            }

            public Builder setVisited(boolean z) {
                this.bitField0_ |= 32;
                this.visited_ = z;
                return this;
            }

            public Builder clearVisited() {
                this.bitField0_ &= -33;
                this.visited_ = false;
                return this;
            }

            public boolean hasUpdateRequest() {
                return (this.bitField0_ & 64) == 64;
            }

            public UpdateRequest getUpdateRequest() {
                return this.updateRequest_;
            }

            public Builder setUpdateRequest(UpdateRequest updateRequest) {
                if (updateRequest != null) {
                    this.updateRequest_ = updateRequest;
                    this.bitField0_ |= 64;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setUpdateRequest(com.avast.cloud.webrep.proto.BrowserInfo.UpdateRequest.Builder builder) {
                this.updateRequest_ = builder.build();
                this.bitField0_ |= 64;
                return this;
            }

            public Builder mergeUpdateRequest(UpdateRequest updateRequest) {
                if ((this.bitField0_ & 64) == 64 && this.updateRequest_ != UpdateRequest.getDefaultInstance()) {
                    this.updateRequest_ = UpdateRequest.newBuilder(this.updateRequest_).mergeFrom(updateRequest).buildPartial();
                } else {
                    this.updateRequest_ = updateRequest;
                }
                this.bitField0_ |= 64;
                return this;
            }

            public Builder clearUpdateRequest() {
                this.updateRequest_ = UpdateRequest.getDefaultInstance();
                this.bitField0_ &= -65;
                return this;
            }

            public boolean hasRequestedServices() {
                return (this.bitField0_ & 128) == 128;
            }

            public int getRequestedServices() {
                return this.requestedServices_;
            }

            public Builder setRequestedServices(int i) {
                this.bitField0_ |= 128;
                this.requestedServices_ = i;
                return this;
            }

            public Builder clearRequestedServices() {
                this.bitField0_ &= -129;
                this.requestedServices_ = 0;
                return this;
            }

            private void ensureCustomKeyValueIsMutable() {
                if ((this.bitField0_ & 256) != 256) {
                    this.customKeyValue_ = new ArrayList(this.customKeyValue_);
                    this.bitField0_ |= 256;
                }
            }

            public List<KeyValue> getCustomKeyValueList() {
                return Collections.unmodifiableList(this.customKeyValue_);
            }

            public int getCustomKeyValueCount() {
                return this.customKeyValue_.size();
            }

            public KeyValue getCustomKeyValue(int i) {
                return (KeyValue) this.customKeyValue_.get(i);
            }

            public Builder setCustomKeyValue(int i, KeyValue keyValue) {
                if (keyValue != null) {
                    ensureCustomKeyValueIsMutable();
                    this.customKeyValue_.set(i, keyValue);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setCustomKeyValue(int i, Builder builder) {
                ensureCustomKeyValueIsMutable();
                this.customKeyValue_.set(i, builder.build());
                return this;
            }

            public Builder addCustomKeyValue(KeyValue keyValue) {
                if (keyValue != null) {
                    ensureCustomKeyValueIsMutable();
                    this.customKeyValue_.add(keyValue);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addCustomKeyValue(int i, KeyValue keyValue) {
                if (keyValue != null) {
                    ensureCustomKeyValueIsMutable();
                    this.customKeyValue_.add(i, keyValue);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addCustomKeyValue(Builder builder) {
                ensureCustomKeyValueIsMutable();
                this.customKeyValue_.add(builder.build());
                return this;
            }

            public Builder addCustomKeyValue(int i, Builder builder) {
                ensureCustomKeyValueIsMutable();
                this.customKeyValue_.add(i, builder.build());
                return this;
            }

            public Builder addAllCustomKeyValue(Iterable<? extends KeyValue> iterable) {
                ensureCustomKeyValueIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(iterable, this.customKeyValue_);
                return this;
            }

            public Builder clearCustomKeyValue() {
                this.customKeyValue_ = Collections.emptyList();
                this.bitField0_ &= -257;
                return this;
            }

            public Builder removeCustomKeyValue(int i) {
                ensureCustomKeyValueIsMutable();
                this.customKeyValue_.remove(i);
                return this;
            }

            public boolean hasReferer() {
                return (this.bitField0_ & 512) == 512;
            }

            public String getReferer() {
                Object obj = this.referer_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.referer_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getRefererBytes() {
                Object obj = this.referer_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.referer_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setReferer(String str) {
                if (str != null) {
                    this.bitField0_ |= 512;
                    this.referer_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearReferer() {
                this.bitField0_ &= -513;
                this.referer_ = UrlInfoRequest.getDefaultInstance().getReferer();
                return this;
            }

            public Builder setRefererBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 512;
                    this.referer_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasWindowNum() {
                return (this.bitField0_ & 1024) == 1024;
            }

            public int getWindowNum() {
                return this.windowNum_;
            }

            public Builder setWindowNum(int i) {
                this.bitField0_ |= 1024;
                this.windowNum_ = i;
                return this;
            }

            public Builder clearWindowNum() {
                this.bitField0_ &= -1025;
                this.windowNum_ = 0;
                return this;
            }

            public boolean hasTabNum() {
                return (this.bitField0_ & 2048) == 2048;
            }

            public int getTabNum() {
                return this.tabNum_;
            }

            public Builder setTabNum(int i) {
                this.bitField0_ |= 2048;
                this.tabNum_ = i;
                return this;
            }

            public Builder clearTabNum() {
                this.bitField0_ &= -2049;
                this.tabNum_ = 0;
                return this;
            }

            public boolean hasWindowEvent() {
                return (this.bitField0_ & 4096) == 4096;
            }

            public EventType getWindowEvent() {
                return this.windowEvent_;
            }

            public Builder setWindowEvent(EventType eventType) {
                if (eventType != null) {
                    this.bitField0_ |= 4096;
                    this.windowEvent_ = eventType;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearWindowEvent() {
                this.bitField0_ &= -4097;
                this.windowEvent_ = EventType.CLICK;
                return this;
            }

            public boolean hasOrigin() {
                return (this.bitField0_ & 8192) == 8192;
            }

            public OriginType getOrigin() {
                return this.origin_;
            }

            public Builder setOrigin(OriginType originType) {
                if (originType != null) {
                    this.bitField0_ |= 8192;
                    this.origin_ = originType;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearOrigin() {
                this.bitField0_ &= -8193;
                this.origin_ = OriginType.LINK;
                return this;
            }

            public boolean hasDnl() {
                return (this.bitField0_ & 16384) == 16384;
            }

            public boolean getDnl() {
                return this.dnl_;
            }

            public Builder setDnl(boolean z) {
                this.bitField0_ |= 16384;
                this.dnl_ = z;
                return this;
            }

            public Builder clearDnl() {
                this.bitField0_ &= -16385;
                this.dnl_ = false;
                return this;
            }

            public boolean hasReserved() {
                return (this.bitField0_ & 32768) == 32768;
            }

            public ByteString getReserved() {
                return this.reserved_;
            }

            public Builder setReserved(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 32768;
                    this.reserved_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearReserved() {
                this.bitField0_ &= -32769;
                this.reserved_ = UrlInfoRequest.getDefaultInstance().getReserved();
                return this;
            }

            public boolean hasSafeShop() {
                return (this.bitField0_ & 65536) == 65536;
            }

            public long getSafeShop() {
                return this.safeShop_;
            }

            public Builder setSafeShop(long j) {
                this.bitField0_ |= 65536;
                this.safeShop_ = j;
                return this;
            }

            public Builder clearSafeShop() {
                this.bitField0_ &= -65537;
                this.safeShop_ = 0;
                return this;
            }

            public boolean hasClient() {
                return (this.bitField0_ & 131072) == 131072;
            }

            public Client getClient() {
                return this.client_;
            }

            public Builder setClient(Client client) {
                if (client != null) {
                    this.client_ = client;
                    this.bitField0_ |= 131072;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setClient(Builder builder) {
                this.client_ = builder.build();
                this.bitField0_ |= 131072;
                return this;
            }

            public Builder mergeClient(Client client) {
                if ((this.bitField0_ & 131072) == 131072 && this.client_ != Client.getDefaultInstance()) {
                    this.client_ = Client.newBuilder(this.client_).mergeFrom(client).buildPartial();
                } else {
                    this.client_ = client;
                }
                this.bitField0_ |= 131072;
                return this;
            }

            public Builder clearClient() {
                this.client_ = Client.getDefaultInstance();
                this.bitField0_ &= -131073;
                return this;
            }
        }

        private UrlInfoRequest(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private UrlInfoRequest(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static UrlInfoRequest getDefaultInstance() {
            return defaultInstance;
        }

        public UrlInfoRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        private UrlInfoRequest(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            Object obj = null;
            int i = 0;
            while (obj == null) {
                try {
                    int i2;
                    Object obj2;
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            readTag = 1;
                            i2 = i;
                            break;
                        case 10:
                            if ((i & 1) != 1) {
                                this.uri_ = new LazyStringArrayList();
                                i |= 1;
                            }
                            this.uri_.add(codedInputStream.readBytes());
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 16:
                            this.bitField0_ |= 1;
                            this.callerId_ = codedInputStream.readSInt64();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 26:
                            this.bitField0_ |= 2;
                            this.locale_ = codedInputStream.readBytes();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 34:
                            this.bitField0_ |= 4;
                            this.apikey_ = codedInputStream.readBytes();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case MotionEventCompat.AXIS_GENERIC_11 /*42*/:
                            Builder builder;
                            if ((this.bitField0_ & 8) != 8) {
                                builder = null;
                            } else {
                                builder = this.identity_.toBuilder();
                            }
                            this.identity_ = (Identity) codedInputStream.readMessage(Identity.PARSER, extensionRegistryLite);
                            if (builder != null) {
                                builder.mergeFrom(this.identity_);
                                this.identity_ = builder.buildPartial();
                            }
                            this.bitField0_ |= 8;
                            obj2 = obj;
                            i2 = i;
                            break;
                        case HwRcsCommonObject.BLACKLIST_MSG_VIDEO_TYPE /*48*/:
                            this.bitField0_ |= 16;
                            this.visited_ = codedInputStream.readBool();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 58:
                            com.avast.cloud.webrep.proto.BrowserInfo.UpdateRequest.Builder builder2;
                            if ((this.bitField0_ & 32) != 32) {
                                builder2 = null;
                            } else {
                                builder2 = this.updateRequest_.toBuilder();
                            }
                            this.updateRequest_ = (UpdateRequest) codedInputStream.readMessage(UpdateRequest.PARSER, extensionRegistryLite);
                            if (builder2 != null) {
                                builder2.mergeFrom(this.updateRequest_);
                                this.updateRequest_ = builder2.buildPartial();
                            }
                            this.bitField0_ |= 32;
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 64:
                            this.bitField0_ |= 64;
                            this.requestedServices_ = codedInputStream.readSInt32();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 74:
                            if ((i & 256) != 256) {
                                this.customKeyValue_ = new ArrayList();
                                i |= 256;
                            }
                            this.customKeyValue_.add(codedInputStream.readMessage(KeyValue.PARSER, extensionRegistryLite));
                            obj2 = obj;
                            i2 = i;
                            break;
                        case Events.E_ANTIVIRUS_SCAN /*82*/:
                            this.bitField0_ |= 128;
                            this.referer_ = codedInputStream.readBytes();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 88:
                            this.bitField0_ |= 256;
                            this.windowNum_ = codedInputStream.readSInt32();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 96:
                            this.bitField0_ |= 512;
                            this.tabNum_ = codedInputStream.readSInt32();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 104:
                            EventType valueOf = EventType.valueOf(codedInputStream.readEnum());
                            if (valueOf != null) {
                                this.bitField0_ |= 1024;
                                this.windowEvent_ = valueOf;
                                obj2 = obj;
                                i2 = i;
                                break;
                            }
                            obj2 = obj;
                            i2 = i;
                            break;
                        case Events.E_ADDVIEW_SET_ALL /*112*/:
                            OriginType valueOf2 = OriginType.valueOf(codedInputStream.readEnum());
                            if (valueOf2 != null) {
                                this.bitField0_ |= 2048;
                                this.origin_ = valueOf2;
                                obj2 = obj;
                                i2 = i;
                                break;
                            }
                            obj2 = obj;
                            i2 = i;
                            break;
                        case CircleViewNew.SIZE_OF_COLOR /*120*/:
                            this.bitField0_ |= 4096;
                            this.dnl_ = codedInputStream.readBool();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 130:
                            this.bitField0_ |= 8192;
                            this.reserved_ = codedInputStream.readBytes();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 136:
                            this.bitField0_ |= 16384;
                            this.safeShop_ = codedInputStream.readInt64();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 146:
                            Builder builder3;
                            if ((this.bitField0_ & 32768) != 32768) {
                                builder3 = null;
                            } else {
                                builder3 = this.client_.toBuilder();
                            }
                            this.client_ = (Client) codedInputStream.readMessage(Client.PARSER, extensionRegistryLite);
                            if (builder3 != null) {
                                builder3.mergeFrom(this.client_);
                                this.client_ = builder3.buildPartial();
                            }
                            this.bitField0_ |= 32768;
                            obj2 = obj;
                            i2 = i;
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                readTag = 1;
                                i2 = i;
                                break;
                            }
                            obj2 = obj;
                            i2 = i;
                            break;
                    }
                    i = i2;
                    obj = obj2;
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    if ((i & 1) == 1) {
                        this.uri_ = new UnmodifiableLazyStringList(this.uri_);
                    }
                    if ((i & 256) == 256) {
                        this.customKeyValue_ = Collections.unmodifiableList(this.customKeyValue_);
                    }
                    makeExtensionsImmutable();
                }
            }
            if ((i & 1) == 1) {
                this.uri_ = new UnmodifiableLazyStringList(this.uri_);
            }
            if ((i & 256) == 256) {
                this.customKeyValue_ = Collections.unmodifiableList(this.customKeyValue_);
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<UrlInfoRequest> getParserForType() {
            return PARSER;
        }

        public List<String> getUriList() {
            return this.uri_;
        }

        public int getUriCount() {
            return this.uri_.size();
        }

        public String getUri(int i) {
            return (String) this.uri_.get(i);
        }

        public ByteString getUriBytes(int i) {
            return this.uri_.getByteString(i);
        }

        public boolean hasCallerId() {
            return (this.bitField0_ & 1) == 1;
        }

        public long getCallerId() {
            return this.callerId_;
        }

        public boolean hasLocale() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getLocale() {
            Object obj = this.locale_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.locale_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getLocaleBytes() {
            Object obj = this.locale_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.locale_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasApikey() {
            return (this.bitField0_ & 4) == 4;
        }

        public ByteString getApikey() {
            return this.apikey_;
        }

        public boolean hasIdentity() {
            return (this.bitField0_ & 8) == 8;
        }

        public Identity getIdentity() {
            return this.identity_;
        }

        public boolean hasVisited() {
            return (this.bitField0_ & 16) == 16;
        }

        public boolean getVisited() {
            return this.visited_;
        }

        public boolean hasUpdateRequest() {
            return (this.bitField0_ & 32) == 32;
        }

        public UpdateRequest getUpdateRequest() {
            return this.updateRequest_;
        }

        public boolean hasRequestedServices() {
            return (this.bitField0_ & 64) == 64;
        }

        public int getRequestedServices() {
            return this.requestedServices_;
        }

        public List<KeyValue> getCustomKeyValueList() {
            return this.customKeyValue_;
        }

        public List<? extends KeyValueOrBuilder> getCustomKeyValueOrBuilderList() {
            return this.customKeyValue_;
        }

        public int getCustomKeyValueCount() {
            return this.customKeyValue_.size();
        }

        public KeyValue getCustomKeyValue(int i) {
            return (KeyValue) this.customKeyValue_.get(i);
        }

        public KeyValueOrBuilder getCustomKeyValueOrBuilder(int i) {
            return (KeyValueOrBuilder) this.customKeyValue_.get(i);
        }

        public boolean hasReferer() {
            return (this.bitField0_ & 128) == 128;
        }

        public String getReferer() {
            Object obj = this.referer_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.referer_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getRefererBytes() {
            Object obj = this.referer_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.referer_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasWindowNum() {
            return (this.bitField0_ & 256) == 256;
        }

        public int getWindowNum() {
            return this.windowNum_;
        }

        public boolean hasTabNum() {
            return (this.bitField0_ & 512) == 512;
        }

        public int getTabNum() {
            return this.tabNum_;
        }

        public boolean hasWindowEvent() {
            return (this.bitField0_ & 1024) == 1024;
        }

        public EventType getWindowEvent() {
            return this.windowEvent_;
        }

        public boolean hasOrigin() {
            return (this.bitField0_ & 2048) == 2048;
        }

        public OriginType getOrigin() {
            return this.origin_;
        }

        public boolean hasDnl() {
            return (this.bitField0_ & 4096) == 4096;
        }

        public boolean getDnl() {
            return this.dnl_;
        }

        public boolean hasReserved() {
            return (this.bitField0_ & 8192) == 8192;
        }

        public ByteString getReserved() {
            return this.reserved_;
        }

        public boolean hasSafeShop() {
            return (this.bitField0_ & 16384) == 16384;
        }

        public long getSafeShop() {
            return this.safeShop_;
        }

        public boolean hasClient() {
            return (this.bitField0_ & 32768) == 32768;
        }

        public Client getClient() {
            return this.client_;
        }

        private void initFields() {
            this.uri_ = LazyStringArrayList.EMPTY;
            this.callerId_ = 0;
            this.locale_ = "";
            this.apikey_ = ByteString.EMPTY;
            this.identity_ = Identity.getDefaultInstance();
            this.visited_ = false;
            this.updateRequest_ = UpdateRequest.getDefaultInstance();
            this.requestedServices_ = 0;
            this.customKeyValue_ = Collections.emptyList();
            this.referer_ = "";
            this.windowNum_ = 0;
            this.tabNum_ = 0;
            this.windowEvent_ = EventType.CLICK;
            this.origin_ = OriginType.LINK;
            this.dnl_ = false;
            this.reserved_ = ByteString.EMPTY;
            this.safeShop_ = 0;
            this.client_ = Client.getDefaultInstance();
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            int i = 0;
            getSerializedSize();
            for (int i2 = 0; i2 < this.uri_.size(); i2++) {
                codedOutputStream.writeBytes(1, this.uri_.getByteString(i2));
            }
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeSInt64(2, this.callerId_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(3, getLocaleBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeBytes(4, this.apikey_);
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeMessage(5, this.identity_);
            }
            if ((this.bitField0_ & 16) == 16) {
                codedOutputStream.writeBool(6, this.visited_);
            }
            if ((this.bitField0_ & 32) == 32) {
                codedOutputStream.writeMessage(7, this.updateRequest_);
            }
            if ((this.bitField0_ & 64) == 64) {
                codedOutputStream.writeSInt32(8, this.requestedServices_);
            }
            while (i < this.customKeyValue_.size()) {
                codedOutputStream.writeMessage(9, (MessageLite) this.customKeyValue_.get(i));
                i++;
            }
            if ((this.bitField0_ & 128) == 128) {
                codedOutputStream.writeBytes(10, getRefererBytes());
            }
            if ((this.bitField0_ & 256) == 256) {
                codedOutputStream.writeSInt32(11, this.windowNum_);
            }
            if ((this.bitField0_ & 512) == 512) {
                codedOutputStream.writeSInt32(12, this.tabNum_);
            }
            if ((this.bitField0_ & 1024) == 1024) {
                codedOutputStream.writeEnum(13, this.windowEvent_.getNumber());
            }
            if ((this.bitField0_ & 2048) == 2048) {
                codedOutputStream.writeEnum(14, this.origin_.getNumber());
            }
            if ((this.bitField0_ & 4096) == 4096) {
                codedOutputStream.writeBool(15, this.dnl_);
            }
            if ((this.bitField0_ & 8192) == 8192) {
                codedOutputStream.writeBytes(16, this.reserved_);
            }
            if ((this.bitField0_ & 16384) == 16384) {
                codedOutputStream.writeInt64(17, this.safeShop_);
            }
            if ((this.bitField0_ & 32768) == 32768) {
                codedOutputStream.writeMessage(18, this.client_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            int i3 = 0;
            for (i2 = 0; i2 < this.uri_.size(); i2++) {
                i3 += CodedOutputStream.computeBytesSizeNoTag(this.uri_.getByteString(i2));
            }
            i2 = (i3 + 0) + (getUriList().size() * 1);
            if ((this.bitField0_ & 1) == 1) {
                i2 += CodedOutputStream.computeSInt64Size(2, this.callerId_);
            }
            if ((this.bitField0_ & 2) == 2) {
                i2 += CodedOutputStream.computeBytesSize(3, getLocaleBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                i2 += CodedOutputStream.computeBytesSize(4, this.apikey_);
            }
            if ((this.bitField0_ & 8) == 8) {
                i2 += CodedOutputStream.computeMessageSize(5, this.identity_);
            }
            if ((this.bitField0_ & 16) == 16) {
                i2 += CodedOutputStream.computeBoolSize(6, this.visited_);
            }
            if ((this.bitField0_ & 32) == 32) {
                i2 += CodedOutputStream.computeMessageSize(7, this.updateRequest_);
            }
            if ((this.bitField0_ & 64) == 64) {
                i2 += CodedOutputStream.computeSInt32Size(8, this.requestedServices_);
            }
            i3 = i2;
            while (i < this.customKeyValue_.size()) {
                i++;
                i3 = CodedOutputStream.computeMessageSize(9, (MessageLite) this.customKeyValue_.get(i)) + i3;
            }
            if ((this.bitField0_ & 128) == 128) {
                i3 += CodedOutputStream.computeBytesSize(10, getRefererBytes());
            }
            if ((this.bitField0_ & 256) == 256) {
                i3 += CodedOutputStream.computeSInt32Size(11, this.windowNum_);
            }
            if ((this.bitField0_ & 512) == 512) {
                i3 += CodedOutputStream.computeSInt32Size(12, this.tabNum_);
            }
            if ((this.bitField0_ & 1024) == 1024) {
                i3 += CodedOutputStream.computeEnumSize(13, this.windowEvent_.getNumber());
            }
            if ((this.bitField0_ & 2048) == 2048) {
                i3 += CodedOutputStream.computeEnumSize(14, this.origin_.getNumber());
            }
            if ((this.bitField0_ & 4096) == 4096) {
                i3 += CodedOutputStream.computeBoolSize(15, this.dnl_);
            }
            if ((this.bitField0_ & 8192) == 8192) {
                i3 += CodedOutputStream.computeBytesSize(16, this.reserved_);
            }
            if ((this.bitField0_ & 16384) == 16384) {
                i3 += CodedOutputStream.computeInt64Size(17, this.safeShop_);
            }
            if ((this.bitField0_ & 32768) == 32768) {
                i3 += CodedOutputStream.computeMessageSize(18, this.client_);
            }
            this.memoizedSerializedSize = i3;
            return i3;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static UrlInfoRequest parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (UrlInfoRequest) PARSER.parseFrom(byteString);
        }

        public static UrlInfoRequest parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UrlInfoRequest) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static UrlInfoRequest parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (UrlInfoRequest) PARSER.parseFrom(bArr);
        }

        public static UrlInfoRequest parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UrlInfoRequest) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static UrlInfoRequest parseFrom(InputStream inputStream) throws IOException {
            return (UrlInfoRequest) PARSER.parseFrom(inputStream);
        }

        public static UrlInfoRequest parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlInfoRequest) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static UrlInfoRequest parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (UrlInfoRequest) PARSER.parseDelimitedFrom(inputStream);
        }

        public static UrlInfoRequest parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlInfoRequest) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static UrlInfoRequest parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (UrlInfoRequest) PARSER.parseFrom(codedInputStream);
        }

        public static UrlInfoRequest parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlInfoRequest) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(UrlInfoRequest urlInfoRequest) {
            return newBuilder().mergeFrom(urlInfoRequest);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface UrlInfoResponseOrBuilder extends MessageLiteOrBuilder {
        UpdateResponse getUpdateResponse();

        UrlInfo getUrlInfo(int i);

        int getUrlInfoCount();

        List<UrlInfo> getUrlInfoList();

        boolean hasUpdateResponse();
    }

    public static final class UrlInfoResponse extends GeneratedMessageLite implements UrlInfoResponseOrBuilder {
        public static Parser<UrlInfoResponse> PARSER = new AbstractParser<UrlInfoResponse>() {
            public UrlInfoResponse parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new UrlInfoResponse(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int UPDATERESPONSE_FIELD_NUMBER = 2;
        public static final int URLINFO_FIELD_NUMBER = 1;
        private static final UrlInfoResponse defaultInstance = new UrlInfoResponse(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private UpdateResponse updateResponse_;
        private List<UrlInfo> urlInfo_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<UrlInfoResponse, Builder> implements UrlInfoResponseOrBuilder {
            private int bitField0_;
            private UpdateResponse updateResponse_ = UpdateResponse.getDefaultInstance();
            private List<UrlInfo> urlInfo_ = Collections.emptyList();

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.urlInfo_ = Collections.emptyList();
                this.bitField0_ &= -2;
                this.updateResponse_ = UpdateResponse.getDefaultInstance();
                this.bitField0_ &= -3;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public UrlInfoResponse getDefaultInstanceForType() {
                return UrlInfoResponse.getDefaultInstance();
            }

            public UrlInfoResponse build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public UrlInfoResponse buildPartial() {
                UrlInfoResponse urlInfoResponse = new UrlInfoResponse((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((this.bitField0_ & 1) == 1) {
                    this.urlInfo_ = Collections.unmodifiableList(this.urlInfo_);
                    this.bitField0_ &= -2;
                }
                urlInfoResponse.urlInfo_ = this.urlInfo_;
                if ((i & 2) == 2) {
                    i2 = 1;
                }
                urlInfoResponse.updateResponse_ = this.updateResponse_;
                urlInfoResponse.bitField0_ = i2;
                return urlInfoResponse;
            }

            public Builder mergeFrom(UrlInfoResponse urlInfoResponse) {
                if (urlInfoResponse == UrlInfoResponse.getDefaultInstance()) {
                    return this;
                }
                if (!urlInfoResponse.urlInfo_.isEmpty()) {
                    if (this.urlInfo_.isEmpty()) {
                        this.urlInfo_ = urlInfoResponse.urlInfo_;
                        this.bitField0_ &= -2;
                    } else {
                        ensureUrlInfoIsMutable();
                        this.urlInfo_.addAll(urlInfoResponse.urlInfo_);
                    }
                }
                if (urlInfoResponse.hasUpdateResponse()) {
                    mergeUpdateResponse(urlInfoResponse.getUpdateResponse());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                UrlInfoResponse urlInfoResponse;
                Throwable th;
                UrlInfoResponse urlInfoResponse2;
                try {
                    urlInfoResponse = (UrlInfoResponse) UrlInfoResponse.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (urlInfoResponse != null) {
                        mergeFrom(urlInfoResponse);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    urlInfoResponse = (UrlInfoResponse) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    urlInfoResponse2 = urlInfoResponse;
                    th = th3;
                }
                if (urlInfoResponse2 != null) {
                    mergeFrom(urlInfoResponse2);
                }
                throw th;
            }

            private void ensureUrlInfoIsMutable() {
                if ((this.bitField0_ & 1) != 1) {
                    this.urlInfo_ = new ArrayList(this.urlInfo_);
                    this.bitField0_ |= 1;
                }
            }

            public List<UrlInfo> getUrlInfoList() {
                return Collections.unmodifiableList(this.urlInfo_);
            }

            public int getUrlInfoCount() {
                return this.urlInfo_.size();
            }

            public UrlInfo getUrlInfo(int i) {
                return (UrlInfo) this.urlInfo_.get(i);
            }

            public Builder setUrlInfo(int i, UrlInfo urlInfo) {
                if (urlInfo != null) {
                    ensureUrlInfoIsMutable();
                    this.urlInfo_.set(i, urlInfo);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setUrlInfo(int i, Builder builder) {
                ensureUrlInfoIsMutable();
                this.urlInfo_.set(i, builder.build());
                return this;
            }

            public Builder addUrlInfo(UrlInfo urlInfo) {
                if (urlInfo != null) {
                    ensureUrlInfoIsMutable();
                    this.urlInfo_.add(urlInfo);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addUrlInfo(int i, UrlInfo urlInfo) {
                if (urlInfo != null) {
                    ensureUrlInfoIsMutable();
                    this.urlInfo_.add(i, urlInfo);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addUrlInfo(Builder builder) {
                ensureUrlInfoIsMutable();
                this.urlInfo_.add(builder.build());
                return this;
            }

            public Builder addUrlInfo(int i, Builder builder) {
                ensureUrlInfoIsMutable();
                this.urlInfo_.add(i, builder.build());
                return this;
            }

            public Builder addAllUrlInfo(Iterable<? extends UrlInfo> iterable) {
                ensureUrlInfoIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(iterable, this.urlInfo_);
                return this;
            }

            public Builder clearUrlInfo() {
                this.urlInfo_ = Collections.emptyList();
                this.bitField0_ &= -2;
                return this;
            }

            public Builder removeUrlInfo(int i) {
                ensureUrlInfoIsMutable();
                this.urlInfo_.remove(i);
                return this;
            }

            public boolean hasUpdateResponse() {
                return (this.bitField0_ & 2) == 2;
            }

            public UpdateResponse getUpdateResponse() {
                return this.updateResponse_;
            }

            public Builder setUpdateResponse(UpdateResponse updateResponse) {
                if (updateResponse != null) {
                    this.updateResponse_ = updateResponse;
                    this.bitField0_ |= 2;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setUpdateResponse(com.avast.cloud.webrep.proto.BrowserInfo.UpdateResponse.Builder builder) {
                this.updateResponse_ = builder.build();
                this.bitField0_ |= 2;
                return this;
            }

            public Builder mergeUpdateResponse(UpdateResponse updateResponse) {
                if ((this.bitField0_ & 2) == 2 && this.updateResponse_ != UpdateResponse.getDefaultInstance()) {
                    this.updateResponse_ = UpdateResponse.newBuilder(this.updateResponse_).mergeFrom(updateResponse).buildPartial();
                } else {
                    this.updateResponse_ = updateResponse;
                }
                this.bitField0_ |= 2;
                return this;
            }

            public Builder clearUpdateResponse() {
                this.updateResponse_ = UpdateResponse.getDefaultInstance();
                this.bitField0_ &= -3;
                return this;
            }
        }

        private UrlInfoResponse(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private UrlInfoResponse(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static UrlInfoResponse getDefaultInstance() {
            return defaultInstance;
        }

        public UrlInfoResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        private UrlInfoResponse(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            Object obj = null;
            int i = 0;
            while (obj == null) {
                try {
                    int i2;
                    Object obj2;
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            readTag = 1;
                            i2 = i;
                            break;
                        case 10:
                            if ((i & 1) != 1) {
                                this.urlInfo_ = new ArrayList();
                                i |= 1;
                            }
                            this.urlInfo_.add(codedInputStream.readMessage(UrlInfo.PARSER, extensionRegistryLite));
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 18:
                            com.avast.cloud.webrep.proto.BrowserInfo.UpdateResponse.Builder builder;
                            if ((this.bitField0_ & 1) != 1) {
                                builder = null;
                            } else {
                                builder = this.updateResponse_.toBuilder();
                            }
                            this.updateResponse_ = (UpdateResponse) codedInputStream.readMessage(UpdateResponse.PARSER, extensionRegistryLite);
                            if (builder != null) {
                                builder.mergeFrom(this.updateResponse_);
                                this.updateResponse_ = builder.buildPartial();
                            }
                            this.bitField0_ |= 1;
                            obj2 = obj;
                            i2 = i;
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                readTag = 1;
                                i2 = i;
                                break;
                            }
                            obj2 = obj;
                            i2 = i;
                            break;
                    }
                    i = i2;
                    obj = obj2;
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    if ((i & 1) == 1) {
                        this.urlInfo_ = Collections.unmodifiableList(this.urlInfo_);
                    }
                    makeExtensionsImmutable();
                }
            }
            if ((i & 1) == 1) {
                this.urlInfo_ = Collections.unmodifiableList(this.urlInfo_);
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<UrlInfoResponse> getParserForType() {
            return PARSER;
        }

        public List<UrlInfo> getUrlInfoList() {
            return this.urlInfo_;
        }

        public List<? extends UrlInfoOrBuilder> getUrlInfoOrBuilderList() {
            return this.urlInfo_;
        }

        public int getUrlInfoCount() {
            return this.urlInfo_.size();
        }

        public UrlInfo getUrlInfo(int i) {
            return (UrlInfo) this.urlInfo_.get(i);
        }

        public UrlInfoOrBuilder getUrlInfoOrBuilder(int i) {
            return (UrlInfoOrBuilder) this.urlInfo_.get(i);
        }

        public boolean hasUpdateResponse() {
            return (this.bitField0_ & 1) == 1;
        }

        public UpdateResponse getUpdateResponse() {
            return this.updateResponse_;
        }

        private void initFields() {
            this.urlInfo_ = Collections.emptyList();
            this.updateResponse_ = UpdateResponse.getDefaultInstance();
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            for (int i = 0; i < this.urlInfo_.size(); i++) {
                codedOutputStream.writeMessage(1, (MessageLite) this.urlInfo_.get(i));
            }
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeMessage(2, this.updateResponse_);
            }
        }

        public int getSerializedSize() {
            int i = this.memoizedSerializedSize;
            if (i != -1) {
                return i;
            }
            int i2 = 0;
            for (i = 0; i < this.urlInfo_.size(); i++) {
                i2 += CodedOutputStream.computeMessageSize(1, (MessageLite) this.urlInfo_.get(i));
            }
            if ((this.bitField0_ & 1) == 1) {
                i2 += CodedOutputStream.computeMessageSize(2, this.updateResponse_);
            }
            this.memoizedSerializedSize = i2;
            return i2;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static UrlInfoResponse parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (UrlInfoResponse) PARSER.parseFrom(byteString);
        }

        public static UrlInfoResponse parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UrlInfoResponse) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static UrlInfoResponse parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (UrlInfoResponse) PARSER.parseFrom(bArr);
        }

        public static UrlInfoResponse parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UrlInfoResponse) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static UrlInfoResponse parseFrom(InputStream inputStream) throws IOException {
            return (UrlInfoResponse) PARSER.parseFrom(inputStream);
        }

        public static UrlInfoResponse parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlInfoResponse) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static UrlInfoResponse parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (UrlInfoResponse) PARSER.parseDelimitedFrom(inputStream);
        }

        public static UrlInfoResponse parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlInfoResponse) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static UrlInfoResponse parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (UrlInfoResponse) PARSER.parseFrom(codedInputStream);
        }

        public static UrlInfoResponse parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlInfoResponse) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(UrlInfoResponse urlInfoResponse) {
            return newBuilder().mergeFrom(urlInfoResponse);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface WebrepOrBuilder extends MessageLiteOrBuilder {
        long getFlags();

        int getRating();

        int getTtl();

        int getWeight();

        boolean hasFlags();

        boolean hasRating();

        boolean hasTtl();

        boolean hasWeight();
    }

    public static final class Webrep extends GeneratedMessageLite implements WebrepOrBuilder {
        public static final int FLAGS_FIELD_NUMBER = 4;
        public static Parser<Webrep> PARSER = new AbstractParser<Webrep>() {
            public Webrep parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new Webrep(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int RATING_FIELD_NUMBER = 1;
        public static final int TTL_FIELD_NUMBER = 3;
        public static final int WEIGHT_FIELD_NUMBER = 2;
        private static final Webrep defaultInstance = new Webrep(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private long flags_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private int rating_;
        private int ttl_;
        private int weight_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<Webrep, Builder> implements WebrepOrBuilder {
            private int bitField0_;
            private long flags_;
            private int rating_;
            private int ttl_;
            private int weight_;

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                this.rating_ = 0;
                this.bitField0_ &= -2;
                this.weight_ = 0;
                this.bitField0_ &= -3;
                this.ttl_ = 0;
                this.bitField0_ &= -5;
                this.flags_ = 0;
                this.bitField0_ &= -9;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public Webrep getDefaultInstanceForType() {
                return Webrep.getDefaultInstance();
            }

            public Webrep build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public Webrep buildPartial() {
                Webrep webrep = new Webrep((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                webrep.rating_ = this.rating_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                webrep.weight_ = this.weight_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                webrep.ttl_ = this.ttl_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                webrep.flags_ = this.flags_;
                webrep.bitField0_ = i2;
                return webrep;
            }

            public Builder mergeFrom(Webrep webrep) {
                if (webrep == Webrep.getDefaultInstance()) {
                    return this;
                }
                if (webrep.hasRating()) {
                    setRating(webrep.getRating());
                }
                if (webrep.hasWeight()) {
                    setWeight(webrep.getWeight());
                }
                if (webrep.hasTtl()) {
                    setTtl(webrep.getTtl());
                }
                if (webrep.hasFlags()) {
                    setFlags(webrep.getFlags());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Webrep webrep;
                Throwable th;
                Webrep webrep2;
                try {
                    webrep = (Webrep) Webrep.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (webrep != null) {
                        mergeFrom(webrep);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    webrep = (Webrep) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    webrep2 = webrep;
                    th = th3;
                }
                if (webrep2 != null) {
                    mergeFrom(webrep2);
                }
                throw th;
            }

            public boolean hasRating() {
                return (this.bitField0_ & 1) == 1;
            }

            public int getRating() {
                return this.rating_;
            }

            public Builder setRating(int i) {
                this.bitField0_ |= 1;
                this.rating_ = i;
                return this;
            }

            public Builder clearRating() {
                this.bitField0_ &= -2;
                this.rating_ = 0;
                return this;
            }

            public boolean hasWeight() {
                return (this.bitField0_ & 2) == 2;
            }

            public int getWeight() {
                return this.weight_;
            }

            public Builder setWeight(int i) {
                this.bitField0_ |= 2;
                this.weight_ = i;
                return this;
            }

            public Builder clearWeight() {
                this.bitField0_ &= -3;
                this.weight_ = 0;
                return this;
            }

            public boolean hasTtl() {
                return (this.bitField0_ & 4) == 4;
            }

            public int getTtl() {
                return this.ttl_;
            }

            public Builder setTtl(int i) {
                this.bitField0_ |= 4;
                this.ttl_ = i;
                return this;
            }

            public Builder clearTtl() {
                this.bitField0_ &= -5;
                this.ttl_ = 0;
                return this;
            }

            public boolean hasFlags() {
                return (this.bitField0_ & 8) == 8;
            }

            public long getFlags() {
                return this.flags_;
            }

            public Builder setFlags(long j) {
                this.bitField0_ |= 8;
                this.flags_ = j;
                return this;
            }

            public Builder clearFlags() {
                this.bitField0_ &= -9;
                this.flags_ = 0;
                return this;
            }
        }

        private Webrep(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private Webrep(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static Webrep getDefaultInstance() {
            return defaultInstance;
        }

        public Webrep getDefaultInstanceForType() {
            return defaultInstance;
        }

        private Webrep(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 8:
                            this.bitField0_ |= 1;
                            this.rating_ = codedInputStream.readSInt32();
                            break;
                        case 16:
                            this.bitField0_ |= 2;
                            this.weight_ = codedInputStream.readSInt32();
                            break;
                        case 24:
                            this.bitField0_ |= 4;
                            this.ttl_ = codedInputStream.readSInt32();
                            break;
                        case 32:
                            this.bitField0_ |= 8;
                            this.flags_ = codedInputStream.readInt64();
                            break;
                        default:
                            if (!parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                obj = 1;
                                break;
                            }
                            break;
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(this);
                } catch (IOException e2) {
                    throw new InvalidProtocolBufferException(e2.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<Webrep> getParserForType() {
            return PARSER;
        }

        public boolean hasRating() {
            return (this.bitField0_ & 1) == 1;
        }

        public int getRating() {
            return this.rating_;
        }

        public boolean hasWeight() {
            return (this.bitField0_ & 2) == 2;
        }

        public int getWeight() {
            return this.weight_;
        }

        public boolean hasTtl() {
            return (this.bitField0_ & 4) == 4;
        }

        public int getTtl() {
            return this.ttl_;
        }

        public boolean hasFlags() {
            return (this.bitField0_ & 8) == 8;
        }

        public long getFlags() {
            return this.flags_;
        }

        private void initFields() {
            this.rating_ = 0;
            this.weight_ = 0;
            this.ttl_ = 0;
            this.flags_ = 0;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.memoizedIsInitialized;
            if (b == (byte) -1) {
                this.memoizedIsInitialized = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeSInt32(1, this.rating_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeSInt32(2, this.weight_);
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeSInt32(3, this.ttl_);
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeInt64(4, this.flags_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeSInt32Size(1, this.rating_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeSInt32Size(2, this.weight_);
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeSInt32Size(3, this.ttl_);
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeInt64Size(4, this.flags_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static Webrep parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (Webrep) PARSER.parseFrom(byteString);
        }

        public static Webrep parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Webrep) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static Webrep parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (Webrep) PARSER.parseFrom(bArr);
        }

        public static Webrep parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Webrep) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static Webrep parseFrom(InputStream inputStream) throws IOException {
            return (Webrep) PARSER.parseFrom(inputStream);
        }

        public static Webrep parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Webrep) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static Webrep parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (Webrep) PARSER.parseDelimitedFrom(inputStream);
        }

        public static Webrep parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Webrep) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static Webrep parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (Webrep) PARSER.parseFrom(codedInputStream);
        }

        public static Webrep parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Webrep) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(Webrep webrep) {
            return newBuilder().mergeFrom(webrep);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    private Urlinfo() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite extensionRegistryLite) {
    }
}
