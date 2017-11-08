package com.avast.cloud.webrep.proto;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WhiteListProto {

    public interface WhiteListRecordOrBuilder extends MessageLiteOrBuilder {
        String getDomain();

        ByteString getDomainBytes();

        String getSite();

        ByteString getSiteBytes();

        boolean hasDomain();

        boolean hasSite();
    }

    public static final class WhiteListRecord extends GeneratedMessageLite implements WhiteListRecordOrBuilder {
        public static final int DOMAIN_FIELD_NUMBER = 1;
        public static Parser<WhiteListRecord> PARSER = new AbstractParser<WhiteListRecord>() {
            public WhiteListRecord parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new WhiteListRecord(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int SITE_FIELD_NUMBER = 2;
        private static final WhiteListRecord defaultInstance = new WhiteListRecord(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private Object domain_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object site_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<WhiteListRecord, Builder> implements WhiteListRecordOrBuilder {
            private int bitField0_;
            private Object domain_ = "";
            private Object site_ = "";

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
                this.domain_ = "";
                this.bitField0_ &= -2;
                this.site_ = "";
                this.bitField0_ &= -3;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public WhiteListRecord getDefaultInstanceForType() {
                return WhiteListRecord.getDefaultInstance();
            }

            public WhiteListRecord build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public WhiteListRecord buildPartial() {
                WhiteListRecord whiteListRecord = new WhiteListRecord((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                whiteListRecord.domain_ = this.domain_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                whiteListRecord.site_ = this.site_;
                whiteListRecord.bitField0_ = i2;
                return whiteListRecord;
            }

            public Builder mergeFrom(WhiteListRecord whiteListRecord) {
                if (whiteListRecord == WhiteListRecord.getDefaultInstance()) {
                    return this;
                }
                if (whiteListRecord.hasDomain()) {
                    this.bitField0_ |= 1;
                    this.domain_ = whiteListRecord.domain_;
                }
                if (whiteListRecord.hasSite()) {
                    this.bitField0_ |= 2;
                    this.site_ = whiteListRecord.site_;
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                WhiteListRecord whiteListRecord;
                WhiteListRecord whiteListRecord2;
                try {
                    whiteListRecord2 = (WhiteListRecord) WhiteListRecord.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (whiteListRecord2 != null) {
                        mergeFrom(whiteListRecord2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    whiteListRecord2 = (WhiteListRecord) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    whiteListRecord = whiteListRecord2;
                    th = th3;
                }
                if (whiteListRecord != null) {
                    mergeFrom(whiteListRecord);
                }
                throw th;
            }

            public boolean hasDomain() {
                return (this.bitField0_ & 1) == 1;
            }

            public String getDomain() {
                Object obj = this.domain_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.domain_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getDomainBytes() {
                Object obj = this.domain_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.domain_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setDomain(String str) {
                if (str != null) {
                    this.bitField0_ |= 1;
                    this.domain_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearDomain() {
                this.bitField0_ &= -2;
                this.domain_ = WhiteListRecord.getDefaultInstance().getDomain();
                return this;
            }

            public Builder setDomainBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.domain_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasSite() {
                return (this.bitField0_ & 2) == 2;
            }

            public String getSite() {
                Object obj = this.site_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.site_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getSiteBytes() {
                Object obj = this.site_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.site_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setSite(String str) {
                if (str != null) {
                    this.bitField0_ |= 2;
                    this.site_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearSite() {
                this.bitField0_ &= -3;
                this.site_ = WhiteListRecord.getDefaultInstance().getSite();
                return this;
            }

            public Builder setSiteBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.site_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }
        }

        private WhiteListRecord(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private WhiteListRecord(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static WhiteListRecord getDefaultInstance() {
            return defaultInstance;
        }

        public WhiteListRecord getDefaultInstanceForType() {
            return defaultInstance;
        }

        private WhiteListRecord(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            this.domain_ = codedInputStream.readBytes();
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.site_ = codedInputStream.readBytes();
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

        public Parser<WhiteListRecord> getParserForType() {
            return PARSER;
        }

        public boolean hasDomain() {
            return (this.bitField0_ & 1) == 1;
        }

        public String getDomain() {
            Object obj = this.domain_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.domain_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getDomainBytes() {
            Object obj = this.domain_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.domain_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasSite() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getSite() {
            Object obj = this.site_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.site_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getSiteBytes() {
            Object obj = this.site_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.site_ = copyFromUtf8;
            return copyFromUtf8;
        }

        private void initFields() {
            this.domain_ = "";
            this.site_ = "";
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
                codedOutputStream.writeBytes(1, getDomainBytes());
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, getSiteBytes());
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, getDomainBytes()) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, getSiteBytes());
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static WhiteListRecord parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (WhiteListRecord) PARSER.parseFrom(byteString);
        }

        public static WhiteListRecord parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (WhiteListRecord) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static WhiteListRecord parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (WhiteListRecord) PARSER.parseFrom(bArr);
        }

        public static WhiteListRecord parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (WhiteListRecord) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static WhiteListRecord parseFrom(InputStream inputStream) throws IOException {
            return (WhiteListRecord) PARSER.parseFrom(inputStream);
        }

        public static WhiteListRecord parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (WhiteListRecord) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static WhiteListRecord parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (WhiteListRecord) PARSER.parseDelimitedFrom(inputStream);
        }

        public static WhiteListRecord parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (WhiteListRecord) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static WhiteListRecord parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (WhiteListRecord) PARSER.parseFrom(codedInputStream);
        }

        public static WhiteListRecord parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (WhiteListRecord) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(WhiteListRecord whiteListRecord) {
            return newBuilder().mergeFrom(whiteListRecord);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface WhitelistedOrBuilder extends MessageLiteOrBuilder {
        WhiteListRecord getRecords(int i);

        int getRecordsCount();

        List<WhiteListRecord> getRecordsList();
    }

    public static final class Whitelisted extends GeneratedMessageLite implements WhitelistedOrBuilder {
        public static Parser<Whitelisted> PARSER = new AbstractParser<Whitelisted>() {
            public Whitelisted parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new Whitelisted(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int RECORDS_FIELD_NUMBER = 1;
        private static final Whitelisted defaultInstance = new Whitelisted(true);
        private static final long serialVersionUID = 0;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private List<WhiteListRecord> records_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<Whitelisted, Builder> implements WhitelistedOrBuilder {
            private int bitField0_;
            private List<WhiteListRecord> records_ = Collections.emptyList();

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
                this.records_ = Collections.emptyList();
                this.bitField0_ &= -2;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public Whitelisted getDefaultInstanceForType() {
                return Whitelisted.getDefaultInstance();
            }

            public Whitelisted build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public Whitelisted buildPartial() {
                Whitelisted whitelisted = new Whitelisted((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                if ((this.bitField0_ & 1) == 1) {
                    this.records_ = Collections.unmodifiableList(this.records_);
                    this.bitField0_ &= -2;
                }
                whitelisted.records_ = this.records_;
                return whitelisted;
            }

            public Builder mergeFrom(Whitelisted whitelisted) {
                if (!(whitelisted == Whitelisted.getDefaultInstance() || whitelisted.records_.isEmpty())) {
                    if (this.records_.isEmpty()) {
                        this.records_ = whitelisted.records_;
                        this.bitField0_ &= -2;
                    } else {
                        ensureRecordsIsMutable();
                        this.records_.addAll(whitelisted.records_);
                    }
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                Whitelisted whitelisted;
                Whitelisted whitelisted2;
                try {
                    whitelisted2 = (Whitelisted) Whitelisted.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (whitelisted2 != null) {
                        mergeFrom(whitelisted2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    whitelisted2 = (Whitelisted) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    whitelisted = whitelisted2;
                    th = th3;
                }
                if (whitelisted != null) {
                    mergeFrom(whitelisted);
                }
                throw th;
            }

            private void ensureRecordsIsMutable() {
                if ((this.bitField0_ & 1) != 1) {
                    this.records_ = new ArrayList(this.records_);
                    this.bitField0_ |= 1;
                }
            }

            public List<WhiteListRecord> getRecordsList() {
                return Collections.unmodifiableList(this.records_);
            }

            public int getRecordsCount() {
                return this.records_.size();
            }

            public WhiteListRecord getRecords(int i) {
                return (WhiteListRecord) this.records_.get(i);
            }

            public Builder setRecords(int i, WhiteListRecord whiteListRecord) {
                if (whiteListRecord != null) {
                    ensureRecordsIsMutable();
                    this.records_.set(i, whiteListRecord);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setRecords(int i, Builder builder) {
                ensureRecordsIsMutable();
                this.records_.set(i, builder.build());
                return this;
            }

            public Builder addRecords(WhiteListRecord whiteListRecord) {
                if (whiteListRecord != null) {
                    ensureRecordsIsMutable();
                    this.records_.add(whiteListRecord);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addRecords(int i, WhiteListRecord whiteListRecord) {
                if (whiteListRecord != null) {
                    ensureRecordsIsMutable();
                    this.records_.add(i, whiteListRecord);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addRecords(Builder builder) {
                ensureRecordsIsMutable();
                this.records_.add(builder.build());
                return this;
            }

            public Builder addRecords(int i, Builder builder) {
                ensureRecordsIsMutable();
                this.records_.add(i, builder.build());
                return this;
            }

            public Builder addAllRecords(Iterable<? extends WhiteListRecord> iterable) {
                ensureRecordsIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(iterable, this.records_);
                return this;
            }

            public Builder clearRecords() {
                this.records_ = Collections.emptyList();
                this.bitField0_ &= -2;
                return this;
            }

            public Builder removeRecords(int i) {
                ensureRecordsIsMutable();
                this.records_.remove(i);
                return this;
            }
        }

        private Whitelisted(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private Whitelisted(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static Whitelisted getDefaultInstance() {
            return defaultInstance;
        }

        public Whitelisted getDefaultInstanceForType() {
            return defaultInstance;
        }

        private Whitelisted(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
            initFields();
            int i = 0;
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 10:
                            if ((i & 1) != 1) {
                                this.records_ = new ArrayList();
                                i |= 1;
                            }
                            this.records_.add(codedInputStream.readMessage(WhiteListRecord.PARSER, extensionRegistryLite));
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
                    if ((i & 1) == 1) {
                        this.records_ = Collections.unmodifiableList(this.records_);
                    }
                    makeExtensionsImmutable();
                }
            }
            if ((i & 1) == 1) {
                this.records_ = Collections.unmodifiableList(this.records_);
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<Whitelisted> getParserForType() {
            return PARSER;
        }

        public List<WhiteListRecord> getRecordsList() {
            return this.records_;
        }

        public List<? extends WhiteListRecordOrBuilder> getRecordsOrBuilderList() {
            return this.records_;
        }

        public int getRecordsCount() {
            return this.records_.size();
        }

        public WhiteListRecord getRecords(int i) {
            return (WhiteListRecord) this.records_.get(i);
        }

        public WhiteListRecordOrBuilder getRecordsOrBuilder(int i) {
            return (WhiteListRecordOrBuilder) this.records_.get(i);
        }

        private void initFields() {
            this.records_ = Collections.emptyList();
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
            for (int i = 0; i < this.records_.size(); i++) {
                codedOutputStream.writeMessage(1, (MessageLite) this.records_.get(i));
            }
        }

        public int getSerializedSize() {
            int i = this.memoizedSerializedSize;
            if (i != -1) {
                return i;
            }
            int i2 = 0;
            for (i = 0; i < this.records_.size(); i++) {
                i2 += CodedOutputStream.computeMessageSize(1, (MessageLite) this.records_.get(i));
            }
            this.memoizedSerializedSize = i2;
            return i2;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static Whitelisted parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (Whitelisted) PARSER.parseFrom(byteString);
        }

        public static Whitelisted parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Whitelisted) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static Whitelisted parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (Whitelisted) PARSER.parseFrom(bArr);
        }

        public static Whitelisted parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Whitelisted) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static Whitelisted parseFrom(InputStream inputStream) throws IOException {
            return (Whitelisted) PARSER.parseFrom(inputStream);
        }

        public static Whitelisted parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Whitelisted) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static Whitelisted parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (Whitelisted) PARSER.parseDelimitedFrom(inputStream);
        }

        public static Whitelisted parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Whitelisted) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static Whitelisted parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (Whitelisted) PARSER.parseFrom(codedInputStream);
        }

        public static Whitelisted parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Whitelisted) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(Whitelisted whitelisted) {
            return newBuilder().mergeFrom(whitelisted);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    private WhiteListProto() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite extensionRegistryLite) {
    }
}
