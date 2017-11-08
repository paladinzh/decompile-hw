package com.avast.cloud.webrep.proto;

import com.avast.cloud.webrep.proto.Urlinfo.Client;
import com.avast.cloud.webrep.proto.Urlinfo.EventType;
import com.avast.cloud.webrep.proto.Urlinfo.Identity;
import com.avast.cloud.webrep.proto.Urlinfo.KeyValue;
import com.avast.cloud.webrep.proto.Urlinfo.KeyValueOrBuilder;
import com.avast.cloud.webrep.proto.Urlinfo.OriginType;
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
import com.huawei.systemmanager.comm.widget.CircleViewNew;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.model.RemainingTimeSceneHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UrlLog {

    public interface GeoIpInfoOrBuilder extends MessageLiteOrBuilder {
        String getCity();

        ByteString getCityBytes();

        String getCountryCode();

        ByteString getCountryCodeBytes();

        String getCountryRegion();

        ByteString getCountryRegionBytes();

        double getGpsXCoordinates();

        double getGpsYCoordinates();

        String getTimeshift();

        ByteString getTimeshiftBytes();

        boolean hasCity();

        boolean hasCountryCode();

        boolean hasCountryRegion();

        boolean hasGpsXCoordinates();

        boolean hasGpsYCoordinates();

        boolean hasTimeshift();
    }

    public static final class GeoIpInfo extends GeneratedMessageLite implements GeoIpInfoOrBuilder {
        public static final int CITY_FIELD_NUMBER = 3;
        public static final int COUNTRYCODE_FIELD_NUMBER = 1;
        public static final int COUNTRYREGION_FIELD_NUMBER = 2;
        public static final int GPSXCOORDINATES_FIELD_NUMBER = 4;
        public static final int GPSYCOORDINATES_FIELD_NUMBER = 5;
        public static Parser<GeoIpInfo> PARSER = new AbstractParser<GeoIpInfo>() {
            public GeoIpInfo parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new GeoIpInfo(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int TIMESHIFT_FIELD_NUMBER = 6;
        private static final GeoIpInfo defaultInstance = new GeoIpInfo(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private Object city_;
        private Object countryCode_;
        private Object countryRegion_;
        private double gpsXCoordinates_;
        private double gpsYCoordinates_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private Object timeshift_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<GeoIpInfo, Builder> implements GeoIpInfoOrBuilder {
            private int bitField0_;
            private Object city_ = "";
            private Object countryCode_ = "";
            private Object countryRegion_ = "";
            private double gpsXCoordinates_;
            private double gpsYCoordinates_;
            private Object timeshift_ = "";

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
                this.countryCode_ = "";
                this.bitField0_ &= -2;
                this.countryRegion_ = "";
                this.bitField0_ &= -3;
                this.city_ = "";
                this.bitField0_ &= -5;
                this.gpsXCoordinates_ = 0.0d;
                this.bitField0_ &= -9;
                this.gpsYCoordinates_ = 0.0d;
                this.bitField0_ &= -17;
                this.timeshift_ = "";
                this.bitField0_ &= -33;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public GeoIpInfo getDefaultInstanceForType() {
                return GeoIpInfo.getDefaultInstance();
            }

            public GeoIpInfo build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public GeoIpInfo buildPartial() {
                GeoIpInfo geoIpInfo = new GeoIpInfo((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                geoIpInfo.countryCode_ = this.countryCode_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                geoIpInfo.countryRegion_ = this.countryRegion_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                geoIpInfo.city_ = this.city_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                geoIpInfo.gpsXCoordinates_ = this.gpsXCoordinates_;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                geoIpInfo.gpsYCoordinates_ = this.gpsYCoordinates_;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                geoIpInfo.timeshift_ = this.timeshift_;
                geoIpInfo.bitField0_ = i2;
                return geoIpInfo;
            }

            public Builder mergeFrom(GeoIpInfo geoIpInfo) {
                if (geoIpInfo == GeoIpInfo.getDefaultInstance()) {
                    return this;
                }
                if (geoIpInfo.hasCountryCode()) {
                    this.bitField0_ |= 1;
                    this.countryCode_ = geoIpInfo.countryCode_;
                }
                if (geoIpInfo.hasCountryRegion()) {
                    this.bitField0_ |= 2;
                    this.countryRegion_ = geoIpInfo.countryRegion_;
                }
                if (geoIpInfo.hasCity()) {
                    this.bitField0_ |= 4;
                    this.city_ = geoIpInfo.city_;
                }
                if (geoIpInfo.hasGpsXCoordinates()) {
                    setGpsXCoordinates(geoIpInfo.getGpsXCoordinates());
                }
                if (geoIpInfo.hasGpsYCoordinates()) {
                    setGpsYCoordinates(geoIpInfo.getGpsYCoordinates());
                }
                if (geoIpInfo.hasTimeshift()) {
                    this.bitField0_ |= 32;
                    this.timeshift_ = geoIpInfo.timeshift_;
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                GeoIpInfo geoIpInfo;
                GeoIpInfo geoIpInfo2;
                try {
                    geoIpInfo2 = (GeoIpInfo) GeoIpInfo.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (geoIpInfo2 != null) {
                        mergeFrom(geoIpInfo2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    geoIpInfo2 = (GeoIpInfo) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    geoIpInfo = geoIpInfo2;
                    th = th3;
                }
                if (geoIpInfo != null) {
                    mergeFrom(geoIpInfo);
                }
                throw th;
            }

            public boolean hasCountryCode() {
                return (this.bitField0_ & 1) == 1;
            }

            public String getCountryCode() {
                Object obj = this.countryCode_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.countryCode_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getCountryCodeBytes() {
                Object obj = this.countryCode_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.countryCode_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setCountryCode(String str) {
                if (str != null) {
                    this.bitField0_ |= 1;
                    this.countryCode_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearCountryCode() {
                this.bitField0_ &= -2;
                this.countryCode_ = GeoIpInfo.getDefaultInstance().getCountryCode();
                return this;
            }

            public Builder setCountryCodeBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.countryCode_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasCountryRegion() {
                return (this.bitField0_ & 2) == 2;
            }

            public String getCountryRegion() {
                Object obj = this.countryRegion_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.countryRegion_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getCountryRegionBytes() {
                Object obj = this.countryRegion_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.countryRegion_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setCountryRegion(String str) {
                if (str != null) {
                    this.bitField0_ |= 2;
                    this.countryRegion_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearCountryRegion() {
                this.bitField0_ &= -3;
                this.countryRegion_ = GeoIpInfo.getDefaultInstance().getCountryRegion();
                return this;
            }

            public Builder setCountryRegionBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 2;
                    this.countryRegion_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasCity() {
                return (this.bitField0_ & 4) == 4;
            }

            public String getCity() {
                Object obj = this.city_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.city_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getCityBytes() {
                Object obj = this.city_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.city_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setCity(String str) {
                if (str != null) {
                    this.bitField0_ |= 4;
                    this.city_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearCity() {
                this.bitField0_ &= -5;
                this.city_ = GeoIpInfo.getDefaultInstance().getCity();
                return this;
            }

            public Builder setCityBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 4;
                    this.city_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasGpsXCoordinates() {
                return (this.bitField0_ & 8) == 8;
            }

            public double getGpsXCoordinates() {
                return this.gpsXCoordinates_;
            }

            public Builder setGpsXCoordinates(double d) {
                this.bitField0_ |= 8;
                this.gpsXCoordinates_ = d;
                return this;
            }

            public Builder clearGpsXCoordinates() {
                this.bitField0_ &= -9;
                this.gpsXCoordinates_ = 0.0d;
                return this;
            }

            public boolean hasGpsYCoordinates() {
                return (this.bitField0_ & 16) == 16;
            }

            public double getGpsYCoordinates() {
                return this.gpsYCoordinates_;
            }

            public Builder setGpsYCoordinates(double d) {
                this.bitField0_ |= 16;
                this.gpsYCoordinates_ = d;
                return this;
            }

            public Builder clearGpsYCoordinates() {
                this.bitField0_ &= -17;
                this.gpsYCoordinates_ = 0.0d;
                return this;
            }

            public boolean hasTimeshift() {
                return (this.bitField0_ & 32) == 32;
            }

            public String getTimeshift() {
                Object obj = this.timeshift_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.timeshift_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getTimeshiftBytes() {
                Object obj = this.timeshift_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.timeshift_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setTimeshift(String str) {
                if (str != null) {
                    this.bitField0_ |= 32;
                    this.timeshift_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearTimeshift() {
                this.bitField0_ &= -33;
                this.timeshift_ = GeoIpInfo.getDefaultInstance().getTimeshift();
                return this;
            }

            public Builder setTimeshiftBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 32;
                    this.timeshift_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }
        }

        private GeoIpInfo(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private GeoIpInfo(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static GeoIpInfo getDefaultInstance() {
            return defaultInstance;
        }

        public GeoIpInfo getDefaultInstanceForType() {
            return defaultInstance;
        }

        private GeoIpInfo(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            this.countryCode_ = codedInputStream.readBytes();
                            break;
                        case 18:
                            this.bitField0_ |= 2;
                            this.countryRegion_ = codedInputStream.readBytes();
                            break;
                        case 26:
                            this.bitField0_ |= 4;
                            this.city_ = codedInputStream.readBytes();
                            break;
                        case 33:
                            this.bitField0_ |= 8;
                            this.gpsXCoordinates_ = codedInputStream.readDouble();
                            break;
                        case 41:
                            this.bitField0_ |= 16;
                            this.gpsYCoordinates_ = codedInputStream.readDouble();
                            break;
                        case 50:
                            this.bitField0_ |= 32;
                            this.timeshift_ = codedInputStream.readBytes();
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

        public Parser<GeoIpInfo> getParserForType() {
            return PARSER;
        }

        public boolean hasCountryCode() {
            return (this.bitField0_ & 1) == 1;
        }

        public String getCountryCode() {
            Object obj = this.countryCode_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.countryCode_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getCountryCodeBytes() {
            Object obj = this.countryCode_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.countryCode_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasCountryRegion() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getCountryRegion() {
            Object obj = this.countryRegion_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.countryRegion_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getCountryRegionBytes() {
            Object obj = this.countryRegion_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.countryRegion_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasCity() {
            return (this.bitField0_ & 4) == 4;
        }

        public String getCity() {
            Object obj = this.city_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.city_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getCityBytes() {
            Object obj = this.city_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.city_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasGpsXCoordinates() {
            return (this.bitField0_ & 8) == 8;
        }

        public double getGpsXCoordinates() {
            return this.gpsXCoordinates_;
        }

        public boolean hasGpsYCoordinates() {
            return (this.bitField0_ & 16) == 16;
        }

        public double getGpsYCoordinates() {
            return this.gpsYCoordinates_;
        }

        public boolean hasTimeshift() {
            return (this.bitField0_ & 32) == 32;
        }

        public String getTimeshift() {
            Object obj = this.timeshift_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.timeshift_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getTimeshiftBytes() {
            Object obj = this.timeshift_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.timeshift_ = copyFromUtf8;
            return copyFromUtf8;
        }

        private void initFields() {
            this.countryCode_ = "";
            this.countryRegion_ = "";
            this.city_ = "";
            this.gpsXCoordinates_ = 0.0d;
            this.gpsYCoordinates_ = 0.0d;
            this.timeshift_ = "";
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
                codedOutputStream.writeBytes(1, getCountryCodeBytes());
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeBytes(2, getCountryRegionBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeBytes(3, getCityBytes());
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeDouble(4, this.gpsXCoordinates_);
            }
            if ((this.bitField0_ & 16) == 16) {
                codedOutputStream.writeDouble(5, this.gpsYCoordinates_);
            }
            if ((this.bitField0_ & 32) == 32) {
                codedOutputStream.writeBytes(6, getTimeshiftBytes());
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, getCountryCodeBytes()) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, getCountryRegionBytes());
            }
            if ((this.bitField0_ & 4) == 4) {
                i += CodedOutputStream.computeBytesSize(3, getCityBytes());
            }
            if ((this.bitField0_ & 8) == 8) {
                i += CodedOutputStream.computeDoubleSize(4, this.gpsXCoordinates_);
            }
            if ((this.bitField0_ & 16) == 16) {
                i += CodedOutputStream.computeDoubleSize(5, this.gpsYCoordinates_);
            }
            if ((this.bitField0_ & 32) == 32) {
                i += CodedOutputStream.computeBytesSize(6, getTimeshiftBytes());
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static GeoIpInfo parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (GeoIpInfo) PARSER.parseFrom(byteString);
        }

        public static GeoIpInfo parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (GeoIpInfo) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static GeoIpInfo parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (GeoIpInfo) PARSER.parseFrom(bArr);
        }

        public static GeoIpInfo parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (GeoIpInfo) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static GeoIpInfo parseFrom(InputStream inputStream) throws IOException {
            return (GeoIpInfo) PARSER.parseFrom(inputStream);
        }

        public static GeoIpInfo parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (GeoIpInfo) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static GeoIpInfo parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (GeoIpInfo) PARSER.parseDelimitedFrom(inputStream);
        }

        public static GeoIpInfo parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (GeoIpInfo) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static GeoIpInfo parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (GeoIpInfo) PARSER.parseFrom(codedInputStream);
        }

        public static GeoIpInfo parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (GeoIpInfo) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(GeoIpInfo geoIpInfo) {
            return newBuilder().mergeFrom(geoIpInfo);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface UrlLogDataPacketResponseOrBuilder extends MessageLiteOrBuilder {
        ReponseStatusCode getStatus();

        boolean hasStatus();
    }

    public static final class UrlLogDataPacketResponse extends GeneratedMessageLite implements UrlLogDataPacketResponseOrBuilder {
        public static Parser<UrlLogDataPacketResponse> PARSER = new AbstractParser<UrlLogDataPacketResponse>() {
            public UrlLogDataPacketResponse parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new UrlLogDataPacketResponse(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int STATUS_FIELD_NUMBER = 1;
        private static final UrlLogDataPacketResponse defaultInstance = new UrlLogDataPacketResponse(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private ReponseStatusCode status_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<UrlLogDataPacketResponse, Builder> implements UrlLogDataPacketResponseOrBuilder {
            private int bitField0_;
            private ReponseStatusCode status_ = ReponseStatusCode.OK;

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
                this.status_ = ReponseStatusCode.OK;
                this.bitField0_ &= -2;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public UrlLogDataPacketResponse getDefaultInstanceForType() {
                return UrlLogDataPacketResponse.getDefaultInstance();
            }

            public UrlLogDataPacketResponse build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public UrlLogDataPacketResponse buildPartial() {
                UrlLogDataPacketResponse urlLogDataPacketResponse = new UrlLogDataPacketResponse((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = 0;
                if ((this.bitField0_ & 1) == 1) {
                    i = 1;
                }
                urlLogDataPacketResponse.status_ = this.status_;
                urlLogDataPacketResponse.bitField0_ = i;
                return urlLogDataPacketResponse;
            }

            public Builder mergeFrom(UrlLogDataPacketResponse urlLogDataPacketResponse) {
                if (urlLogDataPacketResponse != UrlLogDataPacketResponse.getDefaultInstance() && urlLogDataPacketResponse.hasStatus()) {
                    setStatus(urlLogDataPacketResponse.getStatus());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                UrlLogDataPacketResponse urlLogDataPacketResponse;
                UrlLogDataPacketResponse urlLogDataPacketResponse2;
                try {
                    urlLogDataPacketResponse2 = (UrlLogDataPacketResponse) UrlLogDataPacketResponse.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (urlLogDataPacketResponse2 != null) {
                        mergeFrom(urlLogDataPacketResponse2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    urlLogDataPacketResponse2 = (UrlLogDataPacketResponse) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    urlLogDataPacketResponse = urlLogDataPacketResponse2;
                    th = th3;
                }
                if (urlLogDataPacketResponse != null) {
                    mergeFrom(urlLogDataPacketResponse);
                }
                throw th;
            }

            public boolean hasStatus() {
                return (this.bitField0_ & 1) == 1;
            }

            public ReponseStatusCode getStatus() {
                return this.status_;
            }

            public Builder setStatus(ReponseStatusCode reponseStatusCode) {
                if (reponseStatusCode != null) {
                    this.bitField0_ |= 1;
                    this.status_ = reponseStatusCode;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearStatus() {
                this.bitField0_ &= -2;
                this.status_ = ReponseStatusCode.OK;
                return this;
            }
        }

        public enum ReponseStatusCode implements EnumLite {
            OK(0, 0),
            ERROR(1, 1);
            
            public static final int ERROR_VALUE = 1;
            public static final int OK_VALUE = 0;
            private static EnumLiteMap<ReponseStatusCode> internalValueMap;
            private final int value;

            static {
                internalValueMap = new EnumLiteMap<ReponseStatusCode>() {
                    public ReponseStatusCode findValueByNumber(int i) {
                        return ReponseStatusCode.valueOf(i);
                    }
                };
            }

            public final int getNumber() {
                return this.value;
            }

            public static ReponseStatusCode valueOf(int i) {
                switch (i) {
                    case 0:
                        return OK;
                    case 1:
                        return ERROR;
                    default:
                        return null;
                }
            }

            public static EnumLiteMap<ReponseStatusCode> internalGetValueMap() {
                return internalValueMap;
            }

            private ReponseStatusCode(int i, int i2) {
                this.value = i2;
            }
        }

        private UrlLogDataPacketResponse(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private UrlLogDataPacketResponse(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static UrlLogDataPacketResponse getDefaultInstance() {
            return defaultInstance;
        }

        public UrlLogDataPacketResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        private UrlLogDataPacketResponse(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            ReponseStatusCode valueOf = ReponseStatusCode.valueOf(codedInputStream.readEnum());
                            if (valueOf == null) {
                                break;
                            }
                            this.bitField0_ |= 1;
                            this.status_ = valueOf;
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

        public Parser<UrlLogDataPacketResponse> getParserForType() {
            return PARSER;
        }

        public boolean hasStatus() {
            return (this.bitField0_ & 1) == 1;
        }

        public ReponseStatusCode getStatus() {
            return this.status_;
        }

        private void initFields() {
            this.status_ = ReponseStatusCode.OK;
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
                codedOutputStream.writeEnum(1, this.status_.getNumber());
            }
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.memoizedSerializedSize;
            if (i2 != -1) {
                return i2;
            }
            if ((this.bitField0_ & 1) == 1) {
                i = CodedOutputStream.computeEnumSize(1, this.status_.getNumber()) + 0;
            }
            this.memoizedSerializedSize = i;
            return i;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static UrlLogDataPacketResponse parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (UrlLogDataPacketResponse) PARSER.parseFrom(byteString);
        }

        public static UrlLogDataPacketResponse parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UrlLogDataPacketResponse) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static UrlLogDataPacketResponse parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (UrlLogDataPacketResponse) PARSER.parseFrom(bArr);
        }

        public static UrlLogDataPacketResponse parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UrlLogDataPacketResponse) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static UrlLogDataPacketResponse parseFrom(InputStream inputStream) throws IOException {
            return (UrlLogDataPacketResponse) PARSER.parseFrom(inputStream);
        }

        public static UrlLogDataPacketResponse parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlLogDataPacketResponse) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static UrlLogDataPacketResponse parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (UrlLogDataPacketResponse) PARSER.parseDelimitedFrom(inputStream);
        }

        public static UrlLogDataPacketResponse parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlLogDataPacketResponse) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static UrlLogDataPacketResponse parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (UrlLogDataPacketResponse) PARSER.parseFrom(codedInputStream);
        }

        public static UrlLogDataPacketResponse parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlLogDataPacketResponse) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(UrlLogDataPacketResponse urlLogDataPacketResponse) {
            return newBuilder().mergeFrom(urlLogDataPacketResponse);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface UrlLogEventOrBuilder extends MessageLiteOrBuilder {
        long getCallerId();

        Client getClient();

        ByteString getCompressedUri();

        CompressionMethod getCompressionMethod();

        KeyValue getCustomKeyValue(int i);

        int getCustomKeyValueCount();

        List<KeyValue> getCustomKeyValueList();

        boolean getDnl();

        GeoIpInfo getGeoipInfo();

        Identity getIdentity();

        ByteString getIp();

        OriginType getOrigin();

        String getReferer();

        ByteString getRefererBytes();

        String getRegion();

        ByteString getRegionBytes();

        ByteString getReserved();

        int getTabNum();

        long getTimestamp();

        String getUri();

        ByteString getUriBytes();

        int getVersion();

        EventType getWindowEvent();

        int getWindowNum();

        boolean hasCallerId();

        boolean hasClient();

        boolean hasCompressedUri();

        boolean hasCompressionMethod();

        boolean hasDnl();

        boolean hasGeoipInfo();

        boolean hasIdentity();

        boolean hasIp();

        boolean hasOrigin();

        boolean hasReferer();

        boolean hasRegion();

        boolean hasReserved();

        boolean hasTabNum();

        boolean hasTimestamp();

        boolean hasUri();

        boolean hasVersion();

        boolean hasWindowEvent();

        boolean hasWindowNum();
    }

    public static final class UrlLogEvent extends GeneratedMessageLite implements UrlLogEventOrBuilder {
        public static final int CALLERID_FIELD_NUMBER = 5;
        public static final int CLIENT_FIELD_NUMBER = 19;
        public static final int COMPRESSEDURI_FIELD_NUMBER = 7;
        public static final int COMPRESSIONMETHOD_FIELD_NUMBER = 8;
        public static final int CUSTOMKEYVALUE_FIELD_NUMBER = 9;
        public static final int DNL_FIELD_NUMBER = 15;
        public static final int GEOIPINFO_FIELD_NUMBER = 17;
        public static final int IDENTITY_FIELD_NUMBER = 6;
        public static final int IP_FIELD_NUMBER = 3;
        public static final int ORIGIN_FIELD_NUMBER = 14;
        public static Parser<UrlLogEvent> PARSER = new AbstractParser<UrlLogEvent>() {
            public UrlLogEvent parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new UrlLogEvent(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int REFERER_FIELD_NUMBER = 10;
        public static final int REGION_FIELD_NUMBER = 4;
        public static final int RESERVED_FIELD_NUMBER = 16;
        public static final int TABNUM_FIELD_NUMBER = 12;
        public static final int TIMESTAMP_FIELD_NUMBER = 2;
        public static final int URI_FIELD_NUMBER = 1;
        public static final int VERSION_FIELD_NUMBER = 18;
        public static final int WINDOWEVENT_FIELD_NUMBER = 13;
        public static final int WINDOWNUM_FIELD_NUMBER = 11;
        private static final UrlLogEvent defaultInstance = new UrlLogEvent(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private long callerId_;
        private Client client_;
        private ByteString compressedUri_;
        private CompressionMethod compressionMethod_;
        private List<KeyValue> customKeyValue_;
        private boolean dnl_;
        private GeoIpInfo geoipInfo_;
        private Identity identity_;
        private ByteString ip_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private OriginType origin_;
        private Object referer_;
        private Object region_;
        private ByteString reserved_;
        private int tabNum_;
        private long timestamp_;
        private Object uri_;
        private int version_;
        private EventType windowEvent_;
        private int windowNum_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<UrlLogEvent, Builder> implements UrlLogEventOrBuilder {
            private int bitField0_;
            private long callerId_;
            private Client client_ = Client.getDefaultInstance();
            private ByteString compressedUri_ = ByteString.EMPTY;
            private CompressionMethod compressionMethod_ = CompressionMethod.NONE;
            private List<KeyValue> customKeyValue_ = Collections.emptyList();
            private boolean dnl_;
            private GeoIpInfo geoipInfo_ = GeoIpInfo.getDefaultInstance();
            private Identity identity_ = Identity.getDefaultInstance();
            private ByteString ip_ = ByteString.EMPTY;
            private OriginType origin_ = OriginType.LINK;
            private Object referer_ = "";
            private Object region_ = "";
            private ByteString reserved_ = ByteString.EMPTY;
            private int tabNum_;
            private long timestamp_;
            private Object uri_ = "";
            private int version_;
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
                this.uri_ = "";
                this.bitField0_ &= -2;
                this.timestamp_ = 0;
                this.bitField0_ &= -3;
                this.ip_ = ByteString.EMPTY;
                this.bitField0_ &= -5;
                this.region_ = "";
                this.bitField0_ &= -9;
                this.callerId_ = 0;
                this.bitField0_ &= -17;
                this.identity_ = Identity.getDefaultInstance();
                this.bitField0_ &= -33;
                this.compressedUri_ = ByteString.EMPTY;
                this.bitField0_ &= -65;
                this.compressionMethod_ = CompressionMethod.NONE;
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
                this.geoipInfo_ = GeoIpInfo.getDefaultInstance();
                this.bitField0_ &= -65537;
                this.version_ = 0;
                this.bitField0_ &= -131073;
                this.client_ = Client.getDefaultInstance();
                this.bitField0_ &= -262145;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public UrlLogEvent getDefaultInstanceForType() {
                return UrlLogEvent.getDefaultInstance();
            }

            public UrlLogEvent build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public UrlLogEvent buildPartial() {
                UrlLogEvent urlLogEvent = new UrlLogEvent((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                urlLogEvent.uri_ = this.uri_;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                urlLogEvent.timestamp_ = this.timestamp_;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                urlLogEvent.ip_ = this.ip_;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                urlLogEvent.region_ = this.region_;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                urlLogEvent.callerId_ = this.callerId_;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                urlLogEvent.identity_ = this.identity_;
                if ((i & 64) == 64) {
                    i2 |= 64;
                }
                urlLogEvent.compressedUri_ = this.compressedUri_;
                if ((i & 128) == 128) {
                    i2 |= 128;
                }
                urlLogEvent.compressionMethod_ = this.compressionMethod_;
                if ((this.bitField0_ & 256) == 256) {
                    this.customKeyValue_ = Collections.unmodifiableList(this.customKeyValue_);
                    this.bitField0_ &= -257;
                }
                urlLogEvent.customKeyValue_ = this.customKeyValue_;
                if ((i & 512) == 512) {
                    i2 |= 256;
                }
                urlLogEvent.referer_ = this.referer_;
                if ((i & 1024) == 1024) {
                    i2 |= 512;
                }
                urlLogEvent.windowNum_ = this.windowNum_;
                if ((i & 2048) == 2048) {
                    i2 |= 1024;
                }
                urlLogEvent.tabNum_ = this.tabNum_;
                if ((i & 4096) == 4096) {
                    i2 |= 2048;
                }
                urlLogEvent.windowEvent_ = this.windowEvent_;
                if ((i & 8192) == 8192) {
                    i2 |= 4096;
                }
                urlLogEvent.origin_ = this.origin_;
                if ((i & 16384) == 16384) {
                    i2 |= 8192;
                }
                urlLogEvent.dnl_ = this.dnl_;
                if ((i & 32768) == 32768) {
                    i2 |= 16384;
                }
                urlLogEvent.reserved_ = this.reserved_;
                if ((i & 65536) == 65536) {
                    i2 |= 32768;
                }
                urlLogEvent.geoipInfo_ = this.geoipInfo_;
                if ((i & 131072) == 131072) {
                    i2 |= 65536;
                }
                urlLogEvent.version_ = this.version_;
                if ((i & 262144) == 262144) {
                    i2 |= 131072;
                }
                urlLogEvent.client_ = this.client_;
                urlLogEvent.bitField0_ = i2;
                return urlLogEvent;
            }

            public Builder mergeFrom(UrlLogEvent urlLogEvent) {
                if (urlLogEvent == UrlLogEvent.getDefaultInstance()) {
                    return this;
                }
                if (urlLogEvent.hasUri()) {
                    this.bitField0_ |= 1;
                    this.uri_ = urlLogEvent.uri_;
                }
                if (urlLogEvent.hasTimestamp()) {
                    setTimestamp(urlLogEvent.getTimestamp());
                }
                if (urlLogEvent.hasIp()) {
                    setIp(urlLogEvent.getIp());
                }
                if (urlLogEvent.hasRegion()) {
                    this.bitField0_ |= 8;
                    this.region_ = urlLogEvent.region_;
                }
                if (urlLogEvent.hasCallerId()) {
                    setCallerId(urlLogEvent.getCallerId());
                }
                if (urlLogEvent.hasIdentity()) {
                    mergeIdentity(urlLogEvent.getIdentity());
                }
                if (urlLogEvent.hasCompressedUri()) {
                    setCompressedUri(urlLogEvent.getCompressedUri());
                }
                if (urlLogEvent.hasCompressionMethod()) {
                    setCompressionMethod(urlLogEvent.getCompressionMethod());
                }
                if (!urlLogEvent.customKeyValue_.isEmpty()) {
                    if (this.customKeyValue_.isEmpty()) {
                        this.customKeyValue_ = urlLogEvent.customKeyValue_;
                        this.bitField0_ &= -257;
                    } else {
                        ensureCustomKeyValueIsMutable();
                        this.customKeyValue_.addAll(urlLogEvent.customKeyValue_);
                    }
                }
                if (urlLogEvent.hasReferer()) {
                    this.bitField0_ |= 512;
                    this.referer_ = urlLogEvent.referer_;
                }
                if (urlLogEvent.hasWindowNum()) {
                    setWindowNum(urlLogEvent.getWindowNum());
                }
                if (urlLogEvent.hasTabNum()) {
                    setTabNum(urlLogEvent.getTabNum());
                }
                if (urlLogEvent.hasWindowEvent()) {
                    setWindowEvent(urlLogEvent.getWindowEvent());
                }
                if (urlLogEvent.hasOrigin()) {
                    setOrigin(urlLogEvent.getOrigin());
                }
                if (urlLogEvent.hasDnl()) {
                    setDnl(urlLogEvent.getDnl());
                }
                if (urlLogEvent.hasReserved()) {
                    setReserved(urlLogEvent.getReserved());
                }
                if (urlLogEvent.hasGeoipInfo()) {
                    mergeGeoipInfo(urlLogEvent.getGeoipInfo());
                }
                if (urlLogEvent.hasVersion()) {
                    setVersion(urlLogEvent.getVersion());
                }
                if (urlLogEvent.hasClient()) {
                    mergeClient(urlLogEvent.getClient());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                UrlLogEvent urlLogEvent;
                Throwable th;
                UrlLogEvent urlLogEvent2;
                try {
                    urlLogEvent = (UrlLogEvent) UrlLogEvent.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (urlLogEvent != null) {
                        mergeFrom(urlLogEvent);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    urlLogEvent = (UrlLogEvent) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    urlLogEvent2 = urlLogEvent;
                    th = th3;
                }
                if (urlLogEvent2 != null) {
                    mergeFrom(urlLogEvent2);
                }
                throw th;
            }

            public boolean hasUri() {
                return (this.bitField0_ & 1) == 1;
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
                    this.bitField0_ |= 1;
                    this.uri_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearUri() {
                this.bitField0_ &= -2;
                this.uri_ = UrlLogEvent.getDefaultInstance().getUri();
                return this;
            }

            public Builder setUriBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 1;
                    this.uri_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasTimestamp() {
                return (this.bitField0_ & 2) == 2;
            }

            public long getTimestamp() {
                return this.timestamp_;
            }

            public Builder setTimestamp(long j) {
                this.bitField0_ |= 2;
                this.timestamp_ = j;
                return this;
            }

            public Builder clearTimestamp() {
                this.bitField0_ &= -3;
                this.timestamp_ = 0;
                return this;
            }

            public boolean hasIp() {
                return (this.bitField0_ & 4) == 4;
            }

            public ByteString getIp() {
                return this.ip_;
            }

            public Builder setIp(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 4;
                    this.ip_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearIp() {
                this.bitField0_ &= -5;
                this.ip_ = UrlLogEvent.getDefaultInstance().getIp();
                return this;
            }

            public boolean hasRegion() {
                return (this.bitField0_ & 8) == 8;
            }

            public String getRegion() {
                Object obj = this.region_;
                if (obj instanceof String) {
                    return (String) obj;
                }
                String toStringUtf8 = ((ByteString) obj).toStringUtf8();
                this.region_ = toStringUtf8;
                return toStringUtf8;
            }

            public ByteString getRegionBytes() {
                Object obj = this.region_;
                if (!(obj instanceof String)) {
                    return (ByteString) obj;
                }
                ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
                this.region_ = copyFromUtf8;
                return copyFromUtf8;
            }

            public Builder setRegion(String str) {
                if (str != null) {
                    this.bitField0_ |= 8;
                    this.region_ = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearRegion() {
                this.bitField0_ &= -9;
                this.region_ = UrlLogEvent.getDefaultInstance().getRegion();
                return this;
            }

            public Builder setRegionBytes(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 8;
                    this.region_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public boolean hasCallerId() {
                return (this.bitField0_ & 16) == 16;
            }

            public long getCallerId() {
                return this.callerId_;
            }

            public Builder setCallerId(long j) {
                this.bitField0_ |= 16;
                this.callerId_ = j;
                return this;
            }

            public Builder clearCallerId() {
                this.bitField0_ &= -17;
                this.callerId_ = 0;
                return this;
            }

            public boolean hasIdentity() {
                return (this.bitField0_ & 32) == 32;
            }

            public Identity getIdentity() {
                return this.identity_;
            }

            public Builder setIdentity(Identity identity) {
                if (identity != null) {
                    this.identity_ = identity;
                    this.bitField0_ |= 32;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setIdentity(com.avast.cloud.webrep.proto.Urlinfo.Identity.Builder builder) {
                this.identity_ = builder.build();
                this.bitField0_ |= 32;
                return this;
            }

            public Builder mergeIdentity(Identity identity) {
                if ((this.bitField0_ & 32) == 32 && this.identity_ != Identity.getDefaultInstance()) {
                    this.identity_ = Identity.newBuilder(this.identity_).mergeFrom(identity).buildPartial();
                } else {
                    this.identity_ = identity;
                }
                this.bitField0_ |= 32;
                return this;
            }

            public Builder clearIdentity() {
                this.identity_ = Identity.getDefaultInstance();
                this.bitField0_ &= -33;
                return this;
            }

            public boolean hasCompressedUri() {
                return (this.bitField0_ & 64) == 64;
            }

            public ByteString getCompressedUri() {
                return this.compressedUri_;
            }

            public Builder setCompressedUri(ByteString byteString) {
                if (byteString != null) {
                    this.bitField0_ |= 64;
                    this.compressedUri_ = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearCompressedUri() {
                this.bitField0_ &= -65;
                this.compressedUri_ = UrlLogEvent.getDefaultInstance().getCompressedUri();
                return this;
            }

            public boolean hasCompressionMethod() {
                return (this.bitField0_ & 128) == 128;
            }

            public CompressionMethod getCompressionMethod() {
                return this.compressionMethod_;
            }

            public Builder setCompressionMethod(CompressionMethod compressionMethod) {
                if (compressionMethod != null) {
                    this.bitField0_ |= 128;
                    this.compressionMethod_ = compressionMethod;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder clearCompressionMethod() {
                this.bitField0_ &= -129;
                this.compressionMethod_ = CompressionMethod.NONE;
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

            public Builder setCustomKeyValue(int i, com.avast.cloud.webrep.proto.Urlinfo.KeyValue.Builder builder) {
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

            public Builder addCustomKeyValue(com.avast.cloud.webrep.proto.Urlinfo.KeyValue.Builder builder) {
                ensureCustomKeyValueIsMutable();
                this.customKeyValue_.add(builder.build());
                return this;
            }

            public Builder addCustomKeyValue(int i, com.avast.cloud.webrep.proto.Urlinfo.KeyValue.Builder builder) {
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
                this.referer_ = UrlLogEvent.getDefaultInstance().getReferer();
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
                this.reserved_ = UrlLogEvent.getDefaultInstance().getReserved();
                return this;
            }

            public boolean hasGeoipInfo() {
                return (this.bitField0_ & 65536) == 65536;
            }

            public GeoIpInfo getGeoipInfo() {
                return this.geoipInfo_;
            }

            public Builder setGeoipInfo(GeoIpInfo geoIpInfo) {
                if (geoIpInfo != null) {
                    this.geoipInfo_ = geoIpInfo;
                    this.bitField0_ |= 65536;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setGeoipInfo(Builder builder) {
                this.geoipInfo_ = builder.build();
                this.bitField0_ |= 65536;
                return this;
            }

            public Builder mergeGeoipInfo(GeoIpInfo geoIpInfo) {
                if ((this.bitField0_ & 65536) == 65536 && this.geoipInfo_ != GeoIpInfo.getDefaultInstance()) {
                    this.geoipInfo_ = GeoIpInfo.newBuilder(this.geoipInfo_).mergeFrom(geoIpInfo).buildPartial();
                } else {
                    this.geoipInfo_ = geoIpInfo;
                }
                this.bitField0_ |= 65536;
                return this;
            }

            public Builder clearGeoipInfo() {
                this.geoipInfo_ = GeoIpInfo.getDefaultInstance();
                this.bitField0_ &= -65537;
                return this;
            }

            public boolean hasVersion() {
                return (this.bitField0_ & 131072) == 131072;
            }

            public int getVersion() {
                return this.version_;
            }

            public Builder setVersion(int i) {
                this.bitField0_ |= 131072;
                this.version_ = i;
                return this;
            }

            public Builder clearVersion() {
                this.bitField0_ &= -131073;
                this.version_ = 0;
                return this;
            }

            public boolean hasClient() {
                return (this.bitField0_ & 262144) == 262144;
            }

            public Client getClient() {
                return this.client_;
            }

            public Builder setClient(Client client) {
                if (client != null) {
                    this.client_ = client;
                    this.bitField0_ |= 262144;
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setClient(com.avast.cloud.webrep.proto.Urlinfo.Client.Builder builder) {
                this.client_ = builder.build();
                this.bitField0_ |= 262144;
                return this;
            }

            public Builder mergeClient(Client client) {
                if ((this.bitField0_ & 262144) == 262144 && this.client_ != Client.getDefaultInstance()) {
                    this.client_ = Client.newBuilder(this.client_).mergeFrom(client).buildPartial();
                } else {
                    this.client_ = client;
                }
                this.bitField0_ |= 262144;
                return this;
            }

            public Builder clearClient() {
                this.client_ = Client.getDefaultInstance();
                this.bitField0_ &= -262145;
                return this;
            }
        }

        public enum CompressionMethod implements EnumLite {
            NONE(0, 0),
            HUFFMAN(1, 1);
            
            public static final int HUFFMAN_VALUE = 1;
            public static final int NONE_VALUE = 0;
            private static EnumLiteMap<CompressionMethod> internalValueMap;
            private final int value;

            static {
                internalValueMap = new EnumLiteMap<CompressionMethod>() {
                    public CompressionMethod findValueByNumber(int i) {
                        return CompressionMethod.valueOf(i);
                    }
                };
            }

            public final int getNumber() {
                return this.value;
            }

            public static CompressionMethod valueOf(int i) {
                switch (i) {
                    case 0:
                        return NONE;
                    case 1:
                        return HUFFMAN;
                    default:
                        return null;
                }
            }

            public static EnumLiteMap<CompressionMethod> internalGetValueMap() {
                return internalValueMap;
            }

            private CompressionMethod(int i, int i2) {
                this.value = i2;
            }
        }

        private UrlLogEvent(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private UrlLogEvent(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static UrlLogEvent getDefaultInstance() {
            return defaultInstance;
        }

        public UrlLogEvent getDefaultInstanceForType() {
            return defaultInstance;
        }

        private UrlLogEvent(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                            this.bitField0_ |= 1;
                            this.uri_ = codedInputStream.readBytes();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 16:
                            this.bitField0_ |= 2;
                            this.timestamp_ = codedInputStream.readSInt64();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 26:
                            this.bitField0_ |= 4;
                            this.ip_ = codedInputStream.readBytes();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 34:
                            this.bitField0_ |= 8;
                            this.region_ = codedInputStream.readBytes();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 40:
                            this.bitField0_ |= 16;
                            this.callerId_ = codedInputStream.readSInt64();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 50:
                            com.avast.cloud.webrep.proto.Urlinfo.Identity.Builder builder;
                            if ((this.bitField0_ & 32) != 32) {
                                builder = null;
                            } else {
                                builder = this.identity_.toBuilder();
                            }
                            this.identity_ = (Identity) codedInputStream.readMessage(Identity.PARSER, extensionRegistryLite);
                            if (builder != null) {
                                builder.mergeFrom(this.identity_);
                                this.identity_ = builder.buildPartial();
                            }
                            this.bitField0_ |= 32;
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 58:
                            this.bitField0_ |= 64;
                            this.compressedUri_ = codedInputStream.readBytes();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 64:
                            CompressionMethod valueOf = CompressionMethod.valueOf(codedInputStream.readEnum());
                            if (valueOf != null) {
                                this.bitField0_ |= 128;
                                this.compressionMethod_ = valueOf;
                                obj2 = obj;
                                i2 = i;
                                break;
                            }
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
                            this.bitField0_ |= 256;
                            this.referer_ = codedInputStream.readBytes();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 88:
                            this.bitField0_ |= 512;
                            this.windowNum_ = codedInputStream.readSInt32();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 96:
                            this.bitField0_ |= 1024;
                            this.tabNum_ = codedInputStream.readSInt32();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 104:
                            EventType valueOf2 = EventType.valueOf(codedInputStream.readEnum());
                            if (valueOf2 != null) {
                                this.bitField0_ |= 2048;
                                this.windowEvent_ = valueOf2;
                                obj2 = obj;
                                i2 = i;
                                break;
                            }
                            obj2 = obj;
                            i2 = i;
                            break;
                        case Events.E_ADDVIEW_SET_ALL /*112*/:
                            OriginType valueOf3 = OriginType.valueOf(codedInputStream.readEnum());
                            if (valueOf3 != null) {
                                this.bitField0_ |= 4096;
                                this.origin_ = valueOf3;
                                obj2 = obj;
                                i2 = i;
                                break;
                            }
                            obj2 = obj;
                            i2 = i;
                            break;
                        case CircleViewNew.SIZE_OF_COLOR /*120*/:
                            this.bitField0_ |= 8192;
                            this.dnl_ = codedInputStream.readBool();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 130:
                            this.bitField0_ |= 16384;
                            this.reserved_ = codedInputStream.readBytes();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 138:
                            Builder builder2;
                            if ((this.bitField0_ & 32768) != 32768) {
                                builder2 = null;
                            } else {
                                builder2 = this.geoipInfo_.toBuilder();
                            }
                            this.geoipInfo_ = (GeoIpInfo) codedInputStream.readMessage(GeoIpInfo.PARSER, extensionRegistryLite);
                            if (builder2 != null) {
                                builder2.mergeFrom(this.geoipInfo_);
                                this.geoipInfo_ = builder2.buildPartial();
                            }
                            this.bitField0_ |= 32768;
                            obj2 = obj;
                            i2 = i;
                            break;
                        case RemainingTimeSceneHelper.TIME_SCENE_NUM_ONE_DAY /*144*/:
                            this.bitField0_ |= 65536;
                            this.version_ = codedInputStream.readSInt32();
                            obj2 = obj;
                            i2 = i;
                            break;
                        case 154:
                            com.avast.cloud.webrep.proto.Urlinfo.Client.Builder builder3;
                            if ((this.bitField0_ & 131072) != 131072) {
                                builder3 = null;
                            } else {
                                builder3 = this.client_.toBuilder();
                            }
                            this.client_ = (Client) codedInputStream.readMessage(Client.PARSER, extensionRegistryLite);
                            if (builder3 != null) {
                                builder3.mergeFrom(this.client_);
                                this.client_ = builder3.buildPartial();
                            }
                            this.bitField0_ |= 131072;
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
                    if ((i & 256) == 256) {
                        this.customKeyValue_ = Collections.unmodifiableList(this.customKeyValue_);
                    }
                    makeExtensionsImmutable();
                }
            }
            if ((i & 256) == 256) {
                this.customKeyValue_ = Collections.unmodifiableList(this.customKeyValue_);
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<UrlLogEvent> getParserForType() {
            return PARSER;
        }

        public boolean hasUri() {
            return (this.bitField0_ & 1) == 1;
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

        public boolean hasTimestamp() {
            return (this.bitField0_ & 2) == 2;
        }

        public long getTimestamp() {
            return this.timestamp_;
        }

        public boolean hasIp() {
            return (this.bitField0_ & 4) == 4;
        }

        public ByteString getIp() {
            return this.ip_;
        }

        public boolean hasRegion() {
            return (this.bitField0_ & 8) == 8;
        }

        public String getRegion() {
            Object obj = this.region_;
            if (obj instanceof String) {
                return (String) obj;
            }
            ByteString byteString = (ByteString) obj;
            String toStringUtf8 = byteString.toStringUtf8();
            if (byteString.isValidUtf8()) {
                this.region_ = toStringUtf8;
            }
            return toStringUtf8;
        }

        public ByteString getRegionBytes() {
            Object obj = this.region_;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.region_ = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean hasCallerId() {
            return (this.bitField0_ & 16) == 16;
        }

        public long getCallerId() {
            return this.callerId_;
        }

        public boolean hasIdentity() {
            return (this.bitField0_ & 32) == 32;
        }

        public Identity getIdentity() {
            return this.identity_;
        }

        public boolean hasCompressedUri() {
            return (this.bitField0_ & 64) == 64;
        }

        public ByteString getCompressedUri() {
            return this.compressedUri_;
        }

        public boolean hasCompressionMethod() {
            return (this.bitField0_ & 128) == 128;
        }

        public CompressionMethod getCompressionMethod() {
            return this.compressionMethod_;
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
            return (this.bitField0_ & 256) == 256;
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
            return (this.bitField0_ & 512) == 512;
        }

        public int getWindowNum() {
            return this.windowNum_;
        }

        public boolean hasTabNum() {
            return (this.bitField0_ & 1024) == 1024;
        }

        public int getTabNum() {
            return this.tabNum_;
        }

        public boolean hasWindowEvent() {
            return (this.bitField0_ & 2048) == 2048;
        }

        public EventType getWindowEvent() {
            return this.windowEvent_;
        }

        public boolean hasOrigin() {
            return (this.bitField0_ & 4096) == 4096;
        }

        public OriginType getOrigin() {
            return this.origin_;
        }

        public boolean hasDnl() {
            return (this.bitField0_ & 8192) == 8192;
        }

        public boolean getDnl() {
            return this.dnl_;
        }

        public boolean hasReserved() {
            return (this.bitField0_ & 16384) == 16384;
        }

        public ByteString getReserved() {
            return this.reserved_;
        }

        public boolean hasGeoipInfo() {
            return (this.bitField0_ & 32768) == 32768;
        }

        public GeoIpInfo getGeoipInfo() {
            return this.geoipInfo_;
        }

        public boolean hasVersion() {
            return (this.bitField0_ & 65536) == 65536;
        }

        public int getVersion() {
            return this.version_;
        }

        public boolean hasClient() {
            return (this.bitField0_ & 131072) == 131072;
        }

        public Client getClient() {
            return this.client_;
        }

        private void initFields() {
            this.uri_ = "";
            this.timestamp_ = 0;
            this.ip_ = ByteString.EMPTY;
            this.region_ = "";
            this.callerId_ = 0;
            this.identity_ = Identity.getDefaultInstance();
            this.compressedUri_ = ByteString.EMPTY;
            this.compressionMethod_ = CompressionMethod.NONE;
            this.customKeyValue_ = Collections.emptyList();
            this.referer_ = "";
            this.windowNum_ = 0;
            this.tabNum_ = 0;
            this.windowEvent_ = EventType.CLICK;
            this.origin_ = OriginType.LINK;
            this.dnl_ = false;
            this.reserved_ = ByteString.EMPTY;
            this.geoipInfo_ = GeoIpInfo.getDefaultInstance();
            this.version_ = 0;
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
            getSerializedSize();
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeBytes(1, getUriBytes());
            }
            if ((this.bitField0_ & 2) == 2) {
                codedOutputStream.writeSInt64(2, this.timestamp_);
            }
            if ((this.bitField0_ & 4) == 4) {
                codedOutputStream.writeBytes(3, this.ip_);
            }
            if ((this.bitField0_ & 8) == 8) {
                codedOutputStream.writeBytes(4, getRegionBytes());
            }
            if ((this.bitField0_ & 16) == 16) {
                codedOutputStream.writeSInt64(5, this.callerId_);
            }
            if ((this.bitField0_ & 32) == 32) {
                codedOutputStream.writeMessage(6, this.identity_);
            }
            if ((this.bitField0_ & 64) == 64) {
                codedOutputStream.writeBytes(7, this.compressedUri_);
            }
            if ((this.bitField0_ & 128) == 128) {
                codedOutputStream.writeEnum(8, this.compressionMethod_.getNumber());
            }
            for (int i = 0; i < this.customKeyValue_.size(); i++) {
                codedOutputStream.writeMessage(9, (MessageLite) this.customKeyValue_.get(i));
            }
            if ((this.bitField0_ & 256) == 256) {
                codedOutputStream.writeBytes(10, getRefererBytes());
            }
            if ((this.bitField0_ & 512) == 512) {
                codedOutputStream.writeSInt32(11, this.windowNum_);
            }
            if ((this.bitField0_ & 1024) == 1024) {
                codedOutputStream.writeSInt32(12, this.tabNum_);
            }
            if ((this.bitField0_ & 2048) == 2048) {
                codedOutputStream.writeEnum(13, this.windowEvent_.getNumber());
            }
            if ((this.bitField0_ & 4096) == 4096) {
                codedOutputStream.writeEnum(14, this.origin_.getNumber());
            }
            if ((this.bitField0_ & 8192) == 8192) {
                codedOutputStream.writeBool(15, this.dnl_);
            }
            if ((this.bitField0_ & 16384) == 16384) {
                codedOutputStream.writeBytes(16, this.reserved_);
            }
            if ((this.bitField0_ & 32768) == 32768) {
                codedOutputStream.writeMessage(17, this.geoipInfo_);
            }
            if ((this.bitField0_ & 65536) == 65536) {
                codedOutputStream.writeSInt32(18, this.version_);
            }
            if ((this.bitField0_ & 131072) == 131072) {
                codedOutputStream.writeMessage(19, this.client_);
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
                i2 = CodedOutputStream.computeBytesSize(1, getUriBytes()) + 0;
            }
            if ((this.bitField0_ & 2) == 2) {
                i2 += CodedOutputStream.computeSInt64Size(2, this.timestamp_);
            }
            if ((this.bitField0_ & 4) == 4) {
                i2 += CodedOutputStream.computeBytesSize(3, this.ip_);
            }
            if ((this.bitField0_ & 8) == 8) {
                i2 += CodedOutputStream.computeBytesSize(4, getRegionBytes());
            }
            if ((this.bitField0_ & 16) == 16) {
                i2 += CodedOutputStream.computeSInt64Size(5, this.callerId_);
            }
            if ((this.bitField0_ & 32) == 32) {
                i2 += CodedOutputStream.computeMessageSize(6, this.identity_);
            }
            if ((this.bitField0_ & 64) == 64) {
                i2 += CodedOutputStream.computeBytesSize(7, this.compressedUri_);
            }
            if ((this.bitField0_ & 128) == 128) {
                i2 += CodedOutputStream.computeEnumSize(8, this.compressionMethod_.getNumber());
            }
            int i3 = i2;
            while (i < this.customKeyValue_.size()) {
                i++;
                i3 = CodedOutputStream.computeMessageSize(9, (MessageLite) this.customKeyValue_.get(i)) + i3;
            }
            if ((this.bitField0_ & 256) == 256) {
                i3 += CodedOutputStream.computeBytesSize(10, getRefererBytes());
            }
            if ((this.bitField0_ & 512) == 512) {
                i3 += CodedOutputStream.computeSInt32Size(11, this.windowNum_);
            }
            if ((this.bitField0_ & 1024) == 1024) {
                i3 += CodedOutputStream.computeSInt32Size(12, this.tabNum_);
            }
            if ((this.bitField0_ & 2048) == 2048) {
                i3 += CodedOutputStream.computeEnumSize(13, this.windowEvent_.getNumber());
            }
            if ((this.bitField0_ & 4096) == 4096) {
                i3 += CodedOutputStream.computeEnumSize(14, this.origin_.getNumber());
            }
            if ((this.bitField0_ & 8192) == 8192) {
                i3 += CodedOutputStream.computeBoolSize(15, this.dnl_);
            }
            if ((this.bitField0_ & 16384) == 16384) {
                i3 += CodedOutputStream.computeBytesSize(16, this.reserved_);
            }
            if ((this.bitField0_ & 32768) == 32768) {
                i3 += CodedOutputStream.computeMessageSize(17, this.geoipInfo_);
            }
            if ((this.bitField0_ & 65536) == 65536) {
                i3 += CodedOutputStream.computeSInt32Size(18, this.version_);
            }
            if ((this.bitField0_ & 131072) == 131072) {
                i3 += CodedOutputStream.computeMessageSize(19, this.client_);
            }
            this.memoizedSerializedSize = i3;
            return i3;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static UrlLogEvent parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (UrlLogEvent) PARSER.parseFrom(byteString);
        }

        public static UrlLogEvent parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UrlLogEvent) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static UrlLogEvent parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (UrlLogEvent) PARSER.parseFrom(bArr);
        }

        public static UrlLogEvent parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UrlLogEvent) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static UrlLogEvent parseFrom(InputStream inputStream) throws IOException {
            return (UrlLogEvent) PARSER.parseFrom(inputStream);
        }

        public static UrlLogEvent parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlLogEvent) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static UrlLogEvent parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (UrlLogEvent) PARSER.parseDelimitedFrom(inputStream);
        }

        public static UrlLogEvent parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlLogEvent) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static UrlLogEvent parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (UrlLogEvent) PARSER.parseFrom(codedInputStream);
        }

        public static UrlLogEvent parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlLogEvent) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(UrlLogEvent urlLogEvent) {
            return newBuilder().mergeFrom(urlLogEvent);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    public interface UrlLogEventDataPacketOrBuilder extends MessageLiteOrBuilder {
        int getHops();

        UrlLogEvent getUrlLogEvent(int i);

        int getUrlLogEventCount();

        List<UrlLogEvent> getUrlLogEventList();

        boolean hasHops();
    }

    public static final class UrlLogEventDataPacket extends GeneratedMessageLite implements UrlLogEventDataPacketOrBuilder {
        public static final int HOPS_FIELD_NUMBER = 2;
        public static Parser<UrlLogEventDataPacket> PARSER = new AbstractParser<UrlLogEventDataPacket>() {
            public UrlLogEventDataPacket parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
                return new UrlLogEventDataPacket(codedInputStream, extensionRegistryLite);
            }
        };
        public static final int URLLOGEVENT_FIELD_NUMBER = 1;
        private static final UrlLogEventDataPacket defaultInstance = new UrlLogEventDataPacket(true);
        private static final long serialVersionUID = 0;
        private int bitField0_;
        private int hops_;
        private byte memoizedIsInitialized;
        private int memoizedSerializedSize;
        private List<UrlLogEvent> urlLogEvent_;

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<UrlLogEventDataPacket, Builder> implements UrlLogEventDataPacketOrBuilder {
            private int bitField0_;
            private int hops_;
            private List<UrlLogEvent> urlLogEvent_ = Collections.emptyList();

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
                this.urlLogEvent_ = Collections.emptyList();
                this.bitField0_ &= -2;
                this.hops_ = 0;
                this.bitField0_ &= -3;
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public UrlLogEventDataPacket getDefaultInstanceForType() {
                return UrlLogEventDataPacket.getDefaultInstance();
            }

            public UrlLogEventDataPacket build() {
                MessageLite buildPartial = buildPartial();
                if (buildPartial.isInitialized()) {
                    return buildPartial;
                }
                throw com.google.protobuf.AbstractMessageLite.Builder.newUninitializedMessageException(buildPartial);
            }

            public UrlLogEventDataPacket buildPartial() {
                UrlLogEventDataPacket urlLogEventDataPacket = new UrlLogEventDataPacket((com.google.protobuf.GeneratedMessageLite.Builder) this);
                int i = this.bitField0_;
                int i2 = 0;
                if ((this.bitField0_ & 1) == 1) {
                    this.urlLogEvent_ = Collections.unmodifiableList(this.urlLogEvent_);
                    this.bitField0_ &= -2;
                }
                urlLogEventDataPacket.urlLogEvent_ = this.urlLogEvent_;
                if ((i & 2) == 2) {
                    i2 = 1;
                }
                urlLogEventDataPacket.hops_ = this.hops_;
                urlLogEventDataPacket.bitField0_ = i2;
                return urlLogEventDataPacket;
            }

            public Builder mergeFrom(UrlLogEventDataPacket urlLogEventDataPacket) {
                if (urlLogEventDataPacket == UrlLogEventDataPacket.getDefaultInstance()) {
                    return this;
                }
                if (!urlLogEventDataPacket.urlLogEvent_.isEmpty()) {
                    if (this.urlLogEvent_.isEmpty()) {
                        this.urlLogEvent_ = urlLogEventDataPacket.urlLogEvent_;
                        this.bitField0_ &= -2;
                    } else {
                        ensureUrlLogEventIsMutable();
                        this.urlLogEvent_.addAll(urlLogEventDataPacket.urlLogEvent_);
                    }
                }
                if (urlLogEventDataPacket.hasHops()) {
                    setHops(urlLogEventDataPacket.getHops());
                }
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                UrlLogEventDataPacket urlLogEventDataPacket;
                UrlLogEventDataPacket urlLogEventDataPacket2;
                try {
                    urlLogEventDataPacket2 = (UrlLogEventDataPacket) UrlLogEventDataPacket.PARSER.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (urlLogEventDataPacket2 != null) {
                        mergeFrom(urlLogEventDataPacket2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    urlLogEventDataPacket2 = (UrlLogEventDataPacket) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    urlLogEventDataPacket = urlLogEventDataPacket2;
                    th = th3;
                }
                if (urlLogEventDataPacket != null) {
                    mergeFrom(urlLogEventDataPacket);
                }
                throw th;
            }

            private void ensureUrlLogEventIsMutable() {
                if ((this.bitField0_ & 1) != 1) {
                    this.urlLogEvent_ = new ArrayList(this.urlLogEvent_);
                    this.bitField0_ |= 1;
                }
            }

            public List<UrlLogEvent> getUrlLogEventList() {
                return Collections.unmodifiableList(this.urlLogEvent_);
            }

            public int getUrlLogEventCount() {
                return this.urlLogEvent_.size();
            }

            public UrlLogEvent getUrlLogEvent(int i) {
                return (UrlLogEvent) this.urlLogEvent_.get(i);
            }

            public Builder setUrlLogEvent(int i, UrlLogEvent urlLogEvent) {
                if (urlLogEvent != null) {
                    ensureUrlLogEventIsMutable();
                    this.urlLogEvent_.set(i, urlLogEvent);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder setUrlLogEvent(int i, Builder builder) {
                ensureUrlLogEventIsMutable();
                this.urlLogEvent_.set(i, builder.build());
                return this;
            }

            public Builder addUrlLogEvent(UrlLogEvent urlLogEvent) {
                if (urlLogEvent != null) {
                    ensureUrlLogEventIsMutable();
                    this.urlLogEvent_.add(urlLogEvent);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addUrlLogEvent(int i, UrlLogEvent urlLogEvent) {
                if (urlLogEvent != null) {
                    ensureUrlLogEventIsMutable();
                    this.urlLogEvent_.add(i, urlLogEvent);
                    return this;
                }
                throw new NullPointerException();
            }

            public Builder addUrlLogEvent(Builder builder) {
                ensureUrlLogEventIsMutable();
                this.urlLogEvent_.add(builder.build());
                return this;
            }

            public Builder addUrlLogEvent(int i, Builder builder) {
                ensureUrlLogEventIsMutable();
                this.urlLogEvent_.add(i, builder.build());
                return this;
            }

            public Builder addAllUrlLogEvent(Iterable<? extends UrlLogEvent> iterable) {
                ensureUrlLogEventIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(iterable, this.urlLogEvent_);
                return this;
            }

            public Builder clearUrlLogEvent() {
                this.urlLogEvent_ = Collections.emptyList();
                this.bitField0_ &= -2;
                return this;
            }

            public Builder removeUrlLogEvent(int i) {
                ensureUrlLogEventIsMutable();
                this.urlLogEvent_.remove(i);
                return this;
            }

            public boolean hasHops() {
                return (this.bitField0_ & 2) == 2;
            }

            public int getHops() {
                return this.hops_;
            }

            public Builder setHops(int i) {
                this.bitField0_ |= 2;
                this.hops_ = i;
                return this;
            }

            public Builder clearHops() {
                this.bitField0_ &= -3;
                this.hops_ = 0;
                return this;
            }
        }

        private UrlLogEventDataPacket(com.google.protobuf.GeneratedMessageLite.Builder builder) {
            super(builder);
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        private UrlLogEventDataPacket(boolean z) {
            this.memoizedIsInitialized = (byte) -1;
            this.memoizedSerializedSize = -1;
        }

        public static UrlLogEventDataPacket getDefaultInstance() {
            return defaultInstance;
        }

        public UrlLogEventDataPacket getDefaultInstanceForType() {
            return defaultInstance;
        }

        private UrlLogEventDataPacket(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                                this.urlLogEvent_ = new ArrayList();
                                i |= 1;
                            }
                            this.urlLogEvent_.add(codedInputStream.readMessage(UrlLogEvent.PARSER, extensionRegistryLite));
                            break;
                        case 16:
                            this.bitField0_ |= 1;
                            this.hops_ = codedInputStream.readInt32();
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
                        this.urlLogEvent_ = Collections.unmodifiableList(this.urlLogEvent_);
                    }
                    makeExtensionsImmutable();
                }
            }
            if ((i & 1) == 1) {
                this.urlLogEvent_ = Collections.unmodifiableList(this.urlLogEvent_);
            }
            makeExtensionsImmutable();
        }

        static {
            defaultInstance.initFields();
        }

        public Parser<UrlLogEventDataPacket> getParserForType() {
            return PARSER;
        }

        public List<UrlLogEvent> getUrlLogEventList() {
            return this.urlLogEvent_;
        }

        public List<? extends UrlLogEventOrBuilder> getUrlLogEventOrBuilderList() {
            return this.urlLogEvent_;
        }

        public int getUrlLogEventCount() {
            return this.urlLogEvent_.size();
        }

        public UrlLogEvent getUrlLogEvent(int i) {
            return (UrlLogEvent) this.urlLogEvent_.get(i);
        }

        public UrlLogEventOrBuilder getUrlLogEventOrBuilder(int i) {
            return (UrlLogEventOrBuilder) this.urlLogEvent_.get(i);
        }

        public boolean hasHops() {
            return (this.bitField0_ & 1) == 1;
        }

        public int getHops() {
            return this.hops_;
        }

        private void initFields() {
            this.urlLogEvent_ = Collections.emptyList();
            this.hops_ = 0;
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
            for (int i = 0; i < this.urlLogEvent_.size(); i++) {
                codedOutputStream.writeMessage(1, (MessageLite) this.urlLogEvent_.get(i));
            }
            if ((this.bitField0_ & 1) == 1) {
                codedOutputStream.writeInt32(2, this.hops_);
            }
        }

        public int getSerializedSize() {
            int i = this.memoizedSerializedSize;
            if (i != -1) {
                return i;
            }
            int i2 = 0;
            for (i = 0; i < this.urlLogEvent_.size(); i++) {
                i2 += CodedOutputStream.computeMessageSize(1, (MessageLite) this.urlLogEvent_.get(i));
            }
            if ((this.bitField0_ & 1) == 1) {
                i2 += CodedOutputStream.computeInt32Size(2, this.hops_);
            }
            this.memoizedSerializedSize = i2;
            return i2;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public static UrlLogEventDataPacket parseFrom(ByteString byteString) throws InvalidProtocolBufferException {
            return (UrlLogEventDataPacket) PARSER.parseFrom(byteString);
        }

        public static UrlLogEventDataPacket parseFrom(ByteString byteString, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UrlLogEventDataPacket) PARSER.parseFrom(byteString, extensionRegistryLite);
        }

        public static UrlLogEventDataPacket parseFrom(byte[] bArr) throws InvalidProtocolBufferException {
            return (UrlLogEventDataPacket) PARSER.parseFrom(bArr);
        }

        public static UrlLogEventDataPacket parseFrom(byte[] bArr, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            return (UrlLogEventDataPacket) PARSER.parseFrom(bArr, extensionRegistryLite);
        }

        public static UrlLogEventDataPacket parseFrom(InputStream inputStream) throws IOException {
            return (UrlLogEventDataPacket) PARSER.parseFrom(inputStream);
        }

        public static UrlLogEventDataPacket parseFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlLogEventDataPacket) PARSER.parseFrom(inputStream, extensionRegistryLite);
        }

        public static UrlLogEventDataPacket parseDelimitedFrom(InputStream inputStream) throws IOException {
            return (UrlLogEventDataPacket) PARSER.parseDelimitedFrom(inputStream);
        }

        public static UrlLogEventDataPacket parseDelimitedFrom(InputStream inputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlLogEventDataPacket) PARSER.parseDelimitedFrom(inputStream, extensionRegistryLite);
        }

        public static UrlLogEventDataPacket parseFrom(CodedInputStream codedInputStream) throws IOException {
            return (UrlLogEventDataPacket) PARSER.parseFrom(codedInputStream);
        }

        public static UrlLogEventDataPacket parseFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
            return (UrlLogEventDataPacket) PARSER.parseFrom(codedInputStream, extensionRegistryLite);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(UrlLogEventDataPacket urlLogEventDataPacket) {
            return newBuilder().mergeFrom(urlLogEventDataPacket);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }
    }

    private UrlLog() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite extensionRegistryLite) {
    }
}
