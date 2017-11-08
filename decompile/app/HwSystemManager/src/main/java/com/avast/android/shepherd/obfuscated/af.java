package com.avast.android.shepherd.obfuscated;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.GeneratedMessageLite.Builder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import com.google.protobuf.Parser;
import java.io.IOException;

/* compiled from: Unknown */
public final class af {

    /* compiled from: Unknown */
    public interface b extends MessageLiteOrBuilder {
    }

    /* compiled from: Unknown */
    public static final class a extends GeneratedMessageLite implements b {
        public static Parser<a> a = new ag();
        private static final a b = new a(true);
        private int c;
        private int d;
        private ByteString e;
        private byte f;
        private int g;

        /* compiled from: Unknown */
        public static final class a extends Builder<a, a> implements b {
            private int a;
            private int b;
            private ByteString c = ByteString.EMPTY;

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
                this.b = 0;
                this.a &= -2;
                this.c = ByteString.EMPTY;
                this.a &= -3;
                return this;
            }

            public a a(int i) {
                this.a |= 1;
                this.b = i;
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
                    this.a |= 2;
                    this.c = byteString;
                    return this;
                }
                throw new NullPointerException();
            }

            public a a(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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

            public a b() {
                return i().a(e());
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
            public /* synthetic */ MessageLite.Builder m144clear() {
                return a();
            }

            public /* synthetic */ AbstractMessageLite.Builder clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Builder m145clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ MessageLite.Builder m146clone() {
                return b();
            }

            /* renamed from: clone */
            public /* synthetic */ Object m147clone() {
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

            public boolean f() {
                return (this.a & 1) == 1;
            }

            public /* synthetic */ GeneratedMessageLite getDefaultInstanceForType() {
                return c();
            }

            /* renamed from: getDefaultInstanceForType */
            public /* synthetic */ MessageLite m148getDefaultInstanceForType() {
                return c();
            }

            public final boolean isInitialized() {
                return f();
            }

            public /* synthetic */ AbstractMessageLite.Builder mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
                return a(codedInputStream, extensionRegistryLite);
            }

            public /* synthetic */ Builder mergeFrom(GeneratedMessageLite generatedMessageLite) {
                return a((a) generatedMessageLite);
            }

            /* renamed from: mergeFrom */
            public /* synthetic */ MessageLite.Builder m149mergeFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
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
                        case 8:
                            this.c |= 1;
                            this.d = codedInputStream.readInt32();
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

        public static a a(ByteString byteString) {
            return (a) a.parseFrom(byteString);
        }

        public static a g() {
            return a.i();
        }

        private void j() {
            this.d = 0;
            this.e = ByteString.EMPTY;
        }

        public a b() {
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

        public ByteString f() {
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
                i = CodedOutputStream.computeInt32Size(1, this.d) + 0;
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
            boolean z = false;
            byte b = this.f;
            if (b != (byte) -1) {
                if (b == (byte) 1) {
                    z = true;
                }
                return z;
            } else if (c()) {
                this.f = (byte) 1;
                return true;
            } else {
                this.f = (byte) 0;
                return false;
            }
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
                codedOutputStream.writeInt32(1, this.d);
            }
            if ((this.c & 2) == 2) {
                codedOutputStream.writeBytes(2, this.e);
            }
        }
    }
}
