package com.avast.cloud.webrep.proto;

import android.support.v4.view.MotionEventCompat;
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

public final class PhishingProto {

    public interface AndroidWebRequestOrBuilder extends MessageLiteOrBuilder {
        String getLocale();

        ByteString getLocaleBytes();

        ByteString getUri();

        boolean hasLocale();

        boolean hasUri();
    }

    public static final class AndroidWebRequest extends GeneratedMessageLite implements AndroidWebRequestOrBuilder {
        public static final int LOCALE_FIELD_NUMBER = 2;
        public static Parser<AndroidWebRequest> PARSER = new AbstractParser<AndroidWebRequest>() {
            public AndroidWebRequest parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new AndroidWebRequest(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int URI_FIELD_NUMBER = 1;
        private static final AndroidWebRequest defaultInstance = new AndroidWebRequest(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private Object locale_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private ByteString uri_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<AndroidWebRequest, Builder> implements AndroidWebRequestOrBuilder {
            private int bitField0_;
            private Object locale_ = "";
            private ByteString uri_ = ByteString.EMPTY;

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
                this.uri_ = ByteString.EMPTY;
                this.bitField0_ &= -2;
                this.locale_ = "";
                this.bitField0_ &= -3;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public AndroidWebRequest getDefaultInstanceForType() {
                return AndroidWebRequest.getDefaultInstance();
            }

            public AndroidWebRequest build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public AndroidWebRequest buildPartial() {
                AndroidWebRequest androidWebRequest = new AndroidWebRequest((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                androidWebRequest.uri_ = this.uri_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                androidWebRequest.locale_ = this.locale_;
                androidWebRequest.bitField0_ = i2;
                return androidWebRequest;
            }

            public Builder mergeFrom(AndroidWebRequest androidWebRequest) {
                if (androidWebRequest == AndroidWebRequest.getDefaultInstance()) {
                    return this;
                }
                if (androidWebRequest.hasUri()) {
                    setUri(androidWebRequest.getUri());
                }
                if (androidWebRequest.hasLocale()) {
                    this.bitField0_ |= 2;
                    this.locale_ = androidWebRequest.locale_;
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                AndroidWebRequest androidWebRequest;
                AndroidWebRequest androidWebRequest2;
                try {
                    androidWebRequest2 = (AndroidWebRequest) AndroidWebRequest.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (androidWebRequest2 != null) {
                        mergeFrom(androidWebRequest2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    androidWebRequest2 = (AndroidWebRequest) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    androidWebRequest = androidWebRequest2;
                    th = th3;
                }
                if (androidWebRequest != null) {
                    mergeFrom(androidWebRequest);
                }
                throw th;
            }

            public boolean hasUri() {
                return (this.bitField0_ & 1) == 1;
            }

            public ByteString getUri() {
                return this.uri_;
            }

            public Builder setUri(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.uri_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearUri() {
                this.bitField0_ &= -2;
                this.uri_ = AndroidWebRequest.getDefaultInstance().getUri();
                return this;
            }

            public boolean hasLocale() {
                return (this.bitField0_ & 2) == 2;
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
                    this.bitField0_ |= 2;
                    this.locale_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearLocale() {
                this.bitField0_ &= -3;
                this.locale_ = AndroidWebRequest.getDefaultInstance().getLocale();
                return this;
            }

            public Builder setLocaleBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.locale_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }
        }

        private AndroidWebRequest(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private AndroidWebRequest(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static AndroidWebRequest getDefaultInstance() {
            return defaultInstance;
        }

        public AndroidWebRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        private AndroidWebRequest(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            this.uri_ = codedInputStream.readBytes();
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.locale_ = codedInputStream.readBytes();
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

        public Parser<AndroidWebRequest> getParserForType() {
            return PARSER;
        }

        public boolean hasUri() {
            return (this.bitField0_ & 1) == 1;
        }

        public ByteString getUri() {
            return this.uri_;
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

        private void initFields() {
            this.uri_ = ByteString.EMPTY;
            this.locale_ = "";
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
                codedOutputStream.writeBytes(1, this.uri_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, getLocaleBytes());
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, this.uri_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, getLocaleBytes());
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static AndroidWebRequest parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (AndroidWebRequest) PARSER.parseFrom(byteString);
        }

        public static AndroidWebRequest parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (AndroidWebRequest) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static AndroidWebRequest parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (AndroidWebRequest) PARSER.parseFrom(bArr);
        }

        public static AndroidWebRequest parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (AndroidWebRequest) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static AndroidWebRequest parseFrom(InputStream inputStream) throws IOException {
            return (AndroidWebRequest) PARSER.parseFrom(inputStream);
        }

        public static AndroidWebRequest parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (AndroidWebRequest) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static AndroidWebRequest parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (AndroidWebRequest) PARSER.parseDelimitedFrom(inputStream);
        }

        public static AndroidWebRequest parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (AndroidWebRequest) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static AndroidWebRequest parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (AndroidWebRequest) PARSER.parseFrom(codedInputStream);
        }

        public static AndroidWebRequest parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (AndroidWebRequest) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(AndroidWebRequest androidWebRequest) {
            return newBuilder().mergeFrom(androidWebRequest);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface AndroidWebResponseOrBuilder extends MessageLiteOrBuilder {
        int getPhishing();

        int getPhishingDomain();

        int getTtl();

        int getTyposquatting();

        String getTyposquattingBrandDomain();

        ByteString getTyposquattingBrandDomainBytes();

        String getTyposquattingForwardUrl();

        ByteString getTyposquattingForwardUrlBytes();

        boolean hasPhishing();

        boolean hasPhishingDomain();

        boolean hasTtl();

        boolean hasTyposquatting();

        boolean hasTyposquattingBrandDomain();

        boolean hasTyposquattingForwardUrl();
    }

    public static final class AndroidWebResponse extends GeneratedMessageLite implements AndroidWebResponseOrBuilder {
        public static Parser<AndroidWebResponse> PARSER = new AbstractParser<AndroidWebResponse>() {
            public AndroidWebResponse parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new AndroidWebResponse(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int PHISHINGDOMAIN_FIELD_NUMBER = 2;
        public static final int PHISHING_FIELD_NUMBER = 1;
        public static final int TTL_FIELD_NUMBER = 3;
        public static final int TYPOSQUATTINGBRANDDOMAIN_FIELD_NUMBER = 6;
        public static final int TYPOSQUATTINGFORWARDURL_FIELD_NUMBER = 5;
        public static final int TYPOSQUATTING_FIELD_NUMBER = 4;
        private static final AndroidWebResponse defaultInstance = new AndroidWebResponse(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private int phishingDomain_;
        private int phishing_;
        private int ttl_;
        private Object typosquattingBrandDomain_;
        private Object typosquattingForwardUrl_;
        private int typosquatting_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<AndroidWebResponse, Builder> implements AndroidWebResponseOrBuilder {
            private int bitField0_;
            private int phishingDomain_;
            private int phishing_;
            private int ttl_;
            private Object typosquattingBrandDomain_ = "";
            private Object typosquattingForwardUrl_ = "";
            private int typosquatting_;

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
                this.typosquatting_ = 0;
                this.bitField0_ &= -9;
                this.typosquattingForwardUrl_ = "";
                this.bitField0_ &= -17;
                this.typosquattingBrandDomain_ = "";
                this.bitField0_ &= -33;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public AndroidWebResponse getDefaultInstanceForType() {
                return AndroidWebResponse.getDefaultInstance();
            }

            public AndroidWebResponse build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public AndroidWebResponse buildPartial() {
                AndroidWebResponse androidWebResponse = new AndroidWebResponse((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                androidWebResponse.phishing_ = this.phishing_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                androidWebResponse.phishingDomain_ = this.phishingDomain_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                androidWebResponse.ttl_ = this.ttl_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                androidWebResponse.typosquatting_ = this.typosquatting_;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                androidWebResponse.typosquattingForwardUrl_ = this.typosquattingForwardUrl_;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                androidWebResponse.typosquattingBrandDomain_ = this.typosquattingBrandDomain_;
                androidWebResponse.bitField0_ = i2;
                return androidWebResponse;
            }

            public Builder mergeFrom(AndroidWebResponse androidWebResponse) {
                if (androidWebResponse == AndroidWebResponse.getDefaultInstance()) {
                    return this;
                }
                if (androidWebResponse.hasPhishing()) {
                    setPhishing(androidWebResponse.getPhishing());
                }
                if (androidWebResponse.hasPhishingDomain()) {
                    setPhishingDomain(androidWebResponse.getPhishingDomain());
                }
                if (androidWebResponse.hasTtl()) {
                    setTtl(androidWebResponse.getTtl());
                }
                if (androidWebResponse.hasTyposquatting()) {
                    setTyposquatting(androidWebResponse.getTyposquatting());
                }
                if (androidWebResponse.hasTyposquattingForwardUrl()) {
                    this.bitField0_ |= 16;
                    this.typosquattingForwardUrl_ = androidWebResponse.typosquattingForwardUrl_;
                }
                if (androidWebResponse.hasTyposquattingBrandDomain()) {
                    this.bitField0_ |= 32;
                    this.typosquattingBrandDomain_ = androidWebResponse.typosquattingBrandDomain_;
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                AndroidWebResponse androidWebResponse;
                Throwable th;
                AndroidWebResponse androidWebResponse2;
                try {
                    androidWebResponse = (AndroidWebResponse) AndroidWebResponse.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (androidWebResponse != null) {
                        mergeFrom(androidWebResponse);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    androidWebResponse = (AndroidWebResponse) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    androidWebResponse2 = androidWebResponse;
                    th = th3;
                }
                if (androidWebResponse2 != null) {
                    mergeFrom(androidWebResponse2);
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

            public boolean hasTyposquatting() {
                return (this.bitField0_ & 8) == 8;
            }

            public int getTyposquatting() {
                return this.typosquatting_;
            }

            public Builder setTyposquatting(int i) {
                this.bitField0_ |= 8;
                this.typosquatting_ = i;
                return this;
            }

            public Builder clearTyposquatting() {
                this.bitField0_ &= -9;
                this.typosquatting_ = 0;
                return this;
            }

            public boolean hasTyposquattingForwardUrl() {
                return (this.bitField0_ & 16) == 16;
            }

            public String getTyposquattingForwardUrl() {
                Object obj = this.typosquattingForwardUrl_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.typosquattingForwardUrl_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getTyposquattingForwardUrlBytes() {
                Object obj = this.typosquattingForwardUrl_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.typosquattingForwardUrl_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setTyposquattingForwardUrl(String str) {
                if (str != null) {
                    this.bitField0_ |= 16;
                    this.typosquattingForwardUrl_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearTyposquattingForwardUrl() {
                this.bitField0_ &= -17;
                this.typosquattingForwardUrl_ = AndroidWebResponse.getDefaultInstance().getTyposquattingForwardUrl();
                return this;
            }

            public Builder setTyposquattingForwardUrlBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 16;
                    this.typosquattingForwardUrl_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasTyposquattingBrandDomain() {
                return (this.bitField0_ & 32) == 32;
            }

            public String getTyposquattingBrandDomain() {
                Object obj = this.typosquattingBrandDomain_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.typosquattingBrandDomain_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getTyposquattingBrandDomainBytes() {
                Object obj = this.typosquattingBrandDomain_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.typosquattingBrandDomain_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setTyposquattingBrandDomain(String str) {
                if (str != null) {
                    this.bitField0_ |= 32;
                    this.typosquattingBrandDomain_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearTyposquattingBrandDomain() {
                this.bitField0_ &= -33;
                this.typosquattingBrandDomain_ = AndroidWebResponse.getDefaultInstance().getTyposquattingBrandDomain();
                return this;
            }

            public Builder setTyposquattingBrandDomainBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 32;
                    this.typosquattingBrandDomain_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }
        }

        private AndroidWebResponse(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private AndroidWebResponse(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static AndroidWebResponse getDefaultInstance() {
            return defaultInstance;
        }

        public AndroidWebResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        private AndroidWebResponse(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                        case 32:
                            this.bitField0_ |= 8;
                            this.typosquatting_ = codedInputStream.readSInt32();
                            break;
                        case MotionEventCompat.AXIS_GENERIC_11 /*42*/:
                            this.bitField0_ |= 16;
                            this.typosquattingForwardUrl_ = codedInputStream.readBytes();
                            break;
                        case 50:
                            this.bitField0_ |= 32;
                            this.typosquattingBrandDomain_ = codedInputStream.readBytes();
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

        public Parser<AndroidWebResponse> getParserForType() {
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

        public boolean hasTyposquatting() {
            return (this.bitField0_ & 8) == 8;
        }

        public int getTyposquatting() {
            return this.typosquatting_;
        }

        public boolean hasTyposquattingForwardUrl() {
            return (this.bitField0_ & 16) == 16;
        }

        public String getTyposquattingForwardUrl() {
            Object obj = this.typosquattingForwardUrl_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.typosquattingForwardUrl_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getTyposquattingForwardUrlBytes() {
            Object obj = this.typosquattingForwardUrl_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.typosquattingForwardUrl_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasTyposquattingBrandDomain() {
            return (this.bitField0_ & 32) == 32;
        }

        public String getTyposquattingBrandDomain() {
            Object obj = this.typosquattingBrandDomain_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.typosquattingBrandDomain_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getTyposquattingBrandDomainBytes() {
            Object obj = this.typosquattingBrandDomain_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.typosquattingBrandDomain_ = copyFromUtf8;
            return copyFromUtf8;
        }

        private void initFields() {
            this.phishing_ = 0;
            this.phishingDomain_ = 0;
            this.ttl_ = 0;
            this.typosquatting_ = 0;
            this.typosquattingForwardUrl_ = "";
            this.typosquattingBrandDomain_ = "";
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
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeSInt32(4, this.typosquatting_);
            }
            if ((this.bitField0_ & 16) == 16) {
                codedOutputStream.writeBytes(5, getTyposquattingForwardUrlBytes());
            }
            if ((this.bitField0_ & 32) == 32) {
                codedOutputStream.writeBytes(6, getTyposquattingBrandDomainBytes());
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
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeSInt32Size(4, this.typosquatting_);
            }
            if ((this.bitField0_ & 16) == 16) {
                i += CodedOutputStream.computeBytesSize(5, getTyposquattingForwardUrlBytes());
            }
            if ((this.bitField0_ & 32) == 32) {
                i += CodedOutputStream.computeBytesSize(6, getTyposquattingBrandDomainBytes());
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static AndroidWebResponse parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (AndroidWebResponse) PARSER.parseFrom(byteString);
        }

        public static AndroidWebResponse parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (AndroidWebResponse) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static AndroidWebResponse parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (AndroidWebResponse) PARSER.parseFrom(bArr);
        }

        public static AndroidWebResponse parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (AndroidWebResponse) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static AndroidWebResponse parseFrom(InputStream inputStream) throws IOException {
            return (AndroidWebResponse) PARSER.parseFrom(inputStream);
        }

        public static AndroidWebResponse parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (AndroidWebResponse) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static AndroidWebResponse parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (AndroidWebResponse) PARSER.parseDelimitedFrom(inputStream);
        }

        public static AndroidWebResponse parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (AndroidWebResponse) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static AndroidWebResponse parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (AndroidWebResponse) PARSER.parseFrom(codedInputStream);
        }

        public static AndroidWebResponse parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (AndroidWebResponse) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(AndroidWebResponse androidWebResponse) {
            return newBuilder().mergeFrom(androidWebResponse);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    private PhishingProto() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite extensionRegistryLite) {
    }
}
