package com.avast.android.shepherd.obfuscated;

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

/* compiled from: Unknown */
public final class as {

    /* compiled from: Unknown */
    public interface b extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class a extends GeneratedMessageLite implements b {
        public static Parser<a> a = new at();
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
                h = new au();
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

            public b a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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
            public /* synthetic */ MessageLite.Builder m150clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m151clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m152clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m153clone() {
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
            public /* synthetic */ MessageLite m154getDefaultInstanceForType() {
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

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((a) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m155mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.z();
        }

        private a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
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
        public static Parser<c> a = new av();
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

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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
            public /* synthetic */ MessageLite.Builder m156clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m157clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m158clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m159clone() {
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
            public /* synthetic */ MessageLite m160getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((c) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m161mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.l();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private c(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
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
        public static Parser<e> a = new aw();
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

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                e eVar;
                Throwable th;
                e eVar2;
                try {
                    eVar = (e) e.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (eVar != null) {
                        a(eVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    eVar = (e) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    eVar2 = eVar;
                    th = th3;
                }
                if (eVar2 != null) {
                    a(eVar2);
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
            public /* synthetic */ MessageLite.Builder m162clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m163clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m164clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m165clone() {
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
            public /* synthetic */ MessageLite m166getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((e) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m167mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.n();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private e(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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

        public static e a(byte[] bArr) {
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

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
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
        public static Parser<g> a = new ax();
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

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                g gVar;
                Throwable th;
                g gVar2;
                try {
                    gVar = (g) g.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (gVar != null) {
                        a(gVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    gVar = (g) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    gVar2 = gVar;
                    th = th3;
                }
                if (gVar2 != null) {
                    a(gVar2);
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
            public /* synthetic */ MessageLite.Builder m168clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m169clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m170clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m171clone() {
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
            public /* synthetic */ MessageLite m172getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((g) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m173mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.h();
        }

        private g(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            getSerializedSize();
            if ((this.c & 1) == 1) {
                codedOutputStream.writeBytes(1, this.d);
            }
        }
    }

    /* compiled from: Unknown */
    public interface j extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class i extends GeneratedMessageLite implements j {
        public static Parser<i> a = new ay();
        private static final i b = new i(true);
        private int c;
        private b d;
        private ByteString e;
        private ByteString f;
        private long g;
        private long h;
        private byte i;
        private int j;

        /* compiled from: Unknown */
        public static final class a extends Builder<i, a> implements j {
            private int a;
            private b b = b.REMOVE;
            private ByteString c = ByteString.EMPTY;
            private ByteString d = ByteString.EMPTY;
            private long e;
            private long f;

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
                this.b = b.REMOVE;
                this.a &= -2;
                this.c = ByteString.EMPTY;
                this.a &= -3;
                this.d = ByteString.EMPTY;
                this.a &= -5;
                this.e = 0;
                this.a &= -9;
                this.f = 0;
                this.a &= -17;
                return this;
            }

            public a a(long j) {
                this.a |= 8;
                this.e = j;
                return this;
            }

            public a a(b bVar) {
                if (bVar != null) {
                    this.a |= 1;
                    this.b = bVar;
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(i iVar) {
                if (iVar == i.a()) {
                    return this;
                }
                if (iVar.c()) {
                    a(iVar.d());
                }
                if (iVar.e()) {
                    a(iVar.f());
                }
                if (iVar.g()) {
                    b(iVar.h());
                }
                if (iVar.i()) {
                    a(iVar.j());
                }
                if (iVar.k()) {
                    b(iVar.l());
                }
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

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                i iVar;
                Throwable th;
                i iVar2;
                try {
                    iVar = (i) i.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (iVar != null) {
                        a(iVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    iVar = (i) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    iVar2 = iVar;
                    th = th3;
                }
                if (iVar2 != null) {
                    a(iVar2);
                }
                throw th;
            }

            public a b() {
                return h().a(e());
            }

            public a b(long j) {
                this.a |= 16;
                this.f = j;
                return this;
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

            public i c() {
                return i.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m174clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m175clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m176clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m177clone() {
                return b();
            }

            public i d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public i e() {
                i iVar = new i((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                iVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                iVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                iVar.f = this.d;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                iVar.g = this.e;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                iVar.h = this.f;
                iVar.c = i2;
                return iVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m178getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((i) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m179mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        /* compiled from: Unknown */
        public enum b implements EnumLite {
            REMOVE(0, 0),
            LATER(1, 1),
            SEND(2, 2);
            
            private static EnumLiteMap<b> d;
            private final int e;

            static {
                d = new az();
            }

            private b(int i, int i2) {
                this.e = i2;
            }

            public static b a(int i) {
                switch (i) {
                    case 0:
                        return REMOVE;
                    case 1:
                        return LATER;
                    case 2:
                        return SEND;
                    default:
                        return null;
                }
            }

            public final int getNumber() {
                return this.e;
            }
        }

        static {
            b.p();
        }

        private i(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.i = (byte) -1;
            this.j = -1;
            p();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 8:
                            b a = b.a(codedInputStream.readEnum());
                            if (a == null) {
                                break;
                            }
                            this.c |= 1;
                            this.d = a;
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
                        case 40:
                            this.c |= 16;
                            this.h = codedInputStream.readInt64();
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

        private i(Builder builder) {
            super(builder);
            this.i = (byte) -1;
            this.j = -1;
        }

        private i(boolean z) {
            this.i = (byte) -1;
            this.j = -1;
        }

        public static a a(i iVar) {
            return m().a(iVar);
        }

        public static i a() {
            return b;
        }

        public static a m() {
            return a.h();
        }

        private void p() {
            this.d = b.REMOVE;
            this.e = ByteString.EMPTY;
            this.f = ByteString.EMPTY;
            this.g = 0;
            this.h = 0;
        }

        public i b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public b d() {
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

        public Parser<i> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.j;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeEnumSize(1, this.d.getNumber()) + 0;
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
            if ((this.c & 16) == 16) {
                i += CodedOutputStream.computeInt64Size(5, this.h);
            }
            this.j = i;
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
            byte b = this.i;
            if (b == (byte) -1) {
                this.i = (byte) 1;
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

        public boolean k() {
            return (this.c & 16) == 16;
        }

        public long l() {
            return this.h;
        }

        public a n() {
            return m();
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return n();
        }

        public a o() {
            return a(this);
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return o();
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            getSerializedSize();
            if ((this.c & 1) == 1) {
                codedOutputStream.writeEnum(1, this.d.getNumber());
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
            if ((this.c & 16) == 16) {
                codedOutputStream.writeInt64(5, this.h);
            }
        }
    }

    /* compiled from: Unknown */
    public interface l extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class k extends GeneratedMessageLite implements l {
        public static Parser<k> a = new ba();
        private static final k b = new k(true);
        private int c;
        private g d;
        private a e;
        private int f;
        private int g;
        private long h;
        private ByteString i;
        private int j;
        private byte k;
        private int l;

        /* compiled from: Unknown */
        public static final class a extends Builder<k, a> implements l {
            private int a;
            private g b = g.a();
            private a c = a.a();
            private int d;
            private int e;
            private long f;
            private ByteString g = ByteString.EMPTY;
            private int h;

            private a() {
                h();
            }

            private void h() {
            }

            private static a i() {
                return new a();
            }

            public a a() {
                super.clear();
                this.b = g.a();
                this.a &= -2;
                this.c = a.a();
                this.a &= -3;
                this.d = 0;
                this.a &= -5;
                this.e = 0;
                this.a &= -9;
                this.f = 0;
                this.a &= -17;
                this.g = ByteString.EMPTY;
                this.a &= -33;
                this.h = 0;
                this.a &= -65;
                return this;
            }

            public a a(int i) {
                this.a |= 4;
                this.d = i;
                return this;
            }

            public a a(long j) {
                this.a |= 16;
                this.f = j;
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

            public a a(k kVar) {
                if (kVar == k.a()) {
                    return this;
                }
                if (kVar.c()) {
                    a(kVar.d());
                }
                if (kVar.e()) {
                    b(kVar.f());
                }
                if (kVar.g()) {
                    a(kVar.h());
                }
                if (kVar.i()) {
                    b(kVar.j());
                }
                if (kVar.k()) {
                    a(kVar.l());
                }
                if (kVar.m()) {
                    a(kVar.n());
                }
                if (kVar.o()) {
                    c(kVar.p());
                }
                return this;
            }

            public a a(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 32;
                    this.g = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                k kVar;
                Throwable th;
                k kVar2;
                try {
                    kVar = (k) k.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (kVar != null) {
                        a(kVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    kVar = (k) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    kVar2 = kVar;
                    th = th3;
                }
                if (kVar2 != null) {
                    a(kVar2);
                }
                throw th;
            }

            public a b() {
                return i().a(e());
            }

            public a b(int i) {
                this.a |= 8;
                this.e = i;
                return this;
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

            public a c(int i) {
                this.a |= 64;
                this.h = i;
                return this;
            }

            public k c() {
                return k.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m180clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m181clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m182clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m183clone() {
                return b();
            }

            public k d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public k e() {
                k kVar = new k((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                kVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                kVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                kVar.f = this.d;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                kVar.g = this.e;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                kVar.h = this.f;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                kVar.i = this.g;
                if ((i & 64) == 64) {
                    i2 |= 64;
                }
                kVar.j = this.h;
                kVar.c = i2;
                return kVar;
            }

            public g f() {
                return this.b;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m184getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((k) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m185mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.t();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private k(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.k = (byte) -1;
            this.l = -1;
            t();
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
                        case 32:
                            this.c |= 4;
                            this.f = codedInputStream.readInt32();
                            break;
                        case 40:
                            this.c |= 8;
                            this.g = codedInputStream.readInt32();
                            break;
                        case HwRcsCommonObject.BLACKLIST_MSG_VIDEO_TYPE /*48*/:
                            this.c |= 16;
                            this.h = codedInputStream.readInt64();
                            break;
                        case 58:
                            this.c |= 32;
                            this.i = codedInputStream.readBytes();
                            break;
                        case 64:
                            this.c |= 64;
                            this.j = codedInputStream.readInt32();
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

        private k(Builder builder) {
            super(builder);
            this.k = (byte) -1;
            this.l = -1;
        }

        private k(boolean z) {
            this.k = (byte) -1;
            this.l = -1;
        }

        public static a a(k kVar) {
            return q().a(kVar);
        }

        public static k a() {
            return b;
        }

        public static a q() {
            return a.i();
        }

        private void t() {
            this.d = g.a();
            this.e = a.a();
            this.f = 0;
            this.g = 0;
            this.h = 0;
            this.i = ByteString.EMPTY;
            this.j = 0;
        }

        public k b() {
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

        public Parser<k> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.l;
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
                i += CodedOutputStream.computeInt32Size(4, this.f);
            }
            if ((this.c & 8) == 8) {
                i += CodedOutputStream.computeInt32Size(5, this.g);
            }
            if ((this.c & 16) == 16) {
                i += CodedOutputStream.computeInt64Size(6, this.h);
            }
            if ((this.c & 32) == 32) {
                i += CodedOutputStream.computeBytesSize(7, this.i);
            }
            if ((this.c & 64) == 64) {
                i += CodedOutputStream.computeInt32Size(8, this.j);
            }
            this.l = i;
            return i;
        }

        public int h() {
            return this.f;
        }

        public boolean i() {
            return (this.c & 8) == 8;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.k;
            if (b == (byte) -1) {
                this.k = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public int j() {
            return this.g;
        }

        public boolean k() {
            return (this.c & 16) == 16;
        }

        public long l() {
            return this.h;
        }

        public boolean m() {
            return (this.c & 32) == 32;
        }

        public ByteString n() {
            return this.i;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return r();
        }

        public boolean o() {
            return (this.c & 64) == 64;
        }

        public int p() {
            return this.j;
        }

        public a r() {
            return q();
        }

        public a s() {
            return a(this);
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return s();
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            getSerializedSize();
            if ((this.c & 1) == 1) {
                codedOutputStream.writeMessage(1, this.d);
            }
            if ((this.c & 2) == 2) {
                codedOutputStream.writeMessage(2, this.e);
            }
            if ((this.c & 4) == 4) {
                codedOutputStream.writeInt32(4, this.f);
            }
            if ((this.c & 8) == 8) {
                codedOutputStream.writeInt32(5, this.g);
            }
            if ((this.c & 16) == 16) {
                codedOutputStream.writeInt64(6, this.h);
            }
            if ((this.c & 32) == 32) {
                codedOutputStream.writeBytes(7, this.i);
            }
            if ((this.c & 64) == 64) {
                codedOutputStream.writeInt32(8, this.j);
            }
        }
    }

    /* compiled from: Unknown */
    public interface n extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class m extends GeneratedMessageLite implements n {
        public static Parser<m> a = new bb();
        private static final m b = new m(true);
        private int c;
        private g d;
        private i e;
        private ByteString f;
        private byte g;
        private int h;

        /* compiled from: Unknown */
        public static final class a extends Builder<m, a> implements n {
            private int a;
            private g b = g.a();
            private i c = i.a();
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
                this.c = i.a();
                this.a &= -3;
                this.d = ByteString.EMPTY;
                this.a &= -5;
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

            public a a(i iVar) {
                if ((this.a & 2) == 2 && this.c != i.a()) {
                    this.c = i.a(this.c).a(iVar).e();
                } else {
                    this.c = iVar;
                }
                this.a |= 2;
                return this;
            }

            public a a(m mVar) {
                if (mVar == m.a()) {
                    return this;
                }
                if (mVar.c()) {
                    a(mVar.d());
                }
                if (mVar.e()) {
                    a(mVar.f());
                }
                if (mVar.g()) {
                    a(mVar.h());
                }
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

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                m mVar;
                Throwable th;
                m mVar2;
                try {
                    mVar = (m) m.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (mVar != null) {
                        a(mVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    mVar = (m) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    mVar2 = mVar;
                    th = th3;
                }
                if (mVar2 != null) {
                    a(mVar2);
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

            public m c() {
                return m.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m186clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m187clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m188clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m189clone() {
                return b();
            }

            public m d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public m e() {
                m mVar = new m((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                mVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                mVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                mVar.f = this.d;
                mVar.c = i2;
                return mVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m190getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((m) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m191mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.l();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private m(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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
                            a o = (this.c & 2) != 2 ? null : this.e.o();
                            this.e = (i) codedInputStream.readMessage(i.a, extensionRegistryLite);
                            if (o != null) {
                                o.a(this.e);
                                this.e = o.e();
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

        private m(Builder builder) {
            super(builder);
            this.g = (byte) -1;
            this.h = -1;
        }

        private m(boolean z) {
            this.g = (byte) -1;
            this.h = -1;
        }

        public static a a(m mVar) {
            return i().a(mVar);
        }

        public static m a() {
            return b;
        }

        public static m a(byte[] bArr) {
            return (m) a.parseFrom(bArr);
        }

        public static a i() {
            return a.h();
        }

        private void l() {
            this.d = g.a();
            this.e = i.a();
            this.f = ByteString.EMPTY;
        }

        public m b() {
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

        public i f() {
            return this.e;
        }

        public boolean g() {
            return (this.c & 4) == 4;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<m> getParserForType() {
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

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
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
}
