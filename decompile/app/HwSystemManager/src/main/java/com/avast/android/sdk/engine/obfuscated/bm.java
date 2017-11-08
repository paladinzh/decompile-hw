package com.avast.android.sdk.engine.obfuscated;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.GeneratedMessageLite.Builder;
import com.google.protobuf.Internal.EnumLite;
import com.google.protobuf.Internal.EnumLiteMap;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import com.google.protobuf.Parser;
import com.huawei.rcs.common.HwRcsCommonObject;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import java.io.IOException;
import java.io.ObjectStreamException;

/* compiled from: Unknown */
public final class bm {

    /* compiled from: Unknown */
    public interface b extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class a extends GeneratedMessageLite implements b {
        public static Parser<a> a = new bn();
        private static final a b = new a(true);
        private int c;
        private ByteString d;
        private ByteString e;
        private ByteString f;
        private ByteString g;
        private a h;
        private int i;
        private ByteString j;
        private ByteString k;
        private ByteString l;
        private ByteString m;
        private byte n;
        private int o;

        /* compiled from: Unknown */
        public enum a implements EnumLite {
            CHROME(0, 0),
            FIREFOX(1, 1),
            IE(2, 2),
            OPERA(3, 3),
            SAFAR(4, 4),
            PRODUCTS(5, 5),
            VIDEO(6, 6);
            
            private static EnumLiteMap<a> h;
            private final int i;

            static {
                h = new bo();
            }

            private a(int i, int i2) {
                this.i = i2;
            }

            public static a a(int i) {
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

            public final int getNumber() {
                return this.i;
            }
        }

        /* compiled from: Unknown */
        public static final class b extends Builder<a, b> implements b {
            private int a;
            private ByteString b = ByteString.EMPTY;
            private ByteString c = ByteString.EMPTY;
            private ByteString d = ByteString.EMPTY;
            private ByteString e = ByteString.EMPTY;
            private a f = a.CHROME;
            private int g;
            private ByteString h = ByteString.EMPTY;
            private ByteString i = ByteString.EMPTY;
            private ByteString j = ByteString.EMPTY;
            private ByteString k = ByteString.EMPTY;

            private b() {
                g();
            }

            private void g() {
            }

            private static b h() {
                return new b();
            }

            public b a() {
                super.clear();
                this.b = ByteString.EMPTY;
                this.a &= -2;
                this.c = ByteString.EMPTY;
                this.a &= -3;
                this.d = ByteString.EMPTY;
                this.a &= -5;
                this.e = ByteString.EMPTY;
                this.a &= -9;
                this.f = a.CHROME;
                this.a &= -17;
                this.g = 0;
                this.a &= -33;
                this.h = ByteString.EMPTY;
                this.a &= -65;
                this.i = ByteString.EMPTY;
                this.a &= -129;
                this.j = ByteString.EMPTY;
                this.a &= -257;
                this.k = ByteString.EMPTY;
                this.a &= -513;
                return this;
            }

            public b a(int i) {
                this.a |= 32;
                this.g = i;
                return this;
            }

            public b a(a aVar) {
                if (aVar != null) {
                    this.a |= 16;
                    this.f = aVar;
                    return this;
                }
                throw new NullPointerException();
            }

            public b a(a aVar) {
                if (aVar == a.a()) {
                    return this;
                }
                if (aVar.c()) {
                    a(aVar.d());
                }
                if (aVar.e()) {
                    b(aVar.f());
                }
                if (aVar.g()) {
                    c(aVar.h());
                }
                if (aVar.i()) {
                    d(aVar.j());
                }
                if (aVar.k()) {
                    a(aVar.l());
                }
                if (aVar.m()) {
                    a(aVar.n());
                }
                if (aVar.o()) {
                    e(aVar.p());
                }
                if (aVar.q()) {
                    f(aVar.r());
                }
                if (aVar.s()) {
                    g(aVar.t());
                }
                if (aVar.u()) {
                    h(aVar.v());
                }
                return this;
            }

            public b a(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 1;
                    this.b = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public b a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                a aVar;
                a aVar2;
                try {
                    aVar2 = (a) a.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (aVar2 != null) {
                        a(aVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    aVar2 = (a) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    aVar = aVar2;
                    th = th3;
                }
                if (aVar != null) {
                    a(aVar);
                }
                throw th;
            }

            public b b() {
                return h().a(e());
            }

            public b b(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 2;
                    this.c = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public /* synthetic */ MessageLite build() {
                return d();
            }

            public /* synthetic */ MessageLite buildPartial() {
                return e();
            }

            public b c(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 4;
                    this.d = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a c() {
                return a.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m24clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m25clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m26clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m27clone() throws CloneNotSupportedException {
                return b();
            }

            public b d(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 8;
                    this.e = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public b e(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 64;
                    this.h = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a e() {
                a aVar = new a((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                aVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                aVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                aVar.f = this.d;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                aVar.g = this.e;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                aVar.h = this.f;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                aVar.i = this.g;
                if ((i & 64) == 64) {
                    i2 |= 64;
                }
                aVar.j = this.h;
                if ((i & 128) == 128) {
                    i2 |= 128;
                }
                aVar.k = this.i;
                if ((i & 256) == 256) {
                    i2 |= 256;
                }
                aVar.l = this.j;
                if ((i & 512) == 512) {
                    i2 |= 512;
                }
                aVar.m = this.k;
                aVar.c = i2;
                return aVar;
            }

            public b f(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 128;
                    this.i = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public b g(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 256;
                    this.j = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m28getDefaultInstanceForType() {
                return c();
            }

            public b h(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 512;
                    this.k = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((a) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m29mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.z();
        }

        private a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.n = (byte) -1;
            this.o = -1;
            z();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 10:
                            this.c |= 1;
                            this.d = codedInputStream.readBytes();
                            break;
                        case 18:
                            this.c |= 2;
                            this.e = codedInputStream.readBytes();
                            break;
                        case 26:
                            this.c |= 4;
                            this.f = codedInputStream.readBytes();
                            break;
                        case 34:
                            this.c |= 8;
                            this.g = codedInputStream.readBytes();
                            break;
                        case 40:
                            a a = a.a(codedInputStream.readEnum());
                            if (a == null) {
                                break;
                            }
                            this.c |= 16;
                            this.h = a;
                            break;
                        case HwRcsCommonObject.BLACKLIST_MSG_VIDEO_TYPE /*48*/:
                            this.c |= 32;
                            this.i = codedInputStream.readSInt32();
                            break;
                        case 58:
                            this.c |= 64;
                            this.j = codedInputStream.readBytes();
                            break;
                        case Events.E_ANTISPAM_VIEW_CALL /*66*/:
                            this.c |= 128;
                            this.k = codedInputStream.readBytes();
                            break;
                        case 74:
                            this.c |= 256;
                            this.l = codedInputStream.readBytes();
                            break;
                        case Events.E_ANTIVIRUS_SCAN /*82*/:
                            this.c |= 512;
                            this.m = codedInputStream.readBytes();
                            break;
                        default:
                            if (parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                break;
                            }
                            obj = 1;
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

        private a(Builder builder) {
            super(builder);
            this.n = (byte) -1;
            this.o = -1;
        }

        private a(boolean z) {
            this.n = (byte) -1;
            this.o = -1;
        }

        public static b a(a aVar) {
            return w().a(aVar);
        }

        public static a a() {
            return b;
        }

        public static b w() {
            return b.h();
        }

        private void z() {
            this.d = ByteString.EMPTY;
            this.e = ByteString.EMPTY;
            this.f = ByteString.EMPTY;
            this.g = ByteString.EMPTY;
            this.h = a.CHROME;
            this.i = 0;
            this.j = ByteString.EMPTY;
            this.k = ByteString.EMPTY;
            this.l = ByteString.EMPTY;
            this.m = ByteString.EMPTY;
        }

        public a b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ByteString d() {
            return this.d;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public ByteString f() {
            return this.e;
        }

        public boolean g() {
            return (this.c & 4) == 4;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<a> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.o;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, this.e);
            }
            if ((this.c & 4) == 4) {
                i += CodedOutputStream.computeBytesSize(3, this.f);
            }
            if ((this.c & 8) == 8) {
                i += CodedOutputStream.computeBytesSize(4, this.g);
            }
            if ((this.c & 16) == 16) {
                i += CodedOutputStream.computeEnumSize(5, this.h.getNumber());
            }
            if ((this.c & 32) == 32) {
                i += CodedOutputStream.computeSInt32Size(6, this.i);
            }
            if ((this.c & 64) == 64) {
                i += CodedOutputStream.computeBytesSize(7, this.j);
            }
            if ((this.c & 128) == 128) {
                i += CodedOutputStream.computeBytesSize(8, this.k);
            }
            if ((this.c & 256) == 256) {
                i += CodedOutputStream.computeBytesSize(9, this.l);
            }
            if ((this.c & 512) == 512) {
                i += CodedOutputStream.computeBytesSize(10, this.m);
            }
            this.o = i;
            return i;
        }

        public ByteString h() {
            return this.f;
        }

        public boolean i() {
            return (this.c & 8) == 8;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.n;
            if (b == (byte) -1) {
                this.n = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public ByteString j() {
            return this.g;
        }

        public boolean k() {
            return (this.c & 16) == 16;
        }

        public a l() {
            return this.h;
        }

        public boolean m() {
            return (this.c & 32) == 32;
        }

        public int n() {
            return this.i;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return x();
        }

        public boolean o() {
            return (this.c & 64) == 64;
        }

        public ByteString p() {
            return this.j;
        }

        public boolean q() {
            return (this.c & 128) == 128;
        }

        public ByteString r() {
            return this.k;
        }

        public boolean s() {
            return (this.c & 256) == 256;
        }

        public ByteString t() {
            return this.l;
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return y();
        }

        public boolean u() {
            return (this.c & 512) == 512;
        }

        public ByteString v() {
            return this.m;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.c & 1) == 1) {
                codedOutputStream.writeBytes(1, this.d);
            }
            if ((this.c & 2) == 2) {
                codedOutputStream.writeBytes(2, this.e);
            }
            if ((this.c & 4) == 4) {
                codedOutputStream.writeBytes(3, this.f);
            }
            if ((this.c & 8) == 8) {
                codedOutputStream.writeBytes(4, this.g);
            }
            if ((this.c & 16) == 16) {
                codedOutputStream.writeEnum(5, this.h.getNumber());
            }
            if ((this.c & 32) == 32) {
                codedOutputStream.writeSInt32(6, this.i);
            }
            if ((this.c & 64) == 64) {
                codedOutputStream.writeBytes(7, this.j);
            }
            if ((this.c & 128) == 128) {
                codedOutputStream.writeBytes(8, this.k);
            }
            if ((this.c & 256) == 256) {
                codedOutputStream.writeBytes(9, this.l);
            }
            if ((this.c & 512) == 512) {
                codedOutputStream.writeBytes(10, this.m);
            }
        }

        public b x() {
            return w();
        }

        public b y() {
            return a(this);
        }
    }

    /* compiled from: Unknown */
    public interface d extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class c extends GeneratedMessageLite implements d {
        public static Parser<c> a = new bp();
        private static final c b = new c(true);
        private int c;
        private g d;
        private a e;
        private ByteString f;
        private byte g;
        private int h;

        /* compiled from: Unknown */
        public static final class a extends Builder<c, a> implements d {
            private int a;
            private g b = g.a();
            private a c = a.a();
            private ByteString d = ByteString.EMPTY;

            private a() {
                g();
            }

            private void g() {
            }

            private static a h() {
                return new a();
            }

            public a a() {
                super.clear();
                this.b = g.a();
                this.a &= -2;
                this.c = a.a();
                this.a &= -3;
                this.d = ByteString.EMPTY;
                this.a &= -5;
                return this;
            }

            public a a(a aVar) {
                if (aVar != null) {
                    this.c = aVar;
                    this.a |= 2;
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(c cVar) {
                if (cVar == c.a()) {
                    return this;
                }
                if (cVar.c()) {
                    a(cVar.d());
                }
                if (cVar.e()) {
                    b(cVar.f());
                }
                if (cVar.g()) {
                    a(cVar.h());
                }
                return this;
            }

            public a a(a aVar) {
                this.b = aVar.d();
                this.a |= 1;
                return this;
            }

            public a a(g gVar) {
                if ((this.a & 1) == 1 && this.b != g.a()) {
                    this.b = g.a(this.b).a(gVar).e();
                } else {
                    this.b = gVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 4;
                    this.d = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                c cVar;
                Throwable th;
                c cVar2;
                try {
                    cVar = (c) c.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (cVar != null) {
                        a(cVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    cVar = (c) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    cVar2 = cVar;
                    th = th3;
                }
                if (cVar2 != null) {
                    a(cVar2);
                }
                throw th;
            }

            public a b() {
                return h().a(e());
            }

            public a b(a aVar) {
                if ((this.a & 2) == 2 && this.c != a.a()) {
                    this.c = a.a(this.c).a(aVar).e();
                } else {
                    this.c = aVar;
                }
                this.a |= 2;
                return this;
            }

            public /* synthetic */ MessageLite build() {
                return d();
            }

            public /* synthetic */ MessageLite buildPartial() {
                return e();
            }

            public c c() {
                return c.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m30clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m31clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m32clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m33clone() throws CloneNotSupportedException {
                return b();
            }

            public c d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public c e() {
                c cVar = new c((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                cVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                cVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                cVar.f = this.d;
                cVar.c = i2;
                return cVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m34getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((c) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m35mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.l();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private c(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.g = (byte) -1;
            this.h = -1;
            l();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a g = (this.c & 1) != 1 ? null : this.d.g();
                            this.d = (g) codedInputStream.readMessage(g.a, extensionRegistryLite);
                            if (g != null) {
                                g.a(this.d);
                                this.d = g.e();
                            }
                            this.c |= 1;
                            break;
                        case 18:
                            b y = (this.c & 2) != 2 ? null : this.e.y();
                            this.e = (a) codedInputStream.readMessage(a.a, extensionRegistryLite);
                            if (y != null) {
                                y.a(this.e);
                                this.e = y.e();
                            }
                            this.c |= 2;
                            break;
                        case 26:
                            this.c |= 4;
                            this.f = codedInputStream.readBytes();
                            break;
                        default:
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

        private c(Builder builder) {
            super(builder);
            this.g = (byte) -1;
            this.h = -1;
        }

        private c(boolean z) {
            this.g = (byte) -1;
            this.h = -1;
        }

        public static a a(c cVar) {
            return i().a(cVar);
        }

        public static c a() {
            return b;
        }

        public static a i() {
            return a.h();
        }

        private void l() {
            this.d = g.a();
            this.e = a.a();
            this.f = ByteString.EMPTY;
        }

        public c b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public g d() {
            return this.d;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public a f() {
            return this.e;
        }

        public boolean g() {
            return (this.c & 4) == 4;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<c> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.h;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeMessageSize(2, this.e);
            }
            if ((this.c & 4) == 4) {
                i += CodedOutputStream.computeBytesSize(3, this.f);
            }
            this.h = i;
            return i;
        }

        public ByteString h() {
            return this.f;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.g;
            if (b == (byte) -1) {
                this.g = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public a j() {
            return i();
        }

        public a k() {
            return a(this);
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return j();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return k();
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.c & 1) == 1) {
                codedOutputStream.writeMessage(1, this.d);
            }
            if ((this.c & 2) == 2) {
                codedOutputStream.writeMessage(2, this.e);
            }
            if ((this.c & 4) == 4) {
                codedOutputStream.writeBytes(3, this.f);
            }
        }
    }

    /* compiled from: Unknown */
    public interface f extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class e extends GeneratedMessageLite implements f {
        public static Parser<e> a = new bq();
        private static final e b = new e(true);
        private int c;
        private g d;
        private ByteString e;
        private ByteString f;
        private long g;
        private byte h;
        private int i;

        /* compiled from: Unknown */
        public static final class a extends Builder<e, a> implements f {
            private int a;
            private g b = g.a();
            private ByteString c = ByteString.EMPTY;
            private ByteString d = ByteString.EMPTY;
            private long e;

            private a() {
                g();
            }

            private void g() {
            }

            private static a h() {
                return new a();
            }

            public a a() {
                super.clear();
                this.b = g.a();
                this.a &= -2;
                this.c = ByteString.EMPTY;
                this.a &= -3;
                this.d = ByteString.EMPTY;
                this.a &= -5;
                this.e = 0;
                this.a &= -9;
                return this;
            }

            public a a(long j) {
                this.a |= 8;
                this.e = j;
                return this;
            }

            public a a(e eVar) {
                if (eVar == e.a()) {
                    return this;
                }
                if (eVar.c()) {
                    a(eVar.d());
                }
                if (eVar.e()) {
                    a(eVar.f());
                }
                if (eVar.g()) {
                    b(eVar.h());
                }
                if (eVar.i()) {
                    a(eVar.j());
                }
                return this;
            }

            public a a(g gVar) {
                if ((this.a & 1) == 1 && this.b != g.a()) {
                    this.b = g.a(this.b).a(gVar).e();
                } else {
                    this.b = gVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 2;
                    this.c = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                e eVar;
                e eVar2;
                try {
                    eVar2 = (e) e.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (eVar2 != null) {
                        a(eVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    eVar2 = (e) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    eVar = eVar2;
                    th = th3;
                }
                if (eVar != null) {
                    a(eVar);
                }
                throw th;
            }

            public a b() {
                return h().a(e());
            }

            public a b(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 4;
                    this.d = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public /* synthetic */ MessageLite build() {
                return d();
            }

            public /* synthetic */ MessageLite buildPartial() {
                return e();
            }

            public e c() {
                return e.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m36clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m37clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m38clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m39clone() throws CloneNotSupportedException {
                return b();
            }

            public e d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public e e() {
                e eVar = new e((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                eVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                eVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                eVar.f = this.d;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                eVar.g = this.e;
                eVar.c = i2;
                return eVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m40getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((e) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m41mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.n();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private e(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.h = (byte) -1;
            this.i = -1;
            n();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a g = (this.c & 1) != 1 ? null : this.d.g();
                            this.d = (g) codedInputStream.readMessage(g.a, extensionRegistryLite);
                            if (g != null) {
                                g.a(this.d);
                                this.d = g.e();
                            }
                            this.c |= 1;
                            break;
                        case 18:
                            this.c |= 2;
                            this.e = codedInputStream.readBytes();
                            break;
                        case 26:
                            this.c |= 4;
                            this.f = codedInputStream.readBytes();
                            break;
                        case 32:
                            this.c |= 8;
                            this.g = codedInputStream.readInt64();
                            break;
                        default:
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

        private e(Builder builder) {
            super(builder);
            this.h = (byte) -1;
            this.i = -1;
        }

        private e(boolean z) {
            this.h = (byte) -1;
            this.i = -1;
        }

        public static a a(e eVar) {
            return k().a(eVar);
        }

        public static e a() {
            return b;
        }

        public static e a(byte[] bArr) throws InvalidProtocolBufferException {
            return (e) a.parseFrom(bArr);
        }

        public static a k() {
            return a.h();
        }

        private void n() {
            this.d = g.a();
            this.e = ByteString.EMPTY;
            this.f = ByteString.EMPTY;
            this.g = 0;
        }

        public e b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public g d() {
            return this.d;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public ByteString f() {
            return this.e;
        }

        public boolean g() {
            return (this.c & 4) == 4;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<e> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.i;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, this.e);
            }
            if ((this.c & 4) == 4) {
                i += CodedOutputStream.computeBytesSize(3, this.f);
            }
            if ((this.c & 8) == 8) {
                i += CodedOutputStream.computeInt64Size(4, this.g);
            }
            this.i = i;
            return i;
        }

        public ByteString h() {
            return this.f;
        }

        public boolean i() {
            return (this.c & 8) == 8;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.h;
            if (b == (byte) -1) {
                this.h = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public long j() {
            return this.g;
        }

        public a l() {
            return k();
        }

        public a m() {
            return a(this);
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return l();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return m();
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.c & 1) == 1) {
                codedOutputStream.writeMessage(1, this.d);
            }
            if ((this.c & 2) == 2) {
                codedOutputStream.writeBytes(2, this.e);
            }
            if ((this.c & 4) == 4) {
                codedOutputStream.writeBytes(3, this.f);
            }
            if ((this.c & 8) == 8) {
                codedOutputStream.writeInt64(4, this.g);
            }
        }
    }

    /* compiled from: Unknown */
    public interface h extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class g extends GeneratedMessageLite implements h {
        public static Parser<g> a = new br();
        private static final g b = new g(true);
        private int c;
        private ByteString d;
        private byte e;
        private int f;

        /* compiled from: Unknown */
        public static final class a extends Builder<g, a> implements h {
            private int a;
            private ByteString b = ByteString.EMPTY;

            private a() {
                g();
            }

            private void g() {
            }

            private static a h() {
                return new a();
            }

            public a a() {
                super.clear();
                this.b = ByteString.EMPTY;
                this.a &= -2;
                return this;
            }

            public a a(g gVar) {
                if (gVar != g.a() && gVar.c()) {
                    a(gVar.d());
                }
                return this;
            }

            public a a(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 1;
                    this.b = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                g gVar;
                g gVar2;
                try {
                    gVar2 = (g) g.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (gVar2 != null) {
                        a(gVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    gVar2 = (g) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    gVar = gVar2;
                    th = th3;
                }
                if (gVar != null) {
                    a(gVar);
                }
                throw th;
            }

            public a b() {
                return h().a(e());
            }

            public /* synthetic */ MessageLite build() {
                return d();
            }

            public /* synthetic */ MessageLite buildPartial() {
                return e();
            }

            public g c() {
                return g.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m42clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m43clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m44clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m45clone() throws CloneNotSupportedException {
                return b();
            }

            public g d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public g e() {
                g gVar = new g((Builder) this);
                int i = 0;
                if ((this.a & 1) == 1) {
                    i = 1;
                }
                gVar.d = this.b;
                gVar.c = i;
                return gVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m46getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((g) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m47mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.h();
        }

        private g(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.e = (byte) -1;
            this.f = -1;
            h();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 10:
                            this.c |= 1;
                            this.d = codedInputStream.readBytes();
                            break;
                        default:
                            if (parseUnknownField(codedInputStream, extensionRegistryLite, readTag)) {
                                break;
                            }
                            obj = 1;
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

        private g(Builder builder) {
            super(builder);
            this.e = (byte) -1;
            this.f = -1;
        }

        private g(boolean z) {
            this.e = (byte) -1;
            this.f = -1;
        }

        public static a a(g gVar) {
            return e().a(gVar);
        }

        public static g a() {
            return b;
        }

        public static a e() {
            return a.h();
        }

        private void h() {
            this.d = ByteString.EMPTY;
        }

        public g b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ByteString d() {
            return this.d;
        }

        public a f() {
            return e();
        }

        public a g() {
            return a(this);
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<g> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.f;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, this.d) + 0;
            }
            this.f = i;
            return i;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.e;
            if (b == (byte) -1) {
                this.e = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return f();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return g();
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.c & 1) == 1) {
                codedOutputStream.writeBytes(1, this.d);
            }
        }
    }
}
