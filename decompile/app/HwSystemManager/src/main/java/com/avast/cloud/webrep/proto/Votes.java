package com.avast.cloud.webrep.proto;

import com.avast.cloud.webrep.proto.Urlinfo.Identity;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import com.google.protobuf.Parser;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;

public final class Votes {

    public interface GetVoteRequestOrBuilder extends MessageLiteOrBuilder {
        Identity getIdentity();

        String getUri();

        ByteString getUriBytes();

        boolean hasIdentity();

        boolean hasUri();
    }

    public static final class GetVoteRequest extends GeneratedMessageLite implements GetVoteRequestOrBuilder {
        public static final int IDENTITY_FIELD_NUMBER = 1;
        public static Parser<GetVoteRequest> PARSER = new AbstractParser<GetVoteRequest>() {
            public GetVoteRequest parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new GetVoteRequest(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int URI_FIELD_NUMBER = 2;
        private static final GetVoteRequest defaultInstance = new GetVoteRequest(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private Identity identity_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object uri_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<GetVoteRequest, Builder> implements GetVoteRequestOrBuilder {
            private int bitField0_;
            private Identity identity_ = Identity.getDefaultInstance();
            private Object uri_ = "";

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
                this.identity_ = Identity.getDefaultInstance();
                this.bitField0_ &= -2;
                this.uri_ = "";
                this.bitField0_ &= -3;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public GetVoteRequest getDefaultInstanceForType() {
                return GetVoteRequest.getDefaultInstance();
            }

            public GetVoteRequest build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public GetVoteRequest buildPartial() {
                GetVoteRequest getVoteRequest = new GetVoteRequest((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                getVoteRequest.identity_ = this.identity_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                getVoteRequest.uri_ = this.uri_;
                getVoteRequest.bitField0_ = i2;
                return getVoteRequest;
            }

            public Builder mergeFrom(GetVoteRequest getVoteRequest) {
                if (getVoteRequest == GetVoteRequest.getDefaultInstance()) {
                    return this;
                }
                if (getVoteRequest.hasIdentity()) {
                    mergeIdentity(getVoteRequest.getIdentity());
                }
                if (getVoteRequest.hasUri()) {
                    this.bitField0_ |= 2;
                    this.uri_ = getVoteRequest.uri_;
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                GetVoteRequest getVoteRequest;
                GetVoteRequest getVoteRequest2;
                try {
                    getVoteRequest2 = (GetVoteRequest) GetVoteRequest.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (getVoteRequest2 != null) {
                        mergeFrom(getVoteRequest2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    getVoteRequest2 = (GetVoteRequest) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    getVoteRequest = getVoteRequest2;
                    th = th3;
                }
                if (getVoteRequest != null) {
                    mergeFrom(getVoteRequest);
                }
                throw th;
            }

            public boolean hasIdentity() {
                return (this.bitField0_ & 1) == 1;
            }

            public Identity getIdentity() {
                return this.identity_;
            }

            public Builder setIdentity(Identity identity) {
                if (identity != null) {
                    this.identity_ = identity;
                    this.bitField0_ |= 1;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setIdentity(com.avast.cloud.webrep.proto.Urlinfo.Identity.Builder builder) {
                this.identity_ = builder.build();
                this.bitField0_ |= 1;
                return this;
            }

            public Builder mergeIdentity(Identity identity) {
                if ((this.bitField0_ & 1) == 1 && this.identity_ != Identity.getDefaultInstance()) {
                    this.identity_ = Identity.newBuilder(this.identity_).mergeFrom(identity).buildPartial();
                } else {
                    this.identity_ = identity;
                }
                this.bitField0_ |= 1;
                return this;
            }

            public Builder clearIdentity() {
                this.identity_ = Identity.getDefaultInstance();
                this.bitField0_ &= -2;
                return this;
            }

            public boolean hasUri() {
                return (this.bitField0_ & 2) == 2;
            }

            public String getUri() {
                Object obj = this.uri_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.uri_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getUriBytes() {
                Object obj = this.uri_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.uri_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setUri(String str) {
                if (str != null) {
                    this.bitField0_ |= 2;
                    this.uri_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearUri() {
                this.bitField0_ &= -3;
                this.uri_ = GetVoteRequest.getDefaultInstance().getUri();
                return this;
            }

            public Builder setUriBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.uri_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }
        }

        private GetVoteRequest(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private GetVoteRequest(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static GetVoteRequest getDefaultInstance() {
            return defaultInstance;
        }

        public GetVoteRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        private GetVoteRequest(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            com.avast.cloud.webrep.proto.Urlinfo.Identity.Builder builder;
                            if ((this.bitField0_ & 1) != 1) {
                                builder = null;
                            } else {
                                builder = this.identity_.toBuilder();
                            }
                            this.identity_ = (Identity) codedInputStream.readMessage(Identity.PARSER, extensionRegistryLite);
                            if (builder != null) {
                                builder.mergeFrom(this.identity_);
                                this.identity_ = builder.buildPartial();
                            }
                            this.bitField0_ |= 1;
                            obj2 = obj;
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.uri_ = codedInputStream.readBytes();
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

        public Parser<GetVoteRequest> getParserForType() {
            return PARSER;
        }

        public boolean hasIdentity() {
            return (this.bitField0_ & 1) == 1;
        }

        public Identity getIdentity() {
            return this.identity_;
        }

        public boolean hasUri() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getUri() {
            Object obj = this.uri_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.uri_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getUriBytes() {
            Object obj = this.uri_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.uri_ = copyFromUtf8;
            return copyFromUtf8;
        }

        private void initFields() {
            this.identity_ = Identity.getDefaultInstance();
            this.uri_ = "";
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
                codedOutputStream.writeMessage(1, this.identity_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, getUriBytes());
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.identity_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, getUriBytes());
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static GetVoteRequest parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (GetVoteRequest) PARSER.parseFrom(byteString);
        }

        public static GetVoteRequest parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (GetVoteRequest) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static GetVoteRequest parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (GetVoteRequest) PARSER.parseFrom(bArr);
        }

        public static GetVoteRequest parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (GetVoteRequest) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static GetVoteRequest parseFrom(InputStream inputStream) throws IOException {
            return (GetVoteRequest) PARSER.parseFrom(inputStream);
        }

        public static GetVoteRequest parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (GetVoteRequest) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static GetVoteRequest parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (GetVoteRequest) PARSER.parseDelimitedFrom(inputStream);
        }

        public static GetVoteRequest parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (GetVoteRequest) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static GetVoteRequest parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (GetVoteRequest) PARSER.parseFrom(codedInputStream);
        }

        public static GetVoteRequest parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (GetVoteRequest) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(GetVoteRequest getVoteRequest) {
            return newBuilder().mergeFrom(getVoteRequest);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface PutVoteRequestOrBuilder extends MessageLiteOrBuilder {
        Identity getIdentity();

        int getTtl();

        String getUri();

        ByteString getUriBytes();

        Vote getVote();

        boolean hasIdentity();

        boolean hasTtl();

        boolean hasUri();

        boolean hasVote();
    }

    public static final class PutVoteRequest extends GeneratedMessageLite implements PutVoteRequestOrBuilder {
        public static final int IDENTITY_FIELD_NUMBER = 1;
        public static Parser<PutVoteRequest> PARSER = new AbstractParser<PutVoteRequest>() {
            public PutVoteRequest parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new PutVoteRequest(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int TTL_FIELD_NUMBER = 4;
        public static final int URI_FIELD_NUMBER = 2;
        public static final int VOTE_FIELD_NUMBER = 3;
        private static final PutVoteRequest defaultInstance = new PutVoteRequest(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private Identity identity_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private int ttl_;
        private Object uri_;
        private Vote vote_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<PutVoteRequest, Builder> implements PutVoteRequestOrBuilder {
            private int bitField0_;
            private Identity identity_ = Identity.getDefaultInstance();
            private int ttl_;
            private Object uri_ = "";
            private Vote vote_ = Vote.getDefaultInstance();

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
                this.identity_ = Identity.getDefaultInstance();
                this.bitField0_ &= -2;
                this.uri_ = "";
                this.bitField0_ &= -3;
                this.vote_ = Vote.getDefaultInstance();
                this.bitField0_ &= -5;
                this.ttl_ = 0;
                this.bitField0_ &= -9;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public PutVoteRequest getDefaultInstanceForType() {
                return PutVoteRequest.getDefaultInstance();
            }

            public PutVoteRequest build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public PutVoteRequest buildPartial() {
                PutVoteRequest putVoteRequest = new PutVoteRequest((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                putVoteRequest.identity_ = this.identity_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                putVoteRequest.uri_ = this.uri_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                putVoteRequest.vote_ = this.vote_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                putVoteRequest.ttl_ = this.ttl_;
                putVoteRequest.bitField0_ = i2;
                return putVoteRequest;
            }

            public Builder mergeFrom(PutVoteRequest putVoteRequest) {
                if (putVoteRequest == PutVoteRequest.getDefaultInstance()) {
                    return this;
                }
                if (putVoteRequest.hasIdentity()) {
                    mergeIdentity(putVoteRequest.getIdentity());
                }
                if (putVoteRequest.hasUri()) {
                    this.bitField0_ |= 2;
                    this.uri_ = putVoteRequest.uri_;
                }
                if (putVoteRequest.hasVote()) {
                    mergeVote(putVoteRequest.getVote());
                }
                if (putVoteRequest.hasTtl()) {
                    setTtl(putVoteRequest.getTtl());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                PutVoteRequest putVoteRequest;
                Throwable th;
                PutVoteRequest putVoteRequest2;
                try {
                    putVoteRequest = (PutVoteRequest) PutVoteRequest.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (putVoteRequest != null) {
                        mergeFrom(putVoteRequest);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    putVoteRequest = (PutVoteRequest) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    putVoteRequest2 = putVoteRequest;
                    th = th3;
                }
                if (putVoteRequest2 != null) {
                    mergeFrom(putVoteRequest2);
                }
                throw th;
            }

            public boolean hasIdentity() {
                return (this.bitField0_ & 1) == 1;
            }

            public Identity getIdentity() {
                return this.identity_;
            }

            public Builder setIdentity(Identity identity) {
                if (identity != null) {
                    this.identity_ = identity;
                    this.bitField0_ |= 1;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setIdentity(com.avast.cloud.webrep.proto.Urlinfo.Identity.Builder builder) {
                this.identity_ = builder.build();
                this.bitField0_ |= 1;
                return this;
            }

            public Builder mergeIdentity(Identity identity) {
                if ((this.bitField0_ & 1) == 1 && this.identity_ != Identity.getDefaultInstance()) {
                    this.identity_ = Identity.newBuilder(this.identity_).mergeFrom(identity).buildPartial();
                } else {
                    this.identity_ = identity;
                }
                this.bitField0_ |= 1;
                return this;
            }

            public Builder clearIdentity() {
                this.identity_ = Identity.getDefaultInstance();
                this.bitField0_ &= -2;
                return this;
            }

            public boolean hasUri() {
                return (this.bitField0_ & 2) == 2;
            }

            public String getUri() {
                Object obj = this.uri_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.uri_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getUriBytes() {
                Object obj = this.uri_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.uri_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setUri(String str) {
                if (str != null) {
                    this.bitField0_ |= 2;
                    this.uri_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearUri() {
                this.bitField0_ &= -3;
                this.uri_ = PutVoteRequest.getDefaultInstance().getUri();
                return this;
            }

            public Builder setUriBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.uri_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasVote() {
                return (this.bitField0_ & 4) == 4;
            }

            public Vote getVote() {
                return this.vote_;
            }

            public Builder setVote(Vote vote) {
                if (vote != null) {
                    this.vote_ = vote;
                    this.bitField0_ |= 4;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setVote(Builder builder) {
                this.vote_ = builder.build();
                this.bitField0_ |= 4;
                return this;
            }

            public Builder mergeVote(Vote vote) {
                if ((this.bitField0_ & 4) == 4 && this.vote_ != Vote.getDefaultInstance()) {
                    this.vote_ = Vote.newBuilder(this.vote_).mergeFrom(vote).buildPartial();
                } else {
                    this.vote_ = vote;
                }
                this.bitField0_ |= 4;
                return this;
            }

            public Builder clearVote() {
                this.vote_ = Vote.getDefaultInstance();
                this.bitField0_ &= -5;
                return this;
            }

            public boolean hasTtl() {
                return (this.bitField0_ & 8) == 8;
            }

            public int getTtl() {
                return this.ttl_;
            }

            public Builder setTtl(int i) {
                this.bitField0_ |= 8;
                this.ttl_ = i;
                return this;
            }

            public Builder clearTtl() {
                this.bitField0_ &= -9;
                this.ttl_ = 0;
                return this;
            }
        }

        private PutVoteRequest(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private PutVoteRequest(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static PutVoteRequest getDefaultInstance() {
            return defaultInstance;
        }

        public PutVoteRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        private PutVoteRequest(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            com.avast.cloud.webrep.proto.Urlinfo.Identity.Builder builder;
                            if ((this.bitField0_ & 1) != 1) {
                                builder = null;
                            } else {
                                builder = this.identity_.toBuilder();
                            }
                            this.identity_ = (Identity) codedInputStream.readMessage(Identity.PARSER, extensionRegistryLite);
                            if (builder != null) {
                                builder.mergeFrom(this.identity_);
                                this.identity_ = builder.buildPartial();
                            }
                            this.bitField0_ |= 1;
                            obj2 = obj;
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.uri_ = codedInputStream.readBytes();
                            obj2 = obj;
                            break;
                        case 26:
                            Builder builder2;
                            if ((this.bitField0_ & 4) != 4) {
                                builder2 = null;
                            } else {
                                builder2 = this.vote_.toBuilder();
                            }
                            this.vote_ = (Vote) codedInputStream.readMessage(Vote.PARSER, extensionRegistryLite);
                            if (builder2 != null) {
                                builder2.mergeFrom(this.vote_);
                                this.vote_ = builder2.buildPartial();
                            }
                            this.bitField0_ |= 4;
                            obj2 = obj;
                            break;
                        case 32:
                            this.bitField0_ |= 8;
                            this.ttl_ = codedInputStream.readSInt32();
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

        public Parser<PutVoteRequest> getParserForType() {
            return PARSER;
        }

        public boolean hasIdentity() {
            return (this.bitField0_ & 1) == 1;
        }

        public Identity getIdentity() {
            return this.identity_;
        }

        public boolean hasUri() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getUri() {
            Object obj = this.uri_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.uri_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getUriBytes() {
            Object obj = this.uri_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.uri_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasVote() {
            return (this.bitField0_ & 4) == 4;
        }

        public Vote getVote() {
            return this.vote_;
        }

        public boolean hasTtl() {
            return (this.bitField0_ & 8) == 8;
        }

        public int getTtl() {
            return this.ttl_;
        }

        private void initFields() {
            this.identity_ = Identity.getDefaultInstance();
            this.uri_ = "";
            this.vote_ = Vote.getDefaultInstance();
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
                codedOutputStream.writeMessage(1, this.identity_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, getUriBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeMessage(3, this.vote_);
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeSInt32(4, this.ttl_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.identity_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, getUriBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeMessageSize(3, this.vote_);
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeSInt32Size(4, this.ttl_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static PutVoteRequest parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (PutVoteRequest) PARSER.parseFrom(byteString);
        }

        public static PutVoteRequest parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (PutVoteRequest) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static PutVoteRequest parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (PutVoteRequest) PARSER.parseFrom(bArr);
        }

        public static PutVoteRequest parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (PutVoteRequest) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static PutVoteRequest parseFrom(InputStream inputStream) throws IOException {
            return (PutVoteRequest) PARSER.parseFrom(inputStream);
        }

        public static PutVoteRequest parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (PutVoteRequest) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static PutVoteRequest parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (PutVoteRequest) PARSER.parseDelimitedFrom(inputStream);
        }

        public static PutVoteRequest parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (PutVoteRequest) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static PutVoteRequest parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (PutVoteRequest) PARSER.parseFrom(codedInputStream);
        }

        public static PutVoteRequest parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (PutVoteRequest) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(PutVoteRequest putVoteRequest) {
            return newBuilder().mergeFrom(putVoteRequest);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface VoteOrBuilder extends MessageLiteOrBuilder {
        long getFlags();

        int getRating();

        boolean hasFlags();

        boolean hasRating();
    }

    public static final class Vote extends GeneratedMessageLite implements VoteOrBuilder {
        public static final int FLAGS_FIELD_NUMBER = 2;
        public static Parser<Vote> PARSER = new AbstractParser<Vote>() {
            public Vote parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new Vote(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int RATING_FIELD_NUMBER = 1;
        private static final Vote defaultInstance = new Vote(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private long flags_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private int rating_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<Vote, Builder> implements VoteOrBuilder {
            private int bitField0_;
            private long flags_;
            private int rating_;

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
                this.flags_ = 0;
                this.bitField0_ &= -3;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public Vote getDefaultInstanceForType() {
                return Vote.getDefaultInstance();
            }

            public Vote build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public Vote buildPartial() {
                Vote vote = new Vote((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                vote.rating_ = this.rating_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                vote.flags_ = this.flags_;
                vote.bitField0_ = i2;
                return vote;
            }

            public Builder mergeFrom(Vote vote) {
                if (vote == Vote.getDefaultInstance()) {
                    return this;
                }
                if (vote.hasRating()) {
                    setRating(vote.getRating());
                }
                if (vote.hasFlags()) {
                    setFlags(vote.getFlags());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Vote vote;
                Throwable th;
                Vote vote2;
                try {
                    vote = (Vote) Vote.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (vote != null) {
                        mergeFrom(vote);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    vote = (Vote) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    vote2 = vote;
                    th = th3;
                }
                if (vote2 != null) {
                    mergeFrom(vote2);
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

            public boolean hasFlags() {
                return (this.bitField0_ & 2) == 2;
            }

            public long getFlags() {
                return this.flags_;
            }

            public Builder setFlags(long j) {
                this.bitField0_ |= 2;
                this.flags_ = j;
                return this;
            }

            public Builder clearFlags() {
                this.bitField0_ &= -3;
                this.flags_ = 0;
                return this;
            }
        }

        private Vote(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private Vote(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static Vote getDefaultInstance() {
            return defaultInstance;
        }

        public Vote getDefaultInstanceForType() {
            return defaultInstance;
        }

        private Vote(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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

        public Parser<Vote> getParserForType() {
            return PARSER;
        }

        public boolean hasRating() {
            return (this.bitField0_ & 1) == 1;
        }

        public int getRating() {
            return this.rating_;
        }

        public boolean hasFlags() {
            return (this.bitField0_ & 2) == 2;
        }

        public long getFlags() {
            return this.flags_;
        }

        private void initFields() {
            this.rating_ = 0;
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
                codedOutputStream.writeInt64(2, this.flags_);
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
                i += CodedOutputStream.computeInt64Size(2, this.flags_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static Vote parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (Vote) PARSER.parseFrom(byteString);
        }

        public static Vote parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Vote) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static Vote parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (Vote) PARSER.parseFrom(bArr);
        }

        public static Vote parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Vote) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static Vote parseFrom(InputStream inputStream) throws IOException {
            return (Vote) PARSER.parseFrom(inputStream);
        }

        public static Vote parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Vote) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static Vote parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (Vote) PARSER.parseDelimitedFrom(inputStream);
        }

        public static Vote parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Vote) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static Vote parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (Vote) PARSER.parseFrom(codedInputStream);
        }

        public static Vote parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Vote) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(Vote vote) {
            return newBuilder().mergeFrom(vote);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface WeightedVoteOrBuilder extends MessageLiteOrBuilder {
        long getAge();

        float getTimeWeight();

        int getUserWeight();

        Vote getVote();

        boolean hasAge();

        boolean hasTimeWeight();

        boolean hasUserWeight();

        boolean hasVote();
    }

    public static final class WeightedVote extends GeneratedMessageLite implements WeightedVoteOrBuilder {
        public static final int AGE_FIELD_NUMBER = 4;
        public static Parser<WeightedVote> PARSER = new AbstractParser<WeightedVote>() {
            public WeightedVote parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new WeightedVote(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int TIMEWEIGHT_FIELD_NUMBER = 3;
        public static final int USERWEIGHT_FIELD_NUMBER = 2;
        public static final int VOTE_FIELD_NUMBER = 1;
        private static final WeightedVote defaultInstance = new WeightedVote(true);
        private static final long serialVersionUID = 0;
        private long age_;
        private int bitField0_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private float timeWeight_;
        private int userWeight_;
        private Vote vote_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<WeightedVote, Builder> implements WeightedVoteOrBuilder {
            private long age_;
            private int bitField0_;
            private float timeWeight_;
            private int userWeight_;
            private Vote vote_ = Vote.getDefaultInstance();

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
                this.vote_ = Vote.getDefaultInstance();
                this.bitField0_ &= -2;
                this.userWeight_ = 0;
                this.bitField0_ &= -3;
                this.timeWeight_ = 0.0f;
                this.bitField0_ &= -5;
                this.age_ = 0;
                this.bitField0_ &= -9;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public WeightedVote getDefaultInstanceForType() {
                return WeightedVote.getDefaultInstance();
            }

            public WeightedVote build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public WeightedVote buildPartial() {
                WeightedVote weightedVote = new WeightedVote((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                weightedVote.vote_ = this.vote_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                weightedVote.userWeight_ = this.userWeight_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                weightedVote.timeWeight_ = this.timeWeight_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                weightedVote.age_ = this.age_;
                weightedVote.bitField0_ = i2;
                return weightedVote;
            }

            public Builder mergeFrom(WeightedVote weightedVote) {
                if (weightedVote == WeightedVote.getDefaultInstance()) {
                    return this;
                }
                if (weightedVote.hasVote()) {
                    mergeVote(weightedVote.getVote());
                }
                if (weightedVote.hasUserWeight()) {
                    setUserWeight(weightedVote.getUserWeight());
                }
                if (weightedVote.hasTimeWeight()) {
                    setTimeWeight(weightedVote.getTimeWeight());
                }
                if (weightedVote.hasAge()) {
                    setAge(weightedVote.getAge());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                WeightedVote weightedVote;
                WeightedVote weightedVote2;
                try {
                    weightedVote2 = (WeightedVote) WeightedVote.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (weightedVote2 != null) {
                        mergeFrom(weightedVote2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    weightedVote2 = (WeightedVote) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    weightedVote = weightedVote2;
                    th = th3;
                }
                if (weightedVote != null) {
                    mergeFrom(weightedVote);
                }
                throw th;
            }

            public boolean hasVote() {
                return (this.bitField0_ & 1) == 1;
            }

            public Vote getVote() {
                return this.vote_;
            }

            public Builder setVote(Vote vote) {
                if (vote != null) {
                    this.vote_ = vote;
                    this.bitField0_ |= 1;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setVote(Builder builder) {
                this.vote_ = builder.build();
                this.bitField0_ |= 1;
                return this;
            }

            public Builder mergeVote(Vote vote) {
                if ((this.bitField0_ & 1) == 1 && this.vote_ != Vote.getDefaultInstance()) {
                    this.vote_ = Vote.newBuilder(this.vote_).mergeFrom(vote).buildPartial();
                } else {
                    this.vote_ = vote;
                }
                this.bitField0_ |= 1;
                return this;
            }

            public Builder clearVote() {
                this.vote_ = Vote.getDefaultInstance();
                this.bitField0_ &= -2;
                return this;
            }

            public boolean hasUserWeight() {
                return (this.bitField0_ & 2) == 2;
            }

            public int getUserWeight() {
                return this.userWeight_;
            }

            public Builder setUserWeight(int i) {
                this.bitField0_ |= 2;
                this.userWeight_ = i;
                return this;
            }

            public Builder clearUserWeight() {
                this.bitField0_ &= -3;
                this.userWeight_ = 0;
                return this;
            }

            public boolean hasTimeWeight() {
                return (this.bitField0_ & 4) == 4;
            }

            public float getTimeWeight() {
                return this.timeWeight_;
            }

            public Builder setTimeWeight(float f) {
                this.bitField0_ |= 4;
                this.timeWeight_ = f;
                return this;
            }

            public Builder clearTimeWeight() {
                this.bitField0_ &= -5;
                this.timeWeight_ = 0.0f;
                return this;
            }

            public boolean hasAge() {
                return (this.bitField0_ & 8) == 8;
            }

            public long getAge() {
                return this.age_;
            }

            public Builder setAge(long j) {
                this.bitField0_ |= 8;
                this.age_ = j;
                return this;
            }

            public Builder clearAge() {
                this.bitField0_ &= -9;
                this.age_ = 0;
                return this;
            }
        }

        private WeightedVote(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private WeightedVote(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static WeightedVote getDefaultInstance() {
            return defaultInstance;
        }

        public WeightedVote getDefaultInstanceForType() {
            return defaultInstance;
        }

        private WeightedVote(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                                builder = this.vote_.toBuilder();
                            }
                            this.vote_ = (Vote) codedInputStream.readMessage(Vote.PARSER, extensionRegistryLite);
                            if (builder != null) {
                                builder.mergeFrom(this.vote_);
                                this.vote_ = builder.buildPartial();
                            }
                            this.bitField0_ |= 1;
                            obj2 = obj;
                            break;
                        case 16:
                            this.bitField0_ |= 2;
                            this.userWeight_ = codedInputStream.readInt32();
                            obj2 = obj;
                            break;
                        case 29:
                            this.bitField0_ |= 4;
                            this.timeWeight_ = codedInputStream.readFloat();
                            obj2 = obj;
                            break;
                        case 32:
                            this.bitField0_ |= 8;
                            this.age_ = codedInputStream.readInt64();
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

        public Parser<WeightedVote> getParserForType() {
            return PARSER;
        }

        public boolean hasVote() {
            return (this.bitField0_ & 1) == 1;
        }

        public Vote getVote() {
            return this.vote_;
        }

        public boolean hasUserWeight() {
            return (this.bitField0_ & 2) == 2;
        }

        public int getUserWeight() {
            return this.userWeight_;
        }

        public boolean hasTimeWeight() {
            return (this.bitField0_ & 4) == 4;
        }

        public float getTimeWeight() {
            return this.timeWeight_;
        }

        public boolean hasAge() {
            return (this.bitField0_ & 8) == 8;
        }

        public long getAge() {
            return this.age_;
        }

        private void initFields() {
            this.vote_ = Vote.getDefaultInstance();
            this.userWeight_ = 0;
            this.timeWeight_ = 0.0f;
            this.age_ = 0;
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
                codedOutputStream.writeMessage(1, this.vote_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeInt32(2, this.userWeight_);
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeFloat(3, this.timeWeight_);
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeInt64(4, this.age_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.vote_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeInt32Size(2, this.userWeight_);
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeFloatSize(3, this.timeWeight_);
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeInt64Size(4, this.age_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static WeightedVote parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (WeightedVote) PARSER.parseFrom(byteString);
        }

        public static WeightedVote parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (WeightedVote) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static WeightedVote parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (WeightedVote) PARSER.parseFrom(bArr);
        }

        public static WeightedVote parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (WeightedVote) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static WeightedVote parseFrom(InputStream inputStream) throws IOException {
            return (WeightedVote) PARSER.parseFrom(inputStream);
        }

        public static WeightedVote parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (WeightedVote) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static WeightedVote parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (WeightedVote) PARSER.parseDelimitedFrom(inputStream);
        }

        public static WeightedVote parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (WeightedVote) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static WeightedVote parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (WeightedVote) PARSER.parseFrom(codedInputStream);
        }

        public static WeightedVote parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (WeightedVote) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(WeightedVote weightedVote) {
            return newBuilder().mergeFrom(weightedVote);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    private Votes() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite extensionRegistryLite) {
    }
}
