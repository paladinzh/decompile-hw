package com.avast.android.shepherd.obfuscated;

import android.support.v4.view.MotionEventCompat;
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
import com.huawei.systemmanager.comm.widget.CircleViewNew;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* compiled from: Unknown */
public final class bv {

    /* compiled from: Unknown */
    public interface b extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class a extends GeneratedMessageLite implements b {
        public static Parser<a> a = new bw();
        private static final a b = new a(true);
        private int c;
        private ac d;
        private a e;
        private byte f;
        private int g;

        /* compiled from: Unknown */
        public enum a implements EnumLite {
            ADVANCED_FREE(0, 1),
            ADVANCED_PREMIUM(1, 2),
            SIMPLE_FREE(2, 3),
            SIMPLE_PREMIUM(3, 4);
            
            private static EnumLiteMap<a> e;
            private final int f;

            static {
                e = new bx();
            }

            private a(int i, int i2) {
                this.f = i2;
            }

            public static a a(int i) {
                switch (i) {
                    case 1:
                        return ADVANCED_FREE;
                    case 2:
                        return ADVANCED_PREMIUM;
                    case 3:
                        return SIMPLE_FREE;
                    case 4:
                        return SIMPLE_PREMIUM;
                    default:
                        return null;
                }
            }

            public final int getNumber() {
                return this.f;
            }
        }

        /* compiled from: Unknown */
        public static final class b extends Builder<a, b> implements b {
            private int a;
            private ac b = ac.a();
            private a c = a.ADVANCED_FREE;

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
                this.b = ac.a();
                this.a &= -2;
                this.c = a.ADVANCED_FREE;
                this.a &= -3;
                return this;
            }

            public b a(a aVar) {
                if (aVar != null) {
                    this.a |= 2;
                    this.c = aVar;
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
                    a(aVar.f());
                }
                return this;
            }

            public b a(a aVar) {
                this.b = aVar.d();
                this.a |= 1;
                return this;
            }

            public b a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public b a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                a aVar;
                Throwable th;
                a aVar2;
                try {
                    aVar = (a) a.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (aVar != null) {
                        a(aVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    aVar = (a) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    aVar2 = aVar;
                    th = th3;
                }
                if (aVar2 != null) {
                    a(aVar2);
                }
                throw th;
            }

            public b b() {
                return h().a(e());
            }

            public /* synthetic */ MessageLite build() {
                return d();
            }

            public /* synthetic */ MessageLite buildPartial() {
                return e();
            }

            public a c() {
                return a.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m192clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m193clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m194clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m195clone() {
                return b();
            }

            public a d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
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
                aVar.c = i2;
                return aVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m196getDefaultInstanceForType() {
                return c();
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
            public /* synthetic */ MessageLite.Builder m197mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.j();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.f = (byte) -1;
            this.g = -1;
            j();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
                            break;
                        case 16:
                            a a = a.a(codedInputStream.readEnum());
                            if (a != null) {
                                this.c |= 2;
                                this.e = a;
                                break;
                            }
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

        private a(Builder builder) {
            super(builder);
            this.f = (byte) -1;
            this.g = -1;
        }

        private a(boolean z) {
            this.f = (byte) -1;
            this.g = -1;
        }

        public static b a(a aVar) {
            return g().a(aVar);
        }

        public static a a() {
            return b;
        }

        public static b g() {
            return b.h();
        }

        private void j() {
            this.d = ac.a();
            this.e = a.ADVANCED_FREE;
        }

        public a b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
            return this.d;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public a f() {
            return this.e;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<a> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.g;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeEnumSize(2, this.e.getNumber());
            }
            this.g = i;
            return i;
        }

        public b h() {
            return g();
        }

        public b i() {
            return a(this);
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.f;
            if (b == (byte) -1) {
                this.f = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return h();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return i();
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
                codedOutputStream.writeEnum(2, this.e.getNumber());
            }
        }
    }

    /* compiled from: Unknown */
    public interface ab extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class aa extends GeneratedMessageLite implements ab {
        public static Parser<aa> a = new cm();
        private static final aa b = new aa(true);
        private int c;
        private ByteString d;
        private ByteString e;
        private s f;
        private ai g;
        private ByteString h;
        private ByteString i;
        private ByteString j;
        private ByteString k;
        private byte l;
        private int m;

        /* compiled from: Unknown */
        public static final class a extends Builder<aa, a> implements ab {
            private int a;
            private ByteString b = ByteString.EMPTY;
            private ByteString c = ByteString.EMPTY;
            private s d = s.a();
            private ai e = ai.a();
            private ByteString f = ByteString.EMPTY;
            private ByteString g = ByteString.EMPTY;
            private ByteString h = ByteString.EMPTY;
            private ByteString i = ByteString.EMPTY;

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
                this.c = ByteString.EMPTY;
                this.a &= -3;
                this.d = s.a();
                this.a &= -5;
                this.e = ai.a();
                this.a &= -9;
                this.f = ByteString.EMPTY;
                this.a &= -17;
                this.g = ByteString.EMPTY;
                this.a &= -33;
                this.h = ByteString.EMPTY;
                this.a &= -65;
                this.i = ByteString.EMPTY;
                this.a &= -129;
                return this;
            }

            public a a(aa aaVar) {
                if (aaVar == aa.a()) {
                    return this;
                }
                if (aaVar.c()) {
                    a(aaVar.d());
                }
                if (aaVar.e()) {
                    b(aaVar.f());
                }
                if (aaVar.g()) {
                    a(aaVar.h());
                }
                if (aaVar.i()) {
                    a(aaVar.j());
                }
                if (aaVar.k()) {
                    c(aaVar.l());
                }
                if (aaVar.m()) {
                    d(aaVar.n());
                }
                if (aaVar.o()) {
                    e(aaVar.p());
                }
                if (aaVar.q()) {
                    f(aaVar.r());
                }
                return this;
            }

            public a a(ai aiVar) {
                if ((this.a & 8) == 8 && this.e != ai.a()) {
                    this.e = ai.a(this.e).a(aiVar).e();
                } else {
                    this.e = aiVar;
                }
                this.a |= 8;
                return this;
            }

            public a a(a aVar) {
                this.d = aVar.d();
                this.a |= 4;
                return this;
            }

            public a a(s sVar) {
                if ((this.a & 4) == 4 && this.d != s.a()) {
                    this.d = s.a(this.d).a(sVar).e();
                } else {
                    this.d = sVar;
                }
                this.a |= 4;
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
                Throwable th;
                aa aaVar;
                aa aaVar2;
                try {
                    aaVar2 = (aa) aa.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (aaVar2 != null) {
                        a(aaVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    aaVar2 = (aa) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    aaVar = aaVar2;
                    th = th3;
                }
                if (aaVar != null) {
                    a(aaVar);
                }
                throw th;
            }

            public a b() {
                return h().a(e());
            }

            public a b(ByteString byteString) {
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

            public a c(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 16;
                    this.f = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public aa c() {
                return aa.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m198clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m199clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m200clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m201clone() {
                return b();
            }

            public a d(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 32;
                    this.g = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public aa d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public a e(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 64;
                    this.h = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public aa e() {
                aa aaVar = new aa((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                aaVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                aaVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                aaVar.f = this.d;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                aaVar.g = this.e;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                aaVar.h = this.f;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                aaVar.i = this.g;
                if ((i & 64) == 64) {
                    i2 |= 64;
                }
                aaVar.j = this.h;
                if ((i & 128) == 128) {
                    i2 |= 128;
                }
                aaVar.k = this.i;
                aaVar.c = i2;
                return aaVar;
            }

            public a f(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 128;
                    this.i = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m202getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((aa) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m203mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.v();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private aa(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.l = (byte) -1;
            this.m = -1;
            v();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
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
                            a q = (this.c & 4) != 4 ? null : this.f.q();
                            this.f = (s) codedInputStream.readMessage(s.a, extensionRegistryLite);
                            if (q != null) {
                                q.a(this.f);
                                this.f = q.e();
                            }
                            this.c |= 4;
                            break;
                        case 34:
                            a i = (this.c & 8) != 8 ? null : this.g.i();
                            this.g = (ai) codedInputStream.readMessage(ai.a, extensionRegistryLite);
                            if (i != null) {
                                i.a(this.g);
                                this.g = i.e();
                            }
                            this.c |= 8;
                            break;
                        case MotionEventCompat.AXIS_GENERIC_11 /*42*/:
                            this.c |= 16;
                            this.h = codedInputStream.readBytes();
                            break;
                        case 50:
                            this.c |= 32;
                            this.i = codedInputStream.readBytes();
                            break;
                        case 58:
                            this.c |= 64;
                            this.j = codedInputStream.readBytes();
                            break;
                        case Events.E_ANTISPAM_VIEW_CALL /*66*/:
                            this.c |= 128;
                            this.k = codedInputStream.readBytes();
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

        private aa(Builder builder) {
            super(builder);
            this.l = (byte) -1;
            this.m = -1;
        }

        private aa(boolean z) {
            this.l = (byte) -1;
            this.m = -1;
        }

        public static a a(aa aaVar) {
            return s().a(aaVar);
        }

        public static aa a() {
            return b;
        }

        public static a s() {
            return a.h();
        }

        private void v() {
            this.d = ByteString.EMPTY;
            this.e = ByteString.EMPTY;
            this.f = s.a();
            this.g = ai.a();
            this.h = ByteString.EMPTY;
            this.i = ByteString.EMPTY;
            this.j = ByteString.EMPTY;
            this.k = ByteString.EMPTY;
        }

        public aa b() {
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

        public Parser<aa> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.m;
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
                i += CodedOutputStream.computeMessageSize(3, this.f);
            }
            if ((this.c & 8) == 8) {
                i += CodedOutputStream.computeMessageSize(4, this.g);
            }
            if ((this.c & 16) == 16) {
                i += CodedOutputStream.computeBytesSize(5, this.h);
            }
            if ((this.c & 32) == 32) {
                i += CodedOutputStream.computeBytesSize(6, this.i);
            }
            if ((this.c & 64) == 64) {
                i += CodedOutputStream.computeBytesSize(7, this.j);
            }
            if ((this.c & 128) == 128) {
                i += CodedOutputStream.computeBytesSize(8, this.k);
            }
            this.m = i;
            return i;
        }

        public s h() {
            return this.f;
        }

        public boolean i() {
            return (this.c & 8) == 8;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.l;
            if (b == (byte) -1) {
                this.l = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public ai j() {
            return this.g;
        }

        public boolean k() {
            return (this.c & 16) == 16;
        }

        public ByteString l() {
            return this.h;
        }

        public boolean m() {
            return (this.c & 32) == 32;
        }

        public ByteString n() {
            return this.i;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return t();
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

        public a t() {
            return s();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return u();
        }

        public a u() {
            return a(this);
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
                codedOutputStream.writeMessage(3, this.f);
            }
            if ((this.c & 8) == 8) {
                codedOutputStream.writeMessage(4, this.g);
            }
            if ((this.c & 16) == 16) {
                codedOutputStream.writeBytes(5, this.h);
            }
            if ((this.c & 32) == 32) {
                codedOutputStream.writeBytes(6, this.i);
            }
            if ((this.c & 64) == 64) {
                codedOutputStream.writeBytes(7, this.j);
            }
            if ((this.c & 128) == 128) {
                codedOutputStream.writeBytes(8, this.k);
            }
        }
    }

    /* compiled from: Unknown */
    public interface ad extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class ac extends GeneratedMessageLite implements ad {
        public static Parser<ac> a = new cn();
        private static final ac b = new ac(true);
        private int c;
        private ByteString d;
        private ByteString e;
        private ByteString f;
        private ByteString g;
        private ByteString h;
        private ByteString i;
        private ByteString j;
        private ByteString k;
        private int l;
        private byte m;
        private int n;

        /* compiled from: Unknown */
        public static final class a extends Builder<ac, a> implements ad {
            private int a;
            private ByteString b = ByteString.EMPTY;
            private ByteString c = ByteString.EMPTY;
            private ByteString d = ByteString.EMPTY;
            private ByteString e = ByteString.EMPTY;
            private ByteString f = ByteString.EMPTY;
            private ByteString g = ByteString.EMPTY;
            private ByteString h = ByteString.EMPTY;
            private ByteString i = ByteString.EMPTY;
            private int j;

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
                this.c = ByteString.EMPTY;
                this.a &= -3;
                this.d = ByteString.EMPTY;
                this.a &= -5;
                this.e = ByteString.EMPTY;
                this.a &= -9;
                this.f = ByteString.EMPTY;
                this.a &= -17;
                this.g = ByteString.EMPTY;
                this.a &= -33;
                this.h = ByteString.EMPTY;
                this.a &= -65;
                this.i = ByteString.EMPTY;
                this.a &= -129;
                this.j = 0;
                this.a &= -257;
                return this;
            }

            public a a(int i) {
                this.a |= 256;
                this.j = i;
                return this;
            }

            public a a(ac acVar) {
                if (acVar == ac.a()) {
                    return this;
                }
                if (acVar.c()) {
                    a(acVar.d());
                }
                if (acVar.e()) {
                    b(acVar.f());
                }
                if (acVar.g()) {
                    c(acVar.h());
                }
                if (acVar.i()) {
                    d(acVar.j());
                }
                if (acVar.k()) {
                    e(acVar.l());
                }
                if (acVar.m()) {
                    f(acVar.n());
                }
                if (acVar.o()) {
                    g(acVar.p());
                }
                if (acVar.q()) {
                    h(acVar.r());
                }
                if (acVar.s()) {
                    a(acVar.t());
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
                Throwable th;
                ac acVar;
                ac acVar2;
                try {
                    acVar2 = (ac) ac.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (acVar2 != null) {
                        a(acVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    acVar2 = (ac) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    acVar = acVar2;
                    th = th3;
                }
                if (acVar != null) {
                    a(acVar);
                }
                throw th;
            }

            public a b() {
                return h().a(e());
            }

            public a b(ByteString byteString) {
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

            public a c(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 4;
                    this.d = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public ac c() {
                return ac.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m204clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m205clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m206clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m207clone() {
                return b();
            }

            public a d(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 8;
                    this.e = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public ac d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public a e(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 16;
                    this.f = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public ac e() {
                ac acVar = new ac((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                acVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                acVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                acVar.f = this.d;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                acVar.g = this.e;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                acVar.h = this.f;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                acVar.i = this.g;
                if ((i & 64) == 64) {
                    i2 |= 64;
                }
                acVar.j = this.h;
                if ((i & 128) == 128) {
                    i2 |= 128;
                }
                acVar.k = this.i;
                if ((i & 256) == 256) {
                    i2 |= 256;
                }
                acVar.l = this.j;
                acVar.c = i2;
                return acVar;
            }

            public a f(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 32;
                    this.g = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a g(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 64;
                    this.h = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m208getDefaultInstanceForType() {
                return c();
            }

            public a h(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 128;
                    this.i = byteString;
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
                return a((ac) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m209mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.x();
        }

        private ac(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.m = (byte) -1;
            this.n = -1;
            x();
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
                        case MotionEventCompat.AXIS_GENERIC_11 /*42*/:
                            this.c |= 16;
                            this.h = codedInputStream.readBytes();
                            break;
                        case 50:
                            this.c |= 32;
                            this.i = codedInputStream.readBytes();
                            break;
                        case 58:
                            this.c |= 64;
                            this.j = codedInputStream.readBytes();
                            break;
                        case Events.E_ANTISPAM_VIEW_CALL /*66*/:
                            this.c |= 128;
                            this.k = codedInputStream.readBytes();
                            break;
                        case Events.E_ANTISPAM_ADD_BLACKLIST /*72*/:
                            this.c |= 256;
                            this.l = codedInputStream.readInt32();
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

        private ac(Builder builder) {
            super(builder);
            this.m = (byte) -1;
            this.n = -1;
        }

        private ac(boolean z) {
            this.m = (byte) -1;
            this.n = -1;
        }

        public static a a(ac acVar) {
            return u().a(acVar);
        }

        public static ac a() {
            return b;
        }

        public static a u() {
            return a.h();
        }

        private void x() {
            this.d = ByteString.EMPTY;
            this.e = ByteString.EMPTY;
            this.f = ByteString.EMPTY;
            this.g = ByteString.EMPTY;
            this.h = ByteString.EMPTY;
            this.i = ByteString.EMPTY;
            this.j = ByteString.EMPTY;
            this.k = ByteString.EMPTY;
            this.l = 0;
        }

        public ac b() {
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

        public Parser<ac> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.n;
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
                i += CodedOutputStream.computeBytesSize(5, this.h);
            }
            if ((this.c & 32) == 32) {
                i += CodedOutputStream.computeBytesSize(6, this.i);
            }
            if ((this.c & 64) == 64) {
                i += CodedOutputStream.computeBytesSize(7, this.j);
            }
            if ((this.c & 128) == 128) {
                i += CodedOutputStream.computeBytesSize(8, this.k);
            }
            if ((this.c & 256) == 256) {
                i += CodedOutputStream.computeInt32Size(9, this.l);
            }
            this.n = i;
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
            byte b = this.m;
            if (b == (byte) -1) {
                this.m = (byte) 1;
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

        public ByteString l() {
            return this.h;
        }

        public boolean m() {
            return (this.c & 32) == 32;
        }

        public ByteString n() {
            return this.i;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return v();
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

        public int t() {
            return this.l;
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return w();
        }

        public a v() {
            return u();
        }

        public a w() {
            return a(this);
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
                codedOutputStream.writeBytes(5, this.h);
            }
            if ((this.c & 32) == 32) {
                codedOutputStream.writeBytes(6, this.i);
            }
            if ((this.c & 64) == 64) {
                codedOutputStream.writeBytes(7, this.j);
            }
            if ((this.c & 128) == 128) {
                codedOutputStream.writeBytes(8, this.k);
            }
            if ((this.c & 256) == 256) {
                codedOutputStream.writeInt32(9, this.l);
            }
        }
    }

    /* compiled from: Unknown */
    public interface af extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class ae extends GeneratedMessageLite implements af {
        public static Parser<ae> a = new co();
        private static final ae b = new ae(true);
        private int c;
        private ByteString d;
        private ByteString e;
        private ByteString f;
        private byte g;
        private int h;

        /* compiled from: Unknown */
        public static final class a extends Builder<ae, a> implements af {
            private int a;
            private ByteString b = ByteString.EMPTY;
            private ByteString c = ByteString.EMPTY;
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
                this.b = ByteString.EMPTY;
                this.a &= -2;
                this.c = ByteString.EMPTY;
                this.a &= -3;
                this.d = ByteString.EMPTY;
                this.a &= -5;
                return this;
            }

            public a a(ae aeVar) {
                if (aeVar == ae.a()) {
                    return this;
                }
                if (aeVar.c()) {
                    a(aeVar.d());
                }
                if (aeVar.e()) {
                    b(aeVar.f());
                }
                if (aeVar.g()) {
                    c(aeVar.h());
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
                Throwable th;
                ae aeVar;
                ae aeVar2;
                try {
                    aeVar2 = (ae) ae.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (aeVar2 != null) {
                        a(aeVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    aeVar2 = (ae) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    aeVar = aeVar2;
                    th = th3;
                }
                if (aeVar != null) {
                    a(aeVar);
                }
                throw th;
            }

            public a b() {
                return h().a(e());
            }

            public a b(ByteString byteString) {
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

            public a c(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 4;
                    this.d = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public ae c() {
                return ae.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m210clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m211clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m212clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m213clone() {
                return b();
            }

            public ae d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public ae e() {
                ae aeVar = new ae((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                aeVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                aeVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                aeVar.f = this.d;
                aeVar.c = i2;
                return aeVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m214getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((ae) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m215mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.l();
        }

        private ae(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.g = (byte) -1;
            this.h = -1;
            l();
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

        private ae(Builder builder) {
            super(builder);
            this.g = (byte) -1;
            this.h = -1;
        }

        private ae(boolean z) {
            this.g = (byte) -1;
            this.h = -1;
        }

        public static a a(ae aeVar) {
            return i().a(aeVar);
        }

        public static ae a() {
            return b;
        }

        public static a i() {
            return a.h();
        }

        private void l() {
            this.d = ByteString.EMPTY;
            this.e = ByteString.EMPTY;
            this.f = ByteString.EMPTY;
        }

        public ae b() {
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

        public Parser<ae> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.h;
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
                codedOutputStream.writeBytes(1, this.d);
            }
            if ((this.c & 2) == 2) {
                codedOutputStream.writeBytes(2, this.e);
            }
            if ((this.c & 4) == 4) {
                codedOutputStream.writeBytes(3, this.f);
            }
        }
    }

    /* compiled from: Unknown */
    public interface ah extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class ag extends GeneratedMessageLite implements ah {
        public static Parser<ag> a = new cp();
        private static final ag b = new ag(true);
        private int c;
        private ac d;
        private ByteString e;
        private byte f;
        private int g;

        /* compiled from: Unknown */
        public static final class a extends Builder<ag, a> implements ah {
            private int a;
            private ac b = ac.a();
            private ByteString c = ByteString.EMPTY;

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
                this.b = ac.a();
                this.a &= -2;
                this.c = ByteString.EMPTY;
                this.a &= -3;
                return this;
            }

            public a a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(ag agVar) {
                if (agVar == ag.a()) {
                    return this;
                }
                if (agVar.c()) {
                    a(agVar.d());
                }
                if (agVar.e()) {
                    a(agVar.f());
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
                ag agVar;
                Throwable th;
                ag agVar2;
                try {
                    agVar = (ag) ag.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (agVar != null) {
                        a(agVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    agVar = (ag) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    agVar2 = agVar;
                    th = th3;
                }
                if (agVar2 != null) {
                    a(agVar2);
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

            public ag c() {
                return ag.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m216clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m217clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m218clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m219clone() {
                return b();
            }

            public ag d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public ag e() {
                ag agVar = new ag((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                agVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                agVar.e = this.c;
                agVar.c = i2;
                return agVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m220getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((ag) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m221mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.j();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private ag(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.f = (byte) -1;
            this.g = -1;
            j();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
                            break;
                        case 18:
                            this.c |= 2;
                            this.e = codedInputStream.readBytes();
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

        private ag(Builder builder) {
            super(builder);
            this.f = (byte) -1;
            this.g = -1;
        }

        private ag(boolean z) {
            this.f = (byte) -1;
            this.g = -1;
        }

        public static a a(ag agVar) {
            return g().a(agVar);
        }

        public static ag a() {
            return b;
        }

        public static a g() {
            return a.h();
        }

        private void j() {
            this.d = ac.a();
            this.e = ByteString.EMPTY;
        }

        public ag b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
            return this.d;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public ByteString f() {
            return this.e;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<ag> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.g;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, this.e);
            }
            this.g = i;
            return i;
        }

        public a h() {
            return g();
        }

        public a i() {
            return a(this);
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.f;
            if (b == (byte) -1) {
                this.f = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return h();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return i();
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
        }
    }

    /* compiled from: Unknown */
    public interface aj extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class ai extends GeneratedMessageLite implements aj {
        public static Parser<ai> a = new cq();
        private static final ai b = new ai(true);
        private int c;
        private ByteString d;
        private ByteString e;
        private byte f;
        private int g;

        /* compiled from: Unknown */
        public static final class a extends Builder<ai, a> implements aj {
            private int a;
            private ByteString b = ByteString.EMPTY;
            private ByteString c = ByteString.EMPTY;

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
                this.c = ByteString.EMPTY;
                this.a &= -3;
                return this;
            }

            public a a(ai aiVar) {
                if (aiVar == ai.a()) {
                    return this;
                }
                if (aiVar.c()) {
                    a(aiVar.d());
                }
                if (aiVar.e()) {
                    b(aiVar.f());
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
                Throwable th;
                ai aiVar;
                ai aiVar2;
                try {
                    aiVar2 = (ai) ai.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (aiVar2 != null) {
                        a(aiVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    aiVar2 = (ai) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    aiVar = aiVar2;
                    th = th3;
                }
                if (aiVar != null) {
                    a(aiVar);
                }
                throw th;
            }

            public a b() {
                return h().a(e());
            }

            public a b(ByteString byteString) {
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

            public ai c() {
                return ai.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m222clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m223clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m224clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m225clone() {
                return b();
            }

            public ai d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public ai e() {
                ai aiVar = new ai((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                aiVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                aiVar.e = this.c;
                aiVar.c = i2;
                return aiVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m226getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((ai) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m227mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.j();
        }

        private ai(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.f = (byte) -1;
            this.g = -1;
            j();
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

        private ai(Builder builder) {
            super(builder);
            this.f = (byte) -1;
            this.g = -1;
        }

        private ai(boolean z) {
            this.f = (byte) -1;
            this.g = -1;
        }

        public static a a(ai aiVar) {
            return g().a(aiVar);
        }

        public static ai a() {
            return b;
        }

        public static a g() {
            return a.h();
        }

        private void j() {
            this.d = ByteString.EMPTY;
            this.e = ByteString.EMPTY;
        }

        public ai b() {
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

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<ai> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.g;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, this.e);
            }
            this.g = i;
            return i;
        }

        public a h() {
            return g();
        }

        public a i() {
            return a(this);
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.f;
            if (b == (byte) -1) {
                this.f = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return h();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return i();
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
        }
    }

    /* compiled from: Unknown */
    public interface al extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class ak extends GeneratedMessageLite implements al {
        public static Parser<ak> a = new cr();
        private static final ak b = new ak(true);
        private int c;
        private ac d;
        private ByteString e;
        private byte f;
        private int g;

        /* compiled from: Unknown */
        public static final class a extends Builder<ak, a> implements al {
            private int a;
            private ac b = ac.a();
            private ByteString c = ByteString.EMPTY;

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
                this.b = ac.a();
                this.a &= -2;
                this.c = ByteString.EMPTY;
                this.a &= -3;
                return this;
            }

            public a a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(ak akVar) {
                if (akVar == ak.a()) {
                    return this;
                }
                if (akVar.c()) {
                    a(akVar.d());
                }
                if (akVar.e()) {
                    a(akVar.f());
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
                ak akVar;
                Throwable th;
                ak akVar2;
                try {
                    akVar = (ak) ak.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (akVar != null) {
                        a(akVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    akVar = (ak) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    akVar2 = akVar;
                    th = th3;
                }
                if (akVar2 != null) {
                    a(akVar2);
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

            public ak c() {
                return ak.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m228clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m229clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m230clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m231clone() {
                return b();
            }

            public ak d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public ak e() {
                ak akVar = new ak((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                akVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                akVar.e = this.c;
                akVar.c = i2;
                return akVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m232getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((ak) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m233mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.j();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private ak(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.f = (byte) -1;
            this.g = -1;
            j();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
                            break;
                        case 18:
                            this.c |= 2;
                            this.e = codedInputStream.readBytes();
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

        private ak(Builder builder) {
            super(builder);
            this.f = (byte) -1;
            this.g = -1;
        }

        private ak(boolean z) {
            this.f = (byte) -1;
            this.g = -1;
        }

        public static a a(ak akVar) {
            return g().a(akVar);
        }

        public static ak a() {
            return b;
        }

        public static a g() {
            return a.h();
        }

        private void j() {
            this.d = ac.a();
            this.e = ByteString.EMPTY;
        }

        public ak b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
            return this.d;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public ByteString f() {
            return this.e;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<ak> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.g;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, this.e);
            }
            this.g = i;
            return i;
        }

        public a h() {
            return g();
        }

        public a i() {
            return a(this);
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.f;
            if (b == (byte) -1) {
                this.f = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return h();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return i();
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
        }
    }

    /* compiled from: Unknown */
    public interface an extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class am extends GeneratedMessageLite implements an {
        public static Parser<am> a = new cs();
        private static final am b = new am(true);
        private int c;
        private ac d;
        private ByteString e;
        private byte f;
        private int g;

        /* compiled from: Unknown */
        public static final class a extends Builder<am, a> implements an {
            private int a;
            private ac b = ac.a();
            private ByteString c = ByteString.EMPTY;

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
                this.b = ac.a();
                this.a &= -2;
                this.c = ByteString.EMPTY;
                this.a &= -3;
                return this;
            }

            public a a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(am amVar) {
                if (amVar == am.a()) {
                    return this;
                }
                if (amVar.c()) {
                    a(amVar.d());
                }
                if (amVar.e()) {
                    a(amVar.f());
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
                Throwable th;
                am amVar;
                am amVar2;
                try {
                    amVar2 = (am) am.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (amVar2 != null) {
                        a(amVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    amVar2 = (am) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    amVar = amVar2;
                    th = th3;
                }
                if (amVar != null) {
                    a(amVar);
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

            public am c() {
                return am.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m234clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m235clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m236clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m237clone() {
                return b();
            }

            public am d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public am e() {
                am amVar = new am((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                amVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                amVar.e = this.c;
                amVar.c = i2;
                return amVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m238getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((am) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m239mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.j();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private am(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.f = (byte) -1;
            this.g = -1;
            j();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
                            break;
                        case 18:
                            this.c |= 2;
                            this.e = codedInputStream.readBytes();
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

        private am(Builder builder) {
            super(builder);
            this.f = (byte) -1;
            this.g = -1;
        }

        private am(boolean z) {
            this.f = (byte) -1;
            this.g = -1;
        }

        public static a a(am amVar) {
            return g().a(amVar);
        }

        public static am a() {
            return b;
        }

        public static a g() {
            return a.h();
        }

        private void j() {
            this.d = ac.a();
            this.e = ByteString.EMPTY;
        }

        public am b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
            return this.d;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public ByteString f() {
            return this.e;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<am> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.g;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, this.e);
            }
            this.g = i;
            return i;
        }

        public a h() {
            return g();
        }

        public a i() {
            return a(this);
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.f;
            if (b == (byte) -1) {
                this.f = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return h();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return i();
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
        }
    }

    /* compiled from: Unknown */
    public interface ap extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class ao extends GeneratedMessageLite implements ap {
        public static Parser<ao> a = new ct();
        private static final ao b = new ao(true);
        private int c;
        private ac d;
        private ByteString e;
        private byte f;
        private int g;

        /* compiled from: Unknown */
        public static final class a extends Builder<ao, a> implements ap {
            private int a;
            private ac b = ac.a();
            private ByteString c = ByteString.EMPTY;

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
                this.b = ac.a();
                this.a &= -2;
                this.c = ByteString.EMPTY;
                this.a &= -3;
                return this;
            }

            public a a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(ao aoVar) {
                if (aoVar == ao.a()) {
                    return this;
                }
                if (aoVar.c()) {
                    a(aoVar.d());
                }
                if (aoVar.e()) {
                    a(aoVar.f());
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
                Throwable th;
                ao aoVar;
                ao aoVar2;
                try {
                    aoVar2 = (ao) ao.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (aoVar2 != null) {
                        a(aoVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    aoVar2 = (ao) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    aoVar = aoVar2;
                    th = th3;
                }
                if (aoVar != null) {
                    a(aoVar);
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

            public ao c() {
                return ao.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m240clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m241clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m242clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m243clone() {
                return b();
            }

            public ao d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public ao e() {
                ao aoVar = new ao((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                aoVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                aoVar.e = this.c;
                aoVar.c = i2;
                return aoVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m244getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((ao) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m245mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.j();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private ao(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.f = (byte) -1;
            this.g = -1;
            j();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
                            break;
                        case 18:
                            this.c |= 2;
                            this.e = codedInputStream.readBytes();
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

        private ao(Builder builder) {
            super(builder);
            this.f = (byte) -1;
            this.g = -1;
        }

        private ao(boolean z) {
            this.f = (byte) -1;
            this.g = -1;
        }

        public static a a(ao aoVar) {
            return g().a(aoVar);
        }

        public static ao a() {
            return b;
        }

        public static a g() {
            return a.h();
        }

        private void j() {
            this.d = ac.a();
            this.e = ByteString.EMPTY;
        }

        public ao b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
            return this.d;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public ByteString f() {
            return this.e;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<ao> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.g;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, this.e);
            }
            this.g = i;
            return i;
        }

        public a h() {
            return g();
        }

        public a i() {
            return a(this);
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.f;
            if (b == (byte) -1) {
                this.f = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return h();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return i();
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
        }
    }

    /* compiled from: Unknown */
    public interface ar extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class aq extends GeneratedMessageLite implements ar {
        public static Parser<aq> a = new cu();
        private static final aq b = new aq(true);
        private int c;
        private b d;
        private List<ByteString> e;
        private aa f;
        private q g;
        private ak h;
        private a i;
        private g j;
        private ag k;
        private i l;
        private w m;
        private u n;
        private m o;
        private o p;
        private k q;
        private List<c> r;
        private e s;
        private c t;
        private am u;
        private ao v;
        private y w;
        private byte x;
        private int y;

        /* compiled from: Unknown */
        public static final class a extends Builder<aq, a> implements ar {
            private int a;
            private b b = b.UNKNOWN;
            private List<ByteString> c = Collections.emptyList();
            private aa d = aa.a();
            private q e = q.a();
            private ak f = ak.a();
            private a g = a.a();
            private g h = g.a();
            private ag i = ag.a();
            private i j = i.a();
            private w k = w.a();
            private u l = u.a();
            private m m = m.a();
            private o n = o.a();
            private k o = k.a();
            private List<c> p = Collections.emptyList();
            private e q = e.a();
            private c r = c.a();
            private am s = am.a();
            private ao t = ao.a();
            private y u = y.a();

            private a() {
                i();
            }

            private void i() {
            }

            private static a j() {
                return new a();
            }

            private void k() {
                if ((this.a & 2) != 2) {
                    this.c = new ArrayList(this.c);
                    this.a |= 2;
                }
            }

            private void l() {
                if ((this.a & 16384) != 16384) {
                    this.p = new ArrayList(this.p);
                    this.a |= 16384;
                }
            }

            public a a() {
                super.clear();
                this.b = b.UNKNOWN;
                this.a &= -2;
                this.c = Collections.emptyList();
                this.a &= -3;
                this.d = aa.a();
                this.a &= -5;
                this.e = q.a();
                this.a &= -9;
                this.f = ak.a();
                this.a &= -17;
                this.g = a.a();
                this.a &= -33;
                this.h = g.a();
                this.a &= -65;
                this.i = ag.a();
                this.a &= -129;
                this.j = i.a();
                this.a &= -257;
                this.k = w.a();
                this.a &= -513;
                this.l = u.a();
                this.a &= -1025;
                this.m = m.a();
                this.a &= -2049;
                this.n = o.a();
                this.a &= -4097;
                this.o = k.a();
                this.a &= -8193;
                this.p = Collections.emptyList();
                this.a &= -16385;
                this.q = e.a();
                this.a &= -32769;
                this.r = c.a();
                this.a &= -65537;
                this.s = am.a();
                this.a &= -131073;
                this.t = ao.a();
                this.a &= -262145;
                this.u = y.a();
                this.a &= -524289;
                return this;
            }

            public a a(b bVar) {
                this.g = bVar.d();
                this.a |= 32;
                return this;
            }

            public a a(a aVar) {
                if ((this.a & 32) == 32 && this.g != a.a()) {
                    this.g = a.a(this.g).a(aVar).e();
                } else {
                    this.g = aVar;
                }
                this.a |= 32;
                return this;
            }

            public a a(a aVar) {
                this.d = aVar.d();
                this.a |= 4;
                return this;
            }

            public a a(aa aaVar) {
                if ((this.a & 4) == 4 && this.d != aa.a()) {
                    this.d = aa.a(this.d).a(aaVar).e();
                } else {
                    this.d = aaVar;
                }
                this.a |= 4;
                return this;
            }

            public a a(ag agVar) {
                if ((this.a & 128) == 128 && this.i != ag.a()) {
                    this.i = ag.a(this.i).a(agVar).e();
                } else {
                    this.i = agVar;
                }
                this.a |= 128;
                return this;
            }

            public a a(ak akVar) {
                if ((this.a & 16) == 16 && this.f != ak.a()) {
                    this.f = ak.a(this.f).a(akVar).e();
                } else {
                    this.f = akVar;
                }
                this.a |= 16;
                return this;
            }

            public a a(am amVar) {
                if ((this.a & 131072) == 131072 && this.s != am.a()) {
                    this.s = am.a(this.s).a(amVar).e();
                } else {
                    this.s = amVar;
                }
                this.a |= 131072;
                return this;
            }

            public a a(ao aoVar) {
                if ((this.a & 262144) == 262144 && this.t != ao.a()) {
                    this.t = ao.a(this.t).a(aoVar).e();
                } else {
                    this.t = aoVar;
                }
                this.a |= 262144;
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

            public a a(c cVar) {
                if (cVar != null) {
                    l();
                    this.p.add(cVar);
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(aq aqVar) {
                if (aqVar == aq.a()) {
                    return this;
                }
                if (aqVar.c()) {
                    a(aqVar.d());
                }
                if (!aqVar.e.isEmpty()) {
                    if (this.c.isEmpty()) {
                        this.c = aqVar.e;
                        this.a &= -3;
                    } else {
                        k();
                        this.c.addAll(aqVar.e);
                    }
                }
                if (aqVar.f()) {
                    a(aqVar.g());
                }
                if (aqVar.h()) {
                    a(aqVar.i());
                }
                if (aqVar.j()) {
                    a(aqVar.k());
                }
                if (aqVar.l()) {
                    a(aqVar.m());
                }
                if (aqVar.n()) {
                    a(aqVar.o());
                }
                if (aqVar.p()) {
                    a(aqVar.q());
                }
                if (aqVar.r()) {
                    a(aqVar.s());
                }
                if (aqVar.t()) {
                    a(aqVar.u());
                }
                if (aqVar.v()) {
                    a(aqVar.w());
                }
                if (aqVar.x()) {
                    a(aqVar.y());
                }
                if (aqVar.z()) {
                    a(aqVar.A());
                }
                if (aqVar.B()) {
                    a(aqVar.C());
                }
                if (!aqVar.r.isEmpty()) {
                    if (this.p.isEmpty()) {
                        this.p = aqVar.r;
                        this.a &= -16385;
                    } else {
                        l();
                        this.p.addAll(aqVar.r);
                    }
                }
                if (aqVar.D()) {
                    a(aqVar.E());
                }
                if (aqVar.F()) {
                    a(aqVar.G());
                }
                if (aqVar.H()) {
                    a(aqVar.I());
                }
                if (aqVar.J()) {
                    a(aqVar.K());
                }
                if (aqVar.L()) {
                    a(aqVar.M());
                }
                return this;
            }

            public a a(a aVar) {
                this.r = aVar.d();
                this.a |= 65536;
                return this;
            }

            public a a(c cVar) {
                if ((this.a & 65536) == 65536 && this.r != c.a()) {
                    this.r = c.a(this.r).a(cVar).e();
                } else {
                    this.r = cVar;
                }
                this.a |= 65536;
                return this;
            }

            public a a(a aVar) {
                this.q = aVar.d();
                this.a |= 32768;
                return this;
            }

            public a a(e eVar) {
                if ((this.a & 32768) == 32768 && this.q != e.a()) {
                    this.q = e.a(this.q).a(eVar).e();
                } else {
                    this.q = eVar;
                }
                this.a |= 32768;
                return this;
            }

            public a a(b bVar) {
                this.h = bVar.d();
                this.a |= 64;
                return this;
            }

            public a a(g gVar) {
                if ((this.a & 64) == 64 && this.h != g.a()) {
                    this.h = g.a(this.h).a(gVar).e();
                } else {
                    this.h = gVar;
                }
                this.a |= 64;
                return this;
            }

            public a a(a aVar) {
                this.j = aVar.d();
                this.a |= 256;
                return this;
            }

            public a a(i iVar) {
                if ((this.a & 256) == 256 && this.j != i.a()) {
                    this.j = i.a(this.j).a(iVar).e();
                } else {
                    this.j = iVar;
                }
                this.a |= 256;
                return this;
            }

            public a a(a aVar) {
                this.o = aVar.d();
                this.a |= 8192;
                return this;
            }

            public a a(k kVar) {
                if ((this.a & 8192) == 8192 && this.o != k.a()) {
                    this.o = k.a(this.o).a(kVar).e();
                } else {
                    this.o = kVar;
                }
                this.a |= 8192;
                return this;
            }

            public a a(m mVar) {
                if ((this.a & 2048) == 2048 && this.m != m.a()) {
                    this.m = m.a(this.m).a(mVar).e();
                } else {
                    this.m = mVar;
                }
                this.a |= 2048;
                return this;
            }

            public a a(o oVar) {
                if ((this.a & 4096) == 4096 && this.n != o.a()) {
                    this.n = o.a(this.n).a(oVar).e();
                } else {
                    this.n = oVar;
                }
                this.a |= 4096;
                return this;
            }

            public a a(b bVar) {
                this.e = bVar.d();
                this.a |= 8;
                return this;
            }

            public a a(q qVar) {
                if ((this.a & 8) == 8 && this.e != q.a()) {
                    this.e = q.a(this.e).a(qVar).e();
                } else {
                    this.e = qVar;
                }
                this.a |= 8;
                return this;
            }

            public a a(u uVar) {
                if ((this.a & 1024) == 1024 && this.l != u.a()) {
                    this.l = u.a(this.l).a(uVar).e();
                } else {
                    this.l = uVar;
                }
                this.a |= 1024;
                return this;
            }

            public a a(a aVar) {
                this.k = aVar.d();
                this.a |= 512;
                return this;
            }

            public a a(w wVar) {
                if ((this.a & 512) == 512 && this.k != w.a()) {
                    this.k = w.a(this.k).a(wVar).e();
                } else {
                    this.k = wVar;
                }
                this.a |= 512;
                return this;
            }

            public a a(y yVar) {
                if ((this.a & 524288) == 524288 && this.u != y.a()) {
                    this.u = y.a(this.u).a(yVar).e();
                } else {
                    this.u = yVar;
                }
                this.a |= 524288;
                return this;
            }

            public a a(ByteString byteString) {
                if (byteString != null) {
                    k();
                    this.c.add(byteString);
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                Throwable th;
                aq aqVar;
                aq aqVar2;
                try {
                    aqVar2 = (aq) aq.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (aqVar2 != null) {
                        a(aqVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    aqVar2 = (aq) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    aqVar = aqVar2;
                    th = th3;
                }
                if (aqVar != null) {
                    a(aqVar);
                }
                throw th;
            }

            public a b() {
                return j().a(e());
            }

            public /* synthetic */ MessageLite build() {
                return d();
            }

            public /* synthetic */ MessageLite buildPartial() {
                return e();
            }

            public aq c() {
                return aq.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m246clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m247clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m248clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m249clone() {
                return b();
            }

            public aq d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public aq e() {
                aq aqVar = new aq((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                aqVar.d = this.b;
                if ((this.a & 2) == 2) {
                    this.c = Collections.unmodifiableList(this.c);
                    this.a &= -3;
                }
                aqVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 2;
                }
                aqVar.f = this.d;
                if ((i & 8) == 8) {
                    i2 |= 4;
                }
                aqVar.g = this.e;
                if ((i & 16) == 16) {
                    i2 |= 8;
                }
                aqVar.h = this.f;
                if ((i & 32) == 32) {
                    i2 |= 16;
                }
                aqVar.i = this.g;
                if ((i & 64) == 64) {
                    i2 |= 32;
                }
                aqVar.j = this.h;
                if ((i & 128) == 128) {
                    i2 |= 64;
                }
                aqVar.k = this.i;
                if ((i & 256) == 256) {
                    i2 |= 128;
                }
                aqVar.l = this.j;
                if ((i & 512) == 512) {
                    i2 |= 256;
                }
                aqVar.m = this.k;
                if ((i & 1024) == 1024) {
                    i2 |= 512;
                }
                aqVar.n = this.l;
                if ((i & 2048) == 2048) {
                    i2 |= 1024;
                }
                aqVar.o = this.m;
                if ((i & 4096) == 4096) {
                    i2 |= 2048;
                }
                aqVar.p = this.n;
                if ((i & 8192) == 8192) {
                    i2 |= 4096;
                }
                aqVar.q = this.o;
                if ((this.a & 16384) == 16384) {
                    this.p = Collections.unmodifiableList(this.p);
                    this.a &= -16385;
                }
                aqVar.r = this.p;
                if ((i & 32768) == 32768) {
                    i2 |= 8192;
                }
                aqVar.s = this.q;
                if ((i & 65536) == 65536) {
                    i2 |= 16384;
                }
                aqVar.t = this.r;
                if ((i & 131072) == 131072) {
                    i2 |= 32768;
                }
                aqVar.u = this.s;
                if ((i & 262144) == 262144) {
                    i2 |= 65536;
                }
                aqVar.v = this.t;
                if ((524288 & i) == 524288) {
                    i2 |= 131072;
                }
                aqVar.w = this.u;
                aqVar.c = i2;
                return aqVar;
            }

            public boolean f() {
                return (this.a & 1) == 1;
            }

            public int g() {
                return this.p.size();
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m250getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((aq) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m251mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        /* compiled from: Unknown */
        public enum b implements EnumLite {
            AMS(0, 1),
            ISL(1, 2),
            AAT(2, 3),
            ABCK(3, 4),
            ASL(4, 5),
            ABS(5, 6),
            IDM(6, 7),
            AIN(7, 8),
            ARR(8, 9),
            ADM(9, 10),
            UNKNOWN(10, 11),
            ACL(11, 12),
            SDK(12, 13),
            APM(13, 14),
            ISM(14, 15),
            IWF(15, 16),
            AWF(16, 17);
            
            private static EnumLiteMap<b> r;
            private final int s;

            static {
                r = new cv();
            }

            private b(int i, int i2) {
                this.s = i2;
            }

            public static b a(int i) {
                switch (i) {
                    case 1:
                        return AMS;
                    case 2:
                        return ISL;
                    case 3:
                        return AAT;
                    case 4:
                        return ABCK;
                    case 5:
                        return ASL;
                    case 6:
                        return ABS;
                    case 7:
                        return IDM;
                    case 8:
                        return AIN;
                    case 9:
                        return ARR;
                    case 10:
                        return ADM;
                    case 11:
                        return UNKNOWN;
                    case 12:
                        return ACL;
                    case 13:
                        return SDK;
                    case 14:
                        return APM;
                    case 15:
                        return ISM;
                    case 16:
                        return IWF;
                    case 17:
                        return AWF;
                    default:
                        return null;
                }
            }

            public final int getNumber() {
                return this.s;
            }
        }

        /* compiled from: Unknown */
        public enum c implements EnumLite {
            SDK_AAV(0, 1),
            SDK_AAT(1, 2),
            SDK_ASL(2, 3);
            
            private static EnumLiteMap<c> d;
            private final int e;

            static {
                d = new cw();
            }

            private c(int i, int i2) {
                this.e = i2;
            }

            public static c a(int i) {
                switch (i) {
                    case 1:
                        return SDK_AAV;
                    case 2:
                        return SDK_AAT;
                    case 3:
                        return SDK_ASL;
                    default:
                        return null;
                }
            }

            public final int getNumber() {
                return this.e;
            }
        }

        static {
            b.Q();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private aq(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            InvalidProtocolBufferException invalidProtocolBufferException;
            IOException iOException;
            Object obj = null;
            this.x = (byte) -1;
            this.y = -1;
            Q();
            int i = 0;
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 8:
                            b a = b.a(codedInputStream.readEnum());
                            if (a != null) {
                                this.c |= 1;
                                this.d = a;
                                break;
                            }
                            break;
                        case 18:
                            if ((i & 2) == 2) {
                                readTag = i;
                            } else {
                                this.e = new ArrayList();
                                readTag = i | 2;
                            }
                            try {
                                this.e.add(codedInputStream.readBytes());
                                break;
                            } catch (InvalidProtocolBufferException e) {
                                i = readTag;
                                invalidProtocolBufferException = e;
                                break;
                            } catch (IOException e2) {
                                i = readTag;
                                iOException = e2;
                                break;
                            } catch (Throwable th) {
                                i = readTag;
                                Throwable th2 = th;
                                break;
                            }
                        case 26:
                            a u = (this.c & 2) != 2 ? null : this.f.u();
                            this.f = (aa) codedInputStream.readMessage(aa.a, extensionRegistryLite);
                            if (u != null) {
                                u.a(this.f);
                                this.f = u.e();
                            }
                            this.c |= 2;
                            break;
                        case 34:
                            b k = (this.c & 4) != 4 ? null : this.g.k();
                            this.g = (q) codedInputStream.readMessage(q.a, extensionRegistryLite);
                            if (k != null) {
                                k.a(this.g);
                                this.g = k.e();
                            }
                            this.c |= 4;
                            break;
                        case MotionEventCompat.AXIS_GENERIC_11 /*42*/:
                            a i2 = (this.c & 8) != 8 ? null : this.h.i();
                            this.h = (ak) codedInputStream.readMessage(ak.a, extensionRegistryLite);
                            if (i2 != null) {
                                i2.a(this.h);
                                this.h = i2.e();
                            }
                            this.c |= 8;
                            break;
                        case 50:
                            b i3 = (this.c & 16) != 16 ? null : this.i.i();
                            this.i = (a) codedInputStream.readMessage(a.a, extensionRegistryLite);
                            if (i3 != null) {
                                i3.a(this.i);
                                this.i = i3.e();
                            }
                            this.c |= 16;
                            break;
                        case 58:
                            b i4 = (this.c & 32) != 32 ? null : this.j.i();
                            this.j = (g) codedInputStream.readMessage(g.a, extensionRegistryLite);
                            if (i4 != null) {
                                i4.a(this.j);
                                this.j = i4.e();
                            }
                            this.c |= 32;
                            break;
                        case Events.E_ANTISPAM_VIEW_CALL /*66*/:
                            a i5 = (this.c & 64) != 64 ? null : this.k.i();
                            this.k = (ag) codedInputStream.readMessage(ag.a, extensionRegistryLite);
                            if (i5 != null) {
                                i5.a(this.k);
                                this.k = i5.e();
                            }
                            this.c |= 64;
                            break;
                        case 74:
                            a g = (this.c & 128) != 128 ? null : this.l.g();
                            this.l = (i) codedInputStream.readMessage(i.a, extensionRegistryLite);
                            if (g != null) {
                                g.a(this.l);
                                this.l = g.e();
                            }
                            this.c |= 128;
                            break;
                        case Events.E_ANTIVIRUS_SCAN /*82*/:
                            a g2 = (this.c & 256) != 256 ? null : this.m.g();
                            this.m = (w) codedInputStream.readMessage(w.a, extensionRegistryLite);
                            if (g2 != null) {
                                g2.a(this.m);
                                this.m = g2.e();
                            }
                            this.c |= 256;
                            break;
                        case 90:
                            a g3 = (this.c & 512) != 512 ? null : this.n.g();
                            this.n = (u) codedInputStream.readMessage(u.a, extensionRegistryLite);
                            if (g3 != null) {
                                g3.a(this.n);
                                this.n = g3.e();
                            }
                            this.c |= 512;
                            break;
                        case Events.E_TRAFFIC_OVER_DAILY_MARK /*98*/:
                            a g4 = (this.c & 1024) != 1024 ? null : this.o.g();
                            this.o = (m) codedInputStream.readMessage(m.a, extensionRegistryLite);
                            if (g4 != null) {
                                g4.a(this.o);
                                this.o = g4.e();
                            }
                            this.c |= 1024;
                            break;
                        case 106:
                            a g5 = (this.c & 2048) != 2048 ? null : this.p.g();
                            this.p = (o) codedInputStream.readMessage(o.a, extensionRegistryLite);
                            if (g5 != null) {
                                g5.a(this.p);
                                this.p = g5.e();
                            }
                            this.c |= 2048;
                            break;
                        case 114:
                            a g6 = (this.c & 4096) != 4096 ? null : this.q.g();
                            this.q = (k) codedInputStream.readMessage(k.a, extensionRegistryLite);
                            if (g6 != null) {
                                g6.a(this.q);
                                this.q = g6.e();
                            }
                            this.c |= 4096;
                            break;
                        case CircleViewNew.SIZE_OF_COLOR /*120*/:
                            c a2 = c.a(codedInputStream.readEnum());
                            if (a2 != null) {
                                if ((i & 16384) == 16384) {
                                    readTag = i;
                                } else {
                                    this.r = new ArrayList();
                                    readTag = i | 16384;
                                }
                                this.r.add(a2);
                                break;
                            }
                            break;
                        case 122:
                            int pushLimit = codedInputStream.pushLimit(codedInputStream.readRawVarint32());
                            readTag = i;
                            while (codedInputStream.getBytesUntilLimit() > 0) {
                                c a3 = c.a(codedInputStream.readEnum());
                                if (a3 != null) {
                                    if ((readTag & 16384) != 16384) {
                                        this.r = new ArrayList();
                                        readTag |= 16384;
                                    }
                                    this.r.add(a3);
                                }
                            }
                            codedInputStream.popLimit(pushLimit);
                            break;
                        case 130:
                            a i6 = (this.c & 8192) != 8192 ? null : this.s.i();
                            this.s = (e) codedInputStream.readMessage(e.a, extensionRegistryLite);
                            if (i6 != null) {
                                i6.a(this.s);
                                this.s = i6.e();
                            }
                            this.c |= 8192;
                            break;
                        case 138:
                            a g7 = (this.c & 16384) != 16384 ? null : this.t.g();
                            this.t = (c) codedInputStream.readMessage(c.a, extensionRegistryLite);
                            if (g7 != null) {
                                g7.a(this.t);
                                this.t = g7.e();
                            }
                            this.c |= 16384;
                            break;
                        case 146:
                            a i7 = (this.c & 32768) != 32768 ? null : this.u.i();
                            this.u = (am) codedInputStream.readMessage(am.a, extensionRegistryLite);
                            if (i7 != null) {
                                i7.a(this.u);
                                this.u = i7.e();
                            }
                            this.c |= 32768;
                            break;
                        case 154:
                            a i8 = (this.c & 65536) != 65536 ? null : this.v.i();
                            this.v = (ao) codedInputStream.readMessage(ao.a, extensionRegistryLite);
                            if (i8 != null) {
                                i8.a(this.v);
                                this.v = i8.e();
                            }
                            this.c |= 65536;
                            break;
                        case 162:
                            a g8 = (this.c & 131072) != 131072 ? null : this.w.g();
                            this.w = (y) codedInputStream.readMessage(y.a, extensionRegistryLite);
                            if (g8 != null) {
                                g8.a(this.w);
                                this.w = g8.e();
                            }
                            this.c |= 131072;
                            break;
                        default:
                            break;
                    }
                } catch (InvalidProtocolBufferException e3) {
                    invalidProtocolBufferException = e3;
                } catch (IOException e4) {
                    iOException = e4;
                }
            }
            if ((i & 2) == 2) {
                this.e = Collections.unmodifiableList(this.e);
            }
            if ((i & 16384) == 16384) {
                this.r = Collections.unmodifiableList(this.r);
            }
            makeExtensionsImmutable();
            return;
            throw new InvalidProtocolBufferException(iOException.getMessage()).setUnfinishedMessage(this);
            try {
                throw invalidProtocolBufferException.setUnfinishedMessage(this);
            } catch (Throwable th3) {
                th2 = th3;
                if ((i & 2) == 2) {
                    this.e = Collections.unmodifiableList(this.e);
                }
                if ((i & 16384) == 16384) {
                    this.r = Collections.unmodifiableList(this.r);
                }
                makeExtensionsImmutable();
                throw th2;
            }
        }

        private aq(Builder builder) {
            super(builder);
            this.x = (byte) -1;
            this.y = -1;
        }

        private aq(boolean z) {
            this.x = (byte) -1;
            this.y = -1;
        }

        public static a N() {
            return a.j();
        }

        private void Q() {
            this.d = b.UNKNOWN;
            this.e = Collections.emptyList();
            this.f = aa.a();
            this.g = q.a();
            this.h = ak.a();
            this.i = a.a();
            this.j = g.a();
            this.k = ag.a();
            this.l = i.a();
            this.m = w.a();
            this.n = u.a();
            this.o = m.a();
            this.p = o.a();
            this.q = k.a();
            this.r = Collections.emptyList();
            this.s = e.a();
            this.t = c.a();
            this.u = am.a();
            this.v = ao.a();
            this.w = y.a();
        }

        public static a a(aq aqVar) {
            return N().a(aqVar);
        }

        public static aq a() {
            return b;
        }

        public o A() {
            return this.p;
        }

        public boolean B() {
            return (this.c & 4096) == 4096;
        }

        public k C() {
            return this.q;
        }

        public boolean D() {
            return (this.c & 8192) == 8192;
        }

        public e E() {
            return this.s;
        }

        public boolean F() {
            return (this.c & 16384) == 16384;
        }

        public c G() {
            return this.t;
        }

        public boolean H() {
            return (this.c & 32768) == 32768;
        }

        public am I() {
            return this.u;
        }

        public boolean J() {
            return (this.c & 65536) == 65536;
        }

        public ao K() {
            return this.v;
        }

        public boolean L() {
            return (this.c & 131072) == 131072;
        }

        public y M() {
            return this.w;
        }

        public a O() {
            return N();
        }

        public a P() {
            return a(this);
        }

        public aq b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public b d() {
            return this.d;
        }

        public List<ByteString> e() {
            return this.e;
        }

        public boolean f() {
            return (this.c & 2) == 2;
        }

        public aa g() {
            return this.f;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<aq> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.y;
            if (i2 != -1) {
                return i2;
            }
            int i3;
            int computeEnumSize = (this.c & 1) != 1 ? 0 : CodedOutputStream.computeEnumSize(1, this.d.getNumber()) + 0;
            int i4 = 0;
            for (i3 = 0; i3 < this.e.size(); i3++) {
                i4 += CodedOutputStream.computeBytesSizeNoTag((ByteString) this.e.get(i3));
            }
            i2 = (computeEnumSize + i4) + (e().size() * 1);
            if ((this.c & 2) == 2) {
                i2 += CodedOutputStream.computeMessageSize(3, this.f);
            }
            if ((this.c & 4) == 4) {
                i2 += CodedOutputStream.computeMessageSize(4, this.g);
            }
            if ((this.c & 8) == 8) {
                i2 += CodedOutputStream.computeMessageSize(5, this.h);
            }
            if ((this.c & 16) == 16) {
                i2 += CodedOutputStream.computeMessageSize(6, this.i);
            }
            if ((this.c & 32) == 32) {
                i2 += CodedOutputStream.computeMessageSize(7, this.j);
            }
            if ((this.c & 64) == 64) {
                i2 += CodedOutputStream.computeMessageSize(8, this.k);
            }
            if ((this.c & 128) == 128) {
                i2 += CodedOutputStream.computeMessageSize(9, this.l);
            }
            if ((this.c & 256) == 256) {
                i2 += CodedOutputStream.computeMessageSize(10, this.m);
            }
            if ((this.c & 512) == 512) {
                i2 += CodedOutputStream.computeMessageSize(11, this.n);
            }
            if ((this.c & 1024) == 1024) {
                i2 += CodedOutputStream.computeMessageSize(12, this.o);
            }
            if ((this.c & 2048) == 2048) {
                i2 += CodedOutputStream.computeMessageSize(13, this.p);
            }
            computeEnumSize = (this.c & 4096) != 4096 ? i2 : i2 + CodedOutputStream.computeMessageSize(14, this.q);
            i3 = 0;
            while (i < this.r.size()) {
                i++;
                i3 = CodedOutputStream.computeEnumSizeNoTag(((c) this.r.get(i)).getNumber()) + i3;
            }
            i2 = (computeEnumSize + i3) + (this.r.size() * 1);
            if ((this.c & 8192) == 8192) {
                i2 += CodedOutputStream.computeMessageSize(16, this.s);
            }
            if ((this.c & 16384) == 16384) {
                i2 += CodedOutputStream.computeMessageSize(17, this.t);
            }
            if ((this.c & 32768) == 32768) {
                i2 += CodedOutputStream.computeMessageSize(18, this.u);
            }
            if ((this.c & 65536) == 65536) {
                i2 += CodedOutputStream.computeMessageSize(19, this.v);
            }
            if ((this.c & 131072) == 131072) {
                i2 += CodedOutputStream.computeMessageSize(20, this.w);
            }
            this.y = i2;
            return i2;
        }

        public boolean h() {
            return (this.c & 4) == 4;
        }

        public q i() {
            return this.g;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.x;
            if (b == (byte) -1) {
                this.x = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public boolean j() {
            return (this.c & 8) == 8;
        }

        public ak k() {
            return this.h;
        }

        public boolean l() {
            return (this.c & 16) == 16;
        }

        public a m() {
            return this.i;
        }

        public boolean n() {
            return (this.c & 32) == 32;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return O();
        }

        public g o() {
            return this.j;
        }

        public boolean p() {
            return (this.c & 64) == 64;
        }

        public ag q() {
            return this.k;
        }

        public boolean r() {
            return (this.c & 128) == 128;
        }

        public i s() {
            return this.l;
        }

        public boolean t() {
            return (this.c & 256) == 256;
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return P();
        }

        public w u() {
            return this.m;
        }

        public boolean v() {
            return (this.c & 512) == 512;
        }

        public u w() {
            return this.n;
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            int i = 0;
            getSerializedSize();
            if ((this.c & 1) == 1) {
                codedOutputStream.writeEnum(1, this.d.getNumber());
            }
            for (int i2 = 0; i2 < this.e.size(); i2++) {
                codedOutputStream.writeBytes(2, (ByteString) this.e.get(i2));
            }
            if ((this.c & 2) == 2) {
                codedOutputStream.writeMessage(3, this.f);
            }
            if ((this.c & 4) == 4) {
                codedOutputStream.writeMessage(4, this.g);
            }
            if ((this.c & 8) == 8) {
                codedOutputStream.writeMessage(5, this.h);
            }
            if ((this.c & 16) == 16) {
                codedOutputStream.writeMessage(6, this.i);
            }
            if ((this.c & 32) == 32) {
                codedOutputStream.writeMessage(7, this.j);
            }
            if ((this.c & 64) == 64) {
                codedOutputStream.writeMessage(8, this.k);
            }
            if ((this.c & 128) == 128) {
                codedOutputStream.writeMessage(9, this.l);
            }
            if ((this.c & 256) == 256) {
                codedOutputStream.writeMessage(10, this.m);
            }
            if ((this.c & 512) == 512) {
                codedOutputStream.writeMessage(11, this.n);
            }
            if ((this.c & 1024) == 1024) {
                codedOutputStream.writeMessage(12, this.o);
            }
            if ((this.c & 2048) == 2048) {
                codedOutputStream.writeMessage(13, this.p);
            }
            if ((this.c & 4096) == 4096) {
                codedOutputStream.writeMessage(14, this.q);
            }
            while (i < this.r.size()) {
                codedOutputStream.writeEnum(15, ((c) this.r.get(i)).getNumber());
                i++;
            }
            if ((this.c & 8192) == 8192) {
                codedOutputStream.writeMessage(16, this.s);
            }
            if ((this.c & 16384) == 16384) {
                codedOutputStream.writeMessage(17, this.t);
            }
            if ((this.c & 32768) == 32768) {
                codedOutputStream.writeMessage(18, this.u);
            }
            if ((this.c & 65536) == 65536) {
                codedOutputStream.writeMessage(19, this.v);
            }
            if ((this.c & 131072) == 131072) {
                codedOutputStream.writeMessage(20, this.w);
            }
        }

        public boolean x() {
            return (this.c & 1024) == 1024;
        }

        public m y() {
            return this.o;
        }

        public boolean z() {
            return (this.c & 2048) == 2048;
        }
    }

    /* compiled from: Unknown */
    public interface d extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class c extends GeneratedMessageLite implements d {
        public static Parser<c> a = new by();
        private static final c b = new c(true);
        private int c;
        private ae d;
        private byte e;
        private int f;

        /* compiled from: Unknown */
        public static final class a extends Builder<c, a> implements d {
            private int a;
            private ae b = ae.a();

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
                this.b = ae.a();
                this.a &= -2;
                return this;
            }

            public a a(a aVar) {
                this.b = aVar.d();
                this.a |= 1;
                return this;
            }

            public a a(ae aeVar) {
                if ((this.a & 1) == 1 && this.b != ae.a()) {
                    this.b = ae.a(this.b).a(aeVar).e();
                } else {
                    this.b = aeVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(c cVar) {
                if (cVar != c.a() && cVar.c()) {
                    a(cVar.d());
                }
                return this;
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                Throwable th;
                c cVar;
                c cVar2;
                try {
                    cVar2 = (c) c.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (cVar2 != null) {
                        a(cVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    cVar2 = (c) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    cVar = cVar2;
                    th = th3;
                }
                if (cVar != null) {
                    a(cVar);
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

            public c c() {
                return c.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m252clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m253clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m254clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m255clone() {
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
                int i = 0;
                if ((this.a & 1) == 1) {
                    i = 1;
                }
                cVar.d = this.b;
                cVar.c = i;
                return cVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m256getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m257mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.h();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private c(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.e = (byte) -1;
            this.f = -1;
            h();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a k = (this.c & 1) != 1 ? null : this.d.k();
                            this.d = (ae) codedInputStream.readMessage(ae.a, extensionRegistryLite);
                            if (k != null) {
                                k.a(this.d);
                                this.d = k.e();
                            }
                            this.c |= 1;
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
            this.e = (byte) -1;
            this.f = -1;
        }

        private c(boolean z) {
            this.e = (byte) -1;
            this.f = -1;
        }

        public static a a(c cVar) {
            return e().a(cVar);
        }

        public static c a() {
            return b;
        }

        public static a e() {
            return a.h();
        }

        private void h() {
            this.d = ae.a();
        }

        public c b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ae d() {
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

        public Parser<c> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.f;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
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
                codedOutputStream.writeMessage(1, this.d);
            }
        }
    }

    /* compiled from: Unknown */
    public interface f extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class e extends GeneratedMessageLite implements f {
        public static Parser<e> a = new bz();
        private static final e b = new e(true);
        private int c;
        private ae d;
        private ByteString e;
        private byte f;
        private int g;

        /* compiled from: Unknown */
        public static final class a extends Builder<e, a> implements f {
            private int a;
            private ae b = ae.a();
            private ByteString c = ByteString.EMPTY;

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
                this.b = ae.a();
                this.a &= -2;
                this.c = ByteString.EMPTY;
                this.a &= -3;
                return this;
            }

            public a a(a aVar) {
                this.b = aVar.d();
                this.a |= 1;
                return this;
            }

            public a a(ae aeVar) {
                if ((this.a & 1) == 1 && this.b != ae.a()) {
                    this.b = ae.a(this.b).a(aeVar).e();
                } else {
                    this.b = aeVar;
                }
                this.a |= 1;
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
            public /* synthetic */ MessageLite.Builder m258clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m259clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m260clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m261clone() {
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
                eVar.c = i2;
                return eVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m262getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m263mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.j();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private e(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.f = (byte) -1;
            this.g = -1;
            j();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a k = (this.c & 1) != 1 ? null : this.d.k();
                            this.d = (ae) codedInputStream.readMessage(ae.a, extensionRegistryLite);
                            if (k != null) {
                                k.a(this.d);
                                this.d = k.e();
                            }
                            this.c |= 1;
                            break;
                        case 18:
                            this.c |= 2;
                            this.e = codedInputStream.readBytes();
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
            this.f = (byte) -1;
            this.g = -1;
        }

        private e(boolean z) {
            this.f = (byte) -1;
            this.g = -1;
        }

        public static a a(e eVar) {
            return g().a(eVar);
        }

        public static e a() {
            return b;
        }

        public static a g() {
            return a.h();
        }

        private void j() {
            this.d = ae.a();
            this.e = ByteString.EMPTY;
        }

        public e b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ae d() {
            return this.d;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public ByteString f() {
            return this.e;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<e> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.g;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeBytesSize(2, this.e);
            }
            this.g = i;
            return i;
        }

        public a h() {
            return g();
        }

        public a i() {
            return a(this);
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.f;
            if (b == (byte) -1) {
                this.f = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return h();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return i();
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
        }
    }

    /* compiled from: Unknown */
    public interface h extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class g extends GeneratedMessageLite implements h {
        public static Parser<g> a = new ca();
        private static final g b = new g(true);
        private int c;
        private ac d;
        private a e;
        private byte f;
        private int g;

        /* compiled from: Unknown */
        public enum a implements EnumLite {
            FREE(0, 1),
            PREMIUM(1, 2);
            
            private static EnumLiteMap<a> c;
            private final int d;

            static {
                c = new cb();
            }

            private a(int i, int i2) {
                this.d = i2;
            }

            public static a a(int i) {
                switch (i) {
                    case 1:
                        return FREE;
                    case 2:
                        return PREMIUM;
                    default:
                        return null;
                }
            }

            public final int getNumber() {
                return this.d;
            }
        }

        /* compiled from: Unknown */
        public static final class b extends Builder<g, b> implements h {
            private int a;
            private ac b = ac.a();
            private a c = a.FREE;

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
                this.b = ac.a();
                this.a &= -2;
                this.c = a.FREE;
                this.a &= -3;
                return this;
            }

            public b a(a aVar) {
                this.b = aVar.d();
                this.a |= 1;
                return this;
            }

            public b a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public b a(a aVar) {
                if (aVar != null) {
                    this.a |= 2;
                    this.c = aVar;
                    return this;
                }
                throw new NullPointerException();
            }

            public b a(g gVar) {
                if (gVar == g.a()) {
                    return this;
                }
                if (gVar.c()) {
                    a(gVar.d());
                }
                if (gVar.e()) {
                    a(gVar.f());
                }
                return this;
            }

            public b a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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

            public b b() {
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
            public /* synthetic */ MessageLite.Builder m264clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m265clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m266clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m267clone() {
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
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                gVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                gVar.e = this.c;
                gVar.c = i2;
                return gVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m268getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m269mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.j();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private g(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.f = (byte) -1;
            this.g = -1;
            j();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
                            break;
                        case 16:
                            a a = a.a(codedInputStream.readEnum());
                            if (a != null) {
                                this.c |= 2;
                                this.e = a;
                                break;
                            }
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

        private g(Builder builder) {
            super(builder);
            this.f = (byte) -1;
            this.g = -1;
        }

        private g(boolean z) {
            this.f = (byte) -1;
            this.g = -1;
        }

        public static b a(g gVar) {
            return g().a(gVar);
        }

        public static g a() {
            return b;
        }

        public static b g() {
            return b.h();
        }

        private void j() {
            this.d = ac.a();
            this.e = a.FREE;
        }

        public g b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
            return this.d;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public a f() {
            return this.e;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<g> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.g;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeEnumSize(2, this.e.getNumber());
            }
            this.g = i;
            return i;
        }

        public b h() {
            return g();
        }

        public b i() {
            return a(this);
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.f;
            if (b == (byte) -1) {
                this.f = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return h();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return i();
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
                codedOutputStream.writeEnum(2, this.e.getNumber());
            }
        }
    }

    /* compiled from: Unknown */
    public interface j extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class i extends GeneratedMessageLite implements j {
        public static Parser<i> a = new cc();
        private static final i b = new i(true);
        private int c;
        private ac d;
        private byte e;
        private int f;

        /* compiled from: Unknown */
        public static final class a extends Builder<i, a> implements j {
            private int a;
            private ac b = ac.a();

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
                this.b = ac.a();
                this.a &= -2;
                return this;
            }

            public a a(a aVar) {
                this.b = aVar.d();
                this.a |= 1;
                return this;
            }

            public a a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(i iVar) {
                if (iVar != i.a() && iVar.c()) {
                    a(iVar.d());
                }
                return this;
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
            public /* synthetic */ MessageLite.Builder m270clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m271clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m272clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m273clone() {
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
                int i = 0;
                if ((this.a & 1) == 1) {
                    i = 1;
                }
                iVar.d = this.b;
                iVar.c = i;
                return iVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m274getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m275mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.h();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private i(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.e = (byte) -1;
            this.f = -1;
            h();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
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

        private i(Builder builder) {
            super(builder);
            this.e = (byte) -1;
            this.f = -1;
        }

        private i(boolean z) {
            this.e = (byte) -1;
            this.f = -1;
        }

        public static a a(i iVar) {
            return e().a(iVar);
        }

        public static i a() {
            return b;
        }

        public static a e() {
            return a.h();
        }

        private void h() {
            this.d = ac.a();
        }

        public i b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
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

        public Parser<i> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.f;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
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
                codedOutputStream.writeMessage(1, this.d);
            }
        }
    }

    /* compiled from: Unknown */
    public interface l extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class k extends GeneratedMessageLite implements l {
        public static Parser<k> a = new cd();
        private static final k b = new k(true);
        private int c;
        private ac d;
        private byte e;
        private int f;

        /* compiled from: Unknown */
        public static final class a extends Builder<k, a> implements l {
            private int a;
            private ac b = ac.a();

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
                this.b = ac.a();
                this.a &= -2;
                return this;
            }

            public a a(a aVar) {
                this.b = aVar.d();
                this.a |= 1;
                return this;
            }

            public a a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(k kVar) {
                if (kVar != k.a() && kVar.c()) {
                    a(kVar.d());
                }
                return this;
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
                return h().a(e());
            }

            public /* synthetic */ MessageLite build() {
                return d();
            }

            public /* synthetic */ MessageLite buildPartial() {
                return e();
            }

            public k c() {
                return k.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m276clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m277clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m278clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m279clone() {
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
                int i = 0;
                if ((this.a & 1) == 1) {
                    i = 1;
                }
                kVar.d = this.b;
                kVar.c = i;
                return kVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m280getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m281mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.h();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private k(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.e = (byte) -1;
            this.f = -1;
            h();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
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
            this.e = (byte) -1;
            this.f = -1;
        }

        private k(boolean z) {
            this.e = (byte) -1;
            this.f = -1;
        }

        public static a a(k kVar) {
            return e().a(kVar);
        }

        public static k a() {
            return b;
        }

        public static a e() {
            return a.h();
        }

        private void h() {
            this.d = ac.a();
        }

        public k b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
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

        public Parser<k> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.f;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
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
                codedOutputStream.writeMessage(1, this.d);
            }
        }
    }

    /* compiled from: Unknown */
    public interface n extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class m extends GeneratedMessageLite implements n {
        public static Parser<m> a = new ce();
        private static final m b = new m(true);
        private int c;
        private ac d;
        private byte e;
        private int f;

        /* compiled from: Unknown */
        public static final class a extends Builder<m, a> implements n {
            private int a;
            private ac b = ac.a();

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
                this.b = ac.a();
                this.a &= -2;
                return this;
            }

            public a a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(m mVar) {
                if (mVar != m.a() && mVar.c()) {
                    a(mVar.d());
                }
                return this;
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
            public /* synthetic */ MessageLite.Builder m282clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m283clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m284clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m285clone() {
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
                int i = 0;
                if ((this.a & 1) == 1) {
                    i = 1;
                }
                mVar.d = this.b;
                mVar.c = i;
                return mVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m286getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m287mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.h();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private m(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.e = (byte) -1;
            this.f = -1;
            h();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
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
            this.e = (byte) -1;
            this.f = -1;
        }

        private m(boolean z) {
            this.e = (byte) -1;
            this.f = -1;
        }

        public static a a(m mVar) {
            return e().a(mVar);
        }

        public static m a() {
            return b;
        }

        public static a e() {
            return a.h();
        }

        private void h() {
            this.d = ac.a();
        }

        public m b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
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

        public Parser<m> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.f;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
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
                codedOutputStream.writeMessage(1, this.d);
            }
        }
    }

    /* compiled from: Unknown */
    public interface p extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class o extends GeneratedMessageLite implements p {
        public static Parser<o> a = new cf();
        private static final o b = new o(true);
        private int c;
        private ac d;
        private byte e;
        private int f;

        /* compiled from: Unknown */
        public static final class a extends Builder<o, a> implements p {
            private int a;
            private ac b = ac.a();

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
                this.b = ac.a();
                this.a &= -2;
                return this;
            }

            public a a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(o oVar) {
                if (oVar != o.a() && oVar.c()) {
                    a(oVar.d());
                }
                return this;
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                Throwable th;
                o oVar;
                o oVar2;
                try {
                    oVar2 = (o) o.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (oVar2 != null) {
                        a(oVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    oVar2 = (o) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    oVar = oVar2;
                    th = th3;
                }
                if (oVar != null) {
                    a(oVar);
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

            public o c() {
                return o.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m288clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m289clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m290clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m291clone() {
                return b();
            }

            public o d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public o e() {
                o oVar = new o((Builder) this);
                int i = 0;
                if ((this.a & 1) == 1) {
                    i = 1;
                }
                oVar.d = this.b;
                oVar.c = i;
                return oVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m292getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((o) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m293mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.h();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private o(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.e = (byte) -1;
            this.f = -1;
            h();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
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

        private o(Builder builder) {
            super(builder);
            this.e = (byte) -1;
            this.f = -1;
        }

        private o(boolean z) {
            this.e = (byte) -1;
            this.f = -1;
        }

        public static a a(o oVar) {
            return e().a(oVar);
        }

        public static o a() {
            return b;
        }

        public static a e() {
            return a.h();
        }

        private void h() {
            this.d = ac.a();
        }

        public o b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
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

        public Parser<o> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.f;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
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
                codedOutputStream.writeMessage(1, this.d);
            }
        }
    }

    /* compiled from: Unknown */
    public interface r extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class q extends GeneratedMessageLite implements r {
        public static Parser<q> a = new cg();
        private static final q b = new q(true);
        private int c;
        private ac d;
        private ByteString e;
        private a f;
        private byte g;
        private int h;

        /* compiled from: Unknown */
        public enum a implements EnumLite {
            FREE(0, 1),
            PREMIUM(1, 2);
            
            private static EnumLiteMap<a> c;
            private final int d;

            static {
                c = new ch();
            }

            private a(int i, int i2) {
                this.d = i2;
            }

            public static a a(int i) {
                switch (i) {
                    case 1:
                        return FREE;
                    case 2:
                        return PREMIUM;
                    default:
                        return null;
                }
            }

            public final int getNumber() {
                return this.d;
            }
        }

        /* compiled from: Unknown */
        public static final class b extends Builder<q, b> implements r {
            private int a;
            private ac b = ac.a();
            private ByteString c = ByteString.EMPTY;
            private a d = a.FREE;

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
                this.b = ac.a();
                this.a &= -2;
                this.c = ByteString.EMPTY;
                this.a &= -3;
                this.d = a.FREE;
                this.a &= -5;
                return this;
            }

            public b a(a aVar) {
                this.b = aVar.d();
                this.a |= 1;
                return this;
            }

            public b a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public b a(a aVar) {
                if (aVar != null) {
                    this.a |= 4;
                    this.d = aVar;
                    return this;
                }
                throw new NullPointerException();
            }

            public b a(q qVar) {
                if (qVar == q.a()) {
                    return this;
                }
                if (qVar.c()) {
                    a(qVar.d());
                }
                if (qVar.e()) {
                    a(qVar.f());
                }
                if (qVar.g()) {
                    a(qVar.h());
                }
                return this;
            }

            public b a(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 2;
                    this.c = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public b a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                q qVar;
                Throwable th;
                q qVar2;
                try {
                    qVar = (q) q.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (qVar != null) {
                        a(qVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    qVar = (q) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    qVar2 = qVar;
                    th = th3;
                }
                if (qVar2 != null) {
                    a(qVar2);
                }
                throw th;
            }

            public b b() {
                return h().a(e());
            }

            public /* synthetic */ MessageLite build() {
                return d();
            }

            public /* synthetic */ MessageLite buildPartial() {
                return e();
            }

            public q c() {
                return q.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m294clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m295clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m296clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m297clone() {
                return b();
            }

            public q d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public q e() {
                q qVar = new q((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                qVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                qVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                qVar.f = this.d;
                qVar.c = i2;
                return qVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m298getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((q) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m299mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.l();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private q(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
                            break;
                        case 18:
                            this.c |= 2;
                            this.e = codedInputStream.readBytes();
                            break;
                        case 24:
                            a a = a.a(codedInputStream.readEnum());
                            if (a != null) {
                                this.c |= 4;
                                this.f = a;
                                break;
                            }
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

        private q(Builder builder) {
            super(builder);
            this.g = (byte) -1;
            this.h = -1;
        }

        private q(boolean z) {
            this.g = (byte) -1;
            this.h = -1;
        }

        public static b a(q qVar) {
            return i().a(qVar);
        }

        public static q a() {
            return b;
        }

        public static b i() {
            return b.h();
        }

        private void l() {
            this.d = ac.a();
            this.e = ByteString.EMPTY;
            this.f = a.FREE;
        }

        public q b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
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

        public Parser<q> getParserForType() {
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
                i += CodedOutputStream.computeBytesSize(2, this.e);
            }
            if ((this.c & 4) == 4) {
                i += CodedOutputStream.computeEnumSize(3, this.f.getNumber());
            }
            this.h = i;
            return i;
        }

        public a h() {
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

        public b j() {
            return i();
        }

        public b k() {
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
                codedOutputStream.writeBytes(2, this.e);
            }
            if ((this.c & 4) == 4) {
                codedOutputStream.writeEnum(3, this.f.getNumber());
            }
        }
    }

    /* compiled from: Unknown */
    public interface t extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class s extends GeneratedMessageLite implements t {
        public static Parser<s> a = new ci();
        private static final s b = new s(true);
        private int c;
        private ByteString d;
        private ByteString e;
        private ByteString f;
        private ByteString g;
        private ByteString h;
        private int i;
        private byte j;
        private int k;

        /* compiled from: Unknown */
        public static final class a extends Builder<s, a> implements t {
            private int a;
            private ByteString b = ByteString.EMPTY;
            private ByteString c = ByteString.EMPTY;
            private ByteString d = ByteString.EMPTY;
            private ByteString e = ByteString.EMPTY;
            private ByteString f = ByteString.EMPTY;
            private int g;

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
                this.c = ByteString.EMPTY;
                this.a &= -3;
                this.d = ByteString.EMPTY;
                this.a &= -5;
                this.e = ByteString.EMPTY;
                this.a &= -9;
                this.f = ByteString.EMPTY;
                this.a &= -17;
                this.g = 0;
                this.a &= -33;
                return this;
            }

            public a a(int i) {
                this.a |= 32;
                this.g = i;
                return this;
            }

            public a a(s sVar) {
                if (sVar == s.a()) {
                    return this;
                }
                if (sVar.c()) {
                    a(sVar.d());
                }
                if (sVar.e()) {
                    b(sVar.f());
                }
                if (sVar.g()) {
                    c(sVar.h());
                }
                if (sVar.i()) {
                    d(sVar.j());
                }
                if (sVar.k()) {
                    e(sVar.l());
                }
                if (sVar.m()) {
                    a(sVar.n());
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
                Throwable th;
                s sVar;
                s sVar2;
                try {
                    sVar2 = (s) s.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (sVar2 != null) {
                        a(sVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    sVar2 = (s) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    sVar = sVar2;
                    th = th3;
                }
                if (sVar != null) {
                    a(sVar);
                }
                throw th;
            }

            public a b() {
                return h().a(e());
            }

            public a b(ByteString byteString) {
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

            public a c(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 4;
                    this.d = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public s c() {
                return s.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m300clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m301clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m302clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m303clone() {
                return b();
            }

            public a d(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 8;
                    this.e = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public s d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public a e(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 16;
                    this.f = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public s e() {
                s sVar = new s((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                sVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                sVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                sVar.f = this.d;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                sVar.g = this.e;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                sVar.h = this.f;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                sVar.i = this.g;
                sVar.c = i2;
                return sVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m304getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((s) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m305mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.r();
        }

        private s(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.j = (byte) -1;
            this.k = -1;
            r();
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
                        case MotionEventCompat.AXIS_GENERIC_11 /*42*/:
                            this.c |= 16;
                            this.h = codedInputStream.readBytes();
                            break;
                        case HwRcsCommonObject.BLACKLIST_MSG_VIDEO_TYPE /*48*/:
                            this.c |= 32;
                            this.i = codedInputStream.readInt32();
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

        private s(Builder builder) {
            super(builder);
            this.j = (byte) -1;
            this.k = -1;
        }

        private s(boolean z) {
            this.j = (byte) -1;
            this.k = -1;
        }

        public static a a(s sVar) {
            return o().a(sVar);
        }

        public static s a() {
            return b;
        }

        public static a o() {
            return a.h();
        }

        private void r() {
            this.d = ByteString.EMPTY;
            this.e = ByteString.EMPTY;
            this.f = ByteString.EMPTY;
            this.g = ByteString.EMPTY;
            this.h = ByteString.EMPTY;
            this.i = 0;
        }

        public s b() {
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

        public Parser<s> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.k;
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
                i += CodedOutputStream.computeBytesSize(5, this.h);
            }
            if ((this.c & 32) == 32) {
                i += CodedOutputStream.computeInt32Size(6, this.i);
            }
            this.k = i;
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
            byte b = this.j;
            if (b == (byte) -1) {
                this.j = (byte) 1;
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

        public ByteString l() {
            return this.h;
        }

        public boolean m() {
            return (this.c & 32) == 32;
        }

        public int n() {
            return this.i;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return p();
        }

        public a p() {
            return o();
        }

        public a q() {
            return a(this);
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return q();
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
                codedOutputStream.writeBytes(5, this.h);
            }
            if ((this.c & 32) == 32) {
                codedOutputStream.writeInt32(6, this.i);
            }
        }
    }

    /* compiled from: Unknown */
    public interface v extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class u extends GeneratedMessageLite implements v {
        public static Parser<u> a = new cj();
        private static final u b = new u(true);
        private int c;
        private ac d;
        private byte e;
        private int f;

        /* compiled from: Unknown */
        public static final class a extends Builder<u, a> implements v {
            private int a;
            private ac b = ac.a();

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
                this.b = ac.a();
                this.a &= -2;
                return this;
            }

            public a a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(u uVar) {
                if (uVar != u.a() && uVar.c()) {
                    a(uVar.d());
                }
                return this;
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                Throwable th;
                u uVar;
                u uVar2;
                try {
                    uVar2 = (u) u.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (uVar2 != null) {
                        a(uVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    uVar2 = (u) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    uVar = uVar2;
                    th = th3;
                }
                if (uVar != null) {
                    a(uVar);
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

            public u c() {
                return u.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m306clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m307clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m308clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m309clone() {
                return b();
            }

            public u d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public u e() {
                u uVar = new u((Builder) this);
                int i = 0;
                if ((this.a & 1) == 1) {
                    i = 1;
                }
                uVar.d = this.b;
                uVar.c = i;
                return uVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m310getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((u) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m311mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.h();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private u(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.e = (byte) -1;
            this.f = -1;
            h();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
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

        private u(Builder builder) {
            super(builder);
            this.e = (byte) -1;
            this.f = -1;
        }

        private u(boolean z) {
            this.e = (byte) -1;
            this.f = -1;
        }

        public static a a(u uVar) {
            return e().a(uVar);
        }

        public static u a() {
            return b;
        }

        public static a e() {
            return a.h();
        }

        private void h() {
            this.d = ac.a();
        }

        public u b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
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

        public Parser<u> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.f;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
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
                codedOutputStream.writeMessage(1, this.d);
            }
        }
    }

    /* compiled from: Unknown */
    public interface x extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class w extends GeneratedMessageLite implements x {
        public static Parser<w> a = new ck();
        private static final w b = new w(true);
        private int c;
        private ac d;
        private byte e;
        private int f;

        /* compiled from: Unknown */
        public static final class a extends Builder<w, a> implements x {
            private int a;
            private ac b = ac.a();

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
                this.b = ac.a();
                this.a &= -2;
                return this;
            }

            public a a(a aVar) {
                this.b = aVar.d();
                this.a |= 1;
                return this;
            }

            public a a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(w wVar) {
                if (wVar != w.a() && wVar.c()) {
                    a(wVar.d());
                }
                return this;
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                Throwable th;
                w wVar;
                w wVar2;
                try {
                    wVar2 = (w) w.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (wVar2 != null) {
                        a(wVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    wVar2 = (w) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    wVar = wVar2;
                    th = th3;
                }
                if (wVar != null) {
                    a(wVar);
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

            public w c() {
                return w.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m312clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m313clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m314clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m315clone() {
                return b();
            }

            public w d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public w e() {
                w wVar = new w((Builder) this);
                int i = 0;
                if ((this.a & 1) == 1) {
                    i = 1;
                }
                wVar.d = this.b;
                wVar.c = i;
                return wVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m316getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((w) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m317mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.h();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private w(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.e = (byte) -1;
            this.f = -1;
            h();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
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

        private w(Builder builder) {
            super(builder);
            this.e = (byte) -1;
            this.f = -1;
        }

        private w(boolean z) {
            this.e = (byte) -1;
            this.f = -1;
        }

        public static a a(w wVar) {
            return e().a(wVar);
        }

        public static w a() {
            return b;
        }

        public static a e() {
            return a.h();
        }

        private void h() {
            this.d = ac.a();
        }

        public w b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
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

        public Parser<w> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.f;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
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
                codedOutputStream.writeMessage(1, this.d);
            }
        }
    }

    /* compiled from: Unknown */
    public interface z extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class y extends GeneratedMessageLite implements z {
        public static Parser<y> a = new cl();
        private static final y b = new y(true);
        private int c;
        private ac d;
        private byte e;
        private int f;

        /* compiled from: Unknown */
        public static final class a extends Builder<y, a> implements z {
            private int a;
            private ac b = ac.a();

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
                this.b = ac.a();
                this.a &= -2;
                return this;
            }

            public a a(ac acVar) {
                if ((this.a & 1) == 1 && this.b != ac.a()) {
                    this.b = ac.a(this.b).a(acVar).e();
                } else {
                    this.b = acVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(y yVar) {
                if (yVar != y.a() && yVar.c()) {
                    a(yVar.d());
                }
                return this;
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                Throwable th;
                y yVar;
                y yVar2;
                try {
                    yVar2 = (y) y.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (yVar2 != null) {
                        a(yVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    yVar2 = (y) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    yVar = yVar2;
                    th = th3;
                }
                if (yVar != null) {
                    a(yVar);
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

            public y c() {
                return y.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m318clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m319clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m320clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m321clone() {
                return b();
            }

            public y d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public y e() {
                y yVar = new y((Builder) this);
                int i = 0;
                if ((this.a & 1) == 1) {
                    i = 1;
                }
                yVar.d = this.b;
                yVar.c = i;
                return yVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m322getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((y) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m323mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.h();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private y(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.e = (byte) -1;
            this.f = -1;
            h();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 10:
                            a w = (this.c & 1) != 1 ? null : this.d.w();
                            this.d = (ac) codedInputStream.readMessage(ac.a, extensionRegistryLite);
                            if (w != null) {
                                w.a(this.d);
                                this.d = w.e();
                            }
                            this.c |= 1;
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

        private y(Builder builder) {
            super(builder);
            this.e = (byte) -1;
            this.f = -1;
        }

        private y(boolean z) {
            this.e = (byte) -1;
            this.f = -1;
        }

        public static a a(y yVar) {
            return e().a(yVar);
        }

        public static y a() {
            return b;
        }

        public static a e() {
            return a.h();
        }

        private void h() {
            this.d = ac.a();
        }

        public y b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ac d() {
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

        public Parser<y> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.f;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(1, this.d) + 0;
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
                codedOutputStream.writeMessage(1, this.d);
            }
        }
    }
}
