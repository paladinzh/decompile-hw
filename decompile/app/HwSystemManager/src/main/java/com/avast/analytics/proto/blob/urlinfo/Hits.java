package com.avast.analytics.proto.blob.urlinfo;

import com.google.protobuf.AbstractParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.Internal.EnumLite;
import com.google.protobuf.Internal.EnumLiteMap;
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

public final class Hits {

    public interface HitMessageBlobOrBuilder extends MessageLiteOrBuilder {
        long getCallerId();

        HitSource getHitSource(int i);

        int getHitSourceCount();

        List<HitSource> getHitSourceList();

        SendFromService getSendFromService();

        String getUrl();

        ByteString getUrlBytes();

        boolean hasCallerId();

        boolean hasSendFromService();

        boolean hasUrl();
    }

    public static final class HitMessageBlob extends GeneratedMessageLite implements HitMessageBlobOrBuilder {
        public static final int CALLERID_FIELD_NUMBER = 2;
        public static final int HITSOURCE_FIELD_NUMBER = 3;
        public static Parser<HitMessageBlob> PARSER = new AbstractParser<HitMessageBlob>() {
            public HitMessageBlob parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new HitMessageBlob(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int SENDFROMSERVICE_FIELD_NUMBER = 4;
        public static final int URL_FIELD_NUMBER = 1;
        private static final HitMessageBlob defaultInstance = new HitMessageBlob(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private long callerId_;
        private List<HitSource> hitSource_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private SendFromService sendFromService_;
        private Object url_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<HitMessageBlob, Builder> implements HitMessageBlobOrBuilder {
            private int bitField0_;
            private long callerId_;
            private List<HitSource> hitSource_ = Collections.emptyList();
            private SendFromService sendFromService_ = SendFromService.URLINFO;
            private Object url_ = "";

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
                this.url_ = "";
                this.bitField0_ &= -2;
                this.callerId_ = 0;
                this.bitField0_ &= -3;
                this.hitSource_ = Collections.emptyList();
                this.bitField0_ &= -5;
                this.sendFromService_ = SendFromService.URLINFO;
                this.bitField0_ &= -9;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public HitMessageBlob getDefaultInstanceForType() {
                return HitMessageBlob.getDefaultInstance();
            }

            public HitMessageBlob build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public HitMessageBlob buildPartial() {
                HitMessageBlob hitMessageBlob = new HitMessageBlob((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                hitMessageBlob.url_ = this.url_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                hitMessageBlob.callerId_ = this.callerId_;
                if ((this.bitField0_ & 4) == 4) {
                    this.hitSource_ = Collections.unmodifiableList(this.hitSource_);
                    this.bitField0_ &= -5;
                }
                hitMessageBlob.hitSource_ = this.hitSource_;
                if ((i & 8) == 8) {
                    i2 |= 4;
                }
                hitMessageBlob.sendFromService_ = this.sendFromService_;
                hitMessageBlob.bitField0_ = i2;
                return hitMessageBlob;
            }

            public Builder mergeFrom(HitMessageBlob hitMessageBlob) {
                if (hitMessageBlob == HitMessageBlob.getDefaultInstance()) {
                    return this;
                }
                if (hitMessageBlob.hasUrl()) {
                    this.bitField0_ |= 1;
                    this.url_ = hitMessageBlob.url_;
                }
                if (hitMessageBlob.hasCallerId()) {
                    setCallerId(hitMessageBlob.getCallerId());
                }
                if (!hitMessageBlob.hitSource_.isEmpty()) {
                    if (this.hitSource_.isEmpty()) {
                        this.hitSource_ = hitMessageBlob.hitSource_;
                        this.bitField0_ &= -5;
                    } else {
                        ensureHitSourceIsMutable();
                        this.hitSource_.addAll(hitMessageBlob.hitSource_);
                    }
                }
                if (hitMessageBlob.hasSendFromService()) {
                    setSendFromService(hitMessageBlob.getSendFromService());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                HitMessageBlob hitMessageBlob;
                Throwable th;
                HitMessageBlob hitMessageBlob2;
                try {
                    hitMessageBlob = (HitMessageBlob) HitMessageBlob.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (hitMessageBlob != null) {
                        mergeFrom(hitMessageBlob);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    hitMessageBlob = (HitMessageBlob) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    hitMessageBlob2 = hitMessageBlob;
                    th = th3;
                }
                if (hitMessageBlob2 != null) {
                    mergeFrom(hitMessageBlob2);
                }
                throw th;
            }

            public boolean hasUrl() {
                return (this.bitField0_ & 1) == 1;
            }

            public String getUrl() {
                Object obj = this.url_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.url_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getUrlBytes() {
                Object obj = this.url_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.url_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setUrl(String str) {
                if (str != null) {
                    this.bitField0_ |= 1;
                    this.url_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearUrl() {
                this.bitField0_ &= -2;
                this.url_ = HitMessageBlob.getDefaultInstance().getUrl();
                return this;
            }

            public Builder setUrlBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.url_ = byteString;
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

            private void ensureHitSourceIsMutable() {
                if ((this.bitField0_ & 4) != 4) {
                    this.hitSource_ = new ArrayList(this.hitSource_);
                    this.bitField0_ |= 4;
                }
            }

            public List<HitSource> getHitSourceList() {
                return Collections.unmodifiableList(this.hitSource_);
            }

            public int getHitSourceCount() {
                return this.hitSource_.size();
            }

            public HitSource getHitSource(int i) {
                return (HitSource) this.hitSource_.get(i);
            }

            public Builder setHitSource(int i, HitSource hitSource) {
                if (hitSource != null) {
                    ensureHitSourceIsMutable();
                    this.hitSource_.set(i, hitSource);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addHitSource(HitSource hitSource) {
                if (hitSource != null) {
                    ensureHitSourceIsMutable();
                    this.hitSource_.add(hitSource);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addAllHitSource(Iterable<? extends HitSource> iterable) {
                ensureHitSourceIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(iterable, this.hitSource_);
                return this;
            }

            public Builder clearHitSource() {
                this.hitSource_ = Collections.emptyList();
                this.bitField0_ &= -5;
                return this;
            }

            public boolean hasSendFromService() {
                return (this.bitField0_ & 8) == 8;
            }

            public SendFromService getSendFromService() {
                return this.sendFromService_;
            }

            public Builder setSendFromService(SendFromService sendFromService) {
                if (sendFromService != null) {
                    this.bitField0_ |= 8;
                    this.sendFromService_ = sendFromService;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearSendFromService() {
                this.bitField0_ &= -9;
                this.sendFromService_ = SendFromService.URLINFO;
                return this;
            }
        }

        private HitMessageBlob(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private HitMessageBlob(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static HitMessageBlob getDefaultInstance() {
            return defaultInstance;
        }

        public HitMessageBlob getDefaultInstanceForType() {
            return defaultInstance;
        }

        private HitMessageBlob(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            this.bitField0_ |= 1;
                            this.url_ = codedInputStream.readBytes();
                            break;
                        case 16:
                            this.bitField0_ |= 2;
                            this.callerId_ = codedInputStream.readSInt64();
                            break;
                        case 24:
                            HitSource valueOf = HitSource.valueOf(codedInputStream.readEnum());
                            if (valueOf != null) {
                                if ((i & 4) != 4) {
                                    this.hitSource_ = new ArrayList();
                                    i |= 4;
                                }
                                this.hitSource_.add(valueOf);
                                break;
                            }
                            break;
                        case 26:
                            readTag = codedInputStream.pushLimit(codedInputStream.readRawVarint32());
                            while (codedInputStream.getBytesUntilLimit() > 0) {
                                HitSource valueOf2 = HitSource.valueOf(codedInputStream.readEnum());
                                if (valueOf2 != null) {
                                    if ((i & 4) != 4) {
                                        this.hitSource_ = new ArrayList();
                                        i |= 4;
                                    }
                                    this.hitSource_.add(valueOf2);
                                }
                            }
                            codedInputStream.popLimit(readTag);
                            break;
                        case 32:
                            SendFromService valueOf3 = SendFromService.valueOf(codedInputStream.readEnum());
                            if (valueOf3 == null) {
                                break;
                            }
                            this.bitField0_ |= 4;
                            this.sendFromService_ = valueOf3;
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
                    if ((i & 4) == 4) {
                        this.hitSource_ = Collections.unmodifiableList(this.hitSource_);
                    }
                    makeExtensionsImmutable();
                }
            }
            if ((i & 4) == 4) {
                this.hitSource_ = Collections.unmodifiableList(this.hitSource_);
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<HitMessageBlob> getParserForType() {
            return PARSER;
        }

        public boolean hasUrl() {
            return (this.bitField0_ & 1) == 1;
        }

        public String getUrl() {
            Object obj = this.url_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.url_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getUrlBytes() {
            Object obj = this.url_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.url_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasCallerId() {
            return (this.bitField0_ & 2) == 2;
        }

        public long getCallerId() {
            return this.callerId_;
        }

        public List<HitSource> getHitSourceList() {
            return this.hitSource_;
        }

        public int getHitSourceCount() {
            return this.hitSource_.size();
        }

        public HitSource getHitSource(int i) {
            return (HitSource) this.hitSource_.get(i);
        }

        public boolean hasSendFromService() {
            return (this.bitField0_ & 4) == 4;
        }

        public SendFromService getSendFromService() {
            return this.sendFromService_;
        }

        private void initFields() {
            this.url_ = "";
            this.callerId_ = 0;
            this.hitSource_ = Collections.emptyList();
            this.sendFromService_ = SendFromService.URLINFO;
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
                codedOutputStream.writeBytes(1, getUrlBytes());
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeSInt64(2, this.callerId_);
            }
            for (int i = 0; i < this.hitSource_.size(); i++) {
                codedOutputStream.writeEnum(3, ((HitSource) this.hitSource_.get(i)).getNumber());
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeEnum(4, this.sendFromService_.getNumber());
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            int i3;
            if ((this.bitField0_ & 1) != 1) {
                i2 = 0;
            } else {
                i2 = CodedOutputStream.computeBytesSize(1, getUrlBytes()) + 0;
            }
            if ((this.bitField0_ & 2) != 2) {
                i3 = i2;
            } else {
                i3 = i2 + CodedOutputStream.computeSInt64Size(2, this.callerId_);
            }
            int i4 = 0;
            while (i < this.hitSource_.size()) {
                i++;
                i4 = CodedOutputStream.computeEnumSizeNoTag(((HitSource) this.hitSource_.get(i)).getNumber()) + i4;
            }
            i2 = (i3 + i4) + (this.hitSource_.size() * 1);
            if ((this.bitField0_ & 4) == 4) {
                i2 += CodedOutputStream.computeEnumSize(4, this.sendFromService_.getNumber());
            }
            this.memoizedSerializedSize = i2;
            return i2;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static HitMessageBlob parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (HitMessageBlob) PARSER.parseFrom(byteString);
        }

        public static HitMessageBlob parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (HitMessageBlob) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static HitMessageBlob parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (HitMessageBlob) PARSER.parseFrom(bArr);
        }

        public static HitMessageBlob parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (HitMessageBlob) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static HitMessageBlob parseFrom(InputStream inputStream) throws IOException {
            return (HitMessageBlob) PARSER.parseFrom(inputStream);
        }

        public static HitMessageBlob parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (HitMessageBlob) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static HitMessageBlob parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (HitMessageBlob) PARSER.parseDelimitedFrom(inputStream);
        }

        public static HitMessageBlob parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (HitMessageBlob) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static HitMessageBlob parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (HitMessageBlob) PARSER.parseFrom(codedInputStream);
        }

        public static HitMessageBlob parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (HitMessageBlob) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(HitMessageBlob hitMessageBlob) {
            return newBuilder().mergeFrom(hitMessageBlob);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public enum HitSource implements EnumLite {
        TYPO(0, 1),
        PHISHING(1, 10),
        PHISHING_MAILSHELL(2, 11),
        PHISHING_PHISHTANK(3, 12),
        PHISHING_PHISHTANK_RT(4, 13),
        PHISHING_GOOGLE(5, 14),
        PHISHING_APWG(6, 15),
        PHISHING_CYREN(7, 16),
        BLOCKER(8, 100),
        BLOCKER_VIRUSLAB(9, 101),
        BLOCKER_GOOGLE(10, 102);
        
        public static final int BLOCKER_GOOGLE_VALUE = 102;
        public static final int BLOCKER_VALUE = 100;
        public static final int BLOCKER_VIRUSLAB_VALUE = 101;
        public static final int PHISHING_APWG_VALUE = 15;
        public static final int PHISHING_CYREN_VALUE = 16;
        public static final int PHISHING_GOOGLE_VALUE = 14;
        public static final int PHISHING_MAILSHELL_VALUE = 11;
        public static final int PHISHING_PHISHTANK_RT_VALUE = 13;
        public static final int PHISHING_PHISHTANK_VALUE = 12;
        public static final int PHISHING_VALUE = 10;
        public static final int TYPO_VALUE = 1;
        private static EnumLiteMap<HitSource> internalValueMap;
        private final int value;

        static {
            internalValueMap = new EnumLiteMap<HitSource>() {
                public HitSource findValueByNumber(int i) {
                    return HitSource.valueOf(i);
                }
            };
        }

        public final int getNumber() {
            return this.value;
        }

        public static HitSource valueOf(int i) {
            switch (i) {
                case 1:
                    return TYPO;
                case 10:
                    return PHISHING;
                case 11:
                    return PHISHING_MAILSHELL;
                case 12:
                    return PHISHING_PHISHTANK;
                case 13:
                    return PHISHING_PHISHTANK_RT;
                case 14:
                    return PHISHING_GOOGLE;
                case 15:
                    return PHISHING_APWG;
                case 16:
                    return PHISHING_CYREN;
                case 100:
                    return BLOCKER;
                case 101:
                    return BLOCKER_VIRUSLAB;
                case 102:
                    return BLOCKER_GOOGLE;
                default:
                    return null;
            }
        }

        public static EnumLiteMap<HitSource> internalGetValueMap() {
            return internalValueMap;
        }

        private HitSource(int i, int i2) {
            this.value = i2;
        }
    }

    public enum SendFromService implements EnumLite {
        URLINFO(0, 1),
        SECUREME_PROXY(1, 2);
        
        public static final int SECUREME_PROXY_VALUE = 2;
        public static final int URLINFO_VALUE = 1;
        private static EnumLiteMap<SendFromService> internalValueMap;
        private final int value;

        static {
            internalValueMap = new EnumLiteMap<SendFromService>() {
                public SendFromService findValueByNumber(int i) {
                    return SendFromService.valueOf(i);
                }
            };
        }

        public final int getNumber() {
            return this.value;
        }

        public static SendFromService valueOf(int i) {
            switch (i) {
                case 1:
                    return URLINFO;
                case 2:
                    return SECUREME_PROXY;
                default:
                    return null;
            }
        }

        public static EnumLiteMap<SendFromService> internalGetValueMap() {
            return internalValueMap;
        }

        private SendFromService(int i, int i2) {
            this.value = i2;
        }
    }

    private Hits() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite extensionRegistryLite) {
    }
}
