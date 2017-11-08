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

public final class BrowserInfo {

    public interface PluginUpdateOrBuilder extends MessageLiteOrBuilder {
        String getPlyginType();

        ByteString getPlyginTypeBytes();

        String getReleaseNotes();

        ByteString getReleaseNotesBytes();

        String getVersion();

        ByteString getVersionBytes();

        boolean hasPlyginType();

        boolean hasReleaseNotes();

        boolean hasVersion();
    }

    public static final class PluginUpdate extends GeneratedMessageLite implements PluginUpdateOrBuilder {
        public static Parser<PluginUpdate> PARSER = new AbstractParser<PluginUpdate>() {
            public PluginUpdate parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new PluginUpdate(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int PLYGINTYPE_FIELD_NUMBER = 1;
        public static final int RELEASENOTES_FIELD_NUMBER = 3;
        public static final int VERSION_FIELD_NUMBER = 2;
        private static final PluginUpdate defaultInstance = new PluginUpdate(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object plyginType_;
        private Object releaseNotes_;
        private Object version_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<PluginUpdate, Builder> implements PluginUpdateOrBuilder {
            private int bitField0_;
            private Object plyginType_ = "";
            private Object releaseNotes_ = "";
            private Object version_ = "";

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
                this.plyginType_ = "";
                this.bitField0_ &= -2;
                this.version_ = "";
                this.bitField0_ &= -3;
                this.releaseNotes_ = "";
                this.bitField0_ &= -5;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public PluginUpdate getDefaultInstanceForType() {
                return PluginUpdate.getDefaultInstance();
            }

            public PluginUpdate build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public PluginUpdate buildPartial() {
                PluginUpdate pluginUpdate = new PluginUpdate((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                pluginUpdate.plyginType_ = this.plyginType_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                pluginUpdate.version_ = this.version_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                pluginUpdate.releaseNotes_ = this.releaseNotes_;
                pluginUpdate.bitField0_ = i2;
                return pluginUpdate;
            }

            public Builder mergeFrom(PluginUpdate pluginUpdate) {
                if (pluginUpdate == PluginUpdate.getDefaultInstance()) {
                    return this;
                }
                if (pluginUpdate.hasPlyginType()) {
                    this.bitField0_ |= 1;
                    this.plyginType_ = pluginUpdate.plyginType_;
                }
                if (pluginUpdate.hasVersion()) {
                    this.bitField0_ |= 2;
                    this.version_ = pluginUpdate.version_;
                }
                if (pluginUpdate.hasReleaseNotes()) {
                    this.bitField0_ |= 4;
                    this.releaseNotes_ = pluginUpdate.releaseNotes_;
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                PluginUpdate pluginUpdate;
                Throwable th;
                PluginUpdate pluginUpdate2;
                try {
                    pluginUpdate = (PluginUpdate) PluginUpdate.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (pluginUpdate != null) {
                        mergeFrom(pluginUpdate);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    pluginUpdate = (PluginUpdate) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    pluginUpdate2 = pluginUpdate;
                    th = th3;
                }
                if (pluginUpdate2 != null) {
                    mergeFrom(pluginUpdate2);
                }
                throw th;
            }

            public boolean hasPlyginType() {
                return (this.bitField0_ & 1) == 1;
            }

            public String getPlyginType() {
                Object obj = this.plyginType_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.plyginType_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getPlyginTypeBytes() {
                Object obj = this.plyginType_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.plyginType_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setPlyginType(String str) {
                if (str != null) {
                    this.bitField0_ |= 1;
                    this.plyginType_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearPlyginType() {
                this.bitField0_ &= -2;
                this.plyginType_ = PluginUpdate.getDefaultInstance().getPlyginType();
                return this;
            }

            public Builder setPlyginTypeBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.plyginType_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasVersion() {
                return (this.bitField0_ & 2) == 2;
            }

            public String getVersion() {
                Object obj = this.version_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.version_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getVersionBytes() {
                Object obj = this.version_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.version_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setVersion(String str) {
                if (str != null) {
                    this.bitField0_ |= 2;
                    this.version_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearVersion() {
                this.bitField0_ &= -3;
                this.version_ = PluginUpdate.getDefaultInstance().getVersion();
                return this;
            }

            public Builder setVersionBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.version_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasReleaseNotes() {
                return (this.bitField0_ & 4) == 4;
            }

            public String getReleaseNotes() {
                Object obj = this.releaseNotes_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.releaseNotes_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getReleaseNotesBytes() {
                Object obj = this.releaseNotes_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.releaseNotes_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setReleaseNotes(String str) {
                if (str != null) {
                    this.bitField0_ |= 4;
                    this.releaseNotes_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearReleaseNotes() {
                this.bitField0_ &= -5;
                this.releaseNotes_ = PluginUpdate.getDefaultInstance().getReleaseNotes();
                return this;
            }

            public Builder setReleaseNotesBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 4;
                    this.releaseNotes_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }
        }

        private PluginUpdate(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private PluginUpdate(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static PluginUpdate getDefaultInstance() {
            return defaultInstance;
        }

        public PluginUpdate getDefaultInstanceForType() {
            return defaultInstance;
        }

        private PluginUpdate(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            this.plyginType_ = codedInputStream.readBytes();
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.version_ = codedInputStream.readBytes();
                            break;
                        case 26:
                            this.bitField0_ |= 4;
                            this.releaseNotes_ = codedInputStream.readBytes();
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

        public Parser<PluginUpdate> getParserForType() {
            return PARSER;
        }

        public boolean hasPlyginType() {
            return (this.bitField0_ & 1) == 1;
        }

        public String getPlyginType() {
            Object obj = this.plyginType_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.plyginType_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getPlyginTypeBytes() {
            Object obj = this.plyginType_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.plyginType_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasVersion() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getVersion() {
            Object obj = this.version_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.version_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getVersionBytes() {
            Object obj = this.version_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.version_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasReleaseNotes() {
            return (this.bitField0_ & 4) == 4;
        }

        public String getReleaseNotes() {
            Object obj = this.releaseNotes_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.releaseNotes_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getReleaseNotesBytes() {
            Object obj = this.releaseNotes_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.releaseNotes_ = copyFromUtf8;
            return copyFromUtf8;
        }

        private void initFields() {
            this.plyginType_ = "";
            this.version_ = "";
            this.releaseNotes_ = "";
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
                codedOutputStream.writeBytes(1, getPlyginTypeBytes());
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, getVersionBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeBytes(3, getReleaseNotesBytes());
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, getPlyginTypeBytes()) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, getVersionBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeBytesSize(3, getReleaseNotesBytes());
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static PluginUpdate parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (PluginUpdate) PARSER.parseFrom(byteString);
        }

        public static PluginUpdate parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (PluginUpdate) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static PluginUpdate parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (PluginUpdate) PARSER.parseFrom(bArr);
        }

        public static PluginUpdate parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (PluginUpdate) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static PluginUpdate parseFrom(InputStream inputStream) throws IOException {
            return (PluginUpdate) PARSER.parseFrom(inputStream);
        }

        public static PluginUpdate parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (PluginUpdate) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static PluginUpdate parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (PluginUpdate) PARSER.parseDelimitedFrom(inputStream);
        }

        public static PluginUpdate parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (PluginUpdate) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static PluginUpdate parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (PluginUpdate) PARSER.parseFrom(codedInputStream);
        }

        public static PluginUpdate parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (PluginUpdate) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(PluginUpdate pluginUpdate) {
            return newBuilder().mergeFrom(pluginUpdate);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface RuleOrBuilder extends MessageLiteOrBuilder {
        String getDomain();

        ByteString getDomainBytes();

        String getIgnore();

        ByteString getIgnoreBytes();

        String getStyle();

        ByteString getStyleBytes();

        String getUrl();

        ByteString getUrlBytes();

        boolean hasDomain();

        boolean hasIgnore();

        boolean hasStyle();

        boolean hasUrl();
    }

    public static final class Rule extends GeneratedMessageLite implements RuleOrBuilder {
        public static final int DOMAIN_FIELD_NUMBER = 1;
        public static final int IGNORE_FIELD_NUMBER = 3;
        public static Parser<Rule> PARSER = new AbstractParser<Rule>() {
            public Rule parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new Rule(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int STYLE_FIELD_NUMBER = 4;
        public static final int URL_FIELD_NUMBER = 2;
        private static final Rule defaultInstance = new Rule(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private Object domain_;
        private Object ignore_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object style_;
        private Object url_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<Rule, Builder> implements RuleOrBuilder {
            private int bitField0_;
            private Object domain_ = "";
            private Object ignore_ = "";
            private Object style_ = "";
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
                this.domain_ = "";
                this.bitField0_ &= -2;
                this.url_ = "";
                this.bitField0_ &= -3;
                this.ignore_ = "";
                this.bitField0_ &= -5;
                this.style_ = "";
                this.bitField0_ &= -9;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public Rule getDefaultInstanceForType() {
                return Rule.getDefaultInstance();
            }

            public Rule build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public Rule buildPartial() {
                Rule rule = new Rule((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                rule.domain_ = this.domain_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                rule.url_ = this.url_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                rule.ignore_ = this.ignore_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                rule.style_ = this.style_;
                rule.bitField0_ = i2;
                return rule;
            }

            public Builder mergeFrom(Rule rule) {
                if (rule == Rule.getDefaultInstance()) {
                    return this;
                }
                if (rule.hasDomain()) {
                    this.bitField0_ |= 1;
                    this.domain_ = rule.domain_;
                }
                if (rule.hasUrl()) {
                    this.bitField0_ |= 2;
                    this.url_ = rule.url_;
                }
                if (rule.hasIgnore()) {
                    this.bitField0_ |= 4;
                    this.ignore_ = rule.ignore_;
                }
                if (rule.hasStyle()) {
                    this.bitField0_ |= 8;
                    this.style_ = rule.style_;
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Rule rule;
                Throwable th;
                Rule rule2;
                try {
                    rule = (Rule) Rule.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (rule != null) {
                        mergeFrom(rule);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    rule = (Rule) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    rule2 = rule;
                    th = th3;
                }
                if (rule2 != null) {
                    mergeFrom(rule2);
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
                this.domain_ = Rule.getDefaultInstance().getDomain();
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

            public boolean hasUrl() {
                return (this.bitField0_ & 2) == 2;
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
                    this.bitField0_ |= 2;
                    this.url_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearUrl() {
                this.bitField0_ &= -3;
                this.url_ = Rule.getDefaultInstance().getUrl();
                return this;
            }

            public Builder setUrlBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.url_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasIgnore() {
                return (this.bitField0_ & 4) == 4;
            }

            public String getIgnore() {
                Object obj = this.ignore_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.ignore_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getIgnoreBytes() {
                Object obj = this.ignore_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.ignore_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setIgnore(String str) {
                if (str != null) {
                    this.bitField0_ |= 4;
                    this.ignore_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearIgnore() {
                this.bitField0_ &= -5;
                this.ignore_ = Rule.getDefaultInstance().getIgnore();
                return this;
            }

            public Builder setIgnoreBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 4;
                    this.ignore_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasStyle() {
                return (this.bitField0_ & 8) == 8;
            }

            public String getStyle() {
                Object obj = this.style_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.style_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getStyleBytes() {
                Object obj = this.style_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.style_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setStyle(String str) {
                if (str != null) {
                    this.bitField0_ |= 8;
                    this.style_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearStyle() {
                this.bitField0_ &= -9;
                this.style_ = Rule.getDefaultInstance().getStyle();
                return this;
            }

            public Builder setStyleBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 8;
                    this.style_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }
        }

        private Rule(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private Rule(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static Rule getDefaultInstance() {
            return defaultInstance;
        }

        public Rule getDefaultInstanceForType() {
            return defaultInstance;
        }

        private Rule(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            this.url_ = codedInputStream.readBytes();
                            break;
                        case 26:
                            this.bitField0_ |= 4;
                            this.ignore_ = codedInputStream.readBytes();
                            break;
                        case 34:
                            this.bitField0_ |= 8;
                            this.style_ = codedInputStream.readBytes();
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

        public Parser<Rule> getParserForType() {
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

        public boolean hasUrl() {
            return (this.bitField0_ & 2) == 2;
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

        public boolean hasIgnore() {
            return (this.bitField0_ & 4) == 4;
        }

        public String getIgnore() {
            Object obj = this.ignore_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.ignore_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getIgnoreBytes() {
            Object obj = this.ignore_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.ignore_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasStyle() {
            return (this.bitField0_ & 8) == 8;
        }

        public String getStyle() {
            Object obj = this.style_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.style_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getStyleBytes() {
            Object obj = this.style_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.style_ = copyFromUtf8;
            return copyFromUtf8;
        }

        private void initFields() {
            this.domain_ = "";
            this.url_ = "";
            this.ignore_ = "";
            this.style_ = "";
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
                codedOutputStream.writeBytes(2, getUrlBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeBytes(3, getIgnoreBytes());
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeBytes(4, getStyleBytes());
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
                i += CodedOutputStream.computeBytesSize(2, getUrlBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeBytesSize(3, getIgnoreBytes());
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeBytesSize(4, getStyleBytes());
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static Rule parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (Rule) PARSER.parseFrom(byteString);
        }

        public static Rule parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Rule) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static Rule parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (Rule) PARSER.parseFrom(bArr);
        }

        public static Rule parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (Rule) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static Rule parseFrom(InputStream inputStream) throws IOException {
            return (Rule) PARSER.parseFrom(inputStream);
        }

        public static Rule parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Rule) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static Rule parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (Rule) PARSER.parseDelimitedFrom(inputStream);
        }

        public static Rule parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Rule) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static Rule parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (Rule) PARSER.parseFrom(codedInputStream);
        }

        public static Rule parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (Rule) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(Rule rule) {
            return newBuilder().mergeFrom(rule);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface RulesUpdateOrBuilder extends MessageLiteOrBuilder {
        Rule getRules(int i);

        int getRulesCount();

        List<Rule> getRulesList();

        String getVersion();

        ByteString getVersionBytes();

        boolean hasVersion();
    }

    public static final class RulesUpdate extends GeneratedMessageLite implements RulesUpdateOrBuilder {
        public static Parser<RulesUpdate> PARSER = new AbstractParser<RulesUpdate>() {
            public RulesUpdate parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new RulesUpdate(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int RULES_FIELD_NUMBER = 2;
        public static final int VERSION_FIELD_NUMBER = 1;
        private static final RulesUpdate defaultInstance = new RulesUpdate(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private List<Rule> rules_;
        private Object version_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<RulesUpdate, Builder> implements RulesUpdateOrBuilder {
            private int bitField0_;
            private List<Rule> rules_ = Collections.emptyList();
            private Object version_ = "";

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
                this.version_ = "";
                this.bitField0_ &= -2;
                this.rules_ = Collections.emptyList();
                this.bitField0_ &= -3;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public RulesUpdate getDefaultInstanceForType() {
                return RulesUpdate.getDefaultInstance();
            }

            public RulesUpdate build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public RulesUpdate buildPartial() {
                RulesUpdate rulesUpdate = new RulesUpdate((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = 0;
                if ((this.bitField0_ & 1) == 1) {
                    i = 1;
                }
                rulesUpdate.version_ = this.version_;
                if ((this.bitField0_ & 2) == 2) {
                    this.rules_ = Collections.unmodifiableList(this.rules_);
                    this.bitField0_ &= -3;
                }
                rulesUpdate.rules_ = this.rules_;
                rulesUpdate.bitField0_ = i;
                return rulesUpdate;
            }

            public Builder mergeFrom(RulesUpdate rulesUpdate) {
                if (rulesUpdate == RulesUpdate.getDefaultInstance()) {
                    return this;
                }
                if (rulesUpdate.hasVersion()) {
                    this.bitField0_ |= 1;
                    this.version_ = rulesUpdate.version_;
                }
                if (!rulesUpdate.rules_.isEmpty()) {
                    if (this.rules_.isEmpty()) {
                        this.rules_ = rulesUpdate.rules_;
                        this.bitField0_ &= -3;
                    } else {
                        ensureRulesIsMutable();
                        this.rules_.addAll(rulesUpdate.rules_);
                    }
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                RulesUpdate rulesUpdate;
                RulesUpdate rulesUpdate2;
                try {
                    rulesUpdate2 = (RulesUpdate) RulesUpdate.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (rulesUpdate2 != null) {
                        mergeFrom(rulesUpdate2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    rulesUpdate2 = (RulesUpdate) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    rulesUpdate = rulesUpdate2;
                    th = th3;
                }
                if (rulesUpdate != null) {
                    mergeFrom(rulesUpdate);
                }
                throw th;
            }

            public boolean hasVersion() {
                return (this.bitField0_ & 1) == 1;
            }

            public String getVersion() {
                Object obj = this.version_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.version_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getVersionBytes() {
                Object obj = this.version_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.version_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setVersion(String str) {
                if (str != null) {
                    this.bitField0_ |= 1;
                    this.version_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearVersion() {
                this.bitField0_ &= -2;
                this.version_ = RulesUpdate.getDefaultInstance().getVersion();
                return this;
            }

            public Builder setVersionBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.version_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            private void ensureRulesIsMutable() {
                if ((this.bitField0_ & 2) != 2) {
                    this.rules_ = new ArrayList(this.rules_);
                    this.bitField0_ |= 2;
                }
            }

            public List<Rule> getRulesList() {
                return Collections.unmodifiableList(this.rules_);
            }

            public int getRulesCount() {
                return this.rules_.size();
            }

            public Rule getRules(int i) {
                return (Rule) this.rules_.get(i);
            }

            public Builder setRules(int i, Rule rule) {
                if (rule != null) {
                    ensureRulesIsMutable();
                    this.rules_.set(i, rule);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setRules(int i, Builder builder) {
                ensureRulesIsMutable();
                this.rules_.set(i, builder.build());
                return this;
            }

            public Builder addRules(Rule rule) {
                if (rule != null) {
                    ensureRulesIsMutable();
                    this.rules_.add(rule);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addRules(int i, Rule rule) {
                if (rule != null) {
                    ensureRulesIsMutable();
                    this.rules_.add(i, rule);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addRules(Builder builder) {
                ensureRulesIsMutable();
                this.rules_.add(builder.build());
                return this;
            }

            public Builder addRules(int i, Builder builder) {
                ensureRulesIsMutable();
                this.rules_.add(i, builder.build());
                return this;
            }

            public Builder addAllRules(Iterable<? extends Rule> iterable) {
                ensureRulesIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(iterable, this.rules_);
                return this;
            }

            public Builder clearRules() {
                this.rules_ = Collections.emptyList();
                this.bitField0_ &= -3;
                return this;
            }

            public Builder removeRules(int i) {
                ensureRulesIsMutable();
                this.rules_.remove(i);
                return this;
            }
        }

        private RulesUpdate(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private RulesUpdate(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static RulesUpdate getDefaultInstance() {
            return defaultInstance;
        }

        public RulesUpdate getDefaultInstanceForType() {
            return defaultInstance;
        }

        private RulesUpdate(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            this.version_ = codedInputStream.readBytes();
                            break;
                        case 18:
                            if ((i & 2) != 2) {
                                this.rules_ = new ArrayList();
                                i |= 2;
                            }
                            this.rules_.add(codedInputStream.readMessage(Rule.PARSER, extensionRegistryLite));
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
                    if ((i & 2) == 2) {
                        this.rules_ = Collections.unmodifiableList(this.rules_);
                    }
                    makeExtensionsImmutable();
                }
            }
            if ((i & 2) == 2) {
                this.rules_ = Collections.unmodifiableList(this.rules_);
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<RulesUpdate> getParserForType() {
            return PARSER;
        }

        public boolean hasVersion() {
            return (this.bitField0_ & 1) == 1;
        }

        public String getVersion() {
            Object obj = this.version_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.version_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getVersionBytes() {
            Object obj = this.version_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.version_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public List<Rule> getRulesList() {
            return this.rules_;
        }

        public List<? extends RuleOrBuilder> getRulesOrBuilderList() {
            return this.rules_;
        }

        public int getRulesCount() {
            return this.rules_.size();
        }

        public Rule getRules(int i) {
            return (Rule) this.rules_.get(i);
        }

        public RuleOrBuilder getRulesOrBuilder(int i) {
            return (RuleOrBuilder) this.rules_.get(i);
        }

        private void initFields() {
            this.version_ = "";
            this.rules_ = Collections.emptyList();
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
                codedOutputStream.writeBytes(1, getVersionBytes());
            }
            for (int i = 0; i < this.rules_.size(); i++) {
                codedOutputStream.writeMessage(2, (MessageLite) this.rules_.get(i));
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) != 1) {
                i2 = 0;
            } else {
                i2 = CodedOutputStream.computeBytesSize(1, getVersionBytes()) + 0;
            }
            int i3 = i2;
            while (i < this.rules_.size()) {
                i++;
                i3 = CodedOutputStream.computeMessageSize(2, (MessageLite) this.rules_.get(i)) + i3;
            }
            this.memoizedSerializedSize = i3;
            return i3;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static RulesUpdate parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (RulesUpdate) PARSER.parseFrom(byteString);
        }

        public static RulesUpdate parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (RulesUpdate) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static RulesUpdate parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (RulesUpdate) PARSER.parseFrom(bArr);
        }

        public static RulesUpdate parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (RulesUpdate) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static RulesUpdate parseFrom(InputStream inputStream) throws IOException {
            return (RulesUpdate) PARSER.parseFrom(inputStream);
        }

        public static RulesUpdate parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (RulesUpdate) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static RulesUpdate parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (RulesUpdate) PARSER.parseDelimitedFrom(inputStream);
        }

        public static RulesUpdate parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (RulesUpdate) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static RulesUpdate parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (RulesUpdate) PARSER.parseFrom(codedInputStream);
        }

        public static RulesUpdate parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (RulesUpdate) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(RulesUpdate rulesUpdate) {
            return newBuilder().mergeFrom(rulesUpdate);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface UpdateRequestOrBuilder extends MessageLiteOrBuilder {
        String getPluginType();

        ByteString getPluginTypeBytes();

        String getPluginVersion();

        ByteString getPluginVersionBytes();

        String getRulesVersion();

        ByteString getRulesVersionBytes();

        boolean hasPluginType();

        boolean hasPluginVersion();

        boolean hasRulesVersion();
    }

    public static final class UpdateRequest extends GeneratedMessageLite implements UpdateRequestOrBuilder {
        public static Parser<UpdateRequest> PARSER = new AbstractParser<UpdateRequest>() {
            public UpdateRequest parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new UpdateRequest(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int PLUGINTYPE_FIELD_NUMBER = 1;
        public static final int PLUGINVERSION_FIELD_NUMBER = 2;
        public static final int RULESVERSION_FIELD_NUMBER = 3;
        private static final UpdateRequest defaultInstance = new UpdateRequest(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object pluginType_;
        private Object pluginVersion_;
        private Object rulesVersion_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<UpdateRequest, Builder> implements UpdateRequestOrBuilder {
            private int bitField0_;
            private Object pluginType_ = "";
            private Object pluginVersion_ = "";
            private Object rulesVersion_ = "";

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
                this.pluginType_ = "";
                this.bitField0_ &= -2;
                this.pluginVersion_ = "";
                this.bitField0_ &= -3;
                this.rulesVersion_ = "";
                this.bitField0_ &= -5;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public UpdateRequest getDefaultInstanceForType() {
                return UpdateRequest.getDefaultInstance();
            }

            public UpdateRequest build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public UpdateRequest buildPartial() {
                UpdateRequest updateRequest = new UpdateRequest((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                updateRequest.pluginType_ = this.pluginType_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                updateRequest.pluginVersion_ = this.pluginVersion_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                updateRequest.rulesVersion_ = this.rulesVersion_;
                updateRequest.bitField0_ = i2;
                return updateRequest;
            }

            public Builder mergeFrom(UpdateRequest updateRequest) {
                if (updateRequest == UpdateRequest.getDefaultInstance()) {
                    return this;
                }
                if (updateRequest.hasPluginType()) {
                    this.bitField0_ |= 1;
                    this.pluginType_ = updateRequest.pluginType_;
                }
                if (updateRequest.hasPluginVersion()) {
                    this.bitField0_ |= 2;
                    this.pluginVersion_ = updateRequest.pluginVersion_;
                }
                if (updateRequest.hasRulesVersion()) {
                    this.bitField0_ |= 4;
                    this.rulesVersion_ = updateRequest.rulesVersion_;
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                UpdateRequest updateRequest;
                Throwable th;
                UpdateRequest updateRequest2;
                try {
                    updateRequest = (UpdateRequest) UpdateRequest.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (updateRequest != null) {
                        mergeFrom(updateRequest);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    updateRequest = (UpdateRequest) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    updateRequest2 = updateRequest;
                    th = th3;
                }
                if (updateRequest2 != null) {
                    mergeFrom(updateRequest2);
                }
                throw th;
            }

            public boolean hasPluginType() {
                return (this.bitField0_ & 1) == 1;
            }

            public String getPluginType() {
                Object obj = this.pluginType_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.pluginType_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getPluginTypeBytes() {
                Object obj = this.pluginType_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.pluginType_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setPluginType(String str) {
                if (str != null) {
                    this.bitField0_ |= 1;
                    this.pluginType_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearPluginType() {
                this.bitField0_ &= -2;
                this.pluginType_ = UpdateRequest.getDefaultInstance().getPluginType();
                return this;
            }

            public Builder setPluginTypeBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.pluginType_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasPluginVersion() {
                return (this.bitField0_ & 2) == 2;
            }

            public String getPluginVersion() {
                Object obj = this.pluginVersion_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.pluginVersion_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getPluginVersionBytes() {
                Object obj = this.pluginVersion_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.pluginVersion_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setPluginVersion(String str) {
                if (str != null) {
                    this.bitField0_ |= 2;
                    this.pluginVersion_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearPluginVersion() {
                this.bitField0_ &= -3;
                this.pluginVersion_ = UpdateRequest.getDefaultInstance().getPluginVersion();
                return this;
            }

            public Builder setPluginVersionBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.pluginVersion_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasRulesVersion() {
                return (this.bitField0_ & 4) == 4;
            }

            public String getRulesVersion() {
                Object obj = this.rulesVersion_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.rulesVersion_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getRulesVersionBytes() {
                Object obj = this.rulesVersion_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.rulesVersion_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setRulesVersion(String str) {
                if (str != null) {
                    this.bitField0_ |= 4;
                    this.rulesVersion_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearRulesVersion() {
                this.bitField0_ &= -5;
                this.rulesVersion_ = UpdateRequest.getDefaultInstance().getRulesVersion();
                return this;
            }

            public Builder setRulesVersionBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 4;
                    this.rulesVersion_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }
        }

        private UpdateRequest(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private UpdateRequest(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static UpdateRequest getDefaultInstance() {
            return defaultInstance;
        }

        public UpdateRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        private UpdateRequest(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            this.pluginType_ = codedInputStream.readBytes();
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.pluginVersion_ = codedInputStream.readBytes();
                            break;
                        case 26:
                            this.bitField0_ |= 4;
                            this.rulesVersion_ = codedInputStream.readBytes();
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

        public Parser<UpdateRequest> getParserForType() {
            return PARSER;
        }

        public boolean hasPluginType() {
            return (this.bitField0_ & 1) == 1;
        }

        public String getPluginType() {
            Object obj = this.pluginType_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.pluginType_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getPluginTypeBytes() {
            Object obj = this.pluginType_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.pluginType_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasPluginVersion() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getPluginVersion() {
            Object obj = this.pluginVersion_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.pluginVersion_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getPluginVersionBytes() {
            Object obj = this.pluginVersion_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.pluginVersion_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasRulesVersion() {
            return (this.bitField0_ & 4) == 4;
        }

        public String getRulesVersion() {
            Object obj = this.rulesVersion_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.rulesVersion_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getRulesVersionBytes() {
            Object obj = this.rulesVersion_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.rulesVersion_ = copyFromUtf8;
            return copyFromUtf8;
        }

        private void initFields() {
            this.pluginType_ = "";
            this.pluginVersion_ = "";
            this.rulesVersion_ = "";
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
                codedOutputStream.writeBytes(1, getPluginTypeBytes());
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, getPluginVersionBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeBytes(3, getRulesVersionBytes());
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, getPluginTypeBytes()) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, getPluginVersionBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeBytesSize(3, getRulesVersionBytes());
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static UpdateRequest parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (UpdateRequest) PARSER.parseFrom(byteString);
        }

        public static UpdateRequest parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UpdateRequest) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static UpdateRequest parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (UpdateRequest) PARSER.parseFrom(bArr);
        }

        public static UpdateRequest parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UpdateRequest) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static UpdateRequest parseFrom(InputStream inputStream) throws IOException {
            return (UpdateRequest) PARSER.parseFrom(inputStream);
        }

        public static UpdateRequest parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UpdateRequest) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static UpdateRequest parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (UpdateRequest) PARSER.parseDelimitedFrom(inputStream);
        }

        public static UpdateRequest parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UpdateRequest) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static UpdateRequest parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (UpdateRequest) PARSER.parseFrom(codedInputStream);
        }

        public static UpdateRequest parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UpdateRequest) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(UpdateRequest updateRequest) {
            return newBuilder().mergeFrom(updateRequest);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface UpdateResponseOrBuilder extends MessageLiteOrBuilder {
        PluginUpdate getPluginUpdate();

        RulesUpdate getRulesUpdate();

        boolean hasPluginUpdate();

        boolean hasRulesUpdate();
    }

    public static final class UpdateResponse extends GeneratedMessageLite implements UpdateResponseOrBuilder {
        public static Parser<UpdateResponse> PARSER = new AbstractParser<UpdateResponse>() {
            public UpdateResponse parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new UpdateResponse(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int PLUGINUPDATE_FIELD_NUMBER = 1;
        public static final int RULESUPDATE_FIELD_NUMBER = 2;
        private static final UpdateResponse defaultInstance = new UpdateResponse(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private PluginUpdate pluginUpdate_;
        private RulesUpdate rulesUpdate_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<UpdateResponse, Builder> implements UpdateResponseOrBuilder {
            private int bitField0_;
            private PluginUpdate pluginUpdate_ = PluginUpdate.getDefaultInstance();
            private RulesUpdate rulesUpdate_ = RulesUpdate.getDefaultInstance();

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
                this.pluginUpdate_ = PluginUpdate.getDefaultInstance();
                this.bitField0_ &= -2;
                this.rulesUpdate_ = RulesUpdate.getDefaultInstance();
                this.bitField0_ &= -3;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public UpdateResponse getDefaultInstanceForType() {
                return UpdateResponse.getDefaultInstance();
            }

            public UpdateResponse build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public UpdateResponse buildPartial() {
                UpdateResponse updateResponse = new UpdateResponse((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                updateResponse.pluginUpdate_ = this.pluginUpdate_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                updateResponse.rulesUpdate_ = this.rulesUpdate_;
                updateResponse.bitField0_ = i2;
                return updateResponse;
            }

            public Builder mergeFrom(UpdateResponse updateResponse) {
                if (updateResponse == UpdateResponse.getDefaultInstance()) {
                    return this;
                }
                if (updateResponse.hasPluginUpdate()) {
                    mergePluginUpdate(updateResponse.getPluginUpdate());
                }
                if (updateResponse.hasRulesUpdate()) {
                    mergeRulesUpdate(updateResponse.getRulesUpdate());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                UpdateResponse updateResponse;
                UpdateResponse updateResponse2;
                try {
                    updateResponse2 = (UpdateResponse) UpdateResponse.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (updateResponse2 != null) {
                        mergeFrom(updateResponse2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    updateResponse2 = (UpdateResponse) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    updateResponse = updateResponse2;
                    th = th3;
                }
                if (updateResponse != null) {
                    mergeFrom(updateResponse);
                }
                throw th;
            }

            public boolean hasPluginUpdate() {
                return (this.bitField0_ & 1) == 1;
            }

            public PluginUpdate getPluginUpdate() {
                return this.pluginUpdate_;
            }

            public Builder setPluginUpdate(PluginUpdate pluginUpdate) {
                if (pluginUpdate != null) {
                    this.pluginUpdate_ = pluginUpdate;
                    this.bitField0_ |= 1;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setPluginUpdate(Builder builder) {
                this.pluginUpdate_ = builder.build();
                this.bitField0_ |= 1;
                return this;
            }

            public Builder mergePluginUpdate(PluginUpdate pluginUpdate) {
                if ((this.bitField0_ & 1) == 1 && this.pluginUpdate_ != PluginUpdate.getDefaultInstance()) {
                    this.pluginUpdate_ = PluginUpdate.newBuilder(this.pluginUpdate_).mergeFrom(pluginUpdate).buildPartial();
                } else {
                    this.pluginUpdate_ = pluginUpdate;
                }
                this.bitField0_ |= 1;
                return this;
            }

            public Builder clearPluginUpdate() {
                this.pluginUpdate_ = PluginUpdate.getDefaultInstance();
                this.bitField0_ &= -2;
                return this;
            }

            public boolean hasRulesUpdate() {
                return (this.bitField0_ & 2) == 2;
            }

            public RulesUpdate getRulesUpdate() {
                return this.rulesUpdate_;
            }

            public Builder setRulesUpdate(RulesUpdate rulesUpdate) {
                if (rulesUpdate != null) {
                    this.rulesUpdate_ = rulesUpdate;
                    this.bitField0_ |= 2;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setRulesUpdate(Builder builder) {
                this.rulesUpdate_ = builder.build();
                this.bitField0_ |= 2;
                return this;
            }

            public Builder mergeRulesUpdate(RulesUpdate rulesUpdate) {
                if ((this.bitField0_ & 2) == 2 && this.rulesUpdate_ != RulesUpdate.getDefaultInstance()) {
                    this.rulesUpdate_ = RulesUpdate.newBuilder(this.rulesUpdate_).mergeFrom(rulesUpdate).buildPartial();
                } else {
                    this.rulesUpdate_ = rulesUpdate;
                }
                this.bitField0_ |= 2;
                return this;
            }

            public Builder clearRulesUpdate() {
                this.rulesUpdate_ = RulesUpdate.getDefaultInstance();
                this.bitField0_ &= -3;
                return this;
            }
        }

        private UpdateResponse(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private UpdateResponse(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static UpdateResponse getDefaultInstance() {
            return defaultInstance;
        }

        public UpdateResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        private UpdateResponse(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                                builder = this.pluginUpdate_.toBuilder();
                            }
                            this.pluginUpdate_ = (PluginUpdate) codedInputStream.readMessage(PluginUpdate.PARSER, extensionRegistryLite);
                            if (builder != null) {
                                builder.mergeFrom(this.pluginUpdate_);
                                this.pluginUpdate_ = builder.buildPartial();
                            }
                            this.bitField0_ |= 1;
                            obj2 = obj;
                            break;
                        case 18:
                            Builder builder2;
                            if ((this.bitField0_ & 2) != 2) {
                                builder2 = null;
                            } else {
                                builder2 = this.rulesUpdate_.toBuilder();
                            }
                            this.rulesUpdate_ = (RulesUpdate) codedInputStream.readMessage(RulesUpdate.PARSER, extensionRegistryLite);
                            if (builder2 != null) {
                                builder2.mergeFrom(this.rulesUpdate_);
                                this.rulesUpdate_ = builder2.buildPartial();
                            }
                            this.bitField0_ |= 2;
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

        public Parser<UpdateResponse> getParserForType() {
            return PARSER;
        }

        public boolean hasPluginUpdate() {
            return (this.bitField0_ & 1) == 1;
        }

        public PluginUpdate getPluginUpdate() {
            return this.pluginUpdate_;
        }

        public boolean hasRulesUpdate() {
            return (this.bitField0_ & 2) == 2;
        }

        public RulesUpdate getRulesUpdate() {
            return this.rulesUpdate_;
        }

        private void initFields() {
            this.pluginUpdate_ = PluginUpdate.getDefaultInstance();
            this.rulesUpdate_ = RulesUpdate.getDefaultInstance();
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
                codedOutputStream.writeMessage(1, this.pluginUpdate_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeMessage(2, this.rulesUpdate_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.pluginUpdate_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeMessageSize(2, this.rulesUpdate_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static UpdateResponse parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (UpdateResponse) PARSER.parseFrom(byteString);
        }

        public static UpdateResponse parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UpdateResponse) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static UpdateResponse parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (UpdateResponse) PARSER.parseFrom(bArr);
        }

        public static UpdateResponse parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UpdateResponse) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static UpdateResponse parseFrom(InputStream inputStream) throws IOException {
            return (UpdateResponse) PARSER.parseFrom(inputStream);
        }

        public static UpdateResponse parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UpdateResponse) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static UpdateResponse parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (UpdateResponse) PARSER.parseDelimitedFrom(inputStream);
        }

        public static UpdateResponse parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UpdateResponse) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static UpdateResponse parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (UpdateResponse) PARSER.parseFrom(codedInputStream);
        }

        public static UpdateResponse parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UpdateResponse) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(UpdateResponse updateResponse) {
            return newBuilder().mergeFrom(updateResponse);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    private BrowserInfo() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite extensionRegistryLite) {
    }
}
