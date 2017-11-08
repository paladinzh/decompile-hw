package com.avast.android.sdk.engine.obfuscated;

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
import java.util.List;

/* compiled from: Unknown */
public final class z {

    /* compiled from: Unknown */
    public enum a implements EnumLite {
        WIN(0, 1),
        MAC(1, 2),
        ANDROID(2, 3);
        
        private static EnumLiteMap<a> d;
        private final int e;

        static {
            d = new ab();
        }

        private a(int i, int i2) {
            this.e = i2;
        }

        public static a a(int i) {
            switch (i) {
                case 1:
                    return WIN;
                case 2:
                    return MAC;
                case 3:
                    return ANDROID;
                default:
                    return null;
            }
        }

        public final int getNumber() {
            return this.e;
        }
    }

    /* compiled from: Unknown */
    public interface c extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class b extends GeneratedMessageLite implements c {
        public static Parser<b> a = new ac();
        private static final b b = new b(true);
        private int c;
        private Object d;
        private Object e;
        private Object f;
        private Object g;
        private Object h;
        private ByteString i;
        private Object j;
        private Object k;
        private LazyStringList l;
        private a m;
        private byte n;
        private int o;

        /* compiled from: Unknown */
        public static final class a extends Builder<b, a> implements c {
            private int a;
            private Object b = "";
            private Object c = "";
            private Object d = "";
            private Object e = "";
            private Object f = "";
            private ByteString g = ByteString.EMPTY;
            private Object h = "";
            private Object i = "";
            private LazyStringList j = LazyStringArrayList.EMPTY;
            private a k = a.WIN;

            private a() {
                g();
            }

            private void g() {
            }

            private static a h() {
                return new a();
            }

            private void i() {
                if ((this.a & 256) != 256) {
                    this.j = new LazyStringArrayList(this.j);
                    this.a |= 256;
                }
            }

            public a a() {
                super.clear();
                this.b = "";
                this.a &= -2;
                this.c = "";
                this.a &= -3;
                this.d = "";
                this.a &= -5;
                this.e = "";
                this.a &= -9;
                this.f = "";
                this.a &= -17;
                this.g = ByteString.EMPTY;
                this.a &= -33;
                this.h = "";
                this.a &= -65;
                this.i = "";
                this.a &= -129;
                this.j = LazyStringArrayList.EMPTY;
                this.a &= -257;
                this.k = a.WIN;
                this.a &= -513;
                return this;
            }

            public a a(a aVar) {
                if (aVar != null) {
                    this.a |= 512;
                    this.k = aVar;
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(b bVar) {
                if (bVar == b.a()) {
                    return this;
                }
                if (bVar.c()) {
                    this.a |= 1;
                    this.b = bVar.d;
                }
                if (bVar.e()) {
                    this.a |= 2;
                    this.c = bVar.e;
                }
                if (bVar.g()) {
                    this.a |= 4;
                    this.d = bVar.f;
                }
                if (bVar.i()) {
                    this.a |= 8;
                    this.e = bVar.g;
                }
                if (bVar.k()) {
                    this.a |= 16;
                    this.f = bVar.h;
                }
                if (bVar.m()) {
                    a(bVar.n());
                }
                if (bVar.o()) {
                    this.a |= 64;
                    this.h = bVar.j;
                }
                if (bVar.q()) {
                    this.a |= 128;
                    this.i = bVar.k;
                }
                if (!bVar.l.isEmpty()) {
                    if (this.j.isEmpty()) {
                        this.j = bVar.l;
                        this.a &= -257;
                    } else {
                        i();
                        this.j.addAll(bVar.l);
                    }
                }
                if (bVar.t()) {
                    a(bVar.u());
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

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                Throwable th;
                b bVar;
                b bVar2;
                try {
                    bVar2 = (b) b.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (bVar2 != null) {
                        a(bVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    bVar2 = (b) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    bVar = bVar2;
                    th = th3;
                }
                if (bVar != null) {
                    a(bVar);
                }
                throw th;
            }

            public a a(String str) {
                if (str != null) {
                    this.a |= 1;
                    this.b = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public a b() {
                return h().a(e());
            }

            public a b(String str) {
                if (str != null) {
                    this.a |= 2;
                    this.c = str;
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

            public a c(String str) {
                if (str != null) {
                    this.a |= 4;
                    this.d = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public b c() {
                return b.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m0clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m1clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m2clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m3clone() throws CloneNotSupportedException {
                return b();
            }

            public a d(String str) {
                if (str != null) {
                    this.a |= 8;
                    this.e = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public b d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public a e(String str) {
                if (str != null) {
                    this.a |= 16;
                    this.f = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public b e() {
                b bVar = new b((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                bVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                bVar.e = this.c;
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                bVar.f = this.d;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                bVar.g = this.e;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                bVar.h = this.f;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                bVar.i = this.g;
                if ((i & 64) == 64) {
                    i2 |= 64;
                }
                bVar.j = this.h;
                if ((i & 128) == 128) {
                    i2 |= 128;
                }
                bVar.k = this.i;
                if ((this.a & 256) == 256) {
                    this.j = new UnmodifiableLazyStringList(this.j);
                    this.a &= -257;
                }
                bVar.l = this.j;
                if ((i & 512) == 512) {
                    i2 |= 256;
                }
                bVar.m = this.k;
                bVar.c = i2;
                return bVar;
            }

            public a f(String str) {
                if (str != null) {
                    this.a |= 64;
                    this.h = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public a g(String str) {
                if (str != null) {
                    this.a |= 128;
                    this.i = str;
                    return this;
                }
                throw new NullPointerException();
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m4getDefaultInstanceForType() {
                return c();
            }

            public a h(String str) {
                if (str != null) {
                    i();
                    this.j.add(str);
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
                return a((b) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m5mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.y();
        }

        private b(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
            Object obj = null;
            this.n = (byte) -1;
            this.o = -1;
            y();
            int i = 0;
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
                            a a = a.a(codedInputStream.readEnum());
                            if (a == null) {
                                break;
                            }
                            this.c |= 256;
                            this.m = a;
                            break;
                        case Events.E_ANTIVIRUS_SCAN /*82*/:
                            if ((i & 256) != 256) {
                                this.l = new LazyStringArrayList();
                                i |= 256;
                            }
                            this.l.add(codedInputStream.readBytes());
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
                    if ((i & 256) == 256) {
                        this.l = new UnmodifiableLazyStringList(this.l);
                    }
                    makeExtensionsImmutable();
                }
            }
            if ((i & 256) == 256) {
                this.l = new UnmodifiableLazyStringList(this.l);
            }
            makeExtensionsImmutable();
        }

        private b(Builder builder) {
            super(builder);
            this.n = (byte) -1;
            this.o = -1;
        }

        private b(boolean z) {
            this.n = (byte) -1;
            this.o = -1;
        }

        public static a a(b bVar) {
            return v().a(bVar);
        }

        public static b a() {
            return b;
        }

        public static a v() {
            return a.h();
        }

        private void y() {
            this.d = "";
            this.e = "";
            this.f = "";
            this.g = "";
            this.h = "";
            this.i = ByteString.EMPTY;
            this.j = "";
            this.k = "";
            this.l = LazyStringArrayList.EMPTY;
            this.m = a.WIN;
        }

        public b b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public ByteString d() {
            Object obj = this.d;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.d = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public ByteString f() {
            Object obj = this.e;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.e = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean g() {
            return (this.c & 4) == 4;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<b> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.o;
            if (i2 != -1) {
                return i2;
            }
            i2 = (this.c & 1) != 1 ? 0 : CodedOutputStream.computeBytesSize(1, d()) + 0;
            if ((this.c & 2) == 2) {
                i2 += CodedOutputStream.computeBytesSize(2, f());
            }
            if ((this.c & 4) == 4) {
                i2 += CodedOutputStream.computeBytesSize(3, h());
            }
            if ((this.c & 8) == 8) {
                i2 += CodedOutputStream.computeBytesSize(4, j());
            }
            if ((this.c & 16) == 16) {
                i2 += CodedOutputStream.computeBytesSize(5, l());
            }
            if ((this.c & 32) == 32) {
                i2 += CodedOutputStream.computeBytesSize(6, this.i);
            }
            if ((this.c & 64) == 64) {
                i2 += CodedOutputStream.computeBytesSize(7, p());
            }
            if ((this.c & 128) == 128) {
                i2 += CodedOutputStream.computeBytesSize(8, r());
            }
            if ((this.c & 256) == 256) {
                i2 += CodedOutputStream.computeEnumSize(9, this.m.getNumber());
            }
            int i3 = 0;
            while (i < this.l.size()) {
                i3 += CodedOutputStream.computeBytesSizeNoTag(this.l.getByteString(i));
                i++;
            }
            i2 = (i2 + i3) + (s().size() * 1);
            this.o = i2;
            return i2;
        }

        public ByteString h() {
            Object obj = this.f;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.f = copyFromUtf8;
            return copyFromUtf8;
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
            Object obj = this.g;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.g = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean k() {
            return (this.c & 16) == 16;
        }

        public ByteString l() {
            Object obj = this.h;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.h = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean m() {
            return (this.c & 32) == 32;
        }

        public ByteString n() {
            return this.i;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return w();
        }

        public boolean o() {
            return (this.c & 64) == 64;
        }

        public ByteString p() {
            Object obj = this.j;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.j = copyFromUtf8;
            return copyFromUtf8;
        }

        public boolean q() {
            return (this.c & 128) == 128;
        }

        public ByteString r() {
            Object obj = this.k;
            if (!(obj instanceof String)) {
                return (ByteString) obj;
            }
            ByteString copyFromUtf8 = ByteString.copyFromUtf8((String) obj);
            this.k = copyFromUtf8;
            return copyFromUtf8;
        }

        public List<String> s() {
            return this.l;
        }

        public boolean t() {
            return (this.c & 256) == 256;
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return x();
        }

        public a u() {
            return this.m;
        }

        public a w() {
            return v();
        }

        protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) throws IOException {
            getSerializedSize();
            if ((this.c & 1) == 1) {
                codedOutputStream.writeBytes(1, d());
            }
            if ((this.c & 2) == 2) {
                codedOutputStream.writeBytes(2, f());
            }
            if ((this.c & 4) == 4) {
                codedOutputStream.writeBytes(3, h());
            }
            if ((this.c & 8) == 8) {
                codedOutputStream.writeBytes(4, j());
            }
            if ((this.c & 16) == 16) {
                codedOutputStream.writeBytes(5, l());
            }
            if ((this.c & 32) == 32) {
                codedOutputStream.writeBytes(6, this.i);
            }
            if ((this.c & 64) == 64) {
                codedOutputStream.writeBytes(7, p());
            }
            if ((this.c & 128) == 128) {
                codedOutputStream.writeBytes(8, r());
            }
            if ((this.c & 256) == 256) {
                codedOutputStream.writeEnum(9, this.m.getNumber());
            }
            for (int i = 0; i < this.l.size(); i++) {
                codedOutputStream.writeBytes(10, this.l.getByteString(i));
            }
        }

        public a x() {
            return a(this);
        }
    }

    /* compiled from: Unknown */
    public interface e extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class d extends GeneratedMessageLite implements e {
        public static Parser<d> a = new ad();
        private static final d b = new d(true);
        private int c;
        private b d;
        private byte e;
        private int f;

        /* compiled from: Unknown */
        public static final class a extends Builder<d, a> implements e {
            private int a;
            private b b = b.SUCCESS;

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
                this.b = b.SUCCESS;
                this.a &= -2;
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

            public a a(d dVar) {
                if (dVar != d.a() && dVar.c()) {
                    a(dVar.d());
                }
                return this;
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                d dVar;
                Throwable th;
                d dVar2;
                try {
                    dVar = (d) d.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (dVar != null) {
                        a(dVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    dVar = (d) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    dVar2 = dVar;
                    th = th3;
                }
                if (dVar2 != null) {
                    a(dVar2);
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

            public d c() {
                return d.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m6clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m7clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m8clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m9clone() throws CloneNotSupportedException {
                return b();
            }

            public d d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public d e() {
                d dVar = new d((Builder) this);
                int i = 0;
                if ((this.a & 1) == 1) {
                    i = 1;
                }
                dVar.d = this.b;
                dVar.c = i;
                return dVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m10getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((d) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m11mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws IOException {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        /* compiled from: Unknown */
        public enum b implements EnumLite {
            SUCCESS(0, 1),
            FAILURE(1, 2),
            REDIRECT_ID_EXISTS(2, 3);
            
            private static EnumLiteMap<b> d;
            private final int e;

            static {
                d = new ae();
            }

            private b(int i, int i2) {
                this.e = i2;
            }

            public static b a(int i) {
                switch (i) {
                    case 1:
                        return SUCCESS;
                    case 2:
                        return FAILURE;
                    case 3:
                        return REDIRECT_ID_EXISTS;
                    default:
                        return null;
                }
            }

            public final int getNumber() {
                return this.e;
            }
        }

        static {
            b.h();
        }

        private d(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) throws InvalidProtocolBufferException {
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
                        case 8:
                            b a = b.a(codedInputStream.readEnum());
                            if (a == null) {
                                break;
                            }
                            this.c |= 1;
                            this.d = a;
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

        private d(Builder builder) {
            super(builder);
            this.e = (byte) -1;
            this.f = -1;
        }

        private d(boolean z) {
            this.e = (byte) -1;
            this.f = -1;
        }

        public static a a(d dVar) {
            return e().a(dVar);
        }

        public static d a() {
            return b;
        }

        public static d a(InputStream inputStream) throws IOException {
            return (d) a.parseFrom(inputStream);
        }

        public static a e() {
            return a.h();
        }

        private void h() {
            this.d = b.SUCCESS;
        }

        public d b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public b d() {
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

        public Parser<d> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.f;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeEnumSize(1, this.d.getNumber()) + 0;
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
                codedOutputStream.writeEnum(1, this.d.getNumber());
            }
        }
    }
}
