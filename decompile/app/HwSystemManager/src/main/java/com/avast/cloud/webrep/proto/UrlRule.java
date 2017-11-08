package com.avast.cloud.webrep.proto;

import android.support.v4.view.MotionEventCompat;
import com.avast.cloud.webrep.proto.Urlinfo.Identity;
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
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UrlRule {

    public interface ColorRuleOrBuilder extends MessageLiteOrBuilder {
        String getCss();

        ByteString getCssBytes();

        String getDomain();

        ByteString getDomainBytes();

        String getSelector();

        ByteString getSelectorBytes();

        String getUrl();

        ByteString getUrlBytes();

        boolean hasCss();

        boolean hasDomain();

        boolean hasSelector();

        boolean hasUrl();
    }

    public static final class ColorRule extends GeneratedMessageLite implements ColorRuleOrBuilder {
        public static final int CSS_FIELD_NUMBER = 4;
        public static final int DOMAIN_FIELD_NUMBER = 1;
        public static Parser<ColorRule> PARSER = new AbstractParser<ColorRule>() {
            public ColorRule parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new ColorRule(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int SELECTOR_FIELD_NUMBER = 3;
        public static final int URL_FIELD_NUMBER = 2;
        private static final ColorRule defaultInstance = new ColorRule(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private Object css_;
        private Object domain_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object selector_;
        private Object url_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<ColorRule, Builder> implements ColorRuleOrBuilder {
            private int bitField0_;
            private Object css_ = "";
            private Object domain_ = "";
            private Object selector_ = "";
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
                this.selector_ = "";
                this.bitField0_ &= -5;
                this.css_ = "";
                this.bitField0_ &= -9;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public ColorRule getDefaultInstanceForType() {
                return ColorRule.getDefaultInstance();
            }

            public ColorRule build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public ColorRule buildPartial() {
                ColorRule colorRule = new ColorRule((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                colorRule.domain_ = this.domain_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                colorRule.url_ = this.url_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                colorRule.selector_ = this.selector_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                colorRule.css_ = this.css_;
                colorRule.bitField0_ = i2;
                return colorRule;
            }

            public Builder mergeFrom(ColorRule colorRule) {
                if (colorRule == ColorRule.getDefaultInstance()) {
                    return this;
                }
                if (colorRule.hasDomain()) {
                    this.bitField0_ |= 1;
                    this.domain_ = colorRule.domain_;
                }
                if (colorRule.hasUrl()) {
                    this.bitField0_ |= 2;
                    this.url_ = colorRule.url_;
                }
                if (colorRule.hasSelector()) {
                    this.bitField0_ |= 4;
                    this.selector_ = colorRule.selector_;
                }
                if (colorRule.hasCss()) {
                    this.bitField0_ |= 8;
                    this.css_ = colorRule.css_;
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                ColorRule colorRule;
                Throwable th;
                ColorRule colorRule2;
                try {
                    colorRule = (ColorRule) ColorRule.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (colorRule != null) {
                        mergeFrom(colorRule);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    colorRule = (ColorRule) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    colorRule2 = colorRule;
                    th = th3;
                }
                if (colorRule2 != null) {
                    mergeFrom(colorRule2);
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
                this.domain_ = ColorRule.getDefaultInstance().getDomain();
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
                this.url_ = ColorRule.getDefaultInstance().getUrl();
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
                this.selector_ = ColorRule.getDefaultInstance().getSelector();
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

            public boolean hasCss() {
                return (this.bitField0_ & 8) == 8;
            }

            public String getCss() {
                Object obj = this.css_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.css_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getCssBytes() {
                Object obj = this.css_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.css_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setCss(String str) {
                if (str != null) {
                    this.bitField0_ |= 8;
                    this.css_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearCss() {
                this.bitField0_ &= -9;
                this.css_ = ColorRule.getDefaultInstance().getCss();
                return this;
            }

            public Builder setCssBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 8;
                    this.css_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }
        }

        private ColorRule(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private ColorRule(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static ColorRule getDefaultInstance() {
            return defaultInstance;
        }

        public ColorRule getDefaultInstanceForType() {
            return defaultInstance;
        }

        private ColorRule(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            this.selector_ = codedInputStream.readBytes();
                            break;
                        case 34:
                            this.bitField0_ |= 8;
                            this.css_ = codedInputStream.readBytes();
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

        public Parser<ColorRule> getParserForType() {
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

        public boolean hasCss() {
            return (this.bitField0_ & 8) == 8;
        }

        public String getCss() {
            Object obj = this.css_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.css_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getCssBytes() {
            Object obj = this.css_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.css_ = copyFromUtf8;
            return copyFromUtf8;
        }

        private void initFields() {
            this.domain_ = "";
            this.url_ = "";
            this.selector_ = "";
            this.css_ = "";
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
                codedOutputStream.writeBytes(3, getSelectorBytes());
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeBytes(4, getCssBytes());
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
                i += CodedOutputStream.computeBytesSize(3, getSelectorBytes());
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeBytesSize(4, getCssBytes());
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static ColorRule parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (ColorRule) PARSER.parseFrom(byteString);
        }

        public static ColorRule parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (ColorRule) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static ColorRule parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (ColorRule) PARSER.parseFrom(bArr);
        }

        public static ColorRule parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (ColorRule) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static ColorRule parseFrom(InputStream inputStream) throws IOException {
            return (ColorRule) PARSER.parseFrom(inputStream);
        }

        public static ColorRule parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (ColorRule) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static ColorRule parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (ColorRule) PARSER.parseDelimitedFrom(inputStream);
        }

        public static ColorRule parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (ColorRule) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static ColorRule parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (ColorRule) PARSER.parseFrom(codedInputStream);
        }

        public static ColorRule parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (ColorRule) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(ColorRule colorRule) {
            return newBuilder().mergeFrom(colorRule);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface DNTRuleOrBuilder extends MessageLiteOrBuilder {
        boolean getActive();

        Category getCategory();

        String getDomains(int i);

        ByteString getDomainsBytes(int i);

        int getDomainsCount();

        List<String> getDomainsList();

        int getId();

        String getName();

        ByteString getNameBytes();

        String getPattern();

        ByteString getPatternBytes();

        String getReplacement();

        ByteString getReplacementBytes();

        String getSettings(int i);

        ByteString getSettingsBytes(int i);

        int getSettingsCount();

        List<String> getSettingsList();

        boolean hasActive();

        boolean hasCategory();

        boolean hasId();

        boolean hasName();

        boolean hasPattern();

        boolean hasReplacement();
    }

    public static final class DNTRule extends GeneratedMessageLite implements DNTRuleOrBuilder {
        public static final int ACTIVE_FIELD_NUMBER = 5;
        public static final int CATEGORY_FIELD_NUMBER = 3;
        public static final int DOMAINS_FIELD_NUMBER = 8;
        public static final int ID_FIELD_NUMBER = 1;
        public static final int NAME_FIELD_NUMBER = 2;
        public static Parser<DNTRule> PARSER = new AbstractParser<DNTRule>() {
            public DNTRule parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new DNTRule(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int PATTERN_FIELD_NUMBER = 4;
        public static final int REPLACEMENT_FIELD_NUMBER = 6;
        public static final int SETTINGS_FIELD_NUMBER = 7;
        private static final DNTRule defaultInstance = new DNTRule(true);
        private static final long serialVersionUID = 0;
        private boolean active_;
        private int bitField0_;
        private Category category_;
        private LazyStringList domains_;
        private int id_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object name_;
        private Object pattern_;
        private Object replacement_;
        private LazyStringList settings_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<DNTRule, Builder> implements DNTRuleOrBuilder {
            private boolean active_;
            private int bitField0_;
            private Category category_ = Category.OTHERS;
            private LazyStringList domains_ = LazyStringArrayList.EMPTY;
            private int id_;
            private Object name_ = "";
            private Object pattern_ = "";
            private Object replacement_ = "";
            private LazyStringList settings_ = LazyStringArrayList.EMPTY;

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
                this.id_ = 0;
                this.bitField0_ &= -2;
                this.name_ = "";
                this.bitField0_ &= -3;
                this.category_ = Category.OTHERS;
                this.bitField0_ &= -5;
                this.pattern_ = "";
                this.bitField0_ &= -9;
                this.active_ = false;
                this.bitField0_ &= -17;
                this.replacement_ = "";
                this.bitField0_ &= -33;
                this.settings_ = LazyStringArrayList.EMPTY;
                this.bitField0_ &= -65;
                this.domains_ = LazyStringArrayList.EMPTY;
                this.bitField0_ &= -129;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public DNTRule getDefaultInstanceForType() {
                return DNTRule.getDefaultInstance();
            }

            public DNTRule build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public DNTRule buildPartial() {
                DNTRule dNTRule = new DNTRule((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                dNTRule.id_ = this.id_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                dNTRule.name_ = this.name_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                dNTRule.category_ = this.category_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                dNTRule.pattern_ = this.pattern_;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                dNTRule.active_ = this.active_;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                dNTRule.replacement_ = this.replacement_;
                if ((this.bitField0_ & 64) == 64) {
                    this.settings_ = new UnmodifiableLazyStringList(this.settings_);
                    this.bitField0_ &= -65;
                }
                dNTRule.settings_ = this.settings_;
                if ((this.bitField0_ & 128) == 128) {
                    this.domains_ = new UnmodifiableLazyStringList(this.domains_);
                    this.bitField0_ &= -129;
                }
                dNTRule.domains_ = this.domains_;
                dNTRule.bitField0_ = i2;
                return dNTRule;
            }

            public Builder mergeFrom(DNTRule dNTRule) {
                if (dNTRule == DNTRule.getDefaultInstance()) {
                    return this;
                }
                if (dNTRule.hasId()) {
                    setId(dNTRule.getId());
                }
                if (dNTRule.hasName()) {
                    this.bitField0_ |= 2;
                    this.name_ = dNTRule.name_;
                }
                if (dNTRule.hasCategory()) {
                    setCategory(dNTRule.getCategory());
                }
                if (dNTRule.hasPattern()) {
                    this.bitField0_ |= 8;
                    this.pattern_ = dNTRule.pattern_;
                }
                if (dNTRule.hasActive()) {
                    setActive(dNTRule.getActive());
                }
                if (dNTRule.hasReplacement()) {
                    this.bitField0_ |= 32;
                    this.replacement_ = dNTRule.replacement_;
                }
                if (!dNTRule.settings_.isEmpty()) {
                    if (this.settings_.isEmpty()) {
                        this.settings_ = dNTRule.settings_;
                        this.bitField0_ &= -65;
                    } else {
                        ensureSettingsIsMutable();
                        this.settings_.addAll(dNTRule.settings_);
                    }
                }
                if (!dNTRule.domains_.isEmpty()) {
                    if (this.domains_.isEmpty()) {
                        this.domains_ = dNTRule.domains_;
                        this.bitField0_ &= -129;
                    } else {
                        ensureDomainsIsMutable();
                        this.domains_.addAll(dNTRule.domains_);
                    }
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                DNTRule dNTRule;
                DNTRule dNTRule2;
                try {
                    dNTRule2 = (DNTRule) DNTRule.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (dNTRule2 != null) {
                        mergeFrom(dNTRule2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    dNTRule2 = (DNTRule) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    dNTRule = dNTRule2;
                    th = th3;
                }
                if (dNTRule != null) {
                    mergeFrom(dNTRule);
                }
                throw th;
            }

            public boolean hasId() {
                return (this.bitField0_ & 1) == 1;
            }

            public int getId() {
                return this.id_;
            }

            public Builder setId(int i) {
                this.bitField0_ |= 1;
                this.id_ = i;
                return this;
            }

            public Builder clearId() {
                this.bitField0_ &= -2;
                this.id_ = 0;
                return this;
            }

            public boolean hasName() {
                return (this.bitField0_ & 2) == 2;
            }

            public String getName() {
                Object obj = this.name_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.name_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getNameBytes() {
                Object obj = this.name_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.name_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setName(String str) {
                if (str != null) {
                    this.bitField0_ |= 2;
                    this.name_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearName() {
                this.bitField0_ &= -3;
                this.name_ = DNTRule.getDefaultInstance().getName();
                return this;
            }

            public Builder setNameBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.name_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasCategory() {
                return (this.bitField0_ & 4) == 4;
            }

            public Category getCategory() {
                return this.category_;
            }

            public Builder setCategory(Category category) {
                if (category != null) {
                    this.bitField0_ |= 4;
                    this.category_ = category;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearCategory() {
                this.bitField0_ &= -5;
                this.category_ = Category.OTHERS;
                return this;
            }

            public boolean hasPattern() {
                return (this.bitField0_ & 8) == 8;
            }

            public String getPattern() {
                Object obj = this.pattern_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.pattern_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getPatternBytes() {
                Object obj = this.pattern_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.pattern_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setPattern(String str) {
                if (str != null) {
                    this.bitField0_ |= 8;
                    this.pattern_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearPattern() {
                this.bitField0_ &= -9;
                this.pattern_ = DNTRule.getDefaultInstance().getPattern();
                return this;
            }

            public Builder setPatternBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 8;
                    this.pattern_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasActive() {
                return (this.bitField0_ & 16) == 16;
            }

            public boolean getActive() {
                return this.active_;
            }

            public Builder setActive(boolean z) {
                this.bitField0_ |= 16;
                this.active_ = z;
                return this;
            }

            public Builder clearActive() {
                this.bitField0_ &= -17;
                this.active_ = false;
                return this;
            }

            public boolean hasReplacement() {
                return (this.bitField0_ & 32) == 32;
            }

            public String getReplacement() {
                Object obj = this.replacement_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.replacement_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getReplacementBytes() {
                Object obj = this.replacement_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.replacement_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setReplacement(String str) {
                if (str != null) {
                    this.bitField0_ |= 32;
                    this.replacement_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearReplacement() {
                this.bitField0_ &= -33;
                this.replacement_ = DNTRule.getDefaultInstance().getReplacement();
                return this;
            }

            public Builder setReplacementBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 32;
                    this.replacement_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            private void ensureSettingsIsMutable() {
                if ((this.bitField0_ & 64) != 64) {
                    this.settings_ = new LazyStringArrayList(this.settings_);
                    this.bitField0_ |= 64;
                }
            }

            public List<String> getSettingsList() {
                return Collections.unmodifiableList(this.settings_);
            }

            public int getSettingsCount() {
                return this.settings_.size();
            }

            public String getSettings(int i) {
                return (String) this.settings_.get(i);
            }

            public ByteString getSettingsBytes(int i) {
                return this.settings_.getByteString(i);
            }

            public Builder setSettings(int i, String str) {
                if (str != null) {
                    ensureSettingsIsMutable();
                    this.settings_.set(i, str);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addSettings(String str) {
                if (str != null) {
                    ensureSettingsIsMutable();
                    this.settings_.add(str);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addAllSettings(Iterable<String> iterable) {
                ensureSettingsIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(iterable, this.settings_);
                return this;
            }

            public Builder clearSettings() {
                this.settings_ = LazyStringArrayList.EMPTY;
                this.bitField0_ &= -65;
                return this;
            }

            public Builder addSettingsBytes(ByteString byteString) {
                if (byteString != null) {
                    ensureSettingsIsMutable();
                    this.settings_.add(byteString);
                    return this;
                }
                throw new NullPointerException();
            }

            private void ensureDomainsIsMutable() {
                if ((this.bitField0_ & 128) != 128) {
                    this.domains_ = new LazyStringArrayList(this.domains_);
                    this.bitField0_ |= 128;
                }
            }

            public List<String> getDomainsList() {
                return Collections.unmodifiableList(this.domains_);
            }

            public int getDomainsCount() {
                return this.domains_.size();
            }

            public String getDomains(int i) {
                return (String) this.domains_.get(i);
            }

            public ByteString getDomainsBytes(int i) {
                return this.domains_.getByteString(i);
            }

            public Builder setDomains(int i, String str) {
                if (str != null) {
                    ensureDomainsIsMutable();
                    this.domains_.set(i, str);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addDomains(String str) {
                if (str != null) {
                    ensureDomainsIsMutable();
                    this.domains_.add(str);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addAllDomains(Iterable<String> iterable) {
                ensureDomainsIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(iterable, this.domains_);
                return this;
            }

            public Builder clearDomains() {
                this.domains_ = LazyStringArrayList.EMPTY;
                this.bitField0_ &= -129;
                return this;
            }

            public Builder addDomainsBytes(ByteString byteString) {
                if (byteString != null) {
                    ensureDomainsIsMutable();
                    this.domains_.add(byteString);
                    return this;
                }
                throw new NullPointerException();
            }
        }

        public enum Category implements EnumLite {
            OTHERS(0, 0),
            WEB_ANALYTICS(1, 1),
            WEB_TOOLS(2, 2),
            AD_TRACKING(3, 3),
            AD_ANALYTICS(4, 4),
            AD_DELIVERY(5, 5),
            SOCIAL_BUTTONS(6, 11),
            SOCIAL_LOGIN(7, 12),
            SOCIAL_WIDGET(8, 13);
            
            public static final int AD_ANALYTICS_VALUE = 4;
            public static final int AD_DELIVERY_VALUE = 5;
            public static final int AD_TRACKING_VALUE = 3;
            public static final int OTHERS_VALUE = 0;
            public static final int SOCIAL_BUTTONS_VALUE = 11;
            public static final int SOCIAL_LOGIN_VALUE = 12;
            public static final int SOCIAL_WIDGET_VALUE = 13;
            public static final int WEB_ANALYTICS_VALUE = 1;
            public static final int WEB_TOOLS_VALUE = 2;
            private static EnumLiteMap<Category> internalValueMap;
            private final int value;

            static {
                internalValueMap = new EnumLiteMap<Category>() {
                    public Category findValueByNumber(int i) {
                        return Category.valueOf(i);
                    }
                };
            }

            public final int getNumber() {
                return this.value;
            }

            public static Category valueOf(int i) {
                switch (i) {
                    case 0:
                        return OTHERS;
                    case 1:
                        return WEB_ANALYTICS;
                    case 2:
                        return WEB_TOOLS;
                    case 3:
                        return AD_TRACKING;
                    case 4:
                        return AD_ANALYTICS;
                    case 5:
                        return AD_DELIVERY;
                    case 11:
                        return SOCIAL_BUTTONS;
                    case 12:
                        return SOCIAL_LOGIN;
                    case 13:
                        return SOCIAL_WIDGET;
                    default:
                        return null;
                }
            }

            public static EnumLiteMap<Category> internalGetValueMap() {
                return internalValueMap;
            }

            private Category(int i, int i2) {
                this.value = i2;
            }
        }

        private DNTRule(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private DNTRule(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static DNTRule getDefaultInstance() {
            return defaultInstance;
        }

        public DNTRule getDefaultInstanceForType() {
            return defaultInstance;
        }

        private DNTRule(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                        case 8:
                            this.bitField0_ |= 1;
                            this.id_ = codedInputStream.readInt32();
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.name_ = codedInputStream.readBytes();
                            break;
                        case 24:
                            Category valueOf = Category.valueOf(codedInputStream.readEnum());
                            if (valueOf == null) {
                                break;
                            }
                            this.bitField0_ |= 4;
                            this.category_ = valueOf;
                            break;
                        case 34:
                            this.bitField0_ |= 8;
                            this.pattern_ = codedInputStream.readBytes();
                            break;
                        case 40:
                            this.bitField0_ |= 16;
                            this.active_ = codedInputStream.readBool();
                            break;
                        case 50:
                            this.bitField0_ |= 32;
                            this.replacement_ = codedInputStream.readBytes();
                            break;
                        case 58:
                            if ((i & 64) != 64) {
                                this.settings_ = new LazyStringArrayList();
                                i |= 64;
                            }
                            this.settings_.add(codedInputStream.readBytes());
                            break;
                        case Events.E_ANTISPAM_VIEW_CALL /*66*/:
                            if ((i & 128) != 128) {
                                this.domains_ = new LazyStringArrayList();
                                i |= 128;
                            }
                            this.domains_.add(codedInputStream.readBytes());
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
                    if ((i & 64) == 64) {
                        this.settings_ = new UnmodifiableLazyStringList(this.settings_);
                    }
                    if ((i & 128) == 128) {
                        this.domains_ = new UnmodifiableLazyStringList(this.domains_);
                    }
                    makeExtensionsImmutable();
                }
            }
            if ((i & 64) == 64) {
                this.settings_ = new UnmodifiableLazyStringList(this.settings_);
            }
            if ((i & 128) == 128) {
                this.domains_ = new UnmodifiableLazyStringList(this.domains_);
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<DNTRule> getParserForType() {
            return PARSER;
        }

        public boolean hasId() {
            return (this.bitField0_ & 1) == 1;
        }

        public int getId() {
            return this.id_;
        }

        public boolean hasName() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getName() {
            Object obj = this.name_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.name_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getNameBytes() {
            Object obj = this.name_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.name_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasCategory() {
            return (this.bitField0_ & 4) == 4;
        }

        public Category getCategory() {
            return this.category_;
        }

        public boolean hasPattern() {
            return (this.bitField0_ & 8) == 8;
        }

        public String getPattern() {
            Object obj = this.pattern_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.pattern_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getPatternBytes() {
            Object obj = this.pattern_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.pattern_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasActive() {
            return (this.bitField0_ & 16) == 16;
        }

        public boolean getActive() {
            return this.active_;
        }

        public boolean hasReplacement() {
            return (this.bitField0_ & 32) == 32;
        }

        public String getReplacement() {
            Object obj = this.replacement_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.replacement_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getReplacementBytes() {
            Object obj = this.replacement_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.replacement_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public List<String> getSettingsList() {
            return this.settings_;
        }

        public int getSettingsCount() {
            return this.settings_.size();
        }

        public String getSettings(int i) {
            return (String) this.settings_.get(i);
        }

        public ByteString getSettingsBytes(int i) {
            return this.settings_.getByteString(i);
        }

        public List<String> getDomainsList() {
            return this.domains_;
        }

        public int getDomainsCount() {
            return this.domains_.size();
        }

        public String getDomains(int i) {
            return (String) this.domains_.get(i);
        }

        public ByteString getDomainsBytes(int i) {
            return this.domains_.getByteString(i);
        }

        private void initFields() {
            this.id_ = 0;
            this.name_ = "";
            this.category_ = Category.OTHERS;
            this.pattern_ = "";
            this.active_ = false;
            this.replacement_ = "";
            this.settings_ = LazyStringArrayList.EMPTY;
            this.domains_ = LazyStringArrayList.EMPTY;
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
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeInt32(1, this.id_);
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, getNameBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeEnum(3, this.category_.getNumber());
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeBytes(4, getPatternBytes());
            }
            if ((this.bitField0_ & 16) == 16) {
                codedOutputStream.writeBool(5, this.active_);
            }
            if ((this.bitField0_ & 32) == 32) {
                codedOutputStream.writeBytes(6, getReplacementBytes());
            }
            for (int i2 = 0; i2 < this.settings_.size(); i2++) {
                codedOutputStream.writeBytes(7, this.settings_.getByteString(i2));
            }
            while (i < this.domains_.size()) {
                codedOutputStream.writeBytes(8, this.domains_.getByteString(i));
                i++;
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
                i2 = CodedOutputStream.computeInt32Size(1, this.id_) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i2 += CodedOutputStream.computeBytesSize(2, getNameBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                i2 += CodedOutputStream.computeEnumSize(3, this.category_.getNumber());
            }
            if ((this.bitField0_ & 8) == 8) {
                i2 += CodedOutputStream.computeBytesSize(4, getPatternBytes());
            }
            if ((this.bitField0_ & 16) == 16) {
                i2 += CodedOutputStream.computeBoolSize(5, this.active_);
            }
            if ((this.bitField0_ & 32) == 32) {
                i2 += CodedOutputStream.computeBytesSize(6, getReplacementBytes());
            }
            int i4 = 0;
            for (i3 = 0; i3 < this.settings_.size(); i3++) {
                i4 += CodedOutputStream.computeBytesSizeNoTag(this.settings_.getByteString(i3));
            }
            i3 = (getSettingsList().size() * 1) + (i2 + i4);
            i2 = 0;
            while (i < this.domains_.size()) {
                i2 += CodedOutputStream.computeBytesSizeNoTag(this.domains_.getByteString(i));
                i++;
            }
            i2 = (i2 + i3) + (getDomainsList().size() * 1);
            this.memoizedSerializedSize = i2;
            return i2;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static DNTRule parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (DNTRule) PARSER.parseFrom(byteString);
        }

        public static DNTRule parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (DNTRule) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static DNTRule parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (DNTRule) PARSER.parseFrom(bArr);
        }

        public static DNTRule parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (DNTRule) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static DNTRule parseFrom(InputStream inputStream) throws IOException {
            return (DNTRule) PARSER.parseFrom(inputStream);
        }

        public static DNTRule parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (DNTRule) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static DNTRule parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (DNTRule) PARSER.parseDelimitedFrom(inputStream);
        }

        public static DNTRule parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (DNTRule) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static DNTRule parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (DNTRule) PARSER.parseFrom(codedInputStream);
        }

        public static DNTRule parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (DNTRule) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(DNTRule dNTRule) {
            return newBuilder().mergeFrom(dNTRule);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface HintRuleOrBuilder extends MessageLiteOrBuilder {
        String getContent();

        ByteString getContentBytes();

        String getDomain();

        ByteString getDomainBytes();

        int getFrequency();

        String getUrl();

        ByteString getUrlBytes();

        boolean hasContent();

        boolean hasDomain();

        boolean hasFrequency();

        boolean hasUrl();
    }

    public static final class HintRule extends GeneratedMessageLite implements HintRuleOrBuilder {
        public static final int CONTENT_FIELD_NUMBER = 3;
        public static final int DOMAIN_FIELD_NUMBER = 1;
        public static final int FREQUENCY_FIELD_NUMBER = 4;
        public static Parser<HintRule> PARSER = new AbstractParser<HintRule>() {
            public HintRule parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new HintRule(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int URL_FIELD_NUMBER = 2;
        private static final HintRule defaultInstance = new HintRule(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private Object content_;
        private Object domain_;
        private int frequency_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object url_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<HintRule, Builder> implements HintRuleOrBuilder {
            private int bitField0_;
            private Object content_ = "";
            private Object domain_ = "";
            private int frequency_;
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
                this.content_ = "";
                this.bitField0_ &= -5;
                this.frequency_ = 0;
                this.bitField0_ &= -9;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public HintRule getDefaultInstanceForType() {
                return HintRule.getDefaultInstance();
            }

            public HintRule build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public HintRule buildPartial() {
                HintRule hintRule = new HintRule((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                hintRule.domain_ = this.domain_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                hintRule.url_ = this.url_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                hintRule.content_ = this.content_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                hintRule.frequency_ = this.frequency_;
                hintRule.bitField0_ = i2;
                return hintRule;
            }

            public Builder mergeFrom(HintRule hintRule) {
                if (hintRule == HintRule.getDefaultInstance()) {
                    return this;
                }
                if (hintRule.hasDomain()) {
                    this.bitField0_ |= 1;
                    this.domain_ = hintRule.domain_;
                }
                if (hintRule.hasUrl()) {
                    this.bitField0_ |= 2;
                    this.url_ = hintRule.url_;
                }
                if (hintRule.hasContent()) {
                    this.bitField0_ |= 4;
                    this.content_ = hintRule.content_;
                }
                if (hintRule.hasFrequency()) {
                    setFrequency(hintRule.getFrequency());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                HintRule hintRule;
                HintRule hintRule2;
                try {
                    hintRule2 = (HintRule) HintRule.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (hintRule2 != null) {
                        mergeFrom(hintRule2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    hintRule2 = (HintRule) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    hintRule = hintRule2;
                    th = th3;
                }
                if (hintRule != null) {
                    mergeFrom(hintRule);
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
                this.domain_ = HintRule.getDefaultInstance().getDomain();
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
                this.url_ = HintRule.getDefaultInstance().getUrl();
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

            public boolean hasContent() {
                return (this.bitField0_ & 4) == 4;
            }

            public String getContent() {
                Object obj = this.content_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.content_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getContentBytes() {
                Object obj = this.content_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.content_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setContent(String str) {
                if (str != null) {
                    this.bitField0_ |= 4;
                    this.content_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearContent() {
                this.bitField0_ &= -5;
                this.content_ = HintRule.getDefaultInstance().getContent();
                return this;
            }

            public Builder setContentBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 4;
                    this.content_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasFrequency() {
                return (this.bitField0_ & 8) == 8;
            }

            public int getFrequency() {
                return this.frequency_;
            }

            public Builder setFrequency(int i) {
                this.bitField0_ |= 8;
                this.frequency_ = i;
                return this;
            }

            public Builder clearFrequency() {
                this.bitField0_ &= -9;
                this.frequency_ = 0;
                return this;
            }
        }

        private HintRule(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private HintRule(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static HintRule getDefaultInstance() {
            return defaultInstance;
        }

        public HintRule getDefaultInstanceForType() {
            return defaultInstance;
        }

        private HintRule(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            this.content_ = codedInputStream.readBytes();
                            break;
                        case 32:
                            this.bitField0_ |= 8;
                            this.frequency_ = codedInputStream.readSInt32();
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

        public Parser<HintRule> getParserForType() {
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

        public boolean hasContent() {
            return (this.bitField0_ & 4) == 4;
        }

        public String getContent() {
            Object obj = this.content_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.content_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getContentBytes() {
            Object obj = this.content_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.content_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasFrequency() {
            return (this.bitField0_ & 8) == 8;
        }

        public int getFrequency() {
            return this.frequency_;
        }

        private void initFields() {
            this.domain_ = "";
            this.url_ = "";
            this.content_ = "";
            this.frequency_ = 0;
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
                codedOutputStream.writeBytes(3, getContentBytes());
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeSInt32(4, this.frequency_);
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
                i += CodedOutputStream.computeBytesSize(3, getContentBytes());
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeSInt32Size(4, this.frequency_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static HintRule parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (HintRule) PARSER.parseFrom(byteString);
        }

        public static HintRule parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (HintRule) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static HintRule parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (HintRule) PARSER.parseFrom(bArr);
        }

        public static HintRule parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (HintRule) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static HintRule parseFrom(InputStream inputStream) throws IOException {
            return (HintRule) PARSER.parseFrom(inputStream);
        }

        public static HintRule parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (HintRule) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static HintRule parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (HintRule) PARSER.parseDelimitedFrom(inputStream);
        }

        public static HintRule parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (HintRule) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static HintRule parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (HintRule) PARSER.parseFrom(codedInputStream);
        }

        public static HintRule parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (HintRule) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(HintRule hintRule) {
            return newBuilder().mergeFrom(hintRule);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface RuleUpdateRequestOrBuilder extends MessageLiteOrBuilder {
        long getCallerId();

        Identity getIdentity();

        String getRulesVersion();

        ByteString getRulesVersionBytes();

        boolean hasCallerId();

        boolean hasIdentity();

        boolean hasRulesVersion();
    }

    public static final class RuleUpdateRequest extends GeneratedMessageLite implements RuleUpdateRequestOrBuilder {
        public static final int CALLERID_FIELD_NUMBER = 3;
        public static final int IDENTITY_FIELD_NUMBER = 2;
        public static Parser<RuleUpdateRequest> PARSER = new AbstractParser<RuleUpdateRequest>() {
            public RuleUpdateRequest parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new RuleUpdateRequest(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int RULESVERSION_FIELD_NUMBER = 1;
        private static final RuleUpdateRequest defaultInstance = new RuleUpdateRequest(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private long callerId_;
        private Identity identity_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object rulesVersion_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<RuleUpdateRequest, Builder> implements RuleUpdateRequestOrBuilder {
            private int bitField0_;
            private long callerId_;
            private Identity identity_ = Identity.getDefaultInstance();
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
                this.rulesVersion_ = "";
                this.bitField0_ &= -2;
                this.identity_ = Identity.getDefaultInstance();
                this.bitField0_ &= -3;
                this.callerId_ = 0;
                this.bitField0_ &= -5;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public RuleUpdateRequest getDefaultInstanceForType() {
                return RuleUpdateRequest.getDefaultInstance();
            }

            public RuleUpdateRequest build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public RuleUpdateRequest buildPartial() {
                RuleUpdateRequest ruleUpdateRequest = new RuleUpdateRequest((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                ruleUpdateRequest.rulesVersion_ = this.rulesVersion_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                ruleUpdateRequest.identity_ = this.identity_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                ruleUpdateRequest.callerId_ = this.callerId_;
                ruleUpdateRequest.bitField0_ = i2;
                return ruleUpdateRequest;
            }

            public Builder mergeFrom(RuleUpdateRequest ruleUpdateRequest) {
                if (ruleUpdateRequest == RuleUpdateRequest.getDefaultInstance()) {
                    return this;
                }
                if (ruleUpdateRequest.hasRulesVersion()) {
                    this.bitField0_ |= 1;
                    this.rulesVersion_ = ruleUpdateRequest.rulesVersion_;
                }
                if (ruleUpdateRequest.hasIdentity()) {
                    mergeIdentity(ruleUpdateRequest.getIdentity());
                }
                if (ruleUpdateRequest.hasCallerId()) {
                    setCallerId(ruleUpdateRequest.getCallerId());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                RuleUpdateRequest ruleUpdateRequest;
                RuleUpdateRequest ruleUpdateRequest2;
                try {
                    ruleUpdateRequest2 = (RuleUpdateRequest) RuleUpdateRequest.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (ruleUpdateRequest2 != null) {
                        mergeFrom(ruleUpdateRequest2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    ruleUpdateRequest2 = (RuleUpdateRequest) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    ruleUpdateRequest = ruleUpdateRequest2;
                    th = th3;
                }
                if (ruleUpdateRequest != null) {
                    mergeFrom(ruleUpdateRequest);
                }
                throw th;
            }

            public boolean hasRulesVersion() {
                return (this.bitField0_ & 1) == 1;
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
                    this.bitField0_ |= 1;
                    this.rulesVersion_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearRulesVersion() {
                this.bitField0_ &= -2;
                this.rulesVersion_ = RuleUpdateRequest.getDefaultInstance().getRulesVersion();
                return this;
            }

            public Builder setRulesVersionBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.rulesVersion_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasIdentity() {
                return (this.bitField0_ & 2) == 2;
            }

            public Identity getIdentity() {
                return this.identity_;
            }

            public Builder setIdentity(Identity identity) {
                if (identity != null) {
                    this.identity_ = identity;
                    this.bitField0_ |= 2;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setIdentity(com.avast.cloud.webrep.proto.Urlinfo.Identity.Builder builder) {
                this.identity_ = builder.build();
                this.bitField0_ |= 2;
                return this;
            }

            public Builder mergeIdentity(Identity identity) {
                if ((this.bitField0_ & 2) == 2 && this.identity_ != Identity.getDefaultInstance()) {
                    this.identity_ = Identity.newBuilder(this.identity_).mergeFrom(identity).buildPartial();
                } else {
                    this.identity_ = identity;
                }
                this.bitField0_ |= 2;
                return this;
            }

            public Builder clearIdentity() {
                this.identity_ = Identity.getDefaultInstance();
                this.bitField0_ &= -3;
                return this;
            }

            public boolean hasCallerId() {
                return (this.bitField0_ & 4) == 4;
            }

            public long getCallerId() {
                return this.callerId_;
            }

            public Builder setCallerId(long j) {
                this.bitField0_ |= 4;
                this.callerId_ = j;
                return this;
            }

            public Builder clearCallerId() {
                this.bitField0_ &= -5;
                this.callerId_ = 0;
                return this;
            }
        }

        private RuleUpdateRequest(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private RuleUpdateRequest(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static RuleUpdateRequest getDefaultInstance() {
            return defaultInstance;
        }

        public RuleUpdateRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        private RuleUpdateRequest(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            this.rulesVersion_ = codedInputStream.readBytes();
                            obj2 = obj;
                            break;
                        case 18:
                            com.avast.cloud.webrep.proto.Urlinfo.Identity.Builder builder;
                            if ((this.bitField0_ & 2) != 2) {
                                builder = null;
                            } else {
                                builder = this.identity_.toBuilder();
                            }
                            this.identity_ = (Identity) codedInputStream.readMessage(Identity.PARSER, extensionRegistryLite);
                            if (builder != null) {
                                builder.mergeFrom(this.identity_);
                                this.identity_ = builder.buildPartial();
                            }
                            this.bitField0_ |= 2;
                            obj2 = obj;
                            break;
                        case 24:
                            this.bitField0_ |= 4;
                            this.callerId_ = codedInputStream.readSInt64();
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

        public Parser<RuleUpdateRequest> getParserForType() {
            return PARSER;
        }

        public boolean hasRulesVersion() {
            return (this.bitField0_ & 1) == 1;
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

        public boolean hasIdentity() {
            return (this.bitField0_ & 2) == 2;
        }

        public Identity getIdentity() {
            return this.identity_;
        }

        public boolean hasCallerId() {
            return (this.bitField0_ & 4) == 4;
        }

        public long getCallerId() {
            return this.callerId_;
        }

        private void initFields() {
            this.rulesVersion_ = "";
            this.identity_ = Identity.getDefaultInstance();
            this.callerId_ = 0;
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
                codedOutputStream.writeBytes(1, getRulesVersionBytes());
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeMessage(2, this.identity_);
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeSInt64(3, this.callerId_);
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, getRulesVersionBytes()) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeMessageSize(2, this.identity_);
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeSInt64Size(3, this.callerId_);
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static RuleUpdateRequest parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (RuleUpdateRequest) PARSER.parseFrom(byteString);
        }

        public static RuleUpdateRequest parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (RuleUpdateRequest) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static RuleUpdateRequest parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (RuleUpdateRequest) PARSER.parseFrom(bArr);
        }

        public static RuleUpdateRequest parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (RuleUpdateRequest) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static RuleUpdateRequest parseFrom(InputStream inputStream) throws IOException {
            return (RuleUpdateRequest) PARSER.parseFrom(inputStream);
        }

        public static RuleUpdateRequest parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (RuleUpdateRequest) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static RuleUpdateRequest parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (RuleUpdateRequest) PARSER.parseDelimitedFrom(inputStream);
        }

        public static RuleUpdateRequest parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (RuleUpdateRequest) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static RuleUpdateRequest parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (RuleUpdateRequest) PARSER.parseFrom(codedInputStream);
        }

        public static RuleUpdateRequest parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (RuleUpdateRequest) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(RuleUpdateRequest ruleUpdateRequest) {
            return newBuilder().mergeFrom(ruleUpdateRequest);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface RuleUpdateResponseOrBuilder extends MessageLiteOrBuilder {
        ColorRule getColorRules(int i);

        int getColorRulesCount();

        List<ColorRule> getColorRulesList();

        DNTRule getDntRules(int i);

        int getDntRulesCount();

        List<DNTRule> getDntRulesList();

        HintRule getHintRules(int i);

        int getHintRulesCount();

        List<HintRule> getHintRulesList();

        int getTtl();

        String getUserId();

        ByteString getUserIdBytes();

        String getVersion();

        ByteString getVersionBytes();

        boolean hasTtl();

        boolean hasUserId();

        boolean hasVersion();
    }

    public static final class RuleUpdateResponse extends GeneratedMessageLite implements RuleUpdateResponseOrBuilder {
        public static final int COLORRULES_FIELD_NUMBER = 3;
        public static final int DNTRULES_FIELD_NUMBER = 4;
        public static final int HINTRULES_FIELD_NUMBER = 5;
        public static Parser<RuleUpdateResponse> PARSER = new AbstractParser<RuleUpdateResponse>() {
            public RuleUpdateResponse parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new RuleUpdateResponse(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int TTL_FIELD_NUMBER = 2;
        public static final int USERID_FIELD_NUMBER = 6;
        public static final int VERSION_FIELD_NUMBER = 1;
        private static final RuleUpdateResponse defaultInstance = new RuleUpdateResponse(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private List<ColorRule> colorRules_;
        private List<DNTRule> dntRules_;
        private List<HintRule> hintRules_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private int ttl_;
        private Object userId_;
        private Object version_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<RuleUpdateResponse, Builder> implements RuleUpdateResponseOrBuilder {
            private int bitField0_;
            private List<ColorRule> colorRules_ = Collections.emptyList();
            private List<DNTRule> dntRules_ = Collections.emptyList();
            private List<HintRule> hintRules_ = Collections.emptyList();
            private int ttl_;
            private Object userId_ = "";
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
                this.ttl_ = 0;
                this.bitField0_ &= -3;
                this.colorRules_ = Collections.emptyList();
                this.bitField0_ &= -5;
                this.dntRules_ = Collections.emptyList();
                this.bitField0_ &= -9;
                this.hintRules_ = Collections.emptyList();
                this.bitField0_ &= -17;
                this.userId_ = "";
                this.bitField0_ &= -33;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public RuleUpdateResponse getDefaultInstanceForType() {
                return RuleUpdateResponse.getDefaultInstance();
            }

            public RuleUpdateResponse build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public RuleUpdateResponse buildPartial() {
                RuleUpdateResponse ruleUpdateResponse = new RuleUpdateResponse((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                ruleUpdateResponse.version_ = this.version_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                ruleUpdateResponse.ttl_ = this.ttl_;
                if ((this.bitField0_ & 4) == 4) {
                    this.colorRules_ = Collections.unmodifiableList(this.colorRules_);
                    this.bitField0_ &= -5;
                }
                ruleUpdateResponse.colorRules_ = this.colorRules_;
                if ((this.bitField0_ & 8) == 8) {
                    this.dntRules_ = Collections.unmodifiableList(this.dntRules_);
                    this.bitField0_ &= -9;
                }
                ruleUpdateResponse.dntRules_ = this.dntRules_;
                if ((this.bitField0_ & 16) == 16) {
                    this.hintRules_ = Collections.unmodifiableList(this.hintRules_);
                    this.bitField0_ &= -17;
                }
                ruleUpdateResponse.hintRules_ = this.hintRules_;
                if ((i & 32) == 32) {
                    i2 |= 4;
                }
                ruleUpdateResponse.userId_ = this.userId_;
                ruleUpdateResponse.bitField0_ = i2;
                return ruleUpdateResponse;
            }

            public Builder mergeFrom(RuleUpdateResponse ruleUpdateResponse) {
                if (ruleUpdateResponse == RuleUpdateResponse.getDefaultInstance()) {
                    return this;
                }
                if (ruleUpdateResponse.hasVersion()) {
                    this.bitField0_ |= 1;
                    this.version_ = ruleUpdateResponse.version_;
                }
                if (ruleUpdateResponse.hasTtl()) {
                    setTtl(ruleUpdateResponse.getTtl());
                }
                if (!ruleUpdateResponse.colorRules_.isEmpty()) {
                    if (this.colorRules_.isEmpty()) {
                        this.colorRules_ = ruleUpdateResponse.colorRules_;
                        this.bitField0_ &= -5;
                    } else {
                        ensureColorRulesIsMutable();
                        this.colorRules_.addAll(ruleUpdateResponse.colorRules_);
                    }
                }
                if (!ruleUpdateResponse.dntRules_.isEmpty()) {
                    if (this.dntRules_.isEmpty()) {
                        this.dntRules_ = ruleUpdateResponse.dntRules_;
                        this.bitField0_ &= -9;
                    } else {
                        ensureDntRulesIsMutable();
                        this.dntRules_.addAll(ruleUpdateResponse.dntRules_);
                    }
                }
                if (!ruleUpdateResponse.hintRules_.isEmpty()) {
                    if (this.hintRules_.isEmpty()) {
                        this.hintRules_ = ruleUpdateResponse.hintRules_;
                        this.bitField0_ &= -17;
                    } else {
                        ensureHintRulesIsMutable();
                        this.hintRules_.addAll(ruleUpdateResponse.hintRules_);
                    }
                }
                if (ruleUpdateResponse.hasUserId()) {
                    this.bitField0_ |= 32;
                    this.userId_ = ruleUpdateResponse.userId_;
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                RuleUpdateResponse ruleUpdateResponse;
                Throwable th;
                RuleUpdateResponse ruleUpdateResponse2;
                try {
                    ruleUpdateResponse = (RuleUpdateResponse) RuleUpdateResponse.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (ruleUpdateResponse != null) {
                        mergeFrom(ruleUpdateResponse);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    ruleUpdateResponse = (RuleUpdateResponse) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    ruleUpdateResponse2 = ruleUpdateResponse;
                    th = th3;
                }
                if (ruleUpdateResponse2 != null) {
                    mergeFrom(ruleUpdateResponse2);
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
                this.version_ = RuleUpdateResponse.getDefaultInstance().getVersion();
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

            public boolean hasTtl() {
                return (this.bitField0_ & 2) == 2;
            }

            public int getTtl() {
                return this.ttl_;
            }

            public Builder setTtl(int i) {
                this.bitField0_ |= 2;
                this.ttl_ = i;
                return this;
            }

            public Builder clearTtl() {
                this.bitField0_ &= -3;
                this.ttl_ = 0;
                return this;
            }

            private void ensureColorRulesIsMutable() {
                if ((this.bitField0_ & 4) != 4) {
                    this.colorRules_ = new ArrayList(this.colorRules_);
                    this.bitField0_ |= 4;
                }
            }

            public List<ColorRule> getColorRulesList() {
                return Collections.unmodifiableList(this.colorRules_);
            }

            public int getColorRulesCount() {
                return this.colorRules_.size();
            }

            public ColorRule getColorRules(int i) {
                return (ColorRule) this.colorRules_.get(i);
            }

            public Builder setColorRules(int i, ColorRule colorRule) {
                if (colorRule != null) {
                    ensureColorRulesIsMutable();
                    this.colorRules_.set(i, colorRule);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setColorRules(int i, Builder builder) {
                ensureColorRulesIsMutable();
                this.colorRules_.set(i, builder.build());
                return this;
            }

            public Builder addColorRules(ColorRule colorRule) {
                if (colorRule != null) {
                    ensureColorRulesIsMutable();
                    this.colorRules_.add(colorRule);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addColorRules(int i, ColorRule colorRule) {
                if (colorRule != null) {
                    ensureColorRulesIsMutable();
                    this.colorRules_.add(i, colorRule);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addColorRules(Builder builder) {
                ensureColorRulesIsMutable();
                this.colorRules_.add(builder.build());
                return this;
            }

            public Builder addColorRules(int i, Builder builder) {
                ensureColorRulesIsMutable();
                this.colorRules_.add(i, builder.build());
                return this;
            }

            public Builder addAllColorRules(Iterable<? extends ColorRule> iterable) {
                ensureColorRulesIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(iterable, this.colorRules_);
                return this;
            }

            public Builder clearColorRules() {
                this.colorRules_ = Collections.emptyList();
                this.bitField0_ &= -5;
                return this;
            }

            public Builder removeColorRules(int i) {
                ensureColorRulesIsMutable();
                this.colorRules_.remove(i);
                return this;
            }

            private void ensureDntRulesIsMutable() {
                if ((this.bitField0_ & 8) != 8) {
                    this.dntRules_ = new ArrayList(this.dntRules_);
                    this.bitField0_ |= 8;
                }
            }

            public List<DNTRule> getDntRulesList() {
                return Collections.unmodifiableList(this.dntRules_);
            }

            public int getDntRulesCount() {
                return this.dntRules_.size();
            }

            public DNTRule getDntRules(int i) {
                return (DNTRule) this.dntRules_.get(i);
            }

            public Builder setDntRules(int i, DNTRule dNTRule) {
                if (dNTRule != null) {
                    ensureDntRulesIsMutable();
                    this.dntRules_.set(i, dNTRule);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setDntRules(int i, Builder builder) {
                ensureDntRulesIsMutable();
                this.dntRules_.set(i, builder.build());
                return this;
            }

            public Builder addDntRules(DNTRule dNTRule) {
                if (dNTRule != null) {
                    ensureDntRulesIsMutable();
                    this.dntRules_.add(dNTRule);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addDntRules(int i, DNTRule dNTRule) {
                if (dNTRule != null) {
                    ensureDntRulesIsMutable();
                    this.dntRules_.add(i, dNTRule);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addDntRules(Builder builder) {
                ensureDntRulesIsMutable();
                this.dntRules_.add(builder.build());
                return this;
            }

            public Builder addDntRules(int i, Builder builder) {
                ensureDntRulesIsMutable();
                this.dntRules_.add(i, builder.build());
                return this;
            }

            public Builder addAllDntRules(Iterable<? extends DNTRule> iterable) {
                ensureDntRulesIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(iterable, this.dntRules_);
                return this;
            }

            public Builder clearDntRules() {
                this.dntRules_ = Collections.emptyList();
                this.bitField0_ &= -9;
                return this;
            }

            public Builder removeDntRules(int i) {
                ensureDntRulesIsMutable();
                this.dntRules_.remove(i);
                return this;
            }

            private void ensureHintRulesIsMutable() {
                if ((this.bitField0_ & 16) != 16) {
                    this.hintRules_ = new ArrayList(this.hintRules_);
                    this.bitField0_ |= 16;
                }
            }

            public List<HintRule> getHintRulesList() {
                return Collections.unmodifiableList(this.hintRules_);
            }

            public int getHintRulesCount() {
                return this.hintRules_.size();
            }

            public HintRule getHintRules(int i) {
                return (HintRule) this.hintRules_.get(i);
            }

            public Builder setHintRules(int i, HintRule hintRule) {
                if (hintRule != null) {
                    ensureHintRulesIsMutable();
                    this.hintRules_.set(i, hintRule);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setHintRules(int i, Builder builder) {
                ensureHintRulesIsMutable();
                this.hintRules_.set(i, builder.build());
                return this;
            }

            public Builder addHintRules(HintRule hintRule) {
                if (hintRule != null) {
                    ensureHintRulesIsMutable();
                    this.hintRules_.add(hintRule);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addHintRules(int i, HintRule hintRule) {
                if (hintRule != null) {
                    ensureHintRulesIsMutable();
                    this.hintRules_.add(i, hintRule);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addHintRules(Builder builder) {
                ensureHintRulesIsMutable();
                this.hintRules_.add(builder.build());
                return this;
            }

            public Builder addHintRules(int i, Builder builder) {
                ensureHintRulesIsMutable();
                this.hintRules_.add(i, builder.build());
                return this;
            }

            public Builder addAllHintRules(Iterable<? extends HintRule> iterable) {
                ensureHintRulesIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(iterable, this.hintRules_);
                return this;
            }

            public Builder clearHintRules() {
                this.hintRules_ = Collections.emptyList();
                this.bitField0_ &= -17;
                return this;
            }

            public Builder removeHintRules(int i) {
                ensureHintRulesIsMutable();
                this.hintRules_.remove(i);
                return this;
            }

            public boolean hasUserId() {
                return (this.bitField0_ & 32) == 32;
            }

            public String getUserId() {
                Object obj = this.userId_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.userId_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getUserIdBytes() {
                Object obj = this.userId_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.userId_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setUserId(String str) {
                if (str != null) {
                    this.bitField0_ |= 32;
                    this.userId_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearUserId() {
                this.bitField0_ &= -33;
                this.userId_ = RuleUpdateResponse.getDefaultInstance().getUserId();
                return this;
            }

            public Builder setUserIdBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 32;
                    this.userId_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }
        }

        private RuleUpdateResponse(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private RuleUpdateResponse(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static RuleUpdateResponse getDefaultInstance() {
            return defaultInstance;
        }

        public RuleUpdateResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        private RuleUpdateResponse(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                        case 16:
                            this.bitField0_ |= 2;
                            this.ttl_ = codedInputStream.readInt32();
                            break;
                        case 26:
                            if ((i & 4) != 4) {
                                this.colorRules_ = new ArrayList();
                                i |= 4;
                            }
                            this.colorRules_.add(codedInputStream.readMessage(ColorRule.PARSER, extensionRegistryLite));
                            break;
                        case 34:
                            if ((i & 8) != 8) {
                                this.dntRules_ = new ArrayList();
                                i |= 8;
                            }
                            this.dntRules_.add(codedInputStream.readMessage(DNTRule.PARSER, extensionRegistryLite));
                            break;
                        case MotionEventCompat.AXIS_GENERIC_11 /*42*/:
                            if ((i & 16) != 16) {
                                this.hintRules_ = new ArrayList();
                                i |= 16;
                            }
                            this.hintRules_.add(codedInputStream.readMessage(HintRule.PARSER, extensionRegistryLite));
                            break;
                        case 50:
                            this.bitField0_ |= 4;
                            this.userId_ = codedInputStream.readBytes();
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
                        this.colorRules_ = Collections.unmodifiableList(this.colorRules_);
                    }
                    if ((i & 8) == 8) {
                        this.dntRules_ = Collections.unmodifiableList(this.dntRules_);
                    }
                    if ((i & 16) == 16) {
                        this.hintRules_ = Collections.unmodifiableList(this.hintRules_);
                    }
                    makeExtensionsImmutable();
                }
            }
            if ((i & 4) == 4) {
                this.colorRules_ = Collections.unmodifiableList(this.colorRules_);
            }
            if ((i & 8) == 8) {
                this.dntRules_ = Collections.unmodifiableList(this.dntRules_);
            }
            if ((i & 16) == 16) {
                this.hintRules_ = Collections.unmodifiableList(this.hintRules_);
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<RuleUpdateResponse> getParserForType() {
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

        public boolean hasTtl() {
            return (this.bitField0_ & 2) == 2;
        }

        public int getTtl() {
            return this.ttl_;
        }

        public List<ColorRule> getColorRulesList() {
            return this.colorRules_;
        }

        public List<? extends ColorRuleOrBuilder> getColorRulesOrBuilderList() {
            return this.colorRules_;
        }

        public int getColorRulesCount() {
            return this.colorRules_.size();
        }

        public ColorRule getColorRules(int i) {
            return (ColorRule) this.colorRules_.get(i);
        }

        public ColorRuleOrBuilder getColorRulesOrBuilder(int i) {
            return (ColorRuleOrBuilder) this.colorRules_.get(i);
        }

        public List<DNTRule> getDntRulesList() {
            return this.dntRules_;
        }

        public List<? extends DNTRuleOrBuilder> getDntRulesOrBuilderList() {
            return this.dntRules_;
        }

        public int getDntRulesCount() {
            return this.dntRules_.size();
        }

        public DNTRule getDntRules(int i) {
            return (DNTRule) this.dntRules_.get(i);
        }

        public DNTRuleOrBuilder getDntRulesOrBuilder(int i) {
            return (DNTRuleOrBuilder) this.dntRules_.get(i);
        }

        public List<HintRule> getHintRulesList() {
            return this.hintRules_;
        }

        public List<? extends HintRuleOrBuilder> getHintRulesOrBuilderList() {
            return this.hintRules_;
        }

        public int getHintRulesCount() {
            return this.hintRules_.size();
        }

        public HintRule getHintRules(int i) {
            return (HintRule) this.hintRules_.get(i);
        }

        public HintRuleOrBuilder getHintRulesOrBuilder(int i) {
            return (HintRuleOrBuilder) this.hintRules_.get(i);
        }

        public boolean hasUserId() {
            return (this.bitField0_ & 4) == 4;
        }

        public String getUserId() {
            Object obj = this.userId_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.userId_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getUserIdBytes() {
            Object obj = this.userId_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.userId_ = copyFromUtf8;
            return copyFromUtf8;
        }

        private void initFields() {
            this.version_ = "";
            this.ttl_ = 0;
            this.colorRules_ = Collections.emptyList();
            this.dntRules_ = Collections.emptyList();
            this.hintRules_ = Collections.emptyList();
            this.userId_ = "";
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
            int i;
            int i2 = 0;
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeBytes(1, getVersionBytes());
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeInt32(2, this.ttl_);
            }
            for (i = 0; i < this.colorRules_.size(); i++) {
                codedOutputStream.writeMessage(3, (MessageLite) this.colorRules_.get(i));
            }
            for (i = 0; i < this.dntRules_.size(); i++) {
                codedOutputStream.writeMessage(4, (MessageLite) this.dntRules_.get(i));
            }
            while (i2 < this.hintRules_.size()) {
                codedOutputStream.writeMessage(5, (MessageLite) this.hintRules_.get(i2));
                i2++;
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeBytes(6, getUserIdBytes());
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
                i2 = CodedOutputStream.computeBytesSize(1, getVersionBytes()) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i2 += CodedOutputStream.computeInt32Size(2, this.ttl_);
            }
            int i4 = i2;
            for (i3 = 0; i3 < this.colorRules_.size(); i3++) {
                i4 += CodedOutputStream.computeMessageSize(3, (MessageLite) this.colorRules_.get(i3));
            }
            for (i3 = 0; i3 < this.dntRules_.size(); i3++) {
                i4 += CodedOutputStream.computeMessageSize(4, (MessageLite) this.dntRules_.get(i3));
            }
            while (i < this.hintRules_.size()) {
                i4 += CodedOutputStream.computeMessageSize(5, (MessageLite) this.hintRules_.get(i));
                i++;
            }
            if ((this.bitField0_ & 4) == 4) {
                i4 += CodedOutputStream.computeBytesSize(6, getUserIdBytes());
            }
            this.memoizedSerializedSize = i4;
            return i4;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static RuleUpdateResponse parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (RuleUpdateResponse) PARSER.parseFrom(byteString);
        }

        public static RuleUpdateResponse parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (RuleUpdateResponse) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static RuleUpdateResponse parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (RuleUpdateResponse) PARSER.parseFrom(bArr);
        }

        public static RuleUpdateResponse parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (RuleUpdateResponse) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static RuleUpdateResponse parseFrom(InputStream inputStream) throws IOException {
            return (RuleUpdateResponse) PARSER.parseFrom(inputStream);
        }

        public static RuleUpdateResponse parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (RuleUpdateResponse) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static RuleUpdateResponse parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (RuleUpdateResponse) PARSER.parseDelimitedFrom(inputStream);
        }

        public static RuleUpdateResponse parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (RuleUpdateResponse) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static RuleUpdateResponse parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (RuleUpdateResponse) PARSER.parseFrom(codedInputStream);
        }

        public static RuleUpdateResponse parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (RuleUpdateResponse) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(RuleUpdateResponse ruleUpdateResponse) {
            return newBuilder().mergeFrom(ruleUpdateResponse);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    private UrlRule() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite extensionRegistryLite) {
    }
}
