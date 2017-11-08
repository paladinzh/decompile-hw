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
import com.huawei.systemmanager.power.model.RemainingTimeSceneHelper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* compiled from: Unknown */
public final class bc {

    /* compiled from: Unknown */
    public interface b extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class a extends GeneratedMessageLite implements b {
        public static Parser<a> a = new bd();
        private static final a b = new a(true);
        private int c;
        private ByteString d;
        private float e;
        private byte f;
        private int g;

        /* compiled from: Unknown */
        public static final class a extends Builder<a, a> implements b {
            private int a;
            private ByteString b = ByteString.EMPTY;
            private float c;

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
                this.c = 0.0f;
                this.a &= -3;
                return this;
            }

            public a a(float f) {
                this.a |= 2;
                this.c = f;
                return this;
            }

            public a a(a aVar) {
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

            public a b() {
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
            public /* synthetic */ MessageLite.Builder m48clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m49clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m50clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m51clone() {
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
            public /* synthetic */ MessageLite m52getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m53mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.j();
        }

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
                            obj = 1;
                            break;
                        case 10:
                            this.c |= 1;
                            this.d = codedInputStream.readBytes();
                            break;
                        case 21:
                            this.c |= 2;
                            this.e = codedInputStream.readFloat();
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
            this.f = (byte) -1;
            this.g = -1;
        }

        private a(boolean z) {
            this.f = (byte) -1;
            this.g = -1;
        }

        public static a a(a aVar) {
            return g().a(aVar);
        }

        public static a a() {
            return b;
        }

        public static a g() {
            return a.h();
        }

        private void j() {
            this.d = ByteString.EMPTY;
            this.e = 0.0f;
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

        public float f() {
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
                i = CodedOutputStream.computeBytesSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeFloatSize(2, this.e);
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
                codedOutputStream.writeFloat(2, this.e);
            }
        }
    }

    /* compiled from: Unknown */
    public enum aa implements EnumLite {
        VERBOSE(0, 1),
        DEBUG(1, 2),
        INFO(2, 3),
        WARNING(3, 4),
        ERROR(4, 5),
        ASSERT(5, 6);
        
        private static EnumLiteMap<aa> g;
        private final int h;

        static {
            g = new bq();
        }

        private aa(int i, int i2) {
            this.h = i2;
        }

        public static aa a(int i) {
            switch (i) {
                case 1:
                    return VERBOSE;
                case 2:
                    return DEBUG;
                case 3:
                    return INFO;
                case 4:
                    return WARNING;
                case 5:
                    return ERROR;
                case 6:
                    return ASSERT;
                default:
                    return null;
            }
        }

        public final int getNumber() {
            return this.h;
        }
    }

    /* compiled from: Unknown */
    public interface ac extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class ab extends GeneratedMessageLite implements ac {
        public static Parser<ab> a = new br();
        private static final ab b = new ab(true);
        private int c;
        private ByteString d;
        private aa e;
        private byte f;
        private int g;

        /* compiled from: Unknown */
        public static final class a extends Builder<ab, a> implements ac {
            private int a;
            private ByteString b = ByteString.EMPTY;
            private aa c = aa.VERBOSE;

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
                this.c = aa.VERBOSE;
                this.a &= -3;
                return this;
            }

            public a a(aa aaVar) {
                if (aaVar != null) {
                    this.a |= 2;
                    this.c = aaVar;
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(ab abVar) {
                if (abVar == ab.a()) {
                    return this;
                }
                if (abVar.c()) {
                    a(abVar.d());
                }
                if (abVar.e()) {
                    a(abVar.f());
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
                ab abVar;
                ab abVar2;
                try {
                    abVar2 = (ab) ab.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (abVar2 != null) {
                        a(abVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    abVar2 = (ab) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    abVar = abVar2;
                    th = th3;
                }
                if (abVar != null) {
                    a(abVar);
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

            public ab c() {
                return ab.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m54clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m55clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m56clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m57clone() {
                return b();
            }

            public ab d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public ab e() {
                ab abVar = new ab((Builder) this);
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                abVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                abVar.e = this.c;
                abVar.c = i2;
                return abVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m58getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((ab) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m59mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.j();
        }

        private ab(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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
                        case 16:
                            aa a = aa.a(codedInputStream.readEnum());
                            if (a == null) {
                                break;
                            }
                            this.c |= 2;
                            this.e = a;
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

        private ab(Builder builder) {
            super(builder);
            this.f = (byte) -1;
            this.g = -1;
        }

        private ab(boolean z) {
            this.f = (byte) -1;
            this.g = -1;
        }

        public static a a(ab abVar) {
            return g().a(abVar);
        }

        public static ab a() {
            return b;
        }

        public static a g() {
            return a.h();
        }

        private void j() {
            this.d = ByteString.EMPTY;
            this.e = aa.VERBOSE;
        }

        public ab b() {
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

        public aa f() {
            return this.e;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<ab> getParserForType() {
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
                i += CodedOutputStream.computeEnumSize(2, this.e.getNumber());
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
                codedOutputStream.writeEnum(2, this.e.getNumber());
            }
        }
    }

    /* compiled from: Unknown */
    public interface ag extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class ad extends GeneratedMessageLite implements ag {
        public static Parser<ad> a = new bs();
        private static final ad b = new ad(true);
        private List<ae> c;
        private byte d;
        private int e;

        /* compiled from: Unknown */
        public static final class a extends Builder<ad, a> implements ag {
            private int a;
            private List<ae> b = Collections.emptyList();

            private a() {
                h();
            }

            private void h() {
            }

            private static a i() {
                return new a();
            }

            private void j() {
                if ((this.a & 1) != 1) {
                    this.b = new ArrayList(this.b);
                    this.a |= 1;
                }
            }

            public a a() {
                super.clear();
                this.b = Collections.emptyList();
                this.a &= -2;
                return this;
            }

            public a a(ad adVar) {
                if (!(adVar == ad.a() || adVar.c.isEmpty())) {
                    if (this.b.isEmpty()) {
                        this.b = adVar.c;
                        this.a &= -2;
                    } else {
                        j();
                        this.b.addAll(adVar.c);
                    }
                }
                return this;
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                Throwable th;
                ad adVar;
                ad adVar2;
                try {
                    adVar2 = (ad) ad.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (adVar2 != null) {
                        a(adVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    adVar2 = (ad) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    adVar = adVar2;
                    th = th3;
                }
                if (adVar != null) {
                    a(adVar);
                }
                throw th;
            }

            public ae a(int i) {
                return (ae) this.b.get(i);
            }

            public a b() {
                return i().a(e());
            }

            public /* synthetic */ MessageLite build() {
                return d();
            }

            public /* synthetic */ MessageLite buildPartial() {
                return e();
            }

            public ad c() {
                return ad.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m60clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m61clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m62clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m63clone() {
                return b();
            }

            public ad d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public ad e() {
                ad adVar = new ad((Builder) this);
                int i = this.a;
                if ((this.a & 1) == 1) {
                    this.b = Collections.unmodifiableList(this.b);
                    this.a &= -2;
                }
                adVar.c = this.b;
                return adVar;
            }

            public int f() {
                return this.b.size();
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m64getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                for (int i = 0; i < f(); i++) {
                    if (!a(i).isInitialized()) {
                        return false;
                    }
                }
                return true;
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((ad) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m65mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.h();
        }

        private ad(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.d = (byte) -1;
            this.e = -1;
            h();
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
                                this.c = new ArrayList();
                                i |= 1;
                            }
                            this.c.add(codedInputStream.readMessage(ae.a, extensionRegistryLite));
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
                    if ((i & 1) == 1) {
                        this.c = Collections.unmodifiableList(this.c);
                    }
                    makeExtensionsImmutable();
                }
            }
            if ((i & 1) == 1) {
                this.c = Collections.unmodifiableList(this.c);
            }
            makeExtensionsImmutable();
        }

        private ad(Builder builder) {
            super(builder);
            this.d = (byte) -1;
            this.e = -1;
        }

        private ad(boolean z) {
            this.d = (byte) -1;
            this.e = -1;
        }

        public static a a(ad adVar) {
            return e().a(adVar);
        }

        public static ad a() {
            return b;
        }

        public static a e() {
            return a.i();
        }

        private void h() {
            this.c = Collections.emptyList();
        }

        public ae a(int i) {
            return (ae) this.c.get(i);
        }

        public ad b() {
            return b;
        }

        public List<ae> c() {
            return this.c;
        }

        public int d() {
            return this.c.size();
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

        public Parser<ad> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = this.e;
            if (i != -1) {
                return i;
            }
            int i2 = 0;
            for (i = 0; i < this.c.size(); i++) {
                i2 += CodedOutputStream.computeMessageSize(1, (MessageLite) this.c.get(i));
            }
            this.e = i2;
            return i2;
        }

        public final boolean isInitialized() {
            boolean z = false;
            byte b = this.d;
            if (b == (byte) -1) {
                int i = 0;
                while (i < d()) {
                    if (a(i).isInitialized()) {
                        i++;
                    } else {
                        this.d = (byte) 0;
                        return false;
                    }
                }
                this.d = (byte) 1;
                return true;
            }
            if (b == (byte) 1) {
                z = true;
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
            for (int i = 0; i < this.c.size(); i++) {
                codedOutputStream.writeMessage(1, (MessageLite) this.c.get(i));
            }
        }
    }

    /* compiled from: Unknown */
    public interface af extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class ae extends GeneratedMessageLite implements af {
        public static Parser<ae> a = new bt();
        private static final ae b = new ae(true);
        private int c;
        private ByteString d;
        private int e;
        private long f;
        private ByteString g;
        private boolean h;
        private int i;
        private byte j;
        private int k;

        /* compiled from: Unknown */
        public static final class a extends Builder<ae, a> implements af {
            private int a;
            private ByteString b = ByteString.EMPTY;
            private int c;
            private long d;
            private ByteString e = ByteString.EMPTY;
            private boolean f;
            private int g;

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
                this.b = ByteString.EMPTY;
                this.a &= -2;
                this.c = 0;
                this.a &= -3;
                this.d = 0;
                this.a &= -5;
                this.e = ByteString.EMPTY;
                this.a &= -9;
                this.f = false;
                this.a &= -17;
                this.g = 0;
                this.a &= -33;
                return this;
            }

            public a a(int i) {
                this.a |= 2;
                this.c = i;
                return this;
            }

            public a a(long j) {
                this.a |= 4;
                this.d = j;
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
                    a(aeVar.f());
                }
                if (aeVar.g()) {
                    a(aeVar.h());
                }
                if (aeVar.i()) {
                    b(aeVar.j());
                }
                if (aeVar.k()) {
                    a(aeVar.l());
                }
                if (aeVar.m()) {
                    b(aeVar.n());
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

            public a a(boolean z) {
                this.a |= 16;
                this.f = z;
                return this;
            }

            public a b() {
                return i().a(e());
            }

            public a b(int i) {
                this.a |= 32;
                this.g = i;
                return this;
            }

            public a b(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 8;
                    this.e = byteString;
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

            public ae c() {
                return ae.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m66clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m67clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m68clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m69clone() {
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
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                aeVar.g = this.e;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                aeVar.h = this.f;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                aeVar.i = this.g;
                aeVar.c = i2;
                return aeVar;
            }

            public boolean f() {
                return (this.a & 1) == 1;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m70getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return f();
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((ae) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m71mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.r();
        }

        private ae(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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
                        case 16:
                            this.c |= 2;
                            this.e = codedInputStream.readInt32();
                            break;
                        case 24:
                            this.c |= 4;
                            this.f = codedInputStream.readInt64();
                            break;
                        case 34:
                            this.c |= 8;
                            this.g = codedInputStream.readBytes();
                            break;
                        case 40:
                            this.c |= 16;
                            this.h = codedInputStream.readBool();
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

        private ae(Builder builder) {
            super(builder);
            this.j = (byte) -1;
            this.k = -1;
        }

        private ae(boolean z) {
            this.j = (byte) -1;
            this.k = -1;
        }

        public static a a(ae aeVar) {
            return o().a(aeVar);
        }

        public static ae a() {
            return b;
        }

        public static a o() {
            return a.i();
        }

        private void r() {
            this.d = ByteString.EMPTY;
            this.e = 0;
            this.f = 0;
            this.g = ByteString.EMPTY;
            this.h = false;
            this.i = 0;
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

        public int f() {
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
            int i2 = this.k;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeBytesSize(1, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeInt32Size(2, this.e);
            }
            if ((this.c & 4) == 4) {
                i += CodedOutputStream.computeInt64Size(3, this.f);
            }
            if ((this.c & 8) == 8) {
                i += CodedOutputStream.computeBytesSize(4, this.g);
            }
            if ((this.c & 16) == 16) {
                i += CodedOutputStream.computeBoolSize(5, this.h);
            }
            if ((this.c & 32) == 32) {
                i += CodedOutputStream.computeInt32Size(6, this.i);
            }
            this.k = i;
            return i;
        }

        public long h() {
            return this.f;
        }

        public boolean i() {
            return (this.c & 8) == 8;
        }

        public final boolean isInitialized() {
            boolean z = false;
            byte b = this.j;
            if (b != (byte) -1) {
                if (b == (byte) 1) {
                    z = true;
                }
                return z;
            } else if (c()) {
                this.j = (byte) 1;
                return true;
            } else {
                this.j = (byte) 0;
                return false;
            }
        }

        public ByteString j() {
            return this.g;
        }

        public boolean k() {
            return (this.c & 16) == 16;
        }

        public boolean l() {
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
                codedOutputStream.writeInt32(2, this.e);
            }
            if ((this.c & 4) == 4) {
                codedOutputStream.writeInt64(3, this.f);
            }
            if ((this.c & 8) == 8) {
                codedOutputStream.writeBytes(4, this.g);
            }
            if ((this.c & 16) == 16) {
                codedOutputStream.writeBool(5, this.h);
            }
            if ((this.c & 32) == 32) {
                codedOutputStream.writeInt32(6, this.i);
            }
        }
    }

    /* compiled from: Unknown */
    public enum ah implements EnumLite {
        START(0, 1),
        STOP(1, 2);
        
        private static EnumLiteMap<ah> c;
        private final int d;

        static {
            c = new bu();
        }

        private ah(int i, int i2) {
            this.d = i2;
        }

        public static ah a(int i) {
            switch (i) {
                case 1:
                    return START;
                case 2:
                    return STOP;
                default:
                    return null;
            }
        }

        public final int getNumber() {
            return this.d;
        }
    }

    /* compiled from: Unknown */
    public interface d extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class c extends GeneratedMessageLite implements d {
        public static Parser<c> a = new be();
        private static final c b = new c(true);
        private int c;
        private ByteString d;
        private byte e;
        private int f;

        /* compiled from: Unknown */
        public static final class a extends Builder<c, a> implements d {
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

            public a a(c cVar) {
                if (cVar != c.a() && cVar.c()) {
                    a(cVar.d());
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
            public /* synthetic */ MessageLite.Builder m72clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m73clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m74clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m75clone() {
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
            public /* synthetic */ MessageLite m76getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m77mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.h();
        }

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
            this.d = ByteString.EMPTY;
        }

        public c b() {
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
    public interface f extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class e extends GeneratedMessageLite implements f {
        public static Parser<e> a = new bf();
        private static final e b = new e(true);
        private byte c;
        private int d;

        /* compiled from: Unknown */
        public static final class a extends Builder<e, a> implements f {
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
                return this;
            }

            public a a(e eVar) {
                return eVar != e.a() ? this : this;
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
            public /* synthetic */ MessageLite.Builder m78clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m79clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m80clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m81clone() {
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
                return new e((Builder) this);
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m82getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m83mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.f();
        }

        private e(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.c = (byte) -1;
            this.d = -1;
            f();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
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

        private e(Builder builder) {
            super(builder);
            this.c = (byte) -1;
            this.d = -1;
        }

        private e(boolean z) {
            this.c = (byte) -1;
            this.d = -1;
        }

        public static a a(e eVar) {
            return c().a(eVar);
        }

        public static e a() {
            return b;
        }

        public static a c() {
            return a.h();
        }

        private void f() {
        }

        public e b() {
            return b;
        }

        public a d() {
            return c();
        }

        public a e() {
            return a(this);
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<e> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = this.d;
            if (i != -1) {
                return i;
            }
            this.d = 0;
            return 0;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.c;
            if (b == (byte) -1) {
                this.c = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return d();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return e();
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            getSerializedSize();
        }
    }

    /* compiled from: Unknown */
    public interface h extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class g extends GeneratedMessageLite implements h {
        public static Parser<g> a = new bg();
        private static final g b = new g(true);
        private int A;
        private boolean B;
        private byte C;
        private int D;
        private int c;
        private ByteString d;
        private ByteString e;
        private ByteString f;
        private ByteString g;
        private ByteString h;
        private ByteString i;
        private ByteString j;
        private ByteString k;
        private boolean l;
        private boolean m;
        private boolean n;
        private boolean o;
        private ByteString p;
        private boolean q;
        private boolean r;
        private boolean s;
        private ah t;
        private boolean u;
        private int v;
        private int w;
        private int x;
        private int y;
        private int z;

        /* compiled from: Unknown */
        public static final class a extends Builder<g, a> implements h {
            private int a;
            private ByteString b = ByteString.EMPTY;
            private ByteString c = ByteString.EMPTY;
            private ByteString d = ByteString.EMPTY;
            private ByteString e = ByteString.EMPTY;
            private ByteString f = ByteString.EMPTY;
            private ByteString g = ByteString.EMPTY;
            private ByteString h = ByteString.EMPTY;
            private ByteString i = ByteString.EMPTY;
            private boolean j;
            private boolean k;
            private boolean l;
            private boolean m;
            private ByteString n = ByteString.EMPTY;
            private boolean o;
            private boolean p;
            private boolean q;
            private ah r = ah.START;
            private boolean s;
            private int t;
            private int u;
            private int v;
            private int w;
            private int x;
            private int y;
            private boolean z;

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
                this.j = false;
                this.a &= -257;
                this.k = false;
                this.a &= -513;
                this.l = false;
                this.a &= -1025;
                this.m = false;
                this.a &= -2049;
                this.n = ByteString.EMPTY;
                this.a &= -4097;
                this.o = false;
                this.a &= -8193;
                this.p = false;
                this.a &= -16385;
                this.q = false;
                this.a &= -32769;
                this.r = ah.START;
                this.a &= -65537;
                this.s = false;
                this.a &= -131073;
                this.t = 0;
                this.a &= -262145;
                this.u = 0;
                this.a &= -524289;
                this.v = 0;
                this.a &= -1048577;
                this.w = 0;
                this.a &= -2097153;
                this.x = 0;
                this.a &= -4194305;
                this.y = 0;
                this.a &= -8388609;
                this.z = false;
                this.a &= -16777217;
                return this;
            }

            public a a(int i) {
                this.a |= 262144;
                this.t = i;
                return this;
            }

            public a a(ah ahVar) {
                if (ahVar != null) {
                    this.a |= 65536;
                    this.r = ahVar;
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(g gVar) {
                if (gVar == g.a()) {
                    return this;
                }
                if (gVar.c()) {
                    a(gVar.d());
                }
                if (gVar.e()) {
                    b(gVar.f());
                }
                if (gVar.g()) {
                    c(gVar.h());
                }
                if (gVar.i()) {
                    d(gVar.j());
                }
                if (gVar.k()) {
                    e(gVar.l());
                }
                if (gVar.m()) {
                    f(gVar.n());
                }
                if (gVar.o()) {
                    g(gVar.p());
                }
                if (gVar.q()) {
                    h(gVar.r());
                }
                if (gVar.s()) {
                    a(gVar.t());
                }
                if (gVar.u()) {
                    b(gVar.v());
                }
                if (gVar.w()) {
                    c(gVar.x());
                }
                if (gVar.y()) {
                    d(gVar.z());
                }
                if (gVar.A()) {
                    i(gVar.B());
                }
                if (gVar.C()) {
                    e(gVar.D());
                }
                if (gVar.E()) {
                    f(gVar.F());
                }
                if (gVar.G()) {
                    g(gVar.H());
                }
                if (gVar.I()) {
                    a(gVar.J());
                }
                if (gVar.K()) {
                    h(gVar.L());
                }
                if (gVar.M()) {
                    a(gVar.N());
                }
                if (gVar.O()) {
                    b(gVar.P());
                }
                if (gVar.Q()) {
                    c(gVar.R());
                }
                if (gVar.S()) {
                    d(gVar.T());
                }
                if (gVar.U()) {
                    e(gVar.V());
                }
                if (gVar.W()) {
                    f(gVar.X());
                }
                if (gVar.Y()) {
                    i(gVar.Z());
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

            public a a(boolean z) {
                this.a |= 256;
                this.j = z;
                return this;
            }

            public a b() {
                return h().a(e());
            }

            public a b(int i) {
                this.a |= 524288;
                this.u = i;
                return this;
            }

            public a b(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 2;
                    this.c = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a b(boolean z) {
                this.a |= 512;
                this.k = z;
                return this;
            }

            public /* synthetic */ MessageLite build() {
                return d();
            }

            public /* synthetic */ MessageLite buildPartial() {
                return e();
            }

            public a c(int i) {
                this.a |= 1048576;
                this.v = i;
                return this;
            }

            public a c(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 4;
                    this.d = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a c(boolean z) {
                this.a |= 1024;
                this.l = z;
                return this;
            }

            public g c() {
                return g.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m84clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m85clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m86clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m87clone() {
                return b();
            }

            public a d(int i) {
                this.a |= 2097152;
                this.w = i;
                return this;
            }

            public a d(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 8;
                    this.e = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a d(boolean z) {
                this.a |= 2048;
                this.m = z;
                return this;
            }

            public g d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public a e(int i) {
                this.a |= 4194304;
                this.x = i;
                return this;
            }

            public a e(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 16;
                    this.f = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a e(boolean z) {
                this.a |= 8192;
                this.o = z;
                return this;
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
                if ((i & 4) == 4) {
                    i2 |= 4;
                }
                gVar.f = this.d;
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                gVar.g = this.e;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                gVar.h = this.f;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                gVar.i = this.g;
                if ((i & 64) == 64) {
                    i2 |= 64;
                }
                gVar.j = this.h;
                if ((i & 128) == 128) {
                    i2 |= 128;
                }
                gVar.k = this.i;
                if ((i & 256) == 256) {
                    i2 |= 256;
                }
                gVar.l = this.j;
                if ((i & 512) == 512) {
                    i2 |= 512;
                }
                gVar.m = this.k;
                if ((i & 1024) == 1024) {
                    i2 |= 1024;
                }
                gVar.n = this.l;
                if ((i & 2048) == 2048) {
                    i2 |= 2048;
                }
                gVar.o = this.m;
                if ((i & 4096) == 4096) {
                    i2 |= 4096;
                }
                gVar.p = this.n;
                if ((i & 8192) == 8192) {
                    i2 |= 8192;
                }
                gVar.q = this.o;
                if ((i & 16384) == 16384) {
                    i2 |= 16384;
                }
                gVar.r = this.p;
                if ((i & 32768) == 32768) {
                    i2 |= 32768;
                }
                gVar.s = this.q;
                if ((i & 65536) == 65536) {
                    i2 |= 65536;
                }
                gVar.t = this.r;
                if ((i & 131072) == 131072) {
                    i2 |= 131072;
                }
                gVar.u = this.s;
                if ((i & 262144) == 262144) {
                    i2 |= 262144;
                }
                gVar.v = this.t;
                if ((i & 524288) == 524288) {
                    i2 |= 524288;
                }
                gVar.w = this.u;
                if ((1048576 & i) == 1048576) {
                    i2 |= 1048576;
                }
                gVar.x = this.v;
                if ((2097152 & i) == 2097152) {
                    i2 |= 2097152;
                }
                gVar.y = this.w;
                if ((4194304 & i) == 4194304) {
                    i2 |= 4194304;
                }
                gVar.z = this.x;
                if ((8388608 & i) == 8388608) {
                    i2 |= 8388608;
                }
                gVar.A = this.y;
                if ((i & 16777216) == 16777216) {
                    i2 |= 16777216;
                }
                gVar.B = this.z;
                gVar.c = i2;
                return gVar;
            }

            public a f(int i) {
                this.a |= 8388608;
                this.y = i;
                return this;
            }

            public a f(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 32;
                    this.g = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a f(boolean z) {
                this.a |= 16384;
                this.p = z;
                return this;
            }

            public a g(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 64;
                    this.h = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a g(boolean z) {
                this.a |= 32768;
                this.q = z;
                return this;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m88getDefaultInstanceForType() {
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

            public a h(boolean z) {
                this.a |= 131072;
                this.s = z;
                return this;
            }

            public a i(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 4096;
                    this.n = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a i(boolean z) {
                this.a |= 16777216;
                this.z = z;
                return this;
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
            public /* synthetic */ MessageLite.Builder m89mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.ad();
        }

        private g(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.C = (byte) -1;
            this.D = -1;
            ad();
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
                            this.l = codedInputStream.readBool();
                            break;
                        case 80:
                            this.c |= 512;
                            this.m = codedInputStream.readBool();
                            break;
                        case 88:
                            this.c |= 1024;
                            this.n = codedInputStream.readBool();
                            break;
                        case 96:
                            this.c |= 2048;
                            this.o = codedInputStream.readBool();
                            break;
                        case 106:
                            this.c |= 4096;
                            this.p = codedInputStream.readBytes();
                            break;
                        case Events.E_ADDVIEW_SET_ALL /*112*/:
                            this.c |= 8192;
                            this.q = codedInputStream.readBool();
                            break;
                        case CircleViewNew.SIZE_OF_COLOR /*120*/:
                            this.c |= 16384;
                            this.r = codedInputStream.readBool();
                            break;
                        case 128:
                            this.c |= 32768;
                            this.s = codedInputStream.readBool();
                            break;
                        case 136:
                            ah a = ah.a(codedInputStream.readEnum());
                            if (a == null) {
                                break;
                            }
                            this.c |= 65536;
                            this.t = a;
                            break;
                        case RemainingTimeSceneHelper.TIME_SCENE_NUM_ONE_DAY /*144*/:
                            this.c |= 131072;
                            this.u = codedInputStream.readBool();
                            break;
                        case 152:
                            this.c |= 262144;
                            this.v = codedInputStream.readInt32();
                            break;
                        case 160:
                            this.c |= 524288;
                            this.w = codedInputStream.readInt32();
                            break;
                        case 168:
                            this.c |= 1048576;
                            this.x = codedInputStream.readInt32();
                            break;
                        case 176:
                            this.c |= 2097152;
                            this.y = codedInputStream.readInt32();
                            break;
                        case 184:
                            this.c |= 4194304;
                            this.z = codedInputStream.readInt32();
                            break;
                        case 192:
                            this.c |= 8388608;
                            this.A = codedInputStream.readInt32();
                            break;
                        case 200:
                            this.c |= 16777216;
                            this.B = codedInputStream.readBool();
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
            this.C = (byte) -1;
            this.D = -1;
        }

        private g(boolean z) {
            this.C = (byte) -1;
            this.D = -1;
        }

        public static a a(g gVar) {
            return aa().a(gVar);
        }

        public static g a() {
            return b;
        }

        public static a aa() {
            return a.h();
        }

        private void ad() {
            this.d = ByteString.EMPTY;
            this.e = ByteString.EMPTY;
            this.f = ByteString.EMPTY;
            this.g = ByteString.EMPTY;
            this.h = ByteString.EMPTY;
            this.i = ByteString.EMPTY;
            this.j = ByteString.EMPTY;
            this.k = ByteString.EMPTY;
            this.l = false;
            this.m = false;
            this.n = false;
            this.o = false;
            this.p = ByteString.EMPTY;
            this.q = false;
            this.r = false;
            this.s = false;
            this.t = ah.START;
            this.u = false;
            this.v = 0;
            this.w = 0;
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.A = 0;
            this.B = false;
        }

        public boolean A() {
            return (this.c & 4096) == 4096;
        }

        public ByteString B() {
            return this.p;
        }

        public boolean C() {
            return (this.c & 8192) == 8192;
        }

        public boolean D() {
            return this.q;
        }

        public boolean E() {
            return (this.c & 16384) == 16384;
        }

        public boolean F() {
            return this.r;
        }

        public boolean G() {
            return (this.c & 32768) == 32768;
        }

        public boolean H() {
            return this.s;
        }

        public boolean I() {
            return (this.c & 65536) == 65536;
        }

        public ah J() {
            return this.t;
        }

        public boolean K() {
            return (this.c & 131072) == 131072;
        }

        public boolean L() {
            return this.u;
        }

        public boolean M() {
            return (this.c & 262144) == 262144;
        }

        public int N() {
            return this.v;
        }

        public boolean O() {
            return (this.c & 524288) == 524288;
        }

        public int P() {
            return this.w;
        }

        public boolean Q() {
            return (this.c & 1048576) == 1048576;
        }

        public int R() {
            return this.x;
        }

        public boolean S() {
            return (this.c & 2097152) == 2097152;
        }

        public int T() {
            return this.y;
        }

        public boolean U() {
            return (this.c & 4194304) == 4194304;
        }

        public int V() {
            return this.z;
        }

        public boolean W() {
            return (this.c & 8388608) == 8388608;
        }

        public int X() {
            return this.A;
        }

        public boolean Y() {
            return (this.c & 16777216) == 16777216;
        }

        public boolean Z() {
            return this.B;
        }

        public a ab() {
            return aa();
        }

        public a ac() {
            return a(this);
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

        public Parser<g> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.D;
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
                i += CodedOutputStream.computeBoolSize(9, this.l);
            }
            if ((this.c & 512) == 512) {
                i += CodedOutputStream.computeBoolSize(10, this.m);
            }
            if ((this.c & 1024) == 1024) {
                i += CodedOutputStream.computeBoolSize(11, this.n);
            }
            if ((this.c & 2048) == 2048) {
                i += CodedOutputStream.computeBoolSize(12, this.o);
            }
            if ((this.c & 4096) == 4096) {
                i += CodedOutputStream.computeBytesSize(13, this.p);
            }
            if ((this.c & 8192) == 8192) {
                i += CodedOutputStream.computeBoolSize(14, this.q);
            }
            if ((this.c & 16384) == 16384) {
                i += CodedOutputStream.computeBoolSize(15, this.r);
            }
            if ((this.c & 32768) == 32768) {
                i += CodedOutputStream.computeBoolSize(16, this.s);
            }
            if ((this.c & 65536) == 65536) {
                i += CodedOutputStream.computeEnumSize(17, this.t.getNumber());
            }
            if ((this.c & 131072) == 131072) {
                i += CodedOutputStream.computeBoolSize(18, this.u);
            }
            if ((this.c & 262144) == 262144) {
                i += CodedOutputStream.computeInt32Size(19, this.v);
            }
            if ((this.c & 524288) == 524288) {
                i += CodedOutputStream.computeInt32Size(20, this.w);
            }
            if ((this.c & 1048576) == 1048576) {
                i += CodedOutputStream.computeInt32Size(21, this.x);
            }
            if ((this.c & 2097152) == 2097152) {
                i += CodedOutputStream.computeInt32Size(22, this.y);
            }
            if ((this.c & 4194304) == 4194304) {
                i += CodedOutputStream.computeInt32Size(23, this.z);
            }
            if ((this.c & 8388608) == 8388608) {
                i += CodedOutputStream.computeInt32Size(24, this.A);
            }
            if ((this.c & 16777216) == 16777216) {
                i += CodedOutputStream.computeBoolSize(25, this.B);
            }
            this.D = i;
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
            byte b = this.C;
            if (b == (byte) -1) {
                this.C = (byte) 1;
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
            return ab();
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

        public boolean t() {
            return this.l;
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return ac();
        }

        public boolean u() {
            return (this.c & 512) == 512;
        }

        public boolean v() {
            return this.m;
        }

        public boolean w() {
            return (this.c & 1024) == 1024;
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
                codedOutputStream.writeBool(9, this.l);
            }
            if ((this.c & 512) == 512) {
                codedOutputStream.writeBool(10, this.m);
            }
            if ((this.c & 1024) == 1024) {
                codedOutputStream.writeBool(11, this.n);
            }
            if ((this.c & 2048) == 2048) {
                codedOutputStream.writeBool(12, this.o);
            }
            if ((this.c & 4096) == 4096) {
                codedOutputStream.writeBytes(13, this.p);
            }
            if ((this.c & 8192) == 8192) {
                codedOutputStream.writeBool(14, this.q);
            }
            if ((this.c & 16384) == 16384) {
                codedOutputStream.writeBool(15, this.r);
            }
            if ((this.c & 32768) == 32768) {
                codedOutputStream.writeBool(16, this.s);
            }
            if ((this.c & 65536) == 65536) {
                codedOutputStream.writeEnum(17, this.t.getNumber());
            }
            if ((this.c & 131072) == 131072) {
                codedOutputStream.writeBool(18, this.u);
            }
            if ((this.c & 262144) == 262144) {
                codedOutputStream.writeInt32(19, this.v);
            }
            if ((this.c & 524288) == 524288) {
                codedOutputStream.writeInt32(20, this.w);
            }
            if ((this.c & 1048576) == 1048576) {
                codedOutputStream.writeInt32(21, this.x);
            }
            if ((this.c & 2097152) == 2097152) {
                codedOutputStream.writeInt32(22, this.y);
            }
            if ((this.c & 4194304) == 4194304) {
                codedOutputStream.writeInt32(23, this.z);
            }
            if ((this.c & 8388608) == 8388608) {
                codedOutputStream.writeInt32(24, this.A);
            }
            if ((this.c & 16777216) == 16777216) {
                codedOutputStream.writeBool(25, this.B);
            }
        }

        public boolean x() {
            return this.n;
        }

        public boolean y() {
            return (this.c & 2048) == 2048;
        }

        public boolean z() {
            return this.o;
        }
    }

    /* compiled from: Unknown */
    public interface j extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class i extends GeneratedMessageLite implements j {
        public static Parser<i> a = new bh();
        private static final i b = new i(true);
        private byte c;
        private int d;

        /* compiled from: Unknown */
        public static final class a extends Builder<i, a> implements j {
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
                return this;
            }

            public a a(i iVar) {
                return iVar != i.a() ? this : this;
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                Throwable th;
                i iVar;
                i iVar2;
                try {
                    iVar2 = (i) i.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (iVar2 != null) {
                        a(iVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    iVar2 = (i) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    iVar = iVar2;
                    th = th3;
                }
                if (iVar != null) {
                    a(iVar);
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
            public /* synthetic */ MessageLite.Builder m90clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m91clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m92clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m93clone() {
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
                return new i((Builder) this);
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m94getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m95mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.f();
        }

        private i(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.c = (byte) -1;
            this.d = -1;
            f();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
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
            this.c = (byte) -1;
            this.d = -1;
        }

        private i(boolean z) {
            this.c = (byte) -1;
            this.d = -1;
        }

        public static a a(i iVar) {
            return c().a(iVar);
        }

        public static i a() {
            return b;
        }

        public static a c() {
            return a.h();
        }

        private void f() {
        }

        public i b() {
            return b;
        }

        public a d() {
            return c();
        }

        public a e() {
            return a(this);
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<i> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = this.d;
            if (i != -1) {
                return i;
            }
            this.d = 0;
            return 0;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.c;
            if (b == (byte) -1) {
                this.c = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return d();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return e();
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            getSerializedSize();
        }
    }

    /* compiled from: Unknown */
    public interface l extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class k extends GeneratedMessageLite implements l {
        public static Parser<k> a = new bi();
        private static final k b = new k(true);
        private byte c;
        private int d;

        /* compiled from: Unknown */
        public static final class a extends Builder<k, a> implements l {
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
                return this;
            }

            public a a(k kVar) {
                return kVar != k.a() ? this : this;
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                Throwable th;
                k kVar;
                k kVar2;
                try {
                    kVar2 = (k) k.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (kVar2 != null) {
                        a(kVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    kVar2 = (k) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    kVar = kVar2;
                    th = th3;
                }
                if (kVar != null) {
                    a(kVar);
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
            public /* synthetic */ MessageLite.Builder m96clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m97clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m98clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m99clone() {
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
                return new k((Builder) this);
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m100getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m101mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.f();
        }

        private k(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.c = (byte) -1;
            this.d = -1;
            f();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
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

        private k(Builder builder) {
            super(builder);
            this.c = (byte) -1;
            this.d = -1;
        }

        private k(boolean z) {
            this.c = (byte) -1;
            this.d = -1;
        }

        public static a a(k kVar) {
            return c().a(kVar);
        }

        public static k a() {
            return b;
        }

        public static a c() {
            return a.h();
        }

        private void f() {
        }

        public k b() {
            return b;
        }

        public a d() {
            return c();
        }

        public a e() {
            return a(this);
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<k> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = this.d;
            if (i != -1) {
                return i;
            }
            this.d = 0;
            return 0;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.c;
            if (b == (byte) -1) {
                this.c = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return d();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return e();
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            getSerializedSize();
        }
    }

    /* compiled from: Unknown */
    public interface n extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class m extends GeneratedMessageLite implements n {
        public static Parser<m> a = new bj();
        private static final m b = new m(true);
        private int c;
        private int d;
        private int e;
        private int f;
        private List<ByteString> g;
        private byte h;
        private int i;

        /* compiled from: Unknown */
        public static final class a extends Builder<m, a> implements n {
            private int a;
            private int b;
            private int c;
            private int d;
            private List<ByteString> e = Collections.emptyList();

            private a() {
                g();
            }

            private void g() {
            }

            private static a h() {
                return new a();
            }

            private void i() {
                if ((this.a & 8) != 8) {
                    this.e = new ArrayList(this.e);
                    this.a |= 8;
                }
            }

            public a a() {
                super.clear();
                this.b = 0;
                this.a &= -2;
                this.c = 0;
                this.a &= -3;
                this.d = 0;
                this.a &= -5;
                this.e = Collections.emptyList();
                this.a &= -9;
                return this;
            }

            public a a(int i) {
                this.a |= 1;
                this.b = i;
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
                    b(mVar.f());
                }
                if (mVar.g()) {
                    c(mVar.h());
                }
                if (!mVar.g.isEmpty()) {
                    if (this.e.isEmpty()) {
                        this.e = mVar.g;
                        this.a &= -9;
                    } else {
                        i();
                        this.e.addAll(mVar.g);
                    }
                }
                return this;
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                Throwable th;
                m mVar;
                m mVar2;
                try {
                    mVar2 = (m) m.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (mVar2 != null) {
                        a(mVar2);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    mVar2 = (m) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    mVar = mVar2;
                    th = th3;
                }
                if (mVar != null) {
                    a(mVar);
                }
                throw th;
            }

            public a b() {
                return h().a(e());
            }

            public a b(int i) {
                this.a |= 2;
                this.c = i;
                return this;
            }

            public /* synthetic */ MessageLite build() {
                return d();
            }

            public /* synthetic */ MessageLite buildPartial() {
                return e();
            }

            public a c(int i) {
                this.a |= 4;
                this.d = i;
                return this;
            }

            public m c() {
                return m.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m102clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m103clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m104clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m105clone() {
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
                if ((this.a & 8) == 8) {
                    this.e = Collections.unmodifiableList(this.e);
                    this.a &= -9;
                }
                mVar.g = this.e;
                mVar.c = i2;
                return mVar;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m106getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m107mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.m();
        }

        private m(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.h = (byte) -1;
            this.i = -1;
            m();
            int i = 0;
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
                            break;
                        case 8:
                            this.c |= 1;
                            this.d = codedInputStream.readInt32();
                            break;
                        case 16:
                            this.c |= 2;
                            this.e = codedInputStream.readInt32();
                            break;
                        case 24:
                            this.c |= 4;
                            this.f = codedInputStream.readInt32();
                            break;
                        case 34:
                            if ((i & 8) != 8) {
                                this.g = new ArrayList();
                                i |= 8;
                            }
                            this.g.add(codedInputStream.readBytes());
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
                    if ((i & 8) == 8) {
                        this.g = Collections.unmodifiableList(this.g);
                    }
                    makeExtensionsImmutable();
                }
            }
            if ((i & 8) == 8) {
                this.g = Collections.unmodifiableList(this.g);
            }
            makeExtensionsImmutable();
        }

        private m(Builder builder) {
            super(builder);
            this.h = (byte) -1;
            this.i = -1;
        }

        private m(boolean z) {
            this.h = (byte) -1;
            this.i = -1;
        }

        public static a a(m mVar) {
            return j().a(mVar);
        }

        public static m a() {
            return b;
        }

        public static a j() {
            return a.h();
        }

        private void m() {
            this.d = 0;
            this.e = 0;
            this.f = 0;
            this.g = Collections.emptyList();
        }

        public m b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public int d() {
            return this.d;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public int f() {
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
            int i2 = this.i;
            if (i2 != -1) {
                return i2;
            }
            i2 = (this.c & 1) != 1 ? 0 : CodedOutputStream.computeInt32Size(1, this.d) + 0;
            if ((this.c & 2) == 2) {
                i2 += CodedOutputStream.computeInt32Size(2, this.e);
            }
            int computeInt32Size = (this.c & 4) != 4 ? i2 : i2 + CodedOutputStream.computeInt32Size(3, this.f);
            int i3 = 0;
            while (i < this.g.size()) {
                i++;
                i3 = CodedOutputStream.computeBytesSizeNoTag((ByteString) this.g.get(i)) + i3;
            }
            i2 = (computeInt32Size + i3) + (i().size() * 1);
            this.i = i2;
            return i2;
        }

        public int h() {
            return this.f;
        }

        public List<ByteString> i() {
            return this.g;
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

        public a k() {
            return j();
        }

        public a l() {
            return a(this);
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return k();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return l();
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            getSerializedSize();
            if ((this.c & 1) == 1) {
                codedOutputStream.writeInt32(1, this.d);
            }
            if ((this.c & 2) == 2) {
                codedOutputStream.writeInt32(2, this.e);
            }
            if ((this.c & 4) == 4) {
                codedOutputStream.writeInt32(3, this.f);
            }
            for (int i = 0; i < this.g.size(); i++) {
                codedOutputStream.writeBytes(4, (ByteString) this.g.get(i));
            }
        }
    }

    /* compiled from: Unknown */
    public interface p extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class o extends GeneratedMessageLite implements p {
        public static Parser<o> a = new bk();
        private static final o b = new o(true);
        private int c;
        private ByteString d;
        private boolean e;
        private List<ab> f;
        private aa g;
        private ByteString h;
        private boolean i;
        private List<a> j;
        private List<ByteString> k;
        private List<ByteString> l;
        private ad m;
        private m n;
        private byte o;
        private int p;

        /* compiled from: Unknown */
        public static final class a extends Builder<o, a> implements p {
            private int a;
            private ByteString b = ByteString.EMPTY;
            private boolean c;
            private List<ab> d = Collections.emptyList();
            private aa e = aa.VERBOSE;
            private ByteString f = ByteString.EMPTY;
            private boolean g;
            private List<a> h = Collections.emptyList();
            private List<ByteString> i = Collections.emptyList();
            private List<ByteString> j = Collections.emptyList();
            private ad k = ad.a();
            private m l = m.a();

            private a() {
                i();
            }

            private void i() {
            }

            private static a j() {
                return new a();
            }

            private void k() {
                if ((this.a & 4) != 4) {
                    this.d = new ArrayList(this.d);
                    this.a |= 4;
                }
            }

            private void l() {
                if ((this.a & 64) != 64) {
                    this.h = new ArrayList(this.h);
                    this.a |= 64;
                }
            }

            private void m() {
                if ((this.a & 128) != 128) {
                    this.i = new ArrayList(this.i);
                    this.a |= 128;
                }
            }

            private void n() {
                if ((this.a & 256) != 256) {
                    this.j = new ArrayList(this.j);
                    this.a |= 256;
                }
            }

            public a a() {
                super.clear();
                this.b = ByteString.EMPTY;
                this.a &= -2;
                this.c = false;
                this.a &= -3;
                this.d = Collections.emptyList();
                this.a &= -5;
                this.e = aa.VERBOSE;
                this.a &= -9;
                this.f = ByteString.EMPTY;
                this.a &= -17;
                this.g = false;
                this.a &= -33;
                this.h = Collections.emptyList();
                this.a &= -65;
                this.i = Collections.emptyList();
                this.a &= -129;
                this.j = Collections.emptyList();
                this.a &= -257;
                this.k = ad.a();
                this.a &= -513;
                this.l = m.a();
                this.a &= -1025;
                return this;
            }

            public a a(aa aaVar) {
                if (aaVar != null) {
                    this.a |= 8;
                    this.e = aaVar;
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(a aVar) {
                k();
                this.d.add(aVar.d());
                return this;
            }

            public a a(ad adVar) {
                if ((this.a & 512) == 512 && this.k != ad.a()) {
                    this.k = ad.a(this.k).a(adVar).e();
                } else {
                    this.k = adVar;
                }
                this.a |= 512;
                return this;
            }

            public a a(a aVar) {
                this.l = aVar.d();
                this.a |= 1024;
                return this;
            }

            public a a(m mVar) {
                if ((this.a & 1024) == 1024 && this.l != m.a()) {
                    this.l = m.a(this.l).a(mVar).e();
                } else {
                    this.l = mVar;
                }
                this.a |= 1024;
                return this;
            }

            public a a(o oVar) {
                if (oVar == o.a()) {
                    return this;
                }
                if (oVar.c()) {
                    a(oVar.d());
                }
                if (oVar.e()) {
                    a(oVar.f());
                }
                if (!oVar.f.isEmpty()) {
                    if (this.d.isEmpty()) {
                        this.d = oVar.f;
                        this.a &= -5;
                    } else {
                        k();
                        this.d.addAll(oVar.f);
                    }
                }
                if (oVar.i()) {
                    a(oVar.j());
                }
                if (oVar.k()) {
                    b(oVar.l());
                }
                if (oVar.m()) {
                    b(oVar.n());
                }
                if (!oVar.j.isEmpty()) {
                    if (this.h.isEmpty()) {
                        this.h = oVar.j;
                        this.a &= -65;
                    } else {
                        l();
                        this.h.addAll(oVar.j);
                    }
                }
                if (!oVar.k.isEmpty()) {
                    if (this.i.isEmpty()) {
                        this.i = oVar.k;
                        this.a &= -129;
                    } else {
                        m();
                        this.i.addAll(oVar.k);
                    }
                }
                if (!oVar.l.isEmpty()) {
                    if (this.j.isEmpty()) {
                        this.j = oVar.l;
                        this.a &= -257;
                    } else {
                        n();
                        this.j.addAll(oVar.l);
                    }
                }
                if (oVar.r()) {
                    a(oVar.s());
                }
                if (oVar.t()) {
                    a(oVar.u());
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
                o oVar;
                Throwable th;
                o oVar2;
                try {
                    oVar = (o) o.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (oVar != null) {
                        a(oVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    oVar = (o) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    oVar2 = oVar;
                    th = th3;
                }
                if (oVar2 != null) {
                    a(oVar2);
                }
                throw th;
            }

            public a a(boolean z) {
                this.a |= 2;
                this.c = z;
                return this;
            }

            public a b() {
                return j().a(e());
            }

            public a b(ByteString byteString) {
                if (byteString != null) {
                    this.a |= 16;
                    this.f = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a b(boolean z) {
                this.a |= 32;
                this.g = z;
                return this;
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
            public /* synthetic */ MessageLite.Builder m108clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m109clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m110clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m111clone() {
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
                int i = this.a;
                int i2 = 0;
                if ((i & 1) == 1) {
                    i2 = 1;
                }
                oVar.d = this.b;
                if ((i & 2) == 2) {
                    i2 |= 2;
                }
                oVar.e = this.c;
                if ((this.a & 4) == 4) {
                    this.d = Collections.unmodifiableList(this.d);
                    this.a &= -5;
                }
                oVar.f = this.d;
                if ((i & 8) == 8) {
                    i2 |= 4;
                }
                oVar.g = this.e;
                if ((i & 16) == 16) {
                    i2 |= 8;
                }
                oVar.h = this.f;
                if ((i & 32) == 32) {
                    i2 |= 16;
                }
                oVar.i = this.g;
                if ((this.a & 64) == 64) {
                    this.h = Collections.unmodifiableList(this.h);
                    this.a &= -65;
                }
                oVar.j = this.h;
                if ((this.a & 128) == 128) {
                    this.i = Collections.unmodifiableList(this.i);
                    this.a &= -129;
                }
                oVar.k = this.i;
                if ((this.a & 256) == 256) {
                    this.j = Collections.unmodifiableList(this.j);
                    this.a &= -257;
                }
                oVar.l = this.j;
                if ((i & 512) == 512) {
                    i2 |= 32;
                }
                oVar.m = this.k;
                if ((i & 1024) == 1024) {
                    i2 |= 64;
                }
                oVar.n = this.l;
                oVar.c = i2;
                return oVar;
            }

            public boolean f() {
                return (this.a & 512) == 512;
            }

            public ad g() {
                return this.k;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m112getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return !f() || g().isInitialized();
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((o) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m113mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.y();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private o(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            InvalidProtocolBufferException invalidProtocolBufferException;
            IOException iOException;
            Object obj = null;
            this.o = (byte) -1;
            this.p = -1;
            y();
            int i = 0;
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
                        case 16:
                            this.c |= 2;
                            this.e = codedInputStream.readBool();
                            break;
                        case 26:
                            if ((i & 4) == 4) {
                                readTag = i;
                            } else {
                                this.f = new ArrayList();
                                readTag = i | 4;
                            }
                            try {
                                this.f.add(codedInputStream.readMessage(ab.a, extensionRegistryLite));
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
                        case 32:
                            aa a = aa.a(codedInputStream.readEnum());
                            if (a != null) {
                                this.c |= 4;
                                this.g = a;
                                break;
                            }
                            break;
                        case MotionEventCompat.AXIS_GENERIC_11 /*42*/:
                            this.c |= 8;
                            this.h = codedInputStream.readBytes();
                            break;
                        case HwRcsCommonObject.BLACKLIST_MSG_VIDEO_TYPE /*48*/:
                            this.c |= 16;
                            this.i = codedInputStream.readBool();
                            break;
                        case 58:
                            if ((i & 64) == 64) {
                                readTag = i;
                            } else {
                                this.j = new ArrayList();
                                readTag = i | 64;
                            }
                            this.j.add(codedInputStream.readMessage(a.a, extensionRegistryLite));
                            break;
                        case Events.E_ANTISPAM_VIEW_CALL /*66*/:
                            if ((i & 128) == 128) {
                                readTag = i;
                            } else {
                                this.k = new ArrayList();
                                readTag = i | 128;
                            }
                            this.k.add(codedInputStream.readBytes());
                            break;
                        case 74:
                            if ((i & 256) == 256) {
                                readTag = i;
                            } else {
                                this.l = new ArrayList();
                                readTag = i | 256;
                            }
                            this.l.add(codedInputStream.readBytes());
                            break;
                        case Events.E_ANTIVIRUS_SCAN /*82*/:
                            a g = (this.c & 32) != 32 ? null : this.m.g();
                            this.m = (ad) codedInputStream.readMessage(ad.a, extensionRegistryLite);
                            if (g != null) {
                                g.a(this.m);
                                this.m = g.e();
                            }
                            this.c |= 32;
                            break;
                        case 90:
                            a l = (this.c & 64) != 64 ? null : this.n.l();
                            this.n = (m) codedInputStream.readMessage(m.a, extensionRegistryLite);
                            if (l != null) {
                                l.a(this.n);
                                this.n = l.e();
                            }
                            this.c |= 64;
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
            if ((i & 4) == 4) {
                this.f = Collections.unmodifiableList(this.f);
            }
            if ((i & 64) == 64) {
                this.j = Collections.unmodifiableList(this.j);
            }
            if ((i & 128) == 128) {
                this.k = Collections.unmodifiableList(this.k);
            }
            if ((i & 256) == 256) {
                this.l = Collections.unmodifiableList(this.l);
            }
            makeExtensionsImmutable();
            return;
            throw new InvalidProtocolBufferException(iOException.getMessage()).setUnfinishedMessage(this);
            try {
                throw invalidProtocolBufferException.setUnfinishedMessage(this);
            } catch (Throwable th3) {
                th2 = th3;
                if ((i & 4) == 4) {
                    this.f = Collections.unmodifiableList(this.f);
                }
                if ((i & 64) == 64) {
                    this.j = Collections.unmodifiableList(this.j);
                }
                if ((i & 128) == 128) {
                    this.k = Collections.unmodifiableList(this.k);
                }
                if ((i & 256) == 256) {
                    this.l = Collections.unmodifiableList(this.l);
                }
                makeExtensionsImmutable();
                throw th2;
            }
        }

        private o(Builder builder) {
            super(builder);
            this.o = (byte) -1;
            this.p = -1;
        }

        private o(boolean z) {
            this.o = (byte) -1;
            this.p = -1;
        }

        public static a a(o oVar) {
            return v().a(oVar);
        }

        public static o a() {
            return b;
        }

        public static a v() {
            return a.j();
        }

        private void y() {
            this.d = ByteString.EMPTY;
            this.e = false;
            this.f = Collections.emptyList();
            this.g = aa.VERBOSE;
            this.h = ByteString.EMPTY;
            this.i = false;
            this.j = Collections.emptyList();
            this.k = Collections.emptyList();
            this.l = Collections.emptyList();
            this.m = ad.a();
            this.n = m.a();
        }

        public o b() {
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

        public boolean f() {
            return this.e;
        }

        public List<ab> g() {
            return this.f;
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<o> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = 0;
            int i2 = this.p;
            if (i2 != -1) {
                return i2;
            }
            int i3;
            i2 = (this.c & 1) != 1 ? 0 : CodedOutputStream.computeBytesSize(1, this.d) + 0;
            if ((this.c & 2) == 2) {
                i2 += CodedOutputStream.computeBoolSize(2, this.e);
            }
            int i4 = i2;
            for (i3 = 0; i3 < this.f.size(); i3++) {
                i4 += CodedOutputStream.computeMessageSize(3, (MessageLite) this.f.get(i3));
            }
            if ((this.c & 4) == 4) {
                i4 += CodedOutputStream.computeEnumSize(4, this.g.getNumber());
            }
            if ((this.c & 8) == 8) {
                i4 += CodedOutputStream.computeBytesSize(5, this.h);
            }
            if ((this.c & 16) == 16) {
                i4 += CodedOutputStream.computeBoolSize(6, this.i);
            }
            for (i3 = 0; i3 < this.j.size(); i3++) {
                i4 += CodedOutputStream.computeMessageSize(7, (MessageLite) this.j.get(i3));
            }
            int i5 = 0;
            for (i3 = 0; i3 < this.k.size(); i3++) {
                i5 += CodedOutputStream.computeBytesSizeNoTag((ByteString) this.k.get(i3));
            }
            i4 = (i4 + i5) + (p().size() * 1);
            i3 = 0;
            while (i < this.l.size()) {
                i++;
                i3 = CodedOutputStream.computeBytesSizeNoTag((ByteString) this.l.get(i)) + i3;
            }
            i2 = (i4 + i3) + (q().size() * 1);
            if ((this.c & 32) == 32) {
                i2 += CodedOutputStream.computeMessageSize(10, this.m);
            }
            if ((this.c & 64) == 64) {
                i2 += CodedOutputStream.computeMessageSize(11, this.n);
            }
            this.p = i2;
            return i2;
        }

        public int h() {
            return this.f.size();
        }

        public boolean i() {
            return (this.c & 4) == 4;
        }

        public final boolean isInitialized() {
            boolean z = false;
            byte b = this.o;
            if (b != (byte) -1) {
                if (b == (byte) 1) {
                    z = true;
                }
                return z;
            } else if (r() && !s().isInitialized()) {
                this.o = (byte) 0;
                return false;
            } else {
                this.o = (byte) 1;
                return true;
            }
        }

        public aa j() {
            return this.g;
        }

        public boolean k() {
            return (this.c & 8) == 8;
        }

        public ByteString l() {
            return this.h;
        }

        public boolean m() {
            return (this.c & 16) == 16;
        }

        public boolean n() {
            return this.i;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return w();
        }

        public List<a> o() {
            return this.j;
        }

        public List<ByteString> p() {
            return this.k;
        }

        public List<ByteString> q() {
            return this.l;
        }

        public boolean r() {
            return (this.c & 32) == 32;
        }

        public ad s() {
            return this.m;
        }

        public boolean t() {
            return (this.c & 64) == 64;
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return x();
        }

        public m u() {
            return this.n;
        }

        public a w() {
            return v();
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            int i;
            int i2 = 0;
            getSerializedSize();
            if ((this.c & 1) == 1) {
                codedOutputStream.writeBytes(1, this.d);
            }
            if ((this.c & 2) == 2) {
                codedOutputStream.writeBool(2, this.e);
            }
            for (i = 0; i < this.f.size(); i++) {
                codedOutputStream.writeMessage(3, (MessageLite) this.f.get(i));
            }
            if ((this.c & 4) == 4) {
                codedOutputStream.writeEnum(4, this.g.getNumber());
            }
            if ((this.c & 8) == 8) {
                codedOutputStream.writeBytes(5, this.h);
            }
            if ((this.c & 16) == 16) {
                codedOutputStream.writeBool(6, this.i);
            }
            for (i = 0; i < this.j.size(); i++) {
                codedOutputStream.writeMessage(7, (MessageLite) this.j.get(i));
            }
            for (i = 0; i < this.k.size(); i++) {
                codedOutputStream.writeBytes(8, (ByteString) this.k.get(i));
            }
            while (i2 < this.l.size()) {
                codedOutputStream.writeBytes(9, (ByteString) this.l.get(i2));
                i2++;
            }
            if ((this.c & 32) == 32) {
                codedOutputStream.writeMessage(10, this.m);
            }
            if ((this.c & 64) == 64) {
                codedOutputStream.writeMessage(11, this.n);
            }
        }

        public a x() {
            return a(this);
        }
    }

    /* compiled from: Unknown */
    public interface r extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class q extends GeneratedMessageLite implements r {
        public static Parser<q> a = new bl();
        private static final q b = new q(true);
        private int c;
        private o d;
        private g e;
        private u f;
        private s g;
        private c h;
        private e i;
        private i j;
        private w k;
        private y l;
        private k m;
        private byte n;
        private int o;

        /* compiled from: Unknown */
        public static final class a extends Builder<q, a> implements r {
            private int a;
            private o b = o.a();
            private g c = g.a();
            private u d = u.a();
            private s e = s.a();
            private c f = c.a();
            private e g = e.a();
            private i h = i.a();
            private w i = w.a();
            private y j = y.a();
            private k k = k.a();

            private a() {
                i();
            }

            private void i() {
            }

            private static a j() {
                return new a();
            }

            public a a() {
                super.clear();
                this.b = o.a();
                this.a &= -2;
                this.c = g.a();
                this.a &= -3;
                this.d = u.a();
                this.a &= -5;
                this.e = s.a();
                this.a &= -9;
                this.f = c.a();
                this.a &= -17;
                this.g = e.a();
                this.a &= -33;
                this.h = i.a();
                this.a &= -65;
                this.i = w.a();
                this.a &= -129;
                this.j = y.a();
                this.a &= -257;
                this.k = k.a();
                this.a &= -513;
                return this;
            }

            public a a(a aVar) {
                this.f = aVar.d();
                this.a |= 16;
                return this;
            }

            public a a(c cVar) {
                if ((this.a & 16) == 16 && this.f != c.a()) {
                    this.f = c.a(this.f).a(cVar).e();
                } else {
                    this.f = cVar;
                }
                this.a |= 16;
                return this;
            }

            public a a(e eVar) {
                if ((this.a & 32) == 32 && this.g != e.a()) {
                    this.g = e.a(this.g).a(eVar).e();
                } else {
                    this.g = eVar;
                }
                this.a |= 32;
                return this;
            }

            public a a(a aVar) {
                this.c = aVar.d();
                this.a |= 2;
                return this;
            }

            public a a(g gVar) {
                if ((this.a & 2) == 2 && this.c != g.a()) {
                    this.c = g.a(this.c).a(gVar).e();
                } else {
                    this.c = gVar;
                }
                this.a |= 2;
                return this;
            }

            public a a(i iVar) {
                if ((this.a & 64) == 64 && this.h != i.a()) {
                    this.h = i.a(this.h).a(iVar).e();
                } else {
                    this.h = iVar;
                }
                this.a |= 64;
                return this;
            }

            public a a(k kVar) {
                if ((this.a & 512) == 512 && this.k != k.a()) {
                    this.k = k.a(this.k).a(kVar).e();
                } else {
                    this.k = kVar;
                }
                this.a |= 512;
                return this;
            }

            public a a(a aVar) {
                this.b = aVar.d();
                this.a |= 1;
                return this;
            }

            public a a(o oVar) {
                if ((this.a & 1) == 1 && this.b != o.a()) {
                    this.b = o.a(this.b).a(oVar).e();
                } else {
                    this.b = oVar;
                }
                this.a |= 1;
                return this;
            }

            public a a(q qVar) {
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
                if (qVar.i()) {
                    a(qVar.j());
                }
                if (qVar.k()) {
                    a(qVar.l());
                }
                if (qVar.m()) {
                    a(qVar.n());
                }
                if (qVar.o()) {
                    a(qVar.p());
                }
                if (qVar.q()) {
                    a(qVar.r());
                }
                if (qVar.s()) {
                    a(qVar.t());
                }
                if (qVar.u()) {
                    a(qVar.v());
                }
                return this;
            }

            public a a(s sVar) {
                if ((this.a & 8) == 8 && this.e != s.a()) {
                    this.e = s.a(this.e).a(sVar).e();
                } else {
                    this.e = sVar;
                }
                this.a |= 8;
                return this;
            }

            public a a(u uVar) {
                if ((this.a & 4) == 4 && this.d != u.a()) {
                    this.d = u.a(this.d).a(uVar).e();
                } else {
                    this.d = uVar;
                }
                this.a |= 4;
                return this;
            }

            public a a(w wVar) {
                if ((this.a & 128) == 128 && this.i != w.a()) {
                    this.i = w.a(this.i).a(wVar).e();
                } else {
                    this.i = wVar;
                }
                this.a |= 128;
                return this;
            }

            public a a(y yVar) {
                if ((this.a & 256) == 256 && this.j != y.a()) {
                    this.j = y.a(this.j).a(yVar).e();
                } else {
                    this.j = yVar;
                }
                this.a |= 256;
                return this;
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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

            public a b() {
                return j().a(e());
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
            public /* synthetic */ MessageLite.Builder m114clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m115clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m116clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m117clone() {
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
                if ((i & 8) == 8) {
                    i2 |= 8;
                }
                qVar.g = this.e;
                if ((i & 16) == 16) {
                    i2 |= 16;
                }
                qVar.h = this.f;
                if ((i & 32) == 32) {
                    i2 |= 32;
                }
                qVar.i = this.g;
                if ((i & 64) == 64) {
                    i2 |= 64;
                }
                qVar.j = this.h;
                if ((i & 128) == 128) {
                    i2 |= 128;
                }
                qVar.k = this.i;
                if ((i & 256) == 256) {
                    i2 |= 256;
                }
                qVar.l = this.j;
                if ((i & 512) == 512) {
                    i2 |= 512;
                }
                qVar.m = this.k;
                qVar.c = i2;
                return qVar;
            }

            public boolean f() {
                return (this.a & 1) == 1;
            }

            public o g() {
                return this.b;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m118getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return !f() || g().isInitialized();
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((q) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m119mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.z();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private q(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.n = (byte) -1;
            this.o = -1;
            z();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            break;
                        case 18:
                            a x = (this.c & 1) != 1 ? null : this.d.x();
                            this.d = (o) codedInputStream.readMessage(o.a, extensionRegistryLite);
                            if (x != null) {
                                x.a(this.d);
                                this.d = x.e();
                            }
                            this.c |= 1;
                            break;
                        case 26:
                            a ac = (this.c & 2) != 2 ? null : this.e.ac();
                            this.e = (g) codedInputStream.readMessage(g.a, extensionRegistryLite);
                            if (ac != null) {
                                ac.a(this.e);
                                this.e = ac.e();
                            }
                            this.c |= 2;
                            break;
                        case 34:
                            a e = (this.c & 4) != 4 ? null : this.f.e();
                            this.f = (u) codedInputStream.readMessage(u.a, extensionRegistryLite);
                            if (e != null) {
                                e.a(this.f);
                                this.f = e.e();
                            }
                            this.c |= 4;
                            break;
                        case MotionEventCompat.AXIS_GENERIC_11 /*42*/:
                            a e2 = (this.c & 8) != 8 ? null : this.g.e();
                            this.g = (s) codedInputStream.readMessage(s.a, extensionRegistryLite);
                            if (e2 != null) {
                                e2.a(this.g);
                                this.g = e2.e();
                            }
                            this.c |= 8;
                            break;
                        case 50:
                            a g = (this.c & 16) != 16 ? null : this.h.g();
                            this.h = (c) codedInputStream.readMessage(c.a, extensionRegistryLite);
                            if (g != null) {
                                g.a(this.h);
                                this.h = g.e();
                            }
                            this.c |= 16;
                            break;
                        case 58:
                            a e3 = (this.c & 32) != 32 ? null : this.i.e();
                            this.i = (e) codedInputStream.readMessage(e.a, extensionRegistryLite);
                            if (e3 != null) {
                                e3.a(this.i);
                                this.i = e3.e();
                            }
                            this.c |= 32;
                            break;
                        case Events.E_ANTISPAM_VIEW_CALL /*66*/:
                            a e4 = (this.c & 64) != 64 ? null : this.j.e();
                            this.j = (i) codedInputStream.readMessage(i.a, extensionRegistryLite);
                            if (e4 != null) {
                                e4.a(this.j);
                                this.j = e4.e();
                            }
                            this.c |= 64;
                            break;
                        case 74:
                            a e5 = (this.c & 128) != 128 ? null : this.k.e();
                            this.k = (w) codedInputStream.readMessage(w.a, extensionRegistryLite);
                            if (e5 != null) {
                                e5.a(this.k);
                                this.k = e5.e();
                            }
                            this.c |= 128;
                            break;
                        case Events.E_ANTIVIRUS_SCAN /*82*/:
                            a e6 = (this.c & 256) != 256 ? null : this.l.e();
                            this.l = (y) codedInputStream.readMessage(y.a, extensionRegistryLite);
                            if (e6 != null) {
                                e6.a(this.l);
                                this.l = e6.e();
                            }
                            this.c |= 256;
                            break;
                        case 90:
                            a e7 = (this.c & 512) != 512 ? null : this.m.e();
                            this.m = (k) codedInputStream.readMessage(k.a, extensionRegistryLite);
                            if (e7 != null) {
                                e7.a(this.m);
                                this.m = e7.e();
                            }
                            this.c |= 512;
                            break;
                        default:
                            break;
                    }
                } catch (InvalidProtocolBufferException e8) {
                    throw e8.setUnfinishedMessage(this);
                } catch (IOException e9) {
                    throw new InvalidProtocolBufferException(e9.getMessage()).setUnfinishedMessage(this);
                } catch (Throwable th) {
                    makeExtensionsImmutable();
                }
            }
            makeExtensionsImmutable();
        }

        private q(Builder builder) {
            super(builder);
            this.n = (byte) -1;
            this.o = -1;
        }

        private q(boolean z) {
            this.n = (byte) -1;
            this.o = -1;
        }

        public static a a(q qVar) {
            return w().a(qVar);
        }

        public static q a() {
            return b;
        }

        public static q a(InputStream inputStream) {
            return (q) a.parseFrom(inputStream);
        }

        public static q a(byte[] bArr) {
            return (q) a.parseFrom(bArr);
        }

        public static a w() {
            return a.j();
        }

        private void z() {
            this.d = o.a();
            this.e = g.a();
            this.f = u.a();
            this.g = s.a();
            this.h = c.a();
            this.i = e.a();
            this.j = i.a();
            this.k = w.a();
            this.l = y.a();
            this.m = k.a();
        }

        public q b() {
            return b;
        }

        public boolean c() {
            return (this.c & 1) == 1;
        }

        public o d() {
            return this.d;
        }

        public boolean e() {
            return (this.c & 2) == 2;
        }

        public g f() {
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
            int i2 = this.o;
            if (i2 != -1) {
                return i2;
            }
            if ((this.c & 1) == 1) {
                i = CodedOutputStream.computeMessageSize(2, this.d) + 0;
            }
            if ((this.c & 2) == 2) {
                i += CodedOutputStream.computeMessageSize(3, this.e);
            }
            if ((this.c & 4) == 4) {
                i += CodedOutputStream.computeMessageSize(4, this.f);
            }
            if ((this.c & 8) == 8) {
                i += CodedOutputStream.computeMessageSize(5, this.g);
            }
            if ((this.c & 16) == 16) {
                i += CodedOutputStream.computeMessageSize(6, this.h);
            }
            if ((this.c & 32) == 32) {
                i += CodedOutputStream.computeMessageSize(7, this.i);
            }
            if ((this.c & 64) == 64) {
                i += CodedOutputStream.computeMessageSize(8, this.j);
            }
            if ((this.c & 128) == 128) {
                i += CodedOutputStream.computeMessageSize(9, this.k);
            }
            if ((this.c & 256) == 256) {
                i += CodedOutputStream.computeMessageSize(10, this.l);
            }
            if ((this.c & 512) == 512) {
                i += CodedOutputStream.computeMessageSize(11, this.m);
            }
            this.o = i;
            return i;
        }

        public u h() {
            return this.f;
        }

        public boolean i() {
            return (this.c & 8) == 8;
        }

        public final boolean isInitialized() {
            boolean z = false;
            byte b = this.n;
            if (b != (byte) -1) {
                if (b == (byte) 1) {
                    z = true;
                }
                return z;
            } else if (c() && !d().isInitialized()) {
                this.n = (byte) 0;
                return false;
            } else {
                this.n = (byte) 1;
                return true;
            }
        }

        public s j() {
            return this.g;
        }

        public boolean k() {
            return (this.c & 16) == 16;
        }

        public c l() {
            return this.h;
        }

        public boolean m() {
            return (this.c & 32) == 32;
        }

        public e n() {
            return this.i;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return x();
        }

        public boolean o() {
            return (this.c & 64) == 64;
        }

        public i p() {
            return this.j;
        }

        public boolean q() {
            return (this.c & 128) == 128;
        }

        public w r() {
            return this.k;
        }

        public boolean s() {
            return (this.c & 256) == 256;
        }

        public y t() {
            return this.l;
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return y();
        }

        public boolean u() {
            return (this.c & 512) == 512;
        }

        public k v() {
            return this.m;
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            getSerializedSize();
            if ((this.c & 1) == 1) {
                codedOutputStream.writeMessage(2, this.d);
            }
            if ((this.c & 2) == 2) {
                codedOutputStream.writeMessage(3, this.e);
            }
            if ((this.c & 4) == 4) {
                codedOutputStream.writeMessage(4, this.f);
            }
            if ((this.c & 8) == 8) {
                codedOutputStream.writeMessage(5, this.g);
            }
            if ((this.c & 16) == 16) {
                codedOutputStream.writeMessage(6, this.h);
            }
            if ((this.c & 32) == 32) {
                codedOutputStream.writeMessage(7, this.i);
            }
            if ((this.c & 64) == 64) {
                codedOutputStream.writeMessage(8, this.j);
            }
            if ((this.c & 128) == 128) {
                codedOutputStream.writeMessage(9, this.k);
            }
            if ((this.c & 256) == 256) {
                codedOutputStream.writeMessage(10, this.l);
            }
            if ((this.c & 512) == 512) {
                codedOutputStream.writeMessage(11, this.m);
            }
        }

        public a x() {
            return w();
        }

        public a y() {
            return a(this);
        }
    }

    /* compiled from: Unknown */
    public interface t extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class s extends GeneratedMessageLite implements t {
        public static Parser<s> a = new bm();
        private static final s b = new s(true);
        private byte c;
        private int d;

        /* compiled from: Unknown */
        public static final class a extends Builder<s, a> implements t {
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
                return this;
            }

            public a a(s sVar) {
                return sVar != s.a() ? this : this;
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

            public /* synthetic */ MessageLite build() {
                return d();
            }

            public /* synthetic */ MessageLite buildPartial() {
                return e();
            }

            public s c() {
                return s.a();
            }

            public /* synthetic */ Builder clear() {
                return a();
            }

            /* renamed from: clear */
            public /* synthetic */ MessageLite.Builder m120clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m121clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m122clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m123clone() {
                return b();
            }

            public s d() {
                MessageLite e = e();
                if (e.isInitialized()) {
                    return e;
                }
                throw AbstractMessageLite.Builder.newUninitializedMessageException(e);
            }

            public s e() {
                return new s((Builder) this);
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m124getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m125mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.f();
        }

        private s(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.c = (byte) -1;
            this.d = -1;
            f();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
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
            this.c = (byte) -1;
            this.d = -1;
        }

        private s(boolean z) {
            this.c = (byte) -1;
            this.d = -1;
        }

        public static a a(s sVar) {
            return c().a(sVar);
        }

        public static s a() {
            return b;
        }

        public static a c() {
            return a.h();
        }

        private void f() {
        }

        public s b() {
            return b;
        }

        public a d() {
            return c();
        }

        public a e() {
            return a(this);
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<s> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = this.d;
            if (i != -1) {
                return i;
            }
            this.d = 0;
            return 0;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.c;
            if (b == (byte) -1) {
                this.c = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return d();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return e();
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            getSerializedSize();
        }
    }

    /* compiled from: Unknown */
    public interface v extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class u extends GeneratedMessageLite implements v {
        public static Parser<u> a = new bn();
        private static final u b = new u(true);
        private byte c;
        private int d;

        /* compiled from: Unknown */
        public static final class a extends Builder<u, a> implements v {
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
                return this;
            }

            public a a(u uVar) {
                return uVar != u.a() ? this : this;
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
            public /* synthetic */ MessageLite.Builder m126clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m127clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m128clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m129clone() {
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
                return new u((Builder) this);
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m130getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m131mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.f();
        }

        private u(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.c = (byte) -1;
            this.d = -1;
            f();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
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

        private u(Builder builder) {
            super(builder);
            this.c = (byte) -1;
            this.d = -1;
        }

        private u(boolean z) {
            this.c = (byte) -1;
            this.d = -1;
        }

        public static a a(u uVar) {
            return c().a(uVar);
        }

        public static u a() {
            return b;
        }

        public static a c() {
            return a.h();
        }

        private void f() {
        }

        public u b() {
            return b;
        }

        public a d() {
            return c();
        }

        public a e() {
            return a(this);
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<u> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = this.d;
            if (i != -1) {
                return i;
            }
            this.d = 0;
            return 0;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.c;
            if (b == (byte) -1) {
                this.c = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return d();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return e();
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            getSerializedSize();
        }
    }

    /* compiled from: Unknown */
    public interface x extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class w extends GeneratedMessageLite implements x {
        public static Parser<w> a = new bo();
        private static final w b = new w(true);
        private byte c;
        private int d;

        /* compiled from: Unknown */
        public static final class a extends Builder<w, a> implements x {
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
                return this;
            }

            public a a(w wVar) {
                return wVar != w.a() ? this : this;
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
            public /* synthetic */ MessageLite.Builder m132clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m133clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m134clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m135clone() {
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
                return new w((Builder) this);
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m136getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m137mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.f();
        }

        private w(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.c = (byte) -1;
            this.d = -1;
            f();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
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

        private w(Builder builder) {
            super(builder);
            this.c = (byte) -1;
            this.d = -1;
        }

        private w(boolean z) {
            this.c = (byte) -1;
            this.d = -1;
        }

        public static a a(w wVar) {
            return c().a(wVar);
        }

        public static w a() {
            return b;
        }

        public static a c() {
            return a.h();
        }

        private void f() {
        }

        public w b() {
            return b;
        }

        public a d() {
            return c();
        }

        public a e() {
            return a(this);
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<w> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = this.d;
            if (i != -1) {
                return i;
            }
            this.d = 0;
            return 0;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.c;
            if (b == (byte) -1) {
                this.c = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return d();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return e();
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            getSerializedSize();
        }
    }

    /* compiled from: Unknown */
    public interface z extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class y extends GeneratedMessageLite implements z {
        public static Parser<y> a = new bp();
        private static final y b = new y(true);
        private byte c;
        private int d;

        /* compiled from: Unknown */
        public static final class a extends Builder<y, a> implements z {
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
                return this;
            }

            public a a(y yVar) {
                return yVar != y.a() ? this : this;
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                y yVar;
                Throwable th;
                y yVar2;
                try {
                    yVar = (y) y.a.parsePartialFrom(codedInputStream, extensionRegistryLite);
                    if (yVar != null) {
                        a(yVar);
                    }
                    return this;
                } catch (InvalidProtocolBufferException e) {
                    InvalidProtocolBufferException invalidProtocolBufferException = e;
                    yVar = (y) invalidProtocolBufferException.getUnfinishedMessage();
                    throw invalidProtocolBufferException;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    yVar2 = yVar;
                    th = th3;
                }
                if (yVar2 != null) {
                    a(yVar2);
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
            public /* synthetic */ MessageLite.Builder m138clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m139clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m140clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m141clone() {
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
                return new y((Builder) this);
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m142getDefaultInstanceForType() {
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
            public /* synthetic */ MessageLite.Builder m143mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }
        }

        static {
            b.f();
        }

        private y(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
            Object obj = null;
            this.c = (byte) -1;
            this.d = -1;
            f();
            while (obj == null) {
                try {
                    int readTag = codedInputStream.readTag();
                    switch (readTag) {
                        case 0:
                            obj = 1;
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

        private y(Builder builder) {
            super(builder);
            this.c = (byte) -1;
            this.d = -1;
        }

        private y(boolean z) {
            this.c = (byte) -1;
            this.d = -1;
        }

        public static a a(y yVar) {
            return c().a(yVar);
        }

        public static y a() {
            return b;
        }

        public static a c() {
            return a.h();
        }

        private void f() {
        }

        public y b() {
            return b;
        }

        public a d() {
            return c();
        }

        public a e() {
            return a(this);
        }

        public /* synthetic */ MessageLite getDefaultInstanceForType() {
            return b();
        }

        public Parser<y> getParserForType() {
            return a;
        }

        public int getSerializedSize() {
            int i = this.d;
            if (i != -1) {
                return i;
            }
            this.d = 0;
            return 0;
        }

        public final boolean isInitialized() {
            boolean z = true;
            byte b = this.c;
            if (b == (byte) -1) {
                this.c = (byte) 1;
                return true;
            }
            if (b != (byte) 1) {
                z = false;
            }
            return z;
        }

        public /* synthetic */ MessageLite.Builder newBuilderForType() {
            return d();
        }

        public /* synthetic */ MessageLite.Builder toBuilder() {
            return e();
        }

        protected Object writeReplace() {
            return super.writeReplace();
        }

        public void writeTo(CodedOutputStream codedOutputStream) {
            getSerializedSize();
        }
    }
}
